 package dv8.output;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 public class Config {
 	public static boolean loggingToFile;
 	public static String LibsFileName;
 	public static String JavaFileName;
 	public static boolean DebugVerbose = false;
 	public static boolean firstRun;
 	public static File configFile = new File(FileCreator.filesDir + "\\config.txt");
 	
 	public static boolean load(){
 		if(configFile.exists()){
 			firstRun = false;
 			if(readConfig()){
 				return true;
 			}else{
 				return false;
 			}
 		}else {
 			firstRun = true;
 			DebugOutput.out("Did not find Config file, attempting to create one now...", 0);
 			if((configFile = FileCreator.mkFile("config", false)) != null){
 				if(populateDefaults()){
 					DebugOutput.out("Successfully created new Config file and populated it with defaults.", 0);
 					return true;
 				}else{
 					return false;
 				}
 			}else{
 				return false;
 			}
 		}
 	}	
 		
 	private static boolean populateDefaults(){
 		try{
 			BufferedWriter out = new BufferedWriter(new FileWriter(configFile));
 			out.write(
 					"# Lines that start with a \"#\" are ignored.\n"+
 					"#\n"+
 					"# If verbose is enabled, lots more info is put out.  Very helpful.\n"+
 					"DebugVerbose: false\n"+
 					"#\n"+
 					"# Defines the file that contains the java recipes.\n"+
 					"JavaFile: parseMe.txt\n"+
 					"#\n"+
 					"# Defines the file that contains the libs to convert from java to javascript.\n"+
					"Libs: mappingLibs.txt"
 					);
 			out.close();
 			return true;
 		}catch(Exception e){
 			DebugOutput.out("Created new Config files but could not populate new config with defaults", 0);
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	private static boolean readConfig(){
 		BufferedReader br;
 		String strLine;
 		String tempString;
 		try{
 			br = new BufferedReader(new FileReader(configFile));
 			while((strLine = br.readLine())!= null){
 				if(!strLine.startsWith("#")){		//Ignores lines that begin with #
 					if(strLine.contains("DebugVerbose:")){
 						tempString = strLine.substring(strLine.indexOf(':')+1, strLine.length());
 						tempString = tempString.trim();
 						if(tempString.equals("true")){
 							DebugVerbose = true;
 							DebugOutput.out("DebugVerbose: Enabled.", 3);
 						}else if(tempString.equals("false")){
 							DebugVerbose = false;
 							//Do not output that it is disabled because it wouldn't show due to it being disabled.
 						}else{
 							DebugOutput.out(
 									"Could not read the DebugVerbose setting.\n" +
 									"Please make sure it is set to either \"true\" or \"false\" in the config.", 0);
 							br.close();
 							return false;
 						}
 					}else if(strLine.contains("JavaFile:")){
 						JavaFileName = strLine.substring(strLine.indexOf(':')+1, strLine.length());
 						JavaFileName = JavaFileName.trim();
 						DebugOutput.out("JavaFileName: " + JavaFileName, 3);
 					}else if(strLine.contains("Libs:")){
 						LibsFileName = strLine.substring(strLine.indexOf(':')+1, strLine.length());
 						LibsFileName = LibsFileName.trim();
 						DebugOutput.out("LibsFileName: " + LibsFileName, 3);
 					}
 				}
 			}			
 			br.close();
 			DebugOutput.out("Successfully parsed the Config", 3);
 			return true;
 		}catch(IOException e){
 			DebugOutput.out("Could not parse Config.", 0);
 			e.printStackTrace();
 			return false;
 		}
 	}
 }
