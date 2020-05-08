 package org.bitrepository;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.text.SimpleDateFormat;
 
 import org.bitrepository.protocol.security.SecurityManager;
 import org.bitrepository.access.AccessComponentFactory;
 import org.bitrepository.access.getchecksums.GetChecksumsClient;
 import org.bitrepository.access.getfile.GetFileClient;
 import org.bitrepository.access.getfileids.GetFileIDsClient;
 import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumType;
 import org.bitrepository.bitrepositoryelements.FileIDs;
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.modify.ModifyComponentFactory;
 import org.bitrepository.modify.deletefile.DeleteFileClient;
 import org.bitrepository.modify.putfile.PutFileClient;
 import org.bitrepository.modify.replacefile.ReplaceFileClient;
 import org.bitrepository.client.eventhandler.EventHandler;
 import org.bitrepository.client.exceptions.OperationFailedException;
 import org.bitrepository.settings.collectionsettings.CollectionSettings;
 import org.bitrepository.utils.HexUtils;
 import org.bitrepository.utils.XMLGregorianCalendarConverter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class BasicClient {
     private PutFileClient putClient;
     private GetFileClient getClient;
     private GetChecksumsClient getChecksumClient;
     private GetFileIDsClient getFileIDsClient;
     private DeleteFileClient deleteFileClient;
     private ReplaceFileClient replaceFileClient;
     private EventHandler eventHandler;
     private String logFile;
     private SecurityManager securityManager;
     private Settings settings;
     private final Logger log = LoggerFactory.getLogger(getClass());
     private ArrayBlockingQueue<String> shortLog;
     private List<URL> completedFiles;
     private final String clientID;
     
     public BasicClient(Settings settings, SecurityManager securityManager, String logFile, String clientID) {
         log.debug("---- Basic client instanciating ----");
         this.logFile = logFile;
         changeLogFiles();
         shortLog = new ArrayBlockingQueue<String>(50);
         eventHandler = new BasicEventHandler(logFile, shortLog);
         completedFiles = new CopyOnWriteArrayList<URL>();
         this.settings = settings;
         this.securityManager = securityManager;
         this.clientID = clientID;
        settings.setComponentID(clientID);
         putClient = ModifyComponentFactory.getInstance().retrievePutClient(settings, this.securityManager, clientID);
         getClient = AccessComponentFactory.getInstance().createGetFileClient(settings, this.securityManager, clientID);
         getChecksumClient = AccessComponentFactory.getInstance().createGetChecksumsClient(settings, 
                 this.securityManager, clientID);
         getFileIDsClient = AccessComponentFactory.getInstance().createGetFileIDsClient(settings, 
                 this.securityManager, clientID);
         deleteFileClient = ModifyComponentFactory.getInstance().retrieveDeleteFileClient(settings, 
                 this.securityManager, clientID);
         replaceFileClient = ModifyComponentFactory.getInstance().retrieveReplaceFileClient(settings, 
                 this.securityManager, clientID);
         log.debug("---- Basic client instanciated ----");
 
     }
     
     public void shutdown() {
         putClient.shutdown();
         getClient.shutdown();
         getChecksumClient.shutdown();
         getFileIDsClient.shutdown();
         deleteFileClient.shutdown();
         replaceFileClient.shutdown();
     }
     
     public String putFile(String fileID, long fileSize, String URLStr, String putChecksum, String putChecksumType,
     		String putSalt, String approveChecksumType, String approveSalt) {
         URL url;
         ChecksumDataForFileTYPE checksumDataForNewFile = null;
         if(putChecksum != null) {
             checksumDataForNewFile = makeChecksumData(putChecksum, putChecksumType, putSalt);
         }
         
         ChecksumSpecTYPE checksumRequestForNewFile = null;
         if(approveChecksumType != null) {
             checksumRequestForNewFile = makeChecksumSpec(approveChecksumType, approveSalt);
         }
         try {
             url = new URL(URLStr);
             putClient.putFile(url, fileID, fileSize, checksumDataForNewFile, checksumRequestForNewFile, 
             		eventHandler, "Stuff this Christmas turkey!!");
             return "Placing '" + fileID + "' in Bitrepository :)";
         } catch (MalformedURLException e) {
             return "The string: '" + URLStr + "' is not a valid URL!";
         } catch (OperationFailedException e) {
         	return "";
 		}
     }
     
     public String getFile(String fileID, String URLStr) {
         URL url;
         try {
             url = new URL(URLStr);
             GetFileEventHandler handler = new GetFileEventHandler(url, completedFiles, eventHandler);
             getClient.getFileFromFastestPillar(fileID, url, handler);
             return "Fetching '" + fileID + "' from Bitrepository :)";
         } catch (MalformedURLException e) {
             return "The string: '" + URLStr + "' is not a valid URL!";
         }
     }
     
     public String getLog() {
         File logfile = new File(logFile);
         try {
             FileReader fr = new FileReader(logfile);
             BufferedReader br = new BufferedReader(fr);
             String line;
             StringBuilder result = new StringBuilder();
             while ((line = br.readLine()) != null) {
                 result.append(line + "\n");
             }
             return result.toString();
         } catch (FileNotFoundException e) {
             return "Unable find log file... '" + logfile.getAbsolutePath() + "'";
         } catch (IOException e) {
             return "Unable to read log... '" + logfile.getAbsolutePath() + "'";
         }
     }
     
     public String getHtmlLog() {
         File logfile = new File(logFile);
         try {
             FileReader fr = new FileReader(logfile);
             BufferedReader br = new BufferedReader(fr);
             String line;
             StringBuilder result = new StringBuilder();
             while ((line = br.readLine()) != null) {
                 result.append(line + "<br>");
             }
             return result.toString();
         } catch (FileNotFoundException e) {
             return "Unable find log file... '" + logfile.getAbsolutePath() + "'";
         } catch (IOException e) {
             return "Unable to read log... '" + logfile.getAbsolutePath() + "'";
         }
     }
     
     public String getShortHtmlLog() {
     	StringBuilder sb = new StringBuilder();
     	List<String> entries = new ArrayList<String>();
     	for(String entry : shortLog) {
     		entries.add(entry);
     	}
     	Collections.reverse(entries);
     	for(String entry : entries) {
     		sb.append(entry + "<br>");
     	}
     	
     	return sb.toString();
     }
     
     public String getSettingsSummary() {
         StringBuilder sb = new StringBuilder();
         CollectionSettings collectionSettings = settings.getCollectionSettings();
         sb.append("CollectionID: <i>" + collectionSettings.getCollectionID() + "</i><br>");
         sb.append("Pillar(s) in configuration: <br> <i>");
         List<String> pillarIDs = collectionSettings.getClientSettings().getPillarIDs(); 
         for(String pillarID : pillarIDs) {
         	sb.append("&nbsp;&nbsp;&nbsp; " + pillarID + "<br>");
         }
         sb.append("</i>");
         sb.append("Messagebus URL: <br> &nbsp;&nbsp;&nbsp; <i>"); 
         sb.append(collectionSettings.getProtocolSettings().getMessageBusConfiguration().getURL() + "</i><br>");
         return sb.toString();
     }
     
     public List<String> getPillarList() {
     	return settings.getCollectionSettings().getClientSettings().getPillarIDs();
     }
     
     public Map<String, Map<String, String>> getChecksums(String fileIDsText, String checksumType, String salt) {
         ChecksumSpecTYPE checksumSpecItem = makeChecksumSpec(checksumType, salt);
     	FileIDs fileIDs = new FileIDs();
     	fileIDs.setFileID(fileIDsText);
 
     	GetChecksumsResults results = new GetChecksumsResults();
     	GetChecksumsEventHandler handler = new GetChecksumsEventHandler(results, eventHandler);
     	
     	try {
 			getChecksumClient.getChecksums(settings.getCollectionSettings().getClientSettings().getPillarIDs(),
 					fileIDs, checksumSpecItem, null, handler, "Arf arf, deliver those checksums");
 		} catch (OperationFailedException e1) {
 			return results.getResults();
 		}
         
     	
         try {
             while(!results.isDone() && !results.hasFailed()) {
                 Thread.sleep(500);
             }
         } catch (InterruptedException e) {
             // Uhm, we got aborted, should return error..
         }
     	
     	return results.getResults();
     }
     
     public GetFileIDsResults getFileIDs(String fileIDsText, boolean allFileIDs) {
     	GetFileIDsResults results = new GetFileIDsResults(
     			settings.getCollectionSettings().getClientSettings().getPillarIDs());
     	GetFileIDsEventHandler handler = new GetFileIDsEventHandler(results, eventHandler);
     	FileIDs fileIDs = new FileIDs();
     	
     	if(allFileIDs) {
     		fileIDs.setAllFileIDs(allFileIDs);
     	} else {
     	    fileIDs.setFileID(fileIDsText);
     	}
     	try {
     	    getFileIDsClient.getFileIDs(settings.getCollectionSettings().getClientSettings().getPillarIDs(),
     	            fileIDs, null, handler, "Deliver my fileIDs garrh");
 
     	    while(!results.isDone() && !results.hasFailed()) {
     	        Thread.sleep(500);
     	    }
 		} catch (InterruptedException e) {
 			// Uhm, we got aborted, should return error..
 		} catch (OperationFailedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
     	return results;
     }
     
     public String deleteFile(String fileID, String pillarID, String deleteChecksum, String deleteChecksumType, 
             String deleteChecksumSalt, String approveChecksumType, String approveChecksumSalt) {
         if(fileID == null) {
             return "Missing fileID!";
         }
         if(pillarID == null || !settings.getCollectionSettings().getClientSettings().getPillarIDs().contains(pillarID)) {
             return "Missing or unknown pillarID!";
         }
         if(deleteChecksum == null || deleteChecksum.equals("")) {
             return "Checksum for pillar check is missing";
         }
         if(deleteChecksumType == null || deleteChecksumType.equals("")) {
             return "Checksum type for pillar check is invalid";
         }
         ChecksumDataForFileTYPE verifyingChecksum = makeChecksumData(deleteChecksum, deleteChecksumType, 
         		deleteChecksumSalt);
         ChecksumSpecTYPE requestedChecksumSpec = null;
         log.info("----- Got DeleteFileRequest with approveChecksumtype = " + approveChecksumType);
         if(approveChecksumType != null && !approveChecksumType.equals("disabled")) {
             requestedChecksumSpec = makeChecksumSpec(approveChecksumType, approveChecksumSalt);
         }
         
         
         try {
 			deleteFileClient.deleteFile(fileID, pillarID, verifyingChecksum, requestedChecksumSpec, 
 			        eventHandler, "Kick that file");
 		} catch (OperationFailedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         
         return "Deleting file";
     }
     
     public String replaceFile(String fileID, String pillarID, String oldFileChecksum, String oldFileChecksumType,
     		String oldFileChecksumSalt, String oldFileRequestChecksumType, String oldFileRequestChecksumSalt,
     		String urlStr, long newFileSize, String newFileChecksum, String newFileChecksumType,
     		String newFileChecksumSalt, String newFileRequestChecksumType, String newFileRequestChecksumSalt) {
     	if(fileID == null) {
             return "Missing fileID!";
         }
         if(pillarID == null || !settings.getCollectionSettings().getClientSettings().getPillarIDs().contains(pillarID)) {
             return "Missing or unknown pillarID!";
         }
         if(oldFileChecksum == null || oldFileChecksum.equals("")) {
             return "Checksum for pillar check of old file is missing";
         }
         if(oldFileChecksumType == null || oldFileChecksumType.equals("")) {
             return "Checksum type for pillar check of old file is invalid";
         }
         if(newFileChecksum == null || newFileChecksum.equals("")) {
             return "Checksum for pillar check of new file is missing";
         }
         if(newFileChecksumType == null || newFileChecksumType.equals("")) {
             return "Checksum type for pillar check of new file is invalid";
         }
         if(urlStr == null || urlStr.equals("")) {
         	return "A valid fileaddress is missing.";
         }
         URL url;
         ChecksumDataForFileTYPE oldFileChecksumData = makeChecksumData(oldFileChecksum, oldFileChecksumType,
         		oldFileChecksumSalt);
         ChecksumDataForFileTYPE newFileChecksumData = makeChecksumData(newFileChecksum, newFileChecksumType,
         		newFileChecksumSalt);
         ChecksumSpecTYPE oldFileChecksumRequest = null;
         if(oldFileRequestChecksumType != null && (!oldFileRequestChecksumType.equals("disabled") &&
         		!oldFileRequestChecksumType.trim().equals(""))) {
             oldFileChecksumRequest = makeChecksumSpec(oldFileRequestChecksumType, oldFileRequestChecksumSalt);
         }
         ChecksumSpecTYPE newFileChecksumRequest = null;
         if(newFileRequestChecksumType != null && (!newFileRequestChecksumType.equals("disabled") &&
         		!newFileRequestChecksumType.trim().equals(""))) {
         	newFileChecksumRequest = makeChecksumSpec(newFileRequestChecksumType, newFileRequestChecksumSalt);
         }
         
         try {
         	url = new URL(urlStr);
         	replaceFileClient.replaceFile(fileID, pillarID, oldFileChecksumData, oldFileChecksumRequest, url, 
         			newFileSize, newFileChecksumData, newFileChecksumRequest, eventHandler, "Swap away!");
         } catch (MalformedURLException e) {
             return "The string: '" + urlStr + "' is not a valid URL!";
         } catch (OperationFailedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
         
     	return "Replacing file";
     }
     
     public String getCompletedFiles() {
         StringBuilder sb = new StringBuilder();
         sb.append("<b>Completed files:</b><br>");
         for(URL url : completedFiles) {
             sb.append("<a href=\"" + url.toExternalForm() + "\">" + url.getFile() + "</a> <br>");
         }
         
         return sb.toString();
     }
     
     private void changeLogFiles() {
         File oldLogFile = new File(logFile);
         String date = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
         String newName = logFile + "-" + date;
         System.out.println("Moving old log file to: " + newName);
         oldLogFile.renameTo(new File(newName));
     }
     
     private ChecksumDataForFileTYPE makeChecksumData(String checksum, String checksumType, String checksumSalt) {
         ChecksumDataForFileTYPE checksumData = new ChecksumDataForFileTYPE();
         checksumData.setChecksumValue(HexUtils.stringToByteArray(checksum));
         Date now = new Date();
         checksumData.setCalculationTimestamp(XMLGregorianCalendarConverter.asXMLGregorianCalendar(now));
         checksumData.setChecksumSpec(makeChecksumSpec(checksumType, checksumSalt));
         return checksumData;
     }
     
     private ChecksumSpecTYPE makeChecksumSpec(String checksumType, String checksumSalt) {
         ChecksumSpecTYPE spec = new ChecksumSpecTYPE();
         
         if(checksumType == null || checksumType.trim().equals("")) {
             checksumType = settings.getCollectionSettings().getProtocolSettings().getDefaultChecksumType();
         }
         
         if(checksumSalt != null && !checksumSalt.trim().equals("")) {
             spec.setChecksumSalt(HexUtils.stringToByteArray(checksumSalt));
             checksumType = "HMAC_" + checksumType;
         }
         spec.setChecksumType(ChecksumType.fromValue(checksumType));
         
         return spec;
     }
     
 }
