 package com.xaf.ace;
 
 import java.io.*;
 import java.lang.reflect.*;
 import java.util.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import javax.xml.parsers.*;
 
 import org.w3c.dom.*;
 
 import com.xaf.config.*;
 import com.xaf.form.*;
 import com.xaf.html.*;
 import com.xaf.html.component.*;
 import com.xaf.page.*;
 import com.xaf.security.*;
 import com.xaf.skin.*;
 import com.xaf.transform.*;
 import com.xaf.value.*;
 
 public class AceServletPage extends AbstractServletPage
 {
 	static public final String ACE_TESTITEM_ATTRNAME = "ace-test-item";
 	static public final String ACE_CONFIG_ITEMS_PREFIX = "framework.ace.";
 	static public final String ACE_CONFIG_ITEM_PROPBROWSERXSL = ACE_CONFIG_ITEMS_PREFIX + "properties-browser-xsl";
 
 	public String getTitle(PageContext pc)
 	{
 		return "ACE - " + getHeading(pc);
 	}
 
 	public String getTestCommandItem(PageContext pc)
 	{
 		return (String) pc.getRequest().getAttribute(ACE_TESTITEM_ATTRNAME);
 	}
 
 	public void addText(Element parent, String elemName, String text)
 	{
 		Document doc = parent.getOwnerDocument();
 		Element elemNode = doc.createElement(elemName);
 		Text textNode = doc.createTextNode(text);
 		elemNode.appendChild(textNode);
 		parent.appendChild(elemNode);
 	}
 
 	public void transform(PageContext pc, Document doc, String styleSheetConfigName) throws IOException
 	{
 		AppComponentsExplorerServlet servlet = ((AppComponentsExplorerServlet) pc.getServlet());
 		Hashtable styleSheetParams = servlet.getStyleSheetParams();
 
 		if(styleSheetParams.get("config-items-added") == null)
 		{
 			Configuration appConfig = ((PageControllerServlet) pc.getServlet()).getAppConfig();
 			for(Iterator i = appConfig.entrySet().iterator(); i.hasNext(); )
 			{
 				Map.Entry configEntry = (Map.Entry) i.next();
 
 				if(configEntry.getValue() instanceof Property)
 				{
 					Property property = (Property) configEntry.getValue();
 					String propName = property.getName();
 					styleSheetParams.put(propName, appConfig.getValue(pc, propName));
 				}
 			}
 			styleSheetParams.put("config-items-added", new Boolean(true));
 		}
 
 		styleSheetParams.put("root-url", pc.getActivePath().getMatchedPath().getAbsolutePath(pc));
 		styleSheetParams.put("page-heading", getHeading(pc));
 
 		styleSheetParams.remove("detail-type");
 		styleSheetParams.remove("detail-name");
 		styleSheetParams.remove("sub-detail-name");
 
 		VirtualPath.FindResults results = pc.getActivePath();
 		String[] unmatchedItems = results.unmatchedPathItems();
 		if(unmatchedItems != null)
 		{
 			if(unmatchedItems.length > 0)
 				styleSheetParams.put("detail-type", unmatchedItems[0]);
 			if(unmatchedItems.length > 1)
 				styleSheetParams.put("detail-name", unmatchedItems[1]);
 			if(unmatchedItems.length > 2)
 				styleSheetParams.put("sub-detail-name", unmatchedItems[2]);
 		}
 
 		String styleSheet = servlet.getAppConfig().getValue(pc, styleSheetConfigName);
         PrintWriter out = pc.getResponse().getWriter();
 		out.write(Transform.nodeToString(styleSheet, doc, styleSheetParams));
 	}
 
 	public void handlePageMetaData(PageContext pc) throws ServletException, IOException
 	{
 		if(getTestCommandItem(pc) != null)
 			return;
 
 		String sharedScriptsRootURL = ((PageControllerServlet) pc.getServlet()).getSharedScriptsRootURL();
 		String sharedCssRootURL = ((PageControllerServlet) pc.getServlet()).getSharedCssRootURL();
 
 		HierarchicalMenu.DrawContext dc = new HierarchicalMenu.DrawContext();
 		pc.getRequest().setAttribute(HierarchicalMenu.DrawContext.class.getName(), dc);
 
 
 		Component[] menus = ((AppComponentsExplorerServlet) pc.getServlet()).getMenuBar();
 		try
 		{
 			PrintWriter out = pc.getResponse().getWriter();
 			out.print("<head>\n");
 			out.print("<title>");
 			out.print(getTitle(pc));
 			out.print("</title>\n");
 			out.print("<link rel='stylesheet' href='"+ sharedCssRootURL+ "/ace.css'>\n");
 			for(int i = 0; i < menus.length; i++)
 			{
 				dc.firstMenu = i == 0;
 				dc.lastMenu = i == (menus.length - 1);
 				menus[i].printHtml(pc, out);
 			}
 			out.print("</head>\n\n");
 		}
 		catch(IOException e)
 		{
 			throw new ServletException(e);
 		}
 	}
 
 	public void handlePageHeader(PageContext pc) throws ServletException, IOException
 	{
 		if(getTestCommandItem(pc) != null)
 			return;
 
 		AppComponentsExplorerServlet servlet = (AppComponentsExplorerServlet) pc.getServlet();
 		String sharedImagesRootURL = servlet.getSharedImagesRootURL();
 		String homeUrl = servlet.getHomePath().getAbsolutePath(pc);
 
 		try
 		{
 			PrintWriter out = pc.getResponse().getWriter();
 			out.print("<body TOPMARGIN='0' LEFTMARGIN='0' MARGINWIDTH='0' MARGINHEIGHT='0' bgcolor='white'>");
 			out.print("<map name='menu_map'>");
 			out.print(" <area shape='rect' coords='14,19,76,41' href='"+ homeUrl +"'>");
 			out.print(" <area shape='rect' coords='79,20,140,41' onMouseOver=\"popUp('HM_Menu2',event)\" onMouseOut=\"popDown('HM_Menu2')\">");
 			out.print(" <area shape='rect' coords='144,21,207,41' onMouseOver=\"popUp('HM_Menu3',event)\" onMouseOut=\"popDown('HM_Menu3')\">");
 			out.print(" <area shape='rect' coords='211,21,275,41' onMouseOver=\"popUp('HM_Menu4',event)\" onMouseOut=\"popDown('HM_Menu4')\">");
 			out.print("</map>");
 			out.print("<table border='0' cellpadding='0' cellspacing='0' height='44'>");
 			out.print("	<tr>");
			out.print("		<td><a href='"+homeUrl+"'><img src='"+ sharedImagesRootURL +"/ace/Homepage_01.gif' width='158' height='44' border='0'></a></td>");
 			out.print("		<td><img src='"+ sharedImagesRootURL +"/ace/masthead.gif' width='642' height='44' border='0' usemap='#menu_map'></td>");
 			out.print("	</tr>");
 			out.print("</table>");
 			out.print("<table border='0' cellpadding='0' cellspacing='0' height='38' width='100%'>");
 			out.print("	<tr>");
			out.print("		<td width='107'><img src='"+ sharedImagesRootURL +"/ace/Homepage_03.gif' width='107' height='38' border='0'></td>");
			out.print("		<td width='32' bgcolor='white' valign='middle' align='center'><img src='"+ sharedImagesRootURL +"/ace/page-icons/"+ getPageIcon() +"' width='32' height='32' border='0'></td>");
			out.print("		<td width='18'><img src='"+ sharedImagesRootURL +"/ace/Homepage_05.gif' width='18' height='38' border='0'></td>");
 			out.print("		<td background='"+ sharedImagesRootURL +"/ace/2tone.gif' class='subheads' valign='middle' align='left'><nobr>"+ getHeading(pc) +"</nobr></td>");
			out.print("		<td background='"+ sharedImagesRootURL +"/ace/2tone.gif' class='subheads' valign='middle' align='left' width='166'><img src='"+ sharedImagesRootURL +"/ace/Homepage_20.gif' width='166' height='38' border='0'></td>");
 			out.print("	</tr>");
 			out.print(" <tr bgcolor='#003366' height='2'><td bgcolor='#003366' height='2' colspan='5'></td></tr>");
 			out.print("</table>");
 		}
 		catch(IOException e)
 		{
 			throw new ServletException(e);
 		}
 	}
 
 	public void handlePage(PageContext pc) throws ServletException
 	{
 		VirtualPath.FindResults results = pc.getActivePath();
 		String[] unmatchedItems = results.unmatchedPathItems();
 		if(unmatchedItems != null && unmatchedItems.length >= 2 && unmatchedItems[0].equals("test"))
 			pc.getRequest().setAttribute(ACE_TESTITEM_ATTRNAME, unmatchedItems[1]);
 
 		super.handlePage(pc);
 	}
 }
