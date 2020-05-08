 /*
  * This version will attempt to take the screenshot after every move.
  * Possible future improvement would be to make multiple moves for each ss or to add threading.
  *
  * This was written in a day or two over Christmas break.  It could be vastly improved, but it
  * gets the job done.
  *
  * Author: Michael Patterson
  * Date: December 2011
  */
 
 // See: https://code.google.com/p/jnativehook/
 // Look into intercepting abort key combo
 
 import java.awt.Robot;
 import java.awt.AWTException;
 import java.awt.event.InputEvent;
 import java.awt.image.BufferedImage;
 import java.awt.Color;
 import java.awt.Point;
 import java.lang.Math;
 
 import java.io.File;
 import java.awt.Rectangle;
 import javax.imageio.ImageIO;
 
 public class BejeweledBot
 {
 	//hardcode some constants (use windows key + left-arrow) 
 	//If position still isn't right move window to center and then use win+left again.
 	private static final int TOP_LEFT_X = 190; //187;	//was 184
 	private static final int TOP_LEFT_Y = 460; //404;	//was 408
 	private static final int CELL_WIDTH = 40;
 	private static final int CELL_HEIGHT = 40;
 	private static final int NUM_ROWS = 8;
 	private static final int NUM_COLUMNS = 8;
 	
 	/* Old Way
 	private static final Point TIME_UP = new Point(257, 554);
 	private static final Color TIME_UP_COLOR = new Color(250, 208, 63);
 	private static final Point TIME_UP2 = new Point(300, 574);
 	private static final Color TIME_UP2_COLOR = new Color(254, 236, 146);
 	private static final Point TIME_UP3 = new Point(341, 556);
 	private static final Color TIME_UP3_COLOR = new Color(250, 205, 61);
 	*/
 
	private static final Point TIME_UP = new Point(227, 545);
 	private static final Color TIME_UP_COLOR = new Color(121, 193, 170);
	private static final Point TIME_UP2 = new Point(304, 561);
	private static final Color TIME_UP2_COLOR = new Color(103, 156, 150);
	private static final Point TIME_UP3 = new Point(347, 544);
 	private static final Color TIME_UP3_COLOR = new Color(138, 255, 200);
 	
 	//TODO:Green, Orange
 	//490, 560 
 
 	private static final Color YELLOW = new Color(254, 245, 35);
 	private static final Color PURPLE = new Color(236, 14, 236);
 	private static final Color GREEN = new Color(5, 138, 18);	//need to be exact (on center band) (OLD WAY)
 	//private static final Color GREEN = new Color(16, 164, 33);
 	private static final Color BLUE = new Color(15, 138, 254);
 	private static final Color ORANGE = new Color(214, 74, 19);	//need to be exact (on center band) (OLD WAY)
 	//private static final Color ORANGE = new Color(230, 101, 33);
 	private static final Color WHITE = new Color(224, 224, 224);	//3 values ALWAYS identical
 	private static final Color RED = new Color(249, 26, 54);
 	
 	private static final Color YELLOW_MUL = new Color(243, 239, 0);
 	private static final Color PURPLE_MUL = new Color(204, 17, 203);
 	private static final Color GREEN_MUL = new Color(0, 193, 12);
 	private static final Color BLUE_MUL = new Color(26, 95, 155);
 	private static final Color ORANGE_MUL = new Color(242, 120, 41);
 	private static final Color WHITE_MUL = new Color(198, 198, 198);
 	private static final Color RED_MUL = new Color(183, 12, 24);
 	
 	
 	private static Robot r;
 	private static BufferedImage image;
 	private static char[][] state;
 	
 	public static void main(String[] args) throws java.awt.AWTException
 	{
 		initialize();
 		focusWindow();
 		
 		
 		//testMouseMovement();
 		int step = 0;
		while(step < 1200)
 		{
 			updateState();
 			makeMove();
 			step++;
 			System.out.println(step);
 			if(isOver())
 			{
 				System.out.println("Game Over");
 				break;
 			}
 		}
 		//printBoardState();
 	}
 	
 	private static void initialize() throws java.awt.AWTException
 	{
 		if(r == null)
 		{
 			r = new Robot();
 		}
 		if(state == null)
 		{
 			state = new char[NUM_ROWS][NUM_COLUMNS];
 		}
 	}
 	private static void focusWindow()
 	{
 		moveMouse(TOP_LEFT_X, TOP_LEFT_Y);
 		
 		//first click to focus on the bejeweled window
 		clickMouseLeft();
 	}
 	private static void moveMouse(int newX, int newY)
 	{
 		r.mouseMove(newX, newY);
 		delay(10);
 	}
 	private static void clickMouseLeft()
 	{
 		delay(10);
 		r.mousePress(InputEvent.BUTTON1_MASK);
 		delay(10);
 		r.mouseRelease(InputEvent.BUTTON1_MASK);
 		delay(10);
 	}
 	private static void delay(int ms)
 	{
 		r.delay(ms);
 	}
 	private static void moveMouseToCell(int row, int col)
 	{
 		if(row >= NUM_ROWS || col >= NUM_COLUMNS)
 		{
 			throw new IllegalArgumentException("Value out of range.");
 		}
 		Point newCoord = getCoordinatesOfCell(row, col);
 		moveMouse(newCoord.x, newCoord.y);
 	}
 	private static Point getCoordinatesOfCell(int row, int col)
 	{
 		Point coords = new Point();
 		coords.x = TOP_LEFT_X + col*CELL_WIDTH + CELL_WIDTH/2;
 		coords.y = TOP_LEFT_Y + row*CELL_HEIGHT + CELL_HEIGHT/2;
 		return coords;
 	}
 	private static void updateState()
 	{
 		image = r.createScreenCapture(new Rectangle(0, 0, 
 									TOP_LEFT_X + NUM_ROWS*CELL_HEIGHT, 
 									TOP_LEFT_Y + NUM_COLUMNS*CELL_WIDTH));
 		
 		//loop through each square on the board
 		for(int col=0; col<NUM_COLUMNS; col++)
 		{
 			for(int row=0; row<NUM_ROWS; row++)
 			{
 				state[row][col] = calcCellChar(row, col);
 			}
 		}
 		
 		//for testing
 		/*
 		try
 		{
 			File file = new File("screenshot.jpg");
 			ImageIO.write(image, "jpg", file);
 		}
 		catch(Exception e)
 		{
 			System.out.println(e.getMessage());
 			System.exit(1);
 		}
 		*/
 	}
 	
 	private static char calcCellChar(int row, int col)
 	{
 		Point point = getCoordinatesOfCell(row, col);
 		int pixel = image.getRGB(point.x, point.y);
 		int red = (pixel>>16) & 0xff;
 		int green = (pixel>>8) & 0xff;
 		int blue = pixel & 0xff;
 		
 		if(isYellow(red, green, blue))
 		{
 			return 'Y';
 		}
 		else if(isPurple(red, green, blue))
 		{
 			return 'P';
 		}
 		else if(isGreen(red, green, blue))
 		{
 			return 'G';
 		}
 		else if(isBlue(red, green, blue))
 		{
 			return 'B';
 		}
 		else if(isOrange(red, green, blue))
 		{
 			return 'O';
 		}
 		else if(isRed(red, green, blue))
 		{
 			return 'R';
 		}
 		else if(isWhite(red, green, blue))
 		{
 			return 'W';
 		}
 		else
 		{
 			return '?';
 		}
 	}
 	private static boolean isYellow(int red, int green, int blue)
 	{
 		if((Math.abs(red-YELLOW.getRed()) < 10) && 
 				(Math.abs(green-YELLOW.getGreen()) < 10) &&
 				(Math.abs(blue-YELLOW.getBlue()) < 10))
 		{
 			return true;
 		}
 		else if((Math.abs(red-YELLOW_MUL.getRed()) < 10) && 
 				(Math.abs(green-YELLOW_MUL.getGreen()) < 10) &&
 				(Math.abs(blue-YELLOW_MUL.getBlue()) < 10))
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	private static boolean isPurple(int red, int green, int blue)
 	{
 		if((Math.abs(red-PURPLE.getRed()) < 10) && 
 				(Math.abs(green-PURPLE.getGreen()) < 10) &&
 				(Math.abs(blue-PURPLE.getBlue()) < 10))
 		{
 			return true;
 		}
 		else if((Math.abs(red-PURPLE_MUL.getRed()) < 10) && 
 				(Math.abs(green-PURPLE_MUL.getGreen()) < 10) &&
 				(Math.abs(blue-PURPLE_MUL.getBlue()) < 10))
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	private static boolean isGreen(int red, int green, int blue)
 	{
 		if((Math.abs(red-GREEN.getRed()) < 10) && 
 				(Math.abs(green-GREEN.getGreen()) < 10) &&
 				(Math.abs(blue-GREEN.getBlue()) < 10))
 		{
 			return true;
 		}
 		else if((Math.abs(red-GREEN_MUL.getRed()) < 10) && 
 				(Math.abs(green-GREEN_MUL.getGreen()) < 10) &&
 				(Math.abs(blue-GREEN_MUL.getBlue()) < 10))
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	private static boolean isBlue(int red, int green, int blue)
 	{
 		if((Math.abs(red-BLUE.getRed()) < 10) && 
 				(Math.abs(green-BLUE.getGreen()) < 10) &&
 				(Math.abs(blue-BLUE.getBlue()) < 10))
 		{
 			return true;
 		}
 		else if((Math.abs(red-BLUE_MUL.getRed()) < 10) && 
 				(Math.abs(green-BLUE_MUL.getGreen()) < 10) &&
 				(Math.abs(blue-BLUE_MUL.getBlue()) < 10))
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	private static boolean isOrange(int red, int green, int blue)
 	{
 		if((Math.abs(red-ORANGE.getRed()) < 10) && 
 				(Math.abs(green-ORANGE.getGreen()) < 10) &&
 				(Math.abs(blue-ORANGE.getBlue()) < 10))
 		{
 			return true;
 		}
 		else if((Math.abs(red-ORANGE_MUL.getRed()) < 10) && 
 				(Math.abs(green-ORANGE_MUL.getGreen()) < 10) &&
 				(Math.abs(blue-ORANGE_MUL.getBlue()) < 10))
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	private static boolean isRed(int red, int green, int blue)
 	{
 		if((Math.abs(red-RED.getRed()) < 10) && 
 				(Math.abs(green-RED.getGreen()) < 10) &&
 				(Math.abs(blue-RED.getBlue()) < 10))
 		{
 			return true;
 		}
 		else if((Math.abs(red-RED_MUL.getRed()) < 10) && 
 				(Math.abs(green-RED_MUL.getGreen()) < 10) &&
 				(Math.abs(blue-RED_MUL.getBlue()) < 10))
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	private static boolean isWhite(int red, int green, int blue)
 	{
 		//check that all three values are the same (white)
 		if(red == green && green == blue)
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	
 	private static void makeMove()
 	{
 		/*
 		 * moveGem(row, col, direction)
 		 * Direction: 1=left, 2=up, 3=right, 4=down
 		 */
 		//TODO First try and get multipliers every time
 		//Then try and get 5 in a row
 		//Then try for 4 in a row
 		for(int row = NUM_ROWS-1; row >= 0; row--)
 		{
 			for(int col = 0; col < NUM_COLUMNS; col++)
 			{
 				int canScore = canMoveToScore(row, col);
 				if(canScore != 0)
 				{
 					moveGem(row, col, canScore);
 					return;
 				}
 			}
 		}
 	}
 	
 	/*
 	 * Returns the "direction" to move the gem to score.
 	 * Direction: 1=left, 2=up, 3=right, 4=down
 	 */
 	private static int canMoveToScore(int row, int col)
 	{
 		char color = state[row][col];
 		if(color == '?')
 		{
 			return 0;
 		}
 		//check moving left (1)
 		if(col != 0)
 		{
 			if(row > 1)
 			{
 				//current gem will be bottom of the three
 				if(state[row-1][col-1] == color && state[row-2][col-1] == color)
 				{
 					return 1;
 				}
 			}
 			if(row > 0 && row < NUM_ROWS-1)
 			{
 				//current gem will be middle of the three
 				if(state[row-1][col-1] == color && state[row+1][col-1] == color)
 				{
 					return 1;
 				}
 			}
 			if(row < NUM_ROWS-2)
 			{
 				//current gem will be top of the three
 				if(state[row+1][col-1] == color && state[row+2][col-1] == color)
 				{
 					return 1;
 				}
 			}
 			if(col > 2)
 			{
 				//current gem will be the right of the three
 				if(state[row][col-3] == color && state[row][col-2] == color)
 				{
 					return 1;
 				}
 			}
 		}
 		
 		//check moving up (2)
 		if(row != 0)
 		{
 			if(col > 1)
 			{
 				//current gem will be farthest right of the three
 				if(state[row-1][col-2] == color && state[row-1][col-1] == color)
 				{
 					return 2;
 				}
 			}
 			if(col > 0 && col < NUM_COLUMNS-1)
 			{
 				//current gem will be middle of the three
 				if(state[row-1][col-1] == color && state[row-1][col+1] == color)
 				{
 					return 2;
 				}
 			}
 			if(col < NUM_COLUMNS-2)
 			{
 				//current gem will be farthest left of the three
 				if(state[row-1][col+1] == color && state[row-1][col+2] == color)
 				{
 					return 2;
 				}
 			}
 			if(row > 2)
 			{
 				//current gem will be the bottom of the three
 				if(state[row-2][col] == color && state[row-3][col] == color)
 				{
 					return 2;
 				}
 			}
 		}
 		
 		//check moving right (3)
 		if(col != NUM_COLUMNS-1)
 		{
 			if(row > 1)
 			{
 				//current gem will be bottom of the three
 				if(state[row-1][col+1] == color && state[row-2][col+1] == color)
 				{
 					return 3;
 				}
 			}
 			if(row > 0 && row < NUM_ROWS-1)
 			{
 				//current gem will be middle of the three
 				if(state[row-1][col+1] == color && state[row+1][col+1] == color)
 				{
 					return 3;
 				}
 			}
 			if(row < NUM_ROWS-2)
 			{
 				//current gem will be top of the three
 				if(state[row+1][col+1] == color && state[row+2][col+1] == color)
 				{
 					return 3;
 				}
 			}
 			if(col < NUM_COLUMNS-3)
 			{
 				//current gem will be the left of the three
 				if(state[row][col+2] == color && state[row][col+3] == color)
 				{
 					return 3;
 				}
 			}
 		}
 		
 		//check moving down (4)
 		if(row != NUM_ROWS-1)
 		{
 			if(col > 1)
 			{
 				//current gem will be farthest right of the three
 				if(state[row+1][col-2] == color && state[row+1][col-1] == color)
 				{
 					return 4;
 				}
 			}
 			if(col > 0 && col < NUM_COLUMNS-1)
 			{
 				//current gem will be middle of the three
 				if(state[row+1][col-1] == color && state[row+1][col+1] == color)
 				{
 					return 4;
 				}
 			}
 			if(col < NUM_COLUMNS-2)
 			{
 				//current gem will be farthest left of the three
 				if(state[row+1][col+1] == color && state[row+1][col+2] == color)
 				{
 					return 4;
 				}
 			}
 			if(row < NUM_ROWS-3)
 			{
 				//current gem will be the top of the three
 				if(state[row+2][col] == color && state[row+3][col] == color)
 				{
 					return 4;
 				}
 			}
 		}
 		
 		return 0;	//no scoring moves for this gem
 	}
 	
 	/*
 	 * Direction: 1=left, 2=up, 3=right, 4=down
 	 */
 	private static void moveGem(int row, int col, int direction)
 	{
 		//Testing to see if movements are working (or if i need more delays)
 		//System.out.println("Move! Row: " + row + " Col: " + col + " Direction: " + direction);
 		//check that it's a legal move
 		if((direction == 1 && col == 0) || (direction == 2 && row == 0) 
 			|| (direction == 3 && col == NUM_COLUMNS-1) || (direction == 4 && row == NUM_ROWS-1)
 			|| direction < 1 || direction > 4)
 		{
 			System.out.println("Illegal move!");
 			System.out.println("Row: " + row + " Col: " + col + " Direction: " + direction);
 			return;
 		}
 		else
 		{
 			moveMouseToCell(row, col);
 			delay(10);
 			r.mousePress(InputEvent.BUTTON1_MASK);
 			delay(10);
 			
 			if(direction == 1)
 			{
 				moveMouseToCell(row, col-1);
 			}
 			else if(direction == 2)
 			{
 				moveMouseToCell(row-1, col);
 			}
 			else if(direction == 3)
 			{
 				moveMouseToCell(row, col+1);
 			}
 			else
 			{
 				moveMouseToCell(row+1, col);
 			}
 			
 			delay(10);
 			r.mouseRelease(InputEvent.BUTTON1_MASK);
 			delay(10);
 		}
 	}
 	
 	private static boolean isOver()
 	{
 		//check first pixel
 		int pixel = image.getRGB(TIME_UP.x, TIME_UP.y);
 		int red = (pixel>>16) & 0xff;
 		int green = (pixel>>8) & 0xff;
 		int blue = pixel & 0xff;
 		
 		if((Math.abs(red-TIME_UP_COLOR.getRed()) < 10) && 
 				(Math.abs(green-TIME_UP_COLOR.getGreen()) < 10) &&
 				(Math.abs(blue-TIME_UP_COLOR.getBlue()) < 10))
 		{
 			//do nothing
 		}
 		else
 		{
 			return false;
 		}
		System.out.println("Here");
 		//check second pixel
 		pixel = image.getRGB(TIME_UP2.x, TIME_UP2.y);
 		red = (pixel>>16) & 0xff;
 		green = (pixel>>8) & 0xff;
 		blue = pixel & 0xff;
 		
 		if((Math.abs(red-TIME_UP2_COLOR.getRed()) < 10) && 
 				(Math.abs(green-TIME_UP2_COLOR.getGreen()) < 10) &&
 				(Math.abs(blue-TIME_UP2_COLOR.getBlue()) < 10))
 		{
 			//do nothing
 		}
 		else
 		{
 			return false;
 		}
 		
		System.out.println("Here");
 		//check third pixel
 		pixel = image.getRGB(TIME_UP3.x, TIME_UP3.y);
 		red = (pixel>>16) & 0xff;
 		green = (pixel>>8) & 0xff;
 		blue = pixel & 0xff;
 		
 		if((Math.abs(red-TIME_UP3_COLOR.getRed()) < 10) && 
 				(Math.abs(green-TIME_UP3_COLOR.getGreen()) < 10) &&
 				(Math.abs(blue-TIME_UP3_COLOR.getBlue()) < 10))
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	
 	/****************************Test Methods*****************************/
 	
 	private static void testMouseMovement()
 	{
 		//loop through each square on the board
 		for(int col=0; col<NUM_COLUMNS; col++)
 		{
 			for(int row=0; row<NUM_ROWS; row++)
 			{
 				moveMouseToCell(row, col);
 				delay(20);
 			}
 		}
 	}
 	
 	private static void printBoardState()
 	{
 		for(int row=0; row<NUM_ROWS; row++)
 		{
 			for(int col=0; col<NUM_COLUMNS; col++)
 			{
 				System.out.print(state[row][col] +" ");
 			}
 			System.out.println();	//newline
 		}
 	}
 }
