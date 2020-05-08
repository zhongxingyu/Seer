 /**
  *
  */
 package cytoscape.plugin;
 
 import org.jdom.*;
 
 import org.jdom.input.*;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 
 /**
  * @author skillcoy
  *
  */
 public class PluginFileReader {
	private Document Doc;

 	/**
 	 * Creates a new PluginFileReader object.
 	 *
 	 * @param Url
 	 *            DOCUMENT ME!
 	 *
 	 */
 	public PluginFileReader(String Url) throws java.io.IOException, JDOMException {
 		//would be nice to validate later		
 		SAXBuilder Builder = new SAXBuilder(false);
		Doc = Builder.build(new java.net.URL(Url));
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public String getProjectName() {
		return Doc.getRootElement().getChild(this.nameTag).getTextTrim();
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public String getProjectDescription() {
		return Doc.getRootElement().getChild(this.descTag).getTextTrim();
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public String getProjectUrl() {
		return Doc.getRootElement().getChild(this.urlTag).getTextTrim();
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public List<PluginInfo> getPlugins() {
 		List<PluginInfo> Plugins = new ArrayList<PluginInfo>();
 
		Iterator<Element> pluginI = Doc.getRootElement().getChild(this.pluginListTag)
 		                               .getChildren(this.pluginTag).iterator();
 
 		while (pluginI.hasNext()) {
 			Element CurrentPlugin = pluginI.next();
 
 			PluginInfo Info = new PluginInfo(CurrentPlugin.getChildTextTrim(this.uniqueID));
 			Info.setName(CurrentPlugin.getChild(this.nameTag).getTextTrim());
 			Info.setDescription(CurrentPlugin.getChild(this.descTag).getTextTrim());
 			Info.setPluginVersion(CurrentPlugin.getChild(this.pluginVersTag).getTextTrim());
 			Info.setCytoscapeVersion(CurrentPlugin.getChild(this.cytoVersTag).getTextTrim());
 			Info.setUrl(CurrentPlugin.getChild(this.urlTag).getTextTrim());
			Info.setProjectUrl(getProjectUrl());
 
 			if (CurrentPlugin.getChild(this.categoryTag) != null)
 				Info.setCategory(CurrentPlugin.getChild(this.categoryTag).getTextTrim());
 			else
 				Info.setCategory("Uncategorized");
 
 			String Type = CurrentPlugin.getChild(this.fileType).getTextTrim();
 
 			if (Type.equalsIgnoreCase(PluginInfo.FileType.JAR.toString())) {
 				Info.setFiletype(PluginInfo.FileType.JAR);
 			} else if (Type.equalsIgnoreCase(PluginInfo.FileType.ZIP.toString())) {
 				Info.setFiletype(PluginInfo.FileType.ZIP);
 			} else {
 				// unknown type error and move on
 				System.err.println("Unknown plugin file type '" + Type + " skipping");
 
 				continue;
 			}
 
 			Iterator<Element> authI = CurrentPlugin.getChild(this.authorListTag)
 			                                       .getChildren(this.authorTag).iterator();
 
 			while (authI.hasNext()) {
 				Element CurrentAuthor = authI.next();
 				Info.addAuthor(CurrentAuthor.getChild(this.nameTag).getTextTrim(),
 				               CurrentAuthor.getChild(this.instTag).getTextTrim());
 			}
 
 			Plugins.add(Info);
 		}
 
 		return Plugins;
 	}
 
 	// XML Tags PluginTracker uses the same tags
 	protected static final String nameTag = "name";
 	protected static final String descTag = "description";
 	protected static final String classTag = "classname";
 	protected static final String pluginVersTag = "pluginVersion";
 	protected static final String cytoVersTag = "cytoscapeVersion";
 	protected static final String urlTag = "url";
 	protected static final String projUrlTag = "projectUrl";
 	protected static final String categoryTag = "category";
 	protected static final String fileListTag = "filelist";
 	protected static final String fileTag = "file";
 	protected static final String pluginListTag = "pluginlist";
 	protected static final String pluginTag = "plugin";
 	protected static final String authorListTag = "authorlist";
 	protected static final String authorTag = "author";
 	protected static final String instTag = "institution";
 	protected static final String fileType = "filetype";
 	protected static final String uniqueID = "uniqueID";
 }
