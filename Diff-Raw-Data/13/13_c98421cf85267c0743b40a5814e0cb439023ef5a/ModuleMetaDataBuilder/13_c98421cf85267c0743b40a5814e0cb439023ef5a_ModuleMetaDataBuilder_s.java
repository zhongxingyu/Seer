 
 package edu.mines.acmX.exhibit.module_manager;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class ModuleMetaDataBuilder {
     // manifest top level
     private String packageName;
     private String className;
 
     // uses-sdk
     private String minSdkVersion, targetSdkVersion;
 
     // module definition
     private String iconPath, title, author, version;
 
     // inputs
     // document what boolean means
    Map<InputType, DependencyType> inputTypes;
 
     // required modules
    Map<String, DependencyType> moduleDependencies;
 
     public ModuleMetaDataBuilder() {
 		
 	}
 
     public ModuleMetaData build() {
         return new ModuleMetaData(
                 packageName,
                 className,
                 minSdkVersion,
                 targetSdkVersion,
                 iconPath,
                 title,
                 author,
                 version,
                 inputTypes,
                 moduleDependencies);
     }
 
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
 
 	public void setInputTypes(Map<InputType, DependencyType> inputTypes) {
 		this.inputTypes = inputTypes;
 	}
 
     public void addInputType(InputType inputType, DependencyType depend) {
        if ( inputTypes == null ) {
            inputTypes = new HashMap<InputType, DependencyType>();
        }
         inputTypes.put(inputType, depend);
     }
 
 	public void setModuleDependencies(Map<String, DependencyType> moduleDependencies) {
 		this.moduleDependencies = moduleDependencies;
 	}
 
     public void addModuleDependency(String input, DependencyType depend) {
        if ( moduleDependencies == null ) {
            moduleDependencies = new HashMap<String, DependencyType>();
        }
         moduleDependencies.put(input, depend);
     }
 }
 
 
