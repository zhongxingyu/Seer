 /*
  * Copyright (C) 2011 htbest2000@gmail.com
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.htbest2000.v2ex.util;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.util.Log;
 
 import com.htbest2000.v2ex.api.Topics;
 import com.htbest2000.v2ex.provider.Database;
 
 public class Misc {
 	private static Pattern sPatNumber = Pattern.compile("([1-9][0-9]*)");
 	
 	static public long extractNumber( String s ) {
 		Matcher m = sPatNumber.matcher(s);
 		if (null != m && m.find()) {
 			return Integer.parseInt( m.group(1) );
 		}
 		return -1;
 	}
 	
 	public static Cursor getTopicsCursor(Context context) {
 		return Database.getInstance(context).query(
 				Database.TABLE_TOPICS, null, null, null, null, null, null);
 	}
 	
 	public static Cursor getAllNodesCursor(Context context) {
 		return Database.getInstance(context).query(
 				Database.TABLE_NODES, null, null, null, null, null, null);
 	}
 	
 	public static Topics.Topic getTopic( Context context, long id ) {
 		Cursor cursor = null;
 		try {
 			cursor = Database.getInstance(context).query(
 					Database.TABLE_TOPICS, null, 
 					Database.Columns.Topics._ID + "=" + id,
 					null, null, null, null);
 			if (null != cursor && 0 != cursor.getCount()) {
 				cursor.moveToFirst();
 				Topics.Topic topic = new Topics.Topic();
 				topic.id = cursor.getLong(Database.Columns.Topics.ID_TOPTIC_ID);
 				topic.title = cursor.getString(Database.Columns.Topics.ID_TITLE);
 				topic.url = cursor.getString(Database.Columns.Topics.ID_URL);
 				topic.content = cursor.getString(Database.Columns.Topics.ID_CONTENT);
 				topic.content_rendered = cursor.getString(Database.Columns.Topics.ID_CONTENT_RENDERED);
 				topic.replies = cursor.getShort(Database.Columns.Topics.ID_REPLIES);
 				topic.member_id = cursor.getLong(Database.Columns.Topics.ID_MEMBER_ID);
 				topic.member_username = cursor.getString(Database.Columns.Topics.ID_MEMBER_NAME);
 				topic.node_id = cursor.getLong(Database.Columns.Topics.ID_NODE_ID);
 				topic.node_name = cursor.getString(Database.Columns.Topics.ID_NODE_NAME);
 				topic.node_title = cursor.getString(Database.Columns.Topics.ID_NODE_TITLE);
 				topic.node_title_alternative = cursor.getString(Database.Columns.Topics.ID_NODE_TITLE_ALTERNATIVE);
 				topic.node_topics = cursor.getInt(Database.Columns.Topics.ID_NODE_TOPICS);
 				topic.node_url = cursor.getString(Database.Columns.Topics.ID_NODE_URL);
 				return topic;
 			}
 			else {
 				return null;
 			}
 		}
 		finally {
 			if (null!= cursor)
 				cursor.close();
 		}
 	}
 	
 	static long getTopicId( Context context, long id ) {
 		Cursor cursor = null;
 		long ret = -1;
 		try {
 			cursor = Database.getInstance(context).query(
 					Database.TABLE_TOPICS, null, 
 					Database.Columns.Topics._ID + "=" + id,
 					null, null, null, null);
 			if (null != cursor && 0 != cursor.getCount()) {
 				cursor.moveToFirst();
 				ret = cursor.getLong(Database.Columns.Topics.ID_TOPTIC_ID);
 			}
 		}
 		finally {
 			if (null!= cursor)
 				cursor.close();
 		}
 		
 		return ret;
 	}
 	
 	/**
 	 * Get the account of specified record id
 	 * @param id db record id
 	 */
 	public static long getTopicCount(Context context, long id) {
 		Cursor cursor = null;
 		long ret = -1;
 		try {
 			cursor = Database.getInstance(context).query(
 					Database.TABLE_TOPICS, null, 
					Database.Columns.Topics.NAME_TOPTIC_ID + "=" + id,
 					null, null, null, null);
 			if (null != cursor) {
 				ret = cursor.getCount();
 			}
 		}
 		finally {
 			closeCursor(cursor);
 		}
 		
 		return ret;
 	}
 	
 	/**
 	 * get node count by node_id
 	 * @param node_id
 	 * <b>NOTE</b> node_id is NOT _id
 	 */
 	public static long getNodeCount(Context context, long node_id) {
 		Cursor cursor = null;
 		long cnt = -1;
 		try {
 			cursor = Database.getInstance(context).query(
 					Database.TABLE_NODES, null, Database.Columns.Nodes.NAME_NODE_ID + "=" + node_id,
 					null, null, null, null);
 			if (null != cursor) {
 				cnt = cursor.getCount();
 			}
 		}
 		finally {
 			closeCursor(cursor);
 		}
 		
 		return cnt;
 	}
 	
 	public static void dump(String tag, BufferedInputStream in) {
 		try {
 			InputStreamReader buffer = new InputStreamReader(in);
 			BufferedReader buffer2 = new BufferedReader(buffer);
 
 			BufferedReader reader = new BufferedReader(buffer2);
 			while (true) {
 				String line = reader.readLine();
 				if (null == line)
 					break;
 				Log.i("=ht=", tag + ": " + line);
 			}
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public static void closeCursor( Cursor cur ) {
 		if (null != cur)
 			cur.close();
 	}
 	
 	final static long YEAR  =   60 * 60 * 24 * 365;
 	final static long MONTH =   60 * 60 * 24 * 30;
 	final static long WEEK  =   60 * 60 * 24 * 6;
 	final static long DAY   =   60 * 60 * 24;
 	final static long HOUR  =   60 * 60;
 	final static long MIN   =   60;
 	
 
 	public static String formatRelativeTime(long interval) {
 		String ret = "";
 
 		if (interval >= YEAR) {
 			ret = "" + interval / YEAR + " year ago";
 		} else if (interval >= MONTH){
 			ret = "" + interval / MONTH + " month ago";
 		} else if (interval >= WEEK) {
 			ret = "" + interval / WEEK + " week ago";
 		} else if (interval >= DAY) {
 			ret = "" + interval / DAY + " day ago";
 		} else if (interval >= HOUR) {
 			ret = "" + interval / HOUR + " hour ago";
 		} else if (interval >= MIN) {
 			ret = "" + interval / MIN + " min ago";
 		}
 		
 		return ret;
 	}
 }
