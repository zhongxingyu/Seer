 package com.gravypod.PodBot.commands;
 
 import java.io.File;
 
 import com.gravypod.PodBot.CommandClass;
 import com.gravypod.PodBot.CommandParse;
 import com.gravypod.PodBot.PodBot;
 
 public class delcommand extends CommandClass {
 	
 	public delcommand() {
 		
 		if (isUserOp(CommandParse.channel, CommandParse.sender)) {
 			
			File file = new File("commands" + PodBot.pathSep + CommandParse.command + ".txt");
 			
			if (file.exists())
				file.delete();
 			
			sendResponce(CommandParse.args, CommandParse.command, CommandParse.channel, "The file was command!");
 			
 		}
 		
 	}
 
 }
