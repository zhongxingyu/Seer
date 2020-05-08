 package de.hh.changeRing.calendar;
 
 import com.google.common.collect.Lists;
 import de.hh.changeRing.BaseEntity;
 import de.hh.changeRing.Context;
import de.hh.changeRing.infrastructure.eclipselink.MappingCustomizer;
 import de.hh.changeRing.user.User;
import org.eclipse.persistence.annotations.Customizer;
 import org.joda.time.DateTime;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EntityManager;
 import javax.persistence.Enumerated;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.TypedQuery;
 import java.util.List;
 
 import static de.hh.changeRing.calendar.EventModel.TimeFilter;
 import static de.hh.changeRing.calendar.EventModel.TimeFilter.future;
 import static de.hh.changeRing.calendar.EventType.individual;
 import static javax.persistence.EnumType.STRING;
 
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
 @NamedQueries({
 		@NamedQuery(name = "findFilteredAndOrderedFutureEvents",
 				query = "select event from Event event where event.when >= :relevant and event.eventType in :filter order by event.when"),
 		@NamedQuery(name = "findFilteredAndOrderedPastEvents",
 				query = "select event from Event event where event.when <= :relevant and event.eventType in :filter order by event.when desc")
 })

 public class Event extends BaseEntity {
 	@SuppressWarnings("JpaDataSourceORMInspection")
 	@ManyToOne
 	@JoinColumn(name = "user_id", nullable = false)
 	private User user;
 
 	@SuppressWarnings("JpaDataSourceORMInspection")
 	@Column(name = "event_date")
 	private DateTime when;
 
 	private Integer duration;
 
 	private String title;
 
 	@Column(length = 512)
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
 
 	public String getLocation() {
 		return location;
 	}
 
 	public void setLocation(String location) {
 		this.location = location;
 	}
 
 	public DateTime getWhen() {
 		return when;
 	}
 
 	public void setWhen(DateTime from) {
 		this.when = from;
 	}
 
 	public String getFormattedWhen() {
 		return Context.longFormatGermanDate(getWhen());
 	}
 
 	public String getHeadLine() {
 		return getFormattedWhen() + ' '
 				+ getDisplayTitle();
 	}
 
 	public String getPeriod() {
 		String result = Context.formatGermanTime(getWhen());
 		if (duration != null) {
 			result += " - " + Context.formatGermanTime(getWhen().plusMinutes(duration));
 		}
 		return result;
 	}
 
 	public void setDuration(int duration) {
 		this.duration = duration;
 	}
 
 	public String getDisplayTitle() {
 		return (eventType.equals(individual) ? getTitle() : (getEventType().translation + ' ' + getTitle()));
 	}
 
 	public String getDashboardDate() {
 		return getFormattedWhen();
 	}
 
 	public static List<Event> findEvents(EntityManager entityManager, TimeFilter timeFilter, List<EventType> typeFilter, int count) {
 		if (typeFilter.isEmpty()) {
 			return Lists.newArrayList();
 		}
 		return queryEvents(entityManager, timeFilter, typeFilter).setMaxResults(count).getResultList();
 	}
 
 	public static List<Event> findEvents(EntityManager entityManager, TimeFilter timeFilter, List<EventType> typeFilter) {
 		if (typeFilter.isEmpty()) {
 			return Lists.newArrayList();
 		}
 		return queryEvents(entityManager, timeFilter, typeFilter).getResultList();
 	}
 
 	private static TypedQuery<Event> queryEvents(EntityManager entityManager, TimeFilter timeFilter, List<EventType> typeFilter) {
 		return (timeFilter.equals(future)
 				? entityManager.createNamedQuery("findFilteredAndOrderedFutureEvents", Event.class)
 				: entityManager.createNamedQuery("findFilteredAndOrderedPastEvents", Event.class))
 				.setParameter("relevant", timeFilter.relevant())
 				.setParameter("filter", typeFilter);
 	}
 }
