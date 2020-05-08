 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package util;
 
 import edu.wpi.first.wpilibj.DriverStation;
 import edu.wpi.first.wpilibj.DriverStationLCD;
 
 /**
  *
  * @author Merfoo
  */
 public class Config 
 {
     // Motors
     public static final int chanDriveLeft1 = 4;
     public static final int chanDriveLeft2= 5;
     public static final int chanDriveLeft3 = 6;
     public static final int chanDriveRight1 = 1;
     public static final int chanDriveRight2 = 2;
     public static final int chanDriveRight3 = 3;
 
     // Ints
     public static final int iPs3Port = 1;
     public static final int iPs3Buttons = 13;
     
     // Button channel on the joystick
     public static final int btRecord = 9;
     public static final int btAllowEdit = 10;
     public static final int btReplay = 12;
     
     // DriverStation Autonomous Button Channels
     public static final int stDigInAutoCtr = 1;
     public static final int stDigInAutoLft = 2;
     public static final int stDigInAutoRght = 3;
     public static final int stDigInReg = 4;
     
     // Driverstation line
     public static final int stLineFile = 2;
     public static final int stLineAutonomous = 3;
     
      /**
      * Gets the button status from the driverstation, 1 - 8 available.
      * @param iChan
      * @return 
      */
     public static boolean StationGetDigitalIn(int iChan)
     {
         return DriverStation.getInstance().getDigitalIn(iChan);
     }
     
     /**
      * Sets the double to be only at the hundreth's place, ex. 12.34.
      */
     public static double SetDoublePrecision(double dDouble)
     {
         return (Double.valueOf(Math.floor(dDouble * 10 + 0.5) / 10)).doubleValue();
     }
     
     /**
      * Prints specified message to the driver station on the corresponding line
      * 1-6 are available.
      */
     public static void PrintToStation(int iLine, String sMessage)
     {
         switch(iLine)
         {
             case 1:
             {
                 DriverStationLCD.getInstance().println(DriverStationLCD.Line.kUser1, 1, "                   ");
                 DriverStationLCD.getInstance().updateLCD(); 
                 DriverStationLCD.getInstance().println(DriverStationLCD.Line.kUser1, 1, sMessage);
                 break;
             }
             
             case 2:
             {
                 DriverStationLCD.getInstance().println(DriverStationLCD.Line.kUser2, 1, "                   ");
                 DriverStationLCD.getInstance().updateLCD(); 
                 DriverStationLCD.getInstance().println(DriverStationLCD.Line.kUser2, 1, sMessage);
                 break;
             }
                 
             case 3:
             {
                 DriverStationLCD.getInstance().println(DriverStationLCD.Line.kUser3, 1, "                   ");
                 DriverStationLCD.getInstance().updateLCD(); 
                 DriverStationLCD.getInstance().println(DriverStationLCD.Line.kUser3, 1, sMessage);
                 break;
             }
                 
             case 4:
             {
                 DriverStationLCD.getInstance().println(DriverStationLCD.Line.kUser4, 1, "                   ");
                 DriverStationLCD.getInstance().updateLCD(); 
                 DriverStationLCD.getInstance().println(DriverStationLCD.Line.kUser4, 1, sMessage);
                 break;
             }
                 
             case 5:
             {
                 DriverStationLCD.getInstance().println(DriverStationLCD.Line.kUser5, 1, "                   ");
                 DriverStationLCD.getInstance().updateLCD(); 
                 DriverStationLCD.getInstance().println(DriverStationLCD.Line.kUser5, 1, sMessage);
                 break;
             }
                 
             case 6:
             {
                 DriverStationLCD.getInstance().println(DriverStationLCD.Line.kUser6, 1, "                   ");
                 DriverStationLCD.getInstance().updateLCD(); 
                 DriverStationLCD.getInstance().println(DriverStationLCD.Line.kUser6, 1, sMessage);
                 break;
             }
         }
         
         DriverStationLCD.getInstance().updateLCD(); 
     }
 }
