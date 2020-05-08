 import acm.program.*;
 import acm.util.MediaTools;
 import acm.util.RandomGenerator;
 import java.applet.AudioClip;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.event.*;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 
 import javax.swing.*;
 import acm.graphics.*;
 import acmx.export.java.io.FileReader;
 
 
 public class Playgame extends GraphicsProgram {
 
 	/**
 	 * Eclipse bothers me if I don't put this in.
 	 */
 	private static final long serialVersionUID = 1L;
 
 	
 	//Sets up the initial environment like the board, button for switching sound on/off and a simple label indicating score 
 	public void init() {
 
 		setSize(APPLICATION_WIDTH, APPLICATION_HEIGHT);
 		add(Sound, NORTH);
 		add(Score, NORTH);
 		setBackground(Color.black);
 		addActionListeners();
 		addKeyListeners();
 		
 		
 		/*Create the gray foundation on which the tetris blocks rest.*/
 		GRect  b = null;
 		for(int i = 0; i <APPLICATION_WIDTH;) {
 			b = new GRect(SIDE,SIDE);
 			b.setColor(Color.gray);
 			b.setFilled(true);
 			add(b,i,APPLICATION_HEIGHT- 5.5* SIDE);
 			i += SIDE;
 		}
 
 		 PlayMusic();
 
 	}
 
 	//obvious what this does
 	private void PlayMusic() {
 		clip = MediaTools.loadAudioClip("mario.au");
 		clip.loop();
 
 	}
 
 	/* The main method just  starts off a new thread
 	 * All the action happens in the run method. That is the way acm graphics library handles stuff.
 	 */
 	public static void main(String[] args) {
 		new Playgame().start(args);
 	}
 
 	
 	/* This function handles the user input. 
 	 * pressing the keys <-- or --> moves the falling piece by exactly a SIDE's width.
 	 * pressing down causes it to fall faster. Pressing any other key causes the piece to rotate.
 	 */
 	public synchronized void keyPressed(KeyEvent k) {
 
 		switch (k.getKeyCode()) {
 
 		case KeyEvent.VK_RIGHT:
 									if (canMoveRight())
 										Current.move(SIDE, 0);
 
 			break;
 
 		case KeyEvent.VK_LEFT:
 									if (canMoveLeft())
 										Current.move(-SIDE, 0);
 
 			break;
 
 		case KeyEvent.VK_DOWN:
 									if (canMoveDown())
 										Current.move(0, SIDE);
 			break;
 
 			
 		case KeyEvent.VK_UP:
 									if (canRotate())
 										Current.Rotate();
 									
 			break;
 		
 		default:					if (canRotate())
 										Current.Rotate();
 		
 			break;
 		}
 
 	}
 
 	/* A piece can rotate if it doesn't go off the board 
 	 * and if it doesn't cross over into other pieces already on the board.
 	 * And rotation is forbidden if the falling piece is within a square of currently existing piece on the board.
 	 * This function tests for both those conditions.
 	 */
 	private boolean canRotate() {
 
 		if(!noElementInFront()) {
 		
 			return false;
 		
 		}
 		else if(getElementAt(Current.getX() + Current.getWidth() +  SIDE , Current.getY() + Current.getHeight()/2) == null) {
 			
 	
 			if (getElementAt(Current.getX()+ Current.getWidth()/2,Current.getY() + Current.getHeight() + SIDE) != null)
 				return false;
 	
 				
 			if((type == 1) && (Current.rotated == 1) && (Current.getX() + Current.getWidth() >= APPLICATION_WIDTH-4*SIDE)
 					&& (getElementAt(Current.getX()-1,Current.getY()+5) != null) )
 				return false;
 		
 			if(Current.getX() + Current.getWidth() >= APPLICATION_WIDTH-SIDE) 
 				
 				return false;
 			
 			
 		
 		}
 		return true;
 	}
 
 	
 	/*A piece an move down if it's front is clear.
 	 * That is, there is no element in front of it.
 	 */
 	private boolean canMoveDown() {
 
 		if (Current != null)
 			return (noElementInFront());
 		else
 			return false;
 	
 	}
 
 	//same drill
 	private boolean canMoveLeft() {
 		
 		return  ((Current.getX()  > 0) && (getElementAt(Current.getX()-SIDE/2,Current.getY()) == null)
 				&& (getElementAt(Current.getX()-SIDE/2,Current.getY()+Current.getHeight()) == null));
 
 	}
 
 	
 	
 	//same drill
 	private boolean canMoveRight() {
 		
 		return ((Current.getX() + Current.getWidth() <= APPLICATION_WIDTH-SIDE)
 				&&	(getElementAt(Current.getX() + Current.getWidth()+1,Current.getY() + Current.getHeight()) == null)
 				&&  (getElementAt(Current.getX() + Current.getWidth()+1,Current.getY() + Current.getHeight()/2) == null));
 		
 	}			
 	
 	
 	/* The function that tests whether the falling piece can move down any further.
 	 * This function is large and complex because it has to know the shape of the
 	 * currently falling piece and the orientation of the falling piece.
 	 * It keeps track of the shape by using the index into the GShapes array from which
 	 * the piece is taken. It asks the piece for its orientation by using the instance variable
 	 * 'rotated' present in all the classes which represent Tetrimonos.
 	 */
 	
 	private boolean noElementInFront() {
 
 		
 		switch(type) {
 		
 		
 		/*		 GShape[] MY_ARRAY = { 0- new Block(),1- new Bar(),2- new FShape(),
  
 				3 -new LShape(),4- new TShape(),5- new ZigzagRight(),6- new ZigzagLeft() }; 
 		 
 		*/
 			case 0:
 						/*Done.Passing all test cases */
 				
 					if ((getElementAt(Current.getX()+ SIDE/2,Current.getY() + Current.getHeight()) == null)
 							&& (getElementAt(Current.getX()+ Current.getWidth()/2 + SIDE/2, Current.getY()+ Current.getHeight()) == null))
 						return true;
 				
 					else
 				    	
 						return false;		    
 				    
 			case 1:
 						/*Done. Passes all test cases */
 					if (Current.rotated == 1) {
 					
 					
 						return (getElementAt(Current.getX() + SIDE/2 , Current.getY() + Current.getHeight()) == null);		
 							
 					}
 					else {
 					
 						return ((getElementAt(Current.getX() + SIDE/2, Current.getY() + Current.getHeight() ) == null)
 								&& (getElementAt(Current.getX() + 3.5*SIDE, Current.getY() + Current.getHeight() ) == null)
 								&& (getElementAt(Current.getX() + 3.5*SIDE, Current.getY() + Current.getHeight() ) == null));
 				        	
 					}  
 				    
 				    
 						
 			case 2:
 					/*done. passes all test cases */
 				
 					if (Current.rotated == 1) {
 				
 						return ( (getElementAt(Current.getX()+ SIDE/2,Current.getY()+ Current.getHeight()) == null)
 								&& (getElementAt(Current.getX()+ SIDE + SIDE/2 ,Current.getY()+ SIDE + 1) == null) );
 					
 					}
 					else if (Current.rotated == 2) {
 					
 						return ( (getElementAt(Current.getX()+ SIDE/2,Current.getY()+ SIDE + 1) == null)
 								&& (getElementAt(Current.getX()+ SIDE + SIDE/2 ,Current.getY()+ SIDE + 1) == null) 
 							&& (getElementAt(Current.getX()+ 2*SIDE + SIDE/2 ,Current.getY()+ Current.getHeight()) == null));
 								
 					}
 					else if(Current.rotated == 3) {
 					
 						return ((getElementAt(Current.getX()+ SIDE/2,Current.getY()+ Current.getHeight()) == null)
 								&& (getElementAt(Current.getX()+ SIDE + SIDE/2 ,Current.getY()+ Current.getHeight()) == null)); 
 					
 					
 					}
 					else {
 					
 						return ((getElementAt(Current.getX()+ SIDE/2,Current.getY()+ Current.getHeight()) == null)
 								&& (getElementAt(Current.getX()+ SIDE + SIDE/2 ,Current.getY()+ Current.getHeight()) == null)
 								&& (getElementAt(Current.getX()+ 2*SIDE + SIDE/2 ,Current.getY()+ Current.getHeight()) == null) );
 						
 					}
 								
 								
 			case 3:
 					/*Done. Passing all test Cases */
 					if (Current.rotated == 1) {
 					
 						return ( (getElementAt(Current.getX()+ SIDE/2,Current.getY()+ Current.getHeight()) == null)
 								&& (getElementAt(Current.getX()+ SIDE + SIDE/2 ,Current.getY()+ Current.getHeight()) == null));
 							
 					}
 					else if (Current.rotated == 2) {
 					
 						return ( (getElementAt(Current.getX()+ SIDE/2,Current.getY()+ Current.getHeight()) == null)
 								&& (getElementAt(Current.getX()+ SIDE + SIDE/2 ,Current.getY()+ SIDE + 1) == null)
 								&& (getElementAt(Current.getX()+ 2*SIDE + SIDE/2 ,Current.getY()+ SIDE + 1) == null));
 												
 					}
 					else if(Current.rotated == 3) {
 					
 						return ((getElementAt(Current.getX()+ SIDE/2,Current.getY()+ SIDE + 1) == null)
 								&& (getElementAt(Current.getX()+ SIDE + SIDE/2 ,Current.getY()+ Current.getHeight()) == null));
 									
 					
 					}
 					else {
 					
 						return ((getElementAt(Current.getX()+ SIDE/2,Current.getY()+ Current.getHeight()) == null)
 								&& (getElementAt(Current.getX()+ SIDE + SIDE/2 ,Current.getY() +  Current.getHeight()) == null)
 								&& (getElementAt(Current.getX()+ 2*SIDE + SIDE/2 ,Current.getY() +  Current.getHeight()) == null));
 							
 					}
 								
 				
 				
 			case 4:
 					/*Passing all the test cases */
 					if (Current.rotated == 1) {
 					
 						return ( (getElementAt(Current.getX()+ SIDE/2,Current.getY()+ SIDE + 1) == null)
 								&& (getElementAt(Current.getX()+ SIDE + SIDE/2 ,Current.getY()+ Current.getHeight()) == null) 
 								&& (getElementAt(Current.getX()+ 2* SIDE + SIDE/2 ,Current.getY()+ SIDE + 1) == null));
 					
 					}
 					else if (Current.rotated == 2) {
 					
 						return ( (getElementAt(Current.getX()+ SIDE/2,Current.getY()+ 2*SIDE + 1) == null)
 								&& (getElementAt(Current.getX()+ SIDE + SIDE/2 ,Current.getY()+ Current.getHeight()) == null) );
 									
 					}
 					else if(Current.rotated == 3) {
 					
 						return ((getElementAt(Current.getX()+ SIDE/2,Current.getY()+ Current.getHeight()) == null)
 								&& (getElementAt(Current.getX()+ SIDE + SIDE/2 ,Current.getY()+ Current.getHeight()) == null)
 								&& (getElementAt(Current.getX()+ 2*SIDE + SIDE/2 ,Current.getY()+ Current.getHeight()) == null));
 					
 					}
 					else {
 					
 						return ((getElementAt(Current.getX()+ SIDE/2,Current.getY()+ Current.getHeight()) == null)
 								&& (getElementAt(Current.getX()+ SIDE + SIDE/2 ,Current.getY()+ 2*SIDE + 1) == null));
 							
 					
 					}
 					
 					
 			case 5:
 				
 					if (Current.rotated == 1) {
 					
 					
 						return ( (getElementAt(Current.getX() + SIDE/2 , Current.getY() + Current.getHeight()) == null)
 								&& (getElementAt(Current.getX() + SIDE + SIDE/2 , Current.getY() + Current.getHeight()) == null)
 								&& (getElementAt(Current.getX() + 2*SIDE + SIDE/2 , Current.getY() + SIDE + 1) == null) );
 						
 							
 							
 					}
 					else {
 					
 						return ((getElementAt(Current.getX() + SIDE/2, Current.getY() + 2*SIDE + 1 ) == null)
 								&& (getElementAt(Current.getX() + SIDE + SIDE/2 , Current.getY() + Current.getHeight() ) == null));
 				        
 					}  
 				
 				
 			case 6:
 					if (Current.rotated == 1) {
 					
 					
 						return ( (getElementAt(Current.getX() + SIDE/2 , Current.getY() + SIDE + 1) == null)
 								&& (getElementAt(Current.getX() + SIDE + SIDE/2 , Current.getY() + Current.getHeight()) == null)
 								&& (getElementAt(Current.getX() + 2*SIDE + SIDE/2 , Current.getY() + Current.getHeight()) == null));
 						
 					}
 					else {
 					
 						return ((getElementAt(Current.getX() + SIDE/2, Current.getY() + Current.getHeight() ) == null)
 								&& (getElementAt(Current.getX() + SIDE + SIDE/2 , Current.getY() + 2*SIDE + 1 ) == null));
 				        
 					}  
 										
 		}
 		
 		return false;
 
 	}
 
 		
 	
 	// If only life were as simple!			
 	public void run() {
 
 		playTetris();
 
 	}
 
 	
 	/*
 	 * Let's play!
 	 */
 	public void playTetris() {
 		
 	
 		/* Indefinite loop. 
 		 * Exits when the incoming element cannot fall.
 		 */
 		while (true) {
 
 			
 			Current = dropNextBrick();
 			
 			
 			if(Current == null) {	//if the dropNextBrick() is unable to place the shape on the board, it returns a null. game over
 				break;
 			}
 			
 			
 			while (true) {
 				
 				if(canMoveDown())
 					Current.move(0, SIDE); //falls by a single row each time.
 				
 				pause(DELAY);
 				
 				if (!canMoveDown()) {
 					
 					pause(DELAY);
 					
 					if(!canMoveDown())   //really can't go down any further? okay. time to break;
 						break;
 				}
 					
 			}
 								
 	
 			pause(DELAY/2);			//pause and catch your breath			
 			
 			
 			clearFilledRows();		//see if the piece filled any of the rows and clear them before dropping next piece.
 			
 			
 			
 		}
 		
 		//write current score to the file if it is the new HighScore
 		 
 		int High_Score = 0;
 		
 		GLabel gameover = new GLabel("GAME OVER");
 		gameover.setColor(Color.BLUE);
 		gameover.setFont(new Font("Serif", Font.BOLD, 40));
 		add(gameover,100,5*SIDE);
 
 		try  
 		{
 		     FileReader fstream = new FileReader(".highscore");
 		     BufferedReader in  = new BufferedReader(fstream);
 		     High_Score = in.read();
 		     in.close();
 		     fstream.close();
 		}
 		catch (Exception e)
 		{
 		    System.err.println("Error: " + e.getMessage());
 		}
 		
 		if(points > High_Score) {
 			
 				High_Score = points;
 			
 				GLabel Label = new GLabel("New High Score!");
 			
 				Label.setColor(Color.MAGENTA);
 				Label.setFont(new Font("Sans-Serif", Font.BOLD, 40));
 				add(Label, 10, 7*SIDE);
 		
 		
 		
 				try  
 				{
 					FileWriter fstream = new FileWriter(".highscore");
 					BufferedWriter out = new BufferedWriter(fstream);
 					out.write(High_Score);
 					out.close();
 					fstream.close();
 				}
 				catch (Exception e)
 				{
 					System.err.println("Error: " + e.getMessage());
 				}
 		}
 		
 		
 		
 		
 	}
 
 	
 	/* This function clears the rows that are filled. It keeps track of which rows are filled 
 	 * using a single dimensional array.
 	 * There are 25 rows on the board.  
 	 * Next, it removes the rows for which row_filled[i] is true.
 	 * and moves all the elements resting on the 'i' th row one row below.
 	 * Since this uses the original tetris algorithm, its possible to see shapes that are 'floating'.
 	 * See wikipedia for an illustration. Original Tetris algorithm makes the game harder. But you get more points.
 	 * We are not entering the Olympics. We just want to have fun playing tetris. So lets just play!
 	 */
 	
 	private void clearFilledRows() {
 		
 		
 		int i =0 ,j = 0 , k=0;
 		
 		int flag = 0;
 		
 		int row_filled[] = new int[25];
 	
 		for(i = 0; i < 25;i++) {
 		
 			flag = 1;
 			for(j = 0; j <= APPLICATION_WIDTH ;) {
 					
 				if (getElementAt(j,i* SIDE + (SIDE/2) ) == null) {
 					flag = 0;
 					break;
 					
 				}
 
 			
 				j += SIDE-1;
 				
 				
 			}
 			
 			row_filled[i] = flag;		
 				
 		}
 		
 		
 		GShape temp = null;
 		
 		for(i = 0; i <row_filled.length; i++) {
 			
 			if (row_filled[i] == 1) {
 			
 				points += i* 100;
 				
 				Score.setText("Score:" + points);
 				for(int l = 0; l < APPLICATION_WIDTH;) {
 					
 					temp = (GShape) getElementAt(l, i*SIDE);
 					if (temp != null)
 						temp.remove(temp.getElement(0));
 					l += SIDE;
 				}
 				row_filled[i] = 0;
 			
 			
 				for(k = i-1; k > 0; k-- ) {
 					
 					for(j = 0; j <= APPLICATION_WIDTH;) {
 			
 						temp = (GShape) getElementAt(j,k*SIDE);
 						if( (temp != null) && getElementAt(temp.getX(), temp.getY() + temp.getHeight()) == null)
 							temp.move(0,SIDE);
 						j += SIDE;
 					}
 				}
 		
 			
 		}
 			
 	}		
 
 				
 		
 		
 		
 	}	
 		
 	
 	//handler for button toggle for sound
 	
 	public void actionPerformed(ActionEvent e) {
 
 	
 		if (e.getSource() == Sound) {
 
 			if (Sound.getText().equals("Sound On")) {
 				Sound.setText("Sound Off");
 				clip.stop();
 			} else {
 				Sound.setText("Sound On");
 				clip.loop();
 			}
 		}
 		
 	}
 
 	
 	//Guess what this does? :)
 
 	private GShape dropNextBrick() {
 	
 		GShape[] MY_ARRAY = { new Block(), new Bar(), new FShape(), new LShape(), new TShape(), new ZigzagRight(), new ZigzagLeft() };
 		
 		int index;
 		
 		index = Math.abs(rgen.nextInt() % NO_OF_ELEMS);
 		
 		Current = MY_ARRAY[index];
 		
 		type = index;
 		
 		if (getElementAt(10*SIDE,SIDE) == null) {
 			add(Current, 10 * SIDE, 0);
 						 
 			return Current;
 		}
 		else 
 			return null;
 		
 	}
 
 	
 	
 	
 	
 	
 	
 	/*Private instance variables */
 
 	/*This holds the reference to the currently falling object - A tetrimono.
 	 * Since it is of GShape, it exploits polymorphism to call appropriate methods.
 	 */
 	
 	private GShape Current = null;
 	
 	//Variable Label to hold and display the score at the top of the board
 	private JLabel Score = new JLabel("Score: 0");
 	
 	//this variable keeps track of what the Currently falling piece precisely is. Used in the method
 	// noElementInFront()
 	private int type = -1;
 	
 	//A reference to the AudioClip object needed to play music
 	private AudioClip clip;
 	
 	
 	//The all important measure of length of a side in pixels. Please do not mess with this value
 	private static final int SIDE = 20;
 	
 	
 	
 	/*Dirty Hacks. Don't ask. The window was giving me trouble when I tried to set the size I wanted.*/
 	private static final int APPLICATION_HEIGHT = 30 * SIDE + SIDE/2;
 	
 	private static final int APPLICATION_WIDTH = 20 * SIDE + 3;
 
 	
 	//Miscellaneous variables
 	
 	private RandomGenerator rgen = RandomGenerator.getInstance();
 
 	private static final int NO_OF_ELEMS = 7;
 	
 	private JButton Sound = new JButton("Sound On");
 	
 	private int points = 0;
 	
 	private static int DELAY = 600;
 
 }
