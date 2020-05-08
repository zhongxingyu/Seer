 package esl.cuenet.algorithms.firstk.impl;
 
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntClass;
 import com.hp.hpl.jena.rdf.model.*;
 import com.hp.hpl.jena.vocabulary.OWL;
 import com.hp.hpl.jena.vocabulary.RDF;
 import com.hp.hpl.jena.vocabulary.RDFS;
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.util.JSON;
 import esl.cuenet.algorithms.firstk.FirstKAlgorithm;
 import esl.cuenet.algorithms.firstk.Vote;
 import esl.cuenet.algorithms.firstk.exceptions.CorruptDatasetException;
 import esl.cuenet.algorithms.firstk.exceptions.EventGraphException;
 import esl.cuenet.algorithms.firstk.structs.eventgraph.*;
 import esl.cuenet.mapper.parser.ParseException;
 import esl.cuenet.model.Constants;
 import esl.cuenet.query.IResultIterator;
 import esl.cuenet.query.IResultSet;
 import esl.cuenet.query.QueryEngine;
 import esl.cuenet.query.drivers.webjson.HttpDownloader;
 import esl.datastructures.TimeInterval;
 import esl.datastructures.graph.*;
 import org.apache.log4j.Logger;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.*;
 
 public class FirstKDiscoverer extends FirstKAlgorithm {
 
     private Logger logger = Logger.getLogger(FirstKDiscoverer.class);
     private BFSEventGraphTraverser graphTraverser = null;
     private Queue<EventGraphNode> discoveryQueue = new LinkedList<EventGraphNode>();
     private QueryEngine queryEngine = null;
 
     private Property subeventOfProperty = null;
     private Property participatesInProperty = null;
     private Property occursDuringProperty = null;
     private Property occursAtProperty = null;
 
     private EntityVoter voter = null;
     private EventGraph graph = null;
     private LocalFileDataset dataset = null;
 
     private int discoveryCount = 0;
     private int k = 1;
 
     public FirstKDiscoverer() throws IOException, ParseException {
         super();
         queryEngine = new QueryEngine(model, sourceMapper);
         voter = new EntityVoter(queryEngine, model);
 
         subeventOfProperty = model.getProperty(Constants.CuenetNamespace + "subevent-of");
         participatesInProperty = model.getProperty(Constants.DOLCE_Lite_Namespace + "participant-in");
         occursDuringProperty = model.getProperty(Constants.CuenetNamespace + "occurs-during");
         occursAtProperty = model.getProperty(Constants.CuenetNamespace + "occurs-at");
     }
 
     public void execute(LocalFileDataset lds) throws CorruptDatasetException, EventGraphException {
         this.dataset = lds;
         LocalFilePreprocessor preprocessor = new LocalFilePreprocessor(model);
         graph = preprocessor.process(lds);
 
         TraversalContext traversalContext = new TraversalContext();
         traversalContext.setCx(discoveryQueue);
         graphTraverser = new BFSEventGraphTraverser(graph);
         graphTraverser.setTraversalContext(traversalContext);
         graphTraverser.setNodeVisitorCallback(new NodeVisitor() {
             @Override
             @SuppressWarnings("unchecked")
             public void visit(Node node, TraversalContext traversalContext) {
                 Queue<EventGraphNode> dQueue = (Queue<EventGraphNode>) traversalContext.getCx();
                 dQueue.add((EventGraphNode) node);
             }
         });
         graphTraverser.setEdgeVisitorCallback(new EdgeVisitor() {
             @Override
             public void visit(Edge edge, TraversalContext traversalContext) { } });
 
         discover(graph);
     }
 
     private void discover(EventGraph graph) throws EventGraphException {
         if (terminate(graph)) return;
         discoveryQueue.clear();
         graphTraverser.start();
         logger.info("Size of DQ: " + discoveryQueue.size());
 
         while(discoveryQueue.size() > 0) {
             EventGraphNode node = discoveryQueue.remove();
             if (node.getType() == EventGraph.NodeType.EVENT) discover((Event) node);
             else if (node.getType() == EventGraph.NodeType.ENTITY) discover((Entity) node);
         }
 
         System.out.print("Enter choice: ");
         Scanner scanner = new Scanner(System.in);
         String data = scanner.nextLine();
 
         if (data.compareToIgnoreCase("q") == 0) System.exit(0);
         logger.info("Choice: " + data);
 
         discover(graph);
     }
 
     private void discover(Entity entity) throws EventGraphException {
         String name = null;
         String email = null;
 
         if (entity.containsLiteralEdge(Constants.Name)) {
             logger.info(entity.getLiteralValue(Constants.Name));
             name = (String) entity.getLiteralValue(Constants.Name);
         }
         if (entity.containsLiteralEdge(Constants.Email)) {
             logger.info(entity.getLiteralValue(Constants.Email));
             email = (String) entity.getLiteralValue(Constants.Email);
         }
 
         //find related event (if exists), and its related timestamp, location
         TimeInterval targetInterval = getTimeIntervalFromRelatedEvent(entity);
 
         String sparqlQuery = "SELECT ?x \n" +
                 "WHERE { \n" +
                 "?x <" + RDF.type + "> <" + Constants.DOLCE_Lite_Namespace + "event> . \n" +
                 "?p <" + Constants.CuenetNamespace + "participant-in> ?x . \n" +
                 "?p <" + RDF.type + "> <" + Constants.CuenetNamespace + "person> . \n";
 
         if (email != null)
             sparqlQuery += "?p <" + Constants.CuenetNamespace + "email> \"" + email + "\" .\n";
         if (name != null)
             sparqlQuery += "?p <" + Constants.CuenetNamespace + "name> \"" + name + "\" .\n";
         if (targetInterval != null) {
             sparqlQuery += "?x <" + occursDuringProperty.getURI() + "> \"" + targetInterval.getID() + "\" .\n";
         }
 
         sparqlQuery += "} \n";
 
         logger.info("Executing Sparql Query: \n" + sparqlQuery);
 
         List<IResultSet> results = queryEngine.execute(sparqlQuery);
         logger.info("Got results from " + results.size() + " sources.");
 
         List<String> projectVarURIs = new ArrayList<String>();
         projectVarURIs.add(Constants.DOLCE_Lite_Namespace + "event");
         projectVarURIs.add(Constants.CuenetNamespace + "person");
         for (IResultSet result : results) {
             IResultIterator iter = result.iterator();
             while (iter.hasNext()) {
                 Map<String, List<Individual>> resultMap = iter.next(projectVarURIs);
                 List<Individual> possiblePersons = resultMap.get(Constants.CuenetNamespace + "person");
                 verify(possiblePersons);
 
                 List<Individual> associatableEvents = resultMap.get(Constants.DOLCE_Lite_Namespace + "event");
                 mergeEvents(associatableEvents);
             }
         }
     }
 
     private void mergeEvents(List<Individual> associatableEvents) throws EventGraphException{
         for (Individual individual: associatableEvents) mergeEvent(individual);
     }
 
     private void mergeEvent(Individual event) throws EventGraphException {
 
         Statement eti = event.getProperty(occursDuringProperty);
         TimeInterval eItvl = lookupTi(eti.getObject().asResource());
         if (eItvl == null) return;
 
         List<Event> eventsInGraph = graph.getEvents();
         for (Event eventInGraph: eventsInGraph) {
             Statement ti = eventInGraph.getIndividual().getProperty(occursDuringProperty);
             TimeInterval itvl = lookupTi(ti.getObject().asResource());
             if (itvl == null) continue;
             if (eItvl.contains(itvl)) {
                 EventGraphNode node = graph.addIndividual(event, EventGraph.NodeType.EVENT);
                 graph.addSubevent((Event)node, eventInGraph);
                 discoveryQueue.add(node);
             }
             else if (itvl.contains(eItvl)) {
                 EventGraphNode node = graph.addIndividual(event, EventGraph.NodeType.EVENT);
                 graph.addSubevent(eventInGraph, (Event)node);
                 discoveryQueue.add(node);
             }
         }
 
         logger.info("Merging event");
     }
 
     private void verify(List<Individual> possiblePersons) {
 
         if (possiblePersons.size() > 10) {
             Vote[] votes = voter.vote(graph, possiblePersons);
             logger.info("Got: " + votes.length);
             for (Vote v: votes) verify(v.entityID);
             return;
         }
 
         for (Individual person: possiblePersons) {
             Statement statement = person.getProperty(model.getProperty(Constants.CuenetNamespace + "name"));
             logger.info("Verifying person: " + statement.getObject().asLiteral().getValue());
             verify(statement.getObject().asLiteral().getString());
         }
 
         logger.info("Verification Complete");
     }
 
     public void verify (String person) {
         File file = dataset.item();
         String url = "http://api.face.com/faces/recognize.json?api_key=72b454f5b9b9fb7c83a6f7b6bfda3e59&" +
                 "api_secret=a8f9877166d42fc73a1dda1a7d8704e5&urls=" +
                 "http://tracker.ics.uci.edu/content/" + file.getName() + "&uids=" + voter.getUIDs(person);
 
         HttpDownloader downloader = new HttpDownloader();
         try {
             byte[] sa = downloader.get(url);
             BasicDBObject obj = (BasicDBObject) JSON.parse(new String(sa));
             logger.info("Verification Results: (" + person +  ") --> " + getUIDConfidence(obj));
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private String getUIDConfidence(BasicDBObject obj) {
         logger.info("Face.com response: "+ obj);
         BasicDBList photos = (BasicDBList) obj.get("photos");
         BasicDBObject photo = (BasicDBObject) photos.get(0);
         BasicDBList tags = (BasicDBList) photo.get("tags");
        BasicDBObject tag = (BasicDBObject) tags.get(0);
        BasicDBList uids = (BasicDBList) tag.get("uids");
        if (uids.size() == 0) return " NOT RECOGNIZED ";
        BasicDBObject uid = (BasicDBObject) uids.get(0);
        discoveryCount++;
        return uid.getString("confidence");
     }
 
     private TimeInterval getTimeIntervalFromRelatedEvent(Entity entity) {
         List<EventGraphEdge> edges = graph.getEdges(entity);
         for (EventGraphEdge e: edges) {
             if (e.uri().compareTo(participatesInProperty.getURI()) == 0) {
                 logger.info("Found: " + graph.getDestination(e).name());
                 EventGraphNode eventNode = graph.getDestination(e);
                 Statement ti = eventNode.getIndividual().getProperty(occursDuringProperty);
                 TimeInterval itvl = lookupTi(ti.getObject().asResource());
                 if (itvl != null) return itvl;
             }
         }
         return null;
     }
 
     private TimeInterval lookupTi(Resource r) {
         String uri = r.getURI();
         String parts[] = uri.split(" ");
 
         if (parts.length != 2) return null;
 
         if (parts[0].compareTo(Constants.DOLCE_Lite_Namespace + "time-interval") == 0) {
             return TimeInterval.getFromCache(parts[1]);
         }
 
         return null;
     }
 
     private void discover(Event event) throws EventGraphException {
         OntClass ontClass = event.getIndividual().getOntClass();
         List<OntClass> subevents = getPossibleSubeventClasses(ontClass.getURI());
         if (subevents.size() == 0) logger.info("No subevents for: " + ontClass.getURI());
         else logger.info(subevents.size() + " subevents for: " + ontClass.getURI());
 
 
         StmtIterator iterator = event.getIndividual().listProperties();
         StringBuilder builder = new StringBuilder("");
         while(iterator.hasNext()) {
             Statement iter = iterator.nextStatement();
             if (iter.getObject().isLiteral()) {
                 Literal literal = iter.getObject().asLiteral();
                 if (literal.getDatatypeURI().compareTo("http://www.w3.org/2001/XMLSchema#string") != 0) continue;
                 builder.append("?x <").append(iter.getPredicate().getURI()).append("> \"");
                 builder.append(literal.getValue());
                 builder.append("\" .\n");
             }
         }
 
         logger.info("Adding Triples: \n" + builder.substring(0));
 
         String sparqlQuery = "SELECT ?p \n" +
                 "WHERE { \n" +
                 "?x <" + RDF.type + "> <" + ontClass.getURI() + "> .\n" +
                 (( builder.length() == 0) ? "" : builder.substring(0)) +
                 "?p <" + Constants.CuenetNamespace + "participant-in> ?x .\n" +
                 "?p <" + RDF.type + "> <" + Constants.CuenetNamespace + "person> .\n";
 
         sparqlQuery += "}";
 
         logger.info("Executing Sparql Query: \n" + sparqlQuery);
         List<IResultSet> results = queryEngine.execute(sparqlQuery);
         logger.info("Got results from " + results.size() + " sources.");
 
         for (IResultSet result : results) {
             IResultIterator iter = result.iterator();
             List<String> projectVarURIs = new ArrayList<String>();
             projectVarURIs.add(Constants.DOLCE_Lite_Namespace + "event");
             projectVarURIs.add(Constants.CuenetNamespace + "person");
             while (iter.hasNext()) {
                 Map<String, List<Individual>> resultMap = iter.next(projectVarURIs);
                 List<Individual> possiblePersons = resultMap.get(Constants.CuenetNamespace + "person");
                 verify(possiblePersons);
 
                 List<Individual> associatableEvents = resultMap.get(Constants.DOLCE_Lite_Namespace + "event");
                 mergeEvents(associatableEvents);
             }
         }
     }
 
     private List<OntClass> getPossibleSubeventClasses(String superEventURI) {
         List<OntClass> subevents = new ArrayList<OntClass>();
         OntClass superEvent = model.getOntClass(superEventURI);
 
         // list all the events.
         OntClass eventOntClass = model.getOntClass(model.getNsPrefixURI(Constants.DOLCE_Lite_Namespace_Prefix) + "event");
         StmtIterator iterator = model.listStatements(null, RDFS.subClassOf, eventOntClass);
 
         while(iterator.hasNext()) {
             Statement stmt = iterator.nextStatement();
             OntClass event = model.getOntClass(stmt.getSubject().getURI());
             if (isSubeventOf(event, superEvent)) {
                 logger.info("Found subevent: " + event.getURI()) ;
                 subevents.add(event);
             }
         }
 
         return subevents;
     }
 
     private boolean isSubeventOf(OntClass event, OntClass superEvent) {
         OntClass superClass = event.getSuperClass();
 
         StmtIterator iter = model.listStatements(superClass, RDF.type, OWL.Restriction);
         while(iter.hasNext()) {
             Statement stmt = iter.nextStatement();
             Resource restriction = stmt.getSubject();
             if (isSuperEventRestriction(restriction, superEvent)) return true;
         }
 
         return false;
     }
 
     private boolean isSuperEventRestriction(Resource classWithRestriction, OntClass superEvent) {
 
         Statement onProp = classWithRestriction.getProperty(OWL.onProperty);
         Statement someVal = classWithRestriction.getProperty(OWL.someValuesFrom);
 
         if (onProp == null || someVal == null) return false;
 
         if (!(onProp.getObject().equals(subeventOfProperty)))
             return false;
 
         //check if object is superEvent
         return someVal.getObject().equals(superEvent);
     }
 
     private boolean terminate(EventGraph graph) {
         return (discoveryCount == k);
     }
 
 }
