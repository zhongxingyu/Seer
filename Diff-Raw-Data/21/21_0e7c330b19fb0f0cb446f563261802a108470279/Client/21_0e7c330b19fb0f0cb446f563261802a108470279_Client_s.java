 package cs4520.client;
 
 import java.net.*;
 import java.io.*;
 
 /**
  * @author Oliver Maskery
  * 
  * Represents a simple client, making a connection to the CS4520 server, logging in and acquiring the secret data.
  * 
  */
 public class Client {
 	public enum LoginResult
 	{
 		Success,
 		IncorrectUsername,
 		IncorrectSecret,
 		InvalidResponse
 	}
 	
 	private BufferedReader mReader;		// used to read from the client socket
 	private PrintStream mWriter;		// used to write to the client socket
 	private String mSecret;				// the secret data we've acquired from the server, initially null
 	private Socket mClient;				// the client socket to communicate with the server
 	
 	/**
 	 * Client constructor
 	 * @param _target The target to connect to, either an IP or hostname for the machine running the CS4520 server.
 	 * @param _port The port number to establish the connection on, must be the same as the one the server is listening on.
 	 * @throws UnknownHostException
 	 * @throws IOException
 	 */
 	public Client(String _target, int _port) throws UnknownHostException, IOException
 	{
 		mSecret = null;
 		// establish a connection to the server, generally initialise the connection
 		mClient = new Socket(_target, _port);
 		mReader = new BufferedReader(new InputStreamReader(mClient.getInputStream()));
 		mWriter = new PrintStream(mClient.getOutputStream());
 	}
 	
 	/**
 	 * Retrieves the secret obtained from the server
 	 * @return String representation of secret if successfully logged in, null otherwise
 	 */
 	public String getSecret()
 	{
 		return mSecret;
 	}
 	
 	/**
 	 * Attempts to login to the CS4520 server
 	 * @param _username	The username to provide as part of the login credentials
 	 * @param _secret The 'secret' to provide as part of the login credentials
 	 * @return Whether or not the login attempt was successful, and if not, the reason for failure
 	 * @throws IOException
 	 */
 	public LoginResult login(String _username, String _secret) throws IOException
 	{
 		LoginResult result = LoginResult.InvalidResponse;
 		
 		// send the login credentials
 		mWriter.println(_username);
 		mWriter.println(_secret);
 		
 		// await the server's response
 		String response = mReader.readLine();
 		
 		// parse the response in the form of "[success]:[parameter_name]=[parameter_value]", e.g:
 		//		"valid:secret_data=ABCDABCD012301234567"
 		//		"invalid:reason=InvalidUsername"
 		String[] responseTokens = splitResponse(response);
 		
 		if(responseTokens.length == 2)
 		{
 			String[] parameters = splitParameters(responseTokens[1]);
 			
 			if(parameters.length == 2)
 			{
 				// check for "valid" response, with 'secret_data' parameter
 				if(responseTokens[0].equals("valid") && parameters[0].equals("secret_data"))
 				{
 					result = LoginResult.Success;
 					mSecret = parameters[1];
 				}
 				// check for "invalid" response, with 'reason' parameter
 				else if(responseTokens[0].equals("invalid") && parameters[0].equals("reason"))
 				{
 					switch(parameters[1])
 					{
 					case "InvalidUsername":
 						result = LoginResult.IncorrectUsername;
 						break;
 					case "InvalidSecret":
 						result = LoginResult.IncorrectSecret;
 						break;
 					}
 				}
 			}
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Split the response string of the form "[success]:[parameters]"
 	 * @param _response The string to be parsed
 	 * @return An array of tokens, should usually be of length 2
 	 */
 	private String[] splitResponse(String _response)
 	{
 		return _response.split(":");
 	}
 	
 	/**
 	 * Split the parameter string of the form "[parameter_name]=[parameter_value]"
 	 * @param _parameterString The parameter string to be parsed
 	 * @return An array of tokens, should usually be of length 2
 	 */
 	private String[] splitParameters(String _parameterString)
 	{
 		return _parameterString.split("=");
 	}
 	
 	public static void main(String[] args) {
 		// The login credentials to provide to the CS4520 server
		final String username = "aadmin";
 		final String secret = "secretsecret	";
 		
 		try {
 			// Connect
 			System.out.print("Attempting connection to CS4520 server...");
 			Client client = new Client("localhost", 28000);
 			System.out.println("done!");
 			
 			// Attempt login
 			System.out.print("Attempting login as '" + username + "' with secret '" + secret + "'...");
 			LoginResult result = client.login(username, secret);
 			
 			// Process response
 			if(result == LoginResult.Success)
 			{
 				System.out.println("success!");
 				
 				System.out.println("Secret: " + client.getSecret());
 			}
 			else
 			{
 				System.out.println("failure!");
 				
 				System.out.println("Reason for failure: " + result.toString());
 			}
 		} catch (UnknownHostException e) {
 			e.printStackTrace(System.err);
 		} catch (IOException e) {
 			e.printStackTrace(System.err);
 		}
 	}
 
 }
