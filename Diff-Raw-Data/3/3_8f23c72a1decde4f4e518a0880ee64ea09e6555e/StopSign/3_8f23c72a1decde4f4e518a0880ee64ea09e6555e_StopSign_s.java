 package no.saua.mousekiller;
 
 import java.io.IOException;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import no.saua.engine.Entity;
 import no.saua.engine.Texture;
 import android.content.res.AssetManager;
 
 public class StopSign extends Entity {
 	public static Texture tex;
 	public StopSign(Map map, int tilex, int tiley) {
 		setPosition(map.getTileCenterX(tilex), map.getTileCenterY(tiley));
 		setCollisionRadius(12);
 		setCollidable(true);
 		setTexture(tex);
 	}
 	
 	public void collision(Mouse mouse) {
 		mouse.setDirection(Direction.getReverseDirection(mouse.getDirection()));
 	}
 	
 	public static void loadSprites(GL10 gl, AssetManager assets) throws IOException {
 		tex = Texture.loadTexture(gl, assets.open("textures/stop.png"));
 	}
 }
