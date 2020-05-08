 /*
  * DataTableComponent.java
  *
  * Created on January 31, 2011
  * @author jaycverg
  */
 
 package com.rameses.rcp.control.table;
 
 import com.rameses.common.ExpressionResolver;
 import com.rameses.rcp.common.*;
 import com.rameses.rcp.framework.Binding;
 import com.rameses.rcp.framework.ChangeLog;
 import com.rameses.rcp.framework.ClientContext;
 import com.rameses.rcp.ui.*;
 import com.rameses.rcp.util.ActionMessage;
 import com.rameses.util.ValueUtil;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.*;
 import java.util.*;
 import javax.swing.InputVerifier;
 import javax.swing.JCheckBox;
 import javax.swing.JComponent;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.ListSelectionModel;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.text.JTextComponent;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JRootPane;
 import javax.swing.SwingUtilities;
 import javax.swing.border.EmptyBorder;
 
 public class DataTableComponent extends JTable implements ListModelListener, TableControl {
     
     private static final String COLUMN_POINT = "COLUMN_POINT";
     
     private DataTableModel tableModel;
     private Map<Integer, JComponent> editors = new HashMap();
     private Binding itemBinding = new Binding();
     private TableListener tableListener;
     private AbstractListModel listModel;
     
     //internal flags
     private boolean readonly;
     private boolean required;
     private boolean editingMode;
     private boolean editorBeanLoaded;
     private boolean rowCommited = true;
     private JComponent currentEditor;
     private KeyEvent currentKeyEvent;
     private ListItem previousItem;
     
     //row background color options
     private Color evenBackground;
     private Color oddBackground;
     private Color errorBackground = Color.PINK;
     
     //row foreground color options
     private Color evenForeground;
     private Color oddForeground;
     private Color errorForeground = Color.BLACK;
     
     private Binding binding;
     
     private JLabel processingLbl;
     private boolean fetching;
     
     
     public DataTableComponent() {
         initComponents();
     }
     
     //<editor-fold defaultstate="collapsed" desc="  initComponents  ">
     private void initComponents() {
         super.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         
         tableModel = new DataTableModel();
         getTableHeader().setReorderingAllowed(false);
         getTableHeader().setDefaultRenderer(TableUtil.getHeaderRenderer());
         addKeyListener(new TableKeyAdapter());
         
         addComponentListener(new ComponentAdapter() {
             public void componentResized(ComponentEvent e) {
                 hideEditor(false);
             }
         });
         
         int cond = super.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
         KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0);
         getInputMap(cond).put(enter, "selectNextColumnCell");
         
         KeyStroke shiftEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 1);
         getInputMap(cond).put(shiftEnter, "selectPreviousColumnCell");
         
         TableEnterAction ea = new TableEnterAction(this);
         registerKeyboardAction(ea, ea.keyStroke, JComponent.WHEN_FOCUSED);
         
         //row editing ctrl+Z support
         KeyStroke ctrlZ = KeyStroke.getKeyStroke("ctrl Z");
         registerKeyboardAction(new ActionListener() {
             
             public void actionPerformed(ActionEvent e) {
                 if ( !rowCommited ) {
                     ChangeLog log = itemBinding.getChangeLog();
                     if ( log.hasChanges() ) {
                         undo();
                     }
                     //clear row editing flag of everything is undone
                     if ( !log.hasChanges() ) {
                         rowCommited = true;
                         tableListener.cancelRowEdit();
                     }
                 }
             }
             
         }, ctrlZ, JComponent.WHEN_FOCUSED);
         
     }
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc="  Getters/Setters  ">
     public void setListModel(AbstractListModel listModel) {
         this.listModel = listModel;
         listModel.setListener(this);
         tableModel.setListModel(listModel);
         setModel(tableModel);
         buildColumns();
     }
     
     public AbstractListModel getListModel() { return listModel; }
     
     public void setBinding(Binding binding) { this.binding = binding; }
     public Binding getBinding() { return binding; }
     
     public void setListener(TableListener listener) { this.tableListener = listener; }
     
     public boolean isRequired() { return required; }
     public boolean isEditingMode() { return editingMode; }
     
     public boolean isAutoResize() {
         return getAutoResizeMode() != super.AUTO_RESIZE_OFF;
     }
     
     public void setAutoResize(boolean autoResize) {
         if ( autoResize ) {
             setAutoResizeMode(super.AUTO_RESIZE_LAST_COLUMN);
         } else {
             setAutoResizeMode(super.AUTO_RESIZE_OFF);
         }
     }
     
     public boolean isReadonly() { return readonly; }
     public void setReadonly(boolean readonly) { this.readonly = readonly; }
     
     public Color getEvenBackground() { return evenBackground; }
     public void setEvenBackground(Color evenBackground) { this.evenBackground = evenBackground; }
     
     public Color getOddBackground() { return oddBackground; }
     public void setOddBackground(Color oddBackground) { this.oddBackground = oddBackground; }
     
     public Color getErrorBackground() { return errorBackground; }
     public void setErrorBackground(Color errorBackground) { this.errorBackground = errorBackground; }
     
     public Color getEvenForeground() { return evenForeground; }
     public void setEvenForeground(Color evenForeground) { this.evenForeground = evenForeground; }
     
     public Color getOddForeground() { return oddForeground; }
     public void setOddForeground(Color oddForeground) { this.oddForeground = oddForeground; }
     
     public Color getErrorForeground() { return errorForeground; }
     public void setErrorForeground(Color errorForeground) { this.errorForeground = errorForeground; }
     
     public String getVarStatus()            { return tableModel.getVarStatus(); }
     public void setVarStatus(String status) { tableModel.setVarStatus(status); }
     
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc="  buildColumns  ">
     private void buildColumns() {
         removeAll(); //remove all editors
         editors.clear(); //clear column editors map
         required = false; //reset flag to false
         int length = tableModel.getColumnCount();
         
         for ( int i=0; i<length; i++ ) {
             Column col = tableModel.getColumn(i);
             TableCellRenderer cellRenderer = TableUtil.getCellRenderer(col.getType());
             TableColumn tableCol = getColumnModel().getColumn(i);
             tableCol.setCellRenderer(cellRenderer);
             applyColumnProperties(tableCol, col);
             
             if ( !ValueUtil.isEmpty(col.getEditableWhen()) ) {
                 col.setEditable(true);
             }
             if ( !col.isEditable() ) continue;
             if ( editors.containsKey(i) ) continue;
             
             JComponent editor = TableUtil.createCellEditor(col);
             editor.setVisible(false);
             editor.setBounds(-10, -10, 10, 10);
             
             editor.addFocusListener( new EditorFocusSupport() );
             addKeyboardAction(editor, KeyEvent.VK_ENTER, true);
             addKeyboardAction(editor, KeyEvent.VK_TAB, true);
             addKeyboardAction(editor, KeyEvent.VK_ESCAPE, false);
             
             UIInput input = (UIInput) editor;
             editor.setName(col.getName());
             input.setBinding(itemBinding);
             itemBinding.register(input);
             
             if ( input instanceof Validatable ) {
                 Validatable vi = ((Validatable) input);
                 vi.setRequired(col.isRequired());
                 vi.setCaption(col.getCaption());
                 
                 if ( vi.isRequired() ) required = true;
             }
             
             editors.put(i, editor);
             add(editor);
         }
         itemBinding.setOwner( binding.getOwner() );
         itemBinding.setViewContext( binding.getViewContext() );
         itemBinding.init(); //initialize item binding
     }
     
     private void addKeyboardAction(JComponent comp, int key, boolean commit) {
         EditorKeyBoardAction kba = new EditorKeyBoardAction(comp, key, commit);
         comp.registerKeyboardAction(kba, kba.keyStroke, JComponent.WHEN_FOCUSED);
     }
     
     private void applyColumnProperties(TableColumn tc, Column c) {
         if ( c.getMaxWidth() > 0 ) tc.setMaxWidth( c.getMaxWidth() );
         if ( c.getMinWidth() > 0 ) tc.setMinWidth( c.getMinWidth() );
         
         if ( c.getWidth() > 0 ) {
             tc.setWidth( c.getWidth() );
             tc.setPreferredWidth( c.getWidth() );
         }
         
         tc.setResizable( c.isResizable() );
     }
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc="  JTable properties  ">
     protected void paintComponent(Graphics g) {
         super.paintComponent(g);
 
         if( listModel != null && fetching ) {
             if( processingLbl == null ) {
                 processingLbl = new JLabel("<html><h1>Loading...</h1></html>");
                 processingLbl.setForeground(Color.GRAY);
                 processingLbl.setVerticalAlignment(SwingUtilities.TOP);
                 processingLbl.setBorder(new EmptyBorder(5,10,10,10));
             }
             Rectangle rec = getVisibleRect();
             Graphics g2 = g.create();
             g2.translate(rec.x, rec.y);
             processingLbl.setSize(rec.width, rec.height);
             processingLbl.paint(g2);
             g2.dispose();
         }
     }
     
     public void setTableHeader(JTableHeader tableHeader) {
         super.setTableHeader(tableHeader);
         tableHeader.addMouseMotionListener(new MouseMotionAdapter() {
             public void mouseDragged(MouseEvent e) {
                 hideEditor(false);
             }
         });
     }
     
     protected void processMouseEvent(MouseEvent me) {
         if ( me.getClickCount() == 2 ) {
             Point p = new Point(me.getX(), me.getY());
             int colIndex = columnAtPoint(p);
             Column dc = tableModel.getColumn(colIndex);
             if ( dc != null && !dc.isEditable() && me.getID() == MouseEvent.MOUSE_PRESSED ) {
                 me.consume();
                 openItem();
                 return;
             }
         }
         
         super.processMouseEvent(me);
     }
     
     public boolean editCellAt(int rowIndex, int colIndex, EventObject e) {
         if ( readonly ) return false;
         
         MouseEvent me = null;
         if (e instanceof MouseEvent) {
             me = (MouseEvent) e;
             if (me.getClickCount() != 2) return false;
         }
         
         editItem(rowIndex, colIndex, e);
         return false;
     }
     
     public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
         int oldRowIndex = getSelectedRow();
         if (editingMode) {
             Point point = (Point) currentEditor.getClientProperty(COLUMN_POINT);
             if (rowIndex != point.y || columnIndex != point.x) {
                 hideEditor(currentEditor, point.y, point.x, true, true);
             }
         }
         
         super.changeSelection(rowIndex, columnIndex, toggle, extend);
         
         Column col = tableModel.getColumn(columnIndex);
         listModel.setSelectedColumn(col.getName());
         
         if ( rowIndex != oldRowIndex ) {
             rowChanged();
         }
     }
     
     protected void processKeyEvent(KeyEvent e) {
         currentKeyEvent = e;
         super.processKeyEvent(e);
     }
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc="  list model listener methods  ">
     public void refreshList() {
         if ( editingMode ) hideEditor(false);
         if ( !rowCommited ) {
             rowCommited = true;
            rowChanged();
         }
         
         ListItem item = listModel.getSelectedItem();
         int col = getSelectedColumn();
         tableModel.fireTableDataChanged();
         if(item!=null) {
             super.setRowSelectionInterval(item.getIndex(),item.getIndex());
             if ( col >= 0 ) super.setColumnSelectionInterval(col, col);
             
         }
         if ( tableListener != null ) {
             tableListener.refreshList();
         }
         
         editorBeanLoaded = false;
     }
     
     public void refreshItemUpdated(int row) {
         tableModel.fireTableRowsUpdated(row, row);
     }
     
     public void refreshItemAdded(int fromRow, int toRow) {
         tableModel.fireTableRowsInserted(fromRow, toRow);
     }
     
     public void refreshItemRemoved(int fromRow, int toRow) {
         tableModel.fireTableRowsUpdated(fromRow, toRow);
     }
     
     public void refreshSelectedItem() {
         if ( listModel != null && listModel.getSelectedItem() != null ) {
             int row = listModel.getSelectedItem().getIndex();
             if( getSelectedRow() != row ) {
                 this.changeSelection(row, 0, false, false);
             } else {
                 tableModel.fireTableRowsUpdated(row, row);
             }
         }
         
         tableListener.rowChanged();
     }
     
     public void rebuildColumns() {
         tableModel = new DataTableModel();
         tableModel.setListModel(listModel);
         setModel(tableModel);
         buildColumns();
     }
     
     public boolean isProcessing() { return listModel.isProcessing(); }
         
     public void fetchStart() {
         firePropertyChange("busy", false, true);
         fetching = true;
         if( processingLbl != null ) {
             repaint();
         }
     }
 
     public void fetchEnd() {
         firePropertyChange("busy", true, false);
         fetching = false;
         if( processingLbl != null ) {
             repaint();
         }
     }
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc="  row movements/actions support  ">
     public void movePrevRecord() {
         if ( getSelectedRow() == 0 ) {
             rowChanged();
             listModel.moveBackRecord();
         }
     }
     
     public void moveNextRecord() {
         if ( getSelectedRow() == getRowCount() - 1 ) {
             rowChanged();
             listModel.moveNextRecord();
         }
     }
     
     public void rowChanged() {
         if ( !rowCommited && previousItem != null ) {
             int oldRowIndex = previousItem.getIndex();
             boolean valid = validateRow(oldRowIndex);
             if ( valid && previousItem.getState() == 0 ) {
                 listModel.addCreatedItem();
             }
         }
         
         listModel.setSelectedItem(getSelectedRow());
         editorBeanLoaded = false;
         rowCommited = true;
         tableListener.rowChanged();
     }
     
     public void cancelRowEdit() {
         if ( !rowCommited ) {
             ChangeLog log = itemBinding.getChangeLog();
             List<ChangeLog.ChangeEntry> ceList = log.undoAll();
             for(ChangeLog.ChangeEntry ce : ceList) {
                 listModel.setSelectedColumn(ce.getFieldName());
                 listModel.updateSelectedItem();
             }
             
             rowCommited = true;
             int row = getSelectedRow();
             tableModel.fireTableRowsUpdated(row, row);
             tableListener.cancelRowEdit();
         }
     }
     
     public void undo() {
         int row = getSelectedRow();
         ChangeLog.ChangeEntry ce = itemBinding.getChangeLog().undo();
         tableModel.fireTableRowsUpdated(row, row);
         listModel.setSelectedColumn(ce.getFieldName());
         listModel.updateSelectedItem();
     }
     
     public void removeItem() {
         //if the ListModel has error messages
         //allow editing only to the row that caused the error
         if( listModel.hasErrorMessages() && listModel.getErrorMessage(getSelectedRow()) == null ) return;
         
         listModel.removeSelectedItem();
     }
     
     public void editItem(int rowIndex, int colIndex, EventObject e) {
         Column col = tableModel.getColumn(colIndex);
         if (col == null) return;
         
         JComponent editor = editors.get(colIndex);
         if ( editor == null ) return;
         
         ListItem item = listModel.getSelectedItem();
         if ( item.getItem() == null ) return;
         
         //if the ListModel has error messages
         //allow editing only to the row that caused the error
         if( listModel.hasErrorMessages() && listModel.getErrorMessage(rowIndex) == null ) return;
         
         item.setRoot(binding.getBean());
         
         if ( !ValueUtil.isEmpty(col.getEditableWhen()) ) {
             String exp = col.getEditableWhen();
             ExpressionResolver er = ClientContext.getCurrentContext().getExpressionResolver();
             try {
                 Object o = er.evaluate(item, exp);
                 if ( !"true".equals(o+"") ) return;
                 
             } catch(Exception ex) {
                 ex.printStackTrace();
                 return;
             }
         }
         
         showEditor(editor, rowIndex, colIndex, e);
     }
     
     private void openItem() {
         if ( readonly ) return;
         
         if ( tableListener != null ) {
             tableListener.openItem();
         }
     }
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc="  helper methods  ">
     private boolean isPrintableKey() {
         KeyEvent ke = currentKeyEvent;
         
         if ( ke.isActionKey() || ke.isControlDown() || ke.isAltDown() ) return false;
         switch( ke.getKeyCode() ) {
             case KeyEvent.VK_ESCAPE:
             case KeyEvent.VK_DELETE:
             case KeyEvent.VK_ENTER:
                 return false;
         }
         
         return true;
     }
     
     private boolean isEditKey() {
         switch ( currentKeyEvent.getKeyCode() ) {
             case KeyEvent.VK_F2:
             case KeyEvent.VK_INSERT:
             case KeyEvent.VK_BACK_SPACE:
                 return true;
         }
         
         return false;
     }
     
     private void selectAll(JComponent editor, EventObject evt) {
         if ( editor instanceof JTextComponent ) {
             ((JTextComponent) editor).selectAll();
         } else if ( editor instanceof JCheckBox ) {
             ((UIInput) editor).setValue( evt );
         }
     }
     
     private void focusNextCellFrom(int rowIndex, int colIndex) {
         int nextCol = findNextEditableColFrom(colIndex);
         int firstEditable = findNextEditableColFrom(-1);
         
         if ( nextCol >= 0 ) {
             this.changeSelection(rowIndex, nextCol, false, false);
             
         } else if (rowIndex+1 < tableModel.getRowCount()) {
             this.changeSelection(rowIndex+1, firstEditable, false, false);
             
         } else {
             ListItem item = listModel.getSelectedItem();
             boolean lastRow = !(rowIndex + listModel.getTopRow() < listModel.getMaxRows());
             
             if ( item.getState() == 0 ) lastRow = false;
             
             if ( !lastRow ) {
                 this.changeSelection(rowIndex, firstEditable, false, false);
                 moveNextRecord();
             } else {
                 this.changeSelection(0, firstEditable, false, false);
                 listModel.moveFirstPage();
             }
         }
     }
     
     private int findNextEditableColFrom(int colIndex) {
         for (int i = colIndex+1; i < tableModel.getColumnCount(); i++ ) {
             if ( editors.get(i) != null ) return i;
         }
         return -1;
     }
     
     private void hideEditor(boolean commit) {
         hideEditor(commit, true);
     }
     
     private void hideEditor(boolean commit, boolean grabFocus) {
         if ( !editingMode || currentEditor == null ) return;
         Point point = (Point) currentEditor.getClientProperty(COLUMN_POINT);
         hideEditor(currentEditor, point.y, point.x, commit, grabFocus);
     }
     
     private void hideEditor(JComponent editor, int rowIndex, int colIndex, boolean commit, boolean grabFocus) {
         if ( !commit ) {
             editor.setInputVerifier(null);
             ListItem item = listModel.getItemList().get(rowIndex);
             if ( item.getItem() != null ) {
                 ((UIInput) editor).refresh();
             }
         }
         
         editor.setVisible(false);
         editor.setInputVerifier(null);
         editingMode = false;
         currentEditor = null;
         
         if ( commit ) listModel.updateSelectedItem();
         tableModel.fireTableRowsUpdated(rowIndex, rowIndex);
         if ( grabFocus ) grabFocus();
     }
     
     private boolean validateRow(int rowIndex) {
         ActionMessage ac = new ActionMessage();
         itemBinding.validate(ac);
         if ( ac.hasMessages() ) {
             listModel.addErrorMessage(rowIndex, ac.toString());
         } else {
             listModel.removeErrorMessage(rowIndex);
         }
         if ( ac.hasMessages() ) return false;
         
         try {
             listModel.validate( listModel.getItemList().get(rowIndex) );
             listModel.removeErrorMessage(rowIndex);
         } catch (Exception e ) {
             String msg = getMessage(e);
             listModel.addErrorMessage(rowIndex, msg);
             return false;
         }
         
         return true;
     }
     
     private String getMessage(Throwable t) {
         if (t == null) return null;
         
         String msg = t.getMessage();
         Throwable cause = t.getCause();
         while (cause != null) {
             String s = cause.getMessage();
             if (s != null) msg = s;
             
             cause = cause.getCause();
         }
         return msg;
     }
     
     private void showEditor(JComponent editor, int rowIndex, int colIndex, EventObject e) {
         Rectangle bounds = getCellRect(rowIndex, colIndex, false);
         editor.putClientProperty(COLUMN_POINT, new Point(colIndex, rowIndex));
         editor.setBounds(bounds);
         
         UIInput input = (UIInput) editor;
         boolean refreshed = false;
         if ( !editorBeanLoaded ) {
             itemBinding.update(); //clear change log
             Object bean = listModel.getSelectedItem().getItem();
             itemBinding.setBean(bean);
             itemBinding.refresh();
             refreshed = true;
             editorBeanLoaded = true;
         }
         
         if ( e instanceof MouseEvent || isEditKey() ) {
             if ( !refreshed ) {
                 input.refresh();
             }
             selectAll(editor, e);
         } else if ( isPrintableKey() ) {
             input.setValue( currentKeyEvent );
         } else {
             return;
         }
         
         tableListener.editCellAt(rowIndex, colIndex);
         previousItem = listModel.getSelectedItem();
         
         InputVerifier verifier = (InputVerifier) editor.getClientProperty(InputVerifier.class);
         if ( verifier == null ) {
             verifier = editor.getInputVerifier();
             editor.putClientProperty(InputVerifier.class, verifier);
         }
         
         editor.setInputVerifier( verifier );
         editor.setVisible(true);
         editor.grabFocus();
         editingMode = true;
         rowCommited = false;
         currentEditor = editor;
     }
     //</editor-fold>
     
     
     //<editor-fold defaultstate="collapsed" desc="  EditorFocusSupport (class)  ">
     private class EditorFocusSupport implements FocusListener {
         
         private boolean fromTempFocus;
         
         public void focusGained(FocusEvent e) {
             if ( fromTempFocus ) {
                 if ( editingMode ) {
                     hideEditor(true);
                     JComponent comp = (JComponent) e.getSource();
                     Point point = (Point) comp.getClientProperty(COLUMN_POINT);
                     focusNextCellFrom(point.y, point.x);
                 }
                 fromTempFocus = false;
             }
         }
         
         public void focusLost(FocusEvent e) {
             fromTempFocus = e.isTemporary();
             
             if ( !e.isTemporary() ) {
                 hideEditor(true, false);
             }
         }
         
     }
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc="  EditorKeyBoardAction (class) ">
     private class EditorKeyBoardAction implements ActionListener {
         
         KeyStroke keyStroke;
         private boolean commit;
         private ActionListener[] listeners;
         
         EditorKeyBoardAction(JComponent comp, int key, boolean commit) {
             this.commit = commit;
             this.keyStroke = KeyStroke.getKeyStroke(key, 0);
             
             //hold only action on enter key
             //this is usually used by lookup
             if ( key == KeyEvent.VK_ENTER && comp instanceof JTextField ) {
                 JTextField jtf = (JTextField) comp;
                 listeners = jtf.getActionListeners();
             }
         }
         
         public void actionPerformed(ActionEvent e) {
             if ( listeners != null && listeners.length > 0 ) {
                 for ( ActionListener l: listeners) {
                     l.actionPerformed(e);
                 }
                 
             } else {
                 JComponent comp = (JComponent) e.getSource();
                 Point point = (Point) comp.getClientProperty(COLUMN_POINT);
                 if ( commit ) {
                     focusNextCellFrom( point.y, point.x );
                 } else {
                     hideEditor(comp, point.y, point.x, false, true);
                 }
             }
         }
         
     }
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc="  TableKeyAdapter (class)  ">
     private class TableKeyAdapter extends KeyAdapter {
         
         public void keyPressed(KeyEvent e) {
             switch( e.getKeyCode() ) {
                 case KeyEvent.VK_DOWN:
                     moveNextRecord();
                     break;
                 case KeyEvent.VK_UP:
                     movePrevRecord();
                     break;
                 case KeyEvent.VK_PAGE_DOWN:
                     listModel.moveNextPage();
                     break;
                 case KeyEvent.VK_PAGE_UP:
                     listModel.moveBackPage();
                     break;
                 case KeyEvent.VK_DELETE:
                     if( !isReadonly() ) removeItem();
                     break;
                 case KeyEvent.VK_ENTER:
                     if ( e.isControlDown() ) openItem();
                     break;
                 case KeyEvent.VK_HOME:
                     if ( e.isControlDown() ) listModel.moveFirstPage();
                     break;
                 case KeyEvent.VK_ESCAPE:
                     cancelRowEdit();
             }
         }
         
     }
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc="  TableEnterAction (class)  ">
     private class TableEnterAction implements ActionListener {
         
         KeyStroke keyStroke;
         private JComponent component;
         private ActionListener origAction;
         
         TableEnterAction(JComponent component) {
             this.component = component;
             keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
             origAction = component.getActionForKeyStroke(keyStroke);
         }
         
         public void actionPerformed(ActionEvent e) {
             if( !isReadonly()  && editors.size() > 0 ) {
                 JTable tbl = DataTableComponent.this;
                 int row = tbl.getSelectedRow();
                 int col = tbl.getSelectedColumn();
                 focusNextCellFrom(row, col);
             }
             else {
                 JRootPane rp = component.getRootPane();
                 if (rp != null && rp.getDefaultButton() != null ) {
                     JButton btn = rp.getDefaultButton();
                     btn.doClick();
                 } 
                 else {
                     origAction.actionPerformed(e);
                 }
             }
         }
     }
     //</editor-fold>
     
 }
