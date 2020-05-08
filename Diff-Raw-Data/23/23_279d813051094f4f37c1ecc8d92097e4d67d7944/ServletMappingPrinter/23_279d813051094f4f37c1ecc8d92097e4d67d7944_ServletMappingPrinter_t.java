 // ========================================================================
 // Copyright 2010 NEXCOM Systems
 // ------------------------------------------------------------------------
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at 
 // http://www.apache.org/licenses/LICENSE-2.0
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 // ========================================================================
 package org.cipango.console.printer;
 
 import java.io.Writer;
 
import javax.management.AttributeNotFoundException;
 import javax.management.MBeanServerConnection;
 import javax.management.ObjectName;
 
 public class ServletMappingPrinter implements HtmlPrinter
 {
 
 	private ObjectName[] _appContexts;
 	private MBeanServerConnection _connection;
 
 	public ServletMappingPrinter(ObjectName[] appContexts,
 			MBeanServerConnection connection)
 	{
 		_appContexts = appContexts;
 		_connection = connection;
 	}
 
 	public void print(Writer out) throws Exception
 	{
 		out.write("<h2>Servlets</h2>");
 		for (int i = 0; i < _appContexts.length; i++)
 		{
			
			String contextPath;
			try 
			{
				contextPath = (String) _connection.getAttribute(_appContexts[i], "name");

				ObjectName servletHandler = (ObjectName) _connection.getAttribute(_appContexts[i], "servletHandler");
				printAppContext(contextPath, servletHandler, out);
			}
			catch (AttributeNotFoundException e) {
				contextPath = (String) _connection.getAttribute(_appContexts[i], "contextPath");
				out.write("<h3>" + contextPath + "</h3>\n");
				out.write("No SIP servlets for this application");
			}
 		}
 	}
 
 	private void printAppContext(String name, ObjectName servletHandler,
 			Writer out) throws Exception
 	{
		out.write("<h3>" + name + "</h3>\n");
 		ObjectName[] sipServletMappings = (ObjectName[]) _connection
 				.getAttribute(servletHandler, "sipServletMappings");
 
 		if (sipServletMappings != null && sipServletMappings.length != 0)
 		{
 			out.write("<div class=\"data\"><table class=\"table_hover\">");
 			out.write("<tr><th>SIP Servlet name</th><th>Mapping</th></tr>");
 			for (int i = 0; i < sipServletMappings.length; i++)
 			{
 				String matchingRuleExpression = (String) _connection
 						.getAttribute(sipServletMappings[i],
 								"matchingRuleExpression");
 				String servletName = (String) _connection
 						.getAttribute(sipServletMappings[i], "servletName");
 				out.write("<tr class=\"" + (i % 2 == 0 ? "even" : "odd")
 						+ "\">");
 				out.write("<td>" + servletName + "</td><td>"
 						+ matchingRuleExpression + "</td>");
 				out.write("</tr>");
 			}
 			out.write("</table></div>");
 		}
 		else
 		{
 			ObjectName mainServlet = (ObjectName) _connection.getAttribute(servletHandler, "mainServlet");
 			if (mainServlet != null)
 			{
 				ObjectName[] sipServlets = 
 					(ObjectName[]) _connection.getAttribute(servletHandler, "sipServlets");
 				out.write("<div class=\"data\"><table class=\"table_hover\">\n");
 				out.write("<tr><th>SIP Servlet name</th><th>Main servlet</th></tr>");
 				for (int i = 0; i < sipServlets.length; i++)
 				{
 					String servletName = (String) _connection
 							.getAttribute(sipServlets[i], "name");
 					out.write("<tr class=\"" + (i % 2 == 0 ? "even" : "odd")
 							+ "\">");
 					out.write("<td>" + servletName + "</td><td>"
 							+ sipServlets[i].equals(mainServlet) + "</td>");
 					out.write("</tr>");
 				}
 				out.write("</table></div>");
 			}
 			else
 				out.write("No SIP servlets for this application");
 		}
 	}
 
 }
