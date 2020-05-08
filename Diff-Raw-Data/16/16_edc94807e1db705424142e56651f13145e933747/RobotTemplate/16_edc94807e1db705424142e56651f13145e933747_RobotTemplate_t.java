 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.*;
 
 //Driver joystick
 class Driver {
     //Buttons
     static final int TRANS_TOGGLE_LOW = 7;
     static final int TRANS_TOGGLE_HIGH = 8;
     static final int ARCADE_TOGGLE = 1;
 
     //Axes
     static final int X_AXIS_LEFT = 1;
     static final int Y_AXIS_LEFT = 2;
     static final int X_AXIS_RIGHT = 3;
     static final int Y_AXIS_RIGHT = 4;
 }
 
 //Operator joystick
 class Operator {
     //Buttons
     static final int ELEVATOR_STATE_GROUND = 4;
     static final int ELEVATOR_STATE_ONE = 11;
     static final int ELEVATOR_STATE_TWO = 12;
     static final int ELEVATOR_STATE_THREE = 9;
     static final int ELEVATOR_STATE_FOUR = 10;
     static final int ELEVATOR_STATE_FIVE = 7;
     static final int ELEVATOR_STATE_SIX = 8;
     static final int ELEVATOR_STATE_FEED = 6;
     static final int ELEVATOR_MANUAL_TOGGLE = 5;
     static final int GRIPPER_TOGGLE = 1;
     static final int MINIBOT_RELEASE_ONE = 5;
     static final int MINIBOT_RELEASE_TWO = 6;
     
     static final int LIGHT_SELECTION = 2;
     static final int LIGHT_RED = 7;
     static final int LIGHT_WHITE = 9;
     static final int LIGHT_BLUE = 11;
     static final int LIGHT_OFF = 10;
 }
 
 //Enumeration of setpoints for different heights of the elevator
 class ElevatorSetpoint {
     static final double ground = 0;
     static final double posOne = 250;
     static final double posTwo = 620;
     static final double posThree = 2000;
     static final double posFour = 2360;
     static final double posFive = 3750;
     static final double posSix = 3900;
     static final double feed = 1865;
 }
 
 class ElbowState {
     static final int Horizontal = 0;
     static final int Middle = 1;
     static final int Vertical = 2;
 }
 
 //Enumeration of autonomous modes
 class AutonomousState {
     static final int Driving = 0;
     static final int Turning = 1;
     static final int Reset = 2;
     static final int Release = 3;
     static final int Done = 4;
     static final int Sleep = 5;
 }
 
 class Lights {
     static final int Red = 0;
     static final int White = 1;
     static final int Blue = 2;
     static final int Off = 3;
 }
 
 public class RobotTemplate extends IterativeRobot {
     //Practise robot or competition robot
     static final boolean PRACTISE_ROBOT = false;
     //Encoder rate at max speed in slow gear
     static final double SLOW_MAX_ENCODER_RATE = 750.0;
     //Encoder rate at max speed in fast gear
     static final double FAST_MAX_ENCODER_RATE = 1700.0;
     //Speed to set the elevator motor to
     static final double ELEVATOR_SPEED_UP = 1.0;
     static final double ELEVATOR_SPEED_DOWN = 0.75;
     //Max drive motor speed
     static final double MAX_DRIVE_SPEED = 1.0;
     //Encoder counts per metre travelled
     static final double COUNTS_PER_METRE = 500;
     //Number of elevator encoder counts
     static final int MAX_ELEVATOR_COUNTS = 2400;
     //Number of seconds to wait in teleoperated mode before the minibot is allowed to be deployed
     static final double MINIBOT_RELEASE_TIME = 110.0;
     //Number of seconds after the minibot drops before we send it out horizontally
     static final double MINIBOT_SERVO_DELAY = 0.5;
     //Tolerance for the gyro pid
     static final double GYRO_TOLERANCE = 5.0;
     //Delay between
     static final double AUTONOMOUS_RELEASE_DELAY = 0.5;
     //Print delay
     static final double PRINT_DELAY = 0.5;
     static final int AUTONOMOUS_DRIVE_COUNTS = 2600;
 
     //distance in inches for scoring/feeding
     static final double MIN_SCORING_DISTANCE = 30.0;
     static final double MAX_SCORING_DISTANCE = 36.0;
     static final double MIN_FEEDING_DISTANCE = 15.0;
     static final double MAX_FEEDING_DISTANCE = 20.0;
 
     static final double FLASH_TIME = 0.25;
 
     static final double ULTRASONIC_VOLTS_PER_INCH = 0.0098;
 
     //Driver station
     DriverStation ds = DriverStation.getInstance();
 
     //Joysticks
     Joystick stickDriver = new Joystick(1);
     Joystick stickOperator = new Joystick(2);
 
     //Compressor, switch is DI 10, spike is relay 1
     Compressor compressor = new Compressor(10, 1);
 
     Pneumatic lightsOne;
     Pneumatic lightsTwo;
 
     //Solenoids for main robot or practise robot
     Pneumatic transShift;
     Pneumatic elbowTop;
     Pneumatic elbowBottom;
     Pneumatic gripper;
 
     Pneumatic minibotRelease;
 
     //Gyro
     Gyro gyro = new Gyro(1);
     PIDOutputStorage gyroOutput = new PIDOutputStorage();
 
     //Jaguars
     Jaguar jagLeft = new Jaguar(1);
     Jaguar jagRight = new Jaguar(2);
 
     //Stores output from robotDrive
     OutputStorage storageLeft = new OutputStorage();
     OutputStorage storageRight = new OutputStorage();
 
     //DI 3 doesn't work
 
     //Victors
     Victor vicElevator = new Victor(3);
 
     Servo minibotServo = new Servo(4);
 
     //Encoders
     PIDEncoder encLeft;
     Encoder encNull;
     PIDEncoder encElevator;
     PIDEncoder encRight;
 
     DigitalInput elevatorLimit = new DigitalInput(8);
     DigitalInput minibotLimit = new DigitalInput(7);
     DigitalInput rightSensor = new DigitalInput(11);
     DigitalInput middleSensor = new DigitalInput(12);
     DigitalInput leftSensor = new DigitalInput(13);
 
     AnalogChannel ultrasonicSensor = new AnalogChannel(2);
 
     //Provides drive functions (arcade and tank drive)
     RobotDrive robotDrive = new RobotDrive(storageLeft, storageRight);
 
     //PIDs
     PIDController pidLeft;
     PIDController pidRight;
     PIDController pidGyro;
 
     boolean transState;
 
     //Toggle for manual or automated elevator control
     Toggle manualElevatorToggle = new Toggle(false);
     //Toggle for the gripper
     Toggle gripperToggle = new Toggle(false);
     //Toggle for arcade/tank drive
     Toggle arcadeToggle = new Toggle(true);
     Toggle minibotToggle = new Toggle(false);
     
     //State of elbow
     int elbowState;
     int lastElbowState;
 
     //The elevator setpoint, determined by which button on the operator joystick is pressed
     double elevatorSetpoint = ElevatorSetpoint.ground;
 
     //Runs when the robot is turned
     public void robotInit() {
         transState = false;
 
         if(!PRACTISE_ROBOT) {
             encLeft = new PIDEncoder(true, 3, 4, true);
             encNull = new Encoder(9, 14);
             encElevator = new PIDEncoder(false, 5, 6, true);
             encRight = new PIDEncoder(true, 1, 2, true);
         }
         else {
             encLeft = new PIDEncoder(true, 5, 6, true);
             encNull = new Encoder(3, 4);
             encElevator = new PIDEncoder(false, 7, 8);
             encRight = new PIDEncoder(true, 1, 2, true);
         }
 
         pidLeft = new PIDController(0.0, 0.0005, 0.0, encLeft, jagLeft, 0.005);
         pidRight = new PIDController(0.0, 0.0005, 0.0, encRight, jagRight, 0.005);
 
         pidGyro = new PIDController(0.0005, 0.0005, 0.0, gyro, gyroOutput, 0.005);
 
         //Initialize our pneumatics if we are using the practise robot or the real robot
         if(!PRACTISE_ROBOT) {
             transShift = new Pneumatic(new Solenoid(4));
             elbowTop = new Pneumatic(new Solenoid(3));
             elbowBottom = new Pneumatic(new Solenoid(2));
             gripper = new Pneumatic(new Solenoid(1));
             minibotRelease = new Pneumatic(new DoubleSolenoid(6, 7));
 
             lightsOne = new Pneumatic(new Relay(2));
             lightsTwo = new Pneumatic(new Relay(3));
         }
         else {
             transShift = new Pneumatic(new Relay(5));
             elbowTop = new Pneumatic(new DoubleSolenoid(3, 4));
             elbowBottom = new Pneumatic(new DoubleSolenoid(5, 6));
             gripper = new Pneumatic(new DoubleSolenoid(1, 2));
             minibotRelease = new Pneumatic(new DoubleSolenoid(7, 8));
 
             lightsOne = new Pneumatic(new Relay(6));
             lightsTwo = new Pneumatic(new Relay(8));
         }
 
         //Start our encoders
         encRight.start();
         encLeft.start();
         encElevator.start();
 
         //Start our elevator encoder at 0
         encElevator.reset();
 
         //Input/output range for left encoder/motors
         pidLeft.setInputRange(-SLOW_MAX_ENCODER_RATE, SLOW_MAX_ENCODER_RATE);
         pidLeft.setOutputRange(-MAX_DRIVE_SPEED, MAX_DRIVE_SPEED);
 
         //Input/output range for right encoder/motors
         pidRight.setInputRange(-SLOW_MAX_ENCODER_RATE, SLOW_MAX_ENCODER_RATE);
         pidRight.setOutputRange(-MAX_DRIVE_SPEED, MAX_DRIVE_SPEED);
 
         pidGyro.enable();
 
         //Input/output range for the gyro PID
         pidGyro.setInputRange(-360.0, 360.0);
         pidGyro.setOutputRange(-0.5, 0.5);
 
         //Start the compressor
         compressor.start();
     }
 
     //Runs at the beginning of disabled period
     public void disabledInit() {
         //Disable PIDs
         pidLeft.disable();
         pidRight.disable();
         pidGyro.disable();
     }
     
     //Runs periodically during disabled period
     public void disabledPeriodic() {
         //Call our print function with the current mode
         print("Disabled");
     }
 
     //List of autonomous steps
     Step stepList[] = null;
 
     //Iterates through each step
     int stepIndex;
 
     boolean doNothing;
     boolean trackLine;
     boolean heightOne;
     boolean heightTwo;
     boolean heightThree;
     boolean staggeredPeg;
     boolean releaseTube;
     boolean reverse;
     double startPosition;
 
     //Runs at the beginning of autonomous period
     public void autonomousInit() {
         //Digital/analog inputs
         doNothing = ds.getDigitalIn(1);
         trackLine = ds.getDigitalIn(2);
         heightOne = ds.getDigitalIn(3);
         heightTwo = ds.getDigitalIn(4);
         heightThree = ds.getDigitalIn(5);
         staggeredPeg = ds.getDigitalIn(6);
         releaseTube = ds.getDigitalIn(7);
         reverse = ds.getDigitalIn(8);
         startPosition = ds.getAnalogIn(1);
 
         //Minibot defaults to in
         minibotRelease.set(false);
         minibotServo.set(0);
 
        manualElevatorToggle.set(false);
        gripperToggle.set(false);
        arcadeToggle.set(true);
        minibotToggle.set(false);

         //Default to slow driving mode
         transShift.set(!PRACTISE_ROBOT);
 
         //Reset gyro and enable PID on gyro
         gyro.reset();
 
         //Enable PID on wheels
         pidLeft.enable();
         pidRight.enable();
 
         //Reset encoders
         encLeft.reset();
         encRight.reset();
 
         //Current step
         stepIndex = 0;
 
         //Reset the counter for how many times the gyro has reached its setpoint
         gyroCounter = 0;
 
         setElbow(ElbowState.Vertical);
 
         if(doNothing) {
             stepList = new Step[] {
                 new Step(AutonomousState.Done),
             };
         }
         else {
             stepList = new Step[] {
                 new Step(AutonomousState.Driving, AUTONOMOUS_DRIVE_COUNTS),
                 new Step(AutonomousState.Release),
                new Step(AutonomousState.Driving, reverse ? -AUTONOMOUS_DRIVE_COUNTS * 0.75 : 0),
                 new Step(AutonomousState.Turning, reverse ? 180 : 0),
                 new Step(AutonomousState.Done),
             };
         }
 
         //Determine the setpoint of the elevator
         elevatorSetpoint = (heightOne && !staggeredPeg) ? ElevatorSetpoint.posOne : elevatorSetpoint;
         elevatorSetpoint = (heightOne && staggeredPeg) ? ElevatorSetpoint.posTwo : elevatorSetpoint;
 
         elevatorSetpoint = (heightTwo && !staggeredPeg) ? ElevatorSetpoint.posThree : elevatorSetpoint;
         elevatorSetpoint = (heightTwo && staggeredPeg) ? ElevatorSetpoint.posFour : elevatorSetpoint;
 
         elevatorSetpoint = (heightThree && !staggeredPeg) ? ElevatorSetpoint.posFive : elevatorSetpoint;
         elevatorSetpoint = (heightThree && staggeredPeg) ? ElevatorSetpoint.posSix : elevatorSetpoint;
     }
 
     //Runs periodically during autonomous period
     public void autonomousPeriodic() {
         //Call our print function with the current mode
         print("Autonomous");
     }
 
     static final double DEFAULT_STEERING_GAIN = 0.65;
 
     //Runs continuously during autonomous period
     public void autonomousContinuous() {
         if(trackLine && !doNothing) {
             int binaryValue; // a single binary value of the three line tracking
                             // sensors
             int previousValue = 0; // the binary value from the previous loop
             double steeringGain; // the amount of steering correction to apply
 
             // the power profiles for the straight and forked robot path. They are
             // different to let the robot drive more slowly as the robot approaches
             // the fork on the forked line case.
 
 
             double forkProfile[] = {0.70, 0.70, 0.55, 0.60, 0.60, 0.50, 0.40, 0.00};
             double straightProfile[] = {0.7, 0.7, 0.6, 0.6, 0.35, 0.35, 0.35, 0.0};
 
             double powerProfile[];   // the selected power profile
 
             // set the straightLine and left-right variables depending on chosen path
             boolean straightLine = ds.getDigitalIn(1);
             powerProfile = (straightLine) ? straightProfile : forkProfile;
             double stopTime = (straightLine) ? 2.0 : 4.0; // when the robot should look for end
             boolean goLeft = !ds.getDigitalIn(2) && !straightLine;
             System.out.println("StraightLine: " + straightLine);
             System.out.println("GoingLeft: " + goLeft);
 
 
             boolean atCross = false; // if robot has arrived at end
 
             // time the path over the line
             Timer timer = new Timer();
             timer.start();
             timer.reset();
 
             int oldTimeInSeconds = -1;
             double time;
             double speed, turn;
 
             // loop until robot reaches "T" at end or 8 seconds has past
             while ((time = timer.get()) < 8.0 && !atCross) {
                 int timeInSeconds = (int) time;
                 // read the sensors
                 int leftValue = leftSensor.get() ? 1 : 0;
                 int middleValue = middleSensor.get() ? 1 : 0;
                 int rightValue = rightSensor.get() ? 1 : 0;
                 // compute the single value from the 3 sensors. Notice that the bits
                 // for the outside sensors are flipped depending on left or right
                 // fork. Also the sign of the steering direction is different for left/right.
                 if (goLeft) {
                     binaryValue = leftValue * 4 + middleValue * 2 + rightValue;
                     steeringGain = -DEFAULT_STEERING_GAIN;
                 } else {
                     binaryValue = rightValue * 4 + middleValue * 2 + leftValue;
                     steeringGain = DEFAULT_STEERING_GAIN;
                 }
 
                 // get the default speed and turn rate at this time
                 speed = powerProfile[timeInSeconds];
                 turn = 0;
 
                 // different cases for different line tracking sensor readings
                 switch (binaryValue) {
                     case 1:  // on line edge
                         turn = 0;
                         break;
                     case 7:  // all sensors on (maybe at cross)
                         if (time > stopTime) {
                             atCross = true;
                             speed = 0;
                         }
                         break;
                     case 0:  // all sensors off
                         if (previousValue == 0 || previousValue == 1) {
                             turn = steeringGain;
                         } else {
                             turn = -steeringGain;
                         }
                         break;
                     default:  // all other cases
                         turn = -steeringGain;
                 }
                 // print current status for debugging
                 if (binaryValue != previousValue) {
                     System.out.println("Time: " + time + " Sensor: " + binaryValue + " speed: " + speed + " turn: " + turn + " atCross: " + atCross);
                 }
 
                 // set the robot speed and direction
                 robotDrive.arcadeDrive(speed, turn);
 
                 pidLeft.setSetpoint(storageLeft.get() * SLOW_MAX_ENCODER_RATE);
                 pidRight.setSetpoint(storageRight.get() * SLOW_MAX_ENCODER_RATE);
 
                 if (binaryValue != 0) {
                     previousValue = binaryValue;
                 }
                 oldTimeInSeconds = timeInSeconds;
 
                 Timer.delay(0.01);
             }
             // Done with loop - stop the robot. Robot ought to be at the end of the line
             pidLeft.setSetpoint(0.0);
             pidRight.setSetpoint(0.0);
         }
         else {
             //Our current step in our list of steps
             Step currentStep = stepList[stepIndex];
             //The last step we did
             int lastStepIndex = stepIndex;
             //If we have a step to do
             if(currentStep != null) {
                 //Switch the type of step
                 switch(currentStep.type) {
                     //If we want to drive forward
                     case AutonomousState.Driving:
                         int direction = currentStep.get() > 0 ? 1 : currentStep.get() < 0 ? -1 : 0;
                         //If we have reached our value for this step on the left or right side
                         boolean leftDone = false;
                         boolean rightDone = false;
 
                         if(direction == 1) {
                             final double distance = ultrasonicSensor.getVoltage() / ULTRASONIC_VOLTS_PER_INCH;
                             leftDone = -encLeft.encoder.get() >= currentStep.get() || distance <= MAX_SCORING_DISTANCE;
                             rightDone = encRight.encoder.get() >= currentStep.get() || distance <= MAX_SCORING_DISTANCE;
                         }
                         else if (direction == -1) {
                             leftDone  = -encLeft.encoder.get() <= currentStep.get();
                             rightDone = encRight.encoder.get() <= currentStep.get();
                             if(-encLeft.encoder.get() <= currentStep.get() * 0.25 && encRight.encoder.get() <= currentStep.get() * 0.25)
                                 setElbow(ElbowState.Vertical);
                         }
 
                         //Drive each side until we reach the value for each side
                         robotDrive.arcadeDrive(direction * 0.85, gyroPID(true, 0.0));
                         if(!leftDone)
                             pidLeft.setSetpoint(-storageLeft.get() * SLOW_MAX_ENCODER_RATE);
                         else
                             pidLeft.disable();
                         if(!rightDone)
                             pidRight.setSetpoint(-storageRight.get() * SLOW_MAX_ENCODER_RATE);
                         else
                             pidRight.disable();
 
                         //If the value is reached
                         if(elevatorPID() && leftDone && rightDone)
                             ++stepIndex;
                         break;
                     //If we want to turn
                     case AutonomousState.Turning:
                         //Disable PIDs for smoother turning
                         if(pidLeft.isEnable() || pidRight.isEnable()) {
                             pidLeft.disable();
                             pidRight.disable();
                         }
                         if(false) {
                             //Set the setpoint for the gyro PID to the step's setpoint
                             pidGyro.setSetpoint(currentStep.get());
                             //Drive the motors with the output from the gyro PID
                             jagLeft.set(-gyroOutput.get());
                             jagRight.set(-gyroOutput.get());
                             //Difference between our position and our setpoint
                             final double delta = currentStep.get() - gyro.pidGet();
                             //If the gyro is below or above the target angle depending on the direction we are turning
                             if(Math.abs(delta) < GYRO_TOLERANCE)
                                 ++gyroCounter;
                             if(gyroCounter >= 10) {
                                 ++stepIndex;
                                 pidLeft.enable();
                                 pidRight.enable();
                             }
                         } else {
                             gyroPID(false, currentStep.get());
                         }
                         break;
                     case AutonomousState.Reset:
                         setElbow(ElbowState.Vertical);
                         elevatorSetpoint = ElevatorSetpoint.ground;
                         if(elevatorPID())
                             ++stepIndex;
                         break;
                     //To release the tube
                     case AutonomousState.Release:
                         if(releaseTube) {
                             pidLeft.disable();
                             pidRight.disable();
                             setElbow(ElbowState.Middle);
                             Timer.delay(AUTONOMOUS_RELEASE_DELAY);
                             releaseTube();
                             elevatorSetpoint = ElevatorSetpoint.ground;
                         }
                         ++stepIndex;
                         break;
                     //If we are done our autonomous mode
                     case AutonomousState.Done:
                         pidLeft.disable();
                         pidRight.disable();
                         break;
                     //Sleep state
                     case AutonomousState.Sleep:
                         double time = currentStep.get();
                         pidLeft.disable();
                         pidRight.disable();
                         while(time > 0) {
                             print("Autonomous");
                             Timer.delay(1);
                             --time;
                             Watchdog.getInstance().feed();
                         }
                         pidLeft.enable();
                         pidLeft.enable();
                         ++stepIndex;
                         break;
                     default:
                         ++stepIndex;
                         break;
                 }
             }
             //If we want to go to the next step
             if(lastStepIndex != stepIndex) {
                 //Reset everything
                 encLeft.reset();
                 encRight.reset();
                 gyro.reset();
                 vicElevator.set(0.0);
                 //Stop
                 pidLeft.enable();
                 pidRight.enable();
                 pidLeft.setSetpoint(0.0);
                 pidRight.setSetpoint(0.0);
                 //Reset gyro counter to 0
                 gyroCounter = 0;
                 System.out.println("Step: " + stepIndex);
             }
         }
     }
 
     //Start time for teleoperated mode
     double teleopStartTime;
     int lightState = Lights.Off;
 
     //Runs at the beginning of teleoperated period
     public void teleopInit() {
         //Initialize variables
         teleopStartTime = Timer.getFPGATimestamp();
 
         //Minibot defaults to in
         minibotRelease.set(false);
         minibotServo.set(0);

        manualElevatorToggle.set(false);
        gripperToggle.set(false);
        arcadeToggle.set(true);
        minibotToggle.set(false);
     }
 
     //Runs periodically during teleoperated period
     public void teleopPeriodic() {
         //Call our print function with the current mode
         print("Teleoperated");
     }
 
     int lastColor = Lights.Off;
 
     //Runs continuously during teleoperated period
     public void teleopContinuous() {
         //Don't allow the gyro to be more or less than 360 degrees
         if(gyro.pidGet() < -360 || gyro.pidGet() > 360)
             gyro.reset();
 
         boolean finale = Timer.getFPGATimestamp() - teleopStartTime >= MINIBOT_RELEASE_TIME;
         final double distance = ultrasonicSensor.getVoltage() / ULTRASONIC_VOLTS_PER_INCH;
         
         boolean flashCurrentColor = false;
 
         if(!gripper.get()) { //trying to score
             if(distance > MIN_SCORING_DISTANCE && distance < MAX_SCORING_DISTANCE)
                 flashCurrentColor = true;
         }
         else if(elevatorSetpoint == ElevatorSetpoint.feed) { //gripper is open and feed position
             if(distance > MIN_FEEDING_DISTANCE && distance < MAX_FEEDING_DISTANCE)
                 flashCurrentColor = true;
         }
 
         if(stickOperator.getRawButton(Operator.LIGHT_SELECTION)) {
             if(stickOperator.getRawButton(Operator.LIGHT_RED))
                 lightState = Lights.Red;
             if(stickOperator.getRawButton(Operator.LIGHT_WHITE))
                 lightState = Lights.White;
             if(stickOperator.getRawButton(Operator.LIGHT_BLUE))
                 lightState = Lights.Blue;
             if(stickOperator.getRawButton(Operator.LIGHT_OFF))
                 lightState = Lights.Off;
         }
         else {
             //The elevator setpoint based on the corresponding button
             elevatorSetpoint = stickOperator.getRawButton(Operator.ELEVATOR_STATE_GROUND) ? ElevatorSetpoint.ground : elevatorSetpoint;
             elevatorSetpoint = stickOperator.getRawButton(Operator.ELEVATOR_STATE_ONE) ? ElevatorSetpoint.posOne : elevatorSetpoint;
             elevatorSetpoint = stickOperator.getRawButton(Operator.ELEVATOR_STATE_TWO) ? ElevatorSetpoint.posTwo : elevatorSetpoint;
             elevatorSetpoint = stickOperator.getRawButton(Operator.ELEVATOR_STATE_THREE) ? ElevatorSetpoint.posThree : elevatorSetpoint;
             elevatorSetpoint = stickOperator.getRawButton(Operator.ELEVATOR_STATE_FOUR) ? ElevatorSetpoint.posFour : elevatorSetpoint;
             elevatorSetpoint = stickOperator.getRawButton(Operator.ELEVATOR_STATE_FIVE) ? ElevatorSetpoint.posFive : elevatorSetpoint;
             elevatorSetpoint = stickOperator.getRawButton(Operator.ELEVATOR_STATE_SIX) ? ElevatorSetpoint.posSix : elevatorSetpoint;
             elevatorSetpoint = !finale /*this is one of the minibot release buttons*/ && stickOperator.getRawButton(Operator.ELEVATOR_STATE_FEED) ? ElevatorSetpoint.feed : elevatorSetpoint;
         }
 
         flashLED(finale, flashCurrentColor);
 
         //Disable manual elevator toggle during the finale
         manualElevatorToggle.feed(!finale && stickOperator.getRawButton(Operator.ELEVATOR_MANUAL_TOGGLE));
         //Manual or automated elevator control
         if(manualElevatorToggle.get()) {
             double axis = stickOperator.getAxis(Joystick.AxisType.kY);
             if(!elevatorLimit.get())
                 axis = Math.max(axis, 0);
             if(axis > 0)
                 axis *= ELEVATOR_SPEED_DOWN;
             vicElevator.set(axis);
         } else {
             elevatorPID();
         }
 
         //Minus because the left encoder is negative
         double rate = Math.abs((encRight.pidGet() - encLeft.pidGet()) / 2);
 
         final double LOW_SPEED_PERCENT = 0.9;
         final double HIGH_SPEED_PERCENT = 0.6;
 
         if(elbowState == ElbowState.Vertical) {
             if(!transState)
                 transState = rate >= LOW_SPEED_PERCENT * SLOW_MAX_ENCODER_RATE && Math.abs(stickDriver.getRawAxis(Driver.Y_AXIS_LEFT)) >= LOW_SPEED_PERCENT ? true : transState;
             else if(transState)
                 transState = rate <= HIGH_SPEED_PERCENT * FAST_MAX_ENCODER_RATE && Math.abs(stickDriver.getRawAxis(Driver.Y_AXIS_LEFT)) <= HIGH_SPEED_PERCENT ? false : transState;
             transState = stickDriver.getRawButton(Driver.TRANS_TOGGLE_LOW) ? false : transState;
             transState = stickDriver.getRawButton(Driver.TRANS_TOGGLE_HIGH) ? true : transState;
         }
         else
             transState = false; //Low gear
 
         //TODO: the following line was a hack to make the test robot work. remove it
         //transState = false;
 
         //Set the transmission shifter to open or closed based on the state of the toggle
         transShift.set(PRACTISE_ROBOT ? transState : !transState);
 
         //Determine the input range to use (max encoder rate) to use depending on the transmission state we are in
         double maxEncoderRate = transState ? FAST_MAX_ENCODER_RATE : SLOW_MAX_ENCODER_RATE;
         pidLeft.setInputRange(-maxEncoderRate, maxEncoderRate);
         pidRight.setInputRange(-maxEncoderRate, maxEncoderRate);
 
         //Feed the toggle on the gripper button
         gripperToggle.feed(stickOperator.getRawButton(Operator.GRIPPER_TOGGLE));
         //Set the gripper to open or closed based on the state of the toggle
         if(elbowState != ElbowState.Vertical)
             gripper.set(gripperToggle.get());
 
         double elbowInput = -stickOperator.getAxis(Joystick.AxisType.kThrottle);
         if(elbowInput < -0.5)
             elbowState = ElbowState.Horizontal;
         else if(elbowInput > 0.5)
             elbowState = ElbowState.Vertical;
         else
             elbowState = ElbowState.Middle;
         setElbow(elbowState);
         if(elbowState == ElbowState.Horizontal && lastElbowState == ElbowState.Middle)
             gripperToggle.set(true);
         lastElbowState = elbowState;
 
         //Feed the toggle on the arcade/tank drive button
         arcadeToggle.feed(stickDriver.getRawButton(Driver.ARCADE_TOGGLE));
 
         final boolean doPID = false;
         //Drive arcade or tank based on the state of the toggle
         if(arcadeToggle.get()) {
             //If PID is disabled
             if((!pidLeft.isEnable() || !pidRight.isEnable()) && doPID) {
                 //Enable PID
                 pidLeft.enable();
                 pidRight.enable();
             }
             if((pidLeft.isEnable() || pidRight.isEnable()) && !doPID) {
                 pidLeft.disable();
                 pidRight.disable();
             }
 
             double driveAxis = stickDriver.getRawAxis(Driver.Y_AXIS_LEFT);
             driveAxis = Math.abs(driveAxis) < 0.2 ? 0.0 : driveAxis;
 
             double turnAxis = stickDriver.getRawAxis(Driver.X_AXIS_RIGHT);
             turnAxis = Math.abs(turnAxis) < 0.2 ? 0.0 : turnAxis;
 
             //Let the robotdrive class calculate arcade drive for us
             robotDrive.arcadeDrive(driveAxis, turnAxis);
 
             if(doPID) {
                 pidLeft.setSetpoint(storageLeft.get() * maxEncoderRate);
                 pidRight.setSetpoint(storageRight.get() * maxEncoderRate);
             }
             else {
                 jagLeft.set(storageLeft.get());
                 jagRight.set(storageRight.get());
             }
         }
         else {
             //If PID is disabled
             if((!pidLeft.isEnable() || !pidRight.isEnable()) && doPID) {
                 //Enable PID
                 pidLeft.enable();
                 pidRight.enable();
             }
             if((pidLeft.isEnable() || pidRight.isEnable()) && !doPID) {
                 pidLeft.disable();
                 pidRight.disable();
             }
 
             //Left axis
             double leftAxis = stickDriver.getRawAxis(Driver.Y_AXIS_LEFT);
             //Any value less than 0.2 is set to 0.0 to create a dead zone
             leftAxis = Math.abs(leftAxis) < 0.2 ? 0.0 : leftAxis;
 
             //Right axis
             double rightAxis = stickDriver.getRawAxis(Driver.Y_AXIS_RIGHT);
             //Any value less than 0.2 is set to 0.0 to create a dead zone
             rightAxis = Math.abs(rightAxis) < 0.2 ? 0.0 : rightAxis;
 
             if(doPID) {
                 //Set the setpoint as a percentage of the maximum encoder rate
                 pidLeft.setSetpoint(leftAxis * maxEncoderRate);
                 pidRight.setSetpoint(-rightAxis * maxEncoderRate);
             }
             else {
                 jagLeft.set(leftAxis);
                 jagRight.set(-rightAxis);
             }
         }
 
         //If there are 10 seconds left
         if(finale) {
             minibotToggle.feed(stickOperator.getRawButton(Operator.MINIBOT_RELEASE_ONE) && stickOperator.getRawButton(Operator.MINIBOT_RELEASE_TWO));
 
             minibotRelease.set(minibotToggle.get());
 
             if(minibotToggle.get() && !minibotLimit.get()) //Minibot limit switch is engaged when false
                 minibotServo.set(255);
         }
     }
 
     //Returns whether or not the setpoint has been reached
     public boolean elevatorPID() {
         //Difference between setpoint and our position
         final double error = elevatorSetpoint - encElevator.pidGet();
 
         //We can be off by 5%
         final double toleranceWhileGoingUp = MAX_ELEVATOR_COUNTS * 0.05;
         final double toleranceWhileGoingDown = -MAX_ELEVATOR_COUNTS * 0.05;
 
         //Go up when below setpoint, down when above setpoint
         if(error > 0 && error > toleranceWhileGoingUp)
             vicElevator.set(ELEVATOR_SPEED_UP);
         else if(error < 0 && error < toleranceWhileGoingDown && elevatorLimit.get()) //Cant go down unless elevator limit is disengaged
             vicElevator.set(-ELEVATOR_SPEED_DOWN);
         else {
             vicElevator.set(0.0);
             return true;
         }
         return false;
     }
 
     public void setElbow(int state) {
         //Update the elbow state
         elbowState = state;
         //For the elbow pneumatics, closed = true open = false
         //The top elbow is only ever closed in the vertical state
         elbowTop.set(elbowState != ElbowState.Vertical);
         //The bottom elbow is only ever open in the horizontal state
         elbowBottom.set(elbowState == ElbowState.Horizontal);
         //If we are vertical then close the gripper
         if(elbowState == ElbowState.Vertical) {
             gripper.set(false);
             gripperToggle.set(false);
         }
     }
     
     //Number of times our setpoint has been reached
     int gyroCounter;
 
     public double gyroPID(boolean returnSpeed, double target) {
         //Use our own calculations to get to the setpoint of the gyro
         final double delta = target - gyro.getAngle();
         //For straight driving in autonomous mode
         if(returnSpeed) {
             if(Math.abs(delta) < GYRO_TOLERANCE)
                 ++gyroCounter;
             if(gyroCounter >= 100) {
                 gyroCounter = 0;
                 return 0.0;
             }
             //The speed is incorporated into straight driving so it has to be low
             final double speed = 0.1;
             return delta > 0 ? -speed :speed;
         }
         //For turning on the spot
         else {
             if(Math.abs(delta) < GYRO_TOLERANCE)
                 ++gyroCounter;
             if(gyroCounter >= 100)
                 ++stepIndex;
             //We are turning on the spot so the turning speed is high
             final double speed = 0.5;
             jagLeft.set(delta >= 0 ? -speed : speed);
             jagRight.set(delta >= 0 ? -speed : speed);
             return 0.0;
         }
     }
 
     public void releaseTube() {
         setElbow(ElbowState.Horizontal);
         gripper.set(true);
         //Toggle the gripper to be open at the beginning of teleop
         gripperToggle.set(true);
     }
 
     double flashTime = 0;
     boolean flash = false;
 
     public void flashLED(boolean finale, boolean flashingColor) {
         double now = Timer.getFPGATimestamp();
         if(now - flashTime > FLASH_TIME) {
             flash = !flash;
             flashTime = now;
         }
 
         if(finale) {
             if(flash) {
                 lightsOne.set(false);
                 lightsTwo.set(true);
             }
             else {
                 lightsOne.set(true);
                 lightsTwo.set(false);
             }
         }
         else {
             switch(lightState) {
                 case Lights.Red:
                     lightsOne.set(true);
                     if(flashingColor && !flash)
                         lightsOne.relay.set(Relay.Value.kOff);
                     lightsTwo.relay.set(Relay.Value.kOff);
                     break;
                 case Lights.White:
                     lightsOne.set(false);
                     if(flashingColor && !flash)
                         lightsOne.relay.set(Relay.Value.kOff);
                     lightsTwo.relay.set(Relay.Value.kOff);
                     break;
                 case Lights.Blue:
                     lightsOne.relay.set(Relay.Value.kOff);
                     lightsTwo.set(true);
                     if(flashingColor && !flash)
                         lightsTwo.relay.set(Relay.Value.kOff);
                     break;
                 case Lights.Off:
                     //If the lights are off but we want to flash (distance sensor) default to red light
                     if(flashingColor) {
                         lightsOne.set(true);
                         if(!flash)
                             lightsOne.relay.set(Relay.Value.kOff);
                         lightsTwo.relay.set(Relay.Value.kOff);
                     }
                     else {
                         lightsOne.relay.set(Relay.Value.kOff);
                         lightsTwo.relay.set(Relay.Value.kOff);
                     }
                     break;
                 default:
                     lightsOne.relay.set(Relay.Value.kOff);
                     lightsTwo.relay.set(Relay.Value.kOff);
                     break;
             }
 
             if(transState) {
                 lightsOne.relay.set(Relay.Value.kOff);
                 lightsTwo.set(false);
             }
         }
     }
     
     double lastPrintTime = 0;
 
     //Print function for our variables
     public void print(String mode) {
         //Current time
         final double curPrintTime = Timer.getFPGATimestamp();
         //If it has been more than half a second
         if(curPrintTime - lastPrintTime > PRINT_DELAY) {
             //Make a bunch of newlines to clear the screen to only show the current output
             System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
             //Print statements
             System.out.println("[" + mode + "]");
             System.out.println("gripperToggle: " + gripperToggle.get());
             System.out.println("renc count: " + encRight.encoder.get() + " lenc count: " + encLeft.encoder.get() + " elevator counts: " + encElevator.pidGet());
             System.out.println("rencRate: " + encRight.pidGet() + " lencRate: " + encLeft.pidGet());
             System.out.println("rSet: " + pidRight.getSetpoint() + " lSet: " + pidLeft.getSetpoint() + " eSet: " + elevatorSetpoint);
             System.out.println("rPID: " + pidRight.get() + " lPID: " + pidLeft.get());
             System.out.println("manualElevator: " + manualElevatorToggle.get());
             System.out.println("elevAxis: " + stickOperator.getAxis(Joystick.AxisType.kY) + " leftAxis: " + stickDriver.getRawAxis(Driver.Y_AXIS_LEFT) + " rightAxis: " + stickDriver.getRawAxis(Driver.Y_AXIS_RIGHT));
             System.out.println("Gyro PIDget: " + gyro.pidGet() + " gyro output storage: " + gyroOutput.get());
             System.out.println("jagLeft: " + jagLeft.get() + " jagRight: " + jagRight.get());
             System.out.println("elbow input: " + stickOperator.getThrottle() + "elbowState: " + elbowState);
             //System.out.println("Raven gyro min: " + gyro.min + " max: " + gyro.max + " deadzone: " + gyro.deadzone + " center: " + gyro.center);
             System.out.println("rightSensor: " + rightSensor.get() + " middleSensor: " + middleSensor.get() + " leftSensor: " + leftSensor.get());
             System.out.println("light: " + lightState);
             System.out.println("ultrasonic distance: " + ultrasonicSensor.getVoltage() / ULTRASONIC_VOLTS_PER_INCH);
             System.out.println("limitelev: " + elevatorLimit.get() + " minibotlimit: " + minibotLimit.get());
            System.out.println("minibotRelease: " + minibotToggle.get());
             //Update the last print time
             lastPrintTime = curPrintTime;
         }
     }
 }
