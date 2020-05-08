 package app;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JList;
 import javax.swing.JProgressBar;
 import javax.swing.table.DefaultTableModel;
 
 import app.states.State;
 import app.states.SellerState;
 
 @SuppressWarnings("unchecked")
 public class ProductListModel extends DefaultTableModel {
 
 	private static final long serialVersionUID = 1L;
 	public static final Integer INDEX_COL = 0;
 	public static final Integer PROD_COL = 1;
 	public static final Integer STATUS_COL = 2;
 	public static final Integer LIST_COL = 3;
 	public static final Integer PROGRESS_COL = 4;
 	public static final Integer OFFER_COL = 5;
 
 	static String[] columnNames = { "Index", "Product", "Status",
 		"List of ", "Progress", "Offered"};
 
 	/**
 	 * Associate product name with index in data matrix.
 	 */
 	private Map<String, Integer> prodName2Index;
 
 	/**
 	 * 
 	 * @param type
 	 *            Buyer or Seller who asks for or provides the products in this
 	 *            list.
 	 * @param products
 	 *            Id, Name, Status and
 	 */
 	public ProductListModel(Object[][] data) {
 		super(data, columnNames);
 
 		prodName2Index = new HashMap<String, Integer>();
 		for (int i = 0, n = data.length; i < n; i++)
 			prodName2Index.put((String) data[i][PROD_COL], i);
 	}
 
 	public Class<? extends Object> getColumnClass(int col) {
 		Object o = getValueAt(0, col);
 		
 		if (o == null)
 			return Object.class;
 		
 		return o.getClass();
 	}
 
 	@Override
 	public boolean isCellEditable(int row, int col) {
 		return false;
 	}
 
 	public Object getValueFromListCol(int row, int index) {
 
 		JList<String> list = (JList<String>) getValueAt(row, LIST_COL);
 		DefaultListModel<String> model = (DefaultListModel<String>) list
 				.getModel();
 		if (model.size() < index)
 			return null;
 
 		return model.get(index);
 	}
 
 	public Object getValueFromCol(String userName, String product, int col) {
 		Integer row = prodName2Index.get(product);
 		if (row == null)
 			return null;
 
 		DefaultListModel<String> model = (DefaultListModel<String>)((JList<String>)
 				getValueAt(row, LIST_COL)).getModel();
 		int index = model.indexOf(userName);
 		if (index < 0)
 			return null;
 
 		DefaultListModel<Integer> model1 = (DefaultListModel<Integer>)((JList<Integer>)
 				getValueAt(row, col)).getModel();
 		return model1.get(index);
 	}
 
 	public boolean hasStatus(int row, String status) {
 		JList<String> states = (JList<String>) getValueAt(row, STATUS_COL);
 		DefaultListModel<String> model = (DefaultListModel<String>) states
 				.getModel();
 
 		return model.contains(status);
 	}
 
 	public String getStatus(int row, String name) {
 		String status = "";
 		JList<String> list = (JList<String>) getValueAt(row, LIST_COL);
 		DefaultListModel<String> model = (DefaultListModel<String>) list
 				.getModel();
 		if (model.contains(name)) {
 			int index = model.indexOf(name);
 			list = (JList<String>) getValueAt(row,STATUS_COL);
 			model = (DefaultListModel<String>) list.getModel();
 			status = model.get(index);
 		}
 
 		return status;
 	}
 
 	public String getStatus(int row, int listIndex) {
 		String status = "";
 		JList<String> list = (JList<String>) getValueAt(row, LIST_COL);
 		DefaultListModel<String> model = (DefaultListModel<String>) list
 				.getModel();
 		if (model.size() < listIndex)
 			return status;
 
 		list = (JList<String>) getValueAt(row, STATUS_COL);
 		model = (DefaultListModel<String>) list.getModel();
 		status = model.get(listIndex);
 
 		return status;
 	}
 
 	public void setStatus(String userName, String productName, String status) {
 		Integer row = prodName2Index.get(productName);
 		if (row == null)
 			return;
 		JList<String> list = (JList<String>) getValueAt(row, LIST_COL);
 		DefaultListModel<String> listModel = (DefaultListModel<String>) list
 				.getModel();
 
 		int listIndex = listModel.indexOf(userName);
 		if (listIndex > -1) {
 			list = (JList<String>) getValueAt(row, STATUS_COL);
 			DefaultListModel<String> model = (DefaultListModel<String>) list.getModel();
 			model.set(listIndex, status);
 			if (status.equals(State.STATE_OFFERACC)){
 				/* Keep only userName in list models (users, statuses, offers). */
 				listModel.clear();
 				listModel.addElement(userName);
 				model.clear();
 				model.addElement(status);
 
				JList<Integer> prices = (JList<Integer>) getValueAt(row, OFFER_COL);
 				DefaultListModel<Integer> pmodel = (DefaultListModel<Integer>) prices.getModel();
 				int price = pmodel.get(listIndex);
 				pmodel.clear();
 				pmodel.addElement(price);
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @param userName
 	 * @param productName
 	 * @param value Value of progressBar or price. 
 	 * @param status
 	 */
 	public void setStatus(String userName, String productName, int value, String status) {
 		Integer row = prodName2Index.get(productName);
 		if (row == null)
 			return;
 
 		JList<String> list;
 		DefaultListModel<String> model;
 		JList<Integer> vlist;
 		DefaultListModel<Integer> vmodel;
 		int listIndex;
 		switch (status) {
 		case State.STATE_TRANSFERS:
 			break;
 		case State.STATE_TRANSFERP:
 			JProgressBar bar = (JProgressBar) getValueAt(row, PROGRESS_COL);
 			bar.setValue(value);
 			break;
 		case State.STATE_OFFERMADE:
 			list = (JList<String>) getValueAt(row, LIST_COL);
 			model = (DefaultListModel<String>) list.getModel();
 			listIndex = model.indexOf(userName);
 			if (listIndex > -1) {
 				/* Set status */
 				list = (JList<String>) getValueAt(row, STATUS_COL);
 				model = (DefaultListModel<String>) list.getModel();
 				model.set(listIndex, status);
 
 				/* Set offer */
 				vlist = (JList<Integer>) getValueAt(row, OFFER_COL);
 				vmodel = (DefaultListModel<Integer>) vlist.getModel();
 				vmodel.set(listIndex, value);
 			}
 			break;
 		case State.STATE_OFFERE:
 			list = (JList<String>) getValueAt(row, LIST_COL);
 			model = (DefaultListModel<String>) list.getModel();
 			listIndex = model.indexOf(userName);
 			if (listIndex > -1) {
 				/* Set status */
 				list = (JList<String>) getValueAt(row, STATUS_COL);
 				model = (DefaultListModel<String>) list.getModel();
 				model.set(listIndex, status);
 
 				/* Set best offer */
 				vlist = (JList<Integer>) getValueAt(row, SellerState.BEST_OFFER_COL);
 				vmodel = (DefaultListModel<Integer>) vlist.getModel();
 				vmodel.set(listIndex, value);
 			}
 		default:
 			return;
 		}
 	}
 
 	public int initTransfer(String userName, String productName, int value) {
 		Integer row = prodName2Index.get(productName);
 		if (row == null)
 			return -1;
 
 		JProgressBar progressBar = new JProgressBar(0, value);
 		progressBar.setSize(value, value);
 		setValueAt(progressBar, row, PROGRESS_COL);
 
 		return 0;
 	}
 
 	public int updateTransfer(String userName, String productName, int value) {
 		Integer row = prodName2Index.get(productName);
 		if (row == null)
 			return -1;
 
 		((JProgressBar)getValueAt(row, PROGRESS_COL)).setValue(value);
 
 		return 0;
 	}
 
 	public String[] getUsersList(int row) {
 		JList<String> list = (JList<String>) getValueAt(row, LIST_COL);
 		DefaultListModel<String> model = (DefaultListModel<String>) list
 				.getModel();
 		String[] users = new String[model.size()];
 		model.copyInto(users);
 
 		return users;
 	}
 
 	public Integer[] getOffersList(String productName) {
 		Integer row = prodName2Index.get(productName);
 		if (row == null)
 			return null;
 		JList<Integer> list = (JList<Integer>) getValueAt(row, OFFER_COL);
 		DefaultListModel<Integer> model = (DefaultListModel<Integer>) list
 				.getModel();
 		Integer[] offers = new Integer[model.size()];
 		model.copyInto(offers);
 
 		return offers;
 	}
 
 	public void updateUsersList(String productName,
 			Map<String, String> usersStates) {
 		JList<String> list;
 		for (int i = 0, n = getRowCount(); i < n; i++)
 			if (productName.equals(getValueAt(i, PROD_COL))) {
 				list = (JList<String>) getValueAt(i, LIST_COL);
 				DefaultListModel<String> model = (DefaultListModel<String>) list
 						.getModel();
 				DefaultListModel<String> model1 = (DefaultListModel<String>)
 						((JList<String>) getValueAt(i, STATUS_COL)).getModel();
 				DefaultListModel<Integer> offers = (DefaultListModel<Integer>)
 						((JList<Integer>) getValueAt(i, OFFER_COL)).getModel();
 				model.clear();
 				model1.clear();
 				offers.clear();
 				for (Map.Entry<String, String> us : usersStates.entrySet()) {
 					model.addElement(us.getKey());
 					model1.addElement(us.getValue());
 					offers.addElement(null);
 				}
 
 				break;
 			}
 	}
 
 	public void updateProductsModel(String userName, String product, Integer price, int col) {
 		Integer row = prodName2Index.get(product);
 		int listIndex;
 		if (row == null)
 			return;
 
 		DefaultListModel<String> model = (DefaultListModel<String>)
 				((JList<String>) getValueAt(row, LIST_COL))
 				.getModel();
 		listIndex = model.indexOf(userName);
 		DefaultListModel<Integer> model1 = (DefaultListModel<Integer>)
 				((JList<Integer>) getValueAt(row, col))
 				.getModel();
 		model1.set(listIndex, price);
 	}
 
 	public void removeUserFromList(String username) {
 		for (Map.Entry<String, Integer> entry : prodName2Index.entrySet())
 			removeUserFromList(username, entry.getKey());
 	}
 
 	public void removeUserFromList(String username, String product) {
 		Integer index = prodName2Index.get(product);
 		int listIndex;
 		if (index == null)
 			return;
 
 		DefaultListModel<String> model = (DefaultListModel<String>)
 				((JList<String>) getValueAt(index, LIST_COL))
 				.getModel();
 		listIndex = model.indexOf(username);
 		model.removeElement(username);
 
 		if (listIndex > -1) {
 			model = (DefaultListModel<String>)
 					((JList<String>) getValueAt(index, STATUS_COL)).getModel();
 			model.remove(listIndex);
 			if (model.isEmpty())
 				model.addElement(State.STATE_INACTIVE);
 			DefaultListModel<Integer> offers = (DefaultListModel<Integer>)
 					((JList<Integer>) getValueAt(index, OFFER_COL)).getModel();
 			offers.clear();
 		}
 	}
 
 	public int addUserToList(String username, String product) {
 		Integer index = prodName2Index.get(product);
 		if (index == null)
 			return -1;
 		DefaultListModel<String> model = (DefaultListModel<String>)
 				((JList<String>) getValueAt(index, LIST_COL)).getModel();
 		if (!model.contains(username)) {
 			int userIndex;
 			model.addElement(username);
 			userIndex = model.indexOf(username);
 
 			model = (DefaultListModel<String>)
 					((JList<String>) getValueAt(index, STATUS_COL)).getModel();
 			if (model.contains(State.STATE_INACTIVE))
 				model.set(0, State.STATE_NOOFFER);
 			else
 				model.addElement(State.STATE_NOOFFER);
 			DefaultListModel<Integer> offers = (DefaultListModel<Integer>)
 					((JList<Integer>) getValueAt(index, OFFER_COL)).getModel();
 			offers.addElement(null);
 			return userIndex;
 		}
 		return -1;
 	}
 }
