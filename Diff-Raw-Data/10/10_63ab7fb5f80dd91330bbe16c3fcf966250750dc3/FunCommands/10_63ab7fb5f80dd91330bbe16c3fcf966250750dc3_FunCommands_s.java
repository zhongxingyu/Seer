 /************************************************************************
  * This file is part of FunCommands.
  *
  * FunCommands is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * ExamplePlugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with FunCommands.  If not, see <http://www.gnu.org/licenses/>.
  ************************************************************************/
 
 package de.Lathanael.FC.FunCommands;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 
 import de.Lathanael.FC.Commands.Attaint;
 import de.Lathanael.FC.Commands.Barrage;
 import de.Lathanael.FC.Commands.Entomb;
 import de.Lathanael.FC.Commands.FCVoid;
 import de.Lathanael.FC.Commands.Rocket;
 import de.Lathanael.FC.Commands.Sacrifice;
 import de.Lathanael.FC.Commands.Slap;
 import de.Lathanael.FC.Commands.Wound;
 import de.Lathanael.FC.Listeners.FCChatListener;
 import de.Lathanael.FC.Listeners.FCPlayerListener;
 import de.Lathanael.FC.Tools.BlocksOld;
 import be.Balor.Manager.LocaleManager;
 import be.Balor.Manager.Permissions.PermParent;
 import be.Balor.Player.ACPlayer;
 import be.Balor.Tools.Utils;
 import be.Balor.Tools.Configuration.File.ExtendedConfiguration;
 import be.Balor.Tools.Egg.EggTypeClassLoader;
 import be.Balor.bukkit.AdminCmd.ACPluginManager;
 import be.Balor.bukkit.AdminCmd.AbstractAdminCmdPlugin;
 
 
 /**
  * @author Lathanael (aka Philippe Leipold)
  *
  */
 public class FunCommands extends AbstractAdminCmdPlugin {
 
 	public static HashMap<Player, BlocksOld> blockStates;
 	public static HashMap<Location, BlocksOld> blockLocStates;
 	public static HashMap<String, Player> players;
 	public static HashSet<Player> onFire;
 	private static ExtendedConfiguration config;
 	public static Logger log;
 
 	/**
 	 * @param name
 	 */
 	public FunCommands() {
 		super("FunCommands");
 	}
 
 	@Override
 	public void registerCmds() {
 		ACPluginManager.registerCommand(Slap.class);
 		ACPluginManager.registerCommand(Rocket.class);
 		ACPluginManager.registerCommand(Entomb.class);
 		ACPluginManager.registerCommand(FCVoid.class);
 		ACPluginManager.registerCommand(Attaint.class);
 		ACPluginManager.registerCommand(Sacrifice.class);
 		ACPluginManager.registerCommand(Barrage.class);
 		ACPluginManager.registerCommand(Wound.class);
 	}
 
 	@Override
 	protected void registerPermParents() {
 		permissionLinker.setMajorPerm(new PermParent("fun.*"));
 	}
 
 	@Override
 	protected void setDefaultLocale() {
 		Utils.addLocale("slapSender", ChatColor.DARK_AQUA + "You have slapped " + ChatColor.GOLD
 				+ "%target" + ChatColor.DARK_AQUA + "!");
 		Utils.addLocale("slapTarget", ChatColor.DARK_AQUA + "You have been slapped by " + ChatColor.GOLD
 				+ "%sender" + ChatColor.DARK_AQUA + "!");
 		Utils.addLocale("slapYourself", ChatColor.DARK_AQUA + "You have slapped " + ChatColor.GOLD
 				+ "yourself" + ChatColor.DARK_AQUA + "!");
 		Utils.addLocale("attaintYourself", ChatColor.DARK_AQUA + "You changed your Displayname to:" + ChatColor.DARK_RED
 				+ " %name");
 		Utils.addLocale("attaintTarget", ChatColor.DARK_AQUA+  "Your Displayname has been changed by" + ChatColor.GOLD
 				+ " %sender" + ChatColor.DARK_AQUA + " to:" + ChatColor.DARK_RED + "%name");
 		Utils.addLocale("attaintSender", ChatColor.DARK_AQUA + "You have changed the Displayname of" + ChatColor.GOLD
 				+ " %target" + ChatColor.DARK_AQUA + " to:" + ChatColor.DARK_RED +" %name");
 		Utils.addLocale("attaintShowName", ChatColor.DARK_AQUA + "The Displayname of " + ChatColor.GOLD + "%name"
 				+ ChatColor.DARK_AQUA + " is:" + ChatColor.DARK_RED	+ " %dname");
 		Utils.addLocale("entombSender", ChatColor.DARK_AQUA + "You have entombed " + ChatColor.GOLD
 				+ "%target" + ChatColor.DARK_AQUA + "!");
 		Utils.addLocale("entombTarget", ChatColor.DARK_AQUA + "You have been entombed by " + ChatColor.GOLD
 				+ "%sender" + ChatColor.DARK_AQUA + "!");
 		Utils.addLocale("entombYourself", ChatColor.DARK_AQUA + "You have entombed yourself!");
 		Utils.addLocale("voidSender", ChatColor.DARK_AQUA + "You have dropped " + ChatColor.GOLD
 				+ "%target" + ChatColor.DARK_AQUA + " into the " + ChatColor.RED +"VOID" + ChatColor.DARK_AQUA + "!");
 		Utils.addLocale("voidTarget", ChatColor.DARK_AQUA + "You have been dropped into the " + ChatColor.RED + "VOID "
 				+ ChatColor.DARK_AQUA + "by " + ChatColor.GOLD + "%sender" + ChatColor.DARK_AQUA + "!");
 		Utils.addLocale("voidYourself", ChatColor.DARK_AQUA + "You have dropped yourself into the " + ChatColor.RED + "VOID"
 				+ ChatColor.DARK_AQUA + "!");
 		Utils.addLocale("rocketTarget", ChatColor.DARK_AQUA + "You have shot " + ChatColor.GOLD
 				+ "%target" + ChatColor.DARK_AQUA + " high into the air!");
 		Utils.addLocale("rocketSender", ChatColor.DARK_AQUA + "You have been shot high into the air by " + ChatColor.GOLD
 				+ "%sender" + ChatColor.DARK_AQUA + "!");
 		Utils.addLocale("rocketYourself", ChatColor.DARK_AQUA + "You have shot yourself into the air!");
 		LocaleManager.getInstance().save();
 		Utils.addLocale("sacrificeTarget", ChatColor.DARK_AQUA + "You have sacrificed " + ChatColor.GOLD
 				+ "%target" + ChatColor.DARK_AQUA + "!");
 		Utils.addLocale("sacrificeSender", ChatColor.DARK_AQUA + "You have been sacrificed by " + ChatColor.GOLD
 				+ "%sender" + ChatColor.DARK_AQUA + "!");
 		Utils.addLocale("sacrificeYourself", ChatColor.DARK_AQUA + "You have sacrificed yourself!");
 		Utils.addLocale("censorTarget", ChatColor.RED + "Watch your language!");
 		Utils.addLocale("censorbroadcast", ChatColor.GOLD + "%player" + ChatColor.DARK_PURPLE
 				+ " just got slayed because he used bad language!");
 		Utils.addLocale("arrowTarget", ChatColor.DARK_AQUA + "You have shot " + ChatColor.GOLD
 				+ "%target" + ChatColor.DARK_AQUA + " to death!");
 		Utils.addLocale("arrowSender", ChatColor.DARK_AQUA + "You have been shot to death by " + ChatColor.GOLD
 				+ "%sender" + ChatColor.DARK_AQUA + "!");
 		Utils.addLocale("arrowYourself", ChatColor.DARK_AQUA + "You have shot yourself to death!");
 		Utils.addLocale("fireTarget", ChatColor.DARK_AQUA + "You have set " + ChatColor.GOLD
 				+ "%target" + ChatColor.DARK_AQUA + " on fire till he dies!");
 		Utils.addLocale("fireSender", ChatColor.DARK_AQUA + "You have been set on by " + ChatColor.GOLD
 				+ "%sender" + ChatColor.DARK_AQUA + " until you die!");
 		Utils.addLocale("fireYourself", ChatColor.DARK_AQUA + "You set yourself on fire until you die!");
 		Utils.addLocale("woundTarget", ChatColor.DARK_AQUA + "You have wounded " + ChatColor.GOLD
 				+ "%target" + ChatColor.DARK_AQUA + "!");
 		Utils.addLocale("woundSender", ChatColor.DARK_AQUA + "You have been wounded by " + ChatColor.GOLD
 				+ "%sender" + ChatColor.DARK_AQUA + "!");
 		Utils.addLocale("woundYourself", ChatColor.DARK_AQUA + "You wounded yourself!");
 		LocaleManager.getInstance().save();
 	}
 
 	@Override
 	public void onEnable() {
 		super.onEnable();
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log = getLogger();
 		config = ExtendedConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
 		FCConfigEnum.setPluginInfos(pdfFile);
 		FCConfigEnum.setPluginConfig(config);
 		config.options().copyDefaults(true).header(FCConfigEnum.getHeader());
 		config.addDefaults(FCConfigEnum.getDefaultvalues());
 		try {
 			config.save();
 		} catch (IOException e) {
 			log.warning("Error while saving the config.yml!");
 		}
 //		config = Configuration.getInstance();
 //		config.setInstance(this);
 		players = new HashMap<String, Player>();
 		blockStates = new HashMap<Player, BlocksOld>();
 		blockLocStates = new HashMap<Location, BlocksOld>();
 		onFire = new HashSet<Player>();
 		final PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(new FCPlayerListener(), this);
 		if (FCConfigEnum.CENSOR.getBoolean()) {
 			loadChatCensorFile();
 			pm.registerEvents(new FCChatListener(), this);
 		}
 		permissionLinker.registerAllPermParent();
 		EggTypeClassLoader.addPackage(this, "de.Lathanael.FC.Eggs");
 		log.info("Enabled. (Version " + pdfFile.getVersion() + ")");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see org.bukkit.plugin.Plugin#onDisable()
 	 */
 	public void onDisable() {
 		for (Map.Entry<String, Player> entry : players.entrySet())
 			ACPlayer.getPlayer(entry.getValue()).setInformation("displayName", entry.getKey());
 
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info("Disabled. (Version " + pdfFile.getVersion() + ")");
 	}
 
 	private void loadChatCensorFile() {
 		File file = new File(getDataFolder().getPath() + File.separator + "CensorStrings.txt");
 		if (!file.exists()) {
 			try {
 				file.createNewFile();
 				InputStream in = getResource("CensorStrings.txt");
 				FileWriter writer = new FileWriter(file);
 				for (int i = 0; (i = in.read()) > 0;) {
 					writer.write(i);
 				}
 				writer.flush();
 				writer.close();
 				in.close();
 				ChatCensorStrings.loadStrings(file);
 			} catch (IOException e) {
 				log.info("Failed to create CensorStrings.txt!");
 				e.printStackTrace();
 			}
 		} else {
 			ChatCensorStrings.loadStrings(file);
 		}
 	}
 
 	public static ExtendedConfiguration getConfigEnum() {
 		return config;
 	}
 }
