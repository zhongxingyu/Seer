 package org.usfirst.frc1318.minimike.shared;
 
 import org.usfirst.frc1318.generic.reference.GamePad;
 import org.usfirst.frc1318.generic.reference.PS2Controller;
 
 import edu.wpi.first.wpilibj.DigitalInput;
 
 public class MMSensors {
 	
	private static MMSensors instance; 
 	private GamePad gamePad;
 	private DigitalInput leftLimitSwitch;
 	private DigitalInput rightLimitSwitch;
 	
	private MMSensors(){
	}
	
	public static MMSensors getInstance(){
		if(instance == null){
			instance = new MMSensors();
		}
		return instance;
	}
	
 	//set gamepad
 	public void setGamePad(GamePad gamePad)
 	{
 		this.gamePad = gamePad;
 	}
 	
 	//get controller if initialized, else throw exception
 	public GamePad getGamePad() throws Exception
 	{
 		if(null != gamePad)
 		{
 			return gamePad;
 		}else{
 			throw new Exception("Gamepad not initialized");
 		}
 	}
 	
 	//set limit switches
 	public void setLimitSwitches(DigitalInput right, DigitalInput left)
 	{
 		this.leftLimitSwitch = left;
 		this.rightLimitSwitch = right;
 	}
 	
 	public int getLimitSwitchState()
 	{
 		//TODO this doesn't do anything yet.  add functionality if used
 		return 0;
 	}
 	
 	
 	
 	
 }
