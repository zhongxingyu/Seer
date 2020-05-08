 package se.chalmers.tda367.std.core.enemies;
 
 import java.beans.PropertyChangeEvent;
 import java.util.ArrayList;
 import java.util.List;
 
 import se.chalmers.tda367.std.core.effects.IEffect;
 import se.chalmers.tda367.std.utilities.Sprite;
 
 /**
  * A skeleton implementation of the IEnemy.
  * @author Emil Edholm
  * @modified Emil Johansson, Johan Andersson
  * @date Mar 22, 2012
  */
 public abstract class AbstractEnemy implements IEnemy{
 
 	private final int baseHealth;
 	private final float baseSpeed;
 	private final int baseArmor;
 	private int currentHealth;
 	private final Sprite sprite;
 	private final int lootValue;
 //	private float currentSpeed;
 //	private int currentArmor;
 	private int boardValue;
 	private List<IEffect> effects = new ArrayList<IEffect>();
 
 	public AbstractEnemy(int startHealth, float speed, int armor, int lootValue, Sprite sprite){
 		this.baseHealth = startHealth;
		this.currentHealth = this.baseHealth;
 		this.baseSpeed    = speed;
 //		this.currentSpeed	= baseSpeed;
 		this.baseArmor 		= armor;
 //		this.currentArmor = baseArmor;
 		this.lootValue     = lootValue;
 		this.sprite        = sprite;
 	}
 
 	@Override
 	public void addEffect(IEffect effect) {
 		effects.add(effect);
 	}
 
 	@Override
 	public void removeEffect(IEffect effect) {
 		effects.remove(effect);
 	}
 
 	@Override
 	public List<IEffect> getEffects() {
 		return effects;
 	}
 
 	@Override
 	public int getHealth() {
 		return currentHealth;
 	}
 	@Override
 	public int getLootValue(){
 		return lootValue;
 	}
 	@Override
 	public float getSpeed(){
 		double speed = baseSpeed;
 		for (IEffect effect : effects) {
 			speed = speed * effect.getSpeedModifier();
 		}
 		return (float)speed;
 	}
 //	@Override
 //	public float increaseSpeed(float inc){
 //		currentSpeed = currentSpeed + inc;
 //		return currentSpeed;
 //	}
 //	@Override
 //	public float decreaseSpeed(float inc){
 //		currentSpeed = currentSpeed - inc;
 //		return currentSpeed;
 //	}
 
 	@Override
 	public void decreaseHealth(int dmg) {
 		int reducedDmg = dmg - this.getArmor();
 		dmg = (reducedDmg > 0) ? reducedDmg : 0;
 		
 		if(dmg > currentHealth) {
 			currentHealth = 0;
 			return;
 		}
 
 		currentHealth -= dmg;
 
 		// TODO: Send event that enemy has died.
 	}
 
 	@Override
 	public Sprite getSprite() {
 		return sprite;
 	}
 	
 	
 
 
 	@Override
 	public int getBaseHealth() {
 		return baseHealth;
 	}
 
 	@Override
 	public float getBaseSpeed() {
 		return baseSpeed;
 	}
 
 	@Override
 	public int getArmor() {
 		double armor = baseArmor;
 		for(IEffect effect: effects){
 			armor = armor * effect.getArmorModifier();
 		}
 		return (int)armor;
 	}
 
 	@Override
 	public int getBaseArmor() {
 		return baseArmor;
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		if(evt.getPropertyName().equals("damage")){
 			decreaseHealth((Integer)evt.getNewValue());
 		}
 	}
 
 	@Override
 	public String toString(){
 		return this.getClass().getName() + " { " +
 				"\n\tCurrent health: " + currentHealth +
 				"\n\tSprite: "         + sprite        +
 				"\n}\n";
 
 		// This should probably not be "closed" and it should be up to each
 		// concrete implementation to do that and add their specific values.
 
 	}
 
 
 	@Override
 	public int getBoardValue(){
 		return this.boardValue;
 	}
 	@Override
 	public void setBoardValue(int boardValue){
 		this.boardValue = boardValue;
 	}
 	@Override
 	public int compareTo(IEnemy enemy){
 		if (this.getBoardValue() < enemy.getBoardValue()){
 			return -1;
 		} else if (this.getBoardValue() > enemy.getBoardValue()){
 			return 1;
 		} else {
 			return 0;
 		}
 	}
 }
