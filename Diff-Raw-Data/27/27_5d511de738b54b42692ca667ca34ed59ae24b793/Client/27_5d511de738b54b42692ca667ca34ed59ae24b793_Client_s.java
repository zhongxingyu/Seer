 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package GraphDB;
 
 import java.awt.Point;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 
 /**
  *
  * @author michal
  */
 public class Client {
 
     ClientSocket sock;
     String host;
     int port;
     boolean handshaked = false;
     boolean auth = false;
     boolean connected = false;
 
     public Client(String host, int port) {
         this.host = host;
         this.port = port;
         sock = new ClientSocket(port, host);
     }
 
     public void prepareServerSession() {
         try {
             handshake();
             System.out.println("Handshaking successful.");
             authenticate();
             System.out.println("Authentication successful.");
             System.out.println("Login to server successful.");
             connected = true;
         } catch (IOException ex) {
             System.out.println("IO Exception while attempting to make a connection.");
             ex.printStackTrace();
         } catch (ActionFailedException ex) {
             System.out.println("An action failed while attempting to make a connection.");
             System.out.println(ex.getMessage());
         }
     }
 
     public ArrayList<Point> getRegistrationPoints(String modID) {
         try {
             sendModuleData(modID);
             ArrayList<Point> t = (ArrayList<Point>) sock.getObjectMessage();
             return t;
         } catch (IOException ex) {
             System.out.println("Error while getting registration points.");
             ex.printStackTrace();
         } catch (ClassNotFoundException ex) {
             System.out.println("Could not find class while getting registration points.");
             ex.printStackTrace();
         } catch (ClassCastException ex) {
             System.out.println("Could not cast to the required class.");
             ex.printStackTrace();
         }
         return null;
     }
 
     public ArrayList<Point> getRegistrationPoints() throws IOException, ClassNotFoundException, ClassCastException {
         sendModuleData(readModuleData());
         // FIXME ***BELOW MAY CAUSE ERRORS (WHY THE HELL)***
         ArrayList<Point> regPoints = (ArrayList<Point>) sock.getObjectMessage();
         for (Point point : regPoints) {
             System.out.println(point);
         }
         return regPoints;
     }
 
     public void disconnect() {
         try {
             sock.sendString("disconnect");
             String resp = sock.getStringMessage();
             if (resp.equals("disconnecting")) {
                 System.out.println("Server disconnected according to protocol.");
                 sock.disconnect();
             } else {
                 System.out.println("Server did not follow disconnect protocol, but disconnecting anyway.");
                 sock.disconnect();
             }
         } catch (IOException ex) {
             System.out.println("Exception while attempting to disconnect");
             ex.printStackTrace();
         }
         auth = false;
         handshaked = false;
         connected = false;
     }
 
     public void reconnect() {
         connected = false;
         handshaked = false;
         auth = false;
         sock.reset();
     }
 
     public boolean handshakeServ() {
         try {
             String greeting = "graphclient";
             sock.sendString(greeting);
             String resp = sock.getStringMessage();
             if (!resp.equals("graphserver")) {
                 return false;
             }
         } catch (IOException ex) {
             System.out.println("Could not handshake with server");
             ex.printStackTrace();
             return false;
         }
         handshaked = true;
         return true;
     }
 
     private void handshake() throws IOException, ActionFailedException {
         String greeting = "graphclient";
         sock.sendString(greeting);
         String resp = sock.getStringMessage();
         if (!resp.equals("graphserver")) {
             throw new ActionFailedException("Handshaking with the server failed.");
         }
         handshaked = true;
     }
 
     public boolean authenticate(String user, String password) {
         try {
             return sendAuthDetails(user, password, false);
         } catch (IOException ex) {
             System.out.println("IO error while authenticating. May have lost connection to server. Attempting to reconnect.");
             reconnect();
             return false;
         } catch (ActionFailedException ex) {
             System.out.println(ex.getMessage());
             return false;
         }
     }
 
     private boolean authenticate() throws IOException, ActionFailedException {
         BufferedReader cmdIn = new BufferedReader(new InputStreamReader(System.in));
         System.out.println("Please enter your username.");
         String user = cmdIn.readLine();
         System.out.println("Please enter your password.");
         String pass = cmdIn.readLine();
         return sendAuthDetails(user, pass, true);
     }
 
     /**
      * Sends authentication details to the server. If the cmd value is true, it
      * will try and get a response from the user in the commandline if authentication
      * fails.
      * @param user
      * @param password
      * @param cmd
      * @return
      * @throws IOException
      * @throws ActionFailedException
      */
     private boolean sendAuthDetails(String user, String password, boolean cmd) throws IOException, ActionFailedException {
         String authStr = user + ":" + getPasswordHex(password);
         sock.sendString(authStr);
         String authRes = sock.getStringMessage();
         if (!authRes.equals("authsuccess")) {
             if (cmd) {
                 BufferedReader cmdIn = new BufferedReader(new InputStreamReader(System.in));
                 System.out.println("Authentication with server failed. Try again? (y/n)");
                 String retry = cmdIn.readLine();
                 if (retry.equals("y") || retry.equals("yes")) {
                     sock.sendString("reauth");
                     authenticate();
                 } else {
                     sock.sendString("noreauth");
                     throw new ActionFailedException("User terminated authentication with server.");
                 }
             } else {
                 sock.sendString("reauth");
                 return false;
             }
         }
         auth = true;
         return true;
     }
 
     public String readModuleData() throws IOException {
         BufferedReader cmdIn = new BufferedReader(new InputStreamReader(System.in));
         System.out.println("Enter module id.");
         return cmdIn.readLine();
     }
 
     /**
      * Sends module data to the server.
      * @param modID
      * @throws IOException
      */
     public void sendModuleData(String modID) throws IOException {
         System.out.println("Sending request for data to server.");
         sock.sendString("regpointreq");
         if (sock.getStringMessage().equals("ready")) {
             System.out.println("Server accepted request. Sending module details to server.");
             sock.sendString(modID);
         } else {
             System.out.println("Server not ready to process request...waiting.");
             try {
                 Thread.sleep(2000);
             } catch (InterruptedException ex) {
                 System.out.println("Thread was interrupted.");
                 ex.printStackTrace();
             }
             sendModuleData(modID);
         }
     }
 
     public static void main(String[] arstring) {
         Client t = new Client("localhost", 2000);
         t.prepareServerSession();
         try {
             ArrayList<Point> p = t.getRegistrationPoints();
         } catch (IOException ex) {
             System.out.println("IO exception while getting registration points.");
             ex.printStackTrace();
         } catch (ClassNotFoundException ex) {
             System.out.println("Class not found while getting registration points");
             ex.printStackTrace();
         } catch (ClassCastException ex) {
             System.out.println("Classcast exception when attempting to cast received object.");
             ex.printStackTrace();
         }
     }
 
     // <editor-fold defaultstate="collapsed" desc="generates a password hex.">
     public String getPasswordHex(String password) {
         MessageDigest md;
         try {
             md = MessageDigest.getInstance("SHA-1");
             byte[] pass = password.getBytes();
             md.reset();
             md.update(pass);
             byte[] mdByte = md.digest();
             StringBuilder passHex = new StringBuilder();
             for (int i = 0; i
                     < mdByte.length; i++) {
                 passHex.append(Integer.toHexString(0xFF & mdByte[i]));
             }
             return passHex.toString();
         } catch (NoSuchAlgorithmException ex) {
             System.out.println("Encryption algorithm not found.");
             System.exit(1);
         } catch (Exception ex) {
             System.out.println("Exception while trying to process password.");
             System.exit(1);
         } // Should never be reached. An exception will be thrown and the system will exit
         // or the method will return the hex string.
         return null;
     }
 // </editor-fold>
 }
