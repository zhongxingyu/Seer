 package eu.stratuslab.storage.disk.utils;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.restlet.data.Status;
 import org.restlet.resource.ResourceException;
 
 public class ProcessUtils {
 	
 	public static Boolean isExecutable(String filename) {
 		File exec = new File(filename);
 		return isExecutable(exec);
 	}
 	
 	public static Boolean isExecutable(File exec) {
 		return exec.isFile() && exec.canExecute();
 	}
 	
 	public static void execute(String id, ProcessBuilder pb) {
 		int returnCode = 1;
 		Process process;
 
 		try {
 			process = pb.start();
 
 			boolean blocked = true;
 			while (blocked) {
 				process.waitFor();
 				blocked = false;
 			}
 
 			returnCode = process.exitValue();
 		} catch (IOException e) {
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
 					"An error occured while executing " + id + e.getMessage());
 		} catch (InterruptedException consumed) {
 			// Just continue with the loop.
 		}
 
 		if (returnCode != 0) {
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
 					"An error occured while executing " + id);
 		}
 	}
 	
 }
