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
 
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 
 import org.hibernate.Criteria;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 import cz.muni.fi.spc.SchedVis.model.BaseEntity;
 import cz.muni.fi.spc.SchedVis.model.Database;
 
 /**
  * JPA Entity that represents a group of machines.
  * 
  * @author Lukáš Petrovický <petrovicky@mail.muni.cz>
  */
 @Entity
 @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class MachineGroup extends BaseEntity implements
    Comparable<MachineGroup> {
 
 	/**
 	 * Get machine group with a given ID.
 	 * 
 	 * @param id
 	 *          The id in question.
 	 * @return The machine.
 	 */
 	public static MachineGroup get(final Integer id) {
 		return Database.getEntityManager().find(MachineGroup.class, id);
 	}
 
 	/**
 	 * Get all the machine groups.
 	 * 
 	 * @return The machine groups.
 	 */
 	@SuppressWarnings("unchecked")
 	public static Set<MachineGroup> getAll() {
 		final Criteria crit = BaseEntity.getCriteria(Database.getEntityManager(),
 		    MachineGroup.class, true);
 		crit.addOrder(Order.asc("name"));
 		return new TreeSet<MachineGroup>(crit.list());
 	}
 
 	/**
 	 * Get machine group with a given name.
 	 * 
 	 * @param name
 	 *          The machine name in question.
 	 * @return The machine.
 	 */
 	public static MachineGroup getWithName(final String name) {
 		final Criteria crit = BaseEntity.getCriteria(Database.getEntityManager(),
 		    MachineGroup.class, true);
 		crit.add(Restrictions.eq("name", name));
 		crit.setMaxResults(1);
 		return (MachineGroup) crit.uniqueResult();
 	}
 
 	private Integer id;
 
 	private String name;
 
 	private Set<Machine> machines;
 
 	/**
 	 * Add a machine to the group.
 	 * 
 	 * @param me
 	 *          The machine.
 	 */
 	public void addMachine(final Machine me) {
 		this.machines.add(me);
 	}
 
	@Override
	public int compareTo(final MachineGroup o) {
		return this.getName().compareTo(o.getName());
	}

 	@GeneratedValue
 	@Id
 	public Integer getId() {
 		return this.id;
 	}
 
 	/**
 	 * Get all machines in a group.
 	 * 
 	 * The eager fetching is there because lazy fetching caused various runtime
 	 * problems with Hibernate.
 	 * 
 	 * @return The machines.
 	 */
 	@OneToMany(mappedBy = "group", fetch = FetchType.EAGER)
 	public Set<Machine> getMachines() {
 		return this.machines;
 	}
 
 	public String getName() {
 		return this.name;
 	}
 
 	/**
 	 * Remove machine from the group.
 	 * 
 	 * @param me
 	 *          The machine.
 	 */
 	public void removeMachine(final Machine me) {
 		this.machines.remove(me);
 	}
 
 	protected void setId(final Integer id) {
 		this.id = id;
 	}
 
 	protected void setMachines(final Set<Machine> machines) {
 		this.machines = machines;
 	}
 
 	public void setName(final String name) {
 		this.name = name;
 	}
 
 }
