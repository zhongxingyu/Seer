 package phm1.NewJ;
 import javax.swing.*;
 
 import java.awt.*;
 
 public class DiagramPanel extends JPanel{
 	private GUI gui;
 	private NJClass selected;
 	private boolean inheriting;
 	
 	public boolean getInheriting(){
 		return inheriting;
 	}
 	
 	public void setInheriting(boolean inheriting){
 		this.inheriting = (selected != null) && inheriting;
 		if(this.inheriting){
 			selected.setInherits(new NJInheritance());
 		}
 	}
 	
 	public NJClass getSelected() {
 		return selected;
 	}
 
 	public void setSelected(NJClass selected) {
 		if(inheriting){
			if(selected == null && this.selected != null){
 				this.selected.setInherits(null);
 			} else if(selected != null && this.selected.getInherits() != null){
 				this.selected.getInherits().setTo(selected);
 			}
 		}
 		this.selected = selected;
 	}
 
 	public DiagramPanel(GUI g){
 		this.gui = g;
 		this.inheriting = false;
 		this.setLayout(new BorderLayout());	
 		this.setBackground(Color.WHITE);
 	}
 	
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		gui.drawAll(g);
 	}
 	
 	public NJClass findNearestClass(int x, int y) {
 		NJClass c;
 		//calls a method in vector of boxes to find which box the mouse is inside, then returns that box
 		c = gui.clickedInBox(x, y);
 		repaint();
 		return c;
 	}
 	
 	public void unselectAll() {
 		setSelected(null);
 	}
 	
 	
 }
