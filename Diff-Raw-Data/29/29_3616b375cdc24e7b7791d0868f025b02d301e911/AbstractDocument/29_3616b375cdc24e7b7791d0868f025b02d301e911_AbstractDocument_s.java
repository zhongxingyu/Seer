 package edu.caltech.cs141b.hw2.gwt.collab.shared;
 
public abstract class AbstractDocument {
 	protected String key = null;
 	protected String title = null;
 	protected String contents = null;
 	
 	public AbstractDocument(String key, String title, String contents) {
 		this.key = key;
 		this.title = title;
 		this.contents = contents;
 	}
 	
 	public boolean equals(AbstractDocument b) {
 		return (this.key.equals(b.getKey()) &&
 				this.title.equals(b.getTitle()) &&
 				this.contents.equals(b.getContents()));
 	}
 
 	public String getKey() {
 		return key;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public String getContents() {
 		return contents;
 	}
 }
