 package com.redhat.automationportal.scripts;
 
 import com.redhat.automationportal.base.AutomationBase;
 import com.redhat.automationportal.base.Constants;
 import com.redhat.ecs.commonutils.PropertyUtils;
 
 public class BugzillaReportGenerator extends AutomationBase
 {
 	private static String BUILD = "20111125-0710";
 	private static final String TEMPLATE_DIR = "/opt/automation-interface/Report_Generator";
 	
 	private String bugzillaUsername;
 	private String bugzillaPassword;
 	
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
 
 	@Override
 	public String getBuild()
 	{
 		return BUILD;
 	}
 
 	public boolean run()
 	{
 		if (this.bugzillaUsername != null && this.bugzillaPassword != null && this.bugzillaUsername.trim().length() != 0 && this.bugzillaPassword.trim().length() != 0)
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
 			"&& perl report_generator.pl -login=" + bugzillaUsername + " -password=${" + randomString + "}";
 
 			runScript(script, randomInt, true, true, true, null, environment);
 
 			// cleanup the temp dir
 			cleanup(randomInt);
 			
 			return true;
 		}
 		else
 		{
 			this.message = "Please enter a username and password";
 			return false;
 		}
 
 	}
 
 	
 }
