 package net.kiwz.ThePlugin;
 
 import net.kiwz.ThePlugin.commands.EntityCommand;
 import net.kiwz.ThePlugin.commands.GameModeCommand;
 import net.kiwz.ThePlugin.commands.FeedCommand;
 import net.kiwz.ThePlugin.commands.FlyCommand;
 import net.kiwz.ThePlugin.commands.HealCommand;
 import net.kiwz.ThePlugin.commands.HelpCommand;
 import net.kiwz.ThePlugin.commands.HomeCommand;
 import net.kiwz.ThePlugin.commands.IgnoreCommand;
 import net.kiwz.ThePlugin.commands.MemCommand;
 import net.kiwz.ThePlugin.commands.MuteCommand;
 import net.kiwz.ThePlugin.commands.OpenInvCommand;
 import net.kiwz.ThePlugin.commands.PlaceCommand;
 import net.kiwz.ThePlugin.commands.SpawnCommand;
 import net.kiwz.ThePlugin.commands.TPCommand;
 import net.kiwz.ThePlugin.commands.TPSCommand;
 import net.kiwz.ThePlugin.commands.TellCommand;
 import net.kiwz.ThePlugin.commands.TimeCommand;
 import net.kiwz.ThePlugin.commands.ListCommand;
 import net.kiwz.ThePlugin.commands.WhoisCommand;
 import net.kiwz.ThePlugin.commands.WorldCommand;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 
 public class Commands implements CommandExecutor {
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		String cmdName = cmd.getName();
 		
 		if (cmdName.equalsIgnoreCase("entity")) {
 			EntityCommand entity = new EntityCommand();
 			entity.entity(sender, args);
 			return true;
 		}
 		
 		else if (cmdName.equalsIgnoreCase("feed")) {
 			FeedCommand feed = new FeedCommand();
 			feed.feed(sender, args);
 			return true;
 		}
 		
 		else if (cmdName.equalsIgnoreCase("fly")) {
 			FlyCommand fly = new FlyCommand();
 			fly.fly(sender, cmd, args);
 			return true;
 		}
 		
 		else if (cmdName.equalsIgnoreCase("gm")) {
 			GameModeCommand gm = new GameModeCommand();
 			gm.gameMode(sender, cmd, args);
 			return true;
 		}
 		
 		else if (cmdName.equalsIgnoreCase("heal")) {
 			HealCommand heal = new HealCommand();
 			heal.heal(sender, cmd, args);
 			return true;
 		}
 		
 		else if (cmdName.equalsIgnoreCase("help")) {
 			HelpCommand help = new HelpCommand();
 			help.help(sender, args);
 			return true;
 		}
 		
 		else if (cmdName.equalsIgnoreCase("home")) {
 			HomeCommand home = new HomeCommand();
 			home.home(sender, cmd, args);
 			return true;
 		}
 		
 		else if (cmdName.equalsIgnoreCase("ignore")) {
 			IgnoreCommand ignore = new IgnoreCommand();
 			ignore.ignore(sender, args);
 			return true;
 		}
 		
 		else if (cmdName.equalsIgnoreCase("sethome")) {
 			HomeCommand home = new HomeCommand();
 			home.setHome(sender, cmd, args);
 			return true;
 		}
 		
 		else if (cmdName.equalsIgnoreCase("list")) {
 			ListCommand list = new ListCommand();
 			list.list(sender, args);
 			return true;
 	    }
 		
 		else if (cmdName.equalsIgnoreCase("minne")) {
 			MemCommand mem = new MemCommand();
 			mem.mem(sender, args);
 			return true;
 	    }
 		
 		else if (cmdName.equalsIgnoreCase("mute")) {
 			MuteCommand mute = new MuteCommand();
 			mute.mute(sender, args);
 			return true;
 	    }
 		
 		else if (cmdName.equalsIgnoreCase("unmute")) {
 			MuteCommand mute = new MuteCommand();
 			mute.unMute(sender, args);
 			return true;
 	    }
 		
 		else if (cmdName.equalsIgnoreCase("oi")) {
 			OpenInvCommand openinv = new OpenInvCommand();
 			openinv.openInv(sender, cmd, args);
 			return true;
 	    }
 		
 		else if (cmdName.equalsIgnoreCase("plass")) {
 			PlaceCommand place = new PlaceCommand();
 			place.place(sender, cmd, args);
 			return true;
 		}
 		
 		else if (cmdName.equalsIgnoreCase("spawn")) {
 			SpawnCommand spawn = new SpawnCommand();
 			spawn.spawn(sender, cmd, args);
 			return true;
 		}
 		
 		else if (cmdName.equalsIgnoreCase("setspawn")) {
 			SpawnCommand spawn = new SpawnCommand();
 			spawn.setSpawn(sender, cmd, args);
 			return true;
 		}
 		
 		else if (cmdName.equalsIgnoreCase("tell")) {
 			TellCommand tell = new TellCommand();
 			tell.tell(sender, args);
 			return true;
 	    }
 		
 		else if (cmdName.equalsIgnoreCase("tid")) {
 			TimeCommand time = new TimeCommand();
 			time.time(sender, args);
 			return true;
 	    }
 		
 		else if (cmdName.equalsIgnoreCase("tp")) {
 			TPCommand tp = new TPCommand();
 			tp.tp(sender, cmd, args);
 			return true;
 	    }
 		
 		else if (cmdName.equalsIgnoreCase("ticks")) {
 			TPSCommand tps = new TPSCommand();
 			tps.tps(sender, args);
 			return true;
 	    }
 		
 		else if (cmdName.equalsIgnoreCase("whois")) {
 			WhoisCommand whois = new WhoisCommand();
 			whois.whois(sender, args);
 			return true;
 	    }
 		
 		else if (cmdName.equalsIgnoreCase("world")) {
 			WorldCommand world = new WorldCommand();
 			world.world(sender, cmd, args);
 			return true;
 	    }
 	    
 		else if (cmdName.equalsIgnoreCase("test")) {
 			
 			// Test her:
			
 			/*
 			StringBuilder header = new StringBuilder();
 			int i = 0;
 			while (i < 20) {
 				String playerName = "Kiwzjadhwuahds" + i;
 				header.append(ChatColor.RED + playerName + ChatColor.WHITE);
 				header.append(", ");
 				i++;
 			}
 			String[] msg = ChatPaginator.wordWrap(header.substring(0, header.length() - 2) + " ", 55);
 			sender.sendMessage(msg);
 			*/
 			// Test slutter!
 
 			return true;
 		}
 		else {
 			sender.sendMessage(ThePlugin.c2 + "Noe fryktelig galt skjedde, kontakt en Admin");
 			return false;
 		}
 	}
 }
