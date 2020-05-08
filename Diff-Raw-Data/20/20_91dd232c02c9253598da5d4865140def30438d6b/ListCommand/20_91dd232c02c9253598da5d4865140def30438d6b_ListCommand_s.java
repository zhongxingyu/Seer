 package com.cronfire.commands;
 
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.Scanner;
 import java.util.concurrent.TimeUnit;
 
 import com.cronfire.CronFireSettings;
 import com.cronfire.endpoint.EndpointUrl;
 import com.cronfire.queue.CronFireQueue;
 
 public class ListCommand implements Command {
 	CronFireQueue queue = CronFireQueue.getInstance();
 	
 	public void execute(Scanner scanner) {
 		String filter = "";
 		
 		if(scanner.hasNext())
 			filter = scanner.nextLine();
 		
 		// [TODO] sort
 		
 		for(Iterator<Entry<String,EndpointUrl>> i = CronFireSettings.getEndpoints().entrySet().iterator(); i.hasNext(); ) {
 			Entry<String,EndpointUrl> entry = i.next();
 			EndpointUrl endpoint = entry.getValue();
 			
 			String url = endpoint.getUrl();
 			
 			// Strip query args // [TODO] Verbose
 			if(-1 != url.lastIndexOf("?"))
 				url = url.substring(0,url.lastIndexOf("?"));
 			
 			String out = "";
 			
 			out += "* ";
 			
 			if(endpoint.isMissing()) {
 				out += "[404] ";
 			}
 			
 			out += url;
 			
 			if(endpoint.isMissing()) {
 				out += "";
 			} else if(endpoint.isRunning()) {
 				out += " [running] ";
 			} else if(queue.isPaused()) {
 				out += " [paused] ";
 			} else {
 				long secs = endpoint.getDelay(TimeUnit.SECONDS);
 				
 				if(secs > 0) {
 					out += " [" + secs + "s] "; 
 				} else {
 					out += " [waiting] ";
 				}
 			}
 			
 			if(!endpoint.isMissing()) {
 				out += endpoint.getAverageRuntime() + "ms (n=" + endpoint.getRunCount() + ")";
 			}
 			
 			// Filtering?
 			boolean showOutput = true;
			if(!filter.isEmpty()) {
 				// Inverse grep
 				if(filter.startsWith("!") && filter.length() > 1) {
 					if(out.contains(filter.substring(1)))
 						showOutput = false;
 				// Grep
 				} else {
 					if(!out.contains(filter))
 						showOutput = false;
 				}
 			}
 
 			if(showOutput)
 				System.out.println(out);
 		}
 	}
 
 }
