 package warborn.model.spells;
 
 import warborn.model.Warborn;
 
 public class PurpleRitual extends Spell {
 
 	public PurpleRitual(int mana) {
 		super(mana);
 	}
 
 	@Override
 	public boolean validTarget(Warborn model) {
 		return (model.getNbrOfReinforcements() <= 4) && (model.getSelectedTerritory().getOwner() != model.getCurrentPlayer()) && (model.getState() == 1);
 	}
 
 	@Override
 	public String getName() {
 		return "Purple Ritual";
 	}
 
 	@Override
 	public String getDescription() {
 		return "Sacrifice your remaining reiforcements and destroy twice as many troops in target enemy territory to a minimum of 1. \n " +
 				"Usable only during your reinforcement phase and you have more than 4 reinforcements \n \n \"You're of more use to me this way\" ";
 	}
 
 	@Override
 	public void invoke(Warborn model) {
		if(model.getSelectedTerritory().getNbrOfUnits() <= model.getNbrOfReinforcements()*2){
			model.getSelectedTerritory().setNbrOfUnits(1);
		}
		else{
			model.getSelectedTerritory().decrementUnits(model.getNbrOfReinforcements()*2);		
		}
		model.setNbrOfReinforcements(0);
	}	
 
 }
