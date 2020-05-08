 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 
 
 public class GridButtonHandler implements ActionListener{
 	private Puzzle puzzle;
 	private Gui gui;
 	
 	public GridButtonHandler (Puzzle puzzle, Gui gui)
 	{
 		this.puzzle = puzzle;
 		this.gui = gui;
 	}
 	
 	public void actionPerformed(ActionEvent event) {
 		//obtaining the x and y coordinates of the button from the label
 		JButton source = (JButton) event.getSource();
 		String newValue = this.gui.getInputToUse().toString();
 		source.setText(newValue);
 		String label = source.getName();	//coordinates are in the name of the button
 		char[] coordinates = label.toCharArray();
 		int xCoordinate = coordinates[0] - 48;
 		int yCoordinate = coordinates[1] - 48;
 		System.out.println("setting (" + xCoordinate + ", " + yCoordinate + ") to " + this.gui.getInputToUse());
		this.puzzle.addPosition(xCoordinate, yCoordinate, this.gui.getInputToUse());
 	}
 
 }
