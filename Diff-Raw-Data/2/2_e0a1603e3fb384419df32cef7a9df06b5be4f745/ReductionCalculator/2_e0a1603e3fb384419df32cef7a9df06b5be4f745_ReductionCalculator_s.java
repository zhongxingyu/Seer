 package woodsie.avalanche.reduction;
 
 import java.math.BigDecimal;
 import java.math.MathContext;
 
 import woodsie.avalanche.reduction.data.Hazard;
 import woodsie.avalanche.reduction.data.ReductionParams;
 import woodsie.avalanche.reduction.data.Steepness;
 import woodsie.avalanche.reduction.data.Where;
 
 public class ReductionCalculator {
 	public static final BigDecimal EXTREME = BigDecimal.valueOf(100);
 
 	public BigDecimal process(ReductionParams params) {
 		if (params.hazardLevel == Hazard.VERY_HIGH) {
 			return EXTREME;
 		}
 
 		BigDecimal dangerPotential = new BigDecimal(params.hazardLevel.getDangerPotential(params.higherHazard), MathContext.DECIMAL32);
 
 		int reductionFactor = 1;
 
 		if ((params.hazardLevel.getDangerPotential(params.higherHazard) < Hazard.CONSIDERABLE.getDangerPotential(false))
 		        || (params.hazardLevel == Hazard.HIGH && params.steepness == Steepness.NOT_STEEP)
		        || (params.hazardLevel == Hazard.CONSIDERABLE && params.steepness.getReductionFactor() >= 1)) {
 			reductionFactor *= params.steepness.getReductionFactor();
 
 			if (!params.allAspects) {
 
 				if (!params.inverse
 				        || (params.inverse && params.where == Where.AVOID_CRITICAL)) {
 					reductionFactor *= params.where.getReductionFactor();
 				}
 
 				reductionFactor *= params.terrain.getReductionFactor();
 			}
 
 			reductionFactor *= params.groupSize.getReductionFactor();
 		}
 
 		return dangerPotential.divide(new BigDecimal(reductionFactor), MathContext.DECIMAL32);
 	}
 }
