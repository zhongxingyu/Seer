 package model;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Scanner;
 import java.util.StringTokenizer;
 import java.util.ArrayList;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 
 import network.Protocol;
 import network.ProtocolUDP;
 
 /**
  * Classe représentant un client. Hérite de la classe AbstractClientServer.
  * 
  * @author Dorian, Mickaël, Raphaël, Thibault <br/>
  * @see AbstractClientServer
  */
 public class Client extends AbstractClientServer
 {
 	/**
 	 * Identifiant unique du client, fourni par le serveur.
 	 */
 	private String id;
 	/**
 	 * Nom du client
 	 */
 	private String name;
 	/**
 	 * Mot de passe du client
 	 */
 	private String password;
 	/**
 	 * Message perso du client.
 	 */
 	private String personalMessage;
 	/**
 	 * Liste des clients connue par leurs identifiants public. Key : clé
 	 * publique client, value : nom client
 	 */
 	private HashMap<String, String[]> clientList;
 	/**
 	 * Liste des dialogs que le client a.
 	 * 
 	 * @see ClientDialog
 	 */
 	private ArrayList<ClientDialog> dialogs;
 	/**
 	 * Numéro du port d'écoute UDP du client
 	 */
 	private int listeningUDPPort;
 	/**
 	 * Thread d'écoute du port UDP
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
 	 * Port UDP du serveur
 	 */
 	private int udpServerPort;
 	/**
 	 * Port TCP du serveur
 	 */
 	private int tcpServerPort;
 	/**
 	 * Thread de communication client. Il permet de comuniquer avec le serveur
 	 * 
 	 * @see Server
 	 */
 	private ThreadComunicationClient threadComunicationClient;
 
 	/**
 	 * 
 	 */
 	private String errorsMessages;
 
 	/**
 	 * Constructeur d'un Client avec 3 paramètres le nom, le port udp et l'ip
 	 * serveur.
 	 * 
 	 * @param name
 	 *            nom du client
 	 * @param listeningUDPPort
 	 *            port d'écoute UDP
 	 * @param ipServer
 	 *            ip du server
 	 */
 	public Client(String name, int listeningUDPPort, String ipServer)
 	{
 		super();
 		try
 		{
 			FileHandler file = new FileHandler("logClient.txt", true);
 			SimpleFormatter formatter = new SimpleFormatter();
 			file.setFormatter(formatter);
 			setLogger(Logger.getLogger(Client.class.toString()));
 			getLogger().setLevel(Level.FINEST);
 			getLogger().addHandler(file);
 		} catch (SecurityException | IOException e)
 		{
 			System.err.println("Erreur d'ouverture du fichier de log, message : " + e.getMessage());
 		}
 		getLogger().info("Lancement du Client");
 		this.errorsMessages = "";
 		this.name = name;
 		this.password = "";
 		this.personalMessage = "";
 		this.id = "";
 		this.listeningUDPPort = listeningUDPPort;
 		this.protocol = new ProtocolUDP(listeningUDPPort);
 		this.clientList = new HashMap<String, String[]>();
 		this.dialogs = new ArrayList<ClientDialog>();
 		this.ipServer = ipServer;
 		this.udpServerPort = 30971;
 		this.tcpServerPort = 30970;
 		this.threadComunicationClient = new ThreadComunicationClient(this, ipServer);
 		this.threadListenerUDP = new ThreadListenerUDP(this, this.protocol);
 		this.threadListenerUDP.start();
 	}
 
 	/**
 	 * Constructeur du client avec paramétrage du serveur.
 	 * 
 	 * @param name
 	 *            nom du client
 	 * @param listeningUDPPort
 	 *            port d'écoute UDP
 	 * @param ipServer
 	 *            ip du server
 	 * @param udpServerPort
 	 *            port udp du serveur
 	 * @param tcpServerPort
 	 *            port tcp du serveur
 	 */
 	public Client(String name, int listeningUDPPort, String ipServer, int udpServerPort, int tcpServerPort)
 	{
 		super();
 		try
 		{
 			FileHandler file = new FileHandler("logClient.txt", true);
 			SimpleFormatter formatter = new SimpleFormatter();
 			file.setFormatter(formatter);
 			setLogger(Logger.getLogger(Client.class.toString()));
 			getLogger().setLevel(Level.FINEST);
 			getLogger().addHandler(file);
 		} catch (SecurityException | IOException e)
 		{
 			System.err.println("Erreur d'ouverture du fichier de log, message : " + e.getMessage());
 		}
 		getLogger().info("Lancement du Client");
 
 		this.errorsMessages = "";
 		this.name = name;
 		this.password = "";
 		this.personalMessage = "";
 		this.id = "";
 		this.listeningUDPPort = listeningUDPPort;
 		this.protocol = new ProtocolUDP(listeningUDPPort);
 		this.clientList = new HashMap<String, String[]>();
 		this.dialogs = new ArrayList<ClientDialog>();
 		this.ipServer = ipServer;
 		this.udpServerPort = udpServerPort;
 		this.tcpServerPort = tcpServerPort;
 		this.threadComunicationClient = new ThreadComunicationClient(this, ipServer);
 		this.threadListenerUDP = new ThreadListenerUDP(this, this.protocol);
 		this.threadListenerUDP.start();
 	}
 
 	/**
 	 * Methode permettant au client de s'enregister auprès du serveur.
 	 * 
 	 * @see Server
 	 */
 	public void registerToServer()
 	{
 		try
 		{
 			getLogger().info("Enregistrement auprès du serveur : " + this.ipServer);
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
 								Thread.sleep(1000);
 								protocol.sendMessage("alive:" + id, InetAddress.getByName(ipServer), udpServerPort);
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
 			getLogger().severe("Erreur d'enregistrement du client au serveur, message : " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Méthode permettant au client de se déconnecter du serveur.
 	 * 
 	 * @see Server
 	 */
 	public void unregisterToServer()
 	{
 		try
 		{
 			getLogger().info("Desenregistrement auprès du serveur, serveur : " + this.ipServer);
 			this.launchThread();
 			Thread.sleep(500);
 			this.threadComunicationClient.unregisterClient(new StringTokenizer(""));
 		} catch (InterruptedException e)
 		{
 			getLogger().severe("Erreur de desenregistrement du client au serveur, message : " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Méthode permettant au client de demander la liste des clients connectés
 	 * au serveur.
 	 * 
 	 * @see Server
 	 */
 	public void askListToServer()
 	{
 		try
 		{
 			getLogger().info("Demande de list client auprès du serveur, serveur : " + this.ipServer);
 			this.launchThread();
 			Thread.sleep(500);
 			this.threadComunicationClient.askListClient(new StringTokenizer(""));
 		} catch (InterruptedException e)
 		{
 			getLogger().severe("Erreur de demande de liste du client au serveur, message : " + e.getMessage());
 		}
 
 	}
 
 	/**
 	 * Méthode permettant au client de demander au serveur les informations d'un
 	 * autre client connecté, afin de démarrer un dialogue.
 	 * 
 	 * @param clientId
 	 *            clé publique du client
 	 */
 	public void askClientConnectionToServer(String clientId)
 	{
 		try
 		{
 			getLogger().info("Demande d'information client : " + clientId);
 			// On recherche si on a déjà démarré un dialogue avec le client.
 			boolean alreadyDone = false;
 			for (ClientDialog dialog : this.dialogs)
 			{
 				if (dialog.getClients().size() == 1 && dialog.getClients().get(0).getId().equals(clientId))
 				{
 					alreadyDone = true;
 					// Si la conversation existe et que on souhaite en démarrer
 					// une c'est quelle est simplement cachée
 					// alors on la remet en fonction
 					dialog.setInUse(true);
 				}
 			}
 			// Si aucun dialog n'a été demarrer
 			if (!alreadyDone)
 			{
 				this.launchThread();
 				Thread.sleep(500);
 				// On demande les informations du client
 				this.threadComunicationClient.getClientConnection(clientId);
 				int cpt = 0;
 				int sizeClients = this.getClients().size();
 				// On attend de les recevoir
 				while (cpt < 500 && this.getClients().size() == sizeClients)
 				{
 					Thread.sleep(200);
 					cpt++;
 				}
 				// Si on les a bien reçu on démarre une conversation
 				if (this.getClients().size() != sizeClients)
 				{
 					this.startDialogToClient(this.getClients().get(this.getClients().size() - 1));
 				}
 			}
 		} catch (InterruptedException e)
 		{
 			getLogger().severe("Erreur de demande d'information client du client au serveur, message : " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Méthode permettant au client de démarrer un dialogue avec un autre
 	 * client.
 	 * 
 	 * @see ClientServerData
 	 * @see ClientDialog
 	 * @param client
 	 *            client avec lequel on souhaite discuter
 	 */
 	public void startDialogToClient(ClientServerData client)
 	{
 		try
 		{
 			getLogger().info("Demarage d'un dialogue avec un client : " + client.getId());
 			// On verifie si il existe déjà un dialog avec le client
 			boolean alreadyDone = false;
 			for (ClientDialog dialog : this.dialogs)
 			{
 				if (dialog.getClients().size() == 1 && dialog.getClients().get(0).getId().equals(client.getId()))
 				{
 					alreadyDone = true;
 				}
 			}
 			// Si ce n'est pas le cas
 			if (!alreadyDone)
 			{
 				// On crée un dialogue
 				ClientDialog dialog = new ClientDialog(this, this.protocol);
 				// On y ajoute le client avec qui l'on discute
 				dialog.addClient(client);
 				// On recupère l'id du dialogue généré
 				String idDialog = dialog.getIdDialog();
 				// On envoie les informations du dialog au client avec qui l'on
 				// souhaite discuter
 				protocol.sendMessage("dialog:newDialog:" + idDialog, client.getIp(), client.getPort());
 				protocol.sendMessage("dialog:newDialog:clients:" + idDialog + ":" + this.id, client.getIp(), client.getPort());
 				// On ajoute le dialogue à la liste des dialogue du client
 				this.dialogs.add(dialog);
 			}
 		} catch (NumberFormatException e)
 		{
 			getLogger().severe("Erreur de demarage d'un dialogue client, message : " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Méthode permettant au client d'ajouter un autre client à un dialogue ,
 	 * sauf si il fait déjà parti du dialogue.
 	 * 
 	 * @see ClientDialog
 	 * @param clientId
 	 *            client avec lequel on souhaite discuter
 	 * @param dialog
 	 *            dialogue auquel ajouter le client
 	 */
 	public void addClientToDialog(String clientId, ClientDialog dialog)
 	{
 		System.out.println("Ajout d'un client a un dialog");
 		try
 		{
 			boolean alreadyKnow = false;
 			ClientServerData clientAdd = null;
 			System.out.println("On recherche le client");
 			for (ClientServerData client : this.getClients())
 			{
 				if (client.getId().equals(clientId))
 				{
 					alreadyKnow = true;
 					clientAdd = client;
 				}
 			}
 			if (!alreadyKnow)
 			{
 				System.out.println("On ne le connais pas, donc on le recherche au serveur");
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
 				clientAdd = this.getClients().get(this.getClients().size() - 1);
 			}
 
 			if (clientAdd != null)
 			{
 				System.out.println("On envoie les messages de dialog au nouveau client");
 				protocol.sendMessage("dialog:newDialog:" + dialog.getIdDialog(), clientAdd.getIp(), clientAdd.getPort());
 				protocol.sendMessage("dialog:newDialog:clients:" + dialog.getIdDialog() + ":" + this.id, clientAdd.getIp(), clientAdd.getPort());
 
 				String listClient = this.id;
 				for (ClientServerData client : dialog.getClients())
 				{
 					listClient += "," + client.getId();
 					protocol.sendMessage("dialog:clients:" + dialog.getIdDialog() + ":" + clientAdd.getId(), client.getIp(), client.getPort());
 					// A garder, ancienne méthode de groupe, en cas d'erreur
 					// protocol.sendMessage("dialog:clients:" +
 					// dialog.getIdDialog() + ":" + client.getId(),
 					// clientAdd.getIp(), clientAdd.getPort());
 				}
 				dialog.addClient(clientAdd);
 				protocol.sendMessage("dialog:clients:" + dialog.getIdDialog() + ":" + listClient, clientAdd.getIp(), clientAdd.getPort());
 			}
 		} catch (InterruptedException e)
 		{
 			getLogger().severe("Erreur d'ajout d'un client, message : " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Méthode permettant au client d'envoyer un message dans un dialogue.
 	 * 
 	 * @param message
 	 *            message que l'on souhaite envoyer
 	 * @param idDialog
 	 *            id du dialogue avec lequel on souhaite envoyer le message
 	 * @return true si le message est parti, false sinon
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
 	 * Méthode permettant de désactiver un dialogue.
 	 * 
 	 * @see ClientDialog
 	 * @param idDialog
 	 *            id du dialogue
 	 * @return true si réussi, false sinon.
 	 */
 	public boolean hideDialog(String idDialog)
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
 			dialog.setInUse(false);
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Méthode permettant de démarrer le thread de communication avec le
 	 * serveur.
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
 	 * Méthode permettant de mettre à jour la liste des clients connus. La liste
 	 * est envoyée par le serveur. Elle est de la forme
 	 * "ClePublic-NomCLient,ClePublic-NomClient...."
 	 * 
 	 * @param list
 	 *            liste des clients envoyée par le serveur
 	 */
 	public void addClientList(String list)
 	{
 		StringTokenizer token = new StringTokenizer(list, ",");
 		this.clientList = new HashMap<String, String[]>();
 		while (token.hasMoreTokens())
 		{
 			String element = token.nextToken();
 			String elements[] = element.split("-");
 			if (elements.length > 1 && !elements[0].equals(this.id))
 			{
				this.clientList.put(elements[0], new String[] {elements[1],elements[2]});
 			}
 		}
 		System.out.println(this.clientList);
 	}
 
 	/**
 	 * Méthode permettant de traiter les éléments reçus en TCP
 	 * 
 	 * @param object
 	 *            paquet reçu en TCP
 	 */
 	@Override
 	public void treatIncomeTCP(Object object)
 	{
 		// Pour le moment pas d'income TCP a géré vue qu'aucune machine ne se
 		// connecte à un client en TCP
 	}
 
 	/**
 	 * Méthode permettant de traiter les éléments reçus en UDP
 	 * 
 	 * @param message
 	 *            paquet reçu en UDP
 	 */
 	@Override
 	public void treatIncomeUDP(String message)
 	{
 		System.out.println(message);
 		StringTokenizer token = new StringTokenizer(message, ":");
 		if (token.hasMoreTokens())
 		{
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
 	}
 
 	/**
 	 * Méthode permettant de traiter la réception d'une liste de clients et de
 	 * la rediriger vers la bonne méthode pour le bon traitement de données.
 	 * 
 	 * @param token
 	 *            message sous forme de tokens {@link #treatIncomeUDP(String)}
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
 	 * Méthode permettant de traiter la réception de message concernant les
 	 * dialogues et de rediriger vers la bonne methode avec le bon traitement de
 	 * données.
 	 * 
 	 * @param token
 	 *            message sous forme de tokens {@link #treatIncomeUDP(String)}
 	 */
 	public void treatIncomeDialog(StringTokenizer token)
 	{
 		if (token.hasMoreTokens())
 		{
 			String nextToken = token.nextToken();
 			switch (nextToken)
 			{
 			// Création d'un nouveau dialogue
 			case "newDialog":
 				if (token.hasMoreTokens())
 				{
 					// On récupère l'id de conversation
 					String idDialog = token.nextToken();
 					// Si c'est bien un id de conversation, alors on crée la
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
 											ClientServerData newClient = new ClientServerData(client, this.clientList.get(client)[0], ((ProtocolUDP) protocol).getLastAdress(), ((ProtocolUDP) protocol).getLastPort());
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
 			case "clients":
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
 									try
 									{
 										this.launchThread();
 
 										Thread.sleep(500);
 
 										// On demande les informations du client
 										this.threadComunicationClient.getClientConnection(client);
 										int cpt = 0;
 										int sizeClients = this.getClients().size();
 										// On attent de les recevoir
 										while (cpt < 500 && this.getClients().size() == sizeClients)
 										{
 											Thread.sleep(200);
 											cpt++;
 										}
 										// Si on les a bien reçu on démarre une
 										// conversation
 										if (this.getClients().size() != sizeClients)
 										{
 											dialog.addClient(this.getClients().get(this.getClients().size() - 1));
 										}
 									} catch (InterruptedException e)
 									{
 										getLogger().severe("Erreur d'ajout d'un client a une conversation, message : " + e.getMessage());
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
 					// Si c'est bien un id de conversation, alors on redirige le
 					// message vers la conversation
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
 
 	/**
 	 * Getter du nom du client
 	 * 
 	 * @return name, le nom du client
 	 */
 	public String getName()
 	{
 		return name;
 	}
 
 	/**
 	 * Setter qui fixe le nom du client
 	 * 
 	 * @param name
 	 *            nom du client
 	 */
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 
 	/**
 	 * Getter du mot de passe du client
 	 * 
 	 * @return password, le mot de passe du client
 	 */
 	public String getPassword()
 	{
 		return password;
 	}
 
 	/**
 	 * Setter qui fixe le mot de passe du client
 	 * 
 	 * @param password
 	 *            mot de passe du client
 	 */
 	public void setPassword(String password)
 	{
 		this.password = password;
 	}
 
 	/**
 	 * Getter de la clé du client
 	 * 
 	 * @return id, la clé du client
 	 */
 	public String getId()
 	{
 		return id;
 	}
 
 	/**
 	 * Setter qui fixe la clé du client
 	 * 
 	 * @param id
 	 *            la clé du client
 	 */
 	public void setId(String id)
 	{
 		this.id = id;
 	}
 
 	/**
 	 * Getter de la liste de client
 	 * 
 	 * @return clientList, liste de client envoyée par le serveur
 	 */
 	public HashMap<String, String[]> getClientList()
 	{
 		return clientList;
 	}
 
 	/**
 	 * Setter qui fixe la liste de client
 	 * 
 	 * @param clientList
 	 *            liste de clients connectés
 	 */
 	public void setClientList(HashMap<String, String[]> clientList)
 	{
 		this.clientList = clientList;
 	}
 
 	/**
 	 * Getter du port UDP d'écoute
 	 * 
 	 * @return listeningUDPPort, port UDP
 	 */
 	public int getListeningUDPPort()
 	{
 		return listeningUDPPort;
 	}
 
 	/**
 	 * Setter qui fixe le port d'écoute UDP
 	 * 
 	 * @param listeningUDPPort
 	 *            port UDP
 	 */
 	public void setListeningUDPPort(int listeningUDPPort)
 	{
 		if (threadListenerUDP.isAlive())
 		{
 			try
 			{
 				this.protocol.sendMessage("End", InetAddress.getByName("localhost"), this.listeningUDPPort);
 			} catch (UnknownHostException e)
 			{
 				getLogger().severe("Erreur d'auto envoie de message, message : " + e.getMessage());
 			}
 			this.threadListenerUDP.stopThread();
 			this.protocol.close();
 		}
 		this.listeningUDPPort = listeningUDPPort;
 		this.protocol = new ProtocolUDP(listeningUDPPort);
 
 		this.threadListenerUDP = new ThreadListenerUDP(this, this.protocol);
 		this.threadListenerUDP.start();
 	}
 
 	/**
 	 * Getter de la liste de dialogues
 	 * 
 	 * @return dialogs, liste de dialogues
 	 */
 	public ArrayList<ClientDialog> getDialogs()
 	{
 		return dialogs;
 	}
 
 	/**
 	 * Setter qui fixe la liste de dialogues
 	 * 
 	 * @param dialogs
 	 *            liste de dialogues
 	 */
 	public void setDialogs(ArrayList<ClientDialog> dialogs)
 	{
 		this.dialogs = dialogs;
 	}
 
 	/**
 	 * Getter du thread de communication entre le client et le serveur
 	 * 
 	 * @return threadComunicationClient, thread communication CLient/Serveur
 	 */
 	public ThreadComunicationClient getThreadComunicationClient()
 	{
 		return threadComunicationClient;
 	}
 
 	/**
 	 * Setter qui fixe le thread de communication entre le client et le serveur
 	 * 
 	 * @param threadComunicationClient
 	 *            thread communication CLient/Serveur
 	 */
 	public void setThreadComunicationClient(ThreadComunicationClient threadComunicationClient)
 	{
 		this.threadComunicationClient = threadComunicationClient;
 	}
 
 	/**
 	 * Getter du thread d'écoute UDP
 	 * 
 	 * @return threadListenerUDP, thread d'écoute UDP
 	 */
 	public ThreadListenerUDP getThreadListenerUDP()
 	{
 		return threadListenerUDP;
 	}
 
 	/**
 	 * Setter qui fixe le thread d'écoute UDP
 	 * 
 	 * @param threadListenerUDP
 	 *            thread d'écoute UDP
 	 */
 	public void setThreadListenerUDP(ThreadListenerUDP threadListenerUDP)
 	{
 		this.threadListenerUDP = threadListenerUDP;
 	}
 
 	/**
 	 * Getter de l'IP du serveur
 	 * 
 	 * @return String, adresse IP du serveur
 	 */
 	public String getIpServer()
 	{
 		return ipServer;
 	}
 
 	/**
 	 * Setter qui fixe l'IP du serveur
 	 * 
 	 * @param ipServer
 	 *            adresse IP du serveur
 	 */
 	public void setIpServer(String ipServer)
 	{
 		this.ipServer = ipServer;
 	}
 
 	/**
 	 * Getter du port UDP du serveur
 	 * 
 	 * @return int, port UDP du serveur
 	 */
 	public int getUdpServerPort()
 	{
 		return udpServerPort;
 	}
 
 	/**
 	 * Setter qui fixe le port UDP du serveur
 	 * 
 	 * @param udpServerPort
 	 *            port UDP
 	 */
 	public void setUdpServerPort(int udpServerPort)
 	{
 		this.udpServerPort = udpServerPort;
 	}
 
 	/**
 	 * Getter du port TCP du serveur
 	 * 
 	 * @return int, Port TCP du serveur
 	 */
 	public int getTcpServerPort()
 	{
 		return tcpServerPort;
 	}
 
 	/**
 	 * Setter qui fixe le port UDP du serveur
 	 * 
 	 * @param tcpServerPort
 	 *            Port UDP du serveur
 	 */
 	public void setTcpServerPort(int tcpServerPort)
 	{
 		this.tcpServerPort = tcpServerPort;
 	}
 
 	/**
 	 * Getter du protocole du client
 	 * 
 	 * @return protocol, du client
 	 */
 	public Protocol getProtocol()
 	{
 		return protocol;
 	}
 
 	/**
 	 * Setter qui fixe le protocole
 	 * 
 	 * @param protocol
 	 *            Protocole de communication
 	 */
 	public void setProtocol(Protocol protocol)
 	{
 		this.protocol = protocol;
 	}
 
 	/**
 	 * Getter des messages d'erreurs
 	 * 
 	 * @return the errorsMessages
 	 */
 	public String getErrorsMessages()
 	{
 		return errorsMessages;
 	}
 
 	/**
 	 * Setter qui fixe le message d'erreur
 	 * 
 	 * @param errorsMessages
 	 *            the errorsMessages to set
 	 */
 	public void setErrorsMessages(String errorsMessages)
 	{
 		this.errorsMessages = errorsMessages;
 	}
 
 	/**
 	 * Getter du message perso du client
 	 * 
 	 * @return personalMessage, le message perso du client
 	 */
 	public String getPersonalMessage()
 	{
 		return personalMessage;
 	}
 
 	/**
 	 * Setter qui fixe le message perso du client
 	 * 
 	 * @param personalMessage
 	 *            message perso du client
 	 */
 	public void setPersonalMessage(String personalMessage)
 	{
 		this.personalMessage = personalMessage;
 	}
 
 	/**
 	 * Méthode permettant lancer une instance de client en ligne de commande
 	 * 
 	 * @param args
 	 *            aucun paramètre n'est requis
 	 */
 	@Deprecated
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
 				client.sendMessageToClient(mes, client.dialogs.get(0).getIdDialog());
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
