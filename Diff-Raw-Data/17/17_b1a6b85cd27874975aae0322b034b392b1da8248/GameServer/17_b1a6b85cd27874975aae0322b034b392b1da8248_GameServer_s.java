 package minimud_server;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import minimud_shared.*;
 
 public class GameServer
 {
 	public enum ErrorCode
 	{
 		Success,
 		Exception, 
 		ObjectCreationFailed,
 		MsgPostFailed
 	}
 	
 	private HashMap<String, UserConnectionThread> m_userMap = new HashMap<String, UserConnectionThread>();
 	private HashMap<Integer, Room> m_Rooms = new HashMap<Integer, Room>();
 	
 	private MMLogger m_logger = null;
 	
 	private DatabaseConnector m_dbConn = null;
 	
 	private Room m_startingRoom = null;
 	
 	public GameServer(DatabaseConnector dbConn, MMLogger logger)
 	{
 		m_logger = logger;
 		m_dbConn = dbConn;
 	}
 	
 	private void setStartingRoom(Room startingRoom)
 	{
 		m_startingRoom = startingRoom;
 	}
 	
 	private Room getStartingRoom()
 	{
 		return m_startingRoom;
 	}
 	
 	// Loads game data from MySQL database into memory
 	public ErrorCode loadGameData()
 	{
 		ErrorCode retVal = ErrorCode.Success;
 		
 		try
 		{
 			// Load the Room data types
 			ResultSet rooms = m_dbConn.getRooms();
 
 			while(ErrorCode.Success == retVal && null != rooms && rooms.next())
 			{
 				Room newRoom = new Room();
 				
 				newRoom.setID(rooms.getInt("ID"));
 				newRoom.setName(rooms.getString("name"));
 				newRoom.setDescription(rooms.getString("description"));
 				
 				// Make sure that the data fields are valid
 				if(newRoom.isValid())
 				{		
 					retVal = loadMovesIntoRoom(newRoom);
 					
 					if(ErrorCode.Success != retVal)
 					{
 						m_logger.severe("Failed to load moves into room object.");
 						break;
 					}
 					
 					retVal = loadNPCsIntoRoom(newRoom);
 					
 					if(ErrorCode.Success != retVal)
 					{
 						m_logger.severe("Failed to load NPCs into room object.");
 						break;
 					}
 					
 					retVal = loadObjectsIntoRoom(newRoom);
 					
 					if(ErrorCode.Success != retVal)
 					{
 						m_logger.severe("Failed to load Objects into room object.");
 						break;
 					}
 					
 					// Add the room to the 
 					m_Rooms.put(new Integer(newRoom.getID()), newRoom);
 					
 					// If this is the starting room (always 1) then save it as
 					// the starting room
 					if(1 == newRoom.getID())
 					{
 						setStartingRoom(newRoom);
 					}
 				}
 				else
 				{
 					m_logger.severe("Failed to load room object.  Invalid field value.");
 					break;
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			m_logger.severe("Exception caught in GameServer.postUserMessage(): " + e);
 			retVal = ErrorCode.Exception;
 		}
 		
 		return retVal;
 	}
 	
 	private ErrorCode loadMovesIntoRoom(Room room)
 	{
 		ErrorCode retVal = ErrorCode.Success;
 		
 		try
 		{
 			// Now, load all of the moves for the room
 			ResultSet moves = m_dbConn.getMovesForRoom(room.getID());
 			
 			while(null != moves && moves.next())
 			{
 				Move newMove = new Move();
 				
 				newMove.setRoomID(moves.getInt("RoomID"));
 				newMove.setDirection(moves.getString("direction"));
 				newMove.setNextRoomID(moves.getInt("NextRoomID"));
 				newMove.setDescription(moves.getString("description"));
 				
 				// Only add the move if it is valid
 				if(newMove.isValid())
 				{
 					room.addMove(newMove);													
 				}
 				else
 					retVal = ErrorCode.ObjectCreationFailed;
 			}
 		}
 		catch(Exception e)
 		{
 			retVal = ErrorCode.Exception;		
 			m_logger.severe("Exception caught in GameServer.loadMovesIntoRoom()");
 		}
 		
 		return retVal;
 	}
 	
 	private ErrorCode loadNPCsIntoRoom(Room room)
 	{
 		ErrorCode retVal = ErrorCode.Success;
 		
 		try
 		{
 			// Now, load all of the NPCs for the room
 			ResultSet npcs = m_dbConn.getNPCsForRoom(room.getID());
 			
 			while(null != npcs && npcs.next())
 			{
 				NPC newNPC = new NPC();
 				
 				newNPC.setID(npcs.getInt("id"));
 				newNPC.setRoomID(npcs.getInt("room"));
 				newNPC.setName(npcs.getString("name"));
 				newNPC.setDescription(npcs.getString("description"));
 				newNPC.setIntro(npcs.getString("intro"));
 				
 				retVal = loadActionsIntoObject(newNPC);
 				
 				if(ErrorCode.Success != retVal)
 				{
 					m_logger.severe("Failed to load Actions into NPC object.");
 					break;
 				}
 				
 				// Only add the move if it is valid
 				if(newNPC.isValid())
 				{
 					room.addNPC(newNPC);													
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			retVal = ErrorCode.Exception;		
 			m_logger.severe("Exception caught in GameServer.loadNPCsIntoRoom()");
 		}
 		
 		return retVal;
 	}
 	
 	private ErrorCode loadObjectsIntoRoom(Room room)
 	{
 		ErrorCode retVal = ErrorCode.Success;
 		
 		try
 		{
 			// Now, load all of the NPCs for the room
 			ResultSet objects = m_dbConn.getObjectsForRoom(room.getID());
 			
 			while(null != objects && objects.next())
 			{
 				GameObject obj = new GameObject();
 				
 				obj.setID(objects.getInt("id"));
 				obj.setRoomID(objects.getInt("room"));
 				obj.setName(objects.getString("name"));
 				obj.setDescription(objects.getString("description"));
 				
 				retVal = loadActionsIntoObject(obj);
 				
 				if(ErrorCode.Success != retVal)
 				{
 					m_logger.severe("Failed to load Actions into Game object.");
 					break;
 				}
 				
 				// Only add the move if it is valid
 				if(obj.isValid())
 				{
 					room.addObject(obj);													
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			retVal = ErrorCode.Exception;		
 			m_logger.severe("Exception caught in GameServer.loadObjectsIntoRoom()");
 		}
 		
 		return retVal;
 	}
 	
 	private ErrorCode loadActionsIntoObject(GameObject obj)
 	{
 		ErrorCode retVal = ErrorCode.Success;
 		
 		try
 		{
 			// Now, load all of the NPCs for the room
 			ResultSet actions = m_dbConn.getActionsForObject(obj);
 			
 			while(null != actions && actions.next())
 			{
 				Action action = new Action();
 				
 				action.setID(actions.getInt("ID"));
 				action.setName(actions.getString("name"));
 				action.setResult(actions.getInt("result"));
 				action.setQuestDependencyID(actions.getInt("quest_dependency_id"));
 				action.setQuestDependencyStep(actions.getInt("quest_dependency_step"));
                 action.setQuestDependencyCompletion(actions.getInt("quest_dependency_complete"));
 				
 				// Only add the move if it is valid
 				if(action.isValid())
 				{
 					obj.addAction(action);													
 				}
 				else
 				{
 					retVal = ErrorCode.ObjectCreationFailed;
 					m_logger.severe("FAILED to load Action into object.");
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			retVal = ErrorCode.Exception;		
 			m_logger.severe("Exception caught in GameServer.loadActionsIntoObject()");
 		}
 		
 		return retVal;
 	}
 	
 	public void addUser(UserConnectionThread user, String strName)
 	{
 		// Add the user to the game
 		m_logger.info("Adding user " + strName + " to the game.");
 		m_userMap.put(strName, user);
 				
 		m_logger.info("Adding user " + strName + " to starting room " + 
 					  getStartingRoom().getName() + ".");
 		
 		// Set the starting room
 		user.setCurrentRoom(getStartingRoom());
 		
 		// Add the suer to the starting Room
 		getStartingRoom().addUser(user, strName);
 	}
 	
 	public void removeUser(UserConnectionThread user)
 	{
 		m_logger.info("Removing user " + user.getUserInfo().getUserName() + " from the game.");
 		
 		// Remove the user from their current room
 		user.getCurrentRoom().removeUser(user);
 		
 		// Remove the user from the game
 		m_userMap.remove(user.getUserInfo().getUserName());
 	}
 	
 	public synchronized ErrorCode processUserMessage(UserConnectionThread user, Message msg)
 	{
 		ErrorCode retVal = ErrorCode.Success;
 		
 		try
 		{		
 			m_logger.info("Received message from user (" + user.getUserInfo().getUserName() 
 						  + "): " + msg.serialize());
 			
 			// Process user chat message
 			if(MessageID.USER_CHAT == msg.getMessageId())
 			{
 				UserChatMessage userChat = (UserChatMessage)msg;
 				
 				userChat.setFromUser(user.getUserInfo().getUserName());
 				
 				// Method to handle different types of chat commands
 				retVal = processChatMessage(user, userChat);
 			}
 			else if(MessageID.MOVE == msg.getMessageId())
 			{
 				retVal = processMoveMessage(user, (PlayerMoveMessage)msg);
 			}
 			else if(MessageID.ACTION == msg.getMessageId())
 			{
 				retVal = processActionMessage(user, (PlayerActionMessage)msg);
 			}
 		}
 		catch(Exception e)
 		{
 			m_logger.severe("Exception caught in GameServer.postUserMessage(): " + e);
 			retVal = ErrorCode.Exception;
 		}
 		
 		return retVal;
 	}
 	
 	public ErrorCode processChatMessage(UserConnectionThread user, UserChatMessage msg)
 	{
 		ErrorCode retVal = ErrorCode.Success;
 		
         switch(msg.getMsgType())
         {
             case Tell:
             {
                 UserConnectionThread recipient = m_userMap.get(msg.getToUser());
 			
                 if(null != recipient)
                     recipient.processGameServerCommand(new ClientShowTextMessage(msg.getFromUser(), msg.getMessage()));
             }
                 break;
                 
             case Say:
             {
                 Iterator<UserConnectionThread> userIter = m_userMap.values().iterator();
 			
                 while(userIter.hasNext())
                 {
                     UserConnectionThread recipient = userIter.next();
 
                     recipient.processGameServerCommand(new ClientShowTextMessage("server", "Invalid chat message"));
                 }
             }
                 break;
                 
             case Who:
             {
                ArrayList<UserInfo> characters = m_dbConn.getSortedUserList(DatabaseConnector.SortBy.User);
                 
                if(null != characters)
                 {
                     int colCount = 0;
                     String strOut = "";
 
                     sendUserText(user, "Characters Currently Playing:");
                     sendUserText(user, "________________________________");
                     
                    for(int i = 0; i < characters.size(); i++)
                     {
                        UserInfo info = characters.get(i);
                         
                        String strChar = padString(info.getUserName(), 23);
                         strOut += strChar;
                         colCount++;
                         
                        if(3 == colCount || i >= (characters.size() - 1))
                         {
                             sendUserText(user, strOut);
                             colCount = 0;
                             strOut = "";
                         }
                     }
                 }
                 else
                 {
                     m_logger.severe("FAILED to load list of characters.");
                     retVal = ErrorCode.Exception;
                 }
             }
                 break;
         }
 		
 		return retVal;
 	}
     
     private String padString(String str, int nTotalLen)
     {
         String strOut = "";
         
         if(str.length() >= nTotalLen)
             strOut = str;
         else
         {
             strOut = str;
             
             for(int i = 0; i < (nTotalLen-str.length()); i++)
             {
                 strOut += " ";
             }
         }
         
         return strOut;
     }
 	
 	public ErrorCode processMoveMessage(UserConnectionThread user, PlayerMoveMessage msg)
 	{
 		ErrorCode retVal = ErrorCode.Success;
 		
 		// Get the user's current room
 		Room curRoom = user.getCurrentRoom();
 		
 		// Find the room the move goes to
 		int nNextRoom = curRoom.getNextRoomID(msg);
 		
 		// If there is no room in that direction, then tell user
 		if(-1 == nNextRoom)
 		{
 			ClientShowTextMessage clMsg = new ClientShowTextMessage("server", "There is nothing in that direction.");
 			
 			user.processGameServerCommand(clMsg);
 		}
 		// else handle move
 		else
 		{
 			// Get next room
 			Room nextRoom = m_Rooms.get(nNextRoom);
 			
 			if(null != nextRoom)
 			{
 				// Remove the user from the current room
 				curRoom.removeUser(user);
 				
 				// Add the user to the new room
 				nextRoom.addUser(user, user.getUserInfo().getUserName());
 				
 				// Set the new room as the user's current room
 				user.setCurrentRoom(nextRoom);
 				
 				// Have the user display the new room
 				user.displayCurrentRoom();
 			}
 		}	
 		
 		return retVal;
 	}
 	
 	public ErrorCode processActionMessage(UserConnectionThread user, PlayerActionMessage msg)
 	{
 		ErrorCode retVal = ErrorCode.Success;
 		
 		switch(msg.getAction())
 		{
         case Leaders:
         {
            boolean bLeadersGold = false;
            boolean bLeadersXP = false;
            
            if(0 == msg.getObject().compareTo("gold"))
            {
                bLeadersGold = true;
            }
            else if(0 == msg.getObject().compareTo("xp"))
            {
                bLeadersXP = true;
            }
            else
            {
                bLeadersGold = true;
                bLeadersXP = true;
            }
            
            if(bLeadersGold)
                 showLeaderBoard(user, true);
            
            if(bLeadersXP)
                showLeaderBoard(user, false);
            
            sendUserText(user, "");
         }
             break;
 		case Look:
 			if(msg.getObject().isEmpty())
 			{
 				// Display the user's current room
 				user.displayCurrentRoom();
 				break;
 			}
 
 		// If the Look action is aimed at an object or NPC, then
 		// We want to fall through to the next case.
 			
 		case Talk:	
 		case Punch:	
 		case Kick:	
 		case Stab:	
 		case Slash:	
 		case Push:
 		case Shoot:
 		case Take:
 		case Give:
 		{
 			Room room = user.getCurrentRoom();
 			
 			// See if there is a valid NPC in the room
 			NPC npc = room.getNPC(msg.getObject());
 			GameObject obj = null;
 			
 			if(null == npc)
 			{
 				obj = room.getObject(msg.getObject());
 			}
 			else
 				obj = npc;
 			
 			if(null != obj)
 			{
 				// If this is a Look message, then show the objects description
 				if(PlayerActionMessage.Action.Look == msg.getAction())
 				{
 					sendUserText(user, "You see " + obj.getDescription());
 				}
 				else
 				{
 					// Check to see if this NPC has an action matching the name and 
 					// any quest dependencies
 					Action action = obj.getAction(m_dbConn, msg.getActionString(), user.getUserInfo().getUserName());
 					
 					if(null != action)
 					{
 						// There is a valid action, do it and get the result
 						
 						// Look up the ActionResult from the database
 						ArrayList<ActionResult> results = m_dbConn.getActionResults(action.getID());
 						
 						if(null != results)
 						{
                             int nRewardGold = 0;
 							int nRewardXP = 0;
                                 
 							for(int i = 0; i < results.size(); i++)
 							{
 								ActionResult result = results.get(i);                                                                
 								
 								if(0 == result.getType().compareTo("text_only"))
 								{
 									sendUserText(user, result.getDescription());
 								}
 								else if(0 == result.getType().compareTo("item_reward")
 										&& (obj instanceof GameObject))
 								{
 									int nItemID = result.getItemID();
 									
 									Item item = m_dbConn.getItem(nItemID);
 									
 									if(null != item)
 									{
 										// Add the item to the user's inventory
 										m_dbConn.addItemToInventory(nItemID, user.getUserInfo().getUserName());
 										
 										sendUserText(user, "The " + item.getName() + " has been added to your inventory.");
 									}
 									else
 									{
 										m_logger.severe("Item " + nItemID + " coudld not be found.");
 										sendUserText(user, "Invalid command.");
 									}
 								}
 								else if(0 == result.getType().compareTo("xp_reward"))
 								{
 									nRewardXP += result.getValue();
                                     
 									sendUserText(user, "Congratulations.  You have earned " + result.getValue()
 												+ " XP!");
 								}
 								else if(0 == result.getType().compareTo("gold_reward"))
 								{
 									// Update user with rewards
                                     nRewardGold += result.getValue();
                                     
 									sendUserText(user, "Congratulations.  You have earned " + result.getValue()
 											+ " gold pieces!");
 								}
 								else if(0 == result.getType().compareTo("give_quest"))
 								{
 									int nQuestID = result.getValue();
 									
 									if(0 != nQuestID)
 									{
 										m_dbConn.addQuestToUser(nQuestID, user.getUserInfo().getUserName());
 										
 										sendUserText(user, result.getDescription());
 									}
 								}
 								else if(0 == result.getType().compareTo("update_quest"))
 								{
 									int nQuestID = action.getQuestDependencyID();
 									int nNewStep = result.getValue();
 									
 									if(0 != nQuestID)
 									{
 										m_dbConn.updateUserQuestStep(nQuestID, nNewStep, user.getUserInfo().getUserName());
 										
 										sendUserText(user, result.getDescription());
 									}
 								}
 								else if(0 == result.getType().compareTo("complete_quest"))
 								{
 									int nQuestID = action.getQuestDependencyID();
 									
 									if(0 != nQuestID)
 									{
 										// Get quest details
 										Quest quest = m_dbConn.getQuest(nQuestID);
 										
 										if(null != quest)
 										{
 											// Get the quest reward values
 											nRewardGold = quest.getRewardGold();
 											nRewardXP = quest.getRewardXP();
 											
 											// If this is the first time the quest has been completed
 											if(quest.getFirstCompleteUser().isEmpty())
 											{
 												// Add first completion bonus to the rewards
 												nRewardGold += (nRewardGold * quest.getFirstBonus())/100;
 												
 												nRewardXP += (nRewardXP * quest.getFirstBonus())/100;
 
 												// Set the first completion user
 												m_dbConn.setQuestFirstCompletion(nQuestID, user.getUserInfo().getUserName());
 											}
 											
 											m_dbConn.setUserQuestCompleted(nQuestID, user.getUserInfo().getUserName());
 											                                                                                    
 											// Congratulate the user
 											sendUserText(user, result.getDescription());
 											sendUserText(user, "You receive " + nRewardGold + " gold pieces.");
 											sendUserText(user, "You receive " + nRewardXP + " XP.");
 										}
 									}
 								}                                                                
 							}    
                             
                             // Update user with rewards
                             user.getUserInfo().setGold(user.getUserInfo().getGold() + nRewardGold);
                             user.getUserInfo().setXP(user.getUserInfo().getXP() + nRewardXP);
 
                             m_dbConn.saveUserState(user);
 						}
 						else
 						{
 							// There is no valid result.  Tell the user.
 							sendUserText(user, "Your action yields no result.");
 						}
 					}
 					else
 					{
 						// There is no valid action, do it and get the result
 						sendUserText(user, "You can't " + msg.getActionString() + " " + msg.getObject());
 					}
 				}
 			}	
 			else
 			{
 				// Tell user that the object doesn't exist
 				// There is a valid action, do it and get the result
 				sendUserText(user, "You can't do that.  " + msg.getObject() + " is not in the room.");
 			}
 			break;
 		}
 		default:
 			
 		}
 		
 		return retVal;
 	}
 	
 	private static void sendUserText(UserConnectionThread user, String strMsg)
 	{
 		ClientShowTextMessage clMsg = new ClientShowTextMessage("server", strMsg);
 		user.processGameServerCommand(clMsg);
 	}
     
     private ErrorCode showLeaderBoard (UserConnectionThread user, boolean bGold)
     {
         ErrorCode retVal = ErrorCode.Success;
         
         String strFormat = "%32s\t%d";
         
         try
         {
             ArrayList<UserInfo> users = m_dbConn.getSortedUserList(bGold ? DatabaseConnector.SortBy.Gold : DatabaseConnector.SortBy.XP);  
             
             if(null != users)
             {
                 sendUserText(user, "");
                 sendUserText(user, "");
                 
                 String strOut = "";
                 
                 if(bGold)
                 {
                     strOut = String.format("%34s", "Leaderboard by Gold:");
                     sendUserText(user, strOut);
                     
                     strOut = String.format("%32s\t%s", "Character", "Gold");
                     sendUserText(user, strOut);
                 }
                 else
                 {
                     strOut = String.format("%34s", "Leaderboard by Experience Points:");
                     sendUserText(user, strOut);
                     
                     strOut = String.format("%32s\t%s", "Character", "Experience Points");
                     sendUserText(user, strOut);
                 }
                 
                 strOut = String.format("%32s\t%s", "_________", "_________________");
                 sendUserText(user, strOut);
                 
                 for(int i = 0; i < users.size() && i < 10; i++)
                 {
                     UserInfo info = users.get(i);                                     
                     
                     if(bGold)
                         strOut = String.format(strFormat, info.getUserName(), info.getGold());
                     else
                         strOut = String.format(strFormat, info.getUserName(), info.getXP());
                         
                     sendUserText(user, strOut);
                 }
             }
         }
         catch(Exception e)
         {
             m_logger.severe("Exception caught in GameServer.sendUserText(): " + e);
             retVal = ErrorCode.Exception;
         }
         
         return retVal;
     }
 }
