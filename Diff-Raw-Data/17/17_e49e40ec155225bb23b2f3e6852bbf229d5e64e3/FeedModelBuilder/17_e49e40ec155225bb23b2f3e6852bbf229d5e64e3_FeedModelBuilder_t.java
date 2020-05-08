  package nz.co.searchwellington.controllers.models;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import nz.co.searchwellington.feeds.FeedItemLocalCopyDecorator;
 import nz.co.searchwellington.feeds.RssfeedNewsitemService;
 import nz.co.searchwellington.model.Feed;
 import nz.co.searchwellington.model.FeedNewsitem;
 import nz.co.searchwellington.model.frontend.FrontendNewsitem;
 import nz.co.searchwellington.repositories.ContentRetrievalService;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.web.servlet.ModelAndView;
 
 @Component
 public class FeedModelBuilder extends AbstractModelBuilder implements ModelBuilder {
 	
 	private static final String FEED_ATTRIBUTE = "feedAttribute";
 	
 	private RssfeedNewsitemService rssfeedNewsitemService;
 	private GeotaggedNewsitemExtractor geotaggedNewsitemExtractor;
 	private FeedItemLocalCopyDecorator feedItemLocalCopyDecorator;
 		
 	@Autowired
 	public FeedModelBuilder(RssfeedNewsitemService rssfeedNewsitemService,
 			ContentRetrievalService contentRetrievalService,
 			GeotaggedNewsitemExtractor geotaggedNewsitemExtractor,
 			FeedItemLocalCopyDecorator feedItemLocalCopyDecorator) {
 		this.rssfeedNewsitemService = rssfeedNewsitemService;
 		this.contentRetrievalService = contentRetrievalService;
 		this.geotaggedNewsitemExtractor = geotaggedNewsitemExtractor;
 		this.feedItemLocalCopyDecorator = feedItemLocalCopyDecorator;
 	}
 
 	@Override
 	public boolean isValid(HttpServletRequest request) {
 		return request.getAttribute(FEED_ATTRIBUTE) != null;
 	}
 	
 	@Override
 	public ModelAndView populateContentModel(HttpServletRequest request) {
 		if (isValid(request)) {
 			Feed feed = (Feed) request.getAttribute(FEED_ATTRIBUTE);
 			if (feed != null) {
 				ModelAndView mv = new ModelAndView();
				mv.addObject("feed", feed);	// TODO wants to be a frontend feed				
 				populateFeedItems(mv, feed);
 				setRss(mv, feed.getName(), feed.getUrl());		       
 				return mv;
 			}
 		}
 		return null;
 	}
 	
 	@Override
 	public void populateExtraModelContent(HttpServletRequest request, ModelAndView mv) {
 		populateGeotaggedFeedItems(mv);
 		populateSecondaryFeeds(mv);
 	}
 	
 	@Override
 	public String getViewName(ModelAndView mv) {
 		return "viewfeed";
 	}
 	
 	private void populateFeedItems(ModelAndView mv, Feed feed) {
 		List<FeedNewsitem> feedNewsitems = rssfeedNewsitemService.getFeedNewsitems(feed);		
 		if (feedNewsitems != null && !feedNewsitems.isEmpty()) {
 			mv.addObject("main_content", feedItemLocalCopyDecorator.addSupressionAndLocalCopyInformation(feedNewsitems));
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void populateGeotaggedFeedItems(ModelAndView mv) {
 		List<FrontendNewsitem> mainContent = (List<FrontendNewsitem>) mv.getModel().get("main_content");
 		if (mainContent != null) {
 			final List<FrontendNewsitem> geotaggedItems = geotaggedNewsitemExtractor.extractGeotaggedItems(mainContent);
 			if (!geotaggedItems.isEmpty()) {
 				mv.addObject("geocoded", geotaggedItems);				
 			}
 		}
 	}
 	
 }
