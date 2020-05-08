 package eu.alertproject.iccs.socrates.connector.internal;
 
 import eu.alertproject.iccs.socrates.calculator.api.RecommendationService;
 import eu.alertproject.iccs.socrates.domain.ArtefactUpdated;
 import eu.alertproject.iccs.socrates.domain.IdentityUpdated;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.io.IOException;
 
 /**
  * User: fotis
  * Date: 25/02/12
  * Time: 12:41
  */
 public class IdentityUpdatedListener extends SocratesActiveMQListener<IdentityUpdated> {
 
     private Logger logger = LoggerFactory.getLogger(IdentityUpdatedListener.class);
 
     @Autowired
     RecommendationService recommendationService;
 

     @Override
     void updateSimilarities(IdentityUpdated identityUpdated) {

         logger.trace("void updateSimilarities() {} ",identityUpdated);
         recommendationService.updateSimilaritiesForIdentity(identityUpdated);
     }
 
     @Override
     public IdentityUpdated processText(ObjectMapper mapper, String text) throws IOException {
 
         IdentityUpdated identityUpdated = mapper.readValue(text, IdentityUpdated.class);
         logger.trace("IdentityUpdated process() {} ",identityUpdated);
         return identityUpdated;
 
     }
 }
