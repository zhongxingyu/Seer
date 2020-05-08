 package to.rcpt.quest;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Loader;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.net.Uri;
 import android.provider.BaseColumns;
 import android.util.Log;
 
 import com.commonsware.cwac.loaderex.SQLiteCursorLoader;
 
 public final class Metadata {
 	public static String TABLE_NAME = "Metadata";
 	private static String FILE_NAME = "metadata.db";
 
 	private Metadata() {
 	}
 
 	/** Database columns for image URIs */
 	public static abstract class Images {
 		/** Original source imagge */
 		public static final String ORIGINAL = "original";
 		/** Edge-detected image */
 		public static final String LINEARIZED = "linearized";
 		/** Solution image */
 		public static final String SOLUTION = "solution";
 		/** Clue image */
 		public static final String CLUE = "clue";
 	}
 
 	// TODO(dichro): just use reflection?
 	private static String[] SETUP_COMMANDS = { new SQLBuilder()
 			.createTable(TABLE_NAME).textColumn(Images.ORIGINAL)
 			.textColumn(Images.LINEARIZED).textColumn(Images.SOLUTION)
 			.textColumn(Images.CLUE).toString(), };
 
 	public static class Helper extends SQLiteOpenHelper {
 		private static final String TAG = ImageHandoffTask.class.getName();
 		private final Context context; // TODO(dichro): does this need a
 										// weakreference?
 
 		public Helper(Context context) {
 			super(context, FILE_NAME, null, SETUP_COMMANDS.length);
 			this.context = context;
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			executeSetupCommands(db, 0, SETUP_COMMANDS.length - 1);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			executeSetupCommands(db, oldVersion + 1, newVersion - 1);
 		}
 
 		private void executeSetupCommands(SQLiteDatabase db, int from, int to) {
 			for (int i = from; i <= to; i++) {
 				Log.i(TAG, "Executing: " + SETUP_COMMANDS[i]);
 				db.execSQL(SETUP_COMMANDS[i]);
 			}
 		}
 
 		public long newImage(Uri originalUri) {
 			ContentValues values = new ContentValues();
 			values.put(Images.ORIGINAL, originalUri.toString());
			SQLiteDatabase db = getWritableDatabase();
			try {
				return db.insert(TABLE_NAME, null, values);
			} finally {
				db.close();
			}
 		}
 
 		public void setLinearizedImage(long id, Uri uri) {
 			setImage(Images.LINEARIZED, id, uri);
 		}
 
 		public void setSolutionImage(long id, Uri uri) {
 			setImage(Images.SOLUTION, id, uri);
 		}
 
 		public void setClueImage(long id, Uri uri) {
 			setImage(Images.CLUE, id, uri);
 		}
 
 		private void setImage(String columnName, long id, Uri uri) {
 			ContentValues values = new ContentValues();
 			values.put(columnName, uri.toString());
			SQLiteDatabase db = getWritableDatabase();
			db.update(TABLE_NAME, values, BaseColumns._ID + " = ?",
 					new String[] { String.valueOf(id) });
			db.close();
 		}
 
 		public Loader<Cursor> getLoader() {
 			return new SQLiteCursorLoader(context, this,
 					SQLBuilder.select(TABLE_NAME, BaseColumns._ID,
 							Images.ORIGINAL, Images.LINEARIZED,
 							Images.SOLUTION, Images.CLUE).toString(), null);
 		}
 	}
 
 	static class SQLBuilder {
 		protected final StringBuilder leading, trailing;
 
 		SQLBuilder() {
 			leading = new StringBuilder();
 			trailing = new StringBuilder();
 		}
 
 		@Override
 		public String toString() {
 			return leading.append(trailing.toString()).toString();
 		}
 
 		SQLBuilder createTable(String tableName) {
 			leading.append("CREATE TABLE ").append(tableName).append(" (");
 			trailing.append(BaseColumns._ID).append(" INTEGER PRIMARY KEY")
 					.append(")");
 			return this;
 		}
 
 		SQLBuilder textColumn(String columnName) {
 			leading.append(columnName).append(" TEXT,");
 			return this;
 		}
 
 		static SQLBuilder select(String tableName, String firstColumn,
 				String... otherColumns) {
 			StringBuilder columns = new StringBuilder();
 			for (String column : otherColumns) {
 				columns.append(",").append(column);
 			}
 			SQLBuilder sb = new SQLBuilder();
 			sb.leading.append("SELECT ").append(firstColumn)
 					.append(columns.toString()).append(" FROM ")
 					.append(tableName);
 			return sb;
 		}
 	}
 }
