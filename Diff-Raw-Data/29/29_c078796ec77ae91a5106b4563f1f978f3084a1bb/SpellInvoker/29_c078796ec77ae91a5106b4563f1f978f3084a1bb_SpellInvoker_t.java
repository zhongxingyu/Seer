 package warborn.model.spells;
 
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Observer;
 
 public class SpellInvoker implements Observer{
 	private SpellTargetable target;
 	private Spell selectedSpell;
 	private ArrayList<Spell> activeSpells;
 
 	public SpellInvoker(SpellTargetable model) {
 		this.activeSpells = new ArrayList<Spell>();
 		this.target = model;
 		model.addObserver(this);
 	}
 	
 	public Spell getSelectedSpell(){
 		return selectedSpell;
 	}
 	
 	public void setSelectedSpell(Spell spell){
 		selectedSpell = spell;
 		if(spell.isInstant()){
 			invokeSpell(spell);
 		}else{
 			target.setSpellLoaded(true);
 		}
 	}
 	
 	public void invokeSpell(Spell spell){
 		if(spell.validTarget(target) && target.getCurrentPlayer().getMana() >= spell.getManaCost()){
 			target.getCurrentPlayer().changeMana(-(spell.getManaCost()));
 			spell.invoke(target);
 			target.getCurrentPlayer().setSpellCasted(true);
 			if(spell.getTimer() > 0){
 				activeSpells.add(spell);
 			}
 		}
 		target.changed();
		target.resetSelectedTerritory();
 		selectedSpell = null;
 	}
 
 	@Override
 	public void update(Observable o, Object arg) {
 		if(o.getClass()==target.getClass() && arg!=null){
 			if ((Integer)(arg)==1 && target.getState()==1){
 				for(int i = 0; i < activeSpells.size(); i++){
 					activeSpells.get(i).tick(target);
 					if(activeSpells.get(i).getTimer() <= 0){
 						activeSpells.remove(i);
 					}
 				}
 			}else if ((Integer)(arg)==0 && target.getSelectedTerritoryIndex()!=-1){
 				if(selectedSpell != null && target.isSpellLoaded()){
 					invokeSpell(selectedSpell);
 					target.setSpellLoaded(false);
 				}
 			}
 		}
 	}
 }
