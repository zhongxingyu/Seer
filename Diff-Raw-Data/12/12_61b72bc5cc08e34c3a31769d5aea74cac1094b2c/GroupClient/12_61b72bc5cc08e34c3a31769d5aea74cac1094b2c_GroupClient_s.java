 /* Implements the GroupClient Interface */
 
 import java.util.ArrayList;
 import java.util.List;
 import java.io.ObjectInputStream;
 import java.security.*;
 import javax.crypto.*;
 import java.io.*;
 
 public class GroupClient extends Client implements GroupClientInterface {
  
 	private AESKeySet aesKey;
 	private CryptoEngine cEngine;
 	
 	public boolean connect(final String server, final int port)
 	{
 		super.connect(server, port);
 		
 		cEngine = new CryptoEngine();	
 		boolean keyNeedsSet = true;
 		
 		setKey();
 		assert aesKey != null;
 		//i had this marked TODO, but i think its finished? -PHIL 3/6 18:06
 		return true;
 	}
 	
 	private boolean writeObject(ObjectOutputStream output, Object obj)
 	{
 		try
 		{
 			ByteArrayOutputStream toBytes = new ByteArrayOutputStream();//create ByteArrayOutputStream
 			ObjectOutputStream localOutput = new ObjectOutputStream(toBytes);//Make an object outputstream to that bytestream
 			localOutput.writeObject(obj);//write to the bytearrayoutputstream
 			byte[] data = toBytes.toByteArray();//turn our object into byte[]
 			
 			byte[] eData = cEngine.AESEncrypt(data, aesKey);//encrypt the data
 			output.writeObject(eData);//write the data to the client
 			toBytes.close();
 			localOutput.close();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	
 	//Method to read objects
 	private Object readObject(ObjectInputStream input)
 	{
 		Object obj = null;
 		try
 		{
 			byte[] eData = (byte[])input.readObject();
 			byte[] data = cEngine.AESDecrypt(eData, aesKey);
 			ByteArrayInputStream fromBytes = new ByteArrayInputStream(data);
 			ObjectInputStream localInput = new ObjectInputStream(fromBytes);
 			obj = localInput.readObject();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		return obj;
 	}
 	
 	private boolean setKey() 
 	{
 		try
 		{
 			Envelope message, response;
 			message = new Envelope("PUBKEYREQ");//requests the servers public key
 			aesKey = cEngine.genAESKeySet();
 			output.writeObject(message);
 			
 			response = (Envelope)input.readObject();
 			System.out.println(response.getMessage());
 			if(response.getMessage().equals("PUBKEYANSW"))
 			{
 				Key rsaPublic = (Key)response.getObjContents().get(0);
 				//encrypt the aesKey with the rsaPublic
 				
 				ByteArrayOutputStream toBytes = new ByteArrayOutputStream();//create ByteArrayOutputStream
 				ObjectOutputStream localOutput = new ObjectOutputStream(toBytes);//Make an object outputstream to that bytestream
 				
 				localOutput.writeObject(aesKey.getKey());//write to the bytearrayoutputstream
 				
 				byte[] aesKeyBytes = toBytes.toByteArray();
 				byte[] encryptedKey = cEngine.RSAEncrypt(aesKeyBytes, rsaPublic);
 				
 				message = new Envelope("AESKEY");
 				message.addObject(encryptedKey);
				message.addObject(aesKey.getIV());
 				
 				output.writeObject(message);
 				return true;
 			}
 			else return false;
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			System.exit(-1);
 		}
 		return false;
 	}
 	
 	public UserToken getToken(String username)
 	{
 		try
 		{
 			UserToken token = null;
 			Envelope message = null, response = null;	 	
 
 			//Tell the server to return a token.
 			message = new Envelope("GET");
 			message.addObject(username); //Add user name string
 			//output.writeObject(message);
 			writeObject(output, message);
 			
 			//Get the response from the server
 			response = (Envelope)input.readObject();
 			
 			//Successful response
 			if(response.getMessage().equals("OK"))
 			{
 				//If there is a token in the Envelope, return it 
 				ArrayList<Object> temp = null;
 				temp = response.getObjContents();
 				
 				if(temp.size() == 1)
 				{
 					token = (UserToken)temp.get(0);
 					return token;
 				}
 			}
 			
 			return null;
 		}
 		catch(Exception e)
 		{
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 			return null;
 		}
 		
 	 }
 	 
 	 public boolean createUser(String username, UserToken token)
 	 {
 		try
 		{
 			Envelope message = null, response = null;
 
 			//Tell the server to create a user
 			message = new Envelope("CUSER");
 			message.addObject(username); //Add user name string
 			message.addObject(token); //Add the requester's token
 			//output.writeObject(message);
 			writeObject(output, message);
 			
 			//response = (Envelope)input.readObject();
 			response = (Envelope)readObject(input);	
 			//If server indicates success, return true
 			if(response.getMessage().equals("OK"))
 			{
 				return true;
 			}
 				
 			return false;
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
 			message.addObject(username); //Add user name
 			message.addObject(token);  //Add requester's token
 			//output.writeObject(message);
 			writeObject(output, message);
 			
 			//response = (Envelope)input.readObject();
 			response = (Envelope)readObject(input);		
 			//If server indicates success, return true
 			if(response.getMessage().equals("OK"))
 			{
 				return true;
 			}
 				
 			return false;
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
 			message.addObject(groupname); //Add the group name string
 			message.addObject(token); //Add the requester's token
 			//output.writeObject(message);
 			writeObject(output, message);
 			
 			//response = (Envelope)input.readObject();
 			response = (Envelope)readObject(input);		
 			//If server indicates success, return true
 			if(response.getMessage().equals("OK"))
 			{
 				return true;
 			}
 				
 			return false;
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
 			message.addObject(groupname); //Add group name string
 			message.addObject(token); //Add requester's token
 			//output.writeObject(message);
 			writeObject(output, message);
 			
 			//response = (Envelope)input.readObject();
 			response = (Envelope)readObject(input);	
 			//If server indicates success, return true
 			if(response.getMessage().equals("OK"))
 			{
 				return true;
 			}
 			else System.out.println(response.getMessage());
 			return false;
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
 			message.addObject(group); //Add group name string
 			message.addObject(token); //Add requester's token
 			//output.writeObject(message);
 			writeObject(output, message);
 			
 			//response = (Envelope)input.readObject();
 			response = (Envelope)readObject(input);	
 			//If server indicates success, return the member list
 			if(response.getMessage().equals("OK"))
 			{ 
 				return (List<String>)response.getObjContents().get(0); //This cast creates compiler warnings. Sorry.
 			}
 				
 			return null; 
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
 			message.addObject(username); //Add user name string
 			message.addObject(groupname); //Add group name string
 			message.addObject(token); //Add requester's token
 			//output.writeObject(message);
 			writeObject(output, message);
 			
 			//response = (Envelope)input.readObject();
 			response = (Envelope)readObject(input);	
 			//If server indicates success, return true
 			
 			if(response.getMessage().equals("OK"))
 			{
 				return true;
 			}
 				
 			return false;
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
 			message.addObject(username); //Add user name string
 			message.addObject(groupname); //Add group name string
 			message.addObject(token); //Add requester's token
 			//output.writeObject(message);
 			writeObject(output, message);
 			
 			//response = (Envelope)input.readObject();
 			response = (Envelope)readObject(input);	
 			//If server indicates success, return true
 			if(response.getMessage().equals("OK"))
 			{
 				return true;
 			}
 			
 			return false;
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
 			//output.writeObject(message);
 			writeObject(output, message);
 			
 			//response = (Envelope)input.readObject();
 			response = (Envelope)readObject(input);	
 			if(response.getMessage().equals("OK") && response.getObjContents() != null)
 			{
 				return (ArrayList<String>)response.getObjContents().get(0);
 			}
 			return null;
 		}
 		catch(Exception e)
 		{
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 			return null;
 		}
 	}
 }
