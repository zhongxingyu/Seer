 import java.awt.*;
 import java.awt.event.*;
 import java.awt.geom.*;
 import javax.swing.*;
 // This file accounts
 // for all the things that happen while handling the window management
 // and makes the game environment window fashioend
 
 
 
 // GamePanel is a class with extends JPanel in Swing, swing is a library
 // or package you can say that allows you to create high performance
 // GUI apps with swing's single threaded model
 // I have followed the model this file will explain it the best I can.
 
 
 // Consider the panel like a canvas ( a piece of paper )
 // Where we can draw graphics onit
 // These graphics are then displayed on the screen
 // We draw these graphics n times in every second
 
 class GamePanel extends JPanel implements ActionListener{
 	// Private members of the GamePanel
 	private boolean is_over; // Is game over ?
 	public Game game;	 	 // Reference to the game its gonna show
 	private Timer t;		 // Internal timer.
 
 	// Public method to paint the game
 	public void paint_game(Graphics2D g2){
 		if( this.game != null ){
 			// Call the function to display the game , if game is not null
 			// Since we assign the game externally , it can be null
 			this.game.Display(g2);
 		}
 	}
 
 	// public method to pain Game Over text, after game is over.
 	public void paint_game_over(Graphics2D g2){
 		g2.drawString("Game Over",Constants.WIDTH/2 - 60,Constants.HEIGHT/2-5);
 	}
 
 	// Public method paint,
 	// This method is over-ridden method 
 	// The JPanel class has a method called paint which is called
 	// Automatically with the current graphics context every time the window is to be rendered
 	// 
 	// When this method is called it passes it a Graphic Pointer, which allows you to draw objects
 	// The Graphics Pointer points to the current Panel or Canvas of the screen which you will draw on
 	// Initially its blank so we have to manually draw each things
 	// This helps since we dont have to explicitely clear them.
 	public void paint(Graphics win){
 		// Call the super class paint, cleans the canvas and etc
 		super.paint(win);
 		// Convert Graphics to Graphics2D allows us to draw rectangles and other methods
 		Graphics2D g2 =(Graphics2D) win;
 		
 		// Call the paint game function, to paint the game
 		// Even if the game is over the game graphics will be
 		// printed to show the last state of the game.
 		this.paint_game(g2);
 			
 		// If game is over then print game over.
 		if( is_over ){
 			this.paint_game_over(g2);
 		}
 		// Synchronise looks with the canvas, otherwise screen will flicker
 		// Comment out these two lines and re run the code u will understand what i mean
 		Toolkit.getDefaultToolkit().sync();
 		// Dispose off the events.
 		// if this is not called we wont know taht the paint is finished.
      	win.dispose();
 	}
 
 	// AN event listener which will listen to our timer
 	// and perform game logic. ( Read more about timer in the constructor )
 	public void actionPerformed(ActionEvent e) {
 		// Calls the tick;
 		if( this.game != null && !is_over ){
 			// Calls the tick method of Game
 			// Which tells the game to update its state
 			// ie : move the snake and see if it has ate
 			// food if so create new food
 			this.game.tick();
 			// Check if the game was over in the last tick.
 			is_over = this.game.isOver();
 		}
 		// Repaint The game , this will actually call the paint function since it makes a request to repaint
         this.repaint();
     }
 
  	// A private class just to handle keyboards
     private class KeyBoardAgent extends KeyAdapter {
         public void keyReleased(KeyEvent e) {
         	// DO nothing
         }
 
         public void keyPressed(KeyEvent e) {
         	// If key is pressed then handle the changes
             game.handleKeyDown(e);
         }
     }
     
     GamePanel(){
     	// setting focussible allows us to listen keyboard events
 		this.setFocusable(true);
 		// Add a key listener, which will call our keyboard methods
 		// We pass it a new KeyBoardAgent
 		// When we press any key, it will call the KeyPressed method in KeyBoardAgent
 		// When we release the key it will call the KeyReleased Method in the KeyBoardAgent;
     	this.addKeyListener(new KeyBoardAgent());
 
 
 
     	// Now this following line will create a timer
     	// and call an action <our actionPerformed Method> every
     	// 50 seconds. 50 is the delay parameter
     	// That is why i have implemented actionlistener
     	// The advantage we get by doing so is
     	// We call our actionListener which is the function which performs our
     	// game logic every 0.050 s , which allows us to make the game playable
     	// as well as the game doesnt hangs
     	// The game simply renders in 1ms or even less being so simple
     	// the rest time its free and allows us to listen key events on single thread
     	// supporting the swing architecture
     	Timer timer = new Timer(50, this);
         timer.start();
 	}
 }
 
 
 // The window frame TOP LEVEL OBJECT
 // The best practise in java is to make a frame and add a panel to the frame
 // The frame handles all the window tasks
 // For example close button minimise etc
 // while the Panel is used for rendering tasks or basic actions.
 class GameFrame extends JFrame{
 	// Current Game Frames, Panel
 	GamePanel ctx;
 
 	public GameFrame(String title){
 		super(title);
 		// Create a game panel
 		// THat is create a canvas for rendering the game graphics
 		ctx = new GamePanel();
 		// Add it to game context
 		// Add the canvas to our window.
 		this.add(ctx);
 
 		// If the close button is pressed we must exit the program
 		// The following method tels the Window to close if the close button is pressed
 		// without doing anything.
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setBackground(Color.white);
 
 		// Get a reference to the content pane in the Frame(Window)
 		// The content pane in the window is the area in the window
 		// other then the Title bar and Borders
 		Container c = this.getContentPane();
 
 		
 		// Object dimensions will be used
 		// to tell the dimensions of the screen 
 		Dimension Dim = new Dimension(Constants.WIDTH,Constants.HEIGHT);
		
 		// Set preffered sise for both the frame and panel
 		// Prefferred size will be the size it will adapt
 		c.setPreferredSize(Dim);
 		ctx.setPreferredSize(Dim);
 		
 		// Pack the window so that it obtains the preffered size
 		// We can see packing as filling objects ina box
 		// It ensures proper spacing
 		// for the window while filling all its object
 		// and make the window of the size.
 		this.pack();
 
 		// We dont want it to be resized
 		// Since out game has a fixed (800x600) resolution
 		this.setResizable(false);
 
 		// Finally show the window to user.
 		// Without calling this our window wont be visible
 		this.setVisible(true);
 	}
 
 }
