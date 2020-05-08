 /*
  * Input.java
  * 
  * Part of the GameSDK starterkit made for the seminar at "The Gathering 2012 - At the end of the universe"
  * 
  * This source code is provided AS-IS and without any warranty. Use at own risk.
  * You can use and modify this source any way you want, but please send me an E-mail to petterroea@skymiastudios.com
  * if you make a game from it - I would like to keep a list of all games, as i am curious. You may also use this for games
  * that cost money. So it is basically public domain, except that i own the rights to it, but give you the rights to use
  * it for the purposes above. You cant sell this kit. You can sell a game based on it, but not the source code to this kit.
  * Also, this big comment must stay in all code files that originated from the starterkit, no matter how much they are
  * modified.
  * 
  * If you want support for this SDK, feel free to contact me. If you are at "The Gathering 2012", you can
  * look for me in the creative lounge(Where i sit). If not, feel free to send me an E-Mail to the above
  * adress. If you need normal java help, ask a mentor.
  * 
  */
 package net.petterroea.starterkit;
 
 import java.awt.event.*;
 /**
  * Input class used for listening to the mouse and keyboard, and telling the current screen about it. It also can be used to find out which key is pressed down at the moment, as well as the mouse coordinates and button states.
  * @author petterroea
  *
  */
 public class Input implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, FocusListener{
 	/**
 	 * An array of boolean(true/false) values indicating what state the key at the specific index is. True = pressed.
 	 */
 	public static boolean[] keys;
 	/**
 	 * Game object used for telling the current screen
 	 */
 	Game game;
 	/**
 	 * True if the left mouse button is active(pressed)
 	 */
 	boolean leftMouseBtn = false;
 	/**
 	 * True if the right mouse button is active(pressed)
 	 */
 	boolean rightMouseBtn = false;
 	/**
 	 * True if the middle mouse button is active(pressed)
 	 */
 	boolean middleMouseBtn = false;
 	/**
 	 * Mouse X position
 	 */
	int mousex = 0;
 	/**
 	 * Mouse Y position
 	 */
	int mousey = 0;
 	/**
 	 * An enum to make it easier to understand mouse buttons
 	 * @author petterroea
 	 *
 	 */
 	public enum MouseBtn{
 		/**
 		 * Not a valid mosue button, or nothing
 		 */
 		NONE,
 		/**
 		 * Left mouse button
 		 */
 		LEFT,
 		/**
 		 * Middle mouse button
 		 */
 		MIDDLE,
 		/**
 		 * Right mouse button
 		 */
 		RIGHT
 	}
 	/**
 	 * Basic constructor for the input class.
 	 * 
 	 * You need to add the listeners in the game
 	 * @param game The game object
 	 */
 	public Input(Game game)
 	{
 		this.game = game;
 		keys = new boolean[600];
 		for(int i = 0; i < keys.length; i++)
 		{
 			keys[i] = false;
 		}
 	}
 	/**
 	 * Tells you which mouse button is pressed based on the id you get from MouseEvent.getButton()
 	 * @param mouseBtnId The id you get from MouseEvent.getButton()
 	 * @return The mouse button behind the id
 	 */
 	public MouseBtn getMouseButton(int mouseBtnId)
 	{
 		if(mouseBtnId == 1)
 		{
 			return MouseBtn.LEFT;
 		}
 		else if(mouseBtnId == 2)
 		{
 			return MouseBtn.MIDDLE;
 		}
 		else if(mouseBtnId == 3)
 		{
 			return MouseBtn.RIGHT;
 		}
 		return MouseBtn.NONE; //Hvis det ikke er en av de knappene over, s er det ikke en knapp(Eler en fancy knapp). Se http://docs.oracle.com/javase/7/docs/api/java/awt/event/MouseEvent.html#getButton%28%29
 	}
 	@Override
 	/**
 	 * Used for listening
 	 */
 	public void mouseWheelMoved(MouseWheelEvent arg0) {
 		game.screen.mouseWheelMoved(arg0);
 		
 	}
 	@Override
 	/**
 	 * Used for listening
 	 */
 	public void mouseDragged(MouseEvent arg0) {
 		game.screen.mouseDragged(arg0);
 		mousex = arg0.getX();
 		mousey = arg0.getY();
 	}
 
 	@Override
 	/**
 	 * Used for listening
 	 */
 	public void mouseMoved(MouseEvent arg0) {
 		game.screen.mouseMoved(arg0);
 		mousex = arg0.getX();
 		mousey = arg0.getY();
 		
 	}
 
 	@Override
 	/**
 	 * Used for listening
 	 */
 	public void mouseClicked(MouseEvent arg0) {
 		game.screen.mouseClicked(arg0);
 		
 	}
 
 	@Override
 	/**
 	 * Used for listening
 	 */
 	public void mouseEntered(MouseEvent arg0) {
 		game.screen.mouseEntered(arg0);
 		
 	}
 
 	@Override
 	/**
 	 * Used for listening
 	 */
 	public void mouseExited(MouseEvent arg0) {
 		game.screen.mouseExited(arg0);
 		
 	}
 
 	@Override
 	/**
 	 * Used for listening
 	 */
 	public void mousePressed(MouseEvent arg0) {
 		game.screen.mousePressed(arg0);
 		if(arg0.getButton() == 1)
 		{
 			leftMouseBtn = true;
 		}
 		else if(arg0.getButton() == 2)
 		{
 			middleMouseBtn = true;
 		}
 		else if(arg0.getButton() == 3)
 		{
 			rightMouseBtn = true;
 		}
 		
 	}
 
 	@Override
 	/**
 	 * Used for listening
 	 */
 	public void mouseReleased(MouseEvent arg0) {
 		game.screen.mouseReleased(arg0);
 		if(arg0.getButton() == 1)
 		{
 			leftMouseBtn = false;
 		}
 		else if(arg0.getButton() == 2)
 		{
 			middleMouseBtn = false;
 		}
 		else if(arg0.getButton() == 3)
 		{
 			rightMouseBtn = false;
 		}
 	}
 
 	@Override
 	/**
 	 * Used for listening
 	 */
 	public void keyPressed(KeyEvent arg0) {
 		if(arg0.getKeyCode() < 600)
 		{
 			keys[arg0.getKeyCode()] = true;
 		}
 		
 	}
 
 	@Override
 	/**
 	 * Used for listening
 	 */
 	public void keyReleased(KeyEvent arg0) {
 		if(arg0.getKeyCode() < 600)
 		{
 			keys[arg0.getKeyCode()] = false;
 		}
 		
 	}
 
 	@Override
 	/**
 	 * Used for listening
 	 */
 	public void keyTyped(KeyEvent arg0) {
 		game.screen.keyTyped(arg0);
 		
 	}
 	@Override
 	/**
 	 * Used for listening
 	 */
 	public void focusGained(FocusEvent e) {
 		game.screen.focusGained(e);
 		
 	}
 	@Override
 	/**
 	 * Used for listening
 	 */
 	public void focusLost(FocusEvent e) {
 		game.screen.focusLost(e);
 		
 	}
 
 }
