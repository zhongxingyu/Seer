 package eu.stratuslab.storage.disk.utils;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.restlet.data.Status;
 import org.restlet.resource.ResourceException;
 
 public final class ProcessUtils {
 
 	public enum VerboseLevel {
 		Normal,
 		Debug
 	}
 	
 	private static final Logger LOGGER = Logger.getLogger("org.restlet");
	public static VerboseLevel verboseLevel = VerboseLevel.Debug;
 
 	private ProcessUtils() {
 
 	}
 
 	public static void execute(ProcessBuilder pb, String errorMsg) {
 		int returnCode = 1;
 		String stdout = "";
 		String stderr = "";
 		Process process;
 		StringBuffer outputBuf = new StringBuffer();
 
 		pb.redirectErrorStream(true);
 
 		if(verboseLevel == VerboseLevel.Debug) {
 			info(pb);
 		}
 		
 		try {
 			process = pb.start();
 
 			BufferedReader stdOutErr = new BufferedReader(
 					new InputStreamReader(process.getInputStream()));
 			String line;
 			while ((line = stdOutErr.readLine()) != null) {
 				outputBuf.append(line);
 				outputBuf.append("\n");
 			}
 
 			processWait(process);
 
 			stdOutErr.close();
 
 			returnCode = process.exitValue();
 			stdout = streamToString(process.getInputStream());
 			stderr = streamToString(process.getErrorStream());
 		} catch (IOException e) {
 
 			String msg = "An error occurred while executing command: "
 					+ MiscUtils.join(pb.command(), " ") + ".\n" + errorMsg
 					+ ".";
 
 			LOGGER.severe(msg);
 			LOGGER.severe(e.getMessage());
 
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, msg);
 		}
 
 		if (returnCode != 0) {
 
 			process.getErrorStream();
 
 			String msg = "An error occurred while executing command: "
 					+ MiscUtils.join(pb.command(), " ") + ".\n"
 					+ outputBuf.toString() + "\n" + errorMsg
 					+ ".\nReturn code was: " + String.valueOf(returnCode);
 
 			LOGGER.severe(msg);
 			LOGGER.severe("Standard Output: \n" + stdout + "\n");
 			LOGGER.severe("Standard Error: \n" + stderr + "\n");
 
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, msg);
 		}
 	}
 
 	private static void info(ProcessBuilder processBuilder) {
 		LOGGER.info(joinList(processBuilder.command(), " "));
 	}
 	
     private static String joinList(List<String> list, String glue) {
         Iterator<String> i = list.iterator();
         if(i.hasNext() == false) {
         	return "";
         }
         StringBuffer res = new StringBuffer();
         res.append(i.next());
         while (i.hasNext()) {
         	res.append(glue + i.next());
         }
         return res.toString();
     }	
 	
 	public static int executeGetStatus(ProcessBuilder pb) {
 		Process process;
 		try {
 			process = pb.start();
 			return processWaitGetStatus(process);
 		} catch (IOException e) {
 			return -1;
 		}
 	}
 
 	private static void processWait(Process process) {
 		boolean blocked = true;
 		while (blocked) {
 			try {
 				process.waitFor();
 				blocked = false;
 			} catch (InterruptedException consumed) {
 				// just continue to wait
 			}
 		}
 
 	}
 
 	private static int processWaitGetStatus(Process process) {
 		int rc = -1;
 		boolean blocked = true;
 		while (blocked) {
 			try {
 				rc = process.waitFor();
 				blocked = false;
 			} catch (InterruptedException consumed) {
 				// just continue to wait
 			}
 		}
 		return rc;
 	}
 
 	private static String streamToString(InputStream is) {
 
 		StringBuilder sb = new StringBuilder();
 		char[] c = new char[1024];
 
 		Reader r = null;
 
 		try {
 
 			r = new InputStreamReader(is);
 			for (int n = r.read(c); n > 0; n = r.read(c)) {
 				sb.append(c, 0, n);
 			}
 
 		} catch (IOException consumed) {
 			// Do nothing.
 		} finally {
 			if (r != null) {
 				try {
 					r.close();
 				} catch (IOException consumed) {
 					// Do nothing.
 				}
 			}
 		}
 
 		return sb.toString();
 	}
 
 }
