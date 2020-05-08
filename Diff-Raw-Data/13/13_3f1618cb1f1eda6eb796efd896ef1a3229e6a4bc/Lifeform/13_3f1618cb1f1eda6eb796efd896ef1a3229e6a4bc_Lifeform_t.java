 package projectubernahme.lifeforms;
 
 import java.awt.event.KeyEvent;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 
 import projectubernahme.IngestionThread;
 import projectubernahme.Player;
 import projectubernahme.TakeoverThread;
 import projectubernahme.gfx.ConvertedGraphics;
 import projectubernahme.simulator.MainSimulator;
 
 
 /** something with its own mind in the game, can interact with other lifeforms and the world */
 abstract public class Lifeform {	
 	/** the simulator this lifeform is connected to */
 	MainSimulator sim;
 
 	public Lifeform (MainSimulator sim) {
 		this.sim = sim;
 		name = new String();
 		id = next_id++;
 	}
 	
 	/*
 	 * the AI
 	 */
 
 	/** this lets the lifeform act, this can be just sitting around or calling for support or attacking another lifeform */
 	abstract public void act(int sleepTime);
 	
 	/*
 	 * everything that has to do with movement
 	 * - position
 	 * - velocity
 	 * - rotation
 	 * - distance
 	 */
 
 	/** coordinates */
 	double x;
 
 	/** coordinates */
 	double y;
 
 	/** coordinates */
 	double z;
 
 	public double getX() {
 		return x;
 	}
 
 	public void setX(double x) {
 		this.x = x;
 	}
 
 	public double getY() {
 		return y;
 	}
 
 	public void setY(double y) {
 		this.y = y;
 	}
 
 	public double getZ() {
 		return z;
 	}
 
 	public Point2D getPoint2D () {
 		return new Point2D.Double(x, y);
 	}
 
 	/** velocities in the three coordinate axes, [m/s] */
 	double v;
 
 	/** velocities in the three coordinate axes, [m/s] */
 	double vx;
 
 	/** velocities in the three coordinate axes, [m/s] */
 	double vy;
 
 	/** velocities in the three coordinate axes, [m/s] */
 	double vz;
 
 	/** angle of view */
 	public double viewAngle;
 
 	/** whether character is turning */
 	double dViewAngle;
 
 	public double getViewAngle() {
 		return viewAngle;
 	}
 
 	public void setViewAngle(double viewAngle) {
 		this.viewAngle = viewAngle;
 	}
 
 	/** movement state variable */
 	public short localxvsign;
 
 	/** movement state variable */
 	short localrotvsign;
 
	/** maximal velocity */
	private static double vMax = 1.0;

 	/** lets the physics work on the lifeform and moves it by its velocities */
 	public void move(int sleepTime) {
 		/* calculate velocity */
 		if (localxvsign == 0) {
 			v = 0;
 		}
 		else {
 			v += 0.3* localxvsign;
 		}
 	
		if (localxvsign > 0) {
			v = Math.min(vMax, v);
		}
		else if (localxvsign < 0) {
			v = Math.max(-vMax, v);
		}
 	
 		vx = v*Math.cos(viewAngle);
 		vy = v*Math.sin(viewAngle);
 	
 		double t = sleepTime/1000.0;
 		neighborsListAge += t;
		
 		if (canMove || canFly) {
 			double a = x + vx*t;
 			double b = y + vy*t;
 			double c = z + vz*t;
 	
 			/* try to move, check for wall */
 			if (sim.getEnv().isFreeBetween(x, y, z, a, b, c)) {
 				x = a;
 				y = b;
 				z = c;
 			}
 			viewAngle += localrotvsign*t;
 		}
 	}
 
 	/** returns the distance to the other lifeform l, currently just the geometric mean of the axes, later it might include some path trough the world */
 	public double distance (Lifeform l) {
 		return Math.hypot(getX()-l.getX(), getY()-l.getY());
 	}
 
 	/*
 	 * everything with perception
 	 * - senses
 	 * - neighbors
 	 */
 	
 	boolean canSee = false;
 	boolean canSeeIR = false;
 	boolean canSeeXRay = false;
 	boolean canHear = false;
 	boolean canFeel = false;
 	boolean canSmell = false;
 	boolean canTaste = false;
 	boolean canMove = false;
 	boolean canFly = false;
 
 	public boolean isCanSee() {
 		return canSee;
 	}
 
 	public boolean isCanFly() {
 		return canFly;
 	}
 
 	public void setCanSee(boolean canSee) {
 		this.canSee = canSee;
 	}
 
 	public void setCanFly(boolean canFly) {
 		this.canFly = canFly;
 	}
 
 	/** decides whether this lifeform can see some other lifeform l */
 	public abstract boolean canSee (Lifeform l);
 	
 	/** list of other lifeforms that this lifeform can see */
 	ArrayList<Lifeform> neighbors = new ArrayList<Lifeform>();
 
 	/** In order to save power, the list with neighbors is only refreshed when it expires. This tracks the age of the list */
 	private double neighborsListAge = 0.0;
 	
 	public ArrayList<Lifeform> getNeighbors() {
 		if (neighbors == null) {
 			neighbors = new ArrayList<Lifeform>();
 			generateNeighborsList();
 		}
 		/* if the neighbors list is old, generate a new one */
 		if (neighborsListAge > 0.5) {
 			generateNeighborsList();
 			neighborsListAge = 0.0;
 		}
 		return neighbors;
 	}
 
 	/** generates a list with all the lifeforms this one can see */
 	public void generateNeighborsList() {
 		neighbors.clear();
 	
 		/** only traverse if the lifeform can see */
 		if (canSee) {
 			for (Lifeform l : sim.getLifeforms()) {
 				if (canSee(l) && l != this) {
 					neighbors.add(l);
 				}
 			}
 		}
 	}
 
 	/*
 	 * everything with actions
 	 * - ingestion
 	 * - takeover
 	 */
 
 	/** whether this lifeform is carrying out some sort of action */
 	public boolean busy;
 
 	/** progress with that action, from 0.0 to 1.0 */
 	public double actionProgress;
 
 	/** thread that handles the current action */
 	private Thread actionThread;
 
 	public boolean canTakeover(Lifeform prey) {
 		return canSee(prey);
 	}
 
 	/** takes over control of the given lifeform, returns whether that was successful */
 	public boolean takeover(Lifeform lifeform) {
 		if (canTakeover(lifeform) && !busy) {
 			return startTakeover(lifeform);
 		}
 		return false;
 	}
 
 	private boolean startTakeover(Lifeform lifeform) {
 		actionThread = new TakeoverThread(this, controllingPlayer, lifeform);
 		actionThread.start();
 		return true;
 	}
 
 	public boolean canIngest(Lifeform prey) {
 		return true;
 	}
 
 	/** ingests the given lifeform */
 	public void ingest(Lifeform whom) {
 		if (whom == null)
 			return;
 
 		if (canIngest(whom) && !busy) {
 			startIngestion(whom);
 		}
 	}
 
 	private void startIngestion(Lifeform whom) {
 		actionThread = new IngestionThread(this, controllingPlayer, whom, sim);
 		actionThread.start();		
 	}
 
 	/** stop whatever that lifeform is doing right now */
 	private void stopAction() {
 		if (actionThread != null && actionThread.isAlive()) {
 			actionThread.interrupt();
 		}
 	}
 	
 	/*
 	 * everything with key handling
 	 * - key pressed
 	 * - key released
 	 */
 
 	/** lets the lifeform handle a key press */
 	public void handleKeyPressed (KeyEvent e) {
 		switch (e.getKeyChar()) {
 		case 'w': localxvsign = 1; break;
 		case 's': localxvsign = -1; break;
 		case 'a': localrotvsign = -3; break;
 		case 'd': localrotvsign = 3; break;
 		}
 
 		stopAction();
 	}
 
 	/** lets the lifeform handle a key release */
 	public void handleKeyReleased (KeyEvent e) {
 		switch (e.getKeyChar()) {
 		case 'w':
 		case 's': localxvsign = 0; break;
 		case 'a':
 		case 'd': localrotvsign = 0; break;
 		}
 	
 		v = Math.max(0, v);
 	
 		vx = v*Math.cos(viewAngle);
 		vy = v*Math.sin(viewAngle);
 	}
 
 	/*
 	 * everything with external control by a player
 	 */
 	
 	/** the player who controls this lifeforms, if null, the computer controls it */
 	public Player controllingPlayer = null;
 
 	public boolean isControlled() {
 		return controllingPlayer != null;
 	}
 
 	public void setControlled(Player p) {
 		this.controllingPlayer = p;
 	}
 	
 	/*
 	 * everything with personal properties
 	 * - ID
 	 * - name
 	 * - biomass
 	 *   - diameter
 	 * - graphics
 	 */
 
 	/** unique id */
 	int id;
 
 	/** main id counter, determines the next given id */
 	protected static int next_id = 0;
 
 	public int getID() {
 		return id;
 	}
 
 	/** given name of the object */
 	String name = new String();
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String value) {
 		name = value;
 	}
 
 	/** whether this lifeform is still alive */
 	boolean alive = true;
 
 	/** the mass of biological stuff the lifeform ingested so far */
 	private double biomass = 0.0;
 
 	public double getBiomass() {
 		return biomass;
 	}
 
 	public void setBiomass(double biomass) {
 		this.biomass = biomass;
 	}
 
 	/** gives a generic description string of the object */
 	public String toString () {
 		if (name.equals("")) {
 			return getClass().getSimpleName()+"\t"+getBiomass()+" kg";
 		}
 		else {
 			return getName()+"\t("+getClass().getSimpleName()+")\t"+getBiomass()+" kg";
 		}
 	}
 
 	public double getDiameter() {
 		return Math.max(Math.sqrt(biomass)/40, .01);
 	}
 
 	abstract public ConvertedGraphics getConvertedGraphics();
 }
