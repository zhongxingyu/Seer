 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package bt.main;
 
 import bt.control.BTMotor;
 import bt.storage.BTStorage;
 
 /**
  *
  * @author Dlock
  */
 public class BTDriveTrain {
     BTStorage storage;
     BTMotor left;
     BTMotor right;
     BTMotor left_2;
     BTMotor right_2;
     double leftSpeed = 0;
     double rightSpeed = 0;
     public BTDriveTrain(BTStorage store)
     {
         storage = store;
         left = new BTMotor(Constants.LEFT_MOTOR_PORT,true,true,store);
         right = new BTMotor(Constants.RIGHT_MOTOR_PORT,true,true,store);
         left_2 = new BTMotor(Constants.LEFT_2_MOTOR_PORT,true,true,store);
         right_2 = new BTMotor(Constants.RIGHT_2_MOTOR_PORT,true,true,store);
         
         //RampRate
         left.setBTVoltageRampRate(Constants.VOLTAGE_RAMP_RATE);
         left_2.setBTVoltageRampRate(Constants.VOLTAGE_RAMP_RATE);
         right.setBTVoltageRampRate(Constants.VOLTAGE_RAMP_RATE);
         right_2.setBTVoltageRampRate(Constants.VOLTAGE_RAMP_RATE);
     }
     
     public void update()
     {
         leftSpeed = storage.xbox.AXES[Constants.LEFT_STICK_UP_DOWN_PORT];
         rightSpeed = storage.xbox.AXES[Constants.RIGHT_STICK_UP_DOWN_PORT];
         
         leftSpeed = leftSpeed*12.0;
         rightSpeed = rightSpeed*12.0;
         
         storage.data.DRIVE_TRAIN_LEFT_VALUE = leftSpeed;
         
         left.setX(leftSpeed);
         left_2.setX(leftSpeed);
         right.setX(-rightSpeed);
         right_2.setX(-rightSpeed);
     }
 }
