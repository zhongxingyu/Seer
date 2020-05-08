 package edu.cmu.scripting;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 
 import org.slf4j.Logger;
 
 import com.google.gson.Gson;
 
 import edu.cmu.logger.EmulatorLogger;
 
 /**
  * A class used to find and load user scripts that reside
  * in ./BWT_SCRIPTS folder
  * @author ziw
  *
  */
 public class ScriptLoader {
 
 	private static final String SCRIPT_FOLDER_PATH = "./BWT_SCRIPTS";
 	private static final String SCRIPT_EXTENSION = ".bwt";
 	private static final String SAMPLE_SCRIPT_ASSET_PATH = "assets/sample_script.bwt";
 	private static final String SAMPLE_SCRIPT_OUTPUT_PATH = SCRIPT_FOLDER_PATH+"/sample_script.bwt";
 
 	private ScriptLoader(){}//suppress direct instantiation
 	
 	/**
 	 * Check if the BWT_SCRIPTS folder exists. If not, create the folder to hold
 	 * all user scripts.
 	 */
 	public static void checkAndCreateFolder(){
 		File f = new File(SCRIPT_FOLDER_PATH);
 		Logger logger = EmulatorLogger.getEmulatorInfoLogger();
 		if(!f.exists() ||  !f.isDirectory()){
 			logger.info("Script folder doesn't exist. Creating the folder now.");
 			f.mkdir();
 		}
 	}
 	
 	/**
 	 * Check if sample_script.bwt exists. If not, create the sample script.
 	 */
 	public static void createSampleScript(){
 		Logger logger = EmulatorLogger.getEmulatorInfoLogger();
 		if((new File(SAMPLE_SCRIPT_OUTPUT_PATH)).exists()){
 			logger.info("sample_script.bwt already exists. Skip creating sample script.");
 			return;
 		}
 		logger.info("Creating sample script");
 		InputStream stream = ScriptLoader.class.getClassLoader().getResourceAsStream(SAMPLE_SCRIPT_ASSET_PATH);
 		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
 		String line = "";
 		PrintWriter writer = null;
 		try {
 			writer = new PrintWriter(SAMPLE_SCRIPT_OUTPUT_PATH, "UTF-8");
 			while( (line = reader.readLine())!=null){
 				writer.println(line);
 			}
			reader.close();
 		} catch (IOException e) {
 			logger.error("IO exception occurred when creating sample script.");
 			EmulatorLogger.logException(logger,e);
 		}
 		finally{
 			if(writer !=null)
 				writer.close();
 		}
 	}
 	
 	
 	/**
 	 * Return a JSON string representing an array of files names 
 	 * in ./BWT_SCRIPTS folder that end in .bwt
 	 * @return
 	 */
 	public static String getAllScriptNames(){
 		 Logger logger = EmulatorLogger.getEmulatorInfoLogger();
 
 		File f = new File(SCRIPT_FOLDER_PATH);
 		if(!f.exists() ||  !f.isDirectory()){
 			logger.info("Script folder doesn't exist. Creating the folder now.");
 			f.mkdir();
 		}
 		
 		String[] allScripts = f.list(new FilenameFilter(){
 
 			@Override
 			public boolean accept(File dir, String name) {
 				return name!= null && name.endsWith(SCRIPT_EXTENSION);
 			}
 			
 		});
 		Gson g = new Gson();
 		return g.toJson(allScripts);
 	}
 	
 	/**
 	 * Load and compile the script with the given name.
 	 * Return a JSON string representing the result of the compilation. 
 	 * See {@link ScriptParser#parseScript}
 	 * @param scriptName
 	 * @return
 	 */
 	public static String loadScript(String scriptName){
 		return ScriptParser.parseScript(SCRIPT_FOLDER_PATH+"/"+scriptName);
 	}	
 	
 	
 	
 }
 
