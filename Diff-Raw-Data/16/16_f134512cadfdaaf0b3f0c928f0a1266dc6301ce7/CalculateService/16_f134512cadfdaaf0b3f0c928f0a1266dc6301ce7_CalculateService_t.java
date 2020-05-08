 package com.genability.client.api.service;
 
 import org.codehaus.jackson.type.TypeReference;
 
 import com.genability.client.api.request.GetCalculatedCostRequest;
 import com.genability.client.api.request.GetCalculationInputsRequest;
 import com.genability.client.types.Response;
 import com.genability.client.types.CalculatedCost;
 import com.genability.client.types.PropertyData;
 
 public class CalculateService extends BaseService {
 	
 	
 	/**
 	 * Calls the REST service to run a calculation
 	 * 
 	 * @return CalculatedCost
 	 */
 	public Response<CalculatedCost> getCalculatedCost(GetCalculatedCostRequest request) {
 		
 		if(log.isDebugEnabled()) log.debug("getCalculatedCost called");
 		
 		String uri = "public/calculate";
 		if (request.getAccountId() !=null) {
 			uri += "/account/" + request.getAccountId();
			request.setAccountId(null);
 		} else if (request.getMasterTariffId() != null) {
 			uri += "/" + request.getMasterTariffId();
 			request.setMasterTariffId(null);
 		} else {
 			//This will use only query string parameters to run the calc.
 			//Not currently doing this anywhere in the test suite.
 		}
 
 		
 		@SuppressWarnings("unchecked")
 		Response<CalculatedCost> response = (Response<CalculatedCost>) this.callPost(
 				uri,
 				request,
 				new TypeReference<Response<CalculatedCost>>() { });
 		
 		if(log.isDebugEnabled()) log.debug("getCalculatedCost completed");
 		
 		return response;
 		
 	}
 	
 	/**
 	 * Calls the REST service to get the required inputs to run a Calculation
 	 * for a given tariffId.  The tariffId can be explicitly stated within the
 	 * GetCalculationInputsRequest object, or if it is null, the service will 
 	 * expect it to be in the Account that is passed in. 
 	 */
 	 public Response<PropertyData> getCalculationInputs(GetCalculationInputsRequest request) {
 	
 		 if(log.isDebugEnabled()) log.debug("getCalculationInputs called");
 			
 			String uri = "public/calculate";
 			if (request.getMasterTariffId() != null) {
 				uri += "/" + request.getMasterTariffId();
 			} else {
 				//This means we will be getting the tariffId from within the Account.
 				//Do nothing.
 			}
 			
 			@SuppressWarnings("unchecked")
 			Response<PropertyData> response = (Response<PropertyData>) this.callGet(
 					uri,
 					request.getQueryParams(),
 					new TypeReference<Response<PropertyData>>() { });
 			
 			if(log.isDebugEnabled()) log.debug("getCalculationInputs completed");
 			
 			return response;
 	 }
 }
