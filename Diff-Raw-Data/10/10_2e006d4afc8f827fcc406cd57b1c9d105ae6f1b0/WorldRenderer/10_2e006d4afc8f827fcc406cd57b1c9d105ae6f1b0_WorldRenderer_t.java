 package com.ai.ant;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Rectangle;
 
 
 
 /*
  * Block x = 0:6 (on the left side are left for the menu derek)
  * Actual game play will be from x = 6:49
  * 
  */
 
 
 
 public class WorldRenderer {
 
 	private static final float CAMERA_WIDTH = 10f;
 	private static final float CAMERA_HEIGHT = 10f; //unit control
 	
 	private World world;
 	private OrthographicCamera cam; //not sure 
 	
 	private int width;
 	private int height;
 	
 	private float ppuX;
 	private float ppuY;
 	
 	ShapeRenderer debugRenderer = new ShapeRenderer();
 	SpriteBatch batch = new SpriteBatch();
 	private Texture queenAnt;
 	private Texture soldierAnt;
 	private Texture redAnt;
 	private Texture carpenterAnt;
 	private Texture flyingAnt;
 	private Texture grass;
 	
 
 	//towers bullets
 	private Texture spawnTower;
 	private Texture basicTower, basicBullet;
 	private Texture splashTower, splashBullet;
 	private Texture slowTower, slowBullet;
 	private Texture stunTower, stunBullet;
 	private Texture conversionTower, conversionBullet;
 	private Texture waterPuddle, mudPuddle, money, upgrade;
 	
 	private Texture wall;
 	private Texture menuBackground;
 	private Texture quitButton;
 	
 	private Texture homeBase;
 	
 	Player player;
 	BitmapFont font;
 	
 	Map<Integer, Texture> towerMapTexture = new HashMap<Integer, Texture>();
 	
 	public WorldRenderer(World world) {
 		this.world = world;
 		this.cam = new OrthographicCamera(50,50);
 		this.cam.position.set(25, 25, 0);
 		this.cam.update();
 		batch = new SpriteBatch();
 		font = new BitmapFont();
 		loadTextures();
 		buildTextureMap();
 		
 	}
 	
 	public void render() {
 		batch.begin();
 		drawBlocks();
 		drawTowers();
 		drawBullets();
 		drawMobs();
 		//loadCharacter();	
 		
 		//home base
 		batch.draw(homeBase, 90, 210, 75, 75);
 		
 		drawMenu();
 		drawPlayer();
 		batch.end();
 		//drawDebug();
 	}
 	
 	public void setSize(int w, int h) {
 		this.width = w;
 		this.height = h;
 		ppuX = (float) width/ CAMERA_WIDTH;
 		ppuY = (float) height/ CAMERA_HEIGHT;
 	}
 	
 	
 	public void loadTextures() {
 		//ants
 		queenAnt = new Texture(Gdx.files.internal("QueenAnt.png"));
 		soldierAnt = new Texture(Gdx.files.internal("soldierAnt.png"));
 		redAnt = new Texture(Gdx.files.internal("redAnt.png"));
 		carpenterAnt = new Texture(Gdx.files.internal("carpenterAnt.png"));;
 		flyingAnt = new Texture(Gdx.files.internal("flyingAnt.png"));;
 		
 		//Towers and bullets
 		spawnTower = new Texture(Gdx.files.internal("hill_spawning.png"));
 
 		basicTower = new Texture(Gdx.files.internal("tower.png")); //TODO
 		basicBullet = new Texture(Gdx.files.internal("basicBullet.png"));	
 		
 		splashTower = new Texture(Gdx.files.internal("splash_tower.png"));
 		splashBullet = new Texture(Gdx.files.internal("splashBullet.png"));//TODO
 		
 		slowTower = new Texture(Gdx.files.internal("stunFreezeHill.png")); //TODO
 		slowBullet = new Texture(Gdx.files.internal("basicBullet.png"));
 		
 		stunTower = new Texture(Gdx.files.internal("stunFreezeHill.png")); //TODO
 		stunBullet = new Texture(Gdx.files.internal("basicBullet.png")); //TODO
 		
 		conversionTower = new Texture(Gdx.files.internal("conversionHill.png"));
 		conversionBullet = new Texture(Gdx.files.internal("basicBullet.png")); //TODO
 		
 		waterPuddle = new Texture(Gdx.files.internal("waterPuddle.png"));
 		mudPuddle = new Texture(Gdx.files.internal("mudPuddle.png"));
 		
 		//TODO put correct images in for each tower
 
 		homeBase = new Texture(Gdx.files.internal("red_castle2.png"));
 		money = new Texture(Gdx.files.internal("money.png"));
 		upgrade = new Texture(Gdx.files.internal("upgrade.png"));
 		
 		//menu and background
 		wall = new Texture(Gdx.files.internal("wall.png"));
 		grass = new Texture(Gdx.files.internal("grassTexture.png"));
 		menuBackground= new Texture(Gdx.files.internal("woodMenuBackground.png"));
 		quitButton= new Texture(Gdx.files.internal("quit.png"));	
 		
 	}
 	
 	
 	public void loadCharacter() {
 		Character character = world.getCharacter();
 		Rectangle rect = character.getBounds();
 		float x1 = character.getPosition().x + rect.x;
 		float y1 = character.getPosition().y + rect.y;
 		
 		batch.draw(queenAnt, x1 , y1 , character.SIZE * ppuX, character.SIZE *ppuY);
 		//batch.draw(soldierAnt, character.getPosition().x * ppuX  , character.getPosition().y * ppuY, character.SIZE * ppuX, character.SIZE * ppuY);
 	}
 	
 	public void drawBlocks() {
 		for(Block block : world.getBlocks()) {	
 			batch.draw(grass, block.getPosition().x * ppuX, block.getPosition().y * ppuY, Block.SIZE * ppuX, Block.SIZE * ppuY);
 		}
 	}
 	
 	public void drawTowers() {
 		
 		//home base tower
 		
 		
 		ArrayList<Tower> temp = new ArrayList<Tower>();
 		for(Tower tower : world.getTowers()) {
 			if(tower.remove==true){
 				temp.add(tower);
 			}
 			else{
 				if(tower instanceof BasicTower)//TODO FIX THESE IMAGES
 					batch.draw(basicTower, tower.getPosition().x+2, tower.getPosition().y-15, Tower.SIZE*26 , Tower.SIZE*20);
 				else if(tower instanceof SpawnTower)
 					batch.draw(spawnTower, tower.getPosition().x+2, tower.getPosition().y-15, Tower.SIZE*26 , Tower.SIZE*20);
 				else if(tower instanceof SplashTower)
 					batch.draw(splashTower, tower.getPosition().x+2, tower.getPosition().y-15, Tower.SIZE*26 , Tower.SIZE*20);
 				else if(tower instanceof SlowTower)
 					batch.draw(slowTower, tower.getPosition().x+2, tower.getPosition().y-15, Tower.SIZE*26 , Tower.SIZE*20);
 				else if(tower instanceof StunTower)
 					batch.draw(stunTower, tower.getPosition().x+2, tower.getPosition().y-15, Tower.SIZE*26 , Tower.SIZE*20);
 				else if(tower instanceof ConversionTower)
 					batch.draw(conversionTower, tower.getPosition().x+2, tower.getPosition().y-15, Tower.SIZE*26 , Tower.SIZE*20);
 				else if(tower instanceof Wall)
 					batch.draw(wall, tower.getPosition().x+2, tower.getPosition().y-15, Tower.SIZE*26 , Tower.SIZE*20);
 			}
 		}	
 		for (Tower tower : temp) {
 			tower.remove();
 		}
 	}
 	
 	public void drawMobs() {
 		ArrayList<Mob> temp = new ArrayList<Mob>();
 		for(Mob mob :world.getMobs()){
 			if(mob.health <=0){mob.deathFlag=true;}
 			if(!mob.active  || mob.deathFlag){
 				temp.add(mob);
 			}
 			else{
 				if(mob instanceof BasicMob){
 					batch.draw(soldierAnt, mob.getPosition().x, mob.getPosition().y, Mob.SIZE*20, Mob.SIZE*15);
 				}
 				else if(mob instanceof QueenAnt){
 					batch.draw(queenAnt, mob.getPosition().x, mob.getPosition().y, Mob.SIZE*20, Mob.SIZE*15);
 				}
				else if(mob instanceof RedAnt){
					batch.draw(redAnt, mob.getPosition().x, mob.getPosition().y, Mob.SIZE*20, Mob.SIZE*15);
				}
				else if(mob instanceof CarpenterAnt){
					batch.draw(carpenterAnt, mob.getPosition().x, mob.getPosition().y, Mob.SIZE*20, Mob.SIZE*15);
				}
				else if(mob instanceof FlyingAnt){
					batch.draw(flyingAnt, mob.getPosition().x, mob.getPosition().y, Mob.SIZE*20, Mob.SIZE*15);
				}
 			}
 		}
 		for(Mob mob: temp){
 			if(mob.deathFlag){
 				mob.mobDeath();
 			}
 			else{
 				if(mob.target == 1)
 					mob.mobReachedEnd();
 				else
 					world.getPlayer(1).addPoints(mob.points);
 			}
 			mob.removeMob();
 		}
 	}
 	
 	public void drawBullets() {
 		ArrayList<Bullet> temp = new ArrayList<Bullet>();
 		for(Bullet bullet: world.getBullets()){
 			if(!bullet.active){
 				temp.add(bullet);
 			}
 			else{/*TODO: fix images for bullets*/
 				if(bullet.tower instanceof BasicTower)
 					batch.draw(basicBullet, bullet.getPosition().x, bullet.getPosition().y, Bullet.SIZE*5, Bullet.SIZE*5);
 				else if(bullet.tower instanceof SplashTower)
 					batch.draw(basicBullet, bullet.getPosition().x, bullet.getPosition().y, Bullet.SIZE*5, Bullet.SIZE*5);
 				else if(bullet.tower instanceof SlowTower)
 					batch.draw(basicBullet, bullet.getPosition().x, bullet.getPosition().y, Bullet.SIZE*5, Bullet.SIZE*5);
 				else if(bullet.tower instanceof ConversionTower)
 					batch.draw(basicBullet, bullet.getPosition().x, bullet.getPosition().y, Bullet.SIZE*5, Bullet.SIZE*5);
 				else if(bullet.tower instanceof StunTower)
 					batch.draw(basicBullet, bullet.getPosition().x, bullet.getPosition().y, Bullet.SIZE*5, Bullet.SIZE*5);
 			}
 		}
 		for(Bullet bullet: temp){
 				world.bulletList.remove(bullet);
 		}
 	}
 	
 	
 	public void drawMenu() {
 		batch.draw(menuBackground, -38,0);
 		Menu menu = world.getMenu();
 		for(Button button : menu.getButtons()) {
 			batch.draw(towerMapTexture.get(button.getTowerType()), button.getPosition().x*(640/50), button.getPosition().y*(480/50)-10, 30, 30);
 		}
 		
 		
 		/*
 		   0. null
 		   1. Normal Tower- simply attacks ants
 		   2. Slow Tower - movement speed debuff
 		   3. Stun Tower - prevents all movement, or other actions
 		   4. Splash Tower-
 		   5. Conversion Tower- converts to attack opponent
 		   6. Spawn Tower - mob factory
 		   7. waterPuddle
 		 */
 		
 		//seven font commands right here
 		font.setScale(.9f);
 		font.draw(batch, "Level", 2, 55);
 		font.draw(batch, "up", 5, 42);
 		font.draw(batch, "Basic", 5, 82);
 		font.draw(batch, "Slow", 5, 120);
 		font.draw(batch, "Stun", 5, 155);
 		font.draw(batch, "Splash", 5, 190);
 		font.draw(batch, "Conv", 5, 230);
 		font.draw(batch, "Erase", 5, 300);
 		font.draw(batch, "Spawn", 5, 265);
 
 		
 	}
 	
 	
 	
 	public void drawDebug() {
 		
 		debugRenderer.setProjectionMatrix(cam.combined);
 		debugRenderer.begin(ShapeType.Rectangle);
 		
 		for(Block block : world.getBlocks()) {
 			Rectangle rect = block.getBounds();
 			float x1 = block.getPosition().x + rect.x;
 			float y1 = block.getPosition().y + rect.y;
 			if (x1 > 6) {
 				debugRenderer.setColor(new Color(0,0,0,1));
 			} else if (x1 <= 6) {
 				debugRenderer.setColor(new Color(1,1,0,1));
 			}
 			debugRenderer.rect(x1, y1, rect.width, rect.height);
 		}
 	
 		Character character = world.getCharacter();
 		Rectangle rect = character.getBounds();
 		float x1 = character.getPosition().x + rect.x;
 		float y1 = character.getPosition().y + rect.y;
 		
 		debugRenderer.setColor(new Color(1,0,0,1));
 		debugRenderer.rect(x1,  y1, rect.width, rect.height);
 		
 		Character wall = world.getWall();
 		Rectangle rect1 = character.getBounds();
 		float xwall = wall.getPosition().x + rect1.x;
 		float ywall = wall.getPosition().y + rect1.y;
 		debugRenderer.setColor(new Color(1,0,0,1));
 		debugRenderer.rect(xwall,  ywall, rect1.width, rect1.height);
 		debugRenderer.end();
 
 	}
 	//configure hashmap to know which textures to map to
 	
 	/*
 	   0. null
 	   1. Normal Tower- simply attacks ants
 	   2. Slow Tower - movement speed debuff
 	   3. Stun Tower - prevents all movement, or other actions
 	   4. Splash Tower-
 	   5. Conversion Tower- converts to attack opponent
 	   6. Spawn Tower - mob factory
 	   7. waterPuddle
 	 */
 	
 	public void buildTextureMap() {
 		
 		towerMapTexture.put(0, quitButton);
 		towerMapTexture.put(1, basicTower);
 		towerMapTexture.put(2, slowTower); //no class made for this yet
 		towerMapTexture.put(3, stunTower); //no class for this yet
 		towerMapTexture.put(4, splashTower);  
 		towerMapTexture.put(5, conversionTower);
 		towerMapTexture.put(6, spawnTower);
 		towerMapTexture.put(7, money); //no class for this yet 
 		towerMapTexture.put(8, upgrade); //remove tower should be money sign
 		
 		
 	}
 	
 	public void drawPlayer() {
 		//to get the information for player stats and draw them to the screen
 		player = world.getPlayer(1);
 		
 		//Gdx.app.log("player", "currency of player: " + player.currency);
 		font.setScale(1.0f);
 		font.draw(batch, "Currency:", 10, 460);
 		font.draw(batch, "$" + player.getCurrency(), 15, 435);
 		font.draw(batch, "Health: ", 10, 410);
 		font.draw(batch, "" + player.getHealth(), 15, 385);
 		font.draw(batch, "Score: ", 10, 360);
 		font.draw(batch, "" + player.getPoints(), 15, 335);
 		player.setLevel((int) player.getPoints()/1000);
 		font.draw(batch, "Level: " + player.getLevel(), 100, 25);
 		
 		
 	}
 	
 	public WorldRenderer getRenderer() {
 		return this;
 	}
 	
 	public float getScreenWidth() {
 		return ppuX;
 	}
 	
 	public float getScreenHeight() {
 		return ppuY;
 	}
 	
 	public float getCameraWidth() {
 		return CAMERA_WIDTH;
 		
 	}
 	public float getCameraHeight() {
 		return CAMERA_HEIGHT;
 	}
 	
 	public Map<Integer, Texture> getTextureMap() {
 		return towerMapTexture;
 	}
 }
