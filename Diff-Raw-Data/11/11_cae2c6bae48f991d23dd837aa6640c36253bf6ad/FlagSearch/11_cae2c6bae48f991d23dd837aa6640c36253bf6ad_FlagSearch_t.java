 package com.redhat.automationportal.scripts;
 
 import com.redhat.automationportal.base.AutomationBase;
 import com.redhat.automationportal.base.Constants;
 import com.redhat.ecs.commonutils.PropertyUtils;
 
 public class FlagSearch extends AutomationBase
 {
 	private static String BUILD = "20120514-1344";
 	private static final String TEMPLATE_DIR = "/opt/automation-interface/Flag_search";
 	
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
 
 			// enter the scripts directory
 			"&& cd \\\"" + this.getTmpDirectory(randomInt) + "\\\" " +
 
 			// run the python script
			"&& perl flag_search7.pl --login=" + bugzillaUsername + " --password=${" + randomString + "} --product_name=\\\"" + this.productName + "\\\" --component=\\\"" + this.component + "\\\"";
 
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
