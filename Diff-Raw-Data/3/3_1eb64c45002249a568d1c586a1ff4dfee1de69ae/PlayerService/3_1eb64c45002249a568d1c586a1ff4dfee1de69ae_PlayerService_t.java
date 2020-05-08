 package org.opendarts.prototype.internal.service.player;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.opendarts.prototype.internal.model.dart.Dart;
 import org.opendarts.prototype.internal.model.player.ComputerPlayer;
 import org.opendarts.prototype.internal.model.player.Player;
 import org.opendarts.prototype.model.dart.DartSector;
 import org.opendarts.prototype.model.dart.DartZone;
 import org.opendarts.prototype.model.dart.IDart;
 import org.opendarts.prototype.model.player.IPlayer;
 import org.opendarts.prototype.service.player.IPlayerService;
 
 /**
  * The Class PlayerService.
  */
 public class PlayerService implements IPlayerService {
 
 	/** The players. */
 	private final Map<String, IPlayer> players;
 
 	/**
 	 * Instantiates a new player service.
 	 */
 	public PlayerService() {
 		super();
 		this.players = new HashMap<String, IPlayer>();
 		// XXX init
 		IPlayer player;
 		String name = System.getenv("USER");
 		if (name == null || "".equals(name)) {
 			name = System.getenv("USERNAME");
 		}
 		player = new Player(name);
 		this.players.put(player.getName(), player);
 		player = this.getComputerPlayer();
 		this.players.put(player.getName(), player);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.prototype.service.IPlayerService#getPlayer(java.lang.String)
 	 */
 	@Override
 	public IPlayer getPlayer(String playerName) {
 		IPlayer result = this.players.get(playerName);
 		if (result == null) {
 			result = new Player(playerName);
 			this.players.put(playerName, result);
 		}
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.prototype.service.IPlayerService#getComputerPlayer()
 	 */
 	@Override
 	public IPlayer getComputerPlayer() {
 		return new ComputerPlayer();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.prototype.service.player.IPlayerService#getAllPlayers()
 	 */
 	@Override
 	public List<IPlayer> getAllPlayers() {
 		ArrayList<IPlayer> list = new ArrayList<IPlayer>(this.players.values());
 		return list;
 	}
 
 	/**
 	 * Gets the computer dart.
 	 *
 	 * @param wished the wished
 	 * @return the computer dart
 	 */
 	@Override
 	public IDart getComputerDart(IDart wished) {
 		DartZone zone;
 		DartSector sector = null;
 
 		// Zone
 		Random random = new Random();
 		int rand = random.nextInt(1000);
 		switch (wished.getZone()) {
 			case DOUBLE:
 				if (rand < 10) { // unlucky
 					zone = DartZone.NONE;
 					sector = DartSector.UNLUCKY_DART;
 				} else if (rand < 320) { // Yeah
 					zone = DartZone.DOUBLE;
 				} else if (rand > 990) {// ouch
 					zone = DartZone.TRIPLE;
 				} else if (rand > 750) {// out of target
 					zone = DartZone.NONE;
 					sector = DartSector.OUT_OF_TARGET;
 				} else {
 					zone = DartZone.SINGLE;
 				}
 				break;
 			case TRIPLE:
 				if (rand < 10) { // unlucky
 					zone = DartZone.NONE;
 					sector = DartSector.UNLUCKY_DART;
 				} else if (rand < 270) { // Yeah
 					zone = DartZone.TRIPLE;
 				} else if (rand > 995) {// ouch
 					zone = DartZone.NONE;
 					sector = DartSector.OUT_OF_TARGET;
 				} else if (rand > 980) {// oops
 					zone = DartZone.DOUBLE;
 				} else {
 					zone = DartZone.SINGLE;
 				}
 				break;
 			default:
 				if (rand < 5) { // unlucky
 					zone = DartZone.NONE;
 					sector = DartSector.UNLUCKY_DART;
 				} else if (rand < 70) { // oops
 					zone = DartZone.TRIPLE;
 				} else if (rand > 990) {// ouch
 					zone = DartZone.NONE;
 					sector = DartSector.OUT_OF_TARGET;
 				} else if (rand > 950) {// oops
 					zone = DartZone.DOUBLE;
 				} else {
 					zone = DartZone.SINGLE;
 				}
 				break;
 		}
 
 		// Sector
 		if (sector == null) {
 			rand = random.nextInt(1000);
 			switch (wished.getSector()) {
 				case BULL:
 					if (rand < 20) {
 						sector = DartSector.UNLUCKY_DART;
						zone = DartZone.SINGLE;
 					} else if (rand < 120) {
 						sector = DartSector.BULL;
 						zone = DartZone.DOUBLE;
 					} else if (rand < 350) {
 						sector = DartSector.BULL;
 						zone = DartZone.SINGLE;
 					} else {
 						// random single
 						int i = random.nextInt(DartSector.values().length);
 						sector = DartSector.values()[i];
						zone = DartZone.SINGLE;
 					}
 					break;
 				case NONE:
 				case OUT_OF_TARGET:
 				case UNLUCKY_DART:
 					sector = DartSector.NONE;
 					break;
 				default:
 					// basic
 					if (rand < 180) {
 						sector = wished.getSector().getNext();
 					} else if (rand > 720) {
 						sector = wished.getSector().getPrevious();
 					} else {
 						sector = wished.getSector();
 					}
 					break;
 			}
 		}
 
 		return new Dart(sector, zone);
 	}
 
 }
