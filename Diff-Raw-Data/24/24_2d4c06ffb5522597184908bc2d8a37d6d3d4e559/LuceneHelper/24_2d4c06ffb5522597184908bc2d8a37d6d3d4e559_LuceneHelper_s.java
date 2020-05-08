 /**
  * Copyright 2010 CosmoCode GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.cosmocode.lucene;
 
 import java.util.regex.Pattern;
 
 import de.cosmocode.patterns.Factory;
 
 /**
  * This class provides some static helper methods to format or escape input for Lucene queries.
  * 
  * @since 1.0
  * @author Oliver Lorenz
  */
 public final class LuceneHelper {
     
     
     public static final Factory<LuceneQuery> DEFAULT_FACTORY = 
         new DefaultLuceneQueryFactory(QueryModifier.DEFAULT);
     
     /**
      * A QueryModifier that can be used to include one or more IDs into a search.
      * 
      * @see LuceneQuery#MOD_ID
      */
     public static final QueryModifier MOD_ID = LuceneQuery.MOD_ID;
     
     /**
      * A QueryModifier that can be used to exclude one or more IDs from the search.
      * 
      * @see LuceneQuery#MOD_NOT_ID
      */
     public static final QueryModifier MOD_NOT_ID = LuceneQuery.MOD_NOT_ID;
     
     /**
      * A QueryModifier that can be used for required text fields.
      * 
      * @see LuceneQuery#MOD_TEXT
      */
     public static final QueryModifier MOD_TEXT = LuceneQuery.MOD_TEXT;
     
     /**
      * A QueryModifier that can be used for some autocompletion,
      * though the fuzzyness may vary from project to project.
      * 
      * @see LuceneQuery#MOD_AUTOCOMPLETE
      */
     public static final QueryModifier MOD_AUTOCOMPLETE = LuceneQuery.MOD_AUTOCOMPLETE;
     
     
     /** escape +,\,&,|,!,(,),{,},[,],^,~,?,*,: and blanks with a backslash. */
     public static final Pattern ESCAPE_PATTERN             = Pattern.compile("[\\Q+-\\&|!(){}[]^~?*:; \\E]");
     /** escape +,\,&,|,!,(,),{,},[,],^,~,?,*,:," and blanks with a backslash. */
     public static final Pattern ESCAPE_WITH_QUOTES_PATTERN = Pattern.compile("[\\Q+-\\&|!(){}[]^~?*:; \"\\E]");
     
    public static final Pattern QUOTES_PATTERN             = Pattern.compile("[\"]");
     
     
     private LuceneHelper() {
     }
     
     
     //---------------------------
     //   public helper methods
     //---------------------------
     
     /**
      * Escapes quotes (") in a given input (" => \").
      * <br>Example:
      * <pre>
      *   final String escaped = LuceneHelper.escapeQuotes("test \"wichtig\" blubb");
      *   System.out.println(escaped);  // test \"wichtig\" blubb
      * </pre>
      * @param input the input to escape
      * @return the input with quotes escaped ("\"" => "\\\"")
      */
     public static String escapeQuotes(final String input) {
         if (input == null) return "";
         return QUOTES_PATTERN.matcher(input).replaceAll("\\\\$0");
     }
     
     
     /**
      * Removes quotes (") from a given input.
      * <br>Example:
      * <pre>
      *   final String escaped = LuceneHelper.removeQuotes("test \"wichtig\" blubb");
      *   System.out.println(escaped);  // test wichtig blubb
      * </pre>
      * @param input the input to escape
      * @return the input with quotes removed
      */
     public static String removeQuotes(final String input) {
         if (input == null) return "";
         return QUOTES_PATTERN.matcher(input).replaceAll("");
     }
     
     
     /**
      * <p> Removes the special characters for the Lucene API from a given input String.
      * </p>
      * 
      * @param input the input String to filter
      * @return input without special characters 
      */
     public static String removeSpecialCharacters(final String input) {
         if (input == null) return "";
         return ESCAPE_WITH_QUOTES_PATTERN.matcher(input).replaceAll("");
     }
     
     
     /**
      * Escapes special characters for solr.<br>
      * Special chars are: +,\,&,|,!,(,),{,},[,],^,~,?,*,:,;
      * Blanks are also escaped.<br>
      * Special characters are escaped with "\".<br>
      * <br>
      * This function was taken (with heavy modifications) from
      * http://www.javalobby.org/java/forums/t86124.html
      * @param input the input to escape
      * @return the input, escaped for solr
      */
     public static String escapeInput(final String input) {
         if (input == null) return "";
         return ESCAPE_PATTERN.matcher(input).replaceAll("\\\\$0");
     }
     
     
     /**
      * Escapes all special chars, blanks and quotes for solr.
      * <br>Escaped chars are: +,\,&,|,!,(,),{,},[,],^,~,?,*, ,",:
      * @param input the input to escape
      * @return the input with escaped special chars
      */
     public static String escapeAll(final String input) {
         if (input == null) return "";
         return ESCAPE_WITH_QUOTES_PATTERN.matcher(input).replaceAll("\\\\$0");
     }
     
     
     /**
      * Creates a new default LuceneQuery.
      * The returned LuceneQuery is not threadsafe.
      * @return a new default LuceneQuery
      */
     public static LuceneQuery newQuery() {
         return DEFAULT_FACTORY.create();
     }
     
     /**
      * Creates a new {@link LuceneQueryBuilder}.
      * @return a new LuceneQueryBuilder
      */
     public static LuceneQueryBuilder newQueryBuilder() {
         return new LuceneQueryBuilder();
     }
 
 }
