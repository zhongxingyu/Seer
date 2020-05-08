 package pathfinder;
 
 import java.awt.Dimension;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 import map.Cell;
 import map.Cell.Value;
 import map.Map;
 
 import uchicago.src.sim.engine.BasicAction;
 import uchicago.src.sim.engine.Schedule;
 import uchicago.src.sim.engine.SimInit;
 import uchicago.src.sim.engine.SimModelImpl;
 import uchicago.src.sim.gui.DisplaySurface;
 import uchicago.src.sim.gui.Object2DDisplay;
 import uchicago.src.sim.space.Object2DGrid;
 import uchicago.src.sim.util.SimUtilities;
 import agents.ArmyUnit;
 import agents.BasicAgent;
 import agents.Captain;
 import agents.Exit;
 import agents.Soldier;
 import agents.Robot;
 import agents.Wall;
 import astar.AStar;
 import astar.AStarNode;
 
 public class ArmyPathFinderModel extends SimModelImpl {
 	private static final char WALL = '#';
 	private static final char SOLDIER = 's';
 	private static final char CAPTAIN = 'c';
 	private static final char ROBOT = 'r';
 	private static final char EXIT = 'E';
 	private ArrayList<ArmyUnit> agentList;
 	private Schedule schedule;
 	private DisplaySurface dsurf;
 	private Object2DGrid space;
 	private int xSize;
 	private int ySize;
 	private Map map;
 
	private int live_robot, EMPTYWEIGHT, UNKOWNWEIGHT, DISPERSIONWEIGHT, communication_Range, sight_Range;
 
 	public ArmyPathFinderModel() {
 		this.live_robot = 100;
 		this.EMPTYWEIGHT = 1;
 		this.UNKOWNWEIGHT = 2;
 		this.DISPERSIONWEIGHT = 3;
 		this.communication_Range = 3;
		this.sight_Range = 4;
 		
 
 	}
 
 	public String getName() {
 		return "Color Picking Model";
 	}
 
 	public String[] getInitParam() {
		return new String[] { "live_robot", "EMPTYWEIGHT", "UNKOWNWEIGHT", "DISPERSIONWEIGHT", "communication_Range", "sight_Range" };
 	}
 
 	public Schedule getSchedule() {
 		return schedule;
 	}
 
 	public int getlive_robot() {
 		return live_robot;
 	}
 
 	public void setlive_robot(int live_robot) {
 		this.live_robot = live_robot;
 	}
 	
 	
 	public int getEMPTYWEIGHT() {
 		return EMPTYWEIGHT;
 	}
 
 	public void setEMPTYWEIGHT(int EMPTYWEIGHT) {
 		this.EMPTYWEIGHT = EMPTYWEIGHT;
 	}
 	
 	public int getUNKOWNWEIGHT() {
		return UNKOWNWEIGHT;
 	}
 
 	public void setUNKOWNWEIGHT(int UNKOWNWEIGHT) {
 		this.UNKOWNWEIGHT = UNKOWNWEIGHT;
 	}
 	
 	public int getDISPERSIONWEIGHT() {
		return DISPERSIONWEIGHT;
 	}
 
 	public void setDISPERSIONWEIGHT(int DISPERSIONWEIGHT) {
 		this.DISPERSIONWEIGHT = DISPERSIONWEIGHT;
 	}
 	
 	public int getcommunication_Range() {
 		return communication_Range;
 	}
 
 	public void setcommunication_Range(int communication_Range) {
 		this.communication_Range = communication_Range;
 	}
 	
	public int getsight_Range() {
		return sight_Range;
	}

	public void setsight_Range(int sight_Range) {
		this.sight_Range = sight_Range;
	}
	
 
 	public void setup() {
 		schedule = new Schedule();
 		if (dsurf != null)
 			dsurf.dispose();
 		dsurf = new DisplaySurface(this, "Army PathFinder");
 		dsurf.getBounds().width = 600;
 		dsurf.getBounds().height = 200;
 		registerDisplaySurface("Army PathFinder", dsurf);
 
 		/*
 		 * // property descriptors Vector<MovingMode> vMM = new
 		 * Vector<MovingMode>(); for(int i=0; i<MovingMode.values().length; i++)
 		 * { vMM.add(MovingMode.values()[i]); } descriptors.put("MovingMode",
 		 * new ListPropertyDescriptor("MovingMode", vMM));
 		 */
 
 	}
 
 	public void begin() {
 
 		buildModel();
 		buildDisplay();
 		buildSchedule();
 		/*
 		 * Cell init = new Cell(Value.Empty,3,1); Cell goal = new
 		 * Cell(Value.Exit,4,5);
 		 * 
 		 * LinkedList<AStarNode> path = AStar.run(init, goal, map);
 		 * if(path!=null) for (AStarNode n: path)
 		 * System.out.print(n.getCoords()+"->"); else
 		 * System.out.println("No path found");
 		 */
 	}
 
 	public void buildModel() {
 		agentList = new ArrayList<ArmyUnit>();
 		readMap("mapa3.txt");
 
 	
 	}
 
 	private void buildDisplay() {
 		// space and display surface
 		Object2DDisplay display = new Object2DDisplay(space);
 
 		dsurf.addDisplayableProbeable(display, "Agents Space");
 		dsurf.display();
 
 	}
 
 	private void buildSchedule() {
 		schedule.scheduleActionBeginning(0, new MainAction());
 		schedule.scheduleActionAtInterval(1, dsurf, "updateDisplay",
 				Schedule.LAST);
 		// schedule.scheduleActionAtInterval(1, plot, "step", Schedule.LAST);
 	}
 
 	class MainAction extends BasicAction {
 
 		public void execute() {
 
 			// shuffle agents
 			SimUtilities.shuffle(agentList);
 			System.out.println("Another it");
 			// iterate through all agents
 			for (int i = 0; i < agentList.size(); i++) {
 				ArmyUnit agent = (ArmyUnit) agentList.get(i);
 
 				
 				if (!agent.hasReachedExit()) {
 					agent.lookAround();
 					agent.move();		
 					agent.broadcastMap();
 					
 				
 				}
 				
 
 			}
 		}
 
 	}
 
 	public static void main(String[] args) {
 		SimInit init = new SimInit();
 		init.loadModel(new ArmyPathFinderModel(), null, false);
 
 	}
 
 	public void updateGlobalMap(Map map2,Value value) {
 		for (int i = 0; i < map.getY(); i++)
 			for (int j = 0; j < map.getX(); j++)
 				if (map.getPosition(j, i).getValue() == Value.Unknown) {
 					switch (map2.getPosition(j, i).getValue()) {
 					case Me:
 						map.setPosition(j, i, new Cell(value, j, i));
 						break;
 					default:
 						map.setPosition(j, i, map2.getPosition(j, i));
 						break;
 
 					}
 
 				} else if (map.getPosition(j, i).getValue() == Value.Empty) {
 					switch (map2.getPosition(j, i).getValue()) {
 					case Me:
 						map.setPosition(j, i, new Cell(value, j, i));
 						break;
 					case Visited:
 						map.setPosition(j, i, new Cell(Value.Visited, j, i));
 						break;
 					default:
 						break;
 
 					}
 
 				}
 		
 	}
 
 	public void readMap(String filename) {
 
 		try {
 			int x = 0;
 			FileInputStream fstream = new FileInputStream(filename);
 			FileInputStream fstream2 = new FileInputStream(filename);
 
 			DataInputStream in = new DataInputStream(fstream);
 			DataInputStream in2 = new DataInputStream(fstream2);
 
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));
 
 			String firstLine = br2.readLine();
 
 			x = firstLine.length();
 			int y = 1;
 			while ((firstLine = br2.readLine()) != null) {
 				if (firstLine.length() > x)
 					x = firstLine.length();
 				y++;
 			}
 
 			xSize = x;
 			ySize = y;
 			space = new Object2DGrid(xSize, ySize);
 			map = new Map(xSize, ySize);
 
 			int i = 0, j = 0;
 			String line;
 			while ((line = br.readLine()) != null) {
 				while (line.length() > j) {
 					switch (line.charAt(j)) {
 					case WALL:
 						Wall w = new Wall(j, i);
 
 						space.putObjectAt(j, i, w);
 						// map.setPosition(j, i, new Cell(Value.Wall,j,i));
 						break;
 					case SOLDIER:
 						Soldier s = new Soldier(j, i, space, communication_Range);
 						agentList.add(s);
 						space.putObjectAt(j, i, s);
 						map.setPosition(j, i, new Cell(Value.Soldier, j, i));
 						break;
 					case CAPTAIN:
 						Captain c = new Captain(j, i, space);
 						agentList.add(c);
 						space.putObjectAt(j, i, c);
 						map.setPosition(j, i, new Cell(Value.Captain, j, i));
 						break;
 					case ROBOT:
 						Robot r = new Robot(j, i, space, live_robot);
 						agentList.add(r);
 						space.putObjectAt(j, i, r);
 						map.setPosition(j, i, new Cell(Value.Robot, j, i));
 						break;
 					case EXIT:
 						Exit e = new Exit(j, i);
 						space.putObjectAt(j, i, e);
 						map.setPosition(j, i, new Cell(Value.Exit, j, i));
 						break;
 					default:
 						map.setPosition(j, i, new Cell(Value.Unknown, j, i));
 						break;
 
 					}
 					j++;
 
 				}
 
 				j = 0;
 				i++;
 			}
 
 			in.close();
 			in2.close();
 		} catch (Exception e) {
 			System.err.println("Error: " + e.getMessage());
 		}
 	}
 
 }
