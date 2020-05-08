 package edu.caltech.cs141b.hw2.gwt.collab.shared;
 
 import com.google.gwt.user.client.rpc.IsSerializable;
 
 /**
  * Used to contain the entire document, without any locking primitives.
  */
 public class UnlockedDocument extends AbstractDocument implements IsSerializable {
 	
 	// Required by GWT serialization.
 	public UnlockedDocument() {
		super(null, null, null);
 	}
 	
 	public UnlockedDocument(String key, String title, String contents) {
 		super(key, title, contents);
 	}
 	
 	public boolean equals(UnlockedDocument b) {
 		return (key.equals(b.getKey()) &&
 				title.equals(b.getTitle()) &&
 				contents.equals(b.getContents()));
 	}
 	
 }
 
