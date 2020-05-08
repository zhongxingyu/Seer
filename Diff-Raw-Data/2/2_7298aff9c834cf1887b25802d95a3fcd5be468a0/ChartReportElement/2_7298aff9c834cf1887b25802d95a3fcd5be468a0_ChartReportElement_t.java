 /*
  *  StatCvs-XML - XML output for StatCvs.
  *
  *  Copyright by Steffen Pingel, Tammo van Lessen.
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License
  *  version 2 as published by the Free Software Foundation.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
  
 package de.berlios.statcvs.xml.output;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.jdom.Element;
 
 import de.berlios.statcvs.xml.chart.AbstractChart;
 
 
 public class ChartReportElement extends ReportElement {
 
 	AbstractChart chart;
 
 	public ChartReportElement(ReportSettings settings, String defaultTitle, 
 				AbstractChart chart, TooltipMapElement tooltipMap)
 	{
 		super(settings, defaultTitle);
 		
 		this.chart = chart;
 	
 		Element element = new Element("img");
 		element.setAttribute("src", chart.getFilename());
 
		if (tooltipMap != null && settings.getBoolean("showImagemap", false)) {
 			element.setAttribute("usemap", "#" + tooltipMap.getMapName());
 			addContent(tooltipMap);
 		}
 
 		addContent(element);
 	}
 
 	public ChartReportElement(AbstractChart chart)
 	{
 		this(chart, null);
 	}
 	
 	public ChartReportElement(AbstractChart chart, TooltipMapElement tooltipMap)
 	{
 		this(chart.getSettings(), chart.getSubtitle(), chart, tooltipMap);
 	}
 
 	public void saveResources(File outputPath) throws IOException
 	{
 		chart.save(outputPath);
 	}
 	
 }
