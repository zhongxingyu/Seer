 /**
  * This file is part of RibbonServer application (check README).
  * Copyright (C) 2012-2013 Stanislav Nepochatov
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 **/
 
 package ribbonserver;
 
 import java.util.Iterator;
 
 /**
  * Network sessions store class
  * @author Stanislav Nepochatov
  * @since RibbonServer a1
  */
 public final class SessionManager {
     
     private static String LOG_ID = "СЕСІЯ";
     
     /**
      * Arraylist with active network sessions.
      * @since RibbonServer a1
      */
     private static java.util.ArrayList<SessionManager.SessionThread> sessionsStore = new java.util.ArrayList<>();
     
     /**
      * List with stored session entries.
      * @since RibbonServer a2
      */
     public static java.util.ArrayList<SessionManager.SessionEntry> sessionCookie = new java.util.ArrayList<>();
     
     /**
      * Session entry class for quick session resume.
      * @since RibbonServer a2
      */
     public static class SessionEntry extends Generic.CsvElder {
         
         /**
          * Hash id of the session.
          */
         public String SESSION_HASH_ID;
         
         /**
          * Name of the user in that session.
          */
         public String SESSION_USER_NAME;
         
         /**
          * Count of how many times this session used.
          */
         private Integer COUNT;
         
         /**
          * If this entry is obselete.
          */
         public Boolean IS_OBSELETE = false;
         
         /**
          * Empty constructor.
          */
         public SessionEntry() {
             this.baseCount = 3;
             this.currentFormat = Generic.CsvElder.csvFormatType.SimpleCsv;
         }
         
         /**
          * Build object with given csv string.
          * @param givenCsv 
          */
         public SessionEntry(String givenCsv) {
             this();
             java.util.ArrayList<String[]> parsedStruct = Generic.CsvFormat.fromCsv(this, givenCsv);
             String[] baseArray = parsedStruct.get(0);
             this.SESSION_HASH_ID = baseArray[0];
             this.SESSION_USER_NAME = baseArray[1];
             this.COUNT = Integer.parseInt(baseArray[2]);
         }
         
         /**
          * Init empty session entry with given user name.
          */
         public void initWithName(String givenName) {
             this.SESSION_USER_NAME = givenName;
             this.SESSION_HASH_ID = RibbonServer.getHash(this.SESSION_USER_NAME + RibbonServer.getCurrentDate());
             this.COUNT = 0;
         }
         
         /**
          * Use this entry.
          */
         public void useEntry() {
             ++this.COUNT;
             if (this.COUNT == RibbonServer.ACCESS_SESSION_MAX_COUNT) {
                 this.IS_OBSELETE = true;
             }
         }
 
         @Override
         public String toCsv() {
             return this.SESSION_HASH_ID + ",{" + this.SESSION_USER_NAME + "}," + this.COUNT;
         }
     }
     
     /**
      * Single client session class.
      * @since RibbonServer a1
      */
     public static class SessionThread extends Thread {
         
         /**
          * User name of this session.
          */
         public String USER_NAME;
         
         /**
          * User session entry.
          */
         public SessionEntry CURR_ENTRY;
         
         /**
          * Short session description.
          * @since RibbonServer a2
          */
         public String SESSION_TIP;
         
         /**
          * Is session alive.
          */
         public Boolean isAlive = false;
         
         /**
          * Session network socket.
          */
         private java.net.Socket SessionSocket;
         
         /**
          * Input stream from client.
          */
         public java.io.BufferedReader inStream;
         
         /**
          * Output writer to client.
          */
         private java.io.PrintWriter outStream;
         
         /**
          * Lock object for <code>outStream</code>.
          * @since RibbonServer a2
          */
         private final Object outputLock = new Object();
         
         /**
          * Protocol handler, parser and executor.
          */
         private RibbonProtocol ProtocolHandler;
         
         /**
          * Default constructor.
          * @param givenSocket session socket;
          */
         SessionThread(java.net.Socket givenSocket) {
             SessionSocket = givenSocket;
             try {
                 inStream = new java.io.BufferedReader(new java.io.InputStreamReader(SessionSocket.getInputStream(), "UTF-8"));
                 outStream = new java.io.PrintWriter(SessionSocket.getOutputStream(), true);
             } catch (java.io.IOException ex) {
                 RibbonServer.logAppend(LOG_ID, 1, "неможливо створити потоки для мережевого сокета (" + SessionSocket.getInetAddress().getHostAddress() + ")");
             } finally {
                 RibbonServer.logAppend(LOG_ID, 3, "додана нова мережева сессія (" + SessionSocket.getInetAddress().getHostAddress() + ")");
                 ProtocolHandler = new RibbonProtocol(this);
                 this.isAlive = true;
             }
         }
         
         @Override
         public void run() {
             String inputLine, outputLine;
             this.setSessionName();
             try {
                 while (this.isAlive == true) {
                     inputLine = inStream.readLine();
                     synchronized (outputLock) {
                         String answer = this.ProtocolHandler.process(inputLine);
                         if (answer.equals("COMMIT_CLOSE:")) {
                             isAlive = false;
                         }
                         this.outStream.println(answer);
                     }
                     if (this.ProtocolHandler.BROADCAST_TAIL != null) {
                         broadcast(this.ProtocolHandler.BROADCAST_TAIL, this.ProtocolHandler.BROADCAST_TYPE);
                         this.ProtocolHandler.BROADCAST_TAIL = null;
                         this.ProtocolHandler.BROADCAST_TYPE = null;
                     }
                 }
                 this.inStream.close();
                 this.outStream.close();
                 this.SessionSocket.close();
                 RibbonServer.logAppend(LOG_ID, 3, "мережеву сесію зачинено (" + SessionSocket.getInetAddress().getHostAddress() + ")");
                 this.isAlive = false;
                 SessionManager.closeSession(this);
             } catch (java.lang.NullPointerException ex) {
                 RibbonServer.logAppend(LOG_ID, 1, "з'єднання аварійно разірване!");
                 this.isAlive = false;
                 SessionManager.closeSession(this);
             } catch (java.io.IOException ex) {
                 RibbonServer.logAppend(LOG_ID, 1, "неможливо прочитати дані з сокету (" + SessionSocket.getInetAddress().getHostAddress() + ")");
                 this.isAlive = false;
                 SessionManager.closeSession(this);
             }
         }
         
         /**
          * Set name of session thread.
          * @since RibbonServer a2
          */
         public void setSessionName() {
             this.SESSION_TIP = "[" + this.USER_NAME + "] на " + this.SessionSocket.getInetAddress().getHostName();
         }
         
         /**
          * Set reader encoding.
          * @since RibbonServer a2
          */
         public void setReaderEncoding(String charsetName) {
             try {
                 this.inStream = new java.io.BufferedReader(new java.io.InputStreamReader(SessionSocket.getInputStream(), charsetName));
             } catch (java.io.UnsupportedEncodingException ex) {
                 RibbonServer.logAppend(LOG_ID, 1, "неможливо встановити кодову сторінку!");
             } catch (java.io.IOException ex) {
                 RibbonServer.logAppend(LOG_ID, 1, "неможливо прочитати дані з сокету (" + SessionSocket.getInetAddress().getHostAddress() + ")");
             }
         }
         
         /**
          * Send message to this session peer.
          * @param message message to send;
          */
         public void printLnToPeer(String message) {
             synchronized (outputLock) {
                 outStream.println(message);
             }
         }
     }
     
     /**
      * Init session manager.
      * @since RibbonServer a2
      */
     public static void init() {
         SessionManager.sessionCookie = IndexReader.readSessionIndex();
         RibbonServer.logAppend(LOG_ID, 3, "індекс сесій вдало завантажено");
     }
 
     /**
      * Create new session and add it into session list;
      * @param givenSocket socket to open session;
      * @since RibbonServer a1
      */
     public static void createNewSession(java.net.Socket givenSocket) {
         SessionManager.SessionThread createdThread = new SessionManager.SessionThread(givenSocket);
         if (createdThread.isAlive) {
             SessionManager.sessionsStore.add(createdThread);
             createdThread.start();
         }
     }
     
     /**
      * Close session and delete it from sessaion array;
      * @param givenSession sessionb to close;
      * @since RibbonServer a1
      */
     public static void closeSession(SessionManager.SessionThread givenSession) {
         if (!givenSession.isAlive) {
             SessionManager.sessionsStore.remove(givenSession);
         }
     }
     
     /**
      * Broadcast message to all users
      * @param message a single line message
      * @since RibbonServer a1
      */
     public static void broadcast(String message, RibbonProtocol.CONNECTION_TYPES type) {
         java.util.ListIterator<SessionThread> sessionIter = SessionManager.sessionsStore.listIterator();
         while (sessionIter.hasNext()) {
             SessionThread currSession = sessionIter.next();
             if (currSession.ProtocolHandler.CURR_TYPE == type) {
                 currSession.printLnToPeer(message);
             }
         }
     }
     
     /**
      * Check if there is other control connection for the system
      * @param closingControlThread thread which going to close
      * @return result of checking
      * @since RibbonServer a2
      */
     public static Boolean hasOtherControl(SessionThread closingControlThread) {
         java.util.ListIterator<SessionThread> sessionIter = SessionManager.sessionsStore.listIterator();
         while (sessionIter.hasNext()) {
             SessionThread currSession = sessionIter.next();
             if (currSession.ProtocolHandler.CURR_TYPE == RibbonProtocol.CONNECTION_TYPES.CONTROL && !currSession.equals(closingControlThread)) {
                 return true;
             }
         }
         return false;
     }
     
     /**
      * Check server connection limit.
      * @return true if limit achieved/fals if not;
      * @since RibbonServer a2
      */
     public static Boolean checkConnectionLimit() {
         if (RibbonServer.NETWORK_MAX_CONNECTIONS != -1 && SessionManager.sessionsStore.size() == RibbonServer.NETWORK_MAX_CONNECTIONS) {
             RibbonServer.logAppend(LOG_ID, 1, "досягнуто ліміту з'єднань (" + RibbonServer.NETWORK_MAX_CONNECTIONS + ")");
             return true;
         } else {
             return false;
         }
     }
     
     /**
      * Find out is user is already logined in system.
      * @param givenName name of user to search;
      * @return true if user is already logined.
      * @since RibbonServer a2
      */
     public static Boolean isAlreadyLogined(String givenName) {
        for (Iterator<SessionThread> it = SessionManager.sessionsStore.iterator(); it.hasNext();) {
            SessionThread iterSess = it.next();
             if (iterSess.USER_NAME == null) {
                 continue;
             }
             if (iterSess.USER_NAME.equals(givenName)) {
                 return true;
             }
         }
         return false;
     }
     
     /**
      * Create new session entry and add it to list.
      * @param givenUser user name for session;
      * @return new created session entry;
      * @since RibbonServer a2
      */
     public static SessionManager.SessionEntry createSessionEntry(String givenUser) {
         SessionManager.SessionEntry existed = null;
         java.util.ListIterator<SessionManager.SessionEntry> cookIter = SessionManager.sessionCookie.listIterator();
         while (cookIter.hasNext()) {
             SessionManager.SessionEntry currEntry = cookIter.next();
             if (currEntry.SESSION_USER_NAME.equals(givenUser)) {
                 existed = currEntry;
                 break;
             }
         }
         SessionManager.SessionEntry returned = null;
         if (existed != null) {
             existed.initWithName(givenUser);
             IndexReader.updateSessionIndex();
             return existed;
         } else {
             returned = new SessionManager.SessionEntry();
             returned.initWithName(givenUser);
             SessionManager.sessionCookie.add(returned);
             IndexReader.appendToSessionIndex(returned.toCsv());
             return returned;
         }
     }
     
     /**
      * Get session entry from persistent session store.
      * @param givenHashId hash id of the session;
      * @return username of user or null;
      * @since RibbonServer a2
      */
     public static SessionManager.SessionEntry getUserBySessionEntry(String givenHashId) {
         SessionManager.SessionEntry findedSession = null;
         java.util.ListIterator<SessionManager.SessionEntry> cookIter = SessionManager.sessionCookie.listIterator();
         while (cookIter.hasNext()) {
             SessionManager.SessionEntry currEntry = cookIter.next();
             if (currEntry.SESSION_HASH_ID.equals(givenHashId)) {
                 findedSession = currEntry;
                 break;
             }
         }
         if (findedSession != null) {
             return findedSession;
         } else {
             return null;
         }
     }
     
     /**
      * Renew session entry index file or remove it from index.
      * @param givenEntry entry to check;
      * @since RibbonServer a2
      */
     public static void reniewEntry(SessionManager.SessionEntry givenEntry) {
         if (givenEntry.IS_OBSELETE) {
             SessionManager.sessionCookie.remove(givenEntry);
         }
         IndexReader.updateSessionIndex();
     }
 }
