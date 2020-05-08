 package org.ow2.jonas.azure.pastebean.model;
 
 import java.io.Serializable;
 import java.lang.String;
 import java.util.Date;
 import javax.persistence.*;
 
 /**
  * Entity implementation class for Entity: Paste
  *
  */
 @Entity
 @NamedQueries({
 	@NamedQuery(name = "all",
            query = "SELECT * FROM Paste"),
     @NamedQuery(name = "from_hash",
            query = "SELECT * FROM Paste p WHERE p.hash LIKE ':pattern%'")

 })
 public class Paste implements Serializable {
 
	   
 	@Id
 	private String hash;
 	private String author;
 	private String description;
	private String content;
	private Date creationDate;
	private static final long serialVersionUID = 1L;
 
 	public Paste() {
 		super();
 	}   
 	public String getHash() {
 		return this.hash;
 	}
 
 	public void setHash(String hash) {
 		this.hash = hash;
 	}   
 	public String getAuthor() {
 		return this.author;
 	}
 
 	public void setAuthor(String name) {
 		this.author = name;
 	}   
 	public String getDescription() {
 		return this.description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}   
 	public String getContent() {
 		return this.content;
 	}
 
 	public void setContent(String content) {
 		this.content = content;
 	}   
 	public Date getCreationDate() {
 		return this.creationDate;
 	}
 
 	public void setCreationDate(Date creationDate) {
 		this.creationDate = creationDate;
 	}
    
 }
