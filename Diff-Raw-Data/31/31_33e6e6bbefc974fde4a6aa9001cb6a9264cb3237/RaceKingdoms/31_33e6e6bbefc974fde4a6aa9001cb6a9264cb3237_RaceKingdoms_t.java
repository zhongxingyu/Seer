 package racekingdoms;
 
 
 import handlers.DrawableHandler;
 import handlers.KeyListenerHandler;
 import handlers.MainKeyListenerHandler;
 import handlers.MainMouseListenerHandler;
 import handlers.MouseListenerHandler;
 import handlers.StepHandler;
 import processing.core.PApplet;
 import tests.InputTest;
 
 /**
  * This class starts the program and creates the necessary elements of the game. 
  * This class also informs listeners about keystrokes and the mouse
  *
  * @author Gandalf.
  *         Created 13.6.2013.
  */
 public class RaceKingdoms extends PApplet
 {
 	// ATTRIBUTES	-----------------------------------------------------
 	
 	private static final long serialVersionUID = 1L;
 	
 	private MainKeyListenerHandler mainkeyhandler;
 	private MainMouseListenerHandler mainmousehandler;
 	private StepHandler stephandler;
 	private DrawableHandler drawer;
	
 	private KeyListenerHandler testkeylistenerhandler;
 	private MouseListenerHandler testmouselistenerhandler;
 	
 	private boolean needsUpdating; 
 	
 	
 	// IMPLEMENTED METHODS	---------------------------------------------
 
 	@Override
 	public void setup()
 	{
 		// Sets the screen
 		size(1000, 550);
 		noFill();
 		
 		// Initializes the attributes
 		this.mainkeyhandler = new MainKeyListenerHandler();
 		this.mainmousehandler = new MainMouseListenerHandler();
 		this.stephandler = new StepHandler(16, this);
 		this.drawer = new DrawableHandler(false);
 		this.testkeylistenerhandler = new KeyListenerHandler(false);
 		this.testmouselistenerhandler = new MouseListenerHandler(false);
 		
		this.stephandler.addActor(this.mainkeyhandler);
 		this.mainkeyhandler.addListener(this.testkeylistenerhandler);
 		this.mainmousehandler.addMouseListener(this.testmouselistenerhandler);
 		
 		this.needsUpdating = true;
 		
 		// Starts the game
 		new Thread(this.stephandler).start();
 		
 		// Tests the system
 		test();
 	}
 
 	@Override
 	public void draw()
 	{
 		if (this.needsUpdating)
 		{
 			this.drawer.drawSelf(this);
 			this.needsUpdating = false;
 		}
 	}
 	
 	@Override
 	public void keyPressed()
 	{
		System.out.println("Keypressed");
 		this.mainkeyhandler.onKeyPressed(this.key, this.keyCode, this.key == CODED);
 	}
 	
 	@Override
 	public void keyReleased()
 	{
 		this.mainkeyhandler.onKeyReleased(this.key, this.keyCode, this.key == CODED);
 	}
 	
 	@Override
 	public void mousePressed()
 	{
 		this.mainmousehandler.setMouseStatus(this.mouseX, 
 				this.mouseY, true, this.mouseButton);
 	}
 	
 	@Override
 	public void mouseReleased()
 	{
 		this.mainmousehandler.setMouseStatus(this.mouseX, 
 				this.mouseY, false, this.mouseButton);
 	}
 	
 	
 	// OTHER METHODS	--------------------------------------------------
 	
 	/**
 	 * This method should be called when the screen needs redrawing
 	 */
 	public void callScreenUpdate()
 	{
 		this.needsUpdating = true;
 	}
 	
 	/**
 	 * This method updates the mouse's position in the game
 	 */
 	public void callMousePositionUpdate()
 	{
 		this.mainmousehandler.setMousePosition(this.mouseX, this.mouseY);
 	}
 	
 	private void test()
 	{
		System.out.println("Testaa");
		
 		new InputTest(this.stephandler, this.drawer, 
 				this.testkeylistenerhandler, this.testmouselistenerhandler).test();
 	}
 }
