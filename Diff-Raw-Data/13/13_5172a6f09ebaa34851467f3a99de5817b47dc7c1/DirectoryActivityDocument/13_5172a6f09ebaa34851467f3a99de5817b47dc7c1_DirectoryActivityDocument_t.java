 /*
     StatCvs - CVS statistics generation 
     Copyright (C) 2002  Lukasz Pekacki <lukasz@pekacki.de>
     http://statcvs.sf.net/
     
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU Lesser General Public
     License as published by the Free Software Foundation; either
     version 2.1 of the License, or (at your option) any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     Lesser General Public License for more details.
 
     You should have received a copy of the GNU Lesser General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
     
 	$RCSfile: DirectoryActivityDocument.java,v $ 
	Created on $Date: 2003-07-04 13:03:07 $ 
 */
 package net.sf.statcvs.output.xml;
 
 import net.sf.statcvs.I18n;
 import net.sf.statcvs.model.CvsContent;
import net.sf.statcvs.output.xml.chart.AbstractChart;
 import net.sf.statcvs.output.xml.report.CvsCharts;
 
 /**
  * DirectoryActivityDocument
  * 
  * @author Tammo van Lessen
  */
 public class DirectoryActivityDocument extends StatCvsDocument {
 
 	private CvsCharts charts;
 
 	/**
 	 * @param filename
 	 */
 	public DirectoryActivityDocument(CvsContent content) {
 		super(I18n.tr("Module Activity"), "dir_activity");
 		charts = new CvsCharts(content);
 		getRootElement().addContent(new DirectoryActivityReport());
 	}
 
 	private class DirectoryActivityReport extends ReportElement {
 
 		public DirectoryActivityReport() {
 			super(I18n.tr("Module Activity"));
 			addContent(new ChartElement(charts.getModuleActivityChart()));
 		}
 		
 	}
	
	/**
	 * @see net.sf.statcvs.output.xml.StatCvsDocument#getCharts()
	 */
	public AbstractChart[] getCharts() {
		return new AbstractChart[] {charts.getModuleActivityChart()};
	}

 }
