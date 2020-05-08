 package org.vika.routing.network.jade;
 
 import jade.core.AID;
 import jade.core.Agent;
 import jade.domain.FIPAAgentManagement.AMSAgentDescription;
 import jade.lang.acl.ACLMessage;
 import org.vika.routing.Message;
 
 import java.io.IOException;
 
 /**
  * @author oleg
  * @date 21.04.11
  */
 public class AgentsUtil {
     /**
      * Send message to the agent with given id
      */
     public static void sendMessage(final Agent[] agents, final int receiver, final Message message){
         final AMSAgentDescription agent = findAMSAgentDescription(agents, receiver);
         ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
         msg.addReceiver(agent.getName());
         try {
             msg.setContentObject(message);
         } catch (IOException e) {
             // Ignore this
         }
        agents[receiver].send(msg);
     }
 
     static AMSAgentDescription findAMSAgentDescription(final Agent[] agents, final int id) {
         final AMSAgentDescription description = new AMSAgentDescription();
         final AID aid = agents[id].getAID();
         description.setName(aid);
         return description;
     }
 }
