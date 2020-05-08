 import java.util.Random;
 
 
 
 public class WMCommandListener extends PluginListener {
 	
 	public boolean onCommand(Player player, String[] split) {
 		if(split[0].equalsIgnoreCase("/wm")) {
 			
 			if(split.length == 1) {
 				this.sendHelpMsg(player);
 				return true;
 			}
 			WMCommandExecutor executor = new WMCommandExecutor();
 
 			switch(split[1].toLowerCase()) {
 				
 				case "load":
 					if(!player.canUseCommand("/wmload")) {
 						return false;
 					}
 					if(split.length < 3) {
 						this.sendUsage(player, split[1]);
 						break;
 					}
 					executor.executeLoadWorld(split[2]);
 					break;
 					
 				case "unload":
 					if(!player.canUseCommand("/wmunload"))  {
 						return false;
 					}
 					if(split.length < 3) {
 						this.sendUsage(player, split[1]);
 						break;
 					}
 					executor.executeUnloadWorld(split[2]);
 					break;
 					
 				case "create":
 					if(!player.canUseCommand("/wmcreate")) {
 						return false;
 					}
 					if(split.length < 4) {
 						this.sendUsage(player, split[1]);
 						break;
 					}
 					World.Type type;
 					switch(split[3].toLowerCase()) {
 						case "default":
 							type = World.Type.DEFAULT;
 							break;
 						case "flat":
 							type = World.Type.FLAT;
 							break;
 						case "largebiomes":
 							type = World.Type.LARGE_BIOMES;
 							break;
 						default:
 							this.sendUsage(player, split[1]);
 							return true;
 					}
 					
 					if (split.length == 4) {
 						executor.executeCreateWorld(split[2], type, new Random(System.currentTimeMillis()).nextLong(), "");
 					} else if (split.length == 5) {
 						long seed = 0;
 						try {
 							seed = Long.parseLong(split[4]);
 							executor.executeCreateWorld(split[2], type, seed, "");
 						} catch(Exception e) {
 							executor.executeCreateWorld(split[2], type, new Random(System.currentTimeMillis()).nextLong(), split[4]);
 						}
 					} else if(split.length == 6) {
 						executor.executeCreateWorld(split[2], type, Long.parseLong(split[4]), split[5]);
 					} else {
 						this.sendUsage(player, split[1]);
 					}
 					
 					break;
 					
 				case "delete":
 					if(!player.canUseCommand("/wmdelete")) {
 						
 					}
 					if(split.length < 4) {
 						this.sendUsage(player, split[1]);
 						break;
 					}
 					String worldname = split[2];
 					World.Dimension dim;
 					switch(split[3].toLowerCase()) {
 						case "all":
 							dim = null;
 							break;
 						case "normal":
 							dim = World.Dimension.NORMAL;
 							break;
 						case "nether":
 							dim = World.Dimension.NETHER;
 							break;
 						case "end":
 							dim = World.Dimension.END;
 							break;
 						default:
 							this.sendUsage(player, split[1]);
 							return true;
 					}
 					executor.executeDeleteWorld(worldname, dim);
 					break;
 					
 				case "tp":
 					if(!player.canUseCommand("/wmtp")) {
 						return false;
 					}
 					if(split.length < 3) {
 						this.sendUsage(player, split[1]);
 						break;
 					}
 					
 					String playerworldname = player.getWorld().getName();
 					String defaultworldname = etc.getServer().getDefaultWorld().getName();
 					InventoryManager.saveInventory(player, playerworldname.equalsIgnoreCase(defaultworldname) ? 0 : WMWorldConfiguration.configs.get(playerworldname).getPropertiesConfiguration().getInt("inventoryId"));
 					
 					World[] world = etc.getServer().getWorld(split[2]);
 					player.switchWorlds(world[World.Dimension.NORMAL.toIndex()]);
 					if (!world[0].getName().equalsIgnoreCase(etc.getServer().getDefaultWorld().getName())) {
 						int gamemode = WMWorldConfiguration.configs.get(world[0].getName()).getPropertiesConfiguration().getInt("gamemode");
 						player.setCreativeMode(gamemode);
 					} else {
 						player.setCreativeMode(etc.getServer().getDefaultWorld().getGameMode());
 					}
 					
 					playerworldname = player.getWorld().getName();
 					InventoryManager.loadInventory(player, playerworldname.equalsIgnoreCase(defaultworldname) ? 0 : WMWorldConfiguration.configs.get(playerworldname).getPropertiesConfiguration().getInt("inventoryId"));
 					break;
 					
 				case "list":
 					StringBuilder sb = new StringBuilder();
 					sb.append(Colors.Green + "Loaded Worlds:" + Colors.White + " ");
 					for(String worldName : etc.getServer().getLoadedWorldNames()) {
 						sb.append(worldName + ", ");
 					}
 					String message = sb.toString().substring(0, sb.toString().length() - 2);
 					player.sendMessage(message);
 					break;
 					
 				default:
 					this.sendHelpMsg(player);
 			}
 			
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public boolean onConsoleCommand(String[] split) {
 		if (split[0].equalsIgnoreCase("wm") || split[0].equalsIgnoreCase("/wm")) {
 			if (!split[0].contains("/")) {
 				split[0] = "/" + split[0];
 			}
 			if (!split[1].equalsIgnoreCase("tp")) {
 				return this.onCommand(new PlayerConsole(), split);
 			} else {
 				return false;
 			}
 		}
 		return false;
 	}
 	
 	public PluginLoader.HookResult canPlayerUseCommand(Player player, String command) {
 		if(player instanceof PlayerConsole) {
 			return PluginLoader.HookResult.ALLOW_ACTION;
 		}
 		return PluginLoader.HookResult.DEFAULT_ACTION;
 	}
 	
 	public boolean onCommandBlockCommand(CommandBlock block, String[] split) {
 		
 		if(split[0].equalsIgnoreCase("wm")) {
 			switch(split[1].toLowerCase()) {
 				case "tp":
 
 			}
 			
 			return true;
 		}
 		
 		return false;
 	}
 
 	
 	private void sendHelpMsg(Player player) {
 		player.notify(Colors.Green + "WorldManager Help:");
 		player.notify(Colors.Gold + "Commands:");
 		player.notify(Colors.Blue + "/wm load <name>");
 		player.notify(Colors.White + "     Loads an existing World");
 		player.notify(Colors.Blue + "/wm unload <name>");
 		player.notify(Colors.White + "     Unloads an existing World");
 		player.notify(Colors.Blue + "/wm create <name> <DEFAULT|FLAT|LARGEBIOMES> [Seed] [Generator Settings]");
 		player.notify(Colors.White + "     Creates a new World");
 		player.notify(Colors.Blue + "/wm delete <name> <OVERWORLD|NETHER|END|ALL>");
 		player.notify(Colors.White + "     Deletes an existing World, the second parameter is the dimension.");
 		player.notify(Colors.Blue + "/wm tp <worldname>");
 		player.notify(Colors.White + "     Teleports you to a World");
 		player.notify(Colors.Blue + "/wm list");
 		player.notify(Colors.White + "     Lists all loaded worlds");
 	}
 	
 	private void sendUsage(Player player, String command) {
 		String usage = "";
 		switch(command.toLowerCase()) {
 			case "load":
 				usage = "/wm load <name>";
 				break;
 			case "unload":
 				usage = "/wm unload <name>";
 				break;
 			case "create":
 				usage = "/wm create <name> <DEFAULT|FLAT|LARGEBIOMES> [Seed] [Generator Settings]";
 				break;
 			case "delete":
 				usage = "/wm delete <name> <OVERWORLD|NETHER|END|ALL>";
 				break;
 			case "tp":
 				usage = "/wm tp <worldname>";
 				break;
 			default:
 				this.sendHelpMsg(player);
 				return;
 		}
 		
 		player.sendMessage(Colors.Red + "Usage: " + Colors.Gold + usage);
 	}
 
 }
