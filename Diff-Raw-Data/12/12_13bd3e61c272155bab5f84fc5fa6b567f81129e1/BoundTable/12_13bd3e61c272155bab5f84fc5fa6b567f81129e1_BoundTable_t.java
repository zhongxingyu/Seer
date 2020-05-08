 /*
  * BoundTable.java
  *
  * Created on July 24, 2007, 5:30 PM
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.totsp.gwittir.client.ui.table;
 
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FocusListener;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTMLTable;
 import com.google.gwt.user.client.ui.HasFocus;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.ScrollListener;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.SourcesClickEvents;
 import com.google.gwt.user.client.ui.SourcesTableEvents;
 import com.google.gwt.user.client.ui.TableListener;
 import com.google.gwt.user.client.ui.Widget;
 
 import com.totsp.gwittir.client.action.Action;
 import com.totsp.gwittir.client.beans.Bindable;
 import com.totsp.gwittir.client.beans.Binding;
 import com.totsp.gwittir.client.beans.Introspector;
 import com.totsp.gwittir.client.beans.Property;
 import com.totsp.gwittir.client.keyboard.KeyBinding;
 import com.totsp.gwittir.client.keyboard.KeyBindingException;
 import com.totsp.gwittir.client.keyboard.KeyboardController;
 import com.totsp.gwittir.client.keyboard.SuggestedKeyBinding;
 import com.totsp.gwittir.client.keyboard.Task;
 import com.totsp.gwittir.client.log.Level;
 import com.totsp.gwittir.client.ui.BoundWidget;
 import com.totsp.gwittir.client.ui.Button;
 import com.totsp.gwittir.client.ui.Label;
 import com.totsp.gwittir.client.ui.util.BoundWidgetTypeFactory;
 import com.totsp.gwittir.client.util.ListSorter;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import java.util.Map.Entry;
 
 
 /**
  * This is an option-rich table for use with objects implementing the Bindable interfaces.
  * @author <a href="mailto:cooper@screaming-penguin.com">Robert "kebernet" Cooper</a>
  * @see com.totsp.gwittir.client.beans.Bindable
  */
 public class BoundTable extends AbstractTableWidget implements HasChunks {
     private static BoundTable activeTable = null;
 
     /**
      * A placholder for no mask options (0)
      */
     public static final int NONE_MASK = 0;
 
     /**
      * Renderer the table inside a scroll panel.
      *
      * <p>If the table has a DataProvider, it will use the "Google Reader"
      * get-next-chunk-on-max-scroll operation.</p>
      */
     public static final int SCROLL_MASK = 1;
 
     /**
      * Renderers a heading row on the table using the labels on the Column objects.
      */
     public static final int HEADER_MASK = 2;
 
     /**
      * Lets the user have multiple rows in the "selected" state at a time.
      */
     public static final int MULTIROWSELECT_MASK = 4;
 
     /**
      * Turns off row selection and styling.
      */
     public static final int NO_SELECT_ROW_MASK = 8;
 
     /**
      * Turns off selected column stying.
      */
     public static final int NO_SELECT_COL_MASK = 16;
 
     /**
      * Turns off cell selection stying.
      */
     public static final int NO_SELECT_CELL_MASK = 32;
 
     /**
      * Tells the table to render a spacing row in between bound rows.
      */
     public static final int SPACER_ROW_MASK = 64;
 
     /**
      * If this table has a DataProvider AND it is not scrolling, this supresses the
      * first, previous, next and last buttons at the bottom of the table.
      */
     public static final int NO_NAV_ROW_MASK = 128;
 
     /**
      * Enables sorting on the table when a header row is clicked.
      *
      * If this table has a DataProvider, it must be a SortableDataProvider for this to work.
      */
     public static final int SORT_MASK = 256;
 
     /**
      * Enables the click in widget insertion. Note: This will use the default widget type
      * for the model object from the BoundWidgetTypeFactory
      * @see com.totsp.gwittir.client.ui.util.BoundWidgetTypeFactory
      */
     public static final int INSERT_WIDGET_MASK = 512;
 
     /**
      * Determines whether buttons for row handles/selection should be present.
      */
     public static final int ROW_HANDLE_MASK = 1024;
     private static final String DEFAULT_STYLE = "default";
     private static final String NAV_STYLE = "nav";
     private Binding topBinding;
     private Button allRowsHandle;
     private Collection value;
     private DataProvider provider;
     private FlexTable table;
     private HashMap clickListeners = new HashMap() /*<SourcesClickEvents, ClickListener>*/;
     private HashMap focusListeners = new HashMap() /*<HasFocus, FocusListener>*/;
     private HashMap selectedRowStyles /*<Integer, String>*/;
     private List rowHandles; /*<Button>*/
     private Map externalKeyBindings = new HashMap();
     private Map keyBindings = new HashMap();
     private Map widgetCache = new HashMap();
     private Map bindingCache = new HashMap();
     private ScrollPanel scroll;
     private String selectedCellLastStyle;
     private String selectedColLastStyle = BoundTable.DEFAULT_STYLE;
     private String selectedRowLastStyle = BoundTable.DEFAULT_STYLE;
     private Widget base;
     private boolean[] ascending;
     private Field[] columns;
     private boolean inChunk = false;
     private int currentChunk = -1;
     private int lastScrollPosition;
     private int masks;
     private int numberOfChunks;
     private int selectedCellRowLastIndex = -1;
     private int selectedColLastIndex = -1;
     private int selectedRowLastIndex = -1;
     private Timer cleanUpCaches = new Timer(){
         public void run() {
             if( value != null ){
                 for( Iterator it = widgetCache.keySet().iterator(); it.hasNext(); ){
                     Object o = it.next();
                     if(!value.contains( o ) ){
                         widgetCache.remove(o);
                     }
                 }
                 for( Iterator it = bindingCache.keySet().iterator(); it.hasNext(); ){
                     Object o = it.next();
                     if(!value.contains( o ) ){
                         bindingCache.remove(o);
                     }
                 }
             }
         }
         
     };
 
     /** Creates a new instance of BoundTable */
     public BoundTable() {
         super();
         this.init(0);
     }
 
     /**
      * Creates a new instance of Bound table with the indicated options value.
      * @param masks int value containing the sum of the *_MASK options for the table.
      */
     public BoundTable(int masks) {
         super();
         this.init(masks);
     }
 
     /**
      * Creates a new instance of Bound table with the indicated options value.
      * @param typeFactory A BoundWidget type factory used to create the widgets that appear in the table.
      * @param masks int value containing the sum of the *_MASK options for the table.
      */
     public BoundTable(int masks, BoundWidgetTypeFactory typeFactory) {
         super();
         this.factory = typeFactory;
         this.init(masks);
     }
 
     /**
      * Creates a new instance of a table using a Collection as a data set.
      * @param masks int value containing the sum of the *_MASK options for the table.
      * @param cols The Column objects for the table.
      * @param value A collection containing Bindable objects to render in the table.
      */
     public BoundTable(int masks, Field[] cols, Collection value) {
         super();
         this.setColumns(cols);
         this.value = value;
         this.init(masks);
     }
 
     /**
      * Creates a new instance of a table using a Collection as a data set.
      * @param typeFactory A BoundWidget type factory used to create the widgets that appear in the table.
      * @param masks int value containing the sum of the *_MASK options for the table.
      * @param cols The Column objects for the table.
      * @param value A collection containing Bindable objects to render in the table.
      */
     public BoundTable(
         int masks, BoundWidgetTypeFactory typeFactory, Field[] cols, Collection value) {
         super();
         this.setColumns(cols);
         this.value = value;
         this.factory = typeFactory;
         this.init(masks);
     }
 
     /**
      * Creates a new instance of a table using a Collection as a data set.
      *
      * @param masks int value containing the sum of the *_MASK options for the table.
      * @param cols The Column objects for the table.
      */
     public BoundTable(int masks, Field[] cols) {
         super();
         this.setColumns(cols);
         this.init(masks);
     }
 
     /**
      * Creates a new instance of a table using a Collection as a data set.
      * @param typeFactory A BoundWidget type factory used to create the widgets that appear in the table.
      * @param masks int value containing the sum of the *_MASK options for the table.
      * @param cols The Column objects for the table.
      */
     public BoundTable(int masks, BoundWidgetTypeFactory typeFactory, Field[] cols) {
         super();
         this.setColumns(cols);
         this.factory = typeFactory;
         this.init(masks);
     }
 
     /**
      * Creates a new instance of BoundTable
      * @param masks int value containing the sum of the *_MASK options for the table.
      * @param cols The Column objects for the table.
      * @param provider Instance of DataProvider to get chunked data from.
      */
     public BoundTable(int masks, Field[] cols, DataProvider provider) {
         super();
         this.setColumns(cols);
         this.provider = provider;
         this.init(masks);
     }
 
     /**
      * Creates a new instance of BoundTable
      * @param typeFactory A BoundWidget type factory used to create the widgets that appear in the table.
      * @param masks int value containing the sum of the *_MASK options for the table.
      * @param cols The Column objects for the table.
      * @param provider Instance of DataProvider to get chunked data from.
      */
     public BoundTable(
         int masks, BoundWidgetTypeFactory typeFactory, Field[] cols, DataProvider provider) {
         super();
         this.setColumns(cols);
         this.provider = provider;
         this.factory = typeFactory;
         this.init(masks);
     }
 
     public void setActive(boolean active) {
         if ((BoundTable.activeTable == this) & !active) {
             BoundTable.activeTable = null;
             this.changes.firePropertyChange("active", true, false);
         } else if (BoundTable.activeTable != this) {
             if (BoundTable.activeTable != null) {
                 BoundTable.activeTable.setActive(false);
             }
 
             BoundTable.activeTable = this;
             this.changes.firePropertyChange("active", false, true);
         }
     }
 
     public boolean getActive() {
         return BoundTable.activeTable == this;
     }
 
     /**
      * Returns the Binding object used by this table.
      * @return The Binding object for this table.
      */
     public Binding getBinding() {
         return this.topBinding;
     }
 
     public void setBorderWidth(int width) {
         this.table.setBorderWidth(width);
     }
 
     public int getCellCount(int row) {
         return this.table.getCellCount(row);
     }
 
     public HTMLTable.CellFormatter getCellFormatter() {
         return this.table.getCellFormatter();
     }
 
     public void setCellPadding(int padding) {
         this.table.setCellPadding(padding);
     }
 
     public int getCellPadding() {
         return this.table.getCellPadding();
     }
 
     public void setCellSpacing(int spacing) {
         this.table.setCellSpacing(spacing);
     }
 
     public int getCellSpacing() {
         return this.table.getCellSpacing();
     }
 
     /**
      * Called by the DataProvider to pass in a requested chunk of data.
      * THIS METHOD MUST BE CALLED ASYNCRONOUSLY.
      * @param c The next requested chunk of Bindable objects.
      */
     public void setChunk(Collection c) {
         if (!this.inChunk) {
             throw new RuntimeException("This method MUST becalled asyncronously!");
         }
 
         if ((masks & this.SCROLL_MASK) > 0) {
             this.add(c);
         } else {
             this.setValue(c);
         }
 
         if (
             ((masks & this.SCROLL_MASK) > 0) &&
                 (this.scroll.getScrollPosition() >= this.lastScrollPosition)) {
             this.scroll.setScrollPosition(this.lastScrollPosition);
         }
 
         this.inChunk = false;
     }
 
     public HTMLTable.ColumnFormatter getColumnFormatter() {
         return this.table.getColumnFormatter();
     }
 
     /**
      * Sets Column[] object for use on the table.
      * Note, this will foce a re-init of the table.
      * @param columns Column[] to use to render the table.
      */
     public void setColumns(Field[] columns) {
         this.columns = new Field[columns.length];
 
         for (int i = 0; i < columns.length; i++) {
             this.columns[i] = columns[i];
         }
 
         if ((this.masks & BoundTable.SORT_MASK) > 0) {
             this.ascending = new boolean[this.columns.length];
         }
 
         if (this.topBinding != null) { // Used to check that init() has fired.
             this.renderAll();
         }
     }
 
     /**
      * Returns the Columns used in this table.
      * @return Column[] used for rendering this table.
      */
     public Field[] getColumns() {
         Field[] ret = new Field[this.columns.length];
 
         for (int i = 0; i < ret.length; i++) {
             ret[i] = this.columns[i];
         }
 
         return columns;
     }
 
     /**
      * Returns the current fetched chunk from the data provider.
      * @return int index of the current chunk.
      */
     public int getCurrentChunk() {
         return currentChunk;
     }
 
     public void setDataProvider(DataProvider provider) {
         this.provider = provider;
         this.provider.init(this);
         this.inChunk = true;
     }
 
     public FlexTable.FlexCellFormatter getFlexCellFormatter() {
         return this.table.getFlexCellFormatter();
     }
 
     public String getHTML(int row, int column) {
         return this.table.getHTML(row, column);
     }
 
     public void setHeight(String height) {
         this.base.setHeight(height);
     }
 
     /**
      * Returns the number of available chunks (passed in from the DataProvider)
      * @return int number of chunks available from the DataProvider
      */
     public int getNumberOfChunks() {
         return numberOfChunks;
     }
 
     public void setPixelSize(int width, int height) {
         this.table.setPixelSize(width, height);
     }
 
     public int getRowCount() {
         return this.table.getRowCount();
     }
 
     public HTMLTable.RowFormatter getRowFormatter() {
         return this.table.getRowFormatter();
     }
 
     /**
      * Sets the indicated items in the list to "selected" state.
      * @param selected A List of Bindables to set as the Selected value.
      */
     public void setSelected(List selected) {
         int i = 0;
         this.clearSelectedRows();
 
         for (Iterator it = this.topBinding.getChildren().iterator(); it.hasNext(); i++) {
             Bindable b = ((Binding) ((Binding) it.next()).getChildren().get(0)).getRight().object;
 
             if (selected.contains(b)) {
                 this.setSelectedRow(calculateObjectToRowOffset(i));
 
                 if (
                     this.table.getWidget(calculateObjectToRowOffset(i), 0) instanceof HasFocus) {
                     ((HasFocus) this.table.getWidget(calculateObjectToRowOffset(i), 0)).setFocus(
                         true);
                 }
             }
         }
     }
 
     /**
      * Returns a List containing the current selected row objects.
      * @return List of Bindables from the selected rows.
      */
     public List getSelected() {
         ArrayList selected = new ArrayList();
         HashSet realIndexes = new HashSet();
 
         if (this.selectedRowStyles != null) {
             for (Iterator it = this.selectedRowStyles.keySet().iterator(); it.hasNext();) {
                 realIndexes.add(calculateRowToObjectOffset((Integer) it.next()));
             }
         } else if (this.selectedRowLastIndex != -1) {
             realIndexes.add(
                 calculateRowToObjectOffset(new Integer(this.selectedRowLastIndex)));
         }
 
         int i = 0;
 
         for (Iterator it = this.topBinding.getChildren().iterator(); it.hasNext(); i++) {
             if (realIndexes.contains(new Integer(i))) {
                 selected.add(
                     ((Binding) ((Binding) it.next()).getChildren().get(0)).getRight().object);
             } else {
                 it.next();
             }
         }
 
         return selected;
     }
 
     public void setSize(String width, String height) {
         this.base.setSize(width, height);
     }
 
     public void setStyleName(String style) {
         this.base.setStyleName(style);
     }
 
     public String getStyleName() {
         return this.base.getStyleName();
     }
 
     public String getTitle() {
         return this.table.getTitle();
     }
 
     public void setValue(Object value) {
         Collection old = this.value;
         this.value = (Collection) value;
         this.changes.firePropertyChange("value", old, this.value);
 
         if (this.isAttached()) {
             boolean active = this.getActive();
             this.setActive(false);
             this.renderAll();
             this.setActive(active);
         }
     }
 
     public Object getValue() {
         return value;
     }
 
     public BoundWidget getWidget(int row, int col) {
         return (BoundWidget) this.table.getWidget(row, col);
     }
 
     public void setWidth(String width) {
         this.base.setWidth(width);
     }
 
     /**
      * Adds a new Bindable object to the table.
      * @param o An object of type Bindable.
      */
     public void add(Bindable o) {
         if (this.value.add(o)) {
             this.addRow(o);
         }
     }
 
     /**
      * Adds a colleciton of Bindables to the table
      * @param c A collection containing Bindable objects.
      */
     public void add(Collection c) {
         for (Iterator it = c.iterator(); it.hasNext();) {
             this.add((Bindable) it.next());
         }
     }
 
     public void addKeyBinding(KeyBinding binding, BoundWidget widget)
         throws KeyBindingException {
         if (this.getActive()) {
             KeyboardController.INSTANCE.register(binding, widget);
         }
 
         this.addKeyBinding(binding, (Object) widget);
     }
 
     public void addKeyBinding(KeyBinding binding, Task task)
         throws KeyBindingException {
         if (this.getActive()) {
             KeyboardController.INSTANCE.register(binding, task);
         }
 
         this.addKeyBinding(binding, (Object) task);
     }
 
     public void addKeyBinding(KeyBinding binding, Action action)
         throws KeyBindingException {
         if (this.getActive()) {
             KeyboardController.INSTANCE.register(binding, action);
         }
 
         this.addKeyBinding(binding, (Object) action);
     }
 
     public void addKeyBinding(KeyBinding binding) throws KeyBindingException {
         if (this.getActive()) {
             KeyboardController.INSTANCE.register(binding, this);
         }
 
         this.addKeyBinding(binding, (Object) this);
     }
 
     public void addStyleName(String style) {
         this.base.addStyleName(style);
     }
 
     public void addTableListener(TableListener listener) {
         this.table.addTableListener(listener);
     }
 
     /**
      * Clears the table and cleans up all bindings and listeners.
      */
     public void clear() {
         this.topBinding.unbind();
         this.topBinding.getChildren().clear();
 
         if (this.rowHandles != null) {
             this.rowHandles.clear();
         }
 
         if (this.keyBindings != null) {
             this.keyBindings.clear();
         }
 
         for (Iterator it = this.focusListeners.entrySet().iterator(); it.hasNext();) {
             Entry entry = (Entry) it.next();
             ((HasFocus) entry.getKey()).removeFocusListener(
                 (FocusListener) entry.getValue());
         }
 
         for (Iterator it = this.clickListeners.entrySet().iterator(); it.hasNext();) {
             Entry entry = (Entry) it.next();
             ((SourcesClickEvents) entry.getKey()).removeClickListener(
                 (ClickListener) entry.getValue());
         }
 
         this.table.clear();
 
         while (table.getRowCount() > 0) {
             this.table.removeRow(0);
         }
 
         if (this.selectedRowStyles != null) {
             this.selectedRowStyles.clear();
         }
 
         this.clearSelectedCol();
         this.selectedCellLastStyle = BoundTable.DEFAULT_STYLE;
         this.selectedColLastIndex = -1;
         this.selectedColLastStyle = BoundTable.DEFAULT_STYLE;
         this.selectedRowLastIndex = -1;
         this.selectedRowLastStyle = BoundTable.DEFAULT_STYLE;
         this.selectedCellRowLastIndex = -1;
         this.cleanUpCaches.schedule(50);
     }
 
     /**
      * Causes the table to go to the first chunk of data, if a data provider is used.
      */
     public void first() {
         this.currentChunk = 0;
         this.provider.getChunk(this, this.getCurrentChunk());
         this.inChunk = true;
     }
 
     /**
      * Method called by the DataProvider to initialize the first chunk and pass
      * in the to total number of chunks available.
      * @param c Data for Chunk index 0
      * @param numberOfChunks The total number of available chunks of data.
      */
     public void init(Collection c, int numberOfChunks) {
         this.numberOfChunks = numberOfChunks;
         this.currentChunk = 0;
         this.setValue(c);
         this.inChunk = false;
     }
 
     /**
      * Causes the table to render the last chunk of data.
      */
     public void last() {
         if ((this.numberOfChunks - 1) >= 0) {
             this.currentChunk = this.numberOfChunks - 1;
             this.provider.getChunk(this, currentChunk);
             this.inChunk = true;
         }
     }
 
     /**
      * Causes the table to render the next chunk of data.
      */
     public void next() {
         //GWT.log("Next invoked. NOC: " + this.numberOfChunks, null);
         if ((this.currentChunk + 1) < this.numberOfChunks) {
             this.provider.getChunk(this, ++currentChunk);
             this.inChunk = true;
         }
     }
 
     /**
      * Causes teh table to render the previous chunk of data.
      */
     public void previous() {
         if ((this.getCurrentChunk() - 1) >= 0) {
             this.provider.getChunk(this, --currentChunk);
             inChunk = true;
         }
     }
 
     /**
      * Sorts the table based on the value of the property in the specified column
      * index.
      *
      * If using a SortableDataProvider, this will throw a runtime exception if the
      * column denoted by the index is not a supported sortable column.
      * @param index index of the column to sort the table on.
      */
     public void sortColumn(int index) {
         ascending[index] = !ascending[index];
 
         if (this.provider == null) {
             ArrayList sort = new ArrayList();
             sort.addAll(value);
 
             try {
                 ListSorter.sortOnProperty(
                     sort, columns[index].getPropertyName(), ascending[index]);
             } catch (Exception e) {
                 LOG.log( Level.INFO, "Exception during sort", e);
             }
 
             value.clear();
 
             for (Iterator it = sort.iterator(); it.hasNext();) {
                 value.add(it.next());
             }
 
             setValue(value);
         } else if (this.provider instanceof SortableDataProvider) {
             SortableDataProvider sdp = (SortableDataProvider) this.provider;
             boolean canSort = false;
             String[] sortableProperties = sdp.getSortableProperties();
 
             for (int i = 0; (i < sortableProperties.length) && !canSort; i++) {
                 if (sortableProperties[i].equals(this.columns[index].getPropertyName())) {
                     canSort = true;
                 }
             }
 
             if (!canSort) {
                 throw new RuntimeException(
                     "That is not a sortable field from this data provider.");
             }
 
             sdp.sortOnProperty(
                 this, this.columns[index].getPropertyName(), this.ascending[index]);
         }
 
         int startColumn = ((masks & BoundTable.ROW_HANDLE_MASK) > 0) ? 1 : 0;
 
         if ((this.masks & BoundTable.HEADER_MASK) > 0) {
             table.getCellFormatter().setStyleName(
                 0, index + startColumn, this.ascending[index] ? "ascending" : "descending");
         }
     }
 
     protected void onAttach() {
         super.onAttach();
         this.renderAll();
     }
 
     protected void onDetach() {
         super.onDetach();
         this.clear();
         this.setActive(false);
     }
 
     private void setSelectedCell(int row, int col) {
         if (
             (((this.masks & BoundTable.HEADER_MASK) > 0) && (row == 0)) ||
                 ((this.masks & BoundTable.NO_SELECT_CELL_MASK) > 0)) {
             return;
         }
 
         if ((this.selectedColLastIndex != -1) && (this.selectedCellRowLastIndex != -1)) {
             this.getCellFormatter().setStyleName(
                 this.selectedCellRowLastIndex, this.selectedColLastIndex,
                 this.selectedCellLastStyle);
         }
 
         this.selectedCellLastStyle = table.getCellFormatter().getStyleName(row, col);
 
         if (
             (this.selectedCellLastStyle == null) ||
                 (this.selectedCellLastStyle.length() == 0)) {
             this.selectedCellLastStyle = BoundTable.DEFAULT_STYLE;
         }
 
         table.getCellFormatter().setStyleName(row, col, "selected");
     }
 
     private void setSelectedCol(int col) {
         clearSelectedCol();
         this.selectedColLastIndex = col;
 
         if ((this.masks & BoundTable.NO_SELECT_COL_MASK) == 0) {
             this.selectedColLastStyle = table.getColumnFormatter().getStyleName(col);
 
             if (
                 (this.selectedColLastStyle == null) ||
                     (this.selectedColLastStyle.length() == 0)) {
                 this.selectedColLastStyle = BoundTable.DEFAULT_STYLE;
             }
 
             table.getColumnFormatter().setStyleName(col, "selected");
         }
     }
 
     private int setSelectedRow(int row) {
         if (((this.masks & BoundTable.HEADER_MASK) > 0) && (row == 0)) {
             return row;
         }
 
         List old = this.getSelected();
 
         if ((this.masks & BoundTable.MULTIROWSELECT_MASK) > 0) {
             if (this.selectedRowStyles.containsKey(new Integer(row))) {
                 //Handle Widget remove on Multirow
                 this.getRowFormatter().setStyleName(
                     row, (String) this.selectedRowStyles.remove(new Integer(row)));
 
                 if ((this.masks & BoundTable.INSERT_WIDGET_MASK) > 0) {
                     this.removeNestedWidget(row);
                 }
             } else {
                 String lastStyle = table.getRowFormatter().getStyleName(row);
                 lastStyle = ((lastStyle == null) || (lastStyle.length() == 0))
                     ? BoundTable.DEFAULT_STYLE : lastStyle;
                 this.selectedRowStyles.put(new Integer(row), lastStyle);
                 this.getRowFormatter().setStyleName(row, "selected");
 
                 if ((this.masks & BoundTable.INSERT_WIDGET_MASK) > 0) {
                     this.insertNestedWidget(row);
                 }
             }
         } else if ((this.masks & BoundTable.NO_SELECT_ROW_MASK) == 0) {
             if (this.selectedRowLastIndex != -1) {
                 this.getRowFormatter().setStyleName(
                     this.selectedRowLastIndex, this.selectedRowLastStyle);
 
                 if ((this.masks & BoundTable.INSERT_WIDGET_MASK) > 0) {
                     this.removeNestedWidget(this.selectedRowLastIndex);
 
                     if (this.selectedRowLastIndex < row) {
                         row--;
                     }
                 }
             }
 
             String currentStyle = table.getRowFormatter().getStyleName(row);
 
             if ((currentStyle == null) || !currentStyle.equals("selected")) {
                 this.selectedRowLastStyle = currentStyle;
             }
 
             if (
                 (this.selectedRowLastStyle == null) ||
                     (this.selectedRowLastStyle.length() == 0)) {
                 this.selectedRowLastStyle = BoundTable.DEFAULT_STYLE;
             }
 
             table.getRowFormatter().setStyleName(row, "selected");
 
             if ((this.masks & BoundTable.INSERT_WIDGET_MASK) > 0) {
                 this.insertNestedWidget(row);
             }
         }
 
         this.selectedRowLastIndex = (this.selectedRowLastIndex == row) ? (-1) : row;
         this.changes.firePropertyChange("selected", old, this.getSelected());
 
         return row;
     }
 
     private void addKeyBinding(KeyBinding binding, Object object) {
         this.externalKeyBindings.put(binding, object);
     }
 
     private void addRow(final Bindable o) {
         int row = table.getRowCount();
 
         if (
             (
                     (((masks & BoundTable.HEADER_MASK) > 0) && (row >= 2)) ||
                     (((masks & BoundTable.HEADER_MASK) == 0) && (row >= 1))
                 ) && ((masks & BoundTable.SPACER_ROW_MASK) > 0)) {
             table.setWidget(row, 0, new Label(""));
             table.getFlexCellFormatter().setColSpan(row, 0, this.columns.length);
             table.getRowFormatter().setStyleName(row, "spacer");
             row++;
         }
 
         
         
         Binding bindingRow = new Binding();
         topBinding.getChildren().add(bindingRow);
 
         int count = topBinding.getChildren().size();
         final Button handle;
         int startColumn = 0;
 
         if ((this.masks & BoundTable.ROW_HANDLE_MASK) > 0) {
             handle = new Button(
                     (this.getActive() && (rowHandles.size() < 9))
                     ? Integer.toString(this.rowHandles.size() + 1) : " ");
             handle.setStyleName("rowHandle");
             handle.addFocusListener(
                 new FocusListener() {
                     public void onFocus(Widget sender) {
                         setActive(true);
 
                         List newSelected = null;
 
                         if ((masks & BoundTable.MULTIROWSELECT_MASK) > 0) {
                             newSelected = new ArrayList(getSelected());
 
                             if (newSelected.contains(o)) {
                                 newSelected.remove(o);
                             } else {
                                 newSelected.add(o);
                             }
                         } else {
                             newSelected = new ArrayList();
                             newSelected.add(o);
                         }
 
                         setSelected(newSelected);
                     }
 
                     public void onLostFocus(Widget sender) {
                     }
                 });
             handle.addClickListener(
                 new ClickListener() {
                     public void onClick(Widget sender) {
                         setActive(true);
 
                         List newSelected = null;
 
                         if ((masks & BoundTable.MULTIROWSELECT_MASK) > 0) {
                             newSelected = new ArrayList(getSelected());
 
                             if (newSelected.contains(o)) {
                                 newSelected.remove(o);
                             } else {
                                 newSelected.add(o);
                             }
                         } else {
                             newSelected = new ArrayList();
                             newSelected.add(o);
                         }
 
                         if (handle != null) {
                             handle.setFocus(true);
                         }
 
                         if (handle != null) {
                             handle.setFocus(true);
                         }
 
                         setSelected(newSelected);
                     }
                 });
             startColumn++;
             this.rowHandles.add(handle);
             this.table.setWidget(row, 0, handle);
         } else {
             handle = null;
         }
 
         if (count < 10) {
             SuggestedKeyBinding kb = new SuggestedKeyBinding(
                    Integer.toString(count).charAt(0), false, true, false );
             Task task = new Task() {
                     public void run() {
                         List newSelected = new ArrayList(getSelected());
 
                         if (newSelected.contains(o)) {
                             newSelected.remove(o);
                         } else {
                             newSelected.add(o);
                         }
 
                         setSelected(newSelected);
 
                         if (handle != null) {
                             handle.setFocus(true);
                         }
                     }
                 };
 
             this.keyBindings.put(kb, task);
 
             if (this.getActive()) {
                 try {
                     KeyboardController.INSTANCE.register(kb, task);
                 } catch (KeyBindingException kbe) {
                     BoundTable.LOG.log(Level.DEBUG, "Unable to register" + kb, kbe);
                 }
             }
         }
 
         for (int col = 0; col < this.columns.length; col++) {
             Widget widget = (Widget) createCellWidget(bindingRow, col, o);
 
             try {
                 table.setWidget(row, col + startColumn, widget);
 
                 if (widget instanceof HasFocus) {
                     addSelectedFocusListener(
                         (HasFocus) widget, topBinding.getChildren().size() - 1,
                         col + startColumn);
                 }
 
                 if (widget instanceof SourcesClickEvents) {
                     addSelectedClickListener(
                         (SourcesClickEvents) widget, topBinding.getChildren().size() - 1,
                         col + startColumn);
                 }
 
                 if (this.columns[col].getStyleName() != null) {
                     table.getCellFormatter().setStyleName(
                         row, col + startColumn, this.columns[col].getStyleName());
                 }
             } catch (RuntimeException e) {
                 BoundTable.LOG.log(Level.ERROR, widget + "", e);
             }
         }
 
         boolean odd = (this.calculateRowToObjectOffset(new Integer(row)).intValue() % 2) != 0;
         this.table.getRowFormatter().setStyleName(row, odd ? "odd" : "even");
 
         if (this.isAttached()) {
             bindingRow.setLeft();
             bindingRow.bind();
         }
     }
 
     private void addSelectedClickListener(
         final SourcesClickEvents widget, final int objectNumber, final int col) {
         ClickListener l = new ClickListener() {
                 public void onClick(Widget sender) {
                     setActive(true);
 
                     int row = calculateObjectToRowOffset(objectNumber);
                     handleSelect(true, row, col);
                 }
             };
 
         widget.addClickListener(l);
         clickListeners.put(widget, l);
     }
 
     private void addSelectedFocusListener(
         final HasFocus widget, final int objectNumber, final int col) {
         FocusListener l = new FocusListener() {
                 public void onLostFocus(Widget sender) {
                 }
 
                 public void onFocus(Widget sender) {
                     setActive(true);
 
                     int row = calculateObjectToRowOffset(objectNumber);
 
                     //GWT.log("Focus row: " + row + " object: " + objectNumber
                     //        + " col: " + col, null);
                     //GWT.log("SelectedRowLastIndex " + selectedRowLastIndex, null);
                     handleSelect(row != selectedRowLastIndex, row, col);
                 }
             };
 
         widget.addFocusListener(l);
         focusListeners.put(widget, l);
     }
 
     private int calculateObjectToRowOffset(int row) {
         if ((masks & BoundTable.SPACER_ROW_MASK) > 0) {
             row += row;
         }
 
         if ((masks & BoundTable.HEADER_MASK) > 0) {
             row++;
         }
 
         //GWT.log( "Row before: "+ row, null);
         if ((masks & BoundTable.INSERT_WIDGET_MASK) > 0) {
             //if( (masks & BoundTable.MULTIROWSELECT_MASK) > 0){
             //   row += this.selectedRowsBeforeObjectRow(row);
             //} else {
             row += this.selectedRowsBeforeRow(row);
 
             //}
         }
 
         //GWT.log( "Row after "+ row, null);
         return row;
     }
 
     private Integer calculateRowToObjectOffset(Integer rowNumber) {
         int row = rowNumber.intValue();
 
         if ((masks & BoundTable.HEADER_MASK) > 0) {
             row -= 1;
         }
 
         if ((masks & BoundTable.SPACER_ROW_MASK) > 0) {
             row -= (row / 2);
         }
 
         //GWT.log( "Selected rows before row "+row+" "+ this.selectedRowsBeforeRow( row ), null );
         if (
             ((masks & BoundTable.INSERT_WIDGET_MASK) > 0) &&
                 ((masks & BoundTable.MULTIROWSELECT_MASK) > 0)) {
             //GWT.log( "At"+ row+ " Removing: "+this.selectedRowsBeforeRow(row), null );
             row -= this.selectedRowsBeforeRow(row);
         }
 
         //GWT.log( "Returning object instance index: "+row, null);
         return new Integer(row);
     }
 
     private void clearSelectedCell() {
         if ((this.selectedColLastIndex != -1) && (this.selectedCellRowLastIndex != -1)) {
             this.getCellFormatter().setStyleName(
                 this.selectedCellRowLastIndex, this.selectedColLastIndex,
                 this.selectedCellLastStyle);
         }
 
         this.selectedColLastIndex = -1;
         this.selectedCellRowLastIndex = -1;
     }
 
     private void clearSelectedCol() {
         if (this.selectedColLastIndex != -1) {
             this.getColumnFormatter().setStyleName(
                 this.selectedColLastIndex, this.selectedColLastStyle);
         }
     }
 
     private void clearSelectedRows() {
         this.clearSelectedCol();
         this.clearSelectedCell();
 
         List old = this.getSelected();
 
         if ((this.masks & BoundTable.INSERT_WIDGET_MASK) > 0) {
             List removeRows = new ArrayList();
 
             if (this.selectedRowStyles != null) {
                 removeRows.addAll(this.selectedRowStyles.keySet());
             } else {
                 removeRows.add(new Integer(this.selectedRowLastIndex));
             }
 
             for (int i = removeRows.size() - 1; i >= 0; i--) {
                 //GWT.log("Removing nested: " + removeRows.get(i), null);
                 this.removeNestedWidget(((Integer) removeRows.get(i)).intValue());
             }
         }
 
         if ((this.masks & BoundTable.MULTIROWSELECT_MASK) > 0) {
             for (Iterator it = this.selectedRowStyles.entrySet().iterator();
                     it.hasNext();) {
                 Entry entry = (Entry) it.next();
                 int row = ((Integer) entry.getKey()).intValue();
                 this.table.getRowFormatter().setStyleName(row, (String) entry.getValue());
             }
 
             this.selectedRowStyles.clear();
         } else if ((this.masks & BoundTable.NO_SELECT_ROW_MASK) == 0) {
             if (this.selectedRowLastIndex != -1) {
                 this.getRowFormatter().setStyleName(
                     this.selectedRowLastIndex, this.selectedRowLastStyle);
                 this.selectedRowLastStyle = BoundTable.DEFAULT_STYLE;
             }
         }
 
         this.selectedRowLastIndex = -1;
         this.selectedCellRowLastIndex = -1;
 
         this.changes.firePropertyChange("selected", old, this.getSelected());
     }
 
     private BoundWidget createCellWidget(Binding rowBinding,  int colIndex , Bindable target) {
         final BoundWidget widget;
         Field col = this.columns[colIndex];
         BoundWidget[] rowWidgets = (BoundWidget[]) widgetCache.get(target);
 
         if (rowWidgets == null) {
             rowWidgets = new BoundWidget[ this.columns.length ];
             widgetCache.put(target, rowWidgets);
         }
 
         if (rowWidgets[colIndex] != null) {
             widget = rowWidgets[colIndex];
             BoundTable.LOG.log(
                 Level.SPAM,
                 "Using cache widget for " + target + "." + col.getPropertyName(), null);
         } else {
             if (col.getCellProvider() != null) {
                 widget = col.getCellProvider().get();
             } else {
                 final Property p = Introspector.INSTANCE.getDescriptor(target)
                                                         .getProperty(
                         col.getPropertyName());
                 widget = this.factory.getWidgetProvider(
                         col.getPropertyName(), p.getType()).get();
 
                 // TODO Figure out some way to make this read only.
             }
 
             rowWidgets[colIndex]=widget;
             BoundTable.LOG.log(
                 Level.SPAM, "Creating widget for " + target + "." +
                 col.getPropertyName(), null);
         }
 
         Binding[] bindings = (Binding[]) this.bindingCache.get( target );
         if( bindings == null ){
             bindings = new Binding[this.columns.length];
             this.bindingCache.put( target, bindings );
         }
         if( bindings[colIndex] == null ){
             bindings[colIndex] = new Binding(
                     widget, "value", col.getValidator(), col.getFeedback(), target,
                     col.getPropertyName(), null, null);
             BoundTable.LOG.log(Level.SPAM, "Created binding " + bindings[colIndex], null);
         }
         widget.setModel(target);
         rowBinding.getChildren().add(bindings[colIndex]);
 
         return widget;
     }
 
     private Widget createNavWidget() {
         Grid p = new Grid(1, 5);
         p.setStyleName(BoundTable.NAV_STYLE);
 
         Button b = new Button(
                 "<<",
                 new ClickListener() {
                     public void onClick(Widget sender) {
                         first();
                     }
                 });
         b.setStyleName(BoundTable.NAV_STYLE);
 
         if (this.getCurrentChunk() == 0) {
             b.setEnabled(false);
         }
 
         p.setWidget(0, 0, b);
         b = new Button(
                 "<",
                 new ClickListener() {
                     public void onClick(Widget sender) {
                         previous();
                     }
                 });
         b.setStyleName(BoundTable.NAV_STYLE);
 
         if (this.getCurrentChunk() == 0) {
             b.setEnabled(false);
         }
 
         p.setWidget(0, 1, b);
 
         b = new Button(
                 ">",
                 new ClickListener() {
                     public void onClick(Widget sender) {
                         next();
                     }
                 });
         b.setStyleName(BoundTable.NAV_STYLE);
 
         if (this.getCurrentChunk() == (this.getNumberOfChunks() - 1)) {
             b.setEnabled(false);
         }
 
         Label l = new Label(
                 (this.getCurrentChunk() + 1) + " / " + this.getNumberOfChunks());
         p.setWidget(0, 2, l);
         p.getCellFormatter().setHorizontalAlignment(
             0, 2, HasHorizontalAlignment.ALIGN_CENTER);
 
         p.setWidget(0, 3, b);
         b = new Button(
                 ">>",
                 new ClickListener() {
                     public void onClick(Widget sender) {
                         last();
                     }
                 });
         b.setStyleName(BoundTable.NAV_STYLE);
 
         if (this.getCurrentChunk() == (this.getNumberOfChunks() - 1)) {
             b.setEnabled(false);
         }
 
         p.setWidget(0, 4, b);
 
         return p;
     }
 
     private void handleSelect(boolean toggleRow, int row, int col) {
         int calcRow = row;
 
         //GWT.log( "Toggle row "+ toggleRow, null );
         //GWT.log( " ON "+row+", "+col, new RuntimeException() );
         if ((this.masks & BoundTable.INSERT_WIDGET_MASK) > 0) {
             if (
                 (
                         (this.selectedRowStyles == null) &&
                         (this.selectedRowLastIndex != -1) &&
                         (this.selectedRowLastIndex == (row - 1))
                     ) ||
                     (
                         (this.selectedRowStyles != null) &&
                         this.selectedRowStyles.containsKey(new Integer(row - 1))
                     )) {
                 return;
             }
 
             calcRow = row - this.selectedRowsBeforeRow(row);
         }
 
         if (
             (
                     ((masks & BoundTable.SPACER_ROW_MASK) == 0) &&
                     (
                         (((masks & BoundTable.HEADER_MASK) > 0) && (calcRow > 0)) ||
                         ((masks & BoundTable.HEADER_MASK) == 0)
                     )
                 ) || (((masks & BoundTable.HEADER_MASK) > 0) & ((calcRow % 2) != 0)) ||
                 (((masks & BoundTable.HEADER_MASK) == 0) && ((calcRow % 2) != 1))) {
             //GWT.log( "Inside" , null);
             if (
                 (
                         toggleRow &&
                         (
                             ((masks & BoundTable.MULTIROWSELECT_MASK) == 0) &&
                             (row != this.selectedCellRowLastIndex)
                         )
                     ) || (((masks & BoundTable.MULTIROWSELECT_MASK) > 0) && toggleRow)) {
                 //if( toggleRow || (masks & BoundTable.MULTIROWSELECT_MASK) == 0){
                 row = setSelectedRow(row);
             }
 
             setSelectedCell(row, col);
 
             setSelectedCol(col);
 
             this.selectedCellRowLastIndex = row;
         }
     }
 
     private void init(int masksValue) {
         final BoundTable instance = this;
         this.topBinding = new Binding();
         this.masks = masksValue;
         this.factory = (this.factory == null) ? new BoundWidgetTypeFactory(true)
                                               : this.factory;
 
         if (((this.masks & BoundTable.SORT_MASK) > 0) && (this.columns != null)) {
             this.ascending = new boolean[this.columns.length];
         }
 
         if ((this.masks & BoundTable.MULTIROWSELECT_MASK) > 0) {
             this.selectedRowStyles = new HashMap();
         }
 
         if (
             ((this.masks & BoundTable.ROW_HANDLE_MASK) > 0) &&
                 ((this.masks & BoundTable.MULTIROWSELECT_MASK) > 0)) {
             this.allRowsHandle = new Button(
                     "  ",
                     new ClickListener() {
                         public void onClick(Widget sender) {
                             if ((getSelected() != null) && (getSelected().size() == 0)) {
                                 setSelected(new ArrayList((Collection) getValue()));
                             } else {
                                 setSelected(new ArrayList());
                             }
                         }
                     });
             this.allRowsHandle.setStyleName("rowHandle");
             this.allRowsHandle.setHeight("100%");
             this.allRowsHandle.setWidth("100%");
 
             if ((this.masks & BoundTable.MULTIROWSELECT_MASK) == 0) {
                 this.allRowsHandle.setEnabled(false);
             }
         }
 
         if ((this.masks & BoundTable.ROW_HANDLE_MASK) > 0) {
             this.rowHandles = new ArrayList();
         }
 
         this.table = new FlexTable();
         this.table.setCellPadding(0);
         this.table.setCellSpacing(0);
 
         if ((masks & BoundTable.SCROLL_MASK) > 0) {
             this.scroll = new ScrollPanel();
             this.scroll.setWidget(table);
             super.initWidget(scroll);
 
             scroll.addScrollListener(
                 new ScrollListener() {
                     public void onScroll(Widget widget, int scrollLeft, int scrollTop) {
                         //GWT.log("HasProvider: " + (provider != null), null);
                         if (
                             (provider != null) && (inChunk == false) &&
                                 (
                                     scrollTop >= (
                                         table.getOffsetHeight() -
                                         scroll.getOffsetHeight()
                                     )
                                 )) {
                             //GWT.log("Scroll Event fired. ", null);
                             lastScrollPosition = scrollTop - 1;
                             next();
                         }
                     }
                 });
         } else {
             super.initWidget(table);
         }
 
         this.table.setCellSpacing(0);
         table.addTableListener(
             new TableListener() {
                 public void onCellClicked(SourcesTableEvents sender, int row, int cell) {
                     setActive(true);
 
                     int startColumn = ((masks & BoundTable.ROW_HANDLE_MASK) > 0) ? 1 : 0;
 
                     if (startColumn == 0) {
                         handleSelect(true, row, cell);
                     }
 
                     if (
                         ((masks & BoundTable.SORT_MASK) > 0) &&
                             ((masks & BoundTable.HEADER_MASK) > 0) && (row == 0)) {
                         sortColumn(cell - startColumn);
                     }
                 }
             });
         this.base = ((this.scroll == null) ? (Widget) this.table : (Widget) this.scroll);
         this.value = (this.value == null) ? new ArrayList() : this.value;
         this.columns = (this.columns == null) ? new Field[0] : this.columns;
         this.setStyleName("gwittir-BoundTable");
 
         if ((this.provider != null) && (this.getCurrentChunk() == -1)) {
             this.provider.init(this);
             this.inChunk = true;
         }
 
         this.addPropertyChangeListener(
             "selected",
             new PropertyChangeListener() {
                 public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                     if (getAction() != null) {
                         getAction().execute(instance);
                     }
                 }
             });
         this.addPropertyChangeListener(
             "active",
             new PropertyChangeListener() {
                 public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                     boolean newActive = ((Boolean) propertyChangeEvent.getNewValue()).booleanValue();
 
                     if ((masks & BoundTable.ROW_HANDLE_MASK) > 0) {
                         for (int i = 0; i < rowHandles.size(); i++) {
                             ((Button) rowHandles.get(i)).setText(
                                 (newActive && (i <= 8)) ? Integer.toString(i + 1) : " ");
                         }
                     }
 
                     for (Iterator it = keyBindings.entrySet().iterator(); it.hasNext();) {
                         Entry entry = (Entry) it.next();
                         handleBinding(newActive, entry);
                     }
 
                     for (
                         Iterator it = externalKeyBindings.entrySet().iterator();
                             it.hasNext();) {
                         Entry entry = (Entry) it.next();
                         handleBinding(newActive, entry);
                     }
                 }
 
                 private void handleBinding(boolean newActive, Entry entry) {
                     KeyBinding kb = (KeyBinding) entry.getKey();
                     Object execute = entry.getValue();
 
                     if (newActive) {
                         BoundTable.LOG.log(Level.SPAM, "Registering " + kb, null);
 
                         try {
                             if (execute instanceof Task) {
                                 KeyboardController.INSTANCE.register(kb, (Task) execute);
                             } else if (execute instanceof Action) {
                                 KeyboardController.INSTANCE.register(
                                     kb, (Action) execute);
                             } else if (execute instanceof BoundWidget) {
                                 KeyboardController.INSTANCE.register(
                                     kb, (BoundWidget) execute);
                             }
                         } catch (KeyBindingException kbe) {
                             BoundTable.LOG.log(
                                 Level.DEBUG, "Unable to register" + kb, kbe);
                         }
                     } else {
                         boolean result = KeyboardController.INSTANCE.unregister(kb);
                         BoundTable.LOG.log(
                             Level.SPAM, "Unregistering " + kb + " " + result, null);
                     }
                 }
             });
     }
 
     private void insertNestedWidget(int row) {
         //GWT.log( "Inserting nested for row "+row, null);
         Integer realIndex = this.calculateRowToObjectOffset(new Integer(row));
 
         //GWT.log( "RealIndex: "+ realIndex, null );
         int i = 0;
         Bindable o = null;
 
         for (Iterator it = this.topBinding.getChildren().iterator(); it.hasNext(); i++) {
             if (realIndex.intValue() == i) {
                 o = ((Binding) ((Binding) it.next()).getChildren().get(0)).getRight().object;
 
                 break;
             } else {
                 it.next();
             }
         }
 
         BoundWidget widget = this.factory.getWidgetProvider(
                 Introspector.INSTANCE.resolveClass(o)).get();
         widget.setModel(o);
         this.table.insertRow(row + 1);
         this.table.setWidget(row + 1, 0, (Widget) widget);
         this.table.getFlexCellFormatter().setColSpan(row + 1, 0, this.columns.length + 1);
         this.table.getCellFormatter().setStyleName(row + 1, 0, "expanded");
         this.modifySelectedIndexes(row, +1);
     }
 
     private void modifySelectedIndexes(int fromRow, int modifier) {
         if (this.selectedRowLastIndex > fromRow) {
             this.selectedRowLastIndex += modifier;
         }
 
         if (this.selectedCellRowLastIndex > fromRow) {
             this.selectedCellRowLastIndex += modifier;
         }
 
         if (this.selectedRowStyles == null) {
             return;
         }
 
         HashMap newSelectedRowStyles = new HashMap();
 
         for (Iterator it = this.selectedRowStyles.entrySet().iterator(); it.hasNext();) {
             Entry entry = (Entry) it.next();
             Integer entryRow = (Integer) entry.getKey();
 
             if (entryRow.intValue() > fromRow) {
                 newSelectedRowStyles.put(
                     new Integer(entryRow.intValue() + modifier), entry.getValue());
             } else {
                 newSelectedRowStyles.put(entryRow, entry.getValue());
             }
         }
 
         this.selectedRowStyles = newSelectedRowStyles;
     }
 
     private void removeNestedWidget(int row) {
         this.modifySelectedIndexes(row, -1);
         this.table.removeRow(row + 1);
     }
 
     private void renderAll() {
         this.clear();
 
         int startColumn = 0;
 
         if ((this.masks & BoundTable.ROW_HANDLE_MASK) > 0) {
             this.table.setWidget(0, 0, this.allRowsHandle);
             startColumn = 1;
         }
 
         if ((this.masks & BoundTable.HEADER_MASK) > 0) {
             for (int i = 0; i < this.columns.length; i++) {
                 this.table.setWidget(
                     0, i + startColumn, new Label(this.columns[i].getLabel()));
             }
 
             this.table.getRowFormatter().setStyleName(0, "header");
         }
 
         for (
             Iterator it = (this.value == null) ? null : this.value.iterator();
                 (it != null) && it.hasNext();) {
             this.addRow((Bindable) it.next());
         }
 
         if (
             (this.provider != null) && ((this.masks & BoundTable.SCROLL_MASK) == 0) &&
                 ((this.masks & BoundTable.NO_NAV_ROW_MASK) == 0)) {
             int row = this.table.getRowCount();
             this.table.setWidget(row, 0, this.createNavWidget());
             this.table.getFlexCellFormatter().setColSpan(row, 0, this.columns.length);
             table.getCellFormatter().setHorizontalAlignment(
                 row, 0, HasHorizontalAlignment.ALIGN_CENTER);
         }
 
         //GWT.log(this.toString(), null);
     }
 
     private int selectedRowsBeforeRow(int row) {
         //GWT.log( "=======Selected rows before "+row, null);
         //GWT.log( "=======lastRow "+this.selectedRowLastIndex, null );
         if (this.selectedRowStyles == null) {
             return (
                 (this.selectedRowLastIndex == -1) || (this.selectedRowLastIndex >= row)
             ) ? 0 : 1;
         }
 
         int count = 0;
 
         for (Iterator it = this.selectedRowStyles.keySet().iterator(); it.hasNext();) {
             if (((Integer) it.next()).intValue() < row) {
                 count++;
             }
         }
 
         return count;
     }
 }
