 package edu.cmu.solarflare;
 
 import com.sun.spot.peripheral.TimeoutException;
 import com.sun.spot.sensorboard.EDemoBoard;
 
 import java.io.IOException;
 import java.util.Hashtable;
 import java.util.Vector;
 import javax.microedition.midlet.MIDlet;
 import javax.microedition.midlet.MIDletStateChangeException;
  
 public class SolarFlare extends MIDlet {
  
     public EDemoBoard board = EDemoBoard.getInstance();
     private Zigbee zigbee;
     private Wifi wifi;
     public Hashtable clients;       // all known clients in the network (userID => Client object)
     private Vector localClients;    // all wifi users connected to this SPOT (userIDs)
  
     protected void startApp() throws MIDletStateChangeException {
         System.out.println("SPOT on.");
         
         // state
         clients = new Hashtable();
         localClients = new Vector();
         
         // communication
         zigbee = new Zigbee(this);
         wifi = new Wifi(this, "ulala", 10000);  // TCP server on port 10000
         
         try {
             zigbee.init();
         } catch (IOException e) {
             System.out.println("Error, ZigBee I/O: Could not open radiogram send/receive connection. " + e);
         }
         
         try {
             wifi.init();
         } catch (TimeoutException e) {
             System.out.println("Error, WiFi timeout: " + e);
         } catch (IOException e) {
             System.out.println("Error, WiFi I/O: " + e);
         } catch (Exception e) {
             System.out.println("Error, WiFi general: " + e);
         }
         
         zigbee.startComm();
         
         if (wifi.isActive) {
             wifi.startComm();
         }
     }
  
     public void addLocalClient(String userID, String userName) {
         System.out.println("Adding a local client.");
         wifi.sendClientList(userID);     // send our list of known clients to the new user
         localClients.addElement(userID);
         addClient(userID, userName, zigbee.address);    // add user to local state
     }
     
     public void addClient(String userID, String userName, String spotAddress) {
         System.out.println("Adding client: " + userID + " (" + userName + ")");
         Client c = new Client(userID, userName, spotAddress);
         clients.put(userID, c); 
         
         // broadcast new client info if it's a local client
         if (spotAddress.equals(zigbee.address)) {
             zigbee.broadcastClientStatus(c, "online");
         }
         
         // tell all local clients about the new user
         wifi.broadcastClientStatus(c, "online");
     }
     
     public void removeLocalClient(String userID) {
         System.out.println("Removing a local client.");
         localClients.removeElement(userID);
         zigbee.broadcastClientStatus((Client) clients.get(userID), "offline");
         removeClient(userID);
     }
     
     public void removeClient(String userID) {
         Client departingClient = (Client) clients.get(userID);
         System.out.println("Removing client: " + departingClient.userID + " (" + departingClient.userName + ")");
         wifi.broadcastClientStatus(departingClient, "offline");
         clients.remove(userID);
     }
     
     public void relayUserMessage(String senderUserID, String receiverUserID, String msg) {
         if (localClients.contains(receiverUserID)) {
             wifi.relayUserMessage(senderUserID, receiverUserID, msg);
        } else if (clients.containsKey(receiverUserID)) {
             zigbee.sendUserMessage(senderUserID, receiverUserID, ((Client) clients.get(receiverUserID)).spotAddress, msg);
         } else {
             System.out.println("Error, general: SPOT isn't aware of user \"" + receiverUserID + "\". Can't relay message.");
         }
     }
     
     protected void pauseApp() {
     }
  
     protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
     }
 }
