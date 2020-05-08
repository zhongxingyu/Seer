 package linewars.display.panels;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.JList;
 import javax.swing.JScrollPane;
 
 import linewars.display.Animation;
 import linewars.display.Display;
 import linewars.gameLogic.GameStateProvider;
 import linewars.gamestate.Node;
 import linewars.gamestate.Player;
 import linewars.gamestate.mapItems.Unit;
 import menu.components.CustomList;
 import menu.components.MenuScrollPane;
 
 /**
  * Encapsulates the information needed to display Node status information.
  * 
  * @author Titus Klinge
  * @author Ryan Tew
  * 
  */
 @SuppressWarnings("serial")
 public class NodeStatusPanel extends Panel
 {
 	/**
 	 * The ratio of the width of this panel to the with of the main display
 	 */
 	private static final double ASPECT_RATIO = 0.2;
 
 	/**
 	 * The default height and width of the panel
 	 */
 	private static final int DEFAULT_WIDTH = 600;
 	private static final int DEFAULT_HEIGHT = 500;
 
 	/**
 	 * The location of the command button panel within the command card
 	 */
 	private static final int STATUS_PANEL_X = 1;
 	private static final int STATUS_PANEL_Y = 61;
 
 	/**
 	 * The height and width of the command button panel
 	 */
 	private static final int STATUS_PANEL_WIDTH = 574;
 	private static final int STATUS_PANEL_HEIGHT = 439;
 
 	private Display display;
 	private JList nodeStatus;
 	private JScrollPane scrollPane;
 
 	/**
 	 * Creates this Node Status display.
 	 * 
 	 * @param display
 	 *            The display this will be drawn on.
 	 * @param stateManager
 	 *            The gamestate manager for this instance of the game.
 	 * @param anims
 	 *            The list of animations for this panel.
 	 */
 	public NodeStatusPanel(Display display, GameStateProvider stateManager, Animation... anims)
 	{
 		super(stateManager, DEFAULT_WIDTH, DEFAULT_HEIGHT, anims);
 
 		setOpaque(false);
 		
 		this.display = display;
 		this.nodeStatus = new CustomList();
 		this.scrollPane = new MenuScrollPane();
 		scrollPane.setViewportView(nodeStatus);
 		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 
 		add(scrollPane);
 	}
 
 	@Override
 	public void updateLocation()
 	{
 		scaleFactor = (display.getScreenWidth() * ASPECT_RATIO) / DEFAULT_WIDTH;
 
 		super.updateLocation();
 
 		setLocation(0, getParent().getHeight() - getHeight());
 
 		// resizes the inner panel
 		scrollPane.setLocation((int)(STATUS_PANEL_X * scaleFactor), (int)(STATUS_PANEL_Y * scaleFactor));
 		scrollPane.setSize((int)(STATUS_PANEL_WIDTH * scaleFactor), (int)(STATUS_PANEL_HEIGHT * scaleFactor));
 	}
 
 	/**
 	 * Populates the NodeStatusPanel with information about the selected node.
 	 * 
 	 * @param node
 	 *            The selected node.
 	 * @param curTime
 	 *            The current games time in seconds.
 	 */
 	public void updateNodeStatus(Node node, double curTime)
 	{
 		ArrayList<String> status = new ArrayList<String>();
 
 		double timeToNextSpawn = node.getSpawnTime() - (curTime - node.getLastSpawnTime());
 		double timeToCapture = node.getCaptureTime() - (curTime - node.getOccupationStartTime());
 		Player owner = node.getOwner();
 		Player invader = node.getInvader();
 
 		Unit[] units = node.getContainedUnits();
 		Map<Player, Map<String, Integer>> playerToUnits = new HashMap<Player, Map<String, Integer>>();
 		for(Unit u : units)
 		{
 			Player unitOwner = u.getOwner();
 			String unitType = u.getName();
 
 			Map<String, Integer> unitTypeToNumber = playerToUnits.get(unitOwner);
 			if(unitTypeToNumber == null)
 			{
 				unitTypeToNumber = new HashMap<String, Integer>();
 				playerToUnits.put(unitOwner, unitTypeToNumber);
 			}
 
 			Integer numUnits = unitTypeToNumber.get(unitType);
 			if(numUnits == null)
 			{
 				numUnits = 0;
 			}
 
 			unitTypeToNumber.put(unitType, numUnits + 1);
 		}
 
 		status.add("time to next spawn: " + (int)timeToNextSpawn);
 
 		if(invader != null)
 			status.add("time left to capture: " + (int)timeToCapture);
 
 		if(owner != null)
 			status.add("owner: " + owner.getPlayerName());
 
 		if(invader != null)
 			status.add("invader: " + invader.getPlayerName());
 
 		for(Player p : playerToUnits.keySet())
 		{
 			status.add(p.getPlayerName() + "'s units:");
 
 			Map<String, Integer> unitTypeToNumber = playerToUnits.get(p);
 			for(String unitType : unitTypeToNumber.keySet())
 			{
 				status.add("  " + unitType + " x" + unitTypeToNumber.get(unitType));
 			}
 		}
 
 		nodeStatus.setListData(status.toArray());
 	}
 }
