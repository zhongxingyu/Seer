 package com.example.locus.core;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 import com.example.locus.entity.Message;
 import com.example.locus.entity.User;
 
 public class MessageDataSource {
 	private boolean isDatabaseOpen;
 	private SQLiteDatabase database;
 	private MessageSQLiteHelper dbHelper;
 	private AccountDataSource accountDataSource;
 
 	private String[] allColumns = { MessageSQLiteHelper.COLUMN_ID,
 			MessageSQLiteHelper.COLUMN_SRC_ID,
 			MessageSQLiteHelper.COLUMN_DES_ID, MessageSQLiteHelper.COLUMN_KIND,
 			MessageSQLiteHelper.COLUMN_DATA, MessageSQLiteHelper.COLUMN_MSG_ID };
 
 	public MessageDataSource(Context context,
 			AccountDataSource accountDataSource) {
 		dbHelper = new MessageSQLiteHelper(context);
 
 		this.accountDataSource = accountDataSource;
 		isDatabaseOpen = false;
 	}
 
 	public void open() throws SQLException {
 		if (!isDatabaseOpen) {
 			database = dbHelper.getWritableDatabase();
 			isDatabaseOpen = true;
 		}
 	}
 
 	public void close() {
 		if (isDatabaseOpen) {
 			dbHelper.close();
 			isDatabaseOpen = false;
 		}
 	}
 
 	public boolean isOpen() {
 		return isDatabaseOpen;
 	}
 
 	public Message createMessage(Message message) {
 		ContentValues values = new ContentValues();
 		values.put(MessageSQLiteHelper.COLUMN_SRC_ID, message.getSrc().getId());
 		values.put(MessageSQLiteHelper.COLUMN_DES_ID, message.getDst().getId());
 		values.put(MessageSQLiteHelper.COLUMN_KIND, message.getKind());
 		// TODO object msg?
 		values.put(MessageSQLiteHelper.COLUMN_DATA, (String) message.getData());
 		values.put(MessageSQLiteHelper.COLUMN_MSG_ID, message.getId());
 		long insertId = database.insert(MessageSQLiteHelper.TABLE_MESSAGE,
 				null, values);
 		Cursor cursor = database.query(MessageSQLiteHelper.TABLE_MESSAGE,
 				allColumns, MessageSQLiteHelper.COLUMN_ID + " = '" + insertId
 						+ "'", null, null, null, null);
 		cursor.moveToFirst();
 		Message newMessage = cursorToMessage(cursor);
 		cursor.close();
 
 		Log.i(Constants.AppCoreTag, "new message created = " + message);
 		return newMessage;
 	}
 
 	public List<Message> getAllMessagesWithUser(User user) {
 		Log.v(Constants.AppCoreTag, "getAllMessagesWithUser user = " + user);
 
 		List<Message> msgs = new ArrayList<Message>();
 
 		String orderBy = MessageSQLiteHelper.COLUMN_MSG_ID + " ASC";
 
 		Cursor cursor = database.query(MessageSQLiteHelper.TABLE_MESSAGE,
 				allColumns, MessageSQLiteHelper.COLUMN_SRC_ID + "= ? OR "
 						+ MessageSQLiteHelper.COLUMN_DES_ID + "= ?",
 				new String[] { user.getId(), user.getId() }, null, null,
 				orderBy);
 
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()) {
 			Message msg = cursorToMessage(cursor);
 			msgs.add(msg);
 			cursor.moveToNext();
 		}
 		// Make sure to close the cursor
 		cursor.close();
 
 		Log.v(Constants.AppCoreTag, "totally reteive %d messages" + msgs.size());
 
 		return msgs;
 	}
 
 	public List<Message> getAllMessages() {
 		List<Message> msgs = new ArrayList<Message>();
 
 		Cursor cursor = database.query(AccountSQLiteHelper.TABLE_ACCOUNT,
 				allColumns, null, null, null, null, null);
 
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()) {
 			Message msg = cursorToMessage(cursor);
 			msgs.add(msg);
 			cursor.moveToNext();
 		}
 		// Make sure to close the cursor
 		cursor.close();
 		return msgs;
 	}
 
 	private Message cursorToMessage(Cursor cursor) {
 		User srcUser = accountDataSource.getUserById(cursor.getString(1));
 		User dstUser = accountDataSource.getUserById(cursor.getString(2));
 
 		Message msg = new Message(srcUser, dstUser, cursor.getString(3),
 				cursor.getString(4), cursor.getInt(5));
 
 		return msg;
 	}
 }
