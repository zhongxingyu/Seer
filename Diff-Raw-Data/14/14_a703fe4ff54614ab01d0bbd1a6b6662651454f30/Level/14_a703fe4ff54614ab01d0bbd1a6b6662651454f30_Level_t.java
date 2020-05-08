 package com.teamcoffee.game.views;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.teamcoffee.game.OurGame;
 import com.teamcoffee.game.models.Entity;
 import com.teamcoffee.game.models.MoveableEntity;
 import com.teamcoffee.game.models.Player;
 import com.teamcoffee.game.models.Block;
 
 public class Level {
 	
 	public static boolean iscloliding;
 	
 	private OurGame game;
 	private Player player;
 	private Set<Entity> entities;
 	public static final int SPEED = 100;
 	
 	private SpriteBatch batch;
 	
 	private OrthographicCamera cam; 
 	private float width;
 	private float height;
 		
 	public Level(OurGame game){
 		this.game = game;
 		entities = new HashSet<Entity>();
 		
 		player = new Player(SPEED, 0, (float) 50.0, (float) 50.0, new Vector2(0, (Gdx.graphics.getHeight() / 10) + 50));
 		
 		
 		for (int i = 0; i < 10; i++){
 			entities.add(new Block(new Vector2(i * 50, 0), 50, 50));
 		}
 		
 		entities.add(new Block(new Vector2(100, 50), 50, 50));
 		entities.add(new Block(new Vector2(230, 150), 50, 50));
 		entities.add(new Block(new Vector2(230, 200), 50, 50));
 		entities.add(new Block(new Vector2(230, 250), 50, 50));
 		
 		
 		Gdx.input.setInputProcessor(new InputHandler(this));
 		
 		width = Gdx.graphics.getWidth();
 		height = Gdx.graphics.getHeight();
 		
 		cam = new OrthographicCamera();
 		cam.setToOrtho(false, width, height);
 		cam.update();
 		
 		batch = new SpriteBatch();
 		batch.setProjectionMatrix(cam.combined);
 	}
 
 	/**
 	 * This update decides whether the player collided with other entities in the level
 	 */
 	public void update() {
 		
 		//update the player's location and velocity
 		player.update();
 		
 		boolean top = false, bottom = false, left = false, right = false;
 		
 		
 		//Check all other entities
 		for (Entity entity: entities){
 			
 			collidesWith(player, entity);
 			
 		}
 	}
 
 public void render(float delta) {
 		
 		Gdx.gl.glClearColor(0, 0, 0, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		
 		//cam.position.set(player.getPosition().x, player.getPosition().y, 0);
 		cam.update();
 		
 		batch.setProjectionMatrix(cam.combined);
 		batch.begin();
 		batch.draw(player.getTexture(), player.getPosition().x, player.getPosition().y, player.getWidth() / 2, player.getHeight() / 2, player.getWidth(), player.getHeight(), 1, 1, player.getRotation(), 0, 0, player.getTexture().getWidth(), player.getTexture().getHeight(), false, false);
 		
 		for (Entity entity: getEntities()){
 			batch.draw(entity.getTexture(), 
 					entity.getPosition().x, 
 					entity.getPosition().y, 
 					entity.getWidth() / 2, 
 					entity.getHeight() / 2, 
 					entity.getWidth(), 
 					entity.getHeight(),
 					1, 
 					1, 
 					0, 
 					0, 
 					0, 
 					(int)entity.getWidth(), 
 					(int)entity.getHeight(),
 					false, 
 					false);
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
 		float vX = a.getVelocity().x;
 		float vY = a.getVelocity().y;
 		
 		if (aBounds.overlaps(bBounds)){
 			
 			//y direction checks
 			if (vY < 0){
				if (a.getPosition().y > b.getPosition().y){
 					a.getPosition().y = b.getPosition().y + b.getHeight();
 					a.getVelocity().y = 0;
 					a.setOnGround();
 				}				
 			}
 			
 			else if (vY > 0){
 				if (a.getPosition().y + a.getHeight() < b.getPosition().y){
 					a.getPosition().y = b.getPosition().y - a.getHeight();
 					a.getVelocity().y = (float) -0.1;
 				}
 			}
 			
 			//x direction checks
 			//right
 			else if (vX < 0){
 				if (a.getPosition().y <= b.getPosition().y + b.getHeight() - 4){
 					a.getPosition().x = b.getPosition().x + a.getWidth();
 					a.getVelocity().x = 0;
 				}
 			}
 			//left
 			else if (vX > 0){
 				if (a.getPosition().y <= b.getPosition().y + b.getHeight() - 4){
 					a.getPosition().x = b.getPosition().x - a.getWidth();
 					a.getVelocity().x = 0;
 				}
 			}
 
 		}
 		
 		return -1;
 		
 	}
 	
 }
