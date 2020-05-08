 package eu.uberdust.lights;
 
 import eu.uberdust.MainApp;
 import eu.uberdust.communication.UberdustClient;
 import eu.uberdust.communication.rest.RestClient;
 import eu.uberdust.communication.websocket.readings.WSReadingsClient;
 import eu.uberdust.lights.tasks.LightTask;
 import eu.uberdust.lights.tasks.TurnOffTask_2;
 import eu.uberdust.lights.tasks.TurnOffTask_3;
 import eu.uberdust.lights.tasks.TurnOffTask_4;
 import eu.uberdust.util.PropertyReader;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import java.util.PriorityQueue;
 import java.util.Queue;
 import java.util.Timer;
 
 /**
  * Basic functionality.
  */
 public final class FoiController {
 
     /**
      * Static Logger.
      */
     private static final Logger LOGGER = Logger.getLogger(FoiController.class);
     private static final String HTTP_PREFIX = "http://";
     private static final String WS_PREFIX = "ws://";
 
     private boolean zone1;
 
     private boolean zone2;
 
     private boolean zone3;
 
     private long lastPirReading;
 
     private long zone1TurnedOnTimestamp;
 
     private long zone2TurnedOnTimestamp;
 
     private boolean isScreenLocked;
 
     private boolean flag;
 


     private static final int WINDOW = 10;
 
     private static Queue luminosityReadings = new PriorityQueue(WINDOW);
 
    public static final String USER_PREFERENCES = "http://150.140.16.31/api/v1/foi?identifier=" + MainApp.FOI;
 
    private static String mode = GetJson.getInstance().callGetJsonWebService(USER_PREFERENCES, "mode");
 
    private static final String URN_FOI = "urn:wisebed:ctitestbed:virtual:"+ mode +":"+ MainApp.FOI;
 
    private static final String SENSOR_SCREENLOCK_REST = "/rest/testbed/1/node/urn:wisebed:ctitestbed:virtual:" + mode +":"+ MainApp.FOI + "/capability/urn:wisebed:ctitestbed:node:capability:lockScreen/tabdelimited/limit/1";
 
    private static final String FOI_CAPABILITIES = "http://uberdust.cti.gr/rest/testbed/1/node/urn:wisebed:ctitestbed:virtual:" + mode +":"+ MainApp.FOI + "/capabilities/json";
 
    private static final String ACTUATOR_URL = "http://uberdust.cti.gr/rest/testbed/1/node/urn:wisebed:ctitestbed:virtual:" + mode +":"+ MainApp.FOI + "/capability/urn:wisebed:node:capability:lz" + MainApp.ZONES[0] + "/json/limit/1";
 
    private static final String FOI_ACTUATOR = GetJson.getInstance().callGetJsonWebService(ACTUATOR_URL, "nodeId").split("0x")[1];
 
     private long lockscreenDelay;
 
     public long getLockscreenDelay() {
         return lockscreenDelay;
     }
 
     private long pirDelay;
 
     public long getPirDelay() {
         return pirDelay;
     }
 
     private long firstCall = 0;
 
     /**
      * Pir timer.
      */
     private final Timer timer;
 
     /**
      * static instance(ourInstance) initialized as null.
      */
     private static FoiController ourInstance = null;
 
     private double lumThreshold1;
 
     private double lumThreshold2;
 
     public static boolean BYPASS = false;
 
     private double lastLumReading;
 
     private double Median;
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
 
     private void updateLum1Threshold() {
         try {
             lumThreshold1 = Double.parseDouble(GetJson.getInstance().callGetJsonWebService(USER_PREFERENCES, "illumination"));  //350
         } catch (NullPointerException npe) {
             lumThreshold1 = 350;
         }
     }
 
     private void updateLum2Threshold() {
         try {
             lumThreshold2 = Double.parseDouble(GetJson.getInstance().callGetJsonWebService(USER_PREFERENCES, "illumination2"));  //350
         } catch (NullPointerException npe) {
             lumThreshold2 = 350;
         }
     }
 
     /**
      * Private constructor suppresses generation of a (public) default constructor.
      */
     private FoiController() {

         PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("log4j.properties"));
         LOGGER.info("FOI Controller initializing...");
         mode = GetJson.getInstance().callGetJsonWebService(USER_PREFERENCES, "mode");

         LOGGER.info(mode);
         try {
             lockscreenDelay = Long.parseLong(GetJson.getInstance().callGetJsonWebService(USER_PREFERENCES, "lockscreen_delay")) * 1000;
         } catch (NumberFormatException npe) {
             lockscreenDelay = 1000;
         }
         try {
             pirDelay = Long.parseLong(GetJson.getInstance().callGetJsonWebService(FoiController.USER_PREFERENCES, "pir_delay")) * 1000;
         } catch (NumberFormatException npe) {
             pirDelay = 1000;
         }
         updateLum1Threshold();
         updateLum2Threshold();
 
 
         String uberdustUrl = PropertyReader.getInstance().getProperties().getProperty("uberdust.url") != null ?
                 PropertyReader.getInstance().getProperties().getProperty("uberdust.url") : "uberdust.cti.gr:80";
         uberdustUrl = uberdustUrl.replaceAll(HTTP_PREFIX, "");
 
         UberdustClient.setUberdustURL(HTTP_PREFIX + uberdustUrl);
         timer = new Timer();
 
         LOGGER.info("lastLumReading -- " + lastLumReading);
         LOGGER.info("isScreenLocked -- " + isScreenLocked);
 
         zone1 = false;
         zone2 = false;
         zone3 = false;
 
        LOGGER.info(MainApp.FOI);
 
         WSReadingsClient.getInstance().setServerUrl(WS_PREFIX + uberdustUrl + "/readings.ws");
 
 
         initLum();//Not Needed (Readings come in fast enough to wait for that)//RestClient.getInstance().callRestfulWebService(MainApp.SENSOR_LIGHT_READINGS_REST));
         setLastLumReading(0);//setLastLumReading(Double.valueOf(RestClient.getInstance().callRestfulWebService(MainApp.SENSOR_LIGHT_READINGS_REST).split("\t")[1]));
         WSReadingsClient.getInstance().subscribe(URN_FOI, MainApp.CAPABILITY_LIGHT);
 
         //Subscription for notifications.
         String mode = GetJson.getInstance().callGetJsonWebService(USER_PREFERENCES, "mode");
         if ("workstation".equals(mode)) {
 
             setScreenLocked((Double.valueOf(RestClient.getInstance().callRestfulWebService(HTTP_PREFIX + uberdustUrl + SENSOR_SCREENLOCK_REST).split("\t")[1]) == 1) || (Double.valueOf(RestClient.getInstance().callRestfulWebService(HTTP_PREFIX + uberdustUrl + SENSOR_SCREENLOCK_REST).split("\t")[1]) == 3));
             WSReadingsClient.getInstance().subscribe(URN_FOI, MainApp.CAPABILITY_SCREENLOCK);
 
         } else if ("room".equals(mode)) {
 
             //setScreenLocked(false);
             //DUPLICATE of (1) + not needed//setLum(RestClient.getInstance().callRestfulWebService(MainApp.SENSOR_LIGHT_READINGS_REST));
             WSReadingsClient.getInstance().subscribe(URN_FOI, MainApp.CAPABILITY_LIGHT);
             WSReadingsClient.getInstance().subscribe(URN_FOI, MainApp.CAPABILITY_PIR);               //this.URN_FOI
 
         } else if ("ichatz".equals(mode)) {
 
             //DUPLICATE of (1) + not needed//setLum(RestClient.getInstance().callRestfulWebService(MainApp.SENSOR_LIGHT_READINGS_REST));
             setScreenLocked((Double.valueOf(RestClient.getInstance().callRestfulWebService(HTTP_PREFIX + uberdustUrl + SENSOR_SCREENLOCK_REST).split("\t")[1]) == 1) || (Double.valueOf(RestClient.getInstance().callRestfulWebService(HTTP_PREFIX + uberdustUrl + SENSOR_SCREENLOCK_REST).split("\t")[1]) == 3));
 
             WSReadingsClient.getInstance().subscribe(URN_FOI, MainApp.CAPABILITY_SCREENLOCK);
             WSReadingsClient.getInstance().subscribe(URN_FOI, MainApp.CAPABILITY_LIGHT);
             WSReadingsClient.getInstance().subscribe(URN_FOI, MainApp.CAPABILITY_PIR);                           //"urn:wisebed:ctitestbed:virtual:room:0.I.2"
 
         }
         //Adding Observer for the last readings
         WSReadingsClient.getInstance().addObserver(new ReadingsObserver());
         LOGGER.info("FOIController Initialized");
 
     }
 
     public void setLum(final String readings) {
         for (int k = 0, j = 1; k <= WINDOW - 1; k++, j = j + 3) {
 
             if (luminosityReadings.size() == 10) {
                 luminosityReadings.remove();
             }
             luminosityReadings.add(Double.valueOf(readings.split("\t")[j]));
 
             LOGGER.info("luminosityReadings[" + k + "]: " + Double.valueOf(readings.split("\t")[j]));
 
         }
 
     }
 
     public void initLum() {
         for (int k = 0; k <= WINDOW - 1; k++) {
             luminosityReadings.add(0.0);
             LOGGER.info(luminosityReadings.toString());
         }
     }
 
 
     public void setLastLumReading(final double thatReading) {
 
         updateLum1Threshold();
         updateLum2Threshold();
 
 
         double sum = 0;
 
         luminosityReadings.remove();
         luminosityReadings.add(thatReading);
 
         LOGGER.info("thatReading : " + thatReading);
 
         LOGGER.info(luminosityReadings.toString());
         for (Object d : luminosityReadings.toArray()) {
 
             sum += (Double) d;
         }
 
         LOGGER.info("Median : " + (sum / WINDOW));
         Median = sum / WINDOW;
 
         lastLumReading = thatReading;
 
         if (!"room".equals(mode)) {
 
             if (!isScreenLocked) {
 
                 if (Median < lumThreshold1 && lastLumReading > lumThreshold2) {
 
                     controlLight(true, Integer.parseInt(MainApp.ZONES[0]));
 
                     if (GetJson.getInstance().callGetJsonWebService(USER_PREFERENCES, "mode").equals("workstation")) {
 
                         String[] temp = new String[MainApp.ZONES.length - 1];
                         System.arraycopy(MainApp.ZONES, 1, temp, 0, MainApp.ZONES.length - 1);
 
                         for (String z : temp) {
                             controlLight(false, Integer.parseInt(z));
                         }
 
                     } else if ("ichatz".equals(mode)) {
 
                         controlLight(false, Integer.parseInt(MainApp.ZONES[1]));
                     }
 
                 } else if (Median < lumThreshold2) {
 
                     if ("ichatz".equals(mode)) {
 
                         controlLight(true, Integer.parseInt(MainApp.ZONES[0]));
                         controlLight(true, Integer.parseInt(MainApp.ZONES[1]));
 
                     } else {
 
                         for (String z : MainApp.ZONES)
                             controlLight(true, Integer.parseInt(z));
                     }
 
                 } else if (Median > lumThreshold1) {
 
                     for (String z : MainApp.ZONES)
                         controlLight(false, Integer.parseInt(z));
                 }
             }
         }
 
     }
 
     public void setScreenLocked(final boolean screenLocked) {
         isScreenLocked = screenLocked;
         updateLight3();
     }
 
     public synchronized void updateLight3() {
 
         lumThreshold1 = Double.parseDouble(GetJson.getInstance().callGetJsonWebService(USER_PREFERENCES, "illumination"));  //350
         lumThreshold2 = Double.parseDouble(GetJson.getInstance().callGetJsonWebService(USER_PREFERENCES, "illumination2"));  //350
 
         if (!isScreenLocked) {
             if (Median < lumThreshold1 && lastLumReading > lumThreshold2) {
 
                 controlLight(true, Integer.parseInt(MainApp.ZONES[0]));
 
                 if (GetJson.getInstance().callGetJsonWebService(USER_PREFERENCES, "mode").equals("workstation")) {
 
                     String[] temp = new String[MainApp.ZONES.length - 1];
                     System.arraycopy(MainApp.ZONES, 1, temp, 0, MainApp.ZONES.length - 1);
 
                     for (String z : temp) {
                         controlLight(false, Integer.parseInt(z));
                     }
 
                 } else if (GetJson.getInstance().callGetJsonWebService(USER_PREFERENCES, "mode").equals("ichatz")) {
 
                     controlLight(false, Integer.parseInt(MainApp.ZONES[1]));
                 }
             } else if (Median < lumThreshold2) {
 
                 if (GetJson.getInstance().callGetJsonWebService(USER_PREFERENCES, "mode").equals("ichatz")) {
 
                     controlLight(true, Integer.parseInt(MainApp.ZONES[0]));
                     controlLight(true, Integer.parseInt(MainApp.ZONES[1]));
 
                 } else {
 
                     for (String z : MainApp.ZONES)
                         controlLight(true, Integer.parseInt(z));
                 }
 
             } else if (Median > lumThreshold1) {
 
                 for (String z : MainApp.ZONES)
                     controlLight(false, Integer.parseInt(z));
             }
 
         } else if (isScreenLocked) {
 
             lockscreenDelay = Long.parseLong(GetJson.getInstance().callGetJsonWebService(FoiController.USER_PREFERENCES, "lockscreen_delay")) * 1000;
 
             timer.schedule(new TurnOffTask_2(timer), lockscreenDelay);
 
             if (GetJson.getInstance().callGetJsonWebService(USER_PREFERENCES, "mode").equals("workstation")) {
 
                 String[] temp = new String[MainApp.ZONES.length - 1];
                 System.arraycopy(MainApp.ZONES, 1, temp, 0, MainApp.ZONES.length - 1);
 
                 for (String z : temp) {
                     controlLight(false, Integer.parseInt(z));
                 }
 
             } else if (GetJson.getInstance().callGetJsonWebService(USER_PREFERENCES, "mode").equals("ichatz")) {
 
                 //timer.schedule(new TurnOffTask_2(timer), lockscreenDelay);
                 controlLight(false, Integer.parseInt(MainApp.ZONES[1]));
             }
         }
 
     }
 
     public long getLastPirReading() {
         return lastPirReading;
     }
 
     public void setLastPirReading(final long thatReading) {
 
         lastPirReading = thatReading;
 
         if (GetJson.getInstance().callGetJsonWebService(USER_PREFERENCES, "mode").equals("ichatz")) {
             updateLightsState();
         } else {
             pirHandler();
         }
 
 
     }
 
 
     public synchronized void updateLightsState() {
 
         if (GetJson.getInstance().callGetJsonWebService(USER_PREFERENCES, "mode").equals("ichatz")) {
 
             LOGGER.info("ichatz MODE");
 
             if (isScreenLocked) {
 
                 if (Median < lumThreshold1) {
                     //turn on lights
                     turnOnLights();
 
                 }
 
             } else if (!isScreenLocked) {
 
                 if (Median < lumThreshold1) {
 
                     turnOnLight_1();
 
                 }
 
             }
 
         } else if (Median < lumThreshold1) {
 
             LOGGER.info("TURN LIGHTS OOOOOOOOOOOOOON");
             //turn on lights
             pirHandler();
 
         }
 
     }
 
     private synchronized void turnOnLights() {
 
 
         if (!zone1) {
             controlLight(true, Integer.parseInt(MainApp.ZONES[2]));
             zone1TurnedOnTimestamp = lastPirReading;
             timer.schedule(new LightTask(timer), LightTask.DELAY);
         } else if (!zone2) {
             controlLight(true, Integer.parseInt(MainApp.ZONES[2]));
             if (lastPirReading - zone1TurnedOnTimestamp > 15000) {
                 controlLight(true, Integer.parseInt(MainApp.ZONES[1]));
                 zone2TurnedOnTimestamp = lastPirReading;
             }
         } else {
 
             controlLight(true, Integer.parseInt(MainApp.ZONES[1]));
             controlLight(true, Integer.parseInt(MainApp.ZONES[2]));
         }
 
     }
 
 
     public synchronized void pirHandler() {
 
         if (!zone1) {
 
             if (Double.parseDouble(GetJson.getInstance().callGetJsonWebService(USER_PREFERENCES, "pir_delay")) > 0)
                 turnOnLight_1();
             else {
                 controlLight(true, Integer.parseInt(MainApp.ZONES[0]));        //3
 
                 if (MainApp.ZONES.length > 2) {
                     controlLight(true, Integer.parseInt(MainApp.ZONES[1]));
                 }
 
                 zone1TurnedOnTimestamp = lastPirReading;
                 timer.schedule(new LightTask(timer), LightTask.DELAY);
             }
 
         } else if (!zone2 && (MainApp.ZONES.length > 1)) {
 
             controlLight(true, Integer.parseInt(MainApp.ZONES[0]));          //3
 
             if (MainApp.ZONES.length > 2) {
                 controlLight(true, Integer.parseInt(MainApp.ZONES[1]));
             }
 
             if (lastPirReading - zone1TurnedOnTimestamp > 15000) {
 
                 if (MainApp.ZONES.length > 2) {
                     controlLight(true, Integer.parseInt(MainApp.ZONES[2]));
                 } else {
                     controlLight(true, Integer.parseInt(MainApp.ZONES[1]));
                 }
 
                 zone2TurnedOnTimestamp = lastPirReading;
             }
         } else if (MainApp.ZONES.length > 1) {
 
             controlLight(true, Integer.parseInt(MainApp.ZONES[0]));
             controlLight(true, Integer.parseInt(MainApp.ZONES[1]));
 
             if (MainApp.ZONES.length > 2) {
                 controlLight(true, Integer.parseInt(MainApp.ZONES[2]));
             }
 
         }
 
     }
 
 
     public synchronized void turnOnLight_1() {
 
         LOGGER.info("turnOnLight_1()");
 
         if (!flag) {
             firstCall = lastPirReading;
             flag = true;
             timer.schedule(new TurnOffTask_3(timer), TurnOffTask_3.DELAY);
         } else if (!zone1) {
             LOGGER.info("lastPirReading - firstCall = " + (lastPirReading - firstCall));
             if (lastPirReading - firstCall > 15000) {
 
                 if (GetJson.getInstance().callGetJsonWebService(USER_PREFERENCES, "mode").equals("ichatz")) {
                     controlLight(true, Integer.parseInt(MainApp.ZONES[2]));
                 } else {
                     controlLight(true, Integer.parseInt(MainApp.ZONES[0]));
                 }
                 pirDelay = Long.parseLong(GetJson.getInstance().callGetJsonWebService(FoiController.USER_PREFERENCES, "pir_delay")) * 1000;
                 timer.schedule(new TurnOffTask_4(timer), pirDelay);
             }
         }
 
     }
 
     public double getLastLumReading() {
         return lastLumReading;
     }
 
     public double getMedian() {
         return Median;
     }
 
 
     public boolean isScreenLocked() {
         return isScreenLocked;
     }
 
     public boolean isZone1() {
         return zone1;
     }
 
     public boolean isZone2() {
         return zone2;
     }
 
     public boolean isZone3() {
         return zone3;
     }
 
     public boolean isFlag() {
         return flag;
     }
 
     public void setFlag(final boolean newFlag) {
         flag = newFlag;
 
     }
 
     public synchronized void controlLight(final boolean value, final int zone) {
 
         if ("ichatz".equals(mode)) {
 
             if (zone == Integer.parseInt(MainApp.ZONES[2])) {
                 zone1 = value;
             } else if (zone == Integer.parseInt(MainApp.ZONES[1])) {
                 zone2 = value;
             } else {
                 zone3 = value;
             }
 
         } else {
 
             if (zone == Integer.parseInt(MainApp.ZONES[0]) || (MainApp.ZONES.length > 2 && zone == Integer.parseInt(MainApp.ZONES[1]))) {
 
                 zone1 = value;
 
             } else if ((MainApp.ZONES.length == 2 && zone == Integer.parseInt(MainApp.ZONES[1])) || (MainApp.ZONES.length > 2 && zone == Integer.parseInt(MainApp.ZONES[2]))) {
 
                 zone2 = value;
             }
         }
 
 
         BYPASS = Boolean.parseBoolean(GetJson.getInstance().callGetJsonWebService(USER_PREFERENCES, "bypass"));
 
         if (!BYPASS) {
             UberdustClient.getInstance().sendCoapPost(FOI_ACTUATOR, "lz" + zone, value ? "1" : "0"); //FOI_ACTUATOR
         }
     }
 
     public static void main(final String[] args) {
         FoiController.getInstance();
     }
 
 }
