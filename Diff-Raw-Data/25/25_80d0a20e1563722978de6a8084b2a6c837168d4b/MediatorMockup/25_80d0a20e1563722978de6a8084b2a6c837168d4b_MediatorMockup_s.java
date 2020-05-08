 package mediator;
 
 import interfaces.Gui;
 import interfaces.MediatorGui;
 import interfaces.MediatorNetwork;
 import interfaces.MediatorWeb;
 import interfaces.NetworkMediator;
 import interfaces.WebClient;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.StringTokenizer;
 
 import config.FilesConfig;
 import config.GlobalConfig.ServiceType;
 import data.LoginCred;
 import data.Service;
 import data.Service.Status;
 import data.UserProfile;
 import data.UserProfile.UserRole;
 
 /**
  * Mediator module implementation.
  * 
  * @author Paul Vlase <vlase.paul@gmail.com>
  */
 public class MediatorMockup implements MediatorGui, MediatorNetwork, MediatorWeb {
 	private Gui								gui;
 	private NetworkMediator					net;
 	private WebClient						web;
 
 	private UserProfile						profile;
 
 	/* Folosite doar pentru mockup. */
 	private Hashtable<String, Service>		offers;
 	private Hashtable<String, UserProfile>	users;
 
 	public MediatorMockup() {
 		offers = new Hashtable<String, Service>();
 		users = new Hashtable<String, UserProfile>();
 
 		users.put("pvlase", new UserProfile("pvlase", "Paul", "Vlase", UserRole.BUYER, "parola"));
 		users.put("unix140", new UserProfile("unix140", "Ghennadi", "Procopciuc", UserRole.SELLER, "marmota"));
 	}
 
 	@Override
 	public void registerGui(Gui gui) {
 		this.gui = gui;
 	}
 
 	@Override
 	public void registerNetwork(NetworkMediator net) {
 		this.net = net;
 	}
 
 	@Override
 	public void registerWebClient(WebClient web) {
 		this.web = web;
 	}
 
 	@Override
 	public void start() {
 		gui.start();
 	}
 
 	/* Metode pentru accesul la cache-ul de servicii. */
 	public void putOffer(Service service) {
 		offers.put(service.getName(), service);
 	}
 
 	public Service getOffer(String serviceName) {
 		return offers.get(serviceName);
 	}
 
 	public synchronized Hashtable<String, Service> getOffers() {
 		return offers;
 	}
 
 	public synchronized void removeOffer(String serviceName) {
 		offers.remove(serviceName);
 	}
 
 	/* Metode pentru accesul la cache-ul de utilizatori. */
 	public void putUser(UserProfile user) {
 		users.put(user.getUsername(), user);
 	}
 
 	public UserProfile getUser(String username) {
 		return users.get(username);
 	}
 
 	public Hashtable<String, UserProfile> getUsers() {
 		return users;
 	}
 
 	public void removeUser(String username) {
 		users.remove(username);
 	}
 
 	@Override
 	public boolean logIn(LoginCred cred) {
 		System.out.println("MediatorMockup:logIn()] Begin");
 		cred.setAddress(net.getAddress());
 		UserProfile profile = web.logIn(cred);
 
 		if (profile != null) {
 			this.profile = profile;
 			net.logIn();
 			System.out.println("MediatorMockup:logIn()] End (profile != null)");
 			return true;
 		}
 		System.out.println("MediatorMockup:logIn()] End (profile == null)");
 		return false;
 	}
 
 	@Override
 	public void logOut() {
 		System.out.println("[MockupMediator:logOut()] Begin");
 
 		web.logOut();
 		net.logOut();
 
 		profile = null;
 
 		System.out.println("[MockupMediator:logOut()] End");
 	}
 
 	@Override
 	public UserProfile getUserProfile() {
 		return profile;
 	}
 
 	@Override
 	public boolean setUserProfile(UserProfile profile) {
 		return web.setUserProfile(profile);
 	}
 
 	@Override
 	public boolean registerUser(UserProfile profile) {
 		return web.registerUser(profile);
 	}
 
 	@Override
 	public boolean verifyUsername(String username) {
 		return web.verifyUsername(username);
 	}
 
 	public static Date create(int day, int month, int year, int hourofday, int minute, int second) {
 		if (day == 0 && month == 0 && year == 0)
 			return null;
 		Calendar cal = Calendar.getInstance();
 		cal.set(year, month - 1, day, hourofday, minute, second);
 		cal.set(Calendar.MILLISECOND, 0);
 		return cal.getTime();
 	}
 
 	/* MediatorWeb */
 	@Override
 	public ArrayList<Service> loadOffers() {
 		if (profile == null)
 			return null;
 
 		ArrayList<Service> services = null;
 		if (profile.getRole() == UserRole.BUYER)
 			services = loadServicesFile(FilesConfig.DEMANDS_FILENAME, ServiceType.DEMAND);
 
 		if (profile.getRole() == UserRole.SELLER) {
 			services = loadServicesFile(FilesConfig.SUPPLIES_FILENAME, ServiceType.SUPPLY);
 			launchOffers(services);
 		}
 
 		for (Service service : services) {
 			if (service.getUsers() != null) {
 				Collections.sort(service.getUsers());
 			}
 		}
 
 		/*
 		 * ArrayList<Service> services = new ArrayList<Service>();
 		 * if(profile.getRole() == UserRole.SELLER){ ArrayList<UserEntry> users
 		 * = new ArrayList<UserEntry>();
 		 * 
 		 * UserEntry user1 = new UserEntry("Ghennadi", Offer.OFFER_ACCEPTED, new
 		 * Date().getTime() + 123456876, 22.4); UserEntry user2 = new
 		 * UserEntry("Paul", Offer.TRANSFER_STARTED, new Date().getTime() +
 		 * 223456876, 25.4); //user2.setProgress(22); UserEntry user3 = new
 		 * UserEntry("Ana", Offer.OFFER_ACCEPTED, new Date().getTime() +
 		 * 323456876, 2.1); users.add(user1); users.add(user2);
 		 * users.add(user3);
 		 * 
 		 * Service service1 = new Service("service1", users, Status.ACTIVE);
 		 * 
 		 * Service service2 = new Service("service2", null, Status.INACTIVE);
 		 * Service service3 = new Service("service3", null, Status.ACTIVE);
 		 * services.add(service1); services.add(service2);
 		 * services.add(service3); } else { ArrayList<UserEntry> users = new
 		 * ArrayList<UserEntry>();
 		 * 
 		 * UserEntry user2 = new UserEntry("Ghennadi", Offer.OFFER_ACCEPTED, new
 		 * Date().getTime() + 123456876, 22.4); UserEntry user1 = new
 		 * UserEntry("Paul", Offer.TRANSFER_STARTED, new Date().getTime() +
 		 * 223456876, 25.4); user1.setProgress(22); UserEntry user3 = new
 		 * UserEntry("Ana", Offer.OFFER_ACCEPTED, new Date().getTime() +
 		 * 323456876, 2.1); users.add(user1); users.add(user2);
 		 * users.add(user3);
 		 * 
 		 * Service service1 = new Service("service1", users, Status.ACTIVE);
 		 * service1.setProgress(22);
 		 * 
 		 * Service service2 = new Service("service2", null, Status.INACTIVE);
 		 * Service service3 = new Service("service3", null, Status.ACTIVE);
 		 * //services.add(service1); services.add(service2);
 		 * services.add(service3); }
 		 */
 
 		return services;
 	}
 
 	public void launchOffers(ArrayList<Service> services) {
 
 		for (Integer i = 0; i < services.size(); i++) {
 			Service service = services.get(i);
 
 			service.setLaunchOfferState();
 
 			services.set(i, service);
 		}
 
 		publishServices(services);
 	}
 
 	private Service parseLine(String line, ServiceType type) {
 		StringTokenizer st = new StringTokenizer(line, " ");
 		String name;
 		Long time;
 		Double price = 0.0;
 
 		if (!st.hasMoreTokens())
 			return null;
 		name = st.nextToken();
 
 		if (!st.hasMoreTokens())
 			return null;
 
 		try {
 			time = Long.parseLong(st.nextToken());
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 
 		if (type == ServiceType.SUPPLY) {
 			try {
 				price = Double.parseDouble(st.nextToken());
 			} catch (Exception e) {
 				e.printStackTrace();
 				return null;
 			}
 		}
 
 		if (st.hasMoreTokens())
 			return null;
 
 		return createService(name, time, price);
 	}
 
 	private ArrayList<Service> loadServicesFile(String filename, ServiceType type) {
 		ArrayList<Service> services = new ArrayList<Service>();
 
 		File demandsFile = new File(filename);
 		if (demandsFile.exists()) {
 			BufferedReader br = null;
 			try {
 				br = new BufferedReader(new FileReader(demandsFile));
 
 				String line;
 				while ((line = br.readLine()) != null) {
 					System.out.println(line);
 					Service d = parseLine(line, type);
 
 					/* TODO: wrong file format. */
 					services.add(d);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 			try {
 				if (br != null)
 					br.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		return services;
 	}
 
 	@Override
 	public void changeServiceNotify(Service service) {
 		putOffer(service);
 		gui.changeServiceNotify(service);
 	}
 
 	@Override
 	public void changeServicesNotify(ArrayList<Service> services) {
 		gui.changeServicesNotify(services);
 	}
 
 	@Override
 	public void changeProfileNotify(UserProfile profile) {
 		gui.changeProfileNotify(profile);
 	}
 
 	@Override
 	public void stopTransfer(Service service) {
 		net.stopTransfer(service);
 	}
 
 	@Override
 	public void publishService(Service service) {
 		// TODO Auto-generated method stub
 		System.out.println("Public aici in mediator");
 		net.publishService(service);
 		web.publishService(service);
 	}
 
 	@Override
 	public void publishServices(ArrayList<Service> services) {
 		net.publishServices(services);
 		web.publishServices(services);
 	}
 
 	public Service createService(String name, Long time, Double price) {
 		Service service = new Service(name);
 		service.setTime(time);
 		service.setPrice(price);
 
 		return service;
 	}
 
 	public void startTransfer(Service service) {
 		net.startTransfer(service);
 	}
 
 	@Override
 	public InetSocketAddress getNetworkAddress() {
 		return net.getAddress();
 	}
 	
 	@Override
 	public void notifyNetwork(Service service) {
 		net.publishService(service);
 	}
 }
