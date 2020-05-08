 package li.rudin.arduino.core.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class MessageParser
 {
 	/**
 	 * Parses a message from a byte array
 	 * @param bytes
 	 * @param length
 	 * @return
 	 */
 	public static List<Message> parse(byte[] bytes, int length)
 	{
 		return parse(new String(bytes, 0, length));
 	}
 	
 	/**
 	 * Parses a message from a string
 	 * @param msg
 	 * @return
 	 */
 	public static List<Message> parse(String msg)
 	{
 		ArrayList<Message> list = new ArrayList<>();
 		String[] parts = msg.split("[;]");
 		
 		for (String part: parts)
 		{
 			Message message = parseMessage(part);
 			if (message != null)
 				list.add(message);
 		}
 		
 		return list;
 	}
 	
 	/**
 	 * Parses a single message
 	 * @param msg
 	 * @return
 	 */
 	private static Message parseMessage(String msg)
 	{
 		String[] parts = msg.split("[:]");
 		
		if (parts.length != 2)
 			return null;
		
		return new Message(parts[0], parts[1]);
 	}
 }
