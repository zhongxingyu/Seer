 package org.genedb.crawl.model;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 
 @XStreamAlias("file")
 public class FileInfo  {
 	
	private int id;
 	private String file;
 	private String meta;
 	
	public FileInfo(int id, String file, String meta) {
		this.id = id;
 		this.file = file;
 		this.meta = meta;
 	}
 	
 	
 }
