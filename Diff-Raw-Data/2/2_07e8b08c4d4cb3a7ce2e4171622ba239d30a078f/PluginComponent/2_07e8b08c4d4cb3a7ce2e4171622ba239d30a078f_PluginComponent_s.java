 package org.geworkbench.engine.ccm;
 
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  * A java bean to hold the contents of a .cwb.xml file. This is only used for
  * those that have plugin element.
  * 
  * @author tg2321
  * @version $Id$
  * 
  */
 public class PluginComponent {
 	private String clazz = null;
 	private String name = null;
 	private String version = null;
 	private String author = null;
 	private String authorUrl = null;
 	private String tutorialUrl = null;
 	private String toolUrl = null;
 	private String description = null;
 	private String license = null;
 	private boolean mustAccept = false;
 	private String documentation = null;
 	private boolean loadByDefault = false;
 	private boolean isHidden = false;
 
 	private String pluginName;
 	private String pluginId;
 	private String resource;
 
 	private List<String> required = null;
 	private List<String> related = null;
 
 	private Category category = null;
 	
 	static enum Category {ANALYSIS, VISUALIZER, NORMALIZER, FILTER};
 	
 	static Map<String, Category> categoryMap = new TreeMap<String, Category>();
 	static {
		categoryMap.put("anaysis", Category.ANALYSIS);
 		categoryMap.put("visualizer", Category.VISUALIZER);
 		categoryMap.put("filter", Category.FILTER);
 		categoryMap.put("normalizer", Category.NORMALIZER);
 	}
 
 	public PluginComponent(String name, String clazz, String version,
 			String author, String authorUrl, String tutorialUrl,
 			String toolUrl, String description, String license,
 			boolean mustAccept, String documentation, boolean loadByDefault,
 			boolean isHidden, String pluginName, String pluginId,
 			String resource, Category category,
 			List<String> required, List<String> related) {
 		this.name = name;
 		this.clazz = clazz;
 		this.version = version;
 		this.author = author;
 		this.authorUrl = authorUrl;
 		this.tutorialUrl = tutorialUrl;
 		this.toolUrl = toolUrl;
 		this.description = description;
 		this.license = license;
 		this.mustAccept = mustAccept;
 		this.documentation = documentation;
 		this.loadByDefault = loadByDefault;
 		this.isHidden = isHidden;
 		this.pluginName = pluginName;
 		this.pluginId = pluginId;
 		this.resource = resource;
 		this.category = category;
 		
 		this.required = required;
 		this.related = related;
 	}
 
 	public String getClazz() {
 		return clazz;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getVersion() {
 		return version;
 	}
 
 	public String getAuthor() {
 		return author;
 	}
 
 	public String getAuthorUrl() {
 		return authorUrl;
 	}
 
 	public String getTutorialUrl() {
 		return tutorialUrl;
 	}
 
 	public String getToolUrl() {
 		return toolUrl;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public String getLicense() {
 		return license;
 	}
 
 	public boolean isMustAccept() {
 		return mustAccept;
 	}
 
 	public String getDocumentation() {
 		return documentation;
 	}
 
 	public boolean getLoadByDefault() {
 		return loadByDefault;
 	}
 
 	public List<String> getRequired() {
 		return required;
 	}
 
 	public List<String> getRelated() {
 		return related;
 	}
 
 	String getPluginName() {
 		return pluginName;
 	}
 
 	String getPluginId() {
 		return pluginId;
 	}
 
 	String getResource() {
 		return resource;
 	}
 
 	Category getCategory() {
 		return category;
 	}
 
 	boolean isHidden() {
 		return isHidden;
 	}
 
 	public boolean equals(Object object) {
 		if(!(object instanceof PluginComponent))
 			return false;
 		
 		PluginComponent component = (PluginComponent)object;
 		if(component.getName().equals(name))
 			return true;
 		else
 			return false;
 	}
 	
 	public int hashCode() {
 		return name.hashCode();
 	}
 }
