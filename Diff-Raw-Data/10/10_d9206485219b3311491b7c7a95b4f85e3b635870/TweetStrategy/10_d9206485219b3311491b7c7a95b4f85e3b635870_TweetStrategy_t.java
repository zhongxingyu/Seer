 package com.chitter.bot.strategy;
 
 import twitter4j.StatusUpdate;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 
 import com.chitter.external.BitlyAPI;
 import com.chitter.external.TwitterAPI;
 import com.chitter.persistence.UserAccount;
 import com.chitter.utility.ExceptionPrinter;
 import com.google.appengine.api.xmpp.Message;
 
 public class TweetStrategy extends AbstractStrategy {
 
 	@Override
 	public void handleMessage(UserAccount userAccount, Message message) {
 		String messageBody = message.getBody().substring(message.getBody().indexOf(' '), message.getBody().length()).trim();
 		
 		/** 
 		 * This will parse the first word of the tweet,
 		 * If it's a mention (e.g. @cansinyildiz)
 		 * tweet will be sent as in reply to cansinyildiz's 
 		 * last tweet.
 		 */
		String receiverScreenName;
		if(messageBody.indexOf(' ')>0) {
			receiverScreenName = messageBody.substring(0, messageBody.indexOf(' ')).trim();
		} else {
			receiverScreenName = messageBody.substring(0, messageBody.length()).trim();
		}
		if(!receiverScreenName.isEmpty() && receiverScreenName.charAt(0)=='@') {
 			receiverScreenName = receiverScreenName.substring(1,receiverScreenName.length()).trim();
 		} else {
 			receiverScreenName = "";
 		}
 		
 		Twitter twitter = TwitterAPI.getInstanceFor(userAccount);
 
 		try {
 			messageBody = BitlyAPI.shortenUrls(messageBody);
 			if(messageBody.length()<140) {
 				StatusUpdate statusUpdate=new StatusUpdate(messageBody);
 				if(!receiverScreenName.isEmpty()) {
 					statusUpdate.setInReplyToStatusId(twitter.getUserTimeline(receiverScreenName).get(0).getId());
 				}
 				twitter.updateStatus(statusUpdate);
 				replyToMessage(message, "Your tweet has been sent.");
 			} else {
 				replyToMessage(message, "Your message has "+(messageBody.length()-140)+" extra characters.");
 			}
 		} catch (TwitterException e) {
 			ExceptionPrinter.print(System.err, e, "Boss, I couldn't tweet "+userAccount.getGtalkId()+"'s message.");
 			replyToMessage(message, "I couldn't send your tweet.");	
 		}	
 	}
 
 }
