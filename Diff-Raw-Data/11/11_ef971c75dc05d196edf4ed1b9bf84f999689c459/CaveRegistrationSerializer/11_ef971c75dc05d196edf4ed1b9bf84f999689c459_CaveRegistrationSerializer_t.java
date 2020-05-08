 package dk.sst.snomedcave.controllers.serializers;
 
 import com.google.gson.*;
 import dk.sst.snomedcave.dao.ConceptRepository;
 import dk.sst.snomedcave.model.CaveRegistration;
 import org.springframework.stereotype.Component;
 
 import javax.inject.Inject;
 import java.lang.reflect.Type;
 
 import static org.apache.commons.lang3.StringUtils.isNotEmpty;
 
 @Component
 public class CaveRegistrationSerializer implements WebSerializer<CaveRegistration> {
     @Inject
     ConceptRepository conceptRepository;
 
     @Override
     public Class<CaveRegistration> getType() {
         return CaveRegistration.class;
     }
 
     @Override
     public CaveRegistration deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
         final JsonObject json = element.getAsJsonObject();
         final CaveRegistration registration = new CaveRegistration();
         if (json.get("nodeId") != null && isNotEmpty(json.get("nodeId").getAsString())) {
             registration.setNodeId(json.get("nodeId").getAsLong());
         }
         registration.setAllergy(conceptRepository.getByConceptId(json.get("allergyId").getAsString()));
         registration.setVerification(json.get("verification").getAsString());
         registration.setGrade(json.get("grade").getAsString());
         registration.setReaction(json.get("reaction").getAsString());
         registration.setReactionFrequency(json.get("reactionFrequency").getAsString());
        if (json.get("warning") != null) {
            registration.setWarning(json.get("warning").getAsBoolean());
        }
         return registration;
     }
 
     @Override
     public JsonElement serialize(CaveRegistration registration, Type typeOfSrc, JsonSerializationContext context) {
         final JsonObject element = new JsonObject();
         element.addProperty("nodeId", registration.getNodeId());
         element.addProperty("allergyId", registration.getAllergy().getConceptId());
         element.addProperty("allergyTerm", registration.getAllergy().getTerm());
         element.addProperty("verification", registration.getVerification());
         element.addProperty("grade", registration.getGrade());
         element.addProperty("reaction", registration.getReaction());
         element.addProperty("reactionFrequency", registration.getReactionFrequency());
         element.addProperty("warning", registration.isWarning());
         return element;
     }
 }
