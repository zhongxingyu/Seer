 package org.amanzi.awe.views.calls.views;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.refractions.udig.project.IMap;
 import net.refractions.udig.project.ui.ApplicationGIS;
 
 import org.amanzi.awe.catalog.neo.NeoCatalogPlugin;
 import org.amanzi.awe.catalog.neo.upd_layers.events.ChangeSelectionEvent;
 import org.amanzi.awe.statistic.CallTimePeriods;
 import org.amanzi.awe.views.calls.CallAnalyserPlugin;
 import org.amanzi.awe.views.calls.ExportSpreadsheetWizard;
 import org.amanzi.awe.views.calls.Messages;
 import org.amanzi.awe.views.calls.enums.AggregationCallTypes;
 import org.amanzi.awe.views.calls.enums.IStatisticsHeader;
 import org.amanzi.awe.views.calls.enums.StatisticsCallType;
 import org.amanzi.awe.views.calls.enums.StatisticsType;
 import org.amanzi.awe.views.calls.statistics.CallStatistics;
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.database.services.events.ShowPreparedViewEvent;
 import org.amanzi.neo.core.database.services.events.UpdateDrillDownEvent;
 import org.amanzi.neo.core.enums.CallProperties;
 import org.amanzi.neo.core.enums.DriveTypes;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.enums.ProbeCallRelationshipType;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.utils.ActionUtil;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.core.utils.Pair;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableColorProvider;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.TableCursor;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.DateTime;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.part.ViewPart;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.TraversalPosition;
 import org.neo4j.graphdb.Traverser;
 import org.neo4j.graphdb.Traverser.Order;
 
 /**
  * <p>
  * Call Analyser view
  * </p>
  * 
  * @author Cinkel_A
  * @since 1.0.0
  */
 public class CallAnalyserView extends ViewPart {
 
     /** String DRIVE_ID field */
     private static final String DRIVE_ID = "org.amanzi.awe.views.tree.drive.views.DriveTreeView";
     /** String ERROR_VALUE field */
     private static final String ERROR_VALUE = Messages.CAV_ERROR_VALUE;
     private static final String ALL_VALUE = Messages.CAV_ALL_VALUE;
     // row labels
     private static final String LBL_DRIVE = Messages.CAV_LBL_DRIVE;
     private static final String LBL_PROBE = Messages.CAV_LBL_PROBE;
     private static final String LBL_PERIOD = Messages.CAV_LBL_PERIOD;
     private static final String LBL_CALL_TYPE = Messages.CAV_LBL_CALL_TYPE;
     private static final String LBL_START_TIME = Messages.CAV_LBL_START_TIME;
     private static final String LBL_END_TIME = Messages.CAV_LBL_END_TIME;
 
     // column name
     private static final String COL_PERIOD = Messages.CAV_COL_PERIOD;
     private static final String COL_HOST = Messages.CAV_COL_HOST;
 
     /**
      * The ID of the view as specified by the extension.
      */
     public static final String ID = "org.amanzi.awe.views.calls.views.CallAnalyserView";
 
     private static final int MIN_FIELD_WIDTH = 150;
     private static final int MIN_COLUMN_WIDTH = 220;
     public static final int DEF_SIZE = 100;
     private static final String KEY_ALL = ALL_VALUE;
     public static final int MAX_TABLE_LEN = 500;
     private static final String LB_EXPORT = "Export";
     private List<ColumnHeaders> columnHeaders = new ArrayList<ColumnHeaders>();
     private LinkedHashMap<String, Node> callDataset = new LinkedHashMap<String, Node>();
     private LinkedHashMap<String, Node> probeCallDataset = new LinkedHashMap<String, Node>();
 
     private Combo cDrive;
 
     private TableViewer tableViewer;
     private Combo cProbe;    
     private ViewContentProvider provider;
     private ViewLabelProvider labelProvider;
     private Combo cPeriod;
     private Combo cCallType;
     private TableCursor cursor;
     private Button bExport;
     private Color color1;
     private Color color2;
     private Comparator<PeriodWrapper> comparator;
     private List<Integer> sortedColumns = new LinkedList<Integer>();
     private Composite frame;
     private Composite parent;
     private DateTime dateStart;
     private DateTime timeStart;
     private DateTime dateEnd;
     private DateTime timeEnd;
 
     
     private enum SortOrder{
         NONE("icons/None.gif"),
         ASC("icons/Asc.png"),
         DESC("icons/Desc.png");
         private String iconPath;
         
         private SortOrder(String icon) {
             iconPath = icon;
         }
         
         /**
          * @return Returns the iconPath.
          */
         public String getIconPath() {
             return iconPath;
         }
     }
 
     /*
      * The content provider class is responsible for providing objects to the view. It can wrap
      * existing objects in adapters or simply return objects as-is. These objects may be sensitive
      * to the current input of the view, or ignore it and always show the same content (like Task
      * List, for example).
      */
 
     class ViewContentProvider implements IStructuredContentProvider {
         private List<PeriodWrapper> elements = new ArrayList<PeriodWrapper>();
 
         public void inputChanged(Viewer v, Object oldInput, Object newInput) {
             elements.clear();
             if (newInput == null || !(newInput instanceof InputWrapper)) {
                 return;
             }
             InputWrapper inputWr = (InputWrapper)newInput;
             if (!inputWr.isCorrectInput()) {
                 return;
             }
             GraphDatabaseService service = NeoServiceProvider.getProvider().getService();
             Traverser sRowTraverser = inputWr.getSrowTraverser(service);
             Transaction tx = service.beginTx();
             try {                
                 if (sRowTraverser != null) {
                     StatisticsCallType callType = getCallType();
                     for (Node sRow : sRowTraverser) {
                         if (isRowInTime(sRow)) {
                             elements.add(new PeriodWrapper(sRow, callType));
                         }
                     }
                 }
             } finally {
                 tx.finish();
             }
             sort();
 
         }
         
         private boolean isRowInTime(Node row){
             Long start = getStartTime();
             Long end = getEndTime();
             Long time = (Long)row.getProperty(INeoConstants.PROPERTY_TIME_NAME, null);            
             return (time!=null)&&((start==null||start<=time)&&(end==null||time<=end));
         }
 
         public void dispose() {
         }
 
         public Object[] getElements(Object parent) {
             return elements.toArray(new PeriodWrapper[0]);
         }
 
         /**
          *sort rows
          */
         public void sort() {
             Collections.sort(elements, comparator);
             if (elements.isEmpty()) {
                 return;
             }
             Color color = color1;
             elements.get(0).setColor(color);
             for (int i = 1; i < elements.size(); i++) {
                 if (comparator.compare(elements.get(i - 1), elements.get(i)) != 0) {
                     color = color == color1 ? color2 : color1;
                 }
                 elements.get(i).setColor(color);
             }
         }
     }
 
     /**
      * <p>
      * Table Label provider
      * </p>
      * 
      * @author Cinkel_A
      * @since 1.0.0
      */
     class ViewLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider {
 
         private List<TableColumn> columns = new ArrayList<TableColumn>();
 
         public String getColumnText(Object obj, int index) {
             if (obj instanceof PeriodWrapper && index<columnHeaders.size()) {
                 PeriodWrapper period = (PeriodWrapper)obj;
                 return columnHeaders.get(index).getValue(period, index);
             } else {
                 return getText(obj);
             }
         }
 
         public Image getColumnImage(Object obj, int index) {
             return null;
         }
 
         /**
          *create column of table init label provider of tibleView
          */
         public void createTableColumn() {
             Table tabl = tableViewer.getTable();
             TableViewerColumn column;
             TableColumn col;
 
             if (columnHeaders.isEmpty()) {
                 column = new TableViewerColumn(tableViewer, SWT.LEFT);
                 col = column.getColumn();
                 col.setText(COL_PERIOD);
                 columnHeaders.add(new ColumnHeaders(col, null));
                 col.setWidth(DEF_SIZE);
                 col.addSelectionListener(new SelectionListener() {
 
                     @Override
                     public void widgetSelected(SelectionEvent e) {
                         sortedColumns = new LinkedList<Integer>();
                         if (provider != null) {
                             provider.sort();
                         }
                         tableViewer.refresh();
                         tableViewer.getTable().showSelection();
                     }
 
                     @Override
                     public void widgetDefaultSelected(SelectionEvent e) {
                         widgetSelected(e);
                     }
                 });
                 columns.add(col);
                 column = new TableViewerColumn(tableViewer, SWT.LEFT);
                 col = column.getColumn();
                 col.setText(COL_HOST);
                 columnHeaders.add(new ColumnHeaders(col, null));
                 col.setWidth(DEF_SIZE);
                 columns.add(col);
                 column = new TableViewerColumn(tableViewer, SWT.LEFT);
                 col = column.getColumn();
                 col.setText(INeoConstants.PROBE_LA);
                 columnHeaders.add(new ColumnHeaders(col, null));
                 col.setWidth(DEF_SIZE);
                 columns.add(col);
                 column = new TableViewerColumn(tableViewer, SWT.LEFT);
                 col = column.getColumn();
                 col.setText(INeoConstants.PROBE_F);
                 columnHeaders.add(new ColumnHeaders(col, null));
                 col.setWidth(DEF_SIZE);
                 columns.add(col);
                 // TODO move creation of group of single property in one method
                 //
                 StatisticsCallType callType = StatisticsCallType.INDIVIDUAL;
                 for(StatisticsCallType type : StatisticsCallType.values()){
                     if(type.getHeaders().size()>callType.getHeaders().size()){
                         callType = type;
                     }
                 }
                 for (IStatisticsHeader columnHeader : callType.getHeaders()) {
                     column = new TableViewerColumn(tableViewer, SWT.LEFT);
                     col = column.getColumn();             
                     String title = columnHeader.getTitle();
                     GC gc = new GC(col.getParent());
                     col.setText(title);
                     columnHeaders.add(new ColumnHeaders(col, columnHeader));
                     col.setWidth(gc.textExtent(title).x + 20);
                     gc.dispose();
                     columns.add(col);
                 }
             }
             addSotrListeners();
             tabl.setHeaderVisible(true);
             tabl.setLinesVisible(true);
             tableViewer.setLabelProvider(this);
             tableViewer.refresh();
         }
 
         private void addSotrListeners() {
             for (int i=0;i<columnHeaders.size();i++) {
                 final Integer ind = i;
                 final ColumnHeaders currHeader = columnHeaders.get(i);
                 currHeader.getColumn().addSelectionListener(new SelectionListener() {
                     
                     @Override
                     public void widgetSelected(SelectionEvent e) {
                         if(sortedColumns.contains(ind)){
                             sortedColumns.remove(ind);
                         }
                         sortedColumns.add(ind);
                         currHeader.updateSortOrder();
                         if (provider != null) {
                             provider.sort();
                         }
                         tableViewer.refresh();
                         tableViewer.getTable().showSelection();
                     }
                     
                     @Override
                     public void widgetDefaultSelected(SelectionEvent e) {
                         widgetSelected(e);
                     }
                 });
             }
         }
         
         public void updateHeaders(StatisticsCallType callType){
             Table tabl = tableViewer.getTable();
             columnHeaders = new ArrayList<ColumnHeaders>();
             sortedColumns.clear();
             int lastNum = 0;
             columnHeaders.add(new ColumnHeaders(columns.get(lastNum++), null));
             List<IStatisticsHeader> headers = callType.getHeaders();
             if (callType.getLevel().equals(StatisticsCallType.FIRST_LEVEL)) {
                 TableColumn column = columns.get(lastNum++);
                 column.setText(COL_HOST);
                 columnHeaders.add(new ColumnHeaders(column, null));
                 column.setWidth(DEF_SIZE);
                 column = columns.get(lastNum++);
                 column.setText(INeoConstants.PROBE_LA);
                 columnHeaders.add(new ColumnHeaders(column, null));
                 column.setWidth(DEF_SIZE);
                 column = columns.get(lastNum++);
                 column.setText(INeoConstants.PROBE_F);
                 columnHeaders.add(new ColumnHeaders(column, null));
                 column.setWidth(DEF_SIZE);
             }else{
                 headers = getAggregationHeaders();
             }            
             for(IStatisticsHeader header : headers){
                 TableColumn column = columns.get(lastNum++);
                 String title = header.getTitle();
                 GC gc = new GC(column.getParent());
                 column.setText(header.getTitle());
                 columnHeaders.add(new ColumnHeaders(column, header));
                 column.setWidth(gc.textExtent(title).x + 20);
                 gc.dispose();
             }
             for(int i=lastNum; i<columns.size(); i++){
                 TableColumn column = columns.get(i);
                 column.setText("");
                 column.setWidth(0); //hide not needed columns
             }
             addSotrListeners();
             tabl.setHeaderVisible(true);
             tabl.setLinesVisible(true);
             tableViewer.setLabelProvider(this);
             tableViewer.refresh();
         }
 
         @Override
         public Color getBackground(Object element, int columnIndex) {
             return element instanceof PeriodWrapper ? ((PeriodWrapper)element).getColor() : null;
         }
 
         @Override
         public Color getForeground(Object element, int columnIndex) {
             return null;
         }
     }
     
     private int simpleCompare(PeriodWrapper o1, PeriodWrapper o2, int column){        
         ColumnHeaders header = columnHeaders.get(column);
         SortOrder order = sortedColumns.isEmpty()?SortOrder.ASC:header.sortOrder;
         if (header == null) {
             return 0;
         }
         String value1 = header.getValue(o1, column);
         if (value1 == null) {
             value1 = "";
         }
         String value2 = header.getValue(o2, column);
         if (value2 == null) {
             value2 = "";
         }
         switch (order) {
         case DESC:
             return value2.compareTo(value1);
         case ASC:
             return value1.compareTo(value2);
         default:
             return 0;
         }
         
     }
 
 
     /**
      * This is a callback that will allow us to create the viewer and initialize it.
      */
     public void createPartControl(Composite parent) {
         this.parent = parent;
         color1 = new Color(Display.getCurrent(), 240, 240, 240);
         color2 = new Color(Display.getCurrent(), 255, 255, 255);
         sortedColumns = new LinkedList<Integer>();
         comparator = new Comparator<PeriodWrapper>() {
             @Override
             public int compare(PeriodWrapper o1, PeriodWrapper o2) {
                 if(sortedColumns.isEmpty()){
                     return simpleCompare(o1, o2, 0);
                 }
                 int result = 0;
                 int ind = sortedColumns.size();
                 do{
                     ind--;
                     result = simpleCompare(o1, o2, sortedColumns.get(ind));
                 }while(ind>0&&result==0);
                 return result;
             }
         };
         frame = new Composite(parent, SWT.FILL);
         FormLayout formLayout = new FormLayout();
         formLayout.marginHeight = 0;
         formLayout.marginWidth = 0;
         formLayout.spacing = 0;
         frame.setLayout(formLayout);
         // create row composite
         Composite rowComposite = new Composite(frame, SWT.FILL);
         FormData fData = new FormData();
         fData.left = new FormAttachment(0, 0);
         fData.right = new FormAttachment(100, 0);
         rowComposite.setLayoutData(fData);
         FormLayout layout = new FormLayout();
         layout.marginHeight = 2;
         layout.marginWidth = 3;
         rowComposite.setLayout(layout);  
      // ------ fill row
         Composite column1 = new Composite(rowComposite, SWT.FILL);
         fData = new FormData();
         fData.left = new FormAttachment(0, 0);
         fData.width = MIN_COLUMN_WIDTH;
         column1.setLayoutData(fData);
         layout = new FormLayout();
         layout.marginHeight = 2;
         layout.marginWidth = 3;
         column1.setLayout(layout);
         
         Composite cell1 = new Composite(column1, SWT.FILL);
         fData = new FormData();
         fData.left = new FormAttachment(0, 0);
         fData.width = MIN_COLUMN_WIDTH;
         cell1.setLayoutData(fData);
         layout = new FormLayout();
         layout.marginHeight = 2;
         layout.marginWidth = 3;
         cell1.setLayout(layout);
         
         Label label = new Label(cell1, SWT.FLAT);
         label.setText(LBL_DRIVE);
         fData = new FormData();
         fData.left = new FormAttachment(0, 0);
         fData.bottom = new FormAttachment(100,-4);
         label.setLayoutData(fData);
         cDrive = new Combo(cell1, SWT.DROP_DOWN | SWT.READ_ONLY);
         fData = new FormData();
         fData.right = new FormAttachment(100, -2);
         fData.width = MIN_FIELD_WIDTH;               
         cDrive.setLayoutData(fData);
         
         Composite cell2 = new Composite(column1, SWT.FILL);
         fData = new FormData();
         fData.left = new FormAttachment(0, 0);
         fData.top = new FormAttachment(cell1,2);
         fData.width = MIN_COLUMN_WIDTH;
         cell2.setLayoutData(fData);
         layout = new FormLayout();
         layout.marginHeight = 2;
         layout.marginWidth = 3;
         cell2.setLayout(layout);
         
         label = new Label(cell2, SWT.FLAT);
         label.setText(LBL_PERIOD);
         fData = new FormData();
         fData.left = new FormAttachment(0, 0);
         fData.bottom = new FormAttachment(100,-4);
         label.setLayoutData(fData);
         cPeriod = new Combo(cell2, SWT.DROP_DOWN | SWT.READ_ONLY);
         fData = new FormData();
         fData.right = new FormAttachment(100, -2);
         fData.width = MIN_FIELD_WIDTH;
         cPeriod.setLayoutData(fData); 
         
         Composite column2 = new Composite(rowComposite, SWT.FILL);
         fData = new FormData();
         fData.left = new FormAttachment(column1, 2);
         fData.width = MIN_COLUMN_WIDTH+10;
         column2.setLayoutData(fData);
         layout = new FormLayout();
         layout.marginHeight = 2;
         layout.marginWidth = 3;
         column2.setLayout(layout);
         
         cell1 = new Composite(column2, SWT.FILL);
         fData = new FormData();
         fData.left = new FormAttachment(0, 0);
         fData.width = MIN_COLUMN_WIDTH+10;
         cell1.setLayoutData(fData);
         layout = new FormLayout();
         layout.marginHeight = 2;
         layout.marginWidth = 3;
         cell1.setLayout(layout);
         
         label = new Label(cell1, SWT.FLAT);
         label.setText(LBL_CALL_TYPE);
         fData = new FormData();
         fData.left = new FormAttachment(0, 0);
         fData.bottom = new FormAttachment(100,-4);
         label.setLayoutData(fData);
         cCallType = new Combo(cell1, SWT.DROP_DOWN | SWT.READ_ONLY);
         fData = new FormData();
         fData.right = new FormAttachment(100, -2);
         fData.width = MIN_FIELD_WIDTH;
         cCallType.setLayoutData(fData);
         
         cell2 = new Composite(column2, SWT.FILL);
         fData = new FormData();
         fData.left = new FormAttachment(0, 0);
         fData.top = new FormAttachment(cell1,2);
         fData.width = MIN_COLUMN_WIDTH+10;
         cell2.setLayoutData(fData);
         layout = new FormLayout();
         layout.marginHeight = 2;
         layout.marginWidth = 3;
         cell2.setLayout(layout);
         
         label = new Label(cell2, SWT.FLAT);
         label.setText(LBL_START_TIME);
         fData = new FormData();
         fData.left = new FormAttachment(0, 0);
         fData.bottom = new FormAttachment(100,-4);
         label.setLayoutData(fData);
         dateStart = new DateTime(cell2, SWT.FILL | SWT.BORDER | SWT.DATE | SWT.MEDIUM);
         fData = new FormData();
         fData.right = new FormAttachment(95, -MIN_FIELD_WIDTH/3);
         fData.width = 2*MIN_FIELD_WIDTH/3;
         dateStart.setLayoutData(fData);
         timeStart = new DateTime(cell2, SWT.FILL | SWT.BORDER | SWT.TIME | SWT.SHORT);
         fData = new FormData();
         fData.left = new FormAttachment(dateStart, 2);
         fData.right = new FormAttachment(100, -2);
         fData.width = MIN_FIELD_WIDTH/3;
         timeStart.setLayoutData(fData);
         
         Composite column3 = new Composite(rowComposite, SWT.FILL);
         fData = new FormData();
         fData.left = new FormAttachment(column2, 2);
         fData.width = MIN_COLUMN_WIDTH;
         column3.setLayoutData(fData);
         layout = new FormLayout();
         layout.marginHeight = 2;
         layout.marginWidth = 3;
         column3.setLayout(layout);
         
         cell1 = new Composite(column3, SWT.FILL);
         fData = new FormData();
         fData.left = new FormAttachment(0, 0);
         fData.width = MIN_COLUMN_WIDTH;
         cell1.setLayoutData(fData);
         layout = new FormLayout();
         layout.marginHeight = 2;
         layout.marginWidth = 3;
         cell1.setLayout(layout);
         
         label = new Label(cell1, SWT.FLAT);
         label.setText(LBL_PROBE);
         fData = new FormData();
         fData.left = new FormAttachment(0, 0);
         fData.bottom = new FormAttachment(100,-4);
         label.setLayoutData(fData);
         cProbe = new Combo(cell1, SWT.DROP_DOWN | SWT.READ_ONLY);
         fData = new FormData();
         fData.right = new FormAttachment(100, -2);
         fData.width = MIN_FIELD_WIDTH;
         cProbe.setLayoutData(fData);
         
         cell2 = new Composite(column3, SWT.FILL);
         fData = new FormData();
         fData.left = new FormAttachment(0, 0);
         fData.top = new FormAttachment(cell1,2);
         fData.width = MIN_COLUMN_WIDTH;
         cell2.setLayoutData(fData);
         layout = new FormLayout();
         layout.marginHeight = 2;
         layout.marginWidth = 3;
         cell2.setLayout(layout);
         
         label = new Label(cell2, SWT.FLAT);
         label.setText(LBL_END_TIME);
         fData = new FormData();
         fData.left = new FormAttachment(0, 0);
         fData.bottom = new FormAttachment(100,-4);
         label.setLayoutData(fData);
         dateEnd = new DateTime(cell2, SWT.FILL | SWT.BORDER | SWT.DATE | SWT.MEDIUM);
         fData = new FormData();
         fData.right = new FormAttachment(95, -MIN_FIELD_WIDTH/3);
         fData.width = 2*MIN_FIELD_WIDTH/3;
         dateEnd.setLayoutData(fData);
         timeEnd = new DateTime(cell2, SWT.FILL | SWT.BORDER | SWT.TIME | SWT.SHORT);
         fData = new FormData();
         fData.left = new FormAttachment(dateEnd, 2);
         fData.right = new FormAttachment(100, -2);
         fData.width = MIN_FIELD_WIDTH/3;
         timeEnd.setLayoutData(fData);
         
         Composite column4 = new Composite(rowComposite, SWT.FILL);
         fData = new FormData();
         fData.left = new FormAttachment(column3, 2);
         fData.right = new FormAttachment(100, 0);
         column4.setLayoutData(fData);
         layout = new FormLayout();
         layout.marginHeight = 2;
         layout.marginWidth = 3;
         column4.setLayout(layout);
         
         bExport = new Button(column4, SWT.PUSH);
         bExport.setText(LB_EXPORT);
         fData = new FormData();
         fData.right = new FormAttachment(100, 0);
         bExport.setLayoutData(fData);
         
         // ------- table
         tableViewer = new TableViewer(frame, SWT.BORDER | SWT.FULL_SELECTION);
         fData = new FormData();
         fData.left = new FormAttachment(0, 0);
         fData.right = new FormAttachment(100, 0);
         fData.top = new FormAttachment(rowComposite, 2);
         fData.bottom = new FormAttachment(100, -2);
         tableViewer.getControl().setLayoutData(fData);
         cursor = new TableCursor(tableViewer.getTable(), SWT.DefaultSelection);
 
         hookContextMenu();
         addListeners();
         initialize();
         setDefaultTime();
     }
     
     private void setTime(DateTime dateFild,DateTime timeFild, Long time){
         Calendar calendar = new GregorianCalendar();
         calendar.setTimeInMillis(time);
         dateFild.setYear(calendar.get(Calendar.YEAR));
         dateFild.setMonth(calendar.get(Calendar.MONTH));
         dateFild.setDay(calendar.get(Calendar.DAY_OF_MONTH));
         timeFild.setHours(calendar.get(Calendar.HOUR_OF_DAY));
         timeFild.setMinutes(0);
         timeFild.setSeconds(0);
     }
     
     private Long getTime(DateTime dateFild,DateTime timeFild){
         Calendar calendar = new GregorianCalendar();
         calendar.setTimeInMillis(0L);
         calendar.set(dateFild.getYear(), dateFild.getMonth(), dateFild.getDay(), 
                 timeFild.getHours(), timeFild.getMinutes());
         return calendar.getTimeInMillis();
     }
 
     /**
      * @param sRow
      */
     protected void select(final Node node) {
         //TODO refactor
         InputWrapper wr = (InputWrapper)tableViewer.getInput();
         List<Node> nodes = new ArrayList<Node>(2);
         nodes.add(node);
         nodes.add(wr.periodNode);
         NeoCorePlugin.getDefault().getUpdateViewManager().fireUpdateView(new ShowPreparedViewEvent(DRIVE_ID, nodes));
         NeoCorePlugin.getDefault().getUpdateViewManager().fireUpdateView(new UpdateDrillDownEvent(nodes,CallAnalyserView.ID));
         final Node drive = callDataset.get(cDrive.getText());
         Job job = new Job("SelectOnMap") {
 
             @Override
             protected IStatus run(IProgressMonitor monitor) {
                 Transaction tx = NeoServiceProvider.getProvider().getService().beginTx();
                 try {
                     Traverser traverse = node.traverse(Order.DEPTH_FIRST, new StopEvaluator() {
 
                         @Override
                         public boolean isStopNode(TraversalPosition currentPos) {
                             if (currentPos.isStartNode()) {
                                 return false;
                             }
                             Node node = currentPos.currentNode();
                             String type = NeoUtils.getNodeType(node, "");
                             if (type.equals(NodeTypes.CALL.getId())) {
                                 return true;
                             }
                             Relationship relation = currentPos.lastRelationshipTraversed();
                             if (relation.isType(GeoNeoRelationshipTypes.NEXT)) {
                                 return !type.equals(NodeTypes.S_CELL.getId());
                             }
                             return false;
                         }
                     }, new ReturnableEvaluator() {
 
                         @Override
                         public boolean isReturnableNode(TraversalPosition currentPos) {
                             Node node = currentPos.currentNode();
                             return NeoUtils.isCallNode(node) && node.hasRelationship(GeoNeoRelationshipTypes.LOCATION, Direction.OUTGOING);
                         }
                     }, GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING, GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING, GeoNeoRelationshipTypes.SOURCE,
                             Direction.OUTGOING);
                     Collection<Node> nodes = traverse.getAllNodes();
                     if (!nodes.isEmpty()) {
                         selectNodesOnMap(drive, nodes);
                     }
                     return Status.OK_STATUS;
                 } catch (Exception e) {
                     e.printStackTrace();
                     return Status.OK_STATUS;
                 } finally {
                     tx.finish();
                 }
             }
         };
         job.schedule();
     }
 
     /**
      * select nodes on map
      * 
      * @param drive
      * @param nodes nodes to select
      */
     // TODO use selection mechanism!
     private void selectNodesOnMap(Node drive, Collection<Node> nodes) {
 
         if (drive == null) {
             return;
         }
         Node gis = NeoUtils.findGisNodeByChild(drive);
         IMap activeMap = ApplicationGIS.getActiveMap();
         if (activeMap != ApplicationGIS.NO_MAP) {
             NeoCatalogPlugin.getDefault().getLayerManager().sendUpdateMessage(new ChangeSelectionEvent(gis, nodes));
         }
     }
 
     /**
      * initialize startup parameters
      */
     private void initialize() {
         labelProvider = new ViewLabelProvider();
         labelProvider.createTableColumn();
         provider = new ViewContentProvider();
         tableViewer.setContentProvider(provider);
         tableViewer.setInput(0);
         // formPeriods();
         formCallDataset();
     }
 
     /**
      *forms period list
      * 
      * @param statistics
      */
     private void formPeriods(CallStatistics statistics) {
         List<String> periods=new ArrayList<String>();
         if (statistics!=null){
             CallTimePeriods[] allPeriods = CallTimePeriods.values();
             for (int i = 0; i <= statistics.getHighPeriod().ordinal(); i++) {
                 periods.add(allPeriods[i].getId());
             }
         }
         cPeriod.setItems(periods.toArray(new String[0]));
         cPeriod.setText(periods.get(0));
     }
     
     /**
      *forms direction list
      * 
      * @param callTypes
      */
     private void formCallType(Set<StatisticsCallType> callTypesSet) {
         List<String> callTypes=new ArrayList<String>();
         for (StatisticsCallType callType : StatisticsCallType.getSortedTypesList(callTypesSet)) {
             callTypes.add(callType.getViewName());
         }
         cCallType.setItems(callTypes.toArray(new String[0]));
         cCallType.setText(callTypes.get(0));
     }
     
     private void formCallType(StatisticsCallType callType) {
         List<String> callTypes=new ArrayList<String>();
         callTypes.add(callType.getViewName());
         
         cCallType.setItems(callTypes.toArray(new String[0]));
         cCallType.setText(callType.getViewName());
     }
 
     /**
      *form call dataset list
      */
     private void formCallDataset() {
         callDataset.clear();
         callDataset = NeoUtils.getAllDatasetNodesByType(DriveTypes.AMS_CALLS, NeoServiceProvider.getProvider().getService());
         List<String> datasets = new ArrayList<String>(callDataset.keySet());
         Collections.sort(datasets);
         cDrive.setItems(datasets.toArray(new String[0]));
         cCallType.clearSelection();
         cProbe.clearSelection();
         cPeriod.clearSelection();
     }
 
     /**
      *add listeners
      */
     private void addListeners() {
         tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 
             @Override
             public void selectionChanged(SelectionChangedEvent event) {
                 if (event.getSelection() instanceof IStructuredSelection) {
                     IStructuredSelection selections = (IStructuredSelection)event.getSelection();
                     Object selRow = selections.getFirstElement();
                     if (selRow != null && selRow instanceof PeriodWrapper) {
                         PeriodWrapper wr = (PeriodWrapper)selRow;
                         int columnId = cursor.getColumn();
                         if (columnId == 0) {
                             select(wr.sRow);
                             return;
                         }
                         if (columnId == 1 || columnId == 2 || columnId == 3) {
                             select(wr.getProbeNode());
                             return;
                         }
                         ColumnHeaders header = columnHeaders.get(columnId);
                         final String nodeName = header.header.getTitle();
                         Node cellNode = null;
                         Transaction tx = NeoServiceProvider.getProvider().getService().beginTx();
                         try {
                             Iterator<Node> iterator = NeoUtils.getChildTraverser(wr.sRow, new ReturnableEvaluator() {
 
                                 @Override
                                 public boolean isReturnableNode(TraversalPosition currentPos) {
                                     return NeoUtils.getNodeName(currentPos.currentNode()).equals(nodeName);
                                 }
                             }).iterator();
                             cellNode = iterator.hasNext() ? iterator.next() : null;
                         } finally {
                             tx.finish();
                         }
                         if (cellNode != null) {
                             select(cellNode);
                         }
                     }
                 }
             }
         });
 
         cDrive.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 formPropertyList();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         cProbe.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 changeProbe();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         cPeriod.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 changePeriod();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         
         cCallType.addSelectionListener(new SelectionListener() {
             
             @Override
             public void widgetSelected(SelectionEvent e) {
                 changeCallType();                
             }
             
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         bExport.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 startExport();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
          dateStart.addFocusListener(new FocusListener() {        
              @Override
              public void focusLost(FocusEvent e) {
                  changeDate();
              }
             
              @Override
              public void focusGained(FocusEvent e) {
              }
          });
          dateStart.addKeyListener(new KeyListener() {        
              @Override
              public void keyReleased(KeyEvent e) {
                  if (e.keyCode == '\r' || e.keyCode == SWT.KEYPAD_CR) {
                      changeDate();
                  }
              }
             
              @Override
              public void keyPressed(KeyEvent e) {
              }
          });
          timeStart.addFocusListener(new FocusListener() {             
              @Override
              public void focusLost(FocusEvent e) {
                  changeDate();
              }
             
              @Override
              public void focusGained(FocusEvent e) {
              }
          });
          timeStart.addKeyListener(new KeyListener() {        
              @Override
              public void keyReleased(KeyEvent e) {
                  if (e.keyCode == '\r' || e.keyCode == SWT.KEYPAD_CR) {
                      changeDate();
                  }
              }
             
              @Override
              public void keyPressed(KeyEvent e) {
              }
          });
          dateEnd.addFocusListener(new FocusListener() {        
              @Override
              public void focusLost(FocusEvent e) {
                  changeDate();
              }
             
              @Override
              public void focusGained(FocusEvent e) {
              }
          });
          dateEnd.addKeyListener(new KeyListener() {        
              @Override
              public void keyReleased(KeyEvent e) {
                  if (e.keyCode == '\r' || e.keyCode == SWT.KEYPAD_CR) {
                      changeDate();
                  }
              }            
              @Override
              public void keyPressed(KeyEvent e) {
              }
          });
          timeEnd.addFocusListener(new FocusListener() {             
              @Override
              public void focusLost(FocusEvent e) {
                  changeDate();
              }            
              @Override
              public void focusGained(FocusEvent e) {
              }
          });
          timeEnd.addKeyListener(new KeyListener() {        
              @Override
              public void keyReleased(KeyEvent e) {
                  if (e.keyCode == '\r' || e.keyCode == SWT.KEYPAD_CR) {
                      changeDate();
                  }
              }            
              @Override
              public void keyPressed(KeyEvent e) {
              }
          });
     }
 
     /**
      *start export job
      */
     protected void startExport() {
         final IWorkbenchWindow window = getSite().getWorkbenchWindow();
         final List<PeriodWrapper> elements = new ArrayList<PeriodWrapper>(provider.elements);
         if (!elements.isEmpty()) {
             ExportSpreadsheetWizard wizard = new ExportSpreadsheetWizard(elements, columnHeaders);
             wizard.init(window.getWorkbench(), null);
             Shell parent = window.getShell();
             WizardDialog dialog = new WizardDialog(parent, wizard);
             dialog.create();
             dialog.open();
         }
 
     }
     
     protected Long getStartTime(){
         return getTime(dateStart, timeStart);
     }
     
     protected Long getEndTime(){
         return getTime(dateEnd, timeEnd);
     }
 
     /**
      *change start or end time
      */
     protected void changeDate() {
         CallTimePeriods period = getTimePeriod();
         if (period!=null) {
             setTime(dateStart, timeStart, period.getFirstTime(getStartTime())); //set correct time for period
             setTime(dateEnd, timeEnd, period.getFirstTime(getEndTime()));
         }else{
             setDefaultTime();
         }
         updateTable(false);
     }
 
 
     private void setDefaultTime() {
         Long time = CallTimePeriods.HOURLY.getFirstTime(System.currentTimeMillis());
         setTime(dateStart, timeStart, time);
         setTime(dateEnd, timeEnd, time);
     }
 
     /**
      *change period
      */
     protected void changePeriod() {
         Node drive = callDataset.get(cDrive.getText());
         if(drive!=null){
             GraphDatabaseService service = NeoServiceProvider.getProvider().getService();
             Pair<Long, Long> times = NeoUtils.getMinMaxTimeOfDataset(drive, service);
             setTime(dateStart, timeStart, times.getLeft()); 
             setTime(dateEnd, timeEnd, times.getRight());
         }else{
             setDefaultTime();
         }
         updateTable(false);
     }
     
     /**
      *change period
      */
     protected void changeCallType() {
         StatisticsCallType callType = getCallType();
         columnHeaders = new ArrayList<ColumnHeaders>();
         labelProvider.updateHeaders(callType);
         Node drive = callDataset.get(cDrive.getText());
         if (cProbe.getText().isEmpty()) {
             formProbeCall(drive, callType);
         }
         else {            
             if(callType.equals(StatisticsCallType.AGGREGATION_STATISTICS)){
                 cProbe.setText(KEY_ALL);
             }
             String probeName = cProbe.getText();
             formProbeCall(drive, callType);
             if ((probeName.equals(ALL_VALUE)) ||(!probeName.isEmpty() &&
                 NeoUtils.hasCallsOfType(drive, callType.getId(), probeName))) {                
                 cProbe.setText(probeName);
                 updateTable(false);
             }            
             else {
                 updateTable(true);
             }
         }
     }
     
     private StatisticsCallType getCallType(){
         return StatisticsCallType.getTypeByViewName(cCallType.getText());
     }
 
     /**
      *change probe
      */
     protected void changeProbe() {
         updateTable(false);
     }
 
     /**
      *update table if has correct InputWrapper
      */
     private void updateTable(boolean showEmpty) {
         InputWrapper wrapper = createInputWrapper(showEmpty);
         if (wrapper.isCorrectInput()) {
             tableViewer.setInput(wrapper);
         }
     }
 
     /**
      * create InputWrapper depends of user choices
      * 
      * @return InputWrapper
      */
     private InputWrapper createInputWrapper(boolean showEmpty) {
         return new InputWrapper(probeCallDataset.get(cProbe.getText()), callDataset.get(cDrive.getText()),
                 getTimePeriod(), cCallType.getText(), showEmpty);
     }
 
     private CallTimePeriods getTimePeriod() {
         return CallTimePeriods.findById(cPeriod.getText());
     }
 
     /**
      *forms property list depends of selected dataset
      */
     protected void formPropertyList() {
         final Node drive = callDataset.get(cDrive.getText());
         if (drive == null) {
             setDefaultTime();
             return;
         }
         parent.setCursor(new Cursor(parent.getDisplay(), SWT.CURSOR_WAIT));
         frame.setEnabled(false);        
         Job statGetter = new Job("Get statistics") {            
             @Override
             protected IStatus run(IProgressMonitor monitor) {
                 try {
                     GraphDatabaseService service = NeoServiceProvider.getProvider().getService();
                     final CallStatistics statistics = new CallStatistics(drive, service, monitor);
                     final Pair<Long, Long> times = NeoUtils.getMinMaxTimeOfDataset(drive, service);
                     ActionUtil.getInstance().runTask(new Runnable() {
                         @Override
                         public void run() {
                             Transaction tx = NeoUtils.beginTransaction();
                             try {
                                 Set<StatisticsCallType> callTypes = statistics.getStatisticNode().keySet();
                                 if (callTypes.size() == 1) {
                                     StatisticsCallType type = callTypes.iterator().next();
                                     formCallType(type);                
                                 }
                                 else {
                                     formCallType(callTypes);
                                 }
                                 StatisticsCallType callType = getCallType();
                                 formProbeCall(drive, callType);
                                 formPeriods(statistics);
                                 setTime(dateStart, timeStart, times.getLeft());
                                 setTime(dateEnd, timeEnd, times.getRight());
                                 labelProvider.updateHeaders(callType);
                                 updateTable(false);
                             } finally {
                                tx.finish();                                                                
                             }
                         }
                     }, true);
                     
                 } catch (IOException e) {
                     // TODO Handle IOException
                     throw (RuntimeException)new RuntimeException().initCause(e);
                } finally {
                    ActionUtil.getInstance().runTask(new Runnable() {
                        @Override
                        public void run() {
                            frame.setEnabled(true);
                            parent.setCursor(new Cursor(parent.getDisplay(), SWT.CURSOR_ARROW));
                        }
                    }, true);
                 }
                 return Status.OK_STATUS;
             }
         };
         statGetter.schedule();
 
     }
 
     /**
      * forms call probe depends of dataset
      * 
      * @param drive - drive dataset
      */
     private void formProbeCall(Node drive, StatisticsCallType callType) {
         probeCallDataset.clear();
         probeCallDataset.put(KEY_ALL, null);
         if ((drive != null) && (callType != null)&& !callType.equals(StatisticsCallType.AGGREGATION_STATISTICS)) {
             GraphDatabaseService service = NeoServiceProvider.getProvider().getService();
             Transaction tx = service.beginTx();
             try {
                 Collection<Node> allProbesOfDataset = NeoUtils.getAllProbesOfDataset(drive, callType.getId());
                 for (Node probe : allProbesOfDataset) {
                     probeCallDataset.put(NeoUtils.getNodeName(probe), probe);
                 }
             } finally {
                 tx.finish();
             }
         }
         String[] result = probeCallDataset.keySet().toArray(new String[0]);
         Arrays.sort(result);
         cProbe.setItems(result);
         cProbe.setText(KEY_ALL);
     }
     
     private List<IStatisticsHeader> getAggregationHeaders(){
         Node drive = callDataset.get(cDrive.getText());
         if (drive == null) {
             return StatisticsCallType.AGGREGATION_STATISTICS.getHeaders();
         }
         GraphDatabaseService service = NeoServiceProvider.getProvider().getService();
         Transaction tx = service.beginTx();
         try {
             Node statRoot = null;
             for (Relationship link : drive.getRelationships(ProbeCallRelationshipType.CALL_ANALYSIS, Direction.OUTGOING)) {
                 Node root = link.getEndNode();
                 String rootType = root.getProperty(CallProperties.CALL_TYPE.getId(), "").toString();
                 if (rootType.equals(StatisticsCallType.AGGREGATION_STATISTICS.toString())) {
                     statRoot = root;
                     break;
                 }
             }
             if (statRoot == null) {
                 return StatisticsCallType.AGGREGATION_STATISTICS.getHeaders();
             }
             List<IStatisticsHeader> result = new ArrayList<IStatisticsHeader>();
             for (AggregationCallTypes type : AggregationCallTypes.values()) {
                 boolean hasType = (Boolean)statRoot.getProperty(type.getRealType().getId().getProperty(), false);
                 if(hasType){
                     result.addAll(type.getAggrHeaders());
                 }
             }
             return result;
         } finally {
             tx.finish();
         }
     }
 
 
     // TODO implement if necessary
     private void hookContextMenu() {
         // MenuManager menuMgr = new MenuManager("#PopupMenu");
         // menuMgr.setRemoveAllWhenShown(true);
         // menuMgr.addMenuListener(new IMenuListener() {
         // public void menuAboutToShow(IMenuManager manager) {
         // CallAnalyserView.this.fillContextMenu(manager);
         // }
         // });
         // Menu menu = menuMgr.createContextMenu(viewer.getControl());
         // viewer.getControl().setMenu(menu);
         // getSite().registerContextMenu(menuMgr, viewer);
     }
 
     /**
      * Passing the focus request to the viewer's control.
      */
     public void setFocus() {
         tableViewer.getControl().setFocus();
     }
 
     /**
      * <p>
      * Column header - contains information about columns
      * </p>
      * 
      * @author Cinkel_A
      * @since 1.0.0
      */
     public class ColumnHeaders {
 
         final private TableColumn column;
         private final IStatisticsHeader header;
         private String name;
         private SortOrder sortOrder = SortOrder.NONE;
 
 
         /**
          * constructor - only for string properties property.needMappedCount() must be true
          * 
          * @param column TableColumn
          * @param properties - property value
          */
         public ColumnHeaders(TableColumn column, IStatisticsHeader header) {
             this.column = column;
             this.header = header;
             name = column.getText();
         }
 
         /**
          * get value depends PeriodWrapper
          * 
          * @param wr - PeriodWrapper
          * @param index
          * @return statistic value
          */
         public String getValue(PeriodWrapper wr, int index) {
             if (header == null) {
                 if (index==0){
                 return NeoUtils.getNodeName(wr.sRow);
                 }else if (index==1){
                     return wr.getHost();
                 } else if (index == 2) {
                     return wr.getProbeLA();
                 } else {
                     return wr.getProbeF();
                 }
             } else {
                 return wr.getValue(header);
             }
         }
 
         /**
          * @return Returns the name.
          */
         public String getName() {
             return name;
         }
 
         /**
          * @return Returns the column.
          */
         public TableColumn getColumn() {
             return column;
         }
         
         private void updateSortOrder() {
             switch (sortOrder) {
             case NONE:
             case ASC:
                 sortOrder = SortOrder.DESC;
                 break;
             case DESC:
                 sortOrder = SortOrder.ASC;
                 break;
             default:
                 break;
             }
             //column.setImage(CallAnalyserPlugin.getImageDescriptor(sortOrder.getIconPath()).createImage());
         }
     }
 
     /**
      * <p>
      * Period wrapper contains information about calculated period
      * </p>
      * 
      * @author Cinkel_A
      * @since 1.0.0
      */
     public static class PeriodWrapper {
         private final Node sRow;
         private Map<IStatisticsHeader, String> mappedValue = new HashMap<IStatisticsHeader, String>();
         private String host;
         private String probeF = "";
         private String probeLA = "";
         private Node probeNode;
         private Color color;
         /**
          * Constructor
          * 
          * @param beginTime - begin time
          * @param endTime - end time
          * @param indexPartName - index name
          */
         public PeriodWrapper(Node sRow, StatisticsCallType callType) {
             super();
             this.sRow = sRow;
             mappedValue.clear();
             for (Node node : NeoUtils.getChildTraverser(sRow)) {
                 String name = NeoUtils.getNodeName(node);
                 IStatisticsHeader header = callType.getHeaderByTitle(name);
                 if (header != null) {                   
                     Object value = node.getProperty(INeoConstants.PROPERTY_VALUE_NAME, null);
                     mappedValue.put(header, getFormattedValue(value, header));
                 }
             }
             if (!callType.equals(StatisticsCallType.AGGREGATION_STATISTICS)) {
                 probeNode = sRow.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
                     @Override
                     public boolean isReturnableNode(TraversalPosition currentPos) {
                         return NeoUtils.isProbeNode(currentPos.currentNode());
                     }
                 }, GeoNeoRelationshipTypes.SOURCE, Direction.OUTGOING).iterator().next();
                 host = NeoUtils.getNodeName(probeNode).split(" ")[0];
                 Number f = (Number)probeNode.getProperty(INeoConstants.PROBE_F, null);
                 Number la = (Number)probeNode.getProperty(INeoConstants.PROBE_LA, null);
                 probeF = f == null ? "" : f.toString();
                 probeLA = la == null ? "" : la.toString();
             }
         }
         
         private String getFormattedValue(Object value,IStatisticsHeader header){
             if(value == null){
                if(header.getType().equals(StatisticsType.COUNT)){
                    value = 0;
                }else{
                    value = 0f;
                }
             }            
             if(value instanceof Float){
                 BigDecimal decValue = new BigDecimal(((Float)value).doubleValue());
                 if (decValue.equals(BigDecimal.ZERO)) {
                     decValue = decValue.setScale(1);
                 }else{
                     decValue = decValue.setScale(3, RoundingMode.HALF_EVEN);
                 }
                 return decValue.toString();
             }
             return value.toString();
         }
 
         /**
          * @return
          */
         public Color getColor() {
             return color;
         }
 
         /**
          * @param header
          * @return
          */
         public String getValue(IStatisticsHeader header) {
             String string = mappedValue.get(header);
             return string == null ? ERROR_VALUE : string;
         }
 
         /**
          * @return Returns the host.
          */
         public String getHost() {
             return host;
         }
 
         /**
          * @return Returns the probeF.
          */
         public String getProbeF() {
             return probeF;
         }
 
         /**
          * @return Returns the probeLA.
          */
         public String getProbeLA() {
             return probeLA;
         }
 
         /**
          * @return Returns the probeNode.
          */
         public Node getProbeNode() {
             return probeNode;
         }
 
         /**
          * @param color The color to set.
          */
         public void setColor(Color color) {
             this.color = color;
         }
 
     }
 
     /**
      * <p>
      * InputWrapper contains information about input
      * </p>
      * 
      * @author Cinkel_A
      * @since 1.0.0
      */
     public static class InputWrapper {
         private Node probe;
         private Node drive;
         private CallTimePeriods periods;
         private StatisticsCallType callType;
         private Node periodNode;
         
         private boolean showEmpty;
 
         /**
          * constructor
          * 
          * @param probe - probe call node
          * @param drive - call dataset node
          * @param periods - periods
          */
         public InputWrapper(Node probe, Node drive, CallTimePeriods periods, String callType, boolean showEmpty) {
             super();
             this.probe = probe;
             this.drive = drive;
             this.periods = periods;
             this.showEmpty = showEmpty;
             if (callType.isEmpty()) {
                 this.callType = null;
             }
             else {
                 this.callType = StatisticsCallType.getTypeByViewName(callType);
             }
         }
 
         /**
          * @return
          */
         public Traverser getSrowTraverser(GraphDatabaseService service) {
 
             try {
                 CallStatistics statistic = new CallStatistics(drive, service);
                 periodNode = statistic.getPeriodNode(periods, callType);
                 
                 if (showEmpty) {
                     return null;
                 }
                 
                 if (periodNode == null) {
                     return NeoUtils.emptyTraverser(probe);
                 }
                 if(callType.equals(StatisticsCallType.AGGREGATION_STATISTICS)){
                     return NeoUtils.getChildTraverser(periodNode);
                 }
                 return NeoUtils.getChildTraverser(periodNode, new ReturnableEvaluator() {
 
                     @Override
                     public boolean isReturnableNode(TraversalPosition currentPos) {
                         return (probe == null || currentPos.currentNode().traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
                                     @Override
                                     public boolean isReturnableNode(TraversalPosition currentPos) {
                                         return currentPos.currentNode().equals(probe);
                                     }
                                 }, GeoNeoRelationshipTypes.SOURCE, Direction.OUTGOING).iterator().hasNext());
                     }
                 });
             } catch (IOException e) {
                 NeoCorePlugin.error(e.getLocalizedMessage(), e);
                 return NeoUtils.emptyTraverser(probe);
             }
         }
 
         /**
          * get index name
          * 
          * @return
          */
         public String getIndexName() {
             return NeoUtils.getNodeName(probe != null ? probe : drive);
         }
 
         /**
          * check
          * 
          * @return true if InputWrapper contains correct information
          */
         public boolean isCorrectInput() {
             return drive != null /* && probe != null */&& periods != null && callType != null;
         }
 
     }
 
     /**
      *update view
      */
     public void updateView(boolean showEmpty) {
         formCallDataset();
         formProbeCall(null, null);
         tableViewer.setInput(createInputWrapper(showEmpty));
     }
 
 }
