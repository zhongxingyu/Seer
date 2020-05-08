 package models;
 
 import java.util.Date;
 
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 
 import org.hibernate.annotations.OnDelete;
 import org.hibernate.annotations.OnDeleteAction;
 
 import play.data.validation.Required;
 import play.db.jpa.Model;
 
 @Entity
 public class Issue extends Model {
 	
 //	@Required
 	@ManyToOne
 	@OnDelete(action=OnDeleteAction.CASCADE)
 	public Project project;
 //	@Required
 	@ManyToOne
 	public Tracker tracker;
 	@Required
 	public String subject;
 //	@MaxSize 2000
 	public String description;
 	@ManyToOne
 	public User assignee;
 	public Date updated;
 //	@Required
 	@ManyToOne
 	public State state;
 //	@Required
 	@ManyToOne
 	public Enumeration priority;
 	
 	public Issue(Project project) {
 		this.project = project;
 		this.state = State.find("isDefault = ?", true).first();
 		this.priority = Enumeration.find("type = ? and is_default = ?", Enumeration.ISSUE_PRIORITY_TYPE, true).first();
 	}
 
    
 }
