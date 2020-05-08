 package albert.lacambra.server.models;
 
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.annotation.EntitySubclass;
 import com.googlecode.objectify.annotation.Index;
 
 @EntitySubclass(index=true)
 public class IndividualCost extends Cost<IndividualCost> {
 
 	@Index
 	private Long date;
 	
 	public static Key<IndividualCost> key(Key<PersistedBudget> parent, Long id) {
 		return Key.create(parent, IndividualCost.class, id);
 	}
 
 	public IndividualCost(){}
 	public IndividualCost(Key<PersistedBudget> budget, IndividualCostDTO costDTO) {
 
 		this.budget = budget;
 
 		setConcept(costDTO.getConcept())
 		.setCost(costDTO.getCost())
 		.setDate(costDTO.getDate())
 		.setId(costDTO.getId())
 		.setTags(costDTO.getTags());
 
 	}
 
 	public IndividualCost(IndividualCostDTO costDTO) {
 
 		setConcept(costDTO.getConcept())
 		.setBudgetId(costDTO.getBudgetId())
 		.setCost(costDTO.getCost())
 		.setDate(costDTO.getDate())
 		.setId(costDTO.getId())
 		.setTags(costDTO.getTags());
 
 	}
 
 	public Long getDate() {
 		return date;
 	}
 
 	public IndividualCost setDate(Long date) {
 		this.date = date;
 		return this;
 	}
 
 	public IndividualCostDTO getIndividualCostDTO() {
 
 		return new IndividualCostDTO()
//		.setBudgetId(getBudget().getId())
 		.setConcept(getConcept())
 		.setCost(getCost())
 		.setDate(getDate())
 		.setId(getId())
 		.setTags(getTags())
 		;
 
 	}
 
 	@Override
 	public IndividualCostDTO getDTO() {
 		return getIndividualCostDTO();
 	}
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
