 package network;
 
 import java.io.*;
 import java.net.*;
 
 import clients.ServerClient;
 
 import other.Constants;
 import other.Constants.Function;
 
 import managers.CashierManager;
 import managers.CookManager;
 import managers.OrderManager;
 import members.Cashier;
 import members.Cook;
 import datatypes.DataPacket;
 import datatypes.Order;
 
/**
 * Handles incoming connections
 * Processes the DataPacket and acts accordingly
 *
 * @author Tom
 * @version 0.1
 * @history Feb 26, 2012: Created class
 */
 public class Serverlet extends Thread
 {
 	Socket socket;
 	ServerClient client;
 
	/**
	 * Constructor
	 * @param _socket
	 * @param _client
	 */
 	public Serverlet(Socket _socket, ServerClient _client)
 	{
 		this.socket = _socket;
 		this.client = _client;
 
 		// set timeout
 		try{ this.socket.setSoTimeout(Constants.CONNECTION_TIMEOUT); }
 		catch (Exception e) { System.out.println("Serverlet.Severlet: Error, exception: " + e.getMessage()); }
 	}
 
 	@Override
 	public void run()
 	{
 		try
 		{
 			// create the streams
 			BufferedInputStream bis = new BufferedInputStream(this.socket.getInputStream());  
 			ObjectInputStream ois = new ObjectInputStream(bis);
 
 			// get the input
 			DataPacket dataPacket = (DataPacket)ois.readObject();
 			System.out.println("Serverlet recieved DataPacket: " + dataPacket.function.toString());
 
 			switch(dataPacket.function)
 			{
 				case SET_ORDER_COOKED:
 					OrderManager.getInstance().setOrderCooked(dataPacket.order);
 					client.updateDisplayText("Order " + dataPacket.order.getId() + " cooked");
 					break;
 
 				case SET_ORDER_DELIVERED:
 					OrderManager.getInstance().setOrderDelivered(dataPacket.order);
 					client.updateDisplayText("Order " + dataPacket.order.getId() + " delivered");
 					break;
 
 				case GET_NEXT_ORDER:
 					Order nextOrder = OrderManager.getInstance().getOrder();
 					dataPacket.order = nextOrder;
 					client.updateDisplayText("Next order: " + nextOrder.getId());
 					break;
 
 				case GET_ORDER:
 					Order order = OrderManager.getInstance().getOrderForDelivery(dataPacket.data);
 					dataPacket.order = order;
 					client.updateDisplayText("Get order: " + order.getId());
 					break;
 
 				case GET_UNDELIVERED_ORDER:
 					Order undeliveredOrder = OrderManager.getInstance().getUndeliveredOrder();
 					dataPacket.order = undeliveredOrder;
 					if(undeliveredOrder != null) client.updateDisplayText("Get undelivered order: " + undeliveredOrder.getId());
 					break;
 
 				case ADD_ORDER:
 					OrderManager.getInstance().addOrder(dataPacket.order);
 					client.updateDisplayText("Add order");
 					break;
 
 				case CREATE_RANDOM_ORDER:
 					Order randomOrder = OrderManager.getInstance().createRandomOrder(dataPacket.cashier);
 					dataPacket.order = randomOrder;
 					//client.updateDisplayText("Create random order");
 					break;
 
 				case GET_CASHIER:
 					Cashier cashier = CashierManager.getInstance().getRandomCashier();
 					while(cashier.loggedIn()) CashierManager.getInstance().getRandomCashier();
 					dataPacket.cashier = cashier;
 					client.updateDisplayText("Get cashier: " + cashier.getFirstName().charAt(0) + "." + cashier.getSurname());
 					break;
 
 				case GET_COOK:
 					Cook cook = CookManager.getInstance().getRandomCook();
 					while(cook.loggedIn()) cook = CookManager.getInstance().getRandomCook();
 					dataPacket.cook = cook;
 					client.updateDisplayText("Get cook: " + cook.getFirstName().charAt(0) + "." + cook.getSurname());
 					break;
 
 				case EMPLOYEE_LOG_IN:
 				case EMPLOYEE_LOG_OUT:
 					boolean loggedIn = (dataPacket.function == Function.EMPLOYEE_LOG_IN) ? true : false;
 					if(dataPacket.cashier != null) CashierManager.getInstance().getCashier(dataPacket.cashier.getId()).loggedIn(loggedIn);
 					else if(dataPacket.cook != null) CookManager.getInstance().getCook(dataPacket.cook.getId()).loggedIn(loggedIn);
 					
 					String loggedInString = (dataPacket.function == Function.EMPLOYEE_LOG_IN) ? " log in" : " log out";
 					if(dataPacket.cashier != null) client.updateDisplayText("Cashier: " + dataPacket.cashier.getFirstName().charAt(0) + "." + dataPacket.cashier.getSurname() + loggedInString);
 					else if(dataPacket.cook != null) client.updateDisplayText("Cook: " + dataPacket.cook.getFirstName().charAt(0) + "." + dataPacket.cook.getSurname() + loggedInString);
 					
 					break;
 
 				default: 
 					System.out.println("Error function not recognised: " + dataPacket.function.toString());
 					break;
 			}
 
 			if(dataPacket.returnTransmission)
 			{
 				BufferedOutputStream bos = new BufferedOutputStream(this.socket.getOutputStream());
 				ObjectOutputStream oos = new ObjectOutputStream(bos);
 				oos.writeObject(dataPacket);
 				oos.flush();
 				bos.close();
 				oos.close();
 			}
 
 			// update the client display
 			this.client.update();
 
 			// communication complete, close the connection
 			bis.close(); 
 			ois.close();
 			this.socket.close();			
 		}
 		catch (Exception e) {}// System.out.println("Serverlet.run: Error, exception: " + e.getMessage()); }
 	}
 }
