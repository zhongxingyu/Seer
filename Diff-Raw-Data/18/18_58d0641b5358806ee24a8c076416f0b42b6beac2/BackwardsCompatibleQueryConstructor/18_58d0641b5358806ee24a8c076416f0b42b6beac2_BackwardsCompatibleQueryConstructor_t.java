 package uk.ac.ebi.arrayexpress.utils.search;
 
 import org.apache.lucene.index.Term;
 import org.apache.lucene.queryParser.ParseException;
 import org.apache.lucene.search.*;
 import uk.ac.ebi.arrayexpress.utils.RegexHelper;
 import uk.ac.ebi.arrayexpress.utils.StringTools;
 import uk.ac.ebi.arrayexpress.utils.saxon.search.IQueryConstructor;
 import uk.ac.ebi.arrayexpress.utils.saxon.search.IndexEnvironment;
 import uk.ac.ebi.arrayexpress.utils.saxon.search.QueryConstructor;
 
 import java.util.Map;
 
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
 
 public class BackwardsCompatibleQueryConstructor implements IQueryConstructor
 {
     private QueryConstructor originalConstructor;
     private IndexEnvironment env;
 
    private final RegexHelper LUCENE_SPECIAL_CHARS_REGEX = new RegexHelper("([\\+\\-\\!\\(\\)\\{\\}\\[\\]\\^\\~\\*\\?\\:\\\\]|\\&\\&|\\|\\|)", "ig");
 
     public BackwardsCompatibleQueryConstructor()
     {
         this.originalConstructor = new QueryConstructor();
     }
 
     public IQueryConstructor setEnvironment( IndexEnvironment env )
     {
         this.originalConstructor.setEnvironment(env);
         this.env = env;
         return this;
     }
 
     public Query construct( Map<String, String[]> querySource ) throws ParseException
     {
         if ("1".equals(StringTools.arrayToString(querySource.get("queryversion"), ""))) {
             // preserving old stuff:
             // 1. all lucene special chars to be quoted
             // 2. if "wholewords" is "on" or "true" -> don't add *_*, otherwise add *_*
             BooleanQuery result = new BooleanQuery();
             String wholeWords = StringTools.arrayToString(querySource.get("wholewords"), "");
             boolean useWildcards = !("on".equals(wholeWords) || "true".equals(wholeWords));
             for (Map.Entry<String, String[]> queryItem : querySource.entrySet()) {
                 if (this.env.fields.containsKey(queryItem.getKey()) && queryItem.getValue().length > 0) {
                     for ( String value : queryItem.getValue() ) {
                         if (null != value) {
                             value = value.trim();
                             if (0 != value.length()) {
                                String[] tokens = value.split("\\s+");
                                for (String token : tokens) {
                                    token = LUCENE_SPECIAL_CHARS_REGEX.replace(token, "\\$1");
                                    // we use wildcards for keywords depending on "wholewords" switch,
                                    // *ALWAYS* for other fields, *NEVER* for user id
                                    Query q = !"userid".equals(queryItem.getKey()) && (useWildcards || !"keywords".equals(queryItem.getKey()))
                                            ? new WildcardQuery(new Term(queryItem.getKey(), "*" + token + "*"))
                                            : new TermQuery(new Term(queryItem.getKey(), token));
                                    result.add(q, BooleanClause.Occur.MUST);
                                }
                             }
                         }
 
                     }
                 }
             }
             return result;
         } else {
             return this.originalConstructor.construct(querySource);
         }
     }
 }
