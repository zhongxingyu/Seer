 package com.xaf.report;
 
 /**
  * Title:        The Extensible Application Platform
  * Description:
  * Copyright:    Copyright (c) 2001
  * Company:      Netspective Communications Corporation
  * @author Shahid N. Shah
  * @version 1.0
  */
 
 import java.io.*;
 import java.util.*;
 import java.sql.*;
 import java.net.URLEncoder;
 
 import org.w3c.dom.*;
 import com.xaf.form.*;
 import com.xaf.form.field.*;
 import com.xaf.report.column.*;
 import com.xaf.value.*;
 
 public class StandardReport implements Report
 {
     static public final int REPORTFLAG_INITIALIZED      = 1;
     static public final int REPORTFLAG_HASPLACEHOLDERS = REPORTFLAG_INITIALIZED * 2;
 	static public final int REPORTFLAG_FIRST_DATA_ROW_HAS_HEADINGS = REPORTFLAG_HASPLACEHOLDERS * 2;
 
 	private Object canvas;
     private String name;
 	private ReportColumnsList columns = new ReportColumnsList();
 	private boolean contentsFinalized;
     private ReportFrame frame = null;
     private ReportBanner banner = null;
 	private int visibleColsCount = -1;
     private int flags;
 
     public StandardReport()
     {
     }
 
 	public Object getCanvas() { return canvas; }
 	public void setCanvas(Object value) { canvas = value; }
 
     public String getName() { return name; }
 	public ReportFrame getFrame() { return frame; }
     public void setFrame(ReportFrame rf) { frame = rf; }
 	public ReportBanner getBanner() { return banner; }
 
 	public ReportColumnsList getColumns() { return columns; }
 	public ReportColumn getColumn(int i) { return columns.getColumn(i); }
 
 	public final long getFlags() { return flags; }
 	public final boolean flagIsSet(long flag) { return (flags & flag) == 0 ? false : true; }
 	public final void setFlag(long flag) { flags |= flag; }
 	public final void clearFlag(long flag) { flags &= ~flag; }
 	public final void updateFlag(long flag, boolean set) { if(set) flags |= flag; else flags &= ~flag; }
 
 	public void initialize(ResultSet rs, Element defnElem) throws SQLException
 	{
 		if(flagIsSet(REPORTFLAG_INITIALIZED)) return;
 
 		ResultSetMetaData rsmd = rs.getMetaData();
 		int numColumns = rsmd.getColumnCount();
 
 		columns.clear();
 		for(int c = 1; c <= numColumns; c++)
 		{
 			ReportColumn colDefn = ReportColumnFactory.createReportColumn(rsmd, c);
             columns.add(colDefn);
 		}
 
 		if(defnElem != null)
 			importFromXml(defnElem);
 
 		finalizeContents();
 		setFlag(REPORTFLAG_INITIALIZED);
 	}
 
     public void initialize(ReportColumn[] cols, Element defnElem)
     {
         if(cols != null)
         {
             for(int c = 0; c <= cols.length; c++)
                 columns.add(cols[c]);
         }
 
 		if(defnElem != null)
 			importFromXml(defnElem);
 
 		finalizeContents();
     }
 
 	public void finalizeContents()
 	{
 		for(int c = 0; c < columns.size(); c++)
 		{
 			ReportColumn colDefn = columns.getColumn(c);
 			colDefn.finalizeContents(this);
 
             if(colDefn.flagIsSet(ReportColumn.COLFLAG_HASPLACEHOLDERS))
                 setFlag(this.REPORTFLAG_HASPLACEHOLDERS);
 		}
 	}
 
 	public void importFromXml(Element elem)
 	{
 		name = elem.getAttribute("name");
 		if(name.length() == 0)
 			name = "default";
 
 		String heading = elem.getAttribute("heading");
 		if(heading.length() > 0)
 		{
 			if(frame == null) frame = new ReportFrame();
 			frame.setHeading(heading);
 		}
 
 		if(elem.getAttribute("first-row").equals("column-headings"))
 			setFlag(REPORTFLAG_FIRST_DATA_ROW_HAS_HEADINGS);
 
 		NodeList children = elem.getChildNodes();
 		int columnIndex = 0;
 
 		for(int n = 0; n < children.getLength(); n++)
 		{
 			Node node = children.item(n);
 			if(node.getNodeType() != Node.ELEMENT_NODE)
 				continue;
 
 			String childName = node.getNodeName();
 			if(childName.equals("column"))
 			{
 				Element columnElem = (Element) node;
 
 				String value = columnElem.getAttribute("index");
 				if(value.length() > 0)
 					columnIndex = Integer.parseInt(value);
 
                 String colType = columnElem.getAttribute("type");
                 if(colType.length() == 0)
                     colType = null;
 
                 if(columns.size() <= columnIndex)
                 {
                     ReportColumn column = ReportColumnFactory.createReportColumn(colType);
                     column.importFromXml(columnElem);
                     column.setColIndexInArray(columnIndex);
 					if(column instanceof DialogFieldColumn)
 						((DialogFieldColumn) column).setParentField((ReportField) canvas);
                     columns.add(column);
                 }
                 else
                 {
     				ReportColumn column = null;
 					if(colType == null)
 						column = columns.getColumn(columnIndex);
 					else
 					{
 						column = ReportColumnFactory.createReportColumn(colType);
 	                    column.setColIndexInArray(columnIndex);
 						if(column instanceof DialogFieldColumn)
 							((DialogFieldColumn) column).setParentField((ReportField) canvas);
 						columns.set(columnIndex, column);
 					}
 	    			column.importFromXml(columnElem);
                 }
 
 				columnIndex++;
 			}
             else if(childName.equals("banner-item"))
             {
 				Element bannerItemElem = (Element) node;
 
 				String caption = bannerItemElem.getAttribute("caption");
 				String url = bannerItemElem.getAttribute("url");
 
                 if(banner == null)
                     banner = new ReportBanner();
 
                 banner.addItem(new ReportBanner.Item(caption, url));
             }
 		}
 	}
 
 	/**
 	 * Replace contents from rowData using the String row as a template. Each
 	 * occurrence of ${#} will be replaced with rowNum and occurrences of ${x}
 	 * where x is a number between 0 and rowData.length will be replaced with
 	 * the contents of rowData[x]. NOTE: this function needs to be
 	 * improved from both an elegance and performance perspective.
 	 */
 
     public String replaceOutputPatterns(ReportContext rc, long rowNum, Object[] rowData, String row)
     {
         StringBuffer sb = new StringBuffer();
         int i = 0;
         int prev = 0;
         boolean encode = false;
 
         int pos = 0;
         int pos1 = row.indexOf("$", prev);
         int pos2 = row.indexOf("%", prev);
         if (pos2 != -1)
         {
             if (pos1 != -1 && pos2 > pos1)
             {
                 pos = pos1;
             }
             else
             {
                 encode = true;
                 pos = pos2;
             }
         }
         else
         {
             encode = false;
             pos = pos1;
         }
 
         while(pos  >= 0)
 		{
             if(pos>0)
 			{
                 // append the substring before the '$' or '%' character
                 sb.append(row.substring( prev, pos ));
             }
             if( pos == (row.length() - 1))
 			{
                 if (encode)
                     sb.append('%');
                 else
                     sb.append('$');
                 prev = pos + 1;
             }
             else if (row.charAt( pos + 1 ) != '{')
 			{
                sb.append(row.charAt(pos));
                 sb.append(row.charAt(pos + 1));
                 prev=pos+2;
             }
 			else
 			{
                 int endName=row.indexOf('}', pos);
                 if( endName < 0 )
 				{
                     throw new RuntimeException("Syntax error in: " + row);
                 }
                 String expression = row.substring(pos+2, endName);
 
 				if(expression.equals("#"))
 					sb.append(rowNum);
 				else
 				{
 					try
 					{
 						int colIndex = Integer.parseInt(expression);
                         if (encode)
 						    sb.append(URLEncoder.encode(columns.getColumn(colIndex).getFormattedData(rc, rowNum, rowData, false)));
                         else
                             sb.append(columns.getColumn(colIndex).getFormattedData(rc, rowNum, rowData, false));
 					}
 					catch(NumberFormatException e)
 					{
 						SingleValueSource vs = ValueSourceFactory.getSingleValueSource(expression);
 						if(vs == null)
 							sb.append("Invalid: '"+expression+"'");
 						else
 							sb.append(vs.getValue(rc));
 					}
 				}
 
                 prev=endName+1;
             }
 
             pos1 = row.indexOf("$", prev);
             pos2 = row.indexOf("%", prev);
             if (pos2 != -1)
             {
                 if (pos1 != -1 && pos2 > pos1)
                 {
                     pos = pos1;
                 }
                 else
                 {
                     encode = true;
                     pos = pos2;
                 }
             }
             else
             {
                 encode = false;
                 pos = pos1;
             }
         }
 
         if(prev < row.length()) sb.append(row.substring(prev));
         return sb.toString();
     }
 
 	public void makeStateChanges(ReportContext rc, ResultSet rs)
 	{
 		List listeners = rc.getListeners();
 		for(int i = 0; i < listeners.size(); i++)
 			((ReportContextListener) listeners.get(i)).makeReportStateChanges(rc, rs);
 	}
 
 	public void makeStateChanges(ReportContext rc, Object[][] data)
 	{
 		List listeners = rc.getListeners();
 		for(int i = 0; i < listeners.size(); i++)
 			((ReportContextListener) listeners.get(i)).makeReportStateChanges(rc, data);
 	}
 }
