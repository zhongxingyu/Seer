 import java.io.File;
 import java.util.ArrayList;
 import java.util.Date;
 import com.almworks.sqlite4java.SQLiteConnection;
 import com.almworks.sqlite4java.SQLiteException;
 import com.almworks.sqlite4java.SQLiteStatement;
 import com.almworks.sqlite4java.SQLiteQueue;
 import com.almworks.sqlite4java.SQLiteJob;
 
 /** This class handles all the Databaseconnections
   * the program needs only one Object of this type
   * This is where all our operations on the database happens.
   * If we return something we try to keep it an object
   * 
   * This File has mainly three parts - equivalent to the amount of tables we have
   * 
   * @author stylesuxx
   * @version 0.1
   */
 public class DataBase{
   private File dbFile;
   private SQLiteConnection db;
   private SQLiteQueue queue;
   private boolean log = true;
   private String admin;
 
   /** Constructor with existing database
     * 
     * @param database Location of database to use
     * @param admin User who is to add as admin to the database
     */
   public DataBase( String admin, String database){
     this.admin = admin;
     dbFile = new File( database );
     queue = new SQLiteQueue( dbFile );
     queue.start();
   }
   
   /** Default Constructor
     * <ul>
     * <li>This Constructor (tries to create and) uses the file test.db as Database</li>
     * </ul>
     * 
     * @param admin User who is to add as admin to the database
     */
   public DataBase( String admin ){
     this.admin = admin;
     dbFile = new File( "JJB.db" );
     queue = new SQLiteQueue( dbFile );
     queue.start();
   }
 
   /** Conects to database or tries to create new one if given does not exist
     */
   public void connect(){
     db = new SQLiteConnection( dbFile );
 
     try{
       db.open(true);
     }catch( SQLiteException e ){
       System.out.println( "Error: Could not connect to Database." );
       System.exit(-1);
     }
 
     // Create DB if not already exists
     try{
       SQLiteStatement[] st = {
 	db.prepare( "create table user (Jid TEXT PRIMARY KEY, status TEXT, date TEXT);"),
 	db.prepare( "create table tv (tvKey INTEGER PRIMARY KEY, showname TEXT, airtime TEXT, airday TEXT, timezone TEXT, status TEXT, runtime INTEGER, nextTitle TEXT, nextEpisode INTEGER, nextSeason INTEGER, nextDate TEXT);"),
 	db.prepare( "create table stats (statID INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, value TEXT);" )
       };
       for(int i = 0; i < st.length; i++){
 	try{
 	  st[i].step();
 	} finally{ 
 	  st[i].dispose();
 	}
       }
       System.out.println( "Created new Database: " + dbFile.toString() );
     }catch( Exception e ){ System.out.println("Using existing Database: " + dbFile.toString() ); }
     createAdmin( admin );
   }
 
   /** Disconnect from database
     */
   public void disconnect(){
     db.dispose();
     queue.stop( true );
   }
 
   /** get all Show ID's from database
     * 
     * @return ArrayList<Integer>
     */  
   public ArrayList<Integer> getShowids(){
     return queue.execute( new SQLiteJob<ArrayList<Integer>>(){
       protected ArrayList<Integer> job(SQLiteConnection connection){
 	ArrayList<Integer> toReturn = new ArrayList<Integer>();
 	try{
 	  SQLiteStatement st = connection.prepare( "SELECT tvKey from tv" );
 
 	  try {
 	    while( st.step() ){
 	      toReturn.add( st.columnInt(0) );
 	    }
 	  } finally {
 	    st.dispose();
 	  }
 	}catch( Exception e ){
 	  if( log ) System.out.println("DB Select Error: Could not get ID's" );
 	  e.printStackTrace();
 	}
 	return toReturn;
       }
     }).complete();
   }
 
   /** Get all shows from database with the given status
     * 
     * @param status The status of the shows to be returned
     * 
     * @return Arraylist<TVEntity> 
     */
   public ArrayList<TVEntity> getShows( final String status ){
     return queue.execute( new SQLiteJob<ArrayList<TVEntity>>(){
       protected ArrayList<TVEntity> job(SQLiteConnection connection){
 	ArrayList<TVEntity> toReturn = new ArrayList<TVEntity>();
 	try{
 	  SQLiteStatement st = connection.prepare( "SELECT * from tv WHERE status=?;" );
 	  st.bind( 1, status );
 	  try {
 	    while( st.step() ){
 	      if( !st.columnString(6).equals( "" ) ) toReturn.add( new TVEntity( st.columnInt(0), st.columnString(1), st.columnString(2), st.columnString(3), st.columnString(4), st.columnString(5), st.columnInt(6), st.columnString(7), st.columnInt(8), st.columnInt(9), st.columnString(10) ) );
 	      else toReturn.add( new TVEntity( st.columnInt(0), st.columnString(1), st.columnString(2), st.columnString(3), st.columnString(4), st.columnString(5), st.columnInt(6) ) );
 	    }
 	  } finally {
 	    st.dispose();
 	  }
 	}catch( Exception e ){
 	  if( log ) System.out.println("DB Select Error: Could not get '" + status + "' shows" );
 	  e.printStackTrace();
 	}
 	return toReturn;
       }
     }).complete();
   }
 
   /** Crate an admin user - this is used for the user provided when the bot is started
     * 
     * @param jid Jid of the admin user to create
     * 
     * @return boolean 
     */ 
   public boolean createAdmin( final String jid ){
     return queue.execute( new SQLiteJob<Boolean>(){
       protected Boolean job(SQLiteConnection connection){
 	SQLiteStatement st = null;
 
 	try{
 	  st = connection.prepare("INSERT into user ( Jid, status, date ) VALUES( ?, 'admin', 'date' );");
 	  st.bind(1, jid);
 	  st.step();
 	  st.dispose();
 
 	}catch( SQLiteException e ){
 	  if( log ) System.out.println( "DB Insert Error: Admin user already exists" );
 	  return false;
 	}
 
 	return true;
       }
     }).complete();
   }
 
   // TODO: update for next show
   /** Request a new show to be added to the database
     * <ul>
     * <li>Adds a show and its details to the database if found on tv Rage</li>
     * <li>Returns the name of the show if found on TV Rage</li>
     * </ul>
     * 
     * @param tv TVLookup Object
     * 
     * @return String The shows title
     */
   public String requestShow( final TVRageLookup tv ){
     return queue.execute( new SQLiteJob<String>(){
       protected String job(SQLiteConnection connection){
 	SQLiteStatement st = null;
 
 	try{
 	  st = connection.prepare("INSERT into tv ( tvKey, showname, airtime, airday, timezone, status, runtime ) VALUES( ?, ?, ?, ?, ?, 'requested', ? );");
 	  st.bind(1, tv.getShowid() );
 	  st.bind(2, tv.getShowname() );
 	  st.bind(3, tv.getAirtime() );
 	  st.bind(4, tv.getAirday() );
 	  st.bind(5, tv.getTimezone() );
 	  st.bind(6, tv.getRuntime() );
 
 	  try{
 	    st.step();
 	    st.dispose();
 	  }catch( SQLiteException e ){
 	    if( log ) System.out.println( "DB Insert Error: show already in database" );
 	    st = connection.prepare("SELECT showname FROM tv WHERE tvKey = ? ");
 	    st.bind(1, tv.getShowid() );
 	    st.step();
 	    st.dispose();
 	  }	           	
 
 	}catch( SQLiteException e ){
 	  if( log ) System.out.println( "DB Insert Error: inserting requested show" );
 	  return tv.getShowname();
 	}
 
 	return tv.getShowname();
       }
     }).complete();
   }
 
   /** Request a new show to be added to the database with additional infos
     * <ul>
     * <li>Adds a show and its details to the database if found on tv Rage</li>
     * <li>Returns the name of the show if found on TV Rage</li>
     * </ul>
     * 
     * @param tv TVLookup Object
     * @param nee NextEpisodeEntity Object
     * 
     * @return String The shows title
     */
   public String requestShow( final TVRageLookup tv, final NextEpisodeEntity nee ){
     return queue.execute( new SQLiteJob<String>(){
       protected String job(SQLiteConnection connection){
 	SQLiteStatement st = null;
 
 	try{
 	  st = connection.prepare( "INSERT into tv ( tvKey, showname, airtime, airday, timezone, status, runtime, nextEpisode, nextSeason, nextTitle, nextDate ) VALUES( ?, ?, ?, ?, ?, 'requested', ?, ?, ?, ?, ? );" );
 	  st.bind( 1, tv.getShowid() );
 	  st.bind( 2, tv.getShowname() );
 	  st.bind( 3, tv.getAirtime() );
 	  st.bind( 4, tv.getAirday() );
 	  st.bind( 5, tv.getTimezone() );
 	  st.bind( 6, tv.getRuntime() );
 
 	  st.bind( 7, nee.getEpisode() );
 	  st.bind( 8, nee.getSeason() );
 	  st.bind( 9, nee.getEpisodeTitle() );
 	  st.bind( 10, nee.getStringDate() );
 
 	  try{
 	    st.step();
 	    st.dispose();
 	  }catch( SQLiteException e ){
 	    if( log ) System.out.println( "DB Insert Error: show already in database" );
 	    st = connection.prepare("SELECT showname FROM tv WHERE tvKey = ? ");
 	    st.bind(1, tv.getShowid() );
 	    st.step();
 	    st.dispose();
 	  }	           	
 
 	}catch( SQLiteException e ){
 	  if( log ) System.out.println( "DB Insert Error: inserting requested show with next episode" );
 	  return tv.getShowname();
 	}
 
 	return tv.getShowname();
       }
     }).complete();
   }
 
   public boolean updateShow( final int showID, final NextEpisodeEntity nee ){
     return queue.execute( new SQLiteJob<Boolean>(){
       protected Boolean job(SQLiteConnection connection){
 	SQLiteStatement st = null;
 
 	try{
 	  if( nee != null ){
	    st = connection.prepare( "UPDATE tv set nextEpisode = ?, nextSeason = ?, nextTitle = ?, nextDate = ?" );
 	    st.bind( 1, nee.getEpisode() );
 	    st.bind( 2, nee.getSeason() );
 	    st.bind( 3, nee.getEpisodeTitle() );
 	    st.bind( 4, nee.getStringDate() );
 	  }
 	  else
 	    st = connection.prepare( "UPDATE tv set (nextEpisode, nextSeason, nextTitle, nextDate) VALUES( '', '', '', '' );" );
 
 	  st.step();
 	  st.dispose();
 	}catch( SQLiteException e ){
 	  if( log ) System.out.println( "DB Update Error: updating shows(" + showID + ") next episode" );
 	  e.printStackTrace();
 	  return false;
 	}
 
 	return true;
       }
     }).complete();
 
   }
 
   /** Change status of a show
     * 
     * @param showID ID of the show to approve
     * @param status The status to set the show to
     * 
     * @return boolean True if show was in database
     */
   // TODO: no error if show not in DB
   public boolean setShowStatus( final int showID, final String status){
     return queue.execute( new SQLiteJob<Boolean>(){
       protected Boolean job(SQLiteConnection connection){
 	SQLiteStatement st = null;
 	boolean toReturn = false;
 
 	try{
 	  st = connection.prepare("UPDATE tv SET status=? WHERE tvKey = ? ");
 	  st.bind( 2, showID );
 	  st.bind( 1, status );
 	  st.step();
 	  st.dispose();
 	}catch( SQLiteException e ){
 	  if( log ) System.out.println( "DB Update Error: updating show status" );
 	  return false;
 	}
 	
 	return true;
       }
     }).complete();
   }
 
   /** Deletes show from database
     * 
     * @param showID ID of show to delete
     * 
     * @return boolean 
     */
   public boolean deleteShow( final int showID ){ 
     return queue.execute( new SQLiteJob<Boolean>(){
       protected Boolean job(SQLiteConnection connection){
 	SQLiteStatement st = null;
 	boolean toReturn = false;
 
 	try{
 	  st = connection.prepare("DELETE FROM tv WHERE tvKey = ? ");
 	  st.bind( 1, showID );
 	  st.step();
 	  st.dispose();
 	}catch( SQLiteException e ){
 	  if( log ) System.out.println( "DB Delete Error: No show with ID# " + showID );
 	  return false;
 	}  
 
 	return true;
       }
     }).complete();
   }
 
   // This part is for the Users
 
   /** Register a new User
     * 
     * @param jid Jid to register
     * 
     * @return boolean True if User could be added
     */
   public boolean registerUser( final String jid ){
     return queue.execute( new SQLiteJob<Boolean>(){
       protected Boolean job(SQLiteConnection connection){
 	SQLiteStatement st = null;
 	boolean toReturn = false;
 
 	try{
 	  st = connection.prepare("INSERT into user ( Jid, status, date ) VALUES( ?, ?, ? );");
 	  st.bind(1, jid);
 	  st.bind(2, "registered");
 	  st.bind(3, new Date().toString() );
 	  st.step();
 	  st.dispose();
 	}catch( SQLiteException e ){
 	  if( log ) System.out.println( "DB Insert Error: user: " + jid +" already in database" );
 	  return false;
 	}
 	return true;
       }
     }).complete();
   }
 
   /** Deletes User from database
     * 
     * @param jid JID to remove from database
     * 
     * @return boolean True if usere was removed from database
     * TESTED
     * - no exception if user is not in database
     */
   public boolean unregisterUser( final String jid ){
   return queue.execute( new SQLiteJob<Boolean>(){
       protected Boolean job(SQLiteConnection connection){
 	SQLiteStatement st = null;
 	boolean toReturn = false;
 
 	try{
 	  st = connection.prepare("DELETE FROM user WHERE Jid = ? ");
 	  st.bind(1, jid);
 	  st.step();
 	  st.dispose();
 	}catch( SQLiteException e ){
 	  if( log ) System.out.println( "DB Delete Error: could not delete User" );
 	  return false;
 	}
 
 	return true;
       }
     }).complete();  
   }
 
   /** Set the Status of a user
     *
     * @param jid Jid of users who's status to change
     * @param status Status to change to
     * 
     * @return boolean
     */ 
   public boolean setUserStatus( final String jid, final String status){
     return queue.execute( new SQLiteJob<Boolean>(){
       protected Boolean job(SQLiteConnection connection){
 	SQLiteStatement st = null;
 	boolean toReturn = false;
 
 	try{
 	  st = connection.prepare("UPDATE user SET status = ? WHERE Jid = ? ");
 	  st.bind( 2, jid );
 	  st.bind( 1, status );
 	  st.step();
 	  st.dispose();
 	}catch( SQLiteException e ){
 	  if( log ) System.out.println( "DB Update Error: updating user status" );
 	  return false;
 	}
 	
 	return true;
       }
     }).complete();
   }
   
   /** Returns true if the JID is admin
     * 
     * @param jid Jid to check
     * 
     * @return boolean
     */
   public boolean isAdminUser( final String jid ){ 
     return queue.execute( new SQLiteJob<Boolean>(){
       protected Boolean job(SQLiteConnection connection){
 	SQLiteStatement st = null;
 	boolean toReturn = false;
 
 	try{
 	  st = connection.prepare("select status FROM user WHERE Jid = ? ");
 	  st.bind( 1, jid );
 	  st.step();
 	  if( st.columnString(0).equals( "admin" ) ){
 	    st.dispose();
 	    return true;
 	  }
 	}catch( SQLiteException e ){
 	  if( log ) System.out.println( "DB Select Error: Jid not found: " + jid );
 	  return false;	}
 
 	return false;
       }
     }).complete();
   }
 
   /** Returns true if the JID is approved
     * 
     * @param jid Jid to check
     * 
     * @return boolean
     */
   public boolean isApprovedUser( final String jid ){
     return queue.execute( new SQLiteJob<Boolean>(){
       protected Boolean job(SQLiteConnection connection){
 	SQLiteStatement st = null;
 	boolean toReturn = false;
 
 	try{
 	  st = connection.prepare("select status FROM user WHERE Jid = ? ");
 	  st.bind( 1, jid );
 	  st.step();
 	  if( st.columnString(0).equals( "approved" ) || st.columnString(0).equals( "admin" ) ){
 	    st.dispose();
 	    return true;
 	  }
 	}catch( SQLiteException e ){
 	  if( log ) System.out.println( "DB Select Error: Jid not found: " + jid );
 	  return false;
 	}
 
 	return false;
       }
     }).complete();
   }
 
   /** Returns true if the JID is registered      
     * 
     * @param jid Jid to check
     * 
     * @return boolean
     */
   public boolean isRegisteredUser( final String jid ){
     return queue.execute( new SQLiteJob<Boolean>(){
       protected Boolean job(SQLiteConnection connection){
 	SQLiteStatement st = null;
 	boolean toReturn = false;
 
 	try{
 	  st = connection.prepare("select status FROM user WHERE Jid = ? ");
 	  st.bind( 1, jid );
 	  st.step();
 	  if( st.columnString(0).equals( "registered" ) ){
 	    st.dispose();
 	    return true;
 	  }
 	}catch( SQLiteException e ){
 	  if( log ) System.out.println( "DB Select Error: Jid not found: " + jid );
 	  return false;
 	}
 
 	return false;
       }
     }).complete();
   }
 
   /** Get users with certain status
     *
     * @param status Status of users to lookup
     * 
     * @return ArrayList<String>
     */
   public ArrayList<String> getUsers( final String status ){
     return queue.execute( new SQLiteJob<ArrayList<String>>(){
       protected ArrayList<String> job(SQLiteConnection connection){
 	ArrayList<String> toReturn = new ArrayList<String>();
 
 	try{
 	  SQLiteStatement st = connection.prepare( "SELECT Jid from user WHERE status = ?" );
 	  st.bind( 1, status );
 	  try {
 	    while( st.step() ){
 	      toReturn.add( st.columnString(0) );
 	    }
 	  } finally {
 	    st.dispose();
 	  }
 	}catch( Exception e ){
 	  if( log ) System.out.println( "DB Select Error: Could not get users with status: " + status );
 	}
 
 	return toReturn;
       }
     }).complete();
   }
 
 }
