 /*
  * Copyright 2004-2007 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.teeda.extension.render.html;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.io.StringWriter;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.internal.IgnoreAttribute;
 
 import org.seasar.framework.beans.BeanDesc;
 import org.seasar.framework.beans.factory.BeanDescFactory;
 import org.seasar.framework.util.ResourceUtil;
 import org.seasar.framework.util.StringUtil;
 import org.seasar.teeda.core.JsfConstants;
 import org.seasar.teeda.core.context.html.HtmlResponseWriter;
 import org.seasar.teeda.core.scope.impl.DispatchScope;
 import org.seasar.teeda.core.scope.impl.DispatchScopeFactory;
 import org.seasar.teeda.core.util.RendererUtil;
 import org.seasar.teeda.extension.component.UIBody;
 import org.seasar.teeda.extension.component.html.THtmlGrid;
 import org.seasar.teeda.extension.component.html.THtmlGridBody;
 import org.seasar.teeda.extension.component.html.THtmlGridColumn;
 import org.seasar.teeda.extension.component.html.THtmlGridColumnGroup;
 import org.seasar.teeda.extension.component.html.THtmlGridHeader;
 import org.seasar.teeda.extension.component.html.THtmlGridTd;
 import org.seasar.teeda.extension.component.html.THtmlGridTh;
 import org.seasar.teeda.extension.component.html.THtmlGridTr;
 import org.seasar.teeda.extension.render.RenderPreparableRenderer;
 import org.seasar.teeda.extension.render.RendererListener;
 import org.seasar.teeda.extension.render.TBodyRenderer;
 import org.seasar.teeda.extension.render.TForEachRenderer;
 import org.seasar.teeda.extension.util.VirtualResource;
 
 /**
  * @author manhole
  * @author higa
  * @author shot
  */
 public class THtmlGridRenderer extends TForEachRenderer implements
         RenderPreparableRenderer {
 
     public static final String COMPONENT_FAMILY = THtmlGrid.COMPONENT_FAMILY;
 
     public static final String RENDERER_TYPE = THtmlGrid.DEFAULT_RENDERER_TYPE;
 
     private static final String GRID_TABLE_CLASS_NAME = "gridTable";
 
     private static final String GRID_HEADER_CLASS_NAME = "gridHeader";
 
     private static final String GRID_LEFT_HEADER_CLASS_NAME = "gridLeftHeader";
 
     private static final String GRID_RIGHT_HEADER_CLASS_NAME = "gridRightHeader";
 
     private static final String GRID_LEFT_BODY_CLASS_NAME = "gridLeftBody";
 
     private static final String GRID_RIGHT_BODY_CLASS_NAME = "gridRightBody";
 
     private static final String GRID_CELL_CLASS_NAME = "gridCell";
 
     private static final String LEFT_FIXED_CLASS_NAME = "T_leftFixed";
 
     private static final String LEFT_HEADER = "LeftHeader";
 
     private static final String LEFT_HEADER_TABLE = LEFT_HEADER + "Table";
 
     private static final String RIGHT_HEADER = "RightHeader";
 
     private static final String RIGHT_HEADER_TABLE = RIGHT_HEADER + "Table";
 
     private static final String RIGHT_BODY = "RightBody";
 
     private static final String RIGHT_BODY_TABLE = RIGHT_BODY + "Table";
 
     private static final String RIGHT_BODY_SOURCE = RIGHT_BODY + "Source";
 
     private static final String LEFT_BODY = "LeftBody";
 
     private static final String LEFT_BODY_TABLE = LEFT_BODY + "Table";
 
     private static final String LEFT_BODY_SOURCE = LEFT_BODY + "Source";
 
     private static final String GRID_ATTRIBUTE = THtmlGrid.class.getName()
             + ".GRID_ATTRIBUTE";
 
     private int firstRenderRowCount = 50;
 
     private final IgnoreAttribute gridIgnoreAttribute = new IgnoreAttribute();
     {
         gridIgnoreAttribute.addAttributeName(JsfConstants.ID_ATTR);
         gridIgnoreAttribute.addAttributeName(JsfConstants.BORDER_ATTR);
         gridIgnoreAttribute.addAttributeName(JsfConstants.CELLSPACING_ATTR);
         gridIgnoreAttribute.addAttributeName(JsfConstants.CELLPADDING_ATTR);
         gridIgnoreAttribute.addAttributeName(JsfConstants.WIDTH_ATTR);
         gridIgnoreAttribute.addAttributeName(JsfConstants.HEIGHT_ATTR);
         gridIgnoreAttribute.addAttributeName("scrollHorizontal");
         gridIgnoreAttribute.addAttributeName("scrollVertical");
         // forEach
         gridIgnoreAttribute.addAttributeName("indexName");
         gridIgnoreAttribute.addAttributeName("itemName");
         gridIgnoreAttribute.addAttributeName("itemsName");
         gridIgnoreAttribute.addAttributeName("pageName");
         gridIgnoreAttribute.addAttributeName("rowSize");
     }
 
     private final IgnoreAttribute thIgnoreAttribute = new IgnoreAttribute();
     {
         thIgnoreAttribute.addAttributeName(JsfConstants.STYLE_ATTR);
         thIgnoreAttribute.addAttributeName(JsfConstants.STYLE_CLASS_ATTR);
     }
 
     private final IgnoreAttribute tdIgnoreAttribute = new IgnoreAttribute();
     {
         tdIgnoreAttribute.addAttributeName(JsfConstants.STYLE_ATTR);
         tdIgnoreAttribute.addAttributeName(JsfConstants.STYLE_CLASS_ATTR);
     }
 
     public void encodePrepare(final FacesContext context,
             final UIComponent component) throws IOException {
         assertNotNull(context, component);
         if (!component.isRendered()) {
             return;
         }
         encodeHtmlGridPrepare(context, (THtmlGrid) component);
     }
 
     protected void encodeHtmlGridPrepare(final FacesContext context,
             final THtmlGrid htmlGrid) throws IOException {
         final String path = ResourceUtil.getResourcePath(THtmlGrid.class
                 .getName(), "js");
         VirtualResource.addJSResource(context, path);
     }
 
     public void encodeBegin(final FacesContext context,
             final UIComponent component) throws IOException {
         assertNotNull(context, component);
         if (!component.isRendered()) {
             return;
         }
         encodeHtmlGridBegin(context, (THtmlGrid) component);
     }
 
     protected void encodeHtmlGridBegin(final FacesContext context,
             final THtmlGrid htmlGrid) throws IOException {
         if (StringUtil.isBlank(htmlGrid.getId())) {
             throw new IllegalStateException(
                     "THtmlGrid should have 'id' attribute");
         }
         final ResponseWriter writer = context.getResponseWriter();
         writer.startElement(JsfConstants.TABLE_ELEM, htmlGrid);
         RendererUtil.renderIdAttributeIfNecessary(writer, htmlGrid,
                 getIdForRender(context, htmlGrid));
         RendererUtil.renderAttribute(writer, JsfConstants.BORDER_ATTR, "0");
         RendererUtil
                 .renderAttribute(writer, JsfConstants.CELLSPACING_ATTR, "0");
         RendererUtil
                 .renderAttribute(writer, JsfConstants.CELLPADDING_ATTR, "0");
         renderRemainAttributes(htmlGrid, writer, gridIgnoreAttribute);
     }
 
     public void encodeChildren(final FacesContext context,
             final UIComponent component) throws IOException {
         assertNotNull(context, component);
         if (!component.isRendered()) {
             return;
         }
         encodeHtmlGridChildren(context, (THtmlGrid) component);
     }
 
     protected void encodeHtmlGridChildren(final FacesContext context,
             final THtmlGrid htmlGrid) throws IOException {
         final ResponseWriter writer = context.getResponseWriter();
         final THtmlGridRenderer.GridAttribute attribute = getGridAttribute(
                 context, htmlGrid);
         for (final Iterator it = getRenderedChildrenIterator(htmlGrid); it
                 .hasNext();) {
             final UIComponent child = (UIComponent) it.next();
             if (child instanceof THtmlGridHeader) {
                 encodeGridHeader(context, htmlGrid, (THtmlGridHeader) child,
                         writer, attribute);
                 break;
             }
         }
         for (final Iterator it = getRenderedChildrenIterator(htmlGrid); it
                 .hasNext();) {
             final UIComponent child = (UIComponent) it.next();
             if (child instanceof THtmlGridBody) {
                 encodeGridBody(context, htmlGrid, (THtmlGridBody) child,
                         writer, attribute);
                 break;
             }
         }
     }
 
     public void encodeEnd(final FacesContext context,
             final UIComponent component) throws IOException {
         assertNotNull(context, component);
         if (!component.isRendered()) {
             return;
         }
         encodeHtmlGridEnd(context, (THtmlGrid) component);
     }
 
     protected void encodeHtmlGridEnd(final FacesContext context,
             final THtmlGrid htmlGrid) throws IOException {
         final ResponseWriter writer = context.getResponseWriter();
         writer.endElement(JsfConstants.TABLE_ELEM);
         final THtmlGridRenderer.GridAttribute attribute = getGridAttribute(
                 context, htmlGrid);
         encodeGridAdjustJavaScript(context, htmlGrid, writer, attribute);
     }
 
     private void encodeGridAdjustJavaScript(final FacesContext context,
             final THtmlGrid htmlGrid, final ResponseWriter writer,
             final GridAttribute attribute) throws IOException {
         final StringBuffer scriptBody = new StringBuffer(200);
         final String id = getIdForRender(context, htmlGrid);
         scriptBody.append("Teeda.THtmlGrid.adjustGridSize('" + id + "');");
 
         // 横方向にscrollするときは全体の横幅を自動調整する
         if (htmlGrid.isScrollHorizontal()) {
             scriptBody.append(" document.getElementById('" + id + RIGHT_HEADER
                    + "').style.width = " + attribute.getRightHeaderWidth()
                    + ";");
            scriptBody
                    .append(" document.getElementById('" + id + RIGHT_BODY
                            + "').style.width = "
                            + attribute.getRightBodyWidth() + ";");
         }
         //renderJavaScriptElement(writer, new String(scriptBody));
         renderJavaScriptElementOnBodyEnd(context, htmlGrid, scriptBody
                 .toString());
     }
 
     protected void renderJavaScriptElementOnBodyEnd(final FacesContext context,
             final THtmlGrid htmlGrid, final String scriptBody)
             throws IOException {
         if (StringUtil.isBlank(scriptBody)) {
             return;
         }
         final HtmlResponseWriter writer = new HtmlResponseWriter();
         writer.setWriter(new StringWriter());
         writer.write(JsfConstants.LINE_SP);
         writer.startElement(JsfConstants.SCRIPT_ELEM, null);
         writer.writeAttribute(JsfConstants.LANGUAGE_ATTR,
                 JsfConstants.JAVASCRIPT_VALUE, null);
         writer.writeAttribute(JsfConstants.TYPE_ATTR,
                 JsfConstants.TEXT_JAVASCRIPT_VALUE, null);
         writer.write(JsfConstants.LINE_SP);
         writer.write("<!--");
         writer.write(JsfConstants.LINE_SP);
         writer.write(scriptBody);
         writer.write(JsfConstants.LINE_SP);
         writer.write("//-->");
         writer.write(JsfConstants.LINE_SP);
         writer.endElement(JsfConstants.SCRIPT_ELEM);
 
         final UIBody body = TBodyRenderer.findParentBody(htmlGrid);
         final GridEndRendererListener listener = new GridEndRendererListener(
                 writer);
         TBodyRenderer.addRendererListener(body, listener);
     }
 
     private void encodeGridHeader(final FacesContext context,
             final THtmlGrid htmlGrid, final THtmlGridHeader header,
             final ResponseWriter writer,
             final THtmlGridRenderer.GridAttribute attribute) throws IOException {
         writer.startElement(JsfConstants.TR_ELEM, header);
         writer.startElement(JsfConstants.TD_ELEM, header);
 
         final String id = getIdForRender(context, htmlGrid);
         final GridRowContext rowContext = new GridRowContext();
 
         // encodeLeftHeader
         if (attribute.hasLeftFixCols()) {
             writer.startElement(JsfConstants.DIV_ELEM, header);
             RendererUtil.renderAttribute(writer, JsfConstants.ID_ATTR, id
                     + LEFT_HEADER);
             RendererUtil.renderAttribute(writer, JsfConstants.CLASS_ATTR,
                     GRID_LEFT_HEADER_CLASS_NAME);
 
             writer.startElement(JsfConstants.TABLE_ELEM, header);
             RendererUtil.renderAttribute(writer, JsfConstants.ID_ATTR, id
                     + LEFT_HEADER_TABLE);
             renderInnerTableAttributes(writer);
 
             // https://www.seasar.org/issues/browse/TEEDA-176
             writer.startElement(JsfConstants.THEAD_ELEM, header);
             for (final Iterator it = getRenderedChildrenIterator(header); it
                     .hasNext();) {
                 final UIComponent child = (UIComponent) it.next();
                 if (child instanceof THtmlGridTr) {
                     encodeGridLeftHeaderTr(context, (THtmlGridTr) child,
                             writer, attribute, rowContext);
                 }
             }
 
             writer.endElement(JsfConstants.THEAD_ELEM);
             writer.endElement(JsfConstants.TABLE_ELEM);
             writer.endElement(JsfConstants.DIV_ELEM);
             writer.endElement(JsfConstants.TD_ELEM);
 
             writer.startElement(JsfConstants.TD_ELEM, header);
         }
 
         // encodeRightHeader
         writer.startElement(JsfConstants.DIV_ELEM, header);
 
         RendererUtil.renderAttribute(writer, JsfConstants.ID_ATTR, id
                 + RIGHT_HEADER);
         RendererUtil.renderAttribute(writer, JsfConstants.CLASS_ATTR,
                 GRID_RIGHT_HEADER_CLASS_NAME);
         if (htmlGrid.isScrollHorizontal()) {
             RendererUtil.renderAttribute(writer, JsfConstants.STYLE_ATTR,
                     "overflow:hidden;");
         }
 
         writer.startElement(JsfConstants.TABLE_ELEM, header);
         RendererUtil.renderAttribute(writer, JsfConstants.ID_ATTR, id
                 + RIGHT_HEADER_TABLE);
         renderInnerTableAttributes(writer);
         rowContext.resetRow();
         writer.startElement(JsfConstants.THEAD_ELEM, header);
         for (final Iterator it = getRenderedChildrenIterator(header); it
                 .hasNext();) {
             final UIComponent child = (UIComponent) it.next();
             if (child instanceof THtmlGridTr) {
                 encodeGridRightHeaderTr(context, (THtmlGridTr) child, writer,
                         attribute, rowContext);
             }
         }
         writer.endElement(JsfConstants.THEAD_ELEM);
         writer.endElement(JsfConstants.TABLE_ELEM);
         writer.endElement(JsfConstants.DIV_ELEM);
         writer.endElement(JsfConstants.TD_ELEM);
         writer.endElement(JsfConstants.TR_ELEM);
     }
 
     private void encodeGridLeftHeaderTr(final FacesContext context,
             final THtmlGridTr tr, final ResponseWriter writer,
             final THtmlGridRenderer.GridAttribute attribute,
             final GridRowContext rowContext) throws IOException {
         rowContext.nextRow();
         int columnNo = rowContext.getCurrentRowSkipCols();
         if (attribute.getLeftFixCols() <= columnNo) {
             return;
         }
         writer.startElement(JsfConstants.TR_ELEM, tr);
         for (final Iterator it = getRenderedChildrenIterator(tr); it.hasNext();) {
             final UIComponent child = (UIComponent) it.next();
             if (child instanceof THtmlGridTh) {
                 columnNo++;
                 final THtmlGridTh htmlGridTh = (THtmlGridTh) child;
                 final int rowspan = getRowspanAsInt(htmlGridTh);
                 rowContext.addRowspan(rowspan);
                 encodeGridHeaderTh(context, htmlGridTh, writer, attribute,
                         columnNo);
             } else if (child instanceof THtmlGridTd) {
                 columnNo++;
                 final THtmlGridTd htmlGridTd = (THtmlGridTd) child;
                 final int rowspan = getRowspanAsInt(htmlGridTd);
                 rowContext.addRowspan(rowspan);
                 encodeGridHeaderTd(context, htmlGridTd, writer, attribute,
                         columnNo);
             }
             if (attribute.getLeftFixCols() <= columnNo) {
                 break;
             }
         }
         writer.endElement(JsfConstants.TR_ELEM);
     }
 
     private int getRowspanAsInt(final THtmlGridTh th) {
         final String s = th.getRowspan();
         if (StringUtil.isNotBlank(s)) {
             return Integer.parseInt(s);
         }
         return 1;
     }
 
     private int getRowspanAsInt(final THtmlGridTd td) {
         final String s = td.getRowspan();
         if (StringUtil.isNotBlank(s)) {
             return Integer.parseInt(s);
         }
         return 1;
     }
 
     private void encodeGridRightHeaderTr(final FacesContext context,
             final THtmlGridTr tr, final ResponseWriter writer,
             final THtmlGridRenderer.GridAttribute attribute,
             final GridRowContext rowContext) throws IOException {
         rowContext.nextRow();
         int columnNo = rowContext.getCurrentRowSkipCols();
         writer.startElement(JsfConstants.TR_ELEM, tr);
         for (final Iterator it = getRenderedChildrenIterator(tr); it.hasNext();) {
             final UIComponent child = (UIComponent) it.next();
             if (child instanceof THtmlGridTh) {
                 columnNo++;
                 if (columnNo <= attribute.getLeftFixCols()) {
                     continue;
                 }
                 encodeGridHeaderTh(context, (THtmlGridTh) child, writer,
                         attribute, columnNo);
             } else if (child instanceof THtmlGridTd) {
                 columnNo++;
                 if (columnNo <= attribute.getLeftFixCols()) {
                     continue;
                 }
                 encodeGridHeaderTd(context, (THtmlGridTd) child, writer,
                         attribute, columnNo);
             }
         }
         writer.endElement(JsfConstants.TR_ELEM);
     }
 
     private void encodeGridHeaderTd(final FacesContext context,
             final THtmlGridTd td, final ResponseWriter writer,
             final THtmlGridRenderer.GridAttribute attribute, final int columnNo)
             throws IOException {
         writer.startElement(JsfConstants.TD_ELEM, td);
         writer.startElement(JsfConstants.NOBR_ELEM, td);
         encodeDescendantComponent(context, td);
         writer.endElement(JsfConstants.NOBR_ELEM);
         writer.endElement(JsfConstants.TD_ELEM);
     }
 
     private void encodeGridHeaderTh(final FacesContext context,
             final THtmlGridTh th, final ResponseWriter writer,
             final THtmlGridRenderer.GridAttribute attribute, final int columnNo)
             throws IOException {
         writer.startElement(JsfConstants.TH_ELEM, th);
         RendererUtil.renderIdAttributeIfNecessary(writer, th, getIdForRender(
                 context, th));
         renderRemainAttributes(th, writer, thIgnoreAttribute);
         writer.startElement(JsfConstants.DIV_ELEM, th);
         RendererUtil.renderAttribute(writer, JsfConstants.CLASS_ATTR,
                 createThStyleClassAttribute(th, attribute, columnNo));
         RendererUtil.renderAttribute(writer, JsfConstants.STYLE_ATTR,
                 createThStyleAttribute(th, attribute, columnNo));
 
         writer.startElement(JsfConstants.NOBR_ELEM, th);
         encodeDescendantComponent(context, th);
 
         writer.endElement(JsfConstants.NOBR_ELEM);
         writer.endElement(JsfConstants.DIV_ELEM);
         writer.endElement(JsfConstants.TH_ELEM);
     }
 
     private String createThStyleClassAttribute(final THtmlGridTh th,
             final GridAttribute attribute, final int columnNo) {
         final StringBuffer sb = new StringBuffer(50);
         sb.append(GRID_HEADER_CLASS_NAME);
         final String styleClass = th.getStyleClass();
         if (StringUtil.isNotBlank(styleClass)) {
             sb.append(" ");
             sb.append(styleClass);
         }
         return new String(sb);
     }
 
     private String createThStyleAttribute(final THtmlGridTh th,
             final GridAttribute attribute, final int columnNo) {
         final Integer columnWidth = attribute.getColumnWidth(columnNo);
         final StringBuffer sb = new StringBuffer(50);
         sb.append("overflow:hidden;");
         if (columnWidth != null) {
             sb.append(" width:" + columnWidth + "px;");
         }
         final String style = th.getStyle();
         if (StringUtil.isNotBlank(style)) {
             sb.append(" ");
             sb.append(style);
         }
         return new String(sb);
     }
 
     private void encodeGridBody(final FacesContext context,
             final THtmlGrid htmlGrid, final THtmlGridBody body,
             final ResponseWriter writer,
             final THtmlGridRenderer.GridAttribute attribute) throws IOException {
         writer.startElement(JsfConstants.TR_ELEM, body);
         writer.startElement(JsfConstants.TD_ELEM, body);
         RendererUtil.renderAttribute(writer, JsfConstants.STYLE_ATTR,
                 "vertical-align:top;");
 
         final Object page = htmlGrid.getPage(context);
         final BeanDesc beanDesc = BeanDescFactory.getBeanDesc(page.getClass());
         final Object[] items = htmlGrid.getItems(context);
         // TEEDA-150
         final int rowSize = Math.min(firstRenderRowCount,
                 ((items != null) ? items.length : 0));
 
         final String id = getIdForRender(context, htmlGrid);
         // https://www.seasar.org/issues/browse/TEEDA-176
         GridRowContext rowContext = null;
 
         // encodeLeftBody
         if (attribute.hasLeftFixCols()) {
             writer.startElement(JsfConstants.DIV_ELEM, body);
             RendererUtil.renderAttribute(writer, JsfConstants.ID_ATTR, id
                     + LEFT_BODY);
             RendererUtil.renderAttribute(writer, JsfConstants.CLASS_ATTR,
                     GRID_LEFT_BODY_CLASS_NAME);
             if (htmlGrid.isScrollVertical()) {
                 RendererUtil.renderAttribute(writer, JsfConstants.STYLE_ATTR,
                         "overflow:hidden; height:"
                                 + attribute.getLeftBodyHeight() + "px;");
             }
 
             writer.startElement(JsfConstants.TABLE_ELEM, body);
             RendererUtil.renderAttribute(writer, JsfConstants.ID_ATTR, id
                     + LEFT_BODY_TABLE);
             renderInnerTableAttributes(writer);
 
             writer.startElement(JsfConstants.TBODY_ELEM, body);
             for (int i = 0; i < rowSize; ++i) {
                 htmlGrid.enterRow(context, i);
                 if (i < items.length) {
                     htmlGrid.processItem(beanDesc, page, items[i], i);
                 }
                 final GridRowContext row = new GridRowContext();
                 for (final Iterator it = getRenderedChildrenIterator(body); it
                         .hasNext();) {
                     final UIComponent child = (UIComponent) it.next();
                     if (child instanceof THtmlGridTr) {
                         encodeGridLeftBodyTr(context, (THtmlGridTr) child,
                                 writer, attribute, row);
                     }
                 }
                 // 1つだけ取っておく
                 if (rowContext == null) {
                     rowContext = row;
                 }
                 htmlGrid.leaveRow(context);
             }
             writer.endElement(JsfConstants.TBODY_ELEM);
             writer.endElement(JsfConstants.TABLE_ELEM);
             writer.endElement(JsfConstants.DIV_ELEM);
             writer.endElement(JsfConstants.TD_ELEM);
 
             writer.startElement(JsfConstants.TD_ELEM, body);
             RendererUtil.renderAttribute(writer, JsfConstants.STYLE_ATTR,
                     "vertical-align:top;");
         }
         if (rowContext == null) {
             rowContext = new GridRowContext();
         }
 
         // encodeRightBody
         writer.startElement(JsfConstants.DIV_ELEM, body);
         RendererUtil.renderAttribute(writer, JsfConstants.ID_ATTR, id
                 + RIGHT_BODY);
         RendererUtil.renderAttribute(writer, JsfConstants.CLASS_ATTR,
                 GRID_RIGHT_BODY_CLASS_NAME);
         if (htmlGrid.isScrollHorizontal() && htmlGrid.isScrollVertical()) {
             RendererUtil.renderAttribute(writer, JsfConstants.STYLE_ATTR,
                     "overflow:scroll; height:" + attribute.getRightBodyHeight()
                             + "px;");
             String onscroll = "document.getElementById('" + id
                     + "RightHeader').scrollLeft=this.scrollLeft;";
             if (attribute.hasLeftFixCols()) {
                 onscroll = onscroll + " document.getElementById('" + id
                         + "LeftBody').scrollTop=this.scrollTop;";
             }
             RendererUtil.renderAttribute(writer, JsfConstants.ONSCROLL_ATTR,
                     onscroll);
         } else if (htmlGrid.isScrollHorizontal()) {
             RendererUtil.renderAttribute(writer, JsfConstants.STYLE_ATTR,
                     "overflow-x:scroll;");
             RendererUtil.renderAttribute(writer, JsfConstants.ONSCROLL_ATTR,
                     "document.getElementById('" + id
                             + "RightHeader').scrollLeft=this.scrollLeft;");
         } else if (htmlGrid.isScrollVertical()) {
             RendererUtil.renderAttribute(writer, JsfConstants.STYLE_ATTR,
                     "overflow-y:scroll; height:"
                             + attribute.getRightBodyHeight() + "px;");
             if (attribute.hasLeftFixCols()) {
                 RendererUtil.renderAttribute(writer,
                         JsfConstants.ONSCROLL_ATTR, "document.getElementById('"
                                 + id + "LeftBody').scrollTop=this.scrollTop;");
             }
         }
 
         writer.startElement(JsfConstants.TABLE_ELEM, body);
         RendererUtil.renderAttribute(writer, JsfConstants.ID_ATTR, id
                 + RIGHT_BODY_TABLE);
         renderInnerTableAttributes(writer);
         writer.startElement(JsfConstants.TBODY_ELEM, body);
 
         for (int i = 0; i < rowSize; ++i) {
             htmlGrid.enterRow(context, i);
             if (i < items.length) {
                 htmlGrid.processItem(beanDesc, page, items[i], i);
             }
             rowContext.resetRow();
             for (final Iterator it = getRenderedChildrenIterator(body); it
                     .hasNext();) {
                 final UIComponent child = (UIComponent) it.next();
                 if (child instanceof THtmlGridTr) {
                     encodeGridRightBodyTr(context, (THtmlGridTr) child, writer,
                             attribute, rowContext);
                 }
             }
             htmlGrid.leaveRow(context);
         }
 
         writer.endElement(JsfConstants.TBODY_ELEM);
         writer.endElement(JsfConstants.TABLE_ELEM);
         writer.endElement(JsfConstants.DIV_ELEM);
         writer.endElement(JsfConstants.TD_ELEM);
         writer.endElement(JsfConstants.TR_ELEM);
 
         if (rowSize < items.length) {
             final UIBody uiBody = TBodyRenderer.findParentBody(htmlGrid);
             final GridPostRendererListener rendererListener = new GridPostRendererListener();
             rendererListener.attribute = attribute;
             rendererListener.htmlGrid = htmlGrid;
             rendererListener.id = id;
             rendererListener.body = body;
             rendererListener.renderRowLength = rowSize;
             rendererListener.beanDesc = beanDesc;
             rendererListener.page = page;
             rendererListener.items = items;
             TBodyRenderer.addRendererListener(uiBody, rendererListener);
         }
     }
 
     private void encodeGridLeftBodyTr(final FacesContext context,
             final THtmlGridTr tr, final ResponseWriter writer,
             final THtmlGridRenderer.GridAttribute attribute,
             final GridRowContext rowContext) throws IOException {
         rowContext.nextRow();
         int columnNo = rowContext.getCurrentRowSkipCols();
         if (attribute.getLeftFixCols() <= columnNo) {
             return;
         }
         writer.startElement(JsfConstants.TR_ELEM, tr);
         renderStyleClass(tr, writer);
         renderStyle(tr, writer);
         for (final Iterator it = getRenderedChildrenIterator(tr); it.hasNext();) {
             final UIComponent child = (UIComponent) it.next();
             if (child instanceof THtmlGridTd) {
                 columnNo++;
                 final THtmlGridTd htmlGridTd = (THtmlGridTd) child;
                 final int rowspan = getRowspanAsInt(htmlGridTd);
                 rowContext.addRowspan(rowspan);
                 encodeGridBodyTd(context, htmlGridTd, writer, attribute,
                         columnNo);
             }
             if (attribute.getLeftFixCols() <= columnNo) {
                 break;
             }
         }
         writer.endElement(JsfConstants.TR_ELEM);
     }
 
     private void encodeGridRightBodyTr(final FacesContext context,
             final THtmlGridTr tr, final ResponseWriter writer,
             final THtmlGridRenderer.GridAttribute attribute,
             final GridRowContext rowContext) throws IOException {
         rowContext.nextRow();
         int columnNo = rowContext.getCurrentRowSkipCols();
         writer.startElement(JsfConstants.TR_ELEM, tr);
         renderStyleClass(tr, writer);
         renderStyle(tr, writer);
         for (final Iterator it = getRenderedChildrenIterator(tr); it.hasNext();) {
             final UIComponent child = (UIComponent) it.next();
             if (child instanceof THtmlGridTd) {
                 columnNo++;
                 if (columnNo <= attribute.getLeftFixCols()) {
                     continue;
                 }
                 encodeGridBodyTd(context, (THtmlGridTd) child, writer,
                         attribute, columnNo);
             }
         }
         writer.endElement(JsfConstants.TR_ELEM);
     }
 
     private void encodeGridBodyTd(final FacesContext context,
             final THtmlGridTd td, final ResponseWriter writer,
             final THtmlGridRenderer.GridAttribute attribute, final int columnNo)
             throws IOException {
         writer.startElement(JsfConstants.TD_ELEM, td);
         renderRemainAttributes(td, writer, tdIgnoreAttribute);
         writer.startElement(JsfConstants.DIV_ELEM, td);
         writer.writeAttribute(JsfConstants.CLASS_ATTR,
                 createTdStyleClassAttribute(td), JsfConstants.CLASS_ATTR);
         writer.writeAttribute(JsfConstants.STYLE_ATTR, createTdStyleAttribute(
                 td, attribute, columnNo), JsfConstants.STYLE_ATTR);
         writer.startElement(JsfConstants.NOBR_ELEM, td);
 
         encodeDescendantComponent(context, td);
 
         writer.endElement(JsfConstants.NOBR_ELEM);
         writer.endElement(JsfConstants.DIV_ELEM);
         writer.endElement(JsfConstants.TD_ELEM);
     }
 
     private String createTdStyleClassAttribute(final THtmlGridTd td) {
         final String styleClass = td.getStyleClass();
         if (StringUtil.isBlank(styleClass)) {
             return GRID_CELL_CLASS_NAME;
         }
         return GRID_CELL_CLASS_NAME + " " + styleClass;
     }
 
     private String createTdStyleAttribute(final THtmlGridTd td,
             final THtmlGridRenderer.GridAttribute attribute, final int columnNo) {
         final String style = td.getStyle();
         final String s = "overflow:hidden;";
         if (StringUtil.isBlank(style)) {
             return s;
         }
         return s + " " + style;
     }
 
     private THtmlGridRenderer.GridAttribute getGridAttribute(
             final FacesContext context, final THtmlGrid htmlGrid) {
         final DispatchScope dispatchScope = DispatchScopeFactory
                 .getDispatchScope();
         final String key = GRID_ATTRIBUTE + htmlGrid.getClientId(context);
         THtmlGridRenderer.GridAttribute attribute = (GridAttribute) dispatchScope
                 .get(key);
         if (attribute != null) {
             return attribute;
         }
         attribute = new GridAttribute(htmlGrid);
         setupTableSize(htmlGrid, attribute);
         setupWidth(htmlGrid, attribute);
         setupHeight(htmlGrid, attribute);
         dispatchScope.put(key, attribute);
         return attribute;
     }
 
     private void setupTableSize(final THtmlGrid htmlGrid,
             final THtmlGridRenderer.GridAttribute attribute) {
         final String width = htmlGrid.getWidth();
         if (width != null) {
             attribute.setTableWidth(toDigit(width));
         }
         final String height = htmlGrid.getHeight();
         if (height != null) {
             attribute.setTableHeight(toDigit(height));
         }
     }
 
     private void setupWidth(final THtmlGrid htmlGrid,
             final THtmlGridRenderer.GridAttribute attribute) {
         int columnNo = 0;
         for (final Iterator itGrid = getRenderedChildrenIterator(htmlGrid); itGrid
                 .hasNext();) {
             final UIComponent child = (UIComponent) itGrid.next();
             if (child instanceof THtmlGridColumnGroup) {
                 final THtmlGridColumnGroup columnGroup = (THtmlGridColumnGroup) child;
                 for (final Iterator itColGroup = getRenderedChildrenIterator(columnGroup); itColGroup
                         .hasNext();) {
                     final UIComponent element = (UIComponent) itColGroup.next();
                     if (element instanceof THtmlGridColumn) {
                         final THtmlGridColumn col = (THtmlGridColumn) element;
                         // TODO check
                         final int span = Integer.parseInt(col.getSpan());
                         final String styleClass = col.getStyleClass();
                         if (StringUtil.contains(styleClass,
                                 LEFT_FIXED_CLASS_NAME)) {
                             attribute.setLeftFixCols(span
                                     + attribute.getLeftFixCols());
                         }
                         final String widthStr = col.getWidth();
                         if (widthStr == null) {
                             columnNo += span;
                         } else {
                             final int width = toDigit(widthStr);
                             if (StringUtil.contains(styleClass,
                                     LEFT_FIXED_CLASS_NAME)) {
                                 // TODO check
                                 attribute.setLeftWidth(width * span
                                         + attribute.getLeftWidth());
                             }
                             for (int i = 0; i < span; i++) {
                                 columnNo++;
                                 attribute.setColumnWidth(columnNo, new Integer(
                                         width));
                             }
                         }
                     }
                 }
             }
         }
     }
 
     private void setupHeight(final THtmlGrid htmlGrid,
             final THtmlGridRenderer.GridAttribute attribute) {
         for (final Iterator itGrid = getRenderedChildrenIterator(htmlGrid); itGrid
                 .hasNext();) {
             final UIComponent gridChild = (UIComponent) itGrid.next();
             if (gridChild instanceof THtmlGridHeader) {
                 for (final Iterator itHeader = getRenderedChildrenIterator(gridChild); itHeader
                         .hasNext();) {
                     final UIComponent headerChild = (UIComponent) itHeader
                             .next();
                     if (headerChild instanceof THtmlGridTr) {
                         final THtmlGridTr tr = (THtmlGridTr) headerChild;
                         final String height = tr.getHeight();
                         if (height != null) {
                             attribute.setHeaderHeight(toDigit(height)
                                     + attribute.getHeaderHeight());
                         }
                     }
                 }
             }
         }
     }
 
     private int toDigit(String s) {
         if (s.toLowerCase().endsWith("px")) {
             s = s.substring(0, s.length() - 2);
         }
         return Integer.parseInt(s);
     }
 
     public boolean getRendersChildren() {
         return true;
     }
 
     private void renderInnerTableAttributes(final ResponseWriter writer)
             throws IOException {
         RendererUtil
                 .renderAttribute(writer, JsfConstants.CELLSPACING_ATTR, "0");
         RendererUtil
                 .renderAttribute(writer, JsfConstants.CELLPADDING_ATTR, "0");
         RendererUtil.renderAttribute(writer, JsfConstants.CLASS_ATTR,
                 GRID_TABLE_CLASS_NAME);
     }
 
     private void renderStyleClass(final THtmlGridTr tr,
             final ResponseWriter writer) throws IOException {
         final String styleClass = tr.getStyleClass();
         if (styleClass != null) {
             RendererUtil.renderAttribute(writer, JsfConstants.STYLE_CLASS_ATTR,
                     styleClass);
         }
     }
 
     private void renderStyle(final THtmlGridTr tr, final ResponseWriter writer)
             throws IOException {
         final String style = tr.getStyle();
         if (style != null) {
             RendererUtil
                     .renderAttribute(writer, JsfConstants.STYLE_ATTR, style);
         }
     }
 
     public void setFirstRenderRowCount(final int firstRenderRowCount) {
         this.firstRenderRowCount = firstRenderRowCount;
     }
 
     private static class GridAttribute implements Serializable {
 
         private static final long serialVersionUID = 1L;
 
         // TODO Windows XP default...
         private int scrollBarWidth = 16;
 
         private int tableWidth;
 
         private int tableHeight;
 
         private int headerHeight;
 
         private int leftFixCols;
 
         private final Map columnWidths = new HashMap();
 
         private THtmlGrid htmlGrid;
 
         public GridAttribute(final THtmlGrid htmlGrid) {
             this.htmlGrid = htmlGrid;
         }
 
         public Integer getColumnWidth(final int no) {
             return (Integer) columnWidths.get(new Integer(no));
         }
 
         public void setColumnWidth(final int no, final Integer width) {
             columnWidths.put(new Integer(no), width);
         }
 
         public boolean hasLeftFixCols() {
             return 0 < leftFixCols;
         }
 
         private int leftWidth;
 
         public int getHeaderHeight() {
             return headerHeight;
         }
 
         public void setHeaderHeight(final int headerHeight) {
             this.headerHeight = headerHeight;
         }
 
         public int getLeftBodyHeight() {
             if (htmlGrid.isScrollHorizontal()) {
                 return tableHeight - headerHeight - scrollBarWidth;
             }
             return tableHeight - headerHeight;
         }
 
         public int getLeftFixCols() {
             return leftFixCols;
         }
 
         public void setLeftFixCols(final int leftFixCols) {
             this.leftFixCols = leftFixCols;
         }
 
         public int getLeftWidth() {
             return leftWidth;
         }
 
         public void setLeftWidth(final int leftWidth) {
             this.leftWidth = leftWidth;
         }
 
         public int getRightBodyHeight() {
             return tableHeight - headerHeight;
         }
 
         public int getRightBodyWidth() {
             return tableWidth - leftWidth;
         }
 
         public int getRightHeaderWidth() {
             if (htmlGrid.isScrollVertical()) {
                 return tableWidth - leftWidth - scrollBarWidth;
             }
             return tableWidth - leftWidth;
         }
 
         public int getScrollBarWidth() {
             return scrollBarWidth;
         }
 
         public void setScrollBarWidth(final int scrollBarWidth) {
             this.scrollBarWidth = scrollBarWidth;
         }
 
         public int getTableHeight() {
             return tableHeight;
         }
 
         public void setTableHeight(final int tableHeight) {
             this.tableHeight = tableHeight;
         }
 
         public int getTableWidth() {
             return tableWidth;
         }
 
         public void setTableWidth(final int tableWidth) {
             this.tableWidth = tableWidth;
         }
 
     }
 
     class GridPostRendererListener implements RendererListener {
 
         GridAttribute attribute;
 
         THtmlGrid htmlGrid;
 
         String id;
 
         THtmlGridBody body;
 
         int renderRowLength;
 
         BeanDesc beanDesc;
 
         Object page;
 
         Object[] items;
 
         public void renderBeforeBodyEnd(final FacesContext context)
                 throws IOException {
             final ResponseWriter writer = context.getResponseWriter();
             // https://www.seasar.org/issues/browse/TEEDA-176
             GridRowContext rowContext = null;
 
             // leftBody
             if (attribute.hasLeftFixCols()) {
                 writer.startElement(JsfConstants.TABLE_ELEM, body);
                 RendererUtil.renderAttribute(writer, JsfConstants.ID_ATTR, id
                         + LEFT_BODY_SOURCE);
                 RendererUtil.renderAttribute(writer, JsfConstants.STYLE_ATTR,
                         "display:none;");
 
                 writer.startElement(JsfConstants.TBODY_ELEM, body);
                 for (int i = renderRowLength; i < items.length; ++i) {
                     htmlGrid.enterRow(context, i);
                     htmlGrid.processItem(beanDesc, page, items[i], i);
                     final GridRowContext row = new GridRowContext();
                     for (final Iterator it = getRenderedChildrenIterator(body); it
                             .hasNext();) {
                         final UIComponent child = (UIComponent) it.next();
                         if (child instanceof THtmlGridTr) {
                             encodeGridLeftBodyTr(context, (THtmlGridTr) child,
                                     writer, attribute, row);
                         }
                     }
                     // 1つだけ取っておく
                     if (rowContext == null) {
                         rowContext = row;
                     }
                     htmlGrid.leaveRow(context);
                 }
                 writer.endElement(JsfConstants.TBODY_ELEM);
                 writer.endElement(JsfConstants.TABLE_ELEM);
             }
             if (rowContext == null) {
                 rowContext = new GridRowContext();
             }
 
             // rightBody
             writer.startElement(JsfConstants.TABLE_ELEM, body);
             RendererUtil.renderAttribute(writer, JsfConstants.ID_ATTR, id
                     + RIGHT_BODY_SOURCE);
             RendererUtil.renderAttribute(writer, JsfConstants.STYLE_ATTR,
                     "display:none;");
             writer.startElement(JsfConstants.TBODY_ELEM, body);
             for (int i = renderRowLength; i < items.length; ++i) {
                 htmlGrid.enterRow(context, i);
                 htmlGrid.processItem(beanDesc, page, items[i], i);
                 rowContext.resetRow();
                 for (final Iterator it = getRenderedChildrenIterator(body); it
                         .hasNext();) {
                     final UIComponent child = (UIComponent) it.next();
                     if (child instanceof THtmlGridTr) {
                         encodeGridRightBodyTr(context, (THtmlGridTr) child,
                                 writer, attribute, rowContext);
                     }
                 }
                 htmlGrid.leaveRow(context);
             }
             writer.endElement(JsfConstants.TBODY_ELEM);
             writer.endElement(JsfConstants.TABLE_ELEM);
             renderJavaScriptElement(writer, "Teeda.THtmlGrid.loadGridRows('"
                     + id + "');");
         }
     }
 
     class GridEndRendererListener implements RendererListener {
 
         private HtmlResponseWriter partialWriter;
 
         public GridEndRendererListener(final HtmlResponseWriter partialWriter) {
             this.partialWriter = partialWriter;
         }
 
         public void renderBeforeBodyEnd(final FacesContext context)
                 throws IOException {
             final ResponseWriter responseWriter = context.getResponseWriter();
             responseWriter.write(partialWriter.toString());
         }
 
     }
 
     static class GridRowContext {
 
         /*
          * 行番号とrowspanによってskipされる列数を持つ。
          */
         private final Map skipCols = new HashMap();
 
         private int currentRow = -1;
 
         private static final Integer ONE = new Integer(1);
 
         /*
          * 次の行へ処理を進める
          */
         public void nextRow() {
             currentRow++;
         }
 
         public void resetRow() {
             currentRow = -1;
         }
 
         public int getCurrentRowSkipCols() {
             final Integer i = (Integer) skipCols.get(new Integer(currentRow));
             if (i != null) {
                 return i.intValue();
             }
             return 0;
         }
 
         /*
          * rowspanがあったら、次以降の行のskip数をカウントアップする。
          */
         public void addRowspan(final int rowspan) {
             for (int i = 1; i < rowspan; i++) {
                 final Integer targetRow = new Integer(currentRow + i);
                 final Integer o = (Integer) skipCols.get(targetRow);
                 if (o == null) {
                     skipCols.put(targetRow, ONE);
                 } else {
                     skipCols.put(targetRow, new Integer(o.intValue() + 1));
                 }
             }
         }
     }
 
 }
