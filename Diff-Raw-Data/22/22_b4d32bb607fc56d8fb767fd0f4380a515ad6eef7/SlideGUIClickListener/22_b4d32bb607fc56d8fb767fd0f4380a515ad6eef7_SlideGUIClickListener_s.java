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
 		if (event.getKeyCode() == KeyEvent.VK_LEFT)
 			gui.prev();
		else
			gui.next();
 	}
 
 }
