 package org.chai.kevin.value;
 
 import grails.plugin.springcache.annotations.CacheFlush;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.chai.kevin.Period;
 import org.chai.kevin.PeriodService;
 import org.chai.kevin.data.Calculation;
 import org.chai.kevin.data.Data;
 import org.chai.kevin.data.DataElement;
 import org.chai.kevin.data.DataService;
 import org.chai.kevin.data.NormalizedDataElement;
 import org.chai.kevin.data.RawDataElement;
 import org.chai.kevin.location.CalculationLocation;
 import org.chai.kevin.location.DataLocation;
 import org.chai.kevin.location.DataLocationType;
 import org.chai.kevin.task.Progress;
 import org.hibernate.Criteria;
 import org.hibernate.LockMode;
 import org.hibernate.LockOptions;
 import org.hibernate.Query;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Projections;
 import org.springframework.transaction.PlatformTransactionManager;
 import org.springframework.transaction.TransactionStatus;
 import org.springframework.transaction.annotation.Isolation;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.transaction.support.TransactionCallbackWithoutResult;
 import org.springframework.transaction.support.TransactionTemplate;
 
 public class RefreshValueService {
 
 	private final static Log log = LogFactory.getLog(RefreshValueService.class);
 	
 	private PeriodService periodService;
 	private DataService dataService;
 	private SessionFactory sessionFactory;
 	private PlatformTransactionManager transactionManager;
 	private ExpressionService expressionService;
 	private ValueService valueService;
 	
 	private TransactionTemplate transactionTemplate;
 	
 	private TransactionTemplate getTransactionTemplate() {
 		if (transactionTemplate == null) {
 			transactionTemplate = new TransactionTemplate(transactionManager);
 			transactionTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
 		}
 		return transactionTemplate;
 	}
 	
 	@Transactional(readOnly = true)
 	public void refreshNormalizedDataElements(final Progress progress) {
 		final List<NormalizedDataElement> normalizedDataElements = sessionFactory.getCurrentSession().createCriteria(NormalizedDataElement.class).list();
 		while (!normalizedDataElements.isEmpty()) {
 			final NormalizedDataElement normalizedDataElement = normalizedDataElements.get(0);
 			
 			
 			List<NormalizedDataElement> uptodateElements = refreshNormalizedDataElement(normalizedDataElement, progress);
 			normalizedDataElements.removeAll(uptodateElements);
 		}
 	}
 	
 	@Transactional(readOnly = true)
 	public List<NormalizedDataElement> refreshNormalizedDataElement(NormalizedDataElement normalizedDataElement, Progress progress) {
 		// set progress maximum - we count the number of NormalizedDataElement dependencies
 		Set<Data> dependencySet = new HashSet<Data>();
 		collectOrderedDependencies(normalizedDataElement, dependencySet, NormalizedDataElement.class);
 		removeNulls(dependencySet);
 		progress.setMaximum(dependencySet.size() * periodService.listPeriods().size() * countLocations(DataLocation.class));
 		
 		// run the actual expression calculations
 		List<NormalizedDataElement> uptodateElements = new ArrayList<NormalizedDataElement>();
 		refreshDataElement(normalizedDataElement, uptodateElements, progress);
 		
 		return uptodateElements;
 	}
 	
 	private static List<?> NULL_LIST = new ArrayList();
 	
 	static{
 		NULL_LIST.add(null);
 	}
 	
 	private void removeNulls(Collection<?> list) {
 		list.removeAll(NULL_LIST);
 	}
 	
 	private Date refreshDataElement(DataElement dataElement, List<NormalizedDataElement> uptodateElements, Progress progress) {
 		if (dataElement instanceof RawDataElement) return dataElement.getLastValueChanged();
 		else {
 			NormalizedDataElement normalizedDataElement = (NormalizedDataElement)dataElement;
 			Date latestDependency = null;
 			
 			List<DataElement> dependencies = new ArrayList<DataElement>();
 			for (String expression : normalizedDataElement.getExpressions()) {
 				dependencies.addAll(expressionService.getDataInExpression(expression, DataElement.class).values());
 			}
 			removeNulls(dependencies);
 			
 			uptodateElements.add(normalizedDataElement);
 			for (DataElement dependency : dependencies) {
 				if (!uptodateElements.contains(dependency)) {
 					Date dependencyDate = refreshDataElement(dependency, uptodateElements, progress);
 					if (latestDependency == null || (dependencyDate != null && dependencyDate.after(latestDependency))) latestDependency = dependencyDate;
 				}
 			}
 			
 			// we refresh if the data element was changed after the last refresh
 			if (normalizedDataElement.getRefreshed() == null || normalizedDataElement.getTimestamp().after(normalizedDataElement.getRefreshed())) {
 				refreshNormalizedDataElementOnly(normalizedDataElement, progress);
 			}
 			
 			// we refresh if a value was saved after the last refresh
 			else if (normalizedDataElement.getRefreshed() == null || normalizedDataElement.getLastValueChanged().after(normalizedDataElement.getRefreshed())) {
 				refreshNormalizedDataElementOnly(normalizedDataElement, progress);
 			}
 			
 			// we refresh if the last value of the dependency is after the last refreshed date of this element
 			// this means some values of the dependency were changed after us
 			else if (latestDependency != null && latestDependency.after(normalizedDataElement.getRefreshed())) {
 				refreshNormalizedDataElementOnly(normalizedDataElement, progress);
 			}
 			
 			else {
 				progress.incrementProgress(periodService.listPeriods().size() * countLocations(DataLocation.class));
 			}
 			
 			return normalizedDataElement.getLastValueChanged();
 		}
 	}
 	
 	@Transactional(readOnly = false)
 	public void refreshNormalizedDataElement(NormalizedDataElement dataElement, DataLocation dataLocation, Period period) {
 		refreshDataElement(dataElement, dataLocation, period, new ArrayList<NormalizedDataElement>());
 		dataService.save(dataElement);
 	}
 	
 	private Date refreshDataElement(DataElement dataElement, DataLocation dataLocation, Period period, List<NormalizedDataElement> uptodateElements) {
 		DataValue storedValue = valueService.getDataElementValue(dataElement, dataLocation, period);
 		
 		if (dataElement instanceof RawDataElement) {
 			if (storedValue == null) return dataElement.getTimestamp();
 			return storedValue.getTimestamp();
 		}
 		else {
 			NormalizedDataElement normalizedDataElement = (NormalizedDataElement)dataElement;
 			Date latestDependency = null;
 			
 			List<DataElement> dependencies = new ArrayList<DataElement>();
 			for (String expression : normalizedDataElement.getExpressions()) {
 				dependencies.addAll(expressionService.getDataInExpression(expression, DataElement.class).values());
 			}
 			removeNulls(dependencies);
 			
 			uptodateElements.add(normalizedDataElement);
 			for (DataElement dependency : dependencies) {
 				if (!uptodateElements.contains(dependency)) {
 					Date dependencyDate = refreshDataElement(dependency, dataLocation, period, uptodateElements);
 					if (latestDependency == null || (dependencyDate != null && dependencyDate.after(latestDependency))) latestDependency = dependencyDate;
 				}
 			}
 			
 			// we refresh if the value does not exist
 			if (storedValue == null) {
 				storedValue = updateNormalizedDataElementValue(normalizedDataElement, dataLocation, period);
 			}
 			
 			// we refresh if the timestamp of the dataElement is bigger than that of the value
 			else if (dataElement.getTimestamp().after(storedValue.getTimestamp())) {
 				storedValue = updateNormalizedDataElementValue(normalizedDataElement, dataLocation, period);
 			}
 			
 			// we refresh if the timestamp of the dependency is after the timestamp of this value
 			else if (latestDependency != null && latestDependency.after(storedValue.getTimestamp())) {
 				storedValue = updateNormalizedDataElementValue(normalizedDataElement, dataLocation, period);
 			}
 			
 			return storedValue.getTimestamp();
 		}
 	}
 	
 	public NormalizedDataElementValue updateNormalizedDataElementValue(NormalizedDataElement normalizedDataElement, DataLocation dataLocation, Period period) {
 		NormalizedDataElementValue newValue = expressionService.calculateValue(normalizedDataElement, dataLocation, period);
 		NormalizedDataElementValue oldValue = valueService.getDataElementValue(normalizedDataElement, dataLocation, period);
 		
 		if (oldValue == null) oldValue = newValue;
 		else {
 			oldValue.setValue(newValue.getValue());
 			oldValue.setStatus(newValue.getStatus());
 		}
 		
 		valueService.save(oldValue);
 		normalizedDataElement.setLastValueChanged(new Date());
 		
 		sessionFactory.getCurrentSession().evict(oldValue);
 		return oldValue;
 	}
 	
 	private void refreshNormalizedDataElementOnly(final NormalizedDataElement normalizedDataElement, final Progress progress) {
 		if (log.isDebugEnabled()) log.debug("refreshNormalizedDataElement(normalizedDataElement="+normalizedDataElement+")");
 		
 		getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
 			@Override
 			protected void doInTransactionWithoutResult(TransactionStatus arg0) {
				NormalizedDataElement newNormalizedDataElement = dataService.getData(normalizedDataElement.getId(), NormalizedDataElement.class);
				
 				for (Iterator<Object[]> iterator = getCombinations(DataLocation.class); iterator.hasNext();) {
 					Object[] row = (Object[]) iterator.next();
 					DataLocation dataLocation = (DataLocation)row[0];
 					Period period = (Period)row[1];
 					
 					// TODO improve performance by getting all values at the same time
					updateNormalizedDataElementValue(newNormalizedDataElement, dataLocation, period);
 					progress.incrementProgress();
 				}
 				
				newNormalizedDataElement.setRefreshed(new Date());
				dataService.save(newNormalizedDataElement);
 			}
 		});
 		
 		sessionFactory.getCurrentSession().clear();
 	}
 	
 	private void collectOrderedDependencies(Data dataElement, Collection<Data> dependencies, Class<? extends Data> clazz) {
 		dependencies.add(dataElement);
 		if (dataElement instanceof NormalizedDataElement) {
 			NormalizedDataElement normalizedDataElement = (NormalizedDataElement)dataElement;
 			for (String expression : normalizedDataElement.getExpressions()) {
 				Map<String, ? extends Data> dependenciesMap = expressionService.getDataInExpression(expression, clazz);
 				for (Data dependency : dependenciesMap.values()) {
 					if (dependency != null && !dependencies.contains(dependency)) collectOrderedDependencies(dependency, dependencies, clazz);
 				}
 			}
 		}
 	}
 	
 	@Transactional(readOnly = true)
 	public void refreshCalculations(final Progress progress) {
 		// TODO get only those who need to be refreshed
 		List<Calculation<?>> calculations = sessionFactory.getCurrentSession().createCriteria(Calculation.class).list();
 		
 		for (final Calculation<?> calculation : calculations) {
 			refreshCalculation(calculation, progress);
 		}
 	}
 	
 	@Transactional(readOnly = true)
 	public void refreshCalculation(Calculation<?> calculation, Progress progress) {
 		// set progress maximum - we count the number of NormalizedDataElement dependencies
 		Set<Data> dependencySet = new HashSet<Data>();
 		collectOrderedDependencies(calculation, dependencySet, NormalizedDataElement.class);
 		removeNulls(dependencySet);
 		progress.setMaximum(
 				// all dependent NDEs
 				(dependencySet.size() * periodService.listPeriods().size() * countLocations(DataLocation.class)) +
 				// the calculation itself
 				(periodService.listPeriods().size() * countLocations(CalculationLocation.class))
 		);
 		
 		// refresh the calculation
 		Map<String, DataElement> dependenciesMap = expressionService.getDataInExpression(calculation.getExpression(), DataElement.class);
 		
 		Date latestDependency = null;
 		for (DataElement<?> dependency : dependenciesMap.values()) {
 			Date dependencyDate = refreshDataElement(dependency, new ArrayList<NormalizedDataElement>(), progress);
 			if (latestDependency == null || (dependencyDate != null && dependencyDate.after(latestDependency))) latestDependency = dependencyDate;
 		}
 		
 		// we refresh if the data element was changed after the last refresh
 		if (calculation.getRefreshed() == null || calculation.getTimestamp().after(calculation.getRefreshed())) {
 			refreshCalculationOnly(calculation, progress);
 		}
 		
 		// we refresh if a value was saved after the last refresh
 		else if (calculation.getRefreshed() == null || calculation.getLastValueChanged().after(calculation.getRefreshed())) {
 			refreshCalculationOnly(calculation, progress);
 		}
 		
 		// we refresh if the last value of the dependency is after the last refreshed date of this element
 		// this means some values of the dependency were changed after us
 		else if (latestDependency != null && latestDependency.after(calculation.getRefreshed())) {
 			refreshCalculationOnly(calculation, progress);
 		}
 		
 		else {
 			progress.incrementProgress(periodService.listPeriods().size() * countLocations(CalculationLocation.class));
 		}
 	}
 	
 	public void updateCalculationPartialValues(Calculation<?> calculation, CalculationLocation location, Period period) {
 		List<CalculationPartialValue> oldPartialValues = (List<CalculationPartialValue>)valueService.getPartialValues(calculation, location, period);
 		Map<DataLocationType, CalculationPartialValue> oldPartialValueMap = new HashMap<DataLocationType, CalculationPartialValue>();
 		for (CalculationPartialValue oldPartialValue : oldPartialValues) {
 			oldPartialValueMap.put(oldPartialValue.getType(), oldPartialValue);
 		}
 		
 		for (CalculationPartialValue newPartialValue : expressionService.calculatePartialValues(calculation, location, period)) {
 			CalculationPartialValue oldPartialValue = oldPartialValueMap.get(newPartialValue.getType());
 			
 			if (oldPartialValue == null) oldPartialValue = newPartialValue;
 			else {
 				oldPartialValue.setValue(newPartialValue.getValue());
 			}
 			valueService.save(oldPartialValue);
 			sessionFactory.getCurrentSession().evict(oldPartialValue);
 		}
 		calculation.setLastValueChanged(new Date());
 	}
 	
 	private void refreshCalculationOnly(final Calculation<?> calculation, final Progress progress) {
 		if (log.isDebugEnabled()) log.debug("refreshCalculation(calculation="+calculation+")");
 		
 		getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
 			@Override
 			protected void doInTransactionWithoutResult(TransactionStatus arg0) {
				Calculation<?> newCalculation = dataService.getData(calculation.getId(), Calculation.class);
				
 				for (Iterator<Object[]> iterator = getCombinations(CalculationLocation.class); iterator.hasNext();) {
 					Object[] row = (Object[]) iterator.next();
 					CalculationLocation location = (CalculationLocation)row[0];
 					Period period = (Period)row[1];
 					
 					// TODO improve performance by getting all values at the same time
					updateCalculationPartialValues(newCalculation, location, period);
 					progress.incrementProgress();
 				}
 				
				newCalculation.setRefreshed(new Date());
				dataService.save(newCalculation);
 			}
 		});
 		
 		sessionFactory.getCurrentSession().clear();
 	}
 
 	@CacheFlush(caches={"dsrCache", "dashboardCache", "fctCache"})
 	public void flushCaches() { }
 	
 	// TODO move to location service ?
 	private <T extends CalculationLocation> Iterator<Object[]> getCombinations(Class<T> clazz) {
 		Query query = sessionFactory.getCurrentSession().createQuery(
 				"select location, period from "+clazz.getSimpleName()+" location, Period period"
 		).setCacheable(true).setReadOnly(true);
 		return query.iterate();
 	}
 	
 	// TODO move to location service ?
 	private <T extends CalculationLocation> Long countLocations(Class<T> clazz) {
 		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(clazz);
 		criteria.setCacheable(true).setReadOnly(true);
 		return (Long)criteria.setProjection(Projections.rowCount()).uniqueResult();
 	}
 	
 	public void setPeriodService(PeriodService periodService) {
 		this.periodService = periodService;
 	}
 	
 	public void setSessionFactory(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 	
 	public void setExpressionService(ExpressionService expressionService) {
 		this.expressionService = expressionService;
 	}
 	
 	public void setValueService(ValueService valueService) {
 		this.valueService = valueService;
 	}
 	
 	public void setDataService(DataService dataService) {
 		this.dataService = dataService;
 	}
 	
 	public void setTransactionManager(PlatformTransactionManager transactionManager) {
 		this.transactionManager = transactionManager;
 	}
 	
 }
