 package models;
 
 import java.util.*;
 
 import javax.persistence.*;
 import models.enums.*;
 import play.data.validation.*;
 import play.db.jpa.Model;
 
 @Entity
 @Table(name = "StUser")
 public class User extends Model {
 
 	@Required
 	@Unique
 	public String username;
 
 	public String firstName;
 
 	public String lastName;
 
 	public Date birthdate;
 
 	@Required
 	public String password;
 
 	@Email
 	@Required
 	public String email;
 
 	public String about;
 
 	@Required
 	public Country country;
 
 	public String city;
 
 	@ElementCollection
 	@Enumerated
 	public List<Language> languages;
 
 	@OneToMany(cascade = CascadeType.ALL, mappedBy = "author")
 	public List<UniversalPost> posts;
 
 	@OneToOne(cascade = CascadeType.ALL)
 	public ActivityHistory history;
 
 	@Enumerated(EnumType.STRING)
 	public Role role;
 
 	@Enumerated(EnumType.STRING)
 	public UserStatus status;
 
 	@Enumerated(value = EnumType.STRING)
 	public EducationLevel educationLevel;
 
 	@OneToOne(cascade = CascadeType.ALL)
 	public Rating userRating;
 
 	@OneToOne(cascade = CascadeType.ALL)
 	public Rating postRating;
 
 	@OneToMany(cascade = CascadeType.ALL, mappedBy = "author")
 	public List<PortfolioObject> portfolio;
 
 	@OneToMany(cascade = CascadeType.ALL, mappedBy = "receiver")
 	public List<Feedback> feedbacks;
 
 	@OneToOne(cascade = CascadeType.ALL)
 	public ReportState reportState;
 
 	public User() {
 		history = new ActivityHistory();
 
 		postRating = new Rating();
 		userRating = new Rating();
 
 		portfolio = new ArrayList<PortfolioObject>();
 		feedbacks = new ArrayList<Feedback>();
 
 		reportState = new ReportState();
 
 		status = UserStatus.ACTIVE;
 		role = Role.USER;
 	}
 }
