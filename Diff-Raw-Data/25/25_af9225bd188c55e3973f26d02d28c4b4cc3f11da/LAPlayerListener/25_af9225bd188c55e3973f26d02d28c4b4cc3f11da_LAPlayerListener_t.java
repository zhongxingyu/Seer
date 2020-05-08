 package strimy.bukkit.plugins.localauth;
 
 import java.util.Date;
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 public class LAPlayerListener extends PlayerListener 
 {
 	HashMap<String, Date> lastNotif = new HashMap<String, Date>();
 	HashMap<Player, Location> startPosition = new HashMap<Player, Location>();
 	LocalAuth plugin;
 	public LAPlayerListener(LocalAuth plugin)
 	{
 		this.plugin = plugin;
 	}
 	
 	@Override
 	public void onPlayerJoin(PlayerJoinEvent event) 
 	{
 		
 		Player player = event.getPlayer();
 		String playerName = player.getDisplayName();
 		plugin.unloggedPlayers.add(player);
 		startPosition.put(player, player.getLocation());
 		player.sendMessage(ChatColor.LIGHT_PURPLE + "This server is protected by LocalAuth " + plugin.getDescription().getVersion());
 		if(plugin.playerManager.listPlayers.containsKey(playerName))
 		{
 			player.sendMessage(ChatColor.YELLOW + "You are not logged in. Type /auth password your_password to login.");
 		}
 		else
 		{
 			player.sendMessage(ChatColor.RED + "Your username doesn't exist in the player database. If you already have an account on this server, you can use the command /changename your_old_username your_password");
 			player.sendMessage(ChatColor.RED +"If you don't have an account, ask an OP or post on the forums");
 		}
 		super.onPlayerJoin(event);
 
 	}
 
 	@Override
 	public void onPlayerMove(PlayerMoveEvent event) 
 	{
 		Player player = event.getPlayer();
 		if(plugin.unloggedPlayers.contains(player))
 		{
 			if(!startPosition.containsKey(player))
 			{
 				startPosition.put(player, player.getLocation());
 			}
 			if(lastNotif.containsKey(player.getDisplayName()))
 			{
 				Date date = lastNotif.get(player.getDisplayName());
 				if((new Date().getTime() - date.getTime()) > 4000)
 				{
					event.setFrom(startPosition.get(player));
					event.setTo(startPosition.get(player));
					player.sendMessage(ChatColor.YELLOW + "You think you can escape ?" + player.isOnline());
 					lastNotif.remove(player.getDisplayName());
 					lastNotif.put(player.getDisplayName(), new Date());
 					player.teleport(startPosition.get(player));
 				}
 			}
 			else
 			{
 				lastNotif.put(player.getDisplayName(), new Date());
 				player.teleport(startPosition.get(player));
 			}
 		}
 		super.onPlayerMove(event);
 	}
 
 	@Override
 	public void onPlayerDropItem(PlayerDropItemEvent event) {
 		// TODO Auto-generated method stub
 		super.onPlayerDropItem(event);
 	}
 
 	@Override
 	public void onPlayerKick(PlayerKickEvent event) {
 		// TODO Auto-generated method stub
 		super.onPlayerKick(event);
 	}
 
 	@Override
 	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) 
 	{
		if(event.isCancelled())
			return;
		
 		String message = event.getMessage();
 		if(message.startsWith("/auth"))
 		{
 			Player player = event.getPlayer();
 			event.setCancelled(true);
 			String argsStr = message.replaceFirst("\\/auth ", "");
 			String[] args = argsStr.split(" ");
 			
 			if(args.length == 0)
 			{
 				player.sendMessage(ChatColor.RED + "Command arguments are : password, changepassword, logas");
 				return;
 			}
 			
 			if(args[0].equals("password"))
 			{
 				if(!plugin.unloggedPlayers.contains(player))
 				{
 					player.sendMessage(ChatColor.LIGHT_PURPLE + "Are you stupid ?");
 				}
 				else
 				{
 					if(args.length != 2)
 					{
 						player.sendMessage(ChatColor.LIGHT_PURPLE+ "Where is your password ?");
 						return;
 					}
 					String password = args[1];
 					if(plugin.playerManager.CheckPassword(player.getDisplayName(), password))
 					{
 						player.sendMessage(ChatColor.GREEN + "Login success !");
 						plugin.unloggedPlayers.remove(player);
 						if(startPosition.containsKey(player))
 							startPosition.remove(player);
 					}
 					else
 					{
 						player.sendMessage(ChatColor.RED + "Bad password");
 					}
 				}
 			}
 			else if(args[0].equals("changepassword"))
 			{
 				if(plugin.unloggedPlayers.contains(event.getPlayer()))
 				{
 					player.sendMessage(ChatColor.RED + "You can't change your password. Log in before !");
 					return;
 				}
 				if(args.length != 3)
 				{
 					player.sendMessage(ChatColor.LIGHT_PURPLE+ "Command usage : /auth changepassword old_password new_password");
 					return;
 				}
 				if(plugin.playerManager.CheckPassword(player.getDisplayName(), args[1]))
 				{
 					player.sendMessage(ChatColor.GREEN+ "Current password ok");
 					plugin.playerManager.changePassword(player, player.getDisplayName(), args[2]);
 				}
 				else
 				{
 					player.sendMessage(ChatColor.YELLOW+ "Incorrect current password");
 				}
 			}
 			else
 			{
 				player.sendMessage(ChatColor.RED + "Bad arguments !");
 			}
 		}
 		else
 		{
 			if(plugin.unloggedPlayers.contains(event.getPlayer()))
 			{
 				event.setCancelled(true);
 				event.getPlayer().sendMessage(ChatColor.RED + "You only have the permission to use /auth command.");
				return;
 			}
 		}
 		super.onPlayerCommandPreprocess(event);
 	}
 
 	@Override
 	public void onPlayerQuit(PlayerQuitEvent event) 
 	{
		Player p = event.getPlayer();
		if(plugin.unloggedPlayers.contains(p))
 		{
			p.teleport(startPosition.get(p));
			plugin.unloggedPlayers.remove(p);
 		}
 		super.onPlayerQuit(event);
 	}
 }
