 package dk.itu.mario.level.generator.bio;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.NavigableMap;
 import java.util.NavigableSet;
 import java.util.Random;
 import java.util.TreeMap;
 
 public class ScreenChunkLibrary {
 	
 	private static ScreenChunkLibrary instance = null;
 
 	private HashMap<Integer, ScreenChunk> lib;
 	private transient TreeMap<Float, ScreenChunk> treeLib;
 	
 	private int nextId;
 	
 	private static float halfrange = 0.2f; 
 	private Random random;
 	
 	private ScreenChunkLibrary() {
 		lib = new HashMap<Integer, ScreenChunk>();
 		nextId = 0;
 		random = new Random(); // possibly set seed
 	}
 	
 	public static ScreenChunkLibrary getInstance() {
 		if (instance == null)
 			instance = new ScreenChunkLibrary();
 		
 		return instance;
 	}
 	
 	public int getNextId() {
 		while (lib.containsKey(nextId))
 			++nextId;
 		
 		return nextId;
 	}
 	
 	public boolean addChunk(ScreenChunk chunk) {
 		if (lib.containsKey(chunk.getId()))
 			return false;
 		
 		lib.put(chunk.getId(), chunk);
 		
 		return true;
 	}
 	
 	public void removeChunk(int id) {
 		lib.remove(id);
 	}
 	
 	public void removeChunk(Chunk chunk) {
 		removeChunk(chunk.getId());
 	}
 	
 	public ScreenChunk getChunk(int id) {
 		return lib.get(id);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void readLibFromFile(File file) throws Exception {
 		try {
 			FileInputStream fis = new FileInputStream(file);
 			ObjectInputStream ois = new ObjectInputStream(fis);
 			lib = (HashMap<Integer, ScreenChunk>) ois.readObject();
 			ois.close();
 			fis.close();
 			nextId = 0;
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw e;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw e;
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw e;
 		}
 	}
 	
 	public void writeLibToFile(File file) throws Exception {
 		try {
 			FileOutputStream fos = new FileOutputStream(file);
 			ObjectOutputStream oos = new ObjectOutputStream(fos);
 			oos.writeObject(lib);
 			oos.close();
 			fos.close();
 		} catch (IOException e) {
 			// TODO: handle exception
 			e.printStackTrace();
 			throw e;
 		}
 	}
 	
 	public void calcSearchTree() {
 		treeLib = new TreeMap<Float, ScreenChunk>();
 		
 		for (ScreenChunk sc : lib.values()) {
 			//treeLib.put(sc.getTotalValue(), sc);
 			float key = sc.getValue();
 			while (treeLib.containsKey(key))
 				key += 0.000001f; // a small hack to ensure we don't override previous chunks. This has virtually no effect on the weighting. TODO: Do it properly - use e.g. a list to save multiple elements in
 			treeLib.put(key, sc);
 		}
 	}
 	
 	/**
 	 * Returns a screen chunk that approximately matches the wished arousal level. 
 	 * @param arousal The approximate arousal level the screen chunk is expected to generate.
 	 * @param windowsIn The window area the chunk must overlap - can be null.
 	 * @return
 	 */
 	public ScreenChunk getChunk(float arousal, List<byte[]> windowsIn) {
 		// TODO: Have chunks sorted in a TreeMap. Get a subMap and return a random chunk or something.
 		//System.out.println(arousal);
 		
 		ScreenChunk sc = null;
 		float range = halfrange;
 		while (sc == null) {
 			NavigableMap<Float,ScreenChunk> subMap = treeLib.subMap(arousal-range, true, arousal+range, true);
 			
 			if (subMap.isEmpty()) {
 				range *= 1.25f; //increase search range
 				continue;
 			}
 			
 			//if (subMap.size() > 1)
 			//	System.out.println("hello");
 			
 			// Try and pick a random chunk in submap (5 tries)
 			NavigableSet<Float> navigableKeySet = subMap.navigableKeySet();
 			Float[] keys = new Float[navigableKeySet.size()];
 			keys = navigableKeySet.toArray(keys);
 			for (int i = 0; i < 5; ++i) {
 				Float key = keys[random.nextInt(subMap.size())];
 				sc = subMap.get(key);
 				
 				if (windowsIn == null) {
 					
 					break;
 				} else {
 					if (!windowsOverlap(windowsIn, sc.getInWindows()))
 						sc = null;
 				}
 			}
 			
 			if (sc == null) {
 				// we didn't find a valid random chunk; now we traverse the full submap to find a valid chunk
 				for (Float key : keys) {
 					sc = subMap.get(key);
 					
 					if (windowsOverlap(windowsIn, sc.getInWindows()))
 						break;
 					else
 						sc = null;
 				}	
 			}
 			
 			if (range > 0.55f) {
 				Float k = treeLib.higherKey(arousal);
 				if (k != null)
 					sc = treeLib.get(k); // just pick a chunk when we've searched the whole map a couple of times
 				else sc = treeLib.get(treeLib.lowerKey(arousal+0.00001f));
 			}
 			
 			if (sc == null) {
 				range *= 1.25f; //increase search range
 			}		
 		}
 		
 		return sc;
 	}
 	
 	ArrayList<ScreenChunk> schunks;
 	
 	private static Comparator<ScreenChunk> scComparator = new Comparator<ScreenChunk>() {
 		public int compare(ScreenChunk sc1, ScreenChunk sc2) {
 			if (sc1.getValue() > sc2.getValue())
 				return 1;
 			else if (sc1.getValue() < sc2.getValue())
 				return -1;
 			else
 				return 0;
 		}
 	};
 	
 	public void prepGetChunk2() {
 		schunks = new ArrayList<ScreenChunk>();
 		
 		for (ScreenChunk sc : lib.values()) {
 			schunks.add(sc);
 		}
 		
 		Collections.sort(schunks, scComparator);
 	}
 	
 	public ScreenChunk getChunk2(float arousal, List<byte[]> windowsIn) {
 		
 		int ind = 0;
 		// Find s chunk where value is just greater than arousal
 		for (ScreenChunk sc : schunks) {
 			if (sc.getValue() > arousal)
 				break;
 			++ind;
 		}
 		
 		int lb = ind; // Lower bound
 		for (int i = ind - 1; i >= 0; --i) {
 			ScreenChunk sc = schunks.get(i);
 			if (sc.getValue() < arousal - halfrange)
 				break;
 			lb--;
 		}
 		
 		int hb = ind; // Higher bound
 		for (int i = ind + 1; i < schunks.size(); ++i) {
 			ScreenChunk sc = schunks.get(i);
 			if (sc.getValue() > arousal + halfrange)
 				break;
 			hb++;
 		}
 		
 		if (lb != hb && windowsIn != null) //Christoffer did edit for first chunk in level
 		{
 			List<ScreenChunk> subList = schunks.subList(lb, hb);
 			ScreenChunk sc = subList.get(random.nextInt(subList.size()));
 			while (!windowsOverlap(windowsIn, sc.getInWindows()))
 				sc = subList.get(random.nextInt(subList.size()));
 			return sc;
 		} else if (windowsIn != null){
 			//ScreenChunk sc = schunks.get(ind);
			ScreenChunk sc = schunks.get(ind-1); //Christoffer hack
			while (!windowsOverlap(windowsIn, sc.getInWindows())) {
 				++ind;
 				ind %= schunks.size();
 				sc = schunks.get(ind);
			}
 			return sc;
 		} else //Christoffer edit for first chunk in level 
 		{
 			Random rnd = new Random();
 			ScreenChunk sc = schunks.get(rnd.nextInt(schunks.size()));
 			return sc;
 		}
 	}
 	
 	private boolean windowsOverlap(List<byte[]> windowsIn, List<byte[]> windowsOut) {
 		//if(windowsIn != null && windowsOut != null)
 		//{
 			for (byte[] in : windowsIn) {
 				for (byte[] out : windowsOut ) {
 					if (!(in[0] > out[1] || in[1] < out[0]))
 						return true;
 				}
 			}
 		//}
 		
 		return false;
 	}
 
 	public int getNumOfChunks() {
 		// TODO Auto-generated method stub
 		return lib.size();
 	}
 }
