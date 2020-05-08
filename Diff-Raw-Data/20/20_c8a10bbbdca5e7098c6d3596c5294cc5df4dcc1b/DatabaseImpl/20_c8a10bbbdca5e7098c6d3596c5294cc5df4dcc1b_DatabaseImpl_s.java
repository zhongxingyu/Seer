 package pro.trousev.cleer.android.service;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 import android.database.DatabaseErrorHandler;
 import pro.trousev.cleer.Database;
 import pro.trousev.cleer.Database.DatabaseError;
 
 public class DatabaseImpl implements Database {
 	public SQLiteDatabase db;
 
 	// constructor
 	public DatabaseImpl(String path, Context context) {
 		DBHelper dbHelper = new DBHelper(path, context);
 		// open last or create new database with path
 		this.db = dbHelper.getWritableDatabase();
 
 	}
 
 	public class DBHelper extends SQLiteOpenHelper {
		public String section = "default";
 
 		public DBHelper(String path, Context context) {
 			// конструктор суперкласса
 			super(context, path, null, 1);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL("create table " + "section_default " + "("
 					+ "id integer primary key autoincrement," + "value text,"
 					+ "search text" + " keywords text" + ");");
 
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
 		}
 	}
 
 	public class DatabaseObject implements Database.DatabaseObject {
 		private String id = null;
 		private String contents = null;
 		private DatabaseImpl parent = null;
 		private String section = null;
 		private String search = null;
 
 		// constructor number 1
 		public DatabaseObject(String section, DatabaseImpl parent, String id,
 				String contents) {
 			this.parent = parent;
 			this.id = id;
 			this.contents = contents;
 			this.section = section;
 		}
 
 		// constructor number 2
 		public DatabaseObject(DatabaseImpl parent, String id) {
 			this.parent = parent;
 			this.id = id;
 		}
 
 		// return identificator
 		public String id() {
 			return this.id;
 		}
 
 		// return content of object
 		public String contents() {
 
 			if (this.contents == null) {
 				try {
 					// values for query
 					String[] columns = { "value" };
 					String selection = "id = " + this.id;
 					// cursor
 					Cursor c = db.query(this.section, columns, selection, null,
 							null, null, null);
 					this.contents = c.getColumnName(0);
 				} catch (SQLException e) {
 					e.printStackTrace();
 					return null;
 				}
 			}
 			return this.contents;
 
 		}
 
 		// return search context
 		public String search() {
 			return this.search;
 
 		}
 
 		/**
 		 * Метод, обновляющий объект и заменяющий и содержимое, и поисковой
 		 * контекст.
 		 * 
 		 * Если contents или search выставлены в null, то соответствующий объект
 		 * изменяться не должен.
 		 * 
 		 * @throws DatabaseError
 		 **/
 		// update object, change content and search
 		public boolean update(String contents, String search)
 				throws DatabaseError {
 			ContentValues cv = new ContentValues();
 			cv.put("value", contents);
 			cv.put("search", search);
 
 			if (contents == null || search == null) {
 				return true;
 			}
 			try {
 				int updCount = db.update(this.section, cv, "id = " + this.id,
 						new String[] { id });
 			} catch (SQLException e) {
 				throw new DatabaseError(e);
 			}
 
 			return true;
 
 		}
 
 		// update contents
 		public boolean update_contents(String contents) throws DatabaseError {
 			ContentValues cv = new ContentValues();
 			cv.put("value", contents);
 
 			if (contents == null) {
 				return true;
 			}
 			try {
 				int updCount = db.update(this.section, cv, "id = " + this.id,
 						new String[] { id });
 			} catch (SQLException e) {
 				throw new DatabaseError(e);
 			}
 
 			return true;
 
 		}
 
 		// update search
 		public boolean update_search(String search) throws DatabaseError {
 			ContentValues cv = new ContentValues();
 			cv.put("search", search);
 
 			if (search == null) {
 				return true;
 			}
 			try {
 				int updCount = db.update(this.section, cv, "id = " + this.id,
 						new String[] { id });
 			} catch (SQLException e) {
 				throw new DatabaseError(e);
 			}
 
 			return true;
 
 		}
 	}
 
 	// create section
 	@Override
 	public boolean declare_section(String section) throws DatabaseError {
 		try {
			db.execSQL("create table " + section + "("
					+ "id integer primary key autoincrement," + "value text,"
					+ "search text" + "keywords text" + ");");
 		} catch (SQLException e) {
 			throw new DatabaseError(e);
 		}
 		return true;
 	}
 
 	// delete section
 	@Override
 	public boolean clear_section(String section) throws DatabaseError {
 		try {
			db.delete(section, null, null);
 		} catch (SQLException e) {
 			throw new DatabaseError(e);
 		}
 		return true;
 	}
 
 	// insert object into section
 	@Override
 	public pro.trousev.cleer.Database.DatabaseObject store(String section,
 			String contents, String keywords) throws DatabaseError {
 		keywords = keywords.toLowerCase();
 		keywords = keywords.replace("'", "''");
 		contents = contents.replace("'", "''");
 		try {
			db.execSQL("insert into" + section + "(value, keywords) values ("
 					+ contents + ", " + keywords + ");");
 		} catch (SQLException e) {
 			throw new DatabaseError(e);
 		}
 		return null;
 	}
 
 	private String languageMatch(String query) {
 		return String.format("search LIKE '%%%s%%'", query);
 	}
 
 	private String languageLike(String query) {
 		String[] keywords = query.split(" ");
 		String where = null;
 		for (String word : keywords) {
 			word = word.replace("'", "''");
 			word = word.toLowerCase();
 			String clause = String.format("search LIKE '%%%s%%'", word);
 			if (where == null)
 				where = clause;
 			else
 				where += " AND " + clause;
 		}
 		return where;
 	}
 
 	private String languagePyplay(String query) {
 		query = query.replaceAll("\\@", "artist:");
 		query = query.replaceAll("\\$", "album:");
 		query = query.replaceAll("\\!", "title:");
 		query = query.replaceAll("\\^", "genre:");
 		query = query.replaceAll("N\\/R", "rating:0");
 		query = query.replaceAll("\\{0\\}", "rating:0");
 		query = query.replaceAll("\\{1\\}", "rating:1");
 		query = query.replaceAll("\\{2\\}", "rating:2");
 		query = query.replaceAll("\\{3\\}", "rating:3");
 		query = query.replaceAll("\\{4\\}", "rating:4");
 		query = query.replaceAll("\\{5\\}", "rating:5");
 		String where = "";
 		for (String orList : query.split(" ")) {
 			orList = orList.trim();
 			if (orList.isEmpty())
 				continue;
 			String orClause = "";
 			for (String item : orList.split(",")) {
 				if (!orClause.isEmpty())
 					orClause += " OR ";
 				orClause += String.format("search LIKE '%%%s%%'", item);
 			}
 			if (orClause.isEmpty())
 				continue;
 			if (!where.isEmpty())
 				where += " AND ";
 			where += String.format(" ( %s ) ", orClause);
 		}
 		return where;
 	}
 
 	// search element with particular query, defaultLanguage = SearchSqlLike
 	@Override
 	public List<pro.trousev.cleer.Database.DatabaseObject> search(
 			String section, String query) throws DatabaseError {
 
 		return search(section, query, SearchLanguage.SearchSqlLike);
 	}
 
 	// search element with particular query, language = variety
 	@Override
 	public List<pro.trousev.cleer.Database.DatabaseObject> search(
 			String section, String query, SearchLanguage language)
 			throws DatabaseError {
 
 		String where = "";
 		if (language == SearchLanguage.SearchDirectMatch)
 			where = languageMatch(query);
 		if (language == SearchLanguage.SearchSqlLike)
 			where = languageLike(query);
 		if (language == SearchLanguage.SearchPyplay)
 			where = languagePyplay(query);
 		try {
 			return pSearch(section, where);
 		} catch (SQLException e) {
 			throw new DatabaseError(e);
 		}
 	}
 
 	public List<pro.trousev.cleer.Database.DatabaseObject> pSearch(
 			String section, String where) throws DatabaseError {
 
 		List<pro.trousev.cleer.Database.DatabaseObject> answer = new ArrayList<Database.DatabaseObject>();
 		Cursor c;
 
 		String[] columns = { "id", "value" };
 
 		c = db.query(section, columns, where, null, null, null, null);
 		while (c.moveToNext()) {
 			answer.add(new DatabaseObject(section, this, c.getColumnName(0), c
 					.getColumnName(1)));
 		}
 		c.close();
 		return answer;
 	}
 
 	// remove object from database (i.e. from particular section)
 	@Override
 	public boolean remove(String section,
 			pro.trousev.cleer.Database.DatabaseObject object)
 			throws DatabaseError {
 		try {
			db.execSQL("delete from " + section + "where id = " + object.id()
 					+ ";");
 		} catch (SQLException e) {
 			throw new DatabaseError(e);
 		}
 		return true;
 	}
 
 	// close database
 	@Override
 	public void close() throws DatabaseError {
 		db.close();
 
 	}
 
 	// begin transaction
 	@Override
 	public boolean begin() throws DatabaseError {
 		try {
 			db.beginTransaction();
 		} catch (SQLException e) {
 			throw new DatabaseError(e);
 		}
 		return true;
 	}
 
 	// commit transaction
 	@Override
 	public boolean commit() throws DatabaseError {
 		try {
 			db.setTransactionSuccessful();
 		} catch (SQLException e) {
 			throw new DatabaseError(e);
 		}
 		return true;
 	}
 
 	// rollback transaction
 	@Override
 	public boolean rollback() throws DatabaseError {
 		try {
 			db.endTransaction();
 		} catch (SQLException e) {
 			throw new DatabaseError(e);
 		}
 		return true;
 	}
 
 }
