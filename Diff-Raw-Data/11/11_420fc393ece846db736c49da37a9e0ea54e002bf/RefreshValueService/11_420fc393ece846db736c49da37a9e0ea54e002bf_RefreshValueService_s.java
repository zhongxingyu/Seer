 package org.chai.kevin.value;
 
 import grails.plugin.springcache.annotations.CacheFlush;
 
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.chai.kevin.LocationService;
 import org.chai.kevin.data.Calculation;
 import org.chai.kevin.data.DataService;
 import org.chai.kevin.data.NormalizedDataElement;
 import org.chai.kevin.location.CalculationEntity;
 import org.chai.kevin.location.DataEntityType;
 import org.chai.kevin.location.DataLocationEntity;
 import org.codehaus.groovy.grails.commons.GrailsApplication;
 import org.hibernate.CacheMode;
 import org.hibernate.FlushMode;
 import org.hibernate.Query;
 import org.hibernate.SessionFactory;
 import org.hisp.dhis.period.Period;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 public class RefreshValueService {
 
 	private final static Log log = LogFactory.getLog(RefreshValueService.class);
 	
 	private DataService dataService;
 	private SessionFactory sessionFactory;
 	private ExpressionService expressionService;
 	private ValueService valueService;
 	private GrailsApplication grailsApplication;
 	
 	@Transactional(readOnly = false, propagation=Propagation.REQUIRES_NEW)
 	public void refreshNormalizedDataElementInTransaction(NormalizedDataElement normalizedDataElement) {
 		refreshNormalizedDataElement(normalizedDataElement);
 	}
 	
 	public void refreshNormalizedDataElement(NormalizedDataElement normalizedDataElement) {
 		if (log.isDebugEnabled()) log.debug("refreshNormalizedDataElement(normalizedDataElement="+normalizedDataElement+")");
 		sessionFactory.getCurrentSession().setFlushMode(FlushMode.COMMIT);
 		sessionFactory.getCurrentSession().setCacheMode(CacheMode.IGNORE);
 		
 		valueService.deleteValues(normalizedDataElement);
 		for (Iterator<Object[]> iterator = getCombinations(DataLocationEntity.class); iterator.hasNext();) {
 			Object[] row = (Object[]) iterator.next();
 			DataLocationEntity dataLocationEntity = (DataLocationEntity)row[0];
 			Period period = (Period)row[1];
 			NormalizedDataElementValue value = expressionService.calculateValue(normalizedDataElement, dataLocationEntity, period);				
 			valueService.save(value);
 		}
 		normalizedDataElement.setCalculated(new Date());
 		dataService.save(normalizedDataElement);
 	}
 	
 	@Transactional(readOnly = false, propagation=Propagation.REQUIRES_NEW)
 	public void refreshCalculationInTransaction(Calculation<?> calculation) {
 		refreshCalculation(calculation);
 	}
 	
 	public void refreshCalculation(Calculation<?> calculation) {
 		if (log.isDebugEnabled()) log.debug("refreshCalculation(calculation="+calculation+")");
 		sessionFactory.getCurrentSession().setFlushMode(FlushMode.COMMIT);
 		sessionFactory.getCurrentSession().setCacheMode(CacheMode.IGNORE);
 		
 		valueService.deleteValues(calculation);
 		for (Iterator<Object[]> iterator = getCombinations(CalculationEntity.class); iterator.hasNext();) {
 			Object[] row = (Object[]) iterator.next();
 			CalculationEntity entity = (CalculationEntity)row[0];
 			Period period = (Period)row[1];
 			refreshCalculation(calculation, entity, period);
 		}
 		calculation.setCalculated(new Date());
 		dataService.save(calculation);
 	}
 
 	@Transactional(readOnly = true)
 	@CacheFlush({"dsrCache", "dashboardCache", "fctCache"})
 	public void refreshNormalizedDataElements() {
 		sessionFactory.getCurrentSession().setFlushMode(FlushMode.COMMIT);
 		sessionFactory.getCurrentSession().setCacheMode(CacheMode.IGNORE);
 		
 		// TODO get only those who need to be refreshed
 		List<NormalizedDataElement> normalizedDataElements = sessionFactory.getCurrentSession().createCriteria(NormalizedDataElement.class).list();
 		
 		for (NormalizedDataElement normalizedDataElement : normalizedDataElements) {
 			if (normalizedDataElement.getCalculated() == null || normalizedDataElement.getCalculated().before(normalizedDataElement.getTimestamp())) {
 				getMe().refreshNormalizedDataElementInTransaction(normalizedDataElement);
 				sessionFactory.getCurrentSession().clear();
 			}
 		}
 	}
 	
 	@Transactional(readOnly = true)
 	@CacheFlush({"dsrCache", "dashboardCache", "fctCache"})
 	public void refreshCalculations() {
 		sessionFactory.getCurrentSession().setFlushMode(FlushMode.COMMIT);
 		sessionFactory.getCurrentSession().setCacheMode(CacheMode.IGNORE);
 		
 		// TODO get only those who need to be refreshed
 		List<Calculation<?>> calculations = sessionFactory.getCurrentSession().createCriteria(Calculation.class).list();
 		
 		for (Calculation<?> calculation : calculations) {
 			if (calculation.getCalculated() == null || calculation.getCalculated().before(calculation.getTimestamp())) {
 				getMe().refreshCalculationInTransaction(calculation);
 				sessionFactory.getCurrentSession().clear();
 			}
 		}
 	}
 	
//	@Transactional(readOnly = true)
//	public boolean isUpToDate(Calculation<CalculationPartialValue> calculation, CalculationEntity location, Period period) {
//		List<CalculationPartialValue> values = valueService.getPartialValues(calculation, location, period);
//		for (CalculationPartialValue calculationPartialValue : values) {
//			if (calculationPartialValue.getTimestamp().before(calculation.getTimestamp())) return false;
//		}
//		return true;
//	}
	
	@Transactional(readOnly = true)
 	public void refreshCalculation(Calculation<?> calculation, CalculationEntity entity, Period period) {
 		for (CalculationPartialValue partialValue : expressionService.calculatePartialValues(calculation, entity, period)) {
 			valueService.save(partialValue);
 		}
 	}
 
 	private <T extends CalculationEntity> Iterator<Object[]> getCombinations(Class<T> clazz) {
 		Query query = sessionFactory.getCurrentSession().createQuery(
 				"select entity, period " +
 				"from "+clazz.getSimpleName()+" entity, Period period"
 		).setCacheable(false);
 		return query.iterate();
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
 	
 	public void setGrailsApplication(GrailsApplication grailsApplication) {
 		this.grailsApplication = grailsApplication;
 	}
 	
 	// for internal call through transactional proxy
 	public RefreshValueService getMe() {
 		return grailsApplication.getMainContext().getBean(RefreshValueService.class);
 	}
 	
 }
