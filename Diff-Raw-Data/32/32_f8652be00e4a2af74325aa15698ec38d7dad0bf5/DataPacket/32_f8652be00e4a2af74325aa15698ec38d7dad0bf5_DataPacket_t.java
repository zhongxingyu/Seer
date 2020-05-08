 package datatypes;
 
 import java.io.Serializable;
 
 import other.Constants.Function;
 
 import members.Cashier;
 import members.Cook;
 
 /**
  * A wrapper for an number of objects
 * Used to transmit data via sockets.
  *
  * @author Tom
  * @version 0.1
  * @history Feb 16, 2012: Created class
  */
 public class DataPacket implements Serializable
 {
 	private static final long serialVersionUID = 1L;
 	
	// the relevant Order (optional)
 	public Order order = null;
	// the relevant Cashier (optional)
 	public Cashier cashier = null;
	// the relevant Cook (optional)
 	public Cook cook = null;
	// the 'purpose' of the DataPacket
 	public Function function;
	// any extra data (optional)
 	public String data = "";
	// whether a response is required 
 	public boolean returnTransmission = false;
 	
 	public DataPacket(Function _function)
 	{
 		this.function = _function;
 		
 		switch (_function)
 		{
 			case ADD_ORDER: 
 			case SET_ORDER_DELIVERED:
 			case DELIVER_ORDER:
 			case EMPLOYEE_LOG_IN:
 			case EMPLOYEE_LOG_OUT:
 				break;
 				
 			case SET_ORDER_COOKED:
 			case CREATE_RANDOM_ORDER:
 			case GET_CASHIER:
 			case GET_COOK:
 			case GET_NEXT_ORDER: 
 			case GET_UNDELIVERED_ORDER:
 			case GET_ORDER:
 				this.returnTransmission = true; 
 				break;
 
 			default:
 				System.out.println("DataPacket.DataPacket: Error invalid function passed: " + _function);
 				break;
 		}
 	}
 }
