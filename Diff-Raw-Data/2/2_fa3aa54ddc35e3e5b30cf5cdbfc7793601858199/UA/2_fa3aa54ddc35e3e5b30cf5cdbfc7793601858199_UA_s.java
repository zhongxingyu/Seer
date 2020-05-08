 /*
  * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
  *
  * This source code is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This source code is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this source code; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * Author(s):
  * Luca Veltri (luca.veltri@unipr.it)
  */
 
 package net.sourceforge.gjtapi.raw.mjsip.ua;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.telephony.ConnectionEvent;
 import javax.telephony.Event;
 import javax.telephony.ProviderUnavailableException;
 
 import local.ua.RegisterAgent;
 import local.ua.RegisterAgentListener;
 import net.sourceforge.gjtapi.raw.mjsip.MjSipCallId;
 import net.sourceforge.gjtapi.raw.mjsip.MjSipProvider;
 
 import org.zoolu.sip.address.NameAddress;
 import org.zoolu.sip.provider.SipProvider;
 import org.zoolu.sip.provider.SipStack;
 import org.zoolu.tools.Log;
 import org.zoolu.tools.LogLevel;
 
 
 
 /** Simple command-line-based SIP user agent (UA).
  * It includes audio/video applications.
  * <p>It can use external audio/video tools as media applications.
  * Currently only RAT (Robust Audio Tool) and VIC are supported as external applications.
  */
 public class UA implements UserAgentListener, RegisterAgentListener {
     /** Logger instance. */
     private static final Logger LOGGER =
         Logger.getLogger(MjSipProvider.class.getName());
 
     /** Event logger. */
     Log log;
 
     /** User Agent */
     UserAgent ua;
 
     /** Register Agent */
     RegisterAgent ra;
 
     /** UserAgentProfile */
     UserAgentProfile user_profile;
 
     /** Standard input */
     BufferedReader stdin;
 
     /** Streams **/
     //InputStream playStream;
     //OutputStream recStream;
     InputStreamConverter convertedInStream;
     OutputStreamConverter convertedOutStream;
 
     /** Standard output */
     PrintStream stdout;
 
     /** SIP Provder */
     SipProvider sip_provider;
 
     /** MjSipUAProvider Provder */
     MjSipProvider provider;
 
     /** Id from current call
      * It's assumed an agent can only handle one call at a time */
     MjSipCallId callID;
 
     /** Address being called
      * It's assumed an agent can only handle one call at a time */
     String addressCalled;
 
     /*private float FrameRate8Khz = 8000.0F;
     private float FrameRate16Khz = 16000.0F;
     private AudioFormat linear8Khz = new AudioFormat(AudioFormat.Encoding.
             PCM_SIGNED, FrameRate8Khz, 16, 1, 2, FrameRate8Khz, false);
     private AudioFormat linear16Khz = new AudioFormat(AudioFormat.Encoding.
             PCM_SIGNED, FrameRate16Khz, 16, 1, 2, FrameRate8Khz, true);
     private AudioFormat ulawformat = new AudioFormat(AudioFormat.Encoding.ULAW,
             FrameRate8Khz, 8, 1, 1, FrameRate8Khz, false);*/
 
 
     /**
      * Costructs a UA.
      * @param file name of the configuration file
      * @param provider related SIP provider.
      */
     public UA(String file, MjSipProvider provider) {
         File check = new File(file);
         if (!check.exists()) {
             throw new ProviderUnavailableException(
                     "Unable to open configuration file '"
                     + check.getAbsoluteFile() + "'");
         }
         this.provider = provider;
 
         SipStack.init(file);
         sip_provider = new SipProvider(file);
         user_profile = new UserAgentProfile(file);
 
         log = sip_provider.getLog();
 
         ua = new UserAgent(sip_provider, user_profile, this);
         //convertedOutStream = new OutputStreamConverter(ulawformat, linear8Khz);
         //convertedOutStream = new OutputStreamConverter(ulawformat, ulawformat);
         convertedOutStream = new OutputStreamConverter();
 
         //convertedInStream = new InputStreamConverter(linear16Khz, ulawformat);
         //convertedInStream = new InputStreamConverter(ulawformat, ulawformat);
         convertedInStream = new InputStreamConverter();
 
         ua.setRecvStream(convertedOutStream);
         ua.setSendStream(convertedInStream);
        ua.setAudio(true); //por causa da inicializao do audio_line
 
         ra = new RegisterAgent(sip_provider, user_profile.from_url,
                                user_profile.contact_url, user_profile.username,
                                user_profile.realm, user_profile.passwd, this);
 
         if (!user_profile.no_prompt) {
             stdin = new BufferedReader(new InputStreamReader(System.in));
         } else {
             stdin = null;
         }
         if (!user_profile.no_prompt) {
             stdout = System.out;
         } else {
             stdout = null;
         }
 
         run();
     }
 
 
     /** Register with the registrar server.
      * @param expire_time expiration time in seconds */
     public void register(int expire_time) {
         if (ra.isRegistering()) {
             ra.halt();
         }
         ra.register(expire_time);
     }
 
 
     /** Periodically registers the contact address with the registrar server.
      * @param expire_time expiration time in seconds
      * @param renew_time renew time in seconds
      * @param keepalive_time keep-alive packet rate (inter-arrival time) in milliseconds */
     public void loopRegister(int expire_time, int renew_time,
                              long keepalive_time) {
         if (ra.isRegistering()) {
             ra.halt();
         }
         ra.loopRegister(expire_time, renew_time, keepalive_time);
     }
 
 
     /** Unregister with the registrar server */
     public void unregister() {
         if (ra.isRegistering()) {
             ra.halt();
         }
         ra.unregister();
     }
 
 
     /** Unregister all contacts with the registrar server */
     public void unregisterall() {
         if (ra.isRegistering()) {
             ra.halt();
         }
         ra.unregisterall();
     }
 
 
     /** Makes a new call */
     public void call(String target_url) {
         ua.hangup();
         ua.printLog("UAC: CALLING " + target_url);
         if (!ua.user_profile.audio && !ua.user_profile.video) {
             ua.printLog("ONLY SIGNALING, NO MEDIA");
         }
         ua.call(target_url);
         addressCalled = target_url;
     }
 
 
     /** Receives incoming calls (auto accept) */
     public void listen() {
         ua.printLog("UAS: WAITING FOR INCOMING CALL");
         if (!ua.user_profile.audio && !ua.user_profile.video) {
             ua.printLog("ONLY SIGNALING, NO MEDIA");
         }
         ua.listen();
         //printOut("digit the callee's URL to make a call or press 'enter' to exit");
     }
 
 
     /** Starts the UA */
     void run() {
         try { // Set the re-invite
             if (user_profile.re_invite_time > 0) {
                 ua.reInvite(user_profile.contact_url,
                             user_profile.re_invite_time);
             }
 
             // Set the transfer (REFER)
             if (user_profile.transfer_to != null &&
                 user_profile.transfer_time > 0) {
                 ua.callTransfer(user_profile.transfer_to,
                                 user_profile.transfer_time);
             }
 
             if (user_profile.do_unregister_all)
             // ########## unregisters ALL contact URLs
             {
                 ua.printLog("UNREGISTER ALL contact URLs");
                 unregisterall();
             }
 
             if (user_profile.do_unregister)
             // unregisters the contact URL
             {
                 ua.printLog("UNREGISTER the contact URL");
                 unregister();
             }
 
             if (user_profile.do_register)
             // ########## registers the contact URL with the registrar server
             {
                 ua.printLog("REGISTRATION");
                 loopRegister(user_profile.expires, user_profile.expires / 2,
                              user_profile.keepalive_time);
             }
 
             if (user_profile.call_to != null) { // UAC
                 call(user_profile.call_to);
                 LOGGER.info("press 'enter' to hangup");
                 readLine();
                 ua.hangup();
             } else { // UAS
                 if (user_profile.accept_time >= 0) {
                     ua.printLog("UAS: AUTO ACCEPT MODE");
                 }
                 listen();
                 /*while (stdin!=null)
                         {  String line=readLine();
                            if (ua.statusIs(UserAgent.UA_INCOMING_CALL))
                            {  if (line.toLowerCase().startsWith("n"))
                               {  ua.hangup();
                               }
                               else
                               {  ua.accept();
                               }
                            }
                            else
                            if (ua.statusIs(UserAgent.UA_IDLE))
                            {  if (line!=null && line.length()>0)
                               {  call(line);
                               }
                               else
                               {  exit();
                               }
                            }
                            else
                            if (ua.statusIs(UserAgent.UA_ONCALL))
                            {  ua.hangup();
                            }
                         }*/
             }
         } catch (Exception e) {
             LOGGER.severe(e.getMessage());
         }
     }
 
 
     // ******************* UserAgent callback functions ******************
 
     /** When a new call is incoming */
     public void onUaCallIncoming(UserAgent ua, NameAddress callee,
                                  NameAddress caller) {
         LOGGER.info("incoming call from " + caller.toString() + " to " +
                  callee.toString());
         //printOut("accept? [yes/no]");
         callID = new MjSipCallId();
         provider.terminalConnectionRinging(callID, callee.getAddress().toString(),
                                            callee.getAddress().toString(),
                                            ConnectionEvent.CAUSE_NORMAL);
         provider.connectionInProgress(callID, callee.getAddress().toString(),
                                       Event.CAUSE_NORMAL);
         provider.connectionAlerting(callID, callee.getAddress().toString(),
                                     ConnectionEvent.CAUSE_NORMAL);
     }
 
     /** When an outgoing call is remotely ringing */
     public void onUaCallRinging(UserAgent ua) {
         provider.terminalConnectionCreated(callID, user_profile.contact_url,
                                            user_profile.contact_url,
                                            ConnectionEvent.CAUSE_NORMAL);
         provider.connectionInProgress(callID, user_profile.contact_url,
                                       Event.CAUSE_NORMAL);
         provider.connectionAlerting(callID, user_profile.contact_url,
                                     ConnectionEvent.CAUSE_NORMAL);
     }
 
     /** When an outgoing call has been accepted */
     public void onUaCallAccepted(UserAgent ua) {
         provider.connectionConnected(callID, addressCalled,
                                      ConnectionEvent.CAUSE_NORMAL);
         provider.callActive(callID, Event.CAUSE_NORMAL);
     }
 
     /** When a call has been transferred */
     public void onUaCallTrasferred(UserAgent ua) {
     }
 
     /** When an incoming call has been canceled */
     public void onUaCallCancelled(UserAgent ua) {
         listen();
     }
 
     /** When an outgoing call has been refused or timeout */
     public void onUaCallFailed(UserAgent ua) {
         if (ua.user_profile.call_to == null) {
             listen();
         }
     }
 
     /** When a call has been locally or remotely closed */
     public void onUaCallClosed(UserAgent ua) {
         if (ua.user_profile.call_to == null) {
             listen();
         }
         provider.connectionDisconnected(callID, user_profile.contact_url,
                                         Event.CAUSE_NORMAL);
 
     }
 
 
     // **************** RegisterAgent callback functions *****************
 
     /** When a UA has been successfully (un)registered. */
     public void onUaRegistrationSuccess(RegisterAgent ra, NameAddress target,
                                         NameAddress contact, String result) {
         ua.printLog("Registration success: " + result, LogLevel.HIGH);
     }
 
     /** When a UA failed on (un)registering. */
     public void onUaRegistrationFailure(RegisterAgent ra, NameAddress target,
                                         NameAddress contact, String result) {
         ua.printLog("Registration failure: " + result, LogLevel.HIGH);
     }
 
 
     // ****************************** Logs *****************************
 
     /** Read a new line from stantard input. */
     protected String readLine() {
         try {
             if (stdin != null) {
                 return stdin.readLine();
             }
         } catch (IOException e) {}
         return null;
     }
 
     /** Adds a new string to the default Log */
     void printLog(String str) {
         printLog(str, LogLevel.HIGH);
     }
 
     /** Adds a new string to the default Log */
     void printLog(String str, int level) {
         if (log != null) {
             log.println("CommandLineUA: " + str, level + SipStack.LOG_LEVEL_UA);
         }
     }
 
     /** Adds the Exception message to the default Log */
     void printException(Exception e, int level) {
         if (log != null) {
             log.printException(e, level + SipStack.LOG_LEVEL_UA);
         }
     }
 
 
     // **************************** GJTAPI Specific ***************************
 
     /** Returns the User Agent contact address */
     public String getAddress() {
         return ua.user_profile.contact_url;
     }
 
     public void setMjSipCallId(MjSipCallId id) {
         callID = id;
     }
 
     public MjSipCallId getMjSipCallId() {
         return callID;
     }
 
     /** Accept incoming call */
     public void accept() {
         ua.accept();
     }
 
     /** Hangup call */
     public void hangup() {
         ua.hangup();
     }
 
     /** Close media Application */
     public void closeMediaApplication() {
         stop();
         ua.closeMediaApplication();
     }
 
     public void play(InputStream src) {
         convertedInStream.setInputStream(src);
         convertedInStream.waitForEnd();
     }
 
 
     public void record(OutputStream dest) {
         convertedOutStream.setOutputStream(dest);
         try {
             while (convertedOutStream.isOpen()) {
                 Thread.sleep(10);
             }
         } catch (InterruptedException ex) {
             return;
         }
     }
 
     public void stopRecord() {
         try {
             if (convertedOutStream != null) {
                 convertedOutStream.close();
                 try {
                     while (convertedOutStream.isOpen()) {
                         Thread.sleep(10);
                     }
                 } catch (InterruptedException ex) {
                     return;
                 }
             }
         } catch (IOException e) {
             if (LOGGER.isLoggable(Level.FINE)) {
                 LOGGER.fine(e.getMessage());
             }
         }
     }
 
     public void stopPlay() {
         try {
             if (convertedInStream != null) {
                 convertedInStream.close();
                 convertedInStream.waitForEnd();
             }
         } catch (IOException e) {
             LOGGER.warning(e.getMessage());
         }
     }
 
 
     public void stop() {
         LOGGER.info("GJTAPI: Media stopped");
         stopPlay();
         stopRecord();
     }
 
 
 }
