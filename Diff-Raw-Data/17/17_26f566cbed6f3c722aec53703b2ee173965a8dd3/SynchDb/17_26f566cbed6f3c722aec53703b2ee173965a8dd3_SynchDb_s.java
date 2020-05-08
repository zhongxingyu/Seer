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
 package org.bedework.synch.db;
 
 import org.bedework.synch.exception.SynchException;
 
 import edu.rpi.cmt.db.hibernate.HibException;
 import edu.rpi.cmt.db.hibernate.HibSession;
 import edu.rpi.cmt.db.hibernate.HibSessionFactory;
 import edu.rpi.cmt.db.hibernate.HibSessionImpl;
 
 import org.apache.log4j.Logger;
 
 import java.io.Serializable;
 import java.sql.Timestamp;
 import java.util.List;
 
 /** This class manages the Exchange synch database.
  *
  * @author Mike Douglass
  */
 public class SynchDb implements Serializable {
   private transient Logger log;
 
   private final boolean debug;
 
   /** */
   protected boolean open;
 
   /** When we were created for debugging */
   protected Timestamp objTimestamp;
 
   /** Current hibernate session - exists only across one user interaction
    */
   protected HibSession sess;
 
   /**
    *
    */
   public SynchDb() {
     debug = getLogger().isDebugEnabled();
   }
 
   /**
    * @return true if we had to open it. False if already open
    * @throws SynchException
    */
   public boolean open() throws SynchException {
     if (isOpen()) {
       return false;
     }
 
     openSession();
     open = true;
     return true;
   }
 
   /**
    * @return true for open
    */
   public boolean isOpen() {
     return open;
   }
 
   /**
    * @throws SynchException
    */
   public void close() throws SynchException {
     try {
       endTransaction();
     } catch (SynchException wde) {
       try {
         rollbackTransaction();
       } catch (SynchException wde1) {}
       throw wde;
     } finally {
       try {
         closeSession();
       } catch (SynchException wde1) {}
       open = false;
     }
   }
 
   /* ====================================================================
    *                   Config Object methods
    * ==================================================================== */
 
   /**
    * @return SynchConfig
    * @throws SynchException
    */
   public SynchConfig getConfig() throws SynchException {
     try {
       StringBuilder sb = new StringBuilder();
 
       sb.append("from ");
       sb.append(SynchConfig.class.getName());
 
       sess.createQuery(sb.toString());
 
       @SuppressWarnings("unchecked")
       List<SynchConfig> scs = sess.getList();
 
       if (scs.size() == 0) {
         return null;
       }
 
       if (scs.size() == 1) {
         return scs.get(0);
       }
 
       throw new SynchException("Expect only 1 synch config element");
     } catch (HibException he) {
       throw new SynchException(he);
     }
   }
 
   /** Add the synch config.
    *
    * @param sc
    * @throws SynchException
    */
   public void add(final SynchConfig sc) throws SynchException {
     try {
       sess.save(sc);
     } catch (HibException he) {
       throw new SynchException(he);
     }
   }
 
   /** Update the persisted state of the config.
    *
    * @param sc
    * @throws SynchException
    */
   public void update(final SynchConfig sc) throws SynchException {
     try {
       sess.update(sc);
     } catch (HibException he) {
       throw new SynchException(he);
     }
   }
 
   /* ====================================================================
    *                   Subscription Object methods
    * ==================================================================== */
 
   /**
    * @return list of subscriptions
    * @throws SynchException
    */
   @SuppressWarnings("unchecked")
   public List<Subscription> getAll() throws SynchException {
     StringBuilder sb = new StringBuilder();
 
     sb.append("from ");
     sb.append(Subscription.class.getName());
 
     try {
       sess.createQuery(sb.toString());
 
       return sess.getList();
     } catch (HibException he) {
       throw new SynchException(he);
     }
   }
 
   /** The synch engine generates a unique subscription id
    * for each subscription. This is used as a key for each subscription.
    *
    * @param id - unique id
    * @return a matching subscription or null
    * @throws SynchException
    */
   public Subscription get(final String id) throws SynchException {
     try {
       StringBuilder sb = new StringBuilder();
 
       sb.append("from ");
       sb.append(Subscription.class.getName());
       sb.append(" sub where sub.subscriptionId=:subid");
 
       sess.createQuery(sb.toString());
       sess.setString("subid", id);
 
       return (Subscription)sess.getUnique();
     } catch (HibException he) {
       throw new SynchException(he);
     }
   }
 
   /** Find any subscription that matches this one. There can only be one with
    * the same endpoints
    *
    * @param sub
    * @return matching subscriptions
    * @throws SynchException
    */
   public Subscription find(final Subscription sub) throws SynchException {
     try {
       StringBuilder sb = new StringBuilder();
 
       sb.append("from ");
       sb.append(Subscription.class.getName());
       sb.append(" sub where sub.endAConnectorInfo.connectorId=:aconnid");
       sb.append(" and sub.endAConnectorInfo.synchProperties=:aconnprops");
       sb.append(" and sub.endBConnectorInfo.connectorId=:bconnid");
       sb.append(" and sub.endBConnectorInfo.synchProperties=:bconnprops");
       sb.append(" and sub.direction=:dir");
       sb.append(" and sub.master=:mstr");
 
       sess.createQuery(sb.toString());
       sess.setString("aconnid",
                      sub.getEndAConnectorInfo().getConnectorId());
       sess.setString("aconnprops",
                      sub.getEndAConnectorInfo().getSynchProperties());
       sess.setString("bconnid",
                      sub.getEndBConnectorInfo().getConnectorId());
       sess.setString("bconnprops",
                      sub.getEndBConnectorInfo().getSynchProperties());
       sess.setString("dir",
                      sub.getDirection().name());
       sess.setString("mstr",
                      sub.getMaster().name());
 
       return (Subscription)sess.getUnique();
     } catch (HibException he) {
       throw new SynchException(he);
     }
   }
 
   /** Add the subscription.
    *
    * @param sub
    * @throws SynchException
    */
   public void add(final Subscription sub) throws SynchException {
     try {
       sess.save(sub);
     } catch (HibException he) {
       throw new SynchException(he);
     }
   }
 
   /** Update the persisted state of the subscription.
    *
    * @param sub
    * @throws SynchException
    */
   public void update(final Subscription sub) throws SynchException {
     try {
       sess.update(sub);
     } catch (HibException he) {
       throw new SynchException(he);
     }
   }
 
   /** Delete the subscription.
    *
    * @param sub
    * @throws SynchException
    */
   public void delete(final Subscription sub) throws SynchException {
     try {
       sess.delete(sub);
     } catch (HibException he) {
       throw new SynchException(he);
     }
   }
 
   /* ====================================================================
    *                   Session methods
    * ==================================================================== */
 
   protected void checkOpen() throws SynchException {
     if (!isOpen()) {
       throw new SynchException("Session call when closed");
     }
   }
 
   protected synchronized void openSession() throws SynchException {
     if (isOpen()) {
       throw new SynchException("Already open");
     }
 
     open = true;
 
     if (sess != null) {
       warn("Session is not null. Will close");
       try {
         close();
       } finally {
       }
     }
 
     if (sess == null) {
       if (debug) {
         trace("New hibernate session for " + objTimestamp);
       }
       sess = new HibSessionImpl();
       try {
         sess.init(HibSessionFactory.getSessionFactory(), getLogger());
       } catch (HibException he) {
         throw new SynchException(he);
       }
       trace("Open session for " + objTimestamp);
     }
 
     beginTransaction();
   }
 
   protected synchronized void closeSession() throws SynchException {
     if (!isOpen()) {
       if (debug) {
         trace("Close for " + objTimestamp + " closed session");
       }
       return;
     }
 
     if (debug) {
       trace("Close for " + objTimestamp);
     }
 
     try {
       if (sess != null) {
         if (sess.rolledback()) {
           sess = null;
           return;
         }
 
         if (sess.transactionStarted()) {
           sess.rollback();
         }
 //        sess.disconnect();
         sess.close();
         sess = null;
       }
     } catch (Throwable t) {
       try {
         sess.close();
       } catch (Throwable t1) {}
       sess = null; // Discard on error
     } finally {
       open = false;
     }
   }
 
   protected void beginTransaction() throws SynchException {
     checkOpen();
 
     if (debug) {
       trace("Begin transaction for " + objTimestamp);
     }
     try {
       sess.beginTransaction();
     } catch (HibException he) {
       throw new SynchException(he);
     }
   }
 
   protected void endTransaction() throws SynchException {
     checkOpen();
 
     if (debug) {
       trace("End transaction for " + objTimestamp);
     }
 
     try {
       if (!sess.rolledback()) {
         sess.commit();
       }
     } catch (HibException he) {
       throw new SynchException(he);
     }
   }
 
   protected void rollbackTransaction() throws SynchException {
     try {
       checkOpen();
       sess.rollback();
     } catch (HibException he) {
       throw new SynchException(he);
     } finally {
     }
   }
 
   /**
    * @return Logger
    */
   protected Logger getLogger() {
     if (log == null) {
       log = Logger.getLogger(this.getClass());
     }
 
     return log;
   }
 
   /**
    * @param t
    */
   protected void error(final Throwable t) {
     getLogger().error(this, t);
   }
 
   /**
    * @param msg
    */
   protected void warn(final String msg) {
     getLogger().warn(msg);
   }
 
   /**
    * @param msg
    */
   protected void trace(final String msg) {
     getLogger().debug(msg);
   }
 
   /* ====================================================================
    *                   private methods
    * ==================================================================== */
 
 }
