 package de.hh.changeRing.calendar;
 
 import static de.hh.changeRing.calendar.EventType.individual;
 import static java.util.Calendar.MINUTE;
 import static javax.persistence.EnumType.STRING;
 import static javax.persistence.TemporalType.TIMESTAMP;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import javax.persistence.*;
 
 import de.hh.changeRing.BaseEntity;
 import de.hh.changeRing.Context;
 import de.hh.changeRing.user.User;
 
 /**
  * ----------------GNU General Public License--------------------------------
  * <p/>
  * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
  * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * <p/>
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * <p/>
  * You should have received a copy of the GNU General Public License along with this program. If not, see
  * <http://www.gnu.org/licenses/>.
  * <p/>
  * ----------------in addition-----------------------------------------------
  * <p/>
  * In addition, each military use, and the use for interest profit will be excluded. Environmental damage caused by the
  * use must be kept as small as possible.
  */
 @Entity
 public class Event extends BaseEntity {
 	@SuppressWarnings("JpaDataSourceORMInspection")
 	@ManyToOne
 	@JoinColumn(name = "user_id", nullable = false)
 	private User user;
 
 	@Temporal(TIMESTAMP)
 	private Date when;
 
 	private Integer duration;
 
 	private String title;
 
 	private String content;
 
 	private String location;
 
 	@Enumerated(STRING)
 	private EventType eventType;
 
 	public User getUser() {
 		return user;
 	}
 
 	public void setUser(User user) {
 		this.user = user;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getContent() {
 		return content;
 	}
 
 	public void setContent(String content) {
 		this.content = content;
 	}
 
 	public EventType getEventType() {
 		return eventType;
 	}
 
 	public void setEventType(EventType eventType) {
 		this.eventType = eventType;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getLocation() {
 		return location;
 	}
 
 	public void setLocation(String location) {
 		this.location = location;
 	}
 
 	public Date getWhen() {
 		return when;
 	}
 
 	public void setWhen(Date from) {
 		this.when = from;
 	}
 
 	public String getFormattedWhen() {
 		return Context.formatGermanDate(getWhen());
 	}
 
 	public String getHeadLine() {
 		return getFormattedWhen() + ' '
 				+ (eventType.equals(individual) ? getTitle() : (getEventType().translation + ' ' + getTitle()));
 	}
 
     public String getPeriod(){
         String result = Context.formatGermanTime(getWhen());
         if (duration!= null){
             GregorianCalendar endTime = new GregorianCalendar();
             endTime.setTime(getWhen());
             endTime.add(MINUTE,duration);
             result += " - " + Context.formatGermanTime(endTime.getTime());
         }
         return result;
     }
 
     public void setDuration(int duration) {
         this.duration = duration;
     }
 }
