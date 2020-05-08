 package fr.ribesg.alix.api;
 import fr.ribesg.alix.api.message.JoinIrcPacket;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * Represents an IRC Channel.
  *
  * @author Ribesg
  */
 public class Channel extends Receiver {
 
 	/**
 	 * The password of this Channel, if any
 	 */
 	private final String password;
 
 	private String topic;
 
 	private Set<String> users;
 
 	/**
 	 * Channel constructor.
 	 *
 	 * @param server the Server this Channel belongs to
 	 * @param name   the name of the Channel, in the format #foo
 	 */
 	public Channel(final Server server, final String name) {
 		super(server, name);
 		this.password = null;
 	}
 
 	/**
 	 * Password-protected Channel constructor.
 	 *
 	 * @param server   the Server this Channel belongs to
 	 * @param name     the name of the Channel, in the format #foo
 	 * @param password the password of the Channel
 	 */
 	public Channel(final Server server, final String name, final String password) {
 		super(server, name);
 		this.password = password;
 	}
 
 	/**
 	 * @return true if the Channel has a known password, false otherwise
 	 */
 	public boolean hasPassword() {
 		return this.password != null;
 	}
 
 	/**
 	 * @return the password of this Channel, if any
 	 */
 	public String getPassword() {
 		return this.password;
 	}
 
 	/**
 	 * @return the topic of this Channel
 	 */
 	public String getTopic() {
 		return topic;
 	}
 
 	/**
 	 * @param topic the topic of this Channel
 	 */
 	public void setTopic(final String topic) {
 		this.topic = topic;
 	}
 
 	/**
 	 * @return the users of this Channel, op being represented as @user
 	 * and voices as +user
 	 */
 	public Set<String> getUsers() {
 		return users;
 	}
 
 	/**
 	 * @return the users of this Channel, without op or voiced prefix
 	 */
 	public Set<String> getUserNames() {
 		final Set<String> res = new HashSet<>();
 		for (final String userName : this.users) {
 			if (userName.startsWith("@") || userName.startsWith("+")) {
 				res.add(userName.substring(1));
 			} else {
 				res.add(userName);
 			}
 		}
 		return res;
 	}
 
 	/**
 	 * @return the OP users of this Channel, without the @
 	 */
 	public Set<String> getOps() {
 		final Set<String> result = new HashSet<>();
 		for (final String user : this.users) {
 			if (user.startsWith("@")) {
 				result.add(user.substring(1));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @return the Voices users of this Channel, without the @
 	 */
 	public Set<String> getVoiced() {
 		final Set<String> result = new HashSet<>();
 		for (final String user : this.users) {
 			if (user.startsWith("+")) {
 				result.add(user.substring(1));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @param user the user name in any format ('user', '@user', '+user')
 	 *
 	 * @return true if the String '@user' is contained in this Channel's
 	 * user list
 	 */
 	public boolean isOp(final String user) {
 		if (user.startsWith("@") || user.startsWith("+")) {
 			return this.users.contains("@" + user.substring(1));
 		} else {
 			return this.users.contains("@" + user);
 		}
 	}
 
 	/**
 	 * @param user the user name in any format ('user', '@user', '+user')
 	 *
 	 * @return true if the String '+user' is contained in this Channel's
 	 * user list
 	 */
 	public boolean isVoiced(final String user) {
 		if (user.startsWith("@") || user.startsWith("+")) {
 			return this.users.contains("+" + user.substring(1));
 		} else {
 			return this.users.contains("+" + user);
 		}
 	}
 
 	/**
 	 * @param user the user name in any format ('user', '@user', '+user')
 	 *
 	 * @return true if the String '@user' or '+user' is contained in this
 	 * Channel's user list
 	 */
 	public boolean isOpOrVoices(final String user) {
 		if (user.startsWith("@") || user.startsWith("+")) {
 			final String realUser = user.substring(1);
 			return this.users.contains("@" + realUser) || this.users.contains("+" + realUser);
 		} else {
 			return this.users.contains("@" + user) || this.users.contains("+" + user);
 		}
 	}
 
 	/**
 	 * @param users the array of Users found on this Channel
 	 */
	public void addUsers(final String[] users) {
		if (this.users == null) {
			this.users = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		}
 		this.users.addAll(Arrays.asList(users));
 	}
 
 	/**
 	 * Attempt to join this Channel.
 	 *
 	 * @throws IllegalStateException if the Client is not connected
 	 *                               to this Channel's Server
 	 */
 	public void join() {
 		if (getServer().isConnected()) {
 			if (this.hasPassword()) {
 				this.server.send(new JoinIrcPacket(this.getName(), this.getPassword()));
 			} else {
 				this.server.send(new JoinIrcPacket(this.getName()));
 			}
 		} else {
 			throw new IllegalStateException("Not connected!");
 		}
 	}
 }
