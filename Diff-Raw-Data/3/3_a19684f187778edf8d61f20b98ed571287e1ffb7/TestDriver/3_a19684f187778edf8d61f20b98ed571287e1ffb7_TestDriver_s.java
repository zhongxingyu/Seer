 package edu.ntnu.ttk4145.recs.driver;
 
 import java.io.IOException;
 
 public class TestDriver extends Driver{
 
 	@Override
 	protected void buttonPressed(Call button, int floor) {
 		setButtonLamp(button,floor,true);
 		System.out.printf("TestDriver.buttonPressed(%s,%d)%n",button,floor);
 	}
 	
 	@Override
	protected void stopButtonPressed() {
 		setStopLamp(true);
 		System.out.println("Stop button pushed");
 	}
 	
 	@Override
 	protected void floorSensorTriggered(int floor, boolean arriving) {
 		setFloorIndicator(floor);
 		
 		if(floor == 0){
 			setSpeed(1000);
 		} else if (floor == 3){
 			setSpeed(-1000);
 		}
 		System.out.printf("Floor %d, %s%n",floor + 1,arriving ? "arrive" : "depart");
 	}
 
 	@Override
 	protected void obstructionSensorTriggered(boolean enabled) {
 		setDoorOpenLamp(enabled);
 		System.out.println("Obstruction switch flipped");
 	}
 	
 	static int speed = 2000;
 	
 	public static void main(String[] args) {
 		Driver driver = Driver.makeInstance(TestDriver.class);
 		driver.resetAllLamps();
 		driver.setSpeed(speed);
 		
 		driver.startCallbacks();
 		System.out.println("Started");
 		try {
 			System.in.read();
 		} catch (IOException e) {}
 		
 		
 		driver.stopCallbacks();
 	}
 }
