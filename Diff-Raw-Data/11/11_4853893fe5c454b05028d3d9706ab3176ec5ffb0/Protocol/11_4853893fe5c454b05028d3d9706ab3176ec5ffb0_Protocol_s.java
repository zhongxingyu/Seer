 import java.util.*;
 
 public class Protocol
 {
 	public HashMap<String,String> protocols;//holds the numbers corresponding to each protocol
 	
 	public Protocol()
 	{
 		protocols = new HashMap();
 		
 		protocols.put("LOGIN", "100");
 		protocols.put("HELP", "101");
 		protocols.put("BYE", "111");
 		
 		protocols.put("REMOVE", "200");
 		
 		protocols.put("LIST_USERS", "300");
 		protocols.put("LIST_GAMES", "301");
 		protocols.put("OBSERVE", "302");
 		protocols.put("UNOBSERVE", "303");
 		protocols.put("PLAY", "304");
 
 		protocols.put("INVALID_GAME", "400");
 		protocols.put("BAD_COMMAND", "401");
 		
 		protocols.put("100", "LOGIN");
 		protocols.put("101", "HELP");
 		protocols.put("111", "BYE");
 		
 		protocols.put("200", "REMOVE");
 		
 		protocols.put("300", "LIST_USERS");
 		protocols.put("301", "LIST_GAMES");
 		protocols.put("302", "OBSERVE");
 		protocols.put("303", "UNOBSERVE");
 		protocols.put("304", "PLAY");
 
 		protocols.put("400", "INVALID_GAME");
 		protocols.put("401", "BAD_COMMAND");
 	}
 	
 	public String makePacket(String data, String protocol)
 	{
 		String packet = "";
 		int length = data.length();//finds the length of the packet
 		
 		packet += "5 "; //the location of the data
 		packet += Integer.toString(length) + " ";//the size of the packet
 		
 		packet += protocols.get(protocol) + " ";//adds the protocol number
 		
 		packet += data;//the data being sent in the packet
 		
 		return packet;
 	}
 	
 	public String getData(String packet)
 	{
 		int pos = Integer.parseInt(packet.substring(0,1));//gets the start location of the data
 		String data = packet.substring(pos);//gets the data
 		
 		return data;
 		
 	}
 	
 	public int getLength(String packet)
 	{
 		String sizeStr;
 		int size;
 		
 		//finds the end of the length field
 		int pos = packet.indexOf(' ');//the end of the first field in the original string
		pos = packet.substring(pos+1).indexOf(' ');//the end of the 2nd/length field in the string minus the data location field
	
		sizeStr = packet.substring(2,pos+2);//gets the size field from the original string (including the data location field)
 		size = Integer.parseInt(sizeStr);
 		
 		return size;
 	}
 	
 	public String getCommand(String packet)
 	{
 		String commStr;
		System.out.println(packet);
 		int pos = packet.indexOf(' ');//finds the first space
 		pos = pos + 1 + packet.substring(pos+1).indexOf(' ');//finds the 2nd space (after which the protocol number is located)
 		
 		commStr = packet.substring(pos+1,pos+4);//gets the protocol number
 		
		System.out.println(commStr);
 		commStr = protocols.get(commStr);//gets the protocol name
 		
 		return commStr;
 	
 	}
 
 }
