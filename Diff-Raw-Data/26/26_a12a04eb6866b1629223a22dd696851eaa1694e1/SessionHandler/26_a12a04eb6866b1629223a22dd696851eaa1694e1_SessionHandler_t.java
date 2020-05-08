 package session;
 
 import java.util.Vector;
 
 public class SessionHandler {
 
 	private static Vector<session> v_sess;
 	
 	public SessionHandler()
 	{
 		v_sess = new Vector<session>();
 	}
 
 	public static void AddSession(session sess)
 	{
 		if(sess != null)
 			v_sess.add(sess);
 		for(session s : v_sess)
 			if(!s.equals(sess))
 				if(s.know_contact(sess.getUid()) && !sess.has_blocked(s.getUid()))
 					s.contact_connected(sess, false);
 	}
 
 	public static session getContactByUID(Integer _uid)
 	{
 		for(session s : v_sess)
 			if(s.getUid().equals(_uid))
 				return s;
 		return null;
 	}
 	
 	public static boolean isConnected(Integer uid)
 	{
 		if(uid == 0)
 			return false;
 		
 		if(getContactByUID(uid) == null)		
 			return false;
 		else
 			return true;
 	}
 
 	public static void Update(int diff)
 	{
 		/* TODO: ping connected client and wait a response
 			if no response, destroy the session and the listener thread
 		 */
 	}
 
 	public static void DestroySession(session sess, Thread thr) 
 	{
		if(!v_sess.isEmpty())
 		{
			for(int i=0;i<v_sess.size();i++)
			//for(session s : v_sess)
			{
				session s = v_sess.get(i);
				if(s.equals(sess))
					v_sess.remove(sess);
				else
				{
					if(s.know_contact(sess.getUid()))
						s.contact_disconnected(sess,false);
				}
			}
 		}
 		thr.interrupt();
 	}
 }
