 package net.codjo.security.server.login;
 import java.util.HashMap;
 import java.util.Map;
 import net.codjo.agent.Aid;
 import net.codjo.agent.UserId;
 import net.codjo.agent.behaviour.AmsListenerBehaviour;
 import net.codjo.plugin.common.session.SessionManager;
 import net.codjo.plugin.common.session.SessionRefusedException;
 import org.apache.log4j.Logger;
 /**
  *
  */
 class SessionLifecycleBehaviour extends AmsListenerBehaviour {
    private static final Logger LOG = Logger.getLogger(ServerLoginAgent.class);
     private final SessionManager sessionManager;
     private final Map<Aid, UserId> userIdMap = new HashMap<Aid, UserId>();
 
 
     SessionLifecycleBehaviour(SessionManager sessionManager) {
         this.sessionManager = sessionManager;
         setAgentDeathHandler(new AgentDeathHandler());
     }
 
 
     public void declare(Aid aid, UserId userId) throws SessionRefusedException {
         userIdMap.put(aid, userId);
         sessionManager.startSession(userId);
     }
 
 
     private class AgentDeathHandler implements EventHandler {
         public void handle(Aid deadAgentId) {
             UserId userId = userIdMap.remove(deadAgentId);
             if (userId != null) {
                 LOG.info("Arrt de la session de " + userId.encode() + " en cours");
                 sessionManager.stopSession(userId);
             }
         }
     }
 }
