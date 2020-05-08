 package com.titankingdoms.nodinchan.titanchat.channel.util;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.entity.Player;
 
 import com.titankingdoms.nodinchan.titanchat.TitanChat;
 import com.titankingdoms.nodinchan.titanchat.channel.Channel;
 
 /*     Copyright (C) 2012  Nodin Chan <nodinchan@live.com>
  * 
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  * 
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  * 
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 public final class Participant {
 	
 	private final TitanChat plugin;
 	
 	private final String name;
 	
 	private Channel currentChannel;
 	
 	private final Map<String, Channel> channels;
 	
 	private final Invitation invitation;
 	
 	private final Map<String, Boolean> muted;
 	
 	public Participant(Player player) {
 		this.plugin = TitanChat.getInstance();
 		this.name = player.getName();
 		this.channels = new HashMap<String, Channel>();
 		this.invitation = new Invitation(player);
 		this.muted = new HashMap<String, Boolean>();
 	}
 	
 	public List<Channel> getChannels() {
 		return new ArrayList<Channel>(channels.values());
 	}
 	
 	public Channel getCurrentChannel() {
 		return currentChannel;
 	}
 	
 	public Invitation getInvitation() {
 		return invitation;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public Player getPlayer() {
 		return plugin.getPlayer(name);
 	}
 	
 	public boolean isMuted(Channel channel) {
 		if (!muted.containsKey(channel.getName().toLowerCase()))
 			return false;
 		
 		return muted.get(channel.getName().toLowerCase());
 	}
 	
 	public void join(Channel channel) {
 		if (channel == null)
 			return;
 		
		if (!currentChannel.equals(channel))
 			currentChannel = channel;
 		
 		if (!channels.containsKey(channel.getName()))
 			channels.put(channel.getName(), channel);
 	}
 	
 	public void leave(Channel channel) {
 		if (channel == null)
 			return;
 		
 		if (channels.containsKey(channel.getName()))
 			channels.remove(channel.getName());
 		
		if (currentChannel.equals(channel)) {
 			if (channels.isEmpty())
 				currentChannel = null;
 			else
 				currentChannel = getChannels().get(0);
 		}
 	}
 	
 	public void mute(Channel channel, boolean mute) {
 		muted.put(channel.getName().toLowerCase(), mute);
 	}
 }
