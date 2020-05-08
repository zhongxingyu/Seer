 /*
  * Copyright (C) 2013 Joshua Michael Hertlein <jmhertlein@gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.jmhertlein.adminbuddy.daemon;
 
 import net.jmhertlein.core.io.ClientSession;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.nio.ByteBuffer;
 import java.security.InvalidAlgorithmParameterException;
 import java.security.InvalidKeyException;
 import java.security.KeyPair;
 import java.security.NoSuchAlgorithmException;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.crypto.BadPaddingException;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.SecretKey;
 import net.jmhertlein.core.io.PacketReceiveListener;
 import net.jmhertlein.adminbuddy.protocol.ClientPacket;
 import net.jmhertlein.adminbuddy.protocol.StatusCode;
 import net.jmhertlein.core.crypto.EncryptedSecretKey;
 import net.jmhertlein.core.crypto.Keys;
 
 /**
  *
  * @author Joshua Michael Hertlein <jmhertlein@gmail.com>
  */
 public class AdminBuddyDaemon extends Thread {
     private volatile boolean shutdown;
     private ServerSocket socket;
     private int port;
     private DaemonCore core;
     private final PublicKey serverPubkey;
     private final PrivateKey serverPrivateKey;
     private int nextClientId;
     private Map<Integer, SecretKey> sessions;
 
     public AdminBuddyDaemon(int port) {
         sessions = new ConcurrentHashMap<>();
         shutdown = false;
         this.port = port;
         this.core = new DaemonCore();
         nextClientId = 0;
         
         KeyPair pair = Keys.newRSAKeyPair(1024);
         serverPrivateKey = pair.getPrivate();
         serverPubkey = pair.getPublic();
     }
     
     @Override
     public void run() {
         try {
             socket = new ServerSocket(port);
         } catch (IOException ex) {
             Logger.getLogger(AdminBuddyDaemon.class.getName()).log(Level.SEVERE, null, ex);
             System.err.println("Error creating socket.");
             return;
         }
 
         while (!shutdown) {
             Socket client;
             try {
                 System.out.println("Waiting for client.");
                 client = socket.accept();
                 System.out.println("Got client!");
             } catch (IOException ex) {
                 System.err.println("Socket closing.");
                 continue;
             }
             try {
                 System.out.println("Setting up client...");
                 setupClient(client);
            } catch (IOException | ClassNotFoundException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException ex) {
                 Logger.getLogger(AdminBuddyDaemon.class.getName()).log(Level.SEVERE, null, ex);
             }
 
             System.out.println("Client done.");
         }
 
         synchronized (this) { //why do I need to notify...? oh because this is when we're shutting down and closing
             this.notifyAll();
         }
     }
 
     public void terminate(long millisToWait) throws InterruptedException, IOException {
         this.shutdown = true;
         this.socket.close();
         synchronized (this) {
             this.wait(millisToWait); //this is waiting for the above notifyAll() so it knows the server has finished execution
         }
     }
 
     public synchronized void pushChatUpdate(String sender, String message) {
         try {
             core.pushChatUpdate(sender, message);
         } catch (IOException ex) {
             Logger.getLogger(AdminBuddyDaemon.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public DaemonCore getCore() {
         return core;
     }
 
     public static void main(String[] args) {
         AdminBuddyDaemon d = new AdminBuddyDaemon(5000);
 
         d.start();
 
         System.out.println("Send a line feed to exit.");
         Scanner scan = new Scanner(System.in);
         String message;
 
         do {
             message = scan.nextLine();
             d.pushChatUpdate("Everdras", message);
         } while (!message.isEmpty());
 
         scan.close();
         System.out.println("Beginning shutdown...");
         try {
             d.terminate(1000);
         } catch (InterruptedException | IOException ex) {
             Logger.getLogger(AdminBuddyDaemon.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         System.out.println("Done");
     }
 
    private void setupClient(final Socket s) throws IOException, ClassNotFoundException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
         InputStream is = s.getInputStream();
         OutputStream os = s.getOutputStream();
 
         ObjectOutputStream oos;
         ObjectInputStream ois;
         
         int sessionId;
         SecretKey sessionKey;
 
         System.out.println("Reading status code...");
         switch (is.read()) {
             case StatusCode.KEY_EXCHANGE_REQUESTED:
                 System.out.println("KEY EXCHANGE:");
                 
                 System.out.println("Setting up plain OOS's");
                 oos = new ObjectOutputStream(os);
                 ois = new ObjectInputStream(is);
                 System.out.println("Finished setting up plain OOS's");
                 
                 //exchange public keys
                 System.out.println("Writing our key");
                 oos.writeObject(serverPubkey);
                 System.out.println("Receiving their key");
                 PublicKey clientPubkey = (PublicKey) ois.readObject();
                 
                 //gen a session id, and a secret key (encrypting the key first!)
                 System.out.println("Preparing session ID and secret key");
                 sessionId = nextClientId;
                 nextClientId++;
                 sessionKey = Keys.newAESKey(128);
                 EncryptedSecretKey wrapped = new EncryptedSecretKey(sessionKey, clientPubkey, serverPrivateKey);
                 
                 //send client the session id and key
                 System.out.println("Writing sID and secret key");
                 oos.writeInt(sessionId);
                 oos.writeObject(wrapped);
                 
                 //cache the session key
                 sessions.put(sessionId, sessionKey);
                 System.out.println("Closing connection.");
                 s.close();
                 System.out.println("END KEY EXCHANGE");
                 break;
 
             case StatusCode.CONNECTION_REQUESTED:
                 System.out.println("CONNECTION REQUEST START");
                 System.out.println("Reading sID");
                 sessionId = readInt(is);
                 
                 sessionKey = sessions.get(sessionId);
                 
                 if(sessionKey == null) {
                     //TODO: Handle client not having already swapped key pairs
                 }
                 
                 System.out.println("Setting up encrypted streams...");
                 oos = Keys.getEncryptedObjectOutputStream(sessionKey, os);
                 ois = Keys.getEncryptedObjectInputStream(sessionKey, is);
                 System.out.println("Encrypted streams established");
                 
                 System.out.println("Setting up ClientSession object");
                 final ClientSession client = new ClientSession(s, sessionKey, "[ID: " + sessionId + "]");
                 client.initChannels(oos, ois);
                 client.addPacketListener(new PacketReceiveListener() {
 
                     @Override
                     public boolean onPacketReceive(Object data, int channel) {
                         ((ClientPacket) data).onServerReceive(core, client);
                         return false;
                     }
                 });
 
                 System.out.println("Adding client");
                 core.addNewClient(client);
                 System.out.println("END CONNECTION REQUEST");
                 break;
 
             default:
                 System.err.println("50 DKP MINUS FOR NOT KNOWING A VALID STATUS CODE!!!");
         }
     }
     
     private static int readInt(InputStream is) throws IOException {
         byte[] rawInt = new byte[4];
         is.read(rawInt);
         
         return ByteBuffer.wrap(rawInt).getInt();
     }
 }
