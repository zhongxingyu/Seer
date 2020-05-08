 package com.me.mygdxgame;
 
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.actions.Actions;
 
 public class GameProcessor
 {
 	int livesLeft, currentGold;
 
 	HashMap<Float, Enemy> enemyWave = new HashMap<Float, Enemy>();
 	List<Float> waveTime = new ArrayList<Float>();
 	float currentMinionDelay = 2.0f; // Will be set at random
 	int waveSize = 10;
 	int waveParts = 3;
 	int waveIncrements = 5;
 	int wavePartDelay = 7; // Seconds
 	int nextEnemyGeneratedCounter = 0;
 	float lastMinionTime = 0;
 	Random rand = new Random();
 
 	List<Missile> missiles = new ArrayList<Missile>();
 	List<Enemy> enemies = new ArrayList<Enemy>();
 	List<Tower> towers = new ArrayList<Tower>();
 	List<Enemy> diggerEnemies = new ArrayList<Enemy>();
 	Tower selectedTower;
 
 	boolean earthquakeEnabled = true;
 	boolean movesTowers = true;
 	boolean tempNudge = false;
 	
 	float nudgeChanceConstant = 0.2f;
 	float nudgeTimer = 0;
 	float nudgeRemainingTime = 0f;
 	float nudgeRandomizerTimer = 1.0f;
 	float nudgeRandomizerInterval = 0.5f;
 	
 	
 	float superEnemyHealthMultiplier = 1.5f;
 	float superEnemySpeedMultiplierBonus = 0.5f; // If currentSpeedMultiplier for an enemy is 2.0, it will be 2.5 for super minions of that kind.
 	float superEnemySpeedSizeScale = 0.8f; // Size scale for super enemies with speed bonus.
 	float superEnemyHealthSizeScale = 1.2f; // ^ for health super minions
 	
 	
 	public void initialize()
 	{
 		livesLeft = GameConstants.startLives;
 		currentGold = GameConstants.startGold;
 
 	}
 
 	public void generateNextEnemy(float statMultiplier, ThinkTank thinkTank,
 			Map map, TextureAtlas enemiesAtlas, TextureAtlas miscAtlas, float diggerChance, float superEnemyChance)
 	{
 		currentMinionDelay = rand.nextInt(30);
 		currentMinionDelay /= 10.0f;
 		float time = lastMinionTime + currentMinionDelay;
 		lastMinionTime += currentMinionDelay; // Basically same as time
 		Enemy e = null;
 		double randValue = rand.nextDouble();
 
 		if(randValue <= diggerChance)
 		{
 			e = createEnemy("digger", thinkTank, map, enemiesAtlas, miscAtlas);
 			e.modifyOriginalHealth(statMultiplier);
 			e.modifyOriginalMoveSpeed(statMultiplier);
 		}
 		else if (randValue <= 0.50)
 		{
 			e = createEnemy("basic", thinkTank, map, enemiesAtlas, miscAtlas);
 			e.modifyOriginalHealth(statMultiplier);
 			e.modifyOriginalMoveSpeed(statMultiplier);
 		}
 		else if (randValue <= 0.75)
 		{
 			e = createEnemy("fast", thinkTank, map, enemiesAtlas, miscAtlas);
 			e.modifyOriginalMoveSpeed(statMultiplier);
 		}
 		else
 		{
 			e = createEnemy("tough", thinkTank, map, enemiesAtlas, miscAtlas);
 			e.modifyOriginalHealth(statMultiplier);
 		}
 
 		if (!waveTime.contains(time))
 		{
 			float superMinionRand = (float)rand.nextDouble();
 			if(superMinionRand < superEnemyChance)
 			{
 				e.superEnemy = true;
 				if(superMinionRand < superEnemyChance/2)
 				{
 					e.modifyOriginalHealth(superEnemyHealthMultiplier);
 					e.setScale(superEnemyHealthSizeScale);
 					e.setColor(e.getColor().r+150, e.getColor().g, e.getColor().b, e.getColor().a);
 				}
 				else
 				{
 					e.modifyOriginalMoveSpeed(e.getStat("currentMoveSpeedMultiplier") + superEnemySpeedMultiplierBonus);
 					e.setScale(superEnemySpeedSizeScale);
 					e.setColor(e.getColor().r, e.getColor().g, e.getColor().b+150, e.getColor().a);
 				}
 			}
 			waveTime.add(time);
 			enemyWave.put(time, e);
 		}
 		else
 		{
 			generateNextEnemy(statMultiplier, thinkTank, map, enemiesAtlas,
 					miscAtlas, diggerChance, superEnemyChance);
 		}
 	}
 
 	public List<String> selectEnemy(Enemy e)
 	{
 		List<String> textForBox = new ArrayList<String>();
 
 		textForBox.add(e.enemyStats.type);
 		textForBox.add("Health: " + Math.round(e.getStat("currentHealth")));
 		textForBox.add("Speed: " + Math.round(e.getStat("currentMoveSpeedMultiplier")));
 		textForBox.add("Yields: " + Math.round(e.getStat("currentGoldYield")));
 		return textForBox;
 
 	}
 
 	public List<String> selectTower(Tower t,
 			HashMap<String, TowerStats> towerInfo)
 	{
 		if (t != null)
 		{
 			List<String> textForBox = new ArrayList<String>();
 			selectedTower = t;
 			textForBox.add(t.towerStats.type);
 			textForBox.add(t.towerStats.description);
 
 			if (selectedTower.towerStats.upgradesTo.equals("null"))
 			{
 				textForBox.add("Upgrade: MAX");
 			}
 			else
 			{
 				int upgradeCost = towerInfo
 						.get(selectedTower.towerStats.upgradesTo).buildCost
 						- selectedTower.towerStats.buildCost;
 				textForBox.add("Upgrade: " + upgradeCost);
 			}
 			textForBox.add("Sell: " + selectedTower.towerStats.sellPrice);
 			return textForBox;
 		}
 		else
 		{
 			selectedTower = null;
 			return null;
 		}
 	}
 	// Eventually take a towerInfo id, and create appropriate.
 	public Tower createTower(String type, Vector2 tilePosition, TextureAtlas towersAtlas, TextureAtlas miscAtlas, HashMap<String, TowerStats> towerInfo)
 	{
 		Tower t = new Tower(
 				towerInfo.get(type),
 				towersAtlas.createSprite(towerInfo.get(type).towerTexture),
 				miscAtlas.createSprite(towerInfo.get(type).missileTexture));
 		t.setPosition(tilePosition.x * GameConstants.tileSize, tilePosition.y
 				* GameConstants.tileSize);
 		return t;
 	}
 	public void createWave(ThinkTank thinkTank, Map map,
 			TextureAtlas enemiesAtlas, TextureAtlas miscAtlas)
 	{
 		Enemy e = createEnemy("fast", thinkTank, map, enemiesAtlas, miscAtlas);
 		addEnemyToWave(0.1f, e);
 		Collections.sort(waveTime);
 	}
	public void updateGame(float totalTime, Camera gameCamera, Map map, AssetManager assetManager, Stage stage, HeadsUpDisplay hud, float nudgeChanceConstant, float nudgeChance, Sound earthquakeSound)
 	{
		Random random = new Random();
		if (random.nextFloat() < nudgeChance)
			earthquakeEnabled = true;
		else
			earthquakeEnabled = false;
		
 		if (earthquakeEnabled && !isGameWon() && !isGameLost())
 		{
 			doEarthquake(gameCamera, map, nudgeChanceConstant, earthquakeSound);
 		}
 		
 		if (enemies.size() > 0)
 		{
 			for (int i = 0; i < towers.size(); i++)
 			{
 				towers.get(i).calculateTarget(Gdx.graphics.getDeltaTime(), enemies, null);
 				if (towers.get(i).canShoot)
 				{
 					Missile m = towers.get(i).shoot();
 					if (m != null)
 					{
 						assetManager.sounds.get(towers.get(i).towerStats.shootSound).play();
 						stage.addActor(m);
 						missiles.add(m);
 						m.setZIndex(towers.get(i).getZIndex() - 1);
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
 					Iterator<String> it = missiles.get(i).effect.effects.keySet().iterator();
 					while (it.hasNext())
 					{
 						String s = it.next();
 						if (missiles.get(i).effect.effects.get(s).b)
 						{
 							targEnemy.setStat(s, missiles.get(i).effect.effects.get(s).f);
 						} else
 						{
 							targEnemy.editStat(s, missiles.get(i).effect.effects.get(s).f);
 						}
 					}
 					assetManager.sounds.get(missiles.get(i).impactSound).play();
 				}
 				// ----------------------
 				// If TargetStrategy.Circle
 				else if (missiles.get(i).effect.missileTarget.targetingStrategy == TargetingStrategy.Circle)
 				{
 					TargetCircle targetCircle = (TargetCircle) missiles.get(i).effect.missileTarget;
 					List<Enemy> enemiesInCircle = HitDetector.getEnemiesInCircle(enemies, targetCircle.x1, targetCircle.y1, targetCircle.radius);
 					Iterator<String> it = missiles.get(i).effect.effects.keySet().iterator();
 					while (it.hasNext())
 					{
 						String s = it.next();
 						for (int eT = 0; eT < enemiesInCircle.size(); eT++)
 						{
 							if (missiles.get(i).effect.effects.get(s).b)
 							{
 								enemiesInCircle.get(eT).setStat(s, missiles.get(i).effect.effects.get(s).f);
 							} else
 							{
 								enemiesInCircle.get(eT).editStat(s, missiles.get(i).effect.effects.get(s).f);
 							}
 						}
 					}
 					final ExtendedActor visualEffectActor = new ExtendedActor(missiles.get(i).sprite);
 					TargetCircle tC = (TargetCircle) (missiles.get(i).effect.missileTarget);
 					visualEffectActor.setPosition(tC.x1 - tC.radius, tC.y1 - tC.radius);
 					visualEffectActor.setWidth(tC.radius * 2);
 					visualEffectActor.setHeight(tC.radius * 2);
 					visualEffectActor.addAction(sequence(Actions.fadeOut(0.5f), run(new Runnable()
 					{
 						public void run()
 						{
 							visualEffectActor.remove();
 						}
 					})));
 					stage.addActor(visualEffectActor);
 					assetManager.sounds.get(missiles.get(i).impactSound).play();
 
 				}
 				// ----------------------
 				// If TargetStrategy.CircleOnSelf
 				else if (missiles.get(i).effect.missileTarget.targetingStrategy == TargetingStrategy.CircleOnSelf)
 				{
 					TargetCircleOnSelf targetCircle = (TargetCircleOnSelf) missiles.get(i).effect.missileTarget;
 					List<Enemy> enemiesInCircle = HitDetector.getEnemiesInCircle(enemies, (int) (missiles.get(i).getX() + missiles.get(i).getWidth() / 2), (int) (missiles.get(i).getY() + missiles.get(i).getHeight() / 2), targetCircle.radius);
 					Iterator<String> it = missiles.get(i).effect.effects.keySet().iterator();
 					while (it.hasNext())
 					{
 						String s = it.next();
 						for (int eT = 0; eT < enemiesInCircle.size(); eT++)
 						{
 							if (missiles.get(i).effect.effects.get(s).b)
 							{
 								enemiesInCircle.get(eT).setStat(s, missiles.get(i).effect.effects.get(s).f);
 							} else
 							{
 								enemiesInCircle.get(eT).editStat(s, missiles.get(i).effect.effects.get(s).f);
 							}
 						}
 					}
 				}
 				// ----------------------
 				// If TargetStrategy.Line
 				else if (missiles.get(i).effect.missileTarget.targetingStrategy == TargetingStrategy.Line)
 				{
 					TargetLine targetLine = (TargetLine) missiles.get(i).effect.missileTarget;
 					List<Enemy> enemiesInLine = HitDetector.getEnemiesOnLine(enemies, new Vector2(targetLine.x1, targetLine.y1), new Vector2(targetLine.x2, targetLine.y2));
 					Iterator<String> it = missiles.get(i).effect.effects.keySet().iterator();
 					while (it.hasNext())
 					{
 						String s = it.next();
 						for (int eT = 0; eT < enemiesInLine.size(); eT++)
 						{
 							if (missiles.get(i).effect.effects.get(s).b)
 							{
 								enemiesInLine.get(eT).setStat(s, missiles.get(i).effect.effects.get(s).f);
 							} else
 							{
 								enemiesInLine.get(eT).editStat(s, missiles.get(i).effect.effects.get(s).f);
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
 		
 		if(diggerEnemies.size() > 0)
 		{
 			for(Enemy e : diggerEnemies)
 			{
 				int diggerX = (int)Math.floor(e.getX()/GameConstants.tileSize);
 				int diggerY = (int)Math.floor(e.getY()/GameConstants.tileSize);
 				diggerX = Math.max(0, diggerX);
 				diggerX = Math.min(Map.mapWidth-1, diggerX);
 				diggerY = Math.max(0, diggerY);
 				diggerY = Math.min(Map.mapHeight-1, diggerY);
 				if(!map.pathTiles.contains(map.getMap()[diggerX][diggerY]))
 				{
 					int newTile = map.pathTiles.get(rand.nextInt(map.pathTiles.size()));
 					map.map[diggerX][diggerY] = newTile;
 					map.mapActors[diggerX][diggerY].region = new TextureRegion(map.textures.get(newTile));
 					e.offPath = true;
 					e.lastChanged = new Vector2(diggerX, diggerY);
 				}
 				else
 				{
 					if(e.offPath && e.lastChanged.x != diggerX)
 					{
 						map.generateDirections();
 						if (waveTime.size() > 0)
 						{
 							for(float enemyTimes : waveTime)
 							{
 								if(!diggerEnemies.contains(enemyWave.get(enemyTimes)))
 									enemyWave.get(enemyTimes).generateDirections(map.directions);
 							}
 						}
 						e.offPath = false;
 					}
 				}
 			}
 		}
 		
 		for (int counter = 0; counter < enemies.size(); counter++)
 		{
 			Enemy enemy = enemies.get(counter);
 			if (enemy.getStat("currentHealth") <= 0)
 			{
 				enemies.remove(enemy);
 				currentGold += enemy.getStat("currentGoldYield");
 				hud.goldButton.setText("        " + currentGold);
 				enemy.remove();
 				counter--;
 			} else if (enemy.getActions().size == 0)
 			{
 				if(enemy.willDigg)
 					map.findStartPoint();
 				enemies.remove(enemy);
 				enemy.remove();
 				livesLeft = livesLeft == 0 ? 0 : livesLeft - 1; 
 				hud.livesButton.setText("" + livesLeft);
 				counter--;
 			}
 		}
 	}
 	public boolean isGameWon()
 	{
 		if (enemies.size() <= 0 && enemyWave.size() <= 0)
 			return true;
 		else
 			return false;
 	}
 	public boolean isGameLost()
 	{
 		if (livesLeft <= 0)
 			return true;
 		else
 			return false;
 	}
	public void resetGame()
 	{
		//Ikke gjort enda
 	}
 	private void addEnemyToWave(float time, Enemy enemy)
 	{
 		enemyWave.put(time, enemy);
 		waveTime.add(time);
 	}
 	
 	private Enemy createEnemy(String type, ThinkTank thinkTank, Map map,
 			TextureAtlas enemiesAtlas, TextureAtlas miscAtlas)
 	{
 		List<Direction> directions = new ArrayList<Direction>();
 		boolean digger = false;
 		if(type.equals("digger"))
 		{
 			for(int i = 0; i<=Map.mapWidth; i++)
 				directions.add(Direction.Right);
 			digger = true;
 		}
 		else
 			directions = map.directions;
 		
 		return new Enemy(thinkTank.enemyInfo.get(type), map.startPoint,
 				directions, enemiesAtlas.createSprite(thinkTank.enemyInfo
 						.get(type).enemyTexture),
 				miscAtlas.createSprite("healthBarRed"),
 				miscAtlas.createSprite("healthBarYellow"), miscAtlas.createSprite("super"), miscAtlas.createSprite("slowIcon"), miscAtlas.createSprite("DoTIcon"), digger);
 	}
 	
 	private void doEarthquake(Camera gameCamera, Map map, float nudgeChanceConstant, Sound earthquakeSound)
 	{
 		// Earthquake functionality, can be moved wherever, just to test.
 		if (nudgeRemainingTime > 0)
 		{
 			nudgeRemainingTime -= Gdx.graphics.getDeltaTime();
 			nudgeTimer += Gdx.graphics.getDeltaTime();
 			if (nudgeTimer > 0.1)
 			{
 				if (tempNudge)
 				{
 					gameCamera.position.set(rand.nextInt(40 - 20) + gameCamera.viewportWidth / 2, rand.nextInt(40 - 20) + gameCamera.viewportHeight / 2, 0);
 				} else
 				{
 					gameCamera.position.set(rand.nextInt(40 - 20) + gameCamera.viewportWidth / 2, rand.nextInt(40 - 20) + gameCamera.viewportHeight / 2, 0);
 				}
 				nudgeTimer = 0;
 				if (movesTowers)
 				{
 					for (Tower t : towers)
 					{
 						double d = rand.nextDouble();
 						if (d < 0.1)
 						{
 							if (d < 0.025 && map.canBuild((int) Math.round((t.getX()+GameConstants.tileSize) / GameConstants.tileSize), (int) Math.round(t.getY() / GameConstants.tileSize)))
 							{
 								boolean canMove = true;
 								for (int c = 0; c < towers.size(); c++)
 								{
 									if (towers.get(c).getX() == t.getX()+GameConstants.tileSize && towers.get(c).getY() == t.getY())
 										canMove = false;
 								}
 								if(canMove)
 									t.setPosition(t.getX() + GameConstants.tileSize, t.getY());
 							}
 							else if (d < 0.05 && map.canBuild((int) Math.round((t.getX()-GameConstants.tileSize) / GameConstants.tileSize), (int) Math.round(t.getY() / GameConstants.tileSize)))
 							{
 								boolean canMove = true;
 								for (int c = 0; c < towers.size(); c++)
 								{
 									if (towers.get(c).getX() == t.getX()-GameConstants.tileSize && towers.get(c).getY() == t.getY())
 										canMove = false;
 								}
 								if(canMove)
 								{
 									t.setPosition(t.getX() - GameConstants.tileSize, t.getY());
 								}
 							}
 							else if (d < 0.075 && map.canBuild((int) Math.round(t.getX() / GameConstants.tileSize), (int) Math.round((t.getY()+GameConstants.tileSize) / GameConstants.tileSize)))
 							{
 								boolean canMove = true;
 								for (int c = 0; c < towers.size(); c++)
 								{
 									if (towers.get(c).getX() == t.getX() && towers.get(c).getY() == t.getY()+GameConstants.tileSize)
 										canMove = false;
 								}
 								if(canMove)
 									t.setPosition(t.getX(), t.getY()+GameConstants.tileSize);
 							}
 							else if (map.canBuild((int) Math.round(t.getX() / GameConstants.tileSize), (int) Math.round((t.getY()-GameConstants.tileSize) / GameConstants.tileSize)))
 							{
 								boolean canMove = true;
 								for (int c = 0; c < towers.size(); c++)
 								{
 									if (towers.get(c).getX() == t.getX() && towers.get(c).getY() == t.getY()-GameConstants.tileSize)
 										canMove = false;
 								}
 								if(canMove)
 									t.setPosition(t.getX(), t.getY()-GameConstants.tileSize);
 									
 							}
 						}
 					}
 				}
 			}
 			tempNudge = !tempNudge;
 
 			if (nudgeRemainingTime <= 0)
 			{
 				gameCamera.position.set(gameCamera.viewportWidth / 2, gameCamera.viewportHeight / 2, 0);
 			}
 
 			gameCamera.update();
 
 		} else
 		{
 			nudgeRandomizerTimer -= Gdx.graphics.getDeltaTime();
 			if (nudgeRandomizerTimer <= 0)
 			{
 				if (rand.nextDouble() < nudgeChanceConstant)
 				{
 					nudgeRemainingTime = 1.5f;
 					earthquakeSound.play();
 				}
 				nudgeRandomizerTimer = nudgeRandomizerInterval;
 			}
 		}
 		// ---------------
 	}
 }
