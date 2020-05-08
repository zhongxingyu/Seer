 package me.desht.portablehole;
 
 /*
     This file is part of PortableHole
 
     PortableHole is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     PortableHole is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with PortableHole.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import me.desht.dhutils.Cost;
 import me.desht.dhutils.DHUtilsException;
 import me.desht.dhutils.LogUtils;
 import me.desht.dhutils.MiscUtil;
 import me.desht.dhutils.commands.CommandManager;
 import me.desht.portablehole.commands.GiveCommand;
 import me.desht.portablehole.commands.InfoCommand;
 import me.desht.portablehole.commands.ReloadCommand;
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class PortableHolePlugin extends JavaPlugin {
 
 	private HoleManager holeManager;
 	private CreditManager creditManager;
 	private final CommandManager cmds = new CommandManager(this);
 	private Economy economy;
 	private Permission permission;
 	private final Set<String> validAuthors = new HashSet<String>();
 	private final Set<String> validGroups = new HashSet<String>();
 	
 	private static PortableHolePlugin instance = null;
 	
 	public void onEnable() { 
 
 		LogUtils.init(this);
 		
 		PluginManager pm = this.getServer().getPluginManager();
 
 		pm.registerEvents(new PortableholeEventListener(this), this);
 
 		setupVault(pm);
 		
 		registerCommands();
 		
 		this.getConfig().options().copyDefaults(true);
		this.getConfig().options().header("See http://dev.bukkit.org/server-mods/portablehole/configuration");
 		this.saveConfig();
 		
 		holeManager = new HoleManager();
 		creditManager = new CreditManager(this);
 		creditManager.loadCredits();
 
 		processConfig();
 		
 		instance = this;
 	}
 
 	public void onDisable() {
 		// shut down any open holes, restoring the original blocks
 		for (Hole h : holeManager.getHoles()) {
 			h.close(true);
 		}
 		
 		creditManager.saveCredits();
 		
 		instance = null;
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		try {
 			return cmds.dispatch(sender, command.getName(), args);
 		} catch (DHUtilsException e) {
 			MiscUtil.errorMessage(sender, e.getMessage());
 			return true;
 		}
 	}
 	
 	private void setupVault(PluginManager pm) {
 		Plugin vault =  pm.getPlugin("Vault");
 		if (vault != null && vault instanceof net.milkbowl.vault.Vault) {
 			LogUtils.fine("Loaded Vault v" + vault.getDescription().getVersion());
 			if (!setupEconomy()) {
 				LogUtils.warning("No economy plugin detected - economy cost type not available");
 			} else {
 				Cost.setEconomy(economy);
 			}
 			if (!setupPermission()) {
 				LogUtils.warning("No permission plugin detected - author group checking disabled");
 			}
 			LogUtils.warning("Vault not loaded: no economy support");
 		}
 	}
 
 	private boolean setupEconomy() {
 		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
 		if (economyProvider != null) {
 			economy = economyProvider.getProvider();
 		}
 
 		return (economy != null);
 	}
 	
 	private boolean setupPermission() {
 		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
 		if (permissionProvider != null) {
 			permission = permissionProvider.getProvider();
 		}
 
 		return (permission != null);
 	}
 	
 	public static PortableHolePlugin getInstance() {
 		return instance;
 	}
 
 	public HoleManager getHoleManager() {
 		return holeManager;
 	}
 
 	public CreditManager getCreditManager() {
 		return creditManager;
 	}
 
 	private void registerCommands() {
 		cmds.registerCommand(new InfoCommand());
 		cmds.registerCommand(new ReloadCommand());
 		cmds.registerCommand(new GiveCommand());
 	}
 
 	private void setValidAuthors(List<String> authors) {
 		validAuthors.clear();
 		for (String a : authors) {
 			validAuthors.add(a);
 		}
 	}
 	
 
 	private void setValidGroups(List<String> groups) {
 		validGroups.clear();
 		for (String a : groups) {
 			validGroups.add(a);
 		}
 	}
 	
 	public Set<String> getValidAuthors() {
 		return validAuthors;
 	}
 
 	public Set<String> getValidGroups() {
 		return validGroups;
 	}
 	
 	public void processConfig() {
 		Hole.initMaterials(this);
 		
 		setValidAuthors(getConfig().getStringList("author_validation.players"));
 		setValidGroups(getConfig().getStringList("author_validation.groups"));
 		
 		String level = getConfig().getString("log_level");
 		try {
 			LogUtils.setLogLevel(level);
 		} catch (IllegalArgumentException e) {
 			LogUtils.warning("invalid log level " + level + " - ignored");
 		}
 		
 		getCreditManager().loadCosts();
 	}
 
 	public Permission getPermissionHandler() {
 		return permission;
 	}
 
 	public String getMessage(String key) {
 		return getConfig().getString("messages." + key);
 	}
 }
