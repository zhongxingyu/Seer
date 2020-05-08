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
     
 	$RCSfile: ModulesTreeReport.java,v $
	$Date: 2003-07-04 15:17:27 $ 
 */
 package net.sf.statcvs.output.xml.report;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.logging.Logger;
 
 import net.sf.statcvs.I18n;
 import net.sf.statcvs.Main;
 import net.sf.statcvs.model.CvsContent;
 import net.sf.statcvs.model.Directory;
 import net.sf.statcvs.output.ConfigurationOptions;
 import net.sf.statcvs.output.xml.document.ModuleDocument;
 import net.sf.statcvs.util.FileUtils;
 
 import org.jdom.Element;
 
 /**
  * ModulesTreeReport
  * 
  * @author Tammo van Lessen
  */
 public class ModulesTreeReport extends ReportElement {
 
 	private static Logger logger
 		= Logger.getLogger("net.sf.statcvs.output.XMLOutput");
 	/**
 	 * 
 	 */
 	public ModulesTreeReport(CvsContent content) {
 		super(I18n.tr("Repository Tree"));
 		Iterator it = content.getDirectories().iterator();
 		createReport(it);
 	}
 	
 	/**
 	 * 
 	 */
 	public ModulesTreeReport(Directory dir) {
 		super(I18n.tr("Repository Tree"));
 		Iterator it = dir.getSubdirectoriesRecursive().iterator();
 		createReport(it);
 	}
 
 	/**
 	 * 
 	 */
 	private void createReport(Iterator directoryIt) {
 		Element modules = new Element("modulesTree");
		
 		while (directoryIt.hasNext()) {
 			Directory dir = (Directory) directoryIt.next();
 			Element module = new Element("module");
 			if (dir.isRoot()) {
 				module.setAttribute("name", I18n.tr("[root]"));
 			} else {
 				module.setAttribute("name", dir.getName());
 			}
			module.setAttribute("depth", ""+dir.getDepth());
 			module.setAttribute("files", ""+dir.getCurrentFileCount());
 			module.setAttribute("loc", ""+dir.getCurrentLOC());
 			if (dir.isEmpty()) {
 				module.setAttribute("removed", "true");
 			}
 
 			module.setAttribute("url",ModuleDocument.getModulePageUrl(dir));
 			modules.addContent(module);
 		}
 		addContent(modules);
 		// copy dir icon
 		try {
 			FileUtils.copyFile(
 					Main.class.getResourceAsStream("web-files/" + "folder.png"),
 					new File(ConfigurationOptions.getOutputDir() + "folder.png"));
 			FileUtils.copyFile(
 					Main.class.getResourceAsStream("web-files/" + "folder-deleted.png"),
 					new File(ConfigurationOptions.getOutputDir() + "folder-deleted.png"));
 
 		} catch (IOException e) {
 			logger.warning(e.getMessage());
 		}
 	}
 }
