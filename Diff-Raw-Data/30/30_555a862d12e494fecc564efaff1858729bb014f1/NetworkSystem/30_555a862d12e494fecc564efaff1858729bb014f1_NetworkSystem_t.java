 package DD.Network;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import DD.Chat.ChatSystem;
 import DD.CombatSystem.CombatSystem;
 import DD.Message.Message;
 import DD.Message.NetworkMessage;
 import DD.Network.Client.ClientSystem;
 import DD.Network.Client.Interpreter.ClientInterpreter;
 import DD.Network.Server.ServerSystem;
 import DD.Network.Server.Interpreter.ServerInterpreter;
 
 /*****************************************************************************************************
 * NetworkSystem will create a singleton and be the interface through which GameSystem and ChatSystem 
 * (along with any other systems) will communicate with ClientSystem or ServerSystem based on whether
 * the user is a client or a server.
 * 
 * @author Carlos Vasquez
 ******************************************************************************************************/
 
 public class NetworkSystem extends Network
 {
 	/************************************ Class Constants *************************************/
 	private static int I = 0;
 	public static enum NetworkType
 	{
 		CLIENT(),
 		SERVER();
 		
 	} /* end Action enum */
 	
 	/************************************ Class Attributes *************************************/
 	private NetworkType networkType;
 	private Network network;
 	
 	/************************************ Class Methods *************************************/
 	public NetworkSystem() {} /* end NetworkSystem constructor */
 	
 	@Override
 	public boolean sendMessage(int sender, int receiver, Message message)
 	{
		System.out.println("network" + network);
 		return network.sendMessage(sender, receiver, message);
 	} /* end sendMessage method */
 	
 	/******************************************************************************
 	 ******************************* Getter Methods *******************************
 	 ******************************************************************************/
 	public boolean getMessage(int listenerID, NetworkMessage message) 
 	{ /* Get message from sender. (This is how senders pass messages for ClientSystem/ServerSystem to interpret). */
 		return network.getMessage(listenerID, message);
 		
 	} /* end getMessage method */
 	
 	public int getNetID()
 	{
 		Integer id = null;
 		switch(networkType)
 		{
 			case SERVER:
 				id = Network.GM_USER_ID;
 				break;
 			case CLIENT:
 				id = ((ClientSystem)network).getClientID();
 				break;
 		} /* end switch */
 
 		return id;
 	} /* end getUserID */
 	
 	public void start() 
 	{
 		if (network == null)
 		{
 			switch(networkType)
 			{
 				case SERVER:
 					network = new ServerSystem();
 					ServerInterpreter.setServerSystem((ServerSystem)network);
 					ClientInterpreter.setClientSystem(null);
 					break;
 				case CLIENT:
 					network = new ClientSystem();
 					ServerInterpreter.setServerSystem(null);
 					ClientInterpreter.setClientSystem((ClientSystem)network);
 					break;
 			} /* end switch */
 		} /* end if */
 		network.start();
 	} /* end start method */
 
 	public void stop() 
 	{
		if(network != null) network.stop();
 		network = null;
 	} /* end stop method */
 
 	public void terminate() 
 	{
 		network.terminate();
 	} /* end terminate method */
 	
 	public void printUsers()
 	{
 		network.printUsers();		
 	} /* end printUsers method */
 	
 	/******************************************************************************
 	 ******************************* Setter Methods *******************************
 	 ******************************************************************************/
 	public void setNetworkType(NetworkType type)
 	{
 		this.networkType = type;
 		switch(networkType)
 		{
 			case SERVER:
 				network = new ServerSystem();
 				break;
 			case CLIENT:
 				network = new ClientSystem();
 				break;
 		} /* end switch */
 
 	} /* end setNetworkType */
 
 	@Override
 	public void setCombatSystem(CombatSystem cs) 
 	{
 		network.setCombatSystem(cs);		
 	} /* end setCombatSystem method */
 
 	@Override
 	public void setChatSystem(ChatSystem chat) 
 	{
 		network.setChatSystem(chat);		
 	} /* end setChatSystem method */
 	
 	@Override
 	public void setUsername(String username)
 	{
 		this.username = username;
 		network.setUsername(username);
 	} /* end setUsername */
 	
 	@Override
 	public void setServerIP(InetAddress serverIP)
 	{
 		network.setServerIP(serverIP);
 	} /* end setServerIP method */
 	
 	@Override
 	public boolean setServerIP(String ip)
 	{
 		return network.setServerIP(ip);
 	} /* end setServerIP method */
 	
 } /* end Network class */
