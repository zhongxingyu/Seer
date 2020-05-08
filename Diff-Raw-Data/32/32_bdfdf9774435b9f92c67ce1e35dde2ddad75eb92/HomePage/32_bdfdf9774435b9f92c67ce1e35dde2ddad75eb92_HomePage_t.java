 package com.xaf.ace.page;
 
 import java.io.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.util.*;
 
 import com.xaf.*;
 import com.xaf.ace.*;
 import com.xaf.page.*;
 
 public class HomePage extends AceServletPage
 {
 	public final String getName() { return "home"; }
 	public final String getPageIcon() { return "home.gif"; }
 	public final String getCaption(PageContext pc) { return "ACE Home"; }
 	public final String getHeading(PageContext pc) { return "Welcome to ACE"; }
 
 	public void handlePageBody(PageContext pc) throws ServletException, IOException
 	{
 		String sharedImagesRootURL = ((PageControllerServlet) pc.getServlet()).getSharedImagesRootURL();
 		try
 		{
 			PrintWriter out = pc.getResponse().getWriter();
 
 			out.print("<table cool border='0' cellpadding='0' cellspacing='0'>");
 			out.print("<tr height='304'>");
			out.print("	<td width='3590' height='304' colspan='2' valign='top' align='left' xpos='0'><img src='"+ sharedImagesRootURL +"/ace/home-main.gif' width='800' height='304' border='0'></td>");
 			out.print("	<td width='1' height='304'><spacer type='block' width='1' height='304'></td>");
 			out.print("</tr>");
 			out.print("<tr height='214'>");
 			out.print("	<td width='269' height='214' valign='top' align='left' xpos='0'>");
			out.print("		<table border='0' cellpadding='0' cellspacing='0' width='269' height='214' background='"+ sharedImagesRootURL +"/ace/home-bottom-left.gif' align='left'>");
 			out.print("			<tr>");
 			out.print("				<td valign='top' align='left'>");
 			out.print("					<div align='right'>");
 			out.print("						<table border='0' cellpadding='0' cellspacing='0' height='45' width='269'>");
 			out.print("							<tr>");
 			out.print("								<td valign='middle' align='right' class='list'>");
 			out.print("									<div align='right'>");
 			out.print("										<span class='dialog_control'><b>"+ BuildConfiguration.getProductBuild() +"</b><br>");
 			out.print("											");
 			out.print("											 by Netspective Corp.</span></div>");
 			out.print("								</td>");
 			out.print("								<td valign='middle' align='right' width='102'><img src='"+ sharedImagesRootURL +"/ace/spacer.gif' width='1' height='1' border='0'></td>");
 			out.print("							</tr>");
 			out.print("						</table>");
 			out.print("						<table border='0' cellpadding='0' cellspacing='0' height='45' width='269'>");
 			out.print("							<tr>");
 			out.print("								<td valign='middle' align='right' class='list'>");
 			out.print("									<div align='right'>");
 			out.print("										");
 			out.print("										Running on "+ System.getProperty("os.name") +" v. "+ System.getProperty("os.version") +"<br>");
 			out.print("										");
 			out.print("										 Java Version "+ System.getProperty("java.version") +"<br>");
 			out.print("										");
 			out.print("										 by "+ System.getProperty("java.vendor") +"</div>");
 			out.print("								</td>");
 			out.print("								<td valign='middle' align='right' width='82'><img src='"+ sharedImagesRootURL +"/ace/spacer.gif' width='1' height='1' border='0'></td>");
 			out.print("							</tr>");
 			out.print("						</table>");
 			out.print("						<table border='0' cellpadding='0' cellspacing='0' height='45' width='269'>");
 			out.print("							<tr>");
 			out.print("								<td valign='middle' align='right' class='list'>");
 			out.print("									<div align='right'>");
 			out.print("										");
 			out.print("										"+System.getProperty("java.vm.name")+"<br>");
 			out.print("										");
 			out.print("										Version "+System.getProperty("java.vm.version")+"<br>");
 			out.print("										");
 			out.print("										by "+System.getProperty("java.vm.vendor")+"</div>");
 			out.print("								</td>");
 			out.print("								<td valign='middle' align='right' width='50'><img src='"+ sharedImagesRootURL +"/ace/spacer.gif' width='1' height='1' border='0'></td>");
 			out.print("							</tr>");
 			out.print("						</table>");
 			out.print("					</div>");
 			out.print("				</td>");
 			out.print("			</tr>");
 			out.print("		</table>");
 			out.print("	</td>");
			out.print("	<td width='3321' height='214' valign='top' align='left' xpos='269'><img src='"+ sharedImagesRootURL +"/ace/home-bottom-right.gif' width='531' height='214' border='0'></td>");
 			out.print("	<td width='1' height='214'><spacer type='block' width='1' height='214'></td>");
 			out.print("</tr>");
 			out.print("<tr height='8567'>");
 			out.print("	<td width='3591' height='8568' colspan='3' rowspan='2' valign='top' align='left' xpos='0'>");
 			out.print("		<table border='0' cellpadding='0' cellspacing='0' width='100%' background='"+ sharedImagesRootURL +"/ace/2tone.gif' height='100%'>");
 			out.print("			<tr>");
 			out.print("				<td><img src='"+ sharedImagesRootURL +"/ace/spacer.gif' width='1' height='1' border='0'></td>");
 			out.print("			</tr>");
 			out.print("		</table>");
 			out.print("	</td>");
 			out.print("</tr>");
 			out.print("<tr height='1' cntrlrow></tr>");
 			out.print("</table>");
 		}
 		catch(IOException e)
 		{
 			throw new ServletException(e);
 		}
 	}
 }
