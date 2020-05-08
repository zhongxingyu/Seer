 package screens;
 
 
 import managers.StoreInventory;
 import interfaces.ResourceTracker;
 import interfaces.StoreGui;
 import gameObjects.Player;
 import gameObjects.PlayerToken;
 import gameObjects.Buildings.AssayOffice;
 import gameObjects.Buildings.LandOffice;
 import gameObjects.Buildings.Pub;
 import gameObjects.Buildings.Store;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
 import com.me.mygdxgame.Mule;
 
 /**
  * This class will represent the town to the player. It has four locations that the player could visit.
  * @author antonio
  *
  */
 public class TownScreen implements Screen{
 
 	public final String BACKGROUND_LOCATION = "TownScreen/Background.jpg";
 	
 	private final int STORE_GUI_X = 50;
 	private final int STORE_GUI_Y = 50;
 	
 	private Stage stage;
 	
 	private Mule game;
 	
 	private Store store;
 	private Pub pub;
 	private LandOffice landOffice;
 	private AssayOffice assayOffice;
 	private ResourceTracker resourceGUI;
 	private PlayerToken token;
 	private StoreGui storeGui;
 	
 	private int width;
 	private int height;
 	private int realHeight;
 
 	boolean guiOverlay;
 	boolean storeEntered = false;
 	
 	public TownScreen(Mule g){
 		super();
 		game = g;
 		stage = new Stage();
 		setBackground();
 		setBuildings();
 		storeGui = new StoreGui(STORE_GUI_X, STORE_GUI_Y);
 	}
 	
 	/**
 	 * Called when we want to upate the game manager with the store inventory
 	 */
 	public void setGameManagerStoreInventory(){
 		Mule.gm.setStoreInventory(storeGui.getStoreInventory());
 	}
 	
 	public void setTownScreenStoreInventory(){
 		storeGui.setStoreInventory(Mule.gm.getStoreInventory());
 	}
 	
 	public void setTownScreenStoreInventory(StoreInventory si){
 		storeGui.setStoreInventory(si);
 	}
 	
 	public StoreInventory getStoreInventory(){
 		return storeGui.getStoreInventory();
 	}
 	
 	private void setPlayer(){
 		Player p = Mule.pm.getCurrentPlayer();
 		token = new PlayerToken(p, Mule.WIDTH / 2, Mule.HEIGHT / 2);
 		//stage.addActor(token);
 	}
 	
 	/**
 	 * Setting location of buildings. The top buildings have an additional buffer for the UI components
 	 */
 	private void setBuildings(){
 		int buffer_x = 25;
 		int buffer_y = 25;
 		int extraBuffer = 80;
 		
 		store = new Store(stage, buffer_x, buffer_y);
 		pub = new Pub(stage);
 		landOffice = new LandOffice(stage);
 		assayOffice = new AssayOffice(stage);
 		
 		pub.setX(Mule.WIDTH - buffer_x - pub.getWidth());
 		pub.setY(buffer_y);
 		
 		landOffice.setX(buffer_x);
 		landOffice.setY(Mule.HEIGHT - buffer_y - extraBuffer - landOffice.getHeight());
 		
 		assayOffice.setX(Mule.WIDTH - buffer_x - assayOffice.getWidth());
 		assayOffice.setY(Mule.HEIGHT - buffer_y - extraBuffer - assayOffice.getHeight());
 	}
 	
 	private void setBackground(){
 		Group background = new Group();
 		background.setBounds(0, 0, Mule.WIDTH, Mule.HEIGHT);
 		
 		Texture backgroundTexture = new Texture(Gdx.files.internal(BACKGROUND_LOCATION));
 		Sprite backgroundSprite = new Sprite(backgroundTexture);
 		SpriteDrawable backgroundDrawable = new SpriteDrawable(backgroundSprite);
 		Image backgroundImage = new Image(backgroundDrawable);
 		
 		background.addActor(backgroundImage);
 		
 		stage.addActor(background);
 	}
 	
 	@Override
 	public void render(float delta) {
 		if (token == null) {
 			setPlayer(); // hack-y fix. must be moved
 		}
 
 		if (game.gm.getRoundTime()<game.gm.getCurrentPlayerTime()){
 			game.setScreen(Mule.MAPSCREEN);
 			game.gm.nextPlayer();
 		}
 		stage.draw();	
 		SpriteBatch sb = new SpriteBatch();
 		sb.begin();
 		token.draw(sb, 1);
 		resourceGUI.draw(sb);
 		sb.end();
 		if(guiOverlay){
			storeGui.updateValues();
 			drawStoreGui(sb);
 		}
 		sb.dispose();
 		handleInput();
 		
 		game.gm.incrementCurrentPlayerTime(delta);
 
 		checkLocation();
 	}
 	
 	private void drawStoreGui(SpriteBatch batch){
 		storeGui.draw(batch);
 	}
 	
 	private void handleInput(){
 		//check for guiOverlay so player can't move while store is open
 		if(Gdx.input.isKeyPressed(Keys.RIGHT)||Gdx.input.isKeyPressed(Keys.D)){
 			if(!guiOverlay) token.moveRight();
 		} else if(Gdx.input.isKeyPressed(Keys.LEFT)||Gdx.input.isKeyPressed(Keys.A)){
 			if(!guiOverlay) token.moveLeft();
 		} else if(Gdx.input.isKeyPressed(Keys.UP)||Gdx.input.isKeyPressed(Keys.W)){
 			if(!guiOverlay) token.moveUp();
 		} else if(Gdx.input.isKeyPressed(Keys.DOWN)||Gdx.input.isKeyPressed(Keys.S)){
 			if(!guiOverlay) token.moveDown();
 		} else if(Gdx.input.isKeyPressed(Keys.ESCAPE)){
 			guiOverlay = false; //remove GUI store overlay
 		} 
 
 		if(guiOverlay){
 			//see if close button in gui overlay is clicked
 			if(storeGui.isClosed()){
 				guiOverlay = false;
 			} else if(Gdx.input.isButtonPressed(Input.Buttons.LEFT) && Gdx.input.justTouched()){
 				storeGui.mouseClicked(Gdx.input.getX(), Gdx.input.getY());
 			}
 		}
 	}
 	
 	private void checkLocation(){
 		if(token.getX() < 0 || token.getX() + token.getWidth() > Mule.WIDTH ||
 				token.getY() < 0 || token.getY() + token.getHeight() > Mule.HEIGHT-55){
 			token.setX(Mule.WIDTH / 2);
 			token.setY(Mule.HEIGHT / 2);
 			game.setScreen(Mule.MAPSCREEN);
 		}
 		
 		checkBuildingCollision();
 	}
 	
 	private void checkBuildingCollision(){
 		Rectangle tokenRect = token.getRect();
 		Rectangle pubRect = pub.getRect();
 		Rectangle storeRect = store.getRect();
 		if(pubRect.contains(tokenRect)){
 			token.setX(Mule.WIDTH / 2);
 			token.setY(Mule.HEIGHT / 2);
 			game.setScreen(Mule.MAPSCREEN);
 			game.gm.endTurnPub();
 		}
 		if(storeRect.contains(tokenRect)){
 			/* use store entered variable so that gui doesn't appear
 			 * again if it's closed and player token is still in store location
 			 */
 			if(!storeEntered){
 				guiOverlay = true;
 				storeEntered = true;
 				storeGui.resetItemFields();
 			}
 		} else{
 			storeEntered = false;
 		}
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		this.width = width;
 		this.height = height - 80;
 		realHeight = height;
 	}
 
 	@Override
 	public void show() {
 		if(resourceGUI == null) resourceGUI = new ResourceTracker();
 		token = new PlayerToken(Mule.pm.getCurrentPlayer(), Mule.WIDTH / 2, Mule.HEIGHT / 2);
 		storeEntered = false;
 		guiOverlay = false;
 	}
 
 	@Override
 	public void hide() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
