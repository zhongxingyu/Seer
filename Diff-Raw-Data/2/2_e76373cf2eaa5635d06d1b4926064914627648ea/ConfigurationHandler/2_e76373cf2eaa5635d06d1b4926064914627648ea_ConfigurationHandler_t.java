 package me.Miny.Paypassage.config;
 
 import java.io.File;
 import java.io.IOException;
 import me.Miny.Paypassage.Paypassage;
 import me.Miny.Paypassage.logger.LoggerUtility;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 /**
  * 
  * @author ibhh
  */
 public class ConfigurationHandler {
 
 	private YamlConfiguration language_config;
 	private Paypassage plugin;
 
 	/**
 	 * Creates a new ConfigurationHandler
 	 * 
 	 * @param plugin
 	 *            Needed for saving configs
 	 */
 	public ConfigurationHandler(Paypassage plugin) {
 		this.plugin = plugin;
 	}
 
 	/**
 	 * Returns the current language configuration
 	 * 
 	 * @return YamlConfiguration
 	 */
 	public YamlConfiguration getLanguage_config() {
 		return language_config;
 	}
 
 	/**
 	 * 
 	 * @return plugin.getConifg();
 	 */
 	public FileConfiguration getConfig() {
 		return plugin.getConfig();
 	}
 
 	/**
 	 * Called on start
 	 * 
 	 * @return true if config was successfully loaded, false if it failed;
 	 */
 	public boolean onStart() {
 		// loading main config
 		try {
 			plugin.getConfig().options().copyDefaults(true);
 			plugin.saveConfig();
 			plugin.reloadConfig();
 			plugin.getLoggerUtility().log("Config loaded", LoggerUtility.Level.DEBUG);
 		} catch (Exception e) {
 			plugin.getLoggerUtility().log("Cannot create config!", LoggerUtility.Level.ERROR);
 			e.printStackTrace();
 			plugin.onDisable();
 		}
 		createLanguageConfig();
 		return true;
 	}
 
 	/**
 	 * Creates the language config and added defaults
 	 */
 	private void createLanguageConfig() {
 		for (int i = 0; i < 2; i++) {
 			String a = "";
 			if (i == 0) {
 				a = "de";
 			} else {
 				a = "en";
 			}
 			File folder = new File(plugin.getDataFolder() + File.separator);
 			folder.mkdirs();
 			File configl = new File(plugin.getDataFolder() + File.separator + "language_" + a + ".yml");
 			if (!configl.exists()) {
 				try {
 					configl.createNewFile();
 				} catch (IOException ex) {
 					plugin.getLoggerUtility().log("Couldnt create new config file!", LoggerUtility.Level.ERROR);
 				}
 			}
 			language_config = YamlConfiguration.loadConfiguration(configl);
 			if (i == 0) {
 				// permission output
 				language_config.addDefault("permission.error", "Wir haben ein Problem! Dies darfst Du nicht machen!");
 				// privacy output
 				language_config.addDefault("privacy.notification.1", "Dieses plugin speichert nutzerbezogene Daten in eine Datei");
 				language_config.addDefault("privacy.notification.2", "\"/pp allowtracking\" um den Plugin dies zu erlauben");
 				language_config.addDefault("privacy.notification.3", "\"/pp denytracking\" um deine Daten zu anonymisieren");
 				
 				language_config.addDefault("privacy.notification.denied", "Das Plugin speichert nun keine nutzerbezogene Daten mehr");
 				language_config.addDefault("privacy.notification.allowed", "Das Plugin speichert deine Daten, wende Dich an einen Admin um diese z.B. loeschen zu lassen");
 				
 				language_config.addDefault("creation.sign.notification1", "Bitte mache einen rechtsklick auf das Schild");
 				language_config.addDefault("creation.sign.notification2", "Schild akzeptiert");
 				language_config.addDefault("creation.sign.notification21", "Schild schon benutzt.");
 				language_config.addDefault("creation.sign.notification3", "Du hast bereits ein Schild ausgewaehlt.");
 				language_config.addDefault("creation.sign.notification4", "Bitte gehe zum Zielpunkt und mache \"pp setdestination\"");
 				language_config.addDefault("creation.sign.notification5", "Zielpunkt gesetzt. Waehle nun einen Preis mit /pp setprice [Preis]");
 				language_config.addDefault("creation.sign.notification6", "Du erstellst bereits ein PP Schild.");
				language_config.addDefault("creation.sign.notification7", "Teleportentfernung zu weit: ");
				language_config.addDefault("creation.sign.notification8", "Teleportpreis zu gering, min: ");
 				
 				language_config.addDefault("creation.sign.notification.cancel", "Erstellung eines PP Schildes abgebrochen!");
 				language_config.addDefault("creation.sign.notification.success", "Erstellung eines PP Schildes abgeschlossen.");
 				
 				language_config.addDefault("interact.sign.notification.confirm", "Willst Du dich fr %f [Geldeinheit] teleportieren? Bestaetige mit /pp confirm");
 				language_config.addDefault("interact.sign.notification.success", "Teleportiert.");
 				language_config.addDefault("interact.sign.notification.error.nomoney", "Du hast nicht genug [Geldeinheit]!");
 
 				
 				language_config.addDefault("creation.sign.nopaypassagesign", "Du musst ein \"[Paypassage]\" Schild auswaehlen");
 
 				// reload command
 				language_config.addDefault("commands.reload.name", "reload");
 				language_config.addDefault("commands.reload.permission", "Paypassage.reload");
 				language_config.addDefault("commands.reload.description", "Laedt das Plugin neu");
 				language_config.addDefault("commands.reload.usage", "/pp reload");
 				
 				language_config.addDefault("commands.help.name", "help");
 				language_config.addDefault("commands.help.permission", "Paypassage.help");
 				language_config.addDefault("commands.help.description", "Zeigt die Hilfe an");
 				language_config.addDefault("commands.help.usage", "/pp help");
 				
 				language_config.addDefault("commands.version.name", "version");
 				language_config.addDefault("commands.version.permission", "Paypassage.version");
 				language_config.addDefault("commands.version.description", "Zeigt die Version an");
 				language_config.addDefault("commands.version.usage", "/pp version");
 
 				// privacy commands
 				language_config.addDefault("commands.denytracking.name", "denytracking");
 				language_config.addDefault("commands.denytracking.permission", "Paypassage.user");
 				language_config.addDefault("commands.denytracking.description", "Zwingt das Plugin deine Daten zu anonymisieren");
 				language_config.addDefault("commands.denytracking.usage", "/pp denytracking");
 
 				language_config.addDefault("commands.allowtracking.name", "allowtracking");
 				language_config.addDefault("commands.allowtracking.permission", "Paypassage.user");
 				language_config.addDefault("commands.allowtracking.description", "Erlaubt dem Plugin deine Daten zu speichern");
 				language_config.addDefault("commands.allowtracking.usage", "/pp allowtracking");
 
 				//PP Sign commands
 				// create command
 				language_config.addDefault("commands.create.name", "create");
 				language_config.addDefault("commands.create.permission", "Paypassage.create");
 				language_config.addDefault("commands.create.description", "Erstellt ein neues Paypassage Schild");
 				language_config.addDefault("commands.create.usage", "/pp create [name]");
 				
 				language_config.addDefault("commands.confirm.name", "confirm");
 				language_config.addDefault("commands.confirm.permission", "Paypassage.teleport");
 				language_config.addDefault("commands.confirm.description", "Bestaetigt einen Teleport");
 				language_config.addDefault("commands.confirm.usage", "/pp confirm");
 
 				// cancel command
 				language_config.addDefault("commands.cancel.name", "cancel");
 				language_config.addDefault("commands.cancel.permission", "Paypassage.create");
 				language_config.addDefault("commands.cancel.description", "Bricht das erstellen eines PP Schildes ab.");
 				language_config.addDefault("commands.cancel.usage", "/pp cancel");
 
 				//setdestination command
 				language_config.addDefault("commands.setdestination.name", "setdestination");
 				language_config.addDefault("commands.setdestination.permission", "Paypassage.create");
 				language_config.addDefault("commands.setdestination.description", "Setz Zielpunkt des Teleports.");
 				language_config.addDefault("commands.setdestination.usage", "/pp setdestination");
 				
 				language_config.addDefault("commands.setprice.name", "setprice");
 				language_config.addDefault("commands.setprice.permission", "Paypassage.create");
 				language_config.addDefault("commands.setprice.description", "Setzt die Kosten des Schildes");
 				language_config.addDefault("commands.setprice.usage", "/pp setprice [price]");
 				
 			} else {
 				language_config.addDefault("permission.error", "we have a problem! You musnt do this!");
 				
 				language_config.addDefault("privacy.notification.1", "this plugin saves your interact events to a log");
 				language_config.addDefault("privacy.notification.2", "\"/pp allowtracking\" to allow the plugin to save your data");
 				language_config.addDefault("privacy.notification.3", "\"/pp denytracking\" to anonymise your data");
 				
 				language_config.addDefault("privacy.notification.denied", "The plugin anonymises your data now");
 				language_config.addDefault("privacy.notification.allowed", "The plugin saves your data now, to delete the data, please tell an admin");
 				
 				language_config.addDefault("creation.sign.notification1", "Please do a right-click on a sign");
 				language_config.addDefault("creation.sign.notification2", "Sign accepted");
 				language_config.addDefault("creation.sign.notification21", "Sign already used.");
 				language_config.addDefault("creation.sign.notification3", "You have already choosen a sign.");
 				language_config.addDefault("creation.sign.notification4", "Please go to the destination and do \"/pp setdestination\"");
 				language_config.addDefault("creation.sign.notification5", "Destination set! Please choose the price with /pp setprice [price]");
 				language_config.addDefault("creation.sign.notification6", "You are currently creating a PP sign.");
 				language_config.addDefault("creation.sign.notification7", "Teleport distance to long: ");
 				language_config.addDefault("creation.sign.notification8", "Teleport must cost min: ");
 				
 				language_config.addDefault("creation.sign.notification.cancel", "Canceled creation off a PP sign!");
 				language_config.addDefault("creation.sign.notification.success", "PP sign created.");
 
 				language_config.addDefault("interact.sign.notification.confirm", "Do you want to teleport you for %f [Money]? Please execute /pp confirm");
 				language_config.addDefault("interact.sign.notification.success", "Teleported!");
 				language_config.addDefault("interact.sign.notification.error.nomoney", "You haven't got enough money to use this sign!");
 				
 				
 				language_config.addDefault("creation.sign.nopaypassagesign", "You must choose a \"[Paypassage]\" sign!");
 
 				// reload command
 				language_config.addDefault("commands.reload.name", "reload");
 				language_config.addDefault("commands.reload.permission", "Paypassage.reload");
 				language_config.addDefault("commands.reload.description", "Reloads the plugin");
 				language_config.addDefault("commands.reload.usage", "/pp reload");
 				
 				language_config.addDefault("commands.help.name", "help");
 				language_config.addDefault("commands.help.permission", "Paypassage.help");
 				language_config.addDefault("commands.help.description", "Shows help");
 				language_config.addDefault("commands.help.usage", "/pp help");
 				
 				language_config.addDefault("commands.version.name", "version");
 				language_config.addDefault("commands.version.permission", "Paypassage.version");
 				language_config.addDefault("commands.version.description", "Shows current version");
 				language_config.addDefault("commands.version.usage", "/pp version");
 
 				language_config.addDefault("commands.denytracking.name", "denytracking");
 				language_config.addDefault("commands.denytracking.permission", "Paypassage.user");
 				language_config.addDefault("commands.denytracking.description", "forces the plugin to anonymise your data");
 				language_config.addDefault("commands.denytracking.usage", "/pp denytracking");
 
 				language_config.addDefault("commands.allowtracking.name", "allowtracking");
 				language_config.addDefault("commands.allowtracking.permission", "Paypassage.user");
 				language_config.addDefault("commands.allowtracking.description", "Allows the plugin to save userdata");
 				language_config.addDefault("commands.allowtracking.usage", "/pp allowtracking");
 
 				language_config.addDefault("commands.create.name", "create");
 				language_config.addDefault("commands.create.permission", "Paypassage.create");
 				language_config.addDefault("commands.create.description", "Creates a new Paypassage sign");
 				language_config.addDefault("commands.create.usage", "/pp create [name]");
 				
 				language_config.addDefault("commands.confirm.name", "confirm");
 				language_config.addDefault("commands.confirm.permission", "Paypassage.teleport");
 				language_config.addDefault("commands.confirm.description", "Confirms a teleport");
 				language_config.addDefault("commands.confirm.usage", "/pp confirm");
 				
 				// cancel command
 				language_config.addDefault("commands.cancel.name", "cancel");
 				language_config.addDefault("commands.cancel.permission", "Paypassage.create");
 				language_config.addDefault("commands.cancel.description", "Stops the creating of a PP sign.");
 				language_config.addDefault("commands.cancel.usage", "/pp cancel");
 
 				language_config.addDefault("commands.setdestination.name", "setdestination");
 				language_config.addDefault("commands.setdestination.permission", "Paypassage.create");
 				language_config.addDefault("commands.setdestination.description", "Sets destination of a Paypassage sign");
 				language_config.addDefault("commands.setdestination.usage", "/pp setdestination");
 				
 				language_config.addDefault("commands.setprice.name", "setprice");
 				language_config.addDefault("commands.setprice.permission", "Paypassage.create");
 				language_config.addDefault("commands.setprice.description", "Sets the price of the sign");
 				language_config.addDefault("commands.setprice.usage", "/pp setprice [price]");
 
 			}
 			try {
 				language_config.options().copyDefaults(true);
 				language_config.save(configl);
 			} catch (IOException ex) {
 				ex.printStackTrace();
 				plugin.getLoggerUtility().log("Couldnt save language config!", LoggerUtility.Level.ERROR);
 			}
 		}
 		File configl = new File(plugin.getDataFolder() + File.separator + "language_" + plugin.getConfig().getString("language") + ".yml");
 		try {
 			language_config = YamlConfiguration.loadConfiguration(configl);
 		} catch (Exception e) {
 			e.printStackTrace();
 			plugin.getLoggerUtility().log("Couldnt load language config!", LoggerUtility.Level.ERROR);
 			plugin.getConfig().set("language", "en");
 			plugin.saveConfig();
 			plugin.onDisable();
 			return;
 		}
 		plugin.getLoggerUtility().log("language config loaded", LoggerUtility.Level.DEBUG);
 	}
 }
