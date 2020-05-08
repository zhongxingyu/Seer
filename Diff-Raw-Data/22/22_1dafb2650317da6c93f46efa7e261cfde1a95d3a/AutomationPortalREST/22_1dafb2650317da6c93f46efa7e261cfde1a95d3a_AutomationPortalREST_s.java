 package com.redhat.automationportal.rest;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Response;
 
 import org.apache.log4j.Logger;
 
 import com.redhat.automationportal.base.AutomationBase;
 import com.redhat.automationportal.base.Constants;
 import com.redhat.automationportal.scripts.BugzillaReportGenerator;
 import com.redhat.ecs.commonutils.PropertyUtils;
 
 @Path("/")
 public class AutomationPortalREST
 {
 	@GET
 	@Path("/BugzillaReportGenerator/get/json")
 	public Response BugzillaReportGeneratorGetJson(@QueryParam("bugzillaUsername") final String bugzillaUsername, @QueryParam("bugzillaPassword") final String bugzillaPassword)
 	{
 		final Logger logger = Logger.getLogger("com.redhat.automationportal");
 
 		try
 		{
 			logger.info("-> AutomationPortalREST.BugzillaReportGeneratorGetJson()");
 			
			final String test = PropertyUtils.getProperty(Constants.ERROR_FPROPERTY_FILENAME, "AMPT0001");
			
 			final BugzillaReportGenerator script = new BugzillaReportGenerator();
 			script.setBugzillaPassword(bugzillaPassword);
 			script.setBugzillaUsername(bugzillaUsername);
 			final boolean result = script.run();
 
 			final String message = script.getMessage();
 		
 			return Response.status(result ? 200 : 500).entity(message).build();
 		}
 		finally
 		{
 			logger.info("<- AutomationPortalREST.BugzillaReportGeneratorGetJson()");
 		}
 
 	}
 }
