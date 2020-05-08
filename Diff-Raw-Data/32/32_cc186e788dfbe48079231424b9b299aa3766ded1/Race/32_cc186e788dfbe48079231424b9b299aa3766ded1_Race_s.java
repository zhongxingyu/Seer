 /*
 	cursus - Race series management program
 	Copyright 2011  Simon Arlott
 
 	This program is free software: you can redistribute it and/or modify
 	it under the terms of the GNU General Public License as published by
 	the Free Software Foundation, either version 3 of the License, or
 	(at your option) any later version.
 
 	This program is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 	GNU General Public License for more details.
 
 	You should have received a copy of the GNU General Public License
 	along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package eu.lp0.cursus.db.data;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 
 import eu.lp0.cursus.util.Constants;
 
 /**
  * Race
  */
 @Entity(name = "race")
 @Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "event_id", "name" }) })
 public class Race extends AbstractEntity implements Comparable<Race>, RaceEntity {
 	Race() {
 	}
 
 	public Race(Event event) {
 		this(event, "", ""); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	public Race(Event event, String name) {
 		this(event, name, ""); //$NON-NLS-1$
 	}
 
 	public Race(Event event, String name, String description) {
 		setEvent(event);
 		setName(name);
 		setDescription(description);
 	}
 
 	private Event event;
 
 	@ManyToOne(optional = false)
 	@JoinColumn(name = "event_id", nullable = false)
 	public Event getEvent() {
 		return event;
 	}
 
 	public void setEvent(Event event) {
 		this.event = event;
 	}
 
 	private Integer eventOrder;
 
 	@Column(name = "event_order", nullable = false)
 	public Integer getEventOrder() {
 		return eventOrder != null ? eventOrder : getEvent().getRaces().size();
 	}
 
 	@SuppressWarnings("unused")
 	private void setEventOrder(int eventOrder) {
 		this.eventOrder = eventOrder;
 	}
 
 	private String name;
 
 	@Column(nullable = false, length = Constants.MAX_STRING_LEN)
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	private String description;
 
 	@Column(nullable = false, length = Constants.MAX_STRING_LEN)
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	private Map<Pilot, RaceAttendee> attendees = new HashMap<Pilot, RaceAttendee>();
 
 	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "race", orphanRemoval = true)
	@MapKey
	@Column(nullable = false)
 	public Map<Pilot, RaceAttendee> getAttendees() {
 		return attendees;
 	}
 
 	public void setAttendees(Map<Pilot, RaceAttendee> attendees) {
 		this.attendees = attendees;
 	}
 
 	@Override
 	public String toString() {
 		return getName().length() > 0 ? getName() : "[#" + getId() + "]"; //$NON-NLS-1$ //$NON-NLS-2$ 
 	}
 
 	@Override
 	public int compareTo(Race o) {
 		if (this == o) {
 			return 0;
 		}
 
 		int ret = getEvent().compareTo(o.getEvent());
 		if (ret != 0) {
 			return ret;
 		}
 
 		return Integer.valueOf(getEventOrder()).compareTo(o.getEventOrder());
 	}
 }
