 package ru.tulupov.nsuconnect.database.loader;
 
 
 import android.content.Context;
 import android.support.v4.content.AsyncTaskLoader;
 import android.util.Log;
 
 import com.j256.ormlite.stmt.PreparedQuery;
 import com.j256.ormlite.stmt.QueryBuilder;
 
 import java.sql.SQLException;
 import java.util.List;
 
 import ru.tulupov.nsuconnect.database.DatabaseContract;
 import ru.tulupov.nsuconnect.database.HelperFactory;
 import ru.tulupov.nsuconnect.model.Chat;
 import ru.tulupov.nsuconnect.model.Message;
 
 
 public class MessageLoader extends AsyncTaskLoader<List<Message>> {
     private static final String TAG = MessageLoader.class.getSimpleName();
     private Chat chat;
 
     public MessageLoader(Context context, Chat chat) {
         super(context);
         this.chat = chat;
     }
 
     @Override
     public List<Message> loadInBackground() {
         try {
             QueryBuilder<Message, Integer> queryBuilder = HelperFactory.getHelper().getMessageDao().queryBuilder();
             queryBuilder.where().eq(DatabaseContract.Message.CHAT, chat.getId());
             PreparedQuery<Message> preparedQuery = queryBuilder.prepare();
            List<Message> messages = HelperFactory.getHelper().getMessageDao().query(preparedQuery);
            for (Message m : messages) {
                HelperFactory.getHelper().getUserDao().refresh(m.getUser());
            }
            return messages;
         } catch (SQLException e) {
             Log.e(TAG, "Error", e);
         }
         return null;
     }
 
 
 }
