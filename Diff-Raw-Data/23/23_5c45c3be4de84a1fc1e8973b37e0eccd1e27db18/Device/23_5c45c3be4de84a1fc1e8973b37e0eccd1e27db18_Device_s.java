 package es.upm.dit.gsi.shanks.model.element.device;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import es.upm.dit.gsi.shanks.model.element.NetworkElement;
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
      */
     public Device(String id, String initialState, boolean isGateway) {
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
      */
     public void connectToLink(Link link) {
        this.linksList.add(link);
        logger.info("Device " + this.getID() + " is now connected to Link " + link.getID());
     }
     
     /**
      * Disconnect the device From a link
      * 
      * @param link
      */
     public void disconnectFromLink(Link link) {
         boolean disconnected = this.linksList.remove(link);
         if (disconnected) {
             logger.info("Device " + this.getID() + " is now disconnected from Link " + link.getID());
         } else {
             logger.info("Device " + this.getID() + " could not be disconnected from Link " + link.getID() + ", because it was not connected.");
         }
     }
 
     /**
      * @return the isGateway
      */
     public boolean isGateway() {
         return isGateway;
     }
 }
