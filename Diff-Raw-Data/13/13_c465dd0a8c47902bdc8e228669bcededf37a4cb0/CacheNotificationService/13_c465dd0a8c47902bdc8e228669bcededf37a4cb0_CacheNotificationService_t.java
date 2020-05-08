 package org.jasig.portlet.notice.service;
 
 import java.util.List;
 import java.util.Map;
 
 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 import org.jasig.portlet.notice.response.NotificationResponse;
 import org.jasig.portlet.notice.service.exceptions.NotificationServiceException;
 import org.jasig.portlet.notice.service.iface.INotificationService;
 
 import com.googlecode.ehcache.annotations.Cacheable;
 
 /**
  * This class contains all the notification service providers. It implements
  * the EHCache implementation. An instance of this class is called from the
  * DataController in order to retrieve the notifications for a given user.
  */
 public class CacheNotificationService implements INotificationService {
 
    private final Log log = LogFactory.getLog(getClass());

    /**
 	 * Returns the name of the service.
 	 * @return String.
 	 */
 	public String getName()
 	{
 		return "CacheService";
 	}
 
 	@Cacheable(cacheName="notificationCache")
 	public NotificationResponse getNotifications(Map<String, String> userInfo)
 		throws NotificationServiceException
 	{
	    log.debug("Invoking embedded notification services...");
	    
 		NotificationResponse masterResponse = new NotificationResponse();
 
 		for(INotificationService notificationService: embeddedServices)
 		{
 		    NotificationResponse response = notificationService.getNotifications(userInfo);
 		    masterResponse.addResponseData(response);
 		}
 		
 		return masterResponse;
 	}
 
 	private List<INotificationService> embeddedServices;
     public void setEmbeddedServices(List<INotificationService> embeddedServices) {
         this.embeddedServices = embeddedServices;
     }
 }
