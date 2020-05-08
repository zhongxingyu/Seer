 package org.icefaces.mobi.component.dataview;
 
 import org.icefaces.impl.util.DOMUtils;
 import org.icefaces.mobi.component.inputText.InputText;
 import org.icefaces.mobi.renderkit.BaseInputRenderer;
 import org.icefaces.mobi.utils.HTML;
 import org.icemobile.component.IDataView;
 import org.icemobile.model.DataViewColumnModel;
 import org.icemobile.model.DataViewColumnsModel;
 import org.icemobile.model.DataViewDataModel;
 import org.icemobile.model.IndexedIterator;
 
 import javax.el.ELContext;
 import javax.el.ValueExpression;
 import javax.faces.component.*;
 import javax.faces.component.html.*;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.render.Renderer;
 import java.io.IOException;
 import java.io.OptionalDataException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.logging.Logger;
 
 /**
  * Copyright 2010-2013 ICEsoft Technologies Canada Corp.
  * <p/>
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * <p/>
  * http://www.apache.org/licenses/LICENSE-2.0
  * <p/>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * <p/>
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * <p/>
  * User: Nils Lundquist
  * Date: 2013-04-01
  * Time: 10:47 AM
  */
 public class DataViewRenderer extends Renderer {
     private static Logger logger = Logger.getLogger(DataViewRenderer.class.getName());
 
     private DataView dataView;
     private String dvId = null;
 
     @Override
     public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
         dataView = (DataView) component;
         dvId = dataView.getClientId();
         ResponseWriter writer = context.getResponseWriter();
 
         writer.startElement(HTML.DIV_ELEM, null);
         writer.writeAttribute(HTML.ID_ATTR, dvId, null);
 
         String styleClass = IDataView.DATAVIEW_CLASS;
         String userClass = dataView.getStyleClass();
         if (userClass != null) styleClass += " " + userClass;
         writer.writeAttribute(HTML.CLASS_ATTR, styleClass, null);
 
         String userStyle = dataView.getStyle();
         if (userStyle != null)
             writer.writeAttribute(HTML.STYLE_ATTR, userStyle, null);
     }
 
     @Override
     public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
         DataView dataView = (DataView) component;
         ResponseWriter writer = context.getResponseWriter();
 
         encodeDetails(context, writer);
         encodeColumns(context, writer);
     }
 
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
 
         encodeScript(context, writer);
 
         writer.endElement(HTML.DIV_ELEM);
     }
 
     private void encodeScript(FacesContext context, ResponseWriter writer) throws IOException {
         writer.startElement(HTML.SPAN_ELEM, null);
         writer.writeAttribute(HTML.ID_ATTR, dvId + "_jswrp", null);
 
         writer.startElement(HTML.SCRIPT_ELEM, null);
         writer.writeAttribute(HTML.TYPE_ATTR, HTML.SCRIPT_TYPE_TEXT_JAVASCRIPT, null);
         writer.writeAttribute(HTML.ID_ATTR, dvId + "_js", null);
 
         boolean reactive = dataView.isReactiveColumnVisibility();
 
         String cfg = "{";
         cfg += "active:'" + dataView.getActivationMode() + "'";
         if (reactive) cfg = encodeColumnPriorities(cfg);
         cfg += "}";
 
         String js =
             "ice.mobi.dataView.create("
                 + '"' + dvId + '"'
                 + ", " + cfg
             + ");";
 
         writer.writeText(js, null);
         writer.endElement(HTML.SCRIPT_ELEM);
         writer.endElement(HTML.SPAN_ELEM);
     }
 
     private String encodeColumnPriorities(String cfg) {
         cfg += ", colvispri:[";
         Integer[] priorities = dataView.getReactiveColumnPriorities();
         for (int i = 0; i < priorities.length; i++) {
             cfg += priorities[i];
             if (i != priorities.length - 1) cfg += ',';
         }
         cfg += "]";
         return cfg;
     }
 
     private void encodeColumns(FacesContext context,
                                ResponseWriter writer) throws IOException {
         DataViewColumns columns = dataView.getColumns();
         String var = dataView.getVar();
 
         if (columns == null) encodeEmptyBodyTable(writer);
         else {
             writer.startElement(HTML.DIV_ELEM, null);
             writer.writeAttribute(HTML.CLASS_ATTR, IDataView.DATAVIEW_MASTER_CLASS, null);
 
             DataViewColumnsModel columnModel = columns.getModel();
             DataViewDataModel dataModel = dataView.getDataModel();
 
             if (columnModel.hasHeaders())
                 encodeHeaders(writer, columnModel, dataModel, true);
 
             encodeRows(context, writer, var, columnModel, dataModel);
 
             if (columnModel.hasFooters())
                 encodeFooters(writer, columnModel, dataModel, true);
 
             writer.endElement(HTML.DIV_ELEM);
         }
     }
 
     private void encodeEmptyBodyTable(ResponseWriter writer) throws IOException {
         writer.startElement(HTML.DIV_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, IDataView.DATAVIEW_MASTER_CLASS, null);
         writer.startElement(HTML.DIV_ELEM, null);
         writer.startElement(HTML.TABLE_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, IDataView.DATAVIEW_BODY_CLASS, null);
         writer.endElement(HTML.TABLE_ELEM);
         writer.endElement(HTML.DIV_ELEM);
         writer.endElement(HTML.DIV_ELEM);
     }
 
     private void encodeHeaders(ResponseWriter writer,
                                DataViewColumnsModel columnModel,
                                DataViewDataModel dataModel,
                                boolean writeTable) throws IOException {
         /* Skip table when writing duplicate alignment header */
         if (writeTable) {
             writer.startElement(HTML.TABLE_ELEM, null);
             writer.writeAttribute(HTML.CLASS_ATTR, IDataView.DATAVIEW_HEAD_CLASS, null);
         }
 
         writer.startElement(HTML.THEAD_ELEM, null);
         writer.startElement(HTML.TR_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, IDataView.DATAVIEW_HEADER_ROW_CLASS, null);
 
         for (IndexedIterator<DataViewColumnModel> columnIter = columnModel.iterator(); columnIter.hasNext();) {
             DataViewColumnModel column = columnIter.next();
             int index = columnIter.getIndex();
 
             if (column.isRendered()) {
                 writer.startElement(HTML.TH_ELEM, null);
 
                 String className = getColumnStyleClass(column, index);
 
                 writer.writeAttribute(HTML.CLASS_ATTR, className, null);
 
                 if (column.getHeaderText() != null)
                     writer.write(column.getHeaderText());
 
                 writer.startElement("i", null);
                 writer.writeAttribute(HTML.CLASS_ATTR, IDataView.DATAVIEW_SORT_INDICATOR_CLASS, null);
                 writer.endElement("i");
                 writer.endElement(HTML.TH_ELEM);
             }
         }
 
         writer.endElement(HTML.TR_ELEM);
         writer.endElement(HTML.THEAD_ELEM);
 
         if (writeTable) writer.endElement(HTML.TABLE_ELEM);
     }
 
     private void encodeFooters(ResponseWriter writer,
                                DataViewColumnsModel columnModel,
                                DataViewDataModel dataModel, boolean writeTable) throws IOException {
         if (writeTable) {
             writer.startElement(HTML.TABLE_ELEM, null);
             writer.writeAttribute(HTML.CLASS_ATTR, IDataView.DATAVIEW_FOOT_CLASS, null);
         }
 
         writer.startElement(HTML.TFOOT_ELEM, null);
         writer.startElement(HTML.TR_ELEM, null);
 
         for (IndexedIterator<DataViewColumnModel> columnIter = columnModel.iterator(); columnIter.hasNext();) {
             DataViewColumnModel column = columnIter.next();
             int index = columnIter.getIndex();
 
             if (column.isRendered()) {
                 writer.startElement(HTML.TD_ELEM, null);
 
                 String className = getColumnStyleClass(column, index);
 
                 writer.writeAttribute(HTML.CLASS_ATTR, className, null);
 
                 if (column.getFooterText() != null)
                     writer.write(column.getFooterText());
                 writer.endElement(HTML.TD_ELEM);
             }
         }
 
 
         writer.endElement(HTML.TR_ELEM);
         writer.endElement(HTML.TFOOT_ELEM);
 
         if (writeTable) writer.endElement(HTML.TABLE_ELEM);
     }
 
     private String getColumnStyleClass(DataViewColumnModel column, int index) {
         String colStyleClass = column.getStyleClass();
         String className = IDataView.DATAVIEW_COLUMN_CLASS + "-" + index;
 
         if (colStyleClass != null) className += " " + colStyleClass;
         return className;
     }
 
     private void encodeRows(FacesContext context,
                             ResponseWriter writer,
                             String var,
                             DataViewColumnsModel columnModel,
                             DataViewDataModel dataModel) throws IOException {
         ELContext elContext = context.getELContext();
         Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
         String clientId = dvId;
         String bodyClass = IDataView.DATAVIEW_BODY_CLASS;
 
         if (dataView.isRowStroke()) bodyClass += " stroke";
         if (dataView.isRowStripe()) bodyClass += " stripe";
 
         writer.startElement(HTML.DIV_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, "overthrow", null);
         writer.startElement(HTML.TABLE_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, bodyClass, null);
 
         if (columnModel.hasHeaders()) encodeHeaders(writer, columnModel, dataModel, false);
 
         writer.startElement(HTML.TBODY_ELEM, null);
 
         List<UIComponent> detailHolders = getDetailHolders(dataView.getDetails());
         Integer activeIndex = dataView.getActiveRowIndex();
 
         for (IndexedIterator<Object> dataModelIterator = dataModel.iterator(); dataModelIterator.hasNext();) {
             Object rowData = dataModelIterator.next();
             int index = dataModelIterator.getIndex();
             // Init row context
             requestMap.put(var, rowData);
 
             writer.startElement(HTML.TR_ELEM, null);
 
             writer.writeAttribute(HTML.ID_ATTR, clientId + "_" + dataModelIterator.getIndex(), null);
             writer.writeAttribute("data-index", index, null);
 
             if (activeIndex != null && activeIndex.equals(index))
                 writer.writeAttribute(HTML.CLASS_ATTR, IDataView.DATAVIEW_ROW_ACTIVE_CLASS, null);
 
             if (ActivationMode.client.equals(dataView.getActivationMode()))
                 writer.writeAttribute("data-state", encodeRowDetailString(context, detailHolders), null);
 
             for (IndexedIterator<DataViewColumnModel> columnModelIterator = columnModel.iterator(); columnModelIterator.hasNext();)
                 writeColumn(writer, elContext, columnModelIterator.next(), columnModelIterator.getIndex());
 
             writer.endElement(HTML.TR_ELEM);
         }
 
         requestMap.remove(var);
 
         writer.endElement(HTML.TBODY_ELEM);
 
         if (columnModel.hasFooters()) encodeFooters(writer, columnModel, dataModel, false);
 
         writer.endElement(HTML.TABLE_ELEM);
         writer.endElement(HTML.DIV_ELEM);
     }
 
     private void writeColumn(ResponseWriter writer, ELContext elContext, DataViewColumnModel column, int index) throws IOException {
         if (!column.isRendered()) return;
 
         ValueExpression ve = column.getValueExpression();
         Object value = ve == null ? column.getValue() : ve.getValue(elContext); // use value expression if available, value will have been pre-evaluated
         String type = column.getType();
         String colClass = getColumnStyleClass(column, index);
 
         writer.startElement(HTML.TD_ELEM, null);
 
         if (type.equals("markup"))
             writer.write(column.getMarkup().replace("{{value}}", value.toString()));
         else if (type.equals("bool")) {
             colClass += " " + DataView.DATAVIEW_BOOL_COLUMN_CLASS;
             writer.startElement("i", null);
             if (value != null) {
                 Boolean bval = (Boolean)value;
                 String resUrl;
                 if (bval) resUrl = "icon-check";
                 else resUrl = "icon-check-empty";
 
                 writer.writeAttribute(HTML.CLASS_ATTR, resUrl, null);
             }
             writer.endElement("i");
         }
         else if (type.equals("date")) {
             writer.write(getDateFormat(column).format(value));
         }
         else if (type.equals("image")) {
             writer.startElement(HTML.IMG_ELEM, null);
             if (value != null) writer.writeAttribute(HTML.SRC_ATTR, value.toString(), null);
             writer.endElement(HTML.IMG_ELEM);
         }
         else if (type.equals("list")) {
             writer.startElement(HTML.UL_ELEM, null);
             if (value != null && value instanceof List)
                 for (Object i : (List)value) {
                     writer.startElement(HTML.LI_ELEM, null);
                     writer.write(i.toString());
                     writer.endElement(HTML.LI_ELEM);
                 }
             writer.endElement(HTML.UL_ELEM);
         }
         else if (value != null)
             writer.write(value.toString());
 
         writer.writeAttribute(HTML.CLASS_ATTR, colClass, null);
 
         writer.endElement(HTML.TD_ELEM);
     }
 
     private DateFormat getDateFormat(DataViewColumnModel column) {
         String pattern = column.getDatePattern();
         String type = column.getDateType();
         TimeZone timeZone = column.getTimeZone();
         Locale locale = column.getLocale();
         Integer dateStyle = column.getTimeStyle();
         Integer timeStyle = column.getDateStyle();
 
         if (pattern == null && type == null) {
             throw new IllegalArgumentException("Either pattern or type must be specified.");
         }
 
         DateFormat df;
         if (pattern != null) {
             df = new SimpleDateFormat(pattern, locale);
         } else if (type.equals("both")) {
             df = DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
         } else if (type.equals("date")) {
             df = DateFormat.getDateInstance(dateStyle, locale);
         } else if (type.equals("time")) {
             df = DateFormat.getTimeInstance(timeStyle, locale);
         } else {
             // PENDING(craigmcc) - i18n
             throw new IllegalArgumentException("Invalid type: " + type);
         }
         df.setLenient(false);
         df.setTimeZone(timeZone);
 
         return (df);
     }
 
     private String encodeRowDetailString(FacesContext context, List<UIComponent> detailHolders) {
         StringBuilder detStr = new StringBuilder();
 
         for (Iterator<UIComponent> valueHolderIterator = detailHolders.iterator();
                 valueHolderIterator.hasNext();) {
             appendUpdateString(detStr, context, valueHolderIterator);
         }
 
         return DOMUtils.escapeAnsi(detStr.toString());
     }
 
     private void appendUpdateString(StringBuilder detStr, FacesContext context, Iterator<UIComponent> valueHolderIterator) {
         String cId = dvId + UINamingContainer.getSeparatorChar(context);
         ELContext elContext = context.getELContext();
         UIComponent vhComponent = valueHolderIterator.next();
         /* getValueExpressions - point for optimization */
         Set<String> propertyNames = getPropNames(vhComponent);
         boolean first = true;
 
         for (Iterator<String> propIterator = propertyNames.iterator(); propIterator.hasNext();) {
             String value;
             String prop = propIterator.next();
             ValueExpression ve = vhComponent.getValueExpression(prop);
 
             if (ve == null) continue; /* If component property isn't dynamic don't record its state */
 
             if (!first) detStr.append("|");
 
             if (vhComponent instanceof ValueHolder && "value".equals(prop)) {
                 value = BaseInputRenderer.getStringValueToRender(context, vhComponent);
             } else {
                 value = ve.getValue(elContext).toString();
             }
 
             // TOOD : More detailed conversion to string form
             // TODO : Detail id caching
             // TODO : '|' & '=' escaping
 
             // Write Target Id
             detStr.append(vhComponent.getClientId().replaceFirst(cId,"")).append("=");
             // Write Update Directive
             detStr.append(getDirective(vhComponent, prop)).append("=");
             // Write Update Value
             detStr.append(value); /* may change as directives evolve */
 
             first = false;
         }
 
         // If we wrote something and there are still more valueHolders
         if (!first && valueHolderIterator.hasNext()) detStr.append("|");
     }
 
     private static HashSet emptySet = new HashSet();
     private static HashSet mobiInputTextProperties = new HashSet() {{
         add("value"); add("type"); add("placeholder"); add("readonly"); add("maxlength");
         add("size"); add("required"); add("results"); add("title"); add("min"); add("max");
         add("step"); add("disabled"); add("style"); add("styleClass");
     }};
     private static HashSet uiCommandProperties = new HashSet() {{ add("value"); }};
     private static HashSet uiInputProperties = new HashSet() {{ add("value"); }};
     private static HashSet uiOutputProperties = new HashSet() {{ add("value"); }};
 
     private static List<Class> htmlValueHolders = new ArrayList<Class>() {{
         add(HtmlInputTextarea.class);
         add(HtmlOutputText.class);
         add(HtmlOutputLabel.class);
         add(HtmlCommandLink.class);
     }};
 
     private static List<Class> attrValueHolders = new ArrayList<Class>() {{
         add(HtmlInputText.class);
         add(HtmlInputSecret.class);
         add(HtmlInputText.class);
         add(HtmlCommandButton.class);
         add(InputText.class);
     }};
 
     private boolean instanceOf(Object x, List<Class> y) {
         Class xc = x.getClass();
 
         for (Class c : y)
             if (c.isInstance(x)) return true;
 
         return false;
     }
 
     private Set<String> getPropNames(UIComponent vhComponent) {
         if (vhComponent instanceof InputText) return mobiInputTextProperties;
         if (vhComponent instanceof UICommand) return uiCommandProperties;
         if (vhComponent instanceof UIInput) return uiInputProperties;
         if (vhComponent instanceof UIOutput) return uiOutputProperties;
 
         return emptySet;
     }
 
     private String getDirective(UIComponent c, String propertyName) {
         if (propertyName == "value") {
             if (instanceOf(c, htmlValueHolders) || isHtmlValueHolder(c)) {
                 return "html"; /* swap inner html */
             } else if (instanceOf(c, attrValueHolders)) {
                 return "attr=value";
             } else if (c instanceof HtmlSelectBooleanCheckbox) {
                 return "attr=checked";
             }
         }
 
         /* if no specified directive set as prop as dom attr */
         return "attr="+propertyName;
     }
 
     /* Check if component is an html value holder by configuration */
     private boolean isHtmlValueHolder(UIComponent c) {
         /* Only inspect specific component types */
         if (c instanceof InputText) {
             InputText inputText = (InputText)c;
             if (inputText.getType().equals("textarea"))
                 return true;
         }
 
         return false;
     }
 
     private void encodeDetails(FacesContext context,
                                ResponseWriter writer) throws IOException {
         DataViewDetails details = dataView.getDetails();
 
         // Init row context
         Integer index = dataView.getActiveRowIndex();
         String var = dataView.getVar();
         ActivationMode activeMode = dataView.getActivationMode();
        boolean active = ActivationMode.client.equals(activeMode) || (ActivationMode.server.equals(activeMode) && index != null && index >= 0);
         Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
 
         if (index != null) {
             DataViewDataModel dataModel = dataView.getDataModel();
             requestMap.put(var, dataModel.getDataByIndex(index));
         }
 
         // Write detail region
         writer.startElement(HTML.DIV_ELEM, null);
         writer.writeAttribute(HTML.ID_ATTR, dvId + "_det", null);
         writer.writeAttribute(HTML.CLASS_ATTR, IDataView.DATAVIEW_DETAIL_CLASS, null);
         writer.writeAttribute("data-index", index, null);
 
         if (details != null && active)
             details.encodeAll(context);
 
         writer.startElement(HTML.INPUT_ELEM, null);
         writer.writeAttribute(HTML.NAME_ATTR, dvId + "_active", null);
         writer.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_HIDDEN, null);
         if (index == null)
             writer.writeAttribute(HTML.VALUE_ATTR, "", null);
         else
             writer.writeAttribute(HTML.VALUE_ATTR, index, null);
         writer.endElement(HTML.INPUT_ELEM);
 
         writer.endElement(HTML.DIV_ELEM);
 
         requestMap.remove(var);
     }
 
     public boolean getRendersChildren() {
         return true;
     }
 
     private List<UIComponent> getDetailHolders(UIComponent component) {
         if (component.getChildCount() > 0) {
             ArrayList<UIComponent> valueHolders = new ArrayList<UIComponent>();
 
             for (UIComponent child : component.getChildren()) {
                 if (child instanceof ValueHolder) valueHolders.add(child);
                 if (child instanceof UICommand) valueHolders.add(child);
                 valueHolders.addAll(getDetailHolders(child));
             }
             return valueHolders;
         }
         else return Collections.emptyList();
     }
 }
