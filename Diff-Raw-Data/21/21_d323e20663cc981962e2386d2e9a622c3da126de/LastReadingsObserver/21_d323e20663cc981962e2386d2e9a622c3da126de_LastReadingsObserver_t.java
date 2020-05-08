 package eu.uberdust.lights;
 
 import eu.uberdust.MainApp;
 import eu.uberdust.communication.protobuf.Message;
 import eu.uberdust.communication.websocket.readings.WSReadingsClient;
 import org.apache.log4j.Logger;
 
 import java.util.Observable;
 import java.util.Observer;
 
 /**
  * Last Readings Observer.
  */
 public class LastReadingsObserver implements Observer {
 
     /**
      * Static Logger.
      */
     private static final Logger LOGGER = Logger.getLogger(LightController.class);
 
 
     /**
      * This method is called whenever the observed object is changed. An
      * application calls an <tt>Observable</tt> object's
      * <code>notifyObservers</code> method to have all the object's
      * observers notified of the change.
      *
      * @param o   the observable object.
      * @param arg an argument passed to the <code>notifyObservers</code>
      *            method.
      */
     @Override
     public void update(final Observable o, final Object arg) {
 
         if (!(o instanceof WSReadingsClient)) {
             return;
         }
 
         if (!(arg instanceof Message.NodeReadings)) {
             return;
         }
 
         final Message.NodeReadings readings = (Message.NodeReadings) arg;
 
         for (final Message.NodeReadings.Reading reading : readings.getReadingList()) {
 
             if (reading.getCapability().equals(MainApp.CAPABILITY_SCREENLOCK)) {
                 LOGGER.info("New Reading for Screen Lock Capability");
 
                 final String node = "" + reading.getNode().split(":")[2];
                final boolean isScreenLocked = ((reading.getDoubleReading() == 1)||(reading.getDoubleReading() == 3));
                 LOGGER.info(new StringBuilder().append("isScreenLocked: ")
                         .append(reading.getDoubleReading()).append(" -- ")
                         .append(isScreenLocked).append(" -- ").append(node).append(" -- ").append(node.equals("amethyst")).toString());
 
                 if (node.equals("amethyst")) {
                     LightController.getInstance().setAmethystLocked(isScreenLocked);
                 } else if (node.equals("silver")) {
                     LightController.getInstance().setSilverLocked(isScreenLocked);
                 } else if (node.equals("blanco")) {
                     LightController.getInstance().setBlancoLocked(isScreenLocked);
                 } else if (node.equals("yellow")) {
                     LightController.getInstance().setYellowLocked(isScreenLocked);
                 }
 
             } else if (reading.getCapability().equals(MainApp.CAPABILITY_LIGHT)) {
                 LOGGER.info("New Reading for Light Capability");
 
                 final Double value = reading.getDoubleReading();
                 LOGGER.info("Lum: " + value);
 
                 LightController.getInstance().setLastLumReading(value);
 
             } else if (reading.getCapability().equals(MainApp.CAPABILITY_PIR)) {
 
                 final Double value = reading.getDoubleReading();
 
                 if (value == 1) {
                     LOGGER.info("New Reading for Pir Capability");
                     LOGGER.info("Pir value: " + value + "  Luminosity: " + LightController.getInstance().getLastLumReading());
 
                     final long timeStmp = reading.getTimestamp();
                     LightController.getInstance().setLastPirReading(timeStmp);  //timeStmp
 
                 }
 
             }   else if (reading.getCapability().equals(MainApp.CAPABILITY_STATUS)) {
 
                 final String node = "" + reading.getNode().split(":")[2];
                 final Double value = reading.getDoubleReading();
                 final long timeStmp = reading.getTimestamp();
                 
                 LOGGER.info("New Reading for Status Capability of "+node+": "+value);
 
                // LightController.getInstance().setLastStatus(node, timeStmp);
             }
         }
     }
 }
