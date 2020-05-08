 package projectubernahme.lifeforms;
 
 import java.awt.event.KeyEvent;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 
 import projectubernahme.Localizer;
 import projectubernahme.Player;
 import projectubernahme.StringRead;
 import projectubernahme.simulator.MainSimulator;
 
 
 /** something with its own mind in the game, can interact with other lifeforms and the world */
 abstract public class Lifeform {
 	/** unique id */
 	int id;
 
 	static int next_id = 0;
 
 	/** coordinates */
 	double x, y, z;
 
 	/** angle of view */
 	double viewAngle;
 
 	/** velocities in the three coordinate axes, [m/s] */
 	double v, vx, vy, vz;
 
 	/** given name of the object */
 	String name = new String();
 
 	boolean alive = true;
 
 	/** whether character is turning */
 	double dViewAngle;
 
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
 
 	/** whether this lifeform is controlled by some player, meaning that it does not need to be controlled by the computer */
 	private Player controllingPlayer = null;
 
 	/** the mass of biological stuff the lifeform ingested so far */
 	private double biomass = 0.0;
 
 	MainSimulator sim;
 
 	public Lifeform (MainSimulator sim) {
 		this.sim = sim;
 		name = new String();
 		id = next_id++;
 	}
 
 	/** lets the lifeform look around and interact with its proximity */
 	public void lookAround (Player player) {
 		ArrayList<Lifeform> inProximity = new ArrayList<Lifeform>();
 		if (sim.getLifeforms().size() > 0) {
 			for (Lifeform npc : sim.getLifeforms()) {
 				if (this.canSee(npc)) {
 					inProximity.add(npc);
 				}
 			}
 		}
 		if (inProximity.size() > 0) {
 			System.out.println(Localizer.get("This lifeform can see the following other lifeforms:"));
 			int i = 0;
 			for (Lifeform npc : inProximity) {
 				System.out.println(i+++"\t"+npc.toString());
 			}
 			System.out.println(Localizer.get("choose a lifeform to interact with (-1 for no interaction) "));
 			int choice = Integer.parseInt(StringRead.read());
 			if (choice > -1) {
 				Lifeform selected = inProximity.get(choice);
 				System.out.println(Localizer.get("choose an interaction"));
 				System.out.println(Localizer.get("ta\ttake over control"));
 				System.out.println(Localizer.get("in\tingest biomass"));
 				String action = StringRead.read();
 
 				if (action.equals(Localizer.get("ta"))) {
 					player.takeControlOver(selected);
 				}
 				else if (action.equals(Localizer.get("in"))) {
 					sim.getLifeforms().remove(selected);
 					this.setBiomass(this.getBiomass() + selected.getBiomass());
 				}
 			}
 		}
 		else {
 			System.out.println(Localizer.get("This lifeform cannot see any other lifeforms"));
 		}
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
 		return Math.sqrt(Math.pow(getX()-l.getX(), 2) + Math.pow(getY()-l.getY(), 2) + Math.pow(z-l.z, 2));
 	}
 
 	/** polls for a new name and applies it */
 	public void rename () {
 		System.out.print(Localizer.get("New Name: "));
 		name = StringRead.read();
 	}
 
 	/** lets the physics work on the lifeform and moves it by its velocities */
 	public void move(int sleepTime) {
 		if (canMove || canFly) {
			double t = sleepTime/1000.0;
 
 			double a = x + vx*t;
 			double b = y + vy*t;
 			double c = z + vz*t;
 
 			/* try to move, check for wall */
 			if (sim.getEnv().isFreeBetween(x, y, z, a, b, c)) {
 				x = a;
 				y = b;
 				z = c;
 			}
 
 			viewAngle += dViewAngle*t;
 		}
 	}
 
 	/** this lets the lifeform act, this can be just sitting around or calling for support or attacking another lifeform */
 	abstract public void act(int sleepTime);
 
 	public void handleKeyPressed (KeyEvent e) {
 		switch (e.getKeyChar()) {
 		case 'w': v += .3; break;
 		case 's': v += -.3; break;
 		case 'a': dViewAngle = -3.0; break;
 		case 'd': dViewAngle = 3.0; break;
 		}
 
 		v = Math.min(1, v);
 
 		vx = v*Math.cos(viewAngle);
 		vy = v*Math.sin(viewAngle);
 	}
 
 	public void handleKeyReleased (KeyEvent e) {
 		switch (e.getKeyChar()) {
 		case 'w':
 		case 's': v -= 3.0; break;
 		case 'a':
 		case 'd': dViewAngle = 0.0; break;
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
 		}
		if (neighbors.size() == 0) {
 			generateNeighborsList();
 		}
 		return neighbors;
 	}
 
 	/** generates a list with all the lifeforms this one can see */
 	public void generateNeighborsList() {
 		neighbors.clear();
 
 		for (Lifeform l : sim.getLifeforms()) {
 			if (canSee(l) && l != this) {
 				neighbors.add(l);
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
 		return Math.max(Math.sqrt(biomass)/100, .1);
 	}
 }
