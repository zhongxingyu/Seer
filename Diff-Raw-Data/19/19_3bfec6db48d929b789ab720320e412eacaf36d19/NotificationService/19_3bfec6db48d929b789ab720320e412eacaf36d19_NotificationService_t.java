 package at.ac.tuwien.dse.fairsurgeries.service;
 
 import java.util.List;
 
 import org.springframework.roo.addon.layers.service.RooService;
 
 import at.ac.tuwien.dse.fairsurgeries.domain.Hospital;
 import at.ac.tuwien.dse.fairsurgeries.domain.Notification;
 
 @RooService(domainTypes = { at.ac.tuwien.dse.fairsurgeries.domain.Notification.class })
 public interface NotificationService {
 	
	public List<Notification> findByHospital(Hospital hospital);
 }
