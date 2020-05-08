 package com.picotech.lightrunnerlibgdx;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.picotech.lightrunnerlibgdx.GameScreen.GameState;
 import com.picotech.lightrunnerlibgdx.Menu.MenuState;
 
 public class WorldRenderer {
 	public OrthographicCamera camera;
 	private SpriteBatch batch;
 	private World world;
	// private StatLogger2 statlogger;
 	private ShapeRenderer sr;
 	private int width, height;
 	public boolean terminate = false;
 
 	public WorldRenderer(World world) {
 		this.world = world;
 		width = Gdx.graphics.getWidth();
 		height = Gdx.graphics.getHeight();
 		camera = new OrthographicCamera(1, height / width);
 		batch = new SpriteBatch();
 		sr = new ShapeRenderer();
 		loadContent();
 	}
 
 	private void loadContent() {
 		world.loadContent();
 	}
 
 	public void render() {
 		if (GameScreen.state == GameState.PLAYING || GameScreen.state == GameState.MENU) {
 			if (GameScreen.state == GameState.PLAYING
 					|| (GameScreen.state == GameState.MENU && (world.menu.menuState == Menu.MenuState.MAIN
 							|| world.menu.menuState == Menu.MenuState.OPTIONS || world.menu.menuState == Menu.MenuState.GAMEOVER)))
 				if (!GameScreen.dialogBoxActive)
 					world.update();
 
 			world.draw(batch, sr);
 			if (world.menu.menuState != Menu.MenuState.GAMEOVER
 					&& world.player.alive == false) {
 				// ENDING THE GAME ON DEATH
 				StatLogger2.endGame(World.score, World.enemiesKilled,
 						(int) (World.totalTime + 0.5));
 
 				GameScreen.state = GameState.MENU;
 				world.menu.menuState = Menu.MenuState.GAMEOVER;
 
 				// to remove later
 				// terminate = true;
 			}
 		}
 		batch.begin();
 		if (GameScreen.state == GameState.LOADING) {
 			batch.draw(Assets.loadingScreen, 0, 0);
 		}
 		/*
 		 * state = GameState.MENU; world.menu.menuState = MenuState.STATISTICS;
 		 */
 
 		batch.end();
 		Assets.setTextScale(2f);
 	}
 }
