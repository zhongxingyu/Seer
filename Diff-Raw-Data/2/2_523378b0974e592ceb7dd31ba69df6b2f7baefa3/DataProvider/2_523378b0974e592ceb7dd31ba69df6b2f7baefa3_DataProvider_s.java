 package edu.wsn.phoneusage.db;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.wsn.phoneusage.main.SystemInfo;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 import android.util.Pair;
 import android.widget.ArrayAdapter;
 
 /**
  * DataProvider maintains a SQLite DB table as data storage device for one entity.
  * The entity is specified in the derived specific DataProvider class.
  * 
  * @author Lothar Rubusch
  */
 // TODO implement as Chain Of Responsibility, with MainActivity having only one
 // pointer to the initial element, DB thus may be static, or / and passed to
 // the next elements
 public abstract class DataProvider{
 	protected String TAG = "DataProvider";
 
 	/*
 	 * db settings
 	 * 
 	 * there is ONE database, and per derived instance, some tables
 	 */
 	protected String _tablename = "";
 	protected ArrayList<Pair<String, String>> _tableargs;
 
 	private Context _context;
 
 	/*
 	 * if table name has changed, make sure to have cleanly de-installed the app 
 	 * before the next installation, remaining DB artefacts may break the
 	 * program, if not
 	 */
 
 	// db col names
 	protected static final String KEY_ID = "id";
 	protected static final String KEY_TIMESTAMP = "timestamp";
 	protected static final String KEY_VALUE = "value";
 
 	// db handler as priv class
 	protected class DbHelper extends SQLiteOpenHelper {
 		protected static final int DATABASE_VERSION = 2;
 		protected static final String DATABASE_NAME = SystemInfo.DB_NAME;
 
 		public DbHelper( Context context ){
 			super( context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db){
 			Log.d(SystemInfo.TIG, TAG + "::onCreate()");
 			if( 0 == tablename().length() ){
 				Log.e( SystemInfo.TIG,  TAG + "DbHelper::onCreate - no tablename provided");
 			}
 
 			// set up new table, if not existed before
 			String sql = "CREATE TABLE IF NOT EXISTS "
 					+ tablename()
 					+ "( " + KEY_ID   + " INTEGER PRIMARY KEY"
 					+ " , " + KEY_TIMESTAMP + " TEXT";
 
 			Pair<String, String> element;
 			for(int idx = 0; idx < _tableargs.size(); ++idx ){
 				element = _tableargs.get( idx );
 //				Log.d(SystemInfo.TIG, TAG + " ---> tableargs: " + element.first + ", " + element.second );  // XXX
 				sql += " , " + element.first + " " + element.second;
 			}
 
 			sql += " )";
 //			Log.d( SystemInfo.TIG, TAG + " - XXX create table:\n" + sql); // XXX
 			db.execSQL( sql );
 		}
 
 		public void onCreate(){
 			onCreate( db().getWritableDatabase() ); // FIXME NullPointerException
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int old_version, int new_version) {
 			Log.w( SystemInfo.TIG, "upgrade db from version " + old_version 
 					+ " to "                      + new_version 
 					+ ", which will destroy all old data");
 			db.execSQL( "DROP TABLE IF EXISTS " + tablename() );
 
 			onCreate( db );
 		}
 
 		/*
 		 * CRUD funcs
 		 * C-reate
 		 * R-ead
 		 * U-update
 		 * D-elete
 		 */
 		public void addMeasurement(DataElement c) {
 			SQLiteDatabase db = this.getWritableDatabase();
 			ContentValues vals = new ContentValues();
 
 			vals.put( KEY_TIMESTAMP, c.getTimestamp() );
 
 //			Log.d(SystemInfo.TIG, TAG + " XXX - DataElement " + c.toString()); // XXX
 
 			for( int idx=0; idx < _tableargs.size(); ++idx){
 // FIXME always null
 				Log.d(SystemInfo.TIG, TAG + " - XXX element: " + _tableargs.get( idx ).first + " - " + c.getValue( _tableargs.get(idx).first )); // FIXME second value NULL
 //				vals.put( _tableargs.get( idx ).first, c.getValue( _tableargs.get(idx).first )); // XXX
 				vals.put( _tableargs.get( idx ).first, c.getValue( _tableargs.get(idx).first ));
 			}
 
 			try{
 				synchronized(this){
 //					for( int i = 0; i< _tableargs.size(); ++i){
 //						Log.d(SystemInfo.TIG, TAG + " - XXX _tableargs: '" + _tableargs.get( i ).first + "'"); // XXX
//					}
 					
 //					Log.d(SystemInfo.TIG, TAG + " XXX tablename: " + tablename()); // XXX
 					db.insert( tablename(), null, vals);
 					db.close();
 				}
 			}catch( IllegalStateException e){
 				Log.w(SystemInfo.TIG, TAG + "::addMeasurement_battery() - caught IllegalStateException, ignore it for now - trouble ahead! ");
 			}
 		}
 
 		public List<DataElement> getAllMeasurements(){
 			SQLiteDatabase db = this.getWritableDatabase();
 
 			List<DataElement> measurementlist = new ArrayList<DataElement>();
 			String sql = "SELECT * FROM " + tablename();
 			Cursor cursor = db.rawQuery( sql, null);
 
 			// iterate over returned list elems
 			if( cursor.moveToFirst() ){
 				do{
 					DataElement measurement = new DataElement();
 					measurement.setID( Integer.parseInt( cursor.getString(0) ) );
 					measurement.setTimestamp( cursor.getString(1) );
 					for( int idx=0; idx < _tableargs.size(); ++idx ){
 						measurement.setValue( _tableargs.get(idx).first, cursor.getString(2) );
 					}
 					measurementlist.add( measurement );
 				}while( cursor.moveToNext());
 			}
 			cursor.close();
 			return measurementlist;
 		}
 
 		public int getMeasurementsCount( String tablename ){
 			// total number of tables in db
 			SQLiteDatabase db = this.getReadableDatabase();
 			String sql = "SELECT * FROM " + tablename;
 			Cursor cursor = db.rawQuery( sql, null );
 			int count = cursor.getCount(); 
 			cursor.close();
 			return count;
 		}
 
 		public void deleteTable( String tablename ){
 			SQLiteDatabase db = this.getWritableDatabase();
 			db.delete( tablename, null, null);
 			db.close();
 		}
 	};
 
 	protected DbHelper _db;
 	
 	public DbHelper db(){
 		return this._db;
 	}
 	
 	public void setdb( DbHelper db ){
 		this._db = db;
 	}
 
 	
 	/*
 	 * ctor
 	 */
 	public DataProvider( Context context, DbHelper db ){
 		this.setContext( context );
 		if( null == db ){
 			this._db = new DbHelper( context() );
 			return;
 		}
 		this._db = db;
 	}
 
 
 	public abstract void generatePlotData( ArrayAdapter< String > arrayadapter, List<Number> xseries, List<Number> yseries );
 
 	public void data_save(DataElement element){
 		db().addMeasurement( element );
 	}
 
 	public List<DataElement> data(){
 		return db().getAllMeasurements();
 	}
 
 	public void data_reset(){
 		db().deleteTable( tablename() );
 	}
 
 	public int data_count(){
 		return db().getMeasurementsCount( tablename() );
 	}
 
 	public String tablename(){
 		return this._tablename;
 	}
 
 	protected void setContext( Context context ){
 		this._context = context;
 	}
 
 	protected Context context(){
 		return this._context;
 	}
 };
