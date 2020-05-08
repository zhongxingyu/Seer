 package minimud_server;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 public class DatabaseConnector
 {
 
     public enum ErrorCode
     {
         Success,
         Exception,
         InsertFailed
     }
     
     public enum SortBy
     {
         Gold,
         XP,
         User
     }
     
     private Connection m_conn;
     private static String m_strUrl = "jdbc:mysql://";
     private static String m_strDbName = "minimud";
     private static String m_strDriver = "com.mysql.jdbc.Driver";
     private static String m_strUserName = "";
     private static String m_strPassword = "";
     private static MMLogger m_logger = null;
     
     public DatabaseConnector(String strServer, int nPort, String strUser, String strPassword, MMLogger logger)
     {
         m_strUrl += strServer + ":";
         m_strUrl += nPort + "/";
         m_strUserName = strUser;
         m_strPassword = strPassword;
         
         m_logger = logger;
     }
     
     protected synchronized Connection getConnection()
     {
         return m_conn;
     }
     
     public synchronized ErrorCode connect()
     {        
         ErrorCode retVal = ErrorCode.Success;
         
         try        
         {
             m_logger.info("Connecting to database: " + m_strUrl + m_strDbName);
             
             Class.forName(m_strDriver).newInstance();
             m_conn = DriverManager.getConnection(m_strUrl + m_strDbName, m_strUserName, m_strPassword);
             
             m_logger.info("Connection successful!");
         }        
         catch (Exception e)        
         {
             m_logger.severe("Exception connecting to the database " + e);
             retVal = ErrorCode.Exception;
         }
         
         return retVal;
     }
     
     public synchronized ErrorCode disconnect()
     {
         ErrorCode retVal = ErrorCode.Success;
         
         try
         {
             m_logger.info("Disconnecting from database.");
             
             if (null != m_conn && m_conn.isClosed())
             {
                 m_conn.close();
             }
             
             m_logger.info("Disconnection successful!");
         }
         catch (Exception e)
         {
             m_logger.severe("Exception disconnecting from the database " + e);
             retVal = ErrorCode.Exception;
         }
         
         return retVal;
     }
     
     public synchronized boolean checkDuplicateUser(String strUserName)
     {
         boolean retVal = false;
         
         try
         {
             PreparedStatement pstmt = getConnection().prepareStatement("select username from characters where username = ?");
             pstmt.setString(1, strUserName);
             
             ResultSet results = pstmt.executeQuery();
             
             if (results.first())
             {
                 retVal = true;
             }
         }
         catch (Exception e)
         {
             m_logger.severe("Exception in DatabaseConnector::checkDuplicateUser() " + e);
             retVal = true;
         }
         
         return retVal;
     }
     
     public synchronized UserRecord lookupUserRecord(String strUserName)
     {
         UserRecord userRec = null;
         
         try
         {
             PreparedStatement pstmt = getConnection().prepareStatement("select * from characters where username = ?");
             pstmt.setString(1, strUserName);
             
             ResultSet results = pstmt.executeQuery();
             
             if (results.first())
             {
                 userRec = new UserRecord();
                 
                 userRec.setPasswordHash(results.getBytes("pwd_hash"));
                 userRec.setPasswordSalt(results.getBytes("pwd_salt"));
                 userRec.setUsername(strUserName);
             }
         }
         catch (Exception e)
         {
             m_logger.severe("Exception in DatabaseConnector::lookupUserRecord() " + e);
             userRec = null;
         }
         
         return userRec;
     }
     
     public synchronized ErrorCode createNewUser(String strUserName, byte pwdHash[], byte pwdSalt[], int nCharType)
     {
         ErrorCode retVal = ErrorCode.Success;
         
         try
         {
             PreparedStatement pstmt = getConnection().prepareStatement("insert into characters values(?, ?, ?, ?, "
                     + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
             pstmt.setString(1, strUserName);
             pstmt.setBytes(2, pwdHash);
             pstmt.setBytes(3, pwdSalt);
             
             Calendar cal = Calendar.getInstance();
             java.util.Date date = cal.getTime();
             
             java.sql.Date curDate = new java.sql.Date(date.getTime());
             
             pstmt.setDate(4, curDate);
             
             pstmt.setString(5, "");  // Description
             pstmt.setInt(6, nCharType);  // Char Type
             pstmt.setInt(7, 0);    // XP
             pstmt.setInt(8, 0);     // Gold
             pstmt.setInt(9, 100);    // Health
             pstmt.setInt(10, 100);  // Max Halth
             pstmt.setInt(11, 10);    // attack power
             pstmt.setInt(12, 10);    // magic power
             pstmt.setInt(13, 10);    // defense
             pstmt.setInt(14, 5);    // magic_defense
             pstmt.setInt(15, 1);  // last_room
             
             if (0 == pstmt.executeUpdate())
             {
                 retVal = ErrorCode.InsertFailed;
             }
         }
         catch (Exception e)
         {
             m_logger.severe("Exception in DatabaseConnector::checkDuplicateUser() " + e);
             retVal = ErrorCode.Exception;
         }
         
         return retVal;
     }
     
     public synchronized UserInfo loadUserInfo(String strUsername)
     {
         UserInfo userInfo = null;
         
         try
         {
             PreparedStatement pstmt = getConnection().prepareStatement("select * from characters where username = ?");
             pstmt.setString(1, strUsername);
             
             ResultSet results = pstmt.executeQuery();
             
             if (results.first())
             {
                 userInfo = new UserInfo();
                 
                 userInfo.setName(results.getString("username"));
                 userInfo.setDescription(results.getString("description"));
                 userInfo.setCharType(results.getInt("char_type"));
                 userInfo.setGold(results.getInt("gold"));
                 userInfo.setXP(results.getInt("xp"));
                 
                 userInfo.setMaxHealth(results.getInt("max_health"));
                 userInfo.setHealth(results.getInt("health"));                
                 userInfo.setAttackPower(results.getInt("attack_power"));
                 userInfo.setMagicPower(results.getInt("magic_power"));
                 userInfo.setDefense(results.getInt("defense"));
                 userInfo.setMagicDefense(results.getInt("magic_defense"));
                 userInfo.setLastRoom(results.getInt("last_room"));
                 
                 if(!userInfo.isValid())
                 {
                     m_logger.severe("Invalid UserInfo object.");
                     userInfo = null;
                 }
             }
         }
         catch (Exception e)
         {
             m_logger.severe("Exception in DatabaseConnector::loadUserInfo() " + e);
             userInfo = null;
         }
         
         return userInfo;
     }
     
     public synchronized ErrorCode saveUserState(UserConnectionThread user)
     {
         ErrorCode retVal = ErrorCode.Success;
         
         try
         {
             PreparedStatement pstmt = getConnection().prepareStatement("update characters set gold=?, xp=?, health=?, last_room=?"
                     + " where username=?");
             
             pstmt.setInt(1, user.getUserInfo().getGold());
             pstmt.setInt(2, user.getUserInfo().getXP());
             pstmt.setInt(3, user.getUserInfo().getHealth());
             pstmt.setInt(4, user.getUserInfo().getLastRoom());
             pstmt.setString(5, user.getUserInfo().getName());
             
             if (0 == pstmt.executeUpdate())
             {
                 retVal = ErrorCode.InsertFailed;
             }
         }
         catch (Exception e)
         {
             m_logger.severe("Exception in DatabaseConnector::saveUserState() " + e);
             retVal = ErrorCode.Exception;
         }
         
         return retVal;
     }
 
     // Retrieves list of rooms.  Return value is null if there is an error.
     public synchronized ResultSet getRooms()
     {
         ResultSet results = null;
         
         try
         {
             PreparedStatement pstmt = getConnection().prepareStatement("select * from rooms");
             
             results = pstmt.executeQuery();
         }
         catch (Exception e)
         {
             m_logger.severe("Exception in DatabaseConnector::getRooms() " + e);
             results = null;
         }
         
         return results;
     }
 
     // Retrieves list of moves.  Return value is null if there is an error.
     public synchronized ResultSet getMovesForRoom(int nRoomID)
     {
         ResultSet results = null;
         
         try
         {
             PreparedStatement pstmt = getConnection().prepareStatement("select * from moves where RoomID = ?");
             pstmt.setInt(1, nRoomID);
             
             results = pstmt.executeQuery();
             
         }
         catch (Exception e)
         {
             m_logger.severe("Exception in DatabaseConnector::getMoves() " + e);
             results = null;
         }
         
         return results;
     }
 
     // Retrieves list of NPCs.  Return value is null if there is an error.
     public synchronized ResultSet getNPCsForRoom(int nRoomID)
     {
         ResultSet results = null;
         
         try
         {
             PreparedStatement pstmt = getConnection().prepareStatement("select * from npcs where room = ?");
             pstmt.setInt(1, nRoomID);
             
             results = pstmt.executeQuery();
             
         }
         catch (Exception e)
         {
             m_logger.severe("Exception in DatabaseConnector::getNPCsForRoom() " + e);
             results = null;
         }
         
         return results;
     }
 
     // Retrieves list of Objects.  Return value is null if there is an error.
     public synchronized ResultSet getObjectsForRoom(int nRoomID)
     {
         ResultSet results = null;
         
         try
         {
             PreparedStatement pstmt = getConnection().prepareStatement("select * from objects where room = ?");
             pstmt.setInt(1, nRoomID);
             
             results = pstmt.executeQuery();
             
         }
         catch (Exception e)
         {
             m_logger.severe("Exception in DatabaseConnector::getObjectsForRoom() " + e);
             results = null;
         }
         
         return results;
     }
 
     // Retrieves list of monsters for a room.  Return value is null if there is an error.
     public synchronized ResultSet getMonstersForRoom(int nRoomID)
     {
         ResultSet results = null;
         
         try
         {
             PreparedStatement pstmt = getConnection().prepareStatement("select monsters.* from monsters "
                     + "left join monster_locs on monsters.ID = monster_locs.monster_id where monster_locs.room_id = ?");
             
             pstmt.setInt(1, nRoomID);
             
             results = pstmt.executeQuery();
             
         }
         catch (Exception e)
         {
             m_logger.severe("Exception in DatabaseConnector::getMonstersForRoom() " + e);
             results = null;
         }
         
         return results;
     }
 
     // Retrieves list of Actions for an object.  Return value is null if there is an error.
     public synchronized ResultSet getActionsForObject(GameObject obj)
     {
         ResultSet results = null;
         
         try
         {
             PreparedStatement pstmt = getConnection().prepareStatement("select * from actions where parent = ?");
             pstmt.setInt(1, obj.getID());
             
             results = pstmt.executeQuery();
             
         }
         catch (Exception e)
         {
             m_logger.severe("Exception in DatabaseConnector::getActionsForNPC() " + e);
             results = null;
         }
         
         return results;
     }
 
     // Retrieves the result of an action.  Return value is null if there is an error.
     public synchronized ArrayList<ActionResult> getActionResults(int parentID)
     {
         ArrayList<ActionResult> actionResults = new ArrayList<ActionResult>();
         
         try
         {
             PreparedStatement pstmt = getConnection().prepareStatement("select * from action_results where parent = ?");
             pstmt.setInt(1, parentID);
             
             ResultSet results = pstmt.executeQuery();
             
             if (null != results)
             {
                 while (results.next())
                 {
                     ActionResult actionRes = new ActionResult();
                     
                     actionRes.setID(results.getInt("ID"));
                     actionRes.setType(results.getString("type"));
                     actionRes.setDescription(results.getString("description"));
                     actionRes.setItemID(results.getInt("ItemID"));
                     actionRes.setValue(results.getInt("Value"));
                     
                     if(actionRes.isValid())
                         actionResults.add(actionRes);
                     else
                     {
                         m_logger.severe("Invalid ActionResult object.");                        
                     }
                 }
             }
         }
         catch (Exception e)
         {
             m_logger.severe("Exception in DatabaseConnector::getActionResults() " + e);
             actionResults = null;
         }
         
         return actionResults;
     }
 
     // Retrieves an item.  Return value is null if there is an error.
     public synchronized Item getItem(int nID)
     {
         Item item = null;
         
         try
         {
             PreparedStatement pstmt = getConnection().prepareStatement("select * from items where ID = ?");
             pstmt.setInt(1, nID);
             
             ResultSet results = pstmt.executeQuery();
             
             if (null != results && results.next())
             {
                 item = new Item();
                 
                 item.setID(results.getInt("ID"));
                 item.setName(results.getString("name"));
                 item.setDescription(results.getString("description"));                
                 item.setIsWeapon(1 == results.getInt("weapon"));
                 item.setIsStackable((1 == results.getInt("stackable")));
                 
                 String strDamageType = results.getString("damage_type");
                 
                 if (null != strDamageType)
                 {
                     item.setDamageType(strDamageType);
                 }
                 
                 item.setDamage(results.getInt("damage"));
                 item.setReqRoomID(results.getInt("req_room_id"));
                 item.setValue(results.getInt("value"));
                 
                 if(!item.isValid())
                 {
                     m_logger.severe("Invalid Item object.");
                     item = null;
                 }
             }
             
         }
         catch (Exception e)
         {
             m_logger.severe("Exception in DatabaseConnector::getItem() " + e);
             item = null;
         }
         
         return item;
     }
 
     // Retrieves list of Items for a user.  Return value is null if there is an error.
     public synchronized ResultSet getItemsForUser(String strUser)
     {
         ResultSet results = null;
         
         try
         {
             PreparedStatement pstmt = getConnection().prepareStatement("select * from inventory where username = ?");
             pstmt.setString(1, strUser);
             
             results = pstmt.executeQuery();
         }
         catch (Exception e)
         {
             m_logger.severe("Exception in DatabaseConnector::getItemsForUser() " + e);
             results = null;
         }
         
         return results;
     }
     
     public synchronized Item getItemForUser(String strUserName, String strItemName)
     {
         Item item = null;
         
         try
         {
             PreparedStatement pstmt = getConnection().prepareStatement("select items.* from items left join inventory "
                     + "on inventory.ItemID = items.ID where items.name = ? and inventory.username = ?");
             
             pstmt.setString(1, strItemName);
             pstmt.setString(2, strUserName);
             
             ResultSet results = pstmt.executeQuery();
             
             if (null != results && results.next())
             {
                 item = new Item();
                 
                 item.setID(results.getInt("ID"));
                 item.setName(results.getString("name"));
                 item.setDescription(results.getString("description"));
                 item.setIsWeapon(1 == results.getInt("weapon"));
                 item.setIsStackable((1 == results.getInt("stackable")));
                 
                 if(null != results.getString("damage_type"))
                     item.setDamageType(results.getString("damage_type"));
                 
                 if(null != results.getString("damage_type"))
                     item.setDamage(results.getInt("damage"));
                 
                 item.setEffect(results.getString("effect"));
                 item.setReqRoomID(results.getInt("req_room_id"));
                 item.setValue(results.getString("value"));
                 
                 if(!item.isValid())
                 {
                     m_logger.severe("Invalid Item object.");
                     item = null;
                 }
             }
         }
         catch (Exception e)
         {
             m_logger.severe("Exception in DatabaseConnector::getItemForUser() " + e);
             item = null;
         }
         
         return item;
     }
     
     public synchronized ErrorCode addItemToInventory(Item item, String strUserName)
     {
         ErrorCode retVal = ErrorCode.Success;
         boolean bAddNew = true;
         
         try
         {            
             if(item.getIsStackable())
             {
                 // Load the inventory record for the item
                 PreparedStatement pstmt = getConnection().prepareStatement("select * from inventory where "
                         + "ItemID=? and username=?");
                 
                 pstmt.setInt(1, item.getID());
                 pstmt.setString(2, strUserName);
                 
                 ResultSet results = pstmt.executeQuery();
                 
                 // If the item exists, then update the record
                 if(null != results && results.next())
                 {
                     PreparedStatement pstmt2 = getConnection().prepareStatement("update inventory set count=? where ItemID=? and username=?");
                 
                     pstmt2.setInt(1, results.getInt("count") + 1);
                     pstmt2.setInt(2, item.getID());
                     pstmt2.setString(3, strUserName);
                     
                     if (0 == pstmt2.executeUpdate())
                     {
                         retVal = ErrorCode.InsertFailed;
                         m_logger.severe("Failed to insert item into inventory.");
                     }
                     else
                     {
                         m_logger.info("Item " + item.getID() + "added to the inventory of " + strUserName + ".");
                     }
                     
                     bAddNew = false;
                 }
             }
             
             if(bAddNew)
             {
                 PreparedStatement pstmt = getConnection().prepareStatement("insert into inventory values(?,?,1)");
                 pstmt.setInt(1, item.getID());
                 pstmt.setString(2, strUserName);
 
                 if (0 == pstmt.executeUpdate())
                 {
                     retVal = ErrorCode.InsertFailed;
                     m_logger.severe("Failed to insert item into inventory.");
                 }
                 else
                 {
                     m_logger.info("Item " + item.getID() + "added to the inventory of " + strUserName + ".");
                 }
             }
         }
         catch (Exception e)
         {
             m_logger.severe("Exception in DatabaseConnector.addItemToInventory() " + e);
             retVal = ErrorCode.Exception;
         }
         
         return retVal;
     }
     
     public synchronized ErrorCode removeItemFromInventory(Item item, String strUserName)
     {
         ErrorCode retVal = ErrorCode.Success;
         boolean bRemove = true;
         
         try
         {            
             if(item.getIsStackable())
             {
                 // Load the inventory record for the item
                 PreparedStatement pstmt = getConnection().prepareStatement("select * from inventory where "
                         + "ItemID=? and username=?");
                 
                 pstmt.setInt(1, item.getID());
                 pstmt.setString(2, strUserName);
                 
                 ResultSet results = pstmt.executeQuery();
                 
                 // If the item exists and the count > 1, then update the record
                 if(null != results && results.next() && results.getInt("count") > 1)
                 {
                     PreparedStatement pstmt2 = getConnection().prepareStatement("update inventory set count=? where ItemID=? and username=?");
                 
                     pstmt2.setInt(1, results.getInt("count") - 1);
                     pstmt2.setInt(2, item.getID());
                     pstmt2.setString(3, strUserName);
                     
                     if (0 == pstmt2.executeUpdate())
                     {
                         retVal = ErrorCode.InsertFailed;
                         m_logger.severe("Failed to delete item into inventory.");
                     }
                     else
                     {
                         m_logger.info("Item " + item.getID() + "removed to the inventory of " + strUserName + ".");
                     }
                     
                     bRemove = false;
                 }
             }
             
             if(bRemove)
             {
                 PreparedStatement pstmt = getConnection().prepareStatement("delete from inventory where ItemID=? and username=?");
                 pstmt.setInt(1, item.getID());
                 pstmt.setString(2, strUserName);
 
                 if (0 == pstmt.executeUpdate())
                 {
                     retVal = ErrorCode.InsertFailed;
                     m_logger.severe("Failed to delete item into inventory.");
                 }
                 else
                 {
                     m_logger.info("Item " + item.getID() + "removed to the inventory of " + strUserName + ".");
                 }
             }
         }
         catch (Exception e)
         {
             m_logger.severe("Exception in DatabaseConnector.addItemToInventory() " + e);
             retVal = ErrorCode.Exception;
         }
         
         return retVal;
     }
     
     public synchronized ErrorCode addQuestToUser(int nQuestID, String strUserName)
     {
         ErrorCode retVal = ErrorCode.Success;
         
         try
         {            
             PreparedStatement pstmt = getConnection().prepareStatement("insert into quest_status values(?,?,?,?)");
             pstmt.setString(1, strUserName);
             pstmt.setInt(2, nQuestID);
             pstmt.setInt(3, 1);
             pstmt.setInt(4, 0);
             
             if (0 == pstmt.executeUpdate())
             {
                 retVal = ErrorCode.InsertFailed;
                 m_logger.severe("Failed to insert quest into quest_status.");
             }
             else
             {
                 m_logger.info("Quest" + nQuestID + "added to the quest log of " + strUserName + ".");
             }
         }
         catch (Exception e)
         {
             m_logger.severe("Exception in DatabaseConnector.addQuestToUser() " + e);
             retVal = ErrorCode.Exception;
         }
         
         return retVal;
     }
     
     public synchronized boolean isUserOnQuestStep(String strUser, int nQuestID, int nQuestStep)
     {
         boolean bRet = false;
         
         try
         {
             PreparedStatement pstmt = getConnection().prepareStatement("select * from quest_status where "
                     + "username = ? && quest_id = ? and step = ?");
             pstmt.setString(1, strUser);
             pstmt.setInt(2, nQuestID);
             pstmt.setInt(3, nQuestStep);
             
             ResultSet results = pstmt.executeQuery();
             
             if (null != results && results.next())
             {
                 bRet = true;
             }
         }
         catch (Exception e)
         {
             m_logger.severe("Exception in DatabaseConnector::isUserOnQuestStep() " + e);
             bRet = false;
         }
         
         return bRet;
     }
     
     public synchronized ErrorCode updateUserQuestStep(int nQuestID, int nNewStep, String strUserName)
     {
         ErrorCode retVal = ErrorCode.Success;
         
         try
         {            
             PreparedStatement pstmt = getConnection().prepareStatement("update quest_status set step=?"
                     + " where quest_id=? and username=?");
             
             pstmt.setInt(1, nNewStep);
             pstmt.setInt(2, nQuestID);
             pstmt.setString(3, strUserName);            
             
             if (0 == pstmt.executeUpdate())
             {
                 retVal = ErrorCode.InsertFailed;
                 m_logger.severe("Failed to update quest_status.");
             }
             else
             {
                 m_logger.info("Quest " + nQuestID + " updated to step "
                         + nNewStep + " for user " + strUserName + ".");
             }
         }
         catch (Exception e)
         {
             m_logger.severe("Exception in DatabaseConnector.updateUserQuestStep() " + e);
             retVal = ErrorCode.Exception;
         }
         
         return retVal;
     }
 
     // Retrieves Quest object.  Return value is null if there is an error.
     public synchronized Quest getQuest(int nID)
     {
         Quest quest = null;
         
         try
         {
             PreparedStatement pstmt = getConnection().prepareStatement("select * from quests where ID = ?");
             pstmt.setInt(1, nID);
             
             ResultSet results = pstmt.executeQuery();
             
             if (null != results && results.next())
             {
                 quest = new Quest();
                 
                 quest.setID(results.getInt("ID"));
                 quest.setName(results.getString("name"));
                 
                 String strFC = results.getString("first_completion_user");
                 
                 if (null != strFC)
                 {
                     quest.setFirstCompleteUser(strFC);
                 }
                 
                 quest.setRewardGold(results.getInt("reward_gold"));
                 quest.setRewardXP(results.getInt("reward_xp"));
                 quest.setRewardItemID(results.getInt("reward_item"));
                 quest.setFirstBonus(results.getInt("first_bonus"));
                 
                 if(!quest.isValid())
                 {
                     m_logger.severe("Invalid Quest object.");
                     quest = null;
                 }
             }
         }
         catch (Exception e)
         {
             m_logger.severe("Exception in DatabaseConnector::getQuest() " + e);
             quest = null;
         }
         
         return quest;
     }
     
     public synchronized ErrorCode setQuestFirstCompletion(int nQuestID, String strUserName)
     {
         ErrorCode retVal = ErrorCode.Success;
         
         try
         {            
             PreparedStatement pstmt = getConnection().prepareStatement("update quests set first_completion_user=?"
                     + " where ID=?");
             
             pstmt.setString(1, strUserName);
             pstmt.setInt(2, nQuestID);            
             
             if (0 == pstmt.executeUpdate())
             {
                 retVal = ErrorCode.InsertFailed;
                 m_logger.severe("Failed to set quest first completion.");
             }
             else
             {
                 m_logger.info("Quest " + nQuestID + " first completed by " + strUserName + ".");
             }
         }
         catch (Exception e)
         {
             m_logger.severe("Exception in DatabaseConnector.setQuestFirstCompletion() " + e);
             retVal = ErrorCode.Exception;
         }
         
         return retVal;
     }
     
     public synchronized ErrorCode setUserQuestCompleted(int nQuestID, String strUserName)
     {
         ErrorCode retVal = ErrorCode.Success;
         
         try
         {            
             PreparedStatement pstmt = getConnection().prepareStatement("update quest_status set completed=1, step=0"
                     + " where quest_id=? and username=?");
             
             pstmt.setInt(1, nQuestID);
             pstmt.setString(2, strUserName);            
             
             if (0 == pstmt.executeUpdate())
             {
                 retVal = ErrorCode.InsertFailed;
                 m_logger.severe("Failed to set quest completed.");
             }
             else
             {
                 m_logger.info("Quest " + nQuestID + " completed by " + strUserName + ".");
             }
         }
         catch (Exception e)
         {
             m_logger.severe("Exception in DatabaseConnector.setUserQuestCompleted() " + e);
             retVal = ErrorCode.Exception;
         }
         
         return retVal;
     }
     
     public synchronized boolean userCompletedQuest(int nQuestID, String strUserName)
     {
         boolean bRet = false;
         
         try
         {
             PreparedStatement pstmt = getConnection().prepareStatement("select * from quest_status where "
                     + "username = ? && quest_id = ? and completed = 1");
             pstmt.setString(1, strUserName);
             pstmt.setInt(2, nQuestID);
             
             ResultSet results = pstmt.executeQuery();
             
             if (null != results && results.next())
             {
                 bRet = true;
             }
         }
         catch (Exception e)
         {
             m_logger.severe("Exception in DatabaseConnector::userCompletedQuest() " + e);
             bRet = false;
         }
         
         return bRet;
     }
     
     public QuestStep getQuestStep(int nQuestID, int nQuestStep)
     {
         QuestStep step = null;
         
         try
         {
             PreparedStatement pstmt = getConnection().prepareStatement("select * from quest_steps where quest_id = ?"
                     + " and step_number = ?");
             pstmt.setInt(1, nQuestID);
             pstmt.setInt(2, nQuestStep);
             
             ResultSet results = pstmt.executeQuery();
             
             if (null != results && results.next())
             {
                 step = new QuestStep();
                 
                 step.setID(results.getInt("quest_id"));
                 step.setStep(results.getInt("step_number"));
                 step.setDescription(results.getString("description"));
                 step.setHint(results.getString("hint"));
                 step.setRewardGold(results.getInt("reward_gold"));
                 step.setRewardXP(results.getInt("reward_xp"));
                 step.setRewardItemID(results.getInt("reward_item"));
                 
                 if(!step.isValid())
                 {
                     m_logger.severe("Invalid QuestStep object.");
                     step = null;
                 }
             }
         }
         catch (Exception e)
         {
             m_logger.severe("Exception in DatabaseConnector::getQuestStep() " + e);
             step = null;
         }
         
         return step;
     }
     
     public synchronized ArrayList<UserInfo> getSortedUserList(SortBy sortBy)
     {
         ArrayList<UserInfo> actionResults = new ArrayList<UserInfo>();
         
         try
         {
             String sqlString = "";
             
             if (SortBy.Gold == sortBy)
             {
                 sqlString = "select * from characters order by gold desc";
             }
             else if (SortBy.XP == sortBy)
             {
                 sqlString = "select * from characters order by xp desc";
             }
             else if (SortBy.User == sortBy)
             {
                 sqlString = "select * from characters order by username";
             }
             
             PreparedStatement pstmt = getConnection().prepareStatement(sqlString);
             
             ResultSet results = pstmt.executeQuery();
             
             if (null != results)
             {
                 while (results.next())
                 {
                     UserInfo info = new UserInfo();
                     
                     info.setName(results.getString("username"));
                     info.setGold(results.getInt("gold"));
                     info.setXP(results.getInt("xp"));
                     
                     if(info.isValid())
                         actionResults.add(info);  
                     else
                     {
                         m_logger.severe("Invalid UserInfo object");
                     }
                 }
             }
         }
         catch (Exception e)
         {
             m_logger.severe("Exception in DatabaseConnector::getSortedUserList() " + e);
             actionResults = null;
         }
         
         return actionResults;
     }
     
    public synchronized ArrayList<Item> getItemsFromLootTable(int nLootTableID, int nPercent)
     {
         ArrayList<Item> items = new ArrayList<Item>();
         
         try
         {
             PreparedStatement pstmt = getConnection().prepareStatement("select items.* from items left join loot_table on "
                     + "items.ID = loot_table.item_id where loot_table.table_id = ? and loot_table.drop_percent >= ?");
             pstmt.setInt(1, nLootTableID);
             pstmt.setInt(2, 100 - nPercent);
             
             ResultSet results = pstmt.executeQuery();
             
            while (null != results && results.next())
             {
                 Item item = new Item();
                 
                 item = new Item();
                 item.setID(results.getInt("ID"));
                 item.setName(results.getString("name"));
                 item.setDescription(results.getString("description"));
                 item.setIsWeapon(1 == results.getInt("weapon"));
                 item.setIsStackable((1 == results.getInt("stackable")));
                 item.setDamageType(results.getString("damage_type"));
                 item.setDamage(results.getInt("damage"));
                 item.setValue(results.getInt("value"));
                 item.setReqRoomID(results.getInt("req_room_id"));
                 
                 if(!item.isValid())
                 {
                     m_logger.severe("Invalid Item object.");
                     item = null;
                 }
                 else
                     items.add(item);
             }
         }
         catch (Exception e)
         {
             m_logger.severe("Exception in DatabaseConnector::getItemsForUser() " + e);
             items = null;
         }
         
         return items;
     }
     
     public ArrayList<QuestStep> getQuestLogForUser(String strUser)
     {
         ArrayList<QuestStep> questSteps = new ArrayList<QuestStep>();
         
         try
         {
             PreparedStatement pstmt = getConnection().prepareStatement("select quests.name, quest_steps.* from quests, "
                     + "quest_steps left join quest_status on quest_status.quest_id = quest_steps.quest_id and "
                     + "quest_status.step = quest_steps.step_number where quest_status.username = ? "
                     + "and quest_status.completed = 0 and quest_steps.quest_id = quests.ID");
             
             pstmt.setString(1, strUser);
             
             ResultSet results = pstmt.executeQuery();
             
             if (null != results)
             {
                 while (results.next())
                 {
                     QuestStep step = new QuestStep();
                     
                     step.setID(results.getInt("quest_id"));
                     step.setDescription(results.getString("description"));
                     step.setHint(results.getString("hint"));
                     step.setRewardGold(results.getInt("reward_gold"));
                     step.setRewardXP(results.getInt("reward_xp"));
                     step.setRewardItemID(results.getInt("reward_item"));
                     step.setQuestName(results.getString("name"));
                     
                     if(step.isValid())
                         questSteps.add(step);
                     else
                     {
                         m_logger.severe("Invalid QuestStep object.");
                     }
                 }
             }
         }
         catch (Exception e)
         {
             m_logger.severe("Exception in DatabaseConnector::getQuestLogForUser() " + e);
             questSteps = null;
         }
         
         return questSteps;
     }
 }
