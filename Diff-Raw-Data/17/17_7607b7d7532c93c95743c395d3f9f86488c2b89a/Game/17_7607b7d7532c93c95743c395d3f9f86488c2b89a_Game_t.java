 /**
  * Main game
  * 
  * @author	James Schwinabart
  */
 
 import java.awt.*;
 import java.awt.image.BufferStrategy;
 import java.util.ArrayList;
 
 public class Game
 {
 	public static final int SPEED_THROTTLE		= 10;
 	
 	// number of pixels to move by
 	public static final int MOVE_SPEED			= 1;
 	
 	// delay (in number of frames) before another attack can be used
 	public static final int ATTACK_DELAY		= 100;
 	private int attackDelay						= 0;
 	
 	public static final int MIN_STUDENTS		= 20;
 	public static final int MAX_STUDENTS		= 30;
 	
 	public static final int MAX_STUDENT_SPEED	= 1;
 	public static final double GENDER_CHANCE	= 0.5;
 	
 	public static final double INFECT_CHANCE	= 0.4;
 	public static final double CUPPLE_CHANCE	= 0.6;
 	
 	public static final int MIN_NUM_MOVES		= 10;
 	public static final int MAX_NUM_MOVES		= 400;
 	
 	public static final int STATS_BAR_HEIGHT	= 20;
 	public static final int STAT_PAD_BOTTOM		= 6;
 	
 	// graphics
 	private Graphics g;
 	private BufferStrategy strategy;
 	
 	// objects on the screen
 	private Staff player;
 	private ArrayList<Student> students			= new ArrayList<Student>();
 	private ArrayList<Cupple> couples			= new ArrayList<Cupple>();
 	private ArrayList<Attack> attacks			= new ArrayList<Attack>();
 
 	/**
 	 * Constructor
 	 * 
 	 * @param w Width of the game canvas
 	 * @param h Height of the game canvas
 	 * @param g Graphics canvas
 	 * @param strategy Buffer strategy
 	 */
 	public Game(ParkViewProtector p, Graphics g, BufferStrategy strategy)
 	{
 		this.g								= g;
 		this.strategy						= strategy;
 		
 		// initialize everything
 		initPlayer();
 		initStudents();
 	}
 	
 	/**
 	 * Create and initialize player (the staff member we're playing as)
 	 */
 	public void initPlayer()
 	{
 		// FIXME: magic numbers are bad
 		player						= new Stark(0, STATS_BAR_HEIGHT, 10, 10, 10, 10, 10);
 	}
 	
 	/**
 	 * Create and initialize students
 	 */
 	public void initStudents()
 	{
 		int numStudents				= (int) (Math.random() * (MAX_STUDENTS - MIN_STUDENTS + 1)) + MIN_STUDENTS;
 		Student student				= null;
 		
 		int x, y;
 		double speed;
 		char gender;
 		
 		for(int i = 0; i < numStudents; i++)
 		{
 			x						= (int) (Math.random() * ParkViewProtector.WIDTH) + 1;
 			y						= (int) (Math.random() * ParkViewProtector.HEIGHT) + 1;
 			speed					= Math.random() * MAX_STUDENT_SPEED + 1;
 			gender					= (Math.random() <= GENDER_CHANCE) ? 'm' : 'f';
 			
 			student					= new Student(x, y, 5, 5, speed, 0, gender);
 			
 			students.add(student);
 			
 			// FIXME: remove soon, just for testing
 			if(Math.random() > 0.5)
 			{
 				students.get(i).infect();
 			}
 		}
 	}
 	
 	public void show()
 	{
 		Student currStudent;
 		Cupple currCouple;
 		Attack currAttack;
 		
 		int student1, student2;
 		
 		g						= (Graphics) strategy.getDrawGraphics();
 		
 		// draw the background
 		g.setColor(Color.white);
 		g.fillRect(0, 0, ParkViewProtector.WIDTH, ParkViewProtector.HEIGHT);
 		
 		////////////////////////////////////////////////////////////////////////////////////
 		// Draw students
 		////////////////////////////////////////////////////////////////////////////////////
 		
 		for(int i = 0; i < students.size(); i++)
 		{
 			currStudent			= students.get(i);
 			currStudent.draw(g);
 			
 			// random movement
 			moveRandom(currStudent, MOVE_SPEED,
 					(int) (Math.random() * (MAX_NUM_MOVES - MIN_NUM_MOVES) +
 							MIN_NUM_MOVES + 1));
 
 			// collision detection! :D
 			/*if(player.getBounds().intersects(currStudent.getBounds()))
 			{
 				player.adjustHp(1);
 			}*/
 			
 			if(currStudent.isInfected())
 			{
 				for(int j = 0; j < students.size(); j++)
 				{
 					// don't do anything if it's us
 					if(i == j) continue;
 
 					// if we hit another infected student
 					// TODO: decide on and add realistic chances
 					if(currStudent.getBounds().intersects(students.get(j).getBounds())
 							&& students.get(j).isInfected()
 							&& Math.random() <= CUPPLE_CHANCE)
 					{
 						couples.add(new Cupple(currStudent, students.get(j)));
 						
 						
 						student1			= i;
 						student2			= j;
 						
 						if(student2 > student1)
 						{
 							student2--;
 						}
 						
 						try
 						{
 							students.remove(student1);
 							students.remove(student2);
 						}
 						catch(Exception e)
 						{
 							System.out.println("Something went wrong when deleting someone :O");
 						}
 						break;
 					}
 				}
 			}
 		}
 		
 		////////////////////////////////////////////////////////////////////////////////////
 		// Draw couples
 		////////////////////////////////////////////////////////////////////////////////////
 		
 		for(int i = 0; i < couples.size(); i++)
 		{
 			currCouple			= couples.get(i);
 			currCouple.draw(g);
 			
 			// random movement
 			moveRandom(currCouple, MOVE_SPEED,
 					(int) (Math.random() * (MAX_NUM_MOVES - MIN_NUM_MOVES) +
 							MIN_NUM_MOVES + 1));
 			
 			// update students
 			for(int j = 0; j < students.size(); j++)
 			{
 				// if we hit a student that isn't infected
 				if(currCouple.getBounds().intersects(students.get(j).getBounds())
 						&& !students.get(j).isInfected()
 						&& Math.random() <= INFECT_CHANCE)
 				{
 					students.get(j).infect();
 					System.out.println("student #" + j + " infected by couple #" + i);
 					break;
 				}
 			}
 			
 			// hit by an attack?
 			for(int j = 0; j < attacks.size(); j++)
 			{
 				currAttack		= attacks.get(j);
 				
 				if(currAttack.getBounds().intersects(currCouple.getBounds()))
 				{
 					attacks.remove(j);
 					
 					// create students before removing couple
 					//students.add(currCouple.getMale());
 					//students.add(currCouple.getFemale());
 					
 					couples.remove(i);
 					break;
 				}
 			}
 		}
 		
 		////////////////////////////////////////////////////////////////////////////////////
 		// Draw attacks
 		////////////////////////////////////////////////////////////////////////////////////
 		
 		for(int i = 0; i < attacks.size(); i++)
 		{
 			currAttack			= attacks.get(i);
 			currAttack.draw(g);
 			
 			currAttack.move(MOVE_SPEED);
 			
 			// is the attack off the screen?
 			if(currAttack.getBounds().x < -currAttack.getBounds().width ||
 					currAttack.getBounds().x > ParkViewProtector.WIDTH ||
 					currAttack.getBounds().y < -currAttack.getBounds().height ||
 					currAttack.getBounds().y > ParkViewProtector.HEIGHT)
 			{
 				System.out.println("Attack #" + i +" went off screen, removing");
 				
 				attacks.remove(i);
 				i--;
 			}
 		}
 		
 		////////////////////////////////////////////////////////////////////////////////////
 		// Draw player
 		////////////////////////////////////////////////////////////////////////////////////
 		
 		player.draw(g);
 		
 		////////////////////////////////////////////////////////////////////////////////////
 		// Draw statistics
 		////////////////////////////////////////////////////////////////////////////////////
 		// these are painted last to ensure that they are always on top
 
 		// background rectangle
 		g.setColor(new Color(255, 0, 255));
 		g.fillRect(0, 0, ParkViewProtector.WIDTH, STATS_BAR_HEIGHT);
 		
 		// draw HP
 		g.setColor(Color.white);
 		g.setFont(new Font("Courier New", Font.PLAIN, 12));
 		g.drawString("HP:    / " + player.getMaxHp(), 5,
 				STATS_BAR_HEIGHT - STAT_PAD_BOTTOM);
 		
 		g.drawString("" + player.getHp(), 33, STATS_BAR_HEIGHT - STAT_PAD_BOTTOM);
 		
 		g.drawString("Speed: " + player.getSpeed(), 200, STATS_BAR_HEIGHT - STAT_PAD_BOTTOM);
 		
 		// finish drawing
 		g.dispose();
 		strategy.show();
 		
 		////////////////////////////////////////////////////////////////////////////////////
 		// Move the player
 		////////////////////////////////////////////////////////////////////////////////////
 		// TODO: use physics for diagonal movement? (sqrt 2 * MOVE_SPEED^2)
 		
 		if(ParkViewProtector.upPressed && !ParkViewProtector.downPressed
 				&& player.getBounds().y > STATS_BAR_HEIGHT)
 		{
 			player.move(0, -MOVE_SPEED);
 		}
 		
 		if(ParkViewProtector.downPressed && !ParkViewProtector.upPressed
 				&& player.getBounds().y < ParkViewProtector.HEIGHT - player.getBounds().height)
 		{
 			player.move(0, MOVE_SPEED);
 			//downPressed				= false;
 		}
 		
 		if(ParkViewProtector.leftPressed && !ParkViewProtector.rightPressed
 				&& player.getBounds().x > 0)
 		{
 			player.move(-MOVE_SPEED, 0);
 			//leftPressed				= false;
 		}
 		
 		if(ParkViewProtector.rightPressed && !ParkViewProtector.leftPressed
 				&& player.getBounds().x < ParkViewProtector.WIDTH - player.getBounds().width)
 		{
 			player.move(MOVE_SPEED, 0);
 			//rightPressed			= false;
 		}
 		
 		if(ParkViewProtector.attackPressed && attackDelay == 0)
 		{
 			Attack testAttack;
 			testAttack			= new Attack(player.x + player.getBounds().width / 2,
												player.y + player.getBounds().height / 2,
												0, "attack", player.getDirection(), 3, 0, 
												true, 0);
 			testAttack.switchXY();
 			attacks.add(testAttack);
 			
 			// set delay
 			attackDelay			= ATTACK_DELAY;
 		}
 		
 		// decrease delay if there is one
 		if(attackDelay > 0)
 			attackDelay--;
 		
 		// keep the game from running too fast
 		try
 		{
 			Thread.sleep(SPEED_THROTTLE);
 		}
 		catch(Exception e) {}
 	}
 	
 	/**
 	 * Random movement
 	 * 
 	 * @param Movable Object to move
 	 * @param speed Speed to move at
 	 * @param changeMoves Number of moves to change the direction after
 	 */
 	public void moveRandom(Movable obj, int speed, int changeMoves)
 	{
 		// change direction if the move count exceeds the number of moves to change after
 		if(obj.getMoveCount() <= 0 || obj.getMoveCount() > changeMoves)
 		{
 			// choose a new direction
 			obj.setDirection((int) (Math.random() * 4));
 			obj.resetMoveCount();
 		}
 		
 		// change direction if we hit the top or bottom
 		if(obj.getBounds().y <= STATS_BAR_HEIGHT && obj.getDirection() == Direction.NORTH)
 		{
 			obj.setDirection(Direction.SOUTH);
 			obj.resetMoveCount();
 		}
 		else if(obj.getBounds().y >= ParkViewProtector.HEIGHT - obj.getBounds().height  &&
 				obj.getDirection() == Direction.SOUTH)
 		{
 			obj.setDirection(Direction.NORTH);
 			obj.resetMoveCount();
 		}
 		else if(obj.getBounds().x <= 0 && obj.getDirection() == Direction.WEST)
 		{
 			obj.setDirection(Direction.EAST);
 			obj.resetMoveCount();
 		}
 		else if(obj.getBounds().x >= ParkViewProtector.WIDTH - obj.getBounds().width &&
 				obj.getDirection() == Direction.EAST)
 		{
 			obj.setDirection(Direction.WEST);
 			obj.resetMoveCount();
 		}
 		
 		obj.move(speed);
 	}
 }
