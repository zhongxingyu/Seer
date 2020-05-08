 package eu.uberdust.application.foi.manager;
 
 import eu.uberdust.communication.protobuf.Message;
 import eu.uberdust.communication.websocket.readings.WSReadingsClient;
 import org.apache.log4j.Logger;
 
 import java.util.*;
 
 
 public class LuminosityManager extends Observable implements Observer {
 
     /**
      * Static Logger.
      */
     private static final Logger LOGGER = Logger.getLogger(LuminosityManager.class);
 
     /**
      * Our Instance.
      */
     private static LuminosityManager instance = null;
 
     /**
      * DARKLY State Indicator.
      */
     public static final int DARKLY = 1;
 
     /**
      * TOTAL_DARKNESS State Indicator.
      */
     public static final int TOTAL_DARKNESS = 2;
 
     /**
      * BRIGHT State Indicator.
      */
     public static final int BRIGHT = 3;
 
 
     /**
      * Current State of the Luminosity level.
      */
     private int currentState;
 
     /**
      * Lower Luminosity Threshold        200
      */
     private double lumThreshold1;
 
     /**
      * Higher Luminosity Threshold       350
      */
     private double lumThreshold2;
 
     /**
      * Latest Light Reading
      */
     private double LatestLightReading;
 
     public static final int WINDOW = 10;
 
     public static Queue luminosityReadings = new PriorityQueue(WINDOW);
 
     private double Median;
 
 
     public int getCurrentState() {
 
         return  currentState;
 
     }
 
     public void initLum() {
         for (int k = 0; k <= WINDOW - 1; k++) {
             luminosityReadings.add(0.0);
             LOGGER.info(luminosityReadings.toString());
         }
     }
 
 
 
     /**
      * Default Constructor.
      */
     public LuminosityManager() {
         LOGGER.info("-----LuminosityManager initializing------");
         reset();
     }
 
     /**
      * Singleton Get Instance.
      *
      * @return the single instance.
      */
     public static LuminosityManager getInstance() {
         synchronized (LuminosityManager.class) {
             if (instance == null) {
                 instance = new LuminosityManager();
             }
             return instance;
         }
     }
 
     /**
      * Adds a new reading from Uberdust.
      *
      * @param reading the new reading.
      */
     public void addReading(Message.NodeReadings.Reading reading) {
 
         double sum = 0;
 
 
         //interested only in light events
 
         if ("urn:wisebed:node:capability:light".equals(reading.getCapability())) {       //urn:wisebed:ctitestbed:node:capability:light
 
             LOGGER.info("New Reading for Light Capability");
 
             LatestLightReading = reading.getDoubleReading();
 
             luminosityReadings.remove();
             luminosityReadings.add(reading.getDoubleReading());
 
             LOGGER.info(luminosityReadings.toString());
 
             for (Object d : luminosityReadings.toArray()) {
 
                 sum += (Double) d;
             }
 
             LOGGER.info("Sum : " + sum );
 
             Median =  sum / WINDOW;
 
             LOGGER.info("Median : " + Median);
 
             updateLum1Threshold();
             updateLum2Threshold();
 
             //calculate te current Status FSM
             updateStatus();
         }
     }
 
     /**
      * Checks for the Status of Luminosity in a FOI.
      *
      * @return void
      */
     private void updateStatus() {
                                                      //           illumination         illumination2
         //FSM                                                     lumThreshold1    <   lumThreshold2
         //BRIGHT-->DARKLY-->TOTAL_DARKNESS            //    0-----------|----------------|
 
        if ( Median > lumThreshold1 && LatestLightReading < lumThreshold2 ) {
 
             setCurrentState(DARKLY);
 
         } else if(Median < lumThreshold1) {
 
             setCurrentState(TOTAL_DARKNESS);
 
         } else if(Median > lumThreshold2){
 
             setCurrentState(BRIGHT);
 
         }
 
 
     }
 
 
     /**
      * Sets the current state of the operation.
      *
      * @return void
      */
 
     public void setCurrentState(int newState) {
 
         LOGGER.info("Setting next state: "+stateToString(currentState)+" -------->> "+stateToString(newState));
 
         currentState = newState;
 
         this.setChanged();
         this.notifyObservers();
 
     }
 
 
     /**
      * Converts int state value to string.
      *
      * @param state the new state.
      */
     public String stateToString(int state){
 
         String s = "";
 
         switch (state){
 
             case 1:
                 s = "DARKLY";
                 break;
 
             case 2:
                 s = "TOTAL_DARKNESS";
                 break;
 
             case 3:
                 s = "BRIGHT";
                 break;
         }
 
         return s;
 
     }
 
 
 
     private void updateLum1Threshold() {
         try {
             lumThreshold1 = Double.parseDouble(ProfileManager.getInstance().getElement("illumination"));  //350
         } catch (NullPointerException npe) {
             lumThreshold1 = 350;
         } catch (NumberFormatException nfe) {
             lumThreshold1 = 350;
         }
         LOGGER.info("lumThreshold1 : "+lumThreshold1);
     }
 
     private void updateLum2Threshold() {
         try {
             lumThreshold2 = Double.parseDouble(ProfileManager.getInstance().getElement("illumination2"));  //350
         } catch (NullPointerException npe) {
             lumThreshold2 = 350;
         } catch (NumberFormatException nfe) {
             lumThreshold2 = 350;
         }
         LOGGER.info("lumThreshold2 : "+lumThreshold2);
     }
 
 
     @Override
     public void update(Observable o, Object arg) {
 
         if (!(o instanceof WSReadingsClient)) {
             return;
         }
 
         if (!(arg instanceof Message.NodeReadings)) {
             return;
         }
 
         final Message.NodeReadings readings = (Message.NodeReadings) arg;
         for (final Message.NodeReadings.Reading reading : readings.getReadingList()) {
             addReading(reading);
         }
     }
 
     /**
      * Reset the internal state.
      */
     public void reset() {
         initLum();
         updateLum1Threshold();
         updateLum2Threshold();
         setCurrentState(TOTAL_DARKNESS);
 
     }
 }
