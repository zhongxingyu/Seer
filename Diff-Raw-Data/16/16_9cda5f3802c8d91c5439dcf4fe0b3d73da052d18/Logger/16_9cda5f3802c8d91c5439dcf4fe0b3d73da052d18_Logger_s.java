 package edu.wpi.tmathmeyer.protocol;
 
 import java.util.HashMap;
 
 
 public class Logger {
	public static HashMap<String, Packet> map = new HashMap<String, Packet>();
 	public static void log(Packet p){
		Logger.map.put(p.getPacketID()+"", p);
 	}
 	@SuppressWarnings("rawtypes")
	public static Class retrieve(byte ID){
 		return Logger.map.get(ID+"").getClass();
 	}
 }
