 package com.me.mygdxgame;
 
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.InputListener;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.actions.Actions;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 
 public class MyGdxGame implements ApplicationListener {
 	
 	boolean fullScreen = false; // Full screen yes or no.
 	boolean printDebug = true; // Print debug, add or remove writes in end of render.
 
 	ReplayHandler replayHandler = new ReplayHandler();
 	boolean saveReplay = true;
 	boolean useReplay = false;
 	String replayPath = "/AdaptiveTD/Replays/testReplay.txt";			// Must be external, relative to user directory.
 	String replaySavePath = "/AdaptiveTD/Replays/testReplay.txt";
 	
 	boolean savedParameters = false;
 	String parameterSavePath = "/AdaptiveTD/Parameters/parameters.txt";
 	
 	boolean paused = true;
 	boolean resuming = true;
 	
 	private float pauseTime = GameConstants.startTime;
 	
 	private static final int VIRTUAL_WIDTH = 1280;
     private static final int VIRTUAL_HEIGHT = 768;
     private static final float ASPECT_RATIO =
         (float)VIRTUAL_WIDTH/(float)VIRTUAL_HEIGHT;
     private Rectangle viewport;
 	
 	HashMap<Float, Enemy> enemyWave = new HashMap<Float, Enemy>();
     List<Float> waveTime = new ArrayList<Float>();
 	
 	List<Enemy> enemies = new ArrayList<Enemy>();
 	static List<Tower> towers = new ArrayList<Tower>();
 	
 	List<Missile> missiles = new ArrayList<Missile>();
 	Enemy focusFireEnemy;
 	
 	static Tower selectedTower;
 
 	static HashMap<String, TowerStats> towerInfo = new HashMap<String, TowerStats>();
     static HashMap<String, EnemyStats> enemyInfo = new HashMap<String, EnemyStats>();
     static HashMap<String, Sound> sounds = new HashMap<String, Sound>();
 
 	List<String> towerKeys = new ArrayList<String>();
 	
 	Stage stage;
 	ExtendedActor actor;
 	
 	Map map;	
 	TextureAtlas mapTilesAtlas;	// Map tiles
 	TextureAtlas enemiesAtlas;	// Enemies
 	TextureAtlas miscAtlas;		// Various small stuff like bullets, health bar, sell and upgrade buttons
 	TextureAtlas towersAtlas;	// Towers
 	
 	SpriteBatch spriteBatch;
 	
 	BitmapFont font;
 	
 	Sprite buildingTowerSprite = null;
 	
 	boolean building = false, wasTouched = false, won = false, lost = false;
 	
 	float totalTime = 0;
 	
 	double timer = 0;
 	int uC=0, uT=0;
 	
 	Vector2 touchedTile = new Vector2(0,0);
 	
 	String buildingTower = "", towerName = "Tower";
 	
 	Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.BLACK);
 
 	static Label yellowBoxLabel;
 
 	
 	Camera gameCamera;
 
 	ExtendedActor temporaryTowerActor = null;
 	
 	static int livesLeft, currentGold;
 	EventHandler eventHandler = new EventHandler();
 	
 	FeedbackTextInput listener = new FeedbackTextInput();
 	TextButton livesButton, goldButton;
 	
 	ExtendedActor yellowBox;
 	Group yellowBoxGroup;
 	int yellowBoxYPadding = 10;
 	int yellowBoxXPadding = 15;
 	
 	ThinkTank thinkTank = new ThinkTank();
 	
 	Questionaire questionaire;
 	
 	@Override
 	public void create() {
 		gameCamera = new OrthographicCamera(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
 		Gdx.graphics.setTitle("Adaptive Tower Defense v0.001");
 		spriteBatch = new SpriteBatch();
 		
 		font = new BitmapFont(Gdx.files.internal("Fonts/KatyBerry.fnt"), Gdx.files.internal("Fonts/KatyBerry_0.tga"), false);
 		
 		stage = new Stage();
 		stage.setCamera(gameCamera);
 		Gdx.input.setInputProcessor(stage);
 		
 		mapTilesAtlas = new TextureAtlas(Gdx.files.internal("Images/mapTiles.atlas"));
 		enemiesAtlas = new TextureAtlas(Gdx.files.internal("Images/enemies.atlas"));
 		miscAtlas = new TextureAtlas(Gdx.files.internal("Images/misc.atlas"));
 		towersAtlas = new TextureAtlas(Gdx.files.internal("Images/towers.atlas"));
 		
 		map = new Map(mapTilesAtlas);
 		FileHandle handle = Gdx.files.internal("Maps/map.txt");
 		Group g = map.loadMap(handle);
 		stage.addActor(g);
 		
 		FileHandle towerHandle = Gdx.files.internal("Stats/towerStats.txt");
 		try {
 			towerInfo = StatsFetcher.loadTowerStats(towerHandle);
 		} catch (NumberFormatException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		FileHandle enemyHandle = Gdx.files.internal("Stats/enemyStats.txt");
 		enemyInfo = StatsFetcher.generateEnemyInfo(enemyHandle);
 
 		createWave();
 		
 		livesLeft = GameConstants.startLives;
 		currentGold = GameConstants.startGold;
 
 		
 		// UI Creation
 		createUI();
 		// -----------
 		
 		if(useReplay)
 		{
 			FileHandle replayHandle = Gdx.files.external(replayPath);
 			replayHandler.loadReplay(replayHandle);
 		}
 
         Gdx.gl.glClearColor(Color.GRAY.r, Color.GRAY.g, Color.GRAY.b, Color.GRAY.a);
         
         loadSounds();
         
 	}
 
 	@Override
 	public void dispose() {
         stage.dispose();
 	}
 
 	@Override
 	public void render() {	
 
     // clear previous frame
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		if (won || lost)
 		{
			if (questionaire != null && Questionaire.happy > 0 && Questionaire.difficult > 0)
	        	thinkTank.calculateResults(Questionaire.happy, Questionaire.difficult);
 		}
 		if(!paused)
 		{
 			updateGame();
 			// Draws game
 	        stage.draw();
 		}
 		else if(resuming && !won && !lost)
 		{
			totalTime += Gdx.graphics.getDeltaTime();
			
 			if(!useReplay)
 			{
 				handleInput();
 				eventHandler.update();
 			}
 			else{
 				eventHandler.events = replayHandler.playReplay(totalTime);
 			}
 			if(saveReplay)
 			{
 				replayHandler.addEvents(totalTime, eventHandler);
 			}
 			thinkTank.updateParameters(0, currentGold, livesLeft, towers, eventHandler.events, towerInfo);
 			handleEvents();
 			updateYellowBoxPosition();
 			// Draws game
 	        stage.draw();
 	        
 			pauseTime -= Gdx.graphics.getDeltaTime();
 			spriteBatch.begin();
         	font.setScale(10);
         	if(!Integer.toString((int)Math.ceil(pauseTime)).equals("0"))
         		font.draw(spriteBatch, Integer.toString((int)Math.ceil(pauseTime)), GameConstants.screenWidth/2 - 32, GameConstants.screenHeight/2);
         	font.setScale(1);
         	spriteBatch.end();
 			if(pauseTime <= 0)
 				paused = false;
 		}
 
      
         // Fps counter
         timer += Gdx.graphics.getDeltaTime();
         uC++;
         if(timer >= 1)
         {
         	//System.out.println("FPS: " + uC);
         	uC = 0;
         	timer = 0;
         }
         
         if (won)
         {
         	spriteBatch.begin();
         	font.setScale(10);
         	font.draw(spriteBatch, "Game won", GameConstants.screenWidth/2 - 300, GameConstants.screenHeight/2);
         	//questionaire.draw(spriteBatch);
         	font.setScale(1);
         	spriteBatch.end();
         }
         else if (lost)
         {
         	spriteBatch.begin();
         	font.setScale(10);
         	font.draw(spriteBatch, "Game lost", GameConstants.screenWidth/2 - 300, GameConstants.screenHeight/2);
         	//questionaire.draw(spriteBatch);
         	font.setScale(1);
         	spriteBatch.end();
         }
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		
         float aspectRatio = (float)width/(float)height;
         float scale = 1f;
         Vector2 crop = new Vector2(0f, 0f);
         
         if(aspectRatio > ASPECT_RATIO)
         {
             scale = (float)height/(float)VIRTUAL_HEIGHT;
             crop.x = (width - VIRTUAL_WIDTH*scale)/2f;
         }
         else if(aspectRatio < ASPECT_RATIO)
         {
             scale = (float)width/(float)VIRTUAL_WIDTH;
             crop.y = (height - VIRTUAL_HEIGHT*scale)/2f;
         }
         else
         {
             scale = (float)width/(float)VIRTUAL_WIDTH;
         }
  
         float w = (float)VIRTUAL_WIDTH*scale;
         float h = (float)VIRTUAL_HEIGHT*scale;
 
 		Gdx.graphics.setDisplayMode((int)w, (int)h, fullScreen);
 		
         viewport = new Rectangle(crop.x, crop.y, w, h);
 
         Gdx.gl.glViewport((int) viewport.x, (int) viewport.y,
                           (int) viewport.width, (int) viewport.height);
     		
 		gameCamera.position.set(gameCamera.viewportWidth/2,gameCamera.viewportHeight/2,0);
 		
 		gameCamera.update();
 		gameCamera.apply(Gdx.gl10);
     
 	}
 
 	private void updateGame()
 	{
 		totalTime += Gdx.graphics.getDeltaTime();
         
 		if(!useReplay)
 		{
 			handleInput();
 			eventHandler.update();
 		}
 		else{
 			eventHandler.events = replayHandler.playReplay(totalTime);
 		}
 		if(saveReplay)
 		{
 			replayHandler.addEvents(totalTime, eventHandler);
 		}
 		thinkTank.updateParameters(totalTime, currentGold, livesLeft, towers, eventHandler.events, towerInfo);
 		handleEvents();
 		updateYellowBoxPosition();
         checkWave(totalTime);
         if(enemies.size() > 0)
         {
         	for(int i = 0; i < towers.size(); i++)
         	{
         		towers.get(i).calculateTarget(Gdx.graphics.getDeltaTime(), enemies, null);
         		if(towers.get(i).canShoot)
         		{
         			Missile m = towers.get(i).shoot();
         			if(m != null)
         			{
         				sounds.get(towers.get(i).towerStats.shootSound).play();
         				stage.addActor(m);
         				missiles.add(m);
         				m.setZIndex(towers.get(i).getZIndex()-1);
         			}
         		}
         	}
 		}
 		for (int i = 0; i < missiles.size(); i++)
 		{
 			missiles.get(i).timeToHitTarget -= Gdx.graphics.getDeltaTime();
 			if (missiles.get(i).timeToHitTarget <= 0)
 			{
 				// If TargetStrategy.Single
 				if (missiles.get(i).effect.missileTarget.targetingStrategy == TargetingStrategy.Single)
 				{
 					Enemy targEnemy = ((TargetSingle) (missiles.get(i).effect.missileTarget)).targetEnemy;
 					Iterator<String> it = missiles.get(i).effect.effects
 							.keySet().iterator();
 					while (it.hasNext())
 					{
 						String s = it.next();
 						if (missiles.get(i).effect.effects.get(s).b)
 						{
 							targEnemy.setStat(s,
 									missiles.get(i).effect.effects.get(s).f);
 						} else
 						{
 							targEnemy.editStat(s,
 									missiles.get(i).effect.effects.get(s).f);
 						}
 					}
 					sounds.get(missiles.get(i).impactSound).play();
 				}
 				// ----------------------
 				// If TargetStrategy.Circle
 				else if (missiles.get(i).effect.missileTarget.targetingStrategy == TargetingStrategy.Circle)
 				{
 					TargetCircle targetCircle = (TargetCircle) missiles.get(i).effect.missileTarget;
 					List<Enemy> enemiesInCircle = HitDetector
 							.getEnemiesInCircle(enemies, targetCircle.x1,
 									targetCircle.y1, targetCircle.radius);
 					Iterator<String> it = missiles.get(i).effect.effects
 							.keySet().iterator();
 					while (it.hasNext())
 					{
 						String s = it.next();
 						for (int eT = 0; eT < enemiesInCircle.size(); eT++)
 						{
 							if (missiles.get(i).effect.effects.get(s).b)
 							{
 								enemiesInCircle.get(eT)
 										.setStat(
 												s,
 												missiles.get(i).effect.effects
 														.get(s).f);
 							} else
 							{
 								enemiesInCircle.get(eT)
 										.editStat(
 												s,
 												missiles.get(i).effect.effects
 														.get(s).f);
 							}
 						}
 					}
 					final ExtendedActor visualEffectActor = new ExtendedActor(
 							missiles.get(i).sprite);
 					TargetCircle tC = (TargetCircle) (missiles.get(i).effect.missileTarget);
 					visualEffectActor.setPosition(tC.x1 - tC.radius, tC.y1
 							- tC.radius);
 					visualEffectActor.setWidth(tC.radius * 2);
 					visualEffectActor.setHeight(tC.radius * 2);
 					visualEffectActor.addAction(sequence(Actions.fadeOut(0.5f),
 							run(new Runnable()
 							{
 								public void run()
 								{
 									visualEffectActor.remove();
 								}
 							})));
 					stage.addActor(visualEffectActor);
 					sounds.get(missiles.get(i).impactSound).play();
 					
 				}
 				// ----------------------
 				// If TargetStrategy.CircleOnSelf
 				else if (missiles.get(i).effect.missileTarget.targetingStrategy == TargetingStrategy.CircleOnSelf) {
 					TargetCircleOnSelf targetCircle = (TargetCircleOnSelf)missiles.get(i).effect.missileTarget;
 					List<Enemy> enemiesInCircle = HitDetector.getEnemiesInCircle(enemies, (int)(missiles.get(i).getX()+missiles.get(i).getWidth()/2), (int)(missiles.get(i).getY()+missiles.get(i).getHeight()/2), targetCircle.radius);
 					Iterator<String> it = missiles.get(i).effect.effects
 							.keySet().iterator();
 					while (it.hasNext()) {
 						String s = it.next();
 						for(int eT = 0; eT < enemiesInCircle.size(); eT++)
 						{
 							if(missiles.get(i).effect.effects.get(s).b)
 							{
 								enemiesInCircle.get(eT).setStat(s,
 										missiles.get(i).effect.effects.get(s).f);
 							}
 							else
 							{
 								enemiesInCircle.get(eT).editStat(s,
 										missiles.get(i).effect.effects.get(s).f);
 							}
 						}
 					}
 				}
 				// ----------------------
 				// If TargetStrategy.Line
 				else if (missiles.get(i).effect.missileTarget.targetingStrategy == TargetingStrategy.Line) {
 					TargetLine targetLine = (TargetLine)missiles.get(i).effect.missileTarget;
 					List<Enemy> enemiesInLine = HitDetector.getEnemiesOnLine(enemies, new Vector2(targetLine.x1, targetLine.y1), new Vector2(targetLine.x2, targetLine.y2));
 					Iterator<String> it = missiles.get(i).effect.effects
 							.keySet().iterator();
 					while (it.hasNext()) {
 						String s = it.next();
 						for(int eT = 0; eT < enemiesInLine.size(); eT++)
 						{
 							if(missiles.get(i).effect.effects.get(s).b)
 							{
 								enemiesInLine.get(eT).setStat(s,
 										missiles.get(i).effect.effects.get(s).f);
 							}
 							else
 							{
 								enemiesInLine.get(eT).editStat(s,
 										missiles.get(i).effect.effects.get(s).f);
 							}
 						}
 					}
 				}
 				// ----------------------
 				
 				
 				missiles.get(i).remove();
 				missiles.remove(i);
 				i--;
 			}
 			// Complete Missile effects and damages.
 
 		}
 		
         stage.act(Gdx.graphics.getDeltaTime());
         
         for (int counter = 0; counter < enemies.size(); counter++)
         {
         	Enemy enemy = enemies.get(counter);
         	if (enemy.getStat("currentHealth") <= 0)
         	{
         		enemies.remove(enemy);
         		currentGold += enemy.getStat("currentGoldYield");
         		goldButton.setText("        " + currentGold);
         		enemy.remove();
         		counter --;
         	}
         	else if (enemy.getActions().size == 0)
         	{
         		enemies.remove(enemy);
         		enemy.remove();
         		livesLeft --;
         		livesButton.setText("" + livesLeft);
         		counter --;
         	}
         }
         checkWinConditions();
         
 	}
 	
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void resume() {
 	}
 	
 	private void resetGame()
 	{
 		if(saveReplay && !useReplay)
 		{
 			FileHandle saveHandle = Gdx.files.external(replaySavePath);
 			replayHandler.saveReplay(saveHandle);
 		}
 		replayHandler.events.clear();
 		replayHandler.savingEvents.clear();
 		useReplay = false;
 		
 		stage.getActors().clear();
 		
 		towers.clear();
 		enemies.clear();
 		enemyWave.clear();
 		waveTime.clear();
 		towerKeys.clear();
 		totalTime = 0;
 		
 		map = new Map(mapTilesAtlas);
 		FileHandle handle = Gdx.files.internal("Maps/map.txt");
 		Group g = map.loadMap(handle);
 		stage.addActor(g);
 		createWave();
 		createUI();
 		
 		currentGold = GameConstants.startGold;
 		goldButton.setText("        " + currentGold);
 		livesLeft = GameConstants.startLives;
 		livesButton.setText("" + livesLeft);
 		
 		won = false;
 		lost = false;
 		
 		thinkTank.clear();
 		
 		pauseTime = GameConstants.startTime;
 		paused = true;
 		resuming = true;
 		
		if (questionaire != null)
		{
			questionaire.reset();
			questionaire = null;
		}
		
 	}
 	
 	private void checkWave(float totalTime)
 	{
 		if(waveTime.size() > 0)
 		{
 			if (waveTime.get(0) <= totalTime) {
 				stage.addActor(enemyWave.get(waveTime.get(0)));
 				enemies.add(enemyWave.get(waveTime.get(0)));
 				enemyWave.remove(waveTime.get(0));
 				waveTime.remove(0);
 			}
 		}
 	}
 	
 	private void createWave()
 	{
 		/*
 		addEnemyToWave(0.5f, createEnemy("basic"));
 		addEnemyToWave(10.5f, createEnemy("fast"));
 		addEnemyToWave(5.5f, createEnemy("tough"));
 		addEnemyToWave(1.5f, createEnemy("basic"));
 		addEnemyToWave(2.5f, createEnemy("fast"));
 		addEnemyToWave(3.5f, createEnemy("tough"));
 		addEnemyToWave(4.5f, createEnemy("basic"));
 		addEnemyToWave(6.5f, createEnemy("fast"));
 		addEnemyToWave(7.5f, createEnemy("tough"));
 		addEnemyToWave(8.5f, createEnemy("basic"));
 		addEnemyToWave(11.5f, createEnemy("tough"));
 		*/
 		addEnemyToWave(0.1f, createEnemy("fast"));
 		Collections.sort(waveTime);
 	}
 	
 	private void addEnemyToWave(float time, Enemy enemy)
 	{
 		enemyWave.put(time, enemy);
 		waveTime.add(time);
 	}
 	
 	private Enemy createEnemy(String type)
 	{
 		return new Enemy(enemyInfo.get(type), map.startPoint, map.directions, enemiesAtlas.createSprite(enemyInfo.get(type).enemyTexture), miscAtlas.createSprite("healthBarRed"), miscAtlas.createSprite("healthBarYellow"));
 	}
 	
 	public void buildTower(String type, Vector2 tilePosition)
 	{
 		eventHandler.queueEvent(new Event("build", (int)tilePosition.x, (int)tilePosition.y, type));
 	}
 	
 	// Eventually take a towerInfo id, and create appropriate.
 	private Tower createTower(String type, Vector2 tilePosition)
 	{
 		Tower t = new Tower(towerInfo.get(type), towersAtlas.createSprite(towerInfo.get(type).towerTexture), miscAtlas.createSprite(towerInfo.get(type).missileTexture));
 		t.setPosition(tilePosition.x * GameConstants.tileSize, tilePosition.y
 				* GameConstants.tileSize);
 		return t;
 	}
 	
 	private void selectTower(Tower t)
 	{
 		if (t != null)
 		{
 			List<String> textForBox = new ArrayList<String>();
 			selectedTower = t;
 			textForBox.add(t.towerStats.type);
 			//nameLabel.setText(t.towerStats.type);
 
 			textForBox.add(t.towerStats.description);
 			//buildCostLabel.setText(t.towerStats.description);
 			
 			
 			if (selectedTower.towerStats.upgradesTo.equals("null"))
 			{
 				textForBox.add("Upgrade: MAX");
 				//upgradeCostLabel.setText("Upgrade: MAX");
 			}
 			else
 			{
 				int upgradeCost = towerInfo.get(selectedTower.towerStats.upgradesTo).buildCost
 						- selectedTower.towerStats.buildCost;
 				textForBox.add("Upgrade: " + upgradeCost);
 				//upgradeCostLabel.setText("Upgrade: " + upgradeCost);
 			}
 			textForBox.add("Sell: " + selectedTower.towerStats.sellPrice);
 			//uiLabelSellPrice.setText("Sell: " + selectedTower.towerStats.sellPrice);
 			this.fadeInYellowBox(t, textForBox);
 		}
 		else
 		{
 			selectedTower = null;
 			yellowBoxLabel.setText("");
 			this.fadeOutYellowBox();
 		}
 	}
 	
 	private void selectEnemy(Enemy e)
 	{	
 		List<String> textForBox = new ArrayList<String>();
 		
 		textForBox.add(e.enemyStats.type);
 		textForBox.add("Health: " + e.getStat("currentHealth"));
 		textForBox.add("Yields: " + e.getStat("currentGoldYield"));
 		fadeInYellowBox(e, textForBox);
 	}
 	
 	private void handleEvents()
 	{
 		for(int i = 0; i<eventHandler.events.size(); i++)
 		{
 			Event e = eventHandler.events.get(i);
 			if(e.eventType.equals("build"))
 			{
 				if(map.canBuild((int)e.x, (int)e.y))
 				{
 					
 					Tower t = createTower(e.tower, new Vector2(e.x, e.y));
 					int buildCost = towerInfo.get(e.tower).buildCost;
 					boolean canAfford = currentGold >= buildCost ? true : false;
 
 					boolean canBuild = canAfford;
 					if(canAfford)
 					{
 						for(int c = 0; c < towers.size(); c++)
 						{
 							if(towers.get(c).getX() == t.getX() && towers.get(c).getY() == t.getY())
 								canBuild = false;
 						}
 					}
 					if(canBuild)
 					{
 						
 						currentGold -= buildCost;
 						goldButton.setText("        " + currentGold);
 						stage.addActor(t);
 						towers.add(t);
 						selectTower(t);
 					}
 					else
 					{
 						t = null;
 					}
 				}
 			}
 			else if(e.eventType.equals("sell"))
 			{
 				
 			}
 			else if(e.eventType.equals("upgrade"))
 			{
 				for(int u = 0; u < towers.size(); u++)
 				{
 					if(e.x == (int)(towers.get(u).getX()/GameConstants.tileSize) && e.y == (int)(towers.get(u).getY()/GameConstants.tileSize))
 					{
 						if(!towers.get(u).towerStats.upgradesTo.equals("null"))
 						{
 							TowerStats newTowerStats = towerInfo.get(towers.get(u).towerStats.upgradesTo);
 							towers.get(u).upgrade(newTowerStats, towersAtlas.createSprite(newTowerStats.towerTexture), miscAtlas.createSprite(newTowerStats.missileTexture));
 							selectTower(towers.get(u));
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	private void handleInput()
 	{	
 		Vector2 input = new Vector2(Gdx.input.getX(), Gdx.input.getY());
 		input = stage.screenToStageCoordinates(input);
 		Actor a = stage.hit(input.x, input.y, false);
 		if(a != null && a.getClass() == MapTile.class)
 		{
 			touchedTile = new Vector2((float)Math.floor(a.getX()/64), (float)Math.floor(a.getY()/64)); 
 		}
 		if(wasTouched && !Gdx.input.isTouched())
 		{
 			if(building && temporaryTowerActor != null)
 			{
 				if(touchedTile.x<=20 && touchedTile.y<=10 && a!= null && a.getClass() == MapTile.class)
 					buildTower(buildingTower, touchedTile);
 				building = false;
 				temporaryTowerActor.remove();
 				temporaryTowerActor = null;
 			}
 			wasTouched = false;
 		}
 		if(Gdx.input.isTouched())
 		{
 			if(temporaryTowerActor != null && a != null)
 			{
 				temporaryTowerActor.setPosition(a.getX(), a.getY());
 			}
 			wasTouched = true;
 			Actor hit = stage.hit(Gdx.input.getX(), GameConstants.screenHeight-Gdx.input.getY(), false);
 			if(hit != null && hit.getClass() == Tower.class)
 			{
 				Tower t = (Tower)hit;
 				selectTower(t);
 			}
 			else if(hit != null && hit.getClass() == Enemy.class)
 			{
 				Enemy e = (Enemy)hit;
 				selectEnemy(e);
 			}
 			else if(Gdx.input.justTouched())
 			{
 				fadeOutYellowBox();
 			}
 		}
 		else if(temporaryTowerActor != null)
 		{
 			temporaryTowerActor.remove();
 			temporaryTowerActor = null;
 		}
 	}
 
 	ExtendedActor targetYellowBoxActor = null;
 	private void updateYellowBoxPosition()
 	{
 		if(targetYellowBoxActor != null)
 		{
 			float x = targetYellowBoxActor.getX() + targetYellowBoxActor.getWidth()/2 - yellowBox.getWidth()/2;
 			float y = targetYellowBoxActor.getY() + targetYellowBoxActor.getHeight();
 			if(y >= GameConstants.screenHeight - yellowBox.getHeight())
 			{
 				y = targetYellowBoxActor.getY() - yellowBox.getHeight();
 			}
 			yellowBox.setPosition(x, y);
 			yellowBoxLabel.setPosition(x+yellowBoxXPadding, y + (yellowBox.getHeight() - yellowBoxLines*font.getBounds(yellowBoxLabel.getText()).height));
 		}
 	}
 	
 	int yellowBoxLines = 0;
 	private void fadeInYellowBox(ExtendedActor targetActor, List<String> strings)
 	{
 		yellowBoxLines = strings.size();
 		int height = 2*yellowBoxYPadding;
 		int width = 0;
 		yellowBoxLabel.setText("");
 		for(String s : strings)
 		{
 			height += 2*(int)font.getBounds(s).height;
 			yellowBoxLabel.setText(yellowBoxLabel.getText() + s + "\n");
 			if(font.getBounds(s).width > width)
 				width = (int)font.getBounds(s).width;
 		}
 		width += 2*yellowBoxXPadding;
 		yellowBox.setSize(width, height);
 		targetYellowBoxActor = targetActor;
 		yellowBoxGroup.setVisible(true);
 		yellowBoxGroup.setColor(0, 0, 0, 60);
 		yellowBoxGroup.setZIndex(1000);		// Random high value, to keep it above anything.
 	}
 	
 	private void fadeOutYellowBox()
 	{
 		targetYellowBoxActor = null;
 		yellowBoxGroup.setVisible(false);
 	}
 	
 	private void createUI()
 	{
 		System.out.println("Generating UI");
 
 		yellowBoxGroup = new Group();
 		yellowBox = new ExtendedActor(miscAtlas.createSprite("YellowBox"));
 		yellowBoxGroup.addActor(yellowBox);
 		
 		towerKeys.addAll(towerInfo.keySet());
 		
 		Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.BLACK);
 		yellowBoxLabel = new Label("", labelStyle);
 		yellowBoxLabel.setPosition(800, GameConstants.screenHeight-40);
 		yellowBoxGroup.addActor(yellowBoxLabel);
 
 		yellowBoxGroup.setVisible(false);
 		
 		stage.addActor(yellowBoxGroup);
 		
 		TextButton.TextButtonStyle settingsButtonStyle = new TextButton.TextButtonStyle();
 		TextureRegion upStyle = new TextureRegion(miscAtlas.createSprite("settingsButton"));
 		TextureRegion downStyle = new TextureRegion(miscAtlas.createSprite("settingsButton"));
 		settingsButtonStyle.font = font;
 		settingsButtonStyle.up = new TextureRegionDrawable(upStyle);
 		settingsButtonStyle.down = new TextureRegionDrawable(downStyle);
 		TextButton settingsButton = new TextButton("", settingsButtonStyle);
 		settingsButton.addListener(new InputListener() {
 			public boolean touchDown(InputEvent event, float x, float y,
 					int pointer, int button) {
 				resetGame();
 				//paused = !paused;
 				return true;
 			}
 		});
 		settingsButton.setPosition(GameConstants.screenWidth - 2*GameConstants.tileSize, GameConstants.screenHeight-100);
 		stage.addActor(settingsButton);
 		
 		
 		TextButton.TextButtonStyle sellButtonSTyle = new TextButton.TextButtonStyle();
 		TextureRegion upStyleSell = new TextureRegion(miscAtlas.createSprite("sellTowerButton"));
 		TextureRegion downStyleSell = new TextureRegion(miscAtlas.createSprite("sellTowerButton"));
 		sellButtonSTyle.font = font;
 		sellButtonSTyle.up = new TextureRegionDrawable(upStyleSell);
 		sellButtonSTyle.down = new TextureRegionDrawable(downStyleSell);
 		TextButton sellButton = new TextButton("", sellButtonSTyle);
 		sellButton.addListener(new InputListener() {
 			public boolean touchDown(InputEvent event, float x, float y,
 					int pointer, int button) {
 				
 				if (selectedTower != null)
 				{
 					currentGold += selectedTower.towerStats.sellPrice;
 					goldButton.setText("        " + currentGold);
 					selectedTower.remove();
 					MyGdxGame.towers.remove(selectedTower);
 					selectTower(null);
 				}
 				return true;
 			}
 		});
 		sellButton.setPosition(GameConstants.screenWidth - 3*GameConstants.tileSize, GameConstants.screenHeight-100);
 		stage.addActor(sellButton);
 		
 		TextButton.TextButtonStyle upgradeButtonStyle = new TextButton.TextButtonStyle();
 		TextureRegion upStyleUpgrade = new TextureRegion(miscAtlas.createSprite("upgradeTowerButton"));
 		TextureRegion downStyleUpgrade = new TextureRegion(miscAtlas.createSprite("upgradeTowerButton"));
 		upgradeButtonStyle.font = font;
 		upgradeButtonStyle.fontColor = Color.BLACK;
 		upgradeButtonStyle.up = new TextureRegionDrawable(upStyleUpgrade);
 		upgradeButtonStyle.down = new TextureRegionDrawable(downStyleUpgrade);
 		final TextButton upgradeButton = new TextButton("", upgradeButtonStyle);
 		upgradeButton.addListener(new InputListener() {
 			public boolean touchDown(InputEvent event, float x, float y,
 					int pointer, int button) {
 				if (selectedTower == null) 
 					return true;
 				if (selectedTower.towerStats.upgradesTo.equals("null"))
 					return true;
 				int upgradeCost = towerInfo.get(selectedTower.towerStats.upgradesTo).buildCost - selectedTower.towerStats.buildCost;
 				boolean canAfford = currentGold >= upgradeCost ? true : false;
 				if (canAfford) {
 					MyGdxGame.currentGold -= upgradeCost;
 					goldButton.setText("        " + currentGold);
 					eventHandler.queueEvent(new Event("upgrade", (int)(selectedTower.getX()/GameConstants.tileSize), (int)(selectedTower.getY()/GameConstants.tileSize), "x"));
 				}
 				
 				return true;
 			}
 		});
 		upgradeButton.setPosition(GameConstants.screenWidth - 4*GameConstants.tileSize, GameConstants.screenHeight-100);
 		
 		for (int i = 0; i < towerKeys.size(); i++)
 		{
 			if (!towerInfo.get(towerKeys.get(i)).buildable)
 			{
 				towerKeys.remove(i);
 				i--;
 				continue;
 			}
 			TextButton.TextButtonStyle towerButtonStyle = new TextButton.TextButtonStyle();
 			System.out.println("Trying to load: " + towerInfo.get(towerKeys.get(i)).towerTexture);
 			TextureRegion upStyle2 = new TextureRegion(towersAtlas.createSprite(towerInfo.get(towerKeys.get(i)).towerTexture));
 			TextureRegion downStyle2 = new TextureRegion(towersAtlas.createSprite(towerInfo.get(towerKeys.get(i)).towerTexture));
 			towerButtonStyle.font = font;
 			towerButtonStyle.up = new TextureRegionDrawable(upStyle2);
 			towerButtonStyle.down = new TextureRegionDrawable(downStyle2);
 			TextButton arrowTowerButton = new TextButton("", towerButtonStyle);
 			final String currentKey = towerKeys.get(i);
 			arrowTowerButton.addListener(new InputListener() {
 				public boolean touchDown(InputEvent event, float x, float y,
 						int pointer, int button) {
 					building = true;
 					buildingTower = towerInfo.get(currentKey).type;
 					buildingTowerSprite = towersAtlas.createSprite(towerInfo.get(currentKey).towerTexture);
 					towerName = currentKey;
 					temporaryTowerActor = new MapTile(towersAtlas.createSprite(towerInfo.get(towerName).towerTexture), -64,0);
 					stage.addActor(temporaryTowerActor);
 					yellowBoxLabel.setText(towerName);
 					return true;
 				}
 			});
 			arrowTowerButton.setPosition(10 + 10*i + 64*i, GameConstants.screenHeight - 100);
 			stage.addActor(arrowTowerButton);
 		}
 		
 		
 		stage.addActor(upgradeButton);
 	
 		TextButton.TextButtonStyle livesButtonStyle = new TextButton.TextButtonStyle();
 		TextureRegion upStyleLives = new TextureRegion(miscAtlas.createSprite("heart"));
 		TextureRegion downStyleLives = new TextureRegion(miscAtlas.createSprite("heart"));
 		livesButtonStyle.font = font;
 		livesButtonStyle.up = new TextureRegionDrawable(upStyleLives);
 		livesButtonStyle.down = new TextureRegionDrawable(downStyleLives);
 		livesButton = new TextButton("" + livesLeft, livesButtonStyle);
 		livesButton.setPosition(10*GameConstants.tileSize, GameConstants.screenHeight-100);
 		stage.addActor(livesButton);
 		
 		TextButton.TextButtonStyle goldButtonStyle = new TextButton.TextButtonStyle();
 		TextureRegion upStyleGold = new TextureRegion(miscAtlas.createSprite("gold"));
 		TextureRegion downStyleGold = new TextureRegion(miscAtlas.createSprite("gold"));
 		goldButtonStyle.font = font;
 		goldButtonStyle.up = new TextureRegionDrawable(upStyleGold);
 		goldButtonStyle.down = new TextureRegionDrawable(downStyleGold);
 		goldButton = new TextButton("        " + currentGold, goldButtonStyle);
 		goldButton.setPosition(12*GameConstants.tileSize, GameConstants.screenHeight-100);
 		stage.addActor(goldButton);
 	}
 	private void loadSounds()
 	{
 		System.out.println("Trying to load sounds");
 		Iterator<String> it = towerInfo.keySet().iterator();
 		while(it.hasNext())
 		{
 			String s = it.next();
 			if (!towerInfo.get(s).impactSound.equals(""))
 				sounds.put(towerInfo.get(s).impactSound, Gdx.audio.newSound(Gdx.files.internal("sounds/" + towerInfo.get(s).impactSound)));
 			if (!towerInfo.get(s).shootSound.equals(""))
 				sounds.put(towerInfo.get(s).shootSound, Gdx.audio.newSound(Gdx.files.internal("sounds/" + towerInfo.get(s).shootSound)));
 		}
 
 		System.out.println("Finished loading sounds");
 	}
 	private void checkWinConditions()
 	{
 		if (livesLeft <= 0)
         {
         	//Loser
         	lost = true;
         	if (!savedParameters)
         	{
         		thinkTank.writeParameters(new FileHandle(parameterSavePath));
     			savedParameters = true;
     		}
         	questionaire = new Questionaire(miscAtlas.createSprite("starButton"), stage, font);
         }
         else if (enemies.size() <= 0 && enemyWave.size() <= 0)
         {
         	//Winner
         	won = true;
         	//resetGame();
         	if (!savedParameters)
         	{
         		thinkTank.writeParameters(new FileHandle(parameterSavePath));
         		savedParameters = true;
         	}
         	questionaire = new Questionaire(miscAtlas.createSprite("starButton"), stage, font);
         }
 	}
 }
