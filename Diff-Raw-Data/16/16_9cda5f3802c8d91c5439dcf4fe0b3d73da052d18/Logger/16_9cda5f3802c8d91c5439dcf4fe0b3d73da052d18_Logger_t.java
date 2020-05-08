 package edu.wpi.tmathmeyer.protocol;
 
 import java.util.HashMap;
 
 
 public class Logger {
	@SuppressWarnings("rawtypes")
	public static HashMap<String, Class> map = new HashMap<String, Class>();
 	public static void log(Packet p){
		Logger.map.put(p.getPacketID()+"", p.getClass());
 	}
 	@SuppressWarnings("rawtypes")
	public static Class<? extends Class> retrieve(byte ID){
 		return Logger.map.get(ID+"").getClass();
 	}
 }
