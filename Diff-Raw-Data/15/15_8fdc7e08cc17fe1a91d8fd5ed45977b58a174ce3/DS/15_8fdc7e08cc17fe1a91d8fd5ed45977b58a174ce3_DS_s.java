 package org.buzzrobotics.utils;
 
 import edu.wpi.first.wpilibj.DriverStation;
 import edu.wpi.first.wpilibj.DriverStation.Alliance;
 import edu.wpi.first.wpilibj.DriverStationLCD;
 /**
  *
  * @author Kyle Deane
  */
 public class DS {
     
    DriverStation driverstation = DriverStation.getInstance();
     
     public boolean isFMSAttached(){
         return driverstation.isFMSAttached();
     }
     
     public Alliance getAlliance(){
         return driverstation.getAlliance();
     }
     
     public double getMatchTime(){
         return driverstation.getMatchTime();
     }
     
     public double getTeamNumber(){
         return driverstation.getTeamNumber();
     }
     
     public boolean isAutonomous(){
         return driverstation.isAutonomous();
     }
     
     public boolean isDisabled(){
         return driverstation.isDisabled();
     }
     
     public boolean isEnabled(){
         return driverstation.isEnabled();
     }
     
     public boolean isOperatorControl(){
         return driverstation.isOperatorControl();
     }
     
     /*
      * Displays information on the DriverStation LCD (On the diagnostics tab)
      * @param line what line number to print to
      * @param msg message to print
      */
     public static void disp(int line, String msg) {
         DriverStationLCD.Line l;
         switch (line){
             case 1:
                 l = DriverStationLCD.Line.kUser2;
                 break;
             case 2:
                 l = DriverStationLCD.Line.kUser3;
                 break;
             case 3:
                 l = DriverStationLCD.Line.kUser4;
                 break;
             case 4:
                 l = DriverStationLCD.Line.kUser5;
                 break;
             case 5:
                 l = DriverStationLCD.Line.kUser6;
                 break;
             case 6:
                 l = DriverStationLCD.Line.kMain6;
                 break;
             default:
                 l = DriverStationLCD.Line.kUser2;
                 break;
         }
         DriverStationLCD.getInstance().println(l, 1, msg);
         DriverStationLCD.getInstance().updateLCD();
     }
     
 }
