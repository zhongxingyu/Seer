 package org.freewheelschedule.freewheel.remoteworker;
 
 import java.io.BufferedReader;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 
 import lombok.Setter;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.freewheelschedule.freewheel.common.message.JobInitiationMessage;
 
 public class CommandLineExecution implements Execution {
 
 	private static Log log = LogFactory.getLog(CommandLineExecution.class);
 	
 	private @Setter JobInitiationMessage command;
 	
 	@Override
 	public void run() {
 
 		String message = null;
 		PrintWriter stdoutOutput = null;
 		PrintWriter stderrOutput = null;
 		
 		log.info("Running command " + command);
 
 		try {
 			Process process = Runtime.getRuntime().exec(command.getCommand());
 			
 			if (command.getStdout()!= null) {
				stdoutOutput = new PrintWriter(new FileOutputStream(command.getStdout(),command.getAppendStdout()));
 			}
 			if (command.getStderr() != null) {
				stderrOutput = new PrintWriter(new FileOutputStream(command.getStderr(),command.getAppendStderr()));
 			}
 			// getInputStream() returns the stdout of the process that ran
 			BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
 			BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
 			while ((message = stdOut.readLine()) != null) {
 				log.info("stdout: " + message);
 				if (stdoutOutput != null) {
					stdoutOutput.write(message+"\n");
 				}
 			}
 			while ((message = stdErr.readLine()) != null) {
 				log.info("stderr: " + message);
 				if (stderrOutput != null) {
					stderrOutput.write(message+"\n");
 				}
 			}
 			if (stderrOutput != null) {
 				stderrOutput.close();
 				stderrOutput = null;
 			}
 			if (stdoutOutput != null) {
 				stdoutOutput.close();
 				stdoutOutput = null;
 			}
 
 		} catch (IOException e) {
 			log.error("Execution failed", e);
 		} finally {
 			if (stderrOutput != null) {
 				stderrOutput.close();
 			}
 			if (stdoutOutput != null) {
 				stdoutOutput.close();
 			}
 		}
 
 	}
 
 }
