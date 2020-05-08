 package edu.jhu.cvrg.waveform.utility;
 /*
 Copyright 2013 Johns Hopkins University Institute for Computational Medicine
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
 /**
 * @author Chris Jurado
 * 
 */
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import org.apache.commons.net.ftp.FTP;
 import org.apache.commons.net.ftp.FTPClient;
 
import edu.jhu.cvrg.waveform.exception.UploadFailureException;

 public class FTPUtility {
 	
 	private static FTPClient client = new FTPClient();
 	
 	private FTPUtility(){}
 
	public static void uploadToRemote(String outputdir, File file) throws UploadFailureException {
 
 		try {
 			FileInputStream inputStream = new FileInputStream(file);		
 			client.connect(ResourceUtility.getFtpHost());		
 	        client.login(ResourceUtility.getFtpUser(), ResourceUtility.getFtpPassword());
 	        client.enterLocalPassiveMode();
 	        client.setFileType(FTP.BINARY_FILE_TYPE);
 	        client.makeDirectory(outputdir);
 	        client.changeWorkingDirectory(outputdir);
 	        client.storeFile(file.getName(), inputStream);
 	        
 	        if(inputStream.available() > 0){
 	        	System.out.println("File upload failed.");
	        	inputStream.close();
	        	throw new UploadFailureException("Unable to upload the file to the remote location");
 	        }
 	        
 	        inputStream.close();
 	        client.disconnect();
 	        
 		} catch (IOException e) {
 			e.printStackTrace();
 		}	
 	}
 	
 	public static void downloadFromRemote(String remoteDirectory, String fileName){
 		try {
 
 			System.out.println("downloadFromRemote() Remote path/filename:" + remoteDirectory + fileName);
 			
 			FileOutputStream outputStream = new FileOutputStream(ResourceUtility.getLocalDownloadFolder() + fileName);		
 			client.connect(ResourceUtility.getFtpHost());		
 	        client.login(ResourceUtility.getFtpUser(), ResourceUtility.getFtpPassword());
 	        client.enterLocalPassiveMode();
 	        client.setFileType(FTP.BINARY_FILE_TYPE);
 	        System.out.println("Remote Directory Before:" + remoteDirectory);
 	        remoteDirectory = remoteDirectory.substring(1);
 	        System.out.println("Remote Directory After:" + remoteDirectory);
 	        client.changeWorkingDirectory(remoteDirectory);
 	        client.retrieveFile(fileName, outputStream);
 	        outputStream.close();
 	        client.disconnect();
 	        
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void downloadFromRemote(String remoteDirectory, String fileName, String outputDirectory){
 		try {
 
 			System.out.println("downloadFromRemote() Remote path/filename:" + remoteDirectory + fileName);
 			
 			FileOutputStream outputStream = new FileOutputStream(outputDirectory + fileName);		
 			client.connect(ResourceUtility.getFtpHost());		
 	        client.login(ResourceUtility.getFtpUser(), ResourceUtility.getFtpPassword());
 	        client.enterLocalPassiveMode();
 	        client.setFileType(FTP.BINARY_FILE_TYPE);
 	        System.out.println("Remote Directory Before:" + remoteDirectory);
 	        remoteDirectory = remoteDirectory.substring(1);
 	        System.out.println("Remote Directory After:" + remoteDirectory);
 	        client.changeWorkingDirectory(remoteDirectory);
 	        client.retrieveFile(fileName, outputStream);
 	        outputStream.close();
 	        client.disconnect();
 	        
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
