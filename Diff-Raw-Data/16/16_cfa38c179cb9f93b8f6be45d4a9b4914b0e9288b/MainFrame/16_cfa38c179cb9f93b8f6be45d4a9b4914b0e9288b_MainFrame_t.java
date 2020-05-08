 package PizzaPck;
 
import com.trolltech.qt.core.QProcess.ExitStatus;
 import com.trolltech.qt.gui.*;
 import com.trolltech.qt.gui.QSizePolicy.Policy;
 
 /**
  * This class contains the main method and acts as a boss.
  * Mainframe is setting up the entire program
  * 
  * @author Everyone
  *
  */
 public class MainFrame extends QWidget {
 
 	private QGridLayout layout;
 	private QTabWidget tabMain;
 	
 	private OrderGUI orderGui;
 	private Kitchen kitchenWidget;
 	private SettingsWidget settingWidget;
 	private Delivery deliveryWidget;
 	private CustomerWidget customerWidget;
 	private DB db;
 	
 	public Signal1<Boolean> changeTab = new Signal1<Boolean>();

 	
 	
 	/**
 	 * the constructor calls the setUp method
 	 * @author
 	 */
 	public MainFrame(){
 		setUp();
 	}	
 
 
 	
 	/**
 	 * Creates the Main GUI and connects all parts of the program in this class
 	 */
 	private void setUp(){
 
 		//Connect to database
 		db = new DB();
 		
 		
 		//oppretter layout og QtabWidget der vi kan plassere widgeter i hver fane
 		layout = new QGridLayout(this); 
 		tabMain = new QTabWidget();
 		layout.addWidget(tabMain);
 
 
 		/*
 		 * Setter opp fanene, med navn og riktig widget
 		 */
 
 
 		//adds customer widget to main window
 		customerWidget = new CustomerWidget(db);
 		tabMain.addTab(customerWidget, null);
 		tabMain.addTab(customerWidget,"Kunde");
 		
 
 
 		//adds ordergui to main window
 		orderGui = new OrderGUI(db);
 		tabMain.addTab(orderGui, "Bestilling");
 		orderGui.setDisabled(true);
 		
 		//adds kitchen widget to main windows
 		kitchenWidget = new Kitchen(db);
		tabMain.addTab(kitchenWidget, "Kj�kken");
 		
 		//adds map
 		deliveryWidget = new Delivery(db);
 		tabMain.addTab(deliveryWidget, "Kartet");
 		
 		//adds settings widget to main window
 		settingWidget = new SettingsWidget(db);
 		tabMain.addTab(settingWidget, "Settings");
 	
 		
 		//Connect Settingswidget to ordergui
 		
 		settingWidget.singalInsertProduct.connect(orderGui, " updatePizzaList()");
 		//kunde.customer.connect(tab2, "insertOrder(int)");
 		customerWidget.signalCustomer.connect(orderGui, "displayCustomer(int)");
 		customerWidget.changeTab.connect(this, "setCurrentTab()");
 		
 		orderGui.signalKitchen.connect(kitchenWidget, "getOrders()");
 		orderGui.signalCancel.connect(this,"setCurrentTab()");
 		kitchenWidget.signalDelivery.connect(deliveryWidget, "getDeliveries()");
 		
 
 		
 		/*
 		 * Viser og setter størrelsen på widgeten 
 		 */
 		this.setSizePolicy(Policy.Fixed, Policy.Maximum);
 		this.setWindowTitle("PizzaPlace");
 		this.show();
 
 	}
 
 	/**
 	 * Changes the tab according to which tab the user i currently working in
 	 * 
 	 * The program limits the user to just working with order as long as there is a customer who are
 	 * ordering. As a result this will make the user to either decline or finish ordering products before continuing.
 	 * 
 	 */
 	public void setCurrentTab (){
 		int index = tabMain.currentIndex();
 		System.out.println(index);
 		if(index == 0){
 			tabMain.setCurrentIndex(1);
 			
 			customerWidget.setDisabled(true);
 			orderGui.setDisabled(false);
 			kitchenWidget.setDisabled(true);
 			deliveryWidget.setDisabled(true);
 			settingWidget.setDisabled(true);
 		}
 		else if(index == 1) {
 			tabMain.setCurrentIndex(0);
 			customerWidget.setDisabled(false);
 			orderGui.setDisabled(true);
 			kitchenWidget.setDisabled(false);
 			deliveryWidget.setDisabled(false);
 			settingWidget.setDisabled(false);
 		}
 		
 	}
 
 	/**
 	 * Runs the program
 	 * @param args
 	 */
     public static void main(String[] args) {
     	
         QApplication.initialize(args);
         new MainFrame(); // oppretter seg selv og kjører setUp() gjennom constructoren;
         QApplication.exec();
        
     }
     
 }
