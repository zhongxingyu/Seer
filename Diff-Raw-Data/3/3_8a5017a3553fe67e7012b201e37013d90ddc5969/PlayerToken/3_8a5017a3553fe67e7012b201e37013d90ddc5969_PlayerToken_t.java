 package gameObjects;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 
 /**
  * This class is the graphical representation of the player that is currently playing
  * @author antonio
  *
  */
 public class PlayerToken extends Image{
 
 	private Color color;
 	
 	private final int VELOCITY_X = 5;
 	private final int VELOCITY_Y = 5;
 	
 	private final int WIDTH = 50;
 	private final int HEIGHT = 50;
 	
 	private Rectangle rect;
 	
 	public PlayerToken(Player p){
 		super();
 		color = p.getColor();
 	}
 	
 	public PlayerToken(Player p, int x, int y){
 		this(p);
 		super.setX(x);
 		super.setY(y);
 		rect = new Rectangle(getX(), getY(), WIDTH, HEIGHT);
 	}
 	
 	public void draw(SpriteBatch batch, float parentAlpha){
 		super.draw(batch, parentAlpha);
		batch.end();
 		ShapeRenderer sr = new ShapeRenderer();
 		sr.begin(ShapeType.Filled);
 		sr.setColor(color);
 		sr.rect(getX(), getY(), WIDTH, HEIGHT);
 		sr.end();
		batch.begin();
 	}
 	
 	public void moveLeft(){
 		setX(getX() - VELOCITY_X);
 		rect.setX(getX() - VELOCITY_X);
 	}
 	
 	public void moveRight(){
 		setX(getX() + VELOCITY_X);
 		rect.setX(getX() + VELOCITY_X);
 	}
 	
 	public void moveDown(){
 		setY(getY() - VELOCITY_Y);
 		rect.setY(getY() - VELOCITY_Y);
 	}
 	
 	public void moveUp(){
 		setY(getY() + VELOCITY_Y);
 		rect.setY(getY() + VELOCITY_Y);
 	}
 	
 	public Rectangle getRect(){
 		return rect;
 	}
 }
