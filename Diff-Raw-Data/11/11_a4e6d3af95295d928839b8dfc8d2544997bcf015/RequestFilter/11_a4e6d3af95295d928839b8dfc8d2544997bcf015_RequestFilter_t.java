 package nz.co.searchwellington.filters;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 
 import nz.co.searchwellington.filters.attributesetters.AttributeSetter;
 import nz.co.searchwellington.filters.attributesetters.CombinerPageAttributeSetter;
 import nz.co.searchwellington.filters.attributesetters.FeedAttributeSetter;
 import nz.co.searchwellington.filters.attributesetters.PublisherPageAttributeSetter;
 import nz.co.searchwellington.filters.attributesetters.TagPageAttributeSetter;
 import nz.co.searchwellington.repositories.ResourceRepository;
 import nz.co.searchwellington.repositories.TagDAO;
 
 import org.apache.log4j.Logger;
 
 public class RequestFilter {
         
 	private static Logger log = Logger.getLogger(RequestFilter.class);
 	
     private AttributeSetter combinerPageAttributeSetter;
     private AttributeSetter tagPageAttibuteSetter;
     private AttributeSetter publisherPageAttributeSetter;
     private AttributeSetter feedAttributeSetter;
     
 	RequestAttributeFilter[] filters;
 	List<AttributeSetter> attributeSetters;
 
 	public RequestFilter() {         
     }
     
     public RequestFilter(ResourceRepository resourceDAO, TagDAO tagDAO, RequestAttributeFilter[] filters) {
         this.filters = filters;
         this.tagPageAttibuteSetter = new TagPageAttributeSetter(tagDAO);
         this.publisherPageAttributeSetter = new PublisherPageAttributeSetter(resourceDAO);
         this.combinerPageAttributeSetter = new CombinerPageAttributeSetter(tagDAO, resourceDAO);
         this.feedAttributeSetter = new FeedAttributeSetter(resourceDAO);
         
         attributeSetters = new ArrayList<AttributeSetter>();	// TODO push to spring.
         attributeSetters.add(tagPageAttibuteSetter);
         attributeSetters.add(publisherPageAttributeSetter);
         attributeSetters.add(feedAttributeSetter);
         attributeSetters.add(combinerPageAttributeSetter);
     }
     
	public void loadAttributesOntoRequest(HttpServletRequest request) {		
     	for (RequestAttributeFilter filter : filters) {
 			filter.filter(request);
 		}
     	
    	if (isReservedPath(request.getPathInfo())) {
    		return;
    	}
    	
     	for (AttributeSetter attributeSetter : attributeSetters) {
     		if (attributeSetter.setAttributes(request)) {    		
     			return;
     		}
     	}
     	
 		log.debug("Looking for single publisher and tag urls");       
     }
 
 	private boolean isReservedPath(String path) {
     	Set<String> reservedUrlWords = new HashSet<String>();		 // TODO this wants to be in the spring config
     	reservedUrlWords.add("/about");
     	reservedUrlWords.add("/api");
     	reservedUrlWords.add("/autotag");
        	reservedUrlWords.add("/index");
     	reservedUrlWords.add("/feeds");
     	reservedUrlWords.add("/comment");
     	reservedUrlWords.add("/geotagged");
     	reservedUrlWords.add("/tags");
     	
     	for(String prefix : reservedUrlWords) {
     		if (path.startsWith(prefix)) {
     			return true;
     		}
     	}    	
     	return false;
 	}
 	
 }
