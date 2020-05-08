 import java.io.*;
 import java.util.*;
 import java.util.logging.*;
 
 public class Wanted extends Plugin {
 	
 	private List<PluginRegisteredListener> pluginListeners = new ArrayList<PluginRegisteredListener>();
 	private List<WantedPlayer> wantedPlayers = new ArrayList<WantedPlayer>();
 	private Listener pluginListener;
 	private Messenger pluginMessenger;
 	private Settings pluginSettings;
 
 	private static final File pluginSettingsFile = new File("Wanted.dat");
 	private static final List<PluginLoader.Hook> pluginHooks = Arrays.asList(PluginLoader.Hook.COMMAND, PluginLoader.Hook.DAMAGE, PluginLoader.Hook.LOGIN, PluginLoader.Hook.BLOCK_RIGHTCLICKED);
 	private static final Logger pluginLogger = Logger.getLogger("Minecraft");
 	private static final String pluginName = "Wanted!", pluginVersion = "v0.2.1", pluginChatPrefix = Colors.White + "[" + Colors.Yellow + "Wanted!" + Colors.White + "] " + Colors.Rose, pluginPrefix = "[Wanted!] ";
 
 	public void disable() {
 		for (PluginRegisteredListener pluginHook : pluginListeners) {
 			etc.getLoader().removeListener(pluginHook);
 		}
 		pluginSettings.saveSettings();
 		wantedPlayers.clear();
 		pluginLogger.info(pluginName + " " + pluginVersion + " has been disabled!");
 	}
 	
 	protected void finalize() throws Throwable {
 		pluginSettings.saveSettings();
 		super.finalize();
 	}
 
 	public void enable() {
 		pluginListener = new Listener();
 		pluginMessenger = new Messenger();
 		pluginSettings = new Settings();
 		
 		for (PluginLoader.Hook pluginHook : pluginHooks) {
 			pluginListeners.add(etc.getLoader().addListener(pluginHook, pluginListener, this, PluginListener.Priority.MEDIUM));
 		}
 		
 		pluginSettings.loadSettings();
 		pluginLogger.info(pluginName + " " + pluginVersion + " has been initialized!");
 	}
 	
 	enum Command {
 		CREATE, REMOVE, RESET, RELOAD, SAVE, NOCOMMAND;
 		
 		public static Command toCommand(String command) {
 			try {
 				return valueOf(command.toUpperCase());
 			} catch (Throwable ex) {
				pluginLogger.severe("[Wanted!] An exception has been thrown : " + ex.getMessage());
				ex.printStackTrace();
 			}
 			return NOCOMMAND;
 		}
 	}
 	
 	class Listener extends PluginListener {
 
 		public boolean onCommand(Player player, java.lang.String[] split) {
 			try {
 				if (((split[0].equalsIgnoreCase("/wanted")) || (split[0].equalsIgnoreCase("/wd")))
 						&& ((player.canUseCommand(split[0])) || (player.isAdmin()))) {
 					if (split.length < 2) {
 						if (player.isAdmin()) {
 							player.sendMessage(pluginChatPrefix + "Usage: /wd [create] [username] [itemid] [amount]");
 							player.sendMessage(pluginChatPrefix + "Usage: /wd [remove] [username]");
 							player.sendMessage(pluginChatPrefix + "Usage: /wd [reset]  <username>");
 							player.sendMessage(pluginChatPrefix + "Usage: /wd [reload]");
 							player.sendMessage(pluginChatPrefix + "Usage: /wd [save]");
 						} else {
 							player.sendMessage(pluginChatPrefix + "Usage: /wd [create] [username] [itemid] [amount]");
 							player.sendMessage(pluginChatPrefix + "Usage: /wd [remove] [username]");
 						}
 						return true;
 					}
 					
 					Player p = new Player();
 					Inventory playerInventory;
 					
 					switch (Command.toCommand(split[1])) {
 						case CREATE:
 							if (split[2].equalsIgnoreCase(player.getName())) {
 								player.sendMessage(pluginChatPrefix + "You can't give yourself a bounty!");
 								return true;
 							} else if (split.length < 5) {
 								player.sendMessage(pluginChatPrefix + "Usage: /wd [create] [username] [itemid] [amount]");
 								return true;
 							}
 							
 							Integer itemId = Integer.parseInt(split[3]), amount = Integer.parseInt(split[4]);
 							playerInventory = player.getInventory();
 							
 							if ((Item.isValidItem(itemId)) && (playerInventory.hasItem(itemId, amount, Integer.MAX_VALUE))) {
 								for (Integer i = 0; i < amount; i++) {
 									playerInventory.removeItem(new Item(itemId, 1));
 								}
 								playerInventory.updateInventory();
 								wantedPlayers.add(new WantedPlayer(split[2], new PlayerReward(player.getName(), itemId, amount)));
 								player.sendMessage(pluginChatPrefix + "You have created a bounty on \"" + split[2] + "\"!");
 								pluginLogger.info(pluginPrefix + player.getName() + " placed a bounty on " + split[2]);
 								pluginSettings.saveSettings();
 							}
 							break;
 							
 						case REMOVE:
 							if (split.length < 3) {
 								player.sendMessage(pluginChatPrefix + "Usage: /wd [remove] [username]");
 								return true;
 							}
 							
 							for (WantedPlayer wantedPlayer : wantedPlayers) {
 								if (wantedPlayer.getName().equalsIgnoreCase(split[2])) {
 									for (PlayerReward reward : wantedPlayer.getPlayerRewards()) {
 										if (reward.getPlacer().equalsIgnoreCase(player.getName())) {
 											wantedPlayer.removeReward(player.getName());
 											player.giveItem(reward.getItemId(), reward.getAmount());
 											if (wantedPlayer.getRewardCount() < 1) {
 												wantedPlayers.remove(wantedPlayer);
 												player.sendMessage(pluginChatPrefix + "You removed the bounty on \"" + split[2] + "\"!");
 												pluginLogger.info(pluginPrefix + player.getName() + " removed " + split[2] +  "'s bounty");
 											} else {
 												player.sendMessage(pluginChatPrefix + "You removed your portion of the bounty on \"" + split[2] + "\"!");
 												pluginLogger.info(pluginPrefix + player.getName() + " modified " + split[2] +  "'s bounty");
 											}
 											pluginSettings.saveSettings();
 											return true;
 										}
 									}
 								}
 							}
 							
 							player.sendMessage(pluginChatPrefix + "There is no bounty, from you, on " + split[2] + "!");
 							break;
 							
 						case RESET:
 							if (player.isAdmin()) {
 								if (split.length == 2) {
 									wantedPlayers.clear();
 									pluginSettings.saveSettings();
 									pluginLogger.info(pluginPrefix + "All bounties were removed by \"" + player.getName() + "\"");
 									pluginMessenger.messageAll(pluginChatPrefix + "All bounties have been removed!");
 								} else {
 									if (split[2].length() != 0) {
 										for (WantedPlayer wantedPlayer : wantedPlayers) {
 											if (wantedPlayer.getName().equalsIgnoreCase(p.getName())) {
 												wantedPlayers.remove(wantedPlayer);
 												pluginLogger.info(pluginPrefix + split[2] + "'s bounty was removed by \"" + player.getName() + "\"");
 												player.sendMessage(pluginChatPrefix + split[2] + "'s bounty has been removed!");
 												pluginMessenger.messageAll(pluginChatPrefix + split[2] + " no longer has a bounty!");
 												pluginSettings.saveSettings();
 											}
 										}
 									}
 								}
 							}
 							break;
 							
 						case RELOAD:
 							if (player.isAdmin()) {
 								pluginSettings.loadSettings();
 								pluginLogger.info(pluginPrefix + "All bounties were loaded by \"" + player.getName() + "\"");
 								player.sendMessage(pluginChatPrefix + "All bounties have been reloaded!");
 							}
 							break;
 							
 						case SAVE:
 							if (player.isAdmin()) {
 								pluginSettings.saveSettings();
 								pluginLogger.info(pluginPrefix + "All bounties were saved by \"" + player.getName() + "\"");
 								player.sendMessage(pluginChatPrefix + "All bounties have been saved!");
 							}
 							break;
 							
 						default:
							return false;
 					}
 					
 					return true;
 				}
 				return false;
 			} catch (Throwable ex) {
 				pluginLogger.severe("[Wanted!] An exception has been thrown : " + ex.getMessage());
 				ex.printStackTrace();
 	            return false;
 			}
 		}
 		
 		public void onLogin(Player player) {
 			for (WantedPlayer wantedPlayer : wantedPlayers) {
 				if (wantedPlayer.getName().equalsIgnoreCase(player.getName())) {
 					pluginMessenger.messageAll(pluginChatPrefix + player.getName() + " has joined and has a bounty!");
 				}
 			}
 		}
 		
 		public boolean onDamage(PluginLoader.DamageType type, BaseEntity attacker, BaseEntity defender, int amount) {
 			if ((defender != null) && (attacker != null)) {
 				if ((defender.isPlayer()) && (attacker.isPlayer())) {
 					Player defendingPlayer = defender.getPlayer(), attackingPlayer = attacker.getPlayer();
 					if (defendingPlayer.getHealth() <= amount) {
 						for (WantedPlayer player : wantedPlayers) {
 							if (player.getName().equalsIgnoreCase(defendingPlayer.getName())) {
 								pluginMessenger.messageAll(pluginChatPrefix + defendingPlayer.getName() + " was defeated by " + attackingPlayer.getName() + "!");
 								for (PlayerReward reward: player.getPlayerRewards()) {
 									attackingPlayer.giveItem(reward.getItemId(), reward.getAmount());
 								}
 								wantedPlayers.remove(player);
 								pluginSettings.saveSettings();
 							}
 						}
 					}
 				}
 			}
 			return false;
 		}
 		
 		public void onBlockRightClicked(Player player, Block blockClicked, Item item) {
 			if (blockClicked.getType() == 63) {
 				ComplexBlock complexBlock = etc.getServer().getComplexBlock(blockClicked.getX(), blockClicked.getY(), blockClicked.getZ());
 				Sign placedSign = (Sign)complexBlock;
 				
 				try
 				{
 					if (placedSign.getText(0).equalsIgnoreCase("-- WANTED --")) {
 						// The user is trying to create a bounty
 						if (player.canUseCommand("/wd")) {							
 							String split[] = placedSign.getText(2).split(":");
 							Integer itemId = Integer.parseInt(split[0]), amount = Integer.parseInt(split[1]);
 							Inventory playerInventory = player.getInventory();
 							
 							for (WantedPlayer wantedPlayer : wantedPlayers) {
 								if (wantedPlayer.getName().equalsIgnoreCase(placedSign.getText(1))) {
 									if ((Item.isValidItem(itemId)) && (playerInventory.hasItem(itemId, amount, Integer.MAX_VALUE))) {
 										for (Integer i = 0; i < amount; i++) {
 											playerInventory.removeItem(new Item(itemId, 1));
 										}
 										playerInventory.updateInventory();
 										player.sendMessage(pluginChatPrefix + "You have updated the bounty on \"" + placedSign.getText(1) + "\"!");
 										pluginLogger.info(pluginPrefix + player.getName() + " updated the bounty on " + placedSign.getText(1));
 										pluginSettings.saveSettings();
 										return;
 									} else {
 										player.sendMessage(pluginChatPrefix + "Unable to read the sign!");
 										return;
 									}
 								}
 							}
 							
 							if ((Item.isValidItem(itemId)) && (playerInventory.hasItem(itemId, amount, Integer.MAX_VALUE))) {
 								for (Integer i = 0; i < amount; i++) {
 									playerInventory.removeItem(new Item(itemId, 1));
 								}
 								playerInventory.updateInventory();
 								wantedPlayers.add(new WantedPlayer(placedSign.getText(1), new PlayerReward(player.getName(), itemId, amount)));
 								player.sendMessage(pluginChatPrefix + "You have created a bounty on \"" + placedSign.getText(1) + "\"!");
 								pluginLogger.info(pluginPrefix + player.getName() + " placed a bounty on " + placedSign.getText(1));
 								pluginSettings.saveSettings();
 								return;
 							} else {
 								player.sendMessage(pluginChatPrefix + "Unable to read the sign!");
 								return;
 							}
 						}
 					}
 				} catch (Exception e) {
 					pluginLogger.severe("[Wanted!] An exception has been thrown : " + e.getMessage());
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	class Messenger {
 	
 		public void messageAll(final String message) {
 			etc.getServer().addToServerQueue(new Runnable() {
 				
 				public void run() {
 					etc.getServer().messageAll(message);
 				}
 				
 			});
 		}
 	}
 	
 	class Settings {
 		
 		@SuppressWarnings("unchecked")
 		public void loadSettings() {
 			if (this.fileExists()) {
 				try {
 					FileInputStream fileInputStream = new FileInputStream(pluginSettingsFile);
 					ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
 					Object temporaryObject = objectInputStream.readObject();
 					if (temporaryObject instanceof List<?>) {
 						wantedPlayers = (List<WantedPlayer>) temporaryObject;
 					}
 					fileInputStream.close();
 				} catch (Exception e) {
 					pluginLogger.severe("[Wanted!] An exception has been thrown : " + e.getMessage());
 					e.printStackTrace();
 				}
 			} else {
 				wantedPlayers = new ArrayList<WantedPlayer>();
 				saveSettings();
 			}
 		}
 		
 		public void saveSettings() {
 			try {
 				FileOutputStream fileOutputStream = new FileOutputStream(pluginSettingsFile);
 				ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
 				if (fileExists()) {
 					objectOutputStream.writeObject(wantedPlayers);
 				}
 				fileOutputStream.close();
 			} catch (Exception e) {
 				pluginLogger.severe("[Wanted!] An exception has been thrown : " + e.getMessage());
 				e.printStackTrace();
 			}
 		}
 		
 		public boolean fileExists() {
 			return pluginSettingsFile.exists();
 		}
 		
 	}
 }
