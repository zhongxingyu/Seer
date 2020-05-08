 package src;
 
 import java.util.Collection;
 
 import src.core.Game;
 import src.core.IPurchasable;
 import src.core.Player;
 import src.core.Tower;
 import src.core.Upgrade;
 import src.ui.IDrawableCreep;
 import src.ui.IDrawableTower;
 import src.ui.side.Sidebar;
 
 /**
  * Manages the interaction between the GUI and the backend.  Most GUI calls end up here,
  * and the controller manages the interaction with the backend.
  */
 public class GameController {
 	private Game game;
 	private Tower placingTower; // a tower pending purchase
 	private Tower selectedTower; // a tower that is selected
 	private Sidebar side;
 	private boolean isPaused;
 	private boolean isDoubleTime;
 	
 	public GameController() {
 		placingTower = null;
 		selectedTower = null;
 		isPaused = false;
 		isDoubleTime = false;
 	}
 	
 	public void setSidebar(Sidebar side) {
 		this.side = side;
 	}
 
 	public Game getGame() {
 		return game;
 	}
 	
 	public void setGame(Game g) {
 		game = g;
 	}
 	
 	/*
 	 * Time control methods
 	 */
 	
 	/**
 	 * Executes one game "tick", unless the game is paused.
 	 * If the game speed is in double time mode, ticks happen twice as fast/
 	 */
 	public void tick() {
 		if (!isPaused) {
 			game.tick();
 			
 			if (isDoubleTime) {
 				game.tick();
 			}
 		}
 	}
 	
 	public void togglePause(boolean shouldPause) {
 		isPaused = shouldPause;
 	}
 	
 	public void toggleDoubleTime(boolean dt) {
 		isDoubleTime = dt;
 	}
 	
 	/*
 	 * Useful drawing/UI information
 	 */
 	public Collection<? extends IDrawableCreep> getDrawableCreeps() {
 		return game.getCreeps();
 	}
 	
 	public Collection<? extends IDrawableTower> getDrawableTowers() {
 		return game.getTowers();
 	}
 	
 	public boolean tileIsOccupied(int x, int y) {
 		Tower t = getTowerAtTile(x, y);
 		return t == null ? false : true;
 	}
 	
 	private Tower getTowerAtTile(int x, int y) {
 		for (Tower t : game.getTowers()) {
 			if (t.getX() == x && t.getY() == y) {
 				return t;
 			}
 		}
 		
 		return null;
 	}
 	
 	public boolean playerCanAfford(IPurchasable item) {
 		if (game.getPlayer().getGold() >= item.getPrice()) {
 			return true;
 		}
 		
 		return false;
 	}
 	
 	/*
 	 * Tower upgrade handling methods
 	 */
 	public boolean isTowerSelected() {
 		return selectedTower != null;
 	}
 	
 	public Tower getSelectedTower() {
 		return selectedTower;
 	}
 
 	public void setSelectedTower(Tower selectedTower) {
 		this.selectedTower = selectedTower;
 	}
 	
 	/**
 	 * Applies the appropriate upgrade to the currently selected tower.
 	 * @param level The upgrade level of the upgrade to be applied
 	 * @param idx Which upgrade in the given level to apply
 	 */
 	public void applyTowerUpgrade(int level, int idx) {
 		int seenAtLevel = 0;
 		
 		for (Upgrade u : selectedTower.getUpgrades()) {
 			if (u.getLevel() == level) seenAtLevel++;
 			
 			if (seenAtLevel - 1 == idx) {
 				selectedTower.applyUpgrade(u);
 				game.getPlayer().purchase(u);
 				return;
 			}
 		}
 	}
 	
 	/**
 	 * If the tower at the tile (x, y) is already selected, unselects this tower. 
 	 * Otherwise, selects the tower. This acts as a convenience method for the UI, and
 	 * in particular, MapCanvas.
 	 * @param x The x coordinate of the tower
 	 * @param y The y coordinate of the tower
 	 */
 	public void toggleTowerSelection(int x, int y) {
 		Tower t = getTowerAtTile(x, y);
 		
 		if (t == null || selectedTower == getTowerAtTile(x, y)) {
 			unselectTower();
 		} else {
 			selectedTower = t;
 			side.showTowerUpgrade();
 		}
 	}
 	
 	/**
 	 * Unselects the current tower, if any.
 	 */
 	public void unselectTower() {
 		selectedTower = null;
 		side.showTowerPurchase();
 	}
 	
 	/**
 	 * Sells the currently selected tower, if any.  Refunds the user
 	 * a certain percentage of their total investment in the tower.
 	 */
 	public void sellTower() {
 		if (selectedTower == null) return;
 		
 		Player p = game.getPlayer();
 		p.setGold(p.getGold() + selectedTower.getInvestment() * .75);
 		
 		game.getTowers().remove(selectedTower);
 		unselectTower();
 	}
 
 	/*
 	 * Tower purchase handling methods
 	 */
 	public boolean isPlacingTower() {
 		return placingTower != null;
 	}
 
 	public void setPlacingTower(Tower t) {
 		placingTower = t;
 	}
 	
 	public Tower getPlacingTower() {
 		return placingTower;
 	}
 	
 	/**
 	 * Begins the potential sale of a tower.  The sale is not finalized until finalizeTowerPurchase
 	 * is called by the UI.
 	 * @param t The tower being considered for purchase.
 	 * @see cancelTowerPurchase
 	 * @see finalizeTowerPurchase
 	 */
 	public void beginPurchasingTower(Tower t) {
 		setPlacingTower(t);
 		side.showTowerPurchaseCancel();
 	}
 	
 	/**
 	 * Cancels the current tower purchase that is under consideration
 	 */
 	public void cancelTowerPurchase() {
 		setPlacingTower(null);
 		side.showTowerPurchase();
 	}
 	
 	/**
 	 * Finalizes the sale of the tower under consideration (in placingTower).  Performs appropriate
 	 * related actions, such as subtracting the correct amount of gold from the user's stash.
 	 * @param x The x coordinate of the map tile where this tower should be placed.
 	 * @param y The y coordinate of the map tile where this tower should be placed.
 	 */
 	public void finalizeTowerPurchase(int x, int y) {
 		placingTower.setX(x);
 		placingTower.setY(y);
 		
 		game.getPlayer().purchase(placingTower);
 		game.getTowers().add(placingTower);
 		placingTower = null;
 		
 		side.showTowerPurchase();
 	}
 }
