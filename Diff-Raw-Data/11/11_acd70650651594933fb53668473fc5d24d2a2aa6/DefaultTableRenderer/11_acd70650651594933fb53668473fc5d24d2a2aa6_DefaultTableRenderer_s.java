 /*
  * Copyright 2005-2007 jWic group (http://www.jwic.de)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * de.jwic.ecolib.tableviewer.DefaultTableRenderer
  * Created on 12.03.2007
  * $Id: DefaultTableRenderer.java,v 1.23 2012/08/17 09:25:14 adrianionescu12 Exp $
  */
 package de.jwic.controls.tableviewer;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.PrintWriter;
 import java.io.Serializable;
 import java.util.Iterator;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import de.jwic.base.Control;
 import de.jwic.base.Field;
 import de.jwic.base.IControlRenderer;
 import de.jwic.base.ImageRef;
 import de.jwic.base.JWicRuntime;
 import de.jwic.base.RenderContext;
 import de.jwic.data.IContentProvider;
 import de.jwic.data.Range;
 
 /**
  * Default implementation of the ITableRenderer interface. The default renderer
  * generates HTML "by code", without using templates. The reason for this is 
  * because its faster, costs less resources and the generated HTML is quite
  * complex so that templates would be quite ugly anyway.
  * 
  * @author Florian Lippisch
  */
 public class DefaultTableRenderer implements ITableRenderer, Serializable {
 

 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	/** default icon used for sort-up image */
 	public final static ImageRef ICON_SORTUP = new ImageRef("/jwic/gfx/sortup.gif");
 	/** default icon used for sort-down image */
 	public final static ImageRef ICON_SORTDOWN = new ImageRef("/jwic/gfx/sortdn.gif"); 
 	/** default icon used for expand image */
 	public final static ImageRef ICON_EXPAND = new ImageRef("/jwic/gfx/expand.png");
 	/** default icon used for collapse image */
 	public final static ImageRef ICON_COLLAPSE = new ImageRef("/jwic/gfx/collapse.png"); 
 	/** default icon used for indention */
 	public final static ImageRef ICON_CLEAR = new ImageRef("/jwic/gfx/clear.gif"); 
 
 	protected transient Log log = LogFactory.getLog(getClass()); 
 	
 	private boolean expandLinkSpacing = true;
 	private int expandIconWidth = 19;
 	private int expandIconHeight = 16;
 	
 	/* (non-Javadoc)
 	 * @see de.jwic.ecolib.tableviewer.ITableRenderer#renderTable(de.jwic.base.RenderContext, de.jwic.ecolib.tableviewer.TableViewer, de.jwic.ecolib.tableviewer.TableModel)
 	 */
 	public void renderTable(RenderContext renderContext, TableViewer viewer, TableModel model, ITableLabelProvider labelProvider) {
 		
 		// make sure to load the TableViewer library
 		renderContext.addRequiredJSContent(TableViewer.class.getName().replace('.', '/') + ".static.js");
 		
 		String tblvGfxPath = JWicRuntime.getJWicRuntime().getContextPath() + "/jwic/gfx/";
 		
 		PrintWriter writer = renderContext.getWriter();
 		
 		IContentProvider<?> contentProvider = model.getContentProvider();
 		
 		int tblWidth = 0;
 		for (Iterator<TableColumn> it = model.getColumnIterator(); it.hasNext(); ) {
 			TableColumn tc = it.next();
 			if (!tc.isVisible()) {
 				continue;
 			}
 			tblWidth += tc.getWidth();
 		}
 		
 		// Add resizer div 
 //		if (viewer.isResizeableColumns()) {
 //			writer.print("<DIV id=\"tblViewResizer_" + viewer.getControlID() + "\" class=\"tblViewResizer\" ");
 //			/*writer.print("onMouseUp=\"tblViewer_resizeColumnDone()\"" +
 //					" onMouseMove=\"tblViewer_resizeColumMove()\"");*/
 //			writer.print(" style=\"height: " + (viewer.getHeight() != 0 ? viewer.getHeight() : 20) + "px\"");
 //			writer.println("></DIV>");
 //		}
 		
 		// render main table.
 		writer.print("<table cellspacing=\"0\" cellpadding=\"0\" class=\"" + viewer.getCssClass() + "\"");
 		if (viewer.isFillWidth()) {
 			writer.print(" width=\"100%\"");
 		} else {
 			if (viewer.getWidth() != 0) {
 				writer.print(" width=\"" + viewer.getWidth() + "\"");
 			}
 			// FLI: the table does not need a height...
 			/*if (viewer.getHeight() != 0) {
 				writer.print(" height=\"" + viewer.getHeight() + "\"");
 			}*/
 		}
 		writer.println(">");
 
 		
 		writer.print("<tr><td>");
 		
 		// render data table.
 		String divHeight = viewer.getHeight() != 0 ? 
 				(viewer.isShowStatusBar() ? viewer.getHeight() - 18 : viewer.getHeight() ) + "px" 
 				: "100%";
 		String divWidth = viewer.getWidth() != 0 ? viewer.getWidth() + "px" : "100%";
 		
 		// main table content DIV
 		writer.print("<div class=\"tblContent\"");
 		writer.print(" id=\"tblContent_" + viewer.getControlID() + "\"");
 		writer.print(" style=\"height: " + divHeight + "; width: " + divWidth + "; overflow: hidden\">");
 		
 		if (viewer.isScrollable() && viewer.isShowHeader()) {
 			int tmpWidth = viewer.getWidth() != 0 ? viewer.getWidth() : 300;
 			writer.print("<DIV id=\"tblViewHead_" + viewer.getControlID() + "\"");
 			writer.print("style=\"width: " + tmpWidth + "px; ");
 			writer.print("height: 20px; overflow: hidden;");
 			writer.print("\">");
 		}
 		
 		// create required table attributes.
 		StringBuffer sbTblSelAttrs = new StringBuffer();
 		switch (model.getSelectionMode()) {
 		case TableModel.SELECTION_SINGLE: {
 			String clearKey = model.getFirstSelectedKey();
 			if (clearKey == null) {
 				clearKey = "";
 			}
 			sbTblSelAttrs.append(" tbvSelKey=\"" + clearKey + "\"");
 			sbTblSelAttrs.append(" tbvSelMode=\"single\"");
 			break;
 		}
 		case TableModel.SELECTION_MULTI: {
 			sbTblSelAttrs.append(" tbvSelKey=\"\"");
 			sbTblSelAttrs.append(" tbvSelMode=\"multi\"");
 			break;
 		}
 		default: {
 			sbTblSelAttrs.append(" tbvSelKey=\"\"");
 			sbTblSelAttrs.append(" tbvSelMode=\"none\"");
 		}
 		}
 		
 		if (viewer.isShowHeader() || !viewer.isScrollable()) {
 		
 			writer.print("<table");
 			writer.print(" tbvctrlid=\"" + viewer.getControlID() + "\"");
 			writer.print(" id=\"tblViewData_" + viewer.getControlID() + "\"");
 			writer.print(" class=\"tblData\" cellspacing=\"0\" cellpadding=\"0\" ");
 			if (viewer.isScrollable()) {
 				// must add a width attribute, otherwise table-layout: fixed isnt working on Mozilla
 				writer.print(" width=\"" + tblWidth + "\" ");
 			}
 			writer.print(sbTblSelAttrs);
 			writer.println(">");
 		}
 		
 		// render HEADER columns
 		if (viewer.isShowHeader()) {
 			renderHeader(writer, model, viewer, tblvGfxPath);
 		}		
 		if (viewer.isScrollable()) {
 			// if scrollable, seperate the data table from the header
 			// and render it within its own, scrollable DIV. Will only
 			// look proper if the columns have a fixed width.
 			
 			int dataHeight = viewer.getHeight() != 0 ? viewer.getHeight() - (viewer.isShowHeader() ? 20 : 0): 200;
 			if (viewer.isShowStatusBar()) {
 				dataHeight -= 18;
 			}
 			int dataWidth = viewer.getWidth() != 0 ? viewer.getWidth() : 300;
 			if (viewer.isShowHeader()) {
 				writer.println("</TABLE></DIV>");
 			}
 			writer.print("<DIV onscroll=\"JWic.controls.TableViewer.handleScroll(event, '" + viewer.getControlID() + "')\" style=\"");
 			writer.print("width: " + dataWidth + "px; height: " + dataHeight + "px; overflow: auto;");
 			writer.print("\" id=\"tblViewDataLayer_" + viewer.getControlID() + "\"");
 			writer.println(">");
 			writer.print("<table");
 			writer.print(" tbvctrlid=\"" + viewer.getControlID() + "\"");
 			writer.print(" id=\"tblViewDataTbl_" + viewer.getControlID() + "\"");
 			writer.print(" class=\"tblData\" cellspacing=\"0\" cellpadding=\"0\" width=\"" + tblWidth + "\"");			
 			writer.print(sbTblSelAttrs);
 			writer.println(">");
 		}
 		
 		Range range = model.getRange();
 		if (range.getMax() == 0) { // = Auto
 			int max = -1;
 			int rowSpace = viewer.getHeight() - (35 + 20); // - header height + scroll bar height
 			if (rowSpace != 0) {
 				if (viewer.isShowStatusBar()) {
 					rowSpace -= 18;
 				}
 				max = rowSpace / viewer.getRowHeightHint(); //
 				if (max < 1) {
 					max = 1;
 				}
 				model.setLastRenderedPageSize(max);
 			}
 			range = new Range(range.getStart(), max);
 		}
 		
 		// render table BODY
 		writer.println("<TBODY>");
 		try {
 			int count = renderRows(0, false, writer, contentProvider.getContentIterator(range), viewer, labelProvider);
 			
 			if (count == 0) {
 				// if no rows exist, then print an empty row just to preserve the scrolling of the header
 				renderEmptyRow(writer, viewer);
 			}
 			
 			model.setLastRowRenderCount(count);
 		} catch (Exception e) {
 			writer.println("Error reading data from ContentProvider: " + e);
 			log.error("Error reading data from ContentProvider", e);
 		}
 		
 		writer.println("</TBODY>");
 		writer.println("</table>");
 		if (viewer.isScrollable()) {
 			writer.println("</DIV>");
 		}
 		writer.println("</div></td></tr>");
 		
 		// render STATUS BAR
 		if (viewer.isShowStatusBar()) {
 			writer.println("<tr><td>");
 			// render context
 			Control sb = viewer.getControl("statusBar");
 			IControlRenderer renderer = JWicRuntime.getRenderer(sb.getRendererId());
 			renderer.renderControl(sb, renderContext);
 			writer.print("</td></tr>");
 		}
 		
 		writer.println("</table>");
 		
 		if (viewer.isScrollable()) {
 			// add scroll fields
 			Field fldLeft = viewer.getField("left");
 			Field fldTop = viewer.getField("top");
 			writer.println("<INPUT TYPE=\"HIDDEN\" NAME=\"" + fldLeft.getId() + "\" VALUE=\"" + fldLeft.getValue() + "\">");
 			writer.println("<INPUT TYPE=\"HIDDEN\" NAME=\"" + fldTop.getId() + "\" VALUE=\"" + fldTop.getValue() + "\">");
 
 			writer.println("<script language=\"javascript\">");
 			writer.println("window.setTimeout(\"JWic.restoreScrolling('" + viewer.getControlID() + "', 'tblViewDataLayer_" + viewer.getControlID() + "');\", 0);");
 			writer.println("</script>");
 		}
 		
 	}
 	/**
 	 * 
 	 */
 	protected void renderHeader(PrintWriter writer, TableModel model, TableViewer viewer, String tblGfxPath) {
 		
 		boolean isResizable = viewer.isResizeableColumns() && viewer.isEnabled();
 		boolean isColSelectable = viewer.isSelectableColumns() && viewer.isEnabled();
 		
 		writer.println("<THEAD>");
 		
 		writer.println("<tr>");
 		for (Iterator<TableColumn> itC = model.getColumnIterator(); itC.hasNext(); ) {
 			TableColumn column = itC.next();
 			
 			if (!column.isVisible()) {
 				continue;
 			}
 
 			if (isResizable && column.getWidth() == 0) {
 				// must set a default width if resizeable columns is activated
 				column.setWidth(150);
 			}
 			
 			writer.print("<th");
 			int innerWidth = 0;
 			if (column.getWidth() > 0) {
 				writer.print(" width=\"" + column.getWidth() + "\"");
 				innerWidth = column.getWidth() - (viewer.isResizeableColumns() ? 5 : 0);
 				innerWidth = innerWidth - (column.getSortIcon() != TableColumn.SORT_ICON_NONE ? 8 : 0);
 				if (innerWidth < 3) {
 					innerWidth = 3;
 				}
 			}
 			writer.print(" colIdx=\"" + column.getIndex() + "\"");
 			
             //header tooltip
             if (column.getToolTip() != null && column.getToolTip().length() > 0) {
                 writer.print(" title=\"" + column.getToolTip() + "\"");
             }
 		
 			writer.println(">");
 			// create cell table
 			writer.print("<TABLE class=\"tbvColHeader\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\"><TR>");
 			writer.print("<TD class=\"tbvColHeadCell\" width=\"" + innerWidth + "\"");
 			if (isColSelectable) {
 				writer.print(" onClick=\"JWic.fireAction('" + viewer.getControlID() + "', 'columnSelection', '" + column.getIndex() + "')\"");
 				writer.print(" onMouseDown=\"JWic.controls.TableViewer.pushColumn(" + column.getIndex() + ", '" + viewer.getControlID() + "')\"");
 				writer.print(" onMouseUp=\"JWic.controls.TableViewer.releaseColumn()\"");
 				writer.print(" onMouseOut=\"JWic.controls.TableViewer.releaseColumn()\"");
 			}
 			writer.print(">");
 			writer.print("<NOBR>");
 			if (column.getImage() != null) {
 				writer.print("<IMG SRC=\"" + column.getImage().getPath() + "\" border=0/>");
 			}
 			writer.print(column.getTitle());
 			writer.print("</NOBR>");
 			writer.print("</TD>");
 			if (column.getSortIcon() != TableColumn.SORT_ICON_NONE) {
 				ImageRef imgSort = null;
 				switch (column.getSortIcon()) {
 				case TableColumn.SORT_ICON_UP:
 					imgSort = ICON_SORTUP;
 					break;
 				case TableColumn.SORT_ICON_DOWN:
 					imgSort = ICON_SORTDOWN;
 					break;
 				}
 				if (imgSort != null) {
 					writer.print("<TD class=\"tbvColHeadCell\" width=\"8\">");
 					writer.print("<IMG SRC=\"" + imgSort.getPath() + "\" border=0>");
 					writer.print("</TD>");
 				}
 			}
 			if (isResizable) {
 				writer.print("<TD class=\"tbvColHeadCellPoint\" width=\"3\"><IMG SRC=\"" + tblGfxPath + "resizer.gif\" width=\"3\" height=\"13\"");
 				writer.print(" colIdx=\"" + column.getIndex() + "\"");
				writer.print(" onMouseDown=\"JWic.controls.TableViewer.resizeColumn(event, '" + viewer.getControlID() + "')\" class=\"tblResize\" border=0>");
 				writer.print("</TD>");
 			}
 			writer.print("</TR></TABLE>");
 			writer.println("</th>");
 		}
 		
 		// if the width is fixed, we must render an empty column at the end so that the
 		// browser will not adjust the columns width
 		if (viewer.getWidth() != 0) {
 			if (viewer.isScrollable()) {
 				writer.println("<TH width=\"" + viewer.getWidth() + "\">&nbsp;</TH>");
 			} else {
 				writer.println("<TH>&nbsp;</TH>");
 			}
 		}
 		
 		writer.println("</tr>");
 		writer.println("</THEAD>");
 		
 	}
 
 	/**
 	 * @param writer
 	 * @param rootIterator
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	protected int renderRows(int level, boolean hasNext, PrintWriter writer, Iterator<?> it, TableViewer viewer, ITableLabelProvider labelProvider) {
 		
 		TableModel model = viewer.getModel();
 		IContentProvider contentProvider = model.getContentProvider();
 		
 		int count = 0;
 		while(it.hasNext()) {
 			Object row = it.next();
 			count++;
 			String key = contentProvider.getUniqueKey(row) ;
 			boolean expanded = model.isExpanded(key);
 			writer.print("<tr");
 			String rowCssClass = "";
 			if (!(it.hasNext() || hasNext || contentProvider.hasChildren(row))) {
 				rowCssClass="lastRow";
 			}
 
 			// handle selection
 			writer.print(" tbvRowKey=\"" + key + "\"");
 			if (model.getSelectionMode() != TableModel.SELECTION_NONE) {
 				if (viewer.isEnabled()) {
 					writer.print(" onClick=\"JWic.controls.TableViewer.clickRow(this, event)\"");
 					writer.print(" onDblClick=\"JWic.controls.TableViewer.clickRow(this, event, true)\"");
 				}
 				if (model.isSelected(key)) {
 					rowCssClass = rowCssClass + "selected";
 				}
 			}
 			if (rowCssClass.length() != 0) {
 				writer.print(" class=\"" + rowCssClass + "\"");
 			}
 			writer.println(">");
 			for (Iterator<TableColumn> itC = model.getColumnIterator(); itC.hasNext(); ) {
 				TableColumn column = itC.next();
 				if (!column.isVisible()) {
 					continue;
 				}
 
 				CellLabel cell = labelProvider.getCellLabel(row, column, new RowContext(expanded, level));
 				
 				writer.print("<td");
 				if (column.getWidth() > 0) {
 					writer.print(" width=\"" + column.getWidth() + "\"");
 				}
 				if (cell.cssClass != null) {
 					writer.print(" class=\"" + cell.cssClass + "\"");
 				}
                 if (cell.toolTip != null && cell.toolTip.length() > 0) {
                     writer.print(" title=\"" + cell.toolTip + "\"");
                 }
                 if (cell.colspan != null && cell.colspan.intValue() > 0) {
                 	writer.print(" colspan=\"" + cell.colspan + "\"");
                 }
 
 				writer.print(">");
 				
 				boolean innerTable = column.getIndex() == viewer.getExpandableColumn() || 
 										cell.image != null && cell.text != null;
 				
 				if (innerTable) {
 					writer.print("<table class=\"inner\" cellspacing=0 cellpadding=0 border=0><tr><td>");
 				}
 				// handle exp/collapse
 				if (column.getIndex() == viewer.getExpandableColumn()) {
 					// indention
 					for (int i = 0; i < level; i++) {
 						writer.print(ICON_CLEAR.toImgTag(expandIconWidth, expandIconHeight));
 					}
 					if (contentProvider.hasChildren(row)) {
 						if (viewer.isEnabled()) {
 							writer.print("<a href=\"#\" onClick=\"return ");
 							writer.print(expanded ? "trV_Collapse(event)" : "trV_Expand(event)");
 							writer.print("\";return false;\">");
 							writer.print((expanded ? ICON_COLLAPSE : ICON_EXPAND).toImgTag(expandIconWidth, expandIconHeight));
 							writer.print("</A>");
 						} else {
 							writer.print((expanded ? ICON_COLLAPSE : ICON_EXPAND).toImgTag(expandIconWidth, expandIconHeight));
 						}
 					} else {
 						if (expandLinkSpacing) {
 							writer.print(ICON_CLEAR.toImgTag(expandIconWidth, expandIconHeight));
 						}
 					}
 					writer.print("</td><td class=\"content\">");
 				}
 				
 				// print cell value
 				if (cell.image != null) {
 					writer.print(cell.image.toImgTag());
 					if (cell.text != null) {
 						writer.print("</td><td class=\"content\">");
 					}
 				}
 				if (cell.text != null) {
 					writer.print("<NOBR>");
 					if (cell.text.length() == 0 && cell.image == null) {
 						writer.print("&nbsp;");
 					} else {
 						writer.print(cell.text);
 					}
 					writer.print("</NOBR>");
 				}
 				if (innerTable) {
 					writer.print("</td></tr></table>");
 				}				
 				writer.println("</td>");
 				
 			}
 			// if its a fixed width, must render an empty column that fills up the space.
 			if (viewer.getWidth() != 0) {
 				writer.println("<TD>&nbsp;</TD>");
 			}
 			writer.println(" </tr>");
 			
 			if (contentProvider.hasChildren(row) && expanded) {
 				renderRows(level + 1, hasNext || it.hasNext(), writer, contentProvider.getChildren(row), viewer, labelProvider);
 			}
 			
 		}
 		return count;
 	}
 	
 	/**
 	 * @param writer
 	 * @param viewer
 	 */
 	protected void renderEmptyRow(PrintWriter writer, TableViewer viewer) {		
 		writer.print("<tr class=\"lastRow\">");
 		writer.print("<td colspan=\"" + (viewer.getModel().getColumnsCount() - 1) + "\" style=\"font-style: italic\">");
 		writer.print("no rows available");
 		writer.println("</td></tr>");
 	}
 	
 	/**
 	 * @return the expandLinkSpacing
 	 */
 	public boolean isExpandLinkSpacing() {
 		return expandLinkSpacing;
 	}
 	
 	/**
 	 * @param expandLinkSpacing the expandLinkSpacing to set
 	 */
 	public void setExpandLinkSpacing(boolean expandLinkSpacing) {
 		this.expandLinkSpacing = expandLinkSpacing;
 	}
 	
 	/**
 	 * @param expandIconHeight the expandIconHeight to set
 	 */
 	public void setExpandIconHeight(int expandIconHeight) {
 		this.expandIconHeight = expandIconHeight;
 	}
 	
 	/**
 	 * @return the expandIconHeight
 	 */
 	public int getExpandIconHeight() {
 		return expandIconHeight;
 	}
 	
 	/**
 	 * @param expandIconWidth the expandIconWidth to set
 	 */
 	public void setExpandIconWidth(int expandIconWidth) {
 		this.expandIconWidth = expandIconWidth;
 	}
 	
 	/**
 	 * @return the expandIconWidth
 	 */
 	public int getExpandIconWidth() {
 		return expandIconWidth;
 	}
 	/**
 	 * Get new logger after deserialization.
 	 * @param s
 	 * @throws IOException
 	 */
 	private void readObject(ObjectInputStream s) throws IOException  {
 		try {
 			s.defaultReadObject();
 		} catch (ClassNotFoundException e) {
 			throw new IOException("ClassNotFound in readObject");
 		}
 		log = LogFactory.getLog(getClass());
 	}
 
 }
