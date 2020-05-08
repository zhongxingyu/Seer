 package net.GUIpsp.GuiBot;
 
 import java.lang.reflect.*;
 import java.util.*;
 import java.util.regex.*;
 
 import org.jibble.pircbot.*;
 
 public class GuiBot extends PircBot {
 	public static String pref = "$";
 
 	public GuiBot() {
		this.setName(Main.nick);
 	}
 
 	public void onMessage(String channel, String sender, String login,
 			String hostname, String message) {
 		if (message.trim().startsWith(pref)) {
 			message = message.replaceFirst(Matcher.quoteReplacement(pref), "");
 			String[] parts = message.split(" ");
 			String first = parts[0];
 			String result = "";
 			for (int i = 1; i < parts.length; i++) {
 				if (result.length() != 0)
 					result += " ";
 				result += parts[i];
 			}
 			if ((first.equalsIgnoreCase(("exit")) || message
 					.equalsIgnoreCase("quit"))
 					&& sender.equalsIgnoreCase("guipsp")) {
 				System.exit(0);
 			} else if (first.equalsIgnoreCase("reload")) {
 				try {
 					System.out.println("Reloading...");
 					Main.reLoad();
 					System.out.println("Reloaded");
 					Main.bot.sendMessage(channel, "Plugins reloaded.");
 				} catch (Throwable e) {
 					e.printStackTrace();
 				}
 			} else if (first.equalsIgnoreCase("help")) {
 				String toprint = "";
 				if (Main.helpmap.containsKey(result)) {
 					toprint=Main.helpmap.get(result);
 				} else {
 					for (Map.Entry<String, String> entry : Main.helpmap
 							.entrySet()) {
						toprint += entry.getKey()+" ";
 					}
 				}
				Main.bot.sendMessage(channel, toprint.trim());
 			} else if (Main.cmdmap.containsKey(first)) {
 				try {
 					Method met = (Method) Main.cmdmap.get(first);
 
 					met.invoke((BasePlugin) Main.classmap.get(first), channel,
 							sender, login, hostname, result);
 
 				} catch (Throwable e) {
 					e.printStackTrace();
 				}
 
 			}
 		}
 	}
 }
