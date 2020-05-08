 package littlegruz.cowsay.listeners;
 
 import littlegruz.cowsay.MCCowsay;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 public class CowPlayerListener implements Listener {
    private MCCowsay plugin;
    
    public CowPlayerListener(MCCowsay instance){
       plugin = instance;
    }
 
    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
      if(event.getMessage().contains("cowsay ")){
          if(plugin.getCowMap().get(event.getPlayer().getName()).compareTo("T") == 0){
             final String name;
             name = event.getPlayer().getName();
             plugin.getCowMap().put(name, "F");
             
             plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
 
                public void run(){
                   if(plugin.getCooldownTime() != 0)
                      plugin.getServer().getPlayer(name).sendMessage("Your cowsay cooldown is over");
                   plugin.getCowMap().put(name, "T");
                }
             }, plugin.getCooldownTime());
          }
          else{
             event.getPlayer().sendMessage("Please wait until the " +
                   plugin.getCooldownTime() / 20 + " second cooldown is over");
             event.setCancelled(true);
          }
       }
    }
 
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
       plugin.getCowMap().put(event.getPlayer().getName(), "T");
    }
 
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
       plugin.getCowMap().remove(event.getPlayer().getName());
    }
 }
