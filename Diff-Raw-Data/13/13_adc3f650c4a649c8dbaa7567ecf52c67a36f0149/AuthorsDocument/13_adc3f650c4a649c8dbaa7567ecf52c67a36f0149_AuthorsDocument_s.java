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
     
 	$RCSfile: AuthorsDocument.java,v $ 
	Created on $Date: 2003-06-20 10:21:03 $ 
 */
 package net.sf.statcvs.output.xml;
 
 import java.util.*;
 
 import net.sf.statcvs.*;
 import net.sf.statcvs.model.CvsContent;
 import net.sf.statcvs.model.RevisionIterator;
 import net.sf.statcvs.model.*;
 import net.sf.statcvs.output.LOCSeriesBuilder;
 import net.sf.statcvs.renderer.*;
 import net.sf.statcvs.renderer.LOCChart;
 import net.sf.statcvs.reports.*;
 import net.sf.statcvs.util.*;
 
 import org.jdom.Element;
 
 import com.jrefinery.data.BasicTimeSeries;
 
 /**
  * The authors document. Contains links to all author documents.
  * 
  * @author Steffen Pingel
  */
 public class AuthorsDocument extends StatCvsDocument {
 
 	private String[] categoryNamesHours = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", 
 		"8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", 
 		"20", "21",	"22", "23" };
 
 	private String[] categoryNamesDays = new String[] { "Sunday", "Monday", "Tuesday", "Wednesday", 
 		"Thursday", "Friday", "Saturday" };
 
 	private CvsContent content;
 	private RevisionIterator revIt;
 
 	/**
 	 */
 	public AuthorsDocument(CvsContent content) {
 		super("User statistics for " 
 			  + content.getModuleName(), "authors");
 
 		this.content = content;
 
 		getRootElement().addContent(createAuthorsReport());
 	}
 
 	/**
 	 * @see net.sf.statcvs.output.xml.StatCvsDocument#getCharts()
 	 */
 	public Chart[] getCharts() {
 		return new Chart[] {
 			createActivityByHourChart(), createActivityByDayChart(),
 			createLOCPerAuthorChart(),
 		};
 	}
 
 	private Chart createActivityByHourChart()
 	{
 		Chart chart = createActivityChart
 			(revIt, Messages.getString("ACTIVITY_TIME_TITLE"),	
 			 "activity_time.png", categoryNamesHours);
 		revIt.reset();
 		return chart;
 	}
 
 	private Chart createActivityByDayChart()
 	{
 		Chart chart = createActivityChart
 			(revIt, Messages.getString("ACTIVITY_DAY_TITLE"),
 			 "activity_day.png", categoryNamesDays);
 		revIt.reset();
 		return chart;
 	}
 
 	private Chart createActivityChart(RevisionIterator revIt, String title, String fileName, 
 		String[] categoryNames) {
 		return new BarChart(revIt, content.getModuleName(),
 				title, fileName, categoryNames.length, categoryNames);
 	}
 
 	private Chart createLOCPerAuthorChart() {
 		Iterator authorsIt = content.getAuthors().iterator();
 		Map authorSeriesMap = new HashMap();
 		while (authorsIt.hasNext()) {
 			Author author = (Author) authorsIt.next();
 			authorSeriesMap.put(
 					author,
 					new LOCSeriesBuilder(author.getName(), false));
 		}
 		RevisionIterator allRevs = new RevisionSortIterator(content.getRevisionIterator());
 		while (allRevs.hasNext()) {
 			CvsRevision rev = allRevs.next();
 			LOCSeriesBuilder builder =
 					(LOCSeriesBuilder) authorSeriesMap.get(rev.getAuthor());
 			builder.addRevision(rev);
 		}
 		List authors = new ArrayList(authorSeriesMap.keySet());
 		Collections.sort(authors);
 		List seriesList = new ArrayList();
 		authorsIt = authors.iterator();
 		while (authorsIt.hasNext()) {
 			Author author = (Author) authorsIt.next();
 			LOCSeriesBuilder builder = (LOCSeriesBuilder) authorSeriesMap.get(author);
 			BasicTimeSeries series = builder.getTimeSeries();
 			if (series != null) {
 				seriesList.add(series);
 			} 
 		}
 		if (seriesList.isEmpty()) {
 			return null;
 		}	 
 		String projectName = content.getModuleName();
 		String subtitle = Messages.getString("TIME_LOCPERAUTHOR_SUBTITLE");
 		return new LOCChart(seriesList, projectName, subtitle, "loc_per_author.png", 640, 480);
 	}
 
 
 	private Element createAuthorsReport()
 	{
 		Element reportRoot = new ReportElement(I18n.tr("Authors"));
 		reportRoot.addContent(new LocReport().calculate());
 		return reportRoot;
 	}
 
 
 	private class LocReport extends AbstractLocTableReport 
 	{
 		public LocReport()
 		{
 			super(content);
 		}
 
 		public Element calculate()
 		{
 			Element authors = new Element("authors");
 
 			calculateChangesAndLinesPerAuthor
 				(content.getRevisionIterator());
 			Iterator it = getLinesMap().iteratorSortedByValueReverse();
 			while (it.hasNext()) {
 				Author author = (Author) it.next();
 				Element element = new Element("author");
 				element.setAttribute("name", author.getName());
 				element.setAttribute("changes", getChangesMap().get(author) + "");
 				element.setAttribute("loc", getLinesMap().get(author) + "");
 				double percent = (double)getLinesMap().get(author) 
 					/ getLinesMap().sum();
 				element.setAttribute("locPercent", 
 									 Formatter.formatNumber(percent, 1));
 				element.setAttribute("locPerChange", 
 									 Formatter.formatNumber(getLinesMap().get(author) / getChangesMap().get(author), 1));
 				authors.addContent(element);
 			}
 
 			return authors;
 		}
 	}
 
 }
