 package nz.co.searchwellington.controllers;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 
 import nz.co.searchwellington.geocoding.GoogleGeoCodeService;
 import nz.co.searchwellington.model.Geocode;
 import nz.co.searchwellington.model.Image;
 import nz.co.searchwellington.model.Newsitem;
 import nz.co.searchwellington.model.PublishedResource;
 import nz.co.searchwellington.model.Resource;
 import nz.co.searchwellington.model.Tag;
 import nz.co.searchwellington.model.User;
 import nz.co.searchwellington.model.Website;
 import nz.co.searchwellington.repositories.TagDAO;
 import nz.co.searchwellington.repositories.HandTaggingDAO;
 import nz.co.searchwellington.utils.UrlCleaner;
 import nz.co.searchwellington.utils.UrlFilters;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 //TODO process publisher
 public class SubmissionProcessingService {
 
 	private static final String REQUEST_TITLE_NAME = "title";
 	private static final String REQUEST_DATE_NAME = "date";
     private static final String REQUEST_DESCRIPTION_NAME = "description";
     private static final String REQUEST_GEOCODE_NAME = "geocode";
 	private static final String REQUEST_EMBARGO_DATE_NAME = "embargo_date";
     
     Logger log = Logger.getLogger(SubmissionProcessingService.class);
     
     
     private UrlCleaner urlCleaner;
     private GoogleGeoCodeService geocodeService;
     private TagDAO tagDAO;
     private HandTaggingDAO tagVoteDAO;
     
         
 	public SubmissionProcessingService(UrlCleaner urlCleaner, GoogleGeoCodeService geocodeService, TagDAO tagDAO, HandTaggingDAO tagVoteDAO) {
 		this.urlCleaner = urlCleaner;
 		this.geocodeService = geocodeService;
 		this.tagDAO = tagDAO;
 		this.tagVoteDAO = tagVoteDAO;
 		
 	}
 
 
 	public void processTitle(HttpServletRequest req, Resource editResource) {           
         if (req.getParameter(REQUEST_TITLE_NAME) != null) {
             String title = new String(req.getParameter(REQUEST_TITLE_NAME));
             title = UrlFilters.trimWhiteSpace(title);
             title = UrlFilters.stripHtml(title);
             
             log.info("Resource title is: " + title);    
             String flattenedTitle = UrlFilters.lowerCappedSentence(title);           
             if (!flattenedTitle.equals(title)) {
                 title = flattenedTitle;             
                 log.info("Flatten capitalised sentence to '" + title + "'");
             }
             editResource.setName(title);
         }
     }
 	
 		
 	public void processUrl(HttpServletRequest req, Resource editResource) {
 		if (req.getParameter("url") != null) {
 			String url = req.getParameter("url");
 			if (url != null) {
 				url = url.trim();
 				url = UrlFilters.addHttpPrefixIfMissing(url);
 				editResource.setUrl(urlCleaner.cleanSubmittedItemUrl(url));
 			}
 		}
 	}
 	
 	
 	public void processImage(HttpServletRequest request, Newsitem editResource, User loggedInUser) {
 		Image image = (Image) request.getAttribute("image");
     	editResource.setImage(image);
     }
 
 
 
 	public void processGeocode(HttpServletRequest req, Resource editResource) {      
 		log.info("Starting processing of geocode.");
 		if (req.getParameter(REQUEST_GEOCODE_NAME) != null) {           
 
 	    	String address = new String(req.getParameter(REQUEST_GEOCODE_NAME));
 	        log.info("Found address: " + address);
 	        address = UrlFilters.trimWhiteSpace(address);
 	        address = UrlFilters.stripHtml(address);
 	        if (address != null && !address.trim().equals("")) {
 	            Geocode geocode = new Geocode(address);
 	            log.info("Setting geocode to: " + geocode.getAddress());                
 	
 	            log.info("Attempting to resolve geocode: '" + geocode.getAddress() + "'");
 	            geocodeService.resolveAddress(geocode);
 	            
 	            editResource.setGeocode(geocode);
 	            return;
 	        }
 		}
 		editResource.setGeocode(null);
 	}
 
 
 
 	public void processDate(HttpServletRequest request, Resource editResource) {
         editResource.setDate((Date) request.getAttribute(REQUEST_DATE_NAME));
         if (editResource.getDate() == null && editResource.getId() == 0) {
             editResource.setDate(Calendar.getInstance().getTime());
         }
     }
 	
 	
 	public void processEmbargoDate(HttpServletRequest request, Resource editResource) {
 		editResource.setEmbargoedUntil((Date) request.getAttribute(REQUEST_EMBARGO_DATE_NAME));
 	}
 
 
     public void processDescription(HttpServletRequest request, Resource editResource) {
         String description = request.getParameter(REQUEST_DESCRIPTION_NAME);
         if (description != null) {
         	description = StringEscapeUtils.unescapeHtml(description);
         	description = UrlFilters.stripHtml(description);
         }
         editResource.setDescription(description);
     }
     
     
     public void processHeld (HttpServletRequest request, Resource editResource) {
     	if (request.getParameter("has_held") != null) {
     		if (request.getParameter("held") != null) {
     			editResource.setHeld(true);
     			return;
     		}
     		editResource.setHeld(false);
     	}
     	return;
     }
     
     
 	public void processTags(HttpServletRequest request, Resource editResource, User user) {
     	if (request.getParameter("has_tag_select") != null) {
     		processTagSelect(request, editResource, user);    		
     	}
         if (request.getParameter("additional_tags") != null) {
            processAdditionalTags(request, editResource, user);                   
         } else {
         	log.debug("No additional tag string found.");
         }    
         trimTags(editResource, 4);               
     }
 
 
 	@SuppressWarnings("unchecked")
 	private void processTagSelect(HttpServletRequest request, Resource editResource, User user) {
 		if (request.getAttribute("tags") != null) {        	
 			List<Tag> requestTagsList = (List <Tag>) request.getAttribute("tags");
 			Set<Tag> tags = new HashSet<Tag>(requestTagsList);
 			log.info("Found " + tags.size() + " tags on the request");
 			tagVoteDAO.clearTags(editResource, user);
 			for (Tag tag : tags) {
 				tagVoteDAO.addTag(user, tag, editResource);				
 			}
 			
 		} else {
 			tagVoteDAO.clearTags(editResource, user);
 		}
 	}
     
 
     public void processPublisher(HttpServletRequest request, Resource editResource) {
     	boolean isPublishedResource = editResource instanceof PublishedResource;
     	if (isPublishedResource) {
     		((PublishedResource) editResource).setPublisher((Website) request.getAttribute("publisher"));
     	}
     }
     
     
     
    private void processAdditionalTags(HttpServletRequest request, Resource editResource, User user) {
        String additionalTagString = request.getParameter("additional_tags").trim();
         log.debug("Found additional tag string: " + additionalTagString);
         String[] fields = additionalTagString.split(",");
         if (fields.length > 0) {
             
             for (int i = 0; i < fields.length; i++) {
                 String field = fields[i].trim();
                     
                 String displayName = field;
                 field = cleanTagName(field);
                 log.debug("Wants additional tag: " + field);
                                 
                 if (isValidTagName(field)) {
                     Tag existingTag = tagDAO.loadTagByName(field);
                     if (existingTag == null) {
                         log.debug("Tag '" + field + "' is a new tag. Needs to be created.");                                
                       
                         Tag newTag = tagDAO.createNewTag();                                                
                         newTag.setName(field);                       
                         newTag.setDisplayName(displayName);                        
                         tagDAO.saveTag(newTag);
                        tagVoteDAO.addTag(user, newTag, editResource);
                         
                     } else {
                         log.debug("Found an existing tag in the additional list: " + existingTag.getName() + "; adding.");
                        tagVoteDAO.addTag(user, existingTag, editResource);
                                                      
                     }
                 } else {
                     log.debug("Ignoring invalid tag name: " + field);
                 }
             }                    
         }
     }
     
     
 
     private String cleanTagName(String field) {
     	field = StringUtils.strip(field);
         field = StringUtils.remove(field, " ");    
         return field.toLowerCase().trim();
     }
     
     
     
     
     protected void trimTags(Resource editResource, int maxTags) { // TODO reimplement
    /*     if (editResource.getTags().size() > maxTags) {
             Set <Tag> tagsToKeep = new HashSet<Tag>();
             int counter = 0;
             for (Iterator<Tag> iter = editResource.getTags().iterator(); iter.hasNext();) {
                 Tag toKeep= iter.next();
                 counter++;
                 if (counter <= 4) {
                     tagsToKeep.add(toKeep);
                 }
             }
             editResource.setTags(tagsToKeep);
     	}
     */ 
     }
 
     protected boolean isValidTagName(String field) {
         return field != null && field.length() > 0 && field.matches("[a-zA-Z0-9]*");
     }
 
 
 	
     
 		
 }
