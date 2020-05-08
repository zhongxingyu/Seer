 package wombat.gui.actions;
 
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JFrame;
 
 import wombat.gui.frames.MainFrame;
 import wombat.gui.icons.IconManager;
 
 /**
  * Format the active document.
  */
 public class Reset extends AbstractAction {
 	private static final long serialVersionUID = -1234756890127385121L;
 
 	public Reset() {
 		super("Reset", IconManager.icon("Reset.png"));
 		putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		for (Frame frame : JFrame.getFrames())
 			if (frame instanceof MainFrame)
 				((MainFrame) frame).resetScheme();
 	}
 }
