 package eu.uberdust.application.foi.task;
 
 /**
  * Created by IntelliJ IDEA.
  * User: dimitris
  * Date: 4/5/12
  * Time: 2:42 PM
  * To change this template use File | Settings | File Templates.
  */
 
 
 import eu.uberdust.application.foi.manager.LockManager;
 import eu.uberdust.application.foi.manager.LuminosityManager;
 import eu.uberdust.application.foi.manager.RoomZoneManager;
 import eu.uberdust.application.foi.manager.WorkstationZoneManager;
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
 
     private int ScreenState;
 
 
     public TurnOffTask_2(final Timer thatTimer) {
         super();
         this.timer = thatTimer;
 
     }
 
     @Override
     public final void run() {
         LOGGER.info("TurnOffTask_2: Task to turn off Light_2 initialized");
 
        ScreenState = LockManager.getInstance().getCurrentState();
 
         if (ScreenState == LockManager.SCREEN_LOCKED) {
                 LOGGER.info("TurnOffTask_2: Turn off All lights " );
            if(LuminosityManager.getInstance().getCurrentState() == LuminosityManager.DARKLY){
 
                 WorkstationZoneManager.getInstance().switchOffFirst();
 
            } else if(LuminosityManager.getInstance().getCurrentState() == LuminosityManager.TOTAL_DARKNESS){
 
             if(!WorkstationZoneManager.getInstance().isSingleZone()){
 
                  WorkstationZoneManager.getInstance().switchOffSecond();
 
                 } else{
 
                  WorkstationZoneManager.getInstance().switchOffFirst();
              }
 
            }
         }
 
     }
 
 }
