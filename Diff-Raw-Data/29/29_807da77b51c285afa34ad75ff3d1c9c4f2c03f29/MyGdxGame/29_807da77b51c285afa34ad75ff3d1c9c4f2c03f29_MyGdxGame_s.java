 package com.me.mygdxgame;
 
 import java.text.ParseException;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 
 public class MyGdxGame implements ApplicationListener
 {
 	Group mapGroup;
 	boolean wasTab = false;
 
 	boolean fullScreen = false; // Full screen yes or no.
 	boolean printDebug = true; // Print debug, add or remove writes in end of
 								// render.
 
 	ReplayHandler replayHandler = new ReplayHandler();
 	boolean saveReplay = false;
 	boolean useReplay = false;
 	String replayPath = "/AdaptiveTD/Replays/testReplay.txt"; // Must be
 																// external,
 																// relative to
 																// user
 																// directory.
 	String replaySavePath = "/AdaptiveTD/Replays/testReplay.txt";
 
 	boolean savedParametersAndRelations = false;
 	String logSavePath = "/AdaptiveTD/Log/Logfile.txt";
 	String parameterSavePath = "/AdaptiveTD/Parameters/parameters.txt";
 	String relationsSavePath = "/AdaptiveTD/Parameters/relations.txt";
 
 	boolean paused = true;
 	boolean resuming = true;
 
 	private float pauseTime = GameConstants.startTime;
 
 	private static final int VIRTUAL_WIDTH = 1280;
 	private static final int VIRTUAL_HEIGHT = 768;
 	private static final float ASPECT_RATIO = (float) VIRTUAL_WIDTH / (float) VIRTUAL_HEIGHT;
 	private Rectangle viewport;
 
 	Stage stage;
 
 	Map map;
 	
 	Sprite qBG;
 	
 	SpriteBatch spriteBatch;
 
 	boolean building = false, wasTouched = false, won = false, lost = false;
 
 	float totalTime = 0;
 
 	double timer = 0;
 	int uC = 0, uT = 0;
 
 	Vector2 touchedTile = new Vector2(0, 0);
 
 	String buildingTower = "", towerName = "Tower";
 	Sprite buildingTowerSprite = null;
 	
 	Camera gameCamera;
 
 	ExtendedActor temporaryTowerActor = null;
 
 	EventHandler eventHandler = new EventHandler();
 
 	ListenerGenerator listenerGenerator;
 	ButtonGenerator buttonGenerator;
 	ThinkTank thinkTank;
 	Questionaire questionaire;
 	boolean questionaireIsDisplayed = false;
 	HeadsUpDisplay hud;
 	GameProcessor gameProcessor;
 	AssetManager assetManager;
 	StatsFetcher statsFetcher;
 	
 	@Override
 	public void create()
 	{
 		gameCamera = new OrthographicCamera(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
 		Gdx.graphics.setTitle("Adaptive Tower Defense v0.001");
 		spriteBatch = new SpriteBatch();
 
 		assetManager = new AssetManager();
 		assetManager.initialize();
 
 		stage = new Stage();
 		stage.setCamera(gameCamera);
 		Gdx.input.setInputProcessor(stage);
 
 		gameProcessor = new GameProcessor();
 		gameProcessor.initialize();
 		
 		map = new Map(assetManager.mapTilesAtlas);
 		FileHandle handle = Gdx.files.internal("Maps/map.txt");
 		mapGroup = map.loadMap(handle);
 		stage.addActor(mapGroup);
 
 		thinkTank = new ThinkTank();
 		
 		statsFetcher = new StatsFetcher();
 		FileHandle towerHandle = Gdx.files.internal("Stats/towerStats.txt");
 		try
 		{
 			thinkTank.towerInfo = statsFetcher.loadTowerStats(towerHandle);
 		} catch (NumberFormatException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ParseException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		FileHandle enemyHandle = Gdx.files.internal("Stats/enemyStats.txt");
 		thinkTank.enemyInfo = statsFetcher.generateEnemyInfo(enemyHandle);
 
 		//gameProcessor.createWave(thinkTank, map, assetManager.enemiesAtlas, assetManager.miscAtlas);
 
 		listenerGenerator = new ListenerGenerator(this);
 		buttonGenerator = new ButtonGenerator();
 		
 		// UI Creation
 		System.out.println("Generating UI");
 		hud = new HeadsUpDisplay(assetManager.font);
 		hud.createUI(assetManager.miscAtlas, assetManager.towersAtlas, thinkTank.towerInfo, stage, buttonGenerator, listenerGenerator);
 		// -----------
 
 		if (useReplay)
 		{
 			FileHandle replayHandle = Gdx.files.external(replayPath);
 			replayHandler.loadReplay(replayHandle);
 		}
 
 		Gdx.gl.glClearColor(Color.GRAY.r, Color.GRAY.g, Color.GRAY.b, Color.GRAY.a);
 
 		System.out.println("Loading sounds");
 		assetManager.loadSounds(thinkTank.towerInfo);
 		System.out.println("Loading music");
 		assetManager.loadMusic();
 		thinkTank.defaultEnemyInfo = statsFetcher.generateEnemyInfo(enemyHandle);
 		try
 		{
 			thinkTank.defaultTowerInfo = statsFetcher.loadTowerStats(towerHandle);
 		} catch (NumberFormatException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ParseException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		qBG = assetManager.miscAtlas.createSprite("qBG");
 
 		FileHandle parameterHandle = Gdx.files.external(parameterSavePath);
 		FileHandle relationsHandle = Gdx.files.external(relationsSavePath);
 		thinkTank.initializeParameters(parameterHandle);
 		thinkTank.initializeRelations(relationsHandle);
 
 		resetGame();
 	}
 	
 	@Override
 	public void dispose()
 	{
 		stage.dispose();
 	}
 
 	@Override
 	public void render()
 	{
 		// clear previous frame
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		if (won || lost)
 		{
 			if (questionaire != null && questionaire.happy > 0 && questionaire.difficult > 0)
 			{
 				thinkTank.calculateVariables(questionaire.happy, questionaire.difficult, gameProcessor.livesLeft);
 				resetGame();
 				//System.out.println(thinkTank.parameters.get("GlobalMonsterSpeed").value + " " + thinkTank.oldParameters.get("GlobalMonsterSpeed").value);
 				if (thinkTank.parameters.get("GlobalMonsterSpeed").value > thinkTank.oldParameters.get("GlobalMonsterSpeed").value)
 					assetManager.sounds.get("MonsterSpeedIncreased").play();
 				else if (thinkTank.parameters.get("GlobalMonsterSpeed").value < thinkTank.oldParameters.get("GlobalMonsterSpeed").value)
 					assetManager.sounds.get("MonsterSpeedDecreased").play();
 				/*if (thinkTank.parameters.get("GlobalMonsterSpeed").value < thinkTank.oldParameters.get("GlobalMonsterSpeed").value)
 					assetManager.sounds.get("MonsterHealthIncreased").play();
 				else if (thinkTank.parameters.get("GlobalMonsterSpeed").value > thinkTank.oldParameters.get("GlobalMonsterSpeed").value)
 					assetManager.sounds.get("MonsterHealthDecreased").play();
 				*/
 			}
 		}
 		if (!paused)
 		{
 			totalTime += Gdx.graphics.getDeltaTime();
 			if (!useReplay)
 			{
 				handleInput();
 				eventHandler.update();
 			} else
 			{
 				eventHandler.events = replayHandler.playReplay(totalTime);
 			}
 			if (saveReplay)
 			{
 				replayHandler.addEvents(totalTime, eventHandler);
 			}
 			if (!won && !lost)
 			{
 				thinkTank.calculateNewParameters(totalTime,
 						gameProcessor.currentGold, gameProcessor.livesLeft,
 						gameProcessor.towers, eventHandler.events);
 			}
 			handleEvents();
 			hud.updateYellowBoxPosition();
 			checkWave(totalTime);
 				
			gameProcessor.updateGame(totalTime, gameCamera, map, assetManager, stage, hud, gameProcessor.nudgeChanceConstant, thinkTank.nudgeChance, assetManager.sounds.get("earthquake"));
 
 			if (gameProcessor.isGameLost())
 			{
 				lost = true;
 				if (!savedParametersAndRelations)
 				{
 					thinkTank.writeLogToDisk(Gdx.files.external(logSavePath));
 					thinkTank.writeParametersToDisk(Gdx.files.external(parameterSavePath));
 					thinkTank.writeRelationsToDisk(Gdx.files.external(relationsSavePath));
 					savedParametersAndRelations = true;
 				}
 				if (!questionaireIsDisplayed)
 				{
 					questionaire = new Questionaire(qBG, assetManager.miscAtlas.createSprite("thumbUp"), assetManager.miscAtlas.createSprite("thumbDown"), assetManager.miscAtlas.createSprite("thumbSide"), stage, assetManager.font, buttonGenerator);
 					questionaireIsDisplayed = true;
 				}
 			}
 			else if (gameProcessor.isGameWon())
 			{
 				won = true;
 				if (!savedParametersAndRelations)
 				{
 					thinkTank.writeLogToDisk(Gdx.files.external(logSavePath));
 					thinkTank.writeParametersToDisk(Gdx.files.external(parameterSavePath));
 					thinkTank.writeRelationsToDisk(Gdx.files.external(relationsSavePath));
 					savedParametersAndRelations = true;
 				}
 				if (!questionaireIsDisplayed)
 				{
 					questionaire = new Questionaire(qBG, assetManager.miscAtlas.createSprite("thumbUp"), assetManager.miscAtlas.createSprite("thumbDown"), assetManager.miscAtlas.createSprite("thumbSide"), stage, assetManager.font, buttonGenerator);
 					questionaireIsDisplayed = true;
 				}
 			}
 			
 			// Draws game
 			stage.draw();
 		} else if (resuming && !won && !lost)
 		{
 			totalTime += Gdx.graphics.getDeltaTime();
 
 			if (!useReplay)
 			{
 				handleInput();
 				eventHandler.update();
 			} else
 			{
 				eventHandler.events = replayHandler.playReplay(totalTime);
 			}
 			if (saveReplay)
 			{
 				replayHandler.addEvents(totalTime, eventHandler);
 			}
 			thinkTank.calculateNewParameters(0, gameProcessor.currentGold, gameProcessor.livesLeft, gameProcessor.towers, eventHandler.events);
 			handleEvents();
 			hud.updateYellowBoxPosition();
 			// Draws game
 			stage.draw();
 
 			pauseTime -= Gdx.graphics.getDeltaTime();
 			spriteBatch.begin();
 			assetManager.font.setScale(10);
 			if (!Integer.toString((int) Math.ceil(pauseTime)).equals("0"))
 				assetManager.font.draw(spriteBatch, Integer.toString((int) Math.ceil(pauseTime)), GameConstants.screenWidth / 2 - 32, GameConstants.screenHeight / 2);
 			assetManager.font.setScale(1);
 			spriteBatch.end();
 			if (pauseTime <= 0)
 				paused = false;
 		}
 		if(questionaire != null)
 		{
 			spriteBatch.begin();
 			questionaire.draw(spriteBatch);
 			spriteBatch.end();
 		}
 		// Fps counter
 		timer += Gdx.graphics.getDeltaTime();
 		uC++;
 		if (timer >= 1)
 		{
 			// System.out.println("FPS: " + uC);
 			uC = 0;
 			timer = 0;
 		}
 
 		if (Gdx.input.isKeyPressed(Keys.TAB) && !wasTab)
 		{
 			hud.updateConsoleState(true, thinkTank.parameters, thinkTank.thinkTankInfo);
 			wasTab = true;
 		} else if (!Gdx.input.isKeyPressed(Keys.TAB))
 			wasTab = false;
 		if (Gdx.input.isKeyPressed(Keys.X))
 		{
 			gameProcessor.waveTime.clear();
 			gameProcessor.enemyWave.clear();
 			for (Enemy e : gameProcessor.enemies)
 				stage.getActors().removeValue(e, true);
 			gameProcessor.enemies.clear();
 		}
 		
 		assetManager.checkMusic();
 	}
 
 	@Override
 	public void resize(int width, int height)
 	{
 
 		float aspectRatio = (float) width / (float) height;
 		float scale = 1f;
 		Vector2 crop = new Vector2(0f, 0f);
 
 		if (aspectRatio > ASPECT_RATIO)
 		{
 			scale = (float) height / (float) VIRTUAL_HEIGHT;
 			crop.x = (width - VIRTUAL_WIDTH * scale) / 2f;
 		} else if (aspectRatio < ASPECT_RATIO)
 		{
 			scale = (float) width / (float) VIRTUAL_WIDTH;
 			crop.y = (height - VIRTUAL_HEIGHT * scale) / 2f;
 		} else
 		{
 			scale = (float) width / (float) VIRTUAL_WIDTH;
 		}
 
 		float w = (float) VIRTUAL_WIDTH * scale;
 		float h = (float) VIRTUAL_HEIGHT * scale;
 
 		Gdx.graphics.setDisplayMode((int) w, (int) h, fullScreen);
 
 		viewport = new Rectangle(crop.x, crop.y, w, h);
 
 		Gdx.gl.glViewport((int) viewport.x, (int) viewport.y, (int) viewport.width, (int) viewport.height);
 
 		gameCamera.position.set(gameCamera.viewportWidth / 2, gameCamera.viewportHeight / 2, 0);
 
 		gameCamera.update();
 		gameCamera.apply(Gdx.gl10);
 	}
 
 	public void resetGame()
 	{
 		if (saveReplay && !useReplay)
 		{
 			FileHandle saveHandle = Gdx.files.external(replaySavePath);
 			replayHandler.saveReplay(saveHandle);
 		}
 		replayHandler.events.clear();
 		replayHandler.savingEvents.clear();
 		// useReplay = false;
 
 		if (useReplay)
 		{
 			FileHandle replayHandle = Gdx.files.external(replayPath);
 			replayHandler.loadReplay(replayHandle);
 		}
 
 		stage.getActors().clear();
 
		//gameProcessor.resetGame(); 
 		gameProcessor.towers.clear();
 		gameProcessor.enemies.clear();
 		gameProcessor.enemyWave.clear();
 		gameProcessor.waveTime.clear();
 		hud.towerKeys.clear();
 		totalTime = 0;
 
 		mapGroup = map.regenerateMap();
 		//FileHandle handle = Gdx.files.internal("Maps/map.txt");
 		stage.addActor(mapGroup);
 		//gameProcessor.createWave(thinkTank, map, assetManager.enemiesAtlas, assetManager.miscAtlas);
 		hud.createUI(assetManager.miscAtlas, assetManager.towersAtlas, thinkTank.towerInfo, stage, buttonGenerator, listenerGenerator);
 
 		gameProcessor.currentGold = GameConstants.startGold;
 		hud.goldButton.setText("        " + gameProcessor.currentGold);
 		gameProcessor.livesLeft = GameConstants.startLives;
 		hud.livesButton.setText("" + gameProcessor.livesLeft);
 
 		won = false;
 		lost = false;
 
 		thinkTank.clear();
 
 		pauseTime = GameConstants.startTime;
 		paused = true;
 		resuming = true;
 
 		if (questionaire != null)
 		{
 			thinkTank.thinkTankInfo.totalGames++;
 			if(gameProcessor.nudgeChanceConstant > 0 )
 				thinkTank.thinkTankInfo.totalHappinessSuperMobsOn += questionaire.happy;
 			else
 				thinkTank.thinkTankInfo.totalHappinessSuperMobsOff += questionaire.happy;
 			if(thinkTank.diggerChance > 0 )
 				thinkTank.thinkTankInfo.totalHappinessDiggersOn += questionaire.happy;
 			else
 				thinkTank.thinkTankInfo.totalHappinessDiggersOff += questionaire.happy;
 			if(thinkTank.superEnemyChance > 0 )
 				thinkTank.thinkTankInfo.totalHappinessSuperMobsOn += questionaire.happy;
 			else
 				thinkTank.thinkTankInfo.totalHappinessSuperMobsOff += questionaire.happy;
 			questionaire.reset();
 			questionaire = null;
 			questionaireIsDisplayed = false;
 		}
 		// Not sure what we  thought, we need some Min max values, or to multiply with something? E.g. 0.2* chance with diggers, 0.5* chance with nudge, etc.
 		//thinkTank.diggerChance = (float)((thinkTank.thinkTankInfo.totalHappinessDiggersOn/(3*thinkTank.thinkTankInfo.totalGames))/(thinkTank.thinkTankInfo.totalHappinessDiggersOff/(3*thinkTank.thinkTankInfo.totalGames) + thinkTank.thinkTankInfo.totalHappinessDiggersOn/(3*thinkTank.thinkTankInfo.totalGames)));
 		//thinkTank.nudgeChance = (float)((thinkTank.thinkTankInfo.totalHappinessSuperMobsOn/(3*thinkTank.thinkTankInfo.totalGames))/(thinkTank.thinkTankInfo.totalHappinessSuperMobsOff/(3*thinkTank.thinkTankInfo.totalGames) + thinkTank.thinkTankInfo.totalHappinessSuperMobsOn/(3*thinkTank.thinkTankInfo.totalGames)));
 		//thinkTank.superEnemyChance = (float)((thinkTank.thinkTankInfo.totalHappinessSuperMobsOn/(3*thinkTank.thinkTankInfo.totalGames))/(thinkTank.thinkTankInfo.totalHappinessSuperMobsOff/(3*thinkTank.thinkTankInfo.totalGames) + thinkTank.thinkTankInfo.totalHappinessSuperMobsOn/(3*thinkTank.thinkTankInfo.totalGames)));
 		
 		gameProcessor.diggerEnemies.clear();
 		gameProcessor.lastMinionTime = 0;
 		float statMultiplier = 1.0f;
 		for (int t = 0; t < gameProcessor.waveParts; t++)
 		{
 			for (int i = 0; i < gameProcessor.waveSize + (t * gameProcessor.waveIncrements); i++)
 			{
 				gameProcessor.generateNextEnemy(statMultiplier, thinkTank, map, assetManager.enemiesAtlas, assetManager.miscAtlas, thinkTank.diggerChance, thinkTank.superEnemyChance);
 			}
 			statMultiplier += 0.25f;
 			gameProcessor.lastMinionTime += gameProcessor.wavePartDelay;
 		}
 		hud.updateConsoleState(false, thinkTank.parameters, thinkTank.thinkTankInfo);
 		savedParametersAndRelations = false;
 
 		float colorValue;
 		if (thinkTank.parameters.get("GlobalMonsterHP").value <= 1.0f)
 			colorValue = 1.0f;
 		else
 		{
 			colorValue = thinkTank.parameters.get("GlobalMonsterHP").value / 10.0f;
 			colorValue = 1.0f - colorValue;
 		}
 		for (Actor actor : mapGroup.getChildren())
 		{
 			((MapTile) actor).setColor(colorValue,colorValue,colorValue, 1.0f);
 		}
 		assetManager.playSong(thinkTank.speedLevel);
 	}
 
 	private void checkWave(float totalTime)
 	{
 		if (gameProcessor.waveTime.size() > 0)
 		{
 			if (gameProcessor.waveTime.get(0) <= totalTime)
 			{
 				Enemy addEnemy = gameProcessor.enemyWave.get(gameProcessor.waveTime.get(0));
 				if(addEnemy.willDigg)
 				{
 					gameProcessor.diggerEnemies.add(addEnemy);
 					assetManager.sounds.get("diggerEnemy").play();
 				}
 				if (addEnemy.superEnemy)
 					assetManager.sounds.get("superEnemy").play();
 				stage.addActor(addEnemy);
 				gameProcessor.enemies.add(gameProcessor.enemyWave.get(gameProcessor.waveTime.get(0)));
 				gameProcessor.enemyWave.remove(gameProcessor.waveTime.get(0));
 				gameProcessor.waveTime.remove(0);
 				// generateNextEnemy();
 			}
 		}
 	}
 	
 	private void handleEvents()
 	{
 		for (int i = 0; i < eventHandler.events.size(); i++)
 		{
 			Event e = eventHandler.events.get(i);
 			if (e.eventType.equals("build"))
 			{
 				if (map.canBuild((int) e.x, (int) e.y))
 				{
 
 					Tower t = gameProcessor.createTower(e.tower, new Vector2(e.x, e.y), assetManager.towersAtlas, assetManager.miscAtlas, thinkTank.towerInfo);
 					int buildCost = thinkTank.towerInfo.get(e.tower).buildCost;
 					boolean canAfford = gameProcessor.currentGold >= buildCost ? true : false;
 
 					boolean canBuild = canAfford;
 					if (canAfford)
 					{
 						for (int c = 0; c < gameProcessor.towers.size(); c++)
 						{
 							if (gameProcessor.towers.get(c).getX() == t.getX() && gameProcessor.towers.get(c).getY() == t.getY())
 								canBuild = false;
 						}
 					}
 					if (canBuild)
 					{
 
 						gameProcessor.currentGold -= buildCost;
 						hud.goldButton.setText("        " + gameProcessor.currentGold);
 						stage.addActor(t);
 						gameProcessor.towers.add(t);
 						hud.fadeInYellowBox(t, gameProcessor.selectTower(t, thinkTank.towerInfo));
 					} else
 					{
 						t = null;
 					}
 				}
 			} else if (e.eventType.equals("sell"))
 			{
 
 			} else if (e.eventType.equals("upgrade"))
 			{
 				for (int u = 0; u < gameProcessor.towers.size(); u++)
 				{
 					if (e.x == (int) (gameProcessor.towers.get(u).getX() / GameConstants.tileSize) && e.y == (int) (gameProcessor.towers.get(u).getY() / GameConstants.tileSize))
 					{
 						if (!gameProcessor.towers.get(u).towerStats.upgradesTo.equals("null"))
 						{
 							TowerStats newTowerStats = thinkTank.towerInfo.get(gameProcessor.towers.get(u).towerStats.upgradesTo);
 							gameProcessor.towers.get(u).upgrade(newTowerStats, assetManager.towersAtlas.createSprite(newTowerStats.towerTexture), assetManager.miscAtlas.createSprite(newTowerStats.missileTexture));
 							hud.fadeInYellowBox(gameProcessor.towers.get(u), gameProcessor.selectTower(gameProcessor.towers.get(u), thinkTank.towerInfo));
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
 		Actor a = stage.hit(input.x, input.y, true);
 		if (a != null && a.getClass() == MapTile.class)
 		{
 			touchedTile = new Vector2((float) Math.floor(a.getX() / GameConstants.tileSize), (float) Math.floor(a.getY() / GameConstants.tileSize));
 		}
 		if (wasTouched && !Gdx.input.isTouched())
 		{
 			if (building && temporaryTowerActor != null) //Kanskje flytte dette ogs
 			{
 				if (touchedTile.x <= Map.mapWidth && touchedTile.y <= Map.mapHeight  && a != null && a.getClass() == MapTile.class)
 					eventHandler.queueEvent(new Event("build", (int) touchedTile.x, (int) touchedTile.y, buildingTower));
 				building = false;
 				temporaryTowerActor.remove();
 				temporaryTowerActor = null;
 			}
 			wasTouched = false;
 		}
 		if (Gdx.input.isTouched())
 		{
 			if (temporaryTowerActor != null && a != null)
 			{
 				temporaryTowerActor.setPosition(a.getX(), a.getY());
 			}
 			wasTouched = true;
 			Actor hit = stage.hit(Gdx.input.getX(), GameConstants.screenHeight - Gdx.input.getY(), false);
 			if (hit != null && hit.getClass() == Tower.class)
 			{
 				Tower t = (Tower) hit;
 				hud.fadeInYellowBox(t, gameProcessor.selectTower(t, thinkTank.towerInfo));
 			} else if (hit != null && hit.getClass() == Enemy.class)
 			{
 				Enemy e = (Enemy) hit;
 				hud.fadeInYellowBox(e, gameProcessor.selectEnemy(e));
 			} else if (Gdx.input.justTouched())
 			{
 				hud.fadeOutYellowBox();
 			}
 		} else if (temporaryTowerActor != null)
 		{
 			temporaryTowerActor.remove();
 			temporaryTowerActor = null;
 		}
 	}
 	
 	
 	@Override
 	public void pause()
 	{
 	}
 
 	@Override
 	public void resume()
 	{
 	}
 }
