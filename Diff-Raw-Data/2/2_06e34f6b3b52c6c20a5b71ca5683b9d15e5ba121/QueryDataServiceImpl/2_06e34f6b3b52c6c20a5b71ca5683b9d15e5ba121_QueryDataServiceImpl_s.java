 package org.linkedgov.questions.services.impl;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.WordUtils;
 import org.linkedgov.questions.model.Pair;
 import org.linkedgov.questions.model.Query;
 import org.linkedgov.questions.model.QuestionType;
 import org.linkedgov.questions.model.SparqlUtils;
 import org.linkedgov.questions.model.Triple;
 import org.linkedgov.questions.services.QueryDataService;
 import org.linkedgov.questions.services.SparqlDao;
 import org.slf4j.Logger;
 
 import uk.me.mmt.sprotocol.BNode;
 import uk.me.mmt.sprotocol.IRI;
 import uk.me.mmt.sprotocol.Literal;
 import uk.me.mmt.sprotocol.SelectResult;
 import uk.me.mmt.sprotocol.SelectResultSet;
 import uk.me.mmt.sprotocol.SparqlResource;
 
 /**
  * This class generates the object needed by the dataTable component
  * 
  * @author Luke Wilson-Mawer <a href="http://viscri.co.uk/">Viscri</a> and 
  * @author <a href="http://mmt.me.uk/foaf.rdf#mischa">Mischa Tuffield</a> for LinkedGov
  * 
  */
 public class QueryDataServiceImpl implements QueryDataService {
 
     /**
      * Used to get the reliability score
      */
     private static final String GET_RELIABILITY = "SELECT DISTINCT ?rel WHERE " +
     "{<%s> <http://data.linkedgov.org/ns#reliability> ?rel } LIMIT 1 ";
 
     private static final String XSD_INTEGER = "http://www.w3.org/2001/XMLSchema#integer";
     
     /**
      * To do the actual querying with.
      */
     private final SparqlDao sparqlDao;
 
     /**
      * To log stuff with.
      */
     private final Logger log;
 
     /**
      * Automatically called by tapestry when instantiating the service, which is a singleton.
      */
     public QueryDataServiceImpl(SparqlDao sparqlDao, Logger log){
         this.sparqlDao = sparqlDao;
         this.log = log;
     }
 
     /**
      * Get results for the user's question.
      * 
      * @param Query object representing a user's question.
      * @return a list of triples representing the answer to the question.
      */
     public List<Triple> executeQuery(Query query) {
         return executeQuery(query, null, null, null);
     }
 
     /**
      * Converts a result into a triple.
      * 
      * @param head a list of the variable names in the results.
      * @param result the result to convert.
      * @return the triple that represents the result. 
      */
     private Triple resultToTriple(List<String> head, SelectResult result) {
         final Triple triple = new Triple();    
 
         for (String variable : head) {
             final SparqlResource resource =  result.getResult().get(variable);
 
             final Pair<SparqlResource,String> sub = new Pair<SparqlResource,String>();
             final Pair<SparqlResource,String> pred = new Pair<SparqlResource,String>();
             final Pair<SparqlResource,String> obj = new Pair<SparqlResource,String>();
 
             if (variable.equals("sub")) {
                 sub.setFirst(resource);
                 triple.setSubject(sub);
             } else if (variable.equals("pred")) {
                 pred.setFirst(resource);
                 triple.setPredicate(pred);
             } else if (variable.equals("obj")) {
                 obj.setFirst(resource);
                 triple.setObject(obj);
             } else if (variable.equals("cnt")) {
                 sub.setFirst(resource);
                 triple.setSubject(sub);
             } else if (variable.equals("slabel") && resource != null) {
                 sub.setFirst(resource);
                 triple.setSubject(sub);
             } else if (variable.equals("plabel") && resource != null) {
                 resource.setValue(WordUtils.capitalize(resource.getValue()));
                 pred.setFirst(resource);
                 triple.setPredicate(pred);
             } else if (variable.equals("olabel") && resource != null) {
                 obj.setFirst(resource);
                 triple.setObject(obj);
             }
         }
        
         return triple;
     }
     
     /**
      * This function shows how if a URI of a certain rdf:type is returned it can be 
      * implemented as a special case
      */
     private String makeAddressPretty(List<Triple> triples) {
         String street = "";
         String region = "";
         String locality = "";
         String postcode = "";
         for (Triple triple : triples) {
             if (triple.getPredicate().getFirst().getValue().equals("http://www.w3.org/2006/vcard/ns#street-address")) {
                 street = triple.getObject().getFirst().getValue(); 
             } else if (triple.getPredicate().getFirst().getValue().equals("http://www.w3.org/2006/vcard/ns#region")) {
                 region = triple.getObject().getFirst().getValue(); 
             } else if (triple.getPredicate().getFirst().getValue().equals("http://www.w3.org/2006/vcard/ns#locality")) {
                 locality = triple.getObject().getFirst().getValue(); 
             } else if (triple.getPredicate().getFirst().getValue().equals("http://www.w3.org/2006/vcard/ns#postcode")) {
                 postcode = triple.getObject().getFirst().getValue().substring(triple.getObject().getFirst().getValue().lastIndexOf("/")+1); 
             }
         }
         String pretty = StringUtils.strip(street)+", "+StringUtils.strip(region)+", "+StringUtils.strip(locality)+", "+StringUtils.strip(postcode);
         pretty.replaceAll(", ,", "");
         return pretty;
     }
 
     public List<Triple> executeQuery(Query query, Integer limit, Integer offset, String orderBy) {
         final List<Triple> triples = new ArrayList<Triple>();
 
         if (!query.isNull()) {          
             final String sparqlString = query.toSparqlString();
             log.info("SPARQL ASKED:{}", sparqlString);
             log.info("QUESTION ASKED:{}", query.toString());
             final SelectResultSet results = sparqlDao.executeQuery(sparqlString, limit, offset, orderBy);
             for (SelectResult result : results.getResults()) {
                 final Triple triple = resultToTriple(results.getHead(), result);
                 triples.add(triple);
             }
         }
         
         //Specialising bnode.size = 1 and where we define vcard#Address functionality
         final List<Triple> finalTriples = new ArrayList<Triple>();
         for (Triple triple : triples) {
             if (triple.getObject().getFirst() instanceof IRI) {
                 List<Triple> iriTriples = executeIRIQuery(triple.getObject().getFirst().getValue());
                System.err.println("This size of the triples for a Given IRI is"+iriTriples.size());
                 boolean isAddress = false;
                 for (Triple row : iriTriples) {
                     if (row.getPredicate().getFirst().getValue().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") && row.getObject().getFirst().getValue().equals("http://www.w3.org/2006/vcard/ns#Address")) {
                         isAddress = true;
                         break;
                     }
                 }
                 
                 //Am now handling the special cases here 
                 if (isAddress) {
                     Triple newTriple = triple;
                     newTriple.getObject().getFirst().setValue(makeAddressPretty(iriTriples));
                     finalTriples.add(newTriple);
                 } else {
                     finalTriples.add(triple);
                 }
             } else if (triple.getObject().getFirst() instanceof BNode) {
                 List<Triple> bnodeTriples = executeBnodeQuery(triple.getObject().getFirst().getValue());
 
                 if (bnodeTriples.size() == 1) {
                     Triple newTriple = triple;
                     newTriple.getObject().getFirst().setValue(bnodeTriples.get(0).getObject().getFirst().getValue());
                     finalTriples.add(newTriple);
                 } else {
                     finalTriples.add(triple);
                 }
             } else {
                 finalTriples.add(triple);
             }
         }
 
         return finalTriples;
     }
 
     /**
      * Executes a count for this query. If the query itself is a count, it returns 1.
      */
     public int executeCountForQuery(Query query, boolean forPagination) {
 
         if (QuestionType.COUNT.equals(query.getQuestionType())) {
             return 1;
         }
         if (query.isNull()) {
             return 0;
         }
 
         final String countSparqlString = query.toSparqlString(QuestionType.COUNT, forPagination, false);
         final SelectResultSet results = sparqlDao.executeQuery(countSparqlString);
 
         if (results.getResults().isEmpty()) {
             return 0;
         }
 
         final String countLabel = results.getHead().get(0);
         final SelectResult firstResult = results.getResults().get(0);
         final String count = firstResult.getResult().get(countLabel).getValue();
 
         if(count == null){
             return 0;
         }
 
         return Integer.valueOf(count);
     }
     
     /**
      * This get all of the dataset's used to answer a query created by the user
      */
     public Map<String,String> executeGetAllGraphNames(Query query) {
         final String queryGraphs = query.toSparqlString(QuestionType.SELECT, false, true);
         final SelectResultSet graphs = sparqlDao.executeQuery(queryGraphs);
         
         Map<String,String> retValues = new HashMap<String,String>();
 
         for (SelectResult result : graphs.getResults()) {
             final SparqlResource element = result.getResult().get("g");
             if (result.getResult().get("glabel") != null) {
                 retValues.put(element.getValue(),result.getResult().get("glabel").getValue());
             } else {
                 retValues.put(element.getValue(),element.getValue());
             }            
         }
         return retValues;             
     }
 
     /**
      * This get the reliability score by querying the knowledge base
      * 
      * @return the average reliability score of the graphs or 0
      */
     public int executeReliabilityScore(Map<String,String> graphs) {
         SelectResultSet results = new SelectResultSet();
         int reliability = 0;
         int count = 0;
         for (String dataSet : graphs.keySet()) {
             String query = String.format(GET_RELIABILITY, dataSet);
             results = sparqlDao.executeQuery(query);
             for (SelectResult result : results.getResults()) {
                 SparqlResource element = result.getResult().get("rel");
                 if (element instanceof Literal) {
                     if (((Literal) element).getDatatype().equals(XSD_INTEGER)) {
                         if (SparqlUtils.isInteger(element.getValue())) {
                             reliability = reliability + Integer.parseInt(element.getValue());
                             count++;
                         }
                     }
                 }        
             }
         }
         if (reliability > 0 && count > 0) {
             reliability = reliability / count;
         } 
         return reliability;
     }
     
     /**
      * 
      * @param an iri is passed in, this is used to perform the special case's
      * the only special case currently implemented is the vcard#Address
      * @return a list of triples
      */
     public List<Triple> executeIRIQuery(String iri) {
         StringBuilder query = new StringBuilder();
         query.append("SELECT DISTINCT (<");
         query.append(iri);
         query.append("> as ?sub) ?pred ?obj ");
         query.append("WHERE { <");
         query.append(iri);
         query.append("> ?pred ?obj . ");    
         query.append("}");
         
         final String sparqlString = query.toString();
         log.info("SPARQL ASKED to grab IRI Info:{}", sparqlString);
         final SelectResultSet results = sparqlDao.executeQuery(sparqlString);
         final List<Triple> triples = new ArrayList<Triple>();
 
         for (SelectResult result : results.getResults()) {
             final Triple triple = resultToTriple(results.getHead(), result);
             triples.add(triple);
         }    
         return triples;
     }
     
     /**
      * This function is used to return triples about a given Bnode
      * This is used to fill out the Grid Component 
 
      * @param a bnode id is passed in, this is used to perform the special case's
      * the only special case currently implemented is the "single bnode case"
      * @return a list of triples
      */
     public List<Triple> executeBnodeQuery(String bnode) {
         StringBuilder query = new StringBuilder();
         query.append("SELECT DISTINCT (<bnode:");
         query.append(bnode);
         query.append("> as ?sub) ?pred ?obj ?slabel ?plabel ?olabel ");
         query.append("WHERE { <bnode:");
         query.append(bnode);
         query.append("> ?pred ?obj . ");    
         query.append("OPTIONAL {<bnode:");
         query.append(bnode);
         query.append("> <http://www.w3.org/2000/01/rdf-schema#label> ?slabel } . ");
         query.append("OPTIONAL {?pred <http://www.w3.org/2000/01/rdf-schema#label> ?plabel } . ");
         query.append("OPTIONAL {?obj <http://www.w3.org/2000/01/rdf-schema#label> ?olabel } . ");
         query.append("}");
         
         final String sparqlString = query.toString();
         log.info("SPARQL ASKED to grab Bnode Info:{}", sparqlString);
         final SelectResultSet results = sparqlDao.executeQuery(sparqlString);
         final List<Triple> triples = new ArrayList<Triple>();
 
         for (SelectResult result : results.getResults()) {
             final Triple triple = resultToTriple(results.getHead(), result);
             triples.add(triple);
         }    
         return triples;
     }
 }
