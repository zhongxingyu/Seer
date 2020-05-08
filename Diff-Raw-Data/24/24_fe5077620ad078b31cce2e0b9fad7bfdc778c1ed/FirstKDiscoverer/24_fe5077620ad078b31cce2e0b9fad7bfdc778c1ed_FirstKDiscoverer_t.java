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
 import esl.cuenet.query.drivers.mongodb.MongoDBHelper;
 import esl.cuenet.query.drivers.webjson.HttpDownloader;
 import esl.datastructures.TimeInterval;
 import esl.datastructures.graph.*;
 import esl.system.ExperimentsLogger;
 import org.apache.log4j.Logger;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 public class FirstKDiscoverer extends FirstKAlgorithm {
 
     private Logger logger = Logger.getLogger(FirstKDiscoverer.class);
     private ExperimentsLogger expLogger = ExperimentsLogger.getInstance();
     private BFSEventGraphTraverser graphTraverser = null;
     private Queue<EventGraphNode> discoveryQueue = new LinkedList<EventGraphNode>();
 
     private QueryEngine queryEngine = null;
 
     private Property subeventOfProperty = null;
     private Property participatesInProperty = null;
     private Property occursDuringProperty = null;
     private Property nameProperty = null;
     private Property urlProperty = null;
     private Property titleProperty = null;
 
     //private EntityVoter voter = null;
     private HashIndexedEntityVoter voter = null;
     private EventGraph graph = null;
     private LocalFileDataset dataset = null;
 
     private int discoveryCount = 0;
    private int k = 7;
 
     private Event photoCaptureEvent = null;
 
     List<String> projectVarURIs = new ArrayList<String>();
 
     public FirstKDiscoverer() throws IOException, ParseException {
         super();
         queryEngine = new QueryEngine(model, sourceMapper);
 
         //voter = new EntityVoter(queryEngine, model);
         voter = new HashIndexedEntityVoter(queryEngine, model);
 
         subeventOfProperty = model.getProperty(Constants.CuenetNamespace + "subevent-of");
         participatesInProperty = model.getProperty(Constants.DOLCE_Lite_Namespace + "participant-in");
         occursDuringProperty = model.getProperty(Constants.CuenetNamespace + "occurs-during");
         nameProperty = model.getProperty(Constants.CuenetNamespace + "name");
         urlProperty = model.getProperty(Constants.CuenetNamespace + "url");
         titleProperty = model.getProperty(Constants.CuenetNamespace + "title");
 
         projectVarURIs.add(Constants.DOLCE_Lite_Namespace + "event");
         projectVarURIs.add(Constants.CuenetNamespace + "person");
     }
 
     public void setK(int k) {
         this.k = k;
     }
 
     public void execute(LocalFileDataset lds) throws CorruptDatasetException, EventGraphException {
        expLogger.list("k = " + k);

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
 
 
         List<Event> initEvents = graph.getEvents();
         for (Event e: initEvents) if (isPhotoCaptureEvent(e)) photoCaptureEvent = e;
 
         List<Entity> initEntites = graph.getEntities();
         for (Entity e: initEntites) {
             String name = getLiteralValue(e.getIndividual(), nameProperty);
             confirm(verify(name), e);
         }
 
         discover(graph);
     }
 
     private boolean isPhotoCaptureEvent(Event event) {
         OntClass cs = event.getIndividual().getOntClass();
         return cs.getURI().equals(Constants.CuenetNamespace + Constants.PhotoCaptureEvent);
     }
 
     private void discover(EventGraph graph) throws EventGraphException {
         if (terminate(graph)) return;
         expLogger.incrementIteration();
 
         discoveryQueue.clear();
         graphTraverser.start();
         logger.info("Size of DQ: " + discoveryQueue.size());
 
         int gnc = graph.getNodeCount();
 
         while(discoveryQueue.size() > 0) {
             EventGraphNode node = discoveryQueue.remove();
             if (node.getType() == EventGraph.NodeType.EVENT) discover((Event) node);
             else if (node.getType() == EventGraph.NodeType.ENTITY) discover((Entity) node);
             if (terminate(graph)) return;
         }
 
         if (gnc == graph.getNodeCount()) {
             logger.info("Initiating Impulse Response");
             voter.impulse(graph, photoCaptureEvent);
             return;
         }
 
         if (discoveryCount == 8) System.exit(0);
 
 //        System.out.print("Enter choice (k=" + discoveryCount + ") : ");
 //        Scanner scanner = new Scanner(System.in);
 //        String data = scanner.nextLine();
 //
 //        if (data.compareToIgnoreCase("q") == 0) System.exit(0);
 //        logger.info("Choice: " + data);
         discover(graph);
     }
 
     private void discover(Entity entity) throws EventGraphException {
         String name = null;
         String email = null;
 
         if (entity.containsLiteralEdge(Constants.Name)) {
             logger.info(entity.getLiteralValue(Constants.Name));
             name = (String) entity.getLiteralValue(Constants.Name);
         }
 
         expLogger.discoveryStart("entity", name);
 
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
 
         mergeQueryResults(results);
 
 //        List<String> projectVarURIs = new ArrayList<String>();
 //        projectVarURIs.add(Constants.DOLCE_Lite_Namespace + "event");
 //        projectVarURIs.add(Constants.CuenetNamespace + "person");
 //        for (IResultSet result : results) {
 //            IResultIterator iter = result.iterator();
 //            while (iter.hasNext()) {
 //                Map<String, List<Individual>> resultMap = iter.next(projectVarURIs);
 //                List<Individual> discoveredEntities = resultMap.get(Constants.CuenetNamespace + "person");
 //                verify(discoveredEntities);
 //
 //                List<Individual> discoveredEvents = resultMap.get(Constants.DOLCE_Lite_Namespace + "event");
 //                mergeEvents(discoveredEvents);
 //            }
 //        }
     }
 
     private void discover(Event event) throws EventGraphException {
         OntClass ontClass = event.getIndividual().getOntClass();
         List<OntClass> subevents = getPossibleSubeventClasses(ontClass.getURI());
         if (subevents.size() == 0) logger.info("No subevents for: " + ontClass.getURI());
         else logger.info(subevents.size() + " subevents for: " + ontClass.getURI());
 
         StringBuilder builder = new StringBuilder("");
 
         String t = getLiteralValue(event.getIndividual(), urlProperty);
         if (t != null) append(builder, Constants.CuenetNamespace + "url", t);
         expLogger.discoveryStart("event", t);
 
         t = getLiteralValue(event.getIndividual(), nameProperty);
         if (t != null) append(builder, Constants.CuenetNamespace + "name", t);
 
         t = getLiteralValue(event.getIndividual(), titleProperty);
         if (t != null) append(builder, Constants.CuenetNamespace + "title", t);
 
 //        StmtIterator iterator = event.getIndividual().listProperties();
 //
 //        while(iterator.hasNext()) {
 //            Statement iter = iterator.nextStatement();
 //            if (iter.getObject().isLiteral()) {
 //                Literal literal = iter.getObject().asLiteral();
 //                if (literal.getDatatypeURI().compareTo("http://www.w3.org/2001/XMLSchema#string") != 0) continue;
 //                builder.append("?x <").append(iter.getPredicate().getURI()).append("> \"");
 //                builder.append(literal.getValue());
 //                builder.append("\" .\n");
 //            }
 //        }
 
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
 
         mergeQueryResults(results);
 
 //        for (IResultSet result : results) {
 //            IResultIterator iter = result.iterator();
 //            List<String> projectVarURIs = new ArrayList<String>();
 //            projectVarURIs.add(Constants.DOLCE_Lite_Namespace + "event");
 //            projectVarURIs.add(Constants.CuenetNamespace + "person");
 //            while (iter.hasNext()) {
 //                Map<String, List<Individual>> resultMap = iter.next(projectVarURIs);
 //                List<Individual> possiblePersons = resultMap.get(Constants.CuenetNamespace + "person");
 //                verify(possiblePersons);
 //
 //                List<Individual> associatableEvents = resultMap.get(Constants.DOLCE_Lite_Namespace + "event");
 //                mergeEvents(associatableEvents);
 //            }
 //        }
     }
 
     private void append(StringBuilder builder, String propertyURI, String temp) {
         builder.append("?x <").append(propertyURI).append("> \"");
         builder.append(temp);
         builder.append("\" .\n");
     }
 
     private void mergeQueryResults(List<IResultSet> results) throws EventGraphException {
         boolean merged = false;
         for (IResultSet result : results) {
             IResultIterator iter = result.iterator();
             while (iter.hasNext()) {
                 Map<String, List<Individual>> resultMap = iter.next(projectVarURIs);
                 List<Individual> discoveredEntities = resultMap.get(Constants.CuenetNamespace + "person");
                 //verify(discoveredEntities);
 
                 List<Individual> discoveredEvents = resultMap.get(Constants.DOLCE_Lite_Namespace + "event");
                 mergeEvents(discoveredEvents, discoveredEntities);
                 merged = true;
             }
         }
 
         if (!merged) return;
 
         List<Entity> parts = graph.getParticipants(photoCaptureEvent);
         Vote[] votes =  voter.vote(graph, parts);
 
         if (votes.length == 0)  {
             logger.info("voter returned 0 possible entities!");
             verifyOverAllGraphEntities();
         }
         else logger.info("voter returned " + votes.length + " possibilities");
 
         for (Vote vote : votes) {
             logger.info(vote.entityID + "  " + vote.score);
             expLogger.voterScore(vote.entityID, vote.score);
 
             int conf = verify(vote.entityID);
             confirm(conf, vote.entity);
         }
 
 //        if (votes.length > 0) {
 //            if (ix > 0)
 //                throw new EventGraphException("Sandese aate hein");
 //            ix++;
 //        }
 
     }
 
     private void verifyOverAllGraphEntities() throws EventGraphException {
         List<Entity> allEntities = graph.getEntities();
         List<Entity> pcEntities = graph.getParticipants(photoCaptureEvent);
 
         if ( (allEntities.size() - pcEntities.size()) > 10) {
             logger.info("Too many participants to verify on everyone");
             return;
         }
 
         for (Entity gEnt: allEntities) {
             String name = getLiteralValue(gEnt.getIndividual(), nameProperty);
             if (containedIn(name, pcEntities)) continue;
             int conf = verify(name);
             confirm(conf, gEnt);
         }
 
     }
 
 //    private int ix = 0;
 
     private void mergeEvents(List<Individual> associatableEvents, List<Individual> participatingEntities) throws EventGraphException{
         for (Individual individual: associatableEvents) mergeEvent(individual, participatingEntities);
     }
 
     private void mergeEvent(Individual event, List<Individual> participatingEntities) throws EventGraphException {
 
         Statement eti = event.getProperty(occursDuringProperty);
         TimeInterval eItvl = lookupTi(eti.getObject().asResource());
         if (eItvl == null) return;
 
         Event eig = identicalEvent(event);
         if (eig != null) {
             addParticipants(eig, participatingEntities);
             return;
         }
 
         List<Event> eventsInGraph = graph.getEvents();
         for (Event eventInGraph: eventsInGraph) {
             Statement ti = eventInGraph.getIndividual().getProperty(occursDuringProperty);
             TimeInterval itvl = lookupTi(ti.getObject().asResource());
             if (itvl == null) continue;
             if (eItvl.contains(itvl)) {
                 Event node = (Event) graph.addIndividual(event, EventGraph.NodeType.EVENT);
                 graph.addSubevent(node, eventInGraph);
                 addParticipants(node, participatingEntities);
                 discoveryQueue.add(node);
             }
             else if (itvl.contains(eItvl)) {
                 Event node = (Event) graph.addIndividual(event, EventGraph.NodeType.EVENT);
                 graph.addSubevent(eventInGraph, node);
                 addParticipants(node, participatingEntities);
                 discoveryQueue.add(node);
             }
         }
 
         logger.info("Merging event");
     }
 
     private List<Entity> addParticipants(Event event, List<Individual> participatingEntities) throws EventGraphException {
         List<Entity> graphEntities = new ArrayList<Entity>();
         List<Entity> originalParticipants = graph.getParticipants(event);
 
         for (Individual pEnt: participatingEntities) {
             String pEntName = getLiteralValue(pEnt, nameProperty);
             if (containedIn(pEntName, originalParticipants)) continue;
             Entity gEnt = (Entity) graph.addIndividual(pEnt, EventGraph.NodeType.ENTITY);
             graph.addParticipant(event, gEnt);
             graphEntities.add(gEnt);
         }
         return graphEntities;
     }
 
     private boolean containedIn(String pEntName, List<Entity> originalParticipants) {
         for (Entity e: originalParticipants) {
             String p = getLiteralValue(e.getIndividual(), nameProperty);
             if (p.equals(pEntName)) return true;
         }
         return false;
     }
 
     private Event identicalEvent(Individual event) {
         List<Event> eventsInGraph = graph.getEvents();
         String eventURI = event.getURI();
         for (Event eventInGraph: eventsInGraph) {
             String eventInGraphURI = eventInGraph.getIndividual().getURI();
             if (eventInGraphURI.compareTo(eventURI) == 0)
                 return eventInGraph;
         }
 
         return null;
     }
 
     private void confirm(int conf, Individual individual)
             throws EventGraphException {
         Entity ent = null;
 
         List<Entity> gEntities = graph.getEntities();
         for (Entity ge: gEntities) {
             if ( getLiteralValue(ge.getIndividual(), nameProperty).equals(
                     getLiteralValue(individual, nameProperty)) ) {
                 ent = ge; break;
             }
         }
 
         confirm(conf, ent);
     }
 
     private void confirm(int confidence, Entity person) throws EventGraphException {
         voter.addToVerifiedList(person);
         if (confidence < 25) return;
         pushdown(person);
         discoveryCount++;
     }
 
     private void pushdown(Entity verifiedPerson) throws EventGraphException {
         String verifiedPersonName = getLiteralValue(verifiedPerson.getIndividual(), nameProperty);
         List<Event> allEvents = graph.getEvents();
 
         for (Event ae: allEvents) {
             List<Entity> participants = graph.getParticipants(ae);
             if (containedIn(verifiedPersonName, participants)) {
                 graph.dropParticipantEdge(ae, verifiedPerson);
                 graph.addParticipant(photoCaptureEvent, verifiedPerson);
             }
 
         }
 
     }
 
 //    private void addEntity(Entity person) {
 //        String pName = getLiteralValue(person.getIndividual());
 //        if (pName == null) return;
 //        List<Entity> graphEntities = graph.getEntities();
 //
 //        for (Entity entity: graphEntities) {
 //            String entityName = getLiteralValue(entity.getIndividual());
 //            if (entityName == null) continue;
 //            if (entityName.compareToIgnoreCase(pName) == 0) return;
 //        }
 //
 //        graph.addIndividual(person, EventGraph.NodeType.ENTITY);
 //    }
 
     public int verify(String person) {
         File file = dataset.item();
 
         String uid = EntityVoter.getUIDs(person);
 
         String url = "http://api.face.com/faces/recognize.json?api_key=72b454f5b9b9fb7c83a6f7b6bfda3e59&" +
                 "api_secret=a8f9877166d42fc73a1dda1a7d8704e5&urls=" +
                 "http://tracker.ics.uci.edu/content/" + file.getName() + "&uids=" + uid;//voter.getUIDs(person);
 
         HttpDownloader downloader = new HttpDownloader();
         int conf = -1;
         byte[] sa = null;
         try {
             sa = downloader.get(url);
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         if (sa == null) return conf;
 
         BasicDBObject obj = (BasicDBObject) JSON.parse(new String(sa));
         logger.info("Face.com response: "+ obj);
         String confidence = getUIDConfidence(obj);
         try {
             conf = Integer.parseInt(confidence);
             logger.info("Verification Results: (" + person + ") --> " + conf);
             expLogger.recognized(person, conf);
         } catch (NumberFormatException nfe) {
             logger.info("Verification Results: (" + person + ") --> " + " NOT RECOG'D ");
             expLogger.recognized(person, -1);
         }
 
         return conf;
     }
 
     private String getUIDConfidence(BasicDBObject obj) {
         BasicDBList photos = (BasicDBList) obj.get("photos");
         BasicDBObject photo = (BasicDBObject) photos.get(0);
         BasicDBList tags = (BasicDBList) photo.get("tags");
         for (int i=0; i<tags.size(); i++) {
             BasicDBObject tag = (BasicDBObject) tags.get(i);
             BasicDBList uids = (BasicDBList) tag.get("uids");
             if (uids.size() == 0) continue;
             BasicDBObject uid = (BasicDBObject) uids.get(0);
             return uid.getString("confidence");
         }
 
         return " NOT RECOGNIZED ";
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
        return (discoveryCount >= k);
     }
 
     private String getLiteralValue(Individual individual, Property property) {
         Statement statement = individual.getProperty(property);
         if (statement == null) return null;
         if (!statement.getObject().isLiteral()) return null;
         return statement.getObject().asLiteral().getString();
     }
 
     //
 //    private void verify(List<Entity> possiblePersons) {
 //
 //        if (possiblePersons.size() > 10) {
 //            Vote[] votes = voter.vote(graph, possiblePersons);
 //            logger.info("Got: " + votes.length);
 //            for (Vote v: votes) {
 //                int conf = verify(v.entityID);
 //                confirm(conf, new Entity(v.entity));    //TODO: Quick fix for confirm entities.
 //            }
 //            return;
 //        }
 //
 //        for (Entity person: possiblePersons) {
 //            Statement statement = person.getIndividual().getProperty(model.getProperty(Constants.CuenetNamespace + "name"));
 //            logger.info("Verifying person: " + statement.getObject().asLiteral().getValue());
 //            int conf = verify(statement.getObject().asLiteral().getString());
 //            confirm(conf, person);
 //        }
 //
 //        logger.info("Verification Complete");
 //    }
 
 
 }
