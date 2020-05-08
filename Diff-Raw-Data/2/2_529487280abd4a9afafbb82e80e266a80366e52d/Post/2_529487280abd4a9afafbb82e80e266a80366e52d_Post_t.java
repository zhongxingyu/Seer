 package models;
 
 import java.util.*;
 import javax.persistence.*;
 
 import play.db.ebean.*;
 import play.data.format.*;
 import play.data.validation.*;
 import com.avaje.ebean.common.BeanList;
 
 @Entity 
 public class Post extends AbstractModel {
 
 	public String title;
 
 	@Column(columnDefinition="TEXT")
 	public String body;
 
 	@ManyToOne
 	public User author;
 
	@ManyToMany(cascade = CascadeType.ALL)
 	public List<Tag> tags = new ArrayList<Tag>();
 
 
 }
