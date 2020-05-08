 /* OpenMark online assessment system
    Copyright (C) 2007 The Open University
 
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package om.tnavigator.reports;
 
 import java.io.PrintWriter;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * A report writer for writing reports as XML. The document element will be
  * <results batch="[batchnumber]">, and each row will be wrapped in a <result> tag.
  */
 public class XmlReportWriter extends TabularReportWriter {
 
 	/**
 	 * Create an instance of this writer for writing the given report to the given HTTP Servlet
	 * @param pw the place to send output.
 	 * @param report the report we will be responsible for outputting.
 	 * @param columns a list of column definitions.
 	 */
 	public XmlReportWriter(PrintWriter pw,List<TabularReportBase.ColumnDefinition>columns) {
 		super(pw,columns);
 	}
 
 	/* (non-Javadoc)
 	 * @see om.tnavigator.reports.TabularReportWriter#sendHead()
 	 */
 	@Override
 	public void printHead(String batchid, String title, TabularReportBase report) {
 		pw.println("<results batch=\"" + batchid + "\">");
 	}
 
 	/* (non-Javadoc)
 	 * @see om.tnavigator.reports.TabularReportWriter#sendHeaders(javax.servlet.http.HttpServletResponse)
 	 */
 	@Override
 	public void sendHeaders(HttpServletResponse response, String batchid) {
 		response.setContentType("application/xml");
 		response.setCharacterEncoding("UTF-8");
 	}
 
 	/* (non-Javadoc)
 	 * @see om.tnavigator.reports.TabularReportWriter#sendRow(java.util.Map)
 	 */
 	@Override
 	public void printRow(Map<String, String> data) {
 		pw.println("\t<result>");
 		for (TabularReportBase.ColumnDefinition column : columns)
 		{
 			pw.println("\t\t<"+column.id+">"+data.get(column.id)+"</"+column.id+">");
 		}
 		pw.println("\t</result>");
 	}
 
 	/* (non-Javadoc)
 	 * @see om.tnavigator.reports.TabularReportWriter#sendTail()
 	 */
 	@Override
 	public void printTail() {
 		pw.println("</results>");
 	}
 }
