 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.stutiguias.listeners;
 
 import me.stutiguias.mcmmorankup.Mcmmorankup;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author Stutiguias
  */
 public class CommandListener implements CommandExecutor {
     
     public Mcmmorankup plugin;
     
     public CommandListener(Mcmmorankup instance)
     {
         plugin = instance;
     }
 
     @Override
     public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] args) {
         if(!(cs instanceof Player) || args.length == 0)
            return false;
         
          if(args[0].equalsIgnoreCase("check")) {
              Player pl = plugin.getServer().getPlayerExact(cs.getName());
              boolean sucess = plugin.PowerLevel.tryRankUp(pl);
              if(sucess)
              {
                 cs.sendMessage(plugin.MSucess);
              }else{
                 cs.sendMessage(plugin.MFail);
              }
              return true;
          }
          
          return false;
     }
     
 }
