 package pl.edu.agh.two.mud.common;
 
public class Creature implements ICreature {
 
 	private String name;
 	protected int strength = 1;
 	protected int power = 1;
 	protected int agililty = 1;
 	protected int level = 1;
 	protected int healthPoints = 10;
 	protected int maxHealthPoints = 10;
 	protected IPlayer enemy;
	
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public void setName(String name) {
 		this.name = name;
 	}
	
 	@Override
 	public Integer getStrength() {
 		return strength;
 	}
 
 	@Override
 	public void setStrength(Integer strength) {
 		this.strength = strength;
 	}
 
 	@Override
 	public Integer getPower() {
 		return power;
 	}
 
 	@Override
 	public void setPower(Integer power) {
 		this.power = power;
 	}
 
 	@Override
 	public Integer getAgililty() {
 		return agililty;
 	}
 
 	@Override
 	public void setAgililty(Integer agililty) {
 		this.agililty = agililty;
 	}
 
 	@Override
 	public Integer getLevel() {
 		return level;
 	}
 
 	@Override
 	public void setLevel(Integer level) {
 		this.level = level;
 	}
 
 	@Override
 	public Integer getHealthPoints() {
 		return healthPoints;
 	}
 
 	@Override
 	public void setHealthPoints(Integer healthPoints) {
 		this.healthPoints = healthPoints;
 	}
 
 	@Override
 	public void subtractHealthPoints(Integer damage) {
 		healthPoints -= damage;
 	}
 
 	@Override
 	public boolean isInFight() {
 		return enemy != null;
 	}
 
 	@Override
 	public IPlayer getEnemy() {
 		return enemy;
 	}
 
 	@Override
 	public void setEnemy(IPlayer enemy) {
 		this.enemy = enemy;
 	}
 
 	@Override
 	public int getMaxHealthPoints() {
 		return maxHealthPoints;
 	}
 
 	@Override
 	public void setMaxHealthPoints(int maxHealthPoints) {
 		this.maxHealthPoints = maxHealthPoints;
 	}
 
 	@Override
 	public boolean isAlive() {
 		return healthPoints > 0;
 	}
 
 }
