 package riskyspace.logic;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import riskyspace.GameManager;
 import riskyspace.logic.data.ColonizerData;
 import riskyspace.logic.data.ColonyData;
 import riskyspace.logic.data.FleetData;
 import riskyspace.logic.data.PlanetData;
 import riskyspace.model.Fleet;
 import riskyspace.model.Player;
 import riskyspace.model.Position;
 import riskyspace.model.ShipType;
 import riskyspace.model.Sight;
 import riskyspace.model.Territory;
 import riskyspace.model.World;
 
 public class SpriteMapData implements Serializable {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6477313136946984127L;
 
 	private static World world;
 
 	private static final Set<Position> allPos = new HashSet<Position>();
 	private Set<Position> visible = new HashSet<Position>();
 	private Set<Position> fog = new HashSet<Position>(allPos);
 	private static Map<Player, Set<Position>> seen = new HashMap<Player, Set<Position>>();
 	
 	private List<PlanetData> planetData = new ArrayList<PlanetData>();
 	private List<ColonizerData> colonizerData = new ArrayList<ColonizerData>();
 	private List<FleetData> fleetData = new ArrayList<FleetData>();
 	private List<ColonyData> colonyData = new ArrayList<ColonyData>();
 	private Map<Position, Integer> fleetSize = new HashMap<Position, Integer>();
 	private Map<Position, Integer> colonizerAmount = new HashMap<Position, Integer>();
 	private Position[][] paths = null;
 	
 	private SpriteMapData() {}
 	
 	public static void init(World world) {
 		SpriteMapData.world = world;
 		for (int row = 1; row <= world.getRows(); row++) {
 			for (int col = 1; col <= world.getCols(); col++) {
 				allPos.add(new Position(row, col));
 			}
 		}
 		seen.put(Player.BLUE, new HashSet<Position>());
 		seen.put(Player.RED, new HashSet<Position>());
 		seen.put(Player.GREEN, new HashSet<Position>());
 		seen.put(Player.PINK, new HashSet<Position>());
 	}
 	
 	/**
 	 * Create Sprite Data for a Player. This data is used by SpriteMap to draw contents of the game.
 	 * @param player The Player that data is requested for.
 	 * @return SpriteMapData for a Player.
 	 */
 	public static SpriteMapData getData(Player player) {
 		SpriteMapData data = new SpriteMapData();
 		
 		/*
 		 * Calculate visible positions for player
 		 */
 		for (Position pos : world.getContentPositions()) {
 			Territory terr = world.getTerritory(pos);
 			List<Sight> list = new ArrayList<Sight>();
 			for (Fleet fleet : terr.getFleets()) {
 				if (fleet.getOwner() == player) {
 					list.add(fleet);
 				}
 			}
 			if (terr.hasColony() && terr.getColony().getOwner() == player) {
 				list.add(terr.getColony());
 			}
 			for (Sight s : list) {
 				for (int row = pos.getRow() - s.getSightRange(); row <= pos.getRow() + s.getSightRange(); row++) {
 					for (int col = pos.getCol() - s.getSightRange(); col <= pos.getCol() + s.getSightRange(); col++) {
 						if (pos.distanceTo(new Position(row, col)) <= s.getSightRange()) {
 							data.visible.add(new Position(row, col));
 						}
 					}
 				}
 			}
 		}
 		seen.get(player).addAll(data.visible);
 		
 		for (Position pos : world.getContentPositions()) {
 			/*
 			 * Only gather data for visible positions
 			 */
 			if (data.visible.contains(pos)) {
 				Territory terr = world.getTerritory(pos);
 				if (terr.hasPlanet()) {
 					data.planetData.add(new PlanetData(pos, null, terr.getPlanet().getType()));
 					if (terr.hasColony()) {
 						data.colonyData.add(new ColonyData(pos, terr.getColony().getOwner()));
 					}
 				}
 				if (terr.hasColonizer()) {
 					data.colonizerData.add(new ColonizerData(pos, terr.controlledBy()));
 					data.colonizerAmount.put(pos, world.getTerritory(pos).shipCount(ShipType.COLONIZER));
 				}
 				if (terr.hasFleet()) {
 					if (terr.getFleetsFlagships() != ShipType.COLONIZER) {
 						data.fleetData.add(new FleetData(pos, terr.controlledBy(), terr.getFleetsFlagships()));
 					}
 					int size = 0;
 					for(Fleet fleet : world.getTerritory(pos).getFleets()) {
 						size += fleet.fleetSize();
 					}
 					size = size - world.getTerritory(pos).shipCount(ShipType.COLONIZER);
 					data.fleetSize.put(pos, size);
 				}
 			} else if (SpriteMapData.seen.get(player).contains(pos)) {
 				if (world.getTerritory(pos).hasPlanet()) {
 					data.planetData.add(new PlanetData(pos, null, world.getTerritory(pos).getPlanet().getType()));
 				}
 			}
 		}
 		data.fog.removeAll(data.visible);
 		data.paths = GameManager.INSTANCE.getPaths(player);
 		return data;
 	}
 
 	public Position[][] getPaths() {
 		return paths;
 	}
 
 	public Set<Position> getAllPositions() {
		Set<Position> all = new HashSet<Position>(fog);
		all.addAll(visible);
		return all;
 	}
 	
 	public Set<Position> getFog() {
 		return fog;
 	}
 	
 	public List<PlanetData> getPlanetData() {
 		return planetData;
 	}
 
 	public List<FleetData> getFleetData() {
 		return fleetData;
 	}
 
 	public List<ColonizerData> getColonizerData() {
 		return colonizerData;
 	}
 
 	public List<ColonyData> getColonyData() {
 		return colonyData;
 	}
 
 	public int getFleetSize(Position position) {
 		return fleetSize.get(position);
 	}
 	
 	public int getColonizerAmount(Position position) {
 		return colonizerAmount.get(position);
 	}
 }
