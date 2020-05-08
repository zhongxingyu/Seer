 package se.chalmers.dat255.risk.model;
 
 import java.util.ArrayList;
 
 public class BonusHandler {
 
 	private int bonus, startingTroopNr, currentCardBonus;
 	private WorldMap worldMap;
 	
 	public BonusHandler(WorldMap worldMap, int numberOfPlayers){
 		this.worldMap = worldMap;
 		bonus = 0;
 		startingTroopNr = 50 - numberOfPlayers * 5;
 		currentCardBonus = 4;
 	}
 			
 	public void calcBonusesFromCards(ArrayList<String> names, Player activePlayer){
 		for(String name : names){
 			if(worldMap.getOwner(name) == activePlayer){
 				bonus += 2;
 			}
 		}
 		bonus += currentCardBonus;
 		currentCardBonus += 2; // Maybe needs to be changed later, may not be linear.
 	}
 
 	public void calcBonusUnits(Player activePlayer) {
 		int provinces = activePlayer.getNrOfProvinces();
 		if (provinces <= 9) {
 			this.bonus = 3;
 		} else {
 			this.bonus = provinces / 3;
 		}
 		
 		worldMap.updateBonus();
 		this.bonus += worldMap.getBonus(activePlayer);
		System.out.println("this is the worldMaps continentbnus = " + worldMap.getBonus(activePlayer));
 	}
 	
 	public int getBonus(){
 		return bonus;
 	}
 	
 	public void placeBonusUnits(int units, IProvince province) {
 		province.addUnits(units);
 		bonus = bonus - units;
 	}
 	
 	public void calcBonusForF0(int numberOfProvinces){
 		bonus = startingTroopNr - numberOfProvinces;		
 	}
 }
