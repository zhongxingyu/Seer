 package com.turt2live.antishare;
 
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.feildmaster.lib.configuration.PluginWrapper;
 import com.turt2live.antishare.SQL.SQLManager;
 import com.turt2live.antishare.storage.VirtualStorage;
 
 public class AntiShare extends PluginWrapper {
 
 	private ASConfig config;
 	public static Logger log = Logger.getLogger("Minecraft");
 	private SQLManager sql;
 	public VirtualStorage storage;
 
 	public ASConfig config(){
 		return config;
 	}
 
 	public SQLManager getSQLManager(){
 		return sql;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args){
 		if(cmd.equalsIgnoreCase("antishare") ||
 				cmd.equalsIgnoreCase("as") ||
 				cmd.equalsIgnoreCase("antis") ||
 				cmd.equalsIgnoreCase("ashare")){
 			reloadConfig();
 			log.info("AntiShare Reloaded.");
 			if(sender instanceof Player){
 				ASUtils.sendToPlayer(sender, ChatColor.GREEN + "AntiShare Reloaded.");
 			}
 			new Thread(new Runnable(){
 				@Override
 				public void run(){
 					ASMultiWorld.detectWorlds((AntiShare) Bukkit.getServer().getPluginManager().getPlugin("AntiShare"));
 				}
 			});
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void onDisable(){
 		log.info("[" + getDescription().getFullName() + "] Saving virtual storage to disk/SQL");
 		if(sql != null){
 			sql.disconnect();
 		}
 		log.info("[" + getDescription().getFullName() + "] Disabled! (turt2live)");
 	}
 
 	@Override
 	public void onEnable(){
 		config = new ASConfig(this);
 		config.create();
 		config.reload();
 		ASInventory.cleanup();
 		getServer().getPluginManager().registerEvents(new AntiShareListener(this), this);
 		ASMultiWorld.detectWorlds(this);
 		storage = new VirtualStorage(this);
 		if(getConfig().getBoolean("SQL.use")){
 			sql = new SQLManager(this);
 			if(sql.attemptConnectFromConfig()){
 				sql.checkValues();
 			}
 		}
 		log.info("[" + getDescription().getFullName() + "] Enabled! (turt2live)");
 	}
 }
