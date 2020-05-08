 package mazeEscapeApp;
 
 import java.awt.event.MouseEvent;
 import javax.swing.JOptionPane;
 import org.jhotdraw.framework.DrawingEditor;
 import org.jhotdraw.standard.*;
 
 public class ForfeitButton extends AbstractTool {
 
 	private GUIDrawer guiDrawer;
 
 	public ForfeitButton(DrawingEditor newDrawingEditor, GUIDrawer gd, MazeEscape me) {
 		super(newDrawingEditor);
 		guiDrawer = gd;
 	}
 
 	public void setGUIDrawer(GUIDrawer gd) {
 		guiDrawer = gd;
 	}
 
 	public void activate() {
 		JOptionPane.showMessageDialog(null,
 				"You have now lost the game. Click OK to see the solution. Click anywhere for more options.");
 
 			guiDrawer.drawSolution();
 			
 			//TODO: Insert other "game over" code here to lock the maze
 	}
 	
 	public void deactivate()
 	{
 		JOptionPane.showMessageDialog(null, "The maze will now implode.");
 		System.exit(0);
 	}
 	
 	public void mouseDown(MouseEvent e, int x, int y)
 	{
 		JOptionPane.showMessageDialog(null, "Someone pls replace this line of code with real menu code. Thanks!");
 	}
 }
