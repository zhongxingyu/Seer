 package com.gildorymrp.gildorym;
 
 import java.util.Random;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 public class Gildorym extends JavaPlugin {
 
 	public Economy economy;
 	
 	public void onEnable() {
 		this.economy = this.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class).getProvider();
 		if (this.economy == null) {
 			this.getLogger().severe("Could not find a compatible economy, disabling!");
 			this.setEnabled(false);
 		}
 		
 		this.getCommand("newcharacter").setExecutor(new NewCharacterCommand());
 		this.getCommand("setname").setExecutor(new SetNameCommand());
 		this.getCommand("setnameother").setExecutor(new SetNameOtherCommand());
 		this.getCommand("rollinfo").setExecutor(new RollInfoCommand());
 		this.getCommand("radiusemote").setExecutor(new RadiusEmoteCommand());
 		RollCommand rc = new RollCommand(this);
 		this.getCommand("roll").setExecutor(rc);
 		this.getCommand("dmroll").setExecutor(rc);
 		MetaEditorCommands mec = new MetaEditorCommands();
 		this.getCommand("renameitem").setExecutor(mec);
 		this.getCommand("setlore").setExecutor(mec);
 		this.getCommand("addlore").setExecutor(mec);
 		this.getCommand("removelore").setExecutor(mec);
 		this.getCommand("signitem").setExecutor(mec);
 		
 		this.getServer().getPluginManager().registerEvents(new EntityDamageListener(this), this);
 		this.getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
 		this.getServer().getPluginManager().registerEvents(new SignChangeListener(), this);
 	}
 
 	public void onInjury(Player injured, String type, int dieSize, int fallDistance, int roll) {
 		ChatColor b = ChatColor.BLUE;
 		ChatColor r = ChatColor.RED;
 		ChatColor w = ChatColor.WHITE;
 		ChatColor dr = ChatColor.DARK_RED;
 		ChatColor g = ChatColor.GOLD;
 		
 		int severity, fallInFeet, rollPercent, severityPercent, x, y, z, injuryRoll;
 		
 		fallInFeet = fallDistance * 3;
 		severity = (new Random()).nextInt(dieSize) + 1;
 		rollPercent = roll * 5;
 		severityPercent = severity * 2;
 		x = injured.getLocation().getBlockX();
 		y = injured.getLocation().getBlockY();
 		z = injured.getLocation().getBlockZ();
 		injuryRoll = (new Random()).nextInt(3) + 1;
 		
 		String message, reflexAlert, injuryAlert, alert, injury, deathLocation, damage;
 
 		message = "You have fallen " + w + "" + fallInFeet + "";
 		reflexAlert = r + "Reflex: " + w + rollPercent + r + "%";
 		injuryAlert = r + "  Injury: " + w + severityPercent + r + "%";
 		deathLocation = injured.getWorld().getName() + ": x: " + x + " y: " + y + " z: " + z;
 		alert = injured.getName() + b + " has just fallen " + w + fallDistance + b + " blocks, recieving ";
 		
 		if(type.equalsIgnoreCase("none")){
 			message += b + "feet";
 			injury = " escaping without injury.";
 			injured.sendMessage(reflexAlert);
 			injured.sendMessage(b+ message + b + injury);
 			
 		} else if (type.equalsIgnoreCase("major")) {
 			message += r + " feet, recieving ";
 			injury = dr + "error";
 			damage = dr + "error";
 			
 			if (severity < 16) {
 				damage = w + " 4" + r + " damage";
 				
 				switch (injuryRoll) {
 				case 1: injury = "a punctured organ. (anything but the heart);";
 					break;
 				case 2: injury = "a completely crushed limb.;";
 					break;
 				case 3: injury = "a cracked skull or cracked vertebra.;";
 					break;
 				default: 
 					break;
 				}
 				injured.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 35000, 3), true); 
 				injured.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 35000, 3), true);
 				
 			} else if (severity < 31) {
 				damage = w + " 3" + r + " damage";
 				
 				switch (injuryRoll) {
 				case 1: injury = "a crushed limb (Such as hand or foot);";
 					break;
 				case 2: injury = "shattered bones.;";
 					break;
 				case 3: injury = "a severe concussion.;";
 					break;
 				default: 
 					break;
 				}
 				injured.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 35000, 3), true);
 				
 			} else if (severity < 51) {
 				damage = w + " 2" + r + " damage";
 				
 				switch (injuryRoll) {
 				case 1: injury = "a cleanly broken bone;";
 					break;
 				case 2: injury = "a torn major muscle.;";
 					break;
 				case 3: injury = "the loss of a minor limb.;";
 					break;
 				default: 
 					break;
 				}
 				injured.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 35000, 2), true);
 				
 			}
 			injured.sendMessage(reflexAlert + injuryAlert);
 			injured.sendMessage(r + message + r + injury + damage);
 			
 			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
 				if (player.hasPermission("gildorym.falldamage.alert")) {
 					player.sendMessage(alert + r + injury + " major");
 					player.sendMessage(reflexAlert + r + "  Injury: " + w + severity);
 				}
 			}
 		} else if (type.equalsIgnoreCase("minor")) {
 			message += g + " feet, recieving ";
 			injury = dr + "error";
 			damage = dr + "error";
 			
 			if (severity < 11) {
 				damage = w + " 1" + g + " damage";
 				
 				switch (injuryRoll) {
 				case 1: injury = "a minor fracture;";
 					break;
 				case 2: injury = "bruised ribs.;";
 					break;
 				case 3: injury = "dislocation.;";
 					break;
 				default: 
 					break;
 				}
 				injured.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 35000, 1), true);
 				
 			} else if (severity < 21) {
 				damage = w + " 1" + g + " damage";
 				
 				switch (injuryRoll) {
 				case 1: injury = "a minor torn ligement;";
 					break;
 				case 2: injury = "chipped bone.;";
 					break;
 				case 3: injury = "light concussion.;";
 					break;
 				default: 
 					break;
 				}
 				injured.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 35000, 1), true);
 				
 			} else if (severity < 35) {
 				damage = w + " 1" + g + " damage";
 				
 				switch (injuryRoll) {
 				case 1: injury = "a mild sprain;";
 					break;
 				case 2: injury = "a mild laceration.;";
 					break;
 				case 3: injury = "a badly pulled muscle.;";
 					break;
 				default: 
 					break;
 				}
 				injured.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 15000, 1), true);
 				
 			} else if (severity < 50) {
 				damage = w + " 1" + g + " damage";
 				
 				switch (injuryRoll) {
 				case 1: injury = "bruises;";
 					break;
 				case 2: injury = "mild lacerations.;";
 					break;
 				case 3: injury = "minor sprain.;";
 					break;
 				default: 
 					break;
 				}
 				injured.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5000, 0), true);
 			} else if (severity == 50) {
 				injury = "only a few scratches.";
 				
 			}
 			injured.sendMessage(reflexAlert + injuryAlert);
 			injured.sendMessage(g + message + g + injury + damage);
 			
 			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
 				if (player.hasPermission("gildorym.falldamage.alert")) {
 					player.sendMessage(alert + g + injury + " minor");
 					player.sendMessage(reflexAlert + r + "  Injury: " + w + severity);
 				}
 			}
 		} else if(type.equalsIgnoreCase("death")){
 			message += dr + " feet, ";
 			injury = "and died";
 			injured.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999999, 10), true);
 			
 			injured.sendMessage(dr + message + dr + injury);
 			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
 				if (player.hasPermission("gildorym.falldamage.alert")) {
 					player.sendMessage(alert + dr + injury);
 					player.sendMessage(reflexAlert);
 					player.sendMessage(deathLocation);
 				}
 			}
 			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "warp deathbox " + injured.getName());
 		} else {
 			injured.sendMessage(ChatColor.RED + "Error : Injury type "
 					+ ChatColor.RESET + type + ChatColor.RED
 					+ " does not exist.");
 		}
 
 	}
 }
