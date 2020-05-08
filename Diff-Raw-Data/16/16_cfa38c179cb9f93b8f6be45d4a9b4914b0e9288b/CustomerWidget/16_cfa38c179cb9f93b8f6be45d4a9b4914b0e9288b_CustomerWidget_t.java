 package PizzaPck;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.trolltech.qt.core.Qt;
 import com.trolltech.qt.gui.QCompleter;
 import com.trolltech.qt.gui.QGridLayout;
 import com.trolltech.qt.gui.QGroupBox;
 import com.trolltech.qt.gui.QHBoxLayout;
 import com.trolltech.qt.gui.QLabel;
 import com.trolltech.qt.gui.QLayout.SizeConstraint;
 import com.trolltech.qt.gui.QLineEdit;
 import com.trolltech.qt.gui.QListWidget;
 import com.trolltech.qt.gui.QPushButton;
 import com.trolltech.qt.gui.QStringListModel;
 import com.trolltech.qt.gui.QVBoxLayout;
 import com.trolltech.qt.gui.QWidget;
 
 /**
  * This class handle all customer registration
  * @author Everyone
  *
  */
 public class CustomerWidget extends QWidget {
 
 
 	private QPushButton btnSearch, btnOrder, btnUpdate, btnSave;
 	private QLineEdit txtFirstName, txtLastName, txtAdress, txtCity, txtZipCode, txtPhone, txtSearch;
 	private QLabel labFirstName, labLastName, labAdress, labCity, labZipCode, labPhone, labSearch;
 	private QGridLayout layoutGrid;
 	private QGroupBox groupbox;
 	private QHBoxLayout layoutSearch;
 	private QVBoxLayout layoutMain;
 	private QListWidget list;
 	//Database instance
 	private DB db;
 	private QStringListModel words;
 	//Current customer
 	private String[] tmpCustomer;
 	private String customerid;
 	
 	//Signal handler
 	public Signal1<Integer> signalCustomer = new Signal1<Integer>();
 	public Signal1<Boolean> changeTab = new Signal1<Boolean>();
 	//Not used at the moment
 	//public Signal1<String[]> customerInfo = new Signal1<String[]>();
 	
 
 	//TODO: we will have to refactor the kundewidget class to customer widget
 	/**
 	 * Creates a new instance of "kundeWidget" and 
 	 * @param db
 	 */
 	public CustomerWidget(DB db) {
 		this.db = db;
 		setUp();
 	}
 
 	/**
 	 * Creates the user interface for customer registration
 	 */
 	private void setUp(){
 
 		//setter opp de akutelle layoutene
 		
 		groupbox = new QGroupBox();
 		layoutGrid = new QGridLayout();
 		layoutSearch = new QHBoxLayout();
 		layoutMain = new QVBoxLayout(groupbox);
 		layoutMain.addLayout(layoutSearch);
 		layoutMain.addLayout(layoutGrid);
 //		groupbox.setLayout(sok);
 //		groupbox.setLayout(layout);
 		groupbox.setParent(this);
 
 		/*
 		 * Denne linjen gjør at ting ikke blir strekt ut det som er plassert på layout. 
 		 * Da vil alt som er addet til layout også få samme policy. 
 		 * Vil du få dette til å gjelde for label eller lineedit, da kan du burke setSizePolicy
 		 */
 		layoutGrid.setSizeConstraint(SizeConstraint.SetFixedSize); // 
 
 
 
 		/*
 		 * oppretter 
 		 */
 
 		//tekstfelt
 		txtSearch = new QLineEdit();	
 		txtFirstName = new QLineEdit();
 		txtLastName = new QLineEdit();
 		txtAdress = new QLineEdit();
 		txtCity = new QLineEdit();
 		txtZipCode = new QLineEdit();
 		txtPhone = new QLineEdit();
 
 		//labels
 		labFirstName = new QLabel("Fornavn");
 		labLastName = new QLabel("Etternavn");
 		labAdress = new QLabel("Adresse");
 		labCity = new QLabel("Poststed");
 		labZipCode = new QLabel("Postkode");
 		labPhone = new QLabel("Telefon");
 		labSearch = new QLabel("S�k");
 		
 		
 		words = new QStringListModel();
 		QCompleter completer = new QCompleter(words);
 		completer.setCaseSensitivity(Qt.CaseSensitivity.CaseInsensitive);
 		txtSearch.setCompleter(completer);
 		
 		
 		//knapper
 		btnSearch = new QPushButton("S�k");
 		btnOrder = new QPushButton("Bestill");
 		btnSave = new QPushButton("Lagre kunde");
 		btnUpdate = new QPushButton("Oppdater kunde");
 		
 		//Clicked
 		btnSearch.clicked.connect(this, "findCustomer()");
 		btnSave.clicked.connect(this, "insertCustomer()");
 		btnUpdate.clicked.connect(this, "updateUser()");
 		btnOrder.clicked.connect(this, "sendCustomer()");
 		//btnBestill.clicked.connect(this, "sendCustomer()");
 
 		txtSearch.textEdited.connect(this,"findCustomer()");
 		completer.activated.connect(this,"insertInfo(String)");
 		
 		/*
 		 * plasserer knapper, labels og tekstfelt slik i layout
 		 */
 		
 
 		//oppsett av layout
 		txtSearch.setFixedWidth(200);
 		layoutSearch.addStretch(5);
 		layoutSearch.addWidget(txtSearch);
 		layoutSearch.addStretch(1);
 		layoutSearch.addWidget(btnSearch);
 		
 		layoutGrid.addWidget(labFirstName, 1, 0);
 		layoutGrid.addWidget(txtFirstName, 1, 1);
 
 		layoutGrid.addWidget(labLastName, 2, 0);
 		layoutGrid.addWidget(txtLastName, 2, 1);
 
 		layoutGrid.addWidget(labAdress, 3, 0);
 		layoutGrid.addWidget(txtAdress, 3, 1);
 
 		layoutGrid.addWidget(labCity, 4, 0);
 		layoutGrid.addWidget(txtCity, 4, 1);
 
 		layoutGrid.addWidget(labZipCode, 5, 0);
 		layoutGrid.addWidget(txtZipCode, 5, 1);
 
 		layoutGrid.addWidget(labPhone, 6, 0);
 		layoutGrid.addWidget(txtPhone, 6, 1);
 
 		layoutGrid.addWidget(btnUpdate, 7, 0);
 		layoutGrid.addWidget(btnSave, 7, 1);
 
 		layoutGrid.addWidget(btnOrder, 8, 2);
 
 	}
 	
 	/**
 	 * DATA IS PROTECTED!!! Wrapper functions are used to perform tasks on the gui
 	 */
 	private void insertCustomer() {
 		//Get information from gui
 		String [] data = getFields();
 		//Remember the last customer
 		tmpCustomer = data;
 		customer c = new customer(data);
 		
 		try {
 			db.insert(c);
 			ArrayList <String[]> tmp = db.search(data[5], false, false);
 			tmpCustomer = tmp.get(0);
 		}catch(RuntimeException err) {
 			ErrorMessage.invalidInput(this);
 			
 		}
 		
 	}
 	
 	/**
 	 * clear fields
 	 */
 	private void clearFields(){
 		txtSearch.clear();
 		txtFirstName.clear();
 		txtLastName.clear();
 		txtAdress.clear();
 		txtCity.clear();
 		txtZipCode.clear();
 		txtPhone.clear();
 		txtAdress.clear();
 
 	}
 	
 	/**
 	 * finds the customer in the database
 	 */
 	private void findCustomer() {
 		//Search after the given query and return information from kunde (table)
 		try {
 			ArrayList<String[]> list = db.search(txtSearch.text(), false, false);
 			
 			//TODO: Gui needs to support the latest modifications
 			List<String> liste = new ArrayList<String>();
 			String temp = "";
 			//Save current customer
 			if(list.size() ==1){
 			String[] res = list.get(0);
 			tmpCustomer = res;
 			txtFirstName.setText(res[0]); 	//Fornavn
 			txtLastName.setText(res[1]); 	//Etternavn
 			txtAdress.setText(res[2]);   	//Adresse
 			txtCity.setText(res[3]); 	//Sted
 			txtZipCode.setText(res[4]);	//Postkode
 			txtPhone.setText(res[5]);//Telefon
 			
 			tmpCustomer = res;
 			}
 			else{
 				for (String[] string : list) {
 					temp = string[0]+ " " + string[1] + " " + string[5];
 					liste.add(temp);
 				}
 				words.setStringList(liste);
 			}
 			
 			//txtSearch.clear();
 			
 			
 			
 		}catch(RuntimeException err) {
 			//ErrorMessage.noSuchUser(this);
 		}
 		
 	}
 	/**
 	 * This methode is used when a search is done,
 	 * to get the customer information in the different
 	 * fields.
 	 * 
 	 * @param text
 	 */
 	private void insertInfo(String text){
		String data[] = text.split(" ");
		System.out.println(data[2]);
		ArrayList<String[]> result = db.search(data[2], false, false);
 		String[] res = result.get(0);
 		tmpCustomer = res;
 		
 		txtFirstName.setText(res[0]); 	//Fornavn
 		txtLastName.setText(res[1]); 	//Etternavn
 		txtAdress.setText(res[2]);   	//Adresse
 		txtCity.setText(res[3]); 	//Sted
 		txtZipCode.setText(res[4]);	//Postkode
 		txtPhone.setText(res[5]); //telefon
 		
 		tmpCustomer = res;
 		
 		//txtSearch.clear();
 	}
 	
 	/**
 	 * Update user based on CustomerID
 	 */
 	private void updateUser(){
 		try {
 			String[] customer = getFields();
 			
 			db.update(customer, tmpCustomer[6]);
 		}catch(RuntimeException err) {
 			ErrorMessage.invalidInput(this);
 		}
 	}
 	
 	/**
 	 * Return information from txtBoxes
 	 * @return
 	 */
 	private String[] getFields(){
 		String[] data =  {
 			txtFirstName.text(),
 			txtLastName.text(),
 			txtAdress.text(),
 			txtCity.text(),
 			txtZipCode.text(),
 			txtPhone.text()	
 		};
 		
 		return data;
 	}
 	
 	
 	/**
 	 *  
 	 */
 	private void sendCustomer() {
 		if(tmpCustomer == null) {
 			ErrorMessage.noSuchUser(this);
 			System.out.println("No customer");
 			return;
 		}
 		for (int i = 0; i < tmpCustomer.length; i++) {
 			System.out.println(tmpCustomer[i]);
 		}
 		
 		try {
 			
 			int customerID = Integer.parseInt(tmpCustomer[6]);
 			System.out.println(customerID);
 			signalCustomer.emit(customerID);
 			changeTab.emit(true);
 			
 		}catch(RuntimeException err) {
 			ErrorMessage.noSuchUser(this);
 		}
 		
 		clearFields();
 	}
 
 
 
 }
