 package simulator.agents;
 
 import agents.AgentInterface;
 import behaviours.simulation.ReceiveCrossData;
 import jade.core.AID;
 import jade.core.Agent;
 import jade.domain.DFService;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.domain.FIPAAgentManagement.SearchConstraints;
 import jade.domain.FIPAAgentManagement.ServiceDescription;
 import jade.domain.FIPAException;
 import jade.wrapper.AgentController;
 import jade.wrapper.StaleProxyException;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Scanner;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Zagadka
  */
 public class Controller extends Agent implements AgentInterface {
 
     ArrayList<String> names = new ArrayList<String>();
     ArrayList<String> positions = new ArrayList<String>();
 
     @Override
     public void setup() {
 
         System.out.println("Hello I am the Actor Controller agent my name is: "
                 + this.getLocalName());
         try {
             Scanner s = new Scanner(new BufferedReader(new FileReader("src/names.txt")));
             while (s.hasNext()) {
                 //set delimiter to ,<space>
                 s.useDelimiter(",\\s");
                 names.add(s.next());
             }
         } catch (FileNotFoundException ex) {
             System.out.println("src/names.txt not found");
         }
 
         addBehaviour(new ReceiveCrossData(this));
 
 //        for(int i = 0; i < 500; i++){
 //            doWait(10);
 //        }
 
     }
 
     public void startNewPerson() throws StaleProxyException {
 
 //        String agentName = "Stan";
 //        Object[] args = {"00500400", "5000", "2"};
 //
 //        startNewAgent("agents.actors.GeneralPerson", agentName, args);
 //
 //        agentName = "bingo";
 //        Object[] args2 = {"07770470", "5000", "3"};
 //
 //        startNewAgent("agents.actors.GeneralPerson", agentName, args2);
 //
 //
 //        agentName = "bingo2";
 //        Object[] args3 = {"07770470", "5000", "3"};
 //
 //        startNewAgent("agents.actors.GeneralPerson", agentName, args3);
 //
 //
 //        agentName = "bingo3";
 //        Object[] args4 = {"07770470", "5000", "3"};
 //
 //        startNewAgent("agents.actors.GeneralPerson", agentName, args4);
 
         Random random = new Random();
 
         
         for (int i = 0; i < names.size(); i++) {
             String agentName = names.get(i);
            Object[] args = {"05000500",
                "5000", "1"};
             startNewAgent("agents.actors.GeneralPerson", agentName, args);
         }
     }
 
     public void addNewPosition(String position) {
         positions.add(position);
         //System.out.println(positions.size());
         
         if(positions.size()>4){
         try {
             startNewPerson();
         } catch (StaleProxyException ex) {
             Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
         }
         }
         
         
     }
 
     private void startNewAgent(String className, String agentName, Object[] arguments) throws StaleProxyException {
         ((AgentController) getContainerController().createNewAgent(agentName, className, arguments)).start();
     }
 
     private AID lookforValidCross() {
         AID[] cross = searchDF("CrossSection");
         //System.out.print("\nCROSSSECTIONS: ");
         Random random = new Random();
 
         AID across = cross[random.nextInt(cross.length)];
 
         return across;
     }
 
     private AID[] searchDF(String service) {
         DFAgentDescription dfd = new DFAgentDescription();
         ServiceDescription sd = new ServiceDescription();
         sd.setType(service);
         dfd.addServices(sd);
 
         SearchConstraints ALL = new SearchConstraints();
         ALL.setMaxResults(new Long(-1));
         try {
             DFAgentDescription[] result = DFService.search(this, dfd, ALL);
             AID[] agents = new AID[result.length];
             for (int i = 0; i < result.length; i++) {
                 agents[i] = result[i].getName();
             }
             return agents;
         } catch (FIPAException fe) {
         }
         return null;
     }
 }
