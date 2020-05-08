 package at.fhv.audioracer.simulator.world.impl;
 
 import at.fhv.audioracer.core.model.Car;
 import at.fhv.audioracer.core.model.ICarListener;
 import at.fhv.audioracer.simulator.world.SimulationController;
 
 public class CarClientListener implements ICarListener {
 	
 	@Override
 	public void onCarPositionChanged(Car car) {
		SimulationController.getInstance().getCamera()
				.updateCar(car.getCarId(), car.getPosition().getPosX(), car.getPosition().getPosY(), car.getDirection().getDirection());
 	}
 	
 }
