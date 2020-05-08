 package sma;
 
 import java.lang.*;
 import java.io.*;
 import java.sql.SQLException;
 import java.sql.ResultSet;
 import jade.util.leap.ArrayList;
 import jade.util.leap.List;
 import jade.core.*;
 import jade.core.behaviours.*;
 import jade.domain.*;
 import jade.domain.FIPAAgentManagement.*;
 import jade.domain.FIPANames.InteractionProtocol;
 import jade.lang.acl.*;
 import jade.content.*;
 import jade.content.onto.*;
 import jade.proto.AchieveREInitiator;
 import jade.proto.AchieveREResponder;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import sma.ontology.*;
 import sma.gui.*;
 import java.util.*;
 /**
  * <p><B>Title:</b> IA2-SMA</p>
  * <p><b>Description:</b> Practical exercise 2011-12. Recycle swarm.</p>
  * <p><b>Copyright:</b> Copyright (c) 2009</p>
  * <p><b>Company:</b> Universitat Rovira i Virgili (<a
  * href="http://www.urv.cat">URV</a>)</p>
  * @author not attributable
  * @version 2.0
  */
 public class CoordinatorAgent extends Agent {
   private static final long serialVersionUID = 1L;
   
   private static final int STATE_NONE = -1;
   private static final int STATE_INITIAL_REQUEST = 0;
   private static final int STATE_MOVE_BOATS = 1;
   private static final int STATE_UPDATE_MAP = 2;
   private static final int STATE_TURNS_INTERVAL = 3;
 
   private AID centralAgent;
   private AID BoatsCoordinator;
   
   private AuxInfo gameInfo;
   
   // Data of interest during game
   private BoatsPosition boatsPosition;
   
   // Game state
   private int currentNegotiation;
   private int currentTurn;
   private int currentState;
 
   public BoatsPosition getBoatsPosition(){
       return boatsPosition;
   }
   
   /**
    * A message is shown in the log area of the GUI
    * @param str String to show
    */
   private void showMessage(String str) {
     System.out.println(getLocalName() + ": " + str);
   }
 
   public void setGameInfo(AuxInfo gI){
       this.gameInfo = gI;
   }
   
   public void setBoatsPosition(BoatsPosition positions){
       this.boatsPosition = positions;
   }
 
   /**
    * Agent setup method - called when it first come on-line. Configuration
    * of language to use, ontology and initialization of behaviours.
    */
   protected void setup() {
     // Initialize game states
     this.currentNegotiation = 0;
     this.currentTurn = 0;
       
       
     /**** Very Important Line (VIL) *********/
     this.setEnabledO2ACommunication(true, 1);
     /****************************************/
     
     // Register the agent to the DF
     ServiceDescription sd1 = new ServiceDescription();
     sd1.setType(UtilsAgents.COORDINATOR_AGENT);
     sd1.setName(getLocalName());
     sd1.setOwnership(UtilsAgents.OWNER);
     DFAgentDescription dfd = new DFAgentDescription();
     dfd.addServices(sd1);
     dfd.setName(getAID());
     try {
       DFService.register(this, dfd);
       showMessage("Registered to the DF");
     }
     catch (FIPAException e) {
       System.err.println(getLocalName() + " registration with DF " + "failed. Reason: " + e.getMessage());
       doDelete();
     }
 
     // Search for the CentralAgent
     ServiceDescription searchCriterion = new ServiceDescription();
     searchCriterion.setType(UtilsAgents.CENTRAL_AGENT);
     this.centralAgent = UtilsAgents.searchAgent(this, searchCriterion);
 
     // Execute the finite state automata
     this.currentState = STATE_INITIAL_REQUEST;
     finiteStateAutomata();
   } //endof setup
 
   /**************************************************************************/
   /**************************************************************************/
 
   public void finiteStateAutomata(){
       switch(this.currentState){ 
           
           case STATE_INITIAL_REQUEST:
               stateInitialRequest();
               this.currentState = STATE_MOVE_BOATS;
               break;
 
           case STATE_MOVE_BOATS:
               stateMoveBoats();
               this.currentState = STATE_UPDATE_MAP;
               break;
 
           case STATE_UPDATE_MAP:
               stateUpdateMap();
              this.currentState = STATE_UPDATE_MAP;

              // TODO: INFORM CENTRAL AGENT OF NEW BOATS POSITIONS
               this.currentState = STATE_TURNS_INTERVAL;
               break;
                 
           case STATE_TURNS_INTERVAL:
              
               break;
       }
   }
   
   private void stateInitialRequest(){
       // Request initial state to the central agent
       ACLMessage message = buildInitialRequest();
       this.addBehaviour(new StateRequestBehaviour(this, message));
 
       // Create boats coordinator
       UtilsAgents.createAgent(this.getContainerController(), "BoatCoordinator", "sma.BoatCoordinator", null);
 
       //Search for the BoatCoordinator
       ServiceDescription searchBoatCoordCriteria = new ServiceDescription();
       searchBoatCoordCriteria.setName("BoatCoordinator");
       this.BoatsCoordinator = UtilsAgents.searchAgent(this, searchBoatCoordCriteria);
   }
   
   private void stateMoveBoats(){
       // Request boats movement to boats coordinator
       ACLMessage message = buildMovementRequest();
       this.addBehaviour(new MovementRequestBehaviour(this, message));
   }
 
 
   private void stateUpdateMap(){
       // Requests central agent from coordinator agent
       ACLMessage message = buildUpdateRequest();
       this.addBehaviour(new UpdateRequestBehaviour(this, message));
   }
   
   /**************************************************************************/
   /**************************************************************************/
   
   private ACLMessage buildInitialRequest(){
     ACLMessage requestInicial = new ACLMessage(ACLMessage.REQUEST);
     
     requestInicial.clearAllReceiver();
     requestInicial.addReceiver(this.centralAgent);
     requestInicial.setProtocol(InteractionProtocol.FIPA_REQUEST);
     showMessage("Message OK");
     
     try {
       requestInicial.setContent("Initial request");
       showMessage("Content OK: " + requestInicial.getContent());
     } catch (Exception e) {
       e.printStackTrace();
     }
     
     return requestInicial;
   }
   
   private ACLMessage buildMovementRequest(){
     ACLMessage movementRequest = new ACLMessage(ACLMessage.REQUEST);
     
     movementRequest.clearAllReceiver();
     movementRequest.addReceiver(this.BoatsCoordinator);
     movementRequest.setProtocol(InteractionProtocol.FIPA_REQUEST);
     movementRequest.setContent("Movement request");
     
     return movementRequest;
   }
 
   public ACLMessage buildUpdateRequest(){
     ACLMessage updateRequest = new ACLMessage(ACLMessage.REQUEST);
 
     updateRequest.clearAllReceiver();
     updateRequest.addReceiver(this.centralAgent);
     updateRequest.setProtocol(InteractionProtocol.FIPA_REQUEST);
     updateRequest.setContent("Update boats request");
     
     try {
         updateRequest.setContentObject(boatsPosition);
     } catch (Exception e) {
         updateRequest.setPerformative(ACLMessage.FAILURE);
         System.err.println(e.toString());
         e.printStackTrace();
     }
     
     return updateRequest;
   }
 
   /**************************************************************************/
   /**************************************************************************/
 
   // ************************************
   // ** INITIATOR :: STATE REQUEST
   // **   SENDER   -> THIS
   // **   RECEIVER -> CENTRAL AGENT
   // ************************************
   
   class StateRequestBehaviour extends AchieveREInitiator {
     private Agent      sender  = null;
     private ACLMessage msgSent = null;
     
     public StateRequestBehaviour(Agent myAgent, ACLMessage requestMsg) {
       super(myAgent, requestMsg);
       showMessage("AchieveREInitiator StateRequest starts...");
       
       sender = myAgent;
       msgSent = requestMsg;
     }
 
     protected void handleAgree(ACLMessage msg) {
       showMessage("AGREE received from "+ ( (AID)msg.getSender()).getLocalName());
     }
 
     protected void handleInform(ACLMessage msg) {
     	showMessage("INFORM COORD AGENT received from "+ ( (AID)msg.getSender()).getLocalName()+" ... [OK]");
         try {
           AuxInfo info = (AuxInfo)msg.getContentObject();
           setGameInfo(info);
           if (info instanceof AuxInfo) {
             for (InfoAgent ia : info.getAgentsInitialPosition().keySet()){  
           	showMessage("Agent ID: " + ia.getName());          	  
                 if (ia.getAgentType() == AgentType.Boat){
                     showMessage("Agent type: " + ia.getAgentType().toString());
                     Object[] position = new Object[4];
                     position[0] = info.getAgentsInitialPosition().get(ia).getRow();
                     position[1] = info.getAgentsInitialPosition().get(ia).getColumn();
                     position[2] = info.getMap()[0].length;
                     position[3] = info.getMap().length;
                     UtilsAgents.createAgent(this.myAgent.getContainerController(),ia.getName(), "sma.BoatAgent", position);
                 }else showMessage("no agent type");
           	  
                 Cell pos = (Cell)info.getAgentsInitialPosition().get(ia);
                 showMessage("pos: " + pos);
                 
                 // Update state machine
                 ((CoordinatorAgent)sender).finiteStateAutomata();
             }
           }
         } catch (Exception e) {
           showMessage("Incorrect content: "+e.toString());
         }
     }
 
     protected void handleNotUnderstood(ACLMessage msg) {
       showMessage("This message NOT UNDERSTOOD. \n");
     }
 
     protected void handleFailure(ACLMessage msg) {
       showMessage("The action has failed.");
 
     } //End of handleFailure
 
     protected void handleRefuse(ACLMessage msg) {
       showMessage("Action refused.");
     }
   } //Endof class StateRequestBehaviour
   
   // ************************************
   // ** INITIATOR :: MOVEMENT REQUEST
   // **   SENDER   -> THIS
   // **   RECEIVER -> BOATS COORDINATOR
   // ************************************
   
   class MovementRequestBehaviour extends AchieveREInitiator {
     private Agent      sender = null;
     private ACLMessage msgSent = null;
     
     public MovementRequestBehaviour(Agent myAgent, ACLMessage requestMsg) {
       super(myAgent, requestMsg);
       showMessage("AchieveREInitiator starts...");
       
       sender = myAgent;
       msgSent = requestMsg;
     }
     
     protected void handleAgree(ACLMessage msg) {
       showMessage("AGREE received from "+ ( (AID)msg.getSender()).getLocalName());
     }
     
     protected void handleInform(ACLMessage msg) {
     	showMessage("INFORM received from "+ ( (AID)msg.getSender()).getLocalName()+" ... [OK]");
         try {
             
           BoatsPosition info = (BoatsPosition)msg.getContentObject();
           if (info instanceof BoatsPosition) {
             // Update position of boats
             ((CoordinatorAgent)sender).setBoatsPosition(info);
             
             // Run finite state automata
             ((CoordinatorAgent)sender).finiteStateAutomata();
           }
         } catch (Exception e) {
           showMessage("Incorrect content: "+e.toString());
         }
     }
     
     protected void handleNotUnderstood(ACLMessage msg) {
       showMessage("This message NOT UNDERSTOOD. \n");
     }
 
     protected void handleFailure(ACLMessage msg) {
       showMessage("The action has failed.");
 
     }
 
     protected void handleRefuse(ACLMessage msg) {
       showMessage("Action refused.");
     }
   }
 
   // ************************************
   // ** INITIATOR :: UPDATE MAP REQUEST
   // **   SENDER   -> THIS
   // **   RECEIVER -> CENTRAL AGENT
   // ************************************
   
   class UpdateRequestBehaviour extends AchieveREInitiator {
     private Agent      sender = null;
     private ACLMessage msgSent = null;
 
     public UpdateRequestBehaviour(Agent myAgent, ACLMessage requestMsg) {
       super(myAgent, requestMsg);
       showMessage("AchieveREInitiator starts...");
 
       sender = myAgent;
       msgSent = requestMsg;
     }
 
     protected void handleAgree(ACLMessage msg) {
       showMessage("AGREE received from "+ ( (AID)msg.getSender()).getLocalName());
     }
 
     protected void handleInform(ACLMessage msg) {
     	showMessage("INFORM received from "+ ( (AID)msg.getSender()).getLocalName()+" ... [OK]");
 
         // Update seafood
         // TODO : Handle seafood data
     }
 
     protected void handleNotUnderstood(ACLMessage msg) {
       showMessage("This message NOT UNDERSTOOD. \n");
     }
 
     protected void handleFailure(ACLMessage msg) {
       showMessage("The action has failed.");
 
     }
 
     protected void handleRefuse(ACLMessage msg) {
       showMessage("Action refused.");
     }
   }
 }
