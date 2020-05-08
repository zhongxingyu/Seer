 package fr.crafter.tickleman.realadmintools;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Item;
 
 //######################################################################## RealAdminCommandEntities 
 public class RealAdminCommandEntities
 {
 
 	//--------------------------------------------------------------------------------------- command
 	static void command(RealAdminToolsPlugin plugin, CommandSender sender, String[] args)
 	{
 		String subCommand = args.length > 0 ? args[0].toLowerCase() : "";
 		try {
 			BufferedWriter writer = new BufferedWriter(
				new FileWriter(plugin.getDataFolder().getPath() + File.separator + "entities.txt")
 			);
 			writer.write("#class,id,itemTypeId,world,x,y,z\n");
 			String what = args.length > 1 ? args[1].toLowerCase() : "";
 			int removed_entities = 0;
 	    for (World world : plugin.getServer().getWorlds()) {
 	    	sender.sendMessage(world.getName() + " :");
 	    	sender.sendMessage("- " + world.getEntities().size() + " entities");
 	    	sender.sendMessage("- " + world.getLivingEntities().size() + " living entities");
 	    	sender.sendMessage("- " + world.getPlayers().size() + " players");
 	    	for (Entity entity : world.getEntities()) {
 	    		Item item = (entity instanceof Item ? (Item)entity : null);
 	    		writer.write(
 	    			entity.getClass().getName()
 	    			+ "," + entity.getEntityId()
 	    			+ "," + (item != null ? item.getItemStack().getTypeId() : "")
 	    			+ "," + world.getName()
 	    			+ "," + Math.round(Math.floor(entity.getLocation().getX()))
 	    			+ "," + Math.round(Math.floor(entity.getLocation().getY()))
 	    			+ "," + Math.round(Math.floor(entity.getLocation().getZ()))
 	    			+ "\n"
 	    		);
 	    		if (subCommand.equals("remove")) {
 	    			if (
 	    				what.equals(entity.getClass().getName().split(".entity.Craft")[1].toLowerCase())
 	    				|| what.equals("all")
 	    			) {
 	    				String id = args.length > 2 ? args[2].toLowerCase() : "";
 	    				String entityTypeId = (item != null ? "" + item.getItemStack().getTypeId() : "");
 	    				if (
 	    					id.equals(entityTypeId)
 	    					|| id.equals("all")
 	    				) {
 	    					entity.remove();
 	    					removed_entities ++;
 	    				}
 	    			}
 	    		}
 	    	}
 	    }
 	    if (subCommand.equals("remove")) {
 	    	sender.sendMessage("removed " + removed_entities + " " + what);
 	    }
 			writer.flush();
 			writer.close();
 		} catch (Exception e) {
 			plugin.getLog().severe("Could not save " + plugin.getDataFolder().getPath() + "/entities.txt file");
 		}
 	}
 
 }
