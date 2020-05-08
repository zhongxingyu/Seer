 package uk.ac.shef.tiledpres.gui;
 
 import java.awt.event.KeyEvent;
 
 import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
 import edu.umd.cs.piccolo.event.PInputEvent;
 
 public class SlideGUIClickListener extends PBasicInputEventHandler
 {
 	SlideGUI gui;
 	
 	public SlideGUIClickListener(SlideGUI gui)
 	{
 		this.gui = gui;
 	}
 	
 	@Override
 	public void mouseClicked(PInputEvent event)
 	{
 		gui.selectSlide(event.getPosition().getX(), event.getPosition().getY());
 	}
 
 	@Override
 	public void keyPressed(PInputEvent event)
 	{
		System.err.println("KEY = "  + event.getKeyCode());
 		if (event.getKeyCode() == KeyEvent.VK_LEFT)
 			gui.prev();
		else if (event.getKeyCode() != KeyEvent.VK_ALT && event.getKeyCode() != KeyEvent.VK_TAB)
			gui.next();			
 	}
 
 }
