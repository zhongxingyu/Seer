 package tutorials.slickout.gameplay.level;
  
 import java.util.List;
  
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
  
 public interface ILevel {
  
 	public ImageObject getBackground();
  
 	public List<Ball> getBalls();
  
 	public Paddle getPaddle();
  
 	public CollidableImageObject getLeftBumper();
  
 	public CollidableImageObject getRightBumper();
  
 	public CollidableImageObject getTopBumper();
  
 	public List<Brick> getBricks();
  
 	public Ball addNewBall();
 	
	public PowerUp addPowerUp(Vector2f pos);
 	
 	public List<PowerUp> getPowerUps();
 	
 	public double getPowerUpP();
 			
 	/*
 	public void addBall(Ball newBall);
  
 	public void removeBall(Ball ball);
  
 	public int getBallCount();
  
 	public int
 	*/ 
 }
