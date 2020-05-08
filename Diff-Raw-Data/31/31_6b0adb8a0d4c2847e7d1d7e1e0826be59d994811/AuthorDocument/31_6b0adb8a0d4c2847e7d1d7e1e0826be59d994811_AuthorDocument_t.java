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
     
 	$RCSfile: AuthorDocument.java,v $ 
	Created on $Date: 2003-09-01 15:53:06 $ 
 */
 package net.sf.statcvs.output.xml.document;
 
 import net.sf.statcvs.I18n;
 import net.sf.statcvs.model.Author;
 import net.sf.statcvs.model.CvsContent;
 import net.sf.statcvs.model.RevisionIterator;
 import net.sf.statcvs.model.RevisionIteratorSummary;
 import net.sf.statcvs.output.xml.CvsCharts;
 import net.sf.statcvs.output.xml.chart.AbstractChart;
 import net.sf.statcvs.output.xml.element.ChartElement;
import net.sf.statcvs.output.xml.element.ElementContainer;
 import net.sf.statcvs.output.xml.element.ReportElement;
 import net.sf.statcvs.output.xml.element.ValueElement;
 import net.sf.statcvs.output.xml.report.CommitLogReport;
 
 /**
  * 
  * 
  * @author Steffen Pingel
  */
 public class AuthorDocument extends StatCvsDocument {
 
 	private CvsCharts charts;
 	private Author author;
 	private CvsContent content;
 
 	/**
 	 */
 	public AuthorDocument(CvsContent content, Author author) {
 		super("User statistics for " 
 			  + content.getModuleName(),
 			  getAuthorPageFilename(author));
 
 		this.content = content;
 		this.author = author;
 		
 		this.charts = new CvsCharts(content);
 		getRootElement().addContent(new GeneralReport());
 		getRootElement().addContent(new ModuleReport());
 		getRootElement().addContent(new LocReport());
 		getRootElement().addContent(new ActivityReport());
 		getRootElement().addContent(new CommitLogReport(author, 20));
 	}
 
 	/**
 	 * @see net.sf.statcvs.output.xml.StatCvsDocument#getCharts()
 	 */
 	public AbstractChart[] getCharts() {
 		return new AbstractChart[] {
 			charts.getActivityByHourChart(author),
 			charts.getActivityByDayChart(author),
 			charts.getDirectorySizesChart(author),
 			charts.getLocPerAuthorChart(author)
 		};
 	}
 
 
 	private class GeneralReport extends ReportElement {
 		
 		public GeneralReport() {
 			super(I18n.tr("General Statistics for {0}", author.getName()));
 			CvsContent content = AuthorDocument.this.content;
 			RevisionIteratorSummary summary;
 			summary = new RevisionIteratorSummary(content.getRevisionIterator());
 			long totalChangeCount = summary.size();
 			long totalLineCount = summary.getLineValue();
 
 			RevisionIterator userRevs = author.getRevisionIterator();
 			summary = new RevisionIteratorSummary(userRevs);
 			long userChangeCount = summary.size();
 			long userLineCount = summary.getLineValue();
 
 			double percent = (double)userChangeCount * 100 / totalChangeCount;
			ElementContainer ec = new ElementContainer("generalinfo");
			addContent(ec);
			ec.addContent
 				(new ValueElement("totalChanges", userChangeCount, percent,
 					  I18n.tr("Total changes")));
 			percent = (double)userLineCount * 100 / totalLineCount;
 			
			ec.addContent
 				(new ValueElement("loc", userLineCount, percent, 
 					  I18n.tr("Lines of code")));
 		}
 	}
 
 	private class LocReport extends ReportElement {
 
 		public LocReport() {
 			super(I18n.tr("Lines Of Code"));
 			addContent
 				(new ChartElement(charts.getLocPerAuthorChart(author)));
 		}
 	}
 
 	private class ModuleReport extends ReportElement {
 
 		public ModuleReport() {
 			super(I18n.tr("Modules"));
 			addContent
 				(new ChartElement(charts.getDirectorySizesChart(author)));
 		}
 	}
 
 	private class ActivityReport extends ReportElement
 	{
 		public ActivityReport() {
 			super(I18n.tr("Activity By Time"));
 			addContent
 				(new ChartElement(charts.getActivityByHourChart(author)));
 			addContent
 				(new ChartElement(charts.getActivityByDayChart(author)));
 		}
 	}
 
 	/**
 	 * Escapes evil characters in author's names. E.g. "#" must be escaped
 	 * because for an author "my#name" a page "author_my#name.html" will be
 	 * created, and you can't link to that in HTML
 	 * @param authorName an author's name
 	 * @return a version safe for creation of files and URLs
 	 */
 	public static String escapeAuthorName(String authorName) {
 		return authorName.replaceAll("#", "_");
 	}
  
 
 	/**
 	 * @param author an author
 	 * @return filename for author's page
 	 */
 	public static String getAuthorPageFilename(Author author) {
 		return "user_" + escapeAuthorName(author.getName());
 	}
 
 }
