 /*******************************************************************************
  * Copyright (c) 2010 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.ebayopensource.turmeric.ratelimiterservice;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.StringTokenizer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.manager.cassandra.server.CassandraTestManager;
 import org.ebayopensource.turmeric.security.v1.services.CreatePolicyRequest;
 import org.ebayopensource.turmeric.security.v1.services.CreatePolicyResponse;
 import org.ebayopensource.turmeric.security.v1.services.CreateResourcesRequest;
 import org.ebayopensource.turmeric.security.v1.services.CreateResourcesResponse;
 import org.ebayopensource.turmeric.security.v1.services.CreateSubjectGroupsRequest;
 import org.ebayopensource.turmeric.security.v1.services.CreateSubjectGroupsResponse;
 import org.ebayopensource.turmeric.security.v1.services.EnablePolicyRequest;
 import org.ebayopensource.turmeric.security.v1.services.EnablePolicyResponse;
 import org.ebayopensource.turmeric.security.v1.services.FindPoliciesRequest;
 import org.ebayopensource.turmeric.security.v1.services.FindPoliciesResponse;
 import org.ebayopensource.turmeric.security.v1.services.IsRateLimitedRequest;
 import org.ebayopensource.turmeric.security.v1.services.IsRateLimitedResponse;
 import org.ebayopensource.turmeric.security.v1.services.Operation;
 import org.ebayopensource.turmeric.security.v1.services.Policy;
 import org.ebayopensource.turmeric.security.v1.services.PolicyKey;
 import org.ebayopensource.turmeric.security.v1.services.Resource;
 import org.ebayopensource.turmeric.security.v1.services.Subject;
 import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
 import org.ebayopensource.turmeric.security.v1.services.SubjectType;
 import org.ebayopensource.turmeric.services.ratelimiterservice.intf.gen.BaseRateLimiterServiceConsumer;
 import org.ebayopensource.turmeric.test.services.utils.FindPolicyHelper;
 import org.ebayopensource.turmeric.test.services.utils.PolicyServiceTestHelper;
 import org.junit.After;
 import org.junit.Before;
import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 
 @RunWith(Parameterized.class)
 public class RatelimiterTests{
 
 	private static final String CONFIG_DELIMITER = ":";
 	private String m_testCaseName;
 	private String m_resource;
 	private String m_subjects;
 	private String m_numOfHits;
 	private String m_expectedResult;
 	private static Properties props = new Properties();
 	private static Map<String,Long> m_policyIds = new HashMap<String,Long>();
 	private static Map<String,Long> m_subjectIds = new HashMap<String,Long>();
 	private static Map<String,Long> m_resourceIds = new HashMap<String,Long>();
 	private static Map<String,Long> m_subjectGroupIds = new HashMap<String,Long>();
 	private static List<Resource> m_resourceList = new ArrayList<Resource>();
 	private static List<Subject> m_subjList = new ArrayList<Subject>();
 	private static List<SubjectGroup> m_subjGroupList = new ArrayList<SubjectGroup>();
 	private static List<Policy> m_policyList = new ArrayList<Policy>();
 	BaseRateLimiterServiceConsumer consumer = new BaseRateLimiterServiceConsumer();
 	private static final String s_PropFilePath = "RateLimiterTests.properties";
 	private static final String m_PreRequisitePoliciesFilePath = "PreRequisitePolicies.properties";

	@BeforeClass
	public static void setupCassandraConfigFile() throws Exception {
		System.setProperty("log4j.configuration", "META-INF/config/cassandra/log4j.properties");
		System.setProperty("cassandra.config", "META-INF/config/cassandra/cassandra-test.yaml");
		CassandraTestManager.initialize();
	}
 	
		
 	@Before
 	public void setUp() throws Exception {
 		System.out.println(" *** Create Pre-Requisite data ***");
 		createPreRequisiteData();
 		System.out.println(" *** Create Pre-Requisite data has been completed successfully ***");
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		cleanUpPolicies();
 		cleanUpResources();
 		cleanUpSubjectGroups();
 		cleanUpSubjects();
 	}
 	
 	private RatelimiterTests(){
 		
 	}
 
 	public RatelimiterTests(String testCaseName, 
 			String resource, String subjects,String numOfHits, String expectedResult) {
 		this.m_testCaseName = testCaseName;
 		this.m_resource = resource;
 		this.m_subjects = subjects;
 		this.m_numOfHits = numOfHits;
 		this.m_expectedResult = expectedResult;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Parameters
 	public static Collection data(){
 		return loadValidatePolicyData();
 	}
 	
     @Test
 	public void ratelimiterPolicy() throws Exception {
     	System.out.println("\n*** Test Scenario :  " + m_testCaseName + " *** ");
     	IsRateLimitedRequest request = new IsRateLimitedRequest();
     	IsRateLimitedResponse response = new IsRateLimitedResponse();
     	
 			constructIsRatelimiterRequest(request);
 			int numOfSvcInvokes = 0;
 			if (m_numOfHits!=null) {
 				numOfSvcInvokes = Integer.parseInt(m_numOfHits);
 			}
 			for (int i=0; i<numOfSvcInvokes;i++) {
 				response = consumer.isRateLimited(request);	
 			}
 			
 			String errorMessage = response.getErrorMessage() != null ? response
 					.getErrorMessage().getError().get(0).getMessage() : null;
 			StringTokenizer resTokens = new StringTokenizer(m_expectedResult,"|");
 			String expectedAckValue = getToken(resTokens);
 			String expectedErrorMessage = getToken(resTokens);
 			String validationStatus = getToken(resTokens);
 			
 			if (expectedAckValue.equalsIgnoreCase("success")) {
 				assertEquals(errorMessage, AckValue.SUCCESS, response.getAck());
 				assertNull(errorMessage, response.getErrorMessage());
 				assertEquals(validationStatus,response.getStatus().toString());
 			} else {
 				assertEquals(errorMessage, AckValue.FAILURE, response.getAck());
 				assertNotNull(errorMessage, response.getErrorMessage());
 				assertEquals(errorMessage, expectedErrorMessage,errorMessage);
 				assertEquals(validationStatus,response.getStatus().toString());
 			}
 	
 		System.out.println("*** Test Scenario : " + m_testCaseName + " completed successfully ***");
 	}
 
 		
 	@SuppressWarnings("unchecked")
 	public static Collection loadValidatePolicyData() {
 		List  list = new ArrayList();
 		List eachRowData = new ArrayList();
 		Pattern pattern =Pattern.compile("testcase(\\d*).name");
 		int totalTests = 0;
 		try {
 			InputStream inputStream = RatelimiterTests.class.getResourceAsStream(s_PropFilePath);
 			if(inputStream==null){
 				inputStream =loadProperties(RatelimiterTests.class, s_PropFilePath);
 			}
 			props.load(inputStream);
 			Iterator it = props.keySet().iterator();
 			while(it.hasNext()) {
 				String key = it.next().toString();
 				Matcher matcher = pattern.matcher(key);
 				if (matcher.find()) {
 					totalTests++;
 				}
 			}
 			for (int i = 0; i < totalTests; i++) {
 				eachRowData = new ArrayList();
 				String testName = "testcase" + i + ".name";
 				String resource = "testcase" + i + ".request.resource";
 				String subjects = "testcase" + i + ".request.subject";
 				String numOfHits  = "testcase" + i + ".request.numofhits";
 				String expectedResult = "testcase" + i + ".response";
 				eachRowData.add(props.getProperty(testName));
 				eachRowData.add(props.getProperty(resource));
 				eachRowData.add(props.getProperty(subjects));
 				eachRowData.add(props.getProperty(numOfHits));
 				eachRowData.add(props.getProperty(expectedResult));
 				list.add(eachRowData.toArray());
 			}
 		} catch (IOException e) {	}
 		
 		return list;
 	}
 	
 	
 		
 
 	private static String getToken(StringTokenizer tokenizer)
 	{
 		if(tokenizer.hasMoreTokens()){
 			String token = tokenizer.nextToken().trim();
 			return "null".equals(token) ? null : token;
 		} 
 		return null;
 	}
 		
 	private void  constructIsRatelimiterRequest(IsRateLimitedRequest request) throws Exception {
 		
 		if (m_resource!=null) {
 			StringTokenizer resourceTokens = new StringTokenizer(m_resource,CONFIG_DELIMITER);
 			String resType =getToken(resourceTokens);
 			String resName= getToken(resourceTokens);
 			request.setResourceName(resName);
 			request.setResourceType(resType);
 			System.out.println("resourceType = "+ resType+ ", resourceName = "+ resName);
 			while (resourceTokens.hasMoreElements()) {
 				request.setOperationName(getToken(resourceTokens));
 				System.out.print("\n\t Operation name = "+request.getOperationName());
 			}
 		}
 		if (m_subjects!=null) {
 			SubjectType subject = new SubjectType();
 				StringTokenizer subjTokens = new StringTokenizer(m_subjects,CONFIG_DELIMITER);
 				String subjType =getToken(subjTokens);
 				String subjName= getToken(subjTokens);
 				subject.setValue(subjName);
 				subject.setDomain(subjType);
 				request.getSubject().add(subject);
 		}
 			 		
 	}
 
 	@SuppressWarnings("unchecked")
 	private static void createPreRequisiteData() throws Exception {
 		Properties policiesProps = new Properties();
 		Pattern pattern =Pattern.compile("policy(\\d*).policyInfo");
 		int totalPolicies = 0;
 		try {
 			InputStream inputStream = RatelimiterTests.class.getResourceAsStream(m_PreRequisitePoliciesFilePath);
 			if(inputStream==null){
 				inputStream =loadProperties(RatelimiterTests.class, m_PreRequisitePoliciesFilePath);
 			}
 			policiesProps.load(inputStream);
 			Iterator it = policiesProps.keySet().iterator();
 			while(it.hasNext()) {
 				String key = it.next().toString();
 				Matcher matcher = pattern.matcher(key);
 				if (matcher.find()) {
 					totalPolicies++;
 				}
 			}
 		} catch (IOException e) {}
 					
 			String subjects = policiesProps.getProperty("prerequisite.subjects");
 			String subjectsGroups = policiesProps.getProperty("prerequisite.subjectgroups");
 			String resources = policiesProps.getProperty("prerequisite.resources");
 			
 			createSubjects(subjects);
 			createSubjectGroups(subjectsGroups);
 			createResources(resources);
 			CreatePolicyRequest createPolicyRequest = new CreatePolicyRequest();
 			CreatePolicyResponse res = new CreatePolicyResponse();
 			for (int i = 0; i < totalPolicies; i++) {
 				String policyInfo = policiesProps.getProperty("policy" + i + ".policyInfo");
 				String resourcesInfo = policiesProps.getProperty("policy" + i + ".resources");
 				String includeSubjects = policiesProps.getProperty("policy" + i + ".includeSubjects");
 				String excludeSubjects = policiesProps.getProperty("policy" + i + ".excludeSubjects");
 				String includeSubjectGroups = policiesProps.getProperty("policy" + i + ".includeSubjectGroups");
 				String excludeSubjectGroups = policiesProps.getProperty("policy" + i + ".excludeSubjectGroups");
 				String policyEffect = policiesProps.getProperty("policy" + i + ".policyEffect");
 				String ruleCondition = policiesProps.getProperty("policy" + i + ".ruleCondition");
 				
 				FindPolicyHelper helper = new FindPolicyHelper();
 				createPolicyRequest = helper.constructCreatePolicyRequest(policyInfo, resourcesInfo,
 						null, includeSubjects,
 						excludeSubjects, includeSubjectGroups,
 						excludeSubjectGroups, policyEffect,ruleCondition);
 				m_policyList.add(createPolicyRequest.getPolicy());
 				res = PolicyServiceTestHelper.getInstance().createPolicy(createPolicyRequest);
 				
 				// Make sure the plicies got created successfully
 				assertTrue("Creation of policy " + createPolicyRequest.getPolicy().getPolicyName() + " failed.", res.getAck().equals(AckValue.SUCCESS));
 				
 				// After creating the policies they need to be enabled.
 				PolicyServiceTestHelper phelper = PolicyServiceTestHelper.getInstance(false);
 				EnablePolicyRequest epRequest = new EnablePolicyRequest();
 				PolicyKey policyKey = new PolicyKey();
 				policyKey.setPolicyId(res.getPolicyId());
 				policyKey.setPolicyType(createPolicyRequest.getPolicy().getPolicyType());
 				policyKey.setPolicyName(createPolicyRequest.getPolicy().getPolicyName());
 				epRequest.setPolicyKey(policyKey);
 				EnablePolicyResponse epResp =  phelper.enablePolicy(epRequest);
 				
 				if (!epResp.getAck().equals(AckValue.SUCCESS)) {
 					System.out.println("Policy " + policyKey.getPolicyName() + " was not enabled.");
 				}
 						
 				FindPoliciesRequest fprq = new FindPoliciesRequest();
 				PolicyKey pKey = new PolicyKey();
 				pKey.setPolicyId(res.getPolicyId());
 				pKey.setPolicyType(createPolicyRequest.getPolicy().getPolicyType());
 				fprq.getPolicyKey().add(pKey);
 				
 				FindPoliciesResponse fprp = phelper.findPolicies(fprq);
 				assertTrue(fprp.getAck().equals(AckValue.SUCCESS));
 				assertTrue("No policies returned! ", !fprp.getPolicySet().getPolicy().isEmpty());
 							
 				long policyid = 0L;
 				if (res.getAck().equals(AckValue.SUCCESS)) {
 					policyid = res.getPolicyId();
 					m_policyIds.put(createPolicyRequest.getPolicy().getPolicyName(), policyid);
 				}
 			}		
 	}
 	
 	private static void createSubjects(String subjects) {
 		try {
 			if (subjects!=null) {
 				StringTokenizer subjsList = new StringTokenizer(subjects,";");
 				while(subjsList.hasMoreTokens()) {
 					String token = subjsList.nextToken();
 					StringTokenizer subjTokens = new StringTokenizer(token,CONFIG_DELIMITER);
 					Subject subject = new Subject();
 					subject.setSubjectType(getToken(subjTokens));
 					subject.setSubjectName(getToken(subjTokens));
 					System.out.println(" > Subject Name = " + subject.getSubjectName()+ " ; Type = " + subject.getSubjectType());
 					m_subjList.add(subject);
 					long subjectId = PolicyServiceTestHelper.getInstance().findSubjects(subject.getSubjectType(), subject.getSubjectName());
 					if (subjectId > 0)  m_subjectIds.put(subject.getSubjectName(),subjectId);
 					 else {
 						List<Long> subjIdList = PolicyServiceTestHelper.getInstance().createSubjects(subject.getSubjectType(), subject.getSubjectName());
 						if (subjIdList!=null && subjIdList.size() >0)  	m_subjectIds.put(subject.getSubjectName(),subjIdList.get(0));
 					 }
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			assertTrue(e.getMessage(), false);
 		}
 	}
 	
 	private static void createResources(String resources) {
 		CreateResourcesRequest createResourcesRequest = new CreateResourcesRequest();
 		CreateResourcesResponse createResourcesResponse = new CreateResourcesResponse();
 		List<Resource> resourceList = createResourcesRequest.getResources();
 		try {
 			StringTokenizer resTokens = new StringTokenizer(resources,";");
 			while(resTokens.hasMoreTokens()) {
 				String token = resTokens.nextToken();
 				constructCreateResourceRequest(resourceList,token);
 			}
 			m_resourceList = resourceList; 
 			createResourcesResponse =PolicyServiceTestHelper.getInstance().createResources(createResourcesRequest);
 			if (createResourcesResponse.getResourceIds()!=null) {
 				List<Long> resourceIds= createResourcesResponse.getResourceIds();
 				int i=0;
 				for ( Long id :resourceIds) m_resourceIds.put(resourceList.get(i++).getResourceName(),id);
 				
 			}
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			assertTrue(e.getMessage(), false);
 		}
 		
 	}
 	private static void constructCreateResourceRequest(
 			List<Resource> resList, String token) {
 			Resource resource =  new Resource();
 			StringTokenizer resTokens = new StringTokenizer(token,CONFIG_DELIMITER);
 			resource.setResourceType(getToken(resTokens));
 			resource.setResourceName(getToken(resTokens));
 			List<Operation> operations = resource.getOperation();
 			Operation operation = new Operation();
 			while (resTokens.hasMoreTokens()) {
 				String opsToken = getToken(resTokens);
 				if (opsToken!=null) {
 					operation = new Operation();
 					operation.setOperationName(opsToken);
 				}
 				operations.add(operation);
 			}
 			resList.add(resource);
 		}
 
 	private static void createSubjectGroups(String subjectsGroups) {
 		
 		CreateSubjectGroupsRequest createSubjectGroupsRequest = new CreateSubjectGroupsRequest();
 		CreateSubjectGroupsResponse createSubjectGroupsResponse = new CreateSubjectGroupsResponse();
 		List<SubjectGroup> subjGroupList = createSubjectGroupsRequest.getSubjectGroups();
 		try {
 			StringTokenizer subjTokens = new StringTokenizer(subjectsGroups,";");
 			while(subjTokens.hasMoreTokens()) {
 				String token = subjTokens.nextToken();
 				constructCreateSubjectGroupsRequest(subjGroupList,token);
 			}
 			m_subjGroupList = subjGroupList; 
 			createSubjectGroupsResponse =PolicyServiceTestHelper.getInstance().createSubjectGroups(createSubjectGroupsRequest);
 			
 			if (createSubjectGroupsResponse.getSubjectGroupIds()!=null) {
 				List<Long> subjectGroupIds= createSubjectGroupsResponse.getSubjectGroupIds();
 				int i=0;
 				for ( Long id :subjectGroupIds) {
 					m_subjectGroupIds.put(subjGroupList.get(i++).getSubjectGroupName(),id);
 				}
 			}
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			assertTrue(e.getMessage(), false);
 		}
 	}
 	private static void constructCreateSubjectGroupsRequest(
 			List<SubjectGroup> subjGroupList, String token) {
 			SubjectGroup subjectGroup =  new SubjectGroup();
 			StringTokenizer subjGroupTokens = new StringTokenizer(token,CONFIG_DELIMITER);
 			subjectGroup.setSubjectType(getToken(subjGroupTokens));	
 			subjectGroup.setSubjectGroupName(getToken(subjGroupTokens));
 			subjectGroup.setDescription(getToken(subjGroupTokens));
 			System.out.println(" > SubjectGroup Name = " + subjectGroup.getSubjectGroupName()
 					+ " ; Type = " + subjectGroup.getSubjectGroupName()	);
 			// clean up if subjectgroup already exists
 			PolicyServiceTestHelper.getInstance().cleanupSubjectGroup(subjectGroup.getSubjectGroupName(),subjectGroup.getSubjectType());
 			List<Subject> subjList = subjectGroup.getSubject();
 			Subject subject =new Subject();
 			while (subjGroupTokens.hasMoreTokens()) {
 				String subjectName= getToken(subjGroupTokens);
 				subject = new Subject();
 				subject.setSubjectType(subjectGroup.getSubjectType());
 				subject.setSubjectName(subjectName);
 				subjList.add(subject);
 			}
 			subjGroupList.add(subjectGroup);
 						
 	}
 
 	private static void cleanUpPolicies() {
 		for (Policy policy:m_policyList) {
 			PolicyServiceTestHelper.getInstance().cleanupPolicy(policy.getPolicyName(),policy.getPolicyType());
 		}
 	}
 	
 	private static void cleanUpResources() {
 		for(Resource resource:m_resourceList) {
 			PolicyServiceTestHelper.getInstance().cleanUpResource(resource.getResourceName(), resource.getResourceType());
 		}
 	}
 	private static void cleanUpSubjects() {
 		for(Subject subject:m_subjList) {
 			PolicyServiceTestHelper.getInstance().cleanUpSubjects(subject.getSubjectType(),subject.getSubjectName());
 		}
 	}
 	private static void cleanUpSubjectGroups() {
 		for(SubjectGroup subjectGroup:m_subjGroupList) {
 			PolicyServiceTestHelper.getInstance().cleanupSubjectGroup(subjectGroup.getSubjectGroupName(),subjectGroup.getSubjectType());
 		}
 	}
     public static InputStream loadProperties(Class testClass, String propFileName) throws IOException{
     	
 			InputStream input = testClass.getResourceAsStream(propFileName);
 			if(input==null){
 			   input =	new RatelimiterTests().getInputStream(propFileName);
 			}
 			System.out.println("input"+input);
 			System.out.println("propFileName"+propFileName);
 			//GetGroupMembersTests.properties
 			
 			if(input ==null && propFileName!=null){
 				String packagestr= (testClass.getPackage()!=null?testClass.getPackage().getName() :null );
 				if (packagestr!=null){
 					String newPropFile = (packagestr.trim().replaceAll("\\.", "/")).concat("/").concat(propFileName);
 					System.out.println("newPropFile" +newPropFile);
 					 input =	new RatelimiterTests().getInputStream(newPropFile);
 				}
 			}
       return input;
     }
 	public  InputStream getInputStream(String a) {
 		InputStream is = getClass().getResourceAsStream( a );
 	
 		if(is ==null){
 			is =ClassLoader.getSystemResourceAsStream(a);
 		}
 		if(is ==null){
 			Thread.currentThread().getContextClassLoader();
 			URL resource = ClassLoader.getSystemClassLoader().getResource(a);    
 		    try {
 				is = new FileInputStream(new File(resource.toExternalForm()));
 		    }catch (Exception e) {
 		    	e.printStackTrace();
 				
 			}
 		}
 		
 		return is;
 	}
 }
