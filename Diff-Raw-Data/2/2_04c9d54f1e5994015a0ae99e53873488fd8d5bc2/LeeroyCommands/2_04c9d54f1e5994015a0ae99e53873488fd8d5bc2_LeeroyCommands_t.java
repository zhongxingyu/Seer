 package net.loadingchunks.plugins.Leeroy;
 
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import net.loadingchunks.plugins.Leeroy.Types.BasicNPC;
 import net.loadingchunks.plugins.Leeroy.Types.ButlerNPC;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.metadata.MetadataValue;
 import org.fusesource.jansi.Ansi.Color;
 import org.bukkit.craftbukkit.CraftWorld;
 
 public class LeeroyCommands implements CommandExecutor
 {
 	public Leeroy plugin;
 
 	public LeeroyCommands(Leeroy plugin)
 	{
 		this.plugin = plugin;
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
 	{	
 		if(cmd.getName().equalsIgnoreCase("leeroy"))
 		{
 			this.plugin.log.info("[LEEROY] Got Leeroy Command Hook!");
 
 			if(args[0].equalsIgnoreCase("spawn"))
 			{
 				String name;
 				
 				if(args.length == 3)
 					name = args[2];
 				else
 				{
 					return false;
 				}
 
 				this.plugin.log.info("[LEEROY] Got Spawn!");
 
 				if(!(sender instanceof Player))
 				{
 					sender.sendMessage("[LEEROY] This command can only be used in-game.");
 					return false;
 				}
 
 				if(!LeeroyPermissions.canSpawn((Player)sender))
 				{
 					sender.sendMessage("[LEEROY] This command is op-only!");
 					return false;
 				}
 
 				this.plugin.log.info("[LEEROY] Init Handler.");
 				LeeroyNPCHandler npc = new LeeroyNPCHandler(this.plugin);
 				
 				Set<Entry<String, Object>> entries = this.plugin.NPCList.entrySet();
 				
 				for(Entry<String, Object> n : entries)
 				{
 					BasicNPC np = (BasicNPC)n.getValue();
 
 					if(np.name.equalsIgnoreCase(name) && np.npc.getBukkitEntity().getWorld().getName().equalsIgnoreCase(((Player)sender).getWorld().getName()) )
 					{
 						sender.sendMessage("[LEEROY] An NPC already exists within this world with that name.");
 						return true;
 					}
 				}
 				
 				this.plugin.log.info("[LEEROY] Spawning...");
 				npc.spawn(args[1], name, ((Player)sender).getLocation(), "", "", "", "", true, ((Player)sender).getWorld().getName(), null);
 
 				return true;
 			} else if(args[0].equalsIgnoreCase("angle") && sender instanceof Player)
 			{
 				sender.sendMessage("You Yaw is " + ((Player)sender).getLocation().getYaw() + " and your pitch is " + ((Player)sender).getLocation().getPitch() + ".");
 				return true;
 			}
 			else if(args[0].equalsIgnoreCase("kill"))
 			{
 				String name;
 				Integer radius = 5;
 				
 				if(!(sender instanceof Player))
 					return false;
 				
 				if(!LeeroyPermissions.canKill((Player)sender))
 				{
 					sender.sendMessage("[LEEROY] This command is op-only!");
 					return false;
 				}
 
 				if(args.length < 2)
 					return false;
 				
 				name = args[1];
 
 				if(args.length == 3)
 				{
 					radius = Integer.parseInt(args[2]);
 				}
 				
 				Player p = (Player)sender;
 				List<Entity> entities = p.getNearbyEntities(radius, radius, radius);
 				
 				for(Entity e : entities)
 				{
 					if(e instanceof HumanEntity)
 					{
 						List<MetadataValue> md = e.getMetadata("leeroy_id");
 						List<MetadataValue> mdtype = e.getMetadata("leeroy_type");
 
 						if(md.size() > 0)
 						{
 							if(((BasicNPC)this.plugin.NPCList.get(md.get(0).asString())).name.equalsIgnoreCase(name) || name.equalsIgnoreCase("*"))
 							{
 								this.plugin.log.info("[LEEROY] Killing NPC with ID: " + md.get(0).asString());
 								this.plugin.npcs.remove(md.get(0).asString());
 							}
 						}
 					}
 				}
 				return true;
 			} else if(args[0].equalsIgnoreCase("look"))
 			{
 				String name;
 				Integer radius = 5;
 				
 				if(!(sender instanceof Player))
 					return false;
 				
 				if(!LeeroyPermissions.canLook((Player)sender))
 				{
 					sender.sendMessage("[LEEROY] This command is op-only!");
 					return false;
 				}
 
 				if(args.length < 2)
 				{
 					sender.sendMessage("[LEEROY] Not enough args!");
 					return false;
 				}
 				
 				name = args[1];
 
 				if(args.length == 3)
 				{
 					radius = Integer.parseInt(args[2]);
 				}
 				
 				Player p = (Player)sender;
 				List<Entity> entities = p.getNearbyEntities(radius, radius, radius);
 				
 				for(Entity e : entities)
 				{
 					if(e instanceof HumanEntity)
 					{
 						List<MetadataValue> md = e.getMetadata("leeroy_id");
 						List<MetadataValue> mdtype = e.getMetadata("leeroy_type");
 
 						if(md.size() > 0)
 						{
 							if(((BasicNPC)this.plugin.NPCList.get(md.get(0).asString())).name.equalsIgnoreCase(name) || name.equalsIgnoreCase("*"))
 							{
 								((BasicNPC)this.plugin.NPCList.get(md.get(0).asString())).npc.lookAtPoint(((Player)sender).getEyeLocation());
 							}
 						}
 					}
 				}
 				return true;
 			} else if(args[0].equalsIgnoreCase("reload"))
 			{
 				if(sender.isOp())
 				{
 					this.plugin.reloadConfig();
 					sender.sendMessage("Config reloaded!");
 					return true;
 				}
 			} else if(args[0].equalsIgnoreCase("list"))
 			{
 				if(sender.isOp())
 				{
 					for(String s : this.plugin.getConfig().getStringList("general.templates"))
 					{
 						sender.sendMessage("- " + s);
 					}
 				}
 			} else if(args[0].equalsIgnoreCase("purge"))
 			{
 				if(!sender.isOp())
 						return false;
 
 				plugin.log.info("[LEEROY] Running checks on homeworlds.");
 				List<World> worlds = plugin.getServer().getWorlds();
 				for(World w : worlds)
 				{
 					if(w == null)
 						continue;
 
 					if(w.getName().startsWith("homeworld_") && (w.getPlayers().isEmpty() || (w.getPlayers().size() == 1 && (w.getPlayers().get(0).getAddress() == null))))
 					{
 						plugin.log.info("[LEEROY] Checking " + w.getName());
 						if(LeeroyUtils.hasNPC(plugin, w.getName()) && plugin.NPCList.containsKey(w.getName() + "_butler"))
 						{
 							plugin.log.info("[LEEROY] Redundant NPC Found in " + w.getName());
 							((ButlerNPC)plugin.NPCList.get(w.getName() + "_butler")).npc.moveTo(plugin.mvcore.getMVWorldManager().getFirstSpawnWorld().getSpawnLocation());
 							((ButlerNPC)plugin.NPCList.get(w.getName() + "_butler")).npc.getEntity().getBukkitEntity().remove();
 							((CraftWorld) w).getHandle().getPlayerManager().a().removeEntity(((ButlerNPC)plugin.NPCList.get(w.getName() + "_butler")).npc.getEntity());
 							plugin.NPCList.remove(w.getName() + "_butler");
 							plugin.log.info("[LEEROY] Redundant NPC " + w.getName() + "_butler has been removed.");
 						}
 						if(!plugin.mvcore.getMVWorldManager().unloadWorld(w.getName()))
 						{
 							sender.sendMessage("Error purging world " + w.getName());
 							sender.sendMessage("Players/NPCs in World: " + w.getPlayers().size());
 							sender.sendMessage("Players: " + w.getPlayers().toString());
 							sender.sendMessage("HandlePlayers: " + ((CraftWorld) w).getHandle().players.size());
 						}
 					} else if (w.getPlayers().size() > 0 && w.getName().startsWith("homeworld_") && !LeeroyUtils.hasNPC(plugin, w.getName()))
 					{
 						plugin.log.info("[LEEROY] No NPC found in loaded world " + w.getName());
 						Location nl = new Location(w, plugin.getConfig().getDouble("home.butler.x"), plugin.getConfig().getDouble("home.butler.y"), plugin.getConfig().getDouble("home.butler.z"));
 						plugin.npcs.spawn("butler",plugin.getConfig().getString("home.butler.name"), nl, "", "", "", "", false, w.getName(), w.getName() + "_butler");
 					}
 				}
 				return true;
 			}
 		}
 		
 		if(cmd.getName().equalsIgnoreCase("invite"))
 		{
 			if(!(sender instanceof Player))
 			{
 				sender.sendMessage("You can only do this in-game!");
 				return true;
 			}
 			
 			Player p = (Player)sender;
 			
 			if(!(p.getWorld().getName().equalsIgnoreCase("homeworld_" + p.getName())))
 			{
 				p.sendMessage("This is not your home world");
 				return true;
 			}
 			
 			if(!p.hasPermission("leeroy.invite"))
 			{
 				p.sendMessage("You do not have permission to do that!");
 				return true;
 			}
 			
 			if(args.length < 1)
 			{
 				p.sendMessage("You need to specify a player to invite");
 				return true;
 			}
 			
 			if(plugin.getServer().getPlayer(args[0]) != null)
 			{
 				Player to = plugin.getServer().getPlayer(args[0]);
 				to.sendMessage(ChatColor.AQUA + p.getDisplayName() + " has invited you to their home world. Type /accept to go there!");
 				this.plugin.inviteList.put(to.getName(), "homeworld_" + p.getName());
 				p.sendMessage(ChatColor.AQUA + "Invite sent to " + to.getName());
 				return true;
 			}
 			
 			return false;
 		}
 		
 		if(cmd.getName().equalsIgnoreCase("hw"))
 		{
 			if(!(sender instanceof Player))
 			{
 				sender.sendMessage("You can only do this in-game!");
 				return true;
 			}
 			
 			Player p = (Player)sender;
 			
 			if(args.length < 1)
 			{
 				return false;
 			}
 			
 			LeeroyHomeCommand command = this.plugin.sql.GetCommand(args[0]);
 			
 			if(!this.plugin.sql.PlayerHasCommand(command.commandString, p.getName()))
 			{
 				sender.sendMessage("You do not have access to that command.");
 				return true;
 			}
 			
 			String executor = command.commandExec;
 			int argcount = 1;
 			
 			for(String arg : args)
 			{
 				executor = executor.replaceAll("{arg" + argcount + "}", arg);
 			}
 			
 			executor = executor.replaceAll("{player}", p.getName());
 			executor = executor.replaceAll("{playerdisplay}", p.getDisplayName());
 			
 			// Protective checks
 			if(command.commandCheck.length != (args.length - 1))
 			{
 				sender.sendMessage("Invalid arguments given for command /hw " + command.commandString);
 				sender.sendMessage("Usage: " + command.commandUsage);
 				return true;
 			}
 			
 			argcount = 0;
 			
 			for(String a : args)
 			{
 				if(argcount == 0)
 				{
 					argcount++;
 					continue;
 				}
 				
 				switch(command.commandCheck[argcount-1])
 				{
 					case "Player":
 						if(this.plugin.getServer().getPlayer(a) == null)
 						{
 							sender.sendMessage(ChatColor.RED + "Player " + a + " not found.");
 							sender.sendMessage("Usage: " + command.commandUsage);
 							return true;
 						}
 						break;
 						
 					case "Integer":
 						try {
 							Integer.parseInt(a);
 						} catch(NumberFormatException e)
 						{
 							sender.sendMessage(ChatColor.RED + "Invalid number, you supplied " + a);
 							sender.sendMessage("Usage: " + command.commandUsage);
 							return true;
 						}
 						break;
 						
 					case "String":
 						break;
 					
 					default:
 						sender.sendMessage(ChatColor.RED + "An unknown error occurred while checking your command.");
 						this.plugin.getLogger().warning("Bad Homeworld Argument Type Given: " + command.commandCheck[argcount]);
 						sender.sendMessage("Usage: " + command.commandUsage);
 						return true;
 				}
 			}
 			
 			String[] executors = executor.split("\n");
 			
 			for(String ex : executors)
 			{
 				this.plugin.getServer().dispatchCommand((CommandSender) (this.plugin.getServer().getConsoleSender()), ex);
 			}
 			
 			return false;
 		}
 		
 		if(cmd.getName().equalsIgnoreCase("upgrade"))
 		{
 			if(!(sender instanceof Player))
 			{
 				sender.sendMessage("You can only do this in-game!");
 				return true;
 			}
 			
 			Player p = (Player)sender;
 			
 			if(args.length != 1)
 			{
 				sender.sendMessage("You need to specify an upgrade to buy!");
 				return false;
 			}
 			
 			if(this.plugin.sql.PlayerHasCommand(args[0], p.getName()))
 			{
 				sender.sendMessage("You have already purchased this upgrade!");
 				return true;
 			}
 			
 			LeeroyHomeCommand command = this.plugin.sql.GetCommand(args[0]);
 			
 			if(command == null)
 			{
 				sender.sendMessage("Unknown command.");
 				return true;
 			}
 			
 			if(!this.plugin.eco.has(p.getName(), command.commandPrice))
 			{
				sender.sendMessage("You don't have enough money for this upgrade, it costs $" + ChatColor.GOLD + command.commandPrice + ChatColor.WHITE + " and you have $" + ChatColor.GOLD + this.plugin.eco.bankBalance(p.getName()).balance + ChatColor.WHITE + ".");
 				return true;
 			}
 			
 			this.plugin.sql.PurchaseCommand(command.commandString, p.getName());
 			this.plugin.eco.withdrawPlayer(p.getName(), command.commandPrice);
 			this.plugin.eco.depositPlayer("lcbank", command.commandPrice);
 			sender.sendMessage("Purchase successful!");
 			return true;
 		}
 		
 		if(cmd.getName().equalsIgnoreCase("accept"))
 		{
 			if(!(sender instanceof Player))
 			{
 				sender.sendMessage("You can only do this in-game!");
 				return true;
 			}
 			
 			Player p = (Player)sender;
 			
 			if(!p.hasPermission("leeroy.accept"))
 			{
 				p.sendMessage("You do not have permission to do that!");
 				return true;
 			}
 			
 			if(!plugin.inviteList.containsKey(p.getName()))
 			{
 				sender.sendMessage("Nobody has invited you to their home! :(");
 				return true;
 			}
 			
 			if(plugin.mvcore.getMVWorldManager().getUnloadedWorlds().contains(plugin.inviteList.get(p.getName())))
 			{
 				sender.sendMessage("Nobody is in that world any more.");
 				return true;
 			}
 
 			if(plugin.getServer().getPlayer(plugin.inviteList.get(p.getName()).replace("homeworld_", "")) == null)
 			{
 				sender.sendMessage("Player is not online.");
 				return true;
 			}
 			
 			p.teleport(plugin.mvcore.getMVWorldManager().getMVWorld(plugin.inviteList.get(p.getName())).getSpawnLocation());
 			plugin.getServer().getPlayer(plugin.inviteList.get(p.getName()).replace("homeworld_", "")).sendMessage(ChatColor.AQUA + p.getDisplayName() + " has accepted your invitation.");
 			
 			return true;
 			
 		}
 		return false;
 	}
 	
 }
