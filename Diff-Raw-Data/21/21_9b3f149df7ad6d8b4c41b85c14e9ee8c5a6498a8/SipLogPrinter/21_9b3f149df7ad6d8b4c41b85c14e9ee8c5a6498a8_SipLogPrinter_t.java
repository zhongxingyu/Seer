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
 import java.lang.reflect.Method;
 import java.util.Enumeration;
 import java.util.ResourceBundle;
 
 import javax.management.MBeanServerConnection;
 import javax.management.ObjectName;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.sip.SipServletMessage;
 import javax.servlet.sip.SipServletRequest;
 
 import org.cipango.console.ConsoleFilter;
 import org.cipango.console.Parameters;
 import org.cipango.console.printer.MenuPrinter.Page;
 
 public class SipLogPrinter extends AbstractLogPrinter
 {
 	private static final String[] GET_MSG_SIGNATURE =
 	{ Integer.class.getName(), String.class.getName() };
 
 	private static final ResourceBundle FILTERS = ResourceBundle
 			.getBundle("org.cipango.console.sip-filters");
 
 	private static final String 
 		CALL_ID_FILTER = "message.callId",
 		BRANCH_FILTER = "message.topVia.branch",
 		TO_FILTER = "message.to.uRI.toString()",
 		FROM_FILTER = "message.from.uRI.toString()",
 		REMOTE_FILTER = "remote",
 		REQUEST_URI_FILTER = "message.requestURI != null and message.requestURI.toString()";
 	
 	private Object[][] _messagesLogs;
 	private Output _output;
 
 	public SipLogPrinter(MBeanServerConnection connection,
 			HttpServletRequest request, Output output) throws Exception
 	{
 		super(connection, request);
 		_output = output;
 
 		if (isLoggerRunning())
 		{
 			Object[] params =
 			{ new Integer(_maxMessages), _msgFilter };
 			_messagesLogs = (Object[][]) connection.invoke(
 					ConsoleFilter.SIP_CONSOLE_MSG_LOG, "getMessages", params,
 					GET_MSG_SIGNATURE);
 		}
 	}
 
 	public void print(Writer out) throws Exception
 	{
 		if (_output == Output.TEXT)
 		{
 			if (_messagesLogs == null)
 				out.write("JMX message logger is not configured");
 			else
 			{
 				for (int i = 0; i < _messagesLogs.length; i++)
 				{
 					out.write(_messagesLogs[i][0].toString());
 					out.write(_messagesLogs[i][1].toString());
 					out.write('\n');
 				}
 			}
 			return;
 		}
 
 		if (_connection.isRegistered(ConsoleFilter.SIP_MESSAGE_LOG))
 		{
 			out.write("<h2>File Log</h2>");
 			new FileLogPrinter(_connection, getPage(), ConsoleFilter.SIP_MESSAGE_LOG, true).print(out);
 		}
 		if (_connection.isRegistered(getObjectName()))
 		{
 			out.write("<h2>Console Log</h2>");
 			printHeaders(out);
 			if (isLoggerRunning())
 			{
 				printCallflow(out);
 				printConsoleLog(out);
 			}
 		}
 	}
 	
 	@Override
 	public ObjectName getObjectName()
 	{
 		return ConsoleFilter.SIP_CONSOLE_MSG_LOG;
 	}
 
 	protected void printCommonFilters(Writer out) throws Exception
 	{
 		out.write("Filter:&nbsp;" + "<SELECT name=\""
 				+ Parameters.MESSAGE_FILTER + "\">");
 		Enumeration<String> keys = FILTERS.getKeys();
 		boolean selected = false;
 		while (keys.hasMoreElements())
 		{
 			String key = keys.nextElement();
 			if (key.endsWith(".title"))
 			{
 				String title = FILTERS.getString(key);
 				String prefix = key.substring(0, key.length()
 						- ".title".length());
 				String value = FILTERS.getString(prefix + ".filter").trim();
 				out.write("<OPTION value=\"" + value + "\" ");
 				if (value.equals(_msgFilter)
 						|| (value.equals("") && _msgFilter == null))
 				{
 					out.write(" selected");
 					selected = true;
 				}
 				out.write(">" + title + "</OPTION>");
 			}
 		}
 		if (!selected)
 		{
 			out.write("<OPTION value=\"" + _msgFilter + "\" selected>");
 			
 			String filterValue = _msgFilter.substring(_msgFilter.lastIndexOf('(') + 1);
 			filterValue = filterValue.substring(0, filterValue.length() - 1);
 			if (_msgFilter.startsWith(CALL_ID_FILTER))
 				out.write("Call-ID is " + filterValue);
 			else if (_msgFilter.startsWith(BRANCH_FILTER))
 				out.write("Branch is " + filterValue);
 			else if (_msgFilter.startsWith(TO_FILTER))
 				out.write("To URI is " + filterValue);
 			else if (_msgFilter.startsWith(FROM_FILTER))
 				out.write("From URI is " + filterValue);
 			else if (_msgFilter.startsWith(REQUEST_URI_FILTER))
 				out.write("Request-URI is " + filterValue);
 			else if (_msgFilter.startsWith(REMOTE_FILTER))
 				out.write("Remote host is " + filterValue);
 			else
 				out.write(_msgFilter);
 			out.write("</OPTION>");
 		}
 			
 
 		out.write("</SELECT>");
 	}
 	
 
 
 	private void printConsoleLog(Writer out) throws Exception
 	{
 		out.write("<div id=\"messageLog\">");
 		for (int i = 0; i < _messagesLogs.length; i++)
 		{
 			out.write("<a name=\"msg-" + (i + 1) + "\"/><div id=\"log_" + i + "\">");
 			MessageLog log = new MessageLog(_messagesLogs[i]);
 			String info = log.getInfoLine().replaceFirst(log.getRemote(),
 					getFilterLink(REMOTE_FILTER, log.getRemote()));
 			out.write("<div class=\"info\">" + info + "</div>");
 			out.write("<pre class=\"message\">");
 			out.write(sipToHtml(log.getMessage()));
 			out.write("</pre>");
 			out.write("</div>");
 		}
 		out.write("</div>");
 	}
 
 	private void printCallflow(Writer out) throws Exception
 	{
 		if (_messagesLogs != null && _messagesLogs.length > 0)
 		{
 			out.write("<div id=\"callflow\">");
 			out.write("<embed src=\"message.svg?" + Parameters.MAX_MESSAGES
 					+ "=" + _maxMessages);
 			if (_msgFilter != null)
				out.write("&" + Parameters.MESSAGE_FILTER + "=" + encode(_msgFilter));
 			int height = 100 + _messagesLogs.length * 25;
 			out.write("\" width=\"790\" height=\""
 							+ height
 							+ "\" type=\"image/svg+xml\" pluginspage=\"http://www.adobe.com/svg/viewer/install/\"/>");
 			out.write("</div>");
 		}
 	}
	
	private String encode(String unencoded)
	{
		return unencoded.replace("%", "%25");
	}
 
 	private String sipToHtml(SipServletMessage message)
 	{
 		StringBuilder sb = new StringBuilder(message.toString());
 
 		replaceAll(sb, "<", "&lt;");
 		replaceAll(sb, ">", "&gt;");
 		replaceAll(sb, "\"", "&quot;");
 
 		replace(sb, sb.indexOf("Call-ID"), message.getCallId(), CALL_ID_FILTER);
 		
 		try 
 		{
 			// return message.getTopVia().getBranch();
 			Method m = message.getClass().getMethod("getTopVia", new Class[0]);
 			Object o = m.invoke(message, (Object[]) null);
 			m = o.getClass().getMethod("getBranch", new Class[0]);
 			String branch = (String) m.invoke(o, (Object[]) null);
 			
 			replace(sb, sb.indexOf("Via"), branch, BRANCH_FILTER);
 		}
 		catch (Throwable e)
 		{
 			e.printStackTrace();
 		}
 		
 		
 		replace(sb, sb.indexOf("From"), message.getFrom().getURI().toString(), FROM_FILTER);
 		replace(sb, sb.indexOf("To"), message.getTo().getURI().toString(), TO_FILTER);
 		if (message instanceof SipServletRequest)
 			replace(sb, 0,((SipServletRequest) message).getRequestURI().toString(), REQUEST_URI_FILTER);
 
 		return sb.toString();
 	}
 	
 	private void replace(StringBuilder sb, int index, String toChange, String filterId)
 	{
 		replaceOnce(sb, index, toChange, getFilterLink(filterId, toChange));
 	}
 	
 	private String getFilterLink(String name, String value)
 	{
 		StringBuffer sb = new StringBuffer();
 		sb.append("<A href=\"" + MenuPrinter.SIP_LOGS.getName() + "?").append(Parameters.MESSAGE_FILTER).append(
 				"=");
		sb.append(name).append(".equals(%27").append(encode(value)).append("%27)");
 		sb.append("&").append(Parameters.MAX_MESSAGES).append("=").append(
 				_maxMessages);
 		sb.append("\">").append(value).append("</A>");
 		return sb.toString();
 	}
 	
 
 	@Override
 	public Page getPage()
 	{
 		return MenuPrinter.SIP_LOGS;
 	}
 	@Override
 	public String getLogFile()
 	{
 		return "message.log";
 	}
 
 	class MessageLog
 	{
 		private Object[] _array;
 
 		public MessageLog(Object[] array)
 		{
 			_array = array;
 		}
 
 		public String getInfoLine()
 		{
 			return _array[0].toString();
 		}
 
 		public SipServletMessage getMessage()
 		{
 			return (SipServletMessage) _array[1];
 		}
 		
 		public String getRemote()
 		{
 			return (String) _array[2];
 		}
 	}
 }
