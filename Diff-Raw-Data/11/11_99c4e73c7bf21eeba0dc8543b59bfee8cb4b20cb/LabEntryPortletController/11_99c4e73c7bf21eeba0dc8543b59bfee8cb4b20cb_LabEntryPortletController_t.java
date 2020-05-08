 package org.openmrs.module.simplelabentry.web.controller;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Date;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Concept;
 import org.openmrs.Location;
 import org.openmrs.Order;
 import org.openmrs.Patient;
 import org.openmrs.api.OrderService.ORDER_STATUS;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.simplelabentry.SimpleLabEntryService;
 import org.openmrs.web.controller.PortletController;
 import org.springframework.util.StringUtils;
 
 public class LabEntryPortletController extends PortletController {
 	
 	protected final Log log = LogFactory.getLog(getClass());
 
 	@SuppressWarnings("unchecked")
 	protected void populateModel(HttpServletRequest request, Map model) {
     	
 		SimpleLabEntryService ls = (SimpleLabEntryService) Context.getService(SimpleLabEntryService.class);
 		
 		// Supported LabTest Sets
     	model.put("labTestConcepts", ls.getLabTestConcepts());
 		
 		// Retrieve Orders that Match Input Parameters
 		Object patientId = model.get("patientId");
 		String orderLocationId = (String)model.get("orderLocation");
 		String orderSetConceptId = (String)model.get("orderConcept");
 		String orderDateStr = (String)model.get("orderDate");
 		
     	String currentGroupVal = request.getParameter("groupKey");
     	if (currentGroupVal != null) {
 	    	String[] split = currentGroupVal.split("\\.");
 	    	orderLocationId = split.length >= 1 ? split[0] : "";
 	    	orderDateStr = split.length >= 2 ? split[1] : "";
 	    	orderSetConceptId = split.length >= 3 ? split[2] : "";
     	}
 		
 		String limit = (String)model.get("limit");
 		
 		// Retrieve global properties
 		String orderTypeId = Context.getAdministrationService().getGlobalProperty("simplelabentry.labOrderType");
 		model.put("orderTypeId", orderTypeId);
 		
 		log.debug("Retrieving orders for: location="+orderLocationId+",concept="+orderSetConceptId+"," +"date="+orderDateStr+",type="+orderTypeId+",limit="+limit);
 
 		List<Order> labOrderList = new ArrayList<Order>();
 		try {
 			Concept concept = StringUtils.hasText(orderSetConceptId) ? Context.getConceptService().getConcept(Integer.parseInt(orderSetConceptId)) : null;
 			Location location = StringUtils.hasText(orderLocationId) ? Context.getLocationService().getLocation(Integer.parseInt(orderLocationId)) : null;
 			Date orderDate = StringUtils.hasText(orderDateStr) ? Context.getDateFormat().parse(orderDateStr) : null;
 			ORDER_STATUS status = "open".equals(limit) ? ORDER_STATUS.CURRENT : "closed".equals(limit) ? ORDER_STATUS.COMPLETE : ORDER_STATUS.NOTVOIDED;
 			Patient patient = null;
 			boolean check = true;
 			
 			if (patientId != null && !"".equals(patientId)) {
 				patient = Context.getPatientService().getPatient(Integer.valueOf(patientId.toString()));
 				if (patient == null) {
 					check = false;
 				}
				log.debug("Found: " + patient + " for patientId=" + patientId);
 			}
			if (check) {
				// Retrieve matching orders
				if (patient != null || concept != null || location != null || orderDate != null) {
					labOrderList = ls.getLabOrders(concept, location, orderDate, status, patient);
					log.debug("Found: " + labOrderList.size() + " LabOrders");
				}
 			}
 		}
 		catch (Exception e) {
 			throw new RuntimeException("Server Error: Unable to load order list.", e);
 		}
 		model.put("labOrders", labOrderList);
 	}
 }
