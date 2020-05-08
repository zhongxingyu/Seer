 
 import java.util.*;
 import java.awt.*;
 import javax.swing.*;
 
 public class Hopper extends Enemy
 {
 	public static final double GRAVITY = 0.75;
 	double dirX;
 	private int tick, wait;
 	private boolean jump, collide;
 	// Constructor.
 	public Hopper()
 	{
 		this(new Vector(0, 0), 22, 20);
 		sprite = new Sprite(SpriteSheet.HOPPER, 0);
 		int c = 3; // Absolute value of horizontal velocity.
 		dirX = dx = 2 * c * ((int)(Math.random() * 2)) - c; // Random to be negative or positive.
 		wait = tick = 100;
 		collide = false;
 		jump = collide = true;
 
 		list.add("Player");
 		list.add("Platform");
 	}
 	public Hopper(Vector vIn, int wIn, int hIn)
 	{
 		super(vIn, wIn, hIn);
 		int c = 3; // Absolute value of horizontal velocity.
 		dirX = dx = 2.5 * c * ((int)(Math.random() * 2)) - c; // Random to be negative or positive.
 		wait = tick = 100;
 		collide = false;
 		jump = collide = true;
 
 		list.add("Player");
 		list.add("Platform");
 	}
 	@Override
 	public void logic()
 	{
 		//Do nothing if it is dead
 		if(!isAlive())
 		{
 			return;
 		}
 		if(jump)
 		{
 			sprite.animation = 1;
 			if(sprite.frame < sprite.spriteSheet.numFrames(sprite.animation))
 			{
 				sprite.frame += 0.5;
 			}
 			if(!collide)
 			{
 				dx = dirX;
 			}
 			dy += GRAVITY;
 			if(dy > 12)
 			{
 				dy = 12;
 			}
 
 			boolean down = false;
 			if(dy > 0)
 			{
 				down = true;
 			}
 
 			getDest().x = getCenter().x + (int)dx;
 			getDest().y = getCenter().y + (int)dy;
 
 			Entity tmpX = Collision.moveX(this);
 			Entity tmpY = Collision.moveY(this);
 
 			if(tmpX != null)
 			{
 				if(tmpX.toString().equals("Player"))
 				{
 					Player tmp = (Player)tmpX;
 					System.out.println("Player killed.");
 					tmp.kill();
 				}
 				else
 				{
 					dirX *= -1;
 					collide = true;
 				}
 			}
 			if(down && tmpY != null)
 			{
 				if(tmpY.toString().equals("Player"))
 				{
 					Player tmp = (Player)tmpY;
 					tmp.kill();
 				}
 				else
 				{
 					jump = false;
 				}
 			}
 		}
 		else
 		{
 			tick--;
 			sprite.animation = 0;
 			sprite.frame += 0.25;
 			Vector dis = getCenter().sub(getLoc());
 			dirX = (dis.x < 0)? Math.abs(dirX): -Math.abs(dirX);
 			if(tick <= 0 && dis.mag() < 300)
 			{
 				// Jump.
 				dy = -10;
 				jump = true;
 				collide = false;
 				tick = wait;
 			}
 		}
 
 		sprite.xScale = dirX / Math.abs(dirX);
 	}
 	@Override
 	public void dispose()
 	{
 	}
 }
