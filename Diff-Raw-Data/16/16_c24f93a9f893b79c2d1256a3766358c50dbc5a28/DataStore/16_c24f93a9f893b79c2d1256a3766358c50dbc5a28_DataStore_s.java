 package uk.ac.cam.cl.dtg.android.time.BusTimetables;
 
 import java.io.Closeable;
 import java.util.ArrayList;
 import java.util.List;
 
 import uk.ac.cam.cl.dtg.android.time.Constants;
 import uk.ac.cam.cl.dtg.android.time.buses.BusStop;
 import uk.ac.cam.cl.dtg.android.time.data.TransportDataException;
 import uk.ac.cam.cl.dtg.android.time.data.TransportDataProvider;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 public class DataStore implements Runnable, Closeable {
 
 	SQLiteDatabase conn;
 	DataStoreHelper dsh;
 	private ProgressDialog pd;
 	private int dataToDownload = 1;
   private Runnable finishedCallback;
   private Activity finishedContext;
 
 	/**
 	 * Creates a new DataStore instance
 	 * @param c Context the datastore is to be opened under
 	 * @param wantWriteAccess Whether write access is required
 	 */
 	public DataStore(Context c, boolean wantWriteAccess) {
 
 		dsh = new DataStoreHelper(c);
 
 		if(wantWriteAccess) {
 			conn = dsh.getWritableDatabase();
 		} else {
 			conn = dsh.getReadableDatabase();
 		}
 
 		Log.i("DataStore","Opening writeable connection... "+conn.getPath()+"/"+conn.getVersion());
 	}
 
 
 
 	/**
 	 * Closes the connection to the underlying database
 	 */
 	@Override
   public void finalize() throws Throwable {
 		conn.close();
 		super.finalize();
   }
 
   /**
    * Closes the database
    */
   @Override
   public void close() {
     conn.close();
   }
 
 	/**
 	 * Returns the number of bus stops held in the internal database
 	 * @return Total number of bus stops
 	 */
 	public int getBusStopCount() {
 
 		String sql = "SELECT count(*) FROM busstops";
 		Cursor c = conn.rawQuery(sql, null);
     try {
       c.moveToFirst();
       int val = c.getInt(0);
 
       return val;
     } finally {
       c.close();
     }
 	}
 
 	/**
 	 * Marks a bus stop as starred in the database
 	 * @param b BusStop to mark as starred. stopRef must be filled in.
 	 */
 	public void setFavourite(BusStop b) {
 
 		// In case it's already there
 		clearFavourite(b);
 
 		String sql = "INSERT INTO favourites (stopRef) VALUES ("
 			+ "\"" + b.getAtcoCode()
 			+ "\")";
 
 		Log.i("DataStore","Saving favourite: SQL is: "+sql);
 
 		conn.execSQL(sql);
 	}
 
 	/*
 	 * TODO: not saving sms code yet
 	 * that is something for busstopdialog to worry about
 	 */
 
 	/**
 	 * Marks a bus stop as not starred.
 	 * @param stop Bus stop to be unmarked as starred. stopRef must be set.
 	 */
 	public void clearFavourite(BusStop b) {
 
 		String sql = "DELETE FROM favourites WHERE stopRef='"+b.getAtcoCode()+"'";
 
 		Log.i("DataStore","Clearing favourite: SQL is: "+sql);
 		conn.execSQL(sql);
 	}
 
 	/**
 	 * Returns whether a bus stop is starred or not.
 	 * @param b Bus stop to return starred status for. stopRef must be set
 	 * @return
 	 */
 	public boolean isFavourite(BusStop b) {
 
 		String sql = "SELECT * FROM favourites WHERE stopRef='"+b.getAtcoCode()+"'";
 
 		Cursor results = conn.rawQuery(sql, null);
 		boolean ret = (results.getCount()==0) ? false : true;
 
 		results.close();
 
 		return ret;
 	}
 
 	/**
 	 * Empties the internal database of bus stops.
 	 */
 	public void clearStops() {
 		try {
 			conn.execSQL("DELETE FROM busstops WHERE 1");
 		} catch(SQLiteException e) {
 			Log.e("DataStore",e.getMessage());
 		}
 	}
 
 	/**
 	 * Inserts every bus stop held in the given list of stops into the internal database.
 	 * @param stops List of bus stops
 	 * @param dataSet String representation of the data set the stops came from
 	 */
	public void insertStops(List<BusStop> stops, int dataSet) {
 
 		Log.i("UpdateDB","Attempting to insert "+stops.size()+" stops into DB");
 
 		conn.beginTransaction();
 
 		Log.i("UpdateDB","Transation begun");
 		try {
 
 			for(BusStop stop : stops) {
 				ContentValues values = new ContentValues();
 				values.put("stopRef", stop.getAtcoCode());
 				values.put("stopName", stop.getName());
 				values.put("stopSMS", stop.getNaptanCode());
 				values.put("latE6",(int)(stop.getLatitude() * 1E6));
 				values.put("longE6", (int)(stop.getLongitude() * 1E6));
 				values.put("dataSet",dataSet);
 
 				conn.insert("busstops", null, values);
 
 				//Log.i("InsertStop",sql);
 			}
 
 			conn.setTransactionSuccessful();
 
 			Log.i("UpdateDB","Finished inserts, Committed.");
 
 		} catch(SQLiteException e) {
 			Log.e("DataStore",e.getMessage());
 		} finally {
 			conn.endTransaction();
 		}
 	}
 
 	/**
 	 * Returns a list of the n closest bus stops to the given position.
 	 * @param latE6 Latitude of given position, in degrees * 1E6
 	 * @param longE6 Longitude of given position, in degrees * 1E6
 	 * @param howMany How many bus stops to return
 	 * @return
 	 */
 	public List<BusStop> findNearestStops(int latE6, int longE6, int howMany) {
 
 		String sql = "SELECT busstops.stopName, busstops.stopRef, busstops.latE6, busstops.longE6, busstops._id, favourites._id as favid, "
 			+ "((" + latE6 + "-latE6)*(" + latE6 + "-latE6) + (" + longE6 + "-longE6)*(" + longE6 + "-longE6)) AS dist "
 			+ "FROM busstops LEFT OUTER JOIN favourites ON favourites.stopref = busstops.stopref ORDER BY dist ASC LIMIT " + howMany;
 
 		Log.i("DataStore","Querying for nearby: "+sql);
 
 		return queryForStops(sql);
 
 	}
 
 	/**
 	 * Queries the database for stops using the SQL query given and returns results as a List<BusStop>
 	 * @param sql SQL query
 	 * @return
 	 */
 	private List<BusStop> queryForStops(String sql) {
 
 		Log.i("QueryForStops","SQL is: "+sql);
 		Cursor results = conn.rawQuery(sql, null);
     try {
       ArrayList<BusStop> temp = new ArrayList<BusStop>();
 
       // Only if we succeeded - not if empty
       if (results.moveToFirst()) {
 
         while (true) {
           if (results.isAfterLast())
             break;
           // Log.i("queryForStops","STOP FROM DB: "+results.getString(0)+"/"+results.getString(1));
           BusStop s =
               new BusStop(results.getString(0), results.getInt(2), results.getInt(3), results
                   .getString(1));
           s.setMeta("_ID", results.getInt(4));
           if (results.getInt(5) > 0)
             s.setMeta("FavID", results.getInt(5));
           temp.add(s);
           results.moveToNext();
         }
       }
 
       return temp;
     } finally {
       results.close();
     }
 
 	}
 
 	/**
 	 * Returns list of with a name similar to the query given.
 	 * @param query Name to match against
 	 * @return
 	 */
 	public List<BusStop> findStopsLike(String query) {
 
 		// Less than 2 characters? Return nothing as too expensive
 		if(query.length() < 3)
 		{
 			return new ArrayList<BusStop>();
 		}
 
 		// Add wildcards before, after, and in spaces
 		query = "%" + query + "%";
 		query = query.replace(' ','%');
 
 		// Construct SQL
 		String sql = "SELECT busstops.stopName, busstops.stopRef, busstops.latE6, busstops.longE6, busstops._id, favourites._id as favid FROM busstops LEFT OUTER JOIN favourites ON favourites.stopref = busstops.stopref WHERE stopName LIKE \"" + query + "\" ORDER BY stopName";
 		Log.i("DataStore","Querying DB with: "+sql);
 		return queryForStops(sql);
 
 	}
 
 
 	/**
 	 * Gets a bus stop by its stopRef
 	 * @param ref stopRef of desired bus stop
 	 * @return The bus stop with matching stopRef
 	 * @throws DataStoreException
 	 */
 	public BusStop getStop(String ref) throws DataStoreException {
 
 		String sql = "SELECT busstops.stopName, busstops.stopRef, busstops.latE6, busstops.longE6, busstops._id, favourites._id as favid  FROM busstops LEFT OUTER JOIN favourites ON favourites.stopref = busstops.stopref WHERE busstops.stopRef=\"" + ref + "\"";
 		List<BusStop> r = queryForStops(sql);
 
 		if(r.size() == 0) throw new DataStoreException("No such stop in the database.");
 
 		return(r.get(0));
 
 	}
 
 	/**
 	 * Gets a bus stop by internal _ID
 	 * @param internalID internal _ID
 	 * @return The desired bus stop
 	 * @throws DataStoreException
 	 */
 	public BusStop getStopById(int internalID) throws DataStoreException {
 
 		String sql = "SELECT busstops.stopName, busstops.stopRef, busstops.latE6, busstops.longE6, busstops._id, favourites._id as favid   FROM busstops LEFT OUTER JOIN favourites ON favourites.stopref = busstops.stopref WHERE busstops._id=" + internalID;
 		List<BusStop> r = queryForStops(sql);
 
 		if(r.size() == 0) throw new DataStoreException("No such stop in the database.");
 
 		return(r.get(0));
 
 	}
 
 
 	/**
 	 * 
 	 * Clears the current version of the bus stop database held and re-downloads.
 	 */
 	public void updateDatabase(Activity con, Runnable finishedCallback) {
 
 		// Clear database first
 		clearStops();
 
 		Log.i("UpdateDB","Beginning database update");
 		pd = ProgressDialog.show(con, "Please wait...", "Downloading database. This may take a few minutes...", true, false);
 		pd.setIcon(0);
 		this.finishedContext = con;
 		this.finishedCallback = finishedCallback;
 		Thread thread = new Thread(this);
 		thread.start();
 
 
 	}
 
 	@Override
   public void run() {
 
 		// Load in new bus stops
 
 		// Create new TransportDataProvider
 		TransportDataProvider tdp = new TransportDataProvider(Constants.OMNIBUS_APIKEY, Constants.OMNIBUS_FEEDURL);
 
 		Log.i("UpdateDB","Hello from thread. Trying to DL set "+dataToDownload);
 
 		try {
 
 			// insert the markers into the database
 
 			for(int i = 1; i < 4; i++) {
 				
 				Message m;
 				m = Message.obtain();
 				m.arg1 = UPDATE;
 				m.obj = "Downloading and unpacking definitions ("+i+"/3)...";
 				handler.sendMessage(m);
 
 				List<BusStop> downloadedStops = tdp.getBusStops(i);
 
 				m = Message.obtain();
 				m.arg1 = UPDATE;
 				m.obj = "Installing definitions ("+downloadedStops.size()+" new)...";
 				handler.sendMessage(m);
 
 				Log.i("UpdateDB","Got "+downloadedStops.size()+" definitions");
 
				insertStops(downloadedStops, i);
 
 			}
 
 
 		} catch (TransportDataException e) {
 		  // TODO: We should tell the user about this.
 			Log.e("AppMain","Error downloading stop db: "+e.getMessage());
 			Throwable cause = e.getCause();
 			Log.e("AppMain", cause + " " + ((cause != null) ? Log.getStackTraceString(e.getCause()) : Log.getStackTraceString(e)));
 		}
 
 		handler.sendEmptyMessage(DISMIS);
 
 	}
 
 
 
 	private static final int DISMIS = 0;
 	private static final int UPDATE = 1;
 	private Handler handler = new Handler() {
 
 		@Override
     public void handleMessage(Message msg) {
 
       if (msg.arg1 == DISMIS) {
         pd.dismiss();
         if (finishedContext != null && finishedCallback != null){
         finishedContext.runOnUiThread(finishedCallback);
         }
       }
 
 			if(msg.arg1 == UPDATE) {
 				pd.setMessage((String)msg.obj);
 			}
 
 		}
 
 	};
 }
