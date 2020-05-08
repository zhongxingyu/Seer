 package org.motechproject.care.reporting.migration.util;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.HttpMethodBase;
 import org.apache.commons.httpclient.HttpState;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.params.HttpClientParams;
 import org.apache.commons.io.IOUtils;
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.mockito.stubbing.Answer;
 import org.motechproject.care.reporting.migration.common.Constants;
 import org.motechproject.care.reporting.migration.common.Page;
 import org.motechproject.care.reporting.migration.statistics.EndpointStatisticsCollector;
 import org.motechproject.care.reporting.migration.statistics.MigrationStatisticsCollector;
 import org.motechproject.care.reporting.migration.statistics.RequestTimer;
 
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertTrue;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 @RunWith(MockitoJUnitRunner.class)
 public class CommcareAPIHttpClientTest {
     public static final int LIMIT = 1;
     public static final int OFFSET = 2;
     @Mock
     private HttpClient httpClient;
     @Mock
     private MigrationStatisticsCollector migrationStatisticsCollector;
     @Mock
     private EndpointStatisticsCollector endpointStatisticsCollector;
     @Mock
     private RequestTimer requestTimer;
 
     private Properties commcareProperties;
     private CommcareAPIHttpClient commcareAPIHttpClient;
 
     @Before
     public void setUp() {
         commcareProperties = new Properties();
         commcareProperties.putAll(new HashMap<Object, Object>() {{
             put("commcareBaseUrl", "https://india.commcarehq.org/a");
             put("commcareDomain", "care-bihar");
             put("username", "");
             put("password", "");
             put("apiVersion", "v0.4");
            put("retry.count", "5");
         }});
         when(httpClient.getParams()).thenReturn(new HttpClientParams());
         when(httpClient.getState()).thenReturn(new HttpState());
 
         when(migrationStatisticsCollector.commcareEndpoint()).thenReturn(endpointStatisticsCollector);
         when(endpointStatisticsCollector.newRequest()).thenReturn(requestTimer);
         commcareAPIHttpClient = new CommcareAPIHttpClient(httpClient, commcareProperties, migrationStatisticsCollector);
     }
 
     @Test
     public void shouldFetchFormsWithPageOptionAsParameter() throws Exception {
         String receivedOn = DateTime.now().toString();
         when(httpClient.executeMethod(any(GetMethod.class))).thenAnswer(new CommcareRequestAnswer(getFormResponseWithHeaderField("received_on", receivedOn)));
 
         commcareAPIHttpClient.fetchForms(new HashMap<String, String>(), new Page(0, 10));
 
         ArgumentCaptor<GetMethod> methodCaptor = ArgumentCaptor.forClass(GetMethod.class);
         verify(httpClient).executeMethod(methodCaptor.capture());
 
         GetMethod getMethod = methodCaptor.getValue();
         Matcher matcher = findLimitOffset(getMethod);
         assertEquals("10", matcher.group(LIMIT));
         assertEquals("0", matcher.group(OFFSET));
 
         verify(requestTimer).start();
         verify(requestTimer).successful();
         verify(requestTimer, never()).failed();
         verify(requestTimer, never()).retried();
     }
 
     @Test
     public void shouldFetchCasesWithPageOptionAsParameter() throws Exception {
         String serverModifiedOn = DateTime.now().toString();
         when(httpClient.executeMethod(any(GetMethod.class))).thenAnswer(new CommcareRequestAnswer(getCaseResponseWithHeaderField("server_modified_on", serverModifiedOn)));
 
         commcareAPIHttpClient.fetchCases(new HashMap<String, String>(), new Page(20, 100));
 
         ArgumentCaptor<GetMethod> methodCaptor = ArgumentCaptor.forClass(GetMethod.class);
         verify(httpClient).executeMethod(methodCaptor.capture());
 
         GetMethod getMethod = methodCaptor.getValue();
         Matcher matcher = findLimitOffset(getMethod);
         assertEquals("100", matcher.group(LIMIT));
         assertEquals("20", matcher.group(OFFSET));
 
         verify(requestTimer).start();
         verify(requestTimer).successful();
         verify(requestTimer, never()).failed();
         verify(requestTimer, never()).retried();
     }
 
     @Test
     public void shouldOverridePaginationOptionIfAlreadyPresentForCases() throws IOException {
         String serverModifiedOn = DateTime.now().toString();
         Map<String,String> parameters = new HashMap<String,String>(){{
                 put(Constants.LIMIT, "1000");
                 put(Constants.OFFSET, "2000");
         }};
         Page paginationOption = new Page(4000, 200);
         when(httpClient.executeMethod(any(GetMethod.class))).thenAnswer(new CommcareRequestAnswer(getCaseResponseWithHeaderField("server_modified_on", serverModifiedOn)));
         commcareAPIHttpClient.fetchCases(parameters, paginationOption);
 
         ArgumentCaptor<GetMethod> methodCaptor = ArgumentCaptor.forClass(GetMethod.class);
         verify(httpClient).executeMethod(methodCaptor.capture());
 
         GetMethod getMethod = methodCaptor.getValue();
         Matcher matcher = findLimitOffset(getMethod);
         assertEquals("200", matcher.group(LIMIT));
         assertEquals("4000", matcher.group(OFFSET));
 
         verify(requestTimer).start();
         verify(requestTimer).successful();
         verify(requestTimer, never()).failed();
         verify(requestTimer, never()).retried();
     }
 
     @Test
     public void shouldOverridePaginationOptionIfAlreadyPresentForForms() throws IOException {
         String receivedOn = DateTime.now().toString();
         Map<String,String> parameters = new HashMap<String,String>(){{
             put(Constants.LIMIT, "1000");
             put(Constants.OFFSET, "2000");
         }};
         Page paginationOption = new Page(4000, 200);
         when(httpClient.executeMethod(any(GetMethod.class))).thenAnswer(new CommcareRequestAnswer(getCaseResponseWithHeaderField("received_on", receivedOn)));
         commcareAPIHttpClient.fetchForms(parameters, paginationOption);
 
         ArgumentCaptor<GetMethod> methodCaptor = ArgumentCaptor.forClass(GetMethod.class);
         verify(httpClient).executeMethod(methodCaptor.capture());
 
         GetMethod getMethod = methodCaptor.getValue();
         Matcher matcher = findLimitOffset(getMethod);
         assertEquals("200", matcher.group(LIMIT));
         assertEquals("4000", matcher.group(OFFSET));
 
         verify(requestTimer).start();
         verify(requestTimer).successful();
         verify(requestTimer, never()).failed();
         verify(requestTimer, never()).retried();
     }
 
     private Matcher findLimitOffset(GetMethod getMethod) {
         String queryString = getMethod.getQueryString();
         Pattern pattern = Pattern.compile("limit=(\\d+)&offset=(\\d+)");
         Matcher matcher = pattern.matcher(queryString);
         matcher.find();
         return matcher;
     }
 
     private class CommcareRequestAnswer implements Answer {
         private String response;
 
         private CommcareRequestAnswer(String response) {
             this.response = response;
         }
 
         private void setResponse(HttpMethod method) {
             try {
                 Field privateResponseStream = HttpMethodBase.class.getDeclaredField("responseStream");
                 privateResponseStream.setAccessible(true);
                 privateResponseStream.set(method, IOUtils.toInputStream(response));
             } catch (Exception e) {
                 throw new RuntimeException("Exception Setting response stream", e);
             }
         }
 
         @Override
         public Object answer(InvocationOnMock invocation) throws Throwable {
             GetMethod method = (GetMethod) invocation.getArguments()[0];
             setResponse(method);
             return HttpStatus.SC_OK;
         }
     }
 
     private String getFormResponseWithHeaderField(String headerField, String headerValue) {
 
         String field = "";
         if (headerField != null)
             field = String.format("\"%s\": \"%s\",\n", headerField, headerValue);
         return "{\n" +
                 "\"archived\": false,\n" +
                 "\"form\": {},\n" +
                 "\"id\": \"c24d1bc8-ed4d-4c04-ab67-1fb394f2db58\",\n" +
                 "\"md5\": \"OBSOLETED\",\n" +
                 "\"metadata\": {},\n" +
                 field +
                 "\"resource_uri\": \"\",\n" +
                 "\"type\": \"data\",\n" +
                 "\"uiversion\": \"1\",\n" +
                 "\"version\": \"1\"\n" +
                 "}";
     }
 
     private String getCaseResponseWithHeaderField(String headerField, String headerValue) {
         String field = "";
         if (headerField != null)
             field = String.format("\"%s\": \"%s\",\n", headerField, headerValue);
 
         return "{\n" +
                 "\"case_id\": \"361816d9-a98e-41d9-9d67-af1e18a26ea7\",\n" +
                 "\"closed\": true,\n" +
                 "\"date_closed\": null,\n" +
                 "\"date_modified\": \"1900-01-01\",\n" +
                 "\"id\": \"361816d9-a98e-41d9-9d67-af1e18a26ea7\",\n" +
                 "\"indices\": {\n" +
                 "    \"mother_id\": {\n" +
                 "        \"case_id\": \"dafadd3f-5f76-44ee-8734-36e49cda189a\",\n" +
                 "        \"case_type\": \"cc_bihar_pregnancy\"\n" +
                 "    }\n" +
                 "},\n" +
                 "\"properties\": {\n" +
                 "        \"case_name\": \"प्रमोद मुखिया\",\n" +
                 "        \"case_type\": \"cc_bihar_newborn\",\n" +
                 "        \"owner_id\": \"89fda0284e008d2e0c980fb13fc0e87f\"\n" +
                 "},\n" +
                 "\"resource_uri\": \"\",\n" +
                 "\"%s\": \"%s\",\n" +
                 field +
                 "\"user_id\": \"89fda0284e008d2e0c980fb13f9e0967\"\n" +
                 "}";
     }
 
     @Test
     public void shouldEnsureThatCaseAndFormUrlsEndWithSlash() {
         //Or else Redirection will result in double encoding of any "/" (slash) in the parameters.
         assertTrue(commcareAPIHttpClient.getCommcareCaseListUrl().endsWith("/"));
         assertTrue(commcareAPIHttpClient.getCommcareFormListUrl().endsWith("/"));
     }
 }
