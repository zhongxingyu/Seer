 package node.facebook;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 import java.util.Vector;
 import edu.washington.cs.cse490h.lib.Utility;
 import node.rpc.IFacebookServer;
 import edu.washington.cs.cse490h.lib.PersistentStorageReader;
 import edu.washington.cs.cse490h.lib.PersistentStorageWriter;
 
 
 public class FacebookShardSystem extends BaseFacebookSystem implements IFacebookServer {
 
 	private FacebookShardState m_state = new FacebookShardState();
 	private FacebookShardState m_uncommitedState = null;
 	private UUID m_activeTxn = null;
 	
 	private static final String FILE_NAME = "facebookstate.txt";
 	private static final String FILE_NAME_TEMP = "facebookstate_temp.txt";
 	
 	private boolean saveState() {
 		return this.saveStateInFile(this.m_state, FILE_NAME);
 	}
 	
 	private boolean saveUncommitedState() {
 		return this.saveStateInFile(this.m_uncommitedState, FILE_NAME_TEMP);
 	}
 	
 	private boolean saveStateInFile(FacebookShardState state, String file) {
 		boolean success = false;
 		try {
 			String content = FacebookShardState.serialize(state);
 			
 			PersistentStorageWriter psw = m_node.getWriter(file, false);
 			psw.write(content);
 			psw.close();
 			success = true;
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		return success;
 	}
 	
 	public void recoverFromCrash() {
		// TODO-sergio: fix restoreState() to NOT return null when there is no state to recover
		//this.m_state = restoreState(FILE_NAME);
 		this.m_uncommitedState = restoreState(FILE_NAME_TEMP);
 	}
 	
 	private FacebookShardState restoreState(String filename) {
 		try {
 			if (Utility.fileExists(m_node, filename)) {
 				PersistentStorageReader psr = m_node.getReader(filename);
 				char[] buffer = new char[1024];
 				StringBuffer sb = new StringBuffer();
 				do {
 					int len = psr.read(buffer, 0, 1024);
 					if (len > 0) {
 						sb.append(buffer, 0, len);
 					} else {
 						break;
 					}
 				} while (true);
 				
 				return FacebookShardState.deserialize(sb.toString());
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * FacebookShardSystem()
 	 * @param node
 	 */
 	public FacebookShardSystem(FacebookRPCNode node) {
 		super(node);
 	}
 
 	/**
 	 * API: IFacebookServer.createUser
 	 */
 	public String createUser(String userName, String password) throws FacebookException {
 		if (this.m_state.containsUser(userName)) {
 			throw new FacebookException(FacebookException.USER_ALREADY_EXISTS);
 		} else {
 			this.m_state.addUser(userName, new User(userName, password));
 			this.saveState();		
 			this.user_info("created user " + userName + " " + password);
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * API: IFacebookServer.login
 	 */
 	public String login(String userName, String password) throws FacebookException {
 		if (this.m_state.containsUser(userName)) {
 			String token = new SessionToken(userName, createNewSessionSeed()).toString();
 			this.m_state.addSession(token);
 			
 			this.saveState();
 			this.user_info("User: " + userName + " logged in, token: " + token);
 			return token;
 		} else {
 			throw new FacebookException(FacebookException.USER_DONT_EXIST);
 		}
 	}
 	
 	/**
 	 * API: IFacebookServer.logout
 	 */
 	public String logout(String token) throws FacebookException {
 		if (this.m_state.containsSession(token)) {
 			this.m_state.removeSession(token);
 			
 			this.saveState();
 			this.user_info("Token: " + token + " logged out");
 		} else {
 			throw new FacebookException(FacebookException.SESSION_DONT_EXIST);
 		}
 		
 		return null;
 	}
 	
 	public String addFriendReceiver(String adderLogin, String receiverLogin) throws FacebookException {
 		if (!this.m_state.containsUser(receiverLogin)) {
 			throw new FacebookException(FacebookException.USER_DONT_EXIST);
 		}
 
 		// Get the *friend's* request list
 		List<String> listFriends;
 		listFriends = this.m_state.getPendingRequest(receiverLogin);
 		
 		// Add the user to the friend's request list
 		listFriends.add(adderLogin);
 		
 		this.saveState();
 		this.user_info("User: " + adderLogin + " requested to be friends of user " + receiverLogin);
 		
 		return null;
 	}
 	
 	/**
 	 * API: IFacebookServer.acceptFriend
 	 */
 	public String acceptFriendReceiver(String adderLogin, String receiverLogin) throws FacebookException {
 		List<String> requestList;
 		requestList = this.m_state.getPendingRequest(receiverLogin);
 		
 		if (!requestList.contains(adderLogin)) {
 			// Can't accept friendship of somebody who hasn't requested it
 			throw new FacebookException(FacebookException.INVALID_REQUEST);
 		}
 		
 		requestList.remove(adderLogin);
 		this.m_state.addFriendToList(receiverLogin, adderLogin);
 		
 		this.saveState();
 		this.user_info("(Receiver) User: " + receiverLogin + " accepted to be friends of user " + adderLogin);
 		
 		return null;
 	}
 	
 	public String acceptFriendAdder(String token, String adderLogin) throws FacebookException {
 		// TODO: either remove session tokens, or auto-add session token to 'receiverLogin'
 		String receiverLogin = token;
 		
 		if (!this.m_state.containsUser(receiverLogin)) {
 			throw new FacebookException(FacebookException.SESSION_DONT_EXIST);
 		}
 		
 		this.m_state.addFriendToList(receiverLogin, adderLogin);
 		
 		this.saveState();
 		this.user_info("(Adder) User: " + receiverLogin + " was auto-added as friends of user " + adderLogin);
 		
 		return null;
 	}
 	
 
 
 	private String createNewSessionSeed()
 	{
 		// Let's return a fixed value to make our life easier 
 		return "1234";
 	}
 
 	private String extractUserLogin(String token) throws FacebookException
 	{
 		if (!this.m_state.containsSession(token)) {
 			throw new FacebookException(FacebookException.SESSION_DONT_EXIST);
 		}
 		
 		return SessionToken.createFromString(token).getUser();
 	}
 	
 	/**
 	 * API: IFacebookServer.writeMessageAll
 	 */
 	public String writeMessageAll(String from, String transactionId, String message) throws FacebookException {
 
 		if (this.m_uncommitedState != null) {
 			throw new FacebookException(FacebookException.CONCURRENT_TRANSACTIONS_NOT_ALLOWED, transactionId);
 		}
 		
 		if (this.m_activeTxn != null) {
 			throw new FacebookException(FacebookException.CONCURRENT_TRANSACTIONS_NOT_ALLOWED, transactionId);
 		}
 		
 		Set<String> logins = this.m_state.getUserLogins();
 		Message m = new Message(from, message);
 		
 		this.m_activeTxn = UUID.fromString(transactionId);
 		this.m_uncommitedState = this.m_state.clone();
 		
 		for (String login: logins) {
 			// Just add the message if the users are friends
 			if (this.m_uncommitedState.isFriendOf(login, from)) {
 				this.m_uncommitedState.addMessage(login, m);
 			}
 		}
 
 		// Save uncommited state to disk
 		//
 		// TODO-sergio: saving state here is not the right thing to do... instead, we  
 		// should only store the message, separately from the actual shard state. Then,    
 		// during prepare(), the message should be added to the current state and saved 
 		// to disk. Otherwise, if other methods are called between the execution of
 		// the write_message_all and the commit, the state that we're saving now will 
 		// overwrite the changes from the other methods.
 		// Also, no command should be allowed to execute between the prepare() and the 
 		// commit(), for the same reason.
 		
 		boolean saved;
 		saved = this.saveUncommitedState();
 		if (!saved) {
 			abort(m_activeTxn);
 			throw new FacebookException(FacebookException.CANNOT_SAVE_STATE);
 		}
 		
 		return transactionId;
 	}
 	
 	public void writeMessageAllCommit() {
 		boolean success;
 		
 		this.m_state = this.m_uncommitedState;
 		// TODO: must ensure that this *NEVER* fails! Currently it can fail!
 		// This should be something that is guaranteed to succeed, maybe a file
 		// rename after deleting the file with the final name. 
 		success = this.saveState();
 		if (!success)
 		{
 			m_node.error("CATASTROPHIC FAILURE! commit() failed, the changes are lost and the system is inconsistent"); 
 		}
 		
 		this.m_uncommitedState = null;
 		deleteTempFile();
 	}
 	
 	public void writeMessageAllAbort() {
 		this.m_uncommitedState = null;
 		deleteTempFile();
 	}
 	
 	private void deleteTempFile() {
 		try {
 			if (Utility.fileExists(this.m_node, FILE_NAME_TEMP)) {
 				PersistentStorageWriter f = m_node.getWriter(FILE_NAME_TEMP, false);
 				f.delete();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * API: IFacebookServer.readMessageAll
 	 */
 	public String readMessageAll(String token) throws FacebookException {
 		String login = extractUserLogin(token);
 
 		Vector<Message> messages = this.m_state.getUserMessages(login);
 		StringBuffer sb = new StringBuffer();
 
 		if (messages != null) {
 			for (Message message : messages) {
 				sb.append("From:");
 				sb.append(message.getFromLogin());
 			 	sb.append('\n');
 			 	sb.append("Content:");
 			 	sb.append(message.getMessage());
 			 	sb.append('\n');
 			}
 		}
 
 		return sb.toString();
 	}
 	
 	/**
 	 * Called by 2pc to inform the node that it must abort the 
 	 * active transaction.
 	 */
 	public void abort(UUID transactionId)
 	{
 		if (m_activeTxn != null && transactionId.compareTo(m_activeTxn) == 0)
 		{
 			this.writeMessageAllAbort();
 			m_activeTxn = null;
 		}
 		else 
 		{
 			// Unknown transaction, safe to ignore
 		}
 	}
 	
 	/**
 	 * Called by 2pc to inform the node that it must commit the 
 	 * active transaction.
 	 */
 	public void commit(UUID transactionId)
 	{
 		if (m_activeTxn != null && transactionId.compareTo(m_activeTxn) == 0)
 		{
 			this.writeMessageAllCommit();
 			m_activeTxn = null;
 		}
 		else
 		{
 			// TODO: handle this case (not sure how :p) 
 		}
 	}
 	
 	/**
 	 * Called by 2pc to inform the node that it should prepare to commit
 	 * the active transaction (i.e. save state in durable storage).
 	 * Returns true if the state is properly saved, false otherwise.
 	 */
 	public boolean prepare(UUID transactionId)
 	{
 		if (m_activeTxn != null && transactionId.compareTo(m_activeTxn) == 0)
 		{
 			// Transaction IDs match, merge pending message and save state in
 			// a temporary file.
 			
 			// TODO-sergio: actually do the above. See comments in write_message_all().
 			return true;
 		}
 		else
 		{
 			// Unknown transaction, return failure
 			return false;
 		}
 	}
 	
 }
