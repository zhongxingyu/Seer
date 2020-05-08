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
 
 	public void onInjury(Player player, String type, int dieSize, int fallDistance, int roll) {
 		int severity, fallInFeet, rollPercent, severityPercent;
 		
 		fallInFeet = fallDistance * 3;
 		severity = (new Random()).nextInt(dieSize) + 1;
 		rollPercent = roll * 5;
 		severityPercent = severity * 2;
 		
 		String messageP1, messageP2, reflex, injury, alert1, alert2; 
 
 		messageP1 = "You have fallen ";
 		messageP2 = ChatColor.WHITE + "" + fallInFeet + "";
 		reflex = ChatColor.RED + "Reflex: " + rollPercent + "%";
 		injury = ChatColor.RED + "  Injury: " + severityPercent + "%";
 		alert1 = player.getName() + ChatColor.BLUE + " has just fallen " + ChatColor.WHITE + fallDistance + ChatColor.BLUE + " blocks, taking a " + ChatColor.DARK_RED + " major " + ChatColor.BLUE + "injury. Check on them if you want.";
 		alert2 = player.getName() + ChatColor.BLUE + " has just fallen " + ChatColor.WHITE + fallDistance + ChatColor.BLUE + " blocks, taking a " + ChatColor.GOLD + " minor " + ChatColor.BLUE + "injury. Check on them if you want.";
 		
 		if(type.equalsIgnoreCase("none")){
 			player.sendMessage(reflex);
			player.sendMessage(ChatColor.BLUE + messageP1 + messageP2 + ChatColor.BLUE + "escaping without injury.");
 			
 		} else if (type.equalsIgnoreCase("major")) {
 			for (Player player2 : Bukkit.getServer().getOnlinePlayers()) {
 				if (player2.hasPermission("gildorym.falldamage.alert")) {
 					player2.sendMessage(alert1);
 				}
 			}
 			
 			if (severity < 16) {
 				player.sendMessage(reflex + injury);
 				player.sendMessage(ChatColor.RED + messageP1 + messageP2 + " feet, receiving a Punctured Organ (Anything but the Heart), Completely Crushed Limb, Cracked Skull or Cracked Vertebra; 4 Damage.");
 				player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 35000, 4), true); 
 				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 35000, 4), true);
 				
 			} else if (severity < 31) {
 				player.sendMessage(reflex + injury);
 				player.sendMessage(ChatColor.RED + messageP1 + messageP2 + " feet, receiving a Crushed Small limb (Such as hand/foot), Shattered Bones, or Severe Concussion; 3 Damage.");
 				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 35000, 4), true);
 				
 			} else if (severity < 51) {
 				player.sendMessage(reflex + injury);
 				player.sendMessage(ChatColor.RED + messageP1 + messageP2 + " feet, receiving a Cleanly Broken Bone, Torn Major Muscle, or Loss of a Minor Limb; 2 Damage.");
 				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 35000, 3), true);
 				
 			}
 		} else if (type.equalsIgnoreCase("minor")) {
 			for (Player player2 : Bukkit.getServer()
 					.getOnlinePlayers()) {
 				if (player2.hasPermission("gildorym.falldamage.alert")) {
 					player2.sendMessage(alert2);
 				}
 			}
 			
 			if (severity < 11) {
 				player.sendMessage(reflex + injury);
 				player.sendMessage(ChatColor.GOLD + messageP1 + messageP2 + " feet, receiving a Minor Fracture, Brused Ribs, or Dislocation; 1 damage.");
 				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 35000, 2), true);
 				
 			} else if (severity < 21) {
 				player.sendMessage(reflex + injury);
 				player.sendMessage(ChatColor.GOLD + messageP1 + messageP2 + " feet, receiving a Minor Torn Ligement, Chipped Bone, or Light Concussion; 1 damage.");
 				player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 35000, 2), true);
 				
 			} else if (severity < 35) {
 				player.sendMessage(reflex + injury);
 				player.sendMessage(ChatColor.GOLD + messageP1 + messageP2 + " feet, receiving a Mild Sprain, Mild Laceration, or Badly Pulled Muscle; 1 damage.");
 				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 15000, 2), true);
 				
 			} else if (severity < 50) {
 				player.sendMessage(reflex + injury);
 				player.sendMessage(ChatColor.GOLD + messageP1 + messageP2 + " feet, receiving Bruises, Mild Lacerations, or Minor Sprain; 1 damage.");
 				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5000, 1), true);
 			} else if (severity == 50) {
 				player.sendMessage(reflex + injury);
 				player.sendMessage(ChatColor.GOLD + messageP1 + messageP2 + " feet, receiving only a few scratches.");
 				
 			}
 		} else if(type.equalsIgnoreCase("death")){
 			for (Player player2 : Bukkit.getServer()
 					.getOnlinePlayers()) {
 				if (player2.hasPermission("gildorym.falldamage.alert")) {
 					player2.sendMessage(player.getName() + ChatColor.BLUE + " has just fallen " + fallDistance + " blocks, and " + ChatColor.DARK_RED + "died.");
 				}
 			}
			player.sendMessage(ChatColor.DARK_RED + "You have fallen " + fallDistance * 3 + " feet, and died.");
 			player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999999, 10), true);
 			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "warp deathbox " + player.getName());
 		} else {
 			player.sendMessage(ChatColor.RED + "Error : Injury type "
 					+ ChatColor.RESET + type + ChatColor.RED
 					+ " does not exist.");
 		}
 
 	}
 }
