 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mde;
 
 import jade.core.Agent;
 import jade.core.behaviours.CyclicBehaviour;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 /**
  *
  * @author vicente
  */
 public class ShareMultimediaBehaviour extends CyclicBehaviour {
 
   public ShareMultimediaBehaviour() {
   }
 
   public ShareMultimediaBehaviour(Agent a) {
     super(a);
   }
   
   @Override
   public void action() {
     MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
     ACLMessage msg = myAgent.receive(mt);
     if(msg != null) {
       MultimediaSharerAgent multimediaSharerAgent = (MultimediaSharerAgent) myAgent;
       ACLMessage reply = msg.createReply();
      System.out.println("File Name: "+msg.getContent());
      File file = (File) multimediaSharerAgent.getCatalogue().get(msg.getContent());
       byte [] multimediaContent = null;
       if(file != null && file.exists()) {
         try {
           FileInputStream fileInput = new FileInputStream(file);
           multimediaContent = new byte[(int) file.length()];
           fileInput.read(multimediaContent);
         } catch (IOException ex) {
           ex.printStackTrace();
           //Logger.getLogger(ShareMultimediaBehaviour.class.getName()).log(Level.SEVERE, null, ex);
         }
         reply.setPerformative(ACLMessage.INFORM);
         reply.setByteSequenceContent(multimediaContent);
         //cfp.addUserDefinedParameter("multimedia-name", targetMultimedia);
         System.out.println("I'm going to send the multimedia content");
         
       } else {
         reply.setPerformative(ACLMessage.FAILURE);
         reply.setContent("multimedia-not-available");
       }
       myAgent.send(reply);
     } else {
       block();
     }
     
   }
   
 }
