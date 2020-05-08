 package interfaces;
 
 import java.util.Hashtable;
 
 import data.Service;
 import data.UserProfile;
 
 /**
  * Mediator interface for Network module.
  * 
  * @author Paul Vlase <vlase.paul@gmail.com>
  */
 public interface MediatorNetwork {
 	public UserProfile getUserProfile();
 
 	public void registerNetwork(NetworkMediator net);
 
 	public void changeServiceNotify(Service service);
 	
 	public void startTransfer(Service service);
 	
 	public void putOffer(Service service);
 	public Service getOffer(String serviceName);
 	public Hashtable<String, Service> getOffers();
 	public void removeOffer(String serviceName);
 }
