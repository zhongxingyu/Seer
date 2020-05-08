 /*
  * Original Code Copyright Prime Technology.
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
  *
  * Code Modification 2: Improved Scrollable DataTable Column Sizing - ICE-7028
  * Contributors: Nils Lundquist
  */
 package org.icefaces.ace.component.datatable;
 
 import org.icefaces.ace.component.celleditor.CellEditor;
 import org.icefaces.ace.component.column.Column;
 import org.icefaces.ace.component.columngroup.ColumnGroup;
 import org.icefaces.ace.component.panelexpansion.PanelExpansion;
 import org.icefaces.ace.component.row.Row;
 import org.icefaces.ace.component.rowexpansion.RowExpansion;
 import org.icefaces.ace.component.tableconfigpanel.TableConfigPanel;
 import org.icefaces.ace.context.RequestContext;
 import org.icefaces.ace.event.SelectEvent;
 import org.icefaces.ace.event.UnselectEvent;
 import org.icefaces.ace.model.table.*;
 import org.icefaces.ace.renderkit.CoreRenderer;
 import org.icefaces.ace.util.ComponentUtils;
 import org.icefaces.ace.util.HTML;
 import org.icefaces.ace.util.JSONBuilder;
 import org.icefaces.render.MandatoryResourceComponent;
 
 import javax.el.ValueExpression;
 import javax.faces.FacesException;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UINamingContainer;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.event.PhaseId;
 import javax.faces.model.DataModel;
 import javax.faces.model.SelectItem;
 import java.io.IOException;
 import java.util.*;
 
 @MandatoryResourceComponent(tagName="dataTable", value="org.icefaces.ace.component.datatable.DataTable")
 public class DataTableRenderer extends CoreRenderer {
     @Override
 	public void decode(FacesContext context, UIComponent component) {
         DataTable table = (DataTable) component;
 
         // Deferred selection may occur on any request
         if (table.isSelectionEnabled())
             DataTableDecoder.decodeSelection(context, table);
 
         // Other features will occurs as individual requests
         if (table.isFilterRequest(context))
             DataTableDecoder.decodeFilters(context, table);
 
         else if (table.isSortRequest(context))
             DataTableDecoder.decodeSortRequest(context, table, null, null);
 
         else if (table.isPaginationRequest(context))
             DataTableDecoder.decodePageRequest(context, table);
 
         else if (table.isColumnReorderRequest(context))
             DataTableDecoder.decodeColumnReorderRequest(context, table);
 
         else if (table.isTableConfigurationRequest(context))
             DataTableDecoder.decodeTableConfigurationRequest(context, table);
 
         decodeBehaviors(context, component);
 	}
 
     @Override
     public void encodeChildren(FacesContext context, UIComponent component) throws IOException {}
 
     @Override
     public boolean getRendersChildren() { return true; }
 
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException{
         DataTable table = (DataTable) component;
 
         if (table.isPaginator())
             table.calculatePage();
 
         if (table.isSortOrderChanged())
             table.processSorting();
 
         if (table.isFilterValueChanged())
             table.setFilteredData(table.processFilters(context));
 
         // Force regeneration of data model pre-render
         table.setModel(null);
 
         if (table.isScrollingRequest(context))
             encodeLiveRows(context, table);
         else
             encodeEntierty(context, table);
     }
 
 	protected void encodeScript(FacesContext context, DataTable table) throws IOException{
         ResponseWriter writer = context.getResponseWriter();
 		String clientId = table.getClientId(context);
         String filterEvent = table.getFilterEvent();
         UIComponent form = ComponentUtils.findParentForm(context, table);        
         JSONBuilder json = new JSONBuilder();
 
         if (form == null) 
             throw new FacesException("DataTable : \"" + clientId + "\" must be inside a form element.");
 
         boolean paging = table.isPaginator();
         boolean select = table.isSelectionEnabled();
         boolean dblSelect = select && table.isDoubleClickSelect(); 
         boolean ajaxSelect = select && table.hasSelectionClientBehaviour() || (table.getRowSelectListener() != null) || (table.getRowUnselectListener() != null);
         boolean rowExp = table.getRowExpansion() != null;
         boolean pnlExp = table.getPanelExpansion() != null;
         boolean clkHdrSrt = table.isClickableHeaderSorting();        
         boolean resize = table.isResizableColumns();
         boolean reorder = table.isReorderableColumns(); 
         boolean snglSrt = table.isSingleSort(); 
         boolean disable = table.isDisabled();
         boolean scroll = table.isScrollable();
         boolean height = scroll && table.getScrollHeight() != Integer.MIN_VALUE;
 
         json.beginMap();
         json.entry("formId", form.getClientId(context));        
         json.entry("filterEvent", filterEvent);
         json.entryNonNullValue("configPanel", table.getTableConfigPanel());
         if (paging) encodePaginatorConfig(context, json, table);
         if (pnlExp) json.entry("panelExpansion", true);
         if (rowExp) json.entry("rowExpansion", true);
         if (clkHdrSrt) json.entry("clickableHeaderSorting", true);        
         if (height) json.entry("height", table.getScrollHeight());
         if (select) json.entry("selectionMode", table.getSelectionMode());
         if (dblSelect) json.entry("dblclickSelect", true);
         if (ajaxSelect) json.entry("instantSelect", true);
         if (resize) json.entry("resizableColumns", true);
         if (reorder) json.entry("reorderableColumns", true);
         if (snglSrt) json.entry("singleSort", true);
         if (disable) json.entry("disable", true);
         if (scroll) {
             json.entry("scrollable", true);
             json.entry("liveScroll", table.isLiveScroll());
             json.entry("scrollStep", table.getRows());
             json.entry("scrollLimit", table.getRowCount());
         }
 
         encodeClientBehaviors(context, table, json);
 
         json.endMap();
 
         String widgetVar = this.resolveWidgetVar(table);
 		writer.startElement(HTML.SCRIPT_ELEM, table);
 		writer.writeAttribute(HTML.TYPE_ATTR, "text/javascript", null);        
         writer.write("var " + widgetVar + " = new ice.ace.DataTable('" + clientId + "', " + json + ");");
 		writer.endElement(HTML.SCRIPT_ELEM);
 	}
 
     protected void encodePaginatorConfig(FacesContext context, JSONBuilder scriptJson, DataTable table) throws IOException {        
         JSONBuilder configJson = new JSONBuilder();
         String clientId = table.getClientId(context);
         String paginatorPosition = table.getPaginatorPosition();
         String paginatorContainers = paginatorPosition.equalsIgnoreCase("both")
             ? "'" + clientId + "_paginatortop','" + clientId + "_paginatorbottom'"
             : "'" + clientId + "_paginator" + paginatorPosition + "'";
 
         boolean disabled = table.isDisabled();
         String template = table.getPaginatorTemplate() ;
         String rowTemplate = table.getRowsPerPageTemplate();
         String currPgTemplate = table.getCurrentPageReportTemplate();
         boolean notAlwaysVis = !table.isPaginatorAlwaysVisible();
 
         configJson.beginMap();
         configJson.entry("rowsPerPage", table.getRows());
         configJson.entry("totalRecords", table.getRowCount());
         configJson.entry("initialPage", table.getPage());
         configJson.entry("containers", "[" + paginatorContainers + "]", true);
         configJson.entryNonNullValue("template", template);
         configJson.entryNonNullValue("rowsPerPageOptions", rowTemplate);
         configJson.entryNonNullValue("pageReportTemplate", currPgTemplate);
         if (notAlwaysVis) configJson.entry("alwaysVisible",false);
         if (disabled) configJson.entry("pageLinks", 1);
         else configJson.entry("pageLinks", table.getPageCount());
         configJson.endMap();
 
         scriptJson.entry("paginator", "new YAHOO.widget.Paginator(" + configJson + ")", true);
     }
 
 
 	protected void encodeEntierty(FacesContext context, DataTable table) throws IOException{
 		ResponseWriter writer = context.getResponseWriter();
 		String clientId = table.getClientId(context);
         boolean scrollable = table.isScrollable();
 
         // init statemap while row index == -1
         table.getStateMap();
 
         String containerClass = scrollable
                 ? DataTableConstants.CONTAINER_CLASS + " " + DataTableConstants.SCROLLABLE_CONTAINER_CLASS
                 : DataTableConstants.CONTAINER_CLASS;
 
         containerClass = table.getStyleClass() != null
                             ? containerClass + " " + table.getStyleClass()
                             : containerClass;
 
         String style = null;
 
         boolean hasPaginator = table.isPaginator();
         String paginatorPosition = table.getPaginatorPosition();
 
         writer.startElement(HTML.DIV_ELEM, table);
         writer.writeAttribute(HTML.ID_ATTR, clientId, HTML.ID_ATTR);
         writer.writeAttribute(HTML.CLASS_ATTR, containerClass, "styleClass");
 
         if ((style = table.getStyle()) != null) writer.writeAttribute(HTML.STYLE_ELEM, style, HTML.STYLE_ELEM);
 
         encodeFacet(context, table, table.getHeader(), DataTableConstants.HEADER_CLASS);
 
         if (hasPaginator && !paginatorPosition.equalsIgnoreCase("bottom")) encodePaginatorMarkup(context, table, "top");
 
         if (scrollable) encodeScrollableTable(context, table);
         else encodeRegularTable(context, table);
 
         if (hasPaginator && !paginatorPosition.equalsIgnoreCase("top")) encodePaginatorMarkup(context, table, "bottom");
 
         encodeFacet(context, table, table.getFooter(), DataTableConstants.FOOTER_CLASS);
 
         if (table.isSelectionEnabled()) encodeSelectionAndDeselectionHolder(context, table);
 
         encodeScript(context, table);
 
         // Avoid sharing cached stateMap with other iterative instances
         table.clearCachedStateMap();
 
         if (!"false".equals(context.getExternalContext().getInitParameter("ForceFullTableDOMUpdates"))) {
             writer.startElement(HTML.DIV_ELEM, null);
             writer.writeAttribute(HTML.STYLE_ATTR, "display:none;",null);
             writer.writeText(table.getForcedUpdateCounter(), null);
             writer.endElement(HTML.DIV_ELEM);
         }
 
         writer.endElement(HTML.DIV_ELEM);
 	}
 
     protected void encodeUtilityChildren(FacesContext context, DataTable table) throws IOException {
         // Run the encode routines of children who rely on DT to initialize
         for (UIComponent child : table.getChildren()) {
             if (child instanceof TableConfigPanel) child.encodeAll(context);
         }
     }
 
     protected void encodeRegularTable(FacesContext context, DataTable table) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         List<Column> columns = table.getColumns();
         encodeUtilityChildren(context, table);
         writer.startElement(HTML.DIV_ELEM, null);
         writer.startElement(HTML.TABLE_ELEM, null);
         if (table.hasHeaders()) encodeTableHead(context, table, columns);
         encodeTableBody(context, table, columns);
         encodeTableFoot(context, table, columns);
         writer.endElement(HTML.TABLE_ELEM);
         writer.endElement(HTML.DIV_ELEM);
     }
 
     protected void encodeScrollableTable(FacesContext context, DataTable table) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         List<Column> columns = table.getColumns();
         encodeUtilityChildren(context, table);
 
         if (table.hasHeaders()) {
             writer.startElement(HTML.DIV_ELEM, null);
             writer.writeAttribute(HTML.CLASS_ATTR, DataTableConstants.SCROLLABLE_HEADER_CLASS, null);
             writer.startElement(HTML.TABLE_ELEM, null);
             encodeTableHead(context, table, columns);
             writer.endElement(HTML.TABLE_ELEM);
             writer.endElement(HTML.DIV_ELEM);
         }
 
         writer.startElement(HTML.DIV_ELEM, null);
         String scrollClass = DataTableConstants.SCROLLABLE_X_CLASS + " " + DataTableConstants.SCROLLABLE_BODY_CLASS;
         writer.writeAttribute(HTML.CLASS_ATTR, scrollClass, null);
         writer.writeAttribute(HTML.STYLE_ELEM, "max-height:" + table.getScrollHeight() + "px", null);
         writer.startElement(HTML.TABLE_ELEM, null);
 
         if (table.hasHeaders()) {
             table.setInDuplicateSegment(true);
             encodeTableHead(context, table, columns);
             table.setInDuplicateSegment(false);
         }
 
         encodeTableBody(context, table, columns);
 
         table.setInDuplicateSegment(true);
         encodeTableFoot(context, table, columns);
         table.setInDuplicateSegment(false);
 
         writer.endElement(HTML.TABLE_ELEM);
         writer.endElement(HTML.DIV_ELEM);
 
         writer.startElement(HTML.DIV_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, DataTableConstants.SCROLLABLE_FOOTER_CLASS, null);
         writer.startElement(HTML.TABLE_ELEM, null);
         encodeTableFoot(context, table, columns);
         writer.endElement(HTML.TABLE_ELEM);
         writer.endElement(HTML.DIV_ELEM);
     }
 
     private void writeConfigPanelLaunchButton(ResponseWriter writer, UIComponent component, boolean first) throws IOException {
         String jsId = this.resolveWidgetVar(component);
         String panelJsId = this.resolveWidgetVar(((DataTable)component).findTableConfigPanel(FacesContext.getCurrentInstance()));
         String clientId = ((DataTable)component).findTableConfigPanel(FacesContext.getCurrentInstance()).getClientId();
 
         writer.startElement(HTML.SPAN_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, "ui-tableconf-button", null);
         writer.writeAttribute(HTML.STYLE_ELEM, (first) ? "left:0;" : "right:0;", null);
         writer.startElement(HTML.ANCHOR_ELEM, null);
 
         writer.writeAttribute(HTML.CLASS_ATTR, "ui-state-default ui-corner-all", null);
         writer.writeAttribute(HTML.HREF_ATTR, "#", null);
         writer.writeAttribute(HTML.ONCLICK_ATTR, "ice.ace.jq(ice.ace.escapeClientId('"+ clientId +"')).toggle()", null);
         writer.writeAttribute( HTML.ID_ATTR, clientId +"_tableconf_launch", null);
         writer.startElement(HTML.SPAN_ELEM, null);
 
         writer.writeAttribute(HTML.CLASS_ATTR, "ui-icon ui-icon-gear", null);
 
         writer.endElement(HTML.SPAN_ELEM);
         writer.endElement(HTML.ANCHOR_ELEM);
         writer.endElement(HTML.SPAN_ELEM);
 
         writer.startElement(HTML.SCRIPT_ELEM, null);
         writer.writeAttribute(HTML.TYPE_ATTR, "text/javascript", null);
         writer.writeText("ice.ace.jq(function() {\n" + "\tice.ace.jq(ice.ace.escapeClientId('" + clientId + "_tableconf_launch')).hover(function(event){ice.ace.jq(event.currentTarget).toggleClass('ui-state-hover'); event.stopPropagation(); }).click(function(event){ice.ace.jq(event.currentTarget).toggleClass('ui-state-active'); var panel = ice.ace.jq(ice.ace.escapeClientId('" + clientId + "')); if (panel.is(':not(:visible)')) " + panelJsId + ".submitTableConfig(event.currentTarget); else if (" + panelJsId + ".behaviors) if (" + panelJsId + ".behaviors.open) " + panelJsId + ".behaviors.open(); event.stopPropagation(); });\n" + "});", null);
         writer.endElement(HTML.SCRIPT_ELEM);
     }
 
     protected void encodeColumnHeader(FacesContext context, DataTable table, List columnSiblings, Column column, boolean first, boolean last, boolean subRows) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String clientId = column.getClientId(context);
 		boolean isSortable = column.getValueExpression("sortBy") != null;
         boolean hasFilter = column.getValueExpression("filterBy") != null;
         int rightHeaderPadding = 0;
         int leftHeaderPadding = 0;
 
         Column nextColumn = getNextColumn(column, columnSiblings);
         boolean isCurrStacked = isCurrColumnStacked(columnSiblings, column);
         boolean isNextStacked = (nextColumn == null) ? false
                 : (nextColumn.isRendered() && nextColumn.isStacked());
 
         if (!isCurrStacked) {
             String style = column.getStyle();
             String styleClass = column.getStyleClass();
             String columnClass = DataTableConstants.COLUMN_HEADER_CLASS;
             columnClass = (table.isReorderableColumns() && column.isReorderable()) ? columnClass + " " + DataTableConstants.REORDERABLE_COL_CLASS : columnClass;
             columnClass = styleClass != null ? columnClass + " " + styleClass : columnClass;
             columnClass = (column.getSortPriority() != null && !isNextStacked) ? columnClass + " ui-state-active" : columnClass;
 
             writer.startElement(HTML.TH_ELEM, null);
             writer.writeAttribute(HTML.CLASS_ATTR, columnClass, null);
 
             if (style != null) writer.writeAttribute(HTML.STYLE_ELEM, style, null);
             if (column.getRowspan() != 1) writer.writeAttribute(HTML.ROWSPAN_ATTR, column.getRowspan(), null);
             if (column.getColspan() != 1) writer.writeAttribute(HTML.COLSPAN_ATTR, column.getColspan(), null);
         }
 
         else {
             writer.startElement("hr", null);
             writer.endElement("hr");
         }
 
         //Container
         writer.startElement(HTML.DIV_ELEM, null);
         writer.writeAttribute(HTML.ID_ATTR, clientId, null);
 
         String columnClass = DataTableConstants.COLUMN_HEADER_CONTAINER_CLASS;
         columnClass = isSortable ? columnClass + " " + DataTableConstants.SORTABLE_COLUMN_CLASS : columnClass;
         columnClass = table.isClickableHeaderSorting() ? columnClass + " clickable" : columnClass;
         // Add style class to div in stacking case, else style th
         columnClass = (column.getSortPriority() != null && (isCurrStacked || isNextStacked)) ? columnClass + " ui-state-active" : columnClass;
 
         writer.writeAttribute(HTML.CLASS_ATTR, columnClass, null);
         writer.startElement(HTML.SPAN_ELEM, null);
 
         //Configurable first-col controls
         boolean writeConfigPanelLaunchOnLeft = false;
         if (first) {
             TableConfigPanel panel = table.findTableConfigPanel(context);
             if (panel != null && panel.getType().equals("first-col")) {
                 leftHeaderPadding += 35;
                 writeConfigPanelLaunchOnLeft = true;
             }
         }
 
         // Add styling for last-col control container
         if (last) {
             TableConfigPanel panel = table.findTableConfigPanel(context);
             if (panel != null && panel.getType().equals("last-col"))
                 rightHeaderPadding += 35;
         }
 
         String paddingStyle = "";
         //if (rightHeaderPadding > 0) paddingStyle += "margin-right:" + rightHeaderPadding + "px;";
         if (leftHeaderPadding > 0) paddingStyle += "padding-left:" + leftHeaderPadding + "px;";
         if (!paddingStyle.equals("")) writer.writeAttribute(HTML.STYLE_ATTR, paddingStyle, null);
 
         if (writeConfigPanelLaunchOnLeft) {
             writeConfigPanelLaunchButton(writer, table, first);
         }
 
         writer.startElement(HTML.SPAN_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, DataTableConstants.HEAD_TEXT_CLASS, null);
 
         //Header content
         UIComponent header = column.getFacet("header");
         String headerText = column.getHeaderText();
 
         if (header != null) header.encodeAll(context);
         else if (headerText != null) writer.write(headerText);
 
 
         writer.endElement(HTML.SPAN_ELEM);
         writer.endElement(HTML.SPAN_ELEM);
 
         if (isSortable || isLastColConfPanel(context, table))
             writeHeaderRightSideControls(writer, context, table, column, isSortable, last);
 
         //Filter
         if (hasFilter)
             encodeFilter(context, table, column);
 
         writer.endElement(HTML.DIV_ELEM);
         
         if (!isNextStacked) {
             writer.endElement("th");
         } else if (subRows) {
             // If in a multirow header case, and using stacked, enforce these restrictions
             if (!areBothSingleColumnSpan(column, nextColumn))
                 throw new FacesException("DataTable : \"" + table.getClientId(context) + "\" must not have stacked header columns, with colspan values greater than 1.");
             if (!isNextColumnRowSpanEqual(column, nextColumn))
                 throw new FacesException("DataTable : \"" + table.getClientId(context) + "\" must not have stacked header columns, with unequal rowspan values.");
         }
     }
 
     private Column getNextColumn(Column column, List columnSiblings) {
         int index = columnSiblings.indexOf(column);
         if (index >= 0) {
             if ((index + 1) < columnSiblings.size()) {
                 UIComponent next = (UIComponent) columnSiblings.get(index + 1);
                 if (next instanceof Column) {
                     return (Column) next;
                 }
             }
         }
         return null;
     }
 
     private boolean isNextColumnRowSpanEqual(Column column, Column nextCol) {
         return (nextCol.getRowspan() == column.getRowspan());
     }
 
     private boolean areBothSingleColumnSpan(Column column, Column nextCol) {
         return (nextCol.getColspan() == 1) && (column.getColspan() == 1);
     }
 
     private boolean isLastColConfPanel(FacesContext context, DataTable table) {
         TableConfigPanel panel = table.findTableConfigPanel(context);
         return (panel != null && panel.getType().equals("last-col"));
     }
 
     private void writeHeaderRightSideControls(ResponseWriter writer, FacesContext context, DataTable table, Column column, boolean sortable, boolean last) throws IOException {
         writer.startElement(HTML.SPAN_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, DataTableConstants.HEADER_RIGHT_CLASS, null);
 
         //Sort icon
         if (sortable) writeSortControl(writer, context, table, column);
 
         //Configurable last-col controls
         if (last && isLastColConfPanel(context, table))
             writeConfigPanelLaunchButton(writer, table, false);
 
         writer.endElement(HTML.SPAN_ELEM);
     }
 
     private void writeSortControl(ResponseWriter writer, FacesContext context, DataTable table, Column column) throws IOException {
         writer.startElement(HTML.SPAN_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, DataTableConstants.SORTABLE_COLUMN_CONTROL_CLASS, null);
 
         // Write carats
         writer.startElement(HTML.SPAN_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, DataTableConstants.SORTABLE_COLUMN_ICON_CONTAINER, null);
 
         writer.startElement(HTML.ANCHOR_ELEM, null);
         writer.writeAttribute(HTML.TABINDEX_ATTR, 0, null);
         if (column.getSortPriority() != null && column.isSortAscending())
             writer.writeAttribute(HTML.CLASS_ATTR, DataTableConstants.SORTABLE_COLUMN_ICON_UP_CLASS + " ui-toggled", null);
         else writer.writeAttribute(HTML.CLASS_ATTR, DataTableConstants.SORTABLE_COLUMN_ICON_UP_CLASS, null);
         writer.endElement(HTML.ANCHOR_ELEM);
 
         writer.startElement(HTML.ANCHOR_ELEM, null);
         writer.writeAttribute(HTML.TABINDEX_ATTR, 0, null);
         if (column.getSortPriority() != null && !column.isSortAscending())
             writer.writeAttribute(HTML.CLASS_ATTR, DataTableConstants.SORTABLE_COLUMN_ICON_DOWN_CLASS + " ui-toggled", null);
         else writer.writeAttribute(HTML.CLASS_ATTR, DataTableConstants.SORTABLE_COLUMN_ICON_DOWN_CLASS, null);
         writer.endElement(HTML.ANCHOR_ELEM);
 
         writer.endElement(HTML.SPAN_ELEM);
 
 
         // Write Sort Order Integer
         writer.startElement(HTML.SPAN_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, DataTableConstants.SORTABLE_COLUMN_ORDER_CLASS, null);
         if (table.isSingleSort()) writer.writeAttribute(HTML.STYLE_ATTR, "display:none;", null);
         else if (column.getSortPriority() != null) writer.writeText(column.getSortPriority(), null);
         writer.endElement(HTML.SPAN_ELEM);
 
         writer.endElement(HTML.SPAN_ELEM);
     }
 
     protected void encodeFilter(FacesContext context, DataTable table, Column column) throws IOException {
         Map<String,String> params = context.getExternalContext().getRequestParameterMap();
         ResponseWriter writer = context.getResponseWriter();
 
         String widgetVar = this.resolveWidgetVar(table);
         String filterId = column.getClientId(context) + "_filter";
         String filterFunction = widgetVar + ".filter(event)";
         String filterStyleClass = column.getFilterStyleClass();
         String filterEvent = table.getFilterEvent();
         filterStyleClass = filterStyleClass == null ? DataTableConstants.COLUMN_FILTER_CLASS : DataTableConstants.COLUMN_FILTER_CLASS + " " + filterStyleClass;
 
         if (column.getValueExpression("filterOptions") == null) {
             String filterValue = column.getFilterValue() != null ? column.getFilterValue() : "";
 
             writer.startElement(HTML.INPUT_ELEM, null);
             writer.writeAttribute(HTML.ID_ATTR, filterId, null);
             writer.writeAttribute(HTML.NAME_ATTR, filterId, null);
             writer.writeAttribute(HTML.CLASS_ATTR, filterStyleClass, null);
             writer.writeAttribute("size", "1", null); // Webkit requires none zero/null size value to use CSS width correctly.
             writer.writeAttribute("value", filterValue , null);
 
             if (filterEvent.equals("keyup") || filterEvent.equals("blur"))
                 writer.writeAttribute("on"+filterEvent, filterFunction , null);
 
             if (column.getFilterStyle() != null)
                 writer.writeAttribute(HTML.STYLE_ELEM, column.getFilterStyle(), null);
 
             writer.endElement(HTML.INPUT_ELEM);
         }
         else {
             writer.startElement("select", null);
             writer.writeAttribute(HTML.ID_ATTR, filterId, null);
             writer.writeAttribute(HTML.NAME_ATTR, filterId, null);
             writer.writeAttribute(HTML.CLASS_ATTR, filterStyleClass, null);
             writer.writeAttribute("onchange", filterFunction, null);
 
             SelectItem[] itemsArray = (SelectItem[]) getFilterOptions(column);
 
             for (SelectItem item : itemsArray) {
                 writer.startElement("option", null);
                 writer.writeAttribute("value", item.getValue(), null);
                 writer.write(item.getLabel());
                 writer.endElement("option");
             }
 
             writer.endElement("select");
         }
 
     }
 
     protected SelectItem[] getFilterOptions(Column column) {
         Object options = column.getFilterOptions();
         if (options instanceof SelectItem[]) return (SelectItem[]) options;
         else if (options instanceof Collection<?>) return ((Collection<SelectItem>) column.getFilterOptions()).toArray(new SelectItem[] {});
         else throw new FacesException("Filter options for column " + column.getClientId() + " should be a SelectItem array or collection");
     }
 
     protected void encodeColumnFooter(FacesContext context, DataTable table, List columnSiblings, Column column, boolean subRows) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
 
         Column nextColumn = getNextColumn(column, columnSiblings);
         boolean isCurrStacked = isCurrColumnStacked(columnSiblings, column);
         boolean isNextStacked = (nextColumn == null) ? false
                 : (nextColumn.isRendered() && nextColumn.isStacked());
 
         if (!isCurrStacked) {
             String style = column.getStyle();
             String styleClass = column.getStyleClass();
             String footerClass = styleClass != null ? DataTableConstants.COLUMN_FOOTER_CLASS + " " + styleClass : DataTableConstants.COLUMN_FOOTER_CLASS;
 
             writer.startElement(HTML.TD_ELEM, null);
             writer.writeAttribute(HTML.CLASS_ATTR, footerClass, null);
             if (style != null) writer.writeAttribute(HTML.STYLE_ELEM, style, null);
             if (column.getRowspan() != 1) writer.writeAttribute(HTML.ROWSPAN_ATTR, column.getRowspan(), null);
             if (column.getColspan() != 1) writer.writeAttribute(HTML.COLSPAN_ATTR, column.getColspan(), null);
         }
         else {
             writer.startElement("hr", null);
             writer.endElement("hr");
         }
 
         //Container
         writer.startElement(HTML.DIV_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, DataTableConstants.COLUMN_FOOTER_CONTAINER_CLASS, null);
 
         //Footer content
         UIComponent facet = column.getFacet("footer");
         String text = column.getFooterText();
         if (facet != null) {
             facet.encodeAll(context);
         } else if (text != null) {
             writer.write(text);
         }
 
         writer.endElement(HTML.DIV_ELEM);
         
         if (!isNextStacked) {
             writer.endElement(HTML.TD_ELEM);
         } else if (subRows) {
             // If in a multirow footer case, and using stacked, enforce these restrictions
             if (!areBothSingleColumnSpan(column, nextColumn))
                 throw new FacesException("DataTable : \"" + table.getClientId(context) + "\" must not have stacked footer columns, with colspan values greater than 1.");
             if (!isNextColumnRowSpanEqual(column, nextColumn))
                 throw new FacesException("DataTable : \"" + table.getClientId(context) + "\" must not have stacked footer columns, with unequal rowspan values.");
         }
     }
 
     protected void encodeTableHead(FacesContext context, DataTable table, List<Column> columns) throws IOException {
         List headContainer = columns;
         ResponseWriter writer = context.getResponseWriter();
         ColumnGroup group = table.getColumnGroup("header");
         if (group != null) headContainer = group.getChildren();
 
         writer.startElement(HTML.THEAD_ELEM, null);
 
         if (table.isInDuplicateSegment()) writer.writeAttribute(HTML.STYLE_ATTR, "display:none;", null);
 
         // For each row of a col group, or child of a datatable
         boolean firstHeadElement = true;
         boolean subRows = false;
         Iterator<UIComponent> headElementIterator = headContainer.iterator();
         do {
             UIComponent headerElem = headElementIterator.next();
             List<UIComponent> headerRowChildren = new ArrayList<UIComponent>();
             int i = 0;
 
             // If its a row, get the row children, else add the column as a pseduo child, if not column, break.
             if (headerElem.isRendered()) {
                 if (headerElem instanceof Row) {
                     Row headerRow = (Row) headerElem;
                     headerRowChildren = headerRow.getChildren();
                     subRows = true;
                 } else headerRowChildren.add(headerElem);
 
                 // If the element was a row of a col-group render another row for a subrow of the header
                 if (subRows || firstHeadElement) writer.startElement(HTML.TR_ELEM, null);
 
                 // Either loop through row children or render the single column/columns
                 Iterator<UIComponent> componentIterator = headerRowChildren.iterator();
                 boolean firstComponent = true;
                 while (componentIterator.hasNext()) {
                     UIComponent headerRowChild = componentIterator.next();
                     if (headerRowChild.isRendered() && headerRowChild instanceof Column)
                         encodeColumnHeader(context, table,
                                 (subRows) ? headerRowChildren : headContainer,
                                 (Column) headerRowChild,
                                 (firstComponent && firstHeadElement),
                                 (!headElementIterator.hasNext() && !componentIterator.hasNext()),
                                 subRows);
                     firstComponent = false;
                 }
 
                 firstHeadElement = false;
 
                 if (subRows) writer.endElement(HTML.TR_ELEM);
             }
         } while (headElementIterator.hasNext());
 
         if (!subRows) writer.endElement(HTML.TR_ELEM);
 
         writer.endElement(HTML.THEAD_ELEM);
     }
 
     protected void encodeTableBody(FacesContext context, DataTable table, List<Column> columns) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String rowIndexVar = table.getRowIndexVar();
         String clientId = table.getClientId(context);
 
         if (table.isLazy()) table.loadLazyData();
 
         int rows = table.getRows();
 		int first = table.getFirst();
         int page = table.getPage();
         int rowCount = table.getRowCount();
         int rowCountToRender = rows == 0 ? rowCount : rows;
         boolean hasData = rowCount > 0;
 
         String tbodyClass = hasData ? DataTableConstants.DATA_CLASS : DataTableConstants.EMPTY_DATA_CLASS;
 
         writer.startElement(HTML.TBODY_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, tbodyClass, null);
 
         Map<Object, List<String>> rowToSelectedFieldsMap = table.getRowToSelectedFieldsMap();
         
         if (hasData)
             for (int i = first; i < (first + rowCountToRender); i++) {
                 encodeRow(context, table, columns, rowToSelectedFieldsMap, clientId, i,
                         null, rowIndexVar, i == first);
             }
         else encodeEmptyMessage(table, writer, columns);
 
         writer.endElement(HTML.TBODY_ELEM);
 		table.setRowIndex(-1);
 		if (rowIndexVar != null) context.getExternalContext().getRequestMap().remove(rowIndexVar);
     }
 
     private void encodeEmptyMessage(DataTable table, ResponseWriter writer, List<Column> columns) throws IOException {
         String emptyMessage = table.getEmptyMessage();
         if (emptyMessage != null) {
             writer.startElement(HTML.TR_ELEM, null);
             writer.writeAttribute(HTML.CLASS_ATTR, DataTableConstants.ROW_CLASS, null);
 
             writer.startElement(HTML.TD_ELEM, null);
             writer.writeAttribute(HTML.COLSPAN_ATTR, columns.size(), null);
             writer.write(emptyMessage);
             writer.endElement(HTML.TD_ELEM);
 
             writer.endElement(HTML.TR_ELEM);
         }
     }
 
     protected void encodeRow(FacesContext context, DataTable table, List<Column> columns, Map<Object, List<String>> rowToSelectedFieldsMap, String clientId, int rowIndex, String parentIndex, String rowIndexVar, boolean topRow) throws IOException {
         table.setRowIndex(rowIndex);
         if (!table.isRowAvailable()) return;
         if (rowIndexVar != null) context.getExternalContext().getRequestMap().put(rowIndexVar, rowIndex);
 
         RowState rowState = table.getStateMap().get(table.getRowData());
         
         boolean selected = rowState.isSelected();
         boolean unselectable = !rowState.isSelectable();
         boolean expanded = rowState.isExpanded();
         boolean visible = rowState.isVisible();
 
         List<String> selectedCellExpressions = (rowToSelectedFieldsMap != null)
             ? (List<String>)(rowToSelectedFieldsMap.get(table.getRowData()))
             : null;
 
         context.getExternalContext().getRequestMap().put(table.getRowStateVar(), rowState);
         
         if (visible) {
             ResponseWriter writer = context.getResponseWriter();
 
             // Add leading conditional row for this row object if required
             List<Row> leadingRows = table.getConditionalRows(rowIndex, true);
             for (Row r : leadingRows) encodeConditionalRow(context, r);
 
             String userRowStyleClass = table.getRowStyleClass();
             String expandedClass = expanded ? DataTableConstants.EXPANDED_ROW_CLASS : "";
             String unselectableClass = unselectable ? DataTableConstants.UNSELECTABLE_ROW_CLASS : "";
             String rowStyleClass = rowIndex % 2 == 0 ? DataTableConstants.ROW_CLASS + " " + DataTableConstants.EVEN_ROW_CLASS : DataTableConstants.ROW_CLASS + " " + DataTableConstants.ODD_ROW_CLASS;
 
             if (selected && table.getSelectionMode() != null) rowStyleClass = rowStyleClass + " ui-selected ui-state-active";
             if (userRowStyleClass != null) rowStyleClass = rowStyleClass + " " + userRowStyleClass;
 
             writer.startElement(HTML.TR_ELEM, null);
             parentIndex = (parentIndex != null) ? parentIndex + "." : "";
             writer.writeAttribute(HTML.ID_ATTR, clientId + "_row_" + parentIndex + rowIndex, null);
             writer.writeAttribute(HTML.CLASS_ATTR, rowStyleClass + " " + expandedClass + " " + unselectableClass, null);
             writer.writeAttribute(HTML.TABINDEX_ATTR, "0", null);
 
             boolean innerTdDivRequired = (table.isScrollable() || table.isResizableColumns()) & topRow;
 
             for (Column kid : columns) {
                 if (kid.isRendered()) {
                     boolean cellSelected = false;
                     if (selectedCellExpressions != null) {
                         ValueExpression ve = kid.getValueExpression("selectBy") != null ? kid.getValueExpression("selectBy") : kid.getValueExpression("value");
                         if (ve != null)
                             cellSelected = selectedCellExpressions.contains(ve.getExpressionString());
                     }
                     
                     encodeRegularCell(context, columns, kid, cellSelected, innerTdDivRequired);
                 }
             }
 
             if (rowIndexVar != null) context.getExternalContext().getRequestMap().put(rowIndexVar, rowIndex);
             writer.endElement(HTML.TR_ELEM);
 
             if (expanded) {
                 context.getExternalContext().getRequestMap().put(clientId + "_expandedRowId", ""+rowIndex);
                 boolean isPanel = table.getPanelExpansion() != null;
                 boolean isRow = table.getRowExpansion() != null;
 
                 // Ensure that table.getTableId returns correctly for request map look
                 table.setRowIndex(-1);
 
                 if (isPanel && isRow) {
                     if (rowState.getExpansionType() == RowState.ExpansionType.ROW) {
                         encodeRowExpansion(context, table, columns, writer);
                     }
                     else if (rowState.getExpansionType() == RowState.ExpansionType.PANEL) {
                         encodeRowPanelExpansion(context, table);
                     }
                 } else if (isPanel) {
                     encodeRowPanelExpansion(context, table);
                 } else if (isRow) {
                     encodeRowExpansion(context, table, columns, writer);
                 }
 
                 // Row index will have come back different from row expansion.
                 table.setRowIndex(rowIndex);
             }
 
             // Add tailing conditional row for this row object if required
             List<Row> tailingRows = table.getConditionalRows(rowIndex, false);
             for (Row r : tailingRows) encodeConditionalRow(context, r);
         }
     }
 
     protected void encodeConditionalRow(FacesContext context, Row r) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         writer.startElement(HTML.TR_ELEM, null);
         if (r.getStyle() != null) writer.writeAttribute(HTML.STYLE_ATTR, r.getStyle(), null);
         if (r.getStyleClass() != null) writer.writeAttribute(HTML.CLASS_ATTR, "dt-cond-row " + r.getStyleClass(), null);
 
         List<UIComponent> children = r.getChildren();
         List<Column> rowColumns = new ArrayList<Column>(children.size());
         for (UIComponent kid : children)
             if (kid instanceof Column)
                 rowColumns.add((Column)kid);
 
         for (Column kid : rowColumns)
             if (kid.isRendered())
                 encodeConditionalRowCell(context, kid);
 
         writer.endElement(HTML.TR_ELEM);
     }
     
     protected void encodeConditionalRowCell(FacesContext context, Column c) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
 
         writer.startElement(HTML.TD_ELEM, null);
         writer.writeAttribute(HTML.COLSPAN_ATTR, c.getColspan(), null);
         if (c.getStyle() != null) writer.writeAttribute(HTML.STYLE_ATTR, c.getStyle(), null);
         if (c.getStyleClass() != null) writer.writeAttribute(HTML.CLASS_ATTR, c.getStyleClass(), null);
 
         c.encodeAll(context);
         writer.endElement(HTML.TD_ELEM);
     }
 
     protected void encodeRegularCell(FacesContext context, List columnSiblings, Column column, boolean selected, boolean resizable) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
 
         Column nextColumn = getNextColumn(column, columnSiblings);
         boolean isCurrStacked = isCurrColumnStacked(columnSiblings, column);
         boolean isNextStacked = (nextColumn == null) ? false
                 : (nextColumn.isRendered() && nextColumn.isStacked());
         boolean isCurrGrouped = column.getCurrGroupLength() > 0;
         boolean isNextGrouped = isCurrGrouped ? false // No need to calculate next group if grouped 
                     : column.isNextColumnGrouped();
 
         if (isCurrGrouped) {
             column.setCurrGroupLength(column.getCurrGroupLength()-1);
         } else {
             if (!isCurrStacked) {
                 writer.startElement(HTML.TD_ELEM, null);
 
                 if (column.getStyle() != null) writer.writeAttribute(HTML.STYLE_ELEM, column.getStyle(), null);
 
                 if (isNextGrouped) writer.writeAttribute(HTML.ROWSPAN_ATTR, column.findCurrGroupLength()+1, null);
                 
                 CellEditor editor = column.getCellEditor();
 
                 String columnStyleClass = column.getStyleClass();
                 if (editor != null) {
                     columnStyleClass = columnStyleClass == null ? DataTableConstants.EDITABLE_COLUMN_CLASS : DataTableConstants.EDITABLE_COLUMN_CLASS + " " + columnStyleClass;
                 }
                 // Add alternating styling, except when last group is the same value, split by an expansion or conditional row
                 if (column.getValueExpression("groupBy") != null) {
                     if (column.isLastGroupDifferent()) column.setOddGroup(!column.isOddGroup());
                     if (columnStyleClass == null) columnStyleClass = "";
                     columnStyleClass += column.isOddGroup() ? " ui-datatable-group-odd" : " ui-datatable-group-even";
                 }
                 if (selected) columnStyleClass = columnStyleClass == null ? "ui-state-active ui-selected" : columnStyleClass + " ui-state-active ui-selected";
                 if (columnStyleClass != null) writer.writeAttribute(HTML.CLASS_ATTR, columnStyleClass, null);
 
                 if (resizable) writer.startElement(HTML.DIV_ELEM, null);
             }
             else {
                 writer.startElement("hr", null);
                 writer.endElement("hr");
             }
 
             column.encodeAll(context);
 
             if (!isNextStacked) {
                 if (resizable) writer.endElement(HTML.DIV_ELEM);
                 writer.endElement(HTML.TD_ELEM);
             }
         }
     }
 
     protected void encodeTableFoot(FacesContext context, DataTable table, List<Column> columns) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         ColumnGroup group = table.getColumnGroup("footer");
         boolean shouldRender = table.hasFooterColumn(columns) || group != null;
 
         if (!shouldRender) return;
 
         writer.startElement(HTML.TFOOT_ELEM, null);
 
         if (table.isInDuplicateSegment()) writer.writeAttribute(HTML.STYLE_ATTR, "display:none;", null);
 
         if (group != null) {
             for (UIComponent child : group.getChildren()) {
                 if (child.isRendered() && child instanceof Row) {
                     Row footerRow = (Row) child;
                     writer.startElement(HTML.TR_ELEM, null);
 
                     List<UIComponent> footerRowChildren = footerRow.getChildren();
                     for (UIComponent footerRowChild : footerRowChildren)
                         if (footerRowChild.isRendered() && footerRowChild instanceof Column)
                             encodeColumnFooter(context, table, footerRowChildren, (Column) footerRowChild, true);
 
                     writer.endElement(HTML.TR_ELEM);
                 }
             }
         } else {
             writer.startElement(HTML.TR_ELEM, null);
             for (Column column : columns) {
                 encodeColumnFooter(context, table, columns, column, false);
             }
             writer.endElement(HTML.TR_ELEM);
         }
         writer.endElement(HTML.TFOOT_ELEM);
     }
 
     protected void encodeFacet(FacesContext context, DataTable table, UIComponent facet, String styleClass) throws IOException {
         if (facet == null) return;
         ResponseWriter writer = context.getResponseWriter();
 
         writer.startElement(HTML.DIV_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, styleClass, null);
 
         facet.encodeAll(context);
         writer.endElement(HTML.DIV_ELEM);
     }
 
 
     protected void encodePaginatorMarkup(FacesContext context, DataTable table, String position) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String clientId = table.getClientId(context);
 
         String styleClass = "ui-paginator ui-paginator-" + position + " ui-widget-header";
 
         if (!position.equals("top") && table.getFooter() == null)
             styleClass = styleClass + " ui-corner-bl ui-corner-br";
         else if (!position.equals("bottom") && table.getHeader() == null)
             styleClass = styleClass + " ui-corner-tl ui-corner-tr";
 
         writer.startElement(HTML.DIV_ELEM, null);
         writer.writeAttribute(HTML.ID_ATTR, clientId + "_paginator" + position, null);
         writer.writeAttribute(HTML.CLASS_ATTR, styleClass, null);
 
         TableConfigPanel panel = table.findTableConfigPanel(context);
         if (panel != null && panel.getType().equals("paginator-button")) {
             writeConfigPanelLaunchButton(writer, table, false);
         }
 
         writer.endElement(HTML.DIV_ELEM);
     }
 
     protected void encodeSelectionAndDeselectionHolder(FacesContext context, DataTable table) throws IOException {
 		ResponseWriter writer = context.getResponseWriter();
         String id = table.getClientId(context) + "_selection";
 
 		writer.startElement(HTML.INPUT_ELEM, null);
 		writer.writeAttribute(HTML.TYPE_ATTR, "hidden", null);
 		writer.writeAttribute(HTML.ID_ATTR, id, null);
 		writer.writeAttribute(HTML.NAME_ATTR, id, null);
         writer.endElement(HTML.INPUT_ELEM);
 
 
         id = table.getClientId(context) + "_deselection";
         writer.startElement(HTML.INPUT_ELEM, null);
         writer.writeAttribute(HTML.TYPE_ATTR, "hidden", null);
         writer.writeAttribute(HTML.ID_ATTR, id, null);
         writer.writeAttribute(HTML.NAME_ATTR, id, null);
         writer.endElement(HTML.INPUT_ELEM);
 	}
 
     private void encodeRowPanelContraction(FacesContext context, DataTable table) {
         String clientId = table.getClientId(context);
         String expandedRowId = context.getExternalContext().getRequestParameterMap().get(clientId + "_contractedRowId");
 
         DataModel model = table.getDataModel();
         if (!(table.hasTreeDataModel())) {
             table.setRowIndex(Integer.parseInt(expandedRowId));
             table.getStateMap().get(model.getRowData()).setExpanded(false);
             table.setRowIndex(-1);
         } else {
             TreeDataModel rootModel = (TreeDataModel)model;
             rootModel.setRootIndex(expandedRowId);
             table.getStateMap().get(rootModel.getRootData()).setExpanded(false);
             rootModel.setRootIndex(null);
         }
     }
 
     private void encodeRowContraction(FacesContext context, DataTable table) {
         String clientId = table.getClientId(context);
         String expandedRowId = context.getExternalContext().getRequestParameterMap().get(clientId + "_contractedRowId");
 
         Object model = table.getDataModel();
         if (!(table.hasTreeDataModel())) throw new FacesException("DataTable : \"" + clientId + "\" must be bound to an instance of TreeDataModel when using sub-row expansion.");
 
         TreeDataModel rootModel = (TreeDataModel)model;
         rootModel.setRootIndex(expandedRowId);
         table.getStateMap().get(rootModel.getRootData()).setExpanded(false);
         rootModel.setRootIndex(null);
     }
 
     private void encodeRowExpansion(FacesContext context, DataTable table, List<Column> columns, ResponseWriter writer) throws IOException {
         String rowVar = table.getVar();
         String rowIndexVar = table.getRowIndexVar();
         String clientId = table.getClientId(context);
 
         String expandedRowId = context.getExternalContext().getRequestParameterMap().get(clientId + "_expandedRowId");
         if (expandedRowId == null) {
             expandedRowId = (String) context.getExternalContext().getRequestMap().get(clientId + "_expandedRowId");
         }
 
         Object model = table.getDataModel();
 
         if (!(table.hasTreeDataModel())) throw new FacesException("DataTable : \"" + clientId + "\" must be bound to an instance of TreeDataModel when using sub-row expansion.");
         TreeDataModel rootModel = (TreeDataModel)model;
         rootModel.setRootIndex(expandedRowId);
         table.getStateMap().get(rootModel.getRootData()).setExpanded(true);
         table.setRowIndex(0);
 
         if (rootModel.getRowCount() > 0)
         while (rootModel.getRowIndex() < rootModel.getRowCount()) {
 //            System.out.println("----------");
 //            System.out.println(rootModel.getRootIndex());
 //            System.out.println(rootModel.getRowIndex());
 //            System.out.println("----------");
 
             if (rowVar != null) context.getExternalContext().getRequestMap().put(rowVar, rootModel.getRowData());
             if (rowIndexVar != null) context.getExternalContext().getRequestMap().put(rowIndexVar, rootModel.getRowIndex());
 
             RowState rowState = table.getStateMap().get(rootModel.getRowData());
             boolean selected = rowState.isSelected();
             boolean expanded = rowState.isExpanded();
             boolean unselectable = !rowState.isSelectable();
             boolean visible = rowState.isVisible();
             Map<Object, List<String>> rowToSelectedFieldsMap = table.getRowToSelectedFieldsMap();
             List<String> selectedCellExpressions = null;
             if (rowToSelectedFieldsMap != null)
                 selectedCellExpressions = (List<String>)(rowToSelectedFieldsMap.get(table.getRowData()));
             context.getExternalContext().getRequestMap().put(table.getRowStateVar(), rowState);
 
             String expandedClass = expanded ? DataTableConstants.EXPANDED_ROW_CLASS : "";
             String alternatingClass = (rootModel.getRowIndex() % 2 == 0) ? DataTableConstants.EVEN_ROW_CLASS : DataTableConstants.ODD_ROW_CLASS;
             String selectionClass = (selected && table.getSelectionMode() != null) ? "ui-selected ui-state-active" : "";
             String unselectableClass = unselectable ? DataTableConstants.UNSELECTABLE_ROW_CLASS : "";
 
             if (visible) {
                 writer.startElement(HTML.TR_ELEM, null);
                 writer.writeAttribute(HTML.ID_ATTR, clientId + "_row_" + expandedRowId + "." + rootModel.getRowIndex(), null);
                 writer.writeAttribute(HTML.CLASS_ATTR, DataTableConstants.ROW_CLASS + " " + alternatingClass + " " + selectionClass + " " + expandedClass + " " + unselectableClass, null);
 
                 for (Column kid : columns) {
                     if (kid.isRendered()) {
                         boolean cellSelected = false;
                         if (selectedCellExpressions != null) {
                             ValueExpression ve = kid.getValueExpression("selectBy") != null ? kid.getValueExpression("selectBy") : kid.getValueExpression("value");
                             if (ve != null)
                                 cellSelected = selectedCellExpressions.contains(ve.getExpressionString());
                         }
                         
                         encodeRegularCell(context, columns, kid, cellSelected, false);
                     }
                 }
                 writer.endElement(HTML.TR_ELEM);
 
                 if (expanded) {
                     int rowIndex = rootModel.getRowIndex();
                     context.getExternalContext().getRequestMap().put(clientId + "_expandedRowId", expandedRowId+"."+rowIndex);
 
                     PanelExpansion panelExpansion = table.getPanelExpansion();
                     RowExpansion rowExpansion = table.getRowExpansion();
                     boolean isPanel = panelExpansion != null;
                     boolean isRow = rowExpansion != null;
 
                     // Ensure that table.getTableId returns correctly for request map look
                     table.setRowIndex(-1);
 
                     if (isPanel && isRow) {
                         if (rowState.getExpansionType() == RowState.ExpansionType.ROW) {
                             encodeRowExpansion(context, table, columns, writer);
                         }
                         else if (rowState.getExpansionType() == RowState.ExpansionType.PANEL) {
                             encodeRowPanelExpansion(context, table);
                         }
                     } else if (isPanel) {
                         encodeRowPanelExpansion(context, table);
                     } else if (isRow) {
                         encodeRowExpansion(context, table, columns, writer);
                     }
 
                     rootModel = (TreeDataModel) table.getDataModel();
                     rootModel.setRootIndex(expandedRowId);
                     table.setRowIndex(rowIndex); // Row index will have come back different from row expansion.
                     context.getExternalContext().getRequestMap().put(clientId + "_expandedRowId", expandedRowId);
                 }
             }
 
             table.setRowIndex(rootModel.getRowIndex() + 1);
             if (rowIndexVar != null) context.getExternalContext().getRequestMap().remove(rowIndexVar);
             if (rowVar != null) context.getExternalContext().getRequestMap().remove(rowVar);
         }
 
         rootModel.setRootIndex(null);
         table.setRowIndex(-1);
     }
 
     protected void encodeRowPanelExpansion(FacesContext context, DataTable table) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         Map<String,String> params = context.getExternalContext().getRequestParameterMap();
         String clientId = table.getClientId(context);
         Object model = table.getDataModel();
 
 
         String expandedRowId = params.get(clientId + "_expandedRowId");
         if (expandedRowId == null) {
             expandedRowId = (String) context.getExternalContext().getRequestMap().get(clientId + "_expandedRowId");
         }
 
         int sepIndex = expandedRowId.lastIndexOf('.');
         String rootIndex = null;
         if (sepIndex >= 0) {
             rootIndex = expandedRowId.substring(0,sepIndex);
             expandedRowId = expandedRowId.substring(sepIndex+1);
         }
 
         if (rootIndex != null) ((TreeDataModel)model).setRootIndex(rootIndex);
         table.setRowIndex(Integer.parseInt(expandedRowId));
 
         table.getStateMap().get(table.getRowData()).setExpanded(true);
 
         writer.startElement(HTML.TR_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, DataTableConstants.EXPANDED_ROW_CONTENT_CLASS + " ui-widget-content " + DataTableConstants.UNSELECTABLE_ROW_CLASS , null);
 
         writer.startElement(HTML.TD_ELEM, null);
 
         int enabledColumns = 0;
         for (Column c : table.getColumns()) if (c.isRendered() && !c.isStacked()) enabledColumns++;
 
         writer.writeAttribute(HTML.COLSPAN_ATTR, enabledColumns, null);
         table.getPanelExpansion().encodeAll(context);
 
         writer.endElement(HTML.TD_ELEM);
         writer.endElement(HTML.TR_ELEM);
         table.setRowIndex(-1);
     }
 
 
     protected void encodeLiveRows(FacesContext context, DataTable table) throws IOException {
         Map<String,String> params = context.getExternalContext().getRequestParameterMap();
         int scrollOffset = Integer.parseInt(params.get(table.getClientId(context) + "_scrollOffset"));
         String clientId = table.getClientId(context);
         String rowIndexVar = table.getRowIndexVar();
 
         Map<Object, List<String>> rowToSelectedFieldsMap = table.getRowToSelectedFieldsMap();
         
         for (int i = scrollOffset; i < (scrollOffset + table.getRows()); i++)
             encodeRow(context, table, table.getColumns(), rowToSelectedFieldsMap, clientId, i, null, rowIndexVar, i == 0);
     }
 
     private boolean isCurrColumnStacked(List comps, Column currCol) {
         // The first column can not be stacked, only subsequent ones can be
         // stacked under it
         int index = comps.indexOf(currCol);
         if (index == 0) {
             return false;
         }
         return currCol.isStacked();
     }
 }
