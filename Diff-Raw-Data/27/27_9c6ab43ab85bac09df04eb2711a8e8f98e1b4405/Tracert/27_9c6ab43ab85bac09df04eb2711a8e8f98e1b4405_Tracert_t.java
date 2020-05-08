 package me.xir.irc.routeserv.commands;
 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

 import org.pircbotx.hooks.ListenerAdapter;
 import org.pircbotx.hooks.events.MessageEvent;
 
@SuppressWarnings("rawtypes")
 public class Tracert extends ListenerAdapter {
	Runtime rt = Runtime.getRuntime();
	Process p;
	InputStream out;
	BufferedReader reader;
	String s = null;
 	
	public void onMessage(MessageEvent event) throws IOException {
 		if (event.getMessage().startsWith(".tracert")) {
 			String[] arguments = event.getMessage().split(" ");
 			if(arguments.length == 2){
 				String host = "something";
				p = rt.exec("traceroute " + host);
 				
 				event.getBot().sendMessage(event.getChannel(), "--BEGINNING TRACEROUTE TO " + host + " --");
				
				out = p.getInputStream();
				reader = new BufferedReader(new InputStreamReader(out));
				
				while ((s = reader.readLine()) != null) {
					event.getBot().sendMessage(event.getChannel(), s);
				}
 			}
 		}
 	}
 
 }
