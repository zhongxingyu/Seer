 package clientServer;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Scanner;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import dataLink.Protocol;
 import dataLink.ProtocolUDP;
 
 /**
  * 
  * @author raphael
  * @category Class permetant de representer un client. Extends de la class
  *           AbstractClientServer.
  * @see AbstractClientServer
  */
 public class Client extends AbstractClientServer
 {
 	/**
 	 * Identifiant unique du client, fournit par le serveur.
 	 */
 	private String id;
 	/**
 	 * Nom du client
 	 */
 	private String name;
 	/**
 	 * Liste des clients connu par leurs identifiants public. Key : cle public
 	 * client, value : nom client
 	 */
 	private HashMap<String, String> clientList;
 	/**
 	 * Liste des dialogs que le client a.
 	 * 
 	 * @see ClientDialog
 	 */
 	private Vector<ClientDialog> dialogs;
 	/**
 	 * Numero du port d'ecoute UDP du client
 	 */
 	private int listeningUDPPort;
 	/**
 	 * Thread d'ecoute du port UDP
 	 * 
 	 * @see ThreadListenerUDP
 	 */
 	private ThreadListenerUDP threadListenerUDP;
 	/**
 	 * Protocol de communication du client.
 	 * 
 	 * @see Protocol
 	 */
 	private Protocol protocol;
 	/**
 	 * Adresse IP du serveur.
 	 * 
 	 * @see Server
 	 */
 	private String ipServer;
 	/**
 	 * Thread de communication client. Il permet de comuniquer avec le serveur
 	 * 
 	 * @see Server
 	 */
 	private ThreadComunicationClient threadComunicationClient;
 
 	/**
 	 * Constructeur par defaut du Client.
 	 * 
 	 * @param name
 	 * @param listeningUDPPort
 	 * @param ipServer
 	 */
 	public Client(String name, int listeningUDPPort, String ipServer)
 	{
 		super();
 		this.name = name;
 		this.listeningUDPPort = listeningUDPPort;
 		this.protocol = new ProtocolUDP(listeningUDPPort);
 		this.clientList = new HashMap<String, String>();
 		this.dialogs = new Vector<ClientDialog>();
 		this.ipServer = ipServer;
 		this.threadComunicationClient = new ThreadComunicationClient(this, ipServer);
 		this.threadListenerUDP = new ThreadListenerUDP(this, this.protocol);
 		this.threadListenerUDP.start();
 	}
 
 	/**
 	 * Methode permetant au client de s'enregister auprès du serveur.
 	 * 
 	 * @see Server
 	 */
 	public void registerToServer()
 	{
 		try
 		{
 			this.launchThread();
 			Thread.sleep(500);
 			threadComunicationClient.registerClient(new StringTokenizer(""));
 			new Thread(new Runnable()
 			{
 
 				@Override
 				public void run()
 				{
 					while (true)
 					{
 						try
 						{
 							while (id == null || id == "")
 							{
 								Thread.sleep(200);
 							}
 							while (id != "")
 							{
 								protocol.sendMessage("alive:" + id, InetAddress.getByName(ipServer), 30971);
 							}
 						} catch (InterruptedException | UnknownHostException e)
 						{
 							System.err.println("Erreur du thread client d'envoie du message alive, message:" + e.getMessage());
 						}
 					}
 				}
 			}).start();
 		} catch (InterruptedException e)
 		{
 			System.err.println("Erreur d'enregistrement du client au serveur, message : " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Methode permettant de se deconecter du serveur.
 	 * 
 	 * @see Server
 	 */
 	public void unregisterToServer()
 	{
 		try
 		{
 			this.launchThread();
 			Thread.sleep(500);
 			this.threadComunicationClient.unregisterClient(new StringTokenizer(""));
 		} catch (InterruptedException e)
 		{
 			System.err.println("Erreur de desenregistrement du client au serveur, message : " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Methode permetant de demander la liste des clients connecte au serveur.
 	 * 
 	 * @see Server
 	 */
 	public void askListToServer()
 	{
 		try
 		{
 			this.launchThread();
 			Thread.sleep(500);
 			this.threadComunicationClient.askListClient(new StringTokenizer(""));
 		} catch (InterruptedException e)
 		{
 			System.err.println("Erreur de demande de liste du client au serveur, message : " + e.getMessage());
 		}
 
 	}
 
 	/**
 	 * Methode permettant de demander les informations d'un client au serveur,
 	 * afin de pouvoir ensuite demarer un dialog avec. Le parametre correspond a
 	 * la cle public du client.
 	 * 
 	 * @param clientId
 	 */
 	public void askClientConnectionToServer(String clientId)
 	{
 		try
 		{
 			// On recherche si on a deja demarer un dialogue avec le client.
 			boolean alreadyDone = false;
 			for (ClientDialog dialog : this.dialogs)
 			{
 				if (dialog.getClients().size() == 1 && dialog.getClients().firstElement().getId().equals(clientId))
 				{
 					alreadyDone = true;
 				}
 			}
 			// Si aucun dialog n'a ete demarer
 			if (!alreadyDone)
 			{
 				this.launchThread();
 				Thread.sleep(500);
 				// On demande les informations du client
 				this.threadComunicationClient.getClientConnection(clientId);
 				int cpt = 0;
 				int sizeClients = this.getClients().size();
 				// On attent de les recevoir
 				while (cpt < 500 && this.getClients().size() == sizeClients)
 				{
 					Thread.sleep(200);
 					cpt++;
 				}
 				// Si on les a bien reçu on demare une conversation
 				if (this.getClients().size() != sizeClients)
 				{
 					this.startDialogToClient(this.getClients().lastElement());
 				}
 			}
 		} catch (InterruptedException e)
 		{
 			System.err.println("Erreur de demande d'information client du client au serveur, message : " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Methode permettant de demarer un dialogue avec un client. Le parametre
 	 * est une representation d'un client.
 	 * 
 	 * @see ClientServerData
 	 * @see ClientDialog
 	 * @param client
 	 */
 	public void startDialogToClient(ClientServerData client)
 	{
 		try
 		{
 			// On verifie si il existe deja un dialog avec le client
 			boolean alreadyDone = false;
 			for (ClientDialog dialog : this.dialogs)
 			{
 				if (dialog.getClients().size() == 1 && dialog.getClients().firstElement().getId().equals(client.getId()))
 				{
 					alreadyDone = true;
 				}
 			}
 			// Si ce n'est pas le cas
 			if (!alreadyDone)
 			{
 				// On cree un dialogue
 				ClientDialog dialog = new ClientDialog(this, this.protocol);
 				// On y ajoute le client avec qui l'on discute
 				dialog.addClient(client);
 				// On recupere l'id du dialogue generee
 				String idDialog = dialog.getIdDialog();
 				// On envoie les informations du dialog au client avec qui l'on
 				// souhaite discuter
 				protocol.sendMessage("dialog:newDialog:" + idDialog, client.getIp(), client.getPort());
 				protocol.sendMessage("dialog:newDialog:clients:" + idDialog + ":" + this.id, client.getIp(), client.getPort());
 				// On ajoute le dialogue a la liste des dialogue du client
 				this.dialogs.add(dialog);
 			}
 		} catch (NumberFormatException e)
 		{
 			System.err.println("Erreur de demarage d'un dialogue client, message : " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Methode permettant d'envoyer un message sur un dialogue.
 	 * 
 	 * @param message
 	 * @param idDialog
 	 * @return true si le message est partie, false sinon
 	 */
 	public boolean sendMessageToClient(String message, String idDialog)
 	{
 		ClientDialog dialog = null;
 		for (ClientDialog dial : this.dialogs)
 		{
 			if (dial.getIdDialog().equals(idDialog))
 			{
 				dialog = dial;
 			}
 		}
 		if (dialog != null)
 		{
 			dialog.sendMessage(message);
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Methode permettant de demarer le thread de communication avec le serveur.
 	 */
 	public void launchThread()
 	{
 		if (this.threadComunicationClient.isInterrupted())
 		{
 			this.threadComunicationClient.start();
 		} else
 		{
 			this.threadComunicationClient = new ThreadComunicationClient(this, this.ipServer);
 			this.threadComunicationClient.start();
 		}
 	}
 
 	/**
 	 * Methode permettant de mettre a jour la liste des clients connu. La liste
 	 * est envoye par le serveur. Elle est de la forme
 	 * "ClePublic-NomCLient,ClePublic-NomClient...."
 	 * 
 	 * @param list
 	 */
 	public void addClientList(String list)
 	{
 		StringTokenizer token = new StringTokenizer(list, ",");
 		this.clientList = new HashMap<String, String>();
 		while (token.hasMoreTokens())
 		{
 			String element = token.nextToken();
 			String elements[] = element.split("-");
 			if (elements.length > 1 && !elements[0].equals(this.id))
 			{
 				this.clientList.put(elements[0], elements[1]);
 			}
 		}
 		System.out.println(this.clientList);
 	}
 
 	@Override
 	public void treatIncomeTCP(Object object)
 	{
 		// Pour le moment pas d'income TCP a géré vue que aucune machine ne se
 		// connecte a un client en TCP
 	}
 
 	@Override
 	public void treatIncomeUDP(String message)
 	{
 		System.out.println(message);
 		StringTokenizer token = new StringTokenizer(message, ":");
 		String firstToken = token.nextToken();
 		switch (firstToken)
 		{
 		case "dialog":
 			this.treatIncomeDialog(token);
 			break;
 		case "listClient":
 			this.treatIncomeList(token);
 			break;
 
 		default:
 			break;
 		}
 	}
 
 	/**
 	 * Methode permettant de traiter la reception d'un liste client et de la
 	 * rediriger vers la bonne methode en faisant le bon traitement de donnees.
 	 * 
 	 * @param token
 	 */
 	public void treatIncomeList(StringTokenizer token)
 	{
 		if (token.hasMoreTokens())
 		{
 			String nextToken = token.nextToken();
 			this.addClientList(nextToken);
 		}
 	}
 
 	/**
 	 * Methode permettant de traiter la reception de message concernant les
 	 * dialogues et de rediriger vers la bonne methode en faisant le bon
 	 * traitement de donnees.
 	 * 
 	 * @param token
 	 */
 	public void treatIncomeDialog(StringTokenizer token)
 	{
 		if (token.hasMoreTokens())
 		{
 			String nextToken = token.nextToken();
 			switch (nextToken)
 			{
 			// Creation d'un nouveau dialogue
 			case "newDialog":
 				if (token.hasMoreTokens())
 				{
 					// On récupère l'id de conversation
 					String idDialog = token.nextToken();
 					// SI c'est bien un id de conversation, alors on crée la
 					// conversation
 					if (idDialog.length() > 20)
 					{
 						// On crée le dialog
 						this.dialogs.add(new ClientDialog(idDialog, this, this.protocol));
 					}
 					// Si il s'agit d'ajouter des clients à la conversation
 					else if (idDialog.equals("clients"))
 					{
 						System.out.println("je recois un message pour ajouter les clients");
 						if (token.hasMoreTokens())
 						{
 							String realIdDialog = token.nextToken();
 							if (token.hasMoreTokens())
 							{
 								System.out.println("je recherche le dialog " + realIdDialog);
 								ClientDialog dialog = null;
 								for (ClientDialog dialogL : this.dialogs)
 								{
 									System.out.println(dialogL.getIdDialog());
 									if (dialogL.getIdDialog().trim().equals(realIdDialog.trim()))
 									{
 										System.out.println("affectation du dialog");
 										dialog = dialogL;
 									}
 								}
 								System.out.println("le dialog est :" + dialog.getIdDialog());
 								if (dialog != null)
 								{
 									// String[] clients =
 									// token.nextToken().split(",");
 									String clientsT = token.nextToken();
 									String[] clients = clientsT.split(",");
 									for (String client : clients)
 									{
 										System.out.println("client: " + client);
 										boolean estAjoute = false;
 										for (ClientServerData clientSe : this.getClients())
 										{
 											if (clientSe.getId().equals(client))
 											{
 												dialog.addClient(clientSe);
 												estAjoute = true;
 											}
 										}
 										if (!estAjoute)
 										{
 											ClientServerData newClient = new ClientServerData(client, this.clientList.get(client), ((ProtocolUDP) protocol).getLastAdress(), ((ProtocolUDP) protocol).getLastPort());
 											System.out.println(newClient);
 											this.getClients().add(newClient);
 											dialog.addClient(newClient);
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 				break;
 			// Si il s'agit d'un message reçu
 			case "message":
 				if (token.hasMoreTokens())
 				{
 					String idDialog = token.nextToken();
 					// SI c'est bien un id de conversation, alors on redirige le message vers la conversation
 					if (idDialog.length() > 20 && token.hasMoreTokens())
 					{
 						for (ClientDialog dialog : this.dialogs)
 						{
 							if (dialog.getIdDialog().equals(idDialog))
 							{
 								// On récupère le message
 								String message = token.nextToken();
 								while (token.hasMoreTokens())
 								{
 									message += ":" + token.nextToken();
 								}
 								// On indique qu'on a reçu un message
 								dialog.receiveMessage(message);
 							}
 						}
 					}
 				}
 				break;
 
 			default:
 
 				break;
 			}
 		} else
 		{
 
 		}
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 
 	public String getId()
 	{
 		return id;
 	}
 
 	public void setId(String id)
 	{
 		this.id = id;
 	}
 
 	public HashMap<String, String> getClientList()
 	{
 		return clientList;
 	}
 
 	public void setClientList(HashMap<String, String> clientList)
 	{
 		this.clientList = clientList;
 	}
 
 	public int getListeningUDPPort()
 	{
 		return listeningUDPPort;
 	}
 
 	public void setListeningUDPPort(int listeningUDPPort)
 	{
 		this.listeningUDPPort = listeningUDPPort;
 	}
 
 	public Vector<ClientDialog> getDialogs()
 	{
 		return dialogs;
 	}
 
 	public void setDialogs(Vector<ClientDialog> dialogs)
 	{
 		this.dialogs = dialogs;
 	}
 
 	public ThreadComunicationClient getThreadComunicationClient()
 	{
 		return threadComunicationClient;
 	}
 
 	public void setThreadComunicationClient(ThreadComunicationClient threadComunicationClient)
 	{
 		this.threadComunicationClient = threadComunicationClient;
 	}
 
 	public ThreadListenerUDP getThreadListenerUDP()
 	{
 		return threadListenerUDP;
 	}
 
 	public void setThreadListenerUDP(ThreadListenerUDP threadListenerUDP)
 	{
 		this.threadListenerUDP = threadListenerUDP;
 	}
 
 	public static void main(String[] args)
 	{
 		Scanner sc = new Scanner(System.in);
 		Client client = new Client("raphael", 30001, "192.168.99.230");
 		boolean running = true;
 		while (running)
 		{
 			System.out.println("-register");
 			System.out.println("-unregister");
 			System.out.println("-list");
 			System.out.println("-connectionClient");
 			System.out.println("-sendMessage");
 			System.out.println("-refresh");
 			System.out.println("-close");
 			System.out.print("lecture=");
 			switch (sc.nextLine())
 			{
 			case "register":
 				client.registerToServer();
 				break;
 			case "unregister":
 				client.unregisterToServer();
 				break;
 			case "list":
 				client.askListToServer();
 				break;
 			case "connectionClient":
 				Object[] list = client.clientList.keySet().toArray();
 				System.out.println("Liste des clients:");
 				for (int i = 0; i < list.length; i++)
 				{
 					System.out.println("-" + i + ":" + client.clientList.get(list[i]));
 				}
 				System.out.println("entré le numéro d'ordre dans la liste:");
 				String num = sc.nextLine();
 				client.askClientConnectionToServer((String) list[Integer.parseInt(num)]);
 				break;
 			case "sendMessage":
 				sc.reset();
 				System.out.println("Message:");
 				String mes = sc.nextLine();
 				client.sendMessageToClient(mes, client.dialogs.firstElement().getIdDialog());
 				break;
 			case "refrech":
 				break;
 			case "close":
 				running = false;
 				break;
 
 			default:
 				break;
 			}
 
 		}
 		sc.close();
 	}
 }
