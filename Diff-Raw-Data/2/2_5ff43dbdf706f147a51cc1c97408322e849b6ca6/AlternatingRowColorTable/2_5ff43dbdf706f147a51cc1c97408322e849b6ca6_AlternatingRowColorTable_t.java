 package gsingh.learnkirtan.component.shabadeditor;
 
 import gsingh.learnkirtan.validation.Validator;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Point;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.swing.Action;
 import javax.swing.JTable;
 import javax.swing.table.TableCellRenderer;
 
 public class AlternatingRowColorTable extends JTable {
 	
 	private EditUndoManager undoManager;
 	
 	private Set<Point> invalidCells = new HashSet<Point>();
 	
 	public AlternatingRowColorTable(int rows, int cols) {
 		super(new UndoTableModel());
 		undoManager = new EditUndoManager();
 		UndoTableModel model = (UndoTableModel) getModel();
 		model.addUndoableEditListener(undoManager);
 		model.setRowCount(rows);
 		model.setColumnCount(cols);
 	}
 
 	@Override
 	public Component prepareRenderer(TableCellRenderer renderer, int row,
 			int col) {
 		Component c = super.prepareRenderer(renderer, row, col);
 		if (!c.getBackground().equals(getSelectionBackground())) {
 			if (row % 2 == 1) {
 				c.setBackground(Color.LIGHT_GRAY);
 			} else {
 				c.setBackground(Color.WHITE);
 			}
 		}
 
 		String value = (String) getValueAt(row, col);
		if (value != null && !value.equals("")) {
 			Point point = new Point(row, col);
 			if (!Validator.validate(value)) {
 				if (!c.getBackground().equals(getSelectionBackground())) {
 					c.setBackground(new Color(0xFF, 0x30, 0x30)); // Red
 				} else {
 					c.setBackground(new Color(0xFF, 0x70, 0x70)); // Light Red
 				}
 				invalidCells.add(point);
 			} else {
 				if (invalidCells.contains(point)) {
 					invalidCells.remove(point);
 				}
 			}
 		}
 
 		return c;
 	}
 	
 	public boolean isValidShabad() {
 		return invalidCells.isEmpty();
 	}
 
 	public Action getUndoAction() {
 		return undoManager.getUndoAction();
 	}
 
 	public Action getRedoAction() {
 		return undoManager.getRedoAction();
 	}
 }
