 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mde;
 
 import jade.core.AID;
 import jade.core.Agent;
 import jade.core.behaviours.Behaviour;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author vicente
  */
 public class RequestMultimediaBehaviour extends Behaviour {
   
   private AID targetAgent;
   private MessageTemplate mt;
   private int step = 0;
 
   public RequestMultimediaBehaviour() {
   }
   
   public RequestMultimediaBehaviour(Agent a) {
     super(a);
   }
 
   public RequestMultimediaBehaviour(AID targetAgent, Agent a) {
     super(a);
     this.targetAgent = targetAgent;
   }
 
   @Override
   public void action() {
     
     MultimediaSharerAgent multimediaSharerAgent = (MultimediaSharerAgent) myAgent;
     switch(step) {
       case 0:
         ACLMessage request = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
         request.addReceiver(targetAgent);
         request.setContent(multimediaSharerAgent.getTargetMultimedia());
         request.setConversationId("multimedia-trade");
         request.setReplyWith("request" + System.currentTimeMillis());
         myAgent.send(request);
         mt = MessageTemplate.and(MessageTemplate.MatchConversationId("multimedia-trade"),
 			          MessageTemplate.MatchInReplyTo(request.getReplyWith()));
         ++step;
       case 1:
         ACLMessage reply = myAgent.receive(mt);
         if(reply != null) {
           
           if(reply.getPerformative() == ACLMessage.INFORM) {
             try {
               byte [] multimediaContent = reply.getByteSequenceContent();
               FileOutputStream fileOutput = 
                       new FileOutputStream("./MDE Downloads/" + multimediaSharerAgent.getTargetMultimedia());
               fileOutput.write(multimediaContent);
             } catch (IOException ex) {
               ex.printStackTrace();
               //Logger.getLogger(RequestMultimediaBehaviour.class.getName()).log(Level.SEVERE, null, ex);
             }
           } else {
             System.out.println("Attempt failed: " + reply.getContent());
           }
           ++step;  
         }
     }
   }
 
   @Override
   public boolean done() {
     return (step == 2);
   }
   
   //Getters and Setters
 
   public AID getTargetAgent() {
     return targetAgent;
   }
 
   public void setTargetAgent(AID targetAgent) {
     this.targetAgent = targetAgent;
   }
 
   public Agent getMyAgent() {
     return myAgent;
   }
 
   public void setMyAgent(Agent myAgent) {
     this.myAgent = myAgent;
   }
    
 }
