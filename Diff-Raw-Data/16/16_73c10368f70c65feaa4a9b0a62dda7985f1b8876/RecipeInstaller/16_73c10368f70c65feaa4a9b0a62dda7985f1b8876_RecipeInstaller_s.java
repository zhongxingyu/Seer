 package framework.utils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 
 import test.cli.cloudify.CommandTestUtils;
 
 public abstract class RecipeInstaller {
 	
 	private String restUrl;
 	private String recipePath;
 	private boolean disableSelfHealing = false;
 	private Map<String, Object> overrideProperties;
 	private Map<String, Object> cloudOverrideProperties;
 	private long timeoutInMinutes;
 	private boolean waitForFinish = true;
 	private boolean expectToFail = false;
 	private String cloudifyUsername;
 	private String cloudifyPassword;
 	private String authGroups;
 	
 
 	public RecipeInstaller(final String restUrl, int timeout) {
 		this.restUrl = restUrl;
 		this.timeoutInMinutes = timeout;
 	}
 	
 	public boolean isDisableSelfHealing() {
 		return disableSelfHealing;
 	}
 
 	public void setDisableSelfHealing(boolean disableSelfHealing) {
 		this.disableSelfHealing = disableSelfHealing;
 	}
 	
 	public String getRestUrl() {
 		return restUrl;
 	}
 
 	public void setRestUrl(String restUrl) {
 		this.restUrl = restUrl;
 	}
 
 	public String getRecipePath() {
 		return recipePath;
 	}
 
 	public void setRecipePath(String recipePath) {
 		this.recipePath = recipePath;
 	}
 
 	public Map<String, Object> getOverrides() {
 		return overrideProperties;
 	}
 
 	public void setOverrides(Map<String, Object> overridesFilePath) {
 		this.overrideProperties = overridesFilePath;
 	}
 
 	public Map<String, Object> getCloudOverrides() {
 		return cloudOverrideProperties;
 	}
 
 	public void setCloudOverrides(Map<String, Object> cloudOverridesFilePath) {
 		this.cloudOverrideProperties = cloudOverridesFilePath;
 	}
 
 	public long getTimeoutInMinutes() {
 		return timeoutInMinutes;
 	}
 
 	public void setTimeoutInMinutes(long timeoutInMinutes) {
 		this.timeoutInMinutes = timeoutInMinutes;
 	}
 
 	public boolean isWaitForFinish() {
 		return waitForFinish;
 	}
 
 	public void setWaitForFinish(boolean waitForFinish) {
 		this.waitForFinish = waitForFinish;
 	}
 
 	public boolean isExpectToFail() {
 		return expectToFail;
 	}
 
 	public void setExpectToFail(boolean expectToFail) {
 		this.expectToFail = expectToFail;
 	}
 	
 	public void setCloudifyUsername(String cloudifyUsername) {
 		this.cloudifyUsername = cloudifyUsername;
 	}
 
 	public void setCloudifyPassword(String cloudifyPassword) {
 		this.cloudifyPassword = cloudifyPassword;
 	}
 	
 	public String getAuthGroups() {
 		return authGroups;
 	}
 
 	public void setAuthGroups(String authGroups) {
 		this.authGroups = authGroups;
 	}
 
 	public abstract String getInstallCommand();
 	
 	public abstract String getUninstallCommand();
 	
 	public abstract String getRecipeName();
 	
 	public abstract void assertInstall(String output);
 	
 	public abstract void assertUninstall(String output);
 	
	public void install() throws IOException, InterruptedException {
 		
 		if (recipePath == null) {
 			throw new IllegalStateException("recipe path cannot be null. please use setRecipePath before calling install");
 		}
 		
 		StringBuilder connectCommandBuilder = new StringBuilder()
 		.append("connect").append(" ");
 		if (StringUtils.isNotBlank(cloudifyUsername) && StringUtils.isNotBlank(cloudifyPassword)){
 			//TODO : More validations
 			connectCommandBuilder.append("-user").append(" ")
 			.append(cloudifyUsername).append(" ")
 			.append("-pwd").append(" ")
 			.append(cloudifyPassword).append(" ");
 		}
 		connectCommandBuilder.append(restUrl).append(";");
 		
 		StringBuilder commandBuilder = new StringBuilder()
 				.append(getInstallCommand()).append(" ")
 				.append("--verbose").append(" ")
 				.append("-timeout").append(" ")
 				.append(timeoutInMinutes).append(" ");
 		
 		if (disableSelfHealing) {
 			commandBuilder.append("-disableSelfHealing").append(" ");
 		}
 
 		if (cloudOverrideProperties != null && !cloudOverrideProperties.isEmpty()) {	
 			File cloudOverridesFile = IOUtils.createTempOverridesFile(cloudOverrideProperties);
 			commandBuilder.append("-cloud-overrides").append(" ").append(cloudOverridesFile.getAbsolutePath().replace("\\", "/")).append(" ");
 		}
 		if (overrideProperties != null && !overrideProperties.isEmpty()) {
 			File serviceOverridesFile = IOUtils.createTempOverridesFile(overrideProperties);
 			commandBuilder.append("-overrides").append(" ").append(serviceOverridesFile.getAbsolutePath().replace("\\", "/")).append(" ");
 		}
 		
 		commandBuilder.append(recipePath.replace('\\', '/'));
 		final String installCommand = commandBuilder.toString();
 		final String connectCommand = connectCommandBuilder.toString();
 		if (expectToFail) {
			CommandTestUtils.runCommandExpectedFail(connectCommand + installCommand);
			return;
 		}
 		if (waitForFinish) {
			CommandTestUtils.runCommandAndWait(connectCommand + installCommand);
			return;			
 		}
		String output = CommandTestUtils.runCommand(connectCommand + installCommand);
		assertInstall(output);
 	}
 	
 	public void uninstall() throws IOException, InterruptedException {
 		String url = null;
 		try {
 			url = restUrl + "/service/dump/machines/?fileSizeLimit=50000000";
 			DumpUtils.dumpMachines(restUrl, cloudifyUsername, cloudifyPassword);
 		} catch (final Exception e) {
 			LogUtils.log("Failed to create dump for this url - " + url, e);
 		}
 
 		//final String connectCommand = "connect " + restUrl + ";";
 		StringBuilder connectCommandBuilder = new StringBuilder()
 		.append("connect").append(" ");
 		if (StringUtils.isNotBlank(cloudifyUsername) && StringUtils.isNotBlank(cloudifyPassword)){
 			//TODO : More validations
 			connectCommandBuilder.append("-user").append(" ")
 			.append(cloudifyUsername).append(" ")
 			.append("-pwd").append(" ")
 			.append(cloudifyPassword).append(" ");
 		}
 		connectCommandBuilder.append(restUrl).append(";");
 		
 		final String installCommand = new StringBuilder()
 				.append(getUninstallCommand()).append(" ")
 				.append("--verbose").append(" ")
 				.append("-timeout").append(" ")
 				.append(timeoutInMinutes).append(" ")
 				.append(getRecipeName())
 				.toString();
 		String output = CommandTestUtils.runCommandAndWait(connectCommandBuilder.toString() + installCommand);
 		assertUninstall(output);
 	}
 }
