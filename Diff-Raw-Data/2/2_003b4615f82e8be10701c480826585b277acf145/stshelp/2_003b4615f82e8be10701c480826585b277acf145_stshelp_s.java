 package net.othercraft.steelsecurity.commands;
 
 import org.bukkit.command.CommandSender;
 
 public class stshelp {
 
 	public void sts(CommandSender sender, String[] split) {
 		if (split.length==1){
 			p1(sender);
 		}
 		else if (split.length==2){
 			if (split[1]=="1"){
 				p1(sender);
 			}
 		}
 		else {
			
 		}
 	}
 
 	private void p1(CommandSender sender) {
 		sender.sendMessage("/sts: Base Command");
 		
 	}
 
 }
