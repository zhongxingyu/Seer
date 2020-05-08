 package com.github.joakimpersson.tda367.model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.github.joakimpersson.tda367.audio.SoundHandler;
 import com.github.joakimpersson.tda367.audio.SoundType;
 import com.github.joakimpersson.tda367.model.constants.Attribute;
 import com.github.joakimpersson.tda367.model.constants.Direction;
 import com.github.joakimpersson.tda367.model.constants.Parameters;
 import com.github.joakimpersson.tda367.model.constants.PlayerAction;
 import com.github.joakimpersson.tda367.model.constants.PointGiver;
 import com.github.joakimpersson.tda367.model.constants.ResetType;
 import com.github.joakimpersson.tda367.model.map.GameMap;
 import com.github.joakimpersson.tda367.model.map.IGameMap;
 import com.github.joakimpersson.tda367.model.player.Player;
 import com.github.joakimpersson.tda367.model.player.PlayerAttributes.UpgradeType;
 import com.github.joakimpersson.tda367.model.tiles.Tile;
 import com.github.joakimpersson.tda367.model.tiles.WalkableTile;
 import com.github.joakimpersson.tda367.model.tiles.bombs.AreaBomb;
 import com.github.joakimpersson.tda367.model.tiles.bombs.Bomb;
 import com.github.joakimpersson.tda367.model.tiles.bombs.NormalBomb;
 import com.github.joakimpersson.tda367.model.tiles.nonwalkable.Box;
 import com.github.joakimpersson.tda367.model.tiles.nonwalkable.Pillar;
 import com.github.joakimpersson.tda367.model.tiles.walkable.Fire;
 import com.github.joakimpersson.tda367.model.utils.FPosition;
 import com.github.joakimpersson.tda367.model.utils.Position;
 
 /**
  * 
  * @author Viktor Anderling
  * @modified Joakim Persson, Adrian Bjugrd
  * 
  */
 public class BombermanModel implements IBombermanModel {
 
 	// TODO This is not supposed to be here later.
 	SoundHandler sh = SoundHandler.getInstance();
 
 	/**
 	 * Timer-task that is used for scheduling what happens when the fire is
 	 * removed.
 	 */
 	private class FireTimerTask extends TimerTask {
 		@Override
 		public void run() {
 			removeFirstFire();
 		}
 	}
 
 	/**
 	 * Timer-task that is used for scheduling what happens when a bomb
 	 * detonates.
 	 */
 	private class BombTask extends TimerTask {
 		private Bomb bomb;
 
 		public BombTask(Bomb b) {
 			this.bomb = b;
 		}
 
 		@Override
 		public void run() {
 			handleFire(bomb.getPlayer(), bomb.explode(map.getMap()));
 
 			// TODO This is not supposed to be here later.
 			sh.playSound(SoundType.BombExplodeSFX);
 		}
 	}
 
 	private List<Player> players;
 	private IGameMap map;
 	private static BombermanModel instance = null;
 	private LinkedList<Map<Position, Tile>> waitingFirePositions;
 
 	/*
 	 * This list contains, in the order of each explosion, what is supposed to
 	 * be at the positions when the fire of that explosion is to be removed.
 	 */
 
 	private BombermanModel() {
 		this.players = new ArrayList<Player>();
 		// TODO natan perhaps instantiating the players somewhere else
 		players.add(new Player("Joakim", new Position(1, 1)));
 		players.add(new Player("kalle", new Position(13, 11)));
 		this.map = new GameMap();
 		this.waitingFirePositions = new LinkedList<Map<Position, Tile>>();
 
 		// TODO this is not supposed to be here later.
 		sh.playSound(SoundType.TitleBGM);
 
 	}
 
 	/**
 	 * @return The instance of this bombermanModel.
 	 */
 	public static BombermanModel getInstance() {
 		if (instance == null) {
 			instance = new BombermanModel();
 		}
 		return instance;
 	}
 
 	@Override
 	public void upgradePlayer(Player player, Attribute attr) {
 		if (player.getCredits() >= attr.getCost()) {
 			player.upgradeAttr(attr, UpgradeType.Match);
			// TODO perhaps map via the player object
 			player.useCredits(attr.getCost());
 		}
 		// TODO perhaps do something or notify someone when it fails!
 
 	}
 
 	@Override
 	public void updateGame(Player player, PlayerAction action) {
 		Direction direction = null;
 		switch (action) {
 		case MoveUp:
 			direction = Direction.Up;
 			break;
 		case MoveDown:
 			direction = Direction.Down;
 			break;
 		case MoveLeft:
 			direction = Direction.Left;
 			break;
 		case MoveRight:
 			direction = Direction.Right;
 			break;
 		case PlaceBomb:
 			this.placeBomb(player);
 			break;
 		default:
 			break;
 		}
 		if (direction != null) {
 			this.move(player, direction);
 		}
 	}
 
 	/**
 	 * This method moves the player in a double grid. If the tile at the chosen
 	 * direction is walkable or the player will end up at least at the length of
 	 * 0.2 from it, the player will move in that direction and action is taken
 	 * if a new tile is entered. Else, nothing happens.
 	 * 
 	 * @param player
 	 *            The player to move.
 	 * @param direction
 	 *            The direction for the player to move
 	 */
 	private void move(Player player, Direction direction) {
 		Position prevPos = player.getTilePosition();
 		Tile tileAtDirection = map.getTile(new Position(prevPos.getX()
 				+ direction.getX(), prevPos.getY() + direction.getY()));
 		// The tile that the player may walk into.
 
 		if (tileAtDirection.isWalkable()) {
 			player.move(direction);
 		} else {
 			FPosition decimalPos = player.getGamePosition();
 			decimalPos = new FPosition(decimalPos.getX()
 					- (int) decimalPos.getX(), decimalPos.getY()
 					- (int) decimalPos.getY());
 
 			// Removes the integer part of the players position, leaving only
 			// the decimal part.
 
 			double stepSize = Parameters.INSTANCE.getPlayerStepSize();
 			double xStep = stepSize * direction.getX();
 			double yStep = stepSize * direction.getY();
 			decimalPos = new FPosition((float) (decimalPos.getX() + xStep),
 					(float) (decimalPos.getY() + yStep));
 			// Adding the steps to the player's new position.
 
 			// Can't move closer than 0.2 to a non-walkable tile.
 			float pD = 0.2F;
 			if ((direction == Direction.Up && decimalPos.getY() >= pD)
 					|| (direction == Direction.Down && decimalPos.getY() <= 1 - pD)
 					|| (direction == Direction.Left && decimalPos.getX() >= pD)
 					|| (direction == Direction.Right && decimalPos.getX() <= 1 - pD)) {
 				player.move(direction);
 			}
 		}
 
 		Position newPos = player.getTilePosition();
 
 		// Handles what happens if the player walks into a new tile.
 		if (!prevPos.equals(newPos)) {
 			WalkableTile enteredTile = (WalkableTile) map.getTile(newPos);
 			map.setTile(enteredTile.playerEnter(player), newPos);
 		}
 	}
 
 	/**
 	 * This method tells a player to create a bomb, and starts a timer for that
 	 * bomb.
 	 * 
 	 * @param player
 	 *            The player that places the bomb.
 	 */
 	private void placeBomb(Player player) {
 		if (player.canPlaceBomb()
 				&& map.getTile(player.getTilePosition()) instanceof WalkableTile) {
 			Timer bombTimer = new Timer();
 			Bomb bomb = createBomb(player, bombTimer);// player.createBomb(bombTimer);
 			bombTimer.schedule(new BombTask(bomb),
 					Parameters.INSTANCE.getBombDetonationTime());
 
 			map.setTile(bomb, player.getTilePosition());
 		}
 	}
 
 	// TODO changed location of creating the bomb from player to bombermanModel
 	private Bomb createBomb(Player player, Timer bombTimer) {
 
 		switch (player.getAttribute(Attribute.BombType)) {
 		case 1:
 			return new AreaBomb(player, bombTimer);
 		default:
 			return new NormalBomb(player, bombTimer);
 		}
 	}
 
 	/**
 	 * This method takes care of what happens to the the positions that the fire
 	 * strikes, including giving points and place the fire.
 	 * 
 	 * @param positions
 	 *            The list that contains the positions of where the fire is to
 	 *            be placed.
 	 * @param bombOwner
 	 *            The player that placed the bomb.
 	 */
 	private void handleFire(Player bombOwner,
 			Map<Position, Direction> directedFirePositions) {
 		List<Position> list = new ArrayList<Position>(
 				directedFirePositions.keySet());
 		distributePoints(bombOwner, list);
 		setFire(directedFirePositions);
 	}
 
 	/**
 	 * This method is called internally by handle fire to distribute points
 	 * specifically.
 	 * 
 	 * @param positions
 	 *            The list that contains the positions of where the fire
 	 *            strikes.
 	 * @param bombOwner
 	 *            The player that placed the bomb.
 	 */
 	private void distributePoints(Player bombOwner, List<Position> positions) {
 		List<PointGiver> pg = new ArrayList<PointGiver>();
 		Tile tempTile;
 
 		for (Position pos : positions) {
 			// Converting positions into PointGivers
 			for (Player player : players) {
 				if (isPlayerAtPosition(player, pos)) {
 					pg.add(PointGiver.PlayerHit);
 					player.playerHit();
 					if (!player.isAlive()) {
 						pg.add(PointGiver.KillPlayer);
 					}
 				}
 			}
 			tempTile = map.getTile(pos);
 			if (tempTile instanceof Box) {
 				pg.add(PointGiver.Box);
 			} else if (tempTile instanceof Pillar) {
 				pg.add(PointGiver.Pillar);
 			}
 		}
 		bombOwner.updatePlayerPoints(pg);
 	}
 
 	/**
 	 * This method is called internally by handle fire to set fire at positions
 	 * for a fixed time specifically.
 	 * 
 	 * @param positions
 	 *            The list that contains the positions of where the fire is to
 	 *            be placed.
 	 */
 	private void setFire(Map<Position, Direction> directedFirePositions) {
 		Map<Position, Tile> waitingTiles = new HashMap<Position, Tile>();
 		Iterator<Position> iterator = directedFirePositions.keySet().iterator();
 		Position firePos;
 		while (iterator.hasNext()) {
 			firePos = (Position) iterator.next();
 			waitingTiles.put(firePos, map.getTile(firePos).onFire());
 			map.setTile(new Fire(directedFirePositions.get(firePos)), firePos);
 		}
 		/*
 		 * for (Position firePos : positions) { waitingTiles.put(firePos,
 		 * map.getTile(firePos).onFire()); map.setTile(new Fire(), firePos); }
 		 */
 		waitingFirePositions.addLast(waitingTiles);
 		Timer timer = new Timer();
 		timer.schedule(new FireTimerTask(),
 				Parameters.INSTANCE.getFireDuration());
 	}
 
 	/**
 	 * This method is called when the fire is dying, witch causes it to be
 	 * replaced by the appropriate new tile.
 	 */
 	private void removeFirstFire() {
		Map<Position, Tile> fireReplacers = waitingFirePositions.get(0);
		Iterator<Position> iterator = fireReplacers.keySet().iterator();
		Position temp;
		while (iterator.hasNext()) {
			temp = (Position) iterator.next();
			map.setTile(fireReplacers.get(temp), temp);
 		}
		waitingFirePositions.removeFirst();
 	}
 
 	@Override
 	public List<Player> getPlayers() {
 		return players;
 	}
 
 	@Override
 	public Tile[][] getMap() {
 		return map.getMap();
 	}
 
 	@Override
 	public boolean isRoundOver() {
 		for (Player p : players) {
 			if (!p.isAlive()) {
 				// TODO add implementation for reducing player life
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean isGameOver() {
 		// TODO add implementation/support
 		return false;
 	}
 
 	@Override
 	public boolean isMatchOver() {
 		// TODO add implementation/support
 		return true;
 	}
 
 	@Override
 	public void reset(ResetType type) {

 		switch (type) {
 		case Match:
 			matchReset();
 			break;
 		case Round:
 			roundReset();
 			break;
 		default:
 			// Should not happen and therefore we do nothing TODO perhaps
 			break;
 		}
 
 	}
 
 	private void resetPlayer(ResetType type) {
 		for (Player p : players) {
 			p.reset(type);
 		}
 	}
 
 	private void matchReset() {
 		resetPlayer(ResetType.Match);
		resetMap();
 	}
 
 	private void roundReset() {
 		resetPlayer(ResetType.Round);
		map.reset();
 	}
 
 	private void resetMap() {
 		map.reset();
 	}
 
 	private boolean isPlayerAtPosition(Player player, Position pos) {
 		return player.getTilePosition().equals(pos);
 	}
 }
