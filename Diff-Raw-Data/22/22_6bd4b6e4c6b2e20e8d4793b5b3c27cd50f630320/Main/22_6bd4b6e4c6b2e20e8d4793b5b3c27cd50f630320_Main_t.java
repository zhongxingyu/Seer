 package de.morphyum.cybermorphy;
 
 import org.pircbotx.PircBotX;
 
 public class Main {
 	public static void main(String[] args) throws Exception {
 		SysTray tray = new SysTray();
 		PircBotX bot = new PircBotX();
 		bot.getListenerManager().addListener(new CyberMorphy());
 		bot.setName("cybermorphy");
 		bot.setVerbose(true);
 		bot.setAutoReconnect(true);
 		bot.setAutoReconnectChannels(true);
 
 		bot.connect("irc.twitch.tv", 6667, "oauth:fbjpmnege3g0aw4ffv802rgkle1q9vo");
 		Thread.sleep(1000);
 
 		bot.joinChannel("#cybermorphy");
 		Thread.sleep(1000);
 
 		bot.joinChannel("#morphyum");
 		Thread.sleep(1000);
 		
 		bot.joinChannel("#dethwing");
 		Thread.sleep(1000);
 
 		bot.joinChannel("#artegaomega");
 		Thread.sleep(1000);
 
		bot.joinChannel("#truman");
		Thread.sleep(1000);
		
		bot.joinChannel("#xpaco5");
		Thread.sleep(1000);
		
 		bot.joinChannel("#mimiheart9");
 		Thread.sleep(1000);
 		
 		PircBotX bot2 = new PircBotX();
 		bot2.getListenerManager().addListener(new CyberMorphy());
 		bot2.setName("cybermorphy");
 		bot2.setVerbose(true);
 		bot2.setAutoReconnect(true);
 		bot2.setAutoReconnectChannels(true);
 
 		bot2.connect("irc.speedrunslive.com", 6667);
 		Thread.sleep(1000);
 
 		bot2.joinChannel("#smwracers");
 		Thread.sleep(1000);
 		
 		
 	}
 }
