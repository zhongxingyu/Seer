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
  
 package de.berlios.statcvs.xml.report;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Insets;
 import java.awt.RenderingHints;
 import java.awt.Stroke;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import net.sf.statcvs.model.CvsContent;
 import net.sf.statcvs.model.CvsFile;
 import net.sf.statcvs.model.CvsRevision;
 import net.sf.statcvs.model.Directory;
 import net.sf.statcvs.model.SymbolicName;
 
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.LegendItem;
 import org.jfree.chart.LegendItemCollection;
 import org.jfree.chart.plot.Plot;
 import org.jfree.chart.plot.PlotRenderingInfo;
 import org.jfree.chart.plot.PlotState;
 
 import de.berlios.statcvs.xml.I18n;
 import de.berlios.statcvs.xml.chart.AbstractChart;
 import de.berlios.statcvs.xml.output.ChartReportElement;
 import de.berlios.statcvs.xml.output.Report;
 import de.berlios.statcvs.xml.output.ReportSettings;
 import de.berlios.statcvs.xml.output.TooltipMapElement;
 
 /**
  * EvolutionMatrixChart
  * 
  * @author Tammo van Lessen
  */
 public class EvolutionMatrixChart extends AbstractChart {
 
 	private final int SPACER = 25; 
 	private final int LINE_HEIGHT = 4;
 	private final int TEXT_HEIGHT = 15;
     
 	private TooltipMapElement tooltipMap;
 
     /**
      * 
      */
     public EvolutionMatrixChart(CvsContent content, ReportSettings settings) 
     {
 		super(settings, "evolution.png", I18n.tr("Software Evolution Matrix"));
 
 		tooltipMap = new TooltipMapElement("evomatrix");
 	
 		setChart(new JFreeChart(settings.getProjectName(), null, 
 				 new EvolutionMatrixPlot(content, settings), true));
 		setup(true);
 		
     }
     
 	/**
 	 * 
 	 */
 	public static Report generate(CvsContent content, ReportSettings settings)
 	{
 		EvolutionMatrixChart chart = new EvolutionMatrixChart(content, settings);
 		return (chart.getChart() != null) ? new Report(new ChartReportElement(chart, chart.getTooltipMap())) : null;
 	}
 	
     
 	private TooltipMapElement getTooltipMap()
 	{
 		return tooltipMap;
 	}
 	
 	/**
 	 * 
 	 * EvolutionMatrixPlot
 	 * 
 	 * @author Tammo van Lessen
 	 */
 	private class EvolutionMatrixPlot extends Plot
 	{
 		private CvsContent content;
 		private Map filesByVersion = new TreeMap();
 		private Map evoFiles = new HashMap();
 		private SortedSet versions = new TreeSet();
 		
 		/**
 		 * 
 		 */
 		public EvolutionMatrixPlot(CvsContent content, ReportSettings settings) {
 			this.content = content;
 			
 			Iterator it = settings.getSymbolicNameIterator(content);
 			while (it.hasNext()) {
 				SymbolicName sn = (SymbolicName)it.next();
 				Version version = new Version(sn.getName(), sn.getDate());
 
 				int maxLoc = 0;
 				Iterator revIt = sn.getRevisions().iterator();
 				while (revIt.hasNext()) {
 					CvsRevision rev = (CvsRevision)revIt.next();
 					maxLoc = Math.max(maxLoc, rev.getLines());
 					TaggedFile evo = (TaggedFile)evoFiles.get(rev.getFile());
 					if (evo == null) {
 						evo = new TaggedFile(rev.getFile());
 						evoFiles.put(rev.getFile(), evo);	 
 					}
 					
 					evo.addRevision(version, rev);
 
 				}
 				
 				version.setMaxLoc(maxLoc);
 				versions.add(version);
 
 			}
 			
 			// cheat head into map
 			Version version = new Version("HEAD", new Date()); 
 			it = content.getFiles().iterator();
 			int maxLoc = 0;
 			while (it.hasNext()) {
 				CvsFile file = (CvsFile)it.next();
 				if (!file.isDead()) {
 					TaggedFile evo = (TaggedFile)evoFiles.get(file);
 					if (evo == null) {
 						evo = new TaggedFile(file);
 						evoFiles.put(file, evo);	 
 					}
 					maxLoc = Math.max(maxLoc, file.getLatestRevision().getLines());
 					evo.addRevision(version, file.getLatestRevision());
 				}
 			}
 
 			version.setMaxLoc(maxLoc);
 			versions.add(version);			
 		}
 
         /**
          * @see org.jfree.chart.plot.Plot#getPlotType()
          */
         public String getPlotType() {
             return "EvolutionMatrixPlot";
         }
 
         /**
          * @see org.jfree.chart.plot.Plot#draw(java.awt.Graphics2D, 
          * 		java.awt.geom.Rectangle2D, org.jfree.chart.plot.PlotState, 
          * 		org.jfree.chart.plot.PlotRenderingInfo)
          */
         public void draw(Graphics2D g2, Rectangle2D plotArea, 
         				  PlotState state, PlotRenderingInfo info) 
         {
 			// record the plot area...
 			if (info != null) {
 				info.setPlotArea(plotArea);
 			}
 
 			// adjust the drawing area for the plot insets (if any)...
 			Insets insets = getInsets();
 			if (insets != null) {
 				plotArea.setRect(plotArea.getX() + insets.left,
 								 plotArea.getY() + insets.top,
 								 plotArea.getWidth() - insets.left - insets.right,
 								 plotArea.getHeight() - insets.top - insets.bottom);
 			}
 			
 			// store file here if file occurs the first time
 			List newAdded = new ArrayList();
 
 			// get version iterator
 			Iterator verIt = versions.iterator();
 			
 			double vspace = plotArea.getWidth() / versions.size();
 			double x = plotArea.getX();
 			double y = plotArea.getY() + SPACER;
 
 			// set drawing settings
 			Stroke oldStroke = g2.getStroke();
 			Stroke itemStroke = new BasicStroke(1);
 			Stroke borderStroke = new BasicStroke(1); 
 			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
 								RenderingHints.VALUE_ANTIALIAS_OFF);
 
 			Version lastVersion = null;
 			while (verIt.hasNext()) {
 				Version ver = (Version)verIt.next();
 
 				// draw tag names
 				g2.setColor(Color.black);
 				g2.drawString(ver.getName(), (int)x, 
 						(int)plotArea.getY() + SPACER - 10);
 
 				// walk through all directories...
 				Iterator dirIt = content.getDirectories().iterator();
 				while (dirIt.hasNext()) {
 					Directory dir = (Directory)dirIt.next();
 					Iterator fit = dir.getFiles().iterator();
 
 					if (dir.getFiles().size() != 0) {
 						//y += (g2.getFontMetrics().getStringBounds(dir.getName(), g2).getHeight() / 2) - 1;
 						y += (TEXT_HEIGHT / 2) + 1;
 						if (x == plotArea.getX()) {
 							g2.setColor(new Color(0xCCCCCC));
 							
 							g2.setStroke(borderStroke);
 							
 							Rectangle2D r = g2.getFontMetrics().getStringBounds(dir.getName(), g2);
 							g2.fill3DRect((int)x - 3, (int)(y+r.getY() + 1), 
 									(int)plotArea.getWidth(), (int)r.getHeight(), true);
 							g2.setColor(Color.black);
 							g2.drawString(dir.getPath(), (float)x, (float)y);
 							
 						}
 						//y += TEXT_HEIGHT;
 						y += LINE_HEIGHT;// + 1;
 
 					}
 					
 					// and files...
 					while (fit.hasNext()) {
 						CvsFile file = (CvsFile)fit.next();
 						TaggedFile eFile = (TaggedFile)evoFiles.get(file);
 						
 						
 						g2.setStroke(itemStroke);
 							
 						// draw light gray shadow - in contrast to small yellow lines.
 						g2.setColor(Color.lightGray);									
 						drawLine(g2, plotArea, x-1, y, 0, LINE_HEIGHT);
 
 						// tagged file
 						if (eFile != null) {
 							
 							// COLORIZE
 							
 							if (lastVersion != null) {
 								
 								//   new files: green
 								//   untouched files: grey
 								//   modified files: red
 								//   deleted files: black
 								
 								if (!eFile.isInVersion(lastVersion)) {
 									g2.setColor(Color.green);
 								} else if (eFile.hasSameRevision(lastVersion, ver)) {
 									g2.setColor(Color.gray);
 								} else if (!eFile.isInVersion(ver) 
 									&& eFile.isInVersion(lastVersion)) {
 										
 									g2.setColor(Color.black);
 								} else {
 									g2.setColor(Color.red);
 								}
 							} else {
 								// all files of the first version: green
 								g2.setColor(Color.green);
 							}
 							
 							// drawing
 							if (eFile.isInVersion(ver)) {
 								// draw existing file
 								int length = (int)((eFile.getScore(ver)) * (vspace - 10));
 								//g2.drawLine((int)x, (int)y, (int)x + length, (int)y);
 								drawLine(g2, plotArea, x, y, length, LINE_HEIGHT);
 							} else if (eFile.isInVersion(lastVersion)) {
 								// draw deleted file with score of the last known version
 								int length = (int)((eFile.getScore(lastVersion)) * (vspace - 10));
 								//g2.drawLine((int)x, (int)y, (int)x + length, (int)y);
 								drawLine(g2, plotArea, x, y, length, LINE_HEIGHT);
 							}
 							
 							// mark changes
 
 							if (lastVersion != null 
 								&& eFile.getRevision(ver) != null
 								&& eFile.getRevision(lastVersion) != null
 								&& !eFile.hasSameRevision(lastVersion, ver)) {
 
 								// draw changes yellow
 								g2.setColor(Color.yellow);
 								int length = (int)((eFile.getChangedScore(lastVersion, ver)) * (vspace - 10));
 								//g2.drawLine((int)x, (int)y, (int)x + length, (int)y);
 								drawLine(g2, plotArea, x, y, length, LINE_HEIGHT);								
 							}
 						} else {
 							// file was never tagged
 							
 							// draw grey dot
 							//g2.setColor(Color.lightGray);
 							//g2.drawLine((int)x, (int)y, (int)x, (int)y);
 						}
 						
 						CvsRevision linkRev = (eFile == null)?null:eFile.getRevision(ver);
 						String link 
 							= (getSettings().getWebRepository() == null)
 							? "#"
 							: (linkRev == null)?getSettings().getWebRepository().getFileHistoryUrl(file)
 								:getSettings().getWebRepository().getFileViewUrl(eFile.getRevision(ver));
 						
 						tooltipMap.addRectArea((int)x, (int)y, (int)(x + vspace), 
 							(int)y + LINE_HEIGHT, file.getFilenameWithPath(),
 							link);
 							
 						// next line
 						y += LINE_HEIGHT + 1;
 					}
 				}
 				
 				// next block
 				x += vspace;
 				y = plotArea.getY() + SPACER;
 
 				// remember last version
 				lastVersion = ver;	
 			}
 			
 			g2.setStroke(oldStroke);
         }
         
         private void drawLine(Graphics2D g2, Rectangle2D plotArea, 
         						double x, double y, int length, int width)
         {
         	for (int i = 0; i < width; i++) {
 				g2.drawLine((int)x, (int)y + i, (int)x + length, (int)y + i);
         	}
         }
         
         public int getHeight() 
         {	
         	int dirCount = 0;
         	Iterator it = content.getDirectories().iterator();
         	while (it.hasNext()) {
         		if (((Directory)it.next()).getFiles().size() != 0) {
         			dirCount++;
         		}
         	}
 
         	return getInsets().bottom + getInsets().bottom + (4*SPACER) + 
         		(content.getFiles().size() * (LINE_HEIGHT + 1))
         		+ (dirCount * TEXT_HEIGHT);
         }
         /**
          * @see org.jfree.chart.plot.Plot#getLegendItems()
          */
         public LegendItemCollection getLegendItems() {
             LegendItemCollection result = new LegendItemCollection();
             Stroke stroke = new BasicStroke(1);
             
             LegendItem le = new LegendItem(I18n.tr("Added File"), "", null, 
             							Color.green, Color.black, stroke);
             result.add(le);
             
 			le = new LegendItem(I18n.tr("Modified File"), "", null, 
 										Color.red, Color.black, stroke);
 			result.add(le);
 
 			le = new LegendItem(I18n.tr("Unmodified File"), "", null, 
 										Color.gray, Color.black, stroke);
 			result.add(le);
 
 			le = new LegendItem(I18n.tr("Removed File"), "", null, 
 										Color.black, Color.black, stroke);
 			result.add(le);
 
 			le = new LegendItem(I18n.tr("Changes"), "", null, 
 										Color.yellow, Color.black, stroke);
 			result.add(le);
 
             return result;
         }
 
 	}
 	
 	
     /**
      * @see de.berlios.statcvs.xml.chart.AbstractChart#getPreferredHeigth()
      */
     public int getPreferredHeigth() 
     {
 		if (getChart() == null) {
 			return super.getPreferredHeigth();	
 		} else {
 			return ((EvolutionMatrixPlot)getChart().getPlot()).getHeight();
 		}
     }
 
 	private class Version implements Comparable
 	{
 		private String name;
         private Date date;
         private int maxLoc;
 
         public Version(String name, Date date)
 		{
 			this.date = date;
 			this.name = name; 
 		}
 		
 		public String getName()
 		{
 			return name;
 		}
 		
 		public Date getDate()
 		{
 			return date;
 		}
 
 		public int getMaxLoc()
 		{
 			return maxLoc;
 		}
 		
 		public void setMaxLoc(int maxLoc)
 		{
 			this.maxLoc = maxLoc;
 		}
 		
         /**
          * @see java.lang.Comparable#compareTo(java.lang.Object)
          */
         public int compareTo(Object o) 
         {
 			Version other = (Version)o;
 			int dateComp = getDate().compareTo(other.getDate()); 
 			return (dateComp != 0) ? dateComp
 									: getName().compareTo(other.getName());
         }
 
         /**
          * @see java.lang.Object#equals(java.lang.Object)
          */
         public boolean equals(Object obj) 
         {
             return (name + date).equals(obj);
         }
 
         /**
          * @see java.lang.Object#hashCode()
          */
         public int hashCode() 
         {
             return (name + date).hashCode();
         }
 
 	}
 	
 	
 	private class TaggedFile 
 	{
 		private Map revisionByVersion = new TreeMap();
 		private CvsFile file;
 		
 		public TaggedFile(CvsFile file)
 		{
 			this.file = file;
 		}
 		
 		/**
 		 * Add a revision to a version. 
 		 */
 		void addRevision(Version ver, CvsRevision rev)
 		{
 			revisionByVersion.put(ver, rev);
 		}
 		
 		/**
 		 * Returns the revision for this file and the given version.
 		 */
 		public CvsRevision getRevision(Version ver) 
 		{
 			if (ver == null) {
 				return null;
 			}
 			return (CvsRevision)revisionByVersion.get(ver);
 		}
 		
 		/**
 		 * Returns the line score of this file in the given version.
 		 * 
 		 * Divides line count by the max. line count in the given version.  
 		 */
 		public double getScore(Version ver)
 		{
 			if (ver.getMaxLoc() != 0) {
 				return (double)getRevision(ver).getLines() / ver.getMaxLoc();	
 			}
 			else {
 				return 0;
 			}
 			
 		}
 		
 		/**
 		 * Returns the changing score.
 		 * 
 		 * Counts the replaces line for each revision between oldV and thisV
 		 * and divides it by the linecount of thisVs revision.
  		 * thisV must have the higher revision.
 		 */
 		public double getChangedScore(Version oldV, Version thisV)
 		{
 			CvsRevision target = getRevision(oldV);
 			CvsRevision curr = getRevision(thisV);
 			double change = curr.getReplacedLines();	
 			int revCount = 0;
			while (target != curr) {
 				curr = curr.getPreviousRevision();
 				if (curr != null && (curr.getLines() != 0)) {
 					change += curr.getReplacedLines() / curr.getLines();
 					revCount++;	
 				}
 			}
 			
 			if (getRevision(thisV).getLines() != 0) {
 				return (double)change / getRevision(thisV).getLines();	
 			} 
 			else {
 				return (change == 0)?0:1;
 			}
 			
 		}
 		
 		/**
 		 * Returns true, if this file is tagged by the given version
 		 */
 		public boolean isInVersion(Version ver)
 		{
 			return getRevision(ver) != null;	
 		}
 		
 		/**
 		 * Returns true, if this file has for both versions the same revision.
 		 */
 		public boolean hasSameRevision(Version v1, Version v2)
 		{
 			return (getRevision(v1) == getRevision(v2))
 				&& ((getRevision(v1) != null));
 		}
 	}
 }
