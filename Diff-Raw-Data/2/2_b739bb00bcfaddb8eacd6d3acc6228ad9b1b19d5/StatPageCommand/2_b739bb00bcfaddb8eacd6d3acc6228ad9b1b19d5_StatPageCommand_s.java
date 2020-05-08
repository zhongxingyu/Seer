 package com.tehbeard.beardstat.commands;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 
 import com.tehbeard.beardstat.BeardStat;
 import com.tehbeard.beardstat.BeardStatRuntimeException;
 import com.tehbeard.beardstat.containers.EntityStatBlob;
 import com.tehbeard.beardstat.dataproviders.IStatDataProvider;
 import com.tehbeard.beardstat.dataproviders.ProviderQuery;
 import com.tehbeard.beardstat.manager.EntityStatManager;
 import com.tehbeard.beardstat.utils.LanguagePack;
 
 /**
  * Display a statpage
  * 
  * @author James
  * TODO - FIX
  */
 public class StatPageCommand extends BeardStatCommand {
     
     private final Map<String,StatPage> pages = new HashMap<String, StatPageCommand.StatPage>();
     
     public class StatPage {
         private List<StatPageEntry> entries = new ArrayList<StatPageEntry>();
         public class StatPageEntry{
             public final String label;
             public final String domain;
             public final String world;
             public final String category;
             public final String statistic;
             
             StatPageEntry(String line){
                 String[] p = line.split("\\:");
                 if (p.length != 2) {
                     throw new IllegalArgumentException("Could parse line, invalid number of ':' found.");
                 }
                 this.label = p[0];
                 
                 Stack<String> parts = new Stack<String>();
                 for(String s : p[1].split("\\.")){
                     parts.push(s);
                 }
                 
                 this.statistic = parts.pop();
                 this.category = parts.pop();
                 //optional arguments, defaults to default.*.
                 this.world  = parts.isEmpty() ? "*" : parts.pop();
                 this.domain = parts.isEmpty() ? BeardStat.DEFAULT_DOMAIN : parts.pop();
             }
             
             public void toCommandSender(CommandSender sender, EntityStatBlob blob){
                 int value = blob.getStats(domain, world, category, statistic).getValue();
                 //TODO - Format the stat, need read only mode for metadata from database.
                 sender.sendMessage(LanguagePack.getMsg("command.stat.stat",label, playerStatManager.formatStat(statistic, value)));
                 //;
                 
             }
         }
         
         public StatPage(List<String> lines){
             for(String line : lines){
                 entries.add(new StatPageEntry(line));
             }
         }
         
         public void toCommandSender(CommandSender sender, EntityStatBlob blob){
             for(StatPageEntry entry : entries){
                 entry.toCommandSender(sender, blob);
             }
         }
     }
 
     public StatPageCommand(EntityStatManager statManager, BeardStat plugin) {
         super(statManager, plugin);
 
 
        ConfigurationSection pageConfig = plugin.getConfig().getConfigurationSection("stats.pages");
         if (pageConfig != null) {
             Set<String> pageNames = pageConfig.getKeys(false);
             for (String pageName : pageNames) {
                 pages.put(pageName, new StatPage(pageConfig.getStringList(pageName)));
             }
         }
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
         try {
             EntityStatBlob blob = null;
             
             //Find the player to use
             if(sender instanceof Player){
                 blob = playerStatManager.getBlobForPlayer((Player)sender);
             }
             if(blob == null && args.length != 2){
                 return false;
             }
             
             String page = null;
             if(args.length == 2){
                 blob = playerStatManager.getBlob(new ProviderQuery(args[0], IStatDataProvider.PLAYER_TYPE, null, false));
                 page = args[1];
             }
             
             if(args.length == 1){
                 page = args[0];
             }
             
             if(page == null){
                 return false;
             }
             StatPage pageFile = pages.get(page);
             if(pageFile ==null){return false;}
             pageFile.toCommandSender(sender, blob);
             return true;
         } catch (Exception e) {
             this.plugin.handleError(new BeardStatRuntimeException("/statpage threw an error", e, true));
         }
         return true;
     }
 
 }
