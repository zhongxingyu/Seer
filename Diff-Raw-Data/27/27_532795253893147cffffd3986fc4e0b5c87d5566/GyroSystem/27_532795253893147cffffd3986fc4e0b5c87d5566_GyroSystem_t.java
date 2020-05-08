 package com.frc4343.robot2.Systems;
 
 import com.frc4343.robot2.Mappings;
 import com.frc4343.robot2.RobotTemplate;
 import edu.wpi.first.wpilibj.Gyro;
 import edu.wpi.first.wpilibj.Timer;
 
 public class GyroSystem extends System {
 
     public Gyro gyro = new Gyro(Mappings.GYRO_PORT);
     Timer timer = new Timer();
     double initialAngle = 0;
     boolean isButtonPressed = false;
     boolean isRotatingClockwise = true;
     // IDLE indicates no activity.
     static final byte IDLE = 0;
     // ROTATING indicates that the robot is rotating around a point.
     static final byte ROTATING = 1;
     // ALIGNING indicates that the robot is aligning itself to shoot at the pyramid.
     static final byte ALIGNING = 2;
 
     public GyroSystem(RobotTemplate robot) {
         super(robot);
     }
 
     public void switchMode() {
         gyro.reset();
         timer.reset();
         timer.stop();
 
         initialAngle = gyro.getAngle();
 
         systemState = IDLE;
     }
 
     public void run() {
         if (!robot.isAutonomous()) {
             switch (systemState) {
                 case IDLE:
                    if (robot.joystickSystem.getJoystick((byte) 1).getRawButton(Mappings.ROTATE_CLOCKWISE)) {
                         robot.driveSystem.driveIndefinitely(0.0, Mappings.ROTATE_SPEED);
                         initialAngle = gyro.getAngle();
                         robot.driveSystem.isDrivingWithJoystick = false;
                         systemState = ROTATING;
                    } else if (robot.joystickSystem.getJoystick((byte) 1).getRawButton(Mappings.ROTATE_COUNTERCLOCKWISE)) {
                         robot.driveSystem.driveIndefinitely(0.0, -Mappings.ROTATE_SPEED);
                         initialAngle = gyro.getAngle();
                         robot.driveSystem.isDrivingWithJoystick = false;
                         systemState = ROTATING;
                     }
                     break;
                 case ROTATING:
                     if (gyro.getAngle() >= initialAngle + Mappings.ANGLE_TO_ROTATE_BY || gyro.getAngle() <= initialAngle - Mappings.ANGLE_TO_ROTATE_BY) {
                         robot.driveSystem.isDrivingWithJoystick = true;
                         switchMode();
                     }
                     break;
                 case ALIGNING:
                     break;
                 default:
                     break;
             }
         }
     }
 
     public String getState() {
         switch (systemState) {
             case 0:
                 return "IDLE";
             case 1:
                 return "ROTATING";
             case 2:
                 return "ALIGNING";
             default:
                 return "ERROR";
         }
     }
 }
