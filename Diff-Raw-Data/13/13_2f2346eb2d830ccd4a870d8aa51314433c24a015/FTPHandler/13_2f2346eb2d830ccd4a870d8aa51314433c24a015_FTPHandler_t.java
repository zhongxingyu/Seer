 /*
 * Copyright 2011 Danish Maritime Safety Administration. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Danish Maritime Safety Administration ``AS IS''
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of Danish Maritime Safety Administration.
 *
 */
 package dk.frv.eavdam.io;
 
 import dk.frv.eavdam.data.FTP;
 import dk.frv.eavdam.utils.XMLHandler;
 import org.apache.commons.net.ftp.FTPClient;
 import org.apache.commons.net.ftp.FTPFile;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 /**
  * Class for exchanging station data in XML format with ftp sites. Connect method returns an FTP client that can then be
  * passed to other methods for sending, receiving and deleting data and finally disconnecting.
  */
 public class FTPHandler {
 
 	/**
 	 * Connects to a FTP site.
 	 *
 	 * @param ftp  FTP site to connect to
 	 * @return     FTP connection
 	 */
 	public static FTPClient connect(FTP ftp) throws IOException {
 	
 		if (ftp == null || ftp.getServer() == null || ftp.getUsername() == null || ftp.getPassword() == null) {
             throw new IOException("FTP object or its contents is null");
         }	
     
 		System.out.println("Logging into ftp server " + ftp.getServer());
         FTPClient ftpClient = new FTPClient();
         
         // not sure if these are needed...
 		// ftpClient.setConnectTimeout(7500);
 		// ftpClient.setDataTimeout(7500);
 		
         ftpClient.connect(ftp.getServer());
         boolean login = ftpClient.login(ftp.getUsername(), ftp.getPassword());
 		
         if (!ftpClient.isConnected() || !login) {
         	System.out.println("Connection Failed! " + (!login ? "Wrong username/password" : "Connection not available!"));
         	throw new IOException("Connection failed! " + (!login ? "Wrong username/password" : "Connection not available!"));
         }
         
         if (ftp.getDirectory() != null) {
             ftpClient.changeWorkingDirectory(ftp.getDirectory());
         }	
         
 		return ftpClient;
 	}
     
 	/**
 	 * Sends data file to FTP site.
 	 *
 	 * @param  ftpClient  FTP connection
 	 * @param  filename   Name of the file that is to be sent to the FTP site.
 	 */
     public static void sendDataToFTP(FTPClient ftpClient, String filename) throws IOException {
 
 		if (ftpClient == null || !ftpClient.isConnected()) {
 			throw new IOException("FTP Client not connected");
 		}
 
 		System.out.println("Sending " + filename + " to FTP server");
 
         // deletes old files that our organisation has sent
         FTPFile[] files = ftpClient.listFiles();
         for (FTPFile file : files) {
 			if ((filename.indexOf("/") != -1 && file.getName().startsWith(filename.substring(filename.lastIndexOf("/")+1))) ||
 					(filename.indexOf("/") == -1 && file.getName().startsWith(filename))) {
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
 	
 	/**
 	 * Disconnects the FTP connection.
 	 *
 	 * @param ftpClient  FTP connection
 	 */
 	public static void disconnect(FTPClient ftpClient) {
         try {  
 			ftpClient.logout();		
             ftpClient.disconnect();
         } catch (IOException e) {}    
 	}
  
 	/**
 	 * Receives data file from FTP site.
 	 *
 	 * @param ftpClient        FTP connection
 	 * @param importDirectory  Directory to which receive the files
 	 * @param ownFileName      Name of the user's own station data file so that it is not downloaded
 	 * @return                 True if the data was received succesfully, false otherwise
 	 */ 
 	public static boolean importDataFromFTP(FTPClient ftpClient, String importDirectory, String ownFileName) throws IOException {
  
 		if (ftpClient == null || !ftpClient.isConnected()) {
 			throw new IOException("FTP Client not connected");
 		}
  
         if (importDirectory == null) {
             throw new IOException("importDirectory is null");
         }
 		
 		if (ownFileName != null) {
 			if (ownFileName.indexOf("/") != -1) {
 				ownFileName = ownFileName.substring(ownFileName.lastIndexOf("/")+1);
 			}
 		}
 		
         File importDir = new File(importDirectory);
         if(!importDir.exists()) importDir.mkdirs();
         
 		FTPFile[] files = ftpClient.listFiles();
         for (FTPFile file : files) {		
 			if (!file.isDirectory() && (ownFileName == null || !file.getName().equals(ownFileName)) && (file.getName().substring(file.getName().length()-3).equalsIgnoreCase("xml"))) {
 				File tempFile = new File("temp_file_for_testing_timestamp.xml");
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
 						return true;
 					}
 				} else {
 					fos = new FileOutputStream(importedFile);
 					ftpClient.retrieveFile(file.getName(), fos);
 					fos.close();
 					return true;
 				}
 				tempFile.delete();
 			}
 		}
 		return false;
     } 
 	
 	/**
 	 * Deletes user's data file from FTP site.
 	 *
 	 * @param ftpClient        FTP connection
 	 * @param ownFileName      Name of the user's own station data file that is to be deleted
 	 */ 
 	public static void deleteDataFromFTP(FTPClient ftpClient, String ownFileName) throws IOException {
  
 		if (ftpClient == null || !ftpClient.isConnected()) {
 			throw new IOException("FTP Client not connected");
 		}
 		
		if (ownFileName == null) {
			return;
		}
		
 		if (ownFileName != null) {
 			if (ownFileName.indexOf("/") != -1) {
 				ownFileName = ownFileName.substring(ownFileName.lastIndexOf("/")+1);
 			}
 		}		
 
 		System.out.println("Deleting " + ownFileName + " from FTP server");
 
         FTPFile[] files = ftpClient.listFiles();
         for (FTPFile file : files) {
             if (file.getName().startsWith(ownFileName)) {
                 ftpClient.deleteFile(file.getName());
             }
         }
 		
     }
 	
 }
