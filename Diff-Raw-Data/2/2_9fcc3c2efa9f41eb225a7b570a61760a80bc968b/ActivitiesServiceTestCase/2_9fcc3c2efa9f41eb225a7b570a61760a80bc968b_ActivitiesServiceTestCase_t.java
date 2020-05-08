 package eu.elderspaces.activities.services;
 
 import static org.testng.Assert.assertTrue;
 import it.cybion.commons.web.http.CybionHttpClient;
 import it.cybion.commons.web.http.exceptions.CybionHttpException;
 import it.cybion.commons.web.responses.ExternalStringResponse;
 import it.cybion.commons.web.responses.ResponseStatus;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.MediaType;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.testng.annotations.Test;
 
 import com.google.common.collect.Maps;
 
 import eu.elderspaces.activities.ActivitiesEndpoint;
 import eu.elderspaces.model.Activity;
 import eu.elderspaces.model.Entity;
 import eu.elderspaces.model.Person;
 import eu.elderspaces.model.Post;
 
 /**
  * @author Matteo Moci ( matteo (dot) moci (at) gmail (dot) com )
  */
 public class ActivitiesServiceTestCase extends BaseServiceTestCase {
     
     private static final String PUBLISHED = "2013-03-29T3:41:48+0100";
     private static final String VERB = "create";
     private static final String PERSON_THUMBNAIL_URL = "http://thn1.elderspaces.iwiw.hu/0101//user/01/39/13/36/5/user_13913365_1301469612927_tn1";
     private static final String PERSON_DISPLAY_NAME = "Mr. Ederly Hans";
     private static final String PERSON_ID = "13913365:elderspaces.iwiw.hu";
     private static final String POST_TITLE = "said :";
     private static final String POST_BODY = "Hello from Athens!";
     private static final Logger LOGGER = LoggerFactory.getLogger(ActivitiesServiceTestCase.class);
     private final ObjectMapper mapper = new ObjectMapper();
     
     @Test
     public void givenOneEmptyActivityShouldGetNOKStatus() throws CybionHttpException {
     
         final String url = super.base_uri + ActivitiesEndpoint.ACTIVITY
                 + ActivitiesEndpoint.STORE_ACTIVITY;
         
         final Map<String, String> requestHeaderMap = Maps.newHashMap();
         requestHeaderMap.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
         requestHeaderMap.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
         final String emptyRequestEntity = "";
         final ExternalStringResponse stringResponse = CybionHttpClient.performPost(url,
                 requestHeaderMap, emptyRequestEntity);
         
         final String responseObject = stringResponse.getObject();
         LOGGER.debug("response body: " + responseObject);
         assertTrue(ResponseStatus.NOK == stringResponse.getStatus(), "Unexpected result: "
                 + stringResponse.getMessage());
     }
     
     @Test
     public void givenOneActivityShouldGetOKStatus() throws CybionHttpException,
             JsonGenerationException, JsonMappingException, IOException {
     
         final String url = super.base_uri + ActivitiesEndpoint.ACTIVITY
                 + ActivitiesEndpoint.STORE_ACTIVITY;
         
         final Map<String, String> requestHeaderMap = Maps.newHashMap();
         requestHeaderMap.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
         requestHeaderMap.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
         final Person actor = new Person(PERSON_ID, PERSON_DISPLAY_NAME, PERSON_THUMBNAIL_URL);
         final Entity activityObject = new Post(POST_BODY, POST_TITLE, actor);
        final Activity call = new Activity(actor, VERB, activityObject, null, PUBLISHED);
         final String callString = mapper.writeValueAsString(call);
         final String requestEntity = callString;
         final ExternalStringResponse stringResponse = CybionHttpClient.performPost(url,
                 requestHeaderMap, requestEntity);
         
         LOGGER.debug("response body: " + stringResponse.getObject());
         assertTrue(ResponseStatus.OK == stringResponse.getStatus(), "Unexpected result: "
                 + stringResponse.getMessage());
     }
 }
