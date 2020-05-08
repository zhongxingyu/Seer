 package org.chai.kevin.fct;
 
 import grails.plugin.springcache.annotations.Cacheable;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.chai.kevin.LanguageService;
 import org.chai.kevin.LocationSorter;
 import org.chai.kevin.Period;
 import org.chai.kevin.data.Sum;
 import org.chai.kevin.location.CalculationLocation;
 import org.chai.kevin.location.DataLocation;
 import org.chai.kevin.location.DataLocationType;
 import org.chai.kevin.location.Location;
 import org.chai.kevin.location.LocationLevel;
 import org.chai.kevin.reports.ReportProgram;
 import org.chai.kevin.reports.ReportService;
 import org.chai.kevin.value.CalculationValue;
 import org.chai.kevin.value.RawDataElementValue;
 import org.chai.kevin.value.SumValue;
 import org.chai.kevin.value.Value;
 import org.chai.kevin.value.ValueService;
 import org.springframework.transaction.annotation.Transactional;
 
 public class FctService {
 	private static final Log log = LogFactory.getLog(FctService.class);
 	
 	private LanguageService languageService;
 	private ReportService reportService;
 	private ValueService valueService;
 	private Set<String> skipLevels;
 	
 	@Cacheable("fctCache")
 	@Transactional(readOnly = true)
 	public FctTable getFctTable(Location location, ReportProgram program, FctTarget target, Period period, LocationLevel level, Set<DataLocationType> types) {		
 		if (log.isDebugEnabled()) 
 			log.debug("getFctTable(period="+period+",location="+location+",level="+level+",program="+program+",target="+target+")");				
		
		List<FctTargetOption> targetOptions = target.getTargetOptions();
		
 		Map<CalculationLocation, Map<FctTargetOption, Value>> valueMap = new HashMap<CalculationLocation, Map<FctTargetOption, Value>>();
 		List<FctTarget> targets = new ArrayList<FctTarget>();
 		List<CalculationLocation> topLevelLocations = new ArrayList<CalculationLocation>();
 		
 		if(targetOptions.isEmpty())
 			return new FctTable(valueMap, targetOptions, targets, topLevelLocations);
 		Collections.sort(targetOptions);
 		
 		Set<LocationLevel> skips = reportService.getSkipLocationLevels(skipLevels);
 		List<Location> treeLocations = location.collectTreeWithDataLocations(skips, types);
 		List<DataLocation> dataLocations = location.collectDataLocations(skips, types);
 		
 		for (Location treeLocation : treeLocations) {
 			Map<FctTargetOption, Value> targetMap = new HashMap<FctTargetOption, Value>();
 			for(FctTargetOption targetOption : targetOptions){
 				if (log.isDebugEnabled()) log.debug("getting values for sum fct with calculation: "+targetOption.getSum());
 				targetMap.put(targetOption, getFctValue(targetOption, treeLocation, period, types));
 			}
 			valueMap.put(treeLocation, targetMap);
 		}
 		
 		for (DataLocation dataLocation : dataLocations) {
 			Map<FctTargetOption, Value> targetMap = new HashMap<FctTargetOption, Value>();
 			for(FctTargetOption targetOption : targetOptions){
 				if (log.isDebugEnabled()) log.debug("getting values for sum fct with calculation: "+targetOption.getSum());
 				Set<DataLocationType> dataLocationTypes = new HashSet<DataLocationType>();
 				dataLocationTypes.add(dataLocation.getType());
 				targetMap.put(targetOption, getFctValue(targetOption, dataLocation, period, dataLocationTypes));
 			}
 			valueMap.put(dataLocation, targetMap);						
 		}
 		
 		topLevelLocations.addAll(location.getChildrenEntitiesWithDataLocations(skips, types));
 		
 		targets = getFctTargets(program);		
 		Collections.sort(targets);
 		
 		FctTable fctTable = new FctTable(valueMap, targetOptions, targets, topLevelLocations);
 		if (log.isDebugEnabled()) log.debug("getFctTable(...)="+fctTable);
 		return fctTable;
 	}
 
 	private Value getFctValue(FctTargetOption targetOption, CalculationLocation location, Period period, Set<DataLocationType> types) {
 		Value value = null;
 		CalculationValue<?> calculationValue = valueService.getCalculationValue(targetOption.getSum(), location, period, types);
 		if (calculationValue != null) 
 			value = calculationValue.getValue();
 		return value;
 	}
 
 	public List<FctTarget> getFctTargets(ReportProgram program){
 		List<FctTarget> targets = new ArrayList<FctTarget>();
 		List<FctTarget> result = new ArrayList<FctTarget>();
 		targets = reportService.getReportTargets(FctTarget.class, program);
 		for(FctTarget target : targets){
 			if(target.getTargetOptions() != null && !target.getTargetOptions().isEmpty())
 				result.add(target);
 		}
 		return result;
 	}
 	
 	public void setLanguageService(LanguageService languageService) {
 		this.languageService = languageService;
 	}
 	
 	public void setReportService(ReportService reportService) {
 		this.reportService = reportService;
 	}
 	
 	public void setValueService(ValueService valueService) {
 		this.valueService = valueService;
 	}
 	
 	public void setSkipLevels(Set<String> skipLevels) {
 		this.skipLevels = skipLevels;
 	}
 	
 	public Set<LocationLevel> getSkipLocationLevels(){
 		return reportService.getSkipLocationLevels(skipLevels);
 	}
 }
