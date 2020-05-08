 package mazeEscapeUtils;
 
 import java.awt.event.MouseEvent;
 
 import javax.swing.JOptionPane;
 
 import mazeEscapeApp.GUIDrawer;
 import mazeEscapeApp.MazeEscape;
 import mazeEscapeApp.MazeEscapeApp;
 import mazeEscapeApp.WinnerScreen;
 
 import org.jhotdraw.framework.DrawingEditor;
 import org.jhotdraw.standard.AbstractTool;
 
 public class ForfeitButton extends AbstractTool {
 
 	private GUIDrawer guiDrawer;
 	private MazeEscape maze;
 
 	public ForfeitButton(DrawingEditor newDrawingEditor, GUIDrawer gd,
 			MazeEscape m) {
 		super(newDrawingEditor);
 		guiDrawer = gd;
 		maze = m;
 		// Disables this button on launch to prevent errors
 		this.setEnabled(false);
 	}
 
 	public void setGUIDrawer(GUIDrawer gd) {
 		guiDrawer = gd;
 	}
 
 	public void activate() {
 		JOptionPane
 				.showMessageDialog(
 						null,
 						"You have now lost the game. Click OK to see the solution. Click reset to play again.");
 		guiDrawer.drawSolution();
		maze.setSaveButton(false);
		maze.setForfeitButton(false);
 		deactivate();
 		// WinnerScreen screen = new WinnerScreen(maze);
 		// screen.forfeit();
 	}
 
 	public void mouseDown(MouseEvent e, int x, int y) {
 		// WinnerScreen screen = new WinnerScreen(maze);
 		// screen.forfeit();
 	}
 }
