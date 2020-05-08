 /**
  *
  */
 package identity.calendar;
 
 import identity.server.UserInfoException;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * @author jaremy
  * Use this class to create a calendar database for both server's and clients. The type
  * should be specified
  */
 public class CalendarDB
 {
 
 	/**
 	 * This contains a generic flat hashmap that can contain either server or client event 
 	 * types. The flat database model allows this class to be used for both client/server
 	 * applications with generality. Overwrite this with correct generics type.
 	 */
 	public ConcurrentHashMap<Integer, CalendarEntry> db;
 
 	public DateFormat datefmt;
 	public UUID useruuid;
 	public String username;
 
 	private String dbFileName;
 	private File dbFile;
 	
 	private boolean isServer = false;
 
 	
 	/**
 	* @param datefmt
 	*/
 	public CalendarDB( )
 	{}
 
 	public CalendarDB( String uname )
 	{
 		db = new ConcurrentHashMap<Integer, CalendarEntry>(50);
 		username = uname;
 		// read in entries
 		String filename = "calendar" + "_"+uname+".cal";
 		
 		parseFile(filename);
 
 		int delay = 7*1000; // delay for 5 sec.
 		int period = 49*1000; // repeatTimer timer = new Timer();
 		
 		Timer timer = new Timer();
 		timer.scheduleAtFixedRate(new TimerTask()
 		{
 			public void run()
 			{
 				//method to write the objects from hashtable to a file
 				writeFile();
 			}
 		}, delay, period);
 
 	}
 
 	
 	public void parseFile(String filename)
 	{
 		dbFileName = filename;
 		String line;
 		String [] parts;
 		
 		BufferedReader dbStreamIn = null;
 		
 		try {
 			// open file
 			dbFile = (new File(dbFileName)).getAbsoluteFile();
 			
 			System.out.println("Calendar file: " + dbFile);
 
 			// read in file and close
 			// Alright Java, is this elegant? Really, is it? Come on C does better than this!
 			dbStreamIn = new BufferedReader(new FileReader(dbFileName));
 
 			System.out.println("reading Server file: "+isServer());
 
 			// add all the values to the HashMap
 			System.out.println("\nRecreating CalendarHash");
 			CalendarEntry entry;
 
 			try {
 				
 				while ( (line = dbStreamIn.readLine()) != null ) {
 					parts = line.split("#");
 					//System.out.print("parts: "+parts.length+" vals:");
 					//for (String a: parts)
 					//	System.out.print(" "+a);
 					//System.out.println("");
 					entry = CalendarEntry.parseStringArray(parts, isServer());
 					System.out.println("debug read: "+entry);
 					addEntry(entry);
 				}
 			}
 			catch (IllegalArgumentException e) {
 				System.out.flush();
 				System.err.println("Invalid key while recreating list");
 				e.printStackTrace();
 				System.err.flush();
 			}
 
 			dbStreamIn.close();
 
 
 			// TODO! use thread to spin off the timing for checkpointing.
 
 		}
 		catch (FileNotFoundException filenotfound) {
 			System.err.println("No File found. Oh Well, hope your users don't mind!");
 		}
 		catch (IOException ioe) {
 			System.err.println("Error in opening the db file: " + ioe);
 			ioe.printStackTrace();
 			System.exit(2);
 		}
 
 	}
 
 	/**
 	 * This method wraps adding objects to the hashmap in a generic fashion. Overwrite this 
 	 * to change hashmap key types.
 	 * @param entry add this entry to the hashmap
 	 * @throws IllegalArgumentException
 	 */
 	public boolean addEntry(CalendarEntry entry)
 	{
 		if (entry == null)
 			return false;
 		Integer key = entry.id;
 		if ( !db.containsKey(key)) {
 			db.put(key, entry);
 			return true;
 		} 
 		else
 			return false;
 	}
 
 	/**
 	 * This method wraps deleting objects from the HashMap in a generic fashion. Overwrite this 
 	 * to change HashMap key types for a server class.
 	 * @param delete this entry from the hashmap
 	 * @throws IllegalArgumentException
 	 */
 	public boolean delEntry(CalendarEntry entry)
 	{
 		if (entry == null)
 			return false;
 		return delEntry(entry.id);
 	}
 
 	public boolean delEntry(Integer id)
 	{
 		if (id == null)
 			return false;
 		else if (db.remove(id) != null)
 			return true;
 		else
 			return false;
 	}
 	
 	public CalendarEntry getEntry(CalendarEntry entry) {
 		return getEntry(entry.id);
 	}
 
 	public CalendarEntry getEntry(Integer key) {
 		return db.get(key);
 	}
 	
 	/**
 	*  Returns a synchronizedList containing the UUID database.
 	* @return List synchronizedList containing the UUID database
 	 */
 	public ConcurrentHashMap<Integer, CalendarEntry> getHashUUID()
 	{
 		return db;
 	}
 
 
 	/**
 	 * Returns an array of UserInfo[] type from ConcurrentHashMap
 	 * @return array holding the contents of the ConcurrentHashMap
 	 */
 	
 	public CalendarEntry[] toArray()
 	{
 		return db.values().toArray(new CalendarEntry[0]);
 	}
 
 
 	/**
 	 * checkpoint This will checkpoint the file. Need to use thread to spin this off. 
 	 * @throws UserInfoException 
 	 */
 	synchronized public void writeFile()
 	{
 		// writeout file
 		BufferedWriter dbStreamOut;
 		// dumpy hashmap into an array then write the array.
 		CalendarEntry[] dumparray = toArray();
 
 		try {
 			dbFile.delete();
 			dbStreamOut = new BufferedWriter( new FileWriter(dbFile, false) );
 			if (!isServer())
 				dbStreamOut.write(""+useruuid+"#"+username+"#\n");
 			
 			for (CalendarEntry entry : dumparray)
 				dbStreamOut.write(entry.toString() + "\n");
 
 			dbStreamOut.close();
 			System.out.println("Checkpointed calendar file: " + dbFile);
 		}
 		catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (IOException e) {
 			// TODO Auto-generated catch block
 			System.err.println("Error in checkpointing. " + e);
 			e.printStackTrace();
 		}
 	}
 
 	public boolean contains(Integer eid)
 	{
 		return db.containsKey(eid);
 	}
 
 
 	public static void main(String[] args) throws ParseException
 	{
 		//classpath /Users/jaremy/proj/ds/jaremy-vamsi/bin/
 		//classpath /Users/jaremy/proj/ds/jaremy-vamsi/bin/
 		String foodate = "4/11/2006 4:55pm";
 
 		Date datetime = CalendarEntry.getDF().parse(foodate);
 		System.out.println("datetime: " + datetime);
 		System.out.println("foo = " + CalendarEntry.getDF().format(datetime) );
 		DateFormat df = CalendarEntry.getDF();
 		// 
 		int i = 0;
 		
 		CalendarEntry bar[] = new CalendarEntry[5]; 
 		
 		for(i=0;i<bar.length; ++i) {
 			bar[i] = new CalendarEntry( 
 					i*100, 
 					new Date(), 
 					(i%2==0) ? "public" : "private", 
 					"some thing", 
 					i*10 );
 			bar[i].privatizeDescr();
 			System.out.println("bar i:"+i+" :"+bar[i]);
 		}
 		
		CalendarDB test = new CalendarDB("duck");
 		System.out.println("created new CalendarDB");
 		for ( CalendarEntry entry : bar ) {
 			test.addEntry(entry);
 		}
 		test.writeFile();
 		
 		for ( CalendarEntry entry : test.db.values() ) {
 			System.out.println("dump: "+entry);
 		}
 		
 		System.out.println("del: "+test.delEntry(100));
 		System.out.println("del: "+test.delEntry(200));
 		System.out.println("del: "+test.delEntry(100));
 
 
 		for ( CalendarEntry entry : test.db.values() ) {
 			System.out.println("dump: "+entry);
 		}
 		
 		for ( CalendarEntry entry: test.toArray()) {
 			System.out.println("toArray: "+entry);
 		}
 //		System.exit(0);
 	}
 
 	public boolean isServer() {
 		return isServer;
 	}
 
 	public void setServer(boolean isServer) {
 		this.isServer = isServer;
 	}
 
 
 
 }
 
 
 
 
