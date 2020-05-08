 package org.srdbs.core;
 
 import org.apache.log4j.Logger;
 import org.srdbs.sftp.Sftp;
 import org.srdbs.split.FileData;
 import org.srdbs.split.Join;
 import org.srdbs.split.MYSpFile;
 import org.srdbs.split.MyFile;
 import org.srdbs.core.DbConnect;
 
 import javax.crypto.Cipher;
 import javax.crypto.CipherInputStream;
 import javax.crypto.spec.IvParameterSpec;
 import javax.crypto.spec.SecretKeySpec;
 import java.io.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipInputStream;
 
 
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
 
     private static final int IV_LENGTH = 16;
 
     public static boolean chking_zipenc;
     public static boolean chking_enc;
     public static boolean chking_zip;
     public static boolean chking_normal;
 
     public static String restoreFileName;
     public static int fullFileCount;
     public static int curentFileNumber = 0;
 
     public static int runRestore(int FID) {
 
         //Download files
         DbConnect dbConnect2 = new DbConnect();
         List<MYSpFile> getSPFiles = new DbConnect().selectLoadSpQuery(FID);
         fullFileCount = getSPFiles.size();
         for (MYSpFile spfile : getSPFiles) {
             restoreFileName = spfile.getCloud() + "-:" + spfile.getName() + "Total Packets:-" + fullFileCount + "Packets downloaded:-" + curentFileNumber;
             curentFileNumber = curentFileNumber + 1;
 
             if (curentFileNumber > fullFileCount) {
                 curentFileNumber = 1;
             }
             int original = Sftp.download(spfile.getName(), spfile.getCloud(), spfile.getRemotePath());
             if (original != 0) {
                 Sftp.download(spfile.getName(), spfile.getRCloud(), spfile.getRemotePath());
             }
         }
 
 
         try {
             DbConnect dbconnection = new DbConnect();
             List<MYSpFile> listofFiles = ReadSPFile(Global.restoreLocation);
             List<MyFile> listofrecords = dbconnection.selectFullQuery(FID);
 
             for (MyFile mylist : listofrecords) {
                 if (HashCheck(listofFiles, FID)) {
 
                     String Checking_Name = mylist.getName();
                     chking_zipenc = Checking_Name.contains(".zip.enc");
                     chking_enc = Checking_Name.contains(".enc");
                     chking_zip = Checking_Name.contains(".zip");
                     chking_normal = true;
 
                     if (chking_zipenc) {
 
                         chking_normal = false;
 
                         String FileName = mylist.getName();
                         String S_Complete = Global.restoreLocation + "/" + FileName;
                         String D_Com1 = CreateFolder(Global.restoreLocation);
                         String D_Complete = D_Com1 + "/" + FileName;
 
                         Join.join(S_Complete, D_Complete);
                         boolean isFilesDeleted = delete(Global.restoreLocation);
                         System.out.print("All Downloaded parts Are Delete :" + isFilesDeleted);
                         logger.info("All Downloaded parts Are Delete :" + isFilesDeleted);
 
 
                         String rs_fileName = D_Com1 + "/" + FileName;
                         // String resultFileName=fileName+".dec";
                         String resultFileName = rs_fileName.replaceAll(".enc", "");
                         String Ori_name = mylist.getName().replaceAll(".enc", "");
 
                         copy(Cipher.DECRYPT_MODE, D_Complete, resultFileName, "password12345678");
 
                         File myFile = new File(rs_fileName);
                         myFile.delete();
 
                         Decompress(resultFileName, D_Com1, Ori_name);
                         logger.info("File is DeCompressed Successfully :" + Ori_name);
 
                         File zipfile = new File(resultFileName);
                         boolean isDelete = zipfile.delete();
                         System.out.println("Zip File is Deleted :" + isDelete);
                         logger.info("Zip File is Deleted :" + isDelete);
 
                         List<MyFile> fullfilelist = Read(D_Com1);
                         if (FullHashCheck(fullfilelist, FID)) {
 
                             System.out.println("Hashes are matching");
                             logger.info("Hashes are matching");
 
 
                         } else {
                             System.out.println("Error");
                             logger.error("Error");
 
                         }
 
                     } else if (chking_enc) {
 
                         chking_normal = false;
 
                         String FileName = mylist.getName();
                         String S_Complete = Global.restoreLocation + "/" + FileName;
                         String D_Com1 = CreateFolder(Global.restoreLocation);
                         String D_Complete = D_Com1 + "/" + FileName;
 
                         Join.join(S_Complete, D_Complete);
                         boolean isFilesDeleted = delete(Global.restoreLocation);
                         System.out.print("All Downloaded parts Are Delete :" + isFilesDeleted);
                         logger.info("All Downloaded parts Are Delete :" + isFilesDeleted);
 
                         String rs_fileName = D_Com1 + "/" + FileName;
                         String resultFileName = rs_fileName.replaceAll(".enc", "");
 
                         copy(Cipher.DECRYPT_MODE, D_Complete, resultFileName, "password12345678");
 
                         File myFile = new File(rs_fileName);
                         myFile.delete();
 
                         List<MyFile> fullfilelist = Read(D_Com1);
 
                         if (FullHashCheck(fullfilelist, FID)) {
 
                             System.out.println("Hashes are matching");
                             logger.info("Hashes are matching");
 
 
                         } else {
                             System.out.println("Error");
                             logger.error("Error");
                         }
 
 
                     } else if (chking_zip) {
 
                         chking_normal = false;
 
                         String FileName = mylist.getName();
                         String S_Complete = Global.restoreLocation + "/" + FileName;
                         String D_Com1 = CreateFolder(Global.restoreLocation);
                         String D_Complete = D_Com1 + "/" + FileName;
 
                         Join.join(S_Complete, D_Complete);
                         boolean isFilesDeleted = delete(Global.restoreLocation);
                         System.out.print("All Downloaded parts Are Delete :" + isFilesDeleted);
                         logger.info("All Downloaded parts Are Delete :" + isFilesDeleted);
 
                         String rs_fileName = D_Com1 + "/" + FileName;
                         String Ori_name = mylist.getName().replaceAll(".zip", "");
 
                         Decompress(rs_fileName, D_Com1, Ori_name);
                         logger.info("File is DeCompressed Successfully :" + FileName);
 
 
                         File zipfile = new File(rs_fileName);
                         boolean isDelete = zipfile.delete();
                         System.out.println("Zip File is Deleted :" + isDelete);
                         logger.info("Zip File is Deleted :" + isDelete);
 
                         List<MyFile> fullfilelist = Read(D_Com1);
 
                         if (FullHashCheck(fullfilelist, FID)) {
 
                             System.out.println("Hashes are matching");
                             logger.info("Hashes are matching");
 
                         } else {
                             System.out.println("Error");
                             logger.error("Error");
                         }
 
                     } else if (chking_normal) {
 
                         String FileName = mylist.getName();
                         String S_Complete = Global.restoreLocation + "/" + FileName;
                         String D_Com1 = CreateFolder(Global.restoreLocation);
                         String D_Complete = D_Com1 + "/" + FileName;
 
                         Join.join(S_Complete, D_Complete);
                         boolean isFilesDeleted = delete(Global.restoreLocation);
                         System.out.print("All Downloaded parts Are Delete :" + isFilesDeleted);
                         logger.info("All Downloaded parts Are Delete :" + isFilesDeleted);
 
 
                         List<MyFile> fullfilelist = Read(D_Com1);
 
                         if (FullHashCheck(fullfilelist, FID)) {
 
                             System.out.println("Hashes are matching");
                             logger.info("Hashes are matching");
 
                         } else {
                             System.out.println("Hashes are not matching");
                             logger.error("Hashes are not matching");
                         }
 
                     }
 
 
                 } else {
                     restoreLog.error("Error in split part hash.");
                 }
             }
         } catch (Exception ex) {
             restoreLog.error("Error : " + ex);
 
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
         int count = 0;
         DbConnect dbconnect = new DbConnect();
         List<MYSpFile> listofFileSp = dbconnect.selectQuery(restoreFileID);
         int SplitCountFile = dbconnect.SplitFileCount(restoreFileID);
 
         for (MYSpFile myfile : listoffiles) {
 
             for (MYSpFile dbfile : listofFileSp) {
 
                 if (SplitCountFile < count) {
 
                     if ((myfile.getName().equalsIgnoreCase(dbfile.getName()))) {
 
                         System.out.print("Pass" + myfile.getName());
 
                         if (myfile.getHash().equalsIgnoreCase(dbfile.getHash())) {
 
                             Check = true;
                             restoreLog.info("Pass : " + myfile.getName());
                             System.out.print("Pass : " + myfile.getName());
 
                         } else {
 
                             Check = false;
                             restoreLog.error("Fail" + myfile.getName());
                             System.out.print("Fail" + myfile.getName());
                             //download the fail data chunk
                         }
                     }
                 }
             }
         }
 
         return Check;
 
     }
 
     public static boolean Download_HashCheck(List<MYSpFile> listoffiles, int restoreFileID) throws Exception {
 
         boolean Check = true;
         int count = 0;
         DbConnect dbconnect = new DbConnect();
         List<MYSpFile> listofFileSp = dbconnect.selectQuery(restoreFileID);
         int SplitCountFile = dbconnect.SplitFileCount(restoreFileID);
 
         for (MYSpFile myfile : listoffiles) {
 
             for (MYSpFile dbfile : listofFileSp) {
 
                 if (SplitCountFile < count) {
 
                     if ((myfile.getName().equalsIgnoreCase(dbfile.getName()))) {
 
                         if (myfile.getHash().equalsIgnoreCase(dbfile.getHash())) {
 
                             Check = true;
                             restoreLog.info("Pass : " + myfile.getName());
                             System.out.print("Pass : " + myfile.getName());
 
                         } else {
 
                             Check = false;
                             restoreLog.error("Fail" + myfile.getName());
                             System.out.print("Fail" + myfile.getName());
                         }
                     }
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
 
                 if (chking_zipenc) {
 
                     if (myfile.getName().equalsIgnoreCase(dbfile.getName().replaceAll(".zip.enc", ""))
                             && myfile.getHash().equalsIgnoreCase(dbfile.getHash())) {
                         pass = true;
                         restoreLog.info("Pass : " + myfile.getName());
                     } else {
                         pass = false;
                         restoreLog.info("Fail :" + myfile.getName());
                     }
 
                 } else if (chking_enc) {
 
                     if (myfile.getName().equalsIgnoreCase(dbfile.getName().replaceAll(".enc", ""))
                             && myfile.getHash().equalsIgnoreCase(dbfile.getHash())) {
 
                         pass = true;
                         restoreLog.info("Pass : " + myfile.getName());
                     } else {
                         pass = false;
                         restoreLog.info("Fail :" + myfile.getName());
 
                     }
 
                 } else if (chking_zip) {
 
                     if (myfile.getName().equalsIgnoreCase(dbfile.getName().replaceAll(".zip", ""))
                             && myfile.getHash().equalsIgnoreCase(dbfile.getHash())) {
 
                         pass = true;
                         restoreLog.info("Pass : " + myfile.getName());
                     } else {
                         pass = false;
                         restoreLog.info("Fail :" + myfile.getName());
 
                     }
 
                 } else if (chking_normal) {
 
                     if (myfile.getName().equalsIgnoreCase(dbfile.getName())
                             && myfile.getHash().equalsIgnoreCase(dbfile.getHash())) {
                         pass = true;
                         restoreLog.info("Pass : " + myfile.getName());
                     } else {
                         pass = false;
                         restoreLog.info("Fail :" + myfile.getName());
 
                     }
 
                 }
 
             }
         }
 
         return pass;
     }
 
     public static void Decompress(String s_path, String d_path, String Fname) {
 
         String Fname_full = Fname.replaceAll(".zip", "");
         String D_path_full = d_path + "/" + Fname_full;
         try {
             BufferedOutputStream out = null;
             ZipInputStream in = new ZipInputStream
                     (new BufferedInputStream(new FileInputStream(s_path)));
             ZipEntry entry;
             while ((entry = in.getNextEntry()) != null) {
                 int count;
                 byte data[] = new byte[1000];
                 out = new BufferedOutputStream(new
                         FileOutputStream(D_path_full), 1000);
                 while ((count = in.read(data, 0, 1000)) != -1) {
                     out.write(data, 0, count);
                 }
                 out.flush();
                 out.close();
 
             }
 
             in.close();
 
         } catch (Exception e) {
             e.printStackTrace();
             logger.info("Decompression Fails in :" + Fname_full);
         }
 
     }
 
     public static void copy(int mode, String inputFile, String outputFile, String password) throws Exception {
 
         BufferedInputStream is = new BufferedInputStream(new FileInputStream(inputFile));
         BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile));
         if (mode == Cipher.ENCRYPT_MODE) {
             //encrypt(is, os, password);
         } else if (mode == Cipher.DECRYPT_MODE) {
             decrypt(is, os, password);
         } else throw new Exception("unknown mode");
         is.close();
         os.close();
     }
 
     public static void decrypt(InputStream in, OutputStream out, String password) throws Exception {
 
         byte[] iv = new byte[IV_LENGTH];
         in.read(iv);
         //System.out.println(">>>>>>>>red"+Arrays.toString(iv));
 
         Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding"); //"DES/ECB/PKCS5Padding";"AES/CBC/PKCS5Padding"
         SecretKeySpec keySpec = new SecretKeySpec(password.getBytes(), "AES");
         IvParameterSpec ivSpec = new IvParameterSpec(iv);
         cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
 
         in = new CipherInputStream(in, cipher);
         byte[] buf = new byte[1024];
         int numRead = 0;
         while ((numRead = in.read(buf)) >= 0) {
             out.write(buf, 0, numRead);
         }
         out.close();
     }
 
     public static String CreateFolder(String path) throws Exception {
 
         String fldate;
 
         DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
         java.util.Date date = new java.util.Date();
         fldate = dateFormat.format(date);
 
         String strDirectoy = path + "/" + fldate;
 
         // Create one directory
         boolean success = (
                 new File(strDirectoy)).mkdir();
         if (success) {
             System.out.println("Directory: " + strDirectoy + " created");
             logger.info("Directory: " + strDirectoy + " created");
         }
 
         return strDirectoy;
     }
 
     public static boolean delete(String path) {
 
         boolean DeleteCheck = false;
 
         File folder = new File(path);
         String files1;
 
         File[] listOfFiles = folder.listFiles();
         for (int i = 0; i < listOfFiles.length; i++) {
             files1 = listOfFiles[i].getName();
             String Full_path = path + "/" + files1;
             File DelFile = new File(Full_path);
             DeleteCheck = DelFile.delete();
 
             if (!DeleteCheck) {
 
                 System.out.println("File is not Deleted :" + files1);
                 logger.error("File is not Deleted :" + files1);
             } else {
 
                 System.out.println("Delete File :" + files1);
                 logger.info("Delete File :" + files1);
             }
 
 
         }
 
         return DeleteCheck;
     }
 
 }
