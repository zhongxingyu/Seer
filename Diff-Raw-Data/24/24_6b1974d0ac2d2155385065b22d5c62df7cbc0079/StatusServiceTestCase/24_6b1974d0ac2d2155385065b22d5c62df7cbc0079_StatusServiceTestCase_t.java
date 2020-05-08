 package eu.elderspaces.activities.services;
 
 import static org.testng.Assert.assertTrue;
 import it.cybion.commons.web.http.CybionHttpClient;
 import it.cybion.commons.web.http.exceptions.CybionHttpException;
 import it.cybion.commons.web.responses.ExternalStringResponse;
 import it.cybion.commons.web.responses.ResponseStatus;
 
 import java.util.Map;
 
 import javax.ws.rs.core.MediaType;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.testng.annotations.Test;
 
 import com.google.common.collect.Maps;
 
 import eu.elderspaces.activities.ActivitiesEndpoint;
 
 /**
  * @author Matteo Moci ( matteo (dot) moci (at) gmail (dot) com )
  */
 public class StatusServiceTestCase extends BaseServiceTestCase {
     
     private static final Logger LOGGER = LoggerFactory.getLogger(StatusServiceTestCase.class);
     
     @Test
     public void servicesShouldBeUpAndRunning() throws CybionHttpException {
    
        final String url = super.base_uri + ActivitiesEndpoint.REST_RADIX
                + ActivitiesEndpoint.STATUS + ActivitiesEndpoint.NOW;
         
         ExternalStringResponse stringResponse = null;
         
         final Map<String, String> requestHeaderMap = Maps.newHashMap();
         requestHeaderMap.put("Accept", MediaType.APPLICATION_JSON);
         stringResponse = CybionHttpClient.performGet(url, requestHeaderMap);
         
         LOGGER.debug("response body: " + stringResponse.getObject());
        assertTrue(ResponseStatus.OK == stringResponse.getStatus(), "Unexpected result: "
                + stringResponse.getMessage());
     }
 }
