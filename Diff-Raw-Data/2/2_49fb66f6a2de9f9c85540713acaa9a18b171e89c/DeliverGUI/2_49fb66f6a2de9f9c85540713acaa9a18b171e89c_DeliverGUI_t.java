 package pizzaProgram.gui;
 
 import java.awt.GridBagConstraints;
 import java.awt.List;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 
 import javax.swing.JFrame;
 
 import pizzaProgram.dataObjects.Customer;
 import pizzaProgram.dataObjects.Extra;
 import pizzaProgram.dataObjects.Order;
 import pizzaProgram.dataObjects.OrderDish;
 import pizzaProgram.database.DatabaseConnection;
 import pizzaProgram.events.Event;
 import pizzaProgram.events.EventDispatcher;
 import pizzaProgram.events.EventHandler;
 import pizzaProgram.events.EventType;
 import pizzaProgram.modules.GUIModule;
 
 public class DeliverGUI extends GUIModule implements EventHandler{
 
 	private List orderList;
 	private List currentInfoList = new List();
 	private List orderContentList = new List();
 	private DeliveryMap chartArea;
 	private HashMap<String, Order> orderMap = new HashMap<String, Order>();
 	
 	private DatabaseConnection database;
 	private JFrame jFrame;
 	
 	
 	public DeliverGUI(DatabaseConnection dbc, JFrame jFrame, EventDispatcher eventDispatcher) {
 		super(eventDispatcher);
 		this.database = dbc;
 		this.jFrame = jFrame;
 		eventDispatcher.addEventListener(this, EventType.COOK_GUI_REQUESTED);
 		eventDispatcher.addEventListener(this, EventType.ORDER_GUI_REQUESTED);
 		eventDispatcher.addEventListener(this, EventType.DELIVERY_GUI_REQUESTED);
 		initialize();
 		hide();
 		populateLists();
 	}
 	/**
 	 * Her skal koden for � lage og legge til komponenter ligger
 	 */
 	@Override
 	public void initialize() {
 		orderList = new List();
 		orderList.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent e) {
 				currentInfoList.removeAll();
 				orderContentList.removeAll();
 				
 				Order o = orderMap.get(orderList.getSelectedItem());
 				if (o == null){
 					return;
 				}
 				
 				Customer c = o.getCustomer();
 				
 				currentInfoList.add(c.firstName + " " + c.lastName);
 				currentInfoList.add(c.address);
 				currentInfoList.add(c.postalCode + " " + c.city);
 				currentInfoList.add("+47 " + c.phoneNumber);
 				for (OrderDish od : o.getOrderedDishes()){
 					orderContentList.add(od.dish.name + od.dish.price);
 					for (Extra ex : od.getExtras()){
 						orderContentList.add("  - " + ex.name);
 					}
 				}
 				chartArea.loadImage(c.address);
 			}
 		});
 		
 		// Gridden som inneholder Ordre
 		GridBagConstraints orderListConstraints = new GridBagConstraints();
 		orderListConstraints.gridx = 0;
 		orderListConstraints.gridy = 0;
 		orderListConstraints.weightx = 1;
 		orderListConstraints.weighty = 1;
 		orderListConstraints.gridwidth = 1;
 		orderListConstraints.gridheight = 2;
 		orderListConstraints.fill = GridBagConstraints.BOTH;
 		this.jFrame.add(orderList, orderListConstraints);
 		
 		// Gridden som inneholder Adresse
 		GridBagConstraints currentInfoListConstraints = new GridBagConstraints();
 		currentInfoListConstraints.gridx = 1;
 		currentInfoListConstraints.gridy = 0;
 		currentInfoListConstraints.weightx = 0.01;
 		currentInfoListConstraints.weighty = 1;
 		currentInfoListConstraints.gridwidth = 1;
 		currentInfoListConstraints.gridheight = 1;
 		currentInfoListConstraints.fill = GridBagConstraints.BOTH;
 		this.jFrame.add(currentInfoList, currentInfoListConstraints);
 		
		// Gridden som inneholder innholdet i ordren f
 		GridBagConstraints orderContentListConstraints = new GridBagConstraints();
 		orderContentListConstraints.gridx = 2;
 		orderContentListConstraints.gridy = 0;
 		orderContentListConstraints.weightx = 0.01;
 		orderContentListConstraints.weighty = 1;
 		orderContentListConstraints.gridwidth = 1;
 		orderContentListConstraints.gridheight = 1;
 		orderContentListConstraints.fill = GridBagConstraints.BOTH;
 		this.jFrame.add(orderContentList, orderContentListConstraints);
 		
 		// Gridden som inneholder kart
 		chartArea = new DeliveryMap();
 		GridBagConstraints chartAreaConstraints = new GridBagConstraints();
 		chartAreaConstraints.gridx = 1;
 		chartAreaConstraints.gridy = 1;
 		chartAreaConstraints.weightx = 0;
 		chartAreaConstraints.weighty = 0;
 		chartAreaConstraints.gridwidth = 2;
 		chartAreaConstraints.gridheight = 1;
 		chartAreaConstraints.fill = GridBagConstraints.BOTH;
 		this.jFrame.add(chartArea, chartAreaConstraints);
 		
 		
 	}
 	
 	public void populateLists(){
 		ArrayList<Order> tempSort = new ArrayList<Order>();
 		
 		for (Order o : database.getOrders()){
 			if (o.getStatus().equals(Order.HAS_BEEN_COOKED) || o.getStatus().equals(Order.BEING_DELIVERED)){
 				String sc = ("Order " + o.getID() + ": " + o.getCustomer().firstName + " " + o.getCustomer().lastName);
 				tempSort.add(o);
 				orderMap.put(sc, o);
 			}
 		}
 		Comparator<Order> comp = new Comparator<Order>() {
 			@Override
 			public int compare(Order o1, Order o2) {
 				return o1.getTimeRegistered().compareTo(o2.getTimeRegistered());
 			}
 		};
 		
 		Collections.sort(tempSort, comp);
 		
 		for (Order o : tempSort){
 			orderList.add("Order " + o.getID() + ": " + o.getCustomer().firstName + " " + o.getCustomer().lastName);
 			//infoList.add(o.getCustomer().firstName);
 		}
 		
 	}
 	/**
 	 * Her skal koden for � vise komponentene ligge
 	 */
 	@Override
 	public void show() {
 		System.out.println("show");
 		orderList.setVisible(true);
 		currentInfoList.setVisible(true);
 		orderContentList.setVisible(true);
 		chartArea.setVisible(true);
 		jFrame.setVisible(true);
 	}
 	
 	/**
 	 * Her skal koden for � skjule komponentene ligge
 	 */
 	@Override
 	public void hide() {
 		System.out.println("show");
 		orderList.setVisible(false);
 		currentInfoList.setVisible(false);
 		orderContentList.setVisible(false);
 		chartArea.setVisible(false);
 		jFrame.setVisible(true);
 	}
 	
 	@Override
 	public void handleEvent(Event<?> event) {
 		if(event.eventType.equals(EventType.COOK_GUI_REQUESTED)){
 			hide();
 		}else if(event.eventType.equals(EventType.DELIVERY_GUI_REQUESTED)){
 			show();
 		}else if(event.eventType.equals(EventType.ORDER_GUI_REQUESTED)){
 			hide();
 		}
 	}
 	
 }
