 package webServiceClient;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Random;
 
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
 	private WebServiceClientThread thread;
 
 	public WebServiceClientMockup(MediatorWeb med) {
 		this.med = med;
 		
 		med.registerWebServiceClient(this);
 		
 		thread = new WebServiceClientThread(med);
 		thread.start();
 	}
 	
 	public UserProfile logIn(LoginCred cred) {
 		UserProfile profile;
 		
 		profile = getUserProfile(cred.getUsername());
 		if (profile == null) {
 			return null;
 		}
 		
 		if (!profile.getPassword().equals(cred.getPassword())) {
 			return null;
 		}
 		
 		return profile;
 	}
 	
 	public void logOut() {
 		System.out.println("[WebServiceClientMockup:logOut()] Bye bye");
 		
 		try {
 			thread.stopThread();
 			thread.interrupt();
 			thread.join();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}		
 	}
 	
 	public UserProfile getUserProfile(String username) {
 		return thread.getUserProfile(username);
 	}
 	
 	public boolean setUserProfile(UserProfile profile) {
 		return thread.setUserProfile(profile);
 	}
 
 	/* Common */
 	public synchronized boolean launchOffer(Service service) {
 		return thread.launchOffer(service);
 	}
 	
 	public synchronized boolean launchOffers(ArrayList<Service> services) {
 		return thread.launchOffers(services);
 	}
 	
 	public synchronized boolean dropOffer(Service service) {
 		return thread.dropOffer(service);
 	}
 	
 	public synchronized boolean dropOffers(ArrayList<Service> services) {
 		return thread.dropOffers(services);
 	}
 	
 	/* Buyer */
 	@Override
 	public boolean acceptOffer(Pair<Service, Integer> pair) {
 		return thread.acceptOffer(pair);
 	}
 
 	@Override
 	public boolean refuseOffer(Pair<Service, Integer> pair) {
		return thread.refuseOffer(pair);
 	}
 
 	/* Seller */
 	@Override
 	public int makeOffer() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int dropAction() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 }
