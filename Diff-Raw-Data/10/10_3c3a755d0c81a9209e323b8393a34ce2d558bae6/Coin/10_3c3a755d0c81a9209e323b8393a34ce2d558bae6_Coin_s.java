 package weapons;
 
 import java.util.HashMap;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Shape;
 
 public class Coin {
 	
 	HashMap<String,Integer> points = new HashMap<String,Integer>();
 	HashMap<String,Color> colors = new HashMap<String,Color>();
	Image sprite;
 	public int value;
 	public float[] pos;
 	public Color color;
 	
 	public Coin(String color, float[] position) throws SlickException{
 		initPoints();
 		value = points.get(color);
 		pos = position;
 		this.color = colors.get(color);
 		sprite = new Image("Assets/JewelsAndMisc/jewel"+color+".png");
 	}
 	
 	private void initPoints(){
 		points.put("yellow", 10);
 		colors.put("yellow", Color.yellow);
 		points.put("red", 20);
 		colors.put("red", Color.red);
 		points.put("blue", 30);
 		colors.put("blue", Color.blue);
 		points.put("green", 40);
 		colors.put("green", Color.darkGray);
 		points.put("purple", 50);
 		colors.put("purple", Color.magenta);
 		
 	}
 
 	public void Draw() {
 		sprite.draw(pos[0], pos[1]);
 	}
 	
 	public Shape getHitBox() {
 		return new Rectangle(pos[0], pos[1], 16, 16);
 	}
 }
