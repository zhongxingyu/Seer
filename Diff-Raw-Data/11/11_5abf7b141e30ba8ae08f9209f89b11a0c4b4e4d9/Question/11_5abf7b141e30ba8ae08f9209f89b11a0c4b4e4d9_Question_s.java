 package models;
 
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.OrderBy;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.validation.Valid;
 
 import play.data.validation.Constraints.Required;
 import play.db.ebean.Model;
 
 import com.google.common.base.Strings;
 
 /**
  * Poll.
  * 
  * @author adericbourg
  * 
  */
 @Entity
 @Table(name = "question")
 public class Question extends Model {
 
 	private static final long serialVersionUID = 1L;
 
 	@Id
 	@Column(name = "id")
 	public Long id;
 
 	@Required(message = "Title is mandatory")
 	@Column(name = "title", nullable = false)
 	public String title;
 
 	@Column(name = "description")
 	public String description;
 
 	@Valid
 	@OneToMany(cascade = CascadeType.ALL)
 	@OrderBy("sortOrder ASC")
 	public List<QuestionChoice> choices;
 
 	@OneToMany(cascade = CascadeType.ALL)
 	public List<QuestionAnswer> answers;
 
 	@OneToOne(mappedBy = "question")
 	public Poll poll;
 
 	// ---
 
 	@Transient
 	public final boolean hasDescription() {
 		return !Strings.isNullOrEmpty(description);
 	}
 
 	@Transient
 	public final boolean hasRegisteredCreator() {
 		return poll.hasRegisteredCreator();
 	}
 }
