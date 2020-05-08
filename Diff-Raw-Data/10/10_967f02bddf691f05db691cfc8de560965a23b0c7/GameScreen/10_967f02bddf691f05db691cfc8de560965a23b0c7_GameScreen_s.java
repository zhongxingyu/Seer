 package fr.odai.zerozeroduck;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.utils.Array;
 
 import fr.odai.zerozeroduck.controller.MainController;
 import fr.odai.zerozeroduck.model.Trap;
 import fr.odai.zerozeroduck.model.World;
 import fr.odai.zerozeroduck.utils.StageInfo;
 
 public class GameScreen implements Screen, InputProcessor {
 	
 	private ZeroZeroDuck game;
 	
 	private static final Array<StageInfo> sgis = new Array<StageInfo>();
 	static {
 		// Stage 1
 		sgis.add(new StageInfo(0, "Stage0", 
 				new ArrayList<String>(Arrays.asList("images/Stage0-floor.png")), 
 				new ArrayList<Float>(Arrays.asList(0.f)), 
 				8.5f, 0,
 				15, 3, 1,
 				10, 3, 1));
 		sgis.get(0).addTrapInfo(0, "pepper", 3.f);
 		sgis.get(0).addTrapInfo(0, "salt", 5.f);
 		
 		// Stage 2
 		sgis.add(new StageInfo(1, "Stage1", 
 				new ArrayList<String>(Arrays.asList("images/Stage1-floor.png")), 
 				new ArrayList<Float>(Arrays.asList(0.f)), 
 				8.5f, 0,
 				20, 4, 2,
 				5, 1, 1));
 		sgis.get(1).addTrapInfo(0, "pepper", 3.f);
 		sgis.get(1).addTrapInfo(0, "salt", 6.f);
 		sgis.get(1).addTrapInfo(0, "bruleur", 4.5f);
 		
 		// Stage 3
 		sgis.add(new StageInfo(2, "Stage2", 
 				new ArrayList<String>(Arrays.asList("images/Stage2-floor.png")), 
 				new ArrayList<Float>(Arrays.asList(0.f)), 
 				8.5f, 0,
 				35, 5, 3,
 				5, 2, 2));
 
 		sgis.get(2).addTrapInfo(0, "salt", 2.5f);
 		sgis.get(2).addTrapInfo(0, "pepper", 3.5f);
 		sgis.get(2).addTrapInfo(0, "bruleur", 5.f);
 		sgis.get(2).addTrapInfo(0, "salt", 7.f);
 		
 		// Stage 4		
 		sgis.add(new StageInfo(3, "Stage3", 
 				new ArrayList<String>(Arrays.asList("images/Stage3-floor0.png", "images/Stage3-floor1.png")), 
 				new ArrayList<Float>(Arrays.asList(0.f, 0.f)), 
 				8.5f, 0,
 				35, 5, 3,
 				5, 2, 1));
 		
 		sgis.get(3).addTrapInfo(1, "bruleur", 2.f);
 		sgis.get(3).addTrapInfo(1, "salt", 5.f);
 		sgis.get(3).addTrapInfo(0, "pepper", 3.f);
 		sgis.get(3).addTrapInfo(0, "salt", 4.5f);
 		
 		// Stage 5		
 		sgis.add(new StageInfo(4, "Stage4", 
 				new ArrayList<String>(Arrays.asList("images/Stage4-floor0.png", "images/Stage4-floor1.png")), 
 				new ArrayList<Float>(Arrays.asList(0.f, 0.f)), 
 				8.5f, 0,
 				40, 5, 3,
 				15, 2, 1));
 		
 		sgis.get(4).addTrapInfo(1, "bruleur", 2.f);
 		sgis.get(4).addTrapInfo(1, "salt", 5.f);
 		sgis.get(4).addTrapInfo(0, "pepper", 3.f);
 		sgis.get(4).addTrapInfo(0, "salt", 4.5f);
 		
 		// Stage 6		
 		sgis.add(new StageInfo(5, "Stage5", 
 				new ArrayList<String>(Arrays.asList("images/Stage5-floor0.png", "images/Stage5-floor1.png")), 
 				new ArrayList<Float>(Arrays.asList(0.f, 0.f)), 
 				8.5f, 0,
 				40, 5, 3,
 				15, 2, 1));
 		
 		sgis.get(5).addTrapInfo(1, "bruleur", 2.f);
 		sgis.get(5).addTrapInfo(1, "salt", 5.f);
 		sgis.get(5).addTrapInfo(0, "pepper", 3.f);
 		sgis.get(5).addTrapInfo(0, "salt", 4.5f);
 	}
 	
 	private World world;
 	private WorldRenderer renderer;
 	private MainController controller;
 	
 	float animationTime=1.f;
 	
 	float posY=0.f;
 
 	public GameScreen(ZeroZeroDuck game) {
 		super();
 		this.game = game;
 	}
 	
 	public void gameOver(){
 		renderer.stopMusic();
		renderer.dispose();
 		game.setScreen(game.gameOverScreen);
 	}
 	
 	public void gameWin(){
 		renderer.stopMusic();
		renderer.dispose();
 		game.setScreen(game.gameWinScreen);
 	}
 
 	@Override
 	public void render(float delta) {
 		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		controller.update(delta);
 		renderer.render(delta);
 	}
 
 	@Override
 	public void show() {
 		world = new World(sgis.get(game.level));
 		renderer = new WorldRenderer(world, false);
 		controller = new MainController(world, this);
 		Gdx.input.setInputProcessor(this);
 	}
 	
 	@Override
 	public void resize(int width, int height) {
 		renderer.setSize(width, height);
 	}
 	
 
 	@Override
 	public void hide() {
 		Gdx.input.setInputProcessor(null);
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
 		renderer.stopMusic();
		renderer.dispose();
 		Gdx.input.setInputProcessor(null);
 	}
 
 	@Override
 	public boolean keyDown(int keycode) {
 		if (keycode == Keys.SPACE)
 			controller.patatePressed();
 		if (keycode == Keys.ENTER)
 			controller.killallPressed();
 		if (keycode == Keys.S)
 			controller.trapPressed(MainController.Keys.TRAP_S);
 		if (keycode == Keys.F)
 			controller.trapPressed(MainController.Keys.TRAP_F);
 		if (keycode == Keys.H)
 			controller.trapPressed(MainController.Keys.TRAP_H);
 		if (keycode == Keys.K)
 			controller.trapPressed(MainController.Keys.TRAP_K);
 		return false;
 	}
 
 	@Override
 	public boolean keyUp(int keycode){
 		if (keycode == Keys.SPACE)
 			controller.patateReleased();
 		if (keycode == Keys.ENTER)
 			controller.killallReleased();
 		if (keycode == Keys.S)
 			controller.trapReleased(MainController.Keys.TRAP_S);
 		if (keycode == Keys.F)
 			controller.trapReleased(MainController.Keys.TRAP_F);
 		if (keycode == Keys.H)
 			controller.trapReleased(MainController.Keys.TRAP_H);
 		if (keycode == Keys.K)
 			controller.trapReleased(MainController.Keys.TRAP_K);
 		return false;
 	}
 	
 	@Override
 	public boolean touchDown(int x, int y, int pointer, int button) {
 		for(Trap trap : world.getTraps()){
 			if(trap.click(renderer.convertScaleX(x),7-renderer.convertScaleY(y))){
 				controller.trapPressed(trap.getAssociatedKey());
 				return true;
 			}
 		}
 		controller.patatePressed();
 		return true;
 	}
 
 	@Override
 	public boolean touchUp(int x, int y, int pointer, int button) {
 		for(Trap trap : world.getTraps()){
 			if(trap.click(renderer.convertScaleX(x),7-renderer.convertScaleY(y))){
 				controller.trapReleased(trap.getAssociatedKey());
 				return true;
 			}
 		}
 		controller.patateReleased();
 		return true;
 	}
 
 	@Override
 	public boolean keyTyped(char character) {
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
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int amount) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
