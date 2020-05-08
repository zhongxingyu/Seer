 package com.ifsoft.redfire.servlets;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 
 import java.io.*;
 import java.util.*;
 import org.jivesoftware.util.JiveGlobals;
 
 import org.jivesoftware.openfire.XMPPServer;
 import org.jivesoftware.util.*;
 
 
 public class JnlpServlet extends HttpServlet {
 
 	public static final long serialVersionUID = 24362462L;
 
 	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 		response.setContentType("application/x-java-jnlp-file");
 		response.setHeader("Content-Disposition","Inline; filename=screencast.jnlp");
 
         try {
 
 			ServletOutputStream out = response.getOutputStream();
 
 			String stream = request.getParameter("stream");
 			String app = request.getParameter("app");
 			String port = request.getParameter("port");
 			String screenCodec = request.getParameter("codec");
 			String frameRate = request.getParameter("frameRate");
 			String maxWidth = request.getParameter("maxWidth");
 			String maxHeight = request.getParameter("maxHeight");
 
 			if (stream == null) {
 				stream = "screen_share";
 			}
 
 			if (app == null) {
 				app = "inspired";
 			}
 
 			if (port == null) {
 				port = "1935";
 			}
 
 			if (screenCodec == null) {
 				screenCodec = "flashsv2";
 			}
 
 			if (frameRate == null) {
 				frameRate = "15";
 			}
 
 			if (maxWidth == null) {
 				maxWidth = "1024";
 			}
 
 			if (maxHeight == null) {
 				maxHeight = "768";
 			}
 
 			out.println("<?xml version='1.0' encoding='utf-8'?>");
			out.println("<jnlp spec='1.0+' codebase='" + request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/redfire/screen'> ");
 			out.println("	<information> ");
 			out.println("		<title>Redfire ScreenShare</title> ");
 			out.println("		<vendor>Dele Olajide</vendor> ");
 			out.println("		<homepage>http://code.google.com/p/inspired</homepage>");
 			out.println("		<description>Inspired ScreenShare Client Application</description> ");
 			out.println("		<description kind='short'>An Java Webstart application that publishes desktop screen as RTMP video stream</description> ");
 			out.println("		<offline-allowed/> ");
 			out.println("	</information>");
 			out.println("	<security>");
 			out.println("		<all-permissions/>");
 			out.println("	</security>	");
 			out.println("	<resources> ");
 			out.println("	<j2se version='1.4+'/> ");
 			out.println("		<jar href='screenshare.jar'/> ");
 			out.println("	</resources> ");
 			out.println("	<application-desc main-class='org.redfire.screen.ScreenShare'>");
 			out.println("		<argument>" + request.getServerName() + "</argument>");
 			out.println("		<argument>" + app + "</argument>");
 			out.println("		<argument>" + port + "</argument>");
 			out.println("		<argument>" + stream + "</argument> ");
 			out.println("		<argument>" + screenCodec + "</argument> ");
 			out.println("		<argument>" + frameRate + "</argument> ");
 			out.println("		<argument>" + maxWidth + "</argument> ");
 			out.println("		<argument>" + maxHeight + "</argument> ");
 			out.println("	</application-desc> ");
 			out.println("</jnlp>");
 			        }
         catch (Exception e) {
         }
 	}
 }
