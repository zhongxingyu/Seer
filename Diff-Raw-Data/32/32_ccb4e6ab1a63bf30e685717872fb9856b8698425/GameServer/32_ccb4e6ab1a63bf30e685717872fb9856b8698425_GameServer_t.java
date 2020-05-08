 package minimud_server;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.Timer;
 import minimud_shared.*;
 import java.security.SecureRandom;
 
 public class GameServer implements ActionListener
 {
 	public enum ErrorCode
 	{
 		Success,
 		Exception, 
 		ObjectCreationFailed,
 		MsgPostFailed
 	}
     
     private enum FightWinner
     {
         Invalid,
         Player,
         Monster
     }
     
     private static int GAME_SYNC_INTERVAL = (60*1000);
 	
 	private HashMap<String, UserConnectionThread> m_userMap = new HashMap<String, UserConnectionThread>();
 	private HashMap<Integer, Room> m_Rooms = new HashMap<Integer, Room>();
 	
 	private MMLogger m_logger = null;
 	
 	private DatabaseConnector m_dbConn = null;
 	
 	private Room m_startingRoom = null;
     
     private Timer m_syncTimer = new Timer(GAME_SYNC_INTERVAL, (ActionListener)this);
 	
 	public GameServer(DatabaseConnector dbConn, MMLogger logger)
 	{
 		m_logger = logger;
 		m_dbConn = dbConn;
         
         // Set up a timer to trigger game state refreshes
         m_syncTimer.setRepeats(true);
         m_syncTimer.start();
 	}
     
     // Action performed on an interval timer.  Do all actions required to update
     // the game world
     public void actionPerformed(ActionEvent ae) 
     {
         try
         {
             Iterator<Room> roomIter = m_Rooms.values().iterator();
             
             while(roomIter.hasNext())
             {
                 Room room = roomIter.next();
                 
                 // Tell room to refresh internal state.
                 room.refreshState();
             }
         }
         catch(Exception e)
         {
             
         }
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
 				
                 m_logger.info("Loading room " + newRoom.getID());
                 
 				// Make sure that the data fields are valid
 				if(newRoom.isValid())
 				{		
                     m_logger.info("Loading moves into room.");
 					retVal = loadMovesIntoRoom(newRoom);
 					
 					if(ErrorCode.Success != retVal)
 					{
 						m_logger.severe("Failed to load moves into room object.");
 						break;
 					}
 					
                     m_logger.info("Loading NPCs into room.");
 					retVal = loadNPCsIntoRoom(newRoom);
 					
 					if(ErrorCode.Success != retVal)
 					{
 						m_logger.severe("Failed to load NPCs into room object.");
 						break;
 					}
 					
                     m_logger.info("Loading objects into room.");
 					retVal = loadObjectsIntoRoom(newRoom);
 					
 					if(ErrorCode.Success != retVal)
 					{
 						m_logger.severe("Failed to load Objects into room object.");
 						break;
 					}
                     
                     m_logger.info("Loading monsters into room.");
                     retVal = loadMonstersIntoRoom(newRoom);
                     
                     if(ErrorCode.Success != retVal)
                     {
                         m_logger.severe("FAILED to load monsters into room object.");
                         
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
     
     private ErrorCode loadMonstersIntoRoom(Room room)
 	{
 		ErrorCode retVal = ErrorCode.Success;
 		
 		try
 		{
 			// Now, load all of the NPCs for the room
 			ResultSet objects = m_dbConn.getMonstersForRoom(room.getID());
 			
 			while(null != objects && objects.next())
 			{
 				Monster monster = new Monster();
 				
                 monster.setID(objects.getInt("ID"));
 				monster.setName(objects.getString("name"));
                 monster.setDescription(objects.getString("description"));
                 
                 //  Need to set Max Health first because setHealth() does bounds checking based
                 // on max health
                 monster.setMaxHealth(objects.getInt("max_health"));
                 monster.setHealth(objects.getInt("health"));             
                 monster.setAttackPower(objects.getInt("attack_power"));
                 monster.setMagicPower(objects.getInt("magic_power"));
                 monster.setDefense(objects.getInt("defense"));
                 monster.setMagicDefense(objects.getInt("magic_defense"));
                 monster.setLootTableID(objects.getInt("loot_table_id"));
                 monster.setKillGold(objects.getInt("kill_gold"));
                 monster.setKillXP(objects.getInt("kill_xp"));
                 monster.setRespawnTimer(objects.getInt("respawn_timer"));
                 monster.setUpdateQuestID(objects.getInt("update_quest_id"));
                 monster.setUpdateQuestStep(objects.getInt("update_quest_step"));
                 
 				// Only add the move if it is valid
 				if(monster.isValid())
 				{
 					room.addMonster(monster);													
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
 		user.setCurrentRoom(m_Rooms.get(user.getUserInfo().getLastRoom()));
 		
 		// Add the suer to the starting Room
 		user.getCurrentRoom().addUser(user, strName);
 	}
     
     public UserConnectionThread getUser(String strName)
     {
         return m_userMap.get(strName);
     }
 	
 	public void removeUser(UserConnectionThread user)
 	{
 		m_logger.info("Removing user " + user.getUserInfo().getName() + " from the game.");
 		
 		// Remove the user from their current room
 		user.getCurrentRoom().removeUser(user);
 		
 		// Remove the user from the game
 		m_userMap.remove(user.getUserInfo().getName());
 	}
 	
 	public synchronized ErrorCode processUserMessage(UserConnectionThread user, Message msg)
 	{
 		ErrorCode retVal = ErrorCode.Success;
 		
 		try
 		{							
 			// Process user chat message
 			if(MessageID.USER_CHAT == msg.getMessageId())
 			{
 				UserChatMessage userChat = (UserChatMessage)msg;
 				
 				userChat.setFromUser(user.getUserInfo().getName());
 				
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
                 Room curRoom = user.getCurrentRoom();
                 
                 Iterator<UserConnectionThread> userIter = curRoom.getUserList();
 			
                 while(userIter.hasNext())
                 {
                     UserConnectionThread recipient = userIter.next();
 
                     recipient.processGameServerCommand(new ClientShowTextMessage(msg.getFromUser(), msg.getMessage()));
                 }
             }
                 break;
                 
             case Who:
             {
                 Iterator<UserConnectionThread> userIter = m_userMap.values().iterator();
                 
                 if(null != userIter)
                 {
                     int colCount = 0;
                     String strOut = "";
 
                     sendUserText(user, "");
                     sendUserText(user, "");
                     sendUserText(user, "Characters Currently Playing:");
                     sendUserText(user, "________________________________");
                     
                     while(userIter.hasNext())
                     {
                         UserConnectionThread info = userIter.next();
                         
                         String strChar = padString(info.getUserInfo().getName(), 23);
                         strOut += strChar;
                         colCount++;
                         
                         if(3 == colCount || !userIter.hasNext())
                         {
                             sendUserText(user, strOut);
                             colCount = 0;
                             strOut = "";
                         }
                     }
                     sendUserText(user, "");
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
 				nextRoom.addUser(user, user.getUserInfo().getName());
 				
 				// Set the new room as the user's current room
 				user.setCurrentRoom(nextRoom);
                 
                 user.getUserInfo().setLastRoom(nextRoom.getID());
 				
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
            
         case Attack:
         {
             Room room = user.getCurrentRoom();
             
             boolean bValidMonster = false;
             boolean bValidWeapon = false;
             
             // See if we have a valid monster
             Monster monster = room.getMonster(msg.getObject());
             
             if(null != monster)
             {
                 bValidMonster = true;   
             }
             else       
             {
 				sendUserText(user, "You can't attack that.");
             }
             
             try
             {
                 ResultSet results = m_dbConn.getItemsForUser(user.getUserInfo().getName());
                 Item weapon = null;
                 
                 if(null != results)
                 {
                     while(results.next())
                     {
                         Item item = m_dbConn.getItem(results.getInt("ItemID"));
 
                         if(null != item)
                         {
                             if(0 == item.getName().compareTo(msg.getSubject()))
                             {
                                 if(item.getIsWeapon())
                                 {
                                     bValidWeapon = true;
                                     weapon = item;
                                     break;
                                 }
                                 else
                                 {
                                     sendUserText(user, "You can't attack with that.");
                                 }
                                     
                             }
                         }
                     }
                 }
                 else
                 {
                     sendUserText(user, "You can't attack with that.");
                 }
 
                 if(bValidMonster && bValidWeapon)
                 {
                     doFight(user, monster, weapon);
                 }
                 else
                     sendUserText(user, "You can't attack with that.");
             }
             catch(Exception e)
             {
                 m_logger.severe("Unhandled exception in GameServer.processActionMessage():  " + e);
 
             }
         }
             break;
            
 		case Look:
 			if(msg.getObject().isEmpty())
 			{
 				// Display the user's current room
 				user.displayCurrentRoom();
 			}
             else
             {
                 Room room = user.getCurrentRoom();
                 boolean bShowedSomething = false;
 			
                 // See if there is a valid NPC in the room
                 NPC npc = room.getNPC(msg.getObject());
                 
                 if(null != npc)
                 {
                     sendUserText(user, "You see " + npc.getDescription());
                     bShowedSomething = true;
                 }
                 
                 GameObject obj = room.getObject(msg.getObject());
                 
                 if(null != obj)
                 {
                     sendUserText(user, "You see " + obj.getDescription());
                     bShowedSomething = true;
                 }
                 
                 Monster monster = room.getMonster(msg.getObject());
                 
                 if(null != monster)
                 {
                     sendUserText(user, "You see " + monster.getDescription());
                     bShowedSomething = true;
                 }  
                 
                 if(!bShowedSomething)
                     sendUserText(user, "You don't see anything.");
             }
             break;
             
         // Display the items in the player's inventory
         case Inventory:
         {
             try
             {
                 ResultSet results = m_dbConn.getItemsForUser(user.getUserInfo().getName());
 
                 sendUserText(user, "Items in your inventory:");
                 sendUserText(user, "--------------------------------");
                 
                 while(results.next())
                 {
                     int nItemID = results.getInt("ItemID");
 
                     Item item = m_dbConn.getItem(nItemID);
                     
                     if(null != item)
                     {
                         String strOut = item.getName();
                         
                         if(item.getIsStackable())
                             strOut += " (" + results.getInt("count") + ")";
                         
                         sendUserText(user, strOut);
                     }
                 }
             }
             catch(Exception e)
             {
                 m_logger.severe("Failed to load inventory items for user: " + e);
             }
         }    
             break;
         
         // Show the quests in the player's quest log
         case QuestLog:
         {
 
         ArrayList<QuestStep> questSteps = m_dbConn.getQuestLogForUser(user.getUserInfo().getName());
             
             if(null != questSteps)
             {
                 sendUserText(user, "");
                 sendUserText(user, padString("Quest Name:", 30) + "Description:");
                 sendUserText(user, padString("--------------", 30) + "----------------");
                 
                 for(int i = 0; i < questSteps.size(); i++)
                 {
                     QuestStep step = questSteps.get(i);
                     
                     if(step.getDescription().length() <= 50)
                         sendUserText(user, padString(step.getQuestName(), 30) + step.getDescription());
                     else
                     {                      
                         i = 0;
                         do
                         {
                             String strName = (0 == i) ? step.getQuestName() : "";
                             
                             if(step.getDescription().length() > (i+1)*50)
                                 sendUserText(user, padString(strName, 30) + step.getDescription().substring(i*50, (i+1)*50));
                             else
                                 sendUserText(user, padString(strName, 30) + step.getDescription().substring(i*50));
                             
                             i++;
                         }
                         while(step.getDescription().length() > (i*50));
                     }
                     
                     sendUserText(user, "");
                 }
             }
         }
             break;
          
         case CharInfo:
         {
             user.displayInfo();
         }            
             break;
             
         case UseItem:
         {
             // Get object from inventory
             Item item = m_dbConn.getItemForUser(user.getUserInfo().getName(), msg.getObject());
                     
             if(null != item)
             {
                 if(0 != item.getReqRoomID()
                         && user.getCurrentRoom().getID() == item.getReqRoomID())
                 {
                     switch(item.getEffect())
                     {
                         case GiveHealth:
                             int nHealthGained = (int)(0.2 * user.getUserInfo().getMaxHealth());
                             user.getUserInfo().setHealth(user.getUserInfo().getHealth() + nHealthGained);
 
                             sendUserText(user, "You gained " + nHealthGained + " health!");
 
                             m_dbConn.saveUserState(user);
                             break;
 
                         case Teleport:
                             int nRoomID = item.getValue();
 
                             user.getCurrentRoom().removeUser(user);
 
                             Room room = m_Rooms.get(nRoomID);
 
                             if(null != room)
                             {
                                 room.addUser(user, user.getUserInfo().getName());
                                 user.setCurrentRoom(room);
                                 user.displayCurrentRoom();
                             }
                             break;
                     }
                     
                     // Remove item from inventory
                     if(item.getIsStackable())
                         m_dbConn.removeItemFromInventory(item, user.getUserInfo().getName());  
                 }
                 else
                 {
                     sendUserText(user, "You can't use that here.");
                 }
                 break;
             }
         }
         // If the item wasn't in inventory, then treat it as an action targeting
         // an NPC or room object.  DROP TO NEXT CASE
             
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
 			
             m_logger.info("Attempting action on " + msg.getObject());
             
 			if(null == npc)
 			{
                 m_logger.info("No NPC with that name.  Try an object.");
 				obj = room.getObject(msg.getObject());
                 
                 if(null == obj)
                     m_logger.info("That object doesn't exist.");
 			}
 			else
             {
                 m_logger.info("Taking action on NPC.");
 				obj = npc;
             }
 			
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
 					Action action = obj.getAction(m_dbConn, msg.getActionString(), user.getUserInfo().getName());
 					
 					if(null != action)
 					{
 						// There is a valid action, do it and get the result
 						
 						// Look up the ActionResult from the database
 						ArrayList<ActionResult> results = m_dbConn.getActionResults(action.getID());
 						
 						if(null != results)
 						{
                             int nRewardGold = 0;
 							int nRewardXP = 0;
                             int nRewardItemID = 0;
                                 
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
 										m_dbConn.addItemToInventory(item, user.getUserInfo().getName());
 										
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
                                 else if(0 == result.getType().compareTo("give_health"))
                                 {
                                     int nHealthGained = (int)(0.2 * user.getUserInfo().getMaxHealth());
                                     user.getUserInfo().setHealth(user.getUserInfo().getHealth() + nHealthGained);
 
                                     sendUserText(user, "You gained " + nHealthGained + " health!");
 
                                     m_dbConn.saveUserState(user);
                                 }
 								else if(0 == result.getType().compareTo("give_quest"))
 								{
 									int nQuestID = result.getValue();
 									
 									if(0 != nQuestID)
 									{
 										m_dbConn.addQuestToUser(nQuestID, user.getUserInfo().getName());
 										
 										sendUserText(user, result.getDescription());
 									}
 								}
                                 else if(0 == result.getType().compareTo("spawn_monster"))
                                 {
                                     Room curRoom = user.getCurrentRoom();
                                     
                                     if(null != curRoom)
                                     {
                                         Monster mon = curRoom.spawnMonster(result.getValue());
 
                                         sendRoomText(curRoom, "A " + mon.getName() + " has appeared.");
                                     }
                                 }
 								else if(0 == result.getType().compareTo("update_quest"))
 								{
 									int nQuestID = action.getQuestDependencyID();
 									int nNewStep = result.getValue();
 									
 									if(0 != nQuestID)
 									{
 										m_dbConn.updateUserQuestStep(nQuestID, nNewStep, user.getUserInfo().getName());
                                         
                                         QuestStep step = m_dbConn.getQuestStep(nQuestID, nNewStep);
                                         
                                         sendUserText(user, result.getDescription());
 										
 										sendUserText(user, step.getDescription());
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
                                             nRewardItemID = quest.getRewardItemID();                                                                                        
                                             
 											// If this is the first time the quest has been completed
 											if(quest.getFirstCompleteUser().isEmpty())
 											{
 												// Add first completion bonus to the rewards
 												nRewardGold += (nRewardGold * quest.getFirstBonus())/100;
 												
 												nRewardXP += (nRewardXP * quest.getFirstBonus())/100;
 
 												// Set the first completion user
 												m_dbConn.setQuestFirstCompletion(nQuestID, user.getUserInfo().getName());
 											}
 											
 											m_dbConn.setUserQuestCompleted(nQuestID, user.getUserInfo().getName());
 											                                                                                    
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
                             
                             if(0 != nRewardItemID)
                             {
                                 Item item = m_dbConn.getItem(nRewardItemID);
 
                                 if(null != item)
                                 {
                                     sendUserText(user, "You receive a " + item.getName() + "!");
 
                                     m_dbConn.addItemToInventory(item, user.getUserInfo().getName());
                                 }
                             }
 
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
     
     public synchronized FightWinner doFight(UserConnectionThread user, Monster monster, Item weapon)
     {
         FightWinner winner = FightWinner.Invalid;
         
         // Can only attack if the monster is alive and nobody else is attacking it
         if(Monster.State.Open == monster.getState())
         {
             // Mark the monster as in combat
             monster.setState(Monster.State.InCombat);
             
             sendUserText(user, "You attack the " + monster.getName());
 
             winner = simulateFight(user, monster, weapon);
             
             // If the Player wins, give them their gold, xp, and loot
             if(FightWinner.Player == winner)
             {                  
                 // Mark the monster as dead
                 monster.setState(Monster.State.Dead);
                 
                 sendUserText(user, "You killed the " + monster.getName());
                 
                 user.getUserInfo().setGold( user.getUserInfo().getGold() + monster.getKillGold());
                 sendUserText(user, "You earned " + monster.getKillGold() + " gold!");
                 
                 user.getUserInfo().setXP( user.getUserInfo().getXP() + monster.getKillXP());
                 sendUserText(user, "You earned " + monster.getKillXP() + " XP!");
                 
                 //  Roll on loot in the loot table
                 ArrayList<Item> items = rollForLoot(monster.getLootTableID()); 
                 
                 if(null != items)
                 {
                     for(int i = 0; i < items.size(); i++)
                     {
                         Item item = items.get(i);
                         
                         // Add item to user's inventory
                         if(DatabaseConnector.ErrorCode.Success == m_dbConn.addItemToInventory(item, user.getUserInfo().getName()))
                             sendUserText(user, "You receive loot!  A " + item.getName() + " has been added to your inventory.");
                     }
                 }
                 
                 // If a quest needs to be updated, then udpate it
                 if(0 != monster.getUpdateQuestID() && 0 != monster.getUpdateQuestStep())
                 {
                     if(!m_dbConn.userCompletedQuest(monster.getUpdateQuestID(), user.getUserInfo().getName()))
                     {
                         DatabaseConnector.ErrorCode ret = m_dbConn.updateUserQuestStep(monster.getUpdateQuestID(), 
                                                                                     monster.getUpdateQuestStep(), 
                                                                                     user.getUserInfo().getName());
 
                         if(DatabaseConnector.ErrorCode.Success == ret)
                         {
                             // Display the new quest step text
                             QuestStep step = m_dbConn.getQuestStep(monster.getUpdateQuestID(), monster.getUpdateQuestStep());					
                             sendUserText(user, step.getDescription());
                         }
                     }
                 }                       
             }
             // If the monster wins
             else
             {
                 sendUserText(user, "You have died!");
                 
                 // Reset the monster's state to open
                 monster.setState(Monster.State.Open);
                 
                 // Set user's health to half of max health
                 user.getUserInfo().setHealth(user.getUserInfo().getMaxHealth()/2);
                 
                 // Move the user to the Tavern
                 // Remove them from current room
                 Room curRoom = user.getCurrentRoom();
                 curRoom.removeUser(user);
                 
                 // Set the user's starting room
                 user.setCurrentRoom(getStartingRoom());
 
                 // Add the user to the starting Room
                 getStartingRoom().addUser(user, user.getUserInfo().getName());
                 
                 // Show the new room text
                 user.displayCurrentRoom();
             }
         }
         else
             sendUserText(user, "You can't attack the " + monster.getName() + " because it is not available.");
         
         return winner;
     }
     
     public FightWinner simulateFight(UserConnectionThread user, Monster monster, Item weapon)
     {
         FightWinner winner = FightWinner.Invalid;
         SecureRandom random = new SecureRandom();
         
         try
         {
             while(monster.getHealth() > 0 && user.getUserInfo().getHealth() > 0)                    
             {
                 // Player attacks Monster
                 int nPlayerDamage = (random.nextInt() % user.getUserInfo().getAttackPower())
                         + weapon.getDamage() - monster.getDefense();
                 
                 if(nPlayerDamage < 0)
                     nPlayerDamage = 0;
                 
                 monster.setHealth(monster.getHealth() - nPlayerDamage);                                
                 
                 sendUserText(user, "You do " + nPlayerDamage + " damage to the " + monster.getName() + ".");
                 
                 Thread.sleep(1000);
 
                 // If monster is still alive, monster attacks user
                 if(monster.getHealth() <= 0)
                 {
                     break;
                 }
                 else
                 {
                     int nMonsterDamage = (random.nextInt() % monster.getAttackPower()) 
                             - user.getUserInfo().getDefense();
                     
                     if(nMonsterDamage < 0)
                         nMonsterDamage = 0;
                     
                     user.getUserInfo().setHealth(user.getUserInfo().getHealth() - nMonsterDamage);
                     
                     sendUserText(user, "The " + monster.getName() + " does " + nMonsterDamage + " damage to you.");
                     
                     Thread.sleep(1000);
                 }
             }
             
             if(monster.getHealth() <= 0)
                 winner = FightWinner.Player;
             else
                 winner = FightWinner.Monster;
         }
         catch(InterruptedException e)
         {
             m_logger.info("Sleep interrupted, invalidating fight.");
             winner = FightWinner.Invalid;
         }
         
         return winner;
     }
     
     public ArrayList<Item> rollForLoot(int nLootTableID)
     {
         ArrayList<Item> items = null;
         
         java.util.Random rnd = new java.util.Random();
         
         int roll = rnd.nextInt() % 100;
         
         if(roll < 0)
             roll *= -1;
         
        items = m_dbConn.getItemsFromLootTable(nLootTableID, roll);
 
         
         return items;
     }
 	
 	public static void sendUserText(UserConnectionThread user, String strMsg)
 	{
 		ClientShowTextMessage clMsg = new ClientShowTextMessage("server", strMsg);
 		user.processGameServerCommand(clMsg);
 	}
     
     public static void sendRoomText(Room room, String strMsg)
     {
         Iterator<UserConnectionThread> users = room.getUserList();
         
         while(users.hasNext())
         {
             UserConnectionThread user = users.next();
             
             if(null != user)
                 sendUserText(user, strMsg);
         }
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
                         strOut = String.format(strFormat, info.getName(), info.getGold());
                     else
                         strOut = String.format(strFormat, info.getName(), info.getXP());
                         
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
