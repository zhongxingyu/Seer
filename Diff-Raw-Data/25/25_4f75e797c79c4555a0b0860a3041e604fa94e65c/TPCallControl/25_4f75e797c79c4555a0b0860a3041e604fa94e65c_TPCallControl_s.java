 /*
  * Copyright 2007 Sun Microsystems, Inc.
  *
  * This file is part of jVoiceBridge.
  *
  * jVoiceBridge is free software: you can redistribute it and/or modify 
  * it under the terms of the GNU General Public License version 2 as 
  * published by the Free Software Foundation and distributed hereunder 
  * to you.
  *
  * jVoiceBridge is distributed in the hope that it will be useful, 
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Sun designates this particular file as subject to the "Classpath"
  * exception as provided by Sun in the License file that accompanied this 
  * code. 
  */
 
 package com.sun.voip.servlet;
 
 import com.sun.voip.CallParticipant;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import java.io.*;
 import java.util.*;
 import java.net.*;
 
 /**
  * This class is a servlet which accepts requests from an http clients
  * and initiates calls as specified by the clients' request.
  */
 public class TPCallControl extends HttpServlet {
     private String getConferenceInfo;		   // request to get conf. info
 
     private String hangup;			   // request to end call(s)
 
     private PrintWriter out;
 
     public TPCallControl() {
     }
         
     /**
      * Write log messages to System.err.  This goes to the apache 
      * tomcat log file in /usr/apache/tomcat/logs/catalina.out on the
      * server running this servlet.
      */ 
     private void println(String s) {
         System.err.println(s);
         System.err.flush();
     }
 
     /**
      * This is the servlet entry point.
      * <p>
      * @param req the HttpServletRequest with client's request parameters
      * @param res the HttpServletResponse with the html response to the client
      */ 
     public void doGet(HttpServletRequest req, HttpServletResponse res)
         throws ServletException, IOException {
 
         res.setContentType("text/html");
         out = res.getWriter();
 
 	/*
 	 * Handle special requests to hangup, get active calls or 
 	 * conference information.
 	 */
 	hangup = req.getParameter("hangup");
 
 	if (hangup != null) {
             sendRequest("cancel=" + hangup +  "\n" + "detach\n\n");
             out.println("</BODY></HTML>");
             return;
 	}
 
 	getConferenceInfo = req.getParameter("getConferenceInfo");
 
 	if (getConferenceInfo != null) {
             /*
              * This is a request for information on all conferences
              */ 
             sendRequest("conferenceInfo\ndetach\n");
             out.println("</BODY></HTML>");
             return;
 	}
 
 	CallParticipant cp = new CallParticipant();
 
         cp.setPhoneNumber(req.getParameter("firstPartyNumber"));
         cp.setName(req.getParameter("firstPartyName"));
 
 	String treatment = req.getParameter("firstPartyTreatment");
 
 	if (treatment == null && req.getParameter("secondPartyNumber") != null) {
 	    treatment = "please_wait.au,ring_tone.au";
 	}
 
 	cp.setCallAnsweredTreatment(treatment);
 
 	String firstPartyConfirmation = 
 	    req.getParameter("firstPartyConfirmation");
 
 	if (firstPartyConfirmation != null && 
 		firstPartyConfirmation.equalsIgnoreCase("true")) {
 
 	    /*
 	     * Set default parameters for first party configuration.
 	     * When the first party answers the call, they will have
 	     * to press 1 before the second party is called.
 	     *
 	     * This is useful when the first party uses Accessline.
 	     */
 	    cp.setCallAnsweredTreatment("dialtojoin.au");
 	    cp.setCallEstablishedTreatment("please_wait.au,ring_tone.au");
 	    cp.setJoinConfirmationTimeout(90);
 	}
 
 	cp.setCallEstablishedTreatment(
 	    req.getParameter("callEstablishedTreatment"));
 
 	String joinConfirmationTimeout =
 	    req.getParameter("joinConfirmationTimeout");
 
 	if (joinConfirmationTimeout != null) {
 	    int timeout = 90;
 
 	    try {
 	        timeout = Integer.parseInt(joinConfirmationTimeout);
 	    } catch (NumberFormatException e) {
 	        println("Invalid join confirmation timeout "
 	    	    + joinConfirmationTimeout + " defaulting to " + timeout);
 	    }
 
             cp.setJoinConfirmationTimeout(timeout);
 	}
 
 	cp.setCallEndTreatment(req.getParameter("firstPartyCallEndTreatment"));
 	cp.setFirstConferenceMemberTreatment(
 	    req.getParameter("firstConferenceMemberTreatment"));
 
         cp.setSecondPartyNumber(req.getParameter("secondPartyNumber"));
         cp.setSecondPartyName(req.getParameter("secondPartyName"));
 	cp.setSecondPartyTreatment(req.getParameter("secondPartyTreatment"));
 	cp.setSecondPartyCallEndTreatment(
 	    req.getParameter("secondPartyCallEndTreatment"));
 
 	cp.setConferenceId(req.getParameter("conferenceId"));
 
 	cp.setConferenceJoinTreatment(
 	    req.getParameter("conferenceJoinTreatment"));
 
 	cp.setConferenceLeaveTreatment(
 	    req.getParameter("conferenceLeaveTreatment"));
 
 	String handleSessionProgress = 
 	    req.getParameter("handleSessionProgress");
 
 	if (handleSessionProgress != null && handleSessionProgress.equalsIgnoreCase("true")) {
 	    cp.setHandleSessionProgress(true);
 	}
 
         out.println("<HTML>");
 
         //out.println("<SCRIPT>window.resizeTo(800,100);</SCRIPT>");
 
         out.println("<BODY>");
 
         if (cp.getPhoneNumber() == null && cp.getSecondPartyNumber() == null) {
             out.println("<HEAD><META HTTP-EQUIV=\"refresh\" " +
 		"content=\"10\"><TITLE>Get Active Calls</TITLE></HEAD>");
         } else {
             out.println("<HEAD><TITLE>Call Dialer</TITLE></HEAD>");
 	}
 
 	if (cp.getConferenceId() == null) {
             if (cp.getSecondPartyNumber() == null) {
                 out.println("Second Party Number must be specified!");
                 out.println("</BODY></HTML>");
                 return;
             }
         }
 
         String s = "Connecting ";
 
         if (cp.getName() == null || cp.getName().equals("")) {
             s += cp.getPhoneNumber();
             cp.setName("0");        // we have to have a non-empty string
         } else {
             s += (cp.getName() + "@" + cp.getPhoneNumber());
         }
 
         s += " to ";
 
 	if (cp.getConferenceId() == null) {
             if (cp.getSecondPartyName() == null || cp.getSecondPartyName().equals("")) {
                 s += cp.getSecondPartyNumber();
                 cp.setSecondPartyName("1");   // we have to have a non-empty string
             } else {
                 s += (cp.getSecondPartyName() + "@" + cp.getSecondPartyNumber());
 	    }
 	} else {
 	    s += cp.getConferenceId();
 	}
 
         out.println(s);
         out.println("<P></P>");
 
 	println(cp.getCallSetupRequest());
 
         // start TPC and wait for status messages
 	sendRequest(cp.getCallSetupRequest());
 
         out.println("</BODY>");
         out.println("</HTML>");
     }
 
     /**
      * Send a request to the server and wait for responses
      *
      * @param request the String request to send to the server.
      */
     private void sendRequest(String request) {
 	String serverName = 
 	    getServletContext().getInitParameter("BRIDGE_SERVER_NAME");
 
 	if (serverName == null) {
	    serverName = "ecd.sfBay.sun.com";
 	}
 	int serverPort = 6666;
 
         String port = getServletContext().getInitParameter("BRIDGE_PORT");
 
 	if (port != null) {
 	    try {
 	        serverPort = Integer.parseInt(port);
 	    } catch (NumberFormatException e) {
 	    }
 	}
 
 	int maxResponseLength = 1024;
 
 	String maxResp = getServletContext().getInitParameter(
 	    "MAX_RESPONSE_LENGTH");
 
 	if (maxResp != null) {
 	    try {
 	        maxResponseLength = Integer.parseInt(maxResp);
 	    } catch (NumberFormatException e) {
 	    }
 	}
 
 	Socket socket = null;
 
 	try {
 	    socket = new Socket(serverName, serverPort);
 	    socket.getOutputStream().write(request.getBytes());
 	    
 	    try {
                 DataInputStream input = 
 		    new DataInputStream(socket.getInputStream());
 
 	        byte[] buf = new byte[maxResponseLength];
 
 		int len;
 
 		int establishedCount = 0;
 
 	        while ((len = input.read(buf, 0, buf.length)) > 0) {
                     String response = new String(buf, 0, len);
 
 	            println(response);
 
 		    response = response.replaceAll("\n", "<br>");
 
 		    if (response.indexOf("ENDING") >= 0) {
 			break;
 		    }
 
 		    String e = "ESTABLISHED";
 
 		    int ix;
 
 		    if ((ix = response.indexOf(e)) >= 0) {
 		        establishedCount++;
 
 			String s = response.substring(ix + e.length());
 
 			if (s.indexOf(e) >= 0) {
 		            establishedCount++;
 			}
 		
 		        if (establishedCount >= 2) {
 			    break;
 		        }
 		    }
 
 		    out.println(response);
 		}
 	    } catch (Exception e) {
 		println("Exception " + e.getMessage());
 	    }
 	} catch (Exception e) {
 	    println("Can't create socket " + serverName + ":" + serverPort);
 	    out.println("Can't create socket " + serverName + ":" + serverPort);
 	}
 
 	try {
 	    if (socket != null) {
 		println("Detaching...");
 		String s = "detach\r\n";
 	        socket.getOutputStream().write(s.getBytes());
 	        socket.close();
 	    }
 	} catch (Exception e) {
 	}
     }
 
 }
