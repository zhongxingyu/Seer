 package edu.calpoly.csc.pulseman.gameobject;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 
 public class KillingObstacle extends Collidable implements Murderer
 {
 	public enum Orientation {UP, DOWN, LEFT, RIGHT};
 	static Map<String, Image> imageMap = new HashMap<String, Image>();
 	ObjectBehavior behavior;
 	boolean timeAffectable;
 	private Image image;
 	private Orientation orient;
 
 	public static void init(String imageName) throws SlickException
 	{
 		imageMap.put(imageName, new Image(imageName));
 	}
 
 	public KillingObstacle(String imgName, int x, int y, ObjectBehavior behavior, 
 			boolean timeAffectable, Orientation orient)
 	{
 		super(new Rectangle(x, y, imageMap.get(imgName).getWidth(), imageMap.get(imgName).getHeight()), timeAffectable);
 		this.timeAffectable = timeAffectable;
 		image = imageMap.get(imgName);
 		this.behavior = behavior;
 		this.orient = orient;
 	}
 
 	@Override
 	public void render(GameContainer gc, Graphics g)
 	{
 		float rotation = 0;
 		if (orient == Orientation.UP) {
 			rotation = 0;
 		} else if (orient == Orientation.LEFT) {
			rotation = 270;
 		} else if (orient == Orientation.DOWN) {
 			rotation = 180;
 		} else if (orient == Orientation.RIGHT) {
			rotation = 90;
 		}
 		image.rotate(rotation);
 		g.drawImage(image, getHitBox().getX(), getHitBox().getY());
 		image.rotate(-rotation);
 	}
 
 	@Override
 	public void update(GameContainer gc, int delta)
 	{
 		behavior.update(getHitBox(), delta);
 	}
 
 	@Override
 	public boolean isAffectedByPulse() {
 		return timeAffectable;
 	}
 }
