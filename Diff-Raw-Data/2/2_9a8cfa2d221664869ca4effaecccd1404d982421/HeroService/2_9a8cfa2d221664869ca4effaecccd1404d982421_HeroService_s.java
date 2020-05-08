 package com.comphenix.xp.messages;
 
 import org.bukkit.entity.Player;
 
 import com.dthielke.herochat.Channel;
 import com.dthielke.herochat.Chatter;
 import com.dthielke.herochat.Herochat;
 
 public class HeroService implements ChannelService {
 
 	public static final String NAME = "HEROCHAT";
 	
 	public HeroService() {
 		// Make sure we haven't screwed up
 		if (!exists())
 			throw new IllegalArgumentException("HeroChat hasn't been enabled.");
 	}
 	
 	/**
 	 * Determines whether or not the HeroChat plugin is loaded AND enabled.
 	 * @return TRUE if it is, FALSE otherwise.
 	 */
 	public static boolean exists() {
 		try {
 			// Make sure
 			if (Herochat.getPlugin().isEnabled())
 				return true;
 			else
 				return false;
 			
 			// Cannot load plugin
		} catch (Exception e) {
 			return false;
 		}
 	}
 	
 	@Override
 	public boolean hasChannel(String channelID) {
 		// See if this channel exists
 		return Herochat.getChannelManager().hasChannel(channelID);
 	}
 
 	@Override
 	public void announce(String channelID, String message) {
 	
 		// Stores channels in a HashMap, so it should return NULL if the channel doesn't exist 
 		Channel channel = Herochat.getChannelManager().getChannel(channelID);
 			
 		if (channel == null) {
 			throw new IllegalArgumentException("Channel doesn't exist.");
 		}
 		
 		channel.announce(message);
 	}
 
 	@Override
 	public void emote(String channelID, String message, Player sender) {
 
 		Chatter playerChatter = Herochat.getChatterManager().getChatter(sender);
 		
 		// Emote for this character
 		getChannel(channelID).emote(playerChatter, message);
 	}
 
 	private Channel getChannel(String channelID) {
 		
 		// Stores channels in a HashMap, so it should return NULL if the channel doesn't exist 
 		Channel channel = Herochat.getChannelManager().getChannel(channelID);
 			
 		if (channel == null) {
 			throw new IllegalArgumentException("Channel doesn't exist.");
 		} else {
 			return channel;
 		}
 	}
 	
 	@Override
 	public String getServiceName() {
 		return NAME;
 	}
 }
