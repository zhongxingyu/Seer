 package uk.co.CyniCode.CyniChat.Command;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import uk.co.CyniCode.CyniChat.DataManager;
 import uk.co.CyniCode.CyniChat.PermissionManager;
 import uk.co.CyniCode.CyniChat.objects.Channel;
 
 public class AdminCommand {
 
 	public static boolean create( CommandSender player, String name, String nick, Boolean protect ) {
 		if ( player instanceof Player )
 			if ( !PermissionManager.checkPerm( (Player) player, "cynichat.admin.create") )
 				return false;
 		
 		if ( DataManager.getChannel(name) != null ) {
 			player.sendMessage("This channel is already in existance");
 			return true;
 		}
 		Channel newChan = new Channel();
 		
 		if ( DataManager.hasNick( nick ) == true )
 			nick = name.substring(0, 2);
 		
		newChan.create( name.toLowerCase(), nick, protect );
 		DataManager.addChannel( newChan );
 		PermissionManager.addChannelPerms( player, newChan, protect );
 		player.sendMessage( "The channel: " + name + " has now been created" );
 		return true;
 	}
 
 	public static boolean remove( CommandSender player, String name ) {
 		if ( player instanceof Player )
 			if ( PermissionManager.checkPerm( (Player) player, "cynichat.admin.remove") )
 				return false;
 		
 		if ( DataManager.deleteChannel( name ) == true ) {
 			player.sendMessage("Channel has been removed");
 			return true;
 		}
 		player.sendMessage("This channel doesn't exist");
 		return true;
 	}
 
 	public static boolean createInfo( CommandSender player ) {
 		player.sendMessage(ChatColor.RED + "Incorrect Command");
 		player.sendMessage( "/ch create "+ChCommand.necessary("name")+" "+ChCommand.optional("nick") );
 		return true;
 	}
 
 	public static boolean removeInfo( CommandSender player ) {
 		player.sendMessage(ChatColor.RED + "Incorrect Command");
 		player.sendMessage( "/ch remove "+ChCommand.necessary("name") );
 		return true;
 	}
 }
