 package demo;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Colony implements BattleAble {
 	
 	private Player owner = null;
 	private int income = 0;
 	private int damage = 0;
 	private int shield = 0;
 	private int variation = 0;
 	
 	public Colony (Resource resource, Player owner) {
 		this.owner = owner;
 		if (resource == Resource.METAL) {
 			this.income = 40;	
 		} else {
 			this.income = 20;
 		}
 		constructTurret();
 	}
 	
 	public void constructTurret() {
 		this.damage = 5;
 		this.shield = 30;
 		this.variation = 1;
 	}
 	
 	public int getIncome() {
 		return income;
 	}
 
 	@Override
 	public boolean takeDamage(int damage) {
 		shield -= damage;
 		return shield <= 0;
 	}
 
 	@Override
 	public List<Integer> getAttacks() {
 		List<Integer> damage = new ArrayList<Integer>();
		damage.add(this.damage + (int) (Math.random()*(variation+1)));
 		return damage;
 	}
 
 	public Player getOwner() {
 		return owner;
 	}
 	
 	public void resetShield() {
 		shield = 30;
 	}
 }
