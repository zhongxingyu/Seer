 package hw2_P2;
 
 /**
  * Draws P2 solutions graphically
  * for CS44 W10 hw2 p2
  * @author tim tregubov
  * using some code I wrote for cs5 :-)
  */
 
 import java.applet.Applet;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 import javax.swing.Timer;
 
 public class P2Graphical extends Applet implements ActionListener
 {
 	private static final long serialVersionUID = 4596161053061003374L;
 
 	private int GRID_SIZE; // how many cols/rows we want.
 	private final int SQUARE_SIZE = 30; // how big to make each square
 	private final int HORIZ_OFFSET = 10;
 	private final int VERT_OFFSET = 20;
 	private int WIN_SIZE; // how big to make the
 															// window
 	private MMap maze;
 	private RobotSolver rs;
 	private Timer timer;
 	private ArrayList<LinkedList<Loc>> solutions;
 	private frameManager fm;
 
 	/**
 	 * initialize applet
 	 */
 	public void init()
 	{
		maze = new MMap(MMap.MapSet.MAP2);  //initialize our map of choice
 		rs = new RobotSolver(maze);			//initialize solver
 		solutions = rs.solutions;    //TODO: only for testing
 	    timer = new Timer(500, this);    // lets have a timer for changinging animation frames
 	    timer.start();                    // start up the timer
 	    fm = new frameManager();          // lets manage the animation frames
 	    
 	    GRID_SIZE = maze.gridSize;
 	    WIN_SIZE = SQUARE_SIZE * GRID_SIZE;
 	    
 	    setSize(WIN_SIZE, WIN_SIZE);
 		repaint();
 	}
 
 	/**
 	 * paint the board on every repaint()
 	 */
 	public void paint(Graphics page)
 	{
 		drawCheckBoard(page);
 	}
 
 
 	/**
 	 * Draws the maze
 	 * @param page
 	 */
 	private void drawCheckBoard(Graphics page)
 	{
 		for (int x = 0; x < GRID_SIZE; x++)
 		{ // create each column up till our gridsize
 			for (int y = 0; y < GRID_SIZE; y++)
 			{ // create each row up till our gridsize
 				if (maze.isCollision(x, y))
 				{
 					page.setColor(Color.gray);
 				}
 				else
 				{
 					page.setColor(Color.lightGray);
 				}
 				page.fillRect((x * SQUARE_SIZE), (y * SQUARE_SIZE),
 						SQUARE_SIZE, SQUARE_SIZE); // draw our squares!
 				
 				page.setColor(Color.blue);
 				page.setFont(new Font("Arial", Font.BOLD, 18));
 				
 				for (int i = 0; i < maze.numRobots; i++)
 				{
 					if (fm.isRobot(x, y, i))
 					{
 						page.drawString(Integer.toString(i), ((x * SQUARE_SIZE) + HORIZ_OFFSET), ((y * SQUARE_SIZE) + VERT_OFFSET));
 					}
 				}
 			}
 		}
 	}
 
 	
 	/**
 	 * on timer click move to next frame
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e)
 	{
         fm.next();
         repaint();
 	}
 	
 	
 	/**
 	 * this manages the animation frames we are displaying
 	 */
 	public class frameManager
 	{
 		private int currentFrame;
 		private int numFrames;
 		
 		frameManager()
 		{
 			currentFrame = 0;
 			numFrames = getLongestSize();
 		}
 		
 		
 		/**
 		 * checks if this location and this robot index is a hit
 		 * @param x
 		 * @param y
 		 * @param i
 		 * @return
 		 */
 		public boolean isRobot(int x, int y, int i)
 		{
 			int f = solutions.get(i).size() - 1;
 			boolean b = false;
 			
 			if (f < currentFrame) //make sure to display the stopped bots for the whole anim
 			{
 				b = x==solutions.get(i).get(f).x && y==solutions.get(i).get(f).y;
 			}
 			else
 			{
 				b = x==solutions.get(i).get(currentFrame).x && y==solutions.get(i).get(currentFrame).y;
 			}
 			return b;
 		}
 		
 		
 		/**
 		 * go to the next frame
 		 */
 		public void next()
 		{
 			currentFrame = (currentFrame + 1) % numFrames;
 		}
 		
 		
 		/**
 		 * find the longest solution in terms of steps
 		 * @return the size 
 		 */
 		private int getLongestSize()
 		{
 			int max = 0;
 			
 			for (int i = 0; i < maze.numRobots; i++)
 			{
 				max = Math.max(max, solutions.get(i).size());
 			}
 			return max;
 		}
 	}
 
 }
