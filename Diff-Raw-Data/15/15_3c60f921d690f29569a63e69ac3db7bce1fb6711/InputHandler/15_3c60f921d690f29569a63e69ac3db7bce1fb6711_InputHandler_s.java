 package controller;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import view.GUI;
 
 import model.Map;
 import model.Brick;
 
 public class InputHandler implements KeyListener
 {
 	private static InputHandler instance = null;
 	private Map map;
 	private Brick activeBrick;
 	
 	public static InputHandler getInstance()
 	{
 		if(instance == null)
 		{
 			return instance = new InputHandler();
 		}
 		return instance;
 	}
 	
 	private InputHandler()
 	{
 		map = Map.getInstance();
 		activeBrick = Brick.getInstance();
 	}
 
	@Override
 	public void keyPressed(KeyEvent e)
 	{
 		switch(e.getKeyCode())
 		{
 		case 27:
 			System.exit(0);
 			break;
 		case 37:
 			--activeBrick.posX;
 			break;
 		case 39:
 			++activeBrick.posX;
 			break;
 		}
 		GUI.getInstance().repaint();
 		System.out.println(e.getKeyCode());	
 	}
 
	@Override
 	public void keyReleased(KeyEvent e)
 	{
 	}
 
	@Override
 	public void keyTyped(KeyEvent arg0)
 	{
 	}
 }
