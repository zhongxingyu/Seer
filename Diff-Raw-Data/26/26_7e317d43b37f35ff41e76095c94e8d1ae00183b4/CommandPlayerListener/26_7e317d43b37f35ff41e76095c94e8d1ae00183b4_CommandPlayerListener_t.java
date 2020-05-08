 package littlegruz.autoruncommands.listeners;
 
 import littlegruz.autoruncommands.CommandMain;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 public class CommandPlayerListener implements Listener{
    private CommandMain plugin;
    
    public CommandPlayerListener(CommandMain instance){
       plugin = instance;
    }
    
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event){
       //Executes the assigned command if one is set or a global command is set
       if(event.getRightClicked() instanceof Player){
          Player player = (Player) event.getRightClicked();
          String command;
          if(plugin.getPlayerClickMap().get("GLOBAL") != null){
             command = plugin.getCommandMap().get(plugin.getPlayerClickMap().get("GLOBAL")).replace("potato", player.getName());
             plugin.getServer().dispatchCommand(event.getPlayer(), command);
          }
          else if(plugin.getPlayerClickMap().get(event.getPlayer().getName()) != null){
             command = plugin.getCommandMap().get(plugin.getPlayerClickMap().get(event.getPlayer().getName())).replace("potato", player.getName());
             if(command.contains("[op]"))
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
             else
                plugin.getServer().dispatchCommand(event.getPlayer(), command);
          }
       }
       /*This code is for quick testing
       else{
          String command;
          if(plugin.getPlayerClickMap().get("GLOBAL") != null){
             command = plugin.getCommandMap().get(plugin.getPlayerClickMap().get("GLOBAL")).replace("potato", event.getRightClicked().toString());
             plugin.getServer().dispatchCommand(event.getPlayer(), command);
          }
          else if(plugin.getPlayerClickMap().get(event.getPlayer().getName()) != null){
             command = plugin.getCommandMap().get(plugin.getPlayerClickMap().get("Little_Gruz")).replace("potato", "Little_Gruz");
             if(command.contains("[op]"))
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
             else
                plugin.getServer().dispatchCommand(event.getPlayer(), command);
          }
       }*/
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
       //Check if the block should have a command associated
       if(plugin.isPlaceBlock()){
          //If it is air, then a fist was used when right clicking
          if(event.getMaterial().compareTo(Material.AIR) == 0
                  && event.getAction().compareTo(Action.RIGHT_CLICK_BLOCK) == 0){
             event.getPlayer().sendMessage("Command set to block");
             plugin.getBlockCommandMap().put(event.getClickedBlock().getLocation(), plugin.getBlockCommand());
             plugin.setPlaceBlock(false);
          }
       }
       else{
          try{
             if(event.getClickedBlock().getType().compareTo(Material.STONE_BUTTON) == 0
                   && plugin.getBlockCommandMap().get(event.getClickedBlock().getLocation()) != null){
                String command;
                command = plugin.getBlockCommandMap().get(event.getClickedBlock().getLocation());
                if(command.contains("[op]"))
                   plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), plugin.getCommandMap().get(command).replace("potato", event.getPlayer().getName()));
                else
                   plugin.getServer().dispatchCommand(event.getPlayer(), plugin.getCommandMap().get(command).replace("potato", event.getPlayer().getName()));
             }
          }catch(Exception e){}
       }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
       Location loc;
       loc = event.getPlayer().getLocation().getBlock().getLocation();
       loc.setY(loc.getY() - 1);
       
       if(!loc.equals(plugin.getPlayerPosMap().get(event.getPlayer().getName()))){
          //If the player is above a command block then execute the command
          if(plugin.getBlockCommandMap().get(loc) != null
                && loc.getBlock().getType().compareTo(Material.STONE_BUTTON) != 0){
             String command;
             command = plugin.getBlockCommandMap().get(loc);
             plugin.getPlayerPosMap().put(event.getPlayer().getName(), loc);
             if(command.contains("[op]"))
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), plugin.getCommandMap().get(command).replace("potato", event.getPlayer().getName()));
             else
                plugin.getServer().dispatchCommand(event.getPlayer(), plugin.getCommandMap().get(command).replace("potato", event.getPlayer().getName()));
          }
          else
             plugin.getPlayerPosMap().put(event.getPlayer().getName(), null);
       }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
       String command;
       
       plugin.getPlayerPosMap().put(event.getPlayer().getName(), null);
       
       if(plugin.getPlayerJoinCommand().compareTo("chuckTesta") != 0){
          command = plugin.getCommandMap().get(plugin.getPlayerJoinCommand()).replace("potato", event.getPlayer().getName());
          plugin.getServer().dispatchCommand(event.getPlayer(), command);
       }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
       plugin.getPlayerPosMap().remove(event.getPlayer().getName());
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event){
      final String command, playerName;
      
      playerName = event.getPlayer().getName();
       
       if(plugin.getPlayerRespawnMap().get("GLOBAL") != null){
         command = plugin.getCommandMap().get(plugin.getPlayerRespawnMap().get("GLOBAL")).replace("potato", playerName);
          plugin.getServer().dispatchCommand(event.getPlayer(), command);
       }
      else if(plugin.getPlayerRespawnMap().get(playerName) != null){
         command = plugin.getCommandMap().get(plugin.getPlayerRespawnMap().get(playerName)).replace("potato", playerName);
         
         plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){

            public void run(){
               if(command.contains("[op]"))
                  plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
               else
                  plugin.getServer().dispatchCommand(plugin.getServer().getPlayer(playerName), command);
            }
         }, 20L);
       }
    }
 }
