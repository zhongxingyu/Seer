 package wombat.gui.actions;
 
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 

 import javax.swing.AbstractAction;
 import javax.swing.JFrame;
 
import wombat.gui.*;
 import wombat.gui.frames.MainFrame;
 
 /**
  * Close the active document.
  */
 public class ShowError extends AbstractAction {
 	private static final long serialVersionUID = 4777775801276799438L;
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		for (Frame frame : JFrame.getFrames())
 			if (frame instanceof MainFrame)
 				((MainFrame) frame).showDebug();
 	}
 }
