 import java.awt.*;
 import javax.swing.*;
 
 public class Floater extends Enemy
 {
 	public static double SPEED = 2.5;
 	// Constructor.
 	public Floater()
 	{
 		super(new Vector(300, 300), 20, 20);
 		sprite = new Sprite(SpriteSheet.FLY, 0);
 		list.add("Player");
 		setHp(3);
 	}
 	private Vector getLoc()
 	{
 		return Content.playerManager.get(0).getCenter();
 	}
 	@Override
 	public void logic()
 	{
 		if(!isAlive())
 		{
 			return;
 		}
 
 		Vector dis = getLoc().sub(getCenter());
 		if(dis.mag() < 250)//chase if close
 		{
 			dx += 0.2 * dis.x/dis.mag();
 			dy += 0.2 * dis.y/dis.mag();
 			sprite.animation = 1;
 		}
 		else
 		{
 			if(Math.hypot(dx, dy) == 0)
 				dx = 0.2;
 			dx += 0.2 * -dy/Math.hypot(dx, dy);
 			dy += 0.2 * dx/Math.hypot(dx, dy);
 			sprite.animation = 0;
 		}
		//dx += 0.1 * ((dis.x > 0)? 1: -1);
		//dy += 0.1 * ((dis.y > 0)? 1: -1);
 		
 		
 		if(Math.hypot(dx, dy) > SPEED)
 		{
 			dx = SPEED * dx/Math.hypot(dy, dx);
 			dy = SPEED * dy/Math.hypot(dy, dx);
 		}
 
 		getDest().x = getCenter().x + dx;
 		getDest().y = getCenter().y + dy;
 
 		Entity tmpX = Collision.moveX(this);
 		Entity tmpY = Collision.moveY(this);
 
 		if(tmpX instanceof Player)
 		{
 			Player tmp = (Player)tmpX;
 			tmp.kill();
 		}
 		else if(tmpY instanceof Player)
 		{
 			Player tmp = (Player)tmpY;
 			tmp.kill();
 		}
 		
 		if(dx != 0)
 			sprite.xScale = dx/Math.abs(dx);
 		sprite.frame += 1;
 	}
 	@Override
 	public void dispose()
 	{
 	}
 }
