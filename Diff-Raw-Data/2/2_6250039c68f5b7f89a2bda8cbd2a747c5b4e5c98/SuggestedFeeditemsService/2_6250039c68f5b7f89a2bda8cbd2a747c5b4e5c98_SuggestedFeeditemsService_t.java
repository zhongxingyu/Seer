 package nz.co.searchwellington.repositories;
 
 import java.util.List;
 
 import nz.co.searchwellington.feeds.FeedItemLocalCopyDecorator;
 import nz.co.searchwellington.feeds.reading.WhakaokoFeedItemMapper;
 import nz.co.searchwellington.feeds.reading.WhakaoroClientFactory;
 import nz.co.searchwellington.model.Feed;
 import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem;
 import nz.co.searchwellington.model.frontend.FrontendNewsitem;
 
 import org.apache.log4j.Logger;
 import org.elasticsearch.common.collect.Lists;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import uk.co.eelpieconsulting.common.http.HttpBadRequestException;
 import uk.co.eelpieconsulting.common.http.HttpFetchException;
 import uk.co.eelpieconsulting.common.http.HttpForbiddenException;
 import uk.co.eelpieconsulting.common.http.HttpNotFoundException;
 import uk.co.eelpieconsulting.whakaoro.client.exceptions.ParsingException;
 import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem;
 
 @Component
 public class SuggestedFeeditemsService {
 	
 	private static Logger log = Logger.getLogger(SuggestedFeeditemsService.class);
 	
 	private final FeedItemLocalCopyDecorator feedItemLocalCopyDecorator;
 	private final WhakaoroClientFactory whakaoroClientFactory;
 	private final WhakaokoFeedItemMapper whakaokoFeedItemMapper;
 	private final HibernateResourceDAO resourceDAO;
 	
 	@Autowired
 	public SuggestedFeeditemsService(WhakaoroClientFactory whakaoroClientFactory, 
 			WhakaokoFeedItemMapper whakaokoFeedItemMapper,
 			FeedItemLocalCopyDecorator feedItemLocalCopyDecorator,
 			HibernateResourceDAO resourceDAO) {
 		this.whakaoroClientFactory = whakaoroClientFactory;
 		this.whakaokoFeedItemMapper = whakaokoFeedItemMapper;		
 		this.feedItemLocalCopyDecorator = feedItemLocalCopyDecorator;
 		this.resourceDAO = resourceDAO;
 	}
 	
 	public List<FrontendNewsitem> getSuggestionFeednewsitems(int maxItems) {
 		try {
 			List<FrontendFeedNewsitem> suggestions = Lists.newArrayList();
 			for (FeedItem feedItem : whakaoroClientFactory.getChannelFeedItems()) {
 				Feed feed = resourceDAO.loadFeedByWhakaoroId(feedItem.getSubscriptionId());
 				suggestions.add(whakaokoFeedItemMapper.mapWhakaokoFeeditem(feed, feedItem));
 			}			
 			return feedItemLocalCopyDecorator.addSupressionAndLocalCopyInformation(suggestions);
 			
 		} catch (HttpNotFoundException e) {
 			log.error(e);
 		} catch (HttpBadRequestException e) {
 			log.error(e);
 		} catch (HttpForbiddenException e) {
 			log.error(e);
 		} catch (ParsingException e) {
 			log.error(e);
 		} catch (HttpFetchException e) {
 			log.error(e);
 		}
 		
		return Lists.newArrayList();
 	}
 	
 }
