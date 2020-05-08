 package Game;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 
 public class Keyboard implements KeyListener, MouseMotionListener
 {
 	private static boolean[] keys = new boolean[1024];
 	private static int mousex;
 	private static int mousey;
 	static int getMouseX()
 	{
 		return mousex;
 	}
 	static int getMouseY()
 	{
 		return mousey;
 	}
 	public static boolean isKeyPressed(int keycode)
 	{
 		return keys[keycode];
 	}
 	
 	@Override
 	public void keyPressed(KeyEvent e) 
 	{
 		keys[e.getKeyCode()] = true;
 		
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) 
 	{
 		keys[e.getKeyCode()] = false;
 		
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) 
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
	public void mouseDragged(MouseEvent e)
	{
		mousex = e.getX();
		mousey = e.getY();
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) 
 	{
 		mousex = e.getX();
 		mousey = e.getY();
 		
 	}
 	
 }
