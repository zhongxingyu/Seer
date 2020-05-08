 package me.rotzloch.Commands;
 
 import me.rotzloch.Classes.Helper;
 import me.rotzloch.Classes.Land;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class LandCommandExecutor implements CommandExecutor {
 	
 	private static enum Action {
 		BUY,KAUFEN, SELL,VERKAUFEN, INFO, HELP, ADD, REMOVE, LOCK, UNLOCK, MOBS, LIST
 	}
 
 	public LandCommandExecutor() {
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		if (command.getName().equalsIgnoreCase("land")) {
 			if (sender instanceof Player) {
 				Action action;
 				Player player = (Player) sender;
 				
 				try {
 					action = Action.valueOf(args[0].toUpperCase());
 				} catch (Exception notEnum) {
 					Helper.Help(player, "land");
 					return true;
 				}
 				
 				int chunkX = player.getLocation().getChunk().getX();
 				int chunkZ = player.getLocation().getChunk().getZ();
 				String memberName = "";
 				if(action == Action.HELP){
 				
 				} else {
 					if(args.length == 2) {
 						memberName = args[1];
 					}
 					else if(args.length == 3 && action != Action.ADD && action != Action.REMOVE && action != Action.MOBS){
 						chunkX = Integer.parseInt(args[1]);
 						chunkZ = Integer.parseInt(args[2]);
 					} else if(args.length == 4 && (action == Action.ADD || action == Action.REMOVE || action == Action.MOBS)) {
 						memberName = args[1];
 						chunkX = Integer.parseInt(args[2]);
 						chunkZ = Integer.parseInt(args[3]);
 					}
 				}
 				
 				Land land = new Land(player,chunkX, chunkZ);
 				land.sender = sender;
 
 				switch (action) {
 					case KAUFEN:
 					case BUY:
 						land.Buy();
 						break;
 					case VERKAUFEN:
 					case SELL:
 						land.Sell();
 						break;
 					case INFO:
 						land.Info();
 						break;
 					case LOCK:
 						land.Lock();
 						break;
 					case UNLOCK:
 						land.Unlock();
 						break;
 					case ADD:
 						land.AddMember(memberName);
 						break;
 					case REMOVE:
 						land.RemoveMember(memberName);
 						break;
 					case MOBS: 
 						if(memberName.equalsIgnoreCase("true") || memberName.equalsIgnoreCase("false")) {
 							boolean mobs = Boolean.parseBoolean(memberName);
 							land.SpawnMobs(mobs, true, false);
 						} else {
 							Helper.Help(player, "land");
 						}
 						break;
 					case LIST:
 						land.List(memberName);
 						break;
 					case HELP:
 						Helper.Help(player, "land");
 						break;
 				}
 			} else {
 				Helper.LogInfo("Must call by Player");
 			}
 			return true;
 		} else {
 			return false;
 		}
 	}
 }
