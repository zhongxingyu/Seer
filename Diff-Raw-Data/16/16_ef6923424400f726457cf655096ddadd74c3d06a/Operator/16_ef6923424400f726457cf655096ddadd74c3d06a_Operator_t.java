 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package syncer;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.StandardCopyOption;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Marc.Hoaglin
  */
 public class Operator {
 
     static BlockingQueue<String> Q;
     public static BlockingQueue<String> FinishedFilesQ;
     static Map<String, String> Clients;
     static Map<String, String> Inprocess;
     static Map<String, String> XbmcREQSent;
     public final static Logger opLOG = Logger.getLogger(Operator.class.getName());
     public static String szREQlogfolderXBMC;
     public static String szCliFileListFolder;
     public static String szREQlogfolderFILE;
     private static StandardCopyOption overWrite = StandardCopyOption.REPLACE_EXISTING;    
     static ArrayList<String[]> alREQ;
     static String sep = ",,";
     static Map<String, String> Resuming;
     static Map<String, String> RequestLOG;
     static Map<String, String> FilesRequested;
 
     Operator() {
         opLOG.info("Starting Operator Queues and initializing Maps");
         Q = new LinkedBlockingQueue<>(100);
         FinishedFilesQ = new LinkedBlockingQueue<>();
         // Clients is name = UID
         Clients = Collections.synchronizedMap(new HashMap<String, String>());
         // Inprocess is name = ListFile 
         Inprocess = Collections.synchronizedMap(new HashMap<String, String>());
         // Resuming is UID = file
         Resuming = Collections.synchronizedMap(new HashMap<String, String>());
         // RequestLOG  is UID = logfile
         RequestLOG = Collections.synchronizedMap(new HashMap<String, String>());
         XbmcREQSent = Collections.synchronizedMap(new HashMap<String, String>());
         FilesRequested = Collections.synchronizedMap(new HashMap<String, String>());
         szREQlogfolderXBMC = Config.readProp("receive.tmp", Config.cfgFile) + File.separatorChar + "xbmc" + File.separatorChar;
         szREQlogfolderFILE = Config.readProp("receive.tmp", Config.cfgFile) + File.separatorChar + "file" + File.separatorChar;
         szCliFileListFolder = Config.readProp("receive.tmp", Config.cfgFile) + File.separatorChar + "xbmc-client" + File.separatorChar;
         alREQ = new ArrayList<>();
     }
 
     private static void Ops() {
         try {
             //System.out.println("Config file at: " + Config.cfgFile);
             Thread.sleep(500);
         } catch (InterruptedException ex) {
             opLOG.severe(ex.getMessage());
         }
         if (Config.readProp("file.sync", Config.cfgFile).equalsIgnoreCase("true")) {
 //            String cliFile = szREQlogfolderFILE + Clients.get(client) + ".txt";
             // file sync ops here
         }
 //        System.out.println(Config.readProp("xbmc.sync", Config.cfgFile));
         if (Config.readProp("xbmc.sync", Config.cfgFile).equalsIgnoreCase("true")) {
 
             // xbmc sync ops here
             while (Config.readProp("sync.partners.xbmc.csv", Config.cfgFile) != null && !Clients.isEmpty()) {
                 String client[] = Config.readProp("sync.partners.xbmc.csv", Config.cfgFile).split(",");
                 // find if configured clients are connected and do work
                 opLOG.fine("Checking for connected Clients for Operator to start work");
 //                System.out.println("Checking for connected Clients for Operator to start work");
 
                 for (int c = 0; c < client.length; c++) {
 
 //                    System.out.println("Client: " + client[c] + " is connected: " + ConnectionHandler.sockets.get(Clients.get(client[c])).isConnected() + " request sent: " + XbmcREQSent.containsKey(client[c]));
                     if (ConnectionHandler.sockets.get(Clients.get(client[c])) != null) {
                         if (ConnectionHandler.sockets.get(Clients.get(client[c])).isConnected()) {
 
                             worker(client[c]);
                         } else {
                             Clients.remove(client[c]);
                             ConnectionHandler.sockets.remove(Clients.get(client[c]));
                         }
                     }
                 }
                 try {
                     Thread.sleep(1000);
                 } catch (InterruptedException ex) {
                     opLOG.severe(ex.getMessage());
                 }
             }
         }
     }
 
     void Clientwatcher() {
 
         new Thread(new Runnable() {
             public void run() {
 
                 while (true) {
                     Ops();
                     try {
                         Thread.sleep(1000);
                     } catch (InterruptedException ex) {
                         opLOG.severe(ex.getMessage());
                     }
                 }
             }
         }).start();
 //        System.out.println("Client watcher started....");
         opLOG.info("Client watcher started....");
     }
 
     static void worker(String client) {
         // look for unfinished work for connected node(s) and request remaining files
         String cliFile = szREQlogfolderXBMC + Clients.get(client) + ".txt";
         RequestLOG.put(Clients.get(client), cliFile);
         opLOG.fine("Looking for xbmc list at: " + cliFile + " " + new File(cliFile).exists());
         // checking for Files we need to resume first
         opLOG.fine("Checking if there are files to resume");
         Resumer(Clients.get(client));
         opLOG.fine("Resumer info: " + Resuming.get(Clients.get(client)));
 //        int i = 0;
 //        if (new File(cliFile).exists() && i == 0) {
 //            InitSort(cliFile, client, "xbmc");
 //            i = 1;
 //        } else {
 //            
 //        }
         if (new File(cliFile).exists() && !Inprocess.containsKey(cliFile) && !Resuming.containsKey(Clients.get(client))) {
 
             opLOG.info("Starting new sorter because it's not already being processed for " + client);
             //read file and send requests
             xbmcHandler.ReadFile(cliFile, Clients.get(client));
             //let system know file is being processed
             Inprocess.put(cliFile, client);
             InitSort(cliFile, client, "xbmc");
             
             
             
         } else if (!Resuming.containsKey(Clients.get(client)) && !XbmcREQSent.containsKey(client)) {
             //otherwise request new list and sync
             opLOG.info("Requesting new XBMC list");
             Sender.putmQ(Clients.get(client), "REQXLST,," + Config.readProp("local.name", Config.cfgFile));
             XbmcREQSent.put(client, "true");
         }
     }
 
     public static void InitSort(final String szLogOfReq, final String szClient, final String szType) {
 
         new Thread(new Runnable() {
             public void run() {
 
                 while (Inprocess.containsValue(szClient)) {
                     try {
                         //take file String from Q
                         String szFile = FinishedFilesQ.take();
                         
                         WatchNsort(szFile, szLogOfReq, szClient, szType);
                         Thread.sleep(1000);
                     } catch (InterruptedException ex) {
                         opLOG.severe(ex.getMessage());
                     }
                 }
                     
                     
                 
             }
         }).start();
 
         opLOG.info("Complete files Q started....");
     }
 
     static void WatchNsort(String szFile, String szReqLog, String szClient, String szType) {
         //read requested files log for sort decision
         String[] strInfo = GetOutPutPath(szReqLog, szFile);
         opLOG.info("Sorting files received");
         String mTitle = strInfo[1];
         String mYear = strInfo[2];
         String mPath = strInfo[4];
         String mImDb = strInfo[3];
         String mQual = strInfo[5];
         String szOutFile = null;
 //        for (int i = 0; i < strInfo.length; i++) {
 //            System.out.println(strInfo[i]);
 //        }
 
         String szOutFolder = Config.readProp("local.archive.point", Config.cfgFile) + File.separatorChar + szType + File.separatorChar + szClient + File.separatorChar;
         if (szType.equalsIgnoreCase("xbmc")) {
             szOutFolder = szOutFolder + mTitle + " (" + mYear + ")";
             if (!new File(szOutFolder).exists()) {
                 new File(szOutFolder).mkdirs();
             }
             szOutFile = szOutFolder + File.separatorChar + (new File(mPath).getName());
         } else if (szType.equalsIgnoreCase("file")) {
             opLOG.warning("File Sync not implemented...yet");
             //szOutFile = szOutFolder + szFile;
         }
         opLOG.info("Moving: " + szFile + " to: " + szOutFile);
         nioMove(szFile, szOutFile);
         opLOG.info("Removing line containing: " + new File(szFile).getName() + " from: " + szReqLog);
        xbmcHandler.removeLineFromFile(szReqLog, new File(szFile).getName());
 
     }
 
     static String[] GetOutPutPath(String szXList, String szSearch) {
         opLOG.info("Searching for: " + szSearch + " in " + szXList);
         String szMatch[] = new String[6];
         String lineArray[] = new String[6];
         String szSearchMe = new File(szSearch).getName();
         try {
             // Open the file that is the first 
             // command line parameter
             FileInputStream fstream = new FileInputStream(szXList);
             // Get the object of DataInputStream
             DataInputStream in = new DataInputStream(fstream);
             BufferedReader br = new BufferedReader(new InputStreamReader(in));
             String strLine;
             //Read File Line By Line
             while ((strLine = br.readLine()) != null) {
                 // Print the content on the console
 //                System.out.println(strLine);
                 lineArray = strLine.split("\t");
                 if (lineArray[4].contains(szSearchMe)) {
                     szMatch = lineArray;
                 }
 
             }
             //Close the input stream
             in.close();
 
         } catch (Exception e) {//Catch exception if any
             opLOG.severe("Error: " + e.getMessage());
         }
         return szMatch;
     }
 
     public static void nioMove(String szsrcFile, String szdstFile) {
 
         Path source = Paths.get(szsrcFile);
         Path destination = Paths.get(szdstFile);
         Path targetPath = destination.getParent();
         try {
             if (!Files.exists(targetPath)) {
                 Files.createDirectory(targetPath);
             }
             Files.move(source, destination, overWrite);
             opLOG.info("File Moved to:  " + szdstFile);
         } catch (IOException ex) {
             opLOG.severe(ex.getMessage());
         }
     }
 
     public static void csvWrite(String sztext, String szFile) {
         try {
             FileWriter fwrite = new FileWriter(szFile, true);
             BufferedWriter bw = new BufferedWriter(fwrite);
 //            System.out.println(sztext);
             bw.write(sztext);
             bw.newLine();
             bw.close();;
         } catch (Exception ex) {
             opLOG.severe(ex.getMessage());
         }
 
     }
 
     static void Resumer(String clientUID) {
         if (!Resuming.containsKey(clientUID)) {
             File file = new File(Config.getLogFolder() + File.separatorChar + clientUID);
             if (!file.exists()) {
                 file.mkdirs();
             }
             File[] files = file.listFiles();
             for (int fileInList = 0; fileInList < files.length; fileInList++) {
 //                System.out.println(files[fileInList].toString());
                 ReadLastLine(files[fileInList].toString(), clientUID);
             }
         }
 
     }
 
     static void ReadLastLine(String szTlog, String szUID) {
         String lineArray[] = new String[6];
         FileInputStream in = null;
         try {
 
             in = new FileInputStream(szTlog);
             BufferedReader br = new BufferedReader(new InputStreamReader(in));
 
             String strLine = null;
             String tmp = null;
             while ((tmp = br.readLine()) != null) {
                 strLine = tmp;
             }
             String lastLine = strLine;
 //            System.out.println(lastLine);
             in.close();
             lineArray = lastLine.split(sep);
 
             Sender.putmQ(szUID, "REQ" + sep + lineArray[7] + sep + lineArray[4]);
             for (int i = 0; i < lineArray.length; i++) {
                 opLOG.fine("Last line: " + lineArray[i]);
             }
             Resuming.put(szUID, lineArray[7]);
 
         } catch (FileNotFoundException ex) {
             opLOG.severe(ex.getMessage());
         } catch (IOException ex) {
             opLOG.severe(ex.getMessage());
         } finally {
             try {
                 in.close();
             } catch (IOException ex) {
                 opLOG.severe(ex.getMessage());
             }
         }
     }
     public static void putFinishedQ(String szFile) {
         try {
             opLOG.info("Putting " + szFile + " in Finished Q");
             FinishedFilesQ.put(szFile);
 //            System.out.println(FinishedFilesQ.peek() + " is in Finished Q");
         } catch (InterruptedException ex) {
             opLOG.severe(ex.getMessage());
         }
     }
 }
