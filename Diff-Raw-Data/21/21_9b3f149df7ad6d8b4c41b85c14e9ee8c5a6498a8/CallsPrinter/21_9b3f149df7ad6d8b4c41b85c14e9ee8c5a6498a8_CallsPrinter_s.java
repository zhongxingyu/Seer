 // ========================================================================
 // Copyright 2009 NEXCOM Systems
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
 import java.util.Iterator;
 import java.util.List;
 
 import javax.management.MBeanServerConnection;
 import javax.management.ObjectName;
 
 import org.cipango.console.ConsoleFilter;
 
 
 public class CallsPrinter implements HtmlPrinter
 {
 	private static final int MAX_DISPLAY = 100;
 	private String _callId;
 	private MBeanServerConnection _connection;
 	
 	public CallsPrinter(MBeanServerConnection connection, String callId)
 	{
 		_callId = callId;
 		_connection = connection;
 	}
 
 	@SuppressWarnings("unchecked")
 	public void print(Writer out) throws Exception
 	{
 		ObjectName sessionManager = (ObjectName) _connection.getAttribute(ConsoleFilter.SERVER, "sessionManager");
 		List<String> callIds = (List<String>) _connection.getAttribute(sessionManager, "callIds");
 		if (callIds != null)
 		{
 			out.append("<h2>List of call-IDs saved in cache</h2>");
 			out.append("Call-IDs " + Math.min(1, callIds.size())+  " to ");
 			out.append(String.valueOf(Math.min(callIds.size(), MAX_DISPLAY)));
 			out.append(" for a total of ").append(String.valueOf(callIds.size())).append("<br/>");
 			Iterator<String> it = callIds.iterator();
 			int i = 1;
 			while (it.hasNext())
 			{
 				String callId = (String) it.next();
 				out.append("<A href=\"").append(MenuPrinter.CALLS.getName());
				out.append("?callID=").append(callId).append("\">");
 				out.append(callId).append("</A>");
 				if (++i % 5 == 0)
 					out.append("<br/>");
 				else if (i >= MAX_DISPLAY)
 					break;
 				else
 					if (it.hasNext())
 					out.append("&nbsp;");
 			}
 			
 		}
 		if (_callId != null)
 		{
 			String call = (String) _connection.invoke(sessionManager, 
 					"viewCall",
 					new Object[] { _callId }, 
 					new String[] { "java.lang.String" });
 			out.append("<h2>Call with call-ID ").append(_callId).append("</h2>");
 			if (call != null)
 			{
 				call = call.replaceAll("<", "&lt;");
 				call = call.replaceAll(">", "&gt;");
 			}
 			out.append("<pre>").append(call).append("</pre>");
 			
 		}
 	}

 }
