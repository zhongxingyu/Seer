 package com.quanleimu.database;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 
 import com.quanleimu.entity.ChatMessage;
 import com.quanleimu.entity.compare.MsgTimeComparator;
 
 /**
  * 
  * @author liuchong
  *
  */
 public class ChatMessageDatabase extends Database 
 {
 	ChatMessageDatabase(Context ctx) {
 		super(ctx);
 	}
 	
 	public static void prepareDB(Context ctx)
 	{
 		if (database == null)
 		{
 			new ChatMessageDatabase(ctx);
 		}
 	}
 	
 	/**
 	 * Query message by session.
 	 * @param sid
 	 * @return
 	 */
 	public static List<ChatMessage> queryMessageBySession(String sid)
 	{
 		List<ChatMessage> list = new ArrayList<ChatMessage>();
 		
 		Cursor cur = databaseRO.query(DatabaseOpenHelper.CHAT_MESSAGE_TABLE, 
 				new String[] {"msgJson"}, "sessionId='" + sid + "'", null, null, null, null, null);
 		
 		if (cur != null && cur.moveToFirst())
 		{
 			
 			do
 			{
 				String msg = cur.getString(0);//cur.getString(cur.getColumnIndex("msgJson"));
 				try
 				{
 					list.add(ChatMessage.fromJson(msg));
 				}
 				catch(Throwable t) //ignor message in bad format.
 				{
 					
 				}
 			
 			} while (cur.moveToNext());
 		}
 		
 		if (cur != null)
 		{
 			cur.deactivate();
 			cur.close();
 		}
 		
 		return list;
 	}
 	
 	public static long getLastMsgTime(String sid)
 	{
 		List<ChatMessage> msglist = queryMessageBySession(sid);
 		if (msglist.size() == 0)
 		{
 			return -1;
 		}
 		
 		Collections.sort(msglist, new MsgTimeComparator());
 		
 		return msglist.get(msglist.size()-1).getTimestamp();
 	}
 	
 	public static String getSessionId(String from, String to, String adId)
 	{
 		String sid = null;
 		
 		String where = "(" + "(sender='" + from + "' AND receiver='" + to +"')" + " OR " + "(receiver='" + from + "' AND sender='" + to +"')" + ")";
 		if (adId != null && adId.trim().length()>0)
 		{
 			where += " AND adId='" + adId +"'";
 		}
 		
 		Cursor cur = databaseRO.query(DatabaseOpenHelper.CHAT_MESSAGE_TABLE, new String[] {"sessionId"}, where, 
 				null, null, null, null);
 		if (cur != null && cur.moveToFirst())
 		{
 			sid = cur.getString(0);
 			cur.close();
 		}
 		
 		
 		return sid;
 	}
 	
 	public static boolean hasMessage(String msgId)
 	{
 		boolean exists = false;
 		Cursor cur = databaseRO.query(DatabaseOpenHelper.CHAT_MESSAGE_TABLE, 
 				new String[] {"msgId"}, "msgId='" + msgId + "'", null, null, null, null, null);
 		
 		if (cur != null && cur.moveToFirst())
 		{
 			exists = true;
 			cur.deactivate();
 			cur.close();
 		}
 		
 		return exists;
 	}
 	
 	public static void storeMessage(ChatMessage msg)
 	{
 		ContentValues values = new ContentValues();
 		values.put("msgId", msg.getId());
 		values.put("adId", msg.getAdId());
 		values.put("sender", msg.getFrom());
 		values.put("receiver", msg.getTo());
 		values.put("msgJson", msg.toJson());
 		values.put("sessionId", msg.getSession());
 		values.put("timestamp", msg.getTimestamp());
 		
 		if (hasMessage(msg.getId()))
 		{
 			database.update(DatabaseOpenHelper.CHAT_MESSAGE_TABLE, values, "msgId='" + msg.getId() + "'", null);
 		}
 		else
 		{
 			database.insert(DatabaseOpenHelper.CHAT_MESSAGE_TABLE, null, values);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param olderThan time in seconds.
 	 */
 	public static void deleteMsgOlderthan(long olderThan)
 	{
 		database.delete(DatabaseOpenHelper.CHAT_MESSAGE_TABLE, "timestamp < " + olderThan, null);
 	}
 	
 	public static void clearDatabase()
 	{
 		database.execSQL("delete * from " + DatabaseOpenHelper.CHAT_MESSAGE_TABLE);
 	}
 	
 	public static void storeMessage(String msg)
 	{
 		ChatMessage msgObj = ChatMessage.fromJson(msg);
 		if (msgObj.getId() != null)
 		{
 			storeMessage(msgObj);
 		}
 	}
 
 }
