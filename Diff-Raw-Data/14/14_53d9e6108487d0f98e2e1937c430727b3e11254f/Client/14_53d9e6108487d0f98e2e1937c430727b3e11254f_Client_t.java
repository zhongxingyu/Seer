 package fr.ribesg.alix.api;
 /**
  * Represents an IRC Client that can connect to IRC servers,
  * join channels on those various IRC servers, send messages,
  * actions and commands to those IRC servers/channels.
  *
  * @author Ribesg
  */
 public interface Client {
 
 	/**
 	 * Connect to a server and join provided channels
 	 *
 	 * @param server   The server to connect to.
 	 * @param channels The channels to join after connection.
 	 *
 	 * @return False if the client is already connected to this server,
 	 *         otherwise true.
 	 */
 	public boolean connect(Server server, Channel... channels);
 
 	/**
 	 * Join a channel on a connected server
 	 *
 	 * @param channel The channel to join.
 	 *
 	 * @return False if the client is not connected to this channel's server
 	 *         or is already in this channel, otherwise true.
 	 */
 	public boolean join(Channel channel);
 
 	/**
 	 * Leave a channel on a connected server
 	 *
 	 * @param channel The channel to leave.
 	 *
 	 * @return False if the client is not connected to this channel's server
 	 *         or is not in this channel, otherwise true.
 	 */
 	public boolean part(Channel channel);
 
 	/**
 	 * Quit a connected server
 	 *
 	 * @param server The server to quit.
 	 *
 	 * @return False if the client was not connected to this server,
 	 *         otherwise true.
 	 */
 	public boolean quit(Server server);
 
 	/**
	 * Send a message to a receiver on a connected server
 	 *
	 * @param receiver The receiver to send the message to.
	 * @param message  The message to send.
 	 *
	 * @return False if not connected to the receiver's server
 	 */
	public boolean sendMessage(Receiver receiver, String message);
 
 }
