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
 import com.teamcoffee.game.OurGame;
 import com.teamcoffee.game.models.Entity;
 import com.teamcoffee.game.models.Lava;
 import com.teamcoffee.game.models.Fire;
 import com.teamcoffee.game.models.MoveableEntity;
 import com.teamcoffee.game.models.Player;
 import com.teamcoffee.game.models.Block;
 import com.teamcoffee.game.models.Spike;
 import com.teamcoffee.game.models.Water;
 
 public class Level {
 	
 	public static boolean iscloliding;
 	
 	private Player player;
 	private Set<Entity> entities;
 	public static final int SPEED = 100;
 	
 	private SpriteBatch batch;
 	private Texture spriteTexture;
 	private SpriteBatch spriteBatch;
 	private Sprite sprite;
 	
 	private OrthographicCamera cam; 
 	private float width;
 	private float height;
 		
 	public Level(OurGame game){
 		spriteBatch = new SpriteBatch();
 	    spriteTexture = new Texture(Gdx.files.internal("data/Backgound.png"));
 	                 
 	    spriteTexture.setWrap(TextureWrap.MirroredRepeat,TextureWrap.MirroredRepeat);
 	    sprite = new Sprite(spriteTexture, 0, 0, 1280, 720);
 	    sprite.setSize(1280, 720);
 	     
 		entities = new HashSet<Entity>();
		
 		for (int i = 0; i < 30; i++){
 			entities.add(new Block(new Vector2(i * 50, 0), 50, 50));
 		}
 		for (int i = 0; i < 30; i++){
 			entities.add(new Block(new Vector2(i * 50, 500), 50, 50));
 		}
 		for (int i = 0; i < 30; i++){
 			entities.add(new Block(new Vector2(300, 50 * i), 50, 50));
 		}
 		
 		/*entities.add(new Block(new Vector2(100, 50), 50, 50));
 		entities.add(new Block(new Vector2(230, 100), 50, 50));
 		entities.add(new Block(new Vector2(230, 200), 50, 50));
 		entities.add(new Block(new Vector2(230, 250), 50, 50));
 		entities.add(new Block(new Vector2(230, 50), 50, 50));
 		*/
 
		
 		Gdx.input.setInputProcessor(new InputHandler(this));
 		
 		width = Gdx.graphics.getWidth();
 		height = Gdx.graphics.getHeight();
 		
 		cam = new OrthographicCamera();
 		cam.setToOrtho(false, width, height);
 		cam.update();
 		
 		batch = new SpriteBatch();
 		batch.setProjectionMatrix(cam.combined);
 		
 		player = new Player(SPEED, 0, (float) 50.0, (float) 50.0, new Vector2(0, (Gdx.graphics.getHeight() / 10) + 50));
 		
 		for (int i = 0; i < 10; i++){
 			entities.add(new Block(new Vector2(i * 50, 0), 50, 50));
 		}
 		
 		entities.add(new Block(new Vector2(100, 50), 50, 50));
 		entities.add(new Block(new Vector2(230, 100), 50, 50));
 		entities.add(new Block(new Vector2(230, 200), 50, 50));
 		entities.add(new Block(new Vector2(230, 250), 50, 50));
 		entities.add(new Fire(new Vector2(230, 50), 50, 50));
 	}
 
 	/**
 	 * This update decides whether the player collided with other entities in the level
 	 */
 	public void update() {
 		
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
 		
 		cam.position.set(player.getPosition().x, player.getPosition().y, 0);
 		cam.update();
 		
 		spriteBatch.begin();
 		sprite.draw(spriteBatch);
 		spriteBatch.end();
 		
 		batch.setProjectionMatrix(cam.combined);
 		batch.begin();
 		
 		player.render();
 		
 		for (Entity entity: getEntities()){
 			entity.render();
 		}
 		
 		batch.end();
 		
 		//check collisions
 		
 		
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
 		
 		Rectangle aBounds = a.getBounds();
 		Rectangle bBounds = b.getBounds();
 		Vector2 aVelocity = a.getVelocity();
 		Vector2 aPosition = a.getPosition();
 		Vector2 bPosition = b.getPosition();
 		
 		if (aBounds.overlaps(bBounds)){
 			
 			//moving in the -y direction
 			if (aVelocity.y < 0){
 				//make sure the player is above the block
 				if (aPosition.y >= bPosition.y + b.getHeight() - 4 || aPosition.y >= bPosition.y + b.getHeight() ){
 					//if (aPosition.x > bPosition.x - b.getWidth() && aPosition.x < bPosition.x + b.getWidth() )/*rig*/{
 					
 						aPosition.y = bPosition.y + b.getHeight();
 					aVelocity.y = 0;
 					a.setOnGround();
 					//}
 				}				
 			}
 			
 			//moving in the y direction
 			 if (aVelocity.y > 0){
 				//make sure player is below the block
 				if (aPosition.y < bPosition.y - b.getHeight() + 8 ||aPosition.y < bPosition.y - b.getHeight() ){
 					
 					
 					
 						//aPosition.y = bPosition.y - a.getHeight();
 					aVelocity.y = (float) -0.1;
 					
 				}
 			}
 			
 			//x direction checks
 			//moving in the -x direction
 			
 			//right (check  character left side)
 			 if (aVelocity.x < 0){
 				if (aPosition.y <= bPosition.y + b.getHeight() -1){
 					
 						if (aPosition.x >= bPosition.x){
 							aPosition.x = bPosition.x + a.getWidth()+4;
 							aVelocity.x = 0;
 						}
 					
 				}
 			}
 			 
 			//left (checks character right side)
 			 if (aVelocity.x > 0){
 				 if (aPosition.y <= bPosition.y + b.getHeight() - 1){
 					
 						if (aPosition.x <= bPosition.x){
 							aPosition.x = bPosition.x - a.getWidth()-4;
 							aVelocity.x = 0;
 						}
 					
 				}
 			}
 
 		}
 		
 		return -1;
 		
 	}
 	
 }
