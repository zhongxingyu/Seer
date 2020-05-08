 /*
     This file is part of SchedVis.
 
     SchedVis is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     SchedVis is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with SchedVis.  If not, see <http://www.gnu.org/licenses/>.
 
  */
 /**
  */
 package cz.muni.fi.spc.SchedVis.model.entities;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.Entity;
 import javax.persistence.EntityManager;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 import cz.muni.fi.spc.SchedVis.model.BaseEntity;
 import cz.muni.fi.spc.SchedVis.model.Database;
 
 /**
  * @author Lukáš Petrovický <petrovicky@mail.muni.cz>
  * 
  */
 @Entity
 public class Event extends BaseEntity {
 
 	@SuppressWarnings("unchecked")
 	public static List<Integer> getAllTicks() {
 		EntityManager em = Database.newEntityManager();
 		final List<Integer> l = ((Session) em.getDelegate())
 		    .createSQLQuery(
		        "SELECT DISTINCT clock FROM Event WHERE parent_FK IS NULL ORDER BY clock ASC")
 		    .list();
 		em.close();
 		return l;
 	}
 
 	public static Event getFirst() {
 		EntityManager em = Database.newEntityManager();
 		final Criteria crit = BaseEntity.getCriteria(em, Event.class, true);
 		crit.addOrder(Order.asc("clock"));
 		crit.add(Restrictions.isNull("parent"));
 		crit.setMaxResults(1);
 		Event evt = (Event) crit.uniqueResult();
 		em.close();
 		return evt;
 	}
 
 	public static Event getLast() {
 		EntityManager em = Database.newEntityManager();
 		final Criteria crit = BaseEntity.getCriteria(em, Event.class, true);
 		crit.addOrder(Order.desc("clock"));
 		crit.add(Restrictions.isNull("parent"));
 		crit.setMaxResults(1);
 		Event evt = (Event) crit.uniqueResult();
 		em.close();
 		return evt;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static Integer getMaxJobSpan() {
 		EntityManager em = Database.newEntityManager();
 		final List<Integer> l = ((Session) em.getDelegate())
 		    .createSQLQuery(
 		        "SELECT max(expectedEnd - clock) AS s FROM Event GROUP BY parent_fk, sourceMachine_id ORDER BY s DESC LIMIT 1")
 		    .list();
 		em.close();
 		return l.get(0);
 	}
 
 	public static Event getNext(final Integer eventId) {
 		EntityManager em = Database.newEntityManager();
 		final Criteria crit = BaseEntity.getCriteria(em, Event.class, true);
 		crit.addOrder(Order.asc("id"));
 		crit.add(Restrictions.isNull("parent"));
 		crit.add(Restrictions.gt("clock", eventId));
 		crit.setMaxResults(1);
 		Event evt = (Event) crit.uniqueResult();
 		em.close();
 		return evt;
 	}
 
 	public static Event getPrevious(final Integer eventId) {
 		EntityManager em = Database.newEntityManager();
 		final Criteria crit = BaseEntity.getCriteria(em, Event.class, true);
 		crit.addOrder(Order.desc("id"));
 		crit.add(Restrictions.isNull("parent"));
 		crit.add(Restrictions.lt("clock", eventId));
 		crit.setMaxResults(1);
 		Event evt = (Event) crit.uniqueResult();
 		em.close();
 		return evt;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static Integer getTickCount() {
 		EntityManager em = Database.newEntityManager();
 		final List<Integer> l = ((Session) em.getDelegate()).createSQLQuery(
 		    "SELECT DISTINCT clock FROM Event WHERE parent_fk IS NULL").list();
 		em.close();
 		return l.size();
 	}
 
 	private Integer id;
 	private EventType eventType;
 	private Machine srcMachine;
 	private Machine dstMachine;
 	private Integer clock;
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
 
 	public void addChild(final Event e) {
 		this.events.add(e);
 	}
 
 	public String getAssignedCPUs() {
 		return this.assignedCPUs;
 	}
 
 	public Integer getClock() {
 		return this.clock;
 	}
 
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
 	@JoinColumn(name = "parent_fk")
 	public Event getParent() {
 		return this.parent;
 	}
 
 	@OneToOne
 	public Machine getSourceMachine() {
 		return this.srcMachine;
 	}
 
 	@OneToOne
 	public Machine getTargetMachine() {
 		return this.dstMachine;
 	}
 
 	@ManyToOne
 	public EventType getType() {
 		return this.eventType;
 	}
 
 	public void removeChild(final Event e) {
 		this.events.remove(e);
 	}
 
 	public void setAssignedCPUs(final String value) {
 		this.assignedCPUs = value;
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
 
 }
