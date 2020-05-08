 package pizzaProgram.gui;
 
 import java.awt.GridBagConstraints;
 import java.awt.List;
 import java.awt.TextArea;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.HashMap;
 
 import javax.swing.JFrame;
 
 import pizzaProgram.dataObjects.Dish;
 import pizzaProgram.dataObjects.Extra;
 import pizzaProgram.dataObjects.Order;
 import pizzaProgram.dataObjects.OrderDish;
import pizzaProgram.database.DatabaseConnection;
 import pizzaProgram.events.Event;
 import pizzaProgram.events.EventDispatcher;
 import pizzaProgram.events.EventHandler;
 import pizzaProgram.events.EventType;
 import pizzaProgram.modules.GUIModule;
 
 public class CookGUI extends GUIModule implements EventHandler{
 	
 	private List orderList;
 	private HashMap<String, Order> orderMap = new HashMap<String, Order>();
 	private List currentOrderList;
 	private TextArea commentArea;
 	private TextArea descriptionArea;
 	
 	private DatabaseConnection database;
 	private JFrame jFrame;
 	private HashMap<String, Dish> dishMap = new HashMap<String, Dish>();
 	
 	public CookGUI(DatabaseConnection dbc, JFrame jFrame, EventDispatcher eventDispatcher) {
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
 	
 	@Override
 	public void initialize() {
 		orderList = new List();
 		orderList.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent evt) {
 				currentOrderList.removeAll();
 				descriptionArea.setText("");
 				for(OrderDish d : orderMap.get(orderList.getSelectedItem()).getOrderedDishes()){
 					currentOrderList.add(d.dish.name);
 					dishMap.put(d.dish.name, d.dish);
 					for(Extra e : d.getExtras()){
 						String str = "   - " + e.name;
 						currentOrderList.add(str);
 						dishMap.put(str, d.dish);
 					}
 					
 					commentArea.setText(orderMap.get(orderList.getSelectedItem()).getComment());
 				}
 			}
 		});
 		GridBagConstraints orderListConstraints = new GridBagConstraints();
 		orderListConstraints.gridx = 0;
 		orderListConstraints.gridy = 0;
 		orderListConstraints.weightx = 0.5;
 		orderListConstraints.weighty = 1;
 		orderListConstraints.gridwidth = 1;
 		orderListConstraints.gridheight = 6;
 		orderListConstraints.fill = GridBagConstraints.BOTH;
 		this.jFrame.add(orderList, orderListConstraints);
 		
 		currentOrderList = new List();
 		currentOrderList.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent e) {
 				Dish diplayDish = dishMap.get(currentOrderList.getSelectedItem());
 				descriptionArea.setText("Pizza nr: " + diplayDish.dishID + "\n" + diplayDish.name + "\n\n" + diplayDish.description);
 				
 			}
 		});
 		GridBagConstraints currentOrderListConstraints = new GridBagConstraints();
 		currentOrderListConstraints.gridx = 1;
 		currentOrderListConstraints.gridy = 0;
 		currentOrderListConstraints.weightx = 0.5;
 		currentOrderListConstraints.weighty = 0.5;
 		currentOrderListConstraints.gridwidth = 1;
 		currentOrderListConstraints.gridheight = 3;
 		currentOrderListConstraints.fill = GridBagConstraints.BOTH;
 		this.jFrame.add(currentOrderList, currentOrderListConstraints);
 		
 		commentArea = new TextArea("", 2, 10, TextArea.SCROLLBARS_NONE);
 		commentArea.setEditable(false);
 		GridBagConstraints commentAreaConstraints = new GridBagConstraints();
 		commentAreaConstraints.gridx = 1;
 		commentAreaConstraints.gridy = 5;
 		commentAreaConstraints.weightx = 0.5;
 		commentAreaConstraints.gridwidth = 1;
 		commentAreaConstraints.gridheight = 1;
 		commentAreaConstraints.fill = GridBagConstraints.HORIZONTAL;
 		this.jFrame.add(commentArea, commentAreaConstraints);
 		
 		descriptionArea = new TextArea("", 8, 10, TextArea.SCROLLBARS_NONE);
 		descriptionArea.setEditable(false);
 		GridBagConstraints descriptionAreaConstraints = new GridBagConstraints();
 		descriptionAreaConstraints.gridx = 1;
 		descriptionAreaConstraints.gridy = 3;
 		descriptionAreaConstraints.weightx = 0.5;
 		descriptionAreaConstraints.gridwidth = 1;
 		descriptionAreaConstraints.gridheight = 1;
 		descriptionAreaConstraints.fill = GridBagConstraints.HORIZONTAL;
 		this.jFrame.add(descriptionArea, descriptionAreaConstraints);
 		
 	}
 	
 	@Override
 	public void handleEvent(Event<?> event){
 		if(event.eventType.equals(EventType.COOK_GUI_REQUESTED)){
 			show();
 		}else if(event.eventType.equals(EventType.DELIVERY_GUI_REQUESTED)){
 			hide();
 		}else if(event.eventType.equals(EventType.ORDER_GUI_REQUESTED)){
 			hide();
 		}
 	}
 	
 	private void populateLists(){
 		for(Order o : database.getOrders()){
 			if(o.getStatus().equals(Order.REGISTERED) || o.getStatus().equals(Order.BEING_COOKED)){
 				String sc = "Ordre nr: " + o.getID() + " Antall Retter: " + o.getOrderedDishes().size(); 
 				orderList.add(sc);
 				orderMap.put(sc, o);
 			}
 		}
 	}
 
 	@Override
 	public void show() {
 		orderList.setVisible(true);
 		currentOrderList.setVisible(true);
 		commentArea.setVisible(true);
 		descriptionArea.setVisible(true);
 		jFrame.setVisible(true);
 	}
 
 	@Override
 	public void hide() {
 		orderList.setVisible(false);
 		currentOrderList.setVisible(false);
 		commentArea.setVisible(false);
 		descriptionArea.setVisible(false);
 		jFrame.setVisible(true);
 	}
 }
