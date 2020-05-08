 package org.usfirst.frc1318.FRC2013.readers;
 
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.GenericHID.Hand;
 
 import org.usfirst.frc1318.FRC2013.reference.Joystick1Ref;
 import org.usfirst.frc1318.FRC2013.reference.PortRef;
 import org.usfirst.frc1318.FRC2013.shared.ReferenceData;
 import org.usfirst.frc1318.components.RobotComponentBase;
 import org.usfirst.frc1318.generic.controllers.JoystickFilter;
 
 public class Joystick1Reader extends RobotComponentBase {
 	Joystick joystickL;
 	Joystick joystickR;
 	
 	public void robotInit() {
 		joystickR = new Joystick(PortRef.JOYSTICK_R);
 		joystickL = new Joystick(PortRef.JOYSTICK_L);
 	}
 
 	public void teleopPeriodic() {
 		
 		
 		//Manual Tasks
 		ReferenceData.getInstance().getUserInputData().setLiftUp(joystickL.getRawButton(Joystick1Ref.LIFT_UP));
 		ReferenceData.getInstance().getUserInputData().setLiftDown(joystickL.getRawButton(Joystick1Ref.LIFT_DOWN));
 		ReferenceData.getInstance().getUserInputData().setShooterUp(joystickL.getRawButton(Joystick1Ref.SHOOTER_UP));
 		ReferenceData.getInstance().getUserInputData().setShooterDown(joystickL.getRawButton(Joystick1Ref.SHOOTER_DOWN));
 		ReferenceData.getInstance().getUserInputData().setShooterSpeedUp(joystickL.getRawButton(Joystick1Ref.SHOOTER_SPEED_UP));
 		ReferenceData.getInstance().getUserInputData().setShooterSpeedDown(joystickL.getRawButton(Joystick1Ref.SHOOTER_SPEED_DOWN));
 		ReferenceData.getInstance().getUserInputData().setShooterFire(joystickL.getRawButton(Joystick1Ref.SHOOTER_FIRE));
 		ReferenceData.getInstance().getUserInputData().setBothUp(joystickL.getRawButton(Joystick1Ref.SHOOTER_LIFTER_UP));
		ReferenceData.getInstance().getUserInputData().setBothDown(joystickL.getRawButton(Joystick1Ref.SHOOTER_LIFTER_DOWN));
 		
 		//Joysticks
 		double joystickX = JoystickFilter.applyLinearDeadBand(joystickL.getX(),0.1);
 		double joystickY = -JoystickFilter.applyLinearDeadBand(joystickL.getY(),0.1);
 
 		//Joysticks: finetuning on left and reg on right
 //		if(joystickX == 0){
 //			joystickX = DeadBand.applylinearDeadBand(joystickL.getX(), 0.1)/4;
 //		}
 //		if(joystickY == 0){
 //			joystickY = -DeadBand.applylinearDeadBand(joystickL.getY(), 0.1)/4;
 //		}
 		
 		//TODO X and Y were switched on hardware, switched in software
 		ReferenceData.getInstance().getUserInputData().setJoystickY(-joystickX);
 		ReferenceData.getInstance().getUserInputData().setJoystickX(-joystickY);
 
 		
 		//Auto Tasks
 		ReferenceData.getInstance().getUserInputData().setAutoTranslateLeft(joystickL.getRawButton(Joystick1Ref.AUTO_TRANSLATE_LEFT));
 		ReferenceData.getInstance().getUserInputData().setAutoFireAll(joystickL.getRawButton(Joystick1Ref.AUTO_FIREALL));
 		ReferenceData.getInstance().getUserInputData().setAutoLift(joystickL.getRawButton(Joystick1Ref.AUTO_LIFT));
 		ReferenceData.getInstance().getUserInputData().setAutoTranslateRight(joystickL.getRawButton(Joystick1Ref.AUTO_TRANSLATE_RIGHT));
 	}
 }
