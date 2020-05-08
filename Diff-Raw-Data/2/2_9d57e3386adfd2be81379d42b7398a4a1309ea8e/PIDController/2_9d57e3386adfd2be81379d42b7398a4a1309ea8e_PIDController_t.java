 package tweeter0830.pidcontrol;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import tweeter0830.TB661Driver.TB661Driver;
 import tweeter0830.pidcontrol.PID;
 import ioio.lib.api.IOIO;
 import ioio.lib.api.exception.ConnectionLostException;
 
 import android.util.Log;
 import tweeter0830.pidcontrol.SaturationModel;
 public class PIDController implements SaturationModel{
 	String LOGTAG = "PIDController";
 	
 	public PID internalPID_ = new PID();
 	private TB661Driver motorDriver_ = new TB661Driver();
 	
 	private SensorManager sm_;
 	private Sensor accelSensor_;
 	private Sensor magSensor_;
 	private OrientationListener OrientationListener;
 	
 	private float azOrientation_;
 	
 	private double[] motorSpeeds_ = new double[2];
 	private double forwardSpeed_ = 0;
 	
 	public PIDController( SensorManager sm){
 		//force our PID to use the simulateSaturation method on this object
 		internalPID_.attachSatModel(this);
 		//Get the sensor manager object
 		sm_ = sm;
 		//Get a sensor object for the accelerometer
 		accelSensor_ = sm_.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 		magSensor_ = sm_.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
 		OrientationListener = new OrientationListener();
 		boolean sensor1Flag = sm_.registerListener(OrientationListener, accelSensor_, SensorManager.SENSOR_DELAY_FASTEST);
 		boolean sensor2Flag = sm_.registerListener(OrientationListener, magSensor_, SensorManager.SENSOR_DELAY_FASTEST);
 		Log.d(LOGTAG, "Sensor1: "+ accelSensor_.getName()+"\t Sensor2: " + magSensor_.getName()+"\n");
 		Log.d(LOGTAG, "Sensor1Work?: "+ sensor1Flag+"\t Sensor1Work?: " + sensor2Flag+"\n");
 	}
 	
 	public void setSetpoint(double setpoint){
 		internalPID_.setSetpoint(setpoint);
 	}
 	
 	public void setPID(double kp, double ki, double kd){
 		internalPID_.setPID(kp,ki,kd,0,1,0);
 	}
 	
 	public void setPID(double kp, double ki, double kd, double filterCoef){
 		internalPID_.setPID(kp,ki,kd,filterCoef,1,0);
 	}
 	
 	public void setPID(double kp, double ki, double kd, double filterCoef, double beta, double windupFactor){
 		internalPID_.setPID(kp,ki,kd,filterCoef,beta,windupFactor);
 	}
 	
 	public void powerOn() throws ConnectionLostException{
 		motorDriver_.powerOn();
 	}
 	
 	public double getProcessVar(){
 		return internalPID_.getProcessVar();
 	}
 	public double getPIDOutput(){
 		return internalPID_.getCurrentOutput();
 	}
 	public void setMotor(int motorNum, int pin1Num, int pin2Num,
 			int pwmPinNum, int standbyPinNum,
 			int frequency, IOIO ioio) throws ConnectionLostException{
 		motorDriver_.setMotor(motorNum, pin1Num, pin2Num, pwmPinNum, standbyPinNum, frequency, ioio);
 	}
 	
 	public double simulateSaturation(double unsatOutput){
 		double[] motorSpeeds = new double[2];
 		motorSpeeds = mapPIDOutputToMotor(unsatOutput, forwardSpeed_);
 		if(Math.abs(motorSpeeds[0])<=.15 && Math.abs(motorSpeeds[1])<=.15)
 			return 0;
 		else if(unsatOutput>1)
 			return 1;
 		else if(unsatOutput<-1)
 			return -1;
 		else 
 			return unsatOutput;
 	}
 	
 	public boolean updateMotors(double speed) throws ConnectionLostException{
 		if( internalPID_.outputIsSet() ){
 			motorSpeeds_ = mapPIDOutputToMotor(internalPID_.outputUpdate(), speed);
 			motorDriver_.move(1,motorSpeeds_[0]);
 			motorDriver_.move(2,motorSpeeds_[1]);
 			return true;
 		}
 		else
 			return false;
 	}
 	
 	public void close(){
 		motorDriver_.close();
 		sm_.unregisterListener(OrientationListener);
 	}
 	
 	private class OrientationListener implements SensorEventListener{
 		private float[] accelVector_ = new float[3];
 		private float[] magVector_ = new float[3];
 		private long count_ = 0;
 		@Override
 		public void onAccuracyChanged(Sensor sensor, int accuracy){
 		}
 
 		@Override
		public synchronized void onSensorChanged(SensorEvent event) {
 			if( event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
 				accelVector_[0] = event.values[0];
 				accelVector_[1] = event.values[1];
 				accelVector_[2] = event.values[2];
 			}
 			else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
 			{
 				magVector_[0] = event.values[0];
 				magVector_[1] = event.values[1];
 				magVector_[2] = event.values[2]; 
 			}
 			if(magVector_[0]  != 0.0 && accelVector_[0] != 0.0){
 				azOrientation_ = getAzOrientation( accelVector_, magVector_);
 				internalPID_.updateProcessVar(azOrientation_, System.nanoTime());
 			}
 			if(count_%20==0)
 				Log.v(LOGTAG, "SensorName:"+event.sensor.getName() + "\tAccuracy: "+event.accuracy+"/n");
 				Log.v(LOGTAG, "AccelVals: " + accelVector_[0] +"\t"+ accelVector_[1] +"\t"+ accelVector_[2] +"\n");
 				Log.v(LOGTAG, "MagVals: " + magVector_[0] +"\t"+ magVector_[1] +"\t"+ magVector_[2] +"\n");
 			count_++;
 		}
 		
 		private float getAzOrientation( float[] accelArray, float[] magArray){
 			float[] rotMatrix = new float[9];
 			float[] magIncMatrix = new float[9];
 			float[] returnVals = new float[3];
 			
 			SensorManager.getRotationMatrix(rotMatrix, magIncMatrix, accelArray, magArray);
 			returnVals = SensorManager.getOrientation(rotMatrix, magIncMatrix);
 			if(count_%20==0)
 				Log.v(LOGTAG, "OrientationVals: " + returnVals[0] +"\t"+ returnVals[1] +"\t"+ returnVals[2] +"\n");
 			return returnVals[0];
 		}
 	}
 	
 	private double[] mapPIDOutputToMotor(double pidOutput, double speed){
 
 		double leftSpeed = speed - pidOutput/2;
 		double rightSpeed = speed + pidOutput/2;
 		double extraTurn;
 		double[] motorSpeeds = new double[2];
 		
 		if( rightSpeed > 1)
 		{
 			extraTurn = rightSpeed - 1;
 			rightSpeed = 1;
 			leftSpeed = leftSpeed - extraTurn;
 		} else if( leftSpeed > 1){
 			extraTurn = leftSpeed - 1;
 			leftSpeed = 1;
 			rightSpeed = rightSpeed - extraTurn;
 		}else if( rightSpeed < -1) {
 			extraTurn = rightSpeed + 1;
 			rightSpeed = -1;
 			leftSpeed = leftSpeed + extraTurn;
 		}else if( leftSpeed < -1) {
 			extraTurn = leftSpeed + 1;
 			leftSpeed = -1;
 			rightSpeed = rightSpeed + extraTurn;
 		}
 		motorSpeeds[0] = leftSpeed;
 		motorSpeeds[1] = rightSpeed;
 		return motorSpeeds;
 		
 	}
 }
