 package com.redhat.automationportal.scripts;
 
 import java.io.ByteArrayOutputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.redhat.automationportal.base.AutomationBase;
 import com.redhat.automationportal.base.Constants;
 import com.redhat.ecs.commonutils.CollectionUtilities;
 import com.redhat.ecs.commonutils.ExecUtilities;
 import com.redhat.ecs.commonutils.PropertyUtils;
 
 public class FlagSearch extends AutomationBase
 {
 	private static String BUILD = "20120514-1344";
 	private static final String TEMPLATE_DIR = "/opt/automation-interface/Flag_search";
 	private static final String SAVE_DATA_FOLDER = "FlagSearch";
 	private static final String PERSIST_FILENAME = "saved_searches.txt";
 	
 	private String bugzillaUsername;
 	private String bugzillaPassword;
 	private String productName;
 	private String component;
 	
 	public String getBugzillaPassword()
 	{
 		return bugzillaPassword;
 	}
 
 	public void setBugzillaPassword(String bugzillaPassword)
 	{
 		this.bugzillaPassword = bugzillaPassword;
 	}
 
 	public String getBugzillaUsername()
 	{
 		return bugzillaUsername;
 	}
 
 	public void setBugzillaUsername(final String bugzillaUsername)
 	{
 		this.bugzillaUsername = bugzillaUsername;
 	}
 	
 	public String getProductName()
 	{
 		return productName;
 	}
 
 	public void setProductName(String productName)
 	{
 		this.productName = productName;
 	}
 
 	public String getComponent()
 	{
 		return component;
 	}
 
 	public void setComponent(String component)
 	{
 		this.component = component;
 	}
 
 	@Override
 	public String getBuild()
 	{
 		return BUILD;
 	}
 	
 	public List<String> getSavedSearches()
 	{
 		Process process = null;
 		try
 		{
 			final String catCommand = "/bin/su " + (this.username == null ? "automation-user" : this.username) + " -c \"if [ -f ~/" + AutomationBase.SAVE_HOME_FOLDER + "/" + SAVE_DATA_FOLDER + "/" + PERSIST_FILENAME + "  ]; then cat ~/" + AutomationBase.SAVE_HOME_FOLDER + "/" + SAVE_DATA_FOLDER + "/" + PERSIST_FILENAME + "; fi; \"";
 			final String[] command = new String[]	{ "/bin/bash", "-c", catCommand };
 			process = Runtime.getRuntime().exec(command, ExecUtilities.getEnvironmentVars());
 			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 			this.success = ExecUtilities.runCommand(process, outputStream);
 			final String output = outputStream.toString();
 			return CollectionUtilities.toArrayList(output.split("\n"));
 		}
 		catch (final Exception ex)
 		{
 			return new ArrayList<String>();
 		}
 		finally
 		{
 			process.destroy();
 		}
 	}
 
 	public boolean run()
 	{
 		if (this.bugzillaUsername != null && this.bugzillaPassword != null && 
 				this.bugzillaUsername.trim().length() != 0 && this.bugzillaPassword.trim().length() != 0 &&
 				this.productName != null && this.productName != null &&
 				this.component != null && this.component != null)
 		{
 			final Integer randomInt = this.generateRandomInt();
 			final String randomString = this.generateRandomString(10);				
 			
 			if (randomInt == null)
 			{
 				this.message = "BugzillaReportGenerator.run() " + PropertyUtils.getProperty(Constants.ERROR_FPROPERTY_FILENAME, "AMPT0001", this.getClass());
 				return false;
 			}
 			
 			if (randomString == null)
 			{
 				this.message = "BugzillaReportGenerator.run() " + PropertyUtils.getProperty(Constants.ERROR_FPROPERTY_FILENAME, "AMPT0002", this.getClass());
 				return false;
 			}
 
 			this.message = "";
 
 			final String[] environment = new String[] { randomString + "=" + this.bugzillaPassword };
 
 			final String script =
 			// copy the template files
 			"cp -R \\\"" + TEMPLATE_DIR + "/\\\"* \\\"" + this.getTmpDirectory(randomInt) + "\\\" " +
 					
 			// make sure the data folder exists
 			"&& if [ ! -d ~" + (this.username == null ? "automation-user" : this.username) + "/" + SAVE_HOME_FOLDER + "/" + SAVE_DATA_FOLDER + " ]; then " +
 			
 			// create it if it doesn't
 			"mkdir -p ~" + (this.username == null ? "automation-user" : this.username) + "/" + SAVE_HOME_FOLDER + "/" + SAVE_DATA_FOLDER + "; " +
 			
 			/* exit the statement */
 			"fi " +
 			
 			// If the saved file exists
 			"&& if [ -f ~" + (this.username == null ? "automation-user" : this.username) + "/" + SAVE_HOME_FOLDER + "/" + SAVE_DATA_FOLDER + "/" + PERSIST_FILENAME + " ]; then " +
 			
 			/* copy the saved file */
 			"cp ~" + (this.username == null ? "automation-user" : this.username) + "/" + SAVE_HOME_FOLDER + "/" + SAVE_DATA_FOLDER + "/" + PERSIST_FILENAME + " \\\"" + this.getTmpDirectory(randomInt) + "/\\\"; "  +
 			
 			/* exit the statement */
 			"fi " +
 
 			// enter the scripts directory
 			"&& cd \\\"" + this.getTmpDirectory(randomInt) + "\\\" " +
 
 			// run the python script
 			"&& perl flag_search7.pl --login=" + bugzillaUsername + " --password=${" + randomString + "} --product_name=\\\"" + this.productName + "\\\" --component=\\\"" + this.component + "\\\" " +
 								
 			// copy the save_searches.txt to the data folder
			"&& cp \\\"" + this.getTmpDirectory(randomInt) + "/" + PERSIST_FILENAME + " ~" + (this.username == null ? "automation-user" : this.username) + "/" + SAVE_HOME_FOLDER + "/" + SAVE_DATA_FOLDER + "/ "; 
 
 			runScript(script, randomInt, true, true, true, null, environment);
 
 			// cleanup the temp dir
 			cleanup(randomInt);
 			
 			return true;
 		}
 		else
 		{
 			this.message = "Please enter a username, password, product name and component";
 			return false;
 		}
 
 	}
 
 	
 }
