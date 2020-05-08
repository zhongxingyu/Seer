 package be.rottenrei.android.lib.model;
 
 import java.sql.SQLException;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 import be.rottenrei.android.lib.R;
 import be.rottenrei.android.lib.util.ExceptionUtils;
 
 import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
 import com.j256.ormlite.support.ConnectionSource;
 import com.j256.ormlite.table.TableUtils;
 
 /**
  * Base class for DatabaseOpenHelpers.
  * 
  * Manages a context and provides automatic version upgrades.
  */
 public abstract class DatabaseOpenHelperBase extends OrmLiteSqliteOpenHelper {
 
 	private final Context context;
 
 	public DatabaseOpenHelperBase(Context context, String databaseName, int version) {
 		super(context, databaseName, null, version);
 		this.context = context;
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase database, ConnectionSource source) {
 		Log.w(DatabaseOpenHelperBase.class.getName(), "Creating database");
 		try {
 			for (Class<?> modelClass : getMigrationManager().getModelClasses()) {
 				TableUtils.createTable(source, modelClass);
 			}
 			getMigrationManager().populateNewDatabase(database, source);
 		} catch (SQLException e) {
 			ExceptionUtils.handleFatalExceptionWithMessage(e, getContext(), R.string.error_db_migrate, DatabaseOpenHelperBase.class);
 		}
 	}
 
 	abstract protected IMigrationManager getMigrationManager();
 
 	@Override
 	public void onUpgrade(SQLiteDatabase database, ConnectionSource source, int oldVersion, int newVersion) {
 		Log.w(DatabaseOpenHelperBase.class.getName(), "Upgrading database from version "
 				+ oldVersion + " to " + newVersion);
 		try {
 			getMigrationManager().upgradeFrom(database, source, oldVersion);
 		} catch (SQLException e) {
 			ExceptionUtils.handleFatalExceptionWithMessage(e, getContext(), R.string.error_db_migrate, DatabaseOpenHelperBase.class);
 		}
 	}
 
 	public Context getContext() {
 		return context;
 	}
 
 }
