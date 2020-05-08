 package models;
 
 import java.util.*;
 import javax.persistence.*;
 
 import play.db.ebean.*;
 import play.data.validation.*;
 
 @Entity
 public class Rule extends Model {

 	@Id
 	public Long id;
 
 	public Date start;

 	public Date finish;

 	public static Finder<Long, Rule> find = new Finder<Long, Rule>(Long.class, Rule.class);

 	public Rule() {
 		// TODO Auto-generated constructor stub
 	}

 	public Rule(Date start, Date finish) {
 		this.start = start;
 		this.finish = finish;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public Date getStart() {
 		return start;
 	}
 
 	public void setStart(Date start) {
 		this.start = start;
 	}
 
 	public Date getFinish() {
 		return finish;
 	}
 
 	public void setFinish(Date finish) {
 		this.finish = finish;
 	}

 	// Additional logic
 	public boolean canVote() {
 		Date now = new Date();
 		Date start = this.getStart();
 		Date finish = this.getFinish();
 		if (now.after(start) && now.before(finish)) {
 			return true;
 		}
 		else {
 			return false;
 		}
 	}

	public String startString() {
		return start.getDate() + "/" + start.getMonth() + "/" + (start.getYear() + 1900) + " at " + start.getHours() + ":" + start.getMinutes() + ":" + start.getSeconds();
	}

 }
