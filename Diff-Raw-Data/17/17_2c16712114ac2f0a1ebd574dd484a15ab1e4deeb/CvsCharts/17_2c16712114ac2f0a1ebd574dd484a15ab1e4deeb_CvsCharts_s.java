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
     
 	$RCSfile: CvsCharts.java,v $
	$Date: 2003-06-28 11:12:27 $ 
 */package net.sf.statcvs.output.xml.report;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import net.sf.statcvs.model.Author;
 import net.sf.statcvs.model.CvsContent;
 import net.sf.statcvs.model.Directory;
 import net.sf.statcvs.output.xml.chart.AbstractChart;
 import net.sf.statcvs.output.xml.chart.ActivityChart;
 import net.sf.statcvs.output.xml.chart.AuthorsActivityChart;
 import net.sf.statcvs.output.xml.chart.AvgFileSizeChart;
 import net.sf.statcvs.output.xml.chart.DirectorySizesChart;
 import net.sf.statcvs.output.xml.chart.FileCountChart;
 import net.sf.statcvs.output.xml.chart.LocChart;
 import net.sf.statcvs.output.xml.chart.LocPerAuthorChart;
 
 /**
  * CvsCharts
  * 
  * @author Tammo van Lessen
  */
 public class CvsCharts {
 
 	private Map userActByHourCharts = new HashMap();
 	private Map userActByDayCharts = new HashMap();
 	private Map userDirSizesCharts = new HashMap();
 	private Map userLocCharts = new HashMap();
 	private Map moduleLocCharts = new HashMap();
 	private AbstractChart fileCountChart;
 	private AbstractChart fileSizeChart;
 	private AbstractChart dirSizeChart;
 	private AbstractChart actByDayChart;
 	private AbstractChart actByHourChart;
 	private AbstractChart locChart;
 	private AbstractChart locPerAuthorChart;
 	private AbstractChart actChart;
 	private CvsContent content;
 
 	public CvsCharts(CvsContent content) 
 	{
 		this.content = content;
 	}
 
 	public AbstractChart getFileCountChart() {
 		if (fileCountChart == null) {
 			fileCountChart = new FileCountChart(content);
 		}
 		return (fileCountChart.isRendered())?fileCountChart:null;
 	}
 
 	public AbstractChart getAvgFileSizeChart() {
 		if (fileSizeChart == null) {
 			fileSizeChart = new AvgFileSizeChart(content);
 		}
 		return (fileSizeChart.isRendered())?fileSizeChart:null;
 	}
 
 	public AbstractChart getLocChart() {
 		if (locChart == null) {
 			locChart = new LocChart(content);
 		}
 		return (locChart.isRendered())?locChart:null;
 	}
 
 	public AbstractChart getDirectorySizesChart() {
 		if (dirSizeChart == null) {
 			dirSizeChart = new DirectorySizesChart(content);
 		}
 		return (dirSizeChart.isRendered())?dirSizeChart:null;
 	}
 
 	public AbstractChart getDirectorySizesChart(Author author) {
 		AbstractChart dsc = (AbstractChart)userDirSizesCharts.get(author);
 		if (dsc == null) {
 			dsc = new DirectorySizesChart(author);
 			userDirSizesCharts.put(author, dsc);
 		}
 		return (dsc.isRendered())?dsc:null;
 	}
 
 	public AbstractChart getLocPerAuthorChart() {
 		if (locPerAuthorChart == null) {
 			locPerAuthorChart = new LocPerAuthorChart(content);
 		}
 		return (locPerAuthorChart.isRendered())?locPerAuthorChart:null;
 	}
 
 	public AbstractChart getLocPerAuthorChart(Author author) {
 		AbstractChart lpa = (AbstractChart)userLocCharts.get(author);
 		if (lpa == null) {
 			lpa = new LocPerAuthorChart(content, author);
 			userLocCharts.put(author, lpa);
 		}
 		return (lpa.isRendered())?lpa:null;
 	}
 
 	public AbstractChart getActivityByDayChart() {
 		if (actByDayChart == null) {
 			actByDayChart = new ActivityChart(content, ActivityChart.BY_DAY);
 		}
 		return (actByDayChart.isRendered())?actByDayChart:null;
 	}
 
 	public AbstractChart getActivityByHourChart() {
 		if (actByHourChart == null) {
 			actByHourChart = new ActivityChart(content, ActivityChart.BY_HOUR);
 		}
 		return (actByHourChart.isRendered())?actByHourChart:null;
 	}
 
 	public AbstractChart getAuthorsActivityChart() {
 		if (actChart == null) {
 			actChart = new AuthorsActivityChart(content);
 		}
 		return (actChart.isRendered())?actChart:null;
 	}
 
 	public AbstractChart getActivityByHourChart(Author author) {
 		AbstractChart abh = (AbstractChart)userActByHourCharts.get(author);
 		if (abh == null) {
 			abh = new ActivityChart(author, ActivityChart.BY_HOUR);
 			userActByHourCharts.put(author, abh);
 		}
 		return (abh.isRendered())?abh:null;
 	}
 
 	public AbstractChart getActivityByDayChart(Author author) {
 		AbstractChart abd = (AbstractChart)userActByDayCharts.get(author);
 		if (abd == null) {
 			abd = new ActivityChart(author, ActivityChart.BY_DAY);
 			userActByDayCharts.put(author, abd);
 		}
 		return (abd.isRendered())?abd:null;
 	}
 
 	public AbstractChart getLocPerModuleChart(Directory directory) {
 		AbstractChart lpm = (AbstractChart)moduleLocCharts.get(directory);
 		if (lpm == null) {
			lpm = new LocChart(directory);
 			moduleLocCharts.put(directory, lpm);
 		}
 		return (lpm.isRendered())?lpm:null;
 	}
 }
