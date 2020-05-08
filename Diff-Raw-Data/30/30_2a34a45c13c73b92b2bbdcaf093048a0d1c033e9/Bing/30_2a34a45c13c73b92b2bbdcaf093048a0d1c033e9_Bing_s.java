 package net.hexid.hexbot.bots;
 
 import java.util.List;
 import java.util.ArrayList;
 
 public class Bing implements net.hexid.hexbot.bot.Bot {
 	private ArrayList<String> botArgs;
 	public Bing(ArrayList<String> botArgs) {
 		this.botArgs = botArgs;
 	}
 
 	public List<String> getBotExecuteData() {
		if(botArgs.size() <= 0 || botArgs.size() > 4) {
 			System.out.println("botArgs: email [, queryCount [, minDelay [, maxDelay]]]");
 			System.exit(1);
 		} else {
 			botArgs.add("--password="+new String(System.console().readPassword("Password: ")));
 		}
 		return botArgs;
 	}
 	public void processExitCode(int exitCode) {
 		
 	}
 	public String getShortName() {
 		return "Bing";
 	}
 }
