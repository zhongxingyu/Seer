 package gsingh.learnkirtan.ui.shabadeditor.tableeditor;
 
 import gsingh.learnkirtan.ui.WindowTitleManager;
 import gsingh.learnkirtan.validation.Validator;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.util.EventObject;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.swing.Action;
 import javax.swing.JTable;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.table.TableCellEditor;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.text.JTextComponent;
 
 public class AlternatingRowColorTable extends JTable {
 	
 	private EditUndoManager undoManager;
 	
 	private Set<Point> invalidCells = new HashSet<Point>();
 	
 	private static final Font font = new Font("Arial", Font.PLAIN, 20);
 	
 	private boolean isSelectAllForMouseEvent = false;
 	private boolean isSelectAllForActionEvent = false;
 	private boolean isSelectAllForKeyEvent = false;
 
 	public AlternatingRowColorTable(int rows, int cols, WindowTitleManager titleManager) {
 		super(new UndoTableModel());
 		
 		undoManager = new EditUndoManager(titleManager);
 		
 		UndoTableModel model = (UndoTableModel) getModel();
 		model.addUndoableEditListener(undoManager);
 		model.setRowCount(rows);
 		model.setColumnCount(cols);
 
 		getInputMap().put(
 				KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK),
 				"undo");
 		getActionMap().put("undo", getUndoAction());
 
 		getInputMap().put(
 				KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK),
 				"redo");
 		getActionMap().put("redo", getRedoAction());
 	}
 
 	/*
 	 * Override to provide Select All editing functionality
 	 */
 	@Override
 	public boolean editCellAt(int row, int column, EventObject e) {
 		boolean result = super.editCellAt(row, column, e);
 
 		if (isSelectAllForMouseEvent || isSelectAllForActionEvent
 				|| isSelectAllForKeyEvent) {
 			selectAll(e);
 		}
 
 		return result;
 	}
 
 	/*
 	 * Select the text when editing on a text related cell is started
 	 */
 	private void selectAll(EventObject e) {
 		final Component editor = getEditorComponent();
 
 		if (editor == null || !(editor instanceof JTextComponent))
 			return;
 
 		if (e == null) {
 			((JTextComponent) editor).selectAll();
 			return;
 		}
 
 		// Typing in the cell was used to activate the editor
 
 		if (e instanceof KeyEvent && isSelectAllForKeyEvent) {
 			((JTextComponent) editor).selectAll();
 			return;
 		}
 
 		// F2 was used to activate the editor
 
 		if (e instanceof ActionEvent && isSelectAllForActionEvent) {
 			((JTextComponent) editor).selectAll();
 			return;
 		}
 
 		// A mouse click was used to activate the editor.
 		// Generally this is a double click and the second mouse click is
 		// passed to the editor which would remove the text selection unless
 		// we use the invokeLater()
 
 		if (e instanceof MouseEvent && isSelectAllForMouseEvent) {
 			SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					((JTextComponent) editor).selectAll();
 				}
 			});
 		}
 	}
 
 	//
 	// Newly added methods
 	//
 	/*
 	 * Sets the Select All property for for all event types
 	 */
 	public void setSelectAllForEdit(boolean isSelectAllForEdit) {
 		setSelectAllForMouseEvent(isSelectAllForEdit);
 		setSelectAllForActionEvent(isSelectAllForEdit);
 		setSelectAllForKeyEvent(isSelectAllForEdit);
 	}
 
 	/*
 	 * Set the Select All property when editing is invoked by the mouse
 	 */
 	public void setSelectAllForMouseEvent(boolean isSelectAllForMouseEvent) {
 		this.isSelectAllForMouseEvent = isSelectAllForMouseEvent;
 	}
 
 	/*
 	 * Set the Select All property when editing is invoked by the "F2" key
 	 */
 	public void setSelectAllForActionEvent(boolean isSelectAllForActionEvent) {
 		this.isSelectAllForActionEvent = isSelectAllForActionEvent;
 	}
 
 	/*
 	 * Set the Select All property when editing is invoked by typing directly
 	 * into the cell
 	 */
 	public void setSelectAllForKeyEvent(boolean isSelectAllForKeyEvent) {
 		this.isSelectAllForKeyEvent = isSelectAllForKeyEvent;
 	}
 
 	@Override
 	public Component prepareRenderer(TableCellRenderer renderer, int row,
 			int col) {
 		Component c = super.prepareRenderer(renderer, row, col);
 		if (!c.getBackground().equals(getSelectionBackground())) {
 			if (row % 2 == 1) {
 				if (isEnabled()) {
 					c.setBackground(Color.LIGHT_GRAY);
 				} else {
 					c.setBackground(Color.LIGHT_GRAY);
 				}
 			} else {
 				if (isEnabled()) {
 					c.setBackground(Color.WHITE);
 				} else {
 					c.setBackground(new Color(0xE5, 0xE5, 0xE5));
 				}
 			}
 		}
 
 		if (row % 2 == 1) {
 			String value = (String) getValueAt(row, col);
 			if (value != null && !value.equals("")) {
 				Point point = new Point(row, col);
 				if (!Validator.validate(value)) {
 					if (!c.getBackground().equals(getSelectionBackground())) {
 						c.setBackground(new Color(0xFF, 0x30, 0x30)); // Red
 					} else {
 						c.setBackground(new Color(0xFF, 0x70, 0x70)); // Light
 																		// Red
 					}
 					invalidCells.add(point);
 				} else {
 					if (invalidCells.contains(point)) {
 						invalidCells.remove(point);
 					}
 				}
 			}
 		}
 
 		return c;
 	}
 	
 	@Override
 	public Component prepareEditor(TableCellEditor editor, int row, int col) {
 		Component c = super.prepareEditor(editor, row, col);
 		c.setFont(font);
 		return c;
 	}
 
	public void reset() {
		undoManager.discardAllEdits();
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
