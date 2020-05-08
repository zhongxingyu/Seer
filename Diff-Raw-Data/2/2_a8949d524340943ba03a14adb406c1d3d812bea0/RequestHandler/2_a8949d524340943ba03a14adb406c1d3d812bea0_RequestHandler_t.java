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
 
 package com.sun.voip.server;
 
 import com.sun.voip.CallEvent;
 import com.sun.voip.CallEventListener;
 import com.sun.voip.CallParticipant;
 import com.sun.voip.Logger;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import java.net.Socket;
 import java.net.InetAddress;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import java.text.ParseException;
 
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 /**
  * Read client requests from a TCP socket. 
  * This is run as a separate thread so that it can block
  * waiting for input.
  *
  * Each request line consists of ascii text and must be terminated 
  * with a newline.  The end of request is signaled by a line 
  * with only a newline character.
  */
 class RequestHandler extends Thread implements CallEventListener {
     private Socket socket;
 
     private BufferedReader bufferedReader;
     private DataOutputStream output;
 
     private boolean releaseCalls = false;
     private boolean done;
 
     private boolean synchronousMode;
 
     private static String bridgePublicAddress;
     private static int bridgeSipPort;
 
     private static boolean bridgeSuspended;
 
     private static ArrayList<RequestHandler> handlers = 
 	new ArrayList<RequestHandler>();
 
     static {
 	bridgePublicAddress = Bridge.getPublicHost();
 
 	bridgeSipPort = Bridge.getPublicSipPort();
     }
 
     public RequestHandler(Socket socket) throws IOException {
 	this.socket = socket;
 
 	bufferedReader = 
 	    new BufferedReader(new InputStreamReader(socket.getInputStream()));
 
         output = new DataOutputStream(socket.getOutputStream());
 
 	CallEvent callEvent = new CallEvent(CallEvent.NEW_CONNECTION);
 
 	callEvent.setInfo("to " + socket.getInetAddress().getHostName() 
 	    + ":" + socket.getPort() + " BridgePublicAddress='" 
 	    + bridgePublicAddress + ":" + bridgeSipPort + "'");
 
 	writeToSocket(callEvent.toString());
 
 	setName("RequestHandler - "
 	    + socket.getInetAddress().getHostName() + ":" + socket.getPort());
 
 	synchronized (handlers) {
 	    handlers.add(this);
 	}
 
 	start();
     }
 
     /*
      * We remember the most recent call so that commands such as "mute"
      * can be directed to that call without having the caller identify
      * the call.  It would be better to force the caller to identify
      * the call to which a command pertains.  Then this code simplifies.
      */
     private CallParticipant cp;		// most recent call
 
     private long startTime;
     private int requestCount;
 
     private long blockedTime;
 
     /**
      * Thread to read input from the client, parse it, and start a call.
      */
     public void run() {
 	RequestParser requestParser = null;
 
 	while (!done) {
 	    CallParticipant cp = new CallParticipant();
 
 	    if (requestParser != null) {
 		synchronousMode = requestParser.synchronousMode();
 	    }
 
 	    requestParser = new RequestParser(this, cp);
 
 	    requestParser.setSynchronousMode(synchronousMode);
 
 	    if (this.cp != null) {
 		/*
 		 * Remember last specified call Id and conference Id
 		 * so these don't have to be specified with every command
 		 */
 		requestParser.setPreviousValues(this.cp.getCallId(),
 		    this.cp.getConferenceId());
 	    }
 
 	    /*
 	     * Collect call parameters and perform immediate actions
 	     */
 	    String errorMsg = null;
 
 	    while (true) {
 		synchronousMode = requestParser.synchronousMode();
 
 		String request = null;
 
 	        try {
 		    if (startTime == 0) {
 			startTime = System.nanoTime();
 		    }
 
 		    long readLineStartTime = System.nanoTime();
 
 		    request = bufferedReader.readLine(); // read from socket
 
 		    blockedTime += (System.nanoTime() - readLineStartTime);
 
 		    if (Logger.logLevel > Logger.LOG_INFO && ++requestCount == 500) {
 			long elapsed = System.nanoTime() - startTime;
 
 			Logger.println("elapsed " + (elapsed / 1000000000.)
 			    + " blocked " + (blockedTime * 100. / 1000000000.)
 			    + " (" + ((double)blockedTime / elapsed) + "%) "
 			    + ", 500 requests, " + (requestCount / (elapsed / 1000000000.))
 			    + " requests per second");
 			startTime = 0;
 			requestCount = 0;
 			blockedTime = 0;
 		    }
 	        } catch (IOException e) {
 		    endAllCalls("client socket closed");
 		    removeHandler(this);
 		    return;
 	        }
 		    
 		if (request == null) {
 		    endAllCalls("client socket closed");
 		    removeHandler(this);
 		    return;
 		}
 
 		if (suspended) {
 		    if (request.equalsIgnoreCase("resume") == false) {
 		        continue;	// ignore input
 		    }
 		}
 
 		if (request.indexOf("pm=") == 0 || 
 			request.indexOf("privateMix=") == 0 ||
 			request.indexOf("pmx") == 0) {
 
 		     if (ignorePmx == true) {
 			continue;
 		     }
 
 		     Logger.writeFile(request);
 		} else {
 		     if (request.equalsIgnoreCase("gs") == false) {
 		         Logger.println(request);
 		     }
 		}
 
 	        if (request.length() == 0) {
 		    /*
 		     * done with input, start call.
 		     */
 		    try {
 		        validateAndAdjustParameters(cp); 
 		    } catch (ParseException e) {
 			this.cp = null;
 			errorMsg = "Bad parameters:  " + e.getMessage();
 	    	 	break;	// bad parameters
 		    }
 
 		    if (CallHandler.tooManyDuplicateCalls(cp.getPhoneNumber())) {
 			/*
 			 * This is to prevent a bug in someone else's code
 			 * from using up all the phone lines!
 			 */
 			Logger.println("Too many calls to the same number! " 
 			   + cp.getPhoneNumber());
 			writeToSocket("Too many calls to the same number! "
 			   + cp.getPhoneNumber());
 
 			if (synchronousMode) {
 			    writeToSocket(
 				"END -- FAILED:  Too many calls "
 				+ "to the same number!");
 			}
 			continue;
 		    }
 
 		    if (cp.getRemoteMediaInfo() != null) {
 			cp.setProtocol("NS");
 			cp.setConferenceId(null);
 			new IncomingCallHandler(this, cp);
         	    } else if (cp.migrateCall() == true) {
             	        new CallMigrator(this, cp).start();
         	    } else if (cp.getSecondPartyNumber() == null) {
             		OutgoingCallHandler callHandler = 
 			    new OutgoingCallHandler(this, cp);
 			
 			callHandler.start();
 
 			if (synchronousMode) {
 			    String sdp = callHandler.getSdp();
 
 			    if (Logger.logLevel >= Logger.LOG_INFO) {
 			        Logger.println("Writing sdp to socket " + sdp);
 			    }
 
 			    if (sdp != null) {
 			        writeToSocket(sdp);
 			    }
 			}
 	            } else {
            		new TwoPartyCallHandler(this, cp).start();
         	    }
 
 		    this.cp = cp;	// remember most recent call
 	    	    break;		
 		}
 
 		if (request.indexOf("?") == 0) {
 		    writeToSocket(cp.getCallSetupRequest());
 		    if (synchronousMode) {
 		        writeToSocket("END -- SUCCESS");
 		    }
 		    continue;
 		}
 
 	        if (request.toUpperCase().indexOf("CANCEL") >= 0 &&
 		        request.indexOf("=") < 0) {
 
 		    OutgoingCallHandler.hangup(
 			this, "client requested call end");
 
 		    if (synchronousMode) {
 		        writeToSocket("END -- SUCCESS");
 		    }
 		    continue;
 	        }
 
 	        if (request.toUpperCase().indexOf("DETACH") >= 0) {
 	            Logger.println("detaching...");
 		    done = true;
 		    closeSocket();
 		    return;
 	        }
 
 	        try {
 	            if (requestParser.parseRequest(request) == true) {
 		        if (synchronousMode) {
 			    writeToSocket("END -- SUCCESS");
 		        }
 		    }
 	        } catch (ParseException e) {
 	            Logger.error(e.getMessage());
 	            writeToSocket(e.getMessage());
 
 		    if (synchronousMode) {
 		        writeToSocket("END -- FAILED:  " + e.getMessage());
 		    }
 	        }
 	    }
 
 	    if (synchronousMode) {
 		if (errorMsg == null) {
 	            writeToSocket("END -- SUCCESS");
 		} else {
 	            writeToSocket("END -- FAILED:  " + errorMsg);
 		}
 	    }
 	}
     }
 
     private static boolean ignorePmx;
 
     public static void ignorePmx(boolean ignorePmx) {
 	RequestHandler.ignorePmx = ignorePmx;
     }
 
     private void removeHandler(RequestHandler handler) {
 	synchronized (handlers) {
 	    handlers.remove(handler);
 	}
     }
 
     /*
      * Make sure parameters are correct and compatible.
      */
     private void validateAndAdjustParameters(CallParticipant cp) 
 	    throws ParseException {
 
 	String callId = cp.getCallId();
 
 	if (callId == null) {
 	    cp.setCallId(CallHandler.getNewCallId());
 	} else {
 	    if (callId.equals("0")) {
 		Logger.error("Zero is an invalid callId");
 		writeToSocket("Zero is an invalid callId");
 		throw new ParseException("Zero is an invalid callId", 0);
 	    }
 
 	    if (cp.migrateCall() == false) {
 		CallHandler callHandler = CallHandler.findCall(callId);
 
 	        if (callHandler != null) { 
 		    if (callHandler.isCallEnding() == false) {
 		        Logger.error("CallId " + callId + " is already in use");
 		        writeToSocket("CallId " + callId + " is already in use");
 		        throw new ParseException(
 			    "CallId " + callId + " is already in use", 0);
 		    } else {
 			Logger.println("Reusing callId for ending call " + callId);
 		    }
 	        }
 	    }
 	}
 
         handleCallAttendant(cp);
 
 	cp.setSecondPartyNumber(formatPhoneNumber(cp.getSecondPartyNumber(),
 	    cp.getPhoneNumberLocation()));
 
 	if (cp.migrateCall() == false) {
 	    cp.setPhoneNumber(formatPhoneNumber(cp.getPhoneNumber(),
 	        cp.getPhoneNumberLocation()));
 
 	    if (cp.getPhoneNumber() == null) {
 		if (cp.getInputTreatment() == null) {
 	            writeToSocket(
 		        "You must specify a phone number or a soft phone URI");
 
 	            Logger.error(
 		        "You must specify a phone number or a soft phone URI");
 
 	            throw new ParseException(
 			"You must specify a phone number or a soft phone URI", 0);
 		} else {
 		    if (cp.getInputTreatment().equals("null")) {
 			cp.setInputTreatment("");
 		    }
 
 		    cp.setPhoneNumber(cp.getInputTreatment());
 		    cp.setProtocol("NS");
 		}
 	    }
 	}
 
         if (cp.getName() == null || cp.getName().equals("")) {
 	    /*
 	     * Must be non-null
 	     */
             cp.setName("Anonymous");  
 	}
 
 	if (cp.migrateCall() == true) {
 	    if (cp.getCallId() == null || cp.getSecondPartyNumber() == null) {
 		Logger.error("You must specify old and new phone numbers " 
 		    + "to migrate a call");
 		throw new ParseException("You must specify old and new phone numbers " 
 		    + "to migrate a call", 0);
 	    }
 	}
 
 	if (cp.getConferenceId() == null && 
 		cp.getSecondPartyNumber() == null) {
 
 	    if (this.cp != null) {
 		String conferenceId = this.cp.getConferenceId();
 
 	        if (conferenceId != null) {
 		    cp.setConferenceId(conferenceId);
 	            writeToSocket("Using same conference as last call:  "
 		        + conferenceId);
 
 	            Logger.println("Using same conference as last call:  "
 		        + conferenceId);
 		}
 	    } else {
 	        writeToSocket("You must specify a conference Id");
 	        Logger.error("You must specify a conference Id");
 	        throw new ParseException("You must specify a conference Id", 0);
 	    }
 	}
 
 	if (cp.getDisplayName() == null) {
 	    if (cp.getSecondPartyNumber() == null) {
 	        cp.setDisplayName(cp.getConferenceId());
 	    } else {
                 if (cp.getSecondPartyName() != null) {
 	            cp.setDisplayName(cp.getSecondPartyName());
 	        } else {
 	            cp.setDisplayName(cp.getSecondPartyNumber());
 		}
 	    }
 	}
 	
 	/*
 	 * For two party calls.
 	 */
 	if (cp.getConferenceId() != null) {
 	    if (cp.getSecondPartyTreatment() != null) {
 		cp.setConferenceJoinTreatment(cp.getSecondPartyTreatment());
 	    }
 
 	    if (cp.getSecondPartyCallEndTreatment() != null) {
 		cp.setConferenceLeaveTreatment(
 		    cp.getSecondPartyCallEndTreatment());
 	    }
 	}
 
 	if (cp.getSecondPartyNumber() != null) {
 	    if (cp.getConferenceId() == null) {
 		cp.setConferenceId(cp.getPhoneNumber());
 	    }
 	}
     }
 
     /*
      * Some Sun sites such as China have an automated call attendent
      * which asks the user to enter the extension again.
      *
      * If a phone number has ">" in it, we replace the phone number
      * to call with everything before the ">" and set the call
      * answer treatment to be dtmf keys of everything after the ">".
      */
     private void handleCallAttendant(CallParticipant cp) {
         String phoneNumber = cp.getPhoneNumber();
 
         int ix;
 
         if (phoneNumber != null && phoneNumber.indexOf("sip:") < 0 &&
                 phoneNumber.indexOf("@") < 0) {
 
             if ((ix = phoneNumber.indexOf(">")) > 0) {
                 /*
                  * Must have 5 digit extension
                  */
                 if (phoneNumber.length() >= ix + 1 + 5) {
                     cp.setCallAnsweredTreatment("dtmf:" +
                         phoneNumber.substring(ix + 1));
 
                     cp.setPhoneNumber(phoneNumber.substring(0, ix));
                 }
             }
         }
 
         phoneNumber = cp.getSecondPartyNumber();
 
         if (phoneNumber != null &&
                 phoneNumber.indexOf("sip:") < 0 &&
                 phoneNumber.indexOf("@") < 0) {
 
             if ((ix = phoneNumber.indexOf(">")) > 0) {
                 /*
                  * Must have 5 digit extension
                  */
                 if (phoneNumber.length() >= ix + 1 + 5) {
                     cp.setSecondPartyTreatment("dtmf:" +
                         phoneNumber.substring(ix + 1));
 
                     cp.setSecondPartyNumber(phoneNumber.substring(0, ix));
                 }
             }
         }
     }
 
     private static String outsideLinePrefix = "9";  // for outside line
 
     public static void setOutsideLinePrefix(String outsideLinePrefix) {
 	RequestHandler.outsideLinePrefix = outsideLinePrefix;
     }
 
     public static String getOutsideLinePrefix() {
 	return outsideLinePrefix;
     }
 
     private static String longDistancePrefix = "1";  // for long Distancee
 
     public static void setLongDistancePrefix(String longDistancePrefix) {
 	RequestHandler.longDistancePrefix = longDistancePrefix;
     }
 
     public static String getLongDistancePrefix() {
 	return longDistancePrefix;
     }
 
     private static String internationalPrefix = "011";  // for international calls
 
     public static void setInternationalPrefix(String internationalPrefix) {
 	RequestHandler.internationalPrefix = internationalPrefix;
     }
 
     public static String getInternationalPrefix() {
 	return internationalPrefix;
     }
 
     private static boolean prefixPhoneNumber = true;
 
     public static void setPrefixPhoneNumber(boolean prefixPhoneNumber) {
 	RequestHandler.prefixPhoneNumber = prefixPhoneNumber;
     }
 
     public static boolean prefixPhoneNumber() {
 	return prefixPhoneNumber;
     }
     
     /*
      * Add prefixes to phone number, strip out extraneous characters
      */
     private String formatPhoneNumber(String phoneNumber, String location) {
 	if (phoneNumber == null) {
 	    return null;
 	}
 
 	/*
 	 * It's a softphone number.  Leave it as is.
 	 */
 	if (phoneNumber.indexOf("sip:") == 0) {
 	    /*
 	     * There is a problem where Meeting Central gives
 	     * us a phone number with only "sip:" which isn't valid.
 	     * Check for that here.
 	     * XXX
 	     */
 	    if (phoneNumber.indexOf("@") < 0) {
 		return null;
 	    }
 
 	    return phoneNumber;
 	}
 
 	if (phoneNumber.indexOf("@") >= 0) {
 	    return "sip:" + phoneNumber;
 	}
 
 	/*
 	 * If number starts with "Id-" it's a callId.  Leave it as is.
 	 */
 	if (phoneNumber.indexOf("Id-") == 0) {
 	    return phoneNumber;
 	}
 
         /*
          * Get rid of white space in the phone number
          */ 
         phoneNumber = phoneNumber.replaceAll("\\s", "");
 
         /*
          * Get rid of "-" in the phone number
          */
         phoneNumber = phoneNumber.replaceAll("-", "");
 
 	/*
 	 * For Jon Kaplan who likes to use "." as a phone number separator!
 	 */
 	phoneNumber = phoneNumber.replaceAll("\\.", "");
 
 	if (phoneNumber.length() == 0) {
 	    return null;
 	}
 
 	if (prefixPhoneNumber == false) {
 	    return phoneNumber;
 	}
 
         /*
          * Replace leading "+" (from namefinder) with appropriate numbers.
          * +1 is a US number and becomes outsideLinePrefix.
          * +<anything else> is considered to be an international number and 
 	 * becomes internationalPrefix.
          */
         if (phoneNumber.charAt(0) == '+') {
             if (phoneNumber.charAt(1) == '1') {
                 phoneNumber = outsideLinePrefix + phoneNumber.substring(1);
             } else {
                 phoneNumber = outsideLinePrefix + internationalPrefix 
 		    + phoneNumber.substring(1);
 	    }
         } else if (phoneNumber.charAt(0) == 'x' ||
 	        phoneNumber.charAt(0) == 'X') {
 
 	    phoneNumber = phoneNumber.substring(1);
 	}
 
 	if (phoneNumber.length() == 5) {
 	    /*
 	     * This is an internal extension.  Determine if it needs
 	     * a prefix of "70".
 	     */
 	    phoneNumber = PhoneNumberPrefix.getPrefix(location) + phoneNumber;
         } else if (phoneNumber.length() > 7) {
             /*
              * It's an outside number
              *
              * XXX No idea what lengths of 8 and 9 would be for...
              */
             if (phoneNumber.length() == 10) {
                 /*
                  * It's US or Canada, number needs 91
                  */
                 phoneNumber = outsideLinePrefix + longDistancePrefix 
 		    + phoneNumber;
             } else if (phoneNumber.length() >= 11) {
                 /*
                  * If it starts with 9 or 1, it's US or Canada.
                  * Otherwise, it's international.
                  */
                 if (phoneNumber.length() == 11 && 
 			longDistancePrefix.length() > 0 &&
                         phoneNumber.charAt(0) == longDistancePrefix.charAt(0)) {
 
                     phoneNumber = outsideLinePrefix + phoneNumber;
                 } else if (phoneNumber.length() == 11 &&
 			outsideLinePrefix.length() > 0 &&
                         phoneNumber.charAt(0) == outsideLinePrefix.charAt(0)) {
 
                     phoneNumber = outsideLinePrefix + longDistancePrefix 
 			+ phoneNumber.substring(1);
                 } else if (phoneNumber.length() == 12 &&
                         phoneNumber.substring(0,2).equals(outsideLinePrefix 
 			+ longDistancePrefix)) {
                     // nothing to do
                 } else {
                     /*
                      * It's international, number needs outsideLinePrefix plus
 		     * internationalPrefix
                      */
                     if (phoneNumber.substring(0,3).equals(internationalPrefix)) {
                         /*
                          * international prefix is already there, just prepend 
 			 * outsideLinePrefix
                          */
                          phoneNumber = outsideLinePrefix + phoneNumber;
                     } else if (!phoneNumber.substring(0,4).equals(
 			    outsideLinePrefix + internationalPrefix)) {
 
                         phoneNumber = outsideLinePrefix + internationalPrefix 
 			    + phoneNumber;
                     }
                 }
             }
         }
 
         return phoneNumber;
     }
 
     /**
      * monitor the status of a call
      */
     public void monitorCallStatus(String callId, boolean monitor) {
 	CallHandler callHandler = CallHandler.findCall(callId);
 
 	if (callHandler == null) {
 	    writeToSocket("No such callId:  " + callId);
 	    return;
 	}
 
 	if (monitor) {
 	    callHandler.addCallEventListener(this);
 	} else {
 	    callHandler.removeCallEventListener(this);
 	}
     }
 
     /**
      * monitor incoming calls
      */
     private static ArrayList<RequestHandler> incomingCallListeners = 
 	new ArrayList();
 
     public boolean monitorIncomingCalls(boolean monitor) {
 	synchronized(incomingCallListeners) {
 	    //if (monitor == true && incomingCallListener != null) {
 	    //    return false;	// only allow one listener
 	    //}
 	
             if (monitor == true) {
 		if (incomingCallListeners.contains(this)) {
 		    Logger.println("RequestHandler is already an "
 			+ "incomingCallListener");
 		    return false;
 		}
 
                 incomingCallListeners.add(this);
 		IncomingCallHandler.setDirectConferencing(false);
 		Logger.println("adding incoming call monitor, setting "
 		    + "directConferencing to false");
             } else {
                 if (incomingCallListeners.contains(this) == false) {
 		    return false;
 		}
 
 		incomingCallListeners.remove(this);
 
 		if (incomingCallListeners.size() == 0) {
 		    IncomingCallHandler.setDirectConferencing(true);
 		    Logger.println("removing last incoming call monitor "
 		       + "setting directConferencing to true");
 		}
             }
 	}
 	return true;
     }
 
     public static String getIncomingCallListenerInfo() {
         String s = "No incoming call handler";
 
         synchronized (incomingCallListeners) {
 	    boolean firstTime = true;
 
 	    for (RequestHandler requestHandler : incomingCallListeners) {
                 if (requestHandler.getSocket() != null) {
 	 	    if (firstTime) {
 			firstTime = false;
                         s = requestHandler.getSocket().toString();
 		    } else {
			s += "\n\t\t\t\t  " + requestHandler.getSocket().toString();
 		    }
                 }
 	    }
 	}
 
 	return s;
     }
 
     public Socket getSocket() {
 	return socket;
     }
 
     public static ArrayList getIncomingCallListeners() {
 	return incomingCallListeners;
     }
 
     /**
      * monitor outgoing calls
      */
     private static ArrayList<RequestHandler> outgoingCallListeners = new ArrayList();
     
     public void monitorOutgoingCalls(boolean monitor) {
         synchronized(outgoingCallListeners) {
             if (monitor == true) {
                 outgoingCallListeners.add(this);
             } else {
                 outgoingCallListeners.remove(this);
             }     
         }
     }   
 
     public static void incomingCallNotification(CallEvent callEvent) {
 	synchronized (incomingCallListeners) {
 	    for (RequestHandler requestHandler : incomingCallListeners) {
 	        requestHandler.callEventNotification(callEvent);
 	    }
 	}
 
 	notifyConferenceMonitors(callEvent);
     }
 
     public static void outgoingCallNotification(CallEvent callEvent) {
         synchronized(outgoingCallListeners) {
 	    for (RequestHandler requestHandler : outgoingCallListeners) {
                 requestHandler.callEventNotification(callEvent);
             }
         }
     }
 
     private static ArrayList<ConferenceMonitor> conferenceMonitors = new ArrayList();
 
     class ConferenceMonitor {
 
         private RequestHandler handler;
         private String conferenceId;
 
         public ConferenceMonitor(RequestHandler handler, String conferenceId) {
             this.handler = handler;
             this.conferenceId = conferenceId;
         }
 
         public RequestHandler getRequestHandler() {
             return handler;
         }
 
         public String getConferenceId() {
             return conferenceId;
         }
 
     }
 
     /**
      * monitor the status of all the calls in a conference
      */
     public void monitorConferenceStatus(String conferenceId, boolean monitor) {
         if (monitor) {
             synchronized(conferenceMonitors) {
 		if (Logger.logLevel >= Logger.LOG_INFO) {
 		    Logger.println("adding conference monitor for " 
 			+ conferenceId);
 		}
 
                 conferenceMonitors.add(
                     new ConferenceMonitor(this, conferenceId));
             }
 	} else {
             synchronized(conferenceMonitors) {
 	        ArrayList<ConferenceMonitor> monitorsToRemove = new ArrayList();
 
 		for (ConferenceMonitor m : conferenceMonitors) {
 		    if (m.getRequestHandler() != this) {
 			continue;
 		    }
 
                     if (conferenceId.equals(m.getConferenceId())) {
 			monitorsToRemove.add(m);
 		    }
 		}
 
 		for (ConferenceMonitor m : monitorsToRemove) {
 		    Logger.println("Removing conference monitor for "
 			+ conferenceId);
 
                     conferenceMonitors.remove(m);
                 }
             }   
         }
     }   
 
     public void callEventNotification(CallEvent callEvent) {
 	notifyConferenceMonitors(callEvent);
 
 	if (synchronousMode == true) {
 	    return;
 	}
 
 	writeToSocket(callEvent.toString());
     }
 
     public static void notifyConferenceMonitors(CallEvent callEvent) {
         synchronized(conferenceMonitors) {
 	    /*
 	     * Notify conference listeners
 	     */
 	    String conferenceId = callEvent.getConferenceId();
 
 	    String s = callEvent.toString();
 
 	    if (conferenceId == null) {
 		int ix;
 
 		String search = "ConferenceId='";
 
 		if ((ix = s.indexOf(search)) < 0) {
 		    return;
 		}
 
 		conferenceId = s.substring(search.length());
 
 		int end;
 
 		if ((end = conferenceId.indexOf("'")) < 0) {
 		    return;
 		}
 
 		conferenceId = conferenceId.substring(0, end);
 	    }
 
 	    for (ConferenceMonitor m : conferenceMonitors) {
                 if (conferenceId.equals(m.getConferenceId())) {
 		    m.getRequestHandler().writeToSocket(s);
 		}
 	    }
 	}
     }
 
     private StatisticsGenerator statisticsGenerator;
 
     public void setStatisticsTimeout(int statisticsTimeout) {
 	if (statisticsGenerator != null) {
 	    statisticsGenerator.setTimeout(statisticsTimeout);
 	} else {
 	    new StatisticsGenerator(statisticsTimeout).start();
 	}
     }
 
     class StatisticsGenerator extends Thread {
 
         private int timeout;
 
 	public StatisticsGenerator(int timeout) {
 	    setTimeout(timeout);
 	}
 
 	public void setTimeout(int timeout) {
 	    if (timeout < 1) {
 		timeout = 1;
 	    }
 
 	    this.timeout = timeout;
 	}
 
 	public void run() {
 	    while (!done) {
 	        writeToSocket("Conferences:         " 
 		    + ConferenceManager.getConferenceList().size());
 
 	        writeToSocket("Members:             "
 		    + ConferenceManager.getTotalMembers());
 
 		writeToSocket("Time Bewteen Sends:  "
 		    + (Math.round(ConferenceSender.getTimeBetweenSends() * 10000) / 10000.)
 		    + " ms");
 
 		writeToSocket("Average Send time:   " 
 		    + (Math.round(ConferenceSender.getAverageSendTime() * 10000) / 10000.)
 		    + " ms");
 
 
 		writeToSocket("Max Send time:       "
 		    + (Math.round(ConferenceSender.getMaxSendTime() * 10000) / 10000.)
 		    + " ms");
 
 		writeToSocket("");
 
 	        try {
 		    Thread.sleep(timeout * 1000);
 	        } catch (InterruptedException e) {
 	        }
 	    }
 	}
     }
 
     /**
      * Write a String to the client's socket.
      * There is a possibility for the write to the socket to block
      * so a separate thread is used to write to the socket.
      */ 
     private ArrayList<String> dataToWrite = new ArrayList();
     private SocketWriter socketWriter;
 
     private static final int MAX_DATA_TO_WRITE_SIZE = 200;
 
     class SocketWriter extends Thread {
 	public SocketWriter() {
 	    setName("SocketWriter " + socket.getInetAddress().getHostName()
                 + ":" + socket.getPort());
 	    start();
 	}
 
         public void done() {
 	    interrupt();
 	}
 	    
 	public void run() {
 	    while (!done && socket != null) {
 		String s = null;
 
 		synchronized(dataToWrite) {
 		    while (!done && dataToWrite.size() == 0) {
 		        try {
 			    dataToWrite.wait();
 		        } catch (InterruptedException e) {
 			    break;
 		        }
 		    }
 
 		    if (done) {
 		        break;
 		    }
 
 		    s = (String) dataToWrite.remove(0);
 		}
 		
 		if (suspended) {
 		    continue;
 		}
 
                 if (Logger.logLevel >= Logger.LOG_MOREINFO) {
                     Logger.println("sending status to socket " +
                         socket + ": " + s);
                 }
 
                 try {
                     s += "\r\n";
                     output.write(s.getBytes());
                     output.flush();
                 } catch (IOException e) {
                     if (!done) {
                         if (Logger.logLevel >= Logger.LOG_INFO) {
                             Logger.println(
                                 "Can't write to socket output "
 				+ " stream! " + e.getMessage());
 			    Logger.println("No more status will be sent "
 			        + " to this socket!");
 			}
                     }
 		    break;
 		}
 	    }
 
 	    socketWriter = null;
 
 	    if (Logger.logLevel >= Logger.LOG_INFO) {
 	        Logger.println("SocketWriter done");
 	    }
         }
 
     }
 
     public void writeToSocket(String s) {
 	if (socket == null || s == null) {
 	    return;
 	}
 
 	if (suspended) {
 	    return;
 	}
 
 	if (socketWriter == null) {
 	    socketWriter = new SocketWriter();
 	}
 
 	synchronized(dataToWrite) {
 	    if (dataToWrite.size() > MAX_DATA_TO_WRITE_SIZE) {
 	        Logger.println(
 		    "Can't write to socket, dataToWrite.size() exceeds "
 		    + MAX_DATA_TO_WRITE_SIZE);
 
 	
 	        dataToWrite.clear();
 	        return;
 	    }
 
 	    dataToWrite.add(s);
 	    dataToWrite.notifyAll();
 	}
     }
 
     public void endAllCalls(String reason) {
 	if (done) {
 	    return;
 	}
 
 	done = true;
 
 	if (releaseCalls == false) {
 	    OutgoingCallHandler.hangup(this, reason);
 	}
 
 	closeSocket();
     }
 
     /**
      * close our tcp socket to the client.
      */
     private void closeSocket() {
 	if (socket == null) {
 	    return;
 	}
 
 	synchronized (incomingCallListeners) {
 	    boolean removeIncomingCallHandler = false;
 	    for (RequestHandler requestHandler : incomingCallListeners) {
 		if (requestHandler == this) {
 		    removeIncomingCallHandler = true;
 		}
 	    }
 
 	    if (removeIncomingCallHandler) {
 	        monitorIncomingCalls(false);
 	    }
 
 	    monitorOutgoingCalls(false);
 	}
 
         synchronized(conferenceMonitors) {
 	    ArrayList<ConferenceMonitor> monitorsToRemove = new ArrayList();
 
 	    for (ConferenceMonitor m : conferenceMonitors) {
                 if (this == m.getRequestHandler()) {
 		    monitorsToRemove.add(m);
 		}
 	    }
 
 	    for (ConferenceMonitor m : monitorsToRemove) {
 		Logger.println("Removing conference monitor for "
 		    + m.getConferenceId());
 		conferenceMonitors.remove(m);
 	    }
 	}
 
 	try {
 	    Logger.println("Connection closed to "
                 + socket.getInetAddress().getHostName() 
 		+ ":" + socket.getPort());
 	    socket.close();
 	} catch (IOException e) {
 	}
 
         socket = null;
 
 	if (socketWriter != null) {
 	    socketWriter.done();
 	}
 
 	removeHandler(this);
 
 	Logger.flush();
     }
 
     /**
      * set/get flag to indicate if calls should be terminated when
      * request handler finishes
      */
     public void setReleaseCalls(boolean releaseCalls) {
 	this.releaseCalls= releaseCalls;
     }
 
     private boolean suspended;
 
     public void suspendBridge(int seconds) {
 	ArrayList<RequestHandler> handlers = 
 	    new ArrayList<RequestHandler>();
 
 	Logger.println("Suspending...");
 
 	BridgeStatusNotifier.suspendPing(seconds);
 
 	synchronized (handlers) {
 	    for (RequestHandler h : handlers) {
 		if (h != this) {
 		    handlers.add(h);
 		}
 	    }
 	}
 
 	if (seconds > 0) {
 	    Timer timer = new Timer();
 
 	    timer.schedule(new TimerTask() {
 		public void run() {
 		    resumeBridge();
 		}}, seconds * 1000);
 	}
 
 	for (RequestHandler h : handlers) {
 	    h.suspendBridge(seconds == 0);
 	}
 
 	CallHandler.suspendBridge();
 	bridgeSuspended = true;
     }
 
     private void suspendBridge(boolean end) {
 	suspended = true;
 
 	if (end == false) {
 	    return;
 	}
 
 	done = true;
 	closeSocket();
     }
 
     public void resumeBridge() {
 	if (isBridgeSuspended() == false) {
 	    return;
 	}
 
         Logger.println("Resuming...");
 
 	synchronized (handlers) {
 	    for (RequestHandler h : handlers) {
 		if (h != this) {
 		    h.resumeBridgeNow();
 		}
 	    }
 	}
 
 	BridgeStatusNotifier.resumePing();
 	bridgeSuspended = false;
     }
 
     public static boolean isBridgeSuspended() {
 	return bridgeSuspended;
     }
 
     private void resumeBridgeNow() {
 	suspended = false;
     }
 
 }
