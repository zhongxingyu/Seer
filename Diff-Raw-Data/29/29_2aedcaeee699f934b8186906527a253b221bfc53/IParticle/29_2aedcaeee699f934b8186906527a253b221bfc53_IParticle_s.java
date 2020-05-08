 package spaceshooters.aurora.particle;
 
import pl.matterross.engine.gfx.Texture;
import pl.matterross.engine.math.Vector2f;
 
 public interface IParticle {
 	
 	void update(int delta);
 	
 	void render();
 	
 	Texture getTexture();
 	
 	Vector2f getPosition();
 	
 	void setPosition(Vector2f pos);
 	
 	boolean getRenderAfterEntities();
 	
 	float getX();
 	
 	void setX(float x);
 	
 	float getY();
 	
 	void setY(float y);
 	
 	boolean isAlive();
 	
 	void setAlive(boolean alive);
 }
