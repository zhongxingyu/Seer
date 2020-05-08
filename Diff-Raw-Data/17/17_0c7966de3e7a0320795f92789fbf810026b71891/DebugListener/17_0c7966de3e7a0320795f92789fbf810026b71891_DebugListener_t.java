 package net.xmlrouter.mod_pubsub.client;
 
 import java.util.*;
 
 /**
  * @author msg
  *
  */
 public class DebugListener implements Listener
 {
 	/**
 	 * @see net.xmlrouter.mod_pubsub.client.Listener#onMessage(HashMap)
 	 */
 	public void onMessage(Map msg)
 	{
 		dumpMessage(msg);
 	}
 	
 	public static void dumpMessage(Map msg)
 	{
 		Iterator it = msg.keySet().iterator();
 		String name;
 		while (it.hasNext())
 		{
 			name = (String)it.next();
 			System.out.print(name);
 			System.out.print("=");
 			System.out.print(msg.get(name));
 			System.out.println();
 		}
		System.out.println();		
 	}
 }
