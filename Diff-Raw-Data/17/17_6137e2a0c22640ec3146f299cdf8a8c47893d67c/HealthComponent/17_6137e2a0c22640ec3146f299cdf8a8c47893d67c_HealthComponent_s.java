 package edu.csupomona.kyra.component.health;
 
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.state.StateBasedGame;

 import edu.csupomona.kyra.component.Component;
 
 public abstract class HealthComponent extends Component {
 	
 	int currHealth;
 	final int maxHealth;
 	boolean vulnerable, badHit, goodHit;
 	
 	public HealthComponent(String id, int health) {
 		super(id);
 		this.currHealth = health;
 		this.maxHealth = health;
 		vulnerable = true;
 		badHit = false;
 	}
 	
 	public int getCurrHealth() {
 		return currHealth;
 	}
 	
 	public int getMaxHealth() {
 		return maxHealth;
 	}
 	
 	public boolean getBadHit() {
 		if (badHit) {
 			badHit = false;
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean getGoodHit() {
 		if (goodHit) {
 			goodHit = false;
 			return true;
 		}
 		return false;
 	}
 	
 	protected void addHealth(int health) {
 		if ((currHealth + health) > maxHealth)
 			currHealth = maxHealth;
 		else
 			currHealth += health;
 		System.out.println(owner.getId() + "Health: " + currHealth + "/" + maxHealth + ".");
 	}
 	
 	public boolean isVulnerable() {
 		return vulnerable;
 	}
 	
 	protected void makeInvulnerable() {
 		vulnerable = false;
 	}
 	
 	protected void makeVulnerable() {
 		vulnerable = true;
 	}
 	
 	protected void setBadHit() {
 		badHit = true;
 	}
 	
 	protected void setGoodHit() {
 		goodHit = true;
 	}
 	
 	public boolean zeroHealth() {
 		return currHealth <= 0;
 	}
 }
