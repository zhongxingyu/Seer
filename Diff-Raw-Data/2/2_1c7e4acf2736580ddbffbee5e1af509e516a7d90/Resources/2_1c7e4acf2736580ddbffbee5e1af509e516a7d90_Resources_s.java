 package org.smartflow;
 
 public abstract class Resources {
 	
 	public static final String HTML_START =
 			"<html>" +
 			"<head>"+
 			"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\"/>"	+	
 			"<script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.2.6/jquery.min.js\" type=\"text/javascript\" charset=\"iso-8859-1\"></script>" +
 			"<title>Smart Kitchen Workflow Engine</title>" +
			"<script src=\"client.js\"  type=\"text/javascript\" charset=\"ISO-8859-1\"></script>" +
 		    "</head>" +
 			"<body>" +
 		    "<div align=\"center\" id=\"holder\">";
 		    
 	
 	public static final String HTML_END =
 			
 			"</div><div align=\"center\" id=\"timer\"/></body>" +
 			"</html>";
 	
 	public static final String HTML_WORKFLOW_BUTTONS_START =
 			"<table><tr><td><button onclick=\"pollServer('PREVIOUS')\">Previous</button></td>";
 	
 	public static final String HTML_WORKFLOW_BUTTONS_END=
 			"<td><button onclick=\"pollServer('NEXT')\">Next</button></td></tr></table>";
 	
 	public static final String HTML_START_BUTTON = 
 			"<td><button onclick=\"pollServer('START')\">Start</button></td>";
 		
 	public static final String HTML_STOP_BUTTON = 
 			"<td><button onclick=\"pollServer('STOP')\">Stop</button></td>";
 	
 	public static final String RESPONSE_WELCOME_MESSAGE = "<b>Workflow engine server test </b><BR>";
 	
 	public static final String RESPONSE_CLIENT_REQUEST_MESSAGE = "The HTTP Client request is ....<BR>";
 	
 	public static final String ERROR_404_MESSAGE = "<b>ERROR 404: The requested resource not found</b>";
 	
 }
