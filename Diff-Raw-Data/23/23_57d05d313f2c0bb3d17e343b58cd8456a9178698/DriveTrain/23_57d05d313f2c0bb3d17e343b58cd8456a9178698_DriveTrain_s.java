 package storm2013.subsystems;
 
 import edu.wpi.first.wpilibj.Encoder;
 import edu.wpi.first.wpilibj.Gyro;
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.RobotDrive;
 import edu.wpi.first.wpilibj.command.Subsystem;
 import edu.wpi.first.wpilibj.livewindow.LiveWindow;
 import storm2013.RobotMap;
 import storm2013.commands.TankDrive;
 
 /**
  * The subsystem that controls the drive train of the robot. It contains various
  * methods that easily calculate the movement to make up for weird things in the robot,
  * and it contains references to all of the devices that the robot has associated with
  * the drive train.
  * @author Joe
  */
 
 public class DriveTrain extends Subsystem{
     
 //    private double ACCEL_RATE =1.7;
 //    private double DECEL_RATE = 1.7;
     
     Jaguar _left  = new Jaguar(RobotMap.PORT_MOTOR_DRIVE_LEFT),
            _right = new Jaguar(RobotMap.PORT_MOTOR_DRIVE_RIGHT);
     
 //    AccelerationLimiter _leftControl = new AccelerationLimiter(_left, ACCEL_RATE, DECEL_RATE);
 //    AccelerationLimiter _rightControl = new AccelerationLimiter(_right, ACCEL_RATE, DECEL_RATE);
     
     RobotDrive _drive = new RobotDrive(_left,_right);
     
     Encoder _leftEncoder =  new Encoder(RobotMap.PORT_ENCODER_LEFT_1, RobotMap.PORT_ENCODER_LEFT_2);
     Encoder _rightEncoder = new Encoder(RobotMap.PORT_ENCODER_RIGHT_1, RobotMap.PORT_ENCODER_RIGHT_2);
      
     Gyro _gyro = new Gyro(RobotMap.PORT_SENSOR_GYRO);
     
     /**
      * The constructor which simply enables the encoders and adds the gyro to the {@link LiveWindow}
      */
     public DriveTrain() {
 	
         _leftEncoder.start();
         _rightEncoder.start();
 	
 //        LiveWindow.addActuator("Drive Train", "Left Motor", _left);
 //        LiveWindow.addActuator("Drive Train", "Right Motor", _right);
         LiveWindow.addSensor  ("Drive Train", "Gyro", _gyro);
     }
     
     /**
      * Sets the default command for the drive train to run to be {@link TankDrive}.
      */
     public void initDefaultCommand() {
         setDefaultCommand(new TankDrive());
     }
     
     /**
      * Moves the robot based on a speed value and a turn value, kind of like using
      * a joystick with 2 axes on an arcade game.
      * @param speed The speed at which to drive. On a joystick, this would come from the forward/backward axis
      * @param turn The amount to turn. On a joystick, the left/right axis value.
      */
     public void arcadeDrive(double speed,double turn) {
         _drive.arcadeDrive(-speed, turn, false);
 //	_drive.tankDrive(-speed, -speed);
     }
     
     /**
      * Gives specific values to the left and right motors.
      * @param leftValue The value to output to the left motor
      * @param rightValue The value to output to the right motor
      */
     public void tankDrive(double leftValue, double rightValue){
         _drive.tankDrive(-leftValue, -rightValue);
     }
 
     /**
      * Gets the reading on the left encoder
      * @return The encoder value
      */
      public double getLeftDistance() {
         return _leftEncoder.get();
     }
 
      /**
       * Gets the reading on the right encoder. Negated because the 2 encoders face opposite directions
       * @return The encoder value
       */
     public double getRightDistance() {
         return -_rightEncoder.get();
     }
     
     /**
      * Resets the values of both encoders.
      */
     public void clearEncoder(){
         _leftEncoder.reset();
         _rightEncoder.reset();
     }
     
     /**
      * Resets the gyro value.
      */
     public void clearGyro(){
         _gyro.reset();
     }
     
     /**
      * Returns the gyro's angle. Negated due to the gyro being upside down
      * @return The angle the robot has turned
      */
     public double getGyroAngle(){
         return _gyro.getAngle();
     }
     
     public double getLeftSpeed(){
         return _left.get();
     }
     
     public double getRightSpeed(){
         return _right.get();
     }
 }
