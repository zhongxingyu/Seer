 package models;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.Lob;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 
 import org.joda.time.DateTime;
 
 import play.db.jpa.Model;
 
 @Entity
 public class Event extends Model {
 
     public String name;
     public Date start;
     public Date end;
     public boolean isPublic;
     public Date lowerBound;
     public Date upperBound;
     public boolean isFollowable;
 
     @ManyToOne
     public User owner;
 
     @Lob
     public String note;
 
     @ManyToMany
     public List<Calendar> calendars;
 
     public Event(String name, String note, Date start, Date end, User owner,
 	    Calendar calendar) {
 	this.name = name;
 	this.note = note;
 	this.start = start;
 	this.end = end;
 	this.owner = owner;
 
 	this.lowerBound = makeLowerBound(start);
 	this.upperBound = makeUpperBound(end);
 
 	this.calendars = new ArrayList<Calendar>();
 	this.calendars.add(calendar);
 
     }
 
     public Event(String name, String note, Date start, Date end, User owner,
 	    Calendar calendar, boolean isPublic, boolean isFollowable) {
 	this(name, note, end, end, owner, calendar);
 	this.isPublic = isPublic;
 	this.isFollowable = isFollowable;
     }
 
     public void setStart(Date start) {
 	this.start = start;
 	this.lowerBound = makeLowerBound(start);
     }
 
     public void setEnd(Date end) {
 	this.end = end;
 	this.upperBound = makeUpperBound(end);
     }
 
     public static Date makeLowerBound(Date startDate) {
 	return new DateTime(startDate).withTime(0, 0, 0, 0).toDate();
     }
 
     public static Date makeUpperBound(Date endDate) {
 	return new DateTime(endDate).withTime(23, 59, 59, 0).toDate();
     }
 
     public boolean happensOnDay(Date aDay) {
 	DateTime dayLowerBound = new DateTime(makeLowerBound(aDay));
 	DateTime dayUpperBound = new DateTime(makeUpperBound(aDay));
 	DateTime eventStart = new DateTime(this.lowerBound);
 	DateTime eventEnd = new DateTime(this.upperBound);
 	return (dayLowerBound.isAfter(eventStart) || dayLowerBound
 		.equals(eventStart))
 		&& (dayUpperBound.isBefore(eventEnd) || dayUpperBound
 			.equals(eventEnd));
     }
 
     public List<User> getAllFollowers() {
 	List<User> followers = new ArrayList<User>();
 	for (Calendar calendar : this.calendars) {
 	    if (!calendar.owner.equals(this.owner))
 		followers.add(calendar.owner);
 	}
 	return followers;
     }
 
     public void follow(Calendar calendar) {
 	this.calendars.add(calendar);
 	calendar.events.add(this);
     }

    public boolean isFollowedBy(User user) {
	for (Calendar calendar : this.calendars) {
	    if (calendar.owner.equals(user)) {
		return true;
	    }
	}
	return false;
    }
 }
