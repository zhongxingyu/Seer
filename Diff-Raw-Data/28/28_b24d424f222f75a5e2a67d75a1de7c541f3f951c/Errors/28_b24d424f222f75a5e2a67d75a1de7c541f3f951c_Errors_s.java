 package main;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 
 import javax.swing.JOptionPane;
 
 public abstract class Errors {
 	
 	public static MultiDB f;
     public static Logger logger = Logger.getLogger("MultiDBlog");
 	public static Map<Integer, String> map= new HashMap<Integer,String>();
 	//Codes for definition of errors
 	//Database errors
 	public static final Integer LOADING_DB_DRIVER =-1;
     public static final Integer OPENING_DB =-2;
     public static final Integer CLOSING_DB =-3;
     public static final Integer DB_SELECT =-4;
     public static final Integer DB_INSERT =-5;
     public static final Integer DB_DELETE =-6;
     public static final Integer DB_UPDATE =-7;
     public static final Integer DB_UNDEFINED = -8;
     public static final Integer DB_BUP = -9;
     public static final Integer DB_RESTORE = -10;
     
     //WEB READER ERRORS
     public static final Integer UPLOADING_BUP=-11;
     public static final Integer GROUP_NOT_FOUND=-12;
     public static final Integer MOVIE_NOT_FOUND=-13;
     
     //FILE ERRORS
     public static final Integer COPYING_FILE_EXISTS=-14;
     public static final Integer FILE_NOT_FOUND=-15;
     public static final Integer COPYING_IOERROR=-16;
     public static final Integer WRITING_IOERROR=-17;
     
     //ERROR SAVING REVIEW
     public static final Integer SAVING_REVIEW=-18;
     
    //ERROR FOR PRINTING STACKTRACE
    public static final Integer GENERIC_STACK_TRACE = -19;
     
     //FORMAT ERRORS
    public static final Integer NEGATIVE_NUMBER=-20;
     
 	//Strings for definition of errors
     public static final String SEE_LOG = "See log for details";
     public static final String ERROR_UNDEF="Undefined error";
         
     public static void setErrors(){
     	map.put(LOADING_DB_DRIVER,"Error loading database driver");
     	map.put(OPENING_DB,"Error opening database");
     	map.put(CLOSING_DB,"Error closing database");
     	map.put(DB_SELECT,"Error on select sentence on database");
     	map.put(DB_INSERT,"Error on insert sentence on database");
     	map.put(DB_DELETE,"Error on delete sentence on database");
     	map.put(DB_UPDATE,"Error on update sentence on database");
     	map.put(DB_UNDEFINED,"Error on database. "+SEE_LOG);
     	map.put(DB_BUP,"Error while making backup of database");
     	map.put(DB_RESTORE,"Error while restoring backup of database");
     	map.put(UPLOADING_BUP,"Error while uploading backup to web");
     	map.put(GROUP_NOT_FOUND,"Error while looking for band name on web");
     	map.put(MOVIE_NOT_FOUND,"Film not found");
     	map.put(COPYING_FILE_EXISTS,"Error while copying file, file already exists");
     	map.put(FILE_NOT_FOUND,"Error, file not found");
     	map.put(COPYING_IOERROR,"Error copying file, IOError");
     	map.put(WRITING_IOERROR,"Error writing to file, IOError");
     	map.put(SAVING_REVIEW,"Error saving review");
    	map.put(GENERIC_STACK_TRACE,"Error, see log for details");
    	map.put(NEGATIVE_NUMBER,"Cannot work with negative numbers");    	
     }
     
     //without logging message
 	public static void showError(int code){
 		if (map.containsKey(code)) {
 			JOptionPane.showMessageDialog(f, map.get(code));
 			logger.log(Level.SEVERE,map.get(code));
 		}
 		else {
 			JOptionPane.showMessageDialog(f, ERROR_UNDEF);
 			logger.log(Level.SEVERE,ERROR_UNDEF);
 		}
 	}
 	
 	//with logging message
 	public static void showError(int code,String message){
 		if (map.containsKey(code)) {
 			JOptionPane.showMessageDialog(f, map.get(code));
 			logger.log(Level.SEVERE,map.get(code)+"\n"+message);
 		}			
 		else {
 			JOptionPane.showMessageDialog(f, ERROR_UNDEF);
 			logger.log(Level.SEVERE,message);
 		}		
 	}
 	
 	//with logging message but no alert window
 	public static void writeError(int code,String message){
 		if (map.containsKey(code)) {
 			logger.log(Level.SEVERE,map.get(code)+"\n"+message);
 		}			
 		else {
 			logger.log(Level.SEVERE,message);
 		}		
 	}
 	
 	//WARNINGS, doesn't log
 	//directory warning
 	public static void errorDir(String dir) {
 	    // the following statement is used to log any messages   
         //logger.log(Level.WARNING,"El directorio especificado no es correcto: " + dir);
 		JOptionPane.showMessageDialog(f,"Specified directory is wrong: " + dir);
 	}
 
 	//syntax warning
 	public static void errorSint(String dir) {
         //logger.log(Level.WARNING,"El directorio no responde a las especificaciones habituales: "+ dir);
 		f.reviewView.append("Syntax error, directory wrong: "+ dir + "\n");
 	}
 
 
 ///////////////////////////////////////////////////////////////////////////////////
 //////////////////////SETUP LOGGING/////////////////////////////////////////////////////////////
 ///////////////////////////////////////////////////////////////////////////////////
 
 
 	public static void setLogging(){
 		String dirName="";
 		FileHandler fh;
 		String fileName = "multidb.log";
 		
 		String os = System.getProperty("os.name").toLowerCase();
 		if((os.indexOf( "win" ) >= 0)){
 			if (MultiDB.logPath!=null) dirName=MultiDB.logPath; 
 			else{
 				String appPath = System.getProperties().getProperty("user.dir"); 
 				dirName=appPath+File.separator+fileName;
 				}
 		}else if(os.indexOf( "nix") >=0 || os.indexOf( "nux") >=0){
 			dirName="/var/log/multidb.log";
 		}else { return;}
 		
 		try {
 		
 			// This block configure the logger with handler and formatter
 			fh = new FileHandler(dirName, true);
 			logger.addHandler(fh);
 			logger.setLevel(Level.ALL);
 			SimpleFormatter formatter = new SimpleFormatter();
 			fh.setFormatter(formatter);
 			
 			} catch (SecurityException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		
 		
 	}
 	
 	public void log(Level level,String message){
 		logger.log(level,message);
 	}
 	
 	
 }
