 package gsingh.learnkirtan.ui.shabadeditor.tableeditor;
 
 import gsingh.learnkirtan.FileManager;
 import gsingh.learnkirtan.note.Note;
 import gsingh.learnkirtan.note.Note.Length;
 import gsingh.learnkirtan.parser.Parser;
 import gsingh.learnkirtan.shabad.Shabad;
 import gsingh.learnkirtan.shabad.ShabadMetaData;
 import gsingh.learnkirtan.shabad.ShabadNotes;
 import gsingh.learnkirtan.ui.WindowTitleManager;
 import gsingh.learnkirtan.ui.action.ActionFactory;
 import gsingh.learnkirtan.ui.shabadeditor.SwingShabadEditor;
 import gsingh.learnkirtan.ui.shabadeditor.tableeditor.EditUndoManager.UndoEventListener;
 
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 
 @SuppressWarnings("serial")
 public class TableShabadEditor extends SwingShabadEditor implements
 		UndoEventListener {
 
 	private ShabadTable table;
 	private UndoTableModel model;
 
 	private EditUndoManager undoManager = new EditUndoManager();;
 
 	private ShabadMetaData metaData;
 
 	private WindowTitleManager titleManager;
 
 	private boolean metaModified = false;
 
 	private boolean repeating = false;
 
 	private int numRows = 32;
 	private int numCols = 16;
 
 	public TableShabadEditor(final WindowTitleManager titleManager,
 			FileManager fileManager) {
 		this.titleManager = titleManager;
 
 		undoManager.addUndoEventListener(this);
 
 		table = new ShabadTable(numRows, numCols, titleManager,
 				new ActionFactory(this, fileManager));
 		model = (UndoTableModel) table.getModel();
 		model.addUndoableEditListener(undoManager);
 		table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
 
 		setLayout(new GridLayout());
 		add(new JScrollPane(table));
 
 		model.addTableModelListener(new TableModelListener() {
 			@Override
 			public void tableChanged(TableModelEvent event) {
 				modified = true;
 				titleManager.setDocumentModifiedTitle();
 			}
 		});
 
 		metaData = new ShabadMetaData("", "", "", "", "");
 	}
 
 	public void setRepeating(boolean bool) {
 		repeating = bool;
 	}
 
 	public boolean getRepeating() {
 		return repeating;
 	}
 
 	public Action getUndoAction() {
 		return undoManager.getUndoAction();
 	}
 
 	public Action getRedoAction() {
 		return undoManager.getRedoAction();
 	}
 
 	public Action getCutAction() {
 		return table.new CutAction();
 	}
 
 	public Action getCopyAction() {
 		return table.new CopyAction();
 	}
 
 	public Action getPasteAction() {
 		return table.new PasteAction();
 	}
 
 	@Override
 	public Shabad getShabad() {
 		Shabad shabad = new Shabad(getNotesString(), getWords());
 		shabad.setMetaData(metaData);
 		return shabad;
 	}
 
 	@Override
 	public void setShabad(Shabad shabad) {
 		metaData = shabad.getMetaData();
 		setNotes(shabad.getNotes());
 		setWords(shabad.getWords());
 	}
 
 	private void setNotes(ShabadNotes notes) {
 		int numRows = model.getRowCount();

 		int index = 0;
 		for (int i = 1; i < numRows; i += 2) {
 			for (int j = 0; j < numCols; j++) {
 				if (index < notes.size()) {
 					Note note = notes.get(index);
 					if (note == null) {
 						table.setValueAt(null, i, j);
					} else if (note.getLength() == Length.LONG) {
 						table.setValueAt(note.getNoteText(), i, j);
 						if (j == 15) {
 							j = 0;
 							i += 2;
 						} else {
 							j++;
 						}
 						table.setValueAt("-", i, j);
 					} else if (note.getLength() == Length.HALF) {
 						Note note2 = notes.get(++index);
 						table.setValueAt(
 								note.getNoteText() + "-" + note2.getNoteText(),
 								i, j);
 					} else {
 						table.setValueAt(note.getNoteText(), i, j);
 					}
 					index++;
 				} else {
 					table.setValueAt(null, i, j);
 				}
 			}
 		}
 	}
 
 	@Override
 	public ShabadNotes getNotes() {
 		Parser parser = new Parser();
 		return parser.parse(getNotesString());
 	}
 
 	private String getNotesString() {
 		int firstCol, firstRow;
 		int colCount, rowCount;
 
 		if (repeating) {
 			firstCol = table.getSelectedColumn();
 			firstRow = table.getSelectedRow();
 			colCount = table.getSelectedColumnCount();
 			rowCount = table.getSelectedRowCount();
 		} else {
 			firstCol = 0;
 			firstRow = 0;
 			colCount = numCols;
 			rowCount = model.getRowCount();
 		}
 
 		// Only start on shabad rows, not wording rows
 		if (firstRow % 2 == 0) {
 			firstRow++;
 		}
 
 		StringBuilder sb = new StringBuilder();
 		for (int i = firstRow; i < firstRow + rowCount; i += 2) {
 			for (int j = firstCol; j < firstCol + colCount; j++) {
 				sb.append(model.getValueAt(i, j)).append(" ");
 			}
 		}
 
 		return sb.toString();
 	}
 
 	public List<String> getWords() {
 		List<String> l = new ArrayList<String>();
 		int numRows = model.getRowCount();
 		for (int i = 0; i < numRows; i += 2) {
 			for (int j = 0; j < numCols; j++) {
 				l.add((String) model.getValueAt(i, j));
 			}
 		}
 		return l;
 	}
 
 	public boolean isValidShabad() {
 		return table.isValidShabad();
 	}
 
 	public void setWords(List<String> words) {
 		int numRows = model.getRowCount();
 		for (int i = 0; i < numRows; i += 2) {
 			for (int j = 0; j < numCols; j++) {
 				int index = i / 2 * numCols + j;
 				String word;
 				if (index >= words.size())
 					word = "null";
 				else
 					word = words.get(index);
 				if (word != null && !word.equalsIgnoreCase("null")) {
 					table.setValueAt(word, i, j);
 				} else {
 					table.setValueAt(null, i, j);
 				}
 			}
 		}
 	}
 
 	private Action emptyAction = new AbstractAction() {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 		}
 	};
 
 	private void setUndoActions() {
 		if (undoManager.canRedo()) {
 			table.getActionMap().put("redo", getRedoAction());
 		} else {
 			table.getActionMap().put("redo", emptyAction);
 		}
 
 		if (undoManager.canUndo()) {
 			table.getActionMap().put("undo", getUndoAction());
 		} else {
 			table.getActionMap().put("undo", emptyAction);
 		}
 	}
 
 	@Override
 	public void setEditable(boolean bool) {
 		setEditable(bool);
 	}
 
 	@Override
 	public void reset() {
 		modified = false;
 		metaModified = false;
 		undoManager.discardAllEdits();
 	}
 
 	/**
 	 * Inserts a pair of rows into the table
 	 * 
 	 * @param row
 	 *            the row to insert the pair above
 	 */
 	public void addRowAbove(int row) {
 		if (row % 2 == 0) {
 			model.insertRow(row, new Object[] {});
 			model.insertRow(row, new Object[] {});
 		} else {
 			model.insertRow(row - 1, new Object[] {});
 			model.insertRow(row - 1, new Object[] {});
 		}
 	}
 
 	public void addRowBelow(int row) {
 		// TODO
 	}
 
 	/**
 	 * Deletes the specified row from the table. Also deletes the other row in
 	 * the pair
 	 * 
 	 * @param row
 	 *            the row number of the row to delete
 	 */
 	public void deleteRow(int row) {
 		model.removeRow(row);
 		if (row % 2 == 0) {
 			model.removeRow(row);
 		} else {
 			model.removeRow(row - 1);
 		}
 	}
 
 	@Override
 	public void setEnabled(boolean bool) {
 		table.setEnabled(bool);
 	}
 
 	public int getSelectedRow() {
 		return table.getSelectedRow();
 	}
 
 	@Override
 	public ShabadMetaData getMetaData() {
 		return metaData;
 	}
 
 	@Override
 	public void setMetaData(ShabadMetaData metaData) {
 		if (!this.metaData.equals(metaData)) {
 			modified = true;
 			metaModified = true;
 			titleManager.setDocumentModifiedTitle();
 		}
 		this.metaData = metaData;
 	}
 
 	@Override
 	public void undoEventOccurred() {
 		if (!undoManager.canUndo() && !metaModified) {
 			titleManager.setDocumentUnmodifiedTitle();
 			modified = false;
 			metaModified = false;
 		} else {
 			titleManager.setDocumentModifiedTitle();
 		}
 
 		setUndoActions();
 	}
 }
