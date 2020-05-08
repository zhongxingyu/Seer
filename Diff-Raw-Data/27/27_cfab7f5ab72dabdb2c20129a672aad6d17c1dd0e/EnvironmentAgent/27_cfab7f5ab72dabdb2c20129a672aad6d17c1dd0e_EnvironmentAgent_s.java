 package smartpod;
 
 import com.janezfeldin.Math.Vector2D;
 import jade.core.AID;
 import jade.core.Agent;
 import jade.core.behaviours.TickerBehaviour;
 import jade.wrapper.AgentController;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 /**
  * Class for creating environment agent. It extends SPAgent.
  *
  * @author Janez Feldin
  */
 public class EnvironmentAgent extends SPAgent
 {
 	// agent communicator
 
 	private EnvironmentCommunicator communicator = new EnvironmentCommunicator(this);
 	//Variable declaration for storing lists of agents.
 	private ArrayList<PodAgent> podList = new ArrayList<PodAgent>();
 	private ArrayList<StationNodeAgent> stationList = new ArrayList<StationNodeAgent>();
 	private ArrayList<JunctionNodeAgent> junctionList = new ArrayList<JunctionNodeAgent>();
 	private ArrayList<NodeAgent> nodeList = new ArrayList<NodeAgent>();
 	private ArrayList<RoadAgent> roadList = new ArrayList<RoadAgent>();
 	private List<AID> nodeAIDs = new ArrayList<AID>();
 	// path finding agent
 	private PathFindingAgent pathFndingAgent = new PathFindingAgent();
 	//Environment default settings
 	private int mapWidth = 500;
 	private int mapHeight = 500;
 
 	/**
 	 * This method gets called when agent is started. It loads all the settings
 	 * from conf.xml file and starts necessary agents. It also adds the desired
 	 * behaviour to the agent.
 	 */
 	@Override
 	protected void setup()
 	{
 		//Loading of conf.xml file and start of all other agents, this agent is started from argument when starting application or manually from GUI.
 		try
 		{
 			// run DisplayHelper
 //			Runtime r = Runtime.getRuntime();
 //			Process p = r.exec("java -jar SmartPodDisplay.jar");
 //			Thread.sleep(3000);
 
 			// path finding agent
 			((AgentController) getContainerController().acceptNewAgent("PathFinder", pathFndingAgent)).start();
 
 
 			File xmlFile = new File("conf.xml");
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 			Document doc = dBuilder.parse(xmlFile);
 
 			doc.getDocumentElement().normalize();
 
 			//settings for Environment agent
 			Element temp;
 			NodeList tempList;
 
 			temp = (Element) doc.getElementsByTagName("Environment").item(0);
 			mapWidth = Integer.parseInt(temp.getElementsByTagName("width").item(0).getTextContent());
 			mapHeight = Integer.parseInt(temp.getElementsByTagName("height").item(0).getTextContent());
 
 			//settings for junctions
 			tempList = doc.getElementsByTagName("Junction");
 			for (int i = 0; i < tempList.getLength(); i++)
 			{
 				temp = (Element) tempList.item(i);
 				Vector2D tVec = new Vector2D(temp.getElementsByTagName("position").item(0).getTextContent());
 				JunctionNodeAgent tempAgent = new JunctionNodeAgent(tVec, Integer.parseInt(temp.getElementsByTagName("podCapacity").item(0).getTextContent()));
 				((AgentController) getContainerController().acceptNewAgent(temp.getElementsByTagName("name").item(0).getTextContent(), tempAgent)).start();
 				tempAgent.pathFindingAgent = pathFndingAgent.getAID();
 				junctionList.add(tempAgent);
 			}
 
 			//settings for stations
 			tempList = doc.getElementsByTagName("Station");
 			for (int i = 0; i < tempList.getLength(); i++)
 			{
 				temp = (Element) tempList.item(i);
 				Vector2D tVec = new Vector2D(temp.getElementsByTagName("position").item(0).getTextContent());
 				StationNodeAgent tempAgent = new StationNodeAgent(tVec, Integer.parseInt(temp.getElementsByTagName("podCapacity").item(0).getTextContent()), Integer.parseInt(temp.getElementsByTagName("peopleCapacity").item(0).getTextContent()));
 				((AgentController) getContainerController().acceptNewAgent(temp.getElementsByTagName("name").item(0).getTextContent(), tempAgent)).start();
 				tempAgent.pathFindingAgent = pathFndingAgent.getAID();
 				tempAgent.setStationList(nodeAIDs);
 				stationList.add(tempAgent);
 				nodeAIDs.add(tempAgent.getAID());
 			}
 
 			//settings for roads
 			tempList = doc.getElementsByTagName("Road");
 			for (int i = 0; i < tempList.getLength(); i++)
 			{
 				temp = (Element) tempList.item(i);
 				String tempStart = temp.getElementsByTagName("start").item(0).getTextContent();
 				String tempEnd = temp.getElementsByTagName("end").item(0).getTextContent();
 				double tempWeight = Double.parseDouble(temp.getElementsByTagName("weight").item(0).getTextContent());
 				RoadAgent tempAgent = new RoadAgent(tempStart, tempEnd, getNodesPosition(tempStart), getNodesPosition(tempEnd), tempWeight);
 				((AgentController) getContainerController().acceptNewAgent(temp.getElementsByTagName("name").item(0).getTextContent(), tempAgent)).start();
 				tempAgent.weightUpdateDelegate = pathFndingAgent.getAID();
 				roadList.add(tempAgent);
 			}
 
 			//settings for pods
 			temp = (Element) doc.getElementsByTagName("Pods").item(0);
 			String[] tempPodsCapacity = temp.getElementsByTagName("capacity").item(0).getTextContent().split(",");
 			int numPods = Integer.parseInt(temp.getElementsByTagName("numPods").item(0).getTextContent());
 			int j = 0;
 
 			for (int i = 0; i < numPods; i++)
 			{
 				if (j >= tempPodsCapacity.length)
 				{
 					j--;
 				}
 
 				String startingNode = temp.getElementsByTagName("startPosition").item(0).getTextContent();
 				PodAgent tempAgent = new PodAgent(startingNode, getNodesPosition(startingNode), Integer.parseInt(tempPodsCapacity[j]), 0);
 				((AgentController) getContainerController().acceptNewAgent("Pod" + (i + 1), tempAgent)).start();
 				podList.add(tempAgent);
 
 				j++;
 			}
 
 			// load path finding agent map
 			nodeList.addAll(stationList);
 			nodeList.addAll(junctionList);
 			pathFndingAgent.loadMap(nodeList, roadList);
 
 			// testing moving of pod agents
 			
 			CrowdControlAgent crowdGenerator = new CrowdControlAgent();
 			((AgentController) getContainerController().acceptNewAgent("CrowdGenerator", crowdGenerator)).start();
 
 
 //			AID garageAID = new AID("Postaja1", false);
 //			AID destinationAID = new AID("Postaja2", false);
 //			for (int i = 0; i < 5; i++)
 //			{
 //				PassengerGroupAgent testAgent2 = new PassengerGroupAgent(garageAID, destinationAID, 5000+i*500);
 //				((AgentController) getContainerController().acceptNewAgent("Janez"+i, testAgent2)).start();
 //			}
 
 
 
 

			AID garageAID = new AID("Garaža1",false);
			AID destinationAID = new AID("Postaja5",false);
			for (int i = 0; i < 30; i++)
			{
//				AID destinationAID = new AID("Postaja"+(i%6+1),false);
				PassengerGroupAgent testAgent1 = new PassengerGroupAgent(garageAID, destinationAID, 3000+i*500);
				((AgentController) getContainerController().acceptNewAgent("Janez"+i, testAgent1)).start();
			}
 
 //			PassengerGroupAgent testAgent1 = new PassengerGroupAgent(garageAID, destinationAID, 100000);
 //			((AgentController) getContainerController().acceptNewAgent("Janez1234", testAgent1)).start();
 //			
 //			PassengerGroupAgent testAgent2 = new PassengerGroupAgent(garageAID, destinationAID, 200000);
 //			((AgentController) getContainerController().acceptNewAgent("Janez12345", testAgent2)).start();
 
 
 		}
 		catch (Exception ex)
 		{
 			System.err.println(ex.toString());
 		}
 
 
 		// send starting positions to SmartPodDisplay program
 		DisplayDataBridge.sendInitialMessage(mapWidth, mapHeight, podList, stationList, junctionList, roadList,getCurrentDate());
 
 		// add ticker behaviour for updating SmartPodDisplay on 1ms
 		addBehaviour(new EnvironmentAgent.UpdateDisplayDataBridge(this, 50));
 	}
 
 	/**
 	 * Behaviour class for updating SmartPodDisplay. It extends TickerBehaviour.
 	 */
 	public class UpdateDisplayDataBridge extends TickerBehaviour
 	{
 
 		/**
 		 * Constructor for environment's agent behaviour class.
 		 *
 		 * @param a the agent to which behaviour is being applied.
 		 * @param period the tick period in ms
 		 */
 		public UpdateDisplayDataBridge(Agent a, long period)
 		{
 			super(a, period);
 		}
 
 		/**
 		 * This method is invoked periodically with the period defined in the
 		 * constructor.
 		 */
 		@Override
 		public void onTick()
 		{
 			//sends data to SmartPodDisplay
 			DisplayDataBridge.sendUpdateMessage(podList, stationList, junctionList,getCurrentDate());
 		}
 	}
 
 	/**
 	 * Method used for getting the Node's position.
 	 *
 	 * @param name is the name of the desired node's position.
 	 *
 	 * @return New Vector2D that represents the position of the node.
 	 */
 	private Vector2D getNodesPosition(String name)
 	{
 		//searches for the node with specified name between stations
 		for (int i = 0; i < stationList.size(); i++)
 		{
 			if (stationList.get(i).getLocalName().equals(name))
 			{
 				return stationList.get(i).getPosition();
 			}
 		}
 
 		//searches for the node with specified name between junctions
 		for (int i = 0; i < junctionList.size(); i++)
 		{
 			if (junctionList.get(i).getLocalName().equals(name))
 			{
 				return junctionList.get(i).getPosition();
 			}
 		}
 		//return null result if the Node with specified name wasn't found.
 		return null;
 	}
 }
