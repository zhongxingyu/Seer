 package edu.ntnu.ttk4145.recs.driver;
 
 public abstract class Driver {
 
 	public static final int ELEV_DIR_UP = 0;
 	public static final int ELEV_DIR_DOWN = 1;
 	public static final int ELEV_DIR_COMMAND = 2;
 	public static final int ELEV_DIR_NONE = 2;
 	
 	static{
		System.loadLibrary("driver1");
 	}
 	
 	private static Driver instance = null;
 	
 	public static Driver makeInstance(Class<? extends Driver> driverClass){
 		try {
 			return instance = driverClass.newInstance();
 		} catch (InstantiationException e) {
 			throw new IllegalArgumentException(e);
 		} catch (IllegalAccessException e) {
 			throw new IllegalArgumentException(e);
 		}
 	}
 	
 	public static Driver getInstance(){
 		if(instance == null){
 			throw new IllegalStateException("instance is null, call Driver.makeInstance first.");
 		}
 		return instance;
 	}
 	
 	protected Driver(){
 		if(elev_init() == 1){
 			System.out.println("Elevator driver failed to initialize, exiting.");
 			System.exit(1);
 		}
 	}
 
 	private native int elev_init();
 
 	private native void elev_stop();
 	
 	private native void elev_setSpeed(int speed);
 		
 	private native void elev_setDoorOpenLamp(int value);
 		
 	private native void elev_setStopLamp(int value);
 		
 	private native void elev_setFloorIndicator(int floor);
 		
 	private native void elev_setButtonLamp(int lamp, int floor, int value);
 
 	private native void elev_resetAllLamps();
 		
 	private native int elev_getFloorSensorSignal();
 	
 	public void setSpeed(int speed){
 		elev_setSpeed(speed);
 	}
 	
 	public void setDoorOpenLamp(boolean on) {
 		elev_setDoorOpenLamp(on ? 1 : 0);
 	}
 	
 	public void setStopLamp(boolean on) {
 		elev_setStopLamp(on ? 1 : 0);
 	}
 	
 	public void setFloorIndicator(int floor) {
 		elev_setFloorIndicator(floor);
 	}
 	
 	public void setButtonLamp(Button direction, int floor, boolean on) {
 		elev_setButtonLamp(direction.cEnum,floor,on ? 1 : 0);
 	}
 
 	public void resetAllLamps() {
 		elev_resetAllLamps();
 	}
 	
 	public int getFloorSensorSignal() {
 		return elev_getFloorSensorSignal();
 	}
 
 	private void callback_buttons(int floor, int value) {
 		Button button = null;
 		switch(value){
 		case ELEV_DIR_UP:   
 			button = Button.UP;   
 			break;
 		case ELEV_DIR_DOWN: 
 			button = Button.DOWN; 
 			break;
 		case ELEV_DIR_COMMAND: 
 			button = Button.COMMAND; 
 			break;
 		default:
 			throw new IllegalArgumentException(String.format("%d is not a valid value (0,1,2)",value));
 		}
 		buttonPressed(button,floor);
 	}
 	
 	private void callback_sensor(int floor, int value) {
 		floorSensorTriggered(floor,value == 1);
 	}
 	
 	private void callback_stop(int floor, int value) {
 		stopButtonPressed();
 	}
 	
 	private void callback_obstruction(int floor, int value) {
 		obstructionSensorTriggered(value == 1);
 	}
 	
 	protected abstract void buttonPressed(Button button, int floor);
 	
 	protected abstract void floorSensorTriggered(int floor, boolean arriving);
 	
 	protected abstract void stopButtonPressed();
 	
 	protected abstract void obstructionSensorTriggered(boolean enabled);
 	
 	public static enum Button {
 		UP(ELEV_DIR_UP), DOWN(ELEV_DIR_DOWN), COMMAND(ELEV_DIR_COMMAND);
 
 		private int cEnum;
 		
 		private Button(int cEnum){
 			this.cEnum = cEnum;
 		}
 	}
 	
 	public static enum Direction {
 		UP, DOWN, NONE;
 	}
 }
