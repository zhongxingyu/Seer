 package edu.calpoly.csc.pulseman;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.geom.Vector2f;
 
 import edu.calpoly.csc.pulseman.gameobject.Player;
 
 public class Camera
 {
 	private Image sky, layer1, layer2, layer3;
 
 	public Camera(Image sky, Image layer1, Image layer2, Image layer3)
 	{
 		this.sky = sky;
 		this.layer1 = layer1;
 		this.layer2 = layer2;
 		this.layer3 = layer3;
 	}
 
 	/**
 	 * Calculates where we want the camera to be and gradually interpolates the camera toward the destination
 	 */
 	public void render(GameContainer gc, Graphics g, Player p)
 	{
 		g.drawImage(sky, 0, 0);
 
 		float xTranslation = -(float)(p.getHitBox().getX() - Main.getScreenWidth() / 2.0);
 		float yTranslation = -(float)(p.getHitBox().getY() - Main.getScreenHeight() / 2.0);
 
		if(xTranslation > 0.0f)
 		{
 			xTranslation = 0.0f;
 		}
 
		if(yTranslation > 0.0f)
 		{
 			yTranslation = 0.0f;
 		}
 
		if(xTranslation < -World.getWorld().getLevelWidth() + Main.getScreenWidth())
		{
			xTranslation = (float)(-World.getWorld().getLevelWidth() + Main.getScreenWidth());
		}

		if(yTranslation < -World.getWorld().getLevelHeight() + Main.getScreenHeight())
		{
			yTranslation = (float)(-World.getWorld().getLevelHeight() + Main.getScreenHeight());
		}

 		g.drawImage(layer1, xTranslation * 0.2f, yTranslation * 0.2f);
 		g.drawImage(layer2, xTranslation * 0.5f, yTranslation * 0.5f);
 		g.drawImage(layer3, xTranslation * 0.7f, yTranslation * 0.7f);
 
 		g.translate(xTranslation, yTranslation);
 	}
 
 	/**
 	 * Force the camera to the given position rather than a gradual movement
 	 */
 	public void setCamera(Vector2f position)
 	{
 	}
 }
