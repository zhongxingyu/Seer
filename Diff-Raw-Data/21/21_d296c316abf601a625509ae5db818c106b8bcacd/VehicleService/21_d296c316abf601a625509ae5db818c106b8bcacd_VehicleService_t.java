 package com.lsy.vehicle.service;
 
 import java.util.List;
 
 import com.lsy.vehicle.domain.EngineType;
 import com.lsy.vehicle.domain.Vehicle;
 
 public interface VehicleService {
 
 	public Vehicle getCheapestVehicle();
 	
 	public List<Vehicle> getVehicleByManufacture(String name);
 	
 	public List<Vehicle> getVehiclesByEngineType(EngineType... engineType);
 	
 	public Vehicle getVehicleById(Long vehicleId);
 	
 	public Vehicle registerVehicle(Vehicle vehicle);
 
     public Vehicle addVehicleToManufacturer(String manufacturerName, Vehicle vehicle);

    public void delete(Long vehicleId);
     
 }
