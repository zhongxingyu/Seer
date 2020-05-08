 package src.core;
 
 import java.awt.geom.Point2D;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.simpleframework.xml.Attribute;
 import org.simpleframework.xml.Element;
 
 import src.FilePaths;
 import src.core.xml.CreepXMLReader;
 import src.ui.IDrawableCreep;
 
 
 /**
  * Represents an enemy in the game.
  */
 public class Creep implements IDrawableCreep, IAlignment, IPurchasable {
 	private static double WEAKNESS_MODIFIER = 1.2;
 	private static double STRENGTH_MODIFIER = 0.8;
 	
 	private static HashMap<Creep.Type, Creep> templateCreeps;
 	
 	private static Creep createCreep(Type t) {
 		if (templateCreeps == null) { 
 			templateCreeps = CreepXMLReader.readXML(FilePaths.xmlPath + "creeps.xml");
 		}
 		
 		Creep templateCreep = templateCreeps.get(t);
 		Creep newCreep = new Creep();
 		newCreep.setBaseHealth(templateCreep.getBaseHealth());
 		newCreep.setHealth(templateCreep.getBaseHealth());
 		newCreep.setFlying(templateCreep.isFlying());
 		newCreep.setDamageToBase(templateCreep.getDamageToBase());
 		newCreep.setPrice(templateCreep.getPrice());
 		newCreep.setReward(templateCreep.getReward());
 		newCreep.setSpeed(templateCreep.getSpeed());
 		newCreep.setType(t);
 		newCreep.setAlignment(Alignment.NEUTRAL);
 		newCreep.setAdditionalGoldPerWave(templateCreep.getAdditionalGoldPerWave());
 		
 		return newCreep;
 	}
 	
 	public static Creep createCreep(Type t, int waveNumber) {
 		Creep originalCreep = createCreep(t);
 		int scalingFactor = waveNumber / 2;
 		
 		originalCreep.setBaseHealth(originalCreep.getBaseHealth() + scalingFactor*25);
 		originalCreep.setHealth(originalCreep.getBaseHealth());
 		originalCreep.setPrice(originalCreep.getPrice() + scalingFactor*10);
 		
 		return originalCreep;
 	}
 	
 	@Element
 	private double baseHealth;
 	
 	private double health;
 	
 	@Element
 	private double reward;
 	
 	@Element
 	private double speed; // the speed of this creep, as a percentage of normal speed
 	
 	@Attribute
 	private Type type;
 	
 	@Element
 	private boolean flying;
 	
 	@Element
 	private double damageToBase;
 
 	
 	private HashMap<Tower, DamageApplication> damages;
 
 	private CreepPath path;
 	private int pathIndex;
 
 	private Point2D.Double position;
 
 	@Element
 	private double price;
 	
 	@Element
 	private double additionalGoldPerWave;
 
 	public double getPrice() {
 		return price;
 	}
 
 	private IAlignment.Alignment alignment;
 	
 	public enum Type {
 		GENERIC,
 		BIG_GUY,
 		ASSASSIN,
 		FLYING,
 		FAST
 	}
 
 	public Creep() {
 		damages = new HashMap<Tower, DamageApplication>();
 		pathIndex = 0;
 		health = baseHealth;
 	}
 	
 	/**
 	 * Determines the direction this creep is moving in.
 	 * 
 	 * @return A Point2D representing the unit direction vector.
 	 */
 	public Point2D.Double getNextDirection() {
 		Point2D.Double target = path.getPoint(pathIndex);
 		double length = position.distance(target);
 
 		if (length < .1) {
 			setPosition(target);
 			incrementPathTarget();
 			target = path.getPoint(pathIndex);
 
 			if (target == null) {
 				return null;
 			}
 
 			length = position.distance(target);
 		}
 
 		Point2D.Double direction = new Point2D.Double(target.getX()
 				- position.getX(), target.getY() - position.getY());
 
 		return new Point2D.Double(direction.getX() / length, direction.getY()
 				/ length);
 	}
 
 	public double getDamageToBase() {
 		return damageToBase;
 	}
 
 	/**
 	 * Every tower is only allowed to apply time-based damage to each creep once at a time.
 	 * 
 	 * For example, a tower that slows down a creep by 10 percent for 10 seconds may not attach
 	 * this damage modifier to the same creep over and over again.
 	 * 
 	 * @note This method returning true does not necessarily mean that the tower can attack.
 	 * 		 There are other factors, such as whether or not the tower is in range, which are
 	 * 		 checked elsewhere.
 	 * 
 	 * @param t A tower
 	 * @return A boolean indicating whether or not the provided tower is allowed to damage to
 	 * 		   this creep.
 	 */
 	public boolean towerCanApplyDamage(Tower t) {
 		if (!damages.containsKey(t) /* && targetable */) { // TODO: fix this
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public void applyDamage(Damage d, Tower t, int applicationTime) {
 		//factor in increases or decreases of damage due to alignment
 		double damage = d.getInstantDamage();
		if(this.alignment != IAlignment.Alignment.NEUTRAL) {
 			if(t.getAlignment() == this.alignment.getWeakTo()) //if this creep is weak to tower's element
 				damage *= WEAKNESS_MODIFIER;
 			else if(t.getAlignment() == this.alignment.getStrength()) // if this creep is strong to tower's element
 				damage *= STRENGTH_MODIFIER;
 		}
 		health -= damage;
 		
 		// if there will be any timed effects, hold on to them
 		if (d.getEffectDuration() > 0 && !damages.containsKey(t)) {
 			speed *= 1 + d.getSpeedChange();
 			
 			DamageApplication da = new DamageApplication(d, applicationTime);
 			damages.put(t, da);
 		}
 	}
 	
 	public void handleTimedDamage(int time) {
 		Iterator it = damages.keySet().iterator();
 		while (it.hasNext()) {
 			Tower t = (Tower) it.next();
 			
 			if (t == null) { // just to be safe
 				it.remove();
 				continue;
 			}
 			
 			DamageApplication da = damages.get(t);
 			
 			if (da.shouldUnattach(time)) {
 				it.remove();
 				speed /= 1 + da.getDamage().getSpeedChange();
 			} else if (da.shouldDoTimeDamage(time)) {
 				health -= da.getDamage().getTimeDamage();
 			}
 		}
 	}
 
 	public CreepPath getPath() {
 		return path;
 	}
 
 	public void setPath(CreepPath path) {
 		this.path = path;
 	}
 
 	public int getPathTarget() {
 		return pathIndex;
 	}
 
 	public void incrementPathTarget() {
 		pathIndex++;
 	}
 
 	public boolean isFlying() {
 		return flying;
 	}
 
 	public void setFlying(boolean flying) {
 		this.flying = flying;
 	}
 
 	public Type getType() {
 		return type;
 	}
 
 	public double getHealthFraction() {
 		return health / baseHealth;
 	}
 
 	public Point2D.Double getPosition() {
 		return position;
 	}
 
 	public void setPosition(Point2D.Double p) {
 		position = p;
 	}
 
 	public double getSpeed() {
 		return speed;
 	}
 
 	public void setSpeed(double speed) {
 		this.speed = speed;
 	}
 	
 	public double getReward() {
 		return reward;
 	}
 
 	public double getBaseHealth() {
 		return baseHealth;
 	}
 
 	public double getHealth() {
 		return health;
 	}
 
 	public void setHealth(double health) {
 		this.health = health;
 	}
 
 	public void setBaseHealth(double baseHealth) {
 		this.baseHealth = baseHealth;
 	}
 
 	public void setReward(double reward) {
 		this.reward = reward;
 	}
 
 	public void setType(Type type) {
 		this.type = type;
 	}
 
 	public void setDamageToBase(double damageToBase) {
 		this.damageToBase = damageToBase;
 	}
 
 	public void setPrice(double price) {
 		this.price = price;
 	}
 
 	public Alignment getAlignment() {
 		return alignment;
 	}
 
 	public void setAlignment(Alignment alignment) {
 		this.alignment = alignment;
 	}
 
 	public void setAdditionalGoldPerWave(double d) {
 		 additionalGoldPerWave = d;
 	}
 	
 	public double getAdditionalGoldPerWave() {
 		return additionalGoldPerWave;
 	}
 }
