 package com.valleskeyp.WebDefender.screens;
 
 import java.util.HashMap;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.g2d.Animation;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
 import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeBitmapFontData;
 import com.badlogic.gdx.math.Intersector;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.collision.Ray;
 import com.badlogic.gdx.utils.Array;
 import com.valleskeyp.WebDefender.Fly;
 import com.valleskeyp.WebDefender.GoogleInterface;
 import com.valleskeyp.WebDefender.Web;
 
 public class GameScreen implements Screen, InputProcessor {
 	private OrthographicCamera camera;
 	private SpriteBatch batch;
 	private Texture texture;
 	private Sprite sprite, leaf_back, spider, shadow, bigLeaf, easy, medium, hard, chooseDifficulty, playAgain, quitGame, scoreButton;
 	private boolean move_spider = false, isMoving = false, isFighting = false, loggedIn = false, survived = false, survivedMedium = false, survivedHard = false;
 	public Array<HashMap<String, Float>> coord = new Array<HashMap<String, Float>>();
 	public Array<Fly> flies;
 	public Array<Web> webbing;
 	
 	float spawn_timer = 0, fight_timer = 999, totalTime = 0, spiderTime = 0, modifier = 6;
 	
 	private Array<HashMap<String, Float>> flySlots;
 	private Array<Integer> flyCheck;
 	private int fliesEscaped, fightingFlySlot, difficulty = 0, score = 0, smallFly = 0, mediumFly = 0, largeFly = 0, timesFought = 0;
 	GoogleInterface platformInterface;
 	FreeTypeBitmapFontData font;
 	private BitmapFont scoreText;
 	private Animation spiderAnimation, fightAnimation;
 	private float fightingX, fightingY;
 	
 	public GameScreen(GoogleInterface aInterface) {
 		platformInterface = aInterface;
 		loggedIn = platformInterface.getSignedIn();
 	}
 
 
 	@Override
 	public void show() {
 		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("data/PermanentMarker.ttf"));
 		font = generator.generateData(45);
 		generator.dispose();
 		
 		scoreText = new BitmapFont(font, font.getTextureRegion(), false);
 		scoreText.setColor(0, 0, 0, 1);
 		
 		float w = Gdx.graphics.getWidth();
 		float h = Gdx.graphics.getHeight();
 		
 		camera = new OrthographicCamera(1000*1, 1000*h/w);
 		batch = new SpriteBatch();
 		Gdx.input.setInputProcessor(this);
 		Gdx.input.setCatchBackKey(true);
 		
 		createTextures();
 		
 		flyCheck = new Array<Integer>();
 		flies = new Array<Fly>();
 		webbing = new Array<Web>();
 		
 		makeWeb();
 		makeFlySlots();
 		makeAnimations();
 	}
 	
 	@Override
 	public void render(float delta) {
 		if (loggedIn) {
 			if (smallFly > 5 && loggedIn) {
 				platformInterface
 						.incrementAchievement("ach_smallFry", smallFly);
 				smallFly = 0;
 			} else if (mediumFly > 5 && loggedIn) {
 				platformInterface.incrementAchievement("ach_middleMan",
 						mediumFly);
 				mediumFly = 0;
 			} else if (largeFly > 5 && loggedIn) {
 				platformInterface.incrementAchievement("ach_heavyLift",
 						largeFly);
 				largeFly = 0;
 			} else if (timesFought > 5 && loggedIn) {
 				platformInterface.incrementAchievement("ach_brawlin",
 						timesFought);
 				timesFought = 0;
 			}
 		}
 		Gdx.gl.glClearColor(0, 0, 0, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		float dt = Gdx.graphics.getDeltaTime();
 		batch.setProjectionMatrix(camera.combined);
 		
 		
 		batch.begin();
 		
 		if (difficulty > 0) {
 			totalTime += dt;
 			if (totalTime > 60 && loggedIn) {
 				if (difficulty == 1 && survived != true) {
 					platformInterface.unlockAchievement("ach_survive");
 					survived = true;
 				} else if (difficulty == 2 && survivedMedium != true) {
 					platformInterface.unlockAchievement("ach_surviveMedium");
 					survivedMedium = true;
 				} else if (difficulty == 3 && survivedHard != true) {
 					platformInterface.unlockAchievement("ach_surviveHard");
 					survivedHard = true;
 				}
 			}
 			if (fliesEscaped < (6 - difficulty)) {     										//----// NORMAL GAMEPLAY
 				spawn_timer += dt;
 				if (spawn_timer >= (modifier - difficulty)) {
 					modifier -= dt;
 					if (modifier <= difficulty + 1) {
 						modifier = difficulty +1;
 					}
 					Gdx.app.log("MODIFIER", ""+modifier);
 					spawn_timer = 0;
 					spawnFly();
 					spawnFly();
 				}
 
 				if (flies.size < 2) {
 					int spawnNumber = (int) (Math.random() * 2 + 1);
 					for (int i = 0; i < spawnNumber; i++) {
 						spawnFly();
 					}
 				}
 				leaf_back.draw(batch);
 				sprite.draw(batch);
 				
 				//  ----------------------------------------------------------------------------------------------- DRAW SCORE
 				scoreText.setFixedWidthGlyphs("Score: " + score);
 				scoreText.draw(batch, "Score: " + score, -scoreText.getBounds("Score: " + score).width/2, -23);
 				
 				for (Web web : webbing) {// draw web squares
 					web.draw(batch, dt);
 				}
 				if (fightAnimation.isAnimationFinished(fight_timer)) {
 					if (isFighting) {
 						isFighting = false;
 						flyCheck.removeValue(fightingFlySlot, true);
 					}
 					if (!isMoving) {
 						spider.draw(batch);
 					} else {
 						isMoving = false;
 						batch.draw(spiderAnimation.getKeyFrame(spiderTime += dt*4.5), spider.getX(), spider.getY(), 1000*0.128f, 1000*0.1f);
 					}
 				} else {
 					batch.draw(fightAnimation.getKeyFrame(fight_timer += dt*3), fightingX - 50, fightingY - 50, 1000*.2f, 1000*.17f);
 				}
 				if (flies.size > 0) {
 					for (Fly fly : flies) {// draw flies
 						fly.draw(batch, dt);
 					}
 					for (Fly fly : flies) {
 						Sprite sp = fly.spriteReturn();
 						if (!isFighting && !fly.escaped && Intersector.overlaps(spider.getBoundingRectangle(), sp.getBoundingRectangle())) {
 							for (Web web : webbing) {
 								if (web.slotNumber == fly.slotNumber) {
 									for (int flySlot : flyCheck) {
 										if (flySlot == fly.slotNumber) {
 											isFighting = true;
 											fight_timer = 0;
 											move_spider = false;
 											fightingX = fly.xCoord;
 											fightingY = fly.yCoord;
 											batch.draw(fightAnimation.getKeyFrame(fight_timer += dt*3), fightingX - 50, fightingY - 50, 1000*.2f, 1000*.17f);
 											fightingFlySlot = flySlot;
 										}
 									}
 									web.hasFly = false;
 								}
 							}
 							
 							switch (fly.fly_size) {
 							case 1: // small fly
 								score += (int) ((totalTime/5) + 1)  * (5 * difficulty);
 								smallFly += 1;
 								break;
 							case 2: // small fly
 								score += (int) ((totalTime/5) + 1) * (5 * difficulty);
 								smallFly += 1;
 								break;
 							case 3: // medium fly
 								score += (int) ((totalTime/5) + 1) * (10 * difficulty);
 								mediumFly += 1;
 								break;
 							case 4: // large fly
 								score += (int) ((totalTime/5) + 1) * (15 * difficulty);
 								largeFly += 1;
 								break;
 							case 5: // medium fly
 								score += (int) ((totalTime/5) + 1) * (10 * difficulty);
 								mediumFly += 1;
 								break;
 							case 6: // medium fly
 								score += (int) ((totalTime/5) + 1) * (15 * difficulty);
 								largeFly += 1;
 								break;
 							default:
 								break;
 							}
 							timesFought += 1;
 							flies.removeValue(fly, true);
 						} else if (fly.escaped) {
 							fliesEscaped += 1;
 							for (Web web : webbing) {
 								if (web.slotNumber == fly.slotNumber) {
 									web.breakWeb();
 								}
 							}
 							fly.texture.dispose();
 							flies.removeValue(fly, true);
 						}
 					}
 				}
 			} else { 																			//----// GAME OVER
 				if (totalTime > 0) {
 					if (loggedIn) {
 						if (score > 0) {
 							platformInterface.submitScore(score);
 						}
 						if (smallFly > 0 && loggedIn) {
 							platformInterface.incrementAchievement("ach_smallFry", smallFly);
 							smallFly = 0;
 						}
 						if (mediumFly > 0 && loggedIn) {
 							platformInterface.incrementAchievement("ach_middleMan", mediumFly);
 							mediumFly = 0;
 						}
 						if (largeFly > 0 && loggedIn) {
 							platformInterface.incrementAchievement("ach_heavyLift", largeFly);
 							largeFly = 0;
 						}
 						if (timesFought > 0 && loggedIn) {
 							platformInterface.incrementAchievement("ach_brawlin", timesFought);
 							timesFought = 0;
 						}
 
 						if (smallFly > 0) {
 							platformInterface.incrementAchievement("ach_smallFry", smallFly);
 						}
 						if (mediumFly > 0) {
 							platformInterface.incrementAchievement("ach_middleMan", mediumFly);
 						}
 						if (largeFly > 0) {
 							platformInterface.incrementAchievement("ach_heavyLift", largeFly);
 						}
 						if (timesFought > 0) {
 							platformInterface.incrementAchievement("ach_brawlin", timesFought);
 						}
 						smallFly = 0;
 						mediumFly = 0;
 						largeFly = 0;
 						timesFought = 0;
 					}
 				}
				if (fliesEscaped > 0) {
 					modifier = 6;
 					totalTime = 0;
 					for (Fly fly : flies) {
 						fly.texture.dispose();
 					}
 					for (Web web : webbing) {
 						web.texture.dispose();
 					}
 					flies.clear();
 					webbing.clear();
 					flyCheck.clear();
 					makeWeb();
 					makeFlySlots();
 					spider.setPosition(-spider.getWidth()/2, -spider.getHeight()/2);
 					move_spider = false; // moved the auto start over glitch out into START new game.  still bugged?
 				}
 				
 																// drawing game over
 				
 				leaf_back.draw(batch);
 				sprite.draw(batch);
 				for (Web web : webbing) {// draw web squares
 					web.draw(batch, dt);
 				}
 				spider.draw(batch);
 				shadow.draw(batch);
 				bigLeaf.draw(batch);
 				playAgain.draw(batch);
 				quitGame.draw(batch);
 				if (loggedIn) {
 					scoreButton.draw(batch);
 				}
 				scoreText.setFixedWidthGlyphs("" + score);
 				scoreText.draw(batch, "" + score, -scoreText.getBounds(""+score).width/2, 120);
 			}
 		} else {
 			leaf_back.draw(batch);
 			sprite.draw(batch);
 			for (Web web : webbing) {// draw web squares
 				web.draw(batch, dt);
 			}
 			shadow.draw(batch);
 			chooseDifficulty.draw(batch);
 			easy.draw(batch);
 			medium.draw(batch);
 			hard.draw(batch);
 		}
 		batch.end();
 	}
 
 	@Override
 	public void pause() {
 		// TODO pause
 	}
 	
 
 	@Override
 	public void dispose() {
 		if (loggedIn) {
 			platformInterface.submitScore(score);
 			platformInterface.incrementAchievement("ach_smallFry", smallFly);
 			platformInterface.incrementAchievement("ach_middleMan", mediumFly);
 			platformInterface.incrementAchievement("ach_heavyLift", largeFly);
 			platformInterface.incrementAchievement("ach_brawlin", timesFought);
 		}
 		batch.dispose();
 		texture.dispose();
 		scoreText.dispose();
 		for (Fly fly : flies) {
 			fly.texture.dispose();
 		}
 		for (Web web : webbing) {
 			web.texture.dispose();
 		}
 		
 	}
 
 	@Override
 	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 		Vector2 touchPos = new Vector2();
         touchPos.set(Gdx.input.getX(), Gdx.input.getY());
 
         Ray cameraRay = camera.getPickRay(touchPos.x, touchPos.y);
         if (!isFighting && fliesEscaped < (6 - difficulty) && spider.getBoundingRectangle().contains(cameraRay.origin.x, cameraRay.origin.y)) {
         	move_spider = true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
 		Vector2 touchPos = new Vector2();
         touchPos.set(Gdx.input.getX(), Gdx.input.getY());
         
         Ray cameraRay = camera.getPickRay(touchPos.x, touchPos.y);
 		if (fliesEscaped > (5 - difficulty) && playAgain.getBoundingRectangle().contains(cameraRay.origin.x, cameraRay.origin.y)) {
 			score = 0;
 			difficulty = 0;
 			fliesEscaped = 0;
 		}
 		if (fliesEscaped > (5 - difficulty) && quitGame.getBoundingRectangle().contains(cameraRay.origin.x, cameraRay.origin.y)) {
 			((Game) Gdx.app.getApplicationListener()).setScreen(new MenuScreen(platformInterface));
 		}
 		if (fliesEscaped > (5 - difficulty) && loggedIn && scoreButton.getBoundingRectangle().contains(cameraRay.origin.x, cameraRay.origin.y)) {
 			platformInterface.getScores();
 		}
 		if (difficulty == 0 && easy.getBoundingRectangle().contains(cameraRay.origin.x, cameraRay.origin.y)) {
 			difficulty = 1;
 		} else if (difficulty == 0 && medium.getBoundingRectangle().contains(cameraRay.origin.x, cameraRay.origin.y)) {
 			difficulty = 2;
 		} else if (difficulty == 0 && hard.getBoundingRectangle().contains(cameraRay.origin.x, cameraRay.origin.y)) {
 			difficulty = 3;
 		}
 		
 		move_spider = false;
 		return false;
 	}
 	
 	@Override
 	public boolean keyDown(int keycode) {
 		if(keycode == Keys.BACK) {
 			((Game) Gdx.app.getApplicationListener()).setScreen(new MenuScreen(platformInterface));
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean touchDragged(int screenX, int screenY, int pointer) {
 		if (move_spider) {
 			isMoving = true;
 			Vector2 touchPos = new Vector2();
 			touchPos.set(Gdx.input.getX(), Gdx.input.getY());
 			Ray cameraRay = camera.getPickRay(touchPos.x, touchPos.y);
 			
 			spider.setX(cameraRay.origin.x - (spider.getWidth()/2));
 			spider.setY(cameraRay.origin.y - (spider.getWidth()/2));
 		}
 		return false;
 	}
 	
 	private void spawnFly() {
 		int flySize;
 		if (difficulty == 1) {
 			flySize = (int) (Math.random() * 4) + 1;
 		} else if (difficulty == 2) {
 			flySize = (int) (Math.random() * 4) + 2;
 		} else {
 			flySize = (int) (Math.random() * 4) + 3;
 		}
 		
 		int slotChosen = 0;
 		if (flyCheck.size > 0 && flies.size < 10) {
 			boolean notFound = true;
 			while (notFound) {
 				slotChosen = (int) (Math.random() * 10);
 				notFound = false;
 				for (int flySlot : flyCheck) {
 					if (flySlot == slotChosen) {
 						notFound = true;
 					}
 				}
 			}
 			for (Web web_ : webbing) {
 				if (web_.slotNumber == slotChosen) {
 					Fly fly = new Fly(flySlots.get(slotChosen).get("x"), flySlots.get(slotChosen).get("y"), flySize, slotChosen);
 					web_.hasFly = true;
 					flyCheck.add(slotChosen);
 					flies.add(fly);
 				}
 			}
 		} else if(flyCheck.size == 0 && flies.size < 10) {
 			slotChosen = (int) (Math.random() * 10);
 			for (Web web_ : webbing) {
 				if (web_.slotNumber == slotChosen) {
 					Fly fly = new Fly(flySlots.get(slotChosen).get("x"), flySlots.get(slotChosen).get("y"), 2, slotChosen);
 					web_.hasFly = true;
 					flyCheck.add(slotChosen);
 					flies.add(fly);
 				}
 			}
 		}
 	}
 	
 	private void makeWeb() {		
 		Web web = new Web(1000*-.5f, 1000*.1f, "top", 0);
 		webbing.add(web);
 		web = new Web(1000*-.3f, 1000*.1f, "top", 1);
 		webbing.add(web);
 		web = new Web(1000*-.1f, 1000*.1f, "top", 2);
 		webbing.add(web);
 		web = new Web(1000*.0995f, 1000*.1f, "top", 3);
 		webbing.add(web);
 		web = new Web(1000*.299f, 1000*.1f, "top", 4);
 		webbing.add(web);
 		
 		web = new Web(1000*-.5f, 1000*-.299f, "bottom", 5);
 		webbing.add(web);
 		web = new Web(1000*-.3f, 1000*-.299f, "bottom", 6);
 		webbing.add(web);
 		web = new Web(1000*-.1f, 1000*-.299f, "bottom", 7);
 		webbing.add(web);
 		web = new Web(1000*.0995f, 1000*-.299f, "bottom", 8);
 		webbing.add(web);
 		web = new Web(1000*.299f, 1000*-.299f, "bottom", 9);
 		webbing.add(web);
 	}
 	
 	private void makeFlySlots() {
 		flySlots = new Array<HashMap<String, Float>>();
 		
 		HashMap<String, Float> hm = new HashMap<String, Float>(); // top
 		
 		hm.put("x", 1000*-.455f);
 		hm.put("y", 1000*.143f);
 		flySlots.add(hm);
 		
 		hm = new HashMap<String, Float>();
 		
 		hm.put("x", 1000*-.255f);
 		hm.put("y", 1000*.143f);
 		flySlots.add(hm);
 		
 		hm = new HashMap<String, Float>();
 		
 		hm.put("x", 1000*-.055f);
 		hm.put("y", 1000*.143f);
 		flySlots.add(hm);
 		
 		hm = new HashMap<String, Float>();
 
 		hm.put("x", 1000*.145f);
 		hm.put("y", 1000*.143f);
 		flySlots.add(hm);
 
 		hm = new HashMap<String, Float>();
 
 		hm.put("x", 1000*.345f);
 		hm.put("y", 1000*.143f);
 		flySlots.add(hm);
 		
 		hm = new HashMap<String, Float>(); // bottom
 		
 		hm.put("x", 1000*-.455f);
 		hm.put("y", 1000*-.243f);
 		flySlots.add(hm);
 		
 		hm = new HashMap<String, Float>();
 		
 		hm.put("x", 1000*-.255f);
 		hm.put("y", 1000*-.243f);
 		flySlots.add(hm);
 		
 		hm = new HashMap<String, Float>();
 		
 		hm.put("x", 1000*-.055f);
 		hm.put("y", 1000*-.243f);
 		flySlots.add(hm);
 		
 		hm = new HashMap<String, Float>();
 
 		hm.put("x", 1000*.145f);
 		hm.put("y", 1000*-.243f);
 		flySlots.add(hm);
 
 		hm = new HashMap<String, Float>();
 
 		hm.put("x", 1000*.345f);
 		hm.put("y", 1000*-.243f);
 		flySlots.add(hm);
 	}
 	
 	private void createTextures() {
 		texture = new Texture(Gdx.files.internal("data/middle_web.png"));
 		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		
 		TextureRegion region = new TextureRegion(texture, 0, 0, 800, 480);
 		
 		sprite = new Sprite(region);
 		sprite.setSize(1000*1f, 1000*1f * sprite.getHeight() / sprite.getWidth());
 		sprite.setOrigin(sprite.getWidth()/2, sprite.getHeight()/2);
 		sprite.setPosition(-sprite.getWidth()/2, -sprite.getHeight()/2);
 		
 		texture = new Texture(Gdx.files.internal("data/background.png"));
 		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		
 		region = new TextureRegion(texture, 0, 0, 800, 480);
 		
 		leaf_back = new Sprite(region);
 		leaf_back.setSize(1000*1f, 1000*1f * leaf_back.getHeight() / leaf_back.getWidth());
 		leaf_back.setOrigin(leaf_back.getWidth()/2, leaf_back.getHeight()/2);
 		leaf_back.setPosition(-leaf_back.getWidth()/2, -leaf_back.getHeight()/2);
 		
 		texture = new Texture(Gdx.files.internal("data/spider.png"));
 		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		
 		region = new TextureRegion(texture, 0, 0, 128, 100);
 		
 		spider = new Sprite(region);
 		spider.setSize(1000*0.128f, 1000*0.1f);
 		spider.setOrigin(spider.getWidth()/2, spider.getHeight()/2);
 		spider.setPosition(-spider.getWidth()/2, -spider.getHeight()/2);
 		
 		texture = new Texture(Gdx.files.internal("data/shadow.png"));
 		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		
 		region = new TextureRegion(texture, 0, 0, 800, 480);
 		
 		shadow = new Sprite(region);
 		shadow.setSize(1000*1f, 1f * 1000*shadow.getHeight() / shadow.getWidth());
 		shadow.setOrigin(shadow.getWidth()/2, shadow.getHeight()/2);
 		shadow.setPosition(-shadow.getWidth()/2, -shadow.getHeight()/2);
 		
 		texture = new Texture(Gdx.files.internal("data/bigLeaf.png"));
 		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 
 		region = new TextureRegion(texture, 0, 0, 512, 512);
 
 		bigLeaf = new Sprite(region);
 		bigLeaf.setSize(1000*.512f, 1000*.512f);
 		bigLeaf.setOrigin(bigLeaf.getWidth()/2, bigLeaf.getHeight()/2);
 		bigLeaf.setPosition(-bigLeaf.getWidth()/2, -bigLeaf.getHeight()/2);
 		
 		texture = new Texture(Gdx.files.internal("data/playAgain.png"));
 		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 
 		region = new TextureRegion(texture, 0, 0, 128, 64);
 
 		playAgain = new Sprite(region);
 		playAgain.setSize(1000*.128f, 1000*.064f);
 		playAgain.setOrigin(playAgain.getWidth()/2, playAgain.getHeight()/2);
 		playAgain.setPosition(-playAgain.getWidth()/2, -25f);
 		
 		texture = new Texture(Gdx.files.internal("data/quitGame.png"));
 		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 
 		region = new TextureRegion(texture, 0, 0, 128, 64);
 
 		quitGame = new Sprite(region);
 		quitGame.setSize(1000*.128f, 1000*.064f);
 		quitGame.setOrigin(quitGame.getWidth()/2, quitGame.getHeight()/2);
 		quitGame.setPosition(-quitGame.getWidth()/2, -140f);
 		
 		texture = new Texture(Gdx.files.internal("data/scoreButton.png"));
 		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 
 		region = new TextureRegion(texture, 0, 0, 128, 64);
 
 		scoreButton = new Sprite(region);
 		scoreButton.setSize(1000*.128f, 1000*.064f);
 		scoreButton.setOrigin(scoreButton.getWidth()/2, scoreButton.getHeight()/2);
 		scoreButton.setPosition(-scoreButton.getWidth()/2, 130f);
 		
 		texture = new Texture(Gdx.files.internal("data/easyButton.png"));
 		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		
 		region = new TextureRegion(texture, 0, 0, 128, 64);
 		
 		easy = new Sprite(region);
 		easy.setSize(1000*.16f, 1000*.08f);
 		easy.setOrigin(easy.getWidth()/2, easy.getHeight()/2);
 		easy.setPosition(1000*-.24f, 1000*-.116f);
 		
 		texture = new Texture(Gdx.files.internal("data/mediumButton.png"));
 		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		
 		region = new TextureRegion(texture, 0, 0, 128, 64);
 		
 		medium = new Sprite(region);
 		medium.setSize(1000*.16f, 1000*.08f);
 		medium.setOrigin(medium.getWidth()/2, medium.getHeight()/2);
 		medium.setPosition(1000*-.064f, 1000*-.116f);
 		
 		texture = new Texture(Gdx.files.internal("data/hardButton.png"));
 		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		
 		region = new TextureRegion(texture, 0, 0, 128, 64);
 		
 		hard = new Sprite(region);
 		hard.setSize(1000*.16f, 1000*.08f);
 		hard.setOrigin(hard.getWidth()/2, hard.getHeight()/2);
 		hard.setPosition(1000*.112f, 1000*-.116f);
 		
 		texture = new Texture(Gdx.files.internal("data/chooseDifficulty.png"));
 		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		
 		region = new TextureRegion(texture, 0, 0, 512, 128);
 		
 		chooseDifficulty = new Sprite(region);
 		chooseDifficulty.setSize(1000*.512f, 1000*.13f);
 		chooseDifficulty.setOrigin(chooseDifficulty.getWidth()/2, chooseDifficulty.getHeight()/2);
 		chooseDifficulty.setPosition(-chooseDifficulty.getWidth()/2+1000*.005f, 1000*.1f);
 	}
 	
 	private void makeAnimations() {
 		fightAnimation = new Animation(1/10f,
 				new TextureRegion(new Texture(Gdx.files.internal("data/fightAnimation/fightCloud0.png")), 0, 0, 200, 170),
 				new TextureRegion(new Texture(Gdx.files.internal("data/fightAnimation/fightCloud1.png")), 0, 0, 200, 170),
 				new TextureRegion(new Texture(Gdx.files.internal("data/fightAnimation/fightCloud2.png")), 0, 0, 200, 170),
 				new TextureRegion(new Texture(Gdx.files.internal("data/fightAnimation/fightCloud3.png")), 0, 0, 200, 170),
 				new TextureRegion(new Texture(Gdx.files.internal("data/fightAnimation/fightCloud4.png")), 0, 0, 200, 170),
 				new TextureRegion(new Texture(Gdx.files.internal("data/fightAnimation/fightCloud5.png")), 0, 0, 200, 170),
 				new TextureRegion(new Texture(Gdx.files.internal("data/fightAnimation/fightCloud6.png")), 0, 0, 200, 170),
 				new TextureRegion(new Texture(Gdx.files.internal("data/fightAnimation/fightCloud7.png")), 0, 0, 200, 170),
 				new TextureRegion(new Texture(Gdx.files.internal("data/fightAnimation/fightCloud8.png")), 0, 0, 200, 170),
 				new TextureRegion(new Texture(Gdx.files.internal("data/fightAnimation/fightCloud9.png")), 0, 0, 200, 170)
 		);
 		spiderAnimation = new Animation(1/4f,
 				new TextureRegion(new Texture(Gdx.files.internal("data/spiderAnimation/spiderAnimation0.png")), 0, 0, 128, 100),
 				new TextureRegion(new Texture(Gdx.files.internal("data/spiderAnimation/spiderAnimation1.png")), 0, 0, 128, 100),
 				new TextureRegion(new Texture(Gdx.files.internal("data/spiderAnimation/spiderAnimation2.png")), 0, 0, 128, 100),
 				new TextureRegion(new Texture(Gdx.files.internal("data/spiderAnimation/spiderAnimation3.png")), 0, 0, 128, 100)
 		);
 		spiderAnimation.setPlayMode(Animation.LOOP);
 	}
 	
 	@Override
 	public void resume() {
 		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("data/PermanentMarker.ttf"));
 		font = generator.generateData(50);
 		generator.dispose();
 		
 		scoreText = new BitmapFont(font, font.getTextureRegion(), false);
 		scoreText.setColor(00, 300, 00, 1);
 	}
 	
 	// Unused methods --------------------------------------------------------------------------------------------------------
 	
 	
 	
 	@Override
 	public boolean keyUp(int keycode) {
 		return false;
 	}
 	
 	@Override
 	public boolean keyTyped(char character) {
 		return false;
 	}
 	
 	@Override
 	public boolean mouseMoved(int screenX, int screenY) {
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int amount) {
 		return false;
 	}
 
 	@Override
 	public void resize(int width, int height) {
 	}
 	
 	@Override
 	public void hide() {
 	}
 }
