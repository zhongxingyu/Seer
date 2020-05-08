 package nz.co.searchwellington.controllers.models;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import nz.co.searchwellington.controllers.LoggedInUserFilter;
 import nz.co.searchwellington.controllers.RssUrlBuilder;
 import nz.co.searchwellington.controllers.models.helpers.ArchiveLinksService;
 import nz.co.searchwellington.model.User;
 import nz.co.searchwellington.model.frontend.FrontendResource;
 import nz.co.searchwellington.repositories.ContentRetrievalService;
 import nz.co.searchwellington.urls.UrlBuilder;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.web.servlet.ModelAndView;
 
 @Component
 public class IndexModelBuilder extends AbstractModelBuilder implements ModelBuilder {
 	
 	private static final int MAX_OWNED_TO_SHOW_IN_RHS = 4;
 	private static final int NUMBER_OF_COMMENTED_TO_SHOW = 2;
 		
 	private ContentRetrievalService contentRetrievalService;
 	private RssUrlBuilder rssUrlBuilder;
 	private LoggedInUserFilter loggedInUserFilter;
 	private UrlBuilder urlBuilder;
 	private ArchiveLinksService archiveLinksService;
 	
 	@Autowired
 	public IndexModelBuilder(ContentRetrievalService contentRetrievalService,
 			RssUrlBuilder rssUrlBuilder, LoggedInUserFilter loggedInUserFilter,
 			UrlBuilder urlBuilder, ArchiveLinksService archiveLinksService) {
 		this.contentRetrievalService = contentRetrievalService;
 		this.rssUrlBuilder = rssUrlBuilder;
 		this.loggedInUserFilter = loggedInUserFilter;
 		this.urlBuilder = urlBuilder;
 		this.archiveLinksService = archiveLinksService;
 	}
 	
 	@Override
 	public boolean isValid(HttpServletRequest request) {
 		return request.getPathInfo().matches("^/$") || request.getPathInfo().matches("^/json$");
 	}
 	
 	@Override
 	public ModelAndView populateContentModel(HttpServletRequest request) {
 		if (isValid(request)) {
 			ModelAndView mv = new ModelAndView();		
 			final List<FrontendResource> latestNewsitems = contentRetrievalService.getLatestNewsitems(MAX_NEWSITEMS);                
 			mv.addObject("main_content", latestNewsitems);
 			
 			setRss(mv, rssUrlBuilder.getBaseRssTitle(), rssUrlBuilder.getBaseRssUrl());
 			
 			if (latestNewsitems != null) {
 				Date monthOfLastItem = monthOfLastItem(latestNewsitems);
 				if (monthOfLastItem != null) {
 					mv.addObject("main_content_moreurl", urlBuilder.getArchiveLinkUrl(monthOfLastItem));
 				}
 			}
 			
 			return mv;
 		}
 		return null;
 	}
 	
 	@Override
 	public void populateExtraModelContent(HttpServletRequest request, ModelAndView mv) {		
 		populateCommentedNewsitems(mv);
 		populateSecondaryJustin(mv);
 		populateGeocoded(mv);
 		populateFeatured(mv);
 		populateUserOwnedResources(mv, loggedInUserFilter.getLoggedInUser());		
 		archiveLinksService.populateArchiveLinks(mv, contentRetrievalService.getArchiveMonths());
 	}
 	
 	@Override
 	public String getViewName(ModelAndView mv) {
 		return "index";
 	}
 		
 	private Date monthOfLastItem(List<FrontendResource> latestNewsitems) {
 		if (latestNewsitems.size() > 0) {
 			FrontendResource lastNewsitem = latestNewsitems.get(latestNewsitems.size() - 1);
 			if (lastNewsitem.getDate() != null) {
 				return lastNewsitem.getDate();
 			}
 		}
 		return null;
 	}
 	
 	private void populateUserOwnedResources(ModelAndView mv, User loggedInUser) {
 		 if (loggedInUser != null) {
 			 final int ownedCount = contentRetrievalService.getOwnedByCount(loggedInUser);
 			 if (ownedCount > 0) {
 				 mv.addObject("owned", contentRetrievalService.getOwnedBy(loggedInUser, MAX_OWNED_TO_SHOW_IN_RHS));
 				 if (ownedCount > MAX_OWNED_TO_SHOW_IN_RHS) {
					 mv.addObject("owned_moreurl", urlBuilder.getProfileUrlFromProfileName(loggedInUser.getName()));
 				 }
 			 }			 
 		 }
 	}
 	
 	private void populateFeatured(ModelAndView mv) {
         mv.addObject("featured", contentRetrievalService.getFeaturedSites());
     }
 	
 	private void populateCommentedNewsitems(ModelAndView mv) {
 		final List<FrontendResource> recentCommentedNewsitems = contentRetrievalService.getCommentedNewsitems(NUMBER_OF_COMMENTED_TO_SHOW + 1, 0);
 		if (recentCommentedNewsitems.size() <= NUMBER_OF_COMMENTED_TO_SHOW) {
 			mv.addObject("commented_newsitems", recentCommentedNewsitems);
 		} else {
 			mv.addObject("commented_newsitems", recentCommentedNewsitems.subList(0, NUMBER_OF_COMMENTED_TO_SHOW));
 			mv.addObject("commented_newsitems_moreurl", "comment");
 		}
 	}
 	
 	private void populateGeocoded(ModelAndView mv) {
         List<FrontendResource> geocoded = contentRetrievalService.getGeocoded(0, MAX_NUMBER_OF_GEOTAGGED_TO_SHOW);
         if (geocoded.size() > 0) {
             mv.addObject("geocoded", geocoded);
         }
     }
 	
 	private void populateSecondaryJustin(ModelAndView mv) {
 		mv.addObject("secondary_heading", "Just In");
 		mv.addObject("secondary_description", "New additions.");
 		mv.addObject("secondary_content", contentRetrievalService.getLatestWebsites(4));   
 		mv.addObject("secondary_content_moreurl", "justin");        
 	}
 	
 }
