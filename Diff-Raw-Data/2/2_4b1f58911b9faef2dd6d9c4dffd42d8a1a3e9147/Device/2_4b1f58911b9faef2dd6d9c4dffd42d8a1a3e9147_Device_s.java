 package es.upm.dit.gsi.shanks.model.element.device;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import es.upm.dit.gsi.shanks.model.element.NetworkElement;
 import es.upm.dit.gsi.shanks.model.element.exception.TooManyConnectionException;
 import es.upm.dit.gsi.shanks.model.element.exception.UnsupportedNetworkElementStatusException;
 import es.upm.dit.gsi.shanks.model.element.link.Link;
 
 /**
  * Device class
  * 
  * This is used to represent a common Device
  * 
  * @author Daniel Lara
  * @author a.carrera
  * @version 0.2
  * 
  */
 
 public abstract class Device extends NetworkElement {
 
     Logger logger = Logger.getLogger(Device.class.getName());
 
     private List<Link> linksList;
     private boolean isGateway;
 
     /**
      * @param id
      * @param isGateway
      * @throws UnsupportedNetworkElementStatusException
      */
     public Device(String id, String initialState, boolean isGateway)
             throws UnsupportedNetworkElementStatusException {
         super(id, initialState);
         this.isGateway = isGateway;
         this.linksList = new ArrayList<Link>();
     }
 
     /**
      * Return the connections between the device
      * 
      * @return linkList A list with the different connections
      */
     public List<Link> getLinks() {
         return linksList;
     }
 
     /**
      * Connect the device to a link
      * 
      * @param link
      * @throws TooManyConnectionException
      */
     public void connectToLink(Link link) throws TooManyConnectionException {
         if (!this.linksList.contains(link)) {
             this.linksList.add(link);
             logger.finer("Device " + this.getID() + " is now connected to Link "
                     + link.getID());
             link.connectDevice(this);
         }
     }
 
     /**
      * Disconnect the device From a link
      * 
      * @param link
      */
     public void disconnectFromLink(Link link) {
         boolean disconnected = this.linksList.remove(link);
         if (disconnected) {
             link.disconnectDevice(this);
             logger.fine("Device " + this.getID()
                     + " is now disconnected from Link " + link.getID());
         } else {
             logger.warning("Device " + this.getID()
                     + " could not be disconnected from Link " + link.getID()
                     + ", because it was not connected.");
         }
     }
 
     /**
      * Connect the device to other device with a link
      * 
      * @param device
      * @param link
      * @throws TooManyConnectionException
      */
     public void connectToDeviceWithLink(Device device, Link link)
             throws TooManyConnectionException {
         this.connectToLink(link);
         try {
            this.connectToLink(link);
         } catch (TooManyConnectionException e) {
             this.disconnectFromLink(link);
             throw e;
         }
     }
 
     /**
      * @return the isGateway
      */
     public boolean isGateway() {
         return isGateway;
     }
 }
