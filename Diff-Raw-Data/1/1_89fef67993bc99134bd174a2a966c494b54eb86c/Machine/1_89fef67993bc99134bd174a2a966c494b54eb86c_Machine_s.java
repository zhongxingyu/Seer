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
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 
 import org.hibernate.Criteria;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.annotations.Index;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 
 import cz.muni.fi.spc.SchedVis.model.BaseEntity;
 import cz.muni.fi.spc.SchedVis.model.EventType;
 import cz.muni.fi.spc.SchedVis.model.JobHint;
 import cz.muni.fi.spc.SchedVis.util.Database;
 
 /**
  * JPA Entity that represents a machine.
  * 
  * @author Lukáš Petrovický <petrovicky@mail.muni.cz>
  * 
  */
 @Entity
 @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
 public final class Machine extends BaseEntity implements Comparable<Machine> {
 
 	private static Integer internalIdCounter = 0;
 
 	private static PreparedStatement s;
 
 	private static final ConcurrentMap<Integer, Machine> byId = new ConcurrentHashMap<Integer, Machine>();
 
 	private static final ConcurrentMap<String, Machine> byName = new ConcurrentHashMap<String, Machine>();
 
 	private static final Integer[] machineEvents = new Integer[] {
 	    EventType.MACHINE_FAIL.getId(), EventType.MACHINE_FAIL_MOVE_BAD.getId(),
 	    EventType.MACHINE_FAIL_MOVE_GOOD.getId(),
 	    EventType.MACHINE_RESTART.getId() };
 
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
 			crit.add(Restrictions.eq("group", MachineGroup.getWithId(groupId)));
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
 
 	/**
 	 * Get the latest schedule that concerns given machine at a given time. This
 	 * method ties directly to SQLite, even skipping Hibernate - it is essential
 	 * that this method is as fast as possible.
 	 * 
 	 * @param which
 	 *          The machine in which we are interested.
 	 * @param evt
 	 *          The latest schedule is looked for in the interval of event 1 to
 	 *          this number.
 	 * @return Jobs in the schedule.
 	 */
 	public static List<Job> getLatestSchedule(final Machine which, final Event evt) {
 		try {
 			return Machine.getLatestScheduleInternal(which, evt);
 		} catch (final SQLException ex) {
 			return new ArrayList<Job>();
 		}
 	}
 
 	private synchronized static List<Job> getLatestScheduleInternal(
 	    final Machine which, final Event evt) throws SQLException {
 		if (Machine.s == null) {
 			final String query = "SELECT id, assignedCPUs, deadline, number, jobHintId, expectedStart, expectedEnd, bringsSchedule FROM Job WHERE machine_id = ? AND parent = (SELECT max(parent) FROM Job WHERE machine_id = ? AND parent <= ?)";
 			Machine.s = BaseEntity.getConnection(Database.getEntityManager())
 			    .prepareStatement(query);
 		}
 		Machine.s.setInt(1, which.getId());
 		Machine.s.setInt(2, which.getId());
 		Machine.s.setInt(3, evt.getId());
 		final ResultSet rs = Machine.s.executeQuery();
 		final List<Job> schedules = new ArrayList<Job>();
 		while (rs.next()) {
 			final Job schedule = new Job();
 			schedule.setId(rs.getInt(1));
 			schedule.setAssignedCPUs(rs.getString(2));
 			schedule.setDeadline(rs.getInt(3));
 			schedule.setNumber(rs.getInt(4));
 			schedule.setHint(JobHint.getWithId(rs.getInt(5)));
 			schedule.setExpectedStart(rs.getInt(6));
 			schedule.setExpectedEnd(rs.getInt(7));
 			if (rs.getInt(8) == 1) {
 				schedule.setBringsSchedule(true);
 			} else {
 				schedule.setBringsSchedule(false);
 			}
 			schedules.add(schedule);
 		}
 		rs.close();
 		Machine.s.clearParameters();
 		return schedules;
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
 	 *          Whether or not to use the local entity cache.
 	 * @return The machine.
 	 */
 	public static Machine getWithName(final String name, final boolean cache) {
 		if (!cache) {
 			return Machine.getWithName(name);
 		}
 		if (!Machine.byName.containsKey(name)) {
 			final Machine m = Machine.getWithName(name);
 			if (m == null) {
 				return null;
 			}
 			Machine.byName.put(name, m);
 		}
 		final Machine m = Machine.byName.get(name);
 		Machine.byId.putIfAbsent(m.getId(), m);
 		return m;
 	}
 
 	/**
 	 * Determine whether a machine is active (not off-line) at a given point in
 	 * time.
 	 * 
 	 * @param m
 	 *          Machine in question.
 	 * @param evt
 	 *          The given point of time.
 	 * @return False when the last machine event up to and including the given
 	 *         time is machine failure. True otherwise, especially when there are
 	 *         no such events.
 	 */
 	public static boolean isActive(final Machine m, final Event evt) {
 		final Criteria crit = BaseEntity.getCriteria(Event.class);
 		crit.add(Restrictions.lt("id", evt.getId()));
 		crit.add(Restrictions.in("eventTypeId", Machine.machineEvents));
 		crit.setProjection(Projections.max("id"));
 		final Integer evtId = (Integer) crit.uniqueResult();
 		if (evtId == null) {
 			return true;
 		}
 		final Event e = Event.getWithId(evtId);
 		if (e == null) {
 			return true;
 		}
 		final EventType et = e.getType();
 		if ((et == EventType.MACHINE_FAIL)
 		    || (et == EventType.MACHINE_FAIL_MOVE_BAD)
 		    || (et == EventType.MACHINE_FAIL_MOVE_GOOD)) {
 			return false;
 		}
 		return true;
 	}
 
 	private int internalId;
 
 	private String os;
 
 	private int id;
 
 	private int cpus;
 
 	private int hdd;
 	private String name;
 	private String platform;
 	private int ram;
 	private int speed;
 	private MachineGroup group;
 
 	public Machine() {
 		synchronized (Machine.internalIdCounter) {
 			this.setInternalId(Machine.internalIdCounter++);
 		}
 	}
 
 	@Override
 	public int compareTo(final Machine o) {
 		return Integer.valueOf(this.getId()).compareTo(Integer.valueOf(o.getId()));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(final Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (!(obj instanceof Machine)) {
 			return false;
 		}
 		final Machine other = (Machine) obj;
 		if (this.internalId != other.internalId) {
 			return false;
 		}
 		return true;
 	}
 
 	public int getCPUs() {
 		return this.cpus;
 	}
 
 	@ManyToOne
 	@JoinColumn(name = "group_fk")
 	public MachineGroup getGroup() {
 		return this.group;
 	}
 
 	public int getHDD() {
 		return this.hdd;
 	}
 
 	@Id
 	@GeneratedValue
 	public int getId() {
 		return this.id;
 	}
 
 	public int getInternalId() {
 		return this.internalId;
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
 
 	public int getRAM() {
 		return this.ram;
 	}
 
 	public int getSpeed() {
 		return this.speed;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + this.internalId;
 		return result;
 	}
 
 	public void setCPUs(final int cpus) {
 		this.cpus = cpus;
 	}
 
 	public void setGroup(final MachineGroup group) {
 		this.group = group;
 	}
 
 	public void setHDD(final int hdd) {
 		this.hdd = hdd;
 	}
 
 	protected void setId(final int id) {
 		this.id = id;
 	}
 
 	private void setInternalId(final int id) {
 		this.internalId = id;
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
 
 	public void setRAM(final int ram) {
 		this.ram = ram;
 	}
 
 	public void setSpeed(final int speed) {
 		this.speed = speed;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "Machine [cpus=" + this.cpus + ", "
 		    + (this.group != null ? "group=" + this.group + ", " : "") + "hdd="
 		    + this.hdd + ", id=" + this.id + ", "
 		    + (this.name != null ? "name=" + this.name + ", " : "")
 		    + (this.os != null ? "os=" + this.os + ", " : "")
 		    + (this.platform != null ? "platform=" + this.platform + ", " : "")
 		    + "ram=" + this.ram + ", speed=" + this.speed + "]";
 	}
 
 }
