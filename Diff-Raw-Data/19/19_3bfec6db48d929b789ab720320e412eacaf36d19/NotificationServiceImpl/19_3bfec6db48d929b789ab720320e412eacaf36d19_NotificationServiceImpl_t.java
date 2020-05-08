 package at.ac.tuwien.dse.fairsurgeries.service;
 
 import java.util.List;
 
 import at.ac.tuwien.dse.fairsurgeries.domain.Hospital;
 import at.ac.tuwien.dse.fairsurgeries.domain.Notification;
 
 public class NotificationServiceImpl implements NotificationService {
	
	public List<Notification> findByHospital(Hospital hospital) {
		List<Notification> allNotifications = notificationRepository.findAll();
		return allNotifications;
	}
 }
