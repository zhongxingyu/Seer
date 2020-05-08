 /* OpenMark online assessment system
    Copyright (C) 2007 The Open University
 
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package om.tnavigator.reports;
 
 import java.util.*;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import om.OmException;
 import om.tnavigator.NavigatorServlet;
 import om.tnavigator.UserSession;
 import om.tnavigator.reports.std.*;
 
 /**
  * Class for dispatching report requests.
  */
 public class ReportDispatcher
 {
 	private final static String REPORT_PACKAGE = "om.tnavigator.reports.";
 	private final Map<String,OmReport> systemReports = new HashMap<String,OmReport>();
 	private final Map<String,OmTestReport> testReports = new HashMap<String,OmTestReport>();
 	private static final Class<?>[] standardReports = new Class<?>[] {
 		HomeTestReport.class,
 		UserTestReport.class,
 		QuestionTestReport.class,
 	};
 	
 	private NavigatorServlet ns;
 	
 	/**
 	 * Create a ReportDispatcher to serve a particular NavigatorServlet.
 	 * @param ns The navigator servlet
 	 * @param extraReports 
 	 * @throws OmException 
 	 */
 	public ReportDispatcher(NavigatorServlet ns, Collection<String> extraReports) throws OmException
 	{
 		this.ns = ns;
 		Collection<Class<?>> reportClasses = new LinkedList<Class<?>>(Arrays.asList(standardReports));
 		for (String reportClassName : extraReports)
 		{
 			try
 			{
 				Class<?> reportClass = Class.forName(REPORT_PACKAGE + reportClassName);
 				reportClasses.add(reportClass);
 			}
 			catch (ClassNotFoundException e) {
 				throw new OmException("Cannot find report class " + REPORT_PACKAGE + reportClassName, e);
 			}
 		}
 		for (Class<?> reportClass : reportClasses)
 		{
 			try
 			{
 				if (OmReport.class.isAssignableFrom(reportClass))
 				{
 					OmReport report = reportClass.asSubclass(OmReport.class)
 							.getConstructor(NavigatorServlet.class).newInstance(ns);
 					String urlName = report.getUrlReportName();
 					if (systemReports.containsKey(urlName))
 					{
 						throw new OmException("Report with URL " + urlName + " already registerd.");
 					}
 					systemReports.put(urlName, report);
 					ns.getLog().logDebug("ReportDispatcher", "Registering system report '" +
 							urlName + "' implemented by " + report.getClass().getName() + ".");
 				}
 				if (OmTestReport.class.isAssignableFrom(reportClass))
 				{
 					OmTestReport report = reportClass.asSubclass(OmTestReport.class)
 							.getConstructor(NavigatorServlet.class).newInstance(ns);
 					String urlName = report.getUrlTestReportName();
 					if (testReports.containsKey(urlName))
 					{
 						throw new OmException("Report with URL " + urlName + " already registerd.");
 					}
 					testReports.put(urlName, report);
 					ns.getLog().logDebug("ReportDispatcher", "Registering test report '" +
 							urlName + "' implemented by " + report.getClass().getName() + ".");
 				}
 			}
 			catch (Exception e)
 			{
 				throw new OmException("Cannot create instance of report class " + reportClass.getName(), e);
 			}
 		}
 	}
 
 	/**
 	 * @param suffix
 	 * @param request
 	 * @param response
 	 * @throws Exception
 	 */
 	public void handleReport(String suffix,HttpServletRequest request,HttpServletResponse response)
 		throws Exception
 	{
 		if(!ns.checkSecureIP(request))
 		{
 			ns.sendError(null,request,response,HttpServletResponse.SC_FORBIDDEN,
 				false,null,"Forbidden", "System reports may only be accessed from particular computers.", null);
 		}
 
 		String[] bits = suffix.split("!", 2);
 		String reportName = bits[0];
 		if (bits.length > 1)
 		{
 			suffix = bits[1];
 		}
 		else
 		{
 			suffix = "";
 		}
 		if(systemReports.containsKey(reportName))
 		{
 			systemReports.get(reportName).handleReport(suffix, request, response);
 		}
 		else
 		{
			throw new OmException("'" + suffix + "' is not a valid report name.");
 		}
 	}
 
 	/**
 	 * @param us
 	 * @param suffix 
 	 * @param request
 	 * @param response
 	 * @throws Exception
 	 */
 	public void handleTestReport(UserSession us,String suffix,HttpServletRequest request,HttpServletResponse response)
 		throws Exception
 	{
			
 		// Require admin privileges AND within university (just for paranoia's sake)
 		// AND view report thing
 		if(!us.bAdmin || !us.bAllowReports)
 		{
 			ns.sendError(us,request,response,
 				HttpServletResponse.SC_FORBIDDEN,false,null, "Forbidden", "You do not have permission to view reports.", null);
 		}
 		if(!ns.checkLocalIP(request))
 		{
 			ns.sendError(us,request,response,HttpServletResponse.SC_FORBIDDEN,
 				false,null,"Forbidden", "Reports may only be accessed within the local network.", null);
 		}
 
 		String[] bits = suffix.split("!", 2);
 		String reportName = bits[0];
 		if (bits.length > 1)
 		{
 			suffix = bits[1];
 		}
 		else
 		{
 			suffix = "";
 		}
		if(systemReports.containsKey(reportName))
 		{
			systemReports.get(reportName).handleReport(suffix, request, response);
 		}
 		else
 		{
			throw new OmException("'" + suffix + "' is not a valid report name.");
 		}
 	}
 }
