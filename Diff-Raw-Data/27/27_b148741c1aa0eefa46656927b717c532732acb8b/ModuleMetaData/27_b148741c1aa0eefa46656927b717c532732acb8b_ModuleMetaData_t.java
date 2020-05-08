 /*
  * ModuleMetadata.java
  * Copyright (C) 2013 Andrew DeMaria (muff1nman) <ademaria@mines.edu>
  *
  * All Rights Reserved.
  */
 
 package edu.mines.acmX.exhibit.module_manager;
 
 import java.util.List;
 import java.util.Map;
 
 public class ModuleMetaData {
     // manifest top level
     private String packageName;
 
     // uses-sdk
     private String minSdkVersion, targetSdkVersion;
 
     // module definition
     private String iconPath, title, author, version;
 
     // inputs
    // document what boolean means
     Map<String, Boolean> inputTypes;
 
     // required modules
     List<String> requiredModules;
 
 
 
 	public ModuleMetaData(String packageName, String minSdkVersion,
 			String targetSdkVersion, String iconPath, String title,
 			String author, String version, Map<String, Boolean> inputTypes,
 			List<String> requiredModules) {
 		super();
 		this.packageName = packageName;
 		this.minSdkVersion = minSdkVersion;
 		this.targetSdkVersion = targetSdkVersion;
 		this.iconPath = iconPath;
 		this.title = title;
 		this.author = author;
 		this.version = version;
 		this.inputTypes = inputTypes;
 		this.requiredModules = requiredModules;
 	}
 
 	public String getPackageName() {
 		return packageName;
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
 
 	public Map<String, Boolean> getInputTypes() {
 		return inputTypes;
 	}
 
 	public List<String> getRequiredModules() {
 		return requiredModules;
 	}
 
 	// DEBUG PURPOSES ONLY
 	
 	public void setPackageName(String packageName) {
 		this.packageName = packageName;
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
 
 	public void setInputTypes(Map<String, Boolean> inputTypes) {
 		this.inputTypes = inputTypes;
 	}
 
 	public void setRequiredModules(List<String> requiredModules) {
 		this.requiredModules = requiredModules;
 	}
 }
 
 
