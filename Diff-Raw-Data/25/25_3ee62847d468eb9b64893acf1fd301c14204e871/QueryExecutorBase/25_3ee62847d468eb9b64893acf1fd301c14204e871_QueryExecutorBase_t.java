 package cz.cuni.mff.odcleanstore.queryexecution;
 
 import cz.cuni.mff.odcleanstore.configuration.QueryExecutionConfig;
 import cz.cuni.mff.odcleanstore.conflictresolution.AggregationSpec;
 import cz.cuni.mff.odcleanstore.conflictresolution.ConflictResolverFactory;
 import cz.cuni.mff.odcleanstore.conflictresolution.NamedGraphMetadata;
 import cz.cuni.mff.odcleanstore.conflictresolution.NamedGraphMetadataMap;
 import cz.cuni.mff.odcleanstore.connection.JDBCConnectionCredentials;
 import cz.cuni.mff.odcleanstore.connection.VirtuosoConnectionWrapper;
 import cz.cuni.mff.odcleanstore.connection.WrappedResultSet;
 import cz.cuni.mff.odcleanstore.connection.exceptions.ConnectionException;
 import cz.cuni.mff.odcleanstore.connection.exceptions.DatabaseException;
 import cz.cuni.mff.odcleanstore.connection.exceptions.QueryException;
 import cz.cuni.mff.odcleanstore.shared.ErrorCodes;
 import cz.cuni.mff.odcleanstore.shared.Utils;
 import cz.cuni.mff.odcleanstore.vocabulary.ODCS;
 import cz.cuni.mff.odcleanstore.vocabulary.OWL;
 import cz.cuni.mff.odcleanstore.vocabulary.XMLSchema;
 
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.graph.Triple;
 
 import de.fuberlin.wiwiss.ng4j.Quad;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 import java.util.TreeSet;
 
 /**
  * The base class of query executors.
  *
  * Each query executor loads triples relevant for the query and metadata from the clean database, applies
  * conflict resolution to it and returns a holder of the result quads and metadata.
  *
  * This class is not thread-safe.
  *
  * @author Jan Michelfeit
  */
 /*package*/abstract class QueryExecutorBase {
     private static final Logger LOG = LoggerFactory.getLogger(QueryExecutorBase.class);
 
     /** Maximum allowed length of a URI query. */
     public static final int MAX_URI_LENGTH = 1024;
 
     /**
      * (Debug) Only named graph having URI starting with this prefix can be included in query result.
      * If the value is null, there is now restriction on named graph URIs.
      * This constant is only for debugging purposes and should be null in production environment.
      */
    protected static final String ENGINE_TEMP_GRAPH_PREFIX = ODCS.engineTemporaryGraph;
 
     /**
      * (Debug) Only named graph having URI starting with this prefix can be included in query result.
      * If the value is null, there is now restriction on named graph URIs.
      * This constant is only for debugging purposes and should be null in production environment.
      */
     private static final String GRAPH_PREFIX_FILTER = null; //"http://odcs.mff.cuni.cz/namedGraph/qe-test/";
 
     /**
      * Maximum number of properties with explicit aggregation settings.
      * This limit is imposed because all of them might be listed in a query.
      * @see UriQueryExecutor#getSameAsLinks()
      */
     private static final long MAX_PROPERTY_SETTINGS_SIZE = 500;
 
     /**
      * Maximum length of owl:sameAs path considered when searching for synonyms of a URI.
      * @see UriQueryExecutor#addSameAsLinksForURI(String, Collection)
      */
     private static final int MAX_SAMEAS_PATH_LENGTH = 30;
 
     /**
      * A {@link Node} representing the owl:sameAs predicate.
      */
     protected static final Node SAME_AS_PROPERTY = Node.createURI(OWL.sameAs);
 
 
     /**
      * SPARQL snippet restricting result to ?graph having at least the given score.
      * Even though the score must be present, the pattern wouldn't work without OPTIONAL
      * (probably due to Virtuoso inference processing).
      * Must be formatted with the score as an argument.
      */
     private static final String SCORE_FILTER_CLAUSE = " OPTIONAL { ?graph <" + ODCS.score + "> ?_score }"
             + " FILTER(?_score >= %f)";
 
     /**
      * SPARQL snippet restricting result to ?graph having at least the given inserted at date.
      * Even though the score must be present, the pattern wouldn't work without OPTIONAL
      * (probably due to Virtuoso inference processing).
      * Must be formatted with the date given as an argument.
      */
     private static final String INSERTED_AT_FILTER_CLAUSE = " OPTIONAL"
             + " { ?graph <" + ODCS.insertedAt + "> ?_insertedAt }"
             + " FILTER(?_insertedAt >= \"%s\"^^<" + XMLSchema.dateTimeType + ">)";
 
     /**
      * SPARQL snippet restricting a variable to start with the given string.
      * Must be formatted with a string argument.
      */
     private static final String PREFIX_FILTER_CLAUSE = " FILTER (bif:starts_with(str(?%s), '%s'))";
 
     /**
      * SPARQL snippet restricting a variable NOT to start with the given string.
      * Must be formatted with a string argument.
      */
     private static final String PREFIX_FILTER_CLAUSE_NEGATIVE = " FILTER (!bif:starts_with(str(?%s), '%s'))";
 
     /**
      * SPARQL query for retrieving all synonyms (i.e. resources connected by an owl:sameAs path) of a given URI.
      * Must be formatted with arguments: (1) URI, (2) maximum length of owl:sameAs path considered, (3) limit.
      * @see #MAX_SAMEAS_PATH_LENGTH
      */
     private static final String URI_SYNONYMS_QUERY = "SPARQL"
                 + "\n SELECT ?r ?syn"
                 + "\n WHERE {"
                 + "\n   {"
                 + "\n     SELECT ?r ?syn"
                 + "\n     WHERE {"
                 + "\n       { ?r owl:sameAs ?syn }"
                 + "\n       UNION"
                 + "\n       { ?syn owl:sameAs ?r }"
                 + "\n     }"
                 + "\n   }"
                 + "\n   OPTION (TRANSITIVE, t_in(?r), t_out(?syn), t_distinct, t_min(1), t_max(%2$d))"
                 + "\n   FILTER (?r = <%1$s>)"
                 + "\n }"
                 + "\n LIMIT %3$d";
 
     /**
      * Returns a SPARQL snippet restricting a named graph URI referenced by the given variable to GRAPH_PREFIX_FILTER.
      * Returns an empty string if GRAPH_PREFIX_FILTER is null.
      * @see #GRAPH_PREFIX_FILTER
      * @param graphVariable SPARQL variable name
      * @return SPARQL query snippet
      */
     protected static String getGraphPrefixFilter(String graphVariable) {
         String result = String.format(Locale.ROOT, PREFIX_FILTER_CLAUSE_NEGATIVE, graphVariable, ENGINE_TEMP_GRAPH_PREFIX);
         if (GRAPH_PREFIX_FILTER != null) {
             result += String.format(Locale.ROOT, PREFIX_FILTER_CLAUSE, graphVariable, GRAPH_PREFIX_FILTER);
         }
         return result;
     }
 
     /**
      * @see {@link #getGraphFilterClause()}
      * @param constraints constraints on triples returned in the result
      * @return SPARQL query snippet
      */
     private static CharSequence buildGraphFilterClause(QueryConstraintSpec constraints) {
         if (constraints.getMinScore() == null && constraints.getOldestTime() == null) {
             return getGraphPrefixFilter("graph");
         }
         StringBuilder sb = new StringBuilder();
         if (constraints.getMinScore() != null) {
             sb.append(String.format(Locale.ROOT, SCORE_FILTER_CLAUSE, constraints.getMinScore()));
         }
         if (constraints.getOldestTime() != null) {
             java.sql.Timestamp oldestTime = new Timestamp(constraints.getOldestTime().getTime());
             sb.append(String.format(Locale.ROOT, INSERTED_AT_FILTER_CLAUSE, oldestTime.toString()));
         }
         sb.append(getGraphPrefixFilter("graph"));
         return sb;
     }
 
     /** Connection settings for the SPARQL endpoint that will be queried. */
     protected final JDBCConnectionCredentials connectionCredentials;
 
     /** Constraints on triples returned in the result. */
     protected final QueryConstraintSpec constraints;
 
     /** Properties designating a human-readable label formatted to a string for use in a SPARQL query. */
     protected String labelPropertiesList;
 
     /** Global QE configuration settings. */
     protected final QueryExecutionConfig globalConfig;
 
     /** Database connection. */
     private VirtuosoConnectionWrapper connection;
 
     /**
      * Cached graph filter SPARQL snippet.
      * Depends only on settings immutable during the instance lifetime and thus can be cached.
      */
     private CharSequence graphFilterClause;
 
     /** Aggregation settings for conflict resolution. Overrides {@link #defaultAggregationSpec}. */
     protected final AggregationSpec aggregationSpec;
 
     /** Factory for ConflictResolver instances. */
     protected final ConflictResolverFactory conflictResolverFactory;
 
     /** Maximum number of triples returned by each database query (the overall result size may be larger). */
     protected final Long maxLimit;
 
     /**
      * Creates a new instance of QueryExecutorBase.
      * @param connectionCredentials connection settings for the SPARQL endpoint that will be queried
      * @param constraints constraints on triples returned in the result
      * @param aggregationSpec aggregation settings for conflict resolution;
      *        property names must not contain prefixed names
      * @param conflictResolverFactory factory for ConflictResolver
      * @param labelPropertiesList list of label properties formatted as a string for use in a query
      * @param globalConfig global conflict resolution settings;
      * values needed in globalConfig are the following:
      * <dl>
      * <dt>maxQueryResultSize
      * <dd>Maximum number of triples returned by each database query (the overall result size may be larger).
      * <dt>resultGraphPrefix
      * <dd>Prefix of named graphs where the resulting triples are placed.
      * </dl>
      */
     protected QueryExecutorBase(JDBCConnectionCredentials connectionCredentials, QueryConstraintSpec constraints,
             AggregationSpec aggregationSpec, ConflictResolverFactory conflictResolverFactory,
             String labelPropertiesList, QueryExecutionConfig globalConfig) {
         this.connectionCredentials = connectionCredentials;
         this.constraints = constraints;
         this.aggregationSpec = aggregationSpec;
         this.conflictResolverFactory = conflictResolverFactory;
         this.globalConfig = globalConfig;
         this.maxLimit = globalConfig.getMaxQueryResultSize();
         this.labelPropertiesList = labelPropertiesList;
     }
 
     /**
      * Returns a database connection.
      * The connection is shared within this instance until it is closed.
      * @return database connection
      * @throws ConnectionException database connection error
      */
     protected VirtuosoConnectionWrapper getConnection() throws ConnectionException {
         if (connection == null) {
             connection = VirtuosoConnectionWrapper.createConnection(connectionCredentials);
         }
         return connection;
     }
 
     /**
      * Closes an opened database connection, if any.
      */
     protected void closeConnectionQuietly() {
         if (connection != null) {
             try {
                 connection.close();
                 connection = null;
             } catch (ConnectionException e) {
                 // do nothing
             }
         }
     }
 
     /**
      * Returns a SPARQL query snippet restricting results to ?graph variable containing a named graph URI to
      * current query constraints. The value is cached.
      * @return SPARQL query snippet
      */
     protected CharSequence getGraphFilterClause() {
         if (graphFilterClause == null) {
             graphFilterClause = buildGraphFilterClause(constraints);
         }
         return graphFilterClause;
     }
 
     /**
      * Check whether aggregation settings and query constraints are valid.
      * For now, checks only compliance with {@link #MAX_PROPERTY_SETTINGS_SIZE}.
      * @throws QueryExecutionException aggregation settings or query constraints are invalid
      */
     protected void checkValidSettings() throws QueryExecutionException {
         // Check that settings contain valid URIs
         for (String property : aggregationSpec.getPropertyAggregations().keySet()) {
             if (!Utils.isValidIRI(property)) {
                 throw new QueryExecutionException(EnumQueryError.AGGREGATION_SETTINGS_INVALID, ErrorCodes.QE_INPUT_FORMAT_ERR,
                         "'" + property + "' is not a valid URI.");
             }
         }
         for (String property : aggregationSpec.getPropertyMultivalue().keySet()) {
             if (!Utils.isValidIRI(property)) {
                 throw new QueryExecutionException(EnumQueryError.AGGREGATION_SETTINGS_INVALID, ErrorCodes.QE_INPUT_FORMAT_ERR,
                         "'" + property + "' is not a valid URI.");
             }
         }
 
         // Check that the size of settings is reasonable - the query size may depend on it
         int settingsPropertyCount = aggregationSpec.getPropertyAggregations().size()
                 + aggregationSpec.getPropertyMultivalue().size();
         if (settingsPropertyCount > MAX_PROPERTY_SETTINGS_SIZE) {
             throw new QueryExecutionException(EnumQueryError.QUERY_TOO_LONG, ErrorCodes.QE_INPUT_FORMAT_ERR,
                     "Too many explicit property settings.");
         }
 
         // Log a warning if using this debug option
         if (GRAPH_PREFIX_FILTER != null) {
             LOG.warn("Query is limited to named graph starting with '{}'", GRAPH_PREFIX_FILTER);
         }
     }
 
     /**
      * Execute the given SPARQL SELECT and constructs a collection of quads from the result.
      * The query must contain four variables in the result, exactly in this order: named graph, subject,
      * property, object
      * @param sparqlQuery a SPARQL SELECT query with four variables in the result: named graph, subject,
      *        property, object (exactly in this order).
      * @param debugName named of the query used for debug log
      * @return result of the query as a collection of quads
      * @throws DatabaseException database error
      */
     protected Collection<Quad> getQuadsFromQuery(String sparqlQuery, String debugName) throws DatabaseException {
         long startTime = System.currentTimeMillis();
         WrappedResultSet resultSet = getConnection().executeSelect(sparqlQuery);
         LOG.debug("Query Execution: {} query took {} ms", debugName, System.currentTimeMillis() - startTime);
 
         try {
             Collection<Quad> quads = new ArrayList<Quad>();
             while (resultSet.next()) {
                 // CHECKSTYLE:OFF
                 Quad quad = new Quad(
                         resultSet.getNode(1),
                         resultSet.getNode(2),
                         resultSet.getNode(3),
                         resultSet.getNode(4));
                 quads.add(quad);
                 // CHECKSTYLE:ON
             }
 
             LOG.debug("Query Execution: {} in {} ms", debugName, System.currentTimeMillis() - startTime);
             return quads;
         } catch (SQLException e) {
             throw new QueryException(e);
         } finally {
             resultSet.closeQuietly();
         }
     }
 
     /**
      * Extract named graph metadata from the result of the given SPARQL SELECT query.
      * The query must contain three variables in the result, exactly in this order: named graph, property, value
      * @param sparqlQuery a SPARQL SELECT query with three variables in the result: resGraph, property, value
      * @param debugName named of the query used for debug log
      * @return map of named graph metadata
      * @throws DatabaseException database error
      */
     protected NamedGraphMetadataMap getMetadataFromQuery(String sparqlQuery, String debugName)
             throws DatabaseException {
 
         final int graphIndex = 1;
         final int propertyIndex = 2;
         final int valueIndex = 3;
         long startTime = System.currentTimeMillis();
         WrappedResultSet resultSet = getConnection().executeSelect(sparqlQuery);
         LOG.debug("Query Execution: {} query took {} ms", debugName, System.currentTimeMillis() - startTime);
 
         // Build the result
         try {
             NamedGraphMetadataMap metadata = new NamedGraphMetadataMap();
             while (resultSet.next()) {
                 String namedGraphURI = resultSet.getString(graphIndex);
                 NamedGraphMetadata graphMetadata = metadata.getMetadata(namedGraphURI);
                 if (graphMetadata == null) {
                     graphMetadata = new NamedGraphMetadata(namedGraphURI);
                     metadata.addMetadata(graphMetadata);
                 }
 
                 try {
                     String property = resultSet.getString(propertyIndex);
 
                     if (ODCS.source.equals(property)) {
                         String object = resultSet.getString(valueIndex);
                         graphMetadata.setSources(addToSetNullProof(object, graphMetadata.getSources()));
                     } else if (ODCS.score.equals(property)) {
                         Double score = resultSet.getDouble(valueIndex);
                         graphMetadata.setScore(score);
                     } else if (ODCS.insertedAt.equals(property)) {
                         Date insertedAt = resultSet.getJavaDate(valueIndex);
                         graphMetadata.setInsertedAt(insertedAt);
                     } else if (ODCS.insertedBy.equals(property)) {
                         String insertedBy = resultSet.getString(valueIndex);
                         graphMetadata.setInsertedBy(insertedBy);
                     } else if (ODCS.publishedBy.equals(property)) {
                         String object = resultSet.getString(valueIndex);
                         graphMetadata.setPublishers(addToListNullProof(object, graphMetadata.getPublishers()));
                     } else if (ODCS.publisherScore.equals(property)) {
                         Double object = resultSet.getDouble(valueIndex);
                         graphMetadata.setPublisherScores(addToListNullProof(object, graphMetadata.getPublisherScores()));
                     } else if (ODCS.license.equals(property)) {
                         String object = resultSet.getString(valueIndex);
                         graphMetadata.setLicences(addToListNullProof(object, graphMetadata.getLicences()));
                     }
                 } catch (SQLException e) {
                     LOG.warn("Query Execution: invalid metadata for graph {}", namedGraphURI);
                 }
             }
             LOG.debug("Query Execution: {} in {} ms", debugName, System.currentTimeMillis() - startTime);
             return metadata;
         } catch (SQLException e) {
             throw new QueryException(e);
         } finally {
             resultSet.closeQuietly();
         }
     }
 
     /**
      * Add a value to the set given in parameter and return modified set; if set is null, create new instance.
      * @param value value to add to the set
      * @param set set to add to or null
      * @return set containing the given value
     * @param <T> item type
      */
     private <T> Set<T> addToSetNullProof(T value, Set<T> set) {
         Set<T> result = set;
         if (result == null) {
             result = new TreeSet<T>();
         }
         result.add(value);
         return result;
     }
 
     /**
      * Add a value to the list given in parameter and return modified list; if list is null, create new instance.
      * @param value value to add to the list
      * @param list list to add to or null
      * @return list containing the given value
     * @param <T> item type
      */
     private <T> List<T> addToListNullProof(T value, List<T> list) {
         final int defaultListSize = 1;
         List<T> result = list;
         if (result == null) {
             result = new ArrayList<T>(defaultListSize);
         }
         result.add(value);
         return result;
     }
 
     /**
      * Retrieves owl:sameAs links of the given URI to all resources that are a synonym
      * (i.e. connected by a owl:sameAs path) of the given URI. The result is added to the second argument as
      * triples having the owl:sameAs predicate.  The maximum length of the owl:sameAs path is given by
      *  {@link #MAX_SAMEAS_PATH_LENGTH} ({@value #MAX_SAMEAS_PATH_LENGTH}).
      * @param uri URI for which we search for owl:sameAs synonyms
      * @param triples the resulting triples are added to this collection as
      * @throws DatabaseException database error
      */
     protected void addSameAsLinksForURI(String uri, Collection<Triple> triples) throws DatabaseException {
         long startTime = System.currentTimeMillis();
         String query = String.format(Locale.ROOT, URI_SYNONYMS_QUERY, uri, MAX_SAMEAS_PATH_LENGTH, maxLimit);
         WrappedResultSet resultSet = getConnection().executeSelect(query);
         LOG.debug("Query Execution: getURISynonyms() query took {} ms", System.currentTimeMillis() - startTime);
         try {
             while (resultSet.next()) {
                 Triple triple = Triple.create(resultSet.getNode(1), SAME_AS_PROPERTY, resultSet.getNode(2));
                 triples.add(triple);
             }
         } catch (SQLException e) {
             throw new QueryException(e);
         } finally {
             resultSet.closeQuietly();
         }
     }
 
     /**
      * Returns preferred URIs for the result based on aggregation settings.
      * These include the properties explicitly listed in aggregation settings.
      * @return preferred URIs
      */
     protected Set<String> getSettingsPreferredURIs() {
         Set<String> aggregationProperties = aggregationSpec.getPropertyAggregations() == null
                 ? Collections.<String>emptySet()
                 : aggregationSpec.getPropertyAggregations().keySet();
         Set<String> multivalueProperties = aggregationSpec.getPropertyMultivalue() == null
                 ? Collections.<String>emptySet()
                 : aggregationSpec.getPropertyMultivalue().keySet();
         Set<String> preferredURIs = new HashSet<String>(
                 aggregationProperties.size() + multivalueProperties.size() + 1); // +1 for URI added in some types of queries
         preferredURIs.addAll(aggregationProperties);
         preferredURIs.addAll(multivalueProperties);
         return preferredURIs;
     }
 }
