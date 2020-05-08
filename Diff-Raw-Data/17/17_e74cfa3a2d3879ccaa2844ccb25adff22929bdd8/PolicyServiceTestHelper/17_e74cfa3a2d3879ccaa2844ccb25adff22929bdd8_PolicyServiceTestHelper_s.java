 package org.ebayopensource.turmeric.test.services.utils;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 
 import javax.xml.ws.Response;
 
 import junit.framework.Assert;
 import oasis.names.tc.xacml._2_0.policy.schema.os.AttributeValueType;
 import oasis.names.tc.xacml._2_0.policy.schema.os.SubjectAttributeDesignatorType;
 import oasis.names.tc.xacml._2_0.policy.schema.os.SubjectMatchType;
 
 import org.ebayopensource.turmeric.common.v1.types.AckValue;
 import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
 import org.ebayopensource.turmeric.runtime.sif.impl.internal.pipeline.AsyncResponse;
 import org.ebayopensource.turmeric.security.v1.services.Condition;
 import org.ebayopensource.turmeric.security.v1.services.CreateExternalSubjectReferenceRequest;
 import org.ebayopensource.turmeric.security.v1.services.CreateExternalSubjectReferencesResponse;
 import org.ebayopensource.turmeric.security.v1.services.CreatePolicyRequest;
 import org.ebayopensource.turmeric.security.v1.services.CreatePolicyResponse;
 import org.ebayopensource.turmeric.security.v1.services.CreateResourcesRequest;
 import org.ebayopensource.turmeric.security.v1.services.CreateResourcesResponse;
 import org.ebayopensource.turmeric.security.v1.services.CreateSubjectGroupsRequest;
 import org.ebayopensource.turmeric.security.v1.services.CreateSubjectGroupsResponse;
 import org.ebayopensource.turmeric.security.v1.services.CreateSubjectsRequest;
 import org.ebayopensource.turmeric.security.v1.services.CreateSubjectsResponse;
 import org.ebayopensource.turmeric.security.v1.services.DeletePolicyRequest;
 import org.ebayopensource.turmeric.security.v1.services.DeletePolicyResponse;
 import org.ebayopensource.turmeric.security.v1.services.DeleteResourcesRequest;
 import org.ebayopensource.turmeric.security.v1.services.DeleteResourcesResponse;
 import org.ebayopensource.turmeric.security.v1.services.DeleteSubjectGroupsRequest;
 import org.ebayopensource.turmeric.security.v1.services.DeleteSubjectGroupsResponse;
 import org.ebayopensource.turmeric.security.v1.services.DeleteSubjectsRequest;
 import org.ebayopensource.turmeric.security.v1.services.DeleteSubjectsResponse;
 import org.ebayopensource.turmeric.security.v1.services.DisablePolicyRequest;
 import org.ebayopensource.turmeric.security.v1.services.DisablePolicyResponse;
 import org.ebayopensource.turmeric.security.v1.services.EffectType;
 import org.ebayopensource.turmeric.security.v1.services.EnablePolicyRequest;
 import org.ebayopensource.turmeric.security.v1.services.EnablePolicyResponse;
 import org.ebayopensource.turmeric.security.v1.services.Expression;
 import org.ebayopensource.turmeric.security.v1.services.FindExternalSubjectsRequest;
 import org.ebayopensource.turmeric.security.v1.services.FindExternalSubjectsResponse;
 import org.ebayopensource.turmeric.security.v1.services.FindPoliciesRequest;
 import org.ebayopensource.turmeric.security.v1.services.FindPoliciesResponse;
 import org.ebayopensource.turmeric.security.v1.services.FindSubjectGroupsRequest;
 import org.ebayopensource.turmeric.security.v1.services.FindSubjectGroupsResponse;
 import org.ebayopensource.turmeric.security.v1.services.FindSubjectsRequest;
 import org.ebayopensource.turmeric.security.v1.services.FindSubjectsResponse;
 import org.ebayopensource.turmeric.security.v1.services.GetAuthenticationPolicyRequest;
 import org.ebayopensource.turmeric.security.v1.services.GetAuthenticationPolicyResponse;
 import org.ebayopensource.turmeric.security.v1.services.GetEntityHistoryRequest;
 import org.ebayopensource.turmeric.security.v1.services.GetEntityHistoryResponse;
 import org.ebayopensource.turmeric.security.v1.services.GetMetaDataRequest;
 import org.ebayopensource.turmeric.security.v1.services.GetMetaDataResponse;
 import org.ebayopensource.turmeric.security.v1.services.GetOperationsRequest;
 import org.ebayopensource.turmeric.security.v1.services.GetOperationsResponse;
 import org.ebayopensource.turmeric.security.v1.services.GetResourcesRequest;
 import org.ebayopensource.turmeric.security.v1.services.GetResourcesResponse;
 import org.ebayopensource.turmeric.security.v1.services.KeyValuePair;
 import org.ebayopensource.turmeric.security.v1.services.Operation;
 import org.ebayopensource.turmeric.security.v1.services.OperationKey;
 import org.ebayopensource.turmeric.security.v1.services.Policy;
 import org.ebayopensource.turmeric.security.v1.services.PolicyKey;
 import org.ebayopensource.turmeric.security.v1.services.PolicyOutputSelector;
 import org.ebayopensource.turmeric.security.v1.services.PrimitiveValue;
 import org.ebayopensource.turmeric.security.v1.services.Query;
 import org.ebayopensource.turmeric.security.v1.services.QueryCondition;
 import org.ebayopensource.turmeric.security.v1.services.Resolution;
 import org.ebayopensource.turmeric.security.v1.services.Resource;
 import org.ebayopensource.turmeric.security.v1.services.ResourceKey;
 import org.ebayopensource.turmeric.security.v1.services.Resources;
 import org.ebayopensource.turmeric.security.v1.services.Rule;
 import org.ebayopensource.turmeric.security.v1.services.Subject;
 import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
 import org.ebayopensource.turmeric.security.v1.services.SubjectGroupKey;
 import org.ebayopensource.turmeric.security.v1.services.SubjectGroupQuery;
 import org.ebayopensource.turmeric.security.v1.services.SubjectKey;
 import org.ebayopensource.turmeric.security.v1.services.SubjectQuery;
 import org.ebayopensource.turmeric.security.v1.services.Subjects;
 import org.ebayopensource.turmeric.security.v1.services.Target;
 import org.ebayopensource.turmeric.security.v1.services.UpdateMode;
 import org.ebayopensource.turmeric.security.v1.services.UpdatePolicyRequest;
 import org.ebayopensource.turmeric.security.v1.services.UpdatePolicyResponse;
 import org.ebayopensource.turmeric.security.v1.services.UpdateResourcesRequest;
 import org.ebayopensource.turmeric.security.v1.services.UpdateResourcesResponse;
 import org.ebayopensource.turmeric.security.v1.services.UpdateSubjectGroupsRequest;
 import org.ebayopensource.turmeric.security.v1.services.UpdateSubjectGroupsResponse;
 import org.ebayopensource.turmeric.security.v1.services.ValidatePolicyRequest;
 import org.ebayopensource.turmeric.security.v1.services.ValidatePolicyResponse;
 import org.ebayopensource.turmeric.services.policyservice.intf.gen.BasePolicyServiceConsumer;
 /**
  * Utility class to "ease" some Policy Service operations as required by various
  * tests.
  * 
  */
 public class PolicyServiceTestHelper {
 
 	public static final int TIMEOUT_MSECS = 3 * 60 * 1000; // 3 minutes
 
 	private static PolicyServiceTestHelper LOCAL_INSTANCE;
 	private static PolicyServiceTestHelper REMOTE_INSTANCE;
 	private static String SECURITY_TOKEN; 
 	private static String SECURITY_TOKEN_HEADER;
 
 	/**
 	 * Returns the default instance - the remote one.
 	 * 
 	 * @return the remote instance.
 	 */
 	public static PolicyServiceTestHelper getInstance() {
 		return getInstance(false);
 	}
 
 	public static PolicyServiceTestHelper getInstance(boolean remote) {
 		return remote ? getRemoteInstance() : getLocalInstance();
 	}
 
 	private static synchronized PolicyServiceTestHelper getRemoteInstance() {
 		if (REMOTE_INSTANCE == null) {
 			try {
 				REMOTE_INSTANCE = new PolicyServiceTestHelper(true);
 			} catch (ServiceException e) {
 				throw new ExceptionInInitializerError(e);
 			} catch (MalformedURLException e) {
 				throw new ExceptionInInitializerError(e);
 			} catch (Exception e) {
 				throw new ExceptionInInitializerError(e);
 			}
 		}
 
 		return REMOTE_INSTANCE;
 	}
 
 	private static synchronized  PolicyServiceTestHelper getLocalInstance() {
 		if (LOCAL_INSTANCE == null) {
 			try {
 				LOCAL_INSTANCE = new PolicyServiceTestHelper(false);
 			} catch (ServiceException e) {
 				throw new ExceptionInInitializerError(e);
 			} catch (MalformedURLException e) {
 				throw new ExceptionInInitializerError(e);
 			} catch (Exception e) {
 				throw new ExceptionInInitializerError(e);
 			}
 		}
 
 		return LOCAL_INSTANCE;
 	}
 
 	private final BasePolicyServiceConsumer m_policyService = new BasePolicyServiceConsumer();
 
 	/**
 	 * Private constructor to prevent instantiation.
 	 */
 	private PolicyServiceTestHelper(boolean remote) throws ServiceException,
 			MalformedURLException, Exception {
 		SecurityTokenUtility util = TestTokenRetrivalObject.getSecurityTokenRetrival();
 		if (util == null) {
 			throw new NullPointerException("Could not retrieve security token");
 		}
 
 		if (!remote) {
 			m_policyService.getService().getInvokerOptions().setTransportName(
 					"LOCAL");
 		} else {
 			m_policyService.getService().setServiceLocation(
 					new URL("http://localhost:8080/ws/spf"));
 			m_policyService.getService().getInvokerOptions().setTransportName(
 					"HTTP11");
 			// m_policyService.getService().getInvokerOptions().setREST(true);
 			// // uncomment if you wanna REST :)
 		}
 		
 		SECURITY_TOKEN = SECURITY_TOKEN == null ? util.getSecurityToken() : null; 
 				//TokenProviderHelper.getSecurityToken();
 		SECURITY_TOKEN_HEADER= SECURITY_TOKEN_HEADER ==null ?  util.getSecurityTokenHeader() : null;
 				//TokenProviderHelper.getSecurityTokenHeader();
 		
 		if (SECURITY_TOKEN == null || SECURITY_TOKEN.isEmpty())
 			throw new Exception(" Attention! Security Token is EMPTY ");
 
 		if (SECURITY_TOKEN_HEADER == null || SECURITY_TOKEN_HEADER.isEmpty())
 			throw new Exception(" Attention! Security Token Header is EMPTY ");
 
 		m_policyService.getService().setSessionTransportHeader(
 				SECURITY_TOKEN_HEADER, SECURITY_TOKEN);
 
 	}
 	/**
 	 * Helper method to create internal or external subjects 
 	 */
 	public List<Long> createSubjects(String subjectType,
 			Long externalSubjectId, String... subjectNames) {
 		CreateSubjectsRequest createSubjectRequest = new CreateSubjectsRequest();
 		List<Subject> subjects = createSubjectRequest.getSubjects();
 		CreateSubjectsResponse createSubjectResponse = new CreateSubjectsResponse();
 
 		for (String subjectName : subjectNames) {
 			Subject subject = new Subject();
 			subject.setSubjectName(subjectName);
 			subject.setSubjectType(subjectType);
 			if (externalSubjectId != null)
 				subject.setExternalSubjectId(externalSubjectId);
 			subjects.add(subject);
 		}
 
 		try {
 			createSubjectResponse = this
 					.<CreateSubjectsRequest, CreateSubjectsResponse> invokePolicyService(
 							"createSubjects", createSubjectRequest);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 		String errorMessage = null;
 		if (createSubjectResponse.getErrorMessage() != null) {
 			errorMessage = createSubjectResponse.getErrorMessage().getError()
 					.get(0).getMessage();
 		}
 		Assert.assertEquals("Error returned upon creating subjects "
 				+ Arrays.asList(subjectNames) + ": " + errorMessage, "success",
 				createSubjectResponse.getAck().toString().toLowerCase());
 		Assert.assertNull("Error returned upon creating subjects "
 				+ Arrays.asList(subjectNames) + ": "
 				+ createSubjectResponse.getErrorMessage(),
 				createSubjectResponse.getErrorMessage());
 
 		final List<Long> subjectIds = createSubjectResponse.getSubjectIds();
 		Assert.assertEquals(
 				"Wrong size of returned ID list upon creating subjects "
 						+ Arrays.asList(subjectNames), subjectNames.length,
 				subjectIds.size());
 
 		return subjectIds;
 
 	}
 	/**
 	 * Helper method to create multiple internal subjects of same type 
 	 */
 	public List<Long> createSubjects(String subjectType, String... subjectNames) {
 		return createSubjects(subjectType, null, subjectNames);
 	}
 	
 	public CreateSubjectsResponse createSubjects(
 			CreateSubjectsRequest createSubjectsRequest) {
 		CreateSubjectsResponse createSubjectResponse = new CreateSubjectsResponse();
 		try {
 			createSubjectResponse = this
 					.<CreateSubjectsRequest, CreateSubjectsResponse> invokePolicyService(
 							"createSubjects", createSubjectsRequest);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 		return createSubjectResponse;
 	}
 	
 	private Long createExtenalSubjectReference(Subject subject) {
 
 		return createExtenalSubjectReference(subject.getSubjectName(), subject
 				.getSubjectType(), subject.getExternalSubjectId());
 	}
 	/**
 	 * Helper method to create external subjects 
 	 */
 	public Long createExtenalSubjectReference(String subjectName,
 			String subjectType, Long externalSubjectId) {
 
 		CreateExternalSubjectReferenceRequest createExternalSubjectReferenceRequest = new CreateExternalSubjectReferenceRequest();
 		CreateExternalSubjectReferencesResponse createExternalSubjectReferencesResponse = new CreateExternalSubjectReferencesResponse();
 		List<Subject> subjects = createExternalSubjectReferenceRequest
 				.getSubject();
 		Subject subject = new Subject();
 		subject.setSubjectName(subjectName);
 		subject.setSubjectType(subjectType);
 		subject.setExternalSubjectId(externalSubjectId);
 		subject.setDescription("Test");
 		subjects.add(subject);
 		try {
 			createExternalSubjectReferencesResponse = this
 					.<CreateExternalSubjectReferenceRequest, CreateExternalSubjectReferencesResponse> invokePolicyService(
 							"createExternalSubjectReferences",
 							createExternalSubjectReferenceRequest);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 		createExternalSubjectReferencesResponse.getAck().toString()
 				.equalsIgnoreCase("Success");
 
 		Long subjectId = createExternalSubjectReferencesResponse
 				.getSubjectIds() != null
 				&& createExternalSubjectReferencesResponse.getSubjectIds()
 						.size() > 0 ? createExternalSubjectReferencesResponse
 				.getSubjectIds().get(0) : null;
 
 		return subjectId;
 
 	}
 	/**
 	 * Helper method to create subjectgroup and assign existing subjects to it. 
 	 */
 	public List<Long> createSubjectGroups(String subjectGroupName,
 			String subjectGroupType, String... subjectNames) {
 
 		SubjectGroup subjectGroup = new SubjectGroup();
 		Subject subject = new Subject();
 		subjectGroup.setSubjectGroupName(subjectGroupName);
 		subjectGroup.setSubjectType(subjectGroupType);
 		subjectGroup.setDescription("Test Subject Group " + subjectGroupName);
 
 		for (String subjName : subjectNames) {
 			subject = new Subject();
 			subject.setSubjectName(subjName);
 			subject.setSubjectType(subjectGroupType);
 			subjectGroup.getSubject().add(subject);
 		}
 		return createSubjectGroups(subjectGroup);
 	}
 
 	/**
 	 * Helper method to create multiple subjectgroups of same type. 
 	 */
 	public List<Long> createSubjectGroups(SubjectGroup... subjectGroups) {
 		CreateSubjectGroupsRequest createSubjectGroupsRequest = new CreateSubjectGroupsRequest();
 		createSubjectGroupsRequest.getSubjectGroups().addAll(
 				Arrays.asList(subjectGroups));
 		CreateSubjectGroupsResponse createSubjectGroupsResponse = new CreateSubjectGroupsResponse();
 		try {
 			createSubjectGroupsResponse = this
 					.<CreateSubjectGroupsRequest, CreateSubjectGroupsResponse> invokePolicyService(
 							"createSubjectGroups", createSubjectGroupsRequest);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 		String errorMessage = null;
 		if (createSubjectGroupsResponse.getErrorMessage() != null) {
 			errorMessage = createSubjectGroupsResponse.getErrorMessage()
 					.getError().get(0).getMessage();
 		}
 		Assert.assertEquals("Error returned upon creating subject groups: "
 				+ Arrays.asList(subjectGroups) + ": " + errorMessage + ": ",
 				"success", createSubjectGroupsResponse.getAck().toString()
 						.toLowerCase());
 		Assert.assertNull("Error returned upon creating subject groups "
 				+ Arrays.asList(subjectGroups) + ": "
 				+ createSubjectGroupsResponse.getErrorMessage(),
 				createSubjectGroupsResponse.getErrorMessage());
 		final List<Long> subjectGroupIds = createSubjectGroupsResponse
 				.getSubjectGroupIds();
 		Assert.assertEquals(
 				"Wrong size of returned ID list upon creating subject groups "
 						+ Arrays.asList(subjectGroups), subjectGroups.length,
 				subjectGroupIds.size());
 
 		return subjectGroupIds;
 	}
 	
 	public CreateSubjectGroupsResponse createSubjectGroups(
 			CreateSubjectGroupsRequest createSubjectGroupsRequest) {
 		CreateSubjectGroupsResponse createSubjectGroupsResponse = new CreateSubjectGroupsResponse();
 		 try {
 			 createSubjectGroupsResponse = this
 				.<CreateSubjectGroupsRequest, CreateSubjectGroupsResponse> invokePolicyService(
 						"createSubjectGroups", createSubjectGroupsRequest);
 	} catch (InterruptedException e) {
 		throw new RuntimeException(e);
 	} catch (ServiceException e) {
 		throw new RuntimeException(e);
 	} catch (ExecutionException e) {
 		throw new RuntimeException(e);
 	}
 		return createSubjectGroupsResponse;
 	}
 	
 	@SuppressWarnings( { "unchecked", "unused" })
 	private <Req, Resp> Resp invokePolicyServiceWithWait(String method,
 			Req request) throws ServiceException, InterruptedException,
 			ExecutionException {
 		m_policyService.getService().createDispatch(method)
 				.invokeAsync(request);
 		List<Response<?>> responses = m_policyService.getService().poll(true,
 				false, TIMEOUT_MSECS);
 		Assert.assertEquals("Unexpected number of responses", 1, responses
 				.size());
 		Object object = responses.get(0);
 		AsyncResponse<Resp> asyncResponse = (AsyncResponse<Resp>) object;
 		return (Resp) asyncResponse.get();
 	}
 
 	@SuppressWarnings("unchecked")
 	private <Req, Resp> Resp invokePolicyService(String method, Req request)
 			throws ServiceException, InterruptedException, ExecutionException {
 		Object object = m_policyService.getService().createDispatch(method)
 				.invoke(request);
 		Resp response = (Resp) object;
 		return response;
 	}
 
 	public void cleanUpSubjects(String subjectType, String... subjectNames) {
 		DeleteSubjectsRequest deleteSubjs = new DeleteSubjectsRequest();
 		FindSubjectsRequest findSubjectsRequest = new FindSubjectsRequest();
 		FindSubjectsResponse findSubjectsResponse = new FindSubjectsResponse();
 		SubjectQuery subjectQuery = new SubjectQuery();
 		List<SubjectKey> subjectKeyList = subjectQuery.getSubjectKey();
 
 		for (String subjectName : subjectNames) {
 			SubjectKey subjectKey = new SubjectKey();
 			subjectKey.setSubjectName(subjectName);
 			subjectKey.setSubjectType(subjectType);
 			subjectKeyList.add(subjectKey);
 		}
 
 		findSubjectsRequest.setSubjectQuery(subjectQuery);
 
 		try {
 			findSubjectsResponse = this
 					.<FindSubjectsRequest, FindSubjectsResponse> invokePolicyService(
 							"findSubjects", findSubjectsRequest);
 
 		} catch (Exception e) {
 			System.err.println("Error: '" + e.getMessage()
 					+ "' encountered while finding subjects "
 					+ Arrays.asList(subjectNames));
 			e.printStackTrace();
 		}
 
 		if (findSubjectsResponse.getSubjects() != null
 				&& findSubjectsResponse.getSubjects().size() > 0) {
 			for (Subject subject : findSubjectsResponse.getSubjects()) {
 				final SubjectKey subjectKey = new SubjectKey();
 				subjectKey.setSubjectType(subject.getSubjectType());
 				subjectKey.setSubjectName(subject.getSubjectName());
 				deleteSubjs.getSubjectKey().add(subjectKey);
 			}
 			try {
 				DeleteSubjectsResponse deleteSubjectsResponse = this
 						.<DeleteSubjectsRequest, DeleteSubjectsResponse> invokePolicyService(
 								"deleteSubjects", deleteSubjs);
 
 				if (deleteSubjectsResponse.getErrorMessage() != null) {
 					throw new RuntimeException("Delete subjects failed "
 							+ deleteSubjectsResponse.getErrorMessage()
 									.getError().get(0).getMessage());
 				}
 			} catch (Exception e) {
 				System.err.println("Error: '" + e.getMessage()
 						+ "' encountered while cleaning up subjects "
 						+ Arrays.asList(subjectNames));
 				e.printStackTrace();
 			}
 		} else {
 			System.out.println("No subject of type " + subjectType + " among "
 					+ Arrays.asList(subjectNames)
 					+ " was found; nothing to clean");
 		}
 	}
 
 	public void deleteSubjects(String subjectType, String... subjectNames) {
 		cleanUpSubjects(subjectType, subjectNames);
 	}
 
 	public void cleanupPolicy(String policyName, String policyType) {
 		
 		FindPoliciesRequest findPoliciesRequest = new FindPoliciesRequest();
 		FindPoliciesResponse findPoliciesResponse = new FindPoliciesResponse();
 		PolicyKey policyKey = new PolicyKey();
 		policyKey.setPolicyName(policyName);
 		policyKey.setPolicyType(policyType);
 		Query query1 = new Query();
 		query1.setQueryType("ActivePoliciesOnly"); // Effect/SubjectSearchScope/MaskedIds/ActivePoliciesOnly
 		query1.setQueryValue("FALSE"); // BLOCK|FLAG|CHALLENGE|ALLOW/TARGET|EXCLUDED|BOTH/TRUE|FALSE/TRUE|FALSE
 
 		QueryCondition queryCondition = new QueryCondition();
 		queryCondition.getQuery().add(query1);
 
 		findPoliciesRequest.getPolicyKey().add(policyKey);
 		findPoliciesRequest.setQueryCondition(queryCondition);
 		try {
 			findPoliciesResponse = this
 					.<FindPoliciesRequest, FindPoliciesResponse> invokePolicyService(
 							"findPolicies", findPoliciesRequest);
 
 			if (findPoliciesResponse.getPolicySet() != null
 					&& findPoliciesResponse.getPolicySet().getPolicy().size() > 0) {
 				Policy policy = new Policy();
 				for (Policy tempPolicy: findPoliciesResponse.getPolicySet().getPolicy()) {
 					if (tempPolicy.getPolicyName().equals(policyName)) {
 						policy= tempPolicy;	
 						break;
 					}
 					
 				}
 				// extract resources of this policy
 				
 				/*if (policy!=null & policy.getTarget()!=null) {
 					Resources resources = policy.getTarget().getResources();
 					// cleanup policies where resources are assigned
 					cleanUpPolicies(resources);
 					// clean up resources
 					List<Resource> resourceList = resources!=null ? resources.getResource() : null;
 					for (Resource resource : resourceList) {
 						cleanUpResource(resource.getResourceName(), resource.getResourceType());
 					}
 				}*/
 				
 				// extract subjectgroups of this policy
 				if (policy!=null & policy.getTarget()!=null) {
 					Subjects subjects = policy.getTarget().getSubjects();
 					List<SubjectGroup> subjGrpList = new  ArrayList<SubjectGroup>();
 					// clean up policies where subjectgroups are assigned
 					if (subjects!=null) {
 						subjGrpList = subjects.getSubjectGroup();
 						cleanUpPoliciesSubjectGroupKey(subjGrpList);
 					}
 			
 					// if no subject groups found then delete this policy
 					
 					if (subjGrpList.size() < 1) {
 						deletePolicy(policy);
 					}
 					// clean up subject groups
 /*					List<SubjectGroup> subjGrpList = subjects!=null ? subejcts.getSubjectGroup() : null;
 					
 					for (SubjectGroup subjectGroup : subjGrpList) {
 						cleanupSubjectGroup(subjectGroup.getSubjectGroupName(), subjectGroup.getSubjectType());
 					}*/
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println(" While cleaning up policy = " + policyName
 					+ " Error :  " + e.getMessage());
 		}
 		
 	}
 	private void deletePolicy(Policy policy) {
 		DeletePolicyRequest req = new DeletePolicyRequest();
 		PolicyKey key = new PolicyKey();
 		
 		key.setPolicyName(policy.getPolicyName());
 		key.setPolicyType(policy.getPolicyType());
 		key.setPolicyId(policy.getPolicyId());
 		req.setPolicyKey(key);
 		deletePolicy(req);
 	
 	}
 
 	private void cleanUpPoliciesSubjectGroupKey(List<SubjectGroup> subjGrpList) {
 		String policyTypes[] = {
 				"RL",
 				"AUTHZ",
 				"BLACKLIST", 
 				"WHITELIST"
 				};
 		SubjectGroupKey  key = new SubjectGroupKey();
 		PolicyKey policyKey =  new PolicyKey();
 		for (SubjectGroup sg : subjGrpList) {
 			key.setSubjectGroupName(sg.getSubjectGroupName());
 			key.setSubjectType(sg.getSubjectType());
 			for (String type : policyTypes) {
 				policyKey = new PolicyKey();
 				policyKey.setPolicyType(type);
 				FindPoliciesResponse res = findPolicy(key, policyKey);
 				deletePolicy(res);
 			}
 		}
 	}	
 	
 	private void cleanUpPolicies(Resources resList) {
 		String policyTypes[] = {
 				"RL",
 				"AUTHZ",
 				"BLACKLIST", 
 				"WHITELIST"
 				};
 		PolicyKey policyKey =  new PolicyKey();
 		ResourceKey  key = new ResourceKey();
 		if (resList!=null) {
 			List<Resource> resourceList = resList.getResource();
 			for(Resource res : resourceList) {
 				key.setResourceName(res.getResourceName());
 				key.setResourceType(res.getResourceType());
 				for( String type : policyTypes){
 					 policyKey = new PolicyKey();
 					 policyKey.setPolicyType(type);
 					 FindPoliciesResponse response = findPolicy(key,policyKey);
 					 deletePolicy(response);
 				}
 			}
 		}
 	}
 	
 	private void deletePolicy(FindPoliciesResponse res) {
 		
 		DeletePolicyRequest req = new DeletePolicyRequest();
 		PolicyKey key = new PolicyKey();
 		if (res!=null && res.getPolicySet()!=null && res.getPolicySet().getPolicy()!=null) {
 			List<Policy> policyList =  res.getPolicySet().getPolicy();
 			for (Policy policy:policyList ) {
 				key = new PolicyKey();
 				key.setPolicyName(policy.getPolicyName());
 				key.setPolicyType(policy.getPolicyType());
 				key.setPolicyId(policy.getPolicyId());
 				req.setPolicyKey(key);
 				deletePolicy(req);
 			}
 		}
 	}
 	
 
 	public DeletePolicyResponse deletePolicy(
 			DeletePolicyRequest deletePolicyRequest) {
 		DeletePolicyResponse deletePolicyResponse = new DeletePolicyResponse(); 
 		
 		try {
 			deletePolicyResponse = this
 				.<DeletePolicyRequest, DeletePolicyResponse> invokePolicyService(
 						"deletePolicy", deletePolicyRequest);
 			 		
 		} catch (Exception e) {
 			throw new RuntimeException("Delete policy failed "
 					+ e.getMessage());
 		}
 		return deletePolicyResponse;
 	}
 			
 	public void cleanupSubjectGroup(String subjectGroupName, String subjectType) {
 
 		DeleteSubjectGroupsRequest deleteSubjectGroupsRequest = new DeleteSubjectGroupsRequest();
 		DeleteSubjectGroupsResponse deleteSubjectGroupsResponse = new DeleteSubjectGroupsResponse();
 		FindSubjectGroupsRequest findSubjectGroupsRequest = new FindSubjectGroupsRequest();
 		FindSubjectGroupsResponse findSubjectGroupsResponse = new FindSubjectGroupsResponse();
 
 		SubjectGroupQuery subjectGroupQuery = new SubjectGroupQuery();
 		List<SubjectGroupKey> subjectGroupKeyList = subjectGroupQuery
 				.getSubjectGroupKey();
 		SubjectGroupKey subjectGroupKey = new SubjectGroupKey();
 		subjectGroupKey.setSubjectGroupName(subjectGroupName);
 		subjectGroupKey.setSubjectType(subjectType);
 		subjectGroupKeyList.add(subjectGroupKey);
 		findSubjectGroupsRequest.setSubjectGroupQuery(subjectGroupQuery);
 
 		try {
 			findSubjectGroupsResponse = this
 					.<FindSubjectGroupsRequest, FindSubjectGroupsResponse> invokePolicyService(
 							"findSubjectGroups", findSubjectGroupsRequest);
 			if (findSubjectGroupsResponse.getSubjectGroups() != null
 					&& findSubjectGroupsResponse.getSubjectGroups().size() > 0) {
 				
 				// delete policies if any
 				  
 				
 				subjectGroupKeyList = deleteSubjectGroupsRequest
 						.getSubjectGroupKey();
 				subjectGroupKeyList.add(subjectGroupKey);
 				deleteSubjectGroupsResponse = this
 						.<DeleteSubjectGroupsRequest, DeleteSubjectGroupsResponse> invokePolicyService(
 								"deleteSubjectGroups",
 								deleteSubjectGroupsRequest);
 
 				System.out.println("Deleting SubjectGroup " + subjectGroupName
 						+ "  status = "
 						+ deleteSubjectGroupsResponse.getAck().toString());
 			} else {
 				System.out.println("No subject Group of type " + subjectType + " among "
 						+ subjectGroupName
 						+ " was found; nothing to clean");
 			}
 
 		} catch (Exception e) {
 			System.out
 					.println("Error "
 							+ e
 							+ (e.getMessage() == null ? "" : "("
 									+ e.getMessage() + ")")
 							+ " encountered while cleaning up subjectGroupName = "
 							+ subjectGroupName);
 			e.printStackTrace();
 		}
 	}
 
 	public void deleteSubjectGroups(String subjectType,
 			String... subjectGroupNames) {
 		DeleteSubjectGroupsRequest deleteSubjectGroupsRequest = new DeleteSubjectGroupsRequest();
 		DeleteSubjectGroupsResponse deleteSubjectGroupsResponse = new DeleteSubjectGroupsResponse();
 		List<SubjectGroupKey> subjectGroupKeyList = deleteSubjectGroupsRequest
 				.getSubjectGroupKey();
 
 		for (String subjectGroupName : subjectGroupNames) {
 			SubjectGroupKey subjectGroupKey = new SubjectGroupKey();
 			subjectGroupKey.setSubjectGroupName(subjectGroupName);
 			subjectGroupKey.setSubjectType(subjectType);
 			subjectGroupKeyList.add(subjectGroupKey);
 		}
 
 		try {
 			deleteSubjectGroupsResponse = this
 					.<DeleteSubjectGroupsRequest, DeleteSubjectGroupsResponse> invokePolicyService(
 							"deleteSubjectGroups", deleteSubjectGroupsRequest);
 			if (deleteSubjectGroupsResponse.getErrorMessage() != null) {
 				throw new RuntimeException("Delete subjectgroups failed "
 						+ deleteSubjectGroupsResponse.getErrorMessage()
 								.getError().get(0).getMessage());
 			}
 		} catch (Exception e) {
 			System.out
 					.println("Error "
 							+ e
 							+ (e.getMessage() == null ? "" : "("
 									+ e.getMessage() + ")")
 							+ " encountered while cleaning up subjectGroupName = "
 							+ Arrays.asList(subjectGroupNames));
 			e.printStackTrace();
 		}
 	}
 
 	public void cleanUpResource(String resourceName, String resourceType) {
 
 		DeleteResourcesRequest deleteResourcesRequest = new DeleteResourcesRequest();
 		GetResourcesRequest getResourcesRequest = new GetResourcesRequest();
 		GetResourcesResponse getResourcesResponse = new GetResourcesResponse();
 		ResourceKey resourceKey = new ResourceKey();
 		resourceKey.setResourceName(resourceName);
 		resourceKey.setResourceType(resourceType);
 		getResourcesRequest.getResourceKey().add(resourceKey);
 
 		try {
 			getResourcesResponse = this
 					.<GetResourcesRequest, GetResourcesResponse> invokePolicyService(
 							"getResources", getResourcesRequest);
 		} catch (Exception e) {
 			System.err.println("Error: '" + e.getMessage()
 					+ "' encountered while getting resources " + resourceName);
 			e.printStackTrace();
 		}
 		if (getResourcesResponse.getResources() != null
 				&& getResourcesResponse.getResources().size() > 0) {
 
 			try {
 				deleteResourcesRequest.getResourceKey().add(resourceKey);
 				DeleteResourcesResponse deleteResourcesResponse = this
 						.<DeleteResourcesRequest, DeleteResourcesResponse> invokePolicyService(
 								"deleteResources", deleteResourcesRequest);
 				if (deleteResourcesResponse.getErrorMessage() != null) {
 					throw new RuntimeException("Delete Resource failed "
 							+ deleteResourcesResponse.getErrorMessage()
 									.getError().get(0).getMessage());
 				}
 			} catch (Exception e) {
 				System.err.println("Error: '" + e.getMessage()
 						+ "' encountered while cleaning up resource "
 						+ resourceName);
 				e.printStackTrace();
 			}
 		} else {
 			System.out.println("No resource named " + resourceName + "of type "
 					+ resourceType + " was found; nothing to clean");
 		}
 	}
 
 	public Subject getExternalSubject(String name, String type) {
 		FindExternalSubjectsRequest request = new FindExternalSubjectsRequest();
 		Subject subject = null;
 		SubjectQuery subjectQuery = new SubjectQuery();
 		request.setSubjectQuery(subjectQuery);
 		List<SubjectKey> subjectKeyList = subjectQuery.getSubjectKey();
 		SubjectKey subkey = new SubjectKey();
 		subkey.setSubjectType(type);
 		subkey.setSubjectName(name);
 		subjectKeyList.add(subkey);
 		try {
 			FindExternalSubjectsResponse response = new FindExternalSubjectsResponse();
 			response = this
 					.<FindExternalSubjectsRequest, FindExternalSubjectsResponse> invokePolicyService(
 							"findExternalSubjects", request);
 			List<Subject> list = response.getSubjects();
 			subject = (list != null && list.size() > 0) ? list.get(0) : null;
 		} catch (Exception e) {
 			System.err.println("Error: '" + e.getMessage()
 					+ "' encountered while getting external subjectid for "
 					+ name);
 			e.printStackTrace();
 		}
 		return subject;
 	}
 
 	public Long getOperationIdByName(String operationName, String resourceName) {
 		GetOperationsRequest getOperationsRequest = new GetOperationsRequest();
 		List<OperationKey> operationKeys = getOperationsRequest
 				.getOperationKey();
 		OperationKey operationKey = new OperationKey();
 		operationKey.setOperationName(operationName);
 		operationKey.setResourceName(resourceName);
 		operationKey.setResourceType("SERVICE");
 		operationKeys.add(operationKey);
 
 		GetOperationsResponse getOperationsResponse = new GetOperationsResponse();
 		try {
 			getOperationsResponse = this
 					.<GetOperationsRequest, GetOperationsResponse> invokePolicyService(
 							"getOperations", getOperationsRequest);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 
 		Assert.assertEquals(AckValue.SUCCESS,getOperationsResponse.getAck());
 		Assert.assertNull(getOperationsResponse.getErrorMessage());
 		Assert.assertTrue(getOperationsResponse.getOperations().size() == 1);
 		return getOperationsResponse.getOperations().get(0).getOperationId();
 	}
 
 	public Long getResourceIdByName(String resourceName) {
 		GetResourcesRequest getResourcesRequest = new GetResourcesRequest();
 		GetResourcesResponse getResourcesResponse = new GetResourcesResponse();
 		List<ResourceKey> resourceKeyList = getResourcesRequest
 				.getResourceKey();
 		ResourceKey resourceKey = new ResourceKey();
 		resourceKey.setResourceName(resourceName);
 		resourceKey.setResourceType("SERVICE");
 		resourceKeyList.add(resourceKey);
 		try {
 			getResourcesResponse = this
 					.<GetResourcesRequest, GetResourcesResponse> invokePolicyService(
 							"getResources", getResourcesRequest);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 		Assert.assertTrue(getResourcesResponse.getAck().toString()
 				.equalsIgnoreCase("Success"));
 		Assert.assertTrue(getResourcesResponse.getErrorMessage() == null);
 		Assert.assertTrue(getResourcesResponse.getResources().size() == 1);
 		return getResourcesResponse.getResources().get(0).getResourceId();
 	}
 
 	public Subject mapIncludedSubject(	Long subjectId) {
 
 		Subject subject = new Subject();
 		SubjectMatchType subjectMatchType = new SubjectMatchType();
 		subjectMatchType
 				.setMatchId("urn:oasis:names:tc:xacml:1.0:function:integer-equal");
 		AttributeValueType attributeValueType = new AttributeValueType();
 		attributeValueType
 				.setDataType("http://www.w3.org/2001/XMLSchema#integer");
 		attributeValueType.getContent().add(subjectId.toString()); // id is used
 		// as-is
 		subjectMatchType.setAttributeValue(attributeValueType);
 		SubjectAttributeDesignatorType subjectAttributeDesignatorType = new SubjectAttributeDesignatorType();
 		subjectAttributeDesignatorType
 				.setDataType("http://www.w3.org/2001/XMLSchema#integer");
 		subjectAttributeDesignatorType
 				.setAttributeId("urn:oasis:names:tc:xacml:1.0:subject:subject-id");
 		subjectMatchType
 				.setSubjectAttributeDesignator(subjectAttributeDesignatorType);
 		subject.getSubjectMatch().add(subjectMatchType);
 
 		return subject;
 	}
 
 	protected Subject mapGlobalSubject(String domain) {
 
 		Subject s = new Subject();
 		SubjectMatchType subjectMatchType = new SubjectMatchType();
 		subjectMatchType
 				.setMatchId("urn:oasis:names:tc:xacml:1.0:function:string-regexp-match");
 		AttributeValueType attributeValueType = new AttributeValueType();
 		attributeValueType
 				.setDataType("http://www.w3.org/2001/XMLSchema#string");
 		attributeValueType.getContent().add("[0-9]+");
 		subjectMatchType.setAttributeValue(attributeValueType);
 
 		SubjectAttributeDesignatorType subjectAttributeDesignatorType = new SubjectAttributeDesignatorType();
 		subjectAttributeDesignatorType
 				.setDataType("http://www.w3.org/2001/XMLSchema#string");
 		subjectAttributeDesignatorType
 				.setAttributeId("urn:oasis:names:tc:xacml:1.0:subject:subject-id");
 		subjectMatchType
 				.setSubjectAttributeDesignator(subjectAttributeDesignatorType);
 		s.getSubjectMatch().add(subjectMatchType);
 		s.setSubjectName("All " + domain);
 
 		s.setSubjectType(domain);
 		if (s.getSubjectType() == null)
 			throw new IllegalArgumentException("Invalid subject domain "
 					+ domain);
 		return s;
 	}
 
 	public SubjectGroup mapIncludedSubjectGroup(Long sgId) {
 
 		SubjectGroup subjectGroup = new SubjectGroup();
 		SubjectMatchType subjectMatchType = new SubjectMatchType();
 		subjectMatchType
 				.setMatchId("urn:oasis:names:tc:xacml:1.0:function:integer-equal");
 		AttributeValueType attributeValueType = new AttributeValueType();
 		attributeValueType
 				.setDataType("http://www.w3.org/2001/XMLSchema#integer");
 		attributeValueType.getContent().add(sgId.toString());
 		subjectMatchType.setAttributeValue(attributeValueType);
 
 		SubjectAttributeDesignatorType subjectAttributeDesignatorType = new SubjectAttributeDesignatorType();
 		subjectAttributeDesignatorType
 				.setDataType("http://www.w3.org/2001/XMLSchema#integer");
 		subjectAttributeDesignatorType
 				.setAttributeId("urn:oasis:names:tc:xacml:1.0:subject:subject-id");
 		subjectMatchType
 				.setSubjectAttributeDesignator(subjectAttributeDesignatorType);
 
 		subjectGroup.setSubjectMatch(subjectMatchType);
 		subjectGroup.setApplyToAll(true);
 
 		return subjectGroup;
 	}
 
 	public Subject mapExcludedSubject(Long subjectId) {
 		Subject s = new Subject();
 		SubjectMatchType subjectMatchType = new SubjectMatchType();
 		subjectMatchType
 				.setMatchId("urn:oasis:names:tc:xacml:1.0:function:string-regexp-match");
 		AttributeValueType attributeValueType = new AttributeValueType();
 		attributeValueType
 				.setDataType("http://www.w3.org/2001/XMLSchema#string");
 		attributeValueType.getContent().add("(?!" + subjectId + ")");
 		subjectMatchType.setAttributeValue(attributeValueType);
 
 		SubjectAttributeDesignatorType subjectAttributeDesignatorType = new SubjectAttributeDesignatorType();
 		subjectAttributeDesignatorType
 				.setDataType("http://www.w3.org/2001/XMLSchema#string");
 		subjectAttributeDesignatorType
 				.setAttributeId("urn:oasis:names:tc:xacml:1.0:subject:subject-id");
 		subjectMatchType
 				.setSubjectAttributeDesignator(subjectAttributeDesignatorType);
 		s.getSubjectMatch().add(subjectMatchType);
 
 		return s;
 	}
 
 	public SubjectGroup mapExcludedSubjectGroup(Long sgId) {
 
 		SubjectGroup sg = new SubjectGroup();
 		SubjectMatchType subjectMatchType = new SubjectMatchType();
 		subjectMatchType
 				.setMatchId("urn:oasis:names:tc:xacml:1.0:function:string-regexp-match");
 		AttributeValueType attributeValueType = new AttributeValueType();
 		attributeValueType
 				.setDataType("http://www.w3.org/2001/XMLSchema#string");
 		attributeValueType.getContent().add("(?!" + sgId + ")");
 		subjectMatchType.setAttributeValue(attributeValueType);
 		SubjectAttributeDesignatorType subjectAttributeDesignatorType = new SubjectAttributeDesignatorType();
 		subjectAttributeDesignatorType
 				.setDataType("http://www.w3.org/2001/XMLSchema#string");
 		subjectAttributeDesignatorType
 				.setAttributeId("urn:oasis:names:tc:xacml:1.0:subject:subject-id");
 		subjectMatchType
 				.setSubjectAttributeDesignator(subjectAttributeDesignatorType);
 		sg.setApplyToEach(true);
 		sg.setSubjectMatch(subjectMatchType);
 		return sg;
 	}
 
 	public BasePolicyServiceConsumer getPolicyService() {
 		return m_policyService;
 	}
 
 	public void updatePolicy(String policyName, String policyType,
 			String excluseSubject, String includeSubject, String updateMode) {
 
 	}
 	public UpdatePolicyResponse updatePolicy(
 			UpdatePolicyRequest updatePolicyRequest) {
 		UpdatePolicyResponse updatePolicyResponse = new UpdatePolicyResponse();
 		try {
 			updatePolicyResponse = this
 					.<UpdatePolicyRequest, UpdatePolicyResponse> invokePolicyService(
 							"updatePolicy", updatePolicyRequest);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 		return updatePolicyResponse;
 	}
 	
 	
 	public CreatePolicyResponse createPolicy(
 			CreatePolicyRequest createPolicyRequest) {
 		CreatePolicyResponse createPolicyResponse = new CreatePolicyResponse();
 
 		try {
 			createPolicyResponse = this
 					.<CreatePolicyRequest, CreatePolicyResponse> invokePolicyService(
 							"createPolicy", createPolicyRequest);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 		return createPolicyResponse;
 	}
 	public Policy constructPolicy(String policyType, String policyName,
 			String policyDesc, List<String> resourceList,
 			List<String> globalSubjectDomainList,
 			List<String> subjectInclusion, List<String> subjectGInclusion,
 			List<String> subjectExclusion, List<String> subjectGExclusion,
 			long duration, long rolloverperiod, EffectType effect,
 			String condition, boolean isActive) throws Exception {
 		long policyId = -1;
 		Policy policy = null;
 		policyId = createPolicy(policyType, policyName,
 				policyDesc,resourceList,
 				globalSubjectDomainList,
 				subjectInclusion,  subjectGInclusion,
 				subjectExclusion, subjectGExclusion,
 				duration, rolloverperiod,  effect,
 				condition, isActive);
 		
 		if (policyId!=-1) {
 			PolicyKey policyKey = new PolicyKey();
 			policyKey.setPolicyId(policyId);
 			policyKey.setPolicyType(policyType);
 			policyKey.setPolicyName(policyName);
 			FindPoliciesResponse response = findPolicy(policyKey);
 			policy = response.getAck().equals(AckValue.SUCCESS)
 					&& response.getPolicySet() != null ? response
 					.getPolicySet().getPolicy().get(0) : null;
 		}
 			return policy;
 	}
 	
 	
 	public  CreatePolicyRequest constructPolicyRequest(String policyType, String policyName,
 			String policyDesc, List<String> resourceMap,
 			List<String> globalSubjectDomainList,
 			List<String> subjectInclusion, List<String> subjectGInclusion,
 			List<String> subjectExclusion, List<String> subjectGExclusion,
 			long effectDuration, long rolloverperiod, EffectType effect,
 			String condition, boolean isActive,String policyBasedEmail,String subjectBasedEmail) throws Exception {
 
 		CreatePolicyRequest createPolicyRequest = new CreatePolicyRequest();
 		Target target = new Target();
 		Policy policy = new Policy();
 		System.out.println(">> Create Policy Start =>" + policyName);
 		if (policyType == null || policyName == null) {
 			throw new Exception(
 					"PolicyType or PolicyName is missing in the request");
 		}
 		cleanupPolicy(policyName, policyType);
 
 		policy.setPolicyName(policyName);
 		policy.setPolicyType(policyType);
 		policy.setDescription(policyDesc);
 		policy.setTarget(target);
 		Resources resources = new Resources();
 		target.setResources(resources);
 
 		Subjects subjects = new Subjects();
 		target.setSubjects(subjects);
 
 		// target.Resource
 		List<Resource> resourceList = resources.getResource();
 		if (resourceMap != null) {
 			Iterator<String> iterResourceMap = resourceMap.iterator();
 			while (iterResourceMap.hasNext()) {
 				Resource resource = new Resource();
 				String entry = iterResourceMap.next();
 				String[] str = entry.split(":");
 				if (str.length > 2) {
 				    int operationIndex = 2;
 				    Operation op = null;
					while (str.length < operationIndex ) {
 						Long operationId = createOpertionForResource(str[0],str[1], str[operationIndex++]);
 						op = new Operation();
 						op.setOperationId(operationId);
 						resource.getOperation().add(op);
 					}
 				} else {
 					Long resourceId = createResource(str[0], str[1]);
 					resource.setResourceId(resourceId);
 				}
 				resource.setResourceType(str[0]);
 				resource.setResourceName(str[1]);
 				resourceList.add(resource);
 			}
 		}
 		prepareSubject(subjectInclusion, true, subjects);
 		prepareSubjectGroup(subjectGInclusion, true, subjects);
 
 		if (policyType.equals("RL")) {
 			if (globalSubjectDomainList != null) {
 				Iterator<String> iterDomainList = globalSubjectDomainList
 						.iterator();
 				while (iterDomainList.hasNext()) {
 					Subject object = mapGlobalSubject(iterDomainList.next());
 					subjects.getSubject().add(object);
 				}
 			}
 			if (subjectExclusion != null) {
 				prepareSubject(subjectExclusion, false, subjects);
 			}
 			if (subjectGExclusion != null) {
 				prepareSubjectGroup(subjectGExclusion, false, subjects);
 			}
 		}
 
 		if (policyType.equals("BLACKLIST"))
 			effect = EffectType.BLOCK;
 		else if (policyType.equals("WHITELIST"))
 			effect = EffectType.ALLOW;
 
 		if (!policyType.equals("AUTHZ")) {
 			Rule rule = getRuleObject(policyName, effectDuration, rolloverperiod,
 					effect, condition);
 			policy.getRule().add(rule);
 		}
 		
 		createPolicyRequest.setPolicy(policy);
 		
 		
 	return createPolicyRequest;
 		
 	}
 	
 	private  CreatePolicyResponse createPolicy(String policyType, String policyName,
 			String policyDesc, List<String> resourceMap,
 			List<String> globalSubjectDomainList,
 			List<String> subjectInclusion, List<String> subjectGInclusion,
 			List<String> subjectExclusion, List<String> subjectGExclusion,
 			long effectDuration, long rolloverperiod, EffectType effect,String condition,
 			boolean isActive,String policyBasedEmail,String subjectBasedEmail) throws Exception {
 					
 			CreatePolicyRequest createPolicyRequest = new CreatePolicyRequest();
 			createPolicyRequest = constructPolicyRequest(policyType, policyName,
 					policyDesc,resourceMap,
 					globalSubjectDomainList,
 					subjectInclusion,  subjectGInclusion,
 					subjectExclusion, subjectGExclusion,
 					effectDuration, rolloverperiod,  effect,condition,isActive,policyBasedEmail,subjectBasedEmail);
 			
 			CreatePolicyResponse createPolicyResponse = this
 					.<CreatePolicyRequest, CreatePolicyResponse> invokePolicyService(
 							"createPolicy", createPolicyRequest);
 		return createPolicyResponse;
 	}
 	
 	public long createPolicy(String policyType, String policyName,
 			String policyDesc, List<String> resourceMap,
 			List<String> globalSubjectDomainList,
 			List<String> subjectInclusion, List<String> subjectGInclusion,
 			List<String> subjectExclusion, List<String> subjectGExclusion,
 			long duration, long rolloverperiod, EffectType effect,
 			String condition, boolean isActive) throws Exception {
 		long policyId = 0;
 		
 		CreatePolicyResponse createPolicyResponse = createPolicy(policyType, policyName,
 				policyDesc,resourceMap,
 				globalSubjectDomainList,
 				subjectInclusion,  subjectGInclusion,
 				subjectExclusion, subjectGExclusion,
 				duration, rolloverperiod,  effect,
 				condition, isActive,null,null);
 		if (createPolicyResponse.getAck().equals(AckValue.SUCCESS)) {
 				policyId = createPolicyResponse.getPolicyId();
 				EnablePolicyRequest enablePolicyRequest = new EnablePolicyRequest();
 				PolicyKey policyKey = new PolicyKey();
 				policyKey.setPolicyId(policyId);
 				policyKey.setPolicyType(policyType);
 				enablePolicyRequest.setPolicyKey(policyKey);
 				EnablePolicyResponse res = enablePolicy(enablePolicyRequest);
 				if (res.getErrorMessage() != null) {
 					throw  new Exception(res.getErrorMessage().getError()
 							.get(0).getMessage());
 				}
 		} 
 			
 		System.out.println("--Create Policy End ========================>"
 				+ policyName);
 		return policyId;
 	}
 
 	private List<Subject> prepareSubject(List<String> subjectInclusion,
 			boolean isInclusion, Subjects subjects) throws Exception {
 		List<Subject> subjectList = subjects.getSubject();
 		if (subjectInclusion != null) {
 			Iterator<String> iterSubjectInclusion = subjectInclusion.iterator();
 			while (iterSubjectInclusion.hasNext()) {
 				String entry = iterSubjectInclusion.next();
 				String[] str = entry.split(":");
 				if (str.length < 2) {
 					throw new Exception(
 							" Subject name or Subject Type is missing in the request");
 				}
 				String subjectName = str[0];
 				String domain = str[1];
 				Long subjectId = getSubjectDetails(subjectName, domain);
 
 				Subject object;
 				if (isInclusion) {
 
 					object = mapIncludedSubject(subjectId);
 					object.setSubjectName(subjectName);
 					object.setSubjectType(domain);
 
 				} else
 					object = mapExcludedSubject(subjectId);
 				subjectList.add(object);
 			}
 		}
 		return subjectList;
 	}
 
 	public Map<String, Long> createSubjectGroup(List<String> subjectGroup)
 			throws Exception {
 
 		Map<String, Long> sgMap = new HashMap<String, Long>();
 
 		if (subjectGroup != null) {
 
 			for (String subjectG : subjectGroup) {
 				String[] keys = subjectG.split(":");
 				String name = keys[0];
 				String type = keys[1];
 				String desc = keys[2];
 				boolean calculated = false;
 				if (keys.length > 4 && keys[3].equals("1"))
 					calculated = true;
 
 				if (calculated && keys.length < 5) {
 					throw new Exception(
 							" Calculated Subject Group is missing in the request");
 				}
 
 				if (!(calculated) && keys.length < 5) {
 					throw new Exception(
 							" Subjecta List is missing in the request");
 				}
 
 				Long subjectGroupId = getSubjectGroupDetails(name, type, desc,
 						keys[4], calculated);
 				sgMap.put(name, subjectGroupId);
 			}
 
 		}
 		return sgMap;
 
 	}
 	
 	private List<SubjectGroup> prepareSubjectGroup(List<String> subjectG,
 			boolean isInclusion, Subjects subjects) throws Exception {
 		List<SubjectGroup> subjectGList = subjects.getSubjectGroup();
 		if (subjectG != null) {
 			for (String subjectGroup : subjectG) {
 				String[] keys = subjectGroup.split(":");
 
 				String name = keys.length > 1 ? keys[0] : null;
 				String type = keys.length > 2 ? keys[1] : null;
 				String desc = keys.length > 3 ? keys[2] : null;
 				String apply = keys.length > 4 ? keys[3] : null;
 				boolean calcualted = keys.length > 5 && keys[4].equals("1") ? true : false;
 
 				if (calcualted && keys.length < 6) {
 					throw new Exception(
 							" Calculated Subject Group is missing in the request");
 				}
 				String calcSGNameORSubjList = keys.length >= 6 ? keys[5] : null;
 				
 				Long subjectGroupId = getSubjectGroupDetails(name, type, desc,
 						calcSGNameORSubjList, calcualted);
 				SubjectGroup object = null;
 				if (isInclusion) {
 					object = mapIncludedSubjectGroup(subjectGroupId);
 					object.setSubjectType(type);
 					object.setSubjectGroupName(name);
 				} else
 					object = mapExcludedSubjectGroup(subjectGroupId);
 
 				if (apply.equals("applyEach"))
 					object.setApplyToEach(true);
 				else
 					object.setApplyToAll(true);
 
 				subjectGList.add(object);
 
 			}
 
 		}
 		return subjectGList;
 	}
 
 	public Long getSubjectGroupDetails(String subjectGroupName,
 			String subjectDomainType, String desc,
 			String calculatedSGNameORSubjectList, boolean isCalculated)
 			throws Exception {
 		System.out.println("Start getSubjectGroupDetail ==" + subjectGroupName);
 
 		Long subjectGroupId = null;
 		List<Long> subjectGroupIds = null;
 		FindSubjectGroupsRequest findSubjectGroupsRequest = new FindSubjectGroupsRequest();
 		FindSubjectGroupsResponse findSubjectGroupsResponse = new FindSubjectGroupsResponse();
 
 		SubjectGroupQuery subjectGroupQuery = new SubjectGroupQuery();
 		List<SubjectGroupKey> subjectGroupKeyList = subjectGroupQuery
 				.getSubjectGroupKey();
 		SubjectGroupKey subjectGroupKey = new SubjectGroupKey();
 		subjectGroupKey.setSubjectGroupName(subjectGroupName);
 		subjectGroupKey.setSubjectType(subjectDomainType);
 		subjectGroupKeyList.add(subjectGroupKey);
 
 		findSubjectGroupsRequest.setSubjectGroupQuery(subjectGroupQuery);
 
 		try {
 			findSubjectGroupsResponse = this
 					.<FindSubjectGroupsRequest, FindSubjectGroupsResponse> invokePolicyService(
 							"findSubjectGroups", findSubjectGroupsRequest);
 			if (findSubjectGroupsResponse.getSubjectGroups() != null
 					&& findSubjectGroupsResponse.getSubjectGroups().size() > 0) {
 
 				subjectGroupId = findSubjectGroupsResponse.getSubjectGroups()
 						.get(0).getSubjectMatch() != null ? getIdFromSubjectMatch(findSubjectGroupsResponse
 						.getSubjectGroups().get(0).getSubjectMatch())
 						: null;
 			}
 
 			if (subjectGroupId == null) {
 				SubjectGroup subjectGroup = new SubjectGroup();
 				subjectGroup.setSubjectGroupName(subjectGroupName);
 				subjectGroup.setSubjectType(subjectDomainType);
 				subjectGroup.setDescription(desc);
 
 				if (isCalculated) {
 					subjectGroup.setSubjectGroupCalculator(calculatedSGNameORSubjectList);
 				} else if (calculatedSGNameORSubjectList!=null){
 					String[] keys = calculatedSGNameORSubjectList.split("&");
 					for (String subjects : keys) {
 						String[] subjectInfo = subjects.split("#");
 						if (subjectInfo.length < 2) {
 							throw new Exception(
 									" Subject name or Subject Type is missing in the request");
 						}
 
 						Long subjectId = getSubjectDetails(subjectInfo[0],	subjectInfo[1]);
 						Subject subject = new  Subject();
 						if (subjectId!=null) {
 							 subject = mapIncludedSubject(subjectId);	
 						}
                         subject.setSubjectName(subjectInfo[0]);
                         subject.setSubjectType(subjectInfo[1]);
 						subjectGroup.getSubject().add(subject);
 					}
 				}
 				subjectGroupIds = createSubjectGroups(subjectGroup);
 				subjectGroupId = subjectGroupIds != null
 						& subjectGroupIds.size() > 0 ? subjectGroupIds.get(0)
 						: null;
 			}
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 		System.out.println("End getSubjectGroupDetail ==" + subjectGroupName);
 
 		return subjectGroupId;
 
 	}
 
 	public Long getSubjectDetails(String subjectName, String domain) {
 
 		System.out.println("Start getSubjectDetail ==" + subjectName);
 		Long subjectId = null;
 
 		FindSubjectsRequest findSubjectsRequest = new FindSubjectsRequest();
 		FindSubjectsResponse findSubjectsResponse = new FindSubjectsResponse();
 
 		SubjectQuery subjectQuery = new SubjectQuery();
 		List<SubjectKey> subjectKeyList = subjectQuery.getSubjectKey();
 
 		SubjectKey subjectKey = new SubjectKey();
 		subjectKey.setSubjectName(subjectName);
 		subjectKey.setSubjectType(domain);
 		subjectKeyList.add(subjectKey);
 
 		findSubjectsRequest.setSubjectQuery(subjectQuery);
 
 		try {
 			findSubjectsResponse = this
 					.<FindSubjectsRequest, FindSubjectsResponse> invokePolicyService(
 							"findSubjects", findSubjectsRequest);
 
 			if (findSubjectsResponse.getAck().toString().equalsIgnoreCase(
 					"success")
 					&& !(findSubjectsResponse.getSubjects().isEmpty())) {
 
 				subjectId = findSubjectsResponse.getSubjects().size() > 0
 						&& findSubjectsResponse.getSubjects().get(0)
 								.getSubjectMatch().size() > 0 ? getIdFromSubjectMatch(findSubjectsResponse
 						.getSubjects().get(0).getSubjectMatch().get(0))
 						: null;
 
 			}
 			if (subjectId == null) {
 				Subject subject = new Subject();
 				subject.setSubjectName(subjectName);
 				subject.setDescription("Desc " + subject.getSubjectName());
 				subject.setSubjectType(domain);
 				
 				
 				if (isInternalSubject(domain)) {
                     List<Long> subjectIdsList = createSubjects(subject.getSubjectType(),
                                                                subject.getExternalSubjectId(),
                                                                subject.getSubjectName());
 
                     subjectId = (subjectIdsList != null) && subjectIdsList.size() > 0 ? 
                                     subjectIdsList.get(0) : null;
 				} else {
                     Subject extSubjects = getExternalSubject(subjectName, domain);
                     if (extSubjects!=null) 
                         subject.setExternalSubjectId(extSubjects
                             .getExternalSubjectId());
                     
                     subjectId = createExtenalSubjectReference(subject);
 				}
 
 			}
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 		return subjectId;
 	}
 
 	public boolean isInternalSubject(String domain) {
 		GetMetaDataRequest getMetaDataRequest = new GetMetaDataRequest();
 		QueryCondition qc = new QueryCondition();
 		Query query = new Query();
 		query.setQueryType("SUBJECT_TYPE");
 		query.setQueryValue("Type");
 		qc.setResolution(Resolution.AND);
 		qc.getQuery().add(query);
 		getMetaDataRequest.setQueryCondition(qc);
 		GetMetaDataResponse res = getMetaData(getMetaDataRequest);
 				
 		if (res.getAck().equals(AckValue.SUCCESS) && res.getMetadataValue()!=null) {
 			List<KeyValuePair> pairs = res.getMetadataValue();
 			for (KeyValuePair keyVal:pairs) {
 				if (keyVal.getKey().equalsIgnoreCase(domain) && keyVal.getValue().equalsIgnoreCase("0")) {
 					return true;
 				}
 			}
 		} 
 		return false;
 	}
 
 	public static Long getIdFromSubjectMatch(SubjectMatchType matchType) {
 		Long subjectId = null;
 		if (matchType != null) {
 			AttributeValueType attributeValue = matchType.getAttributeValue();
 			String idString = attributeValue.getContent().get(0).toString();
 			if ("urn:oasis:names:tc:xacml:1.0:function:integer-equal"
 					.equals(matchType.getMatchId())) {
 				try {
 					subjectId = Long.parseLong(idString);
 				} catch (Exception e) {
 
 				}
 			}
 
 			if ("urn:oasis:names:tc:xacml:1.0:function:string-regexp-match"
 					.equals(matchType.getMatchId())) {
 				try {
 					subjectId = Long.parseLong(idString.substring(3, idString
 							.length() - 1));
 				} catch (Exception e) {
 
 				}
 			}
 		}
 		return subjectId;
 	}
 
 	public CreateResourcesResponse createResources(
 			CreateResourcesRequest createResourcesRequest) {
 		CreateResourcesResponse createResourcesResponse = new CreateResourcesResponse();
 
 		try {
 			createResourcesResponse = this
 					.<CreateResourcesRequest, CreateResourcesResponse> invokePolicyService(
 							"createResources", createResourcesRequest);
 
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 
 		return createResourcesResponse;
 
 	}
 
 	public DeleteResourcesResponse deleteResources(
 			DeleteResourcesRequest deleteResourcesRequest) {
 		DeleteResourcesResponse deleteResourcesResponse = new DeleteResourcesResponse();
 
 		try {
 			deleteResourcesResponse = this
 					.<DeleteResourcesRequest, DeleteResourcesResponse> invokePolicyService(
 							"deleteResources", deleteResourcesRequest);
 
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 
 		return deleteResourcesResponse;
 
 	}
 
 	public UpdateResourcesResponse updateResources(
 			UpdateResourcesRequest updateResourcesRequest) {
 
 		UpdateResourcesResponse updateResourcesResponse = new UpdateResourcesResponse();
 		try {
 			updateResourcesResponse = this
 					.<UpdateResourcesRequest, UpdateResourcesResponse> invokePolicyService(
 							"updateResources", updateResourcesRequest);
 
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 
 		return updateResourcesResponse;
 	}
 
 	public Long createResource(String resourceType, String resourceName,String... operaionNames) {
 		System.out.println("Start createResource ==" + resourceName + "/"
 				+ resourceName);
 		Long resourceId;
 		GetResourcesResponse getResourcesResponse = new GetResourcesResponse();
 		GetResourcesRequest getResourcesRequest = new GetResourcesRequest();
 		List<ResourceKey> resourceKeyList = getResourcesRequest
 				.getResourceKey();
 		ResourceKey resourceKey = new ResourceKey();
 		resourceKey.setResourceName(resourceName);
 		resourceKey.setResourceType(resourceType);
 		resourceKeyList.add(resourceKey);
 		try {
 			getResourcesResponse = this
 					.<GetResourcesRequest, GetResourcesResponse> invokePolicyService(
 							"getResources", getResourcesRequest);
 
 			resourceId = getResourcesResponse.getAck().toString()
 					.equalsIgnoreCase("success")
 					&& (!getResourcesResponse.getResources().isEmpty()) ? getResourcesResponse
 					.getResources().get(0).getResourceId()
 					: null;
 
 			if (resourceId == null) {
 				Resource resource = new Resource();
 				resource.setResourceName(resourceName);
 				resource.setDescription("Desc " + resourceName);
 				resource.setResourceType(resourceType);
 				
 				for (String operationName : operaionNames ) {
 					Operation operation = new Operation();
 					operation.setOperationName(operationName);
 					resource.getOperation().add(operation);
 				}
 				CreateResourcesRequest createResourcesRequest = new CreateResourcesRequest();
 
 				List<Resource> resourcesList = createResourcesRequest
 						.getResources();
 				resourcesList.add(resource);
 				CreateResourcesResponse createResourcesResponse = new CreateResourcesResponse();
 
 				createResourcesResponse = this
 						.<CreateResourcesRequest, CreateResourcesResponse> invokePolicyService(
 								"createResources", createResourcesRequest);
 				resourceId = createResourcesResponse.getAck().toString()
 						.equalsIgnoreCase("failure")
 						&& createResourcesResponse.getResourceIds().isEmpty() ? null
 						: createResourcesResponse.getResourceIds().get(0);
 			}
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 
 		return resourceId;
 	}
 
 	private Rule getRuleObject(String ruleName, long duration,
 			long rolloverperiod, EffectType effect, String conditionvalue) {
 		System.out.println("Start getRuleObject   => [" + ruleName + "]");
 		Rule rule = new Rule();
 
 		if (conditionvalue != null) {
 			rule.setRuleName(ruleName);
 			rule.setRolloverPeriod(rolloverperiod);
 
 			Condition condition = new Condition();
 			Expression expression = new Expression();
 			PrimitiveValue primitiveValue = new PrimitiveValue();
 			primitiveValue.setValue(conditionvalue);
 			expression.setPrimitiveValue(primitiveValue);
 			condition.setExpression(expression);
 			rule.setCondition(condition);
 			rule.setPriority(0);
 
 			rule.setRolloverPeriod(rolloverperiod);
 			rule.setEffectDuration(duration);
 			rule.setDescription("Desc ");
 		}
 		rule.setEffect(effect);
 
 		System.out.println("End getRuleObject  => [" + ruleName + "]");
 		return rule;
 	}
 
 	public Long createResourceWithSingleOperation(String resourceType,
 			String resourceName, String operationName) {
 
 		return createResourceWithMultipleOperations(resourceType,resourceName,operationName);
 
 	}
 	
 	public Long createResourceWithMultipleOperations(String resourceType,
 			String resourceName, String... operationNames) {
 
 		CreateResourcesRequest createResourcesRequest = new CreateResourcesRequest();
 		CreateResourcesResponse createResourcesResponse = new CreateResourcesResponse();
 		Resource resource = new Resource();
 		Operation operation = new Operation();
 		resource.setResourceName(resourceName);
 		resource.setResourceType(resourceType);
 		for (String operationName:operationNames) {
 			operation = new Operation();
 			operation.setOperationName(operationName);
 			operation.setDescription("Desc :"+operationName);
 			resource.getOperation().add(operation);
 		}
 
 		createResourcesRequest.getResources().add(resource);
 		createResourcesResponse = createResources(createResourcesRequest);
 		Long resourceId = createResourcesResponse.getErrorMessage() == null
 				&& !(createResourcesResponse.getResourceIds().isEmpty()) ? createResourcesResponse
 				.getResourceIds().get(0)
 				: null;
 
 		return resourceId;
 
 	}
 
 	public Long createOpertionForResource(String resourceType, String resourceName,
 			String operationName) {
 		Long operationId = null;
 		Long resourceId = createResource(resourceType, resourceName,operationName);
 				
 		if (operationName != null) {
 
 			System.out.println("Start create operations => [" + resourceName
 					+ "][" + operationName + "]");
 			List<Operation> operationsList = getOperationsByResourceId(resourceId);
 
 			if (operationsList != null) {
 
 				for (Operation op : operationsList) {
 					if (op.getOperationName().equalsIgnoreCase(operationName))
 						return op.getOperationId();
 				}
 			}
 
 			System.out.println("End create operations => [" + resourceName
 					+ "][" + operationName + "]");
 
 			// assing operation
 
 			operationId = createOperation(operationName, resourceId);
 		}
 		return operationId;
 	}
 
 	protected Long createOperation(String operationName, Long resourceId) {
 
 		Long opertionId = null;
 
 		try {
 			UpdateResourcesRequest updateResourcesRequest = new UpdateResourcesRequest();
 			Resource resource = new Resource();
 			resource.setResourceId(resourceId);
 			resource.setResourceType("SERVICE");
 			Operation operation = new Operation();
 			operation.setOperationName(operationName);
 			operation.setDescription("Desc " + operationName);
 			resource.getOperation().add(operation);
 			updateResourcesRequest.getResources().add(resource);
 			updateResourcesRequest.setUpdateMode(UpdateMode.UPDATE);
 
 			UpdateResourcesResponse updateResourcesResponse = this
 					.<UpdateResourcesRequest, UpdateResourcesResponse> invokePolicyService(
 							"updateResources", updateResourcesRequest);
 			if (updateResourcesResponse.getAck().toString().equalsIgnoreCase(
 					"Success")) {
 				List<Operation> operationsList = getOperationsByResourceId(resourceId);
 				for (Operation op : operationsList) {
 					if (op.getOperationName().equalsIgnoreCase(operationName))
 						return op.getOperationId();
 				}
 			}
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 		return opertionId;
 	}
 
 	public List<Operation> getOperationsByResourceId(Long resourceId) {
 
 		List<Operation> operationList;
 		GetOperationsRequest getOperationsRequest = new GetOperationsRequest();
 		List<ResourceKey> resourceKeyList = getOperationsRequest
 				.getResourceKey();
 		ResourceKey resourceKey = new ResourceKey();
 		resourceKey.setResourceId(resourceId);
 		resourceKey.setResourceType("SERVICE");
 		resourceKeyList.add(resourceKey);
 
 		GetOperationsResponse getOperationsResponse = new GetOperationsResponse();
 		try {
 			getOperationsResponse = this
 					.<GetOperationsRequest, GetOperationsResponse> invokePolicyService(
 							"getOperations", getOperationsRequest);
 
 			operationList = (getOperationsResponse.getAck().toString()
 					.equalsIgnoreCase("Success") && (!getOperationsResponse
 					.getOperations().isEmpty())) ? getOperationsResponse
 					.getOperations() : null;
 
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 		return operationList;
 	}
 
 	public boolean updateSubjectGroup(String sgName, String type,
 			String subjectName, UpdateMode mode) {
 		
 		UpdateSubjectGroupsRequest updateSubjectGroupsRequest = new UpdateSubjectGroupsRequest();
 		UpdateSubjectGroupsResponse updateSubjectGroupsResponse = new UpdateSubjectGroupsResponse();
 		
 		try {
 			SubjectGroup subjectGroup = new SubjectGroup();
 			subjectGroup.setSubjectGroupName(sgName);
 			subjectGroup.setSubjectType(type);
 			Subject subject = new Subject();
 			subject.setSubjectName(subjectName);
 			subject.setSubjectType(type);
 			
 			subjectGroup.getSubject().add(subject);
 			updateSubjectGroupsRequest.getSubjectGroups().add(subjectGroup);
 			updateSubjectGroupsRequest.setUpdateMode(mode);
 
 			updateSubjectGroupsResponse = this
 					.<UpdateSubjectGroupsRequest, UpdateSubjectGroupsResponse> invokePolicyService(
 							"updateSubjectGroups", updateSubjectGroupsRequest);
 
 			return updateSubjectGroupsResponse.getAck().equals(AckValue.SUCCESS);
 				
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 		
 	}
 
 	public CreateExternalSubjectReferencesResponse createExternalSubjectReferences(
 			CreateExternalSubjectReferenceRequest createExternalSubjectReferenceRequest) {
 		CreateExternalSubjectReferencesResponse createExternalSubjectReferencesResponse = new CreateExternalSubjectReferencesResponse();
 		try {
 			createExternalSubjectReferencesResponse = this
 					.<CreateExternalSubjectReferenceRequest, CreateExternalSubjectReferencesResponse> invokePolicyService(
 							"createExternalSubjectReferences",
 							createExternalSubjectReferenceRequest);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 		return createExternalSubjectReferencesResponse;
 	}
 
 	public UpdateSubjectGroupsResponse updateSubjectGroups(
 			UpdateSubjectGroupsRequest updateSubjectGroupsRequest) {
 		UpdateSubjectGroupsResponse updateSubjectGroupsResponse = new UpdateSubjectGroupsResponse();
 		try {
 			updateSubjectGroupsResponse = this
 					.<UpdateSubjectGroupsRequest, UpdateSubjectGroupsResponse> invokePolicyService(
 							"updateSubjectGroups", updateSubjectGroupsRequest);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 		return updateSubjectGroupsResponse;
 	}
 
 	 public FindPoliciesResponse findPolicy(PolicyKey policyKey) {
 	        FindPoliciesResponse response = new FindPoliciesResponse();
 	        try {
 	            FindPoliciesRequest request = new FindPoliciesRequest();
 	            request.getPolicyKey().add(policyKey);
 	            request.setOutputSelector(PolicyOutputSelector.ALL);
 	            Query query = new Query();
 	    		query.setQueryType("ActivePoliciesOnly"); // Effect/SubjectSearchScope/MaskedIds/ActivePoliciesOnly
 	    		query.setQueryValue("FALSE"); // BLOCK|FLAG|CHALLENGE|ALLOW/TARGET|EXCLUDED|BOTH/TRUE|FALSE/TRUE|FALSE
 	    		QueryCondition queryCondition = new QueryCondition();
 	    		queryCondition.getQuery().add(query);
 	    		request.setQueryCondition(queryCondition);
 	            response = this.<FindPoliciesRequest,FindPoliciesResponse> invokePolicyService("findPolicies",request);           
 
 	        } catch (InterruptedException e) {
 				throw new RuntimeException(e);
 			} catch (ServiceException e) {
 				throw new RuntimeException(e);
 			} catch (ExecutionException e) {
 				throw new RuntimeException(e);
 			}
 	        return response;
 	    }
 	 public FindPoliciesResponse findPolicy(SubjectGroupKey subjectGroupKey,PolicyKey policyKey) {
 	        FindPoliciesResponse response = new FindPoliciesResponse();
 	        try {
 	            FindPoliciesRequest request = new FindPoliciesRequest();
 	            request.getSubjectGroupKey().add(subjectGroupKey);
 	            request.getPolicyKey().add(policyKey);
 	            request.setOutputSelector(PolicyOutputSelector.ALL);
 	        	Query query = new Query();
 	    		query.setQueryType("ActivePoliciesOnly"); // Effect/SubjectSearchScope/MaskedIds/ActivePoliciesOnly
 	    		query.setQueryValue("FALSE"); // BLOCK|FLAG|CHALLENGE|ALLOW/TARGET|EXCLUDED|BOTH/TRUE|FALSE/TRUE|FALSE
 	    		QueryCondition queryCondition = new QueryCondition();
 	    		queryCondition.getQuery().add(query);
 	    		request.setQueryCondition(queryCondition);	    	
 	    		response = this.<FindPoliciesRequest,FindPoliciesResponse> invokePolicyService("findPolicies",request);           
 
 	        } catch (InterruptedException e) {
 				throw new RuntimeException(e);
 			} catch (ServiceException e) {
 				throw new RuntimeException(e);
 			} catch (ExecutionException e) {
 				throw new RuntimeException(e);
 			}
 	        return response;
 	    }
 	 public FindPoliciesResponse findPolicy(ResourceKey resourceKey,PolicyKey policyKey) {
 	        FindPoliciesResponse response = new FindPoliciesResponse();
 	        try {
 	            FindPoliciesRequest request = new FindPoliciesRequest();
 	            request.getResourceKey().add(resourceKey);
 	            request.getPolicyKey().add(policyKey);
 	            request.setOutputSelector(PolicyOutputSelector.ALL);
 	            Query query = new Query();
 	    		query.setQueryType("ActivePoliciesOnly"); // Effect/SubjectSearchScope/MaskedIds/ActivePoliciesOnly
 	    		query.setQueryValue("FALSE"); // BLOCK|FLAG|CHALLENGE|ALLOW/TARGET|EXCLUDED|BOTH/TRUE|FALSE/TRUE|FALSE
 	    		QueryCondition queryCondition = new QueryCondition();
 	    		queryCondition.getQuery().add(query);
 	    		request.setQueryCondition(queryCondition);
 	    		
 	            response = this.<FindPoliciesRequest,FindPoliciesResponse> invokePolicyService("findPolicies",request);           
 
 	        } catch (InterruptedException e) {
 				throw new RuntimeException(e);
 			} catch (ServiceException e) {
 				throw new RuntimeException(e);
 			} catch (ExecutionException e) {
 				throw new RuntimeException(e);
 			}
 	        return response;
 	    }
 	 
 	 public FindPoliciesResponse findPolicy(OperationKey opKey, PolicyKey policyKey) {
 			FindPoliciesResponse response = null;
 			try {
 					FindPoliciesRequest request = new FindPoliciesRequest();
 				    request.getOperationKey().add(opKey);
 		            request.getPolicyKey().add(policyKey);
 		            request.setOutputSelector(PolicyOutputSelector.ALL);
 		            response = this.<FindPoliciesRequest,FindPoliciesResponse> invokePolicyService("findPolicies",request);
 		            QueryCondition queryCondition = new QueryCondition();
 		            Query query = new Query();
 		            query.setQueryType("ActivePoliciesOnly");
 		            query.setQueryValue("FALSE");
 		            queryCondition.getQuery().add(query);
 		            request.setQueryCondition(queryCondition);
 		            
 		        } catch (InterruptedException e) {
 					throw new RuntimeException(e);
 				} catch (ServiceException e) {
 					throw new RuntimeException(e);
 				} catch (ExecutionException e) {
 					throw new RuntimeException(e);
 				}
 		        return response;
 		    }
 
 	public FindPoliciesResponse findPolicies(FindPoliciesRequest request) {
 		FindPoliciesResponse response = new FindPoliciesResponse();
 		try {
 			request.setOutputSelector(PolicyOutputSelector.ALL);
 			
 			response = this
 					.<FindPoliciesRequest, FindPoliciesResponse> invokePolicyService(
 							"findPolicies", request);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 		return response;
 	}
 	 
 	public FindSubjectsResponse findSubjects(
 			FindSubjectsRequest findSubjectsRequest) {
 		
 		FindSubjectsResponse findSubjectsResponse = new  FindSubjectsResponse();
 		try {
 			findSubjectsResponse = this
 					.<FindSubjectsRequest, FindSubjectsResponse> invokePolicyService(
 							"findSubjects",	findSubjectsRequest);
 			
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 		return findSubjectsResponse;
 		
 	}
 	
 	public long findSubjects(String subjectType,String SubjectName) throws Exception {
 		FindSubjectsRequest findSubjectsRequest = new FindSubjectsRequest();
 		SubjectKey key = new SubjectKey();
 		key.setSubjectName(SubjectName);
 		key.setSubjectType(subjectType);
 		SubjectQuery sq = new SubjectQuery();
 		sq.getSubjectKey().add(key);
 		findSubjectsRequest.setSubjectQuery(sq);
 		FindSubjectsResponse res = findSubjects(findSubjectsRequest);
 		if (res.getErrorMessage()!=null )
 			 throw new Exception(res.getErrorMessage().toString());
 		Long subjectId = getSubjectId(res);
 		return subjectId;
 	}
 
 	private Long getSubjectId(FindSubjectsResponse res) {
 		Long subjectId = -1L;
 		List<SubjectMatchType> smt = res.getSubjects()!=null && res.getSubjects().size()>0 ? res.getSubjects().get(0).getSubjectMatch() : null;
 		AttributeValueType avt = smt!=null && smt.size() > 0 ? smt.get(0).getAttributeValue() : null;
 		subjectId = avt!=null && avt.getContent()!=null && avt.getContent().size() > 0 ? Long.valueOf(avt.getContent().get(0).toString()) : -1L;
 		return subjectId;
 		
 	}
 	public long findSubjectGroup(String subjectType,String subjectGroupName) throws Exception {
 		FindSubjectGroupsRequest findSubjectGroupsRequest = new FindSubjectGroupsRequest();
 		SubjectGroupKey key = new SubjectGroupKey();
 		key.setSubjectGroupName(subjectGroupName);
 		key.setSubjectType(subjectType);
 		SubjectGroupQuery sgq = new SubjectGroupQuery();
 		sgq.getSubjectGroupKey().add(key);
 		findSubjectGroupsRequest.setSubjectGroupQuery(sgq);
 		FindSubjectGroupsResponse res = findSubjectGroups(findSubjectGroupsRequest);
 		if (res.getErrorMessage()!=null )
 			 throw new Exception(res.getErrorMessage().toString());
 		Long subjectGroupId = getSubjectGroupId(res);
 		return subjectGroupId;
 	}
 	
 	private Long getSubjectGroupId(FindSubjectGroupsResponse res) {
 		Long subjectGroupId = -1L;
 		SubjectMatchType smt = res.getSubjectGroups()!=null && res.getSubjectGroups().size()>0 ? res.getSubjectGroups().get(0).getSubjectMatch() : null;
 		AttributeValueType avt = smt!=null ? smt.getAttributeValue() : null;
 		subjectGroupId = avt!=null && avt.getContent()!=null && avt.getContent().size() > 0 ? Long.valueOf(avt.getContent().get(0).toString()) : -1L;
 		return subjectGroupId;
 	}
 
 	public FindSubjectGroupsResponse findSubjectGroups(
 			FindSubjectGroupsRequest findSubjectGroupsRequest) {
 		FindSubjectGroupsResponse findSubjectGroupsResponse = new  FindSubjectGroupsResponse();
 		try {
 			findSubjectGroupsResponse = this
 					.<FindSubjectGroupsRequest, FindSubjectGroupsResponse> invokePolicyService(
 							"findSubjectGroups",	findSubjectGroupsRequest);
 			
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 		return findSubjectGroupsResponse;
 	}
 
 	public FindExternalSubjectsResponse findExternalSubjects(
 			FindExternalSubjectsRequest findExternalSubjectsRequest) {
 		FindExternalSubjectsResponse findExternalSubjectsResponse = new  FindExternalSubjectsResponse();
 		try {
 			findExternalSubjectsResponse = this
 					.<FindExternalSubjectsRequest, FindExternalSubjectsResponse> invokePolicyService(
 							"findExternalSubjects",	findExternalSubjectsRequest);
 			
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 		return findExternalSubjectsResponse;
 	}
 	public Long findExternalSubject(String subjectType, String subjectName) throws Exception {
 	
 		FindExternalSubjectsResponse findExternalSubjectsResponse = new  FindExternalSubjectsResponse();
 		FindExternalSubjectsRequest findExternalSubjectsRequest = new FindExternalSubjectsRequest();
 		SubjectKey key = new SubjectKey();
 		key.setSubjectName(subjectName); key.setSubjectType(subjectType);	
 		SubjectQuery sq= new SubjectQuery();
 		sq.getSubjectKey().add(key);
 		findExternalSubjectsRequest.setSubjectQuery(sq);
 		findExternalSubjectsResponse = findExternalSubjects(findExternalSubjectsRequest);
 		if (findExternalSubjectsResponse.getErrorMessage()!=null )
 			 throw new Exception(findExternalSubjectsResponse.getErrorMessage().getError()
 						.get(0).getMessage());
 		Long subjectId = findExternalSubjectsResponse.getSubjects()!=null && findExternalSubjectsResponse.getSubjects().size() > 0 
 					? findExternalSubjectsResponse.getSubjects().get(0).getExternalSubjectId() : -1;
 		return subjectId;
 	}
 	
 	public ValidatePolicyResponse validatePolicy(
 			ValidatePolicyRequest validatePolicyRequest) {
 		ValidatePolicyResponse validatePolicyResponse = new  ValidatePolicyResponse();
 		try {
 			validatePolicyResponse = this
 					.<ValidatePolicyRequest, ValidatePolicyResponse> invokePolicyService(
 							"validatePolicy", validatePolicyRequest);
 			
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 		return validatePolicyResponse;
 	}
 
 	public GetResourcesResponse getResources(
 			GetResourcesRequest getResourcesRequest) {
 		
 		GetResourcesResponse getResourcesResponse = new  GetResourcesResponse();
 		try {
 			getResourcesResponse = this
 					.<GetResourcesRequest, GetResourcesResponse> invokePolicyService(
 							"getResources",	getResourcesRequest);
 			
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 		return getResourcesResponse;
 	}
 
 	public DisablePolicyResponse disablePolicy(
 			DisablePolicyRequest disablePolicyRequest) {
 	
 		DisablePolicyResponse disablePolicyResponse = new  DisablePolicyResponse();
 		try {
 			disablePolicyResponse = this
 					.<DisablePolicyRequest, DisablePolicyResponse> invokePolicyService(
 							"disablePolicy",	disablePolicyRequest);
 			
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 		return disablePolicyResponse;
 	}
 
 	public EnablePolicyResponse enablePolicy(
 			EnablePolicyRequest enablePolicyRequest) {
 
 		EnablePolicyResponse enablePolicyResponse = new  EnablePolicyResponse();
 		try {
 			enablePolicyResponse = this
 					.<EnablePolicyRequest, EnablePolicyResponse> invokePolicyService(
 							"enablePolicy",	enablePolicyRequest);
 			
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 		return enablePolicyResponse;
 	}
 
 	public GetMetaDataResponse getMetaData(GetMetaDataRequest getMetaDataRequest) {
 		
 		GetMetaDataResponse getMetaDataResponse = new  GetMetaDataResponse();
 		try {
 			getMetaDataResponse = this
 					.<GetMetaDataRequest, GetMetaDataResponse> invokePolicyService(
 							"getMetaData",	getMetaDataRequest);
 			
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 		return getMetaDataResponse;
 	}
 
 	public GetAuthenticationPolicyResponse getAuthenticationPolicy(
 			GetAuthenticationPolicyRequest getAuthenticationPolicyRequest) {
 		GetAuthenticationPolicyResponse getAuthenticationPolicyResponse = new  GetAuthenticationPolicyResponse();
 		try {
 			getAuthenticationPolicyResponse = this
 					.<GetAuthenticationPolicyRequest, GetAuthenticationPolicyResponse> invokePolicyService(
 							"getAuthenticationPolicy",	getAuthenticationPolicyRequest);
 			
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 		return getAuthenticationPolicyResponse;
 		
 	}
 
 	public GetEntityHistoryResponse getEntityHistory(
 			GetEntityHistoryRequest getEntityHistoryRequest) {
 		GetEntityHistoryResponse getEntityHistoryResponse = new GetEntityHistoryResponse();
 	
 		try {
 			getEntityHistoryResponse = this
 					.<GetEntityHistoryRequest, GetEntityHistoryResponse> invokePolicyService(
 							"getEntityHistory",	getEntityHistoryRequest);
 			
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		} catch (ServiceException e) {
 			throw new RuntimeException(e);
 		} catch (ExecutionException e) {
 			throw new RuntimeException(e);
 		}
 		return getEntityHistoryResponse;
 	}
 
 	
 }
