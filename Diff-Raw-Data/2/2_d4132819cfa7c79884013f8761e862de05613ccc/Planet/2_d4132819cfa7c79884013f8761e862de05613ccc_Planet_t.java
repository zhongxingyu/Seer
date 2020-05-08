 package domain;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Random;
 
 import util.Constants;
 import util.StaticAccess;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.FastMath;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Geometry;
 
 import drawing.Circle;
 
 public class Planet implements Drawable, Destructable, Updatable {
 
 	private float actionLimiter = 0.0f;
 
 	private final int maxHealth;
 	private int currentHealth;
 	private final float size;
 	private final Vector3f location;
 	private final Geometry geometry;
 	private int gold = 10;
 	private int score = 0;
 	private final Map<Planet, Integer> revengeMeter;
 	private final Random random = new Random();
 	private boolean sendAttack = false;
 	private Planet attackTarget = null;
 	private Class<? extends Ship> shipType = null;
 	private int goldReserve = 0;
 
 	public Planet(Vector3f location, float size) {
 		this.maxHealth = 200;
 		this.currentHealth = 200;
 		this.size = size;
 		this.location = location;
 		this.geometry = new Geometry("planet", new Circle(this.size, 32));
 		this.geometry.setMaterial(getMaterial());
 		this.geometry.rotate(FastMath.HALF_PI, 0, 0);
 		this.geometry.setLocalTranslation(location);
 		this.revengeMeter = Maps.newHashMap();
 	}
 
 	@Override
 	public Vector3f getLocation() {
 		return this.location;
 	}
 
 	@Override
 	public float getSize() {
 		return this.size;
 	}
 
 	@Override
 	public Geometry getView() {
 		return this.geometry;
 	}
 
 	private Material getMaterial() {
 		Material material = new Material(StaticAccess.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
 		material.setColor("Color", new ColorRGBA(0, 255, 255, 1));
 		return material;
 	}
 
 	@Override
 	public int getMaxHealth() {
 		return this.maxHealth;
 	}
 
 	@Override
 	public int getCurrentHealth() {
 		return this.currentHealth;
 	}
 
 	@Override
 	public float getHealthBarSize() {
 		return Constants.PLANET_HEALTH_BAR_SIZE;
 	}
 
 	@Override
 	public void takeDamage(int damage, Planet source) {
 		this.currentHealth -= damage;
 		if (this.revengeMeter.containsKey(source)) {
 			this.revengeMeter.put(source, this.revengeMeter.get(source) + damage);
 		} else {
 			this.revengeMeter.put(source, damage);
 		}
 	}
 
 	public void scoreDamage(int damage, Planet target) {
 		this.gold += damage;
 		this.score += damage;
 		if (this.revengeMeter.containsKey(target)) {
 			this.revengeMeter.put(target, this.revengeMeter.get(target) - damage);
 		}
 	}
 
 	public int getGold() {
 		return this.gold;
 	}
 
 	public int getScore() {
 		return this.score;
 	}
 
 	public void goldTick() {
 		this.gold++;
 	}
 
 	@Override
 	public void update(float tpf) {
 		this.actionLimiter += tpf;
 		if (this.actionLimiter > Constants.ACTION_RATE) {
 			this.actionLimiter = 0.0f;
 
 			if (this.random.nextInt((int) Constants.DESTROYER_PREFERENCE_RATE) == 1
 					&& this.gold >= Constants.DESTROYER_SHIP_COST) {
 				launchAttack(DestroyerShip.class);
 			} else if (this.random.nextInt((int) Constants.BOMBER_PREFERENCE_RATE) == 1
 					&& this.gold >= Constants.BOMBER_SHIP_COST) {
 				launchAttack(BomberShip.class);
 			} else if (this.gold - this.goldReserve >= Constants.SMALL_SHIP_COST) {
 				launchAttack(SmallShip.class);
 			}
 			if (this.random.nextInt((int) Constants.GOLD_RESERVE_RATE) == 0) {
 				this.goldReserve++;
 			}
 			if (this.random.nextInt((int) Constants.GOLD_RESERVE_RESET_RATE) == 0) {
 				this.goldReserve /= 2;
 			}
 		}
 	}
 
 	private void launchAttack(Class<? extends Ship> clazz) {
 		List<Planet> planets = Lists.newArrayList();
 		for (Entry<Planet, Integer> entry : this.revengeMeter.entrySet()) {
 			if (entry.getValue() > Constants.REVENGE_LIMIT
 					&& (StaticAccess.getPlanets().contains(entry.getKey()) || entry.getKey() == StaticAccess
 							.getHomePlanet())) {
 				planets.add(entry.getKey());
 			}
 		}
 
 		if (0 == planets.size()) {
 			planets = StaticAccess.getPlanets();
 			planets.remove(this);
 		}
 		if (0 == planets.size()) {
 			this.sendAttack = true;
 			this.attackTarget = StaticAccess.getHomePlanet();
 			this.shipType = clazz;
 		} else {
 			int planetIndex = this.random.nextInt(planets.size());
 			this.sendAttack = true;
 			this.attackTarget = planets.get(planetIndex);
 			this.shipType = clazz;
 		}
 	}
 
 	public boolean getSendAttack() {
 		return this.sendAttack;
 	}
 
 	public Planet getAttackTarget() {
 		return this.attackTarget;
 	}
 
 	public Class<? extends Ship> getShipType() {
 		return this.shipType;
 	}
 
 	public void confirmAttack(Class<? extends Ship> shipType) {
 		this.attackTarget = null;
 		this.sendAttack = false;
 		if (shipType == SmallShip.class) {
 			this.gold -= Constants.SMALL_SHIP_COST;
 		} else if (shipType == BomberShip.class) {
 			this.gold -= Constants.BOMBER_SHIP_COST;
		} else if (shipType == DestroyerShip.class) {
			this.gold -= Constants.DESTROYER_SHIP_COST;
 		}
 		this.shipType = null;
 
 	}
 
 	@Override
 	public boolean getDead() {
 		return this.currentHealth <= 0;
 	}
 }
