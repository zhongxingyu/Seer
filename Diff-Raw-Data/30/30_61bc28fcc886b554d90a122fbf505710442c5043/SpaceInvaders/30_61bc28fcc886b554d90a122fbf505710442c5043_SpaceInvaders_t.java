 /**
  * 
  */
 package fr.iutvalence.java.projet.spaceinvaders;
 
 import java.util.Arrays;
 
 /**
  * A space invader game.<br/>
  * 
  * @author Gallet Guyon
  */
 public class SpaceInvaders
 {	
 	//************* Constant *************//
 	/**
 	 * It defines the number of monsters you have in tabMonster by default,
 	 * if it's not set in constructor. 
 	 */
 	// FIXME (SEEN) respect coding conventions
 	private static final int NBMONSTERDEFAULT = 20;
 
 	/**
 	 * It defines the number of tank you have in tabTank by default,
 	 * if it's not set in constructor.
 	 */
 	// FIXME (SEEN) respect coding conventions
 	private static final int NBTANKDEFAULT = 1;
 	
 	/**
 	 * It defines the maximum (default) of X axis,
 	 * if it's not set in constructor.
 	 */
 	// FIXME (SEEN) respect coding conventions
 	private static final int XGRID = 300;
 	
 	/**
 	 * It defines the maximum (default) of Y axis,
 	 * if it's not set in constructor.
 	 */
 	// FIXME (SEEN) respect coding conventions
 	private static final int YGRID = 300;
 	
 	/**
 	 * Default delta between 2 monsters
 	 */
 	// FIXME (SEEN) respect coding conventions
 	private static final int DEFAULTDELTA = 2;
 	
 	/**
 	 * Default size of element (e.g. Doc Movable)
 	 */
 	// FIXME (SEEN) respect coding conventions
 	private static final int DEFAULTSIZE = 10;
 	
 	//************* Variable *************//
 	/**
 	 * Boolean to know if the game is finished
 	 */
 	private boolean work;
 	
 	/**
 	 * The maximum size of the area
 	 */
 	private final Coordinates maxSize;
 	
 
 	/**
 	 * Array containing all monsters
 	 */
 	// FIXME (SEEN) why not rename it just "monsters" ?
 	private Movable[] monsters;
 
 	/**
 	 * Array containing all tanks.
 	 */
 	// FIXME (SEEN) why not rename it just "tanks" ?
 	private Movable[] tanks;
 
 	
 	//************************** Constructors **************************//
 	/**
 	 * Initialize the game.<br/>
 	 * This is the default constructor. It sets the number of tanks to 1, the number of monsters to 20,
 	 * the X axis to 300, and the Y axis to 300 too.<br/>
 	 * If you don't want to use this default characteristic use another constructor 
 	 */
 	public SpaceInvaders()
 	{
 		this.work = true;
 		this.maxSize = new Coordinates(XGRID, YGRID);
 		initTab(NBMONSTERDEFAULT, NBTANKDEFAULT);
 	}
 	
 	/**
 	 * Initialize the game.<br/>
 	 * This is the default constructor. It sets the X axis to 300, and the Y axis to 300 too.<br/>
 	 * If you don't want to use this default characteristic use another constructors
 	 * 
 	 * @param nbMonster Set the number of Monster you want instantiate (with default constructors, it sets to 20)
 	 * @param nbTank Set the number of Tank you want instantiate (with default constructors, it sets to 20)
 	 */
 	public SpaceInvaders(int nbMonster, int nbTank)
 	{
 		this.work = true;
 		this.maxSize = new Coordinates(XGRID, YGRID);
 		initTab(nbMonster, nbTank);
 	}
 	
 	/**
 	 * Initialize the game.<br/>
 	 * This is the default constructor. It uses no default value.
 	 * 
 	 * @param nbMonster Set the number of Monster you want instantiate (with default constructors, it sets to 20)
 	 * @param nbTank Set the number of Tank you want instantiate (with default constructors, it sets to 20)
  	 * @param nbTank Set the X axis (with default constructors, it sets to 300)
  	 * @param nbTank Set the Y axis (with default constructors, it sets to 300)
 	 * @param Max 
 	 */
 	public SpaceInvaders(int nbMonster, int nbTank, Coordinates Max)
 	{
 		this.work = true;
 		this.maxSize = Max;
 		initTab(nbMonster, nbTank);
 	}
 
 	//************************** Methods **************************//
 	
 	
 	/**
 	 * This method begins the game. It's the only entry point. 
 	 */
 	public void start() throws InterruptedException
 	{
 		iteration();
 	}
 	
 	/**
 	 * Initialize the table of movable elements.<br/>
 	 * Algorithm for set-up the monsters' position on the grid, and also Tank.
 	 * It positions the tank of the middle of the grid and the monster
 	 * from the top left to the bottom right (like writing in English or French) 
 	 * 
 	 * @param nbMonster Set the number of monster (The maximum is set (to my mind) to 250, after I'm offload one's responsibilities) 
 	 * @param nbTank Set the number of tank (not implemented yet, so the maximum is 1)
 	 */
 	// FIXME (SEEN) rename parameters
 	// Why I have to rename this parameters?
 	private void initTab(int nbMonster, int nbTank)
 	{
 		// local variable
 		Coordinates tank_position = new Coordinates((this.maxSize.getX() / 2) 
 														- (DEFAULTSIZE / 2), 
 													(this.maxSize.getY() / 2)
 														- (DEFAULTSIZE / 2));
 		Coordinates monster_position = new Coordinates(DEFAULTDELTA, 
 														this.maxSize.getY() - (DEFAULTSIZE + DEFAULTDELTA));
 		int i = 0;
 		// Allocations
 		this.monsters = new Movable[nbMonster];
		this.tanks = new Movable[nbTank];
 
 		// Set-up Tabs
		try
		{
			this.tanks[0] = new Movable(tank_position);
		}
		catch (NegativeCoordinatesException e1)
		{
			System.out.println(e1);
		}
 
 		while(i < nbMonster)
 		{
 			while(i < nbMonster && 
 					monster_position.getX() + (DEFAULTDELTA + DEFAULTSIZE) <= this.maxSize.getX())
 			{
 				try
 				{
 					this.monsters[i] = new Movable(monster_position);
 				}
 				catch (NegativeCoordinatesException e)
 				{
 					System.out.println(e);
 				}
 				monster_position = new Coordinates(monster_position.getX() + (DEFAULTDELTA + DEFAULTSIZE),
 													monster_position.getY());
 				i = i + 1;
 			}				
 			monster_position = new Coordinates(DEFAULTDELTA,
 												monster_position.getY() - (DEFAULTDELTA + DEFAULTSIZE));
 		}
 		
 	}
 	
 	/**
 	 * Main iteration.<br/>
 	 * This function call :
 	 * <ul>
 	 * 		<li>moveTab</li>
 	 * 		<li>testCollision</li>
 	 * </ul>
 	 */
 	// FIXME (SEEN) why returning InterruptedException ?
 	// Eclipse ask me to write it.
 	// FIXME (SEEN) Eclipse suggests you to propagate OR catch it ;-) (to discuss)
 	
 	private void iteration() 
 	{
 		// TODO remove Debug message
 		System.out.println("Begin");
 		//if the player lose, work = false.
 		while(this.work)
 		{
 			// TODO remove Debug message
 			System.out.println("Boucle");
 			
 			try
 			{
 				Thread.sleep(1000); // Clock
 			}
 			catch (InterruptedException e)
 			{
 				System.out.println(e);
 			}
 			
 			// need same resources, so sync?
 			// TODO test collision (thread ? every time check)
 			testCollision();
 			// TODO move enemy (thread ? no-yes, clock mechanism)
 			moveTab(this.monsters);
 		}
 		// TODO remove Debug message
 		System.out.println("Don't work anymore ;)");
 	}
 	
 	/**
 	 * Make move down a table of enemy.<br/>
 	 * The scheme of the descent is :<br/>
 	 * <ul>
 	 * 		<li>Right</li>
 	 * 		<li>Down</li>
 	 * 		<li>Left</li>
 	 * 		<li>Down</li>
 	 * 		<li>Right</li>
 	 * 		<li>Down</li>
 	 * 		<li>Etc...</li>
 	 * </ul>
 	 * 
 	 * @param tab The table of enemy to move down
 	 */
 	private void moveTab(Movable tab[])
 	{
 		// Variable
 		int nb = tab.length;
 		int i = 0;
 		
 		//Down
 		//Left
 		//Down
 		//Right
 		//Down
 		
 		//Right
 		for(i=0; i < nb; i++)
 		{
 			try
 			{
 				tab[i].move(-10, 0);
 			}
 			catch (NegativeCoordinatesException e)
 			{
 				//System.out.println(e);
 			}
 		}
 		//Wait
 		//Down
 		for(i=0; i < nb; i++)
 		{
 			try
 			{
 				tab[i].move(0, -10);
 			}
 			catch (NegativeCoordinatesException e)
 			{
 				System.out.println(e);
 			}
 		}
 		//Wait
 		//Left
 		for(i=0; i < nb; i++)
 		{
 			try
 			{
 				tab[i].move(10, 0);
 			}
 			catch (NegativeCoordinatesException e)
 			{
 				System.out.println(e);
 			}
 		}
 		//Wait
 		//Down
 		for(i=0; i < nb; i++)
 		{
 			try
 			{
 				tab[i].move(10, 0);
 			}
 			catch (NegativeCoordinatesException e)
 			{
 				System.out.println(e);
 			}
 		}
 		//Wait
 	}
 	
 	/**
 	 * This method tests if there is any collisions in all table declared.<br/>
 	 * Collision are tested between each table and not between elements of the same table.<br/>
 	 * If a Tank is touched by an enemy, work is set to false and the game is stopped by the main iteration.
 	 */
 	private void testCollision()
 	{
 		// Test collision of all tables declared
 		// Variable
 		int i=0;
 		// Coordinates for monsters
 		int x1,y1,x2,y2;
 		// Coordinates for tank
 		int tx1,tx2,ty1,ty2;
 		
 		// Initialize tank's coordinates
 		tx1 = this.tanks[0].getArea().getPosition().getX();
 		ty1 = this.tanks[0].getArea().getPosition().getY();
 		tx2 = this.tanks[0].getArea().getSize().getX() + tx1;
 		ty2 = this.tanks[0].getArea().getSize().getY() + ty1;
 		
 		
 		//	Area :
 		//
 		//
 		//	Y
 		//	^
 		//	|
 		//	|
 		//	|
 		//	|	 (x1,y2)__________(x2,y2)
 		//	|		|				 |
 		//	|		|				 |
 		//	|		|				 |
 		//	|		|				 |
 		//	|		|				 |
 		//	|		|				 |
 		//	|		|				 |
 		//	|	 (x1,y1)__________(x2,y1)
 		//	|
 		//	|
 		//	0-------------------------------------------------> X
 		
 		for(i=0; i < this.monsters.length;i++)
 		{
 			// Initialize coordinates
 			x1 = this.monsters[i].getArea().getPosition().getX();
 			y1 = this.monsters[i].getArea().getPosition().getY();
 			x2 = this.monsters[i].getArea().getSize().getX() + x1;
 			y2 = this.monsters[i].getArea().getSize().getY() + y1;
 			
 			// Check if any points of the tank touch enemy
 			if(tx1 > x1 && ty1 > y1 && tx1 < x2 && ty1 < y2)
 			{
 				this.tanks[0].setAlive(false);
 				this.work = false;
 			}
 			else if(tx2 > x1 && ty2 > y1 && tx2 < x2 && ty2 < y2)
 			{
 				this.tanks[0].setAlive(false);
 				this.work = false;
 			}
 			else if(tx1 > x1 && ty2 > y1 && tx1 < x2 && ty2 < y2)
 			{
 				this.tanks[0].setAlive(false);
 				this.work = false;
 			}
 			else if(tx2 > x1 && ty1 > y1 && tx2 < x2 && ty1 < y2)
 			{
 				this.tanks[0].setAlive(false);
 				this.work = false;
 			}				
 		}
 	}
 
 	@Override
 	public String toString()
 	{
 		return "SpaceInvaders [tabMonster=" + Arrays.toString(this.monsters)
 				+ ", tabTank=" + Arrays.toString(this.tanks) + "]";
 	}
 }
