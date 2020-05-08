 /**
  * FurnaceExpManager - Package: syam.furnaceexpmanager
  * Created: 2012/12/10 13:28:09
  */
 package syam.furnaceexpmanager;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import syam.furnaceexpmanager.command.BaseCommand;
 import syam.furnaceexpmanager.command.HelpCommand;
 import syam.furnaceexpmanager.command.ReloadCommand;
 import syam.furnaceexpmanager.listener.BlockListener;
 import syam.furnaceexpmanager.util.Metrics;
 
 /**
  * FurnaceExpManager (FurnaceExpManager.java)
 * 
  * @author syam(syamn)
  */
 public class FurnaceExpManager extends JavaPlugin {
     // ** Logger **
     public final static Logger log = Logger.getLogger("Minecraft");
     public final static String logPrefix = "[FurnaceExpManager] ";
     public final static String msgPrefix = "&6[FurnaceExpManager] &f";
 
     // ** Listener **
    BlockListener blockListener = new BlockListener(this);
 
     // ** Commands **
     private List<BaseCommand> commands = new ArrayList<BaseCommand>();
 
     // ** Private Classes **
     private ConfigurationManager config;
 
     // ** Instance **
     private static FurnaceExpManager instance;
 
     /**
      * プラグイン起動処理
      */
     @Override
     public void onEnable() {
         instance = this;
 
         PluginManager pm = getServer().getPluginManager();
         config = new ConfigurationManager(this);
 
         // loadconfig
         try {
             config.loadConfig(true);
         } catch (Exception ex) {
             log.warning(logPrefix
                     + "an error occured while trying to load the config file.");
             ex.printStackTrace();
         }
 
         // プラグインを無効にした場合進まないようにする
         if (!pm.isPluginEnabled(this)) {
             return;
         }
 
         // Regist Listeners
        pm.registerEvents(blockListener, this);
 
         // コマンド登録
         registerCommands();
 
         // メッセージ表示
         PluginDescriptionFile pdfFile = this.getDescription();
         log.info("[" + pdfFile.getName() + "] version " + pdfFile.getVersion()
                 + " is enabled!");
 
         setupMetrics(); // mcstats
     }
 
     /**
      * プラグイン停止処理
      */
     @Override
     public void onDisable() {
         getServer().getScheduler().cancelTasks(this);
 
         // メッセージ表示
         PluginDescriptionFile pdfFile = this.getDescription();
         log.info("[" + pdfFile.getName() + "] version " + pdfFile.getVersion()
                 + " is disabled!");
     }
 
     /**
      * コマンドを登録
      */
     private void registerCommands() {
         // Intro Commands
         commands.add(new HelpCommand());
 
         // Main Commands
 
         // Admin Commands
         commands.add(new ReloadCommand());
     }
 
     /**
      * Metricsセットアップ
      */
     private void setupMetrics() {
         try {
             Metrics metrics = new Metrics(this);
             metrics.start();
         } catch (IOException ex) {
             log.warning(logPrefix + "cant send metrics data!");
             ex.printStackTrace();
         }
     }
 
     /**
      * コマンドが呼ばれた
      */
     @Override
     public boolean onCommand(CommandSender sender, Command cmd,
             String commandLabel, String args[]) {
         if (cmd.getName().equalsIgnoreCase("furnaceexpmanager")) {
             if (args.length == 0) {
                 // 引数ゼロはヘルプ表示
                 args = new String[] { "help" };
             }
             /*
              * else if (args[0].equalsIgnoreCase("gen")){ args[0] = "generate";
              * }
              */
 
             outer: for (BaseCommand command : commands
                     .toArray(new BaseCommand[0])) {
                 String[] cmds = command.getName().split(" ");
                 for (int i = 0; i < cmds.length; i++) {
                     if (i >= args.length || !cmds[i].equalsIgnoreCase(args[i])) {
                         continue outer;
                     }
                     // 実行
                     return command.run(this, sender, args, commandLabel);
                 }
             }
             // 有効コマンドなし デフォルトコマンド実行
             new HelpCommand().run(this, sender, args, commandLabel);
             return true;
         }
         return false;
     }
 
     /**
      * デバッグログ
     * 
      * @param msg
      */
     public void debug(final String msg) {
         if (config.isDebug()) {
             log.info(logPrefix + "[DEBUG]" + msg);
         }
     }
 
     /* getter */
     /**
      * コマンドを返す
     * 
      * @return List<BaseCommand>
      */
     public List<BaseCommand> getCommands() {
         return commands;
     }
 
     /**
      * 設定マネージャを返す
     * 
      * @return ConfigurationManager
      */
     public ConfigurationManager getConfigs() {
         return config;
     }
 
     /**
      * インスタンスを返す
     * 
      * @return FurnaceExpManagerインスタンス
      */
     public static FurnaceExpManager getInstance() {
         return instance;
     }
 }
