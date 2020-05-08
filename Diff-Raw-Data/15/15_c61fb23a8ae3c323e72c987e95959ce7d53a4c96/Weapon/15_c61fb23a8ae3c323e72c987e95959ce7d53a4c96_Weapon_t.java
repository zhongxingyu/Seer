 package weapons;
 
 
 import org.newdawn.slick.geom.Shape;
 import java.util.ArrayList;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.geom.Rectangle;
 
 import dudes.Dude;
 
 /**
  * Class for a weapon. Keeps track of attack pattern, image, and damage.
  * @author msalvato
  *
  */
 public abstract class Weapon {
 	public Image weaponSprite;
 	Image attackSprite;
 	public int damage;
 	int attackWidth;
 	int attackHeight;
 	public float attackTime;
 	public int delayTime;
 	public ArrayList<Attack> attacks;
 	Dude owner;
 	public float x;
 	public float y;
 	
 	Weapon(){
 		attacks = new ArrayList<Attack>();
		attackTime = 200;
 	}
 
 	// to start attack
 	
 	public abstract void attack();
 		
 	public void assignOwner(Dude owner) {
 		this.owner = owner;
 	}
 	
 	public void drop() {
 		this.x = owner.pos[0];
 		this.y = owner.pos[1];
 		this.owner = null;
 		
 	}
 	
 	public Shape getHitBox() {
 		return new Rectangle(x, y, weaponSprite.getWidth(), weaponSprite.getHeight());
 	}
 	
 	public void Draw() {
 		if (this.owner == null) {
 			weaponSprite.draw(x, y);
 		} else {
 		}
 	}
 	// method for the movement of an attack based on current info.
 	// kinda state machine-y
 	protected abstract boolean updateAttack(Attack attack);
 	
 	public void updateAttacks(){
 		ArrayList<Attack> removals = new ArrayList<Attack>();
 		for(int i = 0; i < attacks.size(); i++){
 			if(!updateAttack(attacks.get(i))){
 				removals.add(attacks.get(i));
 			}
 		}
 		for(Attack attack:removals){
 			attacks.remove(attack);
 		}
 	}
 	
 	
 }
