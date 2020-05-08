 package edu.berkeley.gamesman.core;
 
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.LineNumberReader;
 import java.util.ArrayList;
 import java.util.EnumMap;
 import java.util.EnumSet;
 import java.util.Properties;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import edu.berkeley.gamesman.util.Pair;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * A Configuration object stores information related to a specific configuration of Game, Hasher, and Records.  
  * The information should be specific enough that a database will only match Configuration if the given Game and Hasher 
  * will derive useful information from it.
  * @author Steven Schlansker
  */
 public class Configuration {
 	private Game<?> g;
 	private Hasher<?> h;
 	// storedFields stores a mapping of RecordFields to a pair of integers.
 	// The first integer is the bit offset, the second is the bit length
 	private EnumMap<RecordFields,Pair<Integer,Integer>> storedFields;
 	private Database db;
 	
 	private Properties props;
 	
 	/**
 	 * Reads the key value pairs from the given job file into a Properties object.
 	 * @param path A path to a job file
 	 * @return A Properties object containing all the key-value pairs from the given job file.
 	 */
 	public static Properties readProperties(String path) {
 		Properties props = new Properties();
 		addProperties(props, path);
 		return props;
 	}
 	
 	/**
 	 * Given a Properties, will construct a Configuration
 	 * @param props A Properties object (probably constructed from a job file).
 	 * @param initLater You must call initialize() once you have created the appropriate Game and Hasher objects.
 	 * @throws ClassNotFoundException Could not find game class or hasher class
 	 */
 	public Configuration(Properties props, boolean initLater) throws ClassNotFoundException{
 		this.props = props;
 		initializeStoredFields();
 		if (initLater == false) {
 			String gamename = getProperty("gamesman.game");
 			String hashname = getProperty("gamesman.hasher");
 			initialize(gamename, hashname);
 		}
 	}
 	
 	/**
 	 * Given a Properties, will construct a Configuration
 	 * @param props A Properties object (probably constructed from a job file).
 	 * @throws ClassNotFoundException Could not find game class or hasher class
 	 */
 	public Configuration(Properties props) throws ClassNotFoundException {
 		this(props, false);
 	}
 	/**
 	 * Calls new Configuration(Configuration.readProperties(path))
 	 * @param path The path to the job file to read
 	 * @throws ClassNotFoundException Could not find game class or hasher class
 	 */
 	public Configuration(String path) throws ClassNotFoundException {
 		this(readProperties(path),false);
 	}
 	
 	private void initializeStoredFields() {
 		storedFields = new EnumMap<RecordFields, Pair<Integer,Integer>>(RecordFields.class);
 		String fields = getProperty("record.fields", RecordFields.VALUE.name() + "," + RecordFields.REMOTENESS.name());
 		int i = 0;
 		for(String fld : fields.split(",")){
 			String[] splt = fld.split(":");
 			if(splt.length > 1)
 				storedFields.put(RecordFields.valueOf(splt[0]), new Pair<Integer,Integer>(i++,Integer.parseInt(splt[1])));
 			else
 				storedFields.put(RecordFields.valueOf(splt[0]), new Pair<Integer,Integer>(i++,RecordFields.valueOf(splt[0]).defaultBitSize()));
 		}
 	}
 	
 	/**
 	 * Initialize the Configuration with a game and a hasher object.
 	 * @param newG The Game associated with this configuration.
 	 * @param newH The Hasher associated with this configuration.
 	 */
 	public void initialize(Game<?> newG, Hasher<?> newH) {
 		initialize(g,h,true);
 	}
 
 	/**
 	 * Initialize the Configuration with a game and a hasher object.
 	 * @param newG The Game associated with this configuration.
 	 * @param newH The Hasher associated with this configuration.
 	 */
 	public void initialize(Game<?> newG, Hasher<?> newH, boolean prepare) {
 		g = newG;
 		h = newH;
 		if (prepare) {
 			g.prepare();
 		}
 	}
 
 	/**
 	 * Initialize the Configuration with a game and a hasher object.
 	 * @param in_gamename The Game associated with this configuration.
 	 * @param in_hashname The Hasher associated with this configuration.
 	 * @throws ClassNotFoundException Could not load either the hasher or game class
 	 */
 	public void initialize(final String in_gamename, final String in_hashname, boolean prepare) throws ClassNotFoundException {
 		String gamename = in_gamename, hashname = in_hashname;
 		
 		// Python classes end with ".py"
 		if (gamename.indexOf('.') == -1) {
 			gamename = "edu.berkeley.gamesman.game."+gamename;
 		}
 		setProperty("gamesman.game",gamename);
 		if (hashname.indexOf('.') == -1) {
 			hashname = "edu.berkeley.gamesman.hasher."+hashname;
 		}
 		setProperty("gamesman.hasher",hashname);
		initialize(
			Util.typedInstantiateArg(gamename,Game.class, this),
			Util.typedInstantiateArg(hashname,Hasher.class, this),
			prepare);
 	}
 
 	public void initialize(String gamename, String hashname) throws ClassNotFoundException {
 		initialize(gamename, hashname, true);
 	}
 
 	/**
 	 * @return the Game this configuration plays
 	 */
 	public Game<?> getGame(){
 		return g;
 	}
 	
 	/**
 	 * Specify which fields are to be saved by the database
 	 * Each Field maps to a Pair.  The first element is the integer index it is to be
 	 * stored in the database.
 	 * The second element is the width in bits of that field.
 	 * @param sf EnumMap as described above
 	 */
 	public void setStoredFields(EnumMap<RecordFields,Pair<Integer,Integer>> sf){
 		storedFields = sf;
 	}
 	
 	/**
 	 * Specify which fields are to be saved.  The widths and positions are automatically
 	 * determined and you have no control over them.
 	 * @see #setStoredFields(EnumMap)
 	 * @param set which fields to save
 	 */
 	public void setStoredFields(EnumSet<RecordFields> set){
 		int i = 0;
 		EnumMap<RecordFields,Pair<Integer, Integer>> map = new EnumMap<RecordFields, Pair<Integer,Integer>>(RecordFields.class);
 		for(RecordFields rec : set){
 			map.put(rec, new Pair<Integer, Integer>(i++,rec.defaultBitSize()));
 		}
 		storedFields = map;
 	}
 	
 	/**
 	 * Unserialize a configuration from a bytestream
 	 * @param barr Bytes to deserialize
 	 * @return a Configuration
 	 * @throws ClassNotFoundException Could not find game class or hasher class
 	 */
 	public static Configuration load(byte[] barr) throws ClassNotFoundException {
 		try{
 			DataInputStream instream = new DataInputStream(new ByteArrayInputStream(barr));
 			Properties props = new Properties();
 
 			byte[] t = new byte[instream.readInt()];
 			instream.readFully(t);
 			ByteArrayInputStream bin = new ByteArrayInputStream(t);
 			props.load(bin);
 			Configuration conf = new Configuration(props, true);
 			
 			//assert Util.debug(DebugFacility.CORE, "Deserialized properties:\n"+props);
 
 			EnumMap<RecordFields, Pair<Integer, Integer>> sf = new EnumMap<RecordFields,Pair<Integer,Integer>>(RecordFields.class);
 
 			String gamename = instream.readUTF();
 			String hashername = instream.readUTF();
 			
 			int num = instream.readInt();
 			
 			//assert Util.debug(DebugFacility.CORE, "Expecting "+num+" stored fields");
 
 			for(int i = 0; i < num; i++){
 				String name = instream.readUTF();
 				//assert Util.debug(DebugFacility.CORE," Found field "+name);
 				sf.put(RecordFields.valueOf(name),
 						new Pair<Integer,Integer>(instream.readInt(),instream.readInt()));
 			}
 			conf.setStoredFields(sf);
 			
 
 			try {
 				conf.g = Util.typedInstantiateArg(gamename,Game.class, conf);
 			} catch (ClassNotFoundException e) {
 				conf.g = Util.typedInstantiateArg(conf.getProperty("gamesman.game"),Game.class, conf);
 			}
 			try {
 				conf.h = Util.typedInstantiateArg(hashername,Hasher.class, conf);
 			} catch (ClassNotFoundException e) {
 				conf.h = Util.typedInstantiateArg(conf.getProperty("gamesman.hasher"),Hasher.class, conf);
 			}
 
 			conf.initialize(conf.g,conf.h);
 
 			return conf;
 		}catch (IOException e) {
 			Util.fatalError("Could not resuscitate Configuration from bytes :(\n"+new String(barr),e);
 		}
 		return null;
 	}
 	
 
 	/**
 	 * Return a serialized version of the Configuration suitable for storing persistently
 	 * @return a String with the Configuration information
 	 */
 	public byte[] store(){
 		
 		try {
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(baos);
 		
 		ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
 		
 		props.store(baos2,null);
 		
 		out.writeInt(baos2.size());
 		out.write(baos2.toByteArray());
 		
 		out.writeUTF(g.getClass().getCanonicalName());
 		out.writeUTF(h.getClass().getCanonicalName());
 		
 		out.writeInt(storedFields.size());
 		
 		for(Entry<RecordFields,Pair<Integer, Integer>> e : storedFields.entrySet()){
 			out.writeUTF(e.getKey().name());
 			out.writeInt(e.getValue().car);
 			out.writeInt(e.getValue().cdr);
 		}
 		
 		out.close();
 		
 		return baos.toByteArray();
 		}catch (IOException e) {
 			Util.fatalError("IO Exception shouldn't have happened here",e);
 			return null;
 		}
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if(!(o instanceof Configuration)) return false;
 		Configuration c = (Configuration) o;
 		return c.props.equals(props)
 		&& c.g.getClass().equals(g.getClass()) 
 		&& c.h.getClass().equals(h.getClass());
 	}
 	
 	/**
 	 * @return the records available from a database using this Configuration
 	 */
 	public EnumMap<RecordFields,Pair<Integer,Integer>> getStoredFields(){
 		return storedFields;
 	}
 
 	/**
 	 * @return the Hasher this Configuration is using
 	 */
 	public Hasher<?> getHasher() {
 		return h;
 	}
 	
 	public String toString(){
 		return "Config["+props+","+g+","+h+","+storedFields+"]";
 	}
 	
 	/**
 	 * Get a property by its name
 	 * @param key the name of the configuration property
 	 * @return its value
 	 */
 	public String getProperty(String key){
 		String s = props.getProperty(key);
 		if(s == null)
 			Util.fatalError("Property "+key+" is unset and has no default!");
 		return s;
 	}
 	
 	/**
 	 * For python compatibility.
 	 * @param key the name of the configuration property
 	 * @return its value
 	 * @see #getProperty(String)
 	 */
 	public String __getitem__(String key) {
 		return getProperty(key);
 	}
 	
 	/**
 	 * For python compatibility. Returns false if key does not exist.
 	 * @param key The key to check if it's in the configuration
 	 * @return is the key in the configuration?
 	 * @see #getProperty(String)
 	 */
 	public boolean __contains__(String key) {
 		return props.containsKey(key);
 	}
 	
 	/**
 	 * Parses a property as a boolean.
 	 * @param key the name of the configuration property
 	 * @param dfl default value
 	 * @return false iff s is "false" or s is "0", ignoring case
 	 */
 	public boolean getBoolean(String key, boolean dfl) {
 		String s = props.getProperty(key);
 		if(s != null) {
 			try {
 				return !s.equalsIgnoreCase("false") && !s.equalsIgnoreCase("0");
 			} catch(Exception e) {}
 		}
 		return dfl;
 	}
 	
 	/**
 	 * Parses a property as an Integer.
 	 * @param key the name of the configuration property
 	 * @param dfl default value
 	 * @return The value associated with the key, if defined and an integer. Otherwise, returns dfl.
 	 */
 	public Integer getInteger(String key, Integer dfl) {
 		try {
 			return Integer.parseInt(props.getProperty(key));
 		} catch(Exception e) {}
 		return dfl;
 	}
 	
 	/**
 	 * Parses a property as a Long.
 	 * @param key the name of the configuration property
 	 * @param dfl default value
 	 * @return The value associated with the key, if defined and an long. Otherwise, returns dfl.
 	 */
 	public Long getLong(String key, Long dfl) {
 		try {
 			return Long.parseLong(props.getProperty(key));
 		} catch(Exception e) {}
 		return dfl;
 	}
 	
 	/**
 	 * Parses a property as an array of Integers separated by the regex ", *"
 	 * @param key the name of the configuration property
 	 * @param dfl default value
 	 * @return The value associated with the key, if defined and an integer array. Otherwise, returns dfl.
 	 */
 	public Integer[] getIntegers(String key, Integer[] dfl) {
 		try {
 			return Util.parseIntegers(props.getProperty(key).split(", *"));
 		} catch(Exception e) {}
 		return dfl;
 	}
 	
 	/**
 	 * Get a property by its name.  If the property is not set,
 	 * return dfl
 	 * @param key the name of the configuration property
 	 * @param dfl default value
 	 * @return its value
 	 */
 	public String getProperty(String key,String dfl){
 		return props.getProperty(key,dfl);
 	}
 	
 	/**
 	 * Set a property by its name
 	 * @param key the name of the configuration property to set
 	 * @param value the new value
 	 * @return the old value
 	 */
 	public Object setProperty(String key, String value){
 		return props.setProperty(key, value);
 	}
 	
 	/**
 	 * Read a list of properties from a file
 	 * The properties should be specified as
 	 * key = value
 	 * pairs.  Blank lines are ignored.
 	 * @param path The file path to open
 	 */
 	private static void addProperties(Properties props, String path){
 		LineNumberReader r = null;
 		try {
 			r = new LineNumberReader(new FileReader(path));
 		} catch (FileNotFoundException e) {
 			Util.fatalError("Could not open property file",e);
 		}
 		String line;
 		try {
 			while((line = r.readLine()) != null){
 				line = line.trim();
 				if(line.equals("") || line.charAt(0) == '#') continue;
 				String[] arr = line.split("=",2); // semantics are slightly different from python
 				//The following line can't be here because it causes the Util class to be loaded before
 				//we're ready for it.
 //				Util.assertTrue(arr.length == 2, "Malformed property file at line \""+line+"\"");
 				props.setProperty(arr[0].trim(), arr[1].trim());
 			}
 		} catch (IOException e) {
 			Util.fatalError("Could not read from property file",e);
 		}
 	}
 	
 
 	/**
 	 * Add all the properties into this configuration.  Property strings should be split by = signs.
 	 * @param propStrings the list of properties to set.
 	 */
 	public void addProperties(ArrayList<String> propStrings) {
 		for (String line : propStrings) {
 			if(line.equals("")) continue;
 			String[] arr = line.split("\\s+=\\s+");
 			Util.assertTrue(arr.length == 2, "Malformed property file at line \""+line+"\"");
 			setProperty(arr[0], arr[1]);
 		}
 	}
 	
 	/**
 	 * @return all keys in the configuration
 	 */
 	public Set<Object> getKeys() {
 		return props.keySet();
 	}
 	
 	/**
 	 * Remove a key from the configuration
 	 * @param key the key to remove
 	 */
 	public void deleteProperty(String key) {
 		props.remove(key);
 	}
 
 	public Properties getProperties() {
 		return props;
 	}
 	
 	private static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 
 	/**
 	 * Return a property, prompting the user if it doesn't exist
 	 * @param key the key to get
 	 * @return its value
 	 */
 	public String getPropertyWithPrompt(String key) {
 		String s = props.getProperty(key);
 		if(s == null){
 			System.out.print("Gamesman would like you to specify the value for '"+key+"'\n\t>");
 			System.out.flush();
 			try {
 				return in.readLine();
 			} catch (IOException e) {
 				Util.fatalError("Could not read a line from console",e);
 				return null;
 			}
 		}
 		return s;
 	}
 
 	public void setDatabase(Database db) {
 		this.db = db;
 	}
 
 	public Database getDatabase() {
 		return db;
 	}
 
 	/**
 	 * @return the Database used to store this particular solve
 	 * @throws ClassNotFoundException Could not load the database class
 	 */
 	public Database openDatabase() throws ClassNotFoundException {
 		if(db != null) return db;
 		db = Util.typedInstantiate(
 				"edu.berkeley.gamesman.database."+getProperty("gamesman.database"),
 				Database.class);
 		db.initialize(getProperty("gamesman.db.uri"),this);
 		return db;
 	}
 }
