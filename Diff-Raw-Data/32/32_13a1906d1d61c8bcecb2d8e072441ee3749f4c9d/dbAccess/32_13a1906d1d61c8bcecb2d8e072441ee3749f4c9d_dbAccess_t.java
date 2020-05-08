 package directi.androidteam.training.chatclient.Chat.dbAccess;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.CursorIndexOutOfBoundsException;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 import directi.androidteam.training.StanzaStore.MessageStanza;
 import directi.androidteam.training.db.DBManager;
 
 import java.util.Vector;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ssumit
  * Date: 10/2/12
  * Time: 6:46 PM
  * To change this template use File | Settings | File Templates.
  */
 public class dbAccess {
 
     public void addMessage(MessageStanza messageStanza) {
         ContentValues values = new ContentValues();
        values.put(DBManager.KEY_1_JID_SENDER,messageStanza.getFrom().split("/")[0]);
         values.put(DBManager.KEY_1_JID_RECEIVER,messageStanza.getTo());
         values.put(DBManager.KEY_1_MESSAGE,messageStanza.getBody());
         values.put(DBManager.KEY_1_ID,messageStanza.getID());
         values.put(DBManager.KEY_1_TIME, messageStanza.getTime());
         Log.d("dbdb", "msg : " + messageStanza.getBody());
         DBInsert(values);
         return;
     }
 
     private synchronized void DBInsert(ContentValues values) {
         SQLiteDatabase db = DBManager.getDbManager().getWritableSQLiteDB();
         db.insert(DBManager.TABLE_1_NAME, null, values);
     }
 
     public Vector<MessageStanza> getAllMsg() {
         SQLiteDatabase db = DBManager.getDbManager().getReadableSQLiteDB();
         Cursor cursor = db.query(DBManager.TABLE_1_NAME, null, null , null, null, null, null);
         Vector<MessageStanza> messageStanzas = new Vector<MessageStanza>();
         if (cursor==null) {
             Log.d("DBDB","cursur null");
             return messageStanzas;
         }
         cursor.moveToFirst();
         try {
              do {
                  String to = cursor.getString(1);
                  String body = cursor.getString(2);
                  MessageStanza messageStanza = new MessageStanza(to,body);
                  messageStanza.setFrom(cursor.getString(0));
                  messageStanza.setStatus(true);
                  messageStanza.setID(cursor.getString(3));
                  messageStanza.setTime(cursor.getLong(4));
                  messageStanzas.add(messageStanza);
              } while (cursor.moveToNext());
         } catch (CursorIndexOutOfBoundsException e) {
             cursor.close();
             return messageStanzas;
         }
         cursor.close();
         Log.d("dbdb","size - getalk msg :"+messageStanzas.size());
         return messageStanzas;
     }
 
     public Vector<MessageStanza> getMsgByJID(String jid) {
         return null;
     }
 
     public void removeMsg(String id) {
         SQLiteDatabase db = DBManager.getDbManager().getWritableSQLiteDB();
     }
 }
