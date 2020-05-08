 package edu.caltech.cs141b.hw2.gwt.collab.shared;
 
 import java.util.Date;
 
 import com.google.gwt.user.client.rpc.IsSerializable;
 
 /**
  * Used to contain the entire document along with the locking primitives
  * necessary to modify it.
  */
 public class LockedDocument extends AbstractDocument implements IsSerializable {
 
 	private String lockedBy = null;
 	private Date lockedUntil = null;
 	
 	// Required by GWT serialization.
 	public LockedDocument() {
		super(null, null, null);
 	}
 	
 	public LockedDocument(String lockedBy, Date lockedUntil, String key,
 			String title, String contents) {
 		super(key, title, contents);
 		this.lockedBy = lockedBy;
 		this.lockedUntil = lockedUntil;
 	}
 	
 	public void setTitle(String documentTitle) {
 		this.title = documentTitle;
 	}
 
 	public void setContents(String documentContents) {
 		this.contents = documentContents;
 	}
 
 	public String getLockedBy() {
 		return lockedBy;
 	}
 
 	public Date getLockedUntil() {
 		return lockedUntil;
 	}
 	
 	public UnlockedDocument unlock() {
 		return new UnlockedDocument(key, title, contents);
 	}
 }
 
