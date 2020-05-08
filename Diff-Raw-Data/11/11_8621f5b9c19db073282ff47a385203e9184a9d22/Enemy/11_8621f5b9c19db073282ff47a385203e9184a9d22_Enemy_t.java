 package Game;
 
 import java.awt.image.BufferedImage;
 import java.util.Random;
 
 public class Enemy extends Sprite 
 {
 	private Player player;
     private Random rand;
 	public Enemy(double x, double y, BufferedImage image, Player player)
 	{
 		super(x, y, image);
 		maxspeed = 2;
 		rand = new Random();
 		this.player = player;
 	}
 	
 	public void move()
 	{
		if(x < player.x && x < Board.WIDTH - 50)
 			x += 5;
		if(x > player.x && x > 0)
 			x -= 5;
		if(y < player.y && y < Board.HEIGHT - 50)
 			y += 5;
		if(y > player.y && y > 0)
 			y -= 5;
 		if(getBounds().intersects(player.getBounds()))
 		{
 			player.decrementHP();
 			x = rand.nextInt(300) + 100;
 			y = rand.nextInt(300) + 100;
 		}
 	}
 }
