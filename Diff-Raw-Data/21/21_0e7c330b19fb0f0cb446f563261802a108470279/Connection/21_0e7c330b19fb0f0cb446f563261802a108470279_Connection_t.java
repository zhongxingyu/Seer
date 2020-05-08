 package cs4520.server;
 
 import java.util.*;
 import java.net.*;
 import java.io.*;
 
 /**
  * @author oliver
  *
  * Object to handle a connection to the CS4520 server, handlers user login and serving of 'secret data'
  */
 public class Connection implements Runnable {
 	
 	private static long mNextID = 1;	// static variable used to provide unique ID to each connection, in case that becomes useful
 	
 	// list of all external objects awaiting notification of this Connection's completion
 	private ArrayList<CompletionListener> mListeners = new ArrayList<CompletionListener>();
 	private BufferedReader mReader;		// used for reading from the client socket
 	private PrintStream mWriter;		// used for writing to the client socket
 	private UserManager mUsers;			// reference to a UserManager for querying user credentials
 	private Socket mClient;				// socket connection to remote client
 	private Thread mThread;				// thread object for asynchronously handling client's requests
 	private long mID;					// this connection's unique ID
 	
 	/**
 	 * Constructor for Connection
 	 * @param _client The client that this connection represents
 	 * @throws IOException
 	 */
 	public Connection(Socket _client, UserManager _users) throws IOException
 	{
 		// Create a thread to handle this Connection
 		mThread = new Thread(this);
 		mClient = _client;
 		// store reference to user manager
 		mUsers = _users;
 		// Allocate a unique ID to this connection
 		mID = mNextID ++;
 		
 		// Configure the socket read/write objects
 		mReader = new BufferedReader(new InputStreamReader(mClient.getInputStream()));
 		mWriter = new PrintStream(mClient.getOutputStream());
 	}
 	
 	/**
 	 * Starts this connection's asynchronous handling of the client's requests
 	 */
 	public void start()
 	{
 		System.out.println(this.toString() + ": connected - starting thread");
 		mThread.start();
 	}
 	
 	/**
 	 * Used to register listeners for the Connection's completion
 	 * @param _listener The listener to add
 	 */
 	public void addListener(CompletionListener _listener)
 	{
 		mListeners.add(_listener);
 	}
 	
 	/**
 	 * Used to remove listeners currently listening for the Connection's completion
 	 * @param _listener The listener to remove
 	 */
 	public void removeListener(CompletionListener _listener)
 	{
 		mListeners.remove(_listener);
 	}
 	
 	/**
 	 * Method for transmitting a line of text to the client
 	 * @param _line The line of text to be transmitted
 	 */
 	public void tx(String _line)
 	{
 		mWriter.println(_line);
 	}
 	
 	/**
 	 * Method for reading one line of text from the client, blocks until received
 	 * @return The message sent by the client
 	 * @throws IOException
 	 */
 	public String rx() throws IOException
 	{
 		return mReader.readLine();
 	}
 	
 	/**
 	 * The asynchronous method that handles this Connection
 	 */
 	public void run()
 	{
 		try {
 			// call helper method to talk to client
 			threadRun();
 			// perform cleanup
 			mWriter.close();
 			mReader.close();
 			mClient.close();
 		} catch (Exception e) {
 			System.err.println(this.toString() + ": exception in thread:");
 			e.printStackTrace(System.err);
 		}
 		
 		// notify listeners of this Connection's completion
 		System.out.println(this.toString() + ": thread ending");
 		for(CompletionListener cl : mListeners)
 			cl.onCompletion(this);
 	}
 	
 	/**
 	 * Helper method called to handle a client's requests
 	 * @throws IOException
 	 */
 	private void threadRun() throws IOException
 	{
 		// Start a conversation with the client
 		Conversation loginUsername = createLoginConversation();
 		// Expect the client to send the first message message
 		loginUsername.expect();
 	}
 	
 	/**
 	 * Helper method to create a Conversation object that expects a username message from the client
 	 * @return The Conversation object used to talk to the client
 	 */
 	private Conversation createLoginConversation()
 	{
 		return new Conversation(this, new ResponseListener() {
 			public void onResponse(Conversation _conversation, String _message) throws IOException {
 				// Continue the conversation by expecting a 'secret' from the client, now we have the username
 				Conversation loginPassword = createValidateConversation(_message);
 				// Expect the 'secret' from the client
 				loginPassword.expect();
 			}
 		});
 	}
 	
 	/**
 	 * Helper method to create a Conversation object that expects a password message from the client before
 	 * validating the provided credentials
 	 * @param _username The username obtained from the previous step of the conversation
 	 * @return The Conversation object used to talk to the client
 	 */
 	private Conversation createValidateConversation(final String _username)
 	{
 		return new Conversation(this, new ResponseListener() {
 			public void onResponse(Conversation _conversation, String _message) throws IOException {
 				validateLoginAttempt(_username, _message);
 			}
 		});
 	}
 	
 	/**
 	 * Method that validates login information and responds to the client appropriately
 	 * @param _username The username provided by the client
 	 * @param _secret The secret provided by the client
 	 */
 	private void validateLoginAttempt(String _username, String _secret)
 	{
 		// validate the credentials
 		UserManager.ValidationResult result = mUsers.validateLogin(_username, _secret);
 		boolean valid = (result == UserManager.ValidationResult.ValidCredentials);
 		
 		String successString = valid? "successfully identified" : "failed to identify";
 		System.out.println(this.toString() + ": " + successString + " as '" + _username + "' with secret '" + _secret + "'");
 		
 		if(valid)
 		{
 			// tell the client they were successful, include the secret data
 			tx("valid:secret_data=" + getSecretData());
 		}
 		else
 		{
 			// tell the client they failed to login, provide the reason
 			tx("invalid:reason=" + result.toString());
 		}
 	}
 	
 	
 	
 	/**
 	 * Fetch the super secret data that must never be shared, it contains many secrets. Deep, dark secrets. So secret, and meaningful.
 	 * @return Can't tell you. Shh.
 	 */
 	private String getSecretData()
 	{
 		Random generator = new Random();
 		String secretData = "";
 		
 		String alphabet = "0123456789ABCDEF";
 		
 		for(int i = 0; i < 20; i++)
 		{
 			secretData += alphabet.charAt(generator.nextInt(alphabet.length()));
 		}
 		
 		return secretData;
 	}
 	
 	public String ip()
 	{
 		return ((InetSocketAddress) mClient.getRemoteSocketAddress()).getHostString();
 	}
 	
 	@Override
 	public String toString()
 	{
 		String result = "";
 		result += "[";
 		result += mID + "@" + ((InetSocketAddress) mClient.getRemoteSocketAddress()).getHostString();
 		result += "]";
 		return result;
 	}
 }
