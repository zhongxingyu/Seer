 package poiati.bobby.rest;
 
 
 import poiati.bobby.PersonRepository;
 import poiati.bobby.Person;
 import poiati.bobby.ConnectionManager;
 import poiati.bobby.FacebookIdAlreadyExistsException;
 import poiati.bobby.ResourceNotFoundException;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.ResponseEntity;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.HttpHeaders;
 
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 import java.util.Map;
 import java.util.Set;
 import java.util.HashMap;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.map.PropertyNamingStrategy;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 
 @Controller
 @RequestMapping(value="/api", headers = "Accept=application/json")
 public class BobbyController {
    final transient Logger logger = LoggerFactory.getLogger(BobbyController.class);

     PersonRepository personRepository;
     ConnectionManager connectionManager;
 
     @Autowired
     public BobbyController(final PersonRepository personRepository, final ConnectionManager connectionManager) {
       this.personRepository = personRepository;
       this.connectionManager = connectionManager;
     }
 
     @RequestMapping(value="/person", method = RequestMethod.POST)
    public ResponseEntity<String> create(final @RequestBody String json) {
        JsonNode jsonMap = null;
        try {
            jsonMap = this.parseJson(json);
        } catch (final IOException e) {
            logger.error("Error parsing json request", e);
            return this.badRequest();
        }
         final JsonNode nameNode = jsonMap.get("name");
         final JsonNode facebookIdNode = jsonMap.get("facebook_id");
 
         if (nameNode == null || facebookIdNode == null) {
             return this.badRequest();
         }
 
         try {
             this.personRepository.create(new Person(nameNode.getTextValue(), 
                                                     facebookIdNode.getIntValue()));
         } catch(final IllegalArgumentException e) {
             return this.badRequest();
         } catch(final FacebookIdAlreadyExistsException e) {
             return this.badRequest();
         }
 
         return new ResponseEntity<String>(new HttpHeaders(), HttpStatus.CREATED);
     }
 
     @RequestMapping(value="/person/{facebookId}/friends", method = RequestMethod.POST)
     public ResponseEntity<String> createConnection(final @PathVariable Integer facebookId, 
                                                    final @RequestBody String json) throws IOException {
        JsonNode jsonMap = null;
        try {
            jsonMap = this.parseJson(json);
        } catch (final IOException e) {
            logger.error("Error parsing json request", e);
            return this.badRequest();
        }
 
         try {
             this.connectionManager.connectFriend(facebookId, jsonMap.get("facebook_id").getIntValue());
         } catch(final ResourceNotFoundException e) {
             return this.badRequest();
         }
         return new ResponseEntity<String>(new HttpHeaders(), HttpStatus.CREATED);
     }
 
     @RequestMapping(value="/person/{facebookId}/friends", method = RequestMethod.GET)
     public ResponseEntity<String> friends(final @PathVariable Integer facebookId) throws IOException {
         try {
             final Set<Person> friends = this.personRepository.friendsFor(facebookId);
             return connections(friends);
         } catch(final ResourceNotFoundException e) {
             return this.notFound();
         }
     }
 
     @RequestMapping(value="/person/{facebookId}/friends/recommendations", method = RequestMethod.GET)
     public ResponseEntity<String> suggestions(final @PathVariable Integer facebookId) throws IOException {
         try {
             final Set<Person> suggestions = this.personRepository.suggestionsFor(facebookId);
             return connections(suggestions);
         } catch(final ResourceNotFoundException e) {
             return this.notFound();
         }
     }
 
     @RequestMapping(value="/recommendations/update", method = RequestMethod.PUT)
     public ResponseEntity<String> updateRecommendations() throws IOException {
         this.connectionManager.updateSuggestions();
         return new ResponseEntity<String>(new HttpHeaders(), HttpStatus.OK);
     }
 
     private ResponseEntity<String> connections(final Set<Person> connections) throws IOException {
         final ObjectMapper mapper = new ObjectMapper();
         mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
         final String jsonString = mapper.writeValueAsString(new PersonsJsonMapper(connections));
 
         return new ResponseEntity<String>(jsonString, new HttpHeaders(), HttpStatus.OK);
     }
 
     private JsonNode parseJson(final String json) throws IOException {
         final ObjectMapper mapper = new ObjectMapper();
         return mapper.readValue(json, JsonNode.class);
     }
 
     private ResponseEntity<String> badRequest() {
         return new ResponseEntity<String>(new HttpHeaders(), HttpStatus.BAD_REQUEST);
     }
 
     private ResponseEntity<String> notFound() {
         return new ResponseEntity<String>(new HttpHeaders(), HttpStatus.NOT_FOUND);
     }
 }
