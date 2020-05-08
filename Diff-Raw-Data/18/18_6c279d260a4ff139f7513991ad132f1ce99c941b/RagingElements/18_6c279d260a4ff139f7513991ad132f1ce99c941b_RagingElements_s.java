 package warborn.model.spells;
 
 import warborn.model.Warborn;
 
 public class RagingElements extends Spell {
 
 	public RagingElements(int mana) {
 		super(mana);
 	}
 
 	@Override
 	public boolean validTarget(Warborn model) {
 		return 	(model.getSelectedTerritory().getNbrOfUnits() > 1) && (model.getSelectedTerritory().getOwner() != model.getCurrentPlayer());
 	}
 
 	@Override
 	public String getName() {
 		return "Raging Elements";
 	}
 
 	@Override
 	public String getDescription() {
		return "Destroy 1 troop in target territory  \n \n \" Achtung enraged titan \" ";
 	}
 
 	@Override
 	public void invoke(Warborn model) {
 		model.getSelectedTerritory().decrementUnits(2);
 	}
 
 }
