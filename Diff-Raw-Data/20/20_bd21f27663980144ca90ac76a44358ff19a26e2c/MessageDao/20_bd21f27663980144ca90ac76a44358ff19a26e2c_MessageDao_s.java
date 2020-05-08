 package ru.tulupov.nsuconnect.database.dao;
 
 
 import com.j256.ormlite.dao.BaseDaoImpl;
 import com.j256.ormlite.support.ConnectionSource;
 
 import java.sql.SQLException;
 import java.util.List;
 
 import ru.tulupov.nsuconnect.model.Message;
 
 public class MessageDao extends BaseDaoImpl<Message, Integer> {
     public MessageDao(ConnectionSource connectionSource,
                         Class<Message> dataClass) throws SQLException {
         super(connectionSource, dataClass);
     }
 
     public List<Message> getAllRoles() throws SQLException {
        return this.queryForAll();
     }
 }
