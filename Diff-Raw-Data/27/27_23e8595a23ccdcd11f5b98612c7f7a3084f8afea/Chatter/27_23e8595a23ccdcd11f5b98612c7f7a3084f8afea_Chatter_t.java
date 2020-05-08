 package uk.co.CyniCode.CyniChat.Chatting;
 
 import java.util.Iterator;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import uk.co.CyniCode.CyniChat.CyniChat;
 import uk.co.CyniCode.CyniChat.DataManager;
 import uk.co.CyniCode.CyniChat.PermissionManager;
 import uk.co.CyniCode.CyniChat.Channel.Channel;
 
 public class Chatter implements Listener {
 	
 	public static CyniChat plugin;
 	
 	/**
 	 * Listen for any people joining the server so we can load in their configurations or generate a new one
 	 * @param event : This is what we're listening for
 	 */
 	@EventHandler(priority = EventPriority.MONITOR)
 	public static void joinEvent( PlayerJoinEvent event ) {
 		Player player = event.getPlayer();
 		CyniChat.printDebug("Player joined");
 		DataManager.bindPlayer(player);//Load player details into online users.
 	}
 	
 	/**
 	 * Listen for anyone leaving the server so that we can dump their UserDetails into the config and have shot of them
 	 * @param event : This is what we're listening for
 	 */
 	@EventHandler(priority = EventPriority.MONITOR)
 	public static void leaveEvent( PlayerQuitEvent event ) {
 		CyniChat.printDebug("Player Left");
 		DataManager.unbindPlayer(event.getPlayer());
 	}
 	
 	/**
 	 * Listen for any chatter on the server so that I can print debug of it for the moment
 	 * @param event : This is what we're listening for
 	 */
 	@EventHandler(priority = EventPriority.MONITOR)
 	public static void chatEvent( AsyncPlayerChatEvent event ) {
 		CyniChat.printDebug( "Format ::== " + event.getFormat() );
 		//CyniChat.printDebug( "Recipients ::== " + looper( event.getRecipients() ) );
 		Player player = event.getPlayer();
 		UserDetails user = DataManager.getOnlineDetails( player );
 		Channel current = DataManager.getChannel( user.getCurrentChannel() );
 		String format = "<CurrentChannel> <player> : <message>";
 		format = format.replace("<CurrentChannel>", current.getColour()+"["+current.getNick()+"]" );
 		format = format.replace("player", "%1$s");
 		//format = format.replace("<prefix>", PermissionManager.getPrefix( player ) );
 		//format = format.replace("<suffix>", PermissionManager.getSuffix( player ) );
 		format = format.replace("<message>", current.getColour()+"%2$s");
 		event.setFormat(format);
 		CyniChat.printDebug( "Format ::== " + event.getFormat() );
 		Iterator<Player> receivers = event.getRecipients().iterator();
		Player[] all = new Player[ event.getRecipients().size() ];
		int Count = 0;
 		
 		while ( receivers.hasNext() ) {
 			Player currentPlayer = receivers.next();
 			UserDetails users = DataManager.getOnlineDetails( currentPlayer );
 			CyniChat.printDebug( currentPlayer.getName() + " : " + users.getAllVerboseChannels() );
 			if ( !users.getAllChannels().contains( current.getName() ) ) {
				all[Count] = currentPlayer;
				Count++;
			}
		}
		if ( Count > 0 ) { for ( int i=0; i<Count; i++ ) {
				CyniChat.printDebug("Iteration = " + i );
				CyniChat.printDebug("Removed "+all[i].getDisplayName() );
				event.getRecipients().remove( all[i] );
 			}
 		}
 	}
 	
 	/**
 	 * Loop through a set
 	 * @param item : This is what we're iterating over
 	 * @return the strings within the set
 	 */
 	public static String looper( Set<Player> item ) {
 		String recip = null;
 		int j = item.size();
 		String[] arrItem = null;
 		item.toArray(arrItem);
 		for ( int i=0; i<j; i++ ) {
 			recip += arrItem[i]+", ";
 		}
 		return recip;
 	}
 }
