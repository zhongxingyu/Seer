 package org.cytoscape.browser.internal;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.KeyboardFocusManager;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.Window;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.StringSelection;
 import java.awt.datatransfer.Transferable;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.EventObject;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.BorderFactory;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 import javax.swing.JTable;
 import javax.swing.KeyStroke;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.TransferHandler;
 import javax.swing.border.Border;
 import javax.swing.border.TitledBorder;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableCellEditor;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableModel;
 import javax.swing.table.TableRowSorter;
 
 import org.cytoscape.application.CyApplicationManager;
 import org.cytoscape.browser.internal.util.TableBrowserUtil;
 import org.cytoscape.equations.EquationCompiler;
 import org.cytoscape.event.CyEventHelper;
 import org.cytoscape.model.CyColumn;
 import org.cytoscape.model.CyIdentifiable;
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.model.CyRow;
 import org.cytoscape.model.CyTable;
 import org.cytoscape.model.CyTableManager;
 import org.cytoscape.model.events.ColumnCreatedEvent;
 import org.cytoscape.model.events.ColumnCreatedListener;
 import org.cytoscape.model.events.ColumnDeletedEvent;
 import org.cytoscape.model.events.ColumnDeletedListener;
 import org.cytoscape.model.events.ColumnNameChangedEvent;
 import org.cytoscape.model.events.ColumnNameChangedListener;
 import org.cytoscape.model.events.RowSetRecord;
 import org.cytoscape.model.events.RowsSetEvent;
 import org.cytoscape.model.events.RowsSetListener;
 import org.cytoscape.view.model.CyNetworkView;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class BrowserTable extends JTable implements MouseListener, ActionListener, MouseMotionListener,
 													 ColumnCreatedListener, ColumnDeletedListener,
 													 ColumnNameChangedListener, RowsSetListener {
 
 	private static final long serialVersionUID = 4415856756184765301L;
 
 	private static final Logger logger = LoggerFactory.getLogger(BrowserTable.class);
 
 	private static final Font BORDER_FONT = new Font("Sans-serif", Font.BOLD, 12);
 
 	private static final TableCellRenderer cellRenderer = new BrowserTableCellRenderer();
 	private static final String MAC_OS_ID = "mac";
 
 	private Clipboard systemClipboard;
 	private CellEditorRemover editorRemover = null;
 	private final HashMap<String, Integer> columnWidthMap = new HashMap<String, Integer>();
 
 	// For right-click menu
 	private JPopupMenu rightClickPopupMenu;
 	private JPopupMenu rightClickHeaderPopupMenu;
 	private JMenuItem openFormulaBuilderMenuItem = null;
 
 	private final EquationCompiler compiler;
 	private final PopupMenuHelper popupMenuHelper;
 	private boolean updateColumnComparators;
 
 	private final CyApplicationManager applicationManager;
 	private final CyEventHelper eventHelper;
 	private final CyTableManager tableManager;
 
 	private JPopupMenu cellMenu;
 
 	private int sortedColumnIndex;
 	private boolean sortedColumnAscending;
 	
 	public BrowserTable(final EquationCompiler compiler, final PopupMenuHelper popupMenuHelper,
 			final CyApplicationManager applicationManager, final CyEventHelper eventHelper,
 			final CyTableManager tableManager) {
 		this.compiler = compiler;
 		this.popupMenuHelper = popupMenuHelper;
 		this.updateColumnComparators = false;
 		this.applicationManager = applicationManager;
 		this.eventHelper = eventHelper;
 		this.tableManager = tableManager;
 		this.sortedColumnAscending = true;
 		this.sortedColumnIndex = -1;
 		
 		initHeader();
 		setCellSelectionEnabled(true);
 		setDefaultEditor(Object.class, new MultiLineTableCellEditor());
 		getPopupMenu();
 		getHeaderPopupMenu();
 		setKeyStroke();
 		setTransferHandler(new BrowserTableTransferHandler());
 	}
 
 	public void setUpdateComparators(final boolean updateColumnComparators) {
 		this.updateColumnComparators = updateColumnComparators;
 	}
 
 	/**
 	 * Routine which determines if we are running on mac platform
 	 */
 	private boolean isMacPlatform() {
 		String os = System.getProperty("os.name");
 
 		return os.regionMatches(true, 0, MAC_OS_ID, 0, MAC_OS_ID.length());
 	}
 
 	protected void initHeader() {
 		this.setBackground(Color.white);
 
 		final JTableHeader header = getTableHeader();
 		header.addMouseMotionListener(this);
 		header.setBackground(Color.white);
 		header.setOpaque(false);
 		header.setDefaultRenderer(new CustomHeaderRenderer());
 		header.addMouseListener(this);
 		header.getColumnModel().setColumnSelectionAllowed(true);
 
 		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 
 		final BrowserTable table = this;
 
 		// Event handler. Define actions when mouse is clicked.
 		addMouseListener(new MouseAdapter() {
 
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				final int viewColumn = getColumnModel().getColumnIndexAtX(e.getX());
 				final int viewRow = e.getY() / getRowHeight();
 
 				final BrowserTableModel tableModel = (BrowserTableModel) table.getModel();
 
 				int modelColumn = convertColumnIndexToModel(viewColumn);
 				int modelRow = convertRowIndexToModel(viewRow);
 				
 				// Bail out if we're at the ID column:
 				if (tableModel.isPrimaryKey(modelColumn))
 					return;
 
 				// Make sure the column and row we're clicking on actually
 				// exists!
 				if (modelColumn >= tableModel.getColumnCount() || modelRow >= tableModel.getRowCount())
 					return;
 
 				// If action is right click, then show edit pop-up menu
 				if ((SwingUtilities.isRightMouseButton(e)) || (isMacPlatform() && e.isControlDown())) {
 					final CyColumn cyColumn = tableModel.getColumn(modelColumn);
 					final Object primaryKeyValue = ((ValidatedObjectAndEditString) tableModel.getValueAt(modelRow,
 							tableModel.getDataTable().getPrimaryKey().getName())).getValidatedObject();
 					popupMenuHelper.createTableCellMenu(cyColumn, primaryKeyValue, table, e.getX(), e.getY());
 				} else if (SwingUtilities.isLeftMouseButton(e) && (getSelectedRows().length != 0)) {
 					// Display List menu.
 					showListContents(modelRow, modelColumn, e);
 				}
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				SwingUtilities.invokeLater(new Runnable() {
 					@Override
 					public void run() {
 						selectFromTable();
 					}
 				});
 			}
 		});
 	}
 
 	private void selectFromTable() {
 
 		final TableModel model = this.getModel();
 		if (model instanceof BrowserTableModel == false)
 			return;
 
 		final BrowserTableModel btModel = (BrowserTableModel) model;
 
 		if (btModel.isShowAll() == false)
 			return;
 
 		final CyTable table = btModel.getDataTable();
 		final CyColumn pKey = table.getPrimaryKey();
 		final String pKeyName = pKey.getName();
 
 		final int[] rowsSelected = getSelectedRows();
 		if (rowsSelected.length == 0)
 			return;
 
 		final int selectedRowCount = getSelectedRowCount();
 
 		final Set<CyRow> targetRows = new HashSet<CyRow>();
 		for (int i = 0; i < selectedRowCount; i++) {
 			// getting the row from data table solves the problem with hidden or
 			// moved SUID column. However, since the rows might be sorted we
 			// need to convert the index to model
 			final ValidatedObjectAndEditString selected = (ValidatedObjectAndEditString) btModel.getValueAt(
 					convertRowIndexToModel(rowsSelected[i]), pKeyName);
 			targetRows.add(btModel.getRow(selected.getValidatedObject()));
 		}
 
 		// Clear selection for non-global table
 		if (tableManager.getGlobalTables().contains(table) == false) {
 			List<CyRow> allRows = btModel.getDataTable().getAllRows();
 			for (CyRow row : allRows) {
 				final Boolean val = row.get(CyNetwork.SELECTED, Boolean.class);
 				if (targetRows.contains(row)) {
 					row.set(CyNetwork.SELECTED, true);
 					continue;
 				}
 
 				if (val != null && (val == true))
 					row.set(CyNetwork.SELECTED, false);
 			}
 			
 			final CyNetworkView curView = applicationManager.getCurrentNetworkView();
 			if (curView != null) {
 				eventHelper.flushPayloadEvents();
 				curView.updateView();
 			}
 		}
 	}
 
 	private void setKeyStroke() {
 		final KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
 		// Identifying the copy KeyStroke user can modify this
 		// to copy on some other Key combination.
 		this.registerKeyboardAction(this, "Copy", copy, JComponent.WHEN_FOCUSED);
 		systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
 	}
 
 	@Override
 	public TableCellRenderer getCellRenderer(int row, int column) {
 		return cellRenderer;
 	}
 
 	@Override
 	public boolean isCellEditable(final int row, final int column) {
		return this.getModel().isCellEditable(row, column);
 	}
 
 	@Override
 	public boolean editCellAt(int row, int column, EventObject e) {
 		if (cellEditor != null && !cellEditor.stopCellEditing())
 			return false;
 
 		if (row < 0 || row >= getRowCount() || column < 0 || column >= getColumnCount())
 			return false;
 
 		if (!isCellEditable(row, column))
 			return false;
 
 		if (editorRemover == null) {
 			KeyboardFocusManager fm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
 			editorRemover = new CellEditorRemover(fm);
 			fm.addPropertyChangeListener("permanentFocusOwner", editorRemover);
 		}
 
 		TableCellEditor editor = getCellEditor(row, column);
 
 		// remember the table row, because tableModel will disappear if
 		// user click on open space on canvas, so we have to remember it before
 		// it is gone
 		BrowserTableModel model = (BrowserTableModel) this.getModel();
 		CyRow cyRow = model.getCyRow(convertRowIndexToModel(row));
 		String columnName = model.getColumnName(convertColumnIndexToModel(column));
 		editorRemover.setCellData(cyRow, columnName);
 
 		if ((editor != null) && editor.isCellEditable(e)) {
 			// Do this first so that the bounds of the JTextArea editor
 			// will be correct.
 			setEditingRow(row);
 			setEditingColumn(column);
 			setCellEditor(editor);
 			editor.addCellEditorListener(this);
 
 			editorComp = prepareEditor(editor, row, column);
 
 			if (editorComp == null) {
 				removeEditor();
 				return false;
 			}
 
 			Rectangle cellRect = getCellRect(row, column, false);
 
 			if (editor instanceof MultiLineTableCellEditor) {
 				Dimension prefSize = editorComp.getPreferredSize();
 				((JComponent) editorComp).putClientProperty(MultiLineTableCellEditor.UPDATE_BOUNDS, Boolean.TRUE);
 				editorComp.setBounds(cellRect.x, cellRect.y, Math.max(cellRect.width, prefSize.width),
 						Math.max(cellRect.height, prefSize.height));
 				((JComponent) editorComp).putClientProperty(MultiLineTableCellEditor.UPDATE_BOUNDS, Boolean.FALSE);
 			} else
 				editorComp.setBounds(cellRect);
 
 			add(editorComp);
 			editorComp.validate();
 
 			return true;
 		}
 
 		return false;
 	}
 
 	public void showListContents(int modelRow, int modelColumn, MouseEvent e) {
 		final BrowserTableModel model = (BrowserTableModel) getModel();
 		final Class<?> columnType = model.getColumn(modelColumn).getType();
 
 		if (columnType == List.class) {
 			final ValidatedObjectAndEditString value = (ValidatedObjectAndEditString) model.getValueAt(modelRow, modelColumn);
 
 			if (value != null) {
 				final List<?> list = (List<?>) value.getValidatedObject();
 				if (list != null && !list.isEmpty()) {
 					cellMenu = new JPopupMenu();
 					getCellContentView(List.class, list, "List Contains:", e);
 				}
 			}
 		}
 	}
 
 	private void getCellContentView(final Class<?> type, final List<?> listItems, final String borderTitle,
 			final MouseEvent e) {
 
 		JMenu curItem = null;
 		String dispName;
 
 		for (final Object item : listItems) {
 			dispName = item.toString();
 
 			if (dispName.length() > 60) {
 				dispName = dispName.substring(0, 59) + " ...";
 			}
 
 			curItem = new JMenu(dispName);
 			curItem.setBackground(Color.white);
 			curItem.add(getPopupMenu());
 
 			JMenuItem copyAll = new JMenuItem("Copy all");
 			copyAll.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 					final StringBuilder builder = new StringBuilder();
 					for (Object oneEntry : listItems)
 						builder.append(oneEntry.toString() + "\t");
 
 					final StringSelection selection = new StringSelection(builder.toString());
 					systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
 					systemClipboard.setContents(selection, selection);
 				}
 			});
 			curItem.add(copyAll);
 
 			JMenuItem copy = new JMenuItem("Copy one entry");
 			copy.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 					final StringSelection selection = new StringSelection(item.toString());
 					systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
 					systemClipboard.setContents(selection, selection);
 				}
 			});
 
 			curItem.add(copy);
 			curItem.add(popupMenuHelper.getOpenLinkMenu(dispName));
 			cellMenu.add(curItem);
 		}
 
 		final Border popupBorder = BorderFactory.createTitledBorder(null, borderTitle,
 				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, BORDER_FONT, Color.BLUE);
 		cellMenu.setBorder(popupBorder);
 		cellMenu.setBackground(Color.WHITE);
 		cellMenu.show(e.getComponent(), e.getX(), e.getY());
 	}
 
 	/**
 	 * This method initializes rightClickPopupMenu
 	 * 
 	 * @return the inilialised pop-up menu
 	 */
 	public JPopupMenu getPopupMenu() {
 		if (rightClickPopupMenu != null)
 			return rightClickPopupMenu;
 
 		rightClickPopupMenu = new JPopupMenu();
 
 		openFormulaBuilderMenuItem = new JMenuItem("Open Formula Builder");
 
 		final JTable table = this;
 		openFormulaBuilderMenuItem.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(final ActionEvent e) {
 				final int cellRow = table.getSelectedRow();
 				final int cellColumn = table.getSelectedColumn();
 				final BrowserTableModel tableModel = (BrowserTableModel) getModel();
 				final JFrame rootFrame = (JFrame) SwingUtilities.getRoot(table);
 				if (cellRow == -1 || cellColumn == -1 || !tableModel.isCellEditable(cellRow, cellColumn))
 					JOptionPane.showMessageDialog(rootFrame, "Can't enter a formula w/o a selected cell.",
 							"Information", JOptionPane.INFORMATION_MESSAGE);
 				else {
 					final String columnName = tableModel.getColumnName(cellColumn);
 					final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
 					FormulaBuilderDialog formulaBuilderDialog = new FormulaBuilderDialog(compiler, BrowserTable.this,
 							rootFrame, columnName);
 					formulaBuilderDialog.setLocationRelativeTo(rootFrame);
 					formulaBuilderDialog.setVisible(true);
 				}
 			}
 		});
 
 		return rightClickPopupMenu;
 	}
 
 	private JPopupMenu getHeaderPopupMenu() {
 		if (rightClickHeaderPopupMenu != null)
 			return rightClickHeaderPopupMenu;
 
 		rightClickHeaderPopupMenu = new JPopupMenu();
 
 		return rightClickHeaderPopupMenu;
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent event) {
 	}
 
 	@Override
 	public void mousePressed(MouseEvent event) {
 	}
 
 	@Override
 	public void mouseClicked(final MouseEvent event) {
 		
 		//*******************Sort header code **********************
 
 		final int cursorType = getTableHeader().getCursor().getType();
 		if ((event.getButton() == MouseEvent.BUTTON1) && (cursorType != Cursor.E_RESIZE_CURSOR)
 				&& (cursorType != Cursor.W_RESIZE_CURSOR)) {
 			final int index = getColumnModel().getColumnIndexAtX(event.getX());
 
 			if (index >= 0) {
 				final int modelIndex = getColumnModel().getColumn(index).getModelIndex();
 				if (sortedColumnIndex == index) {
 					sortedColumnAscending = !sortedColumnAscending;
 				}
 
 				sortedColumnIndex = index;
 			}
 		}//end of sorting
 		else if (event.getButton() == MouseEvent.BUTTON3) {
 			final int column = getColumnModel().getColumnIndexAtX(event.getX());
 			final BrowserTableModel tableModel = (BrowserTableModel) getModel();
 
 			// Make sure the column we're clicking on actually exists!
 			if (column >= tableModel.getColumnCount() || column < 0)
 				return;
 
 			// Ignore clicks on the ID column:
 			if (tableModel.isPrimaryKey(convertColumnIndexToModel(column)))
 				return;
 
 			final CyColumn cyColumn = tableModel.getColumn(convertColumnIndexToModel(column));
 			popupMenuHelper.createColumnHeaderMenu(cyColumn, this, event.getX(), event.getY());
 		}
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent event) {
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent event) {
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent event) {
 		if (event.getActionCommand().compareTo("Copy") == 0)
 			copyToClipBoard();
 	}
 
 	private String copyToClipBoard() {
 		final StringBuffer sbf = new StringBuffer();
 
 		/*
 		 * Check to ensure we have selected only a contiguous block of cells.
 		 */
 		final int numcols = this.getSelectedColumnCount();
 		final int numrows = this.getSelectedRowCount();
 
 		final int[] rowsselected = this.getSelectedRows();
 		final int[] colsselected = this.getSelectedColumns();
 
 		// Return if no cell is selected.
 		if (numcols == 0 && numrows == 0)
 			return null;
 
 		if (!((numrows - 1 == rowsselected[rowsselected.length - 1] - rowsselected[0] && numrows == rowsselected.length) && (numcols - 1 == colsselected[colsselected.length - 1]
 				- colsselected[0] && numcols == colsselected.length))) {
 			final JFrame rootFrame = (JFrame) SwingUtilities.getRoot(this);
 			JOptionPane.showMessageDialog(rootFrame, "Invalid Copy Selection", "Invalid Copy Selection",
 					JOptionPane.ERROR_MESSAGE);
 
 			return null;
 		}
 
 		for (int i = 0; i < numrows; i++) {
 			for (int j = 0; j < numcols; j++) {
 				final Object cellValue = this.getValueAt(rowsselected[i], colsselected[j]);
 				if (cellValue == null)
 					continue;
 
 				final String cellText = ((ValidatedObjectAndEditString) cellValue).getEditString();
 				sbf.append(cellText);
 
 				if (j < (numcols - 1))
 					sbf.append("\t");
 			}
 
 			sbf.append("\n");
 		}
 
 		final StringSelection selection = new StringSelection(sbf.toString());
 		systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
 		systemClipboard.setContents(selection, selection);
 
 		return sbf.toString();
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		// save the column width, if user adjust column width manually
 		if (e.getSource() instanceof JTableHeader) {
 			final int index = getColumnModel().getColumnIndexAtX(e.getX());
 			if (index != -1) {
 				int colWidth = getColumnModel().getColumn(index).getWidth();
 				this.columnWidthMap.put(this.getColumnName(index), new Integer(colWidth));
 			}
 		}
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 	}
 
 	private class CellEditorRemover implements PropertyChangeListener {
 		private final KeyboardFocusManager focusManager;
 		private BrowserTableModel model;
 		private int row = -1, column = -1;
 		private CyRow cyRow;
 		private String columnName;
 
 		public CellEditorRemover(final KeyboardFocusManager fm) {
 			this.focusManager = fm;
 		}
 
 		public void propertyChange(PropertyChangeEvent ev) {
 			if (!isEditing()) {
 				return;
 			}
 
 			Component c = focusManager.getPermanentFocusOwner();
 
 			while (c != null) {
 				if (c == BrowserTable.this) {
 					// focus remains inside the table
 					return;
 				} else if (c instanceof Window) {
 					if (c == SwingUtilities.getRoot(BrowserTable.this)) {
 
 						try {
 							getCellEditor().stopCellEditing();
 						} catch (Exception e) {
 							getCellEditor().cancelCellEditing();
 							// Update the cell data based on the remembered
 							// value
 							updateAttributeAfterCellLostFocus();
 						}
 					}
 
 					break;
 				}
 
 				c = c.getParent();
 			}
 		}
 
 		// Cell data passed from previous TableModel, because tableModel will
 		// disappear if
 		// user click on open space on canvas, so we have to remember it before
 		// it is gone
 		public void setCellData(CyRow row, String columnName) {
 			this.cyRow = row;
 			this.columnName = columnName;
 		}
 
 		private void updateAttributeAfterCellLostFocus() {
 			ArrayList parsedData = TableBrowserUtil.parseCellInput(cyRow.getTable(), columnName,
 					MultiLineTableCellEditor.lastValueUserEntered);
 
 			if (parsedData.get(0) != null) {
 				cyRow.set(columnName, MultiLineTableCellEditor.lastValueUserEntered);
 			} else {
 				// Error
 				// discard the change
 			}
 		}
 	}
 
 	public void addColumn(final TableColumn aColumn) {
 		super.addColumn(aColumn);
 
 		if (!updateColumnComparators)
 			return;
 
 		final TableRowSorter rowSorter = (TableRowSorter) getRowSorter();
 		if (rowSorter == null)
 			return;
 
 		final BrowserTableModel tableModel = (BrowserTableModel)getModel();
 		final Class<?> rowDataType = tableModel.getColumnByModelIndex(aColumn.getModelIndex()).getType();
 		rowSorter.setComparator(aColumn.getModelIndex(), new ValidatedObjectAndEditStringComparator(rowDataType));
 	}
 
 	@Override
 	public void paint(Graphics graphics) {
 		synchronized (getModel()) {
 			super.paint(graphics);
 		}
 	}
 	
 	private static class BrowserTableTransferHandler extends TransferHandler {
 		@Override
 		protected Transferable createTransferable(JComponent source) {
 			// Encode cell data in Excel format so we can copy/paste list
 			// attributes as multi-line cells.
 			StringBuilder builder = new StringBuilder();
 			BrowserTable table = (BrowserTable) source;
 			for (int rowIndex : table.getSelectedRows()) {
 				boolean firstColumn = true;
 				for (int columnIndex : table.getSelectedColumns()) {
 					if (!firstColumn) {
 						builder.append("\t");
 					} else {
 						firstColumn = false;
 					}
 					Object object = table.getValueAt(rowIndex, columnIndex);
 					if (object instanceof ValidatedObjectAndEditString) {
 						ValidatedObjectAndEditString raw = (ValidatedObjectAndEditString) object;
 						Object validatedObject = raw.getValidatedObject();
 						if (validatedObject instanceof Collection) {
 							builder.append("\"");
 							boolean firstRow = true;
 							for (Object member : (Collection<?>) validatedObject) {
 								if (!firstRow) {
 									builder.append("\r");
 								} else {
 									firstRow = false;
 								}
 								builder.append(member.toString().replaceAll("\"", "\"\""));
 							}
 							builder.append("\"");
 						} else {
 							builder.append(validatedObject.toString());
 						}
 					} else {
 						if (object != null) {
 							builder.append(object.toString());
 						}
 					}
 				}
 				builder.append("\n");
 			}
 			return new StringSelection(builder.toString());
 		}
 		
 		@Override
 		public int getSourceActions(JComponent c) {
 			return TransferHandler.COPY;
 		}
 	}
 	
 	
 	//*******************Sort header code **********************
 	public int getSortedColumnIndex() {
 		return sortedColumnIndex;
 	}
 
 	
 	public boolean isSortedColumnAscending() {
 		return sortedColumnAscending;
 	}
 
 	public void setVisibleAttributeNames(Collection<String> visibleAttributes) {
 		BrowserTableModel model = (BrowserTableModel) getModel();
 
 		BrowserTableColumnModel columnModel = (BrowserTableColumnModel) getColumnModel();
 		for (final String name : model.getAllAttributeNames()) {
 			int col = model.mapColumnNameToColumnIndex(name);
 			int convCol = convertColumnIndexToView(col);
 			TableColumn column = columnModel.getColumnByModelIndex(col);
 			columnModel.setColumnVisible(column, visibleAttributes.contains(name));
 		}
 		
 		//don't fire this, it will reset all the columns based on model
 		//fireTableStructureChanged();
 	}
 
 	public List<String> getVisibleAttributeNames() {
 		BrowserTableModel model = (BrowserTableModel) getModel();
 		final List<String> visibleAttrNames = new ArrayList<String>();		
 		for (final String name : model.getAllAttributeNames()) {
 			if (isColumnVisible(name))
 				visibleAttrNames.add(name);
 		}
 		
 		return visibleAttrNames;
 	}
 
 	public boolean isColumnVisible(String name) {
 		BrowserTableModel model = (BrowserTableModel) getModel();
 		BrowserTableColumnModel columnModel = (BrowserTableColumnModel) getColumnModel();
 		TableColumn column = columnModel.getColumnByModelIndex(model.mapColumnNameToColumnIndex(name));
 		return columnModel.isColumnVisible(column);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void handleEvent(final ColumnCreatedEvent e) {
 		BrowserTableModel model = (BrowserTableModel) getModel();
 		CyTable dataTable = model.getDataTable();
 		
 		if (e.getSource() != dataTable)
 			return;
 		BrowserTableColumnModel columnModel = (BrowserTableColumnModel) getColumnModel();
 
 		model.addColumn(e.getColumnName());
 		
 		int colIndex = columnModel.getColumnCount(false);
 		TableColumn newCol = new TableColumn(colIndex);
 		newCol.setHeaderValue(e.getColumnName());
 		setUpdateComparators(false);
 		addColumn(newCol);
 		final TableRowSorter<BrowserTableModel> rowSorter = new TableRowSorter<BrowserTableModel>(model);
 		setRowSorter(rowSorter);
 		updateColumnComparators(rowSorter, model);
 		setUpdateComparators(true);
 
 	}
 
 	void updateColumnComparators(final TableRowSorter<BrowserTableModel> rowSorter,
 			final BrowserTableModel browserTableModel) {
 		for (int column = 0; column < browserTableModel.getColumnCount(); ++column)
 			rowSorter.setComparator(
 					column,
 					new ValidatedObjectAndEditStringComparator(
 							browserTableModel.getColumnByModelIndex(column).getType()));
 	}
 
 	@Override
 	public void handleEvent(final ColumnDeletedEvent e) {
 		BrowserTableModel model = (BrowserTableModel) getModel();
 		CyTable dataTable = model.getDataTable();
 		
 		if (e.getSource() != dataTable)
 			return;
 		BrowserTableColumnModel columnModel = (BrowserTableColumnModel) getColumnModel();
 
 		final String columnName = e.getColumnName();
 		boolean columnFound = false;
 		int removedColIndex = -1;
 		
 		List<String> attrNames = model.getAllAttributeNames();
 		for (int i = 0; i < attrNames.size(); ++i) {
 			if (attrNames.get(i).equals(columnName)) {
 				removedColIndex = i;
 				columnModel.removeColumn (columnModel.getColumn(convertColumnIndexToView(i)));
 				columnFound = true;
 			}
 			else if (columnFound){ //need to push back the model indexes for all of the columns after this
 				
 				TableColumn nextCol = columnModel.getColumnByModelIndex(i); 
 				nextCol.setModelIndex(i- 1);
 			}
 		}
 		
 		if (removedColIndex != -1){//remove the item after the loop is done
 			model.removeColumn(removedColIndex);
 		}
 
 	}
 
 	@Override
 	public void handleEvent(final ColumnNameChangedEvent e) {
 		BrowserTableModel model = (BrowserTableModel) getModel();
 		CyTable dataTable = model.getDataTable();
 
 		if (e.getSource() != dataTable)
 			return;
 
 		final String newColumnName = e.getNewColumnName();
 		final int column = model.mapColumnNameToColumnIndex(e.getOldColumnName());
 		if (isColumnVisible(e.getOldColumnName())){
 			int colIndex = convertColumnIndexToView(column);
 			if (colIndex != -1)
 				getColumnModel().getColumn(colIndex).setHeaderValue(newColumnName);
 		}
 		
 		renameColumnName(e.getOldColumnName(), newColumnName);
 
 	}
 
 	private void renameColumnName(final String oldName, final String newName) {
 		BrowserTableColumnModel columnModel = (BrowserTableColumnModel) getColumnModel();
 		BrowserTableModel model = (BrowserTableModel) getModel();
 		
 		int index = model.mapColumnNameToColumnIndex(oldName);
 		if (index >= 0){
 			model.setColumnName(index, newName);
 			columnModel.getColumn(convertColumnIndexToView(index)).setHeaderValue(newName);
 			return;
 		}
 	
 		throw new IllegalStateException("The specified column " + oldName +" does not exist in the model.");
 	}
 
 	@Override
 	public void handleEvent(final RowsSetEvent e) {
 		BrowserTableModel model = (BrowserTableModel) getModel();
 		CyTable dataTable = model.getDataTable();
 
 		if (e.getSource() != dataTable)
 			return;		
 
 		if (!model.isShowAll()) {
 			model.clearSelectedRows();
 			boolean foundANonSelectedColumnName = false;
 			for (final RowSetRecord rowSet : e.getPayloadCollection()) {
 				if (!rowSet.getColumn().equals(CyNetwork.SELECTED)) {
 					foundANonSelectedColumnName = true;
 					break;
 				}
 			}
 			if (!foundANonSelectedColumnName) {
 				model.fireTableDataChanged();
 				return;
 			}
 		}
 
 		final Collection<RowSetRecord> rows = e.getPayloadCollection();
 
 		synchronized (this) {
 			if (!model.isShowAll()) {
 				model.fireTableDataChanged();
 			} else {
 				//table.clearSelection();
 				//fireTableDataChanged();
 				if(tableManager.getGlobalTables().contains(dataTable) == false)
 					bulkUpdate(rows);
 			}
 		}
 	}
 	
 	/**
 	 * Select rows in the table when something selected in the network view.
 	 * @param rows
 	 */
 	private void bulkUpdate(final Collection<RowSetRecord> rows) {
 		BrowserTableModel model = (BrowserTableModel) getModel();
 		CyTable dataTable = model.getDataTable();
 
 		final Map<Long, Boolean> suidMapSelected = new HashMap<Long, Boolean>();
 		final Map<Long, Boolean> suidMapUnselected = new HashMap<Long, Boolean>();
 
 		for(RowSetRecord rowSetRecord : rows) {
 			if(rowSetRecord.getColumn().equals(CyNetwork.SELECTED)){
 				if(((Boolean)rowSetRecord.getValue()) == true){
 					suidMapSelected.put(rowSetRecord.getRow().get(CyIdentifiable.SUID, Long.class), (Boolean) rowSetRecord.getValue());
 				}
 				else{
 					suidMapUnselected.put(rowSetRecord.getRow().get(CyIdentifiable.SUID, Long.class), (Boolean) rowSetRecord.getValue());
 				}
 			}
 		}
 
 		final String pKeyName = dataTable.getPrimaryKey().getName();
 		final int rowCount = getRowCount();
 		for(int i=0; i<rowCount; i++) {
 			//getting the row from data table solves the problem with hidden or moved SUID column. However, since the rows might be sorted we need to convert the index to model
 			int modelRow = convertRowIndexToModel(i);
 			final ValidatedObjectAndEditString tableKey = (ValidatedObjectAndEditString) model.getValueAt(modelRow, pKeyName );
 			Long pk = null;
 			try{
 				// TODO: Temp fix: is it a requirement that all CyTables have a Long SUID column as PK?
 				pk = Long.parseLong(tableKey.getEditString());
 			} catch (NumberFormatException nfe) {
 				System.out.println("Error parsing long from table " + getName() + ": " + nfe.getMessage());
 			}
 			if(pk != null) {
 				if (suidMapSelected.keySet().contains(pk)){
 					addRowSelectionInterval(i, i);
 				}else if (suidMapUnselected.keySet().contains(pk)){
 					removeRowSelectionInterval(i, i);
 				}
 			}
 		}
 	}
 }
