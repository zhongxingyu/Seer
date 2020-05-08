 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 public class Mover implements Runnable
 {
 	protected JApplet a;
 	protected JSlider s;
 	protected int x, y; // coordinates of the Mover
 	protected int state; // state of the Mover (in CS, waiting for CS, or neither)
 	protected int direction; // current direction of Mover
 	
 	public Mover(JApplet app, JSlider slide)
 	{
 		a = app;
 		s = slide;
 	}
 	
 	public void run()
 	{
 		while(true)
 		{
 			try
 			{
 				Thread.sleep(100);
 			} catch(InterruptedException ex){};
 			
 			
 			if(direction == RIGHT) // we are currently moving right
 			{
 				if(x < MainDemo.BRIDGE_LEFT) // not yet to the bridge
 				{
 					setPosition(x + s.getValue(), y + s.getValue());
 					if(x >= MainDemo.BRIDGE_LEFT) // if we reach the bridge
 					{
 						setPosition(MainDemo.BRIDGE_LEFT, MainDemo.BRIDGE_Y);
 						state = WAITING;
 						state = IN_CS; // only to see if visualizer works, will remove
 					}
 				}
 				else if(x >= MainDemo.BRIDGE_LEFT && x < MainDemo.BRIDGE_RIGHT) // we are on the bridge
 				{
 					if(state == WAITING);
 						// mutual exclusion stuff here
 					else if(state == IN_CS)
 					{
 						setPosition(x + s.getValue(), MainDemo.BRIDGE_Y);
 						if(x >= MainDemo.BRIDGE_RIGHT) // if we reach the end of the bridge
 						{
 							setPosition(MainDemo.BRIDGE_RIGHT, MainDemo.BRIDGE_Y);
 							state = NEITHER;
 						}
 					}
 				}
 				else if(x >= MainDemo.BRIDGE_RIGHT) // we are past the bridge
 				{
 					setPosition(x + s.getValue(), y - s.getValue());
 					if(x >= MainDemo.MAX_X || y <= MainDemo.MIN_Y) // we reach the right edge, start going down
 					{
 						x = MainDemo.MAX_X;
 						y = MainDemo.MIN_Y;
 						direction = DOWN;
 					}
 				}
 			}
 			else if(direction == LEFT) // we are currently moving left
 			{
 				if(x > MainDemo.BRIDGE_RIGHT) // not yet to the bridge
 				{
 					setPosition(x - s.getValue(), y - s.getValue());
 					if(x <= MainDemo.BRIDGE_RIGHT) // if we reach the bridge
 					{
 						setPosition(MainDemo.BRIDGE_RIGHT, MainDemo.BRIDGE_Y);
 						state = WAITING;
 						state = IN_CS; // only to see if visualizer works, will remove
 					}
 				}
 				else if(x > MainDemo.BRIDGE_LEFT && x <= MainDemo.BRIDGE_RIGHT) // we are on the bridge
 				{
 					if(state == WAITING);
 						// mutual exclusion stuff here
 					else if(state == IN_CS)
 					{
 						setPosition(x - s.getValue(), MainDemo.BRIDGE_Y);
						if(x <= MainDemo.BRIDGE_LEFT) // if we reach the end of the bridge
 						{
 							setPosition(MainDemo.BRIDGE_LEFT, MainDemo.BRIDGE_Y);
 							state = NEITHER;
 						}
 					}
 				}
 				else if(x <= MainDemo.BRIDGE_LEFT) // we are past the bridge
 				{
 					setPosition(x - s.getValue(), y + s.getValue());
 					if(x <= MainDemo.MIN_X || y >= MainDemo.MAX_Y) // we reach the left edge, start going up
 					{
 						x = MainDemo.MIN_X;
 						y = MainDemo.MAX_Y;
 						direction = UP;
 					}
 				}
 			}
 			else if(direction == UP) // we are currently moving up
 			{
 				setPosition(x, y - s.getValue());
 				if(y < MainDemo.MIN_Y) // if we get to the top of the loop, turn right
 				{
 					y = MainDemo.MIN_Y;
 					direction = RIGHT;
 				}
 			}
 			else if(direction == DOWN)
 			{
 				setPosition(x, y + s.getValue());
 				if(y > MainDemo.MAX_Y) // if we get to the bottom of the loop, turn left
 				{
 					y = MainDemo.MAX_Y;
 					direction = LEFT;
 				}
 			}
 			
 			//repaint the applet to reflect the position change
 			a.repaint();
 		}
 	}
 	
 	// Set x and y coordinates of Mover
 	public void setPosition(int a, int b)
 	{ 
 		x = a;
 		y = b;
 	}
 
 	// get x coordinate
 	public int getX() 
 	{ 
 		return x; 
 	}  
 
 	// get y coordinate
 	public int getY() 
 	{ 
 		return y; 
 	}
 	
 	public int getState()
 	{
 		return state;
 	}
 	
 	public void setState(int n)
 	{
 		if(n >= 0 && n <= 2)
 			state = n;
 		else
 			return;
 	}
 	
 	public int getDirection()
 	{
 		return direction;
 	}
 	
 	public void setDirection(int n)
 	{
 		if(n >= 0 && n <= 3)
 			direction = n;
 		else
 			return;
 	}
 
 	// convert the position of the Mover into a String representation
 	public String toString() 
 	{ 
		return "[" + x + ", " + y + "]";
 	}
 	
 	// these represent the current direction the Mover is moving
 	public static final int LEFT = 0;
 	public static final int RIGHT = 1;
 	public static final int UP = 2;
 	public static final int DOWN = 3;
 	
 	// these represent the states of the Mover regarding the CS
 	public static final int WAITING = 0;
 	public static final int IN_CS = 1;
 	public static final int NEITHER = 2;
 }
