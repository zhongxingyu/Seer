 package app;
 
 import gui.GUIMediator;
 import gui.GUIMediatorImpl;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Vector;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JList;
 import javax.swing.table.TableModel;
 
 import network.NetworkMediator;
 import network.NetworkMediatorImpl;
 import network.NetworkServer;
 
 import org.apache.log4j.Logger;
 
 import wsc.WSClientMediator;
 import wsc.WSClientMediatorImpl;
 import app.model.Seller;
 import app.model.User;
 import app.states.RequestTypes;
 import app.states.State;
 import app.states.StateManager;
 
 public class Mediator {
 
 	private final String LOG_FILE_FORMAT = "%s.log";
 	private String configFile;
 	private ProductListModel products;
 	private StateManager mgr;
 	private NetworkMediator netMed;
 	private GUIMediator guiMed;
 	private WSClientMediator wscMed;
 	private Logger logger = null;
 	private String serverIp;
 	private int serverPort;
 
 	public Mediator(String url, String ip, int port, String configFile) {
 		this.serverIp = ip;
 		this.serverPort = port;
 		this.configFile = configFile;
 		mgr = new StateManager(this);
 		netMed = new NetworkMediatorImpl(this);
 		guiMed = new GUIMediatorImpl(this);
 		wscMed = new WSClientMediatorImpl(url, this);
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
 		guiMed.repaint();
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
 
 	public Map<String, User> getRelevantUsers() {
 		return wscMed.getRelevantUsers();
 	}
 
 	public Map<String, User> getRelevantUsers(String product) {
 		return wscMed.getRelevantUsers(product);
 	}
 
 	synchronized public void forgetRelevantUser(String userName) {
 		wscMed.removeRelevant(userName);
 	}
 
 	public String[] getUsersList(int row) {
 		return products.getUsersList(row);
 	}
 
 	public Integer[] getOffersList(String productName) {
 		return products.getOffersList(productName);
 	}
 
 	public Object getValueFromCol(String userName, String productName, int col) {
 		return products.getValueFromCol(userName, productName, col);
 	}
 
 	public void resetStatus() {
 		products.resetState();
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
 			products.setStatus(userName, productName, status);
 			break;
 		case State.STATE_OFFERMADE:
 			mgr.checkBestOffer(userName, productName, value);
 		case State.STATE_OFFERE:
 			products.setStatus(userName, productName, value, status);
 			break;
 		default:
 			return;
 		}
 
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
 		logger.debug("Init transfer: transfer started status");
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
 		int full = products.updateTransfer(userName, productName, value);
 		if (full == 1)
 			products.setStatus(userName, productName, State.STATE_TRANSFERC);
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
 
 	/*	register to database*/
 	public boolean register(String username, String pass, String type) {
 		String logFile = String.format(LOG_FILE_FORMAT, username);
 		System.setProperty("logfile.name", logFile);
 		System.out.println("LOADING PRODLIST");
 		this.initLogger();
 
 		Vector<String> products = tempReadConfig(username, type);
 		if (products == null || products.size() == 0)
 			return false;
 		StringBuilder prods = new StringBuilder();
 		Iterator<String> it = products.iterator();
 		while (it.hasNext())
 			prods.append(it.next() + ",");
 		int len = prods.length();
 		String prodList = prods.substring(0, len-1);
 		System.out.println(String.format("Register user %s with product list\n\t%s", 
 				username, prodList));
 		return wscMed.register(username, pass, type, prodList);
 	}
 
 	public void initLogger() {
 		if (logger != null)
 			return;
 		logger = Logger.getLogger(Mediator.class.getName());
 		netMed.initLogger();
 		wscMed.initLogger();
 		NetworkServer.initLogger();
 	}
 
 	public boolean logIn(String username, String password, String type) {
 
 		String logFile = String.format(LOG_FILE_FORMAT, username);
 		System.setProperty("logfile.name", logFile);
 		initLogger();
 		logger.info(String.format("Welcome to Auction House, %s!", username));
 
 		if (type.equalsIgnoreCase(User.buyerType))
 			mgr.setBuyerState();
 		else
 			mgr.setSellerState();
 
 		Vector<String> products = wscMed.logIn(username, password, type, serverIp, serverPort);
 		if (products == null) {
 			logger.fatal("[Mediator]: Login Authentication failed!");
 			return false;
 		}
 		createTableModel(username, type, products);
 		mgr.login(username, serverIp, serverPort, products);
 
 		return true;
 	}
 
 	private Vector<String> tempReadConfig(String user, String type) {
 		Vector<String> products = null;
 		File f = new File(configFile);
 		Scanner s = null;
 
 		try {
 			s = new Scanner(f);
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
 
 	// TODO: test and clean up
 	public boolean logOut() {
 		boolean allowed = mgr.logout();
 		if (allowed)
 			//netMed.userLogOut(mgr.getUser());
 			wscMed.logOut(mgr.getUserName());
 		return allowed;
 	}
 
 	public String getUserName() {
 		return mgr.getUserName();
 	}
 
 	public String getListenIp() {
 		return serverIp;
 	}
 
 	public int getListenPort() {
 		return serverPort;
 	}
 
 	/**
 	 * @param row
 	 * @param name
 	 * @return an asscociation on menu entry name and command to be executed on click.
 	 */
 	public Map<String, Command> getServiceMenuItems(int row, String name) {
 		if (name == null)
 			if (products.hasStatus(row, State.STATE_INACTIVE) ||
 					products.hasStatus(row, State.STATE_TRANSFERC))
 				return mgr.getServiceMenuItems(State.STATE_INACTIVE);
 		return mgr.getServiceMenuItems(products.getStatus(row, name));
 	}
 
 	public Map<String, Command> getContextMenuItems(int row, int listIndex) {
 		if (listIndex < 0)
 			return null;
 		return mgr.getContextualMenuItems(products.getStatus(row, listIndex));
 	}
 
 	/**
 	 * Create table model, i.e. what the user would see after log in.
 	 * 
 	 * @param username
 	 * @param type
 	 * @param products
 	 */
 	public void createTableModel(String username, String type,
 			Vector<String> products) {
 
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
 	}
 
 	public String getListName() {
 		return mgr.getListName();
 	}
 
 	/**
 	 * 
 	 * 
 	 * @param product
 	 * @param user
 	 */
 	public void handleLoginEvent(String product, User user) {
 		wscMed.addRelevant(user);
 
 		addUserToList(user.getName(), product);
 		guiMed.repaint();
 	}
 
 	public boolean handleLogoutEvent(String name) {
 
 		return true;
 	}
 
 	public boolean updateGui(int action, String name, String product, int price) {
 		return mgr.receiveStatusUpdate(action, name, product, price);
 	}
 
 	public void postGUIInit() {
 		mgr.postGUIInit();
 	}
 
 	/**
 	 * Notifies a new system seller of any currently open offer requests
 	 * 
 	 * @param seller - newly logged in seller
 	 */
 	public void handleNewSystemSeller(User seller) {
 
 		Vector<String> sellerProds = seller.getProducts();
 
 		Vector<User> destinations = new Vector<User>();
 		destinations.add(seller);
 
 		logger.debug("Handling new system seller: " + seller);
 
 		for (String prod : mgr.getProducts()) {
 
 			String status = products.getStatus(prod);
 
 			if (sellerProds.contains(prod)  && (status.equals(State.STATE_NOOFFER) ||
 					status.equals(State.STATE_OFFERREF))) {
 
 				System.out.println("Notifying open auction for " + prod);
 				netMed.sendNotifications(RequestTypes.REQUEST_LAUNCH_OFFER,
 						mgr.getUserName(), this.serverIp,
 						this.serverPort, prod, destinations);
 
 				addUserToList(seller.getName(), prod);
 			}
 		}
 
 		wscMed.addRelevant(seller);
 		logger.debug("Relevant users: " + wscMed.getRelevantUsers());
 
 		guiMed.repaint();
 	}
 
 	/* TODO: verify GUI part */
 	public void saveUserConnectInfo(String userName, String product, String ip, int port) {
 		User u;
 		Map<String, User> relevantUsers = wscMed.getRelevantUsers();
 		u = relevantUsers.get(userName);
 		if (u == null) {
 			Seller seller = new Seller(userName, ip, port);
 			seller.getProducts().add(product);
 
 			relevantUsers.put(userName, seller);
 
 			addUserToList(userName, product);
 			guiMed.repaint();
 			return;
 		}
 		if (!u.getProducts().contains(product)) {
 			u.getProducts().add(product);
 
 			addUserToList(userName, product);
 			guiMed.repaint();
 		}
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
 
 		Map<String, User> relevantUsers;
 		Vector<User> destinations = new Vector<User>();
 		String userInPackage = userName;
 
 		/* here we build the destinations depending on the given command */
 		switch (action) {
 		case RequestTypes.REQUEST_LAUNCH_OFFER:
 		case RequestTypes.REQUEST_DROP_OFFER:
 			logger.debug("Sending offer request notification");
 			relevantUsers = wscMed.getRelevantUsers(product);
 			for (Map.Entry<String, User> entry : relevantUsers.entrySet()) {
 				logger.debug("Launch offer: dest " + entry.getValue());
 				destinations.add(entry.getValue());
 			}
 
 			if (!netMed.sendNotifications(action, userName, this.serverIp,
 					this.serverPort, product, destinations)) {
 				logger.debug("Failed to send network Notifications!");
 			}
 
 			return;
 
 		case RequestTypes.REQUEST_RELEVANT_BUYERS:
 			if (!netMed.sendNotifications(action, userName, this.serverIp,
 					this.serverPort, product, destinations)) {
 				logger.debug("Failed to send network Notifications!");
 			}
 
 			return;
 
 		/* assumes it can logically be called (we don't have the highest bid) */
 		case RequestTypes.REQUEST_DROP_AUCTION:
 			User user = wscMed.getRelevantUsers().get(userName);
 			if (user == null)
 				return;
 
 			destinations.add(user);
 			userInPackage = getUserName();
 			break;
 
 		case RequestTypes.REQUEST_LOGOUT:
 			logger.debug("Send log out notification");
 			relevantUsers = wscMed.getRelevantUsers();
 			for (Map.Entry<String, User> entry : relevantUsers.entrySet()) {
 				User u = entry.getValue();
 				netMed.sendLoginNotification(RequestTypes.REQUEST_LOGOUT,
 						u.getIp(), u.getPort(), mgr.getUser());
				return;
 			}
 
 		/*
 		 * two-way send command:
 		 * seller->buyer && buyer->notifies all other sellers if best bid made
 		 */
 		case RequestTypes.REQUEST_MAKE_OFFER:
 			logger.debug("Made offer: " + price);
 			destinations = mgr.computeDestinations(action, userName, product, price);
 
 			logger.debug("Sending MAKE_OFFER to " + destinations.size() + " users!");
 
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
 			relevantUsers = wscMed.getRelevantUsers();
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
 
 			logger.debug(String.format("ACCEPT (%s) and DROP (%s)",
 					destinations.toString(), otherDestinations.toString()));
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
 			relevantUsers = wscMed.getRelevantUsers(product);
 			for (Map.Entry<String, User> entry : relevantUsers.entrySet()) 
 				if (!entry.getKey().equals(userName)) {
 					User seller = entry.getValue();
 					otherDestinations.add(seller);
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
 			relevantUsers = wscMed.getRelevantUsers();
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
 				}
 				if (!netMed.sendNotifications(action, getUserName(), product, price, destinations)) {
 					logger.debug("Failed to send network Notifications!");
 				}
 			}
 			break;
 		}
 
 		if (!netMed.sendNotifications(action, userInPackage, product, price, destinations)) {
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
 		User user = wscMed.getRelevantUsers().get(buyerName);
 
 		if (user == null)
 			return;
 		destinations.add(user);
 
 		netMed.sendNotifications(RequestTypes.REQUEST_INITIAL_TRANSFER,
 				buyerName, product, 0, destinations);
 	}
 
 	public void sendNotifications(int action, String userName, String product) {
 		logger.debug("Sending notification for product " + product);
 		sendNotifications(action, userName, product, 0);
 	}
 }
