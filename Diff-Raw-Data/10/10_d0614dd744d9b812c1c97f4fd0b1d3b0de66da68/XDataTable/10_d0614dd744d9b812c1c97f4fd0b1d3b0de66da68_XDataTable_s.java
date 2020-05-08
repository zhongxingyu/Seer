 /*
  * XDataTable.java
  *
  * Created on January 31, 2011, 10:51 AM
  * @author jaycverg
  */
 
 package com.rameses.rcp.control;
 
 import com.rameses.rcp.common.AbstractListDataProvider;
 import com.rameses.rcp.common.BasicListModel;
 import com.rameses.rcp.common.Column;
 import com.rameses.rcp.common.EditorListModel;
 import com.rameses.rcp.common.ListItem;
 import com.rameses.rcp.common.MsgBox;
 import com.rameses.rcp.common.PropertyChangeHandler;
 import com.rameses.rcp.common.PropertySupport;
 import com.rameses.rcp.common.TableModelHandler;
 import com.rameses.rcp.control.table.DataTableComponent;
 import com.rameses.rcp.control.table.DataTableHeader;
 import com.rameses.rcp.control.table.DataTableModel;
 import com.rameses.rcp.control.table.ListScrollBar;
 import com.rameses.rcp.control.table.RowHeaderView;
 import com.rameses.rcp.control.table.TableBorder;
 import com.rameses.rcp.control.table.TableUtil;
 import com.rameses.rcp.framework.Binding;
 import com.rameses.rcp.framework.ClientContext;
 import com.rameses.rcp.ui.*;
 import com.rameses.rcp.util.*;
 import com.rameses.util.ValueUtil;
 import java.awt.*;
 import java.awt.event.*;
 import java.beans.*;
 import java.util.ArrayList;
 import java.util.Map;
 import javax.swing.*;
 
 public class XDataTable extends JPanel implements UIInput, Validatable, FocusListener
 {    
     private DataTableComponentImpl table;
     private ListScrollBar scrollBar;
     private RowHeaderView rowHeaderView;
     private JScrollPane scrollPane;
     
     private PropertyChangeHandlerImpl propertyHandler;    
     private AbstractListDataProvider dataProvider;
     private TableModelHandler tableModelHandler;    
     
     private ActionMessage actionMessage = new ActionMessage();    
     private Binding binding;    
     private Column[] columns; 
     private String[] depends;
     private String items;    
     private String handler;
     private String caption;
     private int index;    
     private boolean dynamic;
     private boolean showRowHeader;
     private boolean immediate;
     private boolean editable; 
             
     private ListItem currentItem;    
     private RowChangeNotifier rowChangeNotifier; 
     private ListModelLoader loader;
         
     public XDataTable() 
     {
         init();
         
         if ( !Beans.isDesignTime() ) 
         {
             rowChangeNotifier = new RowChangeNotifier(); 
             loader = new ListModelLoader();
         }
     }
     
     // <editor-fold defaultstate="collapsed" desc=" setup handlers ">
     
     public void addMouseListener(MouseListener l) {
         table.addMouseListener(l);
     }
     
     public void removeMouseListener(MouseListener l) {
         table.removeMouseListener(l);
     }
     
     public void addKeyListener(KeyListener l) {
         table.addKeyListener(l);
     }
     
     public void removeKeyListener(KeyListener l) {
         table.removeKeyListener(l);
     }
     
     // </editor-fold>
         
     // <editor-fold defaultstate="collapsed" desc="  initialize table  ">
     
     private void init() 
     {
         propertyHandler = new PropertyChangeHandlerImpl();     
         tableModelHandler = new TableModelHandlerImpl();
         
         table = new DataTableComponentImpl();
         scrollBar = new ListScrollBar();
         
         //--create and decorate scrollpane for the JTable
         scrollPane = new JScrollPane(table);
         TableUtil.customize(scrollPane, table);
         //--additional customization for the JScrollPane 
         scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
         scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
         scrollPane.setBorder(BorderFactory.createEmptyBorder());        
                 
         //--attach mouse wheel listener to table
         table.addMouseWheelListener(new MouseWheelListener() {
             public void mouseWheelMoved(MouseWheelEvent e) 
             {
                 if (dataProvider.isProcessing()) return;
                 if (table.isProcessingRequest()) return;
                 
                 int rotation = e.getWheelRotation();
                 if (rotation == 0) return;
                 
                 if (rotation < 0)
                     dataProvider.moveBackRecord();
                 else
                     dataProvider.moveNextRecord(true);
             }
         });
         
         super.setLayout(new BorderLayout());
         add(scrollPane, BorderLayout.CENTER);
         add(new ScrollBarPanel(scrollBar), BorderLayout.EAST);
         setBorder(new TableBorder());
         
         //--default table properties
         setGridColor(new Color(217,216,216));
         setShowRowHeader(false);
         setShowHorizontalLines(false);
         setRowMargin(0);
         setShowVerticalLines(true);
         setAutoResize(true);
         setRowHeight(19);
         
         if ( table.getEvenBackground() == null ) {
             Color bg = (Color) UIManager.get("Table.evenBackground");
             if ( bg == null ) bg = table.getBackground();
             table.setEvenBackground(bg);
         }
         
         if ( table.getEvenForeground() == null ) {
             Color fg = (Color) UIManager.get("Table.evenForeground");
             if ( fg != null ) table.setEvenForeground(fg);
         }
         
         if ( table.getOddBackground() == null ) {
             Color bg = (Color) UIManager.get("Table.oddBackground");
             if ( bg == null ) bg = new Color(225, 232, 246);
             table.setOddBackground(bg);
         }
         
         if ( table.getOddForeground() == null ) {
             Color fg = (Color) UIManager.get("Table.oddForeground");
             if ( fg != null ) table.setOddForeground(fg);
         }
         
         //--design time display
         if ( Beans.isDesignTime() ) {
             if ( rowHeaderView != null )
                 rowHeaderView.setRowCount(1);
             
             setPreferredSize(new Dimension(200,80));
             table.setModel(new javax.swing.table.DefaultTableModel(
                     new Object [][] { {null, null} },
                     new String [] { "Title 1", "Title 2" }
             ));
         }
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="  UIInput properties  ">
     
     public String[] getDepends() { return depends; }    
     public void setDepends(String[] depends) { this.depends = depends; }
     
     public int getIndex() { return index; }    
     public void setIndex(int index) { this.index = index; }
 
     public Binding getBinding() { return binding; }    
     public void setBinding(Binding binding) { this.binding = binding; }
     
     private boolean refreshed;
     private boolean hasLoaded;
     
     public void refresh() 
     {
         //force to update component status
         if (isEnabled()) setReadonly(isReadonly()); 
         
         if ( dataProvider != null ) 
         {
             if ( !refreshed || dynamic ) 
                 EventQueue.invokeLater(loader.load()); 
             else  
                 EventQueue.invokeLater(loader.refresh());  
         } 
         refreshed = true;
     }
     
     public void load() 
     {
         //System.out.println("load: class="+getClass().getName()+"@"+hashCode() + ", loaded="+hasLoaded + ", name="+getName());
         if (hasLoaded) return;
         
         refreshed = false;
         AbstractListDataProvider newProvider = null;
         if ( handler != null ) 
         {
             Object oHandler = UIControlUtil.getBeanValue(this, handler);
             if ( oHandler instanceof AbstractListDataProvider ) 
                newProvider = (AbstractListDataProvider) oHandler;  
         }
         
         if (newProvider == null && getItems() != null) 
         {
             if (isEditable())
                 newProvider = new EditableListModel(getItems()); 
             else 
                 newProvider = new ReadonlyListModel(getItems()); 
         }
         
         if (newProvider != null)
         {
             if (getColumns() != null) newProvider.setColumns(getColumns());
             
             dataProvider = newProvider;
             table.setBinding(binding);
             table.setDataProvider(dataProvider);
             scrollBar.setDataProvider(dataProvider); 
 
             if ( rowHeaderView != null )
             {
                 table.getModel().removeTableModelListener(rowHeaderView); 
                 rowHeaderView.setRowCount( dataProvider.getRows() );
                 table.getModel().addTableModelListener(rowHeaderView);
             }
         }
         hasLoaded = true;
     }
     
     public Object getValue() 
     {
         if ( Beans.isDesignTime() ) return null;
         
         if ( dataProvider == null || dataProvider.getSelectedItem() == null ) 
             return null; 
         else 
             return dataProvider.getSelectedItem().getItem(); 
     } 
     
     public void setValue(Object value) {}
     
     public boolean isNullWhenEmpty() { return true; }
     
     public int compareTo(Object o) {
         return UIControlUtil.compare(this, o);
     }
      
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="  table listener methods  ">
     
     public void cancelRowEdit() 
     {
         if ( rowHeaderView != null ) rowHeaderView.clearEditing();
     }
         
     protected void log(String msg) 
     {
         String name = getClass().getSimpleName(); 
         System.out.println("["+name+"] " + msg);
     }
     
     //</editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="  Getters/Setters  ">
     
     public String getVarName() {
         return (table == null? null: table.getVarName()); 
     }
     public void setVarName(String varName) {
         table.setVarName(varName); 
     }
     
     public String getVarStatus() { 
         return (table == null? null: table.getVarStatus());
     }    
     public void setVarStatus(String varStatus) { 
         table.setVarStatus(varStatus);
     } 
         
     public String getMultiSelectName() { 
         return (table == null? null: table.getMultiSelectName()); 
     } 
     public void setMultiSelectName(String multiSelectName) {
         table.setMultiSelectName(multiSelectName);
     } 
     
     public boolean isRequired() { return table.isRequired(); }    
     public void setRequired(boolean required) {}
     
     public void validateInput() 
     {
         String errmsg = dataProvider.getMessageSupport().getErrorMessages(); 
         actionMessage.clearMessages();
         if ( errmsg != null ) 
         {
             StringBuffer buffer = new StringBuffer(errmsg);
             if ( !ValueUtil.isEmpty(caption) ) {
                 buffer.insert(0, caption + " (\n").append("\n)");
             }
             actionMessage.addMessage(null, buffer.toString(), null);
         }
     }
     
     public ActionMessage getActionMessage() { return actionMessage; }
 
     public boolean isReadonly() { return table.isReadonly(); }    
     public void setReadonly(boolean readonly) { table.setReadonly(readonly); }
         
     public String getCaption() { return this.caption; }    
     public void setCaption(String caption) { this.caption = caption; }
     
     public void setName(String name) 
     {
         super.setName(name);
         
         if ( table != null ) table.setName(name);
     }
     
     public void setLayout(LayoutManager mgr) {;}
     
     public Column[] getColumns() { return columns; }
     public void setColumns(Column[] columns) 
     { 
         this.columns = columns; 
         if (Beans.isDesignTime()) 
         {
             try 
             {
                 ReadonlyListModel lm = new ReadonlyListModel(null);
                 lm.setColumns(columns);                
                 table.setDataProvider(lm);
             }
             catch(Exception ex) {
                 MsgBox.err(ex); 
             }
         }
     }
     
     public String getItems() { return items; } 
     public void setItems(String items) { this.items = items; }
     
     public String getHandler() { return handler; }
     public void setHandler(String handler) { this.handler = handler; }
     
     public boolean isDynamic() { return dynamic; }
     public void setDynamic(boolean dynamic) { this.dynamic = dynamic; }
 
     public boolean isShowHorizontalLines() { return table.getShowHorizontalLines(); }    
     public void setShowHorizontalLines(boolean show) { table.setShowHorizontalLines(show); }
 
     public boolean isShowVerticalLines() { return table.getShowVerticalLines(); }    
     public void setShowVerticalLines(boolean show) { table.setShowVerticalLines(show); }
 
     public boolean isAutoResize() { return table.isAutoResize(); }    
     public void setAutoResize(boolean autoResize) { table.setAutoResize(autoResize); }
     
     public void setRequestFocus(boolean focus) {
         if ( focus ) table.requestFocus();
     }
     
     public void requestFocus() { table.requestFocus(); }
     
     public boolean requestFocusInWindow() { return table.requestFocusInWindow(); }
     
     public void focusGained(FocusEvent e) { table.grabFocus(); }
     public void focusLost(FocusEvent e)   {}
     
     public Color getEvenBackground() { return table.getEvenBackground(); }
     public void setEvenBackground(Color evenBackground) { table.setEvenBackground( evenBackground ); }
     
     public Color getOddBackground() { return table.getOddBackground(); }
     public void setOddBackground(Color oddBackground) { table.setOddBackground( oddBackground ); }
     
     public Color getErrorBackground() { return table.getErrorBackground(); }
     public void setErrorBackground(Color errorBackground) { table.setErrorBackground( errorBackground ); }
     
     public Color getEvenForeground() { return table.getEvenForeground(); }
     public void setEvenForeground(Color evenForeground) { table.setEvenForeground( evenForeground ); }
     
     public Color getOddForeground() { return table.getOddForeground(); }
     public void setOddForeground(Color oddForeground) { table.setOddForeground( oddForeground ); }
     
     public Color getErrorForeground() { return table.getErrorForeground(); }
     public void setErrorForeground(Color errorForeground) { table.setErrorForeground( errorForeground ); }
     
     public boolean isImmediate() { return immediate; }
     public void setImmediate(boolean immediate) { this.immediate = immediate; }
     
     public boolean isShowRowHeader() { return showRowHeader; }
     public void setShowRowHeader(boolean showRowHeader) 
     {
         this.showRowHeader = showRowHeader;
         
         if ( showRowHeader ) 
         {
             JComponent leftCorner = new DataTableHeader.CornerBorder(table, JScrollPane.UPPER_LEFT_CORNER).createComponent();
             scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, leftCorner); 
             scrollPane.setRowHeaderView( (rowHeaderView = new RowHeaderView(table)) );
             rowHeaderView.setRowCount( table.getRowCount() );
         } 
         else 
         {
             scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, null);
             scrollPane.setRowHeaderView( (rowHeaderView = null) );
         }
     }
     
     public int getColumnMargin() { return table.getColumnModel().getColumnMargin(); }
     public void setColumnMargin(int margin) { table.getColumnModel().setColumnMargin(margin); }
     
     public int getRowMargin() { return table.getRowMargin(); }
     public void setRowMargin(int margin) { table.setRowMargin(margin); }
     
     public Color getGridColor() { return table.getGridColor(); }
     public void setGridColor(Color color) { table.setGridColor(color); }
     
     public boolean isEnabled() { return table.isEnabled(); }
     public void setEnabled(boolean e) 
     { 
         table.setEnabled(e); 
         scrollBar.setEnabled(e);
         scrollPane.setEnabled(e);
     }
     
     public int getRowHeight() { return table.getRowHeight(); }
     public void setRowHeight(int h) { table.setRowHeight(h); }
     
     public boolean isScrollbarAlwaysVisible() {
         return scrollBar.isVisibleAlways();
     }
     public void setScrollbarAlwaysVisible(boolean scrollbarAlwaysVisible) {
         scrollBar.setVisibleAlways(scrollbarAlwaysVisible);
     }
 
     public void setPropertyInfo(PropertySupport.PropertyInfo info) {
     }
     
     public boolean isEditable() { return editable; } 
     public void setEditable(boolean editable) { this.editable = editable; }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="  ScrollBarPanel (class)  ">
     
     private class ScrollBarPanel extends JPanel 
     {
         ScrollBarPanel(JScrollBar scrollBar) 
         {
             Dimension ps = scrollBar.getPreferredSize();
             setPreferredSize(ps);
             setLayout(new BorderLayout());
             
             scrollBar.addPropertyChangeListener(new PropertyChangeListener() 
             {
                 public void propertyChange(PropertyChangeEvent evt) 
                 {
                     String propName = evt.getPropertyName();
                     if ( "visible".equals(propName) ) 
                     {
                         Boolean visible = (Boolean) evt.getNewValue();;
                         setVisible(visible.booleanValue());
                     }
                 }
             });
             
             setVisible( scrollBar.isVisible() );
             add(scrollBar, BorderLayout.CENTER);
         }        
     }
     
     // </editor-fold>
         
     // <editor-fold defaultstate="collapsed" desc="  ReadonlyListModel (class)  ">
     
     private class ReadonlyListModel extends BasicListModel 
     {
         private java.util.List userDefinedList;
         private String name;
         
         ReadonlyListModel(String name) { this.name = name; } 
 
         public java.util.List fetchList(Map params) 
         {
             if (Beans.isDesignTime()) return null;
             
             if (userDefinedList == null && name != null)
                 userDefinedList = (java.util.List) UIControlUtil.getBeanValue(binding, name); 
             
             if (userDefinedList == null)
                 return new ArrayList();
             else
                 return userDefinedList;    
         }        
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="  EditableListModel (class)  ">
     
     private class EditableListModel extends EditorListModel 
     {
         private java.util.List userDefinedList;
         private String name;
         
         EditableListModel(String name) { this.name = name; } 
 
         public java.util.List fetchList(Map params) 
         {
             if (userDefinedList == null && name != null)
                 userDefinedList = (java.util.List) UIControlUtil.getBeanValue(binding, name); 
             
             if (userDefinedList == null)
                 return new ArrayList();
             else
                 return userDefinedList; 
         } 
     }
     
     // </editor-fold>    
     
     // <editor-fold defaultstate="collapsed" desc="  RowChangeNotifier (Class)  ">
 
     private class RowChangeNotifier 
     {
         void execute() 
         {
             int rowIndex = 0;
             if (dataProvider.getSelectedItem() != null) 
                 rowIndex = dataProvider.getSelectedItem().getIndex();
             
             updateBean(rowIndex); 
             notifyDepends(rowIndex); 
         }
                
         void updateBean(int rowIndex) 
         {
             try
             {
                 String name = getName();                 
                 ListItem oListItem = dataProvider.getListItem(rowIndex);
                 if ( !ValueUtil.isEmpty(name) ) 
                 {
                     Object value = (oListItem == null? null: oListItem.getItem()); 
                     UIControlUtil.setBeanValue(binding, name, value);
                 }
                 
                 String statName = getVarStatus();
                 if ( !ValueUtil.isEmpty(statName) ) 
                 {
                     Object statValue = dataProvider.createListItemStatus(oListItem); 
                     UIControlUtil.setBeanValue(binding, statName, statValue);
                 } 
             } 
             catch (Exception ex) 
             {
                 if (ClientContext.getCurrentContext().isDebugMode()) 
                     ex.printStackTrace(); 
             }
         }
         
         void notifyDepends(final int rowIndex) 
         {
             if ( !ValueUtil.isEmpty(getName()) ) 
             {
                 if (immediate)
                     binding.notifyDepends(XDataTable.this); 
                 
                 else 
                 {
                     Thread thread = new Thread(new Runnable() {
                         public void run() { 
                             notifyDependsAsync(rowIndex);
                         }
                     });
                     thread.start(); 
                 }
             } 
         }
         
         void notifyDependsCheckedItems(final String name) 
         {
             if (!dataProvider.isMultiSelect()) return;
             
             if (immediate)
                 binding.notifyDepends(XDataTable.this, name); 
 
             else 
             {
                 Thread thread = new Thread(new Runnable() {
                     public void run() { 
                         notifyDependsCheckedItemsAsync(name);
                     }
                 });
                 thread.start(); 
             }
         }
         
         private synchronized void notifyDependsAsync(int selectedRow) 
         {
             try { Thread.sleep(200); } catch(Exception ex) {;}             
             try 
             {
                 if (selectedRow == table.getSelectedRow()) { 
                     binding.notifyDepends(XDataTable.this); 
                 } 
             } 
             catch(Exception ex) {;} 
         }       
         
         private synchronized void notifyDependsCheckedItemsAsync(String name) 
         {
             try {
                 binding.notifyDepends(XDataTable.this, name); 
             } catch(Exception ex) {;} 
         }         
     }
            
     // </editor-fold>     
     
     // <editor-fold defaultstate="collapsed" desc="  ListModelLoader (Class)  ">
 
     private class ListModelLoader implements Runnable 
     {
         private Object LOCK = new Object();        
         private boolean refreshOnly;
         private boolean processing;
                 
         Runnable load() 
         {
             refreshOnly = false; 
             return this; 
         }
         
         Runnable refresh() 
         {
             refreshOnly = true; 
             return this; 
         } 
         
         public void run()
         {
             synchronized(LOCK) 
             {
                 try 
                 {
                     if (refreshOnly)
                         dataProvider.refresh();
                     
                     else 
                     {
                         dataProvider.load();
                         table.onrowChanged(); 
                     }
                 }
                 catch(Exception ex) {
                     showError(ex); 
                 }
             }
         } 
         
         void showError(final Exception error) {
             EventQueue.invokeLater(new Runnable() {
                 public void run() {
                     MsgBox.err(error); 
                 }
             });
         }
     }
            
     // </editor-fold>     
     
     // <editor-fold defaultstate="collapsed" desc="  DataTableComponentImpl (Class)  ">
     
     private class DataTableComponentImpl extends DataTableComponent 
     {
         XDataTable root = XDataTable.this;
         
         PropertyChangeListener propertyHandler = new PropertyChangeListener() {
             public void propertyChange(PropertyChangeEvent e) {
                 onPropertyChange(e);
             }
         };
         
         protected void onPropertyChange(PropertyChangeEvent e) 
         {
             String propName = e.getPropertyName();
             if ("checkedItemsChanged".equals(propName)) 
             {
                 if (!dataProvider.isMultiSelect()) return;
 
                 String multiName = getMultiSelectName();
                 if (multiName == null) 
                     multiName = table.getDataTableModel().DEFAULT_MULTI_SELECT_NAME; 
                 
                 rowChangeNotifier.notifyDependsCheckedItems(multiName); 
             }
         }
 
         protected void uninstall(AbstractListDataProvider dataProvider) 
         {
             dataProvider.removeHandler(root.propertyHandler);
             dataProvider.removeHandler(root.tableModelHandler); 
         }
         
         protected void install(AbstractListDataProvider dataProvider) 
         {
             dataProvider.addHandler(root.propertyHandler);
             dataProvider.addHandler(root.tableModelHandler);             
         }
 
         protected void onTableModelChanged(DataTableModel tableModel) { 
             tableModel.addHandler(propertyHandler); 
         } 
         
         protected void onrowChanged() 
         { 
             ListItem selectedItem = dataProvider.getSelectedItem();
 
             Object oldValue = (currentItem == null? null: currentItem.getItem());
             Object newValue = (selectedItem == null? null: selectedItem.getItem());            
             
             currentItem = null; 
             if (selectedItem != null) 
                 currentItem = selectedItem.clone(); 
             if (oldValue != newValue && rowChangeNotifier != null) 
                 rowChangeNotifier.execute(); 
             if (rowHeaderView != null) 
                 rowHeaderView.clearEditing();
             
             try { 
                 root.scrollBar.adjustValues(); 
             } catch(Exception ex) {;} 
         } 
         
         protected void onopenItem() 
         { 
             try 
             {
                 ListItem selectedItem = dataProvider.getSelectedItem();
                 if (selectedItem != null && selectedItem.getItem() != null) 
                 {
                     Object outcome = dataProvider.openSelectedItem();
                     if (outcome == null) return; 
 
                     binding.fireNavigation(outcome); 
                 }
             } 
             catch(Exception ex) {
                 MsgBox.err(ex); 
             } 
         } 
 
         protected void onchangedItem(ListItem li) 
         {
             currentItem = null;
             if (li != null) currentItem = li.clone(); 
             if (rowChangeNotifier != null) rowChangeNotifier.execute(); 
         }
 
         protected void oneditCellAt(int rowIndex, int colIndex) 
         {
             if (root.rowHeaderView != null) root.rowHeaderView.editRow(rowIndex);
         } 
     } 
     
     // </editor-fold>    
     
     // <editor-fold defaultstate="collapsed" desc="  PropertyChangeHandlerImpl (class)  ">    
     
     private class PropertyChangeHandlerImpl implements PropertyChangeHandler 
     {
         XDataTable root = XDataTable.this; 
         
         public void firePropertyChange(String name, int value) {
         }
 
         public void firePropertyChange(String name, boolean value) 
         {
             if ("loading".equals(name)) {
                 root.scrollBar.setEnabled(!value); 
             }             
         }
 
         public void firePropertyChange(String name, String value) {
         }
 
         public void firePropertyChange(String name, Object value) 
         {
             if ("selectedItemChanged".equals(name)) 
                 root.table.onrowChanged();
         }   
     }
     
     // </editor-fold>     
     
     // <editor-fold defaultstate="collapsed" desc="  TableModelHandlerImpl (class)  ">    
     
     private class TableModelHandlerImpl implements TableModelHandler 
     {
         XDataTable root = XDataTable.this; 
 
         public void fireTableCellUpdated(int row, int column) {}
 
         public void fireTableDataChanged() { 
             root.scrollBar.adjustValues(); 
         }
 
         public void fireTableRowsDeleted(int firstRow, int lastRow) {}
         public void fireTableRowsInserted(int firstRow, int lastRow) {}
         public void fireTableRowsUpdated(int firstRow, int lastRow) {}
         public void fireTableStructureChanged() {}
         public void fireTableRowSelected(int row, boolean focusOnItemDataOnly) {}
     }
     
     // </editor-fold>         
     
 }
