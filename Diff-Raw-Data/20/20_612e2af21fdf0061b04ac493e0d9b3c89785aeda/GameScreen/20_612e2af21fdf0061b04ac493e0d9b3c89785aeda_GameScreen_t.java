 package ch.zhaw.arsphema.screen;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ch.zhaw.arsphema.MyGdxGame;
 import ch.zhaw.arsphema.controller.HeroController;
 import ch.zhaw.arsphema.model.Background;
 import ch.zhaw.arsphema.model.Hero;
 import ch.zhaw.arsphema.model.NavigationOverlay;
 import ch.zhaw.arsphema.model.enemies.AbstractEnemy;
 import ch.zhaw.arsphema.model.enemies.EnemyFactory;
 import ch.zhaw.arsphema.model.shot.OverHeatBar;
 import ch.zhaw.arsphema.model.shot.Shot;
 import ch.zhaw.arsphema.services.Services;
 import ch.zhaw.arsphema.services.SoundManager;
 import ch.zhaw.arsphema.util.Paths;
 import ch.zhaw.arsphema.util.Sizes;
 import ch.zhaw.arsphema.util.Textures;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 
 public class GameScreen extends AbstractScreen {
 	
 	private float ppuX; // pixels per unit on the X axis
 	private float ppuY; // pixels per unit on the Y axis
 	private boolean showOverlay = true;
 	private Hero hero;
 	private HeroController controller;
 	private NavigationOverlay overlay;
 	private List<AbstractEnemy> enemies, killedEnemies;
 	private List<Shot> heroShots, enemyShots, shotsToRemove;
 	private EnemyFactory enemyFactory;
 	private float elapsed = 0;
 	private Background bg1,bg2;
 	private OverHeatBar overheatbar;
 	
 	public GameScreen(MyGdxGame game) {
 		super(game);
 	}
 
 	@Override
 	public void show() {
 		loadTextures();
 		controller = new HeroController(hero);
 		enemies = new ArrayList<AbstractEnemy>();
 		killedEnemies = new ArrayList<AbstractEnemy>();
 		heroShots = new ArrayList<Shot>();
 		enemyShots = new ArrayList<Shot>();
 		shotsToRemove = new ArrayList<Shot>();
 		enemyFactory = EnemyFactory.getInstance();
 		Gdx.input.setInputProcessor(controller);
 		Services.setSoundManager(new SoundManager());
 	}
 
 	private void loadTextures() {
 		hero = new Hero(5, Sizes.DEFAULT_WORLD_HEIGHT / 2 + Sizes.SHIP_HEIGHT / 2, Textures.HERO);
 		overlay = new NavigationOverlay(Textures.OVERLAY_SPRITE);
 		bg1 = new Background(new TextureRegion(Textures.BACKGROUND_STARS),0,0,Sizes.DEFAULT_WORLD_WIDTH,Sizes.DEFAULT_WORLD_HEIGHT);
 		bg2 = new Background(new TextureRegion(Textures.BACKGROUND_STARS),bg1.getWidth(),0,Sizes.DEFAULT_WORLD_WIDTH,Sizes.DEFAULT_WORLD_HEIGHT);
 		overheatbar = OverHeatBar.getInstance();
 		//TODO create one wide file for background and move with textureregion?
 	}
 
 	@Override
 	public void render(float delta) {
 		elapsed += delta;
 		drawGame(delta);
 		
 		//hero stuff
 		hero.move(delta);
 		heroShots.addAll(hero.shoot(delta));
 		heroSuffering();
 		killEnemies();
		enemies.addAll(enemyFactory.dropEnemy(delta, elapsed));
 		
 
 		computeEnemyMovements(delta);
 		
 		enemies.addAll(enemyFactory.dropEnemy(delta, elapsed));
 		controller.update(delta);
 		updateShots();
 	}
 
 	private void drawGame(float delta) {
 		Gdx.gl.glClearColor(0f, 0f, 0f, 0);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		batch.begin();
 		
 		bg1.draw(batch,delta, elapsed,ppuX,ppuY); // draw Background
 		bg2.draw(batch,delta, elapsed,ppuX,ppuY); // draw Background
 		hero.draw(batch,delta, elapsed,ppuX,ppuY);
 		overheatbar.draw(batch,delta, elapsed,ppuX,ppuY);
 
 		for(AbstractEnemy enemy : enemies)
 		{
 			enemy.move(delta);//TODO remove out of view enemies
 		}
 		for(AbstractEnemy enemy : enemies)
 		{
 			enemy.draw(batch, delta, elapsed, ppuX, ppuY);
 		}
 		drawShots(delta);
 		
 		// start overlay is displayed 5 sec
 		if(showOverlay)
 		{
 			// start overlay is displayed 5 sec
 			batch.draw(overlay.getTexture(NavigationOverlay.START), ppuX * overlay.x, ppuY * overlay.y, ppuX * overlay.width, ppuY * overlay.height);
 			if (elapsed >= 5)
 			{
 				enemyFactory.setDropEnemies(true);
 				showOverlay = false;
 			}
 		} else {
 			batch.draw(overlay.getTexture(NavigationOverlay.GAME), ppuX * overlay.x, ppuY * overlay.y, ppuX * overlay.width, ppuY * overlay.height);
 		}
 		batch.end();
 	}
 	
 	private void drawShots(float delta)
  {
 		for (Shot shot : heroShots) {
 			shot.draw(batch, delta, elapsed, ppuX, ppuY);
 			if (shot.shouldBeRemoved()) {
 				shotsToRemove.add(shot);
 			}
 
 		}
 		for (Shot shot : enemyShots) {
 			shot.draw(batch, delta, elapsed, ppuX, ppuY);
 			if (shot.shouldBeRemoved()) {
 				shotsToRemove.add(shot);
 			}
 
 		}
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		super.resize(width, height);
 		controller.resize(width, height);
 		ppuX = (float) width / Sizes.DEFAULT_WORLD_WIDTH;
 		ppuY = (float) height / Sizes.DEFAULT_WORLD_HEIGHT;
 	}
 
 	private void heroSuffering() {
 		for(Shot shot : enemyShots)
 		{
 			if(shot.overlaps(hero))
 			{
 				if(hero.lowerHealth(shot.getDamage())){
 					//TODO gameover screen... hero suffered too much :'(
 				}
 			}
 		}
 	}
 	
 	private void computeEnemyMovements(float delta) {
 		for(AbstractEnemy enemy : enemies)
 		{
 			if(enemy.move(delta))
 				killedEnemies.add(enemy);
 		}
 		killEnemies();
 		enemies.removeAll(killedEnemies);
 		killedEnemies.clear();
 	}
 	
 	private void killEnemies() {
 		for(Shot shot : heroShots)
 		{
 			for(AbstractEnemy enemy : enemies)
 			if(shot.overlaps(enemy))
 			{
 				if(enemy.lowerHealth(shot.getDamage())){
 					//TODO enemy is dead... loot him!!! (point berechnung)
 //					enemy.getBasePoints();
 					killedEnemies.add(enemy);
 				}
 				shotsToRemove.add(shot);
 			}
 		}
 		enemies.removeAll(killedEnemies);
 		killedEnemies.clear();
 	}
 
 	private void updateShots() {
 		heroShots.removeAll(shotsToRemove);
 		enemyShots.removeAll(shotsToRemove);
 		shotsToRemove.clear();		
 	}
 	
 
 	@Override
 	public void hide() {}
 
 	@Override
 	public void pause() {}
 
 	@Override
 	public void resume() {}
 
 }
