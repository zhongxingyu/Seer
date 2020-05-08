 package cz.apo.entity.projectile;
 
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 
 import cz.apo.entity.Collidable;
 import cz.apo.entity.Entity;
 import cz.apo.entity.Tank;
 import cz.apo.entity.TankFacing;
 import cz.apo.paddleGame.PaddleGame;
 import cz.opt.pEngine.PVector;
 import cz.opt.pEngine.Pengine;
 import cz.opt.pEngine.VVector;
 
 public class Missile implements Entity, Projectile
 {
 	private float x, y;
 	private float width, height;
 	private float speed = 15.0f;
 	private float dx, dy;
 	private float angle = 0.0f;
 	
 	private boolean addSpeed = false;
 	
 	private TankFacing facing;
 	
 	public Missile(float x, float y, float width, float height, Tank tank)
 	{
 		this.x = x;
 		this.y = y;
 		this.width = width;
 		this.height = height;
 		this.facing = tank.getFacing();
 		
 		if(tank.isMoving())
 			addSpeed = true;
 	}
 	
 	public Missile(float x, float y, float width, float height, float dx, float dy)
 	{
 		this.x = x;
 		this.y = y;
 		this.width = width;
 		this.height = height;
 		this.dx = dx;
 		this.dy = dy;
 	}
 	
 	public float getX()
 	{
 		return x;
 	}
 	
 	public float getY()
 	{
 		return y;
 	}
 	
 	public float getDX()
 	{
 		return dx;
 	}
 	
 	public float getDY()
 	{
 		return dy;
 	}
 	
 	public float getWidth()
 	{
 		return width;
 	}
 	
 	public float getHeight()
 	{
 		return height;
 	}
 	
 	public void render()
 	{
 		GL11.glTranslatef(x, y, 0);
 		GL11.glRotatef(angle, 0, 0, 1);
 		GL11.glTranslatef(-x, -y, 0);
 		
 		GL11.glColor3f(1, 1, 1);
 		GL11.glBegin(GL11.GL_QUADS);
 			GL11.glVertex2f(x, y);
 			GL11.glVertex2f(x + width, y);
 			GL11.glVertex2f(x + width, y + height);
 			GL11.glVertex2f(x, y + height);			
 		GL11.glEnd();
 	}
 	
 	public void update()
 	{
 		if(facing != null)
 		{
 			switch(facing)
 			{
 				case NORTH:
 					angle = 0.0f;
 					dx = 0;
 					if(addSpeed)
 						dy = -speed - Tank.SPEED;
 					else
 						dy = -speed;
 					break;
 				case SOUTH:
 					angle = 180.0f;
 					dx = 0;
 					if(addSpeed)
 						dy = speed + Tank.SPEED;
 					else
 						dy = speed;
 					break;
 				case EAST:
 					angle = 90.0f;
 					if(addSpeed)
 						dx = speed + Tank.SPEED;
 					else
 						dx = speed;
 					dy = 0;
 					break;
 				case WEST:
 					angle = 270.0f;
 					if(addSpeed)
 						dx = -speed - Tank.SPEED;
 					else
 						dx = -speed;
 					dy = 0;
 					break;
 			}
 		} else
 		{
 			
 		}
 		x += dx;
 		y += dy;
 		
 		if(x > Display.getWidth() || x < 0 || y > Display.getHeight() || y < 0)
 			PaddleGame.entities.remove(this);
 		
 		Pengine eng = new Pengine(new PVector(x + width/2, y + height/2), 10, 90, null);
		eng.setVVector(new VVector(0.5f, 0.5f));
 		eng.create();
 	}
 	
 	public void checkCollision()
 	{
 		for(int i = 0; i < PaddleGame.entities.size(); i++)
 		{
 			Entity e = PaddleGame.entities.get(i);
 			
 			if(e instanceof Collidable)
 				((Collidable) e).intersects(this);
 		}
 	}
 }
