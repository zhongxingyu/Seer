 /*
  * ExactNode - Permission ruleset plugin for Bukkit
  * Copyright (C) 2012 coaster3000 @ github
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
  * USA.
  * 
  * 
  * NOTES: Parts of the code in this file is copyrighted to
  * Modifyworld - PermissionsEx ruleset plugin for Bukkit
  * Copyright (C) 2011 t3hk0d3 http://www.tehkode.ru
  * 
  * Links
  * https://github.com/PEXPlugins/Modifyworld
  * 
  * Lines are marked where duplicate code is.
  */
 package com.github.coaster3000.exactnode;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.ComplexEntityPart;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Tameable;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 
 public abstract class ExactNodeListener implements Listener {
 	protected final static String WILDCARD = "*";
 	public final static String ROOTNODE = "en";
 	protected Plugin plugin;
 	protected ConfigurationSection config;
 	protected boolean useWildNode = false;
 	protected boolean informPlayers = false;
 	private boolean useMaterialNames = true;
	private boolean checkMetadata = true;
 	private boolean useOp = true;
 
 	public ExactNodeListener(Plugin plugin, ConfigurationSection config) {
 		this.plugin = plugin;
 		this.config = config;
 		this.registerEvents();
 
 		checkMetadata = config.getBoolean("checks.metadata", checkMetadata);
 		useMaterialNames = config.getBoolean("settings.use.material-names", useMaterialNames);
 		informPlayers = config.getBoolean("settings.notify-player", informPlayers);
 		useWildNode = config.getBoolean("settings.use.custom-wildcard-node", useWildNode);
 		useOp = config.getBoolean("settings.use.op-perms", useOp);
 	}
 
 	/*
 	 * Duplicate Code Start
 	 */
 
 	protected String getEntityName(Entity entity) {
 
 		if (entity instanceof ComplexEntityPart) {
 			return getEntityName(((ComplexEntityPart) entity).getParent());
 		}
 
 		String entityName = formatEnumString(entity.getType().toString());
 
 		if (entity instanceof Item) {
 			entityName = getItemPermission(((Item) entity).getItemStack());
 		}
 
 		if (entity instanceof Player) {
 			return "player." + ((Player) entity).getName();
 		} else if (entity instanceof Tameable) {
 			Tameable animal = (Tameable) entity;
 
 			return "animal." + entityName + (animal.isTamed() ? "." + animal.getOwner().getName() : "");
 		}
 
 		EntityCategory category = EntityCategory.fromEntity(entity);
 
 		if (category == null) {
 			return entityName; // category unknown (ender crystal)
 		}
 
 		return category.getNameDot() + entityName;
 	}
 
 	protected String getInventoryTypePermission(InventoryType type) {
 		return formatEnumString(type.name());
 	}
 
 	// Functional programming fuck yeah
 	protected String getMaterialPermission(Material type) {
 		return this.useMaterialNames ? formatEnumString(type.name()) : Integer.toString(type.getId());
 	}
 
 	protected String getMaterialPermission(Material type, byte metadata) {
 		return getMaterialPermission(type) + (metadata > 0 ? ":" + metadata : "");
 	}
 
 	protected String getBlockPermission(Block block) {
 		return getBlockPermission(block, checkMetadata);
 	}
 
 	private String getBlockPermission(Block block, boolean useMeta) {
 		return getMaterialPermission(block.getType(), (useMeta) ? block.getData() : 0);
 	}
 
 	public String getItemPermission(ItemStack item) {
 		return getMaterialPermission(item.getType(), item.getData().getData());
 	}
 
 	protected void registerEvents() {
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 	}
 
 	private String formatEnumString(String enumName) {
 		return enumName.toLowerCase().replace("_", "");
 	}
 
 	protected boolean permissionDenied(Player player, String[] perms) {
 		boolean isFound = false;
 		for (String perm : perms) {
 			if (permissionSet(player, perm))
 				if (!hasPermission(player, perm))
 					return false;
 				else
 					isFound = true;
 		}
 
 		if (!isFound)
 			if (useOp)
 				return !player.isOp();
 			else
 				return true;
 		else
 			return true;
 	}
 
 	protected final boolean hasPermission(Player player, String permission) {
 		return player.hasPermission(permission);
 	}
 
 	protected final boolean permissionSet(Player player, String permission) {
 		return player.isPermissionSet(permission);
 	}
 
 	/*
 	 * Duplicate Code End
 	 */
 
 }
