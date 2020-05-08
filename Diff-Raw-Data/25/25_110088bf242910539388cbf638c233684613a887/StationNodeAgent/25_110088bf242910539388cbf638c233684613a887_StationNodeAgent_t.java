 package smartpod;
 
 import com.janezfeldin.Math.Vector2D;
 import jade.core.AID;
 import jade.core.Agent;
 import jade.core.behaviours.CyclicBehaviour;
 import jade.lang.acl.ACLMessage;
 import java.util.ArrayList;
 
 /**
  * Class for creating StationAgent. It extends NodeAgent.
  *
  * @author Janez Feldin
  */
 public class StationNodeAgent extends NodeAgent
 {
 	// agent communicator
 	private NodeCommunicator communicator = new NodeCommunicator(this);
 	
 	//variable declaration for all the agent's properties
     private int podsCapacity;
     private int peopleCapacity;
     private int peopleOnStation;
 
 	/**
 	 * Method that returns the integer value representing the maximum number of pods, that can be on this station at a given moment.
 	 * @return integer value representing stations pod capacity
 	 */
     public int getPodsCapacity()
     {
         return podsCapacity;
     }
 
 	/**
 	 * Method used to set the maximum number of pods, that can be on this station at a given moment.
 	 * @param podsCapacity integer value representing the maximum number of pods.
 	 */
     public void setPodsCapacity(int podsCapacity)
     {
         this.podsCapacity = podsCapacity;
     }
 
 	/**
 	 * Method that returns the maximum number of people, that can be on this station at a given moment.
 	 * @return integer value representing the maximum number of people
 	 */
     public int getPeopleCapacity()
     {
         return peopleCapacity;
     }
 
 	/**
 	 * Method used to set the maximum number of people, that can be on this station at a given moment.
 	 * @param peopleCapacity integer value representing the maximum number of people
 	 */
     public void setPeopleCapacity(int peopleCapacity)
     {
         this.peopleCapacity = peopleCapacity;
     }
 
 	/**
 	 * Method that returns the current number of people on this station.
 	 * @return current number of people waiting on this station
 	 */
     public int getPeopleOnStation()
     {
         return peopleOnStation;
     }
 	
 	/**
 	 * Method used to set the current number of people waiting on this station.
 	 * @param peopleOnStation number of people, that are currently waiting on the station.
 	 */
     public void setPeopleOnStation(int peopleOnStation)
     {
         this.peopleOnStation = peopleOnStation;
     }
 
 	/**
 	 * Method used for adding people to the station.
 	 * @param n integer value representing the number of people to be added, if the station has the sufficient capacity
 	 * @return true if the adding operation succeeded or false if it failed.
 	 */
     public boolean addPeopleToStation(int n)
     {
         if (peopleOnStation + n <= peopleCapacity)
         {
             peopleOnStation += n;
             return true;
         }
         else
         {
             return false;
         }
     }
 
 	/**
 	 * Method used for removing people off the station.
 	 * @param n integer value representing the number of people to be removed, if the station has enough people on it.
 	 * @return true if the adding operation succeeded or  false if it failed
 	 */
     public boolean removePeopleFromStation(int n)
     {
         if (peopleOnStation - n >= 0)
         {
             peopleOnStation -= n;
             return true;
         }
         else
         {
             return false;
         }
     }
 	
 	/**
 	 * Constructor method for the StationNodeAgent.
 	 * @param position Vector2D containing the position of the station
 	 * @param podsCapacity maximum allowed pods on the station at a given moment
 	 * @param peopleCapacity maximum allowed people waiting on the station at a given moment
 	 */
     public StationNodeAgent(Vector2D position, int podsCapacity, int peopleCapacity)
     {
         super(position);
         this.podsCapacity = podsCapacity;
         this.peopleCapacity = peopleCapacity;
     }
 
 	/**
 	 * This method gets called when agent is started.
 	 * It adds the desired behaviour to the agent.
 	 */
     @Override
     protected void setup()
     {
 		//adds the desired behaviour to the agent
         addBehaviour(new StationAgentBehaviour(this));
     }
 
 	/**
 	 * Behaviour class for StationAgent.
 	 * It extends CyclicBehaviour.
 	 */
     public class StationAgentBehaviour extends CyclicBehaviour
     {
 		/**
 		 * Constructor for Station's agent behaviour class.
 		 * @param a the agent to which behaviour is being applied.
 		 */
         public StationAgentBehaviour(Agent a)
         {
             super(a);
         }
 		
 		/**
 		 * Method that performs actions in StationAgentBehaviour class.
 		 * It gets called each time Jade platform has spare resources.
 		 */
         @Override
         public void action()
         {
 			// check departure message box
 			ArrayList<ACLMessage> departureMessages = communicator.checkPodDepartureMessages();
 			for (ACLMessage msg : departureMessages)
 			{
 				System.out.println("com-node : "+msg.getContent());
 				
 				registeredPods.remove(msg.getSender());
 			}
 			
 			// check arrival message box
 			ArrayList<ACLMessage> arrivalMessages = communicator.checkPodArrivalRequestMessages();
 			for (ACLMessage msg : arrivalMessages)
 			{
 				System.out.println("com-node : "+msg.getContent());
 								
 				communicator.confirmPodToNodeArrival(msg);
 				
 				registeredPods.add(msg.getSender());
 
 				// check whether final destination has been reached
 				String destination = msg.getUserDefinedParameter("destination");
 				if (destination.equals(getLocalName()))
 				{
 					// the pod has reached the final destination
 				}
 				else
 				{
 					communicator.requestPathFinding(msg.getSender().getLocalName(), destination);
 				}
 				
 			}
 			
 			// check path finding result message box
 			ArrayList<ACLMessage> pathFinding = communicator.checkPathFindingResultMessages();
 			for (ACLMessage msg : pathFinding)
 			{
 				System.out.println("com-node : "+msg.getContent());
 				
 				AID podAID = getAgentByName(msg.getUserDefinedParameter("pod")).getName();
 				AID roadAID = getAgentByName(msg.getUserDefinedParameter("road_to_take")).getName();
 				communicator.proposePodToRoadDeparture(podAID, roadAID);
 			}
 			
 			// check other messages
 			ArrayList<ACLMessage> messages = communicator.checkMessageBox(null);
 			for (ACLMessage msg : messages)
 			{
 				System.out.println("com-node : "+msg.getContent());
 			}
			
			//testing moving of pod agents
			if ( getCurrentTime() >5000 && !pathFindingRequestSent && getLocalName().equals("Postaja1"))
			{
				pathFindingRequestSent = true;
				System.out.println("Test");
				communicator.requestPathFinding("Pod1", "Postaja2");
			}
         }
		private boolean pathFindingRequestSent = false;
     }
 }
