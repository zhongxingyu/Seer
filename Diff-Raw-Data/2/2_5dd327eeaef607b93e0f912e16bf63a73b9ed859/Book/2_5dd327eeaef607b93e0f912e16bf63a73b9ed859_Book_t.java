 package nl.obs.core.db.entity;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 @Entity
 @Table(name="Book")
 public class Book {
 		
     @Id    
     @GeneratedValue (strategy=GenerationType.AUTO)
     @Column(name="ID")
 	private int id; 
     
    @Column(name="imageurl") // dit is even "zomaar" een aanpassing...
 	private String imagebookurl;
     
     @Column
 	private String title;
     
     @Column
 	private int ISBNnumber;
         
     
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public String getImagebookurl() {
 		return imagebookurl;
 	}
 
 	public void setImagebookurl(String imagebookurl) {
 		this.imagebookurl = imagebookurl;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public int getISBNnumber() {
 		return ISBNnumber;
 	}
 
 	public void setISBNnumber(int iSBNnumber) {
 		ISBNnumber = iSBNnumber;
 	}
 
 }
