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
 
 import net.rim.device.api.system.EventLogger;
 import net.rim.device.api.util.Arrays;
 import net.rim.device.api.util.IntIntHashtable;
 import net.rim.device.api.util.IntVector;
 
 import org.logicprobe.LogicMail.AppInfo;
 import org.logicprobe.LogicMail.mail.MailException;
 import org.logicprobe.LogicMail.mail.MailProgressHandler;
 import org.logicprobe.LogicMail.message.MessageEnvelope;
 import org.logicprobe.LogicMail.util.Connection;
 import org.logicprobe.LogicMail.util.ConnectionResponseTester;
 import org.logicprobe.LogicMail.util.StringArrays;
 import org.logicprobe.LogicMail.util.StringParser;
 
 import java.io.IOException;
 
 import java.util.Hashtable;
 import java.util.Vector;
 
 /**
  * This class implements the commands for the IMAP protocol
  */
 public class ImapProtocol {
     private Connection connection;
     private String idleCommandTag;
     private static final ConnectionResponseTester executeResponseTester = new ImapResponseLineTester();    
 
     /**
      * Counts the commands executed so far in this session. Every command of an
      * IMAP session needs a unique ID that is prepended to the command line.
      */
     private int commandCount = 0;
 
     /** Creates a new instance of ImapProtocol */
     public ImapProtocol() {
     }
 
     /**
      * Sets the connection instance used by this class.
      * This must be set after opening the connection, and prior to calling any
      * command methods.
      *
      * @param connection the new connection instance
      */
     public void setConnection(Connection connection) {
         this.connection = connection;
     }
     
     /**
      * Execute the "STARTTLS" command.
      * The underlying connection mode must be switched after the
      * successful execution of this command.
      */
     public void executeStartTLS() throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeStartTLS()").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         execute(STARTTLS, null, null);
     }
 
     /**
      * Execute the "LOGIN" command
      * @param username The username to login with
      * @param password The password to login with
      * @return True on success, false on authentication failures
      */
     public boolean executeLogin(String username, String password)
         throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeLogin(\"" + username + "\", \"" +
                 password + "\")").getBytes(), EventLogger.DEBUG_INFO);
         }
 
         // Authenticate with the server
         try {
             execute(LOGIN,
                 CHAR_QUOTE + StringParser.addEscapedChars(username) + "\" \"" +
                 StringParser.addEscapedChars(password) + CHAR_QUOTE, null);
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
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeLogout()").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         execute(LOGOUT, null, null);
     }
 
     /**
      * Execute the "CLOSE" command
      */
     public void executeClose() throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeClose()").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         execute(CLOSE, null, null);
     }
 
     /**
      * Execute the "CAPABILITY" command
      * @return Hashtable containing a mapping from String to the boolean
      * value "true" for every capability that exists in the reply.
      */
     public Hashtable executeCapability() throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeCapability()").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         String[] replyText = execute(CAPABILITY, null, null);
 
         if ((replyText == null) || (replyText.length < 1)) {
             throw new MailException("Unable to query server capabilities");
         }
 
         Hashtable table = new Hashtable();
 
         String[] tokens = StringParser.parseTokenString(replyText[0], CHAR_SP);
 
         if ((tokens.length > 2) && tokens[1].equals(CAPABILITY)) {
             for (int i = 2; i < tokens.length; i++)
                 table.put(tokens[i], Boolean.TRUE);
         }
 
         return table;
     }
 
     /**
      * Execute the "NAMESPACE" command
      * @return A fully populated Namespace object
      */
     public NamespaceResponse executeNamespace()
         throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeNamespace()").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         byte[][] replyText = executeResponse(NAMESPACE, null, null);
 
         if ((replyText == null) || (replyText.length < 1)) {
             throw new MailException("Unable to query server namespaces");
         }
 
         // Assume a single-line reply, and modify that reply so its enclosed
         // within a set of outer parenthesis so that the parser is happy.
         int offset = Arrays.getIndex(replyText[0], (byte)'(');
         byte[] data = new byte[(replyText[0].length - offset) + 2];
         data[0] = (byte)'(';
         int j = 1;
         for(int i=offset; i<replyText[0].length; i++) {
             data[j++] = replyText[0][i];
         }
         data[j] = (byte)')';
         
         Vector tokens = ImapParser.parenListParser(data);
 
         // Sanity check on results
         if ((tokens == null) || (tokens.size() < 3)) {
             return new NamespaceResponse();
         }
 
         NamespaceResponse response = new NamespaceResponse();
         Vector nsTokens;
         Vector temp;
         int size;
         int i;
 
         // Parse personal namespace(s)
         if (tokens.elementAt(0) instanceof Vector) {
             nsTokens = (Vector) tokens.elementAt(0);
             size = nsTokens.size();
             response.personal = new Namespace[size];
 
             for (i = 0; i < nsTokens.size(); i++) {
                 if (nsTokens.elementAt(i) instanceof Vector) {
                     temp = (Vector) (nsTokens.elementAt(i));
                     response.personal[i] = new Namespace();
 
                     if (temp.size() >= 2) {
                         if (temp.elementAt(0) instanceof byte[]) {
                             response.personal[i].prefix = new String((byte[]) temp.elementAt(0));
                         }
 
                         if (temp.elementAt(1) instanceof byte[]) {
                             response.personal[i].delimiter = new String((byte[]) temp.elementAt(1));
                         }
                     }
                 }
             }
         }
 
         // Parse other users' namespace(s)
         if (tokens.elementAt(1) instanceof Vector) {
             nsTokens = (Vector) tokens.elementAt(1);
             size = nsTokens.size();
             response.other = new Namespace[size];
 
             for (i = 0; i < nsTokens.size(); i++) {
                 if (nsTokens.elementAt(i) instanceof Vector) {
                     temp = (Vector) (nsTokens.elementAt(i));
                     response.other[i] = new Namespace();
 
                     if (temp.size() >= 2) {
                         if (temp.elementAt(0) instanceof byte[]) {
                             response.other[i].prefix = new String((byte[]) temp.elementAt(0));
                         }
 
                         if (temp.elementAt(1) instanceof byte[]) {
                             response.other[i].delimiter = new String((byte[]) temp.elementAt(1));
                         }
                     }
                 }
             }
         }
 
         // Parse shared namespace(s)
         if (tokens.elementAt(2) instanceof Vector) {
             nsTokens = (Vector) tokens.elementAt(2);
             size = nsTokens.size();
             response.shared = new Namespace[size];
 
             for (i = 0; i < nsTokens.size(); i++) {
                 if (nsTokens.elementAt(i) instanceof Vector) {
                     temp = (Vector) (nsTokens.elementAt(i));
                     response.shared[i] = new Namespace();
 
                     if (temp.size() >= 2) {
                         if (temp.elementAt(0) instanceof byte[]) {
                             response.shared[i].prefix = new String((byte[]) temp.elementAt(0));
                         }
 
                         if (temp.elementAt(1) instanceof byte[]) {
                             response.shared[i].delimiter = new String((byte[]) temp.elementAt(1));
                         }
                     }
                 }
             }
         }
 
         return response;
     }
 
     /**
      * Execute the "SELECT" command
      * @param mboxpath The mailbox path to select
      * @return Parsed response object
      */
     public SelectResponse executeSelect(String mboxpath)
         throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeSelect(\"" + mboxpath + "\")").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         String[] replyText = execute(SELECT,
                 CHAR_QUOTE + StringParser.addEscapedChars(mboxpath) +
                 CHAR_QUOTE, null);
         SelectResponse response = new SelectResponse();
 
         int p;
         int q;
 
         for (int i = 0; i < replyText.length; i++) {
             String rowText = replyText[i];
 
             if ((p = rowText.indexOf(" EXISTS")) != -1) {
                 q = p;
                 p = rowText.indexOf(' ');
 
                 if ((q != -1) && (p != -1) && (q > p)) {
                     try {
                         response.exists = Integer.parseInt(rowText.substring(p +
                                     1, q));
                     } catch (NumberFormatException e) {
                         response.exists = 0;
                     }
                 }
             } else if ((p = rowText.indexOf(" RECENT")) != -1) {
                 q = p;
                 p = rowText.indexOf(' ');
 
                 if ((q != -1) && (p != -1) && (q > p)) {
                     try {
                         response.recent = Integer.parseInt(rowText.substring(p +
                                     1, q));
                     } catch (NumberFormatException e) {
                         response.recent = 0;
                     }
                 }
             } else if ((p = rowText.indexOf("UNSEEN ")) != -1) {
                 p += 6;
                 q = rowText.indexOf(']');
 
                 if ((q != -1) && (p != -1) && (q > p)) {
                     try {
                         response.unseen = Integer.parseInt(rowText.substring(p +
                                     1, q));
                     } catch (NumberFormatException e) {
                         response.unseen = 0;
                     }
                 }
             } else if ((p = rowText.indexOf("UIDVALIDITY ")) != -1) {
                 p += 11;
                 q = rowText.indexOf(']');
 
                 if ((q != -1) && (p != -1) && (q > p)) {
                     try {
                         response.uidValidity = Integer.parseInt(rowText.substring(p +
                                     1, q));
                     } catch (NumberFormatException e) {
                         response.uidValidity = 0;
                     }
                 }
             } else if ((p = rowText.indexOf("UIDNEXT ")) != -1) {
                 p += 7;
                 q = rowText.indexOf(']');
 
                 if ((q != -1) && (p != -1) && (q > p)) {
                     try {
                         response.uidNext = Integer.parseInt(rowText.substring(p +
                                     1, q));
                     } catch (NumberFormatException e) {
                         response.uidNext = -1;
                     }
                 }
             }
         }
 
         return response;
     }
 
     /**
      * Execute the "EXPUNGE" command.
      *
      * @return an array of the indices of all expunged messages
      */
     public int[] executeExpunge() throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeExpunge()").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         String[] replyText = execute("EXPUNGE", null, null);
         
         IntVector indexVector = new IntVector();
         
         for (int i = 0; i < replyText.length; i++) {
             String rowText = replyText[i].toUpperCase();
             int p;
             int q;
             if ((p = rowText.indexOf(" EXPUNGE")) != -1) {
                 q = p;
                 p = rowText.indexOf(' ');
 
                 if ((q != -1) && (p != -1) && (q > p)) {
                     try {
                         indexVector.addElement(
                                 Integer.parseInt(rowText.substring(p + 1, q)));
                     } catch (NumberFormatException e) { }
                 }
             }
         }
         
         return indexVector.toArray();
     }
 
     public StatusResponse[] executeStatus(String[] mboxpaths,
         MailProgressHandler progressHandler) throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             StringBuffer buf = new StringBuffer();
             buf.append("ImapProtocol.executeStatus({\r\n");
 
             if (mboxpaths == null) {
                 buf.append("  null\r\n");
             } else {
                 for (int i = 0; i < mboxpaths.length; i++) {
                     buf.append("  \"" + mboxpaths[i] + "\"\r\n");
                 }
             }
 
             buf.append("})");
             EventLogger.logEvent(AppInfo.GUID, buf.toString().getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         StatusResponse[] response = new StatusResponse[mboxpaths.length];
         String[] arguments = new String[mboxpaths.length];
 
         int i;
 
         for (i = 0; i < mboxpaths.length; i++) {
             arguments[i] = CHAR_QUOTE +
                 StringParser.addEscapedChars(mboxpaths[i]) +
                 "\" (MESSAGES UNSEEN)";
         }
 
         String[] result = executeBatch(STATUS, arguments, progressHandler);
 
         if ((result == null) || (result.length != arguments.length)) {
             throw new MailException("Unable to query folder status");
         }
 
         for (i = 0; i < arguments.length; i++) {
             response[i] = new StatusResponse();
 
             if (result[i] == null) {
                 continue;
             }
 
             int p = result[i].indexOf('(');
             int q = result[i].indexOf(')');
 
             if ((p == -1) || (q == -1) || (p >= q)) {
                 continue;
             }
 
             String[] fields = StringParser.parseTokenString(result[i].substring(p +
                         1, q), CHAR_SP);
 
             if (fields.length != 4) {
                 continue;
             }
 
             for (int j = 0; j < fields.length; j += 2) {
                 if (fields[j].equalsIgnoreCase(MESSAGES)) {
                     try {
                         response[i].exists = Integer.parseInt(fields[j + 1]);
                     } catch (NumberFormatException e) {
                     }
                 } else if (fields[j].equalsIgnoreCase(UNSEEN)) {
                     try {
                         response[i].unseen = Integer.parseInt(fields[j + 1]);
                     } catch (NumberFormatException e) {
                     }
                 }
             }
         }
 
         return response;
     }
 
     /**
      * Execute the "FETCH (FLAGS UID)" command
      * @param firstIndex Index of the first message
      * @param lastIndex Index of the last message
      * @param progressHandler the progress handler
      * @return Array of FetchFlagsResponse objects
      */
     public FetchFlagsResponse[] executeFetchFlags(int firstIndex,
         int lastIndex, MailProgressHandler progressHandler)
         throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeFetchFlags(" + firstIndex + ", " +
                 lastIndex + ")").getBytes(), EventLogger.DEBUG_INFO);
         }
 
         final Vector result = new Vector(lastIndex - firstIndex);
         
         executeResponse(FETCH,
                 Integer.toString(firstIndex) + CHAR_COLON +
                 Integer.toString(lastIndex) + " (FLAGS UID)",
                 new ExecuteCallback() {
             public void processResponse(byte[] rawLine) {
                 FetchFlagsResponse response = prepareFetchFlagsResponse(rawLine);
                 if(response != null) {
                     result.addElement(response);
                 }
             }
             public void executeComplete() { }},
             progressHandler);
 
         FetchFlagsResponse[] resultArray = new FetchFlagsResponse[result.size()];
         result.copyInto(resultArray);
         return resultArray;
     }
 
     /**
      * Execute the "UID FETCH (FLAGS UID)" command
      * @param uidNext Unique ID of the next message
      * @param progressHandler the progress handler
      * @return Array of FetchFlagsResponse objects
      */
     public FetchFlagsResponse[] executeFetchFlagsUid(int uidNext,
         MailProgressHandler progressHandler) throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeFetchFlagsUid(" + uidNext + ")").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         final Vector result = new Vector();
         
         executeResponse(UID_FETCH,
                 Integer.toString(uidNext) + CHAR_COLON_ASTERISK +
                 " (FLAGS UID)",
                 new ExecuteCallback() {
             public void processResponse(byte[] rawLine) {
                 FetchFlagsResponse response = prepareFetchFlagsResponse(rawLine);
                 if(response != null) {
                     result.addElement(response);
                 }
             }
             public void executeComplete() { }},
             progressHandler);
 
         FetchFlagsResponse[] resultArray = new FetchFlagsResponse[result.size()];
         result.copyInto(resultArray);
         return resultArray;
     }
 
     /**
      * Execute the "UID FETCH (FLAGS UID)" command
      * @param uids the set of UIDs to fetch flags for
      * @param progressHandler the progress handler
      * @return Array of FetchFlagsResponse objects
      */
     public FetchFlagsResponse[] executeFetchFlagsUid(int[] uids,
         MailProgressHandler progressHandler) throws IOException, MailException {
         
         if (uids.length == 0) {
             return new FetchFlagsResponse[0];
         }
 
         StringBuffer buf = new StringBuffer();
 
         for (int i = 0; i < (uids.length - 1); i++) {
             buf.append(uids[i]);
             buf.append(',');
         }
 
         buf.append(uids[uids.length - 1]);
 
         String uidList = buf.toString();
         
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeFetchFlagsUid(" + uidList + ")").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         final Vector result = new Vector();
         
         executeResponse(UID_FETCH,
                 uidList + " (FLAGS UID)",
                 new ExecuteCallback() {
             public void processResponse(byte[] rawLine) {
                 FetchFlagsResponse response = prepareFetchFlagsResponse(rawLine);
                 if(response != null) {
                     result.addElement(response);
                 }
             }
             public void executeComplete() { }},
             progressHandler);
 
         FetchFlagsResponse[] resultArray = new FetchFlagsResponse[result.size()];
         result.copyInto(resultArray);
         return resultArray;
     }
 
     private FetchFlagsResponse prepareFetchFlagsResponse(byte[] rawText) {
         if(rawText == null || rawText.length == 0 || rawText[0] != CHAR_ASTERISK) {
             return null;
         }
         
         FetchFlagsResponse flagRespItem = new FetchFlagsResponse();
         
         try {
             Vector parsedText = null;
 
             try {
                 int offset = Arrays.getIndex(rawText, (byte)'(');
                 parsedText = ImapParser.parenListParser(rawText, offset, rawText.length - offset);
             } catch (Exception exp) {
                 parsedText = null;
                 return null;
             }
 
             
             flagRespItem.flags = null;
 
             // Iterate through results, locating and parsing the
             // FLAGS and ENVELOPE sections in an order-independent way.
             int parsedSize = parsedText.size();
 
             for (int j = 0; j < parsedSize; j++) {
                 if (parsedText.elementAt(j) instanceof String) {
                     if (((String) parsedText.elementAt(j)).equals(FLAGS) &&
                             (parsedSize > (j + 1)) &&
                             parsedText.elementAt(j + 1) instanceof Vector) {
                         flagRespItem.flags = ImapParser.parseMessageFlags((Vector) parsedText.elementAt(j +
                                     1));
                     } else if (((String) parsedText.elementAt(j)).equals(
                                 UID) && (parsedSize > (j + 1)) &&
                             parsedText.elementAt(j + 1) instanceof String) {
                         try {
                             flagRespItem.uid = Integer.parseInt((String) parsedText.elementAt(j +
                                         1));
                         } catch (NumberFormatException e) {
                             flagRespItem.uid = -1;
                         }
                     }
                 }
             }
 
             if (flagRespItem.flags == null) {
                 flagRespItem.flags = new MessageFlags();
             }
 
             // Find the message index in the reply
             int spaceIndex = Arrays.getIndex(rawText, (byte)' ');
             int fetchIndex = StringArrays.indexOf(rawText, FETCH.getBytes(), spaceIndex);
             int midx = StringArrays.parseInt(rawText, spaceIndex + 1, fetchIndex - spaceIndex - 2);
 
             flagRespItem.index = midx;
         } catch (Throwable exp) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("Parse error: " + exp).getBytes(), EventLogger.ERROR);
             flagRespItem = null;
         }
 
         return flagRespItem;
     }
 
     /**
      * Execute the "FETCH (FLAGS UID ENVELOPE BODYSTRUCTURE)" command
      * @param firstIndex Index of the first message
      * @param lastIndex Index of the last message
      * @param callback Callback for asynchronous notification of new envelopes
      * @param progressHandler the progress handler
      */
     public void executeFetchEnvelope(int firstIndex, int lastIndex,
         final FetchEnvelopeCallback callback, MailProgressHandler progressHandler)
         throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeFetchEnvelope(" + firstIndex + ", " +
                 lastIndex + ")").getBytes(), EventLogger.DEBUG_INFO);
         }
 
         executeResponse(FETCH,
                 Integer.toString(firstIndex) + CHAR_COLON +
                 Integer.toString(lastIndex) +
                 " (FLAGS UID ENVELOPE BODYSTRUCTURE)",
                 new ExecuteCallback() {
             public void processResponse(byte[] rawLine) {
                 prepareFetchEnvelopeResponse(rawLine, callback);
             }
             public void executeComplete() {
                 if(callback != null) {
                     callback.responseAvailable(null);
                 }
             }
         },
         progressHandler);
     }
 
     /**
      * Execute the "UID FETCH (FLAGS UID ENVELOPE BODYSTRUCTURE)" command
      * @param uidNext Unique ID of the next message
      * @param callback Callback for asynchronous notification of new envelopes
      * @param progressHandler the progress handler
      */
     public void executeFetchEnvelopeUid(int uidNext,
         final FetchEnvelopeCallback callback, MailProgressHandler progressHandler)
         throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeFetchEnvelopeUid(" + uidNext + ")").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         executeResponse(UID_FETCH,
                 Integer.toString(uidNext) + CHAR_COLON_ASTERISK +
                 " (FLAGS UID ENVELOPE BODYSTRUCTURE)",
                 new ExecuteCallback() {
             public void processResponse(byte[] rawLine) {
                 prepareFetchEnvelopeResponse(rawLine, callback);
             }
             public void executeComplete() {
                 if(callback != null) {
                     callback.responseAvailable(null);
                 }
             }
         },
         progressHandler);
     }
     
     /**
      * Execute the "FETCH (FLAGS UID ENVELOPE BODYSTRUCTURE)" command
      * @param indices Set of index values for the messages to fetch
      * @param callback Callback for asynchronous notification of new envelopes
      * @param progressHandler the progress handler
      */
     public void executeFetchEnvelope(int[] indices,
         final FetchEnvelopeCallback callback, MailProgressHandler progressHandler)
         throws IOException, MailException {
         if (indices.length == 0) {
             callback.responseAvailable(null);
 
             return;
         }
 
         StringBuffer buf = new StringBuffer();
 
         for (int i = 0; i < (indices.length - 1); i++) {
             buf.append(indices[i]);
             buf.append(',');
         }
 
         buf.append(indices[indices.length - 1]);
 
         String indexList = buf.toString();
 
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeFetchEnvelopeUid(" + indexList + ")").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         executeResponse(FETCH,
                 indexList + " (FLAGS UID ENVELOPE BODYSTRUCTURE)",
                 new ExecuteCallback() {
             public void processResponse(byte[] rawLine) {
                 prepareFetchEnvelopeResponse(rawLine, callback);
             }
             public void executeComplete() {
                 if(callback != null) {
                     callback.responseAvailable(null);
                 }
             }
         },
         progressHandler);
     }
 
     /**
      * Execute the "UID FETCH (FLAGS UID ENVELOPE BODYSTRUCTURE)" command
      * @param uids Set of unique IDs for the messages to fetch
      * @param callback Callback for asynchronous notification of new envelopes
      * @param progressHandler the progress handler
      */
     public void executeFetchEnvelopeUid(int[] uids,
         final FetchEnvelopeCallback callback, MailProgressHandler progressHandler)
         throws IOException, MailException {
         if (uids.length == 0) {
             callback.responseAvailable(null);
 
             return;
         }
 
         StringBuffer buf = new StringBuffer();
 
         for (int i = 0; i < (uids.length - 1); i++) {
             buf.append(uids[i]);
             buf.append(',');
         }
 
         buf.append(uids[uids.length - 1]);
 
         String uidList = buf.toString();
 
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeFetchEnvelopeUid(" + uidList + ")").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         executeResponse(UID_FETCH,
                 uidList + " (FLAGS UID ENVELOPE BODYSTRUCTURE)",
                 new ExecuteCallback() {
             public void processResponse(byte[] rawLine) {
                 prepareFetchEnvelopeResponse(rawLine, callback);
             }
             public void executeComplete() {
                 if(callback != null) {
                     callback.responseAvailable(null);
                 }
             }
         },
         progressHandler);
     }
 
     private void prepareFetchEnvelopeResponse(byte[] rawText, FetchEnvelopeCallback callback) {
         if(rawText == null || rawText.length == 0 || rawText[0] != CHAR_ASTERISK) {
             return;
         }
         
         FetchEnvelopeResponse envRespItem = null;
         try {
             MessageEnvelope env = null;
             ImapParser.MessageSection structure = null;
             Vector parsedText = null;
 
             try {
                 int offset = Arrays.getIndex(rawText, (byte)'(');
                 parsedText = ImapParser.parenListParser(rawText, offset, rawText.length - offset);
             } catch (Exception exp) {
                 return;
             }
 
             envRespItem = new FetchEnvelopeResponse();
             envRespItem.flags = null;
 
             // Iterate through results, locating and parsing the
             // FLAGS and ENVELOPE sections in an order-independent way.
             int parsedSize = parsedText.size();
 
             for (int j = 0; j < parsedSize; j++) {
                 if (FLAGS.equals(parsedText.elementAt(j))
                         && (parsedSize > (j + 1))
                         && parsedText.elementAt(j + 1) instanceof Vector) {
                     envRespItem.flags = ImapParser.parseMessageFlags((Vector) parsedText.elementAt(j + 1));
                 }
                 else if (UID.equals(parsedText.elementAt(j))
                         && (parsedSize > (j + 1))
                         && parsedText.elementAt(j + 1) instanceof String) {
                     try {
                         envRespItem.uid = Integer.parseInt((String) parsedText.elementAt(j + 1));
                     } catch (NumberFormatException e) {
                         envRespItem.uid = -1;
                     }
                 }
                 else if (ENVELOPE.equals(parsedText.elementAt(j))
                         && (parsedSize > (j + 1))
                         && parsedText.elementAt(j + 1) instanceof Vector) {
                     env = ImapParser.parseMessageEnvelope((Vector) parsedText.elementAt(j + 1));
                 }
                 else if (BODYSTRUCTURE.equals(parsedText.elementAt(j))
                         && (parsedSize > (j + 1))
                         && parsedText.elementAt(j + 1) instanceof Vector) {
                     structure = ImapParser.parseMessageStructureParameter((Vector) parsedText.elementAt(j + 1));
                 }
             }
 
             // If either of the above sections were not found, then populate
             // the reply with the relevant dummy data.
             if (env == null) {
                 env = ImapParser.generateDummyEnvelope();
             }
 
             if (envRespItem.flags == null) {
                 envRespItem.flags = new MessageFlags();
             }
 
             // Find the message index in the reply
             int spaceIndex = Arrays.getIndex(rawText, (byte)' ');
             int fetchIndex = StringArrays.indexOf(rawText, FETCH.getBytes(), spaceIndex);
             int midx = StringArrays.parseInt(rawText, spaceIndex + 1, fetchIndex - spaceIndex - 2);
 
             envRespItem.index = midx;
             envRespItem.envelope = env;
             envRespItem.structure = structure;
         } catch (Exception exp) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("Parse error: " + exp).getBytes(), EventLogger.ERROR);
             envRespItem = null;
         }
 
         if (envRespItem != null) {
             callback.responseAvailable(envRespItem);
         }
     }
 
     /**
      * Execute the "FETCH (BODYSTRUCTURE)" command
      * @param uid Unique ID of the message
      * @return Body structure tree
      */
     public ImapParser.MessageSection executeFetchBodystructure(int uid)
         throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeFetchBodyStructure(" + uid + ")").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         byte[][] rawList = executeResponse(UID_FETCH, uid + " (BODYSTRUCTURE)", null);
 
         ImapParser.MessageSection msgStructure = null;
 
         for (int i = 0; i < rawList.length; i++) {
             byte[] rawText = rawList[i];
             if(rawText == null || rawText.length == 0 || rawText[0] != CHAR_ASTERISK) {
                 continue;
             }
             
             try {
                 msgStructure = ImapParser.parseMessageStructure(rawText);
             } catch (Exception exp) {
                 EventLogger.logEvent(AppInfo.GUID,
                     ("Parse error: " + exp).getBytes(), EventLogger.ERROR);
             }
         }
 
         return msgStructure;
     }
 
     /**
      * Execute the "FETCH (BODY)" command
      * @param uid Unique ID of the message
      * @param address Address of the body section (i.e. "1", "1.2")
      * @param progressHandler the progress handler
      * @return Body text as a string
      */
     public byte[] executeFetchBody(int uid, String address,
         MailProgressHandler progressHandler) throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeFetchBody(" + uid + ", \"" + address +
                 "\")").getBytes(), EventLogger.DEBUG_INFO);
         }
 
         byte[][] rawList = executeResponse(UID_FETCH,
                 uid + " (BODY[" + address + "])", progressHandler);
 
         if (rawList.length < 1) {
             return new byte[0];
         }
 
         int offset = Arrays.getIndex(rawList[0], (byte)'(');
         Vector parsedList = ImapParser.parenListParser(rawList[0], offset, rawList[0].length - offset);
         int size = parsedList.size();
         
         byte[] rawMessage = null;
         for(int i=0; i<(size - 1); i++) {
             Object element = parsedList.elementAt(i);
             if(element instanceof String
                     && ((String)element).startsWith(BODY)
                     && parsedList.elementAt(i + 1) instanceof byte[]) {
                 rawMessage = (byte[])parsedList.elementAt(i + 1);
             }
         }
 
         if(rawMessage == null) {
             rawMessage = new byte[0];
         }
 
 
         return rawMessage;
     }
 
     /**
      * Execute the "STORE" command to update message flags.
      * @param uid The message unique ID to modify.
      * @param addOrRemove True to add flags, false to remove them.
      * @param flags Array of flags to change.  (i.e. "\Seen", "\Answered")
      * @return Updated standard message flags, or null if there was a parse error.
      */
     public MessageFlags executeStore(int uid, boolean addOrRemove,
         String[] flags) throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             StringBuffer buf = new StringBuffer();
 
             for (int i = 0; i < flags.length; i++) {
                 buf.append('\"');
                 buf.append(flags[i]);
                 buf.append('\"');
 
                 if (i < (flags.length - 1)) {
                     buf.append(", ");
                 }
             }
 
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeStore(" + uid + ", " +
                 (addOrRemove ? "add" : "remove") + ", {" + buf.toString() +
                 "})").getBytes(), EventLogger.DEBUG_INFO);
         }
 
         StringBuffer buf = new StringBuffer();
         buf.append(uid);
         buf.append(' ');
         buf.append(addOrRemove ? '+' : '-');
         buf.append("FLAGS (");
 
         for (int i = 0; i < flags.length; i++) {
             buf.append(flags[i]);
 
             if (i < (flags.length - 1)) {
                 buf.append(' ');
             }
         }
 
         buf.append(')');
 
         String[] rawList = execute(UID_STORE, buf.toString(), null);
 
         if (rawList.length < 1) {
            throw new MailException("Unable to set message flags");
         }
 
         MessageFlags result;
 
         try {
             int p = rawList[0].indexOf('(');
             int q = rawList[0].lastIndexOf(')');
             Vector tokenVec = null;
 
             if ((p != -1) && (q != -1) && (p < q)) {
                 Vector parsedText = ImapParser.parenListParser(
                         rawList[0].substring(p, q + 1).getBytes());
                 int size = parsedText.size();
 
                 for (int i = 0; i < size; i++) {
                     Object element = parsedText.elementAt(i);
 
                     if (element instanceof String && element.equals(FLAGS) &&
                             (i < (size - 1)) &&
                             parsedText.elementAt(i + 1) instanceof Vector) {
                         tokenVec = (Vector) parsedText.elementAt(i + 1);
 
                         break;
                     }
                 }
             }
 
             if (tokenVec != null) {
                 result = ImapParser.parseMessageFlags(tokenVec);
             } else {
                 result = null;
             }
         } catch (Exception e) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("Unable to parse STORE response: " + e.toString()).getBytes(),
                 EventLogger.ERROR);
 
             result = null;
         }
 
         return result;
     }
 
     /**
      * Execute the "APPEND" command to add a message to an existing mailbox.
      * @param mboxName Mailbox name.
      * @param rawMessage The raw message text, in RFC2822-complaint format.
      * @param flags Flags to store the message with.
      */
     public void executeAppend(String mboxName, String rawMessage,
         MessageFlags flags) throws IOException, MailException {
         String flagsString = ImapParser.createMessageFlagsString(flags);
 
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeAppend(rawMessage, \"" + flagsString +
                 "\")").getBytes(), EventLogger.DEBUG_INFO);
         }
 
         executeContinue(APPEND,
             CHAR_QUOTE + StringParser.addEscapedChars(mboxName) + "\" (" +
             flagsString + ") {" + rawMessage.length() + "}", rawMessage,
             "Unable to append message to " + mboxName);
     }
 
     /**
      * Execute the "COPY" command to copy a message from the current mailbox
      * to a different mailbox.
      *
      * @param uid The IMAP unique ID of the message to copy.
      * @param mboxPath The path of the destination mailbox
      */
     public void executeCopy(int uid, String mboxPath)
         throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeCopy(\"" + uid + "\", \"" + mboxPath +
                 "\")").getBytes(), EventLogger.DEBUG_INFO);
         }
 
         execute(UID_COPY,
             uid + " \"" + StringParser.addEscapedChars(mboxPath) + CHAR_QUOTE,
             null);
     }
 
     /**
      * Execute the "LIST" command, and return a fully parsed response.
      *
      * @param refName Reference name
      * @param mboxName Mailbox name or wildcards (i.e. "%")
      * @param progressHandler the progress handler
      * @return Vector of ListResponse objects
      */
     public Vector executeList(String refName, String mboxName,
         MailProgressHandler progressHandler) throws IOException, MailException {
         //
         // The default behavior should be to use executeListSubscribed.
         // I've left this in place so that if a future version wants to
         // offer a screen for Subscribe/Unsubscribe to folders, the
         // framework will already be in place.
         //
         return executeListImpl(LIST, refName, mboxName, progressHandler);
     }
 
     /**
      * Execute the "LSUB" command, and return a fully parsed response.
      *
      * @param refName Reference name
      * @param mboxName Mailbox name or wildcards (i.e. "%")
      * @param progressHandler the progress handler
      * @return Vector of ListResponse objects
      */
     public Vector executeLsub(String refName, String mboxName,
         MailProgressHandler progressHandler) throws IOException, MailException {
         return executeListImpl(LSUB, refName, mboxName, progressHandler);
     }
 
     private Vector executeListImpl(String ListVerb, String refName,
         String mboxName, MailProgressHandler progressHandler)
         throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeList(\"" + refName + "\", \"" + mboxName +
                 "\")").getBytes(), EventLogger.DEBUG_INFO);
         }
 
         String[] results;
         results = execute(ListVerb,
                 CHAR_QUOTE + StringParser.addEscapedChars(refName) + "\" \"" +
                 StringParser.addEscapedChars(mboxName) + CHAR_QUOTE,
                 progressHandler);
 
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
 
         if (progressHandler != null) {
             progressHandler.mailProgress(MailProgressHandler.TYPE_PROCESSING,
                 0, -1);
         }
 
         StringBuffer buf = new StringBuffer();
 
         while (line < results.length) {
             p = results[line].indexOf('{');
             q = results[line].indexOf('}');
 
             if ((line < (results.length - 1)) && (p != -1) && (q != -1) &&
                     (p < q) && (q == (results[line].length() - 1))) {
                 int len = Integer.parseInt(results[line].substring(p + 1, q));
 
                 buf.append(results[line].substring(0, p));
                 buf.append('"');
                 buf.append(results[line + 1].substring(0, len));
                 buf.append('"');
                 resultsVec.addElement(buf.toString());
                 buf.setLength(0);
                 line += 2;
             } else {
                 resultsVec.addElement(results[line]);
                 line++;
             }
         }
 
         int resultsSize = resultsVec.size();
 
         if (progressHandler != null) {
             progressHandler.mailProgress(MailProgressHandler.TYPE_PROCESSING,
                 0, resultsSize);
         }
 
         for (int i = 0; i < resultsSize; i++) {
             // Separate out the flag and argument strings
             flagStr = null;
             argStr = null;
             temp = (String) resultsVec.elementAt(i);
             p = temp.indexOf('(');
             q = temp.indexOf(')', p + 1);
 
             if ((p != -1) && (q > p)) {
                 flagStr = temp.substring(p + 1, q);
             }
 
             if (temp.length() > (q + 2)) {
                 argStr = temp.substring(q + 2);
             }
 
             // List response is invalid if both parts are not available
             if ((flagStr == null) || (argStr == null)) {
                 continue;
             }
 
             response = new ListResponse();
             response.delim = "";
             response.name = "";
 
             // Look for flags if present.
             response.canSelect = !(flagStr.indexOf(FLAG_NOSELECT) != -1);
             response.hasChildren = (flagStr.indexOf(FLAG_HAS_CHILDREN) != -1);
             response.noInferiors = (flagStr.indexOf(FLAG_NOINFERIORS) != -1);
             response.marked = (flagStr.indexOf(FLAG_MARKED) != -1);
 
             try {
                 p = 0;
                 q = 0;
 
                 int size = argStr.length();
 
                 if (argStr.charAt(p) == '"') {
                     p++;
                     q = p + 1;
 
                     while ((q < size) && (argStr.charAt(q) != '"')) {
                         q++;
                     }
 
                     response.delim = argStr.substring(p, q);
                     p = q + 2;
                 } else {
                     q = p + 1;
 
                     while ((q < size) && (argStr.charAt(q) != ' ')) {
                         q++;
                     }
 
                     response.delim = argStr.substring(p, q);
                     p = q + 1;
                 }
 
                 if (response.delim.equalsIgnoreCase(NIL)) {
                     response.delim = "";
                 }
 
                 if (argStr.charAt(p) == '"') {
                     p++;
                     q = p + 1;
 
                     while ((q < size) && (argStr.charAt(q) != '"')) {
                         q++;
                     }
 
                     response.name = argStr.substring(p, q);
                 } else {
                     q = p + 1;
 
                     while ((q < size) && (argStr.charAt(q) != ' ')) {
                         q++;
                     }
 
                     response.name = argStr.substring(p, q);
                 }
 
                 // Strip any escaped characters
                 response.name = StringParser.removeEscapedChars(response.name);
                 response.delim = StringParser.removeEscapedChars(response.delim);
 
                 // Only add if the response is not a duplicate of the request
                 if (!(refName.equals(response.name + response.delim) ||
                         refName.equals(response.name))) {
                     retVec.addElement(response);
                 }
             } catch (Exception e) {
                 // Prevent parse errors from being fatal
             }
 
             if (progressHandler != null) {
                 progressHandler.mailProgress(MailProgressHandler.TYPE_PROCESSING,
                     i, resultsSize);
             }
         }
 
         if (progressHandler != null) {
             progressHandler.mailProgress(MailProgressHandler.TYPE_PROCESSING,
                 resultsSize, resultsSize);
         }
 
         return retVec;
     }
 
     /**
      * Execute the "NOOP" command.
      * The parsing of the result is currently very simple, and just looks
      * for the presence of a "* ?? recent" untagged response.
      */
     public boolean executeNoop() throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeNoop()").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         String[] replyText = execute(NOOP, null, null);
 
         if (replyText == null) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("Unable to read NOOP response").getBytes(), EventLogger.WARNING);
 
             return false;
         }
         else if(replyText.length < 1) {
             return false;
         }
         
         for (int i = 0; i < replyText.length; i++) {
             if (replyText[i].startsWith(ASTERISK) &&
                     replyText[i].toLowerCase().endsWith("recent")) {
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * Execute the "IDLE" command.
      */
     public void executeIdle() throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeIdle()").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         idleCommandTag = executeNoReply(IDLE, null);
     }
 
     /**
      * Execute the "DONE" command.
      */
     public void executeIdleDone() throws IOException, MailException {
         if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
             EventLogger.logEvent(AppInfo.GUID,
                 ("ImapProtocol.executeIdleDone()").getBytes(),
                 EventLogger.DEBUG_INFO);
         }
 
         executeUntagged(DONE, null, idleCommandTag);
         idleCommandTag = null;
     }
 
     /**
      * Polls the connection during the IDLE state.
      * For now, this is a simple implementation.  It just checks for
      * the presence of a "* ?? recent" untagged response, and returns
      * true if one is found.
      */
     public boolean executeIdlePoll() throws IOException, MailException {
         String result = receive();
 
         if ((result != null) && result.startsWith(ASTERISK) &&
                 result.toLowerCase().endsWith("recent")) {
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * Executes an IMAP command several times, with different arguments,
      * and return the replies as an array of strings.
      * @param command IMAP command
      * @param arguments Arguments for the commands
      * @param progressHandler the progress handler
      * @return List of returned strings
      */
     protected String[] executeBatch(String command, String[] arguments,
         MailProgressHandler progressHandler) throws IOException, MailException {
         String[] result = new String[arguments.length];
         int count = 0;
 
         IntIntHashtable commandMap = new IntIntHashtable();
         StringBuffer commandBuf = new StringBuffer();
 
         for (int i = 0; i < arguments.length; i++) {
             String tag = TAG_PREFIX + (commandCount++);
             commandMap.put(StringArrays.hashCode(tag.getBytes()), i);
             commandBuf.append(tag);
             commandBuf.append(' ');
             commandBuf.append(command);
             commandBuf.append(((arguments[i] == null) ? ""
                                                       : (CHAR_SP +
                 arguments[i])));
             commandBuf.append(CRLF);
         }
 
         int preCount = connection.getBytesReceived();
         connection.sendRaw(commandBuf.toString());
 
         int postCount = connection.getBytesReceived();
 
         if (progressHandler != null) {
             progressHandler.mailProgress(MailProgressHandler.TYPE_NETWORK,
                 (postCount - preCount), -1);
         }
 
         byte[] temp;
         String tempResult = "";
         int p;
 
         while (count < arguments.length) {
             preCount = postCount;
             temp = connection.receive();
             postCount = connection.getBytesReceived();
 
             if (progressHandler != null) {
                 progressHandler.mailProgress(MailProgressHandler.TYPE_NETWORK,
                     (postCount - preCount), -1);
             }
 
             p = Arrays.getIndex(temp, (byte) ' ');
 
             if ((StringArrays.indexOf(temp, BAD_PREFIX, p + 1) != -1) ||
                     (StringArrays.indexOf(temp, NO_PREFIX, p + 1) != -1)) {
                 throw new MailException(new String(temp));
             }
 
             int keyHashCode = StringArrays.hashCode(temp, 0, p);
 
             if ((p != -1) && commandMap.containsKey(keyHashCode)) {
                 result[commandMap.get(keyHashCode)] = tempResult;
                 tempResult = "";
                 count++;
             } else {
                 tempResult = new String(temp);
             }
         }
 
         return result;
     }
 
     /**
      * Executes an IMAP command, and returns the reply as an
      * array of strings.
      * @param command IMAP command
      * @param arguments Arguments for the command
      * @param progressHandler the progress handler
      * @return List of returned strings
      */
     protected byte[][] executeResponse(String command, String arguments,
         MailProgressHandler progressHandler) throws IOException, MailException {
         byte[][] result = new byte[0][];
 
         String tag = TAG_PREFIX + commandCount++ + CHAR_SP;
         connection.sendCommand(tag + command +
             ((arguments == null) ? "" : (CHAR_SP + arguments)));
 
         byte[] tagBytes = tag.getBytes();
 
         int preCount = connection.getBytesReceived();
         byte[] temp = connection.receive(executeResponseTester);
         int postCount = connection.getBytesReceived();
 
         if (progressHandler != null) {
             progressHandler.mailProgress(MailProgressHandler.TYPE_NETWORK,
                 (postCount - preCount), -1);
         }
 
         while (!StringArrays.startsWith(temp, tagBytes)) {
             Arrays.add(result, temp);
             preCount = postCount;
             temp = connection.receive(executeResponseTester);
             postCount = connection.getBytesReceived();
 
             if (progressHandler != null) {
                 progressHandler.mailProgress(MailProgressHandler.TYPE_NETWORK,
                     (postCount - preCount), -1);
             }
         }
 
         if (StringArrays.startsWith(temp, BAD_PREFIX, tagBytes.length) ||
                 StringArrays.startsWith(temp, NO_PREFIX,
                     tagBytes.length)) {
             throw new MailException(new String(temp));
         }
 
         return result;
     }
 
     /**
      * Executes an IMAP command, invoking the supplied callback on each line
      * of the response.
      * 
      * @param command IMAP command
      * @param arguments Arguments for the command
      * @param callback the callback to invoke on each line of the response
      * @param progressHandler the progress handler
      * @return List of returned strings
      */
     protected void executeResponse(String command, String arguments,
             ExecuteCallback callback, MailProgressHandler progressHandler)
     throws IOException, MailException {
         
         String tag = TAG_PREFIX + commandCount++ + CHAR_SP;
         connection.sendCommand(tag + command +
             ((arguments == null) ? "" : (CHAR_SP + arguments)));
 
         byte[] tagBytes = tag.getBytes();
 
         int preCount = connection.getBytesReceived();
         byte[] temp = connection.receive(executeResponseTester);
         int postCount = connection.getBytesReceived();
 
         if (progressHandler != null) {
             progressHandler.mailProgress(MailProgressHandler.TYPE_NETWORK,
                 (postCount - preCount), -1);
         }
 
         while (!StringArrays.startsWith(temp, tagBytes)) {
             try {
                 callback.processResponse(temp);
             } catch (Throwable t) {
                 EventLogger.logEvent(AppInfo.GUID,
                         ("Unable to parse response: " + t.getMessage()).getBytes(),
                         EventLogger.ERROR);
             }
             
             preCount = postCount;
             temp = connection.receive(executeResponseTester);
             postCount = connection.getBytesReceived();
 
             if (progressHandler != null) {
                 progressHandler.mailProgress(MailProgressHandler.TYPE_NETWORK,
                     (postCount - preCount), -1);
             }
         }
 
         try {
             callback.executeComplete();
         } catch (Throwable t) {
             EventLogger.logEvent(AppInfo.GUID,
                     ("Unable to complete processing the response: " + t.getMessage()).getBytes(),
                     EventLogger.ERROR);
         }
         
         if (StringArrays.startsWith(temp, BAD_PREFIX, tagBytes.length) ||
                 StringArrays.startsWith(temp, NO_PREFIX,
                     tagBytes.length)) {
             throw new MailException(new String(temp));
         }
     }
     
     /**
      * Callback interface for <code>executeResponse()</code>.
      */
     protected interface ExecuteCallback {
         void processResponse(byte[] rawLine);
         void executeComplete();
     }
     
     /**
      * Executes an IMAP command, and returns the reply as an
      * array of strings.
      * @param command IMAP command
      * @param arguments Arguments for the command
      * @param progressHandler the progress handler
      * @return List of returned strings
      */
     protected String[] execute(String command, String arguments,
         MailProgressHandler progressHandler) throws IOException, MailException {
         String[] result = new String[0];
 
         String tag = TAG_PREFIX + commandCount++ + CHAR_SP;
         connection.sendCommand(tag + command +
             ((arguments == null) ? "" : (CHAR_SP + arguments)));
 
         byte[] tagBytes = tag.getBytes();
 
         int preCount = connection.getBytesReceived();
         byte[] temp = connection.receive();
         int postCount = connection.getBytesReceived();
 
         if (progressHandler != null) {
             progressHandler.mailProgress(MailProgressHandler.TYPE_NETWORK,
                 (postCount - preCount), -1);
         }
 
         while (!StringArrays.startsWith(temp, tagBytes)) {
             Arrays.add(result, new String(temp));
             preCount = postCount;
             temp = connection.receive();
             postCount = connection.getBytesReceived();
 
             if (progressHandler != null) {
                 progressHandler.mailProgress(MailProgressHandler.TYPE_NETWORK,
                     (postCount - preCount), -1);
             }
         }
 
         if (StringArrays.startsWith(temp, BAD_PREFIX, tagBytes.length) ||
                 StringArrays.startsWith(temp, NO_PREFIX,
                     tagBytes.length)) {
             throw new MailException(new String(temp));
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
     protected String[] executeContinue(String command, String arguments,
         String text, String errorMsg) throws IOException, MailException {
         String[] result = new String[0];
 
         String tag = TAG_PREFIX + commandCount++ + CHAR_SP;
         connection.sendCommand(tag + command +
             ((arguments == null) ? "" : (CHAR_SP + arguments)));
 
         byte[] tagBytes = tag.getBytes();
 
         byte[] temp = connection.receive();
 
         if (Arrays.getIndex(temp, CHAR_PLUS) == -1) {
             throw new MailException(errorMsg);
         }
 
         connection.sendRaw(text);
         connection.sendRaw(CRLF);
 
         temp = connection.receive();
 
         while (!StringArrays.startsWith(temp, tagBytes)) {
             Arrays.add(result, new String(temp));
             temp = connection.receive();
         }
 
         if (StringArrays.startsWith(temp, BAD_PREFIX, tagBytes.length) ||
                 StringArrays.startsWith(temp, NO_PREFIX,
                     tagBytes.length)) {
             throw new MailException(new String(temp));
         }
 
         return result;
     }
 
     /**
      * Attempts to read a line of text from the server, without sending
      * anything first.
      * @return Returned string, or null if nothing is available
      */
     protected String receive() throws IOException, MailException {
         String result;
 
         if (connection.available() > 0) {
             result = new String(connection.receive());
         } else {
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
         throws IOException, MailException {
         String tag = TAG_PREFIX + commandCount++ + CHAR_SP;
         connection.sendCommand(tag + command +
             ((arguments == null) ? "" : (CHAR_SP + arguments)));
 
         return tag;
     }
 
     /**
      * Executes an IMAP command without a tag prefix,
      * and returns the reply as an array of strings.
      * @param command IMAP command
      * @param arguments Arguments for the command
      * @param endTag Known tag that indicates the end of the result
      * @return List of returned strings
      */
     protected String[] executeUntagged(String command, String arguments,
         String endTag) throws IOException, MailException {
         String[] result = new String[0];
 
         connection.sendCommand(command +
             ((arguments == null) ? "" : (CHAR_SP + arguments)));
 
         byte[] tagBytes = endTag.getBytes();
         byte[] temp = connection.receive();
 
         while (!StringArrays.startsWith(temp, tagBytes)) {
             Arrays.add(result, new String(temp));
             temp = connection.receive();
         }
 
         if (StringArrays.startsWith(temp, BAD_PREFIX, tagBytes.length) ||
                 StringArrays.startsWith(temp, NO_PREFIX,
                     tagBytes.length)) {
             throw new MailException(new String(temp));
         }
 
         return result;
     }
 
     /**
      * Callback for fetching envelopes.
      */
     public static interface FetchEnvelopeCallback {
         void responseAvailable(FetchEnvelopeResponse response);
     }
 
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
 
         /** Message has been forwarded */
         public boolean forwarded;
 
         /** Message has been flagged as junk */
         public boolean junk;
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
         public Namespace[] personal;
 
         /** Other Users' Namespace(s) */
         public Namespace[] other;
 
         /** Shared Namespace(s) */
         public Namespace[] shared;
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
         public int uidNext = -1;
         public int uidValidity;
     }
 
     /**
      * Container for a STATUS response
      */
     public static class StatusResponse {
         public int exists;
         public int unseen;
     }
 
     /**
      * Container for a FETCH (FLAGS) response
      */
     public static class FetchFlagsResponse {
         public int index;
         public int uid;
         public MessageFlags flags;
     }
 
     /**
      * Container for a FETCH (ENVELOPE) response
      */
     public static class FetchEnvelopeResponse extends FetchFlagsResponse {
         public MessageEnvelope envelope;
         public ImapParser.MessageSection structure;
     }
 
     /**
      * Container for a LIST response
      */
     public static class ListResponse {
         public boolean hasChildren;
         public boolean noInferiors;
         public boolean canSelect;
         public boolean marked;
         public String delim;
         public String name;
     }
     
     // String constants
     private static String UID_COPY = "UID COPY";
     private static String UID_STORE = "UID STORE";
     private static String UID_FETCH = "UID FETCH";
     private static String DONE = "DONE";
     private static String IDLE = "IDLE";
     private static String NOOP = "NOOP";
     private static String NIL = "NIL";
     private static String LSUB = "LSUB";
     private static String LIST = "LIST";
     private static String FETCH = "FETCH";
     private static String BODYSTRUCTURE = "BODYSTRUCTURE";
     private static String BODY = "BODY";
     private static String ENVELOPE = "ENVELOPE";
     private static String UID = "UID";
     private static String FLAGS = "FLAGS";
     private static String UNSEEN = "UNSEEN";
     private static String MESSAGES = "MESSAGES";
     private static String NAMESPACE = "NAMESPACE";
     private static String CAPABILITY = "CAPABILITY";
     private static String APPEND = "APPEND";
     private static String STATUS = "STATUS";
     private static String SELECT = "SELECT";
     private static String CLOSE = "CLOSE";
     private static String LOGOUT = "LOGOUT";
     private static String LOGIN = "LOGIN";
     private static String STARTTLS = "STARTTLS";
     private static String FLAG_MARKED = "\\Marked";
     private static String FLAG_NOINFERIORS = "\\Noinferiors";
     private static String FLAG_HAS_CHILDREN = "\\HasChildren";
     private static String FLAG_NOSELECT = "\\Noselect";
     private static String TAG_PREFIX = "A";
     private static final byte[] NO_PREFIX = "NO ".getBytes();
     private static final byte[] BAD_PREFIX = "BAD ".getBytes();
     private static String CHAR_SP = " ";
     private static final byte CHAR_PLUS = (byte) '+';
     private static String CHAR_COLON = ":";
     private static final byte CHAR_ASTERISK = (byte)'*';
     private static String ASTERISK = "*";
     private static String CHAR_QUOTE = "\"";
     private static String CHAR_COLON_ASTERISK = ":*";
     private static String CRLF = "\r\n";
 }
