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
 
 package com.titankingdoms.nodinchan.titanchat.core.participant;
 
 import java.io.File;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 import com.titankingdoms.nodinchan.titanchat.TitanChat;
 import com.titankingdoms.nodinchan.titanchat.core.channel.Channel;
 
 public final class ParticipantManager {
 	
 	private final TitanChat plugin;
 	
 	private File configFile;
 	private FileConfiguration config;
 	
 	private Map<String, Participant> participants;
 	
 	public ParticipantManager() {
 		this.plugin = TitanChat.getInstance();
 		this.participants = new HashMap<String, Participant>();
 	}
 	
 	public boolean existingParticipant(String name) {
 		return participants.containsKey(name.toLowerCase());
 	}
 	
 	public boolean existingParticipant(Player player) {
 		return existingParticipant(player.getName());
 	}
 	
 	public FileConfiguration getConfig() {
 		if (config == null)
 			reloadConfig();
 		
 		return config;
 	}
 	
 	public Participant getParticipant(String name) {
		if (!existingParticipant(name))
			registerParticipant(name);
		
 		return participants.get(name.toLowerCase());
 	}
 	
 	public Participant getParticipant(CommandSender sender) {
 		return getParticipant(sender.getName());
 	}
 	
 	public Participant registerParticipant(Player player) {
		return registerParticipant(player.getName());
	}
	
	private Participant registerParticipant(String name) {
		if (!existingParticipant(name))
			participants.put(name.toLowerCase(), new ChannelParticipant(name));
 		
		Participant participant = getParticipant(name);
 		
 		for (Channel channel : plugin.getChannelManager().getChannels()) {
 			if (participant.hasPermission("TitanChat.autojoin." + channel.getName()))
 				channel.join(participant);
 			
 			if (participant.hasPermission("TitanChat.autoleave." + channel.getName()))
 				channel.leave(participant);
 		}
 		
 		return participant;
 	}
 	
 	public void reloadConfig() {
 		if (configFile == null)
 			configFile = new File(plugin.getDataFolder(), "participants.yml");
 		
 		config = YamlConfiguration.loadConfiguration(configFile);
 		
 		InputStream defConfigStream = plugin.getResource("participants.yml");
 		
 		if (defConfigStream != null)
 			config.setDefaults(YamlConfiguration.loadConfiguration(defConfigStream));
 	}
 	
 	public void saveConfig() {
 		if (configFile == null || config == null)
 			return;
 		
 		try { config.save(configFile); } catch (Exception e) {}
 	}
 }
