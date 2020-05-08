 package communication.api;
 
 import communication.api.Message.*;
 
 
 /**
  * Class with static methods for serializing and deserializing a {@link Message}.
  * 
  */
 public class Serializer {
 
 	public static String serialize(Message msg) {
 		String s = "";
 		s += "type:"+msg.getType()+";";
 		s += "to:"+msg.getTo()+";";
 		s += "from:"+msg.getFrom()+";";
 		s += "data;";
 		for (Field key : msg.getKeys()) {
 			s += key+":";
 			s += msg.getData(key)+";";
 		}
 		return s;
 	}
 	
 	public static Message deserialize(String msg) {
 		Type type;
 		String to, from;
 		type = Type.valueOf(find("type", 0, msg));
 		to = find("to", 0, msg);
 		from = find("from", 0, msg);
 		Message m = new Message(type, to, from);
 		int index2 = msg.indexOf("data;")+5;
 		while (index2 < msg.length()-1) {
 			int index1 = msg.indexOf(":", index2);
 			String key = msg.substring(index2, index1);
 			m.addData(Field.valueOf(key), find(key, index2 ,msg));
 			index2 = msg.indexOf(";", index1)+1;
 		}
 		return m;
 	}
 	
 	private static String find(String key, int start, String msg) {
 		int index1 = -1;
 		String value = null;
		if ((index1 = msg.indexOf(key)) >= 0) {
 			int index2 = msg.indexOf(";", index1+1);
 			value = msg.substring(index1+key.length()+1, index2);
 		}
 		return value;
 	}
 }
