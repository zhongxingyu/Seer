 package org.chai.kevin.planning;
 
 import javax.persistence.AttributeOverride;
 import javax.persistence.AttributeOverrides;
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 import org.chai.kevin.Orderable;
 import org.chai.kevin.Translation;
 
 @Entity(name="PlanningOutputColumn")
 @Table(name="dhsst_planning_output_column")
 
 public class PlanningOutputColumn extends Orderable<Integer> {
 
 	private Long id;
 	
 	private PlanningOutput planningOutput;
	private String header;
 	private Translation names = new Translation();
 	private Integer order;
 	
 	@Id
 	@GeneratedValue
 	public Long getId() {
 		return id;
 	}
 	
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
 	@Basic
 	@Column(name="ordering")
 	public Integer getOrder() {
 		return order;
 	}
 	
 	public void setOrder(Integer order) {
 		this.order = order;
 	}
 	
 	@ManyToOne(targetEntity=PlanningOutput.class)
 	public PlanningOutput getPlanningOutput() {
 		return planningOutput;
 	}
 	
 	public void setPlanningOutput(PlanningOutput planningOutput) {
 		this.planningOutput = planningOutput;
 	}
 
 	@Basic
	public String getHeader() {
		return header;
 	}
 	
	public void setHeader(String header) {
		this.header = header;
 	}
 	
 	@Embedded
 	@AttributeOverrides({ @AttributeOverride(name = "jsonText", column = @Column(name = "jsonNames", nullable = false)) })
 	public Translation getNames() {
 		return names;
 	}
 	
 	public void setNames(Translation names) {
 		this.names = names;
 	}
 }
