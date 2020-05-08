 package warborn.model;
 
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Observer;
 
 //TestDriv skiten!!!!
 public class SpellInvoker implements Observer{
 	private Warborn model;
 	private ISpell selectedSpell;
 	private ArrayList<ISpell> activeSpells;
 
 	public SpellInvoker(Warborn model) {
 		this.activeSpells = new ArrayList<ISpell>();
 		this.model = model;
 		model.addObserver(this);
 	}
 	
 	public ISpell getSelectedSpell(){
 		return selectedSpell;
 	}
 	
 	public void setSelectedSpell(ISpell spell){
 		selectedSpell = spell;
 		if(spell.isInstant()){
 			invokeSpell(spell);
 		}
		model.setSpellLoaded(true);
 	}
 	
 	public void invokeSpell(ISpell spell){
 		if(spell.validTarget(model) && model.getCurrentPlayer().getMana() >= spell.getManaCost()){
 			spell.invoke(model);
 			if(spell.getTimer() > 0){
 				activeSpells.add(spell);
 			}
 		}
 		model.changed();
 		model.resetSelectedTerritory();
 		selectedSpell = null;
 	}
 
 	@Override
 	public void update(Observable o, Object arg) {
 		if(o.getClass()==Warborn.class && arg!=null){
 			if ((Integer)(arg)==1 && model.getState()==1){
 				for(int i = 0; i < activeSpells.size(); i++){
 					activeSpells.get(i).tick(model);
 					if(activeSpells.get(i).getTimer() <= 0){
 						activeSpells.remove(i);
 					}
 				}
 			}
 			if ((Integer)(arg)==0 && model.getSelectedTerritoryIndex()!=-1){
				if(selectedSpell != null && model.isSpellLoaded()){
 					invokeSpell(selectedSpell);
					model.setSpellLoaded(false);
 				}
 			}
 		}
 	}
 }
