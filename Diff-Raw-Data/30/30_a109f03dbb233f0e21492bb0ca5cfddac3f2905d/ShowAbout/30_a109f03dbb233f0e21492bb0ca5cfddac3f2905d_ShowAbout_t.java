 package wombat.gui.actions;
 
 import java.awt.event.ActionEvent;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 
 import wombat.gui.frames.AboutFrame;
 import wombat.gui.icons.IconManager;
 
 /**
  * Close the active document.
  */
 public class ShowAbout extends AbstractAction {
 	private static final long serialVersionUID = 5083860489087944835L;
 
 	public ShowAbout() {
 		super("ShowAbout", IconManager.icon("ShowAbout.png"));
 		putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		AboutFrame.showMe();
 	}
 }
