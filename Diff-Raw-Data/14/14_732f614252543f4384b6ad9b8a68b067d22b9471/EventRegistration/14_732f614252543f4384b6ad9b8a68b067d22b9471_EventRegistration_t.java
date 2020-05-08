 package mave.minecraftjs;
 
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Listener;
 import org.mozilla.javascript.Function;
 import org.mozilla.javascript.Scriptable;
 
 public class EventRegistration
 {
 	public Script m_script;
 	public Event.Type m_eventType;
 	public Function m_scriptFunction;
 	public Priority m_priority;
 	public Scriptable m_scope;
 	
 	public Listener m_listener;
 	
 	public static Event.Type stringToEventType(String s)
 	{
 		s = s.toUpperCase();
 		int idx = s.indexOf('_');
 		if (idx == -1)
 		{
 			throw new IllegalArgumentException();
 		}
 		
 		String category = s.substring(0, idx);
 		String event = s.substring(idx + 1);
 		if (category.length() == 0 || event.length() == 0)
 		{
 			throw new IllegalArgumentException();
 		}
 		
 		try
 		{
 			return Event.Type.valueOf(category + "_" + event);
 		}
 		catch (IllegalArgumentException e)
 		{
 			idx = event.indexOf('_');
 			if (idx == -1)
 			{
 				throw new IllegalArgumentException();
 			}
 			String realCategory = event.substring(0, idx);
 			event = event.substring(idx + 1);
 			if (realCategory.length() == 0 || event.length() == 0)
 			{
 				throw new IllegalArgumentException();
 			}
 			
 			return Event.Type.valueOf(realCategory + "_" + event);
 		}
 	}
 	
 	public static Priority stringToPriority(String s)
 	{
		if (s.length() < 2)
		{
			throw new IllegalArgumentException();
		}
		
		return Priority.valueOf(Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase());
 	}
 }
