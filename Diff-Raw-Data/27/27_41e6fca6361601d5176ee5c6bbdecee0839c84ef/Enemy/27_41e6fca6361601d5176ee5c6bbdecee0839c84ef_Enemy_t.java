 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 import java.awt.AlphaComposite;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.awt.event.KeyEvent;
 import org.codehaus.jackson.annotate.JsonSubTypes;
 import org.codehaus.jackson.annotate.JsonTypeInfo;
 
 @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
 @JsonSubTypes(
 		{
 	@JsonSubTypes.Type(value = Target.class, name = "Target"),
 	@JsonSubTypes.Type(value = Hopper.class, name = "Hopper"),
 	@JsonSubTypes.Type(value = Pacer.class, name = "Pacer"),
 	@JsonSubTypes.Type(value = Floater.class, name = "Floater"),
 	@JsonSubTypes.Type(value = BreakableWall.class, name = "BreakableWall")
 })
 /**
  *
  * @author Branden
  */
 public abstract class Enemy extends Entity
 {
 	//How many hits before the poor guy dies
 	protected int health;
 	private boolean alive;
 	private int damaged = 0;
 	// Constructor.
 	Enemy()
 	{
		this(new Vector(50, 50), 50, 50);
 		alive = true;
 	}
 	public Enemy(Vector vIn, int wIn, int hIn)
 	{
 		super(vIn, wIn, hIn);
 		alive = true;
		if(Frame.easyMode)
			health = 3;
		else
			health = 5;
 	}
 	public void setHp(int hp)
 	{
 		health = hp;
 	}
 	public void damage()
 	{
 		health--;
 		damaged = 3;
 		if(health <= 0)
 		{
 			kill();
 		}
 	}
 	public void kill()
 	{
 		alive = false;
 		Content.nmeDeathMng.add(new EnemyDeath(getCenter(), sprite));
 		getCenter().x = -1000; // Move off screen.
 		getCenter().y = -1000;
 	}
 	public boolean isAlive()
 	{
 		return alive;
 	}
 	@Override
 	public abstract void logic();
 	@Override
 	public void paint(Graphics2D gIn)
 	{
 		if(Keyboard.isPressed(KeyEvent.VK_J) && Keyboard.isPressed(KeyEvent.VK_M) 
 				&& Mouse.X()> 780 && Mouse.Y()> 580) //Cheat code for debug purposes
 				damage();//technically should happen in logic thread, but it's a cheat code so who cares
 		if(!isAlive())
 		{
 			return;
 		}
 		BufferedImage image = new BufferedImage(Frame.WIDTH, Frame.HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);
 		Graphics2D g2d = image.createGraphics();
 		sprite.draw(g2d, getCenter());//draw sprite
 		if(damaged > 0)//enemies should flash white when hit
 		{
 			g2d.setComposite(AlphaComposite.SrcIn);
 			g2d.fillRect(0, 0, Frame.WIDTH, Frame.HEIGHT);
 			damaged--;
 		}
 		gIn.drawImage(image, 0, 0 , null);
 		//Content.darkness.fillOval((int)getCenter().x - 100, (int)getCenter().y - 100, 200, 200);
 	}
 	@Override
 	public abstract void dispose();
 	
 	protected Vector getLoc()
 	{
 		return Content.playerManager.get(0).getCenter();
 	}
 }
