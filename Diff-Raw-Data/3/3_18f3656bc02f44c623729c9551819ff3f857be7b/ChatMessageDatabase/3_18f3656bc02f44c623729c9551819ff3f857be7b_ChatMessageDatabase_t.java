 package com.quanleimu.database;
 
 import java.util.ArrayList;
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
 		try{
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
 		}catch(Throwable e){
 			e.printStackTrace();
 		}
 		
 		return list;
 	}
 	
 //	public static long getLastMsgTime(String sid)
 //	{
 //		List<ChatMessage> msglist = queryMessageBySession(sid);
 //		if (msglist.size() == 0)
 //		{
 //			return -1;
 //		}
 //		
 //		Collections.sort(msglist, new MsgTimeComparator());
 //		
 //		return msglist.get(msglist.size()-1).getTimestamp();
 //	}
 	
 	public static ChatMessage getLastMessage(String sid)
 	{
 		List<ChatMessage> msglist = queryMessageBySession(sid);
 		if (msglist.size() == 0)
 		{
 			return null;
 		}
 		
 		Collections.sort(msglist, new MsgTimeComparator());
 		
 		return msglist.get(msglist.size()-1);
 	}
 	
 	public static void updateReadStatus(String msgId, boolean readStatus)
 	{
 		try{
 			if (hasMessage(msgId))
 			{
 				String sql = "update " + DatabaseOpenHelper.CHAT_MESSAGE_TABLE + " set readstatus = " + Integer.valueOf(readStatus ? 1 : 0);
 				database.execSQL(sql);
 			}
 		}catch(Throwable e){
 			e.printStackTrace();
 		}
 	}
 	
 	public static int getUnreadCount(String sid)
 	{
 		String where = "readstatus=0 OR readstatus is NULL";
 		if (sid != null)
 		{
 			where = "( " + where + " ) AND sessionId='" + sid + "'";
 		}
 		Cursor cur = null;
 		try
 		{
 			cur = databaseRO.query(DatabaseOpenHelper.CHAT_MESSAGE_TABLE, new String[] {"readstatus"}, where, null, null, null, null);
 			if (cur != null)
 			{
 				return cur.getCount();
 			}
 		}
 		finally
 		{
 			if (cur != null)
 			{
 				cur.deactivate();
 				cur.close();
 			}
 		}
 		
 		return 0;
 	}
 	
 	public static String getSessionId(String from, String to, String adId)
 	{
 		String sid = null;
 		
 		String where = "(" + "(sender='" + from + "' AND receiver='" + to +"')" + " OR " + "(receiver='" + from + "' AND sender='" + to +"')" + ")";
 		if (adId != null && adId.trim().length()>0)
 		{
 			where += " AND adId='" + adId +"'";
 		}
 		try{
 			Cursor cur = databaseRO.query(DatabaseOpenHelper.CHAT_MESSAGE_TABLE, new String[] {"sessionId"}, where, 
 					null, null, null, null);
 			if (cur != null && cur.moveToFirst())
 			{
 				sid = cur.getString(0);
 			}
 			
 			if (cur != null)
 			{
 				cur.deactivate();
 				cur.close();
 			}
 		}catch(Throwable e){
 			e.printStackTrace();
 		}
 		
 		
 		return sid;
 	}
 	
 	public static boolean hasMessage(String msgId)
 	{
 		boolean exists = false;
 		try{
 			Cursor cur = databaseRO.query(DatabaseOpenHelper.CHAT_MESSAGE_TABLE, 
 					new String[] {"msgId"}, "msgId='" + msgId + "'", null, null, null, null, null);
 			
 			if (cur != null && cur.moveToFirst())
 			{
 				exists = true;
 			}
 			
 			if (cur != null)
 			{
 				cur.deactivate();
 				cur.close();
 			}
 		}catch(Throwable e){
 			e.printStackTrace();
 		}
 		
 		return exists;
 	}
 	
 	public static ChatMessage queryMessageByMsgId(String msgId)
 	{
 		Cursor cur = null;
 		
 		try
 		{
 			cur = databaseRO.query(DatabaseOpenHelper.CHAT_MESSAGE_TABLE, 
 					new String[] {"msgJson"}, "msgId='" + msgId + "'", null, null, null, null, null);
 			if (cur != null && cur.moveToFirst())
 			{
 				return ChatMessage.fromJson(cur.getString(0));
 			}
 		}
 		finally
 		{
 			if (cur != null)
 			{
 				cur.deactivate();
 				cur.close();
 			}
 		}
 		
 		
 		return null;
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
 		
 		try{
 			if (hasMessage(msg.getId()))
 			{
 				values.put("readstatus", "1");
 				database.update(DatabaseOpenHelper.CHAT_MESSAGE_TABLE, values, "msgId='" + msg.getId() + "'", null);
 			}
 			else
 			{
 				values.put("readstatus", "0");
 				long result = database.insert(DatabaseOpenHelper.CHAT_MESSAGE_TABLE, null, values);
 			}
 		}catch(Throwable e){
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * 
 	 * @param olderThan time in seconds.
 	 */
 	public static void deleteMsgOlderthan(long olderThan)
 	{
 		try{
 			database.delete(DatabaseOpenHelper.CHAT_MESSAGE_TABLE, "timestamp < " + olderThan, null);
 		}catch(Throwable e){
 			e.printStackTrace();
 		}
 	}
 	
 	public static void clearOldMessage(long keepCount)
 	{
 		Cursor cur;
 		long time = 0;
 		
 		try
 		{
 			cur = databaseRO.query(DatabaseOpenHelper.CHAT_MESSAGE_TABLE, 
 					new String[] {"timestamp"}, null, null, null, null, "timestamp", null);
 			if (cur.getCount() > keepCount)
 			{
 				int i = 0;
 				while (i<keepCount)
 				{
 					cur.moveToNext();
 					time = cur.getLong(0);
 					i++;
 				}
 			}
 		} 
 		catch(Throwable t)
 		{
 			
 		}
 		
 		if (time != 0)
 		{
 			deleteMsgOlderthan(time);
 		}
 	}
 	
 	public static void clearDatabase()
 	{
 		try{
//			database.execSQL("delete * from " + DatabaseOpenHelper.CHAT_MESSAGE_TABLE);
			database.delete(DatabaseOpenHelper.CHAT_MESSAGE_TABLE, null, null);
 		}catch(Throwable e){
 			e.printStackTrace();
 		}
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
