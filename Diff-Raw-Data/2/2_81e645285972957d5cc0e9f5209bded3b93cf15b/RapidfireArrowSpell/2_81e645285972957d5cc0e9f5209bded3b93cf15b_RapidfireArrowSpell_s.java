 package aor.SimplePlugin.Spells;
 
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.Material;
 import org.bukkit.Location;
 import org.bukkit.util.Vector;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Arrow;
 
 import aor.SimplePlugin.SimplePlugin;
 import aor.SimplePlugin.Spell;
 
 
 
 public class RapidfireArrowSpell extends Spell {
 
 
 	public static SimplePlugin plugin;
 
 	private static final int MAXDISTANCE = 200; // Sets the maximum distance.
 
 
 	public RapidfireArrowSpell() // Constructor.
 	{
 		spellName = "Rapidfire Arrow";
 		spellDescription = "Quickly fires off eight arrows. Needs four redstone.";
 	}
 
 
 	public void castSpell(Player player)
 	{
 		PlayerInventory inventory = player.getInventory();
 
 		// REQUIRED ITEMS
 		ItemStack[] requiredItems = new ItemStack[2]; // The requireditems itemstack.
 		requiredItems[0] = new ItemStack(Material.ARROW, 8); // We need 8 arrows.
 		requiredItems[1] = new ItemStack(Material.REDSTONE, 4); // We need 2 redstone.
 		// REQUIRED ITEMS
 
 		if (checkInventoryRequirements(inventory, requiredItems))
 		{
 			removeRequiredItemsFromInventory(inventory, requiredItems); // Remove the items.
 			player.shootArrow();
 			for (int i = 0; i < 7; i++) // Fire off 7 more.
 			{
				try { Thread.sleep(150); }						//Question: Is this really a good idea? Do we know if all the spells run on the same thread? Do all instances of this spell run on the same one? This could be basically causing the plugin to stop and start over and over again... Shouldn't we be making a new thread for each player's spellbook or something? or running it in the thread that already exists for each player? -Josh
 				catch(InterruptedException ae)
 				{ System.out.println(ae); } // If sleep fails
 				
 				player.shootArrow();
 			}
 
 			player.sendMessage("Rapidfire!"); // They have the proper items.
 
 		}
 
 
 		else { player.sendMessage("Could not cast! Spell requires 4 redstone, 8 arrow!"); } // They don't have the proper items.
 
 	}
 
 
 }
