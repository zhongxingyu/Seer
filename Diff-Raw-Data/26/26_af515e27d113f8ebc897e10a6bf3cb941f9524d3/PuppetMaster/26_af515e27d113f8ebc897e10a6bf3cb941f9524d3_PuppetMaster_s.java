 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package simulator.agents;
 
 import agents.AgentInterface;
 import jade.core.AID;
 import jade.core.Agent;
 import jade.lang.acl.ACLMessage;
 import jade.wrapper.AgentController;
 import jade.wrapper.StaleProxyException;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Zagadka
  */
 public class PuppetMaster extends Agent implements AgentInterface {
 
     ArrayList<String> usedNames = new ArrayList<String>();
     ArrayList<String> names = new ArrayList<String>();
 
     @Override
     public void setup() {
         System.out.println("Hello I am the PuppetMaster agent my name is: "
                 + this.getLocalName());
 
         //start the actor controller:
         try {
             ((AgentController) getContainerController().createNewAgent(ACTORCONTROL, "simulator.agents.Controller", null)).start();
         } catch (StaleProxyException ex) {
             Logger.getLogger(PuppetMaster.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         
         //start the collector agent:
                 try {
             ((AgentController) getContainerController().createNewAgent(COLLECTOR, "simulator.agents.Collector", null)).start();
         } catch (StaleProxyException ex) {
             Logger.getLogger(PuppetMaster.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         
         
         doWait(1000);
 
         //startNewPerson("now");
 
         //Starting up all the stores:
         try {
             Scanner s = new Scanner(new BufferedReader(new FileReader("src/stores.txt")));
             while (s.hasNextLine()) {
                 Scanner s2 = new Scanner(s.nextLine());
                 s2.useDelimiter(",");
                 //set delimiter to ,<space>
                 String name = s2.next();
                 //String pos = s.next();
                 Object[] args = {s2.next(), s2.next()};
                 try {
                     startNewStore(name, args);
                 } catch (StaleProxyException ex) {
                     Logger.getLogger(PuppetMaster.class.getName()).log(Level.SEVERE, null, ex);
                 }
 
             }
         } catch (FileNotFoundException ex) {
             System.out.println("src/stores.txt not found");
         }
 
         //starting up the Crosssections:
         try {
             Scanner s = new Scanner(new BufferedReader(new FileReader("src/cross.txt")));
             while (s.hasNextLine()) {
                 Scanner s2 = new Scanner(s.nextLine());
                 s2.useDelimiter(",");
                 ArrayList<String> tmp = new ArrayList<String>();
                 String res, name;
                 name = s2.next();
                 res = s2.next();
                 /*Sending the position data of the crossection to the PersonControl
                  * so that it knows where it can legally generate persons:
                  */
                 ACLMessage aclMessage = new ACLMessage(ACLMessage.INFORM);
                 aclMessage.setConversationId("fullcrosssectiondata");                
                 AID r = new AID(ACTORCONTROL, AID.ISLOCALNAME);
                 aclMessage.addReceiver(r);
                 aclMessage.setContent(res);
                 this.send(aclMessage);
 
                 /*...............................................................*/
                 //put the remainder in a zone array:
                 while (s2.hasNext()) {
                     String value = s2.next();
                     tmp.add(value);
                 }
                 Object[] args = new Object[tmp.size() + 1];
                 args[0] = res;
                 for (int i = 1; i <= tmp.size(); i++) {
                     args[i] = tmp.get(i - 1);
                 }
                 try {
                     startNewCrossSection(name, args);
                 } catch (StaleProxyException ex) {
                     Logger.getLogger(PuppetMaster.class.getName()).log(Level.SEVERE, null, ex);
                 }
 
             }
         } catch (FileNotFoundException ex) {
             System.out.println("src/cross.txt not found");
         }
 
 
     }
 
     private void startNewStore(String agentName, Object[] arguments) throws StaleProxyException {
         ((AgentController) getContainerController().createNewAgent(agentName, "agents.actors.GeneralStore", arguments)).start();
     }
 
     private void startNewCrossSection(String agentName, Object[] arguments) throws StaleProxyException {
         ((AgentController) getContainerController().createNewAgent(agentName, "agents.actors.CrossSection", arguments)).start();
     }
     
 }
