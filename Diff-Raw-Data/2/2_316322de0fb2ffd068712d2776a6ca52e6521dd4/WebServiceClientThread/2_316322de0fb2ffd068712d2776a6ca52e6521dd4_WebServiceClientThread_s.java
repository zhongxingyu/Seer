 package webServiceClient;
 
 import interfaces.MediatorWeb;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Random;
 
 import data.LoginCred;
 import data.Service;
 import data.UserEntry;
 import data.UserProfile;
 import data.Service.Status;
 import data.UserEntry.Offer;
 import data.UserProfile.UserRole;
 
 public class WebServiceClientThread extends Thread {
 	private boolean running;
 
 	private Random random;
 	private Date date;
 
 	private MediatorWeb med;
 
 	private Hashtable<String, Service> offers;
 	private Hashtable<String, UserProfile> users;
 	
 	public WebServiceClientThread(MediatorWeb med) {
 		this.med = med;
 		
 		random = new Random();
 		date = new Date();
 
 		users = new Hashtable<String, UserProfile>();
 		offers = new Hashtable<String, Service>();
 
 		users.put("pvlase", new UserProfile("pvlase","Paul",  "Vlase", UserRole.BUYER, "parola"));
 		users.put("unix140", new UserProfile("unix140","Ghennadi",  "Procopciuc", UserRole.BUYER, "marmota"));
 	}
 	
 	public void run() {
 		int timeLimit = 2500;
 		running = true;
 		
 		try {
 			while (isRunning()) {
 				int sleepTime = 1000 + random.nextInt(timeLimit);
 				
 				Thread.sleep(sleepTime);
 				
 				for (Map.Entry<String, Service> offer: offers.entrySet()) {
 					Service service = offer.getValue();
 
 					int event = random.nextInt(1000);
 					System.out.println("event = " + event);
 					if (event < 200) {
 						String username = getRandomString(5 + random.nextInt(16));
 						Long time = date.getTime() + random.nextInt();
 						Double price = random.nextDouble();
 
 						UserEntry user = new UserEntry(username, Offer.NO_OFFER, time, price);
 			
 						service.addUserEntry(user);
 						med.newUserNotify(service);
 					}
 				}
 			}
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private String getRandomString(int len) {
 		char[] str = new char[len];
 
 		for (int i = 0; i < len; i++) {
 			int c;
 			do {
				c = 65 + random.nextInt(123);
 			} while(c >= 91 && c <= 96);
 			str[i] = (char) c;
 		}
 
 		return new String(str);
 	}
 	
 	public synchronized void stopThread() {
 		running = false;
 	}
 	
 	public synchronized boolean isRunning() {
 		return running;
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
 		System.out.println("[WebServiceClientThread:logOut()] Bye bye");
 	}
 	
 	public synchronized UserProfile getUserProfile(String username) {
 		return users.get(username);
 	}
 	
 	public synchronized boolean setUserProfile(UserProfile profile) {
 		users.put(profile.getUsername(), profile);
 		return true;
 	}
 
 	/* Common */
 	public synchronized boolean launchOffer(Service service) {
 		service.setStatus(Status.ACTIVE);
 		offers.put(service.getName(), service);
 
 		System.out.println("[WebServiceClientMockup:addOffer] " + service.getName());
 
 		return true;
 	}
 	
 	public synchronized boolean launchOffers(ArrayList<Service> services) {
 		for (Service service: services) {
 			service.setStatus(Status.ACTIVE);
 			offers.put(service.getName(), service);
 			System.out.println("[WebServiceClientMockup:addOffers] " + service.getName());
 		}
 		
 		return true;
 	}
 	
 	public synchronized boolean dropOffer(Service service) {
 		service.setStatus(Status.INACTIVE);
 		offers.remove(service.getName());
 		System.out.println("[WebServiceClientMockup:dropOffer] " + service.getName());
 		return true;
 	}
 	
 	public synchronized boolean dropOffers(ArrayList<Service> services) {
 		for (Service service: services) {
 			service.setStatus(Status.INACTIVE);
 			offers.remove(service.getName());
 			System.out.println("[WebServiceClientMockup:dropOffers] " + service.getName());
 		}
 		
 		return true;
 	}
 }
