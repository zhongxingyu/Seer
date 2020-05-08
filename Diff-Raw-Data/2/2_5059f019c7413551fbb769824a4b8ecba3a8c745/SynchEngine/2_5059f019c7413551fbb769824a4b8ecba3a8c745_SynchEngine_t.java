 /* ********************************************************************
     Licensed to Jasig under one or more contributor license
     agreements. See the NOTICE file distributed with this work
     for additional information regarding copyright ownership.
     Jasig licenses this file to you under the Apache License,
     Version 2.0 (the "License"); you may not use this file
     except in compliance with the License. You may obtain a
     copy of the License at:
 
     http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing,
     software distributed under the License is distributed on
     an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     KIND, either express or implied. See the License for the
     specific language governing permissions and limitations
     under the License.
 */
 package org.bedework.synch;
 
 import org.bedework.http.client.dav.DavClient;
 import org.bedework.synch.cnctrs.Connector;
 import org.bedework.synch.cnctrs.Connector.NotificationBatch;
 import org.bedework.synch.cnctrs.ConnectorInstance;
 import org.bedework.synch.db.ConnectorConfig;
 import org.bedework.synch.db.Subscription;
 import org.bedework.synch.db.SynchConfig;
 import org.bedework.synch.db.SynchDb;
 import org.bedework.synch.exception.SynchException;
 import org.bedework.synch.wsmessages.SynchEndType;
 
 import edu.rpi.cmt.calendar.XcalUtil.TzGetter;
 import edu.rpi.cmt.security.PwEncryptionIntf;
 import edu.rpi.cmt.timezones.Timezones;
 import edu.rpi.cmt.timezones.TimezonesImpl;
 import edu.rpi.sss.util.Util;
 
 import net.fortuna.ical4j.model.TimeZone;
 
 import org.apache.log4j.Logger;
 import org.oasis_open.docs.ws_calendar.ns.soap.StatusType;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 /** Synch processor.
  * <p>The synch processor manages subscriptions made by a subscriber to a target.
  * Such a subscription might be one way or two way.
  *
  * <p>There are two ends to a subscription handled by connectors. The connectors
  * implement a standard interface which provides sufficient information for the
  * synch process.
  *
  * <p>Synchronization is triggered either when a change takes place - through
  * some sort of push-notification or periodically.
  *
  * <p>For example, we might have a one way subscription from bedework to
  * exchange. Exchange will post notifications to the synch engine which will
  * then resynch the modified entity.
  *
  * <p>Alternatively we might have a subscription to a file which we refresh each
  * day at 4am.
  *
  * <p>A subscription may be in the following states:<ul>
  * <li>dormant - that is there is no current activity, for
  * example a file subscription with a periodic update,</li>
  * <li>active - there is some active connection associated with it, for example,
  * an Exchange push subscription waiting for a notification</li>
  * <li>processing - for example, an Exchange push subscription which is
  * processing a notification</li>
  * <li>unsubscribing - the user has asked to unsubscribe but there is some
  * activity we are waiting for<li>
  * </ul>
  *
  * <p>Interactions with the calendars is carried out through an interface which
  * assumes the CalWs-SOAP protocol. Messages and responses are of that form
  * though the actual implementation may not use the protocol if the target does
  * not support it. For example we convert CalWs-SOAP interactions into ExchangeWS.
  *
  * --------------------- ignore below ----------------------------------------
  *
  * <p>This process manages the setting up of push-subscriptions with the exchange
  * service and provides support for the resulting call-back from Exchange. There
  * will be one instance of this object to handle the tables we create and
  * manipulate.
  *
  * <p>There will be multiple threads calling the various methods. Push
  * subscriptions work more or less as follows:<ul>
  * <li>Subscribe to exchange giving a url to call back to. Set a refresh period</li>
  * <li>Exchange calls back even if there is no change - effectively polling us</li>
  * <li>If we don't respond Exchange doubles the wait period and tries again</li>
  * <li>Repeats that a few times then gives up</li>
  * <li>If we do respond will call again at the specified rate</li>
  * <li>No unsubscribe - wait for a ping then respond with unsubscribe</li>
  * </ul>
  *
  * <p>At startup we ask for the back end system to tell us what the subscription
  * are and we spend some time setting those up.
  *
  * <p>We also provide a way for the system to tell us about new (un)subscribe
  * requests. While starting up we need to queue these as they may be unsubscribes
  * for a subscribe in the startup list.
  *
  * <p>Shutdown ought to wait for the remote systems to ping us for every outstanding
  * subscription. That ought to be fairly quick.
  *
  * @author Mike Douglass
  */
 public class SynchEngine extends TzGetter {
   protected transient Logger log;
 
   private final boolean debug;
 
   private static String appname = "Synch";
 
   private transient PwEncryptionIntf pwEncrypt;
 
   /* Map of currently active notification subscriptions. These are subscriptions
    * for which we get change messages from the remote system(s).
    */
   private final Map<String, Subscription> activeSubs =
       new HashMap<String, Subscription>();
 
   private boolean starting;
 
   private boolean running;
 
   private boolean stopping;
 
   private Configurator config;
 
   private static Object getSyncherLock = new Object();
 
   private static SynchEngine syncher;
 
   private Timezones timezones;
 
   static TzGetter tzgetter;
 
   private SynchlingPool synchlingPool;
 
   private SynchTimer synchTimer;
 
   private BlockingQueue<Notification> notificationInQueue;
 
   /* Where we keep subscriptions that come in while we are starting */
   private List<Subscription> subsList;
 
   private SynchDb db;
 
   private Map<String, Connector> connectorMap = new HashMap<String, Connector>();
 
   /* Some counts */
 
   private StatLong notificationsCt = new StatLong("notifications");
 
   private StatLong notificationsAddWt = new StatLong("notifications add wait");
 
   /** This process handles startup notifications and (un)subscriptions.
    *
    */
   private class NotificationInThread extends Thread {
     long lastTrace;
 
     /**
      */
     public NotificationInThread() {
       super("NotifyIn");
     }
 
     @Override
     public void run() {
       while (true) {
         if (debug) {
           trace("About to wait for notification");
         }
 
         try {
           Notification note = notificationInQueue.take();
           if (note == null) {
             continue;
           }
 
           if (debug) {
             trace("Received notification");
           }
 
           if ((note.getSub() != null) && note.getSub().getDeleted()) {
             // Drop it
 
             if (debug) {
               trace("Dropping deleted notification");
             }
 
             continue;
           }
 
           notificationsCt.inc();
           Synchling sl = null;
 
           try {
             /* Get a synchling from the pool */
             while (true) {
               if (stopping) {
                 return;
               }
 
               sl = synchlingPool.getNoException();
               if (sl != null) {
                 break;
               }
             }
 
             /* The synchling needs to be running it's own thread. */
             StatusType st = handleNotification(sl, note);
 
             if (st == StatusType.WARNING) {
               /* Back on the queue - these need to be flagged so we don't get an
                * endless loop - perhaps we need a delay queue
                */
 
               notificationInQueue.put(note);
             }
           } finally {
             synchlingPool.add(sl);
           }
 
           /* If this is a poll kind then we should add it to a poll queue
            */
           // XXX Add it to poll queue
         } catch (InterruptedException ie) {
           warn("Notification handler shutting down");
           break;
         } catch (Throwable t) {
           if (debug) {
             error(t);
           } else {
             // Try not to flood the log with error traces
             long now = System.currentTimeMillis();
             if ((now - lastTrace) > (30 * 1000)) {
               error(t);
               lastTrace = now;
             } else {
               error(t.getMessage());
             }
           }
         }
       }
     }
   }
 
   private static NotificationInThread notifyInHandler;
 
   /** Constructor
    *
    * @param exintf
    */
   private SynchEngine() throws SynchException {
     debug = getLogger().isDebugEnabled();
 
     System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump",
                        String.valueOf(debug));
   }
 
   /**
    * @return the syncher
    * @throws SynchException
    */
   public static SynchEngine getSyncher() throws SynchException {
     if (syncher != null) {
       return syncher;
     }
 
     synchronized (getSyncherLock) {
       if (syncher != null) {
         return syncher;
       }
       syncher = new SynchEngine();
       return syncher;
     }
   }
 
   /** Set before calling getSyncher
    *
    * @param val
    */
   public static void setAppname(final String val) {
     appname = val;
   }
 
   /**
    * @return appname
    */
   public static String getAppname() {
     return appname;
   }
 
   /** Get a timezone object given the id. This will return transient objects
    * registered in the timezone directory
    *
    * @param id
    * @return TimeZone with id or null
    * @throws Throwable
    */
    @Override
   public TimeZone getTz(final String id) throws Throwable {
      return getSyncher().timezones.getTimeZone(id);
    }
 
    /**
     * @return a getter for timezones
     */
    public static TzGetter getTzGetter() {
      return tzgetter;
    }
 
   /** Start synch process.
    *
    * @throws SynchException
    */
   public void start() throws SynchException {
     try {
       if (starting || running) {
         warn("Start called when already starting or running");
         return;
       }
 
       synchronized (this) {
         subsList = null;
 
         starting = true;
       }
 
       db = new SynchDb();
       config = new Configurator(db);
 
       timezones = new TimezonesImpl();
       timezones.init(config.getSynchConfig().getTimezonesURI());
 
       tzgetter = this;
 
      //DavClient.setDefaultMaxPerHost(20);
       DavClient.setDefaultMaxPerRoute(20);
 
       synchlingPool = new SynchlingPool();
       synchlingPool.start(this,
                           config.getSynchConfig().getSynchlingPoolSize(),
                           config.getSynchConfig().getSynchlingPoolTimeout());
 
       notificationInQueue = new ArrayBlockingQueue<Notification>(100);
 
       info("**************************************************");
       info("Starting synch");
       info("      callback URI: " + config.getSynchConfig().getCallbackURI());
       info("**************************************************");
 
       if (config.getSynchConfig().getKeystore() != null) {
         System.setProperty("javax.net.ssl.trustStore", config.getSynchConfig().getKeystore());
         System.setProperty("javax.net.ssl.trustStorePassword", "bedework");
       }
 
       Set<ConnectorConfig> connectors = config.getSynchConfig().getConnectors();
       String callbackUriBase = config.getSynchConfig().getCallbackURI();
 
       /* Register the connectors and start them */
       for (ConnectorConfig conf: connectors) {
         String cnctrId = conf.getName();
         info("Register and start connector " + cnctrId);
 
         registerConnector(cnctrId, conf);
 
         Connector conn = getConnector(cnctrId);
 
         conn.start(cnctrId,
                    conf,
                    callbackUriBase + cnctrId + "/",
                    this);
 
         while (!conn.isStarted()) {
           /* Wait for it to start */
           synchronized (this) {
             this.wait(250);
           }
 
           if (conn.isFailed()) {
             error("Connector " + cnctrId + " failed to start");
             break;
           }
         }
       }
 
       synchTimer = new SynchTimer(this);
 
       /* Get the list of subscriptions from our database and process them.
        * While starting, new subscribe requests get added to the list.
        */
 
       notifyInHandler = new NotificationInThread();
       notifyInHandler.start();
 
       try {
         db.open();
         List<Subscription> startList = db.getAll();
         db.close();
 
         startup:
         while (starting) {
           if (debug) {
             trace("startList has " + startList.size() + " subscriptions");
           }
 
           for (Subscription sub: startList) {
             setConnectors(sub);
 
             reschedule(sub);
           }
 
           synchronized (this) {
             if (subsList == null) {
               // Nothing came in as we started
               starting = false;
               if (stopping) {
                 break startup;
               }
               running = true;
               break;
             }
 
             startList = subsList;
             subsList = null;
           }
         }
       } finally {
         if ((db != null) && db.isOpen()) {
           db.close();
         }
       }
 
       info("**************************************************");
       info("Synch started");
       info("**************************************************");
     } catch (SynchException se) {
       error(se);
       starting = false;
       running = false;
       throw se;
     } catch (Throwable t) {
       error(t);
       starting = false;
       running = false;
       throw new SynchException(t);
     }
   }
 
   /** Reschedule a subscription for updates.
    *
    * @param sub
    * @throws SynchException
    */
   public void reschedule(final Subscription sub) throws SynchException {
     if (debug) {
       trace("reschedule subscription " + sub);
     }
 
     if (sub.polling()) {
       synchTimer.schedule(sub, sub.nextRefresh());
       return;
     }
 
     // XXX start up the add to active subs
 
     activeSubs.put(sub.getSubscriptionId(), sub);
   }
 
   /**
    * @return true if we're running
    */
   public boolean getRunning() {
     return running;
   }
 
   /**
    * @return stats for synch service bean
    */
   public List<Stat> getStats() {
     List<Stat> stats = new ArrayList<Stat>();
 
     stats.addAll(synchlingPool.getStats());
     stats.addAll(synchTimer.getStats());
     stats.add(notificationsCt);
     stats.add(notificationsAddWt);
 
     return stats;
   }
 
   /** Stop synch process.
    *
    */
   public void stop() {
     if (stopping) {
       return;
     }
 
     stopping = true;
 
     /* Call stop on each connector
      */
     for (Connector conn: getConnectors()) {
       info("Stopping connector " + conn.getId());
       try {
         conn.stop();
       } catch (Throwable t) {
         if (debug) {
           error(t);
         } else {
           error(t.getMessage());
         }
       }
     }
 
     info("Connectors stopped");
 
     if (synchlingPool != null) {
       synchlingPool.stop();
     }
 
     syncher = null;
 
     info("**************************************************");
     info("Synch shutdown complete");
     info("**************************************************");
   }
 
   /**
    * @param note
    * @throws SynchException
    */
   public void handleNotification(final Notification note) throws SynchException {
     try {
       while (true) {
         if (stopping) {
           return;
         }
 
         if (notificationInQueue.offer(note, 5, TimeUnit.SECONDS)) {
           break;
         }
       }
     } catch (InterruptedException ie) {
     }
   }
 
   /**
    * @return config object
    * @throws SynchException
    */
   public SynchConfig getConfig() throws SynchException {
     return config.getSynchConfig();
   }
 
   /**
    * @throws SynchException
    */
   public void updateConfig() throws SynchException {
     config.updateSynchConfig();
   }
 
   /**
    * @param val
    * @return decrypted string
    * @throws SynchException
    */
   public String decrypt(final String val) throws SynchException {
     try {
       return getEncrypter().decrypt(val);
     } catch (SynchException se) {
       throw se;
     } catch (Throwable t) {
       throw new SynchException(t);
     }
   }
 
   /**
    * @return en/decryptor
    * @throws SynchException
    */
   public PwEncryptionIntf getEncrypter() throws SynchException {
     if (pwEncrypt != null) {
       return pwEncrypt;
     }
 
     try {
       String pwEncryptClass = "edu.rpi.cmt.security.PwEncryptionDefault";
       //String pwEncryptClass = getSysparsHandler().get().getPwEncryptClass();
 
       pwEncrypt = (PwEncryptionIntf)Util.getObject(pwEncryptClass,
                                                    PwEncryptionIntf.class);
 
       pwEncrypt.init(config.getSynchConfig().getPrivKeys(),
                      config.getSynchConfig().getPubKeys());
 
       return pwEncrypt;
     } catch (SynchException se) {
       throw se;
     } catch (Throwable t) {
       t.printStackTrace();
       throw new SynchException(t);
     }
   }
 
   /** Gets an instance and implants it in the subscription object.
    * @param sub
    * @param end
    * @return ConnectorInstance or throws Exception
    * @throws SynchException
    */
   public ConnectorInstance getConnectorInstance(final Subscription sub,
                                                 final SynchEndType end) throws SynchException {
     ConnectorInstance cinst;
     Connector conn;
 
     if (end == SynchEndType.A) {
       cinst = sub.getEndAConnInst();
       conn = sub.getEndAConn();
     } else {
       cinst = sub.getEndBConnInst();
       conn = sub.getEndBConn();
     }
 
     if (cinst != null) {
       return cinst;
     }
 
     if (conn == null) {
       throw new SynchException("No connector for " + sub + "(" + end + ")");
     }
 
     cinst = conn.getConnectorInstance(sub, end);
     if (cinst == null) {
       throw new SynchException("No connector instance for " + sub +
                                "(" + end + ")");
     }
 
     if (end == SynchEndType.A) {
       sub.setEndAConnInst(cinst);
     } else {
       sub.setEndBConnInst(cinst);
     }
 
     return cinst;
   }
 
   /** When we start up a new subscription we implant a Connector in the object.
    *
    * @param sub
    * @throws SynchException
    */
   public void setConnectors(final Subscription sub) throws SynchException {
     String connectorId = sub.getEndAConnectorInfo().getConnectorId();
 
     Connector conn = getConnector(connectorId);
     if (conn == null) {
       throw new SynchException("No connector for " + sub + "(" +
                                SynchEndType.A + ")");
     }
 
     sub.setEndAConn(conn);
 
     connectorId = sub.getEndBConnectorInfo().getConnectorId();
 
     conn = getConnector(connectorId);
     if (conn == null) {
       throw new SynchException("No connector for " + sub + "(" +
                                SynchEndType.B + ")");
     }
 
     sub.setEndBConn(conn);
   }
 
   private Collection<Connector> getConnectors() {
     return connectorMap.values();
   }
 
   /** Return a registered connector with the given id.
    *
    * @param id
    * @return connector or null.
    */
   public Connector getConnector(final String id) {
     return connectorMap.get(id);
   }
 
   /**
    * @return registered ids.
    */
   public Set<String> getConnectorIds() {
     return connectorMap.keySet();
   }
 
   private void registerConnector(final String id,
                                  final ConnectorConfig conf) throws SynchException {
     try {
       Class cl = Class.forName(conf.getClassName());
 
       if (connectorMap.containsKey(id)) {
         throw new SynchException("Connector " + id + " already registered");
       }
 
       Connector c = (Connector)cl.newInstance();
       connectorMap.put(id, c);
     } catch (Throwable t) {
       throw new SynchException(t);
     }
   }
 
   /** Processes a batch of notifications. This must be done in a timely manner
    * as a request is usually hanging on this.
    *
    * @param notes
    * @throws SynchException
    */
   public void handleNotifications(
             final NotificationBatch<Notification> notes) throws SynchException {
     for (Notification note: notes.getNotifications()) {
       db.open();
       Synchling sl = null;
 
       try {
         if (note.getSub() != null) {
           sl = synchlingPool.get();
 
           handleNotification(sl, note);
         }
       } finally {
         db.close();
         if (sl != null) {
           synchlingPool.add(sl);
         }
       }
     }
 
     return;
   }
 
   @SuppressWarnings("unchecked")
   private StatusType handleNotification(final Synchling sl,
                                         final Notification note) throws SynchException {
     StatusType st = sl.handleNotification(note);
 
     Subscription sub = note.getSub();
     if (!sub.getMissingTarget()) {
       return st;
     }
 
     if (sub.getErrorCt() > config.getSynchConfig().getMissingTargetRetries()) {
       deleteSubscription(sub);
       info("Subscription deleted after missing target retries exhausted: " + sub);
     }
 
     return st;
   }
 
   /* ====================================================================
    *                        db methods
    * ==================================================================== */
 
   /**
    * @param id
    * @return subscription
    * @throws SynchException
    */
   public Subscription getSubscription(final String id) throws SynchException {
     boolean opened = db.open();
 
     try {
       return db.get(id);
     } finally {
       if (opened) {
         // It's a one-shot
         db.close();
       }
     }
   }
 
   /**
    * @param sub
    * @throws SynchException
    */
   public void addSubscription(final Subscription sub) throws SynchException {
     db.add(sub);
     sub.resetChanged();
   }
 
   /**
    * @param sub
    * @throws SynchException
    */
   public void updateSubscription(final Subscription sub) throws SynchException {
     boolean opened = db.open();
 
     try {
       db.update(sub);
       sub.resetChanged();
     } finally {
       if (opened) {
         // It's a one-shot
         db.close();
       }
     }
   }
 
   /**
    * @param sub
    * @throws SynchException
    */
   public void deleteSubscription(final Subscription sub) throws SynchException {
     db.delete(sub);
   }
 
   /** Find any subscription that matches this one. There can only be one with
    * the same endpoints
    *
    * @param sub
    * @return matching subscriptions
    * @throws SynchException
    */
   public Subscription find(final Subscription sub) throws SynchException {
     boolean opened = db.open();
 
     try {
       return db.find(sub);
     } finally {
       if (opened) {
         // It's a one-shot
         db.close();
       }
     }
   }
 
   /* ====================================================================
    *                        private methods
    * ==================================================================== */
 
   private Logger getLogger() {
     if (log == null) {
       log = Logger.getLogger(this.getClass());
     }
 
     return log;
   }
 
   private void trace(final String msg) {
     getLogger().debug(msg);
   }
 
   private void warn(final String msg) {
     getLogger().warn(msg);
   }
 
   private void error(final String msg) {
     getLogger().error(msg);
   }
 
   private void error(final Throwable t) {
     getLogger().error(this, t);
   }
 
   private void info(final String msg) {
     getLogger().info(msg);
   }
 }
