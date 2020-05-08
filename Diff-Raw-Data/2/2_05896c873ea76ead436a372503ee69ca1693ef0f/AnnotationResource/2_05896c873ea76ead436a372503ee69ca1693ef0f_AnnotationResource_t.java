 package net.qldarch.web.service;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import com.google.common.base.Joiner;
 import com.google.common.base.Optional;
 import com.google.common.base.Splitter;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Multimap;
 import org.apache.shiro.authz.annotation.RequiresPermissions;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.MediaType;
 
 import static com.google.common.base.Optional.fromNullable;
 import static com.google.common.collect.Collections2.transform;
 import static javax.ws.rs.core.Response.Status;
 
 import static net.qldarch.web.service.KnownURIs.*;
 
 @Path("/annotation")
 public class AnnotationResource {
     public static Logger logger = LoggerFactory.getLogger(AnnotationResource.class);
     public static final String XSD_BOOLEAN = "http://www.w3.org/2001/XMLSchema#boolean";
 
     public static String SHARED_ANNOTATION_GRAPH = "http://qldarch.net/rdf/2013-09/annotations";
 
     private RdfDataStoreDao rdfDao;
 
     public static String annotationQuery(URI annotation, BigDecimal time, BigDecimal duration) {
         BigDecimal end = time.add(duration);
 
         String formatStr = 
             "PREFIX : <http://qldarch.net/ns/rdf/2012-06/terms#> " +
             "PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#> " +
             "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
             "select distinct ?s ?p ?o  where {   " +
             "    graph <http://qldarch.net/rdf/2013-09/catalog> {" +
             "        ?u <http://qldarch.net/ns/rdf/2013-09/catalog#hasAnnotationGraph> ?g. " +
             "    } . " +
             "    graph <http://qldarch.net/ns/rdf/2012-06/terms#>  {" +
             "       ?t rdfs:subClassOf :Relationship ." +
             "    } . " +
             "    graph ?g {" +
             "        ?r a ?t ." +
             "        ?r :evidence ?e ." +
             "        ?e :documentedBy <%s> ." +
             "        ?e :timeFrom ?start ." +
             "        ?e :timeTo ?end ." +
             "    } . " +
             "    {   " +
             "        graph ?g {" +
             "            BIND (?r AS ?s) ." +
            "            ?s ?p ?o ." +
             "        } ." +
             "    } UNION {" +
             "        graph ?g {" +
             "            ?r :evidence ?s ." +
             "            ?s ?p ?o ." +
             "        } ." +
             "    } ." +
             "    FILTER ( xsd:decimal(\"%s\") <= ?end &&" +
             "             xsd:decimal(\"%s\") >= ?start ) . " +
             "} ";
 
         String query = String.format(formatStr, annotation, time, end);
 
         logger.debug("AnnotationResource performing SPARQL query: {}", query);
         
         return query;
     }
 
     @GET
     @Produces("application/json")
     public Response performGet(
             @DefaultValue("") @QueryParam("RESOURCE") String resourceStr,
             @DefaultValue("") @QueryParam("TIME") String timeStr,
             @DefaultValue("0.0") @QueryParam("DURATION") String durationStr) {
 
         logger.debug("Querying annotations for resource: {}, time: {}, duration: {}",
                 resourceStr, timeStr, durationStr);
 
         if (resourceStr.isEmpty()) {
             logger.info("Bad request received. No resource provided.");
             return Response
                 .status(Status.BAD_REQUEST)
                 .type(MediaType.TEXT_PLAIN)
                 .entity("QueryParam RESOURCE missing")
                 .build();
         }
         if (timeStr.isEmpty()) {
             logger.info("Bad request received. No time provided.");
             return Response
                 .status(Status.BAD_REQUEST)
                 .type(MediaType.TEXT_PLAIN)
                 .entity("QueryParam TIME missing")
                 .build();
         }
 
         URI resource = null;
         BigDecimal time = null;
         BigDecimal duration = null;
         try {
             resource = KnownPrefixes.resolve(resourceStr);
             time = new BigDecimal(timeStr);
             duration = new BigDecimal(durationStr);
         } catch (MetadataRepositoryException em) {
             logger.info("Unable to resolve submitted URI: {}", resourceStr, em);
             return Response
                 .status(Status.BAD_REQUEST)
                 .type(MediaType.TEXT_PLAIN)
                 .entity("QueryParam RESOURCE malformed")
                 .build();
         } catch (NumberFormatException en) {
             logger.info("Malformed xsd:decimal submitted to annotations request: {}/{}",
                 timeStr, durationStr, en);
             return Response
                 .status(Status.BAD_REQUEST)
                 .type(MediaType.TEXT_PLAIN)
                 .entity("QueryParam TIME or DURATION malformed")
                 .build();
         }
 
         logger.debug("Raw annotations query: {}, {}, {}", resource, time, duration);
 
         String result = new SparqlToJsonString().performQuery(
                 annotationQuery(resource, time, duration));
 
         return Response.ok()
             .entity(result)
             .build();
     }
 
     @POST
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.APPLICATION_JSON)
     @RequiresPermissions("create:annotation")
     public Response addAnnotation(String json) throws IOException {
         RdfDescription rdf = new ObjectMapper().readValue(json, RdfDescription.class);
 
         // Check User Authz
         User user = User.currentUser();
 
         if (user.isAnon()) {
             return Response
                 .status(Status.FORBIDDEN)
                 .type(MediaType.TEXT_PLAIN)
                 .entity("Anonymous users are not permitted to create annotations")
                 .build();
         }
 
         URI userAnnotationGraph = user.getAnnotationGraph();
 
         try {
             List<RdfDescription> evidences = rdf.getSubGraphs(QA_EVIDENCE);
             for (RdfDescription ev : evidences) {
                 List<URI> evTypes = ev.getType();
                 if (evTypes.size() == 0) {
                     logger.info("Bad request received. No rdf:type provided for evidence: {}", ev);
                     return Response
                         .status(Status.BAD_REQUEST)
                         .type(MediaType.TEXT_PLAIN)
                         .entity("No rdf:type provided for evidence")
                         .build();
                 }
                 URI evType = evTypes.get(0);
                 URI evId = user.newId(userAnnotationGraph, evType);
                 
                 ev.setURI(evId);
                 ev.replaceProperty(QA_ASSERTED_BY, user.getUserURI());
                 ev.replaceProperty(QA_ASSERTION_DATE, new Date());
 
                 this.getRdfDao().performInsert(ev, user, QAC_HAS_ANNOTATION_GRAPH,
                         userAnnotationGraph);
             }
 
             List<URI> relTypes = rdf.getType();
             if (relTypes.size() == 0) {
                 logger.info("Bad request received. No rdf:type provided: {}", rdf);
                 return Response
                     .status(Status.BAD_REQUEST)
                     .type(MediaType.TEXT_PLAIN)
                     .entity("No rdf:type provided")
                     .build();
             }
             URI relType = relTypes.get(0);
             URI relId = user.newId(userAnnotationGraph, relType);
 
             rdf.setURI(relId);
 
             // Generate and Perform insert query
             this.getRdfDao().performInsert(rdf, user, QAC_HAS_ANNOTATION_GRAPH,
                     userAnnotationGraph);
         } catch (MetadataRepositoryException em) {
             logger.warn("Error performing insert graph:{}, rdf:{})", userAnnotationGraph, rdf, em);
             return Response
                 .status(Status.INTERNAL_SERVER_ERROR)
                 .type(MediaType.TEXT_PLAIN)
                 .entity("Error performing insert")
                 .build();
         }
 
         String annotation = new ObjectMapper().writeValueAsString(rdf);
         logger.trace("Returning successful annotation: {}", annotation);
         // Return
         return Response.created(rdf.getURI())
             .entity(annotation)
             .build();
     }
 
     public void setRdfDao(RdfDataStoreDao rdfDao) {
         this.rdfDao = rdfDao;
     }
 
     public RdfDataStoreDao getRdfDao() {
         if (this.rdfDao == null) {
             this.rdfDao = new RdfDataStoreDao();
         }
         return this.rdfDao;
     }
 }
