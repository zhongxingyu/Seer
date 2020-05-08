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
 		System.out.println(model.getBattle().shallMove() + "");
 		if(!model.getBattle().shallMove()){
 			this.resetTerritories();
 			model.nextPhase();
			model.nextState();
 		}else{
 			model.getBattle().shallNotMove();
 		}
 		model.changed();
 	}
 	
 }
