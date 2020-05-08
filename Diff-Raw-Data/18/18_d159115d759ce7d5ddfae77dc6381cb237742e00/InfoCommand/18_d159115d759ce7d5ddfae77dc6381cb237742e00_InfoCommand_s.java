 package com.github.thebiologist13.commands.spawners;
 
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import com.github.thebiologist13.CustomSpawners;
 import com.github.thebiologist13.SpawnableEntity;
 import com.github.thebiologist13.Spawner;
 
 public class InfoCommand extends SpawnerCommand {
 
 	public InfoCommand(CustomSpawners plugin) {
 		super(plugin);
 	}
 
 	public InfoCommand(CustomSpawners plugin, String mainPerm) {
 		super(plugin, mainPerm);
 	}
 
 	@Override
 	public void run(Spawner spawner, CommandSender sender, String subCommand, String[] args) {
 		
 		if(spawner.isHidden() && !permissible(sender, "customspawners.spawners.info.hidden")) {
 			PLUGIN.sendMessage(sender, ChatColor.RED + "You are not allowed to view info on that spawner!");
 			return;
 		}
 		
 		PLUGIN.sendMessage(sender, getInfo(spawner));
 		
 	}
 
 	private String[] getInfo(Spawner s) {
 		String typesMessage = "";
 		ArrayList<SpawnableEntity> types = new ArrayList<SpawnableEntity>();
 		for(Integer e : s.getTypeData()) {
 			types.add(CustomSpawners.getEntity(e.toString()));
 		}
 		
 		for(int i = 0; i < types.size(); i++) {
 			if(i == 0) {
 				if(types.get(i).getName().isEmpty()) {
 					typesMessage += types.get(i).getId();
 				} else {
					typesMessage += types.get(i).getId() + " (" + types.get(i).getName() + ")";
 				}
 			} else {
 				if(types.get(i).getName().isEmpty()) {
 					typesMessage += ", " + types.get(i).getId();
 				} else {
					typesMessage += ", " + types.get(i).getId() + " (" + types.get(i).getName() + ")";
 				}
 			}
 		}
 		
 		String modMessage = "";
 		int count = 0;
 		for(String str : s.getModifiers().keySet()) {
 			if(count == 0) {
 				modMessage += str + " = " + s.getModifier(str);
 			} else {
 				modMessage += ", " + str + " = " + s.getModifier(str);
 			}
 			count++;
 		}
 		
 		String times = "";
 		for(int j = 0; j < s.getSpawnTimes().size(); j++) {
 			if(j == 0) {
 				times += "" + s.getSpawnTimes().get(j);
 			} else {
 				times += ", " + s.getSpawnTimes().get(j);
 			}
 		}
 		
 		String header = ChatColor.GREEN + "Information on spawner with ID " + ChatColor.GOLD + String.valueOf(s.getId());
 		
 		if(!s.getName().isEmpty()) {
 			header += " (" + s.getName() + ")";
 		}
 		
 		String converted = "";
 		if(s.isConverted()) {
 			converted = ChatColor.RED + String.valueOf(s.isConverted());
 		} else {
 			converted = ChatColor.GREEN + String.valueOf(s.isConverted());
 		}
 		
 		String rate = "" + s.getRate();
 		rate = (s.hasModifier("rate")) ? rate + " (Dynamic)" : rate;
 		
 		String rad = "" + s.getRadius();
 		rad = (s.hasModifier("radius")) ? rad + " (Dynamic)" : rad;
 		
 		String mps = "" + s.getMobsPerSpawn();
 		mps = (s.hasModifier("mps")) ? mps + " (Dynamic)" : mps;
 		
 		String mobs = "" + s.getMaxMobs();
 		mobs = (s.hasModifier("maxmobs")) ? mobs + " (Dynamic)" : mobs;
 		
 		String maxL = "" + s.getMaxLightLevel();
 		maxL = (s.hasModifier("maxlight")) ? maxL + " (Dynamic)" : maxL;
 		
 		String minL = "" + s.getMinLightLevel();
 		minL = (s.hasModifier("minlight")) ? minL + " (Dynamic)" : minL;
 		
 		String maxD = "" + s.getMaxPlayerDistance();
 		maxD = (s.hasModifier("maxdistance")) ? maxD + " (Dynamic)" : maxD;
 		
 		String minD = "" + s.getMinPlayerDistance();
 		minD = (s.hasModifier("mindistance")) ? minD + " (Dynamic)" : minD;
 		
 		String[] message = {
 				"",
 				header + ChatColor.GREEN + ": ",
 				"",
 				ChatColor.GOLD + "Active: " + String.valueOf(s.isActive()),
 				ChatColor.GOLD + "Converted: " + converted,
 				ChatColor.GOLD + "Hidden: " + String.valueOf(s.isHidden()),
 				ChatColor.GOLD + "Types: " + typesMessage,
 				ChatColor.GOLD + "Location: " + "(" + s.getLoc().getBlockX() + ", " + s.getLoc().getBlockY() + ", " + s.getLoc().getBlockZ() + ")",
 				ChatColor.GOLD + "Spawn Rate: " + mps + " per " + rate + " ticks",
 				ChatColor.GOLD + "Spawn Radius: " + rad,
 				ChatColor.GOLD + "Maximum Mobs: " + mobs,
 				ChatColor.GOLD + "Maximum Light: " + maxL,
 				ChatColor.GOLD + "Minimum Light: " + minL,
 				ChatColor.GOLD + "Maximum Distance: " + maxD,
 				ChatColor.GOLD + "Minimum Distance: " + minD,
 				ChatColor.GOLD + "Redstone Triggered: " + String.valueOf(s.isRedstoneTriggered()),
 				ChatColor.GOLD + "Spawn When Powered: " + String.valueOf(s.isSpawnOnRedstone()),
 				ChatColor.GOLD + "Spawn Times: " + times,
 				ChatColor.GOLD + "Modifiers: " + modMessage,
 				ChatColor.GOLD + "Uses Spawn Area: " + String.valueOf(s.isUsingSpawnArea()),
 				ChatColor.GOLD + "Spawn Area Locations: ",
 				ChatColor.GOLD + "  Point 1 - (" + s.getAreaPoints()[0].getBlockX() + "," +
 						s.getAreaPoints()[0].getBlockY() + "," + s.getAreaPoints()[0].getBlockZ() + ") ",
 				ChatColor.GOLD + "  Point 2 - (" + s.getAreaPoints()[1].getBlockX() + "," + 
 						s.getAreaPoints()[1].getBlockY() + "," + s.getAreaPoints()[1].getBlockZ() + ")",
 				ChatColor.GREEN + "Scroll Up for More Properties."
 				};
 		
 		return message;
 		
 	}
 	
 }
