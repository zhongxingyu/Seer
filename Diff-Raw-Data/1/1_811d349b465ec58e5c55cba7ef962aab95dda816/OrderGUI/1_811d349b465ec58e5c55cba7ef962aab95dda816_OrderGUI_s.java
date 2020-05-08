 package PizzaPck;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import com.trolltech.qt.core.QDate;
 import com.trolltech.qt.core.QTime;
 import com.trolltech.qt.gui.QDateEdit;
 import com.trolltech.qt.gui.QGridLayout;
 import com.trolltech.qt.gui.QGroupBox;
 import com.trolltech.qt.gui.QHBoxLayout;
 import com.trolltech.qt.gui.QLabel;
 import com.trolltech.qt.gui.QListWidget;
 import com.trolltech.qt.gui.QPushButton;
 import com.trolltech.qt.gui.QRadioButton;
 import com.trolltech.qt.gui.QSizePolicy.Policy;
 import com.trolltech.qt.gui.QTextBrowser;
 import com.trolltech.qt.gui.QTimeEdit;
 import com.trolltech.qt.gui.QVBoxLayout;
 import com.trolltech.qt.gui.QWidget;
 
 /**
  * This class extends the {@link QWidget} and creates the GUI
  * for adding products to an order. 
  * 
  * 
  * @author Everyone
  *
  */
 public class OrderGUI extends QWidget{
 
 	private DB db;
 	private int customerID;
 
 	public Signal1<Boolean> test = new Signal1<Boolean>();
 	public Signal1<Integer> signalCustomer = new Signal1<Integer>();
 	public Signal1<String[]> signalBridge = new Signal1<String[]>();
 	public Signal1<Boolean> signalKitchen = new Signal1<Boolean>();
 	public Signal1<Boolean> signalConfirm = new Signal1<Boolean>();
 	public Signal1<Boolean> signalCancel = new Signal1<Boolean>();
 	
 	//Mirror all the element contained in listProducts
 	public List<String[]> listProductsMirror;
 	
 	private PizzaList order_list;
 	private QRadioButton delivery;
 	private QRadioButton pickup;
 	private QGridLayout main;
 	private QTimeEdit changeTime;
 	private QDateEdit changeDate;
 	
 	private QPushButton btnConfirm, btnDelete;
 	private QListWidget listProducts;
 	private QTextBrowser textCustomer;
 
 	
 	/**
 	 * Create a new instance of OrderGUI
 	 * with a reference to a DB object.
 	 * @param db
 	 * @see DB
 	 */
 	public OrderGUI(DB db){
 		this.db = db;
 		setUpGUI();
 		createSignals();
 	}
 
 	
 	/**
 	 * This method creates the necessary signals. 
 	 */
 	private void createSignals(){
 
 		order_list.signalBridge.connect(this, "handleListProducts(String[])");
 		listProducts.doubleClicked.connect(this, "removeFromLists()");
 		btnConfirm.clicked.connect(this, "confirmOrders()");
 		btnDelete.clicked.connect(this,"deleteOrder()");
 	}
 	
 	/**
 	 * This method deletes the latest inserted 
 	 * order in the database.
 	 */
 	public void deleteOrder(){
 		db.deleteOrder(db.getOrderID());
 		signalCancel.emit(true);
 		textCustomer.clear();
 		
 	}
 
 
 	/**
 	 * This method inserts a order into the database,
 	 * with a timestamp so we know when a order is made. 
 	 * 
 	 * @throws RuntimeException
 	 */
 	public void insertOrder() {
 		String time = new java.sql.Timestamp(new java.util.Date().getTime()).toString();
 		Timestamp currentTime = new java.sql.Timestamp(new java.util.Date().getTime());
 		int h = currentTime.getHours();
 		currentTime.setHours(h +1);
 		String del = delivery.isChecked()? "1":"0";
 
 		String test = "0";
 		String test1 = "0";
 
 		String [] data = {
 				Integer.toString(customerID),
 				del,
 				"0",
 				"0",
 				time,
 				currentTime.toString()
 				
 		};
 		try {
 			db.insert(new orders(data));
 		}catch(RuntimeException err) {
 			err.printStackTrace();
 		}
 	}
 
 
 	/**
 	 * This method displays the customer in the upper left
 	 * textbox in OrderGUI. Takes an Integer as parameter and
 	 * searches the database for the customer with the 
 	 * integer as customerID. 
 	 * @param customerID
 	 * @see DB  
 	 */
 	public void displayCustomer(int customerID) {
 		try {
 			this.customerID = customerID;
 			String query = Integer.toString(customerID);
 			String[] data = db.search(query, false, true).get(0);
 			
 			StringBuilder build = new StringBuilder();
 			String[] fields = {"Fornavn: ","Etternavn: ","Adresse: ","Poststed: ","Postkode: ", "Telefon: "};
 			for (int i = 0; i < data.length-1; i++) {
 				build.append(fields[i]+ data[i]+"\n");
 			}
 			textCustomer.setText(build.toString());
 
 			insertOrder();
 
 		}catch(RuntimeException err) {
 			err.printStackTrace();
 		}
 	}
 
 
 	/**
 	 * This method adds data into the product list 
 	 * and the mirror product list. This method is 
 	 * usually called when a button in the 
 	 * productlist is clicked.
 	 * 
 	 * @param data
 	 */
 	private void handleListProducts(String[] data) {
 		listProductsMirror.add(data);
 
 		String tmp = format(data);
 		listProducts.addItem(tmp);
 	}
 	
 	/**
 	 * This method removes a product from the productlist.
 	 */
 
 	private void removeFromLists() {
 		int row = listProducts.currentIndex().row();
 		listProducts.takeItem(row);
 		listProductsMirror.remove(row);
 	}
 
 	/**
 	 * 
 	 * This method iterates over the String[] 
 	 * and returns a string created by a StringBuilder.
 	 * Usually used for formating the text in QListWidget
 	 * @param data
 	 * @return
 	 */
 	private String format(String[] data) {
 		StringBuilder sb = new StringBuilder();
 		for(int i = 0; i < 4; i++) {
 			sb.append(data[i]);
 			sb.append("  ");
 		}
 
 		return sb.toString();
 	}
 
 
 	/**
 	 * This method iterates over the mirrorOrderList 
 	 * and inserts the products into the database. 
 	 * This method will also clear the data in 
 	 * orderlist and the mirrorOrderList.
 	 * If the orderList is empty, a messagebox
 	 * will appear.
 	 * This method also sends signal to
 	 * @see Kitchen witch is adding the order to 
 	 * its orderlist.
 	 * 
 	 */
 	private void confirmOrders() {
 		//TODO: m� fikse hva som skal komme ut n�r man ikke har lagt til produkter og trykker p� bekreft
 		if (listProductsMirror.size() ==0) {
 			ErrorMessage.noDishAdded(this);
 			return;
 		}
 		
 		updateOrder();
 		
 		Iterator<String[]> iter = listProductsMirror.iterator();
 		while(iter.hasNext()) {
 			String[] tmp = iter.next();
 			String size;
 			if (tmp[1].equals("Stor")){
 				size = "1";
 			}
 			else{
 				size = "0";
 			}
 			String [] data = {
 					db.getOrderID(),
 					db.getPizzaID(tmp[0]),
 					tmp[5],
 					size,
 					tmp[2]
 			};
 
 			db.insert(new receipt(data));
 			
 		}
 		signalKitchen.emit(true);
 		signalCancel.emit(true);
 		//resets the gui
 		resetGUI();
 		
 
 		
 	}
 	
 	/**
 	 * Resets all values in orderGUI
 	 */
 	private void resetGUI(){
 		listProductsMirror = new ArrayList<String[]>();
 		listProducts.clear();
 		textCustomer.clear();
 		setTime();
 		
 	}
 	
 	/**
 	 * This method updates and fills the list of products. 
 	 * Usually called when a product has been 
 	 * inserted into the database.
 	 */
 	public void updatePizzaList() {
 		order_list.fillList();
 	}
 	
 	
 	/**
 	 *  This method changes the time 
 	 *  and delivery status in the database.
 	 *  @see DB
 	 */
 	private void updateOrder() {
 		int date = changeDate.date().day();
 		int month = changeDate.date().month()-1;
 		int year = changeDate.date().year()-1900;
 		int hour = changeTime.time().hour();
 		int minute = changeTime.time().minute();
 		int seconds = changeTime.time().second();
 		int nano = 0;
 		Timestamp time = new java.sql.Timestamp(year,
 					month, date, hour, minute, seconds, nano);
 		db.updateTime(time, delivery.isChecked()? 1 : 0 , db.getOrderID());
 		
 	}
 	
 	/**
 	 * sets currentTime in ordergui
 	 */
 	private void setTime(){
 		QTime time = QTime.currentTime().addSecs(3600);
 		changeTime.setTime(time);
 		if (time.hour()<1) {
 			changeDate.setDate(QDate.currentDate().addDays(1));
 		}else{
 			changeDate.setDate(QDate.currentDate());
 		}
 	}
 	
 	/**
 	 * This method creates and setup's the GUI in
 	 * the OrderGUI. 
 	 */
 	private void setUpGUI(){
 		
 		listProductsMirror = new ArrayList<String[]>();
 		
 		/**
 		 * Creates the bar at the top
 		 */
 		//create instances
 		QHBoxLayout top = new QHBoxLayout();
 		
 		delivery = new QRadioButton("Levering");
 		delivery.setChecked(true);
 		pickup = new QRadioButton("Hente selv");
 		changeDate = new QDateEdit();
 		changeTime = new QTimeEdit();
 		
 		
 		//sets the time and date
 		changeDate.setCalendarPopup(true);
 		setTime();
 		
 		
 		//adds to the top layout
 		top.addWidget(delivery);
 		top.addWidget(pickup);
 		top.addWidget(new QLabel("Dato for levering:"));
 		top.addWidget(changeTime);
 		top.addWidget(changeDate);
 		
 		
 		
 		/**
 		 * Creating the left box
 		 */
 		//creates instances
 		QGroupBox boxLeft = new QGroupBox();
 		QVBoxLayout layoutLeft = new QVBoxLayout();
 		textCustomer = new QTextBrowser();
 		listProducts = new QListWidget();
 		
 		//sets the size policies for the lists and textbrowser and the size for the groupbox
 		boxLeft.setFixedWidth(270);
 		textCustomer.setSizePolicy(Policy.Maximum, Policy.Maximum);
 		listProducts.setSizePolicy(Policy.Maximum, Policy.Maximum);
 		
 		//adds the box and list in the left layout
 		boxLeft.setLayout(layoutLeft);
 		layoutLeft.addWidget(textCustomer);
 		layoutLeft.addWidget(listProducts);
 		
 		
 
 		
 		/**
 		 * Creating the right box
 		 */
 		//create instances
 		QVBoxLayout layoutRight= new QVBoxLayout();
 		QGroupBox boxRight = new QGroupBox();
 		order_list = new PizzaList(db);
 		
 		//sets the size
 		order_list.setContentsMargins(1, 1, 1, 1);
 		
 		//adds to layout and sets the layout for the rightbox
 		boxRight.setFixedWidth(600);
 		boxRight.setLayout(layoutRight);
 		
 		layoutRight.addWidget(order_list);
 		
 		
 		/**
 		 * Bottom box
 		 */
 		//create instances
 		btnConfirm = new QPushButton("Bekreft");
 		btnDelete = new QPushButton("Avbryt");
 		
 		
 		
 		/**
 		 * Create the main layout where all the layouts and boxes are added
 		 */
 		//create instances
 		main = new QGridLayout(this); 
 		
 		//adds to layout
 		main.addLayout(top, 0, 0, 1, 0);
 		main.addWidget(boxLeft, 1, 0);
 		main.addWidget(boxRight, 1, 1);
 		main.addWidget(btnConfirm, 2, 1);
 		main.addWidget(btnDelete, 2, 0);
 		
 
 
 	}
 
 }
 
 
