 package littlegruz.autoruncommands.listeners;
 
 import java.util.Iterator;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.Map.Entry;
 
 import littlegruz.autoruncommands.CommandMain;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.server.PluginEnableEvent;
 
 public class CommandServerListener implements Listener {
    private CommandMain plugin;
    
    public CommandServerListener(CommandMain instance){
       plugin = instance;
    }
    
    @EventHandler
    public void onPluginEnable(PluginEnableEvent event){
       // Run the startup tasks one second after everything has loaded
       plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 
          public void run() {
             StringTokenizer st = new StringTokenizer(plugin.getStartupCommands(), ":");
             if(!plugin.isStartupDone()){
                while(st.countTokens() > 0)
                   plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), plugin.getCommandMap().get(st.nextToken()));
                plugin.setStartupDone(true);
             }
          }
      }, 20L);
       
       /* Start the repeating tasks one and a half seconds after everything has
        * loaded.
        * I heard you like scheduled tasks, so I put a scheduled task in your
        * scheduled task */
       plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 
          public void run() {
             int id, interval;
             Iterator<Map.Entry<String, Integer>> it = plugin.getRepeatMap().entrySet().iterator();
             
             // The running of the tasks
             while(it.hasNext()){
                Entry<String, Integer> mp = it.next();
                final String command = mp.getKey();
                interval = mp.getValue();
                
                if((plugin.getCommandMap().get(command) != null
                      || plugin.getCommandMap().get(command + "[op]") != null)
                      && plugin.getRunningRepeatMap().get(command) == null){
                   id = plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin,  new Runnable() {
 
                      public void run() {
                        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
                      }
                   }, interval * 20, interval * 20);
                   
                   plugin.getRunningRepeatMap().put(command, id);
                }
             }
          }
      }, 30L);
    }
 }
