 package src.core;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.simpleframework.xml.Attribute;
 import org.simpleframework.xml.Element;
 import org.simpleframework.xml.ElementList;
 
 import src.FilePaths;
 import src.core.xml.TowerXMLReader;
 import src.ui.IDrawableTower;
 
 /**
  * Represents a tower and all of its attributes, including attack radius, which
  * creeps to attack first, damage done, and how fast to fire.
  */
 public class Tower implements IDrawableTower, IPurchasable {
 	private static HashMap<Type, Tower> templateTowers = null;
 	
 	@Attribute
 	private Tower.Type type;
 	
 	@Element
 	private Damage damage;
 	
 	@Element
 	private double radius;
 	
 	@Element
 	private double fireRate;
 	
 	@Element
 	private double price;
 	
 	@Element(required=false)
 	private TargetingInfo targeting;
 	
 	@ElementList(required=false)
 	private ArrayList<Upgrade> upgrades;
 	
 	private int upgradeLevel;
 	
 	public ArrayList<Upgrade> getUpgrades() {
 		return upgrades;
 	}
 
 	private int x, y;
 	private double investment;
 	
 	public static Tower createTower(Type t){
 		if (templateTowers == null) {
 			templateTowers = TowerXMLReader.readXML(FilePaths.xmlPath + "towerConfig.xml");
 		}
 		
 		Tower template = templateTowers.get(t);
 		Tower tower = new Tower();
 		
 		tower.setDamage(template.getDamage());
 		tower.setFireRate(template.getFireRate());
 		tower.setPrice(template.getPrice());
 		tower.setRadius(template.getRadius());
 		tower.upgrades = template.getUpgrades();
 		tower.setInvestment(tower.getPrice());
 		tower.setType(t);
 		
 		return tower;
 	}
 	
 	public Tower() {
 		targeting = new TargetingInfo();
 		upgradeLevel = 0;
 	}
 
 	public double getFireRate() {
 		return fireRate;
 	}
 
 	public void setFireRate(double fireRate) {
 		this.fireRate = fireRate;
 	}
 
 	public TargetingInfo getTargeting() {
 		return targeting;
 	}
 
 	public double getRadius() {
 		return radius;
 	}
 
 	public void setRadius(double radius) {
 		this.radius = radius;
 	}
 
 	public enum Type {
 		GUN, ANTIAIR, SLOWING, MORTAR, FRIEND, FLAME, STASIS, HTA;
 	}
 
 	public Damage getDamage() {
 		return damage;
 	}
 
 	public void setDamage(Damage damage) {
		//this.damage = damage;
		this.damage = Damage.copyDamage(damage);
 	}
 
 	public double getOrientation() {
 		// TODO: stub
 		return 0;
 	}
 
 	public void setType(Tower.Type t){
 		type = t;
 	}
 	
 	public Type getType() {
 		return type;
 	}
 
 	public int getX() {
 		return x;
 	}
 
 	public void setX(int x) {
 		this.x = x;
 	}
 
 	public int getY() {
 		return y;
 	}
 
 	public void setY(int y) {
 		this.y = y;
 	}
 
 	public void setInvestment(double d) {
 		investment = d;
 	}
 
 	public double getInvestment() {
 		return investment;
 	}
 	
 	public void addInvestment(IPurchasable item) {
 		investment += item.getPrice();
 	}
 
 	// Applies an upgrade u onto this tower, modifying its Damage,
 	// TargetingInfo, and self
 	public void applyUpgrade(Upgrade u) {
 		assert u.getLevel() == upgradeLevel + 1 : "Attempted to apply inappropriate update!";
 		
 		u.updateDamage(damage); // all damage modifications
 		u.updateTargeting(targeting); // canHitFlying
 		u.updateTower(this); // radius, rate of fire, investment
 		upgradeLevel++;
 	}
 	
 	public double getPrice() {
 		return price;
 	}
 
 	public void setPrice(double p) {
 		price = p;
 	}
 	
 	public int getUpgradeLevel() {
 		return upgradeLevel;
 	}
 }
