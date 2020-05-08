 package com.ledomatic.server;
 
 import javax.persistence.EntityManager;
 
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.wideplay.warp.persist.Transactional;
 
 public class DeviceServiceImpl implements DeviceService {
 
     private Provider<EntityManager> entityManager;
 
     @Inject
     public DeviceServiceImpl(Provider<EntityManager> entityManager) {
         this.entityManager = entityManager;
     }
 
 	@Transactional
 	@Override
 	public Device getDevice(String id) {
		return entityManager.get().find(Device.class, id);
 	}
 
 	@Transactional
 	@Override
 	public void saveDevice(Device device) {
 		if (device.getId() == null) {
 			device.setId("L1");
 			entityManager.get().persist(device);
 		} else {
 			entityManager.get().merge(device);
 		}
 	}
 
 }
