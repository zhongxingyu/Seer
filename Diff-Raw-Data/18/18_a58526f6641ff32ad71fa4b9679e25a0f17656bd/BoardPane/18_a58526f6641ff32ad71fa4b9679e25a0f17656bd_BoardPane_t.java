 package jsk.sudoku.ui;
 
 import java.awt.GridLayout;
 
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 
 import jsk.sudoku.BoardType;
 import jsk.sudoku.model.Board;
 import jsk.sudoku.model.Cell;
 import jsk.sudoku.model.CellBlock;
 import jsk.sudoku.model.CellListener;
 
 public class BoardPane extends JPanel {
 	private static final long serialVersionUID = 6349761049080316661L;
 
 	public BoardPane(Board board, boolean buttons) {
 		super(new GridLayout(board.type.baseDimension, board.type.baseDimension, 4, 4));
 		
 		for (CellBlock section : board.sections) {
 			add(createSection(section, board.type, buttons));
 		}
 	}
 
 	private JPanel createSection(CellBlock section, BoardType board, boolean buttons) {
 		JPanel panel = new JPanel(new GridLayout(board.baseDimension, board.baseDimension, 1, 1));
 
 		for (Cell cell : section) {
 			CellListener field = buttons ?
 				new ButtonField(board, cell) : new Field(cell, board, 1);
 			
 			panel.add((JComponent) field);
 			
 			cell.addListener(field);
 		}
 
 		return panel;
 	}
 }
