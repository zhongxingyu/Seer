 package models;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 
 import play.data.validation.Required;
 import play.db.jpa.Model;
 
 @Entity
 public class Task extends Model{
 	 public String title;
 	 
 	 @OneToMany(mappedBy="task", cascade=CascadeType.ALL)
 	 public List<Comment> comments;
 	 
 	 @OneToMany(mappedBy="task", cascade=CascadeType.ALL)
 	 public List<Postit> postits;
 	 
	 @ManyToOne
	 @Required
	 public Scene scene;
	 
 	 public Task(String title){
 		 this.title = title;
 		 this.comments = new ArrayList<Comment>();
 		 this.postits = new ArrayList<Postit>();
 	 }
 }
