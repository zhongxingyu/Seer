 /*
  *  Copyright (C) 2010 Furyhunter
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package mcrpserver;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Properties;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 /**
  * The main class for MCRPServer.
  * @author Furyhunter
  */
 public class MCRPServer {
 
     public static final int VERSION_MAJOR = 0;
     public static final int VERSION_MINOR = 1;
     private static ServerSocket servSock;
     public static Properties config;
     private static boolean running = true;
     private static com.mojang.minecraft.level.Level level = null;
     private static ArrayList<ClientSession> clients;
     private static int clientIncrement;
 
     public static synchronized void log(LogLevel level, String text) {
         Calendar time = Calendar.getInstance();
         SimpleDateFormat sdf = new SimpleDateFormat("[HH:mm:ss ");
 
         // Log if properties record level is higher or equal
         int value = 1;
         try {
             value = Integer.valueOf(config.getProperty("log.level", "1"));
         } catch (NullPointerException ex) {
         }
         if (value >= level.level) {
             System.out.println(sdf.format(time.getTime())
                     + level.str + "] " + text);
         }
     }
 
     /**
      * Loads the config file.
      */
     public static synchronized void loadConfig() {
         try {
             config = new Properties();
             FileReader conffile = new FileReader("mcrpserver.conf");
             config.load(conffile);
         } catch (FileNotFoundException ex) {
             // Generate a default config
             log(LogLevel.MINIMAL, "jbnet2d.conf not found, generating...");
             config.setProperty("network.port", "25565");
             config.setProperty("server.name", "Default MCRP Server");
             config.setProperty("server.motd", "Default MOTD");
             config.setProperty("server.public", "0");
             config.setProperty("log.level", "1"); // Error
             config.setProperty("database.host", "localhost");
             config.setProperty("database.user", "root");
             config.setProperty("database.password", "mcrp");
             config.setProperty("database.name", "mcrp");
             try {
                 FileOutputStream out = new FileOutputStream("mcrpserver.conf");
                 config.store(out, "Default configuration generated");
                 out.flush();
                 out.close();
             } catch (IOException ex1) {
                 log(LogLevel.ERROR, "Could not write new mcrpserver.conf: "
                         + ex1.getMessage());
             }
         } catch (IOException ex) {
             log(LogLevel.ERROR, "FATAL: Failed to read mcrpserver.conf: "
                     + ex.getMessage());
             end();
         }
     }
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         MCRPServer.log(LogLevel.MINIMAL, "MCRPServer version " + MCRPServer.VERSION_MAJOR
                 + "." + MCRPServer.VERSION_MINOR);
 
         MCRPServer.log(LogLevel.MINIMAL, "Loading level server_level.dat");
         MCRPServer.loadLevel("server_level.dat");
 
         MCRPServer.log(LogLevel.MINIMAL, "Starting server");
         while (running) {
             try {
                 // Accept client connection
                 Socket sock = servSock.accept();
 
                 ClientSession cls = new ClientSession(sock, "client"
                         + clientIncrement);
                 clientIncrement++;
                 clients.add(cls);
                 cls.run();
             } catch (SocketException ex) {
             } catch (IOException ex) {
                 MCRPServer.log(LogLevel.ERROR, "Client accept failed: "
                         + ex.getMessage());
             }
         }
         MCRPServer.log(LogLevel.MINIMAL, "Server ending.");
     }
 
     public static synchronized void end() {
         try {
             // Thread-safe cleanup
             servSock.close();
         } catch (IOException ex) {
             MCRPServer.log(LogLevel.ERROR, "Failed to close socket: "
                     + ex.getMessage());
         }
     }
 
     public static synchronized boolean loadLevel(String file) {
         FileInputStream fis = null;
         GZIPInputStream gzis = null;
         ObjectInputStream in = null;
         DataInputStream inputstream = null;
 
         try {
             fis = new FileInputStream(file);
             gzis = new GZIPInputStream(fis);
             inputstream = new DataInputStream(gzis);
             if (inputstream.readInt() != 0x271bb788) {
                 MCRPServer.log(LogLevel.ERROR, "Level magic number invalid");
                 return false;
             }
             if (inputstream.readByte() > 2) {
                 MCRPServer.log(LogLevel.ERROR, "Level version > 2, failed to load");
                 return false;
             }
             in = new ObjectInputStream(gzis);
             level = (com.mojang.minecraft.level.Level) in.readObject();
         } catch (FileNotFoundException ex) {
             MCRPServer.log(LogLevel.ERROR, "Map file '" + file + "' not found");
             return false;
         } catch (IOException ex) {
             MCRPServer.log(LogLevel.ERROR, "IOException reading map: "
                     + ex.getMessage());
             return false;
         } catch (ClassNotFoundException ex) {
             MCRPServer.log(LogLevel.ERROR, "minecraft_server.jar is not in the"
                     + "lib dir.");
             end();
             return false;
         }
         level.initTransient();
 
         return true;
     }
 
     public static synchronized boolean saveLevel(String file) {
         FileOutputStream fos = null;
         GZIPOutputStream gzos = null;
         ObjectOutputStream out = null;
         DataOutputStream outputstream = null;
 
         try {
             fos = new FileOutputStream(file);
             gzos = new GZIPOutputStream(fos);
             outputstream = new DataOutputStream(gzos);
             outputstream.writeInt(0x271bb788);
             outputstream.writeByte(2);
             out = new ObjectOutputStream(gzos);
             out.writeObject(level);
             outputstream.close();
             out.close();
         } catch(IOException ex) {
             MCRPServer.log(LogLevel.ERROR, "IOException saving map: "
                     + ex.getMessage());
         }
         return true;
     }
 
     public static synchronized void broadcastMessage(byte user,
             String message) {
         // iterate every clientsession
         for (ClientSession i : clients) {
             i.sendServerMessage(user, message);
         }
     }
 
     public static synchronized void killClient(ClientSession cls) {
         for (ClientSession i : clients) {
             if (i==cls) {
                 cls.end();
                 clients.remove(i);
             }
         }
     }
 }
