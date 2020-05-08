 package de.MiniDigger.RideThaMob;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 
 public class Commands implements CommandExecutor {
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label,
 			String[] args) {
 		if (cmd.getName().equalsIgnoreCase("ridethamob")) {
 			if ((sender instanceof Player)) {
 				Player p = (Player) sender;
 				if (args.length != 0) {
 					if (args.length == 1) {
 						// Speed Command
 						if (args[0].equalsIgnoreCase("speed")) {
 							if (!p.hasPermission("ridethamob.speed")) {
 								if (RideThaMob.lang.equalsIgnoreCase("de"))
 									p.sendMessage(RideThaMob.cprefix
 											+ "Du hast keinen Zufriff auf den Speed Modus!");
 								else {
 									p.sendMessage(RideThaMob.cprefix
 											+ "You are not allowed to use the speed mode!");
 								}
 								return true;
 							}
 							if (!RideThaMob.speed.contains(p.getName())) {
 								if (RideThaMob.lang.equalsIgnoreCase("de"))
 									p.sendMessage(RideThaMob.cprefix
 											+ "Speed Modus aktiviert");
 								else {
 									p.sendMessage(RideThaMob.cprefix
 											+ "Speed Mode activated");
 								}
 								RideThaMob.speed.add(p.getName());
 							} else {
 								if (RideThaMob.lang.equalsIgnoreCase("de"))
 									p.sendMessage(RideThaMob.cprefix
 											+ "Speed Modus deaktiviert");
 								else {
 									p.sendMessage(RideThaMob.cprefix
 											+ "Speed Mode deactivated");
 								}
 								RideThaMob.speed.remove(p.getName());
 							}
 						} else
 						// Reload Command
 						if (args[0].equalsIgnoreCase("reload")) {
 							if (p.hasPermission("ridethamob.reload")) {
 								RideThaMob.pl.reloadConfig();
 								RideThaMob.pl.loadConfig();
 								if (RideThaMob.lang.equalsIgnoreCase("de"))
 									p.sendMessage(RideThaMob.cprefix
 											+ "Config neu geladen");
 								else
 									p.sendMessage(RideThaMob.cprefix
 											+ "Config reloaded");
 							} else {
 								if (RideThaMob.lang.equalsIgnoreCase("de"))
 									p.sendMessage(RideThaMob.cprefix
 											+ "Du hast keinen Zufriff auf diesen Befehl!");
 								else {
 									p.sendMessage(RideThaMob.cprefix
 											+ "You are not allowed to use RideThaMob command!");
 								}
 								return true;
 							}
 
 						} else
 						// Control Command
 						if (args[0].equalsIgnoreCase("control")) {
 							if (p.hasPermission("ridethamob.control")) {
 								if (!RideThaMob.control.contains(p.getName())) {
 									if (RideThaMob.lang.equalsIgnoreCase("de"))
 										p.sendMessage(RideThaMob.cprefix
 												+ "Control Modus aktiviert");
 									else {
 										p.sendMessage(RideThaMob.cprefix
 												+ "Control Mode activated");
 									}
 									RideThaMob.control.add(p.getName());
 								} else {
 									if (RideThaMob.lang.equalsIgnoreCase("de"))
 										p.sendMessage(RideThaMob.cprefix
 												+ "Control Modus deaktiviert");
 									else {
 										p.sendMessage(RideThaMob.cprefix
 												+ "Control Mode deactivated");
 									}
 									RideThaMob.control.remove(p.getName());
 								}
 							} else if (RideThaMob.lang.equalsIgnoreCase("de"))
 								p.sendMessage(RideThaMob.cprefix
 										+ "Du hast keinen Zufriff auf den Control Modus!");
 							else {
 								p.sendMessage(RideThaMob.cprefix
 										+ "You are not allowed to use the control mode!");
 							}
 
 						} else // Nyan Command
 						if (args[0].equalsIgnoreCase("nyan")) {
 							if (p.hasPermission("ridethamob.nyan")) {
 								if (p.isInsideVehicle()
 										&& p.getVehicle().getType() == EntityType.SHEEP) {
 									NyanTask task = new NyanTask(RideThaMob.pl);
 									task.start(p);
 								} else {
 									if (RideThaMob.lang.equalsIgnoreCase("de"))
 										p.sendMessage(RideThaMob.cprefix
 												+ "Du bist nicht auf einem Schaf!");
 									else {
 										p.sendMessage(RideThaMob.cprefix
 												+ "You have to ride a sheep to use that command!");
 									}
 								}
 
 							} else if (RideThaMob.lang.equalsIgnoreCase("de"))
 								p.sendMessage(RideThaMob.cprefix
 										+ "Du hast keinen Zufriff auf den Nyan Cat Modus!");
 							else {
 								p.sendMessage(RideThaMob.cprefix
 										+ "You are not allowed to use the nyan cat mode!");
 							}
 						} else // Update COmmand
 						if (args[0].equalsIgnoreCase("update")) {
 							if (p.hasPermission("ridethamob.update")) {
 								doUpdate(sender);
 
 							} else if (RideThaMob.lang.equalsIgnoreCase("de"))
 								p.sendMessage(RideThaMob.cprefix
 										+ "Du hast keinen Zufriff auf den Updater!");
 							else {
 								p.sendMessage(RideThaMob.cprefix
 										+ "You are not allowed to use the updater!");
 							}
 						} else // Fly command
 						if (args[0].equalsIgnoreCase("fly")) {
 							if (p.hasPermission("ridethamob.fly")) {
 								if (!RideThaMob.fly.contains(p.getName())) {
 									if (RideThaMob.lang.equalsIgnoreCase("de"))
 										p.sendMessage(RideThaMob.cprefix
 												+ "Fly Modus aktiviert");
 									else {
 										p.sendMessage(RideThaMob.cprefix
 												+ "Fly Mode activated");
 									}
 									RideThaMob.fly.add(p.getName());
 								} else {
 									if (RideThaMob.lang.equalsIgnoreCase("de"))
 										p.sendMessage(RideThaMob.cprefix
 												+ "Fly Modus deaktiviert");
 									else {
 										p.sendMessage(RideThaMob.cprefix
 												+ "Fly Mode deactivated");
 									}
 									RideThaMob.fly.remove(p.getName());
 								}
 							} else if (RideThaMob.lang.equalsIgnoreCase("de"))
 								p.sendMessage(RideThaMob.cprefix
 										+ "Du hast keinen Zufriff auf den Fly Modus!");
 							else {
 								p.sendMessage(RideThaMob.cprefix
 										+ "You are not allowed to use the fly mode!");
 							}
 						} else // command not found
 						if (RideThaMob.lang.equalsIgnoreCase("de")) {
 
 							p.sendMessage(RideThaMob.cprefix
 									+ "Befehl nicht gefunden!");
 						} else {
 							p.sendMessage(RideThaMob.cprefix
 									+ "Command not found!");
 						}
 
 					} else if (RideThaMob.lang.equalsIgnoreCase("de"))
 						p.sendMessage(RideThaMob.cprefix
 								+ "Zu viele Argumente!");
 					else {
 						p.sendMessage(RideThaMob.cprefix
 								+ "Too much arguments!");
 					}
 
 					return true;
 				}
 				// aufsteigen
 				if (p.getVehicle() == null) {
 					RideThaMobListener.checkNearRideable(p);
 				}
 				// Aussteigen
 				else {
 					RideThaMob.player.remove(p.getName());
 					p.getVehicle().eject();
 					if (RideThaMob.lang.equalsIgnoreCase("de"))
 						p.sendMessage(RideThaMob.cprefix
 								+ "Du kann nun wieder alleine gehen :D");
 					else
 						p.sendMessage(RideThaMob.cprefix
 								+ "Now u can walk on your own feeds again");
 				}
 			}
 			// version anzeigen
 			else {
 				if (args.length == 1 && args[0].equalsIgnoreCase("update")) {
 					doUpdate(sender);
 				} else {
 					sender.sendMessage(RideThaMob.prefix
 							+ "---------RideThaMob---------");
 
 					sender.sendMessage(RideThaMob.prefix
 							+ "Version "
 							+ RideThaMob.pl.getDescription().getVersion()
 							+ " by "
 							+ (String) RideThaMob.pl.getDescription()
 									.getAuthors().get(0));
 
 					sender.sendMessage(RideThaMob.prefix
 							+ "----------------------------");
 				}
 			}
 		}
 		return true;
 	}
 
 	public void doUpdate(CommandSender s) {
 		if (RideThaMob.update) {
 			RideThaMob.pl.getLogger().info(
 					"Starting Update. This may take a little while!");
 			Updater updater = new Updater(RideThaMob.pl, 53240,
 					RideThaMob.file, Updater.UpdateType.NO_VERSION_CHECK, true);
 			if (updater.getResult() != Updater.UpdateResult.SUCCESS) {
 				if (RideThaMob.lang.equalsIgnoreCase("de"))
 					s.sendMessage(RideThaMob.cprefix + "Fehler beim Updaten! "
 							+ updater.getResult());
 				else {
 					s.sendMessage(RideThaMob.cprefix + "Error while updating! "
 							+ updater.getResult());
 				}
 			} else if (RideThaMob.lang.equalsIgnoreCase("de"))
 				s.sendMessage(RideThaMob.cprefix
						+ "Das Plugin wurde erfolgreich Upgedatet! Beim nchsten Neustart wirst du die neuste Version haben");
 			else {
 				s.sendMessage(RideThaMob.cprefix
 						+ "Plugin updatet! Please restart your server!");
 			}
 		} else if (RideThaMob.lang.equalsIgnoreCase("de"))
 			s.sendMessage(RideThaMob.cprefix
 					+ "Du hast bereits die neuste Version!");
 		else {
 			s.sendMessage(RideThaMob.cprefix
 					+ "You have to already have newest version!");
 		}
 	}
 }
