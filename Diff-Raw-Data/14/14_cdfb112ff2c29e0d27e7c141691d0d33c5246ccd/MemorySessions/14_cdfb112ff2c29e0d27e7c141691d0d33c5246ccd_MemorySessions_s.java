 /*
 * $Id: MemorySessions.java,v 1.6 2007/08/29 15:21:42 vtschopp Exp $
  *
  * Copyright (c) Members of the EGEE Collaboration. 2004.
  * See http://eu-egee.org/partners/ for details on the copyright holders.
  * For license conditions see the license file or http://eu-egee.org/license.html 
  */
 package org.glite.slcs.session.impl;
 
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.glite.slcs.config.SLCSServerConfiguration;
 import org.glite.slcs.session.SLCSSession;
 import org.glite.slcs.session.SLCSSessions;
 import org.glite.slcs.util.Utils;
 
 /**
  * MemorySessions is the memory implementation of the SLCSSessions. Uses a
  * cleaning task to delete expired sessions.
  * 
  * @author Valery Tschopp &lt;tschopp@switch.ch&gt;
 * @version $Revision: 1.6 $
  */
 public class MemorySessions implements SLCSSessions {
 
     /** logging */
     public static Log LOG = LogFactory.getLog(MemorySessions.class);
 
     /** Date format for toString */
     static private SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(
             "yyyy.MM.dd HH:mm:ss");
 
     /** hashtable to store the (token,SessionEntry) pair */
     private Hashtable sessions_ = null;
 
     private Object sessionsMutex_ = new Object();
 
     /** random bytes generator */
     private SecureRandom random_ = null;
 
     /** memory sessions cleaning thread */
     private MemorySessionsCleaner memorySessionsCleaner_ = null;
 
     /** default TTL: 300 seconds */
     private long sessionTTL_ = 300;
 
     /**
      * Constructor accessed only by factory
      */
     public MemorySessions() {
         super();
         sessions_ = new Hashtable();
         try {
             random_ = SecureRandom.getInstance("SHA1PRNG");
         } catch (NoSuchAlgorithmException e) {
             LOG.error(e);
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.glite.slcs.session.SLCSSessions#init()
      */
     public void init(SLCSServerConfiguration config) {
         // read SessionTTL from config
         if (config.contains(SLCSServerConfiguration.COMPONENTSCONFIGURATION_PREFIX
                 + ".SLCSSessions.SessionTTL")) {
             int sessionTTL = config.getInt(SLCSServerConfiguration.COMPONENTSCONFIGURATION_PREFIX
                     + ".SLCSSessions.SessionTTL");
             this.sessionTTL_ = sessionTTL;
         }
         LOG.info("SLCSSessions.SessionTTL=" + sessionTTL_);
 
         // read CleaningInterval (in seconds) for the memory cleaner thread
         int cleaningInterval = 60; // default 1 minute
         if (config.contains(SLCSServerConfiguration.COMPONENTSCONFIGURATION_PREFIX
                 + ".SLCSSessions.CleaningInterval")) {
             cleaningInterval = config.getInt(SLCSServerConfiguration.COMPONENTSCONFIGURATION_PREFIX
                     + ".SLCSSessions.CleaningInterval");
         }
         LOG.info("SLCSSessions.CleaningInterval=" + cleaningInterval);
         // and create/start the cleaning thread
         memorySessionsCleaner_ = new MemorySessionsCleaner(cleaningInterval);
         memorySessionsCleaner_.start();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.glite.slcs.SLCSServerComponent#shutdown()
      */
     public void shutdown() {
         // stop the cleaning thread
         if (memorySessionsCleaner_ != null) {
             LOG.info("shutdown the MemorySessionsCleaner");
             memorySessionsCleaner_.shutdown();
             memorySessionsCleaner_ = null;
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.glite.slcs.session.SLCSSessions#createSession(java.lang.String)
      */
     public SLCSSession createSession(String dn) {
         // create a random token
         String token = createRandomToken();
         // store the pair
         SLCSSession session = new SessionEntry(token, dn, sessionTTL_);
         if (LOG.isDebugEnabled()) {
             LOG.debug("add: " + session);
         }
         synchronized (sessionsMutex_) {
             sessions_.put(token, session);
         }
         // return the session
         return session;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.glite.slcs.session.SLCSSessions#deleteToken(java.lang.String)
      */
     public void removeSession(String token) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("remove: " + token);
         }
         synchronized (sessionsMutex_) {
             sessions_.remove(token);
         }
     }
 
     /**
      * Returns the SessionEntry for this token.
      * 
      * @param token
      *            The session token (id)
      * @return The session or <code>null</code> if the session doesn't exists.
      */
     private SessionEntry getSessionEntry(String token) {
         if (sessions_.containsKey(token)) {
             SessionEntry session = (SessionEntry) sessions_.get(token);
             return session;
         }
         return null;
     }
 
     /**
      * Returns the session if and only if the token-dn pair exists in the
      * sessions.
      * 
      * @param token
      *            The session token.
      * @param dn
      *            The subject DN.
      * @return The session or <code>null</code> if the session doesn't exists.
      */
     public SLCSSession getSession(String token, String dn) {
         SessionEntry session = getSessionEntry(token);
         if (session != null) {
             String storedDN = session.getDN();
             if (storedDN.equals(dn)) {
                 return session;
             }
         }
        return session;
     }
 
     /**
      * @return A 64 chars random token
      */
     private String createRandomToken() {
         byte[] randomBytes = null;
         randomBytes = new byte[32];
         random_.nextBytes(randomBytes);
         String token = Utils.toHexString(randomBytes);
         return token;
     }
 
     /**
      * SessionEntry implements the SLCSSession interface.
      * 
      * @author Valery Tschopp <tschopp@switch.ch>
     * @version $Revision: 1.6 $
      */
     public class SessionEntry implements SLCSSession {
 
         /** Cerificate DN */
         private String dn_ = null;
 
         /** Authorisation token */
         private String token_ = null;
 
         /** Creation time (milis) */
         private long time_ = 0;
 
         /** Attributes list */
         private List attributes_ = null;
 
         /** TTL in millis */
         private long ttl_ = 0;
 
         /**
          * Constructor.
          * 
          * @param token
          *            The session token.
          * @param dn
          *            The session DN.
          * @param ttl
          *            The time to live in seconds
          */
         public SessionEntry(String token, String dn, long ttl) {
             this.dn_ = dn;
             this.token_ = token;
             this.time_ = System.currentTimeMillis();
             // convert seconds to millis
             this.ttl_ = ttl * 1000;
             // attributes store
             this.attributes_ = new ArrayList();
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see org.glite.slcs.session.SLCSSession#getToken()
          */
         public String getToken() {
             return this.token_;
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see org.glite.slcs.session.SLCSSession#getDN()
          */
         public String getDN() {
             return this.dn_;
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see org.glite.slcs.session.SLCSSession#setAttributes(java.util.List)
          */
         public void setAttributes(List attributes) {
             attributes_ = attributes;
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see org.glite.slcs.session.SLCSSession#getAttributes()
          */
         public List getAttributes() {
             return attributes_;
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see org.glite.slcs.session.SLCSSession#isValid()
          */
         public boolean isValid() {
             long currentTime = System.currentTimeMillis();
             long deathTime = time_ + ttl_;
             boolean valid = currentTime <= deathTime;
             return valid;
         }
 
         /**
          * @return <code>true</code> iff the session is expired.
          */
         public boolean isExpired() {
             return !isValid();
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see java.lang.Object#toString()
          */
         public String toString() {
             StringBuffer sb = new StringBuffer();
             sb.append("Session[");
             sb.append(token_).append(':');
             sb.append(dn_).append(':');
             if (!attributes_.isEmpty()) {
                 sb.append(attributes_).append(':');
             }
             long deathTime = time_ + ttl_;
             Date date = new Date(deathTime);
             sb.append(DATE_FORMATTER.format(date));
             sb.append(']');
             return sb.toString();
         }
     }
 
     /**
      * MemorySessionsCleaner is a cleaning task which removes the expired SLCS
      * sessions every given time interval.
      * 
      * @author Valery Tschopp <tschopp@switch.ch>
     * @version $Revision: 1.6 $
      */
     private class MemorySessionsCleaner extends Timer {
 
         private long cleaningInterval_ = 60 * 1000; // one minute
 
         /**
          * Creates a {@link MemorySessionsCleaner} object with default 60
          * seconds interval.
          */
         public MemorySessionsCleaner() {
             this(60);
         }
 
         /**
          * Creates a {@link MemorySessionsCleaner} object with the given
          * interval in seconds.
          * 
          * @param seconds
          *            The interval between to run.
          */
         public MemorySessionsCleaner(int seconds) {
             super(true); // daemon
             this.cleaningInterval_ = seconds * 1000;
         }
 
         public void start() {
             LOG.info("schedule the MemorySessionsCleaningTask ("
                     + cleaningInterval_ + " ms)");
             scheduleAtFixedRate(new MemorySessionsCleaningTask(), 0,
                     cleaningInterval_);
         }
 
         public void shutdown() {
             LOG.info("cancel the MemorySessionsCleaningTask");
             this.cancel();
         }
 
         /**
          * MemorySessionsCleaningTask scheduled by the MemorySessionsCleaner
          * every cleaningInterval to remove the expired SLCS sessions.
          */
         class MemorySessionsCleaningTask extends TimerTask {
 
             /**
              * Removes expired SLCSSessions.
              */
             public void run() {
                 synchronized (sessionsMutex_) {
                     if (!sessions_.isEmpty()) {
                         if (LOG.isDebugEnabled()) {
                             LOG.debug("checking sessions: " + sessions_.size());
                         }
                         Enumeration sessionEntries = sessions_.elements();
                         while (sessionEntries.hasMoreElements()) {
                             SessionEntry session = (SessionEntry) sessionEntries.nextElement();
                             if (session.isExpired()) {
                                 LOG.info("Removing expired session: " + session);
                                 sessions_.remove(session.getToken());
                             }
                         }
                     }
                 }
             }
         }
 
     }
 }
