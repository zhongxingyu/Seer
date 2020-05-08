 package com.pickaxehero.dragonshoard;
 
 import java.util.Random;
 
 
 import org.apache.commons.lang.Validate;
 import org.bukkit.Bukkit;
import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.world.ChunkLoadEvent;
 import org.bukkit.event.world.ChunkPopulateEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.util.Vector;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.block.SpoutBlock;
 import org.getspout.spoutapi.inventory.SpoutItemStack;
 import org.getspout.spoutapi.material.CustomItem;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 import com.pickaxehero.dragonshoard.dragonshoard.ores.AmethystOre;
 import com.pickaxehero.dragonshoard.dragonshoard.ores.RubyOre;
 import com.pickaxehero.dragonshoard.dragonshoard.ores.SapphireGem;
 import com.pickaxehero.dragonshoard.dragonshoard.ores.SapphireOre;
 import com.pickaxehero.dragonshoard.dragonshoard.strings.MessageI18N;
 import com.pickaxehero.dragonshoard.dragonshoard.strings.Strings;
 
 /**
  * Listens for and handles various events needed in this plugin.
  * 
  * @author Pickaxehero
  *
  */
 public class DragonsHoardEventListener implements Listener {
 
 	private Plugin parent = null; 
 	
 	public DragonsHoardEventListener(Plugin parent) {
 		Validate.notNull(parent, MessageI18N.getString("DragonsHoardEventListener.ErrorPluginHasNoParent")); //$NON-NLS-1$
 		
 		this.parent = parent;
 		
 		DragonsHoardPlugin.logger().info(MessageI18N.getString("DragonsHoardEventListener.InfoEventListenerCreated")); //$NON-NLS-1$
 	}
 	
 	@EventHandler
 	public void onChunkLoad(ChunkLoadEvent chunkLoadEvent) {
 		Validate.notNull(chunkLoadEvent, MessageI18N.getString("DragonsHoardEventListener.ErrorChunkLoadNoEvent")); //$NON-NLS-1$
 		
 	
 	}
 	
 	@EventHandler
 	public void onChunkPopulateEvent(ChunkPopulateEvent chunkPopulateEvent) {
 		
 	}
 	
 	@EventHandler
 	public void onBlockBreakEvent(BlockBreakEvent blockBreakEvent) {
 		Validate.notNull(blockBreakEvent, MessageI18N.getString("DragonsHoardEventListener.ErrorBlockBreakEventNull")); //$NON-NLS-1$
 		
		// Check if the player is in creative mode, so nothing will be dropped when 
		// players break blocks then.
		Player player = blockBreakEvent.getPlayer();
		if((player != null) && (player.getGameMode() == GameMode.CREATIVE)) {
			return;
		}
		
 		SpoutBlock broken = (SpoutBlock) blockBreakEvent.getBlock();
 		CustomItem minedGem = null;
 		
 		if(broken.getCustomBlock() instanceof RubyOre) {
 			minedGem = DragonsHoardPlugin.instance().rubyGemInstance();
 		} else if(broken.getCustomBlock() instanceof SapphireOre) {
 			minedGem = DragonsHoardPlugin.instance().sapphireGemInstance();
 		} else if(broken.getCustomBlock() instanceof AmethystOre) {
 			minedGem = DragonsHoardPlugin.instance().amethystGemInstance();
 		}
 		
 		if(minedGem != null) {
 			blockBreakEvent.setCancelled(true);
 			broken.setType(Material.AIR);
			broken.setCustomBlock(null);
 			
 			int gemCount = 1;
 			
 			SpoutItemStack gemStack = new SpoutItemStack(minedGem);
 			gemStack.setAmount(gemCount);
 			
 			broken.getWorld().dropItemNaturally(
 				broken.getLocation(), 
 				gemStack
 			);
 		}		
 	}
 	
 	@EventHandler
 	public void onEntityExplodeEvent(EntityExplodeEvent entityExplodeEvent) {
 		if(entityExplodeEvent.getEntity().getType() == EntityType.CREEPER) {
 			entityExplodeEvent.setCancelled(true);
 			Bukkit.getServer().broadcastMessage(MessageI18N.getString("DragonsHoardEventListener.InfoCreeperNeutered")); //$NON-NLS-1$
 		}
 	}
 	
 	@EventHandler
 	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
 		if(event.getDamager().getType() == EntityType.CREEPER) {
 			event.setCancelled(true);
 			
 			if(event.getEntityType() == EntityType.PLAYER) {
 				SpoutManager.getSoundManager().playCustomSoundEffect(
 					DragonsHoardPlugin.instance(), 
 					(SpoutPlayer) event.getEntity(), 
 					Strings.getString("DragonsHoardEventListener.CreeperSoundURL"),  //$NON-NLS-1$
 					false,
 					event.getDamager().getLocation()
 				);
 			}
 		}
 		
 		if(event.getDamager().getType() == EntityType.SPIDER) {
 			Random random = new Random(System.currentTimeMillis());
 			
 			event.getDamager().setFireTicks(500);
 			event.getDamager().teleport(
 				event.getEntity().getLocation().add(
 					new Vector(
 						random.nextInt(15),
 						random.nextInt(15),
 						random.nextInt(15)
 					)
 				)
 			);
 		}
 
 	}
 	
 	@EventHandler
 	public void onPlayerJoinEvent(PlayerJoinEvent playerJoinEvent) {
 		playerJoinEvent.getPlayer().sendMessage(
 			MessageI18N.getString("DragonsHoardEventListener.InfoPlayerJoinWelcomeMessage") //$NON-NLS-1$
 		);
 	}
 	
 //	@EventHandler
 //	public void onWorldInitEvent(WorldInitEvent worldInitEvent) {
 //		World world = worldInitEvent.getWorld();
 //		
 //		DragonsHoardPlugin.logger().info("World initiated: " + world.getName());
 //		
 //		if(world.getEnvironment() == Environment.NORMAL) {
 //			world.getPopulators().add(new DragonsHoardBlockPopulator());
 //		}
 //	}
 //	
 //	@EventHandler
 //	public void onWorldLoadEvent(WorldLoadEvent worldLoadEvent) {
 //		World world = worldLoadEvent.getWorld();
 //		
 //		DragonsHoardPlugin.logger().info("World loaded: " + world.getName());
 //	}
 	
 }
