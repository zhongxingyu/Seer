 package eu.uberdust.devicedriver;
 
 import eu.uberdust.DeviceCommand;
 import eu.uberdust.communication.protobuf.Message;
 import eu.uberdust.network.NetworkManager;
 
 import java.util.Observable;
 
 /**
  * Created by IntelliJ IDEA.
  * User: amaxilatis
  * Date: 3/25/12
  * Time: 5:37 PM
  */
 public class CeilingLightDriver extends DeviceDriver {
     private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(CeilingLightDriver.class);
 
     static final String OBSERVED = "light";
    static final String COMMAND_PREFIX = "7f,69,6e,1,";
 
     @Override
     public void update(final Observable observable, final Object o) {
 
         if (o instanceof Message.Control) {
 
             final Message.Control command = (Message.Control) o;
 
             if (!command.hasPayload()) {
 
 
                 if (command.getCapability().contains(OBSERVED)) {
                     LOGGER.info("sending to " + command.getDestination());
                     LOGGER.info("capability " + command.getCapability());
                     LOGGER.info("last value " + command.getLastValue());
                     final int start = command.getCapability().indexOf(OBSERVED) + OBSERVED.length();
                     final String zone = command.getCapability().substring(start);
 
                     final int state = (int) Double.parseDouble(command.getLastValue());
                     final StringBuilder bytes = new StringBuilder(COMMAND_PREFIX);
                     bytes.append(zone).append(",").append(state);
                     final DeviceCommand devCommand = new DeviceCommand(command.getDestination(), bytes.toString());
                     NetworkManager.getInstance().update(devCommand);
                 }
 
 
             }
         }
     }
 
 }
