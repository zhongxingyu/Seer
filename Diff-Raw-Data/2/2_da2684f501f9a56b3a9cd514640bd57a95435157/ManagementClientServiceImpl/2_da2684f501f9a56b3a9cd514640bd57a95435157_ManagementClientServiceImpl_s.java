 /**
  * 
  */
 package at.ac.tuwien.dslab2.service.managementClient;
 
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.NoSuchElementException;
 import java.util.Properties;
 import java.util.Scanner;
 import java.util.SortedSet;
 import java.util.concurrent.ConcurrentSkipListSet;
 
 import at.ac.tuwien.dslab2.domain.Bill;
 import at.ac.tuwien.dslab2.domain.Event;
 import at.ac.tuwien.dslab2.domain.PriceSteps;
 import at.ac.tuwien.dslab2.service.PropertiesService;
 import at.ac.tuwien.dslab2.service.PropertiesServiceFactory;
 import at.ac.tuwien.dslab2.service.analyticsServer.AnalyticsServer;
 import at.ac.tuwien.dslab2.service.billingServer.BillingServer;
 import at.ac.tuwien.dslab2.service.billingServer.BillingServerSecure;
 import at.ac.tuwien.dslab2.service.rmi.RMIClientService;
 import at.ac.tuwien.dslab2.service.rmi.RMIServiceFactory;
 
 /**
  * @author klaus
  * 
  */
 class ManagementClientServiceImpl implements ManagementClientService {
 	private final RMIClientService rcs;
 	private final BillingServer bs;
 	private final AnalyticsServer as;
 	private BillingServerSecure bss;
 	private volatile boolean auto;
 	private final SortedSet<Event> events;
 	private volatile Event latestPrintedEvent;
 	private SubscriptionListener listener;
 	private final MgmtClientCallback callback;
 
 	public ManagementClientServiceImpl(String analyticsServerRef,
 			String billingServerRef) throws IOException {
 		auto = false;
 		latestPrintedEvent = null;
 		events = new ConcurrentSkipListSet<Event>();
 		callback = new MgmtClientCallbackImpl();
 		UnicastRemoteObject.exportObject(callback, 0);
 
 		/*
 		 * Read the properties file
 		 */
 		Properties prop = PropertiesServiceFactory.getPropertiesService()
 				.getRegistryProperties();
 
 		// Parse value
 		int port;
 		String host;
 		host = prop.getProperty(PropertiesService.REGISTRY_PROPERTIES_HOST_KEY);
 
 		Scanner sc = new Scanner(
 				prop.getProperty(PropertiesService.REGISTRY_PROPERTIES_PORT_KEY));
 		if (!sc.hasNextInt()) {
 			throw new IOException("Couldn't parse the properties value of "
 					+ PropertiesService.REGISTRY_PROPERTIES_PORT_KEY);
 		}
 		port = sc.nextInt();
 
 		/*
 		 * Get the RMI interfaces
 		 */
 		this.rcs = RMIServiceFactory.newRMIClientService(host, port);
 		this.as = (AnalyticsServer) this.rcs.lookup(analyticsServerRef);
 		this.bs = (BillingServer) this.rcs.lookup(billingServerRef);
 	}
 
 	@Override
 	public void login(String userName, String password)
 			throws AlreadyLoggedInException, RemoteException {
 		if (userName == null)
 			throw new IllegalArgumentException("user name is null");
 		if (password == null)
 			throw new IllegalAccessError("password is null");
 
 		if (this.bss != null)
 			throw new AlreadyLoggedInException();
 
 		this.bss = this.bs.login(userName, password);
 	}
 
 	@Override
 	public PriceSteps steps() throws LoggedOutException, RemoteException {
 		if (this.bss == null)
 			throw new LoggedOutException();
 
 		return this.bss.getPriceSteps();
 	}
 
 	@Override
 	public void addStep(double startPrice, double endPrice, double fixedPrice,
 			double variablePricePercent) throws LoggedOutException,
 			RemoteException {
 		if (this.bss == null)
 			throw new LoggedOutException();
 
 		this.bss.createPriceStep(startPrice, endPrice, fixedPrice,
 				variablePricePercent);
 	}
 
 	@Override
 	public void removeStep(double startPrice, double endPrice)
 			throws LoggedOutException, RemoteException {
 		if (this.bss == null)
 			throw new LoggedOutException();
 
 		this.bss.deletePriceStep(startPrice, endPrice);
 	}
 
 	@Override
 	public Bill bill(String userName) throws LoggedOutException,
 			RemoteException {
 		if (this.bss == null)
 			throw new LoggedOutException();
 
 		return this.bss.getBill(userName);
 	}
 
 	@Override
 	public void logout() throws LoggedOutException {
		if (this.bss != null)
 			throw new LoggedOutException();
 
 		this.bss = null;
 	}
 
 	@Override
 	public long subscribe(String regex) throws RemoteException {
 		if (regex == null)
 			throw new IllegalArgumentException("regex is null");
 
 		return this.as.subscribe(regex, callback);
 	}
 
 	@Override
 	public void unsubscribe(long id) throws RemoteException {
 		this.as.unsubscribe(id);
 	}
 
 	@Override
 	public void setSubscriptionListener(SubscriptionListener listener) {
 		this.listener = listener;
 	}
 
 	@Override
 	public SortedSet<Event> print() {
 		SortedSet<Event> returnSet;
 		if (latestPrintedEvent == null) {
 			returnSet = events.tailSet(latestPrintedEvent);
 		} else {
 			returnSet = events;
 		}
 		if (returnSet.isEmpty())
 			return null;
 
 		latestPrintedEvent = returnSet.last();
 		return returnSet;
 	}
 
 	@Override
 	public void auto() {
 		this.auto = true;
 
 		try {
 			latestPrintedEvent = events.last();
 		} catch (NoSuchElementException e) {
 			// Ignore if the event list is empty
 		}
 	}
 
 	@Override
 	public void hide() {
 		this.auto = false;
 	}
 
 	@Override
 	public void close() throws IOException {
 		UnicastRemoteObject.unexportObject(callback, false);
 		if (rcs != null)
 			rcs.close();
 	}
 
 	private final class MgmtClientCallbackImpl implements MgmtClientCallback {
 
 		@Override
 		public void processEvent(Event event) throws RemoteException {
 			events.add(event);
 
 			if (auto && listener != null) {
 				listener.autoPrintEvent(print());
 			}
 		}
 
 	}
 
 }
