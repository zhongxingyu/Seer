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
     
 	$RCSfile: ModuleDocument.java,v $ 
	Created on $Date: 2003-09-01 15:53:06 $ 
 */
 package net.sf.statcvs.output.xml.document;
 
 import net.sf.statcvs.I18n;
 import net.sf.statcvs.Settings;
 import net.sf.statcvs.model.CvsContent;
 import net.sf.statcvs.model.Directory;
 import net.sf.statcvs.output.util.WebRepositoryIntegration;
 import net.sf.statcvs.output.xml.CvsCharts;
 import net.sf.statcvs.output.xml.chart.AbstractChart;
 import net.sf.statcvs.output.xml.element.ChartElement;
 import net.sf.statcvs.output.xml.element.ElementContainer;
 import net.sf.statcvs.output.xml.element.LinkElement;
 import net.sf.statcvs.output.xml.element.PeriodElement;
 import net.sf.statcvs.output.xml.element.ReportElement;
 import net.sf.statcvs.output.xml.element.ValueElement;
 import net.sf.statcvs.output.xml.report.AuthorsReport;
 import net.sf.statcvs.output.xml.report.CommitLogReport;
 import net.sf.statcvs.output.xml.report.ModulesTreeReport;
 import net.sf.statcvs.util.DateUtils;
 
 import org.jdom.Element;
 
 /**
  * ModuleDocument
  * 
  * @author Tammo van Lessen
  */
 public class ModuleDocument extends StatCvsDocument {
 
 	private CvsCharts charts;
 	private Directory directory;
 	private CvsContent content;
 
 	public ModuleDocument(CvsContent content, Directory directory) {
 		super(I18n.tr("Module statistics for",0,1)+directory.getPath(),
 				ModuleDocument.getModulePageUrl(directory));
 
 		this.directory = directory;
 		this.content = content;
 		this.charts = new CvsCharts(content);
 		getRootElement().addContent(new GeneralReport());
 		getRootElement().addContent(new ModulesTreeReport(directory));
 		getRootElement().addContent(new LocReport());
 		getRootElement().addContent(new AuthorsReport(directory));
 		getRootElement().addContent(new CommitLogReport(directory));
 	}
 
 	private class GeneralReport extends ReportElement {
 		public GeneralReport() {
 			super(I18n.tr("General statistics for {0}",directory.getPath()));
 			CvsContent content = ModuleDocument.this.content;
			ElementContainer ec = new ElementContainer("generalinfo");
			addContent(ec);
			ec.addContent(new PeriodElement(I18n.tr("Summary Period"),
 								   content.getFirstDate(), content.getLastDate()));
			ec.addContent(new PeriodElement(I18n.tr("Generated"),
 								   DateUtils.currentDate()));
 			
 			if (Settings.getWebRepository() != null) {
 				WebRepositoryIntegration rep = Settings.getWebRepository();
 				String text = I18n.tr("Browse with {0}", rep.getName());
				ec.addContent(new LinkElement(rep.getDirectoryUrl(directory), text));
 			}
 		}
 	}
 	
 	private class LocReport extends ReportElement {
 
 		public LocReport() {
 			super(I18n.tr("Lines of Code"));
 			Element container = new ElementContainer("locchart");
 			addContent(container);
 			container.addContent(new ChartElement(charts.getLocPerModuleChart(directory)));
 			container.addContent(new ValueElement("loc", directory.getCurrentLOC(),I18n.tr("Lines Of Code")));
 		}
 		
 	}
 	
 	/**
 	 * @see net.sf.statcvs.output.xml.StatCvsDocument#getCharts()
 	 */
 	public AbstractChart[] getCharts() {
 		return new AbstractChart[] {
 			charts.getLocPerModuleChart(directory)
 		};
 	}
 
 	public static String escapeModuleName(Directory dir) {
 		String module = dir.getPath();
 		if (!module.startsWith("/")) {
 			module = "/" + module;
 		}
 		return module.substring(0, module.length() - 1).replaceAll("/", "_");
 	}
 	public static String getModulePageUrl(Directory dir) {
 		return "module"+escapeModuleName(dir);
 	}
 
 }
