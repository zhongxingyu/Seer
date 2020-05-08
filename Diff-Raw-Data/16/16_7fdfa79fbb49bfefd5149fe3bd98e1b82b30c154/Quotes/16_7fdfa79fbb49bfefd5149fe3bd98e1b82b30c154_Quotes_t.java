 package org.snack.irc.handler.message;
 
 import org.pircbotx.hooks.events.MessageEvent;
 import org.snack.irc.database.DatabaseManager;
 import org.snack.irc.main.Monitor;
 import org.snack.irc.main.TriggerHandler;
 import org.snack.irc.model.Chan;
 import org.snack.irc.model.Quote;
 import org.snack.irc.settings.Config;
 
 public class Quotes extends TriggerHandler {
 
 	private MessageEvent<?> event;
 
 	public Quotes() {}
 
 	public Quotes(MessageEvent<?> event) {
 		this.event = event;
 	}
 
 	@Override
 	public void run() {
 		getQuote();
 	}
 
 	@Override
 	public boolean trigger(MessageEvent<?> event) {
		return (Config.sett_str.get("IDENTIFIERS").contains(event.getMessage().substring(0, 1)) && (event.getMessage().length() == 6 && event.getMessage().substring(1, 6).equals("quote")) || (event.getMessage().length() > 7 && event.getMessage().split(" ").length >= 1));
 	}
 
 	@Override
 	public boolean permission(Chan chan) {
 		if (chan.functions.containsKey("quote")) {
 			return chan.functions.get("quote");
 		} else {
 			return true;
 		}
 	}
 
 	@Override
 	public void attachEvent(MessageEvent<?> event) {
 		this.event = event;
 	}
 
 	/**
 	 * Gets a quote form the database and returns it, based on the command
 	 * length it'll do a name or random search
 	 */
 	private void getQuote() {
 		DatabaseManager db = DatabaseManager.getInstance();
 
 		String response = "";
 		Quote quote;
 
 		if (event.getMessage().length() == 6) {
 			quote = db.getRandomQuote(event.getChannel().getName());
 			response = Config.speech.get("QU_SUC").replace("<name>", quote.getName()).replace("<quote>", quote.getMessage());
 		} else {
 			String user = event.getMessage().split(" ")[1];
 			quote = db.getQuoteByName(event.getChannel().getName(), user);
 			if (quote.getName().equals(user)) { // Check for empty value
 				response = Config.speech.get("QU_SUC").replace("<name>", quote.getName()).replace("<quote>", quote.getMessage());
 			} else {
 				response = Config.speech.get("QU_ERR");
 			}
 		}
 		Monitor.print("~RESPONSE  Quoted: " + quote.getChannel() + " <" + quote.getName() + "> " + quote.getMessage());
 		event.getBot().sendMessage(event.getChannel(), response);
 	}
 }
