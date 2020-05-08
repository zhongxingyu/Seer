 package net.yeticraft.xxtraineexx.SelfGroup;
 
 
 import net.yeticraft.xxtraineexx.friendsandfoes.utilities.FFConfigHandler;
 
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import ru.tehkode.permissions.*;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 public class SelfGroup extends JavaPlugin {
 	public String pluginName = "YetiFaction"; // Need to do this because there is no way to load the PDF at initialisation time.
 	public final Log logger = new Log(pluginName);
 	@SuppressWarnings("unused")
 	private SelfGroupListener sListener = null;
 		
 	public FileConfiguration config;
 	public ArrayList<String> valid_groups;
 	public int switch_delay; // Days a player must wait to switch again.
 	
 	DateFormat date_format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 	
 	@Override
 	public void onDisable() {
 		PluginDescriptionFile pdffile = this.getDescription();
 		this.logger.info(pdffile.getName() + " version " + pdffile.getVersion() + " is disabled.");
 	}
 	@Override
 	public void onEnable() {
 		sListener = new SelfGroupListener(this);
 		PluginDescriptionFile pdffile = this.getDescription();
 		this.logger.info(pdffile.getName() + " version " + pdffile.getVersion() + " is enabled.");
 		Util.init(this, pluginName, logger);
 		
 		
 		// start real code
 		
 		CommandExecutor SelfGroupCommandExecutor = new SelfGroupCommand(this);
 		getCommand("yetifaction").setExecutor(SelfGroupCommandExecutor);
     	getCommand("yf").setExecutor(SelfGroupCommandExecutor);
     	
     	// Read the config file
     	config = getConfig();
     	
     	// Read the groups from the config file
     	List<String> grps = config.getStringList("groups");
     	if ((grps == null) || (grps.size() == 0))
     	{
     		// If no list was supplied, use the yeti groups
     		grps = new ArrayList<String>();
     		grps.add("Sasquai");
     		grps.add("Sasquai_Front");
     		grps.add("Almas");
     		grps.add("Yowie_Front");
     		grps.add("Yowie");
     		config.set("groups", grps);
     		saveConfig();
     	}
     	  
     	// Store the valid groups in our working list
     	valid_groups = new ArrayList<String>();
     	for (int i = 0; i < grps.size(); i++)
     	{
     		valid_groups.add(grps.get(i));    		
     		
     	}
     	
     	switch_delay = config.getInt("switch-delay", 1);
     	
     	if (switch_delay == 1)
     	{
     		config.set("switch-delay", 1);
     		saveConfig();
     	}
     	
 	}
 	
 	public ArrayList<String> getGroups()
 	{	
 		return valid_groups;
 	}
 
 	public Boolean JoinGroup(CommandSender sender, String group)
 	{
 		PermissionManager pex = PermissionsEx.getPermissionManager();
 		PermissionUser pUser = pex.getUser(sender.getName());
         Boolean group_valid = false;
 		
         // Checking to see if they are allowed to swap groups. If not, return out.
      	if (!checkSwitchDelay(sender, group)) return false;
         
         
		// Looping to see if they are already in a group
        for (int i = 0; i < valid_groups.size(); i++)
        {
        	if (pUser.inGroup(valid_groups.get(i)))
        	{
        		Util.reply(sender, "Sorry, you're already in the faction %s", valid_groups.get(i));
        		return false;
        	}
        	
        }
		
         // Looping to see if the group they provided is a valid group.
         for (int i = 0; i < valid_groups.size(); i++)
         {
         	if (valid_groups.get(i).equalsIgnoreCase(group))
         	{
         		group_valid = true;
         		group=valid_groups.get(i);
         	}
         	
         }
         
        
         // do some actions if the group is valid for join
         if (group_valid)
         {
     		String now = date_format.format(Calendar.getInstance().getTime());
     		config.set("change_times."+sender.getName(), now);
     		config.set("last_groups."+sender.getName(), group);
     		saveConfig();
     		this.logger.info("User %s has joined the %s faction", sender.getName(), group);
         	pUser.addGroup(group);
         	return true;
         }
         else
         {
         	Util.reply(sender, "Sorry, %s is not a valid faction name", group);
         	return false;
         }
         
 	}
 	
 	
 	
 	public Boolean LeaveGroup(CommandSender sender, String group)
 	{
 		PermissionManager pex = PermissionsEx.getPermissionManager();
 		PermissionUser pUser = pex.getUser(sender.getName());
 		Boolean group_valid = false;
 		
 		// Checking to see if they are allowed to swap groups. If not, return out.
 		if (!checkSwitchDelay(sender, group)) return false;
 		
 		// Check to see if they are in this faction. If not... they cant leave. Return out.
 		if (!pUser.inGroup(group))
 		{
 			Util.reply(sender, "Sorry, you're not in the faction %s", group);
 			return false;
 		}
 		
 		// Looping to see if this is a valid group to leave. If so, set the flag.
         for (int i = 0; i < valid_groups.size(); i++)
         {
         	if (valid_groups.get(i).equalsIgnoreCase(group))
         	{
         		 group_valid = true;
         		 group=valid_groups.get(i);
         	}
         }
         
         // doing some actions on the flag
         if (group_valid){
         	String now = date_format.format(Calendar.getInstance().getTime());
     		config.set("change_times."+sender.getName(), now);
     		config.set("last_groups."+sender.getName(), group);
     		saveConfig();
 			
     		this.logger.info("User %s has left the %s faction", sender.getName(), group);
 			pUser.removeGroup(group);
 			
 			// Remove all friends to stop cheaters
 			FFConfigHandler.loadFriendsList();
 			FFConfigHandler.friendslist.set(pUser.getName(), null); //Delete it
 			FFConfigHandler.saveFriendsList(); //Save
 			return true;
         }
         else{
         	// If we made it here, we didn't find the group they tried to remove
             Util.reply(sender, "Sorry, %s is not a valid faction", group);
             return false;
         }
         
         
         
 	}
 	
 	public boolean checkSwitchDelay(CommandSender sender, String group){
 		
 		// This method will return TRUE if the player is allowed to switch factions
 		// It will return FALSE if they are not allowed to swap factions.
 		// The faction check will see if they have a previously stored "swap time"
 		// If so, it will verify it meets the switch delay.
 		
 		Date now = Calendar.getInstance().getTime();
 		// Check the last time the user made an update and see if they've waited long enough
 		String last_updated = config.getString("change_times."+sender.getName());
 		//String last_group = config.getString("last_groups."+sender.getName());
 		if (last_updated != null)
 		{
 		
 			try
 			{
 				Date last = date_format.parse(last_updated);
 				long last_seconds = last.getTime() / 1000; // seconds since epoch for last
 				long now_seconds = now.getTime() / 1000;  // seconds since epoch for now
 				// seconds needed for switch delay = 
 				// delayed days x hours in a day x mins in hour x secs in min 
 				long need_millis = (long)switch_delay * 24 * 60 * 60;
 				if ((now_seconds - last_seconds) < need_millis)
 				{
 					long still_need = need_millis - (now_seconds - last_seconds);
 					String msg = "You have already left or joined a faction in the past "+ switch_delay + " days.  Try again in:";
 					long need_days = still_need / (24 * 60 * 60);
 					still_need = still_need % (24 * 60 * 60);
 					long need_hours = still_need / (60 * 60);
 					still_need = still_need % (60 * 60);
 					long need_minutes = still_need / 60;
 					still_need = still_need % 60;
 					msg += need_days + "days, " + need_hours + "hrs, " 
 							+ need_minutes + "min, and " + still_need + "s";
 					Util.reply(sender, msg);
 					return false;
 				}
 			}
 		catch (ParseException pe) { /*ignore if it didn't parse */}
 		
 		}
 		
 		return true;
 		
 	}
 	
 	public void checkGroup(CommandSender sender)
 	{
 		PermissionManager pex = PermissionsEx.getPermissionManager();
 		PermissionUser pUser = pex.getUser(sender.getName());
 		
         for (int i = 0; i < valid_groups.size(); i++)
         {
         	if (pUser.inGroup(valid_groups.get(i)))
         	{
         		Util.reply(sender, "You are currently in the " + valid_groups.get(i) + " faction.");
         		return;
         	}
         	
         }
         
         Util.reply(sender, "You are not currently in a faction.");
 		return;
 		
 	}
 }
