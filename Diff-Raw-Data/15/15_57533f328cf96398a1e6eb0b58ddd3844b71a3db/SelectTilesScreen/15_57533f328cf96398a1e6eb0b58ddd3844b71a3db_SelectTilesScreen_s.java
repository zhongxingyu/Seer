 package screens;
 
 import gameObjects.Map;
 import gameObjects.Tile;
 import managers.SelectTileManager;
 import renderers.MapRenderer;
 import renderers.PlayerSelectionGui;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.me.mygdxgame.Mule;
 
 /**
  * This screen will handle the displaying of the entire map and allow players to pick a tile for various purposes
  * including buying tiles.
  * @author antonio
  *
  */
 public class SelectTilesScreen implements Screen, InputProcessor{
 
 	private MapRenderer renderer;
 	
 	private Mule game;
 	
 	private Map map;
 	
 	private SelectTileManager manager;
 
 	private Tile[][] tiles;
 	
 	float ppuX, ppuY;
 	boolean firstRound = true;
 	
 	int selectionBoxX = 3;
 	int selectionBoxY = 3;
 
	private final int GUI_HEIGHT = 100;
 	
 	private PlayerSelectionGui playerGui;
 	
 	private boolean switchScreen;
 	
 	public SelectTilesScreen(Mule mule){
 		super();
 		map = Mule.gm.getMap();
 		tiles = null;
 		renderer = new MapRenderer(map);
 		renderer.setSize(Mule.WIDTH, Mule.HEIGHT);
 		map.setDrawPlayer(false);
 		game = mule;
 		manager = new SelectTileManager(game);
 		Gdx.input.setInputProcessor(this);
 		map.setPPU(Mule.WIDTH / 9, (Mule.HEIGHT - GUI_HEIGHT) / 5);
 		MapRenderer.setPPU(Mule.WIDTH / 9, (Mule.HEIGHT - GUI_HEIGHT) / 5);
 		playerGui = new PlayerSelectionGui(manager);
 		switchScreen = false;
 	}
 	
 	@Override
 	public void render(float delta) {
 		SpriteBatch sprite = new SpriteBatch();
 		sprite.begin();
 		
 		map.draw(sprite);
 		playerGui.draw(sprite);
 		
 		update(delta);
 	}
 	
 	public void update(float delta){
 		getInput();
 		if(manager.isDone()){
 			//if true make a mapscreen. If false just switch to the map screen
 			if(firstRound){
 				Mule.MAPSCREEN = new MapScreen(game);
 				Mule.MAPSCREEN.setMap(Mule.gm.getMap());
 				game.setScreen(Mule.MAPSCREEN);
 				firstRound = false;
 			} else {
 				game.setScreen(Mule.MAPSCREEN);
 			}
 		}
 	}
 	
 	private void getInput(){
 		if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
 			manager.buyTile(Gdx.input.getX(), Gdx.input.getY());
 			playerGui.mouseClicked(Gdx.input.getX(), Gdx.input.getY());
 		}
 	}
 
 		
 	@Override
 	public void resize(int width, int height) {
 		
 	}
 
 	@Override
 	public void show() {
 		map.setDrawPlayer(false);
 		playerGui = new PlayerSelectionGui(manager);
 		//manager = new SelectTileManager(game);
 		switchScreen = false;
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
 
 	@Override
 	public boolean keyDown(int keycode) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean keyTyped(char character) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchDragged(int screenX, int screenY, int pointer) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean mouseMoved(int screenX, int screenY) {
 		// TODO Auto-generated method stub
 		// System.out.println("x: " + screenX);
 		// System.out.println("y: " + screenY);
 		if (screenX > 0 && screenX < Mule.WIDTH - 1 && screenY > 0 && screenY < Mule.HEIGHT){
 			manager.highlightTile(screenX, screenY);
 		}
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int amount) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
