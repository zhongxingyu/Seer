 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.test.services.utils;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.ebayopensource.turmeric.security.v1.services.EffectType;
 import org.ebayopensource.turmeric.security.v1.services.Policy;
 
 public class PolicyDataModelHelper {
  
 	public static HashMap<String,Policy> hm = null;
 	
 	public static HashMap<String, Policy> createPolicyDataModel(Class name, String... policyIds) throws Exception{
 		TestDataReader reader = new TestDataReader(name);
 		hm = new HashMap<String,Policy>();
 		for(String policyId : policyIds){
 		  Policy p = createPolicyObject(reader,policyId);
 		  hm.put(policyId, p);
 		}
 		return hm;
 	}
 	
 	public static void deletePolicyDataModel(Class name, String... policyIds) throws Exception{
 		TestDataReader reader = new TestDataReader(name);
 		for(String policyId : policyIds)
 		   deletePolicyObject(reader,policyId);
 	}
 	
 	public static void deletePolicyObject(TestDataReader reader, String policyId){
 		PolicyServiceTestHelper helper = PolicyServiceTestHelper.getInstance(false);
 		String policyName = reader.getEntryValue(policyId, "policyName");
 		String type = reader.getEntryValue(policyId, "policyType");
 		if(policyName!= null || !policyName.equals("") || !type.equals("") || type!=null)
 			helper.cleanupPolicy(policyName.trim(), type);
 	}
 	
 	public static Policy createPolicyObject(TestDataReader reader, String policyId) throws Exception{
         PolicyServiceTestHelper helper = PolicyServiceTestHelper.getInstance(false);
 		
 		String type = reader.getEntryValue(policyId, "policyType").trim();
 		String policyName = reader.getEntryValue(policyId, "policyName");
 		String policyDesc = reader.getEntryValue(policyId, "policyDesc");
 		
 		if(type==null || policyName==null || policyDesc==null || type.equals("") || policyName.equals("") || policyDesc.equals(""))
 			throw new Exception("PolicyType, PolicyName and PolicyDescription should not be null or empty");
 		
 		List<String> resourceMap = new ArrayList<String>();
 		String resourceList = reader.getEntryValue(policyId, "resourceMap");
 		if(resourceList!=null || !resourceList.equals("")){
 		   String[] resKeys = resourceList.trim().split(",");
 		   for(int i=0; i < resKeys.length; i++)
 			   resourceMap.add(resKeys[i]);
 		}
 		
 		List<String> globalSubjectDomainList = new ArrayList<String>();
 		String globalSubjects = reader.getEntryValue(policyId, "globalSubjectDomainList");
 		if(globalSubjects!=null && !globalSubjects.equals("")){
 		   String[] globalSubKeys = globalSubjects.trim().split(",");
 		   for(int i=0; i < globalSubKeys.length; i++)
 			 globalSubjectDomainList.add(globalSubKeys[i]);
 		}
 		
 		List<String> subjectInclusionList = new ArrayList<String>();
 		String subInclusion = reader.getEntryValue(policyId, "subjectInclusion");
 		if(subInclusion!=null && !subInclusion.equals("")){
 		    String[] subInclusionKeys = subInclusion.trim().split(",");
 		    for(int i=0; i < subInclusionKeys.length; i++)
 			   subjectInclusionList.add(subInclusionKeys[i]);
 		}
 		
 		List<String> subjectExclusionList = new ArrayList<String>();
 		String subExclusion = reader.getEntryValue(policyId, "subjectExclusion");
 		if(subExclusion!=null && !subExclusion.equals("")){
 		   String[] subExclusionKeys = subExclusion.trim().split(",");
 		   for(int i=0; i < subExclusionKeys.length; i++)
 			  subjectExclusionList.add(subExclusionKeys[i]);
 		}
 		
 		List<String> subjectGroupInclusion = new ArrayList<String>();
 		String subGroupInclusion = reader.getEntryValue(policyId, "subjectGroupInclusion");
 		if(subGroupInclusion!=null && !subGroupInclusion.equals("")){
 		   String[] subGroupInclusionKeys = subGroupInclusion.trim().split(",");
 		   for(int i=0; i < subGroupInclusionKeys.length; i++){
 				subjectGroupInclusion.add(subGroupInclusionKeys[i]);
 		   }
 		}
 		
 		List<String> subjectGroupExclusion = new ArrayList<String>();
 		String subGroupExclusion = reader.getEntryValue(policyId, "subjectGroupExclusion");
 		if(subGroupExclusion!=null && !subGroupExclusion.equals("")){
 		    String[] subGroupExclusionKeys = subGroupExclusion.trim().split(",");
 		    for(int i=0; i < subGroupExclusionKeys.length; i++){
 				subjectGroupExclusion.add(subGroupExclusionKeys[i]);
 		    }
 		}
 		
 		long duration = 0L;
 		if(reader.getEntryValue(policyId, "duration")!=null)
 			duration = Long.parseLong(reader.getEntryValue(policyId, "duration").trim());
 		
 		long rolloverperiod = 0L;
 		if(reader.getEntryValue(policyId, "rolloverperiod")!=null)
 			rolloverperiod = Long.parseLong(reader.getEntryValue(policyId, "rolloverperiod").trim());
 		
 		String condition = reader.getEntryValue(policyId, "condition");
 		
 		EffectType effect = null;
 		if(reader.getEntryValue(policyId, "effect")!=null)
 			effect = EffectType.valueOf(reader.getEntryValue(policyId, "effect").trim());
 		
 		boolean isActive = true;
 		if(reader.getEntryValue(policyId, "Active")!=null)
 			isActive = Boolean.parseBoolean(reader.getEntryValue(policyId, "Active").trim());
 		
 		if(effect!=null && effect.equals("RL") && (condition==null && condition.equals("")))
 		   throw new Exception("Condition should not be empty or Null when policyType is RL");
	    Policy p = null;
	    p = helper.constructPolicy(type, policyName, policyDesc, resourceMap, globalSubjectDomainList, subjectInclusionList, subjectGroupInclusion, subjectExclusionList, subjectGroupExclusion, duration, rolloverperiod, effect, condition, isActive);
 	    
 	    return p;
 
 	}
 	
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		try {
 //			String[] policies = {"p1","p2"};
 //			TestTokenRetrivalObject.setTokenRetrivalObject((SecurityTokenUtility)new TurmericTokenProviderHelper());	
 //			HashMap<String,Policy> hm = PolicyDataModelHelper.createPolicyDataModel(AuthorizationServiceQATest.class,policies);
 //			Policy p = hm.get("p1");
 //			PolicyDataModelHelper.deletePolicyDataModel(AuthorizationServiceQATest.class, policies);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
