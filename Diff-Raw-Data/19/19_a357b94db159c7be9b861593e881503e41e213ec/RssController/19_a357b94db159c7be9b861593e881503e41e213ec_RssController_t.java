 package nz.co.searchwellington.controllers;
 
 import java.util.HashMap;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import nz.co.searchwellington.model.SiteInformation;
 import nz.co.searchwellington.repositories.ContentRetrievalService;
 import nz.co.searchwellington.views.RssItemMaker;
 import nz.co.searchwellington.views.RssView;
 
 import org.apache.log4j.Logger;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.View;
 import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
 import org.springframework.web.servlet.view.RedirectView;
 
 public class RssController extends MultiActionController {
 	
	static Logger log = Logger.getLogger(RssController.class);
     
     private static final int MAX_RSS_ITEMS = 30;	// TODO move this knowledge towards the CRS
    
     private SiteInformation siteInformation;
 	private ContentRetrievalService contentRetrievalService;
 	private RssItemMaker rssItemMaker;
	private RssUrlBuilder rssUrlBuilder;
	
    public RssController(SiteInformation siteInformation, ContentRetrievalService contentRetrievalService, RssItemMaker rssItemMaker, RssUrlBuilder rssUrlBuilder) {
         this.siteInformation = siteInformation;
         this.contentRetrievalService = contentRetrievalService;
         this.rssItemMaker = rssItemMaker;
        this.rssUrlBuilder = rssUrlBuilder;
     }
     
     public ModelAndView mainRss(HttpServletRequest request, HttpServletResponse response) throws Exception {    	
     	if (siteInformation.getFeedburnerUrl() != null && !siteInformation.getFeedburnerUrl().trim().equals("")) {    		
     		final String userAgent = request.getHeader("User-Agent");
     		boolean clientIsFeedburner = userAgent != null && userAgent.startsWith("FeedBurner");
     		if (!clientIsFeedburner) {
     			return redirectToFeedburnerMainFeed();
     		}
     	}
     	
 		HashMap <String, Object> model = new HashMap <String, Object>();
 		log.info("Building full site rss feed");
 		model.put("heading", siteInformation.getAreaname() + " Newslog");
 		model.put("link", siteInformation.getUrl());
         model.put("description", "Links to " + siteInformation.getAreaname() + " related newsitems.");
         model.put("main_content", contentRetrievalService.getLatestNewsitems(MAX_RSS_ITEMS));
         
        RssView rssView = new RssView(rssItemMaker, rssUrlBuilder);
         return new ModelAndView(rssView, model);        
     }
         
 	private ModelAndView redirectToFeedburnerMainFeed() {
 		View redirectView = new RedirectView(siteInformation.getFeedburnerUrl());
 		return new ModelAndView(redirectView);		
 	}
 
 }
