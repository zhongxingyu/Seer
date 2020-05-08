 package sk.amokk.imagesorter.actions;
 
 import java.awt.event.ActionEvent;
 import java.io.File;
 
 import javax.swing.AbstractAction;
 
 import sk.amokk.imagesorter.gui.PanelMover;
 import sk.amokk.imagesorter.utils.ImageUtils;
 
 public class ActionMove extends AbstractAction {
 
 	private static final long serialVersionUID = 1L;
 	private PanelMover mover;
 
 	public ActionMove(PanelMover panelMover) {
 		this.mover = panelMover;
 
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {

		System.out.println(mover);
 		
 		System.out.println("moved to: " + mover.getPath());
 		File image = ImageUtils.getActualImage();
 		ImageUtils.moveImage(image, mover.getPath());
 	}
 }
