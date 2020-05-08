 /*
  * This file is part of Sprout.
  *
  * © 2013 AlmuraDev <http://www.almuradev.com/>
  * Sprout is licensed under the GNU General Public License.
  *
  * Sprout is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Sprout is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License. If not,
  * see <http://www.gnu.org/licenses/> for the GNU General Public License.
  */
 package com.almuradev.sprout.plugin;
 
 import java.util.Collection;
 
 import com.almuradev.sprout.api.crop.Sprout;
 import com.almuradev.sprout.api.mech.Drop;
 import com.almuradev.sprout.plugin.crop.SimpleSprout;
 import com.almuradev.sprout.plugin.task.GrowthTask;
 import com.rits.cloning.Cloner;
 
 import org.apache.commons.lang.SerializationUtils;
 import org.getspout.spoutapi.block.SpoutBlock;
 import org.getspout.spoutapi.inventory.SpoutItemStack;
 import org.getspout.spoutapi.material.CustomBlock;
 import org.getspout.spoutapi.material.MaterialData;
 
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockFadeEvent;
 import org.bukkit.event.block.BlockFromToEvent;
 import org.bukkit.event.block.BlockPhysicsEvent;
 import org.bukkit.event.block.BlockPistonExtendEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.world.WorldInitEvent;
 import org.bukkit.event.world.WorldSaveEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.PistonBaseMaterial;
 
 public class SproutListener implements Listener {
 	private final SproutPlugin plugin;
 	private final Cloner cloner = new Cloner();
 
 	public SproutListener(SproutPlugin plugin) {
 		this.plugin = plugin;
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onBlockFade(BlockFadeEvent event) {
 		final Block fading = event.getBlock();
 		//Don't do a lookup if it isn't soil.
 		if (fading.getType() != Material.SOIL) {
 			return;
 		}
 		final Block top = event.getBlock().getRelative((BlockFace.UP));
 		if (plugin.getWorldRegistry().contains(top.getWorld().getName(), top.getX(), top.getY(), top.getZ())) {
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onBlockBreak(BlockBreakEvent event) {
 		final Block block = event.getBlock();
 		final Sprout sprout = plugin.getWorldRegistry().remove(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
 		if (sprout == null) {
 			return;
 		}
		plugin.getStorage().remove(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
 		((SpoutBlock) block).setCustomBlock(null);
		disperseDrops(sprout, block);
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onBlockFromTo(BlockFromToEvent event) {
 		final Block to = event.getToBlock();
 		final Sprout sprout = plugin.getWorldRegistry().remove(to.getWorld().getName(), to.getX(), to.getY(), to.getZ());
 		if (sprout == null) {
 			return;
 		}
 		plugin.getStorage().remove(to.getWorld().getName(), to.getX(), to.getY(), to.getZ());
 		event.setCancelled(true);
 		to.setType(Material.AIR);
 		((SpoutBlock) to).setCustomBlock(null);
 		disperseDrops(sprout, to);
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onBlockPhysics(BlockPhysicsEvent event) {
 		final Block physics = event.getBlock();
 		if (physics.getRelative(BlockFace.DOWN).getType() != Material.AIR) {
 			return;
 		}
 		final Sprout sprout = plugin.getWorldRegistry().remove(physics.getWorld().getName(), physics.getX(), physics.getY(), physics.getZ());
 		if (sprout == null) {
 			return;
 		}
 		plugin.getStorage().remove(physics.getWorld().getName(), physics.getX(), physics.getY(), physics.getZ());
 		event.setCancelled(true);
 		physics.setType(Material.AIR);
 		((SpoutBlock) physics).setCustomBlock(null);
 		disperseDrops(sprout, physics);
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onPistonExtend(BlockPistonExtendEvent event) {
 		if (event.getLength() == 0) {
 			return;
 		}
 		boolean hasSprout = false;
 		//Check if any block has a sprout
 		for (Block pushable : event.getBlocks()) {
 			if (!plugin.getWorldRegistry().has(pushable.getWorld().getName(), pushable.getX(), pushable.getY(), pushable.getZ())) {
 				final Block top = pushable.getRelative(BlockFace.UP);
 				if (!plugin.getWorldRegistry().has(top.getWorld().getName(), top.getX(), top.getY(), top.getZ())) {
 					continue;
 				}
 			}
 			hasSprout = true;
 			break;
 		}
 		//Don't affect the piston if there is no sprouts.
 		if (!hasSprout) {
 			return;
 		}
 		event.setCancelled(true);
 		for (Block pushable : event.getBlocks()) {
 			//Check if the pushable is a Sprout
 			final Sprout current = plugin.getWorldRegistry().remove(pushable.getWorld().getName(), pushable.getX(), pushable.getY(), pushable.getZ());
 			if (current == null) {
 				//Check if the pushable has a Sprout above
 				final Block top = pushable.getRelative(BlockFace.UP);
 				final Sprout currentTop = plugin.getWorldRegistry().remove(top.getWorld().getName(), top.getX(), top.getY(), top.getZ());
 				if (currentTop == null) {
 					continue;
 				}
 				plugin.getStorage().remove(top.getWorld().getName(), top.getX(), top.getY(), top.getZ());
 				top.setType(Material.AIR);
 				((SpoutBlock) top).setCustomBlock(null);
 				disperseDrops(currentTop, top);
 			} else {
 				plugin.getStorage().remove(pushable.getWorld().getName(), pushable.getX(), pushable.getY(), pushable.getZ());
 				pushable.setType(Material.AIR);
 				((SpoutBlock) pushable).setCustomBlock(null);
 				disperseDrops(current, pushable);
 			}
 		}
 		final PistonBaseMaterial piston = (PistonBaseMaterial) event.getBlock().getState().getData();
 		piston.setPowered(true);
 		event.getBlock().setData(piston.getData(), true);
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		//Only allow right clicks
 		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
 			return;
 		}
 
 		final ItemStack held = event.getItem();
 		if (held == null) {
 			return;
 		}
 		final Block interacted = event.getClickedBlock();
 		final SpoutItemStack stack = new SpoutItemStack(held);
 		final String name;
 
 		//Grab the material's name
 		if (stack.isCustomItem()) {
 			name = stack.getMaterial().getNotchianName();
 		} else {
 			name = held.getType().name();
 		}
 
 		//Only manage Sprouts
 		final Sprout sprout = plugin.getSproutRegistry().find(name);
 		if (sprout == null) {
 			return;
 		}
 
 		//Place on top of Block. TODO Keep this?
 		if (event.getBlockFace() != BlockFace.UP) {
 			event.setCancelled(true);
 			return;
 		}
 
 		//Place on soil. TODO Expand this concept.
 		if (interacted.getType() != Material.SOIL) {
 			event.setCancelled(true);
 			return;
 		}
 
 		final Block where = interacted.getRelative(BlockFace.UP);
 
 		//Make sure where we are setting the block won't be already obstructed.
 		if (where.getType() != Material.AIR) {
 			event.setCancelled(true);
 			return;
 		}
 
 		final Sprout toInject = cloner.deepClone(sprout);
 		plugin.getWorldRegistry().add(where.getWorld().getName(), where.getX(), where.getY(), where.getZ(), toInject);
 		plugin.getStorage().add(where.getWorld().getName(), where.getX(), where.getY(), where.getZ(), toInject.getName(), 0);
 
 		//Set material
 		if (stack.isCustomItem()) {
 			final CustomBlock block = MaterialData.getCustomBlock(sprout.getBlockSource());
 			//plugin.getLogger().info("Placing source block: " + sprout.getBlockSource());
 			((SpoutBlock) where).setCustomBlock(block);
 
 			//Remove item from inventory.
 			if (!(event.getPlayer().getGameMode() == GameMode.CREATIVE)) {
 				held.setAmount(held.getAmount() - 1);
 				if (held.getAmount() == 0) {
 					event.getPlayer().setItemInHand(new ItemStack(Material.AIR));
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onWorldInit(WorldInitEvent event) {
 		GrowthTask.schedule(plugin, event.getWorld());
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onWorldSave(WorldSaveEvent event) {
 		GrowthTask.unschedule(event.getWorld());
 	}
 
 	private void disperseDrops(final Sprout sprout, final Block block) {
 		final Collection<Drop> drops = sprout.getDrops();
 		for (Drop drop : drops) {
 			final org.getspout.spoutapi.material.Material customMaterial = MaterialData.getCustomItem(drop.getName());
 			if (customMaterial == null) {
 				final Material material = Material.getMaterial(drop.getName());
 				if (material == null) {
 					continue;
 				}
 				final ItemStack stack = new ItemStack(material, drop.getAmount());
 				block.getWorld().dropItemNaturally(block.getLocation(), stack);
 			} else {
 				final SpoutItemStack spoutStack = new SpoutItemStack(customMaterial, drop.getAmount());
 				block.getWorld().dropItemNaturally(block.getLocation(), spoutStack);
 			}
 		}
 	}
 }
