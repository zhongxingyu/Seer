 /*
  * EasyJetpack Minecraft Bukkit plugin
  * Written by j_selby
  * 
  * Version 0.5
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.github.jselby.EasyJetpack;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.player.PlayerToggleSneakEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.util.Vector;
 
 public class EasyJetpack extends JavaPlugin implements Listener {
 	double pluginVersion = 0.5;
 
 	// Load plugin
 	@Override
 	public void onEnable() {
 		getServer().getPluginManager().registerEvents(this, this);
 		checkConfig();
 		getLogger().info("EasyJetpack v" + pluginVersion + " has been enabled.");
 	}
 
 	// Unload plugin
 	@Override
 	public void onDisable() {
 		getLogger().info("EasyJetpack v" + pluginVersion + " has been disabled.");
 	}
 
 	// Configuration
 	public void forceDefaults(String why) throws IOException {
 		// Force the defaults, for older versions
 		this.getConfig().save(this.getDataFolder().getPath() + File.separator + "backupconfig.yml");
 		this.getConfig().options().header("EasyJetpack configuration file\nVersion v" + pluginVersion);
 		this.getConfig().set("jetpack.enabled", true);
 		this.getConfig().set("jetpack.id", 315);
 		this.getConfig().set("jetpack.durability", true);
 		this.getConfig().set("jetpack.noFallDamage", false);
 		this.getConfig().set("tempjetpack.enabled", false);
 		this.getConfig().set("tempjetpack.id", 315);
 		this.getConfig().set("tempjetpack.uses", 10);
 		this.getConfig().set("tempjetpack.noFallDamage", false);
 		this.getConfig().set("boots.enabled", true);
 		this.getConfig().set("boots.id", 301);
 		this.getConfig().set("boots.fallEffects", true);
 		this.getConfig().set("boots.noFallDamageAtAll", false);
 		this.getConfig().set("fuel.enabled", true);
 		this.getConfig().set("fuel.uses", 10);
 		this.getConfig().set("fuel.id", 263);
 		this.getConfig().set("chat.messages", true);
 		this.getConfig().set("chat.nofuel", "You don't have any FUELNAME.");
 		this.getConfig().set("chat.useallfuel", "You used all your FUELNAME.");
 		this.getConfig().set("chat.depletefuel", "You used up a FUELNAME.");
 		this.getConfig().set("chat.jetpackbroke", "Your JETPACKNAME jetpack broke!");
 		getLogger().info("Your configuration file was " + why);
 		getLogger().info("You will need to (re)configure it.");
 		this.saveConfig();
 	}
 
 	public void checkConfig() {
 		// Checks if the config exists
 		if (this.getConfig().get("jetpack.id") == null) {
 			try {
 				forceDefaults("created");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		} else if (this.getConfig().get("chat.jetpackbroke") == null) {
 			try {
 				forceDefaults("reset due to a update");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		// setDefaults();
 	}
 
 	// Get the boot id
 	public int getBootId() {
 		String bootId = (String) this.getConfig().getString("boots.id");
 		if (bootId.indexOf(":") > -1) {
 			return Integer.parseInt(bootId.split(":")[0]);
 		} else {
 			return Integer.parseInt(bootId);
 		}
 	}
 
 	// If durability is set, check for it
 	public short getBootDurability() {
 		String bootId = (String) this.getConfig().getString("boots.id");
 		if (bootId.indexOf(":") > -1) {
 			return Short.parseShort(bootId.split(":")[1]);
 		} else {
 			return 0;
 		}
 	}
 
 	// Checks if the player is wearing the right boots for anti-fall damage
 	public boolean checkPlayerWearingBoots(Player player) {
 		if (getBootDurability() < 1) {
 			return Utils.playerIsWearing(player, 0, getBootId());
 		} else {
 			return Utils.playerIsWearing(player, 0, getBootId(), getBootDurability());
 		}
 	}
 
 	// Get the jetpack id
 	public int getJetpackId() {
 		String jetpackId = (String) this.getConfig().getString("jetpack.id");
 		if (jetpackId.indexOf(":") > -1) {
 			return Integer.parseInt(jetpackId.split(":")[0]);
 		} else {
 			return Integer.parseInt(jetpackId);
 		}
 	}
 
 	// Get the jetpack durability
 	public short getJetpackDurability() {
 		String jetpackId = (String) this.getConfig().getString("jetpack.id");
 		if (jetpackId.indexOf(":") > -1) {
 			return Short.parseShort(jetpackId.split(":")[1]);
 		} else {
 			return 0;
 		}
 	}
 
 	// Checks if the player is wearing the right jetpack for "flying"
 	public boolean checkPlayerWearingJetpack(Player player) {
 		if (getJetpackDurability() < 1) {
 			return Utils.playerIsWearing(player, 2, getJetpackId());
 		} else {
 			return Utils.playerIsWearing(player, 2, getJetpackId(), getJetpackDurability());
 		}
 	}
 
 	// Get the temp-jetpack id
 	public int getTempJetpackId() {
 		String jetpackId = (String) this.getConfig().getString("tempjetpack.id");
 		if (jetpackId.indexOf(":") > -1) {
 			return Integer.parseInt(jetpackId.split(":")[0]);
 		} else {
 			return Integer.parseInt(jetpackId);
 		}
 	}
 
 	// Get the temp-jetpack durability
 	public short getTempJetpackDurability() {
 		String jetpackId = (String) this.getConfig().getString("tempjetpack.id");
 		if (jetpackId.indexOf(":") > -1) {
 			return Short.parseShort(jetpackId.split(":")[1]);
 		} else {
 			return 0;
 		}
 	}
 
 	// Checks if the player is wearing the right jetpack for temp "flying"
 	public boolean checkPlayerWearingTempJetpack(Player player) {
 		if (getTempJetpackDurability() < 1) {
 			return Utils.playerIsWearing(player, 2, getTempJetpackId());
 		} else {
 			return Utils.playerIsWearing(player, 2, getTempJetpackId(), getTempJetpackDurability());
 		}
 	}
 
 	// Get the fuel id
 	public int getFuelId() {
 		String jetpackId = (String) this.getConfig().getString("fuel.id");
 		if (jetpackId.indexOf(":") > -1) {
 			return Integer.parseInt(jetpackId.split(":")[0]);
 		} else {
 			return Integer.parseInt(jetpackId);
 		}
 	}
 
 	// Get the fuel durability
 	public short getFuelDurability() {
 		String jetpackId = (String) this.getConfig().getString("fuel.id");
 		if (jetpackId.indexOf(":") > -1) {
 			return Short.parseShort(jetpackId.split(":")[1]);
 		} else {
 			return 0;
 		}
 	}
 
 	// Checks if the player is holding the right fuel
 	public boolean checkPlayerHoldingFuel(Player player) {
 		if (getFuelDurability() < 1) {
 			return Utils.playerIsHolding(player, getFuelId());
 		} else {
 			return Utils.playerIsHolding(player, getFuelId(), getFuelDurability());
 		}
 	}
 
 	// Plays the respective effect at a specified location
 	public void playEffect(Effect e, Location l, int num) {
 		for (int i = 0; i < this.getServer().getOnlinePlayers().length; i++)
 			this.getServer().getOnlinePlayers()[i].playEffect(l, e, num);
 	}
 
 	// When the player falls, check for boots, and if wearing configured boots,
 	// stop fall damage
 	@EventHandler
 	public void onPlayerFall(EntityDamageEvent event) {
 		checkConfig();
 		if (event.getEntity() instanceof Player) {
			if (event.getCause() == DamageCause.FALL) {
				Player player = (Player) event.getEntity();
 				if (Boolean.parseBoolean((String) this.getConfig().getString("boots.noFallDamageAtAll"))) {
 					// Stop player getting fall damage
 					event.setCancelled(true);
 				} else if (checkPlayerWearingJetpack(player) && Boolean.parseBoolean((String) this.getConfig().getString("jetpack.noFallDamage"))) {
 					// Stop player getting fall damage
 					event.setCancelled(true);
 				}
				if (player.hasPermission("easyjetpack.softlanding") && checkPlayerWearingBoots(player) && event.getCause() == EntityDamageEvent.DamageCause.FALL && Boolean.parseBoolean((String) this.getConfig().getString("boots.enabled"))) {

					// Confuse the player, if they take more then 5 hearts of damage (Enabled in config)
					if (event.getDamage() > 10 && Boolean.parseBoolean((String) this.getConfig().getString("boots.fallEffects"))) {
						PotionEffect confusion = new PotionEffect(PotionEffectType.CONFUSION, 140, 2);
						player.addPotionEffect(confusion, true);
					}
 
					// Stop player getting fall damage
					event.setCancelled(true);
 				}
 			}
 		}
 	}
 
 	// Deals damage to the player
 	public void damageJetpack(Player player) {
 		player.getInventory().getArmorContents()[2].setDurability((short) (player.getInventory().getArmorContents()[2].getDurability() + 1));
 		if (player.getInventory().getArmorContents()[2].getDurability() > 150) {
 			if (Boolean.parseBoolean((String) this.getConfig().getString("chat.messages"))) {
 				player.sendMessage(ChatColor.RED + this.getConfig().getString("chat.jetpackbroke").replaceAll("JETPACKNAME", player.getInventory().getArmorContents()[2].getType().name().toLowerCase().replace("_", " ")));
 			}
 			player.getInventory().setChestplate(getAir());
 		}
 	}
 
 	// Convience method - gets a item stack of air
 	public ItemStack getAir() {
 		ItemStack air = new ItemStack(Material.AIR);
 		return air;
 	}
 
 	// Plays a jetpack effect
 	public void jetpackEffect(Player player) {
 		playEffect(Effect.SMOKE, player.getLocation(), 256);
 		playEffect(Effect.SMOKE, player.getLocation(), 256);
 		playEffect(Effect.GHAST_SHOOT, player.getLocation(), 1);
 	}
 
 	// Launches the player into the air
 	public void launchPlayer(Player player) {
 		Vector dir = player.getLocation().getDirection();
 		Vector vec = new Vector(dir.getX() * 0.8D, 0.8D, dir.getZ() * 0.8D);
 		player.setVelocity(vec);
 	}
 
 	// Deals with fuel consumption
 	public void useFuel(Player player) {
 		if (!player.hasPermission("easyjetpack.fuelless")) {
 
 			if (player.getItemInHand().getDurability() > 100) {
 
 				if (player.getItemInHand().getAmount() == 1) {
 
 					// Player used up all their fuel
 					if (Boolean.parseBoolean((String) this.getConfig().getString("chat.messages"))) {
 						player.sendMessage(this.getConfig().getString("chat.useallfuel").replaceAll("FUELNAME", player.getItemInHand().getType().name().toLowerCase().replace("_", " ")));
 					}
 					player.setItemInHand(getAir());
 
 				} else {
 					// Use 1 fuel
 
 					player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
 					player.getItemInHand().setDurability((short) 0);
 
 					if (Boolean.parseBoolean((String) this.getConfig().getString("chat.messages"))) {
 						player.sendMessage(this.getConfig().getString("chat.depletefuel").replaceAll("FUELNAME", player.getItemInHand().getType().name().toLowerCase().replace("_", " ")));
 					}
 				}
 			} else {
 				// "Damages" the fuel
 				player.getItemInHand().setDurability((short) ((short) player.getItemInHand().getDurability() + (100 / (Integer.parseInt((String) this.getConfig().getString("fuel.uses")) - 1))));
 			}
 
 		}
 	}
 
 	// Tells the player they they don't have any fuel
 	public void noFuel(Player player) {
 		ItemStack fuel = new ItemStack(Integer.parseInt((String) this.getConfig().getString("fuel.id")));
 		if (Boolean.parseBoolean((String) this.getConfig().getString("chat.messages"))) {
 			player.sendMessage(ChatColor.RED + this.getConfig().getString("chat.nofuel").replaceAll("FUELNAME", fuel.getType().name().toLowerCase().replace("_", " ")));
 		}
 	}
 
 	// Deals with the player *spamming* shift
 	@EventHandler
 	public void onPlayerEvent(PlayerToggleSneakEvent event) {
 		checkConfig();
 		Player player = event.getPlayer();
 
 		if (player.isSneaking() && (Boolean) this.getConfig().get("tempjetpack.enabled") && checkPlayerWearingTempJetpack(player)) {
 			// Temp jetpack
 			if (player.getInventory().getArmorContents()[2].getDurability() < 150 - (Integer) this.getConfig().get("tempjetpack.uses")) {
 				player.getInventory().getArmorContents()[2].setDurability((short) (player.getInventory().getArmorContents()[2].getDurability() + (short) (150 / (Integer) this.getConfig().get("tempjetpack.uses"))));
 			}
 			if (player.getInventory().getArmorContents()[2].getDurability() > 150) {
 				if (Boolean.parseBoolean((String) this.getConfig().getString("chat.messages"))) {
 					player.sendMessage(ChatColor.RED + this.getConfig().getString("chat.jetpackbroke").replaceAll("JETPACKNAME", player.getInventory().getArmorContents()[2].getType().name().toLowerCase().replace("_", " ")));
 				}
 				player.getInventory().setChestplate(getAir());
 			}
 			jetpackEffect(player);
 			damageJetpack(player);
 			launchPlayer(player);
 		} else if (player.isSneaking() && player.hasPermission("easyjetpack.fly")) {
 			if (Boolean.parseBoolean((String) this.getConfig().getString("jetpack.enabled")) && checkPlayerWearingJetpack(player)) {
 
 				if (Boolean.parseBoolean((String) this.getConfig().getString("fuel.enabled")) && checkPlayerHoldingFuel(player)) {
 
 					// Do the jetpack effects
 					jetpackEffect(player);
 					// Use fuel
 					useFuel(player);
 
 					// Damage the jetpack
 					if (Boolean.parseBoolean((String) this.getConfig().getString("jetpack.durability"))) {
 						damageJetpack(player);
 					}
 
 					// Throw them in the air
 					launchPlayer(player);
 				} else {
 					if (Boolean.parseBoolean((String) this.getConfig().getString("fuel.enabled"))) {
 						// Tell the player they have no fuel
 						noFuel(player);
 					} else {
 						// Do the jetpack effects
 						jetpackEffect(player);
 
 						// Damage the jetpack
 						if (Boolean.parseBoolean((String) this.getConfig().getString("jetpack.durability"))) {
 							damageJetpack(player);
 						}
 
 						// Throw them in the air
 						launchPlayer(player);
 					}
 				}
 			}
 		}
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if (cmd.getName().equalsIgnoreCase("easyjetpack")) {
 			if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
 				this.reloadConfig();
 				checkConfig();
 				sender.sendMessage("Reloaded config successfully!");
 			} else {
 				sender.sendMessage("Unknown arguments.");
 			}
 			return true;
 		}
 		return false;
 	}
 }
