 package com.cronfire;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.Scanner;
 
 import org.apache.commons.lang3.text.WordUtils;
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 import org.apache.log4j.varia.NullAppender;
 
 import com.cronfire.commands.Command;
 import com.cronfire.load_manager.LoadManager;
 import com.cronfire.queue.CronFireQueue;
 
 public class CronFire {
 	static Logger logger = Logger.getLogger("com.cronfire");
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		if(2 != args.length) {
 			System.err.println("Usage: cronfire.jar <config.xml> <hosts.txt>");
 			System.exit(1);
 		}
 		
 		CronFireSettings.loadConfigFile(args[0]);
 		CronFireSettings.loadUrls(args[1]);
 		
 		try {
 			String log_file = CronFireSettings.getSetting("log_file","");
			if(0 == log_file.length()) {
 				logger.addAppender(new NullAppender());
 			} else {
 				logger.addAppender(new FileAppender(new PatternLayout(), log_file));
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 		
 		LoadManager loadManager = LoadManager.getInstance();
 		loadManager.start();
 
 		CronFireQueue queue = CronFireQueue.getInstance();
 		queue.start();
 		
 		// [TODO] Relative start times (e.g. cron.maint=midnight)
 
 		// CLI
 		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 		Scanner scanner = new Scanner(in);
 		String token = "";
 		
 		while(true) {
 			System.out.print("> ");
 			try {
 				token = scanner.next();
 
				if(0 == token.length()) {
 					continue;
 				}
 				
 				@SuppressWarnings("rawtypes")
 				Class commandClass = null;
 				
 				try {
 					commandClass = Class.forName("com.cronfire.commands." + WordUtils.capitalize(token) + "Command");
 					Command command = (Command) commandClass.newInstance();
 					Scanner command_args = new Scanner(scanner.nextLine().trim());
 					command.execute(command_args);
 					command_args.close();
 					
 				} catch(ClassNotFoundException cnfe) {
 					System.out.println("Unknown command: " + token);
 					token = "";
 					scanner.nextLine();
 					continue;
 				}
 				
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 }
