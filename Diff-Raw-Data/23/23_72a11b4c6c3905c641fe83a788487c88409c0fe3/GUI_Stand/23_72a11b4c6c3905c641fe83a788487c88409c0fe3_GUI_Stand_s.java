 package gui;
 
 import javax.swing.ImageIcon;
 
 public class GUI_Stand extends GUI_Component {
 	ImageIcon standpic;
 	int x, y;
 	GUI_Kit kit;
 	GUI_Kit temp;
 
 	GUI_Stand(int xpos, int ypos) {
 		standpic = new ImageIcon("gfx/Kit_Stand.png");
 		x = xpos;
 		y = ypos;
 		myDrawing = new Drawing(x, y, "Kit_Stand.png");
 		
 	}
 
 	public void removeKit() {
 		temp = kit;
 		kit = null;
 	}
 	
 	public void paintComponent() {
 		myDrawing.posX = x;
 		myDrawing.posY = y;
 		
 		myDrawing.subDrawings.clear();
 		
 		if(kit != null) {
 			kit.paintComponent();
 			myDrawing.subDrawings.add(kit.myDrawing);
 		}
 	}
 
 	public void updateGraphics() {
		// Nothing needed here yet
 	}
 }
