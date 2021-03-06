 /*
  * ################################################################
  *
  * ProActive: The Java(TM) library for Parallel, Distributed,
  *            Concurrent computing with Security and Mobility
  *
  * Copyright (C) 1997-2009 INRIA/University of 
  * 						   Nice-Sophia Antipolis/ActiveEon
  * Contact: proactive@ow2.org
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; version 3 of
  * the License.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
  * USA
  *
  * If needed, contact us to obtain a release under GPL Version 2. 
  *
  *  Initial developer(s):               The ProActive Team
  *                        http://proactive.inria.fr/team_members.htm
  *  Contributor(s): ActiveEon Team - http://www.activeeon.com
  *
  * ################################################################
  * $$ACTIVEEON_CONTRIBUTOR$$
  */
 package org.objectweb.proactive.core.body;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.lang.management.ManagementFactory;
 import java.security.AccessControlException;
 import java.security.PublicKey;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.management.InstanceNotFoundException;
 import javax.management.MBeanRegistrationException;
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 
 import org.apache.log4j.Logger;
 import org.objectweb.proactive.ActiveObjectCreationException;
 import org.objectweb.proactive.Body;
 import org.objectweb.proactive.api.PAGroup;
 import org.objectweb.proactive.core.UniqueID;
 import org.objectweb.proactive.core.body.exceptions.BodyTerminatedReplyException;
 import org.objectweb.proactive.core.body.exceptions.BodyTerminatedRequestException;
 import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;
 import org.objectweb.proactive.core.body.ft.internalmsg.Heartbeat;
 import org.objectweb.proactive.core.body.ft.protocols.FTManager;
 import org.objectweb.proactive.core.body.ft.servers.faultdetection.FaultDetector;
 import org.objectweb.proactive.core.body.future.Future;
 import org.objectweb.proactive.core.body.future.FuturePool;
 import org.objectweb.proactive.core.body.future.MethodCallResult;
 import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;
 import org.objectweb.proactive.core.body.reply.Reply;
 import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
 import org.objectweb.proactive.core.body.request.Request;
 import org.objectweb.proactive.core.body.tags.LocalMemoryTag;
 import org.objectweb.proactive.core.body.tags.MessageTagsFactory;
 import org.objectweb.proactive.core.component.representative.ItfID;
 import org.objectweb.proactive.core.component.request.Shortcut;
 import org.objectweb.proactive.core.config.PAProperties;
 import org.objectweb.proactive.core.debug.debugger.BreakpointType;
 import org.objectweb.proactive.core.debug.debugger.Debugger;
 import org.objectweb.proactive.core.gc.GCMessage;
 import org.objectweb.proactive.core.gc.GCResponse;
 import org.objectweb.proactive.core.gc.GarbageCollector;
 import org.objectweb.proactive.core.group.spmd.ProActiveSPMDGroupManager;
 import org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean;
 import org.objectweb.proactive.core.mop.MethodCall;
 import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
 import org.objectweb.proactive.core.security.DefaultProActiveSecurityManager;
 import org.objectweb.proactive.core.security.InternalBodySecurity;
 import org.objectweb.proactive.core.security.PolicyServer;
 import org.objectweb.proactive.core.security.ProActiveSecurity;
 import org.objectweb.proactive.core.security.ProActiveSecurityManager;
 import org.objectweb.proactive.core.security.Secure;
 import org.objectweb.proactive.core.security.SecurityContext;
 import org.objectweb.proactive.core.security.TypedCertificate;
 import org.objectweb.proactive.core.security.SecurityConstants.EntityType;
 import org.objectweb.proactive.core.security.crypto.KeyExchangeException;
 import org.objectweb.proactive.core.security.crypto.SessionException;
 import org.objectweb.proactive.core.security.exceptions.CommunicationForbiddenException;
 import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
 import org.objectweb.proactive.core.security.exceptions.RuntimeSecurityException;
 import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
 import org.objectweb.proactive.core.security.securityentity.Entities;
 import org.objectweb.proactive.core.security.securityentity.Entity;
 import org.objectweb.proactive.core.util.ThreadStore;
 import org.objectweb.proactive.core.util.log.Loggers;
 import org.objectweb.proactive.core.util.log.ProActiveLogger;
 import org.objectweb.proactive.core.util.profiling.Profiling;
 import org.objectweb.proactive.core.util.profiling.TimerProvidable;
 
 
 /**
  * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
  * <p>
  * This class gives a common implementation of the Body interface. It provides all the non specific
  * behavior allowing sub-class to write the detail implementation.
  * </p>
  * <p>
  * Each body is identify by an unique identifier.
  * </p>
  * <p>
  * All active bodies that get created in one JVM register themselves into a table that allows to
  * tack them done. The registering and deregistering is done by the AbstractBody and the table is
  * managed here as well using some static methods.
  * </p>
  * <p>
  * In order to let somebody customize the body of an active object without subclassing it,
  * AbstractBody delegates lot of tasks to satellite objects that implements a given interface.
  * Abstract protected methods instantiate those objects allowing subclasses to create them as they
  * want (using customizable factories or instance).
  * </p>
  * 
  * @author The ProActive Team
  * @version 1.0, 2001/10/23
  * @since ProActive 0.9
  * @see Body
  * @see UniqueID
  * 
  */
 public abstract class AbstractBody extends AbstractUniversalBody implements Body, Serializable {
     //
     // -- STATIC MEMBERS -----------------------------------------------
     //
     private static Logger logger = ProActiveLogger.getLogger(Loggers.BODY);
 
     //
     // -- PROTECTED MEMBERS -----------------------------------------------
     //
     protected ThreadStore threadStore;
 
     // the current implementation of the local view of this body
     protected LocalBodyStrategy localBodyStrategy;
 
     // SECURITY
     protected ProActiveSecurityManager securityManager;
     protected boolean isSecurityOn = false;
     protected transient InternalBodySecurity internalBodySecurity;
     protected HashSet<Long> openedSessions;
     protected boolean isInterfaceSecureImplemented = false;
 
     // SPMD GROUP
     protected ProActiveSPMDGroupManager spmdManager;
 
     // FAULT TOLERANCE
     protected FTManager ftmanager;
 
     // FORGET ON SEND
     protected boolean isSterileBody;
     protected UniqueID parentUID;
     protected transient SendingQueue sendingQueue; // should not be migrated
 
     // TIMING
     /** A container for timers not migratable for the moment */
     protected transient TimerProvidable timersContainer;
 
     // JMX
     /** The MBean representing this body */
     protected BodyWrapperMBean mbean;
     protected boolean isProActiveInternalObject = false;
 
     // MESSAGE-TAGS Factory
     protected MessageTagsFactory messageTagsFactory;
     protected Map<String, LocalMemoryTag> localMemoryTags;
 
     //
     // -- PRIVATE MEMBERS -----------------------------------------------
     //
 
     /** whether the body has an activity done with a active thread */
     private transient boolean isActive;
 
     /**
      * whether the body has been killed. A killed body has no more activity although stopping the
      * activity thread is not immediate
      */
     private transient boolean isDead;
 
     // GC
     // Initialized in the subclasses after the local body strategy
     protected transient GarbageCollector gc;
 
     // the StepByStep mode
     protected Debugger debugger;
 
     private String reifiedObjectClassName;
 
     //
     // -- CONSTRUCTORS -----------------------------------------------
     //
 
     /**
      * Creates a new AbstractBody. Used for serialization.
      */
     public AbstractBody() {
     }
 
     /**
      * Creates a new AbstractBody for an active object attached to a given node.
      * 
      * @param reifiedObject
      *            the active object that body is for
      * @param nodeURL
      *            the URL of the node that body is attached to
      * @param factory
      *            the factory able to construct new factories for each type of meta objects needed
      *            by this body
      */
     public AbstractBody(Object reifiedObject, String nodeURL, MetaObjectFactory factory, String jobId)
             throws ActiveObjectCreationException {
         super(nodeURL, jobId);
 
         this.threadStore = factory.newThreadStoreFactory().newThreadStore();
 
         // GROUP
         this.spmdManager = factory.newProActiveSPMDGroupManagerFactory().newProActiveSPMDGroupManager();
 
         if (reifiedObject != null)
             this.reifiedObjectClassName = reifiedObject.getClass().getName();
 
         ProActiveSecurity.loadProvider();
 
         this.debugger = factory.newDebuggerFactory().newDebugger();
         this.debugger.setTarget(this);
 
         // MESSAGE TAGS
         this.messageTagsFactory = factory.newRequestTagsFactory();
         this.localMemoryTags = new ConcurrentHashMap<String, LocalMemoryTag>();
 
         // SECURITY
         if (reifiedObject instanceof Secure) {
             this.isInterfaceSecureImplemented = true;
         }
 
         if ((this.securityManager = factory.getProActiveSecurityManager()) == null) {
             this.isSecurityOn = false;
             ProActiveLogger.getLogger(Loggers.SECURITY_BODY).debug("Active Object security Off");
         } else {
             this.isSecurityOn = true;
 
             ProActiveLogger.getLogger(Loggers.SECURITY_BODY).debug(
                     "Application is " + this.securityManager.getApplicationName());
             this.isSecurityOn = this.securityManager.getCertificate() != null;
             this.internalBodySecurity = new InternalBodySecurity(null); // SECURITY
         }
 
     }
 
     public void updateReference(UniversalBodyProxy ref) {
         this.gc.addProxy(this, ref);
     }
 
     public void updateReferences(Collection<UniversalBodyProxy> newReferences) {
         for (UniversalBodyProxy ubp : newReferences) {
             this.gc.addProxy(this, ubp);
         }
     }
 
     public BodyWrapperMBean getMBean() {
         return this.mbean;
     }
 
     public Collection<UniqueID> getReferences() {
         return this.gc.getReferencesID();
     }
 
     public GarbageCollector getGarbageCollector() {
         return this.gc;
     }
 
     public void setRegistered(boolean registered) {
         this.gc.setRegistered(registered);
     }
 
     public String getReifiedClassName() {
         return this.localBodyStrategy.getReifiedObject().getClass().getName();
     }
 
     //
     // -- PUBLIC METHODS -----------------------------------------------
     //
 
     /**
      * Returns a string representation of this object.
      * 
      * @return a string representation of this object
      */
     @Override
     public String toString() {
         // get the incarnation number if ft is enable
         String inc = (this.ftmanager != null) ? ("" + this.ftmanager) : ("");
 
         if (this.localBodyStrategy != null) {
             return "Body for " + this.localBodyStrategy.getName() + " node=" + this.nodeURL + " id=" +
                 this.bodyID + inc;
         }
 
         return "Method call called during Body construction -- the body is not yet initialized ";
     }
 
     //
     // -- implements UniversalBody -----------------------------------------------
     //
     public int receiveRequest(Request request) throws java.io.IOException, RenegotiateSessionException {
         // System.out.println("" + this + " --> receiveRequest m="+request.getMethodName());
         // NON_FT is returned if this object is not fault tolerant
         int ftres = FTManager.NON_FT;
         if (this.ftmanager != null) {
             if (this.isDead) {
                 throw new BodyTerminatedRequestException(reifiedObjectClassName, request != null ? request
                         .getMethodName() : null);
             } else {
                 ftres = this.ftmanager.onReceiveRequest(request);
                 if (request.ignoreIt()) {
                     return ftres;
                 }
             }
         }
         try {
             this.enterInThreadStore();
             if (this.isDead) {
                 throw new BodyTerminatedRequestException(reifiedObjectClassName, request != null ? request
                         .getMethodName() : null);
             }
             if (this.isSecurityOn) {
 
                 /*
                  * if (isInterfaceSecureImplemented) { Session session =
                  * psm.getSession(request.getSessionId()); ((Secure)
                  * getReifiedObject()).receiveRequest(session.getSecurityContext()); }
                  */
                 try {
                     this.renegociateSessionIfNeeded(request.getSessionId());
                     if ((this.internalBodySecurity.isLocalBody()) && request.isCiphered()) {
                         request.decrypt(this.securityManager);
                     }
                 } catch (SecurityNotAvailableException e) {
                     // do nothing
                     e.printStackTrace();
                 }
             }
             this.registerIncomingFutures();
             ftres = this.internalReceiveRequest(request);
             if (GarbageCollector.dgcIsEnabled()) {
                 updateReferences(UniversalBodyProxy.getIncomingReferences());
             }
         } finally {
             this.exitFromThreadStore();
         }
         return ftres;
     }
 
     public int receiveReply(Reply reply) throws java.io.IOException {
         // System.out.println(" --> receiveReply m="+reply.getMethodName());
         // NON_FT is returned if this object is not fault tolerant
         int ftres = FTManager.NON_FT;
         if (this.ftmanager != null) {
             // if the futurepool is not null while body is dead,
             // this AO still has ACs to do.
             if (this.isDead && (this.getFuturePool() == null)) {
                 throw new BodyTerminatedReplyException(reifiedObjectClassName, reply != null ? reply
                         .getMethodName() : null);
             } else {
                 ftres = this.ftmanager.onReceiveReply(reply);
                 if (reply.ignoreIt()) {
                     return ftres;
                 }
             }
         }
 
         try {
             enterInThreadStore();
             if (this.isDead && (this.getFuturePool() == null)) {
                 throw new BodyTerminatedReplyException(reifiedObjectClassName, reply != null ? reply
                         .getMethodName() : null);
             }
 
             // System.out.println("Body receives Reply on NODE : " + this.nodeURL);
             if (this.isSecurityOn) {
                 try {
                     if ((this.internalBodySecurity.isLocalBody()) && reply.isCiphered()) {
                         reply.decrypt(this.securityManager);
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
             this.registerIncomingFutures();
             ftres = internalReceiveReply(reply);
             if (GarbageCollector.dgcIsEnabled()) {
                 updateReferences(UniversalBodyProxy.getIncomingReferences());
             }
         } finally {
             exitFromThreadStore();
         }
         return ftres;
     }
 
     /**
      * This method effectively register futures (ie in the futurePool) that arrive in this active
      * object (by parameter or by result). Incoming futures have been registered in the static table
      * FuturePool.incomingFutures during their deserialization. This effective registration must be
      * perform AFTER entering in the ThreadStore.
      */
     public void registerIncomingFutures() {
         // get list of futures that should be deserialized and registered "behind the ThreadStore"
         java.util.ArrayList<Future> incomingFutures = FuturePool.getIncomingFutures();
 
         if (incomingFutures != null) {
             // if futurePool is not null, we are in an Active Body
             if (getFuturePool() != null) {
                 // some futures have to be registered in the local futurePool
                 java.util.Iterator<Future> it = incomingFutures.iterator();
                 while (it.hasNext()) {
                     Future current = it.next();
                     getFuturePool().receiveFuture(current);
                 }
                 FuturePool.removeIncomingFutures();
             } else {
                 // we are in a migration forwarder,just remove registered futures
                 FuturePool.removeIncomingFutures();
             }
         }
     }
 
     public void enableAC() {
         this.localBodyStrategy.getFuturePool().enableAC();
     }
 
     public void disableAC() {
         this.localBodyStrategy.getFuturePool().disableAC();
     }
 
     public void renegociateSessionIfNeeded(long sID) throws RenegotiateSessionException,
             SecurityNotAvailableException, IOException {
         try {
             enterInThreadStore();
             if (!this.internalBodySecurity.isLocalBody() && (this.openedSessions != null)) {
                 // inside a forwarder
                 Long sessionID;
 
                 // long sID = request.getSessionId();
                 if (sID != 0) {
                     sessionID = Long.valueOf(sID);
                     if (this.openedSessions.contains(sessionID)) {
                         this.openedSessions.remove(sessionID);
                         this.internalBodySecurity.terminateSession(sID);
                         // System.out.println("Object has migrated : Renegociate Session");
                         throw new RenegotiateSessionException(this.internalBodySecurity.getDistantBody());
                     }
                 }
             }
         } finally {
             exitFromThreadStore();
         }
     }
 
     public void terminateSession(long sessionID) throws SecurityNotAvailableException, IOException {
         try {
             enterInThreadStore();
             if (this.isSecurityOn) {
                 if (this.internalBodySecurity.isLocalBody()) {
                     this.securityManager.terminateSession(sessionID);
                 } else {
                     this.internalBodySecurity.terminateSession(sessionID);
                 }
             }
         } finally {
             exitFromThreadStore();
         }
     }
 
     public TypedCertificate getCertificate() throws SecurityNotAvailableException, IOException {
         try {
             enterInThreadStore();
             if (this.isSecurityOn) {
                 if (this.internalBodySecurity.isLocalBody()) {
                     // System.out.println(" getCertificate on demande un security manager a " +
                     // ProActive.getBodyOnThis());
                     // if (psm == null) {
                     // startDefaultProActiveSecurityManager();
                     // }
                     return this.securityManager.getCertificate();
                 }
 
                 return this.internalBodySecurity.getCertificate();
             }
             throw new SecurityNotAvailableException();
         } finally {
             exitFromThreadStore();
         }
     }
 
     public ProActiveSecurityManager getProActiveSecurityManager() {
         if (this.isSecurityOn && this.internalBodySecurity.isLocalBody()) {
             return this.securityManager;
         }
 
         return null;
     }
 
     public ProActiveSPMDGroupManager getProActiveSPMDGroupManager() {
         return this.spmdManager;
     }
 
     public long startNewSession(long distantSessionID, SecurityContext policy,
             TypedCertificate distantCertificate) throws SessionException, SecurityNotAvailableException,
             IOException {
         try {
             enterInThreadStore();
             if (this.isSecurityOn) {
                 if (this.internalBodySecurity.isLocalBody()) {
                     return this.securityManager.startNewSession(distantSessionID, policy, distantCertificate);
                 }
 
                 return this.internalBodySecurity
                         .startNewSession(distantSessionID, policy, distantCertificate);
             }
             throw new SecurityNotAvailableException();
         } finally {
             exitFromThreadStore();
         }
     }
 
     public PublicKey getPublicKey() throws SecurityNotAvailableException, IOException {
         try {
             enterInThreadStore();
             if (this.isSecurityOn) {
                 PublicKey pk;
 
                 if (this.internalBodySecurity.isLocalBody()) {
                     // System.out.println("getPublicKey on demande un security manager a " +
                     // ProActive.getBodyOnThis());
                     // if (psm == null) {
                     // startDefaultProActiveSecurityManager();
                     // }
                     pk = this.securityManager.getPublicKey();
 
                     return pk;
                 } else {
                     pk = this.internalBodySecurity.getPublicKey();
 
                     return pk;
                 }
             }
             throw new SecurityNotAvailableException();
         } finally {
             exitFromThreadStore();
         }
     }
 
     public byte[] randomValue(long sessionID, byte[] clientRandomValue) throws SecurityNotAvailableException,
             RenegotiateSessionException, IOException {
         try {
             enterInThreadStore();
             if (this.isSecurityOn) {
                 byte[] plop;
 
                 if (this.internalBodySecurity.isLocalBody()) {
                     plop = this.securityManager.randomValue(sessionID, clientRandomValue);
 
                     return plop;
                 } else {
                     plop = this.internalBodySecurity.randomValue(sessionID, clientRandomValue);
 
                     return plop;
                 }
             }
             throw new SecurityNotAvailableException();
         } finally {
             exitFromThreadStore();
         }
     }
 
     public byte[] publicKeyExchange(long sessionID, byte[] signature) throws SecurityNotAvailableException,
             RenegotiateSessionException, KeyExchangeException, IOException {
         try {
             enterInThreadStore();
             if (this.isSecurityOn) {
                 renegociateSessionIfNeeded(sessionID);
 
                 if (this.internalBodySecurity.isLocalBody()) {
                     return this.securityManager.publicKeyExchange(sessionID, signature);
                 }
 
                 // else
                 return this.internalBodySecurity.publicKeyExchange(sessionID, signature);
             }
             throw new SecurityNotAvailableException();
         } finally {
             exitFromThreadStore();
         }
     }
 
     public byte[][] secretKeyExchange(long sessionID, byte[] encodedAESKey, byte[] encodedIVParameters,
             byte[] encodedClientMacKey, byte[] encodedLockData, byte[] parametersSignature)
             throws SecurityNotAvailableException, RenegotiateSessionException, IOException {
         try {
             enterInThreadStore();
             if (!this.isSecurityOn) {
                 throw new SecurityNotAvailableException();
             }
 
             byte[][] ske;
 
             renegociateSessionIfNeeded(sessionID);
 
             if (this.internalBodySecurity.isLocalBody()) {
                 // System.out.println("secretKeyExchange demande un security manager a " +
                 // ProActive.getBodyOnThis());
                 ske = this.securityManager.secretKeyExchange(sessionID, encodedAESKey, encodedIVParameters,
                         encodedClientMacKey, encodedLockData, parametersSignature);
 
                 return ske;
             } else {
                 ske = this.internalBodySecurity.secretKeyExchange(sessionID, encodedAESKey,
                         encodedIVParameters, encodedClientMacKey, encodedLockData, parametersSignature);
 
                 return ske;
             }
         } finally {
             this.threadStore.exit();
         }
     }
 
     public SecurityContext getPolicy(Entities local, Entities distant) throws SecurityNotAvailableException,
             IOException {
         try {
             enterInThreadStore();
             if (!this.isSecurityOn) {
                 throw new SecurityNotAvailableException();
             }
 
             if (this.internalBodySecurity.isLocalBody()) {
                 return this.securityManager.getPolicy(local, distant);
             } else {
                 return this.internalBodySecurity.getPolicy(local, distant);
             }
         } finally {
             exitFromThreadStore();
         }
     }
 
     // public byte[] getCertificateEncoded()
     // throws SecurityNotAvailableException, IOException {
     // try {
     // enterInThreadStore();
     //
     // //if (psm == null) {
     // // startDefaultProActiveSecurityManager();
     // // }
     // if (!this.isSecurityOn || (this.securityManager == null)) {
     // throw new SecurityNotAvailableException();
     // }
     //
     // if (this.internalBodySecurity.isLocalBody()) {
     // return this.securityManager.getCertificate().getEncoded();
     // } else {
     // return this.internalBodySecurity.getCertificatEncoded();
     // }
     // } catch (CertificateEncodingException e) {
     // e.printStackTrace();
     // } finally {
     // exitFromThreadStore();
     // }
     // return null;
     // }
     protected void startDefaultProActiveSecurityManager() {
         try {
             // logger.info("starting a new psm ");
             // TODO SECURITY check type (app/object/...)
             this.securityManager = new DefaultProActiveSecurityManager(EntityType.UNKNOWN);
             this.isSecurityOn = true;
             // this.securityManager.setBody(this);
             this.internalBodySecurity = new InternalBodySecurity(null);
         } catch (Exception e) {
             logger.error("Error when contructing a DefaultProActiveManager");
             e.printStackTrace();
         }
     }
 
     public Entities getEntities() throws SecurityNotAvailableException, IOException {
         try {
             enterInThreadStore();
             if (!this.isSecurityOn) {
                 throw new SecurityNotAvailableException();
             }
             if (this.internalBodySecurity.isLocalBody()) {
                 return this.securityManager.getEntities();
             } else {
                 return this.internalBodySecurity.getEntities();
             }
         } finally {
             exitFromThreadStore();
         }
     }
 
     //
     // -- implements Body -----------------------------------------------
     //
     public void terminate() {
         this.terminate(true);
     }
 
     public void terminate(boolean completeACs) {
         if (logger.isDebugEnabled()) {
             logger.debug("Calling terminate for active object " + this.reifiedObjectClassName);
         }
         if (this.isDead && (this.getFuturePool() == null)) {
             return;
         }
         if (Profiling.TIMERS_COMPILED) {
             // THE VARIABLE TIMERS_COMPILED SHOULD BE EVALUATED ALONE
             if (this.timersContainer != null) {
                 // Stops wfr, serve and total
                 this.timersContainer.stopAll();
                 // We need to finalize statistics of the timers container
                 // for this body
                 this.timersContainer.sendResults(this.getName(), this.bodyID.shortString());
                 this.timersContainer = null;
             }
         }
         this.isDead = true;
         // the ACthread is not killed if completeACs is true AND there is
         // some ACs remaining...
         activityStopped(completeACs && this.getFuturePool().remainingAC());
         this.remoteBody = null;
         // unblock is thread was block
         acceptCommunication();
 
         // JMX unregistration
         if (mbean != null) {
             MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
             ObjectName objectName = mbean.getObjectName();
             if (mbs.isRegistered(objectName)) {
                 try {
                     mbs.unregisterMBean(objectName);
                 } catch (InstanceNotFoundException e) {
                     logger.error("The MBean with the objectName " + objectName + " was not found", e);
                 } catch (MBeanRegistrationException e) {
                     logger.error("The MBean with the objectName " + objectName +
                         " can't be unregistered from the MBean server", e);
                 }
             }
             this.mbean = null;
         }
 
         // END JMX unregistration
     }
 
     public void blockCommunication() {
         this.threadStore.close();
     }
 
     public void acceptCommunication() {
         this.threadStore.open();
     }
 
     public void enterInThreadStore() {
         this.threadStore.enter();
     }
 
     public void exitFromThreadStore() {
         this.threadStore.exit();
     }
 
     public boolean isAlive() {
         return !this.isDead;
     }
 
     public boolean isActive() {
         return this.isActive;
     }
 
     /**
      * Set the sterility status for the body. A sterile body will not be allowed to send any
      * request, except to itself or its parent body (the body which send the request currently
      * served).
      */
     public void setSterility(boolean isSterile, UniqueID parentUID) {
         this.isSterileBody = isSterile;
         this.parentUID = parentUID;
     }
 
     public boolean isSterile() {
         return this.isSterileBody;
     }
 
     /**
      * When using a ForgetOnSend strategy for sending a request, retrieves the {@link UniqueID} of
      * the body who sends the request which is currently served. This information is important
      * because sterile body can still send a request to this body.
      */
     public UniqueID getParentUID() {
         return this.parentUID;
     }
 
     /**
      * Invoke this method to use the FOS strategy when sending a request <i>methodName</i> to
      * <i>activeObject</i>.
      */
     public void setForgetOnSendRequest(Object activeObject, String methodName) {
         if (sendingQueue == null) {
             sendingQueue = new SendingQueue();
         }
         sendingQueue.addFosRequest(activeObject, methodName);
     }
 
     /**
      * Invoke this method to stop using the FOS strategy when sending a request <i>methodName</i>
      * to <i>activeObject</i>.
      */
     public void removeForgetOnSendRequest(Object activeObject, String methodName) {
         if (sendingQueue == null) {
             return;
         }
         sendingQueue.removeFosRequest(activeObject, methodName);
     }
 
     public boolean isForgetOnSendRequest(Object activeObject, String methodName) {
         if (sendingQueue == null) {
             return false;
         }
         return sendingQueue.isFosRequest(activeObject, methodName);
     }
 
     /**
      * Retrieve the SendingQueue attached to this body. Can be null
      * 
      * @return the SendingQueue
      */
     public SendingQueue getSendingQueue() {
         return sendingQueue;
     }
 
     public UniversalBody checkNewLocation(UniqueID bodyID) {
         // we look in the location table of the current JVM
         Body body = LocalBodyStore.getInstance().getLocalBody(bodyID);
         if (body != null) {
             // we update our table to say that this body is local
             this.location.updateBody(bodyID, body);
             return body;
         } else {
             // it was not found in this vm let's try the location table
             return this.location.getBody(bodyID);
         }
     }
 
     /*
      * 
      * @see org.objectweb.proactive.Body#getShortcutTargetBody(org.objectweb.proactive.core.component.representative.ItfID)
      */
     public UniversalBody getShortcutTargetBody(ItfID functionalItfID) {
         if (this.shortcuts == null) {
             return null;
         } else {
             if (this.shortcuts.containsKey(functionalItfID)) {
                 return ((Shortcut) this.shortcuts.get(functionalItfID)).getShortcutTargetBody();
             } else {
                 return null;
             }
         }
     }
 
     // public void setPolicyServer(PolicyServer server) {
     // if (server != null) {
     // if ((this.securityManager != null) &&
     // (this.securityManager.getPolicyServer() == null)) {
     // this.securityManager = new ProActiveSecurityManager(EntityType.UNKNOWN, server);
     // this.isSecurityOn = true;
     // logger.debug("Security is on " + this.isSecurityOn);
     // // this.securityManager.setBody(this);
     // }
     // }
     // }
 
     //
     // -- implements LocalBody -----------------------------------------------
     //
     public FuturePool getFuturePool() {
         return this.localBodyStrategy.getFuturePool();
     }
 
     public BlockingRequestQueue getRequestQueue() {
         return this.localBodyStrategy.getRequestQueue();
     }
 
     public Object getReifiedObject() {
         return this.localBodyStrategy.getReifiedObject();
     }
 
     public String getName() {
         return this.localBodyStrategy.getName();
     }
 
     /**
      * Serves the request. The request should be removed from the request queue before serving,
      * which is correctly done by all methods of the Service class. However, this condition is not
      * ensured for custom calls on serve.
      */
     public void serve(Request request) {
         // Sterility control
         // If the methodCall is sterile, the body must be sterile during its service
         if (request != null && request.getMethodCall() != null && request.getMethodCall().isSterile()) {
             setSterility(true, request.getSender().getID());
         }
 
         // Serve
         if (this.ftmanager != null) {
             this.ftmanager.onServeRequestBefore(request);
             this.localBodyStrategy.serve(request);
             this.ftmanager.onServeRequestAfter(request);
         } else {
             this.localBodyStrategy.serve(request);
         }
 
         // Sterility control
         // Once the service of a sterile methodCall is done, the body can be turned back to standard
         // mode
         this.setSterility(false, null);
     }
 
     public void sendRequest(MethodCall methodCall, Future future, UniversalBody destinationBody)
             throws IOException, RenegotiateSessionException {
         long distantSessionID = 0;
 
         // Tag the outgoing request with the barrier tags
         if (!this.spmdManager.isTagsListEmpty()) {
             methodCall.setBarrierTags(this.spmdManager.getBarrierTags());
         }
 
         try {
             if (!this.isSecurityOn) {
                 ProActiveLogger.getLogger(Loggers.SECURITY_BODY).debug("security is off");
             } else {
                 try {
                     if (this.internalBodySecurity.isLocalBody()) {
                         TypedCertificate cert = destinationBody.getCertificate();
                         ProActiveLogger.getLogger(Loggers.SECURITY_BODY).debug(
                                 "send Request AbstractBody " + this + ", method " + methodCall.getName() +
                                     " cert " + cert.getCert().getSubjectDN() + " " +
                                     cert.getCert().getPublicKey());
                         try {
                             distantSessionID = this.securityManager.getSessionTo(cert).getDistantSessionID();
                         } catch (SessionException e) {
                             distantSessionID = this.securityManager.initiateSession(destinationBody)
                                     .getDistantSessionID();
                         }
                     }
                 } catch (SecurityNotAvailableException e) {
                     // do nothing
                     bodyLogger.debug("communication without security");
                     // e.printStackTrace();
                 }
             }
 
             // add StepByStep and ExtrendedDebugger breakpoint
             if (!isProActiveInternalObject) {
                 debugger.breakpoint(BreakpointType.SendRequest, destinationBody);
             }
 
             this.localBodyStrategy.sendRequest(methodCall, future, destinationBody);
 
         } catch (RenegotiateSessionException e) {
             if (e.getUniversalBody() != null) {
                 e.printStackTrace();
                 ProActiveLogger.getLogger(Loggers.SECURITY_CRYPTO).debug(
                         "renegotiate session " + distantSessionID);
                 updateLocation(destinationBody.getID(), e.getUniversalBody());
                 this.securityManager.terminateSession(distantSessionID);
                 sendRequest(methodCall, future, e.getUniversalBody());
             } else {
                 this.securityManager.terminateSession(distantSessionID);
                 sendRequest(methodCall, future, destinationBody);
             }
         } catch (CommunicationForbiddenException e) {
             bodyLogger.debug("Communication forbidden." + e.getMessage());
             bodyLogger.warn(e);
             // if the communication is not allowed, set the result as the exception
             future.receiveReply(new MethodCallResult(null, new RuntimeSecurityException(e)));
             // e.printStackTrace();
         }
     }
 
     public Object receiveFTMessage(FTMessage fte) {
         // delegate to the FTManger
         Object res = null;
         if (this.ftmanager != null) {
             res = this.ftmanager.handleFTMessage(fte);
             // todo cdelbe : should not use FaultDetector here !
             // https://galpage-exp.inria.fr:8181/jira/browse/PROACTIVE-268
         } else if (fte instanceof Heartbeat) {
             if (!this.isAlive()) {
                 res = FaultDetector.IS_DEAD;
             } else {
                 res = FaultDetector.OK;
             }
         }
         return res;
     }
 
     //
     // -- PROTECTED METHODS -----------------------------------------------
     //
 
     /**
      * Receives a request for later processing. The call to this method is non blocking unless the
      * body cannot temporary receive the request.
      * 
      * @param request
      *            the request to process
      * @exception java.io.IOException
      *                if the request cannot be accepted
      */
     protected abstract int internalReceiveRequest(Request request) throws java.io.IOException,
             RenegotiateSessionException;
 
     /**
      * Receives a reply in response to a former request.
      * 
      * @param reply
      *            the reply received
      * @exception java.io.IOException
      *                if the reply cannot be accepted
      */
     protected abstract int internalReceiveReply(Reply reply) throws java.io.IOException;
 
     protected void setLocalBodyImpl(LocalBodyStrategy localBody) {
         this.localBodyStrategy = localBody;
     }
 
     /**
      * Signals that the activity of this body, managed by the active thread has just stopped.
      * 
      * @param completeACs
      *            if true, and if there are remaining AC in the futurepool, the AC thread is not
      *            killed now; it will be killed after the sending of the last remaining AC.
      */
     protected void activityStopped(boolean completeACs) {
         if (!this.isActive) {
             return;
         }
         this.isActive = false;
 
         // If any, stops the SendingThreadPool
         if (sendingQueue != null) {
             sendingQueue.stop();
         }
 
         // We are no longer an active body
         LocalBodyStore.getInstance().unregisterBody(this);
 
         // Thus, contexts are no more needed
         LocalBodyStore.getInstance().clearAllContexts();
 
         // JMX unregistration
         if (this.mbean != null) {
             MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
             ObjectName objectName = this.mbean.getObjectName();
             if (mbs.isRegistered(objectName)) {
                 try {
                     mbs.unregisterMBean(objectName);
                 } catch (InstanceNotFoundException e) {
                     logger.error("The MBean with the objectName " + objectName + " was not found", e);
                 } catch (MBeanRegistrationException e) {
                     logger.error("The MBean with the objectName " + objectName +
                         " can't be unregistered from the MBean server", e);
                 }
             }
             this.mbean = null;
         }
 
         // END JMX unregistration
     }
 
     /**
      * Signals that the activity of this body, managed by the active thread has just started.
      */
     protected void activityStarted() {
         if (this.isActive) {
             return;
         }
         isActive = true;
         // Set the initial context : we associated this body to the thread running it
         LocalBodyStore.getInstance().pushContext(new Context(this, null));
 
         // we register in this JVM
         LocalBodyStore.getInstance().registerBody(this);
     }
 
     /**
      * Set the SPMD group for the active object
      * 
      * @param o -
      *            the new SPMD group
      */
     public void setSPMDGroup(Object o) {
         this.spmdManager.setSPMDGroup(o);
     }
 
     /**
      * Returns the SPMD group of the active object
      * 
      * @return the SPMD group of the active object
      */
     public Object getSPMDGroup() {
         return this.spmdManager.getSPMDGroup();
     }
 
     /**
      * Returns the size of of the SPMD group
      * 
      * @return the size of of the SPMD group
      */
     public int getSPMDGroupSize() {
         return PAGroup.size(this.getSPMDGroup());
     }
 
     /**
      * To get the FTManager of this body
      * 
      * @return Returns the ftm.
      */
     public FTManager getFTManager() {
         return this.ftmanager;
     }
 
     /**
      * To set the FTManager of this body
      * 
      * @param ftm
      *            The ftm to set.
      */
     public void setFTManager(FTManager ftm) {
         this.ftmanager = ftm;
     }
 
     public GCResponse receiveGCMessage(GCMessage msg) {
         return this.gc.receiveGCMessage(msg);
     }
 
     // MESSAGE TAGS MEMORY
     /**
      * Create a local memory for the specified tag for a lease period inferior to 
      * the max lease period defined in properties.
      * @param id    - Tag Identifier
      * @param lease - Lease period of the memroy
      * @return The LocalMemoryTag 
      */
     public LocalMemoryTag createLocalMemoryTag(String id, int lease) {
         int maxLease = PAProperties.PA_MAX_MEMORY_TAG_LEASE.getValueAsInt();
         lease = (lease > maxLease) ? maxLease : lease;
         this.localMemoryTags.put(id, new LocalMemoryTag(id, lease));
         return this.localMemoryTags.get(id);
     }
 
     /**
      * Return the local memory of the specified Tag
      * @param id - Tag identifer
      * @return the LocalMemoryTag of the specified Tag
      */
     public LocalMemoryTag getLocalMemoryTag(String id) {
         return this.localMemoryTags.get(id);
     }
 
     /**
      * Clear the local memory of the specified Tag
      * @param id - Tag identifier
      */
     public void clearLocalMemoryTag(String id) {
         this.localMemoryTags.remove(id);
     }
 
     /**
      * To get the localMemoryTag Map of the body
      * @return Map<String, {@link LocalMemoryTag}> the LocalMemoryTags
      */
     public Map<String, LocalMemoryTag> getLocalMemoryTags() {
         return this.localMemoryTags;
     }
 
     /**
      * Returns true if an immediate service request is being served now.
      */
     public abstract boolean isInImmediateService() throws IOException;
 
     //
     // -- SERIALIZATION METHODS -----------------------------------------------
     //
     private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
         out.defaultWriteObject();
 
         mbean = null;
     }
 
     private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
         this.gc = new GarbageCollector(this);
         in.defaultReadObject();
         // FAULT TOLERANCE
         if (this.ftmanager != null) {
             if (this.ftmanager.isACheckpoint()) {
                 // re-use remote view of the old body if any
                 Body toKill = LocalBodyStore.getInstance().getLocalBody(this.bodyID);
                 if (toKill != null) {
                     // this body is still alive
                     toKill.blockCommunication();
                     RemoteObjectExposer<UniversalBody> toKillRoe = ((AbstractBody) toKill)
                             .getRemoteObjectExposer();
                     toKillRoe.getRemoteObject().setTarget(this);
 
                     this.roe = toKillRoe;
                     toKill.terminate(false);
                     toKill.acceptCommunication();
                 }
             }
         }
     }
 
     public ProActiveSecurityManager getProActiveSecurityManager(Entity user)
             throws SecurityNotAvailableException, AccessControlException {
         if (this.securityManager == null) {
             throw new SecurityNotAvailableException();
         }
         return this.securityManager.getProActiveSecurityManager(user);
     }
 
     public void setProActiveSecurityManager(Entity user, PolicyServer policyServer)
             throws SecurityNotAvailableException, AccessControlException {
         if (this.securityManager == null) {
             throw new SecurityNotAvailableException();
         }
         this.securityManager.setProActiveSecurityManager(user, policyServer);
     }
 
     public Debugger getDebugger() {
         return debugger;
     }
 
 }
