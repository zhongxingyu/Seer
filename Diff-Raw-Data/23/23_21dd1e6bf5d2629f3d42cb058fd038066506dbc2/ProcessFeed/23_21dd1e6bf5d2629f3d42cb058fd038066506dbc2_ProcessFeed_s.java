 package com.africaapps.league.web.server.helper;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.africaapps.league.exception.LeagueException;
 import com.africaapps.league.model.league.League;
 import com.africaapps.league.service.feed.FeedService;
 import com.africaapps.league.service.feed.MatchFilter;
 import com.africaapps.league.service.league.LeagueService;
 
 public class ProcessFeed {
 
 	public static final String[] SPRING_CONFIG_LOCATIONS = new String[] { "classpath:spring.xml" };
 
	private String x = "file:///home/aisling/development/workspaces/games/league/src/test/resources/new-env.wsdl";
	private String y = "ashley.kleynhans@gmail.com";
	private String z = "Mxit_For_AmiscoSA!!";
	

 	private static Logger LOG = LoggerFactory.getLogger(ProcessFeed.class);
 
 	public static void main(String[] args) {
 		new ProcessFeed();
 	}
 
 	public ProcessFeed() {
 		SpringBeanFactory.createApplicationContext(SPRING_CONFIG_LOCATIONS);
 		FeedService feedService = (FeedService) SpringBeanFactory.getBean("FeedService");
 		LOG.info("Got FeedService: " + feedService);
 		LeagueService leagueService = (LeagueService) SpringBeanFactory.getBean("LeagueService");		
 		try {
 			League league = leagueService.getLeague("ABSA Premier Soccer League"); 
 			MatchFilter matchFilter = null;
			feedService.processFeed(league, x, y, z, matchFilter);
 		} catch (LeagueException e) {
 			LOG.error("Error running feed: ", e);
 		} finally {
 			SpringBeanFactory.closeApplicationContext();
 			System.exit(0);
 		}
 	}
 }
