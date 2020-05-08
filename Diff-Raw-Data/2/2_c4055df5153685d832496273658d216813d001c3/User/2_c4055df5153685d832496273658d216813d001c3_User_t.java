 package com.benzrf.sblock.sburbchat;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 import com.benzrf.sblock.sburbchat.channel.AccessLevel;
 import com.benzrf.sblock.sburbchat.channel.ChannelType;
 import com.benzrf.sblock.sburbchat.channel.channels.Channel;
 import com.benzrf.sblock.sburbchat.commandparser.PrivilegeLevel;
 
 public class User
 {
 	public static void addPlayer(Player p) throws IOException
 	{
 		if (users.get(p.getName()) == null)
 		{
 			if (new File("plugins/SburbChat/u_" + p.getName() + ".scd").exists())
 			{
 				String pdata;
 				FileInputStream stream = new FileInputStream(new File("plugins/SburbChat/u_" + p.getName() + ".scd"));
 				try
 				{
 					FileChannel fc = stream.getChannel();
 					MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
 					pdata = Charset.defaultCharset().decode(bb).toString();
 				}
 				finally
 				{
 					stream.close();
 				}
 				new User(p, pdata.split(String.valueOf(separator)));
 			}
 			else
 			{
 				new User(p, SburbChat.getInstance().getChannelManager().getChannel("#"));
 			}
 		}
 	}
 	
 	public static void removePlayer(Player p) throws IOException
 	{
 		if (users.get(p.getName()) != null)
 		{
 			BufferedWriter b = new BufferedWriter(new FileWriter("plugins/SburbChat/u_" + p.getName() + ".scd"));
 			b.write(users.get(p.getName()).logout());
 			b.flush();
 			b.close();
 		}
 	}
 	
 	public static User getUser(String name)
 	{
 		return users.get(name);
 	}
 	
 	public User(Player p, Channel c)
 	{
 		this.pthis = p;
 		this.setCurrent(c);
 		users.put(p.getName(), this);
 	}
 	
 	public User(Player p, String[] ch)
 	{
 		this.pthis = p;
 		this.isMute = ch[0].equals("t");
 		if (!this.setCurrent(SburbChat.getInstance().getChannelManager().getChannel(ch[1])))
 		{
 			this.sendMessage(ChatColor.RED + "Could not rejoin channel " + ChatColor.GOLD + ch[1] + ChatColor.RED + "!");
 			this.setCurrent(SburbChat.getInstance().getChannelManager().getChannel("#"));
 		}
 		for (int i = 2; i < ch.length; i++)
 		{
 			if (!this.current.equals(SburbChat.getInstance().getChannelManager().getChannel(ch[i])))
 			{
 				if (!this.addListening(SburbChat.getInstance().getChannelManager().getChannel(ch[i])))
 				{
 					this.sendMessage(ChatColor.RED + "Could not rejoin channel " + ChatColor.GOLD + ch[i] + ChatColor.RED + "!");
 				}
 			}
 		}
 		users.put(p.getName(), this);
 	}
 	
 	public String logout()
 	{
 		users.remove(this.getName());
 		StringBuilder serialized = new StringBuilder().append(this.isMute ? "t" : "f").append(separator).append(this.current.getName());
 		this.listening.remove(this.current);
 		this.current.userLeave(this);
 		for (Channel c : this.listening)
 		{
 			c.userLeave(this);
 			serialized.append(separator).append(c.getName());
 		}
 		return serialized.toString();
 	}
 	
 	// stuff that uses pthis
 	
 	public void sendMessage(String s)
 	{
 		this.pthis.sendMessage(SburbChat.getInstance().prefix() + s);
 	}
 	
 	public void sendMessageFromChannel(String s, Channel c)
 	{
 		if (!this.isMute || c.equals(this.current))
 		{
 			if (ChatColor.stripColor(s).toLowerCase().indexOf(this.getName().toLowerCase()) > 3)
 			{
 				this.pthis.sendMessage(c.getPrefix() + ChatColor.BLUE + "{!} " + ChatColor.WHITE + s);
 				// this.pthis.sendBlockChange(this.pthis.getLocation().add(0, 2, 0), Material.NOTE_BLOCK, (byte) 0);
 				// this.pthis.playNote(this.pthis.getLocation().add(0, 2, 0), (byte) 0, (byte) 6);
 				// this.pthis.sendBlockChange(this.pthis.getLocation().add(0, 2, 0), this.pthis.getLocation().add(0, 2, 0).getBlock().getType(), this.pthis.getLocation().add(0, 2, 0).getBlock().getData());
 				this.pthis.playEffect(this.pthis.getLocation(), Effect.GHAST_SHOOT, 0);
 			}
 			else
 			{
 				this.pthis.sendMessage(c.getPrefix() + s);
 			}
 		}
 	}
 	
 	public boolean hasPermission(String p)
 	{
 		return this.pthis.hasPermission(p);
 	}
 	
 	public void chat(AsyncPlayerChatEvent event)
 	{
 		String chatmsg = event.getMessage();
 		if(chatmsg.matches("\\A@.+ .+"))
 		{
 			String chname = chatmsg.substring(chatmsg.indexOf('@') + 1, chatmsg.indexOf(" "));
 			String message = chatmsg.substring(chatmsg.indexOf(" ") + 1);
 			Channel channel = SburbChat.getInstance().getChannelManager().getChannel(chname);
 			if(channel == null)
 			{
 				this.sendMessage(ChatColor.GOLD + chname + ChatColor.RED + " does not exist!");
 				return;
 			}
 			if(listening.contains(channel) || this.addListening(channel))
 			{
 				if (message.indexOf('/') == 0)
 				{
 					Channel c = this.current;
 					this.current = channel;
 					this.pthis.performCommand(message.substring(1));
 					this.current = c;
 				}
 				else
 				{
 					channel.setChat(message, this);
 				}
 			}
 		}
 		else
 		{
 			this.current.setChat(chatmsg, this);
 		}
 	}
 	
 	public void kickFrom(Channel c)
 	{
 		this.sendMessageFromChannel(ChatColor.RED + "You have been kicked from " + ChatColor.GOLD + c.getName() + ChatColor.RED + "!", c);
 		this.listening.remove(c);
 		if (this.current.equals(c))
 		{
 			this.setCurrent(SburbChat.getInstance().getChannelManager().getChannel("#"));
 		}
 	}
 	
 	public void banFrom(Channel c)
 	{
 		this.sendMessageFromChannel(ChatColor.RED + "You have been banned from " + ChatColor.GOLD + c.getName() + ChatColor.RED + "!", c);
 		this.listening.remove(c);
 		if (this.current.equals(c))
 		{
 			this.setCurrent(SburbChat.getInstance().getChannelManager().getChannel("#"));
 		}
 	}
 	
 	public String getName()
 	{
 		return this.pthis.getName();
 	}
 	public String getDisplayName()
 	{
		return this.hasPermission("sburbchat.bnick") ? this.pthis.getDisplayName() : this.getName();
 	}
 	
 	public Player getPlayer()
 	{
 		return this.pthis;
 	}
 	
 	// user commands
 	
 	public boolean setCurrent(Channel c)
 	{
 		if (c == null)
 		{
 			return false;
 		}
 		if (!c.equals(this.current))
 		{
 			if (this.listening.contains(c))
 			{
 				this.current = c;
 				this.sendMessage(ChatColor.GREEN + "Now chatting in channel " + ChatColor.GOLD + c.getName() + ChatColor.GREEN + ".");
 				return true;
 			}
 			else if (c.userJoin(this))
 			{
 				this.current = c;
 				this.listening.add(c);
 				this.sendMessage(ChatColor.GREEN + "Now chatting in channel " + ChatColor.GOLD + c.getName() + ChatColor.GREEN + ".");
 				return true;
 			}
 		}
 		else
 		{
 			this.sendMessage(ChatColor.RED + "Already chatting in channel " + ChatColor.GOLD + c.getName() + ChatColor.RED + "!");
 		}
 		return false;
 	}
 	
 	public boolean addListening(Channel c)
 	{
 		if (c == null)
 		{
 			return false;
 		}
 		if (!this.listening.contains(c))
 		{
 			if (c.userJoin(this))
 			{
 				this.listening.add(c);
 				this.sendMessage(ChatColor.GREEN + "Now listening to channel " + ChatColor.GOLD + c.getName() + ChatColor.GREEN + ".");
 				return true;
 			}
 		}
 		else
 		{
 			this.sendMessage(ChatColor.RED + "Already listening to channel " + ChatColor.GOLD + c.getName() + ChatColor.RED + "!");
 		}
 		return false;
 	}
 	
 	public void removeListening(Channel c)
 	{
 		if (this.listening.contains(c))
 		{
 			if (!this.current.equals(c))
 			{
 				c.userLeave(this);
 				this.listening.remove(c);
 				this.sendMessage(ChatColor.GREEN + "No longer listening to channel " + ChatColor.GOLD + c.getName() + ChatColor.GREEN + ".");
 			}
 			else
 			{
 				this.sendMessage(ChatColor.RED + "Cannot leave your current channel " + ChatColor.GOLD + c.getName() + ChatColor.RED + "!");
 			}
 		}
 		else
 		{
 			this.sendMessage(ChatColor.RED + "Not listening to channel " + ChatColor.GOLD + c.getName() + ChatColor.RED + "!");
 		}
 	}
 	
 	public void toggleMute()
 	{
 		if (this.isMute = !this.isMute)
 		{
 			this.sendMessage(ChatColor.GREEN + "All channels have been muted.");
 		}
 		else
 		{
 			this.sendMessage(ChatColor.GREEN + "All channels have been unmuted.");
 		}
 	}
 	
 	/**
 	 * Sends a message to a channel without changing the client's active channel
 	 * @param channel Channel to send to
 	 * @param message Message to be sent
 	 * @author FireNG
 	 */
 	public void sendOnce(Channel channel, String message)
 	{
 		if(listening.contains(channel) || this.addListening(channel))
 		{
 			channel.setChat(message, this);
 		}
 	}
 	
 	public void listUsers()
 	{
 		this.sendMessage(ChatColor.YELLOW + "Users in channel " + ChatColor.GOLD + current.getName() + ChatColor.YELLOW + ":");
 		StringBuilder ul = new StringBuilder();
 		for (User u : this.current.getUsers())
 		{
 			ul.append(ChatColor.WHITE).append(", ").append(this.current.getChatPrefix(u, "").replace("> ", "").replace("<", "")); 
 		}
 		this.pthis.sendMessage(ul.toString().substring(4));
 	}
 	
 	public void listChannels()
 	{
 		this.sendMessage(ChatColor.GOLD + "Channels on this server:");
 		StringBuilder ul = new StringBuilder();
 		if (this.pthis.hasPermission("sburbchat.fulllist"))
 		{
 			for (Channel c : SburbChat.getInstance().getChannelManager().getChannels())
 			{
 				ul.append(ChatColor.WHITE).append(", ").append(ChatColor.GOLD).append(c.getName()); 
 			}
 			this.pthis.sendMessage(ul.toString().substring(4));
 		}
 		else
 		{
 			for (Channel c : SburbChat.getInstance().getChannelManager().getChannels())
 			{
 				if (c.getLAcess() == AccessLevel.PUBLIC) ul.append(ChatColor.WHITE).append(", ").append(ChatColor.GOLD).append(c.getName()); 
 			}
 			this.pthis.sendMessage(ul.toString().substring(4));
 		}
 	}
 	
 	// channel owner commands
 	
 	public void newChannel(String name, ChannelType ct/* hur hur Equius*/, AccessLevel laccess, AccessLevel saccess)
 	{
 		Channel c = SburbChat.getInstance().getChannelManager().newChannel(name, ct, laccess, saccess, this);
 		if (c != null)
 		{
 			this.addListening(c);
 		}
 	}
 	
 	public void addMod(User user)
 	{
 		this.current.addMod(user, this);
 	}
 	
 	public void removeMod(User user)
 	{
 		this.current.removeMod(user, this);
 	}
 	
 	public void disband()
 	{
 		this.current.disband(this);
 	}
 	
 	// mod commands
 	
 	/**
 	 * Sets who is allowed to use chat colors in the current channel
 	 * @param level Access level to use.
 	 */
 	public void setColorAccess(PrivilegeLevel level)
 	{
 		this.current.setColorAccess(level, this);
 	}
 	
 	public void kick(User user)
 	{
 		this.current.kickUser(user, this);
 	}
 	
 	public void ban(User user)
 	{
 		this.current.banUser(user, this);
 	}
 	
 	public void unban(User user)
 	{
 		this.current.unbanUser(user, this);
 	}
 	
 	public void mute(User user)
 	{
 		this.current.muteUser(user, this);
 	}
 	
 	public void unmute(User user)
 	{
 		this.current.unmuteUser(user, this);
 	}
 	
 	public void approve(User user)
 	{
 		this.current.approveUser(user, this);
 	}
 	
 	public void deapprove(User user)
 	{
 		this.current.deapproveUser(user, this);
 	}
 	
 	// nick commands
 	
 	public void setNick(String name)
 	{
 		this.current.setNick(name, this);
 	}
 	
 	public void removeNick()
 	{
 		this.current.removeNick(this);
 	}
 	
 	// alias commands
 	
 	public void addAlias(String name)
 	{
 		this.current.addAlias(name, this);
 	}
 	
 	public void removeAlias(String name)
 	{
 		this.current.removeAlias(name, this);
 	}
 	
 	// info commands
 	
 	public void getListeningChannels()
 	{
 //		if (this.current instanceof NickChannel || this.current instanceof RPChannel)
 		this.sendMessage(ChatColor.GOLD + "Channels you're listening to:");
 		StringBuilder ul = new StringBuilder();
 		for (Channel c : listening)
 		{
 			ul.append(ChatColor.WHITE).append(", ").append(c.equals(this.current) ? ChatColor.GREEN : ChatColor.GOLD).append(c.getName()); 
 		}
 		this.pthis.sendMessage(ul.toString().substring(4));
 	}
 	
 	private Player pthis;
 	private boolean isMute = false;
 	private Set<Channel> listening = new HashSet<Channel>();
 	private Channel current;
 	
 	private static Map<String, User> users = new HashMap<String, User>();
 	private static final char separator = '/';
 }
