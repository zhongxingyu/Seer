 package musician101.controlcreativemode.listeners;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import musician101.controlcreativemode.Config;
 import musician101.controlcreativemode.ControlCreativeMode;
 import musician101.controlcreativemode.lib.Constants;
 import musician101.controlcreativemode.util.CCMUtils;
 
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerBucketEmptyEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * Event handler for player events.
  * 
  * @author Musician101
  */
 public class PlayerListener implements Listener
 {
     ControlCreativeMode plugin;
     
     /**
      * @param plugin References the Main class.
      */
     public PlayerListener(ControlCreativeMode plugin)
     {
         this.plugin = plugin;
     }
 
     /**
      * Runs when a player attempts to empty a bucket.
      * 
      * @param event All info involved in the event.
      */
     @EventHandler
 	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event)
 	{
 		Player player = event.getPlayer();
 		Material material = event.getBucket();
 		if (player.getGameMode() == GameMode.CREATIVE)
 		{
 			if (material == Material.WATER_BUCKET && Config.blockWaterBucket)
 			{
 				if (!player.hasPermission(Constants.PERMISSION_ALLOW_BLOCK))
 				{
 					event.setCancelled(true);
 					player.sendMessage(Constants.NO_PERMISSION_PLACE);
 				}
 				else
 				{
 					CCMUtils.warnStaff(Constants.getBlockWarning(player, event.getBucket(), event.getBlockClicked().getLocation()));
 					plugin.getLogger().info(Constants.getBlockWarning(player, event.getBucket(), event.getBlockClicked().getLocation()));
 				}
 			}
 			else if (material == Material.LAVA_BUCKET && Config.blockLavaBucket)
 			{
 				if (!player.hasPermission(Constants.PERMISSION_ALLOW_BLOCK))
 				{
 					event.setCancelled(true);
 					player.sendMessage(Constants.NO_PERMISSION_PLACE);
 				}
 				else
 				{
 					CCMUtils.warnStaff(Constants.getBlockWarning(player, event.getBucket(), event.getBlockClicked().getLocation()));
 					plugin.getLogger().info(Constants.getBlockWarning(player, event.getBucket(), event.getBlockClicked().getLocation()));
 				}
 			}
 		}
 	}
     
     /**
      * Runs when a player drops an item.
      * 
      * @param event All info involved in the event.
      */
     @EventHandler
     public void onPlayerDropItem(PlayerDropItemEvent event)
     {
     	Player player = event.getPlayer();
     	Item item = event.getItemDrop();
     	List<Integer> ids = new ArrayList<Integer>(Config.noDrop);
     	if (player.getGameMode() == GameMode.CREATIVE)
     	{
     		/** 
 			 * Deprecated method ItemStack.getTypeId() in Bukkit.
 			 * Waiting for a proper alternative before fixing.
 			 */
     		if (!player.hasPermission(Constants.PERMISSION_ALLOW_DROP) && ids.contains(item.getItemStack().getTypeId()))
     		{
         		event.setCancelled(true);
         		player.sendMessage(Constants.NO_PERMISSION_DROP);
             }
     		/** 
 			 * Deprecated method ItemStack.getTypeId() in Bukkit.
 			 * Waiting for a proper alternative before fixing.
 			 */
         	else if (ids.contains(item.getItemStack().getTypeId()))
         	{
         		CCMUtils.warnStaff(Constants.getItemDropWarning(player, item, player.getLocation()));
         		plugin.getLogger().info(Constants.getItemDropWarning(player, item, player.getLocation()));
         	}
     	}
     }
     
     /**
      * Runs when a player left or right clicks.
      * 
      * @param event All info involved in the event.
      */
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event)
     {
     	Action action = event.getAction();
     	Block block = event.getClickedBlock();
     	ItemStack item = event.getItem();
         Player player = event.getPlayer();
         
         /** Setup lists for the various checks. */
     	List<Integer> invBlockIds = new ArrayList<Integer>(Config.noBlockBasedInventory);
     	List<Material> railIds = new ArrayList<Material>(Arrays.asList(Material.ACTIVATOR_RAIL, Material.DETECTOR_RAIL, Material.POWERED_RAIL, Material.RAILS));
     	List<Integer> throwableIds = new ArrayList<Integer>(Config.noThrow);
     	
     	/** Check if a player right clicks a block */
         if (action == Action.RIGHT_CLICK_BLOCK && player.getGameMode() == GameMode.CREATIVE)
         {
         	/** Block Based Inventory Check */
        	// TODO: Find solution to Shift + Right Clicking on inventory warning.
         	/** 
 			 * Deprecated method Block.getTypeId() in Bukkit.
 			 * Waiting for a proper alternative before fixing.
 			 */
        	if (invBlockIds.contains(block.getTypeId()))
         	{
         		if (!player.hasPermission(Constants.PERMISSION_ALLOW_OPEN_CHESTS))
         		{
         			event.setCancelled(true);
                 	player.sendMessage(Constants.NO_PERMISSION_INVENTORY);
         		}
        		else
         		{
         			CCMUtils.warnStaff(Constants.getBlockInteractWarning(player, block.getType(), block.getLocation()));
         			plugin.getLogger().info(Constants.getBlockInteractWarning(player, block.getType(), block.getLocation()));
         		}
         	}
         	/** TNT Minecart Check */
         	// TODO: Find solution to double warning post
         	else if (item.getType() == Material.EXPLOSIVE_MINECART && railIds.contains(block.getTypeId()) && Config.blockTNTMinecart)
         	{
         		if (!player.hasPermission(Constants.PERMISSION_ALLOW_BLOCK))
         		{
         			event.setCancelled(true);
         			player.sendMessage(Constants.NO_PERMISSION_PLACE);
         		}
         		else
         		{
         			CCMUtils.warnStaff(Constants.getCartWarning(player, item.getType(), player.getLocation()));
             		plugin.getLogger().info(Constants.getCartWarning(player, item.getType(), player.getLocation()));
         		}
         	}
         	/** Spawn Eggs Check */
         	else if (!isSpawnAllowed(item.getDurability()))
         	{
         		if (!player.hasPermission(Constants.PERMISSION_ALLOW_SPAWN))
         		{
         			event.setCancelled(true);
             		player.sendMessage(Constants.NO_PERMISSION_SPAWN);
         		}
         		else if (item.getType() == Material.MONSTER_EGG)
         		{
         			CCMUtils.warnStaff(Constants.getSpawnWarning(player, item.getDurability(), block.getLocation()));
         			plugin.getLogger().info(Constants.getSpawnWarning(player, item.getDurability(), block.getLocation()));
         		}
         	}
         	/** Throwable Items Check */
         	/** 
 			 * Deprecated method ItemStack.getTypeId() in Bukkit.
 			 * Waiting for a proper alternative before fixing.
 			 */
         	else if (throwableIds.contains(item.getTypeId()))
         	{
         		if (!player.hasPermission(Constants.PERMISSION_ALLOW_THROW))
         		{
         			event.setCancelled(true);
         			player.sendMessage(Constants.NO_PERMISSION_THROW);
         		}
         		else
             	{
             		CCMUtils.warnStaff(Constants.getThrownItemWarning(player, item, player.getLocation()));
             		plugin.getLogger().info(Constants.getThrownItemWarning(player, item, player.getLocation()));
             	}
         	}
         }
         /** 
          * Check if a player right clicks the air
          * Throwable Items Check
          */
         else if (action == Action.RIGHT_CLICK_AIR && player.getGameMode() == GameMode.CREATIVE)
     	{
         	/** 
 			 * Deprecated method ItemStack.getTypeId() in Bukkit.
 			 * Waiting for a proper alternative before fixing.
 			 */
         	if (!player.hasPermission(Constants.PERMISSION_ALLOW_THROW) && throwableIds.contains(item.getTypeId()))
         	{
         		event.setCancelled(true);
         		player.sendMessage(Constants.NO_PERMISSION_THROW);
         	}
         	/** 
 			 * Deprecated method ItemStack.getTypeId() in Bukkit.
 			 * Waiting for a proper alternative before fixing.
 			 */
         	else if (throwableIds.contains(item.getTypeId()))
         	{
         		CCMUtils.warnStaff(Constants.getThrownItemWarning(player, item, player.getLocation()));
         		plugin.getLogger().info(Constants.getThrownItemWarning(player, item, player.getLocation()));
         	}
     	}
     }
     
     /**
      * Runs when a player right clicks an entity.
      * 
      * @param event All info involved in the event.
      */
     @EventHandler
     public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
     {
     	Entity entity = event.getRightClicked();
     	Player player = event.getPlayer();
     	
     	/** List of entities from the plugin's config. */
     	List<String> entities = new ArrayList<String>(Config.noEntityBasedInventory);
     	
     	/** Entity based inventory check. */
     	if (player.getGameMode() == GameMode.CREATIVE && entities.contains(entity.getType().toString().toLowerCase()) && !player.hasPermission(Constants.PERMISSION_ALLOW_OPEN_CHESTS))
     	{
     		event.setCancelled(true);
     		player.sendMessage(Constants.NO_PERMISSION_INVENTORY);
     	}
     	else if (entities.contains(entity.getType().toString().toLowerCase()))
     	{
     		CCMUtils.warnStaff(Constants.getEntityInteractWarning(player, entity.getType(), entity.getLocation()));
     		plugin.getLogger().info(Constants.getEntityInteractWarning(player, entity.getType(), entity.getLocation()));
     	}
     }
     
     /**
      * Check config for noSpawn.
      * 
      * @param data Damage value of the spawn egg used.
      * @return True if the mob is not specified in the config.
      */
     public boolean isSpawnAllowed(short data)
     {
     	boolean allowed = true;
     	String mob = "";
     	if (data == 50) mob = "creeper";
     	else if (data == 51) mob = "skeleton";
    	else if (data  == 52) mob = "spider";
 		else if (data == 54) mob = "zombie";
 		else if (data == 55) mob = "slime";
 		else if (data == 56) mob = "ghast";
 		else if (data == 57) mob = "zombiePigman";
 		else if (data == 58) mob = "enderman";
 		else if (data == 59) mob = "caveSpider";
 		else if (data == 60) mob = "silverfish";
 		else if (data == 61) mob = "blaze";
 		else if (data == 62) mob = "magmaCube";
 		else if (data == 65) mob = "bat";
 		else if (data == 66) mob = "witch";
 		else if (data == 90) mob = "pig";
 		else if (data == 91) mob = "sheep";
 		else if (data == 92) mob = "cow";
 		else if (data == 93) mob = "chicken";
 		else if (data == 94) mob = "squid";
 		else if (data == 95) mob = "wolf";
 		else if (data == 96) mob = "mooshroom";
 		else if (data == 97) mob = "snowGolem";
 		else if (data == 98) mob = "ocelot";
 		else if (data == 99) mob = "ironGolem";
 		else if (data == 120) mob = "villager";
     	
     	List<String> noSpawn = new ArrayList<String>(Config.noSpawn);
     	if (noSpawn.contains(mob))
     		allowed = false;
     	
     	return allowed;
     }
 }
