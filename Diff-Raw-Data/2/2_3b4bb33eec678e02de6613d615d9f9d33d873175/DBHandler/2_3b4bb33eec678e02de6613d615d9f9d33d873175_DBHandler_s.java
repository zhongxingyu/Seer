 package com.gourmet6;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.annotation.TargetApi;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.os.Build;
 
 /**
  * A class which handles the database, i.e. gives methods to 
  * perform requests, add, update and delete data from the DB.
  * 
  * @author Group 6
  * @version 04.05.2013
  */
 public class DBHandler {
 	
 	/**
 	 * A helper class to manage database creation and version management. 
 	 */
 	private static class DBHelper extends SQLiteOpenHelper {
 
 		private static final String DB_NAME = "gourmet6.sqlite";
 		private static final String DB_DIR = "/data/data/com.gourmet6/databases/";
 		private static String DB_PATH = DB_DIR + DB_NAME;
 		public static final int DB_VERSION = 1;
 		private final Context ourContext;
 		private boolean createDatabase = false;
 
 		/**
 		 * Constructor
 		 * @param context of the activity
 		 */
 		public DBHelper(Context context)
 		{
 			super(context, DB_NAME, null, DB_VERSION);
 			this.ourContext = context;
 			DB_PATH = ourContext.getDatabasePath(DB_NAME).getAbsolutePath();
 		}
 		
 		/**
 		 * Initializes the DB if it does not exist.
 		 */
 		public void initializeBD()
 		{
 			this.getWritableDatabase();
 			if (createDatabase)
 			{
 				try
 				{
 					copyDB();
 				} catch (IOException e)
 				{
 					throw new Error("Error copying the DB on initialization");
 				}
 			}
 		}
 		
 		/**
 		 * Loads the DB by copying it from the assets folder.
 		 */
 		public void copyDB() throws IOException
 		{
 			this.close();
 			
 			InputStream input = ourContext.getAssets().open(DB_NAME);
 			OutputStream output = new FileOutputStream(DB_PATH);
 			byte[] buffer = new byte[1024];
 			int length;
 			while ((length = input.read(buffer)) > 0)
 			{
 				output.write(buffer, 0, length);
 			}
 			output.flush();
 			output.close();
 			input.close();
 			
 			this.getWritableDatabase().close();
 		}
 		
 		@Override
 		public void onCreate(SQLiteDatabase db)
 		{
 			this.createDatabase = true;
 		}
 		
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
 		{
 			// do nothing
 		}
 		
 		@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
 		@Override
 		public void onConfigure (SQLiteDatabase db)
 		{
 			db.setForeignKeyConstraintsEnabled(true);
 		}
 		
 		@Override
 		public void onOpen(SQLiteDatabase db)
 		{
 			super.onOpen(db);
 		}
 		
 		@Override
 		public synchronized void close() 
 		{
 			super.close();
 		}
 	}
 	
 	/* 
 	 *  Name constants
 	 */
 	
 	/* Tables */
 	private static final String TABLE_ALLERGEN = "allergen";
 	private static final String TABLE_CLIENT = "client";
 	private static final String TABLE_CUISINE = "cuisine";
 	private static final String TABLE_DISH = "dish";
 	private static final String TABLE_ORDER_DETAIL = "order_detail";
 	private static final String TABLE_ORDER_OVERVIEW = "order_overview";
 	private static final String TABLE_RESERVATION = "reservation";
 	private static final String TABLE_RESTAURANT = "restaurant";
 	private static final String TABLE_TIMETABLE = "timetable";
 	
 	/* Column names */
 	private static final String ALLERGEN = "alleName";
 	private static final String AVAIL = "avail";
 	private static final String CHAIN = "chainName";
 	private static final String CLIENT = "cliName";
 	private static final String DATETIME = "datetime";
 	private static final String DAY = "day";
 	private static final String DESCRIPTION = "description";
     private static final String DISH = "dishName";
     private static final String LAT = "lat";
     private static final String LONG = "long";
     private static final String MAIL = "mail";
     private static final String ORDER_NR = "orderNr";
     private static final String PRICE = "price";
     private static final String PRICE_CAT = "priceCat";
     private static final String PASSWORD = "password";
     private static final String QUANTITY = "quantity";
     private static final String RATING = "rating";
     private static final String RES = "resName";
     private static final String INVENTORY = "inventory";
     private static final String SEATS = "seats";
     private static final String STREET = "street";
     private static final String SUBTYPE = "subtype";
     private static final String TEL = "tel";
     private static final String TIME_OPEN = "timeOpen";
     private static final String TIME_CLOSE = "timeClose";
     private static final String TOWN = "town";
     private static final String TYPE = "type";
     private static final String VOTES = "votes";
     private static final String ZIP = "zip";
     
     /* helps managing the timetables given a certain day */
     static public HashMap<String,Integer> weekMap = new HashMap<String,Integer>(7);
     static {
     	weekMap.put("dimanche", 0);
     	weekMap.put("lundi", 1);
     	weekMap.put("mardi", 2);
     	weekMap.put("mercredi", 3);
     	weekMap.put("jeudi", 4);
     	weekMap.put("vendredi", 5);
     	weekMap.put("samedi", 6);
     }
     
 	protected SQLiteDatabase db;
 	protected DBHelper dbHelper;
 	
 	private boolean read = false;
 	private boolean write = false;
 	
 
     /*************************
      * 
      * General access methods.
      * 
      *************************/
     /**
      * Constructor
      * @param context of the activity it is called in
      */
 	public DBHandler(Context context)
 	{
 		this.dbHelper = new DBHelper(context);
 		this.dbHelper.initializeBD();
 	}
 	
 	/**
 	 * Opens the DB for reading only.
 	 * @throws SQLiteException if the DB cannot be accessed for reading
 	 */
 	private void openRead() throws SQLiteException
 	{
 		if (!this.read)
 		{
 			this.db = this.dbHelper.getReadableDatabase();
 			
 		}
 		this.read = true;
 	}
 	
 	/**
 	 * Opens the DB for reading and writing.
 	 * @throws SQLiteException if the DB cannot be accessed for reading
 	 */
 	private void openWrite() throws SQLiteException
 	{
 		if(!this.write)
 		{
 			this.db = this.dbHelper.getWritableDatabase();
 		}
 		this.write = true; this.read = true;
 	}
 	
 	/**
 	 * Closes the DB.
 	 */
 	private void close()
 	{
 		this.dbHelper.close();
 		this.read = false; this.write = false;
 	}
 	
 	
 	/************
 	 * 
 	 * Restaurant
 	 * 
 	 ************/
 	
 	/**
 	 * Returns an ArrayList of the distinct towns known by the DB.
 	 * @return a String ArrayList containing the names of all the known towns
 	 * in alphabetical order.
 	 * @throws SQLiteException if the DB cannot be accessed for reading
 	 */
 	public String[] getTowns() throws SQLiteException
 	{
 		this.openRead();
 		Cursor c;
 		
 		c = this.db.query(true, TABLE_RESTAURANT, new String[] {TOWN}, null, null, null, null, TOWN, null);
 		int count = c.getCount();
 		String[] towns = new String[count];
 		int i = 0;
 		while (c.moveToNext())
 		{
 			towns[i] = c.getString(0);
 			i++;
 		}
 		
 		this.close();
 		return towns;
 	}
 	
 	/**
 	 * Returns an ArrayList containing all the distinct town names appearing in the DB table restaurant,
 	 * sorted in alphabetically.
 	 * @param town a town in which to search restaurants
 	 * @return a String ArrayList containing all the town names; if town is null, returns all the towns.
 	 * @throws SQLiteException if the DB cannot be accessed for reading
 	 */
 	public ArrayList<String> getAllResNames(String town) throws SQLiteException
 	{
 		this.openRead();
 		Cursor c;
 		
 		if (town != null)
 		{
 			c = this.db.query(TABLE_RESTAURANT, new String[] {RES,CHAIN}, TOWN+"='"+town+"'", null, null, null, RES);
 		}
 		else
 		{
 			c = this.db.query(TABLE_RESTAURANT, new String[] {RES}, null, null, null, null, RES);
 		}
 		int length = c.getCount();
 		ArrayList<String> restaurants = new ArrayList<String>(length);
 		while (c.moveToNext())
 		{
 			if (c.getString(1) != null)
 				restaurants.add(c.getString(1));
 			else
 				restaurants.add(c.getString(0));
 		}
 		
 		this.close();
 		return restaurants;
 	}
 	
 	/**
 	 * Returns a restaurant object based on his name in the DB.
 	 * @param name the name of the restaurant
 	 * @return the Restaurant corresponding to name, without his dishes
 	 * @throws SQLiteException if the DB cannot be accessed for reading
 	 */
 	public Restaurant getRestaurant(String name) throws SQLiteException
 	{
 		this.openRead();
 		Cursor c;
 		
 		// information held by the restaurant table
 		c = this.db.query(TABLE_RESTAURANT, new String[]{CHAIN,DESCRIPTION,LAT,LONG,STREET,ZIP,
 				TOWN,TEL,RATING,VOTES,SEATS,AVAIL}, RES+"='"+name+"'", null, null, null, null);
 		if (c.getCount() > 1)
 		{
 			System.err.println("Error : two or more retaurants seem to have the same name.");
 		}
 		c.moveToFirst();
 		String chain = c.getString(c.getColumnIndex(CHAIN));
 		String description = c.getString(c.getColumnIndex(DESCRIPTION));
 		float latitude = c.getFloat(c.getColumnIndex(LAT));
 		float longitude = c.getFloat(c.getColumnIndex(LONG));
 		String address = c.getString(c.getColumnIndex(STREET));
 		short zip = c.getShort(c.getColumnIndex(ZIP));
 		String town = c.getString(c.getColumnIndex(TOWN));
 		String tel = c.getString(c.getColumnIndex(TEL));
 		float rating = c.getFloat(c.getColumnIndex(RATING));
 		int votes = c.getInt(c.getColumnIndex(VOTES));
 		short seats = c.getShort(c.getColumnIndex(SEATS));
 		short availableSeats = c.getShort(c.getColumnIndex(AVAIL));
 		
 		// information on the price category
 		float priceCat = getResPriceCat(name);
 		
 		// creates the Restaurant object to be returned
 		Restaurant retour = new Restaurant(name, chain, address, town, tel, description, rating, votes,
 				zip, seats, availableSeats, latitude, longitude, priceCat);
 		
 		// cuisine
 		c = this.db.query(TABLE_CUISINE, new String [] {TYPE}, RES+"='"+name+"'", null, null, null, TYPE);
 		ArrayList<String> cuisine = new ArrayList<String>(c.getCount());
 		while(c.moveToNext())
 		{
 			cuisine.add(c.getString(c.getColumnIndex(TYPE)));
 		}
 		retour.setCuisines(cuisine);
 		
 		// timetable
 		@SuppressWarnings("unchecked")
 		ArrayList<TimeTable> semaine[] = new ArrayList[7];
 		for (int i=0; i<7; i++)
 		{
 			semaine[i] = new ArrayList<TimeTable>(2);
 		}
 		 
 		/*
 		 * fonctionne aussi normalement
 		 * c = db.rawQuery("SELECT day,timeOpen,timeClose FROM timetable WHERE resName='Cr�perie Bretonne' ORDER BY CASE day"+
 		 *		" WHEN 'lundi' THEN 0 WHEN 'mardi' THEN 1 WHEN 'mercredi' THEN 2 WHEN 'jeudi' THEN 3 WHEN 'vendredi' THEN 4 WHEN"+
 		 *		" 'samedi' THEN 5 WHEN 'dimanche' THEN 6 END, timeOpen DESC", null);
 		 */
 		c = db.query(TABLE_TIMETABLE, new String[] {DAY,"strftime('%H:%M', timeOpen)","strftime('%H:%M', timeClose)"},
 				RES+"='"+name+"'", null, null, null, "CASE "+DAY+" WHEN 'lundi' THEN 0 WHEN 'mardi' THEN 1 WHEN 'mercredi'"+
 				" THEN 2 WHEN 'jeudi' THEN 3 WHEN 'vendredi' THEN 4 WHEN 'samedi' THEN 5 WHEN 'dimanche' THEN 6 END, timeOpen");
 		int position;
 		while(c.moveToNext())
 		{
 			position = (int) weekMap.get(c.getString(c.getColumnIndex(DAY)));
 			TimeTable temp = new TimeTable(c.getString(c.getColumnIndex(TIME_OPEN)),c.getString(c.getColumnIndex(TIME_CLOSE)));
 			semaine[position].add(temp);
 		}
 		retour.setSemaine(semaine);
 
 		this.close();
 		return retour;
 	}
 	
 	/**
 	 * Updates the DB after a new rating.
 	 * @param resName the name of the restaurant, must not be null
 	 * @param rating the new rating, must be between 0 and 5
 	 * @param votes the new number of votes, must be > 0
 	 * @return 1 if update was performed on 1 row as expected, any other value means an error has occurred
 	 * @throws SQLiteException if the DB cannot be accessed for writing
 	 * @throws SQLException if one of the arguments is illegal
 	 */
 	public long rateRestaurant (String resName, float rating, int votes) throws SQLiteException, SQLException
 	{
 		// throws an exception if rating is not valid or if resName is null
 		if ((rating > 5) || (rating < 0))
 		{
 			throw new SQLException("Error : wrong rating : "+rating);
 		} 
 		else if (votes<=0)
 		{
 			throw new SQLException("Error : wrong number of votes : "+votes);
 		}
 		else if (resName == null)
 		{
 			throw new SQLException("Error : no restaurant name given for request");
 		}
 		
 		long nrRows = -1;
 		this.openWrite();
 		
 		ContentValues insertValues = new ContentValues(2);
 		insertValues.put(RATING, rating);
 		insertValues.put(VOTES, votes+1);
 		this.db.beginTransaction();
 		nrRows = this.db.update(TABLE_RESTAURANT, insertValues, RES+"='"+resName+"'", null);
 		if (nrRows > 0)
 		{
 			this.db.setTransactionSuccessful();
 		}
 		this.db.endTransaction();
 		
 		this.close();
 		return nrRows;
 	}
 	
 	/**
 	 * Updates the number of available seats for a restaurant in the DB.
 	 * @param resName the name of the restaurant, must not be null
 	 * @param newAvail the new number of available seats, must be >= 0
 	 * @return 1 if update was performed on 1 row as expected, any other value means an error has occurred
 	 * @throws SQLiteException if the DB cannot be accessed for writing
 	 * @throws SQLException if one of the arguments is illegal
 	 */
 	public long updateAvail (String resName, short newAvail) throws SQLiteException, SQLException
 	{
 		// throws an exception if newAvail is not valid or if resName is null
 		if (newAvail < 0)
 		{
 			throw new SQLException("Error : wrong rating : "+newAvail);
 		} 
 		else if (resName == null)
 		{
 			throw new SQLException("Error : no restaurant name given for request");
 		}
 		
 		long nrRows = -1;
 		this.openWrite();
 
 		ContentValues insertValues = new ContentValues(1);
 		insertValues.put(AVAIL, newAvail);
 		this.db.beginTransaction();
 		nrRows = this.db.update(TABLE_RESTAURANT, insertValues, RES+"='"+resName+"'", null);
 		if (nrRows > 0)
 		{
 			this.db.setTransactionSuccessful();
 		}
 		this.db.endTransaction();
 		
 		this.close();
 		return nrRows;
 	}
 
 	
 	/*******
 	 * 
 	 * Dish
 	 * 
 	 *******/
 	
 	/**
 	 * Returns an ArrayList of the dishes served in a restaurant, sorted by type (starter, main course, dessert, drinks)
 	 * and then alphabetically.
 	 * @param resName the name of the restaurant
 	 * @return the ArrayList of all the dishes served in the restaurant resName
 	 * @throws SQLiteException if the DB cannot be accessed for reading
 	 */
 	public ArrayList<Dish> getDishes(String resName) throws SQLiteException
 	{
 		this.openRead();
 		Cursor c;
 		
 		c = db.query(TABLE_DISH, new String[] {DISH,RES,TYPE,SUBTYPE,DESCRIPTION,INVENTORY,PRICE},
 				RES+"='"+resName+"'", null, null, null, " CASE "+TYPE+" WHEN 'Entres' THEN 1 WHEN " +
 				"'Plats' THEN 2 WHEN 'Desserts' THEN 3 WHEN 'Boissons' THEN 4 END, "+SUBTYPE);
 		
 		ArrayList<Dish> dishes = new ArrayList<Dish>(c.getCount());
 		while (c.moveToNext())
 		{
 			String dishName = c.getString(c.getColumnIndex(DISH));
 			String type = c.getString(c.getColumnIndex(TYPE));
 			String subtype = c.getString(c.getColumnIndex(SUBTYPE));
 			String description = c.getString(c.getColumnIndex(DESCRIPTION));
 			int inventory = c.getInt(c.getColumnIndex(INVENTORY));
 			float price = c.getFloat(c.getColumnIndex(PRICE));
 			
 			// the dish's allergens
 			ArrayList<String> allergens = this.searchForAllergens(resName, dishName);
 			
 			// new dish object
 			Dish dish = new Dish(dishName, type, subtype, price, inventory, description, allergens);
 			dishes.add(dish);
 		}
 		
 		this.close();
 		return dishes;
 	}
 	
 	
 	/*********
 	 * 
 	 * Client
 	 * 
 	 *********/
 
 	/**
 	 * Adds a client to the DB.
 	 * @param mail, must not be null
 	 * @param name, must not be null
 	 * @param password, must not be null
 	 * @param tel the client's phone, optional
 	 * @return  if -1, then error; otherwise the rowId of the newly inserted data
 	 * @throws SQLiteException if the DB cannot be accessed for writing
 	 * @throws SQLException if one of the arguments id invalid or if the insert fails
 	 */
 	public long addClient (String mail, String name, String password, String tel) throws SQLiteException, SQLException
 	{
 		// throws an exception if the mandatory information is not given
 		if (mail == null)
 		{
 			throw new SQLException("Error : no client mail given.");
 		}
 		else if (name == null)
 		{
 			throw new SQLException("Error : no client name given.");
 		}
 		else if (password == null)
 		{
 			throw new SQLException("Error : no client password given.");
 		}
 		long rowId = -1;
 		this.openWrite();
 		
 		ContentValues insertValues = new ContentValues(4);
 		insertValues.put(MAIL, mail);
 		insertValues.put(CLIENT, name);
 		insertValues.put(PASSWORD, password);
 		insertValues.put(TEL, tel);
 		this.db.beginTransaction();
 		try
 		{
 			rowId = this.db.insertOrThrow(TABLE_CLIENT, null, insertValues);
 			this.db.setTransactionSuccessful();
 		}
 		finally
 		{
 			this.db.endTransaction();
 			this.close();
 		}
 
 		return rowId;
 	}
 	
 	/**
 	 * Checks whether the client has entered the right password.
 	 * @param clientMail the client's identifier (mail address)
 	 * @param password the password the client has entered
 	 * @return true or false, depending on whether the password is the same as the one found in the DB
 	 * @throws SQLiteException if the DB cannot be accessed for reading
 	 */
 	public boolean checkPassword (String clientMail, String password) throws SQLiteException
 	{
 		this.openRead();
 		Cursor c;
 		
 		c = this.db.query(TABLE_CLIENT, new String[] {PASSWORD}, MAIL+"='"+clientMail+"'", null, null, null, null);
 		int count = c.getCount();
 		if (count == 1)
 		{
 			this.close();
 			return (password.equals(c.getString(1)));
 			
 		}
 		else if (count > 1)
 		{
 			System.err.println("Error : two or more clients seem to have the same mail.");
 		} 
 		else if (count == 0)
 		{
 			// no such client in the DB
 		} 
 		else 
 		{
 			System.err.println("Error : the access to the DB must have failed.");
 		}
 		
 		this.close();
 		return false;
 	}
 	
 	/**
 	 * Changes the mail of a client in the DB.
 	 * @param oldMail the client's old mail (used to identify him), must not be null
 	 * @param newMail the client's new mail, must not be null
 	 * @return 1 if update was performed on 1 row as expected, any other value means an error has occurred
 	 * @throws SQLiteException if the DB cannot be accessed for writing
 	 * @throws SQLException if one of the arguments is invalid
 	 */
 	public long changeMail (String oldMail, String newMail) throws SQLiteException, SQLException
 	{
 		// throws an exception if the mandatory information is not given
 		if (oldMail == null)
 		{
 			throw new SQLException("No old mail given.");
 		}
 		else if (newMail == null)
 		{
 			throw new SQLException("No new mail given.");
 		}
 		
 		long nrRows = -1;
 		this.openWrite();
 		
 		ContentValues insertValues = new ContentValues(1);
 		insertValues.put(MAIL, newMail);
 		this.db.beginTransaction();
 		nrRows = this.db.update(TABLE_CLIENT, insertValues, MAIL+"='"+oldMail+"'", null);
 		if (nrRows > 0)
 		{
 			this.db.setTransactionSuccessful();
 		}
 		
 		this.close();
 		return nrRows;
 	}
 	
 	/**
 	 * Changes the name of a client in the DB.
 	 * @param mail the mail which identifies the client, must not be null
 	 * @param newName the new name the client wants to have in the DB, must not be null
 	 * @return 1 if update was performed on 1 row as expected, any other value means an error has occurred
 	 * @throws SQLiteException if the DB cannot be accessed for writing
 	 * @throws SQLException if one of the arguments is invalid
 	 */
 	public long changeName (String mail, String newName) throws SQLiteException, SQLException
 	{
 		// throws an exception if the mandatory information is not given
 		if (mail == null)
 		{
 			throw new SQLException("No mail given");
 		}
 		else if (newName == null)
 		{
 			throw new SQLException("No new name given.");
 		}
 		
 		long nrRows = -1;
 		this.openWrite();
 
 		ContentValues insertValues = new ContentValues(1);
 		insertValues.put(CLIENT, newName);
 		this.db.beginTransaction();
 		nrRows = this.db.update(TABLE_CLIENT, insertValues, MAIL+"='"+mail+"'", null);
 		if (nrRows > 0)
 		{
 			this.db.setTransactionSuccessful();
 		}
 
 		this.close();
 		return nrRows;
 	}
 	
 	/**
 	 * Changes a client's password in the DB
 	 * @param mail the client's mail
 	 * @param newPassword the client's new password
 	 * @return 1 if update was performed on 1 row as expected, any other value means an error has occurred
 	 * @throws SQLiteException if the DB cannot be accessed for writing
 	 * @throws SQLException if one of the arguments is invalid
 	 */
 	public long changePassword (String mail, String newPassword) throws SQLiteException, SQLException
 	{
 		// throws an exception if the mandatory information is not given
 		if (mail == null)
 		{
 			throw new SQLiteException("No mail given.");
 		}
 		else if (newPassword == null)
 		{
 			throw new SQLException("No password given.");
 		}
 		
 		long nrRows = -1;
 		this.openWrite();
 
 		ContentValues insertValues = new ContentValues(1);
 		insertValues.put(PASSWORD, newPassword);
 		this.db.beginTransaction();
 		nrRows = this.db.update(TABLE_CLIENT, insertValues, MAIL+"='"+mail+"'", null);
 		if (nrRows > 0)
 		{
 			this.db.setTransactionSuccessful();
 		}
 		this.db.endTransaction();
 
 		this.close();
 		return nrRows;
 	}
 
 	/**
 	 * Changes the mail of a client in the DB.
 	 * @param mail the client's mail which identifies him, must not be null
 	 * @param newTel the phone number the client wants to have in the DB, may be null
 	 * @return 1 if update was performed on 1 row as expected, any other value means an error has occurred
 	 * @throws SQLiteException if the DB cannot be accessed for writing
 	 * @throws SQLException if the first argument is illegal
 	 */
 	public long changeTel (String mail, String newTel) throws SQLiteException, SQLException
 	{
 		// throws an exception if the mandatory information is not given
 		if (mail == null)
 		{
 			throw new SQLException("No mail given.");
 		}
 		
 		long nrRows = -1;
 		this.openWrite();
 
 		ContentValues insertValues = new ContentValues(1);
 		insertValues.put(TEL, newTel);
 		this.db.beginTransaction();
 		nrRows = db.update(TABLE_CLIENT, insertValues, MAIL+"='"+mail+"'", null);
 		if (nrRows > 0)
 		{
 			this.db.setTransactionSuccessful();
 		}
 		this.db.endTransaction();
 		
 		this.close();
 		return nrRows;
 	}
 	
 	
 	/*********
 	 * 
 	 * Orders
 	 *
 	 *********/
 	
 	/**
 	 * Gets all the orders a client has ever made that are known to the DB.
 	 * @param mail the client's mail
 	 * @return  an ArrayList containing all orders relative to a client.
 	 * All dishes are included (excepted their description and inventory).
 	 * @throws SQLiteException if the DB cannot be accessed for reading
 	 */
 	public ArrayList<Order> getClientOrders(String mail) throws SQLiteException
 	{
 		this.openRead();
 		Cursor c;
 		
 		// information held by the table order_overview
 		c = this.db.query(TABLE_ORDER_OVERVIEW, new String[] {"_id"}, MAIL+"='"+mail+"'", null, null, null, null);
 		int count = c.getCount();
 		if (count == 0) {
 			this.close();
 			return null;
 		}
 
 		ArrayList<Order> orders = new ArrayList<Order>(count);
 		while (c.moveToNext())
 		{
 			int orderNr = c.getInt(c.getColumnIndex("_id"));
 			Order order = getOrder(orderNr);
 			orders.add(order);
 		}
 
 		this.close();
 		return orders;
 	}
 	
 	/**
 	 * Inserts an order into the DB.
 	 * @param order the order to insert into the DB, must not be null
 	 * @return  if -1, then error; otherwise the rowId of the newly inserted data
 	 * @throws SQLiteException if the DB cannot be accessed for reading
 	 * @throws SQLException if order is null or if insert fails
 	 */
 	public long addOrder(Order order) throws SQLiteException, SQLException
 	{
 		// throws an exception if the mandatory information is not given
 		if (order == null)
 		{
 			throw new SQLException("No order provided.");
 		}
 		
 		this.openWrite();
 		
 		// table order_overview
 		long rowIdOverview = -1;
 		ContentValues insertOverview = new ContentValues(2);
 		insertOverview.put(RES, order.getOrderRestaurant());
 		insertOverview.put(MAIL, order.getOrderEmail());
 		
 		// starts a new transaction
 		this.db.beginTransaction();
 		try
 		{
 			rowIdOverview = this.db.insertOrThrow(TABLE_ORDER_OVERVIEW, null, insertOverview);
 			
 			// table order_detail
 			ContentValues insertDetail = new ContentValues();
 			for (Dish d : order.getOrderDishes())
 			{
 				insertDetail.put(DISH, d.getName());
 				insertDetail.put(QUANTITY, d.getQuantity());
 				insertDetail.put(RES, order.getOrderRestaurant());
 				insertDetail.put(ORDER_NR, rowIdOverview);
 				this.db.insertOrThrow(TABLE_ORDER_DETAIL, null, insertDetail);
 			}
 			this.db.setTransactionSuccessful();
 		}
 		finally
 		{
 			this.db.endTransaction();
 			this.close();
 		}
 		
 		return rowIdOverview;
 	}
 	
 	
 	/**************
 	 * 
 	 * Reservations
 	 *
 	 **************/
 	
 	/**
 	 * Gets all the reservations a client has ever made that are known to the DB.
 	 * @param mail the client's mail
 	 * @return an ArrayList containing all the reservations relative to a client.
 	 * If a reservation holds an order, it is contained.
 	 * @throws SQLiteException if the DB cannot be accessed for reading
 	 */
 	public ArrayList<Reservation> getClientReservations(String mail) throws SQLiteException
 	{
 		this.openRead();
 		Cursor c;
 		
 		// information held by the client table
 		String client = this.getClientName(mail);
 		
 		// information held by the reservation table
 		c = this.db.query(TABLE_RESERVATION, new String[] {RES,ORDER_NR,DATETIME,SEATS}, MAIL+"='"+mail+"'", null, null, null, DATETIME);
 		int count = c.getCount();
 		if (count == 0)
 		{
 			this.close();
 			return null;
 		}
 		
 		ArrayList<Reservation> reservations = new ArrayList<Reservation>(count);
 		while (c.moveToNext())
 		{
 			String resName = c.getString(c.getColumnIndex(RES));
 			String datetime = c.getString(c.getColumnIndex(DATETIME));
 			int seats = c.getInt(c.getColumnIndex(SEATS));
 			
 			// new Reservation
 			Reservation reserv = new Reservation(resName, datetime, seats, client, mail);
 			
 			// adds the corresponding order if there is one
 			if (!c.isNull(c.getColumnIndex(ORDER_NR)))
 			{
 				int orderNr = c.getInt(c.getColumnIndex(ORDER_NR));
 				Order order = getOrder(orderNr);
 				reserv.setReservationOrder(order);
 			}
 		}
 		
 		this.close();
 		return reservations;
 	}
 	
 	/**
 	 * Inserts a reservation into the DB.
 	 * @param reservation the reservation to insert, must not be null
 	 * @param orderNr if > 0, the identifier of the order bound to the reservation, otherwise
 	 * indicates that no order has to be bound.
 	 * @return if -1, then error; otherwise the rowId of the newly inserted data
 	 * @throws SQLiteException if DB cannot be accessed
 	 * @throws SQLException if reservation is null or if insert fails
 	 */
 	public long addReservation(Reservation reservation, int orderNr) throws SQLiteException, SQLException
 	{
 		// throws an exception if the mandatory information is not given
 		if (reservation == null)
 		{
 			throw new SQLException("Arguments missing!");
 		}
 
 		this.openWrite();
 
 		// table reservation
 		long rowId = -1;
 		ContentValues insertValues = new ContentValues(5);
 		insertValues.put(RES, reservation.getReservationResName());
 		insertValues.put(DATETIME, TimeTable.parseDateInString(reservation.getReservationTime()));
 		insertValues.put(SEATS, reservation.getReservationPeople());
 		insertValues.put(MAIL, reservation.getReservationEmail());
 		if (orderNr > 0) {
 			insertValues.put(ORDER_NR, orderNr);
 		}
 		
 		// starts a new transaction
 		this.db.beginTransaction();
 		try
 		{
 			rowId = this.db.insertOrThrow(TABLE_RESERVATION, null, insertValues);
 			this.db.setTransactionSuccessful();
 		}
 		finally
 		{
 			this.db.endTransaction();
 			this.close();
 		}
 
 		return rowId;
 	}
 	
 	public int getAvailOnDateTime(String datetime)
 	{
 		this.openRead();
 		Cursor c;
 		
 		c = this.db.rawQuery("", new String []{""});
 		
 		
 		//TODO
 		return 0;
 	}
 	
 	
 	/*******************
 	 * Internal methods
 	 *******************/
 	
 	/**
 	 * Gets a restaurant's price category.
 	 * @param resName the restaurant's name
 	 * @return the price category
 	 */
 	private float getResPriceCat(String resName)
 	{
 		Cursor c = this.db.rawQuery("SELECT avg(price) as ? FROM ? d, ? r WHERE d.?=r.? AND r.?='?'",
 				new String[]{PRICE_CAT,TABLE_DISH,TABLE_RESTAURANT,RES,RES,RES,resName});
 		if (c.getCount() > 1)
 		{
 			System.err.println("Error : two or more restaurants seem to have the same name.");
 		}
 		c.moveToFirst();
 		float priceCat = c.getFloat(c.getColumnIndex(PRICE_CAT));
 		
 		return priceCat;
 	}
 	
 	/**
 	 * @param resName the restaurant's name
 	 * @param dishName the dish's name
 	 * @return an ArrayList of the allergens contained in the dish
 	 */
 	private ArrayList<String> searchForAllergens(String resName, String dishName)
 	{
 		// information held by the allergen table
 		Cursor c = this.db.query(TABLE_ALLERGEN, new String[]{ALLERGEN},
 				RES+"='"+resName+"' AND "+DISH+"='"+dishName+"'", null, null, null, ALLERGEN);
 		int count = c.getCount();
 		if (count == 0)
 		{
 			return null;
 		}
 		
 		ArrayList<String> allergens = new ArrayList<String>(count);
		while (!c.moveToNext())
 		{
 			allergens.add(c.getString(c.getColumnIndex(ALLERGEN)));
 		}
 
 		return allergens;
 	}
 	
 	/**
 	 * @param mail the client's mail
 	 * @return the client's name
 	 */
 	private String getClientName(String mail)
 	{
 		// information held by the client table
 		Cursor c = this.db.query(TABLE_CLIENT, new String[] {CLIENT}, MAIL+"='"+mail+"'", null, null, null, null);
 		if (c.getColumnCount() > 1)
 		{
 			System.err.println("Error : two or more client seem to have the same mail.");
 		}
 		c.moveToFirst();
 		String name = c.getString(c.getColumnIndex(CLIENT));
 		
 		return name;
 	}
 	
 	/**
 	 * @param orderNr the identifier of the order in the DB
 	 * @return an object order with all the information from the DB
 	 */
 	private Order getOrder(int orderNr)
 	{		
 		Cursor c;
 		Cursor d;
 		
 		// information held by the order_overview table
 		c = this.db.query(TABLE_ORDER_OVERVIEW, new String[] {RES, MAIL}, "_id="+orderNr, null, null, null, null);
 		if (c.getColumnCount() > 1)
 		{
 			System.err.println("Error : two or more orders seem to have the same number.");
 		}
 		c.moveToFirst();
 		String resName = c.getString(c.getColumnIndex(RES));
 		String mail = c.getString(c.getColumnIndex(MAIL));
 		
 		// information held by the client table
 		String client = this.getClientName(mail);
 		
 		// new Order, will be returned
 		Order retour = new Order(resName, client, mail);
 		
 		// information held by the order_detail table
 		c = this.db.query(TABLE_ORDER_DETAIL, new String[] {DISH, QUANTITY}, ORDER_NR+"="+orderNr, null, null, null, null);
 		ArrayList<Dish> dishes = new ArrayList<Dish>(c.getCount());
 		while (c.moveToNext())
 		{
 			String dishName = c.getString(c.getColumnIndex(DISH));
 			int quantity = c.getInt(c.getColumnIndex(QUANTITY));
 			
 			// information held by the dish table
 			d = this.db.query(TABLE_DISH, new String[] {TYPE, SUBTYPE, PRICE}, DISH+"='"+dishName+"' AND "+RES+"='"+resName+"'",
 					null, null, null, null);
 			if (d.getCount() > 1)
 			{
 				System.err.println("Error : two or more dishes seem to have the same name and restaurant.");
 			}
 			String type = c.getString(c.getColumnIndex(TYPE));
 			String subtype = c.getString(c.getColumnIndex(SUBTYPE));
 			float price = c.getFloat(c.getColumnIndex(PRICE));
 			
 			// information held by the allergen table
 			ArrayList<String> allergens = this.searchForAllergens(resName, dishName);
 				
 			Dish dish = new Dish(dishName, type, subtype, price, 0, null, allergens);
 			dish.setQuantity(quantity);
 			dishes.add(dish);
 
 		}
 		retour.setOrderDishes(dishes);
 
 		return retour;
 	}
 }
