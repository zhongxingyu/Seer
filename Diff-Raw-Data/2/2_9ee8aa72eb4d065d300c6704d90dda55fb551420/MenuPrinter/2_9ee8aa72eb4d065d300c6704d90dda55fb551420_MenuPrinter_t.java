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
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.management.MBeanAttributeInfo;
 import javax.management.MBeanInfo;
 import javax.management.MBeanServerConnection;
 import javax.management.ObjectName;
 
 import org.cipango.console.ConsoleFilter;
 import org.cipango.console.Menu;
 import org.cipango.console.ObjectNameFactory;
 import org.cipango.console.Page;
 import org.cipango.console.PageImpl;
 import org.eclipse.jetty.util.log.Log;
 import org.eclipse.jetty.util.log.Logger;
 
 public class MenuPrinter implements HtmlPrinter, Menu
 {
 
 	private static final PageImpl PAGES = new PageImpl("");
 
 	public static final PageImpl 
 		SERVER = PAGES.add(new PageImpl("Server")),
 		ABOUT = SERVER.add(new PageImpl("about", "About")),
 		SYSTEM_PROPERTIES = SERVER.add(new PageImpl("system-properties", "System Properties")),
 		
 		CONFIG = PAGES.add(new PageImpl("Configuration")),
 		CONFIG_SIP = CONFIG.add(new PageImpl("configuration-sip", "SIP Configuration", "SIP")
 		{
 			@Override
 			public boolean isEnabled(MBeanServerConnection c) throws IOException
 			{
 				return c.isRegistered(ConsoleFilter.CONNECTOR_MANAGER);
 			}
 		}),
 		CONFIG_HTTP = CONFIG.add(new PageImpl("configuration-http", "HTTP Configuration", "HTTP")),
 		CONFIG_DIAMETER = CONFIG.add(new PageImpl("configuration-diameter", "Diameter Configuration", "Diameter")
 		{
 			@Override
 			public boolean isEnabled(MBeanServerConnection c) throws IOException
 			{
 				return c.isRegistered(ConsoleFilter.DIAMETER_NODE);
 			}
 		}),
 		CONFIG_SNMP = CONFIG.add(new PageImpl("configuration-snmp", "SNMP Configuration", "SNMP")
 		{
 			@Override
 			public boolean isEnabled(MBeanServerConnection c) throws IOException
 			{
 				return c.isRegistered(ConsoleFilter.SNMP_AGENT);
 			}
 		}),
 		
 		STATISTICS = PAGES.add(new PageImpl("Statistics")),
 		STATISTICS_SIP = STATISTICS.add(new PageImpl("statistics-sip", "SIP Statistics", "SIP")),
 		STATISTICS_HTTP = STATISTICS.add(new PageImpl("statistics-http", "HTTP Statistics", "HTTP")),
 		STATISTICS_DIAMETER = STATISTICS.add(new PageImpl("statistics-diameter", "Diameter Statistics", "Diameter"){
 			@Override
 			public boolean isEnabled(MBeanServerConnection c) throws IOException
 			{
 				return c.isRegistered(ConsoleFilter.DIAMETER_NODE);
 			}
 		}),
 		
 		LOGS = PAGES.add(new PageImpl("Logs")),
 		SIP_LOGS = LOGS.add(new PageImpl("logs-sip", "SIP Logs", "SIP"){
 			@Override
 			public boolean isEnabled(MBeanServerConnection c) throws IOException
 			{
 				return c.isRegistered(ConsoleFilter.SIP_CONSOLE_MSG_LOG)
 							|| c.isRegistered(ConsoleFilter.SIP_MESSAGE_LOG);
 			}
 		}),
 		HTTP_LOGS = LOGS.add(new PageImpl("logs-http", "HTTP Logs", "HTTP")),
 		DIAMETER_LOGS = LOGS.add(new PageImpl("logs-diameter", "Diameter Logs", "Diameter")
 		{
 			@Override
 			public boolean isEnabled(MBeanServerConnection c) throws IOException
 			{
 				return c.isRegistered(ConsoleFilter.DIAMETER_NODE);
 			}
 		}),
 		CALLS = LOGS.add(new PageImpl("logs-calls", "Calls")),
 
 		APPLICATIONS = PAGES.add(new PageImpl("Applications")
 		{
 			@Override
 			public boolean isEnabled(MBeanServerConnection c) throws IOException
 			{
 				return !c.isRegistered(ObjectNameFactory.create("org.cipango.console:page-disabled=application"));
 			}
 		}),
 		MAPPINGS = APPLICATIONS.add(new PageImpl("applications", "Applications Mapping")
 		{
 			@Override
 			public boolean isEnabled(MBeanServerConnection c) throws IOException
 			{
				return getFather().isEnabled(c);
 			}
 		}),
 		DAR = APPLICATIONS.add(new PageImpl("dar", "Default Application Router", "DAR")
 		{
 			@Override
 			public boolean isEnabled(MBeanServerConnection c) throws IOException
 			{
 				return getFather().isEnabled(c) && c.isRegistered(ConsoleFilter.DAR);
 			}
 		});
 	
 	
 	protected MBeanServerConnection _connection;
 	protected PageImpl _currentPage;
 	protected List<PageImpl> _pages;
 	private static Logger _logger = Log.getLogger("console");
 	protected String _contextPath;
 
 	public MenuPrinter(MBeanServerConnection c, String command, String contextPath)
 	{
 		_connection = c;
 		_pages = getPages();
 		_contextPath = contextPath;
 		Iterator<PageImpl> it = _pages.iterator();
 		while (it.hasNext())
 		{
 			PageImpl subPage = getPage(command, it.next());
 			
 			if (subPage != null)
 			{
 				_currentPage = subPage;
 				break;
 			}
 		}
 	}
 	
 	private PageImpl getPage(String command, PageImpl page)
 	{
 		Iterator<PageImpl> it = page.getPages().iterator();
 		while (it.hasNext())
 		{
 			PageImpl subPage = getPage(command, it.next());
 			if (subPage != null)
 				return subPage;
 		}
 
 		if (command != null && command.equals(page.getName()))
 			return page;
 		
 		return null;
 	}
 	
 	
 	public Page getCurrentPage()
 	{
 		return _currentPage;
 	}
 	
 	public String getTitle()
 	{
 		return _currentPage.getTitle();
 	}
 		
 	public String getHtmlTitle()
 	{
 		if (_currentPage.getFather() == null)
 			return "<h1>" + _currentPage.getTitle() + "</h1>";
 		else
 			return "<h1>" + _currentPage.getFather().getTitle() + 
 					"<span> > " + _currentPage.getMenuTitle() + "</span></h1>";
 	}
 
 	public void print(Writer out) throws Exception
 	{
 		out.write("<div id=\"menu\">\n");
 		out.write("<ul>\n");
 		Iterator<PageImpl> it = _pages.iterator();
 		while (it.hasNext())
 			print(out, it.next());
 		out.write("</u1>\n");
 		out.write("</div>\n");
 	}
 	
 	public void print(Writer out, PageImpl page) throws Exception
 	{
 		if (page.isEnabled(_connection))
 		{
 			out.write("<li>");
 			out.write("<a href=\"" + _contextPath + "/" + page.getLink(_connection) + "\"");
 			if (page == _currentPage || page == _currentPage.getFather())
 				out.write("class=\"selected\"");
 			out.write("><span>" + page.getMenuTitle() + "</span></a>\n");
 			out.write("</li>\n");
 		}
 	}
 	
 	public HtmlPrinter getSubMenu()
 	{
 		return new HtmlPrinter()
 		{
 			
 			public void print(Writer out) throws Exception
 			{
 				out.write("<div id=\"submenu\">\n<ul>");
 				if (_currentPage.getFather() != null)
 				{	
 					Iterator<PageImpl> it = _currentPage.getFather().getPages().iterator();
 					while (it.hasNext())
 						MenuPrinter.this.print(out, it.next());
 				}
 				out.write("</ul>\n</div>\n");
 			}
 		};
 	}
 
 	protected List<PageImpl> getPages()
 	{
 		List<PageImpl> l = new ArrayList<PageImpl>(PAGES.getPages());
 				
 		try
 		{
 			@SuppressWarnings("unchecked")
 			Set<ObjectName> set = _connection.queryNames(ConsoleFilter.APPLICATION_PAGES, null);
 			if (set != null && !set.isEmpty())
 			{
 				Iterator<ObjectName> it = set.iterator();
 				while (it.hasNext())
 				{
 					ObjectName objectName = it.next();
 					PageImpl page = getDynamicPage(objectName);
 					addDynamicSubPages(objectName, page);
 					l.add(page);
 				}
 			}
 		}
 		catch (Exception e) 
 		{
 			_logger.warn("Unable to get applications pages", e);
 		}
 		
 		return l;
 	}
 	
 	private PageImpl getDynamicPage(ObjectName objectName) throws Exception
 	{
 		String name = objectName.getKeyProperty("page");
 		MBeanInfo info = _connection.getMBeanInfo(objectName);
 		String title = null;
 		String menuTitle = null;
 		for (int i = 0; i < info.getAttributes().length; i++)
 		{
 			MBeanAttributeInfo attr = info.getAttributes()[i];
 			if ("Title".equals(attr.getName()) && attr.isReadable())
 				title = (String) _connection.getAttribute(objectName, attr.getName());
 			if ("MenuTitle".equals(attr.getName()) && attr.isReadable())
 				menuTitle = (String) _connection.getAttribute(objectName, attr.getName());
 
 		}
 		PageImpl page = new PageImpl(name, title, menuTitle);
 		page.setObjectName(objectName);
 		return page;
 	}
 	
 	private void addDynamicSubPages(ObjectName objectName, PageImpl page) throws Exception
 	{
 		MBeanInfo info = _connection.getMBeanInfo(objectName);
 
 		for (int i = 0; i < info.getAttributes().length; i++)
 		{
 			MBeanAttributeInfo attr = info.getAttributes()[i];
 			if ("SubPages".equals(attr.getName()) && attr.isReadable())
 			{
 				ObjectName[] subPages = (ObjectName[]) _connection.getAttribute(objectName, attr.getName());
 				if (subPages != null)
 				{
 					for (ObjectName name : subPages)
 						page.add(getDynamicPage(name));
 				}
 				
 				break;
 			}
 		}
 	}
 	
 }
