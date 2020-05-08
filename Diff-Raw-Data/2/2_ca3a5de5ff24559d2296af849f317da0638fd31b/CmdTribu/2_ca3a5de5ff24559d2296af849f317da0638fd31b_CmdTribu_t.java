 /*******************************************************************************
  * Copyright or  or Copr. Quentin Godron (2011)
  * 
  * cafe.en.grain@gmail.com
  * 
  * This software is a computer program whose purpose is to create zombie 
  * survival games on Bukkit's server. 
  * 
  * This software is governed by the CeCILL-C license under French law and
  * abiding by the rules of distribution of free software.  You can  use, 
  * modify and/ or redistribute the software under the terms of the CeCILL-C
  * license as circulated by CEA, CNRS and INRIA at the following URL
  * "http://www.cecill.info". 
  * 
  * As a counterpart to the access to the source code and  rights to copy,
  * modify and redistribute granted by the license, users are provided only
  * with a limited warranty  and the software's author,  the holder of the
  * economic rights,  and the successive licensors  have only  limited
  * liability. 
  * 
  * In this respect, the user's attention is drawn to the risks associated
  * with loading,  using,  modifying and/or developing or reproducing the
  * software by the user in light of its specific status of free software,
  * that may mean  that it is complicated to manipulate,  and  that  also
  * therefore means  that it is reserved for developers  and  experienced
  * professionals having in-depth computer knowledge. Users are therefore
  * encouraged to load and test the software's suitability as regards their
  * requirements in conditions enabling the security of their systems and/or 
  * data to be ensured and,  more generally, to use and operate it in the 
  * same conditions as regards security. 
  * 
  * The fact that you are presently reading this means that you have had
  * knowledge of the CeCILL-C license and that you accept its terms.
  ******************************************************************************/
 package graindcafe.tribu.Executors;
 
 import graindcafe.tribu.Package;
 import graindcafe.tribu.PlayerStats;
 import graindcafe.tribu.Tribu;
 import graindcafe.tribu.Signs.TribuSign;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class CmdTribu implements CommandExecutor {
 	// use to confirm deletion of a level
 	private String deletedLevel = "";
 	private Package pck = null;
 	private Tribu plugin;
 
 	public CmdTribu(Tribu instance) {
 		plugin = instance;
 	}
 
 	// usage: /tribu ((create | load | delete) <name>) | enter | leave | package
 	// (create |delete | list)
 	// list | start [<name>] | stop | save | stats
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if (args.length == 0) {
 			return usage(sender);
 		}
 		args[0] = args[0].toLowerCase();
 
 		/*
 		 * Players commands
 		 */
 
 		if (args[0].equalsIgnoreCase("enter") || args[0].equalsIgnoreCase("join")) {
 			if (!plugin.config().PluginModeServerExclusive || sender.isOp())
 			{
 				if (!sender.hasPermission("tribu.use.enter"))
 				{
 					Tribu.messagePlayer(sender,plugin.getLocale("Message.Deny"));
 				} else 
 				if (!(sender instanceof Player)) 
 				{
 					plugin.LogWarning(plugin.getLocale("Warning.ThisCommandCannotBeUsedFromTheConsole"));
 				} else 
 				{
 					if(!plugin.isPlaying((Player) sender))
 					{
 						plugin.addPlayer((Player) sender);
 					} else
 					{
 						Tribu.messagePlayer(sender,plugin.getLocale("Message.AlreadyIn"));
 					}
 				}
 			}
 			return true;
 		} else if (args[0].equals("leave")) 
 		{
 			if (!plugin.config().PluginModeServerExclusive || sender.isOp())
 			{
 				if (!sender.hasPermission("tribu.use.leave"))
 				{
 					Tribu.messagePlayer(sender,plugin.getLocale("Message.Deny"));
 				} else 
 				if (!(sender instanceof Player)) 
 				{
 					plugin.LogWarning(plugin.getLocale("Warning.ThisCommandCannotBeUsedFromTheConsole"));
 				} else 
 				{
 					plugin.removePlayer((Player) sender);
 				}
 			}
 			//add in them to change to main world (world when they leave the game);
 			return true;
 		} else if (args[0].equals("stats")) {
 			if (!sender.hasPermission("tribu.use.stats"))
 				Tribu.messagePlayer(sender,plugin.getLocale("Message.Deny"));
 			else {
 				LinkedList<PlayerStats> stats = plugin.getSortedStats();
 				Tribu.messagePlayer(sender, plugin.getLocale("Message.Stats"));
 				Iterator<PlayerStats> i = stats.iterator();
 				String s;
 				PlayerStats cur;
 				while (i.hasNext()) {
 					s = "";
 					for (byte j = 0; i.hasNext() && j < 3; j++) {
 						cur = i.next();
 						s += ", " + cur.getPlayer().getDisplayName() + " (" + String.valueOf(cur.getPoints()) + ")";
 					}
 
 					Tribu.messagePlayer(sender, s.substring(2));
 				}
 			}
 			return true;
 		} else if (args[0].equals("vote")) {
 			if (!sender.hasPermission("tribu.use.vote"))
 				Tribu.messagePlayer(sender,plugin.getLocale("Message.Deny"));
 			else {
 				if (!(sender instanceof Player)) {
 					plugin.LogWarning(plugin.getLocale("Warning.ThisCommandCannotBeUsedFromTheConsole"));
 					return true;
 				}
 
 				if (args.length == 2) {
 					try {
 						plugin.getLevelSelector().castVote((Player) sender, Integer.parseInt(args[1]));
 					} catch (NumberFormatException e) {
 						Tribu.messagePlayer(sender,plugin.getLocale("Message.InvalidVote"));
 					}
 					return true;
 				}
 			}
 		} else if (args[0].equals("vote1")) {
 			if(!sender.hasPermission("tribu.use.vote"))
 				Tribu.messagePlayer(sender,plugin.getLocale("Message.Deny"));
 			else
 			{
 			if (!(sender instanceof Player)) {
 				plugin.LogWarning(plugin.getLocale("Warning.ThisCommandCannotBeUsedFromTheConsole"));
 				return true;
 			}
 
 			plugin.getLevelSelector().castVote((Player) sender, 1);
 			}
 			return true;
 
 		} else if (args[0].equals("vote2")) {
 			if(!sender.hasPermission("tribu.use.vote"))
 				Tribu.messagePlayer(sender,plugin.getLocale("Message.Deny"));
 			else
 			{
 			if (!(sender instanceof Player)) {
 				plugin.LogWarning(plugin.getLocale("Warning.ThisCommandCannotBeUsedFromTheConsole"));
 				return true;
 			}
 
 			plugin.getLevelSelector().castVote((Player) sender, 2);
 			}
 			return true;
 
 		}
 		/*
 		 * Ops commands
 		 */
 		/* Package management */
 		else if (args[0].equals("package") || args[0].equals("pck")) {
 			if(!sender.hasPermission("tribu.level.package"))
 			{
 				sender.sendMessage(plugin.getLocale("Message.Deny"));
 				return true;
 			}
 			else
 			if (args.length == 1) {
 				return usage(sender);
 			}
 			if (plugin.getLevel() == null) {
 				Tribu.messagePlayer(sender, plugin.getLocale("Message.NoLevelLoaded"));
 				Tribu.messagePlayer(sender, plugin.getLocale("Message.NoLevelLoaded2"));
 				return true;
 			}
 			args[1] = args[1].toLowerCase();
 
 			if (args[1].equals("new") || args[1].equals("create")) {
 				if (args.length == 2) {
 					Tribu.messagePlayer(sender, plugin.getLocale("Message.PckNeedName"));
 				} else {
 					pck = new Package(args[2]);
 					Tribu.messagePlayer(sender, String.format(plugin.getLocale("Message.PckCreated"), args[2]));
 				}
 
 			} else if (args[1].equals("open")) {
 				if (args.length == 2) {
 					Tribu.messagePlayer(sender, plugin.getLocale("Message.PckNeedName"));
 				} else {
 					pck = plugin.getLevel().getPackage(args[2]);
 					if (pck != null)
 						Tribu.messagePlayer(sender, String.format(plugin.getLocale("Message.PckOpened"), args[2]));
 					else
 						Tribu.messagePlayer(sender, String.format(plugin.getLocale("Message.PckNotFound"), args[2]));
 				}
 
 			} else if (args[1].equals("close") || args[1].equals("save")) {
 				if (pck == null) {
 					Tribu.messagePlayer(sender, plugin.getLocale("Message.PckNeedOpen"));
 				} else {
 					plugin.getLevel().addPackage(pck);
 					plugin.getLevel().setChanged();
 					Tribu.messagePlayer(sender, String.format(plugin.getLocale("Message.PckSaved"), pck.getName()));
 					pck = null;
 				}
 
 			} else if (args[1].equals("add")) {
 				boolean success = false;
 
 				if (pck == null)
 					Tribu.messagePlayer(sender, plugin.getLocale("Message.PckNeedOpen"));
 				else if (args.length == 2)
 					Tribu.messagePlayer(sender, plugin.getLocale("Message.PckNeedId"));
 				else {
 					if (args.length == 3)
 						if (args[2].equalsIgnoreCase("this"))
 							if (!(sender instanceof Player)) {
 								plugin.LogWarning(plugin.getLocale("Warning.ThisCommandCannotBeUsedFromTheConsole"));
 								return true;
 							} else
 								success = pck.addItem(((Player) sender).getItemInHand().clone());
 						else
 							success = pck.addItem(args[2]);
 					else if (args.length == 4)
 						if (args[2].equalsIgnoreCase("this"))
 							if (!(sender instanceof Player)) {
 								plugin.LogWarning(plugin.getLocale("Warning.ThisCommandCannotBeUsedFromTheConsole"));
 								return true;
 							} else
 								success = pck.addItem(((Player) sender).getItemInHand().clone(), (short) TribuSign.parseInt(args[3]));
 						else
 							success = pck.addItem(args[2], (short) TribuSign.parseInt(args[3]));
 					else
 						success = pck.addItem(args[2], (short) TribuSign.parseInt(args[3]), (short) TribuSign.parseInt(args[4]));
 					if (success)
 						Tribu.messagePlayer(sender, String.format(plugin.getLocale("Message.PckItemAdded"), pck.getLastItemName()));
 					else
 						Tribu.messagePlayer(sender, String.format(plugin.getLocale("Message.PckItemAddFailed"), args[2]));
 				}
 			} else if (args[1].equals("del") || args[1].equals("delete")) {
 				if (pck == null)
 					Tribu.messagePlayer(sender, plugin.getLocale("Message.PckNeedOpen"));
 				else if (args.length == 4) {
 					pck.deleteItem(TribuSign.parseInt(args[2]), (short) TribuSign.parseInt(args[3]));
 					Tribu.messagePlayer(sender, plugin.getLocale("Message.PckItemDeleted"));
				} else if (args.length==3 && pck.deleteItem(TribuSign.parseInt(args[2])))
 					Tribu.messagePlayer(sender, plugin.getLocale("Message.PckItemDeleted"));
 				else
 					Tribu.messagePlayer(sender, plugin.getLocale("Message.PckNeedSubId"));
 
 			} else if (args[1].equals("remove")) {
 				if (args.length == 3)
 					Tribu.messagePlayer(sender, plugin.getLocale("Message.PckNeedName"));
 				else {
 					plugin.getLevel().removePackage(args[2]);
 					Tribu.messagePlayer(sender, plugin.getLocale("Message.PckRemoved"));
 					pck=null;
 				}
 			} else if (args[1].equals("list")) {
 				Tribu.messagePlayer(sender, String.format(plugin.getLocale("Message.PckList"), plugin.getLevel().listPackages()));
 			} else if (args[1].equals("show") || args[1].equals("describe")) {
 				if (plugin.getLevel() == null) {
 					Tribu.messagePlayer(sender, plugin.getLocale("Message.NoLevelLoaded"));
 					Tribu.messagePlayer(sender, plugin.getLocale("Message.NoLevelLoaded2"));
 					return true;
 				}
 				Package p = pck;
 				if (args.length > 2)
 					p = plugin.getLevel().getPackage(args[2]);
 				if (p != null)
 					Tribu.messagePlayer(sender, p.toString());
 				else
 					Tribu.messagePlayer(
 							sender,
 							String.format(plugin.getLocale("Message.PckNotFound"),
 									args.length > 2 ? args[2] : plugin.getLocale("Message.PckNoneOpened")));
 			} else {
 				return usage(sender);
 			}
 			return true;
 		}
 		/*
 		 * Level management
 		 */
 		else if (args[0].equals("new") || args[0].equals("create")) {
 			if(!sender.hasPermission("tribu.level.create"))
 			{
 				Tribu.messagePlayer(sender,plugin.getLocale("Message.Deny"));
 				return true;
 			}
 			if (args.length == 1) {
 				return usage(sender);
 			}
 
 			if (!(sender instanceof Player)) {
 				plugin.LogWarning(plugin.getLocale("Warning.ThisCommandCannotBeUsedFromTheConsole"));
 				return true;
 			}
 			Player player = (Player) sender;
 			if (!plugin.getLevelLoader().saveLevel(plugin.getLevel())) {
 				Tribu.messagePlayer(sender,plugin.getLocale("Message.UnableToSaveCurrentLevely"));
 				return true;
 			}
 
 			plugin.setLevel(plugin.getLevelLoader().newLevel(args[1], player.getLocation()));
 			player.sendMessage(String.format(plugin.getLocale("Message.LevelCreated"), args[1]));
 
 			return true;
 		} else if (args[0].equals("delete") || args[0].equals("remove")) {
 			if(!sender.hasPermission("tribu.level.delete"))
 			{
 				Tribu.messagePlayer(sender,plugin.getLocale("Message.Deny"));
 				return true;
 			}
 			if (args.length == 1) {
 				return usage(sender);
 			} else if (!plugin.getLevelLoader().exists(args[1])) {
 				Tribu.messagePlayer(sender,String.format(plugin.getLocale("Message.UnknownLevel"), args[1]));
 				Tribu.messagePlayer(sender,plugin.getLocale("Message.MaybeNotSaved"));
 				return true;
 			} else if (!deletedLevel.equals(args[1])) {
 				deletedLevel = args[1];
 				Tribu.messagePlayer(sender, String.format(plugin.getLocale("Message.ConfirmDeletion"), args[1]));
 				Tribu.messagePlayer(sender, plugin.getLocale("Message.ThisOperationIsNotCancellable"));
 				return true;
 			} else {
 				if (!plugin.getLevelLoader().deleteLevel(args[1])) {
 					Tribu.messagePlayer(sender, plugin.getLocale("Message.UnableToDeleteLevel"));
 				} else {
 					Tribu.messagePlayer(sender, plugin.getLocale("Message.LevelDeleted"));
 				}
 				return true;
 			}
 		} else if (args[0].equals("save") || args[0].equals("close")) {
 			if(!sender.hasPermission("tribu.level.save"))
 			{
 				sender.sendMessage(plugin.getLocale("Message.Deny"));
 				return true;
 			}
 			if (plugin.getLevel() != null)
 				plugin.getLevel().addPackage(pck);
 			if (!plugin.getLevelLoader().saveLevel(plugin.getLevel())) {
 				Tribu.messagePlayer(sender, plugin.getLocale("Message.UnableToSaveCurrentLevel"));
 			} else {
 				Tribu.messagePlayer(sender, plugin.getLocale("Message.LevelSaveSuccessful"));
 			}
 			return true;
 
 		} else if (args[0].equals("load") || args[0].equals("open")) {
 			if(!sender.hasPermission("tribu.level.load"))
 			{
 				Tribu.messagePlayer(sender,plugin.getLocale("Message.Deny"));
 				return true;
 			}
 			if (args.length == 1) {
 				return usage(sender);
 			} else {
 				plugin.getLevelSelector().ChangeLevel(args[1], sender instanceof Player ? (Player) sender : null);
 				return true;
 			}
 		} else if (args[0].equals("unload")) {
 			if(!sender.hasPermission("tribu.level.unload"))
 			{
 				Tribu.messagePlayer(sender,plugin.getLocale("Message.Deny"));
 				return true;
 			}
 			plugin.setLevel(null);
 			Tribu.messagePlayer(sender, plugin.getLocale("Message.LevelUnloaded"));
 			return true;
 
 		} else if (args[0].equals("list")) {
 			Set<String> levels = plugin.getLevelLoader().getLevelList();
 			String msg = "";
 			for (String level : levels) {
 				msg += ", " + level;
 			}
 			if (msg != "")
 				Tribu.messagePlayer(sender, String.format(plugin.getLocale("Message.Levels"), msg.substring(2)));
 			return true;
 		}
 		/*
 		 * Game management
 		 */
 		else if (args[0].equals("start")) {
 			if(!sender.hasPermission("tribu.game.start"))
 			{
 				Tribu.messagePlayer(sender,plugin.getLocale("Message.Deny"));
 				return true;
 			}
 			// if a level is given, load it before start
 			if (args.length > 1 && plugin.getLevelLoader().exists(args[1])) {
 				plugin.getLevelSelector().ChangeLevel(args[1], sender instanceof Player ? (Player) sender : null);
 			} else if (plugin.getLevel() == null) {
 				Tribu.messagePlayer(sender, plugin.getLocale("Message.NoLevelLoaded"));
 				Tribu.messagePlayer(sender, plugin.getLocale("Message.NoLevelLoaded2"));
 				return true;
 			}
 			plugin.getLevelSelector().cancelVote();
 			if (plugin.startRunning())
 				Tribu.messagePlayer(sender, plugin.getLocale("Message.ZombieModeEnabled"));
 			else
 				Tribu.messagePlayer(sender, plugin.getLocale("Message.LevelNotReady"));
 			return true;
 		} else if (args[0].equals("stop")) {
 			if(!sender.hasPermission("tribu.game.stop"))
 			{
 				Tribu.messagePlayer(sender,plugin.getLocale("Message.Deny"));
 				return true;
 			}
 			plugin.stopRunning();
 			Tribu.messagePlayer(sender, plugin.getLocale("Message.ZombieModeDisabled"));
 			return true;
 		} else if (args[0].equals("tpfz")) {
 			Location loc = plugin.getSpawner().getFirstZombieLocation();
 			if (loc != null)
 				if (sender instanceof Player)
 					((Player) sender).teleport(loc);
 				else if (args.length > 1)
 					plugin.getServer().getPlayer(args[1]).teleport(loc);
 			return true;
 
 		}else if (args[0].equals("reload")) {
 			if(sender.hasPermission("tribu.plugin.reload"))
 			{
 				plugin.reloadConf();
 				Tribu.messagePlayer(sender,plugin.getLocale("Message.ConfigFileReloaded"));
 			}
 			return true;
 
 		} else if (args[0].equals("help") || args[0].equals("?") || args[0].equals("aide")) {
 
 			if (sender.isOp()) {
 				Tribu.messagePlayer(sender,"There are 4 commands : /zspawn (setting zombie spawns) /ispawn (setting initial spawn) /dspawn (setting death spawn) /tribu.");
 				Tribu.messagePlayer(sender,"This is the /tribu command detail :");
 			}
 			return usage(sender);
 
 		}
 		return usage(sender);
 
 	}
 
 	private boolean usage(CommandSender sender) {
 		if (sender.isOp()) {
 			Tribu.messagePlayer(sender,ChatColor.LIGHT_PURPLE + "Ops commands :");
 			Tribu.messagePlayer(sender,ChatColor.YELLOW + "/tribu ((create | load | delete) <name>) | save | list | start [<name>] | stop");
 			Tribu.messagePlayer(sender,ChatColor.YELLOW + "/tribu package ((add | del)  <id>  [<subid>] [<number>]) | ((new | open | remove) <name> | close) | list");
 			Tribu.messagePlayer(sender,ChatColor.YELLOW + "See also /ispawn /dspawn /zspawn");
 			Tribu.messagePlayer(sender,ChatColor.YELLOW + "Players commands :");
 			
 		}
 		return false;
 
 	}
 }
