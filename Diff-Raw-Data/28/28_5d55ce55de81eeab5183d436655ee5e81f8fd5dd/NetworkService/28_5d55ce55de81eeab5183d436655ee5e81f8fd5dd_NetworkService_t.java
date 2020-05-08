 package interfaces;
 
import data.Service;
 import data.UserProfile;
 
/**
 * Network to Service interface.
 * 
 * @author Paul Vlase <vlase.paul@gmail.com>
 */
 public interface NetworkService {
 	public UserProfile getUserProfile();
	
	public void changeServiceNotify(Service service);
 }
