 package ru.tulupov.nsuconnect.database.dao;
 
 
 import com.j256.ormlite.dao.BaseDaoImpl;
 import com.j256.ormlite.stmt.QueryBuilder;
 import com.j256.ormlite.stmt.UpdateBuilder;
 import com.j256.ormlite.support.ConnectionSource;
 
 import java.sql.SQLException;
 import java.util.List;
 
 import ru.tulupov.nsuconnect.database.DatabaseConstants;
 import ru.tulupov.nsuconnect.database.DatabaseContract;
 import ru.tulupov.nsuconnect.model.Chat;
 import ru.tulupov.nsuconnect.model.Message;
 
 public class ChatDao extends BaseDaoImpl<Chat, Integer> {
     public ChatDao(ConnectionSource connectionSource,
                    Class<Chat> dataClass) throws SQLException {
         super(connectionSource, dataClass);
     }
 
     public List<Chat> getAllRoles() throws SQLException {
         return this.queryForAll();
     }
 
     public Chat getLast() throws SQLException {
         List<Chat> list = query(queryBuilder()
                 .orderBy(DatabaseContract.Chat.ID, false).limit(1).prepare());
         return list.isEmpty() ? null : list.get(0);
     }
 
 
     public Chat getChat(int id) throws SQLException {
 
         QueryBuilder<Chat, Integer> queryBuilder = queryBuilder();
         queryBuilder.where().eq(DatabaseContract.Chat.ID, id);
         List<Chat> list = query(queryBuilder.prepare());
         return list.isEmpty() ? null : list.get(0);
     }
 
    public void deactivateChat(int id) throws SQLException {
        UpdateBuilder<Chat, Integer> updateBuilder = updateBuilder();

        updateBuilder.where().eq(DatabaseContract.Chat.ID, id);
        updateBuilder.updateColumnValue(DatabaseContract.Chat.ACTIVE_FLAG, DatabaseConstants.BOOLEAN_FALSE);
 
        update(updateBuilder.prepare());
    }
 
     public void deactivateAllChats() throws SQLException {
         UpdateBuilder<Chat, Integer> updateBuilder = updateBuilder();
 
 
         updateBuilder.updateColumnValue(DatabaseContract.Chat.ACTIVE_FLAG, DatabaseConstants.BOOLEAN_FALSE);
 
         update(updateBuilder.prepare());
     }
 
     public void updateLastMessage(Chat chat, Message message) throws SQLException {
         UpdateBuilder<Chat, Integer> updateBuilder = updateBuilder();
 
         updateBuilder.where().eq(DatabaseContract.Chat.ID, chat.getId());
         updateBuilder.updateColumnValue(DatabaseContract.Chat.LAST_MESSAGE, message.getId());
 
         update(updateBuilder.prepare());
     }
 }
