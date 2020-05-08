 package uk.ac.aber.dcs.cs221.monstermash.data;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import org.json.JSONException;
 
 public class Battle {
 	private volatile Monster defender = null;
 	private volatile Monster challenger = null;
 	private volatile boolean valid = false;
 	private volatile Monster winner = null;
 	private volatile List<String> log;
 	
 	public Battle(Monster defender, Monster challenger) {
 		this.defender = defender;
 		this.challenger = challenger;
 		log = new LinkedList<String>();
 		
 		if (defender.getHealth() <= 0
 				|| challenger.getHealth() <= 0) {
 			return;
 		}
 		
 		fight();
 		valid = true;
 	}
 	
 	private void fight() {
 		
 		class CombatStats {
 			public int evade,toughness,strength,hitpoints;
 			public String name;
 		}
 		
 		CombatStats[] monsters = new CombatStats[2];
 		monsters[0] = new CombatStats();
 		monsters[1] = new CombatStats();
 		
 		monsters[0].evade = challenger.getEvade();
 		monsters[0].toughness = challenger.getToughness();
 		monsters[0].strength = challenger.getStrength();
		monsters[0].hitpoints = monsters[0].toughness>=1? monsters[0].toughness: 1;
 		monsters[0].name = challenger.getName();
 		
 		monsters[1].evade = defender.getEvade();
 		monsters[1].toughness = defender.getToughness();
 		monsters[1].strength = defender.getStrength();
		monsters[1].hitpoints = monsters[1].toughness>=1? monsters[1].toughness: 1;
 		monsters[1].name = defender.getName();
 		
 		Random prng = new Random();
 		boolean challengerWon = false;
 		
 		while (true) {
 			// Pick a random attacker.
 			int attacker = prng.nextBoolean()? 1:0;
 			int defender = attacker == 1? 0:1;
 			// The other monster gets a chance to evade.
 			if (prng.nextInt()%100 < monsters[defender].evade) {
 				log.add(monsters[attacker].name+" lunged for "+monsters[defender].name+" but missed.");
 				continue;//The attack misses.
 			}
 			// Calculate damage reduction from Toughness.
 			int resistance = monsters[defender].toughness>99? 99:
 				monsters[defender].toughness;
 			resistance = (1-resistance);
			// Random multiplier.
 			double multiplier = Math.abs(prng.nextGaussian() );
			//Calculate damage
 			int damage = (int) (monsters[attacker].strength * resistance * multiplier);
			//Minimum damage is 1.
			damage = damage>=1? damage: 1;
 			monsters[defender].hitpoints -= damage;
 			log.add(monsters[attacker].name+" struck "+monsters[defender].name+" for "+damage+" points of damage!");
 			
 			//Check to see if the defender was knocked out.
 			if (monsters[defender].hitpoints < 1) {
 				log.add(monsters[defender].name+" was killed!");
 				// Set boolean indicating winner.
 				if (defender != 0) {
 					challengerWon = true;
 				}
 				break;//End the fight.
 			}
 			//Continue until one of the two monsters is killed.
 		}
 		
 		//Reap the killed monster.
 		if (challengerWon) {
 			this.defender.reap();
 			this.challenger.checkForInjury(prng);
 			this.challenger.getOwner().deductCash(-this.defender.worth() );
 		} else {
 			this.challenger.reap();
 			this.defender.checkForInjury(prng);
 			this.defender.getOwner().deductCash(-this.challenger.worth() );
 		}
 	}
 	
 	public boolean isValid() {
 		return this.valid;
 	}
 	
 	public Monster getWinner() {
 		return this.winner;
 	}
 	
 	public synchronized String[] getLog() {
 		return log.toArray(new String[1]);
 	}
 
 }
