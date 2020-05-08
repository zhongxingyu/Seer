 package smartpod;
 
 import com.janezfeldin.Display.ImageWindow;
 import com.janezfeldin.Math.Point;
 import jade.core.Agent;
 import jade.core.behaviours.CyclicBehaviour;
 import jade.wrapper.AgentController;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.util.ArrayList;
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
 	private ArrayList<PodAgent>				podList			= new ArrayList<PodAgent>();
 	private ArrayList<StationNodeAgent>		stationList		= new ArrayList<StationNodeAgent>();
 	private ArrayList<JunctionNodeAgent>	junctionList	= new ArrayList<JunctionNodeAgent>();
 	private ArrayList<NodeAgent>			nodeList		= new ArrayList<NodeAgent>();
 	private ArrayList<RoadAgent>			roadList		= new ArrayList<RoadAgent>();
 
 	// path finding agent
	private PathFindingAgent pathFndingAgent = new PathFindingAgent();
 
 	private ImageWindow window = new ImageWindow();
 	private BufferedImage image;
 	//Environment default settings
 	private int mapWidth = 500;
 	private int mapHeight = 500;
 	private String roadBelongingType = "inbound";
 	
 	/**
 	 * This method gets called when agent is started. It loads all the settings
 	 * from conf.xml file and starts necessary agents. It also adds the desired
 	 * behvaiour to the agent.
 	 */
 	@Override
 	protected void setup()
 	{
 		//odpre okno animacije
 		window.setVisible(true);
 
 		//Loading of conf.xml file and start of all other agents, this agent is started from argument when starting application or manually from GUI.
 		try
 		{
 			// path finding agent
 			((AgentController) getContainerController().acceptNewAgent("mainPathFindingAgent", pathFndingAgent)).start();
 
 			
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
 			roadBelongingType = temp.getElementsByTagName("roadBelongingType").item(0).getTextContent();
 
 			//settings for junctions
 			tempList = doc.getElementsByTagName("Junction");
 			for (int i = 0; i < tempList.getLength(); i++)
 			{
 				temp = (Element) tempList.item(i);
 				Point tempPoint = new Point(Integer.parseInt(temp.getElementsByTagName("x").item(0).getTextContent()), Integer.parseInt(temp.getElementsByTagName("y").item(0).getTextContent()));
 				JunctionNodeAgent tempAgent = new JunctionNodeAgent(tempPoint);
 				((AgentController) getContainerController().acceptNewAgent(temp.getElementsByTagName("name").item(0).getTextContent(), tempAgent)).start();
                 tempAgent.pathFindingAgent = pathFndingAgent.getAID();
 				junctionList.add(tempAgent);
 			}
 
 			//settings for stations
 			tempList = doc.getElementsByTagName("Station");
 			for (int i = 0; i < tempList.getLength(); i++)
 			{
 				temp = (Element) tempList.item(i);
 				Point tempPoint = new Point(Integer.parseInt(temp.getElementsByTagName("x").item(0).getTextContent()), Integer.parseInt(temp.getElementsByTagName("y").item(0).getTextContent()));
 				StationNodeAgent tempAgent = new StationNodeAgent(tempPoint, Integer.parseInt(temp.getElementsByTagName("podCapacity").item(0).getTextContent()), Integer.parseInt(temp.getElementsByTagName("peopleCapacity").item(0).getTextContent()));
 				((AgentController) getContainerController().acceptNewAgent(temp.getElementsByTagName("name").item(0).getTextContent(), tempAgent)).start();
                 tempAgent.pathFindingAgent = pathFndingAgent.getAID();
 				stationList.add(tempAgent);
 			}
 
 			//settings for roads
 			tempList = doc.getElementsByTagName("Road");
 			for (int i = 0; i < tempList.getLength(); i++)
 			{
 				temp = (Element) tempList.item(i);
 				String tempStart = temp.getElementsByTagName("start").item(0).getTextContent();
 				String tempEnd = temp.getElementsByTagName("end").item(0).getTextContent();
 				RoadAgent tempAgent = new RoadAgent(tempStart, tempEnd, getNodesPosition(tempStart), getNodesPosition(tempEnd), roadBelongingType);
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
 
 				PodAgent tempAgent = new PodAgent(getNodesPosition(temp.getElementsByTagName("startPosition").item(0).getTextContent()), Integer.parseInt(tempPodsCapacity[j]), 0);
 
 				((AgentController) getContainerController().acceptNewAgent("Pod" + (i + 1), tempAgent)).start();
 				podList.add(tempAgent);
 
 				j++;
 			}
 			
 			// load path finding agent map
 			nodeList.addAll(stationList);
 			nodeList.addAll(junctionList);
 			pathFndingAgent.loadMap(nodeList,roadList);
 		}
 		catch (Exception ex)
 		{
 			System.out.println(ex.toString());
 		}
 
 		
 		//sending starting positions to SmartPodDisplay program
 		DisplayDataBridge.sendInitialMessage(mapWidth,mapHeight,podList, stationList, junctionList, roadList);
 
 		//settings for graphical representation (setting up the image)
 		image = new BufferedImage(mapWidth, mapHeight, BufferedImage.TYPE_INT_RGB);
 
 
 		//Adding of the Behaviour to the EnvironmentAgent
 		addBehaviour(new EnvironmentAgent.EnvironmentAgentBehaviour(this));
 	}
 
 	
 	/**
 	 * Method used for getting the Node's position.
 	 * 
 	 * @param name is the name of the desired node's position.
 	 * @return New point that represents the position of the node.
 	 */
 	private Point getNodesPosition(String name)
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
 
 	/**
 	 * Behaviour class for EnvironmentAgent. It extends CyclicBehaviour.
 	 */
 	public class EnvironmentAgentBehaviour extends CyclicBehaviour
 	{
 		/**
 		 * Constructor for environment's agent behaviour class.
 		 *
 		 * @param a the agent to which behaviour is being applied.
 		 */
 		public EnvironmentAgentBehaviour(Agent a)
 		{
 			super(a);
 		}
 
 		/**
 		 * Method that performs actions in EnvironmentAgentBehaviour class.
 		 * It gets called each time Jade platform has spare resources.
 		 */
 		@Override
 		public void action()
 		{
 			//draws empty background for graphic's representation.
 			drawBackground();
 			
 			//Communication with simulation display
 //			try
 //			{
 //				DisplayDataBridge.sendMessage("message number ");
 //			}
 //			catch (IOException ex)
 //			{
 //				Logger.getLogger(EnvironmentAgent.class.getName()).log(Level.SEVERE, null, ex);
 //			}
 
 
 
 
 			//Graphical representation of agents
 			Graphics g = image.getGraphics();
 			//representation of pods
 			g.setColor(Color.blue);
 			for (int i = 0; i < podList.size(); i++)
 			{
 				Point tempPosition = podList.get(i).getPosition();
 				g.fillRoundRect((int)tempPosition.x - 2, (int)tempPosition.y - 2, 3, 3, 3, 3);
 			}
 			//representations of stations
 			g.setColor(Color.black);
 			for (int i = 0; i < stationList.size(); i++)
 			{
 				Point tempPosition = stationList.get(i).getPosition();
 				g.fillRect((int)tempPosition.x - 8, (int)tempPosition.y - 8, 15, 15);
 			}
 			//representation of junctions
 			g.setColor(Color.gray);
 			for (int i = 0; i < junctionList.size(); i++)
 			{
 				Point tempPosition = junctionList.get(i).getPosition();
 				g.fillRect((int)tempPosition.x - 4, (int)tempPosition.y - 4, 7, 7);
 			}
 			//representation of roads
 			g.setColor(Color.green);
 			for (int i = 0; i < roadList.size(); i++)
 			{
 				Point p1 = roadList.get(i).getStartPosition();
 				Point p2 = roadList.get(i).getEndPosition();
                 g.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
 			}
 
 
 
 
 			//displays the image of the current state.
 			window.showImage(image);
 
 
 //			sleep();
 		}
 
 		/**
 		 * Method, used for clearing the graphics and creating empty background.
 		 */
 		private void drawBackground()
 		{
 			//drawing the empty background
 			Graphics g = image.getGraphics();
 			g.setColor(Color.white);
 			g.fillRect(0, 0, image.getWidth(), image.getHeight());
 		}
 
 		/**
 		 * Method, used to create delay at the end of each environment agent's cycle.
 		 */
 		private void sleep()
 		{
 			try
 			{
 				Thread.sleep(100);
 			}
 			catch (InterruptedException e)
 			{
 				System.out.println(e.toString());
 			}
 		}
 	}
 }
