 package models.com.contactmanager;
 
 import java.util.Date;
 import java.util.List;
 
import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToOne;
 
 import play.data.validation.Constraints;
 import play.db.ebean.Model;
 
 @Entity
 public class Profile extends Model {
 
 	@Id
 	public String id;
 	@Constraints.Required
 	public String name;
 	@Constraints.Required
 	public String login_name;
 
 	public String title;
 
 	@Constraints.Required
 	public Date joined_date;
 
 	@Constraints.Required
 	public Date last_visited_date;
 
 	public Date last_attended_date;
 
 	@Constraints.Required
 	public boolean is_intro;
 
 	@Constraints.Required
 	public boolean is_photo;
 
 	@Constraints.Required
 	public boolean is_assistant_organizer;
 
 	@Constraints.Required
 	public String mailing_list_type;
 
 	public String url;
 
 	@OneToOne(optional = false)
 	@JoinColumn(name = "rsvp_id")
 	public Rsvp rsvp;
 
	@ManyToOne(optional = false, cascade = CascadeType.PERSIST)
 	@JoinColumn(name = "company_id")
 	public Company company;
 
 	public static Model.Finder<Long, Profile> find = new Finder<Long, Profile>(
 			Long.class, Profile.class);
 
 	public static List<Profile> findAll() {
 		return find.all();
 	}
 
 	public static void save(Profile profile) {
		//profile.company.save();
 		profile.rsvp.save();
 		profile.save();
 	}
 
 	public static void update(Profile profile) {
		//profile.getCompany().update();
 		profile.getRsvp().update();
 		profile.update();
 	}
 
 	@Override
 	public boolean equals(Object object) {
 		if (object == null) {
 			return false;
 		}
 		if (object == this) {
 			return true;
 		}
 		if (!(object instanceof Profile)) {
 			return false;
 		}
 		Profile profile = (Profile) object;
 		if (profile.id != null && profile.id.trim().equalsIgnoreCase(this.id)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	@Override
 	public int hashCode() {
 		if (this.name != null && this.name.length() > 0) {
 			return this.name.length();
 		} else {
 			return 0;
 		}
 	}
 
 	public Rsvp getRsvp() {
 		return rsvp;
 	}
 
 	public void setRsvp(Rsvp rsvp) {
 		this.rsvp = rsvp;
 	}
 
 	public Company getCompany() {
 		return company;
 	}
 
 	public void setCompany(Company company) {
 		this.company = company;
 	}
 
 	public Profile(String id) {
 		super();
 		this.id = id;
 	}
 
 }
