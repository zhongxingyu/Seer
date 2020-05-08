 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package littlesmarttool2.util;
 
 import java.io.IOException;
import java.util.ArrayList;
 import java.util.concurrent.TimeoutException;
 import littlesmarttool2.comm.SerialCommand;
 import littlesmarttool2.comm.SerialController;
 
 /**
  *
  * @author Rasmus
  */
 public class PulseDataRecorder {
     private SerialController controller;
     private SerialCommand[] oldPulseData = new SerialCommand[0];
     private final int POSITION = 1;
     public PulseDataRecorder(SerialController controller)
     {
         this.controller = controller;
     }
     
     /**
      * Backup the pulsedata at the given position to memory
      */
     /*public void backupPulseData()
     {
         try {
             oldPulseData = controller.getIRTimings(POSITION, 10000);
         } catch (IOException | TimeoutException ex) {
             System.out.println("Backup failed: " + ex.getMessage());
         }
     }*/
     
     /**
      * Set the pulsedata on the given position to some specific data
      * @param timings The pulsedata to store
      * @throws TimeoutException On timeout
      * @throws IOException On other errors
      */
     /*public void setPulseData(int[] timings) throws TimeoutException, IOException
     {
         for (int i = 0; i < timings.length; i+=2)
         {
             controller.send("I;" + POSITION + ";" + i +  ";" + timings[i] + ";" + timings[i+1], 5000);
         }
         controller.send("M", 5000);
     }*/
     
     /**
      * Restore the pulsedata backed up from backupPulseData()
      */
     /*public void restorePulseData()
     {
         for (SerialCommand cmd : oldPulseData)
         {
             try {
                 controller.send(cmd, 1000);
             } catch (IOException | TimeoutException ex) {
                 System.out.println("Restore failed: " + ex.getMessage());
             }
         }
         try {
             controller.send("M", 5000);
         } catch (IOException | TimeoutException ex) {
             
         }
     }*/
     
     /**
      * Record some pulsedata from the IR receiver
      * @return The recorded pulsedata
      * @throws IOException
      * @throws TimeoutException 
      */
     public int[] recordPulseData() throws IOException, TimeoutException
     {
         String response = controller.send("J;" + POSITION, 10000);
         if (!response.equals("J;1")) throw new TimeoutException("The StratoSnapper timed out while waiting for IR input or the sequenze was too long");
         
         controller.send("M",10000);
         
         SerialCommand[] pulseData = controller.getIRTimings(POSITION, 10000);
        
         int[] timings = new int[pulseData.length*2];
         for (int i = 0; i < pulseData.length; i++)
         {
             int[] args = pulseData[i].convertArgsToInt();
            timings[args[1]-1] = args[2];
            timings[args[1]] = args[3];
         }
        //Count zeros in pairs
        int zeros = 0;
        for (int i = timings.length-1; i > 0; i-=2)
            if (timings[i] == 0 && timings[i-1] == 0) zeros+=2;
        
        int[] reducedTimings = new int[timings.length-zeros];
        System.arraycopy(timings, 0, reducedTimings, 0, reducedTimings.length);
        
        return reducedTimings;
     }
     
     public void playbackRecording() throws TimeoutException, IOException
     {
         controller.send("H;"+POSITION, 5000);
     }
     
 }
