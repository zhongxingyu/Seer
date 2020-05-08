 package com.cs456.project.common;
 
 import java.io.Serializable;
 
 @SuppressWarnings("serial")
 public class FileListObject implements Serializable, Comparable<FileListObject> {
 	private String displayName;
 	private boolean isDirectory;
 	private boolean isDeleteOnly = false;
 	private boolean isMyFile;
 	private boolean isShared;
 
 	public FileListObject(String filePath, String rootDirectory, boolean isMyFile, boolean isShared) {		
 		String temp = filePath.substring(rootDirectory.length(), filePath.length());
 		
 		this.isMyFile = isMyFile;
		this.isShared = isShared;
 		
 		if(temp.contains("\\")) {
 			isDirectory = true;
 			displayName = temp.substring(0, temp.indexOf("\\"));
 		}
 		else {
 			isDirectory = false;
 			displayName = temp;
 			
 			if(filePath.endsWith(".part")) {
 				isDeleteOnly = true;
 			}			
 		}
 	}
 	
 	public boolean isShared() {
 		return this.isShared;
 	}
 	
 	public boolean isMyFile() {
 		return this.isMyFile;
 	}
 	
 	public void setDisplayName(String displayName) {
 		this.displayName = displayName;
 	}
 	
 	public boolean isDeleteOnly() {
 		return this.isDeleteOnly;
 	}
 
 	public boolean isDirectory() {
 		return this.isDirectory;
 	}
 	
 	public String getDisplayName() {
 		return this.displayName;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if(obj == null) return false;
 		if(!(obj instanceof FileListObject)) return false;		
 		
 		FileListObject o = (FileListObject)obj;
 		
 		return (displayName != null && displayName.equals(o.displayName) && isDirectory == o.isDirectory);
 	}
 
 	@Override
 	public int hashCode() {
 		return displayName.hashCode();
 	}
 
 	@Override
 	public int compareTo(FileListObject o) {
 		return displayName.compareTo(o.displayName);
 	}
 	
 }
