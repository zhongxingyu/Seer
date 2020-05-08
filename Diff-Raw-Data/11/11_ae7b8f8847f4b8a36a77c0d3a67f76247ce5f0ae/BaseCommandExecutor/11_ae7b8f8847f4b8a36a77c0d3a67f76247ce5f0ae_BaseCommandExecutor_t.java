 package com.xegaming.worldthreadit.commands;
 
 
 import org.bukkit.entity.Player;
 import com.xegaming.worldthreadit.WorldThreadit;
 
 public class BaseCommandExecutor extends BaseExecutor {
     @Override
     protected void run(Player player, String[] args) {
         if(player.isOp()){//TODO add permissions
         	if (args[0].equalsIgnoreCase("set")) {
                SetCommand.set(player, args);
             } else if (args[0].equalsIgnoreCase("replace")) {
                ReplaceCommand.replace(player, args);
             } else if (args[0].equalsIgnoreCase("wand")) {
                WandCommand.wand(player, args);
             } else if (args[0].equalsIgnoreCase("expand")) {
                ExpandCommand.expand(player, args);
             }
         }
     }
 
     public BaseCommandExecutor(WorldThreadit XE) {
         super(XE);
     }
 }
