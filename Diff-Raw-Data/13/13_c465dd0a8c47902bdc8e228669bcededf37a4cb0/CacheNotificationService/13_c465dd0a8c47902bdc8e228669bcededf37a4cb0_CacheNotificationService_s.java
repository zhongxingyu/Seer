 package org.jasig.portlet.notice.service;
 
import java.io.IOException;
import java.io.InputStream;
 import java.util.List;
 import java.util.Map;
 
 
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
