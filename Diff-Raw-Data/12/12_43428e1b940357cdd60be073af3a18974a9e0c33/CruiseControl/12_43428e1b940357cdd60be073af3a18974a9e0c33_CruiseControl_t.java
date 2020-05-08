 package achievements;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.pircbotx.hooks.events.MessageEvent;
 
 import database.Database;
 
 public class CruiseControl extends Achievement {
 	
 	public CruiseControl(Database db) {
 		super(db);
 	}
 	
 	protected int getAchievementId() {
 		return 1;
 	}
 	
 	/**
 	 * Maps nicks to Data objects
 	 */
 	private Map<String, Data> messageTracker = new HashMap<String, Data>();
 	
 	/**
 	 * Encapulates a message string and the number of times in a row we have seen the message.
 	 */
 	private class Data {
 		public String message;
 		int numTimes;
 		public Data(String message) {
 			this.message = message;
 			numTimes = 1;
 		}
 	}
 	
 	public void onMessage(MessageEvent event) {
 		String message = event.getMessage();
		if(message.toUpperCase().equals(message) && stringContainsAlpha(message)) { // is the message capitalized & contains alpha chars?
 			String nick = event.getUser().getNick();
 			if(messageTracker.containsKey(nick)) { // do we have a previous capitalized event?
 				String prevMessage = messageTracker.get(nick).message;
 				if(prevMessage.equals(message)) {
 					messageTracker.get(nick).numTimes += 1; // we have seen the message one more time in a row
 					if(messageTracker.get(nick).numTimes == 3) {
 						/* achievement awarded */
 						if(!db.hasAchievement(nick, getAchievementId())) { // award if the user didn't already have it
 							db.giveAchievement(nick, getAchievementId());
 							event.respond(getAwardString(nick));
 						}
 					}
 				}
 				else {
 					messageTracker.remove(nick); // remove the old message
 					messageTracker.put(nick, new Data(message)); // insert the new message
 				}
 			}
 			else {
 				// add the new event to our messageTracker
 				messageTracker.put(nick, new Data(message));
 			}
 		}
 	}
	
	public boolean stringContainsAlpha(String str) {
		for(int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if(c >= 'a' && c <= 'z') return true;
			else if(c >= 'A' && c <= 'Z') return true;
		}
		return false;
 	}
 
 	
 }
