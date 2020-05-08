 /*
 * Original Code developed and contributed by Prime Technology.
 * Subsequent Code Modifications Copyright 2011-2012 ICEsoft Technologies Canada Corp. (c)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * NOTE THIS CODE HAS BEEN MODIFIED FROM ORIGINAL FORM
 *
 * Subsequent Code Modifications have been made and contributed by ICEsoft Technologies Canada Corp. (c).
 *
 * Code Modification 1: Integrated with ICEfaces Advanced Component Environment.
 * Contributors: ICEsoft Technologies Canada Corp. (c)
 */
 
 package org.icefaces.ace.component.datatable;
 
 import org.icefaces.ace.component.ajax.AjaxBehavior;
 import org.icefaces.ace.component.column.Column;
 import org.icefaces.ace.component.columngroup.ColumnGroup;
 import org.icefaces.ace.component.row.Row;
 import org.icefaces.ace.component.rowexpander.RowExpander;
 import org.icefaces.ace.component.rowpanelexpander.RowPanelExpander;
 import org.icefaces.ace.component.tableconfigpanel.TableConfigPanel;
 import org.icefaces.ace.event.*;
 import org.icefaces.ace.model.MultipleExpressionComparator;
 import org.icefaces.ace.model.filter.ContainsFilterConstraint;
 import org.icefaces.ace.model.table.LazyDataModel;
 import org.icefaces.ace.model.table.*;
 import org.icefaces.ace.model.table.SortCriteria;
 import org.icefaces.ace.util.ComponentUtils;
 import org.icefaces.ace.util.collections.AllPredicate;
 import org.icefaces.ace.util.collections.AnyPredicate;
 import org.icefaces.ace.util.collections.Predicate;
 import org.icefaces.ace.util.collections.PropertyConstraintPredicate;
 
 import javax.el.MethodExpression;
 import javax.faces.FacesException;
 import javax.faces.application.Application;
 import javax.faces.application.NavigationHandler;
 import javax.faces.component.*;
 import javax.faces.component.behavior.ClientBehavior;
 import javax.faces.component.visit.VisitCallback;
 import javax.faces.component.visit.VisitContext;
 import javax.faces.component.visit.VisitHint;
 import javax.faces.component.visit.VisitResult;
 import javax.faces.context.FacesContext;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.PhaseId;
 import javax.faces.event.PostValidateEvent;
 import javax.faces.event.PreValidateEvent;
 import javax.faces.model.*;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import java.sql.ResultSet;
 import javax.faces.model.ListDataModel;
 import javax.faces.model.ResultDataModel;
 import javax.faces.model.ResultSetDataModel;
 import javax.faces.model.ScalarDataModel;
 import javax.faces.render.Renderer;
 import javax.faces.view.Location;
 
 public class DataTable extends DataTableBase {
     private static Logger log = Logger.getLogger(DataTable.class.getName());
     private static Class SQL_RESULT = null;
 
     // Cached results
     private Map<String, Column> filterMap;
     private TableConfigPanel panel;
     private RowStateMap stateMap;
     private DataModel model;
 
     static {
         try {
             SQL_RESULT = Class.forName("javax.servlet.jsp.jstl.sql.Result");
         } catch (Throwable t)  {
             //ignore if sql.result not available
         }
     }
 
     /*#######################################################################*/
     /*###################### Overridden API #################################*/
     /*#######################################################################*/
     @Override
     public RowStateMap getStateMap() {
         if (stateMap != null) return stateMap;
 
         stateMap = super.getStateMap();
         if (stateMap == null) {
             stateMap = new RowStateMap();
             super.setStateMap(stateMap);
         }
 
         return stateMap;
     }
 
     // Allow renderer to void state map between iterations to avoid
     // sharing stateMap due to caching
     // (caching is necessary to avoid attempting to load a stateMap when the clientId contains a row index)
     protected void clearCachedStateMap() {
         stateMap = null;
     }
 
     @Override
     public Object getValue() {
         Object superValue = super.getValue();
         int superValueHash;
         if (superValue != null) superValueHash = superValue.hashCode();
         else return null;
 
         if (getValueHashCode() == null || superValueHash != getValueHashCode()) {
             setValueHashCode(superValueHash);
             applySorting();
             if (getFilteredData() != null) applyFilters();
             if (superValue != null && superValue instanceof List) {
                 List list = (List)superValue;
             }
         }
 
         List filteredValue = getFilteredData();
         return (filteredValue != null) ? filteredValue : superValue;
     }
 
     @Override
     protected DataModel getDataModel() {
         if (this.model != null) {
             return (model);
         }
 
         Object current = getValue();
 
         // If existing tree check for changes or return cached model
         if (current == null) {
             setDataModel(new ListDataModel(Collections.EMPTY_LIST));
         } else if (current instanceof DataModel) {
             setDataModel((DataModel) current);
         } else if (current instanceof List) {
             List list = (List)current;
             if (list.size() > 0 && list.get(0) instanceof Map.Entry)
                 setDataModel(new TreeDataModel(list));
             else
                 setDataModel(new ListDataModel(list));
         } else if (Object[].class.isAssignableFrom(current.getClass())) {
             setDataModel(new ArrayDataModel((Object[]) current));
         } else if (current instanceof ResultSet) {
             setDataModel(new ResultSetDataModel((ResultSet) current));
         } else if ((null != SQL_RESULT) && SQL_RESULT.isInstance(current)) {
             DataModel dataModel = new ResultDataModel();
             dataModel.setWrappedData(current);
             setDataModel(dataModel);
         } else {
             setDataModel(new ScalarDataModel(current));
         }
 
         return model;
     }
 
     @Override
     protected void setDataModel(DataModel dataModel) {
         this.model = dataModel;
     }
  
     @Override
     public void broadcast(javax.faces.event.FacesEvent event) throws AbortProcessingException {
         super.broadcast(event);
 
         FacesContext context = FacesContext.getCurrentInstance();
         String outcome = null;
         MethodExpression me = null;
 
         if      (event instanceof SelectEvent)   me = getRowSelectListener();
         else if (event instanceof UnselectEvent) me = getRowUnselectListener();
         else if (event instanceof TableFilterEvent) me = getFilterListener();
 
         if (me != null) outcome = (String) me.invoke(context.getELContext(), new Object[] {event});
 
         if (outcome != null) {
             NavigationHandler navHandler = context.getApplication().getNavigationHandler();
             navHandler.handleNavigation(context, null, outcome);
             context.renderResponse();
         }
     }
 
     @Override
     public void processUpdates(FacesContext context) {
         if (context == null) {
             throw new NullPointerException();
         }
         if (!isRendered()) {
             return;
         }
 
         pushComponentToEL(context, this);
         //preUpdate(context);
         iterate(context, PhaseId.UPDATE_MODEL_VALUES);
         popComponentFromEL(context);
         // This is not a EditableValueHolder, so no further processing is required
     }
 
     @Override
     public void processDecodes(FacesContext context) {
         // Required to prevent input component processing on filter and pagination initiated submits.
         if (!isAlwaysExecuteContents() && isTableFeatureRequest(context)) {
             this.decode(context);
         } else {
             if (context == null) {
                 throw new NullPointerException();
             }
             if (!isRendered()) {
                 return;
             }
 
             pushComponentToEL(context, this);
             //super.preDecode() - private and difficult to port
             iterate(context, PhaseId.APPLY_REQUEST_VALUES);
             decode(context);
             popComponentFromEL(context);
         }
 
         if (isFilterValueChanged() == true) {
             Map<String, String> params = context.getExternalContext().getRequestParameterMap();
             queueEvent(
                     new TableFilterEvent(this,
                             getFilterMap().get(params.get(getClientId(context) + "_filteredColumn")))
             );
         }
     }
 
     @Override
     public void processValidators(FacesContext context) {
 
         if (context == null) {
             throw new NullPointerException();
         }
         if (!isRendered()) {
             return;
         }
         pushComponentToEL(context, this);
         Application app = context.getApplication();
         app.publishEvent(context, PreValidateEvent.class, this);
         //preValidate(context);
         iterate(context, PhaseId.PROCESS_VALIDATIONS);
         app.publishEvent(context, PostValidateEvent.class, this);
         popComponentFromEL(context);
     }
 
     @Override
     public int getFirst() {
         return isPaginator() ? super.getFirst() : 0;
     }
 
     @Override
     public void setRowIndex(int index) {
         Map<String, Object> requestMap = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
 
         super.setRowIndex(index);
         if (index > -1 && isRowAvailable()) {
             requestMap.put(getRowStateVar(), getStateMap().get(getRowData()));
         }
     }
 
 
 
 
 
     /*#######################################################################*/
     /*###################### Public API #####################################*/
     /*#######################################################################*/
 
     /**
      * A public proxy to the getDataModel() method, intended for use in situations
      * where a sub-component needs access to a custom DataModel object.
      *
      * @return java.faces.model.DataModel instance currently used by this table
      */
     public DataModel getModel() {
         return getDataModel();
     }
 
     /**
      * A public proxy to the getDataModel() method, intended for use in situations
      * where a sub-component needs to null a cached DataModel to force regeneration.
      *
      * @return java.faces.model.DataModel instance currently used by this table
      */
     public void setModel(DataModel m) {
         try { setDataModel(null); }
         catch (UnsupportedOperationException uoe) {
             //MyFaces doesn't support this method and throws an UnsupportedOperationException
         }
     }
 
     /**
      * Determine if this DataTable is using a custom ICEFaces TreeDataModel.
      * @return true, if a TreeDataModel instance is the result of getDataModel()
      */
     public Boolean hasTreeDataModel() {
         return (model instanceof TreeDataModel);
     }
 
     /**
      * If a RowPanelExpander component is a child of this table, return it.
      * This is intended for table sub-components to vary their behavior varied
      * on the presence of RowPanelExpanders and/or RowExpanders.
      * @return RowPanelExpander child of the table, or null
      */
     public RowPanelExpander getPanelExpansion() {
         for (UIComponent kid : getChildren())
             if (kid instanceof RowPanelExpander) return (RowPanelExpander) kid;
         return null;
     }
 
     /**
      * If a RowExpander component is a child of this table, return it.
      * This is intended for table sub-components to vary their behavior varied
      * on the presence of RowPanelExpanders and/or RowExpanders.
      * @return RowExpander child of the table, or null
      */
     public RowExpander getRowExpansion() {
         for (UIComponent kid : getChildren())
             if (kid instanceof RowExpander) return (RowExpander) kid;
         return null;
     }
 
     /**
      * Generates the list of DataTable Column children, reordered according to the
      * column ordering property, or the header ColumnGroup columns in page order.
      * @param headColumns Enable to return the header columns in page order
      * @return List of ACE Column Components.
      */
     public List<Column> getColumns(boolean headColumns) {
         if (headColumns) {
             ArrayList<Column> columns = new ArrayList<Column>();
 
             for (UIComponent child : getColumnGroup("header").getChildren()) {
                 if (child instanceof Row) {
                     for (UIComponent gchild : child.getChildren()) {
                         if (gchild instanceof Column) {
                             columns.add((Column)gchild);
                         }
                     }
                 }
             }
 
             return columns;
         } else {
             ArrayList<Column> columns = new ArrayList<Column>();
             List<Integer> columnOrdering = generateColumnOrdering();
 
             ArrayList<Column> unordered = new ArrayList<Column>();
             Stack childStack = new Stack<UIComponent>();
             childStack.add(this);
             while (!childStack.empty()) {
                 for (UIComponent child : ((UIComponent)childStack.pop()).getChildren()) {
                     if (!(child instanceof ColumnGroup) && !(child instanceof Column) && !(child instanceof DataTable)) {
                         if (child.getChildren().size() > 0) childStack.add(child);
                     } else if (child instanceof Column) unordered.add((Column) child);
                 }
             }
 
             // Allow the ordering to grow beyond the current set of columns,
             // to allow persistence of order during column swaps.
             while (columnOrdering.size() < unordered.size()) columnOrdering.add(columnOrdering.size());
 
             for (Integer i : columnOrdering)
                 if (i < unordered.size()) columns.add(unordered.get(i));
 
             return columns;
         }
     }
 
     /**
      * Generates the list of DataTable Column children, reordered according to the
      * column ordering property. Note this list doesn't return Column components used
      * in a ColumnGroup to define the header.
      * @return List of ACE Column Components.
      */
     public List<Column> getColumns() {
         return getColumns(false);
     }
 
     /**
      * Generates a list of DataTable Column children intended to render the header, either from the header segement, or
      * from the normal grouping of columns.
      * @return List of ACE Column  Components.
      */
     public List<Column> getHeaderColumns() {
         return null;
     }
 
     /**
      * Associates this table with a particular TableConfigPanel component. That
      * table will render the launch controls, and be configured by the specified
      * panel.
      * @param TableConfigPanel component that will configure this table.
      */
     public void setTableConfigPanel(TableConfigPanel panel) {
         this.panel = panel;
         FacesContext c = FacesContext.getCurrentInstance();
         setTableConfigPanel(panel.getClientId(c)) ;
     }
 
     /**
      * Sets the property value of this dataTable to null.
      */
     public void resetValue() {
         setValue(null);
     }
 
     /**
      * Sets the position of pagination in the table to the first page.
      */
     public void resetPagination() {
         setFirst(0);
         setPage(1);
     }
 
     /**
      * Sets the property of this table to null, clears all filters and resets pagination.
      */
     public void reset() {
         resetValue();
         resetFilters();
         resetPagination();
     }
 
     /**
      * Blanks the sortPriority and set to false the sortAscending property of each Column
      * component.
      */
     public void resetSorting() {
         for (Column c : getColumns()) {
             c.setSortPriority(null);
             c.setSortAscending(false);
         }
         for (Column c : getColumns(true)) {
             c.setSortPriority(null);
             c.setSortAscending(false);
         }
     }
 
     /**
      * Blanks the filterValue property of each Column component and removes the
      * presently filtered set of data.
      */
     public void resetFilters() {
         for (Column c : getColumns()) {
             c.setFilterValue("");
         }
         for (Column c : getColumns(true)) {
             c.setFilterValue("");
         }
         setFilterValue("");
         setFilteredData(null);
     }
 
     /**
      * Processes any changes to sortPriority or sortAscending properties of Columns
      * to the data model; resorting the table according to the new settings.
      */
     public void applySorting() {
         setSortOrderChanged(true);
     }
 
     /**
      * Processes any changes to the filterInput property of the Columns to the data model;
      * refiltering the data model to meet the new criteria.
      */
     public void applyFilters() {
         setFilterValueChanged(true);
     }
 
     public Boolean isFilterValueChanged() {
         return (isConstantRefilter()) ? true : super.isFilterValueChanged();
     }
 
 
     /*#######################################################################*/
     /*###################### Protected API ##################################*/
     /*#######################################################################*/
     protected boolean isPaginationRequest(FacesContext x)         { return isIdPrefixedParamSet("_paging", x); }
     protected boolean isTableConfigurationRequest(FacesContext x) { return isIdPrefixedParamSet("_tableconf", x); }
     protected boolean isColumnReorderRequest(FacesContext x)      { return isIdPrefixedParamSet("_columnReorder", x); }
     protected boolean isSortRequest(FacesContext x)               { return isIdPrefixedParamSet("_sorting", x); }
     protected boolean isFilterRequest(FacesContext x)             { return isIdPrefixedParamSet("_filtering", x); }
     protected boolean isInstantSelectionRequest(FacesContext x)   { return isIdPrefixedParamSet("_instantSelectedRowIndex", x); }
     protected boolean isInstantUnselectionRequest(FacesContext x) { return isIdPrefixedParamSet("_instantUnselectedRowIndex", x); }
     protected boolean isScrollingRequest(FacesContext x)          { return isIdPrefixedParamSet("_scrolling", x); }
     protected boolean isTableFeatureRequest(FacesContext x)       { return isColumnReorderRequest(x) || isScrollingRequest(x) || isInstantUnselectionRequest(x) || isInstantSelectionRequest(x) || isPaginationRequest(x) || isFilterRequest(x) || isSortRequest(x) || isTableConfigurationRequest(x); }
 
 
     protected Map<String,Column> getFilterMap() {
         if (filterMap == null) {
             filterMap = new HashMap<String,Column>();
             ColumnGroup group = getColumnGroup("header");
             if (group != null) {
                 for (UIComponent child : group.getChildren())
                     if (child.isRendered()) for (UIComponent grandchild : child.getChildren())
                         if (grandchild.isRendered() && grandchild.getValueExpression("filterBy") != null)
                             filterMap.put(grandchild.getClientId(FacesContext.getCurrentInstance()) + "_filter", (Column)grandchild);
             } else
                 for (Column column : getColumns())
                     if (column.getValueExpression("filterBy") != null)
                         filterMap.put(column.getClientId(FacesContext.getCurrentInstance()) + "_filter", column);
         }
         return filterMap;
     }
 
     protected ColumnGroup getColumnGroup(String target) {
         for (UIComponent child : this.getChildren())
             if (child instanceof ColumnGroup) {
                 ColumnGroup colGroup = (ColumnGroup) child;
                 if (target.equals(colGroup.getType())) return colGroup;
             }
         return null;
     }
 
     protected SortCriteria[] getSortCriteria() {
         ArrayList<Column> sortableColumns = new ArrayList<Column>();
 
         ColumnGroup group = getColumnGroup("header");
         if (group != null) {
             for (UIComponent child : group.getChildren()) { // child is a Row
                 for (UIComponent headerRowChild : child.getChildren()) {
                     if (headerRowChild instanceof Column) {
                         Column c = (Column)headerRowChild;
                         if (c.getSortPriority() != null) {
                             sortableColumns.add(c);
                         }
                     }
                 }
             }
         } else {
             for (Column c : getColumns()) {
                 if (c.getSortPriority() != null) {
                     sortableColumns.add(c);
                 }
             }
         }
 
         Collections.sort(sortableColumns, new PriorityComparator());
 
         SortCriteria[] criterias = new SortCriteria[sortableColumns.size()];
         int i = 0;
         for (Column c : sortableColumns) {
             Comparator<Object> comp = c.getSortFunction();
             if (comp == null) criterias[i] = new SortCriteria(c.getValueExpression("sortBy"), c.isSortAscending());
             else criterias[i] = new SortCriteria(c.getValueExpression("sortBy"), c.isSortAscending(), comp);
             i++;
         }
         return criterias;
     }
 
     protected Map<String,String> getFilters() {
         HashMap<String, String> map = new HashMap<String, String>();
         for (Column c : getColumns()) {
             String value = c.getFilterValue();
             if (value != null && (value.length() > 0))
                 map.put(ComponentUtils.resolveField(c.getValueExpression("filterBy")), value);
         }
         return map;
     }
 
     protected boolean hasHeaders() {
         for (UIComponent c : getChildren()) {
             if (c instanceof Column && ((c.getFacet("header") != null) || (((Column)c).getHeaderText() != null))) return true;
             else if (c instanceof ColumnGroup && ((ColumnGroup)c).getType().equals("header")) return true;
         }
 
         return false;
     }
 
     public boolean hasSelectionClientBehaviour() {
         List<ClientBehavior> selectBehaviors = getClientBehaviors().get("select");
 
         if (selectBehaviors != null)
         for (ClientBehavior b : selectBehaviors)
             if (b instanceof AjaxBehavior) {
                 if (!((AjaxBehavior) b).isDisabled())
                     return true;
             }
 
         List<ClientBehavior> deselectBehaviors = getClientBehaviors().get("deselect");
 
         if (deselectBehaviors != null)
         for (ClientBehavior b : deselectBehaviors)
             if (b instanceof AjaxBehavior) {
                 if (!((AjaxBehavior) b).isDisabled())
                     return true;
             }
 
         return false;
     }
 
     protected boolean hasFooterColumn(List<Column> columns) {
         for (Column column : columns)
             if (column.getFacet("footer") != null || column.getFooterText() != null)
                 return true;
         return false;
     }
 
     protected boolean isSelectionEnabled() {
         return this.getSelectionMode() != null;
     }
 
     protected boolean isCellSelection() {
         String selectionMode = this.getSelectionMode();
         if (selectionMode != null) return selectionMode.indexOf("cell") != -1;
         else return false;
     }
 
     protected boolean isSingleSelectionMode() {
         String selectionMode = this.getSelectionMode();
         if (selectionMode != null)
             return selectionMode.equalsIgnoreCase("single") || selectionMode.equalsIgnoreCase("singlecell");
         else return false;
     }
 
     protected TableConfigPanel findTableConfigPanel(FacesContext context) {
         if (panel == null & getTableConfigPanel() != null) {
             panel = (TableConfigPanel)this.findComponent(getTableConfigPanel());
 
             if (panel == null)
                 for (UIComponent child : getChildren())
                     if (child instanceof TableConfigPanel)
                         panel = (TableConfigPanel)child;
         }
         return panel;
     }
 
     protected void setColumnOrdering(String[] indexes) {
         ArrayList<Integer> ints = new ArrayList<Integer>();
         int i;
         for (String index : indexes)
             ints.add(Integer.parseInt(index));
 
         setColumnOrdering(ints);
     }
 
     protected void calculatePage() {
         int rows = this.getRows();
         int currentPage = this.getPage();
         int numberOfPages = (int) Math.ceil(this.getRowCount() * 1d / rows);
 
         // If paging to beyond the last page.
         if (currentPage > numberOfPages && numberOfPages > 0) {
             this.setPage(numberOfPages);
         } else if (currentPage < 1) {
             this.setPage(1);
         }
 
         this.setFirst((this.getPage()-1) * rows);
     }
 
     protected void processSorting() {
         Object value = getValue();
         if (value instanceof List) {
             List list = (List)value;
             SortCriteria[] criterias = getSortCriteria();
             String rowVar = getVar();
 
             if (criterias != null && criterias.length > 0) {
                 if (list.size() > 0 && list.get(0) instanceof Map.Entry)
                     Collections.sort(list, new EntryKeyComparatorWrapper(new MultipleExpressionComparator(criterias, rowVar)));
                 else
                     Collections.sort(list, new MultipleExpressionComparator(criterias, rowVar));
             }
         }
         setSortOrderChanged(false);
     }
 
     protected List processFilters(FacesContext context) {
         try {
             Map<String, Column> filterMap = getFilterMap();
             String globalFilter = getFilterValue();
             List<Predicate> columnPredicates = new ArrayList<Predicate>();
             List<Predicate> globalPredicates = new ArrayList<Predicate>();
             boolean hasGlobalFilter = (globalFilter != null && !globalFilter.equals(""));
             if (hasGlobalFilter) globalFilter = globalFilter.toLowerCase();
 
             // Setup filter objects from column properties
             for (Column c : filterMap.values()) {
                 if (c.getFilterValue() != null && !c.getFilterValue().equals("")) {
                     columnPredicates.add(
                             new PropertyConstraintPredicate(context,
                                     c.getValueExpression("filterBy"),
                                     c.getFilterValue(),
                                     c.getFilterConstraint()));
                 }
                 //TODO: Add global filter constraint configurability
                 if (hasGlobalFilter)
                     globalPredicates.add(new PropertyConstraintPredicate(
                             context,
                             c.getValueExpression("filterBy"),
                             globalFilter,
                             new ContainsFilterConstraint()));
             }
 
             if (globalPredicates.size() + columnPredicates.size() == 0)
                 return null;
 
             List filteredData = new ArrayList();
             setFilteredData(null);
             setModel(null);
 
             DataModel model = getDataModel();
             TreeDataModel treeModel = hasTreeDataModel() ? (TreeDataModel)model : null;
             String rowVar = getVar();
             String rowStateVar = getRowStateVar();
 
             // If the global predicate is set, require one column must meet the criteria of the global predicate
             if (globalPredicates.size() > 0) columnPredicates.add(AnyPredicate.getInstance(globalPredicates));
             Predicate filterSet = AllPredicate.getInstance(columnPredicates);
             int index = 0;
 
             // UIData Iteration
             model.setRowIndex(index);
             while (model.isRowAvailable()) {
 
                 Object rowData = model.getRowData();
                 RowState rowState = getStateMap().get(rowData);
 
                 if (rowVar != null) context.getExternalContext().getRequestMap().put(rowVar, rowData);
                 context.getExternalContext().getRequestMap().put(rowStateVar, rowState);
 
                 if  (filterSet.evaluate(rowData)) {
                     if (treeModel != null) filteredData.add(treeModel.getRowEntry());
                     else filteredData.add(model.getRowData());
                 }
                 index++;
                 model.setRowIndex(index);
             }
             // Iteration clean up
             setRowIndex(-1);
             if (rowVar != null) context.getExternalContext().getRequestMap().remove(rowVar);
             context.getExternalContext().getRequestMap().remove(getRowStateVar());
             return  filteredData;
         } finally {
             setFilterValueChanged(false);
         }
     }
 
     protected void loadLazyData() {
         LazyDataModel model = (LazyDataModel) getDataModel();
         model.setPageSize(getRows());
         model.setWrappedData(model.load(getFirst(), getRows(), getSortCriteria(), getFilters()));
     }
 
 
 
 
     /*#######################################################################*/
     /*###################### Private Methods ################################*/
     /*#######################################################################*/
     private boolean isIdPrefixedParamSet(String param, FacesContext x) {
         return x.getExternalContext().getRequestParameterMap().containsKey(this.getClientId(x) + param);
     }
 
     private List<Integer> generateColumnOrdering() {
         List<Integer> superOrder = super.getColumnOrdering();
         if (superOrder == null || superOrder.size() == 0) {
             ArrayList<Column> columns = new ArrayList<Column>();
             Stack childStack = new Stack<UIComponent>();
             childStack.add(this);
             while (!childStack.empty()) {
                 for (UIComponent child : ((UIComponent)childStack.pop()).getChildren()) {
                     if (!(child instanceof ColumnGroup) && !(child instanceof Column)) {
                         if (child.getChildren().size() > 0) childStack.add(child);
                     } else if (child instanceof Column) columns.add((Column) child);
                 }
             }
             ArrayList<Integer> ordering = new ArrayList<Integer>();
             int i=0;
             for (Object o : columns) ordering.add(i++);
             setColumnOrdering(ordering);
             return ordering;
         } else return superOrder;
     }
 
 
 
 
 
     /*#######################################################################*/
     /*###################### VisitTree Impl #################################*/
     /*#######################################################################*/
     @Override
     public boolean visitTree(VisitContext context, VisitCallback callback) {
         boolean ret = false;
         if (this.isVisitable(context)) {
             boolean visitRows = requiresRowIteration(context);
 
             int savedIndex = -1;
             if (visitRows) {
                 savedIndex = getRowIndex();
                 setRowIndex(-1);
             }
 
             this.pushComponentToEL(FacesContext.getCurrentInstance(), this);
             try {
                 VisitResult result = context.invokeVisitCallback(this, callback);
                 if (result.equals(VisitResult.COMPLETE)) return true;
                 if (doVisitChildren(context, visitRows) & result == VisitResult.ACCEPT) {
                     if (visitFacets(context, callback, visitRows)) return true;
                     if (visitColumnsAndColumnFacets(context, callback, visitRows)) return true;
                     if (visitRowsAndExpandedRows(context, callback, visitRows)) return true;
                 }
             } finally {
                 this.popComponentFromEL(FacesContext.getCurrentInstance());
                 //TODO: Fix this duplication
                 this.setRowIndex(savedIndex);
                 if (visitRows) setRowIndex(savedIndex);
             }
         }
         return ret;
     }
 
     private boolean requiresRowIteration(VisitContext ctx) {
         try { // Use JSF 2.1 hints if available
             return !ctx.getHints().contains(VisitHint.SKIP_ITERATION);
         } catch (NoSuchFieldError e) {
             FacesContext fctx = FacesContext.getCurrentInstance();
             return (!PhaseId.RESTORE_VIEW.equals(fctx.getCurrentPhaseId()));
         }
     }
 
     private boolean visitFacets(VisitContext context, VisitCallback callback, boolean visitRows) {
         if (visitRows) setRowIndex(-1);
         if (getFacetCount() > 0) {
             for (UIComponent facet : getFacets().values())
                 if (facet.visitTree(context, callback)) return true;
         }
         return false;
     }
 
     private boolean visitColumnsAndColumnFacets(VisitContext context, VisitCallback callback, boolean visitRows) {
         if (visitRows) setRowIndex(-1);
         if (getChildCount() > 0) {
             for (UIComponent column : getChildren()) {
                 if (column instanceof Column || column instanceof RowPanelExpander) {
                     VisitResult result = context.invokeVisitCallback(column, callback); // visit the column directly
                     if (result == VisitResult.COMPLETE) return true;
                     if (column.getFacetCount() > 0) {
                         for (UIComponent columnFacet : column.getFacets().values()) {
                             if (columnFacet.visitTree(context, callback)) {
                                 return true;
                             }
                         }
                     }
                 } else if (column instanceof ColumnGroup) {
                     UIComponent columnGroup = column;
                     for (UIComponent row : columnGroup.getChildren()) {
                         for (UIComponent c : row.getChildren()) {
                             if (c instanceof Column) {
                                 VisitResult result = context.invokeVisitCallback(c, callback); // visit the column directly
                                 if (result == VisitResult.COMPLETE) return true;
                                 if (c.getFacetCount() > 0) {
                                     for (UIComponent columnFacet : c.getFacets().values()) {
                                         if (columnFacet.visitTree(context, callback)) {
                                             return true;
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
         return false;
     }
 
     private boolean visitRowsAndExpandedRows(VisitContext context, VisitCallback callback, boolean visitRows) {
         int rows = 0;
         int offset = 0;
         int first = getFirst();

        // The cached data model that is used in myFaces may
        // have been generated from incorrect getValue() results (I assume)
        // causing it to mistakenly contain 0 rows.
        // This is a fix until a more general one is available.
        if (getRowCount() == 0) setDataModel(null);

        // Get / Regenerate cached data model.
        Object model = getDataModel();
         RowPanelExpander panelExpander = getPanelExpansion();
         RowExpander rowExpander = getRowExpansion();
         boolean hasPanelExpansion = (panelExpander != null);
         boolean hasRowExpansion = (rowExpander != null);
 
         RowStateMap stateMap = null;
 
         if (visitRows) {
             stateMap = this.getStateMap();
             rows = getRows();
             // If a indeterminate number of rows are shown, visit all rows.
             if (rows == 0) rows = getRowCount();
         }
 
         while (true) {
             if (visitRows) {
                 if (offset >= rows) break;
                 this.setRowIndex(first + offset);
             }
 
             if (!visitRows || isRowAvailable()) {
                 RowState rowState = null;
                 if (visitRows) rowState = stateMap.get(getRowData());
 
                 // Check for tree case
                 if (hasTreeDataModel()) {
                     String currentRootId = "";
                     TreeDataModel dataModel = ((TreeDataModel)this.getDataModel());
                     // Handle row and loop down the tree if expanded.
                     try {
                         do {
                             if (log.isLoggable(Level.FINEST)) log.finest("Visiting Row Id: " + dataModel.getRowIndex());
 
                             // Visit row in tree case.
                             if (getChildCount() > 0) {
                                 for (UIComponent kid : getChildren()) {
                                     if (!(kid instanceof UIColumn) && !(kid instanceof RowPanelExpander)) {
                                         continue;
                                     }
                                     if (kid.getChildCount() > 0) {
                                         for (UIComponent grandkid : kid.getChildren()) {
                                             if (grandkid.visitTree(context, callback)) {
                                                 return true;
                                             }
                                         }
                                     }
                                 }
                             }
 
                             // Handle recursive case
                             // If this row is expanded and has children, set it as the root & keep looping.
                             if (rowState != null && rowState.isExpanded() && dataModel.getCurrentRowChildCount() > 0) {
                                 currentRootId =  currentRootId.equals("") ? (this.getRowIndex()+"") : (currentRootId + "." + getRowIndex());
                                 dataModel.setRootIndex(currentRootId);
                                 this.setRowIndex(0);
                             } else if (dataModel.getRowIndex() < dataModel.getRowCount()-1) {
                                 this.setRowIndex(dataModel.getRowIndex() + 1);
                             } else if (!currentRootId.equals("")) {
                                 // changing currrent node id to reflect pop
                                 this.setRowIndex(dataModel.pop() + 1);
                                 currentRootId = (currentRootId.lastIndexOf('.') != -1)  ? currentRootId.substring(0,currentRootId.lastIndexOf('.')) : "";
                                 if (log.isLoggable(Level.FINEST)) log.finest("Popping Root: " + currentRootId);
                             }
                             // Break out of expansion recursion to continue root node
                             if (currentRootId.equals("")) break;
                         } while (true);
                     } finally { dataModel.setRootIndex(null); }
                 } else {
                     // Visit row in plain model case.
                     if (getChildCount() > 0) {
                         for (UIComponent kid : getChildren()) {
                             if (!(kid instanceof UIColumn) && !(kid instanceof RowPanelExpander)) {
                                 continue;
                             }
                             if (kid.getChildCount() > 0) {
                                 for (UIComponent grandkid : kid.getChildren()) {
                                     if (grandkid.visitTree(context, callback)) {
                                         return true;
                                     }
                                 }
                             }
                         }
                     }
                 }
             } else return false;
 
             if (!visitRows) break;
             offset++;
         }
         return false;
     }
 
     private boolean doVisitChildren(VisitContext context, boolean visitRows) {
         if (visitRows) setRowIndex(-1);
         Collection<String> idsToVisit = context.getSubtreeIdsToVisit(this);
         assert(idsToVisit != null);
         // non-empty collection means we need to visit our children.
         return (!idsToVisit.isEmpty());
     }
 
 
 
     /*#######################################################################*/
     /*###################### ClientId Impl ##################################*/
     /*#######################################################################*/
     private String baseClientId = null;
     private int baseClientIdLength;
     private StringBuilder clientIdBuilder = null;
     private Boolean isNested = null;
     public String getBaseClientId(FacesContext context) {
         if (baseClientId == null && clientIdBuilder == null) {
             if (!isNestedWithinUIData()) {
                 clientIdBuilder = new StringBuilder(UIComponentBase_getClientId(context));
                 baseClientId = clientIdBuilder.toString();
                 baseClientIdLength = (baseClientId.length() + 1);
                 clientIdBuilder.append(UINamingContainer.getSeparatorChar(context));
                 clientIdBuilder.setLength(baseClientIdLength);
             } else {
                 clientIdBuilder = new StringBuilder();
             }
         }
         return baseClientId;
     }
 
     @Override
     public String getContainerClientId(FacesContext context) {
         if (context == null) {
             throw new NullPointerException();
         }
         return this.getClientId(context);
     }
 
     @Override
     public String getClientId(FacesContext context) {
 
         if (context == null) {
             throw new NullPointerException();
         }
 
         // If baseClientId and clientIdBuilder are both null, this is the
         // first time that getClientId() has been called.
         // If we're not nested within another UIData, then:
         //   - create a new StringBuilder assigned to clientIdBuilder containing
         //   our client ID.
         //   - toString() the builder - this result will be our baseClientId
         //     for the duration of the component
         //   - append UINamingContainer.getSeparatorChar() to the builder
         //  If we are nested within another UIData, then:
         //   - create an empty StringBuilder that will be used to build
         //     this instance's ID
         if (baseClientId == null && clientIdBuilder == null) {
             if (!isNestedWithinUIData()) {
                 clientIdBuilder = new StringBuilder(UIComponentBase_getClientId(context));
                 baseClientId = clientIdBuilder.toString();
                 baseClientIdLength = (baseClientId.length() + 1);
                 clientIdBuilder.append(UINamingContainer.getSeparatorChar(context));
                 clientIdBuilder.setLength(baseClientIdLength);
             } else {
                 clientIdBuilder = new StringBuilder();
             }
         }
         int rowIndex = getRowIndex();
         if (rowIndex >= 0) {
             String cid;
             if (!isNestedWithinUIData()) {
                 // we're not nested, so the clientIdBuilder is already
                 // primed with clientID +
                 // UINamingContainer.getSeparatorChar().  Append the
                 // current rowIndex, and toString() the builder.  reset
                 // the builder to it's primed state.
                 if (hasTreeDataModel()) {
                     String rootIndex = ((TreeDataModel)getDataModel()).getRootIndex();
                     if (rootIndex != null && !rootIndex.equals(""))
                         rootIndex += "."+rowIndex;
                     else rootIndex = ""+rowIndex;
                     cid = clientIdBuilder.append(rootIndex).toString();
                 } else
                     cid = clientIdBuilder.append(rowIndex).toString();
 
                 clientIdBuilder.setLength(baseClientIdLength);
             } else {
                 // we're nested, so we have to build the ID from scratch
                 // each time.  Reuse the same clientIdBuilder instance
                 // for each call by resetting the length to 0 after
                 // the ID has been computed.
                 if (hasTreeDataModel()) {
                     String rootIndex = ((TreeDataModel)getDataModel()).getRootIndex();
                     if (rootIndex != null && !rootIndex.equals(""))
                         rootIndex += "."+rowIndex;
                     else rootIndex = ""+rowIndex;
                     cid = clientIdBuilder.append(UIComponentBase_getClientId(context))
                             .append(UINamingContainer.getSeparatorChar(context)).append(rootIndex)
                             .toString();
                 } else
                     cid = clientIdBuilder.append(UIData_getContainerClientId(context)).toString();
 
                 clientIdBuilder.setLength(0);
                 }
             return (cid);
         } else {
             if (!isNestedWithinUIData()) {
                 // Not nested and no row available, so just return our baseClientId
                 return (baseClientId);
             } else {
                 // nested and no row available, return the result of getClientId().
                 // this is necessary as the client ID will reflect the row that
                 // this table represents
                 return UIData_getContainerClientId(context);
             }
         }
     }
 
     /**
      * @return Concatenation of NamingContainer ids
      */
     protected String UIComponentBase_getClientId(FacesContext context) {
         if (context == null) {
             throw new NullPointerException();
         }
 
         //boolean idWasNull = false;
         String id = getId();
         if (id == null) {
             // Although this is an error prone side effect, we automatically create a new id
             // just to be compatible to the RI
 
             // The documentation of UniqueIdVendor says that this interface should be implemented by
             // components that also implements NamingContainer. The only component that does not implement
             // NamingContainer but UniqueIdVendor is UIViewRoot. Anyway we just can't be 100% sure about this
             // fact, so it is better to scan for the closest UniqueIdVendor. If it is not found use
             // viewRoot.createUniqueId, otherwise use UniqueIdVendor.createUniqueId(context,seed).
             UniqueIdVendor parentUniqueIdVendor = findParentUniqueIdVendor(this);
             if (parentUniqueIdVendor == null) {
                 UIViewRoot viewRoot = context.getViewRoot();
                 if (viewRoot != null) {
                     id = viewRoot.createUniqueId();
                 }
                 else {
                     // The RI throws a NPE
                     String location = getComponentLocation(this);
                     throw new FacesException("Cannot create clientId. No id is assigned for component"
                             + " to create an id and UIViewRoot is not defined: "
                             + getPathToComponent(this)
                             + (location != null ? " created from: " + location : ""));
                 }
             }
             else {
                 id = parentUniqueIdVendor.createUniqueId(context, null);
             }
             setId(id);
             // We remember that the id was null and log a warning down below
             // idWasNull = true;
         }
         String clientId;
         UIComponent namingContainer = findParentNamingContainer(this, false);
         if (namingContainer != null) {
             String containerClientId = namingContainer.getContainerClientId(context);
             if (containerClientId != null) {
                 StringBuilder bld = new StringBuilder(containerClientId.length()+1+id.length());
                 clientId = bld.append(containerClientId).append(UINamingContainer.getSeparatorChar(context)).append(id).toString();
             }
             else {
                 clientId = id;
             }
         }
         else {
             clientId = id;
         }
         Renderer renderer = getRenderer(context);
         if (renderer != null) {
             clientId = renderer.convertClientId(context, clientId);
         }
 
         return clientId;
     }
 
     protected String UIData_getContainerClientId(FacesContext facesContext) {
         String clientId = UIComponentBase_getClientId(facesContext);
 
         int rowIndex = getRowIndex();
         if (rowIndex == -1) {
             return clientId;
         }
 
         StringBuilder bld = new StringBuilder(clientId.length()+4);
         String ret = bld.append(clientId).append(UINamingContainer.getSeparatorChar(facesContext)).append(rowIndex).toString();
         return ret;
     }
 
     /**
      * Logic for this method is borrowed from MyFaces
      *
      * @param component
      * @return
      */
     private String getComponentLocation(UIComponent component) {
         Location location = (Location) component.getAttributes().get(UIComponent.VIEW_LOCATION_KEY);
         if (location != null) {
             return location.toString();
         }
         return null;
     }
 
     private String getPathToComponent(UIComponent component) {
         StringBuffer buf = new StringBuffer();
 
         if (component == null) {
             buf.append("{Component-Path : ");
             buf.append("[null]}");
             return buf.toString();
         }
 
         getPathToComponent(component, buf);
 
         buf.insert(0, "{Component-Path : ");
         buf.append("}");
 
         return buf.toString();
     }
 
     private void getPathToComponent(UIComponent component, StringBuffer buf) {
         if (component == null) {
             return;
         }
 
         StringBuffer intBuf = new StringBuffer();
 
         intBuf.append("[Class: ");
         intBuf.append(component.getClass().getName());
         if (component instanceof UIViewRoot) {
             intBuf.append(",ViewId: ");
             intBuf.append(((UIViewRoot) component).getViewId());
         }
         else {
             intBuf.append(",Id: ");
             intBuf.append(component.getId());
         }
         intBuf.append("]");
 
         buf.insert(0, intBuf.toString());
 
         getPathToComponent(component.getParent(), buf);
     }
 
     static UniqueIdVendor findParentUniqueIdVendor(UIComponent component) {
         UIComponent parent = component.getParent();
 
         while (parent != null) {
             if (parent instanceof UniqueIdVendor) {
                 return (UniqueIdVendor) parent;
             }
             parent = parent.getParent();
         }
         return null;
     }
 
     static UIComponent findParentNamingContainer(UIComponent component, boolean returnRootIfNotFound) {
         UIComponent parent = component.getParent();
         if (returnRootIfNotFound && parent == null) {
             return component;
         }
         while (parent != null) {
             if (parent instanceof NamingContainer) {
                 return parent;
             }
             if (returnRootIfNotFound) {
                 UIComponent nextParent = parent.getParent();
                 if (nextParent == null) {
                     return parent; // Root
                 }
                 parent = nextParent;
             }
             else {
                 parent = parent.getParent();
             }
         }
         return null;
     }
 
     private Boolean isNestedWithinUIData() {
         if (isNested == null) {
             UIComponent parent = this;
             while (null != (parent = parent.getParent())) {
                 if (parent instanceof UIData || "facelets.ui.Repeat".equals(parent.getRendererType())) {
                     isNested = Boolean.TRUE;
                     break;
                 }
             }
             if (isNested == null) {
                 isNested = Boolean.FALSE;
             }
             return isNested;
         } else return isNested;
     }
 
 
 
     /*#######################################################################*/
     /*###################### Private Classes ################################*/
     /*#######################################################################*/
     private class EntryKeyComparatorWrapper<T> implements Comparator {
         Comparator<T> comparator;
 
         public EntryKeyComparatorWrapper(Comparator<T> comparator) {
             this.comparator = comparator;
         }
 
         public int compare(Object o1, Object o2) {
             return comparator.compare(((Map.Entry<T, Object>) o1).getKey(), ((Map.Entry<T, Object>) o2).getKey());
         }
     }
 
     private class PriorityComparator implements Comparator<Column> {
         public int compare(Column object, Column object1) {
             return object.getSortPriority().compareTo(object1.getSortPriority());
         }
     }
 
 
 
 
 
 
     /*#######################################################################*/
     /*#################### UIData iterate() impl. ###########################*/
     /*#######################################################################*/
     private void iterate(FacesContext context, PhaseId phaseId) {
         // Process each facet of this component exactly once
         setRowIndex(-1);
         if (getFacetCount() > 0) {
             for (UIComponent facet : getFacets().values()) {
                 if (phaseId == PhaseId.APPLY_REQUEST_VALUES) {
                     facet.processDecodes(context);
                 } else if (phaseId == PhaseId.PROCESS_VALIDATIONS) {
                     facet.processValidators(context);
                 } else if (phaseId == PhaseId.UPDATE_MODEL_VALUES) {
                     facet.processUpdates(context);
                 } else {
                     throw new IllegalArgumentException();
                 }
             }
         }
 
         // Process each facet of our child UIColumn components exactly once
         setRowIndex(-1);
         if (getChildCount() > 0) {
             for (UIComponent column : getChildren()) {
                 if (!(column instanceof UIColumn) || !column.isRendered()) {
                     continue;
                 }
                 if (column.getFacetCount() > 0) {
                     for (UIComponent columnFacet : column.getFacets().values()) {
                         if (phaseId == PhaseId.APPLY_REQUEST_VALUES) {
                             columnFacet.processDecodes(context);
                         } else if (phaseId == PhaseId.PROCESS_VALIDATIONS) {
                             columnFacet.processValidators(context);
                         } else if (phaseId == PhaseId.UPDATE_MODEL_VALUES) {
                             columnFacet.processUpdates(context);
                         } else {
                             throw new IllegalArgumentException();
                         }
                     }
                 }
             }
         }
 
         // Visit tableConfigPanel if one is our child
         setRowIndex(-1);
         for (UIComponent kid : getChildren()) {
             if (!(kid instanceof TableConfigPanel) || !kid.isRendered()) {
                 continue;
             }
 
             if (phaseId == PhaseId.APPLY_REQUEST_VALUES) {
                 kid.processDecodes(context);
             } else if (phaseId == PhaseId.PROCESS_VALIDATIONS) {
                 kid.processValidators(context);
             } else if (phaseId == PhaseId.UPDATE_MODEL_VALUES) {
                 kid.processUpdates(context);
             } else {
                 throw new IllegalArgumentException();
             }
         }
 
         // Iterate over our UIColumn & PanelExpansion children, once per row
         int processed = 0;
         int rowIndex = getFirst() - 1;
         int rows = getRows();
         boolean inSubrows = false;
         RowPanelExpander panelExpander = getPanelExpansion();
         RowStateMap map = getStateMap();
         RowState rowState;
         Boolean expanded;
         TreeDataModel treeDataModel;
 
         // Without this call, at times iteration will use an unfiltered data model
         getDataModel();
 
         while (true) {
             // Have we processed the requested number of rows?
             if (!inSubrows) processed = processed + 1;
             if ((rows > 0) && (processed > rows)) {
                 break;
             }
 
             // Expose the current row in the specified request attribute
             setRowIndex(++rowIndex);
             if (!isRowAvailable()) {
                 if (model instanceof TreeDataModel) {
                     treeDataModel = (TreeDataModel)model;
                     if (treeDataModel.isRootIndexSet()) {
                         rowIndex = treeDataModel.pop()+1;
                         setRowIndex(rowIndex);
                         if (!treeDataModel.isRootIndexSet()) inSubrows = false;
                     } else break;
                 } else break; // Scrolled past the last row
             }
 
             rowState = map.get(getRowData());
             expanded = rowState.isExpanded();
 
             // Perform phase-specific processing as required
             // on the *children* of the UIColumn (facets have
             // been done a single time with rowIndex=-1 already)
             if (getChildCount() > 0) {
                 for (UIComponent kid : getChildren()) {
                     if ((!(kid instanceof UIColumn) && !(kid instanceof RowPanelExpander))
                             || !kid.isRendered()) {
                         continue;
                     }
                     if ((kid instanceof RowPanelExpander) && !expanded) {
                         continue;
                     }
                     if (kid.getChildCount() > 0) {
                         for (UIComponent grandkid : kid.getChildren()) {
                             if (!grandkid.isRendered()) {
                                 continue;
                             }
                             if (phaseId == PhaseId.APPLY_REQUEST_VALUES) {
                                 grandkid.processDecodes(context);
                             } else if (phaseId == PhaseId.PROCESS_VALIDATIONS) {
                                 grandkid.processValidators(context);
                             } else if (phaseId == PhaseId.UPDATE_MODEL_VALUES) {
                                 grandkid.processUpdates(context);
                             } else {
                                 throw new IllegalArgumentException();
                             }
                         }
                     }
                 }
             }
 
             if (expanded && hasTreeDataModel() && (panelExpander == null || rowState.getExpansionType() == RowState.ExpansionType.ROW)) {
                 treeDataModel = (TreeDataModel)model;
                 if (treeDataModel.getCurrentRowChildCount() > 0) {
                     inSubrows = true;
                     treeDataModel.setRootIndex(
                             treeDataModel.getRootIndex().equals("") ?
                                     ""+getRowIndex() :
                                     treeDataModel.getRootIndex() + "." + getRowIndex());
                     rowIndex = -1;
                 }
             }
         }
 
         // Clean up after ourselves
         setRowIndex(-1);
     }
 }
