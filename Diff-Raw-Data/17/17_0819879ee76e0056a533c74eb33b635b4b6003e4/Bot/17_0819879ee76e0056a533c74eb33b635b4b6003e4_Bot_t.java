 package org.mix3.twitter_bot_gae_wicket.page;
 
import javax.servlet.http.HttpServletRequest;

 import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.protocol.http.WebRequest;
 import org.mix3.twitter_bot_gae_wicket.service.Service;
 
 import twitter4j.TwitterException;
 
 import com.google.inject.Inject;
 
public class Bot extends WebPage{
 	@Inject
 	private Service service;
 	
 	public Bot(PageParameters parameters){
 		super(parameters);
 		
		HttpServletRequest request = ((WebRequest) RequestCycle.get().getRequest()).getHttpServletRequest();
		if(request.getHeader("X-AppEngine-Cron") == null || !request.getHeader("X-AppEngine-Cron").equals("true")){
			throw new RestartResponseException(Manage.class);
		}
		
 		add(new FeedbackPanel("feedback"));
 		try {
 			service.post();
 			info("Bot OK!");
 		} catch (TwitterException e) {
 			error("Bot Error!");
 		}
 	}
 }
