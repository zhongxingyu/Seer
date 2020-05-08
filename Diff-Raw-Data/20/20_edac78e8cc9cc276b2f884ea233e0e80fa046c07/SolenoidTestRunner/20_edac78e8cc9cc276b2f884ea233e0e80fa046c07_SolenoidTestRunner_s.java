 package org.usfirst.frc1318.FRC2013.runners;
 
 import org.usfirst.frc1318.FRC2013.reference.PortRef;
 import org.usfirst.frc1318.FRC2013.shared.ReferenceData;
 import org.usfirst.frc1318.FRC2013.shared.SolenoidTestData;
 import org.usfirst.frc1318.components.RobotComponentBase;
 
 import edu.wpi.first.wpilibj.DoubleSolenoid;
 import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
 import edu.wpi.first.wpilibj.Timer;
 
 public class SolenoidTestRunner extends RobotComponentBase{
 	DoubleSolenoid fireSolenoid, armSolenoid, shooterSolenoid;
 	SolenoidTestData data;
 	Timer timer;
 	double onTime = 1.5;
 	double offTime =  2;
 	
 	public void RobotInit()
 	{
 		fireSolenoid = new DoubleSolenoid(PortRef.SHOOTER_FIRE_EXTEND, PortRef.SHOOTER_FIRE_RETRACT);
 		armSolenoid = new DoubleSolenoid(PortRef.LIFTER_EXTENDER_SOLENOID_PORT, PortRef.LIFTER_RETRACTOR_SOLENOID_PORT);
 		shooterSolenoid = new DoubleSolenoid(PortRef.SHOOTER_UP_SOLENOID, PortRef.SHOOTER_DOWN_SOLENOID);
 		data = ReferenceData.getInstance().getSolenoidTestData();
 		data.state = 0;
 		timer = new Timer();
 		timer.start();
 	}
 	
	public void TeleopPeriodoc()
 	{
 		
 		/*
 		 * state machine.  
 		 * even numbers set initial condition
 		 * odd numbers check if finished, turn off solenoid, reset data and 
 		 * 		incremnt sate counter 
 		 */
 		switch(data.state)
 		{
 		case 0:
 			data.startTime = timer.get();
 			fireSolenoid.set(Value.kForward);
 			data.state = 1;
 			break;
 		case 1:
 			if(timer.get() - data.startTime > onTime)
 			{
 				if (data.loopCounter > 3)
 				{
 					data.state = 2;
 					data.loopCounter = 0;
 					fireSolenoid.set(Value.kOff);
 				}
 				++data.loopCounter;
 				fireSolenoid.set(Value.kReverse);
 			}
 			break;
 		case 2:
 			data.startTime = timer.get();
 			armSolenoid.set(Value.kForward);
 			data.state = 3;
 			break;
 		case 3:
 			if(timer.get() - data.startTime > onTime)
 			{
 				if (data.loopCounter > 3)
 				{
 					data.state = 4;
 					data.loopCounter = 0;
 					armSolenoid.set(Value.kOff);
 				}
 				armSolenoid.set(Value.kReverse);
 			}
 		case 4:
 			data.startTime = timer.get();
 			shooterSolenoid.set(Value.kForward);
 			data.state = 5;
 			break;
 		case 5:
 			if(timer.get() - data.startTime > onTime)
 			{
 				if (data.loopCounter > 3)
 				{
 					data.state = 1;
 					data.loopCounter = 0;
 					shooterSolenoid.set(Value.kOff);
 				}
 				shooterSolenoid.set(Value.kReverse);
 			}
 			
 			
 		}//state
 	}//method
 }
