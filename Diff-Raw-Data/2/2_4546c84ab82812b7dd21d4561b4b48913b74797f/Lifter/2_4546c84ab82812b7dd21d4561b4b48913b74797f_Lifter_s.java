 package com.edinarobotics.zed.subsystems;
 
 import com.edinarobotics.utils.commands.MaintainStateCommand;
 import com.edinarobotics.utils.pid.PIDConfig;
 import com.edinarobotics.utils.pid.PIDTuningManager;
 import com.edinarobotics.utils.subsystems.Subsystem1816;
 import edu.wpi.first.wpilibj.CANJaguar;
 
 public class Lifter extends Subsystem1816 {
     private static final int ANGLE_POTENTIOMETER_TURNS = 1;
     private final double P_LIFTER_1 = 1;
     private final double I_LIFTER_1 = 0;
     private final double D_LIFTER_1 = 0;
     private final double P_LIFTER_2 = 1;
     private final double I_LIFTER_2 = 0;
     private final double D_LIFTER_2 = 0;
     private final int NUM_RETRIES = 10;
     private PIDConfig firstLifterPIDConfig;
     private PIDConfig secondLifterPIDConfig;
 
     private double position;
     private CANJaguar lifterJaguarFirst;
     private CANJaguar lifterJaguarSecond;
     
     public Lifter(int firstLifterJaguarNum, int secondLifterJaguarNum) {
         super("Lifter");
         lifterJaguarFirst = createCANJaguar(firstLifterJaguarNum,
                 CANJaguar.PositionReference.kPotentiometer,
                 ANGLE_POTENTIOMETER_TURNS,
                 CANJaguar.ControlMode.kPosition,
                 P_LIFTER_1, I_LIFTER_1, D_LIFTER_1,
                 NUM_RETRIES);
         lifterJaguarSecond = createCANJaguar(secondLifterJaguarNum,
                 CANJaguar.PositionReference.kPotentiometer,
                 ANGLE_POTENTIOMETER_TURNS,
                 CANJaguar.ControlMode.kPosition,
                 P_LIFTER_2, I_LIFTER_2, D_LIFTER_2,
                 NUM_RETRIES);
         firstLifterPIDConfig = PIDTuningManager.getInstance().getPIDConfig("First Lifter");
         secondLifterPIDConfig = PIDTuningManager.getInstance().getPIDConfig("Second Lifter");
     }
 
     protected void initDefaultCommand() {
         setDefaultCommand(new MaintainStateCommand(this));
     }
     
    public void setShooterPosition(double position) {
         this.position = position;
         update();
     }
     
     public void update() {
         try {
             if(lifterJaguarFirst != null) {
                     lifterJaguarFirst.setX(position);
                     //PID tuning code
                     lifterJaguarFirst.setPID(firstLifterPIDConfig.getP(P_LIFTER_1),
                             firstLifterPIDConfig.getI(I_LIFTER_1),
                             firstLifterPIDConfig.getD(D_LIFTER_1));
                     firstLifterPIDConfig.setSetpoint(position);
                     firstLifterPIDConfig.setValue(lifterJaguarFirst.getPosition());
             }
             if(lifterJaguarSecond != null) {
                     lifterJaguarSecond.setX(position);
                     //PID tuning code
                     lifterJaguarSecond.setPID(secondLifterPIDConfig.getP(P_LIFTER_2),
                             secondLifterPIDConfig.getI(I_LIFTER_2),
                             secondLifterPIDConfig.getD(D_LIFTER_2));
                     secondLifterPIDConfig.setSetpoint(position);
                     secondLifterPIDConfig.setValue(lifterJaguarSecond.getPosition());
             }
         }
         catch(Exception e) {
             System.err.println("Failed to update lifter Jaguars.");
             e.printStackTrace();
         }
     }
     
     private CANJaguar createCANJaguar(int id, CANJaguar.PositionReference positionReference,
             int codesPerRev, CANJaguar.ControlMode controlMode,
             double P, double I, double D, int numRetries) {
         CANJaguar canJaguar = null;
         try {
             for(int i = 0; i < numRetries; i++) {
                 canJaguar = new CANJaguar(id);
                 if(canJaguar != null) {
                     break;
                 }
             }
             canJaguar.setPositionReference(positionReference);
             canJaguar.configEncoderCodesPerRev(codesPerRev);
             canJaguar.changeControlMode(controlMode);
             canJaguar.setPID(P, I, D);
         }
         catch(Exception e) {
             System.err.println("Failed to create Jaguar.");
             e.printStackTrace();
         }
         return canJaguar;
     }
    
 }
