 /*
  * DataTableComponent.java
  *
  * Created on January 31, 2011
  * @author jaycverg
  */
 package com.rameses.rcp.control.table;
 
 import com.rameses.common.ExpressionResolver;
 import com.rameses.rcp.common.AbstractListDataProvider;
 import com.rameses.rcp.common.AbstractListModel;
 import com.rameses.rcp.common.Action;
 import com.rameses.rcp.common.Column;
 import com.rameses.rcp.common.EditorListModel;
 import com.rameses.rcp.common.ListItem;
 import com.rameses.rcp.common.ListPageModel;
 import com.rameses.rcp.common.MsgBox;
 import com.rameses.rcp.common.PropertyChangeHandler;
 import com.rameses.rcp.common.StyleRule;
 import com.rameses.rcp.common.TableModelHandler;
 import com.rameses.rcp.control.XCheckBox;
 import com.rameses.rcp.framework.Binding;
 import com.rameses.rcp.framework.ChangeLog;
 import com.rameses.rcp.framework.ClientContext;
 import com.rameses.rcp.ui.UIControl;
 import com.rameses.rcp.ui.UIInput;
 import com.rameses.rcp.ui.Validatable;
 import com.rameses.rcp.util.ActionMessage;
 import com.rameses.rcp.util.UICommandUtil;
 import com.rameses.rcp.util.UIControlUtil;
 import com.rameses.rcp.util.UIInputUtil;
 import com.rameses.util.ValueUtil;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Graphics;
 import java.awt.KeyboardFocusManager;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.beans.Beans;
 import javax.swing.JTable;
 import java.util.*;
 import javax.swing.InputVerifier;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.JRootPane;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.TableModelEvent;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.text.JTextComponent;
 
 public class DataTableComponent extends JTable implements TableControl 
 {    
     private static final String COLUMN_POINT = "COLUMN_POINT";
 
     private Map<Integer, JComponent> editors = new HashMap();
     private DataTableBinding itemBinding = new DataTableBinding();    
         
     private DataTableModel tableModel;
     private TableListener tableListener;
     private ListPageModel pageModel;    
     private EditorListModel editorModel;
     private AbstractListDataProvider dataProvider;
     private PropertyChangeHandlerImpl propertyHandler;
     private TableModelHandlerImpl tableModelHandler;
         
     private String multiSelectName;    
     private String varName = "item";
     private String varStatus;
     private String id;
     
     //internal flags
     private int editingRow = -1;
     private boolean readonly;
     private boolean required;
     private boolean editingMode;
     private boolean editorBeanLoaded;
     private boolean rowCommited = true;
     private boolean processingRequest;
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
     
     private JLabel lblProcessing;
     private boolean fetching;
     private int rowHeaderHeight = -1;
     
     public DataTableComponent() {
         initComponents();
     }
     
     // <editor-fold defaultstate="collapsed" desc="  initComponents  ">
     
     private void initComponents() 
     {
         super.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         
         propertyHandler = new PropertyChangeHandlerImpl(); 
         tableModelHandler = new TableModelHandlerImpl();
         tableModel = new DataTableModel();
 
         setTableHeader(new DataTableHeader(this));
         getTableHeader().setReorderingAllowed(false);
         addKeyListener(new TableKeyAdapter());       
         
         int cond = WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
         KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0);
         getInputMap(cond).put(enter, "selectNextColumnCell");
         
         KeyStroke shiftEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 1);
         getInputMap(cond).put(shiftEnter, "selectPreviousColumnCell");
         
         new TableEnterAction().install(this);
         new TableEscapeAction().install(this);
         
         //row editing ctrl+Z support
         KeyStroke ctrlZ = KeyStroke.getKeyStroke("ctrl Z");
         registerKeyboardAction(new ActionListener() {
             
             public void actionPerformed(ActionEvent e) 
             {
                 if (!rowCommited) 
                 {
                     ChangeLog log = itemBinding.getChangeLog();
                     if (log.hasChanges()) undo();
                     
                     //clear row editing flag of everything is undone
                     if (!log.hasChanges()) 
                     {
                         rowCommited = true;
                         oncancelRowEdit();
                     }
                 }
             }
             
         }, ctrlZ, JComponent.WHEN_FOCUSED);
         
         addComponentListener(new ComponentListener() {
             
             public void componentHidden(ComponentEvent e) {}
             public void componentMoved(ComponentEvent e) {}
             public void componentShown(ComponentEvent e) {}
             
             public void componentResized(ComponentEvent e) 
             {
                 if (currentEditor == null) return;
                 
                 Point colPoint = (Point) currentEditor.getClientProperty(COLUMN_POINT); 
                 Rectangle bounds = getCellRect(colPoint.y, colPoint.x, false);
                 currentEditor.setBounds(bounds); 
                 currentEditor.requestFocus(); 
                 currentEditor.grabFocus(); 
             }            
         }); 
         
         addMouseListener(new PopupMenuAdapter());
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="  Getters/Setters  ">
     
     protected void uninstall(AbstractListDataProvider dataProvider) {}
     protected void install(AbstractListDataProvider dataProvider) {}
     
     public AbstractListDataProvider getDataProvider() { return dataProvider; }    
     public void setDataProvider(AbstractListDataProvider dataProvider) 
     {
         if (Beans.isDesignTime())
         {
             Column[] columns = (dataProvider == null? null: dataProvider.getColumns());
             DataTableModelDesignTime dtm = new DataTableModelDesignTime(columns);
             setModel(dtm); 
             dtm.applyColumnAttributes(this); 
             return; 
         }
         
         AbstractListDataProvider oldDataProvider = this.dataProvider;
         if (oldDataProvider != null) 
         { 
             oldDataProvider.removeHandler(propertyHandler); 
             oldDataProvider.removeHandler(tableModelHandler);
             oldDataProvider.setUIProvider(null); 
             uninstall(oldDataProvider); 
         }
         
         this.dataProvider = dataProvider; 
         this.editorModel = null;
         this.pageModel = null;  
         
         if (this.dataProvider != null) 
             this.dataProvider.addHandler(propertyHandler); 
         if (dataProvider instanceof ListPageModel) 
             this.pageModel = (ListPageModel) dataProvider; 
         if (dataProvider instanceof EditorListModel) 
             this.editorModel = (EditorListModel) dataProvider; 
         
         //dispose the old table model
         if (tableModel != null) tableModel.dispose(); 
         
         tableModel = new DataTableModel();         
         tableModel.setDataProvider(dataProvider); 
         if (dataProvider != null) 
         {
             dataProvider.addHandler(tableModelHandler); 
             dataProvider.setUIProvider(new DefaultUIProvider());             
             install(dataProvider);
         } 
         
         itemBinding.setRoot(getBinding()); 
         tableModel.setBinding(itemBinding); 
         initDataTableModel();        
         setModel(tableModel);
         buildColumns();
         onTableModelChanged(tableModel); 
     }
     
     public DataTableModel getDataTableModel() { return tableModel; } 
             
     public boolean isProcessingRequest() { 
         return (processingRequest || fetching); 
     } 
     
     private void initDataTableModel() {
         if (tableModel == null) return;
         
         tableModel.setId(getId());
         tableModel.setVarName(getVarName());
         tableModel.setVarStatus(getVarStatus());
         tableModel.setMultiSelectName(getMultiSelectName()); 
     }
     
     public String getId() { return id; } 
     public void setId(String id) { 
         this.id = id;
         DataTableModel dtm = getDataTableModel();
         if (dtm != null) dtm.setId(id);
     }    
     
     public String getVarName() { return varName; } 
     public void setVarName(String varName) 
     { 
         this.varName = varName; 
         getDataTableModel().setVarName(varName); 
     }
     
     public String getVarStatus() { return varStatus; }    
     public void setVarStatus(String varStatus) 
     { 
         this.varStatus = varStatus; 
         getDataTableModel().setVarStatus(varStatus);
     } 
         
     public String getMultiSelectName() { return multiSelectName; } 
     public void setMultiSelectName(String multiSelectName) 
     {
         this.multiSelectName = multiSelectName; 
         getDataTableModel().setMultiSelectName(multiSelectName); 
     }    
     
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
     
     public void setRowHeight(int rowHeight) { 
         super.setRowHeight(rowHeight); 
         setRowHeaderHeight(rowHeight);
     } 
     
     public int getRowHeaderHeight() { return rowHeaderHeight; } 
     public void setRowHeaderHeight(int rowHeaderHeight) {
         this.rowHeaderHeight = rowHeaderHeight;
         getTableHeader().setPreferredSize(new Dimension(Short.MAX_VALUE, rowHeaderHeight)); 
         getTableHeader().repaint(); 
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="  buildColumns  ">
     
     private void buildColumns() 
     {
         removeAll(); //remove all editors
         editors.clear(); //clear column editors map
         required = false; //reset flag to false
         
         ColumnHandlerUtil handlerUtil = ColumnHandlerUtil.newInstance();
         int length = tableModel.getColumnCount();        
         for ( int i=0; i<length; i++ ) 
         {
             Column col = tableModel.getColumn(i);
             TableCellRenderer cellRenderer = TableUtil.getCellRenderer(col);
             handlerUtil.prepare(col);
             
             TableColumn tableCol = getColumnModel().getColumn(i);
             tableCol.setCellRenderer(cellRenderer);
             applyColumnProperties(tableCol, col);
             
             if (!ValueUtil.isEmpty(col.getEditableWhen())) 
                 col.setEditable(true);
             
             if (!col.isEditable()) continue;
             if (editors.containsKey(i)) continue;
             
             JComponent editor = TableUtil.createCellEditor(col);
             if (editor == null) continue; 
             if (!(editor instanceof UIControl))
             {
                 System.out.println("Column editor must be an instance of UIControl "); 
                 continue;
             }
 
             editor.setVisible(false);
             editor.setName(col.getName());
             editor.setBounds(-10, -10, 10, 10);
             editor.putClientProperty(JTable.class, true); 
             editor.putClientProperty(Binding.class, getBinding()); 
             editor.putClientProperty(UIInputUtil.Support.class, new EditorInputSupport()); 
             editor.putClientProperty(Validatable.class, new TableColumnValidator(itemBinding, col));
             
             editor.addFocusListener(new EditorFocusSupport());
             addKeyboardAction(editor, KeyEvent.VK_ENTER, true);
             addKeyboardAction(editor, KeyEvent.VK_TAB, true);
             addKeyboardAction(editor, KeyEvent.VK_ESCAPE, false);
             
             UIControl uicomp = (UIControl) editor;
             uicomp.setBinding(itemBinding);
             itemBinding.register(uicomp);
             
             if (editor instanceof Validatable) 
             {
                 Validatable vi = (Validatable) editor;
                 vi.setRequired(col.isRequired());
                 vi.setCaption(col.getCaption());
                 
                 if (vi.isRequired()) required = true;
             }
             
             editors.put(i, editor);
             add(editor);
         }
         
         itemBinding.setOwner( binding.getOwner() );
         itemBinding.setViewContext( binding.getViewContext() );
         itemBinding.init(); //initialize item binding
     } 
     
     public void rebuildColumns() 
     {
         tableModel = new DataTableModel();
         tableModel.setDataProvider(dataProvider);
         tableModel.setVarName(getVarName());
         tableModel.setVarStatus(getVarStatus());
         tableModel.setMultiSelectName(getMultiSelectName()); 
         setModel(tableModel);
         buildColumns();
         onTableModelChanged(tableModel); 
     } 
     
     protected void onTableModelChanged(DataTableModel tableModel){        
     }
     
     private void addKeyboardAction(JComponent comp, int key, boolean commit) 
     {
         EditorKeyBoardAction kba = new EditorKeyBoardAction(comp, key, commit);
         comp.registerKeyboardAction(kba, kba.keyStroke, JComponent.WHEN_FOCUSED);
     }
     
     private void applyColumnProperties(TableColumn tc, Column c) 
     {
         if ( c.getMaxWidth() > 0 ) tc.setMaxWidth( c.getMaxWidth() );
         if ( c.getMinWidth() > 0 ) tc.setMinWidth( c.getMinWidth() );
         
         if ( c.getWidth() > 0 ) 
         {
             tc.setWidth( c.getWidth() );
             tc.setPreferredWidth( c.getWidth() );
         }        
         tc.setResizable( c.isResizable() );
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="  JTable properties  ">
     
     protected void paintComponent(Graphics g) 
     {
         super.paintComponent(g);
 
         if ( dataProvider != null && fetching ) 
         {
             if ( lblProcessing == null ) 
             {
                 lblProcessing = new JLabel("<html><h1>Loading...</h1></html>");
                 lblProcessing.setForeground(Color.GRAY);
                 lblProcessing.setVerticalAlignment(SwingUtilities.TOP);
                 lblProcessing.setBorder(new EmptyBorder(5,10,10,10));
             } 
             
             Rectangle rec = getVisibleRect();
             Graphics g2 = g.create();
             g2.translate(rec.x, rec.y);
             lblProcessing.setSize(rec.width, rec.height);
             lblProcessing.paint(g2);
             g2.dispose();
         }
     }
     
     public void setTableHeader(JTableHeader tableHeader) 
     {
         super.setTableHeader(tableHeader);
         
         tableHeader = getTableHeader(); 
         if (tableHeader == null) return;
         
         //tableHeader.setDefaultRenderer(TableUtil.getHeaderRenderer());
         tableHeader.setDefaultRenderer(new CellRenderers.HeaderRenderer());
         tableHeader.addMouseMotionListener(new MouseMotionAdapter() {
             public void mouseDragged(MouseEvent me) 
             {
                 if (currentEditor == null) return;
                 
                 Point p = new Point(me.getX(), me.getY());
                 int colIndex = columnAtPoint(p);
                 if (colIndex < 0) return;
                 
                 Point colPoint = (Point) currentEditor.getClientProperty(COLUMN_POINT); 
                 if (colPoint.x-1 == colIndex || colPoint.x == colIndex || colPoint.x+1 == colIndex) 
                 {
                     Rectangle bounds = getCellRect(colPoint.y, colPoint.x, false);
                     currentEditor.setBounds(bounds); 
                     currentEditor.requestFocus(); 
                     currentEditor.grabFocus(); 
                 } 
                 else { 
                     hideEditor(false);
                 } 
             }
         });
     }
     
     protected void onopenItem() {}
     private void openItem() 
     {
         // do not do anything if there is an active process running
         if (processingRequest) return;
         
         try 
         { 
             processingRequest = true;
             onopenItem(); 
         } 
         catch(Exception ex) {
             MsgBox.err(ex); 
         } finally {
             processingRequest = false;
         }
     }    
     
     protected void onprocessMouseEvent(MouseEvent me) {}    
     protected void processMouseEvent(MouseEvent me) 
     {
         // do not do anything if there is an active process running
         if (processingRequest) return;
         
         if (me.getID()==MouseEvent.MOUSE_CLICKED && me.getClickCount()==2) 
         {
             Point p = new Point(me.getX(), me.getY());
             int colIndex = columnAtPoint(p);
             Column dc = tableModel.getColumn(colIndex);
             if (dc != null && !dc.isEditable()) 
             {
                 me.consume();
                 openItem(); 
                 return;
             }
         }
     
         onprocessMouseEvent(me); 
         if (me.isConsumed()) {
             //do nothing
         } else { 
             super.processMouseEvent(me);
         }
     }
     
     public boolean editCellAt(int rowIndex, int colIndex, EventObject e) 
     {
         if (isReadonly()) return false;
         
         Column oColumn = tableModel.getColumn(colIndex); 
         if (oColumn == null) return false;
 
         //automatically this column turns editable if handler is SelectionColumnHandler
         if (oColumn.getTypeHandler() instanceof SelectionColumnHandler) 
         {
             if (dataProvider.getListItemData(rowIndex) == null) return false;
             
             JComponent editor = editors.get(colIndex);
             if (editor != null) showEditor(editor, rowIndex, colIndex, e);
             
             return false;
         } 
         
         if (editorModel == null) return false; 
         
         if (e instanceof MouseEvent) 
         {
             MouseEvent me = (MouseEvent) e;
             if (me.getClickCount()==2 && SwingUtilities.isLeftMouseButton(me)) {
                 //do nothing
             } else {
                 return false; 
             }
         }
                 
         //evaluate style rule for this column
         boolean columnEditable = true;
         StyleRule[] rules = getBinding().getStyleRules();
         if (rules != null && getId() != null) {
             String qname = getId()+":"+getVarName()+"."+oColumn.getName(); 
             Object itemData = editorModel.getListItemData(rowIndex);
             for (StyleRule r : rules) {
                 String pattern = r.getPattern();
                 String expr = r.getExpression();
                 if (expr != null && qname.matches(pattern)){
                     try {
                         boolean matched = UIControlUtil.evaluateExprBoolean(createExpressionBean(itemData), expr);
                         if (!matched) continue;
                         
                         Object oval = r.getProperties().get("editable");
                         if (oval == null) continue;
                         
                         if ("true".equals(oval.toString())) 
                             columnEditable = true; 
                         else if ("false".equals(oval.toString())) 
                             columnEditable = false; 
                     } catch (Throwable t){;} 
                 }
             }             
         } 
         
         if (columnEditable) editItem(rowIndex, colIndex, e); 
         
         return false;
     }
     
     public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) 
     {
         // do not do anything if there is an active process running
         if (processingRequest) return;
         
         int oldColIndex = getSelectedColumn();
         int oldRowIndex = getSelectedRow();
         if (editingMode) 
         {
             //Point point = (Point) currentEditor.getClientProperty(COLUMN_POINT);
             if (rowIndex != oldRowIndex || columnIndex != oldColIndex) {
                 boolean success = hideEditor(currentEditor, oldRowIndex, oldColIndex, true, true);
                 if (!success) return; 
             }            
         }
 
         if (rowIndex != oldRowIndex && editorModel != null && editingRow >= 0) 
         {
             ListItem li = editorModel.getListItem(editingRow);
             if (li != null && (editorModel.isTemporaryItem(li) || li.getState()==ListItem.STATE_EDIT)) 
             { 
                 try 
                 {
                     if (!validateRow(editingRow))
                     {
                         String errmsg = editorModel.getMessageSupport().getErrorMessage(editingRow); 
                         if (errmsg != null) throw new Exception(errmsg); 
 
                         //exit from this process
                         return;
                     } 
 
                     if (li.getState() == ListItem.STATE_DRAFT)
                         editorModel.flushTemporaryItem(li);
 
 
                     editorModel.fireCommitItem(li);
                     itemBinding.getChangeLog().clear(); 
                     editingRow = -1;
                 } 
                 catch(Exception ex) 
                 {
                     tableModel.fireTableRowsUpdated(editingRow, editingRow);       
                     MsgBox.err(ex); 
                     return; 
                 } 
             } 
         }
         
         super.changeSelection(rowIndex, columnIndex, toggle, extend);
         putClientProperty("selectionPoint", new Point(columnIndex, rowIndex));        
         if (rowIndex != oldRowIndex) editingRow = -1;
             
         if (columnIndex != oldColIndex && dataProvider != null)
         {
             Column oColumn = tableModel.getColumn(columnIndex);
             dataProvider.setSelectedColumn((oColumn == null? null: oColumn.getName()));
         }
             
         if (rowIndex != oldRowIndex) rowSelectionChanged(rowIndex);
     }
     
     protected void processKeyEvent(KeyEvent e) 
     {
         // do not do anything if there is an active process running
         if (processingRequest) return;
         if (currentEditor != null) return; 
         
         currentKeyEvent = e;
         super.processKeyEvent(e);
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="  row movements/actions support  ">
         
     public void tableChanged(TableModelEvent e) 
     {
         if (getSelectedRow() >= getRowCount()) 
             setRowSelectionInterval(0, 0); 
 
         super.tableChanged(e); 
     }        
         
     protected void onrowChanged() {}     
     private void rowSelectionChanged(int index) 
     {
         if (dataProvider != null) dataProvider.setSelectedItem(index);
         
         editorBeanLoaded = false;
         rowCommited = true;
         previousItem = null; 
         onrowChanged(); 
     } 
     
     protected void oncancelRowEdit() {}
     public final void cancelRowEdit() 
     {
         if (!rowCommited) 
         {
             ChangeLog log = itemBinding.getChangeLog();
             List<ChangeLog.ChangeEntry> ceList = log.undoAll();
             for (ChangeLog.ChangeEntry ce : ceList) 
             {
                 //dataProvider.setSelectedColumn(ce.getFieldName());
                 //dataProvider.updateSelectedItem();
             }
             
             rowCommited = true;
             int row = getSelectedRow();
             tableModel.fireTableRowsUpdated(row, row);
             oncancelRowEdit();
         }
     }
     
     public void undo() 
     {
         int row = getSelectedRow();
         ChangeLog.ChangeEntry ce = itemBinding.getChangeLog().undo();
         tableModel.fireTableRowsUpdated(row, row);
         //dataProvider.setSelectedColumn(ce.getFieldName());
         //dataProvider.updateSelectedItem();
     }
     
     public final void removeItem() 
     {
         if (isReadonly()) return;
         if (editorModel == null) return;
         
         int rowIndex = getSelectedRow();
         if (rowIndex < 0) return;        
         //if the ListModel has error messages
         //allow editing only to the row that caused the error
         if (editorModel.getMessageSupport().hasErrorMessages() && 
             editorModel.getMessageSupport().getErrorMessage(rowIndex) == null) 
             return;
         
         try {
             editorModel.setSelectedItem(rowIndex); 
             editorModel.fireRemoveItem(editorModel.getListItem(rowIndex)); 
         } catch(Exception ex) {
             MsgBox.err(ex); 
         }
     } 
     
     public Object createExpressionBean(Object itemBean) {
         ExprBeanSupport beanSupport = new ExprBeanSupport(binding.getBean());
         beanSupport.setItem(getVarName(), itemBean); 
         return beanSupport.createProxy(); 
     }
     
     protected void onchangedItem(ListItem item) {} 
     
     public void editItem(int rowIndex, int colIndex, EventObject e) {
         if (editorModel == null) return;        
         /*
             if ListItem has error messages, 
             allow editing only to the row that caused the error
          */
         if (dataProvider.getMessageSupport().hasErrorMessages() && 
             dataProvider.getMessageSupport().getErrorMessage(getSelectedRow()) == null) 
             return;
         
         ListItem oListItem = tableModel.getListItem(rowIndex);
         if (!editorModel.isAllowedForEditing(oListItem)) return;
         
         Column col = tableModel.getColumn(colIndex);
         if (col == null || !col.isEditable()) return;
                 
         try {
             if (oListItem.getItem() == null || oListItem.getState() == ListItem.STATE_EMPTY) {
                 editorModel.loadTemporaryItem(oListItem);
                 oListItem.setRoot(binding.getBean()); 
                 tableModel.fireTableRowsUpdated(rowIndex, rowIndex); 
             }       
         } catch(Exception ex) {
             MsgBox.err(ex); 
             return; 
         }
         
         // evaluate the editableWhen expression 
         if ( !ValueUtil.isEmpty(col.getEditableWhen()) ) {
             boolean passed = false;
             
             try {
                 Object exprBean = createExpressionBean(oListItem.getItem());
                 passed = UIControlUtil.evaluateExprBoolean(exprBean, col.getEditableWhen());
             } catch(Exception ex) { 
                 System.out.println("Failed to evaluate expression " + col.getEditableWhen() + " caused by " + ex.getMessage());
             }
             
             if (!passed) {
                 if (dataProvider.getListItem(rowIndex+1) == null) {
                     oListItem.loadItem(null, ListItem.STATE_EMPTY);
                     tableModel.fireTableRowsUpdated(rowIndex, rowIndex); 
                 }                
                 return;
             }
         }
         
         try { 
             boolean editable = editorModel.isColumnEditable(oListItem.getItem(), col.getName());
             if (!editable) return; 
         } catch(Throwable t) {  
             System.out.println("[WARN] error caused by " + t.getMessage());
             return; 
         } 
         
         JComponent editor = editors.get(colIndex);
         if ( editor == null ) return;
         
         if (editorModel.isLastItem(oListItem)) { 
             if (editorModel.isAllowAdd()) {
                 editorModel.addEmptyItem(); 
             } else {
                 //not allowed to add more items
                 oListItem.loadItem(null, ListItem.STATE_EMPTY); 
                 return;
             }
         } 
         
         oListItem.setRoot(binding.getBean());
         tableModel.fireTableRowsUpdated(rowIndex, rowIndex); 
         
         try {
             onchangedItem(oListItem); 
         } catch(Exception ex) {
             MsgBox.err(ex); 
         }
         
         showEditor(editor, rowIndex, colIndex, e);
     }
 
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="  helper/supporting methods  ">
     
     protected void onfocusGained(FocusEvent e) {} 
     protected void onfocusLost(FocusEvent e) {}    
     protected final void processFocusEvent(FocusEvent e) 
     {
         if (e.getID() == FocusEvent.FOCUS_GAINED) 
             onfocusGained(e);
         else if (e.getID() == FocusEvent.FOCUS_LOST) 
             onfocusLost(e);
         
         super.processFocusEvent(e); 
     }
     
     private void log(String msg) 
     {
         String name = getClass().getSimpleName();
         System.out.println("["+name+"] " + msg);
     }
     
     private boolean isPrintableKey(EventObject e) 
     {
         KeyEvent ke = null;
         if (e instanceof KeyEvent) ke = (KeyEvent) e;
         if (ke == null) ke = currentKeyEvent; 
         if (ke == null) return false; 
         
         if (ke.isActionKey() || ke.isControlDown() || ke.isAltDown()) return false;
         
         switch (ke.getKeyCode()) 
         {
             case KeyEvent.VK_ESCAPE:
             case KeyEvent.VK_DELETE:
             case KeyEvent.VK_ENTER:
                 return false;
         }         
         return true;
     }
     
     private boolean isEditKey(EventObject e) 
     {
         if (!(e instanceof KeyEvent)) return false; 
         
         KeyEvent ke = (KeyEvent) e;
         switch (ke.getKeyCode()) 
         {
             case KeyEvent.VK_F2:
             case KeyEvent.VK_INSERT:
             case KeyEvent.VK_BACK_SPACE:
                 return true;
         }        
         return false;
     }
     
     private void selectAll(JComponent editor, EventObject evt) 
     {
         if (editor instanceof JTextComponent) { 
             ((JTextComponent) editor).selectAll();
         }
         else 
         {
             if (editor instanceof UIInput)
                 ((UIInput) editor).setRequestFocus(true);
             
             if (editor instanceof JCheckBox) 
                 ((UIInput) editor).setValue(evt);
         } 
     }
     
     private void focusNextCellFrom(int rowIndex, int colIndex) 
     {
         int nextCol = findNextEditableColFrom(colIndex);
         int firstEditable = findNextEditableColFrom(-1);
         
         if (nextCol >= 0) {
             this.changeSelection(rowIndex, nextCol, false, false);
         } 
         else if (rowIndex+1 < tableModel.getRowCount()) { 
             this.changeSelection(rowIndex+1, firstEditable, false, false); 
         } 
         else {
             ListItem item = dataProvider.getSelectedItem();
             /*
             boolean lastRow = !(rowIndex + dataProvider.getTopRow() < dataProvider.getMaxRows());
             
             if ( item.getState() == ListItem.STATE_EMPTY ) lastRow = false;
             
             if ( !lastRow ) 
             {
                 this.changeSelection(rowIndex, firstEditable, false, false);
                 moveNextRecord();
             } 
             else 
             {
                 this.changeSelection(0, firstEditable, false, false);
                 dataProvider.moveFirstPage();
             }*/
         }
     }
     
     private int findNextEditableColFrom(int colIndex) 
     {
         for (int i=colIndex+1; i<tableModel.getColumnCount(); i++ ) {
             if (editors.get(i) != null) return i;
         }
         return -1;
     }
     
     private void hideEditor(boolean commit) {
         hideEditor(commit, true);
     }
     
     private void hideEditor(boolean commit, boolean grabFocus) 
     {
         if ( !editingMode || currentEditor == null ) return;
         
         Point point = (Point) currentEditor.getClientProperty(COLUMN_POINT);
         hideEditor(currentEditor, point.y, point.x, commit, grabFocus);
     }
     
     private boolean hideEditor(JComponent editor, int rowIndex, int colIndex, boolean commit, boolean grabFocus) 
     {
         if (editor instanceof SelectionCellEditor) commit = false;        
         /*
          * force to invoke the setValue of the editor support when editor is instanceof JCheckBox 
          * to make sure that the data has been sent to the temporary storage before committing. 
          */
         if (editor instanceof JCheckBox && editor instanceof UIInput) 
         {
             UIInput uiinput = (UIInput) editor;
             uiinput.putClientProperty("cellEditorValue", uiinput.getValue()); 
         }
         
         InputVerifier inputVerifier = editor.getInputVerifier();
         editor.setVisible(false);                  
         editor.setInputVerifier(null);
         editingMode = false;        
         currentEditor = null;
         
         Object value = editor.getClientProperty("cellEditorValue"); 
         if ("no_updates".equals(value)) commit = false;
         
         if (commit) {
             tableModel.setBinding(itemBinding); 
             tableModel.setValueAt(value, rowIndex, colIndex); 
 
             boolean hasErrors = false;
             try {
                 if (editorModel != null) {
                     ListItem oListItem = editorModel.getListItem(editingRow);
                     editorModel.fireColumnUpdate(oListItem);
                 }
             } 
             catch(Exception ex) {
                 hasErrors = true;
                 MsgBox.err(ex);
             }
             
             if (hasErrors) { 
                 itemBinding.getChangeLog().undo();                 
                 editor.setVisible(true);
                 editor.setInputVerifier(inputVerifier); 
                 editingMode = true;
                 currentEditor = editor; 
                 currentEditor.grabFocus(); 
                 return false; 
             }            
         }
         
         tableModel.fireTableRowsUpdated(rowIndex, rowIndex); 
         if (grabFocus) grabFocus();
         
         return true;
     } 
     
     public void clearEditors() { 
         if (currentEditor != null) { 
             currentEditor.setVisible(false);
             currentEditor.setInputVerifier(null);
         } 
         editingMode = false;        
         currentEditor = null;
     }
     
     private boolean validateRow(int rowIndex) 
     {
         //exit right away if no editor model specified 
         if (editorModel == null) return true;
         
         ActionMessage ac = new ActionMessage();
         itemBinding.validate(ac);
         if ( ac.hasMessages() )
             dataProvider.getMessageSupport().addErrorMessage(rowIndex, ac.toString());
         else 
             dataProvider.getMessageSupport().removeErrorMessage(rowIndex);
         
         if ( ac.hasMessages() ) return false;
         
         try 
         {
             editorModel.fireValidateItem( dataProvider.getListItem(rowIndex) );
             dataProvider.getMessageSupport().removeErrorMessage(rowIndex);
         } 
         catch (Exception e ) 
         {
             if (ClientContext.getCurrentContext().isDebugMode()) 
                 e.printStackTrace(); 
             
             String msg = getMessage(e)+"";
             dataProvider.getMessageSupport().addErrorMessage(rowIndex, msg);
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
     
    private synchronized void showEditor(final JComponent editor, int rowIndex, int colIndex, EventObject e) 
     {
         Rectangle bounds = getCellRect(rowIndex, colIndex, false);
         editor.putClientProperty(COLUMN_POINT, new Point(colIndex, rowIndex));
         editor.putClientProperty("cellEditorValue", null); 
         editor.setBounds(bounds); 
         editor.validate(); 
         
         UIControl ui = (UIControl) editor;
         Object bean = dataProvider.getListItemData(rowIndex); 
         itemBinding.setBean(bean); 
         boolean refreshed = false;         
         if ( !editorBeanLoaded ) 
         {
             itemBinding.update(); //clear change log
             itemBinding.setRoot(binding); 
             itemBinding.setTableModel(tableModel);
             itemBinding.setRowIndex(rowIndex);
             itemBinding.setColumnIndex(colIndex);             
             itemBinding.refresh();
             refreshed = true;
             editorBeanLoaded = true;
         }
         
         if (e == null) e = currentKeyEvent; 
         
         editor.putClientProperty("updateBeanValue", false); 
         editor.putClientProperty("allowSelectAll", false);
         
         if (e instanceof MouseEvent || isEditKey(e)) 
         {
             if (!refreshed) ui.refresh();
             
             //UIInput uiinput = (UIInput) editor;
             selectAll(editor, e);
             if (editor instanceof XCheckBox) {
                 hideEditor(editor, rowIndex, colIndex, true, true); 
                 return;
             }
         } 
         else if (isPrintableKey(e))  {
             char ch = currentKeyEvent.getKeyChar();
             boolean dispatched = false; 
             
             if (!dispatched && (editor instanceof UIInput)) {
                 UIInput uiinput = (UIInput) editor;
                 uiinput.setValue(currentKeyEvent);
                 
                 if (editor instanceof XCheckBox) {
                     hideEditor(editor, rowIndex, colIndex, true, true); 
                     return;
                 }  
             }
         } 
         else {
             return;
         }
         
         if (editor instanceof ImmediateCellEditor) 
         {
             //exit right away since this was tagged as immediate
             editorBeanLoaded = false;
             return;
         } 
         
         oneditCellAt(rowIndex, colIndex);
         previousItem = dataProvider.getSelectedItem();
         
         InputVerifier verifier = (InputVerifier) editor.getClientProperty(InputVerifier.class);
         if ( verifier == null ) 
         {
             verifier = editor.getInputVerifier();
             editor.putClientProperty(InputVerifier.class, verifier);
         }
 
         editor.setInputVerifier( verifier );
        editor.setVisible(true);
         editor.requestFocus();
         
         editingRow = rowIndex; 
         editingMode = true;
         rowCommited = false;
         currentEditor = editor;
     }
 
     private boolean isValidKeyCode(int keyCode) { 
         return (keyCode >= 32 && keyCode <= 126); 
     } 
     
     protected void oneditCellAt(int rowIndex, int colIndex) {}
     
     public AbstractListModel getListModel() {
         return null;
     }
     
     // </editor-fold>
         
     // <editor-fold defaultstate="collapsed" desc="  EditorInputSupport (class)  ">
     
     private class EditorInputSupport implements UIInputUtil.Support 
     {       
         public void setValue(String name, Object value) {
             setValue(name, value, null); 
         } 
         
         public void setValue(String name, Object value, JComponent jcomp) {
             //temporarily stores the editor value 
             //the value is committed once the cell selection is about to changed            
             if (currentEditor != null) {
                 currentEditor.putClientProperty("cellEditorValue", value); 
             }
             else if (jcomp != null) {
                 jcomp.putClientProperty("cellEditorValue", value);
             }
         } 
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="  EditorFocusSupport (class)  ">
     
     private class EditorFocusSupport implements FocusListener 
     {        
         private boolean fromTempFocus;
         
         public void focusGained(FocusEvent e) 
         {
             if (fromTempFocus) 
             {
                 if (editingMode) 
                 {
                     JComponent comp = (JComponent) e.getSource();
                     UIInput uiinput = (UIInput) comp.getClientProperty(UIInput.class); 
                     
                     String ubv = null;
                     if (uiinput != null) 
                         ubv = uiinput.getClientProperty("updateBeanValue")+"";
                     else 
                         ubv = comp.getClientProperty("updateBeanValue")+""; 
                     
                     if ("false".equals(ubv)) return; 
                     
                     hideEditor(true);
                     
                     Point selPoint = null;
                     if (uiinput != null) 
                         selPoint = (Point) uiinput.getClientProperty(COLUMN_POINT);                     
                     if (selPoint == null) 
                         selPoint = (Point) comp.getClientProperty(COLUMN_POINT);
                     
                     try { 
                         focusNextCellFrom(selPoint.y, selPoint.x);
                     } catch(Exception ex) {;}
                 }
                 fromTempFocus = false;
             } 
         } 
         
         public void focusLost(FocusEvent e) 
         {
             fromTempFocus = e.isTemporary();
             //if (!e.isTemporary()) hideEditor(true, false);
         }
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="  EditorKeyBoardAction (class) ">
     
     private class EditorKeyBoardAction implements ActionListener {
         
         KeyStroke keyStroke;
         private boolean commit;
         private ActionListener[] listeners;
         
         EditorKeyBoardAction(JComponent comp, int key, boolean commit) 
         {
             this.commit = commit;
             this.keyStroke = KeyStroke.getKeyStroke(key, 0);
             
             //hold only action on enter key
             //this is usually used by lookup
             if ( key == KeyEvent.VK_ENTER && comp instanceof JTextField ) 
             {
                 JTextField jtf = (JTextField) comp;
                 listeners = jtf.getActionListeners();
             }
         }
         
         public void actionPerformed(ActionEvent e) 
         {
             if ( listeners != null && listeners.length > 0 ) 
             {
                 for ( ActionListener l: listeners) {
                     l.actionPerformed(e);
                 }
             } 
             else 
             {
                 JComponent comp = (JComponent) e.getSource();
                 Point point = (Point) comp.getClientProperty(COLUMN_POINT);
                 if (commit) 
                     focusNextCellFrom( point.y, point.x );
                 
                 else 
                 {
                     comp.firePropertyChange("enableInputVerifier", true, false); 
                     hideEditor(comp, point.y, point.x, false, true);
                 }
             }
         }        
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="  TableKeyAdapter (class)  ">
     
     private class TableKeyAdapter extends KeyAdapter 
     {        
         public void keyPressed(KeyEvent e) 
         {
             // do not do anything if there is an active process running
             if (processingRequest) return;
         
             switch (e.getKeyCode()) 
             {
                 case KeyEvent.VK_DOWN:
                     if (dataProvider.isLastItem(getSelectedRow())) 
                     {
                         e.consume();
                         dataProvider.moveNextRecord(); 
                     }
                     break;
                     
                 case KeyEvent.VK_UP:
                     if (dataProvider.isFirstItem(getSelectedRow())) 
                     {
                         e.consume();
                         dataProvider.moveBackRecord(); 
                     } 
                     break;
                     
                 case KeyEvent.VK_HOME:
                     if (pageModel != null && e.isControlDown()) 
                     {
                         e.consume();
                         pageModel.moveFirstPage(); 
                     }
                     break;
                     
                 case KeyEvent.VK_PAGE_DOWN:
                     if (pageModel != null) 
                     {
                         e.consume();
                         pageModel.moveNextPage(); 
                     }
                     break;
                     
                 case KeyEvent.VK_PAGE_UP:
                     if (pageModel != null) 
                     {
                         e.consume();
                         pageModel.moveBackPage(); 
                     }
                     break;
                     
                 case KeyEvent.VK_DELETE:
                     removeItem();       
                     EventQueue.invokeLater(new Runnable() {
                         public void run() 
                         {
                             requestFocusInWindow(); 
                             grabFocus();
                         }
                     });
                     break;
                     
                 case KeyEvent.VK_ENTER:
                     if (e.isControlDown()) openItem();
                     
                     break;
                     
                 case KeyEvent.VK_ESCAPE:
                     cancelRowEdit();
                     break;
             }
         }        
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="  TableEnterAction (class)  ">
     
     private class TableEnterAction implements ActionListener 
     {
         private JComponent component;
         private ActionListener oldAction;
         
         void install(JComponent component) 
         {
             this.component = component;
             KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
             oldAction = component.getActionForKeyStroke(ks);
             component.registerKeyboardAction(this, ks, JComponent.WHEN_FOCUSED); 
         }
         
         public void actionPerformed(ActionEvent e) 
         {
             if ( !isReadonly()  && editors.size() > 0 ) 
             {
                 JTable tbl = DataTableComponent.this;
                 int row = tbl.getSelectedRow();
                 int col = tbl.getSelectedColumn();
                 focusNextCellFrom(row, col);
             }
             else 
             {
                 JRootPane rp = component.getRootPane();
                 if (rp != null && rp.getDefaultButton() != null ) 
                 {
                     JButton btn = rp.getDefaultButton();
                     btn.doClick();
                 } 
                 else if (oldAction != null) { 
                     oldAction.actionPerformed(e); 
                 } 
             }
         }
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="  TableEscapeAction (class)  ">
     
     private class TableEscapeAction implements ActionListener 
     {        
         private ActionListener oldAction;
         
         void install(JComponent comp) 
         {
             KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);            
             oldAction = comp.getActionForKeyStroke(ks);
             comp.registerKeyboardAction(this, ks, JComponent.WHEN_FOCUSED); 
         }
         
         public void actionPerformed(ActionEvent e) 
         {
             if (editorModel != null) 
             {
                 fireAction(); 
                 return;
             }
 
             ActionListener actionL = (ActionListener) getRootPane().getClientProperty("Window.closeAction"); 
             if (actionL != null) 
                 actionL.actionPerformed(e); 
             else if (oldAction != null) 
                 oldAction.actionPerformed(e); 
         } 
         
         private void fireAction() 
         {
             int rowIndex = getSelectedRow(); 
             ListItem li = editorModel.getListItem(rowIndex); 
             if (li == null) return; 
             
             if (editorModel.isTemporaryItem(li))
             {
                 editorModel.getMessageSupport().removeErrorMessage(li.getIndex());
                 editorModel.removeTemporaryItem(li); 
                 
                 Point sel = (Point) getClientProperty("selectionPoint"); 
                 if (sel == null) sel = new Point(0, 0);
                 
                 changeSelection(rowIndex, sel.x, false, false);                 
             }
             else if (li.getState() == ListItem.STATE_EDIT && editorModel.getMessageSupport().hasErrorMessages()) 
             {
                 editorModel.getMessageSupport().removeErrorMessage(li.getIndex()); 
                 li.setState(ListItem.STATE_SYNC); 
                 tableModel.fireTableRowsUpdated(rowIndex, rowIndex); 
             } 
             else {
                 tableModel.fireTableRowsUpdated(rowIndex, rowIndex);                 
             }             
         }
     }
     
     // </editor-fold>    
     
     // <editor-fold defaultstate="collapsed" desc="  PropertyChangeHandlerImpl (class)  ">    
     
     private class PropertyChangeHandlerImpl implements PropertyChangeHandler 
     {
         DataTableComponent root = DataTableComponent.this; 
         
         public void firePropertyChange(String name, int value) {
         }
 
         public void firePropertyChange(String name, boolean value) 
         {
             if ("loading".equals(name)) 
             {
                 root.fetching = value;
                 root.repaint(); 
             }             
         }
 
         public void firePropertyChange(String name, String value) {
         }
 
         public void firePropertyChange(String name, Object value) 
         {
             if ("focusSelectedItem".equals(name)) 
                 focusSelectedItem();
         } 
         
         void focusSelectedItem() 
         {
             Point loc = (Point) getClientProperty("selectionPoint");
             if (loc == null) loc = new Point(); 
             
             ListItem li = root.dataProvider.getSelectedItem();
             int rowIndex = (li == null? 0: li.getIndex()); 
             if (!root.dataProvider.validRange(rowIndex)) rowIndex = 0;
             
             root.tableModel.fireTableRowsUpdated(rowIndex, rowIndex); 
             root.setRowSelectionInterval(rowIndex, rowIndex); 
         }
     }
     
     // </editor-fold> 
     
     // <editor-fold defaultstate="collapsed" desc="  TableModelHandlerImpl (class)  ">    
     
     private class TableModelHandlerImpl implements TableModelHandler 
     {
         DataTableComponent root = DataTableComponent.this; 
 
         public void fireTableCellUpdated(int row, int column) {}
         public void fireTableRowsDeleted(int firstRow, int lastRow) {}
         public void fireTableRowsInserted(int firstRow, int lastRow) {}
         public void fireTableRowsUpdated(int firstRow, int lastRow) {}
         
         public void fireTableDataProviderChanged() {
             
         } 
         
         public void fireTableStructureChanged() {
             root.clearEditors();
         }
 
         public void fireTableDataChanged() {
             root.clearEditors(); 
         }
         
         public void fireTableRowSelected(int row, boolean focusOnItemDataOnly) 
         {
             Point sel = (Point) root.getClientProperty("selectionPoint"); 
             if (sel == null) sel = new Point();
             
             ListItem li = root.dataProvider.getListItem(row); 
             if (li == null) 
                 root.getSelectionModel().setSelectionInterval(0, 0); 
             
             else 
             {
                 int preferredRow = 0;
                 int itemCount = root.dataProvider.getListItemCount();
                 for (int i=row; i>=0; i--) 
                 {
                     if (focusOnItemDataOnly) 
                     { 
                         //select the ListItem whose item bean is not null
                         if (root.dataProvider.getListItemData(i) != null) 
                         {
                             preferredRow = i;
                             break;
                         }
                     }
                     else if (i < itemCount) 
                     {
                         //retain the focus index as long the range index is still valid
                         preferredRow = i;
                         break;
                     }
                 }
                 root.getSelectionModel().setSelectionInterval(preferredRow, preferredRow); 
                 onrowChanged();
             }
         }
     }
     
     // </editor-fold>             
     
     // <editor-fold defaultstate="collapsed" desc=" PopupMenu Support ">    
     
     private class PopupMenuAdapter extends MouseAdapter
     {
         DataTableComponent root = DataTableComponent.this; 
         private JPopupMenu popup;
 
         public void mouseClicked(MouseEvent e) {
             if (!SwingUtilities.isRightMouseButton(e)) return;
             if (root.getDataProvider() == null) return;
             
             int rowIndex = root.rowAtPoint(e.getPoint()); 
             DataTableModel dtm = root.getDataTableModel(); 
             ListItem li = dtm.getListItem(rowIndex); 
             if (li == null) return;
 
             int colIndex = root.columnAtPoint(e.getPoint()); 
             if (colIndex < 0) colIndex = root.getSelectedColumn(); 
             
             root.changeSelection(rowIndex, colIndex, false, false);             
             if (popup == null) 
                 popup = new JPopupMenu(); 
             else 
                 popup.setVisible(false); 
             
             PopupMenuRunnable pmr = new PopupMenuRunnable(); 
             pmr.popup = popup;
             pmr.dtm = dtm; 
             pmr.li = li; 
             pmr.e = e; 
             pmr.rowIndex = rowIndex; 
             pmr.colIndex = colIndex; 
             EventQueue.invokeLater(pmr);
         }
     } 
     
     private class PopupMenuRunnable implements Runnable 
     {
         DataTableComponent root = DataTableComponent.this; 
         
         private JPopupMenu popup;
         private DataTableModel dtm;
         private ListItem li;
         private MouseEvent e;
         private int rowIndex;
         private int colIndex;
         
         private String colName;
         
         public void run() {
             try {
                 runImpl();
             } catch(Exception ex) {
                 MsgBox.err(ex); 
             }
         } 
         
         private void runImpl() 
         {
             if (li.getItem() == null) return;
             
             Column oColumn = dtm.getColumn(colIndex); 
             colName = (oColumn == null? null: oColumn.getName()); 
             
             List<Map> menuItems = root.getDataProvider().getContextMenu(li.getItem(), colName); 
             if (menuItems == null || menuItems.isEmpty()) return;
             
             Object items = popup.getClientProperty("ContextMenu.items");
             if (items == null || (items != null && !items.equals(menuItems))) 
             { 
                 popup.removeAll();
                 for (Map data: menuItems) 
                 {
                     String value = getString(data, "value");
                     if ("-".equals(value+"")) {
                         popup.addSeparator();
                         continue;
                     }
 
                     ActionMenuItem jmi = new ActionMenuItem(data, li);
                     Dimension dim = jmi.getPreferredSize();
                     jmi.setPreferredSize(new Dimension(Math.max(dim.width, 100), dim.height)); 
                     popup.add(jmi); 
                 } 
                 popup.putClientProperty("ContextMenu.items", menuItems);
             }
             
             Component[] comps = popup.getComponents();
             for (int i=0; i<comps.length; i++) {
                 if (!(comps[i] instanceof ActionMenuItem)) continue;
                 
                 ActionMenuItem ami = (ActionMenuItem) comps[i];
                 ami.listItem = li;
                 ami.refresh();
             }
             
             popup.pack();
             popup.show(e.getComponent(), e.getX(), e.getY()); 
         }
         
         private String getString(Map data, String name) {
             Object o = data.get(name); 
             return (o == null? null: o.toString()); 
         }
     }
     
     private class ActionMenuItem extends JMenuItem 
     {
         DataTableComponent root = DataTableComponent.this;
         private Map data;
         private ListItem listItem;
         
         ActionMenuItem(Map data, ListItem listItem) {
             this.data = (data == null? new HashMap(): data);
             this.listItem = listItem;
             
             setText(this.data.get("value")+"");
             addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     invokeAction(e);
                 }
             });
             
             if (this.data.get("action") instanceof Action) {
                 Action a = (Action) this.data.get("action");
                 Map newData = a.toMap(); 
                 newData.putAll(this.data); 
                 this.data = newData;
             }
         }
         
         Map getData() { return data; } 
         
         void invokeAction(ActionEvent e) {
             try {
                 Object item = listItem == null? null: listItem.getItem();                 
                 Object result = root.getDataProvider().callContextMenu(item, getData());
                 if (result == null) return;
                 
                 if (result instanceof Action) {
                     Action a = (Action) result;
                     UICommandUtil.processAction(root, root.getBinding(), a); 
                 }
                 else { 
                     root.getBinding().fireNavigation(result); 
                 }
             } 
             catch(Exception ex) {
                 MsgBox.err(ex); 
             }            
         }
         
         void refresh() 
         {
             try {
                 boolean enabled = !"false".equals(this.data.get("enabled")+""); 
                 setEnabled(enabled);
                 
                 Object item = listItem == null? null: listItem.getItem();
                 if (item == null) return;
                 
                 ExpressionResolver resolver = ExpressionResolver.getInstance();
                 synchronized (resolver) {
                     Object exprBean = root.createExpressionBean(item);
                     if (this.data.containsKey("disabledWhen")) {
                         String expr = this.data.get("disabledWhen").toString(); 
                         setEnabled(!evalBoolean(resolver, exprBean, expr)); 
                     }
                     if (this.data.containsKey("visibleWhen")) {
                         String expr = this.data.get("visibleWhen").toString(); 
                         setVisible(evalBoolean(resolver, exprBean, expr)); 
                     } 
                 }
             } 
             catch(Throwable ex) {;}              
         } 
         
         boolean evalBoolean(ExpressionResolver resolver, Object exprBean, String expr) {
             try {
                 return resolver.evalBoolean(expr, exprBean);
             } catch(Throwable ex) {
                 return false; 
             }
         }
     }
     
     // </editor-fold>
         
     // <editor-fold defaultstate="collapsed" desc=" FocusGrabber "> 
     
     private class FocusGrabber implements Runnable {
         
         DataTableComponent root = DataTableComponent.this; 
         private int rowIndex;
         private int colIndex;
         
         FocusGrabber(int rowIndex, int colIndex) {
             this.rowIndex = rowIndex;
             this.colIndex = colIndex;
         }
 
         void focus() {
             EventQueue.invokeLater(this);
         }
         
         public void run() {
             Component pfo = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner(); 
             if (root.equals(pfo)) return;
             
             //if (root.hasFocus()) return;             
             root.grabFocus();
             root.requestFocusInWindow(); 
             
             try { Thread.sleep(200); } catch(Throwable t){;} 
             EventQueue.invokeLater(this);
         }
         
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc=" DefaultUIProvider ">  
     
     private class DefaultUIProvider implements AbstractListDataProvider.UIProvider 
     {
         DataTableComponent root = DataTableComponent.this; 
         
         public Object getBinding() { 
             return root.getBinding(); 
         }         
     }
     
     // </editor-fold>
     
 }
