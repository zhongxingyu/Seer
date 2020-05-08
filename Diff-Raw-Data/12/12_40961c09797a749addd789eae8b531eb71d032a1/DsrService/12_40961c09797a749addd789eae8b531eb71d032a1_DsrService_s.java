 package org.chai.kevin.dsr;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.chai.kevin.Period;
 import org.chai.kevin.data.Calculation;
 import org.chai.kevin.data.DataElement;
 import org.chai.kevin.data.DataService;
 import org.chai.kevin.location.CalculationLocation;
 import org.chai.kevin.location.DataLocation;
 import org.chai.kevin.location.DataLocationType;
 import org.chai.kevin.location.Location;
 import org.chai.kevin.location.LocationLevel;
 import org.chai.kevin.reports.ReportProgram;
 import org.chai.kevin.reports.ReportService;
 import org.chai.kevin.value.CalculationValue;
 import org.chai.kevin.value.DataValue;
 import org.chai.kevin.value.SumValue;
 import org.chai.kevin.value.Value;
 import org.chai.kevin.value.ValueService;
 import org.springframework.cache.annotation.Cacheable;
 import org.springframework.transaction.annotation.Transactional;
 
 public class DsrService {
 	private static final Log log = LogFactory.getLog(DsrService.class);
 	
 	private ReportService reportService;
 	private ValueService valueService;
 	private DataService dataService;
 	private Set<String> skipLevels;
 	
 	@Cacheable("dsrCache")
 	@Transactional(readOnly = true)
 	public DsrTable getDsrTable(Location location, ReportProgram program, Period period, Set<DataLocationType> types, DsrTargetCategory category) {
 		if (log.isDebugEnabled())  log.debug("getDsrTable(period="+period+",location="+location+",program="+program+",types="+types+",category="+category+")");
 
 		Set<LocationLevel> skips = reportService.getSkipLocationLevels(skipLevels);		
 		List<Location> treeLocations = location.collectTreeWithDataLocations(skips, types);
 		List<DataLocation> dataLocations = location.collectDataLocations(skips, types);
 		
 		List<DsrTarget> targets = new ArrayList<DsrTarget>();
 		targets.addAll(category.getTargetsForProgram(program));
 		
 		Map<CalculationLocation, Map<DsrTarget, Value>> valueMap = new HashMap<CalculationLocation, Map<DsrTarget, Value>>();		
 		List<DsrTargetCategory> targetCategories = new ArrayList<DsrTargetCategory>();
 		
 		if(dataLocations.isEmpty() || targets.isEmpty()) return new DsrTable(valueMap, targets, targetCategories);
 		Collections.sort(targets);
 				
 		for (DsrTarget target : targets) {
 			Calculation calculation = dataService.getData(target.getData().getId(), Calculation.class);
 			if(calculation != null){				
 				for(Location treeLocation : treeLocations){			
 					if(!valueMap.containsKey(treeLocation))
 						valueMap.put(treeLocation, new HashMap<DsrTarget, Value>());	
 					valueMap.get(treeLocation).put(target, getDsrValue(target, calculation, treeLocation, period, types));
 				}
 				for(DataLocation dataLocation : dataLocations){					
 					if(!valueMap.containsKey(dataLocation))
 						valueMap.put(dataLocation, new HashMap<DsrTarget, Value>());	
 					valueMap.get(dataLocation).put(target, getDsrValue(target, calculation, dataLocation, period, types));
 				}	
 			}
 			else{
 				DataElement dataElement = dataService.getData(target.getData().getId(), DataElement.class);
 				if(dataElement != null){
 					for(DataLocation dataLocation : dataLocations){										
 						if(!valueMap.containsKey(dataLocation))
 							valueMap.put(dataLocation, new HashMap<DsrTarget, Value>());	
 						valueMap.get(dataLocation).put(target, getDsrValue(dataElement, dataLocation, period));
 					}	
 				}				
 			}
 		}				
 			
 		targetCategories = getTargetCategories(program);
 		Collections.sort(targetCategories);
 		
 		DsrTable dsrTable = new DsrTable(valueMap, targets, targetCategories);
 		if (log.isDebugEnabled()) log.debug("getDsrTable(...)="+dsrTable);
 		return dsrTable;
 	}
 
 	private Value getDsrValue(DataElement dataElement, DataLocation dataLocation, Period period){
 		Value value = null;
 		DataValue dataValue = valueService.getDataElementValue(dataElement, dataLocation, period);
 		if (dataValue != null) value = dataValue.getValue();
 		
 		return value;
 	}
 	
 	private Value getDsrValue(DsrTarget target, Calculation calculation, CalculationLocation location, Period period, Set<DataLocationType> types) {
 		Value value = null;
 		SumValue calculationValue = (SumValue) valueService.getCalculationValue(calculation, location, period, types);
 		if(calculationValue != null){
			if(target.getAverage()) value = calculationValue.getAverage();
 			else value = calculationValue.getValue();
 		}		
 		return value;
	}	
 	
 	public List<DsrTargetCategory> getTargetCategories(ReportProgram program){
 		Set<DsrTargetCategory> categories = new HashSet<DsrTargetCategory>();
 		List<DsrTarget> targets = reportService.getReportTargets(DsrTarget.class, program);
 		for(DsrTarget target : targets)
 			if(target.getCategory() != null) categories.add(target.getCategory());
 		return new ArrayList<DsrTargetCategory>(categories);	
 	}
 
 	public void setReportService(ReportService reportService) {
 		this.reportService = reportService;
 	}
 	
 	public void setValueService(ValueService valueService) {
 		this.valueService = valueService;
 	}
 	
 	public void setDataService(DataService dataService) {
 		this.dataService = dataService;
 	}
 	
 	public void setSkipLevels(Set<String> skipLevels) {
 		this.skipLevels = skipLevels;
 	}
 
 	public Set<LocationLevel> getSkipLocationLevels(){
 		return reportService.getSkipLocationLevels(skipLevels);
 	}
 }
