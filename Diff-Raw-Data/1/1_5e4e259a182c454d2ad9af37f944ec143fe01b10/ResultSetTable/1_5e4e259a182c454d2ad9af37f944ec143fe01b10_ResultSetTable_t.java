 package net.argius.stew.ui.window;
 
 import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;
 import static java.awt.event.KeyEvent.*;
 import static java.awt.event.MouseEvent.MOUSE_DRAGGED;
 import static java.awt.event.MouseEvent.MOUSE_PRESSED;
 import static javax.swing.KeyStroke.getKeyStroke;
 import static net.argius.stew.ui.window.AnyActionKey.*;
 import static net.argius.stew.ui.window.ResultSetTable.ActionKey.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.beans.*;
 import java.io.*;
 import java.sql.*;
 import java.util.*;
 import java.util.List;
 
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.table.*;
 import javax.swing.text.*;
 
 import net.argius.stew.*;
 import net.argius.stew.io.*;
 import net.argius.stew.text.*;
 
 /**
  * Table for Result Set.
  */
 final class ResultSetTable extends JTable implements AnyActionListener, TextSearch {
 
     enum ActionKey {
         copyWithEscape,
         clearSelectedCellValue,
         setCurrentTimeValue,
         copyColumnName,
         findColumnName,
         addEmptyRow,
         insertFromClipboard,
         duplicateRows,
         linkRowsToDatabase,
         deleteRows,
         sort,
         jumpToColumn,
         doNothing,
     }
 
     static final String TAB = "\t";
 
     private static final Logger log = Logger.getLogger(ResultSetTable.class);
     private static final TableCellRenderer nullRenderer = new NullValueRenderer();
 
     private final AnyActionListener anyActionListener;
     private final ColumnHeaderCellRenderer columnHeaderRenderer;
     private final RowHeader rowHeader;
     private final Point mousePositionForColumnHeader = new Point();
 
     private int lastSortedIndex;
     private boolean lastSortedIsReverse;
     private String autoAdjustMode;
 
     // It is used by the process that has no key-event.
     private volatile KeyEvent lastKeyEvent;
 
     /**
      * Constructor.
      */
     ResultSetTable(AnyActionListener anyActionListener) {
         this.anyActionListener = anyActionListener;
         JTableHeader columnHeader = getTableHeader();
         TableCellRenderer columnHeaderDefaultRenderer = columnHeader.getDefaultRenderer();
         final RowHeader rowHeader = new RowHeader(this);
         this.columnHeaderRenderer = new ColumnHeaderCellRenderer(columnHeaderDefaultRenderer);
         this.rowHeader = rowHeader;
         setColumnSelectionAllowed(true);
         setAutoResizeMode(AUTO_RESIZE_OFF);
         columnHeader.setDefaultRenderer(columnHeaderRenderer);
         columnHeader.setReorderingAllowed(false);
         // [Events]
         // column header
         MouseInputListener colHeaderMouseListener = new ColumnHeaderMouseInputListener();
         columnHeader.addMouseListener(colHeaderMouseListener);
         columnHeader.addMouseMotionListener(colHeaderMouseListener);
         // row header
         MouseInputListener rowHeaderMouseListener = new RowHeaderMouseInputListener(rowHeader);
         rowHeader.addMouseListener(rowHeaderMouseListener);
         rowHeader.addMouseMotionListener(rowHeaderMouseListener);
         // cursor
         for (int i = 0; i < 2; i++) {
             final boolean withSelect = (i == 1);
             bindJumpAction("home", VK_HOME, withSelect);
             bindJumpAction("end", VK_END, withSelect);
             bindJumpAction("top", VK_UP, withSelect);
             bindJumpAction("bottom", VK_DOWN, withSelect);
             bindJumpAction("leftmost", VK_LEFT, withSelect);
             bindJumpAction("rightmost", VK_RIGHT, withSelect);
         }
         // key binds
         final int shortcutKey = Utilities.getMenuShortcutKeyMask();
         AnyAction aa = new AnyAction(this);
         aa.bindSelf(copyWithEscape, getKeyStroke(VK_C, shortcutKey | InputEvent.SHIFT_DOWN_MASK));
         aa.bindSelf(paste, getKeyStroke(VK_V, shortcutKey));
         aa.bindSelf(clearSelectedCellValue, getKeyStroke(VK_DELETE, 0));
        aa.bindSelf(deleteRows, getKeyStroke(VK_MINUS, shortcutKey | InputEvent.SHIFT_DOWN_MASK));
         aa.bindKeyStroke(true, adjustColumnWidth, getKeyStroke(VK_SLASH, shortcutKey));
         aa.bindKeyStroke(false, doNothing, getKeyStroke(VK_ESCAPE, 0));
     }
 
     private final class RowHeaderMouseInputListener extends MouseInputAdapter {
     
         @SuppressWarnings("hiding")
         private final RowHeader rowHeader;
         private int dragStartRow;
     
         RowHeaderMouseInputListener(RowHeader rowHeader) {
             this.rowHeader = rowHeader;
         }
     
         @Override
         public void mousePressed(MouseEvent e) {
             changeSelection(e);
         }
     
         @Override
         public void mouseDragged(MouseEvent e) {
             changeSelection(e);
         }
     
         private void changeSelection(MouseEvent e) {
             Point p = new Point(e.getX(), e.getY());
             if (SwingUtilities.isLeftMouseButton(e)) {
                 int id = e.getID();
                 boolean isMousePressed = (id == MOUSE_PRESSED);
                 boolean isMouseDragged = (id == MOUSE_DRAGGED);
                 if (isMousePressed || isMouseDragged) {
                     if (p.y >= rowHeader.getBounds().height) {
                         return;
                     }
                     if (!e.isControlDown() && !e.isShiftDown()) {
                         clearSelection();
                     }
                     int rowIndex = rowAtPoint(p);
                     if (rowIndex < 0 || getRowCount() < rowIndex) {
                         return;
                     }
                     final int index0;
                     final int index1;
                     if (isMousePressed) {
                         if (e.isShiftDown()) {
                             index0 = dragStartRow;
                             index1 = rowIndex;
                         } else {
                             dragStartRow = rowIndex;
                             index0 = rowIndex;
                             index1 = rowIndex;
                         }
                     } else if (isMouseDragged) {
                         index0 = dragStartRow;
                         index1 = rowIndex;
                     } else {
                         return;
                     }
                     addRowSelectionInterval(index0, index1);
                     addColumnSelectionInterval(getColumnCount() - 1, 0);
                     requestFocus();
                     // justify a position between table and its row header
                     JViewport tableView = (JViewport)getParent();
                     Point viewPosition = tableView.getViewPosition();
                     viewPosition.y = ((JViewport)rowHeader.getParent()).getViewPosition().y;
                     tableView.setViewPosition(viewPosition);
                 }
             }
         }
     }
 
     private final class ColumnHeaderMouseInputListener extends MouseInputAdapter {
     
         private int dragStartColumn;
     
         ColumnHeaderMouseInputListener() {
         } // empty
 
         @SuppressWarnings("synthetic-access")
         @Override
         public void mousePressed(MouseEvent e) {
             if (SwingUtilities.isLeftMouseButton(e)) {
                 changeSelection(e);
             }
             mousePositionForColumnHeader.setLocation(e.getPoint());
         }
     
         @Override
         public void mouseDragged(MouseEvent e) {
             if (SwingUtilities.isLeftMouseButton(e)) {
                 changeSelection(e);
             }
         }
     
         private void changeSelection(MouseEvent e) {
             final Point p = e.getPoint();
             int id = e.getID();
             boolean isMousePressed = (id == MOUSE_PRESSED);
             boolean isMouseDragged = (id == MOUSE_DRAGGED);
             if (isMousePressed || isMouseDragged) {
                 if (!e.isControlDown() && !e.isShiftDown()) {
                     clearSelection();
                 }
                 int columnIndex = columnAtPoint(p);
                 if (columnIndex < 0 || getColumnCount() <= columnIndex) {
                     return;
                 }
                 final int index0;
                 final int index1;
                 if (isMousePressed) {
                     if (e.isShiftDown()) {
                         index0 = dragStartColumn;
                         index1 = columnIndex;
                     } else {
                         dragStartColumn = columnIndex;
                         index0 = columnIndex;
                         index1 = columnIndex;
                     }
                 } else if (isMouseDragged) {
                     index0 = dragStartColumn;
                     index1 = columnIndex;
                 } else {
                     return;
                 }
                 selectColumn(index0, index1);
                 requestFocus();
             }
         }
     }
 
     @Override
     protected void processKeyEvent(KeyEvent e) {
         super.processKeyEvent(e);
         lastKeyEvent = e;
     }
 
     @Override
     public void anyActionPerformed(AnyActionEvent ev) {
         try {
             processAnyActionEvent(ev);
         } catch (Exception ex) {
             WindowOutputProcessor.showErrorDialog(this, ex);
         }
     }
 
     public void processAnyActionEvent(AnyActionEvent ev) throws Exception {
         if (ev.isAnyOf(copy, selectAll)) {
             final String cmd = ev.getActionCommand();
             getActionMap().get(cmd).actionPerformed(new ActionEvent(this, 0, cmd));
         } else if (ev.isAnyOf(copyWithEscape)) {
             List<String> rows = new ArrayList<String>();
             for (int rowIndex : getSelectedRows()) {
                 List<Object> row = new ArrayList<Object>();
                 for (int columnIndex : getSelectedColumns()) {
                     final Object o = getValueAt(rowIndex, columnIndex);
                     row.add(CsvFormatter.AUTO.format(o == null ? "" : String.valueOf(o)));
                 }
                 rows.add(TextUtilities.join(TAB, row));
             }
             ClipboardHelper.setStrings(rows);
         } else if (ev.isAnyOf(paste)) {
             try {
                 InputStream is = new ByteArrayInputStream(ClipboardHelper.getString().getBytes());
                 Importer importer = new SmartImporter(is, TAB);
                 try {
                     int[] selectedColumns = getSelectedColumns();
                     for (int rowIndex : getSelectedRows()) {
                         Object[] values = importer.nextRow();
                         final int limit = Math.min(selectedColumns.length, values.length);
                         for (int x = 0; x < limit; x++) {
                             setValueAt(values[x], rowIndex, selectedColumns[x]);
                         }
                     }
                 } finally {
                     importer.close();
                 }
                 repaint();
             } finally {
                 editingCanceled(new ChangeEvent(ev.getSource()));
             }
         } else if (ev.isAnyOf(clearSelectedCellValue)) {
             try {
                 setValueAtSelectedCells(null);
                 repaint();
             } finally {
                 editingCanceled(new ChangeEvent(ev.getSource()));
             }
         } else if (ev.isAnyOf(setCurrentTimeValue)) {
             try {
                 setValueAtSelectedCells(new Timestamp(System.currentTimeMillis()));
                 repaint();
             } finally {
                 editingCanceled(new ChangeEvent(ev.getSource()));
             }
         } else if (ev.isAnyOf(copyColumnName)) {
             List<String> a = new ArrayList<String>();
             ResultSetTableModel m = getResultSetTableModel();
             if (ev.getModifiers() == 0) {
                 for (int i = 0, n = m.getColumnCount(); i < n; i++) {
                     a.add(m.getColumnName(i));
                 }
             } else {
                 for (final int i : getSelectedColumns()) {
                     a.add(m.getColumnName(i));
                 }   
             }
             ClipboardHelper.setString(TextUtilities.join(TAB, a));
         } else if (ev.isAnyOf(findColumnName)) {
             anyActionListener.anyActionPerformed(ev);
         } else if (ev.isAnyOf(addEmptyRow)) {
             ResultSetTableModel m = getResultSetTableModel();
             int[] selectedRows = getSelectedRows();
             if (selectedRows.length > 0) {
                 final int nextRow = selectedRows[selectedRows.length - 1] + 1;
                 for (int i = 0; i < selectedRows.length; i++) {
                     m.insertUnlinkedRow(nextRow, new Object[m.getColumnCount()]);
                 }
             } else {
                 m.addUnlinkedRow(new Object[m.getColumnCount()]);
             }
         } else if (ev.isAnyOf(insertFromClipboard)) {
             try {
                 Importer importer = new SmartImporter(ClipboardHelper.getReaderForText(), TAB);
                 try {
                     ResultSetTableModel m = getResultSetTableModel();
                     while (true) {
                         Object[] row = importer.nextRow();
                         if (row.length == 0) {
                             break;
                         }
                         m.addUnlinkedRow(row);
                         m.linkRow(m.getRowCount() - 1);
                     }
                     repaintRowHeader("model");
                 } finally {
                     importer.close();
                 }
             } finally {
                 editingCanceled(new ChangeEvent(ev.getSource()));
             }
         } else if (ev.isAnyOf(duplicateRows)) {
             ResultSetTableModel m = getResultSetTableModel();
             List<?> rows = m.getDataVector();
             int[] selectedRows = getSelectedRows();
             int index = selectedRows[selectedRows.length - 1];
             for (int rowIndex : selectedRows) {
                 m.insertUnlinkedRow(++index, (Vector<?>)((Vector<?>)rows.get(rowIndex)).clone());
             }
             repaint();
             repaintRowHeader("model");
         } else if (ev.isAnyOf(linkRowsToDatabase)) {
             ResultSetTableModel m = getResultSetTableModel();
             try {
                 for (int rowIndex : getSelectedRows()) {
                     m.linkRow(rowIndex);
                 }
             } finally {
                 repaintRowHeader("unlinkedRowStatus");
             }
         } else if (ev.isAnyOf(deleteRows)) {
             try {
                 ResultSetTableModel m = getResultSetTableModel();
                 while (true) {
                     final int selectedRow = getSelectedRow();
                     if (selectedRow < 0) {
                         break;
                     }
                     if (m.isLinkedRow(selectedRow)) {
                         final boolean removed = m.removeLinkedRow(selectedRow);
                         assert removed;
                     } else {
                         m.removeRow(selectedRow);
                     }
                 }
             } finally {
                 repaintRowHeader("model");
             }
         } else if (ev.isAnyOf(adjustColumnWidth)) {
             adjustColumnWidth();
         } else if (ev.isAnyOf(widenColumnWidth)) {
             changeTableColumnWidth(1.5f);
         } else if (ev.isAnyOf(narrowColumnWidth)) {
             changeTableColumnWidth(1 / 1.5f);
         } else if (ev.isAnyOf(sort)) {
             doSort(getTableHeader().columnAtPoint(mousePositionForColumnHeader));
         } else if (ev.isAnyOf(jumpToColumn)) {
             Object[] args = ev.getArgs();
             if (args != null && args.length > 0) {
                 jumpToColumn(String.valueOf(args[0]));
             }
         } else if (ev.isAnyOf(showColumnNumber)) {
             setShowColumnNumber(!columnHeaderRenderer.fixesColumnNumber);
             updateUI();
         } else {
             log.warn("not expected: Event=%s", ev);
         }
     }
 
     @Override
     public void editingStopped(ChangeEvent e) {
         try {
             super.editingStopped(e);
         } catch (Exception ex) {
             WindowOutputProcessor.showErrorDialog(getParent(), ex);
         }
     }
 
     @Override
     public boolean editCellAt(int row, int column, EventObject e) {
         boolean succeeded = super.editCellAt(row, column, e);
         if (succeeded) {
             if (editorComp instanceof JTextField) {
                 // make it selected when starting edit-mode
                 if (lastKeyEvent != null && lastKeyEvent.getKeyCode() != VK_F2) {
                     JTextField editor = (JTextField)editorComp;
                     initializeEditorComponent(editor);
                     editor.requestFocus();
                     editor.selectAll();
                 }
             }
         }
         return succeeded;
     }
 
     @Override
     public TableCellEditor getCellEditor() {
         TableCellEditor editor = super.getCellEditor();
         if (editor instanceof DefaultCellEditor) {
             DefaultCellEditor d = (DefaultCellEditor)editor;
             initializeEditorComponent(d.getComponent());
         }
         return editor;
     }
 
     private void initializeEditorComponent(Component c) {
         final Color bgColor = Color.ORANGE;
         if (c != null && c.getBackground() != bgColor) {
             // determines initialized state by bgcolor
             if (!c.isEnabled()) {
                 c.setEnabled(true);
             }
             c.setFont(getFont());
             c.setBackground(bgColor);
             if (c instanceof JTextComponent) {
                 final JTextComponent text = (JTextComponent)c;
                 AnyAction aa = new AnyAction(text);
                 aa.setUndoAction();
                 c.addFocusListener(new FocusAdapter() {
                     @Override
                     public void focusLost(FocusEvent e) {
                         editingCanceled(new ChangeEvent(e.getSource()));
                     }
                 });
             }
         }
     }
 
     @Override
     public TableCellRenderer getCellRenderer(int row, int column) {
         final Object v = getValueAt(row, column);
         if (v == null) {
             return nullRenderer;
         }
         return super.getCellRenderer(row, column);
     }
 
     @Override
     public void updateUI() {
         super.updateUI();
         adjustRowHeight(this);
     }
 
     static void adjustRowHeight(JTable table) {
         Component c = new JLabel("0");
         final int height = c.getPreferredSize().height;
         if (height > 0) {
             table.setRowHeight(height);
         }
     }
 
     @Override
     protected void configureEnclosingScrollPane() {
         super.configureEnclosingScrollPane();
         Container p = getParent();
         if (p instanceof JViewport) {
             Container gp = p.getParent();
             if (gp instanceof JScrollPane) {
                 JScrollPane scrollPane = (JScrollPane)gp;
                 JViewport viewport = scrollPane.getViewport();
                 if (viewport == null || viewport.getView() != this) {
                     return;
                 }
                 scrollPane.setRowHeaderView(rowHeader);
             }
         }
     }
 
     @Override
     public void setModel(TableModel dataModel) {
         dataModel.addTableModelListener(rowHeader);
         super.setModel(dataModel);
     }
 
     /**
      * Selects colomns.
      * @param index0 index of start
      * @param index1 index of end
      */
     void selectColumn(int index0, int index1) {
         if (getRowCount() > 0) {
             addColumnSelectionInterval(index0, index1);
             addRowSelectionInterval(getRowCount() - 1, 0);
         }
     }
 
     /**
      * Jumps to specified column.
      * @param index
      * @return whether the column exists or not
      */
     boolean jumpToColumn(int index) {
         final int columnCount = getColumnCount();
         if (0 <= index && index < columnCount) {
             if (getSelectedRowCount() == 0) {
                 changeSelection(-1, index, false, false);
             } else {
                 int[] selectedRows = getSelectedRows();
                 changeSelection(getSelectedRow(), index, false, false);
                 for (final int selectedRow : selectedRows) {
                     changeSelection(selectedRow, index, false, true);
                 }
             }
             return true;
         }
         return false;
     }
 
     /**
      * Jumps to specified column.
      * @param name
      * @return whether the column exists or not
      */
     boolean jumpToColumn(String name) {
         TableColumnModel columnModel = getColumnModel();
         for (int i = 0, n = columnModel.getColumnCount(); i < n; i++) {
             if (name.equals(String.valueOf(columnModel.getColumn(i).getHeaderValue()))) {
                 jumpToColumn(i);
                 return true;
             }
         }
         return false;
     }
 
     RowHeader getRowHeader() {
         return rowHeader;
     }
 
     ResultSetTableModel getResultSetTableModel() {
         return (ResultSetTableModel)getModel();
     }
 
     boolean isShowColumnNumber() {
         return columnHeaderRenderer.fixesColumnNumber;
     }
 
     void setShowColumnNumber(boolean showColumnNumber) {
         final boolean oldValue = this.columnHeaderRenderer.fixesColumnNumber;
         this.columnHeaderRenderer.fixesColumnNumber = showColumnNumber;
         firePropertyChange("showNumber", oldValue, showColumnNumber);
     }
 
     void repaintRowHeader(String propName) {
         if (rowHeader != null) {
             rowHeader.propertyChange(new PropertyChangeEvent(this, propName, null, null));
         }
     }
 
     String getAutoAdjustMode() {
         return autoAdjustMode;
     }
 
     void setAutoAdjustMode(String autoAdjustMode) {
         final String oldValue = this.autoAdjustMode;
         this.autoAdjustMode = autoAdjustMode;
         firePropertyChange("autoAdjustMode", oldValue, autoAdjustMode);
     }
 
     private void bindJumpAction(String suffix, int key, boolean withSelect) {
         final String actionKey = String.format("%s-to-%s", (withSelect) ? "select" : "jump", suffix);
         final CellCursor c = new CellCursor(this, withSelect);
         final int modifiers = Utilities.getMenuShortcutKeyMask()
                               | (withSelect ? SHIFT_DOWN_MASK : 0);
         KeyStroke[] keyStrokes = {getKeyStroke(key, modifiers)};
         c.putValue(Action.ACTION_COMMAND_KEY, actionKey);
         InputMap im = this.getInputMap();
         for (KeyStroke ks : keyStrokes) {
             im.put(ks, actionKey);
         }
         this.getActionMap().put(actionKey, c);
     }
 
     private static final class CellCursor extends AbstractAction {
 
         private final JTable table;
         private final boolean extend;
 
         CellCursor(JTable table, boolean extend) {
             this.table = table;
             this.extend = extend;
         }
 
         int getColumnPosition() {
             int[] a = table.getSelectedColumns();
             if (a == null || a.length == 0) {
                 return -1;
             }
             ListSelectionModel csm = table.getColumnModel().getSelectionModel();
             return (a[0] == csm.getAnchorSelectionIndex()) ? a[a.length - 1] : a[0];
         }
 
         int getRowPosition() {
             int[] a = table.getSelectedRows();
             if (a == null || a.length == 0) {
                 return -1;
             }
             ListSelectionModel rsm = table.getSelectionModel();
             return (a[0] == rsm.getAnchorSelectionIndex()) ? a[a.length - 1] : a[0];
         }
 
         @Override
         public void actionPerformed(ActionEvent e) {
             final String cmd = e.getActionCommand();
             final int ri;
             final int ci;
             if (cmd == null) {
                 assert false : "command is null";
                 return;
             } else if (cmd.endsWith("-to-home")) {
                 ri = 0;
                 ci = 0;
             } else if (cmd.endsWith("-to-end")) {
                 ri = table.getRowCount() - 1;
                 ci = table.getColumnCount() - 1;
             } else if (cmd.endsWith("-to-top")) {
                 ri = 0;
                 ci = getColumnPosition();
             } else if (cmd.endsWith("-to-bottom")) {
                 ri = table.getRowCount() - 1;
                 ci = getColumnPosition();
             } else if (cmd.endsWith("-to-leftmost")) {
                 ri = getRowPosition();
                 ci = 0;
             } else if (cmd.endsWith("-to-rightmost")) {
                 ri = getRowPosition();
                 ci = table.getColumnCount() - 1;
             } else {
                 assert false : "unknown command: " + cmd;
                 return;
             }
             table.changeSelection(ri, ci, false, extend);
         }
         
     }
 
     private static final class NullValueRenderer extends DefaultTableCellRenderer {
 
         NullValueRenderer() {
         } // empty
 
         @Override
         public Component getTableCellRendererComponent(JTable table,
                                                        Object value,
                                                        boolean isSelected,
                                                        boolean hasFocus,
                                                        int row,
                                                        int column) {
             Component c = super.getTableCellRendererComponent(table,
                                                               "NULL",
                                                               isSelected,
                                                               hasFocus,
                                                               row,
                                                               column);
             c.setForeground(new Color(63, 63, 192, 192));
             Font font = c.getFont();
             c.setFont(font.deriveFont(font.getSize() * 0.8f));
             return c;
         }
 
     }
 
     private static final class ColumnHeaderCellRenderer implements TableCellRenderer {
 
         TableCellRenderer renderer;
         boolean fixesColumnNumber;
 
         ColumnHeaderCellRenderer(TableCellRenderer renderer) {
             this.renderer = renderer;
             this.fixesColumnNumber = false;
         }
 
         @Override
         public Component getTableCellRendererComponent(JTable table,
                                                        Object value,
                                                        boolean isSelected,
                                                        boolean hasFocus,
                                                        int row,
                                                        int column) {
             Object o = fixesColumnNumber ? String.format("%d %s", column + 1, value) : value;
             return renderer.getTableCellRendererComponent(table,
                                                           o,
                                                           isSelected,
                                                           hasFocus,
                                                           row,
                                                           column);
         }
 
     }
 
     private static final class RowHeader extends JTable implements PropertyChangeListener {
 
         private static final int DEFAULT_WIDTH = 40;
 
         private DefaultTableModel model;
         private TableCellRenderer renderer;
 
         RowHeader(JTable table) {
             this.model = new DefaultTableModel(0, 1) {
                 @Override
                 public boolean isCellEditable(int row, int column) {
                     return false;
                 }
             };
             final ResultSetTable t = (ResultSetTable)table;
             this.renderer = new DefaultTableCellRenderer() {
                 @Override
                 public Component getTableCellRendererComponent(JTable table,
                                                                Object value,
                                                                boolean isSelected,
                                                                boolean hasFocus,
                                                                int row,
                                                                int column) {
                     assert t.getModel() instanceof ResultSetTableModel;
                     final boolean rowLinked = t.getResultSetTableModel().isLinkedRow(row);
                     JLabel label = new JLabel(String.format("%s ", rowLinked ? row + 1 : "+"));
                     label.setHorizontalAlignment(RIGHT);
                     label.setFont(table.getFont());
                     label.setOpaque(true);
                     return label;
                 }
             };
             setModel(model);
             setWidth(table);
             setFocusable(false);
             table.addPropertyChangeListener(this);
         }
 
         @Override
         public void propertyChange(PropertyChangeEvent e) {
             JTable table = (JTable)e.getSource();
             if (table == null) {
                 return;
             }
             String propertyName = e.getPropertyName();
             if (propertyName.equals("enabled")) {
                 boolean isEnabled = table.isEnabled();
                 setVisible(isEnabled);
                 if (isEnabled) {
                     setWidth(table);
                     resetViewPosition(table);
                 }
             } else if (propertyName.equals("font")) {
                 setFont(table.getFont());
             } else if (propertyName.equals("rowHeight")) {
                 setRowHeight(table.getRowHeight());
             } else if (propertyName.equals("model")) {
                 model.setRowCount(table.getRowCount());
             } else if (propertyName.equals("unlinkedRowStatus")) {
                 repaint();
             } else if (propertyName.equals("ancestor")) {
                 // empty
             } else {
                 // empty
             }
             validate();
         }
 
         @Override
         public void tableChanged(TableModelEvent e) {
             Object src = e.getSource();
             if (model != null && src != null) {
                 model.setRowCount(((TableModel)src).getRowCount());
             }
             super.tableChanged(e);
         }
 
         @Override
         public void updateUI() {
             super.updateUI();
             adjustRowHeight(this);
         }
 
         void setWidth(JTable table) {
             // XXX unstable
             final int rowCount = table.getRowCount();
             model.setRowCount(rowCount);
             JLabel label = new JLabel(String.valueOf(rowCount * 1000L));
             Dimension d = getSize();
             d.width = Math.max(label.getPreferredSize().width, DEFAULT_WIDTH);
             setPreferredScrollableViewportSize(d);
         }
 
         private void resetViewPosition(JTable table) {
             // forces to reset its view position to table's view position
             Container p1 = table.getParent();
             Container p2 = getParent();
             if (p1 instanceof JViewport && p2 instanceof JViewport) {
                 JViewport v1 = (JViewport)p1;
                 JViewport v2 = (JViewport)p2;
                 v2.setViewPosition(v1.getViewPosition());
             }
         }
 
         @Override
         public boolean isCellEditable(int row, int column) {
             return false;
         }
 
         @Override
         public TableCellRenderer getCellRenderer(int row, int column) {
             return renderer;
         }
 
     }
 
     // text search
 
     @Override
     public boolean search(Matcher matcher) {
         final int rowCount = getRowCount();
         if (rowCount <= 0) {
             return false;
         }
         final int columnCount = getColumnCount();
         final boolean backward = matcher.isBackward();
         final int amount = backward ? -1 : 1;
         final int rowStart = backward ? rowCount - 1 : 0;
         final int rowEnd = backward ? 0 : rowCount - 1;
         final int columnStart = backward ? columnCount - 1 : 0;
         final int columnEnd = backward ? 0 : columnCount - 1;
         int row = rowStart;
         int column = columnStart;
         if (getSelectedColumnCount() > 0) {
             column = getSelectedColumn();
             row = getSelectedRow() + amount;
             if (backward) {
                 if (row < 0) {
                     --column;
                     if (column < 0) {
                         return false;
                     }
                     row = rowStart;
                 }
             } else {
                 if (row >= rowCount) {
                     ++column;
                     if (column >= columnCount) {
                         return false;
                     }
                     row = rowStart;
                 }
             }
         }
         final TableModel m = getModel();
         for (; backward ? column >= columnEnd : column <= columnEnd; column += amount) {
             for (; backward ? row >= rowEnd : row <= rowEnd; row += amount) {
                 if (matcher.find(String.valueOf(m.getValueAt(row, column)))) {
                     changeSelection(row, column, false, false);
                     return true;
                 }
             }
             row = rowStart;
         }
         return false;
     }
 
     @Override
     public void reset() {
         // empty
     }
 
     static final class TableHeaderTextSearch implements TextSearch {
         private ResultSetTable rstable;
         private JTableHeader tableHeader;
         TableHeaderTextSearch(ResultSetTable rstable, JTableHeader tableHeader) {
             this.rstable = rstable;
             this.tableHeader = tableHeader;
         }
         @Override
         public boolean search(Matcher matcher) {
             TableColumnModel m = tableHeader.getColumnModel();
             int columnCount = m.getColumnCount();
             if (columnCount < 1) {
                 return false;
             }
             final boolean backward = matcher.isBackward();
             final int amount = backward ? -1 : 1;
             final int columnStart = backward ? columnCount - 1 : 0;
             final int columnEnd = backward ? 0 : columnCount - 1;
             int column = columnStart;
             if (rstable.getSelectedColumnCount() > 0) {
                 column = rstable.getSelectedColumn() + amount;
             }
             for (; backward ? column >= columnEnd : column <= columnEnd; column += amount) {
                 if (matcher.find(String.valueOf(m.getColumn(column).getHeaderValue()))) {
                     rstable.jumpToColumn(column);
                     return true;
                 }
             }
             return false;
         }
         @Override
         public void reset() {
             // empty
         }
     }
 
     // event-handlers
 
     private void setValueAtSelectedCells(Object value) {
         int[] selectedColumns = getSelectedColumns();
         for (int rowIndex : getSelectedRows()) {
             for (int colIndex : selectedColumns) {
                 setValueAt(value, rowIndex, colIndex);
             }
         }
     }
 
     private void adjustColumnWidth() {
         final int rowCount = getRowCount();
         final boolean byHeader;
         final boolean byValue;
         switch (AnyActionKey.of(autoAdjustMode)) {
             case autoAdjustModeNone:
                 byHeader = false;
                 byValue = false;
                 break;
             case autoAdjustModeHeader:
                 byHeader = true;
                 byValue = false;
                 break;
             case autoAdjustModeValue:
                 byHeader = false;
                 byValue = true;
                 break;
             case autoAdjustModeHeaderAndValue:
                 byHeader = true;
                 byValue = true;
                 break;
             default:
                 log.warn("autoAdjustMode=%s", autoAdjustMode);
                 return;
         }
         if (!byHeader && rowCount == 0) {
             return;
         }
         final float max = getParent().getWidth() * 0.8f;
         TableColumnModel columnModel = getColumnModel();
         JTableHeader header = getTableHeader();
         for (int columnIndex = 0, n = getColumnCount(); columnIndex < n; columnIndex++) {
             float size = 0f;
             if (byHeader) {
                 TableColumn column = columnModel.getColumn(columnIndex);
                 TableCellRenderer renderer = column.getHeaderRenderer();
                 if (renderer == null) {
                     renderer = header.getDefaultRenderer();
                 }
                 if (renderer != null) {
                     Component c = renderer.getTableCellRendererComponent(this,
                                                                          column.getHeaderValue(),
                                                                          false,
                                                                          false,
                                                                          0,
                                                                          columnIndex);
                     size = c.getPreferredSize().width * 1.5f;
                 }
             }
             if (byValue) {
                 for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                     TableCellRenderer renderer = getCellRenderer(rowIndex,
                                                                                 columnIndex);
                     if (renderer == null) {
                         continue;
                     }
                     Object value = getValueAt(rowIndex, columnIndex);
                     Component c = renderer.getTableCellRendererComponent(this,
                                                                          value,
                                                                          false,
                                                                          false,
                                                                          rowIndex,
                                                                          columnIndex);
                     size = Math.max(size, c.getPreferredSize().width);
                     if (size >= max) {
                         break;
                     }
                 }
             }
             int width = Math.round(size > max ? max : size) + 1;
             columnModel.getColumn(columnIndex).setPreferredWidth(width);
         }
     }
 
     void doSort(int columnIndex) {
         if (getColumnCount() == 0) {
             return;
         }
         final boolean reverse;
         if (lastSortedIndex == columnIndex) {
             reverse = (lastSortedIsReverse == false);
         } else {
             lastSortedIndex = columnIndex;
             reverse = false;
         }
         lastSortedIsReverse = reverse;
         getResultSetTableModel().sort(columnIndex, reverse);
         repaint();
         repaintRowHeader("unlinkedRowStatus");
     }
 
     void changeTableColumnWidth(double rate) {
         for (TableColumn column : Collections.list(getColumnModel().getColumns())) {
             column.setPreferredWidth((int)(column.getWidth() * rate));
         }
     }
 
 
 }
