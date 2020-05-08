 package com.xegaming.worldthreadit.commands;
 
 
 import org.bukkit.entity.Player;
 import com.xegaming.worldthreadit.WorldThreadit;
 
 public class BaseCommandExecutor extends BaseExecutor {
     @Override
     protected void run(Player player, String[] args) {
         if(player.isOp()){//TODO add permissions
         	if (args[0].equalsIgnoreCase("set")) {
                ExpandCommand.expand(player, args);
             } else if (args[0].equalsIgnoreCase("replace")) {
                
             } else if (args[0].equalsIgnoreCase("wand")) {
            	
             } else if (args[0].equalsIgnoreCase("expand")) {
            	
             }
         }
     }
 
     public BaseCommandExecutor(WorldThreadit XE) {
         super(XE);
     }
 }
