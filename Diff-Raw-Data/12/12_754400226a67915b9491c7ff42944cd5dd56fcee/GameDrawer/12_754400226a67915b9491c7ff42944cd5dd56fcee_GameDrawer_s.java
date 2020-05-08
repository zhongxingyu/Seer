 package hhu.propra_2013.gruppe_13;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 class GameDrawer implements Runnable {
 	
 	// List of all Objects within the game, JPanel and the number of locations
 	private ArrayList<ArrayList<GameObjects>> rooms;
 	private JPanel 	game;
 	private final 	JFrame gameWindow;
 	private int 	location;	
 	private final 	Image background;
 	boolean gameRunning;
 	
 	// Constructor for class
 	GameDrawer(ArrayList<ArrayList<GameObjects>> objectsInit, JFrame inFrame) {
 		rooms 		= objectsInit;
 		gameWindow 	= inFrame;
 		location 	= 0;
 		background 	= Toolkit.getDefaultToolkit().getImage("Layout.jpg");
 	}
 	
 	// Initiate current objects variables, returns constructed JPanel
 	JPanel init(Logic inLogic) {
 		// Build a new panel, override paint method
 		game = new JPanel() {
 			// Serial-ID in order to appease Eclipse
 			private static final long 	serialVersionUID = 1L;
 			private int 				x0, y0, xMax, yMax;
 			private int 				height, width;
 			private double 				step;
 			
 			// Actual paint method, is great for painting stuff... and cookies
 			protected void paintComponent(Graphics g) {
 				Graphics2D g2d = (Graphics2D) g;
 				super.paintComponent(g2d);
 				this.setBackground(Color.BLACK);
 				
 				// height und width speichern die innere Fenstergröße, x0,y0 sind für die Bildposition
 				height 	= gameWindow.getContentPane().getHeight();
 				width	= gameWindow.getContentPane().getWidth();
 				
 				// Abhängig von der Höhe und Breite des Fensters muss das Spielfeld entsprechend angepasst werden
 				x0 = (int)Math.round(0.5*(width-height*4/3.));
 				
 				if (x0 < 0) {							// Fenster ist höher als breit
 					x0 = 0;
 					y0 = (int)Math.round(0.5*(height-3/4.*width));
 					step = width/24.;
 					
 					// male den Hintergrund
 					g2d.drawImage(background, x0, y0, width, (int)(width*3/4.), this);
 				} else {								// Fenster ist breiter als hoch
 					y0 = 0;
 					step = height/18.;					// entspricht 4/3*1/24*height
 
 					// male den Hintergrund
 					g2d.drawImage(background, x0, y0, (int)(height*4/3.), height, this);
 				}
 				
 				// Setze nun den Startpunkt auf die linke obere  Ecke im Spielfeld
 				x0 += step;
 				y0 += step;
 
 				// versuch die korrekte position der zeichenfläche festzulegen(wird bald wegfallen)
 				// TODO: entfernen, nachdem es Benes "Seal of Approval" erhält
 				xMax = (int)Math.round(22*step);
 				yMax = (int)Math.round(13*step);
 				g2d.setColor(Color.LIGHT_GRAY);
 				g2d.fillRect(x0, y0, xMax, yMax);
 				g2d.setColor(Color.black);
 
 				// Iterate over all objects and call draw method
 				ArrayList<GameObjects> list = rooms.get(location);
 				for(GameObjects toDraw : list) {
 					toDraw.draw(g2d, x0, y0, step);
 				}
 			}
 		};
 
 		// initialize the game panel at an appropriate size, return to caller
 		game.setSize(gameWindow.getContentPane().getSize());
 		return game;
 	}
 	
 	// remove a drawable object, thus not every enemy and wall needs to be called if it has been destroyed
 	void removeDrawableObject (GameObjects toRemove) {
 		rooms.get(location).remove(toRemove);
 	}
 	
 	// Tell the draw methods which location to draw
 	void setRoom(int inlocation) {
 		location = inlocation;
 	}
 	
 	void setGameRunning(boolean b) {
 		gameRunning = b;
 		System.out.println("maria wars2");
 	}
 	
 	@Override // Override Thread method run, this will implement the game loop
 	public void run() {
 		long 	time;
 		long 	temp;
 		
 
 		
 		// game loop, TODO: repaint on screen synchronization (not sure if this is possible with our library)
 		while (gameRunning) {
 			// get current system time, this will determine fps
 			time = System.currentTimeMillis();
 			
 			// Repaint the game and wait
 			game.repaint();
 						
 			try {
 				// tries to set the draw method at 62.5fps
 				if((temp = System.currentTimeMillis()-time) < 16)	
 					Thread.sleep(16-temp);				
 			} catch (InterruptedException e) {
 				System.err.println("Graphics Thread interrupted, continuing execution. ");
 			}
 		}
 	}
 }
