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
  * 
  */
 package cz.muni.fi.spc.SchedVis.model.entities;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.Vector;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 
 import org.hibernate.Criteria;
 import org.hibernate.annotations.Index;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 import cz.muni.fi.spc.SchedVis.model.BaseEntity;
 import cz.muni.fi.spc.SchedVis.util.Database;
 
 /**
  * JPA Entity that represents a machine.
  * 
  * @author Lukáš Petrovický <petrovicky@mail.muni.cz>
  * 
  */
 @Entity
 public final class Machine extends BaseEntity implements Comparable<Machine> {
 
 	/**
 	 * Holds so-called "machine events" - those are events that change the state
 	 * of a machine, such as failure or restart. For performance reasons, this is
 	 * static and filled lazily when needed.
 	 */
 	private static EventType[] machineEvents = new EventType[0];
 
 	private static final Map<Integer, Machine> byId = new HashMap<Integer, Machine>();
 	private static final Map<String, Machine> byName = new HashMap<String, Machine>();
 
 	private static PreparedStatement s;
 
 	/**
 	 * Retrieve machine with a given ID.
 	 * 
 	 * @param id
 	 *          The ID in question.
 	 * @return The machine.
 	 */
 	public synchronized static Machine get(final int id) {
 		if (!Machine.byId.containsKey(id)) {
 			Machine.byId.put(id, Database.getEntityManager().find(Machine.class, id));
 		}
 		Machine m = Machine.byId.get(id);
 		if (!Machine.byName.containsKey(m.getName())) {
 			Machine.byName.put(m.getName(), m);
 		}
 		return m;
 	}
 
 	/**
 	 * Retrieve all the machines in a given group.
 	 * 
 	 * @param groupId
 	 *          ID of the group whose machines will be retrieved. If null,
 	 *          machines in no group are retrieved.
 	 * @return The machines.
 	 */
 	@SuppressWarnings("unchecked")
 	public static Set<Machine> getAll(final Integer groupId) {
 		final Criteria crit = BaseEntity.getCriteria(Machine.class);
 		if (groupId != null) {
 			crit.add(Restrictions.eq("group", MachineGroup.get(groupId)));
 		} else {
 			crit.add(Restrictions.isNull("group"));
 		}
 		crit.addOrder(Order.asc("name"));
 		return new TreeSet<Machine>(crit.list());
 	}
 
 	/**
 	 * Retrieve all machines, regardless whether they are or are not in some
 	 * group.
 	 * 
 	 * @return The machines.
 	 */
 	@SuppressWarnings("unchecked")
 	public static Set<Machine> getAllGroupless() {
 		final Criteria crit = BaseEntity.getCriteria(Machine.class);
 		crit.addOrder(Order.asc("name"));
 		return new TreeSet<Machine>(crit.list());
 	}
 
 	public static List<Event> getLatestSchedule(final Machine which,
 	    final Integer clock) {
 		try {
 			if (Machine.s == null) {
				String query = "SELECT id, assignedCPUs, deadline, job, jobHint, expectedStart, expectedEnd, bringsSchedule FROM Event WHERE sourceMachine_id = ? AND parent_fk = (SELECT max(parent_fk) FROM Event WHERE parent_fk IS NOT NULL AND bringsSchedule = 0 AND sourceMachine_id = ? AND virtualClock <= ?)";
 				Machine.s = BaseEntity.getConnection(Database.getEntityManager())
 				    .prepareStatement(query);
 			}
 			Machine.s.setInt(1, which.getId().intValue());
 			Machine.s.setInt(2, which.getId().intValue());
 			Machine.s.setInt(3, clock);
 			ResultSet rs = Machine.s.executeQuery();
 			List<Event> evts = new Vector<Event>();
 			while (rs.next()) {
 				Event evt = new Event();
 				evt.setId(rs.getInt(1));
 				evt.setAssignedCPUs(rs.getString(2));
 				evt.setDeadline(rs.getInt(3));
 				evt.setJob(rs.getInt(4));
 				evt.setJobHint(rs.getInt(5));
 				evt.setExpectedStart(rs.getInt(6));
 				evt.setExpectedEnd(rs.getInt(7));
 				if (rs.getInt(8) == 1) {
 					evt.setBringsSchedule(true);
 				} else {
 					evt.setBringsSchedule(false);
 				}
 				evts.add(evt);
 			}
 			rs.close();
 			Machine.s.clearParameters();
 			return evts;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return new Vector<Event>();
 		}
 	}
 
 	private static Machine getWithName(final String name) {
 		final Criteria crit = BaseEntity.getCriteria(Machine.class);
 		crit.add(Restrictions.eq("name", name));
 		crit.setMaxResults(1);
 		return (Machine) crit.uniqueResult();
 	}
 
 	/**
 	 * Retrieve machine with a given name.
 	 * 
 	 * @param name
 	 *          The name in question.
 	 * @param cache
 	 *          Whether or not to use the entity cache.
 	 * @return The machine.
 	 */
 	public synchronized static Machine getWithName(final String name,
 	    final boolean cache) {
 		if (!cache) {
 			return Machine.getWithName(name);
 		}
 		if (!Machine.byName.containsKey(name)) {
 			Machine m = Machine.getWithName(name);
 			if (m == null) {
 				return null;
 			}
 			Machine.byName.put(name, m);
 		}
 		Machine m = Machine.byName.get(name);
 		if (!Machine.byId.containsKey(m.getId())) {
 			Machine.byId.put(m.getId(), m);
 		}
 		return m;
 	}
 
 	/**
 	 * Determine whether a machine is active (not off-line) at a given point in
 	 * time.
 	 * 
 	 * @param m
 	 *          Machine in question.
 	 * @param clock
 	 *          The given point of time.
 	 * @return False when the last machine event up to and including the given
 	 *         time is machine failure. True otherwise, especially when there are
 	 *         no such events.
 	 */
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
 		final Criteria crit = BaseEntity.getCriteria(Event.class);
 		crit.add(Restrictions.in("type", Machine.machineEvents));
 		crit.add(Restrictions.lt("virtualClock", clock));
 		crit.addOrder(Order.desc("id"));
 		crit.setMaxResults(1);
 		Event e = (Event) crit.uniqueResult();
 		if (e == null) {
 			return true;
 		}
 		Integer id = e.getType().getId();
 		if ((id.equals(EventType.EVENT_MACHINE_FAILURE))
 		    || (id.equals(EventType.EVENT_MACHINE_FAILURE_JOB_MOVE_BAD))
 		    || (id.equals(EventType.EVENT_MACHINE_FAILURE_JOB_MOVE_GOOD))) {
 			return false;
 		}
 		return true;
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
 
 	@Override
 	public int compareTo(final Machine o) {
 		return this.getId().compareTo(o.getId());
 	}
 
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
 
 	@Index(name = "mnIndex")
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
