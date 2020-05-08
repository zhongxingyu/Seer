 package warborn.model;
 
 public class Move extends Template {
 	
 	private Warborn model;
 	
 	public Move(Warborn model){
 		this.model = model;
 	}
 	
 	public void moveUnits(int units){
 		getFirstTerritory().setNbrOfUnits(getFirstTerritory().getNbrOfUnits()+getSecondTerritory().getNbrOfUnits() - units);
 		getSecondTerritory().setNbrOfUnits(units);
		this.resetTerritories();
 		model.changed();
 	}
 	
 }
