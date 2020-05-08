 package framework.utils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 
 import test.cli.cloudify.CommandTestUtils;
 import test.cli.cloudify.security.SecurityConstants;
 
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
 
 	public RecipeInstaller setDisableSelfHealing(boolean disableSelfHealing) {
 		this.disableSelfHealing = disableSelfHealing;
 		return this;
 	}
 	
 	public String getRestUrl() {
 		return restUrl;
 	}
 
 	public RecipeInstaller restUrl(String restUrl) {
 		this.restUrl = restUrl;
 		return this;
 	}
 
 	public String getRecipePath() {
 		return recipePath;
 	}
 
 	public RecipeInstaller recipePath(String recipePath) {
 		this.recipePath = recipePath;
 		return this;
 	}
 
 	public Map<String, Object> getOverrides() {
 		return overrideProperties;
 	}
 
 	public RecipeInstaller overrides(Map<String, Object> overridesFilePath) {
 		this.overrideProperties = overridesFilePath;
 		return this;
 	}
 
 	public Map<String, Object> getCloudOverrides() {
 		return cloudOverrideProperties;
 	}
 
 	public RecipeInstaller cloudOverrides(Map<String, Object> cloudOverridesFilePath) {
 		this.cloudOverrideProperties = cloudOverridesFilePath;
 		return this;
 	}
 
 	public long getTimeoutInMinutes() {
 		return timeoutInMinutes;
 	}
 
 	public RecipeInstaller timeoutInMinutes(long timeoutInMinutes) {
 		this.timeoutInMinutes = timeoutInMinutes;
 		return this;
 	}
 
 	public boolean isWaitForFinish() {
 		return waitForFinish;
 	}
 
 	public RecipeInstaller waitForFinish(boolean waitForFinish) {
 		this.waitForFinish = waitForFinish;
 		return this;
 	}
 
 	public boolean isExpectToFail() {
 		return expectToFail;
 	}
 
 	public RecipeInstaller expectToFail(boolean expectToFail) {
 		this.expectToFail = expectToFail;
 		return this;
 	}
 	
 	public RecipeInstaller cloudifyUsername(String cloudifyUsername) {
 		this.cloudifyUsername = cloudifyUsername;
 		return this;
 	}
 
 	public RecipeInstaller cloudifyPassword(String cloudifyPassword) {
 		this.cloudifyPassword = cloudifyPassword;
 		return this;
 	}
 	
 	public String getCloudifyUsername() {
 		return cloudifyUsername;
 	}
 
 	public String getCloudifyPassword() {
 		return cloudifyPassword;
 	}
 	
 	public String getAuthGroups() {
 		return authGroups;
 	}
 
 	public RecipeInstaller authGroups(String authGroups) {
 		this.authGroups = authGroups;
 		return this;
 	}
 			
 	public String install() throws IOException, InterruptedException {
 		
 		String installCommand = null;
 		String excpectedResult = null;
 		String recipeName = null;
 		if (this instanceof ServiceInstaller) {
 			installCommand = "install-service";
 			recipeName = ((ServiceInstaller)this).getServiceName();
 			excpectedResult = "Service \"" + recipeName + "\" successfully installed";
 		} else if (this instanceof ApplicationInstaller) {
 			installCommand = "install-application";
 			recipeName = ((ApplicationInstaller)this).getApplicationName();
 			excpectedResult = "Application " + recipeName + " installed successfully";
 		}
 		
 		if (recipePath == null) {
 			throw new IllegalStateException("recipe path cannot be null. please use setRecipePath before calling install");
 		}
 			
 		StringBuilder commandBuilder = new StringBuilder()
 				.append(installCommand).append(" ")
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
 		if (authGroups != null && !authGroups.isEmpty()) {
 			commandBuilder.append("-authGroups").append(" ").append(authGroups).append(" ");
 		}
 		
 		if (recipeName != null && !recipeName.isEmpty()) {
 			commandBuilder.append("-name").append(" ").append(recipeName).append(" ");
 		}
 		
 		commandBuilder.append(recipePath.replace('\\', '/'));
 		final String installationCommand = commandBuilder.toString();
 		final String connectCommand = connectCommand();
 		if (expectToFail) {
 			String output = CommandTestUtils.runCommandExpectedFail(connectCommand + ";" + installationCommand);
			AssertUtils.assertTrue("Installation of " + recipeName + " was expected to fail. but it succeeded", output.toLowerCase().contains("operation failed"));
 			return output;
 		}
 		if (waitForFinish) {
 			String output = CommandTestUtils.runCommandAndWait(connectCommand + ";" + installationCommand);
 			AssertUtils.assertTrue("Installation of " + recipeName + " was expected to succeed, but it failed", output.toLowerCase().contains(excpectedResult.toLowerCase()));
 			return output;			
 		} else {
 			return CommandTestUtils.runCommand(connectCommand + ";" + installationCommand);
 		}
 	}
 	
 	public String uninstall() throws IOException, InterruptedException {
 		
 		String uninstallCommand = null;
 		String excpectedResult = null;
 		String recipeName = null;
 		if (this instanceof ServiceInstaller) {
 			uninstallCommand = "uninstall-service";
 			recipeName = ((ServiceInstaller)this).getServiceName();
 			excpectedResult = "Successfully undeployed " + recipeName;
 		} else if (this instanceof ApplicationInstaller) {
 			uninstallCommand = "uninstall-application";
 			recipeName = ((ApplicationInstaller)this).getApplicationName();
 			excpectedResult = "Application " + recipeName + " uninstalled successfully";
 		}
 		
 		String url = null;
 		try {
 			url = restUrl + "/service/dump/machines/?fileSizeLimit=50000000";
 			DumpUtils.dumpMachines(restUrl, SecurityConstants.USER_PWD_ALL_ROLES, SecurityConstants.USER_PWD_ALL_ROLES);
 		} catch (final Exception e) {
 			LogUtils.log("Failed to create dump for this url - " + url, e);
 		}
 		
 		final String uninstallationCommand = new StringBuilder()
 				.append(uninstallCommand).append(" ")
 				.append("--verbose").append(" ")
 				.append("-timeout").append(" ")
 				.append(timeoutInMinutes).append(" ")
 				.append(recipeName)
 				.toString();
 		
 		final String connectCommand = connectCommand();
 		
 		if (expectToFail) {
 			String output = CommandTestUtils.runCommandExpectedFail(connectCommand + ";" + uninstallationCommand);
 			AssertUtils.assertTrue("Uninstallation of " + recipeName + "was expected to fail. but it succeeded", output.toLowerCase().contains("operation failed"));
 			return output;
 		}
 		if (waitForFinish) {
 			String output = CommandTestUtils.runCommandAndWait(connectCommand + ";" + uninstallationCommand);
 			AssertUtils.assertTrue("Uninstallation of " + recipeName + " was expected to succeed, but it failed", output.toLowerCase().contains(excpectedResult.toLowerCase()));
 			return output;			
 		} else {
 			return CommandTestUtils.runCommand(connectCommand + ";" + uninstallationCommand);
 		}
 				
 	}
 	
 	protected String connectCommand(){
 
 		if (restUrl == null || restUrl.isEmpty()) {
 			throw new IllegalStateException("Rest URL cannot be null or empty when trying to connect");
 		}
 		if(StringUtils.isNotBlank(cloudifyUsername) && StringUtils.isNotBlank(cloudifyPassword)){
 			return "connect -user " + cloudifyUsername + " -password " + cloudifyPassword + " " + getRestUrl();
 		}
 
 		return "connect " + getRestUrl();
 	}
 }
