 package org.motechproject.care.reporting.migration.task;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.motechproject.care.reporting.migration.MigratorArguments;
 import org.motechproject.care.reporting.migration.common.CommcareResponseWrapper;
 import org.motechproject.care.reporting.migration.common.Page;
 import org.motechproject.care.reporting.migration.common.PaginatedResponse;
 import org.motechproject.care.reporting.migration.common.PaginatedResponseMeta;
 import org.motechproject.care.reporting.migration.common.ResponseParser;
 import org.motechproject.care.reporting.migration.statistics.MigrationStatisticsCollector;
 import org.motechproject.care.reporting.migration.util.CaseXmlPair;
 import org.motechproject.care.reporting.migration.util.CommcareAPIHttpClient;
 import org.motechproject.care.reporting.migration.util.CommcareDataUtil;
 import org.motechproject.care.reporting.migration.util.MotechAPIHttpClient;
 import org.unitils.reflectionassert.ReflectionAssert;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static junit.framework.Assert.assertEquals;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.anyMap;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.motechproject.care.reporting.migration.common.Constants.CASE_END_DATE;
 import static org.motechproject.care.reporting.migration.common.Constants.CASE_START_DATE;
 import static org.motechproject.care.reporting.migration.common.Constants.CASE_TYPE;
 import static org.motechproject.care.reporting.migration.common.Constants.CASE_VERSION;
 import static org.motechproject.care.reporting.migration.common.Constants.END_DATE;
 import static org.motechproject.care.reporting.migration.common.Constants.LIMIT;
 import static org.motechproject.care.reporting.migration.common.Constants.OFFSET;
 import static org.motechproject.care.reporting.migration.common.Constants.START_DATE;
 import static org.motechproject.care.reporting.migration.common.Constants.TYPE;
 import static org.motechproject.care.reporting.migration.common.Constants.VERSION;
 
 public class CaseMigrationTaskTest {
     @Mock
     private CommcareAPIHttpClient commcareAPIHttpClient;
     @Mock
     private MotechAPIHttpClient motechAPIHttpClient;
     @Mock
     private ResponseParser parser;
     @Mock
     private MigratorArguments migratorArguments;
     @Mock
     private MigrationStatisticsCollector statisticsCollector;
     @Mock
     private CommcareDataUtil commcareDataUtil;
 
     @Before
     public void setUp() throws Exception {
         initMocks(this);
     }
 
     @Test
     public void shouldFetchCaseWithParameters() {
         DateTime now = DateTime.now();
         Map<String, Object> optionsMap = new HashMap<>();
         optionsMap.put(TYPE, "cc_bihar_pregnancy");
         optionsMap.put(VERSION, "version");
         optionsMap.put(START_DATE, now.toDate().toString());
         optionsMap.put(END_DATE, now.toDate().toString());
         optionsMap.put(LIMIT, 100);
         optionsMap.put(OFFSET, 2000);
         MigrationTask caseMigrationTask = new CaseMigrationTask(commcareAPIHttpClient, motechAPIHttpClient, parser, statisticsCollector, new CommcareDataUtil());
         when(migratorArguments.getOptions()).thenReturn(optionsMap);
 
         Map<String, String> expectedNameValuePairs = new HashMap<>();;
         expectedNameValuePairs.put(CASE_TYPE, "cc_bihar_pregnancy");
         expectedNameValuePairs.put(CASE_VERSION, "version");
         expectedNameValuePairs.put(CASE_START_DATE, now.toDate().toString());
         expectedNameValuePairs.put(CASE_END_DATE, now.toDate().toString());
         expectedNameValuePairs.put(LIMIT, String.valueOf(100));
         expectedNameValuePairs.put(OFFSET, String.valueOf(2000));
 
         PaginatedResponse paginatedResult = new PaginatedResponse(new JsonArray(), new PaginatedResponseMeta(null, null, null, 0));
         when(parser.parse("someresponse")).thenReturn(paginatedResult);
         when(commcareAPIHttpClient.fetchCases(any(Map.class), any(Page.class))).thenReturn("someresponse");
 
         caseMigrationTask.migrate(migratorArguments);
 
         ArgumentCaptor<Map> parameterCaptor = ArgumentCaptor.forClass(Map.class);
         verify(commcareAPIHttpClient).fetchCases(parameterCaptor.capture(), any(Page.class));
 
         ReflectionAssert.assertLenientEquals(expectedNameValuePairs, parameterCaptor.getValue());
 
         verify(statisticsCollector).addRecordsDownloaded(0);
         verify(statisticsCollector).addRecordsUploaded(0);
     }
 
     @Test
     public void shouldFetchPaginatedCasesWithParameters() {
         String caseResponse1 = "response1";
         JsonArray jsonResponse1 = getCaseJson("2013-10-30", 1);
         when(parser.parse(caseResponse1)).thenReturn(new PaginatedResponse(jsonResponse1, new PaginatedResponseMeta(new Page(0, 100), new Page(100, 100), null, 100)));
         String caseResponse2 = "response2";
         JsonArray jsonResponse2 = getCaseJson("2013-12-13", 1);
         when(parser.parse(caseResponse2)).thenReturn(new PaginatedResponse(jsonResponse2, new PaginatedResponseMeta(new Page(0, 100), null, null, 100)));
         when(commcareAPIHttpClient.fetchCases(anyMap(), any(Page.class))).thenReturn(caseResponse1).thenReturn(caseResponse2).thenReturn(null);
         MigrationTask caseMigrationTask = new CaseMigrationTask(commcareAPIHttpClient, motechAPIHttpClient, parser, statisticsCollector, new CommcareDataUtil());
 
         caseMigrationTask.migrate(migratorArguments);
 
         ArgumentCaptor<Map> parameterCaptor = ArgumentCaptor.forClass(Map.class);
         ArgumentCaptor<Page> optionCaptor = ArgumentCaptor.forClass(Page.class);
         ArgumentCaptor<CommcareResponseWrapper> caseReponseCaptor = ArgumentCaptor.forClass(CommcareResponseWrapper.class);
 
         verify(commcareAPIHttpClient, times(2)).fetchCases(parameterCaptor.capture(), optionCaptor.capture());
         List<Page> actualOptions = optionCaptor.getAllValues();
         ReflectionAssert.assertReflectionEquals(new Page(0, 100), actualOptions.get(0));
         ReflectionAssert.assertReflectionEquals(new Page(100, 100), actualOptions.get(1));
 
         verify(motechAPIHttpClient, times(2)).postCase(caseReponseCaptor.capture());
         List<CommcareResponseWrapper> actualForms = caseReponseCaptor.getAllValues();
         assertEquals("2013-10-30", actualForms.get(0).getHeaders().get("server-modified-on"));
         assertEquals("2013-12-13", actualForms.get(1).getHeaders().get("server-modified-on"));
 
         ArgumentCaptor<Integer> recordsDownloadedCountCaptor = ArgumentCaptor.forClass(Integer.class);
         ArgumentCaptor<Integer> recordsUploadedCountCaptor = ArgumentCaptor.forClass(Integer.class);
 
         verify(statisticsCollector, times(2)).addRecordsDownloaded(recordsDownloadedCountCaptor.capture());
         verify(statisticsCollector, times(2)).addRecordsUploaded(recordsUploadedCountCaptor.capture());
         List<Integer> recordsDownloadedCounts = recordsDownloadedCountCaptor.getAllValues();
         List<Integer> recordsUploadedCounts = recordsUploadedCountCaptor.getAllValues();
         assertEquals(Integer.valueOf(1), recordsDownloadedCounts.get(0));
         assertEquals(Integer.valueOf(1), recordsDownloadedCounts.get(1));
         assertEquals(Integer.valueOf(1), recordsUploadedCounts.get(0));
         assertEquals(Integer.valueOf(1), recordsUploadedCounts.get(1));
 
     }
 
     @Test
     public void shouldPostMultipleTimesForMultipleCases() {
         String caseResponse1 = "response1";
         JsonArray jsonResponse1 = getCaseJson("2013-10-30", 2);
         when(parser.parse(caseResponse1)).thenReturn(new PaginatedResponse(jsonResponse1, new PaginatedResponseMeta(null, null, null, 0)));
         when(commcareAPIHttpClient.fetchCases(anyMap(), any(Page.class))).thenReturn(caseResponse1).thenReturn(null);
         MigrationTask caseMigrationTask = new CaseMigrationTask(commcareAPIHttpClient, motechAPIHttpClient, parser, statisticsCollector, new CommcareDataUtil());
 
         caseMigrationTask.migrate(migratorArguments);
 
         ArgumentCaptor<CommcareResponseWrapper> caseReponseCaptor = ArgumentCaptor.forClass(CommcareResponseWrapper.class);
 
         verify(commcareAPIHttpClient).fetchCases(anyMap(), any(Page.class));
 
         verify(motechAPIHttpClient, times(2)).postCase(caseReponseCaptor.capture());
         List<CommcareResponseWrapper> actualForms = caseReponseCaptor.getAllValues();
         assertEquals("2013-10-30", actualForms.get(0).getHeaders().get("server-modified-on"));
         assertEquals("2013-10-30", actualForms.get(1).getHeaders().get("server-modified-on"));
 
         verify(statisticsCollector).addRecordsDownloaded(2);
         verify(statisticsCollector).addRecordsUploaded(2);
     }
 
     @Test
     public void shouldPostClosedCaseAlsoForClosed() {
         String caseResponse1 = "response1";
         JsonArray jsonResponse1 = getClosedCase();
         when(parser.parse(caseResponse1)).thenReturn(new PaginatedResponse(jsonResponse1, new PaginatedResponseMeta(null, null, null, 0)));
         when(commcareAPIHttpClient.fetchCases(anyMap(), any(Page.class))).thenReturn(caseResponse1).thenReturn(null);
         MigrationTask caseMigrationTask = new CaseMigrationTask(commcareAPIHttpClient, motechAPIHttpClient, parser, statisticsCollector, new CommcareDataUtil());
 
         caseMigrationTask.migrate(migratorArguments);
 
         ArgumentCaptor<CommcareResponseWrapper> caseReponseCaptor = ArgumentCaptor.forClass(CommcareResponseWrapper.class);
 
         verify(commcareAPIHttpClient).fetchCases(anyMap(), any(Page.class));
 
         verify(motechAPIHttpClient, times(2)).postCase(caseReponseCaptor.capture());
         List<CommcareResponseWrapper> actualForms = caseReponseCaptor.getAllValues();
         assertEquals("2013-10-30", actualForms.get(0).getHeaders().get("server-modified-on"));
         assertEquals("2013-10-30", actualForms.get(1).getHeaders().get("server-modified-on"));
 
         verify(statisticsCollector).addRecordsDownloaded(1);
         verify(statisticsCollector).addRecordsUploaded(2);
     }
 
 
     @Test
     public void shouldProcessCasesWhichHaveClosedActionFirst() {
         MigrationTask caseMigrationTask = new CaseMigrationTask(commcareAPIHttpClient, motechAPIHttpClient, parser, statisticsCollector, commcareDataUtil);

        JsonObject jsonObject1 = new JsonObject();
        jsonObject1.addProperty("prop", "value1");
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.addProperty("prop", "value2");
        JsonObject jsonObject3 = new JsonObject();
        jsonObject3.addProperty("prop", "value3");
        JsonObject jsonObject4 = new JsonObject();
        jsonObject4.addProperty("prop", "value4");

         JsonArray jsonArray = new JsonArray();

         jsonArray.add(jsonObject1);
         jsonArray.add(jsonObject2);
         jsonArray.add(jsonObject3);
         jsonArray.add(jsonObject4);
 
         when(commcareDataUtil.toCaseXml(jsonObject1)).thenReturn(new CaseXmlPair("createUpdate1", null));
        when(commcareDataUtil.toCaseXml(jsonObject2)).thenReturn(new CaseXmlPair("createUpdate2", "close2"));
        when(commcareDataUtil.toCaseXml(jsonObject3)).thenReturn(new CaseXmlPair("createUpdate3", "close3"));
        when(commcareDataUtil.toCaseXml(jsonObject4)).thenReturn(new CaseXmlPair("createUpdate4", null));

         List<CommcareResponseWrapper> commcareResponseWrappers = caseMigrationTask.convertToEntity(jsonArray);
         assertEquals("createUpdate2", commcareResponseWrappers.get(0).getResponseBody());
         assertEquals("createUpdate3", commcareResponseWrappers.get(1).getResponseBody());
         assertEquals("createUpdate1", commcareResponseWrappers.get(2).getResponseBody());
         assertEquals("createUpdate4", commcareResponseWrappers.get(3).getResponseBody());
         assertEquals("close2", commcareResponseWrappers.get(4).getResponseBody());
         assertEquals("close3", commcareResponseWrappers.get(5).getResponseBody());
     }
 
 
     private JsonArray getCaseJson(String serverModifiedOn, int sizeOfArray) {
         String caseJson = getCaseJson(serverModifiedOn);
         JsonParser jsonParser = new JsonParser();
         JsonElement aCase = jsonParser.parse(caseJson);
         JsonArray caseJsons = new JsonArray();
         for (int i = 1; i <= sizeOfArray; i++) {
             caseJsons.add(aCase);
         }
         return caseJsons;
     }
 
     private JsonArray getClosedCase() {
         JsonParser jsonParser = new JsonParser();
         JsonElement aCase = jsonParser.parse(getClosedCaseJson());
         JsonArray jsonElements = new JsonArray();
         jsonElements.add(aCase);
         return jsonElements;
     }
 
     private String getClosedCaseJson() {
         return "{\n" +
                 "            \"case_id\": \"757537e3-6406-410e-8206-5d92226fe6a3\",\n" +
                 "            \"closed\": true,\n" +
                 "            \"date_closed\": \"2012-12-31 22:14:27\",\n" +
                 "            \"date_modified\": \"1980-12-31 22:14:27\",\n" +
                 "            \"id\": \"757537e3-6406-410e-8206-5d92226fe6a3\",\n" +
                 "            \"indices\": {},\n" +
                 "            \"properties\": {\n" +
                 "                \"case_name\": \"Rubi devi\",\n" +
                 "                \"case_type\": \"cc_bihar_pregnancy\",\n" +
                 "                \"date_last_visit\": \"1980-12-31\",\n" +
                 "                \"date_next_reg\": \"1980-12-31\",\n" +
                 "                \"date_opened\": \"1980-12-31T22:14:27\",\n" +
                 "                \"external_id\": null,\n" +
                 "                \"family_number\": \"5\",\n" +
                 "                \"hh_number\": \"4\",\n" +
                 "                \"husband_name\": \"Sobha pasban\",\n" +
                 "                \"last_visit_type\": \"new\",\n" +
                 "                \"mother_alive\": \"yes\",\n" +
                 "                \"mother_dob\": \"\",\n" +
                 "                \"mother_name\": \"Rubi devi\",\n" +
                 "                \"owner_id\": \"demo_user_group_bihar\"\n" +
                 "            },\n" +
                 "            \"resource_uri\": \"\",\n" +
                 "            \"server_date_modified\": \"2013-10-30\",\n" +
                 "            \"server_date_opened\": null,\n" +
                 "            \"user_id\": \"demo_user\",\n" +
                 "            \"xform_ids\": [\n" +
                 "                \"beacfceb-ef1e-4f46-a296-3b66e07f667f\"\n" +
                 "            ]\n" +
                 "        }";
     }
 
     private String getCaseJson(String serverModifiedOn) {
         return "{\n" +
                 "            \"case_id\": \"757537e3-6406-410e-8206-5d92226fe6a3\",\n" +
                 "            \"closed\": false,\n" +
                 "            \"date_closed\": null,\n" +
                 "            \"date_modified\": \"1980-12-31 22:14:27\",\n" +
                 "            \"id\": \"757537e3-6406-410e-8206-5d92226fe6a3\",\n" +
                 "            \"indices\": {},\n" +
                 "            \"properties\": {\n" +
                 "                \"case_name\": \"Rubi devi\",\n" +
                 "                \"case_type\": \"cc_bihar_pregnancy\",\n" +
                 "                \"date_last_visit\": \"1980-12-31\",\n" +
                 "                \"date_next_reg\": \"1980-12-31\",\n" +
                 "                \"date_opened\": \"1980-12-31T22:14:27\",\n" +
                 "                \"external_id\": null,\n" +
                 "                \"family_number\": \"5\",\n" +
                 "                \"hh_number\": \"4\",\n" +
                 "                \"husband_name\": \"Sobha pasban\",\n" +
                 "                \"last_visit_type\": \"new\",\n" +
                 "                \"mother_alive\": \"yes\",\n" +
                 "                \"mother_dob\": \"\",\n" +
                 "                \"mother_name\": \"Rubi devi\",\n" +
                 "                \"owner_id\": \"demo_user_group_bihar\"\n" +
                 "            },\n" +
                 "            \"resource_uri\": \"\",\n" +
                 "            \"server_date_modified\": \"" + serverModifiedOn + "\",\n" +
                 "            \"server_date_opened\": null,\n" +
                 "            \"user_id\": \"demo_user\",\n" +
                 "            \"xform_ids\": [\n" +
                 "                \"beacfceb-ef1e-4f46-a296-3b66e07f667f\"\n" +
                 "            ]\n" +
                 "        }";
     }
 }
