 package org.team751.subsystems;
 
 import edu.wpi.first.wpilibj.CANJaguar;
 import edu.wpi.first.wpilibj.can.CANTimeoutException;
 import edu.wpi.first.wpilibj.command.Subsystem;
import org.team751.commands.CommandBase;
 import org.team751.resources.CANJaguarIDs;
 
 /**
  * A subsystem for the pusher mechanism that pushes disks from the cow
  * into the shooter.
  * 
  * This only does anything if the cow is in position. It disables the cow
  * before extending and enables the cow once retracted.
  * 
  * @author Sam Crow
  */
 public class Pusher extends Subsystem {
     
 	/**
 	 * The power level (0 to 1) to use for the motor
 	 */
 	private static final double MOTOR_POWER = -0.3;
     
     /**
      * The Jaguar used to control the pusher. The two limit switches are
      * connected to it.
      */
     private CANJaguar jaguar;
     
     public Pusher() {
         
         try {
             jaguar = new CANJaguar(CANJaguarIDs.PUSHER);
         } catch (CANTimeoutException ex) {
             ex.printStackTrace();
         }
         
     }
     
     /**
      * Determine if the pusher is retracted enough to safely move the cow
      */
     public boolean isRetracted() {
         try {
             return !jaguar.getReverseLimitOK();
         } catch (CANTimeoutException ex) {
             ex.printStackTrace();
         }
         return false;
     }
 	
 	/**
 	 * Determine if the pusher is fully extended
 	 */
 	public boolean isExtended() {
 		try {
 			return !jaguar.getForwardLimitOK();
 		} catch (CANTimeoutException ex) {
 			ex.printStackTrace();
 		}
 		return false;
 	}
 	
 	/**
 	 * Extend the pusher to push a disk into the shooter. If the cow is not in
 	 * a safe position to extend the pusher, nothing will happen.
 	 */
 	public void push() {
 //		if(CommandBase.cow.isInPosition()) {
 			try {
 				//Set the motor to move forward. The Jaguar will detect that
 				//the limit switch is pressed and stop the motor by itself.
 				jaguar.setX(MOTOR_POWER);
 			} catch (CANTimeoutException ex) {
 				ex.printStackTrace();
 			}
 //		}
 //		else {
 //			System.err.println("Protection failure! Pusher commanded to extend "
 //					+ "when the cow is not in position.");
 //		}
 	}
 	
 	/**
 	 * Retract the pusher
 	 */
 	public void retract() {
 		try {
 			jaguar.setX(-MOTOR_POWER);
 		} catch (CANTimeoutException ex) {
 			ex.printStackTrace();
 		}
 	}
 
     public void initDefaultCommand() {
         
     }
 }
