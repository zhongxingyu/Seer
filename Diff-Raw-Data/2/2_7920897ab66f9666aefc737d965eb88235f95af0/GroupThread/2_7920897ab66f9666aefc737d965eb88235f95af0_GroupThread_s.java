 /* This thread does all the work. It communicates with the client through Envelopes.
  * 
  */
 import java.lang.Thread;
 import java.net.Socket;
 import java.io.*;
 import java.util.*;
 
 public class GroupThread extends Thread 
 {
 	private final Socket socket;
 	private GroupServer my_gs;
 	
 	//These get spun off from GroupServer
 	public GroupThread(Socket _socket, GroupServer _gs)
 	{
 		socket = _socket;
 		my_gs = _gs;
 	}
 	
 	public void run()
 	{
 		boolean proceed = true;
 
 		try
 		{
 			//Announces connection and opens object streams
 			System.out.println("*** New connection from " + socket.getInetAddress() + ":" + socket.getPort() + "***");
 			final ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
 			final ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
 			
 			//handle messages from the input stream(ie. socket)
 			do
 			{
 				Envelope message = (Envelope)input.readObject();
 				System.out.println("Request received: " + message.getMessage());
 				Envelope response;
 				
 //--GET TOKEN---------------------------------------------------------------------------------------------------------
 				if(message.getMessage().equals("GET"))//Client wants a token
 				{
 					String username = (String)message.getObjContents().get(0); //Get the username
 					if(username == null || !my_gs.userList.checkUser(username))
 					{
 						response = new Envelope("FAIL");
 						response.addObject(null);
 						output.writeObject(response);
 					}
 					else
 					{
 						UserToken yourToken = createToken(username); //Create a token
 						
 						//Respond to the client. On error, the client will receive a null token
 						response = new Envelope("OK");
 						response.addObject(yourToken);
 						output.writeObject(response);
 					}
 				}
 //--CREATE USER-------------------------------------------------------------------------------------------------------
 				else if(message.getMessage().equals("CUSER")) //Client wants to create a user
 				{
 					if(message.getObjContents().size() < 2)
 					{
 						response = new Envelope("FAIL");
 					}
 					else
 					{
 						response = new Envelope("FAIL");
 						
 						if(message.getObjContents().get(0) != null)
 						{
 							if(message.getObjContents().get(1) != null)
 							{
 								String username = (String)message.getObjContents().get(0); //Extract the username
 								UserToken yourToken = (UserToken)message.getObjContents().get(1); //Extract the token
 								
 								//create the user if the username/token allow it
 								if(createUser(username, yourToken))
 								{
 									response = new Envelope("OK"); //Success
 								}
 							}
 						}
 					}
 					
 					output.writeObject(response);
 				}
 //--DELETE USER---------------------------------------------------------------------------------------------------------
 				else if(message.getMessage().equals("DUSER")) //Client wants to delete a user
 				{					
 					if(message.getObjContents().size() < 2)
 					{
 						response = new Envelope("FAIL");
 					}
 					else
 					{
 						response = new Envelope("FAIL");
 						
 						if(message.getObjContents().get(0) != null)
 						{
 							if(message.getObjContents().get(1) != null)
 							{
 								String username = (String)message.getObjContents().get(0); //Extract the username
 								UserToken yourToken = (UserToken)message.getObjContents().get(1); //Extract the token
 								
 								//create the user if the username/token allow it
 								if(deleteUser(username, yourToken))
 								{
 									response = new Envelope("OK"); //Success
 								}
 								else response = new Envelope("You could not delete the user");
 							}
 						}
 					}
 					
 					output.writeObject(response);
 				}
 //--CREATE GROUP---------------------------------------------------------------------------------------------------------
 				else if(message.getMessage().equals("CGROUP")) //Client wants to create a group
 				{
 					//if the message is too short, return failure
 					response = new Envelope("FAIL");
 					if(message.getObjContents().size() > 1)
 					{
 						//get the elements of the message
 						if(message.getObjContents().get(0) != null && message.getObjContents().get(1) != null)
 						{
 							String groupName = (String)message.getObjContents().get(0); //Extract the group name
 							UserToken yourToken = (UserToken)message.getObjContents().get(1); //Extract the token
 								
 							//create the group if the it doesn't already exist
 							if(createGroup(groupName, yourToken))
 							{
 								response = new Envelope("OK"); //Success
 							}
 						}
 					}
 					
 					output.writeObject(response);
 				}
 //--DELETE GROUP--------------------------------------------------------------------------------------------------------
 				else if(message.getMessage().equals("DGROUP")) //Client wants to delete a group
 				{
 					//if the message is too short, return failure
 					response = new Envelope("FAIL");
 					if(message.getObjContents().size() > 1)
 					{
 						//get the elements of the message
 						if(message.getObjContents().get(0) != null && message.getObjContents().get(1) != null)
 						{
 							String groupName = (String)message.getObjContents().get(0); //Extract the group name
 							UserToken yourToken = (UserToken)message.getObjContents().get(1); //Extract the token
 								
 							//create the group if the it doesn't already exist
 							if(deleteGroup(groupName, yourToken))
 							{
 								response = new Envelope("OK"); //Success
 							}
 							else response = new Envelope("You could not delete the group.");
 						}
 					}
 					
 					output.writeObject(response);
 				}
 //--LIST MEMBERS--------------------------------------------------------------------------------------------------------
 				else if(message.getMessage().equals("LMEMBERS")) //Client wants a list of members in a group
 				{
 					//if the message is too short, return failure
 					response = new Envelope("FAIL");
 					if(message.getObjContents().size() > 1)
 					{
 						//get the elements of the message
 						if(message.getObjContents().get(0) != null && message.getObjContents().get(1) != null)
 						{
 							String groupName = (String)message.getObjContents().get(0); //Extract the group name
 							UserToken yourToken = (UserToken)message.getObjContents().get(1); //Extract the token
 
 
 							ArrayList<String> users = listMembers(groupName, yourToken);
							if(users.size() > 0)
 							{
 								response = new Envelope("OK");
 								response.addObject(users);
 							}
 							else//no files exist
 							{
 								response = new Envelope("FAIL-NOUSERS");
 							}
 					
 						}
 					}					
 					output.writeObject(response);
 				}
 //--ADD TO GROUP--------------------------------------------------------------------------------------------------------
 				else if(message.getMessage().equals("AUSERTOGROUP")) //Client wants to add user to a group
 				{
 					//if the message is too short, return failure
 					response = new Envelope("FAIL");
 					if(message.getObjContents().size() > 2)
 					{
 						//get the elements of the message
 						if(message.getObjContents().get(0) != null && message.getObjContents().get(1) != null)
 						{
 							String userName = (String)message.getObjContents().get(0); //Extract the user name
 							String groupName = (String)message.getObjContents().get(1); //Extract the group name
 							UserToken yourToken = (UserToken)message.getObjContents().get(2); //Extract the token
 
 							//verify the owner
 							if(my_gs.groupList.getGroupOwners(groupName).contains(yourToken.getSubject()))
 							{
 								//create the group if the it doesn't already exist
 								if(addToGroup(userName, groupName, yourToken))
 								{
 									response = new Envelope("OK"); //Success
 								}
 							}
 					
 						}
 					}					
 					output.writeObject(response);
 				}
 //--REMOVE FROM GROUP----------------------------------------------------------------------------------------------------
 				else if(message.getMessage().equals("RUSERFROMGROUP")) //Client wants to remove user from a group
 				{
 					//if the message is too short, return failure
 					response = new Envelope("FAIL");
 					if(message.getObjContents().size() > 1)
 					{
 						//get the elements of the message
 						if(message.getObjContents().get(0) != null && message.getObjContents().get(1) != null)
 						{
 							String userName = (String)message.getObjContents().get(0); //Extract the user name
 							String groupName = (String)message.getObjContents().get(1); //Extract the group name
 							UserToken yourToken = (UserToken)message.getObjContents().get(2); //Extract the token
 
 							//verify the owner
 							if(my_gs.groupList.getGroupOwners(groupName).contains(yourToken.getSubject()))
 							{
 								//create the group if the it doesn't already exist
 								if(removeFromGroup(userName, groupName, yourToken))
 								{
 									response = new Envelope("OK"); //Success
 								}
 							}
 					
 						}
 					}					
 					output.writeObject(response);
 				}
 //--DISCONNECT----------------------------------------------------------------------------------------------------------
 				else if(message.getMessage().equals("DISCONNECT")) //Client wants to disconnect
 				{
 					socket.close(); //Close the socket
 					proceed = false; //End this communication loop
 				}
 				else
 				{
 					response = new Envelope("FAIL"); //Server does not understand client request
 					output.writeObject(response);
 				}
 			}while(proceed);	
 		}
 		catch(Exception e)
 		{
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 		}
 	}
 	
 	//Method to create tokens
 	private UserToken createToken(String username) 
 	{
 		//Check that user exists
 		if(my_gs.userList.checkUser(username))
 		{
 			//Issue a new token with server's name, user's name, and user's groups
 			UserToken yourToken = new UserToken(my_gs.name, username, my_gs.userList.getUserGroups(username));
 			return yourToken;
 		}
 		else
 		{
 			return null;
 		}
 	}
 	
 	
 	//Method to create a user
 	private boolean createUser(String username, UserToken yourToken)
 	{
 		String requester = yourToken.getSubject();
 		
 		//Check if requester exists
 		if(my_gs.userList.checkUser(requester))
 		{
 			//Get the user's groups
 			ArrayList<String> temp = my_gs.userList.getUserGroups(requester);
 			//requester needs to be an administrator
 			if(temp.contains("ADMIN"))
 			{
 				//Does user already exist?
 				if(my_gs.userList.checkUser(username))
 				{
 					return false; //User already exists
 				}
 				else
 				{
 					my_gs.userList.addUser(username);
 					return true;
 				}
 			}
 			else
 			{
 				return false; //requester not an administrator
 			}
 		}
 		else
 		{
 			return false; //requester does not exist
 		}
 	}
 	
 	//Method to delete a user
 	private boolean deleteUser(String username, UserToken yourToken)
 	{
 		String requester = yourToken.getSubject();
 		
 		//Does requester exist?
 		if(my_gs.userList.checkUser(requester))
 		{
 			ArrayList<String> temp = my_gs.userList.getUserGroups(requester);
 			//requester needs to be an administer
 			if(temp.contains("ADMIN"))
 			{
 				//Does user exist?
 				if(my_gs.userList.checkUser(username))
 				{
 					//User needs deleted from the groups they belong
 					ArrayList<String> deleteFromGroups = new ArrayList<String>();
 					
 					//This will produce a hard copy of the list of groups this user belongs
 					for(int index = 0; index < my_gs.userList.getUserGroups(username).size(); index++)
 					{
 						deleteFromGroups.add(my_gs.userList.getUserGroups(username).get(index));
 					}
 					
 					//Delete the user from the groups
 					//If user is the owner, removeMember will automatically delete group!
 					for(int index = 0; index < deleteFromGroups.size(); index++)
 					{
 //NOTE: removed due to compiling error... waht happened to this function, should we add it? - jmh 2/10
 						my_gs.groupList.removeMember(deleteFromGroups.get(index), username);
 					}
 					
 					//If groups are owned, they must be deleted
 					ArrayList<String> deleteOwnedGroup = new ArrayList<String>();
 					
 					//Make a hard copy of the user's ownership list
 					for(int index = 0; index < my_gs.userList.getUserOwnership(username).size(); index++)
 					{
 						deleteOwnedGroup.add(my_gs.userList.getUserOwnership(username).get(index));
 					}
 					
 					//Delete owned groups
 					for(int index = 0; index < deleteOwnedGroup.size(); index++)
 					{
 						//Use the delete group method. UserToken must be created for this action
 						deleteGroup(deleteOwnedGroup.get(index), new UserToken(my_gs.name, username, deleteOwnedGroup));
 					}
 					
 					//Delete the user from the user list
 					my_gs.userList.deleteUser(username);
 					
 					return true;	
 				}
 				else
 				{
 					return false; //User does not exist
 					
 				}
 			}
 			else
 			{
 				return false; //requester is not an administer
 			}
 		}
 		else
 		{
 			return false; //requester does not exist
 		}
 	}
 
 //----------------------------------------------------------------------------------------------------------------------
 //-- UTILITY FUNCITONS
 //----------------------------------------------------------------------------------------------------------------------
 	private boolean createGroup(String groupName, UserToken yourToken)
 	{
 		//Check if group exists
 		if(!my_gs.groupList.checkGroup(groupName))
 		{
 			my_gs.createGroup(groupName, yourToken.getSubject());
 			return true;
 		}
 		return false; //requester does not exist
 	}
 
 	private boolean deleteGroup(String groupName, UserToken yourToken)
 	{
 		//verify that the group exists, and that the user is an owner
 		if(my_gs.groupList.checkGroup(groupName) && my_gs.groupList.getGroupOwners(groupName).contains(yourToken.getSubject()))
 		{
 			my_gs.deleteGroup(groupName);
 			return true;
 		}
 		return false;
 	}
 
 	private ArrayList<String> listMembers(String groupName, UserToken yourToken)
 	{
 		ArrayList<String> members = null;
 		//verify that the group exists, and that the user is an owner
 		if(my_gs.groupList.checkGroup(groupName) && my_gs.groupList.getGroupOwners(groupName).contains(yourToken.getSubject()))
 		{
 			members = my_gs.groupList.getGroupMembers(groupName);
 		}
 		return members;
 	}
 
 	private boolean addToGroup(String userName, String groupName, UserToken yourToken)
 	{
 		//verify that the group exists, that the user is an owner, and that the user isnt already a member
 		if(my_gs.groupList.checkGroup(groupName) && my_gs.groupList.getGroupOwners(groupName).contains(yourToken.getSubject()) 
 			&& !my_gs.groupList.getGroupMembers(groupName).contains(userName))
 		{
 			my_gs.addUserToGroup(groupName, userName);
 		}
 		return false;
 	}
 
 	private boolean removeFromGroup(String userName, String groupName, UserToken yourToken)
 	{
 		//verify that the group exists, and that the user is an owner
 		if(my_gs.groupList.checkGroup(groupName) && my_gs.groupList.getGroupOwners(groupName).contains(yourToken.getSubject()))
 		{
 			my_gs.removeUserFromGroup(groupName, userName);
 		}
 		return false;
 	}
 
 	private boolean disconnect(String userName, UserToken yourToken)
 	{
 		return false;
 	}
 
 }
