 package com.teamcoffee.game.views;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureWrap;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.teamcoffee.game.LevelInfo;
 import com.teamcoffee.game.LevelLoad;
 import com.teamcoffee.game.OurGame;
 import com.teamcoffee.game.models.Entity;
 import com.teamcoffee.game.models.MoveableEntity;
 import com.teamcoffee.game.models.Player;
 
 public class Level {
 	
 	private Player player;
 	private Set<Entity> entities;
 	private Set<Entity> toAdd;
 	private int startX = 0;
 	private int startY = 0;
 	private int livesLeft;
 	
 	private int levelNumber;
 	
 	public static final int SPEED = 100;
 	
 	private SpriteBatch batch;
 	private Texture spriteTexture;
 	private SpriteBatch spriteBatch;
 	private Sprite sprite;
 	
 	private OrthographicCamera cam; 
 	private float width;
 	private float height;
 	
 	private boolean endLevel = false;
 		
 	public Level(OurGame game, int startX, int startY, Set<Entity> entities, int levelNumber){
 		this.startX = startX;
 		this.startY = startY;
 		this.entities = entities;
 		this.levelNumber = levelNumber;
 		this.livesLeft = 5;
 		
 		toAdd = new HashSet<Entity>();
 		
 		spriteBatch = new SpriteBatch();
 	    spriteTexture = new Texture(Gdx.files.internal("data/Backgound.png"));
 	                 
 	    spriteTexture.setWrap(TextureWrap.MirroredRepeat,TextureWrap.MirroredRepeat);
 	    sprite = new Sprite(spriteTexture, 0, 0, 1280, 720);
 	    sprite.setSize(1280, 720);
 
 		Gdx.input.setInputProcessor(new InputHandler(this));
 		
 		width = Gdx.graphics.getWidth();
 		height = Gdx.graphics.getHeight();
 		
 		cam = new OrthographicCamera();
 		cam.setToOrtho(false, width, height);
 		cam.update();
 		
 		batch = new SpriteBatch();
 		batch.setProjectionMatrix(cam.combined);
 		
 		player = new Player(SPEED, 0, 37, 64, new Vector2(startX, startY));
 		
 	}
 
 	/**
 	 * This update decides whether the player collided with other entities in the level
 	 */
 	public void update() {
 		
 		if (endLevel){
 			LevelInfo info = LevelLoad.loadLevel(levelNumber + 1);
 			levelNumber++;
 			
 			entities.clear();
 			entities.addAll(info.entities);
 			startX = info.startX;
 			startY = info.startY;
 			
 			respawnPlayer();
 			
 			endLevel = false;
 		}
 		
 		//update the player's location and velocity
 		player.update();
 			
 		//Check all other entities
 		for (Entity entity: entities){
 			collidesWith(player, entity);
 		}
 	}
 
 	public void render(float delta) {
 		
 		Gdx.gl.glClearColor(0, 0, 0, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		
 		spriteBatch.setProjectionMatrix(cam.combined);
 		spriteBatch.begin();
 		sprite.draw(spriteBatch);
 		spriteBatch.end();
 		
		float camX = player.getPosition().x;
		float camY = player.getPosition().y;
		cam.position.set(camX, camY, 0);
 		cam.update();
 		
 		batch.setProjectionMatrix(cam.combined);
 		batch.begin();
 		
		if (this.livesLeft > 0){
			player.render(batch);
		}
		
 		for (Entity entity: getEntities()){
 			entity.render(batch);
 		}
 		
 		batch.end();
 		
 		for (Entity e: toAdd){
 			entities.add(e);
 		}
 		
 		toAdd.clear();
 		
 		
 	}
 
 	public void dispose() {
 		batch.dispose();
 	}
 
 	public Player getPlayer() {
 		return player;
 	}
 	
 	public Set<Entity> getEntities(){
 		return entities;
 	}
 	
 	/**
 	 * Determines if Entity a collides with b.
 	 * If a hits b from below, 0 is returned
 	 * If a hits b from above, 1 is returned
 	 * If a hits b from the right, 2 is returned
 	 * If a hits b from the left, 3 is returned
 	 * -1 if no collision
 	 */
 	public int collidesWith(MoveableEntity a, Entity b){
 		
 		Rectangle aBounds = new Rectangle(a.getPosition().x, a.getPosition().y, a.getWidth(), a.getHeight());
 		Rectangle bBounds = new Rectangle(b.getPosition().x, b.getPosition().y, b.getWidth(), b.getHeight());
 		Vector2 aVelocity = a.getVelocity();
 		Vector2 aPosition = a.getPosition();
 		Vector2 bPosition = b.getPosition();
 		
 		//check for a collision
 		if (aBounds.overlaps(bBounds)){
 			
 			System.out.println("a: x: " + a.getPosition().x + 
 					"y: " + a.getPosition().y + 
 					"w: " + a.getWidth() + 
 					"h: " + a.getHeight());
 			System.out.println("b: x: " + b.getPosition().x + 
 					"y: " + b.getPosition().y + 
 					"w: " + b.getWidth() + 
 					"h: " + b.getHeight());
 
 			//now we need to how the player was moving to determine the type of collision
 			//first we should check sideways collisions
 			
 			//--------------------------moving in the -x direction
 			if (aVelocity.x < 0){
 				
 				//do not check the pixels directly above and below the block.
 				//doing so would stop the player from moving
 				if (aPosition.y >= bPosition.y + b.getHeight() - 2 && aPosition.y <= bPosition.y + b.getHeight() + 2){
 					
 				}
 				else{
 					//player collided and was moving in the -x direction
 					b.onCollide(2, a, this);
 					return 2;
 				}
 			}
 			 
 			//--------------------------moving in the +x direction
 			else if (aVelocity.x > 0){
 				
 				//do not check the pixels directly above and below the block.
 				//doing so would stop the player from moving
 				if (aPosition.y >= bPosition.y + b.getHeight() - 2 && aPosition.y <= bPosition.y + b.getHeight() + 2){
 					
 				}
 				else{
 					//player collided and was moving in the +x direction
 					b.onCollide(3, a, this);
 					return 3;
 				}
 			}
 			
 			//--------------------------moving in the -y direction
 			if (aVelocity.y < 0){
 				
 				//make sure the player is somewhat above the block
 				if (aPosition.y >= bPosition.y + (b.getHeight() / 3)){
 					
 					//player collided and was moving in the -y direction
 					b.onCollide(0, a, this);
 					return 0;
 				}				
 			}
 			
 			//--------------------------moving in the +y direction
 			else if (aVelocity.y > 0){
 				
 				//make sure the player is somewhat below the block
 				if (aPosition.y <= bPosition.y - (b.getHeight() / 3)){
 					
 					//player collided and was moving in the +y direction
 					b.onCollide(1, a, this);
 					return 1;
 				}
 			}
 
 		}
 		
 		return -1;
 		
 	}
 	
 	public void addEntity(Entity entity){
 		toAdd.add(entity);
 	}
 	
 	public void nextLevel(){
 		endLevel = true;
 		livesLeft = 5;
 	}
 	
 	public void respawnPlayer(){
 		player.getPosition().x = startX;
 		player.getPosition().y = startY+50;
 		player.getVelocity().x = 0;
 		player.getVelocity().y = 0;
 		livesLeft--;
 	}
 }
