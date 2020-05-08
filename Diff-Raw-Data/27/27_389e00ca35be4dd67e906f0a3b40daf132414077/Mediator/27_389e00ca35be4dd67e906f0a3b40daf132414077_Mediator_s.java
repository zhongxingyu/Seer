 package app;
 
 import gui.GUIMediator;
 import gui.GUIMediatorImpl;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Vector;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JList;
 import javax.swing.table.TableModel;
 
 import org.apache.log4j.Logger;
 
 import network.NetworkMediator;
 import network.NetworkMockup;
 import wsc.WSClientMediator;
 import app.model.Seller;
 import app.model.User;
 import app.states.RequestTypes;
 import app.states.State;
 import app.states.StateManager;
 
 public class Mediator implements WSClientMediator {
 
 	private final String LOG_FILE_FORMAT = "%s.log";
 	private String configFile;
 	private ProductListModel products;
 	private StateManager mgr;
 	private NetworkMediator netMed;
 	private GUIMediator guiMed;
 	private Logger logger = null;
 	private String serverIp;
 	private int serverPort;
 	private boolean inStartupPhase;
 	
 	private HashMap<String, User> relevantUsers;
 
 	public Mediator(String ip, int port, String configFile) {
 		this.serverIp = ip;
 		this.serverPort = port;
 		this.configFile = configFile;
 		mgr = new StateManager(this);
 		netMed = new NetworkMockup(this);
 		guiMed = new GUIMediatorImpl(this);
 		this.inStartupPhase = true;
 
 		this.relevantUsers = new HashMap<String, User>();
 	}
 
 	public NetworkMediator getNetMed() {
 		return netMed;
 	}
 
 	public TableModel getProducts() {
 		return products;
 	}
 
 	public String getProduct(int row, int col) {
 		return (String) products.getValueAt(row, ProductListModel.PROD_COL);
 	}
 
 	public void updateProductsModel(String userName, String product, Integer price, int col) {
 		products.updateProductsModel(userName, product, price, col);
 	}
 
 	/**
 	 * Get a selected username within the users list at row @row.
 	 * 
 	 * @param row
 	 * @param index
 	 *            Index within list.
 	 * @return
 	 */
 	public String getUserInListCol(int row, int index) {
 		return (String) products.getValueFromListCol(row, index);
 	}
 
 	public HashMap<String, User> getRelevantUsers() {
 		return this.relevantUsers;
 	}
 
 	public void forgetRelevantUser(String userName) {
 		this.relevantUsers.remove(userName);
 	}
 
 	public String[] getUsersList(int row) {
 		return products.getUsersList(row);
 	}
 
 	public Integer[] getOffersList(String productName) {
 		return products.getOffersList(productName);
 	}
 
 	public void updateUsersList(String productName, Map<String, String> names) {
 		products.updateUsersList(productName, names);
 		guiMed.repaint();
 	}
 
 	/* Called for TransferFailed|Completed, OfferAccepted|Refused */
 	public void updateStatusList(String userName, String productName,
 			String status) {
 
 		products.setStatus(userName, productName, status);
 		guiMed.repaint();
 	}
 
 	/* Called for TransferStarted|InProgress, OfferMade, OfferExceeded */
 	public void updateStatusList(String userName, String productName, int value,
 			String status) {
 
 		switch (status) {
 		case State.STATE_TRANSFERS:
 		case State.STATE_TRANSFERP:
 			//TODO process value and don't break! (setStatus first)
 		case State.STATE_OFFERMADE:
 		case State.STATE_OFFERE:
 			products.setStatus(userName, productName, value, status);
 			break;
 		default:
 			return;
 		}
 		products.setStatus(userName, productName, status);
 		guiMed.repaint();
 	}
 
 	/**
 	 * Initialize graphical transfer, which means to configure the size of transfer.
 	 * 
 	 * @param userName source of the transfer
 	 * @param productName product/service being transfered
 	 * @param value size of the transfer
 	 */
 	public void initTransfer(String userName, String productName, int value) {
 		products.setStatus(userName, productName, State.STATE_TRANSFERS);
 		products.initTransfer(userName, productName, value);
 		guiMed.repaint();
 	}
 
 	/**
 	 * Update transfer status.
 	 * 
 	 * @param userName source of the transfer
 	 * @param productName product/service being transfered
 	 * @param value size of the received chunk
 	 */
 	public void transfer(String userName, String productName, int value) {
 		products.setStatus(userName, productName, State.STATE_TRANSFERP);
 		products.updateTransfer(userName, productName, value);
 		guiMed.repaint();
 	}
 
 	/* GUI related methods */
 	public GUIMediator getGuiMed() {
 		return guiMed;
 	}
 
 	public void removeUserFromList(String username) {
 		products.removeUserFromList(username);
 		guiMed.repaint();
 	}
 
 	public void removeUserFromList(String username, String product) {
 		products.removeUserFromList(username, product);
 		guiMed.repaint();
 	}
 
 	public int addUserToList(String username, String product) {
 		int index = products.addUserToList(username, product);
 		guiMed.repaint();
 		return index;
 	}
 
 	public boolean register(String username) {
 		// TODO register to database
 		return true;
 	}
 
 	public boolean logIn(String username, String password, String type) {
 		
 		String logFile = String.format(LOG_FILE_FORMAT, username);
 		System.setProperty("logfile.name", logFile);
 		logger = Logger.getLogger(Mediator.class.getName());
 		logger.info(String.format("Welcome to Auction House, %s!", username));
 
 		if (type.equalsIgnoreCase(User.buyerType))
 			mgr.setBuyerState();
 		else
 			mgr.setSellerState();
 		
 		Vector<String> products = tempReadConfig(username, type);
 		
 		mgr.login(username, products);
 		
 		System.out.println("LOADING PRODLIST");
 		loadInitialProdList(username, type, products);
 		
 		if (!netMed.validateUsername(username, password, type,
 									 serverIp, serverPort, products)) {
 			
 			System.out.println("[Mediator]: Login Authentication failed!");
 			return false;
 		}
 		
 		return true;
 	}
 
 	private Vector<String> tempReadConfig(String user, String type) {
 		Vector<String> products = null;
 		File f = new File(configFile);
 		Scanner s = null;
 
 		try {
 			s = new Scanner(f);
 			s.nextLine(); //username
 			s.nextLine(); //password
 			s.nextLine(); //type
 
 			/* Read the list of products. */
 			products = new Vector<String>();
 			while (s.hasNextLine()) {
 				String line = s.nextLine();
 				products.add(line);
 			}
 
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} finally {
 			if (s != null)
 				s.close();
 		}
 
 		return products;
 	}
 
 	public boolean logOut() {
 		// TODO send the logout over the network
 		return mgr.logout();
 	}
 
 	public String getUserName() {
 		return mgr.getUserName();
 	}
 
 	public Map<String, Command> getServiceMenuItems(int row, String name) {
 		if (name == null)
 			if (products.hasStatus(row, State.STATE_INACTIVE))
 				return mgr.getServiceMenuItems(State.STATE_INACTIVE);
 		return mgr.getServiceMenuItems(products.getStatus(row, name));
 	}
 
 	public Map<String, Command> getContextMenuItems(int row, int listIndex) {
 		if (listIndex < 0)
 			return null;
 		return mgr.getContextualMenuItems(products.getStatus(row, listIndex));
 	}
 
 	/* Web Service Client related methods */
 
 	/**
 	 * Load initial configurations for a user and return 0 for success.
 	 * 
 	 * Also initiate logger here, after username is known.
 	 * 
 	 * @param username
 	 * @param type
 	 * @param products
 	 * @return 0 for success, -1 if type is unknown
 	 */
 	@Override
 	public int loadInitialProdList(String username, String type,
 			List<String> products) {
 
 		Object[][] productsInfo;
 
 		productsInfo = new Object[products.size()][mgr.getColumnCount()];
 
 		for (int i = 0, n = products.size(); i < n; i++) {
 			String prodName = products.get(i);
 			productsInfo[i][ProductListModel.INDEX_COL] = i;
 			productsInfo[i][ProductListModel.PROD_COL] = prodName;
 			DefaultListModel<String> statusModel = new DefaultListModel<String>();
 			statusModel.addElement(State.STATE_INACTIVE);
 			productsInfo[i][ProductListModel.STATUS_COL] = new JList<String>(
 					statusModel);
 			productsInfo[i][ProductListModel.LIST_COL] = new JList<String>(
 					new DefaultListModel<String>());
 			productsInfo[i][ProductListModel.OFFER_COL] = new JList<Integer>(
 					new DefaultListModel<Integer>());
 		}
 
 		ProductListModel.columnNames[ProductListModel.LIST_COL] += mgr.getListName();
 		this.products = new ProductListModel(productsInfo);
 		mgr.updateColumns(this.products);
 
 		return 0;
 	}
 
 	public String getListName() {
 		return mgr.getListName();
 	}
 
 	/**
 	 * 
 	 * 
 	 * @param product
 	 * @param users
 	 */
 	public void handleLoginEvent(String product, User user) {
 		
 		if (!relevantUsers.containsKey(user.getName()))
 			relevantUsers.put(user.getName(), user);
 			
 		addUserToList(user.getName(), product);
 	}
 
 	public boolean handleLogoutEvent(String name) {
 
 		return true;
 	}
 
 	public boolean updateGui(int action, String name, String product, int price) {
 		return mgr.receiveStatusUpdate(action, name, product, price);
 	}
 
 	public boolean startupPhase() {
 		return inStartupPhase;
 	}
 	
 	public void fetchRelevantUsers(String product) {	
 		inStartupPhase = false;
 
 		Seller seller = new Seller(mgr.getUserName(), this.serverIp, this.serverPort,
 				new Vector<String>());
 
 		seller.getProducts().add(product);
 
 		if (!getNetMed().fetchRelevantUsers(seller))
 			logger.error("Failed to issue current user list refresh");
 	}
 
 	/* 
 	 * TODO: prevents AcceptOffer (buyer PoV) from sending
 	 * DROP_OFFER messages to ALL potential sellers
 	 */
 	public boolean hasMadeOffer(User seller) {
 		return true;
 	}
 	
 	/*
 	 * when Buyer refuses lowest bid :-), all other auction participants
 	 * must be updated with the new lowest bid
 	 */
 	public boolean hasHighestBid(User seller, String product) {
 		Integer price = (Integer)
 				products.getValueFromCol(seller.getName(), product, ProductListModel.OFFER_COL);
 		if (price == null)
 			return false;
 
 		int minOffer = price;
 		Integer[] offers = getOffersList(product);
 		if (offers != null) {
 			for (Integer offer : offers)
 				if (offer != null)
 					minOffer = (minOffer > offer) ? offer : minOffer;
 			if (minOffer > -1 && price < minOffer)
 				return true;
 		}
 		return false;
 	}
 	
 	public int getBestOffer(String product) {
 		int bestOffer = Integer.MAX_VALUE;
 		
 		for (Map.Entry<String, User> e : relevantUsers.entrySet()) {
 			int bid = e.getValue().getBid(product);
 			
 			if (bid < bestOffer)
 				bestOffer = bid;
 		}
 		
 		return bestOffer;
 	}
 	
 	/**
 	 * Interprets the nature of the user action, determines the required
 	 * destinations and forwards the request to the network module
 	 * 
 	 * @param action - GUI action submitted by the user
 	 * @param userName - sender/receiver userName, depends on action type
 	 * @param product - auctioned product
 	 * @param price - price value, depends on action type
 	 */
 	public void sendNotifications(int action, String userName, String product, int price) {
 
 		Vector<User> destinations = new Vector<User>();
 
 		/* here we build the destinations depending on the given command */
 		switch (action) {
 		case RequestTypes.REQUEST_LAUNCH_OFFER:
 		case RequestTypes.REQUEST_DROP_OFFER:
 			logger.debug("Sending offer request notification");
 			for (Map.Entry<String, User> entry : relevantUsers.entrySet()) {
 				if (entry.getValue().getProducts().contains(product)) {
 					
 					destinations.add(entry.getValue());
 				}
 			}
 			break;
 
 		/* assumes it can logically be called (we don't have the highest bid) */
 		case RequestTypes.REQUEST_DROP_AUCTION:
 			for (Map.Entry<String, User> entry : relevantUsers.entrySet()) {
 				if (entry.getValue().getName().equals(userName)) {
 
 					destinations.add(entry.getValue());
 					break;
 				}
 			}
 			break;
 
 		/* 
 		 * two-way send command:
 		 * seller->buyer && buyer->notifies all other sellers if best bid made
 		 */
 		case RequestTypes.REQUEST_MAKE_OFFER:
 			destinations = mgr.computeDestinations(action, userName, product, price);
 
 			if (!netMed.sendNotifications(action, mgr.getUserName(), product, price, destinations)) {
 				logger.debug("Failed to send network Notifications!");
 			}
 
 			return;
 
 		/*
 		 * accept offer :  send ACCEPT_OFFER to the winner
 		 *  (buyer pov)         DROP_OFFER to the other participants
 		 */
 		case RequestTypes.REQUEST_ACCEPT_OFFER:
 
 			Vector<User> otherDestinations = new Vector<User>();
 			boolean found = false;
 		
 			for (Map.Entry<String, User> entry : relevantUsers.entrySet()) {
 				User seller = entry.getValue();
 				
 				if (!found && seller.getName().equals(userName)) {
 
 					destinations.add(seller);
 					found = true;
 					continue;
 				}
 				
 				if (seller.getProducts().contains(product)) {
 				
 					otherDestinations.add(seller);
 				}
 			}
 			
 			if (!netMed.sendNotifications(action, mgr.getUserName(),
 											product, price, destinations)) {
 				logger.debug("Failed to send network Notifications!");
 			}
 			
 			/* other auction participants simply receive a "DROP_OFFER" message */
 			if (!netMed.sendNotifications(RequestTypes.REQUEST_DROP_OFFER,
 							mgr.getUserName(), product, price, otherDestinations)) {
 				logger.debug("Failed to send network Notifications!");
 			}
 
 			return;
 		/*
 		 * start transfer :  send INITIAL_TRANSFER to the winner
 		 *  (buyer pov)         DROP_AUCTION to the other participants
 		 */
 		case RequestTypes.REQUEST_INITIAL_TRANSFER:
 			otherDestinations = new Vector<User>();
 			found = false;
 		
 			for (Map.Entry<String, User> entry : relevantUsers.entrySet()) 
 				if (!entry.getKey().equals(userName)) {
 					User seller = entry.getValue();
 	
 					if (seller.getProducts().contains(product)) {
 					
 						otherDestinations.add(seller);
 					}
 				}
 			/* Start transfer with the winner. */
 			doProductTransfer(userName, product);
 			
 			/* other auction participants simply receive a "DROP_AUCTION" message */
 			if (!netMed.sendNotifications(RequestTypes.REQUEST_DROP_AUCTION,
 							mgr.getUserName(), product, price, otherDestinations)) {
 				logger.debug("Failed to send network Notifications!");
 			}
 
 			return;
 
 		case RequestTypes.REQUEST_REFUSE_OFFER:
 			User seller = relevantUsers.get(userName);
 			if (seller != null) {
 				destinations.add(seller);
 
 				if (hasHighestBid(seller, product)) {
 					otherDestinations = new Vector<User>();						
 					for (Map.Entry<String, User> e : relevantUsers.entrySet()) {
 						seller = e.getValue();
 						if (seller.getProducts().contains(product) && hasMadeOffer(seller)) {
 						
 							otherDestinations.add(seller);
 						}
 					}
 
 					/* 
 					 * if the best bid was refused, we refresh other sellers with
 					 * a "REQUEST_LAUNCH_OFFER" message
 					 */
 					if (!netMed.sendNotifications(RequestTypes.REQUEST_LAUNCH_OFFER,
 								mgr.getUserName(), product, getBestOffer(product), otherDestinations)) {
 						logger.debug("Failed to send network Notifications!");
 					}
 				}
 				if (!netMed.sendNotifications(action, getUserName(), product, price, destinations)) {
 					logger.debug("Failed to send network Notifications!");
 				}
 			}
 			
 			break;
 		}
 		
 		if (!netMed.sendNotifications(action, userName, product, price, destinations)) {
 			logger.debug("Failed to send network Notifications!");
 		}
 	}
 
 	/**
 	 * Attempts to transfer a product to the buyer which requested the auction
 	 * 
 	 * @param buyerName - name of the buyer
 	 * @param product - name/type of the involved product
 	 */
 	public void doProductTransfer(String buyerName, String product) {
 		
 		Vector<User> destinations = new Vector<User>();
 
 		destinations.add(relevantUsers.get(buyerName));
 
 		netMed.sendNotifications(RequestTypes.REQUEST_INITIAL_TRANSFER,
 				buyerName, product, 0, destinations);
 	}
 
 	public void sendNotifications(int action, String userName, String product) {
 		logger.debug("Sending notification for product " + product);
 		sendNotifications(action, userName, product, 0);
 	}
 }
