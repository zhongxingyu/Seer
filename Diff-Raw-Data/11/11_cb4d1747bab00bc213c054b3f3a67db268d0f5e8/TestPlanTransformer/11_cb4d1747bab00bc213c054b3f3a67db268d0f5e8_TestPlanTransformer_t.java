 package com.mulesoft.tcm.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.mule.api.MuleMessage;
 import org.mule.api.annotations.Transformer;
 import org.mule.api.processor.MessageProcessor;
 import org.mule.api.transformer.TransformerException;
 import org.mule.util.CaseInsensitiveHashMap;
 import org.mule.transformer.AbstractMessageAwareTransformer;
 
 public class TestPlanTransformer extends AbstractMessageAwareTransformer{
 
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public ArrayList<FeatureCoverage> transform(MuleMessage message, String outputEncoding) throws TransformerException {
 		
 		ArrayList<FeatureCoverage> coverage = new ArrayList<FeatureCoverage>();
 		ArrayList<CaseInsensitiveHashMap> records = (ArrayList<CaseInsensitiveHashMap>) message.getPayload();
 		
 		for(CaseInsensitiveHashMap row: records){
 			
			if(!coverage.isEmpty() && containsIssue(coverage, (String) row.get("FtrName")) )
 			{
 				for(FeatureCoverage ftr: coverage){
					if(ftr.name.contains( (String)row.get("FtrName"))){
 						
 						TestCase tc = new TestCase();
 						tc.id = (Integer) row.get("id");
 						tc.name = (String) row.get("name");
 						
 						ftr.testCase.add(tc);				
 							
 						
 					}
 				}
 				
 			}else{
 				FeatureCoverage ftrCoverage = new FeatureCoverage();
 				
 				ftrCoverage.jiraKey = (String) row.get("jiraKey");
 				ftrCoverage.name = (String) row.get("FtrName");
 				
 				ArrayList<TestCase> tcs = new ArrayList<TestCase>();
 				
 				TestCase tc = new TestCase();
 				tc.id = (Integer) row.get("id");
 				tc.name = (String) row.get("name");
 				
 				tcs.add(tc);
 				
 				ftrCoverage.testCase = tcs;
 				coverage.add(ftrCoverage);
 			}
 			
 		}
 				
 		return coverage;
 	}
 
	private boolean containsIssue(ArrayList<FeatureCoverage> coverage, String name) {
 		boolean bret = false;
 		
 		for(FeatureCoverage ftr: coverage){
			if(ftr.name.contains(name)){
 				bret = true;
 			}
 		}
 		
 		return bret;
 	}
 
 	
 	 
 }
