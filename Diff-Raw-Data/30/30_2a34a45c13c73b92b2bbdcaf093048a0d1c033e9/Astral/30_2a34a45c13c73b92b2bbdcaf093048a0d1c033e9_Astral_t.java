 package net.hexid.hexbot.bots;
 
 import java.util.List;
 import java.util.ArrayList;
 
 public class Astral implements net.hexid.hexbot.bot.Bot {
 	private ArrayList<String> botArgs;
 	public Astral(ArrayList<String> botArgs) {
 		this.botArgs = botArgs;
 	}
 
 	public List<String> getBotExecuteData() {
		if(botArgs.size() == 1) {
 			botArgs.add("--password="+new String(System.console().readPassword("Password: ")));
		} else if(botArgs.size() != 2) {
			System.out.println("botArgs: username [, password]");
			System.exit(1);
 		}
 		return botArgs;
 	}
 	public void processExitCode(int exitCode) {
 		
 	}
 	public String getShortName() {
 		return "Astral";
 	}
 }
