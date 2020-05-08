 package clientServer;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Scanner;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import dataLink.Protocol;
 import dataLink.ProtocolUDP;
 
 public class Client extends AbstractClientServer
 {
 	private String id;
 	private String name;
 	private HashMap<String, String> clientList;
 	private Vector<ClientDialog> dialogs;
 	private ThreadComunicationClient threadComunicationClient;
 	private int listeningUDPPort;
 	private ThreadListenerUDP threadListenerUDP;
 	private Protocol protocol;
 	private String ipServer;
 
 	public Client(String name, int listeningUDPPort, String ipServer)
 	{
 		super();
 		this.name = name;
 		this.listeningUDPPort = listeningUDPPort;
 		this.protocol = new ProtocolUDP(listeningUDPPort);
 		this.clientList = new HashMap<String, String>();
 		this.dialogs = new Vector<ClientDialog>();
		this.ipServer=ipServer;
 		this.threadComunicationClient = new ThreadComunicationClient(this, ipServer);
 		this.threadListenerUDP = new ThreadListenerUDP(this, this.protocol);
 		this.threadListenerUDP.start();
 	}
 
 	public void registerToServer()
 	{
 		try
 		{
 			this.launchThread();
 			Thread.sleep(500);
 			threadComunicationClient.registerClient(new StringTokenizer(""));
 		} catch (InterruptedException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public void unregisterToServer()
 	{
 		try
 		{
 			this.launchThread();
 			Thread.sleep(500);
 			this.threadComunicationClient.unregisterClient(new StringTokenizer(""));
 		} catch (InterruptedException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public void askListToServer()
 	{
 		try
 		{
 			this.launchThread();
 			Thread.sleep(500);
 			this.threadComunicationClient.askListClient(new StringTokenizer(""));
 		} catch (InterruptedException e)
 		{
 			e.printStackTrace();
 		}
 
 	}
 
 	public void askClientConnectionToServer(String clientId)
 	{
 		try
 		{
 			this.launchThread();
 			Thread.sleep(500);
 			this.threadComunicationClient.getClientConnection(clientId);
 			int cpt = 0;
 			int sizeClients = this.getClients().size();
 			System.out.println("taille : " + sizeClients);
 			while (cpt < 50 && this.getClients().size() == sizeClients)
 			{
 				Thread.sleep(1000);
 			}
 			System.out.println("taille1 : " + sizeClients);
 			this.startDialogToClient(this.getClients().lastElement());
 		} catch (InterruptedException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public void startDialogToClient(ClientServerData client)
 	{
 		try
 		{
 			System.out.println("Début de dialogue");
 			ClientDialog dialog = new ClientDialog(this.protocol);
 			dialog.addClient(client);
 			String idDialog = dialog.getIdDialog();
 			protocol.sendMessage("dialog:newDialog:" + idDialog, client.getIp(), client.getPort());
 			protocol.sendMessage("dialog:newDialog:clients:" + idDialog + ":" + this.id, client.getIp(), client.getPort());
 			this.dialogs.add(dialog);
 		} catch (NumberFormatException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
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
 
 	public void addClientList(String list)
 	{
 		StringTokenizer token = new StringTokenizer(list, ",");
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
 
 	@Override
 	public void treatIncomeTCP(Object object)
 	{
 		// TODO Auto-generated method stub
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
 
 		default:
 			break;
 		}
 
 	}
 
 	public void treatIncomeDialog(StringTokenizer token)
 	{
 		if (token.hasMoreTokens())
 		{
 			String nextToken = token.nextToken();
 			switch (nextToken)
 			{
 			case "newDialog":
 				// Dans le cas d'une demande de dialogue d'un autre client
 				if (token.hasMoreTokens())
 				{
 					// On récupère l'id de conversation
 					String idDialog = token.nextToken();
 					// SI c'est bien un id de conversation, alors on crée la
 					// conversation
 					if (idDialog.length() > 20)
 					{
 						// On crée le dialog
 						this.dialogs.add(new ClientDialog(idDialog, this.protocol));
 					}
 
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
 									String[] clients = new String[1];
 									clients[0] = clientsT;
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
 				/*
 				 * // Dans le cas d'une génération de dialog de notre part
 				 * 
 				 * else { ClientDialog dialog = new
 				 * ClientDialog(this.listeningUDPPort);
 				 * this.dialogs.add(dialog);
 				 * dialog.sendMessage("dialog:newDialog:" +
 				 * dialog.getIdDialog()); } break;
 				 */
 				break;
 			case "message":
 				if (token.hasMoreTokens())
 				{
 					String idDialog = token.nextToken();
 					// SI c'est bien un id de conversation, alors on crée la
 					// conversation
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
 		/*
 		 * client.registerToServer(); while (client.id == null) { try {
 		 * Thread.sleep(5000); } catch (InterruptedException e) { // TODO
 		 * Auto-generated catch block e.printStackTrace(); } }
 		 * System.out.println("Demande de liste"); client.askListToServer(); try
 		 * { Thread.sleep(5000); } catch (InterruptedException e) { // TODO
 		 * Auto-generated catch block e.printStackTrace(); }
 		 * System.out.println("Demande de deco"); client.unregisterToServer();
 		 */
 	}
 }
