 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dBox.Server;
 
 import dBox.FilePacket;
 import dBox.ServerDetails;
 import dBox.ServerUtils.IServersync;
 import dBox.utils.CustomLogger;
 import dBox.utils.Hashing;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author harsimran.maan
  */
 public class ServerSyncChecker extends Thread
 {
 
     private final String myServername;
     private final PeerDetailsGetter peerDetails;
     private IServersync syncProvider;
     private ServerDetails monitorDetails;
     private final int port;
 
     public ServerSyncChecker(String myServername, int port)
     {
         this.myServername = myServername;
         this.port = port;
         this.peerDetails = new PeerDetailsGetter();
     }
 
     private boolean firstTimeSync() throws Exception
     {
         setMonitor();
         HashMap<String, String> allFileNames = this.syncProvider.getAllFileNames();
         System.out.println(allFileNames);
         for (String path : allFileNames.keySet())
         {
             if (!Hashing.getSHAChecksum(path).equals(allFileNames.get(path)))
             {
                 copyFile(path);
             }
         }
         return true;
     }
 
     private void startSync()
     {
         try
         {
             firstTimeSync();
         }
         catch (Exception ex)
         {
             Logger.getLogger(ServerSyncChecker.class.getName()).log(Level.SEVERE, null, ex);
         }
 //        while (true)
 //        {
 //        }
     }
 
     private void copyFile(String path) throws RemoteException
     {
         FilePacket packet = syncProvider.getFile(path);
         Path get = Paths.get(path);
         String pathOnServer = get.toString().replaceAll(packet.getName() + "$", "");
         CustomLogger.log("Path copied " + pathOnServer);
         File theFile = new File(pathOnServer);
         theFile.mkdirs();
         pathOnServer += packet.getName();
 
         try
         {
             OutputStream out = new FileOutputStream(pathOnServer);
             packet.copy(out);
         }
         catch (FileNotFoundException ex)
         {
             Logger.getLogger(ServerSyncChecker.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 
     private void setMonitor() throws Exception
     {
         this.monitorDetails = peerDetails.getMonitorDetails(myServername);
         setProvider();
     }
 
     @Override
     public void run()
     {
         startSync();
     }
 
     private void setProvider() throws Exception
     {
         Registry registry = LocateRegistry.getRegistry(monitorDetails.getServerName(), monitorDetails.getPort());
         syncProvider = (IServersync) registry.lookup(IServersync.class.getSimpleName());
     }
 }
