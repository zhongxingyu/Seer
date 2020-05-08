 // credit: http://www.screaming-penguin.com/node/7742
 
 package eecs.berkeley.edu.cs294;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteStatement;
 import android.util.Log;
 
 public class DatabaseHelper {
 	private static final String DATABASE_NAME = "noctis.db";
 	private static final int DATABASE_VERSION = 1;
 
 	/* Indexes of the various entries in the user table */
 	public static final int NAME_INDEX_U = 0;
 	public static final int NUMBER_INDEX_U = 1;
 	public static final int EMAIL_INDEX_U = 2;
 	public static final int PASSWORD_INDEX_U = 3;
 	public static final int USER_RAILS_ID_INDEX_U = 4;
 	
 	/* Indexes of the various entries in the todo table */
 	public static final int TITLE_INDEX_T = 0;
 	public static final int PLACE_INDEX_T = 1;
 	public static final int NOTE_INDEX_T = 2;
 	public static final int TAG_INDEX_T = 3;
 	public static final int GROUP_ID_INDEX_T = 4;
 	public static final int STATUS_INDEX_T = 5;
 	public static final int PRIORITY_INDEX_T = 6;
 	public static final int TIMESTAMP_INDEX_T = 7;
 	public static final int DEADLINE_INDEX_T = 8;
 	public static final int TO_DO_RAILS_ID_INDEX_T = 9;
 	public static final int TD_ID_INDEX_T = 10;
 	
 	/* Indexes of the various entries in the group table */
 	public static final int GROUP_ID_INDEX_G = 0;
 	public static final int NAME_INDEX_G = 1;
 	public static final int DESCRIPTION_INDEX_G = 2;
 	public static final int MEMBER_INDEX_G = 3;
 	public static final int TIMESTAMP_INDEX_G = 4;
 	public static final int GROUP_RAILS_ID_INDEX_G = 5;
 	
 	/* Indexes of the various entries in the member table */
 	public static final int MEMBER_ID_INDEX_M = 0;
 	public static final int NAME_INDEX_M = 1;
 	public static final int NUMBER_INDEX_M = 2;
 	public static final int EMAIL_INDEX_M = 3;
 	public static final int GROUP_ID_INDEX_M = 4;
 	public static final int TIMESTAMP_INDEX_M = 5;
 	public static final int MEMBER_RAILS_ID_INDEX_M = 6;
 
 	/* Indexes of the various entries in the sent invitation table */
 	public static final int SENT_ID_INDEX_S = 0;
 	public static final int RECIPIENT_ID_INDEX_S = 1;
 	public static final int GROUPZ_ID_INDEX_S = 2;
 	public static final int STATUS_INDEX_S = 3;
 	public static final int DESCRIPTION_INDEX_S = 4;
 	public static final int TIMESTAMP_INDEX_S = 5;
 	public static final int SENT_RAILS_ID_INDEX_S = 6;
 	
 	/* Indexes of the various entries in the received invitation table */
 	public static final int RECV_ID_INDEX_R = 0;
 	public static final int SENDER_INDEX_R = 1;
 	public static final int GROUPZ_INDEX_R = 2;
 	public static final int TIMESTAMP_INDEX_R = 3;
 	public static final int RECV_RAILS_ID_INDEX_R = 4;
 	
 	/* Tables that exists in the database */
 	private static final String TABLE_NAME_TO_DO = "to_do_table";
 	private static final String TABLE_NAME_GROUP = "group_table";
 	private static final String TABLE_NAME_MEMBER = "member_table";
 	private static final String TABLE_NAME_USER = "user_table";
 	private static final String TABLE_NAME_SENT_INVITATION = "sent_invitation_table";
 	private static final String TABLE_NAME_RECV_INVITATION = "recv_invitation_table";
 	private static final String TABLE_NAME_MAP_GROUP_T0_DO = "map_group_to_do";
 	private static final String TABLE_NAME_MAP_GROUP_MEMBER = "map_group_member";
 	
 	private static final String INSERT_USER = "insert into " + TABLE_NAME_USER + " " +
 			"(name, number, email, password, user_rails_id) values (?, ?, ?, ?, ?)";
 	private static final String INSERT_TO_DO = "insert into " + TABLE_NAME_TO_DO + 
 	" (td_id, title, place, note, tag, group_id, status, priority, timestamp, deadline, " +
 	"to_do_rails_id) values (NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
 	private static final String INSERT_GROUP = "insert into " + TABLE_NAME_GROUP + 
 	" (g_id, name, description, member, timestamp, group_rails_id) " +
 	"values (NULL, ?, ?, ?, ?, ?)";
 	private static final String INSERT_MEMBER = "insert into " + TABLE_NAME_MEMBER + 
 	" (m_id, name, number, email, group_id, timestamp, member_rails_id) " +
 	"values (NULL, ?, ?, ?, ?, ?, ?)";
 	private static final String INSERT_SENT_INVITATION = "insert into " + 
 	TABLE_NAME_SENT_INVITATION + " (sent_id, recipient, groupz, status, description, " +
 			"timestamp, sent_rails_id) values (NULL, ?, ?, ?, ?, ?, ?)";
 	private static final String INSERT_RECV_INVITATION = "insert into " + 
 	TABLE_NAME_RECV_INVITATION + " (recv_id, sender, groupz, timestamp, recv_rails_id) " +
 			"values (NULL, ?, ?, ?, ?)";
 	private static final String INSERT_MAP_GROUP_TO_DO = "insert into " + 
 	TABLE_NAME_MAP_GROUP_T0_DO + " (map_group, map_to_do) values (?, ?)";
 	private static final String INSERT_MAP_GROUP_MEMBER = "insert into " + 
 	TABLE_NAME_MAP_GROUP_MEMBER + " (map_group, map_member) values (?, ?)";
 	
 	public static final String TITLE = "title";
 	public static final String PLACE = "place"; 
 
 	private Context context;
 	private SQLiteDatabase db;
 	private SQLiteStatement insertStmt_to_do, insertStmt_group, insertStmt_member, insertStmt_sent, insertStmt_recv, insertStmt_map_group_to_do, insertStmt_map_group_member, insertStmt_user;
 
 	public DatabaseHelper(Context context) {
 		this.context = context;
 		OpenHelper openHelper = new OpenHelper(this.context);
 		this.db = openHelper.getWritableDatabase();
 		this.insertStmt_to_do = this.db.compileStatement(INSERT_TO_DO);
 		this.insertStmt_group = this.db.compileStatement(INSERT_GROUP);
 		this.insertStmt_member = this.db.compileStatement(INSERT_MEMBER);
 		this.insertStmt_user = this.db.compileStatement(INSERT_USER);
 		this.insertStmt_sent = this.db.compileStatement(INSERT_SENT_INVITATION);
 		this.insertStmt_recv = this.db.compileStatement(INSERT_RECV_INVITATION);
 		this.insertStmt_map_group_to_do = this.db.compileStatement(INSERT_MAP_GROUP_TO_DO);
 		this.insertStmt_map_group_member = this.db.compileStatement(INSERT_MAP_GROUP_MEMBER);
 	}
 	
 	public long insert_user(String name, String number, String email, String password, 
 			String railsID) {	
 		this.insertStmt_user.clearBindings();
 		this.insertStmt_user.bindString(1, name);
 		this.insertStmt_user.bindString(2, number);
 		this.insertStmt_user.bindString(3, email);
 		this.insertStmt_user.bindString(4, password);
 		this.insertStmt_user.bindString(5, railsID);
 		return this.insertStmt_user.executeInsert();
 	}
 	
 	public List<String> select_user() {
 		List<String> list = new ArrayList<String>();
 		Cursor cursor = this.db.query(TABLE_NAME_USER, null, null, null, null, null, null);
 		if (cursor.moveToFirst()) {
 			do {
 				list.add(cursor.getString(cursor.getColumnIndex("name")));
 				list.add(cursor.getString(cursor.getColumnIndex("number")));
 				list.add(cursor.getString(cursor.getColumnIndex("email")));
 				list.add(cursor.getString(cursor.getColumnIndex("password")));
 				list.add(cursor.getString(cursor.getColumnIndex("user_rails_id")));
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null || !cursor.isClosed()) {
 			cursor.close();
 		}
 		return list;
 	}
 
 	public long update_user(String name, String number, String email, String password,
 			String railsID) {
 		ContentValues cv = new ContentValues();
 		if(name != null) {
 			cv.put("name", name);
 		}
 		if(number != null) {
 			cv.put("number", number);
 		}
 		if(email != null) {
 			cv.put("email", email);
 		}
 		if(password != null) {
 			cv.put("password", password);
 		}
 		if(railsID != null) {
 			cv.put("user_rails_id", railsID);
 		}
 		
 		Log.d("DbDEBUG", "UPDATE name: " + name + " number: " + number + " email: " + 
 				email + " password: " + password + " railsID: " + railsID);
 
 		return db.update(TABLE_NAME_USER, cv, null, null);
 	}
 
 	public long insert_to_do(String title, String place, String note, String tag, String group_id, String status, String priority, String timestamp, String deadline, String to_do_rails_id) {	
 		this.insertStmt_to_do.clearBindings();
 		this.insertStmt_to_do.bindString(1, title);
 		this.insertStmt_to_do.bindString(2, place);
 		this.insertStmt_to_do.bindString(3, note);
 		this.insertStmt_to_do.bindString(4, tag);
 		this.insertStmt_to_do.bindString(5, group_id);
 		this.insertStmt_to_do.bindString(6, status);
 		this.insertStmt_to_do.bindString(7, priority);
 		this.insertStmt_to_do.bindString(8, timestamp);
 		this.insertStmt_to_do.bindString(9, deadline);
 		this.insertStmt_to_do.bindString(10, to_do_rails_id);
 		
 		return this.insertStmt_to_do.executeInsert();
 	}
 
 	public long update_to_do(int td_id, String title, String place, String note, String tag, int group_id, String status, String priority, String timestamp, String deadline, String to_do_rails_id) {
 		ContentValues cv = new ContentValues();
 		if(title != null) {
 			cv.put("title", title);
 		}
 		if(place != null) {
 			cv.put("place", place);
 		}
 		if(note != null) {
 			cv.put("note", note);
 		}
 		if(tag != null) {
 			cv.put("tag", tag);
 		}
 		if(group_id >= 1) {
 			cv.put("group_id", group_id);
 		}
 		if(status != null) {
 			cv.put("status", status);
 		}
 		if(priority != null) {
 			cv.put("priority", priority);
 		}
 		if(timestamp != null) {
 			cv.put("timestamp", timestamp);
 		}
 		if(deadline != null) {
 			cv.put("deadline", deadline);
 		}
 		if(to_do_rails_id != null) {
 			cv.put("to_do_rails_id", to_do_rails_id);
 		}
 
 		Log.d("DbDEBUG", "UPDATE title: " + title + " place: " + place + " note: " + note + 
 				" tag: " + tag + " group_id: " + group_id + " status: " + status + 
 				" priority: " + priority + " timestamp: " + timestamp + " deadline: " + deadline +  " to_do_rails_id: " + to_do_rails_id);
 
 		String selection = "td_id = ?";
 		return db.update(TABLE_NAME_TO_DO, cv, selection, new String[] {Integer.toString(td_id)});
 	}
 	
 	public List<String> select_all_to_do(String column) {
 		List<String> list = new ArrayList<String>();
 		Cursor cursor = this.db.query(TABLE_NAME_TO_DO, new String[] {column}, null, null, null, null, "td_id asc");
 		if (cursor.moveToFirst()) {
 			do {
 				list.add(cursor.getString(cursor.getColumnIndex(column)));
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null || !cursor.isClosed()) {
 			cursor.close();
 		}
 		return list;
 	}
 	
 	public List<String[]> select_to_do_title_place() {
 		List<String[]> list = new ArrayList<String[]>();
 		Cursor cursor = this.db.query(TABLE_NAME_TO_DO, null, null, null, null, null, "td_id asc");
 		if (cursor.moveToFirst()) {
 			do {
 				list.add(new String[] {cursor.getString(cursor.getColumnIndex(TITLE)), cursor.getString(cursor.getColumnIndex(PLACE))});
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null || !cursor.isClosed())
 			cursor.close();
 		return list;
 	}
 
 	public List<String> select_to_do(String column, String value) {
 		List<String> list = new ArrayList<String>();
 		String selection = column + " = '" + value + "'";
 		Cursor cursor = this.db.query(TABLE_NAME_TO_DO, null, selection, null, null, null, null);
 		if (cursor.moveToFirst()) {
 			do {
 				list.add(cursor.getString(cursor.getColumnIndex("title")));
 				list.add(cursor.getString(cursor.getColumnIndex("place")));
 				list.add(cursor.getString(cursor.getColumnIndex("note")));
 				list.add(cursor.getString(cursor.getColumnIndex("tag")));
 				list.add(cursor.getString(cursor.getColumnIndex("group_id")));
 				list.add(cursor.getString(cursor.getColumnIndex("status")));
 				list.add(cursor.getString(cursor.getColumnIndex("priority")));
 				list.add(cursor.getString(cursor.getColumnIndex("timestamp")));
 				list.add(cursor.getString(cursor.getColumnIndex("deadline")));
 				list.add(cursor.getString(cursor.getColumnIndex("to_do_rails_id")));
 				list.add(cursor.getString(cursor.getColumnIndex("td_id")));
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null || !cursor.isClosed()) {
 			cursor.close();
 		}
 		return list;
 	}
 
 	public List<String> select_to_do(int td_id) {
 		List<String> list = new ArrayList<String>();
 		String selection = "td_id = " + td_id;
 		Cursor cursor = this.db.query(TABLE_NAME_TO_DO, null, selection, null, null, null, null);
 		if (cursor.moveToFirst()) {
 			do {
 				list.add(cursor.getString(cursor.getColumnIndex("title")));
 				list.add(cursor.getString(cursor.getColumnIndex("place")));
 				list.add(cursor.getString(cursor.getColumnIndex("note")));
 				list.add(cursor.getString(cursor.getColumnIndex("tag")));
 				list.add(cursor.getString(cursor.getColumnIndex("group_id")));
 				list.add(cursor.getString(cursor.getColumnIndex("status")));
 				list.add(cursor.getString(cursor.getColumnIndex("priority")));
 				list.add(cursor.getString(cursor.getColumnIndex("timestamp")));
 				list.add(cursor.getString(cursor.getColumnIndex("deadline")));
 				list.add(cursor.getString(cursor.getColumnIndex("to_do_rails_id")));
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null || !cursor.isClosed()) {
 			cursor.close();
 		}
 		return list;
 	}
 	
 	public int select_to_do_primary_key(String title) {
 		int td_id = -1;
 		String selection = "title" + " = '" + title + "'";
 		Cursor cursor = this.db.query(TABLE_NAME_TO_DO, null, selection, null, null, null, null);
 		if (cursor.moveToFirst()) {
 			do {
 				td_id = cursor.getInt(cursor.getColumnIndex("td_id"));
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null || !cursor.isClosed()) {
 			cursor.close();
 		}
 		return td_id;
 	}
 	
 	public void delete_all_to_do() {
 		this.db.delete(TABLE_NAME_TO_DO, null, null);
 	}
 
 	public void delete_to_do(String column, String value) {
 		String selection = column + " = ?";
 		this.db.delete(TABLE_NAME_TO_DO, selection, new String[] {value});
 	}
 	
 	public List<String> select_group_member(String name) {
 		List<Integer> list = new ArrayList<Integer>();
 		String selection = "name = " + name;
 		Cursor cursor = this.db.query(TABLE_NAME_GROUP, null, selection, null, null, null, null);
 		if (cursor.moveToFirst()) {
 			do {
 				String member_s = cursor.getString(cursor.getColumnIndex("member"));
 				String[] member_sa = member_s.split(" ");
 				for(int i = 0; i < member_sa.length; i++)
 					list.add(Integer.parseInt(member_sa[i]));
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null || !cursor.isClosed()) {
 			cursor.close();
 		}
 		List<String> rtn = new ArrayList<String>();
 		for (int m_id : list){
 			rtn.add(this.db.query(TABLE_NAME_MEMBER, new String[]{"name"}, "m_id = '" + m_id + "'", null, null, null, null).getString(NAME_INDEX_M));
 		}
 		return rtn;
 	}
 	
 	public int select_group_id(String name) {
 		int g_id = -1;
		String selection = "name = " + name;
 		Cursor cursor = this.db.query(TABLE_NAME_GROUP, null, selection, null, null, null, null);
 		if (cursor.moveToFirst()) {
 			do {
 				g_id = cursor.getInt(cursor.getColumnIndex("g_id"));
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null || !cursor.isClosed()) {
 			cursor.close();
 		}
 		return g_id;
 	}
 	
 	public long insert_group(String name, String description, String member, String timestamp, 
 			String group_rails_id) {
 		this.insertStmt_group.clearBindings();
 		this.insertStmt_group.bindString(1, name);
 		this.insertStmt_group.bindString(2, description);
 		this.insertStmt_group.bindString(3, member);
 		this.insertStmt_group.bindString(4, timestamp);
 		this.insertStmt_group.bindString(5, group_rails_id);
 		return this.insertStmt_group.executeInsert();
 	}
 	
 	public void deleteAll_group() {
 		this.db.delete(TABLE_NAME_GROUP, null, null);
 	}
 
 	public List<String> select_all_group_name() {
 		List<String> list = new ArrayList<String>();
 		Cursor cursor = this.db.query(TABLE_NAME_GROUP, new String[] {"name"}, null, null, null, null, "g_id asc");
 		if (cursor.moveToFirst()) {
 			do {
 				list.add(cursor.getString(cursor.getColumnIndex("name")));
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null || !cursor.isClosed()) {
 			cursor.close();
 		}
 		return list;
 	}
 
 	public List<String> select_all_groups(String column) {
 		List<String> list = new ArrayList<String>();
 		Cursor cursor = this.db.query(TABLE_NAME_GROUP, new String[] {column}, null, null, null, null, "g_id asc");
 		if (cursor.moveToFirst()) {
 			do {
 				list.add(cursor.getString(cursor.getColumnIndex(column)));
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null && !cursor.isClosed()) {
 			cursor.close();
 		}
 		return list;
 	}
 	
 	public List<String> select_group(String column, String value) {
 		List<String> list = new ArrayList<String>();
 		String selection = column + " = '" + value + "'";
 		Cursor cursor = this.db.query(TABLE_NAME_GROUP, null, selection, null, null, null, null);
 		if (cursor.moveToFirst()) {
 			do {
 				list.add(cursor.getString(cursor.getColumnIndex("g_id")));
 				list.add(cursor.getString(cursor.getColumnIndex("name")));
 				list.add(cursor.getString(cursor.getColumnIndex("description")));
 				list.add(cursor.getString(cursor.getColumnIndex("member")));
 				list.add(cursor.getString(cursor.getColumnIndex("timestamp")));
 				list.add(cursor.getString(cursor.getColumnIndex("group_rails_id")));
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null && !cursor.isClosed()) {
 			cursor.close();
 		}
 		return list;
 	}
 
 	public long update_group(int g_id, String name, String description, String member, 
 			String timestamp, String group_rails_id) {
 		ContentValues cv = new ContentValues();
 		
 		if(g_id < 1) {
 			Log.d("DbDEBUG", "invalid group id: " + g_id);
 			return -1;
 		}
 		
 		if(name != null) {
 			cv.put("name", name);
 		}
 		if(description != null) {
 			cv.put("description", description);
 		}
 		if(member!= null) {
 			cv.put("member", member);
 		}
 		if(timestamp != null) {
 			cv.put("timestamp", timestamp);
 		}
 		if(group_rails_id != null) {
 			cv.put("group_id", group_rails_id);
 		}
 
 		Log.d("DbDEBUG", "UPDATE name: " + name + " description: " + description + 
 				" member: " + member + " timestamp: " + timestamp + " group_rails_id: " + 
 				group_rails_id);
 
 		String selection = "g_id = ?";
 		return db.update(TABLE_NAME_GROUP, cv, selection, new String[] {Integer.toString(g_id)});
 	}
 
 	public void delete_all_group() {
 		this.db.delete(TABLE_NAME_GROUP, null, null);
 	}
 
 	public void delete_group(String column, String value) {
 		String selection = column + " = ?";
 		this.db.delete(TABLE_NAME_GROUP, selection, new String[] {value});
 	}
 	
 	public long insert_member(String name, String number, String email, String group_id, 
 			String timestamp, String member_rails_id) {
 		this.insertStmt_member.clearBindings();
 		this.insertStmt_member.bindString(1, name);
 		this.insertStmt_member.bindString(2, number);
 		this.insertStmt_member.bindString(3, email);
 		this.insertStmt_member.bindString(4, group_id);
 		this.insertStmt_member.bindString(5, timestamp);
 		this.insertStmt_member.bindString(6, member_rails_id);
 		
 		return this.insertStmt_member.executeInsert();
 	}
 	
 	public List<String> select_all_members(String column) {
 		List<String> list = new ArrayList<String>();
 		Cursor cursor = this.db.query(TABLE_NAME_MEMBER, new String[] {column}, null, null, null, null, "m_id asc");
 		if (cursor.moveToFirst()) {
 			do {
 				list.add(cursor.getString(cursor.getColumnIndex(column)));
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null && !cursor.isClosed()) {
 			cursor.close();
 		}
 		return list;
 	}
 	
 	public List<String> select_member(String column, String value) {
 		List<String> list = new ArrayList<String>();
 		String selection = column + " = '" + value + "'";
 		Cursor cursor = this.db.query(TABLE_NAME_MEMBER, null, selection, null, null, null, null);
 		if (cursor.moveToFirst()) {
 			do {
 				list.add(cursor.getString(cursor.getColumnIndex("m_id")));
 				list.add(cursor.getString(cursor.getColumnIndex("name")));
 				list.add(cursor.getString(cursor.getColumnIndex("number")));
 				list.add(cursor.getString(cursor.getColumnIndex("email")));
 				list.add(cursor.getString(cursor.getColumnIndex("group_id")));
 				list.add(cursor.getString(cursor.getColumnIndex("timestamp")));
 				list.add(cursor.getString(cursor.getColumnIndex("member_rails_id")));
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null && !cursor.isClosed()) {
 			cursor.close();
 		}
 		return list;
 	}
 
 	public long update_member(int m_id, String name, String number, String email, 
 			String group_id, String timestamp, String member_rails_id) {
 		ContentValues cv = new ContentValues();
 		
 		if(m_id < 1) {
 			Log.d("DbDEBUG", "invalid member id: " + m_id);
 			return -1;
 		}
 		
 		if(name != null) {
 			cv.put("name", name);
 		}
 		if(number != null) {
 			cv.put("number", number);
 		}
 		if(email != null) {
 			cv.put("email", email);
 		}
 		if(group_id != null) {
 			cv.put("group_id", group_id);
 		}
 		if(timestamp != null) {
 			cv.put("timestamp", timestamp);
 		}
 		if(member_rails_id != null) {
 			cv.put("member_rails_id", member_rails_id);
 		}
 		
 		Log.d("DbDEBUG", "UPDATE name: " + name + " number: " + number + " email: " + 
 				email + " group_id: " + group_id + " timestamp: " + timestamp); 
 
 		String selection = "m_id = ?";
 		return db.update(TABLE_NAME_MEMBER, cv, selection, 
 				new String[] {Integer.toString(m_id)});
 	}
 	
 	public void delete_all_member() {
 		this.db.delete(TABLE_NAME_MEMBER, null, null);
 	}
 
 	public void delete_member(String column, String value) {
 		String selection = column + " = ?";
 		this.db.delete(TABLE_NAME_MEMBER, selection, new String[] {value});
 	}
 	
 	public long insert_sent_invitation(String recipient, String groupz, String status, 
 			String description, String timestamp, String sent_rails_id) {
 		this.insertStmt_sent.clearBindings();
 		this.insertStmt_sent.bindString(1, recipient);
 		this.insertStmt_sent.bindString(2, groupz);
 		this.insertStmt_sent.bindString(3, status);
 		this.insertStmt_sent.bindString(4, description);
 		this.insertStmt_sent.bindString(5, timestamp);
 		this.insertStmt_sent.bindString(6, sent_rails_id);
 		return this.insertStmt_sent.executeInsert();
 	}
 	
 	public List<String> select_all_sent_invitations(String column) {
 		List<String> list = new ArrayList<String>();
 		Cursor cursor = this.db.query(TABLE_NAME_SENT_INVITATION, new String[] {column}, null, null, null, null, "sent_id asc");
 		if (cursor.moveToFirst()) {
 			do {
 				list.add(cursor.getString(cursor.getColumnIndex(column)));
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null && !cursor.isClosed()) {
 			cursor.close();
 		}
 		return list;
 	}
 	
 	public List<String> select_sent_invitation(String column, String value) {
 		List<String> list = new ArrayList<String>();
 		String selection = column + " = '" + value + "'";
 		Cursor cursor = this.db.query(TABLE_NAME_SENT_INVITATION, null, selection, null, null, null, null);
 		if (cursor.moveToFirst()) {
 			do {
 				list.add(cursor.getString(cursor.getColumnIndex("sent_id")));
 				list.add(cursor.getString(cursor.getColumnIndex("recipient")));
 				list.add(cursor.getString(cursor.getColumnIndex("groupz")));
 				list.add(cursor.getString(cursor.getColumnIndex("status")));
 				list.add(cursor.getString(cursor.getColumnIndex("description")));
 				list.add(cursor.getString(cursor.getColumnIndex("timestamp")));
 				list.add(cursor.getString(cursor.getColumnIndex("sent_rails_id")));
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null && !cursor.isClosed()) {
 			cursor.close();
 		}
 		return list;
 	}
 	
 	public long update_sent_invitation(int sent_id, String recipient, String groupz, 
 			String status, String description, String timestamp, String sent_rails_id) {
 		ContentValues cv = new ContentValues();
 		
 		if(sent_id < 1) {
 			Log.d("DbDEBUG", "invalid sent id: " + sent_id);
 			return -1;
 		}
 		
 		if(recipient != null) {
 			cv.put("recipient", recipient);
 		}
 		if(groupz != null) {
 			cv.put("groupz", groupz);
 		}
 		if(status != null) {
 			cv.put("status", status);
 		}
 		if(description != null) {
 			cv.put("description", description);
 		}
 		if(timestamp != null) {
 			cv.put("timestamp", timestamp);
 		}
 		if(sent_rails_id != null) {
 			cv.put("sent_rails_id", sent_rails_id);
 		}
 		
 		Log.d("DbDEBUG", "UPDATE sent_id: " + sent_id + " recipient: " + recipient + 
 				" groupz: " + groupz + " status: " + status + " description: " + description + 
 				"timestamp: " + timestamp + "sent_rails_id: " + sent_rails_id); 
 
 		String selection = "sent_id = ?";
 		return db.update(TABLE_NAME_SENT_INVITATION, cv, selection, 
 				new String[] {Integer.toString(sent_id)});
 	}
 	
 	public void delete_all_sent_invitation() {
 		this.db.delete(TABLE_NAME_SENT_INVITATION, null, null);
 	}
 
 	public void delete_sent_invitation(String column, String value) {
 		String selection = column + " = ?";
 		this.db.delete(TABLE_NAME_SENT_INVITATION, selection, new String[] {value});
 	}
 	
 	public long insert_recv_invitation(String sender, String groupz, String timestamp, 
 			String recv_rails_id) {
 		this.insertStmt_recv.clearBindings();
 		this.insertStmt_recv.bindString(1, sender);
 		this.insertStmt_recv.bindString(2, groupz);
 		this.insertStmt_recv.bindString(3, timestamp);
 		this.insertStmt_recv.bindString(4, recv_rails_id);
 		return this.insertStmt_recv.executeInsert();
 	}
 
 	public List<String> select_all_recv_invitations(String column) {
 		List<String> list = new ArrayList<String>();
 		Cursor cursor = this.db.query(TABLE_NAME_RECV_INVITATION, new String[] {column}, null, null, null, null, "recv_id asc");
 		if (cursor.moveToFirst()) {
 			do {
 				list.add(cursor.getString(cursor.getColumnIndex(column)));
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null && !cursor.isClosed()) {
 			cursor.close();
 		}
 		return list;
 	}
 
 	public List<String> select_recv_invitation(String column, String value) {
 		List<String> list = new ArrayList<String>();
 		String selection = column + " = '" + value + "'";
 		Cursor cursor = this.db.query(TABLE_NAME_RECV_INVITATION, null, selection, null, null, null, null);
 		if (cursor.moveToFirst()) {
 			do {
 				list.add(cursor.getString(cursor.getColumnIndex("recv_id")));
 				list.add(cursor.getString(cursor.getColumnIndex("sender")));
 				list.add(cursor.getString(cursor.getColumnIndex("groupz")));
 				list.add(cursor.getString(cursor.getColumnIndex("timestamp")));
 				list.add(cursor.getString(cursor.getColumnIndex("recv_rails_id")));
 			} while (cursor.moveToNext());
 		}
 		if (cursor != null && !cursor.isClosed()) {
 			cursor.close();
 		}
 		return list;
 	}
 	
 	public long update_recv_invitation(int recv_id, String sender, String groupz, 
 			String timestamp, String recv_rails_id) {
 		ContentValues cv = new ContentValues();
 		
 		if(recv_id < 1) {
 			Log.d("DbDEBUG", "invalid recv id: " + recv_id);
 			return -1;
 		}
 		
 		if(sender != null) {
 			cv.put("sender", sender);
 		}
 		if(groupz != null) {
 			cv.put("groupz", groupz);
 		}
 		if(timestamp != null) {
 			cv.put("timestamp", timestamp);
 		}
 		if(recv_rails_id != null) {
 			cv.put("recv_rails_id", recv_rails_id);
 		}
 		
 		Log.d("DbDEBUG", "UPDATE recv_id: " + recv_id + " sender: " + sender + 
 				" groupz: " + groupz + "timestamp: " + timestamp + "recv_rails_id: " 
 				+ recv_rails_id); 
 
 		String selection = "recv_id = ?";
 		return db.update(TABLE_NAME_RECV_INVITATION, cv, selection, 
 				new String[] {Integer.toString(recv_id)});
 	}
 	
 	public void delete_all_recv_invitation() {
 		this.db.delete(TABLE_NAME_RECV_INVITATION, null, null);
 	}
 
 	public void delete_recv_invitation(String column, String value) {
 		String selection = column + " = ?";
 		this.db.delete(TABLE_NAME_RECV_INVITATION, selection, new String[] {value});
 	}
 	
 	private static class OpenHelper extends SQLiteOpenHelper {
 		OpenHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL("CREATE TABLE " + TABLE_NAME_TO_DO + " (td_id INTEGER PRIMARY KEY, title TEXT, place TEXT, note TEXT, tag TEXT, group_id TEXT, status TEXT, priority TEXT, timestamp TEXT, deadline TEXT, to_do_rails_id TEXT)");			
 			db.execSQL("CREATE TABLE " + TABLE_NAME_USER + " (name TEXT, number TEXT, email TEXT, password TEXT, user_rails_id TEXT)");	
 			db.execSQL("CREATE TABLE " + TABLE_NAME_GROUP + " (g_id INTEGER PRIMARY KEY, name TEXT, description TEXT, member TEXT, timestamp TEXT, group_rails_id TEXT)");
 			db.execSQL("CREATE TABLE " + TABLE_NAME_MEMBER + " (m_id INTEGER PRIMARY KEY, name TEXT, number TEXT, email TEXT, group_id INTEGER, timestamp TEXT, member_rails_id TEXT)");			
 			db.execSQL("CREATE TABLE " + TABLE_NAME_SENT_INVITATION + " (sent_id INTEGER PRIMARY KEY, recipient TEXT, groupz TEXT, status TEXT, description TEXT, timestamp TEXT, sent_rails_id TEXT)");
 			db.execSQL("CREATE TABLE " + TABLE_NAME_RECV_INVITATION + " (recv_id INTEGER PRIMARY KEY, sender TEXT, groupz TEXT, timestamp TEXT, recv_rails_id TEXT)");
 			db.execSQL("CREATE TABLE " + TABLE_NAME_MAP_GROUP_T0_DO + " (map_group INTEGER, map_to_do INTEGER, FOREIGN KEY(map_group) REFERENCES garden(g_id), FOREIGN KEY(map_to_do) REFERENCES plot(td_id))");
 			db.execSQL("CREATE TABLE " + TABLE_NAME_MAP_GROUP_MEMBER + " (map_group INTEGER, map_member INTEGER, FOREIGN KEY(map_group) REFERENCES garden(g_id), FOREIGN KEY(map_member) REFERENCES plot(m_id))");
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			Log.w("debug", "noctis reset");
 			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_TO_DO);
 			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_GROUP);
 			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_MEMBER);
 			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_USER);
 			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_SENT_INVITATION);
 			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_RECV_INVITATION);
 			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_MAP_GROUP_T0_DO);
 			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_MAP_GROUP_MEMBER);
 			onCreate(db);
 		}
 	}
 }
