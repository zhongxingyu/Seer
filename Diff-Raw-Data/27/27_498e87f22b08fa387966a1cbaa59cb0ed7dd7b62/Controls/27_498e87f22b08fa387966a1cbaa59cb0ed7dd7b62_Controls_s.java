 package Game;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
import java.util.ArrayList;
 
 
 public class Controls implements KeyListener
 {
 
	public int keyPressed = 0;
 	
 	
 	@Override
 	public void keyPressed(KeyEvent arg0) 
 	{
		keyPressed = arg0.getKeyCode();
 	}
 
 	@Override
 	public void keyReleased(KeyEvent arg0) 
 	{
		keyPressed = 0;
 	}
 
 	@Override
 	public void keyTyped(KeyEvent arg0) 
 	{
 		// TODO Auto-generated method stub
 	}
 	
 	public boolean isPressed(int keyCode)
	{
		return keyPressed == keyCode;
 	}
 
 }
