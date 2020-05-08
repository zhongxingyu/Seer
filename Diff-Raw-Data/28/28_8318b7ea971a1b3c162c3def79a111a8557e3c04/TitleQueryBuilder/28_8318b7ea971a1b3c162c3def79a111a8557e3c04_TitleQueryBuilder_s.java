 /* Copyright 2010 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 package org.atlasapi.search.searcher;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.analysis.tokenattributes.TermAttribute;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.DisjunctionMaxQuery;
 import org.apache.lucene.search.FuzzyQuery;
 import org.apache.lucene.search.MultiTermQuery;
 import org.apache.lucene.search.PrefixQuery;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.search.BooleanClause.Occur;
 import org.apache.lucene.util.Version;
 
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 
 public class TitleQueryBuilder {
 
 	private static final Joiner JOINER = Joiner.on("");
 
     private static final int USE_PREFIX_SEARCH_UP_TO = 2;
 
 	private final Map<String, String> EXPANSIONS = ImmutableMap.<String, String>builder()
 	    .put("dr", "doctor")
 	    .put("rd", "road")
 	.build();
 	
 	Query build(String queryString) {	
 		
 		List<String> tokens = tokens(queryString);
 		
 		if (shouldUsePrefixSearch(tokens)) {
 		    String token = Iterables.getOnlyElement(tokens);
 		    DisjunctionMaxQuery query = new DisjunctionMaxQuery(0);
 		    query.add(prefixSearch(token, LuceneContentIndex.FIELD_TITLE_FLATTENED));
 		    query.add(prefixSearch(token, LuceneContentIndex.FIELD_CONTAINER_TITLE_FLATTENED));
 		    return query;
 		} else {
 		    DisjunctionMaxQuery query = new DisjunctionMaxQuery(0);
 		    query.add(fuzzyTermSearch(flatten(queryString), tokens, LuceneContentIndex.FIELD_CONTENT_TITLE, LuceneContentIndex.FIELD_TITLE_FLATTENED, 1.0f));
 		    query.add(fuzzyTermSearch(flatten(queryString), tokens, LuceneContentIndex.FIELD_CONTAINER_CONTENT_TITLE, LuceneContentIndex.FIELD_CONTAINER_TITLE_FLATTENED, 2.0f));
 			return query;
 		}
 	}
 
 	private boolean shouldUsePrefixSearch(List<String> tokens) {
 		return tokens.size() == 1 && Iterables.getOnlyElement(tokens).length() <= USE_PREFIX_SEARCH_UP_TO;
 	}
 
 	private Query prefixSearch(String token, String indexField) {
 	    BooleanQuery withExpansions = new BooleanQuery(true);
 	    withExpansions.setMinimumNumberShouldMatch(1);
 		withExpansions.add(prefixQuery(token, indexField), Occur.SHOULD);
 
 		String expanded = EXPANSIONS.get(token);
 		if (expanded != null) {
 		    withExpansions.add(prefixQuery(expanded, indexField), Occur.SHOULD);
 		}
 	    return withExpansions;
 	}
 
     private PrefixQuery prefixQuery(String token, String indexField) {
         PrefixQuery query = new PrefixQuery(new Term(indexField, token));
         query.setRewriteMethod(MultiTermQuery.CONSTANT_SCORE_FILTER_REWRITE);
         return query;
     }
 
 	private BooleanQuery fuzzyTermSearch(String flattenedQuery, List<String> tokens, String fullTitleIndexField, 
 	        String flattenedTitleIndexField, float boostMultiplier) {
 		BooleanQuery queryForTerms = new BooleanQuery();
 
 		for(String token : tokens) {
 			BooleanQuery queryForThisTerm = new BooleanQuery();
 			queryForThisTerm.setMinimumNumberShouldMatch(1);
 			Term term = new Term(fullTitleIndexField, token);
 			
 			PrefixQuery prefix = new PrefixQuery(term);
 			prefix.setBoost(20 * boostMultiplier);
 			queryForThisTerm.add(prefix, Occur.SHOULD);
 			
 			queryForThisTerm.add(new FuzzyQuery(term, 0.65f, USE_PREFIX_SEARCH_UP_TO),Occur.SHOULD);
 			queryForTerms.add(queryForThisTerm, Occur.MUST);
 		}
 	
 		BooleanQuery either = new BooleanQuery();
 		either.setMinimumNumberShouldMatch(1);
 		either.add(queryForTerms, Occur.SHOULD);
		either.add(fuzzyWithoutSpaces(flattenedQuery, fullTitleIndexField), Occur.SHOULD);
 		
 		Query prefix = prefixSearch(flattenedQuery, flattenedTitleIndexField);
 		prefix.setBoost(50 * boostMultiplier);
 		either.add(prefix, Occur.SHOULD);
 		
 		either.add(exactMatch(flattenedQuery, tokens, fullTitleIndexField, boostMultiplier), Occur.SHOULD);
 		
 		return either;
 	}
 
     private Query exactMatch(String flattenedQuery, Iterable<String> tokens, String fullTitleIndexField, float boostMultiplier) {
         BooleanQuery exactMatch = new BooleanQuery(true);
 		exactMatch.setMinimumNumberShouldMatch(1);
 		exactMatch.add(new TermQuery(new Term(fullTitleIndexField, flattenedQuery)), Occur.SHOULD);
 		
 		Iterable<String> transformed = Iterables.transform(tokens, new Function<String, String>() {
             @Override
             public String apply(String token) {
                 String expanded = EXPANSIONS.get(token);
                 if (expanded != null) {
                     return expanded;
                 }
                 return token;
             }
 		});
 		
 		String flattenedAndExpanded = JOINER.join(transformed);
 		
         if (!flattenedAndExpanded.equals(flattenedQuery)) {
             exactMatch.add(new TermQuery(new Term(fullTitleIndexField, flattenedAndExpanded)), Occur.SHOULD);
         }
 		exactMatch.setBoost(100 * boostMultiplier);
         return exactMatch;
     }
 
 	private FuzzyQuery fuzzyWithoutSpaces(String flattened, String fullTitleIndexField) {
 		return new FuzzyQuery(new Term(fullTitleIndexField, flattened), 0.8f, USE_PREFIX_SEARCH_UP_TO);
 	}
 	
 	private static List<String> tokens(String queryString) {
         TokenStream tokens = new StandardAnalyzer(Version.LUCENE_30).tokenStream("", new StringReader(queryString));
         List<String> tokensAsStrings = Lists.newArrayList();
         try {
             while(tokens.incrementToken()) {
                 TermAttribute token = tokens.getAttribute(TermAttribute.class);
                 tokensAsStrings.add(token.term());
             }
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         return tokensAsStrings;
     }
 
 	public String flatten(String title) {
 		return Joiner.on("").join(tokens(title)).replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
 	}
 }
