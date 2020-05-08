 /* Implements the GroupClient Interface */
 
 import java.util.List;
 import java.security.*;
 import javax.crypto.*;
 import java.io.*;
 import java.util.*;
 
 public class GroupClient extends Client implements GroupClientInterface {
  	
 	public boolean connect(final String server, final int port, String username)
 	{
 		System.out.println("\n*** Attempting to connect to Group Server: NAME: " + server + "; PORT:" + port + " ***");
 
 		super.connect(server, port, username);
 		
 		if(setUpServer(server)==false)
 		{
 			System.out.println("\n!!! Group server connection failed: NAME: " + serverName + "; PORT: " + serverPort + " !!!");
 			return false;
 		}
 		System.out.println("\n*** Group Server connection successful: NAME: " + serverName + "; PORT:" + serverPort + " ***");
 		
 		return true;
 	}
 	
 	
 	public UserToken getToken(String username, String pwd, PublicKey myPublic)
 	{
 		int backupMsgNumber = msgNumber;
 		try
 		{
 			UserToken token = null;
 			Envelope message = null, response = null;
 
 			//Tell the server to return a token.
 			message = new Envelope("TOKEN");
 			message.addObject(msgNumber); 
 			message.addObject(username); //Add user name string
 			message.addObject(pwd);
 			message.addObject(myPublic);
 			System.out.println("\n>> ("+msgNumber+"): Sending Group Server Request: TOKEN");
 			message= cEngine.attachHMAC(message, HMACKey);
 			cEngine.writeAESEncrypted(message, aesKey, output);
 			msgNumber++;	 	
 			
 			//Get the response from the server
 			response = (Envelope)cEngine.readAESEncrypted(aesKey, input);
 			
 			//Successful response
 			if(response.getMessage().equals("OK"))
 			{
 				System.out.println("<< ("+msgNumber+"): Receiving Group Server Response: OK");
 				//If there is a token in the Envelope, return it 
 				ArrayList<Object> temp = null;
 				temp = response.getObjContents();
 				
 				if(temp.size() == 4)
 				{
 					if(!checkMessagePreReqs(response)) return null;
 
 					token = (UserToken)temp.get(1);
 					
 					handleMapRetrieval((HashMap<String, HashMap<Date, AESKeySet>>)response.getObjContents().get(2));
 
 					if(token != null)System.out.println("\n*** Token obtained ***");
 					return token;
 				}
 				return null;
 			}
 			else
 			{	
 				System.out.println(cEngine.formatAsError(response.getMessage()));
 				return null;
 			}
 		}
 		catch(Exception e)
 		{
 			msgNumber = backupMsgNumber;
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 			return null;
 		}
 		
 	 }
 	 
 	 public boolean createUser(String username, String pwd, UserToken token)
 	 {
 		try
 		{
 			Envelope message = null, response = null;
 
 			//Tell the server to create a user
 			message = new Envelope("CUSER");
 			message.addObject(token); //Add the requester's token
 			message.addObject(msgNumber++); //Add the nessage number
 			message.addObject(username); //Add user name string
 			message.addObject(pwd);//add the desired password
 			System.out.println("\n>> ("+msgNumber+"): Sending Group Server Request: CUSER");
 			message= cEngine.attachHMAC(message, HMACKey);
 			cEngine.writeAESEncrypted(message, aesKey, output);
 			
 			response = (Envelope)cEngine.readAESEncrypted(aesKey, input);
 
 				if(!checkMessagePreReqs(response)) return false;
 			//If server indicates success, return true
 			if(response.getMessage().equals("OK"))
 			{
 				System.out.println("<< ("+msgNumber+"): receiving Group Server Response: OK");
 				return true;
 			}
 			else
 			{	
 				System.out.println(cEngine.formatAsError(response.getMessage()));
 				return false;
 			}
 		}
 		catch(Exception e)
 		{
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 			return false;
 		}
 	 }
 	 
 	 public boolean deleteUser(String username, UserToken token)
 	 {
 		try
 		{
 			Envelope message = null, response = null;
 			 
 			//Tell the server to delete a user
 			message = new Envelope("DUSER");
 			message.addObject(token);  //Add requester's token
 			message.addObject(msgNumber++); //Add the nessage number
 			message.addObject(username); //Add user name
 			System.out.println("\n>> ("+msgNumber+"): Sending Group Server Request: DUSER");
 			message= cEngine.attachHMAC(message, HMACKey);
 			cEngine.writeAESEncrypted(message, aesKey, output);
 			
 			response = (Envelope)cEngine.readAESEncrypted(aesKey, input);	
 			
 			if(!checkMessagePreReqs(response)) return false;
 
 			//If server indicates success, return true
 			if(response.getMessage().equals("OK"))
 			{
 				System.out.println("<< ("+msgNumber+"): receiving Group Server Response: OK");
 
 				handleMapRetrieval((HashMap<String, HashMap<Date, AESKeySet>>)response.getObjContents().get(1));
 
 				return true;
 			}
 			else
 			{	
 				System.out.println(cEngine.formatAsError(response.getMessage()));
 				return false;
 			}
 		}
 		catch(Exception e)
 		{
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 			return false;
 		}
 	 }
 	 
 	 public boolean createGroup(String groupname, UserToken token)
 	 {
 		try
 		{
 			Envelope message = null, response = null;
 			//Tell the server to create a group
 			message = new Envelope("CGROUP");
 			message.addObject(token); //Add the requester's token
 			message.addObject(msgNumber++); //Add the nessage number
 			message.addObject(groupname); //Add the group name string
 			System.out.println("\n>> ("+msgNumber+"): Sending Group Server Request: CGROUP");
 			message= cEngine.attachHMAC(message, HMACKey);
 			cEngine.writeAESEncrypted(message, aesKey, output);
 			
 			response = (Envelope)cEngine.readAESEncrypted(aesKey, input);	
 
 			if(!checkMessagePreReqs(response)) return false;
 
 			//If server indicates success, return true
 			if(response.getMessage().equals("OK"))
 			{
 				System.out.println("<< ("+msgNumber+"): receiving Group Server Response: OK");
 
 
 				handleMapRetrieval((HashMap<String, HashMap<Date, AESKeySet>>)response.getObjContents().get(1));
 
 				return true;
 			}
 			else
 			{	
 				System.out.println(cEngine.formatAsError(response.getMessage()));
 				return false;
 			}
 		}
 		catch(Exception e)
 		{
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 			return false;
 		}
 	 }
 	 
 	 public boolean deleteGroup(String groupname, UserToken token)
 	 {
 		try
 		{
 			Envelope message = null, response = null;
 			//Tell the server to delete a group
 			message = new Envelope("DGROUP");
 			message.addObject(token); //Add requester's token
 			message.addObject(msgNumber++); //Add the nessage number
 			message.addObject(groupname); //Add group name string
 			System.out.println("\n>> ("+msgNumber+"): Sending Group Server Request: DGROUP");
 			message= cEngine.attachHMAC(message, HMACKey);
 			cEngine.writeAESEncrypted(message, aesKey, output);
 			
 			response = (Envelope)cEngine.readAESEncrypted(aesKey, input);	
 
 			if(!checkMessagePreReqs(response)) return false;
 
 			//If server indicates success, return true
 			if(response.getMessage().equals("OK"))
 			{
 				System.out.println("<< ("+msgNumber+"): receiving Group Server Response: OK");
 
 
 				handleMapRetrieval((HashMap<String, HashMap<Date, AESKeySet>>)response.getObjContents().get(1));
 
 				return true;
 			}
 			else
 			{	
 				System.out.println(cEngine.formatAsError(response.getMessage()));
 				return false;
 			}
 		}
 		catch(Exception e)
 		{
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 			return false;
 		}
 	 }
 	 
 	 @SuppressWarnings("unchecked")
 	public List<String> listMembers(String group, UserToken token)
 	 {
 		try
 		{
 			Envelope message = null, response = null;
 			//Tell the server to return the member list
 			message = new Envelope("LMEMBERS");
 			message.addObject(token); //Add requester's token
 			message.addObject(msgNumber++); //Add the nessage number
 			message.addObject(group); //Add group name string
 			System.out.println("\n>> ("+msgNumber+"): Sending Group Server Request: LMEMBERS");
 			message= cEngine.attachHMAC(message, HMACKey);
 			cEngine.writeAESEncrypted(message, aesKey, output);
 			
 			response = (Envelope)cEngine.readAESEncrypted(aesKey, input);	
 
 			if(!checkMessagePreReqs(response)) return null;
 
 			//If server indicates success, return the member list
 			if(response.getMessage().equals("OK"))
 			{ 
 				System.out.println("<< ("+msgNumber+"): receiving Group Server Response: OK");
 
 				return (List<String>)response.getObjContents().get(1); //This cast creates compiler warnings. Sorry.
 			}
 			else
 			{	
 				System.out.println(cEngine.formatAsError(response.getMessage()));
 				return null;
 			}
 		}
 		catch(Exception e)
 		{
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 			return null;
 		}
 	}
 	 
 	public boolean addUserToGroup(String username, String groupname, UserToken token)
 	{
 		try
 		{
 			Envelope message = null, response = null;
 			//Tell the server to add a user to the group
 			message = new Envelope("AUSERTOGROUP");
 			message.addObject(token); //Add requester's token
 			message.addObject(msgNumber++); //Add the nessage number
 			message.addObject(username); //Add user name string
 			message.addObject(groupname); //Add group name string
 			System.out.println("\n>> ("+msgNumber+"): Sending Group Server Request: AUSERTOGROUP");
 			message= cEngine.attachHMAC(message, HMACKey);
 			cEngine.writeAESEncrypted(message, aesKey, output);
 			
 			response = (Envelope)cEngine.readAESEncrypted(aesKey, input);	
 
 			if(!checkMessagePreReqs(response)) return false;
 
 			//If server indicates success, return true			
 			if(response.getMessage().equals("OK"))
 			{
 				System.out.println("<< ("+msgNumber+"): receiving Group Server Response: OK");
 				return true;
 			}
 			else
 			{	
 				System.out.println(cEngine.formatAsError(response.getMessage()));
 				return false;
 			}
 		}
 		catch(Exception e)
 		{
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 			return false;
 		}
 	}
 	 
 	public boolean deleteUserFromGroup(String username, String groupname, UserToken token)
 	{
 		try
 		{
 			Envelope message = null, response = null;
 			//Tell the server to remove a user from the group
 			message = new Envelope("RUSERFROMGROUP");
 			message.addObject(token); //Add requester's token
 			message.addObject(msgNumber++); //Add the nessage number
 			message.addObject(username); //Add user name string
 			message.addObject(groupname); //Add group name string
 			System.out.println("\n>> ("+msgNumber+"): Sending Group Server Request: RUSERFROM GROUP");
 			message= cEngine.attachHMAC(message, HMACKey);
 			cEngine.writeAESEncrypted(message, aesKey, output);
 			
 			response = (Envelope)cEngine.readAESEncrypted(aesKey, input);	
 
 			if(!checkMessagePreReqs(response)) return false;
 
 			//If server indicates success, return true
 			if(response.getMessage().equals("OK"))
 			{
 				System.out.println("<< ("+msgNumber+"): receiving Group Server Response: OK");
 
				handleMapRetrieval((HashMap<String, HashMap<Date, AESKeySet>>)response.getObjContents().get(0));
 
 				return true;
 			}
 			else
 			{	
 				System.out.println(cEngine.formatAsError(response.getMessage()));
 				return false;
 			}
 		}
 		catch(Exception e)
 		{
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 			return false;
 		}
 	}
 
 	public ArrayList<String> allUsers(UserToken token)
 	{
 		try
 		{
 			Envelope message = null, response = null;
 			message = new Envelope("ALLUSERS");
 			message.addObject(token); //Add user's token
 			message.addObject(msgNumber++); //Add the nessage number
 			System.out.println("\n>> ("+msgNumber+"): Sending Group Server Request: ALLUSERS");
 			message= cEngine.attachHMAC(message, HMACKey);
 			cEngine.writeAESEncrypted(message, aesKey, output);
 			
 			response = (Envelope)cEngine.readAESEncrypted(aesKey, input);	
 
 			if(!checkMessagePreReqs(response)) return null;
 
 			if(response.getMessage().equals("OK") && response.getObjContents() != null)
 			{
 				System.out.println("<< ("+msgNumber+"): receiving Group Server Response: OK");
 
 				return (ArrayList<String>)response.getObjContents().get(1);
 			}
 			else
 			{	
 				System.out.println(cEngine.formatAsError(response.getMessage()));
 				return null;
 			}
 		}
 		catch(Exception e)
 		{
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 			return null;
 		}
 	}
 
 //----------------------------------------------------------------------------------------------------------------------
 //-- UTILITY FUNCITONS
 //----------------------------------------------------------------------------------------------------------------------
 	
 	public boolean handleMapRetrieval(HashMap<String, HashMap<Date, AESKeySet>> newMap)
 	{
 		if(groupFileKeyMap.syncWithNewKeyMap(newMap, true))
 		{
 			System.out.println(cEngine.formatAsSuccess("Group Key Map retrieval succeeded"));
 		}
 		else System.out.println(cEngine.formatAsError("Group Key Map retrieval failed"));
 
 		return false;
 	}
 	public String getFromattedMapString()
 	{
 		return groupFileKeyMap.toString();
 	}
 }
