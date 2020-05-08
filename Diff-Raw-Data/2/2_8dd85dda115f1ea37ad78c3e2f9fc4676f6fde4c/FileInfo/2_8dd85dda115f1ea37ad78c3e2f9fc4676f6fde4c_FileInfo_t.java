 package org.lwjgl.util.applet;
 
 public class FileInfo {
 
 	private int contentLength;
 
 	private long lastModified;
 
 	private String fileName;
 
 	public FileInfo(String fileName, int contentLength, long lastModified) {
 		super();
 		this.setFileName(fileName);
 		this.setContentLength(contentLength);
 		this.setLastModified(lastModified);
 	}
 	
 	public String getNameWithoutExtension() {
		return getFileName().substring(0, getFileName().indexOf(".jar"));
 	}
 
 	void setFileName(String fileName) {
 		this.fileName = fileName;
 	}
 
 	String getFileName() {
 		return fileName;
 	}
 
 	void setLastModified(long lastModified) {
 		this.lastModified = lastModified;
 	}
 
 	long getLastModified() {
 		return lastModified;
 	}
 
 	void setContentLength(int contentLength) {
 		this.contentLength = contentLength;
 	}
 
 	int getContentLength() {
 		return contentLength;
 	}
 	
 }
