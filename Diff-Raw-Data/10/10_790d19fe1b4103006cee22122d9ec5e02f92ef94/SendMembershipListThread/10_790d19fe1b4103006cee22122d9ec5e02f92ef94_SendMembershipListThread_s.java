 package edu.uiuc.boltdb.groupmembership;
 
 import java.lang.reflect.Type;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.reflect.TypeToken;
 
 public class SendMembershipListThread extends Thread 
 {
 	//private static org.apache.log4j.Logger log = Logger.getLogger(SendMembershipListThread.class);
 	InetAddress ipaddress;
 	int port;
 	
 	public SendMembershipListThread(InetAddress ipaddress, int port) 
 	{
 		this.ipaddress = ipaddress;
 		this.port = port;
 	}
 
 	public void run() 
 	{
 		try
 		{
 			DatagramSocket clientSocket = new DatagramSocket();
 			Gson gson = new GsonBuilder().create();
 			Type typeOfHashMap = new TypeToken<HashMap<String, MembershipBean>>(){}.getType();
 			Iterator<Map.Entry<String, MembershipBean>> iterator = GroupMembership.membershipList.entrySet().iterator();
 			HashMap<String,MembershipBean> listToSend = new HashMap<String,MembershipBean>();
 			while (iterator.hasNext()) 
 			{
 				Map.Entry<String, MembershipBean> entry = iterator.next();
 				if(entry.getValue().toBeDeleted)
 					continue;
 				listToSend.put(entry.getKey(), entry.getValue());
 			}	
 			String json = gson.toJson(listToSend, typeOfHashMap);
 			System.out.println(GroupMembership.pid+"-Sending "+json);
 			byte[] jsonBytes = json.getBytes();
 			DatagramPacket dataPacket = new DatagramPacket(jsonBytes, jsonBytes.length, ipaddress, port);
 			clientSocket.send(dataPacket);
 			clientSocket.close();
 		}
 		catch(Exception e)
 		{
 			System.out.println("EXCEPTION:In SendMembershipListThread");
 		}
 	}	
 }
