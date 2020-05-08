 package internal.tree;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Properties;
 import java.util.Random;
 
 import internal.parser.containers.Constraint;
 import internal.piece.IPiece;
 import internal.piece.PieceFactory;
 import internal.piece.TileInterfaceType;
 import internal.space.Space;
 import internal.space.Coordinates;
 import internal.tree.IWorldTree.IMap;
 import internal.tree.IWorldTree.IRoom;
 import internal.tree.IWorldTree.IRegion;
 import internal.tree.IWorldTree.ITile;
 import internal.space.Space.Direction;
 
 /**
  * Factory class responsible for generating objects of {@code IWorldTree}
  * @author guru
  *
  */
 public class WorldTreeFactory {
 	private String	filePath 		= "init.properties";
 	private File	configFile		= null;
 	private Properties properties	= null;
 	
 	public WorldTreeFactory() {
 		configFile	= new File(filePath);
 		properties 	= new Properties();
 		if(!configFile.exists()) {
 			properties.put("Map.children.size", "2");
 			properties.put("Map.child0.name", "TestRoom");
 			properties.put("Room.children.size", "2");
 			properties.put("Room.children.names", "Normal Dungeon Altar");
 			properties.put("Room.child0.size", "4x4");
 			properties.put("Room.child1.size", "4x4");
 			properties.put("Room.child1.name", "TestRegion");
 			try {
 				properties.store(new FileWriter(filePath), "Auto-generated properties");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		try {
 			properties.load(new FileInputStream(configFile));
 		} catch (FileNotFoundException e) {
 			System.err.println(filePath + " does not exist");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public WorldTreeFactory(String filePath) {
 		this.filePath	= filePath;
 		configFile		= new File(filePath);
 		properties		= new Properties();
 		try {
 			properties.load(new FileInputStream(configFile));
 		} catch (FileNotFoundException e) {
 			System.err.println(filePath + " does not exist");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private  class Map extends WorldTree implements IMap {
 		public Map(String name, IWorldTree parent, Collection<Constraint> constraints) {
 			super(name, parent, constraints);
 		}
 
 		@Override
 		public void initialize() {
 			IWorldTree root = null;
 			List<IWorldTree> possibleRoots = new ArrayList<IWorldTree>();
 			possibleRoots.add(this);
 			while(true) {
 				List<IWorldTree> oldRootList = new ArrayList<IWorldTree>(possibleRoots);
 				root = possibleRoots.get(0);
 				while(root.children() != null) {
 					for(IWorldTree child : root.children())
 						possibleRoots.add(child);
 					possibleRoots.remove(root);
 					break;
 				}
 				
 				if(oldRootList.containsAll(possibleRoots) && possibleRoots.containsAll(oldRootList))
 					break;
 			}
 			
 			if(root == this) {
 				children = new ArrayList<IWorldTree>();
 				String countString 	= properties.getProperty("Map.children.size");
 				if(countString == null)
 					throw new IllegalStateException("Properties file has no size for " + this.getClass());
 				int childrenCount 	= Integer.parseInt(countString);
 				for(int i = 0; i < childrenCount; i++) {
 					String name 	= properties.getProperty("Map.child" + i + ".name");
 					if(name == null)
 						name = "Room" + i;
 					children.add(new Room(name, this, null));
 				}
 			}
 			
 			else {
 				for(IWorldTree child : possibleRoots) {
 					child.initialize();
 				}
 			}
 		}
 
 		@Override
 		public void fullInit() {
 			this.initialize();
 			List<IWorldTree> nodes = new ArrayList<IWorldTree>();
 			nodes.addAll(children);
 			
 			IWorldTree node = null;
 			try {
 				while(nodes.size() > 0) {
 					node = nodes.get(0);
 					node.initialize();
 					nodes.addAll(node.children());
 					nodes.remove(node);
 				}
 			} catch (Exception e) {
 				System.out.print("");
 			}
 		}
 		
 		@Override
 		public void move(test.ui.Direction direction) {
 			// TODO Auto-generated method stub
 			
 		}
 		
 		@Override
 		public IWorldTree neighbour(Direction direction) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 	}
 	
 	/**
 	 * Public interface for creating a new {@code IMap}
 	 * @param name {@code String} containing the name of the new {@code IMap}
 	 * @param parent {@code IWorldTree} representing the parent of the new {@code IMap}
 	 * @param constraints {@code Collection<Constraint>} containing a collection of constraints
 	 * @return {@code IMap} object corresponding to the specified parameters
 	 */
 	public IMap newMap(String name, IWorldTree parent, Collection<Constraint> constraints) {
 		return new Map(name, parent, constraints);
 	}
 	
 	private  class Room extends WorldTree implements IRoom {
 		public Room(String name, IWorldTree parent, Collection<Constraint> constraints) {
 			super(name, parent, constraints);
 		}
 
 //		The Room must decide the location of the tiles (I think..)
 		@Override
 		public void initialize() {
 			children = new ArrayList<IWorldTree>();
 			String[] regionNames = null;
 			
 			String countString	= properties.getProperty("Room.children.size");
 			if(countString == null)
 				throw new IllegalStateException("Properties file has no size for " + this.getClass());
 			int childrenCount 	= Integer.parseInt(countString);
 			
 			if(properties.getProperty("Room.children.names") != null)
 				regionNames = properties.getProperty("Room.children.names").split(" ");
 
 			for(int i = 0; i < childrenCount; i++) {
 				String name = properties.getProperty("Room.child" + i + ".name");
 				if(name == null) {
 					if(regionNames == null)
 						throw new IllegalStateException("Properties file has no name for child of " + this.getClass());
 					int nextInt = (new Random()).nextInt(regionNames.length);
 					name 		= regionNames[nextInt];
 				}
 				
 				if(properties.getProperty("Room.child" + i + ".size") == null)
 					throw new IllegalStateException("Properties file has no size for child " + i + " of " + this.getClass());
 				String[] size 		= properties.getProperty("Room.child" + i + ".size").split("x");
 				int[] dimensions 	= new int[] {Integer.parseInt(size[0]), Integer.parseInt(size[1])}; 
 				children.add(newRegion(name, this, null, new Space(dimensions[0], dimensions[1])));
 			}
 		}
 
 		@Override
 		public void move(test.ui.Direction direction) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public IWorldTree neighbour(Direction direction) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 	}
 	
 	/**
 	 * Public interface for creating a new {@code IRoom}
 	 * @param name {@code String} containing the name of the new {@code IRoom}
 	 * @param parent {@code IWorldTree} representing the parent of the new {@code IRoom}
 	 * @param constraints {@code Collection<Constraint>} containing a collection of constraints
 	 * @return {@code IRoom} object corresponding to the specified parameters
 	 */
 	public IRoom newRoom(String name, IWorldTree parent, Collection<Constraint> constraints) {
 		return new Room(name, parent, constraints);
 	}
 	
 	private  class Region extends WorldTree implements IRegion {
 		private Space space;
 		public Region(String name, IWorldTree parent, Collection<Constraint> constraints, Space space) {
 			super(name, parent, constraints);
 			this.space = space;
 //			First tile
 			ITile tile = initTile(new Coordinates(true, 0, 0));
 			space.setByCoord(new Coordinates(true, 0, 0), tile);
 			space.setCurrentCoordinates(new Coordinates(true, 0, 0));
 			initNeighbours();
 		}
 
 		@Override
 		public void initialize() {
 //			TODO: Ensure all pieces are traverse-able.
 			initRegion();
 			initString();
 		}
 		
 		/**
 		 * This method can be used to instantly initialize this Region and all its children tiles.
 		 */
 		private void initRegion() {
 			for(int i = 0; i < space.getYDimension(); i++) {
 				for(int j = 0; j < space.getXDimension(); j++) {
 					Coordinates coords = new Coordinates(true, j, i);
 					ITile tile = initTile(coords);
 					space.setByCoord(coords, tile);
 				}
 			}
 		}
 		
 		/**
 		 * Create a new {@code ITile} that satisfies the constraints specified
 		 * @param coordinates {@code Coordinates} with reference to which the {@code ITile} is to be created
 		 * @return {@code ITile}
 		 */
 		private ITile initTile(Coordinates coordinates) {
 			Coordinates coords = coordinates;
 			if(!coordinates.cartesian()) {
 				coords = space.arrayToCoord(coordinates);
 			}
 			
 			java.util.Map<String, String> interfaceMap = space.getValidInterfaces(coords);
 			String coordString = "(" + coords.x + "," + coords.y + ")";
 			ITile tile = newTile("tile" + coordString, coords, this, null, PieceFactory.randomPiece(interfaceMap));
 //			Collection<IWorldTree> children = null;		//TODO: Add a way to initialize Objects into Tiles
 			return tile;
 		}
 		
 		/**
 		 * Initialize the neighbours of the current cooridnates
 		 */
 		private void initNeighbours() {
 			Coordinates coords = space.currentCoordinates();
 			initNeighbours(coords);
 		}
 
 		/**
 		 * Initialize the neighbours of the specified coordinates
 		 * @param coordinates {@code Coordinates} specifying the coordinates to which neighbours are to be initialized
 		 */
 		private void initNeighbours(Coordinates coordinates) {
 			
 			List<Direction> directions = new ArrayList<Direction>(Space.listDirections());
 			
 			while(directions.size() > 0) {
 				Coordinates coords = new Coordinates(coordinates);
 				
 				int randomIndex = 0 + (int) (Math.random() * (directions.size() - 0) + 0);
 				Direction direction = directions.get(randomIndex);
 				switch(direction) {
 				case E:
 					coords.x++;
 					break;
 				case N:
 					coords.y++;
 					break;
 				case NE:
 					coords.x++;
 					coords.y++;
 					break;
 				case NW:
 					coords.x--;
 					coords.y++;
 					break;
 				case S:
 					coords.y--;
 					break;
 				case SE:
 					coords.x++;
 					coords.y--;
 					break;
 				case SW:
 					coords.x--;
 					coords.y--;
 					break;
 				case W:
 					coords.x--;
 					break;
 				default:
 					throw new IllegalStateException("Invalid direction? This should have never occured!\n");
 				}
 				
 //				Convert to Cartesian coordinates for validate
 				if(space.validate(coords) && space.getByCoord(coords) == null) {	
 					ITile tile = initTile(coords);
 					space.setByCoord(coords, tile);
 				}
 				directions.remove(direction);
 			}
 		}
 
 		/**
 		 * This is some really ugly code where multi-line visuals of each tile are split into single lines and
 		 * each line of each tile is appended together to the StringBuffer 
 		 * before moving on to the next line of every tile.
 		 * <br>
 		 * This is done to ensure that the visual of a room/region is as it should be!
 		 */
 		@Override
 		public void initString() {
 			stringRepresentation = space.getStringRepresentation();
 			prepareToString();
 		}
 		
 		@Override
 		public Collection<IWorldTree> children() {
 			if(space.collection().size() > 0)
 				return space.collection();
 			else
 				return null;
 		}
 		
 		@Override
 		public void move(test.ui.Direction direction) {		//FIXME
 			Coordinates coordinates = space.currentCoordinates();
 			switch(direction) {
 			case UP:
 				coordinates.y++;
 				break;
 			case DOWN:
 				coordinates.y--;
 				break;
 			case LEFT:
 				coordinates.x--;
 				break;
 			case RIGHT:
 				coordinates.x++;
 				break;
 			default:
 				throw new IllegalStateException("Only directions should be passed to move()");
 			}
 			
 			if(space.validate(coordinates)) {
 				space.setCurrentCoordinates(coordinates);
 				initNeighbours();
 			}
 		}
 
 		@Override
 		public IWorldTree neighbour(Direction direction) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 	}
 	
 	/**
 	 * Public interface for creating a new {@code IRegion}
 	 * @param name {@code String} containing the name of the new {@code IRegion}
 	 * @param parent {@code IWorldTree} representing the parent of the new {@code IRegion}
 	 * @param constraints {@code Collection<Constraint>} containing a collection of constraints
 	 * @return {@code IRegion} object corresponding to the specified parameters
 	 */
 	public IRegion newRegion(String name, IWorldTree parent, Collection<Constraint> constraints, Space space) {
 		return new Region(name, parent, constraints, space);
 	}
 	
 	/**
 	 * The Tile class is used to fill up the space.
 	 * @author guru
 	 *
 	 */
 	public class Tile extends WorldTree implements ITile {
 		public IPiece piece;
 		private Coordinates coordinates;
 		public Tile(String name, Coordinates coord, IWorldTree parent, Collection<Constraint> constraints, IPiece tilePiece) {
 			super(name, parent, constraints);
 			this.coordinates	= coord;
 			this.piece 			= tilePiece;
 			initialize();
 		}
 		
 		@Override
 		public IPiece piece() {
 			return piece;
 		}
 
 		@Override
 		public boolean hasInterface(TileInterfaceType it) {
 			return piece.getValidInterfaces().contains(it.toString());
 		}
 		
 		@Override
 		public void initialize() {
 			children = new ArrayList<IWorldTree>();
 		}
 		
 		@Override
 		public String toString() {
 			return this.name();
 		}
 		
 		public List<String> getStringRepresentation() {
			if(stringRepresentation == null)
 				stringRepresentation = new ArrayList<String>(Arrays.asList(piece().toString().split("\n")));
 			
 			return stringRepresentation;
 		}
 		
 		protected void addChild(IWorldTree child) {
 			this.children.add(child);
 			
 			StringBuffer visual = new StringBuffer();
 			for(String line : this.stringRepresentation)
 				visual.append(line);
 			
 			if(visual.toString().contains("  "))
 				visual = new StringBuffer(visual.toString().replace("  ", child.toString()));
 			else
 				System.err.println("Error: " + this.name() + " is unable to accomodate more children visually\n" +
 						"\tThe object still contains these children");
 		}
 
 		@Override
 		public void move(test.ui.Direction direction) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public IWorldTree neighbour(Direction direction) {
 			Region parent = (Region) this.parent;
 			
 			assert(this.coordinates.cartesian());
 			
 			Coordinates newCoords = new Coordinates(this.coordinates);
 			switch(direction) {
 			case E:
 				newCoords.x++;
 				break;
 			case N:
 				newCoords.y++;
 				break;
 			case NE:
 				newCoords.x++;
 				newCoords.y++;
 				break;
 			case NW:
 				newCoords.x--;
 				newCoords.y++;
 				break;
 			case S:
 				newCoords.y--;
 				break;
 			case SE:
 				newCoords.x++;
 				newCoords.y--;
 				break;
 			case SW:
 				newCoords.x--;
 				newCoords.y--;
 				break;
 			case W:
 				newCoords.x--;
 				break;
 			default:
 				throw new IllegalStateException("Should not have encountered invalid direction!");
 			}
 			
 			if(parent.space.validate(newCoords))
 				return parent.space.getByCoord(newCoords);
 			else
 				return null;
 		}
 
 		@Override
 		public void updateVisual(String visual) {
 			stringRepresentation.removeAll(stringRepresentation);
 			
 			for(String s : visual.split("\n")) {
 				stringRepresentation.add(s);
 			}
 		}
 	}
 	
 	/**
 	 * Public interface for creating a new {@code ITile}
 	 * @param name {@code String} containing the name of the new {@code ITile}
 	 * @param parent {@code IWorldTree} representing the parent of the new {@code ITile}
 	 * @param constraints {@code Collection<Constraint>} containing a collection of constraints
 	 * @return {@code ITile} object corresponding to the specified parameters
 	 */
 	public ITile newTile(String name, Coordinates coordinates, IWorldTree parent,  
 			Collection<Constraint> constraints, IPiece tilePiece) {
 		return new Tile(name, coordinates, parent, constraints, tilePiece);
 	}
 }
