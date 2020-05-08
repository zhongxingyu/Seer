 package frontend;
 
 import info.gridworld.grid.BoundedGrid;
 
 import javax.swing.JComponent;
 import backend.*;
 
 public class MineField extends JComponent {
 	
 	private Minesweeper game;
 	
 	public MineField(Minesweeper game){
 		setGame(game);
 		setUp();
 	}
 	
 	private void setUp() {
 		
 		BoundedGrid<Spot> grid = game.getGrid();
 		
 	}
 
 	public void setGame(Minesweeper g){
 		game = g;
 	}
 	
 }
