 package org.srdbs.core;
 
 import org.apache.log4j.Logger;
 import org.srdbs.split.FileData;
 import org.srdbs.split.Join;
 import org.srdbs.split.MYSpFile;
 import org.srdbs.split.MyFile;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Secure and Redundant Data Backup System.
  * User: Thilina Piyasundara
  * Date: 6/6/12
  * Time: 1:14 PM
  * For more details visit : http://www.thilina.org
  */
 public class RunRestore {
 
     public static Logger logger = Logger.getLogger("systemsLog");
     public static Logger restoreLog = Logger.getLogger("restoreLog");
 
     public static int runRestore(String downloadPath, String restorePath, int restoreFileID) {
 
         try {
             DbConnect dbconnection = new DbConnect();
             List<MYSpFile> listofFiles = ReadSPFile(downloadPath);
             List<MyFile> listofrecords = dbconnection.selectFullQuery(restoreFileID);
             for (MyFile mylist : listofrecords) {
                 if (HashCheck(listofFiles, restoreFileID)) {
                     // MyFile myfile = new MyFile();
                     String FileName = mylist.getName();
                     String S_Complete = downloadPath + "/" + FileName;
                     String D_Complete = restorePath + "/" + FileName;
                     Join.join(S_Complete, D_Complete);
 
                     List<MyFile> fullfilelist = Read(restorePath);
                     if (FullHashCheck(fullfilelist, restoreFileID)) {
                         restoreLog.info("Hashes are matching");
                     } else {
                         restoreLog.error("Error");
                     }
                 } else {
                     restoreLog.error("Error");
                 }
             }
         } catch (Exception ex) {
             restoreLog.error("Error");
         }
 
         return 0;
     }
 
     public static List<MYSpFile> ReadSPFile(String path) throws Exception {
 
         String Full_Path;
         String Hash, date;
 
         File folder = new File(path);
         List<MYSpFile> fileList = new ArrayList<MYSpFile>();
         for (File sysFile : folder.listFiles()) {
             Full_Path = path + "/" + sysFile.getName();
             Hash = FileData.getHash(Full_Path);
             MYSpFile mySPFile = new MYSpFile();
             mySPFile.setName(sysFile.getName());
             mySPFile.setHash(Hash);
             mySPFile.setFile(sysFile);
             fileList.add(mySPFile);
         }
         return fileList;
     }
 
 
     public static List<MyFile> Read(String path) throws Exception {
 
         String Full_Path;
         String Hash, date;
         File folder = new File(path);
         List<MyFile> fileList = new ArrayList<MyFile>();
         for (File sysFile : folder.listFiles()) {
             Full_Path = path + "/" + sysFile.getName();
             Hash = FileData.getHash(Full_Path);
             MyFile myFile = new MyFile();
             myFile.setName(sysFile.getName());
             myFile.setHash(Hash);
             myFile.setFile(sysFile);
             fileList.add(myFile);
         }
 
         return fileList;
     }
 
     public static boolean HashCheck(List<MYSpFile> listoffiles, int restoreFileID) throws Exception {
 
         boolean Check = true;
         DbConnect dbconnect = new DbConnect();
         List<MYSpFile> listofFileSp = dbconnect.selectQuery(restoreFileID);
 
         for (MYSpFile myfile : listoffiles) {
             for (MYSpFile dbfile : listofFileSp) {
                 if ((myfile.getName().equalsIgnoreCase(dbfile.getName()))
                         && (myfile.getHash().equalsIgnoreCase(dbfile.getHash()))) {
                     Check = true;
                     restoreLog.info("Pass");
 
                 } else {
                     Check = false;
                     restoreLog.error("Fail");
                 }
             }
         }
 
         return Check;
     }
 
     public static boolean FullHashCheck(List<MyFile> listoffiles, int i) throws Exception {
 
         boolean pass = true;
         DbConnect dbconnect = new DbConnect();
         List<MyFile> list = dbconnect.selectFullQuery(i);
         for (MyFile myfile : listoffiles) {
             for (MyFile dbfile : list) {
                 if (myfile.getName().equalsIgnoreCase(dbfile.getName())
                         && myfile.getHash().equalsIgnoreCase(dbfile.getHash())) {
                     pass = true;
                     restoreLog.info("Pass");
                 } else {
                     pass = false;
                     restoreLog.info("Fail");
                 }
             }
         }
 
         return pass;
     }
 }
