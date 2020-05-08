 package JNeuralNet;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 class MapSimulation {
 	
 	static final int DEFAULT_XSIZE = 100;
 	static final int DEFAULT_YSIZE = 100;
 	static final int DEFAULT_NUMOFSEEKABLES = 100;
 	
 	//Item[] items;
 	ArrayList<MapItem> mapItems;
 	int xSize;
 	int ySize;
 	
 	MapSimulation(int x, int y, int numOfSeekables, Item seekables) {
 		xSize = x;
 		ySize = y;
 		mapItems = new ArrayList<MapItem>();
 		for(int i=0;i<x*y;i++) {
 			// if at edge of map, wall!
 			if(i%xSize==0 || i/xSize==0 || i%xSize==xSize-1 || i/xSize==ySize-1) {
 				mapItems.add(new MapItem(new int[] { i%xSize, i/xSize }, Item.WALL));
 				//items[i].setFlag(Item.WALL);
 			}
 		}
 		for(int i=0;i<numOfSeekables;i++) {
 			spawnSeekable(seekables);
 		}
 	}
 	
 	MapSimulation(int numOfSeekables, Item seekables) {
 		this(DEFAULT_XSIZE,DEFAULT_YSIZE,numOfSeekables,seekables);
 	}
 	
 	MapSimulation(Item seekables) {
 		this(DEFAULT_NUMOFSEEKABLES,seekables);
 	}
 	
 	void spawnSeekable(Item seekables) {
 		int[] coordinate = getEmptySquare();
 		try {
 		mapItems.add(new MapItem(coordinate, seekables.randItem()));
 		} catch (Exception e) {
 			// since we're pretty damn sure seekables is not empty...
 			System.err.println(e.toString());
 			// then again, maybe it is.  But we don't much care if it is (do we?) uncomment below if we do
 			// System.exit(-1);
 		}
 	}
 	
 	int[] getEmptySquare() {
 		int[] rv = new int[3];
 		Random r = new Random();
 		int i = r.nextInt(mapItems.size());
 		while(mapItems.get(i).isEmpty()) {
 			System.out.println("looping...");
 			// X coordinate is stored in coords[0]
 			rv[0] = i%xSize;
 			// Y coordinate is stored in coords[1]
 			rv[1] = i/xSize; // don't try to be clever and change this to ySize, it will break!
 			rv[2] = i;
 			// try again next time if we don't succeed
			i = r.nextInt(xSize * ySize);
 		}
 		return rv;
 	} // int[] getEmptySquare()
 	
 	boolean isSquareEmpty(int[] coord) throws Exception {
 		for(int i=0;i<mapItems.size();i++)
 			if(mapItems.get(i).getCoords()==coord)
 				return mapItems.get(i).isEmpty();
 		throw new Exception("Array index out of bounds");
 	}
 
 	Item getItemAt(int[] coords) {
 		return mapItems.get((coords[0]*xSize) + coords[1]).getItem();
 	}
 	
 	void setItemAt(int[] coords,Item t) {
 		MapItem to = new MapItem(coords,t);
 		mapItems.set((coords[0]*xSize) + coords[1],to); 
 	}
 
 	short getClosestWall(int[] coords, double heading) {
 		int[] c = coords;
 		while(mapItems.get(c[0]*c[1]).isEmpty()) { // while is empty (remove double negative, woot!)
 			c[0] += (int)Math.floor(Math.cos(heading));
 			c[1] += (int)Math.floor(Math.sin(heading));
 		}
 		// return the integer from the function sqrt(a²+b²) where a and b are the lengths of the vectors between coords and coords+c[0], and
 		// between coords and coords+c[1].  The result is the hypotenuse, according to Pythagorus' theorum.
 		// Since this is a rather long line, I felt it deserved a rather long explanation.
 		return (short)Math.floor(Math.sqrt((c[0]-coords[0])*(c[0]-coords[0]) + (c[1]-coords[1])*(c[1]-coords[1])));
 	}
 
 	short getClosestSeekable(Item seeking, int[] coords, double direction) {
 		int[] c = coords;
 		while(((mapItems.get(c[0]*c[1]).getItem().getFlags()) & seeking.getFlags())==0) {
 			c[0] += (int)Math.floor(Math.cos(direction));
 			c[1] += (int)Math.floor(Math.sin(direction));
 		}
 		// return the integer from the function sqrt(a²+b²) where a and b are the lengths of the vectors between coords and coords+c[0], and
 		// between coords and coords+c[1].  The result is the hypotenuse, according to Pythagorus' theorum.
 		// Since this is a rather long line, I felt it deserved a rather long explanation.
 		return (short)Math.floor(Math.sqrt((c[0]-coords[0])*(c[0]-coords[0]) + (c[1]-coords[1])*(c[1]-coords[1]))); 
 	}
 
 	public void draw() {
 		// TODO: dunno, do something, draw the map I guess.
 		System.out.println("Tick complete (draw() called)");
 	}
 	
 }
