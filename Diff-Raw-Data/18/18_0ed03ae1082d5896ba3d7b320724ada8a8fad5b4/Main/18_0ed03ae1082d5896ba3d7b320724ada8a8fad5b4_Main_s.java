 package de.morphyum.cybermorphy;
 
 import java.util.ArrayList;
 
 import org.pircbotx.Channel;
 import org.pircbotx.PircBotX;
 
 public class Main {
 	static ArrayList<PircBotX> bots = new ArrayList<PircBotX>();
 
 	public static void main(String[] args) throws Exception {
 		SysTray tray = new SysTray();
 		bots.add(newBot("cybermorphy"));
 		bots.add(newBot("morphyum"));
 		bots.add(newBot("dethwing"));
 		bots.add(newBot("artegaomega"));
 		bots.add(newBot("truman"));
 		bots.add(newBot("xpaco5"));
 		bots.add(newBot("mimiheart9"));
 
 		bots.add(srlIrcBot());
 	}
 
 	public static void announce(String message) throws InterruptedException {
 		for (int i = 0; i < bots.size(); i++) {
			Channel[] channels = (Channel[]) bots.get(i).getChannels().toArray();
			for (int j = 0; j < channels.length; j++) {
				bots.get(i).sendMessage(channels[j], message);
 				Thread.sleep(3000);
 			}
 		}
 	}
 
 	public static PircBotX srlIrcBot() {
 		PircBotX bot = new PircBotX();
 		bot.getListenerManager().addListener(new SrlCyberMorphy());
 		bot.setName("cybermorphy");
 		bot.setVerbose(true);
 		bot.setAutoReconnect(true);
 		bot.setAutoReconnectChannels(true);
 		try {
 			bot.connect("irc.speedrunslive.com", 6667);
 			Thread.sleep(1000);
 			bot.joinChannel("#smwracers");
 			Thread.sleep(1000);
 		} catch (Exception e) {
 			System.out.println("error while connecting to SRL irc trying irc2");
 			try {
 				bot.connect("irc2.speedrunslive.com", 6667);
 				Thread.sleep(1000);
 				bot.joinChannel("#smwracers");
 				Thread.sleep(1000);
 			} catch (Exception e2) {
 				System.out.println("error while connecting to SRL irc2 no connection to SRL irc established");
 				System.out.println("restart cybermorphy to try again");
 			}
 		}
 		return bot;
 	}
 
 	public static PircBotX newBot(String channel) throws Exception {
 		PircBotX bot = new PircBotX();
 		bot.getListenerManager().addListener(new CyberMorphy());
 		bot.setName("cybermorphy");
 		bot.setVerbose(true);
 		bot.setAutoReconnect(true);
 		bot.setAutoReconnectChannels(true);
 
 		bot.connect("irc.twitch.tv", 6667, "oauth:fbjpmnege3g0aw4ffv802rgkle1q9vo");
 		Thread.sleep(1000);
 
 		bot.joinChannel("#" + channel);
 		Thread.sleep(1000);
 		return bot;
 	}
 }
