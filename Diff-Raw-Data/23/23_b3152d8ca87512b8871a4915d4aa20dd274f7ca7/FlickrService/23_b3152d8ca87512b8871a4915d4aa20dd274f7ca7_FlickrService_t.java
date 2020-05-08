 package nz.co.searchwellington.flickr;
 
 import nz.co.searchwellington.model.Tag;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 
 import uk.co.eelpieconsulting.common.caching.MemcachedCache;
 
 import com.google.common.base.Strings;
 
 @Component
 public class FlickrService {
 
     private static final int ONE_DAY = 3600 * 24;
 
 	private static Logger log = Logger.getLogger(FlickrService.class);
 
     private FlickrApi flickerApiService;
 	private MemcachedCache cache;
 
	private String poolGroupId;
	
 	@Autowired
    public FlickrService(FlickrApi flickrApi, MemcachedCache cache, @Value("#{config['flickr.poolGroupId']}") String poolGroupId) {
         this.flickerApiService = flickrApi;
         this.cache = cache;
		this.poolGroupId = poolGroupId;
     }    
     
     public int getFlickrPhotoCountFor(Tag tag) {
    	if (Strings.isNullOrEmpty(poolGroupId)) {
     		log.debug("No Flickr pool group id defined; returning 0");
    		return 0;
     	}
     	
         log.debug("Running Flickr tag photo count for tag: " + tag.getDisplayName());        
         final String cacheKey = "flickrphotocount:" + poolGroupId + ":" + tag.getDisplayName().replaceAll("\\s", "");
         
         final Integer cachedCount = (Integer) cache.get(cacheKey);
         if (cachedCount != null) {
         	log.debug("Returning cached count: " + cachedCount);
         	return cachedCount;
         }
         
         final int poolPhotoCountForTag = flickerApiService.getPoolPhotoCountForTag(tag.getDisplayName(), poolGroupId);
         log.debug("Got pool photo count of " + poolPhotoCountForTag + " for tag '" + tag.getDisplayName() + "'");
         
         log.debug("Caching count for tag: " + tag.getDisplayName());
         cache.put(cacheKey, ONE_DAY, poolPhotoCountForTag);
 		return poolPhotoCountForTag;
     }
     
 	public String getPoolId() {
 		return poolGroupId;
 	}
 
 }
