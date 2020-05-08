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
  * 
  */
 package cz.muni.fi.spc.SchedVis.model.entities;
 
 import java.util.List;
 import java.util.Vector;
 
 import javax.persistence.Entity;
 import javax.persistence.EntityManager;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 
 import org.hibernate.Criteria;
 import org.hibernate.annotations.Index;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 import cz.muni.fi.spc.SchedVis.model.BaseEntity;
 import cz.muni.fi.spc.SchedVis.model.Database;
 
 /**
  * @author Lukáš Petrovický <petrovicky@mail.muni.cz>
  * 
  */
 @Entity
 public class Machine extends BaseEntity {
 
	private static EventType[] machineEvents = new EventType[0];
 
 	@SuppressWarnings("unchecked")
 	public static List<Machine> getAll(final Integer groupId) {
 		EntityManager em = Database.newEntityManager();
 		final Criteria crit = BaseEntity.getCriteria(em, Machine.class, true);
 		if (groupId != null) {
 			crit.add(Restrictions.eq("group", MachineGroup.getWithId(groupId)));
 		} else {
 			crit.add(Restrictions.isNull("group"));
 		}
 		crit.addOrder(Order.asc("name"));
 		List<Machine> l = crit.list();
 		em.close();
 		return l;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static List<Machine> getAllGroupless() {
 		EntityManager em = Database.newEntityManager();
 		final Criteria crit = BaseEntity.getCriteria(em, Machine.class, false);
 		crit.addOrder(Order.asc("name"));
 		List<Machine> l = crit.list();
 		em.close();
 		return l;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static List<Event> getLatestSchedule(final Machine which,
 	    final Integer eventId) {
 		EntityManager em = Database.newEntityManager();
 		final Criteria crit = BaseEntity.getCriteria(em, Event.class, false);
 		crit.add(Restrictions.eq("sourceMachine", which));
 		crit.add(Restrictions.le("clock", eventId));
 		crit.add(Restrictions.isNotNull("parent"));
 		crit.addOrder(Order.desc("id"));
 		crit.setMaxResults(1);
 		final Event evt = (Event) crit.uniqueResult();
 		if (evt == null) {
 			em.close();
 			return new Vector<Event>();
 		}
 		Criteria crit2 = BaseEntity.getCriteria(em, Event.class, false);
 		crit2.add(Restrictions.eq("sourceMachine", which));
 		crit2.add(Restrictions.eq("parent", evt.getParent()));
 		crit2.addOrder(Order.asc("expectedStart"));
 		List<Event> l = crit2.list();
 		em.close();
 		return l;
 	}
 
 	public static Machine getWithId(final Integer id) {
 		return (Machine) Database.getSession().get(Machine.class, id);
 	}
 
 	public static Machine getWithName(final String name) {
 		EntityManager em = Database.newEntityManager();
 		final Criteria crit = BaseEntity.getCriteria(em, Machine.class, true);
 		crit.add(Restrictions.eq("name", name));
 		crit.setMaxResults(1);
 		Machine m = (Machine) crit.uniqueResult();
 		em.close();
 		return m;
 	}
 
 	public static boolean isActive(final Machine m, final Integer clock) {
 		synchronized (Machine.machineEvents) {
			if (Machine.machineEvents.length == 0) {
 				Machine.machineEvents = new EventType[] {
 				    EventType.get(EventType.EVENT_MACHINE_FAILURE),
 				    EventType.get(EventType.EVENT_MACHINE_FAILURE_JOB_MOVE_BAD),
 				    EventType.get(EventType.EVENT_MACHINE_FAILURE_JOB_MOVE_GOOD),
 				    EventType.get(EventType.EVENT_MACHINE_RESTART),
 				    EventType.get(EventType.EVENT_MACHINE_RESTART_JOB_MOVE_BAD),
 				    EventType.get(EventType.EVENT_MACHINE_RESTART_JOB_MOVE_GOOD) };
 			}
 		}
 		EntityManager em = Database.newEntityManager();
 		final Criteria crit = BaseEntity.getCriteria(em, Event.class, false);
 		crit.add(Restrictions.in("type", Machine.machineEvents));
 		crit.add(Restrictions.eq("sourceMachine", m));
 		crit.add(Restrictions.lt("clock", clock));
 		crit.addOrder(Order.desc("clock"));
 		crit.setMaxResults(1);
 		Event e = (Event) crit.uniqueResult();
 		em.close();
 		try {
 			Integer id = e.getType().getId();
 			if ((id.equals(EventType.EVENT_MACHINE_FAILURE))
 			    || (id.equals(EventType.EVENT_MACHINE_FAILURE_JOB_MOVE_BAD))
 			    || (id.equals(EventType.EVENT_MACHINE_FAILURE_JOB_MOVE_GOOD))) {
 				return false;
 			} else {
 				return true;
 			}
 		} catch (NullPointerException ex) {
 			// when no such event is found, the machine is active.
 			return true;
 		}
 	}
 
 	private String os;
 	private Integer id;
 	private Integer cpus;
 	private Integer hdd;
 	private String name;
 	private String platform;
 	private Integer ram;
 	private Integer speed;
 
 	private MachineGroup group;
 
 	public Integer getCPUs() {
 		return this.cpus;
 	}
 
 	@ManyToOne
 	@JoinColumn(name = "group_fk")
 	public MachineGroup getGroup() {
 		return this.group;
 	}
 
 	public Integer getHDD() {
 		return this.hdd;
 	}
 
 	@Id
 	@GeneratedValue
 	public Integer getId() {
 		return this.id;
 	}
 
 	@Index(name = "NameIndex")
 	public String getName() {
 		return this.name;
 	}
 
 	public String getOS() {
 		return this.os;
 	}
 
 	public String getPlatform() {
 		return this.platform;
 	}
 
 	public Integer getRAM() {
 		return this.ram;
 	}
 
 	public Integer getSpeed() {
 		return this.speed;
 	}
 
 	public void setCPUs(final Integer cpus) {
 		this.cpus = cpus;
 	}
 
 	public void setGroup(final MachineGroup group) {
 		this.group = group;
 	}
 
 	public void setHDD(final Integer hdd) {
 		this.hdd = hdd;
 	}
 
 	protected void setId(final Integer id) {
 		this.id = id;
 	}
 
 	public void setName(final String name) {
 		this.name = name;
 	}
 
 	public void setOS(final String os) {
 		this.os = os;
 	}
 
 	public void setPlatform(final String platform) {
 		this.platform = platform;
 	}
 
 	public void setRAM(final Integer ram) {
 		this.ram = ram;
 	}
 
 	public void setSpeed(final Integer speed) {
 		this.speed = speed;
 	}
 
 }
