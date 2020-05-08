 package com.censoredsoftware.demigods.item.divine.book;
 
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.Recipe;
 import org.bukkit.inventory.ShapelessRecipe;
 
 import com.censoredsoftware.demigods.Demigods;
 import com.censoredsoftware.demigods.item.DivineItem;
 import com.censoredsoftware.demigods.structure.Structure;
 import com.censoredsoftware.demigods.structure.global.Altar;
 import com.censoredsoftware.demigods.util.Items;
 
 public class BookOfPrayer implements DivineItem.Item
 {
 	@Override
 	public ItemStack getItem()
 	{
 		return Items.create(Material.BOOK, ChatColor.AQUA + "" + ChatColor.BOLD + "Book of Prayer", new ArrayList<String>()
 		{
 			{
 				add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Right click to teleport to the nearest Altar.");
 				add(" ");
 				add(ChatColor.RED + "Consumed on use.");
 			}
 		}, null);
 	}
 
 	@Override
 	public Recipe getRecipe()
 	{
 		ShapelessRecipe recipe = new ShapelessRecipe(getItem());
 		recipe.addIngredient(1, Material.NETHER_STAR);
 		recipe.addIngredient(2, Material.BOOK);
 		return recipe;
 	}
 
 	@Override
 	public Listener getUniqueListener()
 	{
 		return new Listener();
 	}
 
 	class Listener implements org.bukkit.event.Listener
 	{
 		@EventHandler(priority = EventPriority.HIGH)
 		private void onRightClick(PlayerInteractEvent event)
 		{
 			if(Demigods.MiscUtil.isDisabledWorld(event.getPlayer().getLocation())) return;
 
 			// Define variables
 			Player player = event.getPlayer();
 
 			if(event.getAction().equals(Action.RIGHT_CLICK_AIR) && player.getItemInHand().equals(BookOfPrayer.this.getItem()))
 			{
 				// Find and teleport to the nearest Altar
 				if(Altar.Util.isAltarNearby(player.getLocation()))
 				{
 					Structure save = Altar.Util.getAltarNearby(player.getLocation());
 					player.teleport(save.getReferenceLocation().clone().add(2.0, 1.5, 0));
 					player.sendMessage(ChatColor.YELLOW + "Teleporting to the nearest Altar...");
 					player.getWorld().strikeLightningEffect(player.getLocation());
 				}
 				else player.sendMessage(ChatColor.YELLOW + "No Altar found!");
 
 				// Consume the book either way
				player.getInventory().setItemInHand(new ItemStack(Material.AIR));
 			}
 		}
 	}
 }
