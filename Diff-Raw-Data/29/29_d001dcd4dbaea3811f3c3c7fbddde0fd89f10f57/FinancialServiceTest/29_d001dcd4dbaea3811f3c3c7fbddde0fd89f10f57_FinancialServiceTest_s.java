 package com.financial.tools.recorderserver.service;
 
 import java.io.IOException;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 
 import com.fasterxml.jackson.core.JsonGenerationException;
 import com.fasterxml.jackson.databind.JsonMappingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.financial.tools.recorderserver.AbstractComponentTestCase;
 import com.financial.tools.recorderserver.client.FinancialServiceClient;
 import com.financial.tools.recorderserver.payload.CashinRequest;
 import com.financial.tools.recorderserver.payload.CreateUserRequest;
 import com.financial.tools.recorderserver.payload.FinancialRecordRequest;
 import com.financial.tools.recorderserver.payload.UserFinancialInfoResponse;
 import com.google.common.collect.Lists;
 
 public class FinancialServiceTest extends AbstractComponentTestCase {
 
 	private FinancialServiceClient financialServiceClient;
 
 	@Before
 	public void setUp() {
 		String serverAddress = baseUrl + "/api/";
 		financialServiceClient = new FinancialServiceClient(serverAddress);
 	}
 
 	@Test
 	public void test123() throws JsonGenerationException, JsonMappingException, IOException {
 		ObjectMapper mapper = new ObjectMapper();
 		CreateUserRequest request = new CreateUserRequest("Neway", "123456", 100L);
 		String result = mapper.writeValueAsString(request);
 		System.out.println(result);
 
 		FinancialRecordRequest financialRecordRequest = new FinancialRecordRequest();
 		financialRecordRequest.setName("Test");
 		financialRecordRequest.setTotalFee(100L);
 
 		System.out.println(mapper.writeValueAsString(financialRecordRequest));
 	}
 
 	@Test
 	public void testCashin() throws Exception {
 		CashinRequest request = new CashinRequest(1L, 100L);
 		String result = financialServiceClient.cashin(request);
 		Assert.assertEquals("200", result);
 	}
 
 	@Test
 	public void testGetUserFinancialInfo() throws Exception {
 		UserFinancialInfoResponse userFinancialInfo = financialServiceClient.getUserFinancialInfo("2");
 		Assert.assertEquals("Fred", userFinancialInfo.getUserName());
 		Assert.assertEquals(100L, userFinancialInfo.getBalance());
 	}
 
	@Ignore("Need clarify is H2 transaction issue or not.")
 	@Test
 	public void testFinancialCalculateForUser() throws Exception {
 		FinancialRecordRequest financialRecordRequest = new FinancialRecordRequest("ball", 20, Lists.newArrayList(1L,
 				2L));
 		String financialRecordId = financialServiceClient.createFinancialRecord(financialRecordRequest);
 		financialServiceClient.updateFinance(financialRecordId);
 
 		// verify
 		UserFinancialInfoResponse userFinancialInfo = financialServiceClient.getUserFinancialInfo("2");
 		Assert.assertEquals(90L, userFinancialInfo.getBalance());
 	}
 
 }
