 package by.itransition.fanfic.domain;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 
@Entity
 public class Comment {
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private int id;
 	
 	@ManyToOne
 	private User author;
 	
 	@ManyToOne
 	private Fanfic fanfic;
 	
 	private String content;
 
 	public User getAuthor() {
 		return author;
 	}
 
 	public void setAuthor(User author) {
 		this.author = author;
 	}
 
 	public Fanfic getFanfic() {
 		return fanfic;
 	}
 
 	public void setFanfic(Fanfic fanfic) {
 		this.fanfic = fanfic;
 	}
 
 	public String getContent() {
 		return content;
 	}
 
 	public void setContent(String content) {
 		this.content = content;
 	}	
 }
