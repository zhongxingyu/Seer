 package icm.server.intenthandler;
 
 import icm.dao.ICMPersistentManager;
 import icm.dao.User;
 import icm.dao.UserDAO;
 import icm.dao.UserMailCriteria;
 import icm.intent.RequestBrowseIntent;
 import icm.intent.UserMailBrowseIntent;
 import icm.response.Response;
 
 import java.io.IOException;
 
 import ocsf.server.ConnectionToClient;
 
 import org.hibernate.criterion.Restrictions;
 import org.orm.PersistentException;
 import org.orm.PersistentSession;
 
 public class UserRequestBrowseIntentHandler implements IntentHandler<RequestBrowseIntent>
 {
 
 	@Override
 	public void execute( RequestBrowseIntent intent, ConnectionToClient client )
 	{
 		try 
 		{
 			int userid = (int) client.getInfo(User.class.toString());
 			
 			// handle info is null?
 			PersistentSession session = ICMPersistentManager.instance().getSession();
 			
 			User user = UserDAO.getUserByORMID(userid);
 			
			UserMailCriteria mailCriteria = new UserMailCriteria(session); 
			mailCriteria.add( Restrictions.eq("owner.id", user.getUserID()) );
			Object mails[] = (Object[])mailCriteria.list().toArray();
 			
			for( Object o : mails )
 			{
 				try 
 				{
 					client.sendToClient( new Response(intent.getIntentNo(), o) );
 				} 
 				catch (IOException e) {e.printStackTrace();}
 			}
 			
 			client.sendToClient( new Response(intent.getIntentNo(), null) );
 		}
 		catch (PersistentException e) {e.printStackTrace();} 
 		catch (IOException e) {e.printStackTrace();}
 	}
 
 }
