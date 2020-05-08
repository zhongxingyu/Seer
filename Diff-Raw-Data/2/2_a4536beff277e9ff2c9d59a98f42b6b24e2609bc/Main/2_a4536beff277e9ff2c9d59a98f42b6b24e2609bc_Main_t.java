 /**
  *  Name:    Main.java
  *  Created: 00:14:08 - 6 jul 2013
  * 
  *  Author:  Lucas Arnstrm - LucasEmanuel @ Bukkit forums
  *  Contact: lucasarnstrom(at)gmail(dot)com
  *  
  *
  *  Copyright 2013 Lucas Arnstrm
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program. If not, see <http://www.gnu.org/licenses/>.
  *  
  *
  *
  *  Filedescription:
  *
  * 
  */
 
 package me.lucasemanuel.heatseekers;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.event.entity.ProjectileLaunchEvent;
 import org.bukkit.event.inventory.PrepareItemCraftEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitRunnable;
 import org.mcstats.Metrics;
 
 public class Main extends JavaPlugin implements Listener {
 	
 	private ShapedRecipe launcher;
 	private HashMap<Projectile, Entity> projectiles = new HashMap<Projectile, Entity>();
 	
 	public void onEnable() {
 		
 		Config.load(this);
 		
 		if(getConfig().getBoolean("auto-update")) {
			new Updater(this, "heatseekers", this.getFile(), Updater.UpdateType.DEFAULT, false);
 		}
 		
 		try {
 			Metrics metrics = new Metrics(this);
 			metrics.start();
 		}
 		catch (IOException e) {}
 		
 		setupRecipe();
 		
 		this.getServer().getPluginManager().registerEvents(this, this);
 		startTicker();
 	}
 	
 	@SuppressWarnings("serial")
 	private void setupRecipe() {
 		ItemStack bow = new ItemStack(Material.BOW);
 		ItemMeta meta = bow.getItemMeta();
 		meta.setDisplayName("HeatSeekers");
 		meta.setLore(new ArrayList<String>() {{
 			add("WARNING! Extremely dangerous!");
 		}});
 		bow.setItemMeta(meta);
 
 		ShapedRecipe recipe = new ShapedRecipe(new ItemStack(bow));
 		recipe.shape(new String[] { "DDD", "DBD", "DDD" });
 		recipe.setIngredient('D', Material.DIAMOND_BLOCK);
 		recipe.setIngredient('B', Material.BOW);
 		getServer().addRecipe(recipe);
 		
 		launcher = recipe;
 	}
 	
 	private void startTicker() {
 		new BukkitRunnable() {
 			public void run() {
 				
 				HashMap<Projectile, Entity> templist = null;
 				
 				outer:for(Entry<Projectile, Entity> entry : projectiles.entrySet()) {
 					Projectile p = entry.getKey();
 					Entity     e = entry.getValue();
 					
 					if(e == null) {
 						List<Entity> list = p.getNearbyEntities(15, 15, 15);
 						inner:if(!list.isEmpty()) {
 							for(Entity e2 : list) {
 								if(e2 instanceof LivingEntity && e2 != p.getShooter()) {
 									if(templist == null) templist = new HashMap<Projectile, Entity>();
 									templist.put(p, e2);
 									break inner;
 								}
 							}
 							
 							continue outer;
 						}
 					}
 					else {
 						p.setVelocity(e.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(2));
 					}
 				}
 				
 				if(templist != null) {
 					for(Entry<Projectile, Entity> entry : templist.entrySet()) {
 						projectiles.put(entry.getKey(), entry.getValue());
 					}
 				}
 			}
 		}.runTaskTimer(this, 0L, 1L);
 	}
 	
 	private boolean isLauncher(ItemStack is) {
 		
 		if (is.hasItemMeta() 
 				&& is.getItemMeta().hasDisplayName()
 				&& is.getItemMeta().hasLore() 
 				&& is.getItemMeta().getDisplayName().equals("HeatSeekers") 
 				&& is.getItemMeta().getLore().contains("WARNING! Extremely dangerous!")) {
 			return true;
 		}
 		
 		return false;
 	}
 	
 	@EventHandler
 	public void onProjectileHit(ProjectileHitEvent event) {
 		if(projectiles.containsKey(event.getEntity())) {
 			projectiles.remove(event.getEntity());
 		}
 	}
 	
 	@EventHandler
 	public void onProjectileLaunch(ProjectileLaunchEvent event) {
 		if(event.getEntity().getShooter() instanceof Player) {
 			Player player = (Player) event.getEntity().getShooter();
 			Projectile projectile = event.getEntity();
 			
 			if(!getConfig().getBoolean("all-projectiles") && !isLauncher(player.getItemInHand())) {
 				return;
 			}
 			if(getConfig().getBoolean("op-only") && !player.isOp()) {
 				player.sendMessage(ChatColor.RED + "Only OP's are allowed to use heatseekers!");
 				return;
 			}
 			if(!getConfig().getBoolean("all-projectiles") && !(projectile instanceof Arrow)) {
 				return;
 			}
 			
 			List<Entity> list = projectile.getNearbyEntities(15, 15, 15);
 			outer:if(!list.isEmpty()) {
 				for(Entity entity : list) {
 					if(entity instanceof LivingEntity && entity != projectile.getShooter()) {
 						projectiles.put(projectile, entity);
 						break outer;
 					}
 				}
 				projectiles.put(projectile, null);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void preCraft(PrepareItemCraftEvent event) {
 		if(getConfig().getBoolean("craftingpermission") && RecipeUtil.areEqual(event.getRecipe(), launcher)) {
 			HumanEntity human = event.getView().getPlayer();
 			if (!human.hasPermission("heatseekers.craft")) {
 				event.getInventory().setResult(null);
 				if (human instanceof Player) {
 					Player player = (Player) human;
 					player.sendMessage(ChatColor.RED + "Need permission to craft this!");
 				}
 			}
 		}
 	}
 }
