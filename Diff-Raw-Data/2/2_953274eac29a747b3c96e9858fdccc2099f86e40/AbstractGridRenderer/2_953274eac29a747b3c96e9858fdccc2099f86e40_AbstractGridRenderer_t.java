 /**
  * License Agreement.
  *
  *  JBoss RichFaces - Ajax4jsf Component Library
  *
  * Copyright (C) 2007  Exadel, Inc.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License version 2.1 as published by the Free Software Foundation.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
  */
 
 package org.richfaces.renderkit;
 
 import java.io.IOException;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 
 import org.ajax4jsf.component.UIDataAdaptor;
 import org.ajax4jsf.renderkit.RendererUtils.HTML;
 import org.richfaces.component.UIDataGrid;
 
 /**
  * @author shura
  *
  */
 public abstract class AbstractGridRenderer extends AbstractRowsRenderer {
 	
 		
 	public void encodeHeader( FacesContext context,UIDataGrid table) throws IOException {
 		ResponseWriter writer = context.getResponseWriter();
 		UIComponent header = table.getHeader();
 		if(header != null ){
 	           writer.startElement("thead", table);
 	           String headerClass = (String) table.getAttributes().get("headerClass");
 	           encodeTableHeaderFacet(context, table.getColumns(), writer, header, "rich-table-header","rich-table-header-continue", "rich-table-headercell", headerClass, "th");
                writer.endElement("thead");
 		}
 	}
 
 	public void encodeFooter( FacesContext context,UIDataGrid table) throws IOException {
 		ResponseWriter writer = context.getResponseWriter();
 		UIComponent footer = table.getFooter();
 		if(footer != null ){
 	           writer.startElement("tfoot", table);
 	           String footerClass = (String) table.getAttributes().get("footerClass");
 	           encodeTableHeaderFacet(context, table.getColumns(), writer, footer, "rich-table-footer","rich-table-footer-continue", "rich-table-footercell", footerClass, "td");
                writer.endElement("tfoot");
 		}
 		
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see org.richfaces.renderkit.AbstractRowsRenderer#encodeOneRow(javax.faces.context.FacesContext, org.richfaces.renderkit.AbstractRowsRenderer.TableHolder)
 	 */
 	public void encodeOneRow(FacesContext context, TableHolder holder) throws IOException {
 		UIDataAdaptor table = holder.getTable();
 		ResponseWriter writer = context.getResponseWriter();
 		int currentRow = holder.getRowCounter();
 		Integer columns = (Integer) table.getAttributes().get("columns");
 		int cols = (null!=columns && columns.intValue()!=Integer.MIN_VALUE)?columns.intValue():1;
 		
 		if (columns.intValue() == 0) {
 			cols = 1;
 		}
 		
 		int gridRowCounter = holder.getGridRowCounter();
 		if ( cols >0 && currentRow%cols==0) {
 			if (currentRow != 0 ) {
 				writer.endElement(HTML.TR_ELEMENT);
 				holder.setGridRowCounter(++gridRowCounter);
 			}
 
 			writer.startElement(HTML.TR_ELEMENT, table);
 			String rowClass = holder.getRowClass(gridRowCounter);
 			encodeStyleClass(writer, null, "rich-table-row", null, rowClass);
 			encodeRowEvents(context, table);
 			
 		}
 		writer.startElement(HTML.td_ELEM, table);
 		getUtils().encodeId(context, table);
 		String columnClass = holder.getColumnClass(currentRow-gridRowCounter*cols);
 		encodeStyleClass(writer, null, "rich-table-cell", null, columnClass);
 		renderChildren(context, table);
 		writer.endElement(HTML.td_ELEM);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.richfaces.renderkit.AbstractRowsRenderer#doCleanup(org.richfaces.renderkit.TableHolder)
 	 */
 	protected void doCleanup(FacesContext context, TableHolder tableHolder) throws IOException {
 		int rest = 0;
 		UIDataAdaptor table = tableHolder.getTable();
 		ResponseWriter writer = context.getResponseWriter();
 		Integer columns = (Integer) table.getAttributes().get("columns");
 		
 		boolean isCleanable = (null!=columns && columns.intValue()!=Integer.MIN_VALUE)? true:false;  
 		int cols = isCleanable ? columns.intValue():1;
 		
 		rest = tableHolder.getRowCounter()-tableHolder.getGridRowCounter()*cols;
 		if(rest != 0){
 			for(int i = rest;i<columns.intValue();i++){
 				writer.startElement(HTML.td_ELEM, table);
 				String columnClass = tableHolder.getColumnClass(i);
 				encodeStyleClass(writer, null, "rich-table-cell", null, columnClass);
				writer.write("&#160;");// &nbsp;
 				writer.endElement(HTML.td_ELEM);				
 			}
 		}	
 		
 		if(rest != 0){
 			writer.endElement(HTML.TR_ELEMENT);
 		}	
 	}
 	
 }
