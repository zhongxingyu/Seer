 package esl.cuenet.algorithms.firstk.impl;
 
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.vocabulary.RDF;
 import esl.cuenet.algorithms.firstk.Vote;
 import esl.cuenet.algorithms.firstk.structs.eventgraph.Entity;
 import esl.cuenet.algorithms.firstk.structs.eventgraph.Event;
 import esl.cuenet.algorithms.firstk.structs.eventgraph.EventGraph;
 import esl.cuenet.model.Constants;
 import esl.cuenet.query.IResultIterator;
 import esl.cuenet.query.IResultSet;
 import esl.cuenet.query.QueryEngine;
 import esl.datastructures.Location;
 import esl.system.ExperimentsLogger;
 import org.apache.log4j.Logger;
 
 import java.util.*;
 
 public class HashIndexedEntityVoter {
 
     private Logger logger = Logger.getLogger(HashIndexedEntityVoter.class);
     private ExperimentsLogger el = ExperimentsLogger.getInstance();
 
     private CandidateVotingTable<String> candidateTable = new CandidateVotingTable<String>("eventgraph");
     private HashMap<String, CandidateVotingTable<String>> discoveredCandidatesTables = new
             HashMap<String, CandidateVotingTable<String>>();
     private List<String> projectVarURIs = new ArrayList<String>();
 
     private QueryEngine queryEngine = null;
     private Property nameProperty = null;
     private Property emailProperty = null;
     private Property livesAtProperty = null;
     private Property worksAtProperty = null;
     private Property occursAtProperty = null;
 
     private List<EntityContext> verifiedEntities = null;
 
     private boolean impulseMode = false;
 
     public HashIndexedEntityVoter(QueryEngine engine, OntModel model) {
         this.queryEngine = engine;
         nameProperty = model.getProperty(Constants.CuenetNamespace + "name");
         emailProperty = model.getProperty(Constants.CuenetNamespace + "email");
         livesAtProperty = model.getProperty(Constants.CuenetNamespace + "lives-at");
         worksAtProperty = model.getProperty(Constants.CuenetNamespace + "works-at");
         occursAtProperty = model.getProperty(Constants.CuenetNamespace + "occurs-at");
 
         projectVarURIs.add(Constants.CuenetNamespace + "person");
         verifiedEntities = new ArrayList<EntityContext>();
     }
 
     public Vote[] vote(EventGraph graph, List<Entity> discoverableEntities) {
         List<Entity> graphEntities = graph.getEntities();
         logger.info("Entities found: " + graphEntities.size());
 
         Vote[] votes =checkForFewGE(graph, discoverableEntities);
         if (votes != null) return votes;
 
         List<EntityContext> discoverableEntityContexts = new ArrayList<EntityContext>();
 
         for (Entity entity: discoverableEntities) {
             String name = getLiteralValue(entity.getIndividual(), nameProperty);
             String email = getLiteralValue(entity.getIndividual(), emailProperty);
             if (email == null) email = EntityVoter.getEmail(name);
             EntityContext ecx = new EntityContext(entity, name, email);
             discoverableEntityContexts.add(ecx);
         }
 
         for (EntityContext ecx: discoverableEntityContexts) {
             if (discoveredCandidatesTables.get(ecx.name) == null)
                 discover(ecx);
         }
 
         updateScoresForEventAttendees(graphEntities);
         return extractTopDCandidates();
     }
 
     public Vote[] impulse(EventGraph graph, Event photoCaptureEvent) {
         impulseMode = true;
 
         vote(graph, graph.getParticipants(photoCaptureEvent));
         Statement oaStatement = photoCaptureEvent.getIndividual().getProperty(occursAtProperty);
         String uri = oaStatement.getObject().asResource().getURI();
 
         int ix = uri.indexOf('_');
         if (ix == -1) return null;
         Location pceLocation = Location.getFromCache(uri.substring(ix+1));
 
         List<LocationContext> lcxCandidates = new ArrayList<LocationContext>();
         for (String entityName: discoveredCandidatesTables.keySet()) {
             CandidateVotingTable<String> candidateTable = discoveredCandidatesTables.get(entityName);
             Iterator<String> iter = candidateTable.iterator();
             while(iter.hasNext()) {
                 String name = iter.next();
                 Individual dctInd = candidateTable.getScore(name).individual;
                 Location dctIndLocation = getLocation(dctInd);
                 if (dctIndLocation == null) continue;
                 lcxCandidates.add(new LocationContext(dctInd,
                         getLiteralValue(dctInd, nameProperty), dctIndLocation));
             }
         }
 
         orderByLocation(lcxCandidates, pceLocation);
 
         impulseMode = false;
         return new Vote[0];
     }
 
     private void orderByLocation(List<LocationContext> lcxCandidates, final Location l0) {
         logger.info("Order by Location: " + lcxCandidates.size());
 
 
         PriorityQueue<LocationContext> pq = new PriorityQueue<LocationContext>(lcxCandidates.size(), new Comparator<LocationContext>() {
             @Override
             public int compare(LocationContext lcx1, LocationContext lcx2) {
                 double dist1 = lcx1.location.getEuclideanDistance(l0);
                 double dist2 = lcx2.location.getEuclideanDistance(l0);
                 return (int)(dist1-dist2);
             }
         });
 
         int i = 1;
         for (LocationContext lcx: lcxCandidates) pq.add(lcx);
         for (LocationContext l : pq) {
             logger.info(i + " " + l.name);
             el.list(i + " " + l.name);
             i++;
         }
 
         logger.info("Leaving OrderByLocation");
     }
 
     private LocationContext find(String name, List<LocationContext> lcxCandidates) {
         for (LocationContext  lcx: lcxCandidates) {
             if (lcx.name.equals(name)) return lcx;
         }
         return null;
     }
 
     private Location getLocation(Individual dctInd) {
         Statement lst = dctInd.getProperty(livesAtProperty);
         if (lst == null) lst = dctInd.getProperty(worksAtProperty);
         if (lst == null) return null;
 
         String uri = lst.getObject().asResource().getURI();
         int ix = uri.indexOf('_');
         if (ix == -1) return null;
 
         return Location.getFromCache(uri.substring(ix+1));
     }
 
     private Vote[] checkForFewGE(EventGraph graph, List<Entity> discoverableEntities) {
         if (impulseMode) return null;
 
         List<Entity> allEntities = graph.getEntities();
         if ( (allEntities.size() - discoverableEntities.size()) > 10) {
             logger.info("Too many participants to verify on everyone");
             return null;
         }
 
         //Vote[] votes = new Vote[allEntities.size() - discoverableEntities.size()];
         ArrayList<Vote> aVotes = new ArrayList<Vote>();
         for (Entity gEnt: allEntities) {
             String name = getLiteralValue(gEnt.getIndividual(), nameProperty);
             if (containedIn(name, discoverableEntities)) continue;
             if (isVerified(name)) continue;
 
             Vote v = new Vote();
             v.entity = gEnt.getIndividual();
             v.entityID = name;
             v.score = 1;
             aVotes.add(v);
         }
 
         Vote[] votes = new Vote[aVotes.size()];
         aVotes.toArray(votes);
         return votes;
     }
 
     private boolean containedIn(String pEntName, List<Entity> originalParticipants) {
         for (Entity e: originalParticipants) {
             String p = getLiteralValue(e.getIndividual(), nameProperty);
             if (p.equals(pEntName)) return true;
         }
         return false;
     }
 
     public void addToVerifiedList(Entity verifiedEntity) {
         EntityContext ecx = new EntityContext(verifiedEntity,
                 getLiteralValue(verifiedEntity.getIndividual(), nameProperty),
                 getLiteralValue(verifiedEntity.getIndividual(), emailProperty));
         verifiedEntities.add(ecx);
     }
 
     private Vote[] extractTopDCandidates() {
         Iterator<String> ctIter = candidateTable.iterator();
         ArrayList<Vote> nonZeroVotes = new ArrayList<Vote>();
         while(ctIter.hasNext()) {
             String name = ctIter.next();
             Score<String> score = candidateTable.getScore(name);
             if (score.scores > 0) nonZeroVotes.add(
                     new Vote(getLiteralValue(score.individual, nameProperty),
                             score.scores, score.individual));
         }
 
         int dups = 0;
         for (Vote v: nonZeroVotes) {
             if (discoveredCandidatesTables.get(v.entityID) != null ||
                     isVerified(v.entityID)) dups++;
         }
 
         Vote[] votes = new Vote[nonZeroVotes.size() - dups];
 
         int i = 0;
         for (Vote v: nonZeroVotes)
             if (discoveredCandidatesTables.get(v.entityID) == null && !isVerified(v.entityID))
                 votes[i++] = v;
 
         nonZeroVotes.toArray(votes);
         return votes;
     }
 
     private void updateScoresForEventAttendees(List<Entity> entities) {
         String name;
         for (Entity entity: entities) {
             name = getLiteralValue(entity.getIndividual(), nameProperty);
             if ( !candidateTable.contains(name) )
                 candidateTable.addToCandidateTable(name, entity.getIndividual());
             updateScoresForEventAttendee(name);
         }
     }
 
     private void updateScoresForEventAttendee(String name) {
         for (Map.Entry<String, CandidateVotingTable<String>> dctEntry:
                 discoveredCandidatesTables.entrySet()) {
             Score<String> score = dctEntry.getValue().getScore(name);
             if (score == null) continue;
             candidateTable.updateScore(name, score.scores + 1);
         }
     }
 
     private void discover(EntityContext ecx) {
         CandidateVotingTable<String> votingTable = new CandidateVotingTable<String>(ecx.name);
         for (IResultSet resultSet : query(ecx)) {
            logger.info(resultSet.printResults());
             IResultIterator resultIterator = resultSet.iterator();
             while(resultIterator.hasNext()) {
                 Map<String, List<Individual>> result = resultIterator.next(projectVarURIs);
                 List<Individual> relatedCandidates = result.get(Constants.CuenetNamespace + "person");
                 updateScores(votingTable, relatedCandidates);
             }
         }
         discoveredCandidatesTables.put(ecx.name, votingTable);
     }
 
     private void updateScores(CandidateVotingTable<String> votingTable,
                               List<Individual> relatedCandidates) {
         String name;
         for (Individual candidate: relatedCandidates) {
             name = getLiteralValue(candidate, nameProperty);
             if ( !votingTable.contains(name) ) votingTable.addToCandidateTable(name, candidate);
             else votingTable.updateScore(name, 1);
         }
     }
 
     private String getLiteralValue(Individual individual, Property property) {
         Statement statement = individual.getProperty(property);
         if (statement == null) return null;
         if (!statement.getObject().isLiteral()) return null;
         return statement.getObject().asLiteral().getString();
     }
 
     private List<IResultSet> query(EntityContext ecx) {
         String sparqlQuery = "SELECT ?x \n" +
                 " WHERE { \n" +
                 "?x <" + RDF.type + "> <" + Constants.CuenetNamespace + "person> .\n" +
                 "?y <" + RDF.type + "> <" + Constants.CuenetNamespace + "person> .\n" +
                 "?y <" + Constants.CuenetNamespace + "knows" + ">" + " ?x .\n";
 
         if (ecx.email != null)
             sparqlQuery += "?y <" + Constants.CuenetNamespace + "email> \"" + ecx.email + "\" .\n";
         if (ecx.name != null)
             sparqlQuery += "?y <" + Constants.CuenetNamespace + "name> \"" + ecx.name + "\" .\n";
 
         sparqlQuery += "}";
 
         logger.info("Executing Sparql Query: \n" + sparqlQuery);
         return queryEngine.execute(sparqlQuery);
     }
 
     private boolean isVerified(String name) {
         for (EntityContext ecx: verifiedEntities) if (ecx.name.equals(name)) return true;
         return false;
     }
 
     private class EntityContext {
         public EntityContext(Entity entity, String name, String email) {
             this.email = email;
             this.entity = entity;
             this.name = name;
         }
         Entity entity;
         String name;
         String email;
     }
 
     private class LocationContext {
         public LocationContext(Individual ind, String name, Location location) {
             this.ind = ind;
             this.name = name;
             this.location = location;
         }
         Location location;
         String name;
         Individual ind;
     }
 
 }
