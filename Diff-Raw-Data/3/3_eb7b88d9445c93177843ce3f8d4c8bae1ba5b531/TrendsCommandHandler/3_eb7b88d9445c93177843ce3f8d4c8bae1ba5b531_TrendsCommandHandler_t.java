 package com.drtshock.willie.command.fun;
 
 import com.drtshock.willie.Willie;
 import com.drtshock.willie.command.CommandHandler;
 import org.pircbotx.Channel;
 import org.pircbotx.Colors;
 import org.pircbotx.User;
 import twitter4j.Trend;
 import twitter4j.Trends;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.conf.ConfigurationBuilder;
 
 public class TrendsCommandHandler implements CommandHandler {
 
     @Override
     public void handle(Willie bot, Channel channel, User sender, String[] args) {
     	ConfigurationBuilder cb = new ConfigurationBuilder();
         cb.setDebugEnabled(true)
                 .setOAuthConsumerKey(bot.getConfig().getTwitterConsumerKey())
                 .setOAuthConsumerSecret(bot.getConfig().getTwitterConsumerKeySecret())
                 .setOAuthAccessToken(bot.getConfig().getTwitterAccessToken())
                 .setOAuthAccessTokenSecret(bot.getConfig().getTwitterAccessTokenSecret());
         TwitterFactory tf = new TwitterFactory(cb.build());
         Twitter twitter = tf.getInstance();
 		
 		try {
 			Trends trend = twitter.getPlaceTrends(1);
 			int trendsToShow = 5;
 			
 			channel.sendMessage(Colors.CYAN + "Top " + trendsToShow + " trends on Twitter right now...");
 			
 			for (Trend t : trend.getTrends()) {
 				if (trendsToShow > 0) {
 					channel.sendMessage(t.getName());
 				}
 				
 				trendsToShow =- 1;
 			}
 		} catch (TwitterException e) {
 			e.printStackTrace();
 		}
     }
}
