 package net.syamn.sakuracmd.worker;
 
 import net.syamn.sakuracmd.SCHelper;
 import net.syamn.sakuracmd.SakuraCmd;
 import net.syamn.sakuracmd.exception.SakuraCmdException;
 import net.syamn.sakuracmd.player.PlayerManager;
 import net.syamn.sakuracmd.storage.I18n;
 import net.syamn.utils.LogUtil;
 import net.syamn.utils.Util;
 import net.syamn.utils.file.TextFileHandler;
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import static net.syamn.sakuracmd.storage.I18n._;
 
 /**
  * workspace - Package: net.syamn.sakuracmd.worker
  * User: syam
  * Date: 13/03/10 1:53
  */
 public class AnnounceWorker {
     private static AnnounceWorker instance = new AnnounceWorker();
     
     private SakuraCmd plugin;
     private int taskID = -1;
     private String filePath;
 
     private AnnounceWorker(){
         this.plugin = SakuraCmd.getInstance();
         this.lock = new AtomicBoolean(false);
         
         this.onEnable();
     }
 
     public static AnnounceWorker getInstance(){
         if (instance == null){
             synchronized (AFKWorker.class) {
                 if (instance == null){
                     instance = new AnnounceWorker();
                 }
             }
         }
         return instance;
     }
     public static void dispose(){
         if (instance != null){
             instance.onDisable();
         }
         
         instance = null;
     }
     
     private void onEnable(){
         if (plugin.getWorker().getConfig().getAnnounceEnabled()){
             enableTask(null);
         }
     }
     private void onDisable(){
         if (isRunning()){
             disableTask(null);
         }
         
         this.lock = null;
         this.plugin = null;
     }
     
     public void onConfigReload(){
         if (isRunning()){
             disableTask(null);
         }
         
         if (plugin.getWorker().getConfig().getAnnounceEnabled()){
             enableTask(null);
         }
     }
     
     public boolean setSchedule(final boolean enable, final CommandSender sender){
         if (enable){
             return enableTask(sender);
         }else{
             return disableTask(sender);
         }
     }
     
     private boolean enableTask(CommandSender sender){
        if (taskID != -1){
             if (sender != null) Util.message(sender, "&cScheduler already running.");
             LogUtil.warning("Announce scheduler already running.");
             return true;
         }else{
             this.filePath = plugin.getWorker().getConfig().getAnnounceFilePath();
             
             final int mins = plugin.getWorker().getConfig().getAnnounceIntervalMins();
             final long ticks = 20 * 60 * mins;
             // use async task
             taskID = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new AnnounceTask(plugin), ticks, ticks).getTaskId();
             if (taskID == -1){
                 if (sender != null) Util.message(sender, "&cScheduling failed!");
                 LogUtil.warning("Announcetask scheduling failed");
                 return false;
             }else{
                 if (sender != null) Util.message(sender, "&aScheduled every " + mins + " minutes!");
                 LogUtil.info("Scheduled announce every " + mins + " minutes!");
                 return true;
             }
         }
     }
     
     private boolean disableTask(CommandSender sender){
         if (taskID == -1){
             if (sender != null) Util.message(sender, "&cAnnounce task not running!");
             LogUtil.warning("Announce task not running!");
         }else{
             Bukkit.getServer().getScheduler().cancelTask(taskID);
             if (sender != null) Util.message(sender, "&aAnnounce task finished!");
             LogUtil.info("Announce task finished!");
         }
         taskID = -1;
         return true;
     }
     
     public boolean isRunning(){
         return (taskID != -1);
     }
 
     private AtomicBoolean lock;
     private int nextLine = 0;
     
     private class AnnounceTask implements Runnable{
         private SakuraCmd plugin;
         private AnnounceTask(SakuraCmd plugin){
             this.plugin = plugin;
         }
         
         @Override
         public void run(){
             // call here on async thread
             if (!lock.compareAndSet(false, true)){
                 LogUtil.warning("Running announce task already");
                 return;
             }
             
             // do announce
             try{
                 List<String> lines = getLines();
                 if (lines.isEmpty()){
                    //throw new SakuraCmdException("Empty announce file");
                    return; // do nothing
                 }
                 
                if (lines.size() <= nextLine){
                     nextLine = 0;
                 }
                 
                 announce(lines.get(nextLine));
                 nextLine++;
             }catch (SakuraCmdException ex){
                 LogUtil.warning(ex.getMessage() + ", announce aborted");
             }catch (IOException ex){
                 ex.printStackTrace();
             }finally{
                 lock.set(false);
             }
         }
         
         private void announce(final String text){
            final String[] lines = text.split("\\\\n");
             
             Bukkit.getServer().getScheduler().runTaskLater(plugin, new Runnable(){
                 @Override public void run(){
                     for (final String line : lines){
                         Util.broadcastMessage(line);
                     }
                 }
             }, 1L);
         }
         
         private List<String> getLines() throws IOException, SakuraCmdException{
             File file = new File(filePath);
             if (!file.exists()){
                 throw new SakuraCmdException("Announce file '" + file.getPath() + "' not exist");
             }
             if (!file.canRead()){
                 throw new SakuraCmdException("Cannot read announce file '" + file.getPath() + "'");
             }
 
             TextFileHandler handler = new TextFileHandler(file);
             List<String> lines = new ArrayList<>();
             
             for (String line : handler.readLines()){
                 line = line.trim();
                 if (line.length() > 0){
                     lines.add(line);
                 }
             }
             
             return lines;
         }
     }
 }
