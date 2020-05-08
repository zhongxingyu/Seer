 package me.bibo38.Register;
 
 import java.io.File;
 import java.util.List;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 public class Register extends JavaPlugin
 {
 	private Logger log;
 	private PluginDescriptionFile pdFile;
 	private static FileConfiguration cfg;
 	
 	private InteractListener myInteractListener;
 	private static Register main;
 	private static boolean useVault = false;
 	private static Permission perm;
 	private static boolean useCfg = true;
 	
 	private boolean setupPermissions()
 	{
 		if(this.getServer().getPluginManager().getPlugin("Vault") == null)
 		{
 			return false;
 		}
 		
 		RegisteredServiceProvider<Permission> rsp = this.getServer().getServicesManager().getRegistration(Permission.class);
 		if(rsp == null)
 		{
 			return false;
 		}
 		
 		perm = rsp.getProvider();
 		if(perm == null)
 		{
 			return false;
 		}
 		
 		return true;
 	}
 	
 	public void onEnable()
 	{
 		log = this.getLogger();
 		main = this;
 		pdFile = this.getDescription();
 		cfg = this.getConfig();
 		cfg.options().copyDefaults(true);
 		this.saveConfig();
 		
 		useCfg = cfg.getBoolean("use-cfg");
 		
 		File updateord = this.getServer().getUpdateFolderFile();
 		if(updateord.exists() && updateord.listFiles().length == 0)
 		{
 			updateord.delete();
 		}
 		
 		useVault = setupPermissions(); // Soll Vault benutzt werden?
 		
 		myInteractListener = new InteractListener();
 		
 		PluginManager pm = this.getServer().getPluginManager();
 		pm.registerEvents(myInteractListener, this);
 		
 		log.info("Register Version " + pdFile.getVersion() + " wurde aktiviert!");
 		log.info(cfg.getString("password"));
 	}
 	
 	public void onDisable()
 	{
 		log.info("Register Version " + pdFile.getVersion() + " wurde deaktiviert!");
 	}
 	
 	public static FileConfiguration getCfg()
 	{
 		return cfg;
 	}
 	
 	public static boolean hasPerm(String player)
	{
 		if(useCfg)
		{
 			main.reloadConfig();
 			List<String> user = cfg.getStringList("registered");
 			if(user.contains(player))
 			{
 				return true;
 			}
 		}
 		
 		if(useVault)
 		{
 			return (perm.has(main.getServer().getPlayer(player), "register.register") == true); // Null kann auch vorkommen
 		} else
 		{
 			return main.getServer().getPlayer(player).hasPermission("register.register");
		}
 	}
 	
 	public boolean onCommand(CommandSender cs, Command cmd, String commandLabel, String[] args)
 	{
 		if(!(cs instanceof Player))
 		{
 			cs.sendMessage(ChatColor.RED + "You can only run this Command as a Player!");
 			return true;
 		}
 		
 		Player player = (Player) cs;
 		
 		if(cmd.getName().equalsIgnoreCase("register") && !hasPerm(player.getName()))
 		{
 			if(args.length == 0 || !args[0].equals(cfg.getString("password")))
 			{
 				player.sendMessage(ChatColor.RED + "Incorrect Password!");
 			} else
 			{
 				String command = cfg.getString("command1") + " " + player.getName() + " " + cfg.getString("command2");
 				this.getServer().dispatchCommand(this.getServer().getConsoleSender(), command);
 				player.sendMessage("Succesful Registered!");
 				player.setHealth(player.getMaxHealth());
 				player.setFoodLevel(20); // Voller Hunger
 				
 				if(useCfg)
 				{
 					this.reloadConfig();
 					List<String> user = cfg.getStringList("registered");
 					user.add(player.getName());
 					cfg.set("registered", user);
 					this.saveConfig();
 					this.reloadConfig();
 				}
 				
 				log.info(player.getName() + " was successful registered!");
 			}
 		}
 		
 		return true;
 	}
 	
 	
 }
