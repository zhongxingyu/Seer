 package com.teamupnext.robot.commands;
 
 import InsightLT.DecimalData;
 import InsightLT.InsightLT;
 import com.teamupnext.robot.OI;
 import com.teamupnext.robot.RobotMap;
 import com.teamupnext.robot.subsystems.*;
 import edu.wpi.first.wpilibj.Compressor;
 import edu.wpi.first.wpilibj.DriverStation;
 import edu.wpi.first.wpilibj.can.CANTimeoutException;
 import edu.wpi.first.wpilibj.command.Command;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 
 /**
  * The base for all commands. All atomic commands should subclass CommandBase.
  * CommandBase stores creates and stores each control system. To access a
  * subsystem elsewhere in your code in your code use CommandBase.exampleSubsystem
  * @author Author
  */
 public abstract class CommandBase extends Command {
 
     public static OI oi;
     
     private static Compressor compressor = new Compressor(RobotMap.PRESSURE_SWITCH_DIO_CHANNEL, RobotMap.COMPRESSOR_RELAY_CHANNEL);
     
     // Create a single static instance of all of your subsystems    
     public static DriveTrain driveTrain;
     public static Climber climber = new Climber();
     public static Feeder feeder = new Feeder();
     public static PickerUpper pickerUpper = new PickerUpper();
     public static Shooter shooter;
     public static Targeter targeter = new Targeter();
     public static TableTilter tableTilter = new TableTilter();
     
     
     private static InsightLT display;
     private static DriverStation ds;
     
     
     
     public static void init() {
         
         System.out.println("<--------- Top ------------>");
         
         //DriverStation
         ds = DriverStation.getInstance();
         double battVoltage = ds.getBatteryVoltage();
         
         // InsightLT
         display = new InsightLT(InsightLT.FOUR_ZONES);
         DecimalData disp_batteryVoltage = new DecimalData("Batt:");        
         display.registerData(disp_batteryVoltage, 2);
        display.startDisplay();
         disp_batteryVoltage.setData(battVoltage);
         
         System.out.println("------->i'm here<------ " + battVoltage);
         
         compressor.start();
         try {
             shooter = new Shooter();
             driveTrain = new DriveTrain();
         } catch (CANTimeoutException ex) {
             ex.printStackTrace();
         }
         
         oi = new OI();
 
         // Show what command your subsystem is running on the SmartDashboard
         SmartDashboard.putData(driveTrain);
         SmartDashboard.putData(climber);
         SmartDashboard.putData(feeder);
         SmartDashboard.putData(pickerUpper);
         SmartDashboard.putData(shooter);
         SmartDashboard.putData(targeter);
         SmartDashboard.putData(tableTilter);
     }
 
     public CommandBase(String name) {
         super(name);
     }
 
     public CommandBase() {
         super();
     }
 }
