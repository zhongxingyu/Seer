 package src.ui.controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.util.Collection;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JComponent;
 import javax.swing.KeyStroke;
 
 import src.GameMain;
 import src.Runner;
 import src.core.Bullet;
 import src.core.Creep;
 import src.core.Game;
 import src.core.IPurchasable;
 import src.core.Player;
 import src.core.TargetingInfo;
 import src.core.Tower;
 import src.core.Upgrade;
 import src.core.IAlignment.Alignment;
 import src.net.NetworkGame;
 import src.ui.GameOverPanel;
 import src.ui.IDrawableCreep;
 import src.ui.IDrawableTower;
 import src.ui.TitleScreen;
 import src.ui.side.Sidebar;
 
 /**
  * Manages the interaction between the GUI and the backend.  Most GUI calls end up here,
  * and the controller manages the interaction with the backend.
  */
 public class GameController {
 	public static final double towerRefundPercentage = .75;
 	
 	private MultiplayerController multiController;
 	
 	private Game game;
 	private GameMain gameMain;
 	private Tower placingTower; // a tower pending purchase
 	private Tower selectedTower; // a tower that is selected
 	private Sidebar side;
 	private Thread runnerThread;
 	private boolean isPaused;
 	private boolean isDoubleTime;
 	
 	// key handling info
 	private enum KeyBinding {
 		ESC_PRESSED,
 		T_ONE,
 		T_TWO,
 		T_THREE,
 		T_FOUR,
 		T_FIVE,
 		T_SIX,
 		T_SEVEN,
 		T_EIGHT
 	}
 	
 	private Action escAction;
 	
 	public GameController() {
 		placingTower = null;
 		selectedTower = null;
 		isPaused = false;
 		isDoubleTime = false;
 		
 		// initialize the esc key action
 		final GameController gc = this;
 		escAction = new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				if (gc.isPlacingTower()) {
 					gc.cancelTowerPurchase();
 				} else if (gc.isTowerSelected()) {
 					gc.unselectTower();
 				}
 			}
 		};
 		
 		escAction.setEnabled(false);
 	}
 	
 	public void start() {
 		Runner r = new Runner();
 		r.setGameController(this);
 		runnerThread = new Thread(r, "Game Runner Thread");
 		runnerThread.start();
 	}
 	
 	public void stop() {
 		runnerThread.stop(); // this is technically not safe, if it causes problems, we should change it
 	}
 	
 	public void quit() {
 		stop(); 
 		gameMain.showScreen(new TitleScreen(gameMain));
 	}
 	
 	public void setSidebar(Sidebar side) {
 		this.side = side;
 		
 		// this is a bit silly - we want this event to be global, because it appears in multiple contexts
 		// unfortunately, bindings need to be attached to components, and this is the only component we have.
 		// it ends up working great though, so it's all good
 		side.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), KeyBinding.ESC_PRESSED);
 		side.getActionMap().put(KeyBinding.ESC_PRESSED, escAction);
 	}
 
 	public Game getGame() {
 		return game;
 	}
 	
 	public void setGame(Game g) {
 		game = g;
 	}
 	
 	public GameMain getGameMain() {
 		return gameMain;
 	}
 	
 	public void setGameMain(GameMain gm) {
 		gameMain = gm;
 	}
 	
 	public void setMultiplayerController(MultiplayerController mc) {
 		this.multiController = mc;
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
 			
 			if (game.isOver()) {
 				if (game instanceof NetworkGame) {
 					multiController.gameOver(((NetworkGame)game).getOpponent().getHealth() <= 0);
 				} else { 
 					gameMain.showScreen(new GameOverPanel(gameMain));
 				}
 				
 				stop();
 			}
 			
 			/* There seems to be a bug somewhere in this code... TODO: fix this
 
 			if (isFriday() && yesterdayWasThursday() && tomorrowIsSaturday() && sundayComesAfterwards()) {
 				try {
 					becomeFresh();
 					goDownstairs();
 					
 					Cereal c = new Cereal("Cheerios");
 					haveBowl(c);
 					
 					headToBusStop()
 				} catch (MorningRoutineException ex) {
 					\\ OMG, that is totally lame
 				} catch (SeatDecisionException ex) {
 					System.err.println("Which seat can I take? Unable to decide, failing...");
 					System.exit(1);
 				} finally {
 					while (true) {
 						getDown();
 						setExcited("we we we");
						sayToConfusedListeners("You know what it is");
 					}
 				}
 			}
 			
 			*/
 		}
 	}
 	
 	public void togglePause(boolean shouldPause) {
 		isPaused = shouldPause;
 		if (shouldPause) {
 			if (isPlacingTower())
 				cancelTowerPurchase();
 			
 			escAction.setEnabled(false);
 			side.enableSidebar(false);
 		}
 		else {
 			if (isPlacingTower() || isTowerSelected()) escAction.setEnabled(true);
 			
 			side.enableSidebar(true);
 		}
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
 	
 	public Collection<Bullet> getBullets() {
 		return game.getBullets();
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
 	
 	public Upgrade getTowerUpgrade(int level, int idx){
 		int seenAtLevel = 0;
 		for (Upgrade u : selectedTower.getUpgrades()) {
 			if (u.getLevel() == level) seenAtLevel++;
 			
 			if (seenAtLevel - 1 == idx) 
 				return u;
 		}
 		
 		return null;
 	}
 	
 	public void applyAlignment(Alignment al) {
 		game.getPlayer().purchase(al);
 		selectedTower.setAlignment(al);
 	}
 	
 	public void setTowerStrategy(TargetingInfo.Strategy s) {
 		selectedTower.getTargeting().setStrategy(s);
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
 			escAction.setEnabled(false);
 		} else {
 			selectedTower = t;
 			side.showTowerUpgrade();
 			escAction.setEnabled(true);
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
 		p.setGold(p.getGold() + selectedTower.getInvestment() * towerRefundPercentage);
 		
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
 		escAction.setEnabled(true);
 		side.showTowerPurchaseCancel();
 	}
 	
 	/**
 	 * Cancels the current tower purchase that is under consideration
 	 */
 	public void cancelTowerPurchase() {
 		setPlacingTower(null);
 		escAction.setEnabled(false);
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
 		
 		escAction.setEnabled(false);
 		side.showTowerPurchase();
 	}
 	
 	public void finalizeCreepPurchase(Creep c){
 		game.getYourCreeps().add(c);
 	}
 	
 	public boolean getPaused() {
 		return isPaused;
 	}
 	
 	public Sidebar getSideBar() {
 		return side;
 	}
 }
