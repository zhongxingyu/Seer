 package org.chai.kevin.dashboard;
 
 import grails.plugin.springcache.annotations.Cacheable;
 
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.chai.kevin.Period;
 import org.chai.kevin.data.Type;
 import org.chai.location.CalculationLocation;
 import org.chai.location.DataLocationType;
 import org.chai.kevin.value.CalculationValue;
 import org.chai.kevin.value.Value;
 import org.chai.kevin.value.ValueService;
 import org.springframework.transaction.annotation.Transactional;
 
 public class DashboardValueService {
 
 	private static Type type = Type.TYPE_NUMBER();
 	
 //	private InfoService infoService;
 	private DashboardService dashboardService;
 	private ValueService valueService;
 
 	private class PercentageVisitor implements DashboardVisitor<Value> {
 		
 		private Set<DataLocationType> types;
 		
 		public PercentageVisitor(Set<DataLocationType> types) {
 			this.types = types;
 		}
 
 		private final Log log = LogFactory.getLog(PercentageVisitor.class);
 		
 		@Override
 		public Value visitProgram(DashboardProgram program, CalculationLocation location, Period period) {
 			if (log.isDebugEnabled()) log.debug("visitProgram(program="+program+",location="+location+",period="+period+")");
 			
 			Integer totalWeight = 0;
 			Double sum = 0.0d;
 
 			List<DashboardEntity> dashboardEntities = dashboardService.getDashboardEntities(program.getProgram());
 			if (dashboardEntities.isEmpty()) return null;
 			
 			for (DashboardEntity child : dashboardEntities) {
 				Value childPercentage = child.visit(this, location, period);
 				if (childPercentage != null) { 
 					Integer weight = child.getWeight();
 					if (!childPercentage.isNull()) {
 						sum += childPercentage.getNumberValue().doubleValue() * weight;
 						totalWeight += weight;
 					}
 					else {
 						// MISSING_EXPRESSION - we skip it
 						// MISSING_NUMBER - should we count it in as zero ?
 					}
 				}
 			}
 			// TODO what if sum = 0 and totalWeight = 0 ?
 			Double average = sum/totalWeight;
 			Value value = null;
 			if (average.isNaN() || average.isInfinite()) value = Value.NULL_INSTANCE();
 			else value = type.getValue(average);
 			
 			if (log.isDebugEnabled()) log.debug("visitProgram()="+value);
 			return value;
 		}
 
 		@Override
 		public Value visitTarget(DashboardTarget target, CalculationLocation location, Period period) {
 			if (log.isDebugEnabled()) log.debug("visitTarget(target="+target+",location="+location+",period="+period+")");
 			
 			CalculationValue<?> calculationValue = valueService.getCalculationValue(target.getCalculation(), location, period, types);
 			Value percentage = calculationValue.getAverage();
 
 			if (log.isDebugEnabled()) log.debug("visitTarget(...)="+percentage);
 			return percentage;
 		}
 	}
 	
 	// TODO check this
 	@Transactional(readOnly = true)
 	@Cacheable(cache="dashboardCache")
 	public Value getDashboardValue(Period period, CalculationLocation location, Set<DataLocationType> types, DashboardEntity dashboardEntity) {
 		return dashboardEntity.visit(new PercentageVisitor(types), location, period);
 	}
 	
 	public void setDashboardService(DashboardService dashboardService) {
 		this.dashboardService = dashboardService;
 	}
 	
 	public void setValueService(ValueService valueService) {
 		this.valueService = valueService;
 	}
 	
 }
