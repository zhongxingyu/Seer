 /*
  File: PluginTracker.java 
  Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)
 
  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies
 
  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.
 
  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 
 package cytoscape.plugin;
 
 import cytoscape.CytoscapeInit;
 
 import cytoscape.plugin.PluginInfo.AuthorInfo;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 
 import org.jdom.input.SAXBuilder;
 
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 
 import java.io.File;
 import java.io.FileWriter;
 
 import java.util.*;
 
 
 /**
  * @author skillcoy
  *
  */
 public class PluginTracker {
 	private Document trackerDoc;
 	private File installFile;
 	public enum PluginStatus { // xml tags describing status of plugin
 		CURRENT("CurrentPlugins"),
 		DELETE("DeletePlugins"),
 		INSTALL("InstallPlugins");
 
 		private String statusText;
 
 		private PluginStatus(String status) {
 			statusText = status;
 		}
 
 		public String getTagName() {
 			return statusText;
 		}
 	}
 
 	/**
 	 * Used for testing
 	 * @param FileName Xml file name
 	 * @param Dir directory to to write xml file
 	 * @throws java.io.IOException
 	 */
 	protected PluginTracker(File Dir, String FileName) throws java.io.IOException {
 		installFile = new File(Dir, FileName);
 		init();
 	}
 	
 	protected PluginTracker(File file) throws java.io.IOException {
 		installFile = file;
 		init();
 	}
 	
 	/*
 	 * Used for tests.
 	 */
 	protected File getTrackerFile() {
 		return installFile;
 	}
 	
 	/* 
 	 * Sets up the xml doc for tracking.
 	 */
 	private void init() throws java.io.IOException {
 		if (installFile.exists() && installFile.length() > 0) {
 			SAXBuilder Builder = new SAXBuilder(false);
 			try {
 				trackerDoc = Builder.build(installFile);
 				write();
 			} catch (JDOMException E) { // TODO do something with this error
 				E.printStackTrace();
 			}
 		} else {
 			trackerDoc = new Document();
 			trackerDoc.setRootElement(new Element("CytoscapePlugin"));
 			trackerDoc.getRootElement().addContent(new Element(PluginStatus.CURRENT.getTagName()));
 			trackerDoc.getRootElement().addContent(new Element(PluginStatus.INSTALL.getTagName()));
 			trackerDoc.getRootElement().addContent(new Element(PluginStatus.DELETE.getTagName()));
 			write();
 		}
 	}
 
 	
 	/**
 	* Gets a list of plugins by their status. CURRENT: currently installed
 	* DELETED: to be deleted INSTALL: to be installed
 	*
 	* @param Status
 	* @return List of PluginInfo objects
 	*/
 	protected List<PluginInfo> getListByStatus(PluginStatus Status) {
 		return getPluginContent(trackerDoc.getRootElement().getChild(Status.getTagName()));
 	}
 
 	/**
 	* Adds the given PluginInfo object to the list of plugins sharing the given
 	* status.
 	*
 	* @param obj
 	* @param Status
 	*/
 	protected void addPlugin(PluginInfo obj, PluginStatus Status) {
 		Element PluginParent = trackerDoc.getRootElement().getChild(Status.getTagName());
 		
 		Element Plugin = getMatchingPlugin(obj, Status.getTagName());
 		if (Plugin != null) {
 			// update the element
 			if (!obj.getName().equals(obj.getPluginClassName())) {
 				Plugin.getChild(nameTag).setText(obj.getName());
 			}
 			if (!obj.getCategory().equals(PluginInfo.Category.NONE.getCategoryText())) {
 				Plugin.getChild(categoryTag).setText(obj.getCategory());
 			}
 			Plugin.getChild(installLocTag).setText(obj.getInstallLocation());
 			Plugin.getChild(descTag).setText(obj.getDescription());
 			Plugin.getChild(pluginVersTag).setText(obj.getPluginVersion());
 			Plugin.getChild(cytoVersTag).setText(obj.getCytoscapeVersion());
 			if (obj.getPluginClassName() != null) {
 				Plugin.getChild(classTag).setText(obj.getPluginClassName());
 			}
 			Plugin.removeChild(authorListTag);
 			Element Authors = new Element(authorListTag);
 			for(AuthorInfo ai: obj.getAuthors()) {
 				Element Author = new Element(authorTag);
 				Author.addContent( new Element(nameTag).setText(ai.getAuthor()) );
 				Author.addContent( new Element(instTag).setText(ai.getInstitution()) );
 				Authors.addContent(Author);
 			}
 			Plugin.addContent(Authors);
 		} else {
 			PluginParent.addContent(createPluginContent(obj));
 			System.out.println("Adding plugin " + obj.getName() + " status " + Status.name());
 		}
 		write();
 	}
 
 	/**
 	* Removes the given PluginInfo object from the list of plugins sharing the
 	* given status.
 	*
 	* @param obj
 	* @param Status
 	*/
 	protected void removePlugin(PluginInfo obj, PluginStatus Status) {
 		Element PluginParent = trackerDoc.getRootElement().getChild(Status.getTagName());
 		
 		Element Plugin = getMatchingPlugin(obj, Status.getTagName());
 		if (Plugin != null) {
 			PluginParent.removeContent(Plugin);
 			System.out.println("Removing plugin " + obj.getName() + " status " + Status.name());
 			write();
 		}
 	}
 
 	/**
 	 * Matches one of the following rule: 
 	 * 1. Plugin class name
 	 * 2. uniqueID && projUrl
 	 * 3. Plugin specific Url (on the assumption that no two plugins can be downloaded from the same url)
 	 * @param Obj
 	 * @param Tag
 	 * @return
 	 */
 	private Element getMatchingPlugin(PluginInfo Obj, String Tag) {
 		List<Element> Plugins = trackerDoc.getRootElement().getChild(Tag).getChildren(pluginTag);
 
 		for (Element Current : Plugins) {
 			if ((Obj.getPluginClassName() != null && Current.getChildTextTrim(classTag).equals(Obj.getPluginClassName())) ||  
 				(Current.getChildTextTrim(uniqueIdTag).equals(Obj.getID()) &&
 				 Current.getChildTextTrim(projUrlTag).equals(Obj.getProjectUrl())) ||
 				 ((Current.getChildTextTrim(urlTag).length() > 0 && Obj.getUrl() != null) &&
 					Current.getChildTextTrim(urlTag).equals(Obj.getUrl())) ) {
 				  return Current;
 			}
 		}
 		return null;
 	}
 
 	/**
 	* Writes doc to file
 	*/
 	protected void write() {
 		try {
 			XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
 			FileWriter Writer = new FileWriter(installFile);
 			out.output(trackerDoc, Writer);
 			out.outputString(trackerDoc);
 			Writer.close();
 		} catch (java.io.IOException E) {
 			E.printStackTrace();
 		}
 	}
 
 	
 	public String toString() {
 		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
 		return out.outputString(trackerDoc);
 	}
 	
 	
 	/**
 	 * Deletes the tracker file.  This is currently never used outside of tests.
 	 */
 	protected void delete() {
 		installFile.delete();
 	}
 	
 	// TODO PluginFileReader does much the same stuff, need to merge the two
 	/*
 	* Takes a list of elemnts, creates the PluginInfo object for each and returns
 	* list of objects
 	*/
 	private List<PluginInfo> getPluginContent(Element PluginParentTag) {
 		List<PluginInfo> Content = new ArrayList<PluginInfo>();
 
 		List<Element> Plugins = PluginParentTag.getChildren(pluginTag);
 
 		for (Element CurrentPlugin : Plugins) {
 			
 			PluginInfo Info = new PluginInfo(CurrentPlugin.getChildTextTrim(uniqueIdTag));
 			Info.setName(CurrentPlugin.getChildTextTrim(nameTag));
 			Info.setDescription(CurrentPlugin.getChildTextTrim(descTag));
 			Info.setPluginClassName(CurrentPlugin.getChildTextTrim(classTag));
 			Info.setPluginVersion(CurrentPlugin.getChildTextTrim(pluginVersTag));
 			Info.setCytoscapeVersion(CurrentPlugin.getChildTextTrim(cytoVersTag));
 			Info.setCategory(CurrentPlugin.getChildTextTrim(categoryTag));
 			Info.setUrl(CurrentPlugin.getChildTextTrim(urlTag));
 			Info.setProjectUrl(CurrentPlugin.getChildTextTrim(projUrlTag));
 			Info.setInstallLocation(CurrentPlugin.getChildTextTrim(installLocTag));
 			
 			// set file type
 			String FileType = CurrentPlugin.getChildTextTrim(fileTypeTag);
 			if (FileType.equalsIgnoreCase(PluginInfo.FileType.JAR.toString())) {
 				Info.setFiletype(PluginInfo.FileType.JAR);
 			} else if (FileType.equalsIgnoreCase(PluginInfo.FileType.ZIP.toString())) {
 				Info.setFiletype(PluginInfo.FileType.ZIP);
 			}
 			
 			// add plugin files
 			List<Element> Files = CurrentPlugin.getChild(fileListTag).getChildren(fileTag);
 			for (Element File : Files) {
 				Info.addFileName(File.getTextTrim());
 			}
 
 			// add plugin authors
 			List<Element> Authors = CurrentPlugin.getChild(authorListTag)
 			                                     .getChildren(authorTag);
 			for (Element Author : Authors) {
 				Info.addAuthor(Author.getChildTextTrim(nameTag),
 				               Author.getChildTextTrim(instTag));
 			}
 
 			Info = PluginFileReader.addLicense(Info, CurrentPlugin);
 			
 			Content.add(Info);
 		}
 
 		return Content;
 	}
 
 	/*
 	* Create the plugin tag with all the appropriate tags for the PluginInfo
 	* object
 	*/
 	private Element createPluginContent(PluginInfo obj) {
 		Element Plugin = new Element(pluginTag);
 
 		Plugin.addContent(new Element(uniqueIdTag).setText(obj.getID()));
 		Plugin.addContent(new Element(nameTag).setText(obj.getName()));
 		Plugin.addContent(new Element(classTag).setText(obj.getPluginClassName()));
 		Plugin.addContent(new Element(descTag).setText(obj.getDescription()));
 		Plugin.addContent(new Element(pluginVersTag).setText(obj.getPluginVersion()));
 		Plugin.addContent(new Element(cytoVersTag).setText(obj.getCytoscapeVersion()));
 		Plugin.addContent(new Element(urlTag).setText(obj.getUrl()));
 		Plugin.addContent(new Element(projUrlTag).setText(obj.getProjectUrl()));
 		Plugin.addContent(new Element(categoryTag).setText(obj.getCategory()));
 		Plugin.addContent(new Element(fileTypeTag).setText(obj.getFileType().toString()));
 		Plugin.addContent(new Element(installLocTag).setText(obj.getInstallLocation()));
 		
 		// license
 		Element License = new Element(licenseTag);
 		License.addContent( new Element("text").setText(obj.getLicenseText()) );
 		Plugin.addContent(License);
 		
 		// authors
 		Element AuthorList = new Element(authorListTag);
 		for (AuthorInfo CurrentAuthor : obj.getAuthors()) {
 			Element Author = new Element(authorTag);
 			Author.addContent(new Element(nameTag).setText(CurrentAuthor.getAuthor()));
 			Author.addContent(new Element(instTag).setText(CurrentAuthor.getInstitution()));
 			AuthorList.addContent(Author);
 		}
 		Plugin.addContent(AuthorList);
 
 		// files
 		Element FileList = new Element(fileListTag);
 		for (String FileName : obj.getFileList()) {
 			FileList.addContent(new Element(fileTag).setText(FileName));
 		}
 		Plugin.addContent(FileList);
 
 		
 		
 		return Plugin;
 	}
 
 	// XML Tags to prevent misspelling issues, PluginFileReader uses most of the same
 	// tags, the xml needs to stay consistent
 	private String nameTag = PluginFileReader.nameTag;
 	private String descTag = PluginFileReader.descTag;
 	private String classTag = PluginFileReader.classTag;
 	private String pluginVersTag = PluginFileReader.pluginVersTag;
 	private String cytoVersTag = PluginFileReader.cytoVersTag;
 	private String urlTag = PluginFileReader.urlTag;
 	private String projUrlTag = PluginFileReader.projUrlTag;
 	private String categoryTag = PluginFileReader.categoryTag;
 	private String fileListTag = PluginFileReader.fileListTag;
 	private String fileTag = PluginFileReader.fileTag;
 	private String pluginListTag = PluginFileReader.pluginListTag;
 	private String pluginTag = PluginFileReader.pluginTag;
 	private String authorListTag = PluginFileReader.authorListTag;
 	private String authorTag = PluginFileReader.authorTag;
 	private String instTag = PluginFileReader.instTag;
 	private String uniqueIdTag = PluginFileReader.uniqueID;
 	private String fileTypeTag = PluginFileReader.fileType;
 	private String licenseTag = PluginFileReader.licenseTag;
	private String installLocTag = "installLocation";
 }
