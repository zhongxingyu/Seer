 /*
 * Copyright (C) 2012 Chris Hallson
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package timestamp;
 
 import java.io.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 
 /**
 * Timestamped runs a command and prepends it's output (both output and error)
 * with a timestamp based on a default format or one supplied using the -format
 * argument.
 * 
 * @author DrLabman
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
				// Read in from the input until null is received
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
		System.err.printf("Usage: Timestamped [-format 'date format string'] <command>\n");
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
