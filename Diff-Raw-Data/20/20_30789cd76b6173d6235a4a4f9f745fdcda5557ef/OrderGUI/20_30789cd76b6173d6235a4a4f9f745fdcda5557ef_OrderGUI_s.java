 package PizzaPck;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 

 import com.trolltech.qt.core.QDate;
import com.trolltech.qt.core.QDateTime;
 import com.trolltech.qt.core.QTime;
 import com.trolltech.qt.gui.QDateEdit;
import com.trolltech.qt.gui.QDateTimeEdit;
import com.trolltech.qt.gui.QDialogButtonBox;
import com.trolltech.qt.gui.QDialogButtonBox.StandardButton;
 import com.trolltech.qt.gui.QGridLayout;
 import com.trolltech.qt.gui.QGroupBox;
 import com.trolltech.qt.gui.QHBoxLayout;
 import com.trolltech.qt.gui.QLabel;
 import com.trolltech.qt.gui.QListWidget;
import com.trolltech.qt.gui.QMessageBox;
 import com.trolltech.qt.gui.QPushButton;
 import com.trolltech.qt.gui.QRadioButton;
import com.trolltech.qt.gui.QScrollArea;
 import com.trolltech.qt.gui.QSizePolicy.Policy;
import com.trolltech.qt.gui.QStringListModel;
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
 	
 	private QPushButton btnConfirm, btnDelete, btnUpdate;
 	private QListWidget listProducts;
 	private QTextBrowser textCustomer;
 
 	
 	/**
 	 * Create a new instance of ordergui with a reference to a DB obejct.
 	 * @param db
 	 * @see DB
 	 */
 	public OrderGUI(DB db){
 		this.db = db;
 		setUpGUI();
 		createSignals();
 	}
 
 	
 	/**
 	 * creates the necessary signals 
 	 */
 	private void createSignals(){
 
 		order_list.signalBridge.connect(this, "handleListProducts(String[])");
 		listProducts.doubleClicked.connect(this, "removeFromLists()");
 		btnConfirm.clicked.connect(this, "confirmOrders()");
 		btnUpdate.clicked.connect(this,"updateOrder()");
 		btnDelete.clicked.connect(this,"deleteOrder()");
 	}
 	
 	/**
 	 * Deletes the latest inserted order in the database. 
 	 */
 	public void deleteOrder(){
 		db.deleteOrder(db.getOrderID());
 		signalCancel.emit(true);
 		
 	}
 
 
 	/**
 	 * This method inserts a order into the database
 	 * 
 	 * 
 	 * @throws RuntimeException
 	 */
 	public void insertOrder() {
 		String currentTime = new java.sql.Timestamp(new java.util.Date().getTime()).toString();
 		Timestamp time = new java.sql.Timestamp(new java.util.Date().getTime());
 		int h = time.getHours();
 		time.setHours(h +1);
 		String del = delivery.isChecked()? "1":"0";
 
 		String test = "0";
 		String test1 = "0";
 
 		System.out.println("CustomerID "+customerID+"");
 
 		String [] data = {
 				Integer.toString(customerID),
 				del,
 				"0",
 				"0",
 				time.toString(),
 				currentTime
 		};
 		try {
 			db.insert(new orders(data));
 		}catch(RuntimeException err) {
 			System.out.println("INSERT: insert order failed ");
 			err.printStackTrace();
 		}
 	}
 
 
 	/**
 	 * Displays the customer in the upper left textbox in OrderGUI. Takes an Integer as parameter and
 	 * searches the database for the customer with the integer as customerID 
 	 * @param customerID
 	 * @see DB  
 	 */
 	public void displayCustomer(int customerID) {
 		try {
 			this.customerID = customerID;
 			String query = Integer.toString(customerID);
 			System.out.println(query);
 			String[] data = db.Search(query, false, true);
 			StringBuilder build = new StringBuilder();
 			String[] fields = {"Fornavn: ","Etternavn: ","Adresse: ","Poststed: ","Postkode: ", "Telefon: "};
 			for (int i = 0; i < data.length; i++) {
 				build.append(fields[i]+ data[i]+"\n");
 			}
 			textCustomer.setText(build.toString());
 
 			insertOrder();
 
 		}catch(RuntimeException err) {
 			System.out.println("SEARCH: displayCustomer() failed");
 			err.printStackTrace();
 		}
 	}
 
 
 	/**
 	 * This method adds data into the product list and the mirror product list
 	 * This method is usually called when a button in the productlist is clicked.
 	 * 
 	 * @param data
 	 */
 	private void handleListProducts(String[] data) {
 		System.out.println("Insert completed");
 		listProductsMirror.add(data);
 		//Iterator<String[]> iter = listProductsMirror.iterator();
 		String tmp = format(data);
 		listProducts.addItem(tmp);
 
 
 		System.out.println("Størrelsen på mirror list"+listProductsMirror.size()+"");
 	}
 
 	private void removeFromLists() {
 		int row = listProducts.currentIndex().row();
 		listProducts.takeItem(row);
 		listProductsMirror.remove(row);
 		System.out.println("Størrelse på listen"+listProductsMirror.size()+"");
 	}
 
 	//Pizza_name
 	//size 
 	//Amount
 	//Price
 	//ingridients
 	//comments
 	/**
 	 * 
 	 * Iterates over the String[] and returns a string created by a StringBuilder
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
 	 * This method iterates over the mirrorOrderList and inserts the products into the database.
 	 * 
 	 * This method will also clear the data in orderlist and the mirrorOrderList
 	 */
 	private void confirmOrders() {
 		//TODO: m� fikse hva som skal komme ut n�r man ikke har lagt til produkter og trykker p� bekreft
 		if (listProductsMirror.size() ==0) {
 			System.out.println("ikke greit");
 			return;
 		}
 		
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
 			System.out.println(db.getPizzaID(tmp[0]));
 			System.out.println(db.getOrderID());
 			
 		}
 		signalKitchen.emit(true);
 		signalCancel.emit(true);
 		
 		listProductsMirror = new ArrayList<String[]>();
 		listProducts.clear();
 	}
 	
 	/**
 	 * updates the list of products. Usually called when a product has been inserted into the database.
 	 */
 	public void updatePizzaList() {
 		order_list.fillList();
 	}
 	
 	
 	/**
 	 *  Changes the time and delivery status in the database
 	 *  @see DB
 	 */
 	private void updateOrder() {
 		int date = changeDate.date().day();
 		int month = changeDate.date().month();
 		int year = changeDate.date().year()-1900;
 		int hour = changeTime.time().hour()-1;
 		int minute = changeTime.time().minute();
 		int seconds = changeTime.time().second();
 		int nano = 0;
 		Timestamp time = new java.sql.Timestamp(year, month, date, hour, minute, seconds, nano);
 		db.updateTime(time, delivery.isChecked()? 1 : 0 , db.getOrderID());
 		
 	}
 	
 	/**
 	 * Creates and setup's the GUI in 
 	 */
 	private void setUpGUI(){
 		
 		listProductsMirror = new ArrayList<String[]>();
 		
 		/**
 		 * creates the bar at the top
 		 */
 		//create instances
 		QHBoxLayout top = new QHBoxLayout();
 		
 		btnUpdate = new QPushButton("Oppdater");
 		delivery = new QRadioButton("Levering");
 		delivery.setChecked(true);
 		pickup = new QRadioButton("Hente selv");
 		changeDate = new QDateEdit();
 		changeTime = new QTimeEdit();
 		
 		
 		//sets the time and date
 		changeDate.setCalendarPopup(true);
 		changeDate.setDate(QDate.currentDate());
 		changeTime.setTime(QTime.currentTime());
 		
 		//adds to the top layout
 		top.addWidget(delivery);
 		top.addWidget(pickup);
 		top.addWidget(new QLabel("Dato for levering:"));
 		top.addWidget(changeTime);
 		top.addWidget(changeDate);
 		top.addWidget(btnUpdate);
 		
 		
 		
 		/**
 		 * creating the left box
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
 		 * creating the right box
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
 		//layoutRight.addWidget(scroll);
 		
 		
 		/**
 		 * bottom box
 		 */
 		//create instances
 		btnConfirm = new QPushButton("Bekreft");
 		btnDelete = new QPushButton("Avbryt");
 		
 		
 		
 		/**
 		 * create the main layout where all the layouts and boxes are added
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
 
 
