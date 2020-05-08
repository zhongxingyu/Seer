 package autonomous;
 
 import utilities.FileWriter;
 import utilities.Robot;
 import utilities.BotData;
 import utilities.Vars;
 import java.util.Vector;
 import edu.wpi.first.wpilibj.Timer;
 
 /**
  * This class is responsible for writing the bots activities to the specified 
  * file on the cRio.
  * @author Fauzi
  */
 class Recorder {
 
     private int m_Index = 0;
     private boolean m_bRecStarted = false;
     private boolean m_bRecDone = false;
     private boolean m_bVerboseMode = true;
     private String m_sFile = "";
     private Timer m_tmRecorder = new Timer();
     private Vector m_List = new Vector();
     private BotData m_botDataAuto = null;
     private FileWriter m_fileWriter = null;
     private Robot m_bot = null;
     
     public Recorder(Robot robot)
     {
         m_bot = robot;
     }
     
     /**
      * Records bots movements to specified file.
      * @param sFileName 
      */
     public void record(String sFileName)
     {        
         if(!m_bRecStarted)
         {
             if(m_bVerboseMode)
                 System.out.println("Recording!!!");
 			
             m_sFile = sFileName;
             m_tmRecorder.start();
             m_bRecStarted = true;
         }
         
         if(!m_bRecDone)
         {
             /*
              * Have to reinitlalize because adding the object into the list
              * it actually just makes a copy that also points to the original. 
              * So if the original is changed so will the data in the object list.
              */
             m_Index++;
             m_botDataAuto = new BotData();			
             m_botDataAuto.setValues(m_tmRecorder.get(), m_bot);
             m_List.addElement(m_botDataAuto);
         }
         
         else
         {
             Vars.fnSetAllowDrive(false);
             Vars.fnSetAllowShooting(false);
             m_bot.stopRobot();
             m_tmRecorder.stop();
             m_tmRecorder.reset();
         }
     }
     
     /**
      * Resets the recorder so we can record properly next time.
      */
     public void reset()
     {
         if(m_bRecStarted)
         {
            Vars.fnSetAllowDrive(true);
            Vars.fnSetAllowShooting(true);
             writeDataToFile();
             m_List.removeAllElements();
             m_Index = 0;
             m_tmRecorder.stop();
             m_tmRecorder.reset();
             m_sFile = "";
             m_botDataAuto = null;
             m_bRecDone = false;
             m_bRecStarted = false;
         }
     }
     
     /**
      * Stops recording.
      */
     public void stop()
     {
         m_bRecStarted = true;
         m_bRecDone = true;
     }
     
     /** 
      * Gets the time value of the recording, as in how long it has been 
      * replaying
      * @return 
      */
     public double getRecordTime()
     {
         return Vars.fnSetPrecision(m_tmRecorder.get());
     }
     
     /**
      * Actually writes the data to file.
      */
     private void writeDataToFile()
     {
         m_fileWriter = new FileWriter(m_sFile);
         m_fileWriter.writeInt(m_Index);
 
         for(int iPos = 0; iPos < m_Index; iPos++)
         {
             m_botDataAuto.setValues((BotData) m_List.elementAt(iPos));
 			
             if(m_bVerboseMode)
                 System.out.println("Left: " + m_botDataAuto.getMotorLeftSpeed() + 
                     " - Right: " + m_botDataAuto.getMotorRightSpeed() + 
                     " - Shooter: " + m_botDataAuto.getShooterSpeed() + 
                     " - Feed: " + m_botDataAuto.getFeederStatus()); 
 			
             m_fileWriter.writeDouble(m_botDataAuto.getTimer());
             m_fileWriter.writeDouble(m_botDataAuto.getMotorLeftSpeed());
             m_fileWriter.writeDouble(m_botDataAuto.getMotorRightSpeed());
             m_fileWriter.writeDouble(m_botDataAuto.getShooterSpeed());
             m_fileWriter.writeBoolean(m_botDataAuto.getFeederStatus());
         }
 
         m_fileWriter.close();
     }
 }
