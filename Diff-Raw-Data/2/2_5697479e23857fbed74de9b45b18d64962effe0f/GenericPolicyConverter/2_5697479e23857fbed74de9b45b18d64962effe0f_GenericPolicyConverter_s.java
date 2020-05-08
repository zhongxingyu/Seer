 /********************************************************************
  * Copyright (c) 2010 eBay Inc., and others. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************/
 package org.ebayopensource.turmeric.monitoring.client.model.policy;
 
 import java.util.List;
 
 
 /**
  * GenericPolicyConverter
  *
  */
 public class GenericPolicyConverter {
     
 	public static String toNV(GenericPolicy policy) {
         String url = "";
         if (policy == null)
             return url;
         
         url += (policy.getId()==null?"":"&ns1:policy.@PolicyId="+policy.getId().toString());
         
         //type, name are mandatory
         
         url += (policy.getType()==null||policy.getType().equals("")?"":"&ns1:policy.@PolicyType="+policy.getType().toString());
         String tmp = policy.getName();
         if (tmp != null && !"".equals(tmp.trim()))
             url += "&ns1:policy.@PolicyName="+tmp;
 
         //description & status are optional       
        url += "&ns1:policy.@Active="+policy.getEnabled();
         url += (policy.getDescription()==null || "".equals(policy.getDescription().trim())?"":"&ns1:policy.ns2:Description="+policy.getDescription());
         
         //rule is optional for RateLimiting policies
         if (policy.getRules() != null  ){
             int i=0;
         	for (Rule rule:policy.getRules()){ 
 	        	url += (rule.getEffect()==null?"":"&ns1:policy.ns1:Rule("+i+").@Effect="+rule.getEffect());
 	        	url += (rule.getRuleName()==null?"":"&ns1:policy.ns1:Rule("+i+").@RuleName="+rule.getRuleName());
 	        	url += (rule.getPriority()==null?"":"&ns1:policy.ns1:Rule("+i+").@Priority="+rule.getPriority());
 	        	url += (rule.getRolloverPeriod()==null?"":"&ns1:policy.ns1:Rule("+i+").@RolloverPeriod="+rule.getRolloverPeriod());
 	        	url += (rule.getEffectDuration()==null?"":"&ns1:policy.ns1:Rule("+i+").@EffectDuration="+rule.getEffectDuration());
 	        	url += (rule.getConditionDuration()==null?"":"&ns1:policy.ns1:Rule("+i+").@ConditionDuration="+rule.getConditionDuration());
 	         	url += (rule.getDescription()==null?"":"&ns1:policy.ns1:Rules("+i+").@Description="+rule.getDescription());
 	         		
 	         	if(rule.getCondition()!=null){
 	        		Condition condition = rule.getCondition();
 	        		if( condition.getExpression()!=null){
 	        			Expression expression = condition.getExpression();
 	        			url += (expression.getName() == null ? "" :("&ns1:policy.ns1:Rule.ns1:Condition.ns1:Expression.@name="+rule.getConditionDuration()));
 	        			if(expression.getPrimitiveValue() != null){
 	        				PrimitiveValue primitiveValue = expression.getPrimitiveValue();
 	        				url += "&ns1:policy.ns1:Rule.ns1:Condition.ns1:Expression.ns1:PrimitiveValue.@type="+primitiveValue.getType();
 	        				url += "&ns1:policy.ns1:Rule.ns1:Condition.ns1:Expression.ns1:PrimitiveValue.@value="+primitiveValue.getValue();
 	        			}
 	        		
 	        		}
 	        			
 	        	}
       	
 	         	if (rule.getAttributeList() != null ) {
 	         		List<RuleAttribute> attributeList = rule.getAttributeList();
 	         		if(attributeList.size() > 0){
          				int j=0;
 	         			for (RuleAttribute  attribute: attributeList) {
 		                   	url += "&ns1:policy.ns1:Rule.ns1:Attribute("+j+").ns1:key="+attribute.getKey().toString();
 		                    url += "&ns1:policy.ns1:Rule.ns1:Attribute("+j+").ns1:value="+attribute.getValue().toString();
 		                    j++;
 	         			}	
 	         		}
 	         		
 	         	}
 	        	
 	        	
 	        	i++;
         	}
         }
      
         
         //resources
         if (policy.getResources() != null) {
             int i=0;
             for (Resource r:policy.getResources()) {
                 url += (r.getId()==null?"":"&ns1:policy.ns1:Target.ns1:Resources.ns1:Resources("+i+").@ResourceId="+r.getId());
                 url += (r.getResourceName()==null?"":"&ns1:policy.ns1:Target.ns1:Resources.ns1:Resource("+i+").@ResourceName="+r.getResourceName());
                                 url += (r.getResourceType()==null?"":"&ns1:policy.ns1:Target.ns1:Resources.ns1:Resource("+i+").@ResourceType="+r.getResourceType());
                 url += (r.getDescription()==null?"":"&ns1:policy.ns1:Target.ns1:Resources.ns1:Resource("+i+").@Description="+r.getDescription());
                 														
                 if (r.getOpList() != null) {
                     int j=0;
                     for (Operation op:r.getOpList()) {
                         url += (op.getOperationName()==null?"":"&ns1:policy.ns1:Target.ns1:Resources.ns1:Resource("+i+").ns1:Operation("+j+").@OperationName="+op.getOperationName());
                         j++;
                     }
                 }
                 i++;
             }
         }
         
         
         //subjects and subjectgroups are optional also
         //Subject and SubjectGroups have only their keys populated as the server side
         //is only interested in that, so that is name & type
       
         if (policy.getSubjectGroups() != null) {
             int i=0;
             for (SubjectGroup sg:policy.getSubjectGroups()) {
                 url += (sg.getName()==null?"":"&ns1:policy.ns1:Target.ns1:Subjects.ns1:SubjectGroup("+i+").@SubjectGroupName="+sg.getName());
                 url += (sg.getType()==null?"":"&ns1:policy.ns1:Target.ns1:Subjects.ns1:SubjectGroup("+i+").@SubjectType="+sg.getType().toString());
                 i++;
             }
         }
         
         if (policy.getSubjects() != null) {
             int i=0;
             for (Subject s:policy.getSubjects()) {
                 url += (s.getName()==null?"":"&ns1:policy.ns1:Target.ns1:Subjects.ns1:Subject("+i+").@SubjectName="+s.getName());
                 url += (s.getType()==null?"":"&ns1:policy.ns1:Target.ns1:Subjects.ns1:Subject("+i+").@SubjectType="+s.getType().toString());
                 i++;
             }
         }
         return url;
         
     }
     
     public String toJSON (GenericPolicy policy) {
         String json = "";
         if (policy == null)
             return json;
         
         //TODO
         /*
                 String json = ""+
                 "{" +
                 "   \"jsonns.ns1\":\""+NAMESPACE+"\"," +
                 "   \"jsonns.ns2\":\""+OASIS_NAMESPACE+"\"," +
                 "   \"ns1.createPolicyRequest\":{" +
                 "           \"ns1.policy\": {";
                 
                 json += (policy.getType() == null?"":"           \"@PolicyType\":\""+policy.getType().toString().toUpperCase()+"\"");
                 json += (policy.getName() == null?"":",           \"@PolicyName\":\""+policy.getName()+"\"");
                 json += (policy.getDescription()== null?"":",     \"ns2.Description\":\""+policy.getDescription()+"\""); 
                 
                 if (policy.getSubjectGroups() != null) {
                     json += ",\"ns1.Target\": { ";
                     json += "                   \"ns1.Subjects\": {";
                     json += "                                       \"ns1.SubjectGroup\": [";
                     
                     for (int i=0; i<policy.getSubjectGroups().size(); i++) {
                         SubjectGroup sg = policy.getSubjectGroups().get(i);
                         json += "                                                          {\"@SubjectGroupName\": \""+sg.getName()+"\"";
                         json += ",                                                           \"@SubjectType\": \""+sg.getType().toString()+"\"}";
                         if (i<policy.getSubjectGroups().size() - 1)
                             json += ",";
                     }
                     json += "                                                              ]";
                 }
                 
                 if (policy.getSubjects() != null) {
                     
                     if (policy.getSubjectGroups() == null) {
                         json += ",\"ns1.Target\": { ";
                         json += "                   \"ns1.Subjects\": {";
                     }
                     else
                         json += ", ";
                     json += "                                       \"ns1.Subject\": [";
                     for (int i=0; i<policy.getSubjects().size(); i++) {
                         Subject s = policy.getSubjects().get(i);
                         json +=   "                                                    {\"@SubjectName\": \""+s.getName()+"\"";
                         json +=                                                       ",\"@SubjectType\": \""+s.getType().toString()+"\"}";
                         if (i<policy.getSubjects().size())
                             json += ",";
                     }  
                     json += "                                                         ]";
                 }
 
                 if (policy.getSubjects() != null || policy.getSubjectGroups() != null) {
                     json += "                                                }";
                     json += "                            }";    
                 }
 
                 json += "                 }";
                 json += "          }";
                 json += "}";
                 GWT.log(json);
         */
         return json;
     }
 
 }
