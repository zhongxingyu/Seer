 package isnork.g3;
 
 import isnork.sim.SeaLife;
 import isnork.sim.Player;
 
 public class WaterProofSquare extends AbstractSquare{
 	private Set<SeaLife> creatures = Sets.newHashSet();
 	private Set<Player> divers = Sets.newHashSet();
 
 	void addCreature(SeaLife creature) {
 		creatures.add(creature);
 	}
 
 	void addDiver(Player diver) {
 		divers.add(diver);
 	}
 }
