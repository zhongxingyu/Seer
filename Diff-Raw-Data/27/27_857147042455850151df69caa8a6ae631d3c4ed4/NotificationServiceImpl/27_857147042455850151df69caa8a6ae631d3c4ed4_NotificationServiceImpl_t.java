 package at.ac.tuwien.dse.fairsurgeries.service;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import at.ac.tuwien.dse.fairsurgeries.domain.Hospital;
 import at.ac.tuwien.dse.fairsurgeries.domain.Notification;
 
 public class NotificationServiceImpl implements NotificationService {
 	
 	public List<Notification> findByHospital(Hospital hospital) {
 		List<Notification> allNotifications = notificationRepository.findAll();
 		List<Notification> matchingNotifications = new ArrayList<Notification>(allNotifications);
 		
 		for(Notification notification:allNotifications) {
			if (notification.getOpSlot() == null) {
 				matchingNotifications.remove(notification);
 				continue;
 			}
			if (notification.getOpSlot().getHospital() == null) {
 				matchingNotifications.remove(notification);
 				continue;
 			}
			if (!notification.getOpSlot().getHospital().getId().equals(hospital.getId())) {
 				matchingNotifications.remove(notification);
 				continue;
 			}
 		}
		
 		return matchingNotifications;
 	}
 }
