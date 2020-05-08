 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.geom.AffineTransform;
 import java.awt.image.*;
 import java.io.File;
 import java.io.IOException;
 import java.util.Random;
 import javax.swing.*;
 import java.util.concurrent.*;
 import javax.imageio.ImageIO;
 
 // This class is the master content manager, it has a list of managers.
 // Also, this class may manange a few list of entities on it's own.
 public class Content extends JPanel
 {
 	private final Frame frame;
 	private CopyOnWriteArrayList<Manager> list;
 	private Manager<Platform> pltfrmMng;
 	private Manager<Enemy> enemyMng;
 	public static Manager<Bullet> bulletMng;
 	public static Manager<EnemyDeath> nmeDeathMng;
 	public static Manager<PowerUp> powerUpsMng;
 	public static Manager<Player> playerManager;
 	private Player playChar;
 	//private final Manager target;
 	private boolean run;
 	private Room currRoom;
 	public static Graphics2D darkness;
 	private boolean setOpaque = false;
 	private Graphics2D bloodOverlayG;
 	private static float bloodOverlay = 0;
 	static private BufferedImage blood;
 	Vector currSpawn;
 	// Constructor
 	public Content(Frame fIn)
 	{
 		frame = fIn;
 		list = new CopyOnWriteArrayList<>();
 		run = true;
 
 		//Create the room
		setBackground(Color.WHITE);
 		currRoom = new Room("start.json", frame);
 		//Create the player and their spawn point
 		currSpawn = currRoom.getWestSpawn();
 		
 		//currRoom.getData();
 
 		//Create the platforms in the starting room
 		pltfrmMng = new Manager(frame, this);
 		add(pltfrmMng);
 
 		//Spawn all of the enemies
 		enemyMng = new Manager(frame, this);
 		add(enemyMng);
 
 		//Create all of the powerups
 		powerUpsMng = new Manager(frame, this);
 		add(powerUpsMng);
 
 		bulletMng = new Manager(frame, this);
 		add(bulletMng);
 		
 		nmeDeathMng = new Manager(frame, this);
 		add(nmeDeathMng);
 
 		//Create the map
 		Map.defaultMap = new Map(currRoom);
 
 		playerManager = new Manager<>(frame, this);
 		add(playerManager);
 		playChar = new Player(new Vector(0, 0));
 		playerManager.add(playChar);
 		
 		//Get the room configured
 		reloadRoom();
 		setBloodImage();
 	}
 	public void add(Manager mIn)
 	{
 		list.add(mIn);
 	}
 	public Manager getLast()
 	{
 		if(list.isEmpty())
 		{
 			return null;
 		}
 		return list.get(list.size() - 1);
 	}
 	public Manager getType(String sIn)
 	{
 		for(Manager m : list)
 		{
 			if(m.getType() != null)
 			{
 				if(m.getType().equals(sIn))
 				{
 					return m;
 				}
 			}
 		}
 		return null;
 	}
 	public void remove(Manager mIn)
 	{
 		mIn.dispose();
 		list.remove(mIn);
 	}
 	public void dispose()
 	{
 		for(Manager m : list)
 		{
 			remove(m);
 		}
 	}
 	public void logic()
 	{
 		//Pause the game
 		if(Keyboard.isPressed(KeyEvent.VK_P))
 		{
 			run = !run;
 			Keyboard.release(KeyEvent.VK_P);
 			if(!run)
 			{
 				setOpaque = !setOpaque;
 			}
 			else
 			{
 				setOpaque = !setOpaque;
 			}
 		}
 		//Update everything if the game is not paused
 		if(run)
 		{
 			for(Manager m : list)
 			{
 				m.logic();
 			}
 		}
 
 		//Move to the next room if necessary
 		moveRoom();
 		//Respawn the player and reload the room if the character died
 		if(!((Player)getType("Player").get(0)).isAlive())
 		{
 			reloadRoom();
 		}
 	}
 	public void reloadRoom()
 	{
 		if(currRoom.getRoomName().equals("end.json"))
 		{
 			frame.gameComplete.release();
 			return;
 		}
 		
 		//Load the room. This adds overhead, but it either adds it here
 		// or has to clone a lot of objects
 		currRoom.loadRoom();
 		pltfrmMng.drop();
 		bulletMng.drop();
 		nmeDeathMng.drop();
 		enemyMng.drop();
 		powerUpsMng.drop();
 		pltfrmMng.addAll(currRoom.getPlatforms());
 		enemyMng.addAll(currRoom.getEnemies());
 		powerUpsMng.addAll(currRoom.getPowerUps());
 		getType("Player").get(0).setCenter(currSpawn);
 		//System.out.println("(" + getType("Player").get(0).getCenter().x + ", " + getType("Player").get(0).getCenter().y + ")");
 		((Player)getType("Player").get(0)).revive();
 
 	}
 	public void moveRoom()
 	{
 		Vector currPos = getType("Player").get(0).getCenter();
 
 		boolean isMove = false;
 
 		//Go north?
 		if(currPos.y < 0)
 		{
 			//Read the data about the room
 			currRoom.setRoomName(currRoom.getNorth());
 			reloadRoom();
 			currSpawn = currRoom.getSouthSpawn();
 			getType("Player").get(0).setCenter(currSpawn);
 			Map.defaultMap.Update(0, -1, currRoom);
 		}
 		//Go south?
 		else if(currPos.y > frame.getHeight())
 		{
 			//Read the data about the room
 			currRoom.setRoomName(currRoom.getSouth());
 			reloadRoom();
 			currSpawn = currRoom.getNorthSpawn();
 			getType("Player").get(0).setCenter(currSpawn);
 			Map.defaultMap.Update(0, 1, currRoom);
 		}
 		//Go east?
 		else if(currPos.x > frame.getWidth())
 		{
 			//Read the data about the room
 			currRoom.setRoomName(currRoom.getEast());
 			reloadRoom();
 			currSpawn = currRoom.getWestSpawn();
 			getType("Player").get(0).setCenter(currSpawn);
 			Map.defaultMap.Update(1, 0, currRoom);
 		}
 		//Go west?
 		else if(currPos.x < 0)
 		{
 			//Read the data about the room
 			currRoom.setRoomName(currRoom.getWest());
 			reloadRoom();
 			currSpawn = currRoom.getEastSpawn();
 			getType("Player").get(0).setCenter(currSpawn);
 			Map.defaultMap.Update(-1, 0, currRoom);
 		}
 	}
 	public static void setBloodOverlay()
 	{
 		setBloodImage();
 		bloodOverlay = 1;
 	}
 	private static void setBloodImage()
 	{
 		String path = new String("bin\\images\\splatter\\Splatter" + Integer.toString((int)(Math.random() * 5) + 1) + ".png");
 		try
 		{
 			blood = ImageIO.read(new File(path));
 			System.out.println("loaded: " + path);
 		}
 		catch(IOException e)
 		{
 			System.out.println("Error Loading Blood Splatter Image!");
 			System.out.println("Bad Path: " + path);
 		}
 	}
 	@Override
 	public void paint(Graphics gIn)
 	{
 		super.paint(gIn);
 		Graphics2D g = (Graphics2D)gIn;
 		//AffineTransform trans = new AffineTransform();
 		//trans.scale(0.5, 0.5);
 		//g.transform(trans);
 		//g.scale(0.5, 0.5);
 
 		BufferedImage temp = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
 		darkness = temp.createGraphics();
 		darkness.setColor(Color.BLACK);
 		darkness.fillRect(0, 0, frame.getWidth(), frame.getHeight());
 		darkness.setComposite(AlphaComposite.DstOut);
 
 		currRoom.paint(g);
 		for(Manager m : list)
 		{
 			m.paint(g);
 		}
 		g.drawImage(temp, null, 0, 0);
 		if(!setOpaque)
 		{ //If the game is "running" (not paused)
 			if(bloodOverlay > 0)
 			{
 				bloodOverlayG = blood.createGraphics();
 				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER).derive(bloodOverlay));
 				g.drawImage(blood, 0, 0, getWidth(), getHeight(), frame);
 				bloodOverlay -= 0.05;
 				g.setComposite(AlphaComposite.SrcOver);
 			}
 		}
 		else
 		{ //If the game is "Paused"
 			//This is used to Gray out the background When Pause is Selected
 			g.setColor(new Color(255, 255, 255, 51)); //Lets make a box that is 20% opaque!
 			//g.setColor(Color.black); //I was going to do this, but I really like the random color that it makes it haha
 			g.fillRect(0, 0, getWidth(), getHeight());//Lets make it the size of the window!
 			g.setColor(Color.WHITE);
 			drawCenteredString(g, "GAME PAUSED", getWidth() / 2, 15); //Let the Player know the game is Paused
 			drawCenteredString(g, "Press 'P' to Resume Game", getWidth() / 2, 30); //And how they Can Return to it!
 
 			int i = 1;
 			Font tempFont = g.getFont();
 			g.setFont(g.getFont().deriveFont(Font.BOLD));
 			g.drawString("Power Ups:", 10, 50);
 			g.setFont(tempFont);
 			for(PowerUp.Type t : Player.powerUps)
 			{
 				g.drawString(t.toString(), 30, 25 * (i++) + 50);
 			}
 
 			Map.defaultMap.draw(g, getWidth() / 4, getHeight() / 4, getWidth() / 2, getHeight() / 2);
 		}
 	}
 	public static void drawCenteredString(Graphics2D g, String s, int x, int y)
 	{
 		int centerX = (int)g.getFontMetrics().getStringBounds(s, g).getWidth() / 2;
 		g.drawString(s, x - centerX, y);
 	}
 }
