 package org.genedb.crawl.model;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 
 @XStreamAlias("file")
 public class FileInfo  {
 	
	private int fileID;
 	private String file;
 	private String meta;
 	
	public FileInfo(int fileID, String file, String meta) {
		this.fileID = fileID;
 		this.file = file;
 		this.meta = meta;
 	}
 	
 	
 }
