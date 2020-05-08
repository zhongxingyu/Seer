 package yt.bam.library;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Scanner;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.mcstats.MetricsLite;
 import yt.bam.library.commands.*;
 import yt.bam.library.modules.CommandModule;
 import yt.bam.library.modules.ConfigurationModule;
 import yt.bam.library.modules.TranslationModule;
 
 /**
  * @author fr34kyn01535
  */
 
 public class BAMLibrary {
     public static BAMLibrary Instance;
     public static JavaPlugin Plugin;
     public ConfigurationModule Configuration = null;
     public CommandModule Command = null;
     public TranslationModule Translation = null;
     
     public Map<String,String> DefaultTranslation;
     public ArrayList<ICommand> Commands;
     public String[] RootCommands;                    
                         
     public BAMLibrary(JavaPlugin plugin, Map<String,String> defaultTranslation, ArrayList<ICommand> commands,String[] rootCommands,boolean enableMetrics, boolean enableUpdateNotification) {
         Instance = this;
         Plugin = plugin;
         DefaultTranslation = defaultTranslation;
         Commands = commands;
         Commands.add(new CmdHelp());
         Commands.add(new CmdAbout());
         RootCommands = rootCommands;
                 
         Configuration = new ConfigurationModule(plugin);
         Translation = new TranslationModule(plugin,Configuration.getString("translation", "en"),DefaultTranslation);
         Command = new CommandModule(plugin,Translation,RootCommands,Commands);
           
         if(enableMetrics && Configuration.getBoolean("enable-metrics", true)){
             try {
                 MetricsLite metrics = new MetricsLite(plugin);
                 metrics.start();
             } catch (IOException e) {
                 // Failed to submit the stats :-(
             }
         }
        if(enableUpdateNotification && Configuration.getBoolean("check-for-updates", false)){
             try{
                 String latestVersion = null;
                 String updateUrl = null;
                         
                Scanner sc = new Scanner(new URL("http://radio.bam.yt/version.php?plugin="+Plugin.getName()).openStream(), "UTF-8");
                latestVersion = sc.useDelimiter("\\A").next();
                updateUrl = sc.useDelimiter("\\A").next();
                 
                 if(latestVersion!=null && !latestVersion.isEmpty() && updateUrl!=null && !updateUrl.isEmpty()){
                    int lVersion = Integer.parseInt(latestVersion.replace(".", "").replace("v", ""));
                    int cVersion = Integer.parseInt(plugin.getDescription().getVersion().replace(".", "").replace("v", ""));
 
                     if(lVersion>cVersion){
                         plugin.getLogger().info("A new version "+latestVersion+" is available!");
                         plugin.getLogger().info("Get it on "+updateUrl);
                     }
                 }
             }catch (Exception e){
                 //Ok... well then not..
             }
         }
     }
 
         
     public void onEnable() {
         Command.onEnable();
         Translation.onEnable();
         Configuration.onEnable();
     }
         
     public void onDisable() {
         Command.onDisable();
         Translation.onDisable();
         Configuration.onDisable();
     }
     
     public boolean onCommand(CommandSender sender, Command root, String commandLabel, String[] args) {
         return Command.onCommand(sender, root,commandLabel,args);
     }
 }
