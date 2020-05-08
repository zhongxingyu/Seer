 package org.cloudifysource.quality.iTests.test.cli.cloudify;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.text.MessageFormat;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.apache.commons.io.FileUtils;
 import iTests.framework.tools.SGTestHelper;
 import iTests.framework.utils.AssertUtils;
 import iTests.framework.utils.LogUtils;
 import iTests.framework.utils.ScriptUtils;
 
 public class CommandTestUtils {
 
     private static final boolean enableLogstash = Boolean.parseBoolean(System.getProperty("iTests.enableLogstash", "false"));
 
     /**
 	 * Runs the specified cloudify commands and outputs the result log.
 	 * @param command - The actual cloudify commands delimited by ';'.
 	 * @param wait - used for determining if to wait for command 
 	 * @param failCommand  - used for determining if the command is expected to fail
 	 * @return String - log output or null if wait is false
 	 * @throws IOException
 	 * @throws InterruptedException 
 	 */
 	public static String runCommand(final String command, boolean wait, boolean failCommand) throws IOException, InterruptedException {
 		return runLocalCommand( getCloudifyCommand(command), wait, failCommand);
 	}
 
 	/**
 	 * Runs the specified cloudify commands and outputs the result log.
 	 * @param cloudifyCommand - The actual cloudify commands delimited by ';'.
 	 * @return the cloudify output, and the exitcode 
 	 * @throws IOException
 	 * @throws InterruptedException 
 	 */
 	public static ProcessResult runCloudifyCommandAndWait(final String cloudifyCommand) throws IOException, InterruptedException {
 		final Process process = startProcess(getCloudifyCommand(cloudifyCommand));
 	    ProcessResult handleCliOutput = handleCliOutput(process);
 	    if (handleCliOutput.getExitcode() != 0) {
 			AssertUtils.assertFail("CLI execution did not end succesfully, exit code was " + handleCliOutput.getExitcode() + 
 					"output is " + handleCliOutput.getOutput());
 		}
 		return handleCliOutput;
 	}
 
 
 	private static String getCloudifyCommand(String command) {
 		final String commandExtention = getCommandExtention();
 		final String cloudifyPath = ScriptUtils.getBuildPath()+
 				MessageFormat.format("{0}tools{0}cli{0}cloudify.{1} ", File.separatorChar, commandExtention);
 		return (cloudifyPath + command).replace("\\", "/");
 	}
 
 
     public static String runLocalCommand(final String command, boolean wait, boolean failCommand) throws IOException, InterruptedException {
 
         final Process process = startProcess(command);
 
         if(wait)
             return handleCliOutput(process, failCommand);
         else
             return null;
     }
     
     private static Process startProcess(String command)
     		throws IOException {
     	
     	String cmdLine = command;
     	if (isWindows()) {
     		// need to use the call command to intercept the cloudify batch file return code.
     		cmdLine = "cmd /c call " + cmdLine;
     	}
     	
     	final String[] parts = cmdLine.split(" ");
     	final ProcessBuilder pb = new ProcessBuilder(parts);
     	pb.redirectErrorStream(true);
         if(enableLogstash){
            String suiteId = "/suite_" + System.getProperty("iTests.suiteId", "0");
             String systemProperties = System.getProperty("EXT_JAVA_OPTIONS", "");
             LogUtils.log("system properties before addition: " + systemProperties);
 
             String logsPatternProperty = "com.gigaspaces.logger.RollingFileHandler.filename-pattern";
             String logsPatternValue = "{homedir}/logs/" + suiteId + "/{date,yyyy-MM-dd~HH.mm}-gigaspaces-{service}-{host}-{pid}.log";
             LogUtils.log("adding env variable " + logsPatternProperty + " with value " + logsPatternValue);
             pb.environment().put("EXT_JAVA_OPTIONS", systemProperties + " -D" + logsPatternProperty + "=" + logsPatternValue);
 
             systemProperties = System.getProperty("EXT_JAVA_OPTIONS", "");
             LogUtils.log("system properties after addition: " + systemProperties);
         }
 
     	LogUtils.log("Executing Command line: " + cmdLine);
     	
     	final Process process = pb.start();
     	return process;
     }
 
     public static class ProcessResult {
     	
     	private final String output;
     	private final int exitcode;
     	
     	public ProcessResult(String output, int exitcode) {
 			this.output = output;
 			this.exitcode = exitcode;
 		}
     	
 		@Override
 		public String toString() {
 			return "ProcessResult [output=" + getOutput() + ", exitcode=" + getExitcode()
 					+ "]";
 		}
 
 		public String getOutput() {
 			return output;
 		}
 
 		public int getExitcode() {
 			return exitcode;
 		}
     }
     
 	private static String handleCliOutput(Process process, boolean failCommand) throws IOException, InterruptedException{
 		ProcessResult result = handleCliOutput(process);
 		
 		if (result.getExitcode() != 0 && !failCommand) {
 			AssertUtils.assertFail("CLI execution did not end succesfully, exit code was " + result.getExitcode() + 
 					"output is " + result.getOutput());
 		}
 		return result.output;
 	}
 
 	private static ProcessResult handleCliOutput(Process process) throws InterruptedException {
 		// Print CLI output if exists.
 		final BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
 		final StringBuilder consoleOutput = new StringBuilder("");
 		final AtomicReference<Throwable> exception = new AtomicReference<Throwable>();
 		
 		Thread thread = new Thread(new Runnable() {
 			
 			String line = null;
 			
 			@Override
 			public void run() {	
 				try {
 					while ((line = br.readLine()) != null) {
 						LogUtils.log(line);
 						consoleOutput.append(line + "\n");
 					}
 				} catch (Throwable e) {
 					exception.set(e);
 				}
 				
 			}
 		});
 		
 		thread.setDaemon(true);
 		
 		thread.start();
 		
 		int exitcode = process.waitFor();
 		
 		thread.join(5000);
 		
 		if (exception.get() != null) {
 			AssertUtils.assertFail("Failed to get process output. output="+consoleOutput,exception.get());
 		}
 		String stdout = consoleOutput.toString();
 		return new ProcessResult(stdout, exitcode);
 	}
 	
 	/**
 	 * @param command
 	 * @return
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public static String runCommandAndWait(final String command) throws IOException, InterruptedException {
 		return runCommand(command, true, false);
 	}
 
 	/**
 	 * @param command
 	 * @return
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public static String runCommandExpectedFail(final String command) throws IOException, InterruptedException {
 		return runCommand(command, true, true);
 	}
 	
 	public static String runCommand(final String command) throws IOException, InterruptedException {
 		return runCommand(command, false, false);
 	}
 	
 	public static String runCommandUsingFile(final String command) throws IOException, InterruptedException {
 		final File tempFile = File.createTempFile("testcommands", "txt");
 		tempFile.deleteOnExit();
 		FileUtils.writeStringToFile(tempFile, command);
 		return runCommandAndWait("-f=" + tempFile.getAbsolutePath());
 	}
 	
 	private static String getCommandExtention() {
 		String osExtention;
 		if (isWindows()) {
 			osExtention = "bat";
 		} else {
 			osExtention = "sh";
 		}
 		return osExtention;
 	}
 
 	public static boolean isWindows() {
 		return System.getProperty("os.name").toLowerCase().startsWith("win");
 	}
 
 	public static String getPath(String relativePath) {
 		return new File(SGTestHelper.getSGTestRootDir(), relativePath).getAbsolutePath().replace('\\', '/');
 	}
 	
 	public static String getBuildServicesPath() {
 		return SGTestHelper.getBuildDir() + "/recipes/services";
 	}
 	
 	public static String getBuildApplicationsPath() {
 		return SGTestHelper.getBuildDir() + "/recipes/apps";
 	}
 
 
 }
