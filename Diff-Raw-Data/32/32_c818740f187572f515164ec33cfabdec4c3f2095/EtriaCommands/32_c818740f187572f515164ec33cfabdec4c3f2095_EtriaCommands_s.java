 package Main;
 
 import java.io.File;
 import Commands.*;
 import Commands.Trade.TradeCmd;
 import Listeners.EntityListener;
 import Listeners.InventoryListener;
 import Listeners.PlayerListener;
 import Listeners.TradeInventoryListener;
 import Util.CommandHandleException;
 import Util.CommandManager;
 import Util.Utils;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class EtriaCommands extends JavaPlugin {
     
     Config config;
     DBConnection dbc;
     TradeCmd trade;
     
     private CommandManager commands;
 	public static File dataFolder;
 	
 	public static EtriaCommands instance;
 	
 	public MetricsLite ml = null;
 	
     
     // Configuration Load //
     public void loadConfiguration() {
     	if (!new File(getDataFolder() + File.separator + "config.yml").exists())
     		saveDefaultConfig();
     	File file = new File(getDataFolder() + File.separator + "playerdata" + File.separator);
     	boolean exists = file.exists();
     	if (!exists) {
     		try {
     			boolean success = new File(getDataFolder() + File.separator + "playerdata").mkdir();
     			if (success) Utils.log.info("[EtriaCommands] Created playerdata folder.");
     		} catch (Exception e) {
     			Utils.log.severe("[EtriaCommands] Failed to make userdata directory!");
     			Utils.log.severe(e.getMessage());
     		}
     	}
     }
 	
     @Override
     public void onEnable() {
     	
     	instance = this;
         config = new Config(this);
         dbc = new DBConnection();
         commands = new CommandManager();
         
         // Metrics //
         try {
         	ml = new MetricsLite(this);
         	ml.start();
         } catch (Exception ignore) {
         	Utils.log.warning("[EtriaCommands] Metrics could not be started!");
         }
         
         this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
         this.getServer().getPluginManager().registerEvents(new InventoryListener(), this);
         this.getServer().getPluginManager().registerEvents(new EntityListener(), this);
         this.getServer().getPluginManager().registerEvents(new TradeInventoryListener(), this);
         
         commands.registerCommands(new BackCmd().getClass());
         commands.registerCommands(new BackpackCmd().getClass());
         commands.registerCommands(new BanCmd().getClass());
         commands.registerCommands(new CalculatorCmd().getClass());
         commands.registerCommands(new ClearCmd().getClass());
         commands.registerCommands(new CopyinvCmd().getClass());
         commands.registerCommands(new DeletehomeCmd().getClass());
         commands.registerCommands(new DeletewarpCmd().getClass());
         commands.registerCommands(new EnchantCmd().getClass());
         commands.registerCommands(new EnchantingTableCmd().getClass());
         commands.registerCommands(new GamemodeCmd().getClass());
         commands.registerCommands(new GarbageCollectorCmd().getClass());
         commands.registerCommands(new GetposCmd().getClass());
         commands.registerCommands(new GodCmd().getClass());
         commands.registerCommands(new HatCmd().getClass());
         commands.registerCommands(new HealCmd().getClass());
         commands.registerCommands(new HelpCmd().getClass());
         commands.registerCommands(new HomeCmd().getClass());
         commands.registerCommands(new IsbannedCmd().getClass());
         commands.registerCommands(new ItemCmd().getClass());
         commands.registerCommands(new IteminfoCmd().getClass());
         commands.registerCommands(new KickCmd().getClass());
         commands.registerCommands(new ListhomesCmd().getClass());
         commands.registerCommands(new MsgCmd().getClass());
         commands.registerCommands(new MuteCmd().getClass());
         commands.registerCommands(new PingCmd().getClass());
         commands.registerCommands(new RepairCmd().getClass());
         commands.registerCommands(new ReplyCmd().getClass());
         commands.registerCommands(new SethomeCmd().getClass());
         commands.registerCommands(new SetspawnCmd().getClass());
         commands.registerCommands(new SetwarpCmd().getClass());
         commands.registerCommands(new ShootCmd().getClass());
         commands.registerCommands(new SpawnCmd().getClass());
         commands.registerCommands(new SpawnmobCmd().getClass());
         commands.registerCommands(new TimeCmd().getClass());
         commands.registerCommands(new tpCmd().getClass());
         commands.registerCommands(new tpaCmd().getClass());
         commands.registerCommands(new tphereCmd().getClass());
         commands.registerCommands(new TradeCmd().getClass());
         commands.registerCommands(new UnbanCmd().getClass());
         commands.registerCommands(new VanishCmd().getClass());
         commands.registerCommands(new WeatherCmd().getClass());
         commands.registerCommands(new WarpCmd().getClass());
         commands.registerCommands(new WorkbenchCmd().getClass());
     }
     
     public void onDisable() {
         DBConnection.sql.close();
        BackpackCmd.sql.close();
     }
     
     @Override
     public boolean onCommand(CommandSender s, Command c, String l, String[] args) {
         return handleCommand(s, c.getName(), args);
     }
     
     private boolean handleCommand(CommandSender s, String name, String[] args) {
         try {
             return commands.execute(name, s, args);
         } catch (CommandHandleException e) {
             if (e.getMessage().equals("NO_PERMISSION")) {
                 s.sendMessage(Strings.NO_PERMISSION.toString());
             } else if (e.getMessage().equals("NOT_ENOUGH_ARGS")) {
                 s.sendMessage(Strings.NOT_ENOUGH_ARGS.toString());
                 s.sendMessage("e" + Bukkit.getPluginCommand(name).getUsage());
             } else {
                 return false;
             }
             
             return true;
         }
     }
     
     public static EtriaCommands getInstance() {
     	return instance;
     }
     
 }
