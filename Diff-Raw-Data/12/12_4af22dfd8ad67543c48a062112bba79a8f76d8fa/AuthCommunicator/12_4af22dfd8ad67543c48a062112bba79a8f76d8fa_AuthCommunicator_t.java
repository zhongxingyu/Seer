 package edu.rochester.cif.cifreader;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.BufferedReader;
 
 import org.apache.log4j.Logger;
 
 /**
  * Each instance of AuthCommunicator controls a single authenticator daemon.
  * This class spawns a new query daemon and communicates with it via STDIN,
  * STDOUT, and STDERR.}
  * AuthCommunicator hands the daemon a hashed UID and expects a UID, full name,
  * LCC, and auth code in response.
  */
 public class AuthCommunicator {
 
 	/** Handle for the running process */
 	private Process process;
 	
 	private BufferedOutputStream outputStream;
 	//private BufferedInputStream inputStream;
 	private BufferedReader inputBuffer;
 	private BufferedInputStream errorStream;
 	
 	protected static Logger logger = Logger.getLogger(AuthCommunicator.class);
 
 	/**
 	 * AuthCommunicator factory method.
 	 * @return A new AuthCommunicator object on success. On failure, throws an IOException.
 	 * @throws IOException if the query process cannot start
 	 */
 	public static AuthCommunicator getAuthComm() throws IOException {
 		try {
 			return new AuthCommunicator();
 		} catch (IOException ioe) {
 			throw ioe;
 		}
 	}
 	
 	/**
 	 * Start a new authenticator process and capture its I/O streams.
 	 * @throws IOException if the query process cannot start
 	 */
 	private AuthCommunicator() throws IOException {
 		try {
 			// Run the query daemon
 			runProc();
 			
 			// Spawn a process monitor thread
 			Thread monitorThread = new Thread(new ProcessMonitor(process), "AuthCommunicator Process Monitor");
 			monitorThread.start();
 			
 			// Start logging errors
 			Thread errorThread = new Thread(new ErrorLogger(this.errorStream));
 			errorThread.start();
 		} catch(java.io.IOException ioe) {
 			logger.error("Error starting authenticator process", ioe);
 			throw ioe;
 		}
 	}
 
 	/**
 	 * Sets up a process object
 	 */
 	private void runProc() throws IOException {
 		String[] proc = new String[]{Cfg.getString(Cfg.AUTHENTICATOR_CMD)};
 		logger.info("Starting " + proc[0]);
 		this.process = new ProcessBuilder(proc).start();
 		
 		// Get handles to process's streams
 		this.outputStream = new BufferedOutputStream(process.getOutputStream());
 		//this.inputStream = new BufferedInputStream(process.getInputStream());
 		this.inputBuffer = new BufferedReader(new InputStreamReader(process.getInputStream()));
 		this.errorStream = new BufferedInputStream(process.getErrorStream());
 	}
 	
 	/**
 	 * Write a hashed UID to the query process, then wait for a reply.
 	 * Packages the reply into a CifUser object.
 	 * @param hashedUid The hashed UID
 	 * @return A CifUser object containing a UID, full name, LCC, and auth code; null if there was an error
 	 */
 	public CifUser getUser(String hashedUid) {
 		try {
 			
			logger.debug("Sending hashed UID \'"+hashedUid+"\'' to ReaderQuery");
 
 			// Query daemon requires a newline-terminated string
 			//String outString = "\n" + hashedUid + "\n";
 			//this.outputStream.write('\n');
 			this.outputStream.write(hashedUid.getBytes());
 			this.outputStream.write('\n');
 			this.outputStream.flush();
 	
 			// Read the inputStream one byte at a time
 			//BufferedReader in = new BufferedReader(new InputStreamReader(this.inputStream));
 			
 			String line = inputBuffer.readLine();
 			if (line == null) {
				logger.error("ReaderQuery error: Unexpected end of input stream");
 				return null;
 			}
 			else if (line.equals("~")) {
 				// Bad UID
 				logger.error(String.format("Hashed UID %s failed to authenticate", hashedUid));
 				return null;
 			}
 			else {
 				//String[] response = new String(line).split("|");
 				//return new CifUser(response);
				logger.debug("Received string from ReaderQuery: "+line);
 				return CifUser.cifUserFromResponse(line);
 			}
 		} catch (java.io.IOException ioe) {
 			logger.error("ReaderQuery STDOUT error", ioe);
 		}
 		return null;
 	}
 	
 	public void close() {
 		try {
 			this.outputStream.close();
 			//this.inputStream.close();
 			this.inputBuffer.close();
 			this.errorStream.close();
 		} catch (java.io.IOException ioe) {
 			logger.error("Error closing reader query stream", ioe);
 		}
 	}
 	
 	/**
 	 * This thread read an error stream and logs every line it finds.
 	 * If it encounters an error, it logs that, too.
 	 */
 	private class ErrorLogger implements Runnable {
 		private InputStream errorStream;
 		
 		//TODO: Graceful ending
 		
 		public ErrorLogger(InputStream stream) {
 			this.errorStream = stream;
 		}
 		
 		public void run() {
 			// TODO: Better condition than "true"
 			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(this.errorStream));
 				while(true) {
 					String line = in.readLine();
 					
 					if (line == null) {
 						//logger.error("Reader query error: Unexpected end of error stream");
 						continue;
 					}
 					else {
 						logger.error(line);
 					}
 				}
 			} catch (java.io.IOException ioe) {
 				logger.error("ReaderQuery STDERR error", ioe);
 			}
 		}
 	}
 	
 	/**
 	 * Simple thread that watches for a process to end, reports when it does, and tries to restart.
 	 */
 	private class ProcessMonitor implements Runnable {
 		private Process proc;
 		
 		public ProcessMonitor(Process proc) {
 			this.proc = proc;
 		}
 		
 		public void run() {
 			try {
 				//TODO: "true" is probably a bad condition
 				while(true) {
 					this.proc.waitFor();
 					reportEnd();
 					// Restart the process!
 					runProc();
 				}
 			} catch (InterruptedException ie) {
 				logger.error("Query process monitor thread interrupted", ie);
 			} catch(java.io.IOException ioe) {
 				logger.error("Error restarting authenticator process", ioe);
 				throw new RuntimeException(ioe);
 			}
 		}
 		
 		/**
 		 * Grabs the process exit status, reports an error
 		 * @return Process exit status.
 		 */
 		private int reportEnd() {
 			int status = this.proc.exitValue();
 			logger.error(String.format("Query process ended with exit status %d", status));
 			return status;
 		}
 	}
 }
