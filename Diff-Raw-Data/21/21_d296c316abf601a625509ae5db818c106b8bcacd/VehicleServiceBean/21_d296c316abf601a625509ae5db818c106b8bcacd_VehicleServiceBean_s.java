 package com.lsy.vehicle.service.spi;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.lsy.vehicle.dao.ManufacturerDao;
 import com.lsy.vehicle.dao.VehicleDao;
 import com.lsy.vehicle.domain.EngineType;
 import com.lsy.vehicle.domain.Manufacturer;
 import com.lsy.vehicle.domain.Vehicle;
 import com.lsy.vehicle.service.VehicleService;
 
 @Service
 public class VehicleServiceBean implements VehicleService {
     
     @Autowired
     private VehicleDao vehicleDao;
     
     @Autowired
     private ManufacturerDao manufacturerDao;
 
     @Override
     public Vehicle getCheapestVehicle() {
         return vehicleDao.findCheapestVehicle();
     }
 
     @Override
     @Transactional(propagation=Propagation.REQUIRED)
     public List<Vehicle> getVehicleByManufacture(String name) {
         return vehicleDao.findVehicleByManufacturer(name);
     }
 
     @Override
     @Transactional(propagation=Propagation.SUPPORTS, readOnly = true)
     public List<Vehicle> getVehiclesByEngineType(EngineType... engineType) {
         List<Vehicle> vehicles = new LinkedList<>();
         for (EngineType type : engineType) {
             vehicles.addAll(vehicleDao.findVehiclesByEngineType(type));
         }
         
         return vehicles;
     }
 
     @Override
     @Transactional
     public Vehicle registerVehicle(Vehicle vehicle) {
         vehicleDao.create(vehicle);
         return vehicle;
     }
 
     @Override
     @Transactional(propagation=Propagation.REQUIRED)
     public Vehicle addVehicleToManufacturer(String manufacturerName, Vehicle vehicle) {
         Manufacturer manufacturer = manufacturerDao.findManufacturerByName(manufacturerName);
         if (vehicle.getId() != null) {
             vehicle = vehicleDao.update(vehicle);
         }
         manufacturer.addVehicle(vehicle);
         return vehicle;
     }
 
     @Override
     @Transactional(propagation=Propagation.SUPPORTS)
     public Vehicle getVehicleById(Long vehicleId) {
         return vehicleDao.find(vehicleId);
     }
 
 }
