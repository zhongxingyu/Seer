 /*
  * Adventron - Stan Schwertly
  * Port of an old game I made. Learning git too!
  *  
  * Adventron.java
  * 
  * http://www.schwertly.com for my blog
  * http://github.com/Stantheman/Adventron-Game for the GitHub
  */
 
 import java.applet.*;
 import java.awt.*;
 import java.util.ArrayList;
 
 /*
  * - don't forget about buser in linodeland for java talk!
  */
 
 public class Adventron extends Applet implements Runnable
 {	
 	private Image dbImage;
 	private Graphics dbg;
 
 	// Game variables
 	private Player player = new Player();
 	private ArrayList <Map> map = new ArrayList<Map>();
 	private ArrayList <Bullet> bullets = new ArrayList<Bullet>();
 	private ArrayList <Monster> monsters = new ArrayList<Monster>();
 	private Statusbar bar = new Statusbar(player);
 	
 	public void init() 
 	{ 
 		setBackground(Color.black);
 		
 		// Suck in the levels
 		for (int i=0; i<9; i++)
 		{
 			map.add(new Map());
 			map.get(i).readLevel(new String("level" + i + ".dat"), dbg);
 		}
 		
 		// Give everyone local copies
 		player.setMap(map.get(0));
 		Bullet.initWalls();
 		Bullet.setMap(map.get(0));
 		bar.updateStatus();
 		
 		for (int i=0; i<map.get(0).getMonsterPosition().size(); i++)
 		{
 			monsters.add(new Monster(map.get(0).getMonsterPosition().get(i)));
 		}
 	}
 	
 	public void start()
 	{		
 		Thread th = new Thread(this);
 		th.start();
 	}
 	
 	public void stop() { }
 	
 	public void destroy() { }
 	
 	public void run() 
 	{
 		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
 		
 		while (true)
 		{
 			// update the player
 			if (!player.move())
 			{
 				player.setMap(map.get(player.getRoom()));
 				Bullet.setMap(map.get(player.getRoom()));
 				bullets.clear();
 				monsters.clear();
 				for (int i=0; i<map.get(player.getRoom()).getMonsterPosition().size(); i++)
 				{
					monsters.add(new Monster(map.get(0).getMonsterPosition().get(i)));
 				}
 			}
 			
 			// update the bullets
 			for (int i=0; i<bullets.size(); i++)
 			{
 				bullets.get(i).changePosition(player, monsters);
 				
 				if (bullets.get(i).getQuadrant() == Map.OUT_OF_BOUNDS)
 				{
 					bullets.remove(i);
 					i--;
 					continue;
 				}
 				
 				if (player.isHit())
 				{
 					player.setHealth(player.getHealth()-1);
 					player.setHit(false);
 					bar.updateStatus();
 				}
 			}
 			
 			// update the monsters
 			for (int i=0; i<monsters.size(); i++)
 			{
 				monsters.get(i).changePosition(
 						map.get(player.getRoom()).getWalls(monsters.get(i).getQuadrant()), bullets);
 				
 				if (monsters.get(i).isHit())
 				{
 					monsters.remove(i);	
 					player.addToScore(100);
 					bar.updateStatus();
 					i--;
 				}
 			}
 			
 			repaint();
 		
 			try
 			{
 				Thread.sleep(20);
 			}
 			catch (InterruptedException ex)
 			{}
 
 			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
 		}
 	}
 	
 	public void paint(Graphics g) 
 	{ 
 		// draw the walls
 		g.drawImage(map.get(player.getRoom()).image, 0, 0, null);
 		
 		// draw the player
 		g.setColor(Player.COLOR);
 		
 		// This is temporary. Player doesn't get drawn if hit
 		if (player.getHealth()>0)
 			g.fillRect(player.getPosition().x, 
 				   player.getPosition().y, 
 				   Player.WIDTH, 
 				   Player.HEIGHT);
 
 		// draw the monsters
 		g.setColor(Monster.COLOR);
 		for (int i=0; i<monsters.size(); i++)
 		{
 			g.fillRect(monsters.get(i).getPosition().x,
 					monsters.get(i).getPosition().y,
 					Monster.WIDTH, Monster.HEIGHT);
 		}
 		
 		// draw the bullets
 		g.setColor(Bullet.COLOR);
 		for (int i=0; i<bullets.size(); i++)
 		{
 			g.fillRect(bullets.get(i).getPosition().x, 
 					   bullets.get(i).getPosition().y, 
 					   Bullet.WIDTH, Bullet.HEIGHT);
 		}
 		
 		g.setColor(Color.white);
 		g.drawString(bar.toString(), 20, 20);
 	}
 	
 	public void update(Graphics g)
 	{
 		// java reminder: initialize buffer for double buffering
 		if (dbImage == null)
 		{
 			dbImage = createImage(this.getSize().width, this.getSize().height);
 			dbg = dbImage.getGraphics();
 		}
 	    
 		dbg.setColor(getBackground());
 		dbg.fillRect(0, 0, this.getSize().width, this.getSize().height);
 	
 		dbg.setColor(getForeground());
 		paint(dbg);
 	
 		g.drawImage(dbImage, 0, 0, this);
 	}
 	
 	public boolean keyDown(Event e, int key) 
 	{
 		if (key == Event.LEFT)
 		{
 			player.setDirection(Player.LEFT);
 			player.setFacing(Player.LEFT);
 		} 
 		if (key == Event.RIGHT)
 		{
 			player.setDirection(Player.RIGHT);
 			player.setFacing(Player.RIGHT);
 		}		
 		if (key == Event.UP)
 		{
 			player.setDirection(Player.UP);
 			player.setFacing(Player.UP);
 		}
 		if (key == Event.DOWN)
 		{
 			player.setDirection(Player.DOWN);
 			player.setFacing(Player.DOWN);
 		}
 		// user presses space bar
 		if (key == 32)
 		{
 			// Bullet gets added to the outside of the box to avoid accidental hits
 			if (player.getFacing() == Player.LEFT)
 			{
 				bullets.add(new Bullet(
 					player.getPosition().x,
 					player.getPosition().y,
 					Player.LEFT));
 			}
 			else if (player.getFacing() == Player.RIGHT)
 			{
 				bullets.add(new Bullet(
 						player.getPosition().x + Player.WIDTH,
 						player.getPosition().y,
 						Player.RIGHT));				
 			}
 			else if (player.getFacing() == Player.UP)
 			{
 				bullets.add(new Bullet(
 						player.getPosition().x,
 						player.getPosition().y,
 						Player.UP));
 			}
 			else 
 			{
 				bullets.add(new Bullet(
 						player.getPosition().x,
 						player.getPosition().y + Player.HEIGHT,
 						Player.DOWN));
 			}
 		}
 		if (key == Event.ENTER)
 		{
 			bar.switchDebug();
 			bar.updateStatus();
 		}
 		return true;
 	}
 	
 	public boolean keyUp(Event e, int key) 
 	{
 		if ( (key == Event.LEFT) || 
 				(key == Event.RIGHT) ||
 				(key == Event.UP) ||
 				(key == Event.DOWN) )
 		{
 			player.setDirection(Player.STILL);
 		}
 		return true;
 	}
 }
