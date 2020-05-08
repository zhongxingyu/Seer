 package vooga.towerdefense.controller;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import java.util.Set;
 
 import util.Location;
 import vooga.towerdefense.util.Pixmap;
 import vooga.towerdefense.attributes.AttributeConstantsEnum;
 import vooga.towerdefense.controller.modes.BuildMode;
 import vooga.towerdefense.controller.modes.ControlMode;
 import vooga.towerdefense.controller.modes.SelectMode;
 import vooga.towerdefense.factories.elementfactories.GameElementFactory;
 import vooga.towerdefense.gameeditor.gameloader.xmlloaders.GameLoader;
 import vooga.towerdefense.gameelements.GameElement;
 import vooga.towerdefense.model.GameLoop;
 import vooga.towerdefense.model.GameMap;
 import vooga.towerdefense.model.GameModel;
 import vooga.towerdefense.model.Player;
 import vooga.towerdefense.model.Tile;
 import vooga.towerdefense.model.shop.Shop;
 import vooga.towerdefense.model.shop.ShopItem;
 import vooga.towerdefense.view.TDView;
 
 /**
  * Controller is the channel of communication between the Model and the View.
  * 
  * @author Angelica Schwartz
  * @author Erick Gonzalez
  * @author Leonard K. Ng'eno
  * @author Jimmy Longley
  */
 public class Controller {
 
 	/**
 	 * location of resource bundle.
 	 */
 	private static final String DEFAULT_RESOURCE_PACKAGE = "vooga/towerdefense/resources.";
 	/**
 	 * resource bundle for this controller.
 	 */
 	private ResourceBundle myResourceBundle;
 	/**
 	 * model for this game.
 	 */
 	private GameModel myModel;
 	/**
 	 * view for this game.
 	 */
 	private TDView myView;
 	/**
 	 * game loader for this controller.
 	 */
 	private GameLoader myGameLoader;
 	/**
 	 * map of image to the map object.
 	 */
 	private Map<Pixmap, GameMap> myAvailableMaps;
 	/**
 	 * control mode for the controller.
 	 */
 	private ControlMode myControlMode;
 
 	// TODO: controller constructor should take waves & map in order to
 	// initialize GameModel?
 	// TODO: fix where the parameters come from
 	public Controller(String language, String xmlPath)
 			throws IllegalArgumentException, ClassNotFoundException,
 			InstantiationException, IllegalAccessException,
 			InvocationTargetException {
 	    
 		setLanguage(language);
 		myGameLoader = new GameLoader(xmlPath);
 		myView = new TDView(this);
 		myView.showSplashScreen();
 		setMaps();
 	}
 
 	/**
 	 * sets up the view for this game.
 	 * 
 	 * @throws InvocationTargetException
 	 * @throws IllegalAccessException
 	 * @throws InstantiationException
 	 * @throws ClassNotFoundException
 	 * @throws IllegalArgumentException
 	 */
 	private void setView() throws IllegalArgumentException,
 			ClassNotFoundException, InstantiationException,
 			IllegalAccessException, InvocationTargetException {
 		myView = myGameLoader.loadView(myView, this);
 	}
 
 	/**
 	 * sets the maps available for this game.
 	 */
 	private void setMaps() {
 		myAvailableMaps = new HashMap<Pixmap, GameMap>();
 		List<GameMap> mapChoices = myGameLoader.loadMaps();
 		for (GameMap map : mapChoices) {
 			myAvailableMaps.put(map.getBackgroundImage(), map);
 		}
 	}
 
 	/**
 	 * sets the map for this game.
 	 * 
 	 * @param mapChoice
 	 * @throws InvocationTargetException
 	 * @throws IllegalAccessException
 	 * @throws InstantiationException
 	 * @throws ClassNotFoundException
 	 * @throws IllegalArgumentException
 	 */
 	public void setMap(Pixmap mapChoice) throws IllegalArgumentException,
 			ClassNotFoundException, InstantiationException,
 			IllegalAccessException, InvocationTargetException {
 		addMapAndLoadGame(myAvailableMaps.get(mapChoice));
 		setView();
 	}
 
 	/**
 	 * gets the map images for this game.
 	 * 
 	 * @return set of pixmap
 	 */
 	public Set<Pixmap> getMapImages() {
 		return myAvailableMaps.keySet();
 	}
 
 	/**
 	 * adds the map to the game and loads the remainder of the game state.
 	 * 
 	 * @param map
 	 */
 	private void addMapAndLoadGame(GameMap map) {
 	        Player player = myGameLoader.loadPlayer(this);
 	        List<GameElementFactory> factories = myGameLoader.loadElements(map,
 				player);
 		myModel = new GameModel(this, player, map, new Shop(map, factories));
 		myModel.setRules(myGameLoader.loadRules(myModel));
 		myModel.setLevels(myGameLoader.loadLevels(myModel, map, player));
 		myControlMode = new SelectMode();
 		start();
 	}
 
 	/**
 	 * cancels the purchase and stops painting ghost image.
 	 */
 	public void cancelPurchaseFromShop() {
 		myModel.getMap().resetGhostImage();
 		myControlMode = new SelectMode();
 		setVisibilityOfShopCancelButton(false);
 	}
 
 	private void setVisibilityOfShopCancelButton(boolean visibility) {
 		myView.getShopScreen().setCancelButtonVisibility(visibility);
 	}
 
 	/**
 	 * displays information about the GameElement on the tile.
 	 * 
 	 * @param p
 	 *            is the point that was clicked.
 	 */
 	public void displayElementInformation(GameElement e) {
 		if (e != null) {
 			myView.getGameElementInfoScreen().displayInformation(e.getAttributeManager().toString());
 			if (e.getAttributeManager().hasUpgrades()) {
 				List<String> upgrades = new ArrayList<String>(e
 						.getAttributeManager().getUpgrades().keySet());
 				myView.getGameElementInfoScreen().displayUpgradesAndButton(
 						upgrades);
 			}
 		} else {
 			myView.getGameElementInfoScreen().clearScreen();
 		}
 	}
 
 	/**
 	 * updates the display on the MapScreen.
 	 */
 	public void displayMap() {
 		myView.getMapScreen().update();
 	}
 
 	/**
 	 * places the new item onto the map & changes the mode back to SelectMode.
 	 * 
 	 * @param item
 	 * @param p
 	 */
 	public void fixItemOnMap(GameElement item, Point p) {
 		if (myModel.getMap().isTower(item)) {
 			myModel.getMap().blockTiles(item);
 			myModel.getMap().updatePaths();
 		}
 		myModel.getMap().addToMap(item);
 		displayMap();
 		myControlMode = new SelectMode();
 		setVisibilityOfShopCancelButton(false);
 	}
 
 	/**
 	 * gets the associated game element at a point.
 	 * 
 	 * @param p
 	 * @return the game element
 	 */
 	public GameElement getItemAt(Point p) {
 		Tile tile = myModel.getTile(p);
 		if (tile.containsElement()) {
 			return tile.getElement();
 		}
 		return null;
 	}
 
 	/**
 	 * gets the resource bundle for this controller.
 	 * 
 	 * @return the resource bundle
 	 */
 	public ResourceBundle getResourceBundle() {
 		return myResourceBundle;
 	}
 
 	/**
 	 * Get the matching string from the resource bundle.
 	 * 
 	 * @param s
 	 *            is the string to match
 	 * @return the appropriate string in the selected language
 	 */
 	public String getStringFromResources(String s) {
 		return myResourceBundle.getString(s);
 	}
 
 	/**
 	 * handles a click to the map appropriately depending on the mode.
 	 * 
 	 * @param p
 	 *            is the location of the click
 	 */
 	public void handleMapMouseDrag(Point p) {
 		myControlMode.handleMapMouseDrag(p, this);
 	}
 
 	/**
 	 * handles a mouse drag on the map appropriately depending on the mode.
 	 * 
 	 * @param p
 	 *            is the location of the mouse
 	 */
 	public void handleMapClick(Point p) {
 		myControlMode.handleMapClick(p, this);
 	}
 
 	/**
 	 * changes the mode to BuildMode and gets the item the user wants to build
 	 * from the Shop.
 	 * 
 	 * @param itemName
 	 *            is the name of the item the user wants to buy
 	 */
 	public void handleShopClickOnItem(Point p) {
 		ShopItem itemToBuy = myModel.getShopItem(p);
 
 		// no item clicked
		if (itemToBuy == null || myModel.getPlayer().getAttributeManager().getAttribute(AttributeConstantsEnum.MONEY.getStatusCode()).getValue() <= 0)
 			return;
 
 		BuildMode myNewMode = new BuildMode();
 
 		GameElementFactory factory = itemToBuy.getFactory();
 		GameElement t = factory.createElement(new Location());
 		myNewMode.setItemToBuild(t);
 		
 		double cost = t.getAttributeManager()
 				.getAttribute(AttributeConstantsEnum.COST.getStatusCode())
 				.getValue();
 		myNewMode.setCost(cost);
 		
 		myControlMode = myNewMode;
 	}
 
 	/**
 	 * starts the next level in the model.
 	 */
 	public void startNextLevel() {
 		myModel.startNextLevel();
 	}
 
 	public Location getPointSnappedToGrid(Location location) {
 		return myModel.getMap().getTile(location).getCenter();
 	}
 
 	/**
 	 * paints the ghost image of the item on the MapScreen on the mouse's
 	 * location.
 	 * 
 	 * @param p
 	 *            is the mouselocation
 	 * @param itemImage
 	 *            is the image
 	 */
 	public void paintGhostImage(Pixmap itemImage, Location location,
 			Dimension size) {
 		displayMap();
 		myModel.getMap().addGhostImage(itemImage, location, size);
 	}
 
 	/**
 	 * paints the map.
 	 * 
 	 * @param pen
 	 */
 	public void paintMap(Graphics pen) {
 		myModel.paintMap((Graphics2D) pen);
 	}
 
 	/**
 	 * updates the model.
 	 * 
 	 * @param elapsedTime
 	 */
 	public void update(double elapsedTime) {
 		myModel.update(elapsedTime);
 	}
 
 	/**
 	 * upgrades the item to the new type.
 	 * 
 	 * @param upgradeName
 	 */
 	// TODO: Fix for game elements to be towers. -matthew
 	public void upgradeSelectedItemTo(String upgradeName) {
 		GameElement t = ((SelectMode) myControlMode).getCurrentlySelectedItem();
 		// t.upgrade(upgradeName);
 		// TODO: implement upgrade stuff on backend (ask unit team for tower upgrade info!)
 	}
 
 	/**
 	 * Sets the language
 	 * 
 	 * @param language
 	 *            the language to set the controller to
 	 */
 	public void setLanguage(String language) {
 		try {
 		    myResourceBundle = ResourceBundle
 		            .getBundle(DEFAULT_RESOURCE_PACKAGE + language);
 		} catch (MissingResourceException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Start the game controller.
 	 */
 	public void start() {
 		GameLoop game = new GameLoop(this);
 		myModel.startNextLevel();
 		game.start();
 	}
 
 	/**
 	 * paints the shop.
 	 * @param pen
 	 */
 	public void paintShop(Graphics2D pen) {
 		myModel.paintShop(pen);
 	}
 
 	/**
 	 * Used to determine if a ghost image should be painted, it tests if a tower
 	 * can be built at a particular point.
 	 * 
 	 * @param p
 	 * @return
 	 */
 	public boolean canBuildHere(Point p, int tilesWide, int tilesTall) {
 		boolean canBuild = true;
 		for (int i = 0; i < tilesWide; i++) {
 			for (int j = 0; j < tilesTall; j++) {
 				Location location = new Location(p.getX() + i
 						* myModel.getMap().getTileSize().getWidth(), p.getY()
 						+ j * myModel.getMap().getTileSize().getHeight());
 				canBuild = canBuild & myModel.getMap().isBuildable(location);
 			}
 		}
 
 		return canBuild;
 	}
 
 	/**
 	 * displays the player statistics on the appropriate screen.
 	 * @param playerData
 	 */
 	public void displayPlayerStatistics(String playerData) {
 		myView.getStatsScreen().displayInformation(playerData);
 	}
 
 	/**
 	 * gets the tile size for the map.
 	 * @return the tile size as a dimension
 	 */
 	public Dimension getTileSize() {
 		return myModel.getMap().getTileSize();
 	}
 
 	/**
 	 * The function called when the model reaches winning conditions
 	 */
 	public void win() {
 		myView.showWinScreen();
 	}
 
 	/**
 	 * The function called when the model reaches losing conditions
 	 */
 	public void lose() {
 		myView.showLoseScreen();
 	}
 
 	/**
 	 * decrements a players money by a specified amount.
 	 * 
 	 * @param myCost
 	 */
 	public void spend(double cost) {
 		myModel.getPlayer().getAttributeManager()
 				.getAttribute(AttributeConstantsEnum.MONEY.getStatusCode())
 				.modifyValue(-cost);
 	}
 
 	/**
 	 * updates the visible timer for the wave.
 	 * @param timer is the remaining time
 	 */
 	public void updateWaveTimer(double timer) {
 		myView.getNextWaveScreen().updateTimerDisplay(String.valueOf(timer));
 	}
 }
