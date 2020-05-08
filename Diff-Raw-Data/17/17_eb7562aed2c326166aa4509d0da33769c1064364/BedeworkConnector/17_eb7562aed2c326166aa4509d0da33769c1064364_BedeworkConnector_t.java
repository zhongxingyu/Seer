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
 package org.bedework.synch.cnctrs.bedework;
 
 import org.bedework.synch.Notification;
 import org.bedework.synch.Subscription;
 import org.bedework.synch.SynchDefs;
 import org.bedework.synch.SynchDefs.SynchEnd;
 import org.bedework.synch.SynchEngine;
 import org.bedework.synch.cnctrs.Connector;
 import org.bedework.synch.cnctrs.ConnectorInstanceMap;
 import org.bedework.synch.cnctrs.ConnectorPropertyInfo;
 import org.bedework.synch.exception.SynchException;
 import org.bedework.synch.wsmessages.KeepAliveNotificationType;
 import org.bedework.synch.wsmessages.KeepAliveResponseType;
 import org.bedework.synch.wsmessages.StartServiceNotificationType;
 import org.bedework.synch.wsmessages.StartServiceResponseType;
 import org.bedework.synch.wsmessages.SynchIdTokenType;
 import org.bedework.synch.wsmessages.SynchRemoteService;
 import org.bedework.synch.wsmessages.SynchRemoteServicePortType;
 
 import org.apache.log4j.Logger;
 import org.oasis_open.docs.ns.wscal.calws_soap.GetPropertiesResponseType;
 import org.oasis_open.docs.ns.wscal.calws_soap.GetPropertiesType;
 import org.oasis_open.docs.ns.wscal.calws_soap.StatusType;
 import org.oasis_open.docs.ns.xri.xrd_1.XRDType;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.namespace.QName;
 
 /** The synch processor connector for connections to bedework.
  *
  * @author Mike Douglass
  */
 public class BedeworkConnector
       implements Connector<BedeworkConnectorInstance,
                            Notification> {
   private boolean debug;
 
   private transient Logger log;
 
   /** */
   public static final String propnameCalendarHref = "calendar-href";
 
   /** */
   public static final String propnamePrincipal = "principal";
 
   private static List<ConnectorPropertyInfo> propInfo =
       new ArrayList<ConnectorPropertyInfo>();
 
   static {
     propInfo.add(new ConnectorPropertyInfo(propnameCalendarHref,
                                            false,
                                            ""));
 
     propInfo.add(new ConnectorPropertyInfo(propnamePrincipal,
                                            false,
                                            ""));
   }
 
   private SynchEngine syncher;
 
   private BedeworkConnectorConfig config;
 
   private String callbackUri;
 
   private String connectorId;
 
   private static boolean running;
  private static boolean stopped;
 
   /* If non-null this is the token we currently have for bedework */
   private String remoteToken;
 
   private XRDType sysInfo;
 
   private ConnectorInstanceMap<BedeworkConnectorInstance> cinstMap =
       new ConnectorInstanceMap<BedeworkConnectorInstance>();
 
   /** This process will send keep-alive notifications to the remote system.
    * During startup the first notification is sent so this process starts with
    * a wait
    *
    */
   private class PingThread extends Thread {
     boolean showedTrace;
 
     /**
      * @param name - for the thread
      */
     public PingThread(final String name) {
       super(name);
     }
 
     @Override
     public void run() {
      while (!stopped) {
         if (debug) {
           trace("About to call service - token = " + remoteToken);
         }
         /* First see if we need to reinitialize or ping */
 
         try {
           if (remoteToken == null) {
             initConnection();
             if (remoteToken != null) {
               running = true;
             }
           } else {
             ping();
           }
         } catch (Throwable t) {
           if (!showedTrace) {
             error(t);
             showedTrace = true;
           } else {
             error(t.getMessage());
           }
         }
 
         // Wait a bit before trying again
 
         if (debug) {
           trace("About to pause - token = " + remoteToken);
         }
 
         try {
           Object o = new Object();
           long waitTime;
 
           if (remoteToken == null) {
             waitTime = config.getRetryInterval() * 1000;
           } else {
             waitTime = config.getKeepAliveInterval() * 1000;
           }
 
           synchronized (o) {
             o.wait(waitTime);
           }
        } catch (InterruptedException ie) {
          break;
         } catch (Throwable t) {
           error(t.getMessage());
         }
       }
     }
   }
 
   private static PingThread pinger;
 
   @Override
   public void start(final String connectorId,
                     final String callbackUri,
                     final SynchEngine syncher) throws SynchException {
     this.connectorId = connectorId;
     this.syncher = syncher;
     this.callbackUri = callbackUri;
 
    stopped = false;

     debug = getLogger().isDebugEnabled();
 
     config = (BedeworkConnectorConfig)syncher.getAppContext().getBean(connectorId + "BedeworkConfig");
 
     if (pinger == null) {
       pinger = new PingThread(connectorId);
       pinger.start();
     }
 
     this.syncher = syncher;
   }
 
   @Override
   public String getId() {
     return connectorId;
   }
 
   @Override
   public String getCallbackUri() {
     return callbackUri;
   }
 
   @Override
   public SynchEngine getSyncher() {
     return syncher;
   }
 
   @Override
   public List<ConnectorPropertyInfo> getPropertyInfo() {
     return propInfo;
   }
 
   @Override
   public BedeworkConnectorInstance getConnectorInstance(final Subscription sub,
                                                         final SynchEnd end) throws SynchException {
     if (!running) {
       return null;
     }
 
     BedeworkConnectorInstance inst = cinstMap.find(sub, end);
 
     if (inst != null) {
       return inst;
     }
 
     //debug = getLogger().isDebugEnabled();
     BedeworkSubscriptionInfo info;
 
     if (end == SynchEnd.endA) {
       info = new BedeworkSubscriptionInfo(sub.getEndAConnectorInfo());
     } else {
       info = new BedeworkSubscriptionInfo(sub.getEndBConnectorInfo());
     }
 
     inst = new BedeworkConnectorInstance(config, this, sub, end, info);
     cinstMap.add(sub, end, inst);
 
     return inst;
   }
 
   class BedeworkNotificationBatch extends NotificationBatch<Notification> {
   }
 
   @Override
   public BedeworkNotificationBatch handleCallback(final HttpServletRequest req,
                                      final HttpServletResponse resp,
                                      final String[] resourceUri) throws SynchException {
     return null;
   }
 
   @Override
   public void respondCallback(final HttpServletResponse resp,
                               final NotificationBatch<Notification> notifications)
                                                     throws SynchException {
   }
 
   @Override
   public void stop() throws SynchException {
    stopped = true;
     if (pinger != null) {
       pinger.interrupt();
     }
   }
 
   /* ====================================================================
    *                   Protected methods
    * ==================================================================== */
 
   protected void info(final String msg) {
     getLogger().info(msg);
   }
 
   protected void trace(final String msg) {
     getLogger().debug(msg);
   }
 
   protected void error(final Throwable t) {
     getLogger().error(this, t);
   }
 
   protected void error(final String msg) {
     getLogger().error(msg);
   }
 
   protected void warn(final String msg) {
     getLogger().warn(msg);
   }
 
   /* Get a logger for messages
    */
   protected Logger getLogger() {
     if (log == null) {
       log = Logger.getLogger(this.getClass());
     }
 
     return log;
   }
 
   /* ====================================================================
    *                         Package methods
    * ==================================================================== */
 
   SynchRemoteServicePortType getPort() throws SynchException {
     try {
       URL wsURL = new URL(config.getBwWSDLURI());
 
       SynchRemoteService ers =
         new SynchRemoteService(wsURL,
                                new QName(SynchDefs.synchNamespace,
                                          "SynchRemoteService"));
       SynchRemoteServicePortType port = ers.getSynchRSPort();
 
       return port;
     } catch (Throwable t) {
       throw new SynchException(t);
     }
   }
 
   SynchIdTokenType getIdToken(final String principal) {
     SynchIdTokenType idToken = new SynchIdTokenType();
 
     idToken.setPrincipalHref(principal);
     idToken.setSubscribeUrl(callbackUri);
     idToken.setSynchToken(remoteToken);
 
     return idToken;
   }
 
   /* ====================================================================
    *                   Private methods
    * ==================================================================== */
 
   /**
    * @throws SynchException
    */
   public void ping() throws SynchException {
     KeepAliveNotificationType kan = new KeepAliveNotificationType();
 
     kan.setSubscribeUrl(callbackUri);
     kan.setToken(remoteToken);
 
     KeepAliveResponseType kar = getPort().pingService(kan);
 
     if (kar.getStatus() != StatusType.OK) {
       warn("Received status " + kar.getStatus() + " for ping");
       remoteToken = null; // Force reinit after wait
 
       running = false;
     }
   }
 
   private void initConnection() throws SynchException {
     StartServiceNotificationType ssn = new StartServiceNotificationType();
 
     ssn.setSubscribeUrl(callbackUri);
 
     StartServiceResponseType ssr = getPort().startService(ssn);
 
     if (ssr.getStatus() != StatusType.OK) {
       warn("Received status " + ssr.getStatus() + " to start notification");
       return;
     }
 
     remoteToken = ssr.getToken();
 
     if (sysInfo == null) {
       // Try to get info
       GetPropertiesType gp = new GetPropertiesType();
 
       gp.setHref("/");
 
       GetPropertiesResponseType gpr = getPort().getProperties(getIdToken(null),
                                                               gp);
 
       if (gpr != null) {
         sysInfo = gpr.getXRD();
       }
     }
   }
 }
