 package org.openmrs.module.simplelabentry.impl;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Concept;
 import org.openmrs.Encounter;
 import org.openmrs.Location;
 import org.openmrs.Order;
 import org.openmrs.OrderType;
 import org.openmrs.Patient;
 import org.openmrs.api.OrderService.ORDER_STATUS;
 import org.openmrs.api.context.Context;
 import org.openmrs.api.impl.BaseOpenmrsService;
 import org.openmrs.module.simplelabentry.SimpleLabEntryService;
 
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
 	
 	protected final Log log = LogFactory.getLog(getClass());
 	
 	/**
      * @see org.openmrs.module.simplelabentry.SimpleLabEntryService#getOrders(OrderType, Concept, Location, Date, ORDER_STATUS, List<Patient>)
      */
 	public List<Order> getLabOrders(Concept concept, Location location, Date orderDate, ORDER_STATUS status, List<Patient> patients) {
 		
 		List<Order> orderList = new ArrayList<Order>();
 		
 		// Retrieve proper OrderType for Lab Orders
 		OrderType orderType = null;
 		String orderTypeId = Context.getAdministrationService().getGlobalProperty("simplelabentry.labOrderType");
 		if (orderTypeId != null) {
 			try {
 				orderType = Context.getOrderService().getOrderType(Integer.valueOf(orderTypeId));
 			}
 			catch (Exception e) {}
 		}
 		if (orderType == null) {
 			throw new RuntimeException("Unable to retrieve LabOrders since the OrderType of <" + orderTypeId + "> is invalid.");
 		}
 		
 		log.debug("Retrieving lab orders of type "+orderType+" for: location="+location+",concept="+concept+",date="+orderDate+",status="+status+",patients="+patients);
 		
		List<Concept> conceptList = null;
		if (concept == null) {
			conceptList = getLabTestConcepts();
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
 	public List<Concept> getLabTestConcepts() {
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
 }
