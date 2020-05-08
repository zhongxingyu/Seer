 package edu.berkeley.gamesman.core;
 
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.LineNumberReader;
 import java.io.Serializable;
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
 	private static final long serialVersionUID = -5331459097835638972L;
 	private Game<?> g;
 	private Hasher<?> h;
 	private EnumMap<RecordFields,Pair<Integer,Integer>> storedFields;
 	
 	private Properties props;
 	/**
 	 * Initialize a new Configuration.  Both parameters should be fully initialized.
 	 * @param props The properties used to configure options
 	 * @param g The game used
 	 * @param h The hasher used
 	 * @param storedFields Which fields should be stored in the database
 	 */
 	public Configuration(Properties props,Game<?> g, Hasher<?> h, EnumMap<RecordFields,Pair<Integer,Integer>> storedFields) {
 		this.props = props;
 		this.g = g;
 		this.h = h;
 		this.storedFields = storedFields;
		buildConfig();
 		checkCompatibility();
 	}
 	
 	/**
 	 * @return the Game this configuration plays
 	 */
 	public Game<?> getGame(){
 		return g;
 	}
 	
 	/**
 	 * Specify the Game.
 	 * Must be called before using this Configuration.
 	 * @param g the game to play
 	 */
 	public void setGame(Game<?> g){
 		this.g = g;
 	}
 	
 	/**
 	 * Specify the Hasher.
 	 * Must be called before using this Configuration
 	 * @param h the hasher to use
 	 */
 	public void setHasher(Hasher<?> h){
 		this.h = h;
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
 	 * Create a new Configuration
 	 * @param props The properties used to configure options
 	 * @param g The game we're playing
 	 * @param h The hasher to use
 	 * @param set Which records to save
 	 */
 	public Configuration(Properties props,Game<?> g, Hasher<?> h, EnumSet<RecordFields> set){
 		this.props = props;
 		int i = 0;
 		EnumMap<RecordFields,Pair<Integer, Integer>> map = new EnumMap<RecordFields, Pair<Integer,Integer>>(RecordFields.class);
 		for(RecordFields rec : set){
 			map.put(rec, new Pair<Integer, Integer>(i++,rec.defaultBitSize()));
 		}
 		this.g = g;
 		this.h = h;
 		this.storedFields = map;
		buildConfig();
 		checkCompatibility();
 	}
 	
 	/**
 	 * A Configuration that is specified only by properties.
 	 * You <i>must</i> set the game and hasher before using this Configuration
 	 * @see #setGame(Game)
 	 * @see #setHasher(Hasher)
 	 * @see #setStoredFields
 	 * @param props2 The properties to inherit
 	 */
 	public Configuration(Properties props2) {
 		props = props2;
 	}
 	
 	/**
 	 * Load a saved Configuration from a String.
 	 * @param config The serialized Configuration
 	 * @return the Configuration
 	 */
 	
 	//public static Configuration configurationFromString(String config){
 	//	return load(config.getBytes());
 	//}
 	
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
 		conf.setGame((Game<?>) Util.typedInstantiateArg(in.readUTF(),conf));
 		conf.setHasher((Hasher<?>) Util.typedInstantiateArg(in.readUTF(),conf));
 		
 		EnumMap<RecordFields, Pair<Integer, Integer>> sf = new EnumMap<RecordFields,Pair<Integer,Integer>>(RecordFields.class);
 		
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
 		//return deserialize(barr);
 	}
 	//public Configuration(String config) {
 	//	this.config = config;
 	//	this.g = null;
 	//	this.h = null;
 	//	this.storedFields = null;
 	//}
 	
 
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
 		
 		//return new String(serialize());
 		//return config;
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if(!(o instanceof Configuration)) return false;
 		Configuration c = (Configuration) o;
 		return c.props.equals(props) && c.g.getClass().equals(g.getClass()) && c.h.getClass().equals(h.getClass());
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
 
 	/**
 	 * @param bytes A bytestream
 	 * @return the Configuration represented by that bytestream
 	 */
 	//public static Configuration deserialize(byte[] bytes) {
 	//	return Util.deserialize(bytes);
 	//}
 
 	/**
 	 * @return a bytestream representing this Configuration
 	 */
 	//public byte[] serialize() {
 	//	return Util.serialize(this);
 	//}
 	
 	public String toString(){
 		return "Config["+props+","+g+","+h+","+storedFields+"]";
 		//return new String(serialize());
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
 	
 
 }
