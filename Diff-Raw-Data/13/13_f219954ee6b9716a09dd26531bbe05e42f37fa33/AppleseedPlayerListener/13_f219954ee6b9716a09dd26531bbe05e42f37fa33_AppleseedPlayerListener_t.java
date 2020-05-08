 package redsgreens.Appleseed;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * Handle events for all Player related events
  * @author redsgreens
  */
 public class AppleseedPlayerListener extends PlayerListener {
 
 	Appleseed Plugin;
 	
     public AppleseedPlayerListener(Appleseed plugin) 
     {
     	Plugin = plugin;
     }
 
     @Override
     public void onPlayerInteract(PlayerInteractEvent event)
     // catch player right-click events
     {
     	// return if the event is already cancelled, or if it's not a right-click event
 		if(event.isCancelled() || event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
 
 		Block block = event.getClickedBlock();
 		Material blockType = block.getType();
 		
 		// return if the player didn't right click on farmland or tree
 		if(blockType != Material.SOIL && blockType != Material.LOG)
 			return;
 
 		Player player = event.getPlayer();
 		ItemStack iStack = player.getItemInHand();
 
 		if(iStack == null)
 			return;
 
 		if(blockType == Material.SOIL)
 		{
 			// they might have planted something, do some more checks
 
 			// try to get the type of the tree they are planting
 			AppleseedTreeType treeType = null;
 			ItemStack tmpIS = new ItemStack(iStack.getType(), 1, iStack.getDurability());
 			if(Appleseed.Config.TreeTypes.containsKey(tmpIS))
 				treeType = Appleseed.Config.TreeTypes.get(tmpIS);
 			else
 			{
 				ItemStack tmpIS2 = new ItemStack(iStack.getType(), 1);
 				if(Appleseed.Config.TreeTypes.containsKey(tmpIS2))
 					treeType = Appleseed.Config.TreeTypes.get(tmpIS2);
 			}
 			
 			// return if they don't have an allowed item in hand
 			if(treeType == null)
 				return;
 			
 			// return if the block above is not air
 			Block blockRoot = block.getRelative(BlockFace.UP);
 			if(blockRoot.getType() != Material.AIR)
 				return;
 			
 			// return if they don't have permission
			if(iStack.getType() == Material.INK_SACK && iStack.getDurability() == 3)
 			{
				if(!Appleseed.Permissions.hasPermission(player, "plant.cocoa_beans"))
				{
					event.getPlayer().sendMessage("cErr: You don't have permission to plant this tree.");
					return;
				}
 			}
			else if(!Appleseed.Permissions.hasPermission(player, "plant." + iStack.getType().name().toLowerCase()))
 			{
 				event.getPlayer().sendMessage("cErr: You don't have permission to plant this tree.");
 				return;
 			}
 			
 			// all tests satisfied, proceed
 			
 			// cancel the event so we're the only one processing it
 			event.setCancelled(true);
 			
 			// add the root location and type to the list of trees
 			if(Appleseed.Permissions.hasPermission(player, "infinite.plant"))
 				Appleseed.TreeManager.AddTree(blockRoot.getLocation(), new ItemStack(iStack.getType(), 1, iStack.getDurability()), -1, player.getName());
 			else
 				Appleseed.TreeManager.AddTree(blockRoot.getLocation(), new ItemStack(iStack.getType(), 1, iStack.getDurability()), player.getName());
 			
 			// set the clicked block to dirt
 			block.setType(Material.DIRT);
 			
 			// plant a sapling
 			blockRoot.setType(Material.SAPLING);
 			blockRoot.setData(treeType.getSaplingData());
 			
 			// take the item from the player
 			if(iStack.getAmount() == 1)
 				player.setItemInHand(null);
 			else
 			{
 				iStack.setAmount(iStack.getAmount() - 1);
 				player.setItemInHand(iStack);			
 			}
 		}
 		else if(blockType == Material.LOG && iStack.getType() == Material.INK_SACK && iStack.getDurability() == (short)15)
 		{
 			// they might be fertilizing a tree
 			
 			Location loc = block.getLocation();
 			if(!Appleseed.TreeManager.isTree(loc))
 				return;
 
 			if(Appleseed.Permissions.hasPermission(player, "infinite.fertilizer"))
 				Appleseed.TreeManager.ResetTreeDropCount(loc, -1);
 			else
 				Appleseed.TreeManager.ResetTreeDropCount(loc);
 			
 			// take the item from the player
 			if(iStack.getAmount() == 1)
 				player.setItemInHand(null);
 			else
 			{
 				iStack.setAmount(iStack.getAmount() - 1);
 				player.setItemInHand(iStack);			
 			}
 
 		}
 
 		
     }
 }
 
