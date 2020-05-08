 package storm.modules;
 
 
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 import edu.wpi.first.wpilibj.*;
 import edu.wpi.first.wpilibj.CounterBase.EncodingType;
 import storm.RobotState;
 import storm.interfaces.IShooter;
 import storm.utility.Print;
 /**
  *
  * @author Storm
  */
 
 public class Shooter implements IShooter {
 
     //Joystick shootJoystick;
     SpeedController shooterMotor, transferMotor;
     DigitalInput ready, hallEffect;
     Counter counter;
     Joystick shootJoystick;
     boolean shooting,
 	    readyTripped;
     boolean btn7;
     double motorSpeed,
 	    calculatedMotorSpeed,
 	    wantedRPM,
 	    period,
 	    RPMcurrent,
 	    RPMdifference,
 	    RPMthreshold,
 	    RPMold;
     int state,
 	    timeDifference,
 	    currentTime,
 	    goodRangeCount;
        
     public Shooter(int shooterMotorChannel,int transferMotorChannel, int IRready, int hallEffectSensor) {
         
        shooterMotor = new Victor(shooterMotorChannel);
         transferMotor = new Victor(transferMotorChannel);
         ready = new DigitalInput(IRready);
         hallEffect = new DigitalInput(hallEffectSensor);
 	readyTripped = false;
         counter = new Counter(EncodingType.k1X, hallEffect, hallEffect, false);
         counter.clearDownSource();
         counter.setUpSourceEdge(true, false);
     }
     
     public void startShoot(double distance) {
 	shootJoystick = RobotState.joystickShoot;
 
 	motorSpeed = getMotorSpeed(distance);
         counter.start();
         state = 0;
         shooting = true;
 	goodRangeCount = 0;
     }
     
     long startTime = -1;
 
     public void doShoot() {
 	if (!shooting) return;
 	shooterMotor.set(motorSpeed);
 	checkRPM();
 	transferMotor.set(-1);
 	if (shootJoystick.getRawButton(7) && !btn7) {
 	    btn7 = true;
 	    state ++;
 	} else if (!shootJoystick.getRawButton(7) && btn7) {
 	    btn7 = false;
 	}
 	switch (state){
	    case 0: wantedRPM = 2000;
 		break;
	    case 1: wantedRPM = 3300;
 		break;
	    case 2: wantedRPM = 2200;
 		break;
	    case 3: wantedRPM = 3000;
 	}
 	Print.getInstance().setLine(2, "Motor Speed: " + motorSpeed);
 
         // set motor speed, check when ready, move ball into shooter, stop once IR sensor is clear
 	/*if (!shooting) return;
 	switch (state){
 	    case 0:
 		transferMotor.set(-1);
 		shooterMotor.set(motorSpeed);
 		startTime = System.currentTimeMillis();
 		state ++;
 		
 		break;
 	    case 1:
 		if (!ready.get() == true){
 		    transferMotor.set(0);
 		    state ++;}
 		break ;
 	    case 2:
 		if (checkRPM() == true){
 		    state ++;
 		}
 		break;
 	    case 3:
 		transferMotor.set(-1);
 		if (!ready.get() == false) {
 		    startTime = System.currentTimeMillis();
 		    state ++;
 		}
 		break;
 	    case 4:
 		if ((System.currentTimeMillis() - startTime) >= 2000){
 		    state ++;
 		}
 		break;
 	    case 5:
 		transferMotor.set(0);
 		shooterMotor.set(0);
 		shooting = false;
 		state = 0;
 		RobotState.BALL_CONTAINMENT_COUNT --;
 		break;
 	    default:
 		break;
 	}*/
     }
 
     private double getMotorSpeed(double distance) {
         //convert distance into rpm into motor speed value
 	wantedRPM = 333.33*distance + 850.63 ;
 	calculatedMotorSpeed = .0003*wantedRPM + 0.0457;
 	return calculatedMotorSpeed;
     }
     
     private boolean checkRPM(){
 	//check what the current RPM is
 	period = counter.getPeriod();
         RPMcurrent = 60/period;
 	RPMdifference = RPMold - RPMcurrent;
 	RPMold = RPMcurrent;
 	RPMthreshold = wantedRPM / 25;	
 	Print.getInstance().setLine(1, "RPM: " + RPMcurrent);
 	
 	if ((System.currentTimeMillis() - startTime) >= 3000)
 	{
 	    return true;
 	}	
 	if (RPMcurrent >= wantedRPM)
 	{
 	    if (RPMdifference > 0){
 		//do nothing
 	    }else {
 		motorSpeed += .0003*RPMdifference;
 		shooterMotor.set(motorSpeed);
 	    }
 	    
 	    
 	}else {
 	    if (RPMdifference < 0){
 		//do nothing
 	    }else {
 		motorSpeed += .0003*RPMdifference;
 		shooterMotor.set(motorSpeed);
 	    }
 	}
 	
 	if (Math.abs(RPMdifference) < RPMthreshold){
 	    goodRangeCount ++;
 	}else goodRangeCount = 0;
 	
 	if(goodRangeCount > 3){
 	    return true;
 	}else return false;
     }
 
     public boolean isShooting() {
         return shooting;
     }
 
 }
