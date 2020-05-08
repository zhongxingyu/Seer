 package org.usfirst.frc1318.minimike.components;
 
 import org.usfirst.frc1318.components.RobotComponentBase;
 import org.usfirst.frc1318.generic.reference.GamePad;
 import org.usfirst.frc1318.generic.reference.PS2Controller;
 import org.usfirst.frc1318.minimike.shared.MMReferenceData;
 import org.usfirst.frc1318.minimike.shared.constants.MMButtonRef;
 import org.usfirst.frc1318.minimike.shared.constants.MMPortRef;
 import org.usfirst.frc1318.testbed.shared.TBSolenoidData;
 
 import edu.wpi.first.wpilibj.DriverStationLCD;
 import edu.wpi.first.wpilibj.Solenoid;
 
 public class MMGamePadReader extends RobotComponentBase{
 	private GamePad gamepad;
 	
 	public void robotInit() {
		MMReferenceData.getInstance().getMMSensors().setGamePad(PS2Controller.create(MMPortRef.GAMEPAD));
		
		try {
			gamepad = MMReferenceData.getInstance().getMMSensors().getGamePad();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
 	}
 	
 	public void teleopPeriodic() {
 		//update right button
 		if(gamepad.getRawButton(MMButtonRef.ROTATE_RIGHT)){
 			MMReferenceData.getInstance().getMMGamePadData().setRightButton(true);
 			//System.out.println("Right Button");
 
 		}else{
 			MMReferenceData.getInstance().getMMGamePadData().setRightButton(false);
 		}
 		
 		//update left button
 		if(gamepad.getRawButton(MMButtonRef.ROTATE_LEFT)){
 			MMReferenceData.getInstance().getMMGamePadData().setLeftButton(true);
 			//System.out.println("Left Button");
 		}else{
 			MMReferenceData.getInstance().getMMGamePadData().setLeftButton(false);
 		}
 	}
 }
