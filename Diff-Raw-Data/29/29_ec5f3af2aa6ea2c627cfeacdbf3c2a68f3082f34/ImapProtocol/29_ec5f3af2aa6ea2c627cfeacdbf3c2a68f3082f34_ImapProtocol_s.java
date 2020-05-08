 /*-
  * Copyright (c) 2006, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.logicprobe.LogicMail.mail.imap;
 
 import java.io.IOException;
 import java.util.Hashtable;
 import java.util.Vector;
 import net.rim.device.api.system.EventLogger;
 import net.rim.device.api.util.Arrays;
 import org.logicprobe.LogicMail.AppInfo;
 import org.logicprobe.LogicMail.mail.MailException;
 import org.logicprobe.LogicMail.message.MessageEnvelope;
 import org.logicprobe.LogicMail.util.Connection;
 import org.logicprobe.LogicMail.util.StringParser;
 
 /**
  * This class implements the commands for the IMAP protocol
  */
 public class ImapProtocol {
     private Connection connection;
     private String idleCommandTag;
 
     /**
      * Counts the commands executed so far in this session. Every command of an
      * IMAP session needs a unique ID that is prepended to the command line.
      */
     private int commandCount = 0;
 
     /**
      * Flags that can be associated with a message.
      * Used inside reply objects for multiple commands.
      */
     public static class MessageFlags {
         /** Message has been read */
         public boolean seen;
         /** Message has been answered */
         public boolean answered;
         /** Message is flagged for special attention */
         public boolean flagged;
         /** Message is deleted */
         public boolean deleted;
         /** Message has not completed composition */
         public boolean draft;
         /** Message has recently arrived */
         public boolean recent;
         /** Message has been flagged as junk */
         public boolean junk;
     }
     
     /** Creates a new instance of ImapProtocol */
     public ImapProtocol(Connection connection) {
         this.connection = connection;
     }
     
     /**
      * Execute the "LOGIN" command
      * @param username The username to login with
      * @param password The password to login with
      * @return True on success, false on authentication failures
      */
     public boolean executeLogin(String username, String password) throws IOException, MailException {
         if(EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(
             AppInfo.GUID,
             ("ImapProtocol.executeLogin(\""+username+"\", \""+password+"\")").getBytes(),
             EventLogger.DEBUG_INFO);
         }
         
         // Authenticate with the server
         try {
             execute("LOGIN", "\""+ StringParser.addEscapedChars(username) + "\" \"" + StringParser.addEscapedChars(password) + "\"");
         } catch (MailException exp) {
             // Invalid users are caught by execute()
             // and a MailException is thrown
             return false;
         }
         return true;
     }
     
     /**
      * Execute the "LOGOUT" command
      */
     public void executeLogout() throws IOException, MailException {
         if(EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(
             AppInfo.GUID,
             ("ImapProtocol.executeLogout()").getBytes(),
             EventLogger.DEBUG_INFO);
         }
        
         execute("LOGOUT", null);
     }
     
     /**
      * Execute the "CLOSE" command
      */
     public void executeClose() throws IOException, MailException {
         if(EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(
             AppInfo.GUID,
             ("ImapProtocol.executeClose()").getBytes(),
             EventLogger.DEBUG_INFO);
         }
 
         execute("CLOSE", null);
     }
     
     /**
      * Execute the "CAPABILITY" command
      * @return Hashtable containing a mapping from String to the boolean
      * value "true" for every capability that exists in the reply.
      */
     public Hashtable executeCapability() throws IOException, MailException {
         if(EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(
             AppInfo.GUID,
             ("ImapProtocol.executeCapability()").getBytes(),
             EventLogger.DEBUG_INFO);
         }
 
         String replyText[] = execute("CAPABILITY", null);
         if(replyText == null || replyText.length < 1) {
             throw new MailException("Unable to query server capabilities");
         }
         
         Hashtable table = new Hashtable();
 
         String[] tokens = StringParser.parseTokenString(replyText[0], " ");
         if(tokens.length > 2 && tokens[1].equals("CAPABILITY")) {
             for(int i=2;i<tokens.length;i++)
                 table.put(tokens[i], Boolean.TRUE);
         }
         
         return table;
     }
     
     /**
      * Container for a single namespace entry
      */
     public static class Namespace {
         /** Namespace prefix */
         public String prefix;
         /** Hierarchy delimiter */
         public String delimiter;
         // Extensions not currently parsed
     }
     
     /**
      * Container for a NAMESPACE response
      */
     public static class NamespaceResponse {
         /** Personal Namespace(s) */
         public Namespace personal[];
         /** Other Users' Namespace(s) */
         public Namespace other[];
         /** Shared Namespace(s) */
         public Namespace shared[];
     }
     
     /**
      * Execute the "NAMESPACE" command
      * @return A fully populated Namespace object
      */
     public NamespaceResponse executeNamespace() throws IOException, MailException {
         if(EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(
             AppInfo.GUID,
             ("ImapProtocol.executeNamespace()").getBytes(),
             EventLogger.DEBUG_INFO);
         }
 
         String replyText[] = execute("NAMESPACE", null);
         if(replyText == null || replyText.length < 1) {
             throw new MailException("Unable to query server namespaces");
         }
 
         // Assume a single-line reply
         Vector tokens = StringParser.nestedParenStringLexer("("+replyText[0].substring(replyText[0].indexOf('('))+")");
         
         // Sanity check on results
         if(tokens == null || tokens.size() < 3) {
             return new NamespaceResponse();
         }
 
         NamespaceResponse response = new NamespaceResponse();
         Vector nsTokens;
         Vector temp;
         int size;
         int i;
         
         // Parse personal namespace(s)
         if(tokens.elementAt(0) instanceof Vector) {
             nsTokens = (Vector)tokens.elementAt(0);
             size = nsTokens.size();
             response.personal = new Namespace[size];
             for(i = 0; i < nsTokens.size(); i++) {
                 if(nsTokens.elementAt(i) instanceof Vector) {
                     temp = (Vector)(nsTokens.elementAt(i));
                     response.personal[i] = new Namespace();
                     if(temp.size() >= 2) {
                         if(temp.elementAt(0) instanceof String) {
                             response.personal[i].prefix = StringParser.removeEscapedChars((String)temp.elementAt(0));
                         }
                         if(temp.elementAt(1) instanceof String) {
                             response.personal[i].delimiter = StringParser.removeEscapedChars((String)temp.elementAt(1));
                         }
                     }
                 }
             }
         }
         
         // Parse other users' namespace(s)
         if(tokens.elementAt(1) instanceof Vector) {
             nsTokens = (Vector)tokens.elementAt(1);
             size = nsTokens.size();
             response.other = new Namespace[size];
             for(i = 0; i < nsTokens.size(); i++) {
                 if(nsTokens.elementAt(i) instanceof Vector) {
                     temp = (Vector)(nsTokens.elementAt(i));
                     response.other[i] = new Namespace();
                     if(temp.size() >= 2) {
                         if(temp.elementAt(0) instanceof String) {
                             response.other[i].prefix = StringParser.removeEscapedChars((String)temp.elementAt(0));
                         }
                         if(temp.elementAt(1) instanceof String) {
                             response.other[i].delimiter = StringParser.removeEscapedChars((String)temp.elementAt(1));
                         }
                     }
                 }
             }
         }
 
         // Parse shared namespace(s)
         if(tokens.elementAt(2) instanceof Vector) {
             nsTokens = (Vector)tokens.elementAt(2);
             size = nsTokens.size();
             response.shared = new Namespace[size];
             for(i = 0; i < nsTokens.size(); i++) {
                 if(nsTokens.elementAt(i) instanceof Vector) {
                     temp = (Vector)(nsTokens.elementAt(i));
                     response.shared[i] = new Namespace();
                     if(temp.size() >= 2) {
                         if(temp.elementAt(0) instanceof String) {
                             response.shared[i].prefix = StringParser.removeEscapedChars((String)temp.elementAt(0));
                         }
                         if(temp.elementAt(1) instanceof String) {
                             response.shared[i].delimiter = StringParser.removeEscapedChars((String)temp.elementAt(1));
                         }
                     }
                 }
             }
         }
         
         return response;
     }
     
     /**
      * Container for a SELECT response
      */
     public static class SelectResponse {
         //public MessageFlags availableFlags; // not used
         //public MessageFlags permanentFlags; // not used
         public int exists;
         public int recent;
         public int unseen;
         public int uidNext;
         public int uidValidity;
     }
     
     /**
      * Execute the "SELECT" command
      * @param mboxpath The mailbox path to select
      * @return Parsed response object
      */
     public SelectResponse executeSelect(String mboxpath) throws IOException, MailException {
         if(EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(
             AppInfo.GUID,
             ("ImapProtocol.executeSelect(\""+mboxpath+"\")").getBytes(),
             EventLogger.DEBUG_INFO);
         }
 
         String replyText[] = execute("SELECT", "\""+StringParser.addEscapedChars(mboxpath)+"\"");
         SelectResponse response = new SelectResponse();
 
         int p, q;
         for(int i=0;i<replyText.length;i++) {
             String rowText = replyText[i];
             if((p = rowText.indexOf(" EXISTS")) != -1) {
                 q = p;
                 p = rowText.indexOf(' ');
                 if(q != -1 && p != -1 && q > p) {
                     try {
                         response.exists = Integer.parseInt(rowText.substring(p+1, q));
                     } catch (NumberFormatException e) {
                         response.exists = 0;
                     }
                 }
             }
             else if((p = rowText.indexOf(" RECENT")) != -1) {
                 q = p;
                 p = rowText.indexOf(' ');
                 if(q != -1 && p != -1 && q > p) {
                     try {
                         response.recent = Integer.parseInt(rowText.substring(p+1, q));
                     } catch (NumberFormatException e) {
                         response.recent = 0;
                     }
                 }
             }
             else if((p = rowText.indexOf("UNSEEN ")) != -1) {
                 p += 6;
                 q = rowText.indexOf(']');
                 if(q != -1 && p != -1 && q > p) {
                     try {
                         response.unseen = Integer.parseInt(rowText.substring(p+1, q));
                     } catch (NumberFormatException e) {
                         response.unseen = 0;
                     }
                 }                
             }
             else if((p = rowText.indexOf("UIDVALIDITY ")) != -1) {
                 p += 11;
                 q = rowText.indexOf(']');
                 if(q != -1 && p != -1 && q > p) {
                     try {
                         response.uidValidity = Integer.parseInt(rowText.substring(p+1, q));
                     } catch (NumberFormatException e) {
                         response.uidValidity = 0;
                     }
                 }
             }
             else if((p = rowText.indexOf("UIDNEXT ")) != -1) {
                 p += 7;
                 q = rowText.indexOf(']');
                 if(q != -1 && p != -1 && q > p) {
                     try {
                         response.uidNext = Integer.parseInt(rowText.substring(p+1, q));
                     } catch (NumberFormatException e) {
                         response.uidNext = 0;
                     }
                 }
             }
         }
         return response;
     }
     
     /**
      * Container for a STATUS response
      */
     public static class StatusResponse {
         public int exists;
         public int unseen;
     }
     
     public StatusResponse[] executeStatus(String[] mboxpaths) throws IOException, MailException {
         if(EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             StringBuffer buf = new StringBuffer();
             buf.append("ImapProtocol.executeStatus({\r\n");
             if(mboxpaths == null) {
                 buf.append("  null\r\n");
             }
             else {
                 for(int i=0; i<mboxpaths.length; i++) {
                     buf.append("  \""+mboxpaths[i]+"\"\r\n");
                 }
             }
             buf.append("})");
             EventLogger.logEvent(
                 AppInfo.GUID,
                 buf.toString().getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         StatusResponse[] response = new StatusResponse[mboxpaths.length];
         String[] arguments = new String[mboxpaths.length];
         
         int i;
         for(i = 0; i < mboxpaths.length; i++) {
             arguments[i] = "\""+StringParser.addEscapedChars(mboxpaths[i])+"\" (MESSAGES UNSEEN)";
         }
         String[] result = executeBatch("STATUS", arguments);
         if(result == null || result.length != arguments.length) {
             throw new MailException("Unable to query folder status");
         }
         
         for(i = 0; i < arguments.length; i++) {
             response[i] = new StatusResponse();
 
             if(result[i] == null) {
                 continue;
             }
             
             int p = result[i].indexOf('(');
             int q = result[i].indexOf(')');
             
             if(p == -1 || q == -1 || p >= q) {
                 continue;
             }
 
             String[] fields = StringParser.parseTokenString(result[i].substring(p+1, q), " ");
             if(fields.length != 4) {
                 continue;
             }
 
             for(int j = 0; j < fields.length; j+=2) {
                 if(fields[j].equalsIgnoreCase("MESSAGES")) {
                     try {
                         response[i].exists = Integer.parseInt(fields[j+1]);
                     } catch (NumberFormatException e) { }
                 }
                 else if(fields[j].equalsIgnoreCase("UNSEEN")) {
                     try {
                         response[i].unseen = Integer.parseInt(fields[j+1]);
                     } catch (NumberFormatException e) { }
                 }
             }
         }
         
         return response;
     }
     
     /**
      * Container for a FETCH (ENVELOPE) response
      */
     public static class FetchEnvelopeResponse {
         public int index;
         public int uid;
         public MessageFlags flags;
         public MessageEnvelope envelope;
     }
 
     /**
      * Execute the "FETCH (FLAGS UID ENVELOPE)" command
      * @param firstIndex Index of the first message
      * @param lastIndex Index of the last message
      * @return Array of FetchEnvelopeResponse objects
      */
     public FetchEnvelopeResponse[] executeFetchEnvelope(int firstIndex, int lastIndex) throws IOException, MailException {
         if(EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(
             AppInfo.GUID,
             ("ImapProtocol.executeFetchEnvelope("+firstIndex+", "+lastIndex+")").getBytes(),
             EventLogger.DEBUG_INFO);
         }
         
         String[] rawList = execute("FETCH",
                                    Integer.toString(firstIndex) + ":" +
                                    Integer.toString(lastIndex) +
                                    " (FLAGS UID ENVELOPE)");
         return prepareFetchEnvelopeResponse(rawList);
     }
 
     /**
      * Execute the "UID FETCH (FLAGS UID ENVELOPE)" command
      * @param uidNext Unique ID of the next message
      * @return Array of FetchEnvelopeResponse objects
      */
     public FetchEnvelopeResponse[] executeFetchEnvelopeUid(int uidNext) throws IOException, MailException {
         if(EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(
             AppInfo.GUID,
             ("ImapProtocol.executeFetchEnvelopeUid("+uidNext+")").getBytes(),
             EventLogger.DEBUG_INFO);
         }
         
         String[] rawList = execute("UID FETCH",
                 Integer.toString(uidNext) + ":*" +
                 " (FLAGS UID ENVELOPE)");
         
         return prepareFetchEnvelopeResponse(rawList);
     }
     
     private FetchEnvelopeResponse[] prepareFetchEnvelopeResponse(String[] rawList) throws IOException, MailException {
         // Preprocess the returned text to clean up mid-field line breaks
         // This should all become unnecessary once execute()
         // becomes more intelligent in how it handles replies
         String line;
         StringBuffer lineBuf = new StringBuffer();
         Vector rawList2 = new Vector();
         for(int i=0;i<rawList.length;i++) {
             line = rawList[i];
             if(line.length() > 0 &&
                     lineBuf.toString().startsWith("* ") &&
                     line.startsWith("* ")) {
                 rawList2.addElement(lineBuf.toString());
                 lineBuf = new StringBuffer();
             }
             lineBuf.append(line);
             if(i == rawList.length-1 && lineBuf.toString().startsWith("* ")) {
                 rawList2.addElement(lineBuf.toString());
             }
         }
         
         Vector envResponses = new Vector();
         int size = rawList2.size();
         for(int i=0;i<size;i++) {
             try {
                 String rawText = (String)rawList2.elementAt(i);
 
                 MessageEnvelope env = null;
                 Vector parsedText = null;
                 try {
                     parsedText = StringParser.nestedParenStringLexer(rawText.substring(rawText.indexOf('(')));
                 } catch (Exception exp) {
                     parsedText = null;
                 }
 
                 FetchEnvelopeResponse envRespItem = new FetchEnvelopeResponse();
                 envRespItem.flags = null;
                 
                 // Iterate through results, locating and parsing the
                 // FLAGS and ENVELOPE sections in an order-independent way.
                 int parsedSize = parsedText.size();
                 for(int j = 0; j < parsedSize; j++) {
                     if(parsedText.elementAt(j) instanceof String) {
                         if(((String)parsedText.elementAt(j)).equals("FLAGS") &&
                            parsedSize > j+1 &&
                            parsedText.elementAt(j+1) instanceof Vector) {
                             envRespItem.flags = ImapParser.parseMessageFlags((Vector)parsedText.elementAt(j+1));
                         }
                         else if(((String)parsedText.elementAt(j)).equals("UID") &&
                         		parsedSize > j+1 &&
                         		parsedText.elementAt(j+1) instanceof String) {
                         	try {
                         		envRespItem.uid = Integer.parseInt((String)parsedText.elementAt(j+1));
                         	} catch (NumberFormatException e) {
                         		envRespItem.uid = -1;
                         	}
                         }
                         else if(((String)parsedText.elementAt(j)).equals("ENVELOPE") &&
                                 parsedSize > j+1 &&
                                 parsedText.elementAt(j+1) instanceof Vector) {
                             env = ImapParser.parseMessageEnvelope((Vector)parsedText.elementAt(j+1));
                         }
                     }
                 }
 
                 // If either of the above sections were not found, then populate
                 // the reply with the relevant dummy data.
                 if(env == null) {
                     env = ImapParser.generateDummyEnvelope();
                 }
                 if(envRespItem.flags == null) {
                     envRespItem.flags = new MessageFlags();
                 }
 
                 // Find the message index in the reply
                 int midx = Integer.parseInt(rawText.substring(rawText.indexOf(' '), rawText.indexOf("FETCH")-1).trim());
                 
                 envRespItem.index = midx;
                 envRespItem.envelope = env;
                 envResponses.addElement(envRespItem);
             } catch (Exception exp) {
                 System.err.println("Parse error: " + exp);
             }
         }
 
         FetchEnvelopeResponse[] result = new FetchEnvelopeResponse[envResponses.size()];
         envResponses.copyInto(result);
         return result;
     }
     
     /**
      * Execute the "FETCH (BODYSTRUCTURE)" command
      * @param uid Unique ID of the message
      * @return Body structure tree
      */
     public ImapParser.MessageSection executeFetchBodystructure(int uid) throws IOException, MailException {
         if(EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(
             AppInfo.GUID,
             ("ImapProtocol.executeFetchBodyStructure("+uid+")").getBytes(),
             EventLogger.DEBUG_INFO);
         }
         
         String[] rawList = execute("UID FETCH", uid + " (BODYSTRUCTURE)");
 
         // Pre-process the returned text to clean up mid-field line breaks
         // This should all become unnecessary once execute()
         // becomes more intelligent in how it handles replies
         String line;
         StringBuffer lineBuf = new StringBuffer();
         Vector rawList2 = new Vector();
         for(int i=0;i<rawList.length;i++) {
             line = rawList[i];
             if(line.length() > 0 &&
                     lineBuf.toString().startsWith("* ") &&
                     line.startsWith("* ")) {
                 rawList2.addElement(lineBuf.toString());
                 lineBuf = new StringBuffer();
             }
             lineBuf.append(line);
             if(i == rawList.length-1 && lineBuf.toString().startsWith("* ")) {
                 rawList2.addElement(lineBuf.toString());
             }
         }
 
         ImapParser.MessageSection msgStructure = null;
         int size = rawList2.size();
         for(int i=0;i<size;i++) {
             try {
                 msgStructure = ImapParser.parseMessageStructure((String)rawList2.elementAt(i));
             } catch (Exception exp) {
                 System.out.println("Parse error: " + exp);
             }
         }
         
         return msgStructure;
     }
 
     /**
      * Execute the "FETCH (BODY)" command
      * @param uid Unique ID of the message
      * @param address Address of the body section (i.e. "1", "1.2")
      * @return Body text as a string
      */
     public String executeFetchBody(int uid, String address) throws IOException, MailException {
         if(EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(
             AppInfo.GUID,
             ("ImapProtocol.executeFetchBody("+uid+", \""+address+"\")").getBytes(),
             EventLogger.DEBUG_INFO);
         }
 
         String[] rawList = execute("UID FETCH", uid + " (BODY["+ address +"])");
 
         if(rawList.length <= 1) {
             return "";
         }
         
         StringBuffer msgBuf = new StringBuffer();
        for(int i=1;i<rawList.length-1;i++) {
             msgBuf.append(rawList[i] + "\n");
         }
        String lastLine = rawList[rawList.length-1];
         msgBuf.append(lastLine.substring(0, lastLine.lastIndexOf(')')));
         return msgBuf.toString();
     }
 
     /**
      * Execute the "STORE" command to update message flags.
      * @param uid The message unique ID to modify.
      * @param addOrRemove True to add flags, false to remove them.
      * @param flags Array of flags to change.  (i.e. "\Seen", "\Answered")
      * @return Updated standard message flags, or null if there was a parse error.
      */
     public MessageFlags executeStore(int uid, boolean addOrRemove, String[] flags) throws IOException, MailException {
         if(EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             StringBuffer buf = new StringBuffer();
             for(int i=0; i<flags.length; i++) {
                 buf.append('\"');
                 buf.append(flags[i]);
                 buf.append('\"');
                 if(i < flags.length - 1) {
                     buf.append(", ");
                 }
             }
             EventLogger.logEvent(
             AppInfo.GUID,
             ("ImapProtocol.executeStore("+uid+", "+(addOrRemove?"add":"remove")+", {"+buf.toString()+"})").getBytes(),
             EventLogger.DEBUG_INFO);
         }
 
         StringBuffer buf = new StringBuffer();
         buf.append(uid);
         buf.append(' ');
         buf.append(addOrRemove?'+':'-');
         buf.append("FLAGS (");
         for(int i=0; i<flags.length; i++) {
             buf.append(flags[i]);
             if(i < flags.length - 1) {
                 buf.append(' ');
             }
         }
         buf.append(')');
         String[] rawList = execute("UID STORE", buf.toString());
         if(rawList.length < 1) {
             throw new MailException("Unable to set message flags");
         }
         
         try {
             int p = rawList[0].indexOf("UID");
             int q = rawList[0].lastIndexOf(')');
             if(p != -1 && q != -1 && p < q) {
                 int fetchIndex = Integer.parseInt(rawList[0].substring(p+4, q));
                 if(fetchIndex != uid) {
                     return null;
                 }
             }
             p = rawList[0].indexOf("FLAGS (");
             q = rawList[0].indexOf(") UID");
             if(p == -1 || q == -1) {
                 return null;
             }
             String[] tokens = StringParser.parseTokenString(rawList[0].substring(p+7, q), " ");
             Vector tokenVec = new Vector(tokens.length);
             for(int i=0; i<tokens.length; i++) {
                 tokenVec.addElement(tokens[i]);
             }
             return ImapParser.parseMessageFlags(tokenVec);
         } catch (Exception e) {
             EventLogger.logEvent(AppInfo.GUID, ("Unable to parse STORE response: "+e.toString()).getBytes(), EventLogger.ERROR);
             return null;
         }
     }
 
     /**
      * Execute the "APPEND" command to add a message to an existing mailbox.
      * @param mboxName Mailbox name.
      * @param rawMessage The raw message text, in RFC2822-complaint format.
      * @param flags Flags to store the message with.
      */
     public void executeAppend(String mboxName, String rawMessage, MessageFlags flags) throws IOException, MailException {
         String flagsString = ImapParser.createMessageFlagsString(flags);
         if(EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(
             AppInfo.GUID,
             ("ImapProtocol.executeAppend(rawMessage, \""+flagsString+"\")").getBytes(),
             EventLogger.DEBUG_INFO);
         }
         
         executeContinue("APPEND", "\""+StringParser.addEscapedChars(mboxName)+"\" ("+flagsString+") {"+rawMessage.length()+"}", rawMessage,
                 "Unable to append message to "+mboxName);
     }
     
     /**
      * Container for a LIST response
      */
     public static class ListResponse {
         public boolean hasChildren;
         public boolean canSelect;
         public boolean marked;
         public String delim;
         public String name;
     };
     
     /**
      * Execute the "LIST" command, and return a fully parsed response
      * @param refName Reference name
      * @param mboxName Mailbox name or wildcards (i.e. "%")
      * @return Vector of ListResponse objects
      */
     public Vector executeList(String refName, String mboxName) throws IOException, MailException {
         if(EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(
             AppInfo.GUID,
             ("ImapProtocol.executeList(\""+refName+"\", \""+mboxName+"\")").getBytes(),
             EventLogger.DEBUG_INFO);
         }
         
         String[] results;
         results = execute("LIST", "\""+StringParser.addEscapedChars(refName)+"\" \""+StringParser.addEscapedChars(mboxName)+"\"");
         
         Vector retVec = new Vector(results.length);
         ListResponse response;
         String temp;
         String flagStr;
         String argStr;
         int p;
         int q;
         
         // Preprocess the results for specified-length continuation lines
         Vector resultsVec = new Vector();
         int line = 0;
         while(line < results.length) {
             p = results[line].indexOf('{');
             q = results[line].indexOf('}');
             if(line < results.length - 1 && p != -1 && q != -1 && p < q && q == results[line].length() - 1) {
                 int len = Integer.parseInt(results[line].substring(p+1, q));
                 resultsVec.addElement(results[line].substring(0, p) + results[line+1].substring(0, len));
                 line += 2;
             }
             else {
                 resultsVec.addElement(results[line]);
                 line++;
             }
         }
         
         int resultsSize = resultsVec.size();
         for(int i=0;i<resultsSize;i++) {
             // Separate out the flag and argument strings
             flagStr = null;
             argStr = null;
             temp = (String)resultsVec.elementAt(i);
             p = temp.indexOf('(');
             q = temp.indexOf(')', p + 1);
             if((p != -1) && (q > p)) {
                 flagStr = temp.substring(p + 1, q);
             }
             if(temp.length() > q+2) {
                 argStr = temp.substring(q+2);
             }
             
             response = new ListResponse();
             response.delim = "";
             response.name = "";
             
             // Look for flags
             response.canSelect = !(flagStr.indexOf("\\Noselect") != -1);
             response.hasChildren = (flagStr.indexOf("\\HasChildren") != -1);
             response.marked = (flagStr.indexOf("\\Marked") != -1);
 
             try {
                 p = 0;
                 q = 0;
                 int size = argStr.length();
                 if(argStr.charAt(p) == '"') {
                     p++;
                     q = p+1;
                     while(q < size && argStr.charAt(q) != '"') {
                         q++;
                     }
                     response.delim = argStr.substring(p, q);
                     p = q+2;
                 }
                 else {
                     q = p+1;
                     while(q < size && argStr.charAt(q) != ' ') {
                         q++;
                     }
                     response.delim = argStr.substring(p, q);
                     p = q+1;
                 }
 
                 if(response.delim.equalsIgnoreCase("NIL")) {
                     response.delim = "";
                 }
 
                 if(argStr.charAt(p) == '"') {
                     p++;
                     q = p+1;
                     while(q < size && argStr.charAt(q) != '"') {
                         q++;
                     }
                     response.name = argStr.substring(p, q);
                 }
                 else {
                     q = p+1;
                     while(q < size && argStr.charAt(q) != ' ') {
                         q++;
                     }
                     response.name = argStr.substring(p, q);
                 }
                 
                 // Strip any escaped characters
                 response.name = StringParser.removeEscapedChars(response.name);
                 response.delim = StringParser.removeEscapedChars(response.delim);
                 
                 // Only add if the response is not a duplicate of the request
                 if(!(refName.equals(response.name + response.delim) || refName.equals(response.name))) {
                     retVec.addElement(response);
                 }
             } catch (Exception e) {
                 // Prevent parse errors from being fatal
             }
         }
         
         return retVec;
     }
 
     /**
      * Execute the "IDLE" command.
      */
     public void executeIdle() throws IOException, MailException {
         if(EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(
             AppInfo.GUID,
             ("ImapProtocol.executeIdle()").getBytes(),
             EventLogger.DEBUG_INFO);
         }
         idleCommandTag = executeNoReply("IDLE", null);
     }
     
     /**
      * Execute the "DONE" command.
      */
     public void executeIdleDone() throws IOException, MailException {
         if(EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(
             AppInfo.GUID,
             ("ImapProtocol.executeIdleDone()").getBytes(),
             EventLogger.DEBUG_INFO);
         }
     	executeUntagged("DONE", null, idleCommandTag);
     	idleCommandTag = null;
     }
     
     /**
      * Polls the connection during the IDLE state.
      * For now, this is a simple implementation.  It just checks for
      * the presence of a "+ ?? recent" untagged response, and returns
      * true if one is found.
      */
     public boolean executeIdlePoll() throws IOException, MailException {
 		String result = receive();
 		if(result != null && result.startsWith("*") && result.toLowerCase().endsWith("recent")) {
 			return true;
 		}
 		else {
 			return false;
 		}
     }
     
     /**
      * Executes an IMAP command several times, with different arguments,
      * and return the replies as an array of strings.
      * @param command IMAP command
      * @param arguments Arguments for the commands
      * @return List of returned strings
      */
     protected String[] executeBatch(String command, String[] arguments)
         throws IOException, MailException
     {
         String[] result = new String[arguments.length];
         int count = 0;
         
         Hashtable commandMap = new Hashtable();
         StringBuffer commandBuf = new StringBuffer();
         
         for(int i = 0; i < arguments.length; i++) {
             String tag = "A" + (commandCount++);
             commandMap.put(tag, new Integer(i));
             commandBuf.append(tag);
             commandBuf.append(' ');
             commandBuf.append(command);
             commandBuf.append((arguments[i] == null ? "" : " " + arguments[i]));
             commandBuf.append("\r\n");
         }
         connection.sendRaw(commandBuf.toString());
 
         String temp;
         String tempResult = "";
         int p;
         
         while (count < arguments.length) {
             temp = connection.receive();
             
             String temp2 = temp.substring(temp.indexOf(" ")+1);
             if (temp2.startsWith("BAD ") || temp2.startsWith("NO ")) {
                 throw new MailException(temp);
             }
             
             p = temp.indexOf(" ");
             if(p != -1 && commandMap.containsKey(temp.substring(0, p))) {
                 result[((Integer)commandMap.get(temp.substring(0, p))).intValue()] = tempResult;
                 tempResult = "";
                 count++;
             }
             else {
                 tempResult = temp;
             }
         }
 
         return result;
     }
 
     /**
      * Executes an IMAP command, and returns the reply as an
      * array of strings.
      * @param command IMAP command
      * @param arguments Arguments for the command
      * @return List of returned strings
      */
     protected String[] execute(String command, String arguments)
         throws IOException, MailException
     {
         String[] result = new String[0];
 
         String tag = "A" + commandCount++ + " ";
         connection.sendCommand(tag + command + (arguments == null ? "" : " " + arguments));
 
         String temp = connection.receive();
         while (!temp.startsWith(tag)) {
             Arrays.add(result, temp);
             temp = connection.receive();
         }
 
         temp = temp.substring(tag.length());
         if (temp.startsWith("BAD ") || temp.startsWith("NO ")) {
             throw new MailException(temp);
         }
         return result;
     }
 
     /**
      * Executes an IMAP command, waits for a reply starting with a "+",
      * then sends more text, and ultimately returns the reply as an
      * array of strings.
      * This method is designed specifically for executeAppend().
      * @param command IMAP command
      * @param arguments Arguments for the command
      * @param errorMsg Error message if we get back something other than a continue
      * @return List of returned strings
      */
     protected String[] executeContinue(String command, String arguments, String text, String errorMsg)
         throws IOException, MailException
     {
         String[] result = new String[0];
 
         String tag = "A" + commandCount++ + " ";
         connection.sendCommand(tag + command + (arguments == null ? "" : " " + arguments));
 
         String temp = connection.receive();
         if(!temp.startsWith("+")) {
             throw new MailException(errorMsg);
         }
         connection.sendRaw(text);
         connection.sendRaw("\r\n");
 
         temp = connection.receive();
         while (!temp.startsWith(tag)) {
             Arrays.add(result, temp);
             temp = connection.receive();
         }
 
         temp = temp.substring(tag.length());
         if (temp.startsWith("BAD ") || temp.startsWith("NO ")) {
             throw new MailException(temp);
         }
         return result;
     }
     
     /**
      * Attempts to read a line of text from the server, without sending
      * anything first.
      * @return Returned string, or null if nothing is available
      */
     protected String receive()
     	throws IOException, MailException
 	{
     	String result;
     	
     	if(connection.available() > 0) {
    		System.err.println("-->connection.available() == " + connection.available());
 	        result = connection.receive();
	        System.err.println("-->Read in "+result.length());
	        System.err.println("-->connection.available() == " + connection.available());
     	}
     	else {
     		result = null;
     	}
     	return result;
 	}
     
     /**
      * Executes an IMAP command directly and expects no reply.
      * @param command IMAP command
      * @param arguments Arguments for the command
      * @return Tag used to send the command
      */
     protected String executeNoReply(String command, String arguments)
     	throws IOException, MailException
 	{
         String tag = "A" + commandCount++ + " ";
         connection.sendCommand(tag + command + (arguments == null ? "" : " " + arguments));
 
         return tag;
     }
     
     /**
      * Executes an IMAP command without a tag prefix,
      * and returns the reply as an array of strings.
      * @param command IMAP command
      * @param arguments Arguments for the command
      * @param Known tag that indicates the end of the result
      * @return List of returned strings
      */
     protected String[] executeUntagged(String command, String arguments, String endTag)
     	throws IOException, MailException
     {
     	String[] result = new String[0];
     	
         connection.sendCommand(command + (arguments == null ? "" : " " + arguments));
         
         String temp = connection.receive();
         while (!temp.startsWith(endTag)) {
             Arrays.add(result, temp);
             temp = connection.receive();
         }
 
         temp = temp.substring(endTag.length());
         if (temp.startsWith("BAD ") || temp.startsWith("NO ")) {
             throw new MailException(temp);
         }
         return result;
     }
 }
