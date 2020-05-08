 package org.triple_brain.module.model.graph;
 
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 import org.triple_brain.module.common_utils.Uris;
 import org.triple_brain.module.model.FriendlyResource;
 import org.triple_brain.module.model.FriendlyResourceFactory;
 import org.triple_brain.module.model.json.SuggestionJsonFields;
 import org.triple_brain.module.model.suggestion.Suggestion;
 import org.triple_brain.module.model.suggestion.SuggestionFactory;
 
 import javax.inject.Inject;
 import java.net.URI;
 
 /*
 * Copyright Mozilla Public License 1.1
 */
 public class ModelTestScenarios {
 
     @Inject
     FriendlyResourceFactory friendlyResourceFactory;
 
     @Inject
     SuggestionFactory suggestionFactory;
 
     public FriendlyResource personType() {
         return friendlyResourceFactory.createOrLoadUsingUriAndLabel(
                 Uris.get("http://xmlns.com/foaf/0.1/Person"),
                 "Person"
         );
     }
 
     public FriendlyResource computerScientistType() {
         return friendlyResourceFactory.createOrLoadUsingUriAndLabel(
                 Uris.get("http://rdf.freebase.com/rdf/computer.computer_scientist"),
                 "Computer Scientist"
         );
     }
 
     public FriendlyResource timBernersLee() {
         return friendlyResourceFactory.createOrLoadUsingUriAndLabel(
                 Uris.get("http://www.w3.org/People/Berners-Lee/card#i"),
                 "Tim Berners-Lee"
         );
     }
 
     public FriendlyResource creatorPredicate() {
         return friendlyResourceFactory.createOrLoadUsingUriAndLabel(
                 Uris.get("http://purl.org/dc/terms/creator"),
                 "Creator"
         );
     }
 
     public FriendlyResource timBernersLeeInFreebase() {
         return friendlyResourceFactory.createOrLoadUsingUriAndLabel(
                 Uris.get("http://rdf.freebase.com/rdf/en/tim_berners-lee"),
                 "Tim Berners-Lee"
         );
     }
 
     public FriendlyResource person() {
         return friendlyResourceFactory.createOrLoadUsingUriAndLabel(
                 Uris.get("http://xmlns.com/foaf/0.1/Person"),
                 "Person"
         );
     }
 
     public FriendlyResource extraterrestrial() {
         return friendlyResourceFactory.createOrLoadUsingUriAndLabel(
                 Uris.get("http://rdf.example.org/extraterrestrial"),
                 "Extraterrestrial"
         );
     }
 
     public FriendlyResource event() {
         return friendlyResourceFactory.createOrLoadUsingUriAndLabel(
                 Uris.get("http://rdf.freebase.com/rdf/time/event"),
                 "Event"
         );
     }
 
     public Suggestion nameSuggestion() {
         URI personUri = Uris.get("http://xmlns.com/foaf/0.1/Person");
         return suggestionFromSameAsDomainLabelAndOrigins(
                 Uris.get("http://xmlns.com/foaf/0.1/name"),
                 personUri,
                 "Name",
                 personUri
         );
     }
 
     public Suggestion startDateSuggestion() {
         return suggestionFromSameAsDomainLabelAndOrigins(
                 Uris.get("http://rdf.freebase.com/rdf/time/event/start_date"),
                 Uris.get("http://rdf.freebase.com/rdf/type/datetime"),
                 "Start date",
                 Uris.get("http://rdf.freebase.com/rdf/time/event")
         );
     }
 
     private Suggestion suggestionFromSameAsDomainLabelAndOrigins(
             URI sameAsUri,
             URI domainUri,
             String label,
             URI originUri
     ){
         try{
             return suggestionFactory.createFromJsonObject(
                     new JSONObject()
                         .put(SuggestionJsonFields.TYPE_URI, sameAsUri)
                         .put(SuggestionJsonFields.DOMAIN_URI, domainUri)
                         .put(SuggestionJsonFields.LABEL, label)
                         .put(
                                 SuggestionJsonFields.ORIGIN,
                                "identification_" + originUri
                         )
             );
         }catch(JSONException e){
             throw new RuntimeException(e);
         }
     }
 }
