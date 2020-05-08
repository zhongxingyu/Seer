 package cz.cuni.mff.odcleanstore.queryexecution;
 
 import cz.cuni.mff.odcleanstore.configuration.QueryExecutionConfig;
 import cz.cuni.mff.odcleanstore.conflictresolution.AggregationSpec;
 import cz.cuni.mff.odcleanstore.conflictresolution.CRQuad;
 import cz.cuni.mff.odcleanstore.conflictresolution.ConflictResolver;
 import cz.cuni.mff.odcleanstore.conflictresolution.ConflictResolverFactory;
 import cz.cuni.mff.odcleanstore.conflictresolution.NamedGraphMetadataMap;
 import cz.cuni.mff.odcleanstore.conflictresolution.exceptions.ConflictResolutionException;
 import cz.cuni.mff.odcleanstore.connection.JDBCConnectionCredentials;
 import cz.cuni.mff.odcleanstore.connection.exceptions.DatabaseException;
 import cz.cuni.mff.odcleanstore.shared.ErrorCodes;
 import cz.cuni.mff.odcleanstore.vocabulary.ODCS;
 import cz.cuni.mff.odcleanstore.vocabulary.XMLSchema;
 
 import com.hp.hpl.jena.graph.Triple;
 
 import de.fuberlin.wiwiss.ng4j.Quad;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Executes the keyword search query.
  * Triples that contain the given keywords (separated by whitespace) in the object of the triple
  * of type literal are returned.
  *
  * This class is not thread-safe.
  *
  * @author Jan Michelfeit
  */
 /*package*/class KeywordQueryExecutor extends QueryExecutorBase {
     private static final Logger LOG = LoggerFactory.getLogger(KeywordQueryExecutor.class);
 
     /** Maximum allowed length of the query. */
     public static final int MAX_QUERY_LENGTH = 1024;
 
     /**
      * SPARQL query that gets the main result quads.
      * Use of UNION instead of a more complex filter is to make owl:sameAs inference in Virtuoso work.
      * The subquery is necessary to make Virtuoso translate subjects/objects to a single owl:sameAs equivalent.
      * This way we don't need to obtain owl:sameAs links for subjects/objects (passed to ConflictResolverSpec) from
      * the database explicitly.
      *
      * The query must be formatted with these arguments: (1) bif:contains match expressionURI, (2) exact match
      * expression, (3) graph filter clause, (4) limit
      *
      * TODO: Possible optimization - if query doesn't match {@link #NUMERIC_PATTERN} nor {@link #XSD_DATETIME_PATTERN},
      * than the whole exact match part can be dropped (bif:contains will work for both plain literals and xsd:string).
      */
     private static final String KEYWORD_OCCURENCES_QUERY = "SPARQL"
             + "\n DEFINE input:same-as \"yes\""
             + "\n SELECT ?graph ?s ?p ?o"
             + "\n WHERE {"
             + "\n   {"
             + "\n     SELECT DISTINCT ?graph ?s ?p ?o"
             + "\n     WHERE {"
             + "\n       {"
             + "\n         GRAPH ?graph {"
             + "\n           ?s ?p ?o."
             + "\n           ?o bif:contains %1$s"
             // + "\n           OPTION (score ?sc)"
             + "\n         }"
             + "\n       }"
             + "\n       UNION"
             + "\n       {"
             + "\n         GRAPH ?graph {"
             + "\n           ?s ?p ?o."
            // fix of SPARQL compiler error: "sparp_gp_deprecate(): equiv replaces filter but under deprecation"
             + "\n           FILTER (str(?o) = %2$s)"
             + "\n         }"
             + "\n       }"
             + "\n       %3$s"
             + "\n     }"
             + "\n     LIMIT %4$d"
             + "\n   }"
             + "\n }";
     // + "\n ORDER BY DESC(IF(bif:isnull(?sc), 100000, ?sc))";
 
     /**
      * SPARQL query that gets metadata for named graphs containing result quads.
      * Source is the only required value, others can be null.
      * For the reason why UNIONs and subqueries are used, see {@link #URI_OCCURENCES_QUERY}.
      *
      * OPTIONAL clauses for fetching ?graph properties are necessary (probably due to Virtuoso inference processing).
      *
      * Must be formatted with arguments: (1) bif:contains match expressionURI, (2) exact match
      * expression, (3) graph filter clause, (4) label properties, (5) resGraph prefix filter, (6) limit
      */
     private static final String METADATA_QUERY = "SPARQL"
             + "\n DEFINE input:same-as \"yes\""
             + "\n SELECT DISTINCT"
             + "\n   ?resGraph ?p ?o"
             + "\n WHERE {"
             + "\n   {"
             + "\n     {"
             + "\n       SELECT DISTINCT ?graph as ?resGraph"
             + "\n       WHERE {"
             + "\n         {"
             + "\n           GRAPH ?graph {"
             + "\n             ?s ?p ?o."
             + "\n             ?o bif:contains %1$s"
             // + "\n             OPTION (score ?sc)"
             + "\n           }"
             + "\n         }"
             + "\n         UNION"
             + "\n         {"
             + "\n           GRAPH ?graph {"
             + "\n             ?s ?p ?o."
             // fix of SPARQL compiler error: "sparp_gp_deprecate(): equiv replaces filter but under deprecation"
             + "\n             FILTER (str(?o) = %2$s)"
             + "\n           }"
             + "\n         }"
             + "\n         %3$s"
             + "\n       }"
             + "\n       LIMIT %6$d"
             + "\n     }"
             + "\n     UNION"
             + "\n     {"
             + "\n       SELECT DISTINCT ?resGraph"
             + "\n       WHERE {"
             + "\n         {"
             + "\n           SELECT DISTINCT ?r"
             + "\n           WHERE {"
             + "\n             {"
             + "\n               GRAPH ?graph {"
             + "\n                 ?r ?p ?o."
             + "\n                 ?o bif:contains %1$s"
             // + "\n                 OPTION (score ?sc)"
             + "\n               }"
             + "\n             }"
             + "\n             UNION"
             + "\n             {"
             + "\n               GRAPH ?graph {"
             + "\n                 ?r ?p ?o."
             // fix of SPARQL compiler error: "sparp_gp_deprecate(): equiv replaces filter but under deprecation"
             + "\n                 FILTER (str(?o) = %2$s)"
             + "\n               }"
             + "\n             }"
             + "\n             UNION"
             + "\n             {"
             + "\n               GRAPH ?graph {"
             + "\n                 ?s ?r ?o."
             + "\n                 ?o bif:contains %1$s"
             // + "\n                 OPTION (score ?sc)"
             + "\n               }"
             + "\n             }"
             + "\n             UNION"
             + "\n             {"
             + "\n               GRAPH ?graph {"
             + "\n                 ?s ?r ?o."
             // fix of SPARQL compiler error: "sparp_gp_deprecate(): equiv replaces filter but under deprecation"
             + "\n                 FILTER (str(?o) = %2$s)"
             + "\n               }"
             + "\n             }"
             + "\n             %3$s"
             + "\n           }"
             + "\n           LIMIT %6$d"
             + "\n         }"
             + "\n         GRAPH ?resGraph {"
             + "\n           ?r ?labelProp ?label"
             + "\n         }"
             + "\n         FILTER (?labelProp IN (%4$s))"
             + "\n       }"
             + "\n       LIMIT %6$d"
             + "\n     }"
             + "\n   }"
             + "\n   {"
             //+ "\n     {"
             + "\n       ?resGraph <" + ODCS.metadataGraph + "> ?metadataGraph"
             + "\n       GRAPH ?metadataGraph {"
             + "\n         ?resGraph ?p ?o"
             + "\n       }"
             //+ "\n     }"
             //+ "\n     UNION"
             //+ "\n     {"
             //+ "\n       ?resGraph <" + ODCS.publishedBy + "> ?publishedBy."
             //+ "\n       ?publishedBy ?p ?o."
             //+ "\n       FILTER (?p = <" + ODCS.publisherScore + ">)"
             //+ "\n     }"
             + "\n   }"
             + "\n   %5$s"
             //+ "\n   FILTER (bound(?source))"
             + "\n }"
             + "\n LIMIT %6$d";
 
     /**
      * SPARQL query for retrieving labels of resources contained in the result, except for the searched URI
      * (we get that by {@link #URI_OCCURENCES_QUERY}).
      *
      * For the reason why UNIONs and subqueries are used, see {@link #URI_OCCURENCES_QUERY}.
      *
      * Must be formatted with arguments: (1) bif:contains match expressionURI, (2) exact match
      * expression, (3) graph filter clause, (4) label properties, (5) ?labelGraph prefix filter, (6) limit.
      *
      * @see QueryExecutorBase#LABEL_PROPERTIES
      */
     private static final String LABELS_QUERY = "SPARQL"
             + "\n DEFINE input:same-as \"yes\""
             + "\n SELECT ?labelGraph ?r ?labelProp ?label WHERE {{"
             + "\n SELECT DISTINCT ?labelGraph ?r ?labelProp ?label"
             + "\n WHERE {"
             + "\n   {"
             + "\n     SELECT DISTINCT ?graph ?r"
             + "\n     WHERE {"
             + "\n       {"
             + "\n         GRAPH ?graph {"
             + "\n           ?r ?p ?o."
             + "\n           ?o bif:contains %1$s"
             // + "\n           OPTION (score ?sc)"
             + "\n         }"
             + "\n       }"
             + "\n       UNION"
             + "\n       {"
             + "\n         GRAPH ?graph {"
             + "\n           ?r ?p ?o."
            // fix of SPARQL compiler error: "sparp_gp_deprecate(): equiv replaces filter but under deprecation"
             + "\n           FILTER (str(?o) IN (%2$s, %2$s))"
             + "\n         }"
             + "\n       }"
             + "\n       UNION"
             + "\n       {"
             + "\n         GRAPH ?graph {"
             + "\n           ?s ?r ?o."
             + "\n           ?o bif:contains %1$s"
             // + "\n           OPTION (score ?sc)"
             + "\n         }"
             + "\n       }"
             + "\n       UNION"
             + "\n       {"
             + "\n         GRAPH ?graph {"
             + "\n           ?s ?r ?o."
             // fix of SPARQL compiler error: "sparp_gp_deprecate(): equiv replaces filter but under deprecation"
             + "\n           FILTER (str(?o) IN (%2$s, %2$s))"
             + "\n         }"
             + "\n       }"
             + "\n       %3$s"
             + "\n     }"
             + "\n     LIMIT %6$d"
             + "\n   }"
             + "\n   GRAPH ?labelGraph {"
             + "\n     ?r ?labelProp ?label"
             + "\n   }"
             + "\n   FILTER (?labelProp IN (%4$s))"
             + "\n   %5$s"
             + "\n }"
             + "\n LIMIT %6$d"
             + "\n }}";
 
     /**
      * Pattern matching characters removed from the searched keyword for bif:contains match.
      * Must remove single quotes but doesn't remove double quotes since they are used to denote phrases with whitespace.
      * @see #getContainsMatchExpr(String)
      * @see #CONTAINS_FILTER_PATTERN2
      */
     private static final Pattern CONTAINS_FILTER_PATTERN1 = Pattern.compile("[\\x00-\\x09\\x0E-\\x1F'`\\\\]+");
 
     /**
      * Second stage of {@link #CONTAINS_FILTER_PATTERN1}.
      * @see #CONTAINS_FILTER_PATTERN1.
      */
     private static final Pattern CONTAINS_FILTER_PATTERN2 = Pattern.compile("(?<![^\\s\"]{4,})[*]");
 
     /**
      * Pattern matching one keyword in the input query.
      * Keywords are separated by whitespace and/or can be enclosed in double quotes (in that case a keyword may
      * contain whitespace).
      */
     private static final Pattern CONTAINS_KEYWORD_PATTERN = Pattern.compile("\"[^\"]+\"\\s*|[^\"\\s]+\\s*");
 
     /**
      * Pattern matching characters removed from the searched keyword for exact match (equality comparison).
      * Must remove quotes (single and double).
      * @see #getExactMatchExpr(String)
      */
     private static final Pattern EXACT_MATCH_FILTER_PATTERN = Pattern.compile("[\\x00-\\x09\\x0E-\\x1F\"'`\\\\]+");
 
     // CHECKSTYLE:OFF
     /** Pattern matching a valid xsd:dateTime value. */
     private static final Pattern XSD_DATETIME_PATTERN = Pattern.compile("^-?[0-9]{4}-[0-1][0-9]-[0-3][0-9]T[0-2][0-9]:[0-5][0-9]:[0-5][0-9](\\.[0-9]+)?(Z|[+-][0-1][0-9]:[0-5][0-9])?$");
     // CHECKSTYLE:ON
 
     /** Pattern matching a numeric value. */
     private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[+-]?[0-9]*\\.?[0-9]+$");
 
     /** Maximum number of keyword in a contains query. */
     private static final int MAX_CONTAINS_KEYWORDS = 10;
 
     /**
      * Parse the query string to a list of keywords. Double quotes enclosing a phrase are retained.
      * @param keywordsQuery the keyword query
      * @return list of keywords parsed from the query
      */
     private static Collection<String> parseContainedKeywords(String keywordsQuery) {
         String filteredKeywordsQuery = CONTAINS_FILTER_PATTERN1.matcher(keywordsQuery).replaceAll("");
         filteredKeywordsQuery = CONTAINS_FILTER_PATTERN2.matcher(filteredKeywordsQuery).replaceAll("");
         Matcher keywordMatcher = CONTAINS_KEYWORD_PATTERN.matcher(filteredKeywordsQuery);
         final int expectedKeywords = 2;
         ArrayList<String> keywords = new ArrayList<String>(expectedKeywords);
         while (keywordMatcher.find()) {
             String keyword = keywordMatcher.group().trim();
             keywords.add(keyword);
         }
         return keywords;
     }
 
     /**
      * Builds the canonical representation of a query.
      * The result is a concatenation of all parsed keywords by a single space.
      * @param keywords parsed keywords (see {@link #parseContainedKeywords(String)})
      * @return string representation of the query
      */
     private static String buildCanonicalQuery(Collection<String> keywords) {
         if (keywords.isEmpty()) {
             return "";
         }
         StringBuilder result = new StringBuilder();
         for (String keyword : keywords) {
             result.append((keyword));
             result.append(' ');
         }
         return result.substring(0, result.length() - 1).toString();
     }
 
     /**
      * Builds an expression that matches the given keyword(s) in a bif:contains pattern.
      * Returns an empty string if no valid keyword is in keywordsQuery.
      * The result is an expression matching a conjunction of all keywords in keywordsQuery which can be either enclosed
      * in double quotes and/or separated by whitespace.
      *
      * TODO: possible tests: "abc def \"efg\" hij", "abc\"def\"efg", "abc\"def", "a'bc", "\"abc, "abcd*", "abc*",
      * "ab'c*", "ab\"c*"
      *
      * @param keywords parsed keywords (see {@link #parseContainedKeywords(String)})
      * @return an expression that matches the given keyword(s) in a bif:contains pattern
      */
     private static String buildContainsMatchExpr(Collection<String> keywords) {
         if (keywords.isEmpty()) {
             return "";
         }
         StringBuilder expr = new StringBuilder();
         expr.append('\'');
         boolean hasMatch = false;
         int count = 0;
         for (String keyword : keywords) {
             if (++count > MAX_CONTAINS_KEYWORDS) {
                 break;
             }
             if (!hasMatch) {
                 hasMatch = true;
             } else {
                 expr.append(" AND ");
             }
             if (keyword.startsWith("\"")) {
                 assert keyword.length() > 2;
                 expr.append(keyword);
             } else {
                 assert keyword.length() > 0;
                 expr.append('"');
                 expr.append(keyword);
                 expr.append('"');
             }
         }
         expr.append('\'');
         return expr.toString();
     }
 
     /**
      * Returns an expression that equals the whole keywordsQuery, considering a possible type of the literal.
      * If the query looks like a numeric literal or xsd:dateTime literal, returns a typed literal, otherwise returns
      * a quoted string (with quotes inside the query filtered out).
      *
      * If the searched string is not a typed literal, it should still be included in the result by the bif:contains
      * match. The exact match cannot be str(?o) = "..." due to lower performance.
      *
      * @see http://www.w3.org/TR/rdf-sparql-query/#operandDataTypes
      * @param keywordsQuery the keyword query
      * @return a literal value equal to the given keyword, considering type
      */
     private static String buildExactMatchExpr(String keywordsQuery) {
         if (NUMERIC_PATTERN.matcher(keywordsQuery).matches()) {
             return keywordsQuery;
         }
         Matcher dateTimeMatcher = XSD_DATETIME_PATTERN.matcher(keywordsQuery);
         if (dateTimeMatcher.matches()) {
             return (dateTimeMatcher.group(2) != null)
                     ? '"' + keywordsQuery + "\"^^<" + XMLSchema.dateTimeType + '>'
                     : '"' + keywordsQuery + "Z\"^^<" + XMLSchema.dateTimeType + '>'; // Virtuoso won't match without 'Z'
         }
         String query = (EXACT_MATCH_FILTER_PATTERN.matcher(keywordsQuery).replaceAll(""));
         return '"' + query + '"';
     }
 
     /**
      * Creates a new instance of KeywordQueryExecutor.
      * @param connectionCredentials connection settings for the SPARQL endpoint that will be queried
      * @param constraints constraints on triples returned in the result
      * @param aggregationSpec aggregation settings for conflict resolution;
      *        property names must not contain prefixed names
      * @param conflictResolverFactory factory for ConflictResolver
      * @param labelPropertiesList list of label properties formatted as a string for use in a query
      * @param globalConfig global conflict resolution settings
      */
     public KeywordQueryExecutor(JDBCConnectionCredentials connectionCredentials, QueryConstraintSpec constraints,
             AggregationSpec aggregationSpec, ConflictResolverFactory conflictResolverFactory,
             String labelPropertiesList, QueryExecutionConfig globalConfig) {
         super(connectionCredentials, constraints, aggregationSpec, conflictResolverFactory,
                 labelPropertiesList, globalConfig);
     }
 
     /**
      * Executes the keyword search query.
      *
      * @param keywordsQuery searched keywords (separated by whitespace)
      * @return query result holder
      * @throws QueryExecutionException invalid query or database error
      */
     public BasicQueryResult findKeyword(String keywordsQuery) throws QueryExecutionException {
         LOG.info("Keyword query for '{}'", keywordsQuery);
         long startTime = System.currentTimeMillis();
         checkValidSettings();
 
         if (keywordsQuery.length() > MAX_QUERY_LENGTH) {
             throw new QueryExecutionException(
                     EnumQueryError.QUERY_TOO_LONG,
                     ErrorCodes.QE_INPUT_FORMAT_ERR,
                     "The requested keyword query is longer than " + MAX_QUERY_LENGTH + " characters.");
         }
 
         Collection<String> parsedKeywords = parseContainedKeywords(keywordsQuery);
         String canonicalQuery = buildCanonicalQuery(parsedKeywords);
         String containsMatchExpr = buildContainsMatchExpr(parsedKeywords);
         String exactMatchExpr = buildExactMatchExpr(keywordsQuery);
         if (containsMatchExpr.isEmpty() || exactMatchExpr.isEmpty()) {
             // No valid keywords
             return createResult(Collections.<CRQuad>emptyList(), new NamedGraphMetadataMap(), canonicalQuery,
                     System.currentTimeMillis() - startTime);
         }
         try {
             // Get the quads relevant for the query
             Collection<Quad> quads = getKeywordOccurrences(containsMatchExpr, exactMatchExpr);
             if (quads.isEmpty()) {
                 return createResult(Collections.<CRQuad>emptyList(), new NamedGraphMetadataMap(), canonicalQuery,
                         System.currentTimeMillis() - startTime);
             }
             quads.addAll(getLabels(containsMatchExpr, exactMatchExpr));
 
             // Apply conflict resolution
             NamedGraphMetadataMap metadata = getMetadata(containsMatchExpr, exactMatchExpr);
             Iterator<Triple> sameAsLinks = getSameAsLinks().iterator();
             Set<String> preferredURIs = getSettingsPreferredURIs();
             ConflictResolver conflictResolver =
                     conflictResolverFactory.createResolver(aggregationSpec, metadata, sameAsLinks, preferredURIs);
             Collection<CRQuad> resolvedQuads = conflictResolver.resolveConflicts(quads);
 
             return createResult(resolvedQuads, metadata, canonicalQuery, System.currentTimeMillis() - startTime);
         } catch (ConflictResolutionException e) {
             throw new QueryExecutionException(
                     EnumQueryError.CONFLICT_RESOLUTION_ERROR,
                     ErrorCodes.QE_CR_ERR,
                     "Internal error during conflict resolution",
                     e);
         } catch (DatabaseException e) {
             throw new QueryExecutionException(EnumQueryError.DATABASE_ERROR, ErrorCodes.QE_DATABASE_ERR, "Database error", e);
         } finally {
             closeConnectionQuietly();
         }
     }
 
     /**
      * Creates an object holding the results of the query.
      * @param resultQuads result of the query as {@link CRQuad CRQuads}
      * @param metadata provenance metadata for resultQuads
      * @param query the keyword query (as parsed)
      * @param executionTime query execution time in ms
      * @return query result holder
      */
     private BasicQueryResult createResult(
             Collection<CRQuad> resultQuads,
             NamedGraphMetadataMap metadata,
             String query,
             long executionTime) {
 
         LOG.debug("Query Execution: findKeyword() in {} ms", executionTime);
         // Format and return result
         BasicQueryResult queryResult = new BasicQueryResult(resultQuads, metadata, query, EnumQueryType.KEYWORD, constraints,
                 aggregationSpec);
         queryResult.setExecutionTime(executionTime);
         return queryResult;
     }
 
     /**
      * Return a collection of quads relevant for the query (without metadata or any additional quads).
      * @param containsMatchExpr an expression for bif:matches matching the searched keyword(s)
      * @param exactMatchExpr a value matching the searched keyword for equality
      * @return retrieved quads
      * @throws DatabaseException query error
      */
     private Collection<Quad> getKeywordOccurrences(String containsMatchExpr, String exactMatchExpr)
             throws DatabaseException {
         String query = String.format(Locale.ROOT, KEYWORD_OCCURENCES_QUERY, containsMatchExpr, exactMatchExpr,
                 getGraphFilterClause(), maxLimit);
         return getQuadsFromQuery(query, "getKeywordOccurrences()");
     }
 
     /**
      * Return labels of resources returned by {{@link #getKeywordOccurrences(String)} as quads.
      * @param containsMatchExpr an expression for bif:matches matching the searched keyword(s)
      * @param exactMatchExpr a value matching the searched keyword for equality
      * @return labels as quads
      * @throws DatabaseException query error
      */
     private Collection<Quad> getLabels(String containsMatchExpr, String exactMatchExpr) throws DatabaseException {
         return new ArrayList<Quad>();/*
         String query = String.format(Locale.ROOT, LABELS_QUERY, containsMatchExpr, exactMatchExpr,
                 getGraphFilterClause(), labelPropertiesList, getGraphPrefixFilter("labelGraph"),
                 maxLimit);
         return getQuadsFromQuery(query, "getLabels()");*/
     }
 
     /**
      * Return metadata for named graphs containing quads returned in the result.
      * @param containsMatchExpr an expression for bif:matches matching the searched keyword(s)
      * @param exactMatchExpr a value matching the searched keyword for equality
      * @return metadata of result named graphs
      * @throws DatabaseException query error
      */
     private NamedGraphMetadataMap getMetadata(String containsMatchExpr, String exactMatchExpr)
             throws DatabaseException {
         String query = String.format(Locale.ROOT, METADATA_QUERY, containsMatchExpr, exactMatchExpr,
                 getGraphFilterClause(), labelPropertiesList, getGraphPrefixFilter("resGraph"),
                 maxLimit);
         return getMetadataFromQuery(query, "getMetadata()");
     }
 
     /**
      * Returns owl:sameAs links relevant for conflict resolution for this query.
      * Returns only links for properties explicitly listed in aggregation settings;
      * other links (e.g. between subjects/objects in the result) are resolved by Virtuoso.
      * @see #KEYWORD_OCCURENCES_QUERY
      * @return collection of relevant owl:sameAs links
      * @throws DatabaseException query error
      */
     private Collection<Triple> getSameAsLinks() throws DatabaseException {
         long startTime = System.currentTimeMillis();
         Collection<Triple> sameAsTriples = new ArrayList<Triple>();
         assert aggregationSpec.getPropertyAggregations() != null;
         for (String property : aggregationSpec.getPropertyAggregations().keySet()) {
             addSameAsLinksForURI(property, sameAsTriples);
         }
         assert aggregationSpec.getPropertyMultivalue() != null;
         for (String property : aggregationSpec.getPropertyMultivalue().keySet()) {
             addSameAsLinksForURI(property, sameAsTriples);
         }
         LOG.debug("Query Execution: {} in {} ms", "getSameAsLinks()", System.currentTimeMillis() - startTime);
         return sameAsTriples;
     }
 }
