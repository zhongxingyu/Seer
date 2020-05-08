 /**
  * 
  */
 package fr.iutvalence.java.projet.spaceinvaders;
 
 import java.util.Arrays;
 
 import fr.iutvalence.java.projet.unused.Tank;
 
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
 	// FIXME respect coding conventions
 	private static final int nbMonsterDefault = 20;
 
 	/**
 	 * It defines the number of tank you have in tabTank by default,
 	 * if it's not set in constructor.
 	 */
 	// FIXME respect coding conventions
 	private static final int nbTankDefault = 1;
 	
 	/**
 	 * It defines the maximum (default) of X axis,
 	 * if it's not set in constructor.
 	 */
 	// FIXME respect coding conventions
 	private static final int XGrid = 300;
 	
 	/**
 	 * It defines the maximum (default) of Y axis,
 	 * if it's not set in constructor.
 	 */
 	// FIXME respect coding conventions
 	private static final int YGrid = 300;
 	
 	/**
 	 * Default delta between 2 monsters
 	 */
 	// FIXME respect coding conventions
 	private static final int defaultDelta = 2;
 	
 	/**
 	 * Default size of element (e.g. Doc Movable)
 	 */
 	// FIXME respect coding conventions
 	private static final int defaultSize = 10;
 	
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
 	// FIXME why not rename it just "monsters" ?
 	private Movable[] monster;
 
 	/**
 	 * Array containing all tanks.
 	 */
 	// FIXME why not rename it just "tanks" ?
 	private Movable[] tank;
 
 	
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
 		this.maxSize = new Coordinates(XGrid, YGrid);
 		initTab(nbMonsterDefault, nbTankDefault);
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
 		this.maxSize = new Coordinates(XGrid, YGrid);
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
 	public void start()
 	{
 		try
 		{
 			iteration();
 		}
 		catch (InterruptedException e)
 		{
 			e.printStackTrace();
 		}
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
 	// FIXME rename parameters
 	private void initTab(int nbMonster, int nbTank)
 	{
 		// local variable
 		Coordinates tank_position = new Coordinates((this.maxSize.getX() / 2) 
 														- (defaultSize / 2), 
 													(this.maxSize.getY() / 2)
 														- (defaultSize / 2));
 		Coordinates monster_position = new Coordinates(defaultDelta, 
 														this.maxSize.getY() - (defaultSize + defaultDelta));
 		int i = 0;
 		// Allocations
 		this.monster = new Movable[nbMonster];
 		this.tank = new Tank[nbTank];
 
 		// Set-up Tabs
 		this.tank[0] = new Tank(tank_position);
 
 		while(i < nbMonster)
 		{
 			while(i < nbMonster && 
 					monster_position.getX() + (defaultDelta + defaultSize) <= this.maxSize.getX())
 			{
 				this.monster[i] = new Movable(monster_position);
 				monster_position = new Coordinates(monster_position.getX() + (defaultDelta + defaultSize),
 													monster_position.getY());
 				i = i + 1;
 			}				
 			monster_position = new Coordinates(defaultDelta,
 												monster_position.getY() - (defaultDelta + defaultSize));
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
	// FIXME Eclipse suggests you to propagate OR catch it ;-) (to discuss)
 	
 	private void iteration() throws InterruptedException 
 	{
 		// TODO remove Debug message
 		System.out.println("Begin");
 		//if the player lose, work = false.
 		while(this.work)
 		{
 			// TODO remove Debug message
 			System.out.println("Boucle");
 			
 			Thread.sleep(1000); // Clock
 			
 			// need same resources, so sync?
 			// TODO test collision (thread ? every time check)
 			testCollision();
 			// TODO move enemy (thread ? no-yes, clock mechanism)
 			moveTab(this.monster);
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
 			tab[i].move(-10, 0);
 		}
 		//Wait
 		//Down
 		for(i=0; i < nb; i++)
 		{
 			tab[i].move(0, -10);
 		}
 		//Wait
 		//Left
 		for(i=0; i < nb; i++)
 		{
 			tab[i].move(10, 0);
 		}
 		//Wait
 		//Down
 		for(i=0; i < nb; i++)
 		{
 			tab[i].move(10, 0);
 		}
 		//Wait
 	}
 	
 	/**
 	 * This methode tests if there is any collisions in all table declared.<br/>
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
 		tx1 = this.tank[0].getArea().getPosition().getX();
 		ty1 = this.tank[0].getArea().getPosition().getY();
 		tx2 = this.tank[0].getArea().getSize().getX() + tx1;
 		ty2 = this.tank[0].getArea().getSize().getY() + ty1;
 		
 		
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
 		
 		for(i=0; i < this.monster.length;i++)
 		{
 			// Initialize coordinates
 			x1 = this.monster[i].getArea().getPosition().getX();
 			y1 = this.monster[i].getArea().getPosition().getY();
 			x2 = this.monster[i].getArea().getSize().getX() + x1;
 			y2 = this.monster[i].getArea().getSize().getY() + y1;
 			
 			// Check if any points of the tank touch enemy
 			if(tx1 > x1 && ty1 > y1 && tx1 < x2 && ty1 < y2)
 			{
 				this.tank[0].setAlive(false);
 				this.work = false;
 			}
 			else if(tx2 > x1 && ty2 > y1 && tx2 < x2 && ty2 < y2)
 			{
 				this.tank[0].setAlive(false);
 				this.work = false;
 			}
 			else if(tx1 > x1 && ty2 > y1 && tx1 < x2 && ty2 < y2)
 			{
 				this.tank[0].setAlive(false);
 				this.work = false;
 			}
 			else if(tx2 > x1 && ty1 > y1 && tx2 < x2 && ty1 < y2)
 			{
 				this.tank[0].setAlive(false);
 				this.work = false;
 			}				
 		}
 	}
 
 	@Override
 	public String toString()
 	{
 		return "SpaceInvaders [tabMonster=" + Arrays.toString(this.monster)
 				+ ", tabTank=" + Arrays.toString(this.tank) + "]";
 	}
 }
