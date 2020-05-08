 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
  */
 package timestamp;
 
 import java.io.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 
 /**
 *
 * @author chris
  */
 public class Timestamped {
 	static DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 	
 	/**
 	 * Reads from an input stream and writes to an output stream.
 	 */
 	static class Writer implements Runnable {
 		private BufferedReader input;
 		private PrintStream output;
 		private DateFormat format;
 		
 		public Writer(InputStream in, PrintStream out, DateFormat format){
 			this.input = new BufferedReader(new InputStreamReader(in));
 			this.format = format;
 			this.output = out;
 		}
 		
 		@Override
 		public void run() {
 			try {
 				String line;
 				while ((line = input.readLine()) != null) {
 					Date time = Calendar.getInstance().getTime();
 					output.println(format.format(time) + ": " + line);
 				}
 			} catch (IOException ex) {
 				System.err.println("IO Exception occured: " + ex.getLocalizedMessage());
 				ex.printStackTrace();
 			} finally {
 				try {
 					input.close();
 				} catch (IOException ex){
 				
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Print the commands usage string
 	 */
 	private static void printUsage(){
		System.err.println("Usage: Timestamped [-format 'date format string'] <command>");
 	}
 	
 	/**
 	 * @param args the command line arguments
 	 */
 	public static void main(String[] args) {
 		try {
 			// If the user didn't add any arguments, print usage information
 			if (args == null || args.length == 0){
 				printUsage();
 				System.exit(0);
 			}
 			// Check for options
 			if (args[0].startsWith("-")){
 				if (args[0].equals("-format")){
 					//Create new format with passed in string
 					format = new SimpleDateFormat(args[1]);
 					args = Arrays.copyOfRange(args, 2, args.length);
 				} else {
 					System.err.printf("Invalid option '%s'\n", args[0]);
 					printUsage();
 					System.exit(0);
 				}
 			}
 			
 			// Create a command line by combining the arguments
 			StringBuilder sb = new StringBuilder();
 			for (String arg: args){
 				sb.append(arg);
 				sb.append(" ");
 			}
 			String commandline = sb.toString();
 			
 			//Run our command
 			Process p = Runtime.getRuntime().exec(commandline);
 			// Create writers for the standard output and the error output
 			Writer cmdOutput = new Writer(p.getInputStream(), System.out, format);
 			Writer cmdError = new Writer(p.getErrorStream(), System.err, format);
 			// Create threads to pipe out data
 			Thread output = new Thread(cmdOutput);
 			Thread error = new Thread(cmdError);
 			// Pipe out data with timestamps
 			output.start();
 			error.start();
 			// Wait for both threads to finish reading out data
 			output.join();
 			error.join();
 			System.exit(p.exitValue());
 		} catch (InterruptedException ex) {
 			System.err.println("Interrupted Exception while waiting to join output threads.");
 		} catch (IOException ex) {
 			System.err.println("Error while creating a process to run the command.");
 		}
 	}
 }
