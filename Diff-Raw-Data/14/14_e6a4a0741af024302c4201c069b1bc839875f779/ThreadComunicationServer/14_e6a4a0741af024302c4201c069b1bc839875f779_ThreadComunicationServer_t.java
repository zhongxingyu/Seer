 package clientServer;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.StringTokenizer;
 
 import dataLink.Protocol;
 import dataLink.ProtocolTCP;
 
 /**
  * Thread de comunication d'un serveur vers un client. Il permet de gerer les
  * demandes client. Connection, Deconnection, demande de lien etc...
  * 
  * @author raphael
  * 
  */
 public class ThreadComunicationServer extends Thread
 {
 	/**
 	 * Serveur a qui appartient le thread de communication.
 	 * 
 	 * @see Server
 	 */
 	private Server server;
 	/**
 	 * Socket de communication.
 	 * 
 	 * @see Socket
 	 */
 	private Socket socket;
 	/**
 	 * Protocol de communication.
 	 * 
 	 * @see Protocol
 	 */
 	private Protocol protocol;
 	/**
 	 * Parametre permettant d'areter le thread.
 	 */
 	private boolean running;
 	/**
 	 * Variable temportaire utile au traitement des requetes.
 	 */
 	private String tempVar;
 
 	/**
 	 * Constructeur par defaut du Thread.
 	 * 
 	 * @param server
 	 * @param socket
 	 */
 	public ThreadComunicationServer(Server server, Socket socket)
 	{
 		this.server = server;
 		this.socket = socket;
 		this.protocol = new ProtocolTCP(socket);
 	}
 
 	@Override
 	public void run()
 	{
 		try
 		{
 			System.out.println("Lancement du Thread de communication");
 			this.running = true;
 			while (running)
 			{
 				Thread.sleep(500);
 				// On attent que le client nous envoie un message
 				String message = protocol.readMessage();
 				// On traite ensuite le message reçu.
 				this.messageTraitement(message);
 			}
 			System.out.println("Arret du Thread de communication");
 			this.socket.close();
 			this.protocol.close();
 		} catch (IOException | InterruptedException e)
 		{
 			System.err.println("Erreur du ThreadComunicationServer, message: " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Methode permettant de traiter la reception d'un message.
 	 * 
 	 * @param message
 	 */
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
 
 	/**
 	 * Methode permettant de traiter les request d'un serveur.
 	 * 
 	 * @see Server
 	 * @param message
 	 * @param token
 	 */
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
 
 	/**
 	 * Message permettant de traiter les reply d'un serveur.
 	 * 
 	 * @see Server
 	 * @param message
 	 * @param token
 	 */
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
			this.stopThread();
 			break;
 		}
 	}
 
 	/**
 	 * Methode permettant traiter le procesus de desenregistrement
 	 * 
 	 * @param token
 	 */
 	private void unregisterClient(StringTokenizer token)
 	{
 		if (token.hasMoreTokens())
 		{
 			String id = token.nextToken();
 			this.server.removeClient(id);
 			this.protocol.sendMessage("reply:unregister:DONE");
 			this.stopThread();
		} else
		{
			this.protocol.sendMessage("reply:unregister:ERROR");
			this.stopThread();
 		}
 	}
 
 	/**
 	 * Methode permettant de gerer le procesus d'enregistrement.
 	 * 
 	 * @param token
 	 */
 	private void registerClient(StringTokenizer token)
 	{
 		// Si on a un element de plus dans le token, alors il s'agit d'un reply
 		if (token.hasMoreTokens())
 		{
 			String nextToken = token.nextToken();
 			if (token.hasMoreTokens())
 			{
 				switch (nextToken)
 				{
 				case "name":
 					// On informe le client qu'on a bien reçu son nom
 					this.protocol.sendMessage("reply:register:name:OK");
 					this.tempVar = token.nextToken();
 					this.protocol.sendMessage("request:register:port");
 					break;
 				case "port":
 					this.protocol.sendMessage("reply:register:port:OK");
 					String stringPort = token.nextToken();
 					int port = Integer.parseInt(stringPort);
 					String id = this.server.addClient(this.tempVar, this.socket, port);
 					if (id != null)
 					{
 						this.protocol.sendMessage("reply:register:id:" + id);
 						// System.out.println(this.server.getClients());
 						// this.stopThread();
 					}
 					break;
 				case "id":
 					this.protocol.sendMessage("reply:register:DONE");
 					System.out.println(this.server.getClients());
 					this.stopThread();
 					break;
 				default:
					this.stopThread();
 					break;
 				}
 			}
 		}
 		// Sinon c'est qu'il s'agit d'un request
 		else
 		{
 			System.out.println("Demande d'enregistrement");
 			try
 			{
 				PrintWriter out = new PrintWriter(this.socket.getOutputStream());
 				out.println("test");
 			} catch (IOException e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			// On envoie un message pour dire que on a bien recu son message
 			this.protocol.sendMessage("reply:register:OK");
 			// On envoie un autre pour demander son nom
 			this.protocol.sendMessage("request:register:name");
 		}
 	}
 
 	/**
 	 * Methode permettant de gerer le procesus de demande de list Client au
 	 * serveur.
 	 * 
 	 * @param token
 	 */
 	public void askListClient(StringTokenizer token)
 	{
 		if (token.hasMoreTokens())
 		{
 			this.stopThread();
 		} else
 		{
 			this.protocol.sendMessage("reply:list:" + this.server.getListClient());
 		}
 	}
 
 	/**
 	 * Methode permettant de gerer le procesus de demande d'information de
 	 * connexion client.
 	 * 
 	 * @param token
 	 */
 	public void getClientConnection(StringTokenizer token)
 	{
 		if (token.hasMoreTokens())
 		{
 			String nextToken = token.nextToken();
 			if (nextToken.length() > 20)
 			{
 				String requested = this.server.getClient(nextToken);
 				if (requested != null)
 				{
 					this.protocol.sendMessage("reply:clientConnection:" + requested);
 				} else
 				{
 					this.protocol.sendMessage("reply:clientConnection:ERROR");
 					this.stopThread();
 				}
 			} else
 			{
 				if (nextToken.equals("DONE"))
 				{
 					this.stopThread();
 				} else if (nextToken.equals("ERROR"))
 				{
 					this.stopThread();
 				} else
 				{
 					this.protocol.sendMessage("reply:clientConnection:ERROR");
 					this.stopThread();
 				}
 			}
 		} else
 		{
 			this.protocol.sendMessage("reply:clientConnection:ERROR");
 			this.stopThread();
 		}
 	}
 
 	/**
 	 * Methode permettant de stopper le thread
 	 */
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
 
 	public void setProtocol(Protocol protocol)
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
