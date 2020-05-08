 package ultraextreme.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ultraextreme.model.util.Direction;
 import ultraextreme.model.util.PlayerID;
 import ultraextreme.model.util.Position;
 
 /**
  * An inventory containing weapons and bombs.
  * 
  * @author Bjorn Persson Mattsson
 * @author Daniel Jonsson
  * 
  */
 public class ItemBar {
 
 	/**
 	 * Maximum number of items that the item bar can hold.
 	 */
 	private int maximumNumberOfItems;
 
 	/**
 	 * Where the next item will be put into the item bar.
 	 */
 	private int cursorPosition;
 
 	/**
 	 * A list of all items in the item bar.
 	 */
 	private List<AbstractWeapon> items;
 
 	/**
 	 * The player's ID that the item bar belongs to.
 	 */
 	private PlayerID playerId;
 
 	// private Bomb bomb;
 	
 	//private BulletManager bulletManager;
 
 	/**
 	 * Create an item bar.
 	 * 
 	 * @param playerId
 	 *            The player's ID it belongs to.
 	 * @param bulletManager
 	 *            Reference to the bullet manager.
 	 */
 	public ItemBar(PlayerID playerId, BulletManager bulletManager) {
 		this(playerId, bulletManager, 1);
 	}
 
 	/**
 	 * Create an item bar.
 	 * 
 	 * @param playerId
 	 *            The player's ID it belongs to.
 	 * @param bulletManager
 	 *            Reference to the bullet manager.
 	 * @param maximumNumberOfItems
 	 *            Maximum number of items that fit in the item bar.
 	 */
 	public ItemBar(PlayerID playerId, BulletManager bulletManager,
 			int maximumNumberOfItems) {
 		this.playerId = playerId;
 		//this.bulletManager = bulletManager;
 		this.items = new ArrayList<AbstractWeapon>();
 		this.maximumNumberOfItems = maximumNumberOfItems;
 		this.cursorPosition = 0;
 	}
 
 	/**
 	 * Add an item to the item bar.
 	 * 
 	 * @param item
 	 *            The item that should be added.
 	 */
 	public void addItem(AbstractWeapon item) {
 		if (items.size() < maximumNumberOfItems) {
 			items.add(item);
 		} else {
 			items.set(cursorPosition, item);
 		}
 		cursorPosition++;
 		cursorPosition = (maximumNumberOfItems - 1) % cursorPosition;
 	}
 
 	/**
 	 * Fires all the weapons in the ItemBar
 	 * 
 	 * @param firePositon
 	 *            From where the guns should be fired.
 	 */
 	public void fireWeapons(Position firePosition) {
 		for (AbstractWeapon weapon : items) {
 			weapon.fireShot(firePosition, playerId, Direction.UP);
 		}
 	}
 }
