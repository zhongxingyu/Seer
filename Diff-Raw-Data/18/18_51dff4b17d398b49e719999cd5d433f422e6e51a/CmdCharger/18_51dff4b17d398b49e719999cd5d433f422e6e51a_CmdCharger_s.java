 package me.jacklin213.cmdcharger;
 
 import java.io.File;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.chat.Chat;
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class CmdCharger extends JavaPlugin {
 
 	public static CmdCharger plugin;
 	PluginDescriptionFile pdfFile;
 	private static final Logger log = Logger.getLogger("Minecraft");
     public static Economy econ = null;
     public static Permission perms = null;
     public static Chat chat = null;
 
     @Override
     public void onDisable() {
         log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
     }
 
     @Override
     public void onEnable() {
         if (!setupEconomy() ) {
            log.info(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
             getServer().getPluginManager().disablePlugin(this);
             return;
         }
         setupPermissions();
         setupChat();
         createconfig();
         log.info(String.format("[%s] Enabled Version %s", getDescription().getName(), getDescription().getVersion()));
     }
 
 
 	public void createconfig() {
 		// Creates config.yml
 		File file = new File(getDataFolder() + File.separator + "config.yml");
 		// If config.yml doesnt exit
 		if (!file.exists()) {
 			// Tells console its creating a config.yml
 			this.getLogger().info("You don't have a config file!!!");
 			this.getLogger().info("Generating config.yml.....");
 			String nomoneymsg = "Sorry! You need %price% to run %cmd%";
 			this.getConfig().set("No-Money-Msg", nomoneymsg);
 			this.getConfig().set("Prices.Commands.ExampleCommand", "# Put your command name where ExmapleCommand is ");
 			int ecp = 1; //examplecommandprice
 			this.getConfig().set("Prices.Commands.ExampleCommand.Price", ecp );
 			this.getConfig().options().copyDefaults(true);
 			this.saveDefaultConfig();
 			this.getLogger().info("Config.yml generated");
 		}
 
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd,
 			String commandLabel, String[] args) {
 		if (commandLabel.equalsIgnoreCase("cmdcharge") || commandLabel.equalsIgnoreCase("cc")){
			if (args.length > 1){
 				if (args[0].equalsIgnoreCase("setprice") || commandLabel.equalsIgnoreCase("sp")){
 					String command = args[1];
 					try {
                         int price = Integer.parseInt(args[2]);
                         this.getConfig().set("Prices.Commands."+ command + ".Price" , price);
             			this.saveConfig();
             			sender.sendMessage("The price for " + command + "has been set to:" + price);
                     } catch (NumberFormatException e) {
                         sender.sendMessage(ChatColor.RED + "This is not a number! Try typing a number in");
                     }
 					return true;
 				}
			}else if(args.length < 4){
 				sender.sendMessage("Not enough arguments !");
 				return true;
			}
 			sender.sendMessage("test");
 		}
 
 		return false;
 	}
 
 	// Vault api import crap
 	private boolean setupEconomy() {
         if (getServer().getPluginManager().getPlugin("Vault") == null) {
             return false;
         }
         RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
         if (rsp == null) {
             return false;
         }
         econ = rsp.getProvider();
         return econ != null;
     }
 
     private boolean setupChat() {
         RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
         chat = rsp.getProvider();
         return chat != null;
     }
 
     private boolean setupPermissions() {
         RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
         perms = rsp.getProvider();
         return perms != null;
     }
 
 }
