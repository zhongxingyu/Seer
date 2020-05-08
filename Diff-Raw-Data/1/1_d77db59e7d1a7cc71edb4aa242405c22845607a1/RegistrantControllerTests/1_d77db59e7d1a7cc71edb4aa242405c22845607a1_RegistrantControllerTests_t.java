 package com.citrix.regsvc.controller;
 
 /**
  * Created by apaladino on 9/28/14.
  */
 
 import java.util.Date;
 
 import com.citrix.regsvc.Application;
 import com.citrix.regsvc.domain.Registrant;
 import com.citrix.regsvc.domain.social.facebook.profile.FacebookProfile;
 import com.citrix.regsvc.domain.social.linkedin.profile.LinkedInCompanyProfile;
 import com.citrix.regsvc.domain.social.linkedin.profile.LinkedInProfile;
 import com.citrix.regsvc.service.RegistrantService;
 import com.citrix.regsvc.util.LoggingUtil;
 import com.jayway.restassured.RestAssured;
 
 import org.apache.http.HttpStatus;
 import org.hamcrest.Matchers;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.boot.test.IntegrationTest;
 import org.springframework.boot.test.SpringApplicationConfiguration;
 import org.springframework.mock.web.MockHttpServletResponse;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.web.WebAppConfiguration;
 import org.springframework.test.web.servlet.MockMvc;
 
 import static com.jayway.restassured.RestAssured.when;
 import static junit.framework.TestCase.assertNotNull;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @SpringApplicationConfiguration(classes = Application.class)
 @WebAppConfiguration
 @IntegrationTest("server.port:0")
 public class RegistrantControllerTests {
 
 
 
     private MockMvc mockMvc;
 
 
     @Autowired
     private RegistrantService registrantService;
 
     @Autowired
     private RegistrantController registrantController;
 
 
 
     @Value("${local.server.port}")
     int port;
 
 
     @Before
     public void setUp(){
         RestAssured.port = port;
     }
 
     @Test
     public void testCreateRegistrant() throws Exception{
         Registrant registrant = new Registrant();
         registrant.setCreateTime(new Date());
         registrant.setEmail("test@jedix.com");
         registrant.setFirstName("firstName");
         registrant.setLastName("lastName");
 
         LinkedInProfile linkedInProfile = new LinkedInProfile();
         linkedInProfile.setEmail(registrant.getEmail());
         linkedInProfile.setFirstName(registrant.getFirstName());
         linkedInProfile.setLastName(registrant.getLastName());
 
         LinkedInCompanyProfile p1 = new LinkedInCompanyProfile();
         p1.setCompanySize("10-50");
         p1.setIndustry("computer");
         p1.setName("Citrix");
         p1.setStartDate(new Date());
         p1.setIsCurrent(true);
         p1.setSummary("summary info");
         p1.setTitle("chief cook and bottle washer");
         p1.setType("Saas");
 
         LinkedInCompanyProfile p2 = new LinkedInCompanyProfile();
         p2.setCompanySize("10-50");
         p2.setIndustry("computer");
         p2.setName("Yahoo");
         p2.setStartDate(new Date());
         p2.setIsCurrent(true);
         p2.setSummary("summary info");
         p2.setTitle("chief cook and bottle washer");
         p2.setType("Saas");
 
 
         linkedInProfile.getPositions().add(p1);
         linkedInProfile.getPositions().add(p2);
         registrant.setLinkedInProfile(linkedInProfile);
 
         FacebookProfile facebookProfile = new FacebookProfile();
         facebookProfile.setEmail(registrant.getEmail());
         facebookProfile.setFirstName(registrant.getFirstName());
         facebookProfile.setLastName(registrant.getLastName());
         facebookProfile.setAgeRange("10-40");
         facebookProfile.setCreateTime(new Date());
         facebookProfile.setFbLink("fbLink");
         facebookProfile.setLocale("en_US");
         facebookProfile.setPictureUrl("http://someaddress.com");
         facebookProfile.setTimezone("Los_Angeles/Pacific");
         registrant.setFacebookProfile(facebookProfile);
         Long registrantKey = registrantService.createRegistrant(registrant);
 
         Registrant createdRegistrant = registrantController.getRegistrantByID(registrantKey, new MockHttpServletResponse());
         assertNotNull(createdRegistrant);
         System.out.println("##");
         System.out.println(LoggingUtil.toJSON(createdRegistrant));
      /*   String json = new ObjectMapper().writeValueAsString(registrant);
 
         given().contentType("application/json")
                .body(json, ObjectMapperType.JACKSON_2)
                        //.parameters("firstName", "John", "lastName", "Doe", "email" , "test2@jedix.com")
                        .when()
                        .expect().statusCode(201)
                        .post("/registrant")
                        .then()
                        .body("registrantKey", equalTo("1"));
 */
     }
 
     @Test
     public void testGetByRegistrantId() throws Exception {
 
         Long registrantId = 1L;
         when().
                 get("/registrant/{registrantId}", registrantId).
                 then()
                 .statusCode(HttpStatus.SC_OK)
                 .body("name", Matchers.is(""));
     }
 }
 
 /*
     Links:
 
     http://www.jayway.com/2014/07/04/integration-testing-a-spring-boot-application/
     https://code.google.com/p/rest-assured/
  */
