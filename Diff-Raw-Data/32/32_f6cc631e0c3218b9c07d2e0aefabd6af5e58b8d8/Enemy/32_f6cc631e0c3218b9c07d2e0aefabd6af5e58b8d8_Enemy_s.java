package gameCharacter;
 
 import java.util.HashMap;
 
 import utils.Location;
 
 import com.golden.gamedev.Game;
 
 import app.RPGame;
 import attacks.AbstractAttack;
 
 public class Enemy extends AutomatedCharacter implements Attackable{
 
 	private HashMap<String, AbstractAttack> attacks = new HashMap<String, AbstractAttack>();
 	
 	public Enemy(RPGame game, Location loc, String configURL) {
 		super(game, loc, configURL);
 	}
 	
 	public void initAttacks() {
 		//TODO: JSON magic
 		throw new RuntimeException("Enemy initAttacks() undefined");
 	}
 	
 	public HashMap<String, AbstractAttack> getAttacks() {
 		return attacks;
 	}
 
 	public void attack(AbstractAttack at, long elapsedTime){
 		//TODO
 		throw new RuntimeException("Enemy attack() undefined");
 	}
 	@Override
 	public int getHealth() {
 		return getCounters().get("health").getCount();
 	}
 
 	@Override
 	public void addToHealth(int delta) {
 		getCounters().get("health").increase(delta);
 	}
 
 }
