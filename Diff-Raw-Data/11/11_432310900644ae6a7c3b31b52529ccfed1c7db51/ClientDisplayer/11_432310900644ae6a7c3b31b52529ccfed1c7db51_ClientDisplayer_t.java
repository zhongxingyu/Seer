 package client.implementation;
 
 import java.io.Serializable;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.UIManager;
 
 import server.objects.interfaces.DiscussionSubjectInterface;
 import server.objects.interfaces.MessageInterface;
 import server.objects.interfaces.ServerForumInterface;
 import client.gui.ClientDiscussionFrame;
 import client.gui.ClientMainFrame;
 import client.interfaces.ClientDisplayerInterface;
 import client.interfaces.ClientInterface;
 
 /**
  * This class is the displayer that defines the clients attributes
  * @author Grumpy Group
  */
 
 public class ClientDisplayer extends UnicastRemoteObject
 		implements ClientDisplayerInterface, Serializable {
 
 	/**
 	 * ID
 	 */
 	private static final long serialVersionUID = -586751614177645291L;
 	/**
 	 * The {@link ClientInterface client} used 
 	 */
 	private ClientImplementation client;
 	private ClientMainFrame mainFrame;
 	private ServerForumInterface server;
 	private List<ClientDiscussionFrame> openedDiscussion=
 			new ArrayList<ClientDiscussionFrame>();
 
 	/**
 	 * Instance constructor
 	 * @throws RemoteException
 	 */
 	public ClientDisplayer() throws RemoteException {
 		super();
 		try {
 			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		this.mainFrame=new ClientMainFrame(this);
 	}
 
 	@Override
 	public ClientImplementation getClient() throws RemoteException {
 		return this.client;
 	}
 	
 	@Override
 	public void setClient(ClientImplementation client) throws RemoteException {
 		this.client = client;
 	}
 
 	@Override
 	public ServerForumInterface getServer() {
 		return this.server;
 	}
 	
 	@Override
 	public void setServer(ServerForumInterface server) {
 		this.server = server;
 	}
 
 	@Override
 	public ClientMainFrame getMainFrame() throws RemoteException {
 		return this.mainFrame;
 	}
 	
 	@Override
 	public void newUser(ClientInterface client,
 			DiscussionSubjectInterface dsi) throws RemoteException {
 		for(ClientDiscussionFrame cdf:this.openedDiscussion) {
 			if(cdf.getDiscussion().equals(dsi)) {
 				cdf.newUser(client);
 				return;
 			}
 		}
 	}
 	
 	@Override
 	public void leftUser(ClientInterface client,
 			DiscussionSubjectInterface dsi) throws RemoteException {
 		for(ClientDiscussionFrame cdf:this.openedDiscussion) {
 			if(cdf.getDiscussion().equals(dsi)) {
 				cdf.leftUser(client);
 				return;
 			}
 		}
 	}
 	
 	@Override
 	public void serverAskNewOwner(DiscussionSubjectInterface dsi)
 			throws RemoteException {
 		this.server.askNewOwner(dsi);
 	}
 	
 	@Override
 	public void changeOwner(DiscussionSubjectInterface dsi)
 			throws RemoteException{
 		ClientDiscussionFrame cdf=this.getDiscussionFrame(dsi);
 		if(cdf==null) {
 			return;
 		}
 		cdf.changeOwner();
 	}
 	
 	@Override
 	public void exit() throws RemoteException {
		if(this.server!=null) {
			this.server.disconnectUser(this);
		}
 		this.display("See you later dude!", true);
 		this.getMainFrame().setVisible(false);
 		for(ClientDiscussionFrame cdf:this.openedDiscussion) {
 			cdf.close(false);
 		}
 		System.exit(0);
 	}
 	
 	@Override
 	public boolean askNewOwner(DiscussionSubjectInterface dsi)
 			throws RemoteException {
 		return this.getDiscussionFrame(dsi).askNewOwner();
 	}
 	
 	@Override
 	public void closeDiscussionFrame(DiscussionSubjectInterface dsi)
 			throws RemoteException {
 		this.getDiscussionFrame(dsi).closeFrame();
 	}
 	
 	@Override
 	public void setInactiveDiscussion(DiscussionSubjectInterface dsi)
 			throws RemoteException {
 		this.getDiscussionFrame(dsi).setInactive();
 	}
 	
 	@Override
 	public void disposeDiscussionFrame(DiscussionSubjectInterface dsi)
 			throws RemoteException {
 		this.getDiscussionFrame(dsi).dispose();
 	}
 	
 	@Override
 	public void closeDiscussion(DiscussionSubjectInterface dsi)
 			throws RemoteException {
 		ClientDiscussionFrame cdf=this.getDiscussionFrame(dsi);
 		this.openedDiscussion.remove(cdf);
 	}
 	
 	@Override
 	public void error(String message, boolean inFrame) throws RemoteException {
 		if(inFrame) {
 			JOptionPane.showMessageDialog(this.mainFrame, "<html>"+
 					message.replaceAll("\n", "<br>")+"</html>",
 					"Error", JOptionPane.ERROR_MESSAGE);
 		}
 		System.err.println("[ERROR]: "+message);
 		this.mainFrame.displayError(message);
 	}
 
 	@Override
 	public void display(String message, boolean inFrame) throws RemoteException {
 		if(inFrame) {
 			this.display(message, this.mainFrame);
 		}
 		else {
 			System.out.println("[LOG]: "+message);
 			this.mainFrame.displayLog(message);
 		}
 	}
 	
 	@Override
 	public void display(String message, JFrame parent) throws RemoteException {
 		JOptionPane.showMessageDialog(parent, "<html>"+
 				message.replaceAll("\n", "<br>")+"</html>", "Information",
 				JOptionPane.INFORMATION_MESSAGE);
 		System.out.println("[LOG]: "+message);
 		this.mainFrame.displayLog(message);
 	}
 
 	@Override
 	public void getMessage(String message) throws RemoteException {
 		this.display("[SERVER]: "+message, false);
 	}
 	
 	@Override
 	public void getMessage(MessageInterface message, DiscussionSubjectInterface dsi)
 			throws RemoteException {
 		ClientDiscussionFrame frame=this.getDiscussionFrame(dsi);
 		if(frame==null) {
 			return;
 		}
 		frame.setVisible(true);
 		frame.displayMessage(message);
 	}
 	
 	@Override
 	public ClientDiscussionFrame getDiscussionFrame(DiscussionSubjectInterface dsi)
 			throws RemoteException {
 		for(ClientDiscussionFrame frame:this.openedDiscussion) {
 			if(frame==null) {
 				continue;
 			}
 			if(frame.getDiscussion().getTitle().equalsIgnoreCase(dsi.getTitle())) {
 				return frame;
 			}
 		}
 		return null;
 	}
 	
 	@Override
 	public boolean isOpenedDiscussion(DiscussionSubjectInterface dsi)
 		throws RemoteException {
 		return this.getDiscussionFrame(dsi)!=null;
 	}
 	
 	@Override
 	public boolean openDiscussion(ClientDiscussionFrame cdf)
 		throws RemoteException {
 		if(this.openedDiscussion.contains(cdf)) {
 			return false;
 		}
 		return this.openedDiscussion.add(cdf);
 	}
 	
 	@Override
 	public void updateChannelList(List<DiscussionSubjectInterface> list) throws RemoteException {
 		this.getMainFrame().updateSubjectPanel(list);
 	}
 	
 	@Override
 	public String toString() {
 		return "ClientDisplayer [client=" + client + "]";
 	}
 
 }
