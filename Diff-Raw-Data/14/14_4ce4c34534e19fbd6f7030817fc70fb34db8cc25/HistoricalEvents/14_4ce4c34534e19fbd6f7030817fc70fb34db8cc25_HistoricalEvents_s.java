 package controllers;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import models.ModelManager;
 import models.User;
 import models.eventstream.Event;
 import models.eventstream.EventTopic;
 
 import org.ontoware.rdf2go.impl.jena29.ModelImplJena26;
 import org.ontoware.rdf2go.model.Model;
 import org.ontoware.rdf2go.model.Syntax;
 import org.ontoware.rdf2go.model.node.impl.URIImpl;
 
 import play.Logger;
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.mvc.Util;
 
 import com.google.gson.reflect.TypeToken;
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.query.QuerySolution;
 
 import eu.play_project.play_commons.constants.Constants;
 import eu.play_project.play_commons.eventtypes.EventHelpers;
 import fr.inria.eventcloud.api.exceptions.MalformedSparqlQueryException;
 import fr.inria.eventcloud.api.responses.SparqlConstructResponse;
 import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
 import fr.inria.eventcloud.api.wrappers.ResultSetWrapper;
 import fr.inria.eventcloud.webservices.api.EventCloudsManagementWsApi;
 import fr.inria.eventcloud.webservices.api.PutGetWsApi;
 import fr.inria.eventcloud.webservices.factories.WsClientFactory;
 
 /**
  * 
  * @author Alexandre Bourdin
  * @author Roland Stühmer
  * @author Laurent Pellegrino
  *
  */
 public class HistoricalEvents extends Controller {
 	
 	public static String EC_MANAGEMENT_WS_SERVICE = 
 	        Constants.getProperties().getProperty("eventclouds.management.endpoint");
 	
 	@Before
 	private static void checkAuthentification() {
 		if (session.get("socialauth") != null) {
 			session.remove("socialauth");
 			Application.register();
 			return;
 		}
 		String uid = session.get("userid");
 		if (uid == null) {
 			Application.login();
 			return;
 		}
 		User user = ModelManager.get().getUserById(Long.parseLong(uid));
 		if (user == null) {
 			Application.logout();
 			return;
 		}
 		user.lastRequest = new Date();
 		request.args.put("user", user);
 	}
 	
 	
 	public static void historicalByTopic(String topicId) {
 		EventTopic et = ModelManager.get().getTopicById(topicId);
 		if (et == null) {
 			renderJSON("{\"error\":\"Error: Topic not found.\"}");
 		} else {
 			ArrayList<Event> events;
 			try {
 				events = getHistorical(et);
 				if (events.size() == 0) {
 					Logger.warn("Error while getting historic events: No historic events were found in this topic.");
 					renderJSON("{\"error\":\"No historic events were found in this topic.\"}");
 				}
 				renderJSON(events, new TypeToken<ArrayList<Event>>() {}.getType());
 			} catch (Exception e) {
 				Logger.error(e, "Error while getting historic events.");
 				renderJSON("{\"error\":\"" + e.getMessage() + "\"}");
 			}
 		}
 	}
 
 	/**
      * Retrieves the last 30 historical events for a given topic.
      * 
      * @param et event topic.
      * 
      * @return Returns null if topics doesn't exist. Returns an empty 
      * ArrayList if no events were found.
 	 * @throws IOException 
 	 * @throws MalformedSparqlQueryException 
      */
     @Util
     public static ArrayList<models.eventstream.Event> getHistorical(EventTopic et) throws IOException, MalformedSparqlQueryException {
             // Creates an Event Cloud Management Web Service Client
     	EventCloudsManagementWsApi eventCloudManagementWsClient =
                     WsClientFactory.createWsClient(
                     		EventCloudsManagementWsApi.class, EC_MANAGEMENT_WS_SERVICE);
 
             String topicUrl = et.getTopicUrl();
 
             if (!eventCloudManagementWsClient.isCreated(topicUrl)) {
                 throw new IOException("Error: The Web Service client for this topic could not be created. Aborting.");
             }
 
             String putgetProxyEndpoint =
                     findPutGetProxyEndpoint(
                             eventCloudManagementWsClient, topicUrl);
 
             PutGetWsApi putgetProxyClient = 
                     WsClientFactory.createWsClient(PutGetWsApi.class, putgetProxyEndpoint);
 
             /*
 			 * Ask for the graph names first (and finish with a second query later
 			 * in another query, for efficiency in EventCloud. The first query
 			 * returns the internal long graph names to be re-used without
 			 * modification in the second query.
 			 * 
 			 * graph is the value internal to event cloud to be used in further queries
 			 * shortGraph is the cleaned (logical) value to be used for display
 			 */
             String sparqlQuery =
             	  "PREFIX eventcloud: <http://eventcloud.inria.fr/function#> "
             	+ "PREFIX :           <http://events.event-processing.org/types/> "
             	+ "SELECT ?graph ?shortGraph WHERE { "
 				+ "   GRAPH ?graph { "
 				+ "     ?id :endTime ?publicationDateTime . "
 				+ "     BIND(eventcloud:removeMetadata(?graph) AS ?shortGraph) "
 				+ "    } "
 				+ "} ORDER BY DESC(?publicationDateTime) LIMIT 10 ";
 
             Logger.debug("Executing the following historical SPARQL query to get graph names: " + sparqlQuery);
 
             SparqlSelectResponse response = 
                     putgetProxyClient.executeSparqlSelect(sparqlQuery);
 
             ResultSetWrapper result = response.getResult();
             
             ArrayList<models.eventstream.Event> events = 
                     new ArrayList<models.eventstream.Event>();
             
             while (result.hasNext()) {
                 QuerySolution qs = result.next();
                 Node graph = qs.get("graph").asNode();
                 Node shortGraph = qs.get("shortGraph").asNode();
                 sparqlQuery =
                         "CONSTRUCT { ?s ?p ?o } WHERE { " +
                         "    GRAPH <" + graph.getURI() + "> {?s ?p ?o .} " +
                         "} ";
 
                 Logger.debug("Executing the following historical SPARQL query to get events: " + sparqlQuery);
 
                 SparqlConstructResponse constructResponse =
                         putgetProxyClient.executeSparqlConstruct(sparqlQuery);
 
                 Model rdf = new ModelImplJena26(new URIImpl(shortGraph.getURI()), constructResponse.getResult()).open();
                 EventHelpers.addNamespaces(rdf);
 
                 Logger.debug("Resulting RDF: %s", rdf.serialize(Syntax.Turtle));
 
                 events.add(models.eventstream.Event.eventFromRdf(rdf));
             }
             return events;
             
     }
  
 	@Util
     private static String findPutGetProxyEndpoint(EventCloudsManagementWsApi eventCloudManagementWsClient,
                                                   String topicUrl) {
         List<String> putgetProxyEndpoints = 
                 eventCloudManagementWsClient.getPutGetWsProxyEndpointUrls(topicUrl);
         
         if (putgetProxyEndpoints == null || putgetProxyEndpoints.size() == 0) {
             Logger.info("Creating new putget proxy for eventcloud " + topicUrl);
            return eventCloudManagementWsClient.deployPutGetWsProxy(topicUrl);
         } else {
            return putgetProxyEndpoints.get(0);
         }
     }
     
 }
