 package phm1.NewJ;
 import java.awt.event.*;
 
 import javax.swing.SwingUtilities;
 import javax.swing.event.PopupMenuEvent;
 import javax.swing.event.PopupMenuListener;
 
 public class MyMouseListener implements MouseListener, MouseMotionListener, PopupMenuListener {
 
 	private NJClass box;
 	private int offsetX;
 	private int offsetY;
 	private GUI gui;
 	
 	MyMouseListener(GUI g) {
 		this.gui = g;
 	}
 		
 	public void mouseClicked(MouseEvent e) {
 		gui.getdPanel().setInheriting(false);
		gui.getdPanel().setAggregating(false);
 	}
 
 	public void mouseEntered(MouseEvent arg0) {
 		
 	}
 
 	public void mouseExited(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void mousePressed(MouseEvent e) {
 		NJClass box = gui.getdPanel().findNearestClass(e.getX(), e.getY());
 		//if the mouse is clicked inside a box, it sets this.box to that box so other methods can use it - P
 		if(box !=null){
 			gui.getdPanel().setSelected(box);
 			if(SwingUtilities.isLeftMouseButton(e)){
 				this.box = box;
 				this.offsetX = e.getX() - box.getX();
 				this.offsetY = e.getY() - box.getY();
 				// Works out where the cursor is within the box so we can move the box relative to the mouse - J
 			}
 			gui.populateEditMenu(box);
 		}
 		else {
 			gui.getdPanel().unselectAll();
 		}
 	}
 
 	public void mouseReleased(MouseEvent e) {
 		//stops the selected box jumping to the mouse when you click on blank space - P
 		box = null;
 	}
 
 	public void mouseDragged(MouseEvent e) {
 		//lets you drag a box around if it is selected - P
 		//you have to click the box before you can drag it around -p
 		if (box !=null){
 			int newX = e.getX() - this.offsetX;
 			int newY = e.getY() - this.offsetY;
 			// Moves relative to the cursor - J
 			box.update(newX, newY);
 			gui.getdPanel().repaint();
 		}
 		else {
 			gui.getdPanel().unselectAll();
 		}
 	}
 
 	public void mouseMoved(MouseEvent e) {
 		if(gui.getdPanel().getInheriting() && gui.getdPanel().getSelected().getInheritance() != null){
 			gui.getdPanel().getSelected().getInheritance().setXYto(e.getX(), e.getY());
 			gui.getdPanel().repaint();
 		}
 		if(gui.getdPanel().getAggregating() && gui.getdPanel().getSelected() != null && gui.getdPanel().getSelected().getTempAggregation() != null){
 			gui.getdPanel().getSelected().getTempAggregation().setXYto(e.getX(), e.getY());
 			gui.getdPanel().repaint();
 		}
 	}
 
 	@Override
 	public void popupMenuCanceled(PopupMenuEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	
 	
 
 }
