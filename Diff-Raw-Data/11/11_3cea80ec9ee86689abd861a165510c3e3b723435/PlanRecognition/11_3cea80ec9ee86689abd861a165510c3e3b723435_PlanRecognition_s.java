 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.yura.domination.engine.ai.planrecognition;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import net.yura.domination.engine.ai.planrecognition.events.Event;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 import net.yura.domination.engine.ai.planrecognition.environmentdatamanagement.Agent;
 import net.yura.domination.engine.ai.planrecognition.eventhandling.AbstractService;
 import net.yura.domination.engine.ai.planrecognition.eventhandling.EventObserver;
 import net.yura.domination.engine.ai.planrecognition.events.EventCountryReinforced;
 import net.yura.domination.engine.ai.planrecognition.events.EventRegisterAgent;
 import net.yura.domination.engine.ai.planrecognition.events.EventSuccessfulOccupation;
 import net.yura.domination.engine.ai.planrecognition.events.EventArmyMoved;
 import net.yura.domination.engine.ai.planrecognition.events.EventFailedOccupation;
 import net.yura.domination.engine.ai.planrecognition.events.EventRemoveAgent;
 import net.yura.domination.engine.ai.planrecognition.planlibraryobjects.components.action.BasicAction;
 import net.yura.domination.engine.ai.planrecognition.planlibraryobjects.components.explanation.Explanation;
 import net.yura.domination.engine.ai.planrecognition.planlibraryobjects.components.observation.BasicObservation;
 import net.yura.domination.engine.ai.planrecognition.planlibraryobjects.components.observation.ObservationArmyMovement;
 import net.yura.domination.engine.ai.planrecognition.planlibraryobjects.components.observation.ObservationFailedDefence;
 import net.yura.domination.engine.ai.planrecognition.planlibraryobjects.components.observation.ObservationSuccessfulOccupation;
 import net.yura.domination.engine.ai.planrecognition.planlibraryobjects.components.observation.ObservationCountryReinforced;
 import net.yura.domination.engine.ai.planrecognition.planlibraryobjects.components.observation.ObservationFailedOccupation;
 import net.yura.domination.engine.ai.planrecognition.planlibraryobjects.components.observation.ObservationSuccessfulDefence;
 import net.yura.domination.engine.core.Player;
 import net.yura.domination.engine.core.RiskGame;
 
 /**
  *
  * @author s0914007
  */
 public class PlanRecognition extends AbstractService implements Serializable{
 
     public ExplanationManager explanationManager;  
     List<Agent> agentManager = new ArrayList<Agent>();
     
     Set<Explanation> totalExplanationList;
     
     public static long file_name_var;
     double totalProb;
     
     public static Vector Players;
  
     public PlanRecognition(EventObserver processing){
         
         super(processing);
     }
 
     public List<Agent> getAgentManager() {
         
         return agentManager;
     }
 
     @Override
     protected void startUp() throws Exception {
 
             super.startUp();
     }
     
     @Override
     protected void shutDown() throws Exception {
 
             super.shutDown();
     }
     
     @Override
     protected void handle(Event event) throws Exception {
         
         // Adds players to agentManager
         if(event instanceof  EventRegisterAgent){
             
             EventRegisterAgent newA = (EventRegisterAgent) event;
             
             Agent newAgent = new Agent(newA.getNewPlayer().getName());
             
             agentManager.add(newAgent);
         }
         
         // Removes a player given the players index number from the environment
         if(event instanceof EventRemoveAgent){
          
             EventRemoveAgent removeAgent = (EventRemoveAgent) event;
             
             // Removes Agent from Agent Manager
             agentManager.remove(removeAgent.getAgentLocationNumber());
         }
         
         if(event instanceof EventSuccessfulOccupation){
             
             EventSuccessfulOccupation currentEvent = (EventSuccessfulOccupation) event;
             
             String agentWon = currentEvent.getPlayerWonName();
             String agentLost = currentEvent.getPlayerLostName();
             
             String continentName = currentEvent.getContinentName();
             String countryName = currentEvent.getCountryName();
             
             // Compute Explanation probabilites based on latest observation
             for(Agent a : agentManager){
                 
                 if(a.getAgentName().equals(agentWon)){
                     
                     // Add observation to agents observation list
                     a.getAgentObservationSet().add(new ObservationSuccessfulOccupation(continentName, countryName));
 
                     //System.out.println(a.getAgentName());
                     //System.out.println(ActionConstants.successfulOccupation + " " + countryName);
                     //System.out.println(" ");
                     
                     this.computeExpProbs("Weighted", ActionConstants.successfulOccupation, a);
                     
                     // Sum probabilities of all explanations
                     totalProb = a.computeTotalExpProbabilties();
                     
                     this.printExpProbs(a);
                 }
                 
                 if(a.getAgentName().equals(agentLost)){
                     
                     a.getAgentObservationSet().add(new ObservationFailedDefence(continentName, countryName));
                     
                     //System.out.println(a.getAgentName());
                     //System.out.println(ActionConstants.failedDefence + " " + countryName);
                     //System.out.println(" ");
                     
                     this.computeExpProbs("Weighted", ActionConstants.failedDefence, a);
                     
                     totalProb = a.computeTotalExpProbabilties();
                     //a.removeCountryAndUpdateList(continentName, country);
                     
                     this.printExpProbs(a);
                 }
             }
             // Testing - Iterate through observation manager
             /*for(String s : observationManager.keySet()){
                 
                 System.out.println(s);
                 
                 for(BasicObservation b : observationManager.get(s)){
                     
                     if(b instanceof ObservationCountryOccupied){ System.out.println(b.getActionType() + " " + b.getCountryName()); }
                     if(b instanceof ObservationCountryLost){ System.out.println(b.getActionType() + " " + b.getCountryName()); }
                 }
             }*/
             //System.out.println(" ");
         }
         
         if(event instanceof EventFailedOccupation){
             
             EventFailedOccupation currentEvent = (EventFailedOccupation) event;
             
             String agentWon = currentEvent.getPlayerWonName();
             String agentLost = currentEvent.getPlayerLostName();
             
             String continentName = currentEvent.getContinentName();
             String countryName = currentEvent.getCountryName();
             
             // Compute Explanation probabilites based on latest observation
             for(Agent a : agentManager){
                 
                 if(a.getAgentName().equals(agentWon)){
                     
                     // Add observation to agents observation list
                     a.getAgentObservationSet().add(new ObservationSuccessfulDefence(continentName, countryName));
                     
                     //System.out.println(a.getAgentName());
                     //System.out.println(ActionConstants.successfulDefence + " " + countryName);
                     //System.out.println(" ");
 
                     this.computeExpProbs("Weighted", ActionConstants.successfulDefence, a);
                     
                     // Sum probabilities of all explanations
                     totalProb = a.computeTotalExpProbabilties();
                     
                     this.printExpProbs(a);
                 }
                 
                 if(a.getAgentName().equals(agentLost)){
                     
                     a.getAgentObservationSet().add(new ObservationFailedOccupation(continentName, countryName));
                     
                     //System.out.println(a.getAgentName());
                     //System.out.println(ActionConstants.failedOccupation + " " + countryName);
                     //System.out.println(" ");
                     
                     this.computeExpProbs("Weighted", ActionConstants.failedOccupation, a);
                     
                     totalProb = a.computeTotalExpProbabilties();
                     //a.removeCountryAndUpdateList(continentName, country);
                     
                     this.printExpProbs(a);
                 }
             }
         }
         
         if(event instanceof EventCountryReinforced){
             
             EventCountryReinforced reinforcedCountry = (EventCountryReinforced) event;
             
             String playerName = reinforcedCountry.getPlayerName();
             
             String continentName = reinforcedCountry.getContinentName();
             String countryName = reinforcedCountry.getCountryName();
             
             for(Agent a : agentManager){
                 
                 if(a.getAgentName().equals(playerName)){
                     
                     a.getAgentObservationSet().add(new ObservationCountryReinforced(continentName, countryName));
   
                     //System.out.println(a.getAgentName());
                     //System.out.println(ActionConstants.countryReinforced + " " + countryName);
                     //System.out.println(" ");
                     
                     this.computeExpProbs("Weighted", ActionConstants.countryReinforced, a);
                     
                     totalProb = a.computeTotalExpProbabilties();
                     
                     this.printExpProbs(a);
                 }
             }
             //System.out.println(reinforcedCountry.getPlayerName() + " reinforced " + reinforcedCountry.getCountryName());
         }
         
         if(event instanceof EventArmyMoved){
             
             EventArmyMoved countryMovedTo = (EventArmyMoved) event;
             
             String playerName = countryMovedTo.getPlayerName();
             
             String continentName = countryMovedTo.getContinentName();
             String countryName = countryMovedTo.getCountryName();
             
             for(Agent a : agentManager){
                 
                 if(a.getAgentName().equals(playerName)){
                     
                     a.getAgentObservationSet().add(new ObservationArmyMovement(continentName, countryName));
                 
                     //System.out.println(a.getAgentName());
                     //System.out.println(ActionConstants.armyMovement + " " + countryName);
                     //System.out.println(" ");
                     
                     this.computeExpProbs("Weighted", ActionConstants.armyMovement, a);
                     
                     totalProb = a.computeTotalExpProbabilties();
                 
                     this.printExpProbs(a);
                 } 
             }
             //System.out.println(countryMovedTo.getPlayerName() + " " + countryMovedTo.getCountryName()); 
         }
     }
     
     // Method to synchronize Player Vector in this class and all classes that contain a player object
     public void updatePlayers(Vector Players){
 
         PlanRecognition.setPlayers(Players);
     }
 
     // Setup of action space
     public void initialiseActionSpace(Vector Continents){
 
         this.explanationManager = new ExplanationManager(Continents);
 
         totalExplanationList = explanationManager.getAllExplanations();
         
         for(Agent a : agentManager){
             
             a.generateExplanationList(totalExplanationList);
         }
     }
     
     public void printExpProbs(Agent a){
         
         Set<Explanation> expList = a.getAgentExplanationList();
         
         for(Explanation e : expList){
 
             // compute normalized probabilites
             e.setExplanationProbability(e.normalizeExplanationProbability(totalProb));
   
             //if(e.getExplanationProbability() > 0.10){
             //System.out.println(e.getMissionName() + " " + e.getExplanationProbability());
             //}
         }
         //System.out.println(" ");
     }
     
     public void computeExpProbs(String calculationType, String observationType, Agent agent){
         
         Set<BasicAction> activePendingSet = agent.generateActivePendingSet();
         BasicObservation currentObservation = agent.getAgentObservationSet().get(agent.getAgentObservationSet().size() - 1);
         
         for(Explanation e : agent.getAgentExplanationList()){
         
             e.computeMissionProbability(activePendingSet, currentObservation);
         }
     }
 
     public static Vector getPlayers() {
         
         return PlanRecognition.Players;
     }
 
     public static void setPlayers(Vector Players) {
         
         PlanRecognition.Players = Players;
     }
     
     public void printAllProbs() throws IOException {
     
         file_name_var = System.currentTimeMillis();
             
         File folder = new File("C:/Users/s0914007/Desktop/game-logs/data-folder-" + file_name_var);
         //System.out.println("Executed!");
         
         if(!folder.exists()){
             
             folder.mkdir();
         }
         
         File file = new File("C:/Users/s0914007/Desktop/game-logs/data-folder-" + file_name_var + "/data-" + file_name_var + ".csv");
         
         if(!file.exists()){
             
             file.createNewFile();
         }
         
         FileWriter fw = new FileWriter(file);
         BufferedWriter bw = new BufferedWriter(fw);
         
         for(Agent a : this.getAgentManager()){
             
             bw.write(a.getAgentName());
             bw.newLine();
             for(Object o : Players){
                 
                 Player p = (Player) o;
                 
                 if(a.getAgentName().equals(p.getName())){
                     
                     bw.write(p.getMission().getDiscription());
                     
                    for(int i = 1; i <= RiskGame.turnCounter; i++){
                         
                         bw.write(", Turn " + i);
                     }
                     bw.newLine();
                 }
             }
             for(String s : a.getMyMutlimap().keySet()){
 
                 bw.write(s);
                 //System.out.print(s);
                 for(Map<Integer, Double> probs : a.getMyMutlimap().get(s)){
 
                     for(Integer i : probs.keySet()){
 
                         //System.out.print(", " + probs.get(i));
                         bw.write(", " + probs.get(i));
                     }
                 }
                 bw.newLine();
             }
         }
         bw.close();
         System.out.println("Completed! File data-" + file_name_var + ".csv");
     }
 }
 
