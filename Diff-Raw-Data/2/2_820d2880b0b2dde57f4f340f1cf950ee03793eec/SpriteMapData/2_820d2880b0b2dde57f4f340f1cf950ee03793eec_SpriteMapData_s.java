 package riskyspace.logic;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import riskyspace.GameManager;
 import riskyspace.logic.data.AnimationData;
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
 	
 	private int rows, cols;
 	private List<PlanetData> planetData = new ArrayList<PlanetData>();
 	private List<ColonizerData> colonizerData = new ArrayList<ColonizerData>();
 	private List<FleetData> fleetData = new ArrayList<FleetData>();
 	private List<ColonyData> colonyData = new ArrayList<ColonyData>();
 	private List<AnimationData> animData = new ArrayList<AnimationData>();
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
 		seen.put(Player.YELLOW, new HashSet<Position>());
 	}
 	
 	/**
 	 * Create Sprite Data for a Player. This data is used by SpriteMap to draw contents of the game.
 	 * @param player The Player that data is requested for.
 	 * @return SpriteMapData for a Player.
 	 */
 	public static SpriteMapData getData(Player player) {
 		SpriteMapData data = new SpriteMapData();
 		data.rows = world.getRows();
 		data.cols = world.getCols();
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
 				if (terr.hasFleet()) {
 					if (FleetMove.isMoving()) {
 						for (Fleet fleet : terr.getFleets()) {
 							if (GameManager.INSTANCE.hasPath(fleet) && !terr.hasConflict() && fleet.hasEnergy() &&
 									GameManager.INSTANCE.getCurrentPlayer() == fleet.getOwner()) {
 								Position[] steps = GameManager.INSTANCE.getPath(fleet);
 								data.animData.add(new AnimationData(pos, fleet.getOwner(), fleet.getFlagship(), FleetMove.stepTime(), steps));			
 							} else {
 								addFleetData(data, pos, fleet);
 							}
 						}
 					} else {
 						for (Fleet fleet : terr.getFleets()) {
 							addFleetData(data, pos, fleet);
 						}
 						int size = 0;
 						for (Fleet fleet : terr.getFleets()) {
 							size += fleet.fleetSize();
 						}
 						int colonizers = world.getTerritory(pos).shipCount(ShipType.COLONIZER);
 						size = size - colonizers;
 						data.fleetSize.put(pos, size);
 						data.colonizerAmount.put(pos, colonizers);
 					}
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
 
 	private static void addFleetData(SpriteMapData data, Position pos, Fleet fleet) {
 		boolean existed = false;
 		if (fleet.getFlagship() != ShipType.COLONIZER) {
 			for (FleetData fleetData : data.fleetData) {
 				if (fleetData.getPosition().equals(pos)) {
 					if (fleetData.getFlagships().compareTo(fleet.getFlagship()) > 0) {
 						fleetData.setFlagShip(fleet.getFlagship());
 						if (GameManager.INSTANCE.hasPath(fleet)) {
 							fleetData.setSteps(GameManager.INSTANCE.getPath(fleet));
 						} else {
 							fleetData.setSteps(GameManager.INSTANCE.getPath(fleet));
 						}
 					} else if (fleetData.getSteps()[1] == null && GameManager.INSTANCE.hasPath(fleet) && GameManager.INSTANCE.getPath(fleet).length >= 1) {
 						fleetData.setSteps(GameManager.INSTANCE.getPath(fleet));
 					}
 					existed = true;
 				}
 			}
 			if (!existed) {
 				if (GameManager.INSTANCE.hasPath(fleet)) {
 					data.fleetData.add(new FleetData(pos, fleet.getOwner(), fleet.getFlagship(), GameManager.INSTANCE.getPath(fleet)));
 				} else {
 					data.fleetData.add(new FleetData(pos, fleet.getOwner(), fleet.getFlagship(), new Position[2]));
 				}
 			}
 		} else if (fleet.getFlagship() == ShipType.COLONIZER) {
 			for (ColonizerData colonizerData : data.colonizerData) {
 				if (colonizerData.getPosition().equals(pos)) {
 					if (colonizerData.getSteps()[1] == null && GameManager.INSTANCE.hasPath(fleet) && GameManager.INSTANCE.getPath(fleet).length >= 1) {
 						colonizerData.setSteps(GameManager.INSTANCE.getPath(fleet));
 						existed = true;
 					}
 				}
 			}
 			if (!existed) {
 				if (GameManager.INSTANCE.hasPath(fleet)) {
 					data.colonizerData.add(new ColonizerData(pos, fleet.getOwner(), GameManager.INSTANCE.getPath(fleet)));						
 				} else {
 					data.colonizerData.add(new ColonizerData(pos, fleet.getOwner(), new Position[2]));
 				}
 			}
 		}
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
 	
 	public List<AnimationData> getAnimationData() {
 		return animData;
 	}
 	
 	public int getFleetSize(Position position) {
 		if (fleetSize.containsKey(position)) {
 			return fleetSize.get(position);			
 		}
 		return 0;
 	}
 	
 	public int getColonizerAmount(Position position) {
 		if (colonizerAmount.containsKey(position)) {
			return fleetSize.get(position);
 		}
 		return 0;
 	}
 
 	public int getRows() {
 		return rows;
 	}
 	
 	public int getCols() {
 		return cols;
 	}
 }
