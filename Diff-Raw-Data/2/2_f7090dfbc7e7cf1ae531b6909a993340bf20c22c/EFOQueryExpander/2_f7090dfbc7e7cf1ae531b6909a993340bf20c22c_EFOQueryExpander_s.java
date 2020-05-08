 package uk.ac.ebi.arrayexpress.utils.search;
 
 /*
  * Copyright 2009-2010 Microarray Informatics Group, European Bioinformatics Institute
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import uk.ac.ebi.arrayexpress.utils.StringTools;
 import uk.ac.ebi.arrayexpress.utils.saxon.search.IQueryExpander;
 
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 public final class EFOQueryExpander implements IQueryExpander
 {
     // logging machinery
     private final Logger logger = LoggerFactory.getLogger(getClass());
     
     private IEFOExpansionLookup lookup;
 
     public EFOQueryExpander(IEFOExpansionLookup lookup)
     {
         this.lookup = lookup;
     }
 
     public Query expandQuery( Query originalQuery, Map<String, String[]> queryParams )
     {
        boolean shouldExpandEfo = -1 != StringTools.arrayToString(queryParams.get("expandefo"), " ").indexOf("true");
         
         return expand(originalQuery, shouldExpandEfo);
     }
 
     private Query expand( Query query, boolean shouldExpandEfo )
     {
         Query result;
 
         if (query instanceof BooleanQuery) {
             result = new BooleanQuery();
 
             BooleanClause[] clauses = ((BooleanQuery)query).getClauses();
             for (BooleanClause c : clauses) {
                 ((BooleanQuery)result).add(
                         expand(c.getQuery(), shouldExpandEfo)
                         , c.getOccur()
                         );
             }
         } else {
             result = doExpand(query, shouldExpandEfo);
         }
         return result;
     }
 
     private Query doExpand( Query query, boolean shouldExpandEfo )
     {
         String field = getQueryField(query);
         if (null != field && -1 != "keywords sa efv exptype".indexOf(field)) {
 
             shouldExpandEfo = shouldExpandEfo || "exptype".equals(field);   // exptype always expands
 
             EFOExpansionTerms expansionTerms = lookup.getExpansionTerms(query);
             if ((shouldExpandEfo && (0 != expansionTerms.efo.size())) || 0 != expansionTerms.synonyms.size()) {
                 BooleanQuery boolQuery = new BooleanQuery();
 
                 for (String term : expansionTerms.synonyms) {
                     boolQuery.add(newQueryFromString(term.trim(), field), BooleanClause.Occur.SHOULD);
                 }
 
                 if (shouldExpandEfo) {
                     for (String term : expansionTerms.efo) {
                         boolQuery.add(newQueryFromString(term.trim(), field), BooleanClause.Occur.SHOULD);
                     }
                 }
                 return boolQuery;
             }
         }
         return query;
     }
 
     private String getQueryField( Query query )
     {
         String field = null;
         try {
             if (query instanceof PrefixQuery) {
                 field = ((PrefixQuery)query).getPrefix().field();
             } else if (query instanceof WildcardQuery) {
                 field = ((WildcardQuery)query).getTerm().field();
             } else if (query instanceof TermRangeQuery) {
                 field = ((TermRangeQuery)query).getField();
             } else if (query instanceof NumericRangeQuery) {
                 field = ((NumericRangeQuery)query).getField();
             } else if (query instanceof FuzzyQuery) {
                 field = ((FuzzyQuery)query).getTerm().field();
             } else {
                 Set<Term> terms = new HashSet<Term>();
                 query.extractTerms(terms);
                 if (terms.size() > 1) {
                     logger.warn("More than one term found for query [{}]", query.toString());
                 } else if (0 == terms.size()) {
                     logger.error("No terms found for query [{}]", query.toString());
                     return null;
                 }
                 field = ((Term)terms.toArray()[0]).field();
             }
         } catch (UnsupportedOperationException x) {
             logger.error("Query of [{}], class [{}] doesn't allow us to get its terms extracted", query.toString(), query.getClass().getCanonicalName());
         }
 
         return field;
     }
 
     public Query newQueryFromString( String text, String field )
     {
         if (-1 != text.indexOf(" ")) {
             String[] tokens = text.split("\\s+");
             PhraseQuery q = new PhraseQuery();
             for (String token : tokens) {
                 q.add(new Term(field, token));
             }
             return q;
         } else {
             return new TermQuery(new Term(field, text));
         }
     }
 }
