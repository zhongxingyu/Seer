 /**
  * ModuleMetadata.java
  * <p>
  * A data structure to store configuration information found in a Module's
  * manifest file.
  *
  * @author  Andrew DeMaria
  * @author  Austin Diviness
  */
 
 package edu.mines.acmX.exhibit.module_manager;
 
 import java.util.Map;
 
 public class ModuleMetaData {
     // This is only used by the module manager and will known when a
     // modulemetadata object is built
     private String jarFileName;
 
 
     // manifest top level
     private String packageName;
     private String className;
 
     // uses-sdk
     private String minSdkVersion, targetSdkVersion;
 
     // module definition
     private String iconPath, title, author, version;
 
     // inputs
     // document what boolean means
     Map<String, DependencyType> inputTypes;
 
     // required modules
     Map<String, DependencyType> moduleDependencies;
 	private boolean optionalAll;
 
     /**
      * Creates a ModuleMetaData object
      *
      * @param   packageName         Package name of the module.
      *                              ex. com.example.app
      * @param   className           Class name of the class that implements one
      *                              of the ModuleInterface(s)
      * @param   minSdkVersion       Lowest sdk version the module can use
      * @param   targetSdkVersion    Sdk version app ideally wants
      * @param   iconPath            File path to icon image
      * @param   title               Title of module
      * @param   author              Author of module
      * @param   version             Module version
      * @param   inputTypes          Map of Input types used by module. Map 
      *                              keys indicate input type, boolean value
      *                              indicates if input type is required
      * @param   moduleDependencies  Other modules required by the module. A 
      *                              map that associates the module package
      *                              names to their optional/required level
      */
 	public ModuleMetaData(String packageName, String className, String minSdkVersion,
 			String targetSdkVersion, String iconPath, String title,
 			String author, String version, Map<String, DependencyType> inputTypes,
			Map<String, DependencyType> moduleDependencies, boolean optionalAll) {
 		super();
 		this.packageName = packageName;
         this.className = className;
 		this.minSdkVersion = minSdkVersion;
 		this.targetSdkVersion = targetSdkVersion;
 		this.iconPath = iconPath;
 		this.title = title;
 		this.author = author;
 		this.version = version;
 		this.inputTypes = inputTypes;
 		this.moduleDependencies = moduleDependencies;
 		this.optionalAll = optionalAll;
 	}
 	
 	
 
 	@Override
 	public String toString() {
 		return "ModuleMetaData [packageName=" + packageName + ", className="
 				+ className + ", minSdkVersion=" + minSdkVersion
 				+ ", targetSdkVersion=" + targetSdkVersion + ", iconPath="
 				+ iconPath + ", title=" + title + ", author=" + author
 				+ ", version=" + version + ", inputTypes=" + inputTypes
 				+ ", moduleDependencies=" + moduleDependencies + "]";
 	}
 
 
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((author == null) ? 0 : author.hashCode());
 		result = prime * result
 				+ ((className == null) ? 0 : className.hashCode());
 		result = prime * result
 				+ ((iconPath == null) ? 0 : iconPath.hashCode());
 		result = prime * result
 				+ ((inputTypes == null) ? 0 : inputTypes.hashCode());
 		result = prime * result
 				+ ((minSdkVersion == null) ? 0 : minSdkVersion.hashCode());
 		result = prime
 				* result
 				+ ((moduleDependencies == null) ? 0 : moduleDependencies
 						.hashCode());
 		result = prime * result
 				+ ((packageName == null) ? 0 : packageName.hashCode());
 		result = prime
 				* result
 				+ ((targetSdkVersion == null) ? 0 : targetSdkVersion.hashCode());
 		result = prime * result + ((title == null) ? 0 : title.hashCode());
 		result = prime * result + ((version == null) ? 0 : version.hashCode());
 		return result;
 	}
 
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		ModuleMetaData other = (ModuleMetaData) obj;
 		if (author == null) {
 			if (other.author != null)
 				return false;
 		} else if (!author.equals(other.author))
 			return false;
 		if (className == null) {
 			if (other.className != null)
 				return false;
 		} else if (!className.equals(other.className))
 			return false;
 		if (iconPath == null) {
 			if (other.iconPath != null)
 				return false;
 		} else if (!iconPath.equals(other.iconPath))
 			return false;
 		if (inputTypes == null) {
 			if (other.inputTypes != null)
 				return false;
 		} else if (!inputTypes.equals(other.inputTypes))
 			return false;
 		if (minSdkVersion == null) {
 			if (other.minSdkVersion != null)
 				return false;
 		} else if (!minSdkVersion.equals(other.minSdkVersion))
 			return false;
 		if (moduleDependencies == null) {
 			if (other.moduleDependencies != null)
 				return false;
 		} else if (!moduleDependencies.equals(other.moduleDependencies))
 			return false;
 		if (packageName == null) {
 			if (other.packageName != null)
 				return false;
 		} else if (!packageName.equals(other.packageName))
 			return false;
 		if (targetSdkVersion == null) {
 			if (other.targetSdkVersion != null)
 				return false;
 		} else if (!targetSdkVersion.equals(other.targetSdkVersion))
 			return false;
 		if (title == null) {
 			if (other.title != null)
 				return false;
 		} else if (!title.equals(other.title))
 			return false;
 		if (version == null) {
 			if (other.version != null)
 				return false;
 		} else if (!version.equals(other.version))
 			return false;
 		return true;
 	}
 
 
 	public String getPackageName() {
 		return packageName;
 	}
 
     public String getClassName() {
         return className;
     }
 
 	public String getMinSdkVersion() {
 		return minSdkVersion;
 	}
 
 	public String getTargetSdkVersion() {
 		return targetSdkVersion;
 	}
 
 	public String getIconPath() {
 		return iconPath;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public String getAuthor() {
 		return author;
 	}
 
 	public String getVersion() {
 		return version;
 	}
 
 	public Map<String, DependencyType> getInputTypes() {
 		return inputTypes;
 	}
 
 	public Map<String, DependencyType> getModuleDependencies() {
 		return moduleDependencies;
 	}
 
     public String getJarFileName() {
         return jarFileName;
     }
 
 	public boolean getOptionalAll() {
 		return optionalAll;
 	}
 
 	// DEBUG PURPOSES ONLY
 	
 	public void setPackageName(String packageName) {
 		this.packageName = packageName;
 	}
 
     public void setClassName(String className) {
         this.className = className;
     }
 
 	public void setMinSdkVersion(String minSdkVersion) {
 		this.minSdkVersion = minSdkVersion;
 	}
 
 	public void setTargetSdkVersion(String targetSdkVersion) {
 		this.targetSdkVersion = targetSdkVersion;
 	}
 
 	public void setIconPath(String iconPath) {
 		this.iconPath = iconPath;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public void setAuthor(String author) {
 		this.author = author;
 	}
 
 	public void setVersion(String version) {
 		this.version = version;
 	}
 
 	public void setInputTypes(Map<String, DependencyType> inputTypes) {
 		this.inputTypes = inputTypes;
 	}
 
 	public void setModuleDependencies(Map<String, DependencyType> moduleDependencies) {
 		this.moduleDependencies = moduleDependencies;
 	}
 
     public void setJarFileName(String name) {
         this.jarFileName = name;
     }
 }
 
 
