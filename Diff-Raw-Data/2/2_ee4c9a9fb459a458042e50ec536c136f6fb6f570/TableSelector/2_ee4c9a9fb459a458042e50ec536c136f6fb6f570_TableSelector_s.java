 /**
  * Copyright (C) 2009 (nick @ objectdefinitions.com)
  *
  * This file is part of JTimeseries.
  *
  * JTimeseries is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * JTimeseries is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with JTimeseries.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.od.jtimeseries.ui.selector.selectorpanel;
 
 import com.od.jtimeseries.context.TimeSeriesContext;
 import com.od.jtimeseries.ui.timeseries.RemoteChartingTimeSeries;
 import com.od.swing.action.ListSelectionActionModel;
 import com.od.jtimeseries.ui.util.PopupTriggerMouseAdapter;
 import com.jidesoft.grid.*;
 
 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.TableModelEvent;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumnModel;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableCellEditor;
 import java.awt.*;
 import java.util.*;
 import java.beans.IntrospectionException;
 
 /**
  * Created by IntelliJ IDEA.
  * User: nick
  * Date: 25-May-2009
  * Time: 11:37:55
  * To change this template use File | Settings | File Templates.
  */
 public class TableSelector extends SelectorPanel {
 
     private static final Color STALE_SERIES_COLOR = new Color(248,165,169);
     private TimeSeriesContext rootContext;
     private java.util.List<Action> seriesActions;
     private String selectionText;
     private RemoteSeriesTableModel tableModel;
     private SortableTable sortableTable;
     private JPopupMenu tablePopupMenu;
 
     private java.util.List<ColumnInfo> columns = new ArrayList<ColumnInfo>();
     private Map<String, Integer> columnWidthsByColumnName = new HashMap<String,Integer>();
 
     public TableSelector(ListSelectionActionModel<RemoteChartingTimeSeries> seriesActionModel,
                          TimeSeriesContext rootContext,
                          java.util.List<Action> seriesActions,
                          String selectionText) {
 
         super(seriesActionModel);
         this.rootContext = rootContext;
         this.seriesActions = seriesActions;
         this.selectionText = selectionText;
 
         buildColumnList();
         createTableModels();
         refreshSeries();
         createPopupMenu();
         createTable();
         sizeColumns();
 
         sortableTable.addMouseListener(
             new PopupTriggerMouseAdapter(tablePopupMenu, sortableTable)
         );
         setLayout(new BorderLayout());
         add(new JScrollPane(sortableTable), BorderLayout.CENTER);
         addSeriesSelectionListener();
     }
 
     //for each predetermined column, the bean property name RemoteChartingTimeSeries,
     //column name to display and a preferred width. We use this to create the BeanTableModel
     //and size the columns in ColumnModel. The underlying table model also generates some
     //columns dynamically from the tokens in the series path
     private void buildColumnList() {
         columns.add(new ColumnInfo("selected", selectionText, 65));
         columns.add(new ColumnInfo("displayName", "Display Name", 175));
         columns.add(new ColumnInfo("id", "Id", 75));
         columns.add(new ColumnInfo("maxDaysHistory", "Max Days", 100));
         columns.add(new ColumnInfo("refreshTimeSeconds", "Refresh(s)", 100));
        columns.add(new ColumnInfo("contextPath", "Path", 100));
         columns.add(new ColumnInfo("URL", "URL", 100));
         populateColumnWidthsMap();
     }
 
     //we don't currently persist users changes to column order/sizes, but perhaps we should..
     private void sizeColumns() {
         if ( sortableTable != null) { //never apart from on creation of SortableTable
             TableColumnModel m = sortableTable.getColumnModel();
             Enumeration<TableColumn> e = m.getColumns();
             while(e.hasMoreElements()) {
                 TableColumn col = e.nextElement();
                 String name = tableModel.getColumnName(col.getModelIndex());
                 if ( columnWidthsByColumnName.containsKey(name)) {
                     col.setPreferredWidth(columnWidthsByColumnName.get(name));
                 }
             }
         }
     }
 
     private void populateColumnWidthsMap() {
         for ( ColumnInfo c : columns) {
             columnWidthsByColumnName.put(c.getDisplayName(), c.getPreferredWidth());
         }
     }
 
     private void createPopupMenu() {
         tablePopupMenu = new JPopupMenu("Series Actions");
         for ( Action a : seriesActions) {
             tablePopupMenu.add(a);
         }
     }
 
     private void createTable() {
         sortableTable = new SortableTable(tableModel) {
             public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                 Component c = super.prepareRenderer(renderer, row, column);
                 if (! ((RemoteChartingTimeSeries) tableModel.getObject(row)).isConnected()) {
                     c.setBackground(STALE_SERIES_COLOR);
                 } else {
                     if (isCellSelected(row, column)) {
                         c.setBackground(sortableTable.getSelectionBackground());
                     } else {
                         c.setBackground(sortableTable.getBackground());
                     }
                 }
                 return c;
             }
 
             public void tableChanged(TableModelEvent e) {
                 super.tableChanged(e);
                 if ( e.getFirstRow() == TableModelEvent.HEADER_ROW ) {
                     sizeColumns();
                 }
             }
         };
 
         sortableTable.setClearSelectionOnTableDataChanges(false);
         sortableTable.setRowResizable(true);
         sortableTable.setVariousRowHeights(true);
         sortableTable.setSelectInsertedRows(false);
         sortableTable.setAutoSelectTextWhenStartsEditing(true);
         sortableTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 
         AutoFilterTableHeader header = new AutoFilterTableHeader(sortableTable);
         header.setAutoFilterEnabled(true);
         header.setShowFilterName(true);
         header.setAllowMultipleValues(true);
         header.setShowFilterNameAsToolTip(true);
         sortableTable.setTableHeader(header);
     }
 
     private void createTableModels() {
         BeanTableModel<RemoteChartingTimeSeries> model = null;
         String[] colConfigString = generateColumnConfigStringForBeanTableModel();
         try {
             model = new BeanTableModel<RemoteChartingTimeSeries>(
                 new ArrayList(),
                 RemoteChartingTimeSeries.class,
                 colConfigString
             );
         } catch (IntrospectionException e) {
             e.printStackTrace();
         }
         int[] editableCols = new int[] {0, 1, 3, 4};
         tableModel = new RemoteSeriesTableModel(model, editableCols);
     }
 
     //jide BeanTableModel requires the propertyNames and column display names as a String[]
     private String[] generateColumnConfigStringForBeanTableModel() {
         java.util.List<String> colConfigStrings = new LinkedList<String>();
         for(ColumnInfo c : columns) {
             colConfigStrings.add(c.getPropertyName());
             colConfigStrings.add(c.getDisplayName());
         }
         String[] colConfigString = colConfigStrings.toArray(new String[colConfigStrings.size()]);
         return colConfigString;
     }
 
     private void addSeriesSelectionListener() {
         sortableTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
         sortableTable.getSelectionModel().addListSelectionListener(
             new ListSelectionListener() {
                 public void valueChanged(ListSelectionEvent e) {
                     if ( sortableTable.getSelectedRow() > -1 ) {
                         RemoteChartingTimeSeries series = (RemoteChartingTimeSeries)tableModel.getObject(sortableTable.getSelectedRow());
                         getSeriesActionModel().setSelected(series);
                         fireSelectedForDescription(series);
                     }
                 }
             }
         );
     }
 
     public void refreshSeries() {
         tableModel.clearTable();
         tableModel.addRowData(rootContext.findAllTimeSeries().getAllMatches().toArray());
     }
 
     public void removeSeries(java.util.List<RemoteChartingTimeSeries> series) {
         tableModel.removeRowData(series.toArray(new RemoteChartingTimeSeries[series.size()]));
     }
 
     private static class ColumnInfo {
         private String propertyName;
         private String displayName;
         private int preferredWidth;
 
         private ColumnInfo(String propertyName, String displayName, int preferredWidth) {
             this.propertyName = propertyName;
             this.displayName = displayName;
             this.preferredWidth = preferredWidth;
         }
 
         public String getPropertyName() {
             return propertyName;
         }
 
         public String getDisplayName() {
             return displayName;
         }
 
         public int getPreferredWidth() {
             return preferredWidth;
         }
     }
 }
