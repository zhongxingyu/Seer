 /*******************************************************************************
  * * Copyright (c) 2001-2005 quickfixengine.org All rights reserved. * * This
  * file is part of the QuickFIX FIX Engine * * This file may be distributed
  * under the terms of the quickfixengine.org * license as defined by
  * quickfixengine.org and appearing in the file * LICENSE included in the
  * packaging of this file. * * This file is provided AS IS with NO WARRANTY OF
  * ANY KIND, INCLUDING THE * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE. * * See http://www.quickfixengine.org/LICENSE for
  * licensing information. * * Contact ask@quickfixengine.org if any conditions
  * of this licensing are * not clear to you. *
  ******************************************************************************/
 
 package quickfix.netty;
 
 import net.gleamynode.netty2.IoProcessor;
 import net.gleamynode.netty2.LowLatencyEventDispatcher;
 import net.gleamynode.netty2.Message;
 import net.gleamynode.netty2.Session;
 import net.gleamynode.netty2.SessionListener;
 import org.apache.commons.logging.Log;
 import quickfix.*;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Timer;
 import java.util.TimerTask;
 
 public abstract class AbstractSocketInitiator implements Initiator {
     protected Log log = org.apache.commons.logging.LogFactory.getLog(getClass());
     private static final String DEFAULT_IO_THREAD_PREFIX = "quickfix-io";
     private boolean isStopRequested;
     private final SessionSettings settings;
     private final SessionFactory sessionFactory;
     private boolean firstPoll = true;
     protected Thread quickFixThread;
     private IoProcessor ioProcessor;
     private ArrayList sessionConnections = new ArrayList();
     protected ArrayList quickfixSessions = new ArrayList();
     private LowLatencyEventDispatcher eventDispatcher;
     private Timer timer = new Timer();
     private long stopRequestTimestamp;
     private boolean initialized;
 
     protected AbstractSocketInitiator(Application application,
             MessageStoreFactory messageStoreFactory, SessionSettings settings,
             MessageFactory messageFactory) {
         this(application, messageStoreFactory, settings, new ScreenLogFactory(settings),
                 messageFactory);
     }
 
     protected AbstractSocketInitiator(Application application,
             MessageStoreFactory messageStoreFactory, SessionSettings settings,
             LogFactory logFactory, MessageFactory messageFactory) {
         this(new DefaultSessionFactory(application, messageStoreFactory, logFactory, messageFactory), settings);
     }
 
     protected AbstractSocketInitiator(SessionFactory sessionFactory, SessionSettings settings) {
         this.sessionFactory = sessionFactory;
         this.settings = settings;
     }
 
     protected abstract void onBlock();
 
     protected abstract void onStart();
 
     public final void block() throws ConfigError, RuntimeError {
         initialize(true);
         onBlock();
     }
 
     protected abstract void onMessage(Message message);
 
     protected abstract void onTimerEvent(quickfix.Session quickfixSession);
 
     protected abstract boolean onPoll();
 
     public final boolean poll() throws ConfigError, RuntimeError {
         if (firstPoll) {
             initialize(true);
             firstPoll = false;
         }
 
         return onPoll();
     }
 
     public void start() throws ConfigError, RuntimeError {
        initialize(isHandlingMessageInCallingThread());
         onStart();
     }
 
	protected abstract boolean isHandlingMessageInCallingThread();

     protected abstract void onStop();
 
     public final void stop() {
         stop(false);
     }
 
     public final void stop(boolean force) {
         onStop();
 
         if (!isStopRequested) {
             isStopRequested = true;
             stopRequestTimestamp = System.currentTimeMillis();
         }
 
         synchronized (sessionConnections) {
             for (int i = 0; i < sessionConnections.size(); i++) {
                 ((SessionConnection) sessionConnections.get(i)).getQuickFixSession().logout();
             }
         }
 
         if (!force) {
             for (int second = 1; second <= 10 && isLoggedOn(); ++second) {
                 try {
                     Thread.sleep(1000L);
                 } catch (Exception e) {
                     log.error(e);
                 }
             }
         }
 
         timer.cancel();
         ioProcessor.stop();
     }
 
     protected boolean isStopRequested() {
         return isStopRequested;
     }
 
     protected abstract void onInitialize(boolean isBlocking);
 
     private class SessionTimerTask extends TimerTask {
         public void run() {
             synchronized (sessionConnections) {
                 for (int i = 0; i < sessionConnections.size(); i++) {
                     ((SessionConnection) sessionConnections.get(i)).onTimerEvent();
                 }
             }
         }
     }
 
     private void initialize(boolean handleMessageInCaller) throws ConfigError {
         if (initialized) {
             return;
         }
         try {
             boolean continueInitOnError = false;
             if (settings.isSetting(SessionFactory.SETTING_CONTINUE_INIT_ON_ERROR)) {
                 continueInitOnError = settings
                         .getBool(SessionFactory.SETTING_CONTINUE_INIT_ON_ERROR);
             }
 
             onInitialize(handleMessageInCaller);
             eventDispatcher = new LowLatencyEventDispatcher();
             ioProcessor = new IoProcessor();
             ioProcessor.setThreadNamePrefix(DEFAULT_IO_THREAD_PREFIX);
             ioProcessor.setThreadPoolSize(1);
             ioProcessor.start();
 
             timer.schedule(new SessionTimerTask(), 1000L, 1000L);
 
             for (Iterator i = settings.sectionIterator(); i.hasNext();) {
                 SessionID sectionKey = (SessionID) i.next();
                 if (isInitiatorSession(sectionKey)) {
                     try {
                         sessionConnections.add(new SessionConnection(settings, sectionKey));
                     } catch (Throwable e) {
                         if (continueInitOnError) {
                             log.error("error during session initialization, continuing...", e);
                         } else {
                             throw new RuntimeError("error during session initialization", e);
                         }
                     }
                 }
             }
             if (sessionConnections.size() == 0) {
                 throw new ConfigError("no initiators in settings");
             }
         } catch (FieldConvertError e) {
             throw new ConfigError(e);
         } catch (IOException e) {
             throw new RuntimeError(e);
         } finally {
             initialized = true;
         }
     }
 
     private boolean isInitiatorSession(Object sectionKey) throws ConfigError, FieldConvertError {
         return !settings.isSetting((SessionID) sectionKey, SessionFactory.SETTING_CONNECTION_TYPE)
                 || settings.getString((SessionID) sectionKey,
                         SessionFactory.SETTING_CONNECTION_TYPE).equals("initiator");
     }
 
     protected void processTimerEvent(quickfix.Session quickfixSession) {
         try {
             quickfixSession.next();
         } catch (IOException e) {
             LogUtil.logThrowable(quickfixSession.getLog(), "error while processing timer event", e);
         }
     }
 
     protected void processMessage(Message message) {
         FIXMessageData fixMessageData = (FIXMessageData) message;
         quickfix.Session quickfixSession = fixMessageData.getSession();
         try {
             quickfixSession.getState().logIncoming(fixMessageData.toString());
             DataDictionary dataDictionary = quickfixSession.getDataDictionary();
             quickfix.Message fixMessage = fixMessageData.parse(dataDictionary);
             try {
                 quickfixSession.next(fixMessage);
             } catch (Throwable e) {
                 quickfix.Log sessionLog = quickfixSession.getLog();
                 LogUtil.logThrowable(sessionLog, "error while receiving message", e);
                 if (fixMessageData.isLogon()) {
                     try {
                         quickfixSession.disconnect();
                     } catch (IOException ioException) {
                         LogUtil.logThrowable(sessionLog, "error during disconnect", ioException);
                     }
                 }
             }
 
         } catch (InvalidMessage e) {
             // TODO CLEANUP Handle Invalid Message During Parsing
             // Generate a session-level reject for the message
             // The problem here is that the fixMessage was not parsed.
             //quickfixSession.generateReject(fixMessage, "invalid message
             // format");
             LogUtil.logThrowable(quickfixSession.getLog(), "error during message parsing", e);
         }
     }
 
     protected long getStopRequestTimestamp() {
         return stopRequestTimestamp;
     }
 
     private class SessionConnection {
         private quickfix.Session quickfixSession;
         private ArrayList nettySessions = new ArrayList();
         private Session nettySession;
         public boolean responderDisconnected;
         private long lastReconnectAttemptTime = 0;
         private long reconnectInterval;
 
         public SessionConnection(SessionSettings settings, SessionID sessionID) throws ConfigError {
             if (settings.isSetting(sessionID, Initiator.SETTING_RECONNECT_INTERVAL)) {
                 try {
                     reconnectInterval = settings.getLong(sessionID,
                             Initiator.SETTING_RECONNECT_INTERVAL) * 1000L;
                 } catch (ConfigError e) {
                     throw e;
                 } catch (FieldConvertError e) {
                     throw (ConfigError) new ConfigError(e.getMessage()).initCause(e);
                 }
             } else {
                 reconnectInterval = 30000L;
             }
 
             quickfixSession = sessionFactory.create(sessionID, settings);
 
             synchronized (quickfixSessions) {
                 quickfixSessions.add(quickfixSession);
             }
 
             for (int index = 0;; index++) {
                 try {
                     String hostKey = Initiator.SETTING_SOCKET_CONNECT_HOST
                             + (index == 0 ? "" : Integer.toString(index));
                     String portKey = Initiator.SETTING_SOCKET_CONNECT_PORT
                             + (index == 0 ? "" : Integer.toString(index));
                     if (settings.isSetting(sessionID, hostKey)
                             && settings.isSetting(sessionID, portKey)) {
                         String host = settings.getString(sessionID, hostKey);
                         int port = (int) settings.getLong(sessionID, portKey);
                         Session ns = new Session(ioProcessor, new InetSocketAddress(host, port),
                                 FIXMessageData.RECOGNIZER, eventDispatcher);
                         ns.addSessionListener(new NettySessionListener());
                         if (nettySession == null) {
                             nettySession = ns;
                         }
                         nettySessions.add(ns);
                     } else {
                         break;
                     }
                 } catch (ConfigError e) {
                     throw e;
                 } catch (FieldConvertError e) {
                     throw (ConfigError) new ConfigError(e.getMessage()).initCause(e);
                 }
 
             }
 
             nettySession.start();
         }
 
         public quickfix.Session getQuickFixSession() {
             return quickfixSession;
         }
 
         public void onTimerEvent() {
             if (!nettySession.isStarted() && !nettySession.isConnected()
                     && quickfixSession.isEnabled() && isTimeForReconnect()
                     && quickfixSession.isSessionTime()) {
                 nettySession = (Session) nettySessions
                         .get((nettySessions.indexOf(nettySession) + 1) % nettySessions.size());
                 lastReconnectAttemptTime = System.currentTimeMillis();
                 nettySession.start();
                 return;
             }
             // Delegate timer event to base class to it can hand off the event
             // to the appropriate thread
             //
             // Bug #120 - There was a race condition between the timer events, the nettySession
             // start method and the session relogon. Sometimes the relogon would be attempted
             // before the nettySession was reestablished. The following check is intended to
             // keep this from happening.
             if (quickfixSession.getResponder() != null) {
                 AbstractSocketInitiator.this.onTimerEvent(quickfixSession);
             }
         }
 
         private boolean isTimeForReconnect() {
             return System.currentTimeMillis() - lastReconnectAttemptTime > reconnectInterval;
         }
 
         private class NettySessionListener implements SessionListener {
             /*
              * (non-Javadoc)
              *
              * @see net.gleamynode.netty2.SessionListener#connectionEstablished(net.gleamynode.netty2.Session)
              */
             public void connectionEstablished(Session nettySession) {
                 quickfixSession.getState().logEvent("connection established: " + nettySession);
                 try {
                     quickfixSession.setResponder(new QuickFixSessionResponder());
                     quickfixSession.next();
                 } catch (IOException e) {
                     exceptionCaught(nettySession, e);
                 }
             }
 
             /*
              * (non-Javadoc)
              *
              * @see net.gleamynode.netty2.SessionListener#connectionClosed(net.gleamynode.netty2.Session)
              */
             public void connectionClosed(Session session) {
                 quickfixSession.getState().logEvent("connection closed: " + nettySession);
                 try {
                     if (!responderDisconnected) {
                         log.debug("unsolicited disconnect");
                         quickfixSession.disconnect();
                     } else {
                         responderDisconnected = false;
                     }
                 } catch (IOException e) {
                     exceptionCaught(session, e);
                 }
             }
 
             /*
              * (non-Javadoc)
              *
              * @see net.gleamynode.netty2.SessionListener#messageSent(net.gleamynode.netty2.Session,
              *      net.gleamynode.netty2.Message)
              */
             public void messageSent(Session session, Message message) {
                 //quickfixSession.getLog().onOutgoing(message.toString());
             }
 
             /*
              * (non-Javadoc)
              *
              * @see net.gleamynode.netty2.SessionListener#messageReceived(net.gleamynode.netty2.Session,
              *      net.gleamynode.netty2.Message)
              */
             public void messageReceived(Session session, Message message) {
                 try {
                     FIXMessageData fixMessageData = (FIXMessageData) message;
                     fixMessageData.setSession(quickfixSession);
                     onMessage(message);
                 } catch (Exception e) {
                     LogUtil.logThrowable(quickfixSession.getLog(), "error receiving message", e);
                 }
             }
 
             /*
              * (non-Javadoc)
              *
              * @see net.gleamynode.netty2.SessionListener#sessionIdle(net.gleamynode.netty2.Session)
              */
             public void sessionIdle(Session nettySession) {
             }
 
             /*
              * (non-Javadoc)
              *
              * @see quickfix.netty.AbstractSessionListener#exceptionCaught(net.gleamynode.netty2.Session,
              *      java.lang.Throwable)
              */
             public void exceptionCaught(Session session, Throwable cause) {
                 LogUtil.logThrowable(quickfixSession.getLog(), "error in initiator", cause);
             }
         }
 
         private class QuickFixSessionResponder implements Responder {
             /*
              * (non-Javadoc)
              *
              * @see quickfix.Responder#send(java.lang.String)
              */
             public boolean send(String data) {
                 // NOTE: The Netty write operation is asynchronous. This
                 // means that a true result from write() may not result
                 // in an actual write to the socket channel. Hopefully,
                 // this is a close enough approximation to the synchronous
                 // C++ networking code.
                 return nettySession.write(new FIXMessageData(data));
             }
 
             /*
              * (non-Javadoc)
              *
              * @see quickfix.Responder#disconnect()
              */
             public void disconnect() {
                 log.debug("responder: disconnect");
                 responderDisconnected = true;
                 nettySession.close();
 
                 // Reset session/socket to primary socket for next attempt
                 nettySession = (Session) nettySessions.get(0);
             }
 
             public String getRemoteIPAddress() {
                 return nettySession.getSocketAddressString();
             }
         }
     }
 
     public boolean isLoggedOn() {
         synchronized (quickfixSessions) {
             Iterator sessionItr = quickfixSessions.iterator();
             while (sessionItr.hasNext()) {
                 quickfix.Session s = (quickfix.Session) sessionItr.next();
                 if (s.isLoggedOn()) {
                     return true;
                 }
             }
             return false;
         }
     }
 
     public boolean isLoggedOn(SessionID sessionID) {
         quickfix.Session session = quickfix.Session.lookupSession(sessionID);
         if (session != null) {
             return session.isLoggedOn();
         }
         return false;
     }
 
     public ArrayList getSessions() {
         synchronized (quickfixSessions) {
             return new ArrayList(quickfixSessions);
         }
     }
 }
