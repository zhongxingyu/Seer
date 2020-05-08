 package org.amplafi.flow.utils;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.io.*;  
 import org.apache.commons.cli.ParseException;
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import static org.amplafai.dsl.ScriptRunner.*;
 import static org.amplafi.flow.utils.AdminToolCommandLineOptions.*;
 import org.amplafai.dsl.ScriptRunner;
 import org.amplafai.dsl.ScriptDescription;
 import static org.amplafai.dsl.ScriptRunner.*;
 import  java.util.prefs.*;
 
 
 public class AdminTool{
 
 	public static final String COMMAND_SCRIPT_PATH = "src/main/resources/commandScripts";
 
 
 	public static void main(String[] args){
 		System.out.println("AdminTool");
 		AdminToolCommandLineOptions cmdOptions = null;
 		try {
 			cmdOptions = new AdminToolCommandLineOptions(args);
 		} catch (ParseException e) {
 			System.err.println("Could not parse passed arguments, message:" + e.getMessage());
 			e.printStackTrace();
 			System.exit(1);
 		}
 		
		if (cmdOptions.hasOption(HELP) || args.length == 0) {
 			cmdOptions.printHelp();
 			
 			System.exit(0);
 		}
 
 		
 		
 		String list = cmdOptions.getOptionValue(LIST);
 				ScriptRunner runner  = new ScriptRunner("");
 
 		List<ScriptDescription>  descs = runner.describeScriptsInFolder(COMMAND_SCRIPT_PATH);
 		
 		
 		List<ScriptDescription> haveErrors = new ArrayList<ScriptDescription>();
 		List<ScriptDescription> goodScripts = new ArrayList<ScriptDescription>();
 		HashMap<String,ScriptDescription> scriptLookup = new HashMap<String,ScriptDescription>();
 		
 		for (ScriptDescription sd : descs ){
 			if (!sd.getHasErrors()){
 				goodScripts.add(sd);
 				scriptLookup.put(sd.getName(),sd);
 			} else {
 				haveErrors.add(sd);
 			}
 		}
 		
 
 		
 		if (cmdOptions.hasOption(LIST)){
 			for (ScriptDescription sd : goodScripts ){
 				System.out.println("     " + sd.getName() + "       - " + sd.getDescription());
 			}
 			
 			
 			if (haveErrors.size() > 0){
 				System.out.println("The following scripts have errors: ");			
 			}
 			
 			for (ScriptDescription sd : haveErrors ){
 				
 				System.out.println("  " + sd.getPath() + "       - " + sd.getErrorMesg());
 				
 			}		
 	
 			
 		} else{
 			
 			
 			
 			
 			List<String> remainder =  cmdOptions.getRemainingOptions();
 			
 
 			try {
 				String host = getOption(cmdOptions,HOST);
 				String port = getOption(cmdOptions,PORT);
 				String apiVersion = getOption(cmdOptions,API_VERSION);
 				String key = getOption(cmdOptions,API_KEY);
 			
 				
 				
 				if (remainder.size() > 0){
 					String scriptName = remainder.get(0);
 					if (scriptLookup.containsKey(scriptName ) ){
 						ScriptDescription sd = scriptLookup.get(scriptName);
 						ScriptRunner runner2  = new ScriptRunner(host, port, apiVersion, key);
 		
 						runner2.loadAndRunOneScript( sd.getPath());
 	
 					} else {
 						System.out.println("Errror: Script " + scriptName + " Not Found");
 					}
 				}
 			
 			
 			} catch (IOException ioe){
 					System.err.println("Error : " + ioe);  
 			}
 
 		}
 		
 		
 	}
 
 	private static String getOption(AdminToolCommandLineOptions cmdOptions, String key) throws IOException{
 		
 		Preferences prefs = Preferences.userNodeForPackage(AdminTool.class);		
 		String value = null;
 		if (cmdOptions.hasOption(key)) {
 			// if option passed in on commandline then use that
 			value = cmdOptions.getOptionValue(key);
 			
 
 		} else {
 			// if option is in registry then use that
 
 			String prefValue = prefs.get(key, "");
 		
 			if (cmdOptions.hasOption(NOCACHE) || prefValue.equals("")){
 				// prompt the user for the option
 	
 				System.out.println("Please, Enter : " + key);  
 				BufferedReader consoleIn =  new BufferedReader(new InputStreamReader(System.in));  
 				value = consoleIn.readLine(); 	
 			
 			} else {
 				return prefValue;
 			}
 		}
 	
 	
 		// save to registry
 		prefs.put(key, value);
 	
 		return value;
 	
 	}
 
 
 	private ScriptRunner runner = null;
 
 
 	public AdminTool(){
 		
 	}
 
 
 	public List<ScriptDescription> listScripts(){
 	
 		runner  = new ScriptRunner("");
 		return runner.describeScriptsInFolder(COMMAND_SCRIPT_PATH);
 
 	}
 
 
 	public void runScript(String filePath){
 	runner  = new ScriptRunner("");
 		runner.loadAndRunOneScript(filePath);
 
 	}	
 	
 	
 	
 	
 
 }
