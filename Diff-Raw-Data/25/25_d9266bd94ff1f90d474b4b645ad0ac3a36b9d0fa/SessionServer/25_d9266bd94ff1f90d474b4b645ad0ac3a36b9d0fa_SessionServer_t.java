 package com.tinkerpop.rexster.protocol.server;
 
 import com.tinkerpop.rexster.client.RexProException;
 import com.tinkerpop.rexster.protocol.EngineController;
 import com.tinkerpop.rexster.protocol.RexProSession;
 import com.tinkerpop.rexster.protocol.RexProSessions;
 import com.tinkerpop.rexster.protocol.msg.*;
 import com.tinkerpop.rexster.server.RexsterApplication;
 import com.yammer.metrics.Gauge;
 import com.yammer.metrics.MetricRegistry;
 import org.apache.log4j.Logger;
 
 import java.io.IOException;
 import java.util.List;
 
 /**
  * TODO: add session metrics
  */
 public class SessionServer {
 
     private static final Logger logger = Logger.getLogger(SessionServer.class);
 
     private final RexsterApplication rexsterApplication;
 
     public SessionServer(final RexsterApplication rexsterApplication) {
         this.rexsterApplication = rexsterApplication;
 
         final String metricName = MetricRegistry.name("rexpro", "sessions");
         if (!this.rexsterApplication.getMetricRegistry().getGauges().containsKey(metricName)) {
             this.rexsterApplication.getMetricRegistry().register(metricName, new Gauge<Integer>() {
                 @Override
                 public Integer getValue() {
                     return RexProSessions.getSessionKeys().size();
                 }
             });
         }
     }
 
     public void handleRequest(SessionRequestMessage message, RexProRequest request) throws IOException {
         try {
             message.validateMetaData();
         } catch (Exception e) {
             logger.error(e);
             request.writeResponseMessage(
                 MessageUtil.createErrorResponse(
                     message.Request,
                     RexProMessage.EMPTY_SESSION_AS_BYTES,
                     ErrorResponseMessage.INVALID_MESSAGE_ERROR,
                     e.toString()
                 )
             );
         }
 
         if (message.metaGetKillSession()) {
             //destroy the session
             RexProSessions.destroySession(message.sessionAsUUID().toString());
             request.writeResponseMessage(MessageUtil.createEmptySession(message.Request));
 
         } else {
             final EngineController engineController = EngineController.getInstance();
             final List<String> engineLanguages = engineController.getAvailableEngineLanguages();
 
             final SessionResponseMessage responseMessage = MessageUtil.createNewSession(
                     message.Request, engineLanguages);
 
             // construct a session with the right channel
             if(!RexProSessions.hasSessionKey(responseMessage.sessionAsUUID().toString())) {
                 RexProSession session = RexProSessions.createSession(
                         responseMessage.sessionAsUUID().toString(),
                         this.rexsterApplication,
                         message.Channel
                 );
 
                 //set on the request object
                 request.setSession(session);
 
                 //configure the graph object
                 if (message.metaGetGraphName() != null) {
                     try {
                         session.setGraphObj(message.metaGetGraphName(), message.metaGetGraphObjName());
                     } catch (RexProException ex) {
                         //graph config problem
                         request.writeResponseMessage(
                                 MessageUtil.createErrorResponse(
                                         message.Request, RexProMessage.EMPTY_SESSION_AS_BYTES,
                                         ErrorResponseMessage.GRAPH_CONFIG_ERROR,
                                         ex.toString()
                                 )
                         );
                         return;
                     }
                 }
             }
            request.writeResponseMessage(responseMessage);
         }
 
        //todo: move this to the script filter?
//        if (!RexProSessions.hasSessionKey(message.sessionAsUUID().toString())) {
//            // the message is assigned a session that does not exist on the server
//            request.writeResponseMessage(
//                    MessageUtil.createErrorResponse(
//                            message.Request, RexProMessage.EMPTY_SESSION_AS_BYTES,
//                            ErrorResponseMessage.INVALID_SESSION_ERROR,
//                            MessageTokens.ERROR_SESSION_INVALID
//                    )
//            );
//            return;
//        }
 
     }
 }
