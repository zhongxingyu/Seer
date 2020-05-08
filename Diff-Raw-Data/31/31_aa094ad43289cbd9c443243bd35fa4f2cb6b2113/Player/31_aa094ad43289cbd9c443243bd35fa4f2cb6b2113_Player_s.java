 package Entity;
 
 import Main.Game;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
 import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
 import com.badlogic.gdx.math.Vector2;
 
 public class Player extends Sprite {
 	
 	/** The Movement Velocity */
 	private Vector2 velocity = new Vector2();
 	
 	/** The Speed */
 	private float speed = 60 * 2;
 	
 	private TiledMapTileLayer collisionLayer;
 	
 	public Player(Sprite sprite, TiledMapTileLayer collisionLayer) {
 		super(sprite);
 		this.collisionLayer = collisionLayer;
 	}
 	
 	@Override
 	public void draw(SpriteBatch spriteBatch) {
 		update(Gdx.graphics.getDeltaTime());
 		super.draw(spriteBatch);
 	}
 	
 	public void update(float delta) {
 		
 		//save old position
 		float oldX = getX(), oldY = getY();
 		//tile datas
 		float tileWidth = collisionLayer.getTileWidth();
 		float tileHeight = collisionLayer.getTileHeight();
 		boolean collisionX = false, collisionY = false;
 		
 		handleMovements();
 		
 		 // move on x
         setX(getX() + velocity.x * delta);
        Game.map.setLightOffsets(velocity.x, 0);
 
         if(velocity.x < 0) // going left
                 collisionX = collidesLeft();
         else if(velocity.x > 0) // going right
                 collisionX = collidesRight();
 
         // react to x collision
         if(collisionX) {
                 setX(oldX);
                 velocity.x = 0;
         }
 
         // move on y
         setY(getY() + velocity.y * delta);
        Game.map.setLightOffsets(0, velocity.y);
 
         if(velocity.y < 0) // going down
                 collisionY = collidesBottom();
         else if(velocity.y > 0) // going up
                 collisionY = collidesTop();
 
         // react to y collision
         if(collisionY) {
                 setY(oldY);
                 velocity.y = 0;
         }
 	}
 	
 	private boolean isCellBlocked(float x, float y) {
         Cell cell = collisionLayer.getCell((int) (x / collisionLayer.getTileWidth()), (int) (y / collisionLayer.getTileHeight()));
         return cell != null && cell.getTile() != null && cell.getTile().getProperties().containsKey("blocked");
 	}
 
 	public boolean collidesRight() {
         for(float step = 0; step < getHeight(); step += collisionLayer.getTileHeight() / 2)
                 if(isCellBlocked(getX() + getWidth(), getY() + step))
                         return true;
         return false;
 	}
 
 	public boolean collidesLeft() {
         for(float step = 0; step < getHeight(); step += collisionLayer.getTileHeight() / 2)
                 if(isCellBlocked(getX(), getY() + step))
                         return true;
         return false;
 	}
 
 	public boolean collidesTop() {
         for(float step = 0; step < getWidth(); step += collisionLayer.getTileWidth() / 2)
                 if(isCellBlocked(getX() + step, getY() + getHeight()))
                         return true;
         return false;
 
 	}
 
 	public boolean collidesBottom() {
         for(float step = 0; step < getWidth(); step += collisionLayer.getTileWidth() / 2)
                 if(isCellBlocked(getX() + step, getY()))
                         return true;
         return false;
 	}
 	
 	public void handleMovements() {
 		velocity.x = 0;
 		velocity.y = 0;
 		
 		if(Gdx.input.isKeyPressed(Keys.W)) {
 			velocity.y = speed;
 		}
 		
 		if(Gdx.input.isKeyPressed(Keys.S)) {
 			velocity.y = - speed;
 		}
 		
 		if(Gdx.input.isKeyPressed(Keys.A)) {
 			velocity.x = -speed;
 		}
 		
 		if(Gdx.input.isKeyPressed(Keys.D)) {
 			velocity.x = speed;
 		}
 	}
 }
