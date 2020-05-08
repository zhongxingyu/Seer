 package org.spellcraft.castable;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.spellcraft.SpellCraft;
 
 public abstract class NonTargetingSpell extends AbstractSpell {
 
 
 
 	public NonTargetingSpell(Player playerinstance, SpellCraft instance, String name, String desc, ItemStack... items)
 	{
 		super(playerinstance, instance, name, desc, items);
 	}
	
 	@Override
 	public void callSpell()
 	{
 		if (hasRequiredItems())
 		{	
 				castSpell();
 		}
 		else
 		{
 			getPlayer().sendMessage("Could not cast! Spell requires" + printRequiredItems() + "!");
 		}	
 	}
 	
 	@Override
 	protected void castSpell(LivingEntity target)
 	{
 		getPlayer().sendMessage(ChatColor.DARK_RED + "Sorry, the SpellCraft API developer messed up."); // This is never going to be called.
 		getPlayer().sendMessage(ChatColor.DARK_RED + "Please contact them for a refund.");	}
 
 	@Override
 	protected abstract void castSpell();
 	
 	
 	
 }
