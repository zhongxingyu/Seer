 package eu.uberdust.lights.tasks;
 
 /**
  * Created by IntelliJ IDEA.
  * User: dimitris
  * Date: 4/5/12
  * Time: 2:42 PM
  * To change this template use File | Settings | File Templates.
  */
 
 import eu.uberdust.MainApp;
 import eu.uberdust.lights.FoiController;
 import eu.uberdust.lights.GetJson;
 import org.apache.log4j.Logger;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 /**
  * Simple task to switch off lights.
  */
 public class TurnOffTask_2 extends TimerTask {
 
     /**
      * Static Logger.
      */
     private static final Logger LOGGER = Logger.getLogger(TurnOffTask_2.class);
 
 
     private final Timer timer;
 
    public static long DELAY = Long.parseLong(GetJson.getInstance().callGetJsonWebService(FoiController.USER_PREFERENCES,"delay"))*1000;
 
     private boolean isScreenLocked;
 
 
     public TurnOffTask_2(final Timer thatTimer) {
         super();
         this.timer = thatTimer;
 
     }
 
     @Override
     public final void run() {
         LOGGER.info("TurnOffTask_2: Task to turn off Light_2 initialized");
 
         isScreenLocked = FoiController.getInstance().isScreenLocked();
 
         if (isScreenLocked) {
                 LOGGER.info("TurnOffTask_2: Turn off zone " + MainApp.ZONES[0] + " " + MainApp.FOI + " " + isScreenLocked);
                 FoiController.getInstance().controlLight(false, Integer.parseInt(MainApp.ZONES[0]));
         }
 
     }
 
 }
