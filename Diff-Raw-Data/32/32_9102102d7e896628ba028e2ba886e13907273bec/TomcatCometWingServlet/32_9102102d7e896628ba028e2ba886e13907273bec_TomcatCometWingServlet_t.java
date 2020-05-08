 package org.wings.comet;
 
 import org.apache.catalina.CometEvent;
 import org.apache.catalina.CometProcessor;
 import org.wings.session.*;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 import java.io.IOException;
 
 public class TomcatCometWingServlet
         extends CometWingServlet implements CometProcessor {
 
     public void event(CometEvent cometEvent) throws IOException, ServletException {
 
         final HttpServletRequest request = cometEvent.getHttpServletRequest();
         final HttpServletResponse response = cometEvent.getHttpServletResponse();
 
         //cometEvent.setTimeout(TIMEOUT);
 
         if (cometEvent.getEventType() == CometEvent.EventType.BEGIN) {
            log.debug("BEGIN: " + request.getSession());
 
             final Session session = getSession(request);
             if (session == null) {
                 this.service(request, response);
                 cometEvent.close();
                 return;
             }
 
             final Comet comet = session.getComet();
             final Pushable pushable = comet.getPushable();
             final CometConnectionManager connectionManager = comet.getConnectionManager();
 
             final String pathInfo = request.getPathInfo();
 
             if (connectionManager != null) {
                 if (HANGING_PATH.equals(pathInfo)) {
                     if (!connectionManager.hangingGetActive(true)) {
                         synchronized (pushable) {
                             cometEvent.setTimeout((int)comet.getLongPollingTimeout());
                             pushable.setPushInfo(response);
                         }
                     } else {
                         pushable.setPeriodicPolling(response);
                         cometEvent.close();
                     }
                 } else {
                     final String param = request.getParameter(PERIODIC_POLLING_PARAM);
                     if (param != null) {
                         if (!connectionManager.isHangingGetActive()) {
                             synchronized (pushable) {
                                 if (!pushable.isSwitchActive()) {
                                     pushable.setSwitchActive(true);
                                     pushable.switchToHanging();
                                 }
                             }
                         }
                     }
                     this.service(request, response);
                     cometEvent.close();
                 }
             } else {
                 if (HANGING_PATH.equals(pathInfo)) {
                     synchronized (pushable) {
                         cometEvent.setTimeout((int)comet.getLongPollingTimeout());
                         pushable.setPushInfo(response);
                     }
                 } else {
                     this.service(request, response);
                     cometEvent.close();
                 }
             }
 
         } else if (cometEvent.getEventType() == CometEvent.EventType.ERROR) {
            log.error("ERROR: " + request.getSession() + "\n");
 
             if (cometEvent.getEventSubType() == CometEvent.EventSubType.TIMEOUT) {
                log.debug("---Timeout: " + request.getSession() + "\n");
 
                 final Pushable pushable = getPushable(request);
                 synchronized (pushable) {
                     pushable.reset();
                     pushable.reconnect();
                 }
             }
             if (cometEvent.getEventSubType() == CometEvent.EventSubType.CLIENT_DISCONNECT) {
                log.debug("---Client_Disconnect: " + request.getSession() + "\n");
             }
             if (cometEvent.getEventSubType() == CometEvent.EventSubType.IOEXCEPTION) {
                log.debug("---IOException: " + request.getSession() + "\n");
             }
 
             cometEvent.close();
 
         } else if (cometEvent.getEventType() == CometEvent.EventType.END) {
            log.debug("END: " + request.getSession() + "\n");
 
             if (cometEvent.getEventSubType() == CometEvent.EventSubType.WEBAPP_RELOAD) {
                log.debug("---Webapp_Reload: " + request.getSession() + "\n");
             }
             if (cometEvent.getEventSubType() == CometEvent.EventSubType.SESSION_END) {
                log.debug("---Session_End: " + request.getSession() + "\n");
             }
             if (cometEvent.getEventSubType() == CometEvent.EventSubType.SERVER_SHUTDOWN) {
                log.debug("---Server_Shutdown: " + request.getSession() + "\n");
             }
 
             final Pushable pushable = getPushable(request);
             pushable.reset();
 
             cometEvent.close();
         }
     }
 
     private Pushable getPushable(HttpServletRequest request) throws ServletException {
         Session session = getSession(request);
         Comet comet = session.getComet();
         return comet.getPushable();
     }
 }
