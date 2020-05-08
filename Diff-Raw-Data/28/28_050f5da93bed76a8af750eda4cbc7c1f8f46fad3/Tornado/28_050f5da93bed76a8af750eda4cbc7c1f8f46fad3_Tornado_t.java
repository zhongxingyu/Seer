 package aor.Spells.Spells;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.Vector;
 import org.bukkit.Material;
 
 import aor.Spells.SpellsMain;
 import aor.Spells.Spell;
 
 
 public class Tornado extends Spell {
 	
 	public Player globalPlayer;
 	
 	public static SpellsMain plugin;
 	
 	public Tornado(SpellsMain instance) // Constructor.
 	{
 		plugin = instance;
 		spellName = "Tornado";
 		spellDescription = "Flings mobs around the player into the air.";
 		ArrayList<ItemStack> requiredItems=new ArrayList<ItemStack>();
 		requiredItems.add(new ItemStack(Material.WATER_BUCKET, 1));
 		requiredItems.add(new ItemStack(Material.REDSTONE, 4));
 		setRequiredItems(requiredItems);
 		shortName = "Tornado";// We need 1 bucket of water and 4 redstone.
 	}
 	
 	
 	public void castSpell (Player player)
 	{
 		Random gen = new Random();
 		globalPlayer = player;
 		PlayerInventory inventory = player.getInventory();
 
 		if (removeRequiredItemsFromInventory(inventory))
 		{
 			List<Entity> nearbyEntities;
 			nearbyEntities = player.getNearbyEntities(5,5,5); //Selects entities near the player within a 10x10x10 cube.
 			for (int i=0; i<nearbyEntities.size(); i++) {
				Vector newVelocity = new Vector(((gen.nextFloat() - .3) * .8), gen.nextFloat() * 20, (gen.nextFloat() - .3) * .8); //Generate a random vector
 				nearbyEntities.get(i).setVelocity(newVelocity);
 			}
 			
			for (int i = 200; i < 16; i = i+4){
 				delayedRun(i,0);
 			}
 		}
 		
 		else {
 			player.sendMessage("Could not cast! Spell requires 1 water bucket and 4 redstone.");
 			} // They don't have the proper items.
 		
 	}
 
 
 	public void run(Object... argument){
 		Player player = globalPlayer;
 		Random gen = new Random();
 		List<Entity> nearbyEntities;
 		nearbyEntities = player.getNearbyEntities(5,5,5); //Selects entities near the player within a 10x10x10 cube.
 		for (int i=0; i<nearbyEntities.size(); i++) {
			Vector newVelocity = new Vector(((gen.nextFloat() - .5) * .8), gen.nextFloat() * 5, (gen.nextFloat() - .5) * .8); //Generate a random vector
 			nearbyEntities.get(i).setVelocity(newVelocity);
 		}
 
 	}
 	
 }
