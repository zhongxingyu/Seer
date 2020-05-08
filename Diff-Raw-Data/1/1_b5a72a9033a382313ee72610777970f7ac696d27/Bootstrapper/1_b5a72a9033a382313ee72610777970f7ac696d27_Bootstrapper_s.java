 package org.cloudifysource.quality.iTests.framework.utils;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CloudTestUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils.ProcessResult;
 
 
 public abstract class Bootstrapper {
 
 	private int timeoutInMinutes;
 	private String user;
 	private String password;
 	private boolean secured = false;
 	private String securityFilePath;
 	private String keystoreFilePath;
 	private String keystorePassword;
 	private boolean force = false;
 	private String restUrl;
 	private boolean bootstrapExpectedToFail = false;
 	private boolean teardownExpectedToFail = false;
 	private boolean verbose = true;
 	private boolean freshBootstrap = true;
 	private boolean scanForLeakedNodes = true;
     private URL[] restAdminUrls;
     private int numberOfManagementMachines;
 
     public void setNumberOfManagementMachines(final int numberOfManagementMachines) {
         this.numberOfManagementMachines = numberOfManagementMachines;
     }
 
     public int getNumberOfManagementMachines() {
         return numberOfManagementMachines;
     }
 
 	public Bootstrapper verbose(final boolean verbose) {
 		this.verbose = verbose;
 		return this;
 	}
 	
 	public boolean isVerbose() {
 		return verbose;
 	}
 	
 	public boolean isForce() {
 		return force;
 	}
 	
 	public boolean isTeardownExpectedToFail() {
 		return teardownExpectedToFail;
 	}
 
 	public Bootstrapper teardownExpectedToFail(boolean tearDownExpectedToFail) {
 		this.teardownExpectedToFail = tearDownExpectedToFail;
 		return this;
 	}
 
 	protected String lastActionOutput = "no action performed";
 	
 	private boolean bootstrapped;
 	
 	public boolean isBootstraped() {
 		return bootstrapped;
 	}
 
 	public String getLastActionOutput() {
 		return lastActionOutput;
 	}
 
 	public String getRestUrl() {
 		return restUrl;
 	}
 
 	public boolean isBootstrapExpectedToFail() {
 		return bootstrapExpectedToFail;
 	}
 
 	public void setBootstrapExpectedToFail(boolean isAboutToFail) {
 		bootstrapExpectedToFail = isAboutToFail;
 	}
 
 	public void setRestUrl(String restUrl) {
 		this.restUrl = restUrl;
 	}
 
 	public abstract String getCustomOptions() throws Exception;
 
 	public Bootstrapper(int timeoutInMinutes) {
 		this.timeoutInMinutes = timeoutInMinutes;
 	}
 
 	public Bootstrapper timeoutInMinutes(int timeoutInMinutes) {
 		this.timeoutInMinutes = timeoutInMinutes;
 		return this;
 	}
 
 	public Bootstrapper force(boolean force) {
 		this.force = force;
 		return this;
 	}
 
 	public Bootstrapper secured(boolean secured) {
 		this.secured = secured;
 		return this;
 	}
 
 	public Bootstrapper user(String user) {
 		this.user = user;
 		return this;
 	}
 
 	public Bootstrapper password(String password) {
 		this.password = password;
 		return this;
 	}
 
 	public Bootstrapper securityFilePath(String securityFilePath) {
 		this.securityFilePath = securityFilePath;
 		return this;
 	}
 
 	public String getKeystoreFilePath() {
 		return keystoreFilePath;
 	}
 
 	public Bootstrapper keystoreFilePath(String keystoreFilePath) {
 		this.keystoreFilePath = keystoreFilePath;
 		return this;
 	}
 
 	public String getKeystorePassword() {
 		return keystorePassword;
 	}
 
 	public Bootstrapper keystorePassword(String keystorePassword) {
 		this.keystorePassword = keystorePassword;
 		return this;
 	}
 
 	public boolean isSecured() {
 		return secured;
 	}
 
 	public String getUser() {
 		return user;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public String getSecurityFilePath() {
 		return securityFilePath;
 	}
 
 	public ProcessResult bootstrap() throws Exception {
 		
 		String bootstrapCommand = null;
 		String provider = null;
 		if (this instanceof LocalCloudBootstrapper) {
 			bootstrapCommand = "bootstrap-localcloud";
 			provider = "";
 		} else if (this instanceof CloudBootstrapper) {
 			bootstrapCommand = "bootstrap-cloud";
 			provider = ((CloudBootstrapper) this).getProvider();
 		}
 		
 		
 		StringBuilder builder = new StringBuilder();
 
 		String commandAndOptions = bootstrapCommand + " " + getCustomOptions();
 
 		builder
 		.append(commandAndOptions).append(" ");
 		if (verbose) {
 			builder.append("--verbose").append(" ");
 		}
 		builder.append("-timeout").append(" ")
 		.append(timeoutInMinutes).append(" ");
 
 		if(secured){
 			builder.append("-secured").append(" ");
 		}
 
 		if(StringUtils.isNotBlank(user)){
 			builder.append("-user " + user + " ");
 		}
 
 		if(StringUtils.isNotBlank(password)){
 			builder.append("-password " + password + " ");
 		}
 
 		if(StringUtils.isNotBlank(securityFilePath)){
 			builder.append("-security-file " + securityFilePath + " ");
 		}
 
 		if(StringUtils.isNotBlank(keystoreFilePath)){
 			builder.append("-keystore " + keystoreFilePath + " ");
 		}
 
 		if(StringUtils.isNotBlank(keystorePassword)){
 			builder.append("-keystore-password " + keystorePassword + " ");
 		}
 
 		builder.append(provider);
 
 		if (bootstrapExpectedToFail) {
 			String output = CommandTestUtils.runCommandExpectedFail(builder.toString());
 			ProcessResult result = new ProcessResult(output, 1);
 			bootstrapped = false;
 			lastActionOutput = result.getOutput();
 			return result;
 		}
 		ProcessResult result = CommandTestUtils.runCloudifyCommandAndWait(builder.toString());
 		lastActionOutput = result.getOutput();
         if (this instanceof CloudBootstrapper) {
             if (!((CloudBootstrapper) this).isNoWebServices()) {
                 restAdminUrls = extractRestAdminUrls(result.getOutput(), numberOfManagementMachines);
             }  else {
                 LogUtils.log("Not retrveing rest urls since there are no web services");
             }
         }
 		bootstrapped = true;
 		return result;	
 	}
 
 	public ProcessResult teardown() throws IOException, InterruptedException {
 
 		StringBuilder connectCommandBuilder = new StringBuilder();
 		if (restUrl != null) {
 			connectCommandBuilder.append("connect").append(" ");
 			if (StringUtils.isNotBlank(user) && StringUtils.isNotBlank(password)){
 				connectCommandBuilder.append("-user").append(" ")
 				.append(user).append(" ")
 				.append("-password").append(" ")
 				.append(password).append(" ");
 			}
 			connectCommandBuilder.append(restUrl).append(";");
 		}
 
 		String teardownCommand = null;
 		String provider = null;
 		if (this instanceof LocalCloudBootstrapper) {
 			teardownCommand = "teardown-localcloud";
 			provider = "";
 		} else if (this instanceof CloudBootstrapper) {
 			teardownCommand = "teardown-cloud";
 			provider = ((CloudBootstrapper) this).getProvider();
 		}
 		
 		StringBuilder builder = new StringBuilder();
 		builder
 				.append(teardownCommand)
 				.append(" ")
 			    .append("-timeout")
 			    .append(" ")
 			    .append(timeoutInMinutes).append(" ");
 		if (force) {
 			builder.append("-force").append(" ");
 		}
 		builder.append(provider);
 		if (teardownExpectedToFail) {
 			String output = CommandTestUtils.runCommandExpectedFail(connectCommandBuilder.toString() + builder.toString());
 			ProcessResult result = new ProcessResult(output, 1);
 			bootstrapped = true;
 			lastActionOutput = result.getOutput();
 			return result;
 		}
 		ProcessResult result = CommandTestUtils.runCloudifyCommandAndWait(connectCommandBuilder.toString() + builder.toString());
 		lastActionOutput = result.getOutput();
 		return result;
 	}
 
 	public String listApplications(boolean expectedToFail) throws IOException, InterruptedException {
 		String command = connectCommand() + "list-applications";
 		if (expectedToFail) {
 			lastActionOutput = CommandTestUtils.runCommandExpectedFail(command);
 			return lastActionOutput;
 		}
 		lastActionOutput = CommandTestUtils.runCommandAndWait(command);
 		return lastActionOutput;
 	}
 
 	public String listServices(final String applicationName, boolean expectedToFail) throws IOException, InterruptedException {
 		String command = connectCommand() + "use-application " + applicationName + ";list-services";
 		if (expectedToFail) {
 			lastActionOutput = CommandTestUtils.runCommandExpectedFail(command);
 			return lastActionOutput;
 		}
 		lastActionOutput = CommandTestUtils.runCommandAndWait(command);
 		return lastActionOutput;
 
 	}
 
 	public String listInstances(final String applicationName, final String serviceName, boolean expectedToFail) throws IOException, InterruptedException {
 		String command = connectCommand() + "use-application " + applicationName +";list-instances " + serviceName;
 		if (expectedToFail) {
 			lastActionOutput = CommandTestUtils.runCommandExpectedFail(command);
 			return lastActionOutput;
 		}
 		lastActionOutput = CommandTestUtils.runCommandAndWait(command);
 		return lastActionOutput;
 	}
 
 	public String listServiceInstanceAttributes(final String applicationName, final String serviceName, final int instanceNumber, boolean expectedToFail) throws IOException, InterruptedException {
 		String command = connectCommand() + "use-application " + applicationName +";list-attributes -scope service:" + serviceName + ":" + instanceNumber;
 		if (expectedToFail) {
 			lastActionOutput = CommandTestUtils.runCommandExpectedFail(command);
 			return lastActionOutput;
 		}
 		lastActionOutput = CommandTestUtils.runCommandAndWait(command);
 		return lastActionOutput;
 	}
 
 	public String shutdownManagers(final String applicationName, final String backupFilePath, boolean expectedToFail) throws IOException, InterruptedException {
 		String command = connectCommand() + "use-application " + applicationName +";shutdown-managers -file " + backupFilePath;
 		if (expectedToFail) {
 			lastActionOutput = CommandTestUtils.runCommandExpectedFail(command);
 			return lastActionOutput;
 		}
 		lastActionOutput = CommandTestUtils.runCommandAndWait(command);
 		return lastActionOutput;
 	}
 
 	public String shutdownManagers(final String applicationName, boolean expectedToFail) throws IOException, InterruptedException {
 		String command = connectCommand() + "use-application " + applicationName +";shutdown-managers";
 		if (expectedToFail) {
 			lastActionOutput = CommandTestUtils.runCommandExpectedFail(command);
 			return lastActionOutput;
 		}
 		lastActionOutput = CommandTestUtils.runCommandAndWait(command);
 		return lastActionOutput;
 	}
 
 	public String connect(boolean expectedToFail) throws IOException, InterruptedException {
 		String command = connectCommand();
 		if (expectedToFail) {
 			lastActionOutput = CommandTestUtils.runCommandExpectedFail(command);
 			return lastActionOutput;
 		}
 		lastActionOutput = CommandTestUtils.runCommandAndWait(command);
 		return lastActionOutput;
 	}
 
 	public String login(boolean expectedToFail) throws IOException, InterruptedException {
 		lastActionOutput = CommandTestUtils.runCommand(connectCommand() + "login " + user + " " + password, true, expectedToFail);
 		return lastActionOutput;
 	}
 	
 	public String login(final String user, final String password, boolean expectedToFail) throws IOException, InterruptedException {
 		lastActionOutput = CommandTestUtils.runCommand(connectCommand() + "login " + user + " " + password, true, expectedToFail);
 		return lastActionOutput;
 	}
 
 	protected String connectCommand() {
 		StringBuilder connectCommandBuilder = new StringBuilder();
 		connectCommandBuilder.append("connect").append(" ");
 		if (StringUtils.isNotBlank(user)){
 			connectCommandBuilder.append("-user").append(" ")
 			.append(user).append(" ");
 		if (StringUtils.isNotBlank(password))
 			connectCommandBuilder.append("-password").append(" ")
 			.append(password).append(" ");
 		}
 		connectCommandBuilder.append(restUrl).append(";");
 		return connectCommandBuilder.toString();
 	}
 
     public boolean isFreshBootstrap() {
         return freshBootstrap;
     }
 
     public Bootstrapper killJavaProcesses(boolean killJavaProcesses) {
         this.freshBootstrap = killJavaProcesses;
         return this;
     }
 
     public boolean isScanForLeakedNodes() {
         return scanForLeakedNodes;
     }
 
     public Bootstrapper scanForLeakedNodes(boolean scanForLeakedNodes) {
         this.scanForLeakedNodes = scanForLeakedNodes;
         return this;
     }
 
     public URL[] getRestAdminUrls() {
         return restAdminUrls;
     }
 
     private URL[] extractRestAdminUrls(String output, int numberOfManagementMachines)
             throws MalformedURLException {
 
         URL[] restAdminUrls = new URL[numberOfManagementMachines];
 
         Pattern restPattern = Pattern.compile(CloudTestUtils.REST_URL_REGEX);
         Matcher restMatcher = restPattern.matcher(output);
 
         // This is sort of hack.. currently we are outputting this over ssh and locally with different results
         for (int i = 0; i < numberOfManagementMachines; i++) {
             AssertUtils.assertTrue("Could not find actual rest url", restMatcher.find());
             String rawRestAdminUrl = restMatcher.group(1);
             restAdminUrls[i] = new URL(rawRestAdminUrl);
         }
 
         return restAdminUrls;
     }
 
     }
