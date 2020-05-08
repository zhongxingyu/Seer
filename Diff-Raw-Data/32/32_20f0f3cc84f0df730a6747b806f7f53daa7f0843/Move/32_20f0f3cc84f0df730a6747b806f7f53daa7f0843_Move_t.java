 package warborn.model;
 
 public class Move extends TerritoryInteractor {
 	
 	private Warborn model;
 	
 	public Move(Warborn model){
 		this.model = model;
 	}
 	
 	public void moveUnits(int units){
 		getFirstTerritory().setNbrOfUnits(
 				getFirstTerritory().getNbrOfUnits()+
 				getSecondTerritory().getNbrOfUnits() - units);
 		getSecondTerritory().setNbrOfUnits(units);
 		this.resetTerritories();
 		model.getBattle().resetTerritories();
 		if(!model.getBattle().shallMove()){
 			model.nextState();
 		}else{
 			model.getBattle().shallNotMove();
 		}
		model.nextPhase();
 		model.changed();
 	}
 	
 }
