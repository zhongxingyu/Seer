 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.uhsarp.billrive.webservices.rest;
 
 import com.uhsarp.billrive.domain.Bill;
 import com.uhsarp.billrive.services.BillService;
 import java.util.ArrayList;
 import java.util.List;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.context.request.WebRequest;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.View;
 
 /**
  *
  * @author uhsarp
  */
 @Controller
 public class BillController extends GenericController{
     
     	@Autowired
 	private BillService billService;
 
   
 	@Autowired
 	private View jsonView_i;
         
         
         private static final String DATA_FIELD = "data";
 	private static final String ERROR_FIELD = "error";
         private static final Logger logger_c = Logger.getLogger(BillController.class);
         
         
     	@RequestMapping(value = "/rest/{userId}/bills", method = RequestMethod.GET)
 	public ModelAndView getBills(@PathVariable("userId") int userId) {
 		List<Bill> bills = new ArrayList<Bill>();
                 logger_c.info("Value of userId is  "+userId);
 		
                 //int userId=5;
 		try {
			bills = null;//billService.getBills(userId);
 //                        logger_c.info("Value of Bills ArrayList after service call is  "+bills.getFirst().toString());
 		} catch (Exception e) {
 			String sMessage = "Error getting all bills. [%1$s]";
 			return createErrorResponse(String.format(sMessage, e.toString()));
 		}
               
 //        Bill bill = new Bill((long)1,"Walmart", new DateTime(2013,2,3,1,1), userId, "Sample Notes", null, userId);
 //        Bill bill1 = new Bill((long)2,"Costco", new DateTime(2014,2,3,1,1), userId, "Second sample Notes", null, userId);
 //        logger_c.info("Value of bill is  "+bill.getTitle());
 //        bills.add(bill);
 //        bills.add(bill1);
         
         
                 logger_c.info("Value of Bills ArrayList is  ");
 
 		logger_c.debug("Returing Bills: " + bills.toString());
 		return new ModelAndView(jsonView_i, DATA_FIELD, bills);
 	}
         
         
         	@RequestMapping(value = "/rest/{userId}/bills/{billId}", method = RequestMethod.GET)
 	public ModelAndView getBill(@PathVariable("billId") String billId_p,@PathVariable("userId") int userId) {
 		Bill bill = null;
 
 		
 		if (isEmpty(billId_p) || billId_p.length() < 5) {
 			String sMessage = "Error invoking getBill - Invalid bill Id parameter";
 			return createErrorResponse(sMessage);
 		}
 
 		try {
			bill = null;//billService.getBillById(billId_p);
 		} catch (Exception e) {
 			String sMessage = "Error invoking getBill. [%1$s]";
 			return createErrorResponse(String.format(sMessage, e.toString()));
 		}
 
 		logger_c.debug("Returing Bill: " + bill.toString());
 		return new ModelAndView(jsonView_i, DATA_FIELD, bill);
 	}
                 
                 
         @RequestMapping(value = { "/rest/{userId}/bills/" }, method = { RequestMethod.POST })
 	public ModelAndView createBill(@RequestBody Bill bill_p,@PathVariable("userId") int userId,
 			HttpServletResponse httpResponse_p, WebRequest request_p) {
 
 		Bill createdBill;
 		logger_c.debug("Creating Bill: " + bill_p.toString());
 
 		try {
 			createdBill = billService.createBill(bill_p);
 		} catch (Exception e) {
 			String sMessage = "Error creating new bill. [%1$s]";
 			return createErrorResponse(String.format(sMessage, e.toString()));
 		}
 
 		/* set HTTP response code */
 		httpResponse_p.setStatus(HttpStatus.CREATED.value());
 
 		/* set location of created resource */
 		httpResponse_p.setHeader("Location", request_p.getContextPath() + "/rest/{userId}/bills/" + bill_p.getId());
 
 		/**
 		 * Return the view
 		 */
 		return new ModelAndView(jsonView_i, DATA_FIELD, createdBill);
 	}
 
 	/**
 	 * Updates bill with given bill id.
 	 *
 	 * @param bill_p
 	 *            the bill_p
 	 * @return the model and view
 	 */
 	@RequestMapping(value = { "/rest/{userId}/bill/{billId}" }, method = { RequestMethod.PUT })
 	public ModelAndView updateBill(@RequestBody Bill bill_p, @PathVariable("billId") String billId_p,@PathVariable("userId") int userId,
 								   HttpServletResponse httpResponse_p) {
 
 		logger_c.debug("Updating Bill: " + bill_p.toString());
 
 		/* validate bill Id parameter */
 		if (isEmpty(billId_p) || billId_p.length() < 5) {
 			String sMessage = "Error updating bill - Invalid bill Id parameter";
 			return createErrorResponse(sMessage);
 		}
 
 		Bill bill = null;
 
 		try {
 			bill = billService.updateBill(bill_p);
 		} catch (Exception e) {
 			String sMessage = "Error updating bill. [%1$s]";
 			return createErrorResponse(String.format(sMessage, e.toString()));
 		}
 
 		httpResponse_p.setStatus(HttpStatus.OK.value());
 		return new ModelAndView(jsonView_i, DATA_FIELD, bill);
 	}
 
 	/**
 	 * Deletes the bill with the given bill id.
 	 *
 	 * @param billId_p
 	 *            the bill id_p
 	 * @return the model and view
 	 */
 	@RequestMapping(value = "/rest/{userId}/bills/{billId}", method = RequestMethod.DELETE)
 	public ModelAndView removeBill(@PathVariable("billId") String billId_p,@PathVariable("userId") int userId,
 								   HttpServletResponse httpResponse_p) {
 
 		logger_c.debug("Deleting Bill Id: " + billId_p.toString());
 
 		/* validate bill Id parameter */
 		if (isEmpty(billId_p) || billId_p.length() < 5) {
 			String sMessage = "Error deleting bill - Invalid bill Id parameter";
 			return createErrorResponse(sMessage);
 		}
 
 		try {
 			billService.deleteBill(billId_p);
 		} catch (Exception e) {
 			String sMessage = "Error invoking getBills. [%1$s]";
 			return createErrorResponse(String.format(sMessage, e.toString()));
 		}
 
 		httpResponse_p.setStatus(HttpStatus.OK.value());
 		return new ModelAndView(jsonView_i, DATA_FIELD, null);
 	}
         
         public static boolean isEmpty(String s_p) {
 		return (null == s_p) || s_p.trim().length() == 0;
 	}
         
         	private ModelAndView createErrorResponse(String sMessage) {
 		return new ModelAndView(jsonView_i, ERROR_FIELD, sMessage);
 	}
                 
                   public BillService getBillService() {
         return billService;
     }
 
     public void setBillService(BillService billService) {
         this.billService = billService;
     }
 
 }
