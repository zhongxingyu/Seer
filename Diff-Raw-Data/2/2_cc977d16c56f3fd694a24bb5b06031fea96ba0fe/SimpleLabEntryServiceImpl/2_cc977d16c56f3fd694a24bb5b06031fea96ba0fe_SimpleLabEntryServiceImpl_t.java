 package org.openmrs.module.simplelabentry.impl;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Cohort;
 import org.openmrs.Concept;
 import org.openmrs.ConceptSet;
 import org.openmrs.Encounter;
 import org.openmrs.EncounterType;
 import org.openmrs.Location;
 import org.openmrs.Obs;
 import org.openmrs.Order;
 import org.openmrs.OrderType;
 import org.openmrs.Patient;
 import org.openmrs.PatientIdentifierType;
 import org.openmrs.PatientProgram;
 import org.openmrs.PatientState;
 import org.openmrs.PersonAttributeType;
 import org.openmrs.Program;
 import org.openmrs.ProgramWorkflow;
 import org.openmrs.api.APIException;
 import org.openmrs.api.OrderService.ORDER_STATUS;
 import org.openmrs.api.context.Context;
 import org.openmrs.api.impl.BaseOpenmrsService;
 import org.openmrs.module.simplelabentry.SimpleLabEntryService;
 import org.openmrs.module.simplelabentry.report.ConceptColumn;
 import org.openmrs.module.simplelabentry.report.DataSet;
 import org.openmrs.module.simplelabentry.report.ExcelReportRenderer;
 import org.openmrs.module.simplelabentry.report.LabOrderReport;
 import org.openmrs.module.simplelabentry.util.DateUtil;
 import org.openmrs.module.simplelabentry.util.SimpleLabEntryUtil;
 
 /**
  * Default implementation of the SimpleLabEntry-related services class.
  * 
  * This method should not be invoked by itself.  Spring injection is used
  * to inject this implementation into the ServiceContext.  Which 
  * implementation is injected is determined by the spring application 
  * context file: /metadata/api/spring/applicationContext.xml
  * 
  * @see org.openmrs.module.simplelabentry.SimpleLabEntryService
  */
 public class SimpleLabEntryServiceImpl extends BaseOpenmrsService implements SimpleLabEntryService {
 	
 	protected static final Log log = LogFactory.getLog(SimpleLabEntryServiceImpl.class);
 	
 	/**
      * @see org.openmrs.module.simplelabentry.SimpleLabEntryService#getOrders(OrderType, Concept, Location, Date, ORDER_STATUS, List<Patient>)
      */
 	public List<Order> getLabOrders(Concept concept, Location location, Date orderDate, ORDER_STATUS status, List<Patient> patients) {
 		
 		List<Order> orderList = new ArrayList<Order>();
 		
 		// Retrieve proper OrderType for Lab Orders
 		OrderType orderType = SimpleLabEntryUtil.getLabOrderType();
 		
 		log.debug("Retrieving lab orders of type "+orderType+" for: location="+location+",concept="+concept+",date="+orderDate+",status="+status+",patients="+patients);
 		
 		List<Concept> conceptList = null;
 		if (concept == null) {
 			conceptList = getSupportedLabSets();
 		}
 		else {
 			conceptList = Arrays.asList(concept);
 		}
 		
 		if (status == null) {
 			status = ORDER_STATUS.NOTVOIDED;
 		}
 		
 		// Retrieve matching orders
 		List<Order> ordersMatch = Context.getOrderService().getOrders(Order.class, patients, conceptList, status, null, null, Arrays.asList(orderType));
 		for (Order o : ordersMatch) {
 			Encounter e = o.getEncounter();
 			if (location != null && !location.equals(e.getLocation())) {
 				continue; // Order Location Does Not Match
 			}
 			// TODO: This shouldn't be necessary, but it seems like the OrderService does not do it correctly?
 			if (status != null) {
 				if ( (status == ORDER_STATUS.CURRENT && o.isDiscontinued()) || (status == ORDER_STATUS.COMPLETE && o.isCurrent()) ) {
 					continue;
 				}
 			}
 			if (orderDate != null) {
 				Date orderStartDate = o.getStartDate() != null ? o.getStartDate() : e.getEncounterDatetime();
 				if (orderStartDate == null || (!Context.getDateFormat().format(orderDate).equals(Context.getDateFormat().format(orderStartDate)))) {
 					continue; // Order Start Date Does Not Match
 				}
 			}
 			log.debug("Adding lab order: " + o);
 			orderList.add(o);
 		}
 		
 		return orderList;
 	}
 	
 	/**
      * @see org.openmrs.module.simplelabentry.SimpleLabEntryService#getLabTestConceptSets()
      */
 	public List<Concept> getSupportedLabSets() {
     	List<Concept> ret = new ArrayList<Concept>();
     	String testProp = Context.getAdministrationService().getGlobalProperty("simplelabentry.supportedTests");
     	if (testProp != null) {
     		for (String s : testProp.split(",")) {
     			Concept foundConcept = null;
     			try {
     				foundConcept = Context.getConceptService().getConcept(Integer.valueOf(s));
     			}
     			catch (Exception e) {}
     			if (foundConcept == null) {
     				throw new RuntimeException("Unable to retrieve LabTest with id = " + s + ". Please check global property configuration.");
     			}
     			ret.add(foundConcept);
     		}
     	}
     	return ret;
 	}
 	
 	
 	/**
 	 * Get a list of concept IDs 
 	 * @return
 	 */
     public List<Concept> getSupportedLabConcepts() {    	
     	// orders the appropriate concepts according to requirements
     	return SimpleLabEntryUtil.getLabReportConcepts();
 	}		
 	
 	/**
 	 * Get a list of concept columns.
 	 * 
 	 * TODO If we need to override values like precision, we'll do it here.
 	 * 
 	 * @return
 	 */
     public List<ConceptColumn> getConceptColumns() {
     	List<ConceptColumn> columns = new ArrayList<ConceptColumn>();
     	for (Concept concept : getSupportedLabConcepts()) { 
     		try { 
     			columns.add(new ConceptColumn(concept));
     		} catch (Exception e) { 
     			log.error("Error occurred while looking up concept / concept set by ID " + concept.getConceptId(), e);
     			throw new APIException("Invalid concept ID " + concept.getConceptId() + " : " + e.getMessage(), e);
     		}
     	}    	
     	return columns;
 	}	
 	
 	
     /**
      * 
      */
     public LabOrderReport runLabOrderReport(Location location, Date startDate, Date endDate) { 
 
     	LabOrderReport report = new LabOrderReport();    	
     	report.addParameter("location", location);
     	report.addParameter("startDate", startDate);
     	report.addParameter("endDate", endDate);
     	
     	//List<ConceptColumn> columns = getConceptColumns();
     	// TODO We should pass in the report here, but at the time I just needed to add parameters
     	// so that we could have access to them when we render the report.
     	List<Map<String,String>> dataSet = 
     		getLabOrderReportData(location, startDate, endDate);
     	
     	report.setDataSet(new DataSet(dataSet));
     	
     	return report;
     }
     
     
     /**
      * 
      */
 	public File runAndRenderLabOrderReport(Location location, Date startDate, Date endDate) throws IOException {
 		FileOutputStream fos = null;
 		try {
 		
 			LabOrderReport report = runLabOrderReport(location, startDate,endDate);
 			ExcelReportRenderer renderer = new ExcelReportRenderer();
			File file = new File("lab-order-report.xls");
 			fos = new FileOutputStream(file);
 			renderer.render(report, fos);
 			return file;
 
 		} catch (Exception e) {
 			log.error("Unable to render lab order report", e);
 		} finally {
 			if (fos != null) {
 				fos.flush();
 				fos.close();
 			}
 		}
 		return null;
 	}
     
 	
 	/**
 	 * @see DataSetEvaluator#evaluate(DataSetDefinition, EvaluationContext)
 	 */
 	public List<Map<String,String>> getLabOrderReportData(Location location, Date startDate, Date endDate) {
 				
 		List<Map<String,String>> dataset = new ArrayList<Map<String,String>>();
 				
 		log.info("Location=" + location + ", startDate=" + startDate  + ", endDate=" + endDate);
 		
 		List<Encounter> encounters = getLabOrderEncounters(location, startDate, endDate);
 		log.info("Encounters found: " + encounters.size());
 		
 		PatientIdentifierType identifierType = SimpleLabEntryUtil.getPatientIdentifierType();
 		
 		// Eagerly fetching and caching treatment groups so we don't have to do this for each patient
 		Cohort patients = SimpleLabEntryUtil.getCohort(encounters);		
 		Map<Integer, String> treatmentGroups = SimpleLabEntryUtil.getTreatmentGroupCache(patients);
 		
 		for (Encounter encounter : encounters) {			
 			log.info("Encounter for " + encounter.getPatient() + " on " + encounter.getEncounterDatetime());
 			Map<String,String> row = new LinkedHashMap<String,String>();
 			row.put("Patient ID", encounter.getPatient().getPatientId().toString());						
 			row.put("IMB ID", encounter.getPatient().getPatientIdentifier(identifierType).getIdentifier());
 			row.put("Family Name", encounter.getPatient().getFamilyName());
 			row.put("Given", encounter.getPatient().getGivenName());
 			row.put("Age", DateUtil.getTimespan(new Date(), encounter.getPatient().getBirthdate()));
 			row.put("Gender", encounter.getPatient().getGender());			
 			row.put("Group", treatmentGroups.get(encounter.getPatientId()));
 			row.put("Location", encounter.getLocation().getName());				
 			row.put("Result Date", Context.getDateFormat().format(encounter.getEncounterDatetime()));
 
 			/* 
 			 * FIXME Quick hack to get a desired observation by concept. This currently 
 			 * returns the first observation found.  I'm assuming that the Dao orders by 
 			 * date, but need to test this out. Need to implement a more elegant solution 
 			 * for returning the most recent observation
 			 */
 
 			for (ConceptColumn conceptColumn : getConceptColumns()) { 
 				Obs obs = null;
 				for (Obs currentObs : encounter.getObs()) { 
 					// FIXME This only works when comparing conceptId, not concepts
 					if (currentObs.getConcept().getConceptId().equals(conceptColumn.getConcept().getConceptId())) { 	
 						// (1) if obs is null then we use currentObs because it's the first in the list
 						// (2) otherwise we use which observation happened first based on obs datetime 
 						if (obs == null || obs.getObsDatetime().compareTo(currentObs.getObsDatetime()) < 0) { 
 							obs = currentObs;
 						}
 					}
 				}
 				
 				String value = "";
 				
 				// TODO Prevents null pointer exception 
 				obs = (obs != null ? obs : new Obs());
 				
 				// Add obs value to column in row
 				// FIXME Hack to get non-precise (should be done in the Concept.getValueAsString() method).
 				if (conceptColumn.isNumeric() && !conceptColumn.isPrecise()) { 
 					if (obs.getValueNumeric()!=null) { 
 						NumberFormat nf = NumberFormat.getIntegerInstance();
 						nf.setGroupingUsed(false);
 						value = nf.format(obs.getValueNumeric());
 					}
 				} 
 				else { 
 					value = obs.getValueAsString(Context.getLocale());
 				}
 				row.put(conceptColumn.getDisplayName(), value);
 			}			
 			// Add row to dataset
 			dataset.add(row);
 		}
 		return dataset;
 	}
 
 	
 	/**
 	 * Get all lab related encounters during a period defined by the given start date and end date.
 	 * The location parameter can be chosen to refine the results or ignored.
 	 * 
 	 * @param	location	the location of the lab encounter
 	 * @param	startDate	the start date of period to be searched
 	 * @param	endDate		the end date of period to be searched
      */
 	public List<Encounter> getLabOrderEncounters(Location location, Date startDate, Date endDate) { 	
 		Map<Date, Encounter> encountersMap = new TreeMap<Date, Encounter>();
 						
 		for (Order order : getLabOrdersBetweenDates(location, startDate, endDate)) {
 			Encounter encounter = order.getEncounter();
 			Date encounterDate = order.getEncounter().getEncounterDatetime();
 
 			// If location does not match
 			if (location != null && !location.equals(encounter.getLocation())) {
 				continue; 
 			}
 			// If encounter date is before the given start date
 			if (startDate == null || encounterDate.before(startDate)) { 				
 				continue;
 			}
 			// If encounter date is after the given end date
 			if (endDate == null ||  encounterDate.after(endDate)) { 
 				continue;
 			}			
 			// Should filter encounters that do not have any observations
 			if (encounter.getObs().isEmpty()) { 
 				continue;
 			}			
 			encountersMap.put(encounter.getDateCreated(), encounter);
 		}
 		
 		
 		// Create a new ordered list
 		List<Encounter> encounterList = new ArrayList<Encounter>();
 		encounterList.addAll(encountersMap.values());		
 		
 		return encounterList;
 	}
 
 	
 	
 	/** 
 	 * Gets all non-voided lab orders in the system.
 	 * 
 	 * @return
 	 */
 	public List<Order> getAllLabOrders() { 
 		
 		// Only show lab orders
 		List<OrderType> orderTypes = new ArrayList<OrderType>();
 		orderTypes.add(SimpleLabEntryUtil.getLabOrderType());
 				
 		return Context.getOrderService().getOrders(
 					Order.class, 
 					null, 
 					null,
 					ORDER_STATUS.NOTVOIDED, 
 					null, 
 					null, 
 					orderTypes);
 	}
 	
 	
 	
 	/**
 	 * Gets all lab orders that were completed with the given period at the given location.
 	 * 
 	 * @param	location	the location of the lab encounter
 	 * @param	startDate	the start date of period to be searched
 	 * @param	endDate		the end date of period to be searched
 	 */
 	public List<Order> getLabOrdersBetweenDates(Location location, Date startDate, Date endDate) { 
 		
 		// TODO need to implement DAO layer method to get orders by the given parameters
 		List<Order> orders = new ArrayList<Order>();
 
 		// FIXME This should be done in the service OR dao layer
 		for (Order order : getAllLabOrders()) {
 			Encounter encounter = order.getEncounter();
 			Date encounterDate = order.getEncounter().getEncounterDatetime();
 
 			// If location does not match
 			if (location != null && !location.equals(encounter.getLocation())) {
 				continue; 
 			}
 			// If encounter date is before the given start date
 			if (startDate == null || encounterDate.before(startDate)) { 				
 				continue;
 			}
 			// If encounter date is after the given end date
 			if (endDate == null ||  encounterDate.after(endDate)) { 
 				continue;
 			}			
 			// Should filter encounters that do not have any observations
 			if (encounter.getObs().isEmpty()) { 
 				continue;
 			}			
 			orders.add(order);
 		}
 		
 		return orders;
 		
 	}
 	
 	
 
 	
 	
 }
