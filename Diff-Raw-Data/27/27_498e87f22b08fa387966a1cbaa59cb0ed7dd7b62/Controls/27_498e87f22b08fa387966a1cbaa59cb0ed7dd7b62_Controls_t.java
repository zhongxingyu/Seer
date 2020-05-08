 package Game;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 
 public class Controls implements KeyListener
 {
 
	boolean[] keyPressed = new boolean[KeyEvent.KEY_LAST];
 	
 	
 	@Override
 	public void keyPressed(KeyEvent arg0) 
 	{
		keyPressed[arg0.getKeyCode()] = true;
 	}
 
 	@Override
 	public void keyReleased(KeyEvent arg0) 
 	{
		keyPressed[arg0.getKeyCode()] = false;
 	}
 
 	@Override
 	public void keyTyped(KeyEvent arg0) 
 	{
 		// TODO Auto-generated method stub
 	}
 	
 	public boolean isPressed(int keyCode)
	{	
		return keyPressed[keyCode];
 	}
 
 }
