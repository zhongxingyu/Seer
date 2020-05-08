 package app.states;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JList;
 
 import org.apache.log4j.Logger;
 
 import app.*;
 import app.model.Buyer;
 import app.model.Seller;
 import app.model.User;
 
 public class SellerState extends State {
 	public static final int BEST_OFFER_COL = ProductListModel.OFFER_COL + 1;
 	public static final String BEST_OFFER_COL_NAME = "Best Offer";
 	private Logger logger = null;
 
 	public SellerState(Mediator med) {
 		super(med);
 
 		actions = new Vector<Integer>();
 
 		actions.add(RequestTypes.REQUEST_MAKE_OFFER);
 		actions.add(RequestTypes.REQUEST_DROP_AUCTION);
 		actions.add(RequestTypes.REQUEST_INITIAL_TRANSFER);
 		actions.add(RequestTypes.REQUEST_TRANSFER);
 	}
 
 	@Override
 	public String getListName() {
 		return User.buyerType;
 	}
 
 	@Override
 	public void login(String username, Vector<String> products) {
 		logger = Logger.getLogger(SellerState.class.getName());
 		user = new Seller(username, products);
 		logger.debug("Created seller " + username);
 	}
 
 	@Override
 	public boolean logout() {
 		if (allowedLogout()) {
 			med.sendNotifications(RequestTypes.REQUEST_LOGOUT, getUserName(), "");
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * A Seller doesn't have a service menu.
 	 */
 	@Override
 	public Map<String, Command> getServiceMenuItems(String status) {
 		return null;
 	}
 
 	/**
 	 * A Seller may either make an offer or drop auction if its offer was over
 	 * rated.
 	 */
 	@Override
 	public Map<String, Command> getContextualMenuItems(String status) {
 		Map<String, Command> items = null;
 
 		if (status.equals(STATE_NOOFFER) || status.equals(STATE_OFFERREF)
 				|| status.equals(STATE_OFFERE)) {
 			items = new HashMap<String, Command>();
 			items.put("Drop auction", new DropAuctionComm(med));
 			items.put("Make offer", new MakeOfferComm(med));
 		}
 
 		return items;
 	}
 
 	public void postGUIInit() {
 		med.fetchRelevantBuyers();
 	}
 	
 	/* Update the GUI with received info from network _on the EDT_ !! */
 	public boolean receiveStatusUpdate(int action, String userName, String product,
 			int price) {
 
 		switch (action) {
 		case RequestTypes.REQUEST_LAUNCH_OFFER:
 			if (med.addUserToList(userName, product) > -1)
 				med.updateProductsModel(userName, product, null, BEST_OFFER_COL);
 			break;
 		case RequestTypes.REQUEST_DROP_OFFER:
 			med.updateProductsModel(userName, product, null, BEST_OFFER_COL);
 			med.removeUserFromList(userName, product);
 			checkBestOffer(userName, product, -1);
 			break;
 		case RequestTypes.REQUEST_MAKE_OFFER:
 			logger.debug("Best offer update " + price);
 			med.updateProductsModel(userName, product, price, BEST_OFFER_COL);
 			break;
 		case RequestTypes.REQUEST_ACCEPT_OFFER:
 			med.updateStatusList(userName, product, State.STATE_OFFERACC);
 			med.sendNotifications(RequestTypes.REQUEST_INITIAL_TRANSFER, userName, product);
 			break;
 		case RequestTypes.REQUEST_REFUSE_OFFER:
 			logger.debug("Seller was refused for product " + product + " by" + userName);
 			med.updateStatusList(userName, product, State.STATE_OFFERREF);
 			break;
 		case RequestTypes.REQUEST_INITIAL_TRANSFER:
 			med.initTransfer(userName, product, price);
 			break;
 		case RequestTypes.REQUEST_TRANSFER:
 			med.transfer(userName, product, price);
 			break;
 		case RequestTypes.REQUEST_LOGOUT:
 			med.removeUserFromList(userName);
 			med.forgetRelevantUser(userName);
 			break;
 		default:
 			logger.debug("Invalid action: " + action);
 			return false;
 		}
 
 		return true;
 	}
 	
 	public User createUser(String name, String ip, int port, Vector<String> products) {
 		return new Buyer(name, ip, port, products);
 	}
 	
 	/* Network related */
 	public Vector<User> computeDestinations(int action, String userName, String product, int price) {
 		Vector<User> destinations = new Vector<User>();
 		
 		System.out.print("[Mediator]: Searching for user " + userName + "... ");
 		User buyer = med.getRelevantUsers().get(userName);
 		if (buyer != null)
 			destinations.add(buyer);
 		
 		logger.warn(destinations.size() > 0 ? "found!" : "not found!");
 		
 		return destinations;
 	}
 
 	/* GUI related */
 	public int getColumnCount() {
 		return BEST_OFFER_COL + 1;
 	}
 	/**
 	 * Add column for displaying the offer this seller made for each buyer in the list.
 	 */
 	public void updateColumns(ProductListModel model) {
 		Object[] columnData = new Object[model.getRowCount()];
 		for (int i = 0, n = model.getRowCount(); i < n; i++) {
 			DefaultListModel<Integer> colModel = new DefaultListModel<Integer>();
 			columnData[i] = new JList<Integer>(colModel);
 		}
 		model.addColumn(BEST_OFFER_COL_NAME, columnData);
 	}
 
 	/* Update BEST_OFFER_COL if needed. */
 	public void checkBestOffer(String userName, String productName, int value) {
 		if (value == -1)
 			med.updateProductsModel(userName, productName, null, BEST_OFFER_COL);
 		else {
 			Integer bestOffer = (Integer)med.getValueFromCol(userName, productName, BEST_OFFER_COL);
 			if (bestOffer == null || bestOffer > value)
 				/* The best offer is the one which favors the buyer. */
 				med.updateProductsModel(userName, productName, value, BEST_OFFER_COL);
 		}
 	}
 
 	/**
 	 * A Seller, S, may log out either if S's offers are not available anymore
 	 * or no transfer is in progress.
 	 */
 	@SuppressWarnings("unchecked")
 	public boolean allowedLogout() {
 		ProductListModel products = (ProductListModel)med.getProducts();
 
 		for (int row = 0, n = products.getRowCount(); row < n; row++) {
 			JList<String> states = (JList<String>) med.getProducts().
 					getValueAt(row, ProductListModel.STATUS_COL);
 			DefaultListModel<String> model = (DefaultListModel<String>) states
 				.getModel();
 			if (model.contains(State.STATE_TRANSFERS) ||
 					model.contains(State.STATE_TRANSFERP) ||
 					model.contains(State.STATE_OFFERMADE) ||
 					model.contains(State.STATE_OFFERACC))
 				return false;
 		}
 		return true;
 	}
 }
