 package dk.gabriel333.BukkitInventoryTools.DigiLock;
 
 import dk.gabriel333.BukkitInventoryTools.BIT;
 import dk.gabriel333.BukkitInventoryTools.Inventory.BITInventory;
 import dk.gabriel333.Library.BITConfig;
 import dk.gabriel333.Library.BITMessages;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.*;
 import org.getspout.spoutapi.block.SpoutBlock;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class BITBlockListener implements Listener {
 
         @EventHandler
 	public void onBlockRedstoneChange(BlockRedstoneEvent event) {
 		// TODO: THERE IS AN MEMORY LEAK HERE!!!
 		/*
 		 * SpoutBlock sBlock = (SpoutBlock) event.getBlock(); if
 		 * (!BITDigiLock.isLockable(sBlock)) return; if
 		 * (BITDigiLock.isLocked(sBlock)) { if (G333Config.DEBUG_EVENTS)
 		 * G333Messages.showInfo("BlockRedstoneEvt:" +
 		 * event.getBlock().getType() + " getOC:" + event.getOldCurrent() +
 		 * " getNC:" + event.getNewCurrent()); if
 		 * (BITDigiLock.isDoubleDoor(sBlock)) {
 		 * 
 		 * } else if (BITDigiLock.isDoor(sBlock)) { Door door = (Door)
 		 * sBlock.getState().getData(); if (!door.isOpen()) {
 		 * event.setNewCurrent(event.getOldCurrent()); } } }
 		 */
 	}
         
         @EventHandler
 	public void onBlockPhysics(BlockPhysicsEvent event) {
 		if (event.isCancelled())
 			return;
 		Block b = event.getBlock();
		if (b != null && !(event.getBlock() instanceof SpoutBlock)) {
 			
 			if (!BITDigiLock.isLockable(b))
 				return;
 			SpoutBlock sBlock = (SpoutBlock) b;
 			if (BITDigiLock.isLocked(sBlock)) {
 				event.setCancelled(true);
 				if (BITConfig.DEBUG_EVENTS) {
 					BITMessages.showInfo("BlockPhysicsEvt:"
 							+ event.getBlock().getType() + " getCT:"
 							+ event.getChangedType());
 				}
 			}
 		}
 	}
 
 	@EventHandler
 	public void onBlockFromTo(BlockFromToEvent event) {
 		// super.onBlockFromTo(event);
 		if (event.isCancelled() || !(event.getBlock() instanceof SpoutBlock)) // hack for spout/worldborder bugs
 			return;
 		SpoutBlock block = (SpoutBlock) event.getBlock();
 		if (!BITDigiLock.isLockable(block))
 			return;
 		if (BITDigiLock.isLocked(block)) {
 			if (BITDigiLock.isDoubleDoor(block)) {
 				BITMessages.showInfo("Tried to break doubledoor");
 			}
 			event.setCancelled(true);
 			if (BITConfig.DEBUG_EVENTS) {
 				BITMessages.showInfo("BlockFromTo:"
 						+ event.getBlock().getType() + " ToBlk:"
 						+ event.getToBlock().getType());
 			}
 		}
 	}
 
         @EventHandler
 	public void onBlockBreak(BlockBreakEvent event) {
 		if (event.isCancelled())
 			return;
 		SpoutPlayer sPlayer = (SpoutPlayer) event.getPlayer();
 		SpoutBlock sBlock = (SpoutBlock) event.getBlock();
 		SpoutBlock blockOnTop = sBlock.getRelative(BlockFace.UP);
 		if (BITDigiLock.isBookshelf(sBlock)&&!BITDigiLock.isLocked(sBlock)) {
 			if (BITInventory.isBitInventoryCreated(sBlock)) {
 				if (BIT.plugin.economy.hasAccount(sPlayer.getName())) {
 					if (BIT.plugin.economy.has(sPlayer.getName(), BITConfig.BOOKSHELF_DESTROYCOST)
 							|| BITConfig.BOOKSHELF_DESTROYCOST < 0) {
 						BITInventory.removeBookshelfAndDropItems(sPlayer, sBlock);
 					} else {
 						sPlayer.sendMessage("You dont have enough money ("
 								+ BIT.plugin.economy.getBalance(sPlayer.getName())
 								+ "). Cost is:"
 								+ BIT.plugin.economy
 										.format(BITConfig.BOOKSHELF_DESTROYCOST));
 						event.setCancelled(true);
 					}
 				} else {
 					BITInventory.removeBookshelfAndDropItems(sPlayer, sBlock);
 				}
 			}
 		} else
 		if (BITDigiLock.isLocked(sBlock) || BITDigiLock.isLocked(blockOnTop)) {
 			sPlayer.damage(5);
 			event.setCancelled(true);
 		}
 	}
         
         @EventHandler
 	public void onBlockDamage(BlockDamageEvent event) {
 		if (event.isCancelled())
 			return;
 		SpoutBlock sBlock = (SpoutBlock) event.getBlock();
 		if (!BITDigiLock.isLockable(sBlock))
 			return;
 		if (BITDigiLock.isLocked(sBlock)) {
 			// sPlayer.damage(1);
 			event.setCancelled(true);
 		}
 	}
 
         @EventHandler
 	public void onBlockBurn(BlockBurnEvent event) {
 		if (event.isCancelled())
 			return;
 		SpoutBlock sBlock = (SpoutBlock) event.getBlock();
 		if (BITDigiLock.isLocked(sBlock)) {
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler
 	public void onBlockFade(BlockFadeEvent event) {
 		if (event.isCancelled())
 			return;
 		SpoutBlock sBlock = (SpoutBlock) event.getBlock();
 		if (!BITDigiLock.isLockable(sBlock))
 			return;
 		if (BITDigiLock.isLocked(sBlock)) {
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler
 	public void onBlockForm(BlockFormEvent event) {
 		if (event.isCancelled())
 			return;
 		SpoutBlock sBlock = (SpoutBlock) event.getBlock();
 		if (!BITDigiLock.isLockable(sBlock))
 			return;
 		if (BITDigiLock.isLocked(sBlock)) {
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler
 	public void onBlockSpread(BlockSpreadEvent event) {
 		if (event.isCancelled())
 			return;
 		SpoutBlock sBlock = (SpoutBlock) event.getBlock();
 		if (!BITDigiLock.isLockable(sBlock))
 			return;
 		if (BITDigiLock.isLocked(sBlock)) {
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler
 	public void onBlockIgnite(BlockIgniteEvent event) {
 		if (event.isCancelled())
 			return;
 		SpoutBlock sBlock = (SpoutBlock) event.getBlock();
 		if (!BITDigiLock.isLockable(sBlock))
 			return;
 		SpoutPlayer sPlayer = (SpoutPlayer) event.getPlayer();
 		if (BITDigiLock.isLocked(sBlock)) {
 			sPlayer.damage(10);
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler
 	public void onSignChange(SignChangeEvent event) {
 		if (event.isCancelled())
 			return;
 		SpoutBlock sBlock = (SpoutBlock) event.getBlock();
 		if (!BITDigiLock.isLockable(sBlock))
 			return;
 		if (BITDigiLock.isLocked(sBlock)) {
 			if (!BITConfig.LIBRARY_USESIGNEDITGUI) {
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	@EventHandler
 	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
 		if (event.isCancelled())
 			return;
 		SpoutBlock sBlock = (SpoutBlock) event.getBlock();
 		if (!BITDigiLock.isLockable(sBlock))
 			return;
 		if (BITDigiLock.isLocked(sBlock)) {
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler
 	public void onBlockPistonRetract(BlockPistonRetractEvent event) {
 		if (event.isCancelled())
 			return;
 		SpoutBlock sBlock = (SpoutBlock) event.getBlock();
 		if (!BITDigiLock.isLockable(sBlock))
 			return;
 		if (BITDigiLock.isLocked(sBlock)) {
 			event.setCancelled(true);
 		}
 	}
 
 }
