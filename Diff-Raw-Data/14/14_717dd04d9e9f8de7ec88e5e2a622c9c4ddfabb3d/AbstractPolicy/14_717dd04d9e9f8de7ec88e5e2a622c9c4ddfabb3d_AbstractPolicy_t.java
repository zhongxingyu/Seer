 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *    
  *******************************************************************************/
 package org.ebayopensource.turmeric.rateLimiterproviderImpl.Policy;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.ebayopensource.turmeric.common.v1.types.AckValue;
 import org.ebayopensource.turmeric.errorlibrary.turmericratelimiter.ErrorConstants;
 import org.ebayopensource.turmeric.rateLimiterproviderImpl.Policy.model.RateLimiterPolicyModel;
 import org.ebayopensource.turmeric.security.v1.services.FindPoliciesRequest;
 import org.ebayopensource.turmeric.security.v1.services.FindPoliciesResponse;
 import org.ebayopensource.turmeric.security.v1.services.IsRateLimitedRequest;
 import org.ebayopensource.turmeric.security.v1.services.IsRateLimitedResponse;
 import org.ebayopensource.turmeric.security.v1.services.Policy;
 import org.ebayopensource.turmeric.security.v1.services.PolicyKey;
 import org.ebayopensource.turmeric.security.v1.services.Subject;
 import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
 import org.ebayopensource.turmeric.security.v1.services.SubjectGroupType;
 import org.ebayopensource.turmeric.security.v1.services.SubjectType;
 import org.ebayopensource.turmeric.security.v1.services.Target;
 import org.ebayopensource.turmeric.services.policyservice.intf.gen.BasePolicyServiceConsumer;
 import org.ebayopensource.turmeric.services.ratelimiterservice.impl.RateLimiterException;
 import org.ebayopensource.turmeric.services.ratelimiterservice.impl.util.EncodingUtils;
 
 /**
  * The Class AbstractPolicy.
  */
 public abstract class AbstractPolicy extends CounterAbstractPolicy {
 
 	/** The rl request. */
 	protected IsRateLimitedRequest rlRequest = null;
 	
 	/** The request subjects. */
 	protected List<String> requestSubjects = new LinkedList<String>();
 	
 	/** The request subject groups. */
 	protected List<String> requestSubjectGroups = new LinkedList<String>();
 	private static BasePolicyServiceConsumer consumer;
 
 	// used in mock
 	/**
 	 * Sets the consumer.
 	 *
 	 * @param consumer1 the new consumer
 	 */
 	@edu.umd.cs.findbugs.annotations.SuppressWarnings(
 		    value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", 
 		    justification="If you change this it breaks the unit tests.")
 	public void setConsumer(BasePolicyServiceConsumer consumer1) {
 		consumer = consumer1;
 	}
 	
 	/**
 	 * Gets the consumer.
 	 *
 	 * @return the consumer
 	 */
 	public BasePolicyServiceConsumer getConsumer() {
 		return consumer == null ? new BasePolicyServiceConsumer() : consumer;
 	}
 
 	/**
 	 * Instantiates a new abstract policy.
 	 *
 	 * @param rlRequest the rl request
 	 */
 	public AbstractPolicy(IsRateLimitedRequest rlRequest) {
 		this.rlRequest = rlRequest;
 		populate();
 	}
 
 	private void populate() {
 		getActiveRL();
 
 		// retrive all subjects from request
 		List<SubjectType> subjectTypes = rlRequest.getSubject();
 		for (SubjectType st : subjectTypes) {
 			if (isSubjectTypeValid(st)) {
 				// add to subject list
 				if (!requestSubjects.contains(st.getValue().trim()))
 					requestSubjects.add(st.getValue().trim());
 				// add to group list
 				processIncrementSubjectOrGroup(st.getValue().trim());
 			}
 
 		}
 		// retrieve all subject group from request
 		List<SubjectGroupType> subjectGroupTypes = rlRequest
 				.getResolvedSubjectGroup();
 		for (SubjectGroupType sgt : subjectGroupTypes) {
 			if (isSubjectGroupTypeValid(sgt)) {
 				// add to subject list
 				if (!requestSubjectGroups.contains(sgt.getName().trim()))
 					requestSubjectGroups.add(sgt.getName().trim());
 				processIncrementSubjectOrGroup(sgt.getName().trim());
 			}
 
 		}
 
 	}
 
 	/**
 	 * Is the resource excluded from the Policy and thus rate limiting does not
 	 * apply.
 	 * 
 	 * @return true or false
 	 */
 	public abstract boolean isExcluded();
 
 	/**
 	 * Is the resource included in the Policy and thus rate limiting applies.
 	 *
 	 * @return true, if is included
 	 */
 	public abstract boolean isIncluded();
 
 	/**
 	 * There is no Policy defined of this type.
 	 *
 	 * @return true, if is empty
 	 */
 	public abstract boolean isEmpty();
 
 	/**
 	 * Evaluate.
 	 *
 	 * @param response the response
 	 * @param rlRequest the rl request
 	 * @return the checks if is rate limited response
 	 * @throws RateLimiterException the rate limiter exception
 	 */
 	public abstract IsRateLimitedResponse evaluate(
 			IsRateLimitedResponse response, IsRateLimitedRequest rlRequest)
 			throws RateLimiterException;
 
 	/**
 	 * Gets the policy type.
 	 *
 	 * @return the policy type
 	 */
 	public abstract String getPolicyType();
 
 	private FindPoliciesResponse getPoliciesResponse()
 			throws RateLimiterException {
 		PolicyKey policyKey = new PolicyKey();
 		policyKey.setPolicyType(getPolicyType());
 		FindPoliciesRequest policyRequest = new FindPoliciesRequest();
 		policyRequest.getPolicyKey().add(policyKey);
 //		policyRequest.setOutputSelector(PolicyOutputSelector.ALL);
 						
 		FindPoliciesResponse policyResponse = getConsumer().findPolicies(
 				policyRequest);
 		if (!AckValue.SUCCESS.equals(policyResponse.getAck())) {
 			throw new RateLimiterException(
 					ErrorConstants.SVC_RATELIMITER_SYSTEM_ERROR, EncodingUtils
 							.encodeErrorMessage(policyResponse.getAck(),
 									policyResponse.getErrorMessage()));
 		}
 		return policyResponse;
 	}
 
 	/**
 	 * Gets the policies.
 	 *
 	 * @return the policies
 	 * @throws RateLimiterException the rate limiter exception
 	 */
 	protected List<Policy> getPolicies() throws RateLimiterException {
 		FindPoliciesResponse policyResponse = getPoliciesResponse();
 		
 		List<Policy> policies = new ArrayList<Policy>();
 		if (policyResponse != null && !policyResponse.getPolicySet().getPolicy().isEmpty()) {
 			for (Policy policy : policyResponse.getPolicySet().getPolicy()) {
 				Target target = policy.getTarget();
 				org.ebayopensource.turmeric.security.v1.services.Resources resources = target.getResources();
 				if ( resources != null && !resources.getResource().isEmpty()) {
 					String rlResourceName = rlRequest.getResourceName();
 					for (org.ebayopensource.turmeric.security.v1.services.Resource resource : resources.getResource()) {
 						if (resource.getResourceName().equals(rlResourceName)) {
 							policies.add(policy);
 						}
 					}
 				}
 				
 			}
 		}
 		return policies;
 	}
 
 	/**
 	 * Checks if is subject type valid.
 	 *
 	 * @param st the st
 	 * @return true, if is subject type valid
 	 */
 	protected boolean isSubjectTypeValid(SubjectType st) {
 		if (st != null && st.getValue() != null && st.getDomain() != null
 				&& st.getValue().trim().length() != 0
 				&& st.getDomain().trim().length() != 0) {
 			return true;
 		}
 		return false;
 	}
 
 	private boolean isSubjectGroupTypeValid(SubjectGroupType sgt) {
 
 		return (sgt != null && sgt.getName() != null && sgt.getName().trim()
 				.length() != 0);
 	}
 
 	/**
 	 * Checks if is policy subject group valid.
 	 *
 	 * @param st the st
 	 * @return true, if is policy subject group valid
 	 */
 	protected boolean isPolicySubjectGroupValid(Policy st) {
 		if (st != null && st.getTarget() != null
 				&& st.getTarget().getSubjects() != null
 				&& st.getTarget().getSubjects().getSubjectGroup() != null) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Checks if is found in request.
 	 *
 	 * @param request the request
 	 * @param reqSubjects the req subjects
 	 * @param reqSubjectGroups the req subject groups
 	 * @param checkPolicies the check policies
 	 * @return true, if is found in request
 	 */
 	protected boolean isFoundInRequest(IsRateLimitedRequest request,
 			List<String> reqSubjects, List<String> reqSubjectGroups,
 			List<Policy> checkPolicies) {
 		for (Policy p : checkPolicies) {
 			if (isPolicySubjectGroupValid(p)) {
 
 				// check subjectgroup
 				for (SubjectGroup subjectGroup : p.getTarget().getSubjects()
 						.getSubjectGroup()) {
 					if (!reqSubjectGroups.contains(subjectGroup
 							.getSubjectGroupName().trim())) {
 						// response.setStatus(status);
 						// checkRateLimiter(response, request, subjects,
 						// domain);
 						return true;
 					}
 					// check subject
 					for (Subject wl : subjectGroup.getSubject()) {
 						if (!reqSubjects.contains(wl.getSubjectName().trim())) {
 							// response.setStatus(status);
 							// checkRateLimiter(response, request, subjects,
 							// domain);
 							return true;
 						}
 					}
 				}
 			}
 		}
 		return false;
 
 	}
 
 	// add/increment the counter of subjects or group
 	private void processIncrementSubjectOrGroup(String ipOrSubjectGroup) {
 		// since BLACKLIST is the 1st policy to execute the request
 		if ("BLACKLIST".equalsIgnoreCase(getPolicyType().trim())) {
 			// counter per user
 			incrementCounter(ipOrSubjectGroup, new RateLimiterPolicyModel());
 			// to all
 			incrementCounter(HITS, new RateLimiterPolicyModel());
 		}
 	}
 }
