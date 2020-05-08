 package com.github.joakimpersson.tda367.model.player;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.github.joakimpersson.tda367.model.constants.Attribute;
 import com.github.joakimpersson.tda367.model.constants.Direction;
 import com.github.joakimpersson.tda367.model.constants.GameModeType;
 import com.github.joakimpersson.tda367.model.constants.Parameters;
 import com.github.joakimpersson.tda367.model.constants.PointGiver;
 import com.github.joakimpersson.tda367.model.positions.FPosition;
 import com.github.joakimpersson.tda367.model.positions.Position;
 
 /**
  * This class defines a Player in the bomberman-like game.
  * 
  * @author Adrian Bjugrd
  * @modified Viktor Anderling, Joakim Persson
  */
 public class Player {
 
 	private String name;
 	private Position initialPosition;
 	private Position tilePos;
 	private FPosition gamePos;
 	private PlayerAttributes playerAttribute;
 	private PlayerPoints playerPoints;
 	private Direction facingDirection;
 	private int bombsPlaced;
 	private int health;
 	private int playerIndex;
 	private boolean justHit;
 
 	private int rounsdWon;
 	private int matchesWon;
 	private int areaBombsPlaced;
 
 	/**
 	 * Creates a player with a pre-defined position and name.
 	 * 
 	 * @param name
 	 *            The name of the player.
 	 * @param pos
 	 *            The starting position of a player.
 	 */
 	public Player(int playerIndex, String name, Position pos) {
 		this.playerIndex = playerIndex;
 		this.name = name;
 		this.initialPosition = pos;
 		this.rounsdWon = 0;
 		this.matchesWon = 0;
 		initPlayer();
 	}
 	
 	/**
 	 * Creates a clone of a player.
 	 * 
 	 * @param player
 	 * 				The player that is to be cloned.
 	 */
 	public Player(Player player) {
 		this.name = player.name;
 		this.initialPosition = player.initialPosition;
 		this.tilePos = player.tilePos;
 		this.gamePos = player.gamePos;
 		this.playerAttribute = new PlayerAttributes(player.playerAttribute);
 		this.playerPoints = new PlayerPoints(player.playerPoints);
 		this.facingDirection = player.facingDirection;
 		this.bombsPlaced = player.bombsPlaced;
 		this.health = player.health;
 		this.playerIndex = player.playerIndex;
 		this.justHit = player.justHit;
 		this.rounsdWon = player.rounsdWon;
 		this.matchesWon = player.matchesWon;
 		this.areaBombsPlaced = player.areaBombsPlaced;
 	}
 
 	/**
 	 * Method for initializing a player.
 	 */
 	private void initPlayer() {
 		this.playerAttribute = new PlayerAttributes();
 		this.playerPoints = new PlayerPoints();
 		roundReset();
 	}
 
 	/**
 	 * Method used to reset a players state for a new round.
 	 */
 	private void roundReset() {
 		this.playerAttribute.resetAttr(GameModeType.Round);
 		this.health = getAttribute(Attribute.Health);
 		this.tilePos = initialPosition;
 		this.facingDirection = Direction.SOUTH;
 		this.gamePos = new FPosition(initialPosition.getX() + 0.5F,
 				initialPosition.getY() + 0.5F);
 		this.bombsPlaced = 0;
 		this.areaBombsPlaced = 0;
 		this.justHit = false;
 	}
 
 	/**
 	 * Method used to reset a players state for a new match (3 rounds).
 	 */
 	private void matchReset() {
 		this.playerAttribute.resetAttr(GameModeType.Match);
 		roundReset();
 	}
 
 	/**
 	 * Method used to reset a players state for a new match or round.
 	 * 
 	 * @param type
 	 *            The state type that will be reset.
 	 */
 	public void reset(GameModeType type) {
 		switch (type) {
 		case Match:
 			matchReset();
 			break;
 		case Round:
 			roundReset();
 			break;
 		default:
 			break;
 		}
 	}
 
 	/**
 	 * Moves a player in specified direction.
 	 * 
 	 * @param direction
 	 *            The direction in which the player will move.
 	 * @param stepSize
 	 *            How big steps the player will move.
 	 */
 	public void move(Direction direction, double stepSize) {
 		double newFX = gamePos.getX() + stepSize * direction.getX();
 		double newFY = gamePos.getY() + stepSize * direction.getY();
 		gamePos = new FPosition((float) newFX, (float) newFY);
 		tilePos = new Position((int) newFX, (int) newFY);
 		this.facingDirection = direction;
 	}
 
 	/**
 	 * Get how big steps the player moves.
 	 * 
 	 * @return How big steps the player moves.
 	 */
 	public double getSpeededStepSize() {
 		return Parameters.INSTANCE.getBaseStepSize()
 				+ (0.015 * getAttribute(Attribute.Speed));
 	}
 
 	/**
 	 * Checks if the player can place a bomb or not.
 	 * 
 	 * @return If a player can place a bomb or not.
 	 */
 	public boolean canPlaceBomb() {
 		if (getAttribute(Attribute.BombStack) > this.bombsPlaced) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * @return If a player can place an area bomb or not.
 	 */
 	public boolean canPlaceAreaBomb() {
 		if (getAttribute(Attribute.AreaBombs) > this.areaBombsPlaced) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Increases the current number of bombs placed by the player.
 	 */
 	public void increaseBombsPlaced() {
 		this.bombsPlaced++;
 	}
 
 	/**
 	 * Decreases the current number of bombs placed by the player.
 	 */
 	public void decreaseBombsPlaced() {
 		this.bombsPlaced--;
 	}
 
 	/**
 	 * Upgrade either a round or match attribute with one level.
 	 * 
 	 * @param attr
 	 *            The attribute to be upgraded
 	 * @param type
 	 *            The type of the upgrade
 	 */
 	public void upgradeAttr(Attribute attr, GameModeType type) {
 		this.playerAttribute.upgradeAttr(attr, type);
 		if (type.equals(GameModeType.Match)) {
 			this.reloadAttributes();
 		}
 	}
 
 	/**
 	 * Method for getting a players attributes.
 	 * 
 	 * @return A players list of attributes.
 	 */
 	public List<Attribute> getPermanentAttributes() {
 		return new ArrayList<Attribute>(playerAttribute.getAttributes());
 	}
 
 	/**
 	 * Method for getting the value of a specific requested attribute.
 	 * 
 	 * @param attr
 	 *            The type of attribute requested.
 	 * @return The value of the attribute requested.
 	 */
 	public int getAttribute(Attribute attr) {
 		return playerAttribute.getAttrValue(attr);
 	}
 
 	/**
 	 * Method called when player is hurt by fire.
 	 */
 	public void playerHit() {
 		if (!justHit) {
 			Timer justHitTimer = new Timer();
 			int duration = Parameters.INSTANCE.getFireDuration();
 			justHitTimer.schedule(new HitTask(), duration);
 		}
 	}
 
 	/**
 	 * Check whether player is alive.
 	 * 
 	 * @return Player's vitals.
 	 */
 	public boolean isAlive() {
 		return health > 0;
 	}
 
 	/**
 	 * Method to get a players score.
 	 * 
 	 * @return The players score.
 	 */
 	public int getScore() {
 		return playerPoints.getScore();
 	}
 
 	/**
 	 * Method to get a players available credits.
 	 * 
 	 * @return The players available credits.
 	 */
 	public int getCredits() {
 		return playerPoints.getCredits();
 	}
 
 	/**
 	 * Method which uses a players credits.
 	 * 
 	 * @param cost
 	 *            Amount of credits used.
 	 */
 	public void useCredits(int cost) {
 		playerPoints.useCredits(cost);
 	}
 
 	/**
 	 * This will update a players points with a list of PointGiver's.
 	 * 
 	 * @param pointGivers
 	 *            List containing PointGiver's.
 	 */
 	public void updatePlayerPoints(List<PointGiver> pointGivers) {
 		playerPoints.update(pointGivers);
 	}
 
 	public void updatePlayerPoints(PointGiver pointGiver) {
 		playerPoints.update(pointGiver);
 	}
 
 	/**
 	 * Get the players name.
 	 * 
 	 * @return Name of the player.
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Get the players current health.
 	 * 
 	 * @return Current health of the player.
 	 */
 	public int getHealth() {
 		return health;
 	}
 
 	/**
 	 * Get the current Position of the player in the tile grid.
 	 * 
 	 * @return Which position the player has in the tile grid.
 	 */
 	public Position getTilePosition() {
 		return tilePos;
 	}
 
 	/**
 	 * Get the current FPosition of the player in the game grid.
 	 * 
 	 * @return Where the player is on the game grid.
 	 */
 	public FPosition getGamePosition() {
 		return gamePos;
 	}
 
 	/**
 	 * Get the current facing direction of the player.
 	 * 
 	 * @return Which direction the player is facing.
 	 */
 	public Direction getDirection() {
 		return facingDirection;
 	}
 
 	/**
 	 * Get the players current status whether is has been hit by an exploded
 	 * bomb or not
 	 * 
 	 * @return Whether the players has recently been hit by a bomb
 	 */
 	public boolean isImmortal() {
 		return justHit;
 	}
 
 	/**
 	 * Returns the amount of certain destroyed PointGiver type by this player.
 	 * 
 	 * @param type
 	 *            type of PointGiver tile
 	 * @return The number of destroyed tile type in PointGiver
 	 */
 	public int getEarnedPointGiver(PointGiver type) {
 		return playerPoints.getEarnedPointGiver(type);
 	}
 
 	/**
 	 * Get information of what image that will be showed.
 	 * 
 	 * @return A String with information of what image that should be showed.
 	 */
 	public String getImage() {
 		return "player/" + playerIndex + "/still-" + facingDirection;
 	}
 
 	/**
 	 * Get the players PlayerPoints object.
 	 * 
 	 * @return The players PlayerPoints object.
 	 */
 	public PlayerPoints getPoints() {
 		return playerPoints;
 	}
 
 	/**
 	 * Get the players index.
 	 * 
 	 * @return What index the player got.
 	 */
 	public int getIndex() {
 		return playerIndex;
 	}
 
 	/**
 	 * Resets the players health to hundred percent.
 	 */
 	private void reloadAttributes() {
 		health = getAttribute(Attribute.Health);
 	}
 
 	/**
 	 * Moves the player in the current direction.
 	 * 
 	 * @param direction
 	 *            What direction to move the player.
 	 */
 	public void adjustPosition(Direction direction) {
 		if (direction.equals(Direction.NORTH)
 				|| direction.equals(Direction.SOUTH)) {
 			gamePos = new FPosition(tilePos.getX() + 0.5F, gamePos.getY());
 		} else {
 			gamePos = new FPosition(gamePos.getX(), tilePos.getY() + 0.5F);
 		}
 	}
 
 	/**
 	 * Increase the number of rounds a player has win in the current match
 	 */
 	public void roundWon() {
 		rounsdWon += 1;
 	}
 
 	/**
 	 * Get how many rounds a player has win during a match
 	 * 
 	 * @return Number of rounds a player has win
 	 */
 	public int getRoundsWon() {
 		return rounsdWon;
 	}
 
 	/**
 	 * Reset how many rounds a player has win
 	 */
 	public void resetRoundsWon() {
 		rounsdWon = 0;
 	}
 
 	/**
 	 * Increase number of matches a player has win
 	 */
 	public void matchWon() {
 		matchesWon += 1;
 	}
 
 	/**
 	 * Get how many matches a player has win during a game
 	 * 
 	 * @return How many matches a player has win
 	 */
 	public int getMatchesWon() {
 		return matchesWon;
 	}
 	
 	public void increaseAreaBombsPlaced() {
 		this.areaBombsPlaced++;
 	}
 
 	public int getAreaBombsAvailable() {
 		return getAttribute(Attribute.AreaBombs) - this.areaBombsPlaced;
 	}
 
 	@Override
 	public int hashCode() {
 		int sum = 0;
 		sum += playerIndex * 5;
 		sum += name.hashCode() * 7;
 		sum += playerAttribute.hashCode() * 13;
 		sum += tilePos.hashCode() * 17;
 		sum += playerPoints.hashCode() * 19;
 		return sum;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null || getClass() != obj.getClass()) {
 			return false;
 		}
 		Player other = (Player) obj;
 		return this.name.equals(other.name)
 				&& this.playerAttribute.equals(other.playerAttribute)
 				&& this.playerPoints.equals(other.playerPoints)
 				&& this.tilePos.equals(other.tilePos)
 				&& this.playerIndex == other.playerIndex;
 	}
 
 	/**
 	 * Returns the players name, position in the tilegrid and health.
 	 * 
 	 * @return A String includuing the players name, position in the tilegrid
 	 *         and health.
 	 */
 	@Override
 	public String toString() {
 		return "P[" + this.name + ", " + this.tilePos + ", " + this.health
 				+ " HP]";
 	}
 
 	/**
 	 * TimerTask initiated getFireDuration()-milliseconds after player has been
 	 * hit by fire.
 	 */
 	private class HitTask extends TimerTask {
 		/**
 		 * Initiated when player is hit.
 		 */
 		public HitTask() {
 			health--;
 			justHit = true;
 		}
 
 		@Override
 		public void run() {
 			justHit = false;
 		}
 	}
 
 }
