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
 
 package com.titankingdoms.nodinchan.titanchat.permission;
 
 import java.util.EnumSet;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.plugin.PluginManager;
 
 import com.titankingdoms.nodinchan.titanchat.TitanChat;
 import com.titankingdoms.nodinchan.titanchat.channel.Channel;
 
 public final class DefaultPermissions {
 	
 	private final TitanChat plugin;
 	
 	protected static final PermissionDefault ALL = PermissionDefault.TRUE;
 	protected static final PermissionDefault NONE = PermissionDefault.FALSE;
 	protected static final PermissionDefault OP = PermissionDefault.OP;
 	protected static final PermissionDefault PLAYER = PermissionDefault.NOT_OP;
 	
 	private boolean loaded = false;
 	
 	public DefaultPermissions() {
 		this.plugin = TitanChat.getInstance();
 	}
 	
 	public DefaultPermissions load() {
 		if (loaded)
 			return this;
 		
 		PluginManager pm = plugin.getServer().getPluginManager();
 		
 		Permission staff = new Permission("TitanChat.staff", "Grants all permissions");
 		pm.addPermission(staff);
 		
 		Permission bypass = new Permission("TitanChat.bypass.*", "Grants bypasses to all channels");
 		pm.addPermission(bypass);
 		bypass.addParent(staff, true);
 		
 		pm.addPermission(new Permission("TitanChat.update", "Notified about updates"));
 		pm.getPermission("TitanChat.update").addParent(staff, true);
 		
 		pm.addPermission(new Permission("TitanChat.create", "Grants permission to channel creation"));
 		pm.addPermission(new Permission("TitanChat.delete", "Grants permission to channel deletion"));
 		
 		pm.getPermission("TitanChat.create").addParent(staff, true);
 		pm.getPermission("TitanChat.delete").addParent(staff, true);
 		
 		pm.addPermission(new Permission("TitanChat.broadcast", "Grants permission to the broadcast command"));
 		pm.addPermission(new Permission("TitanChat.whisper", "Grants permission to the whisper command"));
 		pm.addPermission(new Permission("TitanChat.emote", "Grants permission to the traditional emote command"));
 		pm.addPermission(new Permission("TitanChat.emote.*", "Grants permission to emote in all channels"));
 		
 		pm.getPermission("TitanChat.broadcast").addParent(staff, true);
 		pm.getPermission("TitanChat.whisper").addParent(staff, true);
 		pm.getPermission("TitanChat.emote").addParent(staff, true);
 		pm.getPermission("TitanChat.emote.*").addParent(staff, true);
 		
 		pm.addPermission(new Permission("TitanChat.force", "Grants permission to force channel joins"));
 		pm.getPermission("TitanChat.force").addParent(staff, true);
 		
 		Permission join = new Permission("TitanChat.join.*", "Grants permission to join all channels", ALL);
 		pm.addPermission(join);
 		join.addParent(bypass, true);
 		
 		Permission voice = new Permission("TitanChat.voice.*", "Grants speaking rights at all times in all channels");
 		pm.addPermission(voice);
 		voice.addParent(bypass, true);
 		
 		Permission speak = new Permission("TitanChat.speak.*", "Grants permission to speak in all channels");
 		pm.addPermission(speak);
 		speak.addParent(voice, true);
 		
 		Permission ban = new Permission("TitanChat.ban.*", "Grants permission to ban in all channels");
 		pm.addPermission(ban);
 		ban.addParent(staff, true);
 		
 		Permission kick = new Permission("TitanChat.kick.*", "Grants permission to kick in all channels");
 		pm.addPermission(kick);
 		kick.addParent(staff, true);
 		
 		Permission mute = new Permission("TitanChat.mute.*", "Grants permission to mute in all channels");
 		pm.addPermission(mute);
 		mute.addParent(staff, true);
 		
 		Permission rank = new Permission("TitanChat.rank.*", "Grants permission to promote and demote in all channels");
 		pm.addPermission(rank);
 		rank.addParent(staff, true);
 		
 		Permission codes = new Permission("TitanChat.format.*", "Grants permission to all format codes");
 		pm.addPermission(codes);
 		codes.addParent(bypass, true);
 		
 		for (ChatColor colourStyle : EnumSet.allOf(ChatColor.class)) {
 			char colourC = colourStyle.getChar();
 			Permission code = new Permission("TitanChat.format.&" + colourC, "Grants permission to the format code");
 			pm.addPermission(code);
 			code.addParent(codes, true);
 		}
 		
 		loaded = true;
 		return this;
 	}
 	
 	public void load(Channel channel) {
 		PluginManager pm = plugin.getServer().getPluginManager();
 		
 		String name = channel.getName();
 		
 		try {
 			Permission bypass = new Permission("TitanChat.bypass." + name, "Grants bypasses to " + name);
 			pm.addPermission(bypass);
 			
 		} catch (Exception e) {}
 		
 		try {
 			Permission spawn = new Permission("TitanChat.spawn." + name, "Sets the channel as default spawn", NONE);
 			pm.addPermission(spawn);
 			
 		} catch (Exception e) {}
 		
 		try {
 			Permission join = new Permission("TitanChat.join." + name, "Grants permission to join " + name);
 			pm.addPermission(join);
 			join.addParent("TitanChat.bypass." + name, true);
 			
 		} catch (Exception e) {}
 		
 		try {
 			Permission voice = new Permission("TitanChat.voice." + name, "Grants speaking rights at all times in " + name);
 			pm.addPermission(voice);
 			voice.addParent("TitanChat.bypass." + name, true);
 			
 		} catch (Exception e) {}
 		
 		try {
 			Permission speak = new Permission("TitanChat.speak." + name, "Grants permission to speak in " + name);
 			pm.addPermission(speak);
			speak.addParent("TitanChat.speak." + name, true);
 			
 		} catch (Exception e) {}
 		
 		try {
 			Permission ban = new Permission("TitanChat.ban." + name, "Grants permission to ban in " + name);
 			pm.addPermission(ban);
 			ban.addParent("TitanChat.ban.*", true);
 			
 		} catch (Exception e) {}
 		
 		try {
 			Permission kick = new Permission("TitanChat.kick." + name, "Grants permission to kick in " + name);
 			pm.addPermission(kick);
 			kick.addParent("TitanChat.kick.*", true);
 			
 		} catch (Exception e) {}
 		
 		try {
 			Permission mute = new Permission("TitanChat.mute." + name, "Grants permission to mute in " + name);
 			pm.addPermission(mute);
 			mute.addParent("TitanChat.mute.*", true);
 			
 		} catch (Exception e) {}
 		
 		try {
 			Permission rank = new Permission("TitanChat.rank." + name, "Grants permission to promote and demote in " + name);
 			pm.addPermission(rank);
 			rank.addParent("TitanChat.rank.*", true);
 			
 		} catch (Exception e) {}
 		
 		try {
 			Permission emote = new Permission("TitanChat.emote." + name, "Grants permission to emote in " + name);
 			pm.addPermission(emote);
 			emote.addParent("TitanChat.emote.*", true);
 			
 		} catch (Exception e) {}
 	}
 	
 	public void load(List<Channel> channels) {
 		for (Channel channel : channels)
 			load(channel);
 	}
 }
