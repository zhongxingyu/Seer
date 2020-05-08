 package PizzaPck;
 
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import com.trolltech.qt.gui.QGridLayout;
 import com.trolltech.qt.gui.QGroupBox;
 import com.trolltech.qt.gui.QLabel;
 import com.trolltech.qt.gui.QLineEdit;
 import com.trolltech.qt.gui.QPushButton;
 import com.trolltech.qt.gui.QSizePolicy.Policy;
 import com.trolltech.qt.gui.QVBoxLayout;
 import com.trolltech.qt.gui.QWidget;
 
 
 
 /**
  * This class have the methods for adding products, 
  * for changing delivery price and for changing the limit
  * for free delivery. 
  * @author Everyone
  */
 public class SettingsWidget extends QWidget {
 	
 	//knapper, tekstfelt og labels
 	private QLineEdit txtName, txtContents, txtPrice, txtBorder, txtDelivery;
 	private QLabel labName,labContents,labPrice, labBorder, labDelivery, labLimit;
 	private QGridLayout pizzaLayout;
 	private QGridLayout borderLayout;
 	private QVBoxLayout box;
 	private QPushButton btnAddpizza, btnChange, btnChangeDelivey;
 	private QGroupBox groupBoxPizza;
 	private QGroupBox groupBoxPrice;
 	private DB db;
 	public static float DELIVERY_LIMIT = 500.0f;
 	public static float DELIVERY_PRICE = 50.0f;
 	
 	
 	
 	//Signal handler
 	public Signal1 <Boolean> singalInsertProduct = new Signal1<Boolean>();
 	
 	
 	/**
 	 * The DB object gives access to methods 
 	 * working towards the database
 	 * @param db
 	 */
 	public SettingsWidget(DB db){
 		setUp();
 		this.db = db;
 	}
 	
 	private void clearFields(){
 		txtName.clear();
 		txtContents.clear();
 		txtPrice.clear();
 	}
 	
 	/**
 	 * Insert a new product from textfields into the database
 	 * and then emits a signal whenever the button is clicked.
 	 */
 	public void insertIntoDB(){
 		
 		String[] data ={
 				txtName.text(),txtPrice.text(),txtContents.text()," "
 		};
 		
 		try{
 			db.insert(new product(data));
 		}catch (RuntimeException e) {
 			
 		}
 		//Send signal indicating change in DB 
 		//System.out.println("Signal emited");
 		singalInsertProduct.emit(true);
 		labLimit.setText("produkt lagt til i database");
 		clearFields();
 	}
 	
 	/**
 	 * Reads existing values for DELIVERY_LIMIT 
 	 * and DELIVERY_PRICE when program starts.
 	 * @throws IOException
 	 */
 	public void readFromFile() throws IOException{
 
 		FileInputStream fstream = new FileInputStream("bin/config.txt");
 		DataInputStream in = new DataInputStream(fstream);
 		BufferedReader br = new BufferedReader(new InputStreamReader(in));
 		String strLine;
 		ArrayList<String> lestInn = new ArrayList<String>();
 
 		while ((strLine = br.readLine()) != null)   {
 			lestInn.add(strLine);
 
 		}
 		DELIVERY_LIMIT = Float.valueOf(lestInn.get(0));
 		DELIVERY_PRICE = Float.valueOf(lestInn.get(1));
 
 		in.close();
 	}
 	
 	/**
 	 * This method writes current values for 
 	 * DELIVERY_LIMIT and DELIVERY_PRICE to file.
 	 * If file doesn't exist, a new file is created.
 	 */
 	public void writeToFile(){
 		String ny = DELIVERY_LIMIT + "\n" + DELIVERY_PRICE;
 		try {
 		    BufferedWriter out = new BufferedWriter(new FileWriter("bin/config.txt"));
 		    out.write(ny);
 		    out.close();
 		} 
 		catch (Exception e) {
 		}
 	}
 	
 	/**
 	 * Updates the Delivery price. It uses the 
 	 * @see writeToFile() method to save the 
 	 * change to file. If incorrect values are used,
 	 * an error-message is displayed, and no changes 
 	 * are made.		
 	 */
 	public void setDeliveyPrice(){
 		
 		try {
 			Float.parseFloat(txtDelivery.text());
 			String var = txtDelivery.text();
 			
 			if (Float.valueOf(var) < 0){
 				labLimit.setStyleSheet("QLabel { color : red; }");
 				labLimit.setText(" Kan ikke være negativ, pris er "
 				+ DELIVERY_PRICE + " og nåværende grense er " + DELIVERY_LIMIT);
 			}
 			else{
 				DELIVERY_PRICE = Float.valueOf(var);
 				writeToFile();
 				labLimit.setStyleSheet("QLabel { color : black; }");
 				labLimit.setText("Nåværende grense er " +
 				DELIVERY_LIMIT+" og nåværende pris er " +DELIVERY_PRICE);
 			}
 			
 			
 		}catch(NumberFormatException e) {
 			//TODO: Error checking
			return;
 		}
 		
 	}
 	
 	/**
 	 * This method updates the Delivery limit. 
 	 * It uses the @see writeToFile() 
 	 * method to save the change to file. 
 	 * If incorrect values are used, an error-message
 	 * is displayed, and no changes are made.
 	 */
 	public void setLimit(){
 		try {
 			Float.parseFloat(txtBorder.text());
 			String var = txtBorder.text();
 			if (Float.valueOf(var) < 0){
 				labLimit.setStyleSheet("QLabel { color : red; }");
 				labLimit.setText("Kan ikke være negativ, grense er "
 				+ DELIVERY_LIMIT + " og nåværende pris er " +DELIVERY_PRICE);
 			}
 			else{
 				DELIVERY_LIMIT = Float.valueOf(var);
 				writeToFile();
 				labLimit.setStyleSheet("QLabel { color : black; }");
 				labLimit.setText("Nåværende grense er " + 
 				DELIVERY_LIMIT+" og nåværende pris er " +DELIVERY_PRICE);
 			}
 		
 		}catch(NumberFormatException e) {
 			//TODO: Error print
			return;
 		}
 		
 		
 	}
 	
 	
 	
 	/**
 	 * This method gets the information about the limit prices
 	 * according to what is set earlier. And it also setup
 	 * the graphical user interface for the widget.
 	 */
 	private void setUp(){
 		//reading values for Free delivery limit and Delivery price.
 		//If they haven't been changed, standard values are used
 		try{
 			readFromFile();
 		}
 		catch(Exception e){
 			//ErrorMessage.couldNotReadFromFile(this);
 		}
 
 		
 		//creates new instances of buttons, labels, lineEdit, etc
 		box = new QVBoxLayout();
 		pizzaLayout = new QGridLayout();
 		borderLayout = new QGridLayout();
 		
 		
 		groupBoxPizza = new QGroupBox("Legg til nye pizzaer");
 		groupBoxPizza.setLayout(pizzaLayout);
 		this.setLayout(box);
 		
 		groupBoxPrice = new QGroupBox("Endre prisgrense for gratis levering og leveringsgebyr");
 		groupBoxPrice.setLayout(borderLayout);
 		
 		labBorder = new QLabel("Ny grense");
 		txtBorder = new QLineEdit();
 		txtBorder.setFixedWidth(200);
 		labDelivery = new QLabel("Ny Leveringspris");
 		txtDelivery = new QLineEdit();
 		txtDelivery.setFixedWidth(200);
 		btnChange = new QPushButton("Endre grensen");
 		btnChangeDelivey = new QPushButton("Endre leveringspris");
 		
 		
 		labName = new QLabel("Navn paa produkt:");
 		txtName = new QLineEdit();
 		txtName.setFixedWidth(200);
 		labContents = new QLabel("Ingredienser");
 		txtContents = new QLineEdit();
 		txtContents.setFixedWidth(200);
 		labPrice = new QLabel("Pris");
 		txtPrice = new QLineEdit();
 		txtPrice.setFixedWidth(200);
 		btnAddpizza = new QPushButton("Legg til produkt i DB");
 		labLimit = new QLabel("Nåværende grense er " + DELIVERY_LIMIT +
 				" og nåværende pris er " +DELIVERY_PRICE);
 		
 		
 		box.addWidget(groupBoxPizza);
 		box.addWidget(groupBoxPrice);
 		
 		//add buttons, labels and lineedits to borderlayout
 		borderLayout.addWidget(labLimit, 0, 0, 1, 0);
 		borderLayout.addWidget(labBorder, 1, 0);
 		borderLayout.addWidget(txtBorder, 1, 1);
 		borderLayout.addWidget(btnChange, 1, 2);
 		labLimit.setSizePolicy(Policy.Fixed, Policy.Fixed);
 		btnChange.clicked.connect(this, "setLimit()");
 		
 		borderLayout.addWidget(labDelivery, 2, 0);
 		borderLayout.addWidget(txtDelivery, 2, 1);
 		borderLayout.addWidget(btnChangeDelivey, 2, 2);		
 		btnChangeDelivey.clicked.connect(this, "setDeliveyPrice()");
 		
 		//add buttons, labels and lineedits to pizzalayout
 		pizzaLayout.addWidget(labName, 0, 0);
 		pizzaLayout.addWidget(txtName, 0, 1);		
 		pizzaLayout.addWidget(labContents, 1, 0);
 		pizzaLayout.addWidget(txtContents, 1, 1);		
 		pizzaLayout.addWidget(labPrice, 2, 0);
 		pizzaLayout.addWidget(txtPrice, 2, 1);		
 		pizzaLayout.addWidget(btnAddpizza, 3, 2);
 		btnAddpizza.clicked.connect(this, "insertIntoDB()");
 	}
 }
