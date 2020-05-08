 package com.github.thebiologist13.commands.entities;
 
 import org.bukkit.ChatColor;
 import org.bukkit.DyeColor;
 import org.bukkit.command.CommandSender;
 
 import com.github.thebiologist13.CustomSpawners;
 import com.github.thebiologist13.SpawnableEntity;
 
 public class EntityColorCommand extends EntityCommand {
 
 	public EntityColorCommand(CustomSpawners plugin) {
 		super(plugin);
 	}
 
 	public EntityColorCommand(CustomSpawners plugin, String mainPerm) {
 		super(plugin, mainPerm);
 	}
 
 	@Override
 	public void run(SpawnableEntity entity, CommandSender sender, String subCommand, String[] args) {
 		
 		DyeColor color = null;
 		
 		String in = getValue(args, 0, "white");
 		
 		if(in.equals("lightblue")) {
 			color = DyeColor.LIGHT_BLUE;
 		} else if(in.equals("lightgreen")) {
 			color = DyeColor.LIME;
 		} else if(in.equals("lightgrey") || in.equals("lightgray")) {
 			color = DyeColor.SILVER;
 		} else if(in.equals("grey")) {
 			color = DyeColor.GRAY;
 		} else {
			color = DyeColor.valueOf(in);
 		}
 		
 		if(color == null) {
 			PLUGIN.sendMessage(sender, ChatColor.RED + "That is not a valid color.");
 			return;
 		}
 		
 		entity.setColor(color.toString());
 		
 		PLUGIN.sendMessage(sender, getSuccessMessage(entity, "color", in));
 		
 	}
 
 }
