 package at.ac.tuwien.infosys.lsdc.scheduler;
 
 import at.ac.tuwien.infosys.lsdc.scheduler.exception.IllegalValueException;
 
 public enum PolicyLevel {
 	GREEN(1.2, 0.5), 
 	GREEN_ORANGE(1.15, 0.6), 
 	ORANGE(1.1, 0.7), 
 	ORANGE_RED(1.05, 0.8), 
 	RED(1.0, 0.9);
 
 	private Double overBudget;
 	private Double threshHold;
 
 
 	private PolicyLevel(Double overBudget, Double threshHold) {
 		this.overBudget = overBudget;
 		this.threshHold = threshHold;
 	}
 
 	public Double getOverBudget() {
 		return overBudget;
 	}
 
 	public Double getThreshold() {
 		return threshHold;
 	}
 
 	public static PolicyLevel getAccordingPolicyLevel(Double percent)
 	throws IllegalValueException {
 		if (percent > 1.0 || percent < 0.0) {
 			throw new IllegalValueException("Value must be below 1.0");
 		}
 
 		if (percent <= PolicyLevel.GREEN.getThreshold())
 			return PolicyLevel.GREEN;
		if (percent > PolicyLevel.RED.getThreshold())
 			return PolicyLevel.RED;
 
 		PolicyLevel result = PolicyLevel.GREEN;
 
 		for (PolicyLevel candidate : PolicyLevel.values()) {
 			if (percent <= candidate.getThreshold()) {
 				result = candidate;
 				break;
 			}
 		}
 
 		return result;
 	}
 }
