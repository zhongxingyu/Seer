 package com.randrdevelopment.propertygroup.command.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.randrdevelopment.propertygroup.PropertyGroup;
 import com.randrdevelopment.propertygroup.PropertyGroupConfig;
 import com.randrdevelopment.propertygroup.command.BaseCommand;
 import com.randrdevelopment.propertygroup.regions.RegionTools;
 
 public class AddToPropertyCommand extends BaseCommand{
 	private PropertyGroupConfig propertyConfig = null;
 	
 	public AddToPropertyCommand(PropertyGroup plugin) {
         super(plugin);
         name = "AddToProperty";
         description = "Adds a player to your property.";
         usage = "/property AddToProperty <player> [property]";
         minArgs = 1;
         maxArgs = 2;
         identifiers.add("property addtoproperty");
         identifiers.add("add");
     }
 	
     @Override
     public void execute(CommandSender sender, String[] args) {
     	propertyConfig = plugin.getPropertyConfig();
     	Player player = (Player)sender;
 		String playerName = player.getName();
 		
     	if (args.length == 1)
     	{
     		// Set to all properties player owns
     		
     		// Loop though all the property groups, and match the owner
     		for (String s : propertyConfig.getKeys(false)) {
             	// s = Property Group
             	int rows = propertyConfig.getInt(s + ".rows");
             	int cols = propertyConfig.getInt(s + ".cols");
     			int properties = rows * cols;
     			
     			for(int i=1; i<=properties; i++) {
     				String PropertyOwner = propertyConfig.getString(s + ".properties."+i+".owner");
     				String worldName = propertyConfig.getString(s + ".startlocation.world");
     				if (PropertyOwner != null) {
     					if (PropertyOwner.equalsIgnoreCase(playerName)) {
     						addMemberToProperty(s, Integer.toString(i), args[0], sender, worldName);
     					}
     				}
     			}
             }
     	}
     	else
     	{
     		// Set to specific property the player ownes
     		
     		// Get property group name and property number
     		String[] pgargs = args[1].split("-");
     		if (pgargs.length != 2) {
     			sender.sendMessage(plugin.getTag() + ChatColor.RED + "Property Group Name is not Valid.");
     			return;
     		}
     		String propertyGroup = pgargs[0];
     		String propertyNumber = pgargs[1];
     		
     		// Validate the property group exists
     		if (propertyConfig.getConfigurationSection(propertyGroup) == null) {
     			sender.sendMessage(plugin.getTag() + ChatColor.RED + "Property Group does not exist.");
     			return;
     		}
     		
     		// Validate the property group is created
     		if (propertyConfig.getBoolean(propertyGroup + ".properties."+propertyNumber+".created") == false) {
     			sender.sendMessage(plugin.getTag() + ChatColor.RED + "Property not created");
     			return;
     		}
     		
     		// Validate sender is owner of the property group
     		String PropertyOwner = propertyConfig.getString(propertyGroup + ".properties."+propertyNumber+".owner");
     		if (!PropertyOwner.equalsIgnoreCase(playerName)) {
     			// Not owner, but maybe can override (do we have permission?)
     			if (!sender.hasPermission("propertygroup.addoverride"))
     	    	{
     				sender.sendMessage(plugin.getTag() + ChatColor.RED + "You are not the owner of that property");
         			return;
     	    	}
     		}
     		
     		// startlocation.world
     	    String worldName = propertyConfig.getString(propertyGroup + ".startlocation.world");
     		
     		// Add player to the property group
     		addMemberToProperty(propertyGroup, propertyNumber, args[0], sender, worldName);
     	}
     }
    
     private void addMemberToProperty(String propertyGroup, String propertyNumber, String RemotePlayerName, CommandSender sender, String worldName) {
     	if (RegionTools.addMemberToProtectedRegion(propertyGroup+"-"+propertyNumber, worldName, RemotePlayerName)) {
     		sender.sendMessage(plugin.getTag()+"Added "+RemotePlayerName+" to Property "+propertyGroup+"-"+propertyNumber);
    		propertyConfig.set(propertyGroup+".properties."+propertyNumber+".members."+RemotePlayerName, true);
    		propertyConfig.save();
     	} else {
     		sender.sendMessage(plugin.getTag()+"Error adding "+RemotePlayerName+" to Property "+propertyGroup+"-"+propertyNumber);
     	}
     }
 }
