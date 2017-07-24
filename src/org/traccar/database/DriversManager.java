/*
 * Copyright 2017 Anton Tananaev (anton@traccar.org)
 * Copyright 2017 Andrey Kunitsyn (andrey@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.database;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.traccar.model.Driver;
import org.traccar.model.BaseModel;

public class DriversManager extends ExtendedObjectManager {

    private final Map<String, Driver> driversByUniqueId = new ConcurrentHashMap<>();

    public DriversManager(DataManager dataManager) {
        super(dataManager, Driver.class);
    }

    @Override
    public void addItem(BaseModel item) throws SQLException {
        super.addItem(item);
        driversByUniqueId.put(((Driver) item).getUniqueId(), (Driver) item);
    }

    @Override
    protected void updateCachedItem(BaseModel item) {
        Driver driver = (Driver) item;
        Driver cachedDriver = (Driver) getById(driver.getId());
        cachedDriver.setName(driver.getName());
        if (!driver.getUniqueId().equals(cachedDriver.getUniqueId())) {
            driversByUniqueId.remove(cachedDriver.getUniqueId());
            cachedDriver.setUniqueId(driver.getUniqueId());
            driversByUniqueId.put(cachedDriver.getUniqueId(), cachedDriver);
        }
        cachedDriver.setAttributes(driver.getAttributes());
    }

    @Override
    protected void removeCachedItem(long driverId) {
        Driver cachedDriver = (Driver) getById(driverId);
        if (cachedDriver != null) {
            String driverUniqueId = cachedDriver.getUniqueId();
            removeCachedItem(driverId);
            driversByUniqueId.remove(driverUniqueId);
        }
    }

    public Driver getDriverByUniqueId(String uniqueId) {
        return driversByUniqueId.get(uniqueId);
    }
}
