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
 	
 	public String getRestUrl() {
 		return restUrl;
 	}
 
 	public void setRestUrl(String restUrl) {
 		this.restUrl = restUrl;
 	}
 
 	public abstract String getBootstrapCommand();
 		
 	public abstract String getTeardownCommand();
 	
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
 		StringBuilder builder = new StringBuilder();
 		
 		String[] bootstrapCommandParts = getBootstrapCommand().split(" ");
 		String commandAndOptions = bootstrapCommandParts[0] + " " + getCustomOptions();
 		
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
 			builder.append("-pwd " + password + " ");
 		}
 		
 		if(StringUtils.isNotBlank(securityFilePath)){
 			builder.append("-securityFile " + securityFilePath + " ");
 		}
 		
 		if(StringUtils.isNotBlank(keystoreFilePath)){
 			builder.append("-keystore " + keystoreFilePath + " ");
 		}
 		
 		if(StringUtils.isNotBlank(keystorePassword)){
 			builder.append("-keystorePassword " + keystorePassword + " ");
 		}
 			
 		if (bootstrapCommandParts.length == 2) {
 			// cloud bootstrap, append provider
 			builder.append(bootstrapCommandParts[1]);
 		} else {
 			// localcloud bootstrap.
 		}
 		
 		return CommandTestUtils.runCloudifyCommandAndWait(builder.toString());	
 	}
 	
 	public ProcessResult teardown() throws IOException, InterruptedException {
 		
 		StringBuilder connectCommandBuilder = new StringBuilder();
 		if (restUrl != null) {
 			connectCommandBuilder.append("connect").append(" ");
 			if (StringUtils.isNotBlank(user) && StringUtils.isNotBlank(password)){
 				//TODO : More validations
 				connectCommandBuilder.append("-user").append(" ")
 				.append(user).append(" ")
 				.append("-pwd").append(" ")
 				.append(password).append(" ");
 			}
 			connectCommandBuilder.append(restUrl).append(";");
 		}
 		
 		String[] teardownCommandParts = getTeardownCommand().split(" ");
 		String command = teardownCommandParts[0];
 
 		StringBuilder builder = new StringBuilder();
 		builder
 			.append(command).append(" ");
		builder.append("-timeout").append(" ")
			.append(timeoutInMinutes).append(" ");
 		if (force) {
 			builder.append("-force").append(" ");
 		}
 		if (teardownCommandParts.length == 2) {
 			// cloud teardown, append provider
 			builder.append(teardownCommandParts[1]);
 		} else {
 			// localcloud teardown.
 		}
 		return CommandTestUtils.runCloudifyCommandAndWait(connectCommandBuilder.toString() + builder.toString());	
 	}
 }
