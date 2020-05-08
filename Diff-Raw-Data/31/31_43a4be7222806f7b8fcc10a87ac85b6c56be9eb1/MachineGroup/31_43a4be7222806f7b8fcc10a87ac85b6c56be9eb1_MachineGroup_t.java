 /*
  * This file is part of SchedVis.
  * 
  * SchedVis is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SchedVis is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SchedVis. If not, see <http://www.gnu.org/licenses/>.
  */
 /**
  * 
  */
 package cz.muni.fi.spc.SchedVis.model.entities;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 
 import org.hibernate.Criteria;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 import cz.muni.fi.spc.SchedVis.model.BaseEntity;
 import cz.muni.fi.spc.SchedVis.model.Database;
 
 /**
  * @author Lukáš Petrovický <petrovicky@mail.muni.cz>
  * 
  */
 @Entity
 public class MachineGroup extends BaseEntity {
 
 	@SuppressWarnings("unchecked")
 	public static Set<MachineGroup> getAll() {
		final Criteria crit = BaseEntity.getCriteria(Database.getEntityManager(),
		    MachineGroup.class, true);
 		crit.addOrder(Order.asc("name"));
		return new HashSet<MachineGroup>(crit.list());
 	}
 
 	public static MachineGroup getWithId(final Integer id) {
		final Criteria crit = BaseEntity.getCriteria(Database.getEntityManager(),
		    MachineGroup.class, true);
 		crit.add(Restrictions.idEq(id));
 		crit.setMaxResults(1);
		return (MachineGroup) crit.uniqueResult();
 	}
 
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
 
 	public void addMachine(final Machine me) {
 		this.machines.add(me);
 	}
 
 	@GeneratedValue
 	@Id
 	public Integer getId() {
 		return this.id;
 	}
 
 	@OneToMany(mappedBy = "group", fetch = FetchType.EAGER)
 	public Set<Machine> getMachines() {
 		return this.machines;
 	}
 
 	public String getName() {
 		return this.name;
 	}
 
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
