 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package com.icesoft.faces.component.ext;
 
 import com.icesoft.faces.component.CSS_DEFAULT;
 import com.icesoft.faces.component.ext.taglib.Util;
 import com.icesoft.faces.component.panelseries.UISeries;
 import com.icesoft.faces.context.effects.Effect;
 import com.icesoft.faces.context.effects.EffectQueue;
 import com.icesoft.faces.context.effects.Highlight;
 import com.icesoft.faces.context.effects.JavascriptContext;
 
 import javax.el.ELContext;
 import javax.el.ELResolver;
 import javax.faces.component.UIColumn;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIData;
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import javax.faces.event.PhaseId;
 
 import java.io.IOException;
 import java.lang.Object;
 import java.lang.String;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  * This is an extension of javax.faces.component.html.HtmlDataTable, which
  * provides some additional behavior to this component such as: <ul> <li>changes
  * the component's rendered state based on the authentication</li> <li>maintain
  * the sorting order, for a column within the dataTable</li> <ul>
  */
 public class HtmlDataTable
         extends UISeries {
 
     public static final String COMPONENT_TYPE =
             "com.icesoft.faces.HtmlDataTable";
     public static final String RENDERER_TYPE = "com.icesoft.faces.Table";
     private String renderedOnUserRole = null;
     private String sortColumn = null;
     private Boolean sortAscending = null;
     private Boolean resizable = null;
     private static final boolean DEFAULT_SORTASCENDING = true;
     
     private Boolean scrollable = null;
     private String columnWidths = null;
     private String scrollHeight = null;
     private String headerClasses = null;
     private Boolean clientOnly = null;
     private Boolean scrollFooter = null;
 
     public HtmlDataTable() {
         super();
         setRendererType(RENDERER_TYPE);
     }
 
     /**
      * <p>Set the value of the <code>renderedOnUserRole</code> property.</p>
      */
     public void setRenderedOnUserRole(String renderedOnUserRole) {
         this.renderedOnUserRole = renderedOnUserRole;
     }
 
     /**
      * <p>Return the value of the <code>renderedOnUserRole</code> property.</p>
      */
     public String getRenderedOnUserRole() {
         if (renderedOnUserRole != null) {
             return renderedOnUserRole;
         }
         ValueBinding vb = getValueBinding("renderedOnUserRole");
         return vb != null ? (String) vb.getValue(getFacesContext()) : null;
     }
 
     /**
      * <p>Return the value of the <code>rendered</code> property.</p>
      */
     public boolean isRendered() {
         if (!Util.isRenderedOnUserRole(this)) {
             return false;
         }
         return super.isRendered();
     }
 
     public void encodeBegin(FacesContext context) throws IOException {
         super.encodeBegin(context);
     }
     
     /**
      * <p>Return the value of the <code>sortColumn</code> property.</p>
      */
     public String getSortColumn() {
         if (sortColumn != null) {
             return sortColumn;
         }
         ValueBinding vb = getValueBinding("sortColumn");
         return vb != null ? (String) vb.getValue(getFacesContext()) : null;
     }
 
     /**
      * <p>Set the value of the <code>sortAscending</code> property.</p>
      */
     public void setSortAscending(boolean sortAscending) {
         this.sortAscending = new Boolean(sortAscending);
         ValueBinding vb = getValueBinding("sortAscending");
         if (vb != null) {
             vb.setValue(getFacesContext(), this.sortAscending);
             this.sortAscending = null;
         }
     }
 
     /**
      * <p>Set the value of the <code>sortColumn</code> property.</p>
      */
     public void setSortColumn(String sortColumn) {
         this.sortColumn = sortColumn;
         ValueBinding vb = getValueBinding("sortColumn");
         if (vb != null) {
             vb.setValue(getFacesContext(), this.sortColumn);
             this.sortColumn = null;
         }
     }
 
     /**
      * <p>Return the value of the <code>sortAscending</code> property.</p>
      */
     public boolean isSortAscending() {
         if (sortAscending != null) {
             return sortAscending.booleanValue();
         }
         ValueBinding vb = getValueBinding("sortAscending");
         Boolean v =
                 vb != null ? (Boolean) vb.getValue(getFacesContext()) : null;
         return v != null ? v.booleanValue() : DEFAULT_SORTASCENDING;
     }
 
     /**
      * <p>Gets the state of the instance as a <code>Serializable</code>
      * Object.</p>
      */
     public Object saveState(FacesContext context) {
         Object values[] = new Object[15];
         values[0] = super.saveState(context);
         values[1] = renderedOnUserRole;
         values[2] = columnWidths;
         values[3] = headerClasses;
         values[4] = sortColumn;
         values[5] = sortAscending;
         values[6] = scrollHeight;
         values[7] = scrollFooter;
         values[8] = clientOnly;
         values[9] = headerClassesArray;
         values[10] = resizable;
         values[11] = resizableTblColumnsWidth;
         values[12] = new Integer(resizableTblColumnsWidthIndex);
         values[13] = scrollable;
         values[14] = Boolean.valueOf(isResizableColumnWidthsSet);
         return ((Object) (values));
     }
 
     /**
      * <p>Perform any processing required to restore the state from the entries
      * in the state Object.</p>
      */
     public void restoreState(FacesContext context, Object state) {
         Object values[] = (Object[]) state;
         super.restoreState(context, values[0]);
         renderedOnUserRole = (String) values[1];
         columnWidths = (String)values[2];
         headerClasses = (String)values[3];
         sortColumn = (String)values[4];
         sortAscending = (Boolean)values[5];
         scrollHeight = (String)values[6];
         scrollFooter = (Boolean)values[7];
         clientOnly = (Boolean)values[8];
         headerClassesArray = (String[]) values[9];
         resizable = (Boolean) values[10];
         resizableTblColumnsWidth = (String[]) values[11];
         resizableTblColumnsWidthIndex = ((Integer) values[12]).intValue();
         scrollable = (Boolean) values[13];
         isResizableColumnWidthsSet = ((Boolean) values[14]).booleanValue();
     }
 
     public String getComponentType() {
         return COMPONENT_TYPE;
     }
 
     protected void iterate(FacesContext facesContext, PhaseId phase) {
         // clear row index
         setRowIndex(-1);
         // process component facets once
         Iterator facets = getFacets().keySet().iterator();
         while (facets.hasNext()) {
             UIComponent facet = (UIComponent) getFacet(facets.next().toString());
             processKids(facesContext, phase, facet);
         }
         // reset row index
         setRowIndex(-1);
         // process each child column and it's facets once
         if (getChildCount() > 0 ) {
             Iterator columns = getChildren().iterator();
             while (columns.hasNext()) {
                 UIComponent column = (UIComponent) columns.next();
                 if (!(column instanceof UIColumn) &&
                     !(column instanceof UIColumns)) {
                     continue;
                 }
                 if (!column.isRendered()) {
                     continue;
                 }
                 if (column instanceof UIColumn) {
                     Iterator columnFacets = column.getFacets().keySet().iterator();
                     while (columnFacets.hasNext()) {
                         UIComponent columnFacet = (UIComponent) column.getFacets()
                                 .get(columnFacets.next());
                         processKids(facesContext, phase, columnFacet);
                     }
     
                 } else if (column instanceof UIColumns) {
                     processKids(facesContext, phase, column);
                 }
             }
         }
 
         // clear rowIndex
         setRowIndex(-1);
 
         int rowsProcessed = 0;
         int currentRowIndex = getFirst() - 1;
         int displayedRows = getRows();
         // loop over dataModel processing each row once
         while (1 == 1) {
             // break if we have processed the number of rows requested
             if ((++currentRowIndex >= getRowCount()) || 
                     ((displayedRows > 0) && (++rowsProcessed > displayedRows))) {
                 break;
             }
             // process the row at currentRowIndex
             setRowIndex(currentRowIndex);
             // break if we've moved past the last row
             if (!isRowAvailable()) {
                 break;
             }
             // loop over children
             if (getChildCount() > 0) {
                 Iterator children = getChildren().iterator();
                 while (children.hasNext()) {
                     UIComponent child = (UIComponent) children.next();
                     if (!(child instanceof UIColumn) &&
                         !(child instanceof UIColumns)) {
                         continue;
                     }
                     if (child instanceof UIColumn) {
                         if (child.getChildCount() > 0) {
                             Iterator granchildren = child.getChildren().iterator();
                             while (granchildren.hasNext()) {
                                 UIComponent granchild =
                                         (UIComponent) granchildren.next();
                                 if (!granchild.isRendered()) {
                                     continue;
                                 }
                                 processKids(facesContext, phase, granchild);
                             }
                         }
                     } else if (child instanceof UIColumns) {
                         processKids(facesContext, phase, child);
                     }
                 }
             }
         }
 
         // clear rowIndex
         setRowIndex(-1);
     }
 
     protected void restoreChildrenState(FacesContext facesContext) {
         if (getChildCount() > 0) {
             Iterator kids = getChildren().iterator();
             while (kids.hasNext()) {
                 UIComponent kid = (UIComponent) kids.next();
                 if (kid instanceof UIColumn) {
                     restoreChildState(facesContext, kid);
                 }
                 else {
                     resetChildClientId(facesContext, kid); 
                 }
             }
         }
     }
 
     protected void resetChildClientId(FacesContext facesContext,
                                      UIComponent component) {
         String id = component.getId();
         System.out.println("UISeriesBase.restoreChildState()  Doer: " + getClass().getSimpleName() + "  On: " + component.getClass().getSimpleName() + "  id: " + id);
         component.setId(id);
         Iterator children = component.getFacetsAndChildren();
         while (children.hasNext()) {
             resetChildClientId(facesContext, (UIComponent) children.next());
         }
     }
 
 
     /**
      * <p>Save state information for all descendant components, as described for
      * <code>setRowIndex()</code>.</p>
      */
     protected void saveChildrenState(FacesContext facesContext) {
         if (getChildCount() > 0) {        
             Iterator kids = getChildren().iterator();
             while (kids.hasNext()) {
                 UIComponent kid = (UIComponent) kids.next();
                 if (kid instanceof UIColumn) {
                     saveChildState(facesContext, kid);
                 }
             }
         }
     }
 
     public void processKids(FacesContext context, PhaseId phaseId,
                             UIComponent kid) {
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
 
     private transient int colNumber = 0;
 
     public int getColNumber() {
         return colNumber;
     }
 
     public void setColNumber(int colNumber) {
         this.colNumber = colNumber;
     }
 
     public Boolean getScrollable(){
         return isScrollable();
     }
 
     public Boolean isScrollable() {
         if (scrollable != null) {
             return scrollable;
         }
         ValueBinding vb = getValueBinding("scrollable");
         Boolean v =
                 vb != null ? (Boolean) vb.getValue(getFacesContext()) : null;
         return v != null ? v : Boolean.FALSE;
     }
 
     public void setScrollable(Boolean scrollable) {
         this.scrollable = scrollable;
     }
     
     public void setScrollable(boolean scrollable) {
         setScrollable(new Boolean (scrollable));
     }
 
     public String getColumnWidths() {
         if (columnWidths != null) {
             return columnWidths;
         }
         ValueBinding vb = getValueBinding("columnWidths");
         return vb != null ? (String) vb.getValue(getFacesContext()) : null;
     }
 
     public void setColumnWidths(String columnWidths) {
         this.columnWidths = columnWidths;
 
     }
 
     public String getScrollHeight() {
         if (scrollHeight != null) {
             return scrollHeight;
         }
         ValueBinding vb = getValueBinding("scrollHeight");
         return vb != null ? (String) vb.getValue(getFacesContext()) : null;
     }
 
     public void setScrollHeight(String scrollHeight) {
         this.scrollHeight = scrollHeight;
 
     }
 
     /**
      * <p>Set the value of the <code>headerClasses</code> property.</p>
      */
     public void setHeaderClasses(String headerClasses) {
         this.headerClasses = headerClasses;
     }
 
     /**
      * <p>Return the value of the <code>headerClasses</code> property.</p>
      */
     public String getHeaderClasses() {
         if (headerClasses != null) {
             return headerClasses;
         }
         ValueBinding vb = getValueBinding("headerClasses");
         return vb != null ? (String) vb.getValue(getFacesContext()) :null;
     }
 
     String[] headerClassesArray = null;
 
     public String getHeaderClassAtIndex(int index) {
         if (headerClassesArray == null) {
             headerClassesArray = getHeaderClasses().split(",");
         }
         if (headerClassesArray.length == 1) {
             return headerClassesArray[0];
         }
         try {
             return headerClassesArray[index];
         } catch (ArrayIndexOutOfBoundsException e) {
             return headerClassesArray[0];
         }
     }
 
 
     protected void restoreChild(FacesContext facesContext,
                                 UIComponent uiComponent) {
         super.restoreChild(facesContext, uiComponent);
         if (uiComponent instanceof UIData) {
             String clientId = uiComponent.getClientId(facesContext);
             Object value = savedChildren.get(clientId);
             ((UIData) uiComponent).setValue(value);
         }
     }
 
     protected void saveChild(FacesContext facesContext,
                              UIComponent uiComponent) {
         super.saveChild(facesContext, uiComponent);
         if (uiComponent instanceof UIData) {
             String clientId = uiComponent.getClientId(facesContext);
             savedChildren.put(clientId, ((UIData) uiComponent).getValue());
         }
     }
     
     public String getStyleClass() {
         return Util.getQualifiedStyleClass(this, 
                 super.getStyleClass(),
                 CSS_DEFAULT.TABLE_STYLE_CLASS,
                 "styleClass");
     }
     
     public String getHeaderClass() {
         return Util.getQualifiedStyleClass(this, 
                 super.getHeaderClass(),
                 CSS_DEFAULT.TABLE_HEADER_CLASS,
                 "headerClass");
     }
 
     public String getFooterClass() {
         return Util.getQualifiedStyleClass(this, 
                 super.getFooterClass(),
                 CSS_DEFAULT.TABLE_FOOTER_CLASS,
                 "footerClass");
     }
     
     public boolean isResizable() {
         if (resizable != null) {
             return resizable.booleanValue();
         }
         ValueBinding vb = getValueBinding("resizable");
         return vb != null ?
                ((Boolean) vb.getValue(getFacesContext())).booleanValue() :
                false;
     }
 
     public void setResizable(boolean resizable) {
         this.resizable = new Boolean(resizable);
     }
     
     public boolean isClientOnly() {
         if (clientOnly != null) {
             return clientOnly.booleanValue();
         }
         ValueBinding vb = getValueBinding("clientOnly");
         Boolean boolVal = vb != null ? (Boolean) vb.getValue(getFacesContext())
                 : null;
         return boolVal != null ? boolVal.booleanValue() : true;
     }
 
     public void setClientOnly(boolean clientOnly) {
         this.clientOnly = Boolean.valueOf(clientOnly);
     }  
     
     /* (non-Javadoc)
      * @see javax.faces.component.UIComponent#decode(javax.faces.context.FacesContext)
      */
     public void decode(FacesContext context) {
         super.decode(context);
         //this code is handling the columns width of resizable table
         Map requestParameterMap =
             context.getExternalContext().getRequestParameterMap();
         if (requestParameterMap.containsKey("ice.event.captured")) {
             String clientOnlyId = getClientId(context) + "clientOnly";
             String clientOnlyIdInParam = String.valueOf(requestParameterMap.get("ice.event.captured"));
             if (clientOnlyId.equals(clientOnlyIdInParam)) {
                 String columnWidths = String.valueOf(requestParameterMap.get(clientOnlyId));
                 resizableTblColumnsWidth = columnWidths.split(",");
                 ValueBinding vb = getValueBinding("resizableColumnWidths");
                 if (vb != null) {
                     vb.setValue(context, columnWidths);
                 }
             }
         }
         //--
     }    
 
     private String resizableTblColumnsWidth[] = new String[0];
     private int resizableTblColumnsWidthIndex = 0;
     private boolean isResizableColumnWidthsSet = false;
     
     public String getNextResizableTblColumnWidth() {
         if (resizableTblColumnsWidthIndex < resizableTblColumnsWidth.length) {
             return resizableTblColumnsWidth[resizableTblColumnsWidthIndex++];
         }
         return null;
     }
     
     public void resetResizableTblColumnsWidthIndex() {
         resizableTblColumnsWidthIndex = 0;
     }
 
    public boolean isScrollFooter() {
         if (scrollFooter != null) {
             return scrollFooter.booleanValue();
         }
         ValueBinding vb = getValueBinding("scrollFooter");
         Boolean boolVal = vb != null ? (Boolean) vb.getValue(getFacesContext())
                 : null;
         return boolVal != null ? boolVal.booleanValue() : true;
     }
 
     public void setScrollFooter(boolean scrollFooter) {
         this.scrollFooter = Boolean.valueOf(scrollFooter);
     }
 
     public String getResizableColumnWidths() {
         if (isResizableColumnWidthsSet) {
             StringBuffer result = new StringBuffer();
             result.append(resizableTblColumnsWidth[0]);
             for (int i = 1; i < resizableTblColumnsWidth.length; i++) {
                 result.append(",");
                 result.append(resizableTblColumnsWidth[i]);
             }
             return result.toString();
         }
         ValueBinding vb = getValueBinding("resizableColumnWidths");
         if (vb == null) return null;
         String columnWidths = (String) vb.getValue(getFacesContext());
         if (columnWidths != null) {
             resizableTblColumnsWidth = columnWidths.split(",");
         }
         return columnWidths;
     }
 
     public void setResizableColumnWidths(String columnWidths) {
         resizableTblColumnsWidth = columnWidths.split(",");
         isResizableColumnWidthsSet = true;
     }
 
     public enum SearchType {
         STARTS_WITH, ENDS_WITH, EXACT, CONTAINS
     }
 
     /**
      * Find the index of a row object in the current DataModel.
      * @param query The string to be searched for in the row object fields.
      * @param fields The fields of the row object to search the String representations of.
      * @param startRow The index to begin searching, inclusive.
      * @param searchType A enumeration representing where to search for a match.
      * @param caseSensitive A boolean representing the case sensitive.
      * @return Index of the row found or -1
      */
     public int findRow(String query, String[] fields, int startRow, SearchType searchType, boolean caseSensitive) {
         int savedRowIndex = getRowIndex();
         ELContext elContext = FacesContext.getCurrentInstance().getELContext();
         ELResolver resolver = elContext.getELResolver();
 
         setRowIndex(startRow);
 
        if (!caseSensitive) query = query.toLowerCase();
 
         try {
             // Contains
             if (searchType.equals(SearchType.CONTAINS))
                 while (isRowAvailable()) {
                     for (String s : fields) {
                         Object rowField = resolver.getValue(elContext, getRowData(), s);
                         String rowString = rowField.toString();
                         if (!caseSensitive) rowString = rowString.toLowerCase();
                         if (rowString.contains(query))
                             return getRowIndex();
                     }
                     setRowIndex(getRowIndex()+1);
                 }
 
             // Ends with
             else if (searchType.equals(SearchType.ENDS_WITH))
                 while (isRowAvailable()) {
                     for (String s : fields) {
                         Object rowField = resolver.getValue(elContext, getRowData(), s);
                         String rowString = rowField.toString();
                         if (!caseSensitive) rowString = rowString.toLowerCase();
                         if (rowString.endsWith(query))
                             return getRowIndex();
                     }
                     setRowIndex(getRowIndex()+1);
                 }
 
             // Starts with
             else if (searchType.equals(SearchType.STARTS_WITH))
                 while (isRowAvailable()) {
                     for (String s : fields) {
                         Object rowField = resolver.getValue(elContext, getRowData(), s);
                         String rowString = rowField.toString();
                         if (!caseSensitive) rowString = rowString.toLowerCase();
                         if (rowString.startsWith(query))
                             return getRowIndex();
                     }
                     setRowIndex(getRowIndex()+1);
                 }
 
             // Exact
             else if (searchType.equals(SearchType.EXACT))
                 while (isRowAvailable()) {
                     for (String s : fields) {
                         Object rowField = resolver.getValue(elContext, getRowData(), s);
                         String rowString = rowField.toString();
                         if (!caseSensitive) rowString = rowString.toLowerCase();
                         if (rowString.equals(query))
                             return getRowIndex();
                     }
                     setRowIndex(getRowIndex()+1);
                 }
 
             // Falls through if not found, or searchType is invalid.
             return -1;
         } finally {
             setRowIndex(savedRowIndex);
         }
     }
 
     /**
      * Find the index of a row object in the current DataModel.
      * @param query The string to be searched for in the row object fields.
      * @param fields The fields of the row object to search the String representations of.
      * @param startRow The index to begin searching, inclusive.
      * @param searchType A enumeration representing where to search for a match.
      * @return Index of the row found or -1
      */
     public int findRow(String query, String[] fields, int startRow, SearchType searchType) {
         return findRow(query, fields, startRow, searchType, true);
     }
 
     /**
      * Find the index of a row object in the current DataModel.
      * @param query The string to be searched for in the row object fields.
      * @param fields The fields of the row object to search the String representations of.
      * @param startRow The index to begin searching, inclusive.
      * @return Index of the row found or -1
      */
     public int findRow(String query, String[] fields, int startRow) {
         return findRow(query, fields, startRow, SearchType.CONTAINS, true);
     }
 
     /**
      * Navigate the client to a row in the table indicating the target row with the given effect.
      * @param row Index of the row to be navigated to.
      * @param effect Effect object to trigger on the given row
      */
     public void navigateToRow(int row, Effect effect) {
         if (row >= getRowCount())
             throw new IndexOutOfBoundsException();
 
         FacesContext context = FacesContext.getCurrentInstance();
         String id = getClientId(context) + ":" + row;
 
         int rowsPerPage = getRows();
         if (rowsPerPage > 0) {
             int page = row / rowsPerPage;
             setFirst(page * rowsPerPage);
         }
 
         JavascriptContext.applicationFocus(context, id);
 
         if (effect != null)
             JavascriptContext.fireEffect(effect, id, context);
     }
 
     /**
      * Navigate the client to a row in the table indicating the target row with a default highlight effect.
      * @param row Index of the row to be navigated to.
      */
     public void navigateToRow(int row) {
         navigateToRow(row, new Highlight("#fda505"));
     }
 }
    
 
   
