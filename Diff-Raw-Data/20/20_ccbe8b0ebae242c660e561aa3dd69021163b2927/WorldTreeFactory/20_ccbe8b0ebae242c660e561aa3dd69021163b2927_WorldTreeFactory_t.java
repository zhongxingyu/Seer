 package internal.tree;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Serializable;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Properties;
 import java.util.Random;
 
 import internal.parser.ParseException;
 import internal.parser.Parser;
 import internal.parser.containers.Constraint;
 import internal.parser.containers.Datum;
 import internal.parser.containers.IStatement;
 import internal.parser.containers.StatementType;
 import internal.parser.containers.condition.ICondition;
 import internal.parser.containers.property.Property;
 import internal.parser.containers.property.PropertyDef;
 import internal.parser.containers.property.PropertyDef.RandomSpec;
 import internal.parser.resolve.ResolutionEngine;
 import internal.parser.resolve.Result;
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
 import static internal.Helper.Hierarchy;
 
 /**
  * Factory class responsible for generating objects of {@code IWorldTree}
  * @author guru
  *
  */
 public class WorldTreeFactory implements Serializable {
 	private static final long serialVersionUID = -981440934139213907L;
 	
 	private String	propFilePath 			= "init.properties";
 	private String worldDefPath				= null;
 	private List<Constraint> constraints 	= null;
 	private List<PropertyDef> definitions 	= null;
 	private Properties properties			= null;
 	
 	public WorldTreeFactory() {
 		File propertiesFile	= new File(propFilePath);
 		properties 	= new Properties();
 		try {
 			properties.load(new FileInputStream(propertiesFile));
 		} catch (FileNotFoundException e) {
 			System.err.println("Invalid properties file!\n" + propFilePath + " does not exist");
 		} catch (IOException e) {
 			System.err.println(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 	
 	public WorldTreeFactory(String propFilePath) {
 		this(propFilePath, null);
 	}
 	public WorldTreeFactory(String propFilePath, String worldDefPath) {
 		this.propFilePath	= propFilePath;
 		this.worldDefPath	= worldDefPath;
 		properties			= new Properties();
 		try {
 			File propertiesFile	= new File(propFilePath);
 			properties.load(new FileInputStream(propertiesFile));
 		} catch (FileNotFoundException e) {
 			System.err.println(propFilePath + " does not exist");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		if(this.worldDefPath == null) {
 			return;
 		}
 		else
 			loadDefinitions();
 	}
 	
 	private void loadDefinitions() {
 		File worldDefinitionsFile = new File(worldDefPath);
 		if(worldDefinitionsFile.exists()) {
 			constraints = new ArrayList<Constraint>();
 			definitions = new ArrayList<PropertyDef>();
 			try {
 				Parser parser = new Parser(new StringReader(""));
 				BufferedReader in = new BufferedReader(new FileReader(worldDefinitionsFile));
 				
 				
 				String line = null;
 				StringBuffer sb = new StringBuffer();
 				while( (line = in.readLine()) != null) {
 //					Trim the string
 					line = line.trim();
 //					Ignore comments
 					if(line.startsWith("#"))
 						continue;
 					sb.append(line + "\n");
 					if(sb.toString().contains(";")) {
 						parser.ReInit(new StringReader(sb.toString()));
 						IStatement statement = parser.parse();
 						if(statement.getType().equals(StatementType.CONSTRAINT))
 							constraints.add((Constraint)statement);
 						else if(statement.getType().equals(StatementType.PROPERTYDEF))
 							definitions.add((PropertyDef)statement);
 						else
 							System.err.println("Warning: WorldDefinitions contains :\n\t" + statement);
 						sb.delete(0, sb.length());
 					}
 				}
 				
 				in.close();
 			} catch(IOException e) {
 				System.err.println(e.getMessage());
 			} catch(ParseException e) {
 				System.err.println(e.getMessage());
 			}
 		}
 		else
 			throw new IllegalArgumentException("Definitions file :" + worldDefPath + " does not exist!");
 	}
 	
 	public Properties properties() {
 		return properties;
 	}
 	
 	public Collection<Constraint> constraints() {
 		return constraints;
 	}
 	
 	public Collection<PropertyDef> definitions() {
 		return definitions;
 	}
 
 	private  class Map extends WorldTree implements IMap {
 		private static final long serialVersionUID = 8573337917599165863L;
 
 		public Map(String name, IWorldTree parent, Collection<Constraint> constraints, Collection<PropertyDef> definitions) {
 			super(name, parent, constraints);
 //			FIXME: Added this to solve NPE on constraints()
 			if(constraints == null)
 				this.setConstraints(new ArrayList<Constraint>(0));
 			this.setDefinitions(definitions);
 		}
 
 		@Override
 		public void initialize() {
 			children = new ArrayList<IWorldTree>();
 			String countString 	= properties.getProperty("Map.children.size");
 			if(countString == null)
 				throw new IllegalStateException("Properties file has no size for " + this.getClass());
 			int childrenCount 	= Integer.parseInt(countString);
 			for(int i = 0; i < childrenCount; i++) {
 				String name 	= properties.getProperty("Map.child" + i + ".name");
 				if(name == null)
 					name = "Room" + i;
 				children.add(new Room(name, this));
 			}
 		}
 		
 		@Override
 		public void initRooms() {
 			initialize();
 		}
 		
 		@Override
 		public void initRegions() {
 			for(IWorldTree child : getRooms()) {
 				child.initialize();
 			}
 		}
 		
 		@Override
 		public void initTiles() {
 			for(IWorldTree child : getRegions()) {
 				child.initialize();
 			}
 		}
 
 		@Override
 		public void fullInit() {
 			this.initialize();
 			List<IWorldTree> nodes = new ArrayList<IWorldTree>();
 			if(this.children() != null)
 				nodes.addAll(this.children());
 			
 			IWorldTree node = null;
 			try {
 				while(nodes.size() > 0) {
 					node = nodes.get(0);
 					node.initialize();
 					if(node.children() != null)
 						nodes.addAll(node.children());
 					nodes.remove(node);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
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
 
 		private Collection<IWorldTree> allNodes() {
 			List<IWorldTree> result = new ArrayList<IWorldTree>();
 			result.add(this);
 			IWorldTree node = null;
 			try {
 				int index = 0;
 				while(index < result.size()) {
 					node = result.get(index);
 					if(node.children() != null)
 						result.addAll(node.children());
 					index++;
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return result;
 		}
 		private Collection<IWorldTree> getRooms() {
 			Collection<IWorldTree> result 	= new ArrayList<IWorldTree>();
 			Collection<IWorldTree> nodes 	= allNodes();
 			
 			for(IWorldTree n : nodes) {
 				if(n.getClass().equals(WorldTreeFactory.Room.class))
 					result.add(n);
 			}
 			return result;
 		}
 		
 		private Collection<IWorldTree> getRegions() {
 			Collection<IWorldTree> result 	= new ArrayList<IWorldTree>();
 			Collection<IWorldTree> nodes 	= allNodes();
 			
 			for(IWorldTree n : nodes) {
 				if(n.getClass().equals(WorldTreeFactory.Region.class))
 					result.add(n);
 			}
 			return result;
 		}
 		
 		private Collection<IWorldTree> getTiles() {
 			Collection<IWorldTree> result 	= new ArrayList<IWorldTree>();
 			Collection<IWorldTree> nodes 	= allNodes();
 			for(IWorldTree n : nodes) {
 				if(n.getClass().equals(WorldTreeFactory.Tile.class))
 					result.add(n);
 			}
 			return result;
 		}
 		
 		@Override
 		public void fill() {
 			for(IWorldTree r : getRegions()) {
 				((Region) r).initRegion();
 			}
 		}
 
 		@Override
 		public void materializeConstraints() {
 			for(IWorldTree node : allNodes()) {
 				node.pushDownConstraints();
 				
 //				for(Constraint constraint : node.constraints()) {
 //					if(constraint.type().equals(Constraint.Type.PROGRAM_GENERATED))
 //						node.removeConstraint(constraint);
 //				}
 			}
 		}
 	}
 	
 	/**
 	 * Public interface for creating a new {@code IMap}
 	 * @param name {@code String} containing the name of the new {@code IMap}
 	 * @param parent {@code IWorldTree} representing the parent of the new {@code IMap}
 	 * @return {@code IMap} object corresponding to the specified parameters
 	 */
 	public IMap newMap(String name, IWorldTree parent) {
 		return new Map(name, parent, constraints, new ArrayList<PropertyDef>(definitions));
 	}
 	
 	private  class Room extends WorldTree implements IRoom {
 		private static final long serialVersionUID = 3733417833903485812L;
 
 		public Room(String name, IWorldTree parent) {
 			super(name, parent, new ArrayList<Constraint>(0));
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
 				children.add(newRegion(name, this, new Space(dimensions[0], dimensions[1])));
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
 	public IRoom newRoom(String name, IWorldTree parent) {
 		return new Room(name, parent);
 	}
 	
 	private  class Region extends WorldTree implements IRegion {
 		private static final long serialVersionUID = 1774553850572473379L;
 		
 		private Space space;
 		public Region(String name, IWorldTree parent, Space space) {
 			super(name, parent, new ArrayList<Constraint>(0));
 			this.space = space;
 		}
 
 		@Override
 		public void setStartEndTiles() {
 //			First tile
 			int startX = 0 + (int) (Math.random() * space.getXDimension());
 			int startY = 0 + (int) (Math.random() * space.getYDimension());
 			Coordinates startCoords = new Coordinates(true, startX, startY);
 			ITile tile = initTile(startCoords, null);	//FIXME
 			tile.addProperty("start", new Datum.Bool(true));
 			tile.addArtifact("S");
 			space.setByCoord(startCoords, tile);
 			space.setCurrentCoordinates(startCoords);
 			initNeighbours();
 			
 //			Set the end tile
 			Coordinates endCoords = null;
 //			Ensure start != end
 			while(endCoords == null) {
 				int endX = 0 + (int) (Math.random() * space.getXDimension());
 				int endY = 0 + (int) (Math.random() * space.getYDimension());
 				if(startX != endX && startY != endY)
 					endCoords = new Coordinates(true, endX, endY);
 			}
 			tile = initTile(endCoords, null);	//FIXME
 			tile.addProperty("end", new Datum.Bool(true));
 			tile.addArtifact("E");
 			space.setByCoord(endCoords, tile);
 		}
 		
 		@Override
 		public void initialize() {
 //			FIXME :Perhaps this should be done better
 			for(int i = 0; i < space.getYDimension(); i++) {
 				for(int j = 0; j < space.getXDimension(); j++) {
 					Coordinates coords = new Coordinates(true, j, i);
 					String coordString = "(" + coords.x + "," + coords.y + ")";
 					ITile tile = new Tile("tile" + coordString, coords, this, PieceFactory.newPiece(""));
 					space.setByCoord(coords, tile);
 				}
 			}
 //			initRegion();
 			initString();
 		}
 		
 		/**
 		 * This method can be used to fully initialize this Region with all its children tiles.
 		 */
 		protected void initRegion() {
 			for(int i = 0; i < space.getYDimension(); i++) {
 				for(int j = 0; j < space.getXDimension(); j++) {
 					Coordinates coords = new Coordinates(true, j, i);
 					ITile tile = initTile(coords, constraints);
 					ITile existingTile = space.getByCoord(coords);
 					existingTile.setPiece(tile.piece());
 				}
 			}
 			initString();
 		}
 		
 		/**
 		 * Create a new {@code ITile} that satisfies the constraints specified
 		 * @param coordinates {@code Coordinates} with reference to which the {@code ITile} is to be created
 		 * @param constraints {@code Collection<Constraint>} containing constraints pertaining to the new {@code ITile}
 		 * @return {@code ITile}
 		 */
 		private ITile initTile(Coordinates coordinates, Collection<Constraint> constraints) {
 			Coordinates coords = coordinates;
 			if(!coordinates.cartesian()) {
 				coords = space.arrayToCoord(coordinates);
 			}
 			
 			java.util.Map<String, String> interfaceMap = space.getValidInterfaces(coords);
 			String coordString = "(" + coords.x + "," + coords.y + ")";
 			ITile tile = newTile("tile" + coordString, coords, this, PieceFactory.randomPiece(interfaceMap));
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
 					ITile tile = initTile(coords, null);
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
 		 * @return List<String> containing the string representation
 		 */
 		@Override
 		public List<String> initString() {
 			List<String> stringRepresentation = space.getStringRepresentation();
 			prepareToString(stringRepresentation);
 			return stringRepresentation;
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
 	public IRegion newRegion(String name, IWorldTree parent, Space space) {
 		return new Region(name, parent, space);
 	}
 	
 	/**
 	 * The Tile class is used to fill up the space.
 	 * @author guru
 	 *
 	 */
 	public class Tile extends WorldTree implements ITile {
 		private static final long serialVersionUID = 7530444796681530305L;
 		
 		public IPiece piece;
 		private List<String> artifacts;
 		public Tile(String name, Coordinates coord, IWorldTree parent, IPiece tilePiece) {
 			super(name, parent, new ArrayList<Constraint>(0));
 			this.piece 			= tilePiece;
 			this.artifacts		= new ArrayList<String>(0);
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
 			children = null;
 		}
 		
 		protected void addChild(IWorldTree child) {
 			throw new IllegalStateException("Cannot add a child to the lowest level in the hierarchy!\n");
 		}
 
 		@Override
 		public void move(test.ui.Direction direction) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public IWorldTree neighbour(Direction direction) {
 			Region parent = (Region) this.parent;
 			
 			String name = this.name();
			Coordinates coordinates = Coordinates.stringToCoord(name.substring(name.indexOf("(")));
 			
 			assert(coordinates.cartesian());
 			
 			Coordinates newCoords = new Coordinates(coordinates);
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
 		public String toString() {
 			return this.name();
 		}
 		
 		public List<String> getStringRepresentation() {
 			List<String> stringRepresentation = new ArrayList<String>(Arrays.asList(piece().toString().split("\n")));
 			updateVisuals(stringRepresentation);
 			return stringRepresentation;
 		}
 		
 		private void updateVisuals(List<String> stringRepresentation) {
 			StringBuffer sb = new StringBuffer();
 			for(String s : stringRepresentation)
 				sb.append(s + "\n");
 			
 			artifacts.clear();
 			for(java.util.Map.Entry<String, Datum> entry : this.properties().entrySet()) {
 				String property = entry.getKey();
 				Datum value		= entry.getValue();
 				
 				artifacts.add(property.charAt(0) + "=" + value);
 			}
 			
 			for(String artifact : artifacts) {
 				StringBuffer searchString = new StringBuffer();
 				while(searchString.length() < artifact.length())
 					searchString.append(" ");
 				int index = sb.indexOf(searchString.toString(), 12);
 				if(index < 0)
 					System.err.println("Unable to update current tile visual! No space for artifact :" + artifact);
 				else {
 					sb.replace(index + 1, index + artifact.length() + 1, artifact);
 				}
 			}
 			
 			stringRepresentation.clear();
 			for(String s : sb.toString().split("\n"))
 				stringRepresentation.add(s);
 		}
 
 		@Override
 		public void addArtifact(String artifact) {
 			artifacts.add(artifact);
 		}
 
 		@Override
 		public void removeArtifact(String artifact) {
 				artifacts.remove(artifact);
 		}
 		
 		@Override
 		public Collection<String> artifacts() {
 			return artifacts;
 		}
 
 		@Override
 		public void setPiece(IPiece piece) {
 			this.piece = piece;
 		}
 	}
 	
 	/**
 	 * Public interface for creating a new {@code ITile}
 	 * @param name {@code String} containing the name of the new {@code ITile}
 	 * @param parent {@code IWorldTree} representing the parent of the new {@code ITile}
 	 * @param constraints {@code Collection<Constraint>} containing a collection of constraints
 	 * @return {@code ITile} object corresponding to the specified parameters
 	 */
 	public ITile newTile(String name, Coordinates coordinates, IWorldTree parent, IPiece tilePiece) {
 		return new Tile(name, coordinates, parent, tilePiece);
 	}
 }
