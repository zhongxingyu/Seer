 package Game;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 public class Keyboard implements KeyListener, MouseMotionListener, MouseListener
 {
 	private static boolean[] keys = new boolean[1024];
 	private static int mousex;
 	private static int mousey;
 	private static int button;
 	static int getMouseX()
 	{
 		return mousex;
 	}
 	static int getMouseY()
 	{
 		return mousey;
 	}
 	public static int getButton()
 	{
 		return button;
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
 	@Override
 	public void mouseClicked(MouseEvent arg0) 
 	{
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void mouseEntered(MouseEvent arg0) 
 	{
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void mouseExited(MouseEvent arg0) 
 	{
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void mousePressed(MouseEvent e) 
 	{
 		button = e.getButton();
 		
 	}
 	@Override
	public void mouseReleased(MouseEvent e) 
 	{
 		button = -1;
 		
 	}
 	
 }
