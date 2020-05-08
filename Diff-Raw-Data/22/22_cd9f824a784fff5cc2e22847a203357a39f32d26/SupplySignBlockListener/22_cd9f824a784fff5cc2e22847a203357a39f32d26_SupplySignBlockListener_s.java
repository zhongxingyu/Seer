 package redsgreens.SupplySign;
 
 import java.util.ArrayList;
 
 import org.bukkit.Material;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.block.*;
 import org.bukkit.event.block.*;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * SupplySign block listener
  * @author redsgreens
  */
 public class SupplySignBlockListener implements Listener {
 
 	private final SupplySign Plugin;
 	
     public SupplySignBlockListener(final SupplySign plugin) 
     { 
     	Plugin = plugin;
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockPlace(BlockPlaceEvent event) 
 	// this prevents a block from being placed against a SupplySign 
 	{
 		// return if the event is already cancelled
 		if (event.isCancelled()) return;
 
 		if(event.getBlockAgainst().getType() == Material.WALL_SIGN || event.getBlockAgainst().getType() == Material.SIGN_POST)
 		{
 			Sign sign = (Sign)event.getBlockAgainst().getState();
 			if (sign.getLine(0).equals("1[Supply]")){
 				event.setCancelled(true);
 				return;
 			}
 		}
 	}
 
     @EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockBreak(BlockBreakEvent event)
 	// only allow players with permission to break a SupplySign or a chest/dispenser with one attached
 	{
 		// return if the event is already cancelled
 		if (event.isCancelled()) return;
 		
 		if(event.getBlock().getType() == Material.WALL_SIGN || event.getBlock().getType() == Material.SIGN_POST)
 		{
 			Sign sign = (Sign)event.getBlock().getState();
 			if (sign.getLine(0).equals("1[Supply]") && !Plugin.isAuthorized(event.getPlayer(), "destroy")){
 				event.setCancelled(true);
 				return;
 			}
 		}
 		else if (event.getBlock().getType() == Material.CHEST || event.getBlock().getType() == Material.DISPENSER)
 		{
 			Sign sign = SupplySignUtil.getAttachedSign(event.getBlock());
 			if(sign != null)
 			{
 				if(!Plugin.isAuthorized(event.getPlayer(), "destroy")){
 					event.setCancelled(true);
 					return;
 				}
 			}
 		}
 
 	}
 
 	
     @EventHandler(priority = EventPriority.HIGHEST)
 	public void onSignChange(SignChangeEvent event)
 	// looks for a new SupplySign and tests it for validity
 	{
 		// return if the event is already cancelled
 		if (event.isCancelled()) return;
 
 		final Block signBlock = event.getBlock();
 
 		if(Plugin.Config.FixSignOnSignGlitch != SupplySignOnSign.Disabled){
 			// delete this sign if it's against another sign
 			Block blockAgainst = null;
 
 			if(signBlock.getType() == Material.SIGN_POST){
 				if(signBlock.getRelative(BlockFace.DOWN).getType() == Material.SIGN_POST || signBlock.getRelative(BlockFace.DOWN).getType() == Material.WALL_SIGN)
 					blockAgainst = signBlock.getRelative(BlockFace.DOWN);
 			}
 			else if(signBlock.getType() == Material.WALL_SIGN)
 				blockAgainst = SupplySignUtil.getBlockBehindWallSign((Sign)signBlock.getState());
 				
 			if(blockAgainst != null){
 				if(blockAgainst.getType() == Material.SIGN_POST || blockAgainst.getType() == Material.WALL_SIGN){
 					// the new sign is against another sign
 					Sign signAgainst = (Sign)blockAgainst.getState();
 					
 					// check the config file to make sure the sign should be deleted
 					if((Plugin.Config.FixSignOnSignGlitch == SupplySignOnSign.SupplySignOnly && signAgainst.getLine(0).equals("1[Supply]")) || Plugin.Config.FixSignOnSignGlitch == SupplySignOnSign.Global){
 						signBlock.setType(Material.AIR);
 						ItemStack signStack = new ItemStack(Material.SIGN, 1);
 						event.getPlayer().setItemInHand(signStack);
 						return;
 					}
 				}
 			}
 		}
 		
 		// done checking sign-on-sign bs, now on with setting up new signs
 		try
 		{
 			// only proceed if it's a new sign
 			if (event.getLine(0).equalsIgnoreCase("[Supply]") ||
 					event.getLine(0).equals("1[Supply]"))
 			{
 				// and they have create permission
 				if (Plugin.isAuthorized(event.getPlayer(), "create")){
 					
 					// they are allowed, continue
 					
 					// set the first line blue if it's not already
 					if(!event.getLine(0).equals("1[Supply]"))
 						event.setLine(0, "1[Supply]");
 
 					// if there is a chest nearby, then create a wallsign against it
 					if(SupplySignUtil.isValidChest(signBlock.getRelative(BlockFace.NORTH)) ||
 							SupplySignUtil.isValidChest(signBlock.getRelative(BlockFace.EAST)) ||
 							SupplySignUtil.isValidChest(signBlock.getRelative(BlockFace.SOUTH)) ||
 							SupplySignUtil.isValidChest(signBlock.getRelative(BlockFace.WEST))){
 
 						final String[] lines = event.getLines();
 
 						signBlock.setType(Material.WALL_SIGN);
 						if(SupplySignUtil.isValidChest(signBlock.getRelative(BlockFace.WEST)))
 							signBlock.setData((byte)5);
 						else if(SupplySignUtil.isValidChest(signBlock.getRelative(BlockFace.NORTH)))
 							signBlock.setData((byte)3);
 						else if(SupplySignUtil.isValidChest(signBlock.getRelative(BlockFace.EAST)))
 							signBlock.setData((byte)4);
 						else if(SupplySignUtil.isValidChest(signBlock.getRelative(BlockFace.SOUTH)))
 							signBlock.setData((byte)2);
 
 						Sign sign = (Sign)signBlock.getState();
 						sign.update(true);
 
 						Plugin.getServer().getScheduler().scheduleSyncDelayedTask(Plugin, new Runnable() {
 						    public void run() {
 
 						    	Sign sign = (Sign)signBlock.getState();
 						    	
 						    	for(int i=0; i<lines.length; i++)
 									sign.setLine(i, lines[i]);
 						    	
 						    	sign.update(true);
 						    }
 						}, 0);
 
 					}
 					// if it's a dispenser, put the sign there
 					else if(SupplySignUtil.isValidDispenser(signBlock.getRelative(BlockFace.NORTH)) ||
 							SupplySignUtil.isValidDispenser(signBlock.getRelative(BlockFace.EAST)) ||
 							SupplySignUtil.isValidDispenser(signBlock.getRelative(BlockFace.SOUTH)) ||
 							SupplySignUtil.isValidDispenser(signBlock.getRelative(BlockFace.WEST))){
 
 						final String[] lines = event.getLines();
 				    	Dispenser dispenser = null;
 
 						signBlock.setType(Material.WALL_SIGN);
 						if(SupplySignUtil.isValidDispenser(signBlock.getRelative(BlockFace.WEST))){
 							signBlock.setData((byte)5);
 							dispenser = (Dispenser)signBlock.getRelative(BlockFace.WEST).getState();
 						}
 						else if(SupplySignUtil.isValidDispenser(signBlock.getRelative(BlockFace.NORTH))){
 							signBlock.setData((byte)3);
 							dispenser = (Dispenser)signBlock.getRelative(BlockFace.NORTH).getState();
 						}
 						else if(SupplySignUtil.isValidDispenser(signBlock.getRelative(BlockFace.EAST))){
 							signBlock.setData((byte)4);
 							dispenser = (Dispenser)signBlock.getRelative(BlockFace.EAST).getState();
 						}
 						else if(SupplySignUtil.isValidDispenser(signBlock.getRelative(BlockFace.SOUTH))){
 							signBlock.setData((byte)2);
 							dispenser = (Dispenser)signBlock.getRelative(BlockFace.SOUTH).getState();
 						}
 
 						final Dispenser d = dispenser;
 						
 						Sign sign = (Sign)signBlock.getState();
 						sign.update(true);
 						
 						Plugin.getServer().getScheduler().scheduleSyncDelayedTask(Plugin, new Runnable() {
 						    public void run() {
 						    	
 						    	Sign sign = (Sign)signBlock.getState();
 
 						    	for(int i=0; i<lines.length; i++)
 									sign.setLine(i, lines[i]);
 
 						    	sign.update(true);
 
 								fillDispenser(d, sign);
 						    }
 						}, 0);
 
 					}
  
 
 				}
 				else{
 					// not allowed
 					if(Plugin.Config.ShowErrorsInClient)
 						event.getPlayer().sendMessage("cErr: Sign cannot be placed");
 					
 					signBlock.setType(Material.AIR);
 					signBlock.getWorld().dropItemNaturally(signBlock.getLocation(), new ItemStack(Material.SIGN, 1));
 				}
 				return;
 			}
 		}
 		catch (Throwable ex)
 		{
 			if(Plugin.Config.ShowErrorsInClient)
 				event.getPlayer().sendMessage("cErr: " + ex.getMessage());
 		}
 	}
 
 	// refill the dispenser after it fires
     @EventHandler(priority = EventPriority.MONITOR)
 	public void onBlockDispense(BlockDispenseEvent event)
 	{
 		if(event.isCancelled())
 			return;
 
		final Dispenser d = (Dispenser)event.getBlock().getState();
		final Sign s = SupplySignUtil.getAttachedSign(event.getBlock());

		if(s != null)
			Plugin.getServer().getScheduler().scheduleSyncDelayedTask(Plugin, new Runnable() {
			    public void run() {
					fillDispenser(d, s);
			    }
			}, 0);
 		
 	}
 	
 	private void fillDispenser(Dispenser dispenser, Sign sign){
 		try{
 			ArrayList<Object> itemList = new ArrayList<Object>();
 			
 			// test to see if it's a kit
 			if(sign.getLine(1).trim().contains("kit:")){
 				String[] split = sign.getLine(1).trim().split(":");
 				itemList = Plugin.Kits.getKit(split[1]);
 			}
 			else
 			{
 				// it's not a kit, so load the items from the lines on the sign
 				if(!sign.getLine(1).trim().equalsIgnoreCase(""))
 					itemList.add(sign.getLine(1).trim());
 				if(!sign.getLine(2).trim().equalsIgnoreCase(""))
 					itemList.add(sign.getLine(2).trim());
 				if(!sign.getLine(3).trim().equalsIgnoreCase(""))
 					itemList.add(sign.getLine(3).trim());
 			}
 
 			// if any valid items were found, fill the inventory with first item in itemList
 			if(itemList.size() > 0)
 			{
 				Inventory inv = dispenser.getInventory();
 				inv.clear();
 				
 				Integer max = itemList.size();
 
 				for(int x=0; x < 9; x++)
 				{
 					if(x < max)
 						inv.setItem(x, Plugin.Items.getItem(itemList.get(x).toString()));
 				}
 			}
 		}
 		catch(Exception ex){}
 		
 	}
 }
