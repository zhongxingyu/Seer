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
 import java.util.EnumMap;
 import java.util.EnumSet;
 import java.util.Properties;
 import java.util.Map.Entry;
 
 import edu.berkeley.gamesman.util.DependencyResolver;
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
 	
 	public Configuration(String path){
 		props = new Properties();
 		addProperties(path);
 		g = Util.typedInstantiateArg("edu.berkeley.gamesman.game."+getProperty("gamesman.game"),this);
 		h = Util.typedInstantiateArg("edu.berkeley.gamesman.hasher."+getProperty("gamesman.hasher"),this);
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
 		g.prepare();
 	}
 	
 	protected Configuration(Properties props2) {
 		props = props2;
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
 	
 	private void checkCompatibility() {
 		if(!DependencyResolver.isHasherAllowed(g.getClass(), h.getClass()))
 			Util.fatalError("Game and hasher are not compatible!");
 	}
 	
 	/**
 	 * Unserialize a configuration from a bytestream
 	 * @param barr Bytes to deserialize
 	 * @return a Configuration
 	 */
 	public static Configuration load(byte[] barr){
 		try{
 		DataInputStream in = new DataInputStream(new ByteArrayInputStream(barr));
 		Properties props = new Properties();
 		
 		byte[] t = new byte[in.readInt()];
 		in.readFully(t);
 		ByteArrayInputStream bin = new ByteArrayInputStream(t);
 		props.load(bin);
 		Configuration conf = new Configuration(props);
 		conf.g = (Game<?>) Util.typedInstantiateArg(in.readUTF(),conf);
 		conf.h = (Hasher<?>) Util.typedInstantiateArg(in.readUTF(),conf);
 		
 		EnumMap<RecordFields, Pair<Integer, Integer>> sf = new EnumMap<RecordFields,Pair<Integer,Integer>>(RecordFields.class);
 		
 		conf.g.prepare();
 		
 		int num = in.readInt();
 		
 		for(int i = 0; i < num; i++){
 			String name = in.readUTF();
 			sf.put(RecordFields.valueOf(name),
 					new Pair<Integer,Integer>(in.readInt(),in.readInt()));
 		}
 		conf.setStoredFields(sf);
 		conf.getGame().prepare();
 		
 		return conf;
 		}catch (IOException e) {
 			Util.fatalError("Could not resuscitate Configuration from bytes :(",e);
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
	 * Parses a property as a boolean.
	 * @param key the name of the configuration property
	 * @param dfl default value
	 * @return false iff s is "false" or s is "0", ignoring case
	 */
	public boolean getBoolean(String key, boolean dfl) {
		String s = props.getProperty(key);
		if(s == null) return dfl;
		return !s.equalsIgnoreCase("false") && !s.equalsIgnoreCase("0");
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
 	public void addProperties(String path){
 		LineNumberReader r = null;
 		try {
 			r = new LineNumberReader(new FileReader(path));
 		} catch (FileNotFoundException e) {
 			Util.fatalError("Could not open property file",e);
 		}
 		String line;
 		try {
 			while((line = r.readLine()) != null){
 				if(line.equals("")) continue;
 				String[] arr = line.split("\\s+=\\s+");
 				Util.assertTrue(arr.length == 2, "Malformed property file at line \""+line+"\"");
 				setProperty(arr[0], arr[1]);
 			}
 		} catch (IOException e) {
 			Util.fatalError("Could not read from property file",e);
 		}
 	}
 	
 	private static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 
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
 	
 	public Database openDatabase() {
 		if(db != null) return db;
 		db = Util.typedInstantiate("edu.berkeley.gamesman.database."+getProperty("gamesman.database"));
 		db.initialize(getProperty("gamesman.db.uri"),this);
 		return db;
 	}
 }
