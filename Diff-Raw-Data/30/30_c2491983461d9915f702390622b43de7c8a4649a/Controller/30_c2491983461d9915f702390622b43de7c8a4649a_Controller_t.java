 package fart.dungeoncrawler;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;
 
 public class Controller implements KeyListener
 {
 	private boolean lastFrame[] = new boolean[525];
 	private boolean thisFrame[] = new boolean[525];
	
	private HashMap<Integer, Boolean> tmp = new HashMap<Integer, Boolean>();
 	
 	public boolean isPressed(int key)
 	{
 		if(key < thisFrame.length)
 			return thisFrame[key];
 		return false;
 	}
 	
 	public boolean isLeftPressed() { return isPressed(37); }
 	public boolean isUpPressed() { return isPressed(38); }
 	public boolean isRightPressed() { return isPressed(39); }
 	public boolean isDownPressed() { return isPressed(40); }
 	
 	public boolean justPressed(int key)
 	{
 		if(key < thisFrame.length)
 		{
 			if(!lastFrame[key] && thisFrame[key])
 				return true;
 		}
 		return false;
 	}
 	
 	public void update()
 	{
		lastFrame = thisFrame.clone();
		for(Map.Entry<Integer, Boolean> entry : tmp.entrySet())
			thisFrame[entry.getKey()] = entry.getValue();
		
		tmp.clear();
 	}
 	
 	@Override
 	public void keyPressed(KeyEvent e)
 	{
		tmp.put(e.getKeyCode(), true);
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e)
 	{
		tmp.put(e.getKeyCode(), false);
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e)
 	{
 		
 	}
 	
 }
