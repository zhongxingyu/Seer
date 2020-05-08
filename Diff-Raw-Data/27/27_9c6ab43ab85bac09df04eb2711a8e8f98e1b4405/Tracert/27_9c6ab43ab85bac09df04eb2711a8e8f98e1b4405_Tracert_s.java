 package me.xir.irc.routeserv.commands;
 
import org.pircbotx.User;
 import org.pircbotx.hooks.ListenerAdapter;
 import org.pircbotx.hooks.events.MessageEvent;
 
 public class Tracert extends ListenerAdapter {
 	
	public void onMessage(MessageEvent event) {
 		if (event.getMessage().startsWith(".tracert")) {
 			String[] arguments = event.getMessage().split(" ");
 			if(arguments.length == 2){
 				String host = "something";
 				
 				event.getBot().sendMessage(event.getChannel(), "--BEGINNING TRACEROUTE TO " + host + " --");
 			}
 		}
 	}
 
 }
