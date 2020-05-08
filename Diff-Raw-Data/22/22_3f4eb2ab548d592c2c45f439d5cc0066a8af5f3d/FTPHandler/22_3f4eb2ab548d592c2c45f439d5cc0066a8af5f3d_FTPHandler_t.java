 
 package dk.frv.eavdam.io;
 
 import dk.frv.eavdam.data.FTP;
 import dk.frv.eavdam.utils.XMLHandler;
 import org.apache.commons.net.ftp.FTPClient;
 import org.apache.commons.net.ftp.FTPFile;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
  
 public class FTPHandler {
 
 	public static FTPClient connect(FTP ftp) throws IOException {
 		if (ftp == null || ftp.getServer() == null || ftp.getUsername() == null || ftp.getPassword() == null) {
             throw new IOException("FTP object or its contents is null");
         }	
     	System.out.println("Logging into ftp server " + ftp.getServer());
         FTPClient ftpClient = new FTPClient();
        
        //Not sure if these are needed...
//		ftpClient.setConnectTimeout(7500); //7.5s
//		ftpClient.setDataTimeout(7500); //7.5s
 		
         ftpClient.connect(ftp.getServer());
         boolean login = ftpClient.login(ftp.getUsername(), ftp.getPassword());
         if(!ftpClient.isConnected() || !login){
         	System.out.println("Connection Failed! "+(!login ? "Wrong username/password" : "Connection not available!"));
         	throw new IOException("Connection failed! "+(!login ? "Wrong username/password" : "Connection not available!"));
 //        	return null;
         }
         
         if (ftp.getDirectory() != null) {
             ftpClient.changeWorkingDirectory(ftp.getDirectory());
         }	
         
 		return ftpClient;
 	}
     
     public static void sendDataToFTP(FTPClient ftpClient, String filename) throws IOException {
 
 		if (ftpClient == null || !ftpClient.isConnected()) {
 			throw new IOException("FTP Client not connected");
 		}
 
 		System.out.println("Sending " + filename + " to FTP server");
 
         // deletes old files that our organisation has sent
         int i = 0;
         if (filename.indexOf("/") != -1) {
             i = filename.lastIndexOf("/")+1;
         }
         String prefix = filename; //.substring(i, filename.length()-18);  // parses the directory and the date + extension from the filename
         FTPFile[] files = ftpClient.listFiles();
         for (FTPFile file : files) {
             if (file.getName().startsWith(prefix)) {
                 ftpClient.deleteFile(file.getName());
             }
         }
 
         FileInputStream fis = new FileInputStream(filename);
         if (filename.indexOf("/") != -1) {
             ftpClient.storeFile(filename.substring(filename.lastIndexOf("/")+1), fis);
         } else {
             ftpClient.storeFile(filename, fis);            
         }
 
         try {
             if (fis != null) {
                 fis.close();
             }    
         } catch (IOException e) {}    
     }
 	
 	public static void disconnect(FTPClient ftpClient) {
         try {  
 			ftpClient.logout();		
             ftpClient.disconnect();
         } catch (IOException e) {}    
 	}
  
 	public static void importDataFromFTP(FTPClient ftpClient, String importDirectory, String ownFileName) throws IOException {
  
 		if (ftpClient == null || !ftpClient.isConnected()) {
 			throw new IOException("FTP Client not connected");
 		}
  
         if (importDirectory == null) {
             throw new IOException("importDirectory is null");
         }
 
         if (ownFileName == null) {
             throw new IOException("ownFileName is null");
         }
		
        File importDir = new File(importDirectory);
        if(!importDir.exists()) importDir.mkdirs();
        
 		FTPFile[] files = ftpClient.listFiles();
         for (FTPFile file : files) {		
 			if (!file.isDirectory() && !file.getName().equals(ownFileName) && (file.getName().substring(file.getName().length()-3).equalsIgnoreCase("xml"))) {
 				File tempFile = new File(importDirectory + File.separator + "temp_file_for_testing_timestamp.xml");
 				FileOutputStream fos = new FileOutputStream(tempFile);
 				ftpClient.retrieveFile(file.getName(), fos);
 				fos.close();
 				File importedFile = new File(importDirectory + File.separator + file.getName());
 				if (importedFile.exists()) {
 					if (XMLHandler.isOlderXML(importedFile, tempFile)) {
 						importedFile.delete();
 						fos = new FileOutputStream(importedFile);
 						ftpClient.retrieveFile(file.getName(), fos);
 						fos.close();
 					}
 				} else {
 					fos = new FileOutputStream(importedFile);
 					ftpClient.retrieveFile(file.getName(), fos);
 					fos.close();
 				}
 				tempFile.delete();
 			}
 		}
     } 
  
 }
