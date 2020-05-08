 package ultraextreme.model;
 
 import java.beans.PropertyChangeEvent;
 import java.util.ArrayList;
 import java.util.List;
 
 import ultraextreme.model.enemy.IEnemy;
 import ultraextreme.model.entity.PlayerShip;
 import ultraextreme.model.item.AbstractWeapon;
 import ultraextreme.model.item.BasicWeapon;
 import ultraextreme.model.item.BulletManager;
 import ultraextreme.model.item.ItemBar;
 import ultraextreme.model.item.SpinningSpreadWeapon;
 import ultraextreme.model.util.Constants;
 import ultraextreme.model.util.Dimension;
 import ultraextreme.model.util.PlayerID;
 import ultraextreme.model.util.Position;
 import ultraextreme.model.util.Rotation;
 
 /**
  * The player. The player has a ship and an item bar containing the ship's items
  * (such as weapons).
  * 
  * @author Bjorn Persson Mattsson
  * @author Daniel Jonsson
  * @author Johan Gronvall
  * @author Viktor Anderling
  */
 public class Player implements IPlayer {
 
 	List<IPlayerListener> listeners = new ArrayList<IPlayerListener>();
 	/**
 	 * Reference to the player's ship.
 	 */
 	final private PlayerShip ship;
 
 	/**
 	 * The player's ID. This is used in the view to differentiate player ships
 	 * from each other.
 	 */
 	final private PlayerID playerId;
 
 	/**
 	 * Item bar containing the items that the player's ship is currently
 	 * holding.
 	 */
 	final private ItemBar itemBar;
 
 	final private BulletManager bulletManager;
 
 	/**
 	 * The lives the player has left.
 	 */
 	private int lives;
 
 	private int score;
 
 	/**
 	 * The time the ship will be invincible after receiving damage.
 	 */
 	private static final double invTime = Constants.getShipInvincibilityTime();
 
 	/**
 	 * A count down for the ships invincibility.
 	 */
 	private double invCountDown;
 
 	/**
 	 * Create a new player.
 	 * 
 	 * @param playerId
 	 *            Assign an ID to the player.
 	 * @param bulletManager
 	 *            A manager that the player ship's guns should add the bullets
 	 *            to.
 	 */
 	public Player(final PlayerID playerId, final BulletManager bulletManager) {
 		this.bulletManager = bulletManager;
 		this.ship = new PlayerShip();
 		setShipToSpawn();
 		this.playerId = playerId;
 		this.itemBar = new ItemBar(playerId, bulletManager, new Rotation(
 				Math.PI), 5);
 		this.itemBar.addItem(new BasicWeapon(bulletManager));
 		this.itemBar.addItem(new SpinningSpreadWeapon(bulletManager));
 		lives = Constants.getInitShipLives();
 		this.score = 0;
 	}
 
 	/**
 	 * Read the player's input data and update the player's ship.
 	 * 
 	 * @param input
 	 *            Input data such as keystrokes.
 	 * @param delta
 	 *            Time since last update.
 	 */
 	public void update(final ModelInput input, final float timeElapsed) {
 		double newX = 0;
 		double newY = 0;
 		if (ship.justGotHit() && invCountDown <= 0) {
 			itemBar.looseItems();
 			if (itemBar.getItems().isEmpty()) {
 				lives -= 1;
 				this.notifyListeners();
 				if (lives == 0) {
 					ship.setDestroyed();
 				} else {
 					itemBar.addItem(new BasicWeapon(bulletManager));
 					setShipToSpawn();
 				}
 			}
 			if (!ship.isDestroyed()) {
 				invCountDown = invTime;
 			}
 		}
 		if (invCountDown > 0) {
 			invCountDown -= timeElapsed;
 		}
 
 		if (ship.canMoveX(input.dX)) {
 			newX = input.dX;
 		}
 		if (ship.canMoveY(input.dY)) {
 			newY = input.dY;
 		}
 		ship.move(newX, newY);
 		if (input.fireWeapons) {
 			itemBar.fireWeapons(ship.getPosition(), timeElapsed);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public PlayerShip getShip() {
 		return ship;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public PlayerID getPlayerId() {
 		return playerId;
 	}
 
 	// test
 	@Override
 	public ItemBar getItemBar() {
 		return itemBar;
 	}
 
 	/**
 	 * adds a weapon (or bomb) to this player's ItemBar
 	 * 
 	 * @param weapon
 	 */
 	public void giveWeapon(final AbstractWeapon weapon) {
 		itemBar.addItem(weapon);
 	}
 
 	/**
 	 * Sets the players ship to its spawn point.
 	 */
 	private void setShipToSpawn() {
 		final Dimension levelDimension = Constants.getLevelDimension();
 		ship.setPosition(new Position(levelDimension.getX() * 0.5
 				- ship.getWidth() / 2, levelDimension.getY() * 0.65));
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent event) {
 		if (event.getPropertyName().equals(Constants.EVENT_ENEMY_KILLED)) {
 			score += ((IEnemy) event.getNewValue()).getScoreValue();
 			notifyListeners();
 		}
 	}
 
 	private void notifyListeners() {
 		for (IPlayerListener l : listeners) {
 			l.playerUpdate(this);
 		}
 	}
 
 	@Override
 	public int getScore() {
 		return score;
 	}
 
 	@Override
 	public int getLives() {
 		return lives;
 	}
 
 	public void registerListener(IPlayerListener listener) {
 		listeners.add(listener);
 	}
 
 	public void unregisterListener(IPlayerListener listener) {
 		listeners.remove(listener);
 	}
 
	public void reset() {
		lives = Constants.getInitShipLives();
		score = 0;
	}
 }
