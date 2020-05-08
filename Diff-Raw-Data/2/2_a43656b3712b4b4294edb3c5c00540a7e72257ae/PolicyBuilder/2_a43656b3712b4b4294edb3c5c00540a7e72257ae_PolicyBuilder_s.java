 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *    
  *******************************************************************************/
 package org.ebayopensource.turmeric.services.policyservice.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import org.ebayopensource.turmeric.policyservice.exceptions.PolicyProviderException;
 import org.ebayopensource.turmeric.policyservice.provider.PolicyTypeProvider;
 import org.ebayopensource.turmeric.policyservice.provider.ResourceTypeProvider;
 import org.ebayopensource.turmeric.policyservice.provider.common.PolicyBuilderObject;
 import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
 import org.ebayopensource.turmeric.security.v1.services.Operation;
 import org.ebayopensource.turmeric.security.v1.services.Policy;
 import org.ebayopensource.turmeric.security.v1.services.Resource;
 import org.ebayopensource.turmeric.security.v1.services.Resources;
 import org.ebayopensource.turmeric.security.v1.services.Rule;
 import org.ebayopensource.turmeric.security.v1.services.Subject;
 import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
 import org.ebayopensource.turmeric.security.v1.services.SubjectTypeInfo;
 import org.ebayopensource.turmeric.security.v1.services.Subjects;
 import org.ebayopensource.turmeric.security.v1.services.Target;
 import org.ebayopensource.turmeric.services.policyservice.provider.config.PolicyServiceProviderFactory;
 
 
 /**
  * PolicyBuilder just deals with the construction of a Policy object
  *
  * @author stecheng
  */
  class PolicyBuilder {
 
 	private FindPolicyRequestHelper m_request;
 	private Policy m_policy;
 	private Map<Long, Resource> resourceMap = new HashMap<Long, Resource>();
 	private PolicyBuilderObject m_builderObject = new PolicyBuilderObject();
 	
 	 PolicyBuilder(FindPolicyRequestHelper request, 
 			Policy policy) {
 		m_request = request;
 		m_policy = policy;
 	}
 
 	 void populatePolicy() throws ServiceException, PolicyProviderException {
 		addTargetsToPolicy();
 		if (m_request.outputRules())
 			addRulesToPolicy();
 		
 		m_builderObject = getPolicyProvider().applyQueryCondition(m_builderObject, m_request.getQueryCondition());
 		
 		buildPolicy();
 		
 	}
 
 	private void buildPolicy() throws ServiceException {
 		addToPolicyTarget(m_builderObject.getResources().values());
 		
 		// add inclusion subjects
 		Map<Long, Subject> inclusionSubjects = m_builderObject.getInclusionSubjects();
 		populateSubjectIds(inclusionSubjects, true);
 		addSubjectsToPolicy(new ArrayList<Subject>(inclusionSubjects.values()));
 		
 		// add exclusion subjects
 		Map<Long, Subject> exclusionSubjects = m_builderObject.getExclusionSubjects();
		populateSubjectIds(exclusionSubjects, true);
 		addSubjectsToPolicy(new ArrayList<Subject>(exclusionSubjects.values()));
 		
 		// add global subject types
 		List<String> globalSubjectsTypes = m_builderObject.getGlobalSubjects();
 		List<Subject> globalSubjects = buildGlobalSubjects(globalSubjectsTypes);
 		addSubjectsToPolicy(globalSubjects);
 		
 		// add inclusion subject groups
 		Map<Long, SubjectGroup> inclusionSubjectGrps = m_builderObject.getInclusionSubjectGrps();
 		populateSubjectGroupIds(inclusionSubjectGrps, true);
 		addSubjectGroupsToPolicy(inclusionSubjectGrps.values());
 		
 		// add exclusion subject groups
 		Map<Long, SubjectGroup> exclusionSubjectGrps = m_builderObject.getExclusionSubjectGrps();
 		populateSubjectGroupIds(exclusionSubjectGrps, false);
 		addSubjectGroupsToPolicy(exclusionSubjectGrps.values());
 		
 		m_policy.getRule().addAll(m_builderObject.getRules().values());
 				
 	}
 
 	private void addTargetsToPolicy() throws ServiceException, PolicyProviderException {
 		if ( m_request.outputResources() )
 			addResourcesToPolicy();
 		if ( m_request.outputSubjects() )
 			addSubjectsToPolicy();		
 	}
 
 	private void addRulesToPolicy() throws ServiceException,  PolicyProviderException {
 		PolicyTypeProvider policyProvider = getPolicyProvider();
 		Map<Long, Rule> rules = policyProvider.getRuleAssignmentOfPolicy(m_policy.getPolicyId(), m_request.getQueryCondition());
 		
 		if (rules != null && !rules.isEmpty()) {
 			// m_policy.getRule().addAll(rules.values());
 			m_builderObject.setRules(rules);
 		}
 	}
 
 	private void addSubjectsToPolicy() throws ServiceException,  PolicyProviderException {
 		PolicyTypeProvider policyProvider = getPolicyProvider();
 		
 		// get individual subjects
 		Long policyId = m_policy.getPolicyId();
 		Map<Long, Subject> inclSubjects = policyProvider.getSubjectAssignmentOfPolicy(policyId, m_request.getQueryCondition());
 		if (inclSubjects != null && !inclSubjects.isEmpty()) {
 			
 			m_builderObject.setInclusionSubjects(inclSubjects);
 		}
 		
 		// get global subjects
 		Map<Long, SubjectTypeInfo> subjectTypeMap = policyProvider.getSubjectTypeAssignmentOfPolicy(policyId, m_request.getQueryCondition());
 		if (subjectTypeMap != null && !subjectTypeMap.isEmpty()) {
 			List<String> subjectTypes = new ArrayList<String>();
 			Iterator<Entry<Long,SubjectTypeInfo>> iter = subjectTypeMap.entrySet().iterator();
 			while(iter.hasNext())
 			{
 				SubjectTypeInfo subjectTypeInfo = iter.next().getValue();
 				subjectTypes.add(subjectTypeInfo.getName());
 			}
 
 			m_builderObject.setGlobalSubjects(subjectTypes);
 		}
 		
 		// get exclusion subjects
 		Map<Long, Subject> exclSubjects  = policyProvider.getExclusionSubjectAssignmentOfPolicy(policyId, m_request.getQueryCondition());
 		if (exclSubjects != null && !exclSubjects.isEmpty()) {
 			m_builderObject.setExclusionSubjects(exclSubjects);
 		}
 		
 		if (m_request.outputSubjectGroups()) {
 		
 			// get subject groups
 			Map<Long, SubjectGroup> inclSubjGrps  = policyProvider.getSubjectGroupAssignmentOfPolicy(policyId, m_request.getQueryCondition());
 			if (inclSubjGrps != null && !inclSubjGrps.isEmpty()) {
 				m_builderObject.setInclusionSubjectGrps(inclSubjGrps);
 			}
 			
 			// get excluded subject groups
 			Map<Long, SubjectGroup> exclSubjGrps   = policyProvider.getExclusionSubjectGroupAssignmentOfPolicy(policyId, m_request.getQueryCondition());
 			if (exclSubjGrps != null && !exclSubjGrps.isEmpty()) {
 				m_builderObject.setExclusionSubjectGrps(exclSubjGrps);
 			}
 		}
 	}
 	
 	private List<Subject> buildGlobalSubjects(List<String> subjectTypes) {
 		List<Subject> globalSubjects = new ArrayList<Subject>(); 
 		for (String subjectType : subjectTypes) {
 			Subject subject = new Subject();
 			subject.setSubjectType(subjectType);
 			Utils.setAllSubjectId(subject);
 			globalSubjects.add(subject);
 		}
 		return globalSubjects;
 	}
 
 	private void populateSubjectIds(Map<Long, Subject> subjects, boolean inclusion) throws ServiceException {
 		for (Map.Entry<Long, Subject> subjectEntry : subjects.entrySet()) {
 			Long id = subjectEntry.getKey();
 			
 			if (inclusion)
 				Utils.setSubjectId(subjectEntry.getValue(), id);
 			else 
 				Utils.setExclusionSubjectId(subjectEntry.getValue(), id);
 		}		
 	}
 	
 	private void populateSubjectGroupIds(Map<Long, SubjectGroup> sgs, boolean inclusion) throws ServiceException {
 		for (Map.Entry<Long, SubjectGroup> sgEntry : sgs.entrySet()) {
 			Long id = sgEntry.getKey();
 			if (inclusion)
 				Utils.setSubjectGroupId(sgEntry.getValue(), id);
 			else
 				Utils.setExclusionSubjectGroupId(sgEntry.getValue(), id);
 		}		
 	}
 
 	private void addSubjectGroupsToPolicy(Collection<SubjectGroup> sgs) {
 		if (m_policy.getTarget() == null) {
 			m_policy.setTarget(new Target());
 		}
 		Target target = m_policy.getTarget();
 		if (target.getSubjects() == null) {
 			target.setSubjects(new Subjects());
 		}
 		Subjects subjectWrapper = target.getSubjects();
 		subjectWrapper.getSubjectGroup().addAll(sgs);
 		
 	}
 
 	private void addSubjectsToPolicy(List<Subject> subjects) {
 		if (m_policy.getTarget() == null) {
 			m_policy.setTarget(new Target());
 		}
 		Target target = m_policy.getTarget();
 		if (target.getSubjects() == null) {
 			target.setSubjects(new Subjects());
 		}
 		Subjects subjectWrapper = target.getSubjects();
 		subjectWrapper.getSubject().addAll(subjects);
 		
 	}
 
 	private PolicyTypeProvider getPolicyProvider() throws ServiceException {
 		return PolicyServiceProviderFactory.getPolicyTypeProvider(m_policy.getPolicyType().toString());
 	}
 
 	private void addResourcesToPolicy() throws ServiceException,  PolicyProviderException {
 		
 		PolicyTypeProvider policyProvider = getPolicyProvider();
 		// first get the resources, 
 		Map<Long, Resource> resources = policyProvider.getResourceAssignmentOfPolicy(m_policy.getPolicyId(), m_request.getQueryCondition());
 		if (resources != null) {
 			for (Map.Entry<Long, Resource> entry : resources.entrySet()) {
 				Resource resource = entry.getValue();
 				resource.setResourceId(entry.getKey());
 				 if (m_request.isTargetExpandResourcesSpecified()) {
 					 ResourceTypeProvider resourceProvider = 
 						 PolicyServiceProviderFactory.getResourceTypeProvider(resource.getResourceType());
 					 List<Operation> operations = resourceProvider.getOperationByResourceId(resource.getResourceId());
 					 resource.getOperation().addAll(operations);
 				 }
 				 resourceMap.put(resource.getResourceId(), resource);
 			}
 		}
 		
 		// Get the associated operations 
 		Map<Long, Operation> operations = policyProvider.getOperationAssignmentOfPolicy(m_policy.getPolicyId(), m_request.getQueryCondition());
 		Long resourceId = null;
 		if (operations != null) {
 			for (Map.Entry<Long, Operation> entry : operations.entrySet()) {
 				Operation operation = entry.getValue();
 				operation.setOperationId(entry.getKey());
 				resourceId = operation.getResourceId(); //unmasked resource id
 				if (resourceMap.containsKey(operation.getResourceId())) {
 					// only add if not expanded already
 					if (!m_request.isTargetExpandResourcesSpecified()) {
 						// add to the operations list
 						resourceMap.get(operation.getResourceId()).getOperation().add(operation);
 					}
 				} else {
 					// create the resource
 					Resource resource = null;
 					Set<String> resourceTypes = PolicyServiceProviderFactory.getResourceTypes();
 					for (String type: resourceTypes)
 					{
 						 ResourceTypeProvider resourceTypeProvider = PolicyServiceProviderFactory.
 							getResourceTypeProvider(type);
 						 
 						resource = resourceTypeProvider.getResourceInfoById(resourceId); 
 						if (resource != null)
 						{
 							resource.getOperation().add(operation);
 							resourceMap.put(resource.getResourceId(), resource);
 							break;
 						}
 					}
 				}	
 			}
 		}
 		m_builderObject.setResources(resourceMap);
 	}
 
 	private void addToPolicyTarget(Collection<Resource> resources) {
 		
 		if (m_policy.getTarget() == null) {
 			m_policy.setTarget(new Target());
 		}
 		Target target = m_policy.getTarget();
 		if (target.getResources() == null) {
 			target.setResources(new Resources());
 		}
 		
 		target.getResources().getResource().addAll(resources);
 	}
 }
