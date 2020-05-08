 /*
  * This software is licensed under the GPLv3 license, included as
  * ./GPLv3-LICENSE.txt in the source distribution.
  *
  * Portions created by Brett Wilson are Copyright 2008 Brett Wilson.
  * All rights reserved.
  */
 
 
 package org.wwscc.util;
 
 import java.util.EnumMap;
 import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

 import javax.swing.SwingUtilities;
 
 public class Messenger
 {
	private static final Logger log = Logger.getLogger(Messenger.class.getCanonicalName());
 	private static EnumMap<MT, HashSet<MessageListener>> listPtrs =
 		new EnumMap<MT, HashSet<MessageListener>>(MT.class);
 
 	static public void unregisterAll(MessageListener listener)
 	{
 		for (HashSet<MessageListener> s : listPtrs.values())
 			s.remove(listener);
 	}
 
 	public static void unregister(MT type, MessageListener listener)
 	{
 		HashSet<MessageListener> h = listPtrs.get(type);
 		if (h != null)
 			h.remove(listener);
 	}
 
 	public static void register(MT type, MessageListener listener)
 	{
 		HashSet<MessageListener> h = listPtrs.get(type);
 		if (h == null)
 		{
 			h = new HashSet<MessageListener>();
 			listPtrs.put(type, h);
 		}
 		h.add(listener);
 	}
 
 	public static void sendEvent(final MT type, final Object data)
 	{
		SwingUtilities.invokeLater(new Runnable() { public void run() { 
			try {
				sendEventNow(type, data); 
			} catch (Exception e) {
				log.log(Level.WARNING, e.getMessage(), e);
			}
		}});
 	}
 
 	public static void sendEventNow(MT type, Object data)
 	{
 		HashSet<MessageListener> h = listPtrs.get(type);
 		if (h == null) return;
 		for (MessageListener ml : h)
 			ml.event(type, data);
 	}
 }
 
 
