 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.awe.views.reuse.mess_table.view;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import org.amanzi.awe.views.reuse.Messages;
 import org.amanzi.awe.views.reuse.mess_table.DataTypes;
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.GisTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.core.utils.PropertyHeader;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.ViewPart;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.TraversalPosition;
 import org.neo4j.graphdb.Traverser.Order;
 
 /**
  * View for Message and Event tabular
  * 
  * @author Shcharbatsevich_A
  * @since 1.0.0
  */
 public class MessageAndEventTableView extends ViewPart {
     
     private static final String EXPRESSION_NOT_EMPTY = "not empty";
     private static final String EXPRESSION_EMPTY = "empty";
     private static final String[] DEFAULT_EXPRESSIONS = new String[]{EXPRESSION_EMPTY,EXPRESSION_NOT_EMPTY};
     
     // memento keys
     private static final String MEM_DATASET = "MEM_DATASET";
     private static final String MEM_PROPERTY = "MEM_PROPERTY";
     private static final String MEM_EXPRESSION = "MEM_EXPRESSION";
     private static final String MEM_DATASET_MAP_COUNT = "MEM_DATASET_COUNT";
     private static final String MEM_DATASET_MAP = "MEM_DATASET_MAP_";
     private static final String MEM_PROPERTY_COUNT = "MEM_PROPERTY_MAP_COUNT_";
     private static final String MEM_PROPERTY_MAP = "MEM_PROPERTY_MAP_";
     
     private static final int MIN_FIELD_WIDTH = 50;
     
     private Combo cDataset;    
     private Combo cProperty;
     private Combo cExpression;
     private TableViewer table;
     
     private TableLabelProvider labelProvider;
     private TableContentProvider contentProvider;
     
     private Action actCommit;
     private Action actRollback;
     private Action actConfigure;
     private Action actClearFilter;
     
     private HashMap<String, DatasetInfo> datasets;
     
     private String initDataset;
     private String initProperty;
     private String initExpression;
     private HashMap<String,List<String>> initDatasets;
 
     @Override
     public void createPartControl(Composite parent) {
         datasets = initDatasetsInfo();
         initMenuBar();
         
         Composite frame = new Composite(parent, SWT.FILL);
         FormLayout formLayout = new FormLayout();
         formLayout.marginHeight = 0;
         formLayout.marginWidth = 0;
         formLayout.spacing = 0;
         frame.setLayout(formLayout);
         
         Composite child = new Composite(frame, SWT.FILL);
         FormData fData = new FormData();
         fData.top = new FormAttachment(0, 2);
         fData.left = new FormAttachment(0, 2);
         fData.right = new FormAttachment(100, -2);
 
         child.setLayoutData(fData);
         GridLayout layout = new GridLayout(12, false);
         child.setLayout(layout);
 
         Label label = new Label(child, SWT.FLAT);
         label.setText(Messages.MessageAndEventTable_label_DATASET);
         label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
         cDataset = new Combo(child, SWT.DROP_DOWN | SWT.READ_ONLY);
 
         GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
         layoutData.minimumWidth = MIN_FIELD_WIDTH;
         cDataset.setLayoutData(layoutData);
         cDataset.setItems(getDatasets());
         
         label = new Label(child, SWT.FLAT);
         label.setText(Messages.MessageAndEventTable_label_PROPERTY);
         label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
         cProperty = new Combo(child, SWT.DROP_DOWN | SWT.READ_ONLY);
 
         layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
         layoutData.minimumWidth = MIN_FIELD_WIDTH;
         cProperty.setLayoutData(layoutData);
 
         label = new Label(child, SWT.FLAT);
         label.setText(Messages.MessageAndEventTable_label_EXPRESSION);
         label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
         cExpression = new Combo(child, SWT.DROP_DOWN);
 
         layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
         layoutData.minimumWidth = MIN_FIELD_WIDTH;
         cExpression.setLayoutData(layoutData);
         
        table = new TableViewer(frame, SWT.VIRTUAL | SWT.BORDER );
         fData = new FormData();
         fData.left = new FormAttachment(0, 10);
         fData.right = new FormAttachment(100, -10);
         fData.top = new FormAttachment(child, 2);
         fData.bottom = new FormAttachment(100, -10);
         table.getControl().setLayoutData(fData);
         table.getControl().setVisible(false);
         
         labelProvider = new TableLabelProvider();
         labelProvider.createTableColumns();
         contentProvider = new TableContentProvider();
         table.setContentProvider(contentProvider);
         
         addListeners();
         initializeStartupProperties();
     }
 
     /**
      * Initialize menu.
      */
     private void initMenuBar() {
         //TODO Icons
         createActions();
         IToolBarManager tm = getViewSite().getActionBars().getToolBarManager();
         IMenuManager mm = getViewSite().getActionBars().getMenuManager();        
         //mm.add(actCommit);
         //tm.add(actCommit);        
         //mm.add(actRollback);
         //tm.add(actRollback);        
         mm.add(actConfigure);
         tm.add(actConfigure);
         mm.add(actClearFilter);
         tm.add(actClearFilter);
     }
     
     /**
      * Create actions for menu bar.
      */
     private void createActions(){
         actCommit = new Action(Messages.MessageAndEventTable_menu_COMMIT){
             @Override
             public void run(){
                 storeChanges();
             }
         };
         actRollback = new Action(Messages.MessageAndEventTable_menu_ROLLBACK){
             @Override
             public void run(){
                 rollbackChanges();
             }
         };
         actConfigure = new Action(Messages.MessageAndEventTable_menu_CONFIGURE){
             @Override
             public void run(){
                 showConfigureTableView();
             }
         };
         actClearFilter = new Action(Messages.MessageAndEventTable_menu_CLEAR){
             @Override
             public void run(){
                 clearFilter();
             }
         };
     }
     
     /**
      * Clear rows filter.
      */
     private void clearFilter() {
         updateProperty();
     }
 
     /**
      * Commit all changes
      */
     private void storeChanges(){
         //TODO when editing added.
     }
     
     /**
      * Revert all changes.
      */
     private void rollbackChanges(){
         //TODO when editing added.
     }
     
     /**
      * Open view for configure columns visibility.
      */
     private void showConfigureTableView(){
         if(cDataset.getSelectionIndex()<0){
             return;
         }
         String datasetName = cDataset.getText();
         DatasetInfo datasetInfo = datasets.get(datasetName);
         TableConfigWizard wizard = new TableConfigWizard(datasetName,datasetInfo.getVisibleProperties(),datasetInfo.getNotVisibleProperties());
         IWorkbenchWindow workbenchWindow = getViewSite().getWorkbenchWindow();
         wizard.init(workbenchWindow.getWorkbench(), null);
         Shell parent = workbenchWindow.getShell();
         WizardDialog dialog = new WizardDialog(parent, wizard);
         dialog.create();
         int result = dialog.open();
         if(result!=0){
             return;
         }
         String[] newVisible = wizard.getVisible();
         for(String property : datasetInfo.getAllProperties()){
             datasetInfo.setPropertyVisible(property, false);
         }
         if(newVisible !=null){
             for(String property : newVisible){
                 datasetInfo.setPropertyVisible(property, true);
             }
             String selected = null;
             if(cProperty.getSelectionIndex()>=0){
                 selected = cProperty.getText();
             }
             String[] filteredProperties = datasetInfo.getFilteredProperties();
             cProperty.setItems(filteredProperties);
             if(selected!=null){
                 int length = filteredProperties.length;
                 int i;
                 for(i=0; i<length; i++){
                     if(selected.equals(cProperty.getItem(i))){
                         cProperty.select(i);
                         break;
                     }
                 }
                 if(i==length){
                     cExpression.setItems(DEFAULT_EXPRESSIONS);
                 }
             }
         }
         updateTable();        
     }
     
     /**
      * Initialize startup properties.
      */
     private void initializeStartupProperties() {
         if (!setProperty(cDataset, initDataset)) {
             return;
         }
         cProperty.setItems(getProperties(initDataset));
         setProperty(cProperty, initProperty);
         cExpression.setItems(DEFAULT_EXPRESSIONS);
         if(!setProperty(cExpression, initExpression)){
             cExpression.setText(initExpression);
         }
         if(!initDatasets.isEmpty()){
             for(String currDataset : initDatasets.keySet()){
                 DatasetInfo datasetInfo = datasets.get(currDataset);
                 if(datasetInfo!=null){
                     for(String property : initDatasets.get(currDataset)){
                         datasetInfo.setPropertyVisible(property, false);
                     }
                 }
             }
         }
         updateTable();
     }
     
     /**
      * Sets value into property
      * 
      * @param combo - Combo
      * @param value - value
      * @return if sets is correctly - return true else false
      */
     private boolean setProperty(Combo combo, String value) {
         if (combo == null || value == null) {
             return false;
         }
         for (int i = 0; i < combo.getItemCount(); i++) {
             if (combo.getItem(i).equals(value)) {
                 combo.select(i);
                 return true;
             }
         }
         return false;
     }
     
     /**
      * Add listeners to components.
      */
     private void addListeners(){
         table.getTable().addListener (SWT.SetData, new Listener () {
             public void handleEvent (Event event) {
                 TableItem item = (TableItem) event.item;
                 final int index = table.getTable().indexOf (item);
                 contentProvider.uploadData(null,index);
             }
         });
 
         cDataset.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 updateProperty();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         cProperty.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 updateExpression();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         cExpression.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 updateTable();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
     }
     
     /**
      * Update properties after changed dataset
      */
     private void updateProperty(){
         String[] propNames =null;
         if(cDataset.getSelectionIndex()<0){
             table.getControl().setVisible(false);
         } else {
             String datasetName = cDataset.getText();
             propNames = getProperties(datasetName);
             if (propNames == null || propNames.length == 0) {
                 table.getControl().setVisible(false);
             }
         }
         cProperty.setItems(propNames);
         updateExpression();
     }
     
     /**
      * Update expressions after change property.
      */
     private void updateExpression(){
         if(cProperty.getSelectionIndex()<0){
             cExpression.clearSelection();
             cExpression.setItems(DEFAULT_EXPRESSIONS);
         }
         updateTable();
     }
     
     /**
      * Update table for new conditions.
      */
     private void updateTable(){
         if(cDataset.getSelectionIndex()<0){
             return;
         }
         String datasetName = cDataset.getText();
         String propertyName = null;
         if(cProperty.getSelectionIndex()>=0){
             propertyName = cProperty.getText();
         }
         String expressionMask = cExpression.getText();
         expressionMask = expressionMask.length()==0?null:expressionMask;        
         setEnableAll(false);
         table.setInput(new InputTableData(datasetName,propertyName,expressionMask));
         setEnableAll(true);
     }
     
     /**
      * Set enabled for all components
      *
      * @param isEnable
      */
     private void setEnableAll(boolean isEnable){        
         cDataset.setEnabled(isEnable);
         cProperty.setEnabled(isEnable);
         cExpression.setEnabled(isEnable);
         actClearFilter.setEnabled(isEnable);
         actCommit.setEnabled(isEnable);
         actRollback.setEnabled(isEnable);
         actConfigure.setEnabled(isEnable);
         table.getControl().setVisible(isEnable);
     }
 
     /**
      * Update datasets.
      */
     public void updateDatasetNodes(){
         initDatasetsInfo();
         cDataset.setItems(getDatasets());
     }
     
     @Override
     public void setFocus() {
     }
     
     /**
      * Initialize all datasets information.
      *
      * @return
      */
     private HashMap<String, DatasetInfo> initDatasetsInfo(){
         GraphDatabaseService service = NeoServiceProvider.getProvider().getService();
         Transaction tx = service.beginTx();
         LinkedHashMap<String, Node> allDatasetNodes = getAllDatasetNodes(service);
         HashMap<String, DatasetInfo> result = new HashMap<String, DatasetInfo>(allDatasetNodes.size());
         try{
             for(String key : allDatasetNodes.keySet()){
                 result.put(key, new DatasetInfo(allDatasetNodes.get(key),service));
             }
         }finally{
             tx.finish();
         }        
         return result;
     }
     
     /**
      * Returns all dataset Nodes.
      *
      * @param service GraphDatabaseService service
      * @return LinkedHashMap<String, Node>
      */
     private LinkedHashMap<String, Node> getAllDatasetNodes(GraphDatabaseService service){
         LinkedHashMap<String, Node> result = NeoUtils.getAllDatasetNodes(service);
         Node refNode = service.getReferenceNode();
         for (Relationship relationship : refNode.getRelationships(Direction.OUTGOING)) {
             Node node = relationship.getEndNode();
             Object type = node.getProperty(INeoConstants.PROPERTY_GIS_TYPE_NAME, "").toString(); //$NON-NLS-1$
             if (NeoUtils.isGisNode(node) && type.equals(GisTypes.NETWORK.getHeader()) || NodeTypes.OSS.checkNode(node)) {
                 String id = NeoUtils.getSimpleNodeName(node, null);
                 result.put(id, node);
             }
         }
 
         return result;
     }
 
     /**
      * Returns datasets names.
      *
      * @return String[]
      */
     private String[] getDatasets(){
         return convertListToArray(new ArrayList<String>(datasets.keySet()));
     }
     
     /**
      * Returns properties names.
      *
      * @return String[]
      */
     private String[] getProperties(String aDataset){
         return datasets.get(aDataset).getFilteredProperties();
     }
     
     /**
      * Convert list of strings to sorted array
      *
      * @param list
      * @return String[]
      */
     private String[] convertListToArray(List<String> list){
         String[] result = new String[list.size()];
         result = list.toArray(result);
         Arrays.sort(result);
         return result;
     }
     
     /**
      * 
      * Dataset information.
      * <p>
      *
      * </p>
      * @author Shcharbatsevich_A
      * @since 1.0.0
      */
     private class DatasetInfo{
         
         private Node dataset;
         private DataTypes type;
         private HashMap<String, Boolean> allProperties;
         private List<String> filteredProperties;
         
         /**
          * Constructor.
          * @param aDataset
          * @param service
          */
         public DatasetInfo(Node aDataset, GraphDatabaseService service) {
             dataset = aDataset;
             type = DataTypes.getTypeByNode(dataset,service);
             initProperties();
         }
         
         /**
          * Initilize properties.
          */
         private void initProperties(){
             allProperties = new HashMap<String, Boolean>();
             filteredProperties = new ArrayList<String>();
             Node propRoot = NeoUtils.findGisNodeByChild(dataset);
             if(propRoot == null){
                 propRoot = dataset;
             }
             PropertyHeader header = new PropertyHeader(propRoot);
             String[] propNames = header.getStringFields();
             if(propNames==null){
                 return;
             }
             for(String property : propNames){
                 allProperties.put(property, true);
                 filteredProperties.add(property);
             }
             propNames = header.getNumericFields();
             if(propNames==null){
                 return;
             }
             for(String property : propNames){
                 allProperties.put(property, true);
             }
         }
         
         /**
          * @return Returns the type.
          */
         public DataTypes getType() {
             return type;
         }
         
         /**
          * @return Returns the dataset.
          */
         public Node getDataset() {
             return dataset;
         }
         
         /**
          * @return Returns the properties.
          */
         public String[] getAllProperties() {
             return convertListToArray(new ArrayList<String>(allProperties.keySet()));
         }
         
         /**
          * Returns only visible properties.
          *
          * @return String[]
          */
         public String[] getVisibleProperties(){
             List<String> result = new ArrayList<String>(allProperties.keySet().size());
             for(String property : allProperties.keySet()){
                 if(allProperties.get(property)){
                     result.add(property);
                 }
             }
             return convertListToArray(result);
         }
         
         /**
          * Returns only visible properties that can be in filter (string properties).
          *
          * @return String[]
          */
         public String[] getFilteredProperties(){
             List<String> result = new ArrayList<String>(filteredProperties.size());
             for(String property : filteredProperties){
                 if(allProperties.get(property)){
                     result.add(property);
                 }
             }
             return convertListToArray(result);
         }
         
         /**
          * Returns only invisible properties.
          *
          * @return String[]
          */
         public String[] getNotVisibleProperties(){
             List<String> result = new ArrayList<String>(allProperties.keySet().size());
             for(String property : allProperties.keySet()){
                 if(!allProperties.get(property)){
                     result.add(property);
                 }
             }
             return convertListToArray(result);
         }
         
         /**
          * Sets property visibility.
          *
          * @param propName
          * @param isVisible
          */
         public void setPropertyVisible(String propName, boolean isVisible){
             allProperties.put(propName,isVisible);
         }
         
         @Override
         public String toString() {
             return allProperties.toString();
         }
         
     }
     
     /**
      * 
      * Label provider for table.
      * <p>
      *
      * </p>
      * @author Shcharbatsevich_A
      * @since 1.0.0
      */
     private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
         
         protected static final int DEF_SIZE = 0;
         protected static final int REAL_SIZE = 150;
         
         private List<TableColumn> columns;
 
         @Override
         public Image getColumnImage(Object element, int columnIndex) {
             return null;
         }
 
         @Override
         public String getColumnText(Object element, int columnIndex) {
             if (element instanceof TableRowWrapper) {
                 TableRowWrapper row = (TableRowWrapper)element;
                 return row.getValue(columnIndex);
             } else {
                 return getText(element);
             }
         }
         
         /**
          * Create all table columns.
          */
         public void createTableColumns() {
             Table tabl = table.getTable();
             int colCount = getMaxColumnCount();
             columns = new ArrayList<TableColumn>(colCount);
             for(int i=0; i<colCount;i++){
                 TableViewerColumn column = new TableViewerColumn(table, SWT.LEFT);
                 TableColumn col = column.getColumn();
                 col.setText("");
                 columns.add(col);
                 col.setWidth(DEF_SIZE);
                 col.setResizable(true);
             }
             tabl.setHeaderVisible(true);
             tabl.setLinesVisible(true);
             table.setLabelProvider(this);
             table.refresh();
         }
         
         /**
          * Refresh table.
          *
          * @param properties
          */
         public void refreshTable(String[] properties){
             clearColumns();
             int count = properties.length;
             for(int i=0; i<count; i++){
                 TableColumn col = columns.get(i);
                 col.setText(properties[i]);
                 col.setWidth(REAL_SIZE);
             }
         }
         
         /**
          * Clear all columns.
          */
         private void clearColumns(){
             for(TableColumn col : columns){
                 col.setText("");
                 col.setWidth(DEF_SIZE);
             }
         }
         
         /**
          * Returns maximum columns that will be needed.
          *
          * @return
          */
         private int getMaxColumnCount(){
             int result = 0;
             for(String key : datasets.keySet()){
                 int curr = datasets.get(key).getAllProperties().length;
                 if(curr>result){
                     result = curr;
                 }
             }
             return result;
         }
         
     }
     
     /**
      * Table content provider.
      * <p>
      *
      * </p>
      * @author Shcharbatsevich_A
      * @since 1.0.0
      */
     private class TableContentProvider implements IStructuredContentProvider {
         
         private static final int PAGE_SIZE = 100;
 
         private List<TableRowWrapper> rows = new ArrayList<TableRowWrapper>();
         private Iterator<Node> allNodes;
         private String dataset;
         private String[] properties;
         
         @Override
         public Object[] getElements(Object inputElement) {
             return rows.toArray(new TableRowWrapper[0]);
         }
 
         @Override
         public void dispose() {
         }
 
         @Override
         public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
             if (newInput == null || !(newInput instanceof InputTableData)) {
                 return;
             }
             InputTableData data = (InputTableData)newInput;
             if (!data.isNeedUpdate()) {
                 return;
             }
             dataset = data.getDataset();
             String filter = data.getProperty();
             properties = datasets.get(dataset).getVisibleProperties();
             if(filter!=null){
                 int filterInd = 0;
                 for(int i=0; i<properties.length;i++){
                     if(properties[i].equals(filter)){
                         filterInd = i;
                         break;
                     }                                    
                 }
                 if(filterInd>0){
                     for(int i=filterInd-1;i>=0;i--){
                         properties[i+1]=properties[i];
                     }
                     properties[0]=filter;
                 }
             }
             labelProvider.refreshTable(properties);
             rows.clear();
             uploadData(data,0);            
             table.getControl().setVisible(true);
         }
 
         /**
          * Upload data for table.
          *
          * @param inputData InputTableData
          * @param index int
          */
         public void uploadData(final InputTableData inputData, final int index) {
            
             Job updateJob = new Job("Upload data to table job") {            
                 @Override
                protected IStatus run(IProgressMonitor monitor) {
                     if((rows.size()-index)>PAGE_SIZE/4){
                         return Status.OK_STATUS;
                     }
                     DatasetInfo datasetInfo = datasets.get(dataset);
                     if(datasetInfo==null){
                         return Status.OK_STATUS;
                     }
                     GraphDatabaseService service = NeoServiceProvider.getProvider().getService();
                     Transaction tx = service.beginTx();            
                     try{
                         if (inputData != null) {
                             NodeTypes childType = datasetInfo.getType().getChildType();
                             allNodes = inputData.getNodesByFilter(service, childType);                            
                         }
                         int start = 0;
                         while(allNodes.hasNext()&&start<PAGE_SIZE){
                             rows.add(parseRow(allNodes.next(), properties));
                             start++;
                         }
                     }finally{
                         tx.finish();
                     }
                     return Status.OK_STATUS;
                 }
             };
             updateJob.schedule(0);
             try {
                 updateJob.join();
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
             
             table.refresh();
         }
         
         /**
          * Parse table row 
          *
          * @param node Node
          * @param properties String[]
          * @return TableRowWrapper
          */
         private TableRowWrapper parseRow(Node node, String[] properties){
             TableRowWrapper row = new TableRowWrapper();
             List<String> values = new ArrayList<String>(properties.length);
             for(String property : properties){
                 String value = node.getProperty(property, "").toString();
                 values.add(value);
             }
             row.setValues(values);
             return row;
         }
     }
     
     /**
      * Data for build table.
      * <p>
      *
      * </p>
      * @author Shcharbatsevich_A
      * @since 1.0.0
      */
     private class InputTableData{
         private String dataset;
         private String property;
         private String expression;
         
         /**
          * Constructor.
          * @param datasetName
          * @param propertyName
          * @param expressionMask
          */
         public InputTableData(String datasetName, String propertyName, String expressionMask) {
             dataset = datasetName;
             property = propertyName;
             expression = expressionMask;
         }
         
         /**
          * Get nodes iterator.
          *
          * @param service NeoService
          * @param childType NodeTypes
          * @return Iterator
          */
         private Iterator<Node> getNodesByFilter(final GraphDatabaseService service, final NodeTypes childType){
             Node datasetNode = datasets.get(dataset).getDataset();
             Iterator<Node> result = datasetNode.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
                 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     Node currentNode = currentPos.currentNode();
                     NodeTypes type = NodeTypes.getNodeType(currentNode, service);
                     if(type==null || !type.equals(childType)){
                         return false;
                     }
                     if(property==null){
                         return true;
                     }
                     Object objValue = currentNode.getProperty(property, null);
                     String realValue = objValue==null?null:objValue.toString();
                     if(expression.equals(EXPRESSION_EMPTY)){
                         return realValue==null;
                     }
                     if(expression.equals(EXPRESSION_NOT_EMPTY)){
                         return realValue!=null;
                     }
                     return isGoodValue(realValue);
                 }
             }, GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING,
                GeoNeoRelationshipTypes.NEXT,Direction.OUTGOING).iterator();
             return result;
         }
         
         /**
          * Is property correct.
          *
          * @param value String
          * @return boolean
          */
         private boolean isGoodValue(String value){
             return value!=null&&Pattern.matches(expression, value);
         }
         
         /**
          * @return Returns the dataset.
          */
         public String getDataset() {
             return dataset;
         }
         
         /**
          * @return Returns the property.
          */
         public String getProperty() {
             return property;
         }
          
         /**
          * Is table need update.
          *
          * @return boolean
          */
         public boolean isNeedUpdate(){
             if(dataset==null){
                 return false;
             }
             if(property == null){
                 return true;
             }
             return expression!=null;
         }
         
     }
     
     /**
      * Table row.
      * <p>
      *
      * </p>
      * @author Shcharbatsevich_A
      * @since 1.0.0
      */
     private class TableRowWrapper{
         private List<String> values;
         
         /**
          * @param values The values to set.
          */
         public void setValues(List<String> values) {
             this.values = values;
         }
         
         /**
          * Returns value by index;
          *
          * @param index
          * @return String
          */
         public String getValue(int index){
             if(index>=values.size()){
                 return "";
             }
             return values.get(index);
         }
     }
     
     @Override
     public void saveState(IMemento memento) {
         super.saveState(memento);
         memento.putString(MEM_DATASET, cDataset.getText());
         memento.putString(MEM_PROPERTY, cProperty.getText());
         memento.putString(MEM_EXPRESSION, cExpression.getText());
         int datasetsCount = 0;
         for(String dataset : datasets.keySet()){
             memento.putString(MEM_DATASET_MAP+datasetsCount++, dataset);
             String[] invisible = datasets.get(dataset).getNotVisibleProperties();
             int propCount = invisible.length;
             String memKey = getMemKey(dataset);
             memento.putInteger(MEM_PROPERTY_COUNT+memKey, propCount);
             for(int i=0; i<propCount;i++){
                 memento.putString(MEM_PROPERTY_MAP+memKey+i, invisible[i]);
             }
         }
         memento.putInteger(MEM_DATASET_MAP_COUNT, datasetsCount);
     }
 
     /**
      * Convert dataset name to key for memento.
      *
      * @param dataset
      * @return String
      */
     private String getMemKey(String dataset) {
         String memKey = dataset.replace(" ", "_");
         memKey = memKey.replace(".", "_");
         memKey = memKey.replace("%", "_");
         memKey = memKey.replace("/", "_");
         memKey = memKey.replace("\\", "_");
         memKey = memKey.replace("+", "_");
         return memKey;
     }
     
     @Override
     public void init(IViewSite site, IMemento memento) throws PartInitException {
         super.init(site, memento);
         if (memento == null) {
             return;
         }
         initDataset = memento.getString(MEM_DATASET);
         initProperty = memento.getString(MEM_PROPERTY);
         initExpression = memento.getString(MEM_EXPRESSION);
         int datasetsCount = memento.getInteger(MEM_DATASET_MAP_COUNT);
         initDatasets = new HashMap<String, List<String>>(datasetsCount);
         for(int i=0;i<datasetsCount;i++){
             String dataset = memento.getString(MEM_DATASET_MAP+i);
             String memKey = getMemKey(dataset);
             int propsCount = memento.getInteger(MEM_PROPERTY_COUNT+memKey);
             List<String> props = new ArrayList<String>(propsCount);
             for(int j=0; j<propsCount;j++){
                 props.add(MEM_PROPERTY_MAP+memKey+i);
             }
             initDatasets.put(dataset, props);
         }
         
     }
 }
