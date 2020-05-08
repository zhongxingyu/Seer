 package webServiceClient;
 
 import interfaces.MediatorWeb;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import data.LoginCred;
 import data.Pair;
 import data.Service;
 import data.UserEntry;
 import data.UserProfile;
 import data.Service.Status;
 import data.UserEntry.Offer;
 import data.UserProfile.UserRole;
 
 /**
  * WebServiceClient module implementation.
  * 
  * @author Paul Vlase <vlase.paul@gmail.com>
  */
 public class WebServiceClientThread extends Thread {
 	private boolean							running;
 
 	private Random							random;
 	private Date							date;
 
 	private MediatorWeb						med;
 
 	private Hashtable<String, Service>		offers;
 	private Hashtable<String, UserProfile>	users;
 
 	public WebServiceClientThread(MediatorWeb med) {
 		this.med = med;
 
 		random = new Random();
 		date = new Date();
 
 		users = new Hashtable<String, UserProfile>();
 		offers = new Hashtable<String, Service>();
 
 		users.put("pvlase", new UserProfile("pvlase", "Paul", "Vlase",
 				UserRole.BUYER, "parola"));
 		users.put("unix140", new UserProfile("unix140", "Ghennadi",
 				"Procopciuc", UserRole.BUYER, "marmota"));
 	}
 
 	public void run() {
 		int timeLimit = 2500;
 		running = true;
 
 		try {
 			while (isRunning()) {
 				int sleepTime = 1000 + random.nextInt(timeLimit);
 
 				Thread.sleep(sleepTime);
 
 				for (Map.Entry<String, Service> offer : offers.entrySet()) {
 					Service service = offer.getValue();
 
 					int event = random.nextInt(1000);
 					System.out.println("event = " + event);
 					if (event < 200) {
 						String username = getRandomString(5 + Math.abs(random
 								.nextInt(16)));
 						Long time = date.getTime()
								+ 10000 + Math.abs(random.nextInt(1000000));
 						Double price = Math.abs(random.nextInt(10000)) / 100.0;
 
 						UserEntry user = new UserEntry(username,
 								Offer.NO_OFFER, time, price);
 
 						service.addUserEntry(user);
 						med.newUserNotify(service);
 					} else if (event < 400) {
 						List<UserEntry> users = service.getUsers();
 
 						if (users != null) {
 							int userIndex;
 
 							do {
 								userIndex = Math.abs(random.nextInt(service
 										.getUsers().size()));
 							} while (users.get(userIndex).getOffer() == Offer.OFFER_MADE
 									&& users.get(userIndex).getOffer() == Offer.OFFER_REFUSED);
 
 							double price = users.get(userIndex).getPrice();
 							if (price > 1) {
 								users.get(userIndex).setOffer(Offer.OFFER_MADE);
 
 								users.get(userIndex).setPrice(price - 1);
 							}
 
 							med.offerMadeNotify(service);
 						}
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
 				c = 48 + random.nextInt(123 - 48);
 			} while ((c >= 91 && c <= 96) || (c >= 58 && c <= 64));
 			str[i] = (char) c;
 		}
 		System.out.println(new String(str));
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
 		med.profileChangedNotify(profile);
 		return true;
 	}
 
 	/* Common */
 	public synchronized boolean launchOffer(Service service) {
 		service.setStatus(Status.ACTIVE);
 		// service.setUsers(new ArrayList<UserEntry>());
 		offers.put(service.getName(), service);
 
 		med.launchOfferNotify(service);
 		System.out.println("[WebServiceClientMockup:addOffer] "
 				+ service.getName());
 
 		return true;
 	}
 
 	public synchronized boolean launchOffers(ArrayList<Service> services) {
 		for (Service service : services) {
 			service.setStatus(Status.ACTIVE);
 			// service.setUsers(new ArrayList<UserEntry>());
 			offers.put(service.getName(), service);
 			System.out.println("[WebServiceClientMockup:addOffers] "
 					+ service.getName());
 		}
 		med.launchOffersNotify(services);
 
 		return true;
 	}
 
 	public synchronized boolean dropOffer(Service service) {
 		service.setStatus(Status.INACTIVE);
 		service.setUsers(null);
 
 		offers.remove(service.getName());
 		System.out.println("[WebServiceClientMockup:dropOffer] "
 				+ service.getName());
 
 		med.dropOfferNotify(service);
 		return true;
 	}
 
 	public synchronized boolean dropOffers(ArrayList<Service> services) {
 		for (Service service : services) {
 			service.setStatus(Status.INACTIVE);
 			service.setStatus(null);
 			offers.remove(service.getName());
 			System.out.println("[WebServiceClientMockup:dropOffers] "
 					+ service.getName());
 		}
 
 		return true;
 	}
 
 	public synchronized boolean acceptOffer(Pair<Service, Integer> pair) {
 		Service service = pair.getKey();
 		ArrayList<UserEntry> users = service.getUsers();
 
 		for (UserEntry user : users) {
 			user.setOffer(Offer.OFFER_REFUSED);
 		}
 
 		UserEntry user = users.get(pair.getValue());
 		user.setOffer(Offer.OFFER_ACCEPTED);
 
 		/* TODO: communicate with server */
 
 		users.clear();
 		users.add(user);
 
 		return true;
 	}
 
 	public synchronized boolean refuseOffer(Pair<Service, Integer> pair) {
 		return true;
 	}
 }
