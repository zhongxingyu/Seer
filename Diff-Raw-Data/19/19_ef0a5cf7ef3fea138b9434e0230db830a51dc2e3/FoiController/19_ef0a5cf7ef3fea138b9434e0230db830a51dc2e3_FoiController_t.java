 package eu.uberdust.application.foi.controller;
 
 
 import eu.uberdust.application.foi.MainApp;
 import eu.uberdust.application.foi.manager.*;
 import eu.uberdust.application.foi.task.TurnOffTask_2;
 import eu.uberdust.communication.UberdustClient;
 import eu.uberdust.communication.websocket.readings.WSReadingsClient;
 import eu.uberdust.util.PropertyReader;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import java.util.*;
 
 /**
  * Basic functionality.
  */
 public final class FoiController implements Observer {
 
     /**
      * Static Logger.
      */
     private static final Logger LOGGER = Logger.getLogger(FoiController.class);
     private static final String HTTP_PREFIX = "http://";
     private static final String WS_PREFIX = "ws://";
 
     private static final String URN_FOI = "urn:wisebed:ctitestbed:virtual:" + MainApp.ACTUATOR_MODE + ":" + MainApp.FOI;
 
     private static final String FOI_CAPABILITIES = "http://uberdust.cti.gr/rest/testbed/1/node/urn:wisebed:ctitestbed:virtual:" + MainApp.ACTUATOR_MODE + ":" + MainApp.FOI + "/capabilities/json";
 
     private long lockscreenDelay;
 
 
     /**
      * Pir timer.
      */
     private final Timer timer;
 
     /**
      * static instance(ourInstance) initialized as null.
      */
     private static FoiController ourInstance = null;
 
 
     private String uberdustUrl;
 
     /**
      * FoiController is loaded on the first execution of FoiController.getInstance()
      * or the first access to FoiController.ourInstance, not before.
      *
      * @return ourInstance
      */
     public static FoiController getInstance() {
         synchronized (FoiController.class) {
             if (ourInstance == null) {
                 ourInstance = new FoiController();
             }
         }
         return ourInstance;
     }
 
 
     /**
      * Private constructor suppresses generation of a (public) default constructor.
      */
     private FoiController() {
 
         PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("log4j.properties"));
         LOGGER.info("FOI Controller initializing...");
 
         timer = new Timer();
 
         LOGGER.info(MainApp.MODE);
 
         try {
             lockscreenDelay = Long.parseLong(ProfileManager.getInstance().getElement("lockscreen_delay")) * 1000;
         } catch (NumberFormatException npe) {
             lockscreenDelay = 1000;
         }
 
 
         String uberdustUrl = PropertyReader.getInstance().getProperties().getProperty("uberdust.url") != null ?
                 PropertyReader.getInstance().getProperties().getProperty("uberdust.url") : "uberdust.cti.gr:80";
 
         uberdustUrl = uberdustUrl.replaceAll(HTTP_PREFIX, "");
 
         UberdustClient.setUberdustURL(HTTP_PREFIX + uberdustUrl);
 
         LOGGER.info(MainApp.FOI);
 
         WSReadingsClient.getInstance().setServerUrl(WS_PREFIX + uberdustUrl + "/readings.ws");
 
 
         if ("Workstation".equals(MainApp.MODE)) {
 
             //Subscription for notifications.
             LockManager.getInstance().addObserver(this);
             LuminosityManager.getInstance().addObserver(this);
 
 
             // subscribe to specific readings
             WSReadingsClient.getInstance().subscribe(URN_FOI, MainApp.CAPABILITY_SCREENLOCK);
             WSReadingsClient.getInstance().subscribe(URN_FOI, MainApp.CAPABILITY_LIGHT);
 
             //Adding Observer for the last readings
             WSReadingsClient.getInstance().addObserver(LockManager.getInstance());
             WSReadingsClient.getInstance().addObserver(LuminosityManager.getInstance());
 
 
         } else if ("PublicRoom".equals(MainApp.MODE)) {
 
             PresenceManageR.getInstance().addObserver(this);
 
             WSReadingsClient.getInstance().subscribe(URN_FOI, MainApp.CAPABILITY_PIR);
 
 //            WSReadingsClient.getInstance().subscribe(URN_FOI, MainApp.CAPABILITY_LIGHT);
 
             //Adding Observer for the last readings
 
             WSReadingsClient.getInstance().addObserver(PresenceManageR.getInstance());
 
         } else if ("ichatzWorkstation".equals(MainApp.MODE) || "ichatzRoom".equals(MainApp.MODE) || "ichatz".equals(MainApp.MODE)) {
 
             //Subscription for notifications.
             LockManager.getInstance().addObserver(this);
             LuminosityManager.getInstance().addObserver(this);
             PresenceManageR.getInstance().addObserver(this);
 
 
             // subscribe to specific readings
             WSReadingsClient.getInstance().subscribe(URN_FOI, MainApp.CAPABILITY_SCREENLOCK);
             WSReadingsClient.getInstance().subscribe(URN_FOI, MainApp.CAPABILITY_LIGHT);
             WSReadingsClient.getInstance().subscribe(URN_FOI, MainApp.CAPABILITY_PIR);
 
             //Adding Observer for the last readings
             WSReadingsClient.getInstance().addObserver(LockManager.getInstance());
             WSReadingsClient.getInstance().addObserver(LuminosityManager.getInstance());
             WSReadingsClient.getInstance().addObserver(PresenceManageR.getInstance());
 
         } else if("SingleLightPir".equals(MainApp.MODE)){
 
             //Subscription for notifications.
             LuminosityManager.getInstance().addObserver(this);
             PresenceManageR.getInstance().addObserver(this);
 
             // subscribe to specific readings
             WSReadingsClient.getInstance().subscribe(URN_FOI, MainApp.CAPABILITY_LIGHT);
             WSReadingsClient.getInstance().subscribe(URN_FOI, MainApp.CAPABILITY_PIR);
 
             //Adding Observer for the last readings
             WSReadingsClient.getInstance().addObserver(LuminosityManager.getInstance());
             WSReadingsClient.getInstance().addObserver(PresenceManageR.getInstance());
 
 
         }
 
 
         LOGGER.info("FOIController Initialized");
 
     }
 
 
     public void WorkstationHandler(){
 
 
         if (LockManager.getInstance().getCurrentState() == LockManager.SCREEN_UNLOCKED) {
 
             if (LuminosityManager.getInstance().getCurrentState() == LuminosityManager.DARKLY) {
 
                 if(WorkstationZoneManager.getInstance().isSingleZone()){
 
                    WorkstationZoneManager.getInstance().switchOnFirst();
 
                } else{
                     WorkstationZoneManager.getInstance().switchOnFirst();
                     WorkstationZoneManager.getInstance().switchLastOff();
                 }
 
 
             } else if (LuminosityManager.getInstance().getCurrentState() == LuminosityManager.TOTAL_DARKNESS) {
 
                 WorkstationZoneManager.getInstance().switchOnAll();
 
             } else if (LuminosityManager.getInstance().getCurrentState() == LuminosityManager.BRIGHT) {
 
                 WorkstationZoneManager.getInstance().switchOffAll();
 
             }
         } else if (LockManager.getInstance().getCurrentState() == LockManager.SCREEN_LOCKED) {
 
             if(LuminosityManager.getInstance().getCurrentState() == LuminosityManager.TOTAL_DARKNESS){
 
                 timer.schedule(new TurnOffTask_2(timer), lockscreenDelay);
 
                 if(!WorkstationZoneManager.getInstance().isSingleZone()){
 
                     WorkstationZoneManager.getInstance().switchOffFirst();
                 }
 
             }else if(LuminosityManager.getInstance().getCurrentState() == LuminosityManager.DARKLY){
 
                 timer.schedule(new TurnOffTask_2(timer), lockscreenDelay);
 
             }
         }
 
 
     }
 
 
     public void ichatzWorkstationHandler(){
 
         if (LockManager.getInstance().getCurrentState() == LockManager.SCREEN_UNLOCKED) {
 
             if (LuminosityManager.getInstance().getCurrentState() == LuminosityManager.DARKLY) {
 
                 WorkstationZoneManager.getInstance().switchOnFirst();
                 WorkstationZoneManager.getInstance().switchOffSecond();
 
             } else if (LuminosityManager.getInstance().getCurrentState() == LuminosityManager.TOTAL_DARKNESS) {
 
                 WorkstationZoneManager.getInstance().switchOnFirstTwo();
 
             } else if (LuminosityManager.getInstance().getCurrentState() == LuminosityManager.BRIGHT) {
 
                 WorkstationZoneManager.getInstance().switchOffAll();
 
             }
         } else if (LockManager.getInstance().getCurrentState() == LockManager.SCREEN_LOCKED) {
 
             if(LuminosityManager.getInstance().getCurrentState() == LuminosityManager.TOTAL_DARKNESS){
 
                 if(PresenceManageR.getInstance().getCurrentState() == PresenceManageR.EMPTY){
 
                     timer.schedule(new TurnOffTask_2(timer), lockscreenDelay);
 
                 }
 
                 WorkstationZoneManager.getInstance().switchOffFirst();
 
             }else if(LuminosityManager.getInstance().getCurrentState() == LuminosityManager.DARKLY){
 
                     timer.schedule(new TurnOffTask_2(timer), lockscreenDelay);
 
             }
         }
 
 
     }
 
 
     public void PublicRoomHandler(){
 
         switch (PresenceManageR.getInstance().getCurrentState()) {
             case PresenceManageR.EMPTY:
                 RoomZoneManager.getInstance().switchOffAll();
                 break;
             case PresenceManageR.LEFT:
                 RoomZoneManager.getInstance().switchLastOff();
                 break;
             case PresenceManageR.NEW_ENTRY:
                 RoomZoneManager.getInstance().switchOnFirst();
                 break;
             case PresenceManageR.OCCUPIED:
                 RoomZoneManager.getInstance().switchOnAll();
                 break;
         }
 
 
     }
 
 
     public void SingleLightPir() {
 
         switch (PresenceManageR.getInstance().getCurrentState()) {
             case PresenceManageR.EMPTY:
                 WorkstationZoneManager.getInstance().switchLastOff();
                 break;
             case PresenceManageR.OCCUPIED:
                 WorkstationZoneManager.getInstance().switchOnLast();
                 break;
         }
 
     }
 
     public  void ichatzPresenceHandler() {
 
         if (LuminosityManager.getInstance().getCurrentState() == LuminosityManager.BRIGHT) {
 
             WorkstationZoneManager.getInstance().switchOffAll();
 
         } else {
 
             if (LockManager.getInstance().getCurrentState() == LockManager.SCREEN_UNLOCKED) {
 
                 if(LuminosityManager.getInstance().getCurrentState() == LuminosityManager.DARKLY){
 
 
 
                     WorkstationZoneManager.getInstance().switchOffSecond();
 
                 }
 
                 SingleLightPir();
 
             } else if (LockManager.getInstance().getCurrentState() == LockManager.SCREEN_LOCKED) {
 
                 switch (PresenceManageR.getInstance().getCurrentState()) {
                     case PresenceManageR.EMPTY:
                         WorkstationZoneManager.getInstance().switchOffAll();
                         break;
                     case PresenceManageR.LEFT:
                         WorkstationZoneManager.getInstance().switchLastOff();
                         break;
                     case PresenceManageR.NEW_ENTRY:
                         WorkstationZoneManager.getInstance().switchOnSecond();
                         break;
                     case PresenceManageR.OCCUPIED:
                         WorkstationZoneManager.getInstance().switchOnLastTwo();
                         break;
                 }
 
             }
 
         }
     }
 
     private void SingleLightPirHandler() {
 
         if (LuminosityManager.getInstance().getCurrentState() == LuminosityManager.DARKLY ||
                 LuminosityManager.getInstance().getCurrentState() == LuminosityManager.TOTAL_DARKNESS) {
 
             SingleLightPir();

        } else if(LuminosityManager.getInstance().getCurrentState() == LuminosityManager.BRIGHT){

            WorkstationZoneManager.getInstance().switchOffAll();

         }
 
     }
 
 
 
     @Override
     public void update(Observable o, Object arg) {
 
         if (o instanceof LockManager) {
 
             if(MainApp.MODE.equals("Workstation")){
 
                 WorkstationHandler();
 
             }else if(MainApp.MODE.equals("ichatzRoom")){
 
                 ichatzPresenceHandler();
 
             } else if(MainApp.MODE.equals("ichatz")){
 
                 ichatzPresenceHandler();
                 ichatzWorkstationHandler();
 
             }
 
         }else if(o instanceof PresenceManageR){
 
             if(MainApp.MODE.equals("PublicRoom")){
 
                 PublicRoomHandler();
 
             } else if(MainApp.MODE.equals("ichatzRoom")){
 
                 ichatzPresenceHandler();
 
             } else if(MainApp.MODE.equals("SingleLightPir")){
 
                 SingleLightPirHandler();
 
             } else if(MainApp.MODE.equals("ichatz")){
 
                 ichatzPresenceHandler();
                 ichatzWorkstationHandler();
 
             }
 
 
         }else if(o instanceof LuminosityManager){
 
             if(MainApp.MODE.equals("Workstation")){
 
                 WorkstationHandler();
 
             }else if(MainApp.MODE.equals("ichatzRoom")){
 
                 ichatzPresenceHandler();
 
             } else if(MainApp.MODE.equals("ichatzWorkstation")){
 
                 ichatzWorkstationHandler();
 
             } else if(MainApp.MODE.equals("SingleLightPir")){
 
                 SingleLightPirHandler();
 
             } else if(MainApp.MODE.equals("ichatz")){
 
                 ichatzPresenceHandler();
                 ichatzWorkstationHandler();
 
             }
 
         }
     }
 
 
 
 
     public static void main(final String[] args) {
         FoiController.getInstance();
     }
 
 
 }
