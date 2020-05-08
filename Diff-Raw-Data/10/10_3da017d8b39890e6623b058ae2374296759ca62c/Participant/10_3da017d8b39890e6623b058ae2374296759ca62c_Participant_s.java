 /*
  *     TitanChat
  *     Copyright (C) 2012  Nodin Chan <nodinchan@live.com>
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
 
 package com.titankingdoms.dev.titanchat.core.participant;
 
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 
 import com.titankingdoms.dev.titanchat.TitanChat;
 import com.titankingdoms.dev.titanchat.core.channel.Channel;
 import com.titankingdoms.dev.titanchat.util.C;
 
 public abstract class Participant {
 	
 	protected final TitanChat plugin;
 	
 	private final String name;
 	
 	private Channel current;
 	private final Map<String, Channel> channels;
 	
 	public Participant(String name) {
 		this.plugin = TitanChat.getInstance();
 		this.name = name;
 		this.channels = new ConcurrentHashMap<String, Channel>();
 	}
 	
 	public final void chat(String message) {
 		chat(current, message);
 	}
 	
 	public final void chat(Channel channel, String message) {
 		if (channel == null) {
 			send(C.GOLD + "You must be in a channel to speak");
 			return;
 		}
 		
 		channel.getChatHandler().processChat(this, channel, message);
 	}
 	
 	public final void direct(Channel channel) {
 		this.current = channel;
 		
 		if (!isParticipating(channel))
 			join(channel);
 	}
 	
 	public final Set<Channel> getChannels() {
 		return new HashSet<Channel>(channels.values());
 	}
 	
 	public abstract CommandSender getCommandSender();
 	
 	public ConfigurationSection getConfig() {
 		return plugin.getParticipantManager().getConfig().getConfigurationSection(name);
 	}
 	
 	public final Channel getCurrentChannel() {
 		return current;
 	}
 	
 	public String getDisplayName() {
		return getConfig().getString("display-name");
 	}
 	
 	public final String getName() {
 		return name;
 	}
 	
 	public abstract boolean hasPermission(String permission);
 	
 	public final boolean isDirectedAt(String channel) {
 		return (current != null) ? current.getName().equalsIgnoreCase(channel) : false;
 	}
 	
 	public final boolean isDirectedAt(Channel channel) {
 		return (channel != null) ? isDirectedAt(channel.getName()) : current == null;
 	}
 	
 	public abstract boolean isOnline();
 	
 	public final boolean isParticipating(String channel) {
 		return channels.containsKey(channel.toLowerCase());
 	}
 	
 	public final boolean isParticipating(Channel channel) {
 		return (channel != null) ? isParticipating(channel.getName()) : false;
 	}
 	
 	public final void join(Channel channel) {
 		if (channel == null)
 			return;
 		
 		this.channels.put(channel.getName().toLowerCase(), channel);
 		
 		if (this.current == null || !this.current.equals(channel))
 			direct(channel);
 		
 		if (!channel.isParticipating(this))
 			channel.join(this);
 	}
 	
 	public final void leave(Channel channel) {
 		if (channel == null)
 			return;
 		
 		this.channels.remove(channel.getName().toLowerCase());
 		
 		if (this.current != null && this.current.equals(channel))
 			direct(getChannels().iterator().hasNext() ? getChannels().iterator().next() : null);
 		
 		if (channel.isParticipating(this))
 			channel.leave(this);
 	}
 	
 	public abstract void send(String... messages);
}
