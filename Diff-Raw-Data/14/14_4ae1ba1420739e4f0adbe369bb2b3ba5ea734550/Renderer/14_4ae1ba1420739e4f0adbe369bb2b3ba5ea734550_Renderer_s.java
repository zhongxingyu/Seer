 package renderer;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import javax.swing.JFrame;
 
 import playable.Player;
 import playable.TypeOfPlayer;
 import renderer.sprite.BaseSprite;
 import renderer.sprite.LvlSprite;
 import renderer.sprite.PlayerSprite;
 import renderer.sprite.ProjectileSprite;
 import renderer.sprite.SelectedSprite;
 import renderer.sprite.Sprite;
 import renderer.sprite.TowerSprite;
 import renderer.sprite.UnitSprite;
 import dispatcher.Dispatcher;
 import dispatcher.TypeOfTower;
 import engine.Base;
 import engine.Projectile;
 import engine.Unit;
 import engine.tower.Tower;
 
 
 /**
  * Renderer is the main class for all displaying stuff. It creates all specific renderers like MapRenderer.
  * It also manages the main frame.
  * 
  * @author Jijidici
  *
  */
 public class Renderer{
 	private static final int WIDTH = 800;
 	private static final int HEIGHT = 600;
 	
 	private JFrame frame;
 	private MapRenderer mapRenderer;
 	private UIRenderer uiRenderer;
 		
 	/**
 	 * Constructor asking for the window title.
 	 * @param frameTitle - String containing the title of the game.
 	 */
 	public Renderer(String frameTitle){
 		super();
 		
 		this.frame = new JFrame(frameTitle);
 		this.mapRenderer = new MapRenderer(this.frame.getLayeredPane(), WIDTH, HEIGHT);
 		this.uiRenderer = new UIRenderer(this.frame.getLayeredPane(), WIDTH, HEIGHT);
 	}
 	
 	/**
 	 * Initialize the Renderer by building every Swing resource and setting the main window.
 	 * @throws IOException
 	 */
 	public void init() throws IOException{
 		//Set up the frame
 		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.frame.setSize(WIDTH, HEIGHT);
 		this.frame.setResizable(false);
 		this.frame.addKeyListener(new KeyListener(){
 			@Override
 			public void keyPressed(KeyEvent e) {
 				if(e.getKeyCode() == 32){ //key "space"
 					SelectedSprite.resetStartingElements();
 					SelectedSprite.resetEndingElement();
 					TowerSprite.resetTowerToBuild();
 					Dispatcher.getRenderer().hideRadialMenus();
					
 				}
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e) {}
 			@Override
 			public void keyTyped(KeyEvent e) {}
 		});
 		
 		//Initialize secondary renderer
 		this.mapRenderer.init();
 		this.uiRenderer.init();
 	}	
 
 	
 	/**
 	 * Display the main frame.
 	 */
 	public void render(){
 		this.frame.setVisible(true);
 	}
 	
 	/**
 	 * This method is called every frame, in order to refresh all important elements in our frame.
 	 * @param idDeletedInEngine this ArrayList<Integer> contains all id of engine elements just deleted.
 	 */
 	public void refreshView(ArrayList<Integer> idDeletedInEngine) {
 		// ----- manage deletion ----- //
 		//update sprites list
 		Iterator<Sprite> iterSprites = this.getSprites().iterator();
 		while(iterSprites.hasNext()){
 			Sprite sprite = iterSprites.next();
 			if(idDeletedInEngine.contains(sprite.getId())){
 				sprite.setVisible(false);
 				this.mapRenderer.getContainer().remove(sprite);
 				iterSprites.remove();
 			}
 			if(sprite.getClass() == ProjectileSprite.class){
 				ProjectileSprite projectileSprite = (ProjectileSprite) sprite;
 				if(projectileSprite.getEngineProjectile().hasTouchedFlag()){
 					projectileSprite.setVisible(false);
 					this.mapRenderer.getContainer().remove(projectileSprite);
 					iterSprites.remove();
 				}
 			}
 		}
 
 		// ----- manage uiRenderer ----- //
 		//update the display or hide of radial menu
 		this.uiRenderer.refreshRadialMenuMovment();
 		this.uiRenderer.refreshRadialMenuTower();
 		
 		// ----- manage players ----- //
 		for(PlayerSprite p:this.uiRenderer.getPlayerSprites()){
 			p.getNbAgents().setText(String.valueOf(p.getEnginePlayer().getTotalNbAgents()));
 			if(!p.getEnginePlayer().isAlive())
 				p.setImage(TypeOfPlayer.NEUTRAL.getImageOfPlayer());
 		}
 		
 	}
 	
 	public void resetTheGame(Player theWinner){
 		mapRenderer.hideSprites();
 		mapRenderer.getSprites().clear();
 		uiRenderer.hidePlayerSprites();
 		uiRenderer.getPlayerSprites().clear();
 		
 		MapRenderer.resetSelectionStartingCorner();
 		MapRenderer.resetSelectionEndingCorner();
 		
 		SelectedSprite.resetStartingElements();
 		SelectedSprite.resetEndingElement();
 		TowerSprite.resetTowerToBuild();
 		TowerSprite.resetChosenTowerType();
 		TowerSprite.resetHighlightedTower();
 		
 		if(theWinner.isPlayer())
 			uiRenderer.hideWinnerBackground(Dispatcher.getCurrentLevel());
 		else
 			uiRenderer.hideLoserBackground(Dispatcher.getCurrentLevel());
 	}
 	
 	//MAPRENDERER INDIRECTIONS
 	
 	/**
 	 * Add the Base Sprite to the MapRenderer Sprite Collection
 	 * @param newBase : the base engine, corresponding to the next created sprite.
 	 */
 	public int addBaseSprite(Base newBase){
 		return this.mapRenderer.addBaseSprite(newBase);
 	}
 	
 	/**
 	 * Add the Unit Sprite to the MapRenderer Sprite Collection
 	 * @param newUnit : the unit engine, corresponding to the next created sprite.
 	 */
 	public int addUnitSprite(Unit newUnit){
 		return this.mapRenderer.addUnitSprite(newUnit);
 	}
 	
 	/**
 	 * Add the Tower Sprite to the MapRenderer Sprite Collection
 	 * @param newTower : the tower engine, corresponding to the next created sprite.
 	 */
 	public int addTowerSprite(Tower newTower){
 		return this.mapRenderer.addTowerSprite(newTower);
 	}
 	
 	/**
 	 * Add the Projectile Sprite to the MapRenderer Sprite Collection
 	 * @param newProjectile : the projectile engine, corresponding to the next created sprite.
 	 * @param type the type of tower which have created the projectile
 	 */
 	public int addProjectileSprite(Projectile newProjectile, TypeOfTower type){
 		return this.mapRenderer.addProjectileSprite(newProjectile, type);
 	}
 	
 	/**
 	 * Remove the sprite which has the given ID
 	 * @param id the id of the sprite to remove
 	 */
 	public void removeSprite(int id){
 		this.mapRenderer.removeSprite(id);
 	}
 	
 	/**
 	 * Update the TowerSprite of a newly specialized Tower in the Sprite Array
 	 * @param newTower The new specialized Tower
 	 * @param oldID id of the old non-specialize tower
 	 */
 	public void updateTowerSprite(Tower newTower, int oldID){
 		this.mapRenderer.updateTowerSprite(newTower, oldID);
 	}
 	
 	/**
 	 * Change the image of background 
 	 */
 	public void setBackgroundImage(){
 		this.mapRenderer.setBackgroundImage(this.uiRenderer.getSelectedBacckgroundImage());
 	}
 	
 	//UIRENDERER INDIRECTIONS
 	
 	/**
 	 * Display winner message
 	 */
 	public void displayWinner(int currentLevel){
 		this.uiRenderer.displayWinner(currentLevel);
 	}
 	
 	/**
 	 * Display loser message
 	 */
 	public void displayLoser(int currentLevel){
 		this.uiRenderer.displayLoser(currentLevel);
 	}
 
 	/**
 	 * Display the menu
 	 */	
 	public void displayMenu() {
 		this.uiRenderer.displayMenu();		
 	}
 	
 	/**
 	 * Hide the menu
 	 */	
 	public void hideMenu() {
 		this.uiRenderer.hideMenu();		
 	}
 
 	/**
 	 * Hide the radial menu
 	 */		
 	public void hideRadialMenuMovment(){
 		this.uiRenderer.hideRadialMenuMovment();
 	}
 	
 	public void hideRadialMenus(){
 		this.uiRenderer.hideRadialMenuMovment();
 		this.uiRenderer.hideRadialMenuTower();
 	}
 	
 	/**
 	 * Add the Player Sprite to the Sprite Collection of the uiRenderer
 	 * @param newPlayer : the player engine, corresponding to the next created sprite.
 	 */
 	public void addPlayerSprites(HashMap<String, Player> newPlayers) {
 		this.uiRenderer.addPlayerSprites(newPlayers);
 	}
 
 	/**
 	 * Indicates if the player is choosing the number of agents to send with the radial menu
 	 * @return true if the player is choosing, else false
 	 */
 	public boolean isChoosingUnit(){
 		return this.uiRenderer.isChoosingUnitFlag();
 	}
 	
 	public double getUnitPercentChosen(){
 		return this.uiRenderer.getUnitPercentChosen();
 	}
 	/**
 	 * Check if the player has chosen his tower type
 	 * @return boolean - true if the tower type is chosen
 	 */
 	public boolean isTowerTypeChosen(){
 		return this.uiRenderer.isTowerTypeChosen();
 	}
 	
 	public LvlSprite getLvlSelected() {
 		return this.uiRenderer.getLvlSelected();
 	}
 	
 	public void resetLvlSelected(){
 		this.uiRenderer.resetLvlSelected();
 	}
 	
 	public void addLvlsToTheMenu() throws IOException{
 		this.uiRenderer.addLvlsToTheMenu();
 	}
 	
 	// GETTERS & SETTERS
 	
 	/**
 	 * Get the sprite by knowledge of its id.
 	 * @param id : the id of the sprite.
 	 * @return a sprite
 	 */
 	public Sprite getSprite(int id) {
 		return this.mapRenderer.getSprite(id);
 	}
 
 	/**
 	 * Get all the sprites.
 	 * @return ArrayList<Sprite>
 	 */
 	public ArrayList<Sprite> getSprites() {
 		return this.mapRenderer.getSprites();
 	}
 
 	/**
 	 * Get only the unitSprites
 	 * @return ArrayList<UnitSprite>
 	 */
 	public ArrayList<UnitSprite> getUnitSprites() {
 		return this.mapRenderer.getUnitSprites();
 	}
 	
 	/**
 	 * Get only the baseSprites
 	 * @return ArrayList<UnitSprite>
 	 */
 	public ArrayList<BaseSprite> getBaseSprites() {
 		return this.mapRenderer.getBaseSprites();
 	}
 	
 	/**
 	 * Get only the towersSprites
 	 * @return ArrayList<UnitSprite>
 	 */
 	public ArrayList<TowerSprite> getTowerSprites() {
 		return this.mapRenderer.getTowerSprites();
 	}
 	
 	public JFrame getFrame(){
 		return frame;
 	}
 
 	public MapRenderer getMapRenderer() {
 		return mapRenderer;
 	}
 
 	public UIRenderer getUIRenderer() {
 		return uiRenderer;
 	}
 }
