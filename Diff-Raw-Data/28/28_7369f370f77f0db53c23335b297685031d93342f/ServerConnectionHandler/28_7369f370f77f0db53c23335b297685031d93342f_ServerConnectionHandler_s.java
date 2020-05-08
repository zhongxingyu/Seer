 package network;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.AddressException;
 
 import database.SessionTokenManager;
 
 import logic.Debt;
 import logic.DebtStatus;
 import logic.User;
 import mail.EmailUtils;
 import mail.MailSender;
 import requests.CreateUserRequest;
 import requests.CreateUserRequestStatus;
 import requests.FriendRequest;
 import requests.LogInRequest;
 import requests.LogInRequestStatus;
 import requests.Request;
 import requests.UpdateRequest;
 import requests.FriendRequest.FriendRequestStatus;
 import requests.xml.XMLSerializable;
 import utils.PasswordHasher;
 
 public class ServerConnectionHandler extends Thread {
 
 	private Socket connection;
 	private ServerConnection serverConnection;
 	private BufferedReader reader;
 	private PrintWriter writer;
 	private User user;
 	private boolean running;
 	// Updates are no longer a part of the handler, since the change of the session system as of 16th of June 2013.
 //	private UpdateRequest update;
 	private long timeOfLastCommand = 0;
 	private String token;
 	
 //	public static void main(String[] args) {
 //		ServerConnection server = new ServerConnection(true);
 //		ServerConnectionHandler h = new ServerConnectionHandler(new Socket(), server);
 //		LogInRequest r = new LogInRequest("Stian", "pwd");
 //		r.setSessionToken(Constants.SESSION_TOKEN_REQUEST);
 //		h.processLoginRequest(r);
 //		h.update.add(new Debt(13337, 33, "hardcoded things", server.getUser("Stian"), server.getUser("Arne"), "TEST", server.getUser("Arne"), DebtStatus.REQUESTED));
 //		System.out.println("Number of updates: " + h.update.size());
 //		ServerConnectionHandler h2 = new ServerConnectionHandler(new Socket(), server);
 ////		h2.processToken(h.token);
 //		h2.
 //		System.out.println("Number of updates: " + h2.update.size());
 //	}
 	
 	public ServerConnectionHandler(Socket connection, ServerConnection serverConnection) {
 		this.connection = connection;
 //		update = new UpdateRequest();
 		this.serverConnection = serverConnection;
 		serverConnection.addConnectionHandler(this);
 		try {
 			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 			writer = new PrintWriter(connection.getOutputStream(), true);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			serverConnection.writeToLog("Failed while initializing handler: " + e.toString());
 		}
 	}
 	
 	/**
 	 * Updates the time of last command to the current time
 	 */
 	public void updateTimeOfLastCommand() {
 		timeOfLastCommand = System.currentTimeMillis();
 	}
 	
 	/**
 	 * @return Time of the last command (in millisec)
 	 */
 	public long getTimeOfLastCommand() {
 		return timeOfLastCommand;
 	}
 	
 	/**
 	 * Adds the given object to this user's send queue
 	 * @param o	The object to send
 	 */
 //	public void sendUpdate(XMLSerializable o) {
 //		update.add(o);
 //	}
 //	
 //	public UpdateRequest getUpdate() {
 //		return update;
 //	}
 	
 	public void processUpdate() {
 		// Retrieve the update
 		UpdateRequest update = serverConnection.getTokenManager().getUpdate(this.token);
 		System.out.println("Fetching update for token: " + this.token);
 		if(update != null) { 
 			// Send the update
 			send(update.toXML());
 			System.out.println("Number of updates sent: " + update.size());
 		} else {
 			// Send an empty one if none exists
 			// TODO: This is a sign that the client is not logged in.. Make the client aware of this?
 			send(new UpdateRequest().toXML());
 			System.out.println("Sending blank update");
 		}
 		// Clear the update
 		update.clear();
 	}
 	
 	/**
 	 * @return	This ServerConnectionHandler's user
 	 */
 	public User getUser() {
 		return user;
 	}
 	
 	/**
 	 * This method will try to receive messages from the connected client, and will pass the messages to the appropriate process method.
 	 * Will run until stopped.
 	 */
 	@Override
 	public void run() {
 		running = true;
 		System.out.println("ServerConnectionHandler running!");
 		String xml;
 		while(running && (xml = receive()) != null) {
 			// Register the time of the command
 			updateTimeOfLastCommand();
 			System.out.println("Received XML: " + xml);
 			try {
 				XMLSerializable o = XMLSerializable.toObject(xml);
 				System.out.println("Done parsing object!");
 				// Check version
 				if(!checkVersion(o)) {
 					// Incompatible version, don't process the request
 					// (but send back a response
 					send(o.toXML());
 					die(true);
 					return;
 				}
 				// Process token if any is attached
 				if(!processToken(o.getSessionToken())) {
 					// Session token has expired.. User has to log in again
 					die(true);
 					return;
 				}
 //				if(!processToken(o.getSessionToken())) { 
 //					System.out.println("Is this happening..?");
 //					die(false);
 //					System.out.println("Should not happen..");
 //					return;
 //				}
 				// Receive LogInRequest
 				if(o instanceof LogInRequest) {
 					System.out.println("Received login request!");
 					processLoginRequest((LogInRequest) o);
 				} else if(o instanceof CreateUserRequest) {
 					System.out.println("Received create user request!");
 					processCreateUserRequest((CreateUserRequest) o);
 				} else {
 					// Check that the connected user is logged in before processing any of these requests..
 					// .. or if username and password is attached
 					if(this.user == null || !this.user.isOnline()) {
 						// Check if the supplied username and password is OK
 						System.out.println("Checking if username and password is attached..");
 						if(o.getUserInformationName() != null && o.getUserInformationPass() != null && serverConnection.checkPassword(serverConnection.getUser(o.getUserInformationName().toLowerCase()), o.getUserInformationPass())) {
 							System.out.println("Username and password attached was OK! User is " + o.getUserInformationName());
 							// Set the user as logged in
 							this.user = serverConnection.getUser(o.getUserInformationName().toLowerCase());
 							this.user.setIsOnline(true);
 						} else {
 							System.out.println("User not logged in. Not processing message!");
 							// TODO: Send error message to user?
 							continue;
 						}
 					}
 					if(o instanceof Debt) {
 						System.out.println("Received debt..");
 						processDebt((Debt) o);
 					} else if(o instanceof FriendRequest) {
 						System.out.println("Received friend request..");
 						processFriendRequest((FriendRequest) o);
 					} else if(o instanceof UpdateRequest) {
 						System.out.println("Received update request..");
 						processUpdate();
 					} else {
 						System.out.println("Received something unknown!");
 						serverConnection.writeToLog("Received something unknown: " + xml);
 						// TODO
 					}
 				}
 			} catch(Exception e) {
 				// TODO
 				System.out.println("Exception: " + e);
 				e.printStackTrace();
 				serverConnection.writeToLog("Failed to parse/process XML: " + e.toString());
 			}
 		}
 		// Only kill thread if it is not an android connection.
 		// This is because the CLientConnectionHandler will be hijacked when the
 		// User polls for updates.
 		if(token == null) {
 			System.out.println("Connection handler is done..");
 			die(true);
 		} else {
 			System.out.println("Keeping ClientConnectionHandler alive, since it is for an Android connection.");
 			// TODO: Killing handler even if it is an Android connection, for now
 			// TODO: Should the user be logged off?
 			die(true);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param o
 	 * @return		True if the version was compatible, false if not
 	 */
 	public boolean checkVersion(XMLSerializable o) {
 		if(o instanceof Request) {
 			Request r = (Request) o;
 			// Check compatibility
 			if(!r.getServerVersion().isCompatible(Constants.SERVER_VERSION)) {
 				// Notify user
 				if(r instanceof LogInRequest) {
 					System.out.println("INCOMPATIBLE VERSION! " + r.getServerVersion());
 					((LogInRequest) r).setStatus(LogInRequestStatus.INCOMPATIBLE_CLIENT_VERSION);
 				} else if (r instanceof CreateUserRequest) {
 					((CreateUserRequest) r).setStatus(CreateUserRequestStatus.INCOMPATIBLE_CLIENT_VERSION);
 					System.out.println("INCOMPATIBLE VERSION! " + r.getServerVersion());
 				} else {
 					serverConnection.writeToLog("Received request that was not login/register from client with incompatible version. Version was: " + r.getServerVersion() + ", current version is: " + Constants.SERVER_VERSION);
 				}
 				return false;
 			}
 			System.out.println("Version ok! " + r.getServerVersion());
 		}
 		return true;
 	}
 	
 	/**
 	 * 
 	 * @param token
 	 * @return		False if the token has expired, true if not
 	 */
 	private boolean processToken(String token) {
 		if(token != null) {
 			if(token.equals(Constants.SESSION_TOKEN_REQUEST)) {
 				System.out.println("SESSION TOKEN REQUEST DETECTED!");
 				return true;
 			}
 			System.out.println("Token detected! " + token);
 			// Check if the token has expired
 			if (serverConnection.getTokenManager().getUsername(token) == null) {
 				// Notify the device that its session has expired
 				System.out.println("User has timed out!");
 				send(Constants.SESSION_EXPIRED);
 				return false;
 			} else {
 				this.token = token;
 				// Set the user
 				this.user = serverConnection.getUser(serverConnection.getTokenManager().getUsername(token));
 				return true;
 			}
 		}
 		// FIXME: If no token is attached.. What should we do?? (ignore lacking token for now and process message)
 		return true;
 	}
 	
 	/**
 	 * 
 	 * @param token
 	 * @return		Should continue
 	 */
 /*	private boolean processToken(String token) {
 		if(token != null) {
 			if(token.equals(Constants.SESSION_TOKEN_REQUEST))
 				return true;
 			System.out.println("Token detected! " + token);
 			// Check if this is our connection
 			ServerConnectionHandler handler = serverConnection.getTokenManager().getHandler(token);
 			if(handler == this) {
 				// Keep going and process the object
 				System.out.println("Token matched to handler! This should probably never happen..");
 			} else {
 				// If not, check if it has a handler
 				if(handler != null) {
 					// We hijack this user
 					// FIXME!!!! UNTESTED!
 					this.update = handler.getUpdate();
 					System.out.println("Number of updates hijacked: " + update.size());
 					this.user = handler.getUser();
 					this.user.setIsOnline(true);
 					this.token = token;
 					System.out.println("Hijacking user: " + (this.user != null ? this.user.getUsername() : "null")+ "!");
 					serverConnection.getTokenManager().remove(token);
 					serverConnection.getTokenManager().registerToken(token, this);
 					System.out.println("Killing old handler.");
 					handler.die(false);
 					return true;
 				} else {
 					// If it has no handler, we take it
 					// Make sure it gets all necessary updates by just throwing in all relevant objects
 					// FIXME!!!!! UNTESTED!
 					// FIXME!! Should we really just take it, if it has no handler?
 					System.out.println("User needs a new handler.");
 					// Add all debts
 					// Set the user
 					String username = serverConnection.getTokenManager().getUsername(token);
 					if(username == null) {
 						serverConnection.writeToLog("Something wrong happened while taking over a session! Username was null!");
 						return false;
 					}
 					user = serverConnection.getUser(username);
 					// And process the received object as normal
 					for (int i = 0; i < getUser().getNumberOfConfirmedDebts(); i++) {
 						update.add(getUser().getConfirmedDebt(i));
 					}
 					for (int i = 0; i < getUser().getNumberOfPendingDebts(); i++) {
 						update.add(getUser().getPendingDebt(i));
 					}
 					// Add all friend requests
 					for (int i = 0; i < getUser().getNumberOfFriendRequests(); i++) {
 						update.add(getUser().getFriendRequest(i));
 					}
 					return true;
 				}
 			}
 		} // If not, we just handle it as normal
 		return true;
 	}*/
 	
 	private void die(boolean logOffUser) {
 		System.out.println("Killing thread.");
 		// Check if user was online
 		if(logOffUser) { 
 			if(this.getUser() != null) {
 				// Then set it offline
 				this.getUser().setIsOnline(false);
 			}
 		}
 		running = false;
 		// Remove ourself from the handler list
 		serverConnection.removeConnectionHandler(this);
 	}
 	
 	/**
 	 * Process the given FriendRequest.
 	 * Will reply with a request with the following status:
 	 * 	- This is a requests to create a friend request:
 	 * 		o USER_NOT_FOUND if the requesting friend was not found in the server's database
 	 * 		o PENDING if the request has been sent to the target user (friend)
 	 * 		o UNHANDLED if the request was not valid
 	 * 	- This is a response to a friend request:
 	 * 		o A copy of the request received
 	 * @param request	The FriendRequest
 	 */
 	public void processFriendRequest(FriendRequest request) {
 		// Validate
 		boolean valid = true;
 		// TODO: This should be unnecessary (should be able to use this.getUser())
 		String tokenUser = serverConnection.getTokenManager().getUsername(request.getSessionToken());
 //		User thisUser = serverConnection.getUser(this.getUser().getUsername());
 		User thisUser = serverConnection.getUser(tokenUser);
 		// Check that this is the requesting user's handler if this is a new request
 //		if(request.getStatus() == FriendRequestStatus.UNHANDLED && !request.getFromUser().equals(this.getUser())) valid = false;
 		// Check that this is the target user's handler if this is a response
 //		else if((request.getStatus() == FriendRequestStatus.ACCEPTED || request.getStatus() == FriendRequestStatus.DECLINED) && !request.getFriendUsername().equals(this.getUser().getUsername())) {
 //			valid = false;
 //		}
 		// No, handlers are no longer associated with users.. Check token instead (!!! TODO: This means that console version/swing version must attatch a token!!!!)
 		// Check requesting
 		if(request.getStatus() == FriendRequestStatus.UNHANDLED && !tokenUser.equalsIgnoreCase(request.getFromUser().getUsername())) {
 			valid = false;
 		} 
 		// Check that this is the target user, if this is a response
 		else if ((request.getStatus() == FriendRequestStatus.ACCEPTED || request.getStatus() == FriendRequestStatus.DECLINED) && !tokenUser.equalsIgnoreCase(request.getFriendUsername())) {
 			valid = false;
 		}
 		// Check if the status is invalid
 		else if(request.getStatus() == FriendRequestStatus.PENDING || request.getStatus() == FriendRequestStatus.USER_NOT_FOUND)
 			valid = false;
 		// Check that the FriendRequest's target exists
 		if(serverConnection.getUser(request.getFriendUsername()) == null) {
 			valid = false;
 			request.setStatus(FriendRequestStatus.USER_NOT_FOUND);
 		}
 		//CHECKING IF THE FRIEND REQUEST IS AN ALREADY ACCEPTED FRIEND
 		for(int i=0; i<thisUser.getNumberOfFriends(); i++){
 			if(request.getFriendUsername().equals(thisUser.getFriend(i).getUsername())){
 				valid=false;
 				System.out.println("CAPS");
 			}
 		}
 //		if(request.getStatus() == FriendRequestStatus.UNHANDLED && !request.getFromUser().equals(this.getUser())) {
 //			System.out.println("Here aswell??");
 //			valid = false;
 //		}
 		// Check that the sending user is allowed to set the given status
 //		if((request.getStatus() == FriendRequestStatus.ACCEPTED || request.getStatus() == FriendRequestStatus.DECLINED) && !request.getFriendUsername().equals(this.getUser().getUsername())) {
 //			valid = false;
 //			System.out.println("SUG");
 //		}
 		// If this is a response to a friend request, check that this has a corresponding friend request
 		if((request.getStatus() == FriendRequestStatus.ACCEPTED || request.getStatus() == FriendRequestStatus.DECLINED) && !thisUser.hasFriendRequest(request)) {
 //		if((request.getStatus() == FriendRequestStatus.ACCEPTED || request.getStatus() == FriendRequestStatus.DECLINED) && !serverConnection.getUser(tokenUser).hasFriendRequest(request)) {
 			valid = false;
 			System.out.println("LAST");
 		}
 		User otherUser = serverConnection.getUser((request.getFromUser().equals(this.getUser()) ? request.getFriendUsername() : request.getFromUser().getUsername()));
 		// Don't let a user send friend requests to the same user twice.
 		// FIXME: Does not work
 //		System.out.println("Checking for already existing requests between these two");
 //		if(request.getStatus() == FriendRequestStatus.PENDING) {
 //			for (int i = 0; i < serverConnection.getUser(this.getUser().getUsername()).getNumberOfFriendRequests(); i++) {
 //				System.out.println("Checking " + i);
 //				if(serverConnection.getUser(this.getUser().getUsername()).getFriendRequest(i).getFromUser().equals(otherUser) || serverConnection.getUser(this.getUser().getUsername()).getFriendRequest(i).getFriendUsername().equalsIgnoreCase(otherUser.getUsername())) {
 //					valid = false;
 //					System.out.println("These users already have requests between them!");
 //					break;
 //				}
 //			}
 //		}
 		// If this is a new friend request, check that these two users don't already have any requests for each other, or that the user is sending a request to himself
 		if(request.getStatus() == FriendRequestStatus.UNHANDLED) {
 			if(thisUser.hasFriendRequest(request) || otherUser.hasFriendRequest(request) ||
 					// Check the opposite way aswell
 					thisUser.hasOppositeFriendRequest(request) || otherUser.hasOppositeFriendRequest(request)) {
 				valid = false;
 				request.setStatus(FriendRequestStatus.ALREADY_EXISTS);
 			}
 			if(request.getFriendUsername().equals(thisUser.getUsername())) {
 				valid = false;
 				System.out.println("Here?");
 			}
 		}
 		if(valid) {
 			System.out.println("FriendRequest was valid.");
 			// If this is a new friend request..
 			if(request.getStatus() == FriendRequestStatus.UNHANDLED) {
 				System.out.println("This was a new friend request.");
 				// Give it an id
 				request.setId(serverConnection.getNextFriendRequestId());
 				// Set the correct status
 				request.setStatus(FriendRequestStatus.PENDING);
 				// Add it to the target friend
 				serverConnection.getUser(request.getFriendUsername()).addFriendRequest(request);
 				// And to the current user
 				serverConnection.getUser(request.getFromUser().getUsername()).addFriendRequest(request);
 			} else {
 				System.out.println("This was a reply to a friend request.");
 				// If this is a accepted/declined friend request, update the requesting user's friends (if accepted, if not accepted we do nothig except to remove the request)
 				if(request.getStatus() == FriendRequestStatus.ACCEPTED) {
 					// Add friends
 					otherUser.addFriend(thisUser);
 					thisUser.addFriend(otherUser);
 				} 
 				// Remove friend request.. NO!
 //				thisUser.removeFriendRequest(request);
 				// Copy status
 				thisUser.getFriendRequestFrom(otherUser.getUsername()).setStatus(request.getStatus());
 			}
 			// Notify other user
 			serverConnection.notifyUser(otherUser.getUsername(), request, tokenUser);
 		} else {
 			System.out.println("FriendRequest was not valid.");
 			serverConnection.writeToLog("Received invalid friend request from: " + thisUser.getUsername() + ": " + request.toXML());
 			// Send some garbage that will trigger an error
 			// TODO Set a crap status?? But don't overwrite already set error status!
 			if(request.getStatus() == FriendRequestStatus.ACCEPTED) {
 				// TODO: IS this ok?
 				request.setStatus(FriendRequestStatus.UNHANDLED);
 			}
 		}
 		System.out.println("Sending with status: " + request.getStatus());
 		send(request.toXML());
 	}
 	
 	/**
 	 * Process the given CreateUserRequest
 	 * @param req	The CreateUserRequest
 	 */
 	public void processCreateUserRequest(CreateUserRequest req) {
 		User user = req.getRequestedUser();
 		// Check that the user don't already exist
 		if(serverConnection.getUser(req.getUsername()) != null) {
 			req.setStatus(CreateUserRequestStatus.USERNAME_ALREADY_TAKEN);
 		} 
 		// And that the user name does not exceed 30 characters
 		// TODO: Add check on username
 		else if(req.getUsername().length() > 30) {
 			System.out.println("User name " + req.getUsername() + " is over 30 characters long, and not valid.");
 			req.setStatus(CreateUserRequestStatus.INVALID_USERNAME);
 		}
 		else {
 			// CreateUserRequests are now never activated, no matter where they come from!!
 			// (Only set as activated for compatibility reasons. Should be set as unactivated when created!)
 			// (Check the request's version to see if email and activation should be processed)
 //			if(req.getVersion() == null) {
 //				System.out.println("Setting newly created user as activated for compatibility reasons.");
 //				// Insert dummy activation key and mail, and set the user as activated, to ensure backwards compatibility
 //				user.setActivationKey("N_supplied");
 //				user.setIsActivated(true);
 //				user.setEmail("Not_supplied");
 //				req.setStatus(CreateUserRequestStatus.ACCEPTED);
 //			} else {
 //				req.setIsAproved(true);
 				req.setStatus(CreateUserRequestStatus.ACCEPTED);
 				user.setIsActivated(false);
 				if(!EmailUtils.verifyEmail(user.getEmail())) {
 					// Invalid email
 					// Give no error messages to email, but log the event, since clients should take care of this.
 //					req.setIsAproved(false);
 					req.setStatus(CreateUserRequestStatus.UNHANDLED);
 					System.out.println("Invalid email!");
 					serverConnection.writeToLog("Invalid email from " + user.getUsername() + " at IP " + connection.getInetAddress().getHostAddress());
 				}
 				// If a version is supplied, process email and activation
 				// Check that the email is not already registered
 				System.out.println("Checking if email is already registered..");
 				boolean foundEmail = false;
 				for (User u : serverConnection.getUsers()) {
 					if(u.getEmail().equals(user.getEmail()))
 						foundEmail = true;
 				}
 				if(foundEmail) {
 					System.out.println("Email already registered!");
 					// Set the request as not approved
 //					req.setIsAproved(false);
 					req.setStatus(CreateUserRequestStatus.EMAIL_ALREADY_REGISTERED);
 				} else {
 					System.out.println("Email OK.");
 				}
 //			}
 			if(req.isApproved()) {
 				// Get an id for the user
 				user.setId(serverConnection.getNextUserId());
 				// Notify the server of the new user if it was approved
 				serverConnection.addUser(user, req.getPassword());
 			}
 		} 
 		// Reply with an answer
 		send(req.toXML());
 		// Set activation key after we have sent the response, so it is not sent to the user
 		if(/* req.getVersion() != null && */ req.isApproved() && user != null) {
 			System.out.println("Generating activation key for user: " + user.getUsername());
 			// Generate activation key
 			user.setActivationKey(PasswordHasher.hashPassword(((System.currentTimeMillis() + (long) (Math.random() * 10000000)) + "")).substring(0, 10));
 			// FIXME Send email with activation key
 			System.out.println("Sending activation key for " + user.getUsername() + " to " + user.getEmail() + ".");
 			try {
 				MailSender.sendActivationKey(user.getActivationKey(), user);
 			} catch (Exception e) {
 				System.out.println("Failed sending mail!");
 				e.printStackTrace();
 				serverConnection.writeToLog("Failed sending activation key to user: " + user.getUsername() + " at " + user.getEmail() + ": " + e.toString());
 			}
 		}
 	}
 	
 	/**
 	 * @return	The IP address of the connected user
 	 */
 	public String getUserIp() {
 		return connection.getInetAddress().getHostAddress();
 	}
 	
 	/**
 	 * Process the given LogInRequest by setting this ServerConnectionHandler's user if login is correct.
 	 * Will also on correct login start a UpdateSender for this connection at the port specified in the LogInRequest
 	 * @param req	The LogInRequest
 	 */
 	public void processLoginRequest(LogInRequest req) {
 		System.out.println("Received log in request!");
 		User user = serverConnection.getUser(req.getUserName());
 		if(user == null) {
 			System.out.println("User not found!");
 		}
 		if(user != null /* && user.getUsername().equalsIgnoreCase(req.getUserName()) */ 
 				&& serverConnection.checkPassword(user, req.getPassword()) 
 				/* && !user.isOnline()*/) {
 			// Check that the user is activated
 			if(!user.isActivated()) {
 				// Check if activation key is attached
 				System.out.println("User not activated!");
 				if(req.getUser().getActivationKey() == null) {
 					req.setStatus(LogInRequestStatus.NOT_ACTIVATED);
 					System.out.println("No activation key attached.");
 				} else {
 					// Verify activation key
 					System.out.println("Verifying activation key..");
 					if(user.getActivationKey().equals(req.getUser().getActivationKey())) {
 						user.setIsActivated(true);
 						System.out.println("Activation key OK!");
 					} else {
 						req.setStatus(LogInRequestStatus.INVALID_ACTIVATION_KEY);
 						System.out.println("Activation failed.");
 					}
 				}
 			} 
 			if(user.isActivated()){
 				System.out.println("Log in OK!");
 				user.setIsOnline(true);
 				this.user = user;
 				req.setAccepted(true);
 				req.setStatus(LogInRequestStatus.ACCEPTED);
 				// Load the user variables
 				req.setUser((User) user);
 				if(req.isAccepted()) {
 					System.out.println("Log in is set to accepted!");
 					// Assign session token if requested
 					if(req.getSessionToken() != null && req.getSessionToken().equals(Constants.SESSION_TOKEN_REQUEST)) {
 						System.out.println("Token request received.");
 						String token = serverConnection.getTokenManager().generateToken(this);
 						System.out.println("Token granted to " + user.getUsername() + ": " + token);
 						req.setSessionToken(token);
 						this.token = token;
 					}
 				}
 			}
 		} else if(user != null && user.isOnline()){
 			req.setStatus(LogInRequestStatus.ALREADY_LOGGED_ON);
 			System.out.println("User already online. (SHOULD NOT HAPPEN!)");
 			serverConnection.writeToLog("User not able to log in, because it is already online.. This should NOT happen.");
 		} else {
 			req.setStatus(LogInRequestStatus.WRONG_INFORMATION);
 			System.out.println("Username or password failed");
 		}
 		String temp = req.toXML();
 		System.out.println("Sending XML: " + temp);
 		send(temp);
 	}
 	
 	/**
 	 * Process the given debt
 	 * @param d	The debt
 	 */
 	public void processDebt(Debt d) {
 		switch(d.getStatus()) {
 		case REQUESTED:
 			System.out.println("REQUESTED");
 			processRequestedDebt(d);
 			break;
 		case CONFIRMED:
 		case DECLINED:
 			System.out.println("CONFIRMED/DECLINED");
 			processConfirmedDeclinedDebt(d);
 			break;
 		case COMPLETED_BY_FROM:
 		case COMPLETED_BY_TO:
 			System.out.println("COMPLETE BY FROM/TO");
 			processCompletedDebt(d);
 			break;
 		case COMPLETED:
 			System.out.println("COMPLETED BY BOTH!!");
 			// TODO: Do we need to process this? No!
 			break;
 		}
 	}
 
 	public void processCompletedDebt(Debt d) {
 		// TODO: Verify!!!!
 		Debt old = null;
 		for (int i = 0; i < getUser().getNumberOfConfirmedDebts(); i++) {
			if(getUser().getConfirmedDebt(i).getId() == d.getId()) old = getUser().getConfirmedDebt(i);
 		}
 		if(old == null) {
 			System.out.println("Something wrong happened while processing completedDebt");
 //			Main.printDebts(getUser().getConfirmedDebts(), "Confirmed debts");
 //			Main.printDebts(getUser().getPendingDebts(), "Pending debts");
 			return;
 		}
 		// Check that this user has not already completed this debt. NO why should we? (Not this way at least.)
 //		if((d.getTo().equals(getUser()) && d.getStatus() == DebtStatus.COMPLETED_BY_TO) || (d.getFrom().equals(getUser()) && d.getStatus() == DebtStatus.COMPLETED_BY_FROM)) {
 //			// TODO: Then what? Send back a correct VERSION of the debt?
 //			System.out.println("Completing of debt failed, because this user has already marked the debt as complete");
 //			return;
 //		}
 		if((old.getStatus() == DebtStatus.COMPLETED_BY_FROM && d.getStatus() == DebtStatus.COMPLETED_BY_TO) || (old.getStatus() == DebtStatus.COMPLETED_BY_TO && d.getStatus() == DebtStatus.COMPLETED_BY_FROM)) {
 			d.setStatus(DebtStatus.COMPLETED);
 		} 
 		old.setStatus(d.getStatus());
 		serverConnection.notifyUser((old.getTo().equals(getUser()) ? old.getFrom() : old.getTo()).getUsername(), old, token);
 		send(old.toXML());
 	}
 	
 	public void processConfirmedDeclinedDebt(Debt d) {
 		// TODO: Verify!!
 		// Find our instance of the debt
 		// We assume that it is pending, or else why would someone accept or decline it?
 		Debt our = null;
 		for (int i = 0; i < getUser().getNumberOfPendingDebts(); i++) {
 			if(getUser().getPendingDebt(i).getId() == d.getId()) our = getUser().getPendingDebt(i); 
 			System.out.println("Checked: " + getUser().getPendingDebt(i).getId() + " should find: " + d.getId());
 		}
 		if(our == null) {
 			// Something wrong has happened! This debt was not ours, or not pending.
 			// TODO Do nothing?
 			System.out.println("SOMETHING WRONG HAPPENED WHILE PROCESSING CONFIRMED OR DECLINED DEBT!");
 			serverConnection.writeToLog("Something wrong happened while processing confirmed or declined debt for " + getUser().getUsername());
 			return;
 		}
 		our.setStatus(d.getStatus());
 		// Remove the debt from the pending list (since it is now confirmed or declined)
 		User other = serverConnection.getUser((our.getFrom().equals(getUser()) ? our.getTo() : our.getFrom()).getUsername());
 		System.out.println("Other user is: " + other.getUsername());
 		getUser().removePendingDebt(our);
 		if(other.removePendingDebt(our)) System.out.println("Other's debt was removed!");
 		else System.out.println("Other's debt was NOT(!!!!!!!!!!!) removed!");
 		if(our.getStatus() == DebtStatus.CONFIRMED) {
 			d = mergeDebts(our);
 			
 			// No longer needed, since mergeDebts does this for us
 //			// If the debt is now confirmed, we must move it to the correct lists
 //			getUser().addConfirmedDebt(our);
 //			// For both users
 //			other.addConfirmedDebt(our);
 		} else {
 			// If the debt was deleted we simply let it be removed..
 		}
 		// Let the requesting user know about the accept/decline
 		serverConnection.notifyUser(d.getRequestedBy().getUsername(), d, token);
 		send(d.toXML());
 		// TODO Anything else?
 	}
 	
 	public void processRequestedDebt(Debt d) {
 		System.out.println("From: " + d.getFrom().getUsername() + ", to: " + d.getTo().getUsername());
 		// Validate that this is a valid debt
 		boolean valid = true;
 		System.out.println("Checkin if new debt is valid..");
 		// Check that this user requested the debt
 		if(d.getRequestedBy().getUsername().equals(user.getUsername())) {
 			// Check if this user is the receiver of the debt, and if the sender is a friend
 			if(d.getTo().getUsername().equals(user.getUsername()) && user.getFriend(d.getFrom().getUsername()) == null) {
 				System.out.println("1");
 				valid = false;
 			// Check if this user is the sender of the debt, and the receiver is a friend
 			} else if(d.getFrom().getUsername().equals(user.getUsername()) && user.getFriend(d.getTo().getUsername()) == null) {
 				System.out.println("2");
 				valid = false;
 			} else {
 //				System.out.println("3");
 //				valid = false;
 			}
 			if(d.isConfirmed()) {
 				System.out.println("4");
 				valid = false;
 			}
 		} else valid = false;
 		System.out.println("New debt is valid? " + valid);
 		if(valid) {
 			d.setId(serverConnection.getNextDebtId());
 			System.out.println("id set to: " + d.getId());
 			// Save the debt
 			serverConnection.getUser((getUser().equals(d.getTo()) ? d.getFrom().getUsername() : d.getTo().getUsername())).addPendingDebt(d);
 			System.out.println("Added debt to " + getUser().getUsername() + " and " + serverConnection.getUser((getUser().equals(d.getTo()) ? d.getFrom().getUsername() : d.getTo().getUsername())).getUsername());
 			getUser().addPendingDebt(d);
 //			System.out.println(serverConnection.getUser((getUser().equals(d.getTo()) ? d.getFrom().getUsername() : d.getTo().getUsername())) == serverConnection.getHandler(serverConnection.getUser((getUser().equals(d.getTo()) ? d.getFrom().getUsername() : d.getTo().getUsername())).getUsername()).getUser());
 			// Notify other user
 			serverConnection.notifyUser((d.getTo().getUsername().equals(user.getUsername()) ? d.getFrom().getUsername() : d.getTo().getUsername()), d, token);
 		} else {
 			serverConnection.writeToLog("Received invalid debt from " + getUser().getUsername());
 		}
 		send(d.toXML());
 	}
 	
 	/**
 	 * 
 	 * @param d
 	 * @return	The debt to send to the clients (no matter if any merging was done)
 	 */
 	public Debt mergeDebts(Debt d) {
 		User thisUser = serverConnection.getUser(this.getUser().getUsername());
 		User otherUser = serverConnection.getUser((d.getTo().equals(thisUser) ? d.getFrom() : d.getTo()).getUsername());
 		List<Debt> debtsToMerge = new ArrayList<Debt>();
 		// Check if these two users already have debts between them
 		// Pending debts
 		for (int i = 0; i < thisUser.getNumberOfConfirmedDebts(); i++) {
 			Debt tDebt = thisUser.getConfirmedDebt(i);
 			if(tDebt.getTo().equals(otherUser) || tDebt.getFrom().equals(otherUser)) {
 				// Check if this debt uses the same currency
 				if(tDebt.getWhat().equalsIgnoreCase(d.getWhat())) {
 					// And check that it is not completed by any of the usersarg0
 					if(tDebt.getStatus() == DebtStatus.CONFIRMED) {
 						debtsToMerge.add(tDebt);
 					}
 				}
 			}
 		}
 		if(!debtsToMerge.isEmpty()) {
 			d.setComment('"' + d.getComment() + '"');
 		}
 		for (Debt debtToMerge : debtsToMerge) {
 			// Remove debts from users .. NO! Set them as deleted
 			thisUser.getConfirmedDebtById(debtToMerge.getId()).setStatus(DebtStatus.DELETED);
 			otherUser.getConfirmedDebtById(debtToMerge.getId()).setStatus(DebtStatus.DELETED);
 //			thisUser.removeConfirmedDebt(debtToMerge);
 //			otherUser.removeConfirmedDebt(debtToMerge);
 			// Merge amount
 			if(d.getTo().equals(debtToMerge.getTo())) {
 				d.setAmount(d.getAmount() + debtToMerge.getAmount());
 			} else {
 				d.setAmount(d.getAmount() - debtToMerge.getAmount());
 			}
 			// Merge comments
 			d.setComment(d.getComment() + " " + '"' + debtToMerge.getComment() + '"');
 		}
 		// Check if amount is negative
 		if(d.getAmount() < 0) {
 			// Swap to and from users
 			User temp = d.getFrom();
 			d.setFrom(d.getTo());
 			d.setTo(temp);
 			d.setAmount(d.getAmount() * -1);
 		}
 		// Check that amount is greater than zero
 		if(Math.abs(d.getAmount()) > 0) {
 			// Add the debt to the users
 			thisUser.addConfirmedDebt(d);
 			otherUser.addConfirmedDebt(d);
 		}
 		if(!debtsToMerge.isEmpty()) {
 			System.out.println("This was a merge!");
 			return new Debt(d.getId(), d.getAmount(), d.getWhat(), d.getFrom(), d.getTo(), d.getComment(), d.getRequestedBy(), DebtStatus.MERGE);
 		} else {
 			System.out.println("This was NOT a merge!");
 			return d;
 		}
 	}
 	
 	public String receive() {
 		try {
 			return reader.readLine();
 		} catch (IOException e) {
 			System.out.println("User disconnected!");
 			if(this.user != null) {
 				this.user.setIsOnline(false);
 			}
 			running = false;
 			return null;
 		}
 	}
 	
 	public void send(String msg) {
 		XMLSerializable o = null;
 		try {
 			o = XMLSerializable.toObject(msg);
 			// Attach version
 			if(o instanceof Request) {
 				((Request) o).setServerVersion(Constants.SERVER_VERSION);
 			}
 			// Attach session token if present
 			if(this.token != null) {
 				System.out.println("Attaching token: " + this.token);
 				o.setSessionToken(this.token);
 			} else {
 				System.out.println("Did not attach any token because this session has none.");
 			}
 			msg = o.toXML();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			serverConnection.writeToLog("Error while parsing before send (in send): " + e.toString());
 		}
 		System.out.println("Sending: " + msg);
 		// FIXME
 		writer.println(msg);
 	}
 	
 	/**
 	 * Attempts to close the connection, log off the user and stop the thread.
 	 */
 	public void close() {
 		running = false;
 		try {
 			connection.close();
 		} catch (Exception e) {}
 		try {
 			reader.close();
 		} catch (Exception e) {}
 		try {
 			writer.close();
 		} catch (Exception e) {}
 		if(this.user != null) {
 			this.user.setIsOnline(false);
 		}
 	}
 }
