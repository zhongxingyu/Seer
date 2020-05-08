 package smartpod;
 
 import jade.core.Agent;
 import jade.core.behaviours.CyclicBehaviour;
 import jade.lang.acl.ACLMessage;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * The agent responsible for path finding.
  * 
  * @author Andreas
  */
 public class PathFindingAgent extends SPAgent
 {
 	// agent communicator
 	private PathFindingCommunicator communicator = new PathFindingCommunicator(this);
 	
 	private Map<String,Double> weightMap	= new HashMap<String,Double>();
 	private Map<String,String> endNodeMap	= new HashMap<String,String>();
 	private Map<String,ArrayList<String>> roadMap = new HashMap<String,ArrayList<String>>();
 			
 	/**
	 * Constructor for path finding agent.
 	 * @param nodeList the ArrayList containing NodeAgents.
 	 * @param roadList the ArrayList containing RoadAgents.
 	 */
    public PathFindingAgent(ArrayList<NodeAgent> nodeList, ArrayList<RoadAgent> roadList)
    {
 		for (NodeAgent node : nodeList)
 		{
 			roadMap.put(node.getLocalName(), new ArrayList<String>());
 		}
 		for (RoadAgent road : roadList)
 		{
 			String roadName = road.getLocalName();
 			weightMap.put(roadName, 0.5);
 			endNodeMap.put(roadName, road.endNode);
 
 			ArrayList<String> roads = roadMap.get(road.startNode);
 			roads.add(roadName);
 		}
    }
     
 	/**
 	 * This method gets called when agent is started.
 	 * It adds the desired behaviour to the agent.
 	 */
     @Override
     protected void setup()
     {
         //adds the behviour to the agent
         addBehaviour(new PathFindingAgent.PathFindingAgentBehaviour(this));
     }
     
     
     /**
 	 * Behaviour class for PathFindingAgent.
 	 * It extends CyclicBehaviour.
 	 */
     public class PathFindingAgentBehaviour extends CyclicBehaviour
     {
 		/**
 		 * Constructor for PathDindingAgent's behaviour class.
 		 * @param a the agent to which behaviour is being applied.
 		 */
         public PathFindingAgentBehaviour(Agent a)
         {
             super(a);
         }
 
 		/**
 		 * Method that performs actions in PathFindingAgentBehaviour class.
 		 * It gets called each time Jade platform has spare resources.
 		 */
         @Override
         public void action()
         {
 			// check road weight update messages
 			ArrayList<ACLMessage> weightMessages = communicator.checkRoadWeightUpdates();
 			for (ACLMessage msg : weightMessages)
 			{
 				System.out.println("com-env : "+msg.getContent());
 			}
 			
 			// check path finding request messages
 			ArrayList<ACLMessage> pathFindingMessages = communicator.checkPathFindingRequests();
 			for (ACLMessage msg : pathFindingMessages)
 			{
 				System.out.println("com-env : "+msg.getContent());
 				
 				// find path
 				communicator.informPathFindingResult(msg, "dummyRoad");
 			}
 			
 			// check remaining messages
 			ArrayList<ACLMessage> messages = communicator.checkMessageBox(null);
 			for (ACLMessage msg : messages)
 			{
 				System.out.println("com-env : "+msg.getContent());
 			}
         }
     }
 }
