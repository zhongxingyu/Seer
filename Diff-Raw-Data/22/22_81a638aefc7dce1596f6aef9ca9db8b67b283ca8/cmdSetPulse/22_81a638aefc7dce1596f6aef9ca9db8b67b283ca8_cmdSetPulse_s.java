 package com.bukkit.gemo.FalseBook.IC.commands;
 
 import com.bukkit.gemo.FalseBook.IC.FalseBookICCore;
 import com.bukkit.gemo.commands.Command;
 import com.grover.mingebag.ic.BaseDataChip;
 
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class cmdSetPulse extends Command {
 
     public cmdSetPulse(String pluginName, String syntax, String arguments, String node) {
         super(pluginName, syntax, arguments, node);
         this.description = "Change the pulse of the IC";
     }
 
     public void execute(String[] args, CommandSender sender) {
     	Integer pulse;
     	try {
         	pulse = Integer.parseInt(args[0]);
     	} catch (Exception e) {
     		return;
     	}
     	
     	if (sender instanceof Player) {
     		Player player = (Player) sender;
 
     		BlockState block = player.getTargetBlock(null, 5).getState();
     		if (block instanceof Sign) {
     			Sign sign = (Sign) block;
         		if (FalseBookICCore.getInstance().getFactory().getIC(sign.getLine(0)) instanceof BaseDataChip) {
         			
         			if (pulse > 600)
         				pulse = 600;
  
         			
        			if (pulse < 1)
        				pulse = 1;
         			
         			sign.setLine(1, pulse.toString());
         			sign.update();
         		}
     		}
     	}
     }
 
 
 }
