 package client.interfaces;
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 import java.util.List;
 
 import javax.swing.JFrame;
 
 import server.objects.interfaces.DiscussionSubjectInterface;
 import server.objects.interfaces.MessageInterface;
 import server.objects.interfaces.ServerForumInterface;
 import client.gui.ClientDiscussionFrame;
 import client.gui.ClientMainFrame;
 import client.implementation.ClientImplementation;
 
 
 /*
  * @author Niiner
  * Interface d'objet distant pour les afficheurs clients d'un sujet de discussion
  * objets localis�s sur les sites clients du forum, capt�s sur le site serveur par r�f�rences
  * pass�es en param�tre depuis les sites clients
  */
 /**
  * Remote Objects interface to display the client displayers from the forum server.
  * @author Grumpy Group
  */
 public interface ClientDisplayerInterface extends Remote {
 
 	/**
 	 * Return the client of this interface
 	 * @return {@link ClientInterface} - The client
 	 * @throws RemoteException
 	 */
 	public ClientImplementation getClient() throws RemoteException;
 
 	/**
 	 * Set the client instance for this user
 	 * @param client {@link ClientImplementation} - The client instance
 	 * @throws RemoteException
 	 */
 	public void setClient(ClientImplementation client)
 			throws RemoteException;
 
 	/**
 	 * Display the message on the client's displayer
 	 * @param message {@link String} - The message to display
 	 * @throws RemoteException
 	 */
 	public void display(String message, boolean inFrame) throws RemoteException;
 
 	/**
 	 * Display the message in frame from a parent frame
 	 * @param message {@link String} - The message to display
	 * @param parent {@link JFrame} - The parent frame
 	 * @throws RemoteException
 	 */
 	public void display(String message, JFrame parent) throws RemoteException;
 
 	/**
 	 * Display the error on the client's displayer
 	 * @param message {@link String} - The error to display
 	 * @throws RemoteException
 	 */
 	public void error(String message, boolean inFrame) throws RemoteException;
 	
 	/**
 	 * The client main frame of graphic interface
 	 * @return {@link ClientMainFrame} - The client main frame
 	 * @throws RemoteException
 	 */
 	public ClientMainFrame getMainFrame() throws RemoteException;
 
 	/**
 	 * Receive a message from server
 	 * @param message {@link String} - The message
 	 * @throws RemoteException
 	 */
 	public void getMessage(String message) throws RemoteException;
 	
 	/**
 	 * Receive a message on a specific channel
 	 * @param message {@link MessageInterface} - The message to receive
 	 * @param dsi {@link DiscussionSubjectInterface} - The channel
 	 * @throws RemoteException
 	 */
 	public void getMessage(MessageInterface message, DiscussionSubjectInterface dsi)
 			throws RemoteException;
 
 	/**
 	 * Close the server connection and exit interface
 	 * @throws RemoteException
 	 */
 	public void exit() throws RemoteException;
 
 	/**
 	 * Define the server instance for the controller
 	 * @param server {@link ServerForumInterface} - The server instance
 	 * @throws RemoteException
 	 */
 	public void setServer(ServerForumInterface server) throws RemoteException;
 
 	/**
 	 * Returns the server instance of the controller
 	 * @return server {@link ServerForumInterface} - The server instance
 	 * @throws RemoteException
 	 */
 	public ServerForumInterface getServer() throws RemoteException;
 	
 	/**
 	 * Open a discussion frame in the client side
 	 * @param cdf {@link ClientDiscussionFrame} - The discussion frame to open
 	 * @return {@link Boolean boolean} - <code>true</code> if the discussion has been
 	 * correctly opened, <code>false</code> otherwise
 	 * @throws RemoteException
 	 */
 	public boolean openDiscussion(ClientDiscussionFrame cdf)
 			throws RemoteException;
 	
 	/**
 	 * Returns <code>true</code> if the discussion is opened in a frame
 	 * @param dsi {@link DiscussionSubjectInterface} - The discussion subject to search
 	 * @return {@link Boolean boolean} - <code>true</code> if the discussion is opened
 	 * in a frame
 	 * @throws RemoteException
 	 */
 	public boolean isOpenedDiscussion(DiscussionSubjectInterface dsi)
 			throws RemoteException;
 	
 	/**
 	 * Returns the {@link ClientDiscussionFrame frame} in which the
 	 * {@link DiscussionSubjectInterface discussion} is opened
 	 * @param dsi {@link DiscussionSubjectInterface} - The discussion subject to search
 	 * @return {@link ClientDiscussionFrame} - 
 	 * @throws RemoteException
 	 */
 	public ClientDiscussionFrame getDiscussionFrame(DiscussionSubjectInterface dsi)
 			throws RemoteException;
 	
 	/**
 	 * Add an user in a specific channel
 	 * @param ci {@link ClientInterface} - The client to add
 	 * @param dsi {@link DiscussionSubjectInterface} - The channel in which to add the client
 	 * @throws RemoteException 
 	 */
 	public void newUser(ClientInterface ci,
 			DiscussionSubjectInterface dsi) throws RemoteException;
 
 	/**
 	 * Remove an user in specific channel
 	 * @param ci {@link ClientInterface} - The client to remove
 	 * @param dsi {@link DiscussionSubjectInterface} - The channel in which to remove the client
 	 * @throws RemoteException
 	 */
 	public void leftUser(ClientInterface ci, DiscussionSubjectInterface dsi)
 			throws RemoteException;
 	
 	/**
 	 * Update the channel buttons list from given channels list
 	 * @param list {@link List}<{@link DiscussionSubjectInterface}> - The list of discussions
 	 * @throws RemoteException
 	 */
 	public void updateChannelList(List<DiscussionSubjectInterface> list) throws RemoteException;
 
 	/**
 	 * Close the discussion frame for specific discussion
 	 * @param dsi {@link DiscussionSubjectInterface} - The discussion to close its frame
 	 * @throws RemoteException
 	 */
 	public void closeDiscussionFrame(DiscussionSubjectInterface dsi) throws RemoteException;
 
 	/**
 	 * Dispose the frame for specific discussion
 	 * @param dsi {@link DiscussionSubjectInterface} - The discussion to close its frame
 	 * @throws RemoteException
 	 */
 	public void disposeDiscussionFrame(DiscussionSubjectInterface dsi)
 			throws RemoteException;
 
 	/**
 	 * Disable the discussion frame
 	 * @param dsi {@link DiscussionSubjectInterface} - The discussion to disable
 	 * @throws RemoteException
 	 */
 	public void setInactiveDiscussion(DiscussionSubjectInterface dsi)
 			throws RemoteException;
 	
 	/**
 	 * Close the discussion and remove it from list of opened discussion
 	 * @param dsi {@link DiscussionSubjectInterface} - The discussion to close
 	 * @throws RemoteException 
 	 */
 	public void closeDiscussion(DiscussionSubjectInterface dsi) throws RemoteException;
 
 	/**
 	 * Ask new discussion owner
 	 * @param dsi {@link DiscussionSubjectInterface} - The discussion
 	 * @return {@link Boolean boolean} - <code>true</code> if the client accepted,
 	 * <code>false</code> otherwise
 	 * @throws RemoteException 
 	 */
 	public boolean askNewOwner(DiscussionSubjectInterface dsi) throws RemoteException;
 
 	/**
 	 * Change the owner name of the discussion
 	 * @param cdf {@link DiscussionSubjectInterface} - The discussion where its frame
 	 * must update the owner name
 	 * @throws RemoteException 
 	 */
 	public void changeOwner(DiscussionSubjectInterface cdf) throws RemoteException;
 
 	/**
 	 * Ask on server to all client connected on specific discussion if they want to be the 
 	 * new discussion owner
 	 * @param discussion {@link DiscussionSubjectInterface} - The discussion to ask
 	 * @throws RemoteException 
 	 */
 	public void serverAskNewOwner(DiscussionSubjectInterface discussion)
 			throws RemoteException;
 
 }
