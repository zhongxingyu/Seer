 /*
  * This file is part of SchedVis.
  * 
  * SchedVis is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * SchedVis is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * SchedVis. If not, see <http://www.gnu.org/licenses/>.
  */
 /**
  */
 package cz.muni.fi.spc.SchedVis.model.entities;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.annotations.Index;
 import org.hibernate.annotations.Table;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 import cz.muni.fi.spc.SchedVis.model.BaseEntity;
 import cz.muni.fi.spc.SchedVis.util.Database;
 
 /**
  * JPA Entity that represents a single event.
  * 
  * One parameter of particular interest is "clock", which is a tick on the
  * timeline of the schedule. The whole application renders schedule snapshots in
  * time based on this parameter.
  * 
  * @author Lukáš Petrovický <petrovicky@mail.muni.cz>
  */
 @Entity
 @Table(appliesTo = "Event", indexes = { @Index(name = "multiIndex", columnNames = {
    "sourceMachine_id", "parent_fk" }) })
 public final class Event extends BaseEntity implements Comparable<Event> {
 
 	public static final Integer JOB_HINT_NONE = 0;
 	public static final Integer JOB_HINT_MOVE_OK = 1;
 	public static final Integer JOB_HINT_MOVE_NOK = 2;
 	public static final Integer JOB_HINT_ARRIVAL = 3;
 
 	private static final Map<Integer, Integer> clocksByVirtualClock = new HashMap<Integer, Integer>();
 
 	private static Event firstEvent = null;
 	private static Event lastEvent = null;
 	/**
 	 * Whether or not there exists an event with a given clock value.
 	 * 
 	 * @param clock
 	 *          The clock value in question.
 	 * @return True if such clock exists, false otherwise.
 	 */
 	public static boolean existsTick(final Integer clock) {
 		final Criteria crit = BaseEntity.getCriteria(Event.class);
 		crit.add(Restrictions.eq("virtualClock", clock));
 		crit.setMaxResults(1);
 		Event evt = (Event) crit.uniqueResult();
 		return (evt != null);
 	}
 
 	/**
 	 * Get all existing values of the "clock" column.
 	 * 
 	 * @return All the existing ticks.
 	 */
 	@SuppressWarnings("unchecked")
 	public static Set<Integer> getAllTicks() {
 		return new TreeSet<Integer>(
 		    ((Session) Database.getEntityManager().getDelegate())
 		        .createSQLQuery(
 		            "SELECT DISTINCT virtualClock FROM Event WHERE parent_FK IS NULL ORDER BY clock ASC")
 		        .list());
 	}
 
 	/**
 	 * Get all the events in a given virtual clock.
 	 * 
 	 * @param virtualClockId
 	 *          The clock in question.
 	 * @return The events.
 	 */
 	@SuppressWarnings("unchecked")
 	public static List<Event> getEventsInTick(final int virtualClockId) {
 		Criteria crit = BaseEntity.getCriteria(Event.class);
 		crit.add(Restrictions.eq("virtualClock", virtualClockId));
 		crit.add(Restrictions.isNull("parent"));
 		return crit.list();
 	}
 
 	/**
 	 * Get first event on the timeline.
 	 * 
 	 * @return The event.
 	 */
 	public static Event getFirst() {
 		if (Event.firstEvent == null) {
 			final Criteria crit = BaseEntity.getCriteria(Event.class);
 			crit.addOrder(Order.asc("id"));
 			crit.add(Restrictions.isNull("parent"));
 			crit.setMaxResults(1);
 			Event.firstEvent = (Event) crit.uniqueResult();
 		}
 		return Event.firstEvent;
 	}
 
 	/**
 	 * Get the last event on the timeline.
 	 * 
 	 * @return The event.
 	 */
 	public static Event getLast() {
 		if (Event.lastEvent == null) {
 			final Criteria crit = BaseEntity.getCriteria(Event.class);
 			crit.addOrder(Order.desc("id"));
 			crit.add(Restrictions.isNull("parent"));
 			crit.setMaxResults(1);
 			Event.lastEvent = (Event) crit.uniqueResult();
 		}
 		return Event.lastEvent;
 	}
 
 	/**
 	 * Retrieve last clock with a given virtual clock id.
 	 * 
 	 * @param virtualClockId
 	 *          The id of the virtual clock in question.
 	 * @return The clock value.
 	 */
 	public static Integer getLastWithVirtualClock(final int virtualClockId) {
 		if (!Event.clocksByVirtualClock.containsKey(virtualClockId)) {
 			final Criteria crit = BaseEntity.getCriteria(Event.class);
 			crit.addOrder(Order.desc("id"));
 			crit.add(Restrictions.eq("virtualClock", virtualClockId));
 			crit.setMaxResults(1);
 			Event evt = (Event) crit.uniqueResult();
 			if (evt == null) {
 				Event.clocksByVirtualClock.put(virtualClockId, Event
 				    .getLastWithVirtualClock(1));
 			} else {
 				Event.clocksByVirtualClock.put(virtualClockId, evt.getClock());
 			}
 		}
 		return Event.clocksByVirtualClock.get(virtualClockId);
 	}
 
 	/**
 	 * Get the maximum length of a job.
 	 * 
 	 * @return The length in ticks.
 	 */
 	public static Integer getMaxJobSpan() {
 		return (Integer) ((Session) Database.getEntityManager().getDelegate())
 		    .createSQLQuery(
 		        "SELECT max(expectedEnd - clock) AS s FROM Event GROUP BY parent_fk, sourceMachine_id ORDER BY s DESC LIMIT 1")
 		    .list().get(0);
 	}
 
 	/**
 	 * Get the event that immediately follows the specified event.
 	 * 
 	 * @param eventId
 	 *          ID of the event in question.
 	 * @return The next event.
 	 */
 	public static Event getNext(final Integer eventId) {
 		return Event.getNext(eventId, null);
 	}
 
 	/**
 	 * Get the event that immediately follows the specified event with relation to
 	 * the given machine..
 	 * 
 	 * @param eventId
 	 *          ID of the event in question.
 	 * @param m
 	 *          ID of the machine in question. If null, no machine is considered.
 	 * 
 	 * @return The next event.
 	 */
 	public static Event getNext(final Integer eventId, final Machine m) {
 		final Criteria crit = BaseEntity.getCriteria(Event.class);
 		crit.addOrder(Order.asc("id"));
 		crit.add(Restrictions.gt("virtualClock", eventId));
 		if (m != null) {
 			crit.add(Restrictions.or(Restrictions.eq("sourceMachine", m),
 			    Restrictions.eq("targetMachine", m)));
 		}
 		crit.setMaxResults(1);
 		Event evt = (Event) crit.uniqueResult();
 		if (evt == null) {
 			return Event.getLast();
 		}
 		return evt;
 	}
 
 	/**
 	 * Get the event that immediately preceeds the specified event.
 	 * 
 	 * @param eventId
 	 *          ID of the event in question.
 	 * @return The previous event.
 	 */
 	public static Event getPrevious(final Integer eventId) {
 		return Event.getPrevious(eventId, null);
 	}
 
 	/**
 	 * Get the event that immediately preceeds the specified event.
 	 * 
 	 * @param eventId
 	 *          ID of the event in question.
 	 * @param m
 	 *          ID of the machine in question. If null, no machine is considered.
 	 * 
 	 * @return The previous event.
 	 */
 	public static Event getPrevious(final Integer eventId, final Machine m) {
 		final Criteria crit = BaseEntity.getCriteria(Event.class);
 		crit.addOrder(Order.desc("id"));
 		crit.add(Restrictions.lt("virtualClock", eventId));
 		if (m != null) {
 			crit.add(Restrictions.or(Restrictions.eq("sourceMachine", m),
 			    Restrictions.eq("targetMachine", m)));
 		}
 		crit.setMaxResults(1);
 		Event evt = (Event) crit.uniqueResult();
 		if (evt == null) {
 			return Event.getFirst();
 		}
 		return evt;
 	}
 
 	private Integer id;
 	private EventType eventType;
 	private Machine srcMachine;
 	private Machine dstMachine;
 	private Integer clock;
 	private Integer virtualClock;
 	private Integer deadline;
 	private Integer expectedEnd;
 	private Integer expectedStart;
 	private Integer job;
 	private Integer neededCPUs;
 
 	private Integer neededHDD;
 
 	private Integer neededRAM;
 
 	private String neededPlatform;
 
 	private Event parent;
 
 	private String assignedCPUs;
 	private Set<Event> events = new HashSet<Event>();
 	private boolean bringsSchedule = false;
 
 	private Integer jobHint = Event.JOB_HINT_NONE;
 
 	public void addChild(final Event e) {
 		this.events.add(e);
 	}
 
 	@Override
 	public int compareTo(final Event o) {
 		return this.getId().compareTo(o.getId());
 	}
 
 	/**
 	 * Get CPUs assigned to a job.
 	 * 
 	 * @return A string containing integers (numbers of assigned CPUs) separated
 	 *         by commas.
 	 */
 	public String getAssignedCPUs() {
 		return this.assignedCPUs;
 	}
 
 	public boolean getBringsSchedule() {
 		return this.bringsSchedule;
 	}
 
 	public Integer getClock() {
 		return this.clock;
 	}
 
 	/**
 	 * Get deadline clock value for a job.
 	 * 
 	 * @return If this is -1, there is no deadline.
 	 */
 	public Integer getDeadline() {
 		return this.deadline;
 	}
 
 	public Integer getExpectedEnd() {
 		return this.expectedEnd;
 	}
 
 	public Integer getExpectedStart() {
 		return this.expectedStart;
 	}
 
 	@OneToMany(mappedBy = "parent")
 	public Set<Event> getChildren() {
 		return this.events;
 	}
 
 	@Id
 	@GeneratedValue
 	public Integer getId() {
 		return this.id;
 	}
 
 	public Integer getJob() {
 		return this.job;
 	}
 
 	/**
 	 * A "hint" to the renderer as to how to render the job. There is a
 	 * "just arrived" hint, a "moved good" hint and a "moved bad" hint.
 	 * 
 	 * @return The hint.
 	 */
 	public Integer getJobHint() {
 		return this.jobHint;
 	}
 
 	public Integer getNeededCPUs() {
 		return this.neededCPUs;
 	}
 
 	public Integer getNeededHDD() {
 		return this.neededHDD;
 	}
 
 	public String getNeededPlatform() {
 		return this.neededPlatform;
 	}
 
 	public Integer getNeededRAM() {
 		return this.neededRAM;
 	}
 
 	@ManyToOne
 	@Index(name = "pIndex")
 	@JoinColumn(name = "parent_fk")
 	public Event getParent() {
 		return this.parent;
 	}
 
 	@OneToOne
 	@Index(name = "mIndex")
 	public Machine getSourceMachine() {
 		return this.srcMachine;
 	}
 
 	@OneToOne
 	public Machine getTargetMachine() {
 		return this.dstMachine;
 	}
 
 	@ManyToOne
 	@Index(name = "tIndex")
 	public EventType getType() {
 		return this.eventType;
 	}
 
 	@Index(name = "vIndex")
 	public Integer getVirtualClock() {
 		return this.virtualClock;
 	}
 
 	public void removeChild(final Event e) {
 		this.events.remove(e);
 	}
 
 	public void setAssignedCPUs(final String value) {
 		this.assignedCPUs = value;
 	}
 
 	public void setBringsSchedule(final boolean value) {
 		this.bringsSchedule = value;
 	}
 
 	public void setClock(final Integer value) {
 		this.clock = value;
 	}
 
 	public void setDeadline(final Integer value) {
 		this.deadline = value;
 	}
 
 	public void setExpectedEnd(final Integer value) {
 		this.expectedEnd = value;
 	}
 
 	public void setExpectedStart(final Integer value) {
 		this.expectedStart = value;
 	}
 
 	protected void setChildren(final Set<Event> events) {
 		this.events = events;
 	}
 
 	public void setId(final Integer id) {
 		this.id = id;
 	}
 
 	public void setJob(final Integer value) {
 		this.job = value;
 	}
 
 	public void setJobHint(final Integer value) {
 		this.jobHint = value;
 	}
 
 	public void setNeededCPUs(final Integer value) {
 		this.neededCPUs = value;
 	}
 
 	public void setNeededHDD(final Integer value) {
 		this.neededHDD = value;
 	}
 
 	public void setNeededPlatform(final String value) {
 		this.neededPlatform = value;
 	}
 
 	public void setNeededRAM(final Integer value) {
 		this.neededRAM = value;
 	}
 
 	public void setParent(final Event parent) {
 		this.parent = parent;
 	}
 
 	public void setSourceMachine(final Machine machine) {
 		this.srcMachine = machine;
 	}
 
 	public void setTargetMachine(final Machine machine) {
 		this.dstMachine = machine;
 	}
 
 	public void setType(final EventType type) {
 		this.eventType = type;
 	}
 
 	public void setVirtualClock(final Integer value) {
 		this.virtualClock = value;
 	}
 
 }
