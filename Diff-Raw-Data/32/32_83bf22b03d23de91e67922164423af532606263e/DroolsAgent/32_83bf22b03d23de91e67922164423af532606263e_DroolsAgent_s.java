 package org.drools.fipa;
 
 
 import org.drools.runtime.StatefulKnowledgeSession;
 
 import java.util.HashMap;
 import java.util.Map;
 
 
 /**
  * First implementation of a drools-based intelligent, communicative agent.
  * The agent is capable of receiving messages and reacting adequately
  *
  * Messages are based on the FIPA agent intercommunication standard
  *
  * TODO: agent needs to be interfaced with more standard communication channels
  */
 public class DroolsAgent {
 
 
     /**
      * Agent Identifier
      */
     private AgentID agentId;
 
     /**
      * Main Agent Knowledge Session
      * Message management (interpretation, routing) and response management will take place in this session
      */
     private StatefulKnowledgeSession mind;
 
     /**
      * Response channel
      */
     private final DroolsAgentResponseInformer responseInformer;
 
     
     
 
     /**
      * Main constructor
      *
      * @param id
      * @param session
      * @param responseInformer
      */
     DroolsAgent(AgentID id, StatefulKnowledgeSession session, DroolsAgentResponseInformer responseInformer) {
         this.agentId = id;
         this.mind = session;
         this.responseInformer = responseInformer;
     }
 
 
     /**
      * Main interface method, used to accept incoming messages.
      * Messages are simply inserted into the main session and processed there
      * @param msg
      */
     public void tell(ACLMessage msg) {
         MessageContentEncoder.decodeBody(msg.getBody(), msg.getEncoding());
         this.mind.insert(msg);
         this.mind.fireAllRules();
     }
 
     /**
      * Destructor
      */
     public void dispose() {
         Map<String,StatefulKnowledgeSession> proxies = (Map<String,StatefulKnowledgeSession>) mind.getGlobal("proxies");
         if (proxies != null) {
             for (String sid : proxies.keySet()) {
                 StatefulKnowledgeSession subSession = proxies.get(sid);
                 if (subSession != null) {
                     subSession.dispose();
                 }
             }
         }
         mind.dispose();
     }
 
     public StatefulKnowledgeSession getInnerSession(String sessionId) {
         if (sessionId == null) {
             return mind;
         } else {
             Map<String,StatefulKnowledgeSession> proxies = (Map<String,StatefulKnowledgeSession>) mind.getGlobal("proxies");
             return proxies.get(sessionId);
         }
 
     }
 
     public AgentID getAgentId() {
         return agentId;
     }
 
     public StatefulKnowledgeSession getMind() {
         return mind;
     }
 
     public DroolsAgentResponseInformer getResponseInformer() {
         return responseInformer;
     }
 
 
 
 
 
 }
