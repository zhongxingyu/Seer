 /*
  File: PluginInfo.java 
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
 import cytoscape.util.URLUtil;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import java.net.URL;
 
 /**
  * @author skillcoy Object describes a plugin
  */
 public class PluginInfo {
 	/**
 	 * Jar and Zip files currently supported
 	 * 
 	 * @author skillcoy
 	 * 
 	 */
 	public enum FileType {
 		JAR("jar"), ZIP("zip");
 
 		private String typeText;
 
 		private FileType(String type) {
 			typeText = type;
 		}
 
 		public String toString() {
 			return typeText;
 		}
 	}
 	private String releaseDate;
 	private FileType fileType;
 
 	private String uniqueID;
 
 	private String pluginClassName;
 
 	private String pluginName;
 
 	private List<AuthorInfo> authors;
 
 	private License license;
 
 	private String pluginDescription;
 
 	private String pluginVersion;
 
 	private String cytoscapeVersion;
 
 	private String pluginUrl;
 
 	private String projectUrl;
 
 	private String downloadUrl;
 	
 	private String pluginCategory;
 
 	private String versionMatch = "^\\d+\\.\\d+";
 
 	private String versionSplit = "\\.";
 
 	private List<String> pluginFiles;
 
 	private boolean licenseRequired = false;
 
 	protected String enclosingJar;
 	
 	protected String installLocation;
 
 	
 	/**
 	 * Initializes a PluginInfo object with the following defaults:
 	 * setName("Unknown"); setDescription("No pluginDescription");
 	 * setPluginVersion("0.1"); setCytoscapeVersion(
 	 * cytoscape.cytoscapeVersion.version ); setCategory("Uncategorized");
 	 * setProjectUrl(CytoscapeInit.getProperties().getProperty("defaultPluginUrl"));
 	 */
 	public PluginInfo() {
 		init();
 	}
 
 	/**
 	 * See PluginInfo()
 	 * 
 	 * @param UniqueID
 	 *            Additionally this sets the unique identifier that will be used
 	 *            to find a new version of the plugin at the given project url.
 	 */
 	public PluginInfo(String UniqueID) {
 		uniqueID = UniqueID;
 		init();
 	}
 
 	/*
 	 * Sets all the fields that are required to a default value in case it is
 	 * not called
 	 */
 	private void init() {
 		pluginFiles = new ArrayList<String>();
 		authors = new ArrayList<AuthorInfo>();
 		setName("Unknown");
 		setDescription("No description");
 		setPluginVersion(0.1);
 		setCytoscapeVersion(cytoscape.CytoscapeVersion.version);
 		setCategory(Category.NONE);
 		setPluginClassName("");
 	}
 
 	/* SET */
 	/**
 	 * Sets name of plugin. This will be displayed to users.
 	 * 
 	 * @param name
 	 */
 	public void setName(String name) {
 		pluginName = name;
 	}
 
 	/**
 	 * Sets the release date of a plugin.  Displayed to users;
 	 * @param date
 	 */
 	public void setReleaseDate(String date) {
 		releaseDate = date;
 	}
 	
 	/**
 	 * Sets the plugin class name. Used for tracking plugins.
 	 * 
 	 * @param className
 	 */
 	protected void setPluginClassName(String className) {
 		pluginClassName = className;
 	}
 
 	/**
 	 * Sets a description of the plugin. This will be displayed to users.
 	 * 
 	 * @param description
 	 */
 	public void setDescription(String description) {
 		pluginDescription = description;
 	}
 
 	/**
 	 * Sets the version of the plugin. Defaults to 0.1
 	 * 
 	 * @param version
 	 * 				String version with format \d+.\d+	
 	 * @throws NumberFormatException
 	 * 				If the string version is of a format other than \d+.\d+
 	 */
 	public void setPluginVersion(double Version) throws NumberFormatException {
 		String version = Double.toString(Version);
 		if (versionOk(version, true)) {
 			pluginVersion = version;
 		} else {
 			throw new NumberFormatException(
 					"Bad plugin version '" + version + "'. Plugin version numbers must be in the format: \\d+.\\d+");
 		}
 	}
 
 	/**
 	 * Sets the Cytoscape version this plugin is compatible with.
 	 * 
 	 * @param version
 	 */
 	public void setCytoscapeVersion(String version)
 			throws NumberFormatException {
 		if (versionOk(version, false)) {
 			cytoscapeVersion = version;
 		} else {
 			throw new NumberFormatException(
 					"Cytoscape version numbers must be in the format: \\d+.\\d+  optional to add: .\\d+-[a-z]");
 		}
 	}
 
 	/**
 	 * pluginUrl this plugin was downloaded from. It is presumed this can be
 	 * used for update later.
 	 * @param url
 	 */
 	protected void setUrl(String url) {
 		pluginUrl = url;
 	}
 
 	/**
 	 * Sets the url of a site describing this plugin project
 	 * @param url
 	 */
 	public void setProjectUrl(String url) {
 		projectUrl = url;
 	}
 
 	/**
 	 * URL for the xml file describing all plugins from any given project
 	 * (ex. http://cytoscape.org/plugins/plugin.xml)
 	 * 
 	 * @param url
 	 */
 	protected void setDownloadUrl(String url) {
 		downloadUrl = url;
 	}
 	
 	/**
 	 * Jar or Zip are currently supported. Use PluginInfo.JAR or PluginInfo.ZIP.
 	 * This will only be set by the PluginManager generally and can only be set once 
 	 * as an object's file type will not change.
 	 * @param type
 	 */
 	protected void setFiletype(FileType type) {
 		if (fileType == null)
 			fileType = type;
 	}
 
 	/**
 	 * Sets a list of files (prefer full paths) installed with this plugin.
 	 * Includes the jar file.
 	 * @param list
 	 */
 	protected void setFileList(List<String> list) {
 		pluginFiles = list;
 	}
 
 	/**
 	 * Sets a category for the plugin. Defaults to "Uncategorized"
 	 * 
 	 * @param category
 	 */
 	public void setCategory(String category) {
 		pluginCategory = category;
 	}
 
 	/**
 	 * Sets the category for the plugin using the enum PluginInfo.Category
 	 * 
 	 * @param catName
 	 */
 	public void setCategory(Category catName) {
 		pluginCategory = catName.getCategoryText();
 	}
 
 	/**
 	 * Adds a file to the list of installed files.
 	 * 
 	 * @param fileName
 	 */
 	protected void addFileName(String fileName) {
 		if (!pluginFiles.contains(fileName))
 			pluginFiles.add(fileName);
 	}
 
 	/**
 	 * Adds an author to the list of authors.
 	 * 
 	 * @param authorName
 	 * @param institution
 	 */
 	public void addAuthor(String authorName, String institution) {
 		authors.add(new AuthorInfo(authorName, institution));
 	}
 
 	/**
 	 * Clears author list.
 	 */
 	public void clearAuthorList() {
 		authors.clear();
 	}
 	
 	/**
 	 * Sets the license information for the plugin. Not required.
 	 * 
 	 * @param java.net.URL
 	 *            object where license can be downloaded from.
 	 */
 	public void setLicense(URL url) {
 		license = new License(url);
 	}
 
 	/**
 	 * Sets the license information for the plugin. Not required.
 	 * 
 	 * @param Text
 	 *            string of license.
 	 * @param alwaysRequired
 	 *            If the user expects the license to be required for both
 	 *            install and update at all times (true) or only at install
 	 *            (false)
 	 */
 	public void setLicense(String licenseText, boolean alwaysRequired) {
 		license = new License(licenseText);
 		licenseRequired = alwaysRequired;
 	}
 
 	// this just checks the plugin version and the cytoscape version
 	private boolean versionOk(String version, boolean plugin) {
 		// \d+.\+d ok
 		String Match = versionMatch;
 		String Split = versionSplit;
 
 		if (plugin) {
 			Match = Match + "$";
 		} else { // cytoscape version
 			Match = Match + "(\\.\\d+)?$";
 			Split = "\\.|-";
 		}
 
 		if (!version.matches(Match)) {
 			return false;
 		}
 
 		String[] SplitVersion = version.split(Split);
 
 		int max = 2;
 		if (!plugin) {
 			max = 3; // cytoscape version numbers
 			// if there's a fourth is must be alpha
 			if (SplitVersion.length == 4) {
 				if (!SplitVersion[3].matches("[a-z]+")) {
 					return false;
 				}
 			}
 		}
 
 		// can't be longer than the accepted version types
 		if (SplitVersion.length > max) {
 			return false;
 		}
 
 		// must be digets
 		for (int i = 0; i < max && i < SplitVersion.length; i++) {
 			if (!SplitVersion[i].matches("\\d+")) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * This is meant to only get set by the PluginManager.  It can only
 	 * be set once as the install location can't move.
 	 * 
 	 * @param Loc
 	 */
 	protected void setInstallLocation(String Loc) {
 		if (installLocation == null)
 			installLocation = Loc;
 	}
 	
 	/* GET */
 	/**
 	 * Gets the full install path for this plugin.  Should look like
 	 * <HOME_DIR>/.cytoscape/<cytoscape_version>/plugins/<pluginName-pluginVersion>
 	 */
 	public java.io.File getPluginDirectory() {
 		 java.io.File PluginDir = new java.io.File(
 				 PluginManager.getPluginManager().getPluginManageDirectory(),
 				 this.getName()+"-"+this.getPluginVersion());
 		return PluginDir;
 	}
 
 	/**
 	 * @return String of the installation location for the plugin and all of it's files.
 	 * 		Generally this is .cytoscape/[cytoscape version]/plugins/PluginName-version
 	 */
 	public String getInstallLocation() {
 		return installLocation;
 	}
 	
 	public String getReleaseDate() {
 		return releaseDate;
 	}
 	
 	/**
 	 * @return The text of the license for this plugin if available.
 	 */
 	public String getLicenseText() {
 		if (license != null)
 			return license.getLicense();
 		else
 			return null;
 	}
 
 	/**
 	 * @return If the license is always required to be accepted for installs and
 	 *         updates this returns true. If it only is required at install time
 	 *         (never at update) returns false.
 	 */
 	public boolean isLicenseRequired() {
 		return licenseRequired;
 	}
 
 	/**
 	 * @return The unique id for this object.
 	 */
 	public String getID() {
 		return uniqueID;
 	}
 
 	/**
 	 * @return FileType of file type for plugin. PluginInfo.JAR or
 	 *         PluginInfo.ZIP
 	 */
 	public FileType getFileType() {
 		return fileType;
 	}
 
 	/**
 	 * @return pluginName of plugin
 	 */
 	public String getName() {
 		return pluginName;
 	}
 
 	/**
 	 * @return Java class name
 	 */
 	public String getPluginClassName() {
 		return pluginClassName;
 	}
 
 	/**
 	 * @return List of authors.
 	 */
 	public List<AuthorInfo> getAuthors() {
 		return authors;
 	}
 
 	/**
 	 * @return Plugin pluginDescription.
 	 */
 	public String getDescription() {
 		return pluginDescription;
 	}
 
 	/**
 	 * @return Plugin version.
 	 */
 	public String getPluginVersion() {
 		return pluginVersion;
 	}
 
 	/**
 	 * @return Compatible Cytoscape version
 	 */
 	public String getCytoscapeVersion() {
 		return cytoscapeVersion;
 	}
 
 	/**
 	 * @return Url to download plugin from
 	 */
 	public String getUrl() {
 		return pluginUrl;
 	}
 
 	/**
 	 * 
 	 * @return Url that points to a site describing this plugin project
 	 */
 	public String getProjectUrl() {
 		return projectUrl;
 	}
 
 	/**
 	 * @return Url that returns the document of available plugins this plugin
 	 *         came from.  Example http://cytoscape.org/plugins/all_plugins.xml
 	 */
 	public String getDownloadUrl()  {
 		return downloadUrl;
 	}
 	
 	/**
 	 * @return Plugin category.
 	 */
 	public String getCategory() {
 		return pluginCategory;
 	}
 
 	/**
 	 * @return List of files installed with this plugin (includes plugin jar
 	 *         file).
 	 */
 	public List<String> getFileList() {
 		return pluginFiles;
 	}
 
 	/**
 	 * @return Returns String of plugin name and version
 	 */
 	public String toString() {
 		return getName() + " " + getPluginVersion();
 	}
 
 	/**
 	 * Compare the version of the object to the given object.
 	 * 
 	 * @param New
 	 *            Potentially newer PluginInfo object
 	 * @return true if given version is newer
 	 */
 	public boolean isNewerPluginVersion(PluginInfo New) {
 		String[] CurrentVersion = getPluginVersion().split(versionSplit);
 		String[] NewVersion = New.getPluginVersion().split(versionSplit);
 		
 		int CurrentMajor = Integer.valueOf(CurrentVersion[0]).intValue();
 		int NewMajor = Integer.valueOf(NewVersion[0]).intValue();
 		
 		int CurrentMinor = Integer.valueOf(CurrentVersion[1]).intValue();
 		int NewMinor = Integer.valueOf(NewVersion[1]).intValue();
 		
 		if ( (CurrentMajor > NewMajor ||
 			 (CurrentMajor == NewMajor && CurrentMinor >= NewMinor)) ) {
 			return false;
 		}
 			
 		return true;
 	}
 
 	/**
 	 * 
 	 * @return true if the plugin is compatible with the current Cytoscape
 	 *         version major.minor (bugfix is only checked if the plugin
 	 *         specifies a bugfix version)
 	 */
 	public boolean isCytoscapeVersionCurrent() {
 		String[] CyVersion = cytoscape.CytoscapeVersion.version
 				.split(versionSplit);
 		String[] PlVersion = getCytoscapeVersion().split(versionSplit);
 
 		for (int i = 0; i < PlVersion.length; i++) {
 			if (Integer.valueOf(CyVersion[i]).intValue() != Integer.valueOf(
 					PlVersion[i]).intValue())
 				return false;
 		}
 
 		return true;
 	}
 
 	// yea, it's ugly...styles taken from cytoscape website
 	public String htmlOutput() {
 		String Html = "<html><style type='text/css'>";
 		Html += "body,th,td,div,p,h1,h2,li,dt,dd ";
 		Html += "{ font-family: Tahoma, \"Gill Sans\", Arial, sans-serif; }";
 		Html += "body { margin: 0px; color: #333333; background-color: #ffffff; }";
 		Html += "#indent { padding-left: 30px; }";
 		Html += "ul {list-style-type: none}";
 		Html += "</style><body>";
 
 		Html += "<b>" + getName() + "</b><p>";
 		Html += "<b>Version:</b>&nbsp;" + getPluginVersion() + "<p>";
 		Html += "<b>Category:</b>&nbsp;" + getCategory() + "<p>";
 		Html += "<b>Description:</b><br>" + getDescription() + "<p>";
 
 		if (getReleaseDate() != null && getReleaseDate().length() > 0) {
 			Html += "<b>Release Date:</b>&nbsp;" + getReleaseDate() + "<p>";
 		}
 		
 		Html += "<b>Released By:</b><br><ul>";
 		for (AuthorInfo ai : getAuthors()) {
 			Html += "<li>" + ai.getAuthor() + ", " + ai.getInstitution()
 					+ "<br>";
 		}
 		Html += "</ul>";
 
 		Html += "</font></body></html>";
 		return Html;
 	}
 
 	/**
 	 * Describes an author for a given plugin.
 	 * 
 	 * @author skillcoy
 	 */
 	public class AuthorInfo {
 		private String authorName;
 
 		private String institutionName;
 
 		public AuthorInfo(String Name, String Institution) {
 			authorName = Name;
 			institutionName = Institution;
 		}
 
 		public String getAuthor() {
 			return authorName;
 		}
 
 		public String getInstitution() {
 			return institutionName;
 		}
 	}
 
 	/**
 	 * Fetches and keeps a plugin license if one is available.
 	 */
 	public class License {
 		private java.net.URL url;
 
 		private String text;
 
 		public License(java.net.URL Url) {
 			url = Url;
 		}
 
 		public License(String LicenseText) {
 			text = LicenseText;
 		}
 
 		/**
 		 * Get the license text as a string. Will download from url if License
 		 * was not initialized with text string.
 		 * 
 		 * @return String
 		 */
 		public String getLicense() {
 			if (text == null) {
 				try {
 					text = URLUtil.download(url);
 				} catch (java.io.IOException E) {
 					E.printStackTrace();
 				}
 			}
 			return text;
 		}
 
 	}
 
 }
