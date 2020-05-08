 package com.chitter.bot;
 
 import java.text.SimpleDateFormat;
import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.SimpleTimeZone;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import twitter4j.DirectMessage;
 import twitter4j.Paging;
 import twitter4j.ResponseList;
 import twitter4j.Status;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 
 import com.chitter.external.TwitterAPI;
 import com.chitter.persistence.UserAccount;
 import com.chitter.persistence.UserTwitterTimeline;
 import com.chitter.utility.ExceptionPrinter;
 import com.google.appengine.api.xmpp.JID;
 import com.google.appengine.api.xmpp.Message;
 import com.google.appengine.api.xmpp.MessageBuilder;
 import com.google.appengine.api.xmpp.MessageType;
 import com.google.appengine.api.xmpp.Presence;
 import com.google.appengine.api.xmpp.XMPPService;
 import com.google.appengine.api.xmpp.XMPPServiceFactory;
 
 @SuppressWarnings("serial")
 public class StreamCronjobServlet extends HttpServlet {
 
 	private static final XMPPService xmppService =
 		XMPPServiceFactory.getXMPPService();
 	private static final MessageBuilder messageBuilder =
 		new MessageBuilder();
 	
 	/*private static final Queue queue = 
 		QueueFactory.getDefaultQueue();
 	*/
 	public void doGet(HttpServletRequest request, HttpServletResponse response) {
 		processRequest(request,response);
 	}
 	public void doPost(HttpServletRequest request, HttpServletResponse response) {
 		processRequest(request,response);
 	}
 	
	@SuppressWarnings("unchecked")
 	private void processRequest(HttpServletRequest request, HttpServletResponse response) {
 		
		List<UserAccount> userAccounts = new ArrayList<UserAccount>();
		userAccounts.addAll(UserAccount.getUserAccountList()); 
 		Iterator<UserAccount> it = userAccounts.iterator();
 		
 		while(it.hasNext()){
 			UserAccount userAccount = it.next();
 			Presence presence=xmppService.getPresence(new JID(userAccount.getGtalkId()));
 			if(presence.isAvailable() && userAccount.getIsTimelineActive()) {
 				//queue.add(Builder.withUrl("/stream-task?gtalkId="+userAccount.getGtalkId()+
 				//						"&accessToken="+userAccount.getTwitterAccessToken()+
 				//						"&accessTokenSecret="+userAccount.getTwitterAccessTokenSecret())
 				//						.method(Method.GET));
 				try {
 					sendTimelineUpdates(userAccount.getGtalkId(),userAccount.getTwitterAccessToken(),userAccount.getTwitterAccessTokenSecret());
 				} catch (TwitterException e) {
					ExceptionPrinter.print(System.out, e, "I couldn't send timeline updates to "+userAccount.getGtalkId());
 				}
 			}
 		}
 	}
 	
 	public static void sendTimelineUpdates(String gtalkId, String accessToken, String accessTokenSecret) throws TwitterException {
 		UserTwitterTimeline userTT = new UserTwitterTimeline(gtalkId);
 		Long ttSinceId = userTT.getTwitterTimelineSinceId();
 		Long dmSinceId = userTT.getTwitterDirectMessageSinceId();
 
 
 		System.out.println("Starting routine task for user "+gtalkId+" ... ttSince "+ttSinceId+ " dmSince "+dmSinceId);
 		System.out.println("with accessToken "+accessToken+" and accessTokenSecret "+accessTokenSecret);
 		if(dmSinceId!=null && ttSinceId !=null && gtalkId != null && accessToken != null && accessTokenSecret != null){
 			System.out.println("started");
 			Twitter twitter = TwitterAPI.getInstanceFor(accessToken, accessTokenSecret);
 			System.out.println("Twitter authentication is set.");
 			
 			ResponseList<Status> timeline = twitter.getFriendsTimeline(new Paging(ttSinceId));
 			ResponseList<DirectMessage> directMessages = twitter.getDirectMessages(new Paging((long)dmSinceId));
 			System.out.println("Lists are fetched for "+ twitter.getScreenName()+ " with sizes "+timeline.size()+" "+directMessages.size());
 			
 			for(int i=timeline.size()-1;i>=0;i--){
 				System.out.println("Trying to get "+i+"th wall post.");
 				Status status = timeline.get(i);
 				System.out.println("Yes, we're in");
 				if(!status.getUser().getScreenName().equals(twitter.getScreenName())) {
 					String messageBody = "_*" +status.getUser().getScreenName()+ ":*_ ";
 					if(status.isRetweet()) {
 						messageBody += "_rt_ _"+status.getRetweetedStatus().getUser().getScreenName()+"_: " +status.getRetweetedStatus().getText();
 					} else {
 						messageBody += status.getText();
 					}
 					Message message = messageBuilder
 					.withRecipientJids(new JID(gtalkId))
 					.withMessageType(MessageType.CHAT)
 					.withBody(messageBody)
 					.build();
 
 					xmppService.sendMessage(message);
 				}
 			}
 			SimpleDateFormat formatter = new SimpleDateFormat("dd.MMM.yyyy HH:mm");
 			SimpleTimeZone timeZone = new SimpleTimeZone(twitter.showUser(twitter.getId()).getUtcOffset()*1000+60*60*1000,"UTC");
 			formatter.setTimeZone(timeZone);
 			
 			for(int i=directMessages.size()-1;i>=0;i--){
 				DirectMessage directMessage = directMessages.get(i);
 				Date date = directMessage.getCreatedAt();
 
 				System.out.println("Trying to get "+i+"th direct post.");
 				Message message = messageBuilder
 				.withRecipientJids(new JID(gtalkId))
 				.withMessageType(MessageType.CHAT)
 				.withBody("_*"+directMessage.getSenderScreenName()
 						+"*_ _direct_ _message_ _"
 						+formatter.format(date)
 						+":_ "+ directMessage.getText())
 						.build();
 
 				xmppService.sendMessage(message);
 			}
 			
 			boolean isSinceIdsChanged = false;
 			if(timeline.size()>0) {
 				ttSinceId=timeline.get(0).getId();
 				isSinceIdsChanged = true;
 			}
 			if(directMessages.size()>0) {
 				dmSinceId=directMessages.get(0).getId();
 				isSinceIdsChanged = true;
 			}
 
 			if(isSinceIdsChanged){
 				new UserTwitterTimeline(gtalkId, ttSinceId, dmSinceId);
 			}			
 		}		
 	}
 }
