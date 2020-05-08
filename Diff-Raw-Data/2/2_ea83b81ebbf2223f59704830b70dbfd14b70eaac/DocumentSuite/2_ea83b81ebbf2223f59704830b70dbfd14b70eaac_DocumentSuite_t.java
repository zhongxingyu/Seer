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
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import net.sf.statcvs.model.Author;
 import net.sf.statcvs.model.CvsContent;
 import net.sf.statcvs.model.Directory;
 
 import org.jdom.Attribute;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 
 import de.berlios.statcvs.xml.model.AuthorGrouper;
 import de.berlios.statcvs.xml.model.DayGrouper;
 import de.berlios.statcvs.xml.model.DirectoryGrouper;
 import de.berlios.statcvs.xml.model.FileGrouper;
 import de.berlios.statcvs.xml.model.ForEachAuthor;
 import de.berlios.statcvs.xml.model.ForEachDirectory;
 import de.berlios.statcvs.xml.model.ForEachModule;
 import de.berlios.statcvs.xml.model.HourGrouper;
 import de.berlios.statcvs.xml.model.Module;
 import de.berlios.statcvs.xml.model.ModuleGrouper;
 
 /**
  * Reads the document suite configuration from an XML file and create the reports.
  * 
  * @author Steffen Pingel
  */
 public class DocumentSuite {
 
 	private static Map filenameByDirectoryPath = new Hashtable();
 
 	private static Map filenameByAuthorName = new Hashtable();
 
 	private static Map filenameByModuleName= new Hashtable();
 
 	private static Map documentTitleByFilename = new LinkedHashMap();
 
 	private static Logger logger = Logger.getLogger(DocumentSuite.class.getName());
 	
 	private CvsContent content;
 	private Document suite;
 	private ReportSettings defaultSettings = new ReportSettings();
 
 	/**
 	 * 
 	 */
 	public DocumentSuite(URL url, CvsContent content) throws IOException
 	{
 		this.content = content;
 		
 		try {
 			SAXBuilder builder = new SAXBuilder();
 			suite = builder.build(url);
 		}
 		catch (JDOMException e) {
 			throw new IOException(e.getMessage());
 		}
 	}
 
 	public StatCvsDocument createDocument(Element root, DocumentRenderer renderer, ReportSettings settings) throws IOException
 	{
 		//StatCvsDocument document = new StatCvsDocument(readAttributes(settings, root));
 
 		// collect reports
 		ReportSettings documentSettings = new ReportSettings(settings);
 		
 		List reports = new ArrayList();
 		int maxPages = 0;
 		
 		for (Iterator it = root.getChildren().iterator(); it.hasNext();) {
 			Element element = (Element)it.next();
 			if ("settings".equals(element.getName())) {
 				documentSettings.load(element);
 			}
 			else if ("report".equals(element.getName())) {
 				Report report = createReport(element, documentSettings);
 				
 				if (report != null && report.getPageCount() != 0) {
 					reports.add(report);
 					maxPages = Math.max(report.getPageCount(), maxPages);
 				}
 			}
 		}
 		
 		//render documents
 		StatCvsDocument firstPage = null;
 		
 		for (int i = 0; i < maxPages; i++) {
 			ReportSettings localSettings = readAttributes(settings, root);
 			localSettings.setPageNr(i);
 			StatCvsDocument document = new StatCvsDocument(localSettings);
 
 			if (i == 0) {
 				firstPage = document;
 			}
 
			if (maxPages > 1) {
 				ReportElement pager = new ReportElement("");
 				pager.addContent(createPagerElement(i, maxPages, firstPage.getFilename()));
 				document.getRootElement().addContent(pager);
 			}
 			
 			for (int r = 0; r < reports.size(); r++) {
 				ReportElement re = ((Report)reports.get(r)).getPage(i);
 				if (re != null) {
 					document.getRootElement().addContent(re);
 				}
 			}
 			
 			renderer.render(document);
 		}
 	
 		return firstPage;
 	}
 
 	private Element createPagerElement(int currPage, int total, String baseName) {
 		
 		Element pager = new Element("pager");
 		pager.setAttribute("current", ""+(currPage + 1));
 		pager.setAttribute("total", ""+total);
 		for (int i=0; i < total; i++) {
 			Element page = new Element("page");
 			page.setAttribute("filename", baseName + ((i == 0)?"":"_"+i));
 			page.setAttribute("nr", ""+(i+1));
 			pager.addContent(page);
 		}
 		return pager;
 	}
 
 	/**
 	 * @param element
 	 */
 	private Report createReport(Element root, ReportSettings documentSettings) 
 	{
 		ReportSettings reportSettings = readAttributes(documentSettings, root);
 //		for (Iterator it = root.getChildren().iterator(); it.hasNext();) {
 //			Element element = (Element)it.next();
 //			if ("settings".equals(element.getName())) {
 //				readSettings(reportSettings, element);
 //			}
 //		}
 		
 		String value = reportSettings.getString("groupby", null);
 		if ("author".equals(value)) {
 			reportSettings.setGrouper(new AuthorGrouper());
 		}
 		else if ("day".equals(value)) {
 			reportSettings.setGrouper(new DayGrouper());
 		}
 		else if ("directory".equals(value)) {
 			reportSettings.setGrouper(new DirectoryGrouper());
 		}
 		else if ("file".equals(value)) {
 			reportSettings.setGrouper(new FileGrouper());
 		}
 		else if ("hour".equals(value)) {
 			reportSettings.setGrouper(new HourGrouper());
 		}
 		else if ("module".equals(value)) {
 			reportSettings.setGrouper(new ModuleGrouper(reportSettings.getModules(content)));
 		}
 		
 		String className = root.getAttributeValue("class");
 		if (className != null) {
 			if (className.indexOf(".") == -1) {
 				className = "de.berlios.statcvs.xml.report." + className;
 			}  
 
 			try {
 				Class c = Class.forName(className);
 				Method m = c.getMethod("generate", new Class[] { CvsContent.class, ReportSettings.class });
 				try {
 					Report report = (Report)m.invoke(null, new Object[] { content, reportSettings });
 					return report;
 				}
 				catch (InvocationTargetException e) {
 					if (e.getCause() instanceof EmptyReportException) {
 						logger.warning("Empty Report");
 					}
 					else {
 						throw e;
 					}
 				}
 			}
 			catch (Exception e) {
 				logger.warning("Could not generate report: " + e);
 				e.printStackTrace();
 			}
 		}
 		
 		return null;
 	}
 
 	public void generate(DocumentRenderer renderer, ReportSettings defaultSettings) 
 		throws IOException 
 	{
 		this.defaultSettings = defaultSettings;
 			
 		// generate documents
 		for (Iterator it = suite.getRootElement().getChildren().iterator(); it.hasNext();) {
 			Element element = (Element)it.next();
 			if ("settings".equals(element.getName())) {
 				defaultSettings.load(element);
 			}
 			else if ("document".equals(element.getName())) {
 				renderDocument(renderer, element);
 			}
 		}
 
 		renderer.postRender();
 	}
 
 	/**
 	 * @param renderer
 	 * @param element
 	 */
 	private void renderDocument(DocumentRenderer renderer, Element element) throws IOException
 	{
 		String value = element.getAttributeValue("foreach");
 		if (value == null) {
 			StatCvsDocument document = createDocument(element, renderer, defaultSettings);
 			documentTitleByFilename.put(document.getFilename(), document.getTitle());
 		}
 		else if ("author".equals(value)) {
 			for (Iterator i = content.getAuthors().iterator(); i.hasNext();) {
 				Author author = (Author)i.next();
 				ReportSettings settings = new ReportSettings(defaultSettings);
 				settings.setForEach(new ForEachAuthor(author));
 				StatCvsDocument doc = createDocument(element, renderer, settings);
 				filenameByAuthorName.put(author.getName(), doc.getFilename());
 			}
 		}
 		else if ("directory".equals(value)) {
 			for (Iterator i = content.getDirectories().iterator(); i.hasNext();) {
 				Directory dir = (Directory)i.next();
 				if (!dir.isEmpty()) {
 					ReportSettings settings = new ReportSettings(defaultSettings);
 					settings.setForEach(new ForEachDirectory(dir));
 					StatCvsDocument doc = createDocument(element, renderer, settings);
 					filenameByDirectoryPath.put(dir.getPath(), doc.getFilename());
 				}
 			}
 		}
 		else if ("module".equals(value)) {
 			ModuleBuilder builder = new ModuleBuilder(defaultSettings.getModules(content), content.getRevisions().iterator());
 			for (Iterator i = builder.getModules().iterator(); i.hasNext();) {
 				Module module = (Module)i.next();
 				ReportSettings settings = new ReportSettings(defaultSettings);
 				settings.setForEach(new ForEachModule(module));
 				StatCvsDocument doc = createDocument(element, renderer, settings);
 				filenameByModuleName.put(module.getName(), doc.getFilename());
 			}
 		}
 		else {
 			throw new IOException("Invalid foreach value");	
 		}
 	}
 
 	/**
 	 * Creates a new ReportSettings object that inherits from parentSettings. 
 	 * All attributes of root are added as key value pairs to the created ReportSettings 
 	 * object and the object is returned.
 	 */
 	private ReportSettings readAttributes(ReportSettings parentSettings, Element root) 
 	{
 		ReportSettings settings = new ReportSettings(parentSettings);
 		for (Iterator it = root.getAttributes().iterator(); it.hasNext();) {
 			Attribute setting = (Attribute)it.next();
 			if (!setting.getName().startsWith(ReportSettings.PRIVATE_SETTING_PREFIX)) {
 				settings.put(setting.getName(), setting.getValue());
 			}
 		}
 		return settings;
 	}
 	
 	public static String getAuthorFilename(String name)
 	{
 		return (String)filenameByAuthorName.get(name);
 	}
 
 	public static String getDirectoryFilename(String path)
 	{
 		return (String)filenameByDirectoryPath.get(path);
 	}
 
 	public static String getModuleFilename(String module)
 	{
 		return (String)filenameByModuleName.get(module);
 	}
 
 	public static Map getDocuments()
 	{
 		return documentTitleByFilename;
 	}
 	
 }
