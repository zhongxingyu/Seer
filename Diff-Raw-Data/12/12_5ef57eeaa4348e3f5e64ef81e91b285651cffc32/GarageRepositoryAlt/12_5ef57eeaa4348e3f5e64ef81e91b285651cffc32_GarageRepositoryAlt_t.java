 package myTest.repository;
 
 import org.springframework.stereotype.Repository;
 
 import myTest.data.Car;
 import myTest.data.Garage;
 
 @Repository
 public class GarageRepositoryAlt implements GarageRepository {
 
 	public void park(Car car) {
		System.out.println("Parking " + car + " in alternative garage.");
 	}
 
 	public Car release(Car car) {
		System.out.println("Collecting car " + car + " from alternative garage.");
		return car;
 	}
 
 	public int availableSlots() {
 		return 0;
 	}
 
 	public void addGarage(Garage garage) {
 
 	}
 
 }
