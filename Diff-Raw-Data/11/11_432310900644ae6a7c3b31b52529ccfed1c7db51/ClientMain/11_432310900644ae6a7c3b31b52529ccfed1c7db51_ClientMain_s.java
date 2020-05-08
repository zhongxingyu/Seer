 package client.main;
 
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.UnknownHostException;
 import java.rmi.ConnectException;
 import java.rmi.Naming;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 
 import javax.swing.JOptionPane;
 
 import server.objects.interfaces.ServerForumInterface;
 import client.gui.GetStringDialog;
 import client.implementation.ClientDisplayer;
 import client.implementation.ClientImplementation;
 import client.interfaces.ClientDisplayerInterface;
 
 /**
  * This is the client launcher
  * @author Grumpy Group
  */
 public class ClientMain {
 
 	/**
 	 * Connection tries
 	 */
 	private int tries=0;
 
 	/**
 	 * The client starting method
 	 * @param cd {@link ClientDisplayer} - The client displayer
 	 * @throws RemoteException
 	 */
	@SuppressWarnings("resource")
 	public void start(ClientDisplayerInterface cd) throws RemoteException {
 
 		if(cd==null) {
 			System.err.println("Client displayer could not be instantiated");
 			System.exit(0);
 		}
 		cd.display("Client starting...", false);
 		
 		int port=1100;
 		String url = null;
 		/*
 		 * Fait automatiquement
 		 */
 		//			if (System.getSecurityManager() == null) {
 		//				System.setSecurityManager(new RMISecurityManager());
 		//			}
 		try {
 			/*
 			 * On n'utilise pas rmi:
 			 */
 			url="//"+InetAddress.getLocalHost().getHostName()+":"+port+"/GrumpyChat";
 			ServerForumInterface server = (ServerForumInterface) Naming.lookup(url);
 			cd.setServer(server);
 
 			this.tries=0;
 
 			String command="";
 			do {
 				command=new GetStringDialog(cd.getMainFrame(), "Please enter your pseudo",
 						"Type your pseudo and validate","Pseudo",true).getValue();
 				if(command==null) {
 					cd.exit();
 				}
 				else if(command.length()<3) {
 					cd.error("Your pseudo must have minimum 3 characters", true);
 					command="";
 				}
 				else if(command.length()>25) {
 					cd.error("Your pseudo must have maximum 25 characters", true);
 					command="";
 				}
 				else if(server.containsPseudo(command)||command.equalsIgnoreCase("server")) {
 					cd.error("Pseudo '"+command+"' already used, please try again", true);
 					command="";
 				}
 			} while(command.isEmpty());
 			cd.setClient(new ClientImplementation(command));
 			server.newUser(cd);
 			cd.display("Welcome "+command, true);
 			cd.getMainFrame().setTitle("Client: "+command);
 			/*
 			 * Define the subjects discussions list in the client frame 
 			 */
 			cd.getMainFrame().updateSubjectPanel(server.getDiscussions());
 
 //			String help="Available commands:\n" +
 //					"\t/help:\t\t\tDisplay commands\n" +
 //					"\t/create <channel>:\tCreate the channel <channel> on the server\n" +
 //					"\t/exit:\t\t\tLeave client\n" +
 //					"\t/list:\t\t\tDisplay channels list\n" +
 //					"\t/messages:\t\tDisplay the current discussion messages\n" +
 //					"\t/say <message>:\tAdd the message <message> from you on your current channel\n" +
 //					"\t/subscribe <channel>:\tSuscribe to channel <channel>\n" +
 //					"\t/switch <channel>:\tSwitch to channel <channel>";
 
 //			cd.display(help, true);
 
 //			DiscussionSubjectInterface currentDiscussion=null;
 //			Scanner sc=new Scanner(System.in);
 //			do {
 //				command=sc.nextLine();
 //				if(command.toLowerCase().startsWith("/help")) {
 //					cd.display(help, true);
 //				}
 //				else if(command.toLowerCase().startsWith("/create")) {
 //					if(server.isFullChannel(cd)) {
 //						cd.error("You can not create more channel, delete one before", true);
 //						continue;
 //					}
 //					int len="/create".length();
 //					if(command.length()<="/create".length()+1) {
 //						cd.error("Please specify a message", true);
 //						continue;
 //					}
 //					String subject=command.substring(len+1,command.length());
 //					if(subject.endsWith(" ")) {
 //						subject=subject.substring(0,subject.lastIndexOf(" "));
 //					}
 //					if(server.getSubjectFromName(subject)!=null) {
 //						cd.error("The channel '"+subject+"' already exists", true);
 //						continue;
 //					}
 //					DiscussionSubjectInterface dsi = server.create(cd,subject);
 //					if(dsi!=null) {
 //						currentDiscussion=dsi;
 //						cd.display("The channel '"+subject+"' has been correctly " +
 //								"created", true);
 //					}
 //					else {
 //						cd.error("The channel '"+subject+"' can not be created",
 //								true);
 //					}
 //				}
 //				else if(command.toLowerCase().startsWith("/list")) {
 //					if(server.getDiscussions().isEmpty()) {
 //						cd.error("There is no discussion on the forum", true);
 //					}
 //					String list="Discussions list:\n";
 //					for(DiscussionSubjectInterface dsi:server.getDiscussions()) {
 //						list+="\t\t"+dsi.getTitle()+(dsi.isConnected(cd.getClient())?
 //								" *":"")+"\n";
 //					}
 //					cd.display(list, false);
 //					cd.getMainFrame().updateSubjectPanel(server.getDiscussions());
 //				}
 //				else if(command.toLowerCase().startsWith("/messages")) {
 //					if(currentDiscussion==null) {
 //						cd.error("You don't have current discussion", true);
 //					}
 //					else {
 //						List<MessageInterface> messages=currentDiscussion.getMessages();
 //						for(MessageInterface msg:messages) {
 //							cd.display(msg.getDateString()+msg.getClient().getPseudo()+
 //									": "+msg.getMessage(), false);
 //						}
 //					}
 //				}
 //				else if(command.toLowerCase().startsWith("/remove")) {
 //					int len="/remove".length();
 //					if(command.length()<="/remove".length()+1) {
 //						cd.error("Please specify a message", true);
 //						continue;
 //					}
 //					String subject=command.substring(len+1,command.length());
 //					if(subject.endsWith(" ")) {
 //						subject=subject.substring(0,subject.lastIndexOf(" "));
 //					}
 //					if(!server.isChannelOwner(cd, subject)) {
 //						cd.error("You are not the channel owner", true);
 //						continue;
 //					}
 //					DiscussionSubjectInterface dsi=server.remove(cd, subject);
 //					if(dsi!=null) {
 //						if(currentDiscussion.equals(dsi)) {
 //							currentDiscussion=dsi;
 //						}
 //						cd.display("The channel '"+subject+"' has been correctly " +
 //								"removed", true);
 //					}
 //					else {
 //						cd.error("The channel '"+subject+"' can not be remove", true);
 //					}
 //				}
 //				else if(command.toLowerCase().startsWith("/say")) {
 //					int len="/say".length();
 //					if(command.length()<="/say".length()+1) {
 //						cd.error("Please specify a message", true);
 //						continue;
 //					}
 //					String message=command.substring(len+1,command.length());
 //					if(message.endsWith(" ")) {
 //						message=message.substring(0,message.lastIndexOf(" "));
 //					}
 //					if(currentDiscussion==null) {
 //						cd.error("You don't have current discussion", true);
 //					}
 //					else {
 //						currentDiscussion.addMessage(
 //								new Message(cd.getClient(), message));
 //					}
 //
 //				}
 //				else if(command.toLowerCase().startsWith("/subscribe")) {
 //					if(command.length()<="/subscribe".length()+1) {
 //						cd.error("Please specify the channel name", true);
 //						continue;
 //					}
 //					int len="/subscribe".length();
 //					String subject=command.substring(len+1,command.length());
 //					if(subject.endsWith(" ")) {
 //						subject=subject.substring(0,subject.lastIndexOf(" "));
 //					}
 //					cd.display("Subscribe to: "+subject, false);
 //					DiscussionSubjectInterface dsi=server.getSubjectFromName(subject);
 //					if(dsi!=null) {
 //						if(server.subscribe(dsi,cd)) {
 //							cd.display("You are now connected to '"+dsi.getTitle()+
 //									"' channel", true);
 //							currentDiscussion=dsi;
 //						}
 //						else {
 //							cd.error("You failed to connect to '"+dsi.getTitle()+
 //									"' channel", true);
 //						}
 //					}
 //					else {
 //						cd.error("The channel '"+subject+"' has not been found", true);
 //					}
 //				}
 //				else if(command.toLowerCase().startsWith("/switch")) {
 //					if(command.length()<="/switch".length()+1) {
 //						cd.error("Please specify the channel name", true);
 //						continue;
 //					}
 //					int len="/switch".length();
 //					String subject=command.substring(len+1,command.length());
 //					if(subject.endsWith(" ")) {
 //						subject=subject.substring(0,subject.lastIndexOf(" "));
 //					}
 //					cd.display("Switch to: "+subject, false);
 //					DiscussionSubjectInterface dsi=server.getSubjectFromName(subject);
 //					if(dsi!=null) {
 //						if(dsi.isConnected(cd.getClient())) {
 //							cd.display("You switched to '"+dsi.getTitle()+
 //									"' channel", false);
 //							currentDiscussion=dsi;
 //						}
 //						else {
 //							cd.error("You did not subscribe to '"+dsi.getTitle()+
 //									"' channel", true);
 //						}
 //					}
 //					else {
 //						cd.error("The channel '"+subject+"' has not been found",
 //								true);
 //						command="";
 //					}
 //				}
 //			} while(!command.equalsIgnoreCase("/exit"));
 //			cd.exit();
 		} catch (ConnectException e) {
 			if(this.tries<5) {
 				int wait=this.tries*5;
 				wait=wait==0?1:wait;
 				cd.error("Connection error, next try in "+(wait)+" seconds",
 						false);
 				try {
 					Thread.sleep(wait*1000);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 				}
 				this.tries++;
 				this.start(cd);
 			}
 			else {
 				cd.error("Can not join the server", true);
 				System.exit(0);
 			}
 		} catch (MalformedURLException e) {
 			cd.error("The server URL seems to be malformed", true);
 			e.printStackTrace();
 			System.exit(0);
 		} catch (NotBoundException e) {
 			cd.error("The server bound does not work", true);
 			e.printStackTrace();
 			System.exit(0);
 		} catch (UnknownHostException e) {
 			cd.error("The server host seems not exist", true);
 			e.printStackTrace();
 			System.exit(0);
 		}
 	}
 
 	/**
 	 * Main class
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			new ClientMain().start(new ClientDisplayer());
 		} catch (RemoteException e) {
 			System.err.println("Error while loading client");
 			JOptionPane.showMessageDialog(null, "The client could not be started", "Client error",
 					JOptionPane.ERROR_MESSAGE);
 			e.printStackTrace();
 			System.exit(0);
 		} catch (Exception e) {
 			System.err.println("Caught error: ");
 			e.printStackTrace();
 			System.exit(0);
 		}
 	}
 
 }
