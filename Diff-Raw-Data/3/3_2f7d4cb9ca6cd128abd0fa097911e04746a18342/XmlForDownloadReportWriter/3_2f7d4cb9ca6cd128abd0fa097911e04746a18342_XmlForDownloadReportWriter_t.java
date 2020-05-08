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
 
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * As for XML output, but forces download of the output.
  */
 public class XmlForDownloadReportWriter extends XmlReportWriter {
 
 	/**
 	 * Create an instance of this writer for writing the given report to the given HTTP Servlet
 	 * @param pw the place to send output.
 	 * @param columns a list of column definitions.
 	 */
 	public XmlForDownloadReportWriter(PrintWriter pw,List<TabularReportBase.ColumnDefinition>columns) {
 		super(pw,columns);
 	}
 
 	/* (non-Javadoc)
 	 * @see om.tnavigator.reports.TabularReportWriter#sendHeaders(javax.servlet.http.HttpServletResponse)
 	 */
 	@Override
 	public void sendHeaders(HttpServletResponse response, String batchid) {
 		super.sendHeaders(response, batchid);
		response.setHeader("Content-Disposition", "attachment; filename=report-"+
				batchid+".xml");
 	}
 }
