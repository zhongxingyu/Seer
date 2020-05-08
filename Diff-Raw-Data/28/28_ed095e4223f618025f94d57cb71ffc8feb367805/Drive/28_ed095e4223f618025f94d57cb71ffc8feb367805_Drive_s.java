 package core;
 import utilities.Vars;
 import utilities.Button;
 import edu.wpi.first.wpilibj.Victor;
 import edu.wpi.first.wpilibj.Joystick;
 
 /**
  * @author fauzi
  */
 
 public class Drive {
     
     // CONSTANTS 
     private final double m_dMinSpeed = 0.05;
     private final double m_dMaxIncrease = .1;
     
     private Victor m_mtRight = new Victor(Vars.chnVicDrvRight);
     private Victor m_mtLeft = new Victor(Vars.chnVicDrvLeft);
     private double m_dRightSpeed = 0;
     private double m_dLeftSpeed = 0;
     private Button m_btChangeDrive = new Button();
     private Button m_btReverseDrive = new Button();
     private String m_sDriveStatus = "";
     private Joystick m_joy;
     
     public Drive(Joystick joystick)
     {
         m_joy = joystick;
     }
     
     public void run()
     {      
         m_btChangeDrive.run(m_joy.getRawButton(Vars.btChangeDrive));
         m_btReverseDrive.run(m_joy.getRawButton(Vars.btReverseDrive));
         
         if(Vars.bDrive)
         {
             if(m_btChangeDrive.getSwitch())
                 tankDrive();
 
             else
                 regDrive();
             
             if(m_btReverseDrive.getSwitch())
             {
                 m_dLeftSpeed = -m_dLeftSpeed;
                 m_dRightSpeed = -m_dRightSpeed;
             }
             
             if(Math.abs(m_dLeftSpeed) + Math.abs(m_dRightSpeed) > m_dMinSpeed)
                 this.setSpeed(m_dLeftSpeed, m_dRightSpeed);
 
             else
                 this.setSpeed(0, 0);
         }
         
         else
             m_sDriveStatus = "Disabled";
         
         Vars.drvStationPrinter.print(Vars.iDriveStatusLine, m_sDriveStatus + getReverseStat());
     }
     
     private void tankDrive()
     {
         // Setting to get Tank Drive Working properly on the ps3 Controller
         // should be 2, 4
         m_joy.setAxisChannel(Joystick.AxisType.kX, 2);
         m_joy.setAxisChannel(Joystick.AxisType.kY, 4);
 
         m_sDriveStatus = "Tank Drive";
         m_dLeftSpeed = m_joy.getX() * Math.abs(m_joy.getX());
         m_dRightSpeed = -m_joy.getY() * Math.abs(m_joy.getY());  
     }
 
     private void regDrive()
     {
         // Setting to get regular drive Working properly on the ps3 Controller
         // should be 3, 2
         m_joy.setAxisChannel(Joystick.AxisType.kX, 3);
         m_joy.setAxisChannel(Joystick.AxisType.kY, 2);
 
         m_sDriveStatus = "Regular Drive";
         double y = m_joy.getY() * Math.abs(m_joy.getY());
         double x = m_joy.getX() * Math.abs(m_joy.getX());
 
         m_dRightSpeed = (-y+x);
         m_dLeftSpeed = (y+x);  
     }
 	
     public void setSpeed(double setMtLeft, double setMtRight)
     {
         overDrive(setMtLeft, setMtRight);
         //ramp(setMtLeft, setMtRight);
     }
     
     public double getMtRightSpeed()
     {
         return m_dRightSpeed;
     }
     
     public double getMtLeftSpeed()
     {
         return m_dLeftSpeed;
     }
     
     private String getReverseStat()
     {
         if(m_btReverseDrive.getSwitch())
             return "Reversed";
         
         else
             return "";
     }
     
     private void ramp(double dLeftSpeed, double dRightSpeed)
     {
         final double dCurrentLeft = m_mtLeft.get();
         final double dCurrentRight = m_mtRight.get();
         
         if(Math.abs(Math.abs(dCurrentLeft) - Math.abs(dLeftSpeed)) > m_dMaxIncrease)
             m_mtLeft.set(dLeftSpeed < 0 ? dCurrentLeft - m_dMaxIncrease : dCurrentLeft + m_dMaxIncrease);
         
         if(Math.abs(Math.abs(dCurrentRight) - Math.abs(dRightSpeed)) > m_dMaxIncrease)
             m_mtRight.set(dRightSpeed < 0 ? dCurrentRight - m_dMaxIncrease : dCurrentRight + m_dMaxIncrease);
     }
     
     private void overDrive(double dLeftSpeed, double dRightSpeed)
     {
         // Get the difference between the current speed and new speed,
        // times the current speed by the difference to amplify it
         // added one to the difference incase the difference is zero 
         // in which case the speed change
         
         final double dCurrentLeft = m_mtLeft.get();
         final double dCurrentRight = m_mtRight.get();
        final double dLeftDiff = Math.abs(Math.abs(dCurrentLeft) - Math.abs(dLeftSpeed)) + 1;
        final double dRightDiff = Math.abs(Math.abs(dCurrentRight) - Math.abs(dRightSpeed)) + 1;
         
        m_mtLeft.set(dCurrentLeft * dLeftDiff);
        m_mtRight.set(dCurrentRight * dRightDiff);
     }
 }
