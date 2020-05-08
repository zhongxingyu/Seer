 package webServiceClient;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Random;
 import java.util.concurrent.ExecutionException;
 
 import data.LoginCred;
 import data.Pair;
 import data.Service;
 import data.UserEntry;
 import data.UserProfile;
 import data.UserEntry.Offer;
 import data.UserProfile.UserRole;
 import interfaces.MediatorWeb;
 import interfaces.WebServiceClient;
 
 /**
  * WebServiceClient module implementation.
  * 
  * @author Paul Vlase <vlase.paul@gmail.com>
  */
 public class WebServiceClientMockup extends Thread implements WebServiceClient {
 	private MediatorWeb med;
 	private WebServiceClientTask task;
 
 	public WebServiceClientMockup(MediatorWeb med) {
 		this.med = med;
 		
 		med.registerWebServiceClient(this);
 		
 		task = new WebServiceClientTask(med);
 	}
 	
 	public UserProfile logIn(LoginCred cred) {
		task.execute();
 		return task.logIn(cred);
 	}
 	
 	public void logOut() {
 		System.out.println("[WebServiceClientMockup:logOut()] Bye bye");
 		
 		try {
 			task.cancel(false);
 			task.get();
 		} catch (Exception e) {
 			//e.printStackTrace();
 		}
 	}
 	
 	public UserProfile getUserProfile(String username) {
 		return task.getUserProfile(username);
 	}
 	
 	public boolean setUserProfile(UserProfile profile) {
 		return task.setUserProfile(profile);
 	}
 
 	/* Common */
 	public synchronized boolean launchOffer(Service service) {
 		return task.launchOffer(service);
 	}
 	
 	public synchronized boolean launchOffers(ArrayList<Service> services) {
 		return task.launchOffers(services);
 	}
 	
 	public synchronized boolean dropOffer(Service service) {
 		return task.dropOffer(service);
 	}
 	
 	public synchronized boolean dropOffers(ArrayList<Service> services) {
 		return task.dropOffers(services);
 	}
 	
 	/* Buyer */
 	@Override
 	public boolean acceptOffer(Pair<Service, Integer> pair) {
 		return task.acceptOffer(pair);
 	}
 
 	@Override
 	public boolean refuseOffer(Pair<Service, Integer> pair) {
 		return task.refuseOffer(pair);
 	}
 
 	/* Seller */
 	@Override
 	public boolean makeOffer(Pair<Service, Integer> pair) {
 		// TODO Auto-generated method stub
 		return task.makeOffer(pair);
 	}
 
 	@Override
 	public boolean dropAuction(Pair<Service, Integer> pair) {
 		return task.dropAuction(pair);
 	}
 }
