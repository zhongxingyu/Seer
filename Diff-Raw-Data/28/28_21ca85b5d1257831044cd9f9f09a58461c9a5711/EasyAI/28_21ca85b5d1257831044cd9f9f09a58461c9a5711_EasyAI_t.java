 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 
//Class Simple AI
 public class EasyAI extends ComputerPlayer{
 	
 	Random r;
	private boolean attackedThisTurn;
 	public EasyAI(GameManager gm){
 		super(gm);
 		r = new Random();
		attackedThisTurn = false;
 	}
 	
 	public HashMap<Territory, Integer> reinforceProcess() {
		HashMap<Territory, Integer> map = new HashMap<Territory, Integer>();
		for(int i=0; i<territories.size(); i++) {
			map.put(territories.get(i), 0);
		}
		for(int i=remainingReinforcements; i>0; i--) {
			Territory randomTerritory = territories.get((int)(Math.random()*territories.size()));
			map.put(randomTerritory, map.get(randomTerritory)+1);
 		}
		return map;
 	}
 
 	
 	protected ArrayList<Territory> askReinforcements(int numReinforcements) {
 		//generates a list of territories that the AI wants to reinforce
 		if(this.hasCardSet() == true){
 			this.turnInCards();
 		}
 		
 		int i = r.nextInt(this.territories.size());
 		ArrayList<Territory> t = new ArrayList<Territory>();
 		for(int j = 0; j < i && j < numReinforcements; j++){
 			t.add(this.territories.get(j));
 		}
 		return t;
 	}
 
 	
 	public Territory askInitReinforce() {
 		// gets a random territory to reinforce at the beginning of the game
 		Territory t = null;
 		int i = r.nextInt(manager.getBoard().getTerritories().size());
 		
 		do{
 			t = manager.getBoard().getTerritories().get(i);
 		}
 		while(t.getOwner() != null);
 		return t;
 	}
 
 	
 	public boolean attackProcess() {
 		//comprises the entire attack process
 		
 		int i = r.nextInt(this.getTerritories().size());
 		Territory from = this.territories.get(i);
 		
 		if(this.canAttack()){
 			while((manager.getBoard().getConnections(from).size()) == 0){
 				i = r.nextInt(this.getTerritories().size());
 				from = this.territories.get(i);
 			}
 			Territory to = this.territories.get(r.nextInt(this.territories.size()));
			return manager.attack(from, to, r.nextInt(from.getTroops()-1));
 		}
 		return false;
 		
 	}
 
 	
 	public void moveProcess() {
 		//makes a random troop movement (random # of troop between 2 random territories)
 		
 		int i = r.nextInt(this.getTerritories().size());
 		Territory from = this.getTerritories().get(i);
 		int j = r.nextInt(this.getTerritories().size());
 		Territory to;
 		if(i != j){
 			to = this.getTerritories().get(j);
 		}
 		else{
 			j++;
 			to = this.getTerritories().get(j);
 		}
 		this.move(from, to, r.nextInt(from.getTroops()-1));
 	}
 
 }
