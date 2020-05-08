 package riskyspace;
 
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import riskyspace.data.GameDataHandler;
 import riskyspace.data.SavedGame;
 import riskyspace.logic.Battle;
 import riskyspace.logic.FleetMove;
 import riskyspace.logic.Path;
 import riskyspace.logic.data.BattleStats;
 import riskyspace.model.BuildAble;
 import riskyspace.model.Colony;
 import riskyspace.model.Fleet;
 import riskyspace.model.Player;
 import riskyspace.model.PlayerStats;
 import riskyspace.model.Position;
 import riskyspace.model.Resource;
 import riskyspace.model.ShipType;
 import riskyspace.model.Territory;
 import riskyspace.model.World;
 import riskyspace.model.building.Ranked;
 import riskyspace.network.PlayerInfo;
 import riskyspace.services.Event;
 import riskyspace.services.EventBus;
 
 public enum GameManager {
 	INSTANCE;
 	
 	private Player[] players = {Player.BLUE, Player.RED, Player.GREEN, Player.YELLOW};
 	
 	private Player currentPlayer = null;
 	private boolean initiated = false;
 	private World world = null;
 	private int turn;
 	private boolean started = false;
 	
 	private List<Player> activePlayers = new ArrayList<Player>();
 	private Map<Player, PlayerInfo> playerInfo = new HashMap<Player, PlayerInfo>();
 	private Map<Player, Selection> selections = new HashMap<Player, Selection>();
 	
 	private class Selection {
 		/*
 		 * Fleet selection and helper variables
 		 */
 		private Position lastFleetSelectPos = null;
 		private int fleetSelectionIndex = 0;
 		private Set<Fleet> selectedFleets = new HashSet<Fleet>();
 		/*
 		 * Paths
 		 */
 		private Map<Fleet, Path> fleetPaths = new HashMap<Fleet, Path>();
 		/*
 		 * Position selected (Colony, Planet)
 		 */
 		private Position selectedPosition = null;
 		
 	}
 	
 	/**
 	 * Used for Save Game
 	 * @param game Loaded game
 	 * @param ips 
 	 */
 	public void init(SavedGame game, String[] ips) {
 		System.out.println("was here");
 		this.world = game.getWorld();
 		this.turn = game.getTurn();
 		this.currentPlayer = game.getCurrentPlayer();
 		this.activePlayers = game.getPlayers();
 		int i = 0;
 		for (Player player : activePlayers) {
 			PlayerInfo info = new PlayerInfo();
 			if (ips != null) {
 				info.setIP(ips[i]);
 				i++;
 			}
 			playerInfo.put(player, info);
 			selections.put(player, new Selection());
 		}
 		initiated = true;
 	}
 	
 	/**
 	 * Used for Network Game
 	 * @param world
 	 */
 	public void init(World world) {
 		this.world = world;
 		turn = 1;
 		initiated = true;
 	}
 	
 	/**
 	 * Used for Hot-Seat Game
 	 * @param world
 	 * @param numberOfPlayers
 	 */
 	public void init(World world, int numberOfPlayers) {
 		init(world);
 		initPlayers(numberOfPlayers);
 	}
 	
 	public void start(boolean changePlayer) {
 		if(!started){
 			if (changePlayer) {
 				changePlayer();
 			}
 			world.updatePlayerStats(getCurrentPlayer());
 			started = true;
 		}
 	}
 	
 	public void initPlayers(int numberOfPlayers) {
 		for (int i = 0; i < numberOfPlayers; i++) {
 			activePlayers.add(players[i]);
 		}
 		for (Player player : activePlayers) {
 			playerInfo.put(player, new PlayerInfo());
 			selections.put(player, new Selection());
 		}
 	}
 	
 	public synchronized Player addPlayer(InetAddress ip){
 		for (Player player : activePlayers) {
 			if (playerInfo.get(player).getIP().equals(ip.getHostAddress())){
 				return player;
 			}
 		}
 		Player player = players[activePlayers.size()];
 		activePlayers.add(player);
 		playerInfo.put(player, new PlayerInfo());
 		playerInfo.get(player).setIP(ip);
 		selections.put(player, new Selection());
 		return player;
 	}
 
 	public Player getCurrentPlayer() {
 		return currentPlayer;
 	}
 	
 	public List<Player> getActivePlayers() {
 		return new LinkedList<Player>(activePlayers);
 	}
 	
 	public int getTurn() {
 		return turn;
 	}
 	
 	public PlayerStats getStats(Player player) {
 		return world.getStats(player);
 	}
 
 	public Integer getWorldRows() {
 		return world.getRows();
 	}
 
 	public Integer getWorldCols() {
 		return world.getCols();
 	}
 	
 	public PlayerInfo getInfo(Player player) {
 		return playerInfo.get(player);
 	}
 	
 	/**
 	 * Checks if a fleet has a path with more than its current position
 	 * @param fleet The Fleet to check
 	 * @return true if there is a path with at least than 1 step.
 	 */
 	public boolean hasPath(Fleet fleet) {
 		return (selections.get(fleet.getOwner()).fleetPaths.get(fleet) != null) && (selections.get(fleet.getOwner()).fleetPaths.get(fleet).getLength() > 0);
 	}
 	
 	public Position[] getPath(Fleet fleet) {
 		return selections.get(fleet.getOwner()).fleetPaths.get(fleet).getPositions();	
 	}
 	
 	private void changePlayer() {
 		currentPlayer = activePlayers.get(((activePlayers.indexOf(currentPlayer) + 1) % activePlayers.size()));
 		Event event = new Event(Event.EventTag.ACTIVE_PLAYER_CHANGED, currentPlayer);
 		EventBus.SERVER.publish(event);
 		world.giveIncome(currentPlayer);
 		world.updatePlayerStats(currentPlayer);
 		world.processBuildQueue(currentPlayer);
 		world.updatePlayerStats(currentPlayer);
 		world.resetShips(currentPlayer);
 		updateSelections();
 		if (currentPlayer == players[0]) {
 			turn++;
 		}
 		if (turn % 2 == 0) {
 			GameDataHandler.autoSave(this.world, activePlayers, currentPlayer, turn, "Annihilation");
 		}
 	}
 
 	public synchronized void handleEvent(Event evt, Player player) {
 		if (!initiated || (!activePlayers.contains(player) && player != null)) {
 			return;
 		}
 		if (evt.getTag() == Event.EventTag.SAVE_GAME) {
 			Calendar cal = GregorianCalendar.getInstance();
 			String year = ("" + cal.get(Calendar.YEAR)).substring(2);
 			String month = "" + (cal.get(Calendar.MONTH) + 1);
 			if (month.length() == 1) {
 				month = 0 + month;
 			}
 			String day = "" + cal.get(Calendar.DATE);
 			if (day.length() == 1) {
 				day = 0 + day;
 			}
 			String hour = "" + cal.get(Calendar.HOUR_OF_DAY);
 			if (hour.length() == 1) {
 				hour = 0 + hour;
 			}
 			String minute = "" + cal.get(Calendar.MINUTE);
 			if (minute.length() == 1) {
 				minute = 0 + minute;
 			}
			String saveName = year + "-" + month + "-" + day + " " + hour + ":" + minute;
 			System.out.println(saveName);
 			GameDataHandler.saveGame(world, activePlayers, player, turn, "Annihilation", saveName);
 			return;
 		}
 		/*
 		 * View triggered Events
 		 */
 		if (!FleetMove.isMoving()) {
 			if(evt.getObjectValue() instanceof Position){
 				Position pos = (Position) evt.getObjectValue();
 				if (evt.getTag() == Event.EventTag.SET_PATH && player == getCurrentPlayer()) {
 					setPath(pos);
 				} else if (evt.getTag() == Event.EventTag.COLONIZER_SELECTED) {
 					setColonizerSelected(pos, player);
 				} else if (evt.getTag() == Event.EventTag.ADD_FLEET_SELECTION) {
 					addFleetSelection(pos, player);
 				} else if (evt.getTag() == Event.EventTag.NEW_FLEET_SELECTION) {
 					newFleetSelection(pos, player);
 				}
 			} else if (evt.getTag() == Event.EventTag.NEXT_TURN && player == getCurrentPlayer()) {
 				changePlayer();
 			} else if (evt.getTag() == Event.EventTag.NEW_FLEET_SELECTION) {
 				newFleetsSelection(evt, player);
 			} else if (evt.getTag() == Event.EventTag.MOVE && player == getCurrentPlayer()) {
 				performMoves();
 			} else if (evt.getTag() == Event.EventTag.QUEUE_SHIP && player == getCurrentPlayer()) {
 				queueBuildAble((BuildAble) evt.getObjectValue(), player);
 			} else if (evt.getTag() == Event.EventTag.QUEUE_BUILDING && player == getCurrentPlayer()) {
 				queueBuilding((String)evt.getObjectValue(), player);
 			} else if (evt.getTag() == Event.EventTag.COLONIZE_PLANET && player == getCurrentPlayer()) {
 				colonizePlanet(player);
 			} 
 		} else if (FleetMove.isMoving() && evt.getTag() == Event.EventTag.MOVE && player == getCurrentPlayer()) {
 			FleetMove.interrupt();
 		}
 		if (evt.getTag() == Event.EventTag.PLANET_SELECTED) {
 			planetSelected((Position) evt.getObjectValue(), player);
 		} 
 		if (evt.getTag() == Event.EventTag.DESELECT) {
 			resetVariables(player);
 		} 
 		/*
 		 * Model or Controller triggered Events
 		 */
 		if (evt.getTag() == Event.EventTag.MOVES_COMPLETE) {
 			movesComplete();
 		}
 		if (evt.getTag() == Event.EventTag.INCOME_CHANGED) {
 			if (evt.getObjectValue() != null) {
 				incomeChanged((Player) evt.getObjectValue());
 			} else {
 				for (Player activePlayer : activePlayers) {
 					incomeChanged(activePlayer);
 				}
 			}
 		}
 		if (evt.getTag() == Event.EventTag.HOME_LOST) {
 			Player loser = (Player) evt.getObjectValue();
 			activePlayers.remove(loser);
 			Event homeLostEvent = new Event(Event.EventTag.HOME_LOST, getCurrentPlayer());
 			homeLostEvent.setPlayer(loser);
 			EventBus.SERVER.publish(homeLostEvent);
 			if (activePlayers.size() == 1) {
 				Event event = new Event(Event.EventTag.GAME_OVER, null);
 				event.setPlayer(getCurrentPlayer());
 				EventBus.SERVER.publish(event);
 			}
 		}
 	}
 	
 	private void updateSelections() {
 		for (Player player : activePlayers) {
 			if (selections.get(player).selectedPosition != null) {
 				Territory selectedTerritory = world.getTerritory(selections.get(player).selectedPosition);
 				if (selectedTerritory.hasPlanet()) {
 					Event evt;
 					if (selectedTerritory.hasColony() && player == selectedTerritory.getColony().getOwner()) {
 						evt = new Event(Event.EventTag.SELECTION, selectedTerritory.getColony());
 					} else if (selectedTerritory.hasColony()){
 						evt = new Event(Event.EventTag.SELECTION, null);
 					} else {
 						evt = new Event(Event.EventTag.SELECTION, selectedTerritory);
 					}
 					evt.setPlayer(player);
 					EventBus.SERVER.publish(evt);
 				}
 			}
 		}
 	}
 	
 	private void queueBuilding(String objectValue, Player player) {
 		Colony c = world.getTerritory(selections.get(getCurrentPlayer()).selectedPosition).getColony();
 		BuildAble building = null;
 		if (objectValue.equals("MINE")){
 			building = c.getMine().isMaxRank() ? null: c.getMine();
 		} else if(objectValue.equals("RADAR")){
 			building = c.getRadar().isMaxRank() ? null: c.getRadar();
 		} else if(objectValue.equals("HANGAR")){
 			building = c.getHangar().isMaxRank() ? null: c.getHangar();
 		} else if(objectValue.equals("TURRET")){
 			building = c.getTurret().isMaxRank() ? null: c.getTurret();
 		}
 		if (building != null){
 			queueBuildAble(building, player);
 		}
 	}
 
 	private void incomeChanged(Player affectedPlayer) {
 		int metalIncome = 10;
 		int gasIncome = 0;
 		for (Position pos : world.getContentPositions()) {
 			Territory terr = world.getTerritory(pos);
 			if (terr.hasColony()) {
 				if (terr.getColony().getOwner() == affectedPlayer) {
 					if (terr.getPlanet().getType() == Resource.METAL) {
 						metalIncome += terr.getColony().getIncome();
 					} else if (terr.getPlanet().getType() == Resource.GAS) {
 						gasIncome += terr.getColony().getIncome();
 					}
 				}
 			}
 		}
 		world.setIncome(affectedPlayer, Resource.METAL, metalIncome);
 		world.setIncome(affectedPlayer, Resource.GAS, gasIncome);
 	}
 	
 	private void planetSelected(Position pos, Player player) {
 		resetVariables(player);
 		selections.get(player).selectedPosition = pos;
 		Territory selectedTerritory = world.getTerritory(pos);
 		if (selectedTerritory.hasPlanet()) {
 			Event evt;
 			if (selectedTerritory.hasColony() && player == selectedTerritory.getColony().getOwner()) {
 				evt = new Event(Event.EventTag.SELECTION, selectedTerritory.getColony());
 			} else if (selectedTerritory.hasColony()){
 				evt = new Event(Event.EventTag.SELECTION, null);
 			} else {
 				evt = new Event(Event.EventTag.SELECTION, selectedTerritory);
 			}
 			evt.setPlayer(player);
 			EventBus.SERVER.publish(evt);
 		}
 	}
 	
 	private void movesComplete() {
 		for (Position pos : world.getContentPositions()) {
 			Territory terr = world.getTerritory(pos);
 			if (terr.hasConflict()) {
 				BattleStats battleStats = Battle.doBattle(terr);
 				for (Fleet f : battleStats.getDestroyedFleets()) {
 					for(Player player : battleStats.getParticipants()) {
 						selections.get(player).fleetPaths.remove(f);
 					}
 				}
 				if (battleStats.isColonyDestroyed()) {
 					world.removeBuildQueue(battleStats.getLoser(), pos);
 				}
 			}
 		}
 		Event event = new Event(Event.EventTag.UPDATE_SPRITEDATA, null);
 		EventBus.SERVER.publish(event);
 	}
 	
 	public Position[][] getPaths(Player player) {
 		Position[][] tmp = new Position[selections.get(player).fleetPaths.size()][];
 		int i = 0;
 		for (Fleet fleet : selections.get(player).fleetPaths.keySet()) {
 			if (fleet.getOwner() == player) {
 				tmp[i] = selections.get(player).fleetPaths.get(fleet).getPositions();
 				i++;
 			}
 		}
 		Position[][] paths = new Position[i][];
 		for (int j = 0; j < paths.length; j++) {
 			paths[j] = tmp[j];
 		}
 		return paths;
 	}
 
 	private void performMoves() {
 		selections.get(getCurrentPlayer()).fleetSelectionIndex = 0;
 		FleetMove.move(world, selections.get(getCurrentPlayer()).fleetPaths, GameManager.INSTANCE.getCurrentPlayer());
 	}
 
 	private void setPath(Position target) {
 		for (Fleet fleet : selections.get(getCurrentPlayer()).selectedFleets) {
 			if (GameManager.INSTANCE.getCurrentPlayer() == fleet.getOwner()) {
 				selections.get(getCurrentPlayer()).fleetPaths.get(fleet).setTarget(target);
 			}
 		}
 		Event evt = new Event(Event.EventTag.UPDATE_SPRITEDATA, null);
 		EventBus.SERVER.publish(evt);
 	}
 	
 	private void colonizePlanet(Player player) {
 		Territory ter = world.getTerritory(selections.get(player).selectedPosition);
 		if (ter.hasFleet() && ter.hasPlanet() && !ter.hasColony()) {
 			for (Fleet fleet : ter.getFleets()) {
 				if (fleet.hasColonizer() && fleet.getOwner() == player) {
 					fleet.useColonizer();
 					ter.getPlanet().buildColony(player);
 					incomeChanged(player);
 					if(fleet.fleetSize() == 0){
 						ter.removeFleet(fleet);
 					}
 					world.updatePlayerStats(player);
 					Event evt = new Event(Event.EventTag.UPDATE_SPRITEDATA, null);
 					EventBus.SERVER.publish(evt);
 					resetVariables(player);
 					break; // Stop looping through fleets.
 				}
 			}
 		}
 	}
 	
 	private void setColonizerSelected(Position pos, Player player) {
 		resetVariables(player);
 		selections.get(player).fleetSelectionIndex = 0;
 		selections.get(player).lastFleetSelectPos = null;
 		if (world.getTerritory(pos).hasColonizer()) {
 			for (Fleet fleet : world.getTerritory(pos).getFleets()) {
 				if (fleet.hasColonizer()) {
 					selections.get(player).selectedFleets.add(fleet);
 					selections.get(player).fleetPaths.put(fleet, new Path(pos));
 					break;
 				}
 			}
 			Event evt = new Event(Event.EventTag.SELECTION, new Fleet(selections.get(player).selectedFleets));
 			evt.setPlayer(player);
 			EventBus.SERVER.publish(evt);
 		}
 		
 	}
 	
 	private void addFleetSelection(Position pos, Player player) {
 		
 		if (selections.get(player).lastFleetSelectPos == null || !selections.get(player).lastFleetSelectPos.equals(pos)) {
 			selections.get(player).lastFleetSelectPos = pos;
 			selections.get(player).fleetSelectionIndex = 0;
 		}
 		if (world.getTerritory(pos).hasFleet() && (world.getTerritory(pos).controlledBy() == player)) {
 			/*
 			 * Check that there are other ships than colonizers
 			 */
 			if (world.getTerritory(pos).getFleets().size() != world.getTerritory(pos).shipCount(ShipType.COLONIZER)) {
 				Fleet fleet;
 				do {
 					fleet = world.getTerritory(pos).getFleet(selections.get(player).fleetSelectionIndex);
 					selections.get(player).fleetSelectionIndex = (selections.get(player).fleetSelectionIndex + 1) % world.getTerritory(pos).getFleets().size();
 				} while(fleet.hasColonizer());
 				addSelectedFleet(fleet, pos);
 				Event evt = new Event(Event.EventTag.SELECTION, new Fleet(selections.get(player).selectedFleets));
 				evt.setPlayer(player);
 				EventBus.SERVER.publish(evt);
 			}
 		}
 	}
 
 	private void newFleetSelection(Position pos, Player player) {
 		resetVariables(player); // Reset all selections as we make a new selection
 		if (selections.get(player).lastFleetSelectPos == null || !selections.get(player).lastFleetSelectPos.equals(pos)) {
 			selections.get(player).lastFleetSelectPos = pos;
 			selections.get(player).fleetSelectionIndex = 0;
 		}
 		if (world.getTerritory(pos).hasFleet() && (world.getTerritory(pos).controlledBy() == player)) {
 			if (world.getTerritory(pos).getFleets().size() != world.getTerritory(pos).shipCount(ShipType.COLONIZER)) {
 				Fleet fleet;
 				do {
 					fleet = world.getTerritory(pos).getFleet(selections.get(player).fleetSelectionIndex);
 					selections.get(player).fleetSelectionIndex = (selections.get(player).fleetSelectionIndex + 1) % world.getTerritory(pos).getFleets().size();
 				} while(fleet.hasColonizer());
 				addSelectedFleet(fleet, pos);
 			}
 		}
 		if (!selections.get(player).selectedFleets.isEmpty()) {
 			Event evt = new Event(Event.EventTag.SELECTION, new Fleet(selections.get(player).selectedFleets));
 			evt.setPlayer(player);
 			EventBus.SERVER.publish(evt);
 		}
 	}
 
 	private void newFleetsSelection(Event evt, Player player) {
 		resetVariables(player); // Reset all selections as we make a new selection
 		if (evt.getObjectValue() instanceof List) {
 			List<?> positions = (List<?>) evt.getObjectValue();
 			for (int i = 0; i < positions.size(); i++) {
 				if (positions.get(i) instanceof Position) {
 					Position pos = (Position) positions.get(i);
 					if (world.getTerritory(pos).hasFleet() && (world.getTerritory(pos).controlledBy() == player)) {
 						for (Fleet fleet : world.getTerritory(pos).getFleets()) {
 							if (!fleet.hasColonizer()) {
 								addSelectedFleet(fleet, pos);
 							}
 						}
 					}
 				}
 			}
 		}
 		if (!selections.get(player).selectedFleets.isEmpty()) {
 			Event event = new Event(Event.EventTag.SELECTION, new Fleet(selections.get(player).selectedFleets));
 			event.setPlayer(player);
 			EventBus.SERVER.publish(event);
 		}
 	}
 	private void addSelectedFleet(Fleet fleet, Position pos){
 		selections.get(fleet.getOwner()).selectedFleets.add(fleet);
 		if (!selections.get(fleet.getOwner()).fleetPaths.containsKey(fleet)) {
 			selections.get(fleet.getOwner()).fleetPaths.put(fleet, new Path(pos));
 		}
 	}
 
 	private void queueBuildAble(BuildAble buildAble, Player player) {
 		for (Position pos : world.getContentPositions()) {
 			if (world.getTerritory(pos).hasColony()) {
 				if (world.getTerritory(pos).getColony() == world.getTerritory(selections.get(getCurrentPlayer()).selectedPosition).getColony()) {
 					if (buildAble instanceof ShipType && !world.getTerritory(pos).getColony().canBuild((ShipType) buildAble)) {
 						// If the colony can not build this shipType
 						return;
 					}
 					if (buildAble instanceof Ranked && world.getBuildQueue(player).containsKey(pos) && world.getBuildQueue(player).get(pos).contains(buildAble)) {
 						// BuildAble Ranked of this type is already in queue
 						return;
 					}
 					if (world.canAfford(player, buildAble)) {
 						if(world.purchase(player, buildAble)){
 							world.addToBuildQueue(buildAble, player, pos);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private void resetVariables(Player player) {
 		if(selections.get(player) != null){
 			selections.get(player).selectedFleets.clear();
 			selections.get(player).selectedPosition = null;
 			Event evt = new Event(Event.EventTag.SELECTION, null);
 			evt.setPlayer(player);
 			EventBus.SERVER.publish(evt);
 		}
 	}
}
