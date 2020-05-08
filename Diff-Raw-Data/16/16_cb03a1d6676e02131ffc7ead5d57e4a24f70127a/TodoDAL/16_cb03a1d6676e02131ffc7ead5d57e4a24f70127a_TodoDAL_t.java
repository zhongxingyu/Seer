 package il.ac.huji.todolist;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.parse.FindCallback;
 import com.parse.Parse;
 import com.parse.ParseACL;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.ParseUser;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 public class TodoDAL {
 	private final DBHelper dbHelper;
 	private static final String TABLE_NAME = "todo";
	private static final Long DB_NULL_DATE = 0L;
 
 	public TodoDAL(Context context) {
 		dbHelper = new DBHelper(context);
 		initParse(context);
 	}
 
 	public boolean insert(ITodoItem todoItem) {
 		if (todoItem.getTitle() == null) {
 			return false;
 		}
 		final ContentValues values = new ContentValues(1);
 		values.put("title", todoItem.getTitle());
		Long due = DB_NULL_DATE;
 		if (todoItem.getDueDate() != null) {
 			due = todoItem.getDueDate().getTime();
 		}
 		values.put("due", due);
 		Object resO = doDBAction(new IDBAction() {
 
 			@Override
 			public Object action(SQLiteDatabase db) {
 				long rowId = db.insert(TABLE_NAME, null, values);
 				return rowId != -1;
 			}
 		}, true);
 
 		if (resO == null) {
 			return false;
 		}
 		boolean res = (Boolean) resO;
 		if (res) {
 			parseAdd(todoItem);
 		}
 		return res;
 	}
 
 	private void parseAdd(ITodoItem todoItem) {
		Long due = DB_NULL_DATE;
 		if (todoItem.getDueDate() != null) {
 			due = todoItem.getDueDate().getTime();
 		}
 		ParseObject parseItem = new ParseObject("todo");
 		parseItem.put("title", todoItem.getTitle());
 		parseItem.put("due", due);
 		parseItem.setACL(new ParseACL(ParseUser.getCurrentUser()));
 		parseItem.saveInBackground();
 	}
 
 	public boolean update(final ITodoItem todoItem) {
 		if (todoItem.getTitle() == null) {
 			return false;
 		}
 		final ContentValues values = new ContentValues(1);
		Long due = DB_NULL_DATE;
 		if (todoItem.getDueDate() != null) {
 			due = todoItem.getDueDate().getTime();
 		}
 		values.put("due", due);
 		Object resO = doDBAction(new IDBAction() {
 
 			@Override
 			public Object action(SQLiteDatabase db) {
 				int rowsAffected = db.update(TABLE_NAME, values, "title = ?", new String[] { todoItem.getTitle() });
 				return rowsAffected > 0;
 			}
 		}, true);
 
 		if (resO == null) {
 			return false;
 		}
 		boolean res = (Boolean) resO;
 		if (res) {
 			parseUpdate(todoItem);
 		}
 		return res;
 	}
 
 	private void parseUpdate(ITodoItem todoItem) {
		Long dueT = DB_NULL_DATE;
 		if (todoItem.getDueDate() != null) {
 			dueT = todoItem.getDueDate().getTime();
 		}
 		final Long due = dueT;
 		ParseQuery query = new ParseQuery("todo");
 		query.whereEqualTo("title", todoItem.getTitle());
 		query.findInBackground(new FindCallback() {
 			@Override
 			public void done(List<ParseObject> objects, ParseException e) {
 				if (e == null && objects.size() > 0) {
 					for (int i = 1; i < objects.size(); i++) {
 						objects.get(i).deleteInBackground();
 					}
 					objects.get(0).put("due", due);
 				}
 			}
 		});
 	}
 
 	public boolean delete(final ITodoItem todoItem) {
 		if (todoItem.getTitle() == null) {
 			return false;
 		}
 		Object resO = doDBAction(new IDBAction() {
 
 			@Override
 			public Object action(SQLiteDatabase db) {
 				int rowsAffected = 0;
 				rowsAffected = db.delete(TABLE_NAME, "title = ?", new String[] { todoItem.getTitle() });
 				return rowsAffected > 0;
 			}
 		}, true);
 
 		if (resO == null) {
 			return false;
 		}
 		boolean res = (Boolean) resO;
 		if (res) {
 			parseDelete(todoItem);
 		}
 		return res;
 	}
 
 	private void parseDelete(ITodoItem todoItem) {
 		ParseQuery query = new ParseQuery("todo");
 		query.whereEqualTo("title", todoItem.getTitle());
 		query.findInBackground(new FindCallback() {
 			@Override
 			public void done(List<ParseObject> objects, ParseException e) {
 				if (e == null && objects.size() > 0) {
 					for (ParseObject o : objects) {
 						o.deleteInBackground();
 					}
 				}
 			}
 		});
 	}
 
 	public List<ITodoItem> all() {
 		Object res = doDBAction(new IDBAction() {
 
 			@Override
 			public Object action(SQLiteDatabase db) {
 				List<ITodoItem> items = new ArrayList<ITodoItem>();
 				Cursor cursor = db.query("todo", new String[] { "title", "due" }, null, null, null, null, null);
 				if (cursor.moveToFirst()) {
 					do {
 						String title = cursor.getString(0);
 						Long due = cursor.getLong(1);
 						Date dueDate = null;
						if (due != DB_NULL_DATE) {
 							dueDate = new Date(due);
 						}
 						items.add(new TodoItem(title, dueDate));
 					} while (cursor.moveToNext());
 				}
 				return items;
 			}
 		});
 		if (res == null) {
 			return new ArrayList<ITodoItem>();
 		}
 		@SuppressWarnings("unchecked")
 		List<ITodoItem> items = (List<ITodoItem>) res;
 		return items;
 	}
 
 	private Object doDBAction(IDBAction action) {
 		return doDBAction(action, false);
 	}
 
 	private Object doDBAction(IDBAction action, boolean writes) {
 		Object res = null;
 		SQLiteDatabase db = writes ? dbHelper.getWritableDatabase() : dbHelper.getReadableDatabase();
 		db.beginTransaction();
 		try {
 			res = action.action(db);
 			db.setTransactionSuccessful();
 		} finally {
 			db.endTransaction();
 		}
 		return res;
 	}
 
 	private void initParse(Context context) {
 		Resources resources = context.getResources();
 		String applicationId = resources.getString(R.string.parseApplication);
 		String clientKey = resources.getString(R.string.clientKey);
 		Parse.initialize(context, applicationId, clientKey);
 		ParseUser.enableAutomaticUser();
 		if (ParseUser.getCurrentUser() == null) {
 			ParseUser.getCurrentUser().saveInBackground();
 		}
 	}
 }
