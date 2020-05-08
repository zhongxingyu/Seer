 package fitnesseTfs;
 
 import java.io.IOException;
 import java.util.List;
 
 public class SourceControlPlugin {
 
 	/*
 	 * cmEdit(file, payload) is called just before file is about to be written
 	 * cmUpdate(file, payload) is called just after file has been written
 	 * cmPreDelete(directory, payload) is called just before the directory defining a page will be deleted. 
 	 * cmDelete(directory, payload) is called just after the directory defining a page has been deleted.
 	 */
 
 	private static String rootPath = "";
     private static String lastEditedFile = "";
 
 	private static TfRunner tfRunner = new TfRunner();
     private static FileSystem fileSystem = new FileSystem();
     private static ConsoleOutputter outputter = new ConsoleOutputter();
 
     public static void setTfRunner(TfRunner tfRunner) {
 		SourceControlPlugin.tfRunner = tfRunner;
 	}
 	
     public static void setFileSystem(FileSystem fileSystem) {
         SourceControlPlugin.fileSystem = fileSystem;        
     }
 
     public static void setOutputter(ConsoleOutputter outputter) {
         SourceControlPlugin.outputter = outputter;
     }
 
     public static void setLastEditedFile(String file) {
         lastEditedFile = file;
     }
 
 	public static void cmEdit(String file, String payload) {
         outputter.output("cmEdit");
 		parsePayload(payload);
 		String fullPath = buildPath(payload, file);
 		if (fileSystem.fileExists(fullPath) && !fileSystem.isWritable(fullPath))
 			tf("checkout", fullPath);
         outputter.output("");
         setLastEditedFile(file);
 	}
 
     public static void cmUpdate(String file, String payload) throws IOException {
         outputter.output("cmUpdate");
         parsePayload(payload);
         String fullPath = buildPath(payload, file);
         if (!file.equals(lastEditedFile))
             tf("add", fullPath);
         outputter.output("");
         setLastEditedFile("");
     }
 
 	public static void cmPreDelete(String file, String payload) throws IOException {
         outputter.output("cmPreDelete");
         outputter.output("");
 	}
 
 	public static void cmDelete(String file, String payload) throws IOException {
         outputter.output("cmDelete");
 		parsePayload(payload);
 		String fullPath = buildPath(payload, file);
         if (tfInPendingChanges(fullPath))
             tf("undo /recursive", fullPath);
         if (tfFileExists(fullPath))
 			tf("delete /recursive", fullPath);
         outputter.output("");
 	}
 
 	private static void parsePayload(String payload) {
 		List<String> parameters = StringSplitter.split(payload);
 		
 		rootPath = parameters.get(1);
 		String pathToTfCommand = parameters.get(2);
 		boolean output = parameters.size() > 3 ? !parameters.get(3).equals("nooutput") : true;
 		
 		tfRunner.setPathToTfCommand(pathToTfCommand);
         outputter.setOutput(output);
         tfRunner.setOutput(output);
         fileSystem.setOutput(output);
 	}
 	
 	private static boolean tfFileExists(String fullPath) {
 		String existsOutput = tf("dir", fullPath);
		return existsOutput != null && existsOutput.contains("1 item");
 	}
 
     private static boolean tfInPendingChanges(String fullPath) {
         String pendingOutput = tf("status", fullPath);
        return pendingOutput !=  null && pendingOutput.contains("1 change");
     }
 
 	private static String tf(String command, String path) {
         return tfRunner.execute(command, path);
 	}
 	
 	private static String buildPath(String payload, String fileName) {
 		if (!rootPath.endsWith("\\"))
 			rootPath += "\\";
 		fileName = fileName.replace("/", "\\");
 		fileName = fileName.replace("..\\", "");
 		fileName = fileName.replace(".\\", "");
 		return rootPath + fileName;
 	}
 
 }
