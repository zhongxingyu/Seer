 package org.chai.kevin.planning;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.AttributeOverride;
 import javax.persistence.AttributeOverrides;
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Lob;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.chai.kevin.LanguageService;
 import org.chai.kevin.Orderable;
 import org.chai.kevin.Translation;
 import org.chai.kevin.data.NormalizedDataElement;
 import org.chai.kevin.util.Utils;
 
 @Entity(name="PlanningCost")
 @Table(name="dhsst_planning_cost")
 public class PlanningCost extends Orderable<Integer> {
 
 	public enum PlanningCostType {OUTGOING("planning.planningcost.type.outgoing"), INCOMING("planning.planningcost.type.incoming");
 		private String code;
 	
 		PlanningCostType(String code) {
 			this.code = code;
 		}
 	
 		public String getCode() {
 			return code;
 		}
 		String getKey() { return name(); }
 	};
 
 	private Long id;
 	private Integer order;
 	
 	private PlanningCostType type;
 	private PlanningType planningType;
 
 	private NormalizedDataElement dataElement;
 	private Translation names = new Translation();
 	
 	private Boolean hideIfZero;
 	
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
 	
 	@Basic
 	public Boolean getHideIfZero() {
 		return hideIfZero;
 	}
 	
 	public void setHideIfZero(Boolean hideIfZero) {
 		this.hideIfZero = hideIfZero;
 	}
 	
 	@ManyToOne(targetEntity=NormalizedDataElement.class)
 	public NormalizedDataElement getDataElement() {
 		return dataElement;
 	}
 
 	public void setDataElement(NormalizedDataElement dataElement) {
 		this.dataElement = dataElement;
 	}
 	
 	@Basic
 	@Enumerated(EnumType.STRING)
 	public PlanningCostType getType() {
 		return type;
 	}
 	
 	public void setType(PlanningCostType type) {
 		this.type = type;
 	}
 	
 	@Embedded
 	@AttributeOverrides({ @AttributeOverride(name = "jsonText", column = @Column(name = "jsonNames", nullable = false)) })
 	public Translation getNames() {
 		return names;
 	}
 	
 	public void setNames(Translation names) {
 		this.names = names;
 	}
 	
 	@ManyToOne(targetEntity=PlanningType.class)
 	public PlanningType getPlanningType() {
 		return planningType;
 	}
 	
 	public void setPlanningType(PlanningType planningType) {
 		this.planningType = planningType;
 	}
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (!(obj instanceof PlanningCost))
 			return false;
 		PlanningCost other = (PlanningCost) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		return true;
 	}
 	
 	public String toString(){
 		return "PlanningCost[getId()=" + getId() + ", getNames()=" + getNames() + ", getType()=" + getType() + "]";
 	}
 
 	private List<String> splitName(LanguageService languageService) {
		String name = getNames().get(languageService.getCurrentLanguage());
 		String[] groupsInNameArray = name.split("-");
 		List<String> groupsInName = new ArrayList<String>();
 		for (String group : groupsInNameArray) {
 			groupsInName.add(group.trim());
 		}
 		return groupsInName;
 	}
 
 	@Transient
 	public List<String> getGroups(LanguageService languageService) {
 		List<String> groupsInName = splitName(languageService);
 		groupsInName.remove(groupsInName.size() - 1);
 		return groupsInName;
 	}
 	
 	@Transient
 	public String getDisplayName(LanguageService languageService) {
 		List<String> groupsInName = splitName(languageService);
 		return groupsInName.get(groupsInName.size() -1);
 	}
 }
