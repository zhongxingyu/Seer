 package org.chai.kevin.planning;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import org.chai.kevin.LocationService;
 import org.chai.kevin.data.DataService;
 import org.chai.kevin.data.Enum;
 import org.chai.kevin.data.Type;
 import org.chai.kevin.location.DataEntityType;
 import org.chai.kevin.location.DataLocationEntity;
 import org.chai.kevin.location.LocationEntity;
 import org.chai.kevin.planning.budget.BudgetCost;
 import org.chai.kevin.planning.budget.PlanningEntryBudget;
 import org.chai.kevin.planning.budget.PlanningTypeBudget;
 import org.chai.kevin.value.NormalizedDataElementValue;
 import org.chai.kevin.value.RawDataElementValue;
 import org.chai.kevin.value.RefreshValueService;
 import org.chai.kevin.value.SumValue;
 import org.chai.kevin.value.Value;
 import org.chai.kevin.value.ValueService;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.transaction.annotation.Transactional;
 
 public class PlanningService {
 
 	private ValueService valueService;
 	private DataService dataService;
 	private RefreshValueService refreshValueService;
 	private LocationService locationService;
 	private SessionFactory sessionFactory;
 	
 	public Planning getDefaultPlanning() {
 		return (Planning)sessionFactory.getCurrentSession()
 				.createCriteria(Planning.class).add(Restrictions.eq("active", true)).uniqueResult();
 	}
 	
 	@Transactional(readOnly=true)
 	public PlanningSummaryPage getSummaryPage(Planning planning, LocationEntity location) {
 		List<DataLocationEntity> dataEntities = location.collectDataLocationEntities(null, null);
 		Map<PlanningType, PlanningTypeSummary> summaries = new HashMap<PlanningType, PlanningTypeSummary>();
 		for (PlanningType planningType : planning.getPlanningTypes()) {
 			summaries.put(planningType, getPlanningTypeSummary(planningType, dataEntities));
 		}
 		
 		return new PlanningSummaryPage(planning.getPlanningTypes(), dataEntities, summaries);
 	}
 	
 	// TODO move to planning type
 	private PlanningTypeSummary getPlanningTypeSummary(PlanningType planningType, List<DataLocationEntity> dataEntities) {
 		Map<DataLocationEntity, Integer> numberOfEntries = new HashMap<DataLocationEntity, Integer>();
 		for (DataLocationEntity entity : dataEntities) {
 			numberOfEntries.put(entity, getPlanningList(planningType, entity).getPlanningEntries().size());
 		}
 		return new PlanningTypeSummary(planningType, numberOfEntries);
 	}
 	
 	@Transactional(readOnly=true)
 	public PlanningList getPlanningList(PlanningType type, DataLocationEntity location) {
 		RawDataElementValue dataElementValue = getDataElementValue(type, location);
 		
 		return new PlanningList(type, dataElementValue, getEnums(type));
 	}
 	
 //	public PlanningEntry getPlanningEntry(PlanningType type, DataLocationEntity location, Integer lineNumber) {
 //		RawDataElementValue dataElementValue = getDataElementValue(type, location);
 //		
 //		return new PlanningEntry(type, dataElementValue, lineNumber, getEnums(type));
 //	}
 
 	private Map<String, Enum> getEnums(PlanningType type) {
 		Map<String, Enum> result = new HashMap<String, Enum>();
 		for (Entry<String, Type> prefix : type.getDataElement().getEnumPrefixes().entrySet()) {
 			result.put(prefix.getValue().getEnumCode(), dataService.findEnumByCode(prefix.getValue().getEnumCode()));
 		}
 		return result;
 	}
 	
 	private RawDataElementValue getDataElementValue(PlanningType type, DataLocationEntity location) {
 		RawDataElementValue dataElementValue = valueService.getDataElementValue(type.getDataElement(), location, type.getPeriod());
 		if (dataElementValue == null) {
 			dataElementValue = new RawDataElementValue(type.getDataElement(), location, type.getPeriod(), Value.NULL_INSTANCE());
 		}
 		return dataElementValue;
 	}
 	
 	@Transactional(readOnly=false)
 	public void deletePlanningEntry(PlanningType type, DataLocationEntity location, Integer lineNumber) {
 		PlanningList planningList = getPlanningList(type, location);
 		PlanningEntry planningEntry = planningList.getPlanningEntries().get(lineNumber);
 		planningEntry.delete();
 		planningList.save(valueService);
 	}
 	
 	@Transactional(readOnly=false)
 	public PlanningEntry modify(PlanningType type, DataLocationEntity location, Integer lineNumber, Map<String, Object> params) {
 		PlanningList planningList = getPlanningList(type, location);
 		PlanningEntry planningEntry = planningList.getOrCreatePlanningEntry(lineNumber);
 		
 		// first we merge the values to create a new value
 		planningEntry.mergeValues(params);
 		planningEntry.setBudgetUpdated(false);
 		
 		// second we run the validation rules
 		// TODO
 		
 		// last we set and save the value
 		planningList.save(valueService);
 		return planningEntry;
 	}
 	
 	@Transactional(readOnly=false)
 	public void submit(PlanningType type, DataLocationEntity location, Integer lineNumber) {
 		PlanningList planningList = getPlanningList(type, location);
 		PlanningEntry planningEntry = planningList.getOrCreatePlanningEntry(lineNumber);
 		
 		// we submit the entry
 		planningEntry.setSubmitted(true);
 				
 		// then we recalculate the budget
 		refreshBudget(planningEntry, location);
 		
 		// last we save the value
 		planningList.save(valueService);
 	}
 	
 	@Transactional(readOnly=false)
 	public void unsubmit(PlanningType type, DataLocationEntity location, Integer lineNumber) {
 		PlanningList planningList = getPlanningList(type, location);
 		PlanningEntry planningEntry = planningList.getOrCreatePlanningEntry(lineNumber);
 		
 		// we submit the entry
 		planningEntry.setSubmitted(false);
 				
 		// last we save the value
 		planningList.save(valueService);
 	}
 		
 	@Transactional(readOnly=false)
 	public void refreshBudget(PlanningType type, DataLocationEntity location) {
 		PlanningList planningList = getPlanningList(type, location);
 		for (PlanningEntry planningEntry : planningList.getPlanningEntries()) {
 			refreshBudget(planningEntry, location);
 		}
 		planningList.save(valueService);
 	}
 	
 	private void refreshBudget(PlanningEntry planningEntry, DataLocationEntity location) {
 		if (planningEntry.isSubmitted() && !planningEntry.isBudgetUpdated()) {
 			for (PlanningCost cost : planningEntry.getPlanningCosts()) {
 				refreshValueService.refreshNormalizedDataElement(cost.getDataElement(), location, cost.getPlanningType().getPeriod());
 			}
 			planningEntry.setBudgetUpdated(true);
 		}
 	}
 	
 	@Transactional(readOnly=true)
 	public PlanningTypeBudget getPlanningTypeBudget(PlanningType type, DataLocationEntity location) {
 		PlanningList planningList = getPlanningList(type, location);
 		
 		Set<DataEntityType> types = new HashSet<DataEntityType>();
 		types.add(location.getType());
 		
 		List<PlanningEntryBudget> planningEntryBudgets = new ArrayList<PlanningEntryBudget>();
 		for (PlanningEntry planningEntry : planningList.getPlanningEntries()) {
 			if (planningEntry.isSubmitted()) {
 				Map<PlanningCost, BudgetCost> budgetCosts = new HashMap<PlanningCost, BudgetCost>();
 				for (PlanningCost planningCost : planningEntry.getPlanningCosts()) {
 					NormalizedDataElementValue value = valueService.getDataElementValue(planningCost.getDataElement(), location, type.getPeriod());
					if (!value.getValue().isNull()) budgetCosts.put(planningCost, new BudgetCost(planningEntry, planningCost, value));
 				}
 				planningEntryBudgets.add(new PlanningEntryBudget(planningEntry, budgetCosts));
 			}
 		}
 		
 		return new PlanningTypeBudget(type, planningList, planningEntryBudgets);
 	}
 	
 	public void setDataService(DataService dataService) {
 		this.dataService = dataService;
 	}
 	
 	public void setValueService(ValueService valueService) {
 		this.valueService = valueService;
 	}
 
 	public void setRefreshValueService(RefreshValueService refreshValueService) {
 		this.refreshValueService = refreshValueService;
 	}
 	public void setLocationService(LocationService locationService) {
 		this.locationService = locationService;
 	}
 
 	public void setSessionFactory(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 }
