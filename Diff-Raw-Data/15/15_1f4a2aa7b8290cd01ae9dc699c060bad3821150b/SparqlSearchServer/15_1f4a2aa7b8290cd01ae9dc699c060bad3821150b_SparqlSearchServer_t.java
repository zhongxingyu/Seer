 /**
  * 
  */
 package org.purl.wf4ever.rosrs.client.search;
 
 import java.net.URI;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.Logger;
 import org.apache.solr.client.solrj.SolrQuery.ORDER;
 import org.joda.time.DateTime;
 import org.joda.time.format.ISODateTimeFormat;
 import org.purl.wf4ever.rosrs.client.ResearchObject;
 import org.purl.wf4ever.rosrs.client.exception.SearchException;
 import org.purl.wf4ever.rosrs.client.search.dataclasses.FoundRO;
 import org.purl.wf4ever.rosrs.client.search.dataclasses.SearchResult;
 
 import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 
 /**
  * A search client implementation using the RODL SPARQL endpoint.
  * 
  * @author piotrek
  * 
  */
 public class SparqlSearchServer implements SearchServer {
 
     /** Logger. */
     private static final Logger LOG = Logger.getLogger(SparqlSearchServer.class);
 
     /** date format for parsing the dates in search results. */
     public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");
 
     /** The regex part of the filter. */
     private static final String SPARQL_REGEX = " REGEX(%s, \"%s\",\"i\") ";
 
     /** The filter in the SPARQL query, where the keywords go. */
     private static final String SPARQL_FILTER = "FILTER (%s).";
 
     /** The SPARQL query template. */
     private static final String SPARQL = "PREFIX ro: <http://purl.org/wf4ever/ro#>\n"
             + "PREFIX dcterms: <http://purl.org/dc/terms/>\n" + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
             + "PREFIX ore: <http://www.openarchives.org/ore/terms/>\n" + "\n"
             + "SELECT ?ro (sample(?creator) as ?thecreator) (min(?created) as ?mincreated)\n" + "WHERE {\n"
             + "    ?ro a ro:ResearchObject ;\n" + "        dcterms:creator ?creator;\n"
             + "        dcterms:created ?created .\n" + "  OPTIONAL {?ro dcterms:title  ?title . }\n" + "  %s \n"
             + "}\n" + "GROUP BY ?ro \n" + "ORDER BY DESC(?mincreated)\n" + "LIMIT 20";
 
     /** SPARQL endpoint URI. */
     private URI sparqlEndpointUri;
 
 
     /**
      * Constructor.
      * 
      * @param sparqlEndpointUri
      *            SPARQL endpoint URI
      */
     public SparqlSearchServer(URI sparqlEndpointUri) {
         this.sparqlEndpointUri = sparqlEndpointUri;
     }
 
 
     @Override
     public SearchResult search(String query) {
         String[] keywords = query.split(" ");
         List<FoundRO> searchResults = new ArrayList<>();
         StringBuilder filter = new StringBuilder();
         for (String keyword : keywords) {
             String[] regex = { String.format(SPARQL_REGEX, "?title", keyword),
                     String.format(SPARQL_REGEX, "?desc", keyword), String.format(SPARQL_REGEX, "str(?ro)", keyword) };
             filter.append(String.format(SPARQL_FILTER, StringUtils.join(regex, "||")));
         }
         String queryS = String.format(SPARQL, filter.toString());
         ResultSet results = QueryExecutionFactory.sparqlService(sparqlEndpointUri.toString(), queryS).execSelect();
         while (results.hasNext()) {
             QuerySolution solution = results.next();
             if (solution.get("ro") == null) {
                 continue;
             }
             URI uri = URI.create(solution.get("ro").asResource().getURI());
             URI creator = URI.create(solution.get("thecreator").asResource().getURI());
             DateTime created = null;
             Object date = solution.getLiteral("mincreated").getValue();
             if (date instanceof XSDDateTime) {
                 created = new DateTime(((XSDDateTime) date).asCalendar().getTimeInMillis());
             } else {
                 try {
                     created = new DateTime(ISODateTimeFormat.dateTime().parseDateTime(date.toString())
                             .toGregorianCalendar().getTimeInMillis());
                 } catch (IllegalArgumentException e) {
                     LOG.warn("Don't know how to parse date: " + date);
                 }
             }
             String title = solution.getLiteral("title") != null ? solution.getLiteral("title").getString() : null;
             ResearchObject ro = new ResearchObject(uri, null);
             ro.setCreator(creator);
             ro.setCreated(created);
             ro.setTitle(title);
             FoundRO result = new FoundRO(ro, -1);
             searchResults.add(result);
         }
         SearchResult sr = new SearchResult();
         sr.setROsList(searchResults);
         return sr;
     }
 
 
     @Override
     public boolean supportsPagination() {
        return true;
     }
 
 
     @Override
    public SearchResult search(String query, Integer offset, Integer limit, Map<String, ORDER> sortFields)
             throws SearchException {
        return null;
     }
 
 }
