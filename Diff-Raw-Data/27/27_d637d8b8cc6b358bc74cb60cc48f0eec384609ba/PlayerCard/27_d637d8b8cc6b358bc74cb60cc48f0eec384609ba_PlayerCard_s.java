 package RingMaster.Cards;
 
 import RingMaster.CardInstance;
 import RingMaster.CardType;
 
 /**
  * Author:      Grant Kurtz
  */
 public class PlayerCard extends Card {
 
 	private int hp;
 
 	public PlayerCard(CardInstance name, CardType type) {
 		super(name, type);
		hp = 20;
 	}
 
 	@Override
 	public Card clone() {
 		return null;
 	}
 
 	@Override
 	public int getAttackPower() {
 		return 1;
 	}
 
 	@Override
 	public int getDefensePower() {
 		return 3;
 	}
 
 	@Override
 	public void applyDamage(int dmg) {
 		hp -= dmg;
 	}
 
 	@Override
 	public int getHealth() {
 		return hp;
 	}
 
 	@Override
 	public boolean expired() {
 		return hp < 1;
 	}
 }
