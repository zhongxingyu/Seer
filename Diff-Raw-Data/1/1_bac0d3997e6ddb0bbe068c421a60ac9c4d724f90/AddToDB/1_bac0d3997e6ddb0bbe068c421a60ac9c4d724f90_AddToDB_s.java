 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 //For forms
 import play.data.*;
 import play.data.Form.*;
 
 //Get scala templates
 import views.html.*;
 //Get objects
 import models.*;
 
 //for testing/file
 import java.io.File;
 import java.util.*;
 
 //For entering data to mysql
 import com.avaje.ebean.*;
 import play.db.*;
 import java.sql.*;
 
 //For regex
 import java.util.regex.*;
 
 public class AddToDB extends Controller{
 	
 	/**
 	 * This function lists all files (not folders) in startFolder and its subdirectories recursively
 	 * @param startFolder A File object that signifies where to start
 	 * @param files The list to append all files to
 	 * Adds all files to supplied List. Does not return it, but list will be altered
 	 */
 	public static void listFilesRecursively (File startFolder, List<String> files){
 		if(!startFolder.isDirectory()){//If startFolder is a file, add it to list
 			files.add(startFolder.toString());
 		}
 		else { //startFolder is a folder
 			for (File folder : startFolder.listFiles()){ //For each folder in subfolder, run this method
 				listFilesRecursively(folder, files);
 			}
 		}
 	}
 	
 	/**
 	 * This function takes a list of file names and inserts it into the database
 	 * @param allFiles is a list of file-name strings that should be added into the DB 
 	 * @return List of strings that corresponds to a log of what happened
 	 */
 	public static List<String> enterIntoDB(List<String> allFiles, String dirname, Long runID){
 		String[] ignoreList = {"Thumbs.db" };//comma seperated list of file names to ignore in the directory
 				
 		List<String> log = new ArrayList<String>();
 		
 		log.add("Log for adding files from "+dirname);
 		log.add("LogLevel___fileName___diffType___RunID____difference____ bugNum");
 		
 		for(String filePath : allFiles){//for each file in List allFiles...
 			//Take out dirname from path
 			filePath=filePath.substring(dirname.length()+1); //and remove beginning \
 			
 			//At this point, filePath is the path to the file after dirname
 			
 			String[] fileAsArray;
 			if(System.getProperty("os.name").startsWith("Windows")){ //If running on windows...
 				fileAsArray = filePath.split("\\\\");//had to escape the backslash
 			}
 			else{ //If running on Unix...
 				fileAsArray = filePath.split("/");
 			}
 			
 			
 			String fileName=getFileName(filePath); //FileName
 			//String[] fileNameArray = fileName.split("\\."); //had to escape again //no longer necessary
 			
 			//If file name is in ignore list
 			if (Arrays.asList(ignoreList).contains(fileName)){
 				continue;//ignore this file and move to the next one
 			}
 			
 			String diffType = getDiffType(fileAsArray[0]);//Store what kind of difference (Better, Neutral, or Worse)
 			//get difference description
 			String difference=null;
 			if(fileAsArray.length>2){
 				difference = getDifference(fileAsArray);//Stores description of difference
 			}
 			//get bug Number
 			Long bugNum=null;
 			if(fileAsArray.length>2){
 				bugNum = getBugNum(fileAsArray);//Stores description of difference
 			}
 			//Insert these values into the database
 			if(diffType!=null && fileName!=null){ //Mandatroy to be inserted
 				log.add(enterFileInfo(fileName, diffType, runID, difference, bugNum)); //Actually insert the data into DB
 			}
 			else{
 				if(diffType==null){
 					log.add("ERROR: Unknown difftype in "+filePath+" ---- Not added");
 				}
 				else{
 					log.add("ERROR: Can't find file name in file: "+filePath+" ---- Not added");
 				}
 			}
 		}
 		
 		//Check and add any files that were not decompressed (files with errors)
 		addFilesWithErrors(dirname, runID, log);
 		
 		return log;
 	}
 	
 	/**
 	 * This method adds any files in the IMAGES NOT DECOMPRESSED directory to the files database and their corresponding error
 	 * @param dirname The Issues to directory to begin at. The search is actually below this folder
 	 * @param runID The run that these files correspond to
 	 * @param log The log to add information to
 	 * @return void
 	 */
 	public static void addFilesWithErrors(String dirname, Long runID, List<String> log){
 		String appendFolder="/IMAGES NOT DECOMPRESSED"; 
 		dirname=dirname.substring(0,dirname.lastIndexOf('/'))+appendFolder;
 		 
 		File startFile = new File(dirname);
 		
 		List<String> errorFiles = new ArrayList<String>();
 		
 		listFilesRecursively(startFile, errorFiles); //allFiles is now a List<String> of all files paths
 		
 		for(String errorFile : errorFiles){//for each file in errorFiles
 			//Get file name
 			String fileName=getFileName(errorFile);
 				//Should i remove .txt?
 			//Get ErrorNum
 			long errorNum = getErrorNum(errorFile,appendFolder);
 			//Get error description
 			String errorDesc = getErrorDesc(errorFile,appendFolder);
 			//Get ID of error Num
 			long errorID = models.Error.getIDByNum(errorNum,errorDesc);
 			//Save this page
 			PageOut newpage = new PageOut();
 			newpage.name=fileName;
 			newpage.run=Run.getRunByID(runID);
 			newpage.error=models.Error.getErrorByID(errorID);
 			newpage.save();
 			//Add log info (if no error) - If error occured, dealt with in sqlUpdate method
 			log.add("INFO: Page '"+fileName+"' added with error number="+errorNum+" and description: "+errorDesc);
 		}
 	}
 	
 	
 	/**
 	 * This method executes a mySQL UPDATE statement
 	 * @param SQLStatement The statemnt as a string that should be run
 	 * @param A first id to insert
 	 * @param B second id to insert
 	 * @return nothing
 	 */
 	public static void sqlUpdate(String SQLStatement, long A, long B){
 		//Start connection
 		Connection connection = DB.getConnection();
 		
 		try{
 			//Create new statement			
 			PreparedStatement stmt = connection.prepareStatement(SQLStatement,Statement.RETURN_GENERATED_KEYS);
 			//Add ids to query
 			stmt.setLong(1,A);
 			stmt.setLong(2,B);
 			//Run query
 			stmt.executeUpdate();
 			//Close statement
 			stmt.close();
 		}
 		catch (SQLException e){
 			e.printStackTrace();
 		}
 		
 		//Close connection
 		try{
 			connection.close();
 		}
 		catch (SQLException e){
 			e.printStackTrace();
 		}
 	}
 	 
 	/**
 	 * This method takes file info as input, inputs them into the DB, and returns a string with ther results
 	 * @param fileName Name of file to enter into DB
 	 * @param diffType The kind of difference
 	 * @param runID The runID that corresponds to this run
 	 * @param difference String describing the difference
 	 * @param bugNum The bug number corresponding to 
 	 * @return Log messege if there was no error
 	 */
 	public static String enterFileInfo(String fileName, String diffType, Long runID, String difference, Long bugNum){
 		//For log info
 		String returnInfo="";
 		
 		long pageID=-1L; //will store ID for page here.
 		//Get page_ID, create one if deosn't already exist
 		pageID=PageOut.getPageID(fileName,runID);
 		if(!PageOut.testPageExists(fileName,runID)){//if page has not been added before
 			//Begin log info for this file
 			returnInfo+="---Log Info for "+fileName+"---";
 			
 			returnInfo+="INFO: "+fileName+" added to DB with runID="+runID;
 		}//End if?
 		
 		//Deal with difference and pagetodifference
 		long diffID=0;
 		//get ID of difference
 		diffID = Difference.getDifferenceID(difference,diffType);
 		if(diffType!=null){ //
 			if(difference==null){//If difference is not set, set it to this default value
 				difference="No description";
 			}		
 			if(!PageOut.testPageDiffExists(fileName,runID,diffID)){ //if pagetodifference does not already exist...
 				//Prepare SQL statement
 				String SQLpagetodiff="INSERT INTO pagetodifference (Page_ID,Difference_ID) VALUES (?,?)";
 				//Run SQL statement
 				sqlUpdate(SQLpagetodiff, pageID, diffID);
 				//Add log info (if no error) - If error occured, dealt with in sqlUpdate method
 				returnInfo+=" INFO: "+fileName+" linked with difference: "+difference+" of difference type "+diffType;
 
 			}
 		}
 		
 		//Deal with bug and pagetobug
 		long bugID=0;
 		if(diffType.equals("Worse")){
 			//Get id of bug Num
 			bugID = Bug.getBugID(bugNum,Difference.getByID(diffID));
 			if(!PageOut.testPageBugExists(fileName,runID,bugID)){ //if page to bug does not already exist....
 				//Prepare SQL statement
 				String SQLpagetobug="INSERT INTO pagetobug (Page_ID,Bug_ID) VALUES (?,?)";
 				//Run SQL statement
 				sqlUpdate(SQLpagetobug, pageID, bugID);
 				//Add log info (if no error) - If error occured, dealt with in sqlUpdate method
 				returnInfo+=" INFO: "+fileName+" linked with bug: "+bugNum+" and difference: "+difference;
 			}
 		}
 		return returnInfo; //return log
 	}
 	
 	/**
 	 * This method gets the file name of a given filePath
 	 * @param filePath A string similar to "Difftype\?Difference\?BugNum\FileName"
 	 * @return FileName
 	 */
 	public static String getFileName(String filePath){
 		String[] fileAsArray;
 		if(System.getProperty("os.name").startsWith("Windows")){ //If running on windows...
 			fileAsArray = filePath.split("\\\\");//had to escape the backslash
 		}
 		else{ //If running on Unix...
 			fileAsArray = filePath.split("/");
 		}
 		String fileName=fileAsArray[fileAsArray.length-1];
 		
 		fileName=fileName.replaceAll(".(ref|new|dif).",""); //Take out .1ref,.2new,.3dif....
 		
 		if(fileName.contains(".")){
 			return fileName;
 		}
 		else{
 			return null;//No filename found
 		}
 	}
 	
 	/**
 	 * This method gets the difference Type of a given filePath
 	 * @param filePath A string similar to "Difftype"
 	 * @return "Better", "Neutral", "Worse", or error
 	 */
 	public static String getDiffType(String filePath){
 		if(filePath.toLowerCase().contains("better")){
 			return "Better";
 		}
 		else if(filePath.toLowerCase().contains("worse")){
 			return "Worse";
 		}
 		else if(filePath.toLowerCase().contains("neutral") || filePath.toLowerCase().contains("different")){
 			return "Neutral";
 		}
 		else{
 			return null;//Difftype not found 
 		}
 	}
 	
 	/**
 	 * This method gets the difference description of filePath
 	 * @param filePath A string similar to "Difftype\?Difference\?BugNum\FileName"
 	 * @return String that describes the difference
 	 */
 	public static String getDifference(String[] fileAsArray){
 		if(fileAsArray[1].matches("[A-z -]*")){
 			return fileAsArray[1];
 		}
 		else if(fileAsArray[2].matches("[A-z -]*")){
 			return fileAsArray[2];
 		}
 		else{
 			return null;
 		}
 	}
 	
 	/**
 	 * This method gets the difference description of filePath
 	 * @param filePath A string similar to "Difftype\?Difference\?BugNum\FileName"
 	 * @return String that describes the difference
 	 */
 	public static Long getBugNum(String[] fileAsArray){
 		
 	
 		if(fileAsArray[1].matches("[0-9]{1,5}")){
 			return Long.parseLong(fileAsArray[1]);
 		}
 		else if(fileAsArray[2].matches("[0-9]{1,5}")){
 			return Long.parseLong(fileAsArray[2]);
 		}
 		else{
 			return null;
 		}
 	}
 	
 	/**
 	 * This method gets the errorNum of errorFile
 	 * @param errorFile The file to get the error number of
 	 * @param appendFolder Name of folder that the errors are in (ex. IMAGES NOT DECOMPRESSED)
 	 * @return Long for that error
 	 */
 	public static Long getErrorNum(String errorFile, String appendFolder){
 		if(System.getProperty("os.name").startsWith("Windows")){ //If running on windows...
 			//Change \ to /
 			appendFolder = appendFolder.replace("/","\\");
 		}
 		//Subtract everything before end of appendFolder
 		//use lastindex of appendFolder and add the length of append folder
 		String file = errorFile.substring(errorFile.lastIndexOf(appendFolder)+appendFolder.length()+1);
 		//Expand name through .
 		String errorNum = file.substring(0,file.indexOf("."));
 		//if error num is present...
 		if(System.getProperty("os.name").startsWith("Windows")){ //If running on windows...
 			if(file.indexOf(".") < file.indexOf("\\")){
 				if(errorNum.matches("\\d*")){//to prevent any errors
 					return Long.valueOf(errorNum);
 				}
 				else{
 					return Long.valueOf(errorNum);
 				}
 			}
 			else{
 				return 0L;
 			}
 		}else{
 			if(file.indexOf(".") < file.indexOf("/")){
 				if(errorNum.matches("\\d*")){//to prevent any errors
 					return Long.valueOf(errorNum);
 				}
 				else{
 					return Long.valueOf(errorNum);
 				}
 			}
 			else{
 				return 0L;
 			}
 		}
 	}
 	 /**
 	 * This method gets the error description of errorFile
 	 * @param errorFile The file to get the error description of
 	 * @param appendFolder Name of folder that the errors are in (ex. IMAGES NOT DECOMPRESSED)
 	 * @return String describing that error
 	 */
 	public static String getErrorDesc(String errorFile, String appendFolder){
 		if(System.getProperty("os.name").startsWith("Windows")){ //If running on windows...
 			//Change \ to /
 			appendFolder = appendFolder.replace("/","\\");
 		}
 		//Subtract everything before end of appendFolder
 		//use lastindex of appendFolder and add the length of append folder
 		String file = errorFile.substring(errorFile.lastIndexOf(appendFolder)+appendFolder.length()+1);
 		
 		
 		if(System.getProperty("os.name").startsWith("Windows")){//if error num is present...
 			if(file.indexOf(".") < file.indexOf("\\")){
 				//Get whats between "." and "/"
 				return file.substring(file.indexOf(".")+1,file.indexOf("\\"));
 			}
 			else{
 				//Get file name
 				return file.substring(0,file.indexOf("\\"));
 			}
 		}else{
 			if(file.indexOf(".") < file.indexOf("/")){
 				//Get whats between "." and "/"
 				return file.substring(file.indexOf(".")+1,file.indexOf("/"));
 			}
 			else{
 				//Get file name
 				return file.substring(0,file.indexOf("/"));
 			}
 		}
 	}
 	
 	
 	
 	//send List to /test
 	public static Result addFilesFromRun(String dirname, Long runID){
 		File startFile = new File(dirname);
 		
 		List<String> allFiles = new ArrayList<String>();
 		
 		listFilesRecursively(startFile, allFiles); //allFiles is now a List<String> of all files paths
 		
 		return ok(
 			logInfo.render(
 				enterIntoDB(allFiles, dirname, runID), runID
 			)
 		);
 	}
 	
 	/**
      * Handle the 'new run form' submission 
      */
     public static Result saveRun() {
         Form<Run> runForm = Form.form(Run.class).bindFromRequest();//Get from info from POST
 		
 		 if(runForm.hasErrors()) { //doesn't do much...
             return badRequest(importRun.render(runForm));
         }
 		
 		String folderPath="";
 		String pathForPlatform=runForm.get().path;
 		if(System.getProperty("os.name").startsWith("Windows")){ //If running on windows...
 			folderPath = runForm.get().path;
 		}
 		else{ //If running on Unix...
 			folderPath = runForm.get().path.replace("\\\\","/mnt/").replace("\\","/");
 		}
 		File folder = new File(folderPath); //for checking if it exists
 		
 		//if path is invalid, log it and don't create run
 		if(folderPath==null || !folder.exists()){
 			List<String> log = new ArrayList<String>();
 			log.add("ERROR: Invalid please set Path to Issues Folder");
 			log.add("Path = "+folderPath+" is invalid or non-existant");
 			return badRequest(
 				logInfo.render(log, -1L)
 			);
 		}
 		
 		//Get foreign keys for all necessary fields
 		String query = "INSERT INTO run (Run_Name,Version_ID,Format_ID,Date_ID,SVN_ID,Performance_ID) VALUES (?,?,?,?,?,?)";
 		long runID=-1L;
 		
 		ResultSet generatedKeys = null;
 		
 		Platform platform = Platform.getPlatformFromPath(pathForPlatform);
 		
 		//Add run into DB
 		try{
 			//Start connection
 			Connection connection = DB.getConnection();
 			//Create new prepared statement
 			PreparedStatement stmt = connection.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
 			//Add data to prepared statement
 			stmt.setString(1,runForm.get().name);
			System.out.println(runForm.get().version.name+" "+runForm.get().version.id);
 			stmt.setLong(2,Version.getVersionID(runForm.get().version.name, platform));
 			stmt.setLong(3,runForm.get().format.id);
 			stmt.setLong(4,models.Date.getDateID(runForm.get().date.name));
 			if(runForm.get().svn.num == null){
 				stmt.setNull(5, java.sql.Types.INTEGER);
 			}
 			else{
 				stmt.setLong(5,SVN.getSvnID(runForm.get().svn.num));
 			}
 			stmt.setNull(6, java.sql.Types.INTEGER); //set performance to null for now
 			//stmt.setLong(6,Performance.getPerformanceID(runForm.get().performance.time));
 			//Run query
 			int affectedRows = stmt.executeUpdate();
 			if(affectedRows == 0){
 				throw new SQLException("Creating page failed, no rows affected.");
 			}
 			//Get ID
 			generatedKeys = stmt.getGeneratedKeys();
 			if (generatedKeys.next()) {
 				runID=(generatedKeys.getLong(1)); //store genereated ID for run as runID
 			} else {
 				throw new SQLException("Creating page failed, no generated key obtained.");
 			}
 			
 			//Close statement
 			generatedKeys.close();
 			stmt.close();
 			connection.close();
 		}
 		catch (SQLException e){
 			e.printStackTrace();
 		}
 		
 		if(runID!=-1L){//Path to issues folder given
 			//Change path to / slashes. Run method
 			String dirname = runForm.get().path.replace("\\","/");
 			
 			if(dirname.charAt(dirname.length()-1)=='/'){//folder path ends in /
 				//Remove "/"
 				dirname=dirname.substring(0,dirname.length()-1);
 			}
 			
 			return addFilesFromRun(dirname, runID); //Run method to add pages toDB from run info
 		}
 		else{//For errors
 			List<String> log = new ArrayList<String>();
 			if(runID==-1L){
 				log.add("ERROR: Error creating run and getting its ID");
 			}
 			else{
 				log.add("ERROR: Unknown error occured before files could be added");
 			}
 			return ok(
 				logInfo.render(log,runID)
 				);
 		}
     }
 }
