 
 package nz.co.searchwellington.modification;
 
 import javax.servlet.http.HttpServletRequest;
 
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

 import nz.co.searchwellington.mail.Notifier;
 import nz.co.searchwellington.model.LinkCheckerQueue;
 import nz.co.searchwellington.model.Resource;
 import nz.co.searchwellington.model.User;
 import nz.co.searchwellington.repositories.ResourceRepository;
 import nz.co.searchwellington.repositories.SuggestionDAO;
 import nz.co.searchwellington.repositories.solr.SolrQueryService;
 
 public class ContentUpdateService {
 
 	private ResourceRepository resourceDAO;
 	private SuggestionDAO suggestionsDAO;
 	private LinkCheckerQueue linkCheckerQueue;
 	private Notifier notifier;
 	private SolrQueryService solrQueryService;
 
 	
 	public ContentUpdateService(ResourceRepository resourceDAO,
 			SuggestionDAO suggestionsDAO, LinkCheckerQueue linkCheckerQueue,
 			Notifier notifier, SolrQueryService solrQueryService) {
 		this.resourceDAO = resourceDAO;
 		this.suggestionsDAO = suggestionsDAO;
 		this.linkCheckerQueue = linkCheckerQueue;
 		this.notifier = notifier;
 		this.solrQueryService = solrQueryService;
 	}
 
 	
	public ContentUpdateService() {
	}

	
 	public void update(Resource resource, User loggedInUser, HttpServletRequest request) {				
 		boolean resourceUrlHasChanged = false;
 		boolean newSubmission = resource.getId() == 0;
 		if (!newSubmission) {
 			Resource existingResource = resourceDAO.loadResourceById(resource.getId());
 			resourceUrlHasChanged = !resource.getUrl().equals(existingResource.getUrl());			
 		}
 
 		boolean needsLinkCheck = resourceUrlHasChanged || newSubmission;
 		if (newSubmission) {        	
 			resource.setOwner(loggedInUser);
 		}
 		
 		update(resource, needsLinkCheck);				
 		if ((newSubmission && (loggedInUser == null || !loggedInUser.isAdmin()))) {
 			 notifier.sendSubmissionNotification("New submission", resource);
 		}
 	}
 	
	
    @Transactional(propagation = Propagation.REQUIRES_NEW)
 	public void update(Resource resource, boolean needsLinkCheck) {
 		resourceDAO.saveResource(resource);
 		solrQueryService.updateIndexForResource(resource);
 		if (resource.getType().equals("N")) {
 			suggestionsDAO.removeSuggestion(resource.getUrl());
 		}
 		if (needsLinkCheck) {
 			resource.setHttpStatus(0);
 			linkCheckerQueue.add(resource);
 		}		
 	}
 	
 }
