 /**
  *
  */
 package tweeter0830.TB661Driver;
 
 import ioio.lib.api.DigitalOutput;
 import ioio.lib.api.IOIO;
 import ioio.lib.api.PwmOutput;
 import ioio.lib.api.exception.ConnectionLostException;
 
 /**
  * @author Jacob Huffman
  *  This class is used to initialize and run one motor attached to a TB6612FNG motor driver
  *  It has the ability to initialize, run, brake, standby and power one motor. However,
  *  it doesn't take into account the fact that the TB6612FNG shares one standby wire
  *  between two motors. This class does not take that into account, so weird bugs may
  *  occur if two motors are attached on the same TB6612FNG and the standby function
  *  is used on one, but not two of the motors
  */
 public class TB661Driver{
 
 	/**
 	 *
 	 */
 	private IOIO ioio_;
 	private boolean closed_ = false;
 	private DigitalOutput standbyPin_ = null;
 	
 	private DigitalOutput motor1Out1_;
 	private DigitalOutput motor1Out2_;
 	private PwmOutput  motor1PWM_;
 
 	private DigitalOutput motor2Out1_;
 	private DigitalOutput motor2Out2_;
 	private PwmOutput  motor2PWM_;
 
 	//private ConnectionLostException exception_;
 	
	public TB661Driver(int motorNum, int pin1Num, int pin2Num,
 			int pwmPinNum, int standbyPinNum,
 			int frequency, IOIO ioio) throws ConnectionLostException{
 		if( closed_ )
 			return;
 		ioio_ = ioio;
 		
 		if( motorNum == 1 ){
 			motor1Out1_ = ioio_.openDigitalOutput(pin1Num);
 			motor1Out2_ = ioio_.openDigitalOutput(pin2Num);
 			motor1PWM_ =  ioio_.openPwmOutput(pwmPinNum, frequency);
 		}
 		if( motorNum == 2){
 			motor2Out1_ = ioio_.openDigitalOutput(pin1Num);
 			motor2Out2_ = ioio_.openDigitalOutput(pin2Num);
 			motor2PWM_ =  ioio_.openPwmOutput(pwmPinNum, frequency);
 		}
 		
 		if (standbyPin_== null)
 			standbyPin_ = ioio_.openDigitalOutput(standbyPinNum);
 	}
 
 	public void moveForward(int motorNum, double speed) throws ConnectionLostException{
 		if( closed_ )
 			return;
 		
 		if( motorNum == 1 || motorNum == 3 ){
 			motor1Out1_.write(true);
 			motor1Out2_.write(false);
 			motor1PWM_.setDutyCycle((float)constrainSpeed( speed ));
 		}
		else if( motorNum == 2 || motorNum == 3 ){
 			motor2Out1_.write(true);
 			motor2Out2_.write(false);
 			motor2PWM_.setDutyCycle((float)constrainSpeed( speed ));
 		}	
 	}
 
 	public void moveBackward(int motorNum, double speed) throws ConnectionLostException{
 		if( closed_ )
 			return;
 		
 		if( motorNum == 1 || motorNum == 3 ){
 			motor1Out1_.write(false);
 			motor1Out2_.write(true);
 			motor1PWM_.setDutyCycle((float)constrainSpeed( speed ));
 		}
		else if( motorNum == 2 || motorNum == 3 ){
 			motor2Out1_.write(false);
 			motor2Out2_.write(true);
 			motor2PWM_.setDutyCycle((float)constrainSpeed( speed ));
 		}	
 	}
 
 	public void move(int motorNum, double speed ) throws ConnectionLostException{
 		if( closed_ )
 			return;
 		if( speed < 0 )
 			moveBackward(motorNum, -1 * speed);
 		else if( speed >= 0 )
 			moveForward(motorNum, speed );
 	}
 
 	public void brake(int motorNum) throws ConnectionLostException{
 		if( closed_ )
 			return;
 		if( motorNum == 1 || motorNum == 3 ){
 			motor1Out1_.write(true);
 			motor1Out2_.write(true);
 			motor1PWM_.setDutyCycle(1);
 		}
		else if( motorNum == 2 || motorNum == 3 ){
 			motor2Out1_.write(true);
 			motor2Out2_.write(true);
 			motor2PWM_.setDutyCycle(1);
 		}	
 	}
 
 	public void standby() throws ConnectionLostException{
 		if( closed_ )
 			return;
 		standbyPin_.write(false);
 	}
 
 	public void powerOn() throws ConnectionLostException{
 		if( closed_ )
 			return;
 		standbyPin_.write(true);
 	}
 
 	public void close(){
 		if( closed_ )
 			return;
 		standbyPin_.close();
 		
 		motor1Out1_.close();
 		motor1Out2_.close();
 		motor1PWM_.close();
 		motor2Out1_.close();
 		motor2Out2_.close();
 		motor2PWM_.close();
 
 		closed_ =  true;
 	}
 
 	private double constrainSpeed( double speed ){
 		if( speed < 0 )
 			return 0.0;
 		else
 			return 1.0;
 	}
 }
