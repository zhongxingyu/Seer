 package game.utils;
 
 import game.Game;
 import game.Map;
 import game.entity.Entity;
 import game.entity.SerialEntity;
 import game.tile.Tile;
 import game.triggers.SerialTrigger;
 import game.triggers.Trigger;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 
 public class FileSaver {
 
 	
 	
 	
 /*	public static ByteBuffer loadPNGTexture(String path)
 	{
 		try{
 		ByteBuffer buf = null;
 		int tWidth = 0,
 			tHeight = 0;
 		InputStream in = new FileInputStream(FileSaver.getCleanPath()+"//res//tiles.png");
 		PNGDecoder decoder = new PNGDecoder(in);
 		
 		tWidth = decoder.getWidth();
 		tHeight = decoder.getHeight();
 		buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
 		decoder.decode(buf, decoder.getWidth() * 4, Format.RGBA);
 		buf.flip();
 		in.close();
 		return buf;
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}*/
 	
 	public static void saveMapFile(Map map, String path)
 	{
 		PrintWriter out;
 		try {
 			Game.log("Saving map file " + path);
 			out = new PrintWriter(path);
 			//SAVE MISC INFO
 			out.println("NAME "+map.name);
 			out.println("VERSION "+map.version);
 			out.println("DESC "+map.desc);
 			out.println("LOCKED "+map.isLocked);
 			out.println("LEVEL "+map.isLevel);
 			
 			
 			//SAVE TILES
 			out.println("TILES");
 			for(int x = 0; x < map.tiles.length; x++)
 			{
 				for(int y = 0; y < map.tiles[x].length; y++)
 				{
 					out.println(x+" "+y+" "+Tile.getTileID(map.tiles[x][y]));
 				}
 			}
 			out.println("END");
 			out.println("ENTITIES");
 			for(SerialEntity e : map.entities)
 			{
 				out.println(e.x+" "+e.y+" "+e.health+" "+e.relatedEntity);
 			}
 			out.println("END");
 			out.println("TRIGGERS");
 			for(Trigger t : map.triggers)
 			{
 				out.println(t.x+" "+t.y+" "+t.getClass().getCanonicalName()+" "+t.lx+" "+t.ly);
 			}
 			out.println("END");
 			
 			
 			out.checkError();
 			out.close();
 		} catch (Exception e) {
 			Game.log("Map saving did an uh oh at:");
 			e.printStackTrace();
 		}
 
 	}
 	
 	public static Map loadMapFile(String path)
 	{
 		int readMode = 0;
 		Map map = new Map();
 		BufferedReader br;
 		try {
 			br = new BufferedReader(new FileReader(path));
 			String line;
 			while ((line = br.readLine()) != null) {
 				if (!(line.charAt(0) == "#".charAt(0))) {
 					String args[] = line.split(" ");
 					if(readMode == 0)
 					{
 						if(args[0].equals("NAME"))map.name = args[1];
 						if(args[0].equals("VERSION"))map.version = args[1];
 						if(args[0].equals("DESC"))map.desc = args[1];
						if(args[0].equals("LOCKED"))map.isLocked = Boolean.getBoolean(args[1]);
						if(args[0].equals("LEVEL"))map.isLevel = Boolean.getBoolean(args[1]);
 						if(args[0].equals("TILES"))readMode = 1;
 						if(args[0].equals("ENTITIES"))readMode = 2;
 						if(args[0].equals("TRIGGERS"))readMode = 3;
 					}else if(readMode > 0)
 					{
 						if(args[0].equals("END"))readMode = 0;
 						if(readMode == 1)
 						{
 							map.tiles[Integer.parseInt(args[0])][Integer.parseInt(args[1])] = Tile.tiles[Integer.parseInt(args[2])];
 						}
 						if(readMode == 2)
 						{
 							map.entities.add(new SerialEntity(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3]));
 						}
 						if(readMode == 3)
 						{
 							map.triggers.add(new Trigger());
 						}
 					}
 				}
 			}
 			br.close();
 
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		return map;
 	}
 	
 	
 	/**
 	 * Used for map loading to retrieve entity instances from the map file.
 	 * @param entlist The list of serial entities supplied by the map file
 	 * @param game The game instance
 	 * @return A list of entity instances
 	 * @throws ClassNotFoundException If entity list is invalid
 	 * @throws InstantiationException If entity list is invalid
 	 * @throws IllegalAccessException If entity list is invalid
 	 */
 	public static ArrayList<Entity> serialToEntity(
 			ArrayList<SerialEntity> entlist, Game game)
 			throws ClassNotFoundException, InstantiationException,
 			IllegalAccessException {
 		System.out.println("Deserializing entity array...");
 		System.out.println(entlist.size() + " entities to deserialize.");
 		ArrayList<Entity> newlist = new ArrayList<Entity>();
 		for (SerialEntity se : entlist) {
 			System.out.println("Deserialized entity " + se.relatedEntity);
 			Class<?> c = Class.forName(se.relatedEntity);
 			Entity dummyentity = (Entity) c.newInstance();
 			dummyentity.x = se.x;
 			dummyentity.y = se.y;
 			dummyentity.game = game;
 			newlist.add(dummyentity);
 		}
 		return newlist;
 	}
 
 	/**
 	 * Used for map saving to convert entities to saveable versions
 	 * @param entlist A list of entities supplied by the Map instance
 	 * @return A list of saveable entities
 	 */
 	public static ArrayList<SerialEntity> entityToSerial(
 			ArrayList<Entity> entlist) {
 		System.out.println("Serializing entity array...");
 		System.out.println(entlist.size() + " entities to serialize.");
 		ArrayList<SerialEntity> newlist = new ArrayList<SerialEntity>();
 		for (Entity e : entlist) {
 			System.out.println("Serialized entity "
 					+ e.getClass().getCanonicalName());
 			newlist.add(new SerialEntity(e.x, e.y, 0, e.getClass()
 					.getCanonicalName()));
 		}
 		return newlist;
 	}
 	
 	public static ArrayList<SerialTrigger> triggerToSerial(ArrayList<Trigger> triggers)
 	{
 		ArrayList<SerialTrigger> retrn = new ArrayList<SerialTrigger>();
 		
 		
 		for(Trigger t : triggers)
 		{
 			boolean newTrigger = true;
 			//Take the first trigger and see if its been serialized allready
 			for(SerialTrigger st : retrn)
 			{
 				if(st.x == t.x && st.y == t.y && st.relatedTrigger == t.getClass().getCanonicalName())
 				{
 					//Must be the same trigger, break
 					System.out.println("Found already serialized trigger "+t.getClass().getCanonicalName());
 					newTrigger = false;
 					break;
 				}
 			}
 			if(newTrigger)
 			{
 				System.out.println("Trigger is still fresh... finding chain...");
 				//If the trigger is still fresh
 				SerialTrigger newSerial = new SerialTrigger(t.x, t.y,null, t.getClass().getCanonicalName());
 				//Now we need to serialize the related trigger
 				Trigger relatedTrigger = t.linkedTrigger;
 				System.out.println("-Chain start at "+relatedTrigger);
 				while(relatedTrigger != null)
 				{
 					System.out.println("|Trigger Element "+relatedTrigger.linkedTrigger);
 					//While we are still in the chain
 					
 					
 					relatedTrigger = relatedTrigger.linkedTrigger;
 				
 				}
 				System.out.println("-Chain end at "+relatedTrigger);
 			}
 		}
 		
 		return retrn;
 	}
 
 	/**
 	 * Used by the crash screen to the stack trace
 	 * @param aThrowable The throwable error
 	 * @return The stacktrace
 	 */
 	public static String getStackTrace(Throwable aThrowable) {
 		final Writer result = new StringWriter();
 		final PrintWriter printWriter = new PrintWriter(result);
 		aThrowable.printStackTrace(printWriter);
 		return result.toString();
 	}
 
 	/**
 	 *  Saves all the properties in the hashmap props into a file like this:
 	 *  Key: Value
 	 * @param props The hashmap to save to a file.
 	 * @param path The path to save at.
 	 */
 	public static void savePropertiesFile(HashMap<String, String> props,
 			String path) {
 		PrintWriter out;
 		try {
 			Game.log("Saving property file " + path);
 			out = new PrintWriter(path);
 			out.println("#" + Game.TITLE + " Property File. ");
 			for (int i = 0; i < props.keySet().size(); i++) {
 
 				String property = props.keySet().toArray()[i] + "";
 				String value = props.get(property);
 				out.println(property + ": " + value);
 			}
 			out.checkError();
 			out.close();
 		} catch (Exception e) {
 			Game.log("File saving did an uh oh at:");
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Used for reading property files
 	 * @param path Path to read from
 	 * @return Hashmap form of property file
 	 */
 	public static HashMap<String, String> readPropertiesFile(String path) {
 		HashMap<String, String> props = new HashMap<String, String>();
 		BufferedReader br;
 		try {
 			br = new BufferedReader(new FileReader(path));
 			String line;
 			while ((line = br.readLine()) != null) {
 				if (!(line.charAt(0) == "#".charAt(0))) {
 					String[] properties = line.split(": ");
 					props.put(properties[0], properties[1]);
 				}
 			}
 			br.close();
 
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return props;
 	}
 
 	/**
 	 * Basic file saver
 	 * @param file File to save
 	 * @param path Path to save at
 	 */
 	public static void save(Object file, String path) {
 		FileOutputStream fos;
 		ObjectOutputStream oos;
 
 		try {
 			Game.log("Saving file " + path);
 
 			fos = new FileOutputStream(path);
 			oos = new ObjectOutputStream(fos);
 			oos.reset();
 			oos.writeObject(file);
 			oos.close();
 			fos.close();
 		} catch (FileNotFoundException e) {
 			new File(path).mkdir();
 			save(file, path);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		Game.log("Done!");
 
 	}
 
 	/**
 	 * Basic file loader
 	 * @param path Path to load from
 	 * @return Object returned
 	 */
 	public static Object load(String path) {
 		try {
 			Game.log("Loading file " + path);
 			FileInputStream fis = new FileInputStream(path);
 			ObjectInputStream ois = new ObjectInputStream(fis);
 			return ois.readObject();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 
 	}
 
 	/**
 	 * Gets working directory
 	 * @return Working directory
 	 */
 	public static String getCleanPath() { 
 		return new File(FileSaver.class.getProtectionDomain().getCodeSource()
 				.getLocation().getPath()).getAbsolutePath();
 	}
 
 }
