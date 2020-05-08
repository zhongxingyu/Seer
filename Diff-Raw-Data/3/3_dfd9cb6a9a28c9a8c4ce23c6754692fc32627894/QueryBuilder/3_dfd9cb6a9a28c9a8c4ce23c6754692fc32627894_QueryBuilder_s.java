 /*
  * This program is part of Zenoss Core, an open source monitoring platform.
  * Copyright (C) 2010, Zenoss Inc.
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License version 2 as published by
  * the Free Software Foundation.
  *
  * For complete information please visit: http://www.zenoss.com/oss/
  */
 
 package org.zenoss.zep.index.impl;
 
 import com.google.protobuf.ProtocolMessageEnum;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.tokenattributes.TermAttribute;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.index.TermEnum;
 import org.apache.lucene.search.BooleanClause;
 import org.apache.lucene.search.BooleanClause.Occur;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.MultiPhraseQuery;
 import org.apache.lucene.search.NumericRangeQuery;
 import org.apache.lucene.search.PhraseQuery;
 import org.apache.lucene.search.PrefixQuery;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.search.WildcardQuery;
 import org.apache.lucene.search.WildcardTermEnum;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.zenoss.protobufs.util.Util.TimestampRange;
 import org.zenoss.protobufs.zep.Zep.EventDetailFilter;
 import org.zenoss.protobufs.zep.Zep.EventDetailItem;
 import org.zenoss.protobufs.zep.Zep.FilterOperator;
 import org.zenoss.protobufs.zep.Zep.NumberRange;
 import org.zenoss.zep.ZepException;
 import org.zenoss.zep.ZepUtils;
 import org.zenoss.zep.dao.EventDetailsConfigDao;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 public class QueryBuilder {
     private static final Logger logger = LoggerFactory.getLogger(QueryBuilder.class);
     private final BooleanClause.Occur operator;
     private final List<Query> queries = new ArrayList<Query>();
 
     public QueryBuilder() {
         this(FilterOperator.AND);
     }
 
     public QueryBuilder(FilterOperator operator) {
         if (operator == FilterOperator.OR) {
             this.operator = BooleanClause.Occur.SHOULD;
         } else {
             this.operator = BooleanClause.Occur.MUST;
         }
     }
 
     public QueryBuilder addField(String key, String value) {
         queries.add(new TermQuery(new Term(key, value)));
         return this;
     }
 
     private static String unquote(String str) {
         final int len = str.length();
         String unquoted = str;
         if (len >= 2 && str.charAt(0) == '"' && str.charAt(len - 1) == '"') {
             unquoted = str.substring(1, len - 1);
         }
         return unquoted;
     }
 
     /**
      * Remove all occurrences of the character from the end of the string.
      *
      * @param original The original string.
      * @param toRemove The character to remove from the end.
      * @return The modified string.
      */
     private static String removeTrailingChar(String original, char toRemove) {
         int i;
         for (i = original.length() - 1; i >= 0; i--) {
             if (original.charAt(i) != toRemove) {
                 break;
             }
         }
         return original.substring(0, i + 1);
     }
 
     /**
      * Tokenizes the given query using the same behavior as when the field is analyzed.
      *
      * @param fieldName The field name in the index.
      * @param analyzer The analyzer to use to tokenize the query.
      * @param query The query to tokenize.
      * @return The tokens from the query.
      * @throws ZepException If an exception occur.
      */
     private static List<String> getTokens(String fieldName, Analyzer analyzer, String query) throws ZepException {
         final List<String> tokens = new ArrayList<String>();
         TokenStream ts = analyzer.tokenStream(fieldName, new StringReader(query));
         TermAttribute term = ts.addAttribute(TermAttribute.class);
         try {
             while (ts.incrementToken()) {
                 tokens.add(term.term());
             }
             ts.end();
             return tokens;
         } catch (IOException e) {
             throw new ZepException(e.getLocalizedMessage(), e);
         } finally {
             ZepUtils.close(ts);
         }
     }
 
     /**
      * Special case queries for identifier fields. Queries that are enclosed in quotes result in
      * an exact query for the string in the non-analyzed field. Queries that end in an asterisk
      * result in a prefix query in the non-analyzed field. Queries of a length less than
      * the {@link IdentifierAnalyzer#MIN_NGRAM_SIZE} are converted to prefix queries on the
      * non-analyzed field. All other queries are send to the NGram analyzed field for efficient
      * substring matches.
      *
      * @param analyzedFieldName    Analyzed field name.
      * @param nonAnalyzedFieldName Non-analyzed field name.
      * @param values               Queries to search on.
      * @param analyzer             The analyzer used for the fields (used to build the NGram queries).
      * @return This query builder instance (for chaining).
      * @throws ZepException If an exception occurs.
      */
     public QueryBuilder addIdentifierFields(String analyzedFieldName, String nonAnalyzedFieldName,
                                             Collection<String> values, Analyzer analyzer) throws ZepException {
         if (!values.isEmpty()) {
             final BooleanClause.Occur occur = BooleanClause.Occur.SHOULD;
             final BooleanQuery booleanQuery = new BooleanQuery();
 
             for (String value : values) {
                 final Query query;
 
                 // Strip off any trailing *
                 value = removeTrailingChar(value, '*');
                 
                 final String unquoted = unquote(value);
 
                 if (value.isEmpty() || !unquoted.equals(value)) {
                     query = new TermQuery(new Term(nonAnalyzedFieldName, unquoted));
                 } else if (value.length() < IdentifierAnalyzer.MIN_NGRAM_SIZE) {
                     query = new PrefixQuery(new Term(analyzedFieldName, value));
                 } else {
                     final PhraseQuery pq = new PhraseQuery();
                     query = pq;
                     List<String> tokens = getTokens(analyzedFieldName, analyzer, value);
                     for (String token : tokens) {
                         pq.add(new Term(analyzedFieldName, token));
                     }
                 }
                 booleanQuery.add(query, occur);
             }
             queries.add(booleanQuery);
         }
         return this;
     }
 
     private static List<Term> getMatchingTerms(String fieldName, IndexReader reader, String value)
             throws ZepException {
         // Don't search for matches if text doesn't contain wildcards
         if (value.indexOf('*') == -1 && value.indexOf('?') == -1) {
             return Collections.singletonList(new Term(fieldName, value));
         }
 
         logger.debug("getMatchingTerms: field={}, value={}", fieldName, value);
         List<Term> matches = new ArrayList<Term>();
         TermEnum wildcardTermEnum = null;
         try {
             wildcardTermEnum = new WildcardTermEnum(reader, new Term(fieldName, value));
             Term match;
             while ((match = wildcardTermEnum.term()) != null) {
                 logger.debug("Match: {}", match.text());
                 matches.add(match);
                 wildcardTermEnum.next();
             }
             return matches;
         } catch (IOException e) {
             throw new ZepException(e.getLocalizedMessage(), e);
         } finally {
             ZepUtils.close(wildcardTermEnum);
         }
     }
 
     /**
      * Special case queries for event classes. Queries that begin with a slash and end
      * with a slash or an asterisk result in a starts-with query on the non-analyzed
      * field name. Queries that begin with a slash and end with anything else are an
      * exact match on the non-analyzed field name. Otherwise, the query is run through
      * the analyzer and substring matching is performed.
      *
      * @param analyzedFieldName    Analyzed field name.
      * @param nonAnalyzedFieldName Non-analyzed field name.
      * @param values               Queries to search on.
      * @param analyzer             The analyzer used for the fields.
      * @param reader               The reader (used to query terms).
      * @return This query builder instance (for chaining).
      * @throws ZepException If an exception occurs.
      */
     public QueryBuilder addEventClassFields(String analyzedFieldName, String nonAnalyzedFieldName,
                                             Collection<String> values, Analyzer analyzer,
                                             IndexReader reader) throws ZepException {
         if (!values.isEmpty()) {
             final BooleanClause.Occur occur = BooleanClause.Occur.SHOULD;
             final BooleanQuery booleanQuery = new BooleanQuery();
 
             for (String value : values) {
                 final Query query;
                 if (value.startsWith("/")) {
                     value = value.toLowerCase();
                     // Starts-with
                     if (value.endsWith("/")) {
                         query = new PrefixQuery(new Term(nonAnalyzedFieldName, value));
                     }
                     // Prefix
                     else if (value.endsWith("*")) {
                         value = value.substring(0, value.length() - 1);
                         query = new PrefixQuery(new Term(nonAnalyzedFieldName, value));
                     }
                     // Exact match
                     else {
                         query = new TermQuery(new Term(nonAnalyzedFieldName, value + "/"));
                     }
                 } else {
                     final MultiPhraseQuery pq = new MultiPhraseQuery();
                     query = pq;
 
                     List<String> tokens = getTokens(analyzedFieldName, analyzer, value);
                     for (String token : tokens) {
                         List<Term> terms = getMatchingTerms(analyzedFieldName, reader, token);
                         // Ensure we don't return results if term doesn't exist
                         if (terms.isEmpty()) {
                             pq.add(new Term(analyzedFieldName, token));
                         }
                         else {
                             pq.add(terms.toArray(new Term[terms.size()]));
                         }
                     }
                 }
                 booleanQuery.add(query, occur);
             }
             queries.add(booleanQuery);
         }
         return this;
     }
 
     public QueryBuilder addWildcardFields(String key, Collection<String> values, boolean lowerCase) {
         if (!values.isEmpty()) {
             final BooleanClause.Occur occur = BooleanClause.Occur.SHOULD;
             final BooleanQuery booleanQuery = new BooleanQuery();
 
             for (String value : values) {
                 if (lowerCase) {
                     value = value.toLowerCase();
                 }
                 booleanQuery.add(new WildcardQuery(new Term(key, value)), occur);
             }
             queries.add(booleanQuery);
         }
         return this;
     }
 
     public QueryBuilder addFullTextFields(String fieldName, Collection<String> values, IndexReader reader,
                                           Analyzer analyzer) throws ZepException {
         if (values.isEmpty()) {
             return this;
         }
 
         BooleanQuery outerQuery = new BooleanQuery(); // SHOULD
         for (String value : values) {
             // If we encounter a term which starts with a quote, start a new multi-phrase query.
             // If we are already in a multi-phrase query, add the term to the query (accounting
             // for prefix queries). When we encounter a term which ends with a quote, end the
             // multi-phrase query. All queries not enclosed in quotes are treated as normal
             // prefix or term queries.
             BooleanQuery innerQuery = new BooleanQuery(); // MUST
             MultiPhraseQuery pq = null;
             
             List<String> tokens = getTokens(fieldName, analyzer, value);
             for (String token : tokens) {
                 final boolean startsWithQuote = token.startsWith("\"");
 
                 if (startsWithQuote && pq == null) {
                     token = token.substring(1);
                     if (token.isEmpty()) {
                         continue;
                     }
                     pq = new MultiPhraseQuery();
                 }
 
                 if (pq == null) {
                     // We aren't in quoted string - just add as query on field
                     innerQuery.add(new WildcardQuery(new Term(fieldName, token)), Occur.MUST);
                 }
                 else {
                     final boolean endsWithQuote = token.endsWith("\"");
                     // Remove trailing quote
                     if (endsWithQuote) {
                         token = token.substring(0, token.length() - 1);
                     }
                     List<Term> terms = getMatchingTerms(fieldName, reader, token);
                     // Ensure we don't return results if term doesn't exist
                     if (terms.isEmpty()) {
                         pq.add(new Term(fieldName, token));
                     }
                     else {
                         pq.add(terms.toArray(new Term[terms.size()]));
                     }
                     if (endsWithQuote) {
                         innerQuery.add(pq, Occur.MUST);
                         pq = null;
                     }
                 }
             }
 
             // This could happen if phrase query is not terminated above (i.e.
             // live search with query '"quick brown dog'. This shouldn't be an
             // error to allow live searches to work as expected.
             if (pq != null) {
                 innerQuery.add(pq, Occur.MUST);
             }
 
             int numClauses = innerQuery.clauses().size();
             if (numClauses == 1) {
                 outerQuery.add(innerQuery.clauses().get(0).getQuery(), Occur.SHOULD);
             }
             else if (numClauses > 1) {
                 outerQuery.add(innerQuery, Occur.SHOULD);
             }
         }
 
         int numClauses = outerQuery.clauses().size();
         if (numClauses == 1) {
             this.queries.add(outerQuery.clauses().get(0).getQuery());
         }
         else if (numClauses > 1) {
             this.queries.add(outerQuery);
         }
         return this;
     }
 
     public QueryBuilder addField(String key, List<String> values) throws ZepException {
         return addField(key, values, FilterOperator.OR);
     }
 
     public QueryBuilder addField(String key, List<String> values, FilterOperator op) {
         if (!values.isEmpty()) {
             final BooleanClause.Occur occur;
             final BooleanQuery booleanQuery = new BooleanQuery();
             if (op == FilterOperator.AND) {
                 occur = BooleanClause.Occur.MUST;
             } else {
                 occur = BooleanClause.Occur.SHOULD;
             }
 
             for (String value : values) {
                 booleanQuery.add(new TermQuery(new Term(key, value)), occur);
             }
             queries.add(booleanQuery);
         }
         return this;
     }
 
     public QueryBuilder addFieldOfIntegers(String key, List<Integer> values) {
         if (!values.isEmpty()) {
             final BooleanClause.Occur occur = BooleanClause.Occur.SHOULD;
             final BooleanQuery booleanQuery = new BooleanQuery();
 
             // Condense adjacent values into one range
             Collections.sort(values);
             Iterator<Integer> it = values.iterator();
             int from = it.next();
             int to = from;
             while (it.hasNext()) {
                 int value = it.next();
                 if (value == to + 1) {
                     to = value;
                 } else {
                     booleanQuery.add(NumericRangeQuery.newIntRange(key, from, to, true, true), occur);
                     from = to = value;
                 }
             }
             booleanQuery.add(NumericRangeQuery.newIntRange(key, from, to, true, true), occur);
 
             queries.add(booleanQuery);
         }
         return this;
     }
 
     public QueryBuilder addFieldOfEnumNumbers(String key, List<? extends ProtocolMessageEnum> values) throws ZepException {
         List<Integer> valuesList = new ArrayList<Integer>(values.size());
         for (ProtocolMessageEnum e : values) {
             valuesList.add(e.getNumber());
         }
         addFieldOfIntegers(key, valuesList);
         return this;
     }
 
     public QueryBuilder addRange(String key, Integer from, Integer to) {
         this.queries.add(NumericRangeQuery.newIntRange(key, from, to, true, true));
         return this;
     }
     public QueryBuilder addRange(String key, Long from, Long to) {
         this.queries.add(NumericRangeQuery.newLongRange(key, from, to, true, true));
         return this;
     }
 
     public QueryBuilder addRange(String key, Float from, Float to) {
         this.queries.add(NumericRangeQuery.newFloatRange(key, from, to, true, true));
         return this;
     }
 
     public QueryBuilder addRange(String key, Double from, Double to) {
         this.queries.add(NumericRangeQuery.newDoubleRange(key, from, to, true, true));
         return this;
     }
 
     public QueryBuilder addTimestampRanges(String key, List<TimestampRange> ranges) {
         if (!ranges.isEmpty()) {
             final BooleanClause.Occur occur = BooleanClause.Occur.SHOULD;
             final BooleanQuery booleanQuery = new BooleanQuery();
 
             Long from = null, to = null;
             for (TimestampRange range : ranges) {
                 if (range.hasStartTime()) {
                     from = range.getStartTime();
                 }
                 if (range.hasEndTime()) {
                     to = range.getEndTime();
                 }
                 booleanQuery.add(NumericRangeQuery.newLongRange(key, from, to, true, true), occur);
             }
             this.queries.add(booleanQuery);
         }
         return this;
     }
 
     public QueryBuilder addRanges(String key, Collection<NumberRange> ranges) throws ZepException {
         if (!ranges.isEmpty()) {
             final BooleanClause.Occur occur = BooleanClause.Occur.SHOULD;
             final BooleanQuery booleanQuery = new BooleanQuery();
 
             for (NumberRange range : ranges) {
                 Integer from = null, to = null;
                 if (range.hasFrom()) {
                     Long f = range.getFrom();
                     if (f < Integer.MIN_VALUE) {
                         throw new ZepException("NumberRange value 'from' must be an integer.");
                     }
                     from = f.intValue();
                 }
                 if (range.hasTo()) {
                     Long t = range.getTo();
                     if (t > Integer.MAX_VALUE) {
                         throw new ZepException("NumberRange value 'to' must be an integer.");
                     }
                     to = t.intValue();
                 }
                 booleanQuery.add(NumericRangeQuery.newIntRange(key, from, to, true, true), occur);
             }
             queries.add(booleanQuery);
         }
         return this;
     }
 
     public QueryBuilder addSubquery(Query subquery) throws ZepException {
         queries.add(subquery);
         return this;
     }
 
 
     public QueryBuilder addDetails(Collection<EventDetailFilter> filters, Map<String,EventDetailItem> detailsConfig)
             throws ZepException {
         if (!filters.isEmpty()) {
             for (EventDetailFilter edf : filters) {
                 QueryBuilder eventDetailQuery = new QueryBuilder(edf.getOp());
                 EventDetailItem detailConfig = detailsConfig.get(edf.getKey());
 
                 if (detailConfig == null) {
                     throw new ZepException("Event detail is not indexed: " + edf.getKey());
                 }
 
                 String key = EventIndexMapper.DETAIL_INDEX_PREFIX + detailConfig.getKey();
                 switch(detailConfig.getType()) {
                     case STRING:
                         eventDetailQuery.addWildcardFields(key, edf.getValueList(), Boolean.FALSE);
                         break;
 
                     case INTEGER:
                         for (String val : edf.getValueList()) {
                             NumericValueHolder<Integer> valueHolder = parseNumericValue(val, Integer.class);
                             eventDetailQuery.addRange(key, valueHolder.from, valueHolder.to);
                         }
                         break;
 
                     case FLOAT:
                         for (String val : edf.getValueList()) {
                             NumericValueHolder<Float> valueHolder = parseNumericValue(val, Float.class);
                             eventDetailQuery.addRange(key, valueHolder.from, valueHolder.to);
                         }
                         break;
 
                     case LONG:
                         for (String val : edf.getValueList()) {
                             NumericValueHolder<Long> valueHolder = parseNumericValue(val, Long.class);
                             eventDetailQuery.addRange(key, valueHolder.from, valueHolder.to);
                         }
                         break;
 
                     case DOUBLE:
                         for (String val : edf.getValueList()) {
                             NumericValueHolder<Double> valueHolder = parseNumericValue(val, Double.class);
                             eventDetailQuery.addRange(key, valueHolder.from, valueHolder.to);
                         }
                         break;
 
                     default:
                         throw new ZepException("Unsupported detail type: " + detailConfig.getType());
                 }
                 
                 if (!eventDetailQuery.queries.isEmpty()) {
                     this.addSubquery(eventDetailQuery.build());
                 }
             }
         }
         return this;
     }
 
     private static <T extends Number> NumericValueHolder<T> parseNumericValue(String value, Class<T> clazz)
             throws ZepException {
         if (value.isEmpty()) {
             throw new ZepException("Empty numeric value");
         }
         String strLeft, strRight;
         int colonIndex = value.indexOf(':');
 
         // any ':' means this is a range of some kind.
         if (colonIndex == -1) {
             strLeft = strRight = value;
         }
         else {
             strLeft = value.substring(0, colonIndex);
             strRight = value.substring(colonIndex + 1);
            if (strLeft.isEmpty() && strRight.isEmpty()) {
                throw new ZepException("Invalid numeric range: " + value);
            }
         }
         return new NumericValueHolder<T>(convertString(strLeft, clazz), convertString(strRight, clazz));
     }
 
     @SuppressWarnings({"unchecked"})
     private static <T extends Number> T convertString(String strVal, Class<T> clazz) {
         if (strVal == null || strVal.isEmpty()) {
             return null;
         }
         try {
             Method valueOf = clazz.getMethod("valueOf", String.class);
             return (T) valueOf.invoke(null, strVal);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     private static class NumericValueHolder<T extends Number> {
         public T from = null;
         public T to = null;
 
         public NumericValueHolder(T from, T to) {
             this.from = from;
             this.to = to;
         }
 
         @Override
         public String toString() {
             return "NumericValueHolder{" +
                     "from=" + from +
                     ", to=" + to +
                     '}';
         }
     }
 
     public BooleanQuery build() {
         if (this.queries.isEmpty()) {
             return null;
         }
 
         BooleanQuery booleanQuery = new BooleanQuery();
         for (Query query : this.queries) {
             booleanQuery.add(query, this.operator);
         }
         this.queries.clear();
         return booleanQuery;
     }
 
     @Override
     public String toString() {
         return "QueryBuilder{" +
                 "queries=" + queries +
                 '}';
     }
 
 }
