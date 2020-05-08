 package nz.co.searchwellington.controllers;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.HashSet;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import nz.co.searchwellington.controllers.admin.AdminRequestFilter;
 import nz.co.searchwellington.controllers.admin.EditPermissionService;
 import nz.co.searchwellington.feeds.FeedItemAcceptor;
 import nz.co.searchwellington.feeds.FeednewsItemToNewsitemService;
 import nz.co.searchwellington.feeds.RssfeedNewsitemService;
 import nz.co.searchwellington.feeds.rss.RssNewsitemPrefetcher;
 import nz.co.searchwellington.htmlparsing.SnapshotBodyExtractor;
 import nz.co.searchwellington.model.Feed;
 import nz.co.searchwellington.model.Newsitem;
 import nz.co.searchwellington.model.PublishedResource;
 import nz.co.searchwellington.model.Resource;
 import nz.co.searchwellington.model.Tag;
 import nz.co.searchwellington.model.UrlWordsGenerator;
 import nz.co.searchwellington.model.User;
 import nz.co.searchwellington.modification.ContentDeletionService;
 import nz.co.searchwellington.modification.ContentUpdateService;
 import nz.co.searchwellington.repositories.HandTaggingDAO;
 import nz.co.searchwellington.repositories.ResourceFactory;
 import nz.co.searchwellington.spam.SpamFilter;
 import nz.co.searchwellington.tagging.AutoTaggingService;
 import nz.co.searchwellington.widgets.AcceptanceWidgetFactory;
 import nz.co.searchwellington.widgets.TagsWidgetFactory;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 
 @Controller
 public class ResourceEditController {
     
 	private static Logger log = Logger.getLogger(ResourceEditController.class);
            
     private RssfeedNewsitemService rssfeedNewsitemService;
     private AdminRequestFilter adminRequestFilter;    
     private TagsWidgetFactory tagWidgetFactory;
     private AutoTaggingService autoTagger;
     private AcceptanceWidgetFactory acceptanceWidgetFactory;
     private RssNewsitemPrefetcher rssPrefetcher;
     private EditPermissionService editPermissionService;
     private SubmissionProcessingService submissionProcessingService;
     private ContentUpdateService contentUpdateService;
 	private ContentDeletionService contentDeletionService;
     private SnapshotBodyExtractor snapBodyExtractor;
     private AnonUserService anonUserService;
 	private HandTaggingDAO tagVoteDAO;
 	private FeedItemAcceptor feedItemAcceptor;
 	private ResourceFactory resourceFactory;
 	private CommonModelObjectsService commonModelObjectsService;
 	private LoggedInUserFilter loggedInUserFilter;
 	private UrlStack urlStack;
 	private FeednewsItemToNewsitemService feednewsItemToNewsitemService;
 	private UrlWordsGenerator urlWordsGenerator;
 	
 	public ResourceEditController() {
 	}
 	
 	@Autowired
     public ResourceEditController(
     		RssfeedNewsitemService rssfeedNewsitemService,
 			AdminRequestFilter adminRequestFilter,
 			TagsWidgetFactory tagWidgetFactory, AutoTaggingService autoTagger,
 			AcceptanceWidgetFactory acceptanceWidgetFactory,
 			RssNewsitemPrefetcher rssPrefetcher,
 			LoggedInUserFilter loggedInUserFilter,
 			EditPermissionService editPermissionService, UrlStack urlStack,
 			SubmissionProcessingService submissionProcessingService,
 			ContentUpdateService contentUpdateService,
 			ContentDeletionService contentDeletionService,
 			SnapshotBodyExtractor snapBodyExtractor,
 			AnonUserService anonUserService,
 			HandTaggingDAO tagVoteDAO, FeedItemAcceptor feedItemAcceptor,
 			ResourceFactory resourceFactory,
 			CommonModelObjectsService commonModelObjectsService,
 			FeednewsItemToNewsitemService feednewsItemToNewsitemService,
 			UrlWordsGenerator urlWordsGenerator) {
 		this.rssfeedNewsitemService = rssfeedNewsitemService;
 		this.adminRequestFilter = adminRequestFilter;
 		this.tagWidgetFactory = tagWidgetFactory;
 		this.autoTagger = autoTagger;
 		this.acceptanceWidgetFactory = acceptanceWidgetFactory;
 		this.rssPrefetcher = rssPrefetcher;
 		this.loggedInUserFilter = loggedInUserFilter;
 		this.editPermissionService = editPermissionService;
 		this.urlStack = urlStack;
 		this.submissionProcessingService = submissionProcessingService;
 		this.contentUpdateService = contentUpdateService;
 		this.contentDeletionService = contentDeletionService;
 		this.snapBodyExtractor = snapBodyExtractor;
 		this.anonUserService = anonUserService;
 		this.tagVoteDAO = tagVoteDAO;
 		this.feedItemAcceptor = feedItemAcceptor;
 		this.resourceFactory = resourceFactory;
 		this.commonModelObjectsService = commonModelObjectsService;
 		this.feednewsItemToNewsitemService = feednewsItemToNewsitemService;
 		this.urlWordsGenerator = urlWordsGenerator;
 	}
     
     @Transactional	
 	@RequestMapping("/edit")
     public ModelAndView edit(HttpServletRequest request, HttpServletResponse response) {
     	log.info("Starting resource edit method");
         response.setCharacterEncoding("UTF-8");
         
     	adminRequestFilter.loadAttributesOntoRequest(request);    	
     	Resource resource = (Resource) request.getAttribute("resource");    	
     	if (request.getAttribute("resource") == null) { 
     		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
     		log.info("No resource attribute found on request; returning 404");
         	return null;
     	}
     	    	
     	final User loggedInUser = loggedInUserFilter.getLoggedInUser();
     	if (!userIsAllowedToEdit(resource, request, loggedInUser)) {    		
     		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
     		log.info("No logged in user or user not allowed to edit resource; returning 403");
         	return null;
     	}
     	
     	ModelAndView mv = new ModelAndView("editResource");
     	commonModelObjectsService.populateCommonLocal(mv);
     	mv.addObject("heading", "Editing a Resource");
     		
         mv.addObject("resource", resource);
         mv.addObject("tag_select", tagWidgetFactory.createMultipleTagSelect(tagVoteDAO.getHandpickedTagsForThisResourceByUser(loggedInUser, resource)));
         mv.addObject("show_additional_tags", 1);
             
         boolean userIsLoggedIn = loggedInUser != null;
         populatePublisherField(mv, userIsLoggedIn, resource);
                 
         if (resource.getType().equals("F")) {            
         	mv.addObject("acceptance_select", acceptanceWidgetFactory.createAcceptanceSelect (((Feed)resource).getAcceptancePolicy()));                   
         }               
         return mv;        
     }
     
 	@Transactional
 	@RequestMapping("/edit/viewsnapshot")	
     public ModelAndView viewSnapshot(HttpServletRequest request, HttpServletResponse response) {
     	
 		adminRequestFilter.loadAttributesOntoRequest(request);    	
     	if (request.getAttribute("resource") == null) { 
     		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
         	return null;
     	}
     	Resource resource = (Resource) request.getAttribute("resource");    	
     	    	
     	final User loggedInUser = loggedInUserFilter.getLoggedInUser();
     	if (!userIsAllowedToEdit(resource, request, loggedInUser)) {
     		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
         	return null;
     	}
     	
     	Resource editResource = (Resource) request.getAttribute("resource");    	
     	if (request.getAttribute("resource") != null && userIsAllowedToEdit(editResource, request, loggedInUser)) {    		
     		ModelAndView mv = new ModelAndView("viewSnapshot");
     		commonModelObjectsService.populateCommonLocal(mv);
     		mv.addObject("heading", "Resource snapshot");
     		
             mv.addObject("resource", editResource);
             mv.addObject("body", snapBodyExtractor.extractLatestSnapshotBodyTextFor(editResource));
             
             mv.addObject("tag_select", tagWidgetFactory.createMultipleTagSelect(tagVoteDAO.getHandpickedTagsForThisResourceByUser(loggedInUser, editResource)));
             mv.addObject("show_additional_tags", 1);
             
             return mv;
         }
        
     	return new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)));
     }
 	
     @Transactional
     @RequestMapping("/edit/accept")
     // TODO should be a straight delegation to the feed acceptor?
     public ModelAndView accept(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, IOException {
         response.setCharacterEncoding("UTF-8");
     	adminRequestFilter.loadAttributesOntoRequest(request);    	// TODO get all admin things into a common path and then make this a web.xml filter
     	
     	User loggedInUser = loggedInUserFilter.getLoggedInUser();
     	if (!editPermissionService.canAcceptFeedItems(loggedInUser)) {
     		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
         	return null;
     	}
     	
     	final String url = request.getParameter("url");
 		if (url == null) {
 			log.warn("No feeditem url given");
 			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
         	return null;
 		}
 		
 		final Feed feed = (Feed) request.getAttribute("feedAttribute");
 		if (feed == null) {
 			throw new RuntimeException("Could not find feed");
 		}
 		
 		Newsitem acceptedNewsitem = feednewsItemToNewsitemService.makeNewsitemFromFeedItem(feed, rssfeedNewsitemService.getFeedNewsitemByUrl(feed, url));
 		if (acceptedNewsitem == null) {
         	log.warn("No matching newsitem found for url: " + url);
         	response.setStatus(HttpServletResponse.SC_NOT_FOUND);
         	return null;
         }
         
         acceptedNewsitem = feedItemAcceptor.acceptFeedItem(loggedInUser, acceptedNewsitem);
 		ModelAndView modelAndView = new ModelAndView("acceptResource");
 		commonModelObjectsService.populateCommonLocal(modelAndView);
 		modelAndView.addObject("heading", "Accepting a submission");
 		modelAndView.addObject("resource", acceptedNewsitem);
 		modelAndView.addObject("publisher_select", "1");
 		modelAndView.addObject("tag_select", tagWidgetFactory.createMultipleTagSelect(new HashSet<Tag>()));
 		modelAndView.addObject("acceptedFromFeed", urlWordsGenerator.makeUrlWordsFromName(acceptedNewsitem.getAcceptedFromFeedName()));
 		return modelAndView;
     }
     
     @Transactional    
     @RequestMapping("/edit/submit/website")
     public ModelAndView submitWebsite(HttpServletRequest request, HttpServletResponse response) {    
         ModelAndView modelAndView = new ModelAndView("submitWebsite");
         modelAndView.addObject("heading", "Submitting a Website");        
         Resource editResource = resourceFactory.createNewWebsite();
         modelAndView.addObject("resource", editResource);
        
         populateSubmitCommonElements(request, modelAndView);
         
         modelAndView.addObject("publisher_select", null);
         return modelAndView;
     }
     
     @Transactional
     @RequestMapping("/edit/submit/newsitem")
     public ModelAndView submitNewsitem(HttpServletRequest request, HttpServletResponse response) {
         ModelAndView modelAndView = new ModelAndView("submitNewsitem");
         modelAndView.addObject("heading", "Submitting a Newsitem");
         Resource editResource = resourceFactory.createNewNewsitem();
         modelAndView.addObject("resource", editResource);
         
         populateSubmitCommonElements(request, modelAndView);        
         return modelAndView;
     }
     
     @Transactional 
     @RequestMapping("/edit/submit/calendar")
     public ModelAndView submitCalendar(HttpServletRequest request, HttpServletResponse response) {        
         ModelAndView modelAndView = new ModelAndView("submitCalendar");
         modelAndView.addObject("heading", "Submitting a Calendar");
         Resource editResource = resourceFactory.createNewCalendarFeed("");
         modelAndView.addObject("resource", editResource);
         
         populateSubmitCommonElements(request, modelAndView);        
         return modelAndView;
     }
     
     @Transactional
     @RequestMapping("/edit/submit/feed")
     public ModelAndView submitFeed(HttpServletRequest request, HttpServletResponse response) {
         ModelAndView modelAndView = new ModelAndView("submitFeed");
         modelAndView.addObject("heading", "Submitting a Feed");
         Resource editResource = resourceFactory.createNewFeed();
         modelAndView.addObject("resource", editResource);
         modelAndView.addObject("acceptance_select", acceptanceWidgetFactory.createAcceptanceSelect(null));
         
         populateSubmitCommonElements(request, modelAndView);
         
         return modelAndView;
     }
     
     @Transactional
     @RequestMapping("/edit/submit/watchlist")
     public ModelAndView submitWatchlist(HttpServletRequest request, HttpServletResponse response) {
         ModelAndView modelAndView = new ModelAndView("submitWatchlist");
         modelAndView.addObject("heading", "Submitting a Watchlist Item");
         Resource editResource = resourceFactory.createNewWebsite();
         modelAndView.addObject("resource", editResource);
         
         populateSubmitCommonElements(request, modelAndView);
         
         return modelAndView;
     }
     
     @Transactional
     @RequestMapping("/delete")
     public ModelAndView delete(HttpServletRequest request, HttpServletResponse response) {    
         ModelAndView modelAndView = new ModelAndView("deletedResource");
         commonModelObjectsService.populateCommonLocal(modelAndView);
         modelAndView.addObject("heading", "Resource Deleted");
         
         adminRequestFilter.loadAttributesOntoRequest(request);    
         Resource editResource = (Resource) request.getAttribute("resource");
         
 		if (editResource != null && editPermissionService.canDelete(editResource)) {
             modelAndView.addObject("resource", editResource);
             editResource = (Resource) request.getAttribute("resource");            
             contentDeletionService.performDelete(editResource);
             
     		if (editResource.getType().equals("F")) { 
     			urlStack.setUrlStack(request, "");
     		}
         }
 		
         // TODO need to given failure message if we didn't actually remove the item.
         return modelAndView;
     }
     
 	@Transactional
 	@RequestMapping(value="/save", method=RequestMethod.POST)
     public ModelAndView save(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {       
         request.setCharacterEncoding("UTF-8");
         response.setCharacterEncoding("UTF-8");
 
         ModelAndView modelAndView = new ModelAndView("savedResource");
         commonModelObjectsService.populateCommonLocal(modelAndView);       
         modelAndView.addObject("heading", "Resource Saved");
         
         User loggedInUser = loggedInUserFilter.getLoggedInUser();
         
         Resource editResource = null;
         adminRequestFilter.loadAttributesOntoRequest(request);   
         
         
         if (request.getAttribute("resource") != null) {
             editResource = (Resource) request.getAttribute("resource");
             
         } else {
             log.info("Creating new resource.");
             if (request.getParameter("type") != null) {
                 String type = request.getParameter("type");
                 if (type.equals("W")) {
                     editResource = resourceFactory.createNewWebsite(); 
                 } else if (type.equals("N")) {
                     editResource = resourceFactory.createNewNewsitem(); 
                 } else if (type.equals("F")) {
                     editResource = resourceFactory.createNewFeed();                     
                 } else if (type.equals("L")) {                    
                     editResource = resourceFactory.createNewWatchlist();                   
                 } else if (type.equals("C")) {
                     editResource = resourceFactory.createNewCalendarFeed("");                   
                 } else {
                     // TODO this should be a caught error.
                     editResource = resourceFactory.createNewWebsite();
                 }
             }
                         
         }
        
         log.info("In save");
         if (editResource != null) {                      
             boolean newSubmission = editResource.getId() == 0;
                         
             if (loggedInUser == null) {
             	loggedInUser = createAndSetAnonUser(request);
             }
             
             if (newSubmission) {        // TODO is wrong place - needs to be shared with the api.
             	editResource.setOwner(loggedInUser);
             }
                    
             submissionProcessingService.processUrl(request, editResource);
             submissionProcessingService.processTitle(request, editResource);
             editResource.setGeocode(submissionProcessingService.processGeocode(request));
             submissionProcessingService.processDate(request, editResource);
             submissionProcessingService.processHeld(request, editResource);
             submissionProcessingService.processEmbargoDate(request, editResource);
             submissionProcessingService.processDescription(request, editResource);
             submissionProcessingService.processPublisher(request, editResource);
             
             if (editResource.getType().equals("N")) {
             	submissionProcessingService.processImage(request, (Newsitem) editResource, loggedInUser);
             	submissionProcessingService.processAcceptance(request, editResource, loggedInUser);
             }
                         
                              
             // Update urlwords.
             if (editResource.getType().equals("W") || editResource.getType().equals("F")) {
             	editResource.setUrlWords(urlWordsGenerator.makeUrlWordsFromName(editResource.getName()));            	
             }
                         
            processFeedAcceptancePolicy(request, editResource);
                       
             SpamFilter spamFilter = new SpamFilter();
             boolean isSpamUrl = spamFilter.isSpam(editResource);
             
             boolean isPublicSubmission = loggedInUser == null || (loggedInUser.isUnlinkedAccount());
             if (isPublicSubmission) {
             	log.info("This is a public submission; marking as held");
             	editResource.setHeld(true);
             }
             
             boolean okToSave = !newSubmission || !isSpamUrl || loggedInUser != null;
             // TODO validate. - what exactly?
             if (okToSave) {
             	// TODO could be a collection?  			 
                 
             	
             	saveResource(request, loggedInUser, editResource);
             	log.info("Saved resource; id is now: " + editResource.getId());
 
             	submissionProcessingService.processTags(request, editResource, loggedInUser);            	
                 if (newSubmission) {
                     log.info("Applying the auto tagger to new submission.");
                     autoTagger.autotag(editResource);
                 }
 
             } else {
                 log.info("Could not save resource. Spam question not answered?");                
             }
            
             modelAndView.addObject("item", editResource);
             
         } else {
             log.warn("No edit resource could be setup.");
         }
        
         return modelAndView;
     }
 
 	// TODO duplicated in public tagging
 	private User createAndSetAnonUser(HttpServletRequest request) {
 		User loggedInUser;
 		log.info("Creating new anon user for resource submission");
 		loggedInUser = anonUserService.createAnonUser();
 		loggedInUserFilter.setLoggedInUser(request, loggedInUser);
 		loggedInUserFilter.loadLoggedInUser(request);
 		return loggedInUser;
 	}
 
    // TODO move to submission handling service.
    private void processFeedAcceptancePolicy(HttpServletRequest request, Resource editResource) {
 	   if (editResource.getType().equals("F")) {		   
 		   ((Feed) editResource).setAcceptancePolicy("ignore");
 		   if (request.getParameter("acceptance") != null) {
 			   ((Feed) editResource).setAcceptancePolicy(request.getParameter("acceptance"));
 			   log.debug("Feed acceptance policy set to: " + ((Feed) editResource).getAcceptancePolicy());
 		   }
 		   rssPrefetcher.decacheAndLoad((Feed) editResource);
 	   }
    	}
    
 	private void saveResource(HttpServletRequest request, User loggedInUser, Resource editResource) {		
 		contentUpdateService.update(editResource);
 	}
 	
     private boolean userIsAllowedToEdit(Resource editResource, HttpServletRequest request, User loggedInUser) {    
     	return editPermissionService.canEdit(editResource);
     }
     
     private void populateSubmitCommonElements(HttpServletRequest request, ModelAndView modelAndView) {
         commonModelObjectsService.populateCommonLocal(modelAndView);
         modelAndView.addObject("tag_select", tagWidgetFactory.createMultipleTagSelect(new HashSet<Tag>()));
    
         User loggedInUser = loggedInUserFilter.getLoggedInUser();
         boolean userIsLoggedIn = loggedInUser != null;       
         modelAndView.addObject("publisher_select", "1");
         
         if (userIsLoggedIn) {
             // TODO duplication - also - what does this do?
             modelAndView.addObject("show_additional_tags", 1);
         }
     }
     
 	protected void populatePublisherField(ModelAndView modelAndView, boolean userIsLoggedIn, Resource editResource) {
         boolean isPublishedResource = editResource instanceof PublishedResource;  
         if (isPublishedResource) {
             modelAndView.addObject("publisher_select", "1");   
         } else {
             log.info("Edit resource is not a publisher resource.");
         }
     }
 
 }
