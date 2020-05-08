 package org.openmrs.module.simplelabentry.web.controller;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Concept;
 import org.openmrs.Location;
 import org.openmrs.Order;
 import org.openmrs.OrderType;
 import org.openmrs.Patient;
 import org.openmrs.PatientIdentifierType;
 import org.openmrs.api.OrderService.ORDER_STATUS;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.simplelabentry.SimpleLabEntryService;
 import org.openmrs.module.simplelabentry.util.SimpleLabEntryUtil;
 import org.openmrs.web.controller.PortletController;
 import org.springframework.util.StringUtils;
 
 public class LabEntryPortletController extends PortletController {
 	
 	protected final Log log = LogFactory.getLog(getClass());
 
 	@SuppressWarnings("unchecked")
 	protected void populateModel(HttpServletRequest request, Map model) {
     	
 		SimpleLabEntryService ls = (SimpleLabEntryService) Context.getService(SimpleLabEntryService.class);
 		
 		// Supported LabTest Sets
     	model.put("labTestConcepts", ls.getSupportedLabSets());
 		
 		// Retrieve Orders that Match Input Parameters
 		String identifier = (String)model.get("identifier");
 		String orderLocationId = (String)model.get("orderLocation");
 		String orderSetConceptId = (String)model.get("orderConcept");
 		String orderDateStr = (String)model.get("orderDate");
 		
     	String currentGroupVal = request.getParameter("groupKey");
     	if (currentGroupVal != null) {
 	    	String[] split = currentGroupVal.split("\\.");
 	    	orderLocationId = split.length >= 1 ? split[0] : "";
 	    	orderDateStr = split.length >= 2 ? split[1] : "";
 	    	orderSetConceptId = split.length >= 3 ? split[2] : "";
 	    	
 	    	// Make sure the concept ID is set in the model.  Used by orderEntry.jsp to set the right checkbox
 	    	model.put("groupConceptId", orderSetConceptId);	    	
     	}
 		//This is a TODO
     	//model.put("failureConceptId", SimpleLabEntryUtil.getTestFailureConcept((String) model.get("groupConceptId")));
 		String limit = (String)model.get("limit");
 		
 		// Retrieve global properties
 		OrderType ot = (OrderType) SimpleLabEntryUtil.getLabOrderType();
 		if (ot == null)
 			throw new RuntimeException("Please set the global property simplelabentry.labOrderType correctly.");
 		String orderTypeId = ot.getOrderTypeId().toString();
 		model.put("orderTypeId", orderTypeId);
 		
 		log.debug("Retrieving orders for: location="+orderLocationId+",concept="+orderSetConceptId+"," +"date="+orderDateStr+",type="+orderTypeId+",limit="+limit);
 		
 		PatientIdentifierType pit = (PatientIdentifierType) SimpleLabEntryUtil.getPatientIdentifierType();
 		if (pit == null)
 			throw new RuntimeException("Please set the global property simplelabentry.patientIdentifierType correctly.");
         model.put("patientIdentifierType", pit);
         
         
 		List<Order> labOrderList = new ArrayList<Order>();
 		try {
 			Concept concept = StringUtils.hasText(orderSetConceptId) ? Context.getConceptService().getConcept(Integer.parseInt(orderSetConceptId)) : null;
 			Location location = StringUtils.hasText(orderLocationId) ? Context.getLocationService().getLocation(Integer.parseInt(orderLocationId)) : null;
 			Date orderDate = StringUtils.hasText(orderDateStr) ? Context.getDateFormat().parse(orderDateStr) : null;
 			ORDER_STATUS status = "open".equals(limit) ? ORDER_STATUS.CURRENT : "closed".equals(limit) ? ORDER_STATUS.COMPLETE : ORDER_STATUS.NOTVOIDED;
 			List<Patient> patients = null;
 			boolean check = true;
 			
 			if (StringUtils.hasText(identifier)) {
 				patients = Context.getPatientService().getPatients(null, identifier, null, true);
 				try {
 					Patient p = Context.getPatientService().getPatient(Integer.parseInt(identifier));
 					if (p != null && !patients.contains(p)) {
 						patients.add(p);
 					}
 				}
 				catch (Exception e) {}
 				if (patients.isEmpty()) {
 					check = false;
 				}
 				log.debug("Found: " + patients + " for identifier=" + identifier);
 			}
 			if (check) {
 				// Retrieve matching orders
 				if (patients != null || concept != null || location != null || orderDate != null) {
 					labOrderList = ls.getLabOrders(concept, location, orderDate, status, patients);
 					log.debug("Found: " + labOrderList.size() + " LabOrders");
 				}
 			}
 		}
 		catch (Exception e) {
 			throw new RuntimeException("Server Error: Unable to load order list.", e);
 		}
 		model.put("labOrders", labOrderList);
 		model.put("notTests", SimpleLabEntryUtil.getConceptIdsInLabSetsThatAreNotTests());
 		model.put("patientIdentifierType", SimpleLabEntryUtil.getPatientIdentifierType());
		model.put("programToDisplay", SimpleLabEntryUtil.getProgram().getName());
 		model.put("workflowToDisplay", SimpleLabEntryUtil.getWorkflow().getName());
 	}
 }
