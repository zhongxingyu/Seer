 package dk.frv.eavdam.utils;
 
 import dk.frv.eavdam.data.AISBaseAndReceiverStationFATDMAChannel;
 import dk.frv.eavdam.data.AISDatalinkCheckIssue;
 import dk.frv.eavdam.data.AISDatalinkCheckResult;
 import dk.frv.eavdam.data.AISFixedStationData;
 import dk.frv.eavdam.data.AISSlotMap;
 import dk.frv.eavdam.data.ActiveStation;
 import dk.frv.eavdam.data.EAVDAMData;
 import dk.frv.eavdam.data.EAVDAMUser;
 import dk.frv.eavdam.data.FATDMAReservation;
 import dk.frv.eavdam.data.FTP;
 import dk.frv.eavdam.data.Options;
 import dk.frv.eavdam.data.Simulation;
 import dk.frv.eavdam.io.FTPHandler;
 import dk.frv.eavdam.io.XMLExporter;
 import dk.frv.eavdam.io.XMLImporter;
import dk.frv.eavdam.layers.StationLayer;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.Calendar;
 import dk.frv.eavdam.io.derby.DerbyDBInterface;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.List;
 import org.apache.commons.net.ftp.FTPClient;
 
 public class DBHandler {
         
     private static EAVDAMData data = null;  // for testing before the database works        
     private static boolean initialized = false;    
     private static boolean updatedXML = false;
 	public static boolean changes = false;
     
 	public static boolean importDataUpdated = false;
     
     public static EAVDAMData getData() {        
 
 //    	System.out.println("Getting data from database!");
 //    	if(data != null && !changes) return data;
     	
     	EAVDAMData dat = new EAVDAMData();
     	if(!initialized){
     		//Check if the database exists. It does the check in the constructor.
     		DerbyDBInterface d = new DerbyDBInterface();
 	
 			new LoadXMLsFromFTPsThread().start();
 	
 	
     		d.closeConnection();
     		
     		dat = XMLHandler.importData();
     		initialized = true;
     	}else{
     	
 	    	try {
 			
				if (importDataUpdated && StationLayer.windowReady) {
 				    dat = XMLHandler.importData();
 					importDataUpdated = false;
 					
 				} else {
 			
 					DerbyDBInterface d = new DerbyDBInterface();
 					EAVDAMUser user = d.retrieveDefaultUser();  
 					if(user != null){
 						System.out.println("Retrieved default user: "+user.getOrganizationName());
 					
 					}
 					
 					dat = d.retrieveAllEAVDAMData(user);
 				
 				}
 	        	
 	            //Test prints
 //	            for(ActiveStation a : dat.getActiveStations()){
 //	            	
 //	            	for(AISFixedStationData f : a.getStations()){
 //	            		System.out.println("Station "+(f.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED ? "Planned" : "Operative"));
 //	            		if(f.getFATDMAChannelA() != null)
 //	            			System.out.println("A "+((AISBaseAndReceiverStationFATDMAChannel)f.getFATDMAChannelA()).getFATDMAScheme().get(0).getOwnership());
 //	            		if(f.getFATDMAChannelB() != null)
 //	            			System.out.println("B "+((AISBaseAndReceiverStationFATDMAChannel)f.getFATDMAChannelB()).getFATDMAScheme().size());
 //	            	}
 //	            }
 	                    
 	                
 	
 	    	} catch (Exception e) {
 	    		e.printStackTrace();
 	//    		System.out.println(e.getMessage());
 	    	}
 
     	}
             
 //    	try{
 //	    	HealthCheckHandler hch = new HealthCheckHandler(dat);
 //	    	
 //	    	double[] point = {60,24.15};
 //	    	AISSlotMap res = hch.slotMapAtPoint(point[0], point[1]);
 //	    	
 //	    	if(res != null){
 //	    		try{
 //	    			System.out.println("Stations found with coverage at (60;24.15)!");
 //	    			if(res.getAIS1Timeslots() != null && res.getAIS1Timeslots().size() > 0){
 //	    				System.out.println("\tReservation: "+res.getBandwidthReservation());
 //	    				System.out.println("\t"+res.getAIS1Timeslots().get(101).toString());
 //	    			}
 //	    				
 //	    			
 //	    		}catch(Exception e){
 //	    			e.printStackTrace();
 //	    		}
 //	    	}else{
 //	    		System.out.println("\tNo stations found with coverage at (60;24.15)");
 //	    	}
 //    	}catch(Exception e){
 //    		e.printStackTrace();
 //    	}
     		
     	data = dat;
     	changes = false;
     	
         return dat;
 
     }
 
     public static EAVDAMData getData(double topLeftLat, double topLeftLon, double lowRightLat, double lowRightLon){
     	EAVDAMData dat = null;
     	
     	try {
     		DerbyDBInterface d = new DerbyDBInterface();
             EAVDAMUser user = d.retrieveDefaultUser();  
             if(user != null){
             	System.out.println("Retrieved default user: "+user.getOrganizationName());
             
             }
             
         	dat = d.retrieveAllEAVDAMData(user, topLeftLat, topLeftLon, lowRightLat, lowRightLon);
         	
                   
                 
 
     	} catch (Exception e) {
     		e.printStackTrace();
     	}
 
     	
     	
     	return dat;
     }
     
     public static void saveData(EAVDAMData data) {
         DBHandler.data = data;   // for testing before the database works
         DerbyDBInterface d = new DerbyDBInterface();
         //d.createDatabase(null);        
         
         
 		d.insertEAVDAMData(data);
 
 		changes = true;
 
 //        if(!updatedXML){
 //			System.out.println("Writing the xml to file...");
 //			try {
 //
 //				XMLHandler.exportData();
 //	    		System.out.println("Writing finished!");
 //			} catch (Exception e) {
 //				e.printStackTrace();
 //			}
 //			
 //			updatedXML = true;
 //        }
     }
     
     public static void saveUserData(EAVDAMUser user, boolean defaultUser){
     	DerbyDBInterface d = new DerbyDBInterface();
         //d.createDatabase(null);
     	try{
     		int id = d.insertEAVDAMUser(user, defaultUser);
 //    		System.out.println("Added user under id "+id+" (default: "+defaultUser+")");
     		changes = true;
     	}catch(Exception e){
     		e.printStackTrace();
     	}
     }
     
     /**
      * Retrieves the options from the database..
      * 
      * @return
      */
     public static Options getOptions() {
 		Options op = new Options();
     	
 		DerbyDBInterface db = new DerbyDBInterface();
 		
     	op = db.getOptions();
 		
 		
 		return op;
 	}
     		
     public static void saveOptions(Options options){
     	DerbyDBInterface db = new DerbyDBInterface();
     	db.insertOptions(options);
     	changes = true;
     }
     
     public static void deleteSimulation(String simulationName){
     	try{
     		DerbyDBInterface db = new DerbyDBInterface();
     		db.deleteSimulation(simulationName);
     		changes = true;
     	}catch (Exception e) {
 			e.printStackTrace();
 		}
     }
     
     public static void deleteStation(int stationID){
     	try{
     		DerbyDBInterface db = new DerbyDBInterface();
     		db.deleteStation(stationID);
     		changes = true;
     	}catch (Exception e) {
 			e.printStackTrace();
 		}
     }
     
     public static void acknowledgeIssues(List<AISDatalinkCheckIssue> issues){
     	
     	try{
     		DerbyDBInterface db = new DerbyDBInterface();
     		db.acknowledgeIssues(issues);
     		changes = true;
     	}catch(Exception e){
     		e.printStackTrace();
     	}
     	
     }
     
     
     public static void deleteIssues(List<AISDatalinkCheckIssue> issues){
     	if(true) return;
     	
     	try{
     		DerbyDBInterface db = new DerbyDBInterface();
     		db.deleteIssues(issues);
     		changes = true;
     	}catch(Exception e){
     		e.printStackTrace();
     	}
     	
     }
 }
 
 
 // loads other users xmls from ftps
 class LoadXMLsFromFTPsThread extends Thread {
 		
 	LoadXMLsFromFTPsThread() {}
 	
 	public void run() {
 		try {
 			Options options = DBHandler.getOptions();
			String ownFileName = XMLHandler.getLatestDataFileName();			
 			if (ownFileName != null && ownFileName.indexOf("/") != -1) {
 				ownFileName = ownFileName.substring(ownFileName.lastIndexOf("/")+1);
			} else {
				XMLHandler.exportData();
				ownFileName = XMLHandler.getLatestDataFileName();
			}
 			List<FTP> ftps = options.getFTPs();
 			if (ftps != null && !ftps.isEmpty()) {
 				for (FTP ftp : ftps) {
 					try {
 						FTPClient ftpClient = FTPHandler.connect(ftp);
 						FTPHandler.importDataFromFTP(ftpClient, XMLHandler.importDataFolder, ownFileName);                                       
 						FTPHandler.disconnect(ftpClient);
 					} catch (IOException ex) {
 						System.out.println(ex.getMessage());
 						ex.printStackTrace();
 					}
 				}
 			}	
 	    } catch (Exception e) {
 	    	e.printStackTrace();
 	//  	System.out.println(e.getMessage());
 	    }
 		DBHandler.importDataUpdated = true;		
 		
 		DBHandler.changes = true;
 	}
 
 }
         
