 package org.linkedgov.questions.components;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.tapestry5.ComponentResources;
 import org.apache.tapestry5.EventConstants;
 import org.apache.tapestry5.Link;
 import org.apache.tapestry5.annotations.AfterRender;
 import org.apache.tapestry5.annotations.Import;
 import org.apache.tapestry5.annotations.InjectComponent;
 import org.apache.tapestry5.annotations.OnEvent;
 import org.apache.tapestry5.annotations.Persist;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.annotations.RequestParameter;
 import org.apache.tapestry5.annotations.SetupRender;
 import org.apache.tapestry5.corelib.components.Zone;
 import org.apache.tapestry5.ioc.Messages;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.json.JSONArray;
 import org.apache.tapestry5.json.JSONObject;
 import org.apache.tapestry5.services.javascript.JavaScriptSupport;
 import org.linkedgov.questions.model.Query;
 import org.linkedgov.questions.model.QueryFilter;
 import org.linkedgov.questions.services.StaticDataService;
 
 /**
  * The main component for asking questions of the data.
  * 
  * @author Luke Wilson-Mawer <a href="http://viscri.co.uk/">Viscri</a> and 
  * @author <a href="http://mmt.me.uk/foaf.rdf#mischa">Mischa Tuffield</a> for LinkedGov
  * 
  */
 @Import(library="Question.js")
 public class Question {
     
     private static final String SUBJECTS = "subjects";
 
     private static final String EDITOR_CLASS = "editorClass";
 
     private static final String OBJECTS = "objects";
 
     private static final String PREDICATES = "predicates";
 
     private static final String STARTING_PREDICATE_CHANGE = "startingPredicateChange";
     
     private static final String ADD_FIRST_FILTER = "addFirstFilter";
     
     private static final String ADD_SECOND_FILTER = "addSecondFilter";
     
     private static final String ADD_FILTER = "addFilter";
     
     private static final String FIRST_FILTER_PREDICATE = "firstFilterPredicate";
     
     private static final String SECOND_FILTER_PREDICATE = "secondFilterPredicate";
 
     private static final String FILTERS = "filters";
 
     /**
      * Object to represent our sparql query. 
      */
     @Property
     @Persist
     private Query query;
     
     /**
      * A list of subjects, used as a model by the subject dropdown.
      */
     @Persist
     @SuppressWarnings("unused")
     @Property
     private Map<String,String> subjects;
     
     /**
      * A list of subjects, used as a model by the subject dropdown.
      */
     @Persist
     @SuppressWarnings("unused")
     @Property
     private Map<String,String> predicates;
     
     /**
      * An empty list, used as a model by selects whose options are populated later by ajax later.
      */
     @Property
     @SuppressWarnings("unused")
     private List<String> emptyList;
     
     /**
      * Zone which holds the results.
      */
     @InjectComponent
     private Zone resultsZone;
     
     /**
      * First filter.
      */
     @InjectComponent
     private Filter firstFilter;
     
     /**
      * Second filter.
      */
     @InjectComponent
     private Filter secondFilter;
     
     /**
      * Service to populate dropdowns with.
      */
     @Inject
     private StaticDataService staticDataService;
     
     /**
      * Some bog standard tapestry services. 
      */
     @Inject
     private JavaScriptSupport jsSupport;
     
     @Inject
     private ComponentResources resources;
     
     @Inject
     private Messages messages;
     
     /**
      * 
      * Set up the page. 
      * 
      * Set up an empty list to be used as a model by select elements that get their options populated later via ajax,
      * setup a new query object, and get the list of all available subjects.
      * 
      */
     @SuppressWarnings("unused")
     @SetupRender
     private void setup() {    
         System.out.println("Setup Render");
         query = new Query();        
         subjects =  staticDataService.getClasses();
         predicates = staticDataService.getPredicates();
         emptyList = new ArrayList<String>();
     }
     
     /**
      * Initialise the JS. 
      * 
      * Uses {@Link org.apache.tapestry5.services.javascript.JavaScriptSupport} to set up bunch of Tapestry initializers
      * which get called by Tapestry after the onDomLoaded JavaScript event. To see the matching initializers, look in 
      * org.linkedgov.questions.components.Question.js.
      */
     @AfterRender
     public void initJs(){        
         addStartingPredicateInitializerCall();
         addAddFilterInitializerCall();
         addFiltersInitializerCall();
     }
 
     /**
      * Does stuff for the new dropdown.
      */
     @OnEvent("startingPredicateChange")
    public Object handleStartingPredicateChange(@RequestParameter(value="startingPredicate",allowBlank=true) String startingPredicate) { 
         final Map<String,String> predicates = staticDataService.getClasses(startingPredicate);
         return generateSelectOptionsJson((HashMap<String, String>) predicates, SUBJECTS);
     }
     /**
      * Handles change events from the add filter button when there are not yet any filters.
      * 
      * @param subject - the subject of the query.
      * @return a {@Link org.apache.tapestry5.json.JSONObject} object containing an array of predicates, e.g. {predicates : [{value:"http://viscri.co.uk/hats",label:"Hats"}]}. 
      * This is used to populate the select element on the client side.
      * 
      */
     @OnEvent(ADD_FIRST_FILTER)
     public Object handleAddFirstFilterEvent(@RequestParameter("subject") String subject) {
         final Map<String,String> predicates = staticDataService.getPredicates(subject);
         return generateSelectOptionsJson((HashMap<String, String>) predicates, PREDICATES);
     }
     
     /**
      * Handles change events from the add filter button when the first filter has already been added.
      * 
      * @param subject - the subject of the query/question.
      * @param predicate - the predicate of the first filter.
      * @param object - the object of the first filter.
      * @return a {@Link org.apache.tapestry5.json.JSONObject} object containing an array of predicates, e.g. {predicates : [{value:"http://viscri.co.uk/hat",label:"Hat"}]}. 
      * This is used to populate the select element on the client side.
      */
     @OnEvent(ADD_SECOND_FILTER)
     public Object handleAddSecondFilterEvent(
             @RequestParameter("subject") String subject, 
             @RequestParameter("predicate")  String predicate, 
             @RequestParameter("object") String object) {
         final Map<String,String> predicates = staticDataService.getPredicates(subject, new QueryFilter(predicate,object));
         return generateSelectOptionsJson((HashMap<String, String>) predicates, PREDICATES);
     }
     
     /**
      * 
      * Handles change events from the predicate field in the first filter by talking to the staticDataService.
      * 
      * @param subject - the subject of the query/question.
      * @param predicate - the predicate of the first filter.
      * @return a {@Link org.apache.tapestry5.json.JSONObject} containing a list of potential objects and the id of the editor to display, 
      * e.g. {objects : [{value : "http://viscri.co.uk/trilby",label:"Trilby"}, editor : myHatEditor]} used on the client side to populate the object editor, if appropriate.
      *
      */
     @OnEvent(FIRST_FILTER_PREDICATE)
     public Object handleFirstFilterPredicateChanged(
             @RequestParameter("subject") String subject,
             @RequestParameter("predicate") String predicate) {
         final Map<String,String> objects = staticDataService.getObjects(subject, predicate);
         return generateJsonForPredicateEvent(subject, (HashMap<String, String>) objects);
     }
     
     /**
      * Handles change events from the predicate field in the second filter.
      * 
      * @param subject - the subject of the query/question.
      * @param predicate - the predicate of the second filter.
      * @param firstFilterPredicate - the predicate of the third filter.
      * @param firstFilterObject - the predicate of the third filter.
      * @return a {@Link org.apache.tapestry5.json.JSONObject} containing a list of potential objects and the id of the editor to display, 
      * e.g. {objects : [{value : "http://viscri.co.uk/trilby",label:"Trilby"}, editor : myHatEditor]} used on the client side to populate the object editor, if appropriate.
      */
     @OnEvent(SECOND_FILTER_PREDICATE)
     public Object handleSecondFilterPredicateChanged(
             @RequestParameter("subject") String subject,
             @RequestParameter("predicate") String predicate,
             @RequestParameter("firstFilterPredicate") String firstFilterPredicate,
             @RequestParameter("firstFilterObject") String firstFilterObject) {
         final QueryFilter firstFilterQueryFilter = new QueryFilter(firstFilterPredicate, firstFilterObject);
         final Map<String, String> objects = staticDataService.getObjects(subject, predicate, firstFilterQueryFilter);        
         return generateJsonForSecondPredicateEvent(subject, (HashMap<String, String>) objects);
     }
 
     /**
      * Generates json suitable for populating a select element from a list of objects and a predicate
      * 
      * @param predicate - the predicate in the filter on which the event was fired.
      * @param objects - the objects to be put into the json.
      * @return a {@Link org.apache.tapestry5.json.JSONObject} containing a list of potential objects and the id of the editor to display (the editor is chosen based on the predicate), 
      * e.g. {objects : [{value : "http://viscri.co.uk/trilby",label:"Trilby"}, editor : myHatEditor]} used on the client side to populate the object editor, if appropriate.
      */
     private Object generateJsonForPredicateEvent(String predicate, HashMap<String,String> objects) {
         final JSONObject data = generateSelectOptionsJson(objects, OBJECTS);
         populateEditorPropertyInJson(predicate, objects, data);
         return data;
     }
 
 	/**
 	 * Generates json suitable for populating a select element from a list of objects and a predicate
 	 * 
 	 * @param predicate - the predicate in the filter on which the event was fired.
 	 * @param objects - the objects to be put into the json.
 	 * @return a {@Link org.apache.tapestry5.json.JSONObject} containing a list of potential objects and the id of the editor to display (the editor is chosen based on the predicate), 
 	 * e.g. {objects : [{value : "http://viscri.co.uk/trilby",label:"Trilby"}, editor : myHatEditor]} used on the client side to populate the object editor, if appropriate.
 	 */
 	private Object generateJsonForSecondPredicateEvent(String predicate, HashMap<String,String> objects) {
 		final JSONObject data = generateSelectOptionsJson(objects, OBJECTS);
 		populateEditorPropertyInJson(predicate, objects, data);
 		
 		return data;
 	}
 	
 	/**
 	 * Decides which ID to use for the editor of the object.
 	 * 
 	 * @param predicate
 	 * @param objects
 	 * @param data
 	 */
 	private void populateEditorPropertyInJson(String predicate,
 			HashMap<String,String> objects, final JSONObject data) {
 		
 		//TODO, Ongoing. Make this smarter and perhaps put it into a service or something.
 		if (predicate.contains("postcode")) {
 			data.put(EDITOR_CLASS, "locationObjectEditor");
 		} else if(objects.size() < 100) {
 			data.put(EDITOR_CLASS, "selectObjectEditor");
 		} else {
 			data.put(EDITOR_CLASS, "freetextObjectEditor");
 		}
 	}
 
     /**
      * Generates json suitable for populating a select element from a list of strings and a name.
      * 
      * @param itemMap the list of items.
      * @param name - the name of the property to be set in the returned json object.
      * @return a json object with one property, given by 'name', and containing the items in the list, e.g
      *  {myName : [{value : "http://viscri.co.uk/trilby",label:"Trilby"}]}
      */
     private JSONObject generateSelectOptionsJson(HashMap<String,String> itemMap, String name) {
         
         final JSONArray items = new JSONArray();
         final JSONObject data = new JSONObject();
         
         final JSONObject blankItem = new JSONObject();
         blankItem.put("value","");        
         blankItem.put("label",messages.get(name+"BlankLabel"));
         
         items.put(blankItem);
         for (String itemString : itemMap.keySet()) {
             final JSONObject item = new JSONObject();
             item.put("value", itemString);
             item.put("label", itemMap.get(itemString));
             items.put(item);
         }
 
         data.put(name, items);
         
         return data;
     }
     
     /**
      * Adds an initializer call to handle changes of the startingPredicate.
      */
     private void addStartingPredicateInitializerCall() {
         final Link startingPredicateEventLink = resources.createEventLink(STARTING_PREDICATE_CHANGE);
         
         final JSONObject specs = new JSONObject();
         specs.put("url", startingPredicateEventLink.toAbsoluteURI());
         
         jsSupport.addInitializerCall("startingPredicate", specs);
     }
     
     /**
      * Adds a client side initializer call to set up the event listener for the predicate field of the first filter and related logic. 
      * Look in org.linkedgov.questions.components.Question.js to see the javascript function that is called.
      */
     private void addFiltersInitializerCall() {
         final Link firstFilterPredicateEventLink = resources.createEventLink(FIRST_FILTER_PREDICATE);
         final Link secondFilterPredicateEventLink = resources.createEventLink(SECOND_FILTER_PREDICATE);
         
         final JSONObject specs = new JSONObject();
         specs.put("firstFilterUrl", firstFilterPredicateEventLink.toAbsoluteURI());
         specs.put("secondFilterUrl", secondFilterPredicateEventLink.toAbsoluteURI());
         
         jsSupport.addInitializerCall(FILTERS, specs);
     }
 
     /**
      * Adds a client side initializer call to set up the event listener for the add filter button.
      * Look in org.linkedgov.questions.components.Question.js to see the javascript function that is called.
      */
     private void addAddFilterInitializerCall() {
         final Link addFirstFilterEventLink = resources.createEventLink(ADD_FIRST_FILTER);
         final Link addSecondFilterEventLink = resources.createEventLink(ADD_SECOND_FILTER);
         
         final JSONObject specs = new JSONObject();
         specs.put("firstFilterUrl", addFirstFilterEventLink.toAbsoluteURI());
         specs.put("secondFilterUrl", addSecondFilterEventLink.toAbsoluteURI());
         specs.put("id", ADD_FILTER);
         
         jsSupport.addInitializerCall(ADD_FILTER, specs);
     }
     
     /**
      * Update the results with those from the query.
      * 
      */
     @OnEvent(EventConstants.SUCCESS)
     public Object askQuestion(){
     	query.setFirstFilter(firstFilter.getFilter());
     	query.setSecondFilter(secondFilter.getFilter());
         return resultsZone.getBody();
     }
 }
 
