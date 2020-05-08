 package org.chai.kevin.planning;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 import org.hisp.dhis.period.Period;
 
 @Entity(name="Planning")
 @Table(name="dhsst_planning")
 public class Planning {
 
 	private Long id;
 	
 	private Period period;
 	private List<PlanningType> planningTypes = new ArrayList<PlanningType>();
 	// TODO add settings form page
 	
 	@Id
 	@GeneratedValue
 	public Long getId() {
 		return id;
 	}
 	
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
 	@OneToMany(mappedBy="planning", targetEntity=PlanningType.class)
 	public List<PlanningType> getPlanningTypes() {
 		return planningTypes;
 	}
 	
 	public void setPlanningTypes(List<PlanningType> planningTypes) {
 		this.planningTypes = planningTypes;
 	}
 	
 	@ManyToOne(targetEntity=Period.class)
 	public Period getPeriod() {
 		return period;
 	}
 	
 	public void setPeriod(Period period) {
 		this.period = period;
 	}
 	
 }
