 package org.spellcraft;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 
 import org.bukkit.entity.Player;
 import org.spellcraft.castable.Spell;
 
 public class SpellBook {
 	private static SpellCraft plugin;
 	private Player player;
 	
 	public SpellBook(Player playerinstance, SpellCraft instance)
 	{
 		plugin = instance;
 		player = playerinstance;
 		
 		for(Class<?> spellClass : plugin.getSpellList())
 		{
 			Spell newSpell;
 			try {
 				newSpell = (Spell) spellClass.getConstructor(SpellCraft.class,Player.class).newInstance(plugin,player);
 			} catch (IllegalArgumentException e) {
 				newSpell = null;
 				e.printStackTrace();
 			} catch (SecurityException e) {
 				newSpell = null;
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				newSpell = null;
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				newSpell = null;
 				e.printStackTrace();
 			} catch (InvocationTargetException e) {
 				newSpell = null;
 				e.printStackTrace();
 			} catch (NoSuchMethodException e) {
 				newSpell = null;
 				e.printStackTrace();
 			}
 			if(newSpell != null)
 			{
 			registerSpell(newSpell);
 			}
 			
 		}
 	}
 	
 	private ArrayList<Spell> spellRegistry = new ArrayList<Spell>();
 	private int index = 0; // The current spell.
 	
 	public ArrayList<Spell> getRegistry() { return spellRegistry; }
 	
 	private void registerSpell(Spell spell)
 	{
 		spellRegistry.add(spell);
 	}
 	
 	public Spell getCurrentSpell() { return spellRegistry.get(index); }
 	
 	public void setCurrentSpell(Spell spell)
 	{
 		index = spellRegistry.indexOf(spell);
 	}
 	
 	public void nextSpell() // Scrolls to the next spell and notifies the player.
 	{
 		if (index!=spellRegistry.size()-1) {index++;} // If we're not on the last one, advance.
 		else { index = 0; } // Or go back to start.
 		if (!getCurrentSpell().usesTargeting())
 		{
 			plugin.getPlayerData(player).setTarget(null); // We lose targets when scrolling throuhg non-targeting spells.
 		}
 	}
 	
 	public Spell getSpell(String spellName)
 	{
 		for(Spell currentSpell : spellRegistry)
 		{
 			if(currentSpell.getShortName().equalsIgnoreCase(spellName))
 			{
 				return currentSpell;
 			}
 		}
 		return null; // If we didn't find anything that matched.
 	}
 	
 }
