 package com.gamelab.mmi;
 
 import java.awt.List;
 import java.sql.BatchUpdateException;
 import java.util.ArrayList;
 import java.util.Random;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Intersector;
 import com.badlogic.gdx.math.Vector2;
 
 public class GameScreen implements Screen {
 
 	private Mmi game;
 	private GameScreenInputHandler gameScreenInputHandler;
 	private OrthographicCamera camera;
 	private SpriteBatch batch;
 	private Texture texture;
 	private Sprite sprite;
 	private Player player;
 	private Door door;
 	private Random rand = new Random();
 	private Map map;
 	private Button[] buttons = new Button[Player.numberOfTools];
 	private ArrayList<Enemy> enemies = new ArrayList<Enemy>();
 	private PercentagePanel percentagePanel;
 
 	private void createButtons() {
 		buttons[0] = new Button(5, 5, 100, 100, "data/Pinsel.png",
 				new ClickEvent() {
 
 					@Override
 					public void onClick(int x, int y) {
 						for (Button b : buttons) {
 							if (b.getState() == Button.STATE_ACTIVE) {
 								b.setState(Button.STATE_INACTIVE);
 							}
 						}
 						buttons[0].setState(Button.STATE_ACTIVE);
 						player.setTool(Player.TOOL_PIXEL);
 
 					}
 				}, Button.STATE_ACTIVE, 0);
 		buttons[1] = new Button(125, 5, 100, 100, "data/Pinsel.png",
 				new ClickEvent() {
 
 					@Override
 					public void onClick(int x, int y) {
 						for (Button b : buttons) {
 							if (b.getState() == Button.STATE_ACTIVE) {
 								b.setState(Button.STATE_INACTIVE);
 							}
 						}
 						buttons[1].setState(Button.STATE_ACTIVE);
 						player.setTool(Player.TOOL_HUETRALIZER);
 
 					}
 				}, Button.STATE_INACTIVE, 5);
 		buttons[2] = new Button(245, 5, 100, 100, "data/Pinsel.png",
 				new ClickEvent() {
 
 					@Override
 					public void onClick(int x, int y) {
 						for (Button b : buttons) {
 							if (b.getState() == Button.STATE_ACTIVE) {
 								b.setState(Button.STATE_INACTIVE);
 							}
 						}
 						buttons[2].setState(Button.STATE_ACTIVE);
 						player.setTool(Player.TOOL_COLOR_SUCKER);
 
 					}
 				}, Button.STATE_INACTIVE, 1);
 		buttons[3] = new Button(365, 5, 100, 100, "data/Pinsel.png",
 				new ClickEvent() {
 
 					@Override
 					public void onClick(int x, int y) {
 						for (Button b : buttons) {
 							if (b.getState() == Button.STATE_ACTIVE) {
 								b.setState(Button.STATE_INACTIVE);
 							}
 						}
 						buttons[3].setState(Button.STATE_ACTIVE);
 						player.setTool(Player.TOOL_PIXEL_SWAPPER);
 
 					}
 				}, Button.STATE_INACTIVE, 3);
 		buttons[4] = new Button(485, 5, 100, 100, "data/Pinsel.png",
 				new ClickEvent() {
 
 					@Override
 					public void onClick(int x, int y) {
 						for (Button b : buttons) {
 							if (b.getState() == Button.STATE_ACTIVE) {
 								b.setState(Button.STATE_INACTIVE);
 							}
 						}
 						buttons[4].setState(Button.STATE_ACTIVE);
 						player.setTool(Player.TOOL_NEGATRON);
 
 					}
 				}, Button.STATE_INACTIVE, 4);
 		buttons[5] = new Button(605, 5, 100, 100, "data/Pinsel.png",
 				new ClickEvent() {
 
 					@Override
 					public void onClick(int x, int y) {
 						for (Button b : buttons) {
 							if (b.getState() == Button.STATE_ACTIVE) {
 								b.setState(Button.STATE_INACTIVE);
 							}
 						}
 						buttons[5].setState(Button.STATE_ACTIVE);
 						player.setTool(Player.TOOL_WETWIPER);
 
 					}
 				}, Button.STATE_INACTIVE, 2);
 		buttons[6] = new Button(725, 5, 100, 100, "data/Pinsel.png",
 				new ClickEvent() {
 
 					@Override
 					public void onClick(int x, int y) {
 						for (Button b : buttons) {
 							if (b.getState() == Button.STATE_ACTIVE) {
 								b.setState(Button.STATE_INACTIVE);
 							}
 						}
 						buttons[6].setState(Button.STATE_ACTIVE);
 						player.setTool(Player.TOOL_WALK);
 
 					}
 				}, Button.STATE_INACTIVE, 8);
 
 	}
 
 	public GameScreen(Mmi game, String file) {
 		this.game = game;
 		float w = Gdx.graphics.getWidth();
 		float h = Gdx.graphics.getHeight();
 
 		createButtons();
 
 		map = new Map(file);
 		camera = new OrthographicCamera(1, h / w);
 		batch = new SpriteBatch();
<<<<<<< HEAD
		door=new Door();
		door.activate(new Vector2(Math.abs(rand.nextInt())%(w-Door.SIZE*2)+Door.SIZE,Math.abs(rand.nextInt())%(h-Door.SIZE*2)+Door.SIZE));
		door.deactivate();
=======
 		door = new Door();
 		door.activate(new Vector2(Math.abs(rand.nextInt())
 				% (w - Door.SIZE * 2) + Door.SIZE, Math.abs(rand.nextInt())
 				% (h - Door.SIZE * 2) + Door.SIZE));
 
>>>>>>> 4bcf3affbf68b1c2d749a1570cdcaa52650fea02
 		texture = new Texture(Gdx.files.internal(file));
 		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 
 		TextureRegion region = new TextureRegion(texture, 0, 0, 512, 275);
 
 		sprite = new Sprite(region);
 		sprite.setSize(0.9f, 0.9f * sprite.getHeight() / sprite.getWidth());
 		sprite.setOrigin(sprite.getWidth() / 2, sprite.getHeight() / 2);
 		sprite.setPosition(-sprite.getWidth() / 2, -sprite.getHeight() / 2);
 
 		player = new Player(new Vector2(w / 2, h / 2), Player.TOOL_PIXEL, map);
 
 		gameScreenInputHandler = new GameScreenInputHandler(this, player);
 		for (Button b : buttons) {
 			gameScreenInputHandler.addEvent(b.getOnClick());
 		}
 
 		Gdx.input.setInputProcessor(gameScreenInputHandler);
 
 		percentagePanel = new PercentagePanel();
 	}
 
 	@Override
 	public void render(float delta) {
 		update(delta);
 		Gdx.gl.glClearColor(1, 1, 1, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		batch.begin();
 
 		map.getMapPh().sprite.draw(batch);
 		batch.end();
 
 		player.render();
 		for (Enemy e : enemies) {
 			e.render();
 		}
 		door.render();
 		for (Button b : buttons) {
 			b.render();
 		}
 		
 		percentagePanel.render(Integer.toString((int) (100 * map.getRelativeTouched())) + "%");
 	}
 
 	private void addEnemy(int enemy) {
 		float randWidth = rand.nextFloat();
 		float randHeight = rand.nextFloat();
 		if (Math.abs(randWidth - 0.5f) < Math.abs(randHeight - 0.5f)) {
 			if (randWidth >= 0) {
 				randWidth = 1;
 			} else {
 				randWidth = 0;
 			}
 		} else {
 			if (randHeight >= 0) {
 				randHeight = 1;
 			} else {
 				randHeight = 0;
 			}
 		}
 		enemies.add(new Enemy(new Vector2(randWidth * Gdx.graphics.getWidth(),
 				randHeight * Gdx.graphics.getHeight()), enemy, map));
 	}
 
 	private void disposeEnemy() {
 		enemies.remove(enemies.size() - 1).dispose();
 	}
 
 	private void updateEnemies(float delta) {
 		if (map.getRelativeTouched() > 0.01f && enemies.size() < 1) {
 			addEnemy(Enemy.Hipster1Enemy);
 		} else if (map.getRelativeTouched() > 0.03f && enemies.size() < 2) {
 			addEnemy(Enemy.Hipster2Enemy);
 		} else if (map.getRelativeTouched() > 0.04f && enemies.size() < 3) {
 			addEnemy(Enemy.SpiesserEnemy);
 		}
 
 		if (map.getRelativeTouched() <= 0.003f && enemies.size() >= 3) {
 			disposeEnemy();
 		} else if (map.getRelativeTouched() <= 0.002f && enemies.size() >= 2) {
 			disposeEnemy();
 		} else if (map.getRelativeTouched() <= 0.001f && enemies.size() >= 1) {
 			disposeEnemy();
 		}
 
 		for (Enemy e : enemies) {
 			e.update(delta);
 		}
 	}
 
 	public void update(float delta) {
 		updateEnemies(delta);
 		player.update(delta);
<<<<<<< HEAD
 		if(!door.isActive()&&map.getRelativeTouched()>=0.39){
 			door.activate(new Vector2(Math.abs(rand.nextInt())%(Gdx.graphics.getWidth()-Door.SIZE*2)+Door.SIZE,Math.abs(rand.nextInt())%(Gdx.graphics.getHeight()-Door.SIZE*2)+Door.SIZE));
 			
 		}
 		if(map.getRelativeTouched()<0.3){
 			door.deactivate();
 		}
 		
 		if(door.isActive()&&Intersector.overlapCircleRectangle(player.getHitbox(),door.getHitbox())){
=======
		if (door.isActive()
				&& Intersector.overlapCircleRectangle(player.getHitbox(),
						door.getHitbox())) {
>>>>>>> 4bcf3affbf68b1c2d749a1570cdcaa52650fea02
 			game.nextLevel();
 		}
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void show() {
 		// TODO Auto-generated method stub
 
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
 		batch.dispose();
 		texture.dispose();
 
 		player.dispose();
 	}
 
 }
