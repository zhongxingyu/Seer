 package storm.modules;
 
 
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 import edu.wpi.first.wpilibj.Counter;
 import edu.wpi.first.wpilibj.CounterBase.EncodingType;
 import edu.wpi.first.wpilibj.DigitalInput;
 import edu.wpi.first.wpilibj.SpeedController;
 import edu.wpi.first.wpilibj.Victor;
 import storm.RobotState;
 import storm.interfaces.IShooter;
 /**
  *
  * @author Storm
  */
 
 public class Shooter implements IShooter {
 
     SpeedController shooterMotor, transferMotor;
     DigitalInput ready, hallEffect;
     Counter counter;
     boolean shooting, readyTripped;
     double motorSpeed, period, rpm;
     int state;
     EncodingType k1x;
        
     public Shooter(int shooterMotorChannel,int transferMotorChannel, int IRready, int hallEffectSensor) {
         
         shooterMotor = new Victor(shooterMotorChannel);
         transferMotor = new Victor(transferMotorChannel);
         ready = new DigitalInput(IRready);
         hallEffect = new DigitalInput(hallEffectSensor);
 	readyTripped = false;
         counter = new Counter(k1x, hallEffect, hallEffect, false);
         counter.clearDownSource();
         counter.setUpSourceEdge(true, false);
     }
     
    public void startShoot() {
         //find out speed motor needs, move ball until ready to shoot,and start shooting process
         counter.start();
         state = 0;
         shooting = true;
     }
 
    public void doShoot(double distance) {
         if (!shooting) return;
	motorSpeed = getMotorSpeed(distance);
 
         // set motor speed, check when ready, move ball into shooter, stop once IR sensor is clear
         shooterMotor.set(motorSpeed);
         if (shooterMotor.get() == motorSpeed){
           state ++;  
         }
         if (state == 1){
             transferMotor.set(1);        
         }
 	if (!ready.get() == true && !readyTripped) {
             RobotState.BALL_CONTAINMENT_COUNT --;
             readyTripped = true;
         }else if(!ready.get() == false && readyTripped){
             readyTripped = false;
         }
         if (!ready.get() == false){
             transferMotor.set(0);
             shooterMotor.set(0);
             shooting = false;
             state = 0;
         }
     }
 
     private double getMotorSpeed(double distance) {
         //convert distance from m/s into rpm into motor speed value
        
         period = counter.getPeriod();
         rpm = 60/period;
         distance = distance * 4;
         return distance;
     }
 
 }
