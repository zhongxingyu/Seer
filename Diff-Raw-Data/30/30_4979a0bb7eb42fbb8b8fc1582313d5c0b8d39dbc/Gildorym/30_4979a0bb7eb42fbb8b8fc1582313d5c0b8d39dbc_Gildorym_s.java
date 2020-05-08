 package com.gildorymrp.gildorym;
 
 import java.util.Random;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 public class Gildorym extends JavaPlugin {
 
 	public void onEnable() {
 		this.getCommand("newcharacter").setExecutor(new NewCharacterCommand());
		this.getCommand("setname").setExecutor(new SetnameCommand());
		this.getCommand("setnameother").setExecutor(new SetnameOtherCommand());
 		this.getCommand("rollinfo").setExecutor(new RollInfoCommand());
 		this.getCommand("roll").setExecutor(new RollCommand());
 		this.getServer().getPluginManager()
 				.registerEvents(new EntityDamageByEntityListener(this), this);
 		MetaEditorCommands mec = new MetaEditorCommands();
 		this.getCommand("renameitem").setExecutor(mec);
 		this.getCommand("setlore").setExecutor(mec);
 		this.getCommand("addlore").setExecutor(mec);
 		this.getCommand("removelore").setExecutor(mec);
 		this.getCommand("signitem").setExecutor(mec);
 	}
 
 	public void onInjury(Player player, String type,int dieSize) {		
 		int severity = (new Random()).nextInt(dieSize) + 1;
 
 		if (type.equalsIgnoreCase("major")) {
 			if (severity < 16) {
 				player.sendMessage(ChatColor.RED
 						+ "You have fallen "
 						+ (int) Math.floor(player.getFallDistance())
 						+ " blocks, receiving a Punctured Organ (Anything but the Heart), Completely Crushed Limb, Cracked Skull or Cracked Vertebra; 4 Damage.");
 				player.addPotionEffect(new PotionEffect(
 						PotionEffectType.BLINDNESS, 35000, 4), true);
 				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,
 						35000, 4), true);
 			} else if (severity < 31) {
 				player.sendMessage(ChatColor.RED
 						+ "You have fallen "
 						+ (int) Math.floor(player.getFallDistance())
 						+ " blocks, receiving a Crushed Small limb (Such as hand/foot), Shattered Bones, or Severe Concussion; 3 Damage.");
 				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,
 						35000, 4), true);
 			} else if (severity < 51) {
 				player.sendMessage(ChatColor.RED
 						+ "You have fallen "
 						+ (int) Math.floor(player.getFallDistance())
 						+ " blocks, receiving a Cleanly Broken Bone, Torn Major Muscle, or Loss of a Minor Limb; 2 Damage.");
 				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,
 						35000, 3), true);
 			}
 		} else if (type.equalsIgnoreCase("minor")) {
 			if (severity < 11) {
 				player
 						.sendMessage(ChatColor.GOLD
 								+ "You have fallen "
 								+ (int) Math.floor(player
 										.getFallDistance())
 								+ " blocks, receiving a Minor Fracture, Brused Ribs, or Dislocation; 1 damage.");
 				player.addPotionEffect(
 						new PotionEffect(PotionEffectType.SLOW, 35000, 2), true);
 			} else if (severity < 21) {
 				player
 						.sendMessage(ChatColor.GOLD
 								+ "You have fallen "
 								+ (int) Math.floor(player
 										.getFallDistance())
 								+ " blocks, receiving a Minor Torn Ligement, Chipped Bone, or Light Concussion; 1 damage.");
 				player.addPotionEffect(
 						new PotionEffect(PotionEffectType.BLINDNESS, 35000, 2),
 						true);
 			} else if (severity < 35) {
 				player
 						.sendMessage(ChatColor.GOLD
 								+ "You have fallen "
 								+ (int) Math.floor(player
 										.getFallDistance())
 								+ " blocks, receiving a Mild Sprain, Mild Laceration, or Badly Pulled Muscle; 1 damage.");
 				player.addPotionEffect(
 						new PotionEffect(PotionEffectType.SLOW, 15000, 2), true);
 			} else if (severity < 50) {
 				player
 						.sendMessage(ChatColor.GOLD
 								+ "You have fallen "
 								+ (int) Math.floor(player
 										.getFallDistance())
 								+ " blocks, receiving Bruises, Mild Lacerations, or Minor Sprain; 1 damage.");
 				player.addPotionEffect(
 						new PotionEffect(PotionEffectType.SLOW, 5000, 1), true);
 			} else if (severity == 50) {
 				player.sendMessage(ChatColor.GOLD
 						+ "You have fallen "
 						+ (int) Math.floor(player.getFallDistance())
 						+ " blocks, receiving only a few scratches.");
 			}
 		} else {
 			player.sendMessage(ChatColor.RED + "Error : Injury type "
 					+ ChatColor.RESET + type + ChatColor.RED
 					+ " does not exist.");
 		}
 
 	}
 }
