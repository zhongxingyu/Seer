 package jsk.sudoku.ui;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import jsk.sudoku.BoardType;
 import jsk.sudoku.model.Cell;
 import jsk.sudoku.model.CellListener;
 import jsk.sudoku.model.Possibilities;
 
 public class ButtonField extends JPanel implements CellListener {
 	private static final long serialVersionUID = 1420539115061160063L;
 	private final BoardType type;
 	private int[] values;
 	
 	private static class Listen implements ActionListener {
 		private final Cell cell;
 		private final int value;
 		private Listen(Cell cell, int value) {
 			this.cell = cell;
 			this.value = value;
 		}
 		@Override
		public void actionPerformed(ActionEvent e) {
			cell.solve(value);
 		}
 	}
 
 	
 	public ButtonField(BoardType type, Cell cell) {
 		super(new GridLayout(type.baseDimension, type.baseDimension, 1, 1));
 		
 		this.type = type;
 		int i = type.baseDimension * type.baseDimension;
 		for (this.values = new int[i]; --i >= 0; values[i] = i);
 		
 		setBorder(BorderFactory.createLineBorder(Color.BLACK));
 		
 		Possibilities poss = cell.getPossibilities();
 		for (int value : values) {
 			JButton button = new JButton(type.format(value));
 			button.setEnabled(poss.contains(value));
 			button.addActionListener(new Listen(cell, value));
 			add(button);
 		}
 	}
 
 	@Override
 	public void cellChanged(Cell cell) {
 		Possibilities poss = cell.getPossibilities();
 		Component[] buttons = getComponents();
 		for (int value : values) {
 			buttons[value].setEnabled(poss.contains(value));
 		}
 	}
 
 	@Override
 	public void solved(Cell cell) {
//		cellChanged(cell);
 		removeAll();
 		GridLayout layout = (GridLayout) getLayout();
 		layout.setColumns(1);
 		layout.setRows(1);
 		
 		add(solved(cell.getValue()));
 		validate();
 	}
 
 	private Component solved(int value) {
 		JLabel label = new JLabel(type.format(value));
 		
 		int fontSize = label.getFont().getSize();
 		label.setFont(getFont().deriveFont(Font.BOLD, fontSize * 2));
 		
 		label.setHorizontalAlignment(JLabel.CENTER);
 		
 		return label;
 	}
 
 }
