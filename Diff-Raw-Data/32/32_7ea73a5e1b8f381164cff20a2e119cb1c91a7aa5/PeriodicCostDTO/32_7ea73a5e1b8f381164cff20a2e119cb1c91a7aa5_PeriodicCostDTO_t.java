 package albert.lacambra.server.models;
 
 
 public class PeriodicCostDTO extends CostDTO<PeriodicCostDTO>{
 
 	protected PeriodStep periodStep;
 	protected Long start;
 	protected Long end;
 	protected Boolean isFixedCost;
 	protected Integer foreseenCost;
 
 	public PeriodStep getPeriodStep() {
 		return periodStep;
 	}
 	public Long getStart() {
 		return start;
 	}
 	public Long getEnd() {
 		return end;
 	}
 	public Boolean getIsFixedCost() {
 		return isFixedCost;
 	}
 	public Integer getForeseenCost() {
 		return foreseenCost;
 	}
 	public PeriodicCostDTO setPeriodStep(PeriodStep periodStep) {
 		this.periodStep = periodStep;
 		return this;
 	}
 	public PeriodicCostDTO setStart(Long start) {
 		this.start = start;
 		return this;
 	}
 	public PeriodicCostDTO setEnd(Long end) {
 		this.end = end;
 		return this;
 	}
 	public PeriodicCostDTO setIsFixedCost(Boolean isFixedCost) {
//		if ( isFixedCost && foreseenCost != null ) {
//			throw new RuntimeException("The cost is fixed and however, "
//					+ "you are trying to give a forseen or expected cost that woulkd be ignored.");
//		}
//		
//		if ( !isFixedCost && foreseenCost == null ) {
//			throw new RuntimeException("The cost is fixed and however, "
//					+ "you are notgiving a forseen or expected cost");
//		}
 		
 		this.isFixedCost = isFixedCost;
 		return this;
 	}
 	public PeriodicCostDTO setForeseenCost(Integer foreseenCost) {
 		this.foreseenCost = foreseenCost;
 		return this;
 	}
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
