 package org.motechproject.ghana.telco.integration;
 
 import org.json.JSONException;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.motechproject.ghana.telco.controller.SubscriptionApiController;
 import org.motechproject.ghana.telco.domain.ProgramType;
 import org.motechproject.ghana.telco.domain.builder.ProgramTypeBuilder;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
 import static org.motechproject.ghana.telco.domain.ProgramType.CHILDCARE;
 import static org.motechproject.ghana.telco.domain.ProgramType.PREGNANCY;
 
 public class SubscriptionApiControllerIntegrationTest extends BaseIntegrationTest {
     @Autowired
     SubscriptionApiController subscriptionApiController;
 
     @Before
     public void setUp() {
         ProgramType pregnancyProgramType = new ProgramTypeBuilder()
                 .withMinWeek(5)
                 .withMaxWeek(35)
                 .withProgramName("Pregnancy")
                 .withProgramKey(PREGNANCY)
                 .withShortCode("P").build();
         ProgramType childCareProgramType = new ProgramTypeBuilder()
                 .withMinWeek(1)
                 .withMaxWeek(52)
                 .withProgramKey(CHILDCARE)
                 .withProgramName("Child Care")
                 .withShortCode("C").build();
 
         allProgramTypes.add(pregnancyProgramType);
         allProgramTypes.add(childCareProgramType);
     }
 
     @Test
     public void shouldRegisterAndSendSuccessJsonResponse() throws JSONException {
         String registerResponse = subscriptionApiController.registerJson("123456", "P", "12");
         assertThat(registerResponse, is("{\"phoneNumber\":\"123456\",\"status\":\"Success\"}"));
     }
 
     @Test
     public void shouldSendErrorJsonResponseIfTheStartTimeIsInvalid() throws JSONException {
         String registerResponse = subscriptionApiController.registerJson("123456", "C", "13");
         assertThat(registerResponse, is("{\"phoneNumber\":\"123456\",\"reason\":\"Start Time is not valid\",\"status\":\"Failed\"}"));
     }
 
     @Test
     public void shouldRegisterAndSendSuccessXmlResponse() throws JSONException {
         String registerResponse = subscriptionApiController.registerXml("123456", "P", "12");
         assertThat(registerResponse, is("<Response>\n" +
                 "    <Status>\n" +
                 "        Success\n" +
                 "    </Status>\n" +
                 "    <UserDetails>\n" +
                 "        <PhoneNumber>123456</PhoneNumber>\n" +
                 "            </UserDetails>\n" +
                 "    </Response>"));
     }
 
     @Test
     public void shouldSendErrorXmlResponseIfTheStartTimeIsInvalid() throws JSONException {
         String registerResponse = subscriptionApiController.registerXml("123456", "C", "13");
         assertThat(registerResponse, is("<Response>\n" +
                 "    <Status>\n" +
                 "        Failed\n" +
                 "    </Status>\n" +
                 "    <UserDetails>\n" +
                 "        <PhoneNumber>123456</PhoneNumber>\n" +
                 "            </UserDetails>\n" +
                 "            <Reason>Start Time is not valid</Reason>\n" +
                 "    </Response>"));
     }
 
     @Test
     public void shouldUnRegisterAndSendSuccessJsonResponse() throws JSONException {
         subscriptionApiController.registerXml("123456", "P", "12");
         String registerResponse = subscriptionApiController.unRegisterJson("123456", "P");
         assertThat(registerResponse, is("{\"phoneNumber\":\"123456\",\"status\":\"Success\"}"));
     }
 
     @Test
     public void shouldSendErrorJsonResponseIfTheUserIsNotRegisteredAndTriedToUnRegister() throws JSONException {
         String registerResponse = subscriptionApiController.unRegisterJson("123451231236", "C");
         assertThat(registerResponse, is("{\"phoneNumber\":\"123451231236\",\"reason\":\"Not registered\",\"status\":\"Failed\"}"));
     }
 
 
     @Test
     public void shouldUnRegisterAndSendSuccessXmlResponse() throws JSONException {
         subscriptionApiController.registerXml("123456", "P", "12");
         String registerResponse = subscriptionApiController.unRegisterXml("123456", "P");
         assertThat(registerResponse, is("<Response>\n" +
                 "    <Status>\n" +
                 "        Success\n" +
                 "    </Status>\n" +
                 "    <UserDetails>\n" +
                 "        <PhoneNumber>123456</PhoneNumber>\n" +
                 "            </UserDetails>\n" +
                 "    </Response>"));
     }
 
     @Test
     public void shouldSendErrorXmlResponseIfTheUserIsNotRegisteredAndTriedToUnRegister() throws JSONException {
         String registerResponse = subscriptionApiController.unRegisterXml("12367631236", "C");
         assertThat(registerResponse, is("<Response>\n" +
                 "    <Status>\n" +
                 "        Failed\n" +
                 "    </Status>\n" +
                 "    <UserDetails>\n" +
                 "        <PhoneNumber>12367631236</PhoneNumber>\n" +
                 "            </UserDetails>\n" +
                 "            <Reason>Not registered</Reason>\n" +
                 "    </Response>"));
     }
 
     @Test
     public void shouldSearchAndSendSuccessJsonResponse() throws JSONException {
         subscriptionApiController.registerXml("432456", "P", "12");
         String registerResponse = subscriptionApiController.searchJson("432456", "P");
         assertThat(registerResponse, is("{\"phoneNumber\":\"432456\",\"status\":\"Success\",\"program\":\"PREGNANCY\",\"userStatus\":\"ACTIVE\"}"));
     }
 
     @Test
     public void shouldSendErrorJsonResponseIfTheUserIsNotRegistered() throws JSONException {
         String registerResponse = subscriptionApiController.searchJson("123451231236", "C");
         assertThat(registerResponse, is("{\"phoneNumber\":\"123451231236\",\"reason\":\"Not registered\",\"status\":\"Failed\"}"));
     }
 
     @Test
     public void shouldSearchAndSendSuccessXmlResponse() throws JSONException {
         subscriptionApiController.registerXml("432456", "P", "12");
         String registerResponse = subscriptionApiController.searchXml("432456", "P");
         assertThat(registerResponse, is("<Response>\n" +
                 "    <Status>\n" +
                 "        Success\n" +
                 "    </Status>\n" +
                 "    <UserDetails>\n" +
                 "        <PhoneNumber>432456</PhoneNumber>\n" +
                 "                    <Program>PREGNANCY</Program>\n" +
                 "            <UserStatus>ACTIVE</UserStatus>\n" +
                 "            </UserDetails>\n" +
                 "    </Response>"));
     }
 
     @Test
     public void shouldSendErrorXmlResponseIfTheUserIsNotRegistered() throws JSONException {
         String registerResponse = subscriptionApiController.searchXml("123451231236", "C");
         assertThat(registerResponse, is("<Response>\n" +
                 "    <Status>\n" +
                 "        Failed\n" +
                 "    </Status>\n" +
                 "    <UserDetails>\n" +
                 "        <PhoneNumber>123451231236</PhoneNumber>\n" +
                 "            </UserDetails>\n" +
                 "            <Reason>Not registered</Reason>\n" +
                 "    </Response>"));
     }
 
     @After
     public void tearDown() {
         allProgramTypes.removeAll();
        remove(allSubscriptions.getAll());
        remove(allSubscribers.getAll());
     }
 }
