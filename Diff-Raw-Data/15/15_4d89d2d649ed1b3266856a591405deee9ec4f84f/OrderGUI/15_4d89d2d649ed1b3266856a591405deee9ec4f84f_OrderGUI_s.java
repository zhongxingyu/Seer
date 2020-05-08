 package pizzaProgram.gui;
 
 import java.util.ArrayList;
 
 import javax.swing.ListSelectionModel;
 import javax.swing.table.DefaultTableModel;
 
 import pizzaProgram.core.GUIConstants;
 import pizzaProgram.dataObjects.Customer;
 import pizzaProgram.dataObjects.Dish;
 import pizzaProgram.dataObjects.Extra;
 import pizzaProgram.dataObjects.UnaddedCustomer;
 import pizzaProgram.events.Event;
 import pizzaProgram.events.EventDispatcher;
 import pizzaProgram.events.EventHandler;
 import pizzaProgram.events.EventType;
 import pizzaProgram.gui.EventHandlers.OrderGUI_OrderViewEventHandler;
 import pizzaProgram.gui.EventHandlers.OrderGUI_SystemEventHandler;
 import pizzaProgram.gui.views.OrderView;
 import pizzaProgram.modules.GUIModule;
 
 public class OrderGUI extends GUIModule implements EventHandler {
 
 	private OrderView orderView;
 	private ProgramWindow programWindow;
 
 	public ArrayList<Customer> currentCustomerList;
 	public ArrayList<Dish> currentDishList;
 	public ArrayList<Extra> currentExtrasList;
 	
 	public Dish currentSelectedDish;
 	
 	OrderGUI_OrderViewEventHandler orderViewEventHandler;
 	OrderGUI_SystemEventHandler orderSystemEventHandler;
 
 	public OrderGUI(ProgramWindow mainWindow, EventDispatcher eventDispatcher) {
 		super(eventDispatcher);
 		eventDispatcher.addEventListener(this, EventType.COOK_GUI_REQUESTED);
 		eventDispatcher.addEventListener(this, EventType.ORDER_GUI_REQUESTED);
 		eventDispatcher.addEventListener(this, EventType.DELIVERY_GUI_REQUESTED);
 		this.orderView = new OrderView();
 		mainWindow.addJPanel(orderView);
 		this.orderView.addPropertyChangeListener(null);
 		this.programWindow = mainWindow;
 		orderViewEventHandler = new OrderGUI_OrderViewEventHandler(this);
 		orderSystemEventHandler = new OrderGUI_SystemEventHandler(eventDispatcher, this);
 		this.setupComponents();
 		hide();
 	}
 	
 	private void setupComponents(){
 		OrderView.deliveryMethodComboBox.removeAllItems();
 		OrderView.deliveryMethodComboBox.addItem(GUIConstants.GUI_PICKUP);
 		OrderView.deliveryMethodComboBox.addItem(GUIConstants.GUI_DELIVER);
 		OrderView.deliveryMethodComboBox.selectWithKeyChar(GUIConstants.GUI_DELIVER.charAt(0));
 		OrderView.extrasSelectionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 		OrderView.dishSelectionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		OrderView.customerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		DefaultTableModel tableModel = (DefaultTableModel)OrderView.orderContentsTable.getModel();
 		tableModel.addColumn("Rett");
 		tableModel.addColumn("Tilbehr");
 		OrderView.orderContentsTable.removeEditor();
 		OrderView.selectCustomerButton.setEnabled(false);
 		OrderView.deleteCustomerButton.setEnabled(false);
 		OrderView.changeCustomerButton.setEnabled(false);
 	}
 
 	public void createNewCustomer(UnaddedCustomer customer) {
 		this.dispatchEvent(new Event<UnaddedCustomer>(EventType.DATABASE_ADD_NEW_CUSTOMER, customer));
 	}
 	public void updateCustomer(Customer customer) {
 		this.dispatchEvent(new Event<Customer>(EventType.DATABASE_UPDATE_CUSTOMER_BY_CUSTOMER_ID, customer));
 	}
 
 	@Override
 	public void handleEvent(Event<?> event) {
 		if(event.eventType.equals(EventType.ORDER_GUI_REQUESTED)){
 			show();
 			this.dispatchEvent(new Event<Object>(EventType.DATABASE_UPDATE_ORDER_GUI_SEND_ALL_CUSTOMERS));
 		}
 	}
 	@Override
 	public void show() {
 		this.programWindow.showPanel(this.orderView);
 
 	}
 	@Override
 	public void hide() {
 		this.programWindow.hidePanel(this.orderView);
		this.orderViewEventHandler.resetOrder();
 	}
 }// END
