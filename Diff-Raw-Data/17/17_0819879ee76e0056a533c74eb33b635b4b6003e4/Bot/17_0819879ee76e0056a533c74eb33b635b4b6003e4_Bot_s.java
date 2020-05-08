 package org.mix3.twitter_bot_gae_wicket.page;
 
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.mix3.twitter_bot_gae_wicket.service.Service;
 
 import twitter4j.TwitterException;
 
 import com.google.inject.Inject;
 
public class Bot extends MyAbstractWebPage{
 	@Inject
 	private Service service;
 	
 	public Bot(PageParameters parameters){
 		super(parameters);
 		
 		add(new FeedbackPanel("feedback"));
 		try {
 			service.post();
 			info("Bot OK!");
 		} catch (TwitterException e) {
 			error("Bot Error!");
 		}
 	}
 }
