 /**
  * Grid.java
  * 
  * Models a cartesian-based coordinate plane for the actors to interact within.
  * 
  * @author Agent team
  * 
  * Wheaton College, CSCI 335, Spring 2013
  */
 package edu.wheaton.simulator.datastructure;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import net.sourceforge.jeval.EvaluationException;
 import edu.wheaton.simulator.entity.Agent;
 import edu.wheaton.simulator.entity.AgentID;
 import edu.wheaton.simulator.entity.Entity;
 import edu.wheaton.simulator.simulation.SimulationPauseException;
 
 public class Grid extends Entity implements Iterable<Agent> {
 
 	/**
 	 * The grid of all Agents. This was implemented as a multi-dimensional
 	 * array, but now it uses a List of Lists, which is equivalent.
 	 */
 	private Agent[][] grid;
 
 	/**
 	 * Current update state
 	 */
 	private Updater updater = new LinearUpdater(this);
 
 	/**
 	 * Observers to watch the grid
 	 */
 	private Set<GridObserver> observers;
 
 	/**
 	 * Number of iterations performed
 	 */
 	private int step;
 
 	/**
 	 * Constructor. Creates a grid with the given width and height
 	 * specifications
 	 * 
 	 * @param width
 	 * @param height
 	 */
 	public Grid(int width, int height) {
 		super();
 		try {
 			addField("width", width + "");
 			addField("height", height + "");
 		} catch (ElementAlreadyContainedException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 
 		// Initialize the ArrayLists.
 		grid = new Agent[width][height];
 
 		updater = new LinearUpdater(this);
 		observers = new HashSet<GridObserver>();
 		step = 0;
 	}
 
 	/**
 	 * Hackish solution to Akon's gridrecorder. Should never ever be called
 	 * other than from the stats people.
 	 */
 	public static AgentID getID() {
 		AgentID id = new AgentID(-1);
 		return id;
 	}
 
 	/**
 	 * Changes the size of the grid
 	 * 
 	 * @param width
 	 * @param height
 	 */
 	public void resizeGrid(int width, int height) {
 		Agent[][] newGrid = new Agent[width][height];
 
 		int currentWidth = grid.length;
 		int currentHeight = 0;
 		if (currentWidth != 0) {
 			currentHeight = grid[0].length;
 		}
 
 		int minWidth = Math.min(width, currentWidth);
 		int minHeight = Math.min(height, currentHeight);
 
 		for (int i = 0; i < minWidth; i++) {
 			for (int j = 0; j < minHeight; j++) {
 				if (grid[i][j] != null) {
 					newGrid[i][j] = grid[i][j];
 				}
 			}
 		}
 
 		grid = newGrid;
 	}
 
 	/**
 	 * Provides this grid's width
 	 * 
 	 * @return
 	 */
 	public Integer getWidth() {
 		return getField("width").getIntValue();
 	}
 
 	/**
 	 * Provides this grid's height
 	 * 
 	 * @return
 	 */
 	public Integer getHeight() {
 		return getField("height").getIntValue();
 	}
 
 	/**
 	 * Returns true if the given space is empty, false otherwise. Also returns
 	 * false if invalid x, y values are given.
 	 * 
 	 * @param x
 	 * @param y
 	 * @return Whether or not the particular position is empty
 	 */
 	public boolean emptyPos(int x, int y) {
 		if (isValidCoord(x, y) && getAgent(x, y) == null)
 			return true;
 		return false;
 	}
 
 	/**
 	 * Checks whether the given x/y position is a valid coordinate (both larger
 	 * than 0 and smaller than width/height respectively)
 	 * 
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	public boolean isValidCoord(int x, int y) {
 		return (x >= 0) && (y >= 0) && x < getWidth() && y < getHeight();
 	}
 
 	/**
 	 * Causes all the triggers of all the entities in the grid to be fired
 	 * 
 	 * @throws SimulationPauseException
 	 */
 	public void updateEntities() throws SimulationPauseException {
 		updater.update();
 		step++;
 	}
 
 	/**
 	 * Provides the iteration number
 	 * 
 	 * @return the grid step
 	 */
 	public int getStep() {
 		return step;
 	}
 
 	/**
 	 * Makes updater a LinearUpdater
 	 */
 	public void setLinearUpdater() {
 		updater = new LinearUpdater(this);
 	}
 
 	/**
 	 * Makes updater a PriorityUpdater
 	 */
 	public void setPriorityUpdater(int minPriority, int maxPriority) {
 		updater = new PriorityUpdater(this, minPriority, maxPriority);
 	}
 
 	/**
 	 * makes updater an AtomicUpdater
 	 */
 	public void setAtomicUpdater() {
 		updater = new AtomicUpdater(this);
 	}
 
 	public String currentUpdater() {
 		return updater.toString();
 	}
 
 	/**
 	 * Returns the Agent at the given coordinates
 	 * 
 	 * @param x
 	 * @param y
 	 */
 	public Agent getAgent(int x, int y) {
 		if (isValidCoord(x, y))
 			return grid[x][y];
 		System.err.println("invalid Coord: " + x + "," + y);
 		return null;
 	}
 
 	/**
 	 * Places an Agent at the given coordinates. This method replaces (kills)
 	 * anything that is currently in that position. The Agent's own position is
 	 * also updated accordingly.
 	 * 
 	 * @param a
 	 * @param x
 	 * @param y
 	 * @return false if the x/y values were invalid
 	 */
 	public boolean addAgent(Agent a, int x, int y) {
 		if (isValidCoord(x, y)) {
 			grid[x][y] = a;
 			a.setPos(x, y);
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Places an Agent at a random position in the grid. This method replaces
 	 * (kills) anything that is currently in that position. The Agent's own
 	 * position is also updated accordingly.
 	 * 
 	 * @param a
 	 * @return returns true if successful
 	 */
 	public boolean addAgent(Agent a) {
 		int randomX = (int) (Math.random() * (getField("width").getIntValue() - 1));
 		int randomY = (int) (Math.random() * (getField("height").getIntValue() - 1));
 		grid[randomX][randomY] = a;
 		a.setPos(randomX, randomY);
 		return true;
 	}
 
 	/**
 	 * Removes an Agent at the given coordinates
 	 * 
 	 * @param x
 	 * @param y
 	 * @return false if invalid coordinates
 	 */
 	public boolean removeAgent(int x, int y) {
 		if (isValidCoord(x, y)) {
 			grid[x][y] = null;
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Adds an observer to the grid's list
 	 * 
 	 * @param ob
 	 */
 	public void addObserver(GridObserver ob) {
 		observers.add(ob);
 	}
 
 	/**
 	 * Notifies all of the observers watching this grid
 	 */
 	public void notifyObservers(boolean layerRunning) {
 		Grid copy = null;
 		Set<AgentAppearance> agentView = new HashSet<AgentAppearance>();
 
 		synchronized (this) {
 			copy = new Grid(getWidth(), getHeight());
 
 			// set grid fields
 			for (String current : getFieldMap().keySet()) {
 				try {
 					copy.addField(current, getFieldValue(current));
 				} catch (ElementAlreadyContainedException e) {
 				}
 			}
 			// add Agents
 			for (Agent current : this) {
 				copy.addAgent(current.clone(), current.getPosX(),
 						current.getPosY());
 				if (layerRunning)
 					try {
 						agentView.add(new AgentAppearance(current
 								.getLayerColor(), current.getDesign(), current
 								.getPosX(), current.getPosY()));
 					} catch (EvaluationException e) {
 						e.printStackTrace();
 					}
 				else
 					agentView.add(new AgentAppearance(current.getColor(),
 							current.getDesign(), current.getPosX(), current
 									.getPosY()));
 			}
 
 			copy.step = this.step;
 		}
 		for (GridObserver current : observers) {
 			current.update(copy);
 			current.update(agentView);
 		}
 	}
 
 	/**
 	 * Returns an iterator that goes through the Agents in the Grid
 	 * 
 	 * @return Iterator<Agent>
 	 */
 	@Override
 	public Iterator<Agent> iterator() {
 		return new Iterator<Agent>() {
 
 			int x = 0;
 			int y = 0;
 
 			@Override
 			public boolean hasNext() {
 				return y < getHeight();
 			}
 
 			@Override
 			public Agent next() {
 				Agent toReturn = getAgent(x, y);
 				if (x < getWidth() - 1) {
 					x++;
 				} else {
 					x = 0;
 					y++;
 				}
 				return toReturn;
 			}
 
 			@Override
 			public void remove() {
 				throw new UnsupportedOperationException();
 			}
 		};
 	}
 
 	/**
 	 * Provides all of the user created fields - thus doesn't include width and
 	 * height
 	 */
 	public Map<String, String> getCustomFieldMap() {
 		Map<String, String> toReturn = new HashMap<String, String>(fields);
 		toReturn.remove("width");
 		toReturn.remove("height");
 		return toReturn;
 	}
 
 }
