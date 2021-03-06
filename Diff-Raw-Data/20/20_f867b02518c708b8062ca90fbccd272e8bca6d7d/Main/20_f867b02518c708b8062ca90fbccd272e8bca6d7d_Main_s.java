 package de.morphyum.cybermorphy;
 
 import java.util.ArrayList;
 
 import org.pircbotx.Channel;
 import org.pircbotx.PircBotX;
 
 public class Main {
 	static ArrayList<PircBotX> bots = new ArrayList<PircBotX>();
 
 	public static void main(String[] args) throws Exception {
 		SysTray tray = new SysTray();
		String[] channels = { "cybermorphy", "morphyum", "dethwing", "artegaomega", "truman", "xpaco5", "mimiheart9", "rush60002" };
 		for (int i = 0; i < channels.length; i++) {
 			if (isChannelWithoutCyber(channels[i]))
 				bots.add(newBot(channels[i]));
 		}
 
 		bots.add(srlIrcBot());
 	}
 
 	public static void announce(String message) throws InterruptedException {
 		for (int i = 0; i < bots.size(); i++) {
 			ArrayList<Channel> channels = new ArrayList<Channel>();
 			for (int h = 0; h < bots.get(i).getChannels().size(); h++) {
 				channels.add(bots.get(i).getChannels().iterator().next());
 			}
 			for (int j = 0; j < channels.size(); j++) {
 				bots.get(i).sendMessage(channels.get(j), message);
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
 
 	public static boolean isChannelWithoutCyber(String channel) {
 		boolean empty = true;
 		for (int i = 0; i < bots.size(); i++) {
			if (bots.get(i).getChannelsNames().contains(channel)) {
 				empty = false;
 				System.out.println("Join error: Cyber already in: " + channel);
 				break;
 			}
 		}
		System.out.println("Joined: " + channel);
 		return empty;
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
