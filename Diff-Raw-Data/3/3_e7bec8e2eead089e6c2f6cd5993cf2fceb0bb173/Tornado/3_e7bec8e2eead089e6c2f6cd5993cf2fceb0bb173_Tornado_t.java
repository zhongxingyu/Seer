 package aor.SimplePlugin.Spells;
 
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.Vector;
 import org.bukkit.Material;
 
 import aor.SimplePlugin.SimplePlugin;
 import aor.SimplePlugin.Spell;
 
 
 public class Tornado extends Spell {
 	
 	
 	public static SimplePlugin plugin;
 	
 	public Tornado(SimplePlugin instance) // Constructor.
 	{
 		plugin = instance;
 		spellName = "Tornado";
 		spellDescription = "Flings mobs around the player into the air.";
 		
 		setRequiredItems(new ItemStack(Material.WATER_BUCKET, 1), new ItemStack(Material.REDSTONE, 4)); // We need 1 bucket of water and 4 redstone.
 	}
 	
 	
 	public void castSpell (Player player)
 	{
 		Random gen = new Random();
 		PlayerInventory inventory = player.getInventory();
 
 		if (checkInventoryRequirements(inventory))
 		{
 			removeRequiredItemsFromInventory(inventory);
 			player.getInventory().addItem(new ItemStack(Material.BUCKET, 1));
 			List<Entity> nearbyEntities;
 			nearbyEntities = player.getNearbyEntities(5,5,5); //Selects entities near the player within a 10x10x10 cube.
 			for (int i=0; i<nearbyEntities.size(); i++) {
 				Vector newVelocity = new Vector((gen.nextFloat() * 1.5 - 0.75), gen.nextFloat() / 2.5 , gen.nextFloat() * 1.5 - 0.75); //Generate a random vector
 				nearbyEntities.get(i).setVelocity(newVelocity);
 			}
 		}
 		
 		else {player.sendMessage("Could not cast! Spell requires 1 water bucket and 4 redstone."); } // They don't have the proper items.
 		
 	}
 }
