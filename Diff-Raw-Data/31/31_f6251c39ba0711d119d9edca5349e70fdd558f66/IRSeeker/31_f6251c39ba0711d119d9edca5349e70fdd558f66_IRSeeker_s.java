 package infrared;
 
 import lejos.nxt.SensorPort;
 //import lejos.nxt.addon.IRSeeker;
 
 public class IRSeeker {
 	
 	private static final IRSeeker instance = new IRSeeker();
	private static lejos.nxt.addon.IRSeeker seeker; 
 	
 	/**
 	 * Creates a new touch sensor as a singleton object.
 	 */
 	private IRSeeker(){
		seeker = new lejos.nxt.addon.IRSeeker(SensorPort.S4);
		seeker.setAddress(0x10);
 	}
 	
 	public static IRSeeker getInstance(){
 		return instance;
 	}
 	
 	/**
 	 * @return	
 	 * 			The direction of the IR source (according to the documentation) (5 is straight ahead)
 	 */
 	public int getDirection(){
 		return seeker.getDirection();
 	}
 	
 	public int getValueInTheDir(){
 		int dir = IRSeeker.getInstance().getDirection();
 		int value = seeker.getSensorValue(dir/2);
 		if(dir%2 == 0){
 			value = (seeker.getSensorValue(dir/2)+seeker.getSensorValue(dir/2+1))/2;
 		}
 		return value;	
 	}
 	
 	public int getValue(int dir){
 		return seeker.getSensorValue(dir);
 	}
 	
 	public int getValueAhead(){
 		return IRSeeker.getInstance().getValue(3);
 	}
 
 }
