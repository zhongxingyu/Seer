 /**
  * Main game
  * 
  * @author	Jamie of the Javateerz
  */
 
 package org.javateerz.ParkViewProtector;
 
 import java.io.*;
 import java.util.ArrayList;
 
 import org.javateerz.ParkViewProtector.Bosses.Boss;
 import org.javateerz.ParkViewProtector.Levels.*;
 import org.javateerz.ParkViewProtector.Menu.GameOver;
 import org.javateerz.ParkViewProtector.Staff.Staff;
 import org.javateerz.ParkViewProtector.Students.Student;
 import org.lwjgl.input.Keyboard;
 
 public class Game extends GameScreen implements Serializable
 {
 	// Number of pixels to move
 	public static final int MOVE_SPEED			= 1;
 	
 	// delay (in number of frames) before another attack can be used
 	// FIXME: should probably go in Staff
 	public static final int ATTACK_DELAY		= 20;
 	public static final int ITEM_USE_DELAY		= 20;
 	private int itemDelay						= 0;
 	public static final int TP_REGEN			= 5;
 	public static int tpRegen					= 0;
 	public static final int ICON_X_OFFSET		= 28;
 	public static final int ICON_Y_OFFSET		= 38;
 	public static final int ICON_SPACING		= 30;
 	
 	public static final int MAX_LEVEL			= 5;
 	
 	////////////////////////////////////////////////////
 	public static final int CHARGE_REGEN		= 10;
 	public static int chargeRegen				= 0;
 	
 	public static final int GAME_OVER_DELAY		= 5000;
 	
 	public static final double COUPLE_CHARGE_CHANCE	= 0.1;
 	public static final int COUPLE_CHARGE_AMOUNT	= 1;
 	
 	public static final double COUPLE_CHANCE	= 0.6;
 	public static final double ATTACK_CHANCE	= 0.1;
 	
 	public static final int MIN_NUM_MOVES		= 10;
 	public static final int MAX_NUM_MOVES		= 400;
 	
 	public static final int DECOUPLE_SPACING	= 40;
 	public static final int COUPLE_CHANCE_MULTIPLIER = 400;
 	
 	public static final int PLAYER_X			= 10;
 	public static final int PLAYER_Y			= 30;
 	public static double hpPercent;
 	public static double tpPercent;
 	
 	public static boolean cheatMode				= false;
 	
 	private static final long serialVersionUID	= 9L;
 	
 	//////////////////////////////////////////////////////////////////////////////////////
 	
 	private transient GameOver gameOver;
 	private transient Statistics stats;
 	
 	private int levelNum						= 1;
 	
 	private transient Boss boss;
 	private transient Level level;
 	private transient Sprite background;
 	private transient ArrayList<Wall> walls;
 	
 	private Staff player;
 	private ArrayList<Student> students			= new ArrayList<Student>();
 	private ArrayList<Couple> couples			= new ArrayList<Couple>();
 	private ArrayList<Attack> attacks			= new ArrayList<Attack>();
 	private ArrayList<Item> items				= new ArrayList<Item>();
 	private ArrayList<VisualFX> fx				= new ArrayList<VisualFX>();
 	private ArrayList<StatusIcon> icons			= new ArrayList<StatusIcon>();
 	
 	private StatusIcon stun;
 	private StatusIcon invul;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param p Instance of ParkViewProtector
 	 */
 	public Game(ParkViewProtector p)
 	{
 		init(p);
 		initGame();
 		
 		students					= level.getStudents();
 	}
 	
 	/**
 	 * Sets the driver
 	 * 
 	 * @param p Instance of ParkViewProtector
 	 */
 	public void init(ParkViewProtector p)
 	{
 		this.driver					= p;
 	}
 	
 	/**
 	 * Initializes statistics, visual effects, icons, and bosses
 	 */
 	private void initGame()
 	{
 		// initialize level
 		initLevel();
 		
 		if(level instanceof BossLevel)
 		{
 			boss					= ((BossLevel) level).getBoss();
 		}
 	}
 	
 	/**
 	 * Initializes the level and walls
 	 */
 	private void initLevel()
 	{
 		switch(levelNum)
 		{
 			default:
 				level				= new Level1(this);
 				break;
 				
 			case 2:
 				level				= new Level2(this);
 				break;
 				
 			case 3:
 				level				= new Level3(this);
 				break;
 				
 			case 4:
 				level				= new Level4(this);
 				break;
 				
 			case 5:
 				level				= new Level5(this);
 				break;
 		}
 		
 		background					= DataStore.INSTANCE.getSprite("bg/" + level.getBG());
 		walls						= level.getWalls();
 		
 		// initialize music
 		setMusic(level.getMusic());
 	}
 	
 	//////////////////////////////////////////////////////////////////////////////////////
 	// Setters and getters
 	//////////////////////////////////////////////////////////////////////////////////////
 	
 	public ArrayList<Student> getStudents()
 	{
 		return students;
 	}
 	
 	public boolean addStudent(Student stu)
 	{
 		return students.add(stu);
 	}
 	
 	public ArrayList<Couple> getCouples()
 	{
 		return couples;
 	}
 	
 	public ArrayList<Wall> getWalls()
 	{
 		return walls;
 	}
 	
 	public ArrayList<Attack> getAttacks()
 	{
 		return attacks;
 	}
 	
 	public Staff getPlayer()
 	{
 		return player;
 	}
 
 	public ArrayList<Item> getItems()
 	{
 		return items;
 	}
 
 	public ArrayList<StatusIcon> getIcons()
 	{
 		return icons;
 	}
 	
 	public ArrayList<VisualFX> getFX()
 	{
 		return fx;
 	}
 	
 	public boolean hasStatus(int status)
 	{
 		for(int i=0; i<icons.size(); i++)
 		{
 			if(icons.get(i).getStatus()==status)
 				return true;
 		}
 		return false;
 	}
 	
 	public boolean isBossLevel()
 	{
 		if(level instanceof BossLevel)
 			return true;
 		return false;
 	}
 	
 	public Boss getBoss()
 	{
 		return boss;
 	}
 	
 	public void setPlayer(Staff player)
 	{
 		this.player					= player;
 		this.player.moveTo(PLAYER_X, PLAYER_Y);
 		stun= new StatusIcon(this, Status.STUN);
 		invul= new StatusIcon(this, Status.INVULNERABLE);
 		
 		// INIRIRAIOOSFO STATISTICS
 		stats						= new Statistics(this);
 	}
 	
 	//////////////////////////////////////////////////////////////////////////////////////
 	
 	public void show()
 	{
 		Student currStudent; 
 		Couple currCouple;
 		Attack currAttack;
 		
 		// ensure music is playing
 		ensureMusicPlaying();
 		
 		// key handling
 		if(Keyboard.isKeyDown(KeyboardConfig.MENU))
 		{
 			ParkViewProtector.showMenu			= true;
 		}
 		else if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
 		{
 			driver.quitGame();
 		}
 		
 		//////////////////////////////////////////////////////////////////////////////////
 		// Are we dead?
 		//////////////////////////////////////////////////////////////////////////////////
 		
 		if(player.getHp() <= 0)
 		{
 			gameOver();
 			return;
 		}
 		
 		//////////////////////////////////////////////////////////////////////////////////
 		// Draw Background
 		//////////////////////////////////////////////////////////////////////////////////
 		
 		background.draw();
 
 		//////////////////////////////////////////////////////////////////////////////////
 		// Draw walls
 		//////////////////////////////////////////////////////////////////////////////////
 		
 		for(Wall w : walls)
 		{
 			w.draw();
 		}
 		
 		//////////////////////////////////////////////////////////////////////////////////
 		// Draw students
 		//////////////////////////////////////////////////////////////////////////////////
 		
 		for(int i = 0; i < students.size(); i++)
 		{
 			currStudent			= students.get(i);
 			currStudent.draw();
 			currStudent.step(this);
 		}
 		
 		//////////////////////////////////////////////////////////////////////////////////
 		// Draw couples
 		//////////////////////////////////////////////////////////////////////////////////
 		
 		for(int i = 0; i < couples.size(); i++)
 		{
 			currCouple			= couples.get(i);
 			currCouple.draw();
 			
 			currCouple.step(this);
 		}
 		
 		//////////////////////////////////////////////////////////////////////////////////
 		// Draw boss
 		//////////////////////////////////////////////////////////////////////////////////
 		
 		if(level instanceof BossLevel && boss != null)
 		{
 			boss.draw();
 			if(boss.getHp()>0)
 				boss.step(this);
 		}
 		
 		//////////////////////////////////////////////////////////////////////////////////
 		// Draw player
 		//////////////////////////////////////////////////////////////////////////////////
 		
 		player.draw();
 		player.step(this);
 		
 		//////////////////////////////////////////////////////////////////////////////////
 		// Draw attacks
 		//////////////////////////////////////////////////////////////////////////////////
 		
 		for(int i = 0; i < attacks.size(); i++)
 		{
 			currAttack			= attacks.get(i);
 			currAttack.draw();
 			
 			currAttack.move(MOVE_SPEED);
 			
 			// is the attack off the screen?
 			/*if(currAttack.getBounds().getX() < -currAttack.getBounds().getWidth() ||
 					currAttack.getBounds().getX() > ParkViewProtector.WIDTH ||
 					currAttack.getBounds().getY() < -currAttack.getBounds().getHeight() ||
 					currAttack.getBounds().getY() > ParkViewProtector.HEIGHT)
 			{
 				System.out.println("Attack #" + i +" went off screen, removing");
 				
 				attacks.remove(i);
 				i--;
 			}*/
 			
 			if(i >= 0 && currAttack.over())
 			{
 				attacks.remove(i);
 				i--;
 			}
 		}
 		
 		//////////////////////////////////////////////////////////////////////////////////
 		// Draw items
 		//////////////////////////////////////////////////////////////////////////////////
 		// FIXME: these should probably be drawn before everything else
 		
 		for(int i = 0;i < items.size();i++)
 		{
 				items.get(i).draw();
 				if(items.get(i).getBounds().intersects(player.getBounds()))
 				{
 					player.pickItem(items.get(i));
 					items.remove(items.get(i));
 				}
 		}
 		
 		//////////////////////////////////////////////////////////////////////////////////
 		// Draw FXs
 		//////////////////////////////////////////////////////////////////////////////////
 		
 		for(int i=0; i<fx.size(); i++)
 		{
 			fx.get(i).draw();
 			if(fx.get(i).tick())
 			{
 				fx.remove(fx.get(i));
 			}
 		}
 		
 		//////////////////////////////////////////////////////////////////////////////////
 		// Move the player
 		//////////////////////////////////////////////////////////////////////////////////
 		
 		int distX = 0, distY = 0;
 		
 		if(Keyboard.isKeyDown(KeyboardConfig.UP) &&
 			!Keyboard.isKeyDown(KeyboardConfig.DOWN) &&
 			player.getBounds().getY() > 0)
 		{
 			distY						= -MOVE_SPEED;
 		}
 		
 		if(Keyboard.isKeyDown(KeyboardConfig.DOWN) &&
 				!Keyboard.isKeyDown(KeyboardConfig.UP) &&
 				player.getBounds().getY() < ParkViewProtector.HEIGHT - player.getBounds().getHeight())
 		{
 			distY						= MOVE_SPEED;
 		}
 		
 		if(Keyboard.isKeyDown(KeyboardConfig.LEFT) &&
 				!Keyboard.isKeyDown(KeyboardConfig.RIGHT) &&
 				player.getBounds().getX() > 0)
 		{
 			distX						= -MOVE_SPEED;
 		}
 		
 		if(Keyboard.isKeyDown(KeyboardConfig.RIGHT) &&
 				!Keyboard.isKeyDown(KeyboardConfig.LEFT) &&
 				player.getBounds().getX() < ParkViewProtector.WIDTH - player.getBounds().getWidth())
 		{
 			distX						= MOVE_SPEED;
 		}
 		
 		// if we are moving, check to see if location is clear, then move
 		if((distX != 0 || distY != 0) && !player.isAttacking() && !player.isStunned())
 		{
 			if(!player.canMove(player.getNewBounds(distX,distY)))
 			{
 				for(int i=0; i<students.size(); i++)
 				{
 					Student s = students.get(i);
 					if(player.getNewBounds(MOVE_SPEED).intersects(s.getBounds()))
 					{
 						player.push(students.get(i));
 					}
 				}
 			}
 			else if(player.canMove(player.getNewBounds(distX, distY)))
 			{
 				player.move(distX, distY);
 			}
 		}
 		
 		//////////////////////////////////////////////////////////////////////////////////
 		// Create attacks
 		//////////////////////////////////////////////////////////////////////////////////
 		
 		if((Keyboard.isKeyDown(KeyboardConfig.ATTACK1) || Keyboard.isKeyDown(KeyboardConfig.ATTACK2)
 				|| Keyboard.isKeyDown(KeyboardConfig.ATTACK3)) && !player.isAttacking())
 		{
 			int attackKey=0;
 			if(Keyboard.isKeyDown(KeyboardConfig.ATTACK1))
 			{
 				attackKey=0;
 			}
 			else if(Keyboard.isKeyDown(KeyboardConfig.ATTACK2))
 			{
 				attackKey=1;
 			}
 			else if(Keyboard.isKeyDown(KeyboardConfig.ATTACK3))
 			{
 				attackKey=2;
 			}
 			player.attack(attackKey);
 		}
 		
 		throttleSpeed();
 		
 		//////////////////////////////////////////////////////////////////////////////////
 		// Use items
 		// "Yeah, yeah, Jamie, I know; this is terrible placement"
 		// 			-Donny
 		//////////////////////////////////////////////////////////////////////////////////
 		
 		if(Keyboard.isKeyDown(KeyboardConfig.USE_HP_ITEM))
 		{
 			if(itemDelay == ITEM_USE_DELAY)
 			{
 				System.out.println("Game class says you healed hp! - itemDelay is " + itemDelay);
 				player.useItem('h');
 				itemDelay = 0;
 			}
 		}
 		if(Keyboard.isKeyDown(KeyboardConfig.USE_TP_ITEM))
 		{
 			if(itemDelay == ITEM_USE_DELAY)
 			{
 				System.out.println("Game class says you healed tp! - itemDelay is " + itemDelay);
 				player.useItem('t');
 				itemDelay = 0;
 			}
 		}
 		if(itemDelay < ITEM_USE_DELAY)
 		{
 			itemDelay++;
 		}
 		
 		if(Keyboard.isKeyDown(KeyboardConfig.SHOW_CHARGES))
 		{
 			showHp();
 		}
 		
 		//////////////////////////////////////////////////////////////////////////////////
 		// Draw statistics
 		//////////////////////////////////////////////////////////////////////////////////
 		// these are painted last to ensure that they are always on top
 		
 		stats.draw(player, levelNum);
 		
 		drawPlayerEffects();
 		
 		//////////////////////////////////////////////////////////////////////////////////
 		// Advance to next level?
 		//////////////////////////////////////////////////////////////////////////////////
 		
 		boolean advanceLevel	= false;
 		
 		if(level instanceof BossLevel)
 		{
 			advanceLevel		= ((BossLevel) level).levelComplete();
 		}
 		else if(couples.size() <= 0)
 		{
 			advanceLevel		= true;
 			
 			for(Student s : students)
 			{
 				if(s.getHp() > 0)
 				{
 					advanceLevel		= false;
 					break;
 				}
 			}
 		}
 		
 		if(advanceLevel)
 		{
 			if(levelNum < MAX_LEVEL)
 			{
				advanceLevel();
 			}
 			else
 				gameOver();
 		}
 	}
 	
	public void advanceLevel()
	{
		levelNum++;
		
		initGame();
		
		students			= level.getStudents();
	}
	
 	public void drawPlayerEffects()
 	{
 		if(!hasStatus(Status.STUN) && player.getStunHit())
 		{
 			icons.add(stun);
 		}
 		
 		if(!hasStatus(Status.INVULNERABLE) && player.getInvulHit())
 		{
 			icons.add(invul);
 		}
 		
 		for(int i=0; i<icons.size(); i++)
 		{
 			icons.get(i).setTime();
 			icons.get(i).draw(ICON_X_OFFSET+i*ICON_SPACING, ICON_Y_OFFSET);
 			if(icons.get(i).isOver())
 			{
 				if(icons.get(i).getStatus()==Status.INVULNERABLE)
 					player.setInvulHit(false);
 				else if(icons.get(i).getStatus()==Status.STUN)
 					player.setStunHit(false);
 				icons.remove(i);
 			}
 		}
 	}
 	
 	public void hitFX(int x, int y)
 	{
 		VisualFX effect=new VisualFX(this, "blip", 10);
 		effect.moveTo(x-effect.getBounds().getWidth()/2, y-effect.getBounds().getHeight()/2);
 		fx.add(effect);
 	}
 	
 	/**
 	 * Returns the specified wall
 	 */
 	public Wall getWall(int wallId)
 	{
 		return walls.get(wallId);
 	}
 	
 	/**
 	 * "Game over" message
 	 */
 	public void gameOver()
 	{
 		bgMusic.stop();
 		
 		if(gameOver == null)
 			gameOver				= new GameOver(driver);
 		
 		gameOver.show();
 	}
 	
 	/**
 	 * Shows the HP of all characters on-screen
 	 */
 	public void showHp()
 	{
 		player.showHp();
 		
 		for(int i = 0;i < students.size();i++)
 		{
 			if(students.get(i).getHp() > 0)
 			{
 				students.get(i).showHp();
 			}
 		}
 		
 		for(int i = 0; i<couples.size(); i++)
 		{
 			if(couples.get(i).getHp()>0)
 			{
 				couples.get(i).showHp();
 			}
 		}
 		
 		if(level instanceof BossLevel && boss.getHp()>0)
 			boss.showHp();
 	}
 	
 	/**
 	 * Read a save file
 	 * 
 	 * @param os Object input stream
 	 */
 	@SuppressWarnings("unchecked")
 	private void readObject(ObjectInputStream os)
 	{
 		try
 		{
 			levelNum	= os.readInt();
 			
 			player		= (Staff) os.readObject();
 			students	= (ArrayList<Student>) os.readObject();
 			couples		= (ArrayList<Couple>) os.readObject();
 			attacks		= (ArrayList<Attack>) os.readObject();
 			items		= (ArrayList<Item>) os.readObject();
 			fx			= (ArrayList<VisualFX>) os.readObject();
 			icons		= (ArrayList<StatusIcon>) os.readObject();
 			
 			initGame();
 		}
 		catch(ClassNotFoundException e)
 		{
 			driver.error("Your save file appears to be corrupt. A ClassNotFoundException occurred.", true);
 		}
 		catch(IOException e)
 		{
 			driver.error("Unable to read the save file", true);
 		}
 	}
 	
 	/**
 	 * Write a save file
 	 * 
 	 * @param os Object output stream
 	 * @throws IOException
 	 */
 	private void writeObject(ObjectOutputStream os) throws IOException
 	{
 		os.writeInt(levelNum);
 		
 		os.writeObject(player);
 		os.writeObject(students);
 		os.writeObject(couples);
 		os.writeObject(attacks);
 		os.writeObject(items);
 		os.writeObject(fx);
 		os.writeObject(icons);
 	}
 
 }
