 package projectubernahme.lifeforms;
 
 import java.awt.event.KeyEvent;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 
 import projectubernahme.Player;
 import projectubernahme.gfx.ConvertedGraphics;
 import projectubernahme.simulator.MainSimulator;
 
 
 /** something with its own mind in the game, can interact with other lifeforms and the world */
 abstract public class Lifeform {	
 	/** unique id */
 	int id;
 
 	/** main id counter, determines the next given id */
 	protected static int next_id = 0;
 
 	/** coordinates */
 	double x, y, z;
 
 	/** angle of view */
 	double viewAngle;
 
 	/** velocities in the three coordinate axes, [m/s] */
 	double v, vx, vy, vz;
 
 	/** given name of the object */
 	String name = new String();
 
 	boolean alive = true;
 	
 	/** Movement state Variables*/
 	short localxvsign, localrotvsign;
 
 	/** whether character is turning */
 	double dViewAngle;
 
 	/** list of other lifeforms that this lifeform can see */
 	ArrayList<Lifeform> neighbors = new ArrayList<Lifeform>();
 
 	/* senses */
 	boolean canSee = false;
 	boolean canSeeIR = false;
 	boolean canSeeXRay = false;
 	boolean canHear = false;
 	boolean canFeel = false;
 	boolean canSmell = false;
 	boolean canTaste = false;
 
 	/* actions */
 	boolean canMove = false;
 	private boolean canFly = false;
 
 	/** the player who controls this lifeforms, if null, the computer controls it */
 	private Player controllingPlayer = null;
 
 	/** the mass of biological stuff the lifeform ingested so far */
 	private double biomass = 0.0;
 
 	/** the simulator this lifeform is connected to */
 	MainSimulator sim;
 
 	/** In order to save power, the list with neighbors is only refreshed when it expires. This tracks the age of the list */
 	private double neighborsListAge = 0.0;
 
 	public Lifeform (MainSimulator sim) {
 		this.sim = sim;
 		name = new String();
 		id = next_id++;
 	}
 
 	/** gives a generic description string of the object */
 	public String toString () {
 		if (name.equals("")) {
 			return getClass().getSimpleName()+"\t"+getBiomass()+" kg";
 		}
 		else {
 			return name+"\t("+getClass().getSimpleName()+")\t"+getBiomass()+" kg";
 		}
 	}
 
 	/** takes over control of the given lifeform, returns whether that was successful */
 	public boolean takeover(Lifeform lifeform) {
 		if (canSee(lifeform)) {
 			lifeform.setControlled(controllingPlayer);
 			return true;
 		}
 		return false;
 	}
 
 	/** decides whether this lifeform can see some other lifeform l */
 	public abstract boolean canSee (Lifeform l);
 
 	/** returns the distance to the other lifeform l, currently just the geometric mean of the axes, later it might include some path trough the world */
 	public double distance (Lifeform l) {
 		// TODO use the easier method instead
 		return Math.sqrt(Math.pow(getX()-l.getX(), 2) + Math.pow(getY()-l.getY(), 2) + Math.pow(z-l.z, 2));
 	}
 
 	/** lets the physics work on the lifeform and moves it by its velocities */
 	public void move(int sleepTime) {
 		//Calculate velocity
 		if(localxvsign == 0) {
 			v = 0;
 		}else {
 			v += 0.3* localxvsign;
 		}
 		
		v = Math.min(1, v * localxvsign);
 
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
 
 	/** this lets the lifeform act, this can be just sitting around or calling for support or attacking another lifeform */
 	abstract public void act(int sleepTime);
 
 	/** lets the lifeform handle a keystroke */
 	public void handleKeyPressed (KeyEvent e) {
 		switch (e.getKeyChar()) {
 		case 'w': localxvsign = 1; break;
 		case 's': localxvsign = -1; break;
 		case 'a': localrotvsign = -3; break;
 		case 'd': localrotvsign = 3; break;
 		}
 	}
 
 	/** lets the lifeform handle a keystroke */
 	public void handleKeyReleased (KeyEvent e) {
 		switch (e.getKeyChar()) {
 		case 'w': localxvsign = 0; break;
 		case 's': localxvsign = 0; break;
 		case 'a': localrotvsign = 0; break;
 		case 'd': localrotvsign = 0; break;
 		}
 
 		v = Math.max(0, v);
 
 		vx = v*Math.cos(viewAngle);
 		vy = v*Math.sin(viewAngle);
 	}
 
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
 
 	public String getName() {
 		return name;
 	}
 
 	public double getBiomass() {
 		return biomass;
 	}
 
 	public void setBiomass(double biomass) {
 		this.biomass = biomass;
 	}
 
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
 
 	public boolean isControlled() {
 		return controllingPlayer != null;
 	}
 
 	public void setControlled(Player p) {
 		this.controllingPlayer = p;
 	}
 
 	public int getID() {
 		return id;
 	}
 
 	public void setName(String value) {
 		name = value;
 
 	}
 
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
 
 	public Point2D getPoint2D () {
 		return new Point2D.Double(x, y);
 	}
 
 	public void setViewAngle(double viewAngle) {
 		this.viewAngle = viewAngle;
 	}
 
 	public double getViewAngle() {
 		return viewAngle;
 	}
 
 	public double getDiameter() {
 		return Math.max(Math.sqrt(biomass)/40, .01);
 	}
 
 	/** ingests the given lifeform */
 	public void ingest(Lifeform whom) {
 		if (whom == null)
 			return;
 		
 		/* remove lifeform from player's list */
 		if (whom.isControlled()) {
 			if (whom.controllingPlayer.getControlledLifeforms().contains(whom)) {
 				whom.controllingPlayer.getControlledLifeforms().remove(whom);
 			}
 			
 
 			/* if the ingested lifeform was selected before, it will get unselected */
 			if (whom.controllingPlayer.getSelectedLifeform() == whom) {
 				whom.controllingPlayer.setSelectedLifeform(null);
 			}
 		}
 		
 
 		/* remove lifeform from simulator */
 		sim.getLifeforms().remove(whom);
 		setBiomass(getBiomass() + whom.getBiomass());
 		if (!getName().equals("") && !whom.getName().equals(""))
 			setName(getName()+"-"+whom.getName());
 	}
 
 
 	abstract public ConvertedGraphics getConvertedGraphics();
 }
