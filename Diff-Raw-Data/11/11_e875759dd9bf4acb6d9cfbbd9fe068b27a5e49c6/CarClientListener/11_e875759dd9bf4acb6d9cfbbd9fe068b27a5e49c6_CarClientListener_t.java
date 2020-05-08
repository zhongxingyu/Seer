 package at.fhv.audioracer.simulator.world.impl;
 
 import at.fhv.audioracer.communication.world.message.UpdateCarMessage;
 import at.fhv.audioracer.core.model.Car;
 import at.fhv.audioracer.core.model.ICarListener;
 import at.fhv.audioracer.simulator.world.SimulationController;
 
 import com.esotericsoftware.kryonet.Client;
 
 public class CarClientListener implements ICarListener {
 	
 	@Override
 	public void onCarPositionChanged(Car car) {
 		Client camera = SimulationController.getInstance().getCamera();
		UpdateCarMessage msg = new UpdateCarMessage();
 		msg.carId = car.getCarId();
 		msg.direction = car.getDirection().getDirection();
 		msg.posX = car.getPosition().getPosX();
 		msg.posY = car.getPosition().getPosY();
 		camera.sendTCP(msg);
 	}
 	
 }
