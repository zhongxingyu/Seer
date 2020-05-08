 package edu.ucsc.barrel.cdf_gen;
 
 /*
 CDF_Gen.java v12.02.28
 
 Description:
    Entry point for .jar file.
    Reads ini file.
    Creates all objects needed for operation. 
    
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    This file is part of The BARREL CDF Generator.
 
    The BARREL CDF Generator is free software: you can redistribute it and/or 
    modify it under the terms of the GNU General Public License as published 
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
 
    The BARREL CDF Generator is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License along with 
    The BARREL CDF Generator.  If not, see <http://www.gnu.org/licenses/>.
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 
 Change Log:
 v13.02.28
    -Mag ID is read from the ini file and passed to LevelTwo.java object.
 
 v13.01.18
    -Added ability to get launch site and launch order from ini file.
    -Keeps track of file revision numnber in the constructor
 	
 v12.11.26
    -Output directory is set in ini file.
    -Output files are sorted into date subdirectories.
    -Sets full output path in the payload loop
    -Calls time extraction code after level zero is created. 
    -Nulls out L1 and L2 objects after use.
    
 v12.11.21
    -Only produces the requested data levels as indicated by command line
    -Changed names of multiple variables all named "data" but in different scopes
    -Calculates and prints throughput after creating Level 0 files.
 
 v12.11.20
    -Changed name to CDF_Gen.java
    
 v12.11.05
    -Now creates instances of DataHolder instead of using it as a static class
    -Added a getter method "getDataSet" to pass the current instance of 
     DataHolder
    -Changed file names to all lowercase
 
 v12.10.11
    -Changed version numbers to a date format
    -Removed function "addHexFrame" and variable "hexDataStr" because they 
    are unused.
    -Creates L2 object
    -Changes the way L1 CDF files are created
    -Moved "Creating Level One" message to LevelOne.java
    -Added functions "copyFile()", "putData()" and "opemCDF()" as public static 
    methods
    -Made directory settings public so they didn't need to be set in each 
    object's constructor
    -putData() does not print "INFO" messages from the cdf status
    -moved CDF type list here
 
 v0.6
    -Changed how output files are stored
    -Changed how Level 1 files are created
    -Changed how the frame data is transfered to Level 1 object
    
 v0.5
    -Created a place to store the hex string the level 1 and 2 
       files will be created with
 
 v0.4
    -Changed how the LevelZero object is created to match new constructor
    
 v0.3
    -Updated class/file name case and added proper package scheme
    -Reads new ini file items
    -Saves ini values to a map rather than individual variables
    -removed many getter/setter programs in favor of the settings map
    -Uses the new constructor for LevelZero and DataCollector classes
       
 v0.2
    -Changed name (previously dataProducts.java)
    -Updated header documentation
    -ini file now supports comments preceeded by a "#"
    -server and payloads listed in the ini file are now one item per line
    
 v0.1
    -Downloads data from servers listed in ini file.
    -Produces level 0 data with no error checking.
 
 Future Plans:
    -Implement level 2 objects
    -Clean up/ Documentation
    -Add logging
 */
 
 import gsfc.nssdc.cdf.CDF;
 import gsfc.nssdc.cdf.CDFConstants;
 import gsfc.nssdc.cdf.CDFException;
 import gsfc.nssdc.cdf.Variable;
 import gsfc.nssdc.cdf.util.CDFTT2000;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.nio.channels.FileChannel;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 public class CDF_Gen implements CDFConstants{
    
    //custom objects
    private static DataHolder data;
    private static DataCollector dataPull;
    private static LevelZero L0;
    
    //private members
    private static ArrayList<String> servers = new ArrayList<String>();
    private static ArrayList<String> payloads = new ArrayList<String>();
    private static Map<String, String> settings = new HashMap<String, String>();
    
    //Directory and file settings
    private static String output_Dir = "out";
    public static String tlm_Dir;
 	public static String L0_Dir;
    public static String L1_Dir;
    public static String L2_Dir;
    public static Logger log;
    
    //List of types of CDF files
    public static String[] fileTypes = 
       {"magn","rcnt","gps-","fspc","mspc","sspc","hkpg","pps-"};
    
    public static DataHolder getDataSet(){return data;}
    
    public static void main(String[] args){
       //array to hold payload id, lauch order, and launch site
 		String[] payload = new String[3];
 		
       //create a log file
       log = new Logger("log.txt");
 
       //ensure there is some user input
       if(args.length == 0){
          System.out.println(
             "Usage: java -jar cdf_gen.jar ini=<ini file> date=<date>"
          );
          System.exit(0);
       }
       
       //read the ini file and command line arguments
       loadConfig(args);
       
 		//for each payload, create an object to download the files,
       // read the list of data files on each server, then download the files
       for(String payload_i : payloads){
 			String
 				date = "000000",
 				id = "00",
 				flt = "00",
 				stn = "0",
 				revNum = "00",
             mag = "0000";
 			
 			//break payload apart into id, flight number and launch station
 			String[] payload_parts = payload_i.split(",");
 			if(payload_parts[0] != null){id = payload_parts[0];}
 			if(payload_parts[1] != null){flt = payload_parts[1];}
 			if(payload_parts[2] != null){stn = payload_parts[2];}
 			if(payload_parts[3] != null){mag = payload_parts[3];}
 			
          //set output paths
          if(getSetting("outDir") != ""){
 			   //check if user specified a place to store the files
 				output_Dir = getSetting("outDir");
 			}
          tlm_Dir = 
             output_Dir + "/tlm/" + id + "/" + getSetting("date") + "/";
          L0_Dir = 
             output_Dir + "/l0/" + id + "/" + getSetting("date") + "/";
          L1_Dir = 
             output_Dir + "/l1/" + id + "/";
          L2_Dir = 
             output_Dir + "/l2/" + id + "/";
          
          //set working payload
          settings.put("currentPayload", payload_i);
          
          //create a new storage object
          data = new DataHolder();
          
          //Figure out where the input files are coming from
          if(getSetting("local") == ""){
             dataPull =
 				   new DataCollector(tlm_Dir, servers, id, settings.get("date"));
             
             //read each repository and build a list of data file URLs
             dataPull.getFileList();
             
             //download each file after the URL list is made
             dataPull.getFiles();
          }else{
             tlm_Dir = getSetting("local");
          }
          
          //Create level zero object and convert the data files to a level 0 file
          try{
             System.out.println("Creating Level Zero...");
             L0 = new LevelZero(
                data,
                Integer.parseInt(settings.get("frameLength")), 
                settings.get("syncWord"),
                tlm_Dir, 
                L0_Dir,
                id,
 					flt,
 					stn,
                getSetting("date")
             );
             L0.processRawFiles();
             L0.finish();
             System.out.println(
                "Completed Level 0 for payload " + getSetting("currentPayload")
             );
          
             //If we didn't get any data, move on to the next payload.
             if(data.getSize("1Hz") > 0){
             
                //calculate throughput value
                System.out.println(
                      "Payload " + getSetting("currentPayload") + 
                      " Throughput: " + (100 * data.getSize("1Hz") - 1) /
    			      (data.frame_1Hz[data.getSize("1Hz") - 1] - 
    			      (data.frame_1Hz[0]))
    			      + " %"
    			   );
             
                //Fill the time variable
                ExtractTiming barrel_time = 
                   new ExtractTiming(getSetting("date"));
                barrel_time = null;
                
                if(getSetting("L").indexOf("1") > -1){
                   //create Level One 
                   LevelOne L1 =
 						   new LevelOne(getSetting("date"), id, flt, stn);
                   L1 = null;
                }
                
                if(getSetting("L").indexOf("2") > -1){
                   //create Level Two
                   LevelTwo L2 =
						   new LevelTwo(getSetting("date"), id, flt, stn, mag);
                   
                   L2 = null;
                }
             }
          }catch(IOException ex){
             System.out.println(
                ex.getMessage()
             );
          }
       }
 
       //close the log file
       log.close();
    }
    
    public static byte[] hexToByte(String s) {
       int len = s.length();
       byte[] bytes = new byte[len / 2];
       for (int i = 0; i < len; i += 2) {
           bytes[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                + Character.digit(s.charAt(i+1), 16));
       }
       return bytes;
   }
    
    private static void loadConfig(String[] args){
 	   String[] setPair;
 	   
 	   //read command line arguments
 	   for(String arg_i : args){
 		   setPair = arg_i.split("=");
 		   settings.put(setPair[0].trim(), setPair[1].trim());
 	   }   
 	   
 	   //load configuration from ini
 	   try{
          FileReader fr = new FileReader(settings.get("ini"));
          BufferedReader iniFile = new BufferedReader(fr);
          
          String line;
          while( (line = iniFile.readLine()) != null){
             
             //split off any comments
         	   setPair = line.split("#");
             line = setPair[0];
             
             //get the key and value pair. Make sure there is only one pair per line
             setPair = line.split("=");
             if(setPair.length == 2){
                //remove leading and trailing whitespace from key and value
             	setPair[0] = setPair[0].trim();
             	setPair[1] = setPair[1].trim();
                
                if(setPair[0].equals("payload")){
                   //Determine what payloads to read
                    payloads.add(setPair[1]);
                }else if(setPair[0].equals("server")){
                   //Add data file servers to the list
                   servers.add(setPair[1]);
                }else{
                   //anything else just add to the settings map 
              	  settings.put(setPair[0], setPair[1]);
              	}
             }
          }
          
          iniFile.close();
       }catch(IOException ex){
          System.out.println(
             "Could not read config file: " + ex.getMessage()
          );
       }
    }
    
    public static String getSetting(String key){
       if(settings.get(key) != null) return settings.get(key);
       else return "";
    }
    
    public static void copyFile(File sourceFile, File destFile, boolean clobber){
       try{
          if(destFile.exists() && !clobber){
             return;
          }
 
          if(!destFile.exists()){
             //create the output directory and file if needed
             new File(destFile.getParent()).mkdirs();
             destFile.createNewFile();
          }
    
          FileChannel source = null;
          FileChannel destination = null;
       
          try{
             source = new FileInputStream(sourceFile).getChannel();
             destination = new FileOutputStream(destFile).getChannel();
             destination.transferFrom(source, 0, source.size());
          }finally {
             if(source != null) {
                source.close();
             }
             if(destination != null) {
                destination.close();
             }
          }
       }catch(IOException ex){
          System.out.println(
             "Could not copy CDF file: "
                + ex.getMessage()
          );
       }
    }
 
    public static CDF openCDF(String fileName){
 
       CDF cdf = null;
       try{
          cdf = CDF.open(fileName);
          
          if (cdf.getStatus() != CDF_OK)
          {
             System.out.print("Error with CDF! ");
             
             if (cdf.getStatus() == CHECKSUM_ERROR){
                System.out.print("Bad checksum!");
             }
             
             if (cdf != null) cdf.close();
             
             System.out.println("");
          }
       }catch(CDFException ex){
          System.out.println(ex.getMessage());
       }
       
       return cdf;
    }
    public static void putData(
       CDF cdf, String targetVar, long record, Object value, long index
    ){
       //make sure we have a valid record number
       if(record == -1L){return;}
       
       //search for variable index
       try{
          Variable var = cdf.getVariable(targetVar);
          
          var.putSingleData(record, new long[] {index}, value);
          
          long status = cdf.getStatus();
          if (status != CDF_OK){
             String statusText = CDF.getStatusText(status);
             if(statusText.indexOf("INFO") == -1){
                System.out.println (
 						"Problem with record " + record + " of " +
 						var.getName() + ": " + statusText
 					);
             }
          }
          
          //if(!found){System.out.println("Could not find " +targetVar);}
       }catch(CDFException ex){
          System.out.println(
             "Error saving CDF data!\n" +
             "Cant save \"" + targetVar + " = " + value.toString() + 
             "\" in record \"" + record + " of " + cdf.getName() + "\": " + 
             ex.getMessage() +
             "\n"
          );
          System.exit(0);
       }
    }
 }
