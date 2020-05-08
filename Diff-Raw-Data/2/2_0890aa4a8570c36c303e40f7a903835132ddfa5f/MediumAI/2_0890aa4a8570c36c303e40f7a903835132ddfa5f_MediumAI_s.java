 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 
 /*medium AI, targeted attacks, random reinforcement*/
 public class MediumAI extends ComputerPlayer{
 	
 	Random r;
 	public MediumAI(GameManager gm){
 		super(gm);
 		r = new Random();
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
 		
 		int i = r.nextInt(territories.size());
 		ArrayList<Territory> t = new ArrayList<Territory>();
 		for(int j = 0; j < i && j < numReinforcements; j++){
 			t.add(territories.get(j));
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
 		int ntroops = 0;
 		if(this.bestStage().getTroops() > 3){
 			ntroops = 3;
 		}
 		else if(bestStage().getTroops() > 1 && bestStage().getTroops() < 3){
 			ntroops = bestStage().getTroops() - 1;
 		}
 		
 		return manager.attack(bestStage(), bestTarget(bestStage()), (bestStage().getTroops() -1));
 	}
 
 	
 	public void moveProcess() {
 		//makes a random troop movement (random # of troop between 2 random territories)
 		
 		int i = r.nextInt(getTerritories().size());
 		Territory from = getTerritories().get(i);
 		int j = r.nextInt(getTerritories().size());
 		Territory to;
 		if(i != j){
 			to = getTerritories().get(j);
 		}
 		else{
 			j++;
 			to = getTerritories().get(j);
 		}
 		this.move(from, to, r.nextInt(from.getTroops()-1));
 	}
 	
 	public Territory bestTarget(Territory t){
 		int min = 1000;
 		Territory best = null;
 		ArrayList<Territory> connect = (ArrayList<Territory>) manager.getBoard().getConnections(t);
		for(int j = 1; j < connect.size(); i++){
 			if(connect.get(j).getTroops() < min && connect.get(j).getOwner() != this);
 			min = connect.get(j).getTroops();
 			best = connect.get(j);
 		}
 		return best;
 	}
 	
 	public Territory bestStage(){
 		//gets the territory with the most troops
 		int max = 0;
 		Territory best = null;
 		for(int i = 0; i < territories.size(); i++){
 			if(territories.get(i).getTroops() > max){
 				max = territories.get(i).getTroops();
 				best = territories.get(i);
 			}
 		}
 		return best;
 	}
 	
 	public boolean continueAttack(int remaining, Object[] attack){
 		if(remaining < 2){
 			return false;
 		}
 		else{
 			return true;
 		}
 	}
 }
