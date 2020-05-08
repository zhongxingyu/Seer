 package net.ropelato.meetpoint.data;
 
 import java.util.*;
 
 public class Game extends Thread
 {
 	public static final int MAX_CLIENTS = 10;
 	public static final long CLIENT_TIMEOUT = 30000;
 
 	private Map<String, Client> clients = new HashMap<String, Client>();
 
 	public Game()
 	{
 		this.start();
 	}
 
 	public synchronized String createClient(int characterId) throws Exception
 	{
 		if(clients.size() < MAX_CLIENTS)
 		{
 			String clientId = generateClientId();
 			clients.put(clientId, new Client(clientId, characterId));
 			System.out.println("client conntected: " + clientId);
 			return clientId;
 		}
 		else
 		{
 			return null;
 		}
 	}
 
 	public synchronized String getInformationFromClientsAlive(String clientId)
 	{
 		String clientInformation = "";
 
 		for(ChatMessage chatMessage : getClient(clientId).getAllMessages())
 		{
 			clientInformation += "@"+chatMessage.sender+";"+chatMessage.message+"\n";
 		}
 
 		for(Client client : clients.values())
 		{
			if(!client.getClientId().equals(clientId) && client.isPlaying())
 			{
 				clientInformation += client.getInformationString() + "\n";
 			}
 		}
 		return clientInformation;
 	}
 
 	public synchronized void addNewChatMessage(ChatMessage message)
 	{
 		for(Client client : clients.values())
 		{
 			client.addMessage(message);
 		}
 	}
 
 	public synchronized void cleanUp()
 	{
 		List<String> clientsToRemove = new ArrayList<String>();
 		for(Map.Entry<String, Client> clientEntry : clients.entrySet())
 		{
 			if(clientEntry.getValue().getLastAccess() < (System.currentTimeMillis() - CLIENT_TIMEOUT))
 			{
 				clientsToRemove.add(clientEntry.getKey());
 			}
 		}
 		for(String clientId : clientsToRemove)
 		{
 			System.out.println("client disconnected: " + clientId);
 			clients.remove(clientId);
 		}
 	}
 
 	public synchronized boolean hasClient(String clientId)
 	{
 		return clients.containsKey(clientId);
 	}
 
 	public synchronized Client getClient(String clientId)
 	{
 		return clients.get(clientId);
 	}
 
 	public void run()
 	{
 		while(true)
 		{
 			try
 			{
 				cleanUp();
 				Thread.sleep(5000);
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private String generateClientId()
 	{
 		String clientId = "";
 		for(int i = 0; i < 8; i++)
 		{
 			clientId += (char)(Math.random() * 26 + 65);
 		}
 
 		if(clients.containsKey(clientId))
 		{
 			return generateClientId();
 		}
 		else
 		{
 			return clientId;
 		}
 	}
 }
