 package icm.server.intenthandler;
 
 import icm.dao.Employee;
 import icm.dao.EmployeeDAO;
 import icm.dao.ICMPersistentManager;
 import icm.dao.User;
 import icm.dao.UserDAO;
 import icm.intent.EmployeeLoginIntent;
 import icm.intent.UserLoginIntent;
 import icm.response.Response;
 import icm.server.ICMServer;
 
 import java.io.IOException;
 
 import ocsf.server.ConnectionToClient;
 
 import org.orm.PersistentException;
 import org.orm.PersistentSession;
 
 public class EmployeeLoginIntentHandler implements IntentHandler<EmployeeLoginIntent>
 {
 	private ICMServer server;
 	
 	public EmployeeLoginIntentHandler( ICMServer server )
 	{
 		this.server = server;
 	}
 	
 	@Override
 	public void execute( EmployeeLoginIntent intent, ConnectionToClient client ) 
 	{
 		
 		try
 		{	
 			Response response = new Response(intent.getIntentNo(), null);		// will be a boolean
 			Response elaboration = new Response(intent.getIntentNo(), null);	// will be a string
 			Response endOfResponse = new Response(intent.getIntentNo(), null);	// will be null
 			
 			// get user
 			PersistentSession session = ICMPersistentManager.instance().getSession();
 			Employee employee = EmployeeDAO.getEmployeeByORMID(intent.getUser());
 			session.close();
 			
 			// no such user?
 			if( employee==null )
 			{
 				response.setData(false);
 				elaboration.setData("no such user");
 				client.sendToClient(response);
 				client.sendToClient(elaboration);
 				client.sendToClient(endOfResponse);
 				return;
 			}
 			
 			// incorrect password?
 			if( !employee.getPassword().equals(intent.getPassword()) )
 			{
 				response.setData(false);
 				elaboration.setData("password incorrect");
 				client.sendToClient(response);
 				client.sendToClient(elaboration);
 				client.sendToClient(endOfResponse);
 				return;
 			}
 			
 			// check if user is already connected from somewhere else
 			for( Thread t : server.getClientConnections() )
 			{
 				if( t instanceof ConnectionToClient )
 				{
 					ConnectionToClient c = (ConnectionToClient) t;
 					Integer curid = (Integer) c.getInfo(User.class.toString());
 					if( curid!=null )
 					{
 						if( curid.equals(intent.getUser()) )
 						{
 							response.setData(false);
 							elaboration.setData("user already connected from another machine");
 							client.sendToClient(response);
 							client.sendToClient(elaboration);
 							client.sendToClient(endOfResponse);
 							return;
 						}
 					}
 				}
 			}
 			
 			// save client id on this connection
			client.setInfo(Employee.class.toString(), employee.getEmployeeId());
 			
 			//respond
 			response.setData(true);
 			elaboration.setData(employee);
 			client.sendToClient(response);
 			client.sendToClient(elaboration);
 			client.sendToClient(endOfResponse);
 		}
 		catch (IOException e) {e.printStackTrace();} 
 		catch (PersistentException e) {e.printStackTrace();} 
 	}
 }
