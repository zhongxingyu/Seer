 package cz.cuni.mff.odcleanstore.queryexecution;
 
 import cz.cuni.mff.odcleanstore.configuration.QueryExecutionConfig;
 import cz.cuni.mff.odcleanstore.conflictresolution.AggregationSpec;
 import cz.cuni.mff.odcleanstore.conflictresolution.ConflictResolverFactory;
 import cz.cuni.mff.odcleanstore.conflictresolution.NamedGraphMetadataMap;
 import cz.cuni.mff.odcleanstore.connection.JDBCConnectionCredentials;
 import cz.cuni.mff.odcleanstore.connection.exceptions.DatabaseException;
 import cz.cuni.mff.odcleanstore.shared.ErrorCodes;
 import cz.cuni.mff.odcleanstore.shared.Utils;
 import cz.cuni.mff.odcleanstore.vocabulary.ODCS;
 
 import de.fuberlin.wiwiss.ng4j.Quad;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Collection;
 import java.util.Locale;
 
 /**
  * Executes the named graph provenance metadata query.
  * Metadata about a given named graph are returned.
  *
  * This class is not thread-safe.
  *
  * @author Jan Michelfeit
  */
 /*package*/class MetadataQueryExecutor extends QueryExecutorBase {
     private static final Logger LOG = LoggerFactory.getLogger(MetadataQueryExecutor.class);
 
     private static final QueryConstraintSpec EMPTY_QUERY_CONSTRAINT_SPEC = new QueryConstraintSpec();
     private static final AggregationSpec EMPTY_AGGREGATION_SPEC = new AggregationSpec();
 
     /**
      * SPARQL query that gets provenance metadata for the given named graph.
      *
      * Must be formatted with arguments: (1) the requested named graph, (2) limit
      *
      * TODO: omit metadata for additional labels?
      */
     private static final String PROVENANCE_METADATA_QUERY = "SPARQL"
             + "\n SELECT "
             + "\n   ?provenanceGraph ?s ?p ?o"
             + "\n WHERE {"
             + "\n   <%1$s> <" + ODCS.provenanceMetadataGraph + "> ?provenanceGraph."
             + "\n   GRAPH ?provenanceGraph {"
             + "\n     ?s ?p ?o"
             + "\n   }"
             + "\n }"
             + "\n LIMIT %2$d";
 
     /**
      * SPARQL query that gets ODCS metadata for the given named graph.
      * OPTIONAL clauses for fetching ?graph properties are necessary (probably due to Virtuoso inference processing).
      *
      * Must be formatted with arguments: (1) the requested named graph, (2) limit
      *
      * TODO: omit metadata for additional labels?
      */
     private static final String ODCS_METADATA_QUERY = "SPARQL"
             + "\n SELECT "
             + "\n   <%1$s> as ?resGraph ?p ?o"
             + "\n WHERE {"
             + "\n   {"
             + "\n     <%1$s> <" + ODCS.metadataGraph + "> ?metadataGraph"
             + "\n     GRAPH ?metadataGraph {"
             + "\n       <%1$s> ?p ?o"
             + "\n     }"
             + "\n   }"
             + "\n   UNION"
             + "\n   {"
             + "\n     <%1$s> <" + ODCS.publishedBy + "> ?publishedBy."
             + "\n     ?publishedBy ?p ?o."
             + "\n     FILTER (?p = <" + ODCS.publisherScore + ">)"
             + "\n   }"
             + "\n }"
             + "\n LIMIT %2$d";
 
     /**
      * Creates a new instance of NamedGraphMetadataQueryExecutor.
      * @param connectionCredentials connection settings for the SPARQL endpoint that will be queried
      * @param conflictResolverFactory factory for ConflictResolver
      * @param labelPropertiesList list of label properties formatted as a string for use in a query
      * @param globalConfig global conflict resolution settings
      */
     public MetadataQueryExecutor(
             JDBCConnectionCredentials connectionCredentials,
             ConflictResolverFactory conflictResolverFactory,
             String labelPropertiesList,
             QueryExecutionConfig globalConfig) {
 
         super(connectionCredentials, EMPTY_QUERY_CONSTRAINT_SPEC, EMPTY_AGGREGATION_SPEC, conflictResolverFactory,
                 labelPropertiesList, globalConfig);
     }
 
     /**
      * Returns metadata about a named graph.
      *
      * @param namedGraphURI the requested named graph; must be an absolute URI, not a prefixed name
      * @return query result holder
      * @throws QueryExecutionException database error or the query was invalid
      */
     public MetadataQueryResult getMetadata(String namedGraphURI) throws QueryExecutionException {
         LOG.info("Metadata query for <{}>", namedGraphURI);
         long startTime = System.currentTimeMillis();
 
         // Check that the URI is valid (must not be empty or null, should match '<' ([^<>"{}|^`\]-[#x00-#x20])* '>' )
         if (namedGraphURI.length() > MAX_URI_LENGTH) {
             throw new QueryExecutionException(EnumQueryError.QUERY_TOO_LONG, ErrorCodes.QE_INPUT_FORMAT_ERR,
                     "The requested URI is longer than " + MAX_URI_LENGTH + " characters.");
         }
         if (!Utils.isValidIRI(namedGraphURI)) {
             throw new QueryExecutionException(EnumQueryError.INVALID_QUERY_FORMAT, ErrorCodes.QE_INPUT_FORMAT_ERR,
                     "The query is not a valid URI.");
         }
 
         try {
             NamedGraphMetadataMap metadata = getODCSMetadata(namedGraphURI);
             Collection<Quad> provenanceMetadata = getProvenanceMetadata(namedGraphURI);
 
             return createResult(provenanceMetadata, metadata, namedGraphURI, System.currentTimeMillis() - startTime);
         } catch (DatabaseException e) {
             throw new QueryExecutionException(
                     EnumQueryError.DATABASE_ERROR, ErrorCodes.QE_NG_METADATA_DB_ERR, "Database error", e);
         } finally {
             closeConnectionQuietly();
         }
     }
 
     /**
      * Creates an object holding the results of the query.
      * @param provenanceMetadata provenance metadata
      * @param metadata ODCS metadata
      * @param query the requested named graph
      * @param executionTime query execution time in ms
      * @return query result holder
      */
     private MetadataQueryResult createResult(Collection<Quad> provenanceMetadata,
             NamedGraphMetadataMap metadata, String query, long executionTime) {
 
         LOG.debug("Query Execution: getMetadata() in {} ms", executionTime);
         // Format and return result
         MetadataQueryResult queryResult = new MetadataQueryResult(
                 provenanceMetadata, metadata, query, EnumQueryType.METADATA);
         queryResult.setExecutionTime(executionTime);
         return queryResult;
     }
 
     /**
      * Return provenance metadata passed to the input webservice as a collection of quads.
      * @param namedGraphURI the requested named graph URI
      * @return provenance metadata placed in a single named graph
      * @throws DatabaseException query error
      */
     private Collection<Quad> getProvenanceMetadata(String namedGraphURI) throws DatabaseException {
         String query = String.format(Locale.ROOT, PROVENANCE_METADATA_QUERY, namedGraphURI, maxLimit);
         return getQuadsFromQuery(query, "getProvenanceMetadata()");
     }
 
     /**
      * Return ODCS metadata for the requested named graph.
      * @param namedGraphURI requested named graph URI
      * @return metadata of the requested named graph
      * @throws DatabaseException query error
      */
     private NamedGraphMetadataMap getODCSMetadata(String namedGraphURI) throws DatabaseException {
         String query = String.format(Locale.ROOT, ODCS_METADATA_QUERY, namedGraphURI, maxLimit);
         return getMetadataFromQuery(query, "getODCSMetadata()");
     }
 }
