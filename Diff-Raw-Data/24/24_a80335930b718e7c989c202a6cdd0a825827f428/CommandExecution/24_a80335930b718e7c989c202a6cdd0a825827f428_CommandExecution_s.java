 package org.treedecor.imp;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 
 /**
  * Provides methods for calling external commands.
  * 
  * @author Sebastian Erdweg <seba at informatik uni-marburg de>
  */
 public class CommandExecution {
 	private static class Logger extends Thread {
 		private final InputStream in;
 		private final StringBuffer sb = new StringBuffer();
 
 		public Logger(InputStream in) {
 			this.in = in;
 		}
 
 		@Override
 		public void run() {
 			try {
 				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
 				String line = null;
 				while ((line = reader.readLine()) != null) {
 					sb.append(line);
 				}
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			}
 		}
 
 		public String toString() {
 			return sb.toString();
 		}
 	}
 	
 	public static String callSync(String[] cmdarray, String in) throws IOException, InterruptedException {
 		Process process = Runtime.getRuntime().exec(cmdarray);
 		
		OutputStreamWriter osw = new OutputStreamWriter(process.getOutputStream());
		osw.write(in);
		osw.close();

 		Logger logger = new Logger(process.getInputStream());
 		logger.start();
 		Logger errorLogger = new Logger(process.getErrorStream());
 		errorLogger.start();
 
 		process.waitFor();
 		return logger.toString();
 	}
 	
 	public static void main(String[] args) throws IOException, InterruptedException {
 		String[] cmdarray = {"/bin/ls", "/home/stefan"};
 		System.out.println(CommandExecution.callSync(cmdarray, ""));
 	}
 }
