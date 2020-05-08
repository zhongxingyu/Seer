 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *    
  *******************************************************************************/
 package org.ebayopensource.turmeric.rateLimiterproviderImpl;
 
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.ebayopensource.turmeric.common.v1.types.AckValue;
 import org.ebayopensource.turmeric.rateLimiterproviderImpl.Policy.BlackListPolicy;
 import org.ebayopensource.turmeric.rateLimiterproviderImpl.Policy.RateLimiterPolicy;
 import org.ebayopensource.turmeric.rateLimiterproviderImpl.Policy.WhiteListPolicy;
 import org.ebayopensource.turmeric.ratelimiter.provider.RateLimiterProvider;
 import org.ebayopensource.turmeric.security.v1.services.IsRateLimitedRequest;
 import org.ebayopensource.turmeric.security.v1.services.IsRateLimitedResponse;
 import org.ebayopensource.turmeric.security.v1.services.RateLimiterStatus;
 import org.ebayopensource.turmeric.services.policyservice.intf.gen.BasePolicyServiceConsumer;
 import org.ebayopensource.turmeric.services.ratelimiterservice.impl.RateLimiterException;
 
 /**
  * This class is an example of a Rate Limiter Provider implementation. It
  * provides a basic rate limiter that checks the counts, whitelist, and
  * blacklist policies. It will check for inclusion and exclusions as well.
  * 
  * It keeps track of counts of a service through the use of a ConcurrentHashMap,
  * and does not cache any results.
  * 
  * Adopters may use this as a basis for their own implementations, but are
  * encourage to create their own Rate Limiters to suite their particular needs.
  * 
  */
 
 public class RateLimiterProviderImpl implements RateLimiterProvider {
 	// used in mock
 	private BasePolicyServiceConsumer consumer;
 
 	// used in mock
 	/**
 	 * Sets the consumer.
 	 *
 	 * @param consumer the new consumer
 	 */
 	public void setConsumer(BasePolicyServiceConsumer consumer) {
 		this.consumer = consumer;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public IsRateLimitedResponse isRateLimited(
 			IsRateLimitedRequest isRateLimitedRequest) {
 		IsRateLimitedResponse response = new IsRateLimitedResponse();
 		response.setStatus(RateLimiterStatus.SERVE_OK);
 		if (!"SERVICE".equalsIgnoreCase(isRateLimitedRequest.getResourceType())) {
 			response.setStatus(RateLimiterStatus.UNSUPPORTED);
 		}
 		
 		if (isRateLimitedRequest.getResourceName() == null
 				|| isRateLimitedRequest.getResourceType() == null)
 			response.setStatus(RateLimiterStatus.BLOCK);
 		checkPolices(response, isRateLimitedRequest);
 		setTimeStamp(response);
 		response.setAck(AckValue.SUCCESS);
 		return response;
 	}
 
 	private void setTimeStamp(IsRateLimitedResponse response) {
 		GregorianCalendar calendar = new GregorianCalendar();
 		calendar.setTime(new Date());
 		try {
 			XMLGregorianCalendar date2 = DatatypeFactory.newInstance()
 					.newXMLGregorianCalendar(calendar);
 			response.setTimestamp(date2);
 		} catch (DatatypeConfigurationException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void checkPolices(IsRateLimitedResponse response,
 			IsRateLimitedRequest request) {
		if (RateLimiterStatus.SERVE_OK.equals(response.getStatus())
				|| RateLimiterStatus.SERVE_GIF.equals(response.getStatus())) {
 
 			RateLimiterPolicy rlPolicy = new RateLimiterPolicy(request);
 			// use for mock the consumer
 			rlPolicy.setConsumer(consumer);
 			BlackListPolicy blPolicy = new BlackListPolicy(request);
 			WhiteListPolicy wlPolicy = new WhiteListPolicy(request);
 
			response = response == null ? new IsRateLimitedResponse()
					: response;
 			try {
				blPolicy.evaluate(response, request);
				wlPolicy.evaluate(response, request);
				rlPolicy.evaluate(response, request);
 			} catch (RateLimiterException e) {
 				// response.setAck(AckValue.FAILURE);
 				// CommonErrorData data = new CommonErrorData();
 				// data.setCause(e.getCause() + "");
 				// data.setMessage(e.getMessage());
 				// if (response.getErrorMessage() == null) {
 				// response.setErrorMessage(new ErrorMessage());
 				// }
 				//
 				// response.getErrorMessage().getError().add(data);
 				// response.setStatus(RateLimiterStatus.INVALID);
 				// e.printStackTrace();
 
 			}
 
 		}
 
 	}
 
 }
