 package rules;
 import java.util.Random;
 
 import org.jibble.pircbot.PircBot;
 
 
 public class QuoteRule implements BotRule {
 
 	Random random;
	private static final double THRESHOLD_TO_QUOTE = 0.05;
 	
 	public QuoteRule() {
 		random = new Random();
 	}
 	@Override
 	public boolean processMessage(String channel, String sender, String login, String hostname, String message, PircBot callback) {
 		if (shouldQuote()) {
 			String[] quotes = message.split(" ");
 			String quote = quotes[random.nextInt(quotes.length)];
 			if (quote.length() >= 5) {
 				callback.sendMessage(channel, "\"" + quote + "\"");
 			}
 		}
 		return false;
 	}
 	
 	private boolean shouldQuote() {
 		return random.nextDouble() < THRESHOLD_TO_QUOTE;
 	}
 }
