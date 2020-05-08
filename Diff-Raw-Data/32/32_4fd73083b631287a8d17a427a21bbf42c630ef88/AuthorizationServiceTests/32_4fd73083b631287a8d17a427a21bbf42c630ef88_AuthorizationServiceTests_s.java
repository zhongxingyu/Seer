 package org.ebayopensource.turmeric.test.services.authorizationservice;
 
 import static org.junit.Assert.fail;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Properties;
 
 import org.ebayopensource.turmeric.security.v1.services.AuthorizeRequestType;
 import org.ebayopensource.turmeric.security.v1.services.AuthorizeResponseType;
 import org.ebayopensource.turmeric.services.authorizationservice.intf.gen.BaseAuthorizationServiceConsumer;
 import org.ebayopensource.turmeric.test.services.utils.TestDataReader;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 
 @RunWith(Parameterized.class)
 public class AuthorizationServiceTests extends CreateValidateAuthz {
 
 	private String request_id;
 	private String testcaseNumber;
 	private String testcaseDesc;
 	private BaseAuthorizationServiceConsumer m_consumer = null;
 	private String policyId;
 	private static final String s_PropFilePath = "AuthorizationServiceTests.properties";
 	private static Class className = AuthorizationServiceTests.class;
 	private static TestDataReader reader = null;
 	private static int max;
 
 	@Before
 	public void setUp() throws Exception {
 		reader = new TestDataReader(className);
 		max = maxPolicies(reader.getProps());
 		createPolicies(reader, max);
 		m_consumer = new BaseAuthorizationServiceConsumer();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		cleanUpPolicy(reader, max);
 		reader.unloadProperties();
 		reader = null;
 		m_consumer = null;
 	}
 
 	public String getName() {
 		return this.testcaseDesc;
 	}
 
 	public AuthorizationServiceTests(String policyId, String request_id,
 			String testcaseNumber, String testcaseDesc) {
 		this.policyId = policyId;
 		this.request_id = request_id;
 		this.testcaseNumber = testcaseNumber;
 		this.testcaseDesc = testcaseDesc;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Parameters
 	public static Collection data() throws IOException {
 		return loadInputData();
 	}
 
 	@SuppressWarnings("unchecked")
 	public static Collection loadInputData() throws IOException {
 		Properties props = new Properties();
 		List list = new ArrayList();
 		List eachRowData = new ArrayList();
 		InputStream inputStream = className.getResourceAsStream(s_PropFilePath);
 		props.load(inputStream);
 		String debug_tests = props.getProperty("test_to_debug");
 		String total_tests = props.getProperty("total_testcases");
 		if (!debug_tests.equals("") && !debug_tests.equals("null")) {
 			String[] debug_num = debug_tests.split(",");
 			for (String s : debug_num) {
 				int num = Integer.parseInt(s);
 				String policyid = props.getProperty("testcase" + s
 						+ ".policyid");
 				String testcasenum = props.getProperty(
 						"testcase" + s + ".number").trim();
 				String testcaseDesc = props.getProperty("testcase" + s
 						+ ".description");
 
 				if (policyid != null) {
 					eachRowData = new ArrayList();
 					eachRowData.add(policyid);
 					eachRowData.add("testcase" + testcasenum);
 					eachRowData.add(testcasenum);
 					eachRowData.add(testcaseDesc);
 					list.add(eachRowData.toArray());
 				}
 			}
 		} else {
 			// int len = Integer.parseInt(total_tests);
 			int len = totalTestCount(props);
 			for (int i = 0; i <= len; i++) {
 				String policyid = props.getProperty("testcase" + i
 						+ ".policyid");
 				String testcasenum = props.getProperty(
 						"testcase" + i + ".number").trim();
 				String testcaseDesc = props.getProperty("testcase" + i
 						+ ".description");
 
 				if (policyid != null) {
 					eachRowData = new ArrayList();
 					eachRowData.add(policyid);
 					eachRowData.add("testcase" + testcasenum);
 					eachRowData.add(testcasenum);
 					eachRowData.add(testcaseDesc);
 					list.add(eachRowData.toArray());
 				}
 			}
 		}
 
 		return list;
 	}
 
 	@Test
 	public void testAuthorize() throws Exception {
 		System.out.println("\n -- Testcase Number = " + testcaseNumber
 				+ "\n -- Testcase Description= " + testcaseDesc);
 
		try {
			String request_id = this.request_id;
			AuthorizeResponseType result = null;
			AuthorizeRequestType req = new AuthorizeRequestType();

			populateAuthzRequest(reader, req, request_id);
			populateSubjectDetails(reader, req, request_id);

			result = m_consumer.authorize(req);

			System.out.println("Error = "
					+ (result.getErrorMessage() != null ? result
							.getErrorMessage().getError().get(0).getMessage()
							: "No Error"));
			validateOutput(reader, result, request_id);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Testcase should not fail." + e.getMessage());
		}
 
 	}
 }
