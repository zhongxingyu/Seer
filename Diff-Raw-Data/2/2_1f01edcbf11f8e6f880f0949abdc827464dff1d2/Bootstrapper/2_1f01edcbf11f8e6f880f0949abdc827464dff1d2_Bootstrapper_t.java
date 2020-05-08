 package framework.utils;
 
 import java.io.IOException;
 
 import org.apache.commons.lang.StringUtils;
 
 import test.cli.cloudify.CommandTestUtils;
 import test.cli.cloudify.CommandTestUtils.ProcessResult;
 
 
 public abstract class Bootstrapper {
 
 	private int timeoutInMinutes;
 	private String user;
 	private String password;
 	private boolean secured = false;
 	private String securityFilePath;
 	private String keystoreFilePath;
 	private String keystorePassword;
 	private boolean force = true;
 	private String restUrl;
 	private boolean bootstrapExpectedToFail = false;
 	private boolean teardownExpectedToFail = false;
 	
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
 		.append(commandAndOptions).append(" ")
 		.append("--verbose").append(" ")
 		.append("-timeout").append(" ")
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
 		ProcessResult result = CommandTestUtils.runCloudifyCommandAndWait(connectCommandBuilder.toString() + builder.toString());
 		lastActionOutput = result.getOutput();
 		return result;
 	}
 
 	public String listApplications(boolean expectedToFail) throws IOException, InterruptedException {
 		String command = connectCommand() + ";list-applications";
 		if (expectedToFail) {
 			lastActionOutput = CommandTestUtils.runCommandExpectedFail(command);
 			return lastActionOutput;
 		}
 		lastActionOutput = CommandTestUtils.runCommandAndWait(command);
 		return lastActionOutput;
 	}
 
 	public String listServices(final String applicationName, boolean expectedToFail) throws IOException, InterruptedException {
 		String command = connectCommand() + ";use-application " + applicationName + ";list-services";
 		if (expectedToFail) {
 			lastActionOutput = CommandTestUtils.runCommandExpectedFail(command);
 			return lastActionOutput;
 		}
 		lastActionOutput = CommandTestUtils.runCommandAndWait(command);
 		return lastActionOutput;
 
 	}
 
 	public String listInstances(final String applicationName, final String serviceName, boolean expectedToFail) throws IOException, InterruptedException {
 		String command = connectCommand() + ";use-application " + applicationName +";list-instances " + serviceName;
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
 		lastActionOutput = CommandTestUtils.runCommand(connectCommand() + ";" + "login " + user + " " + password, true, expectedToFail);
 		return lastActionOutput;
 	}
 	
 	public String login(final String user, final String password, boolean expectedToFail) throws IOException, InterruptedException {
 		lastActionOutput = CommandTestUtils.runCommand(connectCommand() + ";" + "login " + user + " " + password, true, expectedToFail);
 		return lastActionOutput;
 	}
 
 	private String connectCommand() {
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
 }
