 package org.corespring;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.github.tomakehurst.wiremock.junit.WireMockRule;
 import com.google.common.collect.Iterables;
 import org.corespring.authentication.AccessTokenProvider;
 import org.corespring.resource.*;
 import org.corespring.resource.player.Mode;
 import org.corespring.resource.player.Options;
 import org.corespring.resource.player.OptionsResponse;
 import org.corespring.resource.player.Role;
 import org.corespring.resource.question.Answer;
 import org.corespring.resource.question.ItemType;
 import org.corespring.resource.question.Participant;
 import org.corespring.resource.question.Settings;
 import org.corespring.rest.CorespringRestException;
 import org.corespring.rest.ItemQuery;
 import org.junit.Rule;
 import org.junit.Test;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import static org.corespring.resource.question.ItemType.*;
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 public class CorespringClientTest {
 
  private String clientId = "505c4eb7e4b04aa483da6e17";
  private String clientSecret = "euo3cs4h5ychx15t7mfxvlfoh";
 
   private CorespringClient client = new CorespringClient(clientId, clientSecret);
 
   {
    client.setEndpoint("http://localhost:9000");
   }
 
//  private String clientId = "524c5cb5300401522ab21db1";
//  private String clientSecret = "325hm11xiz7ykeen2ibt";
//
//  private CorespringClient client = new CorespringClient(clientId, clientSecret);
//
//  {
//    client.setEndpoint("http://localhost:8089");
//  }

   private final Organization organization =
       new Organization("51114b307fc1eaa866444648", "Demo Organization", new ArrayList<String>(), false);
 
   @Rule
   public WireMockRule wireMockRule = new WireMockRule(8089);
 
   @Test
   public void testGetOrganizations() throws CorespringRestException {
     assertOrganizationsAreCorrect(client.getOrganizations());
   }
 
   private void assertOrganizationsAreCorrect(Collection<Organization> organizations) {
     assertEquals(1, organizations.size());
     Organization organization = organizations.iterator().next();
 
     assertEquals("Demo Organization", organization.getName());
     assertEquals("51114b307fc1eaa866444648", organization.getId());
   }
 
   @Test
   public void testGetAssessments() throws CorespringRestException {
     Collection<Assessment> assessments = client.getAssessments(organization);
     for (Assessment assessment : assessments) {
       checkAssessment(assessment);
     }
   }
 
   @Test
   public void testGetAssessmentById() throws CorespringRestException {
     Assessment assessment = client.getAssessmentById("000000000000000000000002");
     checkAssessment(assessment);
   }
 
   private void checkAssessment(Assessment assessment) {
     if (!assessment.getQuestions().isEmpty()) {
       Collection<Question> questions = assessment.getQuestions();
       for (Question question : questions) {
         assertNotNull(question.getTitle());
         assertNotNull(question.getItemId());
         assertNotNull(question.getSettings());
         assertNotNull(question.getStandards());
       }
 
       Collection<Participant> participants = assessment.getParticipants();
       for (Participant participant : participants) {
         assertNotNull(participant.getAnswers());
         assertNotNull(participant.getExternalUid());
       }
     }
   }
 
 
   @Test
   public void testGetAssessmentTemplates() throws CorespringRestException {
     Collection<AssessmentTemplate> assessmentTemplates = client.getAssessmentTemplates();
     for (AssessmentTemplate assessmentTemplate : assessmentTemplates) {
       checkAssessmentTemplate(assessmentTemplate);
     }
   }
 
   private void checkAssessmentTemplate(AssessmentTemplate assessmentTemplate) {
     assertNotNull(assessmentTemplate.getId());
     assertNotNull(assessmentTemplate.getCollectionId());
     if (!assessmentTemplate.getQuestions().isEmpty()) {
       Collection<Question> questions = assessmentTemplate.getQuestions();
       for (Question question : questions) {
         assertNotNull(question.getTitle());
         assertNotNull(question.getItemId());
         assertNotNull(question.getSettings());
         assertNotNull(question.getStandards());
       }
     }
   }
 
   @Test
   public void testCreateAssessment() throws CorespringRestException {
     Assessment assessment = new Assessment.Builder().title("My new assessment!").addMetadata("authorId", "fd707fc3c").build();
     Assessment updatedAssessment = client.create(assessment);
 
     // Created assessment has id and orgId
     assertNotNull(updatedAssessment.getId());
     assertNotNull(updatedAssessment.getOrgId());
     assertEquals(updatedAssessment.getMetadataValue("authorId"), "fd707fc3c");
   }
 
   @Test
   public void testCreateAssessmentTemplate() throws CorespringRestException {
     AssessmentTemplate assessmentTemplate = new AssessmentTemplate.Builder().collectionId("51114b127fc1eaa866444647").addMetadata("authorId", "fd707fc3c").build();
     AssessmentTemplate updatedAssessmentTemplate = client.create(assessmentTemplate);
 
     assertNotNull(updatedAssessmentTemplate.getId());
     assertNotNull(updatedAssessmentTemplate.getCollectionId());
     assertEquals(updatedAssessmentTemplate.getMetadataValue("authorId"), "fd707fc3c");
   }
 
   @Test
   public void testAddParticipants() throws CorespringRestException, JsonProcessingException {
     Assessment assessment = new Assessment.Builder().title("My new assessment!").build();
     assessment = client.create(assessment);
     assessment = client.addParticipant(assessment, "ben1234");
     assertNotNull(assessment.getParticipant("ben1234"));
   }
 
   @Test
   public void testUpdateAssessment() throws CorespringRestException {
     Assessment assessment = new Assessment.Builder().title("My new assessment!").build();
 
     assessment = client.create(assessment);
     assessment = client.update(new Assessment.Builder(assessment).description("description").build());
 
     assertEquals("description", assessment.getDescription());
   }
 
   @Test
   public void testDeleteAssessment() throws CorespringRestException {
     Assessment assessment = new Assessment.Builder().title("My new assessment!").build();
 
     assessment = client.create(assessment);
     assertNotNull(assessment);
     assessment = client.delete(assessment);
 
     assertNull(assessment);
   }
 
   @Test
   public void testAddAnswer() throws CorespringRestException {
     String externalUid = "ben1234";
     String itemId = "527a3bbe8808335f66e168a5";
     String sessionId = "527a3bca8808335f66e168a6";
 
     Assessment assessment = new Assessment.Builder().title("My new assessment!").build();
 
     assessment = client.create(assessment);
     assessment = client.addParticipant(assessment, externalUid);
 
     Answer answer = new Answer.Builder().itemId(itemId).sessionId(sessionId).build();
     assessment = client.addAnswer(assessment, answer, externalUid);
 
     assertNotNull(assessment.getParticipant(externalUid));
     assertNotNull(assessment.getParticipant(externalUid).getAnswer(itemId));
     assertEquals(sessionId, assessment.getParticipant(externalUid).getAnswer(itemId).getSessionId());
   }
 
   @Test
   public void testGetAssessmentsByAuthor() throws CorespringRestException {
     Collection<Assessment> assessments = client.getAssessmentsByAuthor("fd707fc3c");
     assertEquals(assessments.size(), 1);
   }
 
   @Test
   public void testCreateItemSession() throws CorespringRestException, JsonProcessingException {
 
     ItemSession itemSession = new ItemSession.Builder().itemId("50083ba9e4b071cb5ef79101").build();
     itemSession = client.create(itemSession);
 
     assertNotNull(itemSession);
     assertNotNull(itemSession.getId());
   }
 
   @Test
   public void testUpdateSettings() throws CorespringRestException {
     ItemSession itemSession = new ItemSession.Builder().itemId("50083ba9e4b071cb5ef79101").build();
     itemSession = client.create(itemSession);
     Settings newSettings = new Settings.Builder(itemSession.getSettings()).showFeedback(false).build();
     itemSession = new ItemSession.Builder(itemSession).settings(newSettings).build();
     itemSession = client.updateSettings(itemSession);
 
     assertFalse(itemSession.getSettings().getShowFeedback());
   }
 
   @Test
   public void testGetCollections() throws CorespringRestException {
     Collection<ContentCollection> collections = client.getCollections();
     for (ContentCollection collection : collections) {
       checkCollection(collection);
     }
   }
 
   private void checkCollection(ContentCollection collection) {
     assertNotNull(collection.getId());
     assertNotNull(collection.getName());
     assertNotNull(collection.isPublic());
   }
 
   @Test
   public void testGetItemTypesByCollections() throws CorespringRestException {
     Collection<ContentCollection> collections = client.getCollections();
 
     assertTrue(Iterables.elementsEqual(
         client.getItemTypesByCollections(collections),
         new ArrayList<ItemType>() {
           {
             add(MULTI_CHOICE);
             add(SHORT_ANSWER);
             add(OPEN_ENDED);
             add(PASSAGE_WITH_QUESTIONS_EVIDENCE);
             add(COMPOSITE_PERFORMANCE);
             add(COMPOSITE_MULTI_MULTI_CHOICE);
             add(MULTI_MULTI_CHOICE);
             add(ORDERING);
             add(DRAG_AND_DROP);
             add(INLINE_CHOICE);
             add(COMPOSITE_MULTI_CHOICE_AND_SHORT_ANSWER);
             add(COMPOSITE_MULTI_CHOICE_SHORT_ANSWER_AND_OPEN_ENDED);
             add(COMPOSITE_ACTIVITY);
             add(TEXT_EVIDENCE);
             add(VISUAL_MULTI_CHOICE);
           }
         }
     ));
   }
 
   @Test
   public void testGetContributorsByCollections() throws CorespringRestException {
     Collection<ContentCollection> collections = client.getCollections();
     
     assertTrue(Iterables.elementsEqual(
         client.getContributorsByCollections(collections),
         new ArrayList<String>() {
           {
             add("State of New Jersey Department of Education");
             add("New England Common Assessment Program");
             add("TIMSS");
             add("Illustrative Mathematics");
             add("New York State Education Department");
             add("Kentucky Department of Education");
             add("Mathematics Assessment Resource Service");
             add("Smarter Balanced Assessment Consortium");
             add("Institute of Education Sciences National Center for Education Statistics");
             add("PARCC");
           }
         }
     ));
   }
 
   @Test
   public void testGetFieldValuesByCollection() throws CorespringRestException {
     Map<String, Collection<String>> fieldValues = client.getFieldValuesByCollection("4ff5abe2e4b0e3bfeb9d2011");
 
     assertTrue(Iterables.elementsEqual(
         fieldValues.keySet(),
         new ArrayList<String>() {
           { add("gradeLevel"); add("bloomsTaxonomy"); add("keySkill"); add("itemType"); add("demonstratedKnowledge"); }
         }
     ));
 
     assertTrue(Iterables.elementsEqual(
         fieldValues.get("gradeLevel"),
         new ArrayList<String>() { { add("04"); add("08"); add("10"); add("11"); add("AP"); } }
     ));
 
     assertTrue(Iterables.elementsEqual(
         fieldValues.get("bloomsTaxonomy"),
         new ArrayList<String>() { { add("Analyzing"); add("Applying"); add("Remembering"); add("Understanding"); } }
     ));
 
     assertTrue(Iterables.elementsEqual(
         fieldValues.get("keySkill"),
         new ArrayList<String>() { { add("Analyze"); add("Choose"); add("Classify"); add("Compare"); add("Compute");
           add("Contrast"); add("Convert"); add("Define"); add("Describe"); add("Discuss"); add("Examine");
           add("Explain"); add("Express"); add("Give"); add("Identify"); add("Indicate"); add("Infer"); add("List");
           add("Predict"); add("Recall"); add("Relate"); add("Review"); add("Select"); add("classify"); add("explain");
           add("identify"); add("state");
         } }
     ));
 
     assertTrue(Iterables.elementsEqual(
         fieldValues.get("itemType"),
         new ArrayList<String>() { {
           add("Constructed Response - Open Ended"); add("Constructed Response - Short Answer"); add("Multiple Choice");
           add("Performance");
         } }
     ));
 
     assertTrue(Iterables.elementsEqual(
         fieldValues.get("demonstratedKnowledge"),
         new ArrayList<String>() { { add("Conceptual"); add("Factual"); add("Procedural"); } }
     ));
   }
 
   @Test
   public void testGetValuesByContributor() throws CorespringRestException {
     Map<String, Collection<String>> fieldValues =
         client.getFieldValuesByContributor("New York State Education Department");
 
     assertTrue(Iterables.elementsEqual(
         fieldValues.keySet(),
         new ArrayList<String>() {
           { add("standard"); add("gradeLevel"); add("bloomsTaxonomy"); add("keySkill"); add("itemType");
             add("demonstratedKnowledge");
           }
         }
     ));
 
     assertTrue(Iterables.elementsEqual(
         fieldValues.get("standard"),
         new ArrayList<String>() {
           {
             add("3.NF.A.1"); add("3.NF.A.2a"); add("3.NF.A.2b"); add("3.NF.A.3b"); add("3.OA.A.1"); add("3.OA.A.2");
             add("3.OA.A.3"); add("3.OA.A.4"); add("3.OA.B.5"); add("3.OA.B.6"); add("3.OA.D.8"); add("3.W.9");
             add("4.MD.A.3"); add("4.MD.B.4"); add("4.NF.B.3d"); add("4.NF.B.4c"); add("4.OA.A.2"); add("4.OA.A.3");
             add("4.OA.C.5"); add("5.G.A.2"); add("5.MD.A.1"); add("5.MD.C.5b"); add("5.NBT.A.1"); add("5.NBT.A.2");
             add("5.NBT.A.3"); add("5.NBT.A.3a"); add("5.NF.A.1"); add("5.NF.B.7c"); add("5.OA.A.2"); add("6.EE.A.1");
             add("6.EE.A.2a"); add("6.EE.A.2b"); add("6.EE.A.2c"); add("6.EE.A.3"); add("6.EE.B.7"); add("6.G.A.1");
             add("6.G.A.3"); add("6.G.A.4"); add("6.RP.A.1"); add("6.RP.A.2"); add("6.RP.A.3b"); add("6.RP.A.3c");
             add("6.RP.A.3d"); add("6.SP.B.4"); add("7.EE.A.1"); add("7.EE.B.3"); add("7.EE.B.4b"); add("7.G.A.1");
             add("7.NS.A.1d"); add("7.NS.A.2c"); add("7.RP.A.2b"); add("7.RP.A.2c"); add("7.RP.A.3"); add("7.SP.C.8b");
             add("8.EE.A.1"); add("8.EE.A.4"); add("8.EE.B.6"); add("8.EE.C.7b"); add("8.F.A.2"); add("8.F.B.4");
             add("8.G.A.4"); add("L.5.4"); add("L.6.4"); add("L.6.4a"); add("RI.3.1"); add("RI.3.4"); add("RI.3.7");
             add("RI.3.8"); add("RI.4.1"); add("RI.4.2"); add("RI.4.3"); add("RI.4.5"); add("RI.4.8"); add("RI.4.9");
             add("RI.5.2"); add("RI.5.3"); add("RI.6.2"); add("RI.6.4"); add("RI.6.5"); add("RI.7.1"); add("RI.7.2");
             add("RI.7.4"); add("RI.7.5"); add("RI.8.1"); add("RI.8.2"); add("RI.8.4"); add("RI.8.5"); add("RI.8.6");
             add("RL.3.1"); add("RL.3.3"); add("RL.3.4"); add("RL.3.5"); add("RL.4.1"); add("RL.4.2"); add("RL.4.3");
             add("RL.4.9"); add("RL.5.1"); add("RL.5.2"); add("RL.5.3"); add("RL.5.4"); add("RL.5.5"); add("RL.5.6");
             add("RL.6.1"); add("RL.6.3"); add("RL.6.4"); add("RL.6.5"); add("RL.6.6"); add("RL.6.9"); add("RL.7.1");
             add("RL.7.2"); add("RL.7.3"); add("RL.7.6"); add("RL.8.2"); add("RL.8.3"); add("RL.8.4"); add("RL.8.6");
             add("W.3.2"); add("W.3.4"); add("W.4.2"); add("W.4.4"); add("W.5.2"); add("W.6.1"); add("W.6.4");
             add("W.6.8"); add("W.6.9a"); add("W.6.9b"); add("W.7.2"); add("W.7.9"); add("W.8.2"); add("W.8.9");
           }
         }
     ));
 
     assertTrue(Iterables.elementsEqual(
         fieldValues.get("gradeLevel"),
         new ArrayList<String>() { { add("03"); add("04"); add("05"); add("06"); add("07"); add("08"); } }
     ));
 
     assertTrue(Iterables.elementsEqual(
         fieldValues.get("bloomsTaxonomy"),
         new ArrayList<String>() { { add("Analyzing"); add("Applying"); add("Evaluating"); add("Remembering");
           add("Understanding"); } }
     ));
 
     assertTrue(Iterables.elementsEqual(
         fieldValues.get("keySkill"),
         new ArrayList<String>() {
           {
             add("Analyze"); add("Apply"); add("Appraise"); add("Arrange"); add("Breakdown"); add("Calculate");
             add("Categorize"); add("Choose"); add("Classify"); add("Collect"); add("Compare"); add("Compute");
             add("Contrast"); add("Convert"); add("Create"); add("Defend"); add("Define"); add("Demonstrate");
             add("Describe"); add("Design"); add("Diagram"); add("Discover"); add("Discriminate"); add("Discuss");
             add("Distinguish"); add("Evaluate"); add("Examine"); add("Explain"); add("Express"); add("Extend");
             add("Formulate"); add("Generalize"); add("Generate"); add("Give"); add("Identify"); add("Illustrate");
             add("Indicate"); add("Infer"); add("Interpret"); add("Label"); add("List"); add("Locate"); add("Match");
             add("Model"); add("Name"); add("Operate"); add("Order"); add("Point-Out"); add("Predict"); add("Produce");
             add("Rearrange"); add("Recognize"); add("Relate"); add("Reproduce"); add("Review"); add("Rewrite");
             add("Select"); add("Show"); add("Sketch"); add("Solve"); add("State"); add("Summarize"); add("Translate");
             add("Understand"); add("Write");
           }
         }
     ));
 
     assertTrue(Iterables.elementsEqual(
         fieldValues.get("itemType"),
         new ArrayList<String>() {
           {
             add("Composite - Multiple MC"); add("Constructed Response - Open Ended");
             add("Constructed Response - Short Answer"); add("Multiple Choice"); add("Passage With Questions");
             add("Performance"); add("Visual Multi Choice");
           }
         }
     ));
 
     assertTrue(Iterables.elementsEqual(
        fieldValues.get("demonstratedKnowledge"),
         new ArrayList<String>() { { add("Conceptual"); add("Factual"); add("Metacognitive"); add("Procedural"); } }
     ));
   }
 
   @Test
   public void testFindItems() throws CorespringRestException {
     ItemQuery itemQuery = new ItemQuery.Builder().collection("4ff2e4cae4b077b9e31689fd").gradeLevel("03").itemType("Multiple Choice").build();
     Collection<Item> items = client.findItems(itemQuery);
 
     for (Item item : items) {
       checkItem(item);
     }
   }
 
   @Test
   public void testCountItems() throws CorespringRestException {
     ItemQuery itemQuery = new ItemQuery.Builder().collection("4ff2e4cae4b077b9e31689fd").gradeLevel("03").itemType("Multiple Choice").build();
     int itemCount = client.countItems(itemQuery);
 
     assertEquals(11, itemCount);
   }
 
   private void checkItem(Item item) {
     assertNotNull(item.getId());
     assertNotNull(item.getTitle());
     assertNotNull(item.getCollectionId());
   }
 
   @Test
   public void testAuthTokenRetry() throws CorespringRestException {
     AccessTokenProvider mockAccessTokenProvider = mock(AccessTokenProvider.class);
 
     when(mockAccessTokenProvider.getAccessToken(anyString(), anyString(), anyString()))
         .thenReturn("bad_token").thenReturn("demo_token");
 
     CorespringClient client = new CorespringClient(clientId, clientSecret);
     client.setAccessTokenProvider(mockAccessTokenProvider);
     client.setEndpoint("http://localhost:8089");
 
     assertOrganizationsAreCorrect(client.getOrganizations());
   }
 
   @Test
   public void testEncryptOptions() throws CorespringRestException {
     Options options = new Options.Builder().itemId("*").sessionId("*").mode(Mode.ALL).role(Role.ALL)
         .expiresNever().build();
 
     OptionsResponse optionsResponse = client.encryptOptions(options);
     assertEquals("a369de25bf73cf3479dbcfc76f8c1b4ad983f777fe4834c33e3e57c98d86d31fe9e46bc606a8e3b61c5f2c1b935a6725e9e3cf227f558d3724895ef84ce43107645baf8f53dd068eafc9759b63b1ad44--b4cee74cb43af0d6b652bb044e9f464d",
         optionsResponse.getOptions()
     );
 
     assertEquals("525bf17a92699001e6e81e6d", optionsResponse.getClientId());
   }
 
 }
