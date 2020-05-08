 package clientServer;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.StringTokenizer;
 
 import dataLink.Protocol;
 import dataLink.ProtocolTCP;
 
 /**
  * Thread de comunication avec un client. Il permet de gerer les demandes
  * client. Connection, Deconnection, demande de lien etc...
  * 
  * @author raphael
  * 
  */
 public class ThreadComunicationClient extends Thread
 {
 	private Client client;
 	private Socket socket;
 	private Protocol protocol;
 	private boolean running;
 	private String ipServer;
 
	public ThreadComunicationClient(Client client,String ipServer)
 	{
 		this.client = client;
		this.ipServer=ipServer;
 	}
 
	public ThreadComunicationClient(Client client, Socket socket,String ipServer)
 	{
 		this.client = client;
 		this.socket = socket;
		this.ipServer=ipServer;
 		this.protocol = new ProtocolTCP(socket);
 	}
 
 	@Override
 	public void run()
 	{
 		try
 		{
 			this.socket = new Socket(this.ipServer, 30970);
 			this.protocol = new ProtocolTCP(socket);
 		} catch (Exception e)
 		{
 			System.err.println("Erreur du ThreadComunicationClient,Connexion, message: " + e.getMessage());
 			e.printStackTrace();
 		}
 		try
 		{
 			System.out.println("Lancement du Thread de communication");
 			this.running = true;
 			while (running)
 			{
 				Thread.sleep(500);
 				// On attent que le serveur nous envoie un message
 				String message = protocol.readMessage();
 				// On traite ensuite le message reçu.
 				this.messageTraitement(message);
 			}
 			System.out.println("Arret du Thread de communication");
 			this.socket.close();
 			this.protocol.close();
 		} catch (IOException | InterruptedException e)
 		{
 			System.err.println("Erreur du ThreadComunicationClient, message: " + e.getMessage());
 		}
 	}
 
 	private void messageTraitement(String message)
 	{
 		System.out.println("Début du traitement du message : " + message);
 		StringTokenizer token = new StringTokenizer(message, ":");
 		String firstToken = token.nextToken();
 		if (token.hasMoreTokens())
 		{
 			String nextToken = token.nextToken();
 			switch (firstToken)
 			{
 			case "request":
 				this.messageTraitementRequest(nextToken, token);
 				break;
 			case "reply":
 				this.messageTraitementReply(nextToken, token);
 				break;
 			case "end":
 				this.stopThread();
 				break;
 
 			default:
 				this.stopThread();
 				break;
 			}
 		}
 	}
 
 	private void messageTraitementRequest(String message, StringTokenizer token)
 	{
 		switch (message)
 		{
 		case "register":
 			this.registerClient(token);
 			break;
 		case "unregister":
 			this.unregisterClient(token);
 			break;
 		case "clientConnection":
 			this.getClientConnection(token);
 			break;
 
 		default:
 			break;
 		}
 	}
 
 	private void messageTraitementReply(String message, StringTokenizer token)
 	{
 		switch (message)
 		{
 		case "register":
 			this.registerClient(token);
 			break;
 		case "unregister":
 			this.unregisterClient(token);
 			break;
 		case "list":
 			this.askListClient(token);
 			break;
 		case "clientConnection":
 			this.getClientConnection(token);
 			break;
 
 		default:
 			break;
 		}
 	}
 
 	public void unregisterClient(StringTokenizer token)
 	{
 		if (token.hasMoreTokens())
 		{
 			String nextToken = token.nextToken();
 			switch (nextToken)
 			{
 			case "DONE":
 				this.stopThread();
 				break;
 
 			default:
 				this.stopThread();
 				break;
 			}
 		} else
 		{
 			this.protocol.sendMessage("request:unregister:" + this.client.getId());
 		}
 	}
 
 	public void registerClient(StringTokenizer token)
 	{
 		if (token.hasMoreTokens())
 		{
 			String nextToken = token.nextToken();
 			switch (nextToken)
 			{
 			case "name":
 				if (!token.hasMoreTokens())
 				{
 					this.protocol.sendMessage("reply:register:name:" + this.client.getName());
 				}
 				break;
 			case "port":
 				if (!token.hasMoreTokens())
 				{
 					this.protocol.sendMessage("reply:register:port:" + this.client.getListeningUDPPort());
 				}
 				break;
 			case "id":
 				if (token.hasMoreTokens())
 				{
 					this.client.setId(token.nextToken());
 					this.protocol.sendMessage("reply:register:id:OK");
 				}
 				break;
 			case "OK":
 				break;
 			case "DONE":
 				this.stopThread();
 				break;
 			default:
 				this.stopThread();
 				break;
 			}
 		} else
 		{
 			this.protocol.sendMessage("request:register");
 		}
 	}
 
 	public void askListClient(StringTokenizer token)
 	{
 		if (token.hasMoreTokens())
 		{
 			String list = token.nextToken();
 			this.client.addClientList(list);
 			this.protocol.sendMessage("reply:list:DONE");
 			this.stopThread();
 		} else
 		{
 			this.protocol.sendMessage("request:list");
 		}
 	}
 
 	public void getClientConnection(StringTokenizer token)
 	{
 		if (token.hasMoreTokens())
 		{
 			// System.out.println(token.nextToken());
 			String[] elements = token.nextToken().split(",");
 			if (elements.length == 4)
 			{
 				ClientServerData client;
 				try
 				{
 					client = new ClientServerData(elements[0], elements[1], InetAddress.getByName(elements[2]), Integer.parseInt(elements[3]));
 
 					// this.client.startDialogToClient(client);
 					this.client.getClients().add(client);
 					System.out.println("Ajout du client recu");
 				} catch (NumberFormatException | UnknownHostException e)
 				{
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		} else
 		{
 
 		}
 	}
 
 	public void getClientConnection(String clientId)
 	{
 		this.protocol.sendMessage("request:clientConnection:" + clientId);
 	}
 
 	public void stopThread()
 	{
 		this.running = false;
 	}
 
 	public Socket getSocket()
 	{
 		return socket;
 	}
 
 	public void setSocket(Socket socket)
 	{
 		this.socket = socket;
 	}
 
 	public Protocol getProtocol()
 	{
 		return protocol;
 	}
 
 	public void setProtocol(ProtocolTCP protocol)
 	{
 		this.protocol = protocol;
 	}
 
 	public boolean isRunning()
 	{
 		return running;
 	}
 
 	public void setRunning(boolean running)
 	{
 		this.running = running;
 	}
 }
